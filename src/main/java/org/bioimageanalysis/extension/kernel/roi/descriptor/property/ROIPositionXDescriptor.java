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

/**
 * 
 */
package org.bioimageanalysis.extension.kernel.roi.descriptor.property;

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.common.geom.point.Point5D;

/**
 * Position X ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPositionXDescriptor extends ROIDescriptor
{
    public static final String ID = "Position X";

    public ROIPositionXDescriptor()
    {
        super(ID, "Position X", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Position X (bounding box)";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getPositionX(roi.getPosition5D()));
    }

    /**
     * Returns position X of specified Point5D object
     */
    public static double getPositionX(Point5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getX();
    }
}
