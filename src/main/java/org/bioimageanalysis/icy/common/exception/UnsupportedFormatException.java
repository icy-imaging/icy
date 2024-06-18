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
package org.bioimageanalysis.icy.common.exception;

/**
 * UnsupportedFormatException is the exception thrown when try to load a resource and the format in
 * not recognized or incorrect.
 * 
 * @author Stephane
 */
public class UnsupportedFormatException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = -1571266483842584203L;

    /**
     * 
     */
    public UnsupportedFormatException()
    {
        super();
    }

    public UnsupportedFormatException(String message)
    {
        super(message);
    }

    public UnsupportedFormatException(Throwable cause)
    {
        super(cause);
    }

    public UnsupportedFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
