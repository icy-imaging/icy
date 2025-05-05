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

/**
 * 
 */
package org.bioimageanalysis.icy.common.geom.point;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Utilities for Point2D class.
 * 
 * @author Stephane
 */
public class Point2DUtil
{
    /**
     * @return Test if the 2 specified points are <code>connected</code>.<br>
     * Points are considered connected if max(deltaX, deltaY) &lt;= 1
     * @param p1 2D point
     * @param p2 2D point
     */
    public static boolean areConnected(Point2D p1, Point2D p2)
    {
        return Math.max(Math.abs(p2.getX() - p1.getX()), Math.abs(p2.getY() - p1.getY())) <= 1;
    }

    /**
     * @return Returns the L1 distance between 2 points
     * @param p1 2D point
     * @param p2 2D point
     */
    public static double getL1Distance(Point2D p1, Point2D p2)
    {
        return Math.abs(p2.getX() - p1.getX()) + Math.abs(p2.getY() - p1.getY());
    }

    /**
     * @return Returns the square of the distance between 2 points.
     * @param pt1 2D point
     * @param pt2 2D point
     */
    public static double getSquareDistance(Point2D pt1, Point2D pt2)
    {
        double px = pt2.getX() - pt1.getX();
        double py = pt2.getY() - pt1.getY();
        return (px * px) + (py * py);
    }

    /**
     * @return Returns the distance between 2 points.
     * @param pt1 2D point
     * @param pt2 2D point
     */
    public static double getDistance(Point2D pt1, Point2D pt2)
    {
        return Math.sqrt(getSquareDistance(pt1, pt2));
    }

    /**
     * @return Returns the distance between 2 points using specified scale factor for x/y dimension.
     * @param pt1 2D point
     * @param pt2 2D point
     * @param factorX double
     * @param factorY double
     */
    public static double getDistance(Point2D pt1, Point2D pt2, double factorX, double factorY)
    {
        double px = (pt2.getX() - pt1.getX()) * factorX;
        double py = (pt2.getY() - pt1.getY()) * factorY;
        return Math.sqrt(px * px + py * py);
    }

    /**
     * @return Returns the total distance of the specified list of points.
     * @param points list of 2D points
     * @param factorX double
     * @param factorY double
     * @param connectLastPoint boolean
     */
    public static double getTotalDistance(List<Point2D> points, double factorX, double factorY, boolean connectLastPoint)
    {
        final int size = points.size();
        double result = 0d;

        if (size > 1)
        {
            for (int i = 0; i < size - 1; i++)
                result += getDistance(points.get(i), points.get(i + 1), factorX, factorY);

            // add last to first point distance
            if (connectLastPoint)
                result += getDistance(points.get(size - 1), points.get(0), factorX, factorY);
        }

        return result;
    }
}
