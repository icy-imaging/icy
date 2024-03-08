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

package plugins.kernel.roi.roi3d.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;
import org.jetbrains.annotations.NotNull;
import plugins.kernel.roi.roi3d.ROI3DPoint;

/**
 * Plugin class for ROI3DPoint.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI3DPointPlugin extends Plugin implements PluginROI {
    @Override
    public @NotNull String getROIClassName() {
        return ROI3DPoint.class.getName();
    }

    @Override
    public ROI createROI(final Point5D pt) {
        return new ROI3DPoint(pt);
    }

    @Override
    public ROI createROI() {
        return new ROI3DPoint();
    }
}
