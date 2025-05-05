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
package org.bioimageanalysis.icy.model.overlay.anchor;

import java.awt.Color;
import java.awt.event.InputEvent;

import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.gui.EventUtil;

/**
 * Anchor for line type shape.<br>
 * Support special line drag operation when shift is maintained.
 * 
 * @author Stephane
 */
public abstract class LineAnchor3D extends Anchor3D
{
    protected boolean fixedZ;

    public LineAnchor3D(Point3D position, Color color, Color selectedColor)
    {
        super(position.getX(), position.getY(), position.getZ(), color, selectedColor);
    }

    public boolean isFixedZ()
    {
        return fixedZ;
    }

    public void setFixedZ(boolean value)
    {
        fixedZ = value;
    }

    @Override
    protected boolean updateDrag(InputEvent e, double x, double y, double z)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        final Anchor3D anchor = getPreviousPoint();

        // shift action and fixed Z --> special drag
        if (EventUtil.isShiftDown(e) && (anchor != null) && isFixedZ())
        {
            final Point3D pos = anchor.getPosition();

            double dx = x - pos.getX();
            double dy = y - pos.getY();

            final double absDx = Math.abs(dx);
            final double absDy = Math.abs(dy);
            final double dist;

            if ((absDx != 0) && (absDy != 0))
                dist = absDx / absDy;
            else
                dist = 0;

            // square drag
            if ((dist > 0.5) && (dist < 1.5))
            {
                // align to DY
                if (absDx > absDy)
                {
                    if (dx >= 0)
                        dx = absDy;
                    else
                        dx = -absDy;
                }
                // align to DX
                else
                {
                    if (dy >= 0)
                        dy = absDx;
                    else
                        dy = -absDx;
                }
            }
            else
            // one direction drag
            {
                // drag X
                if (absDx > absDy)
                    dy = 0;
                // drag Y
                else
                    dx = 0;
            }

            // set new position
            setPosition(pos.getX() + dx, pos.getY() + dy, pos.getZ());
        }
        else
        {
            final double dx = x - startDragMousePosition.getX();
            final double dy = y - startDragMousePosition.getY();
            final double dz = z - startDragMousePosition.getZ();

            // set new position
            setPosition(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy,
                    startDragPainterPosition.getZ() + dz);
        }

        return true;
    }

    protected abstract Anchor3D getPreviousPoint();
}
