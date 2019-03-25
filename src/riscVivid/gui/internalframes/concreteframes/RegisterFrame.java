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
package riscVivid.gui.internalframes.concreteframes;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JCheckBox;

import riscVivid.RegisterSet;
import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint8;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.factories.tableFactories.RegisterTableFactory;
import riscVivid.gui.internalframes.util.TableSizeCalculator;

@SuppressWarnings("serial")
public final class RegisterFrame extends OpenDLXSimInternalFrame implements ItemListener
{

    private RegisterSet rs;
    private JTable registerTable;
    private JCheckBox checkBoxHex;

    public RegisterFrame(String title)
    {
        super(title, false);
        this.rs = MainFrame.getInstance().getOpenDLXSim().getPipeline().getRegisterSet();
        initialize();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < ArchCfg.getRegisterCount(); ++i)
        {
            final String value;
            final uint32 register_value = rs.read(new uint8(i));
            if (Preference.displayRegistersAsHex())
                value = register_value.getValueAsHexString();
            else
                value = register_value.getValueAsDecimalString();
            
            registerTable.getModel().setValueAt(value, i, 1);
        }
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        //make the scrollpane
        registerTable = new RegisterTableFactory(rs).createTable();
        JScrollPane scrollpane = new JScrollPane(registerTable);
        scrollpane.setFocusable(false);
        registerTable.setFillsViewportHeight(true);
        TableSizeCalculator.setDefaultMaxTableSize(scrollpane, registerTable,
                TableSizeCalculator.SET_SIZE_BOTH);
        //config internal frame
        setLayout(new BorderLayout());
        add(scrollpane, BorderLayout.CENTER);
        // new checkbox
        checkBoxHex = new JCheckBox("values as hex");
		checkBoxHex.setSelected(Preference.displayRegistersAsHex());
		checkBoxHex.addItemListener(this);
		add(checkBoxHex, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    @Override
    public void clean()
    {
        setVisible(false);
        dispose();
    }

	@Override
	public void itemStateChanged(ItemEvent e) {
		Preference.pref.putBoolean(Preference.displayRegistersAsHex, checkBoxHex.isSelected());
		update();
		
	}

}
