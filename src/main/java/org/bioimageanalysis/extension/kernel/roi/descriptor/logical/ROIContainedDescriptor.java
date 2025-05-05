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
package org.bioimageanalysis.extension.kernel.roi.descriptor.logical;

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventSourceType;

/**
 * Number of contained ROI(s) descriptor (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIContainedDescriptor extends ROIDescriptor
{
    public static final String ID = "Contained";

    public ROIContainedDescriptor()
    {
        super(ID, "Contained", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Number of contained ROI(s)";
    }

    @Override
    public boolean separateChannel()
    {
        return false;
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        return (change.getSourceType() == SequenceEventSourceType.SEQUENCE_ROI);
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws InterruptedException
    {
        return Double.valueOf(computeContainedROIs(roi, sequence));
    }

    /**
     * Returns the number of contained ROI (the ones attached to the given Sequence) by the specified ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the number of contained ROIs
     * @param sequence
     *        the Sequence containing the ROIs we want to test for <i>contain</i> operation (test against itself is automatically discarded)
     * @throws InterruptedException
     */
    public static double computeContainedROIs(ROI roi, Sequence sequence) throws InterruptedException
    {
        if ((roi == null) || (sequence == null))
            return 0;

        int result = 0;
        for (ROI r : sequence.getROIs())
        {
            if (Thread.interrupted())
                throw new InterruptedException("ROI contained descriptor computation interrupted.");

            if ((r != roi) && (r != null) && (roi.contains(r)))
                result++;
        }

        return result;
    }
}
