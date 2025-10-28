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

import java.awt.*;
import java.util.List;

public final class ColorSelectionProperty extends SelectionProperty<Color> {
    public ColorSelectionProperty(final @NotNull String name, final @NotNull String description, final @NotNull List<Color> options, final @NotNull Color defaultValue) {
        super(name, description, options, defaultValue);
    }

    public ColorSelectionProperty(final @NotNull String name, final @NotNull List<Color> options, final @NotNull Color defaultValue) {
        super(name, options, defaultValue);
    }
}
