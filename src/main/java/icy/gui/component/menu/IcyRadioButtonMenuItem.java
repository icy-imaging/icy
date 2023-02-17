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
package icy.gui.component.menu;

import icy.action.IcyAbstractAction;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcyIconFont;
import jiconfont.IconCode;

import javax.swing.*;

/**
 * @author Thomas MUSSET
 */
public class IcyRadioButtonMenuItem extends JRadioButtonMenuItem {
    public IcyRadioButtonMenuItem(final String text, final IconCode defaultIcon, final IconCode disabledIcon, final IconCode selectedIcon, final float size) {
        super(text);

        setIcon(new IcyIconFont(defaultIcon, size, LookAndFeelUtil.ColorType.UI_MENUITEM_DEFAULT));
        setDisabledIcon(new IcyIconFont(disabledIcon, size, LookAndFeelUtil.ColorType.UI_MENUITEM_DISABLED));
        setSelectedIcon(new IcyIconFont(selectedIcon, size, LookAndFeelUtil.ColorType.UI_MENUITEM_SELECTED));
    }

    public IcyRadioButtonMenuItem(final String text, final IconCode defaultIcon, final IconCode disabledIcon, final IconCode selectedIcon) {
        this(text, defaultIcon, disabledIcon, selectedIcon, LookAndFeelUtil.getDefaultIconSizeAsFloat());
    }

    public IcyRadioButtonMenuItem(final IconCode defaultIcon, final IconCode disabledIcon, final IconCode selectedIcon) {
        this(null, defaultIcon, disabledIcon, selectedIcon);
    }

    public IcyRadioButtonMenuItem(final IconCode defaultIcon, final IconCode disabledIcon, final IconCode selectedIcon, final float size) {
        this(null, defaultIcon, disabledIcon, selectedIcon, size);
    }

    /**
     * Create a button with specified text and icon
     */
    public IcyRadioButtonMenuItem(final String text, final IconCode defaultIcon) {
        this(text, defaultIcon, defaultIcon, defaultIcon);
    }

    /**
     * Create a button with specified icon.
     */
    public IcyRadioButtonMenuItem(final IconCode defaultIcon) {
        this(null, defaultIcon, defaultIcon, defaultIcon);
    }

    /**
     * Create a button with specified text and classic icon
     */
    public IcyRadioButtonMenuItem(final String text, final Icon icon) {
        super(text, icon);
    }

    /**
     * Create a menu with specified text.
     */
    public IcyRadioButtonMenuItem(final String text) {
        super(text);
    }

    @Override
    public void setAction(Action a) {
        super.setAction(a);

        // override tooltip set from action
        IcyAbstractAction.setToolTipTextFromAction(this, a);
    }

    // TODO: 13/02/2023 Move to IconUtil
    public void updateIconFont() {
        final Icon i = getIcon();
        if (i instanceof IcyIconFont)
            ((IcyIconFont) i).updateIcon();
        final Icon di = getDisabledIcon();
        if (di instanceof IcyIconFont)
            ((IcyIconFont) di).updateIcon();
        final Icon si = getSelectedIcon();
        if (si instanceof IcyIconFont)
            ((IcyIconFont) si).updateIcon();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateIconFont();
    }
}
