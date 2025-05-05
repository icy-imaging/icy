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
package org.bioimageanalysis.icy.gui.listener;

import org.bioimageanalysis.icy.gui.viewer.Viewer;

import java.util.EventListener;

/**
 * Global {@link Viewer} listener class.
 * Used to listen open, focus and close event for all viewer.
 * 
 * @author Stephane
 */
public interface GlobalViewerListener extends EventListener
{
    /**
     * Viewer was just opened.
     */
    public void viewerOpened(Viewer viewer);

    /**
     * Viewer was just closed.
     */
    public void viewerClosed(Viewer viewer);
    
}
