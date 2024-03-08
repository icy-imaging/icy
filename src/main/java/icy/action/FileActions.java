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

import icy.file.Saver;
import icy.gui.dialog.LoaderDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.SaverDialog;
import icy.gui.menu.ApplicationMenuFile;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.util.ClassUtil;
import icy.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File actions (open / save / close...)
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class FileActions {
    public static final @NotNull IcyAbstractAction clearRecentFilesAction = new IcyAbstractAction(
            "Clear Recent Files",
            "Clear recent files",
            "Clear the list of last opened files"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final ApplicationMenuFile appMenu = ApplicationMenuFile.getInstance();
            appMenu.getRecentFileList().clear();
            return true;
        }
    };

    // TODO Replace multiple "new sequence" actions with one action that open a dialog to select the type to create
    public static final @NotNull IcyAbstractAction newSequenceAction = new IcyAbstractAction(
            "Empty Sequence",
            "Create an empty sequence"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().addSequence(
                    new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1, DataType.UBYTE))
            );
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction newGraySequenceAction = new IcyAbstractAction(
            "Grayscale Sequence",
            "Create a new gray sequence",
            "Create a new single channel (gray level) sequence."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            ThreadUtil.bgRun(() -> Icy.getMainInterface().addSequence(
                    new Sequence("Single channel sequence", new IcyBufferedImage(512, 512, 1, DataType.UBYTE)))
            );

            return true;
        }
    };

    public static final @NotNull IcyAbstractAction newRGBSequenceAction = new IcyAbstractAction(
            "RGB Sequence",
            "Create a new RGB color sequence",
            "Create a 3 channels sequence (red, green, blue)."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            ThreadUtil.bgRun(() -> Icy.getMainInterface().addSequence(
                    new Sequence("RGB sequence", new IcyBufferedImage(512, 512, 3, DataType.UBYTE)))
            );

            return true;
        }
    };

    public static final @NotNull IcyAbstractAction newARGBSequenceAction = new IcyAbstractAction(
            "RGBA Sequence",
            "Create a new RGBA color sequence",
            "Create a 4 channels sequence (red, green, blue, alpha).",
            true,
            "Creating RGBA sequence...") {
        @Override
        public boolean doAction(final ActionEvent e) {
            final IcyBufferedImage image = new IcyBufferedImage(512, 512, 4, DataType.UBYTE);
            Arrays.fill(image.getDataXYAsByte(3), (byte) -1);
            Icy.getMainInterface().addSequence(new Sequence("RGBA sequence", image));

            return true;
        }
    };

    public static final @NotNull IcyAbstractAction openSequenceAction = new IcyAbstractAction(
            "Open...",
            "Open a file",
            "Display a file selection dialog and choose the file to open",
            KeyEvent.VK_O,
            SystemUtil.getMenuCtrlMaskEx()
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new LoaderDialog();
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction openSequenceRegionAction = new IcyAbstractAction(
            "Open Region...",
            "Open selected region",
            "Open the selected ROI region from the original image",
            KeyEvent.VK_O,
            SystemUtil.getMenuCtrlMaskEx() | InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                List<ROI> rois = sequence.getROIs();
                int size = rois.size();

                if (size == 0) {
                    MessageDialog.showDialog("There is no ROI in the current sequence.\nYou need a ROI to define the region to open.", MessageDialog.INFORMATION_MESSAGE);
                    return false;
                }
                else if (size > 1) {
                    rois = sequence.getSelectedROIs();
                    size = rois.size();

                    if (size == 0) {
                        MessageDialog.showDialog("You need to select a ROI to do this operation.", MessageDialog.INFORMATION_MESSAGE);
                        return false;
                    }
                    else if (size > 1) {
                        MessageDialog.showDialog("You must have only one selected ROI to do this operation.", MessageDialog.INFORMATION_MESSAGE);
                        return false;
                    }
                }

                final ROI roi = rois.get(0);
                final Rectangle bounds = SequenceUtil.getOriginRectangle(roi.getBounds5D().toRectangle2D().getBounds(), sequence);
                final String path = sequence.getFilename();

                if (!bounds.isEmpty())
                    new LoaderDialog(path, bounds, sequence.getSeries(), true);
                else
                    new LoaderDialog(path, null, sequence.getSeries(), true);
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (!StringUtil.isEmpty(seq.getFilename()) && seq.hasROI());
        }
    };

    public static final @NotNull IcyAbstractAction saveSequenceAction = new IcyAbstractAction(
            "Save",
            "Save active sequence",
            "Save the active sequence with its default filename",
            KeyEvent.VK_S,
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final Sequence seq = viewer.getSequence();

            if (seq != null) {
                final String filename = seq.getOutputFilename(true);

                if (StringUtil.isEmpty(filename))
                    new SaverDialog(seq, true);
                else {
                    // background process
                    ThreadUtil.bgRun(() -> {
                        final File file = new File(filename);

                        Saver.save(seq, file, !file.exists() || file.isDirectory(), true);
                    });
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null);
        }
    };

    public static final @NotNull IcyAbstractAction saveAsSequenceAction = new IcyAbstractAction(
            "Save as...",
            "Save active sequence",
            "Save the active sequence under selected file name",
            KeyEvent.VK_S,
            SystemUtil.getMenuCtrlMaskEx() | InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence seq = viewer.getSequence();

                if (seq != null) {
                    new SaverDialog(seq);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null);
        }
    };

    public static final @NotNull IcyAbstractAction saveMetaDataAction = new IcyAbstractAction(
            "Save Metadata",
            "Save active sequence metadata",
            "Save the metadata of the active sequence now",
            KeyEvent.VK_S,
            InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
            true,
            "Saving metadata..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            // Sequence persistence enabled --> save XML metadata
            if (GeneralPreferences.getSequencePersistence()) {
                final Sequence seq = Icy.getMainInterface().getActiveSequence();
                if (seq != null)
                    return seq.saveXMLData();
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null);
        }
    };

    public static final @NotNull IcyAbstractAction closeCurrentSequenceAction = new IcyAbstractAction(
            "Close Sequence",
            "Close active sequence",
            "Close the current active sequence",
            KeyEvent.VK_Q,
            InputEvent.CTRL_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                viewer.close();
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null);
        }
    };

    public static final @NotNull IcyAbstractAction closeOthersSequencesAction = new IcyAbstractAction(
            "Close Others",
            "Close others sequences",
            "Close all opened sequences except the active one.",
            KeyEvent.VK_Q,
            InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer focusedViewer = Icy.getMainInterface().getActiveViewer();

            for (final Viewer viewer : Icy.getMainInterface().getViewers())
                if (viewer != focusedViewer)
                    viewer.close();

            return true;
        }

        @Override
        public boolean isEnabled() {
            final ArrayList<Viewer> viewers = Icy.getMainInterface().getViewers();

            return super.isEnabled() && (viewers != null) && (viewers.size() > 1);
        }
    };

    public static final @NotNull IcyAbstractAction closeAllSequencesAction = new IcyAbstractAction(
            "Close All Sequences",
            "Close all sequences",
            "Close all opened sequences.",
            KeyEvent.VK_Q,
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.getMainInterface().closeAllViewers();
            return true;
        }

        @Override
        public boolean isEnabled() {
            final ArrayList<Viewer> viewers = Icy.getMainInterface().getViewers();

            return super.isEnabled() && (viewers != null) && (!viewers.isEmpty());
        }
    };

    /**
     * Return all actions of this class
     */
    @Deprecated(forRemoval = true)
    public static @NotNull List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : FileActions.class.getFields()) {
            final Class<?> type = field.getType();

            try {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (final Exception e) {
                // ignore
            }
        }

        return result;
    }

    public static @NotNull List<IcyAbstractAction> getAllActiveSequenceActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        result.add(openSequenceRegionAction);
        result.add(saveSequenceAction);
        result.add(saveAsSequenceAction);
        result.add(saveMetaDataAction);
        result.add(closeCurrentSequenceAction);

        return result;
    }

    public static @NotNull List<IcyAbstractAction> getAllGlobalViewerActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        result.add(closeOthersSequencesAction);
        result.add(closeAllSequencesAction);

        return result;
    }
}
