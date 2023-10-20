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

import javax.swing.*;

/**
 * @author Thomas MUSSET
 */
abstract class IcyToolbar extends JToolBar {
    private final int orientation;
    private final boolean floatable;

    protected IcyToolbar(final int orientation, final boolean floatable) {
        super();

        super.setOrientation(orientation);
        super.setFloatable(floatable);

        this.orientation = orientation;
        this.floatable = floatable;
    }

    /**
     * Forbid orientation change.
     */
    @Override
    public final void setOrientation(final int o) {
        super.setOrientation(orientation);
    }

    /**
     * Forbid floatable change.
     */
    @Override
    public final void setFloatable(final boolean b) {
        super.setFloatable(floatable);
    }
}
