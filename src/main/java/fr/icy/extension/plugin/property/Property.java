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
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public abstract class Property<V> {
    private final int uid;
    protected final @NonNull String name;
    protected final @Nullable String description;

    protected @Nullable V value;
    protected @NonNull V defaultValue;

    Property(final @NonNull String name, final @NonNull String description, final @NonNull V defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;

        this.uid = this.name.toLowerCase(Locale.getDefault()).replace(" ", "-").hashCode();
    }

    Property(final @NonNull String name, final @NonNull V defaultValue) {
        this.name = name;
        this.description = null;
        this.defaultValue = defaultValue;

        this.uid = this.name.toLowerCase(Locale.getDefault()).replace(" ", "-").hashCode();
    }

    @Contract(pure = true)
    public final int getUid() {
        return uid;
    }

    @Contract(pure = true)
    public final @NonNull String getName() {
        return name;
    }

    @Contract(pure = true)
    public final @Nullable String getDescription() {
        return description;
    }

    public void setValue(final @Nullable V value) {
        this.value = Objects.requireNonNullElse(value, defaultValue);
    }

    @Contract(pure = true)
    public final @NonNull V getValue() {
        return value == null ? defaultValue : value;
    }

    @Contract(pure = true)
    public final @NonNull V getDefaultValue() {
        return defaultValue;
    }
}
