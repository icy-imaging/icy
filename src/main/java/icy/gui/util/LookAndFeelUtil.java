/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.gui.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import icy.common.listener.SkinChangeListener;
import icy.image.ImageUtil;
import icy.preferences.GeneralPreferences;
import icy.system.SystemUtil;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

/**
 * LookAndFeelUtil class. Use to install all skins used by Icy and helps manipulating UI manager.
 *
 * @author Thomas Musset
 */
public final class LookAndFeelUtil {
    @NotNull
    private static final Set<SkinChangeListener> LISTENERS = new HashSet<>();
    @NotNull
    private static final List<FlatLaf> SKINS = new ArrayList<>();
    @Nullable
    private static FlatLaf currentSkin = null;
    @Nullable
    private static UIDefaults defaults = null;
    private static boolean fontInstalled = false;
    private static int fontSize = getDefaultFontSize();

    @NotNull
    private static final Color RED_DARK = new Color(200, 64, 64);
    @NotNull
    private static final Color RED_BRIGHT = new Color(255, 128, 128);
    @NotNull
    private static final Color GREEN_DARK = new Color(64, 128, 64);
    @NotNull
    private static final Color GREEN_BRIGHT = new Color(128, 255, 128);
    @NotNull
    private static final Color BLUE_DARK = new Color(0, 85, 174);
    @NotNull
    private static final Color BLUE_BRIGHT = new Color(157, 205, 255);
    @NotNull
    private static final Color YELLOW_DARK = new Color(200, 100, 0);
    @NotNull
    private static final Color YELLOW_BRIGHT = new Color(255, 150, 0);

    public enum ColorType {
        UI_BUTTON_DEFAULT("Button.foreground"),
        UI_BUTTON_DISABLED("Button.disabledText"),
        UI_BUTTON_SELECTED("Button.selectedForeground"),
        UI_TOGGLEBUTTON_DEFAULT("ToggleButton.foreground"),
        UI_TOGGLEBUTTON_DISABLED("ToggleButton.disabledText"),
        UI_TOGGLEBUTTON_SELECTED("ToggleButton.selectedForeground"),
        UI_MENU_DEFAULT("Menu.foreground"),
        UI_MENU_DISABLED("Menu.disabledForeground"),
        UI_MENU_SELECTED("Menu.selectionForeground"),
        UI_MENUITEM_DEFAULT("MenuItem.foreground"),
        UI_MENUITEM_DISABLED("MenuItem.disabledForeground"),
        UI_MENUITEM_SELECTED("MenuItem.selectionForeground");

        @NotNull
        public final String type;

        ColorType(@NotNull final String s) {
            type = s;
        }


        @NotNull
        @Override
        public final String toString() {
            return type;
        }
    }

    public static void init() {
        if (SystemUtil.isWindows()) {
            // enabled LAF decoration instead of native ones
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        if (SystemUtil.isWindows() || SystemUtil.isUnix()) {
            System.setProperty("flatlaf.menuBarEmbedded", "true");
        }
        else if (SystemUtil.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "false");
        }

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try (final InputStream is = LookAndFeelUtil.class.getResourceAsStream("/fonts/JetBrainsMono-Medium.ttf")) {
            if (is != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                fontInstalled = true;
            }
        }
        catch (final IOException | FontFormatException e) {
            IcyLogger.warn(LookAndFeelUtil.class, e, "Unable to install font.");
            fontInstalled = false;
        }

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
        setFontSize(GeneralPreferences.getGuiFontSize());

        UIManager.put("SplitPaneDivider.gripDotCount", 1);
    }

    private static void addSkin(@NotNull final FlatLaf skin) {
        SKINS.add(skin);
    }

    /**
     * @return the skins list
     */
    @NotNull
    public static List<FlatLaf> getSkins() {
        return SKINS;
    }

    private static void setUIDefaults(@Nullable final UIDefaults d) {
        defaults = d;
    }

    @Nullable
    public static UIDefaults getUIDefaults() {
        return defaults;
    }

    @Nullable
    public static Color getUIColor(@NotNull final ColorType colorType) {
        if (defaults == null)
            return null;
        return defaults.getColor(colorType.type);
    }

    @NotNull
    public static Color getAccentForeground() {
        if (isDarkMode())
            return Color.WHITE;
        else
            return Color.BLACK;
    }

    @NotNull
    public static Color getRed() {
        if (isDarkMode())
            return RED_DARK;
        else
            return RED_BRIGHT;
    }

    @NotNull
    public static Color getGreen() {
        if (isDarkMode())
            return GREEN_DARK;
        else
            return GREEN_BRIGHT;
    }

    @NotNull
    public static Color getBlue() {
        if (isDarkMode())
            return BLUE_DARK;
        else
            return BLUE_BRIGHT;
    }

    public static Color getYellow() {
        if (isDarkMode())
            return YELLOW_DARK;
        else
            return YELLOW_BRIGHT;
    }

    public static boolean isRed(@Nullable final Color color) {
        return (RED_DARK.equals(color) || RED_BRIGHT.equals(color));
    }

    public static boolean isGreen(@Nullable final Color color) {
        return (GREEN_DARK.equals(color) || GREEN_BRIGHT.equals(color));
    }

    public static boolean isBlue(@Nullable final Color color) {
        return (BLUE_DARK.equals(color) || BLUE_BRIGHT.equals(color));
    }

