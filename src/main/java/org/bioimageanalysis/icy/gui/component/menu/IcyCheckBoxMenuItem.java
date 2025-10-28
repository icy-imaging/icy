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

package org.bioimageanalysis.icy.gui.component.menu;

import org.bioimageanalysis.icy.gui.action.IcyAbstractAction;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.gui.component.icon.IcyIconPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Thomas Musset
 */
public class IcyCheckBoxMenuItem extends JCheckBoxMenuItem {
    private @Nullable IcyIconPack iconPack;
    private @Nullable Icon iconDefault = null, iconDisabled = null, iconSelected = null, iconDisabledSelected = null;
    private final int iconSize;

    // Constructors with text and SVGIcons

    /**
     * Create a {@link JCheckBoxMenuItem} with specified text, SVG icons for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyCheckBoxMenuItem(final @NotNull String text, final @NotNull IcyIconPack pack, final int size) {
        super(text);
        iconPack = pack;
        iconSize = size;
        setSVGIcons();
    }

    /**
     * Create a {@link JCheckBoxMenuItem} with specified text, SVG icons for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyCheckBoxMenuItem(final @NotNull String text, final @NotNull IcyIconPack pack) {
        this(text, pack, LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Create a {@link JCheckBoxMenuItem} with specified text, same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyCheckBoxMenuItem(final @NotNull String text, final @NotNull SVGResource icon, final int size) {
        this(text, new IcyIconPack(icon), size);
    }

    /**
     * Create a {@link JCheckBoxMenuItem} with specified text, same SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyCheckBoxMenuItem(final @NotNull String text, final @NotNull SVGResource icon) {
        this(text, new IcyIconPack(icon), LookAndFeelUtil.getDefaultIconSize());
    }

    // Constructors with SVGIcons but without text

    /**
     * Create a {@link JCheckBoxMenuItem} with specified SVG icons for each state (default, disabled, selected and disabled-selected) and custom size, but without text.
     */
    public IcyCheckBoxMenuItem(final @NotNull IcyIconPack pack, final int size) {
        super();
        iconPack = pack;
        iconSize = size;
        setSVGIcons();
    }

    /**
     * Create a {@link JCheckBoxMenuItem} with specified SVG icons for each state (default, disabled, selected and disabled-selected) and default size, but without text.
     */
    public IcyCheckBoxMenuItem(final @NotNull IcyIconPack pack) {
        this(pack, LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Create a {@link JCheckBoxMenuItem} with same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size, but without text.
     */
    public IcyCheckBoxMenuItem(final @NotNull SVGResource icon, final int size) {
        this(new IcyIconPack(icon), size);
    }

    /**
     * Create a {@link JCheckBoxMenuItem} with same SVG icon for each state (default, disabled, selected and disabled-selected) and default size, but without text.
     */
    public IcyCheckBoxMenuItem(final @NotNull SVGResource icon) {
        this(new IcyIconPack(icon), LookAndFeelUtil.getDefaultIconSize());
    }

    // Constructors with Action and SVGIcons

    /**
     * Create a {@link JRadioButtonMenuItem} with specified {@link Action}, SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyCheckBoxMenuItem(final @NotNull Action action, final @NotNull IcyIconPack pack, final int size) {
        super(action);
        iconPack = pack;
        iconSize = size;
        setSVGIcons();
        IcyAbstractAction.setToolTipTextFromAction(this, action);
    }

    /**
     * Create a {@link JRadioButtonMenuItem} with specified {@link Action}, SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyCheckBoxMenuItem(final @NotNull Action action, final @NotNull IcyIconPack pack) {
        this(action, pack, LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Create a {@link JRadioButtonMenuItem} with specified {@link Action}, same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyCheckBoxMenuItem(final @NotNull Action action, final @NotNull SVGResource icon, final int size) {
        this(action, new IcyIconPack(icon), size);
    }

    /**
     * Create a {@link JRadioButtonMenuItem} with specified {@link Action}, same SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyCheckBoxMenuItem(final @NotNull Action action, final @NotNull SVGResource icon) {
        this(action, new IcyIconPack(icon), LookAndFeelUtil.getDefaultIconSize());
    }

    // Constructors with Action but without SVGIcons

    /**
     * Create a {@link JRadioButtonMenuItem} with specified {@link Action} but without custom icon.
     */
    public IcyCheckBoxMenuItem(final @NotNull Action action) {
        super(action);
        iconPack = null;
        iconSize = LookAndFeelUtil.getDefaultIconSize();
        resetIcons();
        IcyAbstractAction.setToolTipTextFromAction(this, action);
    }

    protected final void setSVGIconPack(final @NotNull IcyIconPack pack) {
        this.iconPack = pack;
        setSVGIcons();
    }

    /**
     * Set icon for each state (default, disabled, selected and disabled-selected).<br>
     * Internal use only.
     */
    protected final void setIcons(final @NotNull Icon icon, final @NotNull Icon disabled, final @NotNull Icon selected, final @NotNull Icon disabledSelected) {
        iconDefault = icon;
        iconDisabled = disabled;
        iconSelected = selected;
        iconDisabledSelected = disabledSelected;

        resetIcons();
    }

    /**
     * Set icon for each state (default, disabled, selected and disabled-selected).<br>
     * Internal use only.
     */
    protected final void setIcons(final @NotNull Icon icon, final @NotNull Icon selected) {
        setIcons(icon, icon, selected, selected);
    }

    /**
     * Set icon for each state (default, disabled, selected and disabled-selected).<br>
     * Internal use only.
     */
    protected final void setIcons(final @NotNull Icon icon) {
        setIcons(icon, icon, icon, icon);
    }

    /**
     * Set SVG icon for each state (default, disabled, selected and disabled-selected).<br>
     * Internal use only.
     */
    protected final void setSVGIcons() {
        if (iconPack == null)
            return;

        iconDefault = new IcySVG(iconPack.getDefaultIcon()).getIcon(iconSize, LookAndFeelUtil.ColorType.MENUITEM_DEFAULT);
        iconDisabled = new IcySVG(iconPack.getDisabledIcon()).getIcon(iconSize, LookAndFeelUtil.ColorType.MENUITEM_DISABLED);
        iconSelected = new IcySVG(iconPack.getSelectedtIcon()).getIcon(iconSize, LookAndFeelUtil.ColorType.MENUITEM_SELECTED);
        iconDisabledSelected = new IcySVG(iconPack.getDisabledSelectedIcon()).getIcon(iconSize, LookAndFeelUtil.ColorType.MENUITEM_DISABLED);

        resetIcons();
    }

    /**
     * Reset all icons (useful when Action overrides icons).<br>
     * Internal use only.
     */
    protected final void resetIcons() {
        setIcon(iconDefault);
        setDisabledIcon(iconDisabled);
        setSelectedIcon(iconSelected);
        setDisabledSelectedIcon(iconDisabledSelected);
    }
}
