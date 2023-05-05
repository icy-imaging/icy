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
package plugins.kernel.roi.roi2d;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.painter.Anchor2D;
import icy.painter.PathAnchor2D;
import icy.resource.ResourceUtil;
import icy.roi.ROI;
import icy.type.geom.areax.AreaX;
import icy.type.point.Point5D;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

/**
 * ROI Path.<br>
 * This ROI can display a Path2D shape.<br>
 * You can modify and remove points (adding new point isn't supported).
 * 
 * @author Stephane
 */
public class ROI2DPath extends ROI2DShape
{
    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";
    public static final String ID_WINDING = "winding";

    protected AreaX closedAreaX;
    protected Path2D openPath;

    static Path2D initPath(Point2D position)
    {
        final Path2D result = new Path2D.Double();

        result.reset();
        if (position != null)
            result.moveTo(position.getX(), position.getY());

        return result;
    }

    /**
     * Build a new ROI2DPath from the specified path.
     */
    public ROI2DPath(Path2D path, AreaX closedAreaX, Path2D openPath)
    {
        super(path);

        rebuildControlPointsFromPath();

        if (closedAreaX == null)
            this.closedAreaX = new AreaX(ShapeUtil.getClosedPath(path));
        else
            this.closedAreaX = closedAreaX;
        if (openPath == null)
            this.openPath = ShapeUtil.getOpenPath(path);
        else
            this.openPath = openPath;

        // set icon (default name is defined by getDefaultName())
        setIcon(ResourceUtil.ICON_ROI_POLYLINE);
    }

    /**
     * Build a new ROI2DPath from the specified path.
     */
    public ROI2DPath(Path2D path)
    {
        this(path, null, null);

    }

