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

import java.util.prefs.Preferences;

import riscVivid.config.GlobalConfig;

public class Preference
{
    public static Preferences pref = Preferences.userRoot().node(
            GlobalConfig.PREFERENCES_DIR);

    // global definitions of the different preference keys
    public static final String forwardingPreferenceKey = "forwardingenabled";
    public static final String mipsCompatibilityPreferenceKey = "mipscompatibilityenabled";
    public static final String noBranchDelaySlotPreferenceKey = "nobranchdelayslot";
    public static final String isaTypePreferenceKey = "isatype";
    public static final String bpTypePreferenceKey = "bptype";
    public static final String bpInitialStatePreferenceKey = "bbinitialstate";
    public static final String btbSizePreferenceKey = "btbsize";
    public static final String maxCyclesPreferenceKey = "maxcycles";
    public static final String displayMemoryAsHex = "displayMemoryAsHex";
    public static final String showExitMessage = "showexitmessage";
    public static final String lookAndFeel = "lookandfeel";
    public static final String fontSize = "fontsize";
    public static final String saveChooserPathPreferenceKey = "savefilechooserpath";

    public static final String displayRegistersAsHex = "displayRegistersAsHex";
    public static final String initializeOptionPreferenceKey = "initializeoption";
    public static final String showInitializeOptionMessage = "showinitoptionmessage";
    public static final String stopOnMemoryWarningPreferenceKey = "stopOnMemoryWarning";


    public static String getSaveFileChooserPath() {
        return Preference.pref.get(saveChooserPathPreferenceKey, "/home");
    }
    
    public static boolean displayMemoryAsHex()
    {
        return pref.getBoolean(displayMemoryAsHex, true);
    }

    public static boolean displayRegistersAsHex()
    {
        return pref.getBoolean(displayRegistersAsHex, true);
    }

    public static int getFontSize() {
    	return pref.getInt(Preference.fontSize, 12);
    }

    public static boolean isStopOnMemoryWarningEnabled() {
        return Preference.pref.getBoolean(stopOnMemoryWarningPreferenceKey, false);
    }
    // TODO: Also move all configuration stuff into this file.
}
