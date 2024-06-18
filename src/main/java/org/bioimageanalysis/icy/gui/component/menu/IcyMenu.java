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

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIconPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Thomas Musset
 */
public class IcyMenu extends JMenu {
    private @Nullable SVGIconPack iconPack;
    private @Nullable Icon iconDefault = null, iconDisabled = null, iconSelected = null, iconDisabledSelected = null;
    private final int iconSize;

    /**
     * Create a {@link JMenu} with specified text, SVG icons for each state (default, disabled and selected) and custom size.
     */
    public IcyMenu(final @NotNull String text, final @NotNull SVGIconPack pack, final int size) {
        super(text);
        iconPack = pack;
        iconSize = size;
        setSVGIcons();
    }

    /**
     * Create a {@link JMenu} with specified text, SVG icons for each state (default, disabled and selected) and default size.
     */
    public IcyMenu(final @NotNull String text, final @NotNull SVGIconPack pack) {
        this(text, pack, LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Create a {@link JMenu} with specified text, same SVG icon for each state (default, disabled and selected) and custom size.
     */
    public IcyMenu(final @NotNull String text, final @NotNull SVGIcon icon, final int size) {
        this(text, new SVGIconPack(icon), size);
    }

    /**
     * Create a {@link JMenu} with specified text, same SVG icon for each state (default, disabled and selected) and default size.
     */
    public IcyMenu(final @NotNull String text, final @NotNull SVGIcon icon) {
        this(text, new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Create a {@link JMenu} with specified SVG icons for each state (default, disabled and selected) and custom size, but without text.
     */
    public IcyMenu(final @NotNull SVGIconPack pack, final int size) {
        super();
        iconPack = pack;
        iconSize = size;
        setSVGIcons();
    }

    /**
     * Create a {@link JMenu} with specified SVG icons for each state (default, disabled and selected) and default size, but without text.
     */
    public IcyMenu(final @NotNull SVGIconPack pack) {
        this(pack, LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Create a {@link JMenu} with same SVG icon for each state (default, disabled and selected) and custom size, but without text.
     */
    public IcyMenu(final @NotNull SVGIcon icon, final int size) {
        this(new SVGIconPack(icon), size);
    }

    /**
     * Create a {@link JMenu} with same SVG icon for each state (default, disabled and selected) and default size, but without text.
     */
    public IcyMenu(final @NotNull SVGIcon icon) {
        this(new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize());
    }

    protected final void setSVGIconPack(final @NotNull SVGIconPack pack) {
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

        iconDefault = new IcySVGIcon(iconPack.getDefaultIcon(), LookAndFeelUtil.ColorType.UI_MENUITEM_DEFAULT, iconSize);
        iconDisabled = new IcySVGIcon(iconPack.getDisabledIcon(), LookAndFeelUtil.ColorType.UI_MENUITEM_DISABLED, iconSize);
        iconSelected = new IcySVGIcon(iconPack.getSelectedtIcon(), LookAndFeelUtil.ColorType.UI_MENUITEM_SELECTED, iconSize);
        iconDisabledSelected = new IcySVGIcon(iconPack.getDisabledSelectedIcon(), LookAndFeelUtil.ColorType.UI_MENUITEM_DISABLED, iconSize);

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
