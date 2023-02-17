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

import javax.swing.JComboBox;
import javax.swing.JList;

import icy.gui.lut.ColormapIcon;
import icy.gui.util.LookAndFeelUtil;
import icy.image.colormap.IcyColorMap;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class ColormapComboBoxRenderer extends CustomComboBoxRenderer {
    /**
     * @deprecated Use {@link #ColormapComboBoxRenderer(JComboBox)} instead
     */
    @Deprecated
    public ColormapComboBoxRenderer(JComboBox<IcyColorMap> combo, int w, int h) {
        this(combo);
    }

    public ColormapComboBoxRenderer(JComboBox<IcyColorMap> combo) {
        super(combo);
    }

    @Override
    protected void updateItem(JList<?> list, Object value) {
        if (value instanceof IcyColorMap) {
            final IcyColorMap colormap = (IcyColorMap) value;

            final int size = LookAndFeelUtil.getDefaultIconSizeAsInt();

            setIcon(new ColormapIcon(colormap, size * 3, size));
            setText("");
            setToolTipText("Set " + colormap.getName() + " colormap");
            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
