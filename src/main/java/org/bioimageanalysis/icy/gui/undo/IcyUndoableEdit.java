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

package org.bioimageanalysis.icy.gui.undo;

import javax.swing.*;
import javax.swing.undo.UndoableEdit;

/**
 * Icy {@link UndoableEdit} interface
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public interface IcyUndoableEdit extends UndoableEdit {
    /**
     * @return Retrieve source of this edit
     */
    Object getSource();

    /**
     * @return the icon
     */
    Icon getIcon();

    /**
     * @return <code>true</code> if this edit can be merged with a compatible edit
     */
    boolean isMergeable();
}