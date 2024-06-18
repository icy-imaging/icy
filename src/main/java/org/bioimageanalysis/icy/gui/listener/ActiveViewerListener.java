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
import org.bioimageanalysis.icy.gui.viewer.ViewerEvent;

import java.util.EventListener;

/**
 * Listener interface for the current active {@link Viewer}.
 * 
 * @author Stephane
 */
public interface ActiveViewerListener extends EventListener
{
    /**
     * Viewer just get the active state.
     * This event is generally preceded by a {@link #viewerDeactivated(Viewer)} event describing
     * the viewer which actually lose activation.
     */
    public void viewerActivated(Viewer viewer);

    /**
     * Viewer just lost the active state.
     * This event is always followed by a {@link #viewerActivated(Viewer)} event describing the
     * new activated viewer.
     */
    public void viewerDeactivated(Viewer viewer);

    /**
     * A property change of current active viewer has changed.
     */
    public void activeViewerChanged(ViewerEvent event);
}
