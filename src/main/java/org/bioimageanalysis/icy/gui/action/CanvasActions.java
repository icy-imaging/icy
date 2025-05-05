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
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.gui.canvas.Layer;
import org.bioimageanalysis.icy.gui.listener.ActiveViewerListener;
import org.bioimageanalysis.icy.gui.listener.WeakActiveViewerListener;
import org.bioimageanalysis.icy.gui.toolbar.panel.LayersPanel;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.gui.viewer.ViewerEvent;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Canvas associated actions (disable/enable layers, fit, remove layer...)
 *
 * @author Stephane
 * @author Thomas Musset
 */
public final class CanvasActions {
    public static final class ToggleLayersAction extends IcyAbstractAction implements ActiveViewerListener {
        public ToggleLayersAction(final boolean selected) {
            super("Layers", "Show/Hide layers", KeyEvent.VK_L);

            setSelected(selected);
            if (selected)
                setDescription("Hide layers");
            else
                setDescription("Show layers");

            Icy.getMainInterface().addActiveViewerListener(new WeakActiveViewerListener(this));
        }

        public ToggleLayersAction() {
            this(false);
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null) {
                final boolean visible = !canvas.isLayersVisible();

                canvas.setLayersVisible(visible);

                if (visible)
                    setDescription("Hide layers");
                else
                    setDescription("Show layers");

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }

        @Override
        public void viewerActivated(final Viewer viewer) {
            // notify enabled change
            enabledChanged();
        }

        @Override
        public void viewerDeactivated(final Viewer viewer) {
        }

        @Override
        public void activeViewerChanged(final ViewerEvent event) {
        }
    }

    public static final class GlobalToggleLayersAction extends IcyAbstractAction implements ActiveViewerListener {
        public GlobalToggleLayersAction(final boolean selected) {
            super(
                    "LAyers (global)",
                    //new IcyIcon(ResourceUtil.ICON_LAYER_H2),
                    "Show/Hide layers (global)",
                    KeyEvent.VK_L,
                    //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
                    InputEvent.SHIFT_DOWN_MASK
            );

            setSelected(selected);
            if (selected)
                setDescription("Hide layers (global)");
            else
                setDescription("Show layers (global)");

            Icy.getMainInterface().addActiveViewerListener(new WeakActiveViewerListener(this));
        }

        public GlobalToggleLayersAction() {
            this(false);
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            Boolean change = null;

            for (final Viewer viewer : Icy.getMainInterface().getViewers()) {
                if (viewer != null) {
                    final IcyCanvas canvas = viewer.getCanvas();

                    if (canvas != null) {
                        if (change == null)
                            change = Boolean.valueOf(!canvas.isLayersVisible());

                        canvas.setLayersVisible(change.booleanValue());

                        if (change.booleanValue())
                            setDescription("Hide layers (global)");
                        else
                            setDescription("Show layers (global)");

                    }

                    // refresh layer button state
                    viewer.refreshToolBar();
                }
            }

            return true;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }

        @Override
        public void viewerActivated(final Viewer viewer) {
            // notify enabled change
            enabledChanged();
        }

        @Override
        public void viewerDeactivated(final Viewer viewer) {
        }

        @Override
        public void activeViewerChanged(final ViewerEvent event) {
        }
    }

