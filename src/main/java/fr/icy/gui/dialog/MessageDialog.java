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

package fr.icy.gui.dialog;

import fr.icy.Icy;
import fr.icy.system.thread.ThreadUtil;
import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class MessageDialog {
    private static final Logger LOGGER = Logger.getLogger(MessageDialog.class.getName());

    public static void showDialog(final String message) {
        showDialog("Information", message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDialog(final String message, final @MagicConstant(intValues = {JOptionPane.INFORMATION_MESSAGE, JOptionPane.ERROR_MESSAGE, JOptionPane.WARNING_MESSAGE, JOptionPane.QUESTION_MESSAGE,JOptionPane.PLAIN_MESSAGE}) int messageType) {
        final String title = switch (messageType) {
            case JOptionPane.INFORMATION_MESSAGE -> "Information";
            case JOptionPane.WARNING_MESSAGE -> "Warning";
            case JOptionPane.ERROR_MESSAGE -> "Error";
            case JOptionPane.QUESTION_MESSAGE -> "Confirmation";
            default -> "Message";
        };

        showDialog(title, message, messageType);
    }

    public static void showDialog(final String title, final String message) {
        showDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDialog(final String title, final String message, final @MagicConstant(intValues = {JOptionPane.INFORMATION_MESSAGE, JOptionPane.ERROR_MESSAGE, JOptionPane.WARNING_MESSAGE, JOptionPane.QUESTION_MESSAGE,JOptionPane.PLAIN_MESSAGE}) int messageType) {
        if (!Icy.getMainInterface().isHeadLess()) {
            ThreadUtil.invokeLater(() -> {
                final JFrame parent = Icy.getMainInterface().getMainFrame();
                JOptionPane.showMessageDialog(parent, message, title, messageType);
            });
        }
        else {
            if (messageType == JOptionPane.ERROR_MESSAGE)
                LOGGER.severe(title + ": " + message);
            else
                LOGGER.info(title + ": " + message);
        }
    }
}
