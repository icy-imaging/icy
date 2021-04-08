package plugins.kernel.roi.tool;

import java.awt.Rectangle;

import icy.image.IcyBufferedImage;
import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import ij.IJ;
import plugins.kernel.roi.roi2d.ROI2DArea;

/**
 * Magic Wand for Icy.<br>
 * Merely based on Magic Wand Tool from ImageJ from Michael Schmid and
 * from the smooth tolerance control ideas from Jerome Mutterer.
 * 
 * @author Stephane
 */
public class MagicWand
{
    private final static int UNKNOWN = 0, OUTSIDE = 1, INSIDE = -1; // mask pixel values

    public static class MagicWandSetting
    {
        public double valueTolerance;
        public double gradientTolerance;
        public double colorSensitivity;
        public boolean connect4;
        public boolean includeHoles;

        public MagicWandSetting()
        {
            super();

            // default
            valueTolerance = 0;
            gradientTolerance = 0;
            colorSensitivity = 0;
            connect4 = false;
            includeHoles = false;
        }
    }

    /**
     * @param sequence
     * @param xStart
     * @param yStart
     * @param z
     * @param t
     * @param channel
     *        -1 means color mode
     * @param valueTolerance
     * @param colorSensitivity
     * @param gradientTolerance
     * @return
     */
    public static ROI2D doWand2D(Sequence sequence, int xStart, int yStart, int z, int t, int channel,
            double valueTolerance, double colorSensitivity, double gradientTolerance)
    {
        if (sequence == null)
            return null;

        final MagicWandSetting mws = new MagicWandSetting();

        mws.valueTolerance = valueTolerance;
        mws.colorSensitivity = colorSensitivity;
        mws.gradientTolerance = gradientTolerance;

        return doWand2D(sequence, xStart, yStart, z, t, channel, mws);
    }

