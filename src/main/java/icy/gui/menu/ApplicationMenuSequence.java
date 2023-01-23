package icy.gui.menu;

import icy.action.SequenceOperationActions;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.*;

public final class ApplicationMenuSequence extends AbstractApplicationMenu {
    private final JMenu menuConversion;
    private final JMenu menuRawConversion;
    private final JMenu menuExtractChannel;
    private final JMenu menuRemoveChannel;

    private final JMenuItem itemDuplicate;
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
    private final JMenuItem itemFastCrop;
    private final JMenuItem itemResizeCanvas;
    private final JMenuItem itemResizeImage;
    private final JMenuItem itemExtractAllChannels;
    private final JMenuItem itemMergeChannels;
    private final JMenuItem itemReverseSlices;
    private final JMenuItem itemExtractSlice;
    private final JMenuItem itemRemoveSlice;
    private final JMenuItem itemAddSlices;
    private final JMenuItem itemMergeSlices;
    private final JMenuItem itemRemoveSlices;
    private final JMenuItem itemReverseFrames;
    private final JMenuItem itemExtractFrame;
    private final JMenuItem itemRemoveFrame;
    private final JMenuItem itemAddFrames;
    private final JMenuItem itemMergeFrames;
    private final JMenuItem itemRemoveFrames;
    private final JMenuItem itemConvertToSlices;
    private final JMenuItem itemConvertToFrames;
    private final JMenuItem itemConvertAdvancedZT;

    public ApplicationMenuSequence() {
        super("Sequence");

        itemDuplicate = new JMenuItem("Duplicate Sequence");
        setIcon(itemDuplicate, GoogleMaterialDesignIcons.PHOTO_LIBRARY);
        itemDuplicate.addActionListener(SequenceOperationActions.cloneSequenceAction);
        add(itemDuplicate);

        // TODO: 19/01/2023 Refactor conversion and raw conversion submenu
        /* submenu conversion */
        menuConversion = new JMenu("Convert to");
        setIcon(menuConversion, GoogleMaterialDesignIcons.LABEL);
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
        menuRawConversion = new JMenu("Convert to Raw");
        setIcon(menuRawConversion, GoogleMaterialDesignIcons.LABEL_OUTLINE);
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
        itemFastCrop = new JMenuItem("Fast Crop ROI");
        setIcon(itemFastCrop, GoogleMaterialDesignIcons.CROP);
        itemFastCrop.addActionListener(SequenceOperationActions.cropSequenceAction);
        add(itemFastCrop);

        itemResizeCanvas = new JMenuItem("Resize Canvas...");
        setIcon(itemResizeCanvas, GoogleMaterialDesignIcons.IMAGE_ASPECT_RATIO);
        itemResizeCanvas.addActionListener(SequenceOperationActions.canvasResizeAction);
        add(itemResizeCanvas);

        itemResizeImage = new JMenuItem("Resize Image...");
        setIcon(itemResizeImage, GoogleMaterialDesignIcons.ASPECT_RATIO);
        itemResizeImage.addActionListener(SequenceOperationActions.imageResizeAction);
        add(itemResizeImage);

        addSeparator();

        // TODO: 19/01/2023 Make extract and remove channel actions auto reload
        menuExtractChannel = new JMenu("Extract Channel...");
        add(menuExtractChannel);

        itemExtractAllChannels = new JMenuItem("All");
        itemExtractAllChannels.addActionListener(SequenceOperationActions.extractAllChannelAction);
        menuExtractChannel.add(itemExtractAllChannels);

        menuExtractChannel.addSeparator();

        menuRemoveChannel = new JMenu("Remove Channel");
        add(menuRemoveChannel);

        itemMergeChannels = new JMenuItem("Merge Channels...");
        itemMergeChannels.addActionListener(SequenceOperationActions.mergeChannelsAction);
        add(itemMergeChannels);

        addSeparator();

        itemReverseSlices = new JMenuItem("Reverse Z Slices");
        itemReverseSlices.addActionListener(SequenceOperationActions.reverseSlicesAction);
        add(itemReverseSlices);

        itemExtractSlice = new JMenuItem("Extract Selected Z Slice");
        itemExtractSlice.addActionListener(SequenceOperationActions.extractSliceAction);
        add(itemExtractSlice);

        itemRemoveSlice = new JMenuItem("Remove Selected Z Slice");
        itemRemoveSlice.addActionListener(SequenceOperationActions.removeSliceAction);
        add(itemRemoveSlice);

        itemAddSlices = new JMenuItem("Add Z Slices...");
        itemAddSlices.addActionListener(SequenceOperationActions.addSlicesAction);
        add(itemAddSlices);

        itemMergeSlices = new JMenuItem("Merge Z Slices...");
        itemMergeSlices.addActionListener(SequenceOperationActions.mergeSlicesAction);
        add(itemMergeSlices);

        itemRemoveSlices = new JMenuItem("Remove Multiple Z Slices...");
        itemRemoveSlices.addActionListener(SequenceOperationActions.removeSlicesAction);
        add(itemRemoveSlices);

        addSeparator();

        itemReverseFrames = new JMenuItem("Reverse T Frames");
        itemReverseFrames.addActionListener(SequenceOperationActions.reverseFramesAction);
        add(itemReverseFrames);

        itemExtractFrame = new JMenuItem("Extract Selected T Frame");
        itemExtractFrame.addActionListener(SequenceOperationActions.extractFrameAction);
        add(itemExtractFrame);

        itemRemoveFrame = new JMenuItem("Remove Selected T Frame");
        itemRemoveFrame.addActionListener(SequenceOperationActions.removeFrameAction);
        add(itemRemoveFrame);

        itemAddFrames = new JMenuItem("Add T Frames...");
        itemAddFrames.addActionListener(SequenceOperationActions.addFramesAction);
        add(itemAddFrames);

        itemMergeFrames = new JMenuItem("Merge T Frames...");
        itemMergeFrames.addActionListener(SequenceOperationActions.mergeFramesAction);
        add(itemMergeFrames);

        itemRemoveFrames = new JMenuItem("Remove Multiple T Frames...");
        itemRemoveFrames.addActionListener(SequenceOperationActions.removeFramesAction);
        add(itemRemoveFrames);

        addSeparator();

        itemConvertToSlices = new JMenuItem("Convert T to Z");
        itemConvertToSlices.addActionListener(SequenceOperationActions.convertToSlicesAction);
        add(itemConvertToSlices);

        itemConvertToFrames = new JMenuItem("Convert Z to T");
        itemConvertToFrames.addActionListener(SequenceOperationActions.convertToFramesAction);
        add(itemConvertToFrames);

        itemConvertAdvancedZT = new JMenuItem("Advanced Z-T Convertion...");
        itemConvertAdvancedZT.addActionListener(SequenceOperationActions.advancedZTConvertAction);
        add(itemConvertAdvancedZT);

        reloadSequenceMenu();

        addSkinChangeListener();
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
                        final JMenuItem itemExtractChannel = new JMenuItem(active.getChannelName(c));
                        itemExtractChannel.addActionListener(SequenceOperationActions.extractChannelActions[c]);
                        menuExtractChannel.add(itemExtractChannel);

                        final JMenuItem itemRemoveChannel = new JMenuItem(active.getChannelName(c));
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
