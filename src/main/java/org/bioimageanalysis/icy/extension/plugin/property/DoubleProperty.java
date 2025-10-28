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

public final class DoubleProperty extends NumberProperty<Double> {
    public DoubleProperty(final @NotNull String name, final @NotNull String description, final @NotNull Double defaultValue, final @NotNull Double min, final @NotNull Double max, final @NotNull Double step) {
        super(name, description, defaultValue, min, max, step);
    }

    public DoubleProperty(final @NotNull String name, final @NotNull String description, final @NotNull Double min, final @NotNull Double max, final @NotNull Double step) {
        this(name, description, 0D, min, max, step);
    }

    public DoubleProperty(final @NotNull String name, final @NotNull String description, final @NotNull Double defaultValue) {
        this(name, description, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, .1D);
    }

    public DoubleProperty(final @NotNull String name, final @NotNull String description) {
        this(name, description, 0D, Double.MIN_VALUE, Double.MAX_VALUE, .1D);
    }

    public DoubleProperty(final @NotNull String name, final @NotNull Double defaultValue, final @NotNull Double min, final @NotNull Double max, final @NotNull Double step) {
        super(name, defaultValue, min, max, step);
    }

    public DoubleProperty(final @NotNull String name, final @NotNull Double min, final @NotNull Double max, final @NotNull Double step) {
        this(name, 0D, min, max, step);
    }

    public DoubleProperty(final @NotNull String name, final @NotNull Double defaultValue) {
        this(name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, .1D);
    }

    public DoubleProperty(final @NotNull String name) {
        this(name, 0D, Double.MIN_VALUE, Double.MAX_VALUE, .1D);
    }
}
