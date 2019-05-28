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

import java.awt.Component;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.JInternalFrame;

import riscVivid.asm.Labels;
import riscVivid.datatypes.uint32;
import riscVivid.exception.MemoryException;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.systemLevel.CommandLoadFrameConfigurationSysLevel;
import riscVivid.gui.command.systemLevel.CommandSaveFrameConfigurationSysLevel;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.factories.InternalFrameFactory;
import riscVivid.gui.internalframes.factories.tableFactories.MemoryTableFactory;
import riscVivid.gui.internalframes.util.TableSizeCalculator;
import riscVivid.gui.internalframes.util.ValueInput;
import riscVivid.gui.util.MWheelFontSizeChanger;

@SuppressWarnings("serial")
public final class MemoryFrame extends OpenDLXSimInternalFrame implements ActionListener, KeyListener, FocusListener, ItemListener
{

    //tables, scrollpane and table models
    private JTable memoryTable;
    private JButton reload;
	private JCheckBox checkBoxHex;
    private static int rows = 100, startAddr = 0;
    private JTextField rowInput;
    private JTextField addrInput;
    private JLabel rowLabel;
    private JLabel addrLabel;
    private MainFrame mf;

    public MemoryFrame(String name, MainFrame mf)
    {
        super(name, false);
        this.mf = mf;
	// find start address of first data segment in config
	if (mf.getOpenDLXSim().getConfig().contains("data_begin_0")) 
		startAddr = ValueInput.getValueSilent(mf.getOpenDLXSim().getConfig().getProperty("data_begin_0"));
	else if (mf.getOpenDLXSim().getConfig().contains("data_begin")) 
		startAddr = ValueInput.getValueSilent(mf.getOpenDLXSim().getConfig().getProperty("data_begin")); 

        initialize();
    }

