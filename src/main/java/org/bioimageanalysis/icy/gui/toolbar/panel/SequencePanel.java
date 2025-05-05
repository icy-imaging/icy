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

package org.bioimageanalysis.icy.gui.toolbar.panel;

import org.bioimageanalysis.icy.gui.component.panel.PopupPanel;
import org.bioimageanalysis.icy.gui.listener.ActiveSequenceListener;
import org.bioimageanalysis.icy.gui.listener.ActiveViewerListener;
import org.bioimageanalysis.icy.gui.sequence.SequenceInfosPanel;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.gui.viewer.ViewerEvent;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;

import javax.swing.*;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class SequencePanel extends ToolbarPanel implements ActiveSequenceListener, ActiveViewerListener {
    private static SequencePanel instance = null;

    public static SequencePanel getInstance() {
        if (instance == null)
            instance = new SequencePanel();
        return instance;
    }

    private final JPanel canvasPanel;
    private final JPanel lutPanel;

    private final SequenceInfosPanel sequenceInfosPanel;

    private SequencePanel() {
        super(new Dimension(400, 0));

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final PopupPanel canvasPopupPanel = new PopupPanel("Canvas");
        canvasPanel = canvasPopupPanel.getMainPanel();
        canvasPanel.setLayout(new BorderLayout());
        canvasPopupPanel.expand();
        final GridBagConstraints gbc_canvasPopupPanel = new GridBagConstraints();
        gbc_canvasPopupPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_canvasPopupPanel.insets = new Insets(0, 0, 0, 0);
        gbc_canvasPopupPanel.gridx = 0;
        gbc_canvasPopupPanel.gridy = 0;
        add(canvasPopupPanel, gbc_canvasPopupPanel);

        final PopupPanel lutPopupPanel = new PopupPanel("Histogram and colormap");
        lutPanel = lutPopupPanel.getMainPanel();
        lutPanel.setLayout(new BorderLayout());
        lutPopupPanel.expand();
        final GridBagConstraints gbc_lutPopupPanel = new GridBagConstraints();
        gbc_lutPopupPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_lutPopupPanel.insets = new Insets(0, 0, 0, 0);
        gbc_lutPopupPanel.gridx = 0;
        gbc_lutPopupPanel.gridy = 1;
        add(lutPopupPanel, gbc_lutPopupPanel);

        sequenceInfosPanel = new SequenceInfosPanel();
        sequenceInfosPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        final PopupPanel infosPopupPanel = new PopupPanel("Sequence Properties");
        final JPanel infosPanel = infosPopupPanel.getMainPanel();
        infosPanel.setLayout(new BorderLayout());
        infosPopupPanel.expand();
        infosPanel.add(sequenceInfosPanel, BorderLayout.CENTER);
        final GridBagConstraints gbc_infosPopupPanel = new GridBagConstraints();
        gbc_infosPopupPanel.insets = new Insets(0, 0, 0, 0);
        gbc_infosPopupPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_infosPopupPanel.gridx = 0;
        gbc_infosPopupPanel.gridy = 2;
        add(infosPopupPanel, gbc_infosPopupPanel);

        Icy.getMainInterface().addActiveViewerListener(this);
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    private void setCanvasPanel(final JPanel panel) {
        canvasPanel.removeAll();

        if (panel != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            canvasPanel.add(panel, BorderLayout.CENTER);
        }

        canvasPanel.revalidate();
    }

    private void setLutPanel(final JPanel panel) {
        lutPanel.removeAll();

        if (panel != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            lutPanel.add(panel, BorderLayout.CENTER);
        }

        lutPanel.revalidate();
    }

    @Override
    public void viewerActivated(final Viewer viewer) {
        if (viewer != null) {
            setLutPanel(viewer.getLutViewer());
            setCanvasPanel(viewer.getCanvasPanel());
        }
        else {
            setLutPanel(null);
            setCanvasPanel(null);
        }
    }

    @Override
    public void viewerDeactivated(final Viewer viewer) {
        // nothing here
    }

    @Override
    public void activeViewerChanged(final ViewerEvent event) {
        // we receive from current focused viewer only
        switch (event.getType()) {
            case CANVAS_CHANGED:
                // refresh canvas panel
                setCanvasPanel(event.getSource().getCanvasPanel());
                break;

            case LUT_CHANGED:
                // refresh lut panel
                setLutPanel(event.getSource().getLutViewer());
                break;

            case POSITION_CHANGED:
                // nothing to do
                break;
        }
    }

    @Override
    public void sequenceActivated(final Sequence sequence) {
        sequenceInfosPanel.sequenceActivated(sequence);
    }

    @Override
    public void sequenceDeactivated(final Sequence sequence) {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        sequenceInfosPanel.activeSequenceChanged(event);
    }

}
