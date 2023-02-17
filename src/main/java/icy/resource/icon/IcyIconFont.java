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
package icy.resource.icon;

import icy.gui.util.LookAndFeelUtil;
import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas MUSSET
 */
public final class IcyIconFont implements Icon {
    final IconCode ic;
    float size;
    private final LookAndFeelUtil.ColorType colorType;

    Icon internalIcon;

    public IcyIconFont(final IconCode iconCode, final float size, final LookAndFeelUtil.ColorType colorType) {
        this.ic = iconCode;
        this.size = size;
        this.colorType = colorType;

        updateIcon();
    }

    public IcyIconFont(final IconCode iconCode, final LookAndFeelUtil.ColorType colorType) {
        this.ic = iconCode;
        this.size = LookAndFeelUtil.getDefaultIconSizeAsFloat();
        this.colorType = colorType;

        updateIcon();
    }

    public IconCode getIconCode() {
        return ic;
    }

    public void updateIcon(final float size) {
        this.size = size;
        updateIcon();
    }

    public void updateIcon() {
        internalIcon = IconFontSwing.buildIcon(ic, size, LookAndFeelUtil.getUIColor(colorType));
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        internalIcon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return internalIcon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return internalIcon.getIconHeight();
    }
}
