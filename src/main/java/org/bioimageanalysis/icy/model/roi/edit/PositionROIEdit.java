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

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.common.geom.point.Point5D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class PositionROIEdit extends AbstractROIEdit
{
    Point5D prevPos;
    Point5D currentPos;

    public PositionROIEdit(ROI roi, Point5D prevPos, boolean mergeable)
    {
        super(roi, "ROI position changed");

        this.prevPos = prevPos;
        this.currentPos = roi.getPosition5D();

        setMergeable(mergeable);
    }

    public PositionROIEdit(ROI roi, Point5D prevPos)
    {
        this(roi, prevPos, true);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().setPosition5D(prevPos);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI().setPosition5D(currentPos);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof PositionROIEdit)
        {
            final PositionROIEdit posEdit = (PositionROIEdit) edit;

            // same ROI ?
            if (posEdit.getROI() == getROI())
            {
                // collapse edits
                currentPos = posEdit.currentPos;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        prevPos = null;
        currentPos = null;
    }
}
