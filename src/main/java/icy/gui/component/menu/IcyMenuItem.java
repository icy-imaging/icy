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

package icy.gui.component.menu;

import icy.action.IcyAbstractAction;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcyIconFont;
import icy.system.SystemUtil;
import jiconfont.IconCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Thomas MUSSET
 */
public class IcyMenuItem extends JMenuItem {
    public IcyMenuItem(@Nullable final String text, @NotNull final IconCode defaultIcon, @NotNull final IconCode disabledIcon, @NotNull final IconCode selectedIcon, final float size) {
        super(text);

        if (!SystemUtil.isMac() || (SystemUtil.isMac() && !System.getProperty("apple.laf.useScreenMenuBar").equals("true"))) {
            setIcon(new IcyIconFont(defaultIcon, size, LookAndFeelUtil.ColorType.UI_MENUITEM_DEFAULT));
            setDisabledIcon(new IcyIconFont(disabledIcon, size, LookAndFeelUtil.ColorType.UI_MENUITEM_DISABLED));
            setSelectedIcon(new IcyIconFont(selectedIcon, size, LookAndFeelUtil.ColorType.UI_MENUITEM_SELECTED));
        }
    }

    public IcyMenuItem(@Nullable final String text, @NotNull final IconCode defaultIcon, @NotNull final IconCode disabledIcon, @NotNull final IconCode selectedIcon) {
        this(text, defaultIcon, disabledIcon, selectedIcon, LookAndFeelUtil.getDefaultIconSizeAsFloat());
    }

    public IcyMenuItem(@NotNull final IconCode defaultIcon, @NotNull final IconCode disabledIcon, @NotNull final IconCode selectedIcon) {
        this(null, defaultIcon, disabledIcon, selectedIcon);
    }

    public IcyMenuItem(@NotNull final IconCode defaultIcon, @NotNull final IconCode disabledIcon, @NotNull final IconCode selectedIcon, final float size) {
        this(null, defaultIcon, disabledIcon, selectedIcon, size);
    }

    /**
     * Create a button with specified text and icon
     */
    public IcyMenuItem(@NotNull final String text, @NotNull final IconCode defaultIcon) {
        this(text, defaultIcon, defaultIcon, defaultIcon);
    }

    /**
     * Create a button with specified icon.
     */
    public IcyMenuItem(@NotNull final IconCode defaultIcon) {
        this(null, defaultIcon, defaultIcon, defaultIcon);
    }

    /**
     * Create a button with specified text and classic icon
     */
    public IcyMenuItem(@NotNull final String text, @NotNull final Icon icon) {
        super(text, icon);
    }

    /**
     * Create a menu with specified text.
     */
    public IcyMenuItem(@NotNull final String text) {
        super(text);
    }

    @Override
    public void setAction(@Nullable final Action a) {
        super.setAction(a);

        // override tooltip set from action
        IcyAbstractAction.setToolTipTextFromAction(this, a);
    }

    public void updateIconFont() {
        IcyIconFont.updateIcon(getIcon());
        IcyIconFont.updateIcon(getDisabledIcon());
        IcyIconFont.updateIcon(getSelectedIcon());

        /*final Icon i = getIcon();
        if (i instanceof IcyIconFont)
            ((IcyIconFont) i).updateIcon();
        final Icon di = getDisabledIcon();
        if (di instanceof IcyIconFont)
            ((IcyIconFont) di).updateIcon();
        final Icon si = getSelectedIcon();
        if (si instanceof IcyIconFont)
            ((IcyIconFont) si).updateIcon();*/
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateIconFont();
    }
}
