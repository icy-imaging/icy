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

import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;

import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DShape;

/**
 * 2D control point removed implementation for ROI undoable edition.
 * 
 * @author Stephane
 */
public class Point2DRemovedROIEdit extends AbstractPoint2DROIEdit
{
    Point2D position;
    final int index;

    public Point2DRemovedROIEdit(ROI2DShape roi, List<Anchor2D> previousPoints, Anchor2D point)
    {
        super(roi, point, "ROI point removed");

        position = point.getPosition();
        
        // we need to save the index in the old point list
        int i = 0;
        for(Anchor2D p: previousPoints)
        {
            if (p.getPosition().equals(position))
                break;
            
            i++;
        }
        
        index = i;
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        point.setPosition(position);
        ((ROI2DShape) getROI()).addPoint(point, Math.min(index, getROI2DShape().getControlPoints().size()));
    }

    @Override
    public void redo() throws CannotRedoException
    {
        super.redo();

        // redo
        ((ROI2DShape) getROI()).removePoint(point);
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
