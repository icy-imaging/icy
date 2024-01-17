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

package icy.gui.toolbar.panel;

import icy.common.listener.SkinChangeListener;
import icy.gui.util.LookAndFeelUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas Musset
 */
public abstract class ToolbarPanel extends JPanel implements SkinChangeListener {
    private Dimension saveSize;

    protected ToolbarPanel(final Dimension dimension) {
        super(new BorderLayout());
        setPreferredSize(dimension);
        saveSize = dimension;
        LookAndFeelUtil.addListener(this);
    }

    public final Dimension getSaveSize() {
        return saveSize;
    }

    public final void setSaveSize(final Dimension dimension) {
        if (dimension.width < getPreferredSize().width || dimension.height < getPreferredSize().height)
            saveSize = getPreferredSize();
        else
            saveSize = dimension;
    }

    @Override
    public void skinChanged() {
        this.updateUI();

        for (final Component c : this.getComponents()) {
            updateComponentUI(c);
        }
    }

    private void updateComponentUI(final Component component) {
        if (component instanceof final JComponent jc) {
            jc.updateUI();
            for (final Component c : jc.getComponents())
                updateComponentUI(c);
        }
    }
}
