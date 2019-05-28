package riscVivid.gui.command.userLevel;

import javax.swing.JCheckBoxMenuItem;

import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;

public class CommandSetStopOnMemoryWarning implements Command {
    
    JCheckBoxMenuItem menuItem;
    
    public CommandSetStopOnMemoryWarning(JCheckBoxMenuItem menuItem) {
    	this.menuItem = menuItem;
    }
    @Override
    public void execute() {
    	Preference.pref.putBoolean(Preference.stopOnMemoryWarningPreferenceKey, menuItem.isSelected());
    }
}
