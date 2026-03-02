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

package fr.icy.extension.kernel.roi.descriptor.measure;

import fr.icy.common.geom.point.Point5D;
import fr.icy.model.roi.ROI;
import fr.icy.model.roi.ROIDescriptor;
import fr.icy.model.roi.ROIUtil;
import fr.icy.model.sequence.Sequence;

/**
 * MassCenter X coordinate ROI descriptor class (see {@link ROIDescriptor})
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIMassCenterXDescriptor extends ROIDescriptor {
    public static final String ID = "Mass center X";

    public ROIMassCenterXDescriptor() {
        super(ID, "Center X", Double.class);
    }

    @Override
    public String getDescription() {
        return "Mass center X";
    }

    @Override
    public Object compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return Double.valueOf(getMassCenterX(ROIUtil.computeMassCenter(roi)));
    }

    /**
     * Returns position X of specified Point5D object
     */
    public static double getMassCenterX(final Point5D point) {
        if (point == null)
            return Double.NaN;

        return point.getX();
    }
}
