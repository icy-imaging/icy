/**
 * 
 */
package plugins.kernel.roi.roi3d;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.painter.Anchor3D;
import icy.painter.LineAnchor3D;
import icy.resource.ResourceUtil;
import icy.type.collection.CollectionUtil;
import icy.type.geom.FlatPolygon3D;
import icy.type.geom.Polygon2D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.util.XMLUtil;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;

/**
 * Class defining a 3D Flat Polygon ROI.
 * 
 * @author Stephane
 */
public class ROI3DFlatPolygon extends ROI3DZShape
{
    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";

    protected class ROI3DPolygonAnchor3D extends LineAnchor3D
    {
        public ROI3DPolygonAnchor3D(Point3D position, Color color, Color selectedColor)
        {
            super(position, color, selectedColor);
        }

        @Override
        protected Anchor3D getPreviousPoint()
        {
            final int ind = controlPoints.indexOf(this);

            if (ind == 0)
            {
                if (controlPoints.size() > 1)
                    return controlPoints.get(1);

                return null;
            }

            if (ind != -1)
                return controlPoints.get(ind - 1);

            return null;
        }
    }

    public ROI3DFlatPolygon(FlatPolygon3D polygon3D)
    {
        // use empty 3D polygon
        super(new FlatPolygon3D());
        // as we rebuilt it here
        setPolygon3D(polygon3D);

        // set icon
        setIcon(ResourceUtil.ICON_ROI_FLATPOLYGON3D);
    }

