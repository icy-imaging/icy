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

package fr.icy.gui.component.table;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Basically a JTable component with minor improvement.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class IcyTable extends JTable {
    /**
     * @see JTable#JTable(int, int)
     */
    public IcyTable(final int numRows, final int numColumns) {
        super(numRows, numColumns);
    }

    /**
     * @see JTable#JTable(Object[][], Object[])
     */
    public IcyTable(final Object[][] rowData, final Object[] columnNames) {
        super(rowData, columnNames);
    }

    /**
     * @see JTable#JTable(TableModel, TableColumnModel, ListSelectionModel)
     */
    public IcyTable(final TableModel dm, final TableColumnModel cm, final ListSelectionModel sm) {
        super(dm, cm, sm);
    }

    /**
     * @see JTable#JTable(TableModel, TableColumnModel)
     */
    public IcyTable(final TableModel dm, final TableColumnModel cm) {
        super(dm, cm);
    }

    /**
     * @see JTable#JTable(TableModel)
     */
    public IcyTable(final TableModel dm) {
        super(dm);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        final boolean result = super.getScrollableTracksViewportWidth();

        if (result)
            return getPreferredSize().width < getParent().getWidth();

        return false;
    }
}
