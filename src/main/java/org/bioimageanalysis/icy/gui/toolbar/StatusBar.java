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

package org.bioimageanalysis.icy.gui.toolbar;

import org.bioimageanalysis.icy.gui.toolbar.button.MemoryMonitorButton;
import org.bioimageanalysis.icy.Icy;

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas Musset
 */
public final class StatusBar extends IcyToolbar {
    /**
     * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
     */
    public StatusBar() {
        super(HORIZONTAL, false);

        add(Box.createHorizontalStrut(10));

        add(new JLabel("Icy v" + Icy.VERSION.toShortString()));

        add(Box.createHorizontalGlue());

        add(new TaskProgress());

        add(Box.createHorizontalStrut(5));

        add(new MemoryMonitorButton());

        add(Box.createHorizontalStrut(10));

        /*Dimension progressBarDimension = new Dimension(40, 10);

        JProgressBar progressBarCPU = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        //progressBarCPU.setStringPainted(true);
        progressBarCPU.setPreferredSize(progressBarDimension);
        progressBarCPU.setMaximumSize(progressBarDimension);
        progressBarCPU.setMinimumSize(progressBarDimension);
        progressBarCPU.setValue(50);
        add(progressBarCPU);

        add(Box.createHorizontalStrut(5));

        JProgressBar progressBarRAM = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        //progressBarRAM.setStringPainted(true);
        progressBarRAM.setPreferredSize(progressBarDimension);
        progressBarRAM.setMaximumSize(progressBarDimension);
        progressBarRAM.setMinimumSize(progressBarDimension);
        progressBarRAM.setValue(50);
        add(progressBarRAM);*/

        //add(Box.createHorizontalStrut(5));
    }
}
