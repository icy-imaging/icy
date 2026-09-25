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

import java.util.List;

public final class NumberSelectionProperty<N extends Number> extends SelectionProperty<N> {
    public NumberSelectionProperty(final @NonNull String name, final @NonNull String description, final @NonNull List<N> collection, final @NonNull N defaultValue) {
        super(name, description, collection, defaultValue);
    }

    public NumberSelectionProperty(final @NonNull String name, final @NonNull List<N> collection, final @NonNull N defaultValue) {
        super(name, collection, defaultValue);
    }
}
