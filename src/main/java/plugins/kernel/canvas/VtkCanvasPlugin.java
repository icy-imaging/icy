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

package plugins.kernel.canvas;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.IcyPlugin;
import icy.plugin.interface_.PluginCanvas;

/**
 * Plugin wrapper for VtkCanvas
 *
 * @author Stephane Dallongeville
 */
@IcyPlugin(name = "3D", shortDescription = "", icon = "/plugins/kernel/canvas/vtk_canvas.svg")
public class VtkCanvasPlugin extends Plugin implements PluginCanvas {
    @Override
    public IcyCanvas createCanvas(final Viewer viewer) {
        return new VtkCanvas(viewer);
    }

    @Override
    public String getCanvasClassName() {
        return VtkCanvas.class.getName();
    }
}
