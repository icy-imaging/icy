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

import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas2D;
import org.bioimageanalysis.icy.gui.canvas.VtkCanvas;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.gui.render.IcyVtkPanel;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.OverlayEvent;
import org.bioimageanalysis.icy.model.overlay.OverlayEvent.OverlayEventType;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIEvent;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.w3c.dom.Node;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkSphereSource;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * ROI 2D Point class.<br>
 * Define a single point ROI<br>
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI2DPoint extends ROI2DShape {
    public class ROI2DPointPainter extends ROI2DShapePainter {
        vtkSphereSource vtkSource;

        @Override
        protected boolean isSmall(final Rectangle2D bounds, final Graphics2D g, final IcyCanvas canvas) {
            return !isSelected();
        }

        @Override
        protected boolean isTiny(final Rectangle2D bounds, final Graphics2D g, final IcyCanvas canvas) {
            return !isSelected();
        }

        @Override
        public void drawROI(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
            if (canvas instanceof IcyCanvas2D) {
                final Graphics2D g2 = (Graphics2D) g.create();

                if (isSelected() && !isReadOnly()) {
                    // draw control point if selected
                    synchronized (controlPoints) {
                        for (final Anchor2D pt : controlPoints)
                            pt.paint(g2, sequence, canvas);
                    }
                }
                else {
                    final Point2D pos = getPoint();
                    final double ray = getAdjustedStroke(canvas);
                    final Ellipse2D ellipse = new Ellipse2D.Double(pos.getX() - ray, pos.getY() - ray, ray * 2, ray * 2);

                    // draw shape
                    g2.setColor(getDisplayColor());
                    g2.fill(ellipse);
                }

                g2.dispose();
            }
            else
                // just use parent method
                super.drawROI(g, sequence, canvas);
        }

        @Override
        protected void initVtkObjects() {
            super.initVtkObjects();

            // init 3D painters stuff
            vtkSource = new vtkSphereSource();
            vtkSource.SetRadius(getStroke());
            vtkSource.SetThetaResolution(12);
            vtkSource.SetPhiResolution(12);

            // delete previously created objects that we will recreate
            if (actor != null)
                actor.Delete();
            if (polyMapper != null)
                polyMapper.Delete();

            polyMapper = new vtkPolyDataMapper();
            polyMapper.SetInputConnection((vtkSource).GetOutputPort());

            actor = new vtkActor();
            actor.SetMapper(polyMapper);

            // initialize color
            final Color col = getColor();
            actor.GetProperty().SetColor(col.getRed() / 255d, col.getGreen() / 255d, col.getBlue() / 255d);
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        @Override
        protected void rebuildVtkObjects() {
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

            final Point2D pos = getPoint();
            double curZ = getZ();

            // all slices ?
            if (curZ == -1)
                // set object at middle of the volume
                curZ = seq.getSizeZ() / 2d;

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try {
                // need to handle scaling on radius and position to keep a "round" sphere (else we obtain ellipsoid)
                vtkSource.SetRadius(getStroke() * scaling[0]);
                vtkSource.SetCenter(pos.getX() * scaling[0], pos.getY() * scaling[1], (curZ + 0.5d) * scaling[2]);
                polyMapper.Update();

                // vtkSource.SetRadius(getStroke());
                // vtkSource.SetCenter(pos.getX(), pos.getY(), curZ);
                // polyMapper.Update();
                // actor.SetScale(scaling);
            }
            finally {
                vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }

        @Override
        protected void updateVtkDisplayProperties() {
            if (actor == null)
                return;

            final VtkCanvas cnv = canvas3d.get();
            final Color col = getDisplayColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;
            // final float opacity = getOpacity();

            final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

            // we need to lock canvas as actor can be accessed during rendering
            if (vtkPanel != null)
                vtkPanel.lock();
            try {
                actor.GetProperty().SetColor(r, g, b);
            }
            finally {
                if (vtkPanel != null)
                    vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }

        @Override
        protected boolean updateFocus(final InputEvent e, final Point5D imagePoint, final IcyCanvas canvas) {
            // specific VTK canvas processing
            if (canvas instanceof VtkCanvas) {
                // mouse is over the ROI actor ? --> focus the ROI
                final boolean focus = (actor != null) && (actor == ((VtkCanvas) canvas).getPickedObject());

                setFocused(focus);

                return focus;
            }

            return super.updateFocus(e, imagePoint, canvas);
        }
    }

    public static final String ID_POSITION = "position";

    private final Anchor2D position;

    public ROI2DPoint(final Point2D position) {
        super(new Line2D.Double());

        this.position = createAnchor(position);
        this.position.setSelected(true);
        addPoint(this.position);

        // set icon (default name is defined by getDefaultName())
        setIcon(SVGResource.ROI_POINT);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPoint(final Point5D pt) {
        this(pt.toPoint2D());
        // getOverlay().setMousePos(pt);
    }

    public ROI2DPoint(final double x, final double y) {
        this(new Point2D.Double(x, y));
    }

    public ROI2DPoint() {
        this(new Point2D.Double());
    }

    @Override
    public String getDefaultName() {
        return "Point2D";
    }

    @Override
    protected ROI2DShapePainter createPainter() {
        return new ROI2DPointPainter();
    }

    public Line2D getLine() {
        return (Line2D) shape;
    }

    public Point2D getPoint() {
        return position.getPosition();
    }

    /**
     * Called when anchor overlay changed
     */
    @Override
    public void controlPointOverlayChanged(final OverlayEvent event) {
        // we only mind about painter change from anchor...
        if (event.getType() == OverlayEventType.PAINTER_CHANGED) {
            // here we want to have ROI focused when point is selected (special case for ROIPoint)
            // Stephane: not a good idea if we selected several ROI points as setFocused is *exclusive*
            // if (hasSelectedPoint())
            // setFocused(true);

            // anchor changed --> ROI painter changed
            getOverlay().painterChanged();
        }
    }

    /**
     * @return true if specified point coordinates overlap the ROI edge.
     */
    @Override
    public boolean isOverEdge(final IcyCanvas canvas, final double x, final double y) {
        // selected ? --> use control point isOver(..)
        if (isSelected())
            return position.isOver(canvas, x, y);

        return super.isOverEdge(canvas, x, y);
    }

    @Override
    public boolean contains(final double x, final double y) {
        return false;
    }

    @Override
    public boolean contains(final Point2D p) {
        return false;
    }

    @Override
    public boolean contains(final double x, final double y, final double w, final double h) {
        return false;
    }

    @Override
    public boolean contains(final Rectangle2D r) {
        return false;
    }

    @Override
    public boolean contains(final ROI roi) {
        return false;
    }

    @Override
    public boolean intersects(final ROI r) throws InterruptedException {
        // special case of ROI2DPoint
        if (r instanceof ROI2DPoint)
            return onSamePos(((ROI2DPoint) r), false) && ((ROI2DPoint) r).getPoint().equals(getPoint());

        return super.intersects(r);
    }

    /**
     * roi changed
     */
    @Override
    public void onChanged(final CollapsibleEvent object) {
        final ROIEvent event = (ROIEvent) object;

        // do here global process on ROI change
        switch (event.getType()) {
            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                // stroke changed --> rebuild vtk object
                if (StringUtil.equals(property, PROPERTY_STROKE))
                    ((ROI2DShapePainter) getOverlay()).needRebuild = true;
                break;

            case SELECTION_CHANGED:
                // always select the control point when ROI was just selected
                if (isSelected())
                    position.setSelected(true);
                break;

            default:
                break;
        }

        super.onChanged(object);
    }

    @Override
    protected void updateShape() {
        final Point2D pt = getPoint();
        final double x = pt.getX();
        final double y = pt.getY();

        getLine().setLine(x, y, x, y);

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean canAddPoint() {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    protected boolean removePoint(final IcyCanvas canvas, final Anchor2D pt) {
        if (canvas != null) {
            // remove point on this ROI remove the ROI from current sequence
            canvas.getSequence().removeROI(this);
            return true;
        }

        return false;
    }

    @Override
    public double computeNumberOfContourPoints() {
        return 0d;
    }

    @Override
    public double computeNumberOfPoints() {
        return 0d;
    }

    @Override
    public boolean loadFromXML(final Node node) {
        beginUpdate();
        try {
            if (!super.loadFromXML(node))
                return false;

            position.loadPositionFromXML(XMLUtil.getElement(node, ID_POSITION));
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

        position.savePositionToXML(XMLUtil.setElement(node, ID_POSITION));

        return true;
    }
}
