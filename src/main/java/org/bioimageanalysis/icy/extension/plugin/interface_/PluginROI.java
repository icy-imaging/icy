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

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin ROI interface.<br>
 * Used to define a plugin representing a specific ROI.<br>
 * The plugin will appears in the ROI list.<br>
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public interface PluginROI extends PluginNoEDTConstructor {
    /**
     * Return the ROI class name (ROIClass.getClassName())
     *
     * @return ROI's class name
     */
    @NotNull String getROIClassName();

    /**
     * Create and return a new ROI for <i>interactive</i> mode.<br>
     * The first point will be created in <i>selected</i> state so will support direct drag
     * operation.
     *
     * @param pt location of the creation point
     * @return the new created ROI
     */
    ROI createROI(Point5D pt);

    /**
     * Create and return a new ROI.<br>
     * Default constructor.
     *
     * @return the new created ROI
     */
    ROI createROI();
}
