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
package riscVivid.datatypes;

public class BranchPredictionModuleExecuteData
{

	private boolean do_speculative_jump;
	private uint32 branch_tgt;
	private uint32 branch_pc;
	
	public BranchPredictionModuleExecuteData(boolean doSpeculativeJump, uint32 branchPc, uint32 branchTgt)
	{
		do_speculative_jump = doSpeculativeJump;
		branch_tgt = new uint32(branchTgt);
		branch_pc = new uint32(branchPc);
	}

	public boolean getDoSpeculativeJump()
	{
		return do_speculative_jump;
	}

	public uint32 getBranchTgt()
	{
		return branch_tgt;
	}
	
	public uint32 getPc()
	{
		return branch_pc;
	}

}
