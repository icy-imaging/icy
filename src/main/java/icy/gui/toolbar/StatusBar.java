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

package icy.gui.toolbar;

import icy.gui.toolbar.button.MemoryMonitorButton;
import icy.main.Icy;

import javax.swing.*;

/**
 * @author Thomas MUSSET
 */
public final class StatusBar extends IcyToolbar {
    /**
     * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
     */
    public StatusBar() {
        super(HORIZONTAL, false);

        add(Box.createHorizontalStrut(5));

        add(new JLabel("Icy v" + Icy.VERSION.toShortString()));

        add(Box.createHorizontalGlue());

        add(new MemoryMonitorButton());
    }
}
