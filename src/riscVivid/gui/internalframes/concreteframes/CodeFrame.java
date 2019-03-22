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
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import riscVivid.RiscVividSimulator;
import riscVivid.PipelineContainer;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.factories.tableFactories.CodeTableFactory;
import riscVivid.gui.internalframes.util.TableSizeCalculator;
import riscVivid.gui.util.MWheelFontSizeChanger;

@SuppressWarnings("serial")
public final class CodeFrame extends OpenDLXSimInternalFrame
{

    private final RiscVividSimulator openDLXSim;
    private JTable codeTable;
    private String IFValue = "";
    private String IDValue = "";
    private String EXValue = "";
    private String MEMValue = "";
    private String WBValue = "";

    public CodeFrame(String title)
    {
        super(title, false);
        openDLXSim = MainFrame.getInstance().getOpenDLXSim();
        initialize();
    }

    @Override
    public void update()
    {
        final PipelineContainer pipeline = openDLXSim.getPipeline();
        IFValue = pipeline.getFetchDecodeLatch().element().getPc().getValueAsHexString();
        IDValue = pipeline.getDecodeExecuteLatch().element().getPc().getValueAsHexString();
        EXValue = pipeline.getExecuteMemoryLatch().element().getPc().getValueAsHexString();
        MEMValue = pipeline.getMemoryWriteBackLatch().element().getPc().getValueAsHexString();
        WBValue = pipeline.getWriteBackLatch().element().getPc().getValueAsHexString();

        TableModel model = codeTable.getModel();
        for (int row = 0; row < model.getRowCount(); ++row)
        {
            String addr = model.getValueAt(row, 0).toString().substring(0, 10);

            if (addr.contains(IFValue))
            {
                addr += "  " + "IF";

                // move IF row into focus - i.e. scroll to IF-row
                if (codeTable.getParent() != null)
                    codeTable.scrollRectToVisible(codeTable.getCellRect(row, 0, true));
            }
            else if (addr.contains(IDValue))
                addr += "  " + "ID";
            else if (addr.contains(EXValue))
                addr += "  " + "EX";
            else if (addr.contains(MEMValue))
                addr += "  " + "MEM";
            else if (addr.contains(WBValue))
                addr += "  " + "WB";

            model.setValueAt(addr, row, 0);
        }
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        //make the scrollpane
        codeTable = new CodeTableFactory(openDLXSim).createTable();
        JScrollPane scrollpane = new JScrollPane(codeTable);
        scrollpane.setFocusable(false);
        MWheelFontSizeChanger.getInstance().add(scrollpane);

        codeTable.setFillsViewportHeight(true);
        TableSizeCalculator.setDefaultMaxTableSize(scrollpane, codeTable,
                TableSizeCalculator.SET_SIZE_WIDTH);
        //config internal frame
        setLayout(new BorderLayout());
        add(scrollpane, BorderLayout.CENTER);
        setFont(codeTable.getFont().deriveFont((float)Preference.getFontSize()));

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
    public void setFont(Font f) {
    	super.setFont(f);
    	codeTable.setFont(f);
    	codeTable.getTableHeader().setFont(f);
    	codeTable.setRowHeight(f.getSize() + 4);
    	int addressWidth = codeTable.getFontMetrics(f).stringWidth("0x00000000_MEM____");
    	int hexWidth = codeTable.getFontMetrics(f).stringWidth("0x00000000___");
    	// if width is shortened, first set min width, then max width
    	if (addressWidth < codeTable.getColumn("address").getMaxWidth()) {
        	codeTable.getColumn("address").setMinWidth(addressWidth);
        	codeTable.getColumn("address").setMaxWidth(addressWidth);
        	codeTable.getColumn("code hex").setMinWidth(hexWidth);
        	codeTable.getColumn("code hex").setMaxWidth(hexWidth);
    	} else {
        	codeTable.getColumn("address").setMaxWidth(addressWidth);
        	codeTable.getColumn("address").setMinWidth(addressWidth);
        	codeTable.getColumn("code hex").setMaxWidth(hexWidth);
        	codeTable.getColumn("code hex").setMinWidth(hexWidth);
    	}
    	// the last column may expand indefinitely
    	int codeWidth = codeTable.getFontMetrics(f).stringWidth("add zero, zero, zero_");
    	codeTable.getColumn("code RISCV").setMaxWidth(Integer.MAX_VALUE);
    	codeTable.getColumn("code RISCV").setMinWidth(codeWidth);
    }

}
