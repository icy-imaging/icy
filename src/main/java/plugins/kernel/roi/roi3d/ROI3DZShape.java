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
package plugins.kernel.roi.roi3d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.painter.Anchor3D;
import icy.painter.OverlayEvent;
import icy.painter.OverlayListener;
import icy.painter.ZBoxAnchor3D;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.sequence.Sequence;
import icy.type.geom.ZShape3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.util.ColorUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkUtil;
import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.roi.roi2d.ROI2DShape;
import plugins.kernel.roi.roi2d.ROI2DShape.ROI2DShapePainter;
import vtk.vtkCellArray;
import vtk.vtkPoints;

/**
 * Base class defining a generic 3D ROI as an Z extended 2DShape
 * 
 * @author Stephane Dallongeville
 */
public abstract class ROI3DZShape extends ROI3DShape
{
    public static final String ID_BASE_ROI = "shape2DROI";
    public static final String ID_POS_Z = "z";
    public static final String ID_SIZE_Z = "sizeZ";
    public static final String ID_CLOSEZ = "close_z";
    public static final String ID_FARZ = "far_z";

    /**
     * base 2D Shape roi
     */
    protected final ROI2DShape shape2DROI;

    protected final ZBoxAnchor3D closeZ;
    protected final ZBoxAnchor3D farZ;

    /**
     * Creates a new 3D ROI based on the given Z extended 3D shape.
     * 
     * @param shape
     *        base 3D shape
     */
    public ROI3DZShape(ZShape3D shape)
    {
        super(shape);

        final Rectangle3D bounds = shape.getBounds();

        closeZ = new ZBoxAnchor3D(new Point3D.Double(bounds.getCenterX(), bounds.getCenterY(), bounds.getMinZ()),
                ColorUtil.mix(ColorUtil.mix(getColor(), Color.lightGray), Color.lightGray), getFocusedColor());
        farZ = new ZBoxAnchor3D(new Point3D.Double(bounds.getCenterX(), bounds.getCenterY(), bounds.getMaxZ()),
                ColorUtil.mix(ColorUtil.mix(getColor(), Color.lightGray), Color.lightGray), getFocusedColor());

        addPoint(this.closeZ);
        addPoint(this.farZ);

        // build the 2D shape ROI (used for 2D interaction)
        shape2DROI = createROI2DShape();
        // important to force infinite Z dimension on base roi
        shape2DROI.setZ(-1);

        shape2DROI.addListener(new ROIListener()
        {
            @Override
            public void roiChanged(ROIEvent event)
            {
                // propagate base ROI change event
                shape2DROIChanged(event);
            }
        });
        getShape2DROIOverlay().addOverlayListener(new OverlayListener()
        {

            @Override
            public void overlayChanged(OverlayEvent event)
            {
                // propagate base ROI overlay change event
                shape2DROIOverlayChanged(event);
            }
        });
    }

    @Override
    public String getDefaultName()
    {
        return "ROI3DZShape";
    }

    protected abstract ROI2DShape createROI2DShape();

    @Override
    protected ROI3DShapePainter createPainter()
    {
        return new ROI3DZShapePainter();
    }

    protected ZShape3D getZShape()
    {
        return (ZShape3D) shape;
    }

    public ROI2DShape getShape2DROI()
    {
        return shape2DROI;
    }

    protected ROI2DShapePainter getShape2DROIOverlay()
    {
        return (ROI2DShapePainter) getShape2DROI().getOverlay();
    }

