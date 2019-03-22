package riscVivid.gui.util;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;

import javax.swing.JScrollPane;

import riscVivid.gui.command.userLevel.CommandChangeFontSize;

public class MWheelFontSizeChanger {
	
	private static MWheelFontSizeChanger instance = null;
	private CommandChangeFontSize enlarge, reduce;
	private HashMap<Component, JScrollPane> map = new HashMap<Component, JScrollPane>();
	private Listener listener = new Listener();
	
	private MWheelFontSizeChanger() {
		enlarge = new CommandChangeFontSize(+1);
		reduce = new CommandChangeFontSize(-1);

	}
	
	public static MWheelFontSizeChanger getInstance() {
		if (instance == null)
			instance = new MWheelFontSizeChanger();
		return instance;
	}
	
	/**
	 * @param c Component to listen to focus and mousewheel
	 * @param scrollPane to be deactivated when ctrl is pressed
	 * When focus is lost, the scrollpane will be activated again;
	 * if the mousewheel is scrolled and CTRL is pressed, the font size will be adjusted
	 */
	public void add(Component c, JScrollPane scrollPane) {
		assert(c != null &&  scrollPane != null);
		map.put(c, scrollPane);
		scrollPane.addMouseWheelListener(listener);
		c.addKeyListener(listener);
		c.addFocusListener(listener);
		
	}
	
	/**
	 * @param scrollPane to listen to mousewheel; adjust the font size if scrolled + CTRL pressed
	 */
	public void add (Component c) {
		assert(c != null);
		c.addMouseWheelListener(listener);
	}
	
	private class Listener implements KeyListener, FocusListener, MouseWheelListener {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown()) {
				if (e.getWheelRotation() > 0)
					enlarge.execute();
				else
					reduce.execute();
			}
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			//Unused
		}

		@Override
		public void focusLost(FocusEvent e) {
			map.get(e.getSource()).setWheelScrollingEnabled(true);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				map.get(e.getSource()).setWheelScrollingEnabled(false);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				map.get(e.getSource()).setWheelScrollingEnabled(true);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// Unused
		}
	}
}
