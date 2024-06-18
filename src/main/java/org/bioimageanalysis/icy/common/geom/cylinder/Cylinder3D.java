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

package org.bioimageanalysis.icy.common.geom.cylinder;

import org.bioimageanalysis.icy.common.geom.shape.BoxShape3D;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Cylinder3D extends BoxShape3D
{
    /**
     * base ellipse 2D
     */
    public Ellipse2D.Double ellipse;

    /**
     * Z position
     */
    public double z;
    /**
     * Z size
     */
    public double sizeZ;

    public Cylinder3D(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        super();

        ellipse = new Ellipse2D.Double(x, y, sizeX, sizeY);
        this.z = z;
        this.sizeZ = sizeZ;
    }

    public Cylinder3D(BoxShape3D boxShape)
    {
        this(boxShape.getX(), boxShape.getY(), boxShape.getZ(), boxShape.getSizeX(), boxShape.getSizeY(),
                boxShape.getSizeZ());
    }

    public Cylinder3D(Ellipse2D e, double z, double sizeZ)
    {
        this(e.getX(), e.getY(), z, e.getWidth(), e.getHeight(), sizeZ);
    }

    public Cylinder3D()
    {
        this(0d, 0d, 0d, 0d, 0d, 0d);
    }

    @Override
    public Object clone()
    {
        return new Cylinder3D(this);
    }

    @Override
    public double getX()
    {
        return ellipse.getX();
    }

    @Override
    public double getY()
    {
        return ellipse.getY();
    }

    @Override
    public double getZ()
    {
        return z;
    }

    @Override
    public double getSizeX()
    {
        return ellipse.getWidth();
    }

    @Override
    public double getSizeY()
    {
        return ellipse.getHeight();
    }

    @Override
    public double getSizeZ()
    {
        return sizeZ;
    }

    /**
     * Sets the Z coordinate of the close-upper-left corner of
     * the framing box in <code>double</code> precision.
     * 
     * @param value
     *        the Z coordinate of the close-upper-left corner of
     *        the framing box.
     */
    public void setZ(double value)
    {
        z = value;
    }

    /**
     * Sets the sizeZ of the framing box in <code>double</code> precision.
     * 
     * @param value
     *        the sizeZ of the framing box.
     */
    public void setSizeZ(double value)
    {
        sizeZ = value;
    }

    @Override
    public boolean isEmpty()
    {
        return ellipse.isEmpty() || (getSizeZ() <= 0d);
    }

    @Override
    public void setFrame(double x, double y, double z, double sx, double sy, double sz)
    {
        ellipse.setFrame(x, y, sx, sy);
        this.z = z;
        this.sizeZ = sz;
    }

    @Override
    public boolean contains(double x, double y, double z)
    {
        return ellipse.contains(x, y) && containsZ(z);
    }

    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return ellipse.intersects(x, y, sizeX, sizeY) && intersectsZ(z, sizeZ);
    }

    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return ellipse.contains(sizeX, y, sizeX, sizeY) && containsZ(z, sizeZ);
    }

    @Override
    public Shape getShape2D()
    {
        return new Ellipse2D.Double(getX(), getY(), getSizeX(), getSizeY());
    }
}
