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

package org.bioimageanalysis.extension.kernel.filtering.selection;

import org.bioimageanalysis.icy.model.sequence.Sequence;

public interface SelectionFilter
{

    /**
     * Filter the given sequence with the specified non-linear filter on the specified (square)
     * neighborhood. Note that some operations require double floating-point precision, therefore
     * the input sequence will be internally converted to double precision. However the result will
     * be converted back to the same type as the given input sequence <i>with re-scaling</i>.
     *
     * @param sequence
     *        the sequence to filter (its data will be overwritten)
     * @param radius
     *        the neighborhood radius in each dimension (the actual neighborhood size will be
     *        <code>1+(2*radius)</code> to ensure it is centered on each pixel). If a single
     *        value is given, this value is used for all sequence dimensions. If two values are
     *        given for a 3D sequence, the filter is considered in 2D and applied to each Z
     *        section independently.
     * @throws InterruptedException
     *         If the execution gets cancelled.
     */
    Sequence processSequence(Sequence sequence, int... radius) throws RuntimeException, InterruptedException;

    String getFilterName();

    double processNeighborhood(double currentValue, double[] neighborhood, int neighborhoodSize);

}
