/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.gui.toolbar.button;

import fr.icy.Icy;
import fr.icy.gui.component.button.IcyToggleButton;
import fr.icy.gui.component.icon.IcyIconPack;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.frame.progress.FailedAnnounceFrame;
import fr.icy.gui.main.MainFrame;
import fr.icy.gui.viewer.Viewer;
import fr.icy.model.cache.ImageCache;
import fr.icy.system.preferences.ApplicationPreferences;
import fr.icy.system.preferences.GeneralPreferences;
import org.jetbrains.annotations.Contract;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thomas Musset
 */
public final class EHCacheButton extends IcyToggleButton implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(EHCacheButton.class.getName());

    public EHCacheButton() {
        super(new IcyIconPack(IcySVG.FLASH_OFF, IcySVG.FLASH_ON));
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
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Unable to init/shutdown image cache.", e);
        }

        // failed to change virtual mode state ?
        if (!ok) {
            // trying to disable cache ? --> show a message so user can understand why it didn't work
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
    @Contract(pure = true)
    @Override
    public void setEnabled(final boolean b) {
        // Do nothing
    }
}
