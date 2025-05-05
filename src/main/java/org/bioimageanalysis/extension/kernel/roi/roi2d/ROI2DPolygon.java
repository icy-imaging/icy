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
package org.bioimageanalysis.extension.kernel.roi.roi2d;

import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.poly.Polygon2D;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;
import org.bioimageanalysis.icy.model.overlay.anchor.LineAnchor2D;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * ROI 2D polygon class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI2DPolygon extends ROI2DShape {
    protected class ROI2DPolygonAnchor2D extends LineAnchor2D {
        public ROI2DPolygonAnchor2D(final Point2D position, final Color color, final Color selectedColor) {
            super(position, color, selectedColor);
        }

        @Override
        protected Anchor2D getPreviousPoint() {
            final int ind = controlPoints.indexOf(this);

            if (ind == 0) {
                if (controlPoints.size() > 1)
                    return controlPoints.get(1);

                return null;
            }

            if (ind != -1)
                return controlPoints.get(ind - 1);

            return null;
        }
    }

    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";

    public ROI2DPolygon(final Point2D pt) {
        super(new Polygon2D());

        final Anchor2D point = createAnchor(pt);
        point.setSelected(true);
        addPoint(point);

        // set icon (default name is defined by getDefaultName())
        setIcon(SVGIcon.PENTAGON);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPolygon(final Point5D pt) {
        this(pt.toPoint2D());
        // getOverlay().setMousePos(pt);
    }

    public ROI2DPolygon(final List<Point2D> points) {
        this(new Point2D.Double());

        setPoints(points);
        unselectAllPoints();
    }

    public ROI2DPolygon(final Polygon2D polygon) {
        this(new Point2D.Double());

        setPolygon2D(polygon);
        unselectAllPoints();
    }

    public ROI2DPolygon() {
        this(new Point2D.Double());
    }

    @Override
    public String getDefaultName() {
        return "Polygon2D";
    }

    @Override
    protected Anchor2D createAnchor(final Point2D pos) {
        return new ROI2DPolygonAnchor2D(pos, getColor(), getFocusedColor());
    }

    public void setPoints(final List<Point2D> pts) {
        beginUpdate();
        try {
            final List<Anchor2D> ctrlPts = getControlPoints();

            // same number of points ?
            if (pts.size() == ctrlPts.size()) {
                for (int i = 0; i < pts.size(); i++) {
                    final Point2D newPt = pts.get(i);
                    final Anchor2D pt = ctrlPts.get(i);

                    // set new position (this allow to not throw ROI change event if no changes)
                    pt.setPosition(newPt.getX(), newPt.getY());
                }
            }
            else {
                // simpler to just remove all points and set again
                removeAllPoint();
                for (final Point2D pt : pts)
                    addNewPoint(pt, false);
            }
        }
        finally {
            endUpdate();
        }
    }

    public Polygon2D getPolygon2D() {
        return (Polygon2D) shape;
    }

    public void setPolygon2D(final Polygon2D polygon2D) {
        setPoints(polygon2D.getPoints());
    }

    public Polygon getPolygon() {
        return getPolygon2D().getPolygon();
    }

    public void setPolygon(final Polygon polygon) {
        setPolygon2D(new Polygon2D(polygon));
    }

    @Override
    protected void updateShape() {
        final int len;
        final double[] ptsX;
        final double[] ptsY;

        synchronized (controlPoints) {
            len = controlPoints.size();
            ptsX = new double[len];
            ptsY = new double[len];

            for (int i = 0; i < len; i++) {
                final Anchor2D pt = controlPoints.get(i);

                ptsX[i] = pt.getX();
                ptsY[i] = pt.getY();
            }
        }

        final Polygon2D polygon2d = getPolygon2D();

        // we can have a problem here if we try to redraw while we are modifying the polygon points
        synchronized (polygon2d) {
            polygon2d.npoints = len;
            polygon2d.xpoints = ptsX;
            polygon2d.ypoints = ptsY;
            polygon2d.calculatePath();
        }

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean loadFromXML(final Node node) {
        beginUpdate();
        try {
            if (!super.loadFromXML(node))
                return false;

            removeAllPoint();

            final List<Node> nodesPoint = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null) {
                for (final Node n : nodesPoint) {
                    final Anchor2D pt = createAnchor(new Point2D.Double());
                    pt.loadPositionFromXML(n);
                    addPoint(pt);
                }
            }
        }
        finally {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(final Node node) {
        if (!super.saveToXML(node))
            return false;

        final Element nodePoints = XMLUtil.setElement(node, ID_POINTS);
        synchronized (controlPoints) {
            for (final Anchor2D pt : controlPoints)
                pt.savePositionToXML(XMLUtil.addElement(nodePoints, ID_POINT));
        }
        return true;
    }
}
