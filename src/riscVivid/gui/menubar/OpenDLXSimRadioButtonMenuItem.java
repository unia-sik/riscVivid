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
package riscVivid.gui.menubar;

import javax.swing.JRadioButtonMenuItem;

import riscVivid.gui.GUI_CONST;

@SuppressWarnings("serial")
public class OpenDLXSimRadioButtonMenuItem extends JRadioButtonMenuItem
{

    GUI_CONST.OpenDLXSimState state[];

    public OpenDLXSimRadioButtonMenuItem(GUI_CONST.OpenDLXSimState state[], String text)
    {
        super(text);
        this.state = state;
    }

}
