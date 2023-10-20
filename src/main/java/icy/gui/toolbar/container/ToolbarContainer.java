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

package icy.gui.toolbar.container;

import icy.gui.toolbar.panel.ToolbarPanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

/**
 * Base for container panel in Icy's toolbar
 *
 * @author Thomas MUSSET
 */
public abstract class ToolbarContainer extends JPanel {
    private static final Dimension DEFAULT_DIMENSION = new Dimension(0, 0);

    private ToolbarPanel panel = null;

    protected ToolbarContainer() {
        super(new BorderLayout());
        updateSize(null);
    }

    protected final void updateSize(@Nullable final ToolbarPanel panel) {
        if (panel != null) {
            setPreferredSize((panel.getSaveSize() != null) ? panel.getSaveSize() : panel.getPreferredSize());
            setMinimumSize(panel.getPreferredSize());
        }
        else {
            setPreferredSize(DEFAULT_DIMENSION);
            setMinimumSize(DEFAULT_DIMENSION);
        }
    }

    private void saveSize() {
        if (panel != null)
            panel.setSaveSize(new Dimension(getWidth(), getHeight()));
    }

    protected abstract void showParent(@Nonnull final JSplitPane pane);

    public final void show(@Nonnull final ToolbarPanel panel) {
        removeAll();
        this.panel = panel;
        add(panel, BorderLayout.CENTER);
        updateSize(panel);
        revalidate();
        getParent().repaint();

        final Container c = getParent();
        if (c instanceof JSplitPane)
            showParent((JSplitPane) c);
    }

    protected abstract void closeParent(@Nonnull final JSplitPane pane);

    public final void close() {
        saveSize();
        this.panel = null;
        removeAll();
        updateSize(null);
        revalidate();
        getParent().repaint();

        final Container c = getParent();
        if (c instanceof JSplitPane)
            closeParent((JSplitPane) c);
    }
}
