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
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Stephane
 */
public class SequenceResizeFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -8638672567750415881L;

    final SequenceResizePanel resizePanel;

    public SequenceResizeFrame(Sequence sequence)
    {
        super("Image size");

        resizePanel = new SequenceResizePanel(sequence);
        getMainPanel().add(resizePanel, BorderLayout.CENTER);
        validate();

        // action
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Resizing sequence...");
                        try
                        {
                            final Sequence seqOut = SequenceUtil.scale(resizePanel.getSequence(),
                                    resizePanel.getNewWidth(), resizePanel.getNewHeight(),
                                    resizePanel.getResizeContent(), resizePanel.getXAlign(), resizePanel.getYAlign(),
                                    resizePanel.getFilterType());

                            Icy.getMainInterface().addSequence(seqOut);
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
}
