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

package org.bioimageanalysis.icy.gui.frame.sequence;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.gui.frame.ActionFrame;
import org.bioimageanalysis.icy.gui.listener.ActiveSequenceListener;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic frame to do a simple action on the current active sequence.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ActiveSequenceActionFrame extends ActionFrame implements ActiveSequenceListener {
    public interface SourceChangeListener {
        void sourceSequenceChanged(Sequence seq);
    }

    /**
     * listeners and event handler
     */
    private final List<SourceChangeListener> sourceChangeListeners;

    /*
     * gui
     */
    JPanel sourcePanel;
    JLabel sequenceLabel;

    public ActiveSequenceActionFrame(final String title, final boolean resizable, final boolean iconifiable) {
        super(title, resizable, iconifiable);

        sourceChangeListeners = new ArrayList<>();

        buildGUI();

        final Sequence sequence = getSequence();

        if (sequence != null)
            sequenceLabel.setText(sequence.getName());
        else
            sequenceLabel.setText("no sequence");

        // add listener
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    public ActiveSequenceActionFrame(final String title, final boolean resizable) {
        this(title, resizable, false);
    }

    public ActiveSequenceActionFrame(final String title) {
        this(title, false);
    }

    @Override
    public void onClosed() {
        Icy.getMainInterface().removeActiveSequenceListener(this);

        super.onClosed();
    }

    protected void buildGUI() {
        // GUI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        sourcePanel = new JPanel();
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Selected sequence"));
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.LINE_AXIS));

        // sequence label
        sequenceLabel = new JLabel();
        sequenceLabel.setMinimumSize(new Dimension(100, 24));

        sourcePanel.add(Box.createHorizontalStrut(10));
        sourcePanel.add(sequenceLabel);
        sourcePanel.add(Box.createHorizontalGlue());

        // fix the height of source panel
        ComponentUtil.setFixedHeight(sourcePanel, 54);

        mainPanel.add(sourcePanel);
    }

    /**
     * @return the sourcePanel
     */
    public JPanel getSourcePanel() {
        return sourcePanel;
    }

    /**
     * @return the active sequence
     */
    public Sequence getSequence() {
        return Icy.getMainInterface().getActiveSequence();
    }

    public void addSourceChangeListener(final SourceChangeListener listener) {
        if (!sourceChangeListeners.contains(listener))
            sourceChangeListeners.add(listener);
    }

    public void removeSourceChangeListener(final SourceChangeListener listener) {
        sourceChangeListeners.remove(listener);
    }

    private void fireSequenceChangeEvent(final Sequence seq) {
        for (final SourceChangeListener listener : sourceChangeListeners)
            listener.sourceSequenceChanged(seq);
    }

    @Override
    public void sequenceActivated(final Sequence sequence) {
        if (sequence != null)
            sequenceLabel.setText(sequence.getName());
        else
            sequenceLabel.setText("no sequence");

        fireSequenceChangeEvent(sequence);
    }

    @Override
    public void sequenceDeactivated(final Sequence sequence) {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        // nothing here
    }
}
