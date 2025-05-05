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

public class ZBoxAnchor3D extends Anchor3D
{
    public ZBoxAnchor3D(Point3D position, Color color, Color selectedColor)
    {
        super(position.getX(), position.getY(), position.getZ(), color, selectedColor);
    }

    @Override
    protected boolean updateDrag(InputEvent e, double x, double y, double z)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        // only on z axis
        double dz = z - startDragMousePosition.getZ();
        // set position
        setPosition(new Point3D.Double(getX(), getY(), startDragPainterPosition.getZ() + dz));

        return true;
    }
}
