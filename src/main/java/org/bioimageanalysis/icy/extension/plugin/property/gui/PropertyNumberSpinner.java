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

import org.bioimageanalysis.icy.extension.plugin.property.NumberProperty;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class PropertyNumberSpinner<N extends Number> extends PropertyComponent<JSpinner, N> {
    PropertyNumberSpinner(@NotNull final NumberProperty<N> property) {
        super(property);
    }

    @NotNull
    @Override
    protected JSpinner createComponent() {
        final NumberProperty<N> np = (NumberProperty<N>) property;
        return new JSpinner(new SpinnerNumberModel(oldValue, np.getMin(), np.getMax(), np.getStep()));
    }

    @NotNull
    @Override
    protected N getValue() {
        return property.getValue();
    }

    @Override
    protected void resetValue() {
        component.setValue(oldValue);
    }

    @Override
    protected void resetToDefault() {
        component.setValue(property.getDefaultValue());
    }
}
