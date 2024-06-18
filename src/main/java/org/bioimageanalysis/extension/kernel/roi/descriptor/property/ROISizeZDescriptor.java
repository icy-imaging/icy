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

package org.bioimageanalysis.extension.kernel.roi.descriptor.property;

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;

/**
 * Size Z ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISizeZDescriptor extends ROIDescriptor
{
    public static final String ID = "Size Z";

    public ROISizeZDescriptor()
    {
        super(ID, "Size Z", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Size in Z dimension";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getSizeZ(roi.getBounds5D()));
    }

    /**
     * Returns size Z of specified Rectangle5D object
     */
    public static double getSizeZ(Rectangle5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getSizeZ();
    }
}