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
package icy.roi;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import icy.image.IcyBufferedImage;
import icy.image.IntensityInfo;
import icy.math.DataIteratorMath;
import icy.math.MathUtil;
import icy.painter.Anchor2D;
import icy.painter.Anchor3D;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.SequenceUtil;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.type.collection.CollectionUtil;
import icy.type.dimension.Dimension3D;
import icy.type.dimension.Dimension5D;
import icy.type.geom.GeomUtil;
import icy.type.geom.Polygon2D;
import icy.type.point.Point3D;
import icy.type.point.Point4D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle2DUtil;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import icy.util.ShapeUtil.BooleanOperator;
import icy.util.StringUtil;
import plugins.kernel.roi.descriptor.intensity.ROIIntensityDescriptorsPlugin;
import plugins.kernel.roi.descriptor.intensity.ROIMaxIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIMeanIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIMinIntensityDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROIStandardDeviationDescriptor;
import plugins.kernel.roi.descriptor.intensity.ROISumIntensityDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIAreaDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIBasicMeasureDescriptorsPlugin;
import plugins.kernel.roi.descriptor.measure.ROIContourDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIInteriorDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIMassCenterDescriptorsPlugin;
import plugins.kernel.roi.descriptor.measure.ROIPerimeterDescriptor;
import plugins.kernel.roi.descriptor.measure.ROISurfaceAreaDescriptor;
import plugins.kernel.roi.descriptor.measure.ROIVolumeDescriptor;
import plugins.kernel.roi.morphology.ROIDilationCalculator;
import plugins.kernel.roi.morphology.ROIDistanceTransformCalculator;
import plugins.kernel.roi.morphology.ROIErosionCalculator;
import plugins.kernel.roi.morphology.skeletonization.ROISkeletonCalculator;
import plugins.kernel.roi.morphology.watershed.ROIWatershedCalculator;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi2d.ROI2DEllipse;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DRectShape;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import plugins.kernel.roi.roi2d.ROI2DShape;
import plugins.kernel.roi.roi3d.ROI3DArea;
import plugins.kernel.roi.roi3d.ROI3DBox;
import plugins.kernel.roi.roi3d.ROI3DCylinder;
import plugins.kernel.roi.roi3d.ROI3DFlatPolygon;
import plugins.kernel.roi.roi3d.ROI3DPoint;
import plugins.kernel.roi.roi3d.ROI3DShape;
import plugins.kernel.roi.roi3d.ROI3DStack;
import plugins.kernel.roi.roi3d.ROI3DStackEllipse;
import plugins.kernel.roi.roi3d.ROI3DStackPolygon;
import plugins.kernel.roi.roi3d.ROI3DStackRectangle;
import plugins.kernel.roi.roi3d.ROI3DZShape;
import plugins.kernel.roi.roi4d.ROI4DArea;
import plugins.kernel.roi.roi5d.ROI5DArea;

/**
 * ROI utilities class.
 * 
 * @author Stephane
 */
public class ROIUtil
{
    final public static String ZEXT_SUFFIX = " Z extended";
    final public static String STACK_SUFFIX = " stack";
    final public static String MASK_SUFFIX = " mask";
    final public static String SHAPE_SUFFIX = " shape";
    final public static String OBJECT_SUFFIX = " object";
    final public static String PART_SUFFIX = " part";

    /**
     * Returns all available ROI descriptors (see {@link ROIDescriptor}) and their attached plugin
     * (see {@link PluginROIDescriptor}).<br>
     * This list can be extended by installing new plugin(s) implementing the {@link PluginROIDescriptor}
     * interface.<br>
     * This method is an alias of {@link ROIDescriptor#getDescriptors()}
     * 
     * @see ROIDescriptor#compute(ROI, Sequence)
     * @see PluginROIDescriptor#compute(ROI, Sequence)
     */
    public static Map<ROIDescriptor, PluginROIDescriptor> getROIDescriptors()
    {
        return ROIDescriptor.getDescriptors();
    }

    /**
     * Computes the specified descriptor from the input {@link ROIDescriptor} set on given ROI
     * and returns the result (or <code>null</code> if the descriptor is not found).<br>
     * This method is an alias of {@link ROIDescriptor#computeDescriptor(Collection, String, ROI, Sequence)}
     * 
     * @param roiDescriptors
     *        the input {@link ROIDescriptor} set (see {@link #getROIDescriptors()} method)
     * @param descriptorId
     *        the id of the descriptor we want to compute ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for
     *        instance)
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     *         specified set
     * @throws InterruptedException
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(Collection<ROIDescriptor> roiDescriptors, String descriptorId, ROI roi,
            Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        return ROIDescriptor.computeDescriptor(roiDescriptors, descriptorId, roi, sequence);
    }

    /**
     * @throws InterruptedException
     * @throws UnsupportedOperationException
     * @deprecated Use {@link ROIDescriptor#computeDescriptor(Collection, String, ROI, Sequence)} instead
     */
    @Deprecated
    public static Object computeDescriptor(Set<ROIDescriptor> roiDescriptors, String descriptorId, ROI roi,
            Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        return ROIDescriptor.computeDescriptor(roiDescriptors, descriptorId, roi, sequence);
    }

