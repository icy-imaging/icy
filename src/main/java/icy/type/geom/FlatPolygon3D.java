package icy.type.geom;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import icy.type.rectangle.Rectangle3D;

public class FlatPolygon3D extends ZShape3D
{
    /**
     * base polygon 2D
     */
    public Polygon2D polygon;

    /**
     * Z position
     */
    public double z;
    /**
     * Z size
     */
    public double sizeZ;

    public FlatPolygon3D(Polygon2D p, double z, double sizeZ)
    {
        super();

        polygon = new Polygon2D(p);
        this.z = z;
        this.sizeZ = sizeZ;
    }

    public FlatPolygon3D()
    {
        this(new Polygon2D(), 0d, 0d);
    }

    @Override
    public double getZ()
    {
        return z;
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
    @Override
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
    @Override
    public void setSizeZ(double value)
    {
        sizeZ = value;
    }

    /**
     * @return the list of (2D) points defining the polygon
     */
    public List<Point2D> getPoints()
    {
        return polygon.getPoints();
    }

    /**
     * Set the polygon from a list of (2D) points
     */
    public void setPoints(List<Point2D> points)
    {
        polygon.setPoints(points);
    }

    @Override
    public boolean contains(double x, double y, double z)
    {
        return polygon.contains(x, y) && containsZ(z);
    }

    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return polygon.intersects(x, y, sizeX, sizeY) && intersectsZ(z, sizeZ);
    }

    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return polygon.contains(sizeX, y, sizeX, sizeY) && containsZ(z, sizeZ);
    }

    @Override
    public Polygon2D getShape2D()
    {
        return new Polygon2D(polygon);
    }

    @Override
    public Rectangle3D getBounds()
    {
        final Rectangle2D bnd2d = polygon.getBounds2D();

        return new Rectangle3D.Double(bnd2d.getX(), bnd2d.getY(), z, bnd2d.getWidth(), bnd2d.getHeight(), sizeZ);
    }
}
