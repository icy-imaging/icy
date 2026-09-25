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
import org.jspecify.annotations.NonNull;

public abstract class NumberProperty<N extends Number> extends Property<N> {
    private final @NonNull Comparable<N> min;
    private final @NonNull Comparable<N> max;
    private final @NonNull N step;

    NumberProperty(final @NonNull String name, final @NonNull String description, final @NonNull N defaultValue, final @NonNull Comparable<N> min, final @NonNull Comparable<N> max, final @NonNull N step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        if (getMin().compareTo(defaultValue) <= 0 && getMax().compareTo(defaultValue) >= 0)
            throw new IllegalArgumentException("Default value must be between " + min + " and " + max);
    }

    NumberProperty(final @NonNull String name, final @NonNull N defaultValue, final @NonNull Comparable<N> min, final @NonNull Comparable<N> max, final @NonNull N step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        if (getMin().compareTo(defaultValue) <= 0 && getMax().compareTo(defaultValue) >= 0)
            throw new IllegalArgumentException("Default value must be between " + min + " and " + max);
    }

    @Contract(pure = true)
    public final @NonNull Comparable<N> getMin() {
        return min;
    }

    @Contract(pure = true)
    public final @NonNull Comparable<N> getMax() {
        return max;
    }

    @Contract(pure = true)
    public final @NonNull N getStep() {
        return step;
    }
}
