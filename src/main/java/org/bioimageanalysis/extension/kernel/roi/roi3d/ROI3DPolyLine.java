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

package org.bioimageanalysis.extension.kernel.roi.roi3d;

import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.geom.line.Line3D;
import org.bioimageanalysis.icy.common.geom.line.Line3DIterator;
import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.poly.Polyline3D;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.gui.canvas.VtkCanvas;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.gui.render.IcyVtkPanel;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor3D;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIEvent;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import vtk.vtkTubeFilter;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D Polyline ROI
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI3DPolyLine extends ROI3DShape {
    public class ROI3DPolyLinePainter extends ROI3DShapePainter {
        // extra VTK 3D objects
        protected vtkTubeFilter tubeFilter;

        /**
         * Constructor
         */
        public ROI3DPolyLinePainter() {
            super();

            // don't create VTK object on constructor
            tubeFilter = null;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            // release allocated VTK resources
            if (tubeFilter != null)
                tubeFilter.Delete();
        }

        @Override
        protected void initVtkObjects() {
            super.initVtkObjects();

            // init specific tube filter
            tubeFilter = new vtkTubeFilter();
            tubeFilter.SetInputData(polyData);
            tubeFilter.SetRadius(1d);
            tubeFilter.CappingOn();
            tubeFilter.SetNumberOfSides(8);
            // tubeFilter.SidesShareVerticesOff();
            polyMapper.SetInputConnection(tubeFilter.GetOutputPort());
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected void rebuildVtkObjects() {
            super.rebuildVtkObjects();

            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            // sub VTK object not yet initialized (it can happen, have to check why ??)
            if (tubeFilter == null)
                return;

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try {
                // just be sure the tube filter is also up to date
                tubeFilter.Update();
            }
            finally {
                vtkPanel.unlock();
            }
        }

        protected void updateVtkTubeRadius() {
            // VTK object not yet initialized
            if (actor == null)
                return;

            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            // sub VTK object not yet initialized (it can happen, have to check why ??)
            if (tubeFilter == null)
                return;

            // update tube radius base on canvas scale X and image scale X
            final double radius = canvas.canvasToImageLogDeltaX((int) getStroke()) * scaling[0];

            if (tubeFilter.GetRadius() != radius) {
                // actor can be accessed in canvas3d for rendering so we need to synchronize access
                vtkPanel.lock();
                try {
                    tubeFilter.SetRadius(radius);
                    tubeFilter.Update();
                }
                finally {
                    vtkPanel.unlock();
                }

                // need to repaint
                painterChanged();
            }
        }

        @Override
        public void drawROI(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
            super.drawROI(g, sequence, canvas);

            // update VTK tube radius if needed
            if (canvas instanceof VtkCanvas)
                updateVtkTubeRadius();
        }

        @Override
        protected void drawShape(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas, final boolean simplified) {
            drawShape(g, sequence, canvas, simplified, false);
        }
    }

    /**
     * Construct 3D polyline ROI
     *
     * @param pt
     *        source 3D point
     */
    public ROI3DPolyLine(final Point3D pt) {
        super(new Polyline3D());

        // add points to list
        final Anchor3D anchor = createAnchor(pt);
        // just add the new point at last position
        addPoint(anchor);
        // always select
        anchor.setSelected(true);

        updateShape();

        // set icon
        setIcon(SVGResource.ROI_POLYLINE);
    }

    /**
     * Generic constructor for interactive mode
     *
     * @param pt
     *        source 5D point
     */
    public ROI3DPolyLine(final Point5D pt) {
        this(pt.toPoint3D());
    }

    /**
     * Construct 3D polyline ROI
     *
     * @param polyline
     *        source polyline
     */
    public ROI3DPolyLine(final Polyline3D polyline) {
        this(new Point3D.Double());

        setPolyline3D(polyline);
    }

    /**
     * Construct 3D polyline ROI
     *
     * @param points
     *        source 3D points list
     */
    public ROI3DPolyLine(final List<Point3D> points) {
        this(new Point3D.Double());

        setPoints(points);
    }

    /**
     * Construct 3D polyline ROI
     */
    public ROI3DPolyLine() {
        this(new Point3D.Double());
    }

    @Override
    public String getDefaultName() {
        return "PolyLine3D";
    }

    @Override
    protected ROI3DPolyLinePainter createPainter() {
        return new ROI3DPolyLinePainter();
    }

    /**
     * @return 3D polyline shape
     */
    public Polyline3D getPolyline3D() {
        return (Polyline3D) shape;
    }

    /**
     * Set ROI from lst of points
     *
     * @param pts
     *        source 3D points list
     */
    public void setPoints(final List<Point3D> pts) {
        beginUpdate();
        try {
            removeAllPoint();
            for (final Point3D pt : pts)
                addNewPoint(pt, false);
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Set ROI from shape
     *
     * @param value
     *        the source shape
     */
    public void setPolyline3D(final Polyline3D value) {
        beginUpdate();
        try {
            removeAllPoint();
            for (int i = 0; i < value.npoints; i++)
                addNewPoint(new Point3D.Double(value.xpoints[i], value.ypoints[i], value.zpoints[i]), false);
        }
        finally {
            endUpdate();
        }
    }

    @Override
    protected double getTotalDistance(final List<Point3D> points, final double factorX, final double factorY, final double factorZ) {
        // for polyline the total length don't need last point connection
        return Point3D.getTotalDistance(points, factorX, factorY, factorZ, false);
    }

    @Override
    public boolean[] getBooleanMask2D(final int x, final int y, final int width, final int height, final int z, final boolean inclusive) {
        if ((width <= 0) || (height <= 0))
            return new boolean[0];

        final List<Point3D> points = getPointsInternal();
        final boolean[] result = new boolean[width * height];

        // 2D bounds
        final Rectangle bounds2d = new Rectangle(x, y, width, height);

        for (int i = 1; i < points.size(); i++)
            drawLine3DInBooleanMask2D(bounds2d, result, z, points.get(i - 1), points.get(i));

        return result;
    }

    /**
     * Draw (print) a 3D line into the given 2D BooleanMask
     *
     * @param bounds2d
     *        boolean mask bounds
     * @param result
     *        boolean mask array
     * @param z
     *        Z position
     * @param p1
     *        start 3D point of line
     * @param p2
     *        end 3D point of line
     */
    public static void drawLine3DInBooleanMask2D(final Rectangle bounds2d, final boolean[] result, final int z, final Point3D p1, final Point3D p2) {
        final Line2D l = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());

        // 2D intersection ?
        if (l.intersects(bounds2d)) {
            // 3D intersection ?
            if (((p1.getZ() <= z) && (p2.getZ() >= z)) || ((p2.getZ() <= z) && (p1.getZ() >= z))) {
                final int bx = bounds2d.x;
                final int by = bounds2d.y;
                final int pitch = bounds2d.width;
                final Line3DIterator it = new Line3DIterator(new Line3D(p1, p2), 1d);

                while (it.hasNext()) {
                    final Point3D pt = it.next();

                    // same Z ?
                    if (Math.floor(pt.getZ()) == z) {
                        final int x = (int) Math.floor(pt.getX());
                        final int y = (int) Math.floor(pt.getY());

                        // draw inside the mask
                        if (bounds2d.contains(x, y))
                            result[(x - bx) + ((y - by) * pitch)] = true;
                    }
                }
            }
        }
    }

    /**
     * roi changed
     */
    @Override
    public void onChanged(final CollapsibleEvent object) {
        final ROIEvent event = (ROIEvent) object;

        // do here global process on ROI change
        switch (event.getType()) {
            case ROI_CHANGED:
                // refresh shape
                updateShape();
                break;

            case FOCUS_CHANGED:
                ((ROI3DPolyLinePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case SELECTION_CHANGED:
                final boolean s = isSelected();

                // update controls point state given the selection state of the ROI
                synchronized (controlPoints) {
                    for (final Anchor3D pt : controlPoints) {
                        pt.setVisible(s);
                        if (!s)
                            pt.setSelected(false);
                    }
                }

                ((ROI3DPolyLinePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                if (StringUtil.equals(property, PROPERTY_STROKE) || StringUtil.equals(property, PROPERTY_COLOR)
                        || StringUtil.equals(property, PROPERTY_OPACITY))
                    ((ROI3DPolyLinePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            default:
                break;
        }

        super.onChanged(object);
    }

    @Override
    public double computeNumberOfPoints() {
        return 0d;
    }

    @Override
    public boolean contains(final ROI roi) {
        return false;
    }

    @Override
    protected void updateShape() {
        final int len = controlPoints.size();
        final double[] ptsX = new double[len];
        final double[] ptsY = new double[len];
        final double[] ptsZ = new double[len];

        for (int i = 0; i < len; i++) {
            final Anchor3D pt = controlPoints.get(i);

            ptsX[i] = pt.getX();
            ptsY[i] = pt.getY();
            ptsZ[i] = pt.getZ();
        }

        final Polyline3D polyline3d = getPolyline3D();

        // we can have a problem here if we try to redraw while we are modifying the polygon points
        synchronized (polyline3d) {
            polyline3d.npoints = len;
            polyline3d.xpoints = ptsX;
            polyline3d.ypoints = ptsY;
            polyline3d.zpoints = ptsZ;
            polyline3d.calculateLines();
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

            final ArrayList<Node> nodesPoint = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null) {
                for (final Node n : nodesPoint) {
                    final Anchor3D pt = createAnchor(new Point3D.Double());
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

        final Element dependances = XMLUtil.setElement(node, ID_POINTS);
        for (final Anchor3D pt : controlPoints)
            pt.savePositionToXML(XMLUtil.addElement(dependances, ID_POINT));

        return true;
    }
}