    /**
     * Build a new ROI2DPath from the specified path.
     */
    public ROI2DPath(Shape shape)
    {
        this(new Path2D.Double(shape), (shape instanceof AreaX) ? (AreaX) shape : null, null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPath(Point2D pt, boolean cm)
    {
        this(pt);
    }

    public ROI2DPath(Point2D position)
    {
        this(initPath(position));
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPath(Point5D pt)
    {
        this(pt.toPoint2D());
    }

    public ROI2DPath()
    {
        this(new Path2D.Double(Path2D.WIND_NON_ZERO));
    }

    @Override
    public String getDefaultName()
    {
        return "Path2D";
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new PathAnchor2D(pos.getX(), pos.getY(), getColor(), getFocusedColor());
    }

    protected void rebuildControlPointsFromPath()
    {
        beginUpdate();
        try
        {
            // remove all point
            removeAllPoint();

            // add path points to the control point list
            for (Anchor2D pt : ShapeUtil.getAnchorsFromShape(getPath(), getColor(), getFocusedColor()))
                addPoint(pt);
        }
        finally
        {
            endUpdate();
        }
    }

    protected Path2D getPath()
    {
        return (Path2D) shape;
    }

    /**
     * Returns the closed AreaX part of the ROI2DPath in {@link AreaX} shape format
     */
    public AreaX getClosedAreaX()
    {
        return closedAreaX;
    }

    /**
     * Returns the open path part of the ROI2DPath in {@link Path2D} shape format
     */
    public Path2D getOpenPath()
    {
        return openPath;
    }

    @Override
    public boolean canAddPoint()
    {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    public boolean contains(double x, double y)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(x, y);
    }

    @Override
    public boolean contains(Point2D p)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(p);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(r);
    }

    @Override
    public boolean contains(ROI roi) throws InterruptedException
    {
        // not closed --> do not contains anything
        if (!ShapeUtil.isClosed(shape))
            return false;

        return super.contains(roi);
    }

    public List<ROI2DShape> addFast(List<ROI2DShape> rois) throws InterruptedException
    {
        final List<ROI2DShape> discardedRois = new ArrayList<>();

        for (ROI2DShape roi : rois)
        {
            if (Thread.interrupted())
                throw new InterruptedException("ROI OR merging process interrupted.");

            // only if on same position
            if ((getZ() == roi.getZ()) && (getT() == roi.getT()) && (getC() == roi.getC()))
            {
                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed AreaX and open path parts
                    closedAreaX.add(roiPath.closedAreaX);
                    openPath.append(roiPath.openPath, false);
                }
                else
                {
                    final Shape sh = roi.getShape();

                    // compute closed AreaX and open path parts
                    if (sh instanceof AreaX)
                        closedAreaX.add((AreaX) sh);
                    else
                        closedAreaX.add(new AreaX(ShapeUtil.getClosedPath(sh)));
                    openPath.append(ShapeUtil.getOpenPath(sh), false);
                }
            }
            else
                discardedRois.add(roi);
        }

        return discardedRois;
    }

    public List<ROI2DShape> intersectFast(List<ROI2DShape> rois) throws InterruptedException
    {
        final List<ROI2DShape> discardedRois = new ArrayList<>();

        if (!rois.isEmpty())
            openPath.reset();

        for (ROI2DShape roi : rois)
        {
            if (Thread.interrupted())
                throw new InterruptedException("ROI AND merging process interrupted.");

            // only if on same position
            if ((getZ() == roi.getZ()) && (getT() == roi.getT()) && (getC() == roi.getC()))
            {
                if (roi instanceof ROI2DPath)
                {
                    // compute closed AreaX intersection and clear open path
                    closedAreaX.intersect(((ROI2DPath) roi).closedAreaX);
                }
                else
                {
                    final Shape sh = roi.getShape();

                    // compute closed AreaX intersection and clear open path
                    if (sh instanceof AreaX)
                        closedAreaX.intersect((AreaX) sh);
                    else
                        closedAreaX.intersect(new AreaX(ShapeUtil.getClosedPath(sh)));
                }
            }
            else
                discardedRois.add(roi);
        }

        return discardedRois;
    }

    public List<ROI2DShape> exclusiveAddFast(List<ROI2DShape> rois) throws InterruptedException
    {
        final List<ROI2DShape> discardedRois = new ArrayList<>();

        for (ROI2DShape roi : rois)
        {
            if (Thread.interrupted())
                throw new InterruptedException("ROI XOR merging process interrupted.");

            // only if on same position
            if ((getZ() == roi.getZ()) && (getT() == roi.getT()) && (getC() == roi.getC()))
            {
                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute exclusive union on closed AreaX and simple append for open path
                    closedAreaX.exclusiveOr(roiPath.closedAreaX);
                    openPath.append(roiPath.openPath, false);
                }
                else
                {
                    final Shape sh = roi.getShape();

                    // compute exclusive union on closed AreaX and simple append for open path
                    if (sh instanceof AreaX)
                        closedAreaX.exclusiveOr((AreaX) sh);
                    else
                        closedAreaX.exclusiveOr(new AreaX(ShapeUtil.getClosedPath(sh)));
                    openPath.append(ShapeUtil.getOpenPath(sh), false);
                }
            }
            else
                discardedRois.add(roi);
        }

        return discardedRois;
    }

    public void updatePath()
    {
        final Path2D path = getPath();

        // then rebuild path from closed and open parts
        path.reset();
        path.append(closedAreaX, false);
        path.append(openPath, false);

        rebuildControlPointsFromPath();
        roiChanged(true);
    }

