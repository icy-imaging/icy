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

package org.bioimageanalysis.extension.kernel.roi.descriptor.measure;

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.roi.ROIUtil;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;

/**
 * Interior ROI descriptor class (see {@link ROIDescriptor})
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIInteriorDescriptor extends ROIDescriptor {
    public static final String ID = "Interior";

    public ROIInteriorDescriptor() {
        super(ID, "Interior", Double.class);
    }

    @Override
    public String getUnit(final Sequence sequence) {
        return "px";
    }

    @Override
    public String getDescription() {
        return "Number of points for the interior";
    }

    @Override
    public Object compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return Double.valueOf(computeInterior(roi));
    }

    /**
     * Returns the number of point inside the specified ROI.
     *
     * @param roi the ROI on which we want to compute the number of contour point
     * @return the number of point inside the ROI
     */
    public static double computeInterior(final @NotNull ROI roi) throws InterruptedException {
        return roi.getNumberOfPoints();
    }

    /**
     * Returns the interior size from a given number of interior points in the best unit (see
     * {@link Sequence#getBestPixelSizeUnit(int, int)}) for the specified sequence and dimension.<br>
     * Ex:
     * <ul>
     * <li>computeInterior(sequence, roi, 2) return the area value</li>
     * <li>computeInterior(sequence, roi, 3) return the volume value</li>
     * </ul>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     *
     * @param interiorPoints the number of interior points (override the ROI value)
     * @param roi            the ROI we want to compute the interior size
     * @param sequence       the input sequence used to retrieve operation unit by using pixel size information.
     * @param dim            the dimension for the interior size operation (2 = area, 3 = volume, ...)
     * @return the number of point inside the ROI
     * @throws UnsupportedOperationException if the interior calculation for the specified dimension is not supported by the ROI
     * @see Sequence#getBestPixelSizeUnit(int, int)
     */
    public static double computeInterior(final double interiorPoints, final ROI roi, final Sequence sequence, final int dim) throws UnsupportedOperationException {
        final double mul = ROIUtil.getMultiplierFactor(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul == 0d)
            throw new UnsupportedOperationException("Can't process '" + ID + "' calculation for dimension " + dim + " on the ROI: " + roi.getName());

        return sequence.calculateSizeBestUnit(interiorPoints * mul, dim, dim);
    }
}
