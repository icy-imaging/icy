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

import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract ROI list sequence undoable edit.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class AbstractROIsSequenceEdit extends AbstractSequenceEdit {
    Collection<ROI> rois;

    public AbstractROIsSequenceEdit(final Sequence sequence, final Collection<ROI> rois, final String name, final SVGIcon icon) {
        super(sequence, name, icon);

        this.rois = rois;
    }

    public AbstractROIsSequenceEdit(final Sequence sequence, final Collection<ROI> rois, final String name) {
        this(sequence, rois, name, null);
    }

    public AbstractROIsSequenceEdit(final Sequence sequence, final Collection<ROI> rois) {
        this(sequence, rois, (rois.size() > 1) ? "ROIs changed" : "ROI changed", null);
    }

    public List<ROI> getROIs() {
        return new ArrayList<>(rois);
    }

    @Override
    public void die() {
        super.die();

        rois = null;
    }
}
