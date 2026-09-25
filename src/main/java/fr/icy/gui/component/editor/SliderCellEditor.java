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

package fr.icy.gui.component.editor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class SliderCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor, ChangeListener {
    private final JSlider slider;

    /**
     * internals
     */
    private JTable table;
    private int row;
    private int column;
    boolean liveUpdate;

    /**
     * Create a SliderEditor for JTable or JTree.
     *
     * @param liveUpdate set to true if you want live update on slider change
     */
    public SliderCellEditor(final boolean liveUpdate) {
        slider = new JSlider(0, 1000);

        slider.addChangeListener(this);
        slider.setRequestFocusEnabled(false);

        table = null;
        this.liveUpdate = liveUpdate;
    }

    @Override
    public Object getCellEditorValue() {
        return slider.getValue();
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        if (slider.getValueIsAdjusting()) {
            // not very elegant but needed for live update on JTable
            if (liveUpdate && (table != null)) {
                final int value = slider.getValue();

                if ((Integer) table.getValueAt(row, column) != value)
                    table.setValueAt(value, row, column);
            }
        }
        else
            stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        this.table = table;
        this.row = row;
        this.column = column;

        final int intValue = (Integer) value;

        if (slider.getValue() != intValue)
            slider.setValue(intValue);

        slider.invalidate();

        return slider;
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
        final int intValue = (Integer) value;

        if (slider.getValue() != intValue)
            slider.setValue(intValue);

        return slider;
    }
}