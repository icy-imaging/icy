/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.gui.frame;

import java.util.EventListener;

/**
 * @author Stéphane Dallongeville
 */
public interface IcyFrameListener extends EventListener {
    /**
     * Invoked the first time the icyFrame is made visible.
     */
    void icyFrameOpened(IcyFrameEvent e);

    /**
     * Invoked when the user attempts to close the icyFrame from the icyFrame's system menu.
     */
    void icyFrameClosing(IcyFrameEvent e);

    /**
     * Invoked when an icyFrame has been closed as the result of calling disposal on the icyFrame.
     */
    void icyFrameClosed(IcyFrameEvent e);

    /**
     * Invoked when an icyFrame is changed from a normal to a minimized state. For many platforms, a
     * minimized icyFrame is displayed as the icon specified in the icyFrame's iconImage property.
     */
    void icyFrameIconified(IcyFrameEvent e);

    /**
     * Invoked when an icyFrame is changed from a minimized to a normal state.
     */
    void icyFrameDeiconified(IcyFrameEvent e);

    /**
     * Invoked when the icyFrame is set to be the active icyFrame. The active Window is always
     * either the focused Window or the first Frame or Dialog that is an owner of the focused
     * Window.
     */
    void icyFrameActivated(IcyFrameEvent e);

    /**
     * Invoked when an icyFrame is no longer the active Window. The active Window is always either
     * the focused Window or the first Frame or Dialog that is an owner of the focused Window.
     */
    void icyFrameDeactivated(IcyFrameEvent e);

    /**
     * Invoked when an IcyFrame is changed to externalized to internalized state
     */
    void icyFrameInternalized(IcyFrameEvent e);

    /**
     * Invoked when an IcyFrame is changed to internalized to externalized state
     */
    void icyFrameExternalized(IcyFrameEvent e);
}
