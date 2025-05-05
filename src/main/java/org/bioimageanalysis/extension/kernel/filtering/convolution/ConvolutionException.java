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

package org.bioimageanalysis.extension.kernel.filtering.convolution;

public class ConvolutionException extends RuntimeException
{

    static final long serialVersionUID = -9042546074193202791L;

    public ConvolutionException()
    {
        super();
    }

    public ConvolutionException(String message)
    {
        super(message);
    }

    public ConvolutionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
