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

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.main.MainFrame;
import icy.gui.sequence.tools.*;
import icy.gui.viewer.Viewer;
import icy.image.cache.ImageCache;
import icy.main.Icy;
import icy.roi.ROI;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.SequenceUtil;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.undo.IcyUndoManager;
import icy.util.ClassUtil;

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
 * @author Stephane
 * @author Thomas MUSSET
 */
public final class SequenceOperationActions {
    static final class SequenceConvertAction extends IcyAbstractAction {
        final DataType dataType;
        final boolean scaled;

        public SequenceConvertAction(final DataType dataType, final boolean scaled) {
            super(
                    dataType.toString(true),
                    //new IcyIcon(ResourceUtil.ICON_BAND_RIGHT),
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
                        final Sequence out = SequenceUtil.convertToType(Icy.getMainInterface().getActiveSequence(),
                                dataType, scaled);

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
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
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
                    (channel == -1) ? "all channels" : "channel " + channel,
                    //new IcyIcon(ResourceUtil.ICON_INDENT_DECREASE),
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
                    //new IcyIcon(ResourceUtil.ICON_INDENT_REMOVE),
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
                "Merge channels",
                "Merge Z slices",
                "Merge T frames"
        };
        private static final String[] tooltips = {
                null, null, null,
                "Merge channels from severals input sequences to build a new sequence.",
                "Merge Z slices from severals input sequences to build a new sequence.",
                "Merge T frames from severals input sequences to build a new sequence."
        };

        final DimensionId dim;

        public MergeDimensionAction(final DimensionId dim) {
            super(
                    "Merge...",
                    //new IcyIcon(ResourceUtil.ICON_INDENT_INCREASE),
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

            if (!ImageCache.isEnabled())
                setDescription("Image cache is disabled, cannot use virtual sequence");
            else if (value)
                setDescription("Disable virtual sequence (caching)");
            else
                setDescription("Enable virtual sequence (caching)");
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && ImageCache.isEnabled();
        }
    }

    public static final IcyAbstractAction cloneSequenceAction = new IcyAbstractAction(
            "Duplicate",
            //new IcyIcon(ResourceUtil.ICON_COPY),
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

    public static final IcyAbstractAction convertUByteScaledSequenceAction = new SequenceConvertAction(DataType.UBYTE, true);
    public static final IcyAbstractAction convertUByteSequenceAction = new SequenceConvertAction(DataType.UBYTE, false);
    public static final IcyAbstractAction convertByteScaledSequenceAction = new SequenceConvertAction(DataType.BYTE, true);
    public static final IcyAbstractAction convertByteSequenceAction = new SequenceConvertAction(DataType.BYTE, false);
    public static final IcyAbstractAction convertUShortScaledSequenceAction = new SequenceConvertAction(DataType.USHORT, true);
    public static final IcyAbstractAction convertUShortSequenceAction = new SequenceConvertAction(DataType.USHORT, false);
    public static final IcyAbstractAction convertShortScaledSequenceAction = new SequenceConvertAction(DataType.SHORT, true);
    public static final IcyAbstractAction convertShortSequenceAction = new SequenceConvertAction(DataType.SHORT, false);
    public static final IcyAbstractAction convertUIntScaledSequenceAction = new SequenceConvertAction(DataType.UINT, true);
    public static final IcyAbstractAction convertUIntSequenceAction = new SequenceConvertAction(DataType.UINT, false);
    public static final IcyAbstractAction convertIntScaledSequenceAction = new SequenceConvertAction(DataType.INT, true);
    public static final IcyAbstractAction convertIntSequenceAction = new SequenceConvertAction(DataType.INT, false);
    public static final IcyAbstractAction convertFloatScaledSequenceAction = new SequenceConvertAction(DataType.FLOAT, true);
    public static final IcyAbstractAction convertFloatSequenceAction = new SequenceConvertAction(DataType.FLOAT, false);
    public static final IcyAbstractAction convertDoubleScaledSequenceAction = new SequenceConvertAction(DataType.DOUBLE, true);
    public static final IcyAbstractAction convertDoubleSequenceAction = new SequenceConvertAction(DataType.DOUBLE, false);

    // color operations
    public static final IcyAbstractAction argbSequenceAction = new SequenceColorAction(BufferedImage.TYPE_INT_ARGB);
    public static final IcyAbstractAction rgbSequenceAction = new SequenceColorAction(BufferedImage.TYPE_INT_RGB);
    public static final IcyAbstractAction graySequenceAction = new SequenceColorAction(BufferedImage.TYPE_BYTE_GRAY);

    // XY plan operations
    public static final IcyAbstractAction cropSequenceAction = new IcyAbstractAction(
            "Fast crop",
            //new IcyIcon(ResourceUtil.ICON_CUT),
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
                MessageDialog.showDialog(
                        "There is no ROI in the current sequence.\nYou need a ROI to define the region to crop.",
                        MessageDialog.INFORMATION_MESSAGE);
                return false;
            }
            else if (size > 1) {
                rois = seq.getSelectedROIs();
                size = rois.size();

                if (size == 0) {
                    MessageDialog.showDialog("You need to select a ROI to do this operation.",
                            MessageDialog.INFORMATION_MESSAGE);
                    return false;
                }
                else if (size > 1) {
                    MessageDialog.showDialog("You must have only one selected ROI to do this operation.",
                            MessageDialog.INFORMATION_MESSAGE);
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
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction canvasResizeAction = new IcyAbstractAction(
            "Canvas size...",
            //new IcyIcon(ResourceUtil.ICON_CROP),
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

    public static final IcyAbstractAction imageResizeAction = new IcyAbstractAction(
            "Image size...",
            //new IcyIcon(ResourceUtil.ICON_FIT_CANVAS),
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
    public static final IcyAbstractAction extractAllChannelAction = new ExtractChannelAction(-1);
    public static final IcyAbstractAction[] extractChannelActions = {
            new ExtractChannelAction(0),
            new ExtractChannelAction(1),
            new ExtractChannelAction(2),
            new ExtractChannelAction(3),
            new ExtractChannelAction(4),
            new ExtractChannelAction(5)
    };
    public static final IcyAbstractAction[] removeChannelActions = {
            new RemoveChannelAction(0),
            new RemoveChannelAction(1),
            new RemoveChannelAction(2),
            new RemoveChannelAction(3),
            new RemoveChannelAction(4),
            new RemoveChannelAction(5)
    };
    public static final IcyAbstractAction mergeChannelsAction = new MergeDimensionAction(DimensionId.C);

    // Z operations
    public static final IcyAbstractAction reverseSlicesAction = new IcyAbstractAction(
            "Reverse order",
            //new IcyIcon(ResourceUtil.ICON_LAYER_REVERSE_V),
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
    };

    public static final IcyAbstractAction extractSliceAction = new IcyAbstractAction(
            "Extract slice",
            //new IcyIcon(ResourceUtil.ICON_LAYER_EXTRACT_V),
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

    public static final IcyAbstractAction removeSliceAction = new IcyAbstractAction(
            "Remove slice",
            //new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_V),
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

    public static final IcyAbstractAction addSlicesAction = new IcyAbstractAction(
            "Add...",
            //new IcyIcon(ResourceUtil.ICON_LAYER_ADD_V),
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

    public static final IcyAbstractAction mergeSlicesAction = new MergeDimensionAction(DimensionId.Z);

    public static final IcyAbstractAction removeSlicesAction = new IcyAbstractAction(
            "Remove...",
            //new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_ADV_V),
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
    public static final IcyAbstractAction reverseFramesAction = new IcyAbstractAction(
            "Reverse order",
            //new IcyIcon(ResourceUtil.ICON_LAYER_REVERSE_H),
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

    public static final IcyAbstractAction extractFrameAction = new IcyAbstractAction(
            "Extract frame",
            //new IcyIcon(ResourceUtil.ICON_LAYER_EXTRACT_H),
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

    public static final IcyAbstractAction removeFrameAction = new IcyAbstractAction(
            "Remove frame",
            //new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_H),
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

    public static final IcyAbstractAction addFramesAction = new IcyAbstractAction(
            "Add...",
            //new IcyIcon(ResourceUtil.ICON_LAYER_ADD_H),
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

    public static final IcyAbstractAction mergeFramesAction = new MergeDimensionAction(DimensionId.T);

    public static final IcyAbstractAction removeFramesAction = new IcyAbstractAction(
            "Remove...",
            //new IcyIcon(ResourceUtil.ICON_LAYER_REMOVE_ADV_H),
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
    public static final IcyAbstractAction convertToSlicesAction = new IcyAbstractAction(
            "Convert to stack",
            //new IcyIcon(ResourceUtil.ICON_LAYER_V1),
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

    public static final IcyAbstractAction convertToFramesAction = new IcyAbstractAction(
            "Convert to time",
            //new IcyIcon(ResourceUtil.ICON_LAYER_H1),
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

    public static final IcyAbstractAction advancedZTConvertAction = new IcyAbstractAction(
            "Advanced...",
            //new IcyIcon(ResourceUtil.ICON_COG),
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

    /**
     * @deprecated Use {@link RoiActions#fillInteriorAction} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static final IcyAbstractAction fillSequenceAction = new IcyAbstractAction(
            "Fill",
            //new IcyIcon(ResourceUtil.ICON_BRUSH),
            "Fill ROI content",
            "Fill content of the selected ROI with specified value",
            true,
            "Fill ROI content"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null) {
                    final double value = 1d;

                    try {
                        for (final ROI roi : sequence.getSelectedROIs())
                            DataIteratorUtil.set(new SequenceDataIterator(sequence, roi, true), value);
                    }
                    catch (final InterruptedException e1) {
                        MessageDialog.showDialog("Operation interrupted", e1.getLocalizedMessage(),
                                MessageDialog.ERROR_MESSAGE);
                    }

                    sequence.dataChanged();

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
    };

    public static IcyAbstractAction getConvertSequenceAction(final DataType dataType, final boolean scaled) {
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

    public static final IcyAbstractAction undoAction = new IcyAbstractAction(
            "Undo",
            //new IcyIcon(ResourceUtil.ICON_UNDO),
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

    public static final IcyAbstractAction redoAction = new IcyAbstractAction(
            "Redo",
            //new IcyIcon(ResourceUtil.ICON_REDO),
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

    public static final IcyAbstractAction undoClearAction = new IcyAbstractAction(
            "Clear history",
            //new IcyIcon(ResourceUtil.ICON_TRASH),
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

    public static final IcyAbstractAction undoClearAllButLastAction = new IcyAbstractAction(
            "Clear all but last",
            //new IcyIcon(ResourceUtil.ICON_CLEAR_BEFORE),
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
    public static List<IcyAbstractAction> getAllActions() {
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
}
