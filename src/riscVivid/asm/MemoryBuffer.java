/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * (C)opyright 2013-2016 The riscVivid project, University of Augsburg, Germany
 * https://github.com/unia-sik/riscVivid
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, see <LICENSE>. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package riscVivid.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import riscVivid.gui.Preference;

/**
 * This class manages a resizable byteArray and supports methods for writing and
 * reading bytes(8bit),half words(16bit) and words(32bit). There is support for
 * little endian mode and a marker for the entry point.
 * 
 */
public class MemoryBuffer {

	private static final int LINE_WRAPPING = 0x10;
	private static final int INITIAL_SIZE = 0xFF;

	private byte[] byteArray;
	private boolean littleEndian;
	private int entryPoint;
	
	private ArrayList<MemSegment> dataSegments = new ArrayList<MemSegment>();
	private ArrayList<MemSegment> textSegments = new ArrayList<MemSegment>();
	// current*Segment contains the index of the segment that was created most recently
	// by a call of set*Begin()
	int currentDataSegment = -1;
	int currentTextSegment = -1;

	/**
	 * create new MemoryBuffer with INITIAL_SIZE size and little endian mode
	 */
	public MemoryBuffer() {
		this(INITIAL_SIZE);
	}

	/**
	 * create new MemoryBuffer with initSize size but at least 4 and little
	 * endian mode
	 * 
	 * @param initSize
	 */
	public MemoryBuffer(int initSize) {
		if (initSize < 4)
			initSize = 4;
		byteArray = new byte[initSize];
		littleEndian = true;
		entryPoint = 0;
		
		// initialize memory
		int init = Preference.pref.getInt(Preference.initializeOptionPreferenceKey, 0);
		boolean random = init > 0xff || init < 0;

		Random rand = new Random();
		for (int i = 0; i < initSize; ++i) {
		    byteArray[i] = random ? (byte)rand.nextInt(256) : (byte)init;
		}
	}

	/*
	 * ============================* Getter/Setter *============================
	 */
	/**
	 * 
	 * @return true if in little endian mode else false
	 */
	public boolean isLittleEndian() {
		return littleEndian;
	}

