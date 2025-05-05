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

package org.bioimageanalysis.extension.kernel.filtering;

import org.bioimageanalysis.extension.kernel.filtering.selection.LocalMaxMethod;
import org.bioimageanalysis.extension.kernel.filtering.selection.SelectionFilter;
import org.bioimageanalysis.icy.model.sequence.Sequence;

public class LocalMaxFiltering
{
    private Sequence sequence;
    private int[] radius;

    public static LocalMaxFiltering create(Sequence sequence, int... radius)
    {
        return new LocalMaxFiltering(sequence, radius);
    }

    private LocalMaxFiltering(Sequence sequence, int... radius)
    {
        this.sequence = sequence;
        this.radius = radius;
    }

    private Sequence result;

    public void computeFiltering() throws RuntimeException, InterruptedException
    {
        SelectionFilter filter = new LocalMaxMethod();
        result = filter.processSequence(this.sequence, this.radius);
    }

    public Sequence getFilteredSequence()
    {
        return result;
    }
}
