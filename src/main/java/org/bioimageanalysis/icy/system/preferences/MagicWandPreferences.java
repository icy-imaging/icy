/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.system.preferences;

import org.bioimageanalysis.icy.model.roi.tool.MagicWand.MagicWandConnectivity;
import org.bioimageanalysis.icy.model.roi.tool.MagicWand.MagicWandGradientToleranceMode;

/**
 * @author Stephane
 */
public class MagicWandPreferences
{
    /**
     * preferences id
     */
    private static final String PREF_ID = "magicWand";

    /**
     * id
     */
    private static final String ID_GRADIENT_TOLERANCE_MODE = "gradientToleranceMode";
    private static final String ID_GRADIENT_TOLERANCE_VALUE = "gradientToleranceValue";
    private static final String ID_CONNECTIVITY = "connectivity";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load()
    {
        // load preferences
        preferences = ApplicationPreferences.getPreferences().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences()
    {
        return preferences;
    }

    public static MagicWandGradientToleranceMode getGradientToleranceMode()
    {
        return MagicWandGradientToleranceMode.values()[preferences.getInt(ID_GRADIENT_TOLERANCE_MODE, 0)];
    }

    public static double getGradientToleranceValue()
    {
        return preferences.getDouble(ID_GRADIENT_TOLERANCE_VALUE, 0d);
    }

    public static MagicWandConnectivity getConnectivity()
    {
        return MagicWandConnectivity.values()[preferences.getInt(ID_CONNECTIVITY, 0)];
    }

    public static void setGradientToleranceMode(MagicWandGradientToleranceMode value)
    {
        preferences.putInt(ID_GRADIENT_TOLERANCE_MODE, value.ordinal());
    }

    public static void setGradientToleranceValue(double value)
    {
        preferences.putDouble(ID_GRADIENT_TOLERANCE_VALUE, value);
    }

    public static void setConnectivity(MagicWandConnectivity value)
    {
        preferences.putInt(ID_CONNECTIVITY, value.ordinal());
    }
}
