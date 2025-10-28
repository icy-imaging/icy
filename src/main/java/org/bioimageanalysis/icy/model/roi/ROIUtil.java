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

package org.bioimageanalysis.icy.model.roi;

import org.bioimageanalysis.extension.kernel.roi.morphology.ROIDilationCalculator;
import org.bioimageanalysis.extension.kernel.roi.morphology.ROIDistanceTransformCalculator;
import org.bioimageanalysis.extension.kernel.roi.morphology.ROIErosionCalculator;
import org.bioimageanalysis.extension.kernel.roi.morphology.skeletonization.ROISkeletonCalculator;
import org.bioimageanalysis.extension.kernel.roi.roi2d.*;
import org.bioimageanalysis.extension.kernel.roi.roi3d.*;
import org.bioimageanalysis.icy.common.collection.CollectionUtil;
import org.bioimageanalysis.icy.common.geom.GeomUtil;
import org.bioimageanalysis.icy.common.geom.areax.AreaX;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension3D;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension5D;
import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.poly.Polygon2D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle2DUtil;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle4D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.icy.common.geom.shape.ShapeUtil.BooleanOperator;
import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.DataIteratorUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROIDescriptor;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor3D;
import org.bioimageanalysis.icy.model.roi.descriptor.IntensityDescriptorInfos;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2D;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask3D;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask4D;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask5D;
import org.bioimageanalysis.icy.model.roi.watershed.ROIWatershedCalculator;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceDataIterator;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * ROI utilities class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIUtil {
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
    public static Map<ROIDescriptor<?>, PluginROIDescriptor> getROIDescriptors() {
        return ROIDescriptor.getDescriptors();
    }

    /**
     * Computes the specified descriptor from the input {@link ROIDescriptor} set on given ROI
     * and returns the result (or <code>null</code> if the descriptor is not found).<br>
     * This method is an alias of {@link ROIDescriptor#computeDescriptor(Collection, String, ROI, Sequence)}
     *
     * @param roiDescriptors the input {@link ROIDescriptor} set (see {@link #getROIDescriptors()} method)
     * @param descriptorId   the id of the descriptor we want to compute
     * @param roi            the ROI on which the descriptor(s) should be computed
     * @param sequence       an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     * specified set
     * @throws UnsupportedOperationException if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *                                       <code>null</code> while the calculation requires it, or if
     *                                       the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(final Collection<ROIDescriptor<?>> roiDescriptors, final String descriptorId, final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return ROIDescriptor.computeDescriptor(roiDescriptors, descriptorId, roi, sequence);
    }

    /**
     * Computes the specified descriptor on given ROI and returns the result (or <code>null</code> if the descriptor is
     * not found).<br>
     * This method is an alias of {@link ROIDescriptor#computeDescriptor(String, ROI, Sequence)}
     *
     * @param descriptorId the id of the descriptor we want to compute
     * @param roi          the ROI on which the descriptor(s) should be computed
     * @param sequence     an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     * specified set
     * @throws UnsupportedOperationException if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *                                       <code>null</code> while the calculation requires it, or if
     *                                       the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(final String descriptorId, final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return ROIDescriptor.computeDescriptor(descriptorId, roi, sequence);
    }

    /**
     * Returns the number of sequence pixels contained in the specified ROI.
     *
     * @param sequence The sequence we want to get the number of pixel.
     * @param roi      The ROI define the region where we want to compute the number of pixel.
     * @param z        The specific Z position (slice) where we want to compute the number of pixel or <code>-1</code> to use the
     *                 ROI Z dimension information.
     * @param t        The specific T position (frame) where we want to compute the number of pixel or <code>-1</code> to use the
     *                 ROI T dimension information.
     * @param c        The specific C position (channel) where we want to compute the number of pixel or <code>-1</code> to use
     *                 the ROI C dimension information.
     */
    public static long getNumPixel(final Sequence sequence, final ROI roi, final int z, final int t, final int c) throws InterruptedException {
        return DataIteratorUtil.count(new SequenceDataIterator(sequence, roi, false, z, t, c));
    }

    /**
     * Returns the number of sequence pixels contained in the specified ROI.
     */
    public static long getNumPixel(final Sequence sequence, final ROI roi) throws InterruptedException {
        return getNumPixel(sequence, roi, -1, -1, -1);
    }

    /**
     * Returns the effective ROI number of dimension needed for the specified bounds.
     */
    public static int getEffectiveDimension(final Rectangle5D bounds) {
        int result = 5;

        if (bounds.isInfiniteC() || (bounds.getSizeC() <= 1d)) {
            result--;
            if (bounds.isInfiniteT() || (bounds.getSizeT() <= 1d)) {
                result--;
                if (bounds.isInfiniteZ() || (bounds.getSizeZ() <= 1d))
                    result--;
            }
        }

        return result;
    }

    /**
     * Calculate the multiplier factor depending the wanted dimension information.
     */
    public static double getMultiplierFactor(final Sequence sequence, final @NotNull ROI roi, final int dim) {
        final int dimRoi = roi.getDimension();

        // cannot give this information for this roi
        if (dimRoi > dim)
            return 0d;

        final Rectangle5D boundsRoi = roi.getBounds5D();
        double mul = 1d;

        switch (dim) {
            case 5:
                if (dimRoi == 4) {
                    final int sizeC = sequence.getSizeC();

                    if ((boundsRoi.getSizeC() == Double.POSITIVE_INFINITY) && (sizeC > 1))
                        mul *= sizeC;
                        // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 4:
                if (dimRoi == 3) {
                    final int sizeT = sequence.getSizeT();

                    if ((boundsRoi.getSizeT() == Double.POSITIVE_INFINITY) && (sizeT > 1))
                        mul *= sizeT;
                        // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 3:
                if (dimRoi == 2) {
                    final int sizeZ = sequence.getSizeZ();

                    if ((boundsRoi.getSizeZ() == Double.POSITIVE_INFINITY) && (sizeZ > 1))
                        mul *= sizeZ;
                        // cannot give this information for this roi
                    else
                        mul = 0d;
                }
            case 2:
                if (dimRoi == 1) {
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
     * Return 5D dimension for specified operation dimension
     */
    private static Dimension5D.Integer getOpDim(final int dim, final Rectangle5D.Integer bounds) {
        final Dimension5D.Integer result = new Dimension5D.Integer();

        switch (dim) {
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
    private static ROI getOpResult(final int dim, final BooleanMask5D mask, final Rectangle5D.Integer bounds) {
        final ROI result;

        switch (dim) {
            case 2: // XY ROI with fixed ZTC
                result = new ROI2DArea(mask.getMask2D(bounds.z, bounds.t, bounds.c));

                // set ZTC position
                result.beginUpdate();
                try {
                    ((ROI2D) result).setZ(bounds.z);
                    ((ROI2D) result).setT(bounds.t);
                    ((ROI2D) result).setC(bounds.c);
                }
                finally {
                    result.endUpdate();
                }
                break;

            case 3: // XYZ ROI with fixed TC
                result = new ROI3DArea(mask.getMask3D(bounds.t, bounds.c));

                // set TC position
                result.beginUpdate();
                try {
                    ((ROI3D) result).setT(bounds.t);
                    ((ROI3D) result).setC(bounds.c);
                }
                finally {
                    result.endUpdate();
                }
                break;

            // TODO remove this code once ROI4D and ROI5D completly removed
            /*case 4: // XYZT ROI with fixed C
                result = new ROI4DArea(mask.getMask4D(bounds.c));
                // set C position
                ((ROI4D) result).setC(bounds.c);
                break;

            case 5: // XYZTC ROI
                result = new ROI5DArea(mask);
                break;*/

            default:
                throw new UnsupportedOperationException("Can't process boolean operation on a ROI with unknown dimension.");
        }

        return result;
    }

    /**
     * Compute the resulting bounds for <i>union</i> operation between specified ROIs.<br>
     * It throws an exception if the <i>union</i> operation cannot be done (incompatible dimension).
     */
    public static Rectangle5D getUnionBounds(final ROI roi1, final ROI roi2) throws UnsupportedOperationException {
        // null checking
        if (roi1 == null) {
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
            throw new UnsupportedOperationException("Can't process union on ROI with a finite C dimension and infinite T or Z dimension");
        // cannot process union if T dimension is finite but Z is infinite
        if (!it && iz)
            throw new UnsupportedOperationException("Can't process union on ROI with a finite T dimension and infinite Z dimension");

        return bounds1;
    }

    /**
     * Compute the resulting bounds for <i>intersection</i> operation between specified ROIs.<br>
     * It throws an exception if the <i>intersection</i> operation cannot be done (incompatible dimension).
     */
    protected static Rectangle5D getIntersectionBounds(final ROI roi1, final ROI roi2) throws UnsupportedOperationException {
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
            throw new UnsupportedOperationException("Can't process intersection on ROI with a finite C dimension and infinite T or Z dimension");
        // cannot process intersection if T dimension is finite but Z is infinite
        if (!it && iz)
            throw new UnsupportedOperationException("Can't process intersection on ROI with a finite T dimension and infinite Z dimension");

        return bounds1;
    }

    /**
     * Compute the resulting bounds for <i>subtraction</i> of (roi1 - roi2).<br>
     * It throws an exception if the <i>subtraction</i> operation cannot be done (incompatible dimension).
     */
    protected static Rectangle5D getSubtractionBounds(final ROI roi1, final ROI roi2) throws UnsupportedOperationException {
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
            throw new UnsupportedOperationException("Can't process subtraction: ROI 1 has infinite C dimension while ROI 2 has a finite one");
        if (it1 && !it2)
            throw new UnsupportedOperationException("Can't process subtraction: ROI 1 has infinite T dimension while ROI 2 has a finite one");
        if (iz1 && !iz2)
            throw new UnsupportedOperationException("Can't process subtraction: ROI 1 has infinite Z dimension while ROI 2 has a finite one");

        return bounds1;
    }

    /**
     * Computes union of specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    public static ROI getUnion(final ROI roi1, final ROI roi2) throws UnsupportedOperationException, InterruptedException {
        // null checking
        if (roi1 == null) {
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

        final BooleanMask4D[] mask5D = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++) {
            final BooleanMask3D[] mask4D = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++) {
                final BooleanMask2D[] mask3D = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++) {
                    mask3D[z] = BooleanMask2D.getUnion(roi1.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
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
     */
    public static ROI getIntersection(final ROI roi1, final ROI roi2) throws UnsupportedOperationException, InterruptedException {
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

        final BooleanMask4D[] mask5D = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++) {
            final BooleanMask3D[] mask4D = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++) {
                final BooleanMask2D[] mask3D = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++) {
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
     */
    public static ROI getExclusiveUnion(final ROI roi1, final ROI roi2) throws UnsupportedOperationException, InterruptedException {
        // null checking
        if (roi1 == null) {
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

        final BooleanMask4D[] mask5D = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++) {
            final BooleanMask3D[] mask4D = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++) {
                final BooleanMask2D[] mask3D = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++) {
                    mask3D[z] = BooleanMask2D.getExclusiveUnion(roi1.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
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
     */
    public static ROI getSubtraction(final ROI roi1, final ROI roi2) throws UnsupportedOperationException, InterruptedException {
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

        final BooleanMask4D[] mask5D = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++) {
            final BooleanMask3D[] mask4D = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++) {
                final BooleanMask2D[] mask3D = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++) {
                    mask3D[z] = BooleanMask2D.getSubtraction(roi1.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
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
     * @param rois     ROIs we want to merge.
     * @param operator {@link BooleanOperator} to apply.
     * @return {@link ROI} representing the result of the merge operation.
     */
    public static ROI merge(final List<? extends ROI> rois, final BooleanOperator operator) throws UnsupportedOperationException, InterruptedException {
        if (rois.size() == 0)
            return null;

        final List<ROI2DShape> roi2dShapes = new ArrayList<>();
        final List<ROI> roiOthers = new ArrayList<>();

        // classify roi by type
        for (final ROI roi : rois) {
            if (roi instanceof ROI2DShape)
                roi2dShapes.add((ROI2DShape) roi);
            else
                roiOthers.add(roi);
        }

        ROI result;
        if (!roi2dShapes.isEmpty()) {
            final ROI2DShape roi = roi2dShapes.get(0);
            result = new ROI2DPath(roi);
            copyROIProperties(roi, result, true);
            ((ROI2DPath) result).setZ(roi.getZ());
            ((ROI2DPath) result).setT(roi.getT());
            ((ROI2DPath) result).setC(roi.getC());
        }
        else
            result = rois.get(0).getCopy();

        // copy can fail...
        if (result != null) {
            switch (operator) {
                case AND:
                    // ROI2DShape optimization
                    if (!roi2dShapes.isEmpty()) {
                        roiOthers.addAll(((ROI2DPath) result).intersectFast(roi2dShapes));
                        ((ROI2DPath) result).updatePath();
                    }

                    for (final ROI roiOther : roiOthers) {
                        // interrupt task
                        if (Thread.currentThread().isInterrupted())
                            throw new InterruptedException("ROI AND merging process interrupted.");
                        result = result.intersect(roiOther, true);
                    }
                    break;
                case OR:
                    // ROI2DShape optimization
                    if (!roi2dShapes.isEmpty()) {
                        roiOthers.addAll(((ROI2DPath) result).addFast(roi2dShapes));
                        ((ROI2DPath) result).updatePath();
                    }

                    for (final ROI roiOther : roiOthers) {
                        // interrupt task
                        if (Thread.currentThread().isInterrupted())
                            throw new InterruptedException("ROI OR merging process interrupted.");
                        result = result.add(roiOther, true);
                    }
                    break;

                case XOR:
                    // FIXME Strange behavior when more than 2 ROIs (possibly fastXOR)
                    // ROI2DShape optimization
                    if (!roi2dShapes.isEmpty()) {
                        roiOthers.addAll(((ROI2DPath) result).exclusiveAddFast(roi2dShapes));
                        ((ROI2DPath) result).updatePath();
                    }

                    for (final ROI roiOther : roiOthers) {
                        // interrupt task
                        if (Thread.currentThread().isInterrupted())
                            throw new InterruptedException("ROI XOR merging process interrupted.");
                        result = result.exclusiveAdd(roiOther, true);
                    }
                    break;
            }
        }

        return result;
    }

    /**
     * Builds and returns a ROI corresponding to the union of the specified ROI list.
     */
    public static ROI getUnion(final List<? extends ROI> rois) throws UnsupportedOperationException, InterruptedException {
        return merge(rois, BooleanOperator.OR);
    }

    /**
     * Builds and returns a ROI corresponding to the exclusive union of the specified ROI list.
     */
    public static ROI getExclusiveUnion(final List<? extends ROI> rois) throws UnsupportedOperationException, InterruptedException {
        return merge(rois, BooleanOperator.XOR);
    }

    /**
     * Builds and returns a ROI corresponding to the intersection of the specified ROI list.
     */
    public static ROI getIntersection(final List<? extends ROI> rois) throws UnsupportedOperationException, InterruptedException {
        return merge(rois, BooleanOperator.AND);
    }

    /**
     * Subtract the content of the roi2 from the roi1 and return the result as a new {@link ROI}.<br>
     * This is equivalent to: <code>roi1.getSubtraction(roi2)</code>
     *
     * @return {@link ROI} representing the result of subtraction.
     */
    public static ROI subtract(final ROI roi1, final ROI roi2) throws UnsupportedOperationException, InterruptedException {
        return roi1.getSubtraction(roi2);
    }

    /**
     * Converts the specified ROI to a ROI Point ({@link ROI2DPoint} or {@link ROI3DPoint}) representing the mass center of the input ROI.
     *
     * @return the ROI point representing the mass center of the input ROI.
     */
    public static ROI convertToPoint(final ROI roi) throws InterruptedException {
        final ROI result;
        final Point5D pt = computeMassCenter(roi);

        if (roi instanceof ROI2D) {
            result = new ROI2DPoint(pt.getX(), pt.getY());
            ((ROI2DPoint) result).setZ(((ROI2D) roi).getZ());
            ((ROI2DPoint) result).setT(((ROI2D) roi).getT());
            ((ROI2DPoint) result).setC(((ROI2D) roi).getC());
        }
        else if (roi instanceof ROI3D) {
            result = new ROI3DPoint(pt.getX(), pt.getY(), pt.getZ());
            ((ROI3DPoint) result).setT(((ROI3D) roi).getT());
            ((ROI3DPoint) result).setC(((ROI3D) roi).getC());
        }
        else {
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
     */
    public static ROI2DEllipse convertToEllipse(final ROI roi, final double radiusX, final double radiusY) throws InterruptedException {
        final Point5D pt = computeMassCenter(roi);
        final double x = pt.getX();
        final double y = pt.getY();
        final ROI2DEllipse result = new ROI2DEllipse(x - radiusX, y - radiusY, x + radiusX, y + radiusY);

        if (roi instanceof ROI2D) {
            result.setZ(((ROI2D) roi).getZ());
            result.setT(((ROI2D) roi).getT());
            result.setC(((ROI2D) roi).getC());
        }
        else if (roi instanceof ROI3D) {
            result.setZ((int) pt.getZ());
            result.setT(((ROI3D) roi).getT());
            result.setC(((ROI3D) roi).getC());
        }
        else {
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
     */
    public static ROI2DRectangle convertToRectangle(final ROI roi, final double width, final double height) throws InterruptedException {
        final Point5D pt = computeMassCenter(roi);
        final double x = pt.getX();
        final double y = pt.getY();
        final double rw = width / 2;
        final double rh = height / 2;
        final ROI2DRectangle result = new ROI2DRectangle(x - rw, y - rh, x + rw, y + rh);

        if (roi instanceof ROI2D) {
            result.setZ(((ROI2D) roi).getZ());
            result.setT(((ROI2D) roi).getT());
            result.setC(((ROI2D) roi).getC());
        }
        else if (roi instanceof ROI3D) {
            result.setZ((int) pt.getZ());
            result.setT(((ROI3D) roi).getT());
            result.setC(((ROI3D) roi).getC());
        }
        else {
            result.setZ((int) pt.getZ());
            result.setT((int) pt.getT());
            result.setC((int) pt.getC());
        }

        // preserve properties
        ROIUtil.copyROIProperties(roi, result, true);

        return result;
    }

    /**
     * Converts the specified 2D ROI to 3D ROI by elongating it along the Z axis with the given Z position and size Z parameters.
     *
     * @return the converted 3D ROI
     */
    public static ROI convertTo3D(final ROI2D roi, final double z, final double sizeZ) throws InterruptedException {
        ROI result = null;

        switch (roi) {
            case final ROI2DRectangle roi2DRectangle -> result = new ROI3DBox(roi.getBounds2D(), z, sizeZ);
            case final ROI2DEllipse roi2DEllipse -> result = new ROI3DCylinder(roi.getBounds2D(), z, sizeZ);
            case final ROI2DPolygon roi2DPolygon -> result = new ROI3DFlatPolygon(roi2DPolygon.getPolygon2D(), z, sizeZ);
            case null, default -> {
                final int zMin = (int) z;
                int zMax = (int) (z + sizeZ);
                // integer ? --> decrement by one
                if ((double) zMax == (z + sizeZ))
                    zMax--;

                // empty
                if (zMin > zMax)
                    return null;

                if (roi instanceof ROI2DArea)
                    result = new ROI3DArea(roi.getBooleanMask(true), zMin, zMax);
                else if (roi != null)
                    result = new ROI3DArea(roi.getBooleanMask2D(roi.getZ(), roi.getT(), roi.getC(), true), zMin, zMax);
            }
        }

        if (result != null) {
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
     */
    public static ROI[] convertTo2D(final ROI3D roi) throws InterruptedException {
        ROI[] result = null;

        if (roi instanceof final ROI3DPoint roi3d) {
            final Point3D position = roi3d.getPosition3D();
            final ROI2DPoint roi2d = new ROI2DPoint(position.getX(), position.getY());
            roi2d.c = roi3d.c;
            roi2d.t = roi3d.t;
            roi2d.z = (int) Math.round(position.getZ());
            result = new ROI[]{roi2d};
        }
        else if (roi instanceof final ROI3DStack<?> roi3d) {
            final List<ROI> rois2d = new ArrayList<>(roi3d.getSizeZ());
            final int z0 = (int) Math.floor(roi3d.getBounds3D().getZ());
            for (int z = z0; z < z0 + roi3d.getSizeZ(); z++) {
                ROI2D roi2d = roi3d.getSlice(z);
                if (roi2d != null) {
                    roi2d = (ROI2D) roi2d.getCopy();
                    roi2d.setZ(z);
                    roi2d.setC(roi3d.c);
                    roi2d.setT(roi3d.t);
                    rois2d.add(roi2d);
                }
            }
            result = rois2d.toArray(new ROI[0]);
        }
        else if (roi instanceof final ROI3DZShape roi3d) {
            ROI2DShape roi2d = roi3d.getShape2DROI();
            if (roi2d != null) {
                roi2d = (ROI2DShape) roi2d.getCopy();
                roi2d.setZ(-1);
                roi2d.setC(roi3d.c);
                roi2d.setT(roi3d.t);
            }
            result = new ROI[]{roi2d};
        }
        else if (roi instanceof final ROI3DArea roi3d) {
            final List<ROI> rois2d = new ArrayList<>(roi3d.getSizeZ());
            final int z0 = (int) Math.floor(roi3d.getBounds3D().getZ());
            for (int z = z0; z < z0 + roi3d.getSizeZ(); z++) {
                final ROI2DArea roi2d = new ROI2DArea(roi3d.getBooleanMask2D(z, true));
                roi2d.setZ(z);
                roi2d.setC(roi3d.c);
                roi2d.setT(roi3d.t);
                rois2d.add(roi2d);
            }
            result = rois2d.toArray(new ROI[0]);
        }
        else if (roi != null) {
            final int sizeZ = (int) Math.round(roi.getBounds3D().getSizeZ());
            final List<ROI> rois2d = new ArrayList<>(sizeZ);
            for (int z = (int) Math.floor(roi.getBounds3D().getZ()); z <= sizeZ; z++) {
                final BooleanMask2D mask2d = roi.getBooleanMask2D(z, true);
                if (mask2d != null && !mask2d.isEmpty()) {
                    final ROI2DArea roi2d = new ROI2DArea(mask2d);
                    roi2d.setZ(z);
                    roi2d.setC(roi.c);
                    roi2d.setT(roi.t);
                    rois2d.add(roi2d);
                }
            }
            result = rois2d.toArray(new ROI[0]);
        }

        if ((roi != null) && (result != null)) {
            String name = roi.getName();

            // remove "stack" suffix is present
            if (name.endsWith(STACK_SUFFIX))
                name = StringUtil.removeLast(name, STACK_SUFFIX.length());
            else if (name.endsWith(ZEXT_SUFFIX))
                name = StringUtil.removeLast(name, ZEXT_SUFFIX.length());

            // unselect all control points
            for (final ROI roi2d : result) {
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
     * Converts the specified ROI to a boolean mask type ROI (ROI AreaX).
     *
     * @return the ROI AreaX corresponding to the input ROI.<br>
     * If the ROI is already of boolean mask type then it's directly returned without any conversion.
     */
    public static ROI convertToMask(final ROI roi) throws InterruptedException {
        // no conversion needed
        // TODO remove this comment once ROI4D and ROI5D completely removed
        if ((roi instanceof ROI2DArea) || (roi instanceof ROI3DArea)/* || (roi instanceof ROI4DArea) || (roi instanceof ROI5DArea)*/)
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
        final BooleanMask4D[] mask5D = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++) {
            final BooleanMask3D[] mask4D = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++) {
                final BooleanMask2D[] mask3D = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                    mask3D[z] = new BooleanMask2D(new Rectangle(bounds2D), roi.getBooleanMask2D(bounds2D, bounds.z + z, bounds.t + t, bounds.c + c, true));

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
     * @param roi          the roi to convert to shape type ROI
     * @param maxDeviation maximum allowed deviation/distance of resulting ROI polygon from the input ROI contour (in pixel).
     *                     Use <code>-1</code> for automatic maximum deviation calculation.
     * @return the ROI Polygon or ROI Mesh corresponding to the input ROI.<br>
     * If the ROI is already of shape type then it's directly returned without any conversion.
     */
    public static ROI convertToShape(final ROI roi, final double maxDeviation) throws UnsupportedOperationException, InterruptedException {
        if (roi instanceof ROI2DShape)
            return roi;

        if (roi instanceof ROI2D) {
            final BooleanMask2D[] componentMasks = ((ROI2D) roi).getBooleanMask(true).getComponents();
            final AreaX area = new AreaX();
            ROI2DShape result = new ROI2DPolygon();

            // for each component
            for (final BooleanMask2D componentMask : componentMasks) {
                // get contour points in connected order
                final List<Point> points = componentMask.getConnectedContourPoints();
                // convert to point2D and center points in observed pixel
                final List<Point2D> points2D = new ArrayList<>(points.size());
                for (final Point pt : points)
                    points2D.add(new Point2D.Double(pt.x + 0.5d, pt.y + 0.5d));

                final double dev;

                // auto deviation (compute it from ROI border len)
                if (maxDeviation < 0)
                    dev = Math.log10(Math.sqrt(points2D.size()) / 3);
                else
                    dev = maxDeviation;

                // compute polygon for this component
                final Polygon2D polygon = Polygon2D.getPolygon2D(points2D, dev);

                // single component ? --> create polygon ROI
                if (componentMasks.length == 1)
                    result = new ROI2DPolygon(polygon);
                    // add polygon to area
                else
                    area.add(new AreaX(polygon));
            }

            // multiple components ? --> create ROI2DPath from area
            if (componentMasks.length > 1)
                result = new ROI2DPath(area);

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

        if (roi instanceof ROI3D) {
            // not yet supported
            throw new UnsupportedOperationException("ROIUtil.convertToShape(ROI): Operation not supported for 3D ROI.");

        }

        throw new UnsupportedOperationException("ROIUtil.convertToShape(ROI): Operation not supported for this ROI: " + roi.getName());
    }

    /**
     * Returns connected component from specified ROI as a list of ROI (AreaX type).
     */
    public static List<ROI> getConnectedComponents(final ROI roi) throws UnsupportedOperationException, InterruptedException {
        final List<ROI> result = new ArrayList<>();

        if (roi instanceof final ROI2D roi2d) {
            int ind = 0;

            for (final BooleanMask2D component : roi2d.getBooleanMask(true).getComponents()) {
                final ROI2DArea componentRoi = new ROI2DArea(component);

                if (!componentRoi.isEmpty()) {
                    // keep original ROI informations
                    componentRoi.setName(roi.getName() + OBJECT_SUFFIX + " #" + ind++);
                    copyROIProperties(roi, componentRoi, false);

                    result.add(componentRoi);
                }
            }

            return result;
        }

        if (roi instanceof final ROI3D roi3d) {
            int ind = 0;

            for (final BooleanMask3D component : roi3d.getBooleanMask(true).getComponents()) {
                final ROI3DArea componentRoi = new ROI3DArea(component);

                if (!componentRoi.isEmpty()) {
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

        throw new UnsupportedOperationException("ROIUtil.getConnectedComponents(ROI): Operation not supported for this ROI: " + roi.getName());
    }

    static boolean computePolysFromLine(final Line2D line, final Point2D edgePt1, final Point2D edgePt2, final Polygon2D poly1, final Polygon2D poly2, final boolean inner) {
        final Line2D edgeLine = new Line2D.Double(edgePt1, edgePt2);

        // they intersect ?
        if (edgeLine.intersectsLine(line)) {
            final Point2D intersection = GeomUtil.getIntersection(edgeLine, line);
            Objects.requireNonNull(intersection);

            // are we inside poly2 ?
            if (inner) {
                // add intersection to poly2
                poly2.addPoint(intersection);
                // add intersection and pt2 to poly1
                poly1.addPoint(intersection);
                poly1.addPoint(edgePt2);
            }
            else {
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
     */
    public static List<ROI> split(final ROI roi, final Line2D line) throws UnsupportedOperationException, InterruptedException {
        final Rectangle2D bounds2d = roi.getBounds5D().toRectangle2D();
        // need to enlarge bounds a bit to avoid roundness issues on line intersection
        final Rectangle2D extendedBounds2d = Rectangle2DUtil.getScaledRectangle(bounds2d, 1.1d, true);
        // enlarge line to ROI bounds
        final Line2D extendedLine = Rectangle2DUtil.getIntersectionLine(extendedBounds2d, line);

        // if the extended line intersects the ROI bounds
        if ((extendedLine != null) && bounds2d.intersectsLine(extendedLine)) {
            final List<ROI> result = new ArrayList<>();
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
     * @param inputRois list of ROI to convert
     * @param sizeX     the wanted size X of output Sequence, if set to <code>0</code> then Sequence size X is computed
     *                  automatically from
     *                  the global ROI bounds.
     * @param sizeY     the wanted size Y of output Sequence, if set to <code>0</code> then Sequence size Y is computed
     *                  automatically from
     *                  the global ROI bounds.
     * @param sizeC     the wanted size C of output Sequence, if set to <code>0</code> then Sequence size C is computed
     *                  automatically from
     *                  the global ROI bounds.
     * @param sizeZ     the wanted size Z of output Sequence, if set to <code>0</code> then Sequence size Z is computed
     *                  automatically from
     *                  the global ROI bounds.
     * @param sizeT     the wanted size T of output Sequence, if set to <code>0</code> then Sequence size T is computed
     *                  automatically from
     *                  the global ROI bounds.
     * @param dataType  the wanted dataType of output Sequence
     * @param label     if set to <code>true</code> then each ROI will be draw as a separate label (value) in the sequence
     *                  starting from 1.
     */
    public static Sequence convertToSequence(final List<ROI> inputRois, final int sizeX, final int sizeY, final int sizeC, final int sizeZ, final int sizeT, final DataType dataType, final boolean label)
            throws InterruptedException {
        final List<ROI> rois = new ArrayList<>();
        final Rectangle5D bounds = new Rectangle5D.Double();
        // we cannot merge ROI for labeled sequence
        boolean canMerge = !label;

        // can merge ROI ? (faster)
        if (canMerge) {
            try {
                // compute the union of all ROI
                final ROI roi = ROIUtil.merge(inputRois, BooleanOperator.OR);
                // get bounds of result
                bounds.add(Objects.requireNonNull(roi).getBounds5D());
                // add this single ROI to list
                rois.add(roi);
            }
            catch (final Exception e) {
                // merge failed
                canMerge = false;
            }
        }

        if (!canMerge) {
            for (final ROI roi : inputRois) {
                // compute global bounds
                if (roi != null) {
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
        try {
            for (int t = 0; t < sT; t++)
                for (int z = 0; z < sZ; z++)
                    out.setImage(t, z, new IcyBufferedImage(sX, sY, sC, dataType));

            double fillValue = 1d;

            // set value from ROI(s)
            for (final ROI roi : rois) {
                if (!roi.getBounds5D().isEmpty())
                    DataIteratorUtil.set(new SequenceDataIterator(out, roi), fillValue);

                if (label)
                    fillValue += 1d;
            }

            // notify data changed
            out.dataChanged();
        }
        finally {
            out.endUpdate();
        }

        return out;
    }

    /**
     * Convert a list of ROI into a binary / labeled Sequence.
     *
     * @param inputRois list of ROI to convert
     * @param sequence  the sequence used to define the wanted sequence dimension in return.<br>
     *                  If this field is <code>null</code> then the global ROI bounds will be used to define the Sequence
     *                  dimension
     * @param label     if set to <code>true</code> then each ROI will be draw as a separate label (value) in the sequence
     *                  starting from 1.
     */
    public static Sequence convertToSequence(final List<ROI> inputRois, final Sequence sequence, final boolean label) throws InterruptedException {
        if (sequence == null)
            return convertToSequence(inputRois, 0, 0, 0, 0, 0, label ? ((inputRois.size() > 255) ? DataType.USHORT : DataType.UBYTE) : DataType.UBYTE, label);

        return convertToSequence(inputRois, sequence.getSizeX(), sequence.getSizeY(), 1, sequence.getSizeZ(), sequence.getSizeT(), sequence.getDataType(), label);
    }

    /**
     * Convert a single ROI into a binary / labeled Sequence.
     *
     * @param inputRoi ROI to convert
     * @param sequence the sequence used to define the wanted sequence dimension in return.<br>
     *                 If this field is <code>null</code> then the global ROI bounds will be used to define the Sequence
     *                 dimension
     */
    public static Sequence convertToSequence(final ROI inputRoi, final Sequence sequence) throws InterruptedException {
        return convertToSequence(CollectionUtil.createArrayList(inputRoi), sequence, false);
    }

    /**
     * Scale (3D) the given ROI by specified X,Y,Z factors.<br>
     * Only {@link ROI2DShape} and {@link ROI3DShape} are supported !
     *
     * @param roi input ROI we want to rescale
     * @throws UnsupportedOperationException if input ROI is not ROI2DShape or ROI3DShape (scaling supported only for these ROI)
     */
    public static void scale(final ROI roi, final double scaleX, final double scaleY, final double scaleZ) throws UnsupportedOperationException {
        // shape ROI --> can rescale easily
        switch (roi) {
            case final ROI2DRectShape roi2DRectShape -> {
                roi2DRectShape.beginUpdate();
                try {
                    final Rectangle2D bounds = roi2DRectShape.getBounds2D();

                    // reshape directly
                    bounds.setFrame(bounds.getX() * scaleX, bounds.getY() * scaleY, bounds.getWidth() * scaleX, bounds.getHeight() * scaleY);
                    roi2DRectShape.setBounds2D(bounds);

                    final int z = roi2DRectShape.getZ();

                    // re scale Z position if needed
                    if ((z != -1) && (scaleZ != 1d))
                        roi2DRectShape.setZ((int) (z * scaleZ));
                }
                finally {
                    roi2DRectShape.endUpdate();
                }
            }
            case final ROI2DShape roi2DShape -> {
                roi2DShape.beginUpdate();
                try {
                    // adjust control point position directly
                    for (final Anchor2D pt : roi2DShape.getControlPoints()) {
                        final Point2D pos = pt.getPosition();
                        // change control point position
                        pt.setPosition(pos.getX() * scaleX, pos.getY() * scaleY);
                    }

                    final int z = roi2DShape.getZ();

                    // re scale Z position if needed
                    if ((z != -1) && (scaleZ != 1d))
                        roi2DShape.setZ((int) (z * scaleZ));
                }
                finally {
                    roi2DShape.endUpdate();
                }
            }
            case final ROI3DShape roi3DShape -> {
                roi3DShape.beginUpdate();
                try {
                    // adjust control point position directly
                    for (final Anchor3D pt : roi3DShape.getControlPoints()) {
                        final Point3D pos = pt.getPosition();
                        // change control point position
                        pt.setPosition(pos.getX() * scaleX, pos.getY() * scaleY, pos.getZ() * scaleZ);
                    }
                }
                finally {
                    roi3DShape.endUpdate();
                }
            }
            case null, default -> throw new UnsupportedOperationException("ROIUtil.scale: cannot rescale " + Objects.requireNonNull(roi).getSimpleClassName() + " !");
        }
    }

    /**
     * Scale (2D) the given ROI by specified X/Y factor.<br>
     * Only {@link ROI2DShape} and {@link ROI3DShape} are supported !
     *
     * @param roi input ROI we want to rescale
     * @throws UnsupportedOperationException if input ROI is not ROI2DShape or ROI3DShape (scaling supported only for these ROI)
     */
    public static void scale(final ROI roi, final double scaleX, final double scaleY) throws UnsupportedOperationException {
        scale(roi, scaleX, scaleY, 1d);
    }

    /**
     * Scale the given ROI by specified scale factor.<br>
     * Only {@link ROI2DShape} and {@link ROI3DShape} are supported !
     *
     * @param roi input ROI we want to rescale
     * @throws UnsupportedOperationException if input ROI is not ROI2DShape or ROI3DShape (scaling supported only for these ROI)
     */
    public static void scale(final ROI roi, final double scale) throws UnsupportedOperationException {
        scale(roi, scale, scale, scale);
    }

    /**
     * Create and returns a new ROI which is a 2x up/down scaled version of the input ROI.<br>
     * Note that the returned ROI can be ROI2DArea or ROI3DArea if original ROI format doesn't support 2X scale
     * operation.
     *
     * @param roi      input ROI we want to get the up scaled form
     * @param scaleOnZ Set to <code>true</code> to scale as well on Z dimension (XY dimension only otherwise)
     * @param down     Set to <code>true</code> for down scaling and <code>false</code> for up scaling operation
     * @throws UnsupportedOperationException if input ROI is ROI4D or ROI5D (up scaling not supported for these ROI)
     */
    public static ROI get2XScaled(final ROI roi, final boolean scaleOnZ, final boolean down) throws UnsupportedOperationException, InterruptedException {
        if (roi == null)
            return null;

        final double scaling = down ? 0.5d : 2d;
        ROI result = roi.getCopy();

        // shape ROI --> can rescale easily
        if ((result instanceof ROI2DShape) || (result instanceof ROI3DShape))
            scale(result, scaling, scaling, scaleOnZ ? scaling : 1d);
        else if (result instanceof ROI2D) {
            final ROI2DArea roi2DArea;

            if (result instanceof ROI2DArea) {
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
            else {
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
        else if (result instanceof ROI3D) {
            final ROI3DArea roi3DArea;

            // we want a ROI2DArea
            if (result instanceof ROI3DArea) {
                roi3DArea = (ROI3DArea) result;

                // scale
                if (down) {
                    if (scaleOnZ)
                        roi3DArea.downscale();
                    else
                        roi3DArea.downscale2D();
                }
                else {
                    if (scaleOnZ)
                        roi3DArea.upscale();
                    else
                        roi3DArea.upscale2D();
                }
            }
            else {
                final BooleanMask3D bm = ((ROI3D) result).getBooleanMask(true);

                // scale
                if (down) {
                    if (scaleOnZ)
                        roi3DArea = new ROI3DArea(bm.downscale());
                    else
                        roi3DArea = new ROI3DArea(bm.downscale2D());
                }
                else {
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
     * @param roi      input ROI we want to get the up scaled form
     * @param scaleOnZ Set to <code>true</code> to scale as well on Z dimension (XY dimension only otherwise)
     * @throws UnsupportedOperationException if input ROI is ROI4D or ROI5D (up scaling not supported for these ROI)
     */
    public static ROI getUpscaled(final ROI roi, final boolean scaleOnZ) throws UnsupportedOperationException, InterruptedException {
        return get2XScaled(roi, scaleOnZ, false);
    }

    /**
     * Create and returns a new ROI which is a 2x down scaled version of the input ROI.<br>
     * Note that the returned ROI can be ROI2DArea or ROI3DArea if original ROI format doesn't support up scale
     * operation.
     *
     * @param roi      input ROI we want to get the up scaled form
     * @param scaleOnZ Set to <code>true</code> to scale as well on Z dimension (XY dimension only otherwise)
     * @throws UnsupportedOperationException if input ROI is ROI4D or ROI5D (up scaling not supported for these ROI)
     */
    public static ROI getDownscaled(final ROI roi, final boolean scaleOnZ) throws UnsupportedOperationException, InterruptedException {
        return get2XScaled(roi, scaleOnZ, true);
    }

    /**
     * Create a copy of the specified ROI coming from <i>source</i> sequence adjusted to the <i>destination</i> sequence.<br>
     * The resulting ROI coordinates can be different if the {@link Sequence#getPosition()} are not identical on the 2 sequences.<br>
     * The resulting ROI can be scaled if the {@link Sequence#getPixelSize()} are not identical on the 2 sequences<br>
     * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format, also the scale operation may not be possible depending
     * the original ROI format.
     *
     * @param roi                input ROI we want to adjust
     * @param source             the source sequence where the ROI was initially generated (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param destination        the destination sequence where we want to copy the ROI (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param translate          if we allow the returned ROI to be translated compared to the original ROI
     * @param scale              if we allow the returned ROI to be scaled compared to the original ROI
     * @param ignoreErrorOnScale ignore unsupported scaling operation, in which case the result ROI won't be correctly scaled
     * @return adjusted ROI
     * @throws UnsupportedOperationException if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI) and <code>ignoreErrorOnScale</code> is set to
     *                                       <code>FALSE</code>
     */
    public static ROI adjustToSequence(final ROI roi, final Sequence source, final Sequence destination, final boolean translate, final boolean scale, final boolean ignoreErrorOnScale)
            throws UnsupportedOperationException, InterruptedException {
        if (roi == null)
            return null;

        // create a copy
        ROI result = roi.getCopy();

        if (scale) {
            final double scaleX = source.getPixelSizeX() / destination.getPixelSizeX();
            final double scaleY = source.getPixelSizeY() / destination.getPixelSizeY();
            final double scaleZ = source.getPixelSizeZ() / destination.getPixelSizeZ();

            // ROI is a 2D or 3D shape ? --> use easy scaling
            if ((result instanceof ROI2DShape) || (result instanceof ROI3DShape))
                scale(result, scaleX, scaleY, scaleZ);
            else {
                boolean doRescale = true;
                boolean doRescaleZ = true;

                if (MathUtil.round(scaleX / scaleY, 3) != 1d) {
                    doRescale = false;
                    if (ignoreErrorOnScale)
                        IcyLogger.warn(ROIUtil.class, "ROIUtil.adjustToSequence: cannot rescale ROI with different X/Y scale ratio.");
                    else
                        throw new UnsupportedOperationException("ROIUtil.adjustToSequence: cannot rescale ROI (different X/Y scale ratio) !");
                }

                // get log2 of scaleX/Y (round it a bit)
                final double resDelta = MathUtil.round(Math.log(scaleX) / Math.log(2), 1);

                // too far from 2^x scaling
                if (Math.round(resDelta) != resDelta) {
                    doRescale = false;
                    if (ignoreErrorOnScale)
                        IcyLogger.warn(ROIUtil.class, "ROIUtil.adjustToSequence: cannot rescale ROI with scale XY = " + scaleX);
                    else
                        throw new UnsupportedOperationException("ROIUtil.adjustToSequence: cannot rescale ROI (scale XY = " + scaleX + ") !");
                }

                // get log2 of scaleZ (round it a bit)
                final double resDeltaZ = MathUtil.round(Math.log(scaleZ) / Math.log(2), 1);

                if (Math.round(resDeltaZ) != resDeltaZ) {
                    doRescaleZ = false;
                    if (ignoreErrorOnScale)
                        IcyLogger.warn(ROIUtil.class, "ROIUtil.adjustToSequence: ignoring ROI Z rescaling (scale Z = " + scaleZ + ")");
                    else
                        throw new UnsupportedOperationException("ROIUtil.adjustToSequence: cannot rescale ROI (scale Z = " + scaleZ + ") !");
                }

                final boolean zScaling = resDeltaZ != 0d;

                // Z rescaling needed ? --> we need to have same XY and Z scale ratio
                if (zScaling && (MathUtil.round(resDeltaZ / resDelta, 3) != 1d)) {
                    doRescaleZ = false;
                    if (ignoreErrorOnScale)
                        IcyLogger.warn(ROIUtil.class, "ROIUtil.adjustToSequence: ignoring ROI Z rescaling (scale XY = " + scaleX + " while scale Z = " + scaleZ + ")");
                    else
                        throw new UnsupportedOperationException("ROIUtil.adjustToSequence: cannot rescale ROI (scale XY = " + scaleX + " while scale Z = " + scaleZ + ") !");
                }

                try {
                    if (doRescale) {
                        int i = (int) resDelta;

                        // destination resolution level > source resolution level
                        if (resDelta > 0) {
                            // down scaling
                            while (i-- > 0)
                                result = getUpscaled(result, zScaling && doRescaleZ);
                        }
                        else {
                            // up scaling
                            while (i++ < 0)
                                result = getDownscaled(result, zScaling && doRescaleZ);
                        }
                    }
                }
                catch (final UnsupportedOperationException e) {
                    // we should propagate it then
                    if (!ignoreErrorOnScale)
                        throw e;
                }
            }
        }

        // can set position ? --> relocate it
        if (translate && result.canSetPosition()) {
            // get current position
            final Point5D pos = result.getPosition5D();

            // can change it Z position ?
            if (!Double.isInfinite(pos.getZ())) {
                // we just want to get offset as scaling already taken care of converting relative ROI position
                final Point3D offset = SequenceUtil.convertPoint(new Point3D.Double(), source, destination);

                // compute position in destination
                pos.setX(pos.getX() + offset.getX());
                pos.setY(pos.getY() + offset.getY());
                pos.setZ(pos.getZ() + offset.getZ());
            }
            else {
                // we just want to get offset as scaling already taken care of converting relative ROI position
                final Point2D offset = SequenceUtil.convertPoint(new Point2D.Double(), source, destination);

                // compute position in destination
                pos.setX(pos.getX() + offset.getX());
                pos.setY(pos.getY() + offset.getY());
            }

            // can change it ? (we don't scale T dimension)
            if (!Double.isInfinite(pos.getT())) {
                // get time interval in ms
                final double timeIntervalMs = destination.getTimeInterval() * 1000d;

                if (timeIntervalMs > 0d) {
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
     * @param roi         input ROI we want to adjust
     * @param source      the source sequence where the ROI was initially generated (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param destination the destination sequence where we want to copy the ROI (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param translate   if we allow the returned ROI to be translated compared to the original ROI
     * @param scale       if we allow the returned ROI to be scaled compared to the original ROI
     * @return adjusted ROI
     * @throws UnsupportedOperationException if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI) and <code>ignoreErrorOnScale</code> is set to
     *                                       <code>FALSE</code>
     */
    public static ROI adjustToSequence(final ROI roi, final Sequence source, final Sequence destination, final boolean translate, final boolean scale) throws UnsupportedOperationException, InterruptedException {
        return adjustToSequence(roi, source, destination, translate, scale, false);
    }

    /**
     * Create a copy of the specified ROI coming from <i>source</i> sequence adjusted to the <i>destination</i> sequence.<br>
     * The resulting ROI coordinates can be different if the {@link Sequence#getPosition()} are not identical on the 2 sequences.<br>
     * The resulting ROI can be scaled if the {@link Sequence#getPixelSize()} are not identical on the 2 sequences<br>
     * Note that the returned ROI can have a Boolean Mask format if we can't re-use original ROI format, also the scale operation may not be possible depending
     * the original ROI format.
     *
     * @param roi         input ROI we want to adjust
     * @param source      the source sequence where the ROI was initially generated (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @param destination the destination sequence where we want to copy the ROI (should contains valid <i>origin</i> information, see Sequence#getOriginXXX(} methods)
     * @return adjusted ROI
     * @throws UnsupportedOperationException if input ROI is ROI4D or ROI5D while scaling is required (scaling not supported for these ROI)
     */
    public static ROI adjustToSequence(final ROI roi, final Sequence source, final Sequence destination) throws UnsupportedOperationException, InterruptedException {
        return adjustToSequence(roi, source, destination, true, true);
    }

    /**
     * Copy properties (name, color...) from <code>source</code> ROI and apply it to <code>destination</code> ROI.
     */
    public static void copyROIProperties(final ROI source, final ROI destination, final boolean copyName) {
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
        for (final Entry<String, String> propertyEntry : source.getProperties().entrySet())
            destination.setProperty(propertyEntry.getKey(), propertyEntry.getValue());
    }

    public static Sequence computeDistanceMap(final ROI roi, final Dimension5D imageSize, final Dimension3D pixelSize, final boolean constrainBorders) throws InterruptedException {
        final ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, constrainBorders);
        dt.addROI(roi);
        return dt.getDistanceMap();
    }

    public static Sequence computeDistanceMap(final Collection<? extends ROI> selectedROIs, final Dimension5D imageSize, final Dimension3D pixelSize, final boolean constrainBorders) throws InterruptedException {
        final ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, constrainBorders);
        dt.addAll(selectedROIs);
        return dt.getDistanceMap();
    }

    /**
     * @param selectedRois ROIs that need to be separated. Note: These ROIs will be joined before watersheding.
     * @param seedRois     Seed points used to initialize watershed.
     * @param imageSize    Size of the image where the ROIs are located.
     * @param pixelSize    Physical size of the pixels in the image.
     * @return List of separated ROIs.
     * @throws InterruptedException If the process gets interrupted.
     */
    public static List<ROI> computeWatershedSeparation(final Collection<? extends ROI> selectedRois, final List<? extends ROI> seedRois, final Dimension5D imageSize, final Dimension3D pixelSize) throws InterruptedException {
        final ROIWatershedCalculator.Builder wsBuilder = new ROIWatershedCalculator.Builder(imageSize, pixelSize);

        wsBuilder.addObjects(selectedRois);
        wsBuilder.addSeeds(seedRois);
        wsBuilder.setNewBasinsAllowed(false);
        final ROIWatershedCalculator wsCalculator = wsBuilder.build();

        try {
            wsCalculator.call();
        }
        catch (final InterruptedException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new RuntimeException("Error computing watershed: " + e.getMessage(), e);
        }

        return wsCalculator.getLabelRois();
    }

    /**
     * @param usedSeedRois collection where detected seeds will be stored.
     */
    public static List<ROI> computeWatershedSeparation(final Collection<? extends ROI> selectedRois, final Dimension5D imageSize, final Dimension3D pixelSize, final List<ROI> usedSeedRois) throws InterruptedException {
        final ROIWatershedCalculator.Builder wsBuilder = new ROIWatershedCalculator.Builder(imageSize, pixelSize);

        wsBuilder.addObjects(selectedRois);
        wsBuilder.addSeeds(usedSeedRois);
        wsBuilder.setNewBasinsAllowed(false);
        final ROIWatershedCalculator wsCalculator = wsBuilder.build();

        try {
            wsCalculator.call();
        }
        catch (final InterruptedException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new RuntimeException("Error computing watershed: " + e.getMessage(), e);
        }

        usedSeedRois.clear();
        usedSeedRois.addAll(wsCalculator.getSeeds());
        return wsCalculator.getLabelRois();
    }

    public static List<ROI> computeSkeleton(final List<ROI2D> selectedROIs, final Dimension3D pixelSize, final double distance) throws InterruptedException {
        final List<ROI> result = new ArrayList<>();
        for (final ROI roi : selectedROIs) {
            if (roi.getBounds5D().getSizeX() == 0)
                continue;

            final Point5D oldPosition = new Point5D.Double();
            oldPosition.setLocation(roi.getPosition5D());
            roi.setPosition5D(new Point5D.Double());
            try {
                final ROISkeletonCalculator skeletonizer = new ROISkeletonCalculator(roi, pixelSize);
                final ROI skeletonRoi = skeletonizer.getSkeletonROI();
                if (skeletonRoi != null && skeletonRoi.getBounds5D().getSizeX() > 0)
                // TODO remove equals null
                {
                    final Point5D skeletonPosition = skeletonRoi.getPosition5D();
                    skeletonPosition.setX(skeletonPosition.getX() + oldPosition.getX());
                    skeletonPosition.setY(skeletonPosition.getY() + oldPosition.getY());
                    skeletonPosition.setZ(skeletonPosition.getZ() + oldPosition.getZ());
                    skeletonRoi.setPosition5D(skeletonPosition);
                    result.add(skeletonRoi);
                }
            }
            finally {
                roi.setPosition5D(oldPosition);
            }
        }
        return result;
    }

    public static List<ROI> computeDilation(final List<? extends ROI> selectedROIs, final Dimension3D pixelSize, final double distance) throws InterruptedException {
        final List<ROI> result = new ArrayList<>();
        for (final ROI roi : selectedROIs) {
            if (roi.getBounds5D().getSizeX() == 0)
                continue;

            final Rectangle5D oldBounds = new Rectangle5D.Double(roi.getBounds5D());
            final Rectangle5D processingBounds = new Rectangle5D.Double(roi.getBounds5D());
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
            if (roi.getBounds5D().getSizeZ() > 1 && Double.isFinite(roi.getBounds5D().getSizeZ())) {
                processingBounds.setZ(0);
                processingBounds.setSizeZ(oldBounds.getSizeZ());
            }
            roi.setBounds5D(processingBounds);
            if (roi instanceof ROI2DArea) {
                ((ROI2DArea) roi).setPosition2D(new Point2D.Double(0, 0));
            }

            try {
                final ROIDilationCalculator dilator = new ROIDilationCalculator(roi, pixelSize, distance);
                final ROI dilationRoi = dilator.getDilation();
                final Rectangle5D dilationBounds = dilationRoi.getBounds5D();
                if (dilationBounds.getSizeX() > 0) {
                    final Point5D dPos = dilationBounds.getPosition();
                    final Point5D oPos = oldBounds.getPosition();
                    dilationBounds.setX(dPos.getX() + oPos.getX());
                    dilationBounds.setY(dPos.getY() + oPos.getY());
                    if (Double.isFinite(oldBounds.getSizeZ())) {
                        dilationBounds.setZ(dPos.getZ() + oPos.getZ());
                        if (Double.isFinite(oldBounds.getSizeZ()) && Double.isInfinite(dilationBounds.getSizeZ())) {
                            dilationBounds.setSizeZ(oldBounds.getSizeZ());
                        }
                    }

                    if (Double.isFinite(oldBounds.getSizeT())) {
                        dilationBounds.setT(oPos.getT());
                        dilationBounds.setSizeT(oldBounds.getSizeT());
                    }

                    if (dilationRoi.canSetBounds())
                        dilationRoi.setBounds5D(dilationBounds);
                    else if (dilationRoi instanceof final ROI2DArea areaRoi) {
                        areaRoi.setC(Double.isFinite(dilationBounds.getC()) ? (int) dilationBounds.getC() : -1);
                        areaRoi.setZ(Double.isFinite(dilationBounds.getZ()) ? (int) dilationBounds.getZ() : -1);
                        areaRoi.setT(Double.isFinite(dilationBounds.getT()) ? (int) dilationBounds.getT() : -1);
                        final Rectangle2D bounds = areaRoi.getBounds2D();
                        areaRoi.translate(dilationBounds.getX() - bounds.getX(), dilationBounds.getY() - bounds.getY());
                    }

                }
                result.add(dilationRoi);

            }
            finally {
                roi.setBounds5D(oldBounds);
                if (roi instanceof ROI2DArea) {
                    ((ROI2DArea) roi).setPosition2D(new Point2D.Double(oldBounds.getX(), oldBounds.getY()));
                }
            }
        }
        return result;
    }

    public static List<ROI> computeErosion(final List<? extends ROI> selectedROIs, final Dimension3D pixelSize, final double distance) throws InterruptedException {
        final List<ROI> result = new ArrayList<>();
        for (final ROI roi : selectedROIs) {
            if (roi.getBounds5D().getSizeX() == 0)
                continue;

            final Point5D oldPosition = new Point5D.Double();
            oldPosition.setLocation(roi.getPosition5D());
            roi.setPosition5D(new Point5D.Double());
            try {
                final ROIErosionCalculator eroder = new ROIErosionCalculator(roi, pixelSize, distance);
                final ROI erosionRoi = eroder.getErosion();
                if (erosionRoi.getBounds5D().getSizeX() > 0) {
                    final Point5D erosionPosition = erosionRoi.getPosition5D();
                    erosionPosition.setX(erosionPosition.getX() + oldPosition.getX());
                    erosionPosition.setY(erosionPosition.getY() + oldPosition.getY());
                    erosionPosition.setZ(erosionPosition.getZ() + oldPosition.getZ());
                    erosionRoi.setPosition5D(erosionPosition);
                    result.add(erosionRoi);
                }
            }
            finally {
                roi.setPosition5D(oldPosition);
            }
        }
        return result;
    }

    /**
     * Compute and returns the mass center of specified ROI.
     */
    public static Point5D computeMassCenter(final @NotNull ROI roi) throws InterruptedException {
        final Rectangle5D bounds = roi.getBounds5D();

        // special case of empty bounds ? --> return position
        if (bounds.isEmpty())
            return bounds.getPosition();
        // special case of single point ? --> return position
        if ((roi instanceof ROI2DPoint) || (roi instanceof ROI3DPoint))
            return bounds.getPosition();

        final ROIIterator it = new ROIIterator(roi, true);
        double x, y, z, t, c;
        long numPts;

        x = 0d;
        y = 0d;
        z = 0d;
        t = 0d;
        c = 0d;
        numPts = 0;
        while (!it.done()) {
            // check for interruption sometime
            if (((numPts & 0xFFFF) == 0) && Thread.interrupted())
                throw new InterruptedException("ROI mass center descriptor computation interrupted.");

            x += it.getX();
            y += it.getY();
            z += it.getZ();
            t += it.getT();
            c += it.getC();

            it.next();
            numPts++;
        }

        if (numPts == 0)
            return new Point5D.Double();

        return new Point5D.Double(x / numPts, y / numPts, z / numPts, t / numPts, c / numPts);
    }

    /**
     * Returns the pixel intensity information for the specified ROI and Sequence.<br>
     * Be careful: the returned result may be incorrect or exception may be thrown if the ROI change while the
     * descriptor is being computed.
     *
     * @param roi               the ROI on which we want to compute the intensity descriptors
     * @param sequence          the Sequence used to compute the intensity descriptors
     * @param allowMultiChannel Allow multi channel intensity computation. If this parameter is set to <code>false</code> and the ROI
     *                          number of channel is &gt; 1 then a {@link UnsupportedOperationException} is launch.
     * @throws UnsupportedOperationException If the C dimension of the ROI is &gt; 1 while allowMultiChannel parameter is set to <code>false</code>
     * @throws InterruptedException          if the thread was interrupted during the computation of the intensity descriptor
     * @throws Exception                     If the ROI dimension changed during the descriptor computation.
     */
    public static @NotNull IntensityDescriptorInfos computeIntensityDescriptors(final ROI roi, final Sequence sequence, final boolean allowMultiChannel) throws UnsupportedOperationException, InterruptedException {
        if (!allowMultiChannel && (roi.getBounds5D().getSizeC() > 1d))
            throw new UnsupportedOperationException("Not allowed to compute intensity descriptor on a multi channel ROI (sizeC > 1).");

        final IntensityDescriptorInfos result = new IntensityDescriptorInfos();

        long numPixels = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double sum = 0;
        double sum2 = 0;

        // FIXME: we were using interior pixels only, now we also use edge pixels so we can have intensities info
        // for intersection only ROI --> see if that is a good idea...
        final SequenceDataIterator it = new SequenceDataIterator(sequence, roi, true);

        while (!it.done()) {
            // check for interruption sometime
            if (((numPixels & 0xFFFF) == 0) && Thread.interrupted())
                throw new InterruptedException("ROI intensity descriptor computation interrupted.");

            final double value = it.get();

            if (min > value)
                min = value;
            if (max < value)
                max = value;
            sum += value;
            sum2 += value * value;
            numPixels++;

            it.next();
        }

        if (numPixels > 0) {
            result.min = min;
            result.max = max;
            result.sum = sum;

            final double mean = sum / numPixels;
            final double x1 = (sum2 / numPixels);
            final double x2 = mean * mean;

            result.mean = mean;
            result.deviation = Math.sqrt(x1 - x2);
        }
        else {
            result.min = 0d;
            result.mean = 0d;
            result.max = 0d;
            result.sum = 0d;
            result.deviation = 0d;
        }

        return result;
    }
}
