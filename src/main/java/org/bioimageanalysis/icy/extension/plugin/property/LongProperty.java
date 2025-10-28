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

public final class LongProperty extends NumberProperty<Long> {
    public LongProperty(final @NotNull String name, final @NotNull String description, final @NotNull Long defaultValue, final @NotNull Long min, final @NotNull Long max, final @NotNull Long step) {
        super(name, description, defaultValue, min, max, step);
    }

    public LongProperty(final @NotNull String name, final @NotNull String description, final @NotNull Long min, final @NotNull Long max, final @NotNull Long step) {
        this(name, description, 0L, min, max, step);
    }

    public LongProperty(final @NotNull String name, final @NotNull String description, final @NotNull Long defaultValue) {
        this(name, description, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE, 1L);
    }

    public LongProperty(final @NotNull String name, final @NotNull String description) {
        this(name, description, 0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L);
    }

    public LongProperty(final @NotNull String name, final @NotNull Long defaultValue, final @NotNull Long min, final @NotNull Long max, final @NotNull Long step) {
        super(name, defaultValue, min, max, step);
    }

    public LongProperty(final @NotNull String name, final @NotNull Long min, final @NotNull Long max, final @NotNull Long step) {
        this(name, 0L, min, max, step);
    }

    public LongProperty(final @NotNull String name, final @NotNull Long defaultValue) {
        this(name, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE, 1L);
    }

    public LongProperty(final @NotNull String name) {
        this(name, 0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L);
    }
}
