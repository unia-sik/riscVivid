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
package riscVivid.util;

import java.io.File;

public class TmpFileCleaner
{

    public static void cleanUp()
    {
    	// delete temporary files
    	String tmp = System.getProperty("java.io.tmpdir");
    	File f = new File(tmp);
    	File allFiles[] = f.listFiles();
    	for (int i = 0; i < allFiles.length; ++i)
    	{
    		// FIXME -> static name used!
    		if (allFiles[i].getName().contains("riscVivid"))
    		{
    			allFiles[i].deleteOnExit();
    		}
    	}
    	System.out.println("Cleaned up: java.io.tmpdir = " + f.getAbsolutePath());
    }

}
