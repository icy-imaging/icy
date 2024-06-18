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
package org.bioimageanalysis.icy.gui.component.editor;

import org.bioimageanalysis.icy.gui.component.renderer.VisibleCellRenderer;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class VisibleCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {
    protected final JLabel label;
    protected boolean visible;

    public VisibleCellEditor() {
        label = new JLabel();

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                visible = !visible;
                stopCellEditing();
            }
        });

        visible = true;
    }

    @Override
    public Object getCellEditorValue() {
        return Boolean.valueOf(visible);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(VisibleCellRenderer.VISIBILITY);
        else
            label.setIcon(VisibleCellRenderer.VISIBILITY_OFF);

        return label;
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(VisibleCellRenderer.VISIBILITY);
        else
            label.setIcon(VisibleCellRenderer.VISIBILITY_OFF);

        return label;
    }
}
