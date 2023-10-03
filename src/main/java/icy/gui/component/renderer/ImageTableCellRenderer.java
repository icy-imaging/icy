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
package icy.gui.component.renderer;

import icy.gui.component.ColorIcon;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.ResourceUtil;

import java.awt.Color;
import java.awt.Image;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class ImageTableCellRenderer extends DefaultTableCellRenderer {
    final int size;

    @Deprecated(since = "2.4.3", forRemoval = true)
    public ImageTableCellRenderer(int size) {
        super();

        this.size = size;
        setIconTextGap(0);
    }

    public ImageTableCellRenderer() {
        super();

        this.size = LookAndFeelUtil.getDefaultIconSizeAsInt();
        setIconTextGap(0);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Image)
            setIcon(ResourceUtil.getImageIcon((Image) value, size));
        else if (value instanceof Color)
            setIcon(new ColorIcon((Color) value, size, size));
        else
            super.setValue(value);
    }
}
