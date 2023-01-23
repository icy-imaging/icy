/*
 * Copyright 2010-2023 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
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
package icy.gui.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import icy.common.listener.SkinChangeListener;
import icy.common.listener.weak.WeakListener;
import icy.image.ImageUtil;
import icy.preferences.GeneralPreferences;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import ij.util.Java2;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.*;
import java.util.List;

import com.formdev.flatlaf.intellijthemes.*;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;

/**
 * LookAndFeelUtil class. Use to install all skins used by Icy and helps manipulating UI manager.
 * @author Thomas MUSSET
 */
// TODO: 18/01/2023 Add setFontSize(float)
public final class LookAndFeelUtil {
    private static final Set<SkinChangeListener> listeners = new HashSet<>();
    private static final List<FlatLaf> SKINS = new ArrayList<>();
    private static FlatLaf currentSkin = null;
    private static UIDefaults defaults = null;

    public static void init() {
        try {
            // so ImageJ won't try to change the look and feel later
            Java2.setSystemLookAndFeel();
        }
        catch (Throwable t) {
            // just ignore the error here
        }

        // enabled LAF decoration instead of native ones
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Default themes
        addSkin(new FlatLightLaf());
        addSkin(new FlatDarkLaf());

        // Extra themes
        addSkin(new FlatArcIJTheme());
        addSkin(new FlatArcOrangeIJTheme());
        addSkin(new FlatArcDarkIJTheme());
        addSkin(new FlatArcDarkOrangeIJTheme());
        addSkin(new FlatCarbonIJTheme());
        addSkin(new FlatCobalt2IJTheme());
        addSkin(new FlatCyanLightIJTheme());
        addSkin(new FlatDarkFlatIJTheme());
        addSkin(new FlatDarkPurpleIJTheme());
        addSkin(new FlatDraculaIJTheme());
        addSkin(new FlatGradiantoDarkFuchsiaIJTheme());
        addSkin(new FlatGradiantoDeepOceanIJTheme());
        addSkin(new FlatGradiantoMidnightBlueIJTheme());
        addSkin(new FlatGradiantoNatureGreenIJTheme());
        addSkin(new FlatGrayIJTheme());
        addSkin(new FlatGruvboxDarkHardIJTheme());
        addSkin(new FlatGruvboxDarkMediumIJTheme());
        addSkin(new FlatGruvboxDarkSoftIJTheme());
        addSkin(new FlatHiberbeeDarkIJTheme());
        addSkin(new FlatHighContrastIJTheme());
        addSkin(new FlatLightFlatIJTheme());
        addSkin(new FlatMaterialDesignDarkIJTheme());
        addSkin(new FlatMonocaiIJTheme());
        addSkin(new FlatMonokaiProIJTheme());
        addSkin(new FlatNordIJTheme());
        addSkin(new FlatOneDarkIJTheme());
        addSkin(new FlatSolarizedDarkIJTheme());
        addSkin(new FlatSolarizedLightIJTheme());
        addSkin(new FlatSpacegrayIJTheme());
        addSkin(new FlatVuesionIJTheme());
        addSkin(new FlatXcodeDarkIJTheme());

        // sort skin to put light skins first
        SKINS.sort((o1, o2) -> Boolean.compare(o1.isDark(), o2.isDark()));

        // get saved skin, if not found, get default skin and save it
        setSkin(GeneralPreferences.getGuiSkin());
    }

    private static void addSkin(FlatLaf skin) {
        SKINS.add(skin);
    }

    /**
     * @return the skins list
     */
    public static List<FlatLaf> getSkins() {
        return SKINS;
    }

    private static void setUIDefaults(UIDefaults d) {
        defaults = d;
    }

    public static UIDefaults getUIDefaults() {
        return defaults;
    }

    public static Color getForeground() {
        if (defaults == null)
            return null;
        return defaults.getColor("MenuItem.foreground");
    }

    public static Color getDisabledForeground() {
        if (defaults == null)
            return null;
        return defaults.getColor("MenuItem.disabledForeground");
    }

    public static Color getSelectionForeground() {
        if (defaults == null)
            return null;
        return defaults.getColor("MenuItem.selectionForeground");
    }

    /**
     * Get skin by its name
     * @param name the skin name.
     * @return the skin if found, otherwise returns null.
     */
    public static FlatLaf getSkinByName(String name) {
        for (FlatLaf skin : SKINS)
            if (skin.getName().equals(name))
                return skin;
        return null;
    }

