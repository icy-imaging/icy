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

import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

final class Order0X extends CurveX {
	final private double x;
	final private double y;

	public Order0X(double x, double y) {
		super(INCREASING);
		this.x = x;
		this.y = y;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public double getXTop() {
		return x;
	}

	@Override
	public double getYTop() {
		return y;
	}

	@Override
	public double getXBot() {
		return x;
	}

	@Override
	public double getYBot() {
		return y;
	}

	@Override
	public double getXMin() {
		return x;
	}

	@Override
	public double getXMax() {
		return x;
	}

	@Override
	public double getX0() {
		return x;
	}

	@Override
	public double getY0() {
		return y;
	}

	@Override
	public double getX1() {
		return x;
	}

	@Override
	public double getY1() {
		return y;
	}

	@Override
	public double XforY(double y) {
		return y;
	}

	@Override
	public double TforY(double y) {
		return 0;
	}

	@Override
	public double XforT(double t) {
		return x;
	}

	@Override
	public double YforT(double t) {
		return y;
	}

	@Override
	public double dXforT(double t, int deriv) {
		return 0;
	}

	@Override
	public double dYforT(double t, int deriv) {
		return 0;
	}

	@Override
	public double nextVertical(double t0, double t1) {
		return t1;
	}

	@Override
	public int crossingsFor(double x, double y) {
		return 0;
	}

	@Override
	public boolean accumulateCrossings(CrossingsX c) {
		return (x > c.getXLo() && x < c.getXHi() && y > c.getYLo() && y < c
				.getYHi());
	}

	@Override
	public void enlarge(Rectangle2D r) {
		r.add(x, y);
	}

	@Override
	public CurveX getSubCurve(double ystart, double yend, int dir) {
		return this;
	}

	@Override
	public CurveX getReversedCurve() {
		return this;
	}

	@Override
	public int getSegment(double coords[]) {
		coords[0] = x;
		coords[1] = y;
		return PathIterator.SEG_MOVETO;
	}
}