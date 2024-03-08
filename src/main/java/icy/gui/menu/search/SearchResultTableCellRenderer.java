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

package icy.gui.menu.search;

import icy.image.ImageUtil;
import icy.search.SearchResult;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * This class is a renderer to display the filtered data.
 *
 * @author Thomas Provoost
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SearchResultTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof final SearchResult result) {
            final String title = result.getTitle();
            final String description = result.getDescription();
            final Image img = result.getImage();
            final int cellWidth = (int) (table.getCellRect(row, column, false).width * 0.90);
            String text;

            if (img != null)
                setIcon(new ImageIcon(ImageUtil.scale(img, 32, 32)));
            else
                setIcon(null);

            if (StringUtil.isEmpty(title))
                text = "<b>Unknow</b>";
            else
                text = "<b>" + title + "</b>";
            if (!StringUtil.isEmpty(description))
                text += "<br>" + GraphicsUtil.limitStringFor(table, description, cellWidth);
            setText("<html>" + text);

            setToolTipText(result.getTooltip());
            setVerticalAlignment(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);

            // override enabled state
            if (!result.isEnabled())
                setEnabled(false);
            else
                setEnabled(table.isEnabled());
        }

        return this;
    }
}
