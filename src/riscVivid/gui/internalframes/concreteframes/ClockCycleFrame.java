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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import riscVivid.RiscVividSimulator;
import riscVivid.asm.DLXAssembler;
import riscVivid.datatypes.uint32;
import riscVivid.exception.MemoryException;
import riscVivid.gui.GUI_CONST;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.renderer.ClockCycleFrameTableCellRenderer;
import riscVivid.gui.internalframes.util.NotSelectableTableModel;
import riscVivid.gui.util.MWheelFontSizeChanger;
import riscVivid.util.ClockCycleLog;

@SuppressWarnings("serial")
public final class ClockCycleFrame extends OpenDLXSimInternalFrame implements GUI_CONST
{

    private final RiscVividSimulator openDLXSim;
    //frame text
    private final String addrHeaderText = "Address";
    private final String instHeaderText = "Instructions/Cycles";
    //default size values
    private final int instructionNameMaxColWidth = 150;
    private int block = 80;
    private int tableColumnWidth = 30;
    private int codeTableColumnWidth = 150;
    private int addrTableColumnWidth = 100;
    //tables, scrollpane and table models
    private JTable table, codeTable, addrTable;
    private NotSelectableTableModel model, codeModel, addrModel;
    private JScrollPane clockCycleScrollPane;
    private JScrollPane addrScrollPane;
    private JScrollPane codeScrollPane;
    private JScrollBar clockCycleScrollBarVertical;
    private JScrollBar addrScrollBar;
    private JScrollBar codeScrollBar;
    //private JScrollBar clockCycleScrollBarHorizontal;

    public ClockCycleFrame(String title)
    {
        super(title, true);
        openDLXSim = MainFrame.getInstance().getOpenDLXSim();
        initialize();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setLayout(new BorderLayout());

        //Code Table
        codeModel = new NotSelectableTableModel();
        codeTable = new JTable(codeModel);
        codeTable.setFocusable(false);
        codeTable.setShowGrid(false);
        codeTable.getTableHeader().setReorderingAllowed(false);
        codeTable.setShowHorizontalLines(true);
        codeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        codeModel.addColumn(instHeaderText);
        TableColumnModel tcm = codeTable.getColumnModel();
        tcm.getColumn(0).setMaxWidth(instructionNameMaxColWidth);
        tcm.getColumn(0).setMinWidth(instructionNameMaxColWidth);
        codeScrollPane = new JScrollPane(codeTable);
        codeScrollPane.setPreferredSize(new Dimension(tcm.getColumn(0).getMaxWidth(),
                codeScrollPane.getPreferredSize().height));
        codeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        codeScrollPane.getHorizontalScrollBar().setEnabled(false);
        codeScrollBar = codeScrollPane.getVerticalScrollBar();
        codeScrollBar.addAdjustmentListener(new AdjustmentListener()
        {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                clockCycleScrollBarVertical.setValue(e.getValue());
                addrScrollBar.setValue(e.getValue());
            }

        });
        //Address Table
        addrModel = new NotSelectableTableModel();
        addrTable = new JTable(addrModel);
        addrTable.setFocusable(false);
        addrTable.setShowGrid(false);
        addrTable.getTableHeader().setReorderingAllowed(false);
        addrTable.setShowHorizontalLines(true);
        addrTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        addrModel.addColumn(addrHeaderText);
        TableColumnModel tcm2 = addrTable.getColumnModel();
        tcm2.getColumn(0).setMaxWidth(instructionNameMaxColWidth);
        tcm2.getColumn(0).setMinWidth(instructionNameMaxColWidth);
        addrScrollPane = new JScrollPane(addrTable);
        addrScrollPane.setPreferredSize(new Dimension(tcm2.getColumn(0).getMaxWidth(),
                addrScrollPane.getPreferredSize().height));
        addrScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        addrScrollPane.getHorizontalScrollBar().setEnabled(false);
        addrScrollBar = addrScrollPane.getVerticalScrollBar();
        addrScrollBar.addAdjustmentListener(new AdjustmentListener()
        {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                clockCycleScrollBarVertical.setValue(e.getValue());
                codeScrollBar.setValue(e.getValue());
            }

        });

        //scroll pane and frame
        clockCycleScrollPane = makeTableScrollPane();

        for (JScrollPane s : new JScrollPane[] {clockCycleScrollPane,
                addrScrollPane, codeScrollPane})
            MWheelFontSizeChanger.getInstance().add(s);
        setFont(addrTable.getFont().deriveFont((float)Preference.getFontSize()));
        add(addrScrollPane, BorderLayout.EAST);
        add(codeScrollPane, BorderLayout.WEST);
        add(clockCycleScrollPane, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    private JScrollPane makeTableScrollPane()
    {
        //Clock Cycle Table
        model = new NotSelectableTableModel();
        table = new JTable(model);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new ClockCycleFrameTableCellRenderer());
        clockCycleScrollPane = new JScrollPane(table);
        clockCycleScrollBarVertical = clockCycleScrollPane.getVerticalScrollBar();
        clockCycleScrollBarVertical.addAdjustmentListener(new AdjustmentListener()
        {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                addrScrollBar.setValue(e.getValue());
                codeScrollBar.setValue(e.getValue());
            }

        });

        // scrolling synchronously causes view violations
        // requires a more complex/sophisticated approach
