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

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas Musset
 */
// TODO
public class TaskProgress extends JProgressBar {
    public TaskProgress() {
        super(HORIZONTAL, 0, 100);

        final Dimension defaultSize = new Dimension(100, 5);

        setPreferredSize(defaultSize);
        setMinimumSize(defaultSize);
        setMaximumSize(defaultSize);

        setValue(0);
        //setIndeterminate(true);

        setToolTipText("Click to show background processes");

        setVisible(false);
    }
}
