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

import java.util.concurrent.atomic.AtomicBoolean;

import org.bioimageanalysis.icy.model.image.IcyBufferedImageCursor;

/**
 * This class allows to optimally access randomly around an {@link VolumetricImage}. Instances of this class can perform reading and writing operations on
 * non-contiguous positions of the volume without incurring in important performance issues. When a set of modifications to pixel data is performed a call to
 * {@link #commitChanges()} must be made in order to make this changes permanent of the image and let other resources using the image be aware of to these
 * changes.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class VolumetricImageCursor
{
    private VolumetricImage vol;

    private AtomicBoolean volumeChanged;

    private IcyBufferedImageCursor[] planeCursors;

    /**
     * Creates a cursor on the given volume {@code vol}.
     * 
     * @param vol
     *        Target volume.
     */
    public VolumetricImageCursor(VolumetricImage vol)
    {
        this.vol = vol;
        planeCursors = new IcyBufferedImageCursor[vol.getSize()];
        volumeChanged = new AtomicBoolean(false);
        currentZ = -1;
    }

    /**
     * Creates a cursor on the volume at time position {@code t} in the given sequence {@code seq}.
     * 
     * @param seq
     *        Target sequence.
     * @param t
     *        Time position where the volume is located in {@code seq}.
     */
    public VolumetricImageCursor(Sequence seq, int t)
    {
        this(seq.getVolumetricImage(t));
    }

    /**
     * Retrieves the intensity of the channel {@code c} of the pixel located at position ({@code x}, {@code y}, {@code z}).
     * 
     * @param x
     *        Position in the X-axis.
     * @param y
     *        Position in the Y-axis.
     * @param z
     *        Position in the Z-axis.
     * @param c
     *        Channel index.
     * @return Intensity value at specified position.
     * @throws IndexOutOfBoundsException
     *         If the position is not in the image.
     * @throws RuntimeException
     *         If the data type is not a valid format.
     */
    public double get(int x, int y, int z, int c) throws IndexOutOfBoundsException, RuntimeException
    {
        return getPlaneCursor(z).get(x, y, c);

    }

    /**
     * Sets the intensity of the channel {@code c} of the pixel located at position ({@code x}, {@code y}, {@code z}).
     * 
     * @param x
     *        Position in the X-axis.
     * @param y
     *        Position in the Y-axis.
     * @param z
     *        Position in the Z-axis.
     * @param c
     *        Channel index.
     * @param val
     *        Intensity value to set.
     * @throws IndexOutOfBoundsException
     *         If the position is not in the image.
     * @throws RuntimeException
     *         If the data type is not a valid format.
     */
    public synchronized void set(int x, int y, int z, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getPlaneCursor(z).set(x, y, c, val);
        volumeChanged.set(true);
    }

    /**
     * Sets the intensity of the channel {@code c} of the pixel located at position ({@code x}, {@code y}, {@code z}). This method limits the
     * value of the intensity according to the image data type value range.
     * 
     * @param x
     *        Position in the X-axis.
     * @param y
     *        Position in the Y-axis.
     * @param z
     *        Position in the Z-axis.
     * @param c
     *        Channel index.
     * @param val
     *        Intensity value to set.
     * @throws IndexOutOfBoundsException
     *         If the position is not in the image.
     * @throws RuntimeException
     *         If the data type is not a valid format.
     */
    public synchronized void setSafe(int x, int y, int z, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getPlaneCursor(z).setSafe(x, y, c, val);
        volumeChanged.set(true);
    }

    private IcyBufferedImageCursor currentCursor;
    private int currentZ;

    private synchronized IcyBufferedImageCursor getPlaneCursor(int z) throws IndexOutOfBoundsException
    {
        if (currentZ != z)
        {
            if (planeCursors[z] == null)
            {
                planeCursors[z] = new IcyBufferedImageCursor(vol.getImage(z));
            }
            currentCursor = planeCursors[z];
            currentZ = z;
        }
        return currentCursor;
    }

    /**
     * This method should be called after a set of intensity changes have been made to the target volume. This methods allows other resources using the target
     * volume to be informed about the changes made to it.
     */
    public synchronized void commitChanges()
    {
        if (volumeChanged.get())
        {
            for (int i = 0; i < planeCursors.length; i++)
            {
                if (planeCursors[i] != null)
                    planeCursors[i].commitChanges();
            }
            volumeChanged.set(false);
        }
    }

    @Override
    public String toString()
    {
        return "last Z=" + currentZ + " " + currentCursor != null ? currentCursor.toString() : "";
    }
}
