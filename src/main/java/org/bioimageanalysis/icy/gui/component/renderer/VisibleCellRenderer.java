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

package org.bioimageanalysis.icy.gui.component.renderer;

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class VisibleCellRenderer extends JLabel implements TableCellRenderer, TreeCellRenderer {
    public static final @NotNull Icon VISIBILITY = new IcySVGIcon(SVGIcon.VISIBILITY, LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);
    public static final @NotNull Icon VISIBILITY_OFF = new IcySVGIcon(SVGIcon.VISIBILITY_OFF, LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);

    public VisibleCellRenderer() {
        super();
    }

    @Override
    public @NotNull Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (value instanceof Boolean) {
            final boolean b = ((Boolean) value).booleanValue();

            if (b)
                setIcon(VISIBILITY);
            else
                setIcon(VISIBILITY_OFF);
        }

        return this;
    }

    @Override
    public @NotNull Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        if (value instanceof Boolean) {
            final boolean b = ((Boolean) value).booleanValue();

            if (b)
                setIcon(VISIBILITY);
            else
                setIcon(VISIBILITY_OFF);
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
