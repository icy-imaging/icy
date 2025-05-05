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

import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;
import org.bioimageanalysis.icy.model.overlay.anchor.RectAnchor2D;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * Base class for rectangular shape ROI.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class ROI2DRectShape extends ROI2DShape {
    protected class ROI2DRectAnchor2D extends RectAnchor2D {
        public ROI2DRectAnchor2D(final Point2D position, final Color color, final Color selectedColor) {
            super(position, color, selectedColor);
        }

        @Override
        protected Anchor2D getOppositePoint() {
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

    protected final Anchor2D topLeft;
    protected final Anchor2D topRight;
    protected final Anchor2D bottomLeft;
    protected final Anchor2D bottomRight;

    protected boolean internalPositionSet;

    public ROI2DRectShape(final RectangularShape shape, final Point2D topLeft, final Point2D bottomRight) {
        super(shape);

        this.topLeft = createAnchor(topLeft);
        this.topRight = createAnchor(new Point2D.Double(bottomRight.getX(), topLeft.getY()));
        this.bottomLeft = createAnchor(new Point2D.Double(topLeft.getX(), bottomRight.getY()));
        this.bottomRight = createAnchor(bottomRight);
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
    protected Anchor2D createAnchor(final Point2D pos) {
        return new ROI2DRectAnchor2D(pos, getColor(), getFocusedColor());
    }

    protected RectangularShape getRectangularShape() {
        return (RectangularShape) shape;
    }

    @Override
    public boolean canSetBounds() {
        return true;
    }

    @Override
    public void setBounds2D(final Rectangle2D bounds) {
        beginUpdate();
        try {
            // set anchors (only 2 significants anchors need to be adjusted)
            topLeft.setPosition(bounds.getMinX(), bounds.getMinY());
            bottomRight.setPosition(bounds.getMaxX(), bounds.getMaxY());
        }
        finally {
            endUpdate();
        }
    }

    @Override
    protected void updateShape() {
        getRectangularShape().setFrameFromDiagonal(topLeft.getPosition(), bottomRight.getPosition());

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
    public void controlPointPositionChanged(final Anchor2D source) {
        // we are modifying internally the position --> exit
        if (internalPositionSet)
            return;

        internalPositionSet = true;
        try {
            // adjust dependents anchors
            if (source == topLeft) {
                bottomLeft.setX(topLeft.getX());
                topRight.setY(topLeft.getY());
            }
            else if (source == topRight) {
                bottomRight.setX(topRight.getX());
                topLeft.setY(topRight.getY());
            }
            else if (source == bottomLeft) {
                topLeft.setX(bottomLeft.getX());
                bottomRight.setY(bottomLeft.getY());
            }
            else if (source == bottomRight) {
                topRight.setX(bottomRight.getX());
                bottomLeft.setY(bottomRight.getY());
            }
        }
        finally {
            internalPositionSet = false;
        }

        super.controlPointPositionChanged(source);
    }

    @Override
    public void translate(final double dx, final double dy) {
        beginUpdate();
        try {
            // translate (only 2 significants anchors need to be adjusted)
            topLeft.translate(dx, dy);
            bottomRight.translate(dx, dy);
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

        topLeft.savePositionToXML(XMLUtil.setElement(node, ID_TOPLEFT));
        bottomRight.savePositionToXML(XMLUtil.setElement(node, ID_BOTTOMRIGHT));

        return true;
    }
}
