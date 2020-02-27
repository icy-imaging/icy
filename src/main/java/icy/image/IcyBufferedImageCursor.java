package icy.image;

import java.util.concurrent.atomic.AtomicBoolean;

import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.TypeUtil;

/**
 * This class allows to optimally access randomly around an {@link IcyBufferedImage}. Instances of this class can perform access and writing operations on
 * non-contiguous positions of the image without incurring in important performance issues. When a set of modifications to pixel data is performed a call to
 * {@link #commitChanges()} must be made in order to make this changes permanent of the image and let other users of the image be aware of to these changes.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class IcyBufferedImageCursor
{

    IcyBufferedImage plane;
    int[] planeSize;
    DataType planeType;

    AtomicBoolean planeChanged;

    Object planeData;

    /**
     * Creates a new cursor from the given {@code plane}.
     */
    public IcyBufferedImageCursor(IcyBufferedImage plane)
    {
        this.plane = plane;
        this.planeSize = new int[] {plane.getSizeX(), plane.getSizeY()};
        this.planeType = plane.getDataType_();

        plane.lockRaster();
        this.planeData = plane.getDataXYC();
        this.currentChannelData = null;
        this.currentChannel = -1;
        planeChanged = new AtomicBoolean(false);
    }

    /**
     * Creates a new cursor based on the image from the given {@link Sequence} {@code seq} at time {@code t} and stack position {@code z}.
     * 
     * @param seq
     *        Sequence from which the target image is retrieved.
     * @param t
     *        Time point where the target image is located.
     * @param z
     *        Stack position where the target image is located.
     */
    public IcyBufferedImageCursor(Sequence seq, int t, int z)
    {
        this(seq.getImage(t, z));
    }

    Object currentChannelData;
    int currentChannel;

    /**
     * @param x
     *        Position on the X-axis.
     * @param y
     *        Position on the Y-axis.
     * @param c
     *        Position on the channel axis.
     * @return Intensity of the pixel located at the given coordinates ({@code x}, {@code y}) in the channel {@code c}.
     * @throws IndexOutOfBoundsException
     *         If the position is not valid on the target image.
     * @throws RuntimeException
     *         If the format of the image is not supported.
     */
    public double get(int x, int y, int c) throws IndexOutOfBoundsException, RuntimeException
    {
        Object channelData = getChannelData(c);

        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                return TypeUtil.toDouble(((byte[]) channelData)[x + y * planeSize[0]], planeType.isSigned());
            case USHORT:
            case SHORT:
                return TypeUtil.toDouble(((short[]) channelData)[x + y * planeSize[0]], planeType.isSigned());
            case UINT:
            case INT:
                return TypeUtil.toDouble(((int[]) channelData)[x + y * planeSize[0]], planeType.isSigned());
            case FLOAT:
                return ((float[]) channelData)[x + y * planeSize[0]];
            case DOUBLE:
                return ((double[]) channelData)[x + y * planeSize[0]];
            default:
                throw new RuntimeException("Unsupported data type: " + planeType);
        }
    }

    /**
     * Sets {@code val} as the intensity of the pixel located at the given coordinates ({@code x}, {@code y}) in the channel {@code c}.
     * 
     * @param x
     *        Position on the X-axis.
     * @param y
     *        Position on the Y-axis.
     * @param c
     *        Position on the channel axis.
     * @param val
     *        Value to set.
     * @throws IndexOutOfBoundsException
     *         If the position is not valid on the target image.
     * @throws RuntimeException
     *         If the format of the image is not supported.
     */
    public synchronized void set(int x, int y, int c, double val) throws IndexOutOfBoundsException, RuntimeException
    {
        Object channelData = getChannelData(c);

        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                ((byte[]) channelData)[x + y * planeSize[0]] = (byte) val;
                break;
            case USHORT:
            case SHORT:
                ((short[]) channelData)[x + y * planeSize[0]] = (short) val;
                break;
            case UINT:
            case INT:
                ((int[]) channelData)[x + y * planeSize[0]] = (int) val;
                break;
            case FLOAT:
                ((float[]) channelData)[x + y * planeSize[0]] = (float) val;
                break;
            case DOUBLE:
                ((double[]) channelData)[x + y * planeSize[0]] = val;
                break;
            default:
                throw new RuntimeException("Unsupported data type");
        }
        planeChanged.set(true);
    }

    /**
     * Sets {@code val} as the intensity of the pixel located at the given coordinates ({@code x}, {@code y}) in the channel {@code c}. This method limits the
     * value of the intensity according to the image data type value range.
     * 
     * @param x
     *        Position on the X-axis.
     * @param y
     *        Position on the Y-axis.
     * @param c
     *        Position on the channel axis.
     * @param val
     *        Value to set.
     * @throws IndexOutOfBoundsException
     *         If the position is not valid on the target image.
     * @throws RuntimeException
     *         If the format of the image is not supported.
     */
    public synchronized void setSafe(int x, int y, int c, double val) throws IndexOutOfBoundsException, RuntimeException
    {
        Object channelData = getChannelData(c);
        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                ((byte[]) channelData)[x + y * planeSize[0]] = (byte) Math.round(getSafeValue(val));
                break;
            case USHORT:
            case SHORT:
                ((short[]) channelData)[x + y * planeSize[0]] = (short) Math.round(getSafeValue(val));
                break;
            case UINT:
            case INT:
                ((int[]) channelData)[x + y * planeSize[0]] = (int) Math.round(getSafeValue(val));
                break;
            case FLOAT:
                ((float[]) channelData)[x + y * planeSize[0]] = (float) getSafeValue(val);
                break;
            case DOUBLE:
                ((double[]) channelData)[x + y * planeSize[0]] = val;
                break;
            default:
                throw new RuntimeException("Unsupported data type");
        }
        planeChanged.set(true);
    }

    private synchronized Object getChannelData(int c) throws IndexOutOfBoundsException, RuntimeException
    {

        if (currentChannel != c)
        {
            switch (planeType)
            {
                case UBYTE:
                case BYTE:
                    currentChannelData = ((byte[][]) planeData)[c];
                    break;
                case USHORT:
                case SHORT:
                    currentChannelData = ((short[][]) planeData)[c];
                    break;
                case UINT:
                case INT:
                    currentChannelData = ((int[][]) planeData)[c];
                    break;
                case FLOAT:
                    currentChannelData = ((float[][]) planeData)[c];
                    break;
                case DOUBLE:
                    currentChannelData = ((double[][]) planeData)[c];
                    break;
                default:
                    throw new RuntimeException("Unsupported data type: " + planeType);
            }
            currentChannel = c;
        }
        return currentChannelData;

    }

    private double getSafeValue(double val)
    {
        return Math.max(Math.min(val, planeType.getMaxValue()), planeType.getMinValue());
    }

    /**
     * This method should be called after a set of intensity changes have been done to the target image. This methods allows other resources using the target
     * image to be informed about the changes made to it.
     */
    public synchronized void commitChanges()
    {
        plane.releaseRaster(planeChanged.get());
        if (planeChanged.get())
        {
            plane.dataChanged();
        }
    }

}