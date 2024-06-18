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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * An unperformed operation to an <code>AreaX</code>.
 */
public class AreaXOperation {
	public static final int ADD = 0;
	public static final int SUBTRACT = 1;
	public static final int XOR = 2;
	public static final int INTERSECT = 3;

	protected final Shape operand;
	protected final int operator;

	Rectangle2D bounds;

	public AreaXOperation(Shape shape, int operator) {
		if (!(operator == ADD || operator == SUBTRACT || operator == XOR || operator == INTERSECT))
			throw new IllegalArgumentException("unrecognized operator ("
					+ operator + ")");
		if (shape == null)
			throw new NullPointerException();

		this.operand = shape;
		this.operator = operator;
	}

	public int getOperator() {
		return operator;
	}

	public Shape getOperand() {
		return operand;
	}

	public Rectangle2D getBounds() {
		if (bounds == null) {
			if (operand instanceof AreaX || operand instanceof Area) {
				bounds = operand.getBounds2D();
			} else {
				bounds = ShapeBounds.getBounds(operand);
			}
		}
		return bounds;
	}
}