    /**
     * Computes the specified descriptor on given ROI and returns the result (or <code>null</code> if the descriptor is
     * not found).<br>
     * This method is an alias of {@link ROIDescriptor#computeDescriptor(String, ROI, Sequence)}
     * 
     * @param descriptorId
     *        the id of the descriptor we want to compute ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for
     *        instance)
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     *         specified set
     * @throws InterruptedException
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(String descriptorId, ROI roi, Sequence sequence)
            throws UnsupportedOperationException, InterruptedException
    {
        return ROIDescriptor.computeDescriptor(descriptorId, roi, sequence);
    }

    /**
     * @deprecated Use {@link ROIStandardDeviationDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static double getStandardDeviation(Sequence sequence, ROI roi, int z, int t, int c)
    {
        try
        {
            final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, false, z, t, c);

            long numPixels = 0;
            double sum = 0;
            double sum2 = 0;

            // faster to do all calculation in a single iteration run
            while (!it.done())
            {
                final double value = it.get();

                sum += value;
                sum2 += value * value;
                numPixels++;

                it.next();
            }

            if (numPixels > 0)
            {
                double x1 = (sum2 / numPixels);
                double x2 = sum / numPixels;
                x2 *= x2;

                return Math.sqrt(x1 - x2);
            }
        }
        catch (Exception e)
        {
            // we can have exception as the process can be really long
            // and size modified during this period
        }

        return 0d;
    }

    /**
     * @deprecated Use {@link ROIIntensityDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi, int z, int t, int c)
    {
        try
        {
            final IntensityInfo result = new IntensityInfo();
            final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, false, z, t, c);

            long numPixels = 0;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            double sum = 0;

            // faster to do all calculation in a single iteration run
            while (!it.done())
            {
                final double value = it.get();

                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
                sum += value;
                numPixels++;

                it.next();
            }

            if (numPixels > 0)
            {
                result.minIntensity = min;
                result.maxIntensity = max;
                result.meanIntensity = sum / numPixels;
            }
            else
            {
                result.minIntensity = 0d;
                result.maxIntensity = 0d;
                result.meanIntensity = 0d;
            }

            return result;
        }
        catch (Exception e)
        {
            // we can have exception as the process can be really long
            // and size modified during this period
            return null;
        }
    }

    /**
     * Returns the number of sequence pixels contained in the specified ROI.
     * 
     * @param sequence
     *        The sequence we want to get the number of pixel.
     * @param roi
     *        The ROI define the region where we want to compute the number of pixel.
     * @param z
     *        The specific Z position (slice) where we want to compute the number of pixel or <code>-1</code> to use the
     *        ROI Z dimension information.
     * @param t
     *        The specific T position (frame) where we want to compute the number of pixel or <code>-1</code> to use the
     *        ROI T dimension information.
     * @param c
     *        The specific C position (channel) where we want to compute the number of pixel or <code>-1</code> to use
     *        the ROI C dimension information.
     * @throws InterruptedException
     */
    public static long getNumPixel(Sequence sequence, ROI roi, int z, int t, int c) throws InterruptedException
    {
        return DataIteratorUtil.count(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMinIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMinIntensity(Sequence sequence, ROI roi, int z, int t, int c) throws InterruptedException
    {
        return DataIteratorMath.min(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMaxIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMaxIntensity(Sequence sequence, ROI roi, int z, int t, int c) throws InterruptedException
    {
        return DataIteratorMath.max(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMeanIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMeanIntensity(Sequence sequence, ROI roi, int z, int t, int c) throws InterruptedException
    {
        return DataIteratorMath.mean(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROISumIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getSumIntensity(Sequence sequence, ROI roi, int z, int t, int c) throws InterruptedException
    {
        return DataIteratorMath.sum(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * @deprecated Use {@link ROIStandardDeviationDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static double getStandardDeviation(Sequence sequence, ROI roi)
    {
        return getStandardDeviation(sequence, roi, -1, -1, -1);
    }

    /**
     * @deprecated Use {@link ROIIntensityDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static IntensityInfo getIntensityInfo(Sequence sequence, ROI roi)
    {
        return getIntensityInfo(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the number of sequence pixels contained in the specified ROI.
     * 
     * @throws InterruptedException
     */
    public static long getNumPixel(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getNumPixel(sequence, roi, -1, -1, -1);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMinIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMinIntensity(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getMinIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMaxIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMaxIntensity(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getMaxIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMeanIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getMeanIntensity(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getMeanIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROISumIntensityDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static double getSumIntensity(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getSumIntensity(sequence, roi, -1, -1, -1);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point5D getMassCenter(ROI roi) throws InterruptedException
    {
        switch (roi.getDimension())
        {
            case 2:
                final ROI2D roi2d = (ROI2D) roi;
                final Point2D pt2d = getMassCenter(roi2d);
                return new Point5D.Double(pt2d.getX(), pt2d.getY(), roi2d.getZ(), roi2d.getT(), roi2d.getC());

            case 3:
                final ROI3D roi3d = (ROI3D) roi;
                final Point3D pt3d = getMassCenter(roi3d);
                return new Point5D.Double(pt3d.getX(), pt3d.getY(), pt3d.getZ(), roi3d.getT(), roi3d.getC());

            case 4:
                final ROI4D roi4d = (ROI4D) roi;
                final Point4D pt4d = getMassCenter(roi4d);
                return new Point5D.Double(pt4d.getX(), pt4d.getY(), pt4d.getZ(), pt4d.getT(), roi4d.getC());

            case 5:
                return getMassCenter((ROI5D) roi);

            default:
                return null;
        }
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point2D getMassCenter(ROI2D roi) throws InterruptedException
    {
        double x = 0, y = 0;
        long len = 0;

        final BooleanMask2D mask = roi.getBooleanMask(true);
        final boolean m[] = mask.mask;
        final int h = mask.bounds.height;
        final int w = mask.bounds.width;

        int off = 0;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                if (m[off++])
                {
                    x += i;
                    y += j;
                    len++;
                }
            }
        }

        // get bounds
        final Rectangle2D bounds2D = roi.getBounds2D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point2D.Double(bounds2D.getCenterX(), bounds2D.getCenterY());

        return new Point2D.Double(bounds2D.getX() + (x / len), bounds2D.getY() + (y / len));
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point3D getMassCenter(ROI3D roi) throws InterruptedException
    {
        double x = 0, y = 0, z = 0;
        long len = 0;
        final BooleanMask3D mask3d = roi.getBooleanMask(true);

        for (Integer zSlice : mask3d.mask.keySet())
        {
            final int zi = zSlice.intValue();
            final double zd = zi;
            final BooleanMask2D mask = mask3d.getMask2D(zi);
            final boolean m[] = mask.mask;
            final double bx = mask.bounds.x;
            final double by = mask.bounds.y;
            final int h = mask.bounds.height;
            final int w = mask.bounds.width;

            int off = 0;
            for (int j = 0; j < h; j++)
            {
                for (int i = 0; i < w; i++)
                {
                    if (m[off++])
                    {
                        x += bx + i;
                        y += by + j;
                        z += zd;
                        len++;
                    }
                }
            }
        }

        // get bounds
        final Rectangle3D bounds3D = roi.getBounds3D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point3D.Double(bounds3D.getCenterX(), bounds3D.getCenterY(), bounds3D.getCenterZ());

        return new Point3D.Double((x / len), (y / len), (z / len));
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point4D getMassCenter(ROI4D roi) throws InterruptedException
    {
        final BooleanMask4D mask4d = roi.getBooleanMask(true);
        double x = 0, y = 0, z = 0, t = 0;
        long len = 0;

        for (Integer tFrame : mask4d.mask.keySet())
        {
            final int ti = tFrame.intValue();
            final double td = ti;
            final BooleanMask3D mask3d = mask4d.getMask3D(ti);

            for (Integer zSlice : mask3d.mask.keySet())
            {
                final int zi = zSlice.intValue();
                final double zd = zi;
                final BooleanMask2D mask = mask3d.getMask2D(zi);
                final boolean m[] = mask.mask;
                final double bx = mask.bounds.x;
                final double by = mask.bounds.y;
                final int h = mask.bounds.height;
                final int w = mask.bounds.width;

                int off = 0;
                for (int j = 0; j < h; j++)
                {
                    for (int i = 0; i < w; i++)
                    {
                        if (m[off++])
                        {
                            x += bx + i;
                            y += by + j;
                            z += zd;
                            t += td;
                            len++;
                        }
                    }
                }
            }
        }

        // get bounds
        final Rectangle4D bounds4D = roi.getBounds4D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point4D.Double(bounds4D.getCenterX(), bounds4D.getCenterY(), bounds4D.getCenterZ(),
                    bounds4D.getCenterT());

        return new Point4D.Double((x / len), (y / len), (z / len), (t / len));

    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIMassCenterDescriptorsPlugin} or {@link #computeDescriptor(String, ROI, Sequence)}
     *             method instead.
     */
    @Deprecated
    public static Point5D getMassCenter(ROI5D roi) throws InterruptedException
    {
        final BooleanMask5D mask5d = roi.getBooleanMask(true);
        double x = 0, y = 0, z = 0, t = 0, c = 0;
        long len = 0;

        for (Integer cChannel : mask5d.mask.keySet())
        {
            final int ci = cChannel.intValue();
            final double cd = ci;
            final BooleanMask4D mask4d = mask5d.getMask4D(ci);

            for (Integer tFrame : mask4d.mask.keySet())
            {
                final int ti = tFrame.intValue();
                final double td = ti;
                final BooleanMask3D mask3d = mask4d.getMask3D(ti);

                for (Integer zSlice : mask3d.mask.keySet())
                {
                    final int zi = zSlice.intValue();
                    final double zd = zi;
                    final BooleanMask2D mask = mask3d.getMask2D(zi);
                    final boolean m[] = mask.mask;
                    final double bx = mask.bounds.x;
                    final double by = mask.bounds.y;
                    final int h = mask.bounds.height;
                    final int w = mask.bounds.width;

                    int off = 0;
                    for (int j = 0; j < h; j++)
                    {
                        for (int i = 0; i < w; i++)
                        {
                            if (m[off++])
                            {
                                x += bx + i;
                                y += by + j;
                                z += zd;
                                t += td;
                                c += cd;
                                len++;
                            }
                        }
                    }
                }
            }
        }

        // get bounds
        final Rectangle5D bounds5D = roi.getBounds5D();

        // empty roi --> use bounds center
        if (len == 0)
            return new Point5D.Double(bounds5D.getCenterX(), bounds5D.getCenterY(), bounds5D.getCenterZ(),
                    bounds5D.getCenterT(), bounds5D.getCenterC());

        return new Point5D.Double((x / len), (y / len), (z / len), (t / len), (c / len));
    }

    /**
     * @deprecated
     */
    @Deprecated
    private static double getMultiplier(Sequence sequence, ROI roi, int dim)
    {
        final int dimRoi = roi.getDimension();

        // cannot give this information for this roi
        if (dimRoi > dim)
            return 0d;

        final Rectangle5D boundsRoi = roi.getBounds5D();
        double mul = 1d;

        switch (dim)
        {
            case 5:
                if (dimRoi == 4)
                {
                    final int sizeC = sequence.getSizeC();

                    if ((boundsRoi.getSizeC() == Double.POSITIVE_INFINITY) && (sizeC > 1))
                        mul *= sizeC;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 4:
                if (dimRoi == 3)
                {
                    final int sizeT = sequence.getSizeT();

                    if ((boundsRoi.getSizeT() == Double.POSITIVE_INFINITY) && (sizeT > 1))
                        mul *= sizeT;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 3:
                if (dimRoi == 2)
                {
                    final int sizeZ = sequence.getSizeZ();

                    if ((boundsRoi.getSizeZ() == Double.POSITIVE_INFINITY) && (sizeZ > 1))
                        mul *= sizeZ;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 2:
                if (dimRoi == 1)
                {
                    final int sizeY = sequence.getSizeY();

                    if ((boundsRoi.getSizeY() == Double.POSITIVE_INFINITY) && (sizeY > 1))
                        mul *= sizeY;
                    // cannot give this information for this roi
                    else
                        mul = 0d;
                }
        }

        return mul;
    }

    /**
     * @deprecated Use {@link ROIContourDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getContourSize(Sequence sequence, double contourPoints, ROI roi, int dim, int roundSignificant)
    {
        final double mul = getMultiplier(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul != 0d)
            return sequence.calculateSize(MathUtil.roundSignificant(contourPoints, roundSignificant) * mul, dim,
                    dim - 1, 5);

        return "";
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIContourDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getContourSize(Sequence sequence, ROI roi, int dim, int roundSignificant)
            throws InterruptedException
    {
        return getContourSize(sequence, roi.getNumberOfContourPoints(), roi, dim, roundSignificant);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIContourDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getContourSize(Sequence sequence, ROI roi, int dim) throws InterruptedException
    {
        return getContourSize(sequence, roi, dim, 0);
    }

    /**
     * @deprecated Use {@link ROIInteriorDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getInteriorSize(Sequence sequence, double interiorPoints, ROI roi, int dim,
            int roundSignificant)
    {
        final double mul = getMultiplier(sequence, roi, dim);

        // 0 means the operation is not supported for this ROI
        if (mul != 0d)
            return sequence.calculateSize(MathUtil.roundSignificant(interiorPoints, roundSignificant) * mul, dim, dim,
                    5);

        return "";
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIInteriorDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getInteriorSize(Sequence sequence, ROI roi, int dim, int roundSignificant)
            throws InterruptedException
    {
        return getInteriorSize(sequence, roi.getNumberOfPoints(), roi, dim, roundSignificant);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIInteriorDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getInteriorSize(Sequence sequence, ROI roi, int dim) throws InterruptedException
    {
        return getInteriorSize(sequence, roi, dim, 0);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIPerimeterDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getPerimeter(Sequence sequence, ROI roi, int roundSignificant) throws InterruptedException
    {
        return getContourSize(sequence, roi, 2, roundSignificant);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIPerimeterDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getPerimeter(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getPerimeter(sequence, roi, 0);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getArea(Sequence sequence, ROI roi, int roundSignificant) throws InterruptedException
    {
        return getInteriorSize(sequence, roi, 2, roundSignificant);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getArea(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getArea(sequence, roi, 0);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROISurfaceAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getSurfaceArea(Sequence sequence, ROI roi, int roundSignificant) throws InterruptedException
    {
        return getContourSize(sequence, roi, 3, roundSignificant);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROISurfaceAreaDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method
     *             instead.
     */
    @Deprecated
    public static String getSurfaceArea(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getSurfaceArea(sequence, roi, 0);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIVolumeDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getVolume(Sequence sequence, ROI roi, int roundSignificant) throws InterruptedException
    {
        return getInteriorSize(sequence, roi, 3, roundSignificant);
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link ROIVolumeDescriptor} or {@link #computeDescriptor(String, ROI, Sequence)} method instead.
     */
    @Deprecated
    public static String getVolume(Sequence sequence, ROI roi) throws InterruptedException
    {
        return getVolume(sequence, roi, 0);
    }

    /**
     * Returns the effective ROI number of dimension needed for the specified bounds.
     */
    public static int getEffectiveDimension(Rectangle5D bounds)
    {
        int result = 5;

        if (bounds.isInfiniteC() || (bounds.getSizeC() <= 1d))
        {
            result--;
            if (bounds.isInfiniteT() || (bounds.getSizeT() <= 1d))
            {
                result--;
                if (bounds.isInfiniteZ() || (bounds.getSizeZ() <= 1d))
                    result--;
            }
        }

        return result;
    }

    /**
     * Return 5D dimension for specified operation dimension
     */
    private static Dimension5D.Integer getOpDim(int dim, Rectangle5D.Integer bounds)
    {
        final Dimension5D.Integer result = new Dimension5D.Integer();

        switch (dim)
        {
            case 2: // XY ROI with fixed ZTC
                result.sizeZ = 1;
                result.sizeT = 1;
                result.sizeC = 1;
                break;

            case 3: // XYZ ROI with fixed TC
                result.sizeZ = bounds.sizeZ;
                result.sizeT = 1;
                result.sizeC = 1;
                break;

            case 4: // XYZT ROI with fixed C
                result.sizeZ = bounds.sizeZ;
                result.sizeT = bounds.sizeT;
                result.sizeC = 1;
                break;

            default: // XYZTC ROI
                result.sizeZ = bounds.sizeZ;
                result.sizeT = bounds.sizeT;
                result.sizeC = bounds.sizeC;
                break;
        }

        return result;
    }

    /**
     * Get ROI result for specified 5D mask and operation dimension.
     */
    private static ROI getOpResult(int dim, BooleanMask5D mask, Rectangle5D.Integer bounds)
    {
        final ROI result;

        switch (dim)
        {
            case 2: // XY ROI with fixed ZTC
                result = new ROI2DArea(mask.getMask2D(bounds.z, bounds.t, bounds.c));

                // set ZTC position
                result.beginUpdate();
                try
                {
                    ((ROI2D) result).setZ(bounds.z);
                    ((ROI2D) result).setT(bounds.t);
                    ((ROI2D) result).setC(bounds.c);
                }
                finally
                {
                    result.endUpdate();
                }
                break;

            case 3: // XYZ ROI with fixed TC
                result = new ROI3DArea(mask.getMask3D(bounds.t, bounds.c));

                // set TC position
                result.beginUpdate();
                try
                {
                    ((ROI3D) result).setT(bounds.t);
                    ((ROI3D) result).setC(bounds.c);
                }
                finally
                {
                    result.endUpdate();
                }
                break;

            case 4: // XYZT ROI with fixed C
                result = new ROI4DArea(mask.getMask4D(bounds.c));
                // set C position
                ((ROI4D) result).setC(bounds.c);
                break;

            case 5: // XYZTC ROI
                result = new ROI5DArea(mask);
                break;

            default:
                throw new UnsupportedOperationException(
                        "Can't process boolean operation on a ROI with unknown dimension.");
        }

        return result;
    }

    /**
     * Compute the resulting bounds for <i>union</i> operation between specified ROIs.<br>
     * It throws an exception if the <i>union</i> operation cannot be done (incompatible dimension).
     */
    public static Rectangle5D getUnionBounds(ROI roi1, ROI roi2) throws UnsupportedOperationException
    {
        // null checking
        if (roi1 == null)
        {
            if (roi2 == null)
                return new Rectangle5D.Double();
            return roi2.getBounds5D();
        }
        else if (roi2 == null)
            return roi1.getBounds5D();

        final Rectangle5D bounds1 = roi1.getBounds5D();
        final Rectangle5D bounds2 = roi2.getBounds5D();

        // init infinite dim infos
        final boolean ic1 = bounds1.isInfiniteC();
        final boolean ic2 = bounds2.isInfiniteC();
        final boolean it1 = bounds1.isInfiniteT();
        final boolean it2 = bounds2.isInfiniteT();
        final boolean iz1 = bounds1.isInfiniteZ();
        final boolean iz2 = bounds2.isInfiniteZ();

        // cannot process union when we have an infinite dimension with a finite one
        if ((ic1 ^ ic2) || (it1 ^ it2) || (iz1 ^ iz2))
            throw new UnsupportedOperationException("Can't process union on ROI with different infinite dimension");

        // do union
        Rectangle5D.union(bounds1, bounds2, bounds1);

        // init infinite dim infos on result
        final boolean ic = bounds1.isInfiniteC(); // || (bounds1.getSizeC() <= 1d);
        final boolean it = bounds1.isInfiniteT(); // || (bounds1.getSizeT() <= 1d);
        final boolean iz = bounds1.isInfiniteZ(); // || (bounds1.getSizeZ() <= 1d);

        // cannot process union if C dimension is finite but T or Z is infinite
        if (!ic && (it || iz))
            throw new UnsupportedOperationException(
                    "Can't process union on ROI with a finite C dimension and infinite T or Z dimension");
        // cannot process union if T dimension is finite but Z is infinite
        if (!it && iz)
            throw new UnsupportedOperationException(
                    "Can't process union on ROI with a finite T dimension and infinite Z dimension");

        return bounds1;
    }

    /**
     * Compute the resulting bounds for <i>intersection</i> operation between specified ROIs.<br>
     * It throws an exception if the <i>intersection</i> operation cannot be done (incompatible dimension).
     */
    protected static Rectangle5D getIntersectionBounds(ROI roi1, ROI roi2) throws UnsupportedOperationException
    {
        // null checking
        if ((roi1 == null) || (roi2 == null))
            return new Rectangle5D.Double();

        final Rectangle5D bounds1 = roi1.getBounds5D();
        final Rectangle5D bounds2 = roi2.getBounds5D();

        // do intersection
        Rectangle5D.intersect(bounds1, bounds2, bounds1);

        // init infinite dim infos
        final boolean ic = bounds1.isInfiniteC(); // || (bounds1.getSizeC() <= 1d);
        final boolean it = bounds1.isInfiniteT(); // || (bounds1.getSizeT() <= 1d);
        final boolean iz = bounds1.isInfiniteZ(); // || (bounds1.getSizeZ() <= 1d);

        // cannot process intersection if C dimension is finite but T or Z is infinite
        if (!ic && (it || iz))
            throw new UnsupportedOperationException(
                    "Can't process intersection on ROI with a finite C dimension and infinite T or Z dimension");
        // cannot process intersection if T dimension is finite but Z is infinite
        if (!it && iz)
            throw new UnsupportedOperationException(
                    "Can't process intersection on ROI with a finite T dimension and infinite Z dimension");

        return bounds1;
    }

    /**
     * Compute the resulting bounds for <i>subtraction</i> of (roi1 - roi2).<br>
     * It throws an exception if the <i>subtraction</i> operation cannot be done (incompatible dimension).
     */
    protected static Rectangle5D getSubtractionBounds(ROI roi1, ROI roi2) throws UnsupportedOperationException
    {
        // null checking
        if (roi1 == null)
            return new Rectangle5D.Double();
        if (roi2 == null)
            return roi1.getBounds5D();

        final Rectangle5D bounds1 = roi1.getBounds5D();
        final Rectangle5D bounds2 = roi2.getBounds5D();

        // init infinite dim infos
        final boolean ic1 = bounds1.isInfiniteC();
        final boolean ic2 = bounds2.isInfiniteC();
        final boolean it1 = bounds1.isInfiniteT();
        final boolean it2 = bounds2.isInfiniteT();
        final boolean iz1 = bounds1.isInfiniteZ();
        final boolean iz2 = bounds2.isInfiniteZ();

        // cannot process subtraction when we have an finite dimension on second ROI
        // while having a infinite one on the first ROI
        if (ic1 && !ic2)
            throw new UnsupportedOperationException(
                    "Can't process subtraction: ROI 1 has infinite C dimension while ROI 2 has a finite one");
        if (it1 && !it2)
            throw new UnsupportedOperationException(
                    "Can't process subtraction: ROI 1 has infinite T dimension while ROI 2 has a finite one");
        if (iz1 && !iz2)
            throw new UnsupportedOperationException(
                    "Can't process subtraction: ROI 1 has infinite Z dimension while ROI 2 has a finite one");

        return bounds1;
    }

    /**
     * Computes union of specified <code>ROI</code> and return result in a new <code>ROI</code>.
     * 
     * @throws InterruptedException
     */
    public static ROI getUnion(ROI roi1, ROI roi2) throws UnsupportedOperationException, InterruptedException
    {
        // null checking
        if (roi1 == null)
        {
            // return empty ROI
            if (roi2 == null)
                return new ROI2DArea();
            // return simple copy
            return roi2.getCopy();
        }
        else if (roi2 == null)
            return roi1.getCopy();

        final Rectangle5D bounds5D = getUnionBounds(roi1, roi2);
        final int dim = getEffectiveDimension(bounds5D);

        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getUnion(
                            roi1.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            roi2.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(new Rectangle3D.Integer(bounds3D), mask3D);
            }

            mask5D[c] = new BooleanMask4D(new Rectangle4D.Integer(bounds4D), mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Union");

        return result;
    }

    /**
     * Computes intersection of specified <code>ROI</code> and return result in a new <code>ROI</code>.
     * 
     * @throws InterruptedException
     */
    public static ROI getIntersection(ROI roi1, ROI roi2) throws UnsupportedOperationException, InterruptedException
    {
        // null checking
        if ((roi1 == null) || (roi2 == null))
            // return empty ROI
            return new ROI2DArea();

        final Rectangle5D bounds5D = getIntersectionBounds(roi1, roi2);
        final int dim = getEffectiveDimension(bounds5D);

        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 2D, 3D and 4D bounds
        final Rectangle bounds2D = (Rectangle) bounds.toRectangle2D();
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    final BooleanMask2D roi1Mask2D = new BooleanMask2D(new Rectangle(bounds2D),
                            roi1.getBooleanMask2D(bounds2D, bounds.z + z, bounds.t + t, bounds.c + c, true));
                    final BooleanMask2D roi2Mask2D = new BooleanMask2D(new Rectangle(bounds2D),
                            roi2.getBooleanMask2D(bounds2D, bounds.z + z, bounds.t + t, bounds.c + c, true));

                    mask3D[z] = BooleanMask2D.getIntersection(roi1Mask2D, roi2Mask2D);
                }

                mask4D[t] = new BooleanMask3D(new Rectangle3D.Integer(bounds3D), mask3D);
            }

            mask5D[c] = new BooleanMask4D(new Rectangle4D.Integer(bounds4D), mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Intersection");

        return result;
    }

    /**
     * Compute exclusive union of specified <code>ROI</code> and return result in a new <code>ROI</code>.
     * 
     * @throws InterruptedException
     */
    public static ROI getExclusiveUnion(ROI roi1, ROI roi2) throws UnsupportedOperationException, InterruptedException
    {
        // null checking
        if (roi1 == null)
        {
            // return empty ROI
            if (roi2 == null)
                return new ROI2DArea();
            // return simple copy
            return roi2.getCopy();
        }
        else if (roi2 == null)
            return roi1.getCopy();

        final Rectangle5D bounds5D = getUnionBounds(roi1, roi2);
        final int dim = getEffectiveDimension(bounds5D);

        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getExclusiveUnion(
                            roi1.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            roi2.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(new Rectangle3D.Integer(bounds3D), mask3D);
            }

            mask5D[c] = new BooleanMask4D(new Rectangle4D.Integer(bounds4D), mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Exclusive union");

        return result;
    }

    /**
     * Computes the subtraction of roi1 - roi2 and returns result in a new <code>ROI</code>.
     * 
     * @throws InterruptedException
     */
    public static ROI getSubtraction(ROI roi1, ROI roi2) throws UnsupportedOperationException, InterruptedException
    {
        // return empty ROI
        if (roi1 == null)
            return new ROI2DArea();
        // return copy of ROI1
        if (roi2 == null)
            return roi1.getCopy();

        final Rectangle5D bounds5D = getSubtractionBounds(roi1, roi2);
        final int dim = getEffectiveDimension(bounds5D);

        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getSubtraction(
                            roi1.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            roi2.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(new Rectangle3D.Integer(bounds3D), mask3D);
            }

            mask5D[c] = new BooleanMask4D(new Rectangle4D.Integer(bounds4D), mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Substraction");

        return result;
    }

    /**
     * Merge the specified array of {@link ROI} with the given {@link BooleanOperator}.<br>
     * 
     * @param rois
     *        ROIs we want to merge.
     * @param operator
     *        {@link BooleanOperator} to apply.
     * @return {@link ROI} representing the result of the merge operation.
     * @throws InterruptedException
     */
    public static ROI merge(List<? extends ROI> rois, BooleanOperator operator)
            throws UnsupportedOperationException, InterruptedException
    {
        if (rois.size() == 0)
            return null;

        ROI result = rois.get(0).getCopy();

        // copy can fail...
        if (result != null)
        {
            switch (operator)
            {
                case AND:
                    for (int i = 1; i < rois.size(); i++)
                    {
                        // interrupt task
                        if (Thread.currentThread().interrupted())
                            throw new InterruptedException("ROI AND merging process interrupted.");
                        result = result.intersect(rois.get(i), true);
                    }
                    break;
                case OR:
                    for (int i = 1; i < rois.size(); i++)
                    {
                        // interrupt task
                        if (Thread.currentThread().interrupted())
                            throw new InterruptedException("ROI OR merging process interrupted.");
                        result = result.add(rois.get(i), true);
                    }
                    break;
                case XOR:
                    for (int i = 1; i < rois.size(); i++)
                    {
                        // interrupt task
                        if (Thread.currentThread().interrupted())
                            throw new InterruptedException("ROI XOR merging process interrupted.");
                        result = result.exclusiveAdd(rois.get(i), true);
                    }
                    break;
            }
        }

        // for (int i = 1; i < rois.size(); i++)
        // {
        // final ROI roi = rois.get(i);
        //
        // switch (operator)
        // {
        // case AND:
        // result = result.getIntersection(roi);
        // break;
        // case OR:
        // result = result.getUnion(roi);
        // break;
        // case XOR:
        // result = result.getExclusiveUnion(roi);
        // break;
        // }
        // }

        return result;

    }

    /**
     * Builds and returns a ROI corresponding to the union of the specified ROI list.
     * 
     * @throws InterruptedException
     */
    public static ROI getUnion(List<? extends ROI> rois) throws UnsupportedOperationException, InterruptedException
    {
        return merge(rois, BooleanOperator.OR);
    }

    /**
     * Builds and returns a ROI corresponding to the exclusive union of the specified ROI list.
     * 
     * @throws InterruptedException
     */
    public static ROI getExclusiveUnion(List<? extends ROI> rois)
            throws UnsupportedOperationException, InterruptedException
    {
        return merge(rois, BooleanOperator.XOR);
    }

    /**
     * Builds and returns a ROI corresponding to the intersection of the specified ROI list.
     * 
     * @throws InterruptedException
     */
    public static ROI getIntersection(List<? extends ROI> rois)
            throws UnsupportedOperationException, InterruptedException
    {
        return merge(rois, BooleanOperator.AND);
    }

    /**
     * Subtract the content of the roi2 from the roi1 and return the result as a new {@link ROI}.<br>
     * This is equivalent to: <code>roi1.getSubtraction(roi2)</code>
     * 
     * @return {@link ROI} representing the result of subtraction.
     * @throws InterruptedException
     */
    public static ROI subtract(ROI roi1, ROI roi2) throws UnsupportedOperationException, InterruptedException
    {
        return roi1.getSubtraction(roi2);
    }

    /**
     * Converts the specified ROI to a ROI Point ({@link ROI2DPoint} or {@link ROI3DPoint}) representing the mass center of the input ROI.
     * 
     * @return the ROI point representing the mass center of the input ROI.
     * @throws InterruptedException
     */
    public static ROI convertToPoint(ROI roi) throws InterruptedException
    {
        final ROI result;
        final Point5D pt = ROIMassCenterDescriptorsPlugin.computeMassCenter(roi);

        if (roi instanceof ROI2D)
        {
            result = new ROI2DPoint(pt.getX(), pt.getY());
            ((ROI2DPoint) result).setZ(((ROI2D) roi).getZ());
            ((ROI2DPoint) result).setT(((ROI2D) roi).getT());
            ((ROI2DPoint) result).setC(((ROI2D) roi).getC());
        }
        else if (roi instanceof ROI3D)
        {
            result = new ROI3DPoint(pt.getX(), pt.getY(), pt.getZ());
            ((ROI3DPoint) result).setT(((ROI3D) roi).getT());
            ((ROI3DPoint) result).setC(((ROI3D) roi).getC());
        }
        else
        {
            result = new ROI3DPoint(pt.getX(), pt.getY(), pt.getZ());
            ((ROI3DPoint) result).setT((int) pt.getT());
            ((ROI3DPoint) result).setC((int) pt.getC());
        }

        // preserve properties
        ROIUtil.copyROIProperties(roi, result, true);

        return result;
    }

    /**
     * Converts the specified ROI to a 2D ellipse type ROI centered on the mass center of the input ROI.
     * 
     * @return the 2D ellipse ROI centered on the mass center of the input ROI.
     * @throws InterruptedException
     */
    public static ROI2DEllipse convertToEllipse(ROI roi, double radiusX, double radiusY) throws InterruptedException
    {
        final Point5D pt = ROIMassCenterDescriptorsPlugin.computeMassCenter(roi);
        final double x = pt.getX();
        final double y = pt.getY();
        final ROI2DEllipse result = new ROI2DEllipse(x - radiusX, y - radiusY, x + radiusX, y + radiusY);

        if (roi instanceof ROI2D)
        {
            result.setZ(((ROI2D) roi).getZ());
            result.setT(((ROI2D) roi).getT());
            result.setC(((ROI2D) roi).getC());
        }
        else if (roi instanceof ROI3D)
        {
            result.setZ((int) pt.getZ());
            result.setT(((ROI3D) roi).getT());
            result.setC(((ROI3D) roi).getC());
        }
        else
        {
            result.setZ((int) pt.getZ());
            result.setT((int) pt.getT());
            result.setC((int) pt.getC());
        }

        // preserve properties
        ROIUtil.copyROIProperties(roi, result, true);

        return result;
    }

    /**
     * Converts the specified ROI to a 2D rectangle type ROI centered on the mass center of the input ROI.
     * 
     * @return the 2D rectangle ROI centered on the mass center of the input ROI.
     * @throws InterruptedException
     */
    public static ROI2DRectangle convertToRectangle(ROI roi, double width, double height) throws InterruptedException
    {
        final Point5D pt = ROIMassCenterDescriptorsPlugin.computeMassCenter(roi);
        final double x = pt.getX();
        final double y = pt.getY();
        final double rw = width / 2;
        final double rh = height / 2;
        final ROI2DRectangle result = new ROI2DRectangle(x - rw, y - rh, x + rw, y + rh);

        if (roi instanceof ROI2D)
        {
            result.setZ(((ROI2D) roi).getZ());
            result.setT(((ROI2D) roi).getT());
            result.setC(((ROI2D) roi).getC());
        }
        else if (roi instanceof ROI3D)
        {
            result.setZ((int) pt.getZ());
            result.setT(((ROI3D) roi).getT());
            result.setC(((ROI3D) roi).getC());
        }
        else
        {
            result.setZ((int) pt.getZ());
            result.setT((int) pt.getT());
            result.setC((int) pt.getC());
        }

        // preserve properties
        ROIUtil.copyROIProperties(roi, result, true);

        return result;
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link #convertTo3D(ROI2D, double, double)} instead.
     */
    public static ROI convertToStack(ROI2D roi, int zMin, int zMax) throws InterruptedException
    {
        ROI result = null;

        if (roi instanceof ROI2DRectangle)
            result = new ROI3DStackRectangle(((ROI2DRectangle) roi).getRectangle(), zMin, zMax);
        else if (roi instanceof ROI2DEllipse)
            result = new ROI3DStackEllipse(((ROI2DEllipse) roi).getEllipse(), zMin, zMax);
        else if (roi instanceof ROI2DPolygon)
            result = new ROI3DStackPolygon(((ROI2DPolygon) roi).getPolygon2D(), zMin, zMax);
        else if (roi instanceof ROI2DArea)
            result = new ROI3DArea(((ROI2DArea) roi).getBooleanMask(true), zMin, zMax);
        else if (roi != null)
            result = new ROI3DArea(roi.getBooleanMask2D(roi.getZ(), roi.getT(), roi.getC(), true), zMin, zMax);

        if ((roi != null) && (result != null))
        {
            // unselect all control points
            result.unselectAllPoints();
            // keep original ROI informations
            result.setName(roi.getName() + STACK_SUFFIX);
            copyROIProperties(roi, result, false);
        }

        return result;
    }

    /**
     * @throws InterruptedException
     * @deprecated Use {@link #convertTo2D(ROI3D)} instead
     */
    public static ROI[] unstack(ROI3D roi) throws InterruptedException
    {
        return convertTo2D(roi);
    }

    /**
     * Converts the specified 2D ROI to 3D ROI by elongating it along the Z axis with the given Z position and size Z parameters.
     * 
     * @return the converted 3D ROI
     * @throws InterruptedException
     */
    public static ROI convertTo3D(ROI2D roi, double z, double sizeZ) throws InterruptedException
    {
        ROI result = null;

        if (roi instanceof ROI2DRectangle)
            result = new ROI3DBox(roi.getBounds2D(), z, sizeZ);
        else if (roi instanceof ROI2DEllipse)
            result = new ROI3DCylinder(roi.getBounds2D(), z, sizeZ);
        else if (roi instanceof ROI2DPolygon)
            result = new ROI3DFlatPolygon(((ROI2DPolygon) roi).getPolygon2D(), z, sizeZ);
        else
        {
            int zMin = (int) z;
            int zMax = (int) (z + sizeZ);
            // integer ? --> decrement by one
            if ((double) zMax == (z + sizeZ))
                zMax--;

            // empty
            if (zMin > zMax)
                return null;

            if (roi instanceof ROI2DArea)
                result = new ROI3DArea(((ROI2DArea) roi).getBooleanMask(true), zMin, zMax);
            else
                result = new ROI3DArea(roi.getBooleanMask2D(roi.getZ(), roi.getT(), roi.getC(), true), zMin, zMax);
        }

        if (result != null)
        {
            // unselect all control points
            result.unselectAllPoints();
            // preserve origin name is not the default name
            if (!roi.isDefaultName())
                result.setName(roi.getName() + ZEXT_SUFFIX);
            // keep original ROI informations
            copyROIProperties(roi, result, false);
        }

        return result;
    }

    /**
     * Converts the specified 3D ROI to 2D ROI(s).<br>
     * 3D stack ROI are converted to multiple ROI2D representing each Z slice of the original 3D stack.
     * 
     * @return the converted 2D ROIs or <code>null</code> if the input ROI was null
     * @throws InterruptedException
     */
    public static ROI[] convertTo2D(ROI3D roi) throws InterruptedException
    {
        ROI[] result = null;

        if (roi instanceof ROI3DPoint)
        {
            ROI3DPoint roi3d = ((ROI3DPoint) roi);
            Point3D position = roi3d.getPosition3D();
            ROI2DPoint roi2d = new ROI2DPoint(position.getX(), position.getY());
            roi2d.c = roi3d.c;
            roi2d.t = roi3d.t;
            roi2d.z = (int) Math.round(position.getZ());
            result = new ROI[] {roi2d};
        }
        else if (roi instanceof ROI3DStack)
        {
            ROI3DStack<?> roi3d = ((ROI3DStack<?>) roi);
            List<ROI> rois2d = new ArrayList<ROI>(roi3d.getSizeZ());
            int z0 = (int) Math.floor(roi3d.getBounds3D().getZ());
            for (int z = z0; z < z0 + roi3d.getSizeZ(); z++)
            {
                ROI2D roi2d = roi3d.getSlice(z);
                if (roi2d != null)
                {
                    roi2d = (ROI2D) roi2d.getCopy();
                    roi2d.setZ(z);
                    roi2d.setC(roi3d.c);
                    roi2d.setT(roi3d.t);
                    rois2d.add(roi2d);
                }
            }
            result = rois2d.toArray(new ROI[rois2d.size()]);
        }
        else if (roi instanceof ROI3DZShape)
        {
            ROI3DZShape roi3d = ((ROI3DZShape) roi);
            ROI2DShape roi2d = roi3d.getShape2DROI();
            if (roi2d != null)
            {
                roi2d = (ROI2DShape) roi2d.getCopy();
                roi2d.setZ(-1);
                roi2d.setC(roi3d.c);
                roi2d.setT(roi3d.t);
            }
            result = new ROI[] {roi2d};
        }
        else if (roi instanceof ROI3DArea)
        {
            ROI3DArea roi3d = ((ROI3DArea) roi);
            List<ROI> rois2d = new ArrayList<ROI>(roi3d.getSizeZ());
            int z0 = (int) Math.floor(roi3d.getBounds3D().getZ());
            for (int z = z0; z < z0 + roi3d.getSizeZ(); z++)
            {
                ROI2DArea roi2d = new ROI2DArea(roi3d.getBooleanMask2D(z, true));
                roi2d.setZ(z);
                roi2d.setC(roi3d.c);
                roi2d.setT(roi3d.t);
                rois2d.add(roi2d);
            }
            result = rois2d.toArray(new ROI[rois2d.size()]);
        }
        else if (roi != null)
        {
            ROI3D roi3d = ((ROI3D) roi);
            int sizeZ = (int) Math.round(roi3d.getBounds3D().getSizeZ());
            List<ROI> rois2d = new ArrayList<ROI>(sizeZ);
            for (int z = (int) Math.floor(roi3d.getBounds3D().getZ()); z <= sizeZ; z++)
            {
                BooleanMask2D mask2d = roi3d.getBooleanMask2D(z, true);
                if (mask2d != null && !mask2d.isEmpty())
                {
                    ROI2DArea roi2d = new ROI2DArea(mask2d);
                    roi2d.setZ(z);
                    roi2d.setC(roi3d.c);
                    roi2d.setT(roi3d.t);
                    rois2d.add(roi2d);
                }
            }
            result = rois2d.toArray(new ROI[rois2d.size()]);
        }

        if ((roi != null) && (result != null))
        {
            String name = roi.getName();

            // remove "stack" suffix is present
            if (name.endsWith(STACK_SUFFIX))
                name = StringUtil.removeLast(name, STACK_SUFFIX.length());
            else if (name.endsWith(ZEXT_SUFFIX))
                name = StringUtil.removeLast(name, ZEXT_SUFFIX.length());

            // unselect all control points
            for (ROI roi2d : result)
            {
                roi2d.unselectAllPoints();
                // preserve origin name is not the default name
                if (!roi.isDefaultName())
                    roi2d.setName(name);
                // keep original ROI informations
                copyROIProperties(roi, roi2d, false);
            }
        }

        return result;
    }

    /**
     * Converts the specified ROI to a boolean mask type ROI (ROI Area).
     * 
     * @return the ROI Area corresponding to the input ROI.<br>
     *         If the ROI is already of boolean mask type then it's directly returned without any conversion.
     * @throws InterruptedException
     */
    public static ROI convertToMask(ROI roi) throws InterruptedException
    {
        // no conversion needed
        if ((roi instanceof ROI2DArea) || (roi instanceof ROI3DArea) || (roi instanceof ROI4DArea)
                || (roi instanceof ROI5DArea))
            return roi;

        final Rectangle5D bounds5D = roi.getBounds5D();
        final int dim = getEffectiveDimension(bounds5D);

        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 2D, 3D and 4D bounds
        final Rectangle bounds2D = (Rectangle) bounds.toRectangle2D();
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        // build 5D mask result
        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                    mask3D[z] = new BooleanMask2D(new Rectangle(bounds2D),
                            roi.getBooleanMask2D(bounds2D, bounds.z + z, bounds.t + t, bounds.c + c, true));

                mask4D[t] = new BooleanMask3D(new Rectangle3D.Integer(bounds3D), mask3D);
            }

            mask5D[c] = new BooleanMask4D(new Rectangle4D.Integer(bounds4D), mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);

        // keep original ROI informations
        String newName = roi.getName() + MASK_SUFFIX;
        // check if we can shorter name
        final String cancelableSuffix = SHAPE_SUFFIX + MASK_SUFFIX;
        if (newName.endsWith(cancelableSuffix))
            newName = newName.substring(0, newName.length() - cancelableSuffix.length());
        // set name
        result.setName(newName);
        // copy properties
        copyROIProperties(roi, result, false);

        return result;
    }

    /**
     * Converts the specified ROI to a shape type ROI (ROI Polygon or ROI Mesh).
     * 
     * @param roi
     *        the roi to convert to shape type ROI
     * @param maxDeviation
     *        maximum allowed deviation/distance of resulting ROI polygon from the input ROI contour (in pixel).
     *        Use <code>-1</code> for automatic maximum deviation calculation.
     * @return the ROI Polygon or ROI Mesh corresponding to the input ROI.<br>
     *         If the ROI is already of shape type then it's directly returned without any conversion.
     * @throws InterruptedException
     */
    public static ROI convertToShape(ROI roi, double maxDeviation)
            throws UnsupportedOperationException, InterruptedException
    {
        if (roi instanceof ROI2DShape)
            return roi;

        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;

            // get contour points in connected order
            final List<Point> points = roi2d.getBooleanMask(true).getConnectedContourPoints();

            // convert to point2D and center points in observed pixel.
            final List<Point2D> points2D = new ArrayList<Point2D>(points.size());
            for (Point pt : points)
                points2D.add(new Point2D.Double(pt.x + 0.5d, pt.y + 0.5d));

            final double dev;

            // auto deviation
            if (maxDeviation < 0)
            {
                // compute it from ROI size
                final Rectangle2D bnd = roi2d.getBounds2D();
                dev = Math.log10(Math.sqrt(bnd.getWidth() * bnd.getHeight())) / Math.log10(3);
            }
            else
                dev = maxDeviation;

            // convert to ROI polygon
            final ROI2DPolygon result = new ROI2DPolygon(Polygon2D.getPolygon2D(points2D, dev));

            // keep original ROI informations
            String newName = roi.getName() + SHAPE_SUFFIX;
            // check if we can shorter name
            final String cancelableSuffix = MASK_SUFFIX + SHAPE_SUFFIX;
            if (newName.endsWith(cancelableSuffix))
                newName = newName.substring(0, newName.length() - cancelableSuffix.length());
            // set name
            result.setName(newName);
            // copy properties
            copyROIProperties(roi, result, false);

            return result;
        }

        if (roi instanceof ROI3D)
        {
            // not yet supported
            throw new UnsupportedOperationException("ROIUtil.convertToShape(ROI): Operation not supported for 3D ROI.");

        }

        throw new UnsupportedOperationException(
                "ROIUtil.convertToShape(ROI): Operation not supported for this ROI: " + roi.getName());
    }

    /**
     * Returns connected component from specified ROI as a list of ROI (Area type).
     * 
     * @throws InterruptedException
     */
    public static List<ROI> getConnectedComponents(ROI roi) throws UnsupportedOperationException, InterruptedException
    {
        final List<ROI> result = new ArrayList<ROI>();

        if (roi instanceof ROI2D)
        {
            final ROI2D roi2d = (ROI2D) roi;
            int ind = 0;

            for (BooleanMask2D component : roi2d.getBooleanMask(true).getComponents())
            {
                final ROI2DArea componentRoi = new ROI2DArea(component);

                if (!componentRoi.isEmpty())
                {
                    // keep original ROI informations
                    componentRoi.setName(roi.getName() + OBJECT_SUFFIX + " #" + ind++);
                    copyROIProperties(roi, componentRoi, false);

                    result.add(componentRoi);
                }
            }

            return result;
        }

        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;
            int ind = 0;

            for (BooleanMask3D component : roi3d.getBooleanMask(true).getComponents())
            {
                final ROI3DArea componentRoi = new ROI3DArea(component);

                if (!componentRoi.isEmpty())
                {
                    // keep original ROI informations
                    componentRoi.setName(roi.getName() + " object #" + ind++);
                    componentRoi.setT(roi3d.t);
                    componentRoi.setC(roi3d.c);
                    copyROIProperties(roi, componentRoi, false);

                    result.add(componentRoi);
                }
            }
            return result;
        }

        throw new UnsupportedOperationException(
                "ROIUtil.getConnectedComponents(ROI): Operation not supported for this ROI: " + roi.getName());
    }

    static boolean computePolysFromLine(Line2D line, Point2D edgePt1, Point2D edgePt2, Polygon2D poly1, Polygon2D poly2,
            boolean inner)
    {
        final Line2D edgeLine = new Line2D.Double(edgePt1, edgePt2);

        // they intersect ?
        if (edgeLine.intersectsLine(line))
        {
            final Point2D intersection = GeomUtil.getIntersection(edgeLine, line);

            // are we inside poly2 ?
            if (inner)
            {
                // add intersection to poly2
                poly2.addPoint(intersection);
                // add intersection and pt2 to poly1
                poly1.addPoint(intersection);
                poly1.addPoint(edgePt2);
            }
            else
            {
                // add intersection to poly1
                poly1.addPoint(intersection);
                // add intersection and pt2 to poly2
                poly2.addPoint(intersection);
                poly2.addPoint(edgePt2);
            }

            // we changed region
            return !inner;
        }

        // inside poly2 --> add point to poly2
        if (inner)
            poly2.addPoint(edgePt2);
        else
            poly1.addPoint(edgePt2);

        // same region
        return inner;
    }

    /**
     * Cut the specified ROI with the given Line2D (extended to ROI bounds) and return the 2 resulting ROI in a
     * list.<br>
     * If the specified ROI cannot be cut by the given Line2D then <code>null</code> is returned.
     * 
     * @throws InterruptedException
     * @throws UnsupportedOperationException
     */
    public static List<ROI> split(ROI roi, Line2D line) throws UnsupportedOperationException, InterruptedException
    {
        final Rectangle2D bounds2d = roi.getBounds5D().toRectangle2D();
        // need to enlarge bounds a bit to avoid roundness issues on line intersection
        final Rectangle2D extendedBounds2d = Rectangle2DUtil.getScaledRectangle(bounds2d, 1.1d, true);
        // enlarge line to ROI bounds
        final Line2D extendedLine = Rectangle2DUtil.getIntersectionLine(extendedBounds2d, line);

        // if the extended line intersects the ROI bounds
        if ((extendedLine != null) && bounds2d.intersectsLine(extendedLine))
        {
            final List<ROI> result = new ArrayList<ROI>();
            final Point2D topLeft = new Point2D.Double(bounds2d.getMinX(), bounds2d.getMinY());
            final Point2D topRight = new Point2D.Double(bounds2d.getMaxX(), bounds2d.getMinY());
            final Point2D bottomRight = new Point2D.Double(bounds2d.getMaxX(), bounds2d.getMaxY());
            final Point2D bottomLeft = new Point2D.Double(bounds2d.getMinX(), bounds2d.getMaxY());
            final Polygon2D poly1 = new Polygon2D();
            final Polygon2D poly2 = new Polygon2D();
            boolean inner;

            // add first point to poly1
            poly1.addPoint(topLeft);
            // we are inside poly1 for now
            inner = false;

            // compute the 2 rectangle part (polygon) from top, right, bottom and left lines
            inner = computePolysFromLine(extendedLine, topLeft, topRight, poly1, poly2, inner);
            inner = computePolysFromLine(extendedLine, topRight, bottomRight, poly1, poly2, inner);
            inner = computePolysFromLine(extendedLine, bottomRight, bottomLeft, poly1, poly2, inner);
            inner = computePolysFromLine(extendedLine, bottomLeft, topLeft, poly1, poly2, inner);

            // get intersection result from both polygon
            final ROI roiPart1 = new ROI2DPolygon(poly1).getIntersection(roi);
            final ROI roiPart2 = new ROI2DPolygon(poly2).getIntersection(roi);

            // keep original ROI informations
            roiPart1.setName(roi.getName() + PART_SUFFIX + " #1");
            copyROIProperties(roi, roiPart1, false);
            roiPart2.setName(roi.getName() + PART_SUFFIX + " #2");
            copyROIProperties(roi, roiPart2, false);

            // add to result list
            result.add(roiPart1);
            result.add(roiPart2);

            return result;
        }

        return null;
    }

    /**
     * Convert a list of ROI into a binary / labeled Sequence.
     * 
     * @param inputRois
     *        list of ROI to convert
     * @param sizeX
     *        the wanted size X of output Sequence, if set to <code>0</code> then Sequence size X is computed
     *        automatically from
     *        the global ROI bounds.
     * @param sizeY
     *        the wanted size Y of output Sequence, if set to <code>0</code> then Sequence size Y is computed
     *        automatically from
     *        the global ROI bounds.
     * @param sizeC
     *        the wanted size C of output Sequence, if set to <code>0</code> then Sequence size C is computed
     *        automatically from
     *        the global ROI bounds.
     * @param sizeZ
     *        the wanted size Z of output Sequence, if set to <code>0</code> then Sequence size Z is computed
     *        automatically from
     *        the global ROI bounds.
     * @param sizeT
     *        the wanted size T of output Sequence, if set to <code>0</code> then Sequence size T is computed
     *        automatically from
     *        the global ROI bounds.
     * @param dataType
     *        the wanted dataType of output Sequence
     * @param label
     *        if set to <code>true</code> then each ROI will be draw as a separate label (value) in the sequence
     *        starting from 1.
     * @throws InterruptedException
     */
    public static Sequence convertToSequence(List<ROI> inputRois, int sizeX, int sizeY, int sizeC, int sizeZ, int sizeT,
            DataType dataType, boolean label) throws InterruptedException
    {
        final List<ROI> rois = new ArrayList<ROI>();
        final Rectangle5D bounds = new Rectangle5D.Double();
        // we cannot merge ROI for labeled sequence
        boolean canMerge = !label;

        // can merge ROI ? (faster)
        if (canMerge)
        {
            try
            {
                // compute the union of all ROI
                final ROI roi = ROIUtil.merge(inputRois, BooleanOperator.OR);
                // get bounds of result
                bounds.add(roi.getBounds5D());
                // add this single ROI to list
                rois.add(roi);
            }
            catch (Exception e)
            {
                // merge failed
                canMerge = false;
            }
        }

        if (!canMerge)
        {
            for (ROI roi : inputRois)
            {
                // compute global bounds
                if (roi != null)
                {
                    bounds.add(roi.getBounds5D());
                    rois.add(roi);
                }
            }
        }

        int sX = sizeX;
        int sY = sizeY;
        int sC = sizeC;
        int sZ = sizeZ;
        int sT = sizeT;

        if (sX == 0)
            sX = (int) bounds.getSizeX();
        if (sY == 0)
            sY = (int) bounds.getSizeY();
        if (sC == 0)
            sC = (bounds.isInfiniteC() ? 1 : (int) bounds.getSizeC());
        if (sZ == 0)
            sZ = (bounds.isInfiniteZ() ? 1 : (int) bounds.getSizeZ());
        if (sT == 0)
            sT = (bounds.isInfiniteT() ? 1 : (int) bounds.getSizeT());

        // empty base dimension and empty result --> generate a empty 320x240 image
        if (sX == 0)
            sX = 320;
        if (sY == 0)
            sY = 240;
        if (sC == 0)
            sC = 1;
        if (sZ == 0)
            sZ = 1;
        if (sT == 0)
            sT = 1;

        final Sequence out = new Sequence("ROI conversion");

        out.beginUpdate();
        try
        {
            for (int t = 0; t < sT; t++)
                for (int z = 0; z < sZ; z++)
                    out.setImage(t, z, new IcyBufferedImage(sX, sY, sC, dataType));

            double fillValue = 1d;

            // set value from ROI(s)
            for (ROI roi : rois)
            {
                if (!roi.getBounds5D().isEmpty())
                    DataIteratorUtil.set(new SequenceDataIterator(out, roi), fillValue);

                if (label)
                    fillValue += 1d;
            }

            // notify data changed
            out.dataChanged();
        }
        finally
        {
            out.endUpdate();
        }

        return out;
    }

    /**
     * Convert a list of ROI into a binary / labeled Sequence.
     * 
     * @param inputRois
     *        list of ROI to convert
     * @param sequence
     *        the sequence used to define the wanted sequence dimension in return.<br>
     *        If this field is <code>null</code> then the global ROI bounds will be used to define the Sequence
     *        dimension
     * @param label
     *        if set to <code>true</code> then each ROI will be draw as a separate label (value) in the sequence
     *        starting from 1.
     * @throws InterruptedException
     */
    public static Sequence convertToSequence(List<ROI> inputRois, Sequence sequence, boolean label)
            throws InterruptedException
    {
        if (sequence == null)
            return convertToSequence(inputRois, 0, 0, 0, 0, 0,
                    label ? ((inputRois.size() > 255) ? DataType.USHORT : DataType.UBYTE) : DataType.UBYTE, label);

        return convertToSequence(inputRois, sequence.getSizeX(), sequence.getSizeY(), 1, sequence.getSizeZ(),
                sequence.getSizeT(), sequence.getDataType_(), label);
    }

    /**
     * Convert a single ROI into a binary / labeled Sequence.
     * 
     * @param inputRoi
     *        ROI to convert
     * @param sequence
     *        the sequence used to define the wanted sequence dimension in return.<br>
     *        If this field is <code>null</code> then the global ROI bounds will be used to define the Sequence
     *        dimension
     * @throws InterruptedException
     */
    public static Sequence convertToSequence(ROI inputRoi, Sequence sequence) throws InterruptedException
    {
        return convertToSequence(CollectionUtil.createArrayList(inputRoi), sequence, false);
    }

    /**
     * Scale (3D) the given ROI by specified X,Y,Z factors.<br>
     * Only {@link ROI2DShape} and {@link ROI3DShape} are supported !
     * 
     * @param roi
     *        input ROI we want to rescale
     * @throws UnsupportedOperationException
     *         if input ROI is not ROI2DShape or ROI3DShape (scaling supported only for these ROI)
     */
    public static void scale(ROI roi, double scaleX, double scaleY, double scaleZ) throws UnsupportedOperationException
    {
        // shape ROI --> can rescale easily
        if (roi instanceof ROI2DRectShape)
        {
            final ROI2DRectShape roi2DRectShape = (ROI2DRectShape) roi;

            roi2DRectShape.beginUpdate();
            try
            {
                final Rectangle2D bounds = roi2DRectShape.getBounds2D();

                // reshape directly
                bounds.setFrame(bounds.getX() * scaleX, bounds.getY() * scaleY, bounds.getWidth() * scaleX,
                        bounds.getHeight() * scaleY);
                roi2DRectShape.setBounds2D(bounds);

                final int z = roi2DRectShape.getZ();

                // re scale Z position if needed
                if ((z != -1) && (scaleZ != 1d))
                    roi2DRectShape.setZ((int) (z * scaleZ));
            }
            finally
            {
                roi2DRectShape.endUpdate();
            }
        }
        else if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roi2DShape = (ROI2DShape) roi;

            roi2DShape.beginUpdate();
            try
            {
                // adjust control point position directly
                for (Anchor2D pt : roi2DShape.getControlPoints())
                {
                    final Point2D pos = pt.getPosition();
                    // change control point position
                    pt.setPosition(pos.getX() * scaleX, pos.getY() * scaleY);
                }

                final int z = roi2DShape.getZ();

                // re scale Z position if needed
                if ((z != -1) && (scaleZ != 1d))
                    roi2DShape.setZ((int) (z * scaleZ));
            }
            finally
            {
                roi2DShape.endUpdate();
            }
        }
        else if (roi instanceof ROI3DShape)
        {
            final ROI3DShape roi3DShape = (ROI3DShape) roi;

            roi3DShape.beginUpdate();
            try
            {
                // adjust control point position directly
                for (Anchor3D pt : roi3DShape.getControlPoints())
                {
                    final Point3D pos = pt.getPosition();
                    // change control point position
                    pt.setPosition(pos.getX() * scaleX, pos.getY() * scaleY, pos.getZ() * scaleZ);
                }
            }
            finally
            {
                roi3DShape.endUpdate();
            }
        }
        else
            throw new UnsupportedOperationException("ROIUtil.scale: cannot rescale " + roi.getSimpleClassName() + " !");
    }

    /**
     * Scale (2D) the given ROI by specified X/Y factor.<br>
     * Only {@link ROI2DShape} and {@link ROI3DShape} are supported !
     * 
     * @param roi
     *        input ROI we want to rescale
     * @throws UnsupportedOperationException
     *         if input ROI is not ROI2DShape or ROI3DShape (scaling supported only for these ROI)
     */
    public static void scale(ROI roi, double scaleX, double scaleY) throws UnsupportedOperationException
    {
        scale(roi, scaleX, scaleY, 1d);
    }

    /**
     * Scale the given ROI by specified scale factor.<br>
     * Only {@link ROI2DShape} and {@link ROI3DShape} are supported !
     * 
     * @param roi
     *        input ROI we want to rescale
     * @throws UnsupportedOperationException
     *         if input ROI is not ROI2DShape or ROI3DShape (scaling supported only for these ROI)
     */
    public static void scale(ROI roi, double scale) throws UnsupportedOperationException
    {
        scale(roi, scale, scale, scale);
    }

    /**
     * Create and returns a new ROI which is a 2x up/down scaled version of the input ROI.<br>
     * Note that the returned ROI can be ROI2DArea or ROI3DArea if original ROI format doesn't support 2X scale
     * operation.
     * 
     * @param roi
     *        input ROI we want to get the up scaled form
     * @param scaleOnZ
     *        Set to <code>true</code> to scale as well on Z dimension (XY dimension only otherwise)
     * @param down
     *        Set to <code>true</code> for down scaling and <code>false</code> for up scaling operation
     * @throws UnsupportedOperationException
     *         if input ROI is ROI4D or ROI5D (up scaling not supported for these ROI)
     * @throws InterruptedException
     */
    public static ROI get2XScaled(ROI roi, boolean scaleOnZ, boolean down)
            throws UnsupportedOperationException, InterruptedException
    {
        if (roi == null)
            return null;

        final double scaling = down ? 0.5d : 2d;
        ROI result = roi.getCopy();

        // shape ROI --> can rescale easily
        if ((result instanceof ROI2DShape) || (result instanceof ROI3DShape))
            scale(result, scaling, scaling, scaleOnZ ? scaling : 1d);
        else if (result instanceof ROI2D)
        {
            final ROI2DArea roi2DArea;

            if (result instanceof ROI2DArea)
            {
                roi2DArea = (ROI2DArea) result;

                // scale
                if (down)
                    roi2DArea.downscale();
                else
                    roi2DArea.upscale();

                // scale Z position if wanted
                if ((roi2DArea.getZ() != -1) && scaleOnZ)
                    roi2DArea.setZ((int) (roi2DArea.getZ() * scaling));
            }
            else
            {
                final BooleanMask2D bm = ((ROI2D) result).getBooleanMask(true);

                // scale
                if (down)
                    roi2DArea = new ROI2DArea(bm.downscale());
                else
                    roi2DArea = new ROI2DArea(bm.upscale());

                // get original position
                final Point5D pos = result.getPosition5D();

                // restore Z,T,C position
                if (Double.isInfinite(pos.getZ()))
                    roi2DArea.setZ(-1);
                else
                    roi2DArea.setZ((int) (pos.getZ() * (scaleOnZ ? scaling : 1d)));
                if (Double.isInfinite(pos.getT()))
                    roi2DArea.setT(-1);
                else
                    roi2DArea.setT((int) pos.getT());
                if (Double.isInfinite(pos.getC()))
                    roi2DArea.setC(-1);
                else
                    roi2DArea.setC((int) pos.getC());

                // copy properties
                copyROIProperties(result, roi2DArea, true);

                result = roi2DArea;
            }
        }
        else if (result instanceof ROI3D)
        {
            final ROI3DArea roi3DArea;

            // we want a ROI2DArea
            if (result instanceof ROI3DArea)
            {
                roi3DArea = (ROI3DArea) result;

                // scale
                if (down)
                {
                    if (scaleOnZ)
                        roi3DArea.downscale();
                    else
                        roi3DArea.downscale2D();
                }
                else
                {
                    if (scaleOnZ)
                        roi3DArea.upscale();
                    else
                        roi3DArea.upscale2D();
                }
            }
            else
            {
                final BooleanMask3D bm = ((ROI3D) result).getBooleanMask(true);

                // scale
                if (down)
                {
                    if (scaleOnZ)
                        roi3DArea = new ROI3DArea(bm.downscale());
                    else
                        roi3DArea = new ROI3DArea(bm.downscale2D());
                }
                else
                {
                    if (scaleOnZ)
                        roi3DArea = new ROI3DArea(bm.upscale());
                    else
                        roi3DArea = new ROI3DArea(bm.upscale2D());
                }

                // get original position
                final Point5D pos = result.getPosition5D();

                // restore T,C position
                if (Double.isInfinite(pos.getT()))
                    roi3DArea.setT(-1);
                else
                    roi3DArea.setT((int) pos.getT());
                if (Double.isInfinite(pos.getC()))
                    roi3DArea.setC(-1);
                else
                    roi3DArea.setC((int) pos.getC());

                // copy properties
                copyROIProperties(result, roi3DArea, true);

                result = roi3DArea;
            }
        }
        // 4D or 5D ROI --> scaling not supported
        else
            throw new UnsupportedOperationException("ROIUtil.get2XScaled: cannot rescale ROI4D or ROI5D !");

        return result;
    }

    /**
     * Create and returns a new ROI which is a 2x up scaled version of the input ROI.<br>
     * Note that the returned ROI can be ROI2DArea or ROI3DArea if original ROI format doesn't support up scale
     * operation.
     * 
     * @param roi
     *        input ROI we want to get the up scaled form
     * @param scaleOnZ
     *        Set to <code>true</code> to scale as well on Z dimension (XY dimension only otherwise)
     * @throws UnsupportedOperationException
     *         if input ROI is ROI4D or ROI5D (up scaling not supported for these ROI)
     * @throws InterruptedException
     */
    public static ROI getUpscaled(ROI roi, boolean scaleOnZ) throws UnsupportedOperationException, InterruptedException
    {
        return get2XScaled(roi, scaleOnZ, false);
    }

    /**
     * Create and returns a new ROI which is a 2x down scaled version of the input ROI.<br>
     * Note that the returned ROI can be ROI2DArea or ROI3DArea if original ROI format doesn't support up scale
     * operation.
     * 
     * @param roi
     *        input ROI we want to get the up scaled form
     * @param scaleOnZ
     *        Set to <code>true</code> to scale as well on Z dimension (XY dimension only otherwise)
     * @throws UnsupportedOperationException
     *         if input ROI is ROI4D or ROI5D (up scaling not supported for these ROI)
     * @throws InterruptedException
     */
    public static ROI getDownscaled(ROI roi, boolean scaleOnZ)
            throws UnsupportedOperationException, InterruptedException
    {
        return get2XScaled(roi, scaleOnZ, true);
    }

    /**
     * Create a copy of the specified ROI coming from <i>source</i> sequence adjusted to the <i>destination</i> sequence.<br>
     * The resulting ROI coordinates can be different if the {@link Sequence#getPosition()} are not identical on the 2 sequences.<br>
     * The resulting ROI can be scaled if the {@link Sequence#getPixelSize()} are not identical on the 2 sequences<br>
     * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format, also the scale operation may not be possible depending
     * the original ROI format.
     * 
     * @param roi
     *        input ROI we want to adjust
     * @param source
     *        the source sequence where the ROI was initially generated (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param destination
     *        the destination sequence where we want to copy the ROI (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param translate
     *        if we allow the returned ROI to be translated compared to the original ROI
     * @param scale
     *        if we allow the returned ROI to be scaled compared to the original ROI
     * @param ignoreErrorOnScale
     *        ignore unsupported scaling operation, in which case the result ROI won't be correctly scaled
     * @return adjusted ROI
     * @throws UnsupportedOperationException
     *         if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI) and <code>ignoreErrorOnScale</code> is set to
     *         <code>FALSE</code>
     * @throws InterruptedException
     */
    public static ROI adjustToSequence(ROI roi, Sequence source, Sequence destination, boolean translate, boolean scale,
            boolean ignoreErrorOnScale) throws UnsupportedOperationException, InterruptedException
    {
        if (roi == null)
            return null;

        // create a copy
        ROI result = roi.getCopy();

        if (scale)
        {
            final double scaleX = source.getPixelSizeX() / destination.getPixelSizeX();
            final double scaleY = source.getPixelSizeY() / destination.getPixelSizeY();
            final double scaleZ = source.getPixelSizeZ() / destination.getPixelSizeZ();

            // ROI is a 2D or 3D shape ? --> use easy scaling
            if ((result instanceof ROI2DShape) || (result instanceof ROI3DShape))
                scale(result, scaleX, scaleY, scaleZ);
            else
            {
                boolean doRescale = true;
                boolean doRescaleZ = true;

                if (MathUtil.round(scaleX / scaleY, 3) != 1d)
                {
                    doRescale = false;
                    if (ignoreErrorOnScale)
                        System.out.println(
                                "[Warning] ROIUtil.adjustToSequence: cannot rescale ROI with different X/Y scale ratio.");
                    else
                        throw new UnsupportedOperationException(
                                "ROIUtil.adjustToSequence: cannot rescale ROI (different X/Y scale ratio) !");
                }

                // get log2 of scaleX/Y (round it a bit)
                final double resDelta = MathUtil.round(Math.log(scaleX) / Math.log(2), 1);

                // too far from 2^x scaling
                if (Math.round(resDelta) != resDelta)
                {
                    doRescale = false;
                    if (ignoreErrorOnScale)
                        System.out.println(
                                "[Warning] ROIUtil.adjustToSequence: cannot rescale ROI with scale XY = " + scaleX);
                    else
                        throw new UnsupportedOperationException(
                                "ROIUtil.adjustToSequence: cannot rescale ROI (scale XY = " + scaleX + ") !");
                }

                // get log2 of scaleZ (round it a bit)
                final double resDeltaZ = MathUtil.round(Math.log(scaleZ) / Math.log(2), 1);

                if (Math.round(resDeltaZ) != resDeltaZ)
                {
                    doRescaleZ = false;
                    if (ignoreErrorOnScale)
                        System.out.println("[Warning] ROIUtil.adjustToSequence: ignoring ROI Z rescaling (scale Z = "
                                + scaleZ + ")");
                    else
                        throw new UnsupportedOperationException(
                                "ROIUtil.adjustToSequence: cannot rescale ROI (scale Z = " + scaleZ + ") !");
                }

                final boolean zScaling = resDeltaZ != 0d;

                // Z rescaling needed ? --> we need to have same XY and Z scale ratio
                if (zScaling && (MathUtil.round(resDeltaZ / resDelta, 3) != 1d))
                {
                    doRescaleZ = false;
                    if (ignoreErrorOnScale)
                        System.out.println("[Warning] ROIUtil.adjustToSequence: ignoring ROI Z rescaling (scale XY = "
                                + scaleX + " while scale Z = " + scaleZ + ")");
                    else
                        throw new UnsupportedOperationException(
                                "ROIUtil.adjustToSequence: cannot rescale ROI (scale XY = " + scaleX
                                        + " while scale Z = " + scaleZ + ") !");
                }

                try
                {
                    if (doRescale)
                    {
                        int i = (int) resDelta;

                        // destination resolution level > source resolution level
                        if (resDelta > 0)
                        {
                            // down scaling
                            while (i-- > 0)
                                result = getUpscaled(result, zScaling && doRescaleZ);
                        }
                        else
                        {
                            // up scaling
                            while (i++ < 0)
                                result = getDownscaled(result, zScaling && doRescaleZ);
                        }
                    }
                }
                catch (UnsupportedOperationException e)
                {
                    // we should propagate it then
                    if (!ignoreErrorOnScale)
                        throw e;
                }
            }
        }

        // can set position ? --> relocate it
        if (translate && result.canSetPosition())
        {
            // get current position
            final Point5D pos = result.getPosition5D();

            // can change it Z position ?
            if (!Double.isInfinite(pos.getZ()))
            {
                // we just want to get offset as scaling already taken care of converting relative ROI position
                final Point3D offset = SequenceUtil.convertPoint(new Point3D.Double(), source, destination);

                // compute position in destination
                pos.setX(pos.getX() + offset.getX());
                pos.setY(pos.getY() + offset.getY());
                pos.setZ(pos.getZ() + offset.getZ());
            }
            else
            {
                // we just want to get offset as scaling already taken care of converting relative ROI position
                final Point2D offset = SequenceUtil.convertPoint(new Point2D.Double(), source, destination);

                // compute position in destination
                pos.setX(pos.getX() + offset.getX());
                pos.setY(pos.getY() + offset.getY());
            }

            // can change it ? (we don't scale T dimension)
            if (!Double.isInfinite(pos.getT()))
            {
                // get time interval in ms
                final double timeIntervalMs = destination.getTimeInterval() * 1000d;

                if (timeIntervalMs > 0d)
                {
                    // get delta in timestamp (ms)
                    final double deltaT = source.getPositionT() - destination.getPositionT();
                    // get wanted destination T offset
                    final double tOffset = deltaT / timeIntervalMs;

                    // assume correct T range is ~1000
                    if ((tOffset > -1000d) && (tOffset < 1000d))
                        pos.setT(Math.min(999, Math.max(0, pos.getT() + tOffset)));
                }
            }

            // set back position
            result.setPosition5D(pos);
        }

        return result;
    }

    /**
     * Create a copy of the specified ROI coming from <i>source</i> sequence adjusted to the <i>destination</i> sequence.<br>
     * The resulting ROI coordinates can be different if the {@link Sequence#getPosition()} are not identical on the 2 sequences.<br>
     * The resulting ROI can be scaled if the {@link Sequence#getPixelSize()} are not identical on the 2 sequences<br>
     * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format, also the scale operation may not be possible depending
     * the original ROI format.
     * 
     * @param roi
     *        input ROI we want to adjust
     * @param source
     *        the source sequence where the ROI was initially generated (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param destination
     *        the destination sequence where we want to copy the ROI (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param translate
     *        if we allow the returned ROI to be translated compared to the original ROI
     * @param scale
     *        if we allow the returned ROI to be scaled compared to the original ROI
     * @return adjusted ROI
     * @throws UnsupportedOperationException
     *         if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI) and <code>ignoreErrorOnScale</code> is set to
     *         <code>FALSE</code>
     * @throws InterruptedException
     */
    public static ROI adjustToSequence(ROI roi, Sequence source, Sequence destination, boolean translate, boolean scale)
            throws UnsupportedOperationException, InterruptedException
    {
        return adjustToSequence(roi, source, destination, translate, scale, false);
    }

    /**
     * Create a copy of the specified ROI coming from <i>source</i> sequence adjusted to the <i>destination</i> sequence.<br>
     * The resulting ROI coordinates can be different if the {@link Sequence#getPosition()} are not identical on the 2 sequences.<br>
     * The resulting ROI can be scaled if the {@link Sequence#getPixelSize()} are not identical on the 2 sequences<br>
     * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format, also the scale operation may not be possible depending
     * the original ROI format.
     * 
     * @param roi
     *        input ROI we want to adjust
     * @param source
     *        the source sequence where the ROI was initially generated (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param destination
     *        the destination sequence where we want to copy the ROI (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @return adjusted ROI
     * @throws UnsupportedOperationException
     *         if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI)
     * @throws InterruptedException
     */
    public static ROI adjustToSequence(ROI roi, Sequence source, Sequence destination)
            throws UnsupportedOperationException, InterruptedException
    {
        return adjustToSequence(roi, source, destination, true, true);
    }

    // /**
    // * Create and returns a new ROI adjusted to the specified sequence if it represents a sub region of another
    // * Sequence.<br>
    // * You need to use this function when you generated the ROI on the origin Sequence and want to have it adjusted to
    // * the given sub region Sequence coordinates.<br>
    // * ROI coordinates can be affected if the {@link Sequence#getOriginXYRegion()} is not <code>null</code>.<br>
    // * If {@link Sequence#getOriginResolution()} is not <code>0</code> then the returned ROI will be down scaled to fit
    // * the Sequence image resolution.<br>
    // * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format..
    // *
    // * @param roi
    // * input ROI we want to get the adjusted form
    // * @param subSequence
    // * the sequence representing the sub region of the origin Sequence (it should contains <i>origin</i>
    // * information, see Sequence#getOriginXXX(} methods)
    // * @return adjusted ROI
    // * @throws UnsupportedOperationException
    // * if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI)
    // */
    // public static ROI adjustToSequence(ROI roi, Sequence subSequence) throws UnsupportedOperationException
    // {
    // if (roi == null)
    // return null;
    //
    // ROI result = roi.getCopy();
    //
    // if (subSequence != null)
    // {
    // int res = subSequence.getOriginResolution();
    // final double scaleFactor = 1d / Math.pow(2d, res);
    //
    // // down scaling
    // while (res-- > 0)
    // result = getDownscaled(result, false);
    //
    // // can set position ? --> relocate it
    // if (result.canSetPosition())
    // {
    // // get current position
    // final Point5D pos = result.getPosition5D();
    //
    // final Rectangle originPos = subSequence.getOriginXYRegion();
    // // sub region ?
    // if (originPos != null)
    // {
    // pos.setX(pos.getX() - (originPos.getX() * scaleFactor));
    // pos.setY(pos.getY() - (originPos.getY() * scaleFactor));
    // }
    //
    // final int zMin = subSequence.getOriginZMin();
    // // sub Z stack part ?
    // if (zMin != -1)
    // {
    // // can change it ? (we don't scale Z dimension)
    // if (!Double.isInfinite(pos.getZ()))
    // pos.setZ(pos.getZ() - zMin);
    // }
    //
    // final int tMin = subSequence.getOriginTMin();
    // // sub T sequence part ?
    // if (tMin != -1)
    // {
    // // can change it ? (we don't scale T dimension)
    // if (!Double.isInfinite(pos.getT()))
    // pos.setT(pos.getT() - tMin);
    // }
    //
    // final int c = subSequence.getOriginChannel();
    // // sub channel ?
    // if (c != -1)
    // {
    // // can change it ? (we don't scale C dimension)
    // if (!Double.isInfinite(pos.getC()))
    // pos.setC(pos.getC() - c);
    // }
    //
    // // set back position
    // result.setPosition5D(pos);
    // }
    // }
    //
    // return result;
    // }
    //
    // /**
    // * Create and returns a new ROI adjusted to the origin sequence given the sub region Sequence.<br>
    // * You need to use this function when you generated the ROI on the given sub region Sequence and want to have it
    // * back in the origin Sequence coordinates.<br>
    // * ROI coordinates can be affected if the {@link Sequence#getOriginXYRegion()} is not <code>null</code>.<br>
    // * If {@link Sequence#getOriginResolution()} is not <code>0</code> then the returned ROI will be up scaled to fit
    // * the original image resolution.<br>
    // * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format.
    // *
    // * @param roi
    // * input ROI we want to get the adjusted form
    // * @param subSequence
    // * the sequence representing the sub region of the origin Sequence (it should contains <i>origin</i>
    // * information, see Sequence#getOriginXXX(} methods)
    // * @return adjusted ROI
    // * @throws UnsupportedOperationException
    // * if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI)
    // */
    // public static ROI adjustToOriginSequence(ROI roi, Sequence subSequence) throws UnsupportedOperationException
    // {
    // if (roi == null)
    // return null;
    //
    // ROI result = roi.getCopy();
    //
    // if (subSequence != null)
    // {
    // int res = subSequence.getOriginResolution();
    //
    // // up scaling (2D)
    // while (res-- > 0)
    // result = getUpscaled(result, false);
    //
    // // can set position ? --> relocate it
    // if (result.canSetPosition())
    // {
    // // get current position
    // final Point5D pos = result.getPosition5D();
    //
    // final Rectangle originPos = subSequence.getOriginXYRegion();
    // // sub region ?
    // if (originPos != null)
    // {
    // pos.setX(pos.getX() + originPos.getX());
    // pos.setY(pos.getY() + originPos.getY());
    // }
    //
    // final int zMin = subSequence.getOriginZMin();
    // // sub Z stack part ?
    // if (zMin != -1)
    // {
    // // can change it ?
    // if (!Double.isInfinite(pos.getZ()))
    // pos.setZ(pos.getZ() + zMin);
    // }
    //
    // final int tMin = subSequence.getOriginTMin();
    // // sub T sequence part ?
    // if (tMin != -1)
    // {
    // // can change it ?
    // if (!Double.isInfinite(pos.getT()))
    // pos.setT(pos.getT() + tMin);
    // }
    //
    // final int c = subSequence.getOriginChannel();
    // // sub channel ?
    // if (c != -1)
    // {
    // // can change it ?
    // if (!Double.isInfinite(pos.getC()))
    // pos.setC(pos.getC() + c);
    // }
    //
    // // set back position
    // result.setPosition5D(pos);
    // }
    // }
    //
    // return result;
    // }

    /**
     * Copy properties (name, color...) from <code>source</code> ROI and apply it to <code>destination</code> ROI.
     */
    public static void copyROIProperties(ROI source, ROI destination, boolean copyName)
    {
        if ((source == null) || (destination == null))
            return;

        if (copyName)
            destination.setName(source.getName());
        destination.setColor(source.getColor());
        destination.setOpacity(source.getOpacity());
        destination.setStroke(source.getStroke());
        destination.setReadOnly(source.isReadOnly());
        destination.setSelected(source.isSelected());
        destination.setShowName(source.getShowName());
        // destination.setGroupId(source.getGroupId());

        // copy extended properties
        for (Entry<String, String> propertyEntry : source.getProperties().entrySet())
            destination.setProperty(propertyEntry.getKey(), propertyEntry.getValue());
    }

    public static Sequence computeDistanceMap(ROI roi, Dimension5D imageSize, Dimension3D pixelSize,
            boolean constrainBorders) throws InterruptedException
    {
        ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, constrainBorders);
        dt.addROI(roi);
        return dt.getDistanceMap();
    }

    public static Sequence computeDistanceMap(Collection<? extends ROI> selectedROIs, Dimension5D imageSize,
            Dimension3D pixelSize, boolean constrainBorders) throws InterruptedException
    {
        ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, constrainBorders);
        dt.addAll(selectedROIs);
        return dt.getDistanceMap();
    }

    /**
     * @param selectedRois
     *        ROIs that need to be separated. Note: These ROIs will be joined before watersheding.
     * @param seedRois
     *        Seed points used to initialize watershed.
     * @param imageSize
     *        Size of the image where the ROIs are located.
     * @param pixelSize
     *        Physical size of the pixels in the image.
     * @return List of separated ROIs.
     * @throws InterruptedException
     *         If the process gets interrupted.
     */
    public static List<ROI> computeWatershedSeparation(Collection<? extends ROI> selectedRois,
            List<? extends ROI> seedRois, Dimension5D imageSize, Dimension3D pixelSize) throws InterruptedException
    {
        ROIWatershedCalculator.Builder wsBuilder = new ROIWatershedCalculator.Builder(imageSize, pixelSize);

        wsBuilder.addObjects(selectedRois);
        wsBuilder.addSeeds(seedRois);
        wsBuilder.setNewBasinsAllowed(false);
        ROIWatershedCalculator wsCalculator = wsBuilder.build();

        try
        {
            wsCalculator.call();
        }
        catch (InterruptedException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error computing watershed: " + e.getMessage(), e);
        }

        return wsCalculator.getLabelRois();
    }

    /**
     * @param selectedRois
     * @param imageSize
     * @param pixelSize
     * @param usedSeedRois
     *        collection where detected seeds will be stored.
     * @return
     * @throws InterruptedException
     */
    public static List<ROI> computeWatershedSeparation(Collection<? extends ROI> selectedRois, Dimension5D imageSize,
            Dimension3D pixelSize, List<ROI> usedSeedRois) throws InterruptedException
    {
        ROIWatershedCalculator.Builder wsBuilder = new ROIWatershedCalculator.Builder(imageSize, pixelSize);

        wsBuilder.addObjects(selectedRois);
        wsBuilder.addSeeds(usedSeedRois);
        wsBuilder.setNewBasinsAllowed(false);
        ROIWatershedCalculator wsCalculator = wsBuilder.build();

        try
        {
            wsCalculator.call();
        }
        catch (InterruptedException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error computing watershed: " + e.getMessage(), e);
        }

        usedSeedRois.clear();
        usedSeedRois.addAll(wsCalculator.getSeeds());
        return wsCalculator.getLabelRois();
    }

    public static List<ROI> computeSkeleton(List<ROI2D> selectedROIs, Dimension3D pixelSize, double distance)
            throws InterruptedException
    {
        List<ROI> result = new ArrayList<ROI>();
        for (ROI roi : selectedROIs)
        {
            if (roi.getBounds5D().getSizeX() == 0)
                continue;

            Point5D oldPosition = new Point5D.Double();
            oldPosition.setLocation(roi.getPosition5D());
            roi.setPosition5D(new Point5D.Double());
            try
            {
                ROISkeletonCalculator skeletonizer = new ROISkeletonCalculator(roi, pixelSize);
                ROI skeletonRoi = skeletonizer.getSkeletonROI();
                if (skeletonRoi != null && skeletonRoi.getBounds5D().getSizeX() > 0)
                // TODO remove equals null
                {
                    Point5D skeletonPosition = skeletonRoi.getPosition5D();
                    skeletonPosition.setX(skeletonPosition.getX() + oldPosition.getX());
                    skeletonPosition.setY(skeletonPosition.getY() + oldPosition.getY());
                    skeletonPosition.setZ(skeletonPosition.getZ() + oldPosition.getZ());
                    skeletonRoi.setPosition5D(skeletonPosition);
                    result.add(skeletonRoi);
                }
            }
            finally
            {
                roi.setPosition5D(oldPosition);
            }
        }
        return result;
    }

    public static List<ROI> computeDilation(List<? extends ROI> selectedROIs, Dimension3D pixelSize, double distance)
            throws InterruptedException
    {
        List<ROI> result = new ArrayList<ROI>();
        for (ROI roi : selectedROIs)
        {
            if (roi.getBounds5D().getSizeX() == 0)
                continue;

            Rectangle5D oldBounds = new Rectangle5D.Double(roi.getBounds5D());
            Rectangle5D processingBounds = new Rectangle5D.Double(roi.getBounds5D());
            processingBounds.setX(0);
            processingBounds.setY(0);
            processingBounds.setZ(0);
            processingBounds.setC(0);
            processingBounds.setT(0);
            processingBounds.setSizeX(oldBounds.getSizeX());
            processingBounds.setSizeY(oldBounds.getSizeY());
            processingBounds.setSizeZ(1);
            processingBounds.setSizeC(1);
            processingBounds.setSizeT(1);
            if (roi.getBounds5D().getSizeZ() > 1 && Double.isFinite(roi.getBounds5D().getSizeZ()))
            {
                processingBounds.setZ(0);
                processingBounds.setSizeZ(oldBounds.getSizeZ());
            }
            roi.setBounds5D(processingBounds);
            if (roi instanceof ROI2DArea)
            {
                ((ROI2DArea) roi).setPosition2D(new Point2D.Double(0, 0));
            }

            try
            {
                ROIDilationCalculator dilator = new ROIDilationCalculator(roi, pixelSize, distance);
                ROI dilationRoi = dilator.getDilation();
                Rectangle5D dilationBounds = dilationRoi.getBounds5D();
                if (dilationBounds.getSizeX() > 0)
                {
                    Point5D dPos = dilationBounds.getPosition();
                    Point5D oPos = oldBounds.getPosition();
                    dilationBounds.setX(dPos.getX() + oPos.getX());
                    dilationBounds.setY(dPos.getY() + oPos.getY());
                    if (Double.isFinite(oldBounds.getSizeZ()))
                    {
                        dilationBounds.setZ(dPos.getZ() + oPos.getZ());
                        if (Double.isFinite(oldBounds.getSizeZ()) && Double.isInfinite(dilationBounds.getSizeZ()))
                        {
                            dilationBounds.setSizeZ(oldBounds.getSizeZ());
                        }
                    }

                    if (Double.isFinite(oldBounds.getSizeT()))
                    {
                        dilationBounds.setT(oPos.getT());
                        dilationBounds.setSizeT(oldBounds.getSizeT());
                    }

                    if (dilationRoi.canSetBounds())
                        dilationRoi.setBounds5D(dilationBounds);
                    else if (dilationRoi instanceof ROI2DArea)
                    {
                        ROI2DArea areaRoi = (ROI2DArea) dilationRoi;
                        areaRoi.setC(Double.isFinite(dilationBounds.getC()) ? (int) dilationBounds.getC() : -1);
                        areaRoi.setZ(Double.isFinite(dilationBounds.getZ()) ? (int) dilationBounds.getZ() : -1);
                        areaRoi.setT(Double.isFinite(dilationBounds.getT()) ? (int) dilationBounds.getT() : -1);
                        Rectangle2D bounds = areaRoi.getBounds2D();
                        areaRoi.translate(dilationBounds.getX() - bounds.getX(), dilationBounds.getY() - bounds.getY());
                    }

                }
                result.add(dilationRoi);

            }
            finally
            {
                roi.setBounds5D(oldBounds);
                if (roi instanceof ROI2DArea)
                {
                    ((ROI2DArea) roi).setPosition2D(new Point2D.Double(oldBounds.getX(), oldBounds.getY()));
                }
            }
        }
        return result;
    }

    public static List<ROI> computeErosion(List<? extends ROI> selectedROIs, Dimension3D pixelSize, double distance)
            throws InterruptedException
    {
        List<ROI> result = new ArrayList<ROI>();
        for (ROI roi : selectedROIs)
        {
            if (roi.getBounds5D().getSizeX() == 0)
                continue;

            Point5D oldPosition = new Point5D.Double();
            oldPosition.setLocation(roi.getPosition5D());
            roi.setPosition5D(new Point5D.Double());
            try
            {
                ROIErosionCalculator eroder = new ROIErosionCalculator(roi, pixelSize, distance);
                ROI erosionRoi = eroder.getErosion();
                if (erosionRoi.getBounds5D().getSizeX() > 0)
                {
                    Point5D erosionPosition = erosionRoi.getPosition5D();
                    erosionPosition.setX(erosionPosition.getX() + oldPosition.getX());
                    erosionPosition.setY(erosionPosition.getY() + oldPosition.getY());
                    erosionPosition.setZ(erosionPosition.getZ() + oldPosition.getZ());
                    erosionRoi.setPosition5D(erosionPosition);
                    result.add(erosionRoi);
                }
            }
            finally
            {
                roi.setPosition5D(oldPosition);
            }
        }
        return result;
    }
}
