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
package icy.gui.component.button;

import icy.action.IcyAbstractAction;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcyIcon;
import icy.resource.icon.IcyIconFont;
import jiconfont.IconCode;

import javax.swing.*;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class IcyToggleButton extends JToggleButton {
    private boolean flat;

    public IcyToggleButton(final String text, final IconCode defaultIcon, final IconCode selectedIcon, final float size) {
        super(text);

        setIcon(new IcyIconFont(defaultIcon, size, LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_DEFAULT));
        setDisabledIcon(new IcyIconFont(defaultIcon, size, LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_DISABLED));
        setSelectedIcon(new IcyIconFont(selectedIcon, size, LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_SELECTED));
        setDisabledSelectedIcon(new IcyIconFont(selectedIcon, size, LookAndFeelUtil.ColorType.UI_TOGGLEBUTTON_DISABLED));

        flat = false;
        init();
    }

    public IcyToggleButton(final String text, final IconCode defaultIcon, final IconCode selectedIcon) {
        this(text, defaultIcon, selectedIcon, LookAndFeelUtil.getDefaultIconSizeAsFloat());
    }

    /**
     * Create a button with specified text and icon
     */
    public IcyToggleButton(final String text, final IconCode icon) {
        this(text, icon, icon);
    }

    /**
     * Create a button with specified icon
     */
    public IcyToggleButton(final IconCode defaultIcon, final IconCode selectedIcon) {
        this(null, defaultIcon, selectedIcon);
    }

    /**
     * Create a button with specified icon size
     */
    public IcyToggleButton(final IconCode defaultIcon, final IconCode selectedIcon, final float size) {
        this(null, defaultIcon, selectedIcon, size);
    }

    /**
     * Create a button with specified icon.
     */
    public IcyToggleButton(final IconCode icon) {
        this(null, icon, icon);
    }

    /**
     * Create a button with specified text and classic icon
     */
    public IcyToggleButton(final String text, final Icon icon) {
        super(text, icon);
        flat = false;
        init();
    }

    /**
     * Create a button with specified icon.
     */
    public IcyToggleButton(final Icon icon) {
        this(null, icon);
    }

    /**
     * Create a button with specified text.
     */
    public IcyToggleButton(final String text) {
        super(text);
        flat = false;
        init();
    }

    /**
     * Create a button with specified action.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public IcyToggleButton(final IcyAbstractAction action) {
        super(action);
        flat = false;
        init();
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

    @Override
    public void setAction(Action a) {
        super.setAction(a);

        // override tooltip set from action
        IcyAbstractAction.setToolTipTextFromAction(this, a);
    }

    /**
     * @return the flat
     */
    public boolean isFlat() {
        return flat;
    }

    /**
     * @param flat the flat to set
     */
    public void setFlat(boolean flat) {
        if (this.flat != flat) {
            this.flat = flat;

            setBorderPainted(!flat);
            setFocusPainted(!flat);
            setFocusable(!flat);
        }
    }

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

    /**
     * Return the icon as IcyIcon
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public IcyIcon getIcyIcon() {
        final Icon icon = getIcon();

        if (icon instanceof IcyIcon)
            return (IcyIcon) icon;

        return null;
    }

    /**
     * @return the icon name
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public String getIconName() {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            return icon.getName();

        return null;
    }

    /**
     * @param iconName the iconName to set
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setIconName(String iconName) {
        final IcyIcon icon = getIcyIcon();

        if (icon != null) {
            icon.setName(iconName);
        }
    }

    /**
     * @return the icon size
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public int getIconSize() {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            return icon.getSize();

        return -1;
    }

    /**
     * @param iconSize the iconSize to set
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setIconSize(int iconSize) {
        final IcyIcon icon = getIcyIcon();

        if (icon != null) {
            icon.setSize(iconSize);
        }
    }


    @Override
    protected void actionPropertyChanged(Action action, String propertyName) {
        // override tooltip set from action
        if (propertyName.equals(Action.LONG_DESCRIPTION) || propertyName.equals(Action.SHORT_DESCRIPTION))
            IcyAbstractAction.setToolTipTextFromAction(this, action);
        else
            super.actionPropertyChanged(action, propertyName);
    }
}
