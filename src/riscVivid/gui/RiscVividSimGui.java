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

import java.awt.Font;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import riscVivid.gui.command.userLevel.CommandChangeFontSize;
import riscVivid.gui.util.MWheelFontSizeChanger;

public class RiscVividSimGui
{

    public static void riscVividGui_main()
    {
        //set default
        String lafClassName = UIManager.getLookAndFeel().getClass().getCanonicalName();
        //get user preference
        lafClassName = Preference.pref.get(Preference.lookAndFeel, lafClassName);
        
        try {
            // check if the lafClassName is valid, 
            // since it could be the case that the preferences file contains an invalid class
            Class.forName(lafClassName, false, null);
        } catch (ClassNotFoundException e) {
            // If the lafClassName is not valid, set it to the class name of the current L&F class.
            String currentLafClassName = UIManager.getLookAndFeel().getClass().getCanonicalName();
            if(currentLafClassName == lafClassName)
            {
                System.err.println("Failed to set look and feel '" + lafClassName + "'");
                e.printStackTrace();
            }
            else
            {
                lafClassName = currentLafClassName;
                // store new (valid) L&F in preferences
                Preference.pref.put(Preference.lookAndFeel, lafClassName);
            }
            
        }
       	
        //set selected L&F
        setLookAndFeelWithoutTreeUpdate(lafClassName);

        MainFrame.getInstance();

        // Font size options: set initial font size
        CommandChangeFontSize.setFontSize(Preference.getFontSize());
        MWheelFontSizeChanger.getInstance().add(MainFrame.getInstance());
    }

    //set look and feel, but do not update the Swing Component Tree
    //  (this is for use when first initializing the UI)
    private static void setLookAndFeelWithoutTreeUpdate(String lafClassName)
    {
        UIManager.put("TextArea.font", new Font(Font.MONOSPACED, Font.PLAIN, 12));
        UIManager.put("TextPane.font", new Font(Font.MONOSPACED, Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font(Font.MONOSPACED, Font.PLAIN, 12));
        UIManager.put("Table.font", new Font(Font.MONOSPACED, Font.PLAIN, 12));

        try
        {   //sets the systems default LookAndFeel
            UIManager.setLookAndFeel(lafClassName);
        }
        catch (Exception e)
        {
            System.err.println("Failed to set look and feel '" + lafClassName + "'");
            e.printStackTrace();
        }
    }

    //set the look at feel application-wide, save it to preferences, and update
    //  the Swing Component Tree
    public static void setLookAndFeel(String lafClassName)
    {
        setLookAndFeelWithoutTreeUpdate(lafClassName);
        SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
    }
}
