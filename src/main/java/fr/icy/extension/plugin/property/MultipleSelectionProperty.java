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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public abstract class MultipleSelectionProperty<V> extends Property<List<V>> {
    private static final Logger LOGGER = Logger.getLogger(MultipleSelectionProperty.class.getName());

    @Unmodifiable
    private final @NonNull List<V> options;

    private final boolean allowEmpty;

    MultipleSelectionProperty(final @NonNull String name, final @NonNull String description, final @NonNull List<V> options, final @NonNull List<V> defaultValue) {
        super(name, description, defaultValue);
        this.options = List.copyOf(options);
        if (!defaultValue.isEmpty()) {
            if (!Set.copyOf(this.options).containsAll(this.defaultValue)) {
                LOGGER.warning("Value(s) " + defaultValue + " is not a valid value for " + name + ".");
                this.defaultValue = List.of(this.options.get(0));
            }
            allowEmpty = false;
        }
        else {
            this.defaultValue = Collections.emptyList();
            allowEmpty = true;
        }
    }

    MultipleSelectionProperty(final @NonNull String name, final @NonNull String description, final @NonNull List<V> options, final @NonNull V defaultValue) {
        this(name, description, options, List.of(defaultValue));
    }

    MultipleSelectionProperty(final @NonNull String name, final @NonNull List<V> options, final @NonNull List<V> defaultValue) {
        super(name, defaultValue);
        this.options = List.copyOf(options);
        if (!defaultValue.isEmpty()) {
            if (!Set.copyOf(this.options).containsAll(this.defaultValue)) {
                LOGGER.warning("Value(s) " + defaultValue + " is not a valid value for " + name + ".");
                this.defaultValue = List.of(this.options.get(0));
            }
            allowEmpty = false;
        }
        else {
            this.defaultValue = Collections.emptyList();
            allowEmpty = true;
        }
    }

    MultipleSelectionProperty(final @NonNull String name, final @NonNull List<V> options, final @NonNull V defaultValue) {
        this(name, options, List.of(defaultValue));
    }

    @Override
    public final void setValue(final @Nullable List<V> value) {
        if (value == null || !Set.copyOf(options).containsAll(value))
            this.value = defaultValue;
        else
            this.value = value;
    }

    @Contract(pure = true)
    @Unmodifiable
    public final @NonNull List<V> getOptions() {
        return options;
    }

    @Contract(pure = true)
    public final boolean allowsEmpty() {
        return allowEmpty;
    }
}
