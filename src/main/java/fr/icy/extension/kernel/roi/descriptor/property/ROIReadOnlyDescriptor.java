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
import org.jspecify.annotations.NonNull;

/**
 * Read-Only descriptor class (see {@link ROIDescriptor})
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class ROIReadOnlyDescriptor extends ROIDescriptor<Boolean> {
    public static final String ID = "Read only";

    public ROIReadOnlyDescriptor() {
        super(ID, "Read Only", Boolean.class);
    }

    @Override
    public String getDescription() {
        return "Read only state";
    }

    @Override
    public boolean needRecompute(final @NonNull ROIEvent change) {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED)
                && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_READONLY));
    }


    @Override
    public @NonNull Boolean compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException {
        return getReadOnly(roi);
    }

    /**
     * Returns ROI read-only state
     */
    public static @NonNull Boolean getReadOnly(final ROI roi) {
        if (roi == null)
            return false;

        return roi.isReadOnly();
    }
}