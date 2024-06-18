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

package org.bioimageanalysis.icy.gui.action;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.gui.listener.ActiveSequenceListener;
import org.bioimageanalysis.icy.gui.listener.GlobalROIListener;
import org.bioimageanalysis.icy.gui.listener.GlobalSequenceListener;
import org.bioimageanalysis.icy.gui.listener.GlobalViewerListener;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class ActionManager {
    /**
     * All registered action
     */
    public static List<IcyAbstractAction> activeSequenceActions = null;
    public static List<IcyAbstractAction> globalROIActions = null;
    public static List<IcyAbstractAction> globalSequenceActions = null;
    public static List<IcyAbstractAction> globalViewerActions = null;

    public static synchronized void init() {
        // init actions
        if (activeSequenceActions == null) {
            activeSequenceActions = new ArrayList<>();

            // add all kernels actions
            activeSequenceActions.addAll(FileActions.getAllActiveSequenceActions());
            activeSequenceActions.addAll(SequenceOperationActions.getAllActiveSequenceActions());
            activeSequenceActions.addAll(RoiActions.getAllActiveSequenceActions());

            activeSequenceActions.addAll(CanvasActions.getAllActions());
            activeSequenceActions.addAll(ViewerActions.getAllActions());
            activeSequenceActions.addAll(WindowActions.getAllActions());

            final ActiveSequenceListener activeSequenceListener = new ActiveSequenceListener() {
                @Override
                public void sequenceDeactivated(final Sequence sequence) {
                    // nothing here
                }

                @Override
                public void sequenceActivated(final Sequence sequence) {
                    // force action components refresh
                    for (final IcyAbstractAction action : activeSequenceActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }

                @Override
                public void activeSequenceChanged(final SequenceEvent event) {
                    // nothing here
                }
            };

            // listen these event
            Icy.getMainInterface().addActiveSequenceListener(activeSequenceListener);
        }

        if (globalROIActions == null) {
            globalROIActions = new ArrayList<>();

            final GlobalROIListener globalROIListener = new GlobalROIListener() {
                @Override
                public void roiAdded(final ROI roi) {
                    // force action components refresh
                    for (final IcyAbstractAction action : globalROIActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }

                @Override
                public void roiRemoved(final ROI roi) {
                    // force action components refresh
                    for (final IcyAbstractAction action : globalROIActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }
            };

            // listen these event
            Icy.getMainInterface().addGlobalROIListener(globalROIListener);
        }

        if (globalSequenceActions == null) {
            globalSequenceActions = new ArrayList<>();

            final GlobalSequenceListener globalSequenceListener = new GlobalSequenceListener() {
                @Override
                public void sequenceOpened(final Sequence sequence) {
                    // force action components refresh
                    for (final IcyAbstractAction action : globalSequenceActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }

                @Override
                public void sequenceClosed(final Sequence sequence) {
                    // force action components refresh
                    for (final IcyAbstractAction action : globalSequenceActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }
            };

            // listen these event
            Icy.getMainInterface().addGlobalSequenceListener(globalSequenceListener);
        }

        if (globalViewerActions == null) {
            globalViewerActions = new ArrayList<>();

            globalViewerActions.addAll(FileActions.getAllGlobalViewerActions());
            globalViewerActions.addAll(SequenceOperationActions.getAllGlobalViewerActions());

            final GlobalViewerListener globalViewerListener = new GlobalViewerListener() {
                @Override
                public void viewerOpened(final Viewer viewer) {
                    // force action components refresh
                    for (final IcyAbstractAction action : globalViewerActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }

                @Override
                public void viewerClosed(final Viewer viewer) {
                    // force action components refresh
                    for (final IcyAbstractAction action : globalViewerActions) {
                        action.enabledChanged();
                        action.setSelected(action.isSelected());
                    }
                }
            };

            // listen these event
            Icy.getMainInterface().addGlobalViewerListener(globalViewerListener);
        }
    }
}
