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

import fr.icy.common.string.StringUtil;
import fr.icy.model.roi.ROI;
import fr.icy.model.roi.ROIDescriptor;
import fr.icy.model.roi.ROIEvent;
import fr.icy.model.roi.ROIEvent.ROIEventType;
import fr.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;

/**
 * Opacity descriptor class (see {@link ROIDescriptor})
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIOpacityDescriptor extends ROIDescriptor {
    public static final String ID = "Opacity";

    public ROIOpacityDescriptor() {
        super(ID, "Opacity", Float.class);
    }

    @Override
    public String getDescription() {
        return "Opacity factor to display ROI content";
    }

    @Override
    public boolean needRecompute(final @NotNull ROIEvent change) {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_OPACITY));
    }


    @Override
    public Object compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException {
        return Float.valueOf(getOpacity(roi));
    }

    /**
     * Returns ROI opacity
     */
    public static float getOpacity(final ROI roi) {
        if (roi == null)
            return 1f;

        return roi.getOpacity();
    }
}
