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
package riscVivid.gui.internalframes.concreteframes.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.undo.*;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.command.EventCommandLookUp;
import riscVivid.gui.command.userLevel.CommandChangeFontSize;
import riscVivid.gui.command.userLevel.CommandLoadAndRunFile;
import riscVivid.gui.command.userLevel.CommandLoadFile;
import riscVivid.gui.command.userLevel.CommandLoadFileBelow;
import riscVivid.gui.command.userLevel.CommandNewFile;
import riscVivid.gui.command.userLevel.CommandPerformEditorRedo;
import riscVivid.gui.command.userLevel.CommandPerformEditorUndo;
import riscVivid.gui.command.userLevel.CommandRunFromEditor;
import riscVivid.gui.command.userLevel.CommandSave;
import riscVivid.gui.internalframes.FrameConfiguration;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.factories.InternalFrameFactory;
import riscVivid.gui.util.MWheelFontSizeChanger;

@SuppressWarnings("serial")
public final class EditorFrame extends OpenDLXSimInternalFrame implements ActionListener, KeyListener, UndoableEditListener
{
    //the editor frame is a singleton
    
    private MainFrame mf;
    
    //default size values

    private final int size_x = 250;
    private final int size_y = 300;
    //text area
    //private JTextArea input;
    //buttons
    private JButton assem;
    private JButton load;
    private JButton loadandassem;
    private JButton addcode;
    private JButton save;
    private JButton clear;
    
    /* TODO
     * For now the undo/redo functionality is limited to 
     * 	- character based actions
     */
    private JButton undo;
    private JButton redo;
    
    private JButton enlarge;
    private JButton reduce;

    private static EditorFrame instance = null;
    private JTextArea jta;
    private JScrollPane scrollPane;
    private TextNumberingPanel tln;

    private UndoManager undoMgr;
    
    private CommandPerformEditorUndo undoCommand;
    private CommandPerformEditorRedo redoCommand;

    
    private int saved_state_hash;
    private String editor_frame_title;

    private EditorFrame(String title, MainFrame mf)
    {
        super(title, true);
        editor_frame_title = title;
        this.mf = mf;
        initialize();
    }

    public static EditorFrame getInstance(MainFrame mf)
    {
        if (instance == null)
        {
            instance = new EditorFrame(InternalFrameFactory.getFrameName(EditorFrame.class), mf);
            FrameConfiguration fc = new FrameConfiguration(instance);
            fc.loadFrameConfiguration();
        }
        return instance;
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        setLayout(new BorderLayout());
        // input = new JTextArea();
        //input.setSize(size_x, size_y);
        //JScrollPane scroll = new JScrollPane(input);

       new JTextPane();
//       JTextPane jtp = new JTextPane();
        /*
         *  
         final JScrollPane jsp = new JScrollPane();
         jtp.setEditorKitForContentType("text/riscVivid", new OpenDLXSimEditorKit());
         jtp.setContentType("text/riscVivid");
         jtp.getDocument().addDocumentListener(new DocumentListener()
         {
         public String getText()
         {
         int caretPosition = jtp.getDocument().getLength();
         Element root = jtp.getDocument().getDefaultRootElement();
         String text = "1" + System.getProperty("line.separator");
         for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++)
         {
         text += i + System.getProperty("line.separator");
         }
         return text;
         }

         @Override
         public void changedUpdate(DocumentEvent de)
         {
         lines.setText(getText());
         jsp.repaint();
         System.out.println(getParent());

         }

         @Override
         public void insertUpdate(DocumentEvent de)
         {
         lines.setText(getText());
         jsp.repaint();
         System.out.println(getParent());

         }

         @Override
         public void removeUpdate(DocumentEvent de)
         {
         lines.setText(getText());
         jsp.repaint();
         System.out.println(getParent());

         }

         });
         */


        jta = new JTextArea();
        setSavedState();
        scrollPane = new JScrollPane(jta);
        tln = new TextNumberingPanel(jta);
        jta.addKeyListener(this);
        jta.getDocument().addUndoableEditListener(this);
        
        MWheelFontSizeChanger.getInstance().add(jta, scrollPane);

        scrollPane.setRowHeaderView(tln);
        add(scrollPane, BorderLayout.CENTER);

        clear = createButton("New", "Clear All [ALT+C]", KeyEvent.VK_C, "/img/icons/tango/clear.png");
        load = createButton("Open...", "Open Program [CTRL+O]", KeyEvent.VK_O, "/img/icons/tango/load.png");
        addcode = createButton("Add Code...", "Add Code to Programms... [CTRL+I]", KeyEvent.VK_I, "/img/icons/tango/addcode.png");
        save = createButton("Save As...", "Save Program As... [ALT+S]", KeyEvent.VK_S, "/img/icons/tango/saveas.png");
        assem = createButton("Assemble", "Assemble [ALT+A]", KeyEvent.VK_A, "/img/icons/tango/run.png");
        loadandassem = createButton("Open & Assemble...", "Open Program and Assemble [CTRL+R]", KeyEvent.VK_O, "/img/icons/tango/loadandrun.png");
        undo = createButton("Undo", "Undo [CTRL+Z]", KeyEvent.VK_U, "/img/icons/tango/undo.png");
        redo = createButton("Redo", "Redo [CTRL+SHIFT+Z]", KeyEvent.VK_R, "/img/icons/tango/redo.png"); 
        reduce = createButton("Reduce", "Reduce [CTRL+DOWN]", KeyEvent.VK_DOWN, "/img/icons/tango/reduce.png");
        enlarge = createButton("Enlarge", "Enlarge [CTRL+UP]", KeyEvent.VK_UP, "/img/icons/tango/enlarge.png");
        
        // if  parameter command = null, command is not yet implemented and should be implemented soon   

        EventCommandLookUp.put(assem, new CommandRunFromEditor(mf));
        EventCommandLookUp.put(load, new CommandLoadFile(mf));
        EventCommandLookUp.put(loadandassem, new CommandLoadAndRunFile(mf));
        EventCommandLookUp.put(addcode, new CommandLoadFileBelow(mf));
        EventCommandLookUp.put(save, new CommandSave());
        EventCommandLookUp.put(clear, new CommandNewFile(mf));
//        EventCommandLookUp.put(undo, undoCommand); 
//        EventCommandLookUp.put(redo, redoCommand);
        EventCommandLookUp.put(enlarge,  new CommandChangeFontSize(+1));
        EventCommandLookUp.put(reduce,  new CommandChangeFontSize(-1));

        
        assem.addActionListener(this);
        load.addActionListener(this);
        loadandassem.addActionListener(this);
        addcode.addActionListener(this);
        save.addActionListener(this);
        clear.addActionListener(this);
        undo.addActionListener(this);
        redo.addActionListener(this);
        enlarge.addActionListener(this);
        reduce.addActionListener(this);

        // TODO deactivate toolbar when simulator is in run mode
        JToolBar toolBar = new JToolBar("Editor toolbar");
        toolBar.add(clear);
        toolBar.add(load);
        toolBar.add(addcode);
        toolBar.add(save);
        toolBar.add(assem);
        toolBar.add(loadandassem);
        toolBar.add(undo);
        toolBar.add(redo);
        toolBar.add(reduce);
        toolBar.add(enlarge);
        toolBar.setFloatable(false);
        toolBar.setRollover(false);
        toolBar.setFocusable(false);
        add(toolBar, BorderLayout.PAGE_START);
        
        
        setPreferredSize(new Dimension(size_x, size_y));
        setFont(jta.getFont().deriveFont((float)Preference.getFontSize()));

        pack();
        setVisible(true);
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	for (Component c : new Component[] {jta, tln}) {
        	c.setFont(f);
    	}
    	/* Don't change button size as it can yield weird looking results
    	Font buttonFont = f.deriveFont((float)f.getSize());
    	for (Component c : new Component[] {assem, load, loadandassem, addcode, 
    			save, clear, undo, redo, enlarge, reduce}) {
        	c.setFont(buttonFont);
    	}
    	*/
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        clean();
        EventCommandLookUp.get(e.getSource()).execute();
    }

