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
package riscVivid;

import java.util.Queue;


import org.apache.log4j.Logger;

import riscVivid.datatypes.*;
import riscVivid.exception.MemoryException;
import riscVivid.exception.MemoryStageException;
import riscVivid.exception.PipelineException;
import riscVivid.exception.UnreservedMemoryAccessException;
import riscVivid.gui.Preference;
import riscVivid.memory.DataMemory;
import riscVivid.util.Statistics;

public class Memory
{
	private static Logger logger = Logger.getLogger("MEMORY");
	private Statistics stat = Statistics.getInstance(); 
	private DataMemory dmem;
	private Queue<ExecuteMemoryData> execute_memory_latch;


	public Memory(DataMemory dmem)
	{
		this.dmem = dmem;
	}

	public void setInputLatch(Queue<ExecuteMemoryData> executeMemoryLatch)
	{
		execute_memory_latch = executeMemoryLatch;
	}

	public MemoryOutputData doCycle() throws MemoryStageException, MemoryException
	{
		ExecuteMemoryData emd = execute_memory_latch.element();
		uint32[] alu_out = emd.getAluOut();
		uint32 alu_outLO = alu_out[0];
		// uint32 alu_outHI = alu_out[1];
//		uint32 store_value = emd.getStoreValue();
		uint64 sv = new uint64(emd.getStoreValue().getValue());
		Instruction inst = emd.getInst();
		uint32 pc = emd.getPc();
		boolean jump = emd.getJump();

		long lv=0;
		try {
			if (inst.getLoad()) {
				if (dmem.getRequestDelay(RequestType.DATA_RD, alu_outLO) == 0) {
					switch (inst.getMemoryWidth()) {
						case BYTE:
							lv = dmem.read_u8(alu_outLO, true).getValue();
							break;
						case UBYTE:
							lv = (long) dmem.read_u8(alu_outLO, true).getValue() & 0xff;
							break;
						case HWORD:
							lv = dmem.read_u16(alu_outLO, true).getValue();
							break;
						case UHWORD:
							lv = (long) dmem.read_u16(alu_outLO, true).getValue() & 0xffff;
							break;
						case WORD:
							lv = dmem.read_u32(alu_outLO, true).getValue();
							break;
						case UWORD:
							lv = (long) dmem.read_u32(alu_outLO, true).getValue() & 0xffffffffL;
							break;
						case DWORD:
						case UDWORD:
							lv = dmem.read_u64(alu_outLO, true).getValue();
							break;
						default:
							logger.error("wrong memory width: " + inst.getMemoryWidth());
							throw new MemoryStageException("Wrong memory width: " + inst.getMemoryWidth(), pc);
					}
					logger.debug("PC: " + pc.getValueAsHexString()
							+ " load from addr: " + alu_outLO.getValueAsHexString()
							+ " value: 0x" + Long.toHexString(lv));
					stat.countMemRead();
				}
				// else stall
			} else if (inst.getStore()) {
				if (dmem.getRequestDelay(RequestType.DATA_WR, alu_outLO) == 0) {
					logger.debug("PC: " + pc.getValueAsHexString()
							+ " store value: " + sv.getValueAsHexString()
							+ " to addr: " + alu_outLO.getValueAsHexString());
					switch (inst.getMemoryWidth()) {
						case BYTE:
						case UBYTE:
							dmem.write_u8(alu_outLO, new uint8((byte) sv.getValue()));
							break;
						case HWORD:
						case UHWORD:
							dmem.write_u16(alu_outLO, new uint16((short) sv.getValue()));
							break;
						case WORD:
						case UWORD:
							dmem.write_u32(alu_outLO, new uint32((int) sv.getValue()));
							break;
						case DWORD:
						case UDWORD:
							dmem.write_u64(alu_outLO, sv);
							break;
/*
				case WORD_RIGHT_PART:
					// refer to page A-153 of the MIPS IV Instruction Set Rev. 3.2
					switch(alu_outLO.getValue()&0x3)
					{
					case 0:
						dmem.write_u32(alu_outLO, store_value);
						
						logger.warn("Verify operation of SWR (0)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (0)!", pc);
						}
						break;
					case 1:
						dmem.write_u8(alu_outLO, new uint8((store_value.getValue())&0xFF));
						dmem.write_u8(new uint32(alu_outLO.getValue()+1), new uint8((store_value.getValue()>>8)&0xFF));
						dmem.write_u8(new uint32(alu_outLO.getValue()+2), new uint8((store_value.getValue()>>16)&0xFF));
						
						logger.warn("Verify operation of SWR (1)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (1)!", pc);
						}
						break;
					case 2:
						dmem.write_u8(alu_outLO, new uint8((store_value.getValue())&0xFF));
						dmem.write_u8(new uint32(alu_outLO.getValue()+1), new uint8((store_value.getValue()>>8)&0xFF));
						
						logger.warn("Verify operation of SWR (3)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (2)!", pc);
						}
						break;
					case 3:
						dmem.write_u8(alu_outLO, new uint8((store_value.getValue())&0xFF));
						
						logger.warn("Verify operation of SWR (3)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (3)!", pc);
						}
						break;
					}
					break;
				case WORD_LEFT_PART:
					// refer to page A-150 of the MIPS IV Instruction Set Rev. 3.2
					switch(alu_outLO.getValue()&0x3)
					{
					case 0:
						dmem.write_u8(alu_outLO, new uint8((store_value.getValue()>>24)&0xFF));
						
						logger.warn("Verify operation of SWL (0)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (0)!", pc);
						}
						break;
					case 1:
						dmem.write_u8(alu_outLO, new uint8((store_value.getValue()>>24)&0xFF));
						dmem.write_u8(new uint32(alu_outLO.getValue()-1), new uint8((store_value.getValue()>>16)&0xFF));
						
						logger.warn("Verify operation of SWL (1)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (1)!", pc);
						}
						break;
					case 2:
						dmem.write_u8(alu_outLO, new uint8((store_value.getValue()>>24)&0xFF));
						dmem.write_u8(new uint32(alu_outLO.getValue()-1), new uint8((store_value.getValue()>>16)&0xFF));
						dmem.write_u8(new uint32(alu_outLO.getValue()-2), new uint8((store_value.getValue()>>8)&0xFF));
						
						logger.warn("Verify operation of SWL (2)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (2)!", pc);
						}
						break;
					case 3:
						dmem.write_u32(new uint32(alu_outLO.getValue()-3), store_value);
						
						logger.warn("Verify operation of SWL (3)!");
						if(throwExceptionForUntestedAccesses)
						{
							throw new MemoryStageException("Verify operation of SWR (2)!", pc);
						}
						break;
					}
					break;
*/
						default:
							logger.error("Wrong memory width: " + inst.getMemoryWidth());
							throw new MemoryStageException("Wrong memory width: " + inst.getMemoryWidth(), pc);
					}
					stat.countMemWrite();
				} else {
					// stall
				}
			} else {
				logger.debug("PC: " + pc.getValueAsHexString() + " nothing to do");
			}
		} catch (MemoryException | MemoryStageException e) {
			e.setInstructionAddress(pc);
			throw e;
		}
		
		PipelineException memEx = null;
		if ((inst.getStore() || inst.getLoad()) && Preference.isMemoryWarningsEnabled()) {
			uint32 addr = alu_outLO;
			int byteLen = inst.getMemoryWidth().getByteWidth();
			if (!dmem.isReserved(addr,  byteLen))
				 memEx = new UnreservedMemoryAccessException(addr, byteLen, UnreservedMemoryAccessException.Stage.MEMORY, pc);
		}

		MemoryWritebackData mwd = new MemoryWritebackData(inst, pc, alu_out, new uint32((int)lv), jump);

		return new MemoryOutputData(mwd, memEx);
	}
}
