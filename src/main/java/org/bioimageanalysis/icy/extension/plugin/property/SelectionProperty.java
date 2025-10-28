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

import java.util.List;

public abstract class SelectionProperty<V> extends Property<V> {
    @Unmodifiable
    @NotNull
    private final List<V> options;

    SelectionProperty(@NotNull final String name, @NotNull final String description, @NotNull final List<V> options, @NotNull final V defaultValue) {
        super(name, description, defaultValue);
        this.options = List.copyOf(options);
        if (!this.options.contains(defaultValue)) {
            IcyLogger.warn(this.getClass(), "Value " + defaultValue + " is not a valid default value for " + name + ".");
            this.defaultValue = this.options.getFirst();
        }
    }

    SelectionProperty(@NotNull final String name, @NotNull final List<V> options, @NotNull final V defaultValue) {
        super(name, defaultValue);
        this.options = List.copyOf(options);
        if (!this.options.contains(defaultValue)) {
            IcyLogger.warn(this.getClass(), "Value " + defaultValue + " is not a valid default value for " + name + ".");
            this.defaultValue = this.options.getFirst();
        }
    }

    @Override
    public final void setValue(@Nullable final V value) {
        if (value == null || !options.contains(value))
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
}
