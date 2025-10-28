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

package org.bioimageanalysis.icy.model.sequence.edit;

import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;

/**
 * Abstract ROI sequence undoable edit.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class AbstractROISequenceEdit extends AbstractSequenceEdit {
    ROI roi;

    public AbstractROISequenceEdit(final Sequence sequence, final ROI roi, final String name, final SVGResource icon) {
        super(sequence, name, icon);

        this.roi = roi;
    }

    public AbstractROISequenceEdit(final Sequence sequence, final ROI roi, final String name) {
        this(sequence, roi, name, roi.getIcon());
    }

    public AbstractROISequenceEdit(final Sequence sequence, final ROI roi) {
        this(sequence, roi, "ROI changed", roi.getIcon());
    }

    public ROI getROI() {
        return roi;
    }

    @Override
    public void die() {
        super.die();

        roi = null;
    }
}
