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

package org.bioimageanalysis.icy.extension.plugin.property;

import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class MultipleSelectionProperty<V> extends Property<List<V>> {
    @Unmodifiable
    @NotNull
    private final List<V> options;

    private final boolean allowEmpty;

    MultipleSelectionProperty(@NotNull final String name, @NotNull final String description, @NotNull final List<V> options, @NotNull final List<V> defaultValue) {
        super(name, description, defaultValue);
        this.options = List.copyOf(options);
        if (!defaultValue.isEmpty()) {
            if (!Set.copyOf(this.options).containsAll(this.defaultValue)) {
                IcyLogger.warn(this.getClass(), "Value(s) " + defaultValue + " is not a valid value for " + name + ".");
                this.defaultValue = List.of(this.options.getFirst());
            }
            allowEmpty = false;
        }
        else {
            this.defaultValue = Collections.emptyList();
            allowEmpty = true;
        }
    }

    MultipleSelectionProperty(@NotNull final String name, @NotNull final String description, final @NotNull List<V> options, final @NotNull V defaultValue) {
        this(name, description, options, List.of(defaultValue));
    }

    MultipleSelectionProperty(@NotNull final String name, @NotNull final List<V> options, @NotNull final List<V> defaultValue) {
        super(name, defaultValue);
        this.options = List.copyOf(options);
        if (!defaultValue.isEmpty()) {
            if (!Set.copyOf(this.options).containsAll(this.defaultValue)) {
                IcyLogger.warn(this.getClass(), "Value(s) " + defaultValue + " is not a valid value for " + name + ".");
                this.defaultValue = List.of(this.options.getFirst());
            }
            allowEmpty = false;
        }
        else {
            this.defaultValue = Collections.emptyList();
            allowEmpty = true;
        }
    }

    MultipleSelectionProperty(@NotNull final String name, @NotNull final List<V> options, @NotNull final V defaultValue) {
        this(name, options, List.of(defaultValue));
    }

    @Override
    public final void setValue(@Nullable final List<V> value) {
        if (value == null || !Set.copyOf(options).containsAll(value))
            this.value = defaultValue;
        else
            this.value = value;
    }

    @Contract(pure = true)
    @Unmodifiable
    @NotNull
    public final List<V> getOptions() {
        return options;
    }

    @Contract(pure = true)
    public final boolean allowsEmpty() {
        return allowEmpty;
    }
}
