/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

package org.bioimageanalysis.icy.extension.plugin.property.gui;

import org.bioimageanalysis.icy.extension.plugin.property.SelectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Vector;

public final class PropertyComboBox<V> extends PropertyComponent<JComboBox<V>, V> {
    private int indexOldValue = 0;
    private int indexDefaultValue = 0;

    PropertyComboBox(@NotNull final SelectionProperty<V> property) {
        super(property);
    }

    @Override
    protected @NotNull JComboBox<V> createComponent() {
        final SelectionProperty<V> sp = (SelectionProperty<V>) property;
        final Vector<V> vector = new Vector<>(sp.getOptions());
        indexOldValue = vector.indexOf(oldValue);
        indexDefaultValue = vector.indexOf(property.getDefaultValue());
        final DefaultComboBoxModel<V> model = new DefaultComboBoxModel<>(vector);
        final JComboBox<V> comboBox = new JComboBox<>(model);
        comboBox.setMaximumRowCount(5);
        comboBox.setSelectedIndex(indexOldValue);
        return comboBox;
    }

    @Override
    protected @Nullable V getValue() {
        return component.getItemAt(component.getSelectedIndex());
    }

    @Override
    protected void resetValue() {
        component.setSelectedIndex(indexOldValue);
    }

    @Override
    protected void resetToDefault() {
        component.setSelectedIndex(indexDefaultValue);
    }
}
