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

package org.bioimageanalysis.icy.gui.main;

import org.bioimageanalysis.icy.common.listener.AcceptListener;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROI;
import org.bioimageanalysis.icy.gui.listener.*;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.lut.LUT;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.system.preferences.XMLPreferences;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.network.search.SearchEngine;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.swimmingPool.SwimmingPool;
import org.bioimageanalysis.icy.common.collection.CollectionUtil;
import org.bioimageanalysis.icy.gui.undo.IcyUndoManager;
import org.bioimageanalysis.icy.common.string.StringUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MainInterfaceBatch
 * Default implementation used when Icy is launched in batch mode, without any GUI
 *
 * @author Nicolas Herve
 * @author Stephane Dallongeville
 * @author Thomas Musset
 * @see MainInterfaceGui
 */
public class MainInterfaceBatch implements MainInterface {
    /**
     * Swimming Pool can be useful even in batch mode
     */
    private final SwimmingPool swimmingPool;
    /**
     * We keep trace of active sequence.
     */
    private Sequence activeSequence;
    /**
     * We keep trace of active plugin.
     */
    private Plugin activePlugin;

    public MainInterfaceBatch() {
        swimmingPool = new SwimmingPool();
    }

    @Override
    public void init() {
        activeSequence = null;
        activePlugin = null;
    }

    @Override
    public boolean isHeadLess() {
        // always true with this interface
        return true;
    }

    @Override
    public ArrayList<JFrame> getExternalFrames() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<JInternalFrame> getInternalFrames() {
        return new ArrayList<>();
    }

    @Override
    public XMLPreferences getPreferences() {
        return null;
    }

    @Override
    public ArrayList<Plugin> getActivePlugins() {
        return CollectionUtil.createArrayList(activePlugin, false);
    }

    @Override
    public LUT getActiveLUT() {
        return null;
    }

    @Override
    public Viewer getActiveViewer() {
        return null;
    }

    @Override
    public Sequence getActiveSequence() {
        return activeSequence;
    }

    @Override
    public IcyBufferedImage getActiveImage() {
        if (activeSequence != null)
            return activeSequence.getFirstImage();

        return null;
    }

    @Override
    public IcyUndoManager getUndoManager() {
        if (activeSequence != null)
            return activeSequence.getUndoManager();

        return null;
    }

    @Override
    public boolean undo() {
        if (activeSequence != null)
            return activeSequence.undo();

        return false;
    }

    @Override
    public boolean redo() {
        if (activeSequence != null)
            return activeSequence.redo();

        return false;
    }

    @Override
    public ArrayList<Viewer> getViewers() {
        return new ArrayList<>();
    }

    @Override
    public void setActiveViewer(final Viewer viewer) {
    }

    @Override
    public void addToDesktopPane(final JInternalFrame internalFrame) {
    }

    @Override
    public IcyDesktopPane getDesktopPane() {
        return null;
    }

    @Override
    public TaskFrameManager getTaskWindowManager() {
        return null;
    }

    @Override
    public void registerPlugin(final Plugin plugin) {
        if (plugin != null)
            activePlugin = plugin;
    }

    @Override
    public void unRegisterPlugin(final Plugin plugin) {
        if (plugin == activePlugin)
            activePlugin = null;
    }

    @Override
    public void registerViewer(final Viewer viewer) {
    }

    @Override
    public void unRegisterViewer(final Viewer viewer) {
    }

    @Override
    public MainFrame getMainFrame() {
        return null;
    }

    @Override
    public void closeSequence(final Sequence sequence) {
        if (sequence == activeSequence)
            activeSequence = null;
    }

    @Override
    public void closeAllViewers() {
        activeSequence = null;
    }

    @Override
    public Viewer getFirstViewer(final Sequence sequence) {
        return null;
    }

    @Override
    public ArrayList<Viewer> getViewers(final Sequence sequence) {
        return new ArrayList<>();
    }

    @Override
    public boolean isUniqueViewer(final Viewer viewer) {
        return false;
    }

    @Override
    public ArrayList<Sequence> getSequences() {
        if (activeSequence != null)
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<>();
    }

