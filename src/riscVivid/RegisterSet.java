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

import java.util.Random;

import org.apache.log4j.Logger;

import riscVivid.asm.instruction.Registers;
import riscVivid.datatypes.*;
import riscVivid.gui.Preference;

public class RegisterSet
{
	private static Logger logger = Logger.getLogger("REGISTERSET");
	private final byte register_count = 32;
	private uint32[] gp_registers;
	private uint32 HI;
	private uint32 LO;
	
	public RegisterSet()
	{
		gp_registers = new uint32[register_count];
		HI = new uint32();
		LO = new uint32();
		initRegisters();
	}
	
	public uint32 read(uint8 reg)
	{
		return new uint32(gp_registers[reg.getValue()].getValue());
	}
	
	public void write(uint8 reg, uint32 value)
	{
		if(reg.getValue() == 0)
		{
			logger.error("Cannot write register 0");
		}
		else
		{
			gp_registers[reg.getValue()].setValue(value);
		}
	}
	
	public uint32 read_SP(SpecialRegisters reg)
	{
		uint32 value = new uint32();
		switch(reg)
		{
		case HI:
			value.setValue(HI);
			break;
		case LO:
			value.setValue(LO);
			break;
		}
		return value;
	}
	
	public void write_SP(SpecialRegisters reg, uint32 value)
	{
		switch(reg)
		{
		case HI:
			HI.setValue(value);
			break;
		case LO:
			LO.setValue(value);
			break;
		}
	}
	
	private void initRegisters()
	{
	    	int init = Preference.pref.getInt(Preference.initializeOptionPreferenceKey, 0);
	    	boolean random = init > 0xff || init < 0;

	    	Random rand = new Random();
	    	if (!random) { // 0 <= init <= 0xff
		    // Set all bytes of init to (byte)init (conversion not needed, as init <= 0xff)
	    	    init = init << 24 | init << 16 | init << 8 | init;
	    	}
		for(byte i = 0; i < register_count; i++)
		{
			uint32 val = new uint32(random ? rand.nextInt() : init);
			gp_registers[i] = val;
		}
		HI = new uint32(random ? rand.nextInt() : init);
		LO = new uint32(random ? rand.nextInt() : init);
		
		gp_registers[Registers.instance().getInteger("zero")] = new uint32(0);
	}

	public void printContent()
	{
		logger.debug("   |  0             1             2             3             4             5             6             7            |");
		logger.debug("---+-----------------------------------------------------------------------------------------------------------------+");
		logger.debug(" 0 | " + ArchCfg.getRegisterDescription(0) + " " + gp_registers[0].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(1) + " " + gp_registers[1].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(2) + " " + gp_registers[2].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(3) + " " + gp_registers[3].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(4) + " " + gp_registers[4].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(5) + " " + gp_registers[5].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(6) + " " + gp_registers[6].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(7) + " " + gp_registers[7].getValueAsHexString() + " |");
		logger.debug(" 8 | " + ArchCfg.getRegisterDescription(8) + " " + gp_registers[8].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(9) + " " + gp_registers[9].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(10) + " " + gp_registers[10].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(11) + " " + gp_registers[11].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(12) + " " + gp_registers[12].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(13) + " " + gp_registers[13].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(14) + " " + gp_registers[14].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(15) + " " + gp_registers[15].getValueAsHexString() + " |");
		logger.debug("16 | " + ArchCfg.getRegisterDescription(16) + " " + gp_registers[16].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(17) + " " + gp_registers[17].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(18) + " " + gp_registers[18].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(19) + " " + gp_registers[19].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(20) + " " + gp_registers[20].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(21) + " " + gp_registers[21].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(22) + " " + gp_registers[22].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(23) + " " + gp_registers[23].getValueAsHexString() + " |");
		logger.debug("24 | " + ArchCfg.getRegisterDescription(24) + " " + gp_registers[24].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(25) + " " + gp_registers[25].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(26) + " " + gp_registers[26].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(27) + " " + gp_registers[27].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(28) + " " + gp_registers[28].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(29) + " " + gp_registers[29].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(30) + " " + gp_registers[30].getValueAsHexString() + " " + ArchCfg.getRegisterDescription(31) + " " + gp_registers[31].getValueAsHexString() + " |");
		logger.debug("SP | HI " + HI.getValueAsHexString() + " LO " + LO.getValueAsHexString() + "                                                                                     |");
		logger.debug("---+-----------------------------------------------------------------------------------------------------------------+");
	}
	
	public String getRegisterName(uint8 reg)
	{
		return ArchCfg.getRegisterDescription(reg.getValue());
	}

	public void setStackPointer(uint32 sp)
	{
		gp_registers[29] = sp;
	}
        
}
