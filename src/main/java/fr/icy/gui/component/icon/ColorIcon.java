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

package fr.icy.gui.component.icon;

import fr.icy.gui.LookAndFeelUtil;
import org.jetbrains.annotations.Contract;

import javax.swing.*;
import java.awt.*;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public final class ColorIcon implements Icon {
    private final Color color;
    private int w;
    private int h;

    @Contract(pure = true)
    public ColorIcon(final Color color, final int width, final int height) {
        super();

        this.color = color;
        w = (width <= 0) ? 64 : width;
        h = (height <= 0) ? 20 : height;
    }

    public ColorIcon(final Color color) {
        //this(color, 32, 20);
        this(color, LookAndFeelUtil.getDefaultIconSize(), LookAndFeelUtil.getDefaultIconSize());
    }

    @Contract(pure = true)
    public Color getColor() {
        return color;
    }

    @Contract(mutates = "this")
    public void setWidth(final int value) {
        // width >= 8
        w = Math.min(8, value);
    }

    @Contract(mutates = "this")
    public void setHeight(final int value) {
        h = value;
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        if (color != null) {
            g.setColor(color);
            g.fillRect(0, 0, w, h);
        }
        else {
            g.setColor(Color.black);
            g.drawRect(0, 0, w, h);
        }
    }

    @Contract(pure = true)
    @Override
    public int getIconWidth() {
        return w;
    }

    @Contract(pure = true)
    @Override
    public int getIconHeight() {
        return h;
    }
}
