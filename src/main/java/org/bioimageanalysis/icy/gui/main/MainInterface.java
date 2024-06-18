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
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROI;
import org.bioimageanalysis.icy.gui.listener.*;
import org.bioimageanalysis.icy.gui.undo.IcyUndoManager;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.lut.LUT;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.swimmingPool.SwimmingPool;
import org.bioimageanalysis.icy.network.search.SearchEngine;
import org.bioimageanalysis.icy.system.preferences.XMLPreferences;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.util.ArrayList;
import java.util.List;

/**
 * MainInterface
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 * @see MainInterfaceGui
 */
public interface MainInterface {
    /**
     * Creates the windows in the Icy.getMainInterface()
     */
    void init();

    /**
     * Check if exit is allowed from registered listeners
     */
    boolean canExitExternal();

    /**
     * Return true is the application is running in headless mode (no screen device).
     */
    boolean isHeadLess();

    /**
     * Return true is the <i>virtual mode</i> is enabled.<br>
     * When virtual mode is enabled all Sequence are made virtual (same as volatile) so data is streamed from hard drive on demand. This is useful when you
     * manipulate large Sequence which don't fit in memory but this can make process much slower. Also some plugins won't work correctly on virtual Sequence
     * (modified data can be lost) so use it carefully.
     *
     * @see IcyBufferedImage#setVolatile(boolean)
     */
    boolean isVirtualMode();

    /**
     * Sets the <i>virtual mode</i>.<br>
     * When virtual mode is enabled all Sequence are made virtual (same as volatile) so data is streamed from hard drive on demand. This is useful when you
     * manipulate large Sequence which don't fit in memory but this can make process much slower. Also some plugins won't work correctly on virtual Sequence
     * (modified data can be lost) so use it carefully.
     *
     * @see IcyBufferedImage#setVolatile(boolean)
     */
    void setVirtualMode(boolean value);

    /**
     * Open a viewer for the specified sequence.
     */
    void addSequence(Sequence sequence);

    /**
     * Returns all internal frames
     */
    ArrayList<JInternalFrame> getInternalFrames();

    /**
     * Returns all external frames
     */
    ArrayList<JFrame> getExternalFrames();

    XMLPreferences getPreferences();

    /**
     * Returns the currently active plugins
     */
    ArrayList<Plugin> getActivePlugins();

    /**
     * Returns the active viewer window.
     * Returns <code>null</code> if there is no sequence opened.
     */
    Viewer getActiveViewer();

    /**
     * Returns the LUT from the active viewer window.
     * Returns <code>null</code> if there is no sequence opened.
     */
    LUT getActiveLUT();

    /**
     * Returns the current active sequence.<br>
     * Returns <code>null</code> if there is no sequence opened.
     */
    Sequence getActiveSequence();

    /**
     * Returns the current active image.<br>
     * It can return <code>null</code> if the active viewer is <code>null</code> or
     * if it uses 3D display so prefer {@link #getActiveSequence()} instead.
     */
    IcyBufferedImage getActiveImage();

    /**
     * Returns the current active {@link UndoManager} (UndoManager from active sequence).
     * It returns <code>null</code> if the active sequence is <code>null</code>.
     *
     * @see Sequence#getUndoManager()
     */
    IcyUndoManager getUndoManager();

    /**
     * Undo to the last <i>Undoable</i> change set in the active Sequence {@link UndoManager}
     *
     * @return <code>true</code> if the operation succeed
     * @see Sequence#undo()
     */
    boolean undo();

    /**
     * Redo the next <i>Undoable</i> change set in the active Sequence {@link UndoManager}
     *
     * @return <code>true</code> if the operation succeed
     * @see Sequence#redo()
     */
    boolean redo();

    /**
     * Returns all active viewers
     */
    ArrayList<Viewer> getViewers();

    /**
     * Set the current active viewer.
     *
     * @param viewer viewer which received activation
     */
    void setActiveViewer(Viewer viewer);

    /**
     * Set all active viewers to specified synchronization group id (0 means unsynchronized).
     */
    void setGlobalViewSyncId(int id);

    /**
     * Add the frame to the Desktop pane and change its layer value to make it over the other
     * internal frames.
     */
    void addToDesktopPane(JInternalFrame internalFrame);

    IcyDesktopPane getDesktopPane();

    TaskFrameManager getTaskWindowManager();

    void registerPlugin(Plugin plugin);

    void unRegisterPlugin(Plugin plugin);

    void registerViewer(Viewer viewer);

    void unRegisterViewer(Viewer viewer);

    /**
     * Get Icy main frame
     */
    MainFrame getMainFrame();

    /**
     * Get Icy main searh engine.
     */
    SearchEngine getSearchEngine();

    /**
     * Close all viewers displaying the specified sequence.
     */
    void closeSequence(Sequence sequence);

    /**
     * Close all viewers
     */
    void closeAllViewers();

