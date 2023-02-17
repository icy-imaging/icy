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

import java.awt.*;

import javax.swing.*;

/**
 * CustomComboBox renderer, based on Substance look and feel code.<br>
 * Override the getListCellRendererComponent() or updateItem() methods to do your own rendering.
 *
 * @author Stephane
 * @author Thomas MUSSET
 */
public class CustomComboBoxRenderer extends DefaultListCellRenderer {
    private final JComboBox<?> combo;

    private static final Color background = new Color(220, 220, 220);
    private static final Color foreground = Color.DARK_GRAY;

    public CustomComboBoxRenderer(JComboBox<?> combo) {
        this.combo = combo;
        this.combo.setBackground(background);
        this.combo.setForeground(foreground);
    }

    public JComboBox<?> getComboBox() {
        return combo;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (!getComponentOrientation().equals(list.getComponentOrientation()))
            setComponentOrientation(list.getComponentOrientation());

        setEnabled(combo.isEnabled() & isEnabled());

        setBackground(background);
        setForeground(foreground);

        updateItem(list, value);

        return this;
    }

    protected void updateItem(JList<?> list, Object value) {
        if (value instanceof Icon) {
            setIcon((Icon) value);
            setText("");
        }
        else {
            setIcon(null);
            setText((value == null) ? "" : value.toString());
            setToolTipText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
    }
}
