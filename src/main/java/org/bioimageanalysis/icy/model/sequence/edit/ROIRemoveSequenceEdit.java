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
 * ROI remove Sequence edit event
 * 
 * @author Stephane
 */
public class ROIRemoveSequenceEdit extends AbstractROISequenceEdit
{
    public ROIRemoveSequenceEdit(Sequence sequence, ROI source)
    {
        super(sequence, source, "ROI removed");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        getSequence().addROI(getROI(), false);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        getSequence().removeROI(getROI(), false);
    }
}