    @Override
    public void update()
    {
        TableModel model = memoryTable.getModel();
        if (model.getColumnCount() > 0)
        {
            try
            {
                for (int i = 0; i < model.getRowCount(); ++i)
                {
                    final uint32 uint_val = MainFrame.getInstance().
                    	getOpenDLXSim().getPipeline().getMainMemory().
                    	read_u32(new uint32(startAddr + i * 4), false);
                    final Object value;
                    if (Preference.displayMemoryAsHex())
                        value = uint_val.getValueAsHexString();
                    else
                        value = uint_val.getValue();
                    model.setValueAt(value, i, 1);
                }
            }
            catch (MemoryException e)
            {
                mf.getPipelineExceptionHandler().handlePipelineExceptions(e);
            }
        }
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        setLayout(new BorderLayout());
        memoryTable = new MemoryTableFactory(rows, startAddr).createTable();
        JScrollPane scrollpane = new JScrollPane(memoryTable);
        MWheelFontSizeChanger.getInstance().add(scrollpane);
        scrollpane.setFocusable(false);
        memoryTable.setFillsViewportHeight(true);
        TableSizeCalculator.setDefaultMaxTableSize(scrollpane, memoryTable, TableSizeCalculator.SET_SIZE_WIDTH);
        add(scrollpane, BorderLayout.CENTER);
        
        //input
        JPanel inputPanel = new JPanel();
        addrLabel = new JLabel("start addr");
        inputPanel.add(addrLabel);
        addrInput = new JTextField(10);
        addrInput.addKeyListener(this);
        addrInput.setText("0x" + Integer.toHexString(startAddr));
        addrInput.addFocusListener(this);
        inputPanel.add(addrInput);
        rowLabel = new JLabel("rows");
        inputPanel.add(rowLabel);
        rowInput = new JTextField(10);
        rowInput.setText("" + rows);
        rowInput.addKeyListener(this);
        rowInput.addFocusListener(this);
        inputPanel.add(rowInput);
        add(inputPanel, BorderLayout.NORTH);
        //controls
        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        reload = new JButton("reload");
        reload.addActionListener(this);
        JPanel reloadPanel = new JPanel();
      	reloadPanel.add(reload);
      	controlPanel.add(reloadPanel);
        checkBoxHex = new JCheckBox("values as hex");
		checkBoxHex.setSelected(Preference.displayMemoryAsHex());
		checkBoxHex.addItemListener(this);
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.add(checkBoxHex);
		controlPanel.add(checkBoxPanel);
        add(controlPanel, BorderLayout.SOUTH);
        setFont(memoryTable.getFont().deriveFont((float)Preference.getFontSize()));
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
    public void actionPerformed(ActionEvent e)
    {
        try {
        	Integer newStartAddr;
        	try {
                newStartAddr = ValueInput.getValueSilent(addrInput.getText());
        	} catch(NumberFormatException ex) { // addrInput is possibly a label
        		if (Labels.labels != null && Labels.labels.containsKey(addrInput.getText()))
        			   newStartAddr = (Integer) Labels.labels.get(addrInput.getText());
        		else 
        			throw new Exception();
        	}
        	// Test if newStartAddress is aligned; only set startAddr if that's the case
        	if (newStartAddr != null) {
		    	try {
		    		MainFrame.getInstance().
		    		getOpenDLXSim().getPipeline().getMainMemory().
		    		read_u32(new uint32(newStartAddr), false);
		    		// if successful, set startAddr to new one
		    		startAddr = newStartAddr;
		    	} catch (MemoryException ex) {
		            mf.getPipelineExceptionHandler().handlePipelineExceptions(ex);
		            return;
		    	}
        	}
        	
            Integer newRows = ValueInput.getValueSilent(rowInput.getText());
            if (newRows == null)
            	newRows = rows;
        	// if number of rows stays the same, only update the table
        	if (newRows == rows) { 
        	    addrInput.setText("0x" + Integer.toHexString(startAddr));
        	    // update row numbers
                TableModel model = memoryTable.getModel();
                for (int i = 0; i < model.getRowCount(); ++i)
                {
                    model.setValueAt(new uint32(startAddr + i * 4).getValueAsHexString(), i, 0);
                }
                // update values
        		update();
        	} 
        	else // if number of rows changes, replace the Memory Frame with a new one and set it on top
        	{
        		rows = newRows;
        		new CommandSaveFrameConfigurationSysLevel(mf).execute();

                clean();
                InternalFrameFactory.getInstance().createMemoryFrame(mf);
                new CommandLoadFrameConfigurationSysLevel(mf).execute();
               
                // find new MemoryFrame and put it on top
               for (JInternalFrame f : mf.getinternalFrames()) {
            	   if (f instanceof MemoryFrame) {
            		   f.moveToFront();
                       try {
                          f.setSelected(true);
                       }
                       catch (PropertyVetoException ex)
                       {
                           ex.printStackTrace();
                       }
                       break;
            	   }
               }
            }
        } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "for input only hex (0x..) " +
                        "address, decimal address or label (e.g. \"main\") allowed");
        }
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	for (Component c : new Component[] { addrLabel, addrInput, rowLabel, rowInput, reload, checkBoxHex})
    		if (c != null)
    			c.setFont(f);

    	if (memoryTable != null) {
    		memoryTable.setFont(f);
    		memoryTable.getTableHeader().setFont(f);;
    		memoryTable.setRowHeight(f.getSize() + 4);
			int columnWidth = memoryTable.getFontMetrics(f).stringWidth("0x00000000___");
			// if width is shortened, first set min width, then max width
			if (columnWidth < memoryTable.getColumn("address").getMaxWidth()) {
		    	memoryTable.getColumn("address").setMinWidth(columnWidth);
		    	memoryTable.getColumn("address").setMaxWidth(columnWidth);
			} else {
		    	memoryTable.getColumn("address").setMaxWidth(columnWidth);
		    	memoryTable.getColumn("address").setMinWidth(columnWidth);
			}
			// Last column may expand indefinitely
			memoryTable.getColumn("value").setMaxWidth(Integer.MAX_VALUE);
			memoryTable.getColumn("value").setMinWidth(columnWidth);
    	}
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            reload.doClick();
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            reload.doClick();
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void focusGained(FocusEvent e)
    {
        JTextField f = (JTextField) e.getSource();
        f.selectAll();
    }

    @Override
    public void focusLost(FocusEvent e)
    {
    }
    
	@Override
	public void itemStateChanged(ItemEvent e) {
		Preference.pref.putBoolean(Preference.displayMemoryAsHex, checkBoxHex.isSelected());
		update();
	}

}
