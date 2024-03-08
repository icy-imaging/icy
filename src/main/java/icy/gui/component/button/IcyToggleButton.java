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

package icy.gui.component.button;

import icy.action.IcyAbstractAction;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcySVGIcon;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class IcyToggleButton extends JToggleButton {
    private @Nullable SVGIconPack iconPack;
    private @Nullable Icon iconDefault = null, iconDisabled = null, iconSelected = null, iconDisabledSelected = null;
    private final int iconSize;
    private boolean flat;

    // Constructors with text and SVGIcons

    /**
     * Create a {@link JToggleButton} with specified text, SVG icons for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIconPack pack, final int size, final boolean flat) {
        super(text);

        iconPack = pack;
        iconSize = size;
        setSVGIcons();

        this.flat = flat;
        init();
    }

    /**
     * Create a {@link JToggleButton} with specified text, SVG icons for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIconPack pack, final boolean flat) {
        this(text, pack, LookAndFeelUtil.getDefaultIconSize(), flat);
    }

    /**
     * Create a {@link JToggleButton} with specified text, SVG icons for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIconPack pack) {
        this(text, pack, LookAndFeelUtil.getDefaultIconSize(), false);
    }

    /**
     * Create a {@link JToggleButton} with specified text, same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIcon icon, final int size, final boolean flat) {
        this(text, new SVGIconPack(icon), size, flat);
    }

    /**
     * Create a {@link JToggleButton} with specified text, same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIcon icon, final int size) {
        this(text, new SVGIconPack(icon), size, false);
    }

    /**
     * Create a {@link JToggleButton} with specified text, same SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIcon icon, final boolean flat) {
        this(text, new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize(), flat);
    }

    /**
     * Create a {@link JToggleButton} with specified text, same SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull String text, final @NotNull SVGIcon icon) {
        this(text, new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize(), false);
    }

    // Constructors with SVGIcons but without text

    /**
     * Create a {@link JToggleButton} with specified SVG icons for each state (default, disabled, selected and disabled-selected) and custom size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIconPack pack, final int size, final boolean flat) {
        super();

        iconPack = pack;
        iconSize = size;
        setSVGIcons();

        this.flat = flat;
        init();
    }

    /**
     * Create a {@link JToggleButton} with specified SVG icons for each state (default, disabled, selected and disabled-selected) and default size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIconPack pack, final boolean flat) {
        this(pack, LookAndFeelUtil.getDefaultIconSize(), flat);
    }

    /**
     * Create a {@link JToggleButton} with specified SVG icons for each state (default, disabled, selected and disabled-selected) and default size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIconPack pack) {
        this(pack, LookAndFeelUtil.getDefaultIconSize(), false);
    }

    /**
     * Create a {@link JToggleButton} with same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIconPack pack, final int size) {
        this(pack, size, false);
    }

    /**
     * Create a {@link JToggleButton} with same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIcon icon, final int size, final boolean flat) {
        this(new SVGIconPack(icon), size, flat);
    }

    /**
     * Create a {@link JToggleButton} with same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIcon icon, final int size) {
        this(new SVGIconPack(icon), size, false);
    }

    /**
     * Create a {@link JToggleButton} with same SVG icon for each state (default, disabled, selected and disabled-selected) and default size, but without text.
     */
    public IcyToggleButton(final @NotNull SVGIcon icon) {
        this(new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize(), false);
    }

    // Constructors with Action and SVGIcons

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIconPack pack, final int size, final boolean flat) {
        super(action);
        iconPack = pack;
        iconSize = size;
        setSVGIcons();
        IcyAbstractAction.setToolTipTextFromAction(this, action);

        this.flat = flat;
        init();
    }

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIconPack pack, final boolean flat) {
        this(action, pack, LookAndFeelUtil.getDefaultIconSize(), flat);
    }

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIconPack pack) {
        this(action, pack, LookAndFeelUtil.getDefaultIconSize(), false);
    }

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIcon icon, final int size, final boolean flat) {
        this(action, new SVGIconPack(icon), size, flat);
    }

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, same SVG icon for each state (default, disabled, selected and disabled-selected) and custom size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIcon icon, final int size) {
        this(action, new SVGIconPack(icon), size, false);
    }

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, same SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIcon icon, final boolean flat) {
        this(action, new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize(), flat);
    }

    /**
     * Create a {@link JToggleButton} with specified {@link Action}, same SVG icon for each state (default, disabled, selected and disabled-selected) and default size.
     */
    public IcyToggleButton(final @NotNull Action action, final @NotNull SVGIcon icon) {
        this(action, new SVGIconPack(icon), LookAndFeelUtil.getDefaultIconSize(), false);
    }

    private void init() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);

        if (flat) {
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
        }
    }

    /**
     * @return the flat
     */
    public final boolean isFlat() {
        return flat;
    }

    /**
     * @param flat the flat to set
     */
    public final void setFlat(final boolean flat) {
        if (this.flat != flat) {
            this.flat = flat;

            setBorderPainted(!flat);
            setFocusPainted(!flat);
            setFocusable(!flat);
        }
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

        iconDefault = new IcySVGIcon(iconPack.getDefaultIcon(), LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_DEFAULT, iconSize);
        iconDisabled = new IcySVGIcon(iconPack.getDisabledIcon(), LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_DISABLED, iconSize);
        iconSelected = new IcySVGIcon(iconPack.getSelectedtIcon(), LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_SELECTED, iconSize);
        iconDisabledSelected = new IcySVGIcon(iconPack.getDisabledSelectedIcon(), LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_DISABLED, iconSize);

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
