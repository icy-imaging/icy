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

package icy.gui.statusbar;

import icy.main.Icy;

import javax.swing.*;

/**
 * @author Thomas MUSSET
 */
public final class StatusBar extends JToolBar {
    /**
     * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
     */
    public StatusBar() {
        super(JToolBar.HORIZONTAL);
        setFloatable(false);

        final String version = Icy.version.toShortString();

        final JLabel icy = new JLabel("Icy v" + version);
        icy.setEnabled(false);
        add(icy);

        add(Box.createHorizontalGlue());

        add(new MemoryMonitor());
        add(new Separator());
        add(new EHCacheButton());
    }
}
