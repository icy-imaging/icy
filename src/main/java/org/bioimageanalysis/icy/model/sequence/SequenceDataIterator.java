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
package org.bioimageanalysis.icy.model.sequence;

import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D.Integer;
import org.bioimageanalysis.icy.common.type.DataIterator;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.image.ImageDataIterator;
import org.bioimageanalysis.icy.model.roi.ROI;

import java.awt.*;
import java.util.NoSuchElementException;

/**
 * Sequence data iterator.<br>
 * This class permit to use simple iterator to read / write <code>Sequence</code> data<br>
 * as double in XYCZT <i>([T[Z[C[Y[X}}]]])</i> dimension order.<br>
 * Whatever is the internal {@link DataType} data is returned and set as double.<br>
 * <b>If the sequence size or type is modified during iteration the iterator
 * becomes invalid and can exception can happen.</b>
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SequenceDataIterator implements DataIterator {
    protected final Sequence sequence;
    protected final ROI roi;

    protected final Rectangle XYBounds;
    protected final int startC, endC;
    protected final int startZ, endZ;
    protected final int startT, endT;
    protected final boolean inclusive;

    /**
     * internals
     */
    protected int c, z, t;
    protected boolean done;
    protected ImageDataIterator imageIterator;

    /**
     * Create a new SequenceData iterator to iterate data through the specified 5D region
     * (inclusive).
     *
     * @param sequence
     *        Sequence we want to iterate data from
     * @param bounds5D
     *        the 5D rectangular region we want to iterate
     */
    public SequenceDataIterator(final Sequence sequence, final Integer bounds5D) throws InterruptedException {
        super();

        this.sequence = sequence;
        roi = null;
        imageIterator = null;
        inclusive = true;

        if (sequence != null) {
            final Integer bounds = (Integer) bounds5D.createIntersection(sequence.getBounds5D());

            XYBounds = (Rectangle) bounds.toRectangle2D();

            startZ = bounds.z;
            endZ = (bounds.z + bounds.sizeZ) - 1;
            startT = bounds.t;
            endT = (bounds.t + bounds.sizeT) - 1;
            startC = bounds.c;
            endC = (bounds.c + bounds.sizeC) - 1;
        }
        else {
            XYBounds = null;
            startZ = 0;
            endZ = 0;
            startT = 0;
            endT = 0;
            startC = 0;
            endC = 0;
        }

        // start iterator
        reset();
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified dimensions
     * (inclusive).
     *
     * @param sequence
     *        Sequence we want to iterate data from
     * @param XYBounds
     *        XY region to iterate
     * @param z
     *        Z position (stack) we want to iterate data
     * @param t
     *        T position (time) we want to iterate data
     * @param c
     *        C position (channel) we want to iterate data
     */
    public SequenceDataIterator(final Sequence sequence, final Rectangle XYBounds, final int z, final int t, final int c) throws InterruptedException {
        this(sequence, new Integer(
                XYBounds.x, XYBounds.y, z, t, c,
                (XYBounds.x + XYBounds.width) - 1, (XYBounds.y + XYBounds.height) - 1, 1, 1, 1
        ));
    }

    /**
     * Create a new SequenceData iterator to iterate data of specified channel.
     *
     * @param sequence
     *        Sequence we want to iterate data from
     * @param z
     *        Z position (stack) we want to iterate data
     * @param t
     *        T position (time) we want to iterate data
     * @param c
     *        C position (channel) we want to iterate data
     */
    public SequenceDataIterator(final Sequence sequence, final int z, final int t, final int c) throws InterruptedException {
        this(sequence, new Integer(0, 0, z, t, c, sequence.getSizeX(), sequence.getSizeY(), 1, 1, 1));
    }

    /**
     * Create a new SequenceData iterator to iterate all data.
     *
     * @param sequence
     *        Sequence we want to iterate data from.
     */
    public SequenceDataIterator(final Sequence sequence) throws InterruptedException {
        this(sequence, new Integer(0, 0, 0, 0, 0, sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeZ(), sequence.getSizeT(), sequence.getSizeC()));
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified ROI.
     *
     * @param sequence
     *        Sequence we want to iterate data from.
     * @param roi
     *        ROI defining the region to iterate.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels in the ROI are included.
     * @param z
     *        The specific Z position (slice) we want to iterate or <code>-1</code> to iterate over
     *        the whole ROI Z dimension.
     * @param t
     *        The specific T position (frame) we want to iterate or <code>-1</code> to iterate over
     *        the whole ROI T dimension.
     * @param c
     *        The specific C position (channel) we want to iterate or <code>-1</code> to iterate
     *        over the whole ROI C dimension.
     */
    public SequenceDataIterator(final Sequence sequence, final ROI roi, final boolean inclusive, final int z, final int t, final int c) throws InterruptedException {
        super();

        this.sequence = sequence;
        this.roi = roi;
        this.inclusive = inclusive;
        XYBounds = null;

        if ((sequence != null) && (roi != null)) {
            final Rectangle5D bounds5D = roi.getBounds5D();

            // force Z position
            if (z != -1) {
                bounds5D.setZ(z);
                bounds5D.setSizeZ(1d);
            }
            // force T position
            if (t != -1) {
                bounds5D.setT(t);
                bounds5D.setSizeT(1d);
            }
            // force C position
            if (c != -1) {
                bounds5D.setC(c);
                bounds5D.setSizeC(1d);
            }

            // get final bounds
            final Integer bounds = (Integer) sequence.getBounds5D().createIntersection(bounds5D);

            startZ = bounds.z;
            endZ = (bounds.z + bounds.sizeZ) - 1;
            startT = bounds.t;
            endT = (bounds.t + bounds.sizeT) - 1;
            startC = bounds.c;
            endC = (bounds.c + bounds.sizeC) - 1;
        }
        else {
            startZ = 0;
            endZ = 0;
            startT = 0;
            endT = 0;
            startC = 0;
            endC = 0;
        }

        // start iterator
        reset();
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified ROI.
     *
     * @param sequence
     *        Sequence we want to iterate data from.
     * @param roi
     *        ROI defining the region to iterate.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels in the ROI are included.
     */
    public SequenceDataIterator(final Sequence sequence, final ROI roi, final boolean inclusive) throws InterruptedException {
        this(sequence, roi, inclusive, -1, -1, -1);
    }

    /**
     * Create a new SequenceData iterator to iterate data through the specified ROI.
     *
     * @param sequence
     *        Sequence we want to iterate data from.
     * @param roi
     *        ROI defining the region to iterate.
     */
    public SequenceDataIterator(final Sequence sequence, final ROI roi) throws InterruptedException {
        this(sequence, roi, false);
    }

    @Override
    public void reset() throws InterruptedException {
        done = (sequence == null) || (startT > endT) || (startZ > endZ) || (startC > endC);

        if (!done) {
            t = startT;
            z = startZ;
            c = startC;

            // prepare XY data
            prepareDataXY();
            nextImageifNeeded();
        }
    }

    protected void flushDataXY() {
        if (imageIterator != null)
            imageIterator.flush();
    }

    /**
     * Prepare data for XY iteration.
     */
    protected void prepareDataXY() throws InterruptedException {
        // flush previous dataXY
        flushDataXY();

        final IcyBufferedImage img = sequence.getImage(t, z);

        // get the 2D mask for specified C
        if (roi != null) {
            switch (roi.getDimension()) {
                case 2:
                    // ignore Z, T and C roi informations (wanted for fixed Z, T and C positions)
                    imageIterator = new ImageDataIterator(img, roi.getBooleanMask2D(-1, -1, -1, inclusive), c);
                    break;

                case 3:
                    // ignore T and C roi informations (wanted for fixed T and C positions)
                    imageIterator = new ImageDataIterator(img, roi.getBooleanMask2D(z, -1, -1, inclusive), c);
                    break;

                case 4:
                    // ignore C roi information (wanted for fixed C position)
                    imageIterator = new ImageDataIterator(img, roi.getBooleanMask2D(z, t, -1, inclusive), c);
                    break;

                // assume 5D
                default:
                    imageIterator = new ImageDataIterator(img, roi.getBooleanMask2D(z, t, c, inclusive), c);
            }
        }
        else
            imageIterator = new ImageDataIterator(img, XYBounds, c);
    }

    @Override
    public void next() throws InterruptedException {
        imageIterator.next();
        nextImageifNeeded();
    }

    /**
     * Advance one image position.
     */
    protected void nextImageifNeeded() throws InterruptedException {
        while (imageIterator.done() && !done) {
            if (++c > endC) {
                c = startC;

                if (++z > endZ) {
                    z = startZ;

                    if (++t > endT) {
                        done = true;
                        return;
                    }
                }
            }

            prepareDataXY();
        }
    }

    @Override
    public boolean done() {
        return done;
    }

    @Override
    public double get() {
        if (done)
            throw new NoSuchElementException("SequenceDataIterator.get() error: no more element !");

        return imageIterator.get();
    }

    @Override
    public void set(final double value) {
        if (done)
            throw new NoSuchElementException("SequenceDataIterator.get() error: no more element !");

        imageIterator.set(value);
    }

    /**
     * Return current X position.
     */
    public int getPositionX() {
        if (imageIterator != null)
            return imageIterator.getX();

        return 0;
    }

    /**
     * Return current Y position.
     */
    public int getPositionY() {
        if (imageIterator != null)
            return imageIterator.getY();

        return 0;
    }

    /**
     * Return current C position.
     */
    public int getPositionC() {
        return c;
    }

    /**
     * Return current Z position.
     */
    public int getPositionZ() {
        return z;
    }

    /**
     * Return current T position.
     */
    public int getPositionT() {
        return t;
    }

    /**
     * Ensure changed data are correctly saved back to original data source (should be called at the end)
     */
    public void flush() {
        flushDataXY();
    }
}
