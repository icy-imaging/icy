/*
 * Copyright 2010-2023 Institut Pasteur.
 *
 * This file is part of Icy.
 *
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
package icy.gui.inspector;

import icy.gui.component.PopupPanel;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.main.ActiveViewerListener;
import icy.gui.sequence.SequenceInfosPanel;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import java.awt.*;

import javax.swing.*;

/**
 * @author Stephane
 * @author Thomas Musset
 *
 * @deprecated Use {@link icy.gui.toolbar.panel.SequencePanel} instead.
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class SequencePanel extends JPanel implements ActiveSequenceListener, ActiveViewerListener {
    private PopupPanel canvasPopupPanel;
    private PopupPanel lutPopupPanel;
    private PopupPanel infosPopupPanel;

    private JPanel canvasPanel;
    private JPanel lutPanel;
    private JPanel infosPanel;

    private SequenceInfosPanel sequenceInfosPanel;

    public SequencePanel() {
        super();

        setPreferredSize(new Dimension(400, 0));

        initialize();
    }

    private void initialize() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        canvasPopupPanel = new PopupPanel("Canvas");
        canvasPanel = canvasPopupPanel.getMainPanel();
        canvasPanel.setLayout(new BorderLayout());
        canvasPopupPanel.expand();
        GridBagConstraints gbc_canvasPopupPanel = new GridBagConstraints();
        gbc_canvasPopupPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_canvasPopupPanel.insets = new Insets(0, 0, 0, 0);
        gbc_canvasPopupPanel.gridx = 0;
        gbc_canvasPopupPanel.gridy = 0;
        add(canvasPopupPanel, gbc_canvasPopupPanel);

        lutPopupPanel = new PopupPanel("Histogram and colormap");
        lutPanel = lutPopupPanel.getMainPanel();
        lutPanel.setLayout(new BorderLayout());
        lutPopupPanel.expand();
        GridBagConstraints gbc_lutPopupPanel = new GridBagConstraints();
        gbc_lutPopupPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_lutPopupPanel.insets = new Insets(0, 0, 0, 0);
        gbc_lutPopupPanel.gridx = 0;
        gbc_lutPopupPanel.gridy = 1;
        add(lutPopupPanel, gbc_lutPopupPanel);

        sequenceInfosPanel = new SequenceInfosPanel();
        sequenceInfosPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        infosPopupPanel = new PopupPanel("Sequence Properties");
        infosPanel = infosPopupPanel.getMainPanel();
        infosPanel.setLayout(new BorderLayout());
        infosPopupPanel.expand();
        infosPanel.add(sequenceInfosPanel, BorderLayout.CENTER);
        GridBagConstraints gbc_infosPopupPanel = new GridBagConstraints();
        gbc_infosPopupPanel.insets = new Insets(0, 0, 0, 0);
        gbc_infosPopupPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_infosPopupPanel.gridx = 0;
        gbc_infosPopupPanel.gridy = 2;
        add(infosPopupPanel, gbc_infosPopupPanel);
    }

    public void setCanvasPanel(JPanel panel) {
        canvasPanel.removeAll();

        if (panel != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            canvasPanel.add(panel, BorderLayout.CENTER);
        }

        canvasPanel.revalidate();
        // we need it for zoom value refresh in detached mode
        // FIXME : normally revalidate should be enough (seems fixed now)
        //canvasPanel.repaint();
    }

    public void setLutPanel(JPanel panel) {
        lutPanel.removeAll();

        if (panel != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            lutPanel.add(panel, BorderLayout.CENTER);
        }

        lutPanel.revalidate();
        // we need it for histogram refresh in detached mode
        // FIXME : normally revalidate should be enough (seems fixed now)
        //lutPanel.repaint();
    }

    @Override
    public void viewerActivated(Viewer viewer) {
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
    public void viewerDeactivated(Viewer viewer) {
        // nothing here
    }

    @Override
    public void activeViewerChanged(ViewerEvent event) {
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
    public void sequenceActivated(Sequence sequence) {
        sequenceInfosPanel.sequenceActivated(sequence);
    }

    @Override
    public void sequenceDeactivated(Sequence sequence) {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event) {
        sequenceInfosPanel.activeSequenceChanged(event);
    }

}