    @Override
    public ArrayList<Sequence> getSequences(final String name) {
        if ((activeSequence != null) && StringUtil.equals(name, activeSequence.getName()))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<>();
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(final ROI roi) {
        if ((activeSequence != null) && activeSequence.contains(roi))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<>();
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(final Overlay overlay) {
        if ((activeSequence != null) && activeSequence.contains(overlay))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<>();
    }

    @Override
    public ArrayList<ROI> getROIs() {
        if (activeSequence != null)
            return activeSequence.getROIs();

        // TODO: add ROI from swimming pool ?

        return new ArrayList<>();
    }

    @Override
    public ROI getROI(final Overlay overlay) {
        final List<ROI> rois = getROIs();

        for (final ROI roi : rois)
            if (roi.getOverlay() == overlay)
                return roi;

        return null;
    }

    @Override
    public List<Overlay> getOverlays() {
        if (activeSequence != null)
            return activeSequence.getOverlays();

        // TODO: add Overlay from swimming pool ?

        return new ArrayList<>();
    }

    @Override
    public SwimmingPool getSwimmingPool() {
        return swimmingPool;
    }

    @Override
    public String getSelectedTool() {
        return null;
    }

    @Override
    public void setSelectedTool(final String command) {

    }

    @Override
    public boolean isAlwaysOnTop() {
        return false;
    }

    @Override
    public boolean isDetachedMode() {
        return false;
    }

    @Override
    public void setAlwaysOnTop(final boolean value) {
    }

    @Override
    public void setDetachedMode(final boolean value) {
    }

    @Override
    public void addCanExitListener(final AcceptListener listener) {
    }

    @Override
    public void removeCanExitListener(final AcceptListener listener) {
    }

    @Override
    public boolean isOpened(final Sequence sequence) {
        return (sequence == activeSequence);
    }

    @Override
    public Sequence getFirstSequenceContaining(final ROI roi) {
        if ((activeSequence != null) && activeSequence.contains(roi))
            return activeSequence;

        return null;
    }

    @Override
    public Sequence getFirstSequenceContaining(final Overlay overlay) {
        if ((activeSequence != null) && activeSequence.contains(overlay))
            return activeSequence;

        return null;
    }

    @Override
    public Viewer getFirstViewerContaining(final ROI roi) {
        return null;
    }

    @Override
    public Viewer getFirstViewerContaining(final Overlay overlay) {
        return null;
    }

    @Override
    public boolean canExitExternal() {
        return true;
    }

    @Override
    public void addGlobalViewerListener(final GlobalViewerListener listener) {
    }

    @Override
    public void removeGlobalViewerListener(final GlobalViewerListener listener) {
    }

    @Override
    public void addGlobalSequenceListener(final GlobalSequenceListener listener) {
    }

    @Override
    public void removeGlobalSequenceListener(final GlobalSequenceListener listener) {
    }

    @Override
    public void addGlobalROIListener(final GlobalROIListener listener) {
    }

    @Override
    public void removeGlobalROIListener(final GlobalROIListener listener) {
    }

    @Override
    public void addGlobalOverlayListener(final GlobalOverlayListener listener) {
    }

    @Override
    public void removeGlobalOverlayListener(final GlobalOverlayListener listener) {
    }

    @Override
    public void addGlobalPluginListener(final GlobalPluginListener listener) {
    }

    @Override
    public void removeGlobalPluginListener(final GlobalPluginListener listener) {
    }

    @Override
    public void addActiveViewerListener(final ActiveViewerListener listener) {
    }

    @Override
    public void removeActiveViewerListener(final ActiveViewerListener listener) {
    }

    @Override
    public void addActiveSequenceListener(final ActiveSequenceListener listener) {
    }

    @Override
    public void removeActiveSequenceListener(final ActiveSequenceListener listener) {
    }

    @Override
    public void addROIToolChangeListener(final ROIToolChangeListener listener) {
    }

    @Override
    public void removeROIToolChangeListener(final ROIToolChangeListener listener) {
    }

    @Override
    public void addSequence(final Sequence sequence) {
        if (sequence != null)
            activeSequence = sequence;
    }

    @Override
    public void setGlobalViewSyncId(final int id) {

    }

    @Override
    public SearchEngine getSearchEngine() {
        return null;
    }

    @Override
    public boolean isVirtualMode() {
        return GeneralPreferences.getVirtualMode();
    }

    @Override
    public void setVirtualMode(final boolean value) {
        GeneralPreferences.setVirtualMode(value);
    }

    @Override
    public void changeROITool(final Class<? extends PluginROI> pluginROI) {
    }
}
