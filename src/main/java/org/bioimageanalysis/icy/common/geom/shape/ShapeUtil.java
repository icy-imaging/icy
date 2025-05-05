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

package org.bioimageanalysis.icy.common.geom.shape;

import org.bioimageanalysis.icy.common.geom.areax.AreaX;
import org.bioimageanalysis.icy.gui.GraphicsUtil;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;
import org.bioimageanalysis.icy.model.overlay.anchor.PathAnchor2D;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ShapeUtil {
    public interface PathConsumer {
        /**
         * Consume the specified path.<br>
         * Return false to interrupt consumption.
         */
        boolean consumePath(Path2D path, boolean closed);
    }

    public interface ShapeConsumer {
        /**
         * Consume the specified Shape.<br>
         * Return false to interrupt consumption.
         */
        boolean consume(Shape shape);
    }

    public enum BooleanOperator {
        OR, AND, XOR
    }

    /**
     * Use the {@link Graphics} clip area and {@link Shape} bounds informations to determine if
     * the specified {@link Shape} is visible in the specified Graphics object.
     */
    public static boolean isVisible(final Graphics g, final Shape shape) {
        if (shape == null)
            return false;

        return GraphicsUtil.isVisible(g, shape.getBounds2D());
    }

    /**
     * Returns <code>true</code> if the specified Shape define a closed Shape (AreaX).<br>
     * Returns <code>false</code> if the specified Shape define a open Shape (Path).<br>
     */
    public static boolean isClosed(final Shape shape) {
        final PathIterator path = shape.getPathIterator(null);
        final double[] crd = new double[6];

        while (!path.isDone()) {
            if (path.currentSegment(crd) == PathIterator.SEG_CLOSE)
                return true;

            path.next();
        }

        return false;
    }

    /**
     * Merge the specified list of {@link Shape} with the given {@link BooleanOperator}.<br>
     *
     * @param shapes
     *        Shapes we want to merge.
     * @param operator
     *        {@link BooleanOperator} to apply.
     * @return {@link AreaX} shape representing the result of the merge operation.
     */
    public static Shape merge(final List<Shape> shapes, final BooleanOperator operator) {
        Shape result = new AreaX();

        // merge shapes
        for (final Shape shape : shapes) {
            result = switch (operator) {
                case OR -> union(result, shape);
                case AND -> intersect(result, shape);
                case XOR -> exclusiveUnion(result, shape);
            };
        }

        return result;
    }

    /**
     * Process union between the 2 shapes and return result in a new Shape.
     */
    public static Shape union(final Shape shape1, final Shape shape2) {
        // first compute closed area union
        final AreaX area = new AreaX(getClosedPath(shape1));
        area.add(new AreaX(getClosedPath(shape2)));
        // then compute open path (polyline) union
        final Path2D result = new Path2D.Double(getOpenPath(shape1));
        result.append(getOpenPath(shape2), false);
        // then append result
        result.append(area, false);

        return result;
    }

    /**
     * Intersects 2 shapes and return result in an {@link AreaX} type shape.<br>
     * If one of the specified Shape is not an AreaX (do not contains any pixel) then an empty AreaX is returned.
     */
    public static AreaX intersect(final Shape shape1, final Shape shape2) {
        // trivial optimization
        if (!isClosed(shape1) || !isClosed(shape2))
            return new AreaX();

        final AreaX result = new AreaX(getClosedPath(shape1));

        result.intersect(new AreaX(getClosedPath(shape2)));

        return result;
    }

    /**
     * Do exclusive union between the 2 shapes and return result in an {@link AreaX} type shape.<br>
     * If one of the specified Shape is not an AreaX (do not contains any pixel) then it just return the other Shape in
     * AreaX format. If both Shape are not AreaX then an empty AreaX is returned.
     */
    public static AreaX exclusiveUnion(final Shape shape1, final Shape shape2) {
        // trivial optimization
        if (!isClosed(shape1)) {
            if (!isClosed(shape2))
                return new AreaX();

            return new AreaX(shape2);
        }

        // trivial optimization
        if (!isClosed(shape2))
            return new AreaX(shape1);

        final AreaX result = new AreaX(getClosedPath(shape1));

        result.exclusiveOr(new AreaX(getClosedPath(shape2)));

        return result;
    }

    /**
     * Subtract shape2 from shape1 return result in an {@link AreaX} type shape.
     */
    public static AreaX subtract(final Shape shape1, final Shape shape2) {
        // trivial optimization
        if (!isClosed(shape1))
            return new AreaX();
        if (!isClosed(shape2))
            return new AreaX(shape1);

        final AreaX result = new AreaX(getClosedPath(shape1));

        result.subtract(new AreaX(getClosedPath(shape2)));

        return result;
    }

    /**
     * Scale the specified {@link RectangularShape} by specified factor.
     *
     * @param shape
     *        the {@link RectangularShape} to scale
     * @param factor
     *        the scale factor
     * @param centered
     *        if true then scaling is centered (shape location is modified)
     * @param scalePosition
     *        if true then position is also 'rescaled' (shape location is modified)
     */
    public static void scale(final RectangularShape shape, final double factor, final boolean centered, final boolean scalePosition) {
        final double w = shape.getWidth();
        final double h = shape.getHeight();
        final double newW = w * factor;
        final double newH = h * factor;
        final double newX;
        final double newY;

        if (scalePosition) {
            newX = shape.getX() * factor;
            newY = shape.getY() * factor;
        }
        else {
            newX = shape.getX();
            newY = shape.getY();
        }

        if (centered) {
            final double deltaW = (newW - w) / 2;
            final double deltaH = (newH - h) / 2;

            shape.setFrame(newX - deltaW, newY - deltaH, newW, newH);
        }
        else
            shape.setFrame(newX, newY, newW, newH);
    }

    /**
     * Scale the specified {@link RectangularShape} by specified factor.
     *
     * @param shape
     *        the {@link RectangularShape} to scale
     * @param factor
     *        the scale factor
     * @param centered
     *        if true then scaling is centered (shape location is modified)
     */
    public static void scale(final RectangularShape shape, final double factor, final boolean centered) {
        scale(shape, factor, centered, false);
    }

    /**
     * Enlarge the specified {@link RectangularShape} by specified width and height.
     *
     * @param shape
     *        the {@link RectangularShape} to scale
     * @param width
     *        the width to add
     * @param height
     *        the height to add
     * @param centered
     *        if true then enlargement is centered (shape location is modified)
     */
    public static void enlarge(final RectangularShape shape, final double width, final double height, final boolean centered) {
        final double w = shape.getWidth();
        final double h = shape.getHeight();
        final double newW = w + width;
        final double newH = h + height;

        if (centered) {
            final double deltaW = (newW - w) / 2;
            final double deltaH = (newH - h) / 2;

            shape.setFrame(shape.getX() - deltaW, shape.getY() - deltaH, newW, newH);
        }
        else
            shape.setFrame(shape.getX(), shape.getY(), newW, newH);
    }

    /**
     * Translate a rectangular shape by the specified dx and dy value
     */
    public static void translate(final RectangularShape shape, final int dx, final int dy) {
        shape.setFrame(shape.getX() + dx, shape.getY() + dy, shape.getWidth(), shape.getHeight());
    }

    /**
     * Translate a rectangular shape by the specified dx and dy value
     */
    public static void translate(final RectangularShape shape, final double dx, final double dy) {
        shape.setFrame(shape.getX() + dx, shape.getY() + dy, shape.getWidth(), shape.getHeight());
    }

    /**
     * Permit to describe any PathIterator in a list of Shape which are returned
     * to the specified ShapeConsumer
     */
    public static boolean consumeShapeFromPath(final PathIterator path, final ShapeConsumer consumer) {
        final Line2D.Double line = new Line2D.Double();
        final QuadCurve2D.Double quadCurve = new QuadCurve2D.Double();
        final CubicCurve2D.Double cubicCurve = new CubicCurve2D.Double();
        double lastX, lastY, curX, curY, movX, movY;
        final double[] crd = new double[6];

        curX = 0;
        curY = 0;
        movX = 0;
        movY = 0;

        while (!path.isDone()) {
            final int segType = path.currentSegment(crd);

            lastX = curX;
            lastY = curY;

            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    curX = crd[0];
                    curY = crd[1];
                    movX = curX;
                    movY = curY;
                    break;

                case PathIterator.SEG_LINETO:
                    curX = crd[0];
                    curY = crd[1];
                    line.setLine(lastX, lastY, curX, curY);
                    if (!consumer.consume(line))
                        return false;
                    break;

                case PathIterator.SEG_QUADTO:
                    curX = crd[2];
                    curY = crd[3];
                    quadCurve.setCurve(lastX, lastY, crd[0], crd[1], curX, curY);
                    if (!consumer.consume(quadCurve))
                        return false;
                    break;

                case PathIterator.SEG_CUBICTO:
                    curX = crd[4];
                    curY = crd[5];
                    cubicCurve.setCurve(lastX, lastY, crd[0], crd[1], crd[2], crd[3], curX, curY);
                    if (!consumer.consume(cubicCurve))
                        return false;
                    break;

                case PathIterator.SEG_CLOSE:
                    line.setLine(lastX, lastY, movX, movY);
                    if (!consumer.consume(line))
                        return false;
                    break;
            }

            path.next();
        }

        return true;
    }

    /**
     * Consume all sub path of the specified {@link PathIterator}.<br>
     * We consider a new sub path when we meet both a {@link PathIterator#SEG_MOVETO} segment or after a
     * {@link PathIterator#SEG_CLOSE} segment (except the ending one).
     */
    public static void consumeSubPath(final PathIterator pathIt, final PathConsumer consumer) {
        final double[] crd = new double[6];
        Path2D current = null;

        while (!pathIt.isDone()) {
            switch (pathIt.currentSegment(crd)) {
                case PathIterator.SEG_MOVETO:
                    // had a previous not closed path ? --> consume it
                    if (current != null)
                        consumer.consumePath(current, false);

                    // create new path
                    current = new Path2D.Double(pathIt.getWindingRule());
                    current.moveTo(crd[0], crd[1]);
                    break;

                case PathIterator.SEG_LINETO:
                    Objects.requireNonNull(current).lineTo(crd[0], crd[1]);
                    break;

                case PathIterator.SEG_QUADTO:
                    Objects.requireNonNull(current).quadTo(crd[0], crd[1], crd[2], crd[3]);
                    break;

                case PathIterator.SEG_CUBICTO:
                    Objects.requireNonNull(current).curveTo(crd[0], crd[1], crd[2], crd[3], crd[4], crd[5]);
                    break;

                case PathIterator.SEG_CLOSE:
                    // close path and consume it
                    Objects.requireNonNull(current).closePath();
                    consumer.consumePath(current, true);

                    // clear path
                    current = null;
                    break;
            }

            pathIt.next();
        }

        // have a last not closed path ? --> consume it
        if (current != null)
            consumer.consumePath(current, false);
    }

    /**
     * Consume all sub path of the specified {@link Shape}.<br>
     * We consider a new sub path when we meet both a {@link PathIterator#SEG_MOVETO} segment or after a
     * {@link PathIterator#SEG_CLOSE} segment (except the ending one).
     */
    public static void consumeSubPath(final Shape shape, final PathConsumer consumer) {
        consumeSubPath(shape.getPathIterator(null), consumer);
    }

    /**
     * Returns only the open path part of the specified Shape.<br>
     * By default all sub path inside a Shape are considered closed which can be a problem when drawing or using
     * {@link Path2D#contains(double, double)} method.
     */
    public static Path2D getOpenPath(final Shape shape) {
        final PathIterator pathIt = shape.getPathIterator(null);
        final Path2D result = new Path2D.Double(pathIt.getWindingRule());

        consumeSubPath(pathIt, (path, closed) -> {
            if (!closed)
                result.append(path, false);

            return true;
        });

        return result;
    }

    /**
     * Returns only the closed path part of the specified Shape.<br>
     * By default all sub path inside a Shape are considered closed which can be a problem when drawing or using
     * {@link Path2D#contains(double, double)} method.
     */
    public static Path2D getClosedPath(final Shape shape) {
        final PathIterator pathIt = shape.getPathIterator(null);
        final Path2D result = new Path2D.Double(pathIt.getWindingRule());

        consumeSubPath(pathIt, (path, closed) -> {
            if (closed)
                result.append(path, false);

            return true;
        });

        return result;
    }

    /**
     * Return all PathAnchor points from the specified shape
     */
    public static ArrayList<PathAnchor2D> getAnchorsFromShape(final Shape shape, final Color color, final Color selectedColor) {
        final PathIterator pathIt = shape.getPathIterator(null);
        final ArrayList<PathAnchor2D> result = new ArrayList<>();
        final double[] crd = new double[6];
        final double[] mov = new double[2];

        while (!pathIt.isDone()) {
            final int segType = pathIt.currentSegment(crd);
            PathAnchor2D pt = null;

            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    mov[0] = crd[0];
                    mov[1] = crd[1];

                case PathIterator.SEG_LINETO:
                    pt = new PathAnchor2D(crd[0], crd[1], color, selectedColor, segType);
                    break;

                case PathIterator.SEG_QUADTO:
                    pt = new PathAnchor2D(crd[0], crd[1], crd[2], crd[3], color, selectedColor);
                    break;

                case PathIterator.SEG_CUBICTO:
                    pt = new PathAnchor2D(crd[0], crd[1], crd[2], crd[3], crd[4], crd[5], color, selectedColor);
                    break;

                case PathIterator.SEG_CLOSE:
                    pt = new PathAnchor2D(mov[0], mov[1], color, selectedColor, segType);
                    // CLOSE points aren't visible
                    pt.setVisible(false);
                    break;
            }

            if (pt != null)
                result.add(pt);

            pathIt.next();
        }

        return result;
    }

    /**
     * Return all PathAnchor points from the specified shape
     */
    public static ArrayList<PathAnchor2D> getAnchorsFromShape(final Shape shape) {
        return getAnchorsFromShape(shape, Anchor2D.DEFAULT_NORMAL_COLOR, Anchor2D.DEFAULT_SELECTED_COLOR);
    }

    /**
     * Update specified path from the specified list of PathAnchor2D
     */
    public static Path2D buildPathFromAnchors(final Path2D path, final List<PathAnchor2D> points, final boolean closePath) {
        path.reset();

        for (final PathAnchor2D pt : points) {
            switch (pt.getType()) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(pt.getX(), pt.getY());
                    break;

                case PathIterator.SEG_LINETO:
                    path.lineTo(pt.getX(), pt.getY());
                    break;

                case PathIterator.SEG_QUADTO:
                    path.quadTo(pt.getPosQExtX(), pt.getPosQExtY(), pt.getX(), pt.getY());
                    break;

                case PathIterator.SEG_CUBICTO:
                    path.curveTo(pt.getPosCExtX(), pt.getPosCExtY(), pt.getPosQExtX(), pt.getPosQExtY(), pt.getX(),
                            pt.getY());
                    break;

                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
            }
        }

        if ((points.size() > 1) && closePath)
            path.closePath();

        return path;
    }

    /**
     * Update specified path from the specified list of PathAnchor2D
     */
    public static Path2D buildPathFromAnchors(final Path2D path, final List<PathAnchor2D> points) {
        return buildPathFromAnchors(path, points, true);
    }

    /**
     * Create and return a path from the specified list of PathAnchor2D
     */
    public static Path2D getPathFromAnchors(final List<PathAnchor2D> points, final boolean closePath) {
        return buildPathFromAnchors(new Path2D.Double(), points, closePath);
    }

    /**
     * Create and return a path from the specified list of PathAnchor2D
     */
    public static Path2D getPathFromAnchors(final List<PathAnchor2D> points) {
        return buildPathFromAnchors(new Path2D.Double(), points, true);
    }

    /**
     * Return true if the specified PathIterator intersects with the specified Rectangle
     */
    public static boolean pathIntersects(final PathIterator path, final Rectangle2D rect) {
        return !consumeShapeFromPath(path, shape -> !shape.intersects(rect));
    }

}
