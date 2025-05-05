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
package org.bioimageanalysis.icy.gui;

import org.bioimageanalysis.icy.system.SystemUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Since we use the OGL component the CustomPopupFactory isn't anymore required on OSX.<br>
 * Still we keep the class just in case as OpenGL will be soon removed from OSX and we may need to tweak that again then :-/
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class CustomPopupFactory extends PopupFactory {
    private final boolean macos;

    public CustomPopupFactory() {
        super();

        macos = SystemUtil.isMac();
    }

    @Override
    public Popup getPopup(final Component owner, final Component contents, final int x, final int y) {
        if (macos) {
            if (contents == null)
                throw new IllegalArgumentException("Popup.getPopup must be passed non-null contents");

            try {
                super.getPopup(owner, contents, x, y, true);
            }
            catch (final Exception e) {
                // ignore
            }
        }

        return super.getPopup(owner, contents, x, y);
    }
}
