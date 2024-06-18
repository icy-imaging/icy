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

package org.bioimageanalysis.icy.common.type;

import org.bioimageanalysis.icy.model.image.ImageDataIterator;
import org.bioimageanalysis.icy.model.sequence.SequenceDataIterator;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for {@link DataIterator} classes.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class DataIteratorUtil {
    /**
     * Returns the number of element contained in the specified {@link DataIterator}.
     */
    public static long count(final @NotNull DataIterator it) throws InterruptedException {
        long result = 0;

        it.reset();

        while (!it.done()) {
            it.next();
            result++;

            // check for interruption from time to time as this can be a long process
            if (((result & 0xFFF) == 0xFFF) && Thread.interrupted())
                throw new InterruptedException("DataIteratorUtil.count(..) process interrupted.");
        }

        return result;
    }

    /**
     * Sets the specified value to the specified {@link DataIterator}.
     */
    public static void set(final DataIterator it, final double value) throws InterruptedException {
        it.reset();

        try {
            int i = 0;
            while (!it.done()) {
                it.set(value);
                it.next();

                // check for interruption from time to time as this can be a long process
                if (((i & 0xFFF) == 0xFFF) && Thread.interrupted())
                    throw new InterruptedException("DataIteratorUtil.set(..) process interrupted.");
            }
        }
        finally {
            // not really nice to do that here, but it's to preserve backward compatibility
            if (it instanceof SequenceDataIterator)
                ((SequenceDataIterator) it).flush();
            else if (it instanceof ImageDataIterator)
                ((ImageDataIterator) it).flush();
        }
    }
}
