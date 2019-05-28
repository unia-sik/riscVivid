package riscVivid.gui.util;

import java.io.File;

import javax.swing.JOptionPane;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.userLevel.CommandSave;

public final class DialogWrapper {
	/**
	 * Asks user in a dialog if current content should be saved,
	 * and does so in that case
	 * To be called whenever the program modifies the text in the editor,
	 * e.g. when loading a new file
	 * @return: true if program can proceed,
	 * 			false if program should abort
	 * @param clearEditor:  clear Editor except if user chooses 'Cancel'
	 *
	 */
	public static boolean askForSave(boolean clearEditor) {
		MainFrame mf = MainFrame.getInstance();
		File loadedFile = new File(mf.getLoadedCodeFilePath());
		String dialogText = loadedFile.exists() ?
				"Save changes to file \"" + loadedFile.getName() + "\"?" :
					"Save editor changes?";

        switch(JOptionPane.showConfirmDialog(mf,dialogText)) {
        case JOptionPane.OK_OPTION:
			new CommandSave().execute();
        case JOptionPane.NO_OPTION:
			if (clearEditor) {
                 mf.setEditorText("");
                 mf.setEditorSavedState();
                 mf.setLoadedCodeFilePath("");
			}
			return true;
        case JOptionPane.CANCEL_OPTION:
        default:
			return false;         
         }
	}
	
	public static void showWarning(String title, String content) {
		JOptionPane.showMessageDialog(MainFrame.getInstance(), content, title, JOptionPane.WARNING_MESSAGE);
	}
}