    /**
     * Get skin by its classname
     * @param className the skin classname in String.
     * @return the skin if found, otherwise returns null.
     */
    public static FlatLaf getSkinByClassName(String className) {
        for (FlatLaf skin : SKINS)
            if (skin.getClass().getName().equals(className))
                return skin;
        return null;
    }

    /**
     * @return the currently used skin. May be null if called before {@link #init()}.
     */
    public static FlatLaf getCurrentSkin() {
        return currentSkin;
    }

    /**
     * @return the currently used skin display name. May be empty if called before {@link #init()}.
     * @see #getCurrentSkin()
     */
    public static String getCurrentSkinName() {
        final FlatLaf skin = getCurrentSkin();
        return (skin != null) ? skin.getName() : "";
    }

    /**
     * @return the default skin name.
     */
    public static String getDefaultSkinName() {
        return FlatLightLaf.NAME;
    }

    /**
     * @return the default font size.
     */
    // FIXME: 18/01/2023 does nothing with FlatLaf
    public static int getDefaultFontSize() {
        return 13;
    }

    public static float getDefaultIconSize() {
        return 20f;
    }

    /**
     * Switch to specified skin. Do nothing if new skin equals actual skin.
     * @param skin the skin to use.
     * @see #fireSkinChangeListeners()
     */
    public static void setSkin(FlatLaf skin) {
        if ((getCurrentSkin() == null || !skin.getClass().equals(getCurrentSkin().getClass())) && SKINS.contains(skin)) {
            ThreadUtil.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(skin);

                    FlatLaf.updateUI();

                    setUIDefaults(skin.getDefaults());
                    currentSkin = skin;
                    GeneralPreferences.setGuiSkin(skin.getName());

                    fireSkinChangeListeners();
                }
                catch (Exception e) {
                    System.err.println("LookAndFeelUtil.setSkin(" + skin.getName() + ") error :");
                    IcyExceptionHandler.showErrorMessage(e, false);
                }
            });
        }
    }

    /**
     * Switch to specified skin name. Do nothing if new skin name equals actual skin name.
     * @param skinName the skin name.
     * @see #setSkin(FlatLaf)
     */
    public static void setSkin(final String skinName) {
        if (!StringUtil.equals(skinName, getCurrentSkinName()))
            try {
                for (FlatLaf skin : SKINS)
                    if (skinName.equals(skin.getName()))
                        setSkin(skin);
            }
            catch (Exception e) {
                System.err.println("LookAndFeelUtil.setSkin(" + skinName + ") error :");
                IcyExceptionHandler.showErrorMessage(e, false);
            }
    }

    /**
     * @param listener the listener to add.
     */
    public static void addListener(SkinChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener the listener to remove.
     */
    public static void removeListener(SkinChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fire all the listeners.
     */
    private static void fireSkinChangeListeners() {
        for (SkinChangeListener listener : listeners)
            listener.skinChanged();
    }

    /**
     * @deprecated use {@link icy.common.listener.weak.WeakSkinChangeListener} instead.
     */
    @Deprecated
    public static class WeakSkinChangeListener extends WeakListener<SkinChangeListener> implements SkinChangeListener {
        public WeakSkinChangeListener(SkinChangeListener listener)
        {
            super(listener);
        }

        @Override
        public void removeListener(Object source)
        {
            LookAndFeelUtil.removeListener(this);
        }

        @Override
        public void skinChanged()
        {
            final SkinChangeListener listener = getListener();

            if (listener != null)
                listener.skinChanged();
        }
    }

    /* OLD FUNCTIONS */
    /**
     * Return the foreground color for the specified component
     */
    public static Color getForeground(Component c) {
        if (c != null)
            return c.getForeground();

        //return Color.white;
        return getForeground();
    }

    /**
     * Paint foreground component color in 'out' image<br>
     * depending original alpha intensity from 'alphaImage'
     */
    public static Image paintForegroundImageFromAlphaImage(Component c, Image alphaImage, Image out) {
        return ImageUtil.paintColorImageFromAlphaImage(alphaImage, out, getForeground(c));
    }

    /**
     * Return the background color for the specified component
     */
    public static Color getBackground(Component c)
    {
        if (c != null)
            return c.getBackground();

        //return Color.lightGray;
        return getUIDefaults().getColor("Panel.background");
    }
}