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
package riscVivid.gui.internalframes;

import static riscVivid.gui.Preference.pref;

import javax.swing.JInternalFrame;

public class FrameConfiguration
{

    private JInternalFrame jif = null; //in, out
    private String posXPreferenceKey = "posx";
    private String posYPreferenceKey = "posy";
    private String sizeXPreferenceKey = "sizex";
    private String sizeYPreferenceKey = "sizey";
    private String isVisiblePreferenceKey = "isvisible";

    public FrameConfiguration(JInternalFrame jif)
    {
        this.jif = jif;
    }

    public void saveFrameConfiguration()
    {
    	String frameTitle = jif.getClass().getSimpleName();
        pref.putInt(frameTitle + posXPreferenceKey, jif.getX());
        pref.putInt(frameTitle + posYPreferenceKey, jif.getY());
        pref.putInt(frameTitle + sizeXPreferenceKey, jif.getSize().width);
        pref.putInt(frameTitle + sizeYPreferenceKey, jif.getSize().height);
        pref.putBoolean(frameTitle + isVisiblePreferenceKey, jif.isVisible());
    }

    public void loadFrameConfiguration()
    {
    	String frameTitle = jif.getClass().getSimpleName();
        jif.setBounds(pref.getInt(frameTitle + posXPreferenceKey, jif.getX()),
                pref.getInt(frameTitle + posYPreferenceKey, jif.getY()),
                pref.getInt(frameTitle + sizeXPreferenceKey, jif.getWidth()),
                pref.getInt(frameTitle + sizeYPreferenceKey, jif.getHeight()));

        try
        {
            jif.setVisible(pref.getBoolean(jif.getTitle() + isVisiblePreferenceKey, true));
        }
        catch (Exception e)
        {
            System.err.println("failed setting JInternalFrame to visible/invisible");
            e.printStackTrace();
        }
    }
}
