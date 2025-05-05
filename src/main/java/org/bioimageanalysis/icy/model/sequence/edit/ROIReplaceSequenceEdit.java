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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * ROI replace Sequence edit event.
 * 
 * @author Stephane
 */
public class ROIReplaceSequenceEdit extends AbstractROISequenceEdit
{
    final ROI oldRoi;

    public ROIReplaceSequenceEdit(Sequence sequence, ROI oldRoi, ROI newRoi, String name)
    {
        super(sequence, newRoi, name);

        this.oldRoi = oldRoi;
    }

    public ROIReplaceSequenceEdit(Sequence sequence, ROI oldRoi, ROI newRoi)
    {
        this(sequence, oldRoi, newRoi, "ROI replaced");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        final Sequence seq = getSequence();

        seq.removeROI(getROI(), false);
        seq.addROI(oldRoi, false);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        final Sequence seq = getSequence();

        seq.removeROI(oldRoi, false);
        seq.addROI(getROI(), false);
    }
}
