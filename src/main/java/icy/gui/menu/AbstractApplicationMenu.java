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

package icy.gui.menu;

import icy.gui.main.ActiveSequenceListener;
import icy.gui.main.GlobalROIListener;
import icy.gui.main.GlobalSequenceListener;
import icy.main.Icy;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import javax.swing.*;

/**
 * @author Thomas Musset
 */
public abstract class AbstractApplicationMenu extends JMenu implements GlobalSequenceListener, PluginLoaderListener, ActiveSequenceListener, GlobalROIListener {

    protected AbstractApplicationMenu(final String text) {
        super(text);
    }

    protected final void addGlobalSequenceListener() {
        Icy.getMainInterface().addGlobalSequenceListener(this);
    }

    protected final void removeGlobalSequenceListener() {
        Icy.getMainInterface().removeGlobalSequenceListener(this);
    }

    protected final void addPluginLoaderListener() {
        PluginLoader.addListener(this);
    }

    protected final void removePluginLoaderListener() {
        PluginLoader.removeListener(this);
    }

    protected final void addActiveSequenceListener() {
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    protected final void removeActiceSequenceListener() {
        Icy.getMainInterface().removeActiveSequenceListener(this);
    }

    protected final void addGlobalROIListener() {
        Icy.getMainInterface().addGlobalROIListener(this);
    }

    protected final void removeGlobalROIListener() {
        Icy.getMainInterface().removeGlobalROIListener(this);
    }

    @Override
    public void sequenceOpened(final Sequence sequence) {}

    @Override
    public void sequenceClosed(final Sequence sequence) {}

    @Override
    public void pluginLoaderChanged(final PluginLoaderEvent e) {}

    @Override
    public void sequenceActivated(final Sequence sequence) {}

    @Override
    public void sequenceDeactivated(final Sequence sequence) {}

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {}

    @Override
    public void roiAdded(final ROI roi) {}

    @Override
    public void roiRemoved(final ROI roi) {}
}
