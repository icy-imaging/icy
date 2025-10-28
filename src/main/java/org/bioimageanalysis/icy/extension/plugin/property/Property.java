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
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public abstract class Property<V> {
    private final int uid;
    @NotNull
    protected final String name;
    @Nullable
    protected final String description;

    @Nullable
    protected V value;
    @NotNull
    protected V defaultValue;

    Property(@NotNull final String name, @NotNull final String description, @NotNull final V defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;

        this.uid = this.name.toLowerCase(Locale.getDefault()).replaceAll(" ", "-").hashCode();
    }

    Property(@NotNull final String name, @NotNull final V defaultValue) {
        this.name = name;
        this.description = null;
        this.defaultValue = defaultValue;

        this.uid = this.name.toLowerCase(Locale.getDefault()).replaceAll(" ", "-").hashCode();
    }

    public final int getUid() {
        return uid;
    }

    @NotNull
    public final String getName() {
        return name;
    }

    @Nullable
    public final String getDescription() {
        return description;
    }

    public void setValue(@Nullable final V value) {
        this.value = Objects.requireNonNullElse(value, defaultValue);
    }

    @NotNull
    public final V getValue() {
        return value == null ? defaultValue : value;
    }

    @NotNull
    public final V getDefaultValue() {
        return defaultValue;
    }
}
