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
package icy.gui.component.renderer;

import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcyIconFont;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class VisibleCellRenderer extends JLabel implements TableCellRenderer, TreeCellRenderer {
    @Deprecated(since = "3.0.0", forRemoval = true)
    float iconSize = LookAndFeelUtil.getDefaultIconSizeAsFloat();

    // TODO: 02/02/2023 Move icon creation
    public static final IcyIconFont VISIBILITY = new IcyIconFont(GoogleMaterialDesignIcons.VISIBILITY, LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);
    public static final IcyIconFont VISIBILITY_OFF = new IcyIconFont(GoogleMaterialDesignIcons.VISIBILITY_OFF, LookAndFeelUtil.ColorType.UI_BUTTON_DEFAULT);

    @Deprecated(since = "3.0.0", forRemoval = true)
    public VisibleCellRenderer(int iconSize) {
        this();

        this.iconSize = (float) iconSize;

        VISIBILITY.updateIcon(iconSize);
        VISIBILITY_OFF.updateIcon(iconSize);
    }

    public VisibleCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
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
        VISIBILITY.updateIcon();
        VISIBILITY_OFF.updateIcon();
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
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(Rectangle r) {
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
