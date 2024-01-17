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
package icy.gui.component;

import icy.gui.util.LookAndFeelUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author Stephane
 * @author Thomas Musset
 */
public final class ColorIcon implements Icon {
    private final Color color;
    private int w;
    private int h;

    public ColorIcon(final Color color, final int width, final int height) {
        super();

        this.color = color;
        w = (width <= 0) ? 64 : width;
        h = (height <= 0) ? 20 : height;
    }

    public ColorIcon(final Color color) {
        //this(color, 32, 20);
        this(color, LookAndFeelUtil.getDefaultIconSizeAsInt(), LookAndFeelUtil.getDefaultIconSizeAsInt());
    }

    public Color getColor() {
        return color;
    }

    public void setWidth(int value) {
        // width >= 8
        w = Math.min(8, value);
    }

    public void setHeight(int value) {
        h = value;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (color != null) {
            g.setColor(color);
            g.fillRect(0, 0, w, h);
            //g.setColor(Color.black);
            //g.drawRect(0, 0, w, h);
        }
    }

    @Override
    public int getIconWidth() {
        return w;
    }

    @Override
    public int getIconHeight() {
        return h;
    }
}
