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

import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.EventCommandLookUp;
import riscVivid.gui.command.userLevel.*;
import riscVivid.gui.internalframes.FrameConfiguration;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.factories.InternalFrameFactory;
import riscVivid.gui.util.MWheelFontSizeChanger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.Date;

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
    private JButton saveAs;
    private JButton save;
    private JButton clear;
    private JButton addbreakpoints;
    private JButton find;
    private JButton enlarge;
    private JButton reduce;
    private JButton reformat;
    
    /* TODO
     * For now the undo/redo functionality is limited to 
     * 	- character based actions
     */
    private JButton undo;
    private JButton redo;
    
    private static EditorFrame instance = null;
    private JTextArea jta;
    private JScrollPane scrollPane;
    private TextNumberingPanel tln;

    private UndoManager undoMgr;
    
    private CommandPerformEditorUndo undoCommand;
    private CommandPerformEditorRedo redoCommand;

    private int selectionStartBeforeTab = -1;
    private String selectedTextBeforeTab = null;
    
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
    protected void initialize() {
        super.initialize();
        setLayout(new BorderLayout());
        // input = new JTextArea();
        //input.setSize(size_x, size_y);
        //JScrollPane scroll = new JScrollPane(input);

//       new JTextPane();
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
        tln = new TextNumberingPanel(jta, mf);
        jta.addKeyListener(this);
        jta.getDocument().addUndoableEditListener(this);

        MWheelFontSizeChanger.getInstance().add(jta, scrollPane);

        scrollPane.setRowHeaderView(tln);
        add(scrollPane, BorderLayout.CENTER);

        clear = createButton("New", "Clear All [ALT+C]", KeyEvent.VK_C, "/img/icons/tango/clear.png");
        addbreakpoints = createButton("Add Breakpoints", "Add Breakpoints [ALT+B]", KeyEvent.VK_B, "/img/icons/tango/breakpoint.png");
        load = createButton("Open...", "Open Program [CTRL+O]", KeyEvent.VK_O, "/img/icons/tango/load.png");
        addcode = createButton("Add Code...", "Add Code to Programms... [CTRL+I]", KeyEvent.VK_I, "/img/icons/tango/addcode.png");
        save = createButton("Save", "Save current file[CTRL+S]", KeyEvent.VK_S, "/img/icons/tango/save.png");
        saveAs = createButton("Save As...", "Save Program As... [CTRL+D]", KeyEvent.VK_D, "/img/icons/tango/saveas.png");
        assem = createButton("Assemble", "Assemble [ALT+A]", KeyEvent.VK_A, "/img/icons/tango/run.png");
        loadandassem = createButton("Open & Assemble...", "Open Program and Assemble [CTRL+R]", KeyEvent.VK_O, "/img/icons/tango/loadandrun.png");
        undo = createButton("Undo", "Undo [CTRL+Z]", KeyEvent.VK_Z, "/img/icons/tango/undo.png");
        redo = createButton("Redo", "Redo [CTRL+SHIFT+Z]", KeyEvent.VK_Y, "/img/icons/tango/redo.png");
        reduce = createButton("Reduce", "Reduce [CTRL+DOWN]", KeyEvent.VK_DOWN, "/img/icons/tango/reduce.png");
        enlarge = createButton("Enlarge", "Enlarge [CTRL+UP]", KeyEvent.VK_UP, "/img/icons/tango/enlarge.png");
        find = createButton("Find", "Find/Replace [CTRL+F]", KeyEvent.VK_F, "/img/icons/tango/find.png");
        reformat = createButton("Format", "Reformat code [CTRL+ALT+F]", KeyEvent.VK_R, "/img/icons/tango/reformat.png");

        // if  parameter command = null, command is not yet implemented and should be implemented soon   

        EventCommandLookUp.put(assem, new CommandRunFromEditor(mf));
        EventCommandLookUp.put(load, new CommandLoadFile(mf));
        EventCommandLookUp.put(loadandassem, new CommandLoadAndRunFile(mf));
        EventCommandLookUp.put(addcode, new CommandLoadFileBelow(mf));
        EventCommandLookUp.put(save, new CommandSave());
        EventCommandLookUp.put(saveAs, new CommandSaveAs());
        EventCommandLookUp.put(clear, new CommandNewFile(mf));

        EventCommandLookUp.put(addbreakpoints, new CommandAddBreakpoints(tln));
//        EventCommandLookUp.put(undo, undoCommand);   // this is done in setUndoManager()
//        EventCommandLookUp.put(redo, redoCommand);
        EventCommandLookUp.put(enlarge, new CommandChangeFontSize(+1));
        EventCommandLookUp.put(reduce, new CommandChangeFontSize(-1));
        EventCommandLookUp.put(find, new CommandFindReplace(mf));
        EventCommandLookUp.put(reformat, new CommandReformatCode());

        JToolBar toolBar = new JToolBar("Editor toolbar");

        for (JButton j : new JButton[]{clear, addbreakpoints, load, addcode, save, saveAs, assem, loadandassem, undo, redo,
                reduce, enlarge, find, reformat})
        {
            j.addActionListener(this);
            toolBar.add(j);
        }

        toolBar.setFloatable(false);
        toolBar.setRollover(false);
        toolBar.setFocusable(false);
        add(toolBar, BorderLayout.PAGE_START);
        
        
        resetLocationAndSize();
        setVisible(true);
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	for (Component c : new Component[] {jta, tln}) {
    		if (c != null)
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
        tln.update();
    }

    public int getTabSize() {
        return this.jta.getTabSize();
    }
    public void setTabSize(int size) {
        this.jta.setTabSize(size);
    }

    public String getText()
    {
        return jta.getText();
    }

    public void setText(String text)
    {
        jta.setText(text);
        // TODO: better solution to keep the ViewPosition than to hold it for a fixed time
        holdScrollPane( 100);
        updateTitle();
    }

    /**
     * fixes the position of the scroll pane for the given time
     * @param milliseconds
     */
    public void holdScrollPane(int milliseconds) {
        this.holdScrollPane(scrollPane.getViewport().getViewPosition(), milliseconds);
    }

    /**
     * fixes the position of the scroll pane at Point p for the given time
     */
    public void holdScrollPane(final Point p, final int milliseconds) {
        // don't allow the scrollPane to change the ViewPosition for DELAY ms
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            private Date removeTime =  new Date(new Date().getTime() + milliseconds);
            @Override
            public void stateChanged(ChangeEvent e) {
                JViewport viewport = (JViewport)e.getSource();
                if (new Date().after(removeTime))
                    viewport.removeChangeListener(this);
                else if (!viewport.getViewPosition().equals(p)) {
                    viewport.setViewPosition(p);
                }
            }
        });
    }

    public void insertText(String text)
    {
        jta.append(text);
    }

    public void colorLine(int l)
    {
        try {
            colorSection(jta.getLineStartOffset(l), jta.getLineEndOffset(l), Color.red);
        } catch (BadLocationException ble) { }
    }
    
    public void colorSection(int startIndex, int endIndex, Color color) throws BadLocationException {
        Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(color);
        jta.getHighlighter().addHighlight(startIndex, endIndex, painter);
    }
    
    
    public void removeColorHighlights() {
        jta.getHighlighter().removeAllHighlights();
    }
    
    public void selectLine(int l) {
        try {
            selectSection(jta.getLineStartOffset(l), jta.getLineEndOffset(l));
        } catch (BadLocationException e) {}
    }
    public String getSelectedText() {
        return jta.getSelectedText();
    }
    public void selectSection(int startIndex, int endIndex) {
        jta.select(startIndex,  endIndex);
    }
    public int getSelectionStart() {
        return jta.getSelectionStart();
    }
    public int getSelectionEnd() {
        return jta.getSelectionEnd();
    }
    
    @Override
    public void clean()
    {
        jta.getHighlighter().removeAllHighlights();
    }

    public void resetLocationAndSize()
    {
        Dimension desktopSize = mf.getContentPane().getSize();
        setPreferredSize(new Dimension(desktopSize.width/2, desktopSize.height - 10));
        setFont(jta.getFont().deriveFont((float)Preference.getFontSize()));
        pack();
        this.setLocation(desktopSize.width/2, 0);
    }

    public void validateButtons(OpenDLXSimState currentState)
    {
        if (currentState == OpenDLXSimState.RUNNING)
        {
            for (JButton j : new JButton[] {clear, addbreakpoints, load, addcode, save, saveAs, assem, loadandassem, undo, redo,
                    reduce, enlarge, find, reformat})
                j.setEnabled(false);
        }
        else
        {
            for (JButton j : new JButton[] {clear, addbreakpoints, load, addcode, save, saveAs, assem, loadandassem, undo, redo,
                    reduce, enlarge, find, reformat})
                j.setEnabled(true);
        }
        // update the TextNumberingPanel if the Simulator State changes
        tln.update();
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
    public void keyTyped(KeyEvent e)
    {
        // Set indent equal to the indent in the previous line
       if (e.getKeyChar() == '\n') {
           int caretPos = jta.getCaretPosition();
           try {
               int lineNum = jta.getLineOfOffset(caretPos) - 1;
               if (jta.getText(jta.getLineStartOffset(lineNum), 1).equals("\t")){
                   String tabsToInsert = "\t";
                   for (int pos = jta.getLineStartOffset(lineNum)+1; jta.getText(pos, 1).equals("\t"); ++pos)
                       tabsToInsert += "\t";
                   jta.insert(tabsToInsert, jta.getCaretPosition());
               }
           } catch (BadLocationException ex) {}
       } else if (e.getKeyChar() == '\t') { // change indent of selected lines
           if (this.selectedTextBeforeTab != null) {
               try {
                   if (!e.isShiftDown())
                        jta.replaceRange(selectedTextBeforeTab, selectionStartBeforeTab, selectionStartBeforeTab+1);
                   int selectionStart = selectionStartBeforeTab;
                   int selectionEnd = selectionStart + selectedTextBeforeTab.length(); // end is exclusive
                   jta.setSelectionStart(selectionStartBeforeTab);
                   int firstLine = jta.getLineOfOffset(selectionStartBeforeTab);
                   int lastLine = firstLine;
                   int idx = -1;
                   while ((idx = selectedTextBeforeTab.indexOf("\n", idx+1)) >= 0)
                       lastLine++;
                   for (int line = firstLine; line <= lastLine; line++){
                       int lineOffset = jta.getLineStartOffset(line);
                       if (e.isShiftDown()) { // decrease indent of selected lines
                           if (jta.getText(lineOffset, 1).equals("\t")) {
                               jta.replaceRange("", lineOffset, lineOffset + 1);
                               if (lineOffset < selectionStart)
                                   selectionStart--;
                               selectionEnd--;
                           }
                       } else { // increase indent of selected lines
                           jta.insert("\t", lineOffset);
                           if (lineOffset < selectionStart)
                               selectionStart++;
                           selectionEnd++;
                       }
                   }
                   jta.setSelectionStart(selectionStart);
                   jta.setSelectionEnd(selectionEnd);
               } catch (Exception ex) {
               } finally {
                   this.selectionStartBeforeTab = -1;
                   this.selectedTextBeforeTab = null;
               }
           } else if (e.isShiftDown()) {
               try {
                   int pos = jta.getCaretPosition();
                   int lineStartOff = jta.getLineStartOffset(jta.getLineOfOffset(pos));
                   if (jta.getText(lineStartOff, 1).equals("\t")) // decrease indent if possible
                       jta.replaceRange("", lineStartOff, lineStartOff+1);
               } catch (BadLocationException ex) {
               }
           }
       }
    }
    
    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyChar() == '\t') {
            this.selectedTextBeforeTab = jta.getSelectedText();
            if (selectedTextBeforeTab != null) {
                this.selectionStartBeforeTab = jta.getSelectionStart();
            }
        }
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
    
    public String getFrameTitle() {
    	return editor_frame_title;
    }
    
    public void setFrameTitle(String title) {
    	this.editor_frame_title = title;
    	updateTitle();
    }
    
    public void scrollLineToVisible(int line) {
        try {
            int startOffset = jta.getDocument().getDefaultRootElement().getElement(line - 1).getStartOffset();
            jta.scrollRectToVisible(jta.modelToView(startOffset));
        } catch (BadLocationException e) {
        }
    }
}