    @Override
	public void undoableEditHappened(UndoableEditEvent e) {
		undoMgr.addEdit(e.getEdit());
	}

    @Override
    public void update()
    {
    }

    public String getText()
    {
        return jta.getText();
    }

    public void setText(String text)
    {
        jta.setText(text);
    }

    public void insertText(String text)
    {
        String tmp = jta.getText();
        tmp += text;
        jta.setText(tmp);
    }

    public void colorLine(int l)
    {
        Highlighter.HighlightPainter redPainter =
                new DefaultHighlighter.DefaultHighlightPainter(Color.red);
        try
        {
            System.out.println(l);
            int startIndex = jta.getLineStartOffset(l);
            int endIndex = jta.getLineEndOffset(l);
            jta.getHighlighter().addHighlight(startIndex, endIndex, redPainter);
        }
        catch (BadLocationException ble)
        {
            System.err.println("Failed coloring editor line");
        }

    }

    @Override
    public void clean()
    {
        jta.getHighlighter().removeAllHighlights();
    }

    public void validateButtons(OpenDLXSimState currentState)
    {
        if (currentState == OpenDLXSimState.RUNNING)
        {
            assem.setEnabled(false);
            clear.setEnabled(false);
            save.setEnabled(false);

        }
        else
        {
            assem.setEnabled(true);
            clear.setEnabled(true);
            save.setEnabled(true);
        }
    }
    
    public void setSavedState()
    {
        saved_state_hash = getTextHash();
        updateTitle();
    }
    
    private int getTextHash()
    {
        // TODO hashCode() might not be the the most suitable function to safe the editor state
        return getText().hashCode();
    }

    public boolean isTextSaved()
    {
        return (saved_state_hash == getTextHash());
    }


    @Override
    public void keyReleased(KeyEvent arg0)
    {
        updateTitle();
    }

    @Override
    public void keyTyped(KeyEvent arg0)
    {
        // Unused
    }
    
    @Override
    public void keyPressed(KeyEvent arg0)
    {
        // Unused
    }
    
    private void updateTitle()
    {
        if(!isTextSaved())
        {
            setTitle("*"+editor_frame_title);
        }
        else
        {
            setTitle(editor_frame_title);
        }
    }

    public void setUndoManager(UndoManager UndoMgr) 
    {
        undoMgr = UndoMgr;
        undoCommand = new CommandPerformEditorUndo(undoMgr);
        redoCommand = new CommandPerformEditorRedo(undoMgr);
        
        EventCommandLookUp.put(undo, undoCommand); 
        EventCommandLookUp.put(redo, redoCommand);

    }

    private JButton createButton(String name, String tooltip, int mnemonic, String icon_path) 
    {
        JButton button = new JButton();
        URL icon_url;
        if((icon_path != null) && ((icon_url = getClass().getResource(icon_path)) != null))
        {
            button.setIcon(new ImageIcon(icon_url));
        }
        else
        {
            button.setText(name);
        }
        button.setMnemonic(mnemonic); 
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        return button;
    }
}