    public ROI3DFlatPolygon(Polygon2D polygon, double z, double sizeZ)
    {
        this(new FlatPolygon3D(polygon, z, sizeZ));
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI3DFlatPolygon(Point5D pt)
    {
        this(new Polygon2D(CollectionUtil.createArrayList(pt.toPoint2D())), pt.getZ(), 0d);
    }

    public ROI3DFlatPolygon()
    {
        this(new FlatPolygon3D());
    }

    @Override
    public String getDefaultName()
    {
        return "FlatPolygon3D";
    }

    @Override
    protected Anchor3D createAnchor(Point3D pos)
    {
        return new ROI3DPolygonAnchor3D(pos, getColor(), getFocusedColor());
    }

    @Override
    protected ROI2DPolygon createROI2DShape()
    {
        return new ROI2DPolygon();
    }

    public FlatPolygon3D getPolygon3D()
    {
        return (FlatPolygon3D) shape;
    }

    public void setPolygon3D(FlatPolygon3D polygon3D)
    {
        beginUpdate();
        try
        {
            final double centerZ = polygon3D.getCenterZ();
            final List<Point2D> pts = polygon3D.getPoints();
            final List<Anchor3D> ctrlPts = getControlPoints();

            // same number of points ? (minus 2 as we have closeZ and farZ which are fixed)
            if (pts.size() == (ctrlPts.size() - 2))
            {
                for (int i = 0; i < pts.size(); i++)
                {
                    final Point2D newPt = pts.get(i);
                    // index + 2 as we have closeZ and farZ at index 0 and 1
                    final Anchor3D pt = ctrlPts.get(i + 2);

                    // set new position (this allow to not throw ROI change event if no changes)
                    pt.setPosition(newPt.getX(), newPt.getY(), centerZ);
                }
            }
            else
            {
                // simpler to just remove all points and set again
                removeAllPoint();
                for (Point2D pt : polygon3D.getPoints())
                    addNewPoint(new Point3D.Double(pt.getX(), pt.getY(), centerZ), false);
            }

            closeZ.setZ(polygon3D.getMinZ());
            farZ.setZ(polygon3D.getMaxZ());
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @param pts
     *        the list of 3points
     */
    public void setPoints(List<Point2D> pts)
    {
        beginUpdate();
        try
        {
            final List<Anchor3D> ctrlPts = getControlPoints();

            // same number of points ? (minus 2 as we have closeZ and farZ which are fixed)
            if (pts.size() == (ctrlPts.size() - 2))
            {
                for (int i = 0; i < pts.size(); i++)
                {
                    final Point2D newPt = pts.get(i);
                    // index + 2 as we have closeZ and farZ at index 0 and 1
                    final Anchor3D pt = ctrlPts.get(i + 2);

                    // set new position (this allow to not throw ROI change event if no changes)
                    pt.setPosition(newPt.getX(), newPt.getY(), pt.getZ());
                }
            }
            else
            {
                // simpler to just remove all points and set again
                removeAllPoint();
                for (Point2D pt : pts)
                    addNewPoint(pt, false);
            }
        }
        finally
        {
            endUpdate();
        }
    }

    public void setPolygon2D(Polygon2D polygon2D)
    {
        setPoints(polygon2D.getPoints());
    }

    /**
     * Add a new point to the Polyline 3D ROI.
     * 
     * @param pos
     *        position of the new point
     * @param insert
     *        if set to <code>true</code> the new point will be inserted between the 2 closest
     *        points (in pixels distance) else the new point is inserted at the end of the point
     *        list
     * @return the new created Anchor3D point
     */
    public Anchor3D addNewPoint(Point2D pos, boolean insert)
    {
        if (!canAddPoint())
            return null;

        return addNewPoint(new Point3D.Double(pos.getX(), pos.getY(), getZShape().getCenterZ()), insert);
    }

    @Override
    public boolean canSetBounds()
    {
        return true;
    }

    @Override
    public void setBounds3D(Rectangle3D bounds)
    {
        beginUpdate();
        try
        {
            // only support set Z bounds
            closeZ.setPosition(closeZ.getX(), closeZ.getY(), bounds.getMinZ());
            farZ.setPosition(farZ.getX(), farZ.getY(), bounds.getMaxZ());
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    protected void updateShape2DROI()
    {
        final ROI2DShape r = getShape2DROI();

        // that should be the case
        if (r instanceof ROI2DPolygon)
        {
            final ROI2DPolygon roi = (ROI2DPolygon) r;

            // update 2D roi from 3D shape
            roi.setPolygon2D(getPolygon3D().getShape2D());
        }
    }

    @Override
    protected void updateFromShape2DROI()
    {
        final ROI2DShape r = getShape2DROI();

        // that should be the case
        if (r instanceof ROI2DPolygon)
        {
            final ROI2DPolygon roi = (ROI2DPolygon) r;

            beginUpdate();
            try
            {
                // update 3D control points from 2D ROI
                setPolygon2D(roi.getPolygon2D());
            }
            finally
            {
                endUpdate();
            }
        }
    }

    @Override
    protected void updateShapeInternal()
    {
        // will update Z coordinates
        super.updateShapeInternal();
        // then update polygon
        getPolygon3D().setPoints(getPoints2D());
        // cached properties need to be recomputed
        boundsInvalid = true;

        // update Z control points XY positions
        final Rectangle3D bounds = getBounds3D();

        closeZ.setX(bounds.getCenterX());
        closeZ.setY(bounds.getCenterY());
        farZ.setX(bounds.getCenterX());
        farZ.setY(bounds.getCenterY());

        synchronized (controlPoints)
        {
            // update Z position of others points
            for (int i = 2; i < controlPoints.size(); i++)
                controlPoints.get(i).setZ(bounds.getCenterZ());
        }
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            removeAllPoint();

            final List<Node> nodesPoint = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null)
            {
                for (Node n : nodesPoint)
                {
                    final Anchor3D pt = createAnchor(new Point3D.Double());
                    pt.loadPositionFromXML(n);
                    addPoint(pt);
                }
            }
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        final Element nodePoints = XMLUtil.setElement(node, ID_POINTS);
        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                pt.savePositionToXML(XMLUtil.addElement(nodePoints, ID_POINT));
        }
        return true;
    }
}
