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
import fr.icy.common.geom.rectangle.Rectangle5D;

/**
 * Size X ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISizeXDescriptor extends ROIDescriptor
{
    public static final String ID = "Size X";

    public ROISizeXDescriptor()
    {
        super(ID, "Size X", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Size in X dimension";
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return Double.valueOf(getSizeX(roi.getBounds5D()));
    }

    /**
     * Returns size X of specified Rectangle5D object
     */
    public static double getSizeX(Rectangle5D point)
    {
        if (point == null)
            return Double.NaN;

        return point.getSizeX();
    }
}
