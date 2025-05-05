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

package org.bioimageanalysis.icy.gui.toolbar.button;

import org.bioimageanalysis.icy.gui.component.button.IcyToggleButton;
import org.bioimageanalysis.icy.gui.frame.progress.FailedAnnounceFrame;
import org.bioimageanalysis.icy.gui.main.MainFrame;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.model.cache.ImageCache;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.preferences.ApplicationPreferences;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIconPack;
import org.bioimageanalysis.icy.system.logging.IcyLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Thomas Musset
 */
public final class EHCacheButton extends IcyToggleButton implements ActionListener {
    public EHCacheButton() {
        super(new SVGIconPack(SVGIcon.FLASH_OFF, SVGIcon.FLASH_ON));
        setFocusable(false);
        if (Icy.isCacheDisabled()) {
            super.setEnabled(false);
            setToolTipText("Image cache is disabled, cannot use the virtual mode");
        }
        else {
            setSelected(GeneralPreferences.getVirtualMode());
            setToolTipText((GeneralPreferences.getVirtualMode()) ? "Click to deactivate cache" : "Click to activate cache");
            addActionListener(this);
        }
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        setVirtualModeInternal(!GeneralPreferences.getVirtualMode());
    }

    private void setVirtualModeInternal(final boolean value) {
        boolean ok = false;
        try {
            // start / end image cache
            if (value)
                ok = ImageCache.init(ApplicationPreferences.getCacheMemoryMB(), ApplicationPreferences.getCachePath());
            else
                ok = ImageCache.shutDownIfEmpty();
        }
        catch (final Exception e) {
            IcyLogger.error(EHCacheButton.class, e, "Unable to init/shutdown image cache.");
        }

        // failed to change virtual mode state ?
        if (!ok) {
            // trying to disable cache ? --> show a message so user can understand why it didn't worked
            if (!value)
                new FailedAnnounceFrame("Cannot disable Image cache now. Some open images or sequences are using it.");

            // restore button state
            setSelected(!value);
            return;
        }

        // switch virtual mode state
        GeneralPreferences.setVirtualMode(value);
        setToolTipText((value) ? "Click to deactivate cache" : "Click to activate cache");

        // refresh viewers toolbar
        for (final Viewer viewer : Icy.getMainInterface().getViewers())
            viewer.refreshToolBar();
        // refresh title (display virtual mode or not)
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        if (mainFrame != null)
            mainFrame.refreshTitle();
    }

    /**
     * Method disable to prevent use from public access.
     */
    @Override
    public void setEnabled(final boolean b) {
        // Do nothing
    }
}
