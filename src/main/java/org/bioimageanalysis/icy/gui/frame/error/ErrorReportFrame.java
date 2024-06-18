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

package org.bioimageanalysis.icy.gui.frame.error;

import org.bioimageanalysis.icy.gui.frame.IcyFrame;
import org.bioimageanalysis.icy.gui.frame.TitledFrame;
import org.bioimageanalysis.icy.gui.frame.progress.ProgressFrame;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.logging.IcyLogger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ErrorReportFrame extends TitledFrame implements ActionListener {
    /**
     * @return This function test if we already have an active error report frame opened.
     */
    public static boolean hasErrorFrameOpened() {
        return !IcyFrame.getAllFrames(ErrorReportFrame.class).isEmpty();
    }

    // GUI
    protected ErrorReportPanel panel;

    // internals
    protected ActionListener reportAction;

    /**
     * Create the frame.
     * @param icon icon
     * @param title string
     * @param message string
     */
    public ErrorReportFrame(final Icon icon, final String title, final String message) {
        super("Bug report", true, true, true, true);

        panel = new ErrorReportPanel(icon, title, message);

        panel.reportButton.addActionListener(this);
        panel.closeButton.addActionListener(this);

        // default report action
        reportAction = e -> {
            final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

            try {
                IcyExceptionHandler.report(panel.getReportMessage());
            }
            catch (final BadLocationException ex) {
                IcyLogger.error(ErrorReportFrame.class, ex, "Error while reporting error.");
            }
            finally {
                progressFrame.close();
            }
        };

        mainPanel.add(panel, BorderLayout.CENTER);

        addToDesktopPane();
        setSize(new Dimension(640, 450));
        setVisible(true);
        requestFocus();
        center();
    }

    /**
     * @return Returns formatted report message (ready to send to web site).
     *
     * @throws BadLocationException exceptioj
     */
    public String getReportMessage() throws BadLocationException {
        return panel.getReportMessage();
    }

    /**
     * @param action Set a specific action on the report button
     */
    public void setReportAction(final ActionListener action) {
        reportAction = action;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ((e.getSource() == panel.reportButton) && (reportAction != null))
            reportAction.actionPerformed(e);

        close();
    }
}
