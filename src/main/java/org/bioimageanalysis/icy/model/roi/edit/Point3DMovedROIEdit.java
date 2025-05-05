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

import org.bioimageanalysis.icy.model.overlay.anchor.Anchor3D;
import org.bioimageanalysis.icy.model.roi.ROI3D;
import org.bioimageanalysis.icy.common.geom.point.Point3D;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * 3D control point position change implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point3DMovedROIEdit extends AbstractPoint3DROIEdit
{
    protected Point3D prevPos;
    protected Point3D currentPos;

    public Point3DMovedROIEdit(ROI3D roi, Anchor3D point, Point3D prevPos)
    {
        super(roi, point, "ROI point moved");

        this.prevPos = prevPos;
        this.currentPos = point.getPosition();
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        point.setPosition(prevPos);
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        point.setPosition(currentPos);
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        if (!isMergeable())
            return false;

        if (edit instanceof Point3DMovedROIEdit)
        {
            final Point3DMovedROIEdit posEdit = (Point3DMovedROIEdit) edit;

            // same ROI and point ?
            if ((posEdit.getROI() == getROI()) && (posEdit.getPoint() == getPoint()))
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