	/**
	 * set little endian mode to true or false
	 * 
	 * @param littleEndian
	 */
	public void setLittleEndian(boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	/**
	 * 
	 * @return position of first instruction that should be executed
	 */
	public int getEntryPoint() {
		return entryPoint;
	}

	/**
	 * set position of first instruction that should be executed
	 * 
	 * @param entryPoint
	 */
	public void setEntyPoint(int entryPoint) {
		if (entryPoint < 0)
			entryPoint = 0;
		this.entryPoint = entryPoint;
	}
	
	public int getTextBegin(int segment) {
		return textSegments.get(segment).begin;
	}
	public int getTextEnd(int segment) {
		return textSegments.get(segment).end;
	}
	public int getDataBegin(int segment) {
		return dataSegments.get(segment).begin;
	}
	public int getDataEnd(int segment) {
		return dataSegments.get(segment).end;
	}

	public int getNumTextSegments() {
		return textSegments.size(); 
	}
	public int getNumDataSegments() {
		return dataSegments.size();
	}
	/**
	 * set begin of a new data segment
	 * 
	 * @param dataBegin
	 */
	public void setDataBegin(int dataBegin) {
		if (dataBegin < 0)
			dataBegin = 0;
		// remove currentDataSegment if setDataEnd() has never been called
		if (currentDataSegment >= 0)
			if (dataSegments.get(currentDataSegment).begin ==
					dataSegments.get(currentDataSegment).end)
				dataSegments.remove(currentDataSegment);
		// find position of the new segment in the list so that the list is sorted by MemSegment.begin
		int idx;
		for (idx = 0; idx < dataSegments.size(); ++idx) {
			if (dataBegin < dataSegments.get(idx).begin)
				break;
		}
		dataSegments.add(idx, new MemSegment(dataBegin, dataBegin));
		currentDataSegment = idx;
	}

	/**
	 * set begin of a new text segment
	 * 
	 * @param textBegin
	 */
	public void setTextBegin(int textBegin) {
		if (textBegin < 0)
			textBegin = 0;
		// remove currentTextSegment if setTextEnd() has never been called
		if (currentTextSegment >= 0)
			if (textSegments.get(currentTextSegment).begin ==
					textSegments.get(currentTextSegment).end)
				textSegments.remove(currentTextSegment);
		// find position of the new segment in the list so that the list is sorted by MemSegment.begin
		int idx;
		for (idx = 0; idx < textSegments.size(); ++idx) {
			if (textBegin < textSegments.get(idx).begin)
				break;
		}
		textSegments.add(idx, new MemSegment(textBegin, textBegin));
		currentTextSegment = idx;
		// fill text segment with bubbles: everything after textBegin should be zero
		// Needed because the simulator reads some (bubble-)instructions after the last instruction
		Arrays.fill(byteArray, textBegin, byteArray.length, (byte)0);
	}

	/**
	 * Set the end pointer of the data section.
	 * The end pointer can only be increased. So the parameter is only set, if it is larger than the current end pointer of the data section.
	 * 
	 * @param dataEnd textEnd is only set, if it is larger than the current end pointer of the data section.
	 */
	public void setDataEnd(int dataEnd) {
		if (dataEnd > dataSegments.get(currentDataSegment).end)
			dataSegments.get(currentDataSegment).end = dataEnd;
	}

	/**
	 * Set the end pointer of the text section.
	 * The end pointer can only be increased. So the parameter is only set, if it is larger than the current end pointer of the text section.
	 * 
	 * @param textEnd textEnd is only set, if it is larger than the current end pointer of the text section.
	 */
	public void setTextEnd(int textEnd) {
		if (textSegments.get(currentTextSegment).end < textEnd)
			textSegments.get(currentTextSegment).end = textEnd;
	}

	/**
	 * Tests if some segments overlap
	 * @return "" empty string if no segments overlap,
	 * 		in case of an overlap: a string informing about the two segments
	 */
	public String segmentsOverlap() {
		ArrayList<MemSegment> segments = new ArrayList<MemSegment>(dataSegments);
		// sort textSegments into sorted list of dataSegments
		int textIdx = 0;
		// sort textSegments into list of dataSegments
		for (int i = 0; textIdx < textSegments.size() && i < segments.size(); ++i) {
			if (textSegments.get(textIdx).begin < segments.get(i).begin) {
				segments.add(i, textSegments.get(textIdx));
				textIdx++;
			}
		}
		// add remaining textSegments to the end of the list
		for (;textIdx < textSegments.size(); ++textIdx)
			segments.add(textSegments.get(textIdx));

		// test if two subsequent segments overlap
		for (int i = 1; i < segments.size(); ++i) {
			MemSegment first = segments.get(i-1), second = segments.get(i);
		
			// test if second segment starts within the first segment
			// second.begin < first.end instead of <= because end is exclusive
			if (first.begin <= second.begin && second.begin < first.end) {
				// Create String about the type of segments (data/text) and their start and ends
				String s = (dataSegments.contains(first) ? "data segment " : "text segment ") + 
						"0x" + Integer.toHexString(first.begin) + " - 0x" + 
						Integer.toHexString(first.end) + " and " +
						(dataSegments.contains(second) ? "data segment " : "text segment ") + 
						"0x" + Integer.toHexString(second.begin) + " - 0x" + 
						Integer.toHexString(second.end) + " overlap.";
				return s;
			}
		}
		return "";
	}
	
	/**
	 * checks all data segment if it contains the address
	 */
	public boolean isInDataSegment(int address) {
		for (MemSegment s : dataSegments) {
			if (s.begin <= address && address < s.end)
				return true;
		}
		return false;
	}
	/**
	 * 
	 * @return size of buffer
	 */
	public int size() {
		return byteArray.length;
	}

	/*
	 * =============================* Read/Write *=============================
	 */

	/**
	 * read byte at position
	 * 
	 * @param position
	 * @return byte at position
	 */
	public byte readByte(int position) {
		return byteArray[position];
	}

	/**
	 * write byte to position
	 * 
	 * @param position
	 * @param value
	 */
	public void writeByte(int position, byte value) {
		if (position >= byteArray.length) {
			reserve(2 * (position + 4 - position % 4));
		}
		byteArray[position] = value;
	}

	/**
	 * read half word at position
	 * 
	 * @param position
	 * @return half word at position
	 */
	public short readHalf(int position) {
		//little bit complicated because of sign extending when casting
		int value;
		if (littleEndian) {
			value = ((int) readByte(position++) & 0xFF);
			value += ((int) readByte(position) & 0xFF) << 8;
		} else {
			value = ((int) readByte(position++) & 0xFF) << 8;
			value += ((int) readByte(position) & 0xFF);
		}
		return (short) value;
	}

	/**
	 * write half word to position
	 * 
	 * @param position
	 * @param value
	 */
	public void writeHalf(int position, short value) {
		if (littleEndian) {
			writeByte(position, (byte) (value & 0xFF));
			writeByte(position + 1, (byte) ((value >> 8) & 0xFF));
		} else {
			writeByte(position + 1, (byte) (value & 0xFF));
			writeByte(position, (byte) ((value >> 8) & 0xFF));
		}
	}

	/**
	 * read word at position
	 * 
	 * @param position
	 * @return byte at position
	 */
	public int readWord(int position) {
		//little bit complicated because of sign extending when casting
		int value;
		if (littleEndian) {
			value = ((int) readByte(position++) & 0xFF);
			value += ((int) readByte(position++) & 0xFF) << 8;
			value += ((int) readByte(position++) & 0xFF) << 16;
			value += ((int) readByte(position) & 0xFF) << 24;
		} else {
			value = ((int) readByte(position++) & 0xFF) << 24;
			value += ((int) readByte(position++) & 0xFF) << 16;
			value += ((int) readByte(position++) & 0xFF) << 8;
			value += ((int) readByte(position) & 0xFF);
		}
		return value;
	}

	/**
	 * write word to position
	 * 
	 * @param position
	 * @param value
	 */
	public void writeWord(int position, int value) {
		if (littleEndian) {
			writeByte(position, (byte) (value & 0xFF));
			writeByte(position + 1, (byte) ((value >> 8) & 0xFF));
			writeByte(position + 2, (byte) ((value >> 16) & 0xFF));
			writeByte(position + 3, (byte) ((value >> 24) & 0xFF));
		} else {
			writeByte(position + 3, (byte) (value & 0xFF));
			writeByte(position + 2, (byte) ((value >> 8) & 0xFF));
			writeByte(position + 1, (byte) ((value >> 16) & 0xFF));
			writeByte(position, (byte) ((value >> 24) & 0xFF));
		}
	}

	/*
	 * =============================* Conversions *=============================
	 */
	/**
	 * 
	 * @return byte array copy
	 */
	public byte[] toByteArray() {
		byte[] tmp = new byte[byteArray.length];
		System.arraycopy(byteArray, 0, tmp, 0, byteArray.length);
		return tmp;
	}

	/**
	 * After LINE_WRAPPING bytes a newline is set. A byte count is written at
	 * the beginning of each line.
	 * 
	 * @return string representation of memoryBuffer
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer(4 * this.size());
		strBuf.append("entryPoint: 0x" + Integer.toHexString(getEntryPoint()) + "\n");
		strBuf.append("dataBegin: 0x" + Integer.toHexString(getDataBegin(0)) + "\n");
		strBuf.append("textBegin: 0x" + Integer.toHexString(getTextBegin(0)) + "\n");
		for (int i = 0; i < size(); i++) {
			if (i % LINE_WRAPPING == 0) {
				strBuf.append("\n" + String.format("%1$04x", i) + ":");
			}
			strBuf.append(String.format(" %1$02x", byteArray[i]));
		}
		return strBuf.toString();
	}

	/*
	 * ==============================* Internals *==============================
	 */
	private void reserve(int size) {
		if (size > byteArray.length) {
			byte[] tmp = new byte[size];
			System.arraycopy(byteArray, 0, tmp, 0, byteArray.length);
			byteArray = tmp;
		}
	}
	
	private class MemSegment {
		// begin is inclusive, end is exclusive
		public int begin, end;
		
		public MemSegment(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}
	}
}
