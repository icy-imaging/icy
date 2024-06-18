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
import org.bioimageanalysis.icy.common.math.ArrayMath;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.logging.IcyLogger;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI2DEllipse extends ROI2DRectShape {
    public ROI2DEllipse(final Point2D topLeft, final Point2D bottomRight) {
        super(new Ellipse2D.Double(), topLeft, bottomRight);

        // set icon (default name is defined by getDefaultName()) 
        setIcon(SVGIcon.CIRCLE);
    }

    /**
     * Create a ROI ellipse from its rectangular bounds.
     */
    public ROI2DEllipse(final double xmin, final double ymin, final double xmax, final double ymax) {
        this(new Point2D.Double(xmin, ymin), new Point2D.Double(xmax, ymax));
    }

    public ROI2DEllipse(final Rectangle2D rectangle) {
        this(rectangle.getMinX(), rectangle.getMinY(), rectangle.getMaxX(), rectangle.getMaxY());
    }

    public ROI2DEllipse(final Ellipse2D ellipse) {
        this(ellipse.getBounds2D());
    }

    public ROI2DEllipse(final Point2D pt) {
        this(new Point2D.Double(pt.getX(), pt.getY()), pt);
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DEllipse(final Point5D pt) {
        this(pt.toPoint2D());
    }

    public ROI2DEllipse() {
        this(new Point2D.Double(), new Point2D.Double());
    }

    @Override
    public String getDefaultName() {
        return "Ellipse2D";
    }

    public Ellipse2D getEllipse() {
        return (Ellipse2D) shape;
    }

    public void setEllipse(final Ellipse2D ellipse) {
        setBounds2D(ellipse.getBounds2D());
    }

    @Override
    public double getLength(final Sequence sequence) throws UnsupportedOperationException {
        final Ellipse2D ellipse = getEllipse();
        return computeEllipsePerimeter(ellipse.getWidth() * 0.5d * sequence.getPixelSizeX(), ellipse.getHeight() * 0.5d * sequence.getPixelSizeY());
    }

    @Override
    public double computeNumberOfContourPoints() {
        final Ellipse2D ellipse = getEllipse();
        return computeEllipsePerimeter(ellipse.getWidth() * 0.5d, ellipse.getHeight() * 0.5d);
    }

    /**
     * Calculating the perimeter of an ellipse is non-trivial. Here we follow the approximation
     * proposed in:<br>
     * Ramanujan, S., "Modular Equations and Approximations to Pi," Quart. J. Pure. Appl. Math.,
     * v.45 (1913-1914), 350-372
     *
     * @since Icy 1.5.3.2
     */
    public static double computeEllipsePerimeter(final double w, final double h) {
        double result = (w - h) / (w + h);
        result *= result;

        return Math.PI * (w + h) * (1 + (result / 4) + ((result * result) / 64) + ((result * result * result) / 256));

    }

    @Override
    public double computeNumberOfPoints() {
        final Ellipse2D ellipse = getEllipse();
        return Math.PI * ellipse.getWidth() * ellipse.getHeight() / 4d;
    }

    /**
     * Adjust the ROI to fit the specified list of coordinates with a circle
     *
     * @param points
     *        the list of points to fit
     */
    public void setToFitCircle(final Collection<? extends Point2D> points) {
        int nbPoints = points.size();

        final double[] xCoords = new double[nbPoints];
        final double[] yCoords = new double[nbPoints];

        for (final Point2D point : points) {
            nbPoints--;
            xCoords[nbPoints] = point.getX();
            yCoords[nbPoints] = point.getY();
        }

        setToFitCircle(xCoords, yCoords);
    }

    /**
     * Circle fit by Taubin. <br/>
     * Reference: G. Taubin, "Estimation Of Planar Curves, Surfaces And Nonplanar Space Curves
     * Defined By Implicit
     * Equations, With Applications To Edge And Range Image Segmentation", IEEE Trans. PAMI, Vol.
     * 13, pages 1115-1138,
     * (1991).<br/>
     * This method is a port to Icy from the original <a
     * href="http://www.mathworks.com/matlabcentral/fileexchange/22678">Matlab code from Nikolai
     * Chernov (2009)</a>
     *
     * @param xCoords
     *        the X coordinates of the points to fit
     * @param yCoords
     *        the Y coordinates of the points to fit
     */
    private void setToFitCircle(final double[] xCoords, final double[] yCoords) {
        final int n = xCoords.length;

        if (n != yCoords.length)
            throw new IllegalArgumentException("Coordinate arrays must have the same size");

        final Point2D centroid = new Point2D.Double(ArrayMath.mean(xCoords), ArrayMath.mean(yCoords));

        // temporary buffer to save memory
        final double[] buffer = new double[n];

        final double[] X = ArrayMath.subtract(xCoords, centroid.getX());
        final double[] Y = ArrayMath.subtract(yCoords, centroid.getY());
        final double[] XX = ArrayMath.multiply(X, X);
        final double[] YY = ArrayMath.multiply(Y, Y);
        final double[] Z = ArrayMath.add(XX, YY);

        // compute normalized moments
        final double Mxx = ArrayMath.sum(XX) / n;
        final double Mxy = ArrayMath.sum(ArrayMath.multiply(X, Y, buffer)) / n;
        final double Myy = ArrayMath.sum(YY) / n;
        final double Mxz = ArrayMath.sum(ArrayMath.multiply(X, Z, buffer)) / n;
        final double Myz = ArrayMath.sum(ArrayMath.multiply(Y, Z, buffer)) / n;
        final double Mzz = ArrayMath.sum(ArrayMath.multiply(Z, Z, buffer)) / n;

        // computing the coefficients of the characteristic polynomial

        final double Mz = Mxx + Myy;
        final double Cov_xy = Mxx * Myy - Mxy * Mxy;
        final double A3 = 4 * Mz;
        final double A2 = -3 * Mz * Mz - Mzz;
        final double A1 = Mzz * Mz + 4 * Cov_xy * Mz - Mxz * Mxz - Myz * Myz - Mz * Mz * Mz;
        final double A0 = Mxz * Mxz * Myy + Myz * Myz * Mxx - Mzz * Cov_xy - 2 * Mxz * Myz * Mxy + Mz * Mz * Cov_xy;
        final double A22 = A2 + A2;
        final double A33 = A3 + A3 + A3;

        double xold, xnew = 0;
        double yold, ynew = 1e+20;
        final double epsilon = 1e-12;
        final int IterMax = 20;

        // Newton's method starting at x=0

        for (int iter = 0; iter < IterMax; iter++) {
            yold = ynew;
            ynew = A0 + xnew * (A1 + xnew * (A2 + xnew * A3));
            if (Math.abs(ynew) > Math.abs(yold)) {
                IcyLogger.error(ROI2DEllipse.class, "Circle fitting error: Newton-Taubin goes wrong direction: |ynew| > |yold|");
                xnew = 0;
                break;
            }

            final double Dy = A1 + xnew * (A22 + xnew * A33);
            xold = xnew;
            xnew = xold - ynew / Dy;

            if (Math.abs((xnew - xold) / xnew) < epsilon)
                break;

            if (iter >= IterMax) {
                IcyLogger.error(ROI2DEllipse.class, "Circle fitting error: Newton-Taubin will not converge");
                xnew = 0;
            }

            if (xnew < 0) {
                // TODO check if it's a warning, error or juste debug message
                IcyLogger.warn(ROI2DEllipse.class, "Newton-Taubin negative root: x=" + xnew);
                xnew = 0;
            }
        }

        // computing the circle parameters

        final double DET = xnew * xnew - xnew * Mz + Cov_xy;
        double xCenter = (Mxz * (Myy - xnew) - Myz * Mxy) / DET / 2;
        double yCenter = (Myz * (Mxx - xnew) - Mxz * Mxy) / DET / 2;
        final double radius = Math.sqrt(xCenter * xCenter + yCenter * yCenter + Mz);
        xCenter += centroid.getX();
        yCenter += centroid.getY();

        setEllipse(new Ellipse2D.Double(xCenter - radius, yCenter - radius, 2 * radius, 2 * radius));
    }
}
