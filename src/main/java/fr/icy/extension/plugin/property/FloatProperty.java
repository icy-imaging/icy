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

import org.jspecify.annotations.NonNull;

public final class FloatProperty extends NumberProperty<Float> {
    public FloatProperty(final @NonNull String name, final @NonNull String description, final @NonNull Float defaultValue, final @NonNull Float min, final @NonNull Float max, final @NonNull Float step) {
        super(name, description, defaultValue, min, max, step);
    }

    public FloatProperty(final @NonNull String name, final @NonNull String description, final @NonNull Float min, final @NonNull Float max, final @NonNull Float step) {
        this(name, description, 0F, min, max, step);
    }

    public FloatProperty(final @NonNull String name, final @NonNull String description, final @NonNull Float defaultValue) {
        this(name, description, defaultValue, Float.MIN_VALUE, Float.MAX_VALUE, .1F);
    }

    public FloatProperty(final @NonNull String name, final @NonNull String description) {
        this(name, description, 0F, Float.MIN_VALUE, Float.MAX_VALUE, .1F);
    }

    public FloatProperty(final @NonNull String name, final @NonNull Float defaultValue, final @NonNull Float min, final @NonNull Float max, final @NonNull Float step) {
        super(name, defaultValue, min, max, step);
    }

    public FloatProperty(final @NonNull String name, final @NonNull Float min, final @NonNull Float max, final @NonNull Float step) {
        this(name, 0F, min, max, step);
    }

    public FloatProperty(final @NonNull String name, final @NonNull Float defaultValue) {
        this(name, defaultValue, Float.MIN_VALUE, Float.MAX_VALUE, .1F);
    }

    public FloatProperty(final @NonNull String name) {
        this(name, 0F, Float.MIN_VALUE, Float.MAX_VALUE, .1F);
    }
}
