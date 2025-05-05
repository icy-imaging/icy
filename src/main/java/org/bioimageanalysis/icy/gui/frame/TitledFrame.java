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

package org.bioimageanalysis.icy.gui.frame;

import javax.swing.*;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 */
public class TitledFrame extends IcyFrame {
    protected final JPanel mainPanel;

    public TitledFrame(final String title) {
        this(title, null, false, false, false, false);
    }

    public TitledFrame(final String title, final boolean resizable) {
        this(title, null, resizable, false, false, false);
    }

    public TitledFrame(final String title, final boolean resizable, final boolean closable) {
        this(title, null, resizable, closable, false, false);
    }

    public TitledFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable) {
        this(title, null, resizable, closable, maximizable, false);
    }

    public TitledFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable, final boolean iconifiable) {
        this(title, null, resizable, closable, maximizable, iconifiable);
    }

    public TitledFrame(final String title, final Dimension dim, final boolean resizable, final boolean closable, final boolean maximizable, final boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        setLayout(new BorderLayout());

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * @return the mainPanel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
}
