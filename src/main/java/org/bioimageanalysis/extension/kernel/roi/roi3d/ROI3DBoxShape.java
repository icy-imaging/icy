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

import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DRectShape;
import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DShape;
import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle3D;
import org.bioimageanalysis.icy.common.geom.shape.BoxShape3D;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor3D;
import org.bioimageanalysis.icy.model.overlay.anchor.RectAnchor3D;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class ROI3DBoxShape extends ROI3DZShape {
    protected class ROI3DBoxAnchor3D extends RectAnchor3D {
        public ROI3DBoxAnchor3D(final Point3D position, final Color color, final Color selectedColor) {
            super(position, color, selectedColor);

            setFixedZ(true);
        }

        @Override
        protected Anchor3D getOppositePoint() {
            if (this == topLeft)
                return bottomRight;
            if (this == topRight)
                return bottomLeft;
            if (this == bottomLeft)
                return topRight;

            return topLeft;
        }
    }

    public static final String ID_TOPLEFT = "top_left";
    public static final String ID_BOTTOMRIGHT = "bottom_right";

    protected final ROI3DBoxAnchor3D topLeft;
    protected final ROI3DBoxAnchor3D topRight;
    protected final ROI3DBoxAnchor3D bottomLeft;
    protected final ROI3DBoxAnchor3D bottomRight;

    protected boolean internalPositionSet;

    public ROI3DBoxShape(final BoxShape3D shape) {
        super(shape);

        final Rectangle3D bounds = shape.getBounds();

        this.topLeft = (ROI3DBoxAnchor3D) createAnchor(new Point3D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getCenterZ()));
        this.topRight = (ROI3DBoxAnchor3D) createAnchor(new Point3D.Double(bounds.getMaxX(), bounds.getMinY(), bounds.getCenterZ()));
        this.bottomLeft = (ROI3DBoxAnchor3D) createAnchor(new Point3D.Double(bounds.getMinX(), bounds.getMaxY(), bounds.getCenterZ()));
        this.bottomRight = (ROI3DBoxAnchor3D) createAnchor(new Point3D.Double(bounds.getMaxX(), bounds.getMaxY(), bounds.getCenterZ()));
        // select the bottom right point by default for interactive mode
        this.bottomRight.setSelected(true);

        internalPositionSet = false;

        // order is important as we compute distance from connected points
        addPoint(this.topLeft);
        addPoint(this.topRight);
        addPoint(this.bottomRight);
        addPoint(this.bottomLeft);
    }

    @Override
    protected Anchor3D createAnchor(final Point3D pos) {
        return new ROI3DBoxAnchor3D(pos, getColor(), getFocusedColor());
    }

    protected BoxShape3D getBoxShape() {
        return (BoxShape3D) shape;
    }

    @Override
    public boolean canSetBounds() {
        return true;
    }

    @Override
    public void setBounds3D(final Rectangle3D bounds) {
        beginUpdate();
        try {
            // set anchors (only 2 significants anchors need to be adjusted)
            topLeft.setPosition(bounds.getMinX(), bounds.getMinY(), bounds.getCenterZ());
            bottomRight.setPosition(bounds.getMaxX(), bounds.getMaxY(), bounds.getCenterZ());
            closeZ.setPosition(bounds.getCenterX(), bounds.getCenterY(), bounds.getMinZ());
            farZ.setPosition(bounds.getCenterX(), bounds.getCenterY(), bounds.getMaxZ());
        }
        finally {
            endUpdate();
        }
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
    protected boolean removePoint(final IcyCanvas canvas, final Anchor3D pt) {
        // this ROI doesn't support point remove
        return false;
    }

    @Override
    public void controlPointPositionChanged(final Anchor3D source) {
        // we are modifying internally the position --> exit
        if (internalPositionSet)
            return;

        internalPositionSet = true;
        beginUpdate();
        try {
            // adjust dependents anchors
            if (source == topLeft) {
                bottomLeft.setX(topLeft.getX());
                bottomLeft.setZ(topLeft.getZ());
                topRight.setY(topLeft.getY());
                topRight.setZ(topLeft.getZ());
            }
            else if (source == topRight) {
                bottomRight.setX(topRight.getX());
                bottomRight.setZ(topRight.getZ());
                topLeft.setY(topRight.getY());
                topLeft.setZ(topRight.getZ());
            }
            else if (source == bottomLeft) {
                topLeft.setX(bottomLeft.getX());
                topLeft.setZ(bottomLeft.getZ());
                bottomRight.setY(bottomLeft.getY());
                bottomRight.setZ(bottomLeft.getZ());
            }
            else if (source == bottomRight) {
                topRight.setX(bottomRight.getX());
                topRight.setZ(bottomRight.getZ());
                bottomLeft.setY(bottomRight.getY());
                bottomLeft.setZ(bottomRight.getZ());
            }
        }
        finally {
            endUpdate();
            internalPositionSet = false;
        }

        super.controlPointPositionChanged(source);
    }

    @Override
    public void translate(final double dx, final double dy, final double dz) {
        beginUpdate();
        try {
            // translate (only 2 significants anchors need to be adjusted)
            topLeft.translate(dx, dy, dz);
            bottomRight.translate(dx, dy, dz);

            super.translate(dx, dy, dz);
        }
        finally {
            endUpdate();
        }
    }

    @Override
    public boolean loadFromXML(final Node node) {
        beginUpdate();
        try {
            if (!super.loadFromXML(node))
                return false;

            // only 2 significants anchors need to be loaded
            topLeft.loadPositionFromXML(XMLUtil.getElement(node, ID_TOPLEFT));
            bottomRight.loadPositionFromXML(XMLUtil.getElement(node, ID_BOTTOMRIGHT));
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

        // only 2 significants anchors need to be saved
        topLeft.savePositionToXML(XMLUtil.setElement(node, ID_TOPLEFT));
        bottomRight.savePositionToXML(XMLUtil.setElement(node, ID_BOTTOMRIGHT));

        return true;
    }

    @Override
    protected void updateShape2DROI() {
        final ROI2DShape r = getShape2DROI();

        // that should be the case
        if (r instanceof final ROI2DRectShape roi) {
            roi.setBounds2D(getBounds3D().toRectangle2D());
        }
    }

    @Override
    protected void updateFromShape2DROI() {
        // not yet initialized ? --> return
        if ((topLeft == null) || (bottomRight == null))
            return;

        final ROI2DShape r = getShape2DROI();

        // that should be the case
        if (r instanceof ROI2DRectShape) {
            final Rectangle2D rect = r.getBounds2D();

            beginUpdate();
            try {
                // update 3D control points positions from 2D ROI
                topLeft.setPosition(rect.getMinX(), rect.getMinY(), topLeft.getZ());
                bottomRight.setPosition(rect.getMaxX(), rect.getMaxY(), bottomRight.getZ());
            }
            finally {
                endUpdate();
            }
        }
    }

    @Override
    protected void updateShapeInternal() {
        // not yet initialized ? --> return
        if ((topLeft == null) || (bottomRight == null))
            return;

        // get bounds points
        final Point3D pt1 = topLeft.getPosition();
        final Point3D pt2 = bottomRight.getPosition();

        // fix Z position
        pt1.setZ(closeZ.getZ());
        pt2.setZ(farZ.getZ());

        // set shape
        getBoxShape().setFrameFromDiagonal(pt1, pt2);
        // cached properties need to be recomputed
        boundsInvalid = true;

        // update Z control points XY positions
        final Rectangle3D bounds = getBounds3D();

        closeZ.setX(bounds.getCenterX());
        closeZ.setY(bounds.getCenterY());
        farZ.setX(bounds.getCenterX());
        farZ.setY(bounds.getCenterY());

        // update Z position of others points
        topLeft.setZ(bounds.getCenterZ());
        bottomRight.setZ(bounds.getCenterZ());
    }
}
