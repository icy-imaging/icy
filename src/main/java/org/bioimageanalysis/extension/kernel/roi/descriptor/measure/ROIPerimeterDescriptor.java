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
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventSourceType;
import org.bioimageanalysis.icy.common.string.StringUtil;

/**
 * Perimeter ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIPerimeterDescriptor extends ROIDescriptor
{
    public static final String ID = "Perimeter";

    public ROIPerimeterDescriptor()
    {
        super(ID, "Perimeter", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Perimeter";
    }

    @Override
    public String getUnit(Sequence sequence)
    {
        if (sequence != null)
            return sequence.getBestPixelSizeUnit(2, 1).toString() + "m";

        return UnitPrefix.MICRO.toString() + "m";
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
        return Double.valueOf(computePerimeter(roi, sequence));
    }

    /**
     * Computes and returns the perimeter expressed in the unit of the descriptor (see {@link #getUnit(Sequence)}) for
     * the specified ROI.<br>
     * It may thrown an <code>UnsupportedOperationException</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the perimeter
     * @param sequence
     *        the sequence from which the pixel size can be retrieved
     * @return the perimeter expressed in the unit of the descriptor (see {@link #getUnit(Sequence)})
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     * @throws InterruptedException 
     */
    public static double computePerimeter(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        if (sequence == null)
            throw new UnsupportedOperationException("Cannot compute Perimeter with null Sequence parameter !");

        final UnitPrefix bestUnit = sequence.getBestPixelSizeUnit(2, 1);
        final double length = roi.getLength(sequence);

        return UnitUtil.getValueInUnit(length, UnitPrefix.MICRO, bestUnit, 1);
    }

    // /**
    // * Computes and returns the perimeter from a given number of contour points expressed in the
    // * unit of the descriptor (see {@link #getUnit(Sequence)}) for the specified sequence and ROI.<br>
    // * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
    // *
    // * @param contourPoints
    // * the number of contour points (override the ROI value)
    // * @param roi
    // * the ROI we want to compute the perimeter
    // * @param sequence
    // * the input sequence used to retrieve operation unit by using pixel size
    // * information.
    // * @return the perimeter
    // * @throws UnsupportedOperationException
    // * if the operation is not supported for this ROI
    // */
    // static double computePerimeter(double contourPoints, ROI roi, Sequence sequence)
    // throws UnsupportedOperationException
    // {
    // return ROIContourDescriptor.computeContour(contourPoints, roi, sequence, 2);
    // }
}
