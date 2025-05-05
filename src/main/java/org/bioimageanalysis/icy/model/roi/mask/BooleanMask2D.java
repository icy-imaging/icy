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
package org.bioimageanalysis.icy.model.roi.mask;

import org.bioimageanalysis.icy.common.collection.array.DynamicArray;
import org.bioimageanalysis.icy.common.geom.point.Point2DUtil;
import org.bioimageanalysis.icy.common.type.TypeUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;

/**
 * Class to define a 2D boolean mask region and make basic boolean operation between masks.<br>
 * The bounds property of this object represents the region defined by the boolean mask.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class BooleanMask2D implements Cloneable {
    protected static final class Component {
        public DynamicArray.Int points;
        public List<Component> children;
        private Component root;

        public Component() {
            points = new DynamicArray.Int(0);
            children = new ArrayList<>();
            root = this;
        }

        public void addPoint(final int x, final int y) {
            points.addSingle(x);
            points.addSingle(y);
        }

        public void addComponent(final Component c) {
            final Component croot = c.root;

            if ((c != this) && (croot != root)) {
                children.add(croot);
                croot.setRoot(root);
            }
        }

        public boolean isRoot() {
            return root == this;
        }

        private void setRoot(final Component value) {
            root = value;

            for (final Component child : children)
                child.setRoot(value);
        }

        public int getTotalSize() {
            int result = points.getSize();

            for (final Component child : children)
                result += child.getTotalSize();

            return result;
        }

        public int[] getAllPoints() {
            final int[] result = new int[getTotalSize()];

            // get all point
            getAllPoints(result, 0);

            return result;
        }

        private int getAllPoints(final int[] result, final int offset) {
            final int[] intPoints = points.asArray();

            System.arraycopy(intPoints, 0, result, offset, intPoints.length);

            int off = offset + intPoints.length;
            for (final Component child : children)
                off = child.getAllPoints(result, off);

            return off;
        }
    }

    // find first non visited contour point
    private static int findStartPoint(final int startOffset, final boolean[] mask, final boolean[] visitedMask) {
        for (int i = startOffset; i < mask.length; i++)
            if (mask[i] && !visitedMask[i])
                return i;

        return -1;
    }

    private static List<Point> insertPoints(final List<Point> result, final List<Point> source) {
        if (result.isEmpty()) {
            result.addAll(source);
            return result;
        }
        // nothing to insert
        if (source.isEmpty())
            return result;

        final Point firstPointSource = source.get(0);
        final Point lastPointSource = source.get(source.size() - 1);
        final int len = result.size();

        for (int i = 0; i < len; i++) {
            final Point p = result.get(i);

            if (Point2DUtil.areConnected(p, firstPointSource)) {
                final List<Point> newResult = new ArrayList<>(result.subList(0, i + 1));
                newResult.addAll(source);
                if ((i + 1) < len)
                    newResult.addAll(new ArrayList<>(result.subList(i + 1, len)));
                return newResult;
            }
            if (Point2DUtil.areConnected(p, lastPointSource)) {
                final List<Point> newResult = new ArrayList<>(result.subList(0, i + 1));
                Collections.reverse(source);
                newResult.addAll(source);
                if ((i + 1) < len)
                    newResult.addAll(new ArrayList<>(result.subList(i + 1, len)));
                return newResult;
            }
        }

        return null;
    }

    private static List<Point> connect(final List<Point> result, final List<Point> source) {
        if (result.isEmpty()) {
            result.addAll(source);
            return result;
        }
        // nothing to connect
        if (source.isEmpty())
            return result;

        final Point2D firstPointResult = result.get(0);
        final Point2D lastPointResult = result.get(result.size() - 1);
        final Point2D firstPointSource = source.get(0);
        final Point2D lastPointSource = source.get(source.size() - 1);

        // res-tail - src-head connection --> res = res + src
        if (Point2DUtil.areConnected(firstPointSource, lastPointResult)) {
            result.addAll(source);
            return result;
        }
        // res-head - src-head connection --> res = reverse(src) + res
        if (Point2DUtil.areConnected(firstPointSource, firstPointResult)) {
            Collections.reverse(source);
            source.addAll(result);
            return source;
        }
        // res-head - src-tail connection --> res = src + res
        if (Point2DUtil.areConnected(lastPointSource, firstPointResult)) {
            source.addAll(result);
            return source;
        }
        // res-tail - src-tail connection --> res = res + reverse(src)
        if (Point2DUtil.areConnected(lastPointSource, lastPointResult)) {
            Collections.reverse(source);
            result.addAll(source);
            return result;
        }

        // can't connect
        return null;
    }

    /**
     * Return a list of Point representing the contour points of the mask.<br>
     * Points are returned in ascending XY order:
     *
     * <pre>
     *  1234 
     *  5  6
     *  7 8
     *   9
     * </pre>
     */
    public static List<Point> getContourPoints(final Rectangle bounds, final boolean[] mask) {
        if (bounds.isEmpty())
            return new ArrayList<>(1);

        final List<Point> points = new ArrayList<>(mask.length / 16);
        final int h = bounds.height;
        final int w = bounds.width;
        final int minx = bounds.x;
        final int miny = bounds.y;
        final int maxx = minx + (w - 1);
        final int maxy = miny + (h - 1);

        // cache
        boolean top; // = false;
        boolean bottom; // = false;
        boolean left; // = false;
        boolean right; // = false;
        boolean current;

        int offset = 0;

        // special case of single pixel mask
        if ((w == 1) && (h == 1)) {
            if (mask[0])
                points.add(new Point(minx, miny));
        }
        // special case of single pixel width mask
        else if (w == 1) {
            // first pixel of row
            top = false;
            current = mask[offset];
            bottom = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && bottom))
                points.add(new Point(minx, miny));

            // row
            for (int y = miny + 1; y < maxy; y++) {
                // cache
                top = current;
                current = bottom;
                bottom = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && bottom))
                    points.add(new Point(minx, y));
            }

            // cache
            top = current;
            current = bottom;
            bottom = false;

            // current pixel is a border ?
            if (current && !(top && bottom))
                points.add(new Point(minx, maxy));
        }
        // special case of single pixel height mask
        else if (h == 1) {
            // first pixel of line
            left = false;
            current = mask[offset];
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(left && right))
                points.add(new Point(minx, miny));

            // line
            for (int x = minx + 1; x < maxx; x++) {
                // cache
                left = current;
                current = right;
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(left && right))
                    points.add(new Point(x, miny));
            }

            // last pixel of first line
            left = current;
            current = right;
            right = false;

            // current pixel is a border ?
            if (current && !(left && right))
                points.add(new Point(maxx, miny));
        }
        // normal case
        else {
            // first pixel of first line
            top = false;
            left = false;
            current = mask[offset];
            bottom = mask[offset + w];
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(minx, miny));

            // first line
            for (int x = minx + 1; x < maxx; x++) {
                // cache
                left = current;
                current = right;
                bottom = mask[offset + w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(x, miny));
            }

            // last pixel of first line
            left = current;
            current = right;
            bottom = mask[offset + w];
            right = false;
            offset++;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(maxx, miny));

            for (int y = miny + 1; y < maxy; y++) {
                // first pixel of line
                left = false;
                current = mask[offset];
                top = mask[offset - w];
                bottom = mask[offset + w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(minx, y));

                for (int x = minx + 1; x < maxx; x++) {
                    // cache
                    left = current;
                    current = right;
                    top = mask[offset - w];
                    bottom = mask[offset + w];
                    right = mask[++offset];

                    // current pixel is a border ?
                    if (current && !(top && left && right && bottom))
                        points.add(new Point(x, y));
                }

                // last pixel of line
                left = current;
                current = right;
                top = mask[offset - w];
                bottom = mask[offset + w];
                right = false;
                offset++;

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(maxx, y));
            }

            // first pixel of last line
            left = false;
            current = mask[offset];
            top = mask[offset - w];
            bottom = false;
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(minx, maxy));

            // last line
            for (int x = minx + 1; x < maxx; x++) {
                // cache
                left = current;
                current = right;
                top = mask[offset - w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom))
                    points.add(new Point(x, maxy));
            }

            // last pixel of last line
            left = current;
            current = right;
            top = mask[offset - w];
            right = false;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom))
                points.add(new Point(maxx, maxy));
        }

        return points;
    }

    /**
     * Fast 2x up scaling (each point become 2x2 bloc points).<br>
     * This method create a new boolean mask.
     */
    public static BooleanMask2D upscale(final BooleanMask2D mask) throws InterruptedException {
        final Rectangle srcBounds;
        final boolean[] srcMask;
        final boolean[] resMask;

        //synchronized (mask) {
        srcBounds = mask.bounds;
        srcMask = mask.mask;
        //}

        final int srcW = srcBounds.width;
        final int srcH = srcBounds.height;

        resMask = new boolean[srcW * srcH * 2 * 2];

        int offSrc = 0;
        int offRes = 0;
        for (int y = 0; y < srcH; y++) {
            for (int x = 0; x < srcW; x++) {
                final boolean v = srcMask[offSrc++];

                // unpack in 4 points
                resMask[offRes/* + 0*/] = v;
                resMask[offRes + 1] = v;
                resMask[offRes + (srcW * 2)/* + 0*/] = v;
                resMask[offRes + (srcW * 2) + 1] = v;

                // next
                offRes += 2;
            }

            // check for interruption from time to time as this can be a long process
            if (((y & 0xF) == 0xF) && Thread.interrupted())
                throw new InterruptedException("BooleanMask.upscale(..) process interrupted.");

            // pass 1 line
            offRes += srcW * 2;
        }

        return new BooleanMask2D(new Rectangle(srcBounds.x * 2, srcBounds.y * 2, srcW * 2, srcH * 2), resMask);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point value).<br>
     * This method create a new int[] array returning the number of <code>true</code> point for each 2x2 block.
     *
     * @param mask
     *        the boolean mask to download
     */
    public static byte[] getDownscaleValues(final BooleanMask2D mask) throws InterruptedException {
        final Rectangle srcBounds;
        final boolean[] srcMask;
        final byte[] resMask;

        //synchronized (mask) {
        srcBounds = mask.bounds;
        srcMask = mask.mask;
        //}

        final int resW = srcBounds.width / 2;
        final int resH = srcBounds.height / 2;

        resMask = new byte[resW * resH];

        int offSrc = 0;
        int offRes = 0;
        for (int y = 0; y < resH; y++) {
            for (int x = 0; x < resW; x++) {
                byte v = 0;

                if (srcMask[offSrc/* + 0*/])
                    v++;
                if (srcMask[offSrc + 1])
                    v++;
                if (srcMask[offSrc + (resW * 2)/* + 0*/])
                    v++;
                if (srcMask[offSrc + (resW * 2) + 1])
                    v++;

                // pack in 1 point
                resMask[offRes++] = v;
                // next
                offSrc += 2;
            }

            // check for interruption from time to time as this can be a long process
            if (((y & 0xF) == 0xF) && Thread.interrupted())
                throw new InterruptedException("BooleanMask.getDownscaleValues(..) process interrupted.");

            // pass 1 line
            offSrc += resW * 2;
            // fix for odd width
            if ((srcBounds.width & 1) == 1)
                offSrc += 2;
        }

        return resMask;
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     *
     * @param mask
     *        the boolean mask to download
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1 to 4
     */
    public static BooleanMask2D downscale(final BooleanMask2D mask, final int nbPointForTrue) throws InterruptedException {
        final Rectangle srcBounds;

        //synchronized (mask) {
        srcBounds = mask.bounds;
        //}

        final int validPt = Math.min(Math.max(nbPointForTrue, 1), 4);
        final int resW = srcBounds.width / 2;
        final int resH = srcBounds.height / 2;

        final byte[] valueMask = getDownscaleValues(mask);
        final boolean[] resMask = new boolean[resW * resH];

        for (int i = 0; i < valueMask.length; i++)
            resMask[i] = valueMask[i] >= validPt;

        return new BooleanMask2D(new Rectangle(srcBounds.x / 2, srcBounds.y / 2, resW, resH), resMask);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     */
    public static BooleanMask2D downscale(final BooleanMask2D mask) throws InterruptedException {
        return downscale(mask, 2);
    }

    /**
     * Build global boolean mask from union of all specified mask
     */
    public static BooleanMask2D getUnion(final List<BooleanMask2D> masks) throws InterruptedException {
        BooleanMask2D result = null;

        // compute global union boolean mask of all ROI2D
        for (final BooleanMask2D bm : masks) {
            // update global mask
            if (result == null)
                result = new BooleanMask2D(bm.bounds, bm.mask);
            else
                result = getUnion(result, bm);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            result = new BooleanMask2D();

        return result;
    }

    /**
     * Build resulting mask from union of the mask1 and mask2.<br>
     * If <code>mask1</code> is <code>null</code> then a copy of <code>mask2</code> is returned.<br>
     * If <code>mask2</code> is <code>null</code> then a copy of <code>mask1</code> is returned.<br>
     * An empty mask is returned if both <code>mask1</code> and <code>mask2</code> are <code>null</code>.
     */
    public static BooleanMask2D getUnion(final BooleanMask2D mask1, final BooleanMask2D mask2) throws InterruptedException {
        if ((mask1 == null) && (mask2 == null))
            return new BooleanMask2D();

        if ((mask1 == null) || mask1.isEmpty())
            return (BooleanMask2D) mask2.clone();
        if ((mask2 == null) || mask2.isEmpty())
            return (BooleanMask2D) mask1.clone();

        return getUnion(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from union of the mask1 and mask2:
     *
     * <pre>
     *        mask1          +       mask2        =      result
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public static BooleanMask2D getUnion(final Rectangle bounds1, final boolean[] mask1, final Rectangle bounds2, final boolean[] mask2) throws InterruptedException {
        final Rectangle union = bounds1.union(bounds2);

        if (!union.isEmpty()) {
            final boolean[] mask = new boolean[union.width * union.height];
            int offDst, offSrc;

            // calculate offset
            offDst = ((bounds1.y - union.y) * union.width) + (bounds1.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds1.height; y++) {
                for (int x = 0; x < bounds1.width; x++)
                    mask[offDst + x] |= mask1[offSrc++];

                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getUnion(..) process interrupted.");

                offDst += union.width;
            }

            // calculate offset
            offDst = ((bounds2.y - union.y) * union.width) + (bounds2.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds2.height; y++) {
                for (int x = 0; x < bounds2.width; x++)
                    mask[offDst + x] |= mask2[offSrc++];

                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getUnion(..) process interrupted.");

                offDst += union.width;
            }

            return new BooleanMask2D(union, mask);
        }

        return new BooleanMask2D();
    }

    /**
     * Build global boolean mask from intersection of all specified mask
     */
    public static BooleanMask2D getIntersection(final List<BooleanMask2D> masks) throws InterruptedException {
        BooleanMask2D result = null;

        // compute global intersect boolean mask of all ROI2D
        for (final BooleanMask2D bm : masks) {
            // update global mask
            if (result == null)
                result = new BooleanMask2D(bm.bounds, bm.mask);
            else
                result = getIntersection(result, bm);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            result = new BooleanMask2D();

        return result;
    }

    /**
     * Build resulting mask from intersection of the mask1 and mask2.<br>
     * An empty mask is returned if <code>mask1</code> or <code>mask2</code> is <code>null</code>.
     */
    public static BooleanMask2D getIntersection(final BooleanMask2D mask1, final BooleanMask2D mask2) throws InterruptedException {
        if ((mask1 == null) || (mask2 == null))
            return new BooleanMask2D();

        return getIntersection(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from intersection of the mask1 and mask2:
     *
     * <pre>
     *        mask1     intersect     mask2      =        result
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     */
    public static BooleanMask2D getIntersection(final Rectangle bounds1, final boolean[] mask1, final Rectangle bounds2, final boolean[] mask2) throws InterruptedException {
        final Rectangle intersect = bounds1.intersection(bounds2);

        if (!intersect.isEmpty()) {
            final boolean[] mask = new boolean[intersect.width * intersect.height];

            // calculate offsets
            int off1 = ((intersect.y - bounds1.y) * bounds1.width) + (intersect.x - bounds1.x);
            int off2 = ((intersect.y - bounds2.y) * bounds2.width) + (intersect.x - bounds2.x);
            int off = 0;

            for (int y = 0; y < intersect.height; y++) {
                for (int x = 0; x < intersect.width; x++)
                    mask[off++] = mask1[off1 + x] & mask2[off2 + x];

                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getIntersection(..) process interrupted.");

                off1 += bounds1.width;
                off2 += bounds2.width;
            }

            return new BooleanMask2D(intersect, mask);
        }

        return new BooleanMask2D();
    }

    /**
     * Build global boolean mask from exclusive union of all specified mask
     */
    public static BooleanMask2D getExclusiveUnion(final List<BooleanMask2D> masks) throws InterruptedException {
        BooleanMask2D result = null;

        // compute global exclusive union boolean mask of all ROI2D
        for (final BooleanMask2D bm : masks) {
            // update global mask
            if (result == null)
                result = new BooleanMask2D(bm.bounds, bm.mask);
            else
                result = getExclusiveUnion(result, bm);
        }

        // return an empty BooleanMask2D instead of null
        if (result == null)
            result = new BooleanMask2D();

        return result;
    }

    /**
     * Build resulting mask from exclusive union of the mask1 and mask2.<br>
     * If <code>mask1</code> is <code>null</code> then a copy of <code>mask2</code> is returned.<br>
     * If <code>mask2</code> is <code>null</code> then a copy of <code>mask1</code> is returned.<br>
     * <code>null</code> is returned if both <code>mask1</code> and <code>mask2</code> are <code>null</code>.
     */
    public static BooleanMask2D getExclusiveUnion(final BooleanMask2D mask1, final BooleanMask2D mask2) throws InterruptedException {
        if ((mask1 == null) && (mask2 == null))
            return new BooleanMask2D();

        if ((mask1 == null) || mask1.isEmpty())
            return (BooleanMask2D) mask2.clone();
        if ((mask2 == null) || mask2.isEmpty())
            return (BooleanMask2D) mask1.clone();

        return getExclusiveUnion(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from exclusive union of the mask1 and mask2:
     *
     * <pre>
     *          mask1       xor      mask2        =       result
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public static BooleanMask2D getExclusiveUnion(final Rectangle bounds1, final boolean[] mask1, final Rectangle bounds2, final boolean[] mask2) throws InterruptedException {
        final Rectangle union = bounds1.union(bounds2);

        if (!union.isEmpty()) {
            final boolean[] mask = new boolean[union.width * union.height];
            int offDst, offSrc;

            // calculate offset
            offDst = ((bounds1.y - union.y) * union.width) + (bounds1.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds1.height; y++) {
                for (int x = 0; x < bounds1.width; x++)
                    mask[offDst + x] ^= mask1[offSrc++];

                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getExclusiveUnion(..) process interrupted.");

                offDst += union.width;
            }

            // calculate offset
            offDst = ((bounds2.y - union.y) * union.width) + (bounds2.x - union.x);
            offSrc = 0;

            for (int y = 0; y < bounds2.height; y++) {
                for (int x = 0; x < bounds2.width; x++)
                    mask[offDst + x] ^= mask2[offSrc++];

                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getExclusiveUnion(..) process interrupted.");

                offDst += union.width;
            }

            final BooleanMask2D result = new BooleanMask2D(union, mask);

            // optimize bounds
            result.optimizeBounds();

            return result;
        }

        return new BooleanMask2D();
    }

    /**
     * Build resulting mask from the subtraction of mask2 from mask1.<br>
     * If <code>mask2</code> is <code>null</code> then a copy of <code>mask1</code> is returned.<br>
     * If <code>mask1</code> is <code>null</code> then a empty mask is returned.
     */
    public static BooleanMask2D getSubtraction(final BooleanMask2D mask1, final BooleanMask2D mask2) throws InterruptedException {
        if (mask1 == null)
            return new BooleanMask2D();
        if (mask2 == null)
            return (BooleanMask2D) mask1.clone();

        return getSubtraction(mask1.bounds, mask1.mask, mask2.bounds, mask2.mask);
    }

    /**
     * Build resulting mask from the subtraction of mask2 from mask1:
     *
     * <pre>
     *        mask1          -        mask2       =  result
     *
     *     ################     ################
     *     ##############         ##############     ##
     *     ############             ############     ####
     *     ##########                 ##########     ######
     *     ########                     ########     ########
     *     ######                         ######     ######
     *     ####                             ####     ####
     *     ##                                 ##     ##
     * </pre>
     */
    public static BooleanMask2D getSubtraction(final Rectangle bounds1, final boolean[] mask1, final Rectangle bounds2, final boolean[] mask2) throws InterruptedException {
        final boolean[] mask = mask1.clone();
        final Rectangle subtract = new Rectangle(bounds1);
        final BooleanMask2D result = new BooleanMask2D(subtract, mask);

        // compute intersection
        final Rectangle intersection = bounds1.intersection(bounds2);

        // need to subtract something ?
        if (!intersection.isEmpty()) {
            // calculate offset
            int offDst = ((intersection.y - subtract.y) * subtract.width) + (intersection.x - subtract.x);
            int offSrc = ((intersection.y - bounds2.y) * bounds2.width) + (intersection.x - bounds2.x);

            for (int y = 0; y < intersection.height; y++) {
                for (int x = 0; x < intersection.width; x++)
                    mask[offDst + x] &= !mask2[offSrc + x];

                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getSubtraction(..) process interrupted.");

                offDst += subtract.width;
                offSrc += bounds2.width;
            }

            // optimize bounds
            result.optimizeBounds();
        }

        return result;
    }

    /**
     * Region represented by the mask.
     */
    public Rectangle bounds;
    /**
     * Boolean mask array.
     */
    public boolean[] mask;

    /**
     * Create an empty BooleanMask2D
     */
    public BooleanMask2D() {
        this(new Rectangle(), new boolean[0]);
    }

    public BooleanMask2D(final Rectangle bounds, final boolean[] mask) {
        super();

        this.bounds = bounds;
        this.mask = mask;
    }

    /**
     * Build a new boolean mask from the specified array of {@link Point}.<br>
     */
    public BooleanMask2D(final Point[] points) {
        super();

        if ((points == null) || (points.length == 0)) {
            bounds = new Rectangle();
            mask = new boolean[0];
        }
        else {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (final Point pt : points) {
                final int x = pt.x;
                final int y = pt.y;

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
            }

            bounds = new Rectangle(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
            mask = new boolean[bounds.width * bounds.height];

            for (final Point pt : points)
                mask[((pt.y - minY) * bounds.width) + (pt.x - minX)] = true;
        }
    }

    /**
     * Build a new boolean mask from the specified array of integer representing points.<br>
     * <br>
     * The array should contains point coordinates defined as follow:<br>
     * <code>result.length</code> = number of point * 2<br>
     * <code>result[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     */
    public BooleanMask2D(final int[] points) {
        super();

        if ((points == null) || (points.length == 0)) {
            bounds = new Rectangle();
            mask = new boolean[0];
        }
        else {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < points.length; i += 2) {
                final int x = points[i/* + 0*/];
                final int y = points[i + 1];

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
            }

            bounds = new Rectangle(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
            mask = new boolean[bounds.width * bounds.height];

            for (int i = 0; i < points.length; i += 2) {
                final int x = points[i/* + 0*/];
                final int y = points[i + 1];

                mask[((y - minY) * bounds.width) + (x - minX)] = true;
            }
        }
    }

    /**
     * Return true if boolean mask is empty<br>
     */
    public boolean isEmpty() {
        return bounds.isEmpty();
    }

    /**
     * Return true if mask contains the specified point
     */
    public boolean contains(final int x, final int y) {
        if (bounds.contains(x, y))
            return mask[(x - bounds.x) + ((y - bounds.y) * bounds.width)];

        return false;
    }

    /**
     * Return true if mask contains the specified 2D mask.
     */
    public boolean contains(final BooleanMask2D booleanMask) {
        return contains(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Return true if mask contains the specified 2D mask.
     */
    public boolean contains(final Rectangle rect, final boolean[] bmask) {
        final Rectangle intersect = bounds.intersection(rect);

        // intersection should be equal to rect
        if (intersect.equals(rect)) {
            // calculate offsets
            int off1 = ((intersect.y - bounds.y) * bounds.width) + (intersect.x - bounds.x);
            int off2 = ((intersect.y - rect.y) * rect.width) + (intersect.x - rect.x);

            for (int y = 0; y < intersect.height; y++) {
                for (int x = 0; x < intersect.width; x++)
                    if (bmask[off2 + x] && !mask[off1 + x])
                        return false;

                off1 += bounds.width;
                off2 += rect.width;
            }

            return true;
        }

        return false;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask.
     */
    public boolean intersects(final BooleanMask2D booleanMask) {
        return intersects(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask region.
     */
    public boolean intersects(final Rectangle rect, final boolean[] bmask) {
        final Rectangle intersect = bounds.intersection(rect);

        if (!intersect.isEmpty()) {
            // calculate offsets
            int off1 = ((intersect.y - bounds.y) * bounds.width) + (intersect.x - bounds.x);
            int off2 = ((intersect.y - rect.y) * rect.width) + (intersect.x - rect.x);

            for (int y = 0; y < intersect.height; y++) {
                for (int x = 0; x < intersect.width; x++)
                    if (mask[off1 + x] && bmask[off2 + x])
                        return true;

                off1 += bounds.width;
                off2 += rect.width;
            }
        }

        return false;
    }

    /**
     * Return the number of points contained in this boolean mask.
     */
    public int getNumberOfPoints() {
        int result = 0;

        for (int i = 0; i < mask.length; )
            if (mask[i++])
                result++;

        return result;
    }

    /**
     * Return an array of {@link Point} representing all points of the current mask.<br>
     * Points are returned in ascending XY order :
     *
     * <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     * </pre>
     *
     * @see #getPointsAsIntArray()
     */
    public Point[] getPoints() throws InterruptedException {
        return TypeUtil.toPoint(getPointsAsIntArray());
    }

    /**
     * Return an array of integer representing all points of the current mask.<br>
     * <code>result.length</code> = number of point * 2<br>
     * <code>result[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XY order :
     *
     * <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     * </pre>
     */
    public int[] getPointsAsIntArray() throws InterruptedException {
        if (bounds.isEmpty())
            return new int[0];

        final int[] points = new int[mask.length * 2];
        final int maxx = bounds.x + bounds.width;
        final int maxy = bounds.y + bounds.height;

        int pt = 0;
        int off = 0;
        for (int y = bounds.y; y < maxy; y++) {
            for (int x = bounds.x; x < maxx; x++) {
                if (mask[off++]) {
                    points[pt++] = x;
                    points[pt++] = y;
                }
            }

            // check for interruption from time to time as this can be a long process
            if (((y & 0xF) == 0xF) && Thread.interrupted())
                throw new InterruptedException("BooleanMask.set(..) process interrupted.");
        }

        final int[] result = new int[pt];

        System.arraycopy(points, 0, result, 0, pt);

        return result;
    }

    /**
     * Return a 2D array of integer representing points of each component of the current mask.<br>
     * A component is basically an isolated object which does not touch any other objects.<br>
     * Internal use only.
     */
    protected List<Component> getComponentsPointsInternal() throws InterruptedException {
        final List<Component> components = new ArrayList<>();
        final int w = bounds.width;
        final int minx = bounds.x;
        final int miny = bounds.y;

        // cache
        final Component[] line0 = new Component[w + 2];
        final Component[] line1 = new Component[w + 2];

        Arrays.fill(line0, null);
        Arrays.fill(line1, null);

        Component[] prevLine = line0;
        Component[] currLine = line1;

        Component left;
        Component top;
        Component topleft;

        int offset = 0;
        for (int y = 0; y < bounds.height; y++) {
            // prepare previous cache
            topleft = null;
            left = null;

            for (int x = 0; x < w; x++) {
                top = prevLine[x + 1];

                // we have a component pixel
                if (mask[offset++]) {
                    if (topleft != null) {
                        // merge component
                        if ((left != null) && (left != topleft))
                            topleft.addComponent(left);

                        left = topleft;
                    }
                    else if (top != null) {
                        // merge component
                        if ((left != null) && (left != top))
                            top.addComponent(left);

                        left = top;
                    }
                    else if (left == null) {
                        // new component
                        left = new Component();
                        components.add(left);
                    }

                    // add pixel to component
                    left.addPoint(x + minx, y + miny);
                }
                else {
                    // merge component
                    if ((left != null) && (top != null) && (left != top))
                        top.addComponent(left);

                    left = null;
                }

                topleft = top;
                // set current component index line cache
                currLine[x + 1] = left;
            }

            // check for interruption from time to time as this can be a long process
            if (((y & 0xF) == 0xF) && Thread.interrupted())
                throw new InterruptedException("BooleanMask.getComponentsPointsInternal() process interrupted.");

            final Component[] pl = prevLine;
            prevLine = currLine;
            currLine = pl;
        }

        final List<Component> result = new ArrayList<>();

        // remove partial components
        for (final Component component : components)
            if (component.isRoot())
                result.add(component);

        return result;
    }

    /**
     * Compute and return a 2D array of {@link Point} representing points of each component of the
     * current mask.<br>
     * A component is basically an isolated object which do not touch any other object.<br>
     * <br>
     * The array is returned in the following format :<br>
     * <code>result.lenght</code> = number of component.<br>
     * <code>result[c].length</code> = number of point of component c.<br>
     * <code>result[c][n]</code> = Point n of component c.<br>
     *
     * @param sorted
     *        When true points are returned in ascending XY order :
     *
     *        <pre>
     * Ymin  12 
     *      3456
     *       78
     * Ymax   9
     *        </pre>
     *
     * @see #getComponentsPointsAsIntArray()
     */
    public Point[][] getComponentsPoints(final boolean sorted) throws InterruptedException {
        if (bounds.isEmpty())
            return new Point[0][0];

        final Comparator<Point> pointComparator;

        if (sorted) {
            pointComparator = Comparator.comparingInt(p -> p.y);
        }
        else
            pointComparator = null;

        final int[][] cPoints = getComponentsPointsAsIntArray();
        final Point[][] cResult = new Point[cPoints.length][];

        for (int i = 0; i < cPoints.length; i++) {
            final Point[] result = TypeUtil.toPoint(cPoints[i]);

            // sort points
            if (pointComparator != null)
                Arrays.sort(result, pointComparator);

            cResult[i] = result;
        }

        return cResult;
    }

    /**
     * Compute and return a 2D array of integer representing points of each component of the
     * current mask.<br>
     * A component is basically an isolated object which do not touch any other object.<br>
     * <br>
     * The array is returned in the following format :<br>
     * <code>result.lenght</code> = number of component.<br>
     * <code>result[c].length</code> = number of point * 2 for component c.<br>
     * <code>result[c][(pt * 2) + 0]</code> = X coordinate for point <i>pt</i> of component
     * <i>c</i>.<br>
     * <code>result[c][(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i> of component
     * <i>c</i>.<br>
     *
     * @see #getComponentsPoints(boolean)
     */
    public int[][] getComponentsPointsAsIntArray() throws InterruptedException {
        if (bounds.isEmpty())
            return new int[0][0];

        final List<Component> components = getComponentsPointsInternal();
        final int[][] result = new int[components.size()][];

        // convert list of point to Point array
        for (int i = 0; i < result.length; i++)
            result[i] = components.get(i).getAllPoints();

        return result;
    }

    /**
     * Return an array of boolean mask representing each independent component of the current
     * mask.<br>
     * A component is basically an isolated object which does not touch any other objects.
     */
    public BooleanMask2D[] getComponents() throws InterruptedException {
        if (bounds.isEmpty())
            return new BooleanMask2D[0];

        final int[][] componentsPoints = getComponentsPointsAsIntArray();
        final List<BooleanMask2D> result = new ArrayList<>();

        // convert array of point to boolean mask
        for (final int[] componentPoints : componentsPoints)
            result.add(new BooleanMask2D(componentPoints));

        return result.toArray(new BooleanMask2D[0]);
    }

    /**
     * Returns a list of Point representing the contour points of the mask in connected order.<br>
     * Note that you should use this method carefully at it does not make any sense to use this method for mask
     * containing disconnected objects.<br>
     * Also it may returns incorrect result if the contour is not consistent (several connected contour possible).
     *
     * @see #getContourPoints()
     */
    public List<Point> getConnectedContourPoints() throws InterruptedException {
        final int[] intArrayPoints = getContourPointsAsIntArray();
        final List<List<Point>> allPoints = new ArrayList<>();

        // empty contour
        if (intArrayPoints.length == 0)
            return new ArrayList<>(0);

        final boolean[] contourMask;
        final Rectangle contourBounds;

        synchronized (this) {
            contourMask = new boolean[mask.length];
            contourBounds = bounds;
        }

        final int minx = contourBounds.x;
        final int miny = contourBounds.y;
        final int w = contourBounds.width;

        // build contour mask
        for (int i = 0; i < intArrayPoints.length; i += 2)
            contourMask[(intArrayPoints[i/* + 0*/] - minx) + ((intArrayPoints[i + 1] - miny) * w)] = true;

        final int maxx = minx + (w - 1);
        final int maxy = miny + (contourBounds.height - 1);
        // create visited pixel mask
        final boolean[] visitedMask = new boolean[contourMask.length];

        // first start point
        int startOffset = findStartPoint(0, contourMask, visitedMask);

        while (startOffset != -1) {
            final List<Point> points = new ArrayList<>(intArrayPoints.length / 2);

            int offset = startOffset;
            int x = (offset % w) + minx;
            int y = (offset / w) + miny;

            // add first point
            visitedMask[offset] = true;
            points.add(new Point(x, y));

            // scanning order
            // 567
            // 4x0
            // 321
            int scan = 0;
            int remain = 8;

            // scan connected pixels (8 points)
            while (remain-- > 0) {
                final int tmpOff;

                switch (scan & 7) {
                    case 0:
                        // not last X
                        if (x < maxx) {
                            tmpOff = offset + 1;

                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                x++;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 7 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 1:
                        // not last X and not last Y
                        if ((x < maxx) && (y < maxy)) {
                            tmpOff = offset + w + 1;
                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                x++;
                                y++;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 7 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 2:
                        // not last Y
                        if (y < maxy) {
                            tmpOff = offset + w;
                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                y++;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 0; // 1 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 3:
                        // not first X and not last Y
                        if ((x > minx) && (y < maxy)) {
                            tmpOff = (offset + w) - 1;

                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                x--;
                                y++;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 0; // 1 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 4:
                        // not first X
                        if (x > minx) {
                            tmpOff = offset - 1;
                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                x--;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 3 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 5:
                        // not first X and not first Y
                        if ((x > minx) && (y > miny)) {
                            tmpOff = offset - (w + 1);
                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                x--;
                                y--;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 3 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 6:
                        // not first Y
                        if (y > miny) {
                            tmpOff = offset - w;
                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                y--;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 5 - 1;
                                remain = 8;
                            }
                        }
                        break;

                    case 7:
                        // not last X and not first Y
                        if ((x < maxx) && (y > miny)) {
                            tmpOff = (offset - w) + 1;
                            // contour ?
                            if (contourMask[tmpOff]) {
                                // contour already visited --> done
                                if (visitedMask[tmpOff]) {
                                    remain = 0;
                                    break;
                                }

                                // set new position
                                x++;
                                y--;
                                offset = tmpOff;
                                // mark as visited
                                visitedMask[offset] = true;
                                // and add point
                                points.add(new Point(x, y));
                                // change next scan pos
                                scan = 5 - 1;
                                remain = 8;
                            }
                        }
                        break;
                }

                scan = (scan + 1) & 7;
            }

            allPoints.add(points);
            // get next start point
            startOffset = findStartPoint(startOffset, contourMask, visitedMask);
        }

        List<Point> result = new ArrayList<>(allPoints.get(0));
        allPoints.remove(0);

        // connect all found paths
        int i = 0;
        while (i < allPoints.size()) {
            final List<Point> newResult = connect(result, allPoints.get(i));

            if (newResult != null) {
                result = newResult;
                allPoints.remove(i);
                i = 0;
            }
            else
                i++;
        }

        // try to insert remaining paths
        i = 0;
        while (i < allPoints.size()) {
            final List<Point> newResult = insertPoints(result, allPoints.get(i));

            if (newResult != null) {
                result = newResult;
                allPoints.remove(i);
                i = 0;
            }
            else
                i++;
        }

        // debug
        // for(Point pt:result)
        // System.out.println(pt);

        // some parts can't be connected --> display warning
        // if (!allPoints.isEmpty())
        // System.out.println("Warning: can't connect some points...");

        return result;
    }

    /**
     * Returns an array of {@link Point} containing the contour points of the mask.<br>
     * Points are returned in ascending XY order:
     *
     * <pre>
     *  123 
     *  4 5
     *  6 7
     *   89
     * </pre>
     *
     * @see #getContourPointsAsIntArray()
     */
    public Point[] getContourPoints() throws InterruptedException {
        return TypeUtil.toPoint(getContourPointsAsIntArray());
    }

    /**
     * Returns an array of integer containing the contour points of the mask.<br>
     * <code>result.length</code> = number of point * 2<br>
     * <code>result[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XY order:
     *
     * <pre>
     *  123 
     *  4 5
     *  6 7
     *   89
     * </pre>
     *
     * @see #getConnectedContourPoints()
     */
    public int[] getContourPointsAsIntArray() throws InterruptedException {
        if (isEmpty())
            return new int[0];

        final boolean[] mask;
        final Rectangle bounds;

        synchronized (this) {
            mask = this.mask;
            bounds = this.bounds;
        }

        final int[] points = new int[mask.length * 2];
        final int h = bounds.height;
        final int w = bounds.width;
        final int maxx = bounds.x + (w - 1);
        final int maxy = bounds.y + (h - 1);

        // cache
        boolean top; // = false;
        boolean bottom; // = false;
        boolean left; // = false;
        boolean right; // = false;
        boolean current;

        int offset = 0;
        int pt = 0;

        // special case where width <= 2 or height <= 2 in which case all pixels count as border
        if ((w <= 2) || (h <= 2)) {
            for (int y = bounds.y; y <= maxy; y++) {
                for (int x = bounds.x; x <= maxx; x++) {
                    // current pixel is a border ?
                    if (mask[offset++]) {
                        points[pt++] = x;
                        points[pt++] = y;
                    }
                }
            }
        }
        else {
            // first pixel of first line
            top = false;
            left = false;
            current = mask[offset];
            bottom = mask[offset + w];
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom)) {
                points[pt++] = bounds.x;
                points[pt++] = bounds.y;
            }

            // first line
            for (int x = bounds.x + 1; x < maxx; x++) {
                // cache
                left = current;
                current = right;
                bottom = mask[offset + w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom)) {
                    points[pt++] = x;
                    points[pt++] = bounds.y;
                }
            }

            // last pixel of first line
            left = current;
            current = right;
            bottom = mask[offset + w];
            right = false;
            offset++;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom)) {
                points[pt++] = maxx;
                points[pt++] = bounds.y;
            }

            for (int y = bounds.y + 1; y < maxy; y++) {
                // check for interruption from time to time as this can be a long process
                if (((y & 0xF) == 0xF) && Thread.interrupted())
                    throw new InterruptedException("BooleanMask.getContourPointsAsIntArray() process interrupted.");

                // first pixel of line
                left = false;
                current = mask[offset];
                top = mask[offset - w];
                bottom = mask[offset + w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom)) {
                    points[pt++] = bounds.x;
                    points[pt++] = y;
                }

                for (int x = bounds.x + 1; x < maxx; x++) {
                    // cache
                    left = current;
                    current = right;
                    top = mask[offset - w];
                    bottom = mask[offset + w];
                    right = mask[++offset];

                    // current pixel is a border ?
                    if (current && !(top && left && right && bottom)) {
                        points[pt++] = x;
                        points[pt++] = y;
                    }
                }

                // last pixel of line
                left = current;
                current = right;
                top = mask[offset - w];
                bottom = mask[offset + w];
                right = false;
                offset++;

                // current pixel is a border ?
                if (current && !(top && left && right && bottom)) {
                    points[pt++] = maxx;
                    points[pt++] = y;
                }
            }

            // first pixel of last line
            left = false;
            current = mask[offset];
            top = mask[offset - w];
            bottom = false;
            right = mask[++offset];

            // current pixel is a border ?
            if (current && !(top && left && right && bottom)) {
                points[pt++] = bounds.x;
                points[pt++] = maxy;
            }

            // last line
            for (int x = bounds.x + 1; x < maxx; x++) {
                // cache
                left = current;
                current = right;
                top = mask[offset - w];
                right = mask[++offset];

                // current pixel is a border ?
                if (current && !(top && left && right && bottom)) {
                    points[pt++] = x;
                    points[pt++] = maxy;
                }
            }

            // last pixel of last line
            left = current;
            current = right;
            top = mask[offset - w];
            right = false;

            // current pixel is a border ?
            if (current && !(top && left && right && bottom)) {
                points[pt++] = maxx;
                points[pt++] = maxy;
            }
        }

        final int[] result = new int[pt];

        System.arraycopy(points, 0, result, 0, pt);

        return result;
    }

    /**
     * Computes and returns the length of the contour.<br>
     * This is different from the number of contour point as it takes care of approximating
     * correctly distance between each contour point.
     *
     * @return the length of the contour
     */
    public double getContourLength() throws InterruptedException {
        double perimeter = 0;

        final int[] edge = getContourPointsAsIntArray();
        final int baseX = bounds.x;
        final int baseY = bounds.y;
        final int width = bounds.width;
        final int height = bounds.height;

        // count the edges and corners in 2D/3D
        double sideEdges = 0, cornerEdges = 0;

        for (int i = 0; i < edge.length; i += 2) {
            final int x = edge[i/* + 0*/] - baseX;
            final int y = edge[i + 1] - baseY;
            final int xy = x + (y * width);

            final boolean topLeftConnected;
            final boolean topConnected;
            final boolean topRightConnected;
            final boolean bottomLeftConnected;
            final boolean bottomConnected;
            final boolean bottomRightConnected;

            if (y != 0) {
                topLeftConnected = (x != 0) && mask[(xy - 1) - width];
                topConnected = mask[(xy/* + 0*/) - width];
                topRightConnected = (x != (width - 1)) && mask[(xy + 1) - width];
            }
            else {
                topLeftConnected = false;
                topConnected = false;
                topRightConnected = false;
            }

            final boolean leftConnected = (x != 0) && mask[xy - 1];
            final boolean rightConnected = (x != (width - 1)) && mask[xy + 1];

            if (y != (height - 1)) {
                bottomLeftConnected = (x != 0) && mask[(xy - 1) + width];
                bottomConnected = mask[(xy/* + 0*/) + width];
                bottomRightConnected = (x != (width - 1)) && mask[(xy + 1) + width];
            }
            else {
                bottomLeftConnected = false;
                bottomConnected = false;
                bottomRightConnected = false;
            }

            // count the connections
            int directConnection = 0;
            int diagConnection = 0;

            if (topLeftConnected)
                diagConnection++;
            if (topConnected)
                directConnection++;
            if (topRightConnected)
                diagConnection++;
            if (leftConnected)
                directConnection++;
            if (rightConnected)
                directConnection++;
            if (bottomLeftConnected)
                diagConnection++;
            if (bottomConnected)
                directConnection++;
            if (bottomRightConnected)
                diagConnection++;

            switch (directConnection) {
                case 0: // no direct connection
                    switch (diagConnection) {
                        case 0: // isolated point
                            perimeter += Math.PI;
                            break;

                        case 1: // ending point (diagonal)
                            cornerEdges++;
                            perimeter += Math.sqrt(2) + (Math.PI / 2);
                            break;

                        default: // diagonal angle line
                            cornerEdges += 2;
                            perimeter += 2 * Math.sqrt(2);
                            break;
                    }
                    break;

                case 1: // ending point
                    switch (diagConnection) {
                        case 0: // ending point straight
                            sideEdges++;
                            perimeter += 1 + (Math.PI / 2);
                            break;

                        default: // assume triangle with 45&deg; angle
                            cornerEdges++;
                            sideEdges++;
                            perimeter += 1 + Math.sqrt(2);
                            break;
                    }
                    break;

                case 2:
                    if ((leftConnected && rightConnected) || (topConnected && bottomConnected)) {
                        final double dgc = diagConnection * 0.5;
                        final double dtc = 2 - dgc;

                        cornerEdges += dgc;
                        sideEdges += dtc;
                        perimeter += dtc + (dgc * Math.sqrt(2));
                    }
                    else {
                        // consider 90&deg; corner
                        cornerEdges++;
                        perimeter += Math.sqrt(2);
                    }
                    break;

                case 3: // classic border (180&deg;)
                    switch (diagConnection) {
                        default: // classic border
                            sideEdges++;
                            perimeter++;
                            break;

                        case 3:
                            // consider 225&deg; interior corner
                            cornerEdges += 0.5;
                            sideEdges += 0.5;
                            perimeter += 0.5 + (Math.sqrt(2) / 2);
                            break;

                        case 4: // hole inside contour
                            cornerEdges++;
                            perimeter += Math.sqrt(2);
                            break;
                    }
                    break;

                case 4: // internal point --> should not happen
                    break;
            }
        }

        // adjust the perimeter empirically according to the edge distribution
        final double overShoot = Math.min(sideEdges / 10, cornerEdges);

        return perimeter - overShoot;
    }

    /**
     * Compute intersection with specified mask and return result in a new mask.
     *
     * @see #intersect(BooleanMask2D)
     */
    public BooleanMask2D getIntersection(final BooleanMask2D booleanMask) throws InterruptedException {
        return getIntersection(this, booleanMask);
    }

    /**
     * Compute intersection with specified mask and return result in a new mask.
     *
     * @see #intersect(Rectangle, boolean[])
     */
    public BooleanMask2D getIntersection(final Rectangle bounds, final boolean[] mask) throws InterruptedException {
        return getIntersection(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Compute union with specified mask and return result in a new mask.
     *
     * @see #add(BooleanMask2D)
     */
    public BooleanMask2D getUnion(final BooleanMask2D booleanMask) throws InterruptedException {
        return getUnion(this, booleanMask);
    }

    /**
     * Compute union with specified mask and return result in a new mask.
     *
     * @see #add(Rectangle, boolean[])
     */
    public BooleanMask2D getUnion(final Rectangle bounds, final boolean[] mask) throws InterruptedException {
        return getUnion(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Compute exclusive union operation with specified mask and return result in a new mask.
     *
     * @see #exclusiveAdd(BooleanMask2D)
     */
    public BooleanMask2D getExclusiveUnion(final BooleanMask2D booleanMask) throws InterruptedException {
        return getExclusiveUnion(this, booleanMask);
    }

    /**
     * Compute exclusive union operation with specified mask and return result in a new mask
     *
     * @see #exclusiveAdd(Rectangle, boolean[])
     */
    public BooleanMask2D getExclusiveUnion(final Rectangle bounds, final boolean[] mask) throws InterruptedException {
        return getExclusiveUnion(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Subtract the specified mask from current and return result in a new mask.
     */
    public BooleanMask2D getSubtraction(final BooleanMask2D mask) throws InterruptedException {
        return getSubtraction(this, mask);
    }

    /**
     * Subtract the specified mask from current and return result in a new mask.
     */
    public BooleanMask2D getSubtraction(final Rectangle bounds, final boolean[] mask) throws InterruptedException {
        return getSubtraction(this.bounds, this.mask, bounds, mask);
    }

    /**
     * Add the specified mask into the current mask (bounds can be enlarged):
     *
     * <pre>
     *       current mask    +         mask       =       result
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public void add(final BooleanMask2D booleanMask) throws InterruptedException {
        add(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Add the specified mask into the current mask (bounds can be enlarged):
     *
     * <pre>
     *       current mask    +         mask       =       result
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public void add(final Rectangle boundsToAdd, final boolean[] maskToAdd) throws InterruptedException {
        // don't need to reallocate the mask
        if (bounds.contains(boundsToAdd)) {
            int offDst, offSrc;

            // calculate offset
            offDst = ((boundsToAdd.y - bounds.y) * bounds.width) + (boundsToAdd.x - bounds.x);
            offSrc = 0;

            for (int y = 0; y < boundsToAdd.height; y++) {
                for (int x = 0; x < boundsToAdd.width; x++)
                    if (maskToAdd[offSrc++])
                        mask[offDst + x] = true;

                offDst += bounds.width;
            }
        }
        else {
            // create a new mask
            final BooleanMask2D result = getUnion(boundsToAdd, maskToAdd);

            // update bounds and mask
            synchronized (this) {
                this.bounds = result.bounds;
                this.mask = result.mask;
            }
        }
    }

    /**
     * Set the content of current mask with the result of the intersection with the specified mask:
     *
     * <pre>
     *     current mask  intersect     newMask       =       result
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     */
    public void intersect(final BooleanMask2D booleanMask) throws InterruptedException {
        intersect(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Set the content of current mask with the result of the intersection with the specified mask:
     *
     * <pre>
     *     current mask  intersect     newMask       =       result
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     */
    public void intersect(final Rectangle boundsToIntersect, final boolean[] maskToIntersect) throws InterruptedException {
        // faster to always create a new mask
        final BooleanMask2D result = getIntersection(boundsToIntersect, maskToIntersect);

        synchronized (this) {
            this.bounds = result.bounds;
            this.mask = result.mask;
        }
    }

    /**
     * Exclusively add the specified mask into the current mask (bounds can change):
     *
     * <pre>
     *          mask1       xor      mask2        =       result
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public void exclusiveAdd(final BooleanMask2D booleanMask) throws InterruptedException {
        exclusiveAdd(booleanMask.bounds, booleanMask.mask);
    }

    /**
     * Exclusively add the specified mask into the current mask (bounds can change):
     *
     * <pre>
     *          mask1       xor      mask2        =       result
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     */
    public void exclusiveAdd(final Rectangle boundsToXAdd, final boolean[] maskToXAdd) throws InterruptedException {
        // don't need to reallocate the mask
        if (bounds.contains(boundsToXAdd)) {
            int offDst, offSrc;

            // calculate offset
            offDst = ((boundsToXAdd.y - bounds.y) * bounds.width) + (boundsToXAdd.x - bounds.x);
            offSrc = 0;

            for (int y = 0; y < boundsToXAdd.height; y++) {
                for (int x = 0; x < boundsToXAdd.width; x++)
                    if (maskToXAdd[offSrc++])
                        mask[offDst + x] = !mask[offDst + x];

                offDst += bounds.width;
            }

            // bounds may have changed
            optimizeBounds();
        }
        else {
            // create a new mask
            final BooleanMask2D result = getExclusiveUnion(boundsToXAdd, maskToXAdd);

            // update bounds and mask
            synchronized (this) {
                this.bounds = result.bounds;
                this.mask = result.mask;
            }
        }
    }

    /**
     * Subtract the specified mask from the current mask (bounds can change):
     *
     * <pre>
     *       current mask    -        mask        =  result
     *
     *     ################     ################
     *     ##############         ##############     ##
     *     ############             ############     ####
     *     ##########                 ##########     ######
     *     ########                     ########     ########
     *     ######                         ######     ######
     *     ####                             ####     ####
     *     ##                                 ##     ##
     * </pre>
     */
    public void subtract(final Rectangle boundsToSubtract, final boolean[] maskToSubtract) {
        // compute intersection
        final Rectangle intersection = bounds.intersection(boundsToSubtract);

        // need to subtract something ?
        if (!intersection.isEmpty()) {
            // calculate offset
            int offDst = ((intersection.y - bounds.y) * bounds.width) + (intersection.x - bounds.x);
            int offSrc = ((intersection.y - boundsToSubtract.y) * boundsToSubtract.width)
                    + (intersection.x - boundsToSubtract.x);

            for (int y = 0; y < intersection.height; y++) {
                for (int x = 0; x < intersection.width; x++)
                    if (maskToSubtract[offSrc + x])
                        mask[offDst + x] = false;

                offDst += bounds.width;
                offSrc += boundsToSubtract.width;
            }

            // optimize bounds
            optimizeBounds();
        }
    }

    /**
     * Get the smallest bounds which fit mask content.
     */
    public Rectangle getOptimizedBounds() {
        // find best bounds
        final int sizeX = bounds.width;
        final int sizeY = bounds.height;

        int minX = sizeX;
        int minY = sizeY;
        int maxX = -1;
        int maxY = -1;
        int offset = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                if (mask[offset++]) {
                    if (x < minX)
                        minX = x;
                    if (x > maxX)
                        maxX = x;
                    if (y < minY)
                        minY = y;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }

        // empty --> return empty bounds
        if (minX == sizeX)
            return new Rectangle(bounds.x, bounds.y, 0, 0);

        // new calculated bounds
        return new Rectangle(bounds.x + minX, bounds.y + minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

    /**
     * Optimize mask bounds so it fit mask content.
     */
    public void optimizeBounds() {
        moveBounds(getOptimizedBounds());
    }

    /**
     * Change the bounds of BooleanMask.<br>
     * Keep mask data intersecting from old bounds.
     */
    public void moveBounds(final Rectangle value) {
        // bounds changed ?
        if (!bounds.equals(value)) {
            // copy bounds as we modify them
            final Rectangle oldBounds = new Rectangle(bounds);
            final Rectangle newBounds = new Rectangle(value);

            // replace to origin (relative to old bounds)
            oldBounds.translate(-bounds.x, -bounds.y);
            newBounds.translate(-bounds.x, -bounds.y);

            final boolean[] newMask = new boolean[Math.max(0, newBounds.width) * Math.max(0, newBounds.height)];
            final Rectangle intersect = newBounds.intersection(oldBounds);

            if (!intersect.isEmpty()) {
                int offSrc = 0;
                int offDst = 0;

                // adjust offset in source mask
                if (intersect.x > 0)
                    offSrc += intersect.x;
                if (intersect.y > 0)
                    offSrc += intersect.y * oldBounds.width;
                // adjust offset in destination mask
                if (newBounds.x < 0)
                    offDst -= newBounds.x;
                if (newBounds.y < 0)
                    offDst += -newBounds.y * newBounds.width;

                // preserve data
                for (int j = 0; j < intersect.height; j++) {
                    System.arraycopy(mask, offSrc, newMask, offDst, intersect.width);

                    offSrc += oldBounds.width;
                    offDst += newBounds.width;
                }
            }

            // update mask and bounds
            synchronized (this) {
                mask = newMask;
                bounds = value;
            }
        }
    }

    /**
     * Fast 2x up scaling (each point become 2x2 bloc point).<br>
     * This method create a new boolean mask.
     */
    public BooleanMask2D upscale() throws InterruptedException {
        return upscale(this);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     *
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1-4 (default is 3)
     */
    public BooleanMask2D downscale(final int nbPointForTrue) throws InterruptedException {
        return downscale(this, nbPointForTrue);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     */
    public BooleanMask2D downscale() throws InterruptedException {
        return downscale(this);
    }

    @Override
    public Object clone() {
        return new BooleanMask2D((Rectangle) bounds.clone(), mask.clone());
    }

}
