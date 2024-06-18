/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package org.bioimageanalysis.icy.extension.plugin.classloader.exception;

/**
 * General custom exception
 *
 * @author Kamran Zafar
 * @author Thomas Musset
 */
public class JclException extends RuntimeException {
    /**
     * Default constructor
     */
    public JclException() {
        super();
    }

    public JclException(final String message) {
        super(message);
    }

    public JclException(final Throwable cause) {
        super(cause);
    }

    public JclException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
