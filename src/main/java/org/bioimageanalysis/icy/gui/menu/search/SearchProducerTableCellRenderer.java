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
package org.bioimageanalysis.icy.gui.menu.search;

import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.network.search.SearchResultProducer;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Thomas Musset
 */
public class SearchProducerTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof SearchResultProducer) {
            final SearchResultProducer producer = (SearchResultProducer) value;

            setText(producer.getName());
            ComponentUtil.setFontBold(this);
            setToolTipText(producer.getTooltipText());
        }
        else {
            setText(null);
            setToolTipText(null);
            setIcon(null);
        }

        return this;
    }
}