    /** Here the wand operation actually happens */
    public static ROI2D doWand2D(Sequence sequence, int xStart, int yStart, int z, int t, int channel,
            MagicWandSetting mws)
    {
        final IcyBufferedImage img = sequence.getImage(t, z);
        final int width = img.getWidth();
        final int height = img.getHeight();
        final int sizeC = img.getSizeC();
        final DataType dataType = img.getDataType_();
        final Object[] pixels = new Object[sizeC];

        // get pixels buffer
        for (int c = 0; c < sizeC; c++)
            pixels[c] = img.getDataXY(c);

        final Object grayPixels = pixels[(channel != -1) && (channel < sizeC)  ? channel : 0];
        // prepare mask (default value = 0 = UNKNOWN)
        final byte[] maskPixels = new byte[width * height];

        // offsets to neighboring pixels in x, y and pixel number (clock ordering)
        final int[] dirXoffset = new int[] {0, 1, 1, 1, 0, -1, -1, -1};
        final int[] dirYoffset = new int[] {-1, -1, 0, 1, 1, 1, 0, -1,};
        final int[] dirOffset = new int[] {-width, -width + 1, +1, +width + 1, +width, +width - 1, -1, -width - 1};

        // pixelPointers array for positions that we have to process; initial size is 4096 = 0x1000 points
        int pixelPointerMask = 0xf; // binary AND with this mask gets array index
        int[] pixelPointers = new int[pixelPointerMask + 1];

        // pixel value range
        final double grayRef = getPixel(grayPixels, dataType, xStart, yStart, width); // reference level from this pixel
        final double lowLimit = grayRef - mws.valueTolerance;
        final double highLimit = grayRef + mws.valueTolerance;

        // Prepare color threshold (only for RGB image)
        final boolean colorMode = (mws.colorSensitivity > -100) && (sizeC == 3) && (channel == -1);
        // RGB pixel buffer
        final int[] rgb = new int[3];
        // RGB value of the starting/reference point
        final int[] rgb0 = new int[3];
        // weights for getting 'parallel' or 'gray' component from RGB
        final double[] rgbWeights = new double[3];

        if (colorMode)
        {
            // get color reference value
            getRGBPixel(pixels, dataType, xStart, yStart, width, rgb0);

            if (mws.colorSensitivity < 0d)
            {
                rgbWeights[0] = 0.299d;
                rgbWeights[1] = 0.587d;
                rgbWeights[2] = 0.114d;
            }
        }

        // Prepare gradient threshold
        boolean useGradient = (mws.gradientTolerance > 0d) && (mws.gradientTolerance < mws.valueTolerance);
        double toleranceGrayGradTmp = mws.gradientTolerance;
        double toleranceGrayGrad2 = sqr(toleranceGrayGradTmp);

        // the uppermost selected pixel, needed for reliable IJ.doWand
        int ymin = height;
        // queue-based flood fill algorithm
        int lastCoord = 0;
        // starting point / offset
        int offset0 = getOffset(xStart, yStart, width);
        maskPixels[offset0] = INSIDE;
        pixelPointers[0] = offset0;

        for (int iCoord = 0; iCoord <= lastCoord; iCoord++)
        {
            final int offset = pixelPointers[iCoord & pixelPointerMask];
            final int x = offset % width;
            final int y = offset / width;
            final boolean isInner = ((x != 0) && (y != 0) && (x != (width - 1)) && (y != (height - 1)));
            final double v = getPixel(grayPixels, dataType, x, y, width);
            boolean largeGradient = false;
            double xGradient = 0d, yGradient = 0d;

            if (useGradient)
            {
                if (isInner)
                {
                    final double vmm = getPixel(grayPixels, dataType, x - 1, y - 1, width);
                    final double v_m = getPixel(grayPixels, dataType, x, y - 1, width);
                    final double vpm = getPixel(grayPixels, dataType, x + 1, y - 1, width);
                    final double vm_ = getPixel(grayPixels, dataType, x - 1, y, width);
                    final double vp_ = getPixel(grayPixels, dataType, x + 1, y, width);
                    final double vmp = getPixel(grayPixels, dataType, x - 1, y + 1, width);
                    final double v_p = getPixel(grayPixels, dataType, x, y + 1, width);
                    final double vpp = getPixel(grayPixels, dataType, x + 1, y + 1, width);

                    // Sobel-filter like gradient
                    xGradient = 0.125d * ((2d * (vp_ - vm_)) + (vpp - vmm) + (vpm - vmp));
                    yGradient = 0.125d * ((2d * (v_p - v_m)) + (vpp - vmm) - (vpm - vmp));
                }
                else
                {
                    int xCount = 0, yCount = 0;

                    for (int d = 0; d < 8; d++)
                    {
                        if (isWithin(width, height, x, y, d))
                        {
                            int x2 = x + dirXoffset[d];
                            int y2 = y + dirYoffset[d];
                            double v2 = getPixel(grayPixels, dataType, x2, y2, width);
                            // 2 for straight, 1 for diag
                            int weight = (2 - (d & 0x1));
                            xGradient += dirXoffset[d] * (v2 - v) * weight;
                            xCount += weight * (dirXoffset[d] != 0 ? 1 : 0);
                            yGradient += dirYoffset[d] * (v2 - v) * weight;
                            yCount += weight * (dirYoffset[d] != 0 ? 1 : 0);
                        }
                    }

                    xGradient /= xCount;
                    yGradient /= yCount;
                }

                largeGradient = (sqr(xGradient) + sqr(yGradient)) > toleranceGrayGrad2;
            }

            for (int d = 0; d < 8; d += mws.connect4 ? 2 : 1)
            {
                // analyze all neighbors (in 4 or 8 directions)
                int offset2 = offset + dirOffset[d];

                if ((isInner || isWithin(width, height, x, y, d)) && maskPixels[offset2] == UNKNOWN)
                {
                    int x2 = x + dirXoffset[d];
                    int y2 = y + dirYoffset[d];
                    double v2 = 0;
                    boolean valueOK;

                    if (largeGradient || !colorMode)
                        v2 = getPixel(grayPixels, dataType, x2, y2, width);

                    // color mode
                    if (colorMode)
                    {
                        getRGBPixel(pixels, dataType, offset2, rgb);
                        valueOK = checkColor(rgb, rgb0, rgbWeights, mws);
                    }
                    // gray mode
                    else
                        valueOK = (v2 >= lowLimit) && (v2 <= highLimit);

                    if (!valueOK)
                        // don't analyze any more
                        maskPixels[offset2] = OUTSIDE;
                    else if (!largeGradient
                            || ((v2 - v) * ((xGradient * dirXoffset[d]) + (yGradient * dirYoffset[d])) <= 0))
                    {
                        // add new point
                        maskPixels[offset2] = INSIDE;

                        if (ymin > y2)
                            ymin = y2;

                        if (lastCoord - iCoord > pixelPointerMask)
                        {
                            // not enough space in array, expand it
                            int newSize = 2 * (pixelPointerMask + 1);
                            int newMask = newSize - 1;
                            int[] newPixelPointers = new int[newSize];
                            System.arraycopy(pixelPointers, 0, newPixelPointers, 0, pixelPointerMask + 1);
                            System.arraycopy(pixelPointers, 0, newPixelPointers, pixelPointerMask + 1,
                                    pixelPointerMask + 1);
                            pixelPointers = newPixelPointers;
                            pixelPointerMask = newMask;
                        }

                        lastCoord++;
                        pixelPointers[lastCoord & pixelPointerMask] = offset2;
                    }
                }
            }

            // check for interruption from time to time
            if (((iCoord & 0xfff) == 1) && Thread.currentThread().isInterrupted())
                return null;
        }

        // prepare mask (default value = 0 = UNKNOWN)
        final boolean[] boolMask = new boolean[width * height];
        for (int i = 0; i < boolMask.length; i++)
            boolMask[i] = (maskPixels[i] == INSIDE);

        // build ROI out of mask
        final ROI2DArea result = new ROI2DArea(new BooleanMask2D(new Rectangle(0, 0, width, height), boolMask));
        // need to optimize bounds
        result.optimizeBounds();
        
        return result;
    }

