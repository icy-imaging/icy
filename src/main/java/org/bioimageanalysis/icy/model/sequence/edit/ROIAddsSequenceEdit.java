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

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import java.util.Collection;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI group add Sequence edit event.
 * 
 * @author Stephane
 */
public class ROIAddsSequenceEdit extends AbstractROIsSequenceEdit
{
    public ROIAddsSequenceEdit(Sequence sequence, Collection<ROI> rois, String name)
    {
        super(sequence, rois, name);
    }

    public ROIAddsSequenceEdit(Sequence sequence, Collection<ROI> rois)
    {
        this(sequence, rois, (rois.size() > 1) ? "ROI group added" : "ROI added");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        final Sequence sequence = getSequence();

        sequence.beginUpdate();
        try
        {
            for (ROI roi : getROIs())
                sequence.removeROI(roi, false);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        final Sequence sequence = getSequence();

        sequence.beginUpdate();
        try
        {
            for (ROI roi : getROIs())
                sequence.addROI(roi, false);
        }
        finally
        {
            sequence.endUpdate();
        }
    }
}
