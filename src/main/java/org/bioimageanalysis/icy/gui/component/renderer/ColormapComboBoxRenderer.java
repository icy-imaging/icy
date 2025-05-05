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
package org.bioimageanalysis.icy.gui.component.renderer;

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.lut.ColormapIcon;
import org.bioimageanalysis.icy.model.colormap.IcyColorMap;

import javax.swing.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ColormapComboBoxRenderer extends CustomComboBoxRenderer {
    public ColormapComboBoxRenderer(final JComboBox<IcyColorMap> combo) {
        super(combo);
    }

    @Override
    protected void updateItem(final JList<?> list, final Object value) {
        if (value instanceof final IcyColorMap colormap) {
            final int size = LookAndFeelUtil.getDefaultIconSize();

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
