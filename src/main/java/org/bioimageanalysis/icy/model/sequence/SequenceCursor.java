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

/**
 * This class allows to optimally access randomly around a {@link Sequence}. Instances of this class can perform reading and writing operations on
 * non-contiguous positions of the sequence without incurring in important performance issues. When a set of modifications to pixel data is performed a call to
 * {@link #commitChanges()} must be made in order to make this changes permanent of the image and let other resources using the image be aware of to these
 * changes.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class SequenceCursor
{
    private Sequence seq;
    private VolumetricImageCursor[] volumeCursors;
    private AtomicBoolean sequenceChanged;

    /**
     * Creates a cursor for the given sequence {@code seq}.
     */
    public SequenceCursor(Sequence seq)
    {
        this.seq = seq;
        this.volumeCursors = new VolumetricImageCursor[seq.getSizeT()];
        this.sequenceChanged = new AtomicBoolean();
        this.currentT = -1;
    }

    /**
     * Retrieves the intensity of the channel {@code c} of the pixel located at position ({@code x}, {@code y}, {@code z}) at time {@code t}.
     * 
     * @param x
     *        Position in the X-axis.
     * @param y
     *        Position in the Y-axis.
     * @param z
     *        Position in the Z-axis.
     * @param t
     *        Time point index.
     * @param c
     *        Channel index.
     * @return Intensity value at specified position.
     * @throws IndexOutOfBoundsException
     *         If the position is not in the image.
     * @throws RuntimeException
     *         If the data type is not a valid format.
     */
    public double get(int x, int y, int z, int t, int c) throws IndexOutOfBoundsException, RuntimeException
    {
        return getVolumeCursor(t).get(x, y, z, c);
    }

    /**
     * Sets the intensity of the channel {@code c} of the pixel located at position ({@code x}, {@code y}, {@code z}) at time {@code t}.
     * 
     * @param x
     *        Position in the X-axis.
     * @param y
     *        Position in the Y-axis.
     * @param z
     *        Position in the Z-axis.
     * @param t
     *        Time point index.
     * @param c
     *        Channel index.
     * @param val
     *        Intensity value to set.
     * @throws IndexOutOfBoundsException
     *         If the position is not in the image.
     * @throws RuntimeException
     *         If the data type is not a valid format.
     */
    public synchronized void set(int x, int y, int z, int t, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getVolumeCursor(t).set(x, y, z, c, val);
        sequenceChanged.set(true);
    }

    /**
     * Sets the intensity of the channel {@code c} of the pixel located at position ({@code x}, {@code y}, {@code z}) at time {@code t}. This method limits the
     * value of the intensity according to the image data type value range.
     * 
     * @param x
     *        Position in the X-axis.
     * @param y
     *        Position in the Y-axis.
     * @param z
     *        Position in the Z-axis.
     * @param t
     *        Time point index.
     * @param c
     *        Channel index.
     * @param val
     *        Intensity value to set.
     * @throws IndexOutOfBoundsException
     *         If the position is not in the image.
     * @throws RuntimeException
     *         If the data type is not a valid format.
     */
    public synchronized void setSafe(int x, int y, int z, int t, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getVolumeCursor(t).setSafe(x, y, z, c, val);
        sequenceChanged.set(true);
    }

    private int currentT;
    private VolumetricImageCursor currentCursor;

    private synchronized VolumetricImageCursor getVolumeCursor(int t) throws IndexOutOfBoundsException
    {
        if (currentT != t)
        {
            if (volumeCursors[t] == null) {
                volumeCursors[t] = new VolumetricImageCursor(seq, t);
            }
            currentCursor = volumeCursors[t];
            currentT = t;
        }
        return currentCursor;
    }

    /**
     * This method should be called after a set of intensity changes have been made to the target sequence. This methods allows other resources using the target
     * sequence to be informed about the changes made to it.
     */
    public synchronized void commitChanges()
    {
        if (sequenceChanged.get())
        {
            for (int i = 0; i < volumeCursors.length; i++)
            {
                if (volumeCursors[i] != null)
                    volumeCursors[i].commitChanges();
            }
            sequenceChanged.set(false);
        }
    }

    @Override
    public String toString()
    {
        return "last T=" + currentT + " " + currentCursor != null ? currentCursor.toString() : "";
    }
}
