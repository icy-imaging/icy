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

package fr.icy.extension.plugin.property;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.logging.Logger;

public abstract class SelectionProperty<V> extends Property<V> {
    private static final Logger LOGGER = Logger.getLogger(SelectionProperty.class.getName());

    @Unmodifiable
    private final @NonNull List<V> options;

    SelectionProperty(final @NonNull String name, final @NonNull String description, final @NonNull List<V> options, final @NonNull V defaultValue) {
        super(name, description, defaultValue);
        this.options = List.copyOf(options);
        if (!this.options.contains(defaultValue)) {
            LOGGER.warning("Value " + defaultValue + " is not a valid default value for " + name + ".");
            this.defaultValue = this.options.get(0);
        }
    }

    SelectionProperty(final @NonNull String name, final @NonNull List<V> options, final @NonNull V defaultValue) {
        super(name, defaultValue);
        this.options = List.copyOf(options);
        if (!this.options.contains(defaultValue)) {
            LOGGER.warning("Value " + defaultValue + " is not a valid default value for " + name + ".");
            this.defaultValue = this.options.get(0);
        }
    }

    @Override
    public final void setValue(final @Nullable V value) {
        if (value == null || !options.contains(value))
            this.value = defaultValue;
        else
            this.value = value;
    }

    @Contract(pure = true)
    @Unmodifiable
    public final @NonNull List<V> getOptions() {
        return options;
    }
}