    @Override
    public ROI add(ROI roi, boolean allowCreate) throws UnsupportedOperationException, InterruptedException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed AreaX and open path parts
                    closedAreaX.add(roiPath.closedAreaX);
                    openPath.append(roiPath.openPath, false);
                }
                else
                {
                    // compute closed AreaX and open path parts
                    if (roiShape.getShape() instanceof AreaX)
                        closedAreaX.add((AreaX) roiShape.getShape());
                    else
                        closedAreaX.add(new AreaX(ShapeUtil.getClosedPath(roiShape)));
                    openPath.append(ShapeUtil.getOpenPath(roiShape), false);
                }

                // then rebuild path from closed and open parts
                path.reset();
                path.append(closedAreaX, false);
                path.append(openPath, false);

                rebuildControlPointsFromPath();
                roiChanged(true);

                return this;
            }
        }

        return super.add(roi, allowCreate);
    }

    @Override
    public ROI intersect(ROI roi, boolean allowCreate) throws UnsupportedOperationException, InterruptedException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed AreaX intersection and clear open path
                    closedAreaX.intersect(roiPath.closedAreaX);
                    openPath.reset();
                }
                else
                {
                    // compute closed AreaX intersection and clear open path
                    if (roiShape.getShape() instanceof AreaX)
                        closedAreaX.intersect((AreaX) roiShape.getShape());
                    else
                        closedAreaX.intersect(new AreaX(ShapeUtil.getClosedPath(roiShape)));
                    openPath.reset();
                }

                // then rebuild path from closed AreaX (open part is empty)
                path.reset();
                path.append(closedAreaX, false);

                rebuildControlPointsFromPath();
                roiChanged(true);

                return this;
            }
        }

        return super.intersect(roi, allowCreate);
    }

    @Override
    public ROI exclusiveAdd(ROI roi, boolean allowCreate) throws UnsupportedOperationException, InterruptedException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute exclusive union on closed AreaX and simple append for open path
                    closedAreaX.exclusiveOr(roiPath.closedAreaX);
                    openPath.append(roiPath.openPath, false);
                }
                else
                {
                    // compute exclusive union on closed AreaX and simple append for open path
                    if (roiShape.getShape() instanceof AreaX)
                        closedAreaX.exclusiveOr((AreaX) roiShape.getShape());
                    else
                        closedAreaX.exclusiveOr(new AreaX(ShapeUtil.getClosedPath(roiShape)));
                    openPath.append(ShapeUtil.getOpenPath(roiShape), false);
                }

                // then rebuild path from closed and open parts
                path.reset();
                path.append(closedAreaX, false);
                path.append(openPath, false);

                rebuildControlPointsFromPath();
                roiChanged(true);

                return this;
            }
        }

        return super.exclusiveAdd(roi, allowCreate);
    }

    @Override
    public ROI subtract(ROI roi, boolean allowCreate) throws UnsupportedOperationException, InterruptedException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed AreaX intersection and clear open path parts
                    closedAreaX.exclusiveOr(roiPath.closedAreaX);
                    if (!roiPath.closedAreaX.isEmpty())
                        openPath.reset();
                }
                else
                {
                    final AreaX AreaX;

                    // compute closed AreaX and open path parts
                    if (roiShape.getShape() instanceof AreaX)
                        AreaX = (AreaX) roiShape.getShape();
                    else
                        AreaX = new AreaX(ShapeUtil.getClosedPath(roiShape));
                    if (!AreaX.isEmpty())
                    {
                        closedAreaX.exclusiveOr(AreaX);
                        openPath.reset();
                    }
                }

                // then rebuild path from closed and open parts
                path.reset();
                path.append(closedAreaX, false);
                path.append(openPath, false);

                rebuildControlPointsFromPath();
                roiChanged(true);

                return this;
            }
        }

        return super.subtract(roi, allowCreate);
    }

    /**
     * Return the list of control points for this ROI.
     */
    public List<PathAnchor2D> getPathAnchors()
    {
        final List<PathAnchor2D> result = new ArrayList<PathAnchor2D>();

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                result.add((PathAnchor2D) pt);
        }

        return result;
    }

    protected void updateCachedStructures()
    {
        closedAreaX = new AreaX(ShapeUtil.getClosedPath(getPath()));
        openPath = ShapeUtil.getOpenPath(getPath());
    }

    @Override
    protected void updateShape()
    {
        ShapeUtil.buildPathFromAnchors(getPath(), getPathAnchors(), false);
        // update internal closed AreaX and open path
        updateCachedStructures();

        // call super method after shape has been updated
        super.updateShape();
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
                    final PathAnchor2D pt = (PathAnchor2D) createAnchor(new Point2D.Double());
                    pt.loadPositionFromXML(n);
                    addPoint(pt);
                }
            }

            getPath().setWindingRule(XMLUtil.getElementIntValue(node, ID_WINDING, Path2D.WIND_NON_ZERO));
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

        final Element points = XMLUtil.setElement(node, ID_POINTS);

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                pt.savePositionToXML(XMLUtil.addElement(points, ID_POINT));
        }

        XMLUtil.setElementIntValue(node, ID_WINDING, getPath().getWindingRule());

        return true;
    }
}
