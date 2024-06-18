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

import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.roi.ROIEvent;
import org.bioimageanalysis.icy.model.roi.ROIEvent.ROIEventType;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;

/**
 * Internal unique Id descriptor class (see {@link ROIDescriptor})
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIIdDescriptor extends ROIDescriptor {
    public static final String ID = "Id";

    public ROIIdDescriptor() {
        super(ID, "Id", String.class);
    }

    @Override
    public String getDescription() {
        return "Internal id";
    }

    @Override
    public boolean needRecompute(final @NotNull ROIEvent change) {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_ID));
    }

    @Override
    public Object compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException {
        return Integer.valueOf(getId(roi));
    }

    /**
     * Returns ROI group id
     */
    public static int getId(final ROI roi) {
        if (roi == null)
            return 0;

        return roi.getId();
    }
}
