package riscVivid.gui.command.userLevel;

import java.awt.Font;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;

public class CommandChangeFontSize implements Command {
	final private int dir;
	
	public CommandChangeFontSize(int dir) {
		this.dir = (int) Math.signum(dir);
	}
	
	@Override
	public void execute() {
		int oldSize = Preference.getFontSize();
		// set bounds for the font size
		if ((oldSize <= 7 && dir < 0) || (oldSize >= 70 && dir > 0))
			return;
		// set stepSize =1 for small font sizes, = 2 for medium and = 5 for large font sizes
		int stepSize;
		if (oldSize > 30 || (oldSize == 30 & dir < 0))
			stepSize = 5;
		else if (oldSize > 10 || (oldSize == 10 & dir < 0))
			stepSize = 2;
		else
			stepSize = 1;
		
		int newSize = oldSize + dir * stepSize;
		setFontSize(newSize);
		
	}
	
	public static void setFontSize(int fontSize) {
		MainFrame mf = MainFrame.getInstance();

		setMenuBarFontSize(mf, fontSize);
		setInternalFramesFontSize(mf, fontSize);
		
        Preference.pref.putInt(Preference.fontSize, fontSize);
	}
	
	private static void setMenuBarFontSize(MainFrame mf, int fontSize) {
		for (int i = 0; i < mf.getJMenuBar().getMenuCount(); ++i) {
			JMenu menu = mf.getJMenuBar().getMenu(i);
			Font newFont = menu.getFont().deriveFont((float)fontSize);
			menu.setFont(newFont);
			for (int itemIdx = 0; itemIdx < menu.getItemCount(); ++itemIdx) {
				JMenuItem item = menu.getItem(itemIdx);
				if (item != null)
					item.setFont(newFont);
			}
		}
	}
	
	private static void setInternalFramesFontSize(MainFrame mf, int fontSize) {
		for (JInternalFrame frame : mf.getinternalFrames() ) {
				frame.setFont(frame.getFont().deriveFont((float)fontSize));
		}
	}
}
