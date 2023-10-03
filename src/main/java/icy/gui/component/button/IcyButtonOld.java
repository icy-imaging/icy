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
import icy.gui.util.ComponentUtil;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.*;

/**
 * @author Stephane
 * @deprecated Use {@link IcyButton} instead
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class IcyButtonOld extends JButton {
    private boolean flat;

    /**
     * Create a button with specified text and icon
     */
    public IcyButtonOld(String text, IcyIcon icon) {
        super(text, icon);

        flat = false;
        init();
    }

    /**
     * Create a button with specified icon.
     */
    public IcyButtonOld(IcyIcon icon) {
        this(null, icon);
    }

    /**
     * Create a button with specified text.
     */
    public IcyButtonOld(String text) {
        this(text, (IcyIcon) null);
    }

    /**
     * Create a button with specified action.
     */
    public IcyButtonOld(IcyAbstractAction action) {
        super(action);

        flat = false;
        init();
    }

    /**
     * @deprecated User {@link #IcyButtonOld(IcyAbstractAction)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(icy.common.IcyAbstractAction action) {
        super(action);

        flat = false;
        init();
    }

    /**
     * @deprecated Use {@link #IcyButtonOld(String, IcyIcon)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(String text, Image iconImage, int iconSize) {
        this(text, new IcyIcon(iconImage, iconSize));
    }

    /**
     * @deprecated Use {@link #IcyButtonOld(String, IcyIcon)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(String text, Image iconImage) {
        this(text, iconImage, IcyIcon.DEFAULT_SIZE);
    }

    /**
     * @deprecated Use {@link #IcyButtonOld(IcyIcon)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(Image iconImage, int iconSize) {
        this(null, iconImage, iconSize);
    }

    /**
     * @deprecated Use {@link #IcyButtonOld(IcyIcon)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(Image iconImage) {
        this(null, iconImage);
    }

    /**
     * @deprecated Use {@link #IcyButtonOld(String, IcyIcon)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(String text, String iconName, int iconSize) {
        this(text, new IcyIcon(iconName, iconSize));
    }

    /**
     * @deprecated Use {@link #IcyButtonOld(String, IcyIcon)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyButtonOld(String text, String iconName) {
        this(text, iconName, IcyIcon.DEFAULT_SIZE);
    }

    private void init() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);

        if (flat) {
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
        }

        // manual change notify
        updateSize();
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

            updateSize();
        }
    }

    /**
     * Return the icon as IcyIcon
     */
    public IcyIcon getIcyIcon() {
        final Icon icon = getIcon();

        if (icon instanceof IcyIcon)
            return (IcyIcon) icon;

        return null;
    }

    /**
     * @return the icon name
     */
    public String getIconName() {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            return icon.getName();

        return null;
    }

    /**
     * @param iconName the iconName to set
     */
    public void setIconName(String iconName) {
        final IcyIcon icon = getIcyIcon();

        if (icon != null) {
            icon.setName(iconName);
            updateSize();
        }
    }

    /**
     * @return the icon size
     */
    public int getIconSize() {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            return icon.getSize();

        return -1;
    }

    /**
     * @param iconSize the iconSize to set
     */
    public void setIconSize(int iconSize) {
        final IcyIcon icon = getIcyIcon();

        if (icon != null) {
            icon.setSize(iconSize);
            updateSize();
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);

        updateSize();
    }

    public void updateSize() {
        final IcyIcon icon = getIcyIcon();
        boolean noText = StringUtil.isEmpty(getText());
        noText |= (getAction() != null) && getHideActionText();

        // adjust size to icon size if no text
        if (flat && (icon != null) && noText) {
            final Dimension dim = icon.getDimension();
            dim.height += 2;
            dim.width += 2;
            ComponentUtil.setFixedSize(this, dim);
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