    public static ROI3D doWand3D(Sequence sequence, int xStart, int yStart, int zStart, int t, int channel,
            double valueTolerance, double colorSensitivity, double gradientTolerance)
    {
        if (sequence == null)
            return null;

        final MagicWandSetting mws = new MagicWandSetting();

        mws.valueTolerance = valueTolerance;
        mws.colorSensitivity = colorSensitivity;
        mws.gradientTolerance = gradientTolerance;

        return doWand3D(sequence, t, xStart, yStart, zStart, channel, mws);
    }

    public static ROI3D doWand3D(Sequence sequence, int x, int y, int z, int t, int channel, MagicWandSetting mws)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static int getOffset(int x, int y, int w)
    {
        return (y * w) + x;
    }

    private static double getPixel(Object pixels, DataType dataType, int offset)
    {
        return Array1DUtil.getValue(pixels, offset, dataType);
    }

    private static double getPixel(Object pixels, DataType dataType, int x, int y, int w)
    {
        return getPixel(pixels, dataType, getOffset(x, y, w));
    }

    private static void getRGBPixel(Object[] pixels, DataType dataType, int offset, int[] dest)
    {
        dest[0] = (int) Array1DUtil.getValue(pixels[0], offset, dataType);
        dest[1] = (int) Array1DUtil.getValue(pixels[1], offset, dataType);
        dest[2] = (int) Array1DUtil.getValue(pixels[2], offset, dataType);
    }

    private static void getRGBPixel(Object[] pixels, DataType dataType, int x, int y, int w, int[] dest)
    {
        getRGBPixel(pixels, dataType, getOffset(x, y, w), dest);
    }

