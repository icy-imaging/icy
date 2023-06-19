package icy.roi;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.collection.array.DynamicArray;
import icy.type.point.Point3D;
import icy.type.rectangle.Rectangle3D;
import plugins.kernel.roi.roi3d.ROI3DArea;

/**
 * Class to define a 3D boolean mask region and make basic boolean operation between masks.<br>
 * The bounds property of this object represents the region defined by the boolean mask.
 * 
 * @author Stephane
 */
public class BooleanMask3D implements Cloneable
{

    /**
     * A temporary component structure used during the extraction process. Many such components may
     * be extracted from the sequence, and some are fused into the final extracted ROI
     */
    private static class ConnectedComponent
    {

        /**
         * final label that should replace the current label if fusion is needed
         */
        int targetLabel;

        /**
         * if non-null, indicates the parent object with which the current object should be fused
         */
        ConnectedComponent targetComponent;

        /**
         * Creates a new label with the given value. If no parent is set to this label, the given
         * value will be the final one
         * 
         * @param value
         *        the pixel value
         * @param label
         *        the label value
         */
        ConnectedComponent(int label)
        {
            this.targetLabel = label;
        }

        /**
         * Retrieves the final object label (recursively)
         * 
         * @return
         */
        int getTargetLabel()
        {
            return targetComponent == null ? targetLabel : targetComponent.getTargetLabel();
        }
    }

    // Internal use only
    private static BooleanMask2D doUnion2D(BooleanMask2D m1, BooleanMask2D m2) throws InterruptedException
    {
        if (m1 == null)
        {
            // only use the 2D mask from second mask
            if (m2 != null)
                return (BooleanMask2D) m2.clone();

            return null;
        }
        else if (m2 == null)
            // only use the 2D mask from first mask
            return (BooleanMask2D) m1.clone();

        // process union of 2D mask
        return BooleanMask2D.getUnion(m1, m2);
    }

    // Internal use only
    private static BooleanMask2D doIntersection2D(BooleanMask2D m1, BooleanMask2D m2) throws InterruptedException
    {
        if ((m1 == null) || (m2 == null))
            return null;

        // process intersection of 2D mask
        return BooleanMask2D.getIntersection(m1, m2);
    }

    // Internal use only
    private static BooleanMask2D doExclusiveUnion2D(BooleanMask2D m1, BooleanMask2D m2) throws InterruptedException
    {
        if (m1 == null)
        {
            // only use the 2D mask from second mask
            if (m2 != null)
                return (BooleanMask2D) m2.clone();

            return null;
        }
        else if (m2 == null)
            // only use the 2D mask from first mask
            return (BooleanMask2D) m1.clone();

        // process exclusive union of 2D mask
        return BooleanMask2D.getExclusiveUnion(m1, m2);
    }

