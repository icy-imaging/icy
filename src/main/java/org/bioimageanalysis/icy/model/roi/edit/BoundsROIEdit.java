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

package org.bioimageanalysis.icy.model.roi.edit;

import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class BoundsROIEdit extends AbstractROIEdit
{
    Rectangle5D prevBounds;
    Rectangle5D currentBounds;

    public BoundsROIEdit(ROI roi, Rectangle5D prevBounds, boolean mergeable)
    {
        super(roi, "ROI bounds changed");

        this.prevBounds = prevBounds;
        this.currentBounds = roi.getBounds5D();

        setMergeable(mergeable);
    }

    public BoundsROIEdit(ROI roi, Rectangle5D prevBounds)
    {
        this(roi, prevBounds, true);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().setBounds5D(prevBounds);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI().setBounds5D(currentBounds);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof BoundsROIEdit)
        {
            final BoundsROIEdit bndEdit = (BoundsROIEdit) edit;

            // same ROI ?
            if (bndEdit.getROI() == getROI())
            {
                // collapse edits
                currentBounds = bndEdit.currentBounds;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        prevBounds = null;
        currentBounds = null;
    }
}
