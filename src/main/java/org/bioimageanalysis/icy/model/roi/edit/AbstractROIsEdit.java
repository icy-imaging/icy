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
package org.bioimageanalysis.icy.model.roi.edit;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.gui.undo.AbstractIcyUndoableEdit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base multiple ROI undoable edit.
 * 
 * @author Stephane
 */
public abstract class AbstractROIsEdit extends AbstractIcyUndoableEdit
{
    public AbstractROIsEdit(List<? extends ROI> rois, String name)
    {
        super(rois, name);
    }

    public AbstractROIsEdit(List<? extends ROI> rois)
    {
        this(rois, (rois.size() > 1) ? "ROIs changed" : "ROI changed");
    }

    @SuppressWarnings("unchecked")
    public List<? extends ROI> getROIs()
    {
        return (List<? extends ROI>) getSource();
    }

    protected Set<Sequence> getSequences()
    {
        final Set<Sequence> result = new HashSet<Sequence>();

        for (ROI roi : getROIs())
            result.addAll(Icy.getMainInterface().getSequencesContaining(roi));

        return result;
    }
}
