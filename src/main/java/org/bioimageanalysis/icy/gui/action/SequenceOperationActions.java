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

import org.bioimageanalysis.icy.gui.dialog.IdConfirmDialog;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.gui.frame.progress.FailedAnnounceFrame;
import org.bioimageanalysis.icy.gui.sequence.tools.*;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.DimensionId;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.gui.undo.IcyUndoManager;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Actions for "Sequence Operation" tab.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class SequenceOperationActions {
    static final class SequenceConvertAction extends IcyAbstractAction {
        final DataType dataType;
        final boolean scaled;

        public SequenceConvertAction(final @NotNull DataType dataType, final boolean scaled) {
            super(
                    dataType.toString(true),
                    "Convert to " + dataType.toString(true),
                    "Convert sequence data type to " + dataType.toString(true),
                    true,
                    "Converting sequence to " + dataType.toString(false) + " ..."
            );

            this.dataType = dataType;
            this.scaled = scaled;
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null) {
                    try {
                        final Sequence out = SequenceUtil.convertToType(Icy.getMainInterface().getActiveSequence(), dataType, scaled);

                        ThreadUtil.invokeLater(() -> {
                            // get output viewer
                            final Viewer vout = new Viewer(out);
                            // restore colormap from input viewer
                            vout.getLut().setColorMaps(viewer.getLut(), false);
                        });
                    }
                    catch (final InterruptedException e1) {
                        // ignore
                    }

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

        /**
         * Returns the selected state (for toggle button type).
         */
        @Override
        public boolean isSelected() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            if (seq == null)
                return super.isSelected();

            return (seq.getDataType().equals(dataType));
        }
    }

    static final class SequenceColorAction extends IcyAbstractAction {
        /*private static final Image[] images = {
                null,
                ResourceUtil.ICON_RGB_COLOR,
                ResourceUtil.ICON_ARGB_COLOR,
                null, null, null, null, null, null, null,
                ResourceUtil.ICON_GRAY_COLOR,
                null, null, null, null, null
        };*/
        private static final String[] names = {
                null,
                "RGB image",
                "ARGB image",
                null, null, null, null, null, null, null,
                "Gray image",
                null, null, null, null, null
        };
        private static final String[] titles = {
                null,
                "Build RGB image",
                "Build ARGB image",
                null, null, null, null, null, null, null,
                "Build gray image",
                null, null, null, null, null
        };
        private static final String[] tooltips = {
                null,
                "Create a RGB color rendered version of the current sequence.\nResulting sequence is 3 channels with unsigned byte (8 bits) data type.",
                "Create an ARGB color (support transparency) rendered version of the current sequence.\nResulting sequence is 4 channels with unsigned byte (8 bits) data type.",
                null, null, null, null, null, null, null,
                "Create a gray rendered version of the current sequence.\nResulting sequence is single channel with unsigned byte (8 bits) data type.",
                null, null, null, null, null
        };
        private static final String[] processMessages = {
                null,
                "Converting to RGB image...",
                "Converting to ARGB image...",
                null, null, null, null, null, null, null,
                "Converting to gray image...",
                null, null, null, null, null
        };

        final int imageType;

        public SequenceColorAction(final int imageType) {
            super(
                    names[imageType],
                    //new IcyIcon(images[imageType], false),
                    titles[imageType],
                    tooltips[imageType],
                    true,
                    processMessages[imageType]
            );

            this.imageType = imageType;
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null) {
                    try {
                        // convert the sequence
                        final Sequence out = SequenceUtil.convertColor(sequence, imageType, viewer.getLut());
                        Icy.getMainInterface().addSequence(out);
                    }
                    catch (final InterruptedException e1) {
                        // ignore
                    }

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    }

    public static final class ExtractChannelAction extends IcyAbstractAction {
        final int channel;

        public ExtractChannelAction(final int channel) {
            super(
                    (channel == -1) ? "All Channels" : "Channel " + channel,
                    (channel == -1) ? "Extract all channels" : "Extract channel " + channel,
                    (channel == -1) ? "Separate all channels of active sequence" : "Create a new single channel sequence from channel " + channel + " of active sequence",
                    true,
                    (channel == -1) ? "Extracting channel(s)..." : "Extracting channel " + channel + "..."
            );

            this.channel = channel;
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                try {
                    if (channel == -1) {
                        for (int c = 0; c < sequence.getSizeC(); c++)
                            Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(sequence, c));
                    }
                    else
                        Icy.getMainInterface().addSequence(SequenceUtil.extractChannel(sequence, channel));
                }
                catch (final InterruptedException e1) {
                    // ignore
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (channel < seq.getSizeC());
        }
    }

    public static final class RemoveChannelAction extends IcyAbstractAction {
        final int channel;

        public RemoveChannelAction(final int channel) {
            super(
                    "channel " + channel,
                    "Remove channel " + channel,
                    "Remove channel " + channel + " from active sequence",
                    true,
                    "Removing channel " + channel + "..."
            );

            this.channel = channel;
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                try {
                    // create undo point
                    final boolean canUndo = sequence.createUndoPoint("Channel " + channel + "removed");

                    // cannot backup
                    if (!canUndo) {
                        // ask confirmation to continue
                        if (!IdConfirmDialog.confirm(
                                "Not enough memory to undo the operation, do you want to continue ?",
                                "ChannelRemoveNoUndoConfirm"))
                            return false;
                    }

                    SequenceUtil.removeChannel(sequence, channel);

                    // no undo, clear undo manager after modification
                    if (!canUndo)
                        sequence.clearUndoManager();
                }
                catch (final InterruptedException e1) {
                    // ignore
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (channel < seq.getSizeC());
        }
    }

    public static final class MergeDimensionAction extends IcyAbstractAction {
        private static final String[] titles = {
                null, null, null,
                "Merge Channels...",
                "Merge Z Slices...",
                "Merge T Frames..."
        };
        private static final String[] tooltips = {
                null, null, null,
                "Merge channels from severals input sequences to build a new sequence.",
                "Merge Z slices from severals input sequences to build a new sequence.",
                "Merge T frames from severals input sequences to build a new sequence."
        };

        final DimensionId dim;

        public MergeDimensionAction(final @NotNull DimensionId dim) {
            super(
                    titles[dim.ordinal()],
                    titles[dim.ordinal()],
                    tooltips[dim.ordinal()]
            );

            this.dim = dim;
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            new SequenceDimensionMergeFrame(dim);
            return true;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (seq.getSize(dim) > 1);
        }
    }

    public static final class ToggleVirtualSequenceAction extends IcyAbstractAction {
        public ToggleVirtualSequenceAction(final boolean selected) {
            super(
                    "Virtual",
                    "Enable/Disable virtual mode (data streaming) for this sequence"
            );

            setSelected(selected);
            setBgProcess(true);
        }

        public ToggleVirtualSequenceAction() {
            this(false);
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            final JToggleButton btn = (e.getSource() instanceof JToggleButton) ? (JToggleButton) e.getSource() : null;
            final boolean value = (btn != null) ? btn.isSelected() : isSelected();

            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            boolean result;
            String errMess = "";

            try {
                // apply on sequence
                if (sequence != null)
                    sequence.setVirtual(value);

                result = true;
            }
            catch (final OutOfMemoryError error) {
                errMess = "Not enough available memory to put back the sequence in memory (still in virtual state).";
                result = false;
            }
            catch (final UnsupportedOperationException error) {
                errMess = "Image cache engine is disable.";
                result = false;
            }
            catch (final Throwable error) {
                errMess = error.getMessage();
                result = false;
            }

            // restore previous state if operation failed
            if (!result) {
                // display error
                if (!value)
                    new FailedAnnounceFrame(errMess);

                // revert to original state
                if (btn != null)
                    btn.setSelected(!value);
                setSelected(!value);
            }

            if (btn != null) {
                if (btn.isSelected())
                    btn.setToolTipText("Disable virtual sequence (caching)");
                else
                    btn.setToolTipText("Enable virtual sequence (caching)");
            }

            return result;
        }

        @Override
        public void setSelected(final boolean value) {
            super.setSelected(value);

            if (Icy.isCacheDisabled())
                setDescription("Image cache is disabled, cannot use virtual sequence");
            else if (value)
                setDescription("Disable virtual sequence (caching)");
            else
                setDescription("Enable virtual sequence (caching)");
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && !Icy.isCacheDisabled();
        }
    }

    public static final @NotNull IcyAbstractAction cloneSequenceAction = new IcyAbstractAction(
            "Duplicate Sequence",
            "Duplicate sequence",
            "Create a fresh copy of the sequence",
            true,
            "Duplicating sequence..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            if (viewer == null)
                return false;

            final Sequence seq = viewer.getSequence();
            if (seq == null)
                return false;

            try {
                // create output sequence
                final Sequence out = SequenceUtil.getCopy(seq);

                ThreadUtil.invokeLater(() -> {
                    // get output viewer
                    final Viewer vout = new Viewer(out);
                    // copy colormap from input viewer
                    vout.getLut().copyFrom(viewer.getLut());
                });
            }
            catch (final InterruptedException e1) {
                // just ignore...
            }

            return true;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final @NotNull IcyAbstractAction convertUByteScaledSequenceAction = new SequenceConvertAction(DataType.UBYTE, true);
    public static final @NotNull IcyAbstractAction convertUByteSequenceAction = new SequenceConvertAction(DataType.UBYTE, false);
    public static final @NotNull IcyAbstractAction convertByteScaledSequenceAction = new SequenceConvertAction(DataType.BYTE, true);
    public static final @NotNull IcyAbstractAction convertByteSequenceAction = new SequenceConvertAction(DataType.BYTE, false);
    public static final @NotNull IcyAbstractAction convertUShortScaledSequenceAction = new SequenceConvertAction(DataType.USHORT, true);
    public static final @NotNull IcyAbstractAction convertUShortSequenceAction = new SequenceConvertAction(DataType.USHORT, false);
    public static final @NotNull IcyAbstractAction convertShortScaledSequenceAction = new SequenceConvertAction(DataType.SHORT, true);
    public static final @NotNull IcyAbstractAction convertShortSequenceAction = new SequenceConvertAction(DataType.SHORT, false);
    public static final @NotNull IcyAbstractAction convertUIntScaledSequenceAction = new SequenceConvertAction(DataType.UINT, true);
    public static final @NotNull IcyAbstractAction convertUIntSequenceAction = new SequenceConvertAction(DataType.UINT, false);
    public static final @NotNull IcyAbstractAction convertIntScaledSequenceAction = new SequenceConvertAction(DataType.INT, true);
    public static final @NotNull IcyAbstractAction convertIntSequenceAction = new SequenceConvertAction(DataType.INT, false);
    public static final @NotNull IcyAbstractAction convertFloatScaledSequenceAction = new SequenceConvertAction(DataType.FLOAT, true);
    public static final @NotNull IcyAbstractAction convertFloatSequenceAction = new SequenceConvertAction(DataType.FLOAT, false);
    public static final @NotNull IcyAbstractAction convertDoubleScaledSequenceAction = new SequenceConvertAction(DataType.DOUBLE, true);
    public static final @NotNull IcyAbstractAction convertDoubleSequenceAction = new SequenceConvertAction(DataType.DOUBLE, false);

    // color operations
    public static final @NotNull IcyAbstractAction argbSequenceAction = new SequenceColorAction(BufferedImage.TYPE_INT_ARGB);
    public static final @NotNull IcyAbstractAction rgbSequenceAction = new SequenceColorAction(BufferedImage.TYPE_INT_RGB);
    public static final @NotNull IcyAbstractAction graySequenceAction = new SequenceColorAction(BufferedImage.TYPE_BYTE_GRAY);

    // XY plan operations
    public static final @NotNull IcyAbstractAction cropSequenceAction = new IcyAbstractAction(
            "Fast Crop ROI",
            "Fast crop image",
            "Crop an image from a ROI",
            true,
            "Doing image crop..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            if (viewer == null)
                return false;

            final Sequence seq = viewer.getSequence();
            if (seq == null)
                return false;

            List<ROI> rois = seq.getROIs();
            int size = rois.size();

            if (size == 0) {
                MessageDialog.showDialog("There is no ROI in the current sequence.\nYou need a ROI to define the region to crop.", MessageDialog.INFORMATION_MESSAGE);
                return false;
            }
            else if (size > 1) {
                rois = seq.getSelectedROIs();
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

            try {
                // create output sequence
                final Sequence out = SequenceUtil.getSubSequence(seq, roi);

                ThreadUtil.invokeLater(() -> {
                    // get output viewer
                    new Viewer(out);
                });
            }
            catch (final InterruptedException e1) {
                // just ignore...
            }

            return true;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (!seq.getROIs().isEmpty());
        }
    };

    public static final @NotNull IcyAbstractAction canvasResizeAction = new IcyAbstractAction(
            "Resize Canvas...",
            "Canvas resize",
            "Resize the canvas without changing image size."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceCanvasResizeFrame(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final @NotNull IcyAbstractAction imageResizeAction = new IcyAbstractAction(
            "Resize Image...",
            "Image resize",
            "Resize the image."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceResizeFrame(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    // channel operations
    public static final @NotNull IcyAbstractAction extractAllChannelAction = new ExtractChannelAction(-1);
    public static final @NotNull IcyAbstractAction[] extractChannelActions = {
            new ExtractChannelAction(0),
            new ExtractChannelAction(1),
            new ExtractChannelAction(2),
            new ExtractChannelAction(3),
            new ExtractChannelAction(4),
            new ExtractChannelAction(5)
    };
    public static final @NotNull IcyAbstractAction[] removeChannelActions = {
            new RemoveChannelAction(0),
            new RemoveChannelAction(1),
            new RemoveChannelAction(2),
            new RemoveChannelAction(3),
            new RemoveChannelAction(4),
            new RemoveChannelAction(5)
    };
    public static final @NotNull IcyAbstractAction mergeChannelsAction = new MergeDimensionAction(DimensionId.C);

    // Z operations
    public static final @NotNull IcyAbstractAction reverseSlicesAction = new IcyAbstractAction(
            "Reverse Z Slices",
            "Reverse Z slices",
            "Reverse Z slices order",
            true,
            "Reversing slices..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                SequenceUtil.reverseZ(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null) && (seq.getSizeZ() > 0);
        }
    };

    public static final @NotNull IcyAbstractAction extractSliceAction = new IcyAbstractAction(
            "Extract Selected Z Slice",
            "Extract current Z slice",
            "Create a new sequence by extracting current Z slice of active sequence.",
            false,
            "Extracting slice..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null) {
                    final int z = viewer.getPositionZ();

                    if (z != -1) {
                        final Sequence out = SequenceUtil.extractSlice(sequence, z);

                        ThreadUtil.invokeLater(() -> {
                            // get output viewer
                            final Viewer vout = new Viewer(out);
                            // copy colormap from input viewer
                            vout.getLut().copyFrom(viewer.getLut());
                        });

                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int z = (viewer == null) ? -1 : viewer.getPositionZ();

            return super.isEnabled() && (z != -1);
        }
    };

    public static final @NotNull IcyAbstractAction removeSliceAction = new IcyAbstractAction(
            "Remove Selected Z Slice",
            "Remove current Z slice",
            "Remove the current Z slice of active sequence.",
            false,
            "Removing slice..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int z = (viewer == null) ? -1 : viewer.getPositionZ();

            if (z != -1) {
                SequenceUtil.removeZAndShift(viewer.getSequence(), z);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int z = (viewer == null) ? -1 : viewer.getPositionZ();

            return super.isEnabled() && (z != -1);
        }
    };

    public static final @NotNull IcyAbstractAction addSlicesAction = new IcyAbstractAction(
            "Add Z Slices...",
            "Add slice(s)",
            "Extends Z dimension by adding empty or duplicating slices."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceDimensionExtendFrame(Icy.getMainInterface().getActiveSequence(), DimensionId.Z);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static final @NotNull IcyAbstractAction mergeSlicesAction = new MergeDimensionAction(DimensionId.Z);

    public static final @NotNull IcyAbstractAction removeSlicesAction = new IcyAbstractAction(
            "Remove Multiple Z Slices...",
            "Advanced slice remove",
            "Advanced Z slice remove operation."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceDimensionAdjustFrame(sequence, DimensionId.Z);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    // T operations
    public static final @NotNull IcyAbstractAction reverseFramesAction = new IcyAbstractAction(
            "Reverse T Frames",
            "Reverse T frames",
            "Reverse T frames order",
            true,
            "Reversing frames..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                SequenceUtil.reverseT(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final @NotNull IcyAbstractAction extractFrameAction = new IcyAbstractAction(
            "Extract Selected T Frame",
            "Extract current T frame",
            "Create a new sequence by extracting current T frame of active sequence.",
            false,
            "Extracting frame..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();

                if (sequence != null) {
                    final int t = viewer.getPositionT();

                    if (t != -1) {
                        final Sequence out = SequenceUtil.extractFrame(sequence, t);

                        ThreadUtil.invokeLater(() -> {
                            // get output viewer
                            final Viewer vout = new Viewer(out);
                            // copy colormap from input viewer
                            vout.getLut().copyFrom(viewer.getLut());
                        });

                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int t = (viewer == null) ? -1 : viewer.getPositionT();

            return super.isEnabled() && (t != -1);
        }
    };

    public static final @NotNull IcyAbstractAction removeFrameAction = new IcyAbstractAction(
            "Remove Selected T Frame",
            "Remove current T frame",
            "Remove the current T frame of active sequence.",
            false,
            "Removing frame..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int t = (viewer == null) ? -1 : viewer.getPositionT();

            if (t != -1) {
                SequenceUtil.removeTAndShift(viewer.getSequence(), t);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final int t = (viewer == null) ? -1 : viewer.getPositionT();

            return super.isEnabled() && (t != -1);
        }
    };

    public static final @NotNull IcyAbstractAction addFramesAction = new IcyAbstractAction(
            "Add T Frames...",
            "Add frame(s)",
            "Extends T dimension by adding empty or duplicating frames."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceDimensionExtendFrame(Icy.getMainInterface().getActiveSequence(), DimensionId.T);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static final @NotNull IcyAbstractAction mergeFramesAction = new MergeDimensionAction(DimensionId.T);

    public static final @NotNull IcyAbstractAction removeFramesAction = new IcyAbstractAction(
            "Remove Multiple T Frames...",
            "Advanced frame remove",
            "Advanced T frame remove operation."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceDimensionAdjustFrame(sequence, DimensionId.T);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    // ZT conversion
    public static final @NotNull IcyAbstractAction convertToSlicesAction = new IcyAbstractAction(
            "Convert T Frames to Z Slices",
            "Convert to stack",
            "Set all images in Z dimension.",
            true,
            "Converting to stack..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();
                final int t = viewer.getPositionT();

                if (sequence != null) {
                    SequenceUtil.convertToStack(sequence);
                    viewer.setPositionZ(t);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final @NotNull IcyAbstractAction convertToFramesAction = new IcyAbstractAction(
            "Convert Z Slices to T Frames",
            "Convert to time sequence",
            "Set all images in T dimension.",
            true,
            "Converting to time..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence sequence = viewer.getSequence();
                final int z = viewer.getPositionZ();

                if (sequence != null) {
                    SequenceUtil.convertToTime(sequence);
                    viewer.setPositionT(z);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final @NotNull IcyAbstractAction advancedZTConvertAction = new IcyAbstractAction(
            "Advanced Z-T Convertion...",
            "Advanced dimension conversion",
            "Advanced dimension conversion operation."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                new SequenceDimensionConvertFrame(sequence);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static @Nullable IcyAbstractAction getConvertSequenceAction(final @NotNull DataType dataType, final boolean scaled) {
        return switch (dataType) {
            case UBYTE -> {
                if (scaled)
                    yield convertUByteScaledSequenceAction;
                yield convertUByteSequenceAction;
            }
            case BYTE -> {
                if (scaled)
                    yield convertByteScaledSequenceAction;
                yield convertByteSequenceAction;
            }
            case USHORT -> {
                if (scaled)
                    yield convertUShortScaledSequenceAction;
                yield convertUShortSequenceAction;
            }
            case SHORT -> {
                if (scaled)
                    yield convertShortScaledSequenceAction;
                yield convertShortSequenceAction;
            }
            case UINT -> {
                if (scaled)
                    yield convertUIntScaledSequenceAction;
                yield convertUIntSequenceAction;
            }
            case INT -> {
                if (scaled)
                    yield convertIntScaledSequenceAction;
                yield convertIntSequenceAction;
            }
            case FLOAT -> {
                if (scaled)
                    yield convertFloatScaledSequenceAction;
                yield convertFloatSequenceAction;
            }
            case DOUBLE -> {
                if (scaled)
                    yield convertDoubleScaledSequenceAction;
                yield convertDoubleSequenceAction;
            }
            default ->
                // not supported
                    null;
        };
    }

    public static final @NotNull IcyAbstractAction undoAction = new IcyAbstractAction(
            "Undo",
            "Undo last operation (Ctrl+Z)",
            KeyEvent.VK_Z,
            SystemUtil.getMenuCtrlMaskEx()
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            return Icy.getMainInterface().undo();
        }

        @Override
        public boolean isEnabled() {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (super.isEnabled() && (undoManager != null))
                return undoManager.canUndo();

            return false;
        }
    };

    public static final @NotNull IcyAbstractAction redoAction = new IcyAbstractAction(
            "Redo",
            "Redo last operation (Ctrl+Y)",
            KeyEvent.VK_Y,
            SystemUtil.getMenuCtrlMaskEx()
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            return Icy.getMainInterface().redo();
        }

        @Override
        public boolean isEnabled() {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (super.isEnabled() && (undoManager != null))
                return undoManager.canRedo();

            return false;
        }
    };

    public static final @NotNull IcyAbstractAction undoClearAction = new IcyAbstractAction(
            "Clear history",
            "Clear all history (will release some memory)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (undoManager != null) {
                undoManager.discardAllEdits();
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (super.isEnabled() && (undoManager != null))
                return undoManager.canUndo() || undoManager.canRedo();

            return false;
        }
    };

    public static final @NotNull IcyAbstractAction undoClearAllButLastAction = new IcyAbstractAction(
            "Clear all but last",
            "Clear all history but the last operation (can release some memory)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (undoManager != null) {
                undoManager.discardOldEdits(1);
                undoManager.discardFutureEdits();
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final IcyUndoManager undoManager = Icy.getMainInterface().getUndoManager();

            if (super.isEnabled() && (undoManager != null))
                return undoManager.canUndo();

            return false;
        }
    };

    /**
     * Return all actions of this class
     */
    @Deprecated(forRemoval = true)
    public static @NotNull List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : SequenceOperationActions.class.getFields()) {
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

        result.add(cloneSequenceAction);

        result.add(convertUByteScaledSequenceAction);
        result.add(convertUByteSequenceAction);
        result.add(convertByteScaledSequenceAction);
        result.add(convertByteSequenceAction);
        result.add(convertUShortScaledSequenceAction);
        result.add(convertUShortSequenceAction);
        result.add(convertShortScaledSequenceAction);
        result.add(convertShortSequenceAction);
        result.add(convertUIntScaledSequenceAction);
        result.add(convertUIntSequenceAction);
        result.add(convertIntScaledSequenceAction);
        result.add(convertIntSequenceAction);
        result.add(convertFloatScaledSequenceAction);
        result.add(convertFloatSequenceAction);
        result.add(convertDoubleScaledSequenceAction);
        result.add(convertDoubleSequenceAction);

        result.add(argbSequenceAction);
        result.add(rgbSequenceAction);
        result.add(graySequenceAction);

        result.add(cropSequenceAction);
        result.add(canvasResizeAction);
        result.add(imageResizeAction);

        result.add(extractAllChannelAction);
        for (int c = 0; c <= 5; c++) {
            result.add(extractChannelActions[c]);
            result.add(removeChannelActions[c]);
        }

        result.add(mergeChannelsAction);
        result.add(reverseSlicesAction);
        result.add(addSlicesAction);
        result.add(mergeSlicesAction);
        result.add(removeSlicesAction);
        result.add(reverseFramesAction);
        result.add(addFramesAction);
        result.add(mergeFramesAction);
        result.add(removeFramesAction);
        result.add(convertToSlicesAction);
        result.add(convertToFramesAction);
        result.add(advancedZTConvertAction);

        result.add(undoAction);
        result.add(redoAction);
        result.add(undoClearAction);
        result.add(undoClearAllButLastAction);

        return result;
    }

    public static @NotNull List<IcyAbstractAction> getAllGlobalViewerActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        result.add(extractSliceAction);
        result.add(removeSliceAction);
        result.add(extractFrameAction);
        result.add(removeFrameAction);

        return result;
    }
}
