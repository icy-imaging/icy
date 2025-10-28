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

import org.jetbrains.annotations.NotNull;

public abstract class NumberProperty<N extends Number> extends Property<N> {
    private final @NotNull Comparable<N> min;
    private final @NotNull Comparable<N> max;
    private final @NotNull N step;

    NumberProperty(final @NotNull String name, final @NotNull String description, final @NotNull N defaultValue, final @NotNull Comparable<N> min, final @NotNull Comparable<N> max, final @NotNull N step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        if (getMin().compareTo(defaultValue) <= 0 && getMax().compareTo(defaultValue) >= 0)
            throw new IllegalArgumentException("Default value must be between " + min + " and " + max);
    }

    NumberProperty(final @NotNull String name, final @NotNull N defaultValue, final @NotNull Comparable<N> min, final @NotNull Comparable<N> max, final @NotNull N step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        if (getMin().compareTo(defaultValue) <= 0 && getMax().compareTo(defaultValue) >= 0)
            throw new IllegalArgumentException("Default value must be between " + min + " and " + max);
    }

    public final @NotNull Comparable<N> getMin() {
        return min;
    }

    public final @NotNull Comparable<N> getMax() {
        return max;
    }

    public final @NotNull N getStep() {
        return step;
    }
}
