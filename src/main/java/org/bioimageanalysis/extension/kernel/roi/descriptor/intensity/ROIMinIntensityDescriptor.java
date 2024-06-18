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

package org.bioimageanalysis.extension.kernel.roi.descriptor.intensity;

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.roi.ROIUtil;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventSourceType;
import org.jetbrains.annotations.NotNull;

/**
 * Minimum intensity ROI descriptor class (see {@link ROIDescriptor})
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIMinIntensityDescriptor extends ROIDescriptor {
    public static final String ID = "Min intensity";

    public ROIMinIntensityDescriptor() {
        super(ID, "Min Intensity", Double.class);
    }

    @Override
    public String getDescription() {
        return "Minimum intensity";
    }

    @Override
    public boolean separateChannel() {
        return true;
    }

    @Override
    public boolean needRecompute(final @NotNull SequenceEvent change) {
        return (change.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA);
    }

    @Override
    public Object compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return Double.valueOf(computeMinIntensity(roi, sequence));
    }

    /**
     * Computes and returns the minimum intensity for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     *
     * @param roi      the ROI on which we want to compute the minimum intensity
     * @param sequence the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException if the operation is not supported for this ROI
     */
    public static double computeMinIntensity(final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return ROIUtil.computeIntensityDescriptors(roi, sequence, false).min;
    }
}