    /**
     * Returns first viewer for the sequence containing specified ROI
     */
    Viewer getFirstViewerContaining(ROI roi);

    /**
     * Returns first viewer for the sequence containing specified Overlay
     */
    Viewer getFirstViewerContaining(Overlay overlay);

    /**
     * Returns first viewer attached to specified sequence
     */
    Viewer getFirstViewer(Sequence sequence);

    /**
     * Returns viewers attached to specified sequence
     */
    ArrayList<Viewer> getViewers(Sequence sequence);

    /**
     * Returns true if specified viewer is the unique viewer for its attached sequence
     */
    boolean isUniqueViewer(Viewer viewer);

    /**
     * Returns the list of opened sequence (sequence actually displayed in a viewer)
     */
    ArrayList<Sequence> getSequences();

    /**
     * Returns the list of opened sequence (sequence actually displayed in a viewer) matching the specified name.
     */
    ArrayList<Sequence> getSequences(String name);

    /**
     * Returns true if specified sequence is currently opened (displayed in a viewer)
     */
    boolean isOpened(Sequence sequence);

    /**
     * Returns the first active sequence containing the specified ROI
     */
    Sequence getFirstSequenceContaining(ROI roi);

    /**
     * Returns the first active sequence containing the specified Overlay
     */
    Sequence getFirstSequenceContaining(Overlay overlay);

    /**
     * Returns all active sequence containing the specified ROI
     */
    ArrayList<Sequence> getSequencesContaining(ROI roi);

    /**
     * Returns all active sequence containing the specified Overlay
     */
    List<Sequence> getSequencesContaining(Overlay overlay);

    /**
     * Returns all active ROI
     */
    ArrayList<ROI> getROIs();

    /**
     * Returns the ROI containing the specified overlay (if any)
     */
    ROI getROI(Overlay overlay);

    /**
     * Returns all active Overlay.
     */
    List<Overlay> getOverlays();

    /**
     * Returns the SwimmingPool object
     */
    SwimmingPool getSwimmingPool();

    /**
     * Returns current selected tool (ROI / Selection)
     */
    String getSelectedTool();

    /**
     * Set current selected tool (ROI / Selection).
     */
    void setSelectedTool(String command);

    /**
     * Returns true if the main frame is set as "always on top"
     */
    boolean isAlwaysOnTop();

    /**
     * Set the main frame as "always on top"
     */
    void setAlwaysOnTop(boolean value);

    /**
     * Returns true if the application is in "detached" mode
     */
    boolean isDetachedMode();

    /**
     * Set the the application is in "detached" mode
     */
    void setDetachedMode(boolean value);

    /**
     * Add global Viewer listener
     */
    void addGlobalViewerListener(GlobalViewerListener listener);

    /**
     * Remove global Viewer listener
     */
    void removeGlobalViewerListener(GlobalViewerListener listener);

    /**
     * Add global Sequence listener
     */
    void addGlobalSequenceListener(GlobalSequenceListener listener);

    /**
     * Remove global Sequence listener
     */
    void removeGlobalSequenceListener(GlobalSequenceListener listener);

    /**
     * Add global ROI listener
     */
    void addGlobalROIListener(GlobalROIListener listener);

    /**
     * Remove global ROI listener
     */
    void removeGlobalROIListener(GlobalROIListener listener);

    /**
     * Add global Overlay listener
     */
    void addGlobalOverlayListener(GlobalOverlayListener listener);

    /**
     * Remove global Overlay listener
     */
    void removeGlobalOverlayListener(GlobalOverlayListener listener);

    /**
     * Add global Plugin listener
     */
    void addGlobalPluginListener(GlobalPluginListener listener);

    /**
     * Remove global Plugin listener
     */
    void removeGlobalPluginListener(GlobalPluginListener listener);

    /**
     * Add active viewer listener.<br>
     * This permit to receive events of activated viewer only.<br>
     * It can also be used to detect viewer activation change.
     */
    void addActiveViewerListener(ActiveViewerListener listener);

    /**
     * Remove active viewer listener.
     */
    void removeActiveViewerListener(ActiveViewerListener listener);

    /**
     * Add active sequence listener.<br>
     * This permit to receive events of activated sequence only.<br>
     * It can also be used to detect sequence activation change.
     */
    void addActiveSequenceListener(ActiveSequenceListener listener);

    /**
     * Remove focused sequence listener.
     */
    void removeActiveSequenceListener(ActiveSequenceListener listener);

    /**
     * Add "can exit" listener.<br>
     * <br>
     * CAUTION : A weak reference is used to reference the listener for easier release<br>
     * so you should have a hard reference to your listener to keep it alive.
     */
    void addCanExitListener(AcceptListener listener);

    /**
     * Remove "can exit" listener
     */
    void removeCanExitListener(AcceptListener listener);

    void addROIToolChangeListener(ROIToolChangeListener listener);

    void removeROIToolChangeListener(ROIToolChangeListener listener);

    void changeROITool(Class<? extends PluginROI> pluginROI);
}