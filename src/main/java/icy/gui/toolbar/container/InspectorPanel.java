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

package icy.gui.toolbar.container;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * @author Thomas Musset
 */
public final class InspectorPanel extends ToolbarContainer {
    private static InspectorPanel instance = null;

    public static InspectorPanel getInstance() {
        if (instance == null)
            instance = new InspectorPanel();
        return instance;
    }

    @Override
    protected void showParent(@Nonnull final JSplitPane pane) {
        pane.setDividerLocation(pane.getWidth() - getPreferredSize().width - 5);
        pane.setEnabled(true);
        pane.updateUI();
    }

    @Override
    protected void closeParent(@Nonnull final JSplitPane pane) {
        pane.setDividerLocation(1.d);
        pane.setEnabled(false);
    }
}
