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

package icy.gui.frame.progress;

import icy.system.thread.ThreadUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class AnnounceFrame extends TaskFrame implements ActionListener {
    protected JButton button;
    protected JLabel label;
    protected Timer timer;
    protected Runnable action;

    /**
     * Show an announcement with specified message.
     *
     * @param message
     *        message to display in announcement
     */
    public AnnounceFrame(final String message) {
        this(message, "Ok", null, 0);
    }

    /**
     * Show an announcement with specified parameters.
     *
     * @param message
     *        message to display in announcement
     * @param btnAction
     *        action on button click
     */
    public AnnounceFrame(final String message, final Runnable btnAction) {
        this(message, "Ok", btnAction, 0);
    }

    /**
     * Show an announcement with specified parameters
     *
     * @param message
     *        message to display in announcement
     * @param liveTime
     *        life time in second (0 = infinite)
     */
    public AnnounceFrame(final String message, final int liveTime) {
        this(message, "Ok", null, liveTime);
    }

    /**
     * Show an announcement with specified parameters
     *
     * @param message
     *        message to display in announcement
     * @param btnAction
     *        action on button click
     * @param liveTime
     *        life time in second (0 = infinite)
     */

    public AnnounceFrame(final String message, final Runnable btnAction, final int liveTime) {
        this(message, "Ok", btnAction, liveTime);
    }

    /**
     * Show an announcement with specified parameters
     *
     * @param message
     *        message to display in announcement
     * @param buttonText
     *        button text
     * @param btnAction
     *        action on button click
     * @param liveTime
     *        life time in second (0 = infinite)
     */
    public AnnounceFrame(final String message, final String buttonText, final Runnable btnAction, final int liveTime) {
        super("");

        // don't try to go further
        if (headless)
            return;

        if (liveTime != 0) {
            timer = new Timer("Announce timer");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // close frame (EDT safe)
                    close();
                }
            }, liveTime * 1000L);
        }

        ThreadUtil.invokeLater(() -> {
            button = new JButton();
            label = new JLabel();
            action = btnAction;

            //label.setText("   " + message + "   ");
            label.setText("<html><b>" + message + "</b></html>");
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            button.setText(buttonText);
            button.setFocusable(false);
            button.addActionListener(AnnounceFrame.this);

            mainPanel.setLayout(new BorderLayout());

            mainPanel.add(label, BorderLayout.CENTER);
            mainPanel.add(button, BorderLayout.EAST);

            pack();
        });
    }

    /**
     * @return the action
     */
    public Runnable getAction() {
        return action;
    }

    /**
     * @param action
     *        the action to set
     */
    public void setAction(final Runnable action) {
        this.action = action;
    }

    @Override
    public void internalClose() {
        // don't try to go further
        if (headless)
            return;

        // stop timer
        if (timer != null)
            timer.cancel();

        super.internalClose();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // don't try to go further
        if (headless)
            return;

        if (e.getSource() == button) {
            // execute action
            if (action != null)
                action.run();
        }

        // close frame on both action (timer or button)
        close();
    }

}
