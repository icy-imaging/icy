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

import java.awt.geom.Dimension2D;
import java.beans.Transient;

import org.bioimageanalysis.icy.common.geom.dimension.Dimension3D;
import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle3D;

/**
 * The <code>BoxShape3D</code> interface provides definitions for objects
 * that represent some form of 3D boxed geometric.
 */
public abstract class BoxShape3D extends ZShape3D
{
    /**
     * This is an abstract class that cannot be instantiated directly.
     */
    protected BoxShape3D()
    {
        super();
    }

    /**
     * Returns the X coordinate of the close-upper-left corner of
     * the framing box in <code>double</code> precision.
     * 
     * @return the X coordinate of the close-upper-left corner of
     *         the framing box.
     */
    public abstract double getX();

    /**
     * Returns the Y coordinate of the close-upper-left corner of
     * the framing box in <code>double</code> precision.
     * 
     * @return the Y coordinate of the close-upper-left corner of
     *         the framing box.
     */
    public abstract double getY();

    /**
     * Returns the width of the framing box in
     * <code>double</code> precision.
     * 
     * @return the width of the framing box.
     */
    public abstract double getSizeX();

    /**
     * Returns the height of the framing box
     * in <code>double</code> precision.
     * 
     * @return the height of the framing box.
     */
    public abstract double getSizeY();

