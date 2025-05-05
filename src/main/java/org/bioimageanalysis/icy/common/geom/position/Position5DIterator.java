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
package org.bioimageanalysis.icy.common.geom.position;

import java.util.NoSuchElementException;

import org.bioimageanalysis.icy.common.geom.point.Point5D;

/**
 * Position 5D iterator.
 * 
 * @author Stephane
 */
public interface Position5DIterator
{
    /**
     * Reset iterator to initial position.
     */
    public void reset() throws InterruptedException;

    /**
     * Pass to the next element.
     * 
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public void next() throws NoSuchElementException, InterruptedException;

    /**
     * Returns <i>true</i> if the iterator has no more elements.
     */
    public boolean done();

    /**
     * @return the current position of the iterator
     * @exception NoSuchElementException
     *            iteration has no more elements.
     */
    public Point5D get() throws NoSuchElementException;

    /**
     * @return the current position X of the iterator
     */
    public int getX() throws NoSuchElementException;

    /**
     * @return the current position Y of the iterator
     */
    public int getY() throws NoSuchElementException;;

    /**
     * @return the current position Z of the iterator
     */
    public int getZ() throws NoSuchElementException;;

    /**
     * @return the current position T of the iterator
     */
    public int getT() throws NoSuchElementException;;

    /**
     * @return the current position C of the iterator
     */
    public int getC() throws NoSuchElementException;;
}
