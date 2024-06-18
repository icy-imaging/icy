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

/**
 * 
 */
package org.bioimageanalysis.icy.model.roi;

import java.util.NoSuchElementException;

import org.bioimageanalysis.icy.common.geom.position.Position5DIterator;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2DIterator;

/**
 * ROI iterator.<br>
 * This class permit to use simple iterator to navigate each point of specified ROI in XYCZT
 * <i>([T[Z[C[Y[X]]]]])</i> dimension order.<br>
 * <b>If the ROI is modified during iteration the iterator becomes invalid and exception can
 * happen.</b>
 * 
 * @author Stephane
 */
public class ROIIterator implements Position5DIterator
{
    protected final ROI roi;
    protected final Rectangle5D.Integer bounds;
    protected final boolean inclusive;
    protected int c, z, t;
    protected boolean done;
    protected BooleanMask2DIterator maskIterator;

    /**
     * Create a new ROI iterator to iterate through each point of the specified ROI.
     * 
     * @param roi
     *        ROI defining the region to iterate.
     * @param region
     *        A 5D region to limit the ROI region area to iterate for.<br>
     *        Keep it to <code>null</code> to iterate all over the ROI.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels in the ROI are included.
     * @throws InterruptedException 
     */
    public ROIIterator(ROI roi, Rectangle5D region, boolean inclusive) throws InterruptedException
    {
        super();

        this.roi = roi;
        // get final bounds
        if (region != null)
            bounds = region.createIntersection(roi.getBounds5D()).toInteger();
        else
            bounds = roi.getBounds5D().toInteger();
        this.inclusive = inclusive;

        // fix infinite dimensions
        if (bounds.isInfiniteZ())
        {
            bounds.z = -1;
            bounds.sizeZ = 1;
        }
        if (bounds.isInfiniteT())
        {
            bounds.t = -1;
            bounds.sizeT = 1;
        }
        if (bounds.isInfiniteC())
        {
            bounds.c = -1;
            bounds.sizeC = 1;
        }

        // start iterator
        reset();
    }

    /**
     * Create a new ROI iterator to iterate through each point of the specified ROI.
     * 
     * @param roi
     *        ROI defining the region to iterate.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels in the ROI are included.
     * @throws InterruptedException 
     */
    public ROIIterator(ROI roi, boolean inclusive) throws InterruptedException
    {
        this(roi, null, inclusive);
    }

    public int getMinZ()
    {
        return bounds.z;
    }

    public int getMaxZ()
    {
        return (bounds.z + bounds.sizeZ) - 1;
    }

    public int getMinT()
    {
        return bounds.t;
    }

    public int getMaxT()
    {
        return (bounds.t + bounds.sizeT) - 1;
    }

    public int getMinC()
    {
        return bounds.c;
    }

    public int getMaxC()
    {
        return (bounds.c + bounds.sizeC) - 1;
    }

    @Override
    public void reset() throws InterruptedException
    {
        done = bounds.isEmpty();

        if (!done)
        {
            z = getMinZ();
            t = getMinT();
            c = getMinC();

            prepareXY();
            nextXYIfNeeded();
        }
    }

    /**
     * Prepare for XY iteration.
     * @throws InterruptedException 
     */
    protected void prepareXY() throws InterruptedException
    {
        maskIterator = new BooleanMask2DIterator(roi.getBooleanMask2D(z, t, c, inclusive));
    }

    @Override
    public void next() throws InterruptedException
    {
        if (done)
            throw new NoSuchElementException();

        maskIterator.next();
        nextXYIfNeeded();
    }

    /**
     * Advance one image position.
     * @throws InterruptedException 
     */
    protected void nextXYIfNeeded() throws InterruptedException
    {
        while (maskIterator.done() && !done)
        {
            if (++c > getMaxC())
            {
                c = getMinC();

                if (++z > getMaxZ())
                {
                    z = getMinZ();

                    if (++t > getMaxT())
                    {
                        done = true;
                        return;
                    }
                }
            }

            prepareXY();
        }
    }

    @Override
    public boolean done()
    {
        return done;
    }

    @Override
    public Point5D get()
    {
        if (done)
            throw new NoSuchElementException();

        return new Point5D.Integer(maskIterator.getX(), maskIterator.getY(), z, t, c);
    }

    @Override
    public int getX()
    {
        if (done)
            throw new NoSuchElementException();

        return maskIterator.getX();
    }

    @Override
    public int getY()
    {
        if (done)
            throw new NoSuchElementException();

        return maskIterator.getY();
    }

    @Override
    public int getC()
    {
        if (done)
            throw new NoSuchElementException();

        return c;
    }

    @Override
    public int getZ()
    {
        if (done)
            throw new NoSuchElementException();

        return z;
    }

    @Override
    public int getT()
    {
        if (done)
            throw new NoSuchElementException();

        return t;
    }

}
