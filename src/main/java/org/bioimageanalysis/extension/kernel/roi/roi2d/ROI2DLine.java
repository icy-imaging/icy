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

import org.bioimageanalysis.icy.common.geom.point.Point2DUtil;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;
import org.bioimageanalysis.icy.model.overlay.anchor.LineAnchor2D;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * ROI 2D Line.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI2DLine extends ROI2DShape {
    protected class ROI2DLineAnchor2D extends LineAnchor2D {
        public ROI2DLineAnchor2D(final Point2D position) {
            super(position, getOverlay().getColor(), getOverlay().getFocusedColor());
        }

        @Override
        protected Anchor2D getPreviousPoint() {
            if (this == pt1)
                return pt2;
            return pt1;
        }
    }

    public class ROI2DLinePainter extends ROI2DShapePainter {
        @Override
        protected boolean isTiny(final Rectangle2D bounds, final Graphics2D g, final IcyCanvas canvas) {
            if (isSelected())
                return false;

            return super.isTiny(bounds, g, canvas);
        }
    }

    public static final String ID_PT1 = "pt1";
    public static final String ID_PT2 = "pt2";

    protected final Anchor2D pt1;
    protected final Anchor2D pt2;

    public ROI2DLine(final Point2D pt1, final Point2D pt2) {
        super(new Line2D.Double());

        this.pt1 = createAnchor(pt1);
        this.pt2 = createAnchor(pt2);
        // keep pt2 selected to size the line for "interactive mode"
        this.pt2.setSelected(true);

        addPoint(this.pt1);
        addPoint(this.pt2);

        // set icon (default name is defined by getDefaultName()) 
        setIcon(SVGIcon.LINE);
    }

    public ROI2DLine(final Line2D line) {
        this(line.getP1(), line.getP2());
    }

    public ROI2DLine(final Point2D pt) {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DLine(final Point5D pt) {
        this(pt.toPoint2D());
        // getOverlay().setMousePos(pt);
    }

    public ROI2DLine(final double x1, final double y1, final double x2, final double y2) {
        this(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2));
    }

    public ROI2DLine() {
        this(new Point2D.Double(), new Point2D.Double());
    }

    @Override
    public String getDefaultName() {
        return "Line2D";
    }

    @Override
    protected ROI2DShapePainter createPainter() {
        return new ROI2DLinePainter();
    }

    @Override
    protected Anchor2D createAnchor(final Point2D pos) {
        return new ROI2DLineAnchor2D(pos);
    }

    public Line2D getLine() {
        return (Line2D) shape;
    }

    @Override
    public boolean canSetBounds() {
        return true;
    }

    @Override
    public void setBounds2D(final Rectangle2D bounds) {
        beginUpdate();
        try {
            pt1.setPosition(bounds.getMinX(), bounds.getMinY());
            pt2.setPosition(bounds.getMaxX(), bounds.getMaxY());
        }
        finally {
            endUpdate();
        }
    }

    public void setLine(final Line2D line) {
        beginUpdate();
        try {
            pt1.setPosition(line.getP1());
            pt2.setPosition(line.getP2());
        }
        finally {
            endUpdate();
        }
    }

    @Override
    protected void updateShape() {
        getLine().setLine(pt1.getPosition(), pt2.getPosition());

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean canAddPoint() {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    public boolean canRemovePoint() {
        // this ROI doesn't support point remove
        return false;
    }

    @Override
    protected boolean removePoint(final IcyCanvas canvas, final Anchor2D pt) {
        // this ROI doesn't support point remove
        return false;
    }

    @Override
    protected double getTotalDistance(final List<Point2D> points, final double factorX, final double factorY) {
        // for ROI2DLine the total length don't need last point connection
        return Point2DUtil.getTotalDistance(points, factorX, factorY, false);
    }

    @Override
    public double computeNumberOfPoints() {
        return 0d;
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
        // special case of ROI2DLine
        if (r instanceof ROI2DLine)
            return onSamePos(((ROI2DLine) r), false) && ((ROI2DLine) r).getLine().intersectsLine(getLine());
        // special case of ROI2DRectangle
        if (r instanceof ROI2DRectangle)
            return onSamePos(((ROI2DRectangle) r), false)
                    && ((ROI2DRectangle) r).getRectangle().intersectsLine(getLine());

        return super.intersects(r);
    }

    @Override
    public boolean loadFromXML(final Node node) {
        beginUpdate();
        try {
            if (!super.loadFromXML(node))
                return false;

            pt1.loadPositionFromXML(XMLUtil.getElement(node, ID_PT1));
            pt2.loadPositionFromXML(XMLUtil.getElement(node, ID_PT2));
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

        pt1.savePositionToXML(XMLUtil.setElement(node, ID_PT1));
        pt2.savePositionToXML(XMLUtil.setElement(node, ID_PT2));

        return true;
    }
}
