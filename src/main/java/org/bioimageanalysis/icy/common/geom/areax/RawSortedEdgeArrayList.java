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
 * This software is released as part of the Pumpernickel project.
 * 
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://github.com/mickleness/pumpernickel/raw/master/License.txt
 * 
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package org.bioimageanalysis.icy.common.geom.areax;

import java.util.Comparator;

public class RawSortedEdgeArrayList extends RawEdgeArrayList {

	Comparator<EdgeX> comparator;

	protected RawSortedEdgeArrayList(int initialCapacity,
			Comparator<EdgeX> comparator) {
		super(initialCapacity);
		this.comparator = comparator;
	}

	@Override
	protected void add(EdgeX element) {
		int min = 0;
		int max = elementCount;
		while (min != max) {
			int middle = (min + max) / 2;
			int k = comparator.compare(get(middle), element);
			if (k == 0) {
				min = middle + 1;
				max = middle + 1;
			} else if (k < 1) {
				min = middle + 1;
			} else {
				max = middle;
			}
		}

		ensureCapacity(elementCount + 1);
		if (elementCount - min > 0) {
			System.arraycopy(elementData, min, elementData, min + 1,
					elementCount - min);
		}
		elementData[min] = element;
		elementCount++;
	}
}