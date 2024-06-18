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

import org.bioimageanalysis.icy.common.listener.weak.WeakListener;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.gui.viewer.ViewerEvent;
import org.bioimageanalysis.icy.Icy;

/**
 * @author Stephane
 */
public class WeakActiveViewerListener extends WeakListener<ActiveViewerListener> implements ActiveViewerListener
{
    public WeakActiveViewerListener(ActiveViewerListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        Icy.getMainInterface().removeActiveViewerListener(this);
    }

    @Override
    public void viewerActivated(Viewer viewer)
    {
        final ActiveViewerListener listener = getListener(null);

        if (listener != null)
            listener.viewerActivated(viewer);
    }

    @Override
    public void viewerDeactivated(Viewer viewer)
    {
        final ActiveViewerListener listener = getListener(null);

        if (listener != null)
            listener.viewerDeactivated(viewer);
    }

    @Override
    public void activeViewerChanged(ViewerEvent event)
    {
        final ActiveViewerListener listener = getListener(null);

        if (listener != null)
            listener.activeViewerChanged(event);
    }
}
