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

package org.bioimageanalysis.icy.gui.toolbar.container;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Thomas Musset
 */
public final class StatusPanel extends ToolbarContainer {
    private static StatusPanel instance = null;

    public static StatusPanel getInstance() {
        if (instance == null)
            instance = new StatusPanel();
        return instance;
    }

    @Override
    protected void showParent(@NotNull final JSplitPane pane) {
        pane.setDividerLocation(pane.getHeight() - getPreferredSize().height - 5);
        pane.setEnabled(true);
    }

    @Override
    protected void closeParent(@NotNull final JSplitPane pane) {
        pane.setDividerLocation(1.d);
        pane.setEnabled(false);
    }
}
