/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package org.bioimageanalysis.icy.extension.plugin.interface_;

import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.gui.viewer.Viewer;

/**
 * Plugin Canvas interface.<br>
 * Used to define a plugin representing a specific IcyCanvas.<br>
 * The plugin will appears in the Canvas list.<br>
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public interface PluginCanvas<C extends IcyCanvas> extends PluginNoEDTConstructor {
    /**
     * Return the Canvas class name (CanvasClass.getClassName())
     *
     * @return Class name of the canvas
     */
    String getCanvasClassName();

    /**
     * Create and return a new IcyCanvas
     *
     * @param viewer the viewer create and will contain the Canvas
     * @return the new created Canvas
     */
    C createCanvas(Viewer viewer);
}