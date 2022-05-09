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
package riscVivid.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.undo.UndoManager;

import riscVivid.RiscVividSimulator;
import riscVivid.config.GlobalConfig;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.command.EventCommandLookUp;
import riscVivid.gui.command.userLevel.CommandExitProgram;
import riscVivid.gui.dialog.Input;
import riscVivid.gui.dialog.Output;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.gui.internalframes.factories.InternalFrameFactory;
import riscVivid.gui.menubar.MainFrameMenuBarFactory;
import riscVivid.gui.menubar.StateValidator;
import riscVivid.gui.util.PipelineExceptionHandler;
import riscVivid.util.BreakpointManager;
import riscVivid.util.RISCVSyscallHandler;
import riscVivid.util.TrapObservableDefault;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener, ItemListener
{
    // MainFrame is a Singleton.
    // hence it has a private constructor
    private static final MainFrame mf = new MainFrame();

    public Output output;
    public Input input;

    private RiscVividSimulator sim = null;
    private UndoManager undoMgr;
    private EditorFrame editor;
    private JDesktopPane desktop;
    private OpenDLXSimState state = OpenDLXSimState.IDLE;
    private File configFile;
    private JMenuBar menuBar;
    private PipelineExceptionHandler pexHandler = null;
    private String loadedCodeFilePath="";
    
    private MainFrame()
    {
        super("riscVivid " + GlobalConfig.VERSION);
        initialize();
        final ImageIcon icon = new ImageIcon(getClass().getResource("/img/riscVivid-quadrat128x128.png"), "riscVivid icon");
        setIconImage(icon.getImage());

        // Register output for pipeline
        TrapObservableDefault observableOutput = new TrapObservableDefault();
        observableOutput.addObserver(output);
        RISCVSyscallHandler.getInstance().setTrapObserverOutput(observableOutput);

        // Register input for pipeline
        TrapObservableDefault observableInput = new TrapObservableDefault();
        observableInput.addObserver(input);
        RISCVSyscallHandler.getInstance().setTrapObserverInput(observableInput);
        RISCVSyscallHandler.getInstance().setInput(input);
    }

    //thus it has a static access method
    public static MainFrame getInstance()
    {
        return mf;
    }

    //main frame delegates all incoming events caused by its submembers to command classes
    @Override
    public void actionPerformed(ActionEvent e)
    {
        EventCommandLookUp.get(e.getSource()).execute();
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        EventCommandLookUp.get(e.getSource()).execute();
    }

    private void initialize()
    {
        undoMgr = new UndoManager();
        undoMgr.setLimit(500);

        setMinimumSize(new Dimension(200, 200));
        desktop = new JDesktopPane();
        desktop.setBackground(Color.WHITE);
        setContentPane(desktop);

        //uses a factory to outsource creation of the menuBar
        MainFrameMenuBarFactory menuBarFactory = new MainFrameMenuBarFactory(this, this, this);
        Hashtable<String, JMenuItem> importantItems = new Hashtable<>();
        menuBar = menuBarFactory.createJMenuBar();
        setJMenuBar(menuBar);

        output = Output.getInstance(mf);
        input = Input.getInstance(mf);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(200, 200));
        setExtendedState(MAXIMIZED_BOTH);
        setVisible(true);

        /// create and select editor frame
        try
        {
            editor = EditorFrame.getInstance(this);
            editor.setUndoManager(undoMgr);
            desktop.add(editor);
            editor.setSelected(true);
        } catch (PropertyVetoException e1)
        {
            e1.printStackTrace();
        }

        setOpenDLXSimState(OpenDLXSimState.IDLE);
        pexHandler = new PipelineExceptionHandler(this);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                MainFrame frame = (MainFrame)e.getSource();
                CommandExitProgram exit = new CommandExitProgram(frame);
                if (exit.close())
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });

    }

    //INTERFACE
    public RiscVividSimulator getOpenDLXSim()
    {
        return sim;
    }

    public JInternalFrame[] getinternalFrames()
    {
        return desktop.getAllFrames();
    }

    public void setOpenDLXSim(RiscVividSimulator sim)
    {
        this.sim = sim;
        pexHandler.setSimulator(sim);
    }

    public void setOpenDLXSimState(OpenDLXSimState s)
    {
        this.state = s;
        StateValidator.validateMenu(menuBar, s);
        editor.validateButtons(getOpenDLXSimState());
    }

    public OpenDLXSimState getOpenDLXSimState()
    {
        return this.state;
    }

    public boolean isRunning()
    {
        return (state == OpenDLXSimState.RUNNING);
    }

    public boolean isExecuting()
    {
        return (state == OpenDLXSimState.EXECUTING);
    }

    public boolean isLazy()
    {
        return (state == OpenDLXSimState.IDLE);
    }

    public void addInternalFrame(OpenDLXSimInternalFrame mif)
    {
        desktop.add(mif);
    }

    public String getEditorText()
    {
        return editor.getText();
    }

    public void setEditorText(String text)
    {
        editor.setText(text);

    }

    public void colorEditorLine(int l)
    {
        editor.colorLine(l);
    }

    public void insertEditorText(String text)
    {
        editor.insertText(text);

    }

    public void setEditorSavedState()
    {
        editor.setSavedState();
    }

    public boolean isEditorTextSaved()
    {
        return editor.isTextSaved();
    }

    public void setEditorFrameVisible()
    {
    	editor.setVisible(true);
    }
    
    public File getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile(File configFile)
    {
        this.configFile = configFile;
    }

    public PipelineExceptionHandler getPipelineExceptionHandler()
    {
        return pexHandler;
    }

    public String getLoadedCodeFilePath()
    {
        return loadedCodeFilePath;
    }

    public void setLoadedCodeFilePath(String loadedCodeFilePath)
    {
        this.loadedCodeFilePath = loadedCodeFilePath;
        String editorTitle = new File(loadedCodeFilePath).getName();
        if (editorTitle.length() == 0)
        	editorTitle = InternalFrameFactory.getFrameName(EditorFrame.class);
        this.editor.setFrameTitle(editorTitle);
    }

    public UndoManager getEditorUndoManager() {
        return undoMgr;
    }

    public void setDesktopBackground(Color newBackground) {
        desktop.setBackground(newBackground);
    }

    public Color getMenuBarColor() {
        return menuBar.getBackground();
    }
}
