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
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.roi.ROI;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI2DRectangle extends ROI2DRectShape {
    public ROI2DRectangle(final Point2D topLeft, final Point2D bottomRight) {
        super(new Rectangle2D.Double(), topLeft, bottomRight);

        // set icon (default name is defined by getDefaultName()) 
        setIcon(SVGIcon.RECTANGLE);
    }

    public ROI2DRectangle(final double xmin, final double ymin, final double xmax, final double ymax) {
        this(new Point2D.Double(xmin, ymin), new Point2D.Double(xmax, ymax));
    }

    public ROI2DRectangle(final Rectangle2D rectangle) {
        this(new Point2D.Double(rectangle.getMinX(), rectangle.getMinY()), new Point2D.Double(rectangle.getMaxX(),
                rectangle.getMaxY()));
    }

    public ROI2DRectangle(final Point2D pt) {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DRectangle(final Point5D pt) {
        this(pt.toPoint2D());
    }

    public ROI2DRectangle() {
        this(new Point2D.Double(), new Point2D.Double());
    }

    @Override
    public String getDefaultName() {
        return "Rectangle2D";
    }

    public Rectangle2D getRectangle() {
        return (Rectangle2D) shape;
    }

    public void setRectangle(final Rectangle2D rectangle) {
        setBounds2D(rectangle);
    }

    @Override
    public boolean contains(final ROI roi) throws InterruptedException {
        // special case of ROI2DPoint
        if (roi instanceof ROI2DPoint)
            return onSamePos(((ROI2DPoint) roi), true) && contains(((ROI2DPoint) roi).getPoint());
        // special case of ROI2DLine
        if (roi instanceof ROI2DLine)
            return onSamePos(((ROI2DLine) roi), true) && contains(((ROI2DLine) roi).getBounds2D());
        // special case of ROI2DRectangle
        if (roi instanceof ROI2DRectangle)
            return onSamePos(((ROI2DRectangle) roi), true) && contains(((ROI2DRectangle) roi).getRectangle());

        return super.contains(roi);
    }

    @Override
    public boolean intersects(final ROI roi) throws InterruptedException {
        // special case of ROI2DPoint
        if (roi instanceof ROI2DPoint)
            return onSamePos(((ROI2DPoint) roi), false) && contains(((ROI2DPoint) roi).getPoint());
        // special case of ROI2DLine
        if (roi instanceof ROI2DLine)
            return onSamePos(((ROI2DLine) roi), false) && ((ROI2DLine) roi).getLine().intersects(getRectangle());
        // special case of ROI2DRectangle
        if (roi instanceof ROI2DRectangle)
            return onSamePos(((ROI2DRectangle) roi), false)
                    && ((ROI2DRectangle) roi).getRectangle().intersects(getRectangle());

        return super.intersects(roi);
    }

    @Override
    public double computeNumberOfPoints() {
        final Rectangle2D r = getRectangle();
        return r.getWidth() * r.getHeight();
    }
}