//        clockCycleScrollBarHorizontal = clockCycleScrollPane.getHorizontalScrollBar();
        /*  clockCycleScrollBarHorizontal.addAdjustmentListener(new AdjustmentListener()
         {
         @Override
         public void adjustmentValueChanged(AdjustmentEvent e)
         {
         clockCycleScrollBarVertical.setValue(e.getValue());

         }

         });*/
        clockCycleScrollPane.setPreferredSize(new Dimension(3 * block,
                clockCycleScrollPane.getPreferredSize().height));
        return clockCycleScrollPane;

    }

    @Override
    public void update()
    {

        //clear table
        addrModel.setRowCount(0);
        codeModel.setRowCount(0);
        model.setColumnCount(0);
        model.setRowCount(0);
        DLXAssembler asm = new DLXAssembler();

        int i = 0;
        for (uint32 addr : ClockCycleLog.code)
        {
            try
            {
                uint32 inst = openDLXSim.getPipeline().getInstructionMemory().read_u32(addr);
                String instStr = asm.Instr2Str(addr.getValue(), inst.getValue());
                addrModel.addRow(new String[] { addr.getValueAsHexString() });
                codeModel.addRow(new String[] { instStr });
                model.addColumn(i);
                model.addRow(new String[] { "" });

                final HashMap<uint32, String> h = ClockCycleLog.log.get(i);
                for (uint32 checkAddr : h.keySet())
                {
                    final ArrayList<uint32> forbidden = new ArrayList<>();
                    for (int k = addrModel.getRowCount() - 1; k >= 0; --k)
                    {
                        if (addrModel.getValueAt(k, 0).equals(checkAddr.getValueAsHexString())
                                && !forbidden.contains(checkAddr)
                                && !instStr.contains("bubble"))
                        {
                            model.setValueAt(h.get(checkAddr), k, i);
                            forbidden.add(checkAddr);
                        }
                    }
                }
                ++i;
            }
            catch (MemoryException e)
            {
                MainFrame.getInstance().getPipelineExceptionHandler().handlePipelineExceptions(e);
            }
        }
        for (int j = 0; j < table.getColumnModel().getColumnCount(); ++j)
        {
            TableColumn column = table.getColumnModel().getColumn(j);
            column.setMaxWidth(tableColumnWidth);
            column.setResizable(false);
        }

        // clockCycleScrollBarHorizontal.setValue(clockCycleScrollBarHorizontal.getMaximum());
        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, table.getColumnCount() - 1, true));
        codeTable.scrollRectToVisible(codeTable.getCellRect(table.getRowCount() - 1, 0, true));
        addrTable.scrollRectToVisible(addrTable.getCellRect(addrTable.getRowCount() - 1, 0, true));

    }

    @Override
    public void clean()
    {
        setVisible(false);
        dispose();
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	
    	// if size becomes smaller, first set the min width, then the max width
    	boolean minFirst = false;
    	if (f.getSize() < table.getFont().getSize())
    		minFirst = true;
    	
    	for (JTable t : new JTable[] {table, codeTable, addrTable}) {
    		t.setFont(f);
    		t.getTableHeader().setFont(f);
    		t.setRowHeight(f.getSize() + 4);
    	}
    	
    	// adjust width of the columns of the tables
    	this.tableColumnWidth = table.getFontMetrics(f).stringWidth("MEM_");
        for (int j = 0; j < table.getColumnModel().getColumnCount(); ++j)
        {
            TableColumn column = table.getColumnModel().getColumn(j);
            if (minFirst) {
                column.setMinWidth(tableColumnWidth);
                column.setMaxWidth(tableColumnWidth);
            } else {
                column.setMaxWidth(tableColumnWidth);
                column.setMinWidth(tableColumnWidth);
            }
        }
        this.codeTableColumnWidth = codeTable.getFontMetrics(f).stringWidth("bge a0, zero, pc0x00000000");
        this.addrTableColumnWidth = addrTable.getFontMetrics(f).stringWidth("0x00000000___");
        if (minFirst) {
        	codeTable.getColumnModel().getColumn(0).setMinWidth(codeTableColumnWidth);
        	codeTable.getColumnModel().getColumn(0).setMaxWidth(codeTableColumnWidth);
            addrTable.getColumnModel().getColumn(0).setMinWidth(addrTableColumnWidth);
            addrTable.getColumnModel().getColumn(0).setMaxWidth(addrTableColumnWidth);
        } else {
        	codeTable.getColumnModel().getColumn(0).setMaxWidth(codeTableColumnWidth);
        	codeTable.getColumnModel().getColumn(0).setMinWidth(codeTableColumnWidth);
            addrTable.getColumnModel().getColumn(0).setMaxWidth(addrTableColumnWidth);
            addrTable.getColumnModel().getColumn(0).setMinWidth(addrTableColumnWidth);
        }
        
        // adjust width of the scrollpanes
        codeScrollPane.setPreferredSize(new Dimension(
        		codeTableColumnWidth + codeScrollPane.getVerticalScrollBar().getWidth() + 3,
        		codeScrollPane.getPreferredSize().height));
        addrScrollPane.setPreferredSize(new Dimension(
        		addrTableColumnWidth + addrScrollPane.getVerticalScrollBar().getWidth() + 3,
                addrScrollPane.getPreferredSize().height));
        
        //scroll to the bottom; 
        if (minFirst) {
        	clockCycleScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
        	clockCycleScrollPane.getHorizontalScrollBar().setValue(Integer.MAX_VALUE);
        } else {
        	table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, table.getColumnCount() - 1, true));
        }
    }

}
