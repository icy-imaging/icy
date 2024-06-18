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
package org.bioimageanalysis.icy.gui;

import org.bioimageanalysis.icy.system.SystemUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Event related utilities
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class EventUtil {
    /**
     * Returns true if Shift key is pressed for the specified event.
     */
    public static boolean isShiftDown(final InputEvent e) {
        return isShiftDown(e, false);
    }

    /**
     * Returns true if Shift key is pressed for the specified event.
     */
    @SuppressWarnings("deprecation")
    public static boolean isShiftDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiers() == InputEvent.SHIFT_MASK);

        return e.isShiftDown();
    }

    /**
     * Returns true if Alt key is pressed for the specified event
     */
    public static boolean isAltDown(final InputEvent e) {
        return isAltDown(e, false);
    }

    /**
     * Returns true if Alt key is pressed for the specified event.
     */
    @SuppressWarnings("deprecation")
    public static boolean isAltDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiers() == InputEvent.ALT_MASK);

        return e.isAltDown();
    }

    /**
     * Returns true if Ctrl key is pressed for the specified event
     */
    public static boolean isControlDown(final InputEvent e) {
        return isControlDown(e, false);
    }

    /**
     * Returns true if Ctrl key is pressed for the specified event
     */
    @SuppressWarnings("deprecation")
    public static boolean isControlDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiers() == InputEvent.CTRL_MASK);

        return e.isControlDown();
    }

    /**
     * Returns true if Ctrl/Cmd menu key is pressed for the specified event.
     */
    public static boolean isMenuControlDown(final InputEvent e) {
        return isMenuControlDown(e, false);
    }

    /**
     * Returns true if Ctrl/Cmd menu key is pressed for the specified event.
     */
    @SuppressWarnings({"deprecation", "MagicConstant"})
    public static boolean isMenuControlDown(final InputEvent e, final boolean exclusive) {
        if (exclusive)
            return (e.getModifiers() == SystemUtil.getMenuCtrlMask());

        // take care of OSX CMD key here
        return (e.getModifiers() & SystemUtil.getMenuCtrlMask()) != 0;
    }

    /**
     * Returns true if there is no any modifiers in the specified input event.
     */
    @SuppressWarnings("deprecation")
    public static boolean isNoModifier(final @NotNull InputEvent e) {
        return e.getModifiers() == 0;
    }

    /**
     * Returns true if there is no any modifiers in the specified input event.<br>
     * Be careful of how extended modifiers are managed by Java.
     */
    @Contract(pure = true)
    public static boolean isNoModifierEx(final @NotNull InputEvent e) {
        return e.getModifiersEx() == 0;
    }

    /**
     * Returns true if the mouse event specifies the left mouse button.
     *
     * @param e a MouseEvent object
     * @return true if the left mouse button was active
     */
    public static boolean isLeftMouseButton(final @NotNull MouseEvent e) {
        //return ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK);
        return SwingUtilities.isLeftMouseButton(e);
    }

    /**
     * Returns true if the mouse event specifies the middle mouse button.
     *
     * @param e a MouseEvent object
     * @return true if the middle mouse button was active
     */
    public static boolean isMiddleMouseButton(final @NotNull MouseEvent e) {
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
    public static boolean isRightMouseButton(final @NotNull MouseEvent e) {
        //return ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
        return SwingUtilities.isRightMouseButton(e);
    }
}
