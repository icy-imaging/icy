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
package org.bioimageanalysis.icy.gui.sequence.tools;

import org.bioimageanalysis.icy.gui.dialog.ActionDialog;
import org.bioimageanalysis.icy.gui.frame.progress.ProgressFrame;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.model.sequence.AbstractSequenceModel;
import org.bioimageanalysis.icy.model.sequence.DimensionId;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil.MergeCHelper;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil.MergeTHelper;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil.MergeZHelper;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;

/**
 * @author Stephane
 */
public class SequenceDimensionMergeFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 840989682349623342L;

    private class SequenceDimensionMergeFrameModel extends AbstractSequenceModel
    {
        public SequenceDimensionMergeFrameModel()
        {
            super();
        }

        @Override
        public int getSizeX()
        {
            return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.X);
        }

        @Override
        public int getSizeY()
        {
            return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.Y);
        }

        @Override
        public int getSizeZ()
        {
            if (getDimensionId() != DimensionId.Z)
                return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.Z);

            int size = 0;
            for (Sequence seq : mergePanel.getSequences())
                size += seq.getSizeZ();

            return size;
        }

        @Override
        public int getSizeT()
        {
            if (getDimensionId() != DimensionId.T)
                return SequenceUtil.getMaxDim(mergePanel.getSequences(), DimensionId.T);

            int size = 0;
            for (Sequence seq : mergePanel.getSequences())
                size += seq.getSizeT();

            return size;
        }

        @Override
        public int getSizeC()
        {
            int size = 0;

            if (getDimensionId() != DimensionId.C)
            {
                for (Sequence seq : mergePanel.getSequences())
                    size = Math.max(size, seq.getSizeC());

                return size;
            }

            // in this case we have only single channel sequence
            return mergePanel.getSelectedChannels().length;
        }

        @Override
        public BufferedImage getImage(int t, int z)
        {
            final Sequence[] sequences = mergePanel.getSequences();

            final int sizeX = SequenceUtil.getMaxDim(sequences, DimensionId.X);
            final int sizeY = SequenceUtil.getMaxDim(sequences, DimensionId.Y);
            final int sizeC = getSizeC();

            final IcyBufferedImage image;

            switch (getDimensionId())
            {
                default:
                case C:

                    image = MergeCHelper.getImage(sequences, mergePanel.getSelectedChannels(), sizeX, sizeY, t, z,
                            mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled());
                    break;

                case Z:
                    image = MergeZHelper.getImage(sequences, sizeX, sizeY, sizeC, t, z, mergePanel.isInterlaceEnabled(),
                            mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled());
                    break;

                case T:
                    image = MergeTHelper.getImage(sequences, sizeX, sizeY, sizeC, t, z, mergePanel.isInterlaceEnabled(),
                            mergePanel.isFillEmptyImageEnabled(), mergePanel.isFitImagesEnabled());
                    break;
            }

            return image;
        }

        @Override
        public BufferedImage getImage(int t, int z, int c)
        {
            final IcyBufferedImage img = (IcyBufferedImage) getImage(t, z);

            if (img != null)
                return img.getImage(c);

            return null;
        }

    }

    final SequenceDimensionMergePanel mergePanel;

    public SequenceDimensionMergeFrame(DimensionId dim)
    {
        super(dim.toString() + " Dimension merge");

        mergePanel = new SequenceDimensionMergePanel(dim);
        mergePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        mainPanel.add(mergePanel, BorderLayout.CENTER);
        validate();

        mergePanel.setModel(new SequenceDimensionMergeFrameModel());

        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Merging sequences...");

                        try
                        {
                            final Sequence out;

                            switch (getDimensionId())
                            {
                                default:
                                case C:
                                    out = SequenceUtil.concatC(mergePanel.getSequences(),
                                            mergePanel.getSelectedChannels(), mergePanel.isFillEmptyImageEnabled(),
                                            mergePanel.isFitImagesEnabled(), pf);
                                    break;

                                case Z:
                                    out = SequenceUtil.concatZ(mergePanel.getSequences(),
                                            mergePanel.isInterlaceEnabled(), mergePanel.isFillEmptyImageEnabled(),
                                            mergePanel.isFitImagesEnabled(), pf);
                                    break;

                                case T:
                                    out = SequenceUtil.concatT(mergePanel.getSequences(),
                                            mergePanel.isInterlaceEnabled(), mergePanel.isFillEmptyImageEnabled(),
                                            mergePanel.isFitImagesEnabled(), pf);
                                    break;
                            }

                            Icy.getMainInterface().addSequence(out);
                        }
                        finally
                        {
                            pf.close();
                        }
                    }
                });
            }
        });

        setSize(420, 520);
        ComponentUtil.center(this);

        setVisible(true);
    }

    DimensionId getDimensionId()
    {
        return mergePanel.getDimensionId();
    }

}
