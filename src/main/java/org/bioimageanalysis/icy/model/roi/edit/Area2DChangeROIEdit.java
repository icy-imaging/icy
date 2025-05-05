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

import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DArea;

/**
 * ROI2DARea change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Area2DChangeROIEdit extends AbstractROIEdit
{
    BooleanMask2D oldMask;
    BooleanMask2D newMask;

    public Area2DChangeROIEdit(ROI2DArea roi, BooleanMask2D oldMask, String name) throws InterruptedException
    {
        super(roi, name);

        this.oldMask = oldMask;
        // get actual mask
        this.newMask = roi.getBooleanMask(true);
    }

    public Area2DChangeROIEdit(ROI2DArea roi, BooleanMask2D oldMask) throws InterruptedException
    {
        this(roi, oldMask, "ROI mask changed");
    }

    public ROI2DArea getROI2DArea()
    {
        return (ROI2DArea) source;
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        getROI2DArea().setAsBooleanMask(oldMask);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        getROI2DArea().setAsBooleanMask(newMask);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        // if (!isMergeable())
        // return false;
        //
        // if (edit instanceof Area2DChangeROIEdit)
        // {
        // final Area2DChangeROIEdit changeEdit = (Area2DChangeROIEdit) edit;
        //
        // // same ROI ?
        // if (changeEdit.getROI() == getROI())
        // {
        // // collapse edits
        // newMask = changeEdit.newMask;
        // return true;
        // }
        // }

        return false;
    }

    @Override
    public void die()
    {
        super.die();

        oldMask = null;
        newMask = null;
    }

}
