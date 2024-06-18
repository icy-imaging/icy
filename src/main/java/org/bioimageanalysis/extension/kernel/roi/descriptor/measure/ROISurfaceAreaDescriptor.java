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
package org.bioimageanalysis.extension.kernel.roi.descriptor.measure;

import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.common.math.UnitUtil.UnitPrefix;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROI3D;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventSourceType;
import org.bioimageanalysis.icy.common.string.StringUtil;

/**
 * Surface area ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROISurfaceAreaDescriptor extends ROIDescriptor
{
    public static final String ID = "Surface area";

    public ROISurfaceAreaDescriptor()
    {
        super(ID, "Surface Area", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Surface area";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(3, 2).toString() + "m2";

        return UnitPrefix.MICRO.toString() + "m2";
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        final SequenceEventSourceType sourceType = change.getSourceType();

        if (sourceType == SequenceEventSourceType.SEQUENCE_DATA)
            return true;
        if (sourceType == SequenceEventSourceType.SEQUENCE_META)
        {
            final String metaName = (String) change.getSource();

            return StringUtil.isEmpty(metaName) || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_X)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Y)
                    || StringUtil.equals(metaName, Sequence.ID_PIXEL_SIZE_Z);
        }

        return false;
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        return Double.valueOf(computeSurfaceArea(roi, sequence));
    }

    /**
     * Computes and returns the surface area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * for the specified ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the surface area
     * @param sequence
     *        the sequence from which the pixel size can be retrieved
     * @return the surface area expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     * @throws InterruptedException 
     */
    public static double computeSurfaceArea(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        if (!(roi instanceof ROI3D))
            throw new UnsupportedOperationException("Can't process " + ID + " calculation for on " + roi.getDimension()
            + "D ROI: '" + roi.getName() + "'");
        if (sequence == null)
            throw new UnsupportedOperationException(
                    "Can't process " + ID + " calculation with null Sequence parameter !");

        final UnitPrefix bestUnit = sequence.getBestPixelSizeUnit(3, 2);
        final double surfaceArea = ((ROI3D) roi).getSurfaceArea(sequence);

        return UnitUtil.getValueInUnit(surfaceArea, UnitPrefix.MICRO, bestUnit, 2);
    }

    // /**
    // * Computes and returns the surface area from a given number of contour points expressed in the
    // * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
    // * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
    // *
    // * @param contourPoints
    // * the number of contour points (override the ROI value)
    // * @param roi
    // * the ROI we want to compute the surface area
    // * @param sequence
    // * the input sequence used to retrieve operation unit by using pixel size
    // * information.
    // * @return the surface area
    // * @throws UnsupportedOperationException
    // * if the operation is not supported for this ROI
    // */
    // static double computeSurfaceArea(double contourPoints, ROI roi, Sequence sequence)
    // throws UnsupportedOperationException
    // {
    // return ROIContourDescriptor.computeContour(contourPoints, roi, sequence, 3);
    // }
}
