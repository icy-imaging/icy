/*
 * Copyright 2010-2023 Institut Pasteur.
 *
 * This file is part of Icy.
 *
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
package icy.gui.main;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.undo.UndoManager;

import icy.common.listener.AcceptListener;
import icy.gui.inspector.InspectorPanel;
import icy.gui.inspector.LayersPanel;
import icy.gui.inspector.RoisPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ROITask;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.imagej.ImageJWrapper;
import icy.painter.Overlay;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.search.SearchEngine;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingPool;
import icy.undo.IcyUndoManager;

/**
 * MainInterface
 *
 * @author Fabrice de Chaumont &amp; Stephane
 * @author Thomas MUSSET
 * @see icy.gui.main.MainInterfaceGui
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
     * Returns the inspector object (right informations panel)
     */
    InspectorPanel getInspector();

    /**
     * Returns the ROI manager panel
     */
    RoisPanel getRoisPanel();

    /**
     * Returns the Layer manager panel
     */
    LayersPanel getLayersPanel();

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
     * @deprecated Use {@link #getActiveViewer()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    Viewer getFocusedViewer();

    /**
     * @deprecated Use {@link #getActiveSequence()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    Sequence getFocusedSequence();

    /**
     * @deprecated Use {@link #getActiveImage()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    IcyBufferedImage getFocusedImage();

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
     * @deprecated Use {@link #setActiveViewer(Viewer)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void setFocusedViewer(Viewer viewer);

    /**
     * Set all active viewers to specified synchronization group id (0 means unsynchronized).
     */
    void setGlobalViewSyncId(int id);

    /**
     * Add the frame to the Desktop pane and change its layer value to make it over the other
     * internal frames.
     *
     * @param internalFrame
     */
    void addToDesktopPane(JInternalFrame internalFrame);

    IcyDesktopPane getDesktopPane();

    @Deprecated(since = "3.0.0", forRemoval = true)
    ApplicationMenu getApplicationMenu();

    TaskFrameManager getTaskWindowManager();

    @Deprecated(since = "2.4.3", forRemoval = true)
    void registerExternalFrame(JFrame frame);

    @Deprecated(since = "2.4.3", forRemoval = true)
    void unRegisterExternalFrame(JFrame frame);

    void registerPlugin(Plugin plugin);

    void unRegisterPlugin(Plugin plugin);

    void registerViewer(Viewer viewer);

    void unRegisterViewer(Viewer viewer);

    /**
     * @deprecated Use {@link #getMainFrame()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    MainFrame getFrame();

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
     * @deprecated Use {@link #closeSequence(Sequence)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void closeViewersOfSequence(Sequence sequence);

    /**
     * Close all viewers
     */
    void closeAllViewers();

    /**
     * Returns first viewer for the sequence containing specified ROI
     */
    Viewer getFirstViewerContaining(ROI roi);

    /**
     * Returns first viewer for the sequence containing specified Painter.
     *
     * @deprecated use {@link #getFirstViewerContaining(Overlay)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    Viewer getFirstViewerContaining(Painter painter);

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
     * Use {@link #getFirstSequenceContaining(ROI)} instead
     *
     * @deprecated
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    Sequence getFirstSequencesContaining(ROI roi);

    /**
     * Use {@link #getFirstSequenceContaining(Overlay)} instead
     *
     * @deprecated
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    Sequence getFirstSequencesContaining(Painter painter);

    /**
     * Returns the first active sequence containing the specified ROI
     */
    Sequence getFirstSequenceContaining(ROI roi);

    /**
     * Returns the first active sequence containing the specified Painter
     *
     * @deprecated Use {@link #getFirstSequenceContaining(Overlay)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    Sequence getFirstSequenceContaining(Painter painter);

    /**
     * Returns the first active sequence containing the specified Overlay
     */
    Sequence getFirstSequenceContaining(Overlay overlay);

    /**
     * Returns all active sequence containing the specified ROI
     */
    ArrayList<Sequence> getSequencesContaining(ROI roi);

    /**
     * Returns all active sequence containing the specified Painter
     *
     * @deprecated Use {@link #getSequencesContaining(Overlay)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    ArrayList<Sequence> getSequencesContaining(Painter painter);

    /**
     * Returns all active sequence containing the specified Overlay
     */
    List<Sequence> getSequencesContaining(Overlay overlay);

    /**
     * Returns all active ROI
     */
    ArrayList<ROI> getROIs();

    /**
     * Returns the ROI containing the specified painter (if any).
     *
     * @deprecated Use {@link #getROI(Overlay)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    ROI getROI(Painter painter);

    /**
     * Returns the ROI containing the specified overlay (if any)
     */
    ROI getROI(Overlay overlay);

    /**
     * Returns all active Painter.
     *
     * @deprecated Use {@link #getOverlays()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    ArrayList<Painter> getPainters();

    /**
     * Returns all active Overlay.
     */
    List<Overlay> getOverlays();

    /**
     * Returns the SwimmingPool object
     */
    SwimmingPool getSwimmingPool();

    /**
     * Returns the ImageJ object instance
     */
    ImageJWrapper getImageJ();

    /**
     * Returns current selected tool (ROI / Selection)
     */
    String getSelectedTool();

    /**
     * Set current selected tool (ROI / Selection).
     */
    void setSelectedTool(String command);

    /**
     * Returns the ROI task of the Ribbon menu.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    ROITask getROIRibbonTask();

    /**
     * @deprecated Use {@link #getROIRibbonTask()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    ToolRibbonTask getToolRibbon();

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
     * @deprecated Use addGlobalXXXListener instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void addListener(MainListener listener);

    /**
     * @deprecated Use removeGlobalXXXListener instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void removeListener(MainListener listener);

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
     * @deprecated Use {@link #addActiveViewerListener(ActiveViewerListener)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void addFocusedViewerListener(FocusedViewerListener listener);

    /**
     * @deprecated Use {@link #removeActiveViewerListener(ActiveViewerListener)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void removeFocusedViewerListener(FocusedViewerListener listener);

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
     * @deprecated Use {@link #addActiveSequenceListener(ActiveSequenceListener)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void addFocusedSequenceListener(FocusedSequenceListener listener);

    /**
     * @deprecated Use {@link #removeActiveSequenceListener(ActiveSequenceListener)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void removeFocusedSequenceListener(FocusedSequenceListener listener);

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

    /**
     * @deprecated
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void beginUpdate();

    /**
     * @deprecated
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    void endUpdate();

    /**
     * @deprecated
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    boolean isUpdating();
}