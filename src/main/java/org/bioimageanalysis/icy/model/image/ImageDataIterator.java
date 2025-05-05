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
package org.bioimageanalysis.icy.model.image;

import org.bioimageanalysis.icy.common.collection.array.Array1DUtil;
import org.bioimageanalysis.icy.common.type.DataIterator;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2D;

import java.awt.*;
import java.util.NoSuchElementException;

/**
 * Image data iterator.<br>
 * This class permit to use simple iterator to read / write <code>IcyBufferedImage</code> data<br>
 * as double in XYC <i>([C[Y[X]]])</i> dimension order .<br>
 * Whatever is the internal {@link DataType} data is returned and set as double.<br>
 * <b>If the image size or type is modified during iteration the iterator
 * becomes invalid and can causes exception to happen.</b>
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ImageDataIterator implements DataIterator, AutoCloseable {
    protected final IcyBufferedImage image;
    protected final DataType dataType;

    /**
     * internals
     */
    protected final BooleanMask2D mask;
    protected final Rectangle regionBounds;
    protected final Rectangle imageBounds;
    protected final Rectangle finalBounds;
    protected final int c;
    protected final int w, h;
    protected int x, y;
    protected int imgOff;
    protected int maskOff;
    protected boolean changed;
    protected boolean done;
    protected Object data;

    /**
     * Create a new ImageData iterator to iterate data through the specified XY region and channel.
     *
     * @param image
     *        Image we want to iterate data from
     * @param channel
     *        channel (C position) we want to iterate data
     */
    protected ImageDataIterator(final IcyBufferedImage image, final Rectangle boundsXY, final BooleanMask2D maskXY, final int channel) {
        super();

        if (maskXY != null)
            regionBounds = maskXY.bounds;
        else
            regionBounds = boundsXY;

        this.image = image;
        this.mask = maskXY;

        if (image != null) {
            imageBounds = image.getBounds();
            dataType = image.getDataType();
            c = channel;
            // retain data while we are iterating over image data
            image.lockRaster();
        }
        else {
            imageBounds = new Rectangle();
            dataType = DataType.UBYTE;
            c = 0;
        }

        finalBounds = regionBounds.intersection(imageBounds);

        // cached
        w = finalBounds.width;
        h = finalBounds.height;

        changed = false;

        // start iterator
        reset();
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified XY region and channel.
     *
     * @param image
     *        Image we want to iterate data from
     * @param boundsXY
     *        XY region to iterate (inclusive).
     * @param channel
     *        channel (C position) we want to iterate data
     */
    public ImageDataIterator(final IcyBufferedImage image, final Rectangle boundsXY, final int channel) {
        this(image, boundsXY, null, channel);
    }

    /**
     * Create a new ImageData iterator to iterate data of specified channel.
     *
     * @param image
     *        Image we want to iterate data from
     * @param c
     *        C position (channel) we want to iterate data
     */
    public ImageDataIterator(final IcyBufferedImage image, final int c) {
        this(image, image.getBounds(), c);
    }

    /**
     * Create a new ImageData iterator to iterate data through the specified <code>BooleanMask2D</code> and C dimension.
     *
     * @param image
     *        Image we want to iterate data from
     * @param maskXY
     *        BooleanMask2D defining the XY region to iterate
     * @param channel
     *        channel (C position) we want to iterate data
     */
    public ImageDataIterator(final IcyBufferedImage image, final BooleanMask2D maskXY, final int channel) {
        this(image, null, maskXY, channel);
    }

    @Override
    public void close() throws Exception {
        flush();
    }

    public int getMinX() {
        return finalBounds.x;
    }

    public int getMaxX() {
        return (finalBounds.x + w) - 1;
    }

    public int getMinY() {
        return finalBounds.y;
    }

    public int getMaxY() {
        return (finalBounds.y + h) - 1;
    }

    @Override
    public void reset() {
        done = (image == null) || (c < 0) || (c >= image.getSizeC()) || finalBounds.isEmpty();

        if (!done) {
            // get data
            data = image.getDataXY(c);

            // reset position
            y = 0;
            x = -1;
            imgOff = (finalBounds.x - imageBounds.x) + ((finalBounds.y - imageBounds.y) * imageBounds.width);
            imgOff--;
            if (mask != null) {
                maskOff = (finalBounds.x - regionBounds.x) + ((finalBounds.y - regionBounds.y) * regionBounds.width);
                maskOff--;
            }
            // allow to correctly set initial position in boolean mask
            next();
        }
    }

    @Override
    public void next() {
        nextPosition();

        if (mask != null) {
            // advance while mask do not contains current point
            while (!done && !mask.mask[maskOff])
                nextPosition();
        }
    }

    /**
     * Advance one position
     */
    protected void nextPosition() {
        if (mask != null) {
            imgOff++;
            maskOff++;
            if (++x >= w) {
                x = 0;
                imgOff += imageBounds.width - finalBounds.width;
                maskOff += regionBounds.width - finalBounds.width;

                if (++y >= h)
                    done = true;
            }
        }
        else {
            imgOff++;
            if (++x >= w) {
                x = 0;
                imgOff += imageBounds.width - finalBounds.width;

                if (++y >= h)
                    done = true;
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }

    @Override
    public double get() {
        if (done)
            throw new NoSuchElementException("ImageDataIterator.get() error: no more element !");

        return Array1DUtil.getValue(data, imgOff, dataType);
    }

    @Override
    public void set(final double value) {
        if (done)
            throw new NoSuchElementException("ImageDataIterator.get() error: no more element !");

        Array1DUtil.setValue(data, imgOff, dataType, value);
        changed = true;
    }

    /**
     * Returns current X position.
     */
    public int getX() {
        return finalBounds.x + x;
    }

    /**
     * Returns current Y position.
     */
    public int getY() {
        return finalBounds.y + y;
    }

    /**
     * Returns C position (fixed)
     */
    public int getC() {
        return c;
    }

    public void flush() {
        // release image raster and save changes to cache
        if (image != null)
            image.releaseRaster(changed);
        changed = false;
    }
}
