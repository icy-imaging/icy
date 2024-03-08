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

package plugins.kernel.roi.tool.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;
import org.jetbrains.annotations.NotNull;
import plugins.kernel.roi.tool.ROIMagicWand;

/**
 * Plugin class for ROIMagicWand.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIMagicWandPlugin extends Plugin implements PluginROI {
    @Override
    public @NotNull String getROIClassName() {
        return ROIMagicWand.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt) {
        return new ROIMagicWand(pt);
    }

    @Override
    public ROI createROI() {
        return new ROIMagicWand();
    }
}
