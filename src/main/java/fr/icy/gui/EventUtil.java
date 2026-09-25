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

package fr.icy.gui;

import fr.icy.system.SystemUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Event-related utilities
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class EventUtil {
    /**
     * Returns true if the Shift key is pressed for the specified event.
     */
    public static boolean isShiftDown(final InputEvent e) {
        return isShiftDown(e, false);
    }

    /**
     * Returns true if the Shift key is pressed for the specified event.
     */
    public static boolean isShiftDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK);

        return e.isShiftDown();
    }

    /**
     * Returns true if the Alt key is pressed for the specified event
     */
    public static boolean isAltDown(final InputEvent e) {
        return isAltDown(e, false);
    }

    /**
     * Returns true if the Alt key is pressed for the specified event.
     */
    public static boolean isAltDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiersEx() == InputEvent.ALT_DOWN_MASK);

        return e.isAltDown();
    }

    /**
     * Returns true if the Ctrl key is pressed for the specified event
     */
    public static boolean isControlDown(final InputEvent e) {
        return isControlDown(e, false);
    }

    /**
     * Returns true if the Ctrl key is pressed for the specified event
     */
    public static boolean isControlDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK);

        return e.isControlDown();
    }

    /**
     * Returns true if the Ctrl/Cmd menu key is pressed for the specified event.
     */
    public static boolean isMenuControlDown(final InputEvent e) {
        return isMenuControlDown(e, false);
    }

    /**
     * Returns true if the Ctrl/Cmd menu key is pressed for the specified event.
     */
    @SuppressWarnings("MagicConstant")
    public static boolean isMenuControlDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiersEx() == SystemUtil.getMenuCtrlMaskEx());

        // take care of OSX CMD key here
        return (e.getModifiersEx() & SystemUtil.getMenuCtrlMaskEx()) != 0;
    }

    /**
     * Returns true if there is no any modifiers in the specified input event.<br>
     * Be careful of how Java manages extended modifiers.
     */
    @Contract(pure = true)
    public static boolean isNoModifierEx(final @NonNull InputEvent e) {
        return e.getModifiersEx() == 0;
    }

    /**
     * Returns true if the mouse event specifies the left mouse button.
     *
     * @param e a MouseEvent object
     * @return true if the left mouse button was active
     */
    public static boolean isLeftMouseButton(final @NonNull MouseEvent e) {
        //return ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        return SwingUtilities.isLeftMouseButton(e);
    }

    /**
     * Returns true if the mouse event specifies the middle mouse button.
     *
     * @param e a MouseEvent object
     * @return true if the middle mouse button was active
     */
    public static boolean isMiddleMouseButton(final @NonNull MouseEvent e) {
        //return ((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK);
        return SwingUtilities.isMiddleMouseButton(e);
    }

    /**
     * Returns true if the mouse event specifies the right mouse button.
     *
     * @param e a MouseEvent object
     * @return true if the right mouse button was active
     * @see SwingUtilities#isRightMouseButton(MouseEvent)
     */
    public static boolean isRightMouseButton(final @NonNull MouseEvent e) {
        //return ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
        return SwingUtilities.isRightMouseButton(e);
    }
}
