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

package fr.icy.extension.plugin.property.gui;

import fr.icy.extension.plugin.property.BooleanProperty;
import org.jspecify.annotations.NonNull;

import javax.swing.*;

public final class PropertyCheckBox extends PropertyComponent<JCheckBox, Boolean> {
    PropertyCheckBox(final @NonNull BooleanProperty property) {
        super(property);
    }

    @Override
    protected @NonNull JCheckBox createComponent() {
        final JCheckBox component = new JCheckBox();
        component.setSelected(oldValue);
        return component;
    }

    @Override
    protected @NonNull Boolean getValue() {
        return component.isSelected();
    }

    @Override
    protected void resetValue() {
        component.setSelected(oldValue);
    }

    @Override
    protected void resetToDefault() {
        component.setSelected(property.getDefaultValue());
    }
}
