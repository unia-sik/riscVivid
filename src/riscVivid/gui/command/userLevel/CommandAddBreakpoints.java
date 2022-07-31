package riscVivid.gui.command.userLevel;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.dialog.Input;
import riscVivid.gui.internalframes.concreteframes.editor.TextNumberingPanel;
import riscVivid.gui.util.DialogWrapper;

import javax.swing.text.BadLocationException;

public class CommandAddBreakpoints implements Command {
    private TextNumberingPanel tln;

    public CommandAddBreakpoints(TextNumberingPanel tln) {
        this.tln = tln;
    }

    @Override
    public void execute() {
        Input input = Input.getInstance(MainFrame.getInstance());
        input.update("");

        String inputReturn = input.getInput();
        if (inputReturn.equals("")) return;

        String[] breakpoints = inputReturn.split(" ");
        for (String bp : breakpoints) {
            if (bp.contains(" ")) continue;

            try {
                int line = Integer.parseInt(bp);

                if (tln.isBreakpoint(line)) {
                    tln.removeBreakpoint(line);
                } else {
                    tln.addBreakpoint(line);
                }

                tln.updateUI();
            } catch (BadLocationException | NumberFormatException e) {
                DialogWrapper.showErrorDialog(MainFrame.getInstance(), e.getClass().getSimpleName() + " during adding breakpoints: " + e.getMessage());
            }
        }
    }
}
