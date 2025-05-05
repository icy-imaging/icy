/*
 * Copyright (c) 2010-2024. Institut Pasteur.
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

package org.bioimageanalysis.icy.extension.abstract_;

import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

/**
 * @author Thomas Musset
 */
public abstract class Extension {
    protected final ArrayList<Class<? extends Plugin>> plugins = new ArrayList<>();

    @Contract(pure = true)
    public final ArrayList<Class<? extends Plugin>> getPlugins() {
        return plugins;
    }
}
