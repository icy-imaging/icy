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
import fr.icy.system.logging.IcyLogger;
import fr.icy.system.preferences.GeneralPreferences;
import fr.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * LookAndFeelUtil class. Use to install all skins used by Icy and helps manipulating UI manager.
 *
 * @author Thomas Musset
 */
public final class LookAndFeelUtil {
    private static final @NotNull Set<SkinChangeListener> LISTENERS = new HashSet<>();
    private static final @NotNull List<FlatLaf> SKINS = new ArrayList<>();
    private static @Nullable FlatLaf currentSkin = null;
    private static @Nullable UIDefaults defaults = null;
    private static boolean fontInstalled = false;
    private static @Nullable String fontFamily = null;
    private static int fontSize = getDefaultFontSize();

    private static final @NotNull Color RED_DARK = new Color(200, 64, 64);
    private static final @NotNull Color RED_BRIGHT = new Color(255, 128, 128);
    private static final @NotNull Color GREEN_DARK = new Color(64, 128, 64);
    private static final @NotNull Color GREEN_BRIGHT = new Color(128, 255, 128);
    private static final @NotNull Color BLUE_DARK = new Color(0, 85, 174);
    private static final @NotNull Color BLUE_BRIGHT = new Color(157, 205, 255);
    private static final @NotNull Color YELLOW_DARK = new Color(200, 100, 0);
    private static final @NotNull Color YELLOW_BRIGHT = new Color(255, 150, 0);

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

        final @NotNull String type;