    /**
     * Returns the smallest X coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the smallest X coordinate of the framing
     *         rectangle of the <code>Shape</code>.
     */
    public double getMinX()
    {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the smallest Y coordinate of the framing
     *         rectangle of the <code>Shape</code>.
     */
    public double getMinY()
    {
        return getY();
    }

    /**
     * Returns the largest X coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the largest X coordinate of the framing
     *         rectangle of the <code>Shape</code>.
     */
    public double getMaxX()
    {
        return getX() + getSizeX();
    }

    /**
     * Returns the largest Y coordinate of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the largest Y coordinate of the framing
     *         rectangle of the <code>Shape</code>.
     */
    public double getMaxY()
    {
        return getY() + getSizeY();
    }

    /**
     * Returns the X coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the X coordinate of the center of the framing box
     *         of the <code>Shape</code>.
     */
    public double getCenterX()
    {
        return getX() + getSizeX() / 2.0;
    }

    /**
     * Returns the Y coordinate of the center of the framing
     * rectangle of the <code>Shape</code> in <code>double</code>
     * precision.
     * 
     * @return the Y coordinate of the center of the framing box
     *         of the <code>Shape</code>.
     */
    public double getCenterY()
    {
        return getY() + getSizeY() / 2.0;
    }

    /**
     * Returns the framing {@link Rectangle3D}
     * that defines the overall shape of this object.
     * 
     * @return a <code>Rectangle3D</code>, specified in
     *         <code>double</code> coordinates.
     * @see #setFrame(double, double, double, double, double, double)
     * @see #setFrame(Point3D, Dimension3D)
     * @see #setFrame(Rectangle3D)
     */
    @Transient
    public Rectangle3D getFrame()
    {
        return new Rectangle3D.Double(getX(), getY(), getZ(), getSizeX(), getSizeY(), getSizeZ());
    }

    /**
     * Sets the location and size of the framing box of this
     * <code>Shape</code> to the specified rectangular values.
     *
     * @param x
     *        the X coordinate of the close-upper-left corner of the
     *        specified box shape
     * @param y
     *        the Y coordinate of the close-upper-left corner of the
     *        specified box shape
     * @param z
     *        the Z coordinate of the close-upper-left corner of the
     *        specified box shape
     * @param sx
     *        the width of the specified box shape
     * @param sy
     *        the height of the specified box shape
     * @param sz
     *        the depth of the specified box shape
     * @see #getFrame
     */
    public abstract void setFrame(double x, double y, double z, double sx, double sy, double sz);

    /**
     * Sets the location and size of the framing box of this
     * <code>Shape</code> to the specified {@link Point3D} and
     * {@link Dimension2D}, respectively. The framing box is used
     * by the subclasses of <code>BoxShape3D</code> to define
     * their geometry.
     * 
     * @param loc
     *        the specified <code>Point3D</code>
     * @param size
     *        the specified <code>Dimension2D</code>
     * @see #getFrame
     */
    public void setFrame(Point3D loc, Dimension3D size)
    {
        setFrame(loc.getX(), loc.getY(), loc.getZ(), size.getSizeX(), size.getSizeY(), size.getSizeZ());
    }

    /**
     * Sets the framing box of this <code>Shape</code> to
     * be the specified <code>Rectangle3D</code>. The framing box is
     * used by the subclasses of <code>BoxShape3D</code> to define
     * their geometry.
     * 
     * @param r
     *        the specified <code>Rectangle3D</code>
     * @see #getFrame
     */
    public void setFrame(Rectangle3D r)
    {
        setFrame(r.getX(), r.getY(), r.getZ(), r.getSizeX(), r.getSizeY(), r.getSizeZ());
    }

    /**
     * Sets the diagonal of the framing box of this <code>Shape</code>
     * based on the two specified coordinates. The framing box is
     * used by the subclasses of <code>BoxShape3D</code> to define
     * their geometry.
     *
     * @param x1
     *        the X coordinate of the start point of the specified diagonal
     * @param y1
     *        the Y coordinate of the start point of the specified diagonal
     * @param z1
     *        the Z coordinate of the start point of the specified diagonal
     * @param x2
     *        the X coordinate of the end point of the specified diagonal
     * @param y2
     *        the Y coordinate of the end point of the specified diagonal
     * @param z2
     *        the Z coordinate of the end point of the specified diagonal
     */
    public void setFrameFromDiagonal(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        if (x2 < x1)
        {
            double t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y2 < y1)
        {
            double t = y1;
            y1 = y2;
            y2 = t;
        }
        if (z2 < z1)
        {
            double t = z1;
            z1 = z2;
            z2 = t;
        }
        setFrame(x1, y1, z1, x2 - x1, y2 - y1, z2 - z1);
    }

    /**
     * Sets the diagonal of the framing box of this <code>Shape</code>
     * based on two specified <code>Point3D</code> objects. The framing
     * rectangle is used by the subclasses of <code>BoxShape3D</code>
     * to define their geometry.
     *
     * @param p1
     *        the start <code>Point3D</code> of the specified diagonal
     * @param p2
     *        the end <code>Point3D</code> of the specified diagonal
     */
    public void setFrameFromDiagonal(Point3D p1, Point3D p2)
    {
        setFrameFromDiagonal(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
    }

    /**
     * Sets the framing box of this <code>Shape</code>
     * based on the specified center point coordinates and corner point
     * coordinates. The framing box is used by the subclasses of
     * <code>BoxShape3D</code> to define their geometry.
     *
     * @param centerX
     *        the X coordinate of the specified center point
     * @param centerY
     *        the Y coordinate of the specified center point
     * @param centerZ
     *        the Z coordinate of the specified center point
     * @param cornerX
     *        the X coordinate of the specified corner point
     * @param cornerY
     *        the Y coordinate of the specified corner point
     * @param cornerZ
     *        the Z coordinate of the specified corner point
     */
    public void setFrameFromCenter(double centerX, double centerY, double centerZ, double cornerX, double cornerY,
            double cornerZ)
    {
        double halfX = Math.abs(cornerX - centerX);
        double halfY = Math.abs(cornerY - centerY);
        double halfZ = Math.abs(cornerZ - centerZ);

        setFrame(centerX - halfX, centerY - halfY, centerZ - halfZ, halfX * 2d, halfY * 2d, halfZ * 2d);
    }

    /**
     * Sets the framing box of this <code>Shape</code> based on a
     * specified center <code>Point3D</code> and corner
     * <code>Point3D</code>. The framing box is used by the subclasses
     * of <code>BoxShape3D</code> to define their geometry.
     * 
     * @param center
     *        the specified center <code>Point3D</code>
     * @param corner
     *        the specified corner <code>Point3D</code>
     */
    public void setFrameFromCenter(Point3D center, Point3D corner)
    {
        setFrameFromCenter(center.getX(), center.getY(), center.getZ(), corner.getX(), corner.getY(), corner.getZ());
    }

    @Override
    public Rectangle3D getBounds()
    {
        return new Rectangle3D.Double(getX(), getY(), getZ(), getSizeX(), getSizeY(), getSizeZ());
    }
}
