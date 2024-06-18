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

import org.bioimageanalysis.extension.kernel.roi.roi3d.ROI3DPolyLine;

/**
 * 3D control point added implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point3DAddedROIEdit extends AbstractPoint3DROIEdit
{
    Point3D position;
    final int index;

    public Point3DAddedROIEdit(ROI3D roi, Anchor3D point)
    {
        super(roi, point, "ROI point added");

        position = point.getPosition();

        if (roi instanceof ROI3DPolyLine)
            index = ((ROI3DPolyLine) roi).getControlPoints().indexOf(point);
        else
            throw new IllegalArgumentException("Point3DAddedROIEdit: " + roi.getClassName() + " class not supported !");
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        if (getROI3D() instanceof ROI3DPolyLine)
        {
            final ROI3DPolyLine roi = (ROI3DPolyLine) getROI3D();
            roi.removePoint(point);
        }
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        point.setPosition(position);
        if (getROI3D() instanceof ROI3DPolyLine)
        {
            final ROI3DPolyLine roi = (ROI3DPolyLine) getROI3D();
            roi.addPoint(point, Math.min(index, roi.getControlPoints().size()));
        }
    }

    @Override
    public boolean addEdit(UndoableEdit edit)
    {
        // don't collapse here
        return false;
    }

    @Override
    public void die()
    {
        super.die();

        position = null;
    }
}
