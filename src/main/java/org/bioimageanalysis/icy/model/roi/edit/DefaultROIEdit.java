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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Default lazy implementation for ROI undoable edition (full copy)
 * 
 * @author Stephane
 */
public class DefaultROIEdit extends AbstractROIEdit
{
    ROI previous;
    ROI current;

    public DefaultROIEdit(ROI previous, ROI current)
    {
        super(current);

        this.previous = previous;
        this.current = current.getCopy();
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI().copyFrom(previous);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        if (current != null)
            getROI().copyFrom(current);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof DefaultROIEdit)
        {
            final DefaultROIEdit defEdit = (DefaultROIEdit) edit;

            // same ROI ?
            if (defEdit.getROI() == getROI())
            {
                // collapse edits
                current = defEdit.current;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        previous = null;
        current = null;
    }
}
