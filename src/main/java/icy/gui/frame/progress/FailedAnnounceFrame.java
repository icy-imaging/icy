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

package icy.gui.frame.progress;

import icy.common.listener.SkinChangeListener;
import icy.common.listener.weak.WeakSkinChangeListener;
import icy.gui.util.LookAndFeelUtil;
import icy.system.thread.ThreadUtil;

/**
 * @author Stephane
 */
public class FailedAnnounceFrame extends AnnounceFrame {
    private static final int DEFAULT_LIVETIME = 0;

    /**
     * Show a <i>failed</i> announcement with specified parameters.
     *
     * @param message  message to display in announcement
     * @param liveTime life time in second (0 = infinite)
     */
    public FailedAnnounceFrame(final String message, final int liveTime) {
        super(message, liveTime);

        // don't try to go further
        if (headless)
            return;

        ThreadUtil.invokeLater(() -> {
            mainPanel.setOpaque(true);
            mainPanel.setBackground(LookAndFeelUtil.getRed());
            label.setForeground(LookAndFeelUtil.getAccentForeground());
        });
    }

    /**
     * Show a <i>failed</i> announcement with specified parameters.
     *
     * @param message message to display in announcement
     */
    public FailedAnnounceFrame(final String message) {
        this(message, DEFAULT_LIVETIME);
    }
}
