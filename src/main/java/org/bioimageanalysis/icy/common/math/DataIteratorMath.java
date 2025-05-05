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
package org.bioimageanalysis.icy.common.math;

import org.bioimageanalysis.icy.common.type.DataIterator;

/**
 * Math utilities for {@link DataIterator} classes.
 *
 * @author Stephane
 */
public class DataIteratorMath {
    /**
     * Returns the sum of all values contained in the specified {@link DataIterator}.
     * Returns <code>0</code> if no value in <code>DataIterator</code>.
     */
    public static double sum(final DataIterator it) throws InterruptedException {
        double result = 0;

        it.reset();

        while (!it.done()) {
            result += it.get();
            it.next();
        }

        return result;
    }

    /**
     * Returns the minimum value found in the specified {@link DataIterator}.
     * Returns <code>Double.MAX_VALUE</code> if no value in <code>DataIterator</code>.
     */
    public static double min(final DataIterator it) throws InterruptedException {
        double result = Double.MAX_VALUE;

        it.reset();

        while (!it.done()) {
            final double value = it.get();
            if (value < result)
                result = value;
            it.next();
        }

        return result;
    }

    /**
     * Returns the maximum value found in the specified {@link DataIterator}.
     * Returns <code>Double.MIN_VALUE</code> if no value in <code>DataIterator</code>.
     */
    public static double max(final DataIterator it) throws InterruptedException {
        double result = -Double.MAX_VALUE;

        it.reset();

        while (!it.done()) {
            final double value = it.get();
            if (value > result)
                result = value;
            it.next();
        }

        return result;
    }

    /**
     * Returns the mean value found in the specified {@link DataIterator}.
     * Returns <code>0</code> if no value in <code>DataIterator</code>.
     */
    public static double mean(final DataIterator it) throws InterruptedException {
        double result = 0;
        long numSample = 0;

        it.reset();

        while (!it.done()) {
            result += it.get();
            numSample++;
            it.next();
        }

        return result / numSample;
    }
}