        @Contract(pure = true)
        ColorType(final @NotNull String s) {
            type = s;
        }
    }

    /**
     * Initializes the application's Look and Feel settings, including platform-specific configurations,
     * custom font registrations, and theme management.
     * <p>
     * This method handles the following:
     * 1. Configures Look and Feel decorations based on the operating system.
     * 2. Sets platform-specific system properties for menu bar behavior.
     * 3. Registers custom fonts (regular, bold, and italic) if available; logs warnings if registration fails.
     * 4. Adds default and extra themes to the skin repository.
     * 5. Sorts the themes to display light skins before dark ones.
     * 6. Applies saved preferences for the selected theme and font size.
     * 7. Configures additional UIManager settings, such as split-pane divider appearance.
     * <p>
     * If custom fonts fail to load, it logs a warning and sets {@code fontInstalled} to {@code false}.
     * Otherwise, it sets {@code fontInstalled} to {@code true}.
     * <p>
     * Note: This method should only be called once during application startup to ensure proper UI initialization.
     */
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
        for (final String ffn : ge.getAvailableFontFamilyNames()) {
            if (ffn.equalsIgnoreCase("0xProto")) {
                fontFamily = ffn;
                fontInstalled = true;
                IcyLogger.debug(LookAndFeelUtil.class, "Font already installed: " + fontFamily);
                break;
            }
        }

        if (fontFamily == null) {
            boolean ttfRegular = false;
            try (final InputStream is = Icy.class.getResourceAsStream("/fonts/0xProto-Regular.ttf")) {
                if (is != null) {
                    final Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                    if (ge.registerFont(f)) {
                        ttfRegular = true;
                        fontFamily = f.getFamily(Locale.getDefault());
                    }
                }
            }
            catch (final IOException | FontFormatException e) {
                IcyLogger.warn(LookAndFeelUtil.class, e, "Unable to load regular font.");
            }

            // Registers bold and italic fonts if regular loaded
            if (ttfRegular) {
                try (final InputStream is = Icy.class.getResourceAsStream("/fonts/0xProto-Bold.ttf")) {
                    if (is != null)
                        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                }
                catch (final IOException | FontFormatException e) {
                    IcyLogger.warn(LookAndFeelUtil.class, e, "Unable to load bold font.");
                }

                try (final InputStream is = Icy.class.getResourceAsStream("/fonts/0xProto-Italic.ttf")) {
                    if (is != null)
                        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                }
                catch (final IOException | FontFormatException e) {
                    IcyLogger.warn(LookAndFeelUtil.class, e, "Unable to load italic font.");
                }
            }

            if (ttfRegular) {
                fontInstalled = true;
            }
            else {
                fontInstalled = false;
                IcyLogger.warn(LookAndFeelUtil.class, "Fonts not installed. Some graphical elements will be broken.");
            }
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

        UIManager.put("SplitPaneDivider.gripDotCount", 1);
    }

    private static void addSkin(final @NotNull FlatLaf skin) {
        SKINS.add(skin);
    }

    public static @Nullable String getFontFamily() {
        return fontFamily;
    }

    /**
     * @return the skins list
     */
    @Contract(pure = true)
    public static @NotNull List<FlatLaf> getSkins() {
        return SKINS;
    }

    private static void setUIDefaults(final @Nullable UIDefaults d) {
        defaults = d;
    }

    @Contract(pure = true)
    public static @Nullable UIDefaults getUIDefaults() {
        return defaults;
    }

    public static @Nullable Color getUIColor(final @NotNull ColorType colorType) {
        if (defaults == null)
            return null;
        return defaults.getColor(colorType.type);
    }

    public static @NotNull Color getAccentForeground() {
        if (isDarkMode())
            return Color.WHITE;
        else
            return Color.BLACK;
    }

    public static @NotNull Color getRed() {
        if (isDarkMode())
            return RED_DARK;
        else
            return RED_BRIGHT;
    }

    public static @NotNull Color getGreen() {
        if (isDarkMode())
            return GREEN_DARK;
        else
            return GREEN_BRIGHT;
    }

    public static @NotNull Color getBlue() {
        if (isDarkMode())
            return BLUE_DARK;
        else
            return BLUE_BRIGHT;
    }

    public static @NotNull Color getYellow() {
        if (isDarkMode())
            return YELLOW_DARK;
        else
            return YELLOW_BRIGHT;
    }

    public static boolean isRed(final @Nullable Color color) {
        return (RED_DARK.equals(color) || RED_BRIGHT.equals(color));
    }

    public static boolean isGreen(final @NotNull Color color) {
        return (GREEN_DARK.equals(color) || GREEN_BRIGHT.equals(color));
    }

    public static boolean isBlue(final @NotNull Color color) {
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
    public static @Nullable FlatLaf getSkinByName(final @NotNull String name) {
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
    public static @Nullable FlatLaf getSkinByClassName(final @NotNull String className) {
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
    public static @NotNull String getCurrentSkinName() {
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
    @Contract(pure = true)
    public static int getDefaultFontSize() {
        return 13;
    }

    public static void setFontSize(final int size) {
        setUIFont(new FontUIResource(fontFamily, Font.PLAIN, size));
        fontSize = size;
        GeneralPreferences.setGuiFontSize(size);
    }

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
    public static void setSkin(final @NotNull FlatLaf skin) {
        if ((getCurrentSkin() == null || !skin.getClass().equals(getCurrentSkin().getClass())) && SKINS.contains(skin)) {
            ThreadUtil.invokeLater(() -> {
                try {
                    GeneralPreferences.setGuiSkin(skin.getName());
                    UIManager.setLookAndFeel(skin);
                    if (fontInstalled)
                        setUIFont(new FontUIResource(fontFamily, Font.PLAIN, getFontSize()));
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
    public static void setSkin(final @NotNull String skinName) {
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

    private static @NotNull String @NotNull [] UiDefaultsFontKeys() {
        return new String[]{
                "EditorPane.font",
                "Button.font",
                "MenuBar.font",
                "Menu.acceleratorFont",
                "ComboBox.font",
                "Label.font",
                "InternalFrame.optionDialogTitleFont",
                "InternalFrame.paletteTitleFont",
                "Spinner.font",
                "Table.font",
                "TextField.font",
                "TableHeader.font",
                "Slider.font",
                "List.font",
                "RadioButton.font",
                "MenuItem.font",
                "FormattedTextField.font",
                "OptionPane.messageFont",
                "RadioButtonMenuItem.font",
                "MenuItem.acceleratorFont",
                "PopupMenu.font",
                "Panel.font",
                "CheckBox.font",
                "ToggleButton.font",
                "ScrollPane.font",
                "InternalFrame.titleFont",
                "RadioButtonMenuItem.acceleratorFont",
                "Tree.font",
                "List.font",
                "Slider.font",
                "CheckBox.font",
                "Table.font",
                "RadioButtonMenuItem.font",
                "TextPane.font",
                "large.font",
                "PopupMenu.font",
                "PasswordField.font",
                "Slider.font",
                "CheckBox.font",
                "TabbedPane.font",
                "ColorChooser.font",
                "Viewport.font",
                "ProgressBar.font",
                "CheckBoxMenuItem.acceleratorFont",
                "ToolTip.font",
                "ToolBar.font",
                "TextPane.font",
                "List.font",
                "h00.font",
                "PopupMenu.font",
                "h1.regular.font",
                "TipOfTheDay.font",
                "RadioButtonMenuItem.font",
                "Table.font",
                "monospaced.font",
                "TitlePane.font",
                "Menu.font",
                "TitlePane.small.font",
                "InternalFrame.optionDialogTitleFont",
                "JXHeader.descriptionFont",
                "TextArea.font",
                "TitledBorder.font",
                "OptionPane.font",
                "TextArea.font",
                "Spinner.font",
                "TextField.font",
                "CheckBoxMenuItem.font",
                "CheckBoxMenuItem.font",
                "Spinner.font",
                "TextField.font",
                "light.font",
                "h3.font",
                "defaultFont",
                "h3.regular.font",
                "RadioButtonMenuItem.acceleratorFont",
                "FormattedTextField.font",
                "ToolTip.font",
                "mini.font",
                "OptionPane.font",
                "FormattedTextField.font",
                "h4.font",
                "RadioButtonMenuItem.acceleratorFont",
                "TableHeader.font",
                "MenuItem.font",
                "ColorChooser.font",
                "TaskPane.font",
                "MenuItem.acceleratorFont",
                "ScrollPane.font",
                "ToggleButton.font",
                "JXMonthView.font",
                "PasswordField.font",
                "h1.font",
                "TableHeader.font",
                "RootPane.font",
                "MenuItem.acceleratorFont",
                "Viewport.font",
                "ToggleButton.font",
                "ScrollPane.font",
                "MenuItem.font",
                "h2.font",
                "JXHeader.titleFont",
                "TabbedPane.font",
                "small.font",
                "Menu.acceleratorFont",
                "ToolBar.font",
                "MenuBar.font",
                "TitledBorder.font",
                "InternalFrame.paletteTitleFont",
                "Menu.acceleratorFont",
                "MenuBar.font",
                "Button.font",
                "EditorPane.font",
                "Menu.font",
                "Label.font",
                "ComboBox.font",
                "Tree.font",
                "EditorPane.font",
                "Button.font",
                "h2.regular.font",
                "Tree.font",
                "Label.font",
                "h0.font",
                "ComboBox.font",
                "RadioButton.font",
                "medium.font",
                "InternalFrame.titleFont",
                "OptionPane.messageFont",
                "ProgressBar.font",
                "JXTitledPanel.titleFont",
                "Panel.font",
                "CheckBoxMenuItem.acceleratorFont",
                "semibold.font",
                "RadioButton.font",
                "InternalFrame.titleFont",
                "Panel.font"
        };
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
    public static void addListener(final @NotNull SkinChangeListener listener) {
        LISTENERS.add(listener);
    }

    /**
     * @param listener the listener to remove.
     */
    public static void removeListener(final @NotNull SkinChangeListener listener) {
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