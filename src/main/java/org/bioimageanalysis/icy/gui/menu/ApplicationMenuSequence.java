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

package org.bioimageanalysis.icy.gui.menu;

import org.bioimageanalysis.icy.gui.action.IcyAbstractAction;
import org.bioimageanalysis.icy.gui.action.SequenceOperationActions;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenu;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;
import org.bioimageanalysis.icy.gui.component.menu.IcyRadioButtonMenuItem;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.type.DataType;

import java.util.Objects;

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

    private final IcyMenuItem itemExtractAllChannels;

    private ApplicationMenuSequence() {
        super("Sequence");

        final IcyMenuItem itemUndo = new IcyMenuItem(SequenceOperationActions.undoAction, SVGIcon.UNDO);
        add(itemUndo);

        final IcyMenuItem itemRedo = new IcyMenuItem(SequenceOperationActions.redoAction, SVGIcon.REDO);
        add(itemRedo);

        addSeparator();

        final IcyMenuItem itemDuplicate = new IcyMenuItem(SequenceOperationActions.cloneSequenceAction, SVGIcon.PHOTO_LIBRARY);
        add(itemDuplicate);

        /* submenu conversion */
        menuConversion = new IcyMenu("Convert to", SVGIcon.SWITCH_ACCESS_2);
        add(menuConversion);

        /* submenu raw conversion */
        menuRawConversion = new IcyMenu("Convert to Raw", SVGIcon.SWITCH_ACCESS_2);
        add(menuRawConversion);

        final DataType[] dataTypes = {
                DataType.UBYTE,
                DataType.BYTE,
                DataType.USHORT,
                DataType.SHORT,
                DataType.UINT,
                DataType.INT,
                DataType.FLOAT,
                DataType.DOUBLE
        };

        for (final DataType dataType : dataTypes) {
            final IcyAbstractAction scaled = Objects.requireNonNull(SequenceOperationActions.getConvertSequenceAction(dataType, true));
            menuConversion.add(new IcyRadioButtonMenuItem(scaled));

            final IcyAbstractAction raw = Objects.requireNonNull(SequenceOperationActions.getConvertSequenceAction(dataType, false));
            menuRawConversion.add(new IcyRadioButtonMenuItem(raw));
        }

        addSeparator();

        final IcyMenuItem itemFastCrop = new IcyMenuItem(SequenceOperationActions.cropSequenceAction, SVGIcon.CROP);
        add(itemFastCrop);

        final IcyMenuItem itemResizeCanvas = new IcyMenuItem(SequenceOperationActions.canvasResizeAction, SVGIcon.IMAGE_ASPECT_RATIO);
        add(itemResizeCanvas);

        final IcyMenuItem itemResizeImage = new IcyMenuItem(SequenceOperationActions.imageResizeAction, SVGIcon.ASPECT_RATIO);
        add(itemResizeImage);

        addSeparator();

        menuExtractChannel = new IcyMenu("Extract Channel...");
        add(menuExtractChannel);

        itemExtractAllChannels = new IcyMenuItem(SequenceOperationActions.extractAllChannelAction);
        menuExtractChannel.add(itemExtractAllChannels);

        menuExtractChannel.addSeparator();

        menuRemoveChannel = new IcyMenu("Remove Channel");
        add(menuRemoveChannel);

        final IcyMenuItem itemMergeChannels = new IcyMenuItem(SequenceOperationActions.mergeChannelsAction);
        add(itemMergeChannels);

        addSeparator();

        final IcyMenuItem itemReverseSlices = new IcyMenuItem(SequenceOperationActions.reverseSlicesAction);
        add(itemReverseSlices);

        final IcyMenuItem itemExtractSlice = new IcyMenuItem(SequenceOperationActions.extractSliceAction);
        add(itemExtractSlice);

        final IcyMenuItem itemRemoveSlice = new IcyMenuItem(SequenceOperationActions.removeSliceAction);
        add(itemRemoveSlice);

        final IcyMenuItem itemAddSlices = new IcyMenuItem(SequenceOperationActions.addSlicesAction);
        add(itemAddSlices);

        final IcyMenuItem itemMergeSlices = new IcyMenuItem(SequenceOperationActions.mergeSlicesAction);
        add(itemMergeSlices);

        final IcyMenuItem itemRemoveSlices = new IcyMenuItem(SequenceOperationActions.removeSlicesAction);
        add(itemRemoveSlices);

        addSeparator();

        final IcyMenuItem itemReverseFrames = new IcyMenuItem(SequenceOperationActions.reverseFramesAction);
        add(itemReverseFrames);

        final IcyMenuItem itemExtractFrame = new IcyMenuItem(SequenceOperationActions.extractFrameAction);
        add(itemExtractFrame);

        final IcyMenuItem itemRemoveFrame = new IcyMenuItem(SequenceOperationActions.removeFrameAction);
        add(itemRemoveFrame);

        final IcyMenuItem itemAddFrames = new IcyMenuItem(SequenceOperationActions.addFramesAction);
        add(itemAddFrames);

        final IcyMenuItem itemMergeFrames = new IcyMenuItem(SequenceOperationActions.mergeFramesAction);
        add(itemMergeFrames);

        final IcyMenuItem itemRemoveFrames = new IcyMenuItem(SequenceOperationActions.removeFramesAction);
        add(itemRemoveFrames);

        addSeparator();

        final IcyMenuItem itemConvertToSlices = new IcyMenuItem(SequenceOperationActions.convertToSlicesAction);
        add(itemConvertToSlices);

        final IcyMenuItem itemConvertToFrames = new IcyMenuItem(SequenceOperationActions.convertToFramesAction);
        add(itemConvertToFrames);

        final IcyMenuItem itemConvertAdvancedZT = new IcyMenuItem(SequenceOperationActions.advancedZTConvertAction);
        add(itemConvertAdvancedZT);

        reloadSequenceMenu();

        addActiveSequenceListener();
    }

    private void reloadSequenceMenu() {
        ThreadUtil.invokeLater(() -> {
            final Sequence active = Icy.getMainInterface().getActiveSequence();

            if (active != null) {
                // Channels (C) menu
                final int sizeC = active.getSizeC();
                menuExtractChannel.removeAll();
                menuRemoveChannel.removeAll();

                if (sizeC > 0) {
                    menuExtractChannel.add(itemExtractAllChannels);
                    menuExtractChannel.addSeparator();

                    for (int c = 0; c < sizeC; c++) {
                        final IcyMenuItem itemExtractChannel = new IcyMenuItem(SequenceOperationActions.extractChannelActions[c]);
                        menuExtractChannel.add(itemExtractChannel);

                        final IcyMenuItem itemRemoveChannel = new IcyMenuItem(SequenceOperationActions.removeChannelActions[c]);
                        menuRemoveChannel.add(itemRemoveChannel);
                    }

                    menuExtractChannel.setEnabled(true);
                    menuRemoveChannel.setEnabled(sizeC > 1);
                }
                else {
                    menuExtractChannel.setEnabled(false);
                    menuRemoveChannel.setEnabled(false);
                }
            }

            menuConversion.setEnabled(active != null);
            menuRawConversion.setEnabled(active != null);
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
