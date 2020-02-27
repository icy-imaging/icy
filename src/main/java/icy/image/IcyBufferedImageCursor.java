package icy.image;

import java.util.concurrent.atomic.AtomicBoolean;

import icy.image.IcyBufferedImage;
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
     */
    public synchronized double get(int x, int y, int c)
    {

        if (currentChannel != c)
        {
            currentChannelData = getChannelData(c);
            currentChannel = c;
        }
        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                return TypeUtil.toDouble(((byte[]) currentChannelData)[x + y * planeSize[0]], planeType.isSigned());
            case USHORT:
            case SHORT:
                return TypeUtil.toDouble(((short[]) currentChannelData)[x + y * planeSize[0]], planeType.isSigned());
            case UINT:
            case INT:
                return TypeUtil.toDouble(((int[]) currentChannelData)[x + y * planeSize[0]], planeType.isSigned());
            case FLOAT:
                return ((float[]) currentChannelData)[x + y * planeSize[0]];
            case DOUBLE:
                return ((double[]) currentChannelData)[x + y * planeSize[0]];
            default:
                throw new RuntimeException("Unsupported data type: " + planeType);
        }
    }

    private Object getChannelData(int c)
    {
        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                return ((byte[][]) planeData)[c];
            case USHORT:
            case SHORT:
                return ((short[][]) planeData)[c];
            case UINT:
            case INT:
                return ((int[][]) planeData)[c];
            case FLOAT:
                return ((float[][]) planeData)[c];
            case DOUBLE:
                return ((double[][]) planeData)[c];
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
     */
    public synchronized void set(int x, int y, int c, double val)
    {
        if (currentChannel != c)
        {
            currentChannelData = getChannelData(c);
            currentChannel = c;
        }
        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                ((byte[]) currentChannelData)[x + y * planeSize[0]] = (byte) val;
                break;
            case USHORT:
            case SHORT:
                ((short[]) currentChannelData)[x + y * planeSize[0]] = (short) val;
                break;
            case UINT:
            case INT:
                ((int[]) currentChannelData)[x + y * planeSize[0]] = (int) val;
                break;
            case FLOAT:
                ((float[]) currentChannelData)[x + y * planeSize[0]] = (float) val;
                break;
            case DOUBLE:
                ((double[]) currentChannelData)[x + y * planeSize[0]] = val;
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
     */
    public synchronized void setSafe(int x, int y, int c, double val)
    {
        if (currentChannel != c)
        {
            currentChannelData = getChannelData(c);
            currentChannel = c;
        }
        switch (planeType)
        {
            case UBYTE:
            case BYTE:
                ((byte[]) currentChannelData)[x + y * planeSize[0]] = (byte) Math.round(getSafeValue(val));
                break;
            case USHORT:
            case SHORT:
                ((short[]) currentChannelData)[x + y * planeSize[0]] = (short) Math.round(getSafeValue(val));
                break;
            case UINT:
            case INT:
                ((int[]) currentChannelData)[x + y * planeSize[0]] = (int) Math.round(getSafeValue(val));
                break;
            case FLOAT:
                ((float[]) currentChannelData)[x + y * planeSize[0]] = (float) getSafeValue(val);
                break;
            case DOUBLE:
                ((double[]) currentChannelData)[x + y * planeSize[0]] = val;
                break;
            default:
                throw new RuntimeException("Unsupported data type");
        }
        planeChanged.set(true);
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