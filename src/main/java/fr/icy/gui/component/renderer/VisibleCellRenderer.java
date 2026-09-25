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

package fr.icy.gui.component.renderer;

import fr.icy.gui.LookAndFeelUtil;
import fr.icy.gui.component.icon.IcySVG;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class VisibleCellRenderer extends JLabel implements TableCellRenderer, TreeCellRenderer {
    public VisibleCellRenderer() {
        super();
    }

    @Override
    public @NonNull Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (value instanceof Boolean) {
            final boolean b = (Boolean) value;

            if (b)
                setIcon(IcySVG.VISIBILITY.getIcon(LookAndFeelUtil.getDefaultIconSize()));
            else
                setIcon(IcySVG.VISIBILITY_OFF.getIcon(LookAndFeelUtil.getDefaultIconSize()));
        }

        return this;
    }

    @Override
    public @NonNull Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        if (value instanceof Boolean) {
            final boolean b = (Boolean) value;

            if (b)
                setIcon(IcySVG.VISIBILITY.getIcon(LookAndFeelUtil.getDefaultIconSize()));
            else
                setIcon(IcySVG.VISIBILITY_OFF.getIcon(LookAndFeelUtil.getDefaultIconSize()));
        }

        return this;
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void invalidate() {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(final long tm, final int x, final int y, final int width, final int height) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(final Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint() {
    }
}
