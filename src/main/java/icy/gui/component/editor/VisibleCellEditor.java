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
package icy.gui.component.editor;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

import icy.gui.component.renderer.VisibleCellRenderer;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class VisibleCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {
    protected JLabel label;
    @Deprecated
    int iconSize;
    boolean visible;

    @Deprecated
    public VisibleCellEditor(int iconSize) {
        this();
        this.iconSize = iconSize;
    }

    public VisibleCellEditor() {
        label = new JLabel();

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
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
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(VisibleCellRenderer.VISIBILITY);
        else
            label.setIcon(VisibleCellRenderer.VISIBILITY_OFF);

        return label;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        visible = ((Boolean) value).booleanValue();

        if (visible)
            label.setIcon(VisibleCellRenderer.VISIBILITY);
        else
            label.setIcon(VisibleCellRenderer.VISIBILITY_OFF);

        return label;
    }
}
