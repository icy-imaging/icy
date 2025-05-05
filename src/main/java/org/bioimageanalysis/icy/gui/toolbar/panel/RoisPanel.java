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

import org.bioimageanalysis.icy.gui.component.panel.AbstractRoisPanel;
import org.bioimageanalysis.icy.gui.roi.RoiControlPanel;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIEvent;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventSourceType;

import javax.swing.event.ListSelectionEvent;
import java.awt.*;

/**
 * ROI Panel component displayed in the Icy inspector.<br>
 * Use the {@link AbstractRoisPanel} if you want to embed the ROI table in your own component.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class RoisPanel extends AbstractRoisPanel {
    private static RoisPanel instance = null;

    public static RoisPanel getInstance() {
        if (instance == null)
            instance = new RoisPanel();
        return instance;
    }

    private static final String PREF_ID = "ROIPanel";

    // GUI
    private RoiControlPanel roiControlPanel;

    private RoisPanel() {
        super(GeneralPreferences.getPreferences().node(PREF_ID));

        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    @Override
    protected void initialize() {
        super.initialize();

        // build control panel
        roiControlPanel = new RoiControlPanel(this);

        add(roiControlPanel, BorderLayout.SOUTH);

        validate();
    }

    @Override
    protected ROIResults createNewROIResults(final ROI roi) {
        return new CustomROIResults(roi);
    }

    @Override
    protected Sequence getSequence() {
        return Icy.getMainInterface().getActiveSequence();
    }

    @Override
    protected void refreshTableDataStructureInternal() {
        super.refreshTableDataStructureInternal();

        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    @Override
    protected void refreshTableDataInternal() {
        super.refreshTableDataInternal();

        // notify the ROI control panel that selection changed (force data refresh)
        roiControlPanel.selectionChanged();
    }

    @Override
    protected void refreshTableSelectionInternal() {
        super.refreshTableSelectionInternal();

        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    // called when selection changed in the ROI roiTable
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        super.valueChanged(e);

        // currently changing the selection ? --> exit
        if (e.getValueIsAdjusting())
            return;
        // currently changing the selection ? --> exit
        if (roiSelectionModel.getValueIsAdjusting())
            return;
        // currently changing the selection ? --> exit
        if (modifySelection.availablePermits() <= 0)
            return;

        // notify the ROI control panel that selection changed
        roiControlPanel.selectionChanged();
    }

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        super.activeSequenceChanged(event);

        // if data changed (more or less Z, T or C) we need to refresh action
        // so we can change ROI position correctly
        if (event.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA)
            roiControlPanel.refreshROIActions();
    }

    protected class CustomROIResults extends ROIResults {
        protected CustomROIResults(final ROI roi) {
            super(roi);
        }

        @Override
        public void roiChanged(final ROIEvent event) {
            final ROI r = event.getSource();

            // ROI selected ? --> propagate event to control panel
            if (r.isSelected())
                roiControlPanel.roiChanged(event);

            super.roiChanged(event);
        }
    }
}
