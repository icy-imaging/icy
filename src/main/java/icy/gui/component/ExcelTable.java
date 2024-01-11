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

package icy.gui.component;

import icy.system.thread.ThreadUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Excel table view
 *
 * @author Fabrice de Chaumont
 * @author Alexandre Dufour
 * @author Thomas MUSSET
 */
public class ExcelTable extends JScrollPane {
    private final JTable table;

    public ExcelTable() {
        table = new JTable();
    }

    public ExcelTable(final @NotNull Sheet page) {
        this();
        updateSheet(page);
        setViewportView(table);
        setAutoscrolls(true);
    }

    public synchronized void updateSheet(final @NotNull Sheet page) {
        ThreadUtil.invokeLater(() -> {
            synchronized (table) {
                clearTable();
                setViewportView(table);
                table.setModel(new SheetTableModel(page));
            }
        });
    }

    private synchronized void clearTable() {
        synchronized (table) {
            final DefaultTableModel model = (DefaultTableModel) table.getModel();
            final int rows = model.getRowCount();
            for (int i = rows - 1; i >= 0; i--)
                model.removeRow(i);
        }
    }

    private record SheetTableModel(Sheet sheet) implements TableModel {
        @Override
        public int getRowCount() {
            return sheet.getPhysicalNumberOfRows();
        }

        @Override
        public int getColumnCount() {
            int cellNumber = 0;
            for (final Row row : sheet) {
                cellNumber = Math.max(cellNumber, row.getPhysicalNumberOfCells());
            }
            return cellNumber;
        }

        /**
         * Copied from javax.swing.table.AbstractTableModel, to name columns using spreadsheet
         * conventions: A, B, C, . Z, AA, AB, etc.
         */
        @Override
        public @NotNull String getColumnName(int column) {
            final StringBuilder result = new StringBuilder();
            for (; column >= 0; column = column / 26 - 1) {
                result.insert(0, (char) ((char) (column % 26) + 'A'));
            }
            return result.toString();
        }

        @Override
        public @NotNull Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return false;
        }

        @Override
        public @Nullable Object getValueAt(final int rowIndex, final int columnIndex) {
            try {
                final Cell cell = sheet.getRow(rowIndex).getCell(columnIndex);
                return switch (cell.getCellType()) {
                    case STRING -> cell.getStringCellValue();
                    case BOOLEAN -> cell.getBooleanCellValue();
                    case NUMERIC -> cell.getNumericCellValue();
                    default -> null;
                };
            }
            catch (final Exception e) {
                return null;
            }
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

        }

        @Override
        public void addTableModelListener(final TableModelListener l) {

        }

        @Override
        public void removeTableModelListener(final TableModelListener l) {

        }
    }
}
