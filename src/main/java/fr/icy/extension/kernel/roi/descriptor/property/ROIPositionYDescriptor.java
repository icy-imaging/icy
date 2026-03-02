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

package fr.icy.extension.kernel.roi.descriptor.property;

import fr.icy.model.roi.ROI;
import fr.icy.model.roi.ROIDescriptor;
import fr.icy.model.sequence.Sequence;
import fr.icy.common.geom.point.Point5D;

/**
 * Position Y ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPositionYDescriptor extends ROIDescriptor
{
    public static final String ID = "Position Y";

    public ROIPositionYDescriptor()
    {
        super(ID, "Position Y", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position Y (bounding box)";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionY(roi.getPosition5D()));
    }

    /**
     * Returns position Y of specified Point5D object
     */
    public static double getPositionY(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getY();
    }
}
