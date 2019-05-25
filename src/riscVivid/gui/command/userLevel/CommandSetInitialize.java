package riscVivid.gui.command.userLevel;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import riscVivid.config.GlobalConfig;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;
import riscVivid.gui.internalframes.util.ValueInput;

public class CommandSetInitialize implements Command {
	public static enum Choice {
	    ZERO("0x00", 0), FF("0xFF", 0xff), C0("0xC0", 0xC0), 
	    _100("100", 100), RAND("RAND", Integer.MAX_VALUE);
	    private String numberStr;
	    private int number;
	    Choice(String numberAsStr, int number) {
	        this.numberStr = numberAsStr;
	        this.number = number;
	    }
    };

	private Choice c;
	
	public CommandSetInitialize(Choice c) {
		this.c = c;
	}
	
	@Override
	public void execute() {
	    int oldChoice = Preference.pref.getInt(Preference.initializeOptionPreferenceKey, getChoiceInt(Choice.ZERO));

	    Preference.pref.putInt(Preference.initializeOptionPreferenceKey, getChoiceInt(c));

	    if (getChoiceInt(c) != oldChoice && MainFrame.getInstance().getOpenDLXSim() != null &&
	    		Preference.pref.getBoolean(Preference.showInitializeOptionMessage, true))
	    {
            final String message = "In order to apply the new settings, the program must be recompiled.";
            final JCheckBox checkbox = new JCheckBox("Do not show this message again.");
            final Object content[] = {message, checkbox};
            JOptionPane.showMessageDialog(MainFrame.getInstance(), content);

            Preference.pref.putBoolean(Preference.showInitializeOptionMessage, !checkbox.isSelected());
	    }
	}

	public static int getChoiceInt(Choice c) {
	    return c.number;
	}

	public static String getChoiceString(Choice c) {
	    return c.numberStr;
	}
}
