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

package org.bioimageanalysis.icy.gui.dialog;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import javax.swing.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class MessageDialog {
    public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
    /** Used for information messages. */
    public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
    /** Used for warning messages. */
    public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
    /** Used for questions. */
    public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
    /** No icon is used. */
    public static final int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;

    public static void showDialog(final String message) {
        showDialog("Information", message, INFORMATION_MESSAGE);
    }

    public static void showDialog(final String message, final int messageType) {
        final String title = switch (messageType) {
            case INFORMATION_MESSAGE -> "Information";
            case WARNING_MESSAGE -> "Warning";
            case ERROR_MESSAGE -> "Error";
            case QUESTION_MESSAGE -> "Confirmation";
            default -> "Message";
        };

        showDialog(title, message, messageType);
    }

    public static void showDialog(final String title, final String message) {
        showDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDialog(final String title, final String message, final int messageType) {
        if (!Icy.getMainInterface().isHeadLess()) {
            ThreadUtil.invokeLater(() -> {
                final JFrame parent = Icy.getMainInterface().getMainFrame();
                JOptionPane.showMessageDialog(parent, message, title, messageType);
            });
        }
        else {
            if (messageType == ERROR_MESSAGE)
                IcyLogger.error(MessageDialog.class, title + ": " + message);
            else
                IcyLogger.info(MessageDialog.class, title + ": " + message);
        }
    }
}
