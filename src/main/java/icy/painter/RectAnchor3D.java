/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
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
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.painter;

import java.awt.Color;
import java.awt.event.InputEvent;

import icy.type.point.Point3D;
import icy.util.EventUtil;

/**
 * Anchor for 3D rectangular shape.<br>
 * Support special rectangular drag operation when shift is maintained.
 * 
 * @author Stephane
 */
public abstract class RectAnchor3D extends Anchor3D
{
    protected boolean fixedZ;

    public RectAnchor3D(Point3D position, Color color, Color selectedColor)
    {
        super(position.getX(), position.getY(), position.getZ(), color, selectedColor);

        fixedZ = false;
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

        final Anchor3D anchor = getOppositePoint();

        // shift action --> square drag
        if (EventUtil.isShiftDown(e) && (anchor != null))
        {
            final Point3D pos = anchor.getPosition();

            double dx = x - pos.getX();
            double dy = y - pos.getY();

            final double absDx = Math.abs(dx);
            final double absDy = Math.abs(dy);

            // fixed Z position ?
            if (isFixedZ())
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

                // set new position
                setPosition(pos.getX() + dx, pos.getY() + dy, getZ());
            }
            else
            {
                double dz = z - pos.getZ();
                final double absDz = Math.abs(dz);

                // align to DZ
                if ((absDx > absDz) && (absDy > absDz))
                {
                    if (dx >= 0)
                        dx = absDy;
                    else
                        dx = -absDy;
                }
                // align to DY
                else if ((absDx > absDy) && (absDz > absDy))
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

                // set new position
                setPosition(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
            }
        }
        else
        {
            // normal drag
            final double dx = x - startDragMousePosition.getX();
            final double dy = y - startDragMousePosition.getY();
            final double dz = z - startDragMousePosition.getZ();

            // set new position
            setPosition(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy,
                    startDragPainterPosition.getZ() + dz);
        }

        return true;
    }

    protected abstract Anchor3D getOppositePoint();
}
