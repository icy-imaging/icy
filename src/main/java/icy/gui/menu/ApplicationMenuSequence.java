/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.gui.menu;

import icy.action.SequenceOperationActions;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuSequence extends AbstractApplicationMenu {

    private static ApplicationMenuSequence instance = null;

    public static synchronized ApplicationMenuSequence getInstance() {
        if (instance == null)
            instance = new ApplicationMenuSequence();

        return instance;
    }

    private final IcyMenu menuConversion;
    private final IcyMenu menuRawConversion;
    private final IcyMenu menuExtractChannel;
    private final IcyMenu menuRemoveChannel;

    private final JRadioButtonMenuItem itemConversionUnsigned8bits;
    private final JRadioButtonMenuItem itemConversionSigned8bits;
    private final JRadioButtonMenuItem itemConversionUnsigned16bits;
    private final JRadioButtonMenuItem itemConversionSigned16bits;
    private final JRadioButtonMenuItem itemConversionUnsigned32bits;
    private final JRadioButtonMenuItem itemConversionSigned32bits;
    private final JRadioButtonMenuItem itemConversionFloat32bits;
    private final JRadioButtonMenuItem itemConversionDouble64bits;
    private final JRadioButtonMenuItem itemRawConversionUnsigned8bits;
    private final JRadioButtonMenuItem itemRawConversionSigned8bits;
    private final JRadioButtonMenuItem itemRawConversionUnsigned16bits;
    private final JRadioButtonMenuItem itemRawConversionSigned16bits;
    private final JRadioButtonMenuItem itemRawConversionUnsigned32bits;
    private final JRadioButtonMenuItem itemRawConversionSigned32bits;
    private final JRadioButtonMenuItem itemRawConversionFloat32bits;
    private final JRadioButtonMenuItem itemRawConversionDouble64bits;
    private final IcyMenuItem itemExtractAllChannels;
    private final IcyMenuItem itemMergeChannels;
    private final IcyMenuItem itemReverseSlices;
    private final IcyMenuItem itemExtractSlice;
    private final IcyMenuItem itemRemoveSlice;
    private final IcyMenuItem itemMergeSlices;
    private final IcyMenuItem itemRemoveSlices;
    private final IcyMenuItem itemReverseFrames;
    private final IcyMenuItem itemExtractFrame;
    private final IcyMenuItem itemRemoveFrame;
    private final IcyMenuItem itemMergeFrames;
    private final IcyMenuItem itemRemoveFrames;
    private final IcyMenuItem itemConvertToSlices;
    private final IcyMenuItem itemConvertToFrames;

    private ApplicationMenuSequence() {
        super("Sequence");

        final IcyMenuItem itemDuplicate = new IcyMenuItem("Duplicate Sequence", GoogleMaterialDesignIcons.PHOTO_LIBRARY);
        itemDuplicate.addActionListener(SequenceOperationActions.cloneSequenceAction);
        add(itemDuplicate);

        // TODO: 19/01/2023 Refactor conversion and raw conversion submenu
        /* submenu conversion */
        menuConversion = new IcyMenu("Convert to", GoogleMaterialDesignIcons.LABEL);
        add(menuConversion);

        final ButtonGroup groupConversion = new ButtonGroup();

        itemConversionUnsigned8bits = new JRadioButtonMenuItem("Unsigned Byte (8 bits)");
        itemConversionUnsigned8bits.addActionListener(SequenceOperationActions.convertUByteScaledSequenceAction);
        groupConversion.add(itemConversionUnsigned8bits);
        menuConversion.add(itemConversionUnsigned8bits);
        itemConversionSigned8bits = new JRadioButtonMenuItem("Signed Byte (8 bits)");
        itemConversionSigned8bits.addActionListener(SequenceOperationActions.convertByteScaledSequenceAction);
        groupConversion.add(itemConversionSigned8bits);
        menuConversion.add(itemConversionSigned8bits);
        itemConversionUnsigned16bits = new JRadioButtonMenuItem("Unsigned Short (16 bits)");
        itemConversionUnsigned16bits.addActionListener(SequenceOperationActions.convertUShortScaledSequenceAction);
        groupConversion.add(itemConversionUnsigned16bits);
        menuConversion.add(itemConversionUnsigned16bits);
        itemConversionSigned16bits = new JRadioButtonMenuItem("Signed Short (16 bits)");
        itemConversionSigned16bits.addActionListener(SequenceOperationActions.convertShortScaledSequenceAction);
        groupConversion.add(itemConversionSigned16bits);
        menuConversion.add(itemConversionSigned16bits);
        itemConversionUnsigned32bits = new JRadioButtonMenuItem("Unsigned Int (32 bits)");
        itemConversionUnsigned32bits.addActionListener(SequenceOperationActions.convertUIntScaledSequenceAction);
        groupConversion.add(itemConversionUnsigned32bits);
        menuConversion.add(itemConversionUnsigned32bits);
        itemConversionSigned32bits = new JRadioButtonMenuItem("Signed Int (32 bits)");
        itemConversionSigned32bits.addActionListener(SequenceOperationActions.convertIntScaledSequenceAction);
        groupConversion.add(itemConversionSigned32bits);
        menuConversion.add(itemConversionSigned32bits);
        itemConversionFloat32bits = new JRadioButtonMenuItem("Float (32 bits)");
        itemConversionFloat32bits.addActionListener(SequenceOperationActions.convertFloatScaledSequenceAction);
        groupConversion.add(itemConversionFloat32bits);
        menuConversion.add(itemConversionFloat32bits);
        itemConversionDouble64bits = new JRadioButtonMenuItem("Double (64 bits)");
        itemConversionDouble64bits.addActionListener(SequenceOperationActions.convertDoubleScaledSequenceAction);
        groupConversion.add(itemConversionDouble64bits);
        menuConversion.add(itemConversionDouble64bits);

        /* submenu raw conversion */
        menuRawConversion = new IcyMenu("Convert to Raw", GoogleMaterialDesignIcons.LABEL_OUTLINE);
        add(menuRawConversion);

        final ButtonGroup groupRawConversion = new ButtonGroup();

        itemRawConversionUnsigned8bits = new JRadioButtonMenuItem("Unsigned Byte (8 bits)");
        itemRawConversionUnsigned8bits.addActionListener(SequenceOperationActions.convertUByteSequenceAction);
        groupRawConversion.add(itemRawConversionUnsigned8bits);
        menuRawConversion.add(itemRawConversionUnsigned8bits);
        itemRawConversionSigned8bits = new JRadioButtonMenuItem("Signed Byte (8 bits)");
        itemRawConversionSigned8bits.addActionListener(SequenceOperationActions.convertByteSequenceAction);
        groupRawConversion.add(itemRawConversionSigned8bits);
        menuRawConversion.add(itemRawConversionSigned8bits);
        itemRawConversionUnsigned16bits = new JRadioButtonMenuItem("Unsigned Short (16 bits)");
        itemRawConversionUnsigned16bits.addActionListener(SequenceOperationActions.convertUShortSequenceAction);
        groupRawConversion.add(itemRawConversionUnsigned16bits);
        menuRawConversion.add(itemRawConversionUnsigned16bits);
        itemRawConversionSigned16bits = new JRadioButtonMenuItem("Signed Short (16 bits)");
        itemRawConversionSigned16bits.addActionListener(SequenceOperationActions.convertUShortSequenceAction);
        groupRawConversion.add(itemRawConversionSigned16bits);
        menuRawConversion.add(itemRawConversionSigned16bits);
        itemRawConversionUnsigned32bits = new JRadioButtonMenuItem("Unsigned Int (32 bits)");
        itemRawConversionUnsigned32bits.addActionListener(SequenceOperationActions.convertUIntSequenceAction);
        groupRawConversion.add(itemRawConversionUnsigned32bits);
        menuRawConversion.add(itemRawConversionUnsigned32bits);
        itemRawConversionSigned32bits = new JRadioButtonMenuItem("Signed Int (32 bits)");
        itemRawConversionSigned32bits.addActionListener(SequenceOperationActions.convertIntSequenceAction);
        groupRawConversion.add(itemRawConversionSigned32bits);
        menuRawConversion.add(itemRawConversionSigned32bits);
        itemRawConversionFloat32bits = new JRadioButtonMenuItem("Float (32 bits)");
        itemRawConversionFloat32bits.addActionListener(SequenceOperationActions.convertFloatSequenceAction);
        groupRawConversion.add(itemRawConversionFloat32bits);
        menuRawConversion.add(itemRawConversionFloat32bits);
        itemRawConversionDouble64bits = new JRadioButtonMenuItem("Double (64 bits)");
        itemRawConversionDouble64bits.addActionListener(SequenceOperationActions.convertDoubleSequenceAction);
        groupRawConversion.add(itemRawConversionDouble64bits);
        menuRawConversion.add(itemRawConversionDouble64bits);

        addSeparator();

        // TODO: 19/01/2023 Check if there is ROI inside active sequence
        final IcyMenuItem itemFastCrop = new IcyMenuItem("Fast Crop ROI", GoogleMaterialDesignIcons.CROP);
        itemFastCrop.addActionListener(SequenceOperationActions.cropSequenceAction);
        add(itemFastCrop);

        final IcyMenuItem itemResizeCanvas = new IcyMenuItem("Resize Canvas...", GoogleMaterialDesignIcons.IMAGE_ASPECT_RATIO);
        itemResizeCanvas.addActionListener(SequenceOperationActions.canvasResizeAction);
        add(itemResizeCanvas);

        final IcyMenuItem itemResizeImage = new IcyMenuItem("Resize Image...", GoogleMaterialDesignIcons.ASPECT_RATIO);
        itemResizeImage.addActionListener(SequenceOperationActions.imageResizeAction);
        add(itemResizeImage);

        addSeparator();

        // TODO: 19/01/2023 Make extract and remove channel actions auto reload
        menuExtractChannel = new IcyMenu("Extract Channel...");
        add(menuExtractChannel);

        itemExtractAllChannels = new IcyMenuItem("All");
        itemExtractAllChannels.addActionListener(SequenceOperationActions.extractAllChannelAction);
        menuExtractChannel.add(itemExtractAllChannels);

        menuExtractChannel.addSeparator();

        menuRemoveChannel = new IcyMenu("Remove Channel");
        add(menuRemoveChannel);

        itemMergeChannels = new IcyMenuItem("Merge Channels...");
        itemMergeChannels.addActionListener(SequenceOperationActions.mergeChannelsAction);
        add(itemMergeChannels);

        addSeparator();

        itemReverseSlices = new IcyMenuItem("Reverse Z Slices");
        itemReverseSlices.addActionListener(SequenceOperationActions.reverseSlicesAction);
        add(itemReverseSlices);

        itemExtractSlice = new IcyMenuItem("Extract Selected Z Slice");
        itemExtractSlice.addActionListener(SequenceOperationActions.extractSliceAction);
        add(itemExtractSlice);

        itemRemoveSlice = new IcyMenuItem("Remove Selected Z Slice");
        itemRemoveSlice.addActionListener(SequenceOperationActions.removeSliceAction);
        add(itemRemoveSlice);

        final IcyMenuItem itemAddSlices = new IcyMenuItem("Add Z Slices...");
        itemAddSlices.addActionListener(SequenceOperationActions.addSlicesAction);
        add(itemAddSlices);

        itemMergeSlices = new IcyMenuItem("Merge Z Slices...");
        itemMergeSlices.addActionListener(SequenceOperationActions.mergeSlicesAction);
        add(itemMergeSlices);

        itemRemoveSlices = new IcyMenuItem("Remove Multiple Z Slices...");
        itemRemoveSlices.addActionListener(SequenceOperationActions.removeSlicesAction);
        add(itemRemoveSlices);

        addSeparator();

        itemReverseFrames = new IcyMenuItem("Reverse T Frames");
        itemReverseFrames.addActionListener(SequenceOperationActions.reverseFramesAction);
        add(itemReverseFrames);

        itemExtractFrame = new IcyMenuItem("Extract Selected T Frame");
        itemExtractFrame.addActionListener(SequenceOperationActions.extractFrameAction);
        add(itemExtractFrame);

        itemRemoveFrame = new IcyMenuItem("Remove Selected T Frame");
        itemRemoveFrame.addActionListener(SequenceOperationActions.removeFrameAction);
        add(itemRemoveFrame);

        final IcyMenuItem itemAddFrames = new IcyMenuItem("Add T Frames...");
        itemAddFrames.addActionListener(SequenceOperationActions.addFramesAction);
        add(itemAddFrames);

        itemMergeFrames = new IcyMenuItem("Merge T Frames...");
        itemMergeFrames.addActionListener(SequenceOperationActions.mergeFramesAction);
        add(itemMergeFrames);

        itemRemoveFrames = new IcyMenuItem("Remove Multiple T Frames...");
        itemRemoveFrames.addActionListener(SequenceOperationActions.removeFramesAction);
        add(itemRemoveFrames);

        addSeparator();

        itemConvertToSlices = new IcyMenuItem("Convert T to Z");
        itemConvertToSlices.addActionListener(SequenceOperationActions.convertToSlicesAction);
        add(itemConvertToSlices);

        itemConvertToFrames = new IcyMenuItem("Convert Z to T");
        itemConvertToFrames.addActionListener(SequenceOperationActions.convertToFramesAction);
        add(itemConvertToFrames);

        final IcyMenuItem itemConvertAdvancedZT = new IcyMenuItem("Advanced Z-T Convertion...");
        itemConvertAdvancedZT.addActionListener(SequenceOperationActions.advancedZTConvertAction);
        add(itemConvertAdvancedZT);

        reloadSequenceMenu();

        addActiveSequenceListener();
    }

    private void reloadSequenceMenu() {
        ThreadUtil.invokeLater(() -> {
            final Sequence active = Icy.getMainInterface().getActiveSequence();
            for (Component c : getMenuComponents())
                c.setEnabled(active != null);

            if (active != null) {
                // Unselect scaled datatype
                for (final Component c : menuConversion.getMenuComponents())
                    if (c instanceof JRadioButtonMenuItem)
                        ((JRadioButtonMenuItem) c).setSelected(false);

                // Unselect raw datatype
                for (final Component c : menuRawConversion.getMenuComponents()) {
                    if (c instanceof JRadioButtonMenuItem)
                        ((JRadioButtonMenuItem) c).setSelected(false);
                }

                final int bitSize = active.getDataType_().getBitSize();
                final boolean isFloat = active.getDataType_().isFloat();
                final boolean isSigned = active.getDataType_().isSigned();

                // Select correct datatype in menu
                switch (bitSize) {
                    case 8:
                        if (isSigned) {
                            itemConversionSigned8bits.setSelected(true);
                            itemRawConversionSigned8bits.setSelected(true);
                        } else {
                            itemConversionUnsigned8bits.setSelected(true);
                            itemRawConversionUnsigned8bits.setSelected(true);
                        }
                        break;
                    case 16:
                        if (isSigned) {
                            itemConversionSigned16bits.setSelected(true);
                            itemRawConversionSigned16bits.setSelected(true);
                        } else {
                            itemConversionUnsigned16bits.setSelected(true);
                            itemRawConversionUnsigned16bits.setSelected(true);
                        }
                        break;
                    case 32:
                        if (isFloat) {
                            itemConversionFloat32bits.setSelected(true);
                            itemRawConversionFloat32bits.setSelected(true);
                            break;
                        }
                        if (isSigned) {
                            itemConversionSigned32bits.setSelected(true);
                            itemRawConversionSigned32bits.setSelected(true);
                        } else {
                            itemConversionUnsigned32bits.setSelected(true);
                            itemRawConversionUnsigned32bits.setSelected(true);
                        }
                        break;
                    case 64:
                        itemConversionDouble64bits.setSelected(true);
                        itemRawConversionDouble64bits.setSelected(true);
                        break;
                }

                // Channels (C) menu
                final int sizeC = active.getSizeC();
                menuExtractChannel.removeAll();
                menuRemoveChannel.removeAll();

                if (sizeC > 0) {
                    menuExtractChannel.add(itemExtractAllChannels);
                    menuExtractChannel.addSeparator();

                    for (int c = 0; c < sizeC; c ++) {
                        final IcyMenuItem itemExtractChannel = new IcyMenuItem(active.getChannelName(c));
                        itemExtractChannel.addActionListener(SequenceOperationActions.extractChannelActions[c]);
                        menuExtractChannel.add(itemExtractChannel);

                        final IcyMenuItem itemRemoveChannel = new IcyMenuItem(active.getChannelName(c));
                        itemRemoveChannel.addActionListener(SequenceOperationActions.removeChannelActions[c]);
                        menuRemoveChannel.add(itemRemoveChannel);
                    }

                    menuRemoveChannel.setEnabled(sizeC > 1);
                    itemMergeChannels.setEnabled(sizeC > 1);
                }
                else {
                    menuExtractChannel.setEnabled(false);
                    menuRemoveChannel.setEnabled(false);
                }

                // Slices (Z) menu
                final int sizeZ = active.getSizeZ();

                if (sizeZ < 2) {
                    itemReverseSlices.setEnabled(false);
                    itemExtractSlice.setEnabled(false);
                    itemRemoveSlice.setEnabled(false);
                    itemMergeSlices.setEnabled(false);
                    itemRemoveSlices.setEnabled(false);
                }

                // Frames (T) menu
                final int sizeT = active.getSizeT();

                if (sizeT < 2) {
                    itemReverseFrames.setEnabled(false);
                    itemExtractFrame.setEnabled(false);
                    itemRemoveFrame.setEnabled(false);
                    itemMergeFrames.setEnabled(false);
                    itemRemoveFrames.setEnabled(false);
                }

                if (sizeT < 2)
                    itemConvertToSlices.setEnabled(false);
                if (sizeZ < 2)
                    itemConvertToFrames.setEnabled(false);
            }
        });
    }

    @Override
    public void sequenceActivated(final Sequence sequence) {
        reloadSequenceMenu();
    }

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        reloadSequenceMenu();
    }
}
