/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import fr.icy.Icy;
import fr.icy.common.string.StringUtil;
import fr.icy.gui.listener.SkinChangeListener;
import fr.icy.system.SystemUtil;
import fr.icy.system.preferences.GeneralPreferences;
import fr.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LookAndFeelUtil class. Use to install all skins used by Icy and helps manipulating UI manager.
 *
 * @author Thomas Musset
 */
public final class LookAndFeelUtil {
    private static final Logger LOGGER = Logger.getLogger(LookAndFeelUtil.class.getName());

    private static final @NonNull Set<SkinChangeListener> LISTENERS = new HashSet<>();
    private static final @NonNull List<FlatLaf> SKINS = new ArrayList<>();
    private static @Nullable FlatLaf currentSkin = null;
    private static @Nullable UIDefaults defaults = null;
    private static boolean fontInstalled = false;
    private static int fontSize = getDefaultFontSize();

    private static final @NonNull Color RED_DARK = new Color(200, 64, 64);
    private static final @NonNull Color RED_BRIGHT = new Color(255, 128, 128);
    private static final @NonNull Color GREEN_DARK = new Color(64, 128, 64);
    private static final @NonNull Color GREEN_BRIGHT = new Color(128, 255, 128);
    private static final @NonNull Color BLUE_DARK = new Color(0, 85, 174);
    private static final @NonNull Color BLUE_BRIGHT = new Color(157, 205, 255);
    private static final @NonNull Color YELLOW_DARK = new Color(200, 100, 0);
    private static final @NonNull Color YELLOW_BRIGHT = new Color(255, 150, 0);

    public enum ColorType {
        BUTTON_DEFAULT("Button.foreground"),
        BUTTON_DISABLED("Button.disabledText"),
        BUTTON_SELECTED("Button.selectedForeground"),
        TOGGLEBUTTON_DEFAULT("ToggleButton.foreground"),
        TOGGLEBUTTON_DISABLED("ToggleButton.disabledText"),
        TOGGLEBUTTON_SELECTED("ToggleButton.selectedForeground"),
        MENU_DEFAULT("Menu.foreground"),
        MENU_DISABLED("Menu.disabledForeground"),
        MENU_SELECTED("Menu.selectionForeground"),
        MENUITEM_DEFAULT("MenuItem.foreground"),
        MENUITEM_DISABLED("MenuItem.disabledForeground"),
        MENUITEM_SELECTED("MenuItem.selectionForeground");

        final @NonNull String type;

        @Contract(pure = true)
        ColorType(final @NonNull String s) {
            type = s;
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

        // TODO: replace with flatlaf font installer
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        boolean ttfMedium = false;
        try (final InputStream is = Icy.class.getResourceAsStream("/fonts/JetBrainsMono-Medium.ttf")) {
            if (is != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                ttfMedium = true;
            }
        }
        catch (final IOException | FontFormatException e) {
            LOGGER.log(Level.WARNING, "Unable to install default font.", e);
        }

        boolean ttfBold = false;
        if (ttfMedium) {
            try (final InputStream is = Icy.class.getResourceAsStream("/fonts/JetBrainsMono-Bold.ttf")) {
                if (is != null) {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                    ttfBold = true;
                }
            }
            catch (final IOException | FontFormatException e) {
                LOGGER.log(Level.WARNING, "Unable to install bold font.", e);
            }
        }

        if (ttfMedium) {
            fontInstalled = true;
            if (!ttfBold)
                LOGGER.warning("Bold font not installed. Using only default font.");
        }
        else {
            fontInstalled = false;
            LOGGER.warning("Default font not installed. Some graphical elements will be broken.");
        }

        // Default themes
        addSkin(new FlatLightLaf());
        addSkin(new FlatDarkLaf());

        // Extra themes
        //addSkin(new FlatArcIJTheme());
        //addSkin(new FlatArcOrangeIJTheme());
        //addSkin(new FlatArcDarkIJTheme());
        //addSkin(new FlatArcDarkOrangeIJTheme());
        //addSkin(new FlatCarbonIJTheme());
        //addSkin(new FlatCobalt2IJTheme());
        //addSkin(new FlatCyanLightIJTheme());
        //addSkin(new FlatDarkFlatIJTheme());
        //addSkin(new FlatDarkPurpleIJTheme());
        //addSkin(new FlatDraculaIJTheme());
        //addSkin(new FlatGradiantoDarkFuchsiaIJTheme());
        //addSkin(new FlatGradiantoDeepOceanIJTheme());
        //addSkin(new FlatGradiantoMidnightBlueIJTheme());
        //addSkin(new FlatGradiantoNatureGreenIJTheme());
        //addSkin(new FlatGrayIJTheme());
        //addSkin(new FlatGruvboxDarkHardIJTheme());
        //addSkin(new FlatHiberbeeDarkIJTheme());
        //addSkin(new FlatHighContrastIJTheme());
        //addSkin(new FlatLightFlatIJTheme());
        //addSkin(new FlatMaterialDesignDarkIJTheme());
        //addSkin(new FlatMonocaiIJTheme());
        //addSkin(new FlatMonokaiProIJTheme());
        //addSkin(new FlatNordIJTheme());
        //addSkin(new FlatOneDarkIJTheme());
        //addSkin(new FlatSolarizedDarkIJTheme());
        //addSkin(new FlatSolarizedLightIJTheme());
        //addSkin(new FlatSpacegrayIJTheme());
        //addSkin(new FlatVuesionIJTheme());
        //addSkin(new FlatXcodeDarkIJTheme());

        // sort skin to put light skins first
        SKINS.sort((o1, o2) -> Boolean.compare(o1.isDark(), o2.isDark()));

        // get saved skin, if not found, get default skin and save it
        setSkin(GeneralPreferences.getGuiSkin());
        setFontSize(GeneralPreferences.getGuiFontSize());

        UIManager.put("SplitPaneDivider.gripDotCount", 3);
        //UIManager.put("SplitPaneDivider.gripDotSize", 5);
    }

