/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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
import org.bioimageanalysis.icy.gui.component.icon.ColorIcon;
import org.bioimageanalysis.icy.model.image.ImageUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ImageTableCellRenderer extends DefaultTableCellRenderer {
    final int size;

    public ImageTableCellRenderer() {
        super();

        this.size = LookAndFeelUtil.getDefaultIconSize();
        setIconTextGap(0);
    }

    @Override
    public void setValue(final Object value) {
        switch (value) {
            case final Image image -> setIcon(new ImageIcon(ImageUtil.scale(image, size, size)));
            case final Icon icon -> setIcon(icon);
            case final Color color -> setIcon(new ColorIcon(color, size, size));
            case null, default -> super.setValue(value);
        }
    }
}
