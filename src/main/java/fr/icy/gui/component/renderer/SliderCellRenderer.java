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

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class SliderCellRenderer extends JSlider implements TableCellRenderer, TreeCellRenderer {
    public SliderCellRenderer() {
        super(0, 1000);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (value instanceof Integer) {
            final int intValue = (Integer) value;

            if (getValue() != intValue)
                setValue(intValue);
        }

        return this;
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        if (value instanceof Integer) {
            final int intValue = (Integer) value;

            if (getValue() != intValue)
                setValue(intValue);
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
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }
}