    private static void addSkin(final @NonNull FlatLaf skin) {
        SKINS.add(skin);
    }

    /**
     * @return the skins list
     */
    @Contract(pure = true)
    public static @NonNull List<FlatLaf> getSkins() {
        return SKINS;
    }

    private static void setUIDefaults(final @Nullable UIDefaults d) {
        defaults = d;
    }

    @Contract(pure = true)
    public static @Nullable UIDefaults getUIDefaults() {
        return defaults;
    }

    public static @Nullable Color getUIColor(final @NonNull ColorType colorType) {
        if (defaults == null)
            return null;
        return defaults.getColor(colorType.type);
    }

    public static @NonNull Color getAccentForeground() {
        if (isDarkMode())
            return Color.WHITE;
        else
            return Color.BLACK;
    }

    public static @NonNull Color getRed() {
        if (isDarkMode())
            return RED_DARK;
        else
            return RED_BRIGHT;
    }

    public static @NonNull Color getGreen() {
        if (isDarkMode())
            return GREEN_DARK;
        else
            return GREEN_BRIGHT;
    }

    public static @NonNull Color getBlue() {
        if (isDarkMode())
            return BLUE_DARK;
        else
            return BLUE_BRIGHT;
    }

    public static @NonNull Color getYellow() {
        if (isDarkMode())
            return YELLOW_DARK;
        else
            return YELLOW_BRIGHT;
    }

    public static boolean isRed(final @Nullable Color color) {
        return (RED_DARK.equals(color) || RED_BRIGHT.equals(color));
    }

    public static boolean isGreen(final @NonNull Color color) {
        return (GREEN_DARK.equals(color) || GREEN_BRIGHT.equals(color));
    }

    public static boolean isBlue(final @NonNull Color color) {
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
    public static @Nullable FlatLaf getSkinByName(final @NonNull String name) {
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
    public static @Nullable FlatLaf getSkinByClassName(final @NonNull String className) {
        for (final FlatLaf skin : SKINS)
            if (skin.getClass().getName().equals(className))
                return skin;
        return null;
    }

    /**
     * @return the currently used skin. May be null if called before {@link #init()}.
     */
    @Contract(pure = true)
    public static @Nullable FlatLaf getCurrentSkin() {
        return currentSkin;
    }

    /**
     * @return the currently used skin display name. May be empty if called before {@link #init()}.
     * @see #getCurrentSkin()
     */
    public static @NonNull String getCurrentSkinName() {
        final FlatLaf skin = getCurrentSkin();
        return (skin != null) ? skin.getName() : "";
    }

    /**
     * @return the default skin name.
     */
    @Contract(pure = true)
    public static @NonNull String getDefaultSkinName() {
        return FlatLightLaf.NAME;
    }

    /**
     * @return the default font size.
     */
    @Contract(pure = true)
    public static int getDefaultFontSize() {
        return 13;
    }

    public static void setFontSize(final int size) {
        setUIFont(new FontUIResource("JetBrains Mono Medium", Font.TRUETYPE_FONT, size));
        fontSize = size;
        GeneralPreferences.setGuiFontSize(size);
    }

    @Contract(pure = true)
    public static int getFontSize() {
        return fontSize;
    }

    @Contract(pure = true)
    public static int getDefaultIconSize() {
        return 22;
    }

    /**
     * Switch to specified skin. Do nothing if new skin equals actual skin.
     *
     * @param skin the skin to use.
     * @see #fireSkinChangeListeners()
     */
    public static void setSkin(final @NonNull FlatLaf skin) {
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
                    LOGGER.log(Level.SEVERE, "LookAndFeelUtil.setSkin(" + skin.getName() + ") error.", e);
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
    public static void setSkin(final @NonNull String skinName) {
        if (!StringUtil.equals(skinName, getCurrentSkinName()))
            try {
                for (final FlatLaf skin : SKINS)
                    if (skinName.equals(skin.getName()))
                        setSkin(skin);
            }
            catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "LookAndFeelUtil.setSkin(" + skinName + ") error.", e);
            }
    }

    public static void updateUI() {
        ThreadUtil.invokeLater(() -> {
            FlatLaf.updateUI();
            fireSkinChangeListeners();
        });
    }

    private static void setUIFont(final @Nullable FontUIResource f) {
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
    public static void addListener(final @NonNull SkinChangeListener listener) {
        LISTENERS.add(listener);
    }

    /**
     * @param listener the listener to remove.
     */
    public static void removeListener(final @NonNull SkinChangeListener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Fire all the listeners.
     */
    private static void fireSkinChangeListeners() {
        for (final SkinChangeListener listener : LISTENERS)
            listener.skinChanged();
    }
}