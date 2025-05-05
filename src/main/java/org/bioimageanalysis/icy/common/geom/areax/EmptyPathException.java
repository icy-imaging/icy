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

/**
 * This indicates that a path had no shape data.
 * <P>
 * This means it had no lines, quadratic or cubic segments in it (although it
 * may have had a MOVE_TO and a CLOSE segment).
 *
 */
public class EmptyPathException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EmptyPathException() {
	}

	public EmptyPathException(String message) {
		super(message);
	}

	public EmptyPathException(Throwable cause) {
		super(cause);
	}

	public EmptyPathException(String message, Throwable cause) {
		super(message, cause);
	}

}