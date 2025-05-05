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

package org.bioimageanalysis.icy.common.geom.rectangle;

import org.bioimageanalysis.icy.common.geom.GeomUtil;
import org.bioimageanalysis.icy.common.geom.shape.ShapeUtil;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Rectangle2DUtil {
    /**
     * Returns the shortest line segment which result from the intersection of the given rectangle <b>bounds</b> and
     * line. It returns <code>null</code> if the line segment does not intersects the Rectangle <b>content</b>.
     */
    public static Line2D getIntersectionLine(final Rectangle2D rectangle, final Line2D line) {
        if (rectangle.intersectsLine(line)) {
            final List<Point2D> result = new ArrayList<>();

            final Point2D topLeft = new Point2D.Double(rectangle.getMinX(), rectangle.getMinY());
            final Point2D topRight = new Point2D.Double(rectangle.getMaxX(), rectangle.getMinY());
            final Point2D bottomRight = new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY());
            final Point2D bottomLeft = new Point2D.Double(rectangle.getMinX(), rectangle.getMaxY());
            Point2D intersection;

            intersection = GeomUtil.getIntersection(new Line2D.Double(topLeft, topRight), line, true, false);
            if (intersection != null)
                result.add(intersection);
            intersection = GeomUtil.getIntersection(new Line2D.Double(topRight, bottomRight), line, true, false);
            if (intersection != null)
                result.add(intersection);
            intersection = GeomUtil.getIntersection(new Line2D.Double(bottomRight, bottomLeft), line, true, false);
            if (intersection != null)
                result.add(intersection);
            intersection = GeomUtil.getIntersection(new Line2D.Double(bottomLeft, topLeft), line, true, false);
            if (intersection != null)
                result.add(intersection);

            if (result.size() >= 2)
                return new Line2D.Double(result.get(0), result.get(1));
        }

        return null;
    }

    /**
     * Returns a scaled form of the specified {@link Rectangle2D} by specified factor.
     *
     * @param rect
     *        the {@link Rectangle2D} to scale
     * @param factor
     *        the scale factor
     * @param centered
     *        if true then scaling is centered (rect location is modified)
     * @param scalePosition
     *        if true then position is also rescaled (rect location is modified)
     */
    public static Rectangle2D getScaledRectangle(final Rectangle2D rect, final double factor, final boolean centered, final boolean scalePosition) {
        final Rectangle2D result = new Rectangle2D.Double();

        result.setFrame(rect);
        ShapeUtil.scale(result, factor, centered, scalePosition);

        return result;
    }

    /**
     * Returns a scaled form of the specified {@link Rectangle2D} by specified factor.
     *
     * @param rect
     *        the {@link Rectangle2D} to scale
     * @param factor
     *        the scale factor
     * @param centered
     *        if true then scaling is centered (rect location is modified)
     */
    public static Rectangle2D getScaledRectangle(final Rectangle2D rect, final double factor, final boolean centered) {
        return getScaledRectangle(rect, factor, centered, false);
    }
}
