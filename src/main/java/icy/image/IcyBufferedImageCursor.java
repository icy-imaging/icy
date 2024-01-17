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

package icy.image;

import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.TypeUtil;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class allows to optimally access randomly around an {@link IcyBufferedImage}. Instances of this class can perform access and writing operations on
 * non-contiguous positions of the image without incurring in important performance issues. When a set of modifications to pixel data is performed a call to
 * {@link #commitChanges()} must be made in order to make this changes permanent of the image and let other users of the image be aware of to these changes.
 *
 * @author Daniel Felipe Gonzalez Obando
 * @author Thomas Musset
 */
public class IcyBufferedImageCursor {

    private final IcyBufferedImage plane;
    private final int sizeX;
    private final DataType planeType;

    private final AtomicBoolean planeChanged;

    private final Object planeData;

    /**
     * Creates a new cursor from the given {@code plane}.
     */
    public IcyBufferedImageCursor(final IcyBufferedImage plane) {
        this.plane = plane;
        this.sizeX = plane.getSizeX();
        this.planeType = plane.getDataType();

        plane.lockRaster();
        this.planeData = plane.getDataXYC();
        this.currentChannelData = null;
        this.currentChannel = -1;
        planeChanged = new AtomicBoolean(false);
    }

    /**
     * Creates a new cursor based on the image from the given {@link Sequence} {@code seq} at time {@code t} and stack position {@code z}.
     *
     * @param seq Sequence from which the target image is retrieved.
     * @param t Time point where the target image is located.
     * @param z Stack position where the target image is located.
     */
    public IcyBufferedImageCursor(final Sequence seq, final int t, final int z) {
        this(seq.getImage(t, z));
    }

    private Object currentChannelData;
    private int currentChannel;

    /**
     * @param x Position on the X-axis.
     * @param y Position on the Y-axis.
     * @param c Position on the channel axis.
     * @return Intensity of the pixel located at the given coordinates ({@code x}, {@code y}) in the channel {@code c}.
     * @throws IndexOutOfBoundsException If the position is not valid on the target image.
     * @throws RuntimeException If the format of the image is not supported.
     */
    public double get(final int x, final int y, final int c) throws IndexOutOfBoundsException, RuntimeException {
        final Object channelData = getChannelData(c);

        return switch (planeType) {
            case UBYTE, BYTE -> TypeUtil.toDouble(((byte[]) channelData)[x + y * sizeX], planeType.isSigned());
            case USHORT, SHORT -> TypeUtil.toDouble(((short[]) channelData)[x + y * sizeX], planeType.isSigned());
            case UINT, INT -> TypeUtil.toDouble(((int[]) channelData)[x + y * sizeX], planeType.isSigned());
            case FLOAT -> ((float[]) channelData)[x + y * sizeX];
            case DOUBLE -> ((double[]) channelData)[x + y * sizeX];
            default -> throw new RuntimeException("Unsupported data type: " + planeType);
        };
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
    public synchronized void set(final int x, final int y, final int c, final double val) throws IndexOutOfBoundsException, RuntimeException {
        final Object channelData = getChannelData(c);

        switch (planeType) {
            case UBYTE:
            case BYTE:
                ((byte[]) channelData)[x + y * sizeX] = (byte) val;
                break;
            case USHORT:
            case SHORT:
                ((short[]) channelData)[x + y * sizeX] = (short) val;
                break;
            case UINT:
            case INT:
                ((int[]) channelData)[x + y * sizeX] = (int) val;
                break;
            case FLOAT:
                ((float[]) channelData)[x + y * sizeX] = (float) val;
                break;
            case DOUBLE:
                ((double[]) channelData)[x + y * sizeX] = val;
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
    public synchronized void setSafe(final int x, final int y, final int c, final double val) throws IndexOutOfBoundsException, RuntimeException {
        final Object channelData = getChannelData(c);
        switch (planeType) {
            case UBYTE:
            case BYTE:
                ((byte[]) channelData)[x + y * sizeX] = (byte) Math.round(getSafeValue(val));
                break;
            case USHORT:
            case SHORT:
                ((short[]) channelData)[x + y * sizeX] = (short) Math.round(getSafeValue(val));
                break;
            case UINT:
            case INT:
                ((int[]) channelData)[x + y * sizeX] = (int) Math.round(getSafeValue(val));
                break;
            case FLOAT:
                ((float[]) channelData)[x + y * sizeX] = (float) getSafeValue(val);
                break;
            case DOUBLE:
                ((double[]) channelData)[x + y * sizeX] = val;
                break;
            default:
                throw new RuntimeException("Unsupported data type");
        }
        planeChanged.set(true);
    }

    private synchronized Object getChannelData(final int c) throws RuntimeException {
        if (currentChannel != c) {
            switch (planeType) {
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

    private double getSafeValue(final double val) {
        return Math.max(Math.min(val, planeType.getMaxValue()), planeType.getMinValue());
    }

    /**
     * This method should be called after a set of intensity changes have been done to the target image. This methods allows other resources using the target
     * image to be informed about the changes made to it.
     */
    public synchronized void commitChanges() {
        plane.releaseRaster(planeChanged.get());
        if (planeChanged.get()) {
            plane.dataChanged();
            planeChanged.set(false);
        }
    }

    @Override
    public String toString() {
        return "last channel=" + currentChannel;
    }
}