    public static boolean isDarkMode() {
        if (getCurrentSkin() == null)
            return false;
        return getCurrentSkin().isDark();
    }

    /**
     * Get skin by its name
     *
     * @param name the skin name.
     * @return the skin if found, otherwise returns null.
     */
    @Nullable
    public static FlatLaf getSkinByName(@NotNull final String name) {
        for (final FlatLaf skin : SKINS)
            if (skin.getName().equals(name))
                return skin;
        return null;
    }

    /**
     * Get skin by its classname
     *
     * @param className the skin classname in String.
     * @return the skin if found, otherwise returns null.
     */
    @Nullable
    public static FlatLaf getSkinByClassName(@NotNull final String className) {
        for (final FlatLaf skin : SKINS)
            if (skin.getClass().getName().equals(className))
                return skin;
        return null;
    }

    /**
     * @return the currently used skin. May be null if called before {@link #init()}.
     */
    @Nullable
    public static FlatLaf getCurrentSkin() {
        return currentSkin;
    }

    /**
     * @return the currently used skin display name. May be empty if called before {@link #init()}.
     * @see #getCurrentSkin()
     */
    @NotNull
    public static String getCurrentSkinName() {
        final FlatLaf skin = getCurrentSkin();
        return (skin != null) ? skin.getName() : "";
    }

    /**
     * @return the default skin name.
     */
    @NotNull
    public static String getDefaultSkinName() {
        return FlatLightLaf.NAME;
    }

    /**
     * @return the default font size.
     */
    public static int getDefaultFontSize() {
        return 13;
    }

    public static void setFontSize(final int size) {
        setUIFont(new FontUIResource("JetBrains Mono Medium", Font.TRUETYPE_FONT, size));
        fontSize = size;
        GeneralPreferences.setGuiFontSize(size);
    }

    public static int getFontSize() {
        return fontSize;
    }

    @Deprecated(forRemoval = true)
    public static float getDefaultIconSizeAsFloat() {
        return (float) getDefaultIconSize();
    }

    public static int getDefaultIconSize() {
        return 22;
    }

    /**
     * Switch to specified skin. Do nothing if new skin equals actual skin.
     *
     * @param skin the skin to use.
     * @see #fireSkinChangeListeners()
     */
    public static void setSkin(@NotNull final FlatLaf skin) {
        if ((getCurrentSkin() == null || !skin.getClass().equals(getCurrentSkin().getClass())) && SKINS.contains(skin)) {
            ThreadUtil.invokeLater(() -> {
                try {
                    GeneralPreferences.setGuiSkin(skin.getName());
                    UIManager.setLookAndFeel(skin);
                    if (fontInstalled)
                        setUIFont(new FontUIResource("JetBrains Mono Medium", Font.TRUETYPE_FONT, getFontSize()));
                    setUIDefaults(skin.getDefaults());
                    currentSkin = skin;

                    updateUI();
                }
                catch (final Exception e) {
                    IcyLogger.error(LookAndFeelUtil.class, e, "LookAndFeelUtil.setSkin(" + skin.getName() + ") error.");
                }
            });
        }
    }

    /**
     * Switch to specified skin name. Do nothing if new skin name equals actual skin name.
     *
     * @param skinName the skin name.
     * @see #setSkin(FlatLaf)
     */
    public static void setSkin(@NotNull final String skinName) {
        if (!StringUtil.equals(skinName, getCurrentSkinName()))
            try {
                for (final FlatLaf skin : SKINS)
                    if (skinName.equals(skin.getName()))
                        setSkin(skin);
            }
            catch (final Exception e) {
                IcyLogger.error(LookAndFeelUtil.class, e, "LookAndFeelUtil.setSkin(" + skinName + ") error.");
            }
    }

    public static void updateUI() {
        ThreadUtil.invokeLater(() -> {
            FlatLaf.updateUI();
            fireSkinChangeListeners();
        });
    }

    private static void setUIFont(final FontUIResource f) {
        final Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            final Object key = keys.nextElement();
            final Object value = UIManager.get(key);
            if (value instanceof FontUIResource)
                UIManager.put(key, f);
        }
    }

    /**
     * @param listener the listener to add.
     */
    public static void addListener(@NotNull final SkinChangeListener listener) {
        LISTENERS.add(listener);
    }

    /**
     * @param listener the listener to remove.
     */
    public static void removeListener(@NotNull final SkinChangeListener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Fire all the listeners.
     */
    private static void fireSkinChangeListeners() {
        for (final SkinChangeListener listener : LISTENERS)
            listener.skinChanged();
    }

    /* OLD FUNCTIONS */

    /**
     * Return the foreground color for the specified component
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @NotNull
    public static Color getForeground(@Nullable final Component c) {
        if (c != null)
            return c.getForeground();

        return getAccentForeground();
    }

    /**
     * Paint foreground component color in 'out' image<br>
     * depending original alpha intensity from 'alphaImage'
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Nullable
    public static Image paintForegroundImageFromAlphaImage(@Nullable final Component c, @Nullable final Image alphaImage, @Nullable final Image out) {
        return ImageUtil.paintColorImageFromAlphaImage(alphaImage, out, getForeground(c));
    }

    /**
     * Return the background color for the specified component
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Nullable
    public static Color getBackground(@Nullable final Component c) {
        if (c != null)
            return c.getBackground();

        //return Color.lightGray;
        if (getUIDefaults() != null)
            return getUIDefaults().getColor("Panel.background");
        else
            return null;
    }
}