    /** Returns whether pixel with rgb is inside color tolerance */
    private static boolean checkColor(int[] rgb, int[] rgb0, double[] rgbWeights, MagicWandSetting mws)
    {
        final int r = rgb[0];
        final int g = rgb[1];
        final int b = rgb[2];
        final int r0 = rgb0[0];
        final int g0 = rgb0[1];
        final int b0 = rgb0[2];
        final int deltaR = r - r0;
        final int deltaG = g - g0;
        final int deltaB = b - b0;
        final double deltaSqr = sqr(deltaR) + sqr(deltaG) + sqr(deltaB);

        if ((mws.colorSensitivity == 0) || ((r0 == 0) && (g0 == 0) && (b0 == 0)))
            return deltaSqr <= (3 * sqr(mws.valueTolerance));

        if (mws.colorSensitivity < 0)
        {
            // more grayscale-sensitive
            double deltaGray = (deltaR * rgbWeights[0]) + (deltaG * rgbWeights[1]) + (deltaB * rgbWeights[2]);
            return (deltaSqr * ((1d / 3d) + (0.01d / 3d) * mws.colorSensitivity))
                    - (0.01 * mws.colorSensitivity * sqr(deltaGray)) <= sqr(mws.valueTolerance);
        }

        final double rgb0Sqr = sqr(r0) + sqr(g0) + sqr(b0);
        // colorSensitivity > 0; more hue-sensitive
        double deltaParSqr = sqr((deltaR * r0) + (deltaG * g0) + (deltaB * b0)) / rgb0Sqr;
        double deltaPerpSqr = 0;
        double deltaPerpSqrFactor = 0;

        // special case: black pixel
        if ((r == 0) && (g == 0) && (b == 0))
        {
            // treat black as a gray color with this length: r, b, g =eps/sqrt(3)
            final double eps = 1e-6;
            final double cosine = (r0 + g0 + b0) / (Math.sqrt(3) * Math.sqrt(rgb0Sqr));

            deltaParSqr = sqr(Math.sqrt(deltaSqr - (cosine * eps)));
            deltaPerpSqr = (1 - sqr(cosine)) * sqr(eps);
            deltaPerpSqrFactor = ((1d - (0.01d * mws.colorSensitivity)) + (0.01d * mws.colorSensitivity) * rgb0Sqr)
                    / sqr(eps);
        }
        else
        {
            deltaPerpSqr = deltaSqr - deltaParSqr;
            deltaPerpSqrFactor = ((1d - (0.01d * mws.colorSensitivity)) + (0.01 * mws.colorSensitivity) * rgb0Sqr)
                    / (sqr(r) + sqr(g) + sqr(b));
        }

        return ((deltaParSqr * (1d - (0.01d * mws.colorSensitivity))) + (deltaPerpSqr * deltaPerpSqrFactor)) <= (3
                * sqr(mws.valueTolerance));
    }

    /**
     * Returns whether the neighbor in a given direction is within the image
     * NOTE: it is assumed that the pixel x,y itself is within the image!
     * Uses class variables width, height: dimensions of the image
     * 
     * @param x
     *        x-coordinate of the pixel that has a neighbor in the given direction
     * @param y
     *        y-coordinate of the pixel that has a neighbor in the given direction
     * @param direction
     *        the direction from the pixel towards the neighbor (see makeDirectionOffsets)
     * @return true if the neighbor is within the image (provided that x, y is within)
     */
    private static boolean isWithin(int w, int h, int x, int y, int direction)
    {
        final int xmax = w - 1;
        final int ymax = h - 1;

        switch (direction)
        {
            default:
                return false; // should never occur, we use it only for directions 0-7

            case 0:
                return (y > 0);
            case 1:
                return (x < xmax) && (y > 0);
            case 2:
                return (x < xmax);
            case 3:
                return (x < xmax) && (y < ymax);
            case 4:
                return (y < ymax);
            case 5:
                return (x > 0) && (y < ymax);
            case 6:
                return (x > 0);
            case 7:
                return (x > 0) && (y > 0);
        }
    }

    // /* normalize 3-element vector */
    // private static void normalize(double[] v)
    // {
    // double norm = 1d / Math.sqrt(sqr(v[0]) + sqr(v[1]) + sqr(v[2]));
    //
    // for (int i = 0; i < 3; i++)
    // v[i] *= norm;
    // }

    private static double sqr(double x)
    {
        return x * x;
    }
}
