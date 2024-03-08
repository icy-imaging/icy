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

package icy.action;

import icy.gui.frame.progress.CancelableProgressFrame;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Icy basic AbstractAction class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class IcyAbstractAction extends AbstractAction {
    /**
     * Sets the tooltip text of a component from an Action.
     *
     * @param c the Component to set the tooltip text on
     * @param a the Action to set the tooltip text from, may be null
     */
    public static void setToolTipTextFromAction(final JComponent c, final Action a) {
        if (a != null) {
            final String longDesc = (String) a.getValue(Action.LONG_DESCRIPTION);
            final String shortDesc = (String) a.getValue(Action.SHORT_DESCRIPTION);

            if (StringUtil.isEmpty(longDesc))
                c.setToolTipText(shortDesc);
            else
                c.setToolTipText(longDesc);
        }
    }

    /**
     * The "enabled" property key.
     */
    public static final String ENABLED_KEY = "enabled";

    /**
     * internals
     */
    protected boolean bgProcess;
    protected boolean processing;
    protected String processMessage;
    protected CancelableProgressFrame progressFrame;

    public IcyAbstractAction(
            final String name,
            final String description,
            final String longDescription,
            final int keyCode,
            final int modifiers,
            final boolean bgProcess,
            final String processMessage
    ) {
        super(name);

        // by default we use the name as Action Command
        putValue(ACTION_COMMAND_KEY, name);
        if (keyCode != 0)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
        if (!StringUtil.isEmpty(description))
            putValue(SHORT_DESCRIPTION, description);
        if (!StringUtil.isEmpty(longDescription))
            putValue(LONG_DESCRIPTION, longDescription);

        this.bgProcess = bgProcess;
        this.processMessage = processMessage;
        progressFrame = null;
        processing = false;
    }

    public IcyAbstractAction(final String name, final String description, final String longDescription, final int keyCode, final int modifiers) {
        this(name, description, longDescription, keyCode, modifiers, false, null);
    }

    public IcyAbstractAction(final String name, final String description, final String longDescription, final boolean bgProcess, final String processMessage) {
        this(name, description, longDescription, 0, 0, bgProcess, processMessage);
    }

    public IcyAbstractAction(final String name, final String description, final boolean bgProcess, final String processMessage) {
        this(name, description, null, 0, 0, bgProcess, processMessage);
    }

    public IcyAbstractAction(final String name, final String description, final int keyCode, final int modifiers) {
        this(name, description, null, keyCode, modifiers, false, null);
    }

    public IcyAbstractAction(final String name, final String description, final int keyCode) {
        this(name, description, null, keyCode, 0, false, null);
    }

    public IcyAbstractAction(final String name, final String description, final String longDescription) {
        this(name, description, longDescription, 0, 0, false, null);
    }

    public IcyAbstractAction(final String name, final String description) {
        this(name, description, null, 0, 0, false, null);
    }

    public IcyAbstractAction(final String name) {
        this(name, null, null, 0, 0, false, null);
    }

    /**
     * @return true if this action process is done in a background thread.
     */
    public boolean isBgProcess() {
        return bgProcess;
    }

    /**
     * Set to true if you want to action to be processed in a background thread.
     *
     * @see #isBgProcess()
     * @see #setProcessMessage(String)
     */
    public void setBgProcess(final boolean bgProcess) {
        this.bgProcess = bgProcess;
    }

    /**
     * @return the process message to display for background action process.
     * @see #setProcessMessage(String)
     * @see #isBgProcess()
     */
    public String getProcessMessage() {
        return processMessage;
    }

    /**
     * Set the process message to display for background action process.<br>
     * If set to null then no message is displayed (default).
     *
     * @see #setBgProcess(boolean)
     */
    public void setProcessMessage(final String processMessage) {
        this.processMessage = processMessage;
    }

    public void setName(final String value) {
        putValue(Action.NAME, value);
    }

    public String getName() {
        return (String) getValue(Action.NAME);
    }

    public void setDescription(final String value) {
        putValue(Action.SHORT_DESCRIPTION, value);
    }

    public String getDescription() {
        return (String) getValue(Action.SHORT_DESCRIPTION);
    }

    public void setLongDescription(final String value) {
        putValue(Action.LONG_DESCRIPTION, value);
    }

    public String getLongDescription() {
        return (String) getValue(Action.LONG_DESCRIPTION);
    }

    public void setAccelerator(final int keyCode, final int modifiers) {
        if (keyCode != 0)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
        else
            putValue(ACCELERATOR_KEY, null);
    }

    public void setAccelerator(final int keyCode) {
        setAccelerator(keyCode, 0);
    }

    /**
     * Returns the selected state (for toggle button type).
     */
    public boolean isSelected() {
        return Boolean.TRUE.equals(getValue(SELECTED_KEY));
    }

    /**
     * Sets the selected state (for toggle button type).
     */
    public void setSelected(final boolean value) {
        putValue(SELECTED_KEY, Boolean.valueOf(value));
    }

    /**
     * Returns the {@link KeyStroke} for this action (can be null).
     */
    public KeyStroke getKeyStroke() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }

    /**
     * @return true if action is currently processing.<br>
     * Meaningful only when {@link #setBgProcess(boolean)} is set to true)
     */
    public boolean isInterrupted() {
        return processing;
    }

    /**
     * @return true if action is currently processing.<br>
     * Meaningful only when {@link #setBgProcess(boolean)} is set to true)
     */
    public boolean isProcessing() {
        return processing;
    }

    @Override
    public boolean isEnabled() {
        return enabled && !processing;
    }

    @Override
    public void setEnabled(final boolean value) {
        if (enabled != value) {
            final boolean wasEnabled = isEnabled();
            enabled = value;
            final boolean isEnabled = isEnabled();

            // notify enabled change
            if (wasEnabled != isEnabled)
                firePropertyChange(ENABLED_KEY, Boolean.valueOf(wasEnabled), Boolean.valueOf(isEnabled));
        }
    }

    protected void setProcessing(final boolean value) {
        if (processing != value) {
            final boolean wasEnabled = isEnabled();
            processing = value;
            final boolean isEnabled = isEnabled();

            // notify enabled change
            if (wasEnabled != isEnabled)
                firePropertyChange(ENABLED_KEY, Boolean.valueOf(wasEnabled), Boolean.valueOf(isEnabled));
        }
    }

    /**
     * Helper method to fire enabled changed event (this force component refresh)
     */
    public void enabledChanged() {
        final boolean enabledState = isEnabled();

        // notify enabled change
        firePropertyChange(ENABLED_KEY, Boolean.valueOf(!enabledState), Boolean.valueOf(enabledState));
    }

    /**
     * Returns a {@link JLabel} component representing the action.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public JLabel getLabelComponent(final boolean wantIcon, final boolean wantText) {
        final JLabel result = new JLabel();

        /*if (wantIcon)
            result.setIcon(getIcon());*/
        if (wantText)
            result.setText(getName());

        final String desc = getDescription();

        if (StringUtil.isEmpty(desc))
            result.setToolTipText(getLongDescription());
        else
            result.setToolTipText(getDescription());

        return result;
    }

    /**
     * Returns a {@link JLabel} component representing the action.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public JLabel getLabelComponent() {
        return getLabelComponent(true, true);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        setProcessing(true);

        if (!isBgProcess()) {
            try {
                doAction(e);
            }
            finally {
                setProcessing(false);
            }

            // done !
            return;
        }

        // background processing...
        final String mess = StringUtil.isEmpty(getProcessMessage()) ? "Processing..." : getProcessMessage();
        if (!Icy.getMainInterface().isHeadLess())
            progressFrame = new CancelableProgressFrame(mess);
        else
            progressFrame = null;

        // BG processing thread
        final Thread bgProcessThread = new Thread(() -> {
            try {
                doAction(e);
            }
            finally {
                if (progressFrame != null) {
                    progressFrame.close();
                    progressFrame = null;
                }

                // need to be done on the EDT (can change the enabled state)
                ThreadUtil.invokeLater(() -> setProcessing(false));
            }
        }, "BG task: " + mess);

        // start processing
        bgProcessThread.start();

        // check for cancelation
        ThreadUtil.bgRun(() -> {
            CancelableProgressFrame pf;

            do {
                pf = progressFrame;
                // don't spent too much time on it
                ThreadUtil.sleep(10);
            }
            while ((pf != null) && !pf.isCancelRequested());

            // action canceled ? --> interrupt process
            if (pf != null)
                bgProcessThread.interrupt();
        });
    }

    /**
     * Execute action (delayed execution if action requires it)
     */
    public void execute() {
        actionPerformed(new ActionEvent(this, 0, ""));
    }

    /**
     * Execute action now (wait for execution to complete)
     */
    public boolean executeNow() {
        return doAction(new ActionEvent(this, 0, ""));
    }

    /**
     * Action implementation
     */
    protected abstract boolean doAction(ActionEvent e);
}