    public static final IcyAbstractAction screenShotAction = new IcyAbstractAction(
            "Screeshot (view)",
            "Take a screenshot of current view",
            true,
            "Rendering..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            // so it won't change during process
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;
            final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;

            if ((sequence != null) && (canvas != null)) {
                try {
                    final Sequence seqOut = canvas.getRenderedSequence(true, progressFrame);

                    if (seqOut != null) {
                        // set sequence name
                        seqOut.setName("Screen shot of '" + sequence.getName() + "' view");
                        // add sequence
                        Icy.getMainInterface().addSequence(seqOut);

                        return true;
                    }
                }
                catch (InterruptedException ex) {
                    // just ignore
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }
    };

    public static final IcyAbstractAction screenShotAlternateAction = new IcyAbstractAction(
            "Screenshot (global)",
            "Take a screenshot of current view with original sequence dimension",
            true,
            "Rendering..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            // so it won't change during process
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;
            final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;

            if ((sequence != null) && (canvas != null)) {
                try {
                    final Sequence seqOut = canvas.getRenderedSequence(false, progressFrame);

                    if (seqOut != null) {
                        // set sequence name
                        seqOut.setName("Rendering of '" + sequence.getName() + "' view");
                        // add sequence
                        Icy.getMainInterface().addSequence(seqOut);

                        return true;
                    }
                }
                catch (InterruptedException ex) {
                    // just ignore
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }
    };

    public static final IcyAbstractAction unselectAction = new IcyAbstractAction(
            "Unselect",
            "Unselect layer(s)",
            KeyEvent.VK_ESCAPE
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final LayersPanel layersPanel = LayersPanel.getInstance();

            if (layersPanel != null) {
                layersPanel.clearSelected();

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public final static IcyAbstractAction deleteLayersAction = new IcyAbstractAction(
            "Delete",
            //new IcyIcon(ResourceUtil.ICON_DELETE),
            "Delete selected layer(s)",
            KeyEvent.VK_DELETE
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();
                final LayersPanel layersPanel = LayersPanel.getInstance();

                if ((sequence != null) && (layersPanel != null)) {
                    final List<Layer> layers = layersPanel.getSelectedLayers();

                    if (layers.size() > 0) {
                        sequence.beginUpdate();
                        try {
                            // delete selected layer
                            for (Layer layer : layers)
                                if (layer.getCanBeRemoved())
                                    // we try first to remove the overlay from Sequence
                                    if (!sequence.removeOverlay(layer.getOverlay()))
                                        // the overlay is just present on the canvas so we remove from it only
                                        viewer.getCanvas().removeLayer(layer);
                        }
                        finally {
                            sequence.endUpdate();
                        }

                        return true;

                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final LayersPanel layersPanel = LayersPanel.getInstance();
            return super.isEnabled() && (sequence != null) && (layersPanel != null)
                    && (layersPanel.getSelectedLayers().size() > 0);
        }
    };

    public static final IcyAbstractAction toggleLayersAction = new ToggleLayersAction();
    public static final IcyAbstractAction globalToggleLayersAction = new GlobalToggleLayersAction();

    public static final IcyAbstractAction globalDisableSyncAction = new IcyAbstractAction(
            "Disabled (all)",
            //new IcyIcon(ResourceUtil.ICON_LOCK_OPEN),
            "Synchronization disabled on all viewers",
            KeyEvent.VK_0,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().setGlobalViewSyncId(0);

            return true;
        }
    };

    public static final IcyAbstractAction globalSyncGroup1Action = new IcyAbstractAction(
            "Group 1 (all)",
            //new IcyIcon(ResourceUtil.getLockedImage(1)),
            "All viewers set to full synchronization group 1 (view and Z/T position)",
            KeyEvent.VK_1,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().setGlobalViewSyncId(1);

            return true;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction globalSyncGroup2Action = new IcyAbstractAction(
            "Group 2 (all)",
            //new IcyIcon(ResourceUtil.getLockedImage(2)),
            "All viewers set to full synchronization group 2 (view and Z/T position)",
            KeyEvent.VK_2,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().setGlobalViewSyncId(2);

            return true;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction globalSyncGroup3Action = new IcyAbstractAction(
            "Group 3 (all)",
            //new IcyIcon(ResourceUtil.getLockedImage(3)),
            "All viewers set to view synchronization group (view synched but not Z/T position)",
            KeyEvent.VK_3,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().setGlobalViewSyncId(3);

            return true;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction globalSyncGroup4Action = new IcyAbstractAction(
            "Group 4 (all)",
            //new IcyIcon(ResourceUtil.getLockedImage(4)),
            "All viewers set to navigation synchronization group (Z/T position synched but not view)",
            KeyEvent.VK_4,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().setGlobalViewSyncId(4);

            return true;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction disableSyncAction = new IcyAbstractAction(
            "disabled",
            //new IcyIcon(ResourceUtil.ICON_LOCK_OPEN),
            "Synchronization disabled (global)",
            KeyEvent.VK_0
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null) {
                canvas.setSyncId(0);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction syncGroup1Action = new IcyAbstractAction(
            "Group 1",
            //new IcyIcon(ResourceUtil.getLockedImage(1)),
            "Full synchronization group 1 (view and Z/T position)",
            KeyEvent.VK_1
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null) {
                // already set --> remove it
                if (canvas.getSyncId() == 1)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(1);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction syncGroup2Action = new IcyAbstractAction(
            "Group 2",
            //new IcyIcon(ResourceUtil.getLockedImage(2)),
            "Full synchronization group 2 (view and Z/T position)",
            KeyEvent.VK_2
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null) {
                // already set --> remove it
                if (canvas.getSyncId() == 2)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(2);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction syncGroup3Action = new IcyAbstractAction(
            "Group 3",
            //new IcyIcon(ResourceUtil.getLockedImage(3)),
            "View synchronization group (view synched but not Z/T position)",
            KeyEvent.VK_3
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null) {
                // already set --> remove it
                if (canvas.getSyncId() == 3)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(3);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static final IcyAbstractAction syncGroup4Action = new IcyAbstractAction(
            "Group 4",
            //new IcyIcon(ResourceUtil.getLockedImage(4)),
            "Navigation synchronization group (Z/T position synched but not view)",
            KeyEvent.VK_4
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null) {
                // already set --> remove it
                if (canvas.getSyncId() == 4)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(4);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : CanvasActions.class.getFields()) {
            final Class<?> type = field.getType();

            try {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e) {
                // ignore
            }
        }

        return result;
    }
}
