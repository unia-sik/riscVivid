/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * Copyright (C) 2013-2016 The riscVivid project, University of Augsburg, Germany
 * <https://github.com/unia-sik/riscVivid>
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
package riscVivid.datatypes;


public class MemoryWritebackData
{

	private Instruction inst;
	private uint32 pc;
	private uint32[] alu_out;
	private uint32 ld_result;
	private boolean jump;

	public MemoryWritebackData(Instruction inst, uint32 pc, uint32[] alu_out, uint32 ld_result, boolean jump)
	{
		this.inst = inst;
		this.pc = pc;
		this.alu_out = alu_out;
		this.ld_result = ld_result;
		this.jump = jump;
	}

	public Instruction getInst()
	{
		return inst;
	}
	
	public uint32 getPc()
	{
		return pc;
	}

	public uint32[] getAluOut()
	{
		return alu_out;
	}

	public uint32 getLdResult()
	{
		return ld_result;
	}
	
	public boolean getJump()
	{
		return jump;
	}
	
	public void flush()
	{
		inst = new Instruction(new uint32(0));
		pc = new uint32(0);
		alu_out[0] = new uint32(0);
		alu_out[1] = new uint32(0);
		ld_result = new uint32(0);
		jump = false;
	}

}