    @Override
    public void setCreating(boolean value)
    {
        beginUpdate();
        try
        {
            super.setCreating(value);
            shape2DROI.setCreating(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setReadOnly(boolean value)
    {
        beginUpdate();
        try
        {
            super.setReadOnly(value);
            shape2DROI.setReadOnly(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setFocused(boolean value)
    {
        beginUpdate();
        try
        {
            super.setFocused(value);
            shape2DROI.setFocused(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setSelected(boolean value)
    {
        beginUpdate();
        try
        {
            super.setSelected(value);
            shape2DROI.setSelected(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setName(String value)
    {
        beginUpdate();
        try
        {
            super.setName(value);
            shape2DROI.setName(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setT(int value)
    {
        beginUpdate();
        try
        {
            super.setT(value);
            shape2DROI.setT(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setC(int value)
    {
        beginUpdate();
        try
        {
            super.setC(value);
            shape2DROI.setC(value);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @return Returns <code>true</code> if the ROI is empty.
     */
    @Override
    public boolean isEmpty()
    {
        return getBounds3D().isEmpty();
    }

    /**
     * @return the list of position for all control points of the ROI excluding closeZ/farZ points which just contains Z information.
     */
    @Override
    public List<Point3D> getPoints()
    {
        final List<Point3D> result = new ArrayList<Point3D>();

        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                if ((pt != farZ) && (pt != closeZ))
                    result.add(pt.getPosition());
        }

        return result;
    }

    /**
     * @return the list of positions from control points excluding closeZ/farZ points which just contains Z information.<br>
     *         This is the direct internal position reference, don't modify them !
     */
    @Override
    protected List<Point3D> getPointsInternal()
    {
        final List<Point3D> result = new ArrayList<Point3D>();

        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                if ((pt != farZ) && (pt != closeZ))
                    result.add(pt.getPositionInternal());
        }

        return result;
    }

    /**
     * @return the list of 2D position from control points.
     */
    public List<Point2D> getPoints2D()
    {
        final List<Point2D> result = new ArrayList<>();

        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                if ((pt != farZ) && (pt != closeZ))
                    result.add(pt.getPosition().toPoint2D());
        }

        return result;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        // not yet initialized
        if (shape2DROI == null)
            return false;

        return shape2DROI.hasSelectedPoint();
    }

    @Override
    public void unselectAllPoints()
    {
        if (shape2DROI != null)
            shape2DROI.unselectAllPoints();
    }

    @Override
    protected boolean removePoint(IcyCanvas canvas, Anchor3D pt)
    {
        if ((pt == farZ) || (pt == closeZ))
            return false;

        return super.removePoint(canvas, pt);
    }

    @Override
    protected void removeAllPoint()
    {
        final List<Anchor3D> toRemove = new ArrayList<>();

        // farZ and closeZ cannot be removed
        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                if ((pt != farZ) && (pt != closeZ))
                    toRemove.add(pt);

            synchronized (((ROI3DShapePainter) getOverlay()).actorsToRemove)
            {
                // store all points in the "actor to remove" list
                ((ROI3DShapePainter) getOverlay()).actorsToRemove.addAll(toRemove);
            }
            synchronized (((ROI3DShapePainter) getOverlay()).actorsToAdd)
            {
                // and remove them from the "actor to add" list
                ((ROI3DShapePainter) getOverlay()).actorsToAdd.removeAll(toRemove);
            }

            for (Anchor3D pt : toRemove)
            {
                pt.removeOverlayListener(anchor3DOverlayListener);
                pt.removePositionListener(anchor3DPositionListener);
            }

            // remove them from list
            controlPoints.removeAll(toRemove);
        }
    }

    /**
     * Called when shape 2D ROI has changed (used for 2D interaction)
     */
    protected void shape2DROIChanged(ROIEvent event)
    {
        final ROI source = event.getSource();

        switch (event.getType())
        {
            case ROI_CHANGED:
                // rebuild ROI from Shape2DROI
                updateFromShape2DROI();
                // then trigger change (order is important)
                roiChanged(StringUtil.equals(event.getPropertyName(), ROI_CHANGED_ALL));
                break;

            case FOCUS_CHANGED:
                setFocused(source.isFocused());
                break;

            case SELECTION_CHANGED:
                setSelected(source.isSelected());
                break;

            case PROPERTY_CHANGED:
                final String propertyName = event.getPropertyName();

                if ((propertyName == null) || propertyName.equals(PROPERTY_READONLY))
                    setReadOnly(source.isReadOnly());
                if ((propertyName == null) || propertyName.equals(PROPERTY_CREATING))
                    setCreating(source.isCreating());
                break;
        }
    }

    /**
     * Called when shape 2D ROI Overlay has changed (used for 2D interaction)
     */
    protected void shape2DROIOverlayChanged(OverlayEvent event)
    {
        switch (event.getType())
        {
            case PAINTER_CHANGED:
                // forward the event to ROI stack overlay
                getOverlay().painterChanged();
                break;

            case PROPERTY_CHANGED:
                // forward the event to ROI stack overlay
                getOverlay().propertyChanged(event.getPropertyName());
                break;
        }
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        final int cnvZ = canvas.getPositionZ();
        return ((cnvZ == -1) || getZShape().containsZ(cnvZ))
                && isActiveFor(canvas.getPositionT(), canvas.getPositionC());
    }

    @Override
    public double computeNumberOfContourPoints()
    {
        // 3D contour points = first slice points + all slices perimeter + last slice points
        return (shape2DROI.getNumberOfPoints() * 2) + (shape2DROI.getNumberOfContourPoints() * getZShape().getSizeZ());
    }

    @Override
    public double computeNumberOfPoints()
    {
        return shape2DROI.getNumberOfPoints() * getZShape().getSizeZ();
    }

    // default approximated implementation for ROI3DZShape
    @Override
    public double computeSurfaceArea(Sequence sequence) throws UnsupportedOperationException
    {
        final double psx = sequence.getPixelSizeX();
        final double psy = sequence.getPixelSizeY();
        final double psz = sequence.getPixelSizeZ();

        // 3D contour points = first slice points + all slices perimeter + last slice points
        return (shape2DROI.getNumberOfPoints() * 2 * psx * psy)
                + (shape2DROI.getNumberOfContourPoints() * getZShape().getSizeZ() * psz);
    }

    @Override
    public boolean canTranslate()
    {
        return shape2DROI.canTranslate();
    }

    @Override
    public boolean canSetBounds()
    {
        return shape2DROI.canSetBounds();
    }

    @Override
    public void setBounds3D(Rectangle3D bounds)
    {
        if (!canSetBounds())
            return;

        beginUpdate();
        try
        {
            shape2DROI.setBounds2D(bounds.toRectangle2D());
            // important to update control points here (shape will be updated from them)
            closeZ.setZ(bounds.getMinZ());
            farZ.setZ(bounds.getMaxZ());
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @param deltaZ
     *        Translate the ROI of specified delta Z.
     */
    public void translateZ(double deltaZ)
    {
        beginUpdate();
        try
        {
            // important to update control points here (shape will be updated from them)
            closeZ.translate(0d, 0d, deltaZ);
            farZ.translate(0d, 0d, deltaZ);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void translate(double dx, double dy, double dz)
    {
        beginUpdate();
        try
        {
            shape2DROI.translate(dx, dy);
            translateZ(dz);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, boolean inclusive)
    {
        // require full z contains ?
        if ((inclusive && getZShape().containsZ(z, 1d)) || getZShape().containsZ(z))
            return shape2DROI.getBooleanMask(x, y, width, height, inclusive);

        return new boolean[width * height];
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, boolean inclusive)
    {
        // require full z contains ?
        if ((inclusive && getZShape().containsZ(z, 1d)) || getZShape().containsZ(z))
            return shape2DROI.getBooleanMask(inclusive);

        return new BooleanMask2D(new Rectangle(), new boolean[0]);
    }

    /**
     * Update ROIShape2D from 3D control points or shape (need to test if we have changes)
     */
    protected abstract void updateShape2DROI();

    /**
     * Update 3D control points from ROIShape2D (need to test if we have changes)
     */
    protected abstract void updateFromShape2DROI();

    /**
     * Update 3D shape from control points.<br>
     * This method should be overridden by derived classes which<br>
     * have to call the super.updateShape() method at end.
     */
    protected void updateShapeInternal()
    {
        getZShape().setZ(closeZ.getZ());
        getZShape().setSizeZ(farZ.getZ() - closeZ.getZ());

        // cached properties need to be recomputed
        boundsInvalid = true;
    }

    /**
     * Rebuild shape.<br>
     * This method should be overridden by derived classes which<br>
     * have to call the super.updateShape() method at end.
     */
    @Override
    protected void updateShape()
    {
        beginUpdate();
        try
        {
            // update shape from 3D control points
            updateShapeInternal();
            // then rebuild shape2DROI from 3D control points / shape
            updateShape2DROI();
            // call after shape has been updated
            super.updateShape();
        }
        finally
        {
            endUpdate();
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

            closeZ.loadPositionFromXML(XMLUtil.getElement(node, ID_CLOSEZ));
            farZ.loadPositionFromXML(XMLUtil.getElement(node, ID_FARZ));
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

        closeZ.savePositionToXML(XMLUtil.setElement(node, ID_CLOSEZ));
        farZ.savePositionToXML(XMLUtil.setElement(node, ID_FARZ));

        return true;
    }

    public class ROI3DZShapePainter extends ROI3DShapePainter
    {
        public ROI3DZShapePainter()
        {
            super();
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected void rebuildVtkObjects()
        {
            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            final Sequence seq = canvas.getSequence();
            // nothing to update
            if (seq == null)
                return;

            // get bounds
            final double xs = scaling[0];
            final double ys = scaling[1];
            final double zs = scaling[2];

            // update polydata object
            final List<double[]> point3DList = new ArrayList<double[]>();
            final List<int[]> polyList = new ArrayList<int[]>();
            final double[] coords = new double[6];

            // starting position
            final double z0 = getZShape().getMinZ() * zs;
            final double z1 = getZShape().getMaxZ() * zs;
            double xm = 0d;
            double ym = 0d;
            double x0 = 0d;
            double y0 = 0d;
            double x1 = 0d;
            double y1 = 0d;
            int ind;

            // use flat path
            final PathIterator path = getZShape().get2DPathIterator(null, 0.5d);

            // build point data
            while (!path.isDone())
            {
                switch (path.currentSegment(coords))
                {
                    case PathIterator.SEG_MOVETO:
                        x0 = xm = coords[0] * xs;
                        y0 = ym = coords[1] * ys;
                        break;

                    case PathIterator.SEG_LINETO:
                        x1 = coords[0] * xs;
                        y1 = coords[1] * ys;

                        ind = point3DList.size();

                        point3DList.add(new double[] {x0, y0, z0});
                        point3DList.add(new double[] {x1, y1, z0});
                        point3DList.add(new double[] {x0, y0, z1});
                        point3DList.add(new double[] {x1, y1, z1});
                        polyList.add(new int[] {1 + ind, 2 + ind, 0 + ind});
                        polyList.add(new int[] {3 + ind, 2 + ind, 1 + ind});

                        x0 = x1;
                        y0 = y1;
                        break;

                    case PathIterator.SEG_CLOSE:
                        x1 = xm;
                        y1 = ym;

                        ind = point3DList.size();

                        point3DList.add(new double[] {x0, y0, z0});
                        point3DList.add(new double[] {x1, y1, z0});
                        point3DList.add(new double[] {x0, y0, z1});
                        point3DList.add(new double[] {x1, y1, z1});
                        polyList.add(new int[] {1 + ind, 2 + ind, 0 + ind});
                        polyList.add(new int[] {3 + ind, 2 + ind, 1 + ind});

                        x0 = x1;
                        y0 = y1;
                        break;
                }

                path.next();
            }

            // convert to array
            final double[][] vertices = new double[point3DList.size()][3];
            final int[][] indexes = new int[polyList.size()][3];

            ind = 0;
            for (double[] pt3D : point3DList)
                vertices[ind++] = pt3D;

            ind = 0;
            for (int[] poly : polyList)
                indexes[ind++] = poly;

            final vtkCellArray previousCells = vCells;
            final vtkPoints previousPoints = vPoints;
            vCells = VtkUtil.getCells(polyList.size(), VtkUtil.prepareCells(indexes));
            vPoints = VtkUtil.getPoints(vertices);

            final Rectangle3D bounds = getBounds3D();

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                // update outline data
                VtkUtil.setOutlineBounds(outline, bounds.getMinX() * xs, bounds.getMaxX() * xs, bounds.getMinY() * ys,
                        bounds.getMaxY() * ys, z0, z1, canvas);
                outlineMapper.Update();
                // update polygon data from cell and points
                polyData.SetPolys(vCells);
                polyData.SetPoints(vPoints);
                polyMapper.Update();

                // release previous allocated VTK objects
                if (previousCells != null)
                    previousCells.Delete();
                if (previousPoints != null)
                    previousPoints.Delete();
            }
            finally
            {
                vtkPanel.unlock();
            }

            // update color and others properties
            updateVtkDisplayProperties();
        }

        @Override
        public void run()
        {
            rebuildVtkObjects();
        }

        @Override
        public void setReadOnly(boolean value)
        {
            super.setReadOnly(readOnly);
            getShape2DROIOverlay().setReadOnly(value);
        }

        @Override
        public void setColor(Color value)
        {
            super.setColor(value);
            
            // so we can distinguish them
            closeZ.setColor(ColorUtil.mix(ColorUtil.mix(value, Color.lightGray), Color.lightGray));
            farZ.setColor(ColorUtil.mix(ColorUtil.mix(value, Color.lightGray), Color.lightGray));
            
            getShape2DROIOverlay().setColor(value);
        }

        @Override
        public void setOpacity(float value)
        {
            super.setOpacity(value);
            getShape2DROIOverlay().setOpacity(value);
        }

        @Override
        public void setStroke(double value)
        {
            super.setStroke(value);
            getShape2DROIOverlay().setStroke(value);
        }

        @Override
        public void setShowName(boolean value)
        {
            super.setShowName(value);
            getShape2DROIOverlay().setShowName(value);
        }

        @Override
        public void setPriority(OverlayPriority value)
        {
            super.setPriority(priority);
            getShape2DROIOverlay().setPriority(value);
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
                super.paint(g, sequence, canvas);
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().keyPressed(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.keyPressed(e, imagePoint, canvas);
        }

        @Override
        public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().keyReleased(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.keyReleased(e, imagePoint, canvas);
        }

        @Override
        public void mouseEntered(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseEntered(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseEntered(e, imagePoint, canvas);
        }

        @Override
        public void mouseExited(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseExited(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseExited(e, imagePoint, canvas);
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseMove(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseMove(e, imagePoint, canvas);
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseDrag(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseDrag(e, imagePoint, canvas);
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mousePressed(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mousePressed(e, imagePoint, canvas);
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseReleased(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseReleased(e, imagePoint, canvas);
        }

        @Override
        public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseClick(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseClick(e, imagePoint, canvas);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
            {
                // test isActive here as shape2DROI doesn't test for Z
                if (isActiveFor(canvas))
                    getShape2DROIOverlay().mouseWheelMoved(e, imagePoint, canvas);
            }
            // use default implementation
            else
                super.mouseWheelMoved(e, imagePoint, canvas);
        }

        @Override
        public void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            // 2D canvas ? --> use shape2DROI implementation
            if (canvas instanceof IcyCanvas2D)
                getShape2DROIOverlay().drawROI(g, sequence, canvas);
            else
                super.drawROI(g, sequence, canvas);
        }
    }
}
