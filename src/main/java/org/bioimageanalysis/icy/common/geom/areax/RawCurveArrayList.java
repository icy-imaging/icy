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

public class RawCurveArrayList extends CurveList {

	protected RawCurveArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Removes all of the elements from this list. The list will be empty after
	 * this call returns.
	 */
	protected void clear() {
		// Let gc do its work
		for (int i = 0; i < elementCount; i++)
			elementData[i] = null;

		elementCount = 0;
	}

	protected CurveX[] getArray() {
		return elementData;
	}

	protected CurveX[] getArray(int minArraySize) {
		ensureCapacity(minArraySize);
		return elementData;
	}
}