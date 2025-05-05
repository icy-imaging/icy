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

package org.bioimageanalysis.icy.gui.frame.progress;

import org.bioimageanalysis.icy.common.listener.ProgressListener;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * A progress TaskFrame (thread safe)
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ProgressFrame extends TaskFrame implements ProgressListener, Runnable {
    /**
     * gui
     */
    JProgressBar progressBar;

    /**
     * length (in bytes) of download
     */
    protected double length;
    /**
     * current position (in bytes) of download
     */
    protected double position;

    /**
     * current message
     */
    protected String message;
    /**
     * current tooltip
     */
    protected String tooltip;

    /**
     * internals
     */
    // private final SingleProcessor processor;
    public ProgressFrame(final String message) {
        super("");

        // default
        length = 100d;
        position = -1d;
        // processor = new SingleProcessor(true);
        this.message = message;

        // don't try to go further
        if (headless)
            return;

        ThreadUtil.invokeLater(() -> {
            progressBar = new JProgressBar();
            progressBar.setString(buildMessage(message));
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setBorder(BorderFactory.createEmptyBorder());
            progressBar.setMinimum(0);
            // this is enough for a smooth progress
            progressBar.setMaximum(1000);

            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(progressBar, BorderLayout.CENTER);

            pack();
        });
    }

    protected String buildMessage(final String message) {
        return "  " + message + "  ";
    }

    @Override
    public void run() {
        // don't spent too much time in EDT
        ThreadUtil.sleep(10);
        updateDisplay();
    }

    public void refresh() {
        // refresh (need single delayed execution)
        ThreadUtil.runSingle(this);
    }

    protected void updateDisplay() {
        // don't try to go further
        if (headless)
            return;

        // repacking need to be done on EDT
        ThreadUtil.invokeNow(() -> {
            // position information
            if ((position != -1d) && (length > 0d)) {
                // remove indeterminate state
                if (progressBar.isIndeterminate())
                    progressBar.setIndeterminate(false);

                // set progress
                final int value = (int) (position * 1000d / length);
                if (progressBar.getValue() != value)
                    progressBar.setValue(value);
            }
            else {
                // set indeterminate state
                if (!progressBar.isIndeterminate())
                    progressBar.setIndeterminate(true);
            }

            final String text = buildMessage(message);

            // set progress message
            if (!StringUtil.equals(progressBar.getString(), text)) {
                progressBar.setString(text);
                // so component is resized according to its string length
                progressBar.invalidate();

                // repack frame
                pack();
            }

            // set tooltip
            if (!StringUtil.equals(progressBar.getToolTipText(), tooltip))
                progressBar.setToolTipText(tooltip);
        });
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String value) {
        if (!Objects.equals(message, value)) {
            message = value;
            refresh();
        }
    }

    // we want tooltip set on the progress component only
    @Override
    public void setToolTipText(final String value) {
        if (!Objects.equals(tooltip, value)) {
            tooltip = value;
            refresh();
        }
    }

    /**
     * @return the length
     */
    public double getLength() {
        return length;
    }

    /**
     * @param value
     *        the length to set
     */
    public void setLength(final double value) {
        if (length != value) {
            length = value;
            refresh();
        }
    }

    /**
     * @return the position
     */
    public double getPosition() {
        return position;
    }

    /**
     * increment progress position
     */
    public void incPosition() {
        setPosition(position + 1);
    }

    /**
     * @param value
     *        the position to set
     */
    public void setPosition(final double value) {
        if (position != value) {
            position = value;
            refresh();
        }
    }

    @Override
    public boolean notifyProgress(final double position, final double length) {
        setPosition(position);
        setLength(length);

        return true;
    }
}
