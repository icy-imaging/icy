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

package org.bioimageanalysis.icy.common.geom.shape;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;

import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle3D;

/**
 * The <code>ZShape3D</code> interface provides definitions for objects
 * that represent some form of 3D shape geometric based on a 2D Shape extended on Z axis.
 */
public abstract class ZShape3D implements Shape3D, Cloneable
{
    /**
     * This is an abstract class that cannot be instantiated directly.
     */
    protected ZShape3D()
    {
        super();
    }

    /**
     * @return the Z coordinate of 3D shape
     */
    public abstract double getZ();

    /**
     * @return the depth of the 3D shape
     */
    public abstract double getSizeZ();

    /**
     * Sets the Z coordinate of the 3D shape
     * 
     * @param value
     *        the Z coordinate of the 3D shape
     */
    public abstract void setZ(double value);

    /**
     * Sets the depth of the 3D shape
     * 
     * @param value
     *        the depth of the 3D shape
     */
    public abstract void setSizeZ(double value);

    /**
     * @return the smallest Z coordinate of the framing
     *         rectangle of the <code>Shape</code>.
     */
    public double getMinZ()
    {
        return getZ();
    }

    /**
     * @return the largest Z coordinate of the framing
     *         rectangle of the <code>Shape</code>.
     */
    public double getMaxZ()
    {
        return getZ() + getSizeZ();
    }

    /**
     * Returns the Z coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the Z coordinate of the center of the framing box
     *         of the <code>Shape</code>.
     */
    public double getCenterZ()
    {
        return getZ() + (getSizeZ() / 2.0);
    }

    /**
     * Determines whether the <code>BoxShape3D</code> is empty.
     * When the <code>BoxShape3D</code> is empty, it encloses no
     * area.
     * 
     * @return <code>true</code> if the <code>BoxShape3D</code> is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return getBounds().isEmpty();
    }

    /**
     * Test if a specified Z position is inside the Z boundary of the shape
     * 
     * @param z
     *        z position to test
     * @return true if the specified z position is inside the Z boundary of the shape
     */
    public boolean containsZ(double z)
    {
        return (getMinZ() <= z) && (getMaxZ() > z);
    }

    /**
     * Test if a specified Z range is inside the Z boundary of the shape
     * 
     * @param z
     *        start z position of Z range to test
     * @param sizeZ
     *        depth of Z range to test
     * @return true if the specified z range is inside the Z boundary of the shape
     */
    public boolean containsZ(double z, double sizeZ)
    {
        return (getMinZ() <= z) && (getMaxZ() >= (z + sizeZ));
    }

    /**
     * Test if a specified Z range intersects the Z boundary of the shape
     * 
     * @param z
     *        start z position of Z range to test
     * @param sizeZ
     *        depth of Z range to test
     * @return true if the specified Z range intersects the Z boundary of the shape
     */
    public boolean intersectsZ(double z, double sizeZ)
    {
        return (getMinZ() < (z + sizeZ)) && (getMaxZ() > z);
    }

    @Override
    public boolean contains(Point3D p)
    {
        return contains(p.getX(), p.getY(), p.getZ());
    }

    @Override
    public boolean intersects(Rectangle3D r)
    {
        return intersects(r.getX(), r.getY(), r.getZ(), r.getSizeX(), r.getSizeY(), r.getSizeZ());
    }

    @Override
    public boolean contains(Rectangle3D r)
    {
        return contains(r.getX(), r.getY(), r.getZ(), r.getSizeX(), r.getSizeY(), r.getSizeZ());
    }

    /**
     * @return base 2D shape
     */
    public abstract Shape getShape2D();

    /**
     * Returns an 2D path iterator object that iterates along the
     * <code>Shape</code> object's boundary and provides access to a
     * flattened view of the outline of the <code>Shape</code>
     * object's geometry.
     * <p>
     * Only SEG_MOVETO, SEG_LINETO, and SEG_CLOSE point types will
     * be returned by the iterator.
     * <p>
     * The amount of subdivision of the curved segments is controlled
     * by the <code>flatness</code> parameter, which specifies the
     * maximum distance that any point on the unflattened transformed
     * curve can deviate from the returned flattened path segments.
     * An optional {@link AffineTransform} can
     * be specified so that the coordinates returned in the iteration are
     * transformed accordingly.
     * 
     * @param at
     *        an optional <code>AffineTransform</code> to be applied to the
     *        coordinates as they are returned in the iteration,
     *        or <code>null</code> if untransformed coordinates are desired.
     * @param flatness
     *        the maximum distance that the line segments used to
     *        approximate the curved segments are allowed to deviate
     *        from any point on the original curve
     * @return a <code>PathIterator</code> object that provides access to
     *         the <code>Shape</code> object's flattened geometry.
     */
    public PathIterator get2DPathIterator(AffineTransform at, double flatness)
    {
        return new FlatteningPathIterator(getShape2D().getPathIterator(at), flatness);
    }

    /**
     * Creates a new object of the same class and with the same
     * contents as this object.
     * 
     * @return a clone of this instance.
     * @exception OutOfMemoryError
     *            if there is not enough memory.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
}