    // Internal use only
    private static BooleanMask2D doSubtraction2D(BooleanMask2D m1, BooleanMask2D m2) throws InterruptedException
    {
        if (m1 == null)
            return null;
        // only use the 2D mask from first mask
        if (m2 == null)
            return (BooleanMask2D) m1.clone();

        // process subtraction of 2D mask
        return BooleanMask2D.getSubtraction(m1, m2);
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
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D getUnion(BooleanMask3D mask1, BooleanMask3D mask2) throws InterruptedException
    {
        if ((mask1 == null) && (mask2 == null))
            return new BooleanMask3D();

        if ((mask1 == null) || mask1.isEmpty())
            return (BooleanMask3D) mask2.clone();
        if ((mask2 == null) || mask2.isEmpty())
            return (BooleanMask3D) mask1.clone();

        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createUnion(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doUnion2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doUnion2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
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
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D getIntersection(BooleanMask3D mask1, BooleanMask3D mask2) throws InterruptedException
    {
        if ((mask1 == null) || (mask2 == null))
            return new BooleanMask3D();

        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createIntersection(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doIntersection2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doIntersection2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
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
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D getExclusiveUnion(BooleanMask3D mask1, BooleanMask3D mask2) throws InterruptedException
    {
        if ((mask1 == null) && (mask2 == null))
            return new BooleanMask3D();

        if ((mask1 == null) || mask1.isEmpty())
            return (BooleanMask3D) mask2.clone();
        if ((mask2 == null) || mask2.isEmpty())
            return (BooleanMask3D) mask1.clone();

        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createUnion(mask2.bounds);

        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doExclusiveUnion2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doExclusiveUnion2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return new BooleanMask3D();
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
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D getSubtraction(BooleanMask3D mask1, BooleanMask3D mask2) throws InterruptedException
    {
        if (mask1 == null)
            return new BooleanMask3D();
        if (mask2 == null)
            return (BooleanMask3D) mask1.clone();

        final Rectangle3D.Integer bounds = (Rectangle3D.Integer) mask1.bounds.createIntersection(mask2.bounds);

        // need to subtract something ?
        if (!bounds.isEmpty())
        {
            final BooleanMask2D[] mask;

            // special case of infinite Z dimension
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // we can allow merge ROI only if they both has infinite Z dimension
                if ((mask1.bounds.sizeZ != Integer.MAX_VALUE) || (mask2.bounds.sizeZ != Integer.MAX_VALUE))
                    throw new UnsupportedOperationException(
                            "Cannot merge an infinite Z dimension ROI with  a finite Z dimension ROI");

                mask = new BooleanMask2D[1];

                final BooleanMask2D m2d1 = mask1.mask.firstEntry().getValue();
                final BooleanMask2D m2d2 = mask2.mask.firstEntry().getValue();

                mask[0] = doSubtraction2D(m2d1, m2d2);
            }
            else
            {
                mask = new BooleanMask2D[bounds.sizeZ];

                for (int z = 0; z < bounds.sizeZ; z++)
                {
                    final BooleanMask2D m2d1 = mask1.getMask2D(z + bounds.z);
                    final BooleanMask2D m2d2 = mask2.getMask2D(z + bounds.z);

                    mask[z] = doSubtraction2D(m2d1, m2d2);
                }
            }

            return new BooleanMask3D(bounds, mask);
        }

        return (BooleanMask3D) mask1.clone();
    }

    /**
     * Fast 2x up scaling (each point become 2x2x2 bloc points).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D upscale(BooleanMask3D mask) throws InterruptedException
    {
        final TreeMap<Integer, BooleanMask2D> srcMask = mask.mask;
        final TreeMap<Integer, BooleanMask2D> resMask = new TreeMap<Integer, BooleanMask2D>();

        synchronized (mask)
        {
            final int minZ = srcMask.firstKey().intValue();
            final int maxZ = srcMask.lastKey().intValue();

            // single Z --> check for special MAX_INTEGER case
            if ((minZ == maxZ) && (mask.bounds.sizeZ == Integer.MAX_VALUE))
            {
                // put up scaled version for all Z
                resMask.put(Integer.valueOf(Integer.MIN_VALUE), srcMask.firstEntry().getValue().upscale());
            }
            else
            {
                for (Entry<Integer, BooleanMask2D> entry : srcMask.entrySet())
                {
                    final int key = entry.getKey().intValue();
                    // get upscaled 2D mask
                    final BooleanMask2D bm = entry.getValue().upscale();

                    // duplicate it at (Z pos) * 2
                    resMask.put(Integer.valueOf((key * 2) + 0), bm);
                    resMask.put(Integer.valueOf((key * 2) + 1), (BooleanMask2D) bm.clone());
                }
            }
        }

        return new BooleanMask3D(resMask);
    }

    /**
     * Internal use only
     * 
     * @throws InterruptedException
     */
    protected static BooleanMask2D mergeForDownscale(TreeMap<Integer, BooleanMask2D> masks, int destZ,
            int nbPointForTrue) throws InterruptedException
    {
        final BooleanMask2D bm1 = masks.get(Integer.valueOf((destZ * 2) + 0));
        final BooleanMask2D bm2 = masks.get(Integer.valueOf((destZ * 2) + 1));
        final Rectangle bounds;

        if (bm1 == null)
        {
            if (bm2 == null)
                return null;

            bounds = new Rectangle(bm2.bounds);
        }
        else if (bm2 == null)
            bounds = new Rectangle(bm1.bounds);
        else
            bounds = bm1.bounds.union(bm2.bounds);

        final byte[] maskValues1;
        final byte[] maskValues2;
        final int resW = bounds.width / 2;
        final int resH = bounds.height / 2;

        // get mask values from both mask
        if (bm1 != null)
        {
            bm1.moveBounds(bounds);
            maskValues1 = BooleanMask2D.getDownscaleValues(bm1);
        }
        else
            maskValues1 = new byte[resW * resH];
        if (bm2 != null)
        {
            bm2.moveBounds(bounds);
            maskValues2 = BooleanMask2D.getDownscaleValues(bm2);
        }
        else
            maskValues2 = new byte[resW * resH];

        final int validPt = Math.min(Math.max(nbPointForTrue, 1), 8);

        final boolean[] resMask = new boolean[resW * resH];

        for (int i = 0; i < resMask.length; i++)
            resMask[i] = (maskValues1[i] + maskValues2[i]) >= validPt;

        return new BooleanMask2D(new Rectangle(bounds.x / 2, bounds.y / 2, resW, resH), resMask);
    }

    /**
     * Fast 2x down scaling (each 2x2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     * 
     * @param mask
     *        the boolean mask to download
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1 to 8 (default is 5)
     * @throws InterruptedException
     */
    public static BooleanMask3D downscale(BooleanMask3D mask, int nbPointForTrue) throws InterruptedException
    {
        final TreeMap<Integer, BooleanMask2D> srcMask = mask.mask;
        final TreeMap<Integer, BooleanMask2D> resMask = new TreeMap<Integer, BooleanMask2D>();

        synchronized (mask)
        {
            final int minZ = srcMask.firstKey().intValue();
            final int maxZ = srcMask.lastKey().intValue();

            // single Z --> check for special MAX_INTEGER case
            if ((minZ == maxZ) && (mask.bounds.sizeZ == Integer.MAX_VALUE))
                // put down scaled version for all Z
                resMask.put(Integer.valueOf(Integer.MIN_VALUE), mergeForDownscale(srcMask, -1, nbPointForTrue));
            else
            {
                for (int z = minZ; z < maxZ; z += 2)
                {
                    final int destZ = z / 2;
                    resMask.put(Integer.valueOf(destZ), mergeForDownscale(srcMask, destZ, nbPointForTrue));
                }
            }
        }

        return new BooleanMask3D(resMask);
    }

    /**
     * Fast 2x down scaling (each 2x2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D downscale(BooleanMask3D mask) throws InterruptedException
    {
        return downscale(mask, 5);
    }

    /**
     * Fast 2x up scaling (each point become 2x2 bloc points).<br>
     * 2D version (down scale is done on XY dimension only).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D upscale2D(BooleanMask3D mask) throws InterruptedException
    {
        final TreeMap<Integer, BooleanMask2D> srcMask = mask.mask;
        final TreeMap<Integer, BooleanMask2D> resMask = new TreeMap<Integer, BooleanMask2D>();

        synchronized (mask)
        {
            final int minZ = srcMask.firstKey().intValue();
            final int maxZ = srcMask.lastKey().intValue();

            // single Z --> check for special MAX_INTEGER case
            if ((minZ == maxZ) && (mask.bounds.sizeZ == Integer.MAX_VALUE))
            {
                // put up scaled version for all Z
                resMask.put(Integer.valueOf(Integer.MIN_VALUE), srcMask.firstEntry().getValue().upscale());
            }
            else
            {
                // put up scaled version for each Z
                for (Entry<Integer, BooleanMask2D> entry : srcMask.entrySet())
                    resMask.put(entry.getKey(), entry.getValue().upscale());
            }
        }

        return new BooleanMask3D(resMask);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * 2D version (down scale is done on XY dimension only).<br>
     * This method create a new boolean mask.
     * 
     * @param mask
     *        the boolean mask to download
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1 to 4 (default is 3)
     * @throws InterruptedException
     */
    public static BooleanMask3D downscale2D(BooleanMask3D mask, int nbPointForTrue) throws InterruptedException
    {
        final TreeMap<Integer, BooleanMask2D> srcMask = mask.mask;
        final TreeMap<Integer, BooleanMask2D> resMask = new TreeMap<Integer, BooleanMask2D>();

        synchronized (mask)
        {
            final int minZ = srcMask.firstKey().intValue();
            final int maxZ = srcMask.lastKey().intValue();

            // single Z --> check for special MAX_INTEGER case
            if ((minZ == maxZ) && (mask.bounds.sizeZ == Integer.MAX_VALUE))
            {
                // put down scaled version for all Z
                resMask.put(Integer.valueOf(Integer.MIN_VALUE),
                        srcMask.firstEntry().getValue().downscale(nbPointForTrue));
            }
            else
            {
                // put down scaled version for each Z
                for (Entry<Integer, BooleanMask2D> entry : srcMask.entrySet())
                    resMask.put(entry.getKey(), entry.getValue().downscale(nbPointForTrue));
            }
        }

        return new BooleanMask3D(resMask);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * 2D version (down scale is done on XY dimension only).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public static BooleanMask3D downscale2D(BooleanMask3D mask) throws InterruptedException
    {
        return downscale2D(mask, 2);
    }

    /**
     * Region represented by the mask.
     */
    public Rectangle3D.Integer bounds;
    /**
     * Boolean mask 2D array.
     */
    public final TreeMap<Integer, BooleanMask2D> mask;

    public BooleanMask3D(Rectangle3D.Integer bounds, TreeMap<Integer, BooleanMask2D> mask)
    {
        super();

        this.mask = mask;
        this.bounds = bounds;
    }

    public BooleanMask3D(TreeMap<Integer, BooleanMask2D> mask)
    {
        this(new Rectangle3D.Integer(), mask);

        // bounds need to exist before calling getOptimizedBounds()
        bounds = getOptimizedBounds(false);
    }

    /**
     * Build a new 3D boolean mask with specified bounds and 2D mask array.<br>
     * The 2D mask array length should be &gt;= to <code>bounds.getSizeZ()</code>.
     */
    public BooleanMask3D(Rectangle3D.Integer bounds, BooleanMask2D[] mask)
    {
        super();

        this.bounds = bounds;
        this.mask = new TreeMap<Integer, BooleanMask2D>();

        // special case of infinite Z dim
        if (bounds.sizeZ == Integer.MAX_VALUE)
            this.mask.put(Integer.valueOf(Integer.MIN_VALUE), mask[0]);
        else
        {
            for (int z = 0; z < bounds.sizeZ; z++)
                if (mask[z] != null)
                    this.mask.put(Integer.valueOf(bounds.z + z), mask[z]);
        }
    }

    /**
     * Build a new 3D boolean mask from the specified array of {@link Point3D}.<br>
     */
    public BooleanMask3D(Point3D.Integer[] points)
    {
        super();

        mask = new TreeMap<Integer, BooleanMask2D>();

        if ((points == null) || (points.length == 0))
            bounds = new Rectangle3D.Integer();
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Point3D.Integer pt : points)
            {
                final int x = pt.x;
                final int y = pt.y;
                final int z = pt.z;

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
                if (z < minZ)
                    minZ = z;
                if (z > maxZ)
                    maxZ = z;
            }

            // define bounds
            bounds = new Rectangle3D.Integer(minX, minY, minZ, (maxX - minX) + 1, (maxY - minY) + 1, (maxZ - minZ) + 1);

            // set mask
            for (Point3D.Integer pt : points)
            {
                BooleanMask2D m = mask.get(Integer.valueOf(pt.z));

                // allocate boolean mask if needed
                if (m == null)
                {
                    m = new BooleanMask2D(new Rectangle(minX, minY, bounds.sizeX, bounds.sizeY),
                            new boolean[bounds.sizeX * bounds.sizeY]);
                    // set 2D mask for position Z
                    mask.put(Integer.valueOf(pt.z), m);
                }

                // set mask point
                m.mask[((pt.y - minY) * bounds.sizeX) + (pt.x - minX)] = true;
            }

            // optimize mask 2D bounds
            for (BooleanMask2D m : mask.values())
                m.optimizeBounds();
        }
    }

    /**
     * Build a new boolean mask from the specified array of {@link Point3D}.<br>
     * 
     * @throws InterruptedException
     */
    public BooleanMask3D(Point3D[] points)
    {
        super();

        mask = new TreeMap<Integer, BooleanMask2D>();

        if ((points == null) || (points.length == 0))
            bounds = new Rectangle3D.Integer();
        else
        {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Point3D pt : points)
            {
                final int x = (int) pt.getX();
                final int y = (int) pt.getY();
                final int z = (int) pt.getZ();

                if (x < minX)
                    minX = x;
                if (x > maxX)
                    maxX = x;
                if (y < minY)
                    minY = y;
                if (y > maxY)
                    maxY = y;
                if (z < minZ)
                    minZ = z;
                if (z > maxZ)
                    maxZ = z;
            }

            // define bounds
            bounds = new Rectangle3D.Integer(minX, minY, minZ, (maxX - minX) + 1, (maxY - minY) + 1, (maxZ - minZ) + 1);

            // set mask
            for (Point3D pt : points)
            {
                BooleanMask2D m = mask.get(Integer.valueOf((int) pt.getZ()));

                // allocate boolean mask if needed
                if (m == null)
                {
                    m = new BooleanMask2D(new Rectangle(minX, minY, bounds.sizeX, bounds.sizeY),
                            new boolean[bounds.sizeX * bounds.sizeY]);
                    // set 2D mask for position Z
                    mask.put(Integer.valueOf((int) pt.getZ()), m);
                }

                // set mask point
                m.mask[(((int) pt.getY() - minY) * bounds.sizeX) + ((int) pt.getX() - minX)] = true;
            }

            // optimize mask 2D bounds
            for (BooleanMask2D m : mask.values())
                m.optimizeBounds();
        }
    }

    public BooleanMask3D()
    {
        this(new Rectangle3D.Integer(), new BooleanMask2D[0]);
    }

    /**
     * Returns the 2D boolean mask for the specified Z position
     */
    public BooleanMask2D getMask2D(int z)
    {
        // special case of infinite Z dimension
        if (bounds.sizeZ == Integer.MAX_VALUE)
            return mask.firstEntry().getValue();

        return mask.get(Integer.valueOf(z));
    }

    /**
     * Return <code>true</code> if boolean mask is empty
     */
    public boolean isEmpty()
    {
        return bounds.isEmpty();
    }

    /**
     * Return true if mask contains the specified point
     */
    public boolean contains(int x, int y, int z)
    {
        if (bounds.contains(x, y, z))
        {
            final BooleanMask2D m2d = getMask2D(z);

            if (m2d != null)
                return m2d.contains(x, y);
        }

        return false;
    }

    /**
     * Return true if mask contains the specified 2D mask at position Z.
     */
    public boolean contains(BooleanMask2D booleanMask, int z)
    {
        if (isEmpty())
            return false;

        final BooleanMask2D mask2d = getMask2D(z);

        if (mask2d != null)
            return mask2d.contains(booleanMask);

        return false;
    }

    /**
     * Return true if mask contains the specified 3D mask.
     */
    public boolean contains(BooleanMask3D booleanMask)
    {
        if (isEmpty())
            return false;

        final int sizeZ = booleanMask.bounds.sizeZ;

        // check for special MAX_INTEGER case (infinite Z dim)
        if (sizeZ == Integer.MAX_VALUE)
        {
            // we cannot contains it if we are not on infinite Z dim too
            if (bounds.sizeZ != Integer.MAX_VALUE)
                return false;

            return booleanMask.mask.firstEntry().getValue().contains(mask.firstEntry().getValue());
        }

        // quick test
        if ((bounds.sizeZ != Integer.MAX_VALUE) && !bounds.contains(booleanMask.bounds))
            return false;

        final int offZ = booleanMask.bounds.z;

        for (int z = offZ; z < offZ + sizeZ; z++)
            if (!contains(booleanMask.getMask2D(z), z))
                return false;

        return true;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 2D mask at
     * position Z.
     */
    public boolean intersects(BooleanMask2D booleanMask, int z)
    {
        if (isEmpty())
            return false;

        final BooleanMask2D mask2d = getMask2D(z);

        if (mask2d != null)
            return mask2d.intersects(booleanMask);

        return false;
    }

    /**
     * Return true if mask intersects (contains at least one point) the specified 3D mask region.
     */
    public boolean intersects(BooleanMask3D booleanMask)
    {
        if (isEmpty())
            return false;

        final int sizeZ = booleanMask.bounds.sizeZ;

        // check for special MAX_INTEGER case (infinite Z dim)
        if (sizeZ == Integer.MAX_VALUE)
        {
            // get the single Z slice
            final BooleanMask2D mask2d = booleanMask.mask.firstEntry().getValue();

            // test with every slice
            for (BooleanMask2D m : mask.values())
                if (m.intersects(mask2d))
                    return true;

            return false;
        }

        // check for special MAX_INTEGER case (infinite Z dim)
        if (bounds.sizeZ == Integer.MAX_VALUE)
        {
            // get the single Z slice
            final BooleanMask2D mask2d = mask.firstEntry().getValue();

            // test with every slice
            for (BooleanMask2D m : booleanMask.mask.values())
                if (m.intersects(mask2d))
                    return true;

            return false;
        }

        // quick test
        if (!bounds.intersects(booleanMask.bounds))
            return false;

        final int offZ = booleanMask.bounds.z;

        for (int z = offZ; z < offZ + sizeZ; z++)
            if (intersects(booleanMask.getMask2D(z), z))
                return true;

        return false;
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public Rectangle3D.Integer getOptimizedBounds(boolean compute2DBounds)
    {
        final Rectangle3D.Integer result = new Rectangle3D.Integer();

        if (mask.isEmpty())
            return result;

        Rectangle bounds2D = null;

        for (BooleanMask2D m2d : mask.values())
        {
            // get optimized 2D bounds for each Z
            final Rectangle optB2d;

            if (compute2DBounds)
                optB2d = m2d.getOptimizedBounds();
            else
                optB2d = new Rectangle(m2d.bounds);

            // only add non empty bounds
            if (!optB2d.isEmpty())
            {
                if (bounds2D == null)
                    bounds2D = optB2d;
                else
                    bounds2D.add(optB2d);
            }
        }

        // empty ?
        if ((bounds2D == null) || bounds2D.isEmpty())
            return result;

        int minZ = mask.firstKey().intValue();
        int maxZ = mask.lastKey().intValue();

        // set 2D bounds to start with
        result.setX(bounds2D.x);
        result.setY(bounds2D.y);
        result.setSizeX(bounds2D.width);
        result.setSizeY(bounds2D.height);

        // single Z --> check for special infinite Z case
        if ((minZ == maxZ) && ((minZ == Integer.MIN_VALUE) || (bounds.sizeZ == Integer.MAX_VALUE)))
        {
            result.setZ(Integer.MIN_VALUE);
            result.setSizeZ(Integer.MAX_VALUE);
        }
        else
        {
            result.setZ(minZ);
            result.setSizeZ((maxZ - minZ) + 1);
        }

        return result;
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public Rectangle3D.Integer getOptimizedBounds()
    {
        return getOptimizedBounds(true);
    }

    /**
     * Optimize mask bounds so it fits mask content.
     */
    public void optimizeBounds()
    {
        // start by optimizing 2D bounds
        for (BooleanMask2D m : mask.values())
            m.optimizeBounds();

        moveBounds(getOptimizedBounds(false));
    }

    /**
     * Change the bounds of BooleanMask.<br>
     * Keep mask data intersecting from old bounds.
     */
    public void moveBounds(Rectangle3D.Integer value)
    {
        // bounds changed ?
        if (!bounds.equals(value))
        {
            // changed to empty mask
            if (value.isEmpty())
            {
                // clear bounds and mask
                bounds = new Rectangle3D.Integer();
                mask.clear();
                return;
            }

            final Rectangle bounds2D = new Rectangle(value.x, value.y, value.sizeX, value.sizeY);

            // it was infinite Z dim ?
            if (bounds.sizeZ == Integer.MAX_VALUE)
            {
                // get the single 2D mask
                final BooleanMask2D m2d = mask.firstEntry().getValue();

                // adjust 2D bounds if needed to the single 2D mask
                m2d.moveBounds(bounds2D);

                // we passed from infinite Z to defined Z range
                if (value.sizeZ != Integer.MAX_VALUE)
                {
                    // assign the same 2D mask for all Z position
                    mask.clear();
                    for (int z = 0; z <= value.sizeZ; z++)
                        mask.put(Integer.valueOf(z + value.z), (BooleanMask2D) m2d.clone());
                }
            }
            // we pass to infinite Z dim
            else if (value.sizeZ == Integer.MAX_VALUE)
            {
                // try to use the 2D mask at Z position
                BooleanMask2D mask2D = getMask2D(value.z);

                // otherwise we use the first found 2D mask
                if ((mask2D == null) && !mask.isEmpty())
                    mask2D = mask.firstEntry().getValue();

                // set new mask
                mask.clear();
                if (mask2D != null)
                    mask.put(Integer.valueOf(Integer.MIN_VALUE), mask2D);
            }
            else
            {
                // create new mask array
                final BooleanMask2D[] newMask = new BooleanMask2D[value.sizeZ];

                for (int z = 0; z < value.sizeZ; z++)
                {
                    final BooleanMask2D mask2D = getMask2D(value.z + z);

                    if (mask2D != null)
                        // adjust 2D bounds
                        mask2D.moveBounds(bounds2D);

                    newMask[z] = mask2D;
                }

                // set new mask
                mask.clear();
                for (int z = 0; z < value.sizeZ; z++)
                    mask.put(Integer.valueOf(value.z + z), newMask[z]);
            }

            bounds = value;
        }
    }

    /**
     * Fast 2x up scaling (each point become 2x2x2 bloc point).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public BooleanMask3D upscale() throws InterruptedException
    {
        return upscale(this);
    }

    /**
     * Fast 2x down scaling (each 2x2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     * 
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1-8 (default is 5).
     * @throws InterruptedException
     */
    public BooleanMask3D downscale(int nbPointForTrue) throws InterruptedException
    {
        return downscale(this, nbPointForTrue);
    }

    /**
     * Fast 2x down scaling (each 2x2x2 block points become 1 point).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public BooleanMask3D downscale() throws InterruptedException
    {
        return downscale(this);
    }

    /**
     * Fast 2x up scaling (each point become 2x2 bloc point).<br>
     * 2D version (down scale is done on XY dimension only).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public BooleanMask3D upscale2D() throws InterruptedException
    {
        return upscale2D(this);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * 2D version (down scale is done on XY dimension only).<br>
     * This method create a new boolean mask.
     * 
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1-4 (default is 3).
     * @throws InterruptedException
     */
    public BooleanMask3D downscale2D(int nbPointForTrue) throws InterruptedException
    {
        return downscale2D(this, nbPointForTrue);
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * 2D version (down scale is done on XY dimension only).<br>
     * This method create a new boolean mask.
     * 
     * @throws InterruptedException
     */
    public BooleanMask3D downscale2D() throws InterruptedException
    {
        return downscale2D(this);
    }

    /**
     * Transforms the specified 3D coordinates int array [x,y,z] in 4D coordinates int array [x,y,z,t] with the
     * specified T value.
     */
    public static int[] toInt3D(int[] source2D, int z)
    {
        final int[] result = new int[(source2D.length * 3) / 2];

        int pt = 0;
        for (int i = 0; i < source2D.length; i += 2)
        {
            result[pt++] = source2D[i + 0];
            result[pt++] = source2D[i + 1];
            result[pt++] = z;
        }

        return result;
    }

    /**
     * Return the number of points contained in this boolean mask.
     */
    public int getNumberOfPoints() throws InterruptedException
    {
        int result = 0;

        for (BooleanMask2D mask2d : mask.values())
        {
            if (Thread.interrupted())
                throw new InterruptedException("BooleanMask.getNumberOfPoints() computation interrupted !");

            result += mask2d.getNumberOfPoints();
        }

        return result;
    }

    /**
     * Return an array of {@link icy.type.point.Point3D.Integer} representing all points of the
     * current 3D mask.<br>
     * Points are returned in ascending XYZ order.
     * 
     * @throws InterruptedException
     */
    public Point3D.Integer[] getPoints() throws InterruptedException
    {
        return Point3D.Integer.toPoint3D(getPointsAsIntArray());
    }

    /**
     * Return an array of integer representing all points of the current 3D mask.<br>
     * <code>result.length</code> = number of point * 3<br>
     * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XYZ order.
     */
    public int[] getPointsAsIntArray() throws InterruptedException
    {
        final DynamicArray.Int result = new DynamicArray.Int(8);

        for (Entry<Integer, BooleanMask2D> entry : mask.entrySet())
        {
            if (Thread.interrupted())
                throw new InterruptedException("BooleanMask.getPointsAsIntArray() computation interrupted !");

            result.add(toInt3D(entry.getValue().getPointsAsIntArray(), entry.getKey().intValue()));
        }

        return result.asArray();
    }

    /**
     * Return an array of {@link icy.type.point.Point3D.Integer} containing the contour/surface
     * points of the 3D mask.<br>
     * Points are returned in ascending XYZ order. <br>
     * <br>
     * WARNING: The default implementation is not totally accurate.<br>
     * It returns all points from the first and the last Z slices + contour points for intermediate
     * Z slices.
     * 
     * @throws InterruptedException
     * @see #getContourPointsAsIntArray()
     */
    public Point3D.Integer[] getContourPoints() throws InterruptedException
    {
        return Point3D.Integer.toPoint3D(getContourPointsAsIntArray());
    }

    /**
     * Return an array of integer containing the contour/surface points of the 3D mask.<br>
     * <code>result.length</code> = number of point * 3<br>
     * <code>result[(pt * 3) + 0]</code> = X coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 1]</code> = Y coordinate for point <i>pt</i>.<br>
     * <code>result[(pt * 3) + 2]</code> = Z coordinate for point <i>pt</i>.<br>
     * Points are returned in ascending XYZ order.<br>
     * <br>
     * WARNING: The default implementation is not totally accurate.<br>
     * It returns all points from the first and the last Z slices + contour points for intermediate
     * Z slices.
     * 
     * @throws InterruptedException
     * @see #getContourPoints()
     */
    public int[] getContourPointsAsIntArray() throws InterruptedException
    {
        final DynamicArray.Int result = new DynamicArray.Int(8);

        // TODO: fix this method and use real 3D contour point
        if (mask.size() <= 2)
        {
            for (Entry<Integer, BooleanMask2D> entry : mask.entrySet())
                result.add(toInt3D(entry.getValue().getPointsAsIntArray(), entry.getKey().intValue()));
        }
        else
        {
            final Entry<Integer, BooleanMask2D> firstEntry = mask.firstEntry();
            final Entry<Integer, BooleanMask2D> lastEntry = mask.lastEntry();
            final Integer firstKey = firstEntry.getKey();
            final Integer lastKey = lastEntry.getKey();

            result.add(toInt3D(firstEntry.getValue().getPointsAsIntArray(), firstKey.intValue()));

            for (Entry<Integer, BooleanMask2D> entry : mask.subMap(firstKey, false, lastKey, false).entrySet())
                result.add(toInt3D(entry.getValue().getContourPointsAsIntArray(), entry.getKey().intValue()));

            result.add(toInt3D(lastEntry.getValue().getPointsAsIntArray(), lastKey.intValue()));
        }

        return result.asArray();
    }

    /**
     * Computes and returns the length of the contour.<br>
     * This is different from the number of contour point as it takes care of approximating
     * correctly distance between each contour point.
     *
     * @return the length of the contour
     * @throws InterruptedException
     */
    public double getContourLength() throws InterruptedException
    {
        double result = 0;

        final int[] edge = getContourPointsAsIntArray();

        // count the edges and corners in 2D/3D
        double sideEdges = 0, cornerEdges = 0;

        for (int i = 0; i < edge.length; i += 3)
        {
            final int x = edge[i + 0];
            final int y = edge[i + 1];
            final int z = edge[i + 2];

            // start on current plan
            BooleanMask2D mask2D = getMask2D(z);

            final boolean leftConnected = mask2D.contains(x - 1, y);
            final boolean rightConnected = mask2D.contains(x + 1, y);
            final boolean topConnected = mask2D.contains(x, y - 1);
            final boolean bottomConnected = mask2D.contains(x + 1, y + 1);
            // lower plan
            mask2D = getMask2D(z - 1);
            final boolean southConnected = (mask2D != null) && mask2D.contains(x, y);
            // upper plan
            mask2D = getMask2D(z + 1);
            final boolean northConnected = (mask2D != null) && mask2D.contains(x, y);

            // count the connections (6 max)
            int connection = 0;

            if (leftConnected)
                connection++;
            if (rightConnected)
                connection++;
            if (topConnected)
                connection++;
            if (bottomConnected)
                connection++;
            if (southConnected)
                connection++;
            if (northConnected)
                connection++;

            switch (connection)
            {
                // case 0: // isolated point
                // cornerEdges += 3;
                // sideEdges++;
                // result += 1 + Math.sqrt(2) + (2 * Math.sqrt(3));
                // break;
                //
                // case 1: // filament end
                // cornerEdges += 2;
                // sideEdges++;
                // result += 1 + (2 * Math.sqrt(3));
                // break;
                //
                // case 2: // filament point
                // if ((leftConnected && rightConnected) || (topConnected && bottomConnected)
                // || (northConnected && southConnected))
                // {
                // // quadruple "side" edge
                // sideEdges += 4;
                // result += 4;
                // }
                // else
                // {
                // cornerEdges += 3;
                // result += 3 * Math.sqrt(2);
                // }
                // // cornerEdges += 3;
                // // perimeter += Math.sqrt(3);
                // break;
                //
                // case 3: // "salient" point
                // if ((leftConnected && rightConnected) || (topConnected && bottomConnected)
                // || (northConnected && southConnected))
                // {
                // // triple "side" edge
                // sideEdges += 3;
                // result += 3;
                // }
                // else
                // {
                // cornerEdges += 2;
                // result += 2 * Math.sqrt(2);
                // }
                default:
                    cornerEdges++;
                    result += Math.sqrt(3);
                    break;

                case 4:
                    if (leftConnected && rightConnected && topConnected && bottomConnected)
                    {
                        // double "side" edge
                        sideEdges += 2;
                        result += 2;
                    }
                    else if (leftConnected && rightConnected && northConnected && southConnected)
                    {
                        // double "side" edge
                        sideEdges += 2;
                        result += 2;
                    }
                    else if (topConnected && bottomConnected && northConnected && southConnected)
                    {
                        // double "side" edge
                        sideEdges += 2;
                        result += 2;
                    }
                    else
                    {
                        // "corner" edge
                        cornerEdges++;
                        result += Math.sqrt(2);
                    }
                    break;

                case 5: // "side" edge
                    sideEdges++;
                    result++;
                    break;

                // internal point --> should not happen
                case 6:
                    break;
            }

            // case 0:
            // break;
            // case 1:
            // sideEdges++;
            // perimeter++;
            // break;
            // case 2:
            // cornerEdges++;
            // perimeter += Math.sqrt(2);
            // break;
            // case 3:
            // cornerEdges += 2;
            // perimeter += 2 * Math.sqrt(2);
            // break;
            // default:
            // cornerEdges += 3;
            // perimeter += Math.sqrt(3);
        }

        // adjust the surface area empirically according to the edge distribution
        double overShoot = Math.min(sideEdges / 10, cornerEdges);

        return result - overShoot;
    }

    @Override
    public Object clone()
    {
        final BooleanMask3D result = new BooleanMask3D();

        result.bounds = new Rectangle3D.Integer(bounds);
        for (Entry<Integer, BooleanMask2D> entry : mask.entrySet())
            result.mask.put(entry.getKey(), (BooleanMask2D) entry.getValue().clone());

        return result;
    }

    /**
     * Extract masks in the specified sequence though its connected components (8-connectivity is
     * considered in 2D, and 26-connectivity in 3D). The algorithm uses a fast single-pass sweeping
     * technique that builds a list of intermediate connected components which are eventually fused
     * according to their connectivity.<br>
     * NB: in 3D, the algorithm caches some intermediate buffers to disk to save RAM
     * 
     * @return List of BooleanMask3D instances (each connected component).
     */
    public List<BooleanMask3D> getComponents() throws InterruptedException
    {
        int width = bounds.sizeX;
        int height = bounds.sizeY;
        int slice = width * height;
        int depth = bounds.sizeZ;

        final Map<Integer, ConnectedComponent> ccs = new HashMap<>();
        final Map<Integer, ROI3DArea> roiMap = new HashMap<>();

        int[] neighborLabels = new int[13];
        int nbNeighbors = 0;

        // temporary label buffer
        final Sequence labelSequence = new Sequence();
        boolean virtual = false;

        int[] _labelsAbove = null;

        // first image pass: naive labeling with simple backward neighborhood
        int highestKnownLabel = 0;

        for (int z = 0; z < depth; z++)
        {
            int[] _labelsHere;

            try
            {
                _labelsHere = new int[slice];
            }
            catch (OutOfMemoryError error)
            {
                // not enough memory --> pass in virtual mode
                if (!virtual)
                {
                    labelSequence.setVolatile(true);
                    virtual = true;

                    // re-allocate (we should have enough memory now)
                    _labelsHere = new int[slice];
                }
                else
                    throw error;
            }

            // if (is3D) System.out.println("[Label Extractor] First pass (Z" + z + ")");

            BooleanMask2D inputData = getMask2D(z);

            for (int y = 0, maskY = bounds.y, inOffset = 0; y < height; y++, maskY++)
            {
                if (Thread.currentThread().isInterrupted())
                    return new ArrayList<>();

                for (int x = 0, maskX = bounds.x; x < width; x++, maskX++, inOffset++)
                {
                    boolean currentImageValue = inputData.contains(maskX, maskY);

                    // do not process the current pixel if:
                    // - extractUserValue is true and pixelEqualsUserValue is false
                    // - extractUserValue is false and pixelEqualsUserValue is true

                    if (!currentImageValue)
                        continue;

                    // from here on, the current pixel should be labeled

                    // -> look for existing labels in its neighborhood

                    // 1) define the neighborhood of interest here
                    // NB: this is a single pass method, so backward neighborhood is sufficient

                    // legend:
                    // e = edge
                    // x = current pixel
                    // n = valid neighbor
                    // . = other neighbor

                    if (z == 0)
                    {
                        if (y == 0)
                        {
                            if (x == 0)
                            {
                                // e e e
                                // e x .
                                // e . .

                                // do nothing
                            }
                            else
                            {
                                // e e e
                                // n x .
                                // . . .

                                neighborLabels[0] = _labelsHere[inOffset - 1];
                                nbNeighbors = 1;
                            }
                        }
                        else
                        {
                            int north = inOffset - width;

                            if (x == 0)
                            {
                                // e n n
                                // e x .
                                // e . .

                                neighborLabels[0] = _labelsHere[north];
                                neighborLabels[1] = _labelsHere[north + 1];
                                nbNeighbors = 2;
                            }
                            else if (x == width - 1)
                            {
                                // n n e
                                // n x e
                                // . . e

                                neighborLabels[0] = _labelsHere[north - 1];
                                neighborLabels[1] = _labelsHere[north];
                                neighborLabels[2] = _labelsHere[inOffset - 1];
                                nbNeighbors = 3;
                            }
                            else
                            {
                                // n n n
                                // n x .
                                // . . .

                                neighborLabels[0] = _labelsHere[north - 1];
                                neighborLabels[1] = _labelsHere[north];
                                neighborLabels[2] = _labelsHere[north + 1];
                                neighborLabels[3] = _labelsHere[inOffset - 1];
                                nbNeighbors = 4;
                            }
                        }
                    }
                    else
                    {
                        if (y == 0)
                        {
                            int south = inOffset + width;

                            if (x == 0)
                            {
                                // e e e | e e e
                                // e n n | e x .
                                // e n n | e . .

                                neighborLabels[0] = _labelsAbove[inOffset];
                                neighborLabels[1] = _labelsAbove[inOffset + 1];
                                neighborLabels[2] = _labelsAbove[south];
                                neighborLabels[3] = _labelsAbove[south + 1];
                                nbNeighbors = 4;
                            }
                            else if (x == width - 1)
                            {
                                // e e e | e e e
                                // n n e | n x e
                                // n n e | . . e

                                neighborLabels[0] = _labelsAbove[inOffset - 1];
                                neighborLabels[1] = _labelsAbove[inOffset];
                                neighborLabels[2] = _labelsAbove[south - 1];
                                neighborLabels[3] = _labelsAbove[south];
                                neighborLabels[4] = _labelsHere[inOffset - 1];
                                nbNeighbors = 5;
                            }
                            else
                            {
                                // e e e | e e e
                                // n n n | n x .
                                // n n n | . . .

                                neighborLabels[0] = _labelsAbove[inOffset - 1];
                                neighborLabels[1] = _labelsAbove[inOffset];
                                neighborLabels[2] = _labelsAbove[inOffset + 1];
                                neighborLabels[3] = _labelsAbove[south - 1];
                                neighborLabels[4] = _labelsAbove[south];
                                neighborLabels[5] = _labelsAbove[south + 1];
                                neighborLabels[6] = _labelsHere[inOffset - 1];
                                nbNeighbors = 7;
                            }
                        }
                        else if (y == height - 1)
                        {
                            int north = inOffset - width;

                            if (x == 0)
                            {
                                // e n n | e n n
                                // e n n | e x .
                                // e e e | e e e

                                neighborLabels[0] = _labelsAbove[north];
                                neighborLabels[1] = _labelsAbove[north + 1];
                                neighborLabels[2] = _labelsAbove[inOffset];
                                neighborLabels[3] = _labelsAbove[inOffset + 1];
                                neighborLabels[4] = _labelsHere[north];
                                neighborLabels[5] = _labelsHere[north + 1];
                                nbNeighbors = 6;
                            }
                            else if (x == width - 1)
                            {
                                // n n e | n n e
                                // n n e | n x e
                                // e e e | e e e

                                neighborLabels[0] = _labelsAbove[north - 1];
                                neighborLabels[1] = _labelsAbove[north];
                                neighborLabels[2] = _labelsAbove[inOffset - 1];
                                neighborLabels[3] = _labelsAbove[inOffset];
                                neighborLabels[4] = _labelsHere[north - 1];
                                neighborLabels[5] = _labelsHere[north];
                                neighborLabels[6] = _labelsHere[inOffset - 1];
                                nbNeighbors = 7;
                            }
                            else
                            {
                                // n n n | n n n
                                // n n n | n x .
                                // e e e | e e e

                                neighborLabels[0] = _labelsAbove[north - 1];
                                neighborLabels[1] = _labelsAbove[north];
                                neighborLabels[2] = _labelsAbove[north + 1];
                                neighborLabels[3] = _labelsAbove[inOffset - 1];
                                neighborLabels[4] = _labelsAbove[inOffset];
                                neighborLabels[5] = _labelsAbove[inOffset + 1];
                                neighborLabels[6] = _labelsHere[north - 1];
                                neighborLabels[7] = _labelsHere[north];
                                neighborLabels[8] = _labelsHere[north + 1];
                                neighborLabels[9] = _labelsHere[inOffset - 1];
                                nbNeighbors = 10;
                            }
                        }
                        else
                        {
                            int north = inOffset - width;
                            int south = inOffset + width;

                            if (x == 0)
                            {
                                // e n n | e n n
                                // e n n | e x .
                                // e n n | e . .

                                neighborLabels[0] = _labelsAbove[north];
                                neighborLabels[1] = _labelsAbove[north + 1];
                                neighborLabels[2] = _labelsAbove[inOffset];
                                neighborLabels[3] = _labelsAbove[inOffset + 1];
                                neighborLabels[4] = _labelsAbove[south];
                                neighborLabels[5] = _labelsAbove[south + 1];
                                neighborLabels[6] = _labelsHere[north];
                                neighborLabels[7] = _labelsHere[north + 1];
                                nbNeighbors = 8;
                            }
                            else if (x == width - 1)
                            {
                                int northwest = north - 1;
                                int west = inOffset - 1;

                                // n n e | n n e
                                // n n e | n x e
                                // n n e | . . e

                                neighborLabels[0] = _labelsAbove[northwest];
                                neighborLabels[1] = _labelsAbove[north];
                                neighborLabels[2] = _labelsAbove[west];
                                neighborLabels[3] = _labelsAbove[inOffset];
                                neighborLabels[4] = _labelsAbove[south - 1];
                                neighborLabels[5] = _labelsAbove[south];
                                neighborLabels[6] = _labelsHere[northwest];
                                neighborLabels[7] = _labelsHere[north];
                                neighborLabels[8] = _labelsHere[west];
                                nbNeighbors = 9;
                            }
                            else
                            {
                                int northwest = north - 1;
                                int west = inOffset - 1;
                                int northeast = north + 1;
                                int southwest = south - 1;
                                int southeast = south + 1;

                                // n n n | n n n
                                // n n n | n x .
                                // n n n | . . .

                                neighborLabels[0] = _labelsAbove[northwest];
                                neighborLabels[1] = _labelsAbove[north];
                                neighborLabels[2] = _labelsAbove[northeast];
                                neighborLabels[3] = _labelsAbove[west];
                                neighborLabels[4] = _labelsAbove[inOffset];
                                neighborLabels[5] = _labelsAbove[inOffset + 1];
                                neighborLabels[6] = _labelsAbove[southwest];
                                neighborLabels[7] = _labelsAbove[south];
                                neighborLabels[8] = _labelsAbove[southeast];
                                neighborLabels[9] = _labelsHere[northwest];
                                neighborLabels[10] = _labelsHere[north];
                                neighborLabels[11] = _labelsHere[northeast];
                                neighborLabels[12] = _labelsHere[west];
                                nbNeighbors = 13;
                            }
                        }
                    }

                    // 2) the neighborhood is ready, move to the labeling step

                    // to avoid creating too many labels and fuse them later on,
                    // find the minimum non-zero label in the neighborhood
                    // and assign that minimum label right now

                    int currentLabel = Integer.MAX_VALUE;

                    for (int i = 0; i < nbNeighbors; i++)
                    {
                        int neighborLabel = neighborLabels[i];

                        // "zero" neighbors belong to the background...
                        if (neighborLabel == 0)
                            continue;

                        if (!currentImageValue)
                            continue;

                        // here, the neighbor label is valid
                        // => check if it is lower
                        if (neighborLabel < currentLabel)
                        {
                            currentLabel = neighborLabel;
                        }
                    }

                    if (currentLabel == Integer.MAX_VALUE)
                    {
                        // currentLabel didn't change
                        // => there is no lower neighbor
                        // => register a new connected component
                        highestKnownLabel++;
                        currentLabel = highestKnownLabel;
                        ccs.put(currentLabel, new ConnectedComponent(currentLabel));
                    }
                    else
                    {
                        // currentLabel has been modified
                        // -> browse the neighborhood again
                        // --> fuse high labels with low labels

                        ConnectedComponent currentCC = ccs.get(currentLabel);
                        int currentTargetLabel = currentCC.getTargetLabel();

                        for (int i = 0; i < nbNeighbors; i++)
                        {
                            int neighborLabel = neighborLabels[i];

                            if (neighborLabel == 0)
                                continue; // no object in this pixel

                            ConnectedComponent neighborCC = ccs.get(neighborLabel);
                            int neighborTargetLabel = neighborCC.getTargetLabel();

                            if (neighborTargetLabel == currentTargetLabel)
                                continue;

                            if (!currentImageValue)
                                continue;

                            // fuse the highest with the lowest
                            if (neighborTargetLabel > currentTargetLabel)
                            {
                                ccs.get(neighborTargetLabel).targetComponent = ccs.get(currentTargetLabel);
                            }
                            else
                            {
                                ccs.get(currentTargetLabel).targetComponent = ccs.get(neighborTargetLabel);
                            }
                        }
                    }

                    // -> store this label in the labeled image
                    if (currentLabel != 0)
                        _labelsHere[inOffset] = currentLabel;
                }
            }

            final IcyBufferedImage img = new IcyBufferedImage(width, height, _labelsHere);
            // pass to volatile if needed
            if (virtual)
                img.setVolatile(true);

            // store image in label sequence
            labelSequence.setImage(0, z, img);
            // store labels from previous slice
            _labelsAbove = _labelsHere;
        }

        // for debugging
        // Sequence preLabels = new Sequence();
        // preLabels.addImage(new IcyBufferedImage(width, height, new int[][] { _labels[0] }));
        // preLabels.getColorModel().setColorMap(0, new FireColorMap(), true);
        // addSequence(preLabels);

        // end of the first pass, all pixels have a label
        // (though might not be unique within a given component)

        if (Thread.currentThread().isInterrupted())
            return new ArrayList<BooleanMask3D>();

        // fusion strategy: fuse higher labels with lower ones
        // "highestKnownLabel" holds the highest known label
        // -> loop downwards from there to accumulate object size recursively

        int finalLabel = 0;

        for (int currentLabel = highestKnownLabel; currentLabel > 0; currentLabel--)
        {
            ConnectedComponent currentCC = ccs.get(currentLabel);

            // if the target label is higher than or equal to the current label
            if (currentCC.targetLabel >= currentLabel)
            {
                // label has same labelValue and targetLabelValue
                // -> it cannot be fused to anything
                // -> this is a terminal label

                // -> assign its final labelValue (for the final image labeling pass)
                finalLabel++;
                currentCC.targetLabel = finalLabel;
            }
            else
            {
                // the current label should be fused to targetLabel
                currentCC.targetComponent = ccs.get(currentCC.targetLabel);
            }
        }

        // 3) second image pass: replace all labels by their final values

        finalPass: for (int z = 0, zMask = 0; z < depth; z++, zMask++)
        {
            // get labels for that slice
            final int[] _labelsHere = (int[]) labelSequence.getDataXY(0, z, 0);
            for (int j = 0, jMask = bounds.y, offset = 0; j < height; j++, jMask++)
            {
                if (Thread.currentThread().isInterrupted())
                    break finalPass;

                for (int i = 0, iMask = bounds.x; i < width; i++, iMask++, offset++)
                {
                    int targetLabel = _labelsHere[offset];

                    if (targetLabel == 0)
                        continue;

                    // if a fusion was indicated, retrieve the final label value
                    targetLabel = ccs.get(targetLabel).getTargetLabel();

                    // store the current pixel in the component
                    if (!roiMap.containsKey(targetLabel))
                    {
                        ROI3DArea roi3D = new ROI3DArea();
                        roi3D.setName("" + targetLabel);
                        roiMap.put(targetLabel, roi3D);
                    }
                    roiMap.get(targetLabel).addPoint(iMask, jMask, zMask);
                }
            }
        }

        final List<BooleanMask3D> result = new ArrayList<>();

        for (ROI3DArea roi : roiMap.values())
            result.add(roi.getBooleanMask(true));

        return result;
    }

}
