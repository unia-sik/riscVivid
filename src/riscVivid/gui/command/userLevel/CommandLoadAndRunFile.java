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
package riscVivid.gui.command.userLevel;

import java.io.File;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandCompileCode;
import riscVivid.gui.command.systemLevel.CommandLoadCodeFileToEditor;
import riscVivid.gui.command.systemLevel.CommandOpenCodeFile;
import riscVivid.gui.command.systemLevel.CommandResetSimulator;
import riscVivid.gui.command.systemLevel.CommandSaveFrameConfigurationSysLevel;
import riscVivid.gui.command.systemLevel.CommandStartExecuting;
import riscVivid.gui.util.DialogWrapper;

public class CommandLoadAndRunFile implements Command
{

    private MainFrame mf;

    public CommandLoadAndRunFile(MainFrame mf)
    {
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        if (!mf.isRunning())
        {
            if (!mf.isEditorTextSaved())
            {
                if(!DialogWrapper.askForSave(true))
                	return;
            }
            //save current window position
            new CommandSaveFrameConfigurationSysLevel(mf).execute();
            //show filechooser dialog and choose file
            CommandOpenCodeFile c10 = new CommandOpenCodeFile(mf);
            c10.execute();
            //get chosen file
            File f = c10.getCodeFile();

            //file is null if user canceled filechooser dialog
            if (f != null)
            {
                //reset simulator before loading new content
                new CommandResetSimulator(mf).execute();

                //put code into editorFrame
                new CommandLoadCodeFileToEditor(mf, f, true).execute();
                mf.setLoadedCodeFilePath(f.getAbsolutePath());

                //compile/assemble code with asm package
                CommandCompileCode c8 = new CommandCompileCode(mf, f);
                c8.execute();
                //get result = config file
                File configFile = c8.getConfigFile();

                //check if assembly was successfull
                if (configFile != null)
                {
                    //initialize riscVivid and create internal frames, set status to executing
                    new CommandStartExecuting(mf, configFile).execute();
                }
                mf.setEditorFrameVisible();
            }

        }
    }

}
