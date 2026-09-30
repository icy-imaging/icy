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

import fr.icy.gui.LookAndFeelUtil;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.icon.IcySVGIcon;
import fr.icy.gui.component.renderer.VisibleCellRenderer;
import fr.icy.gui.listener.SkinChangeListener;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Stéphane Dallongeville
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
        return visible;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        visible = (Boolean) value;

        if (visible)
            label.setIcon(IcySVG.VISIBILITY.getIcon(18, LookAndFeelUtil.ColorType.BUTTON_DEFAULT));
        else
            label.setIcon(IcySVG.VISIBILITY_OFF.getIcon(18, LookAndFeelUtil.ColorType.BUTTON_DEFAULT));

        return label;
    }

    @Override
    public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row) {
        visible = (Boolean) value;

        if (visible)
            label.setIcon(IcySVG.VISIBILITY.getIcon(18, LookAndFeelUtil.ColorType.BUTTON_DEFAULT));
        else
            label.setIcon(IcySVG.VISIBILITY_OFF.getIcon(18, LookAndFeelUtil.ColorType.BUTTON_DEFAULT));

        return label;
    }
}
