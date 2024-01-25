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

package icy.gui.inspector;

import icy.gui.component.ExtTabbedPanel;
import icy.gui.component.ExternalizablePanel;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.main.ActiveViewerListener;
import icy.gui.main.MainFrame;
import icy.gui.system.OutputConsolePanel;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.image.cache.ImageCache;
import icy.main.Icy;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.*;

/**
 * This window shows all details about the current sequence.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class InspectorPanel extends ExternalizablePanel implements ActiveViewerListener, ActiveSequenceListener {
    // GUI
    final ExtTabbedPanel mainPane;

    final SequencePanel sequencePanel;
    final RoisPanel roisPanel;
    final LayersPanel layersPanel;
    final UndoManagerPanel historyPanel;
    final OutputConsolePanel outputConsolePanel;
    // final ChatPanel chatPanel;

    @Deprecated(since = "3.0.0", forRemoval = true)
    final IcyToggleButton virtualModeBtn;

    /**
     * The width of the inner component of the inspector should not exceed 300.
     */
    public InspectorPanel() {
        super("Inspector", "inspector", new Point(600, 140), new Dimension(300, 600));

        // tab panel
        mainPane = new ExtTabbedPanel();
        mainPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // main panels
        sequencePanel = new SequencePanel();
        // final JPanel pluginsPanel = new PluginsPanel();
        roisPanel = new RoisPanel();
        layersPanel = new LayersPanel();
        historyPanel = new UndoManagerPanel();
        outputConsolePanel = new OutputConsolePanel();
        // chatPanel = new ChatPanel();

        // virtual mode button (set the same size as memory monitor)
        virtualModeBtn = new IcyToggleButton(
                GoogleMaterialDesignIcons.FLASH_OFF,
                GoogleMaterialDesignIcons.FLASH_ON,
                48f
        );
        virtualModeBtn.setToolTipText("Enable / disable the virtual mode (all images are created in virtual mode)");
        virtualModeBtn.setHideActionText(true);
        virtualModeBtn.setFocusable(false);
        virtualModeBtn.setSelected(GeneralPreferences.getVirtualMode());
        virtualModeBtn.addActionListener(e -> setVirtualModeInternal(!GeneralPreferences.getVirtualMode()));

        final JScrollPane scrollPane = new JScrollPane(
                sequencePanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // add main tab panels
        mainPane.addTab("Sequence", null, scrollPane, "Sequence informations");
        // mainPane.add("Active Plugin", pluginsPanel);
        mainPane.addTab("ROI", null, roisPanel, "Manage / edit your ROI");
        mainPane.addTab("Layer", null, layersPanel, "Show all layers details");
        mainPane.addTab("History", null, historyPanel, "Actions history");
        mainPane.addTab("Output", null, outputConsolePanel, "Console output");
        // mainPane.addTab("Chat", null, chatPanel, "Chat room");

        // minimum required size for sequence infos panel
        final Dimension minDim = new Dimension(300, 480);
        getFrame().setMinimumSizeInternal(minDim);
        getFrame().setMinimumSizeExternal(minDim);
        setMinimumSize(minDim);
        setLayout(new BorderLayout());

        add(mainPane, BorderLayout.CENTER);

        validate();
        setVisible(true);

        outputConsolePanel.addOutputConsoleChangeListener((source, isError) -> {
            final int index = getIndexOfTab(outputConsolePanel);

            if ((index != -1) && (mainPane.getSelectedIndex() != index)) {
                final boolean fIsError = isError;

                ThreadUtil.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // if it's already red, don't change background
                        final boolean isRed = LookAndFeelUtil.isRed(mainPane.getBackgroundAt(index));
                        if (isRed)
                            return;

                        // change output console tab color when new data
                        if (fIsError)
                            mainPane.setBackgroundAt(index, LookAndFeelUtil.getRed());
                        else
                            mainPane.setBackgroundAt(index, LookAndFeelUtil.getBlue());

                        mainPane.setForegroundAt(index, LookAndFeelUtil.getAccentForeground());
                    }
                });
            }
        });

        // add focused sequence & viewer listener
        Icy.getMainInterface().addActiveViewerListener(this);
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    /**
     * @return the mainPane
     */
    public ExtTabbedPanel getMainPane() {
        return mainPane;
    }

    /**
     * @return the sequencePanel
     */
    public SequencePanel getSequencePanel() {
        return sequencePanel;
    }

    /**
     * @return the roisPanel
     */
    public RoisPanel getRoisPanel() {
        return roisPanel;
    }

    /**
     * @return the layersPanel
     */
    public LayersPanel getLayersPanel() {
        return layersPanel;
    }

    /**
     * @return the historyPanel
     */
    public UndoManagerPanel getHistoryPanel() {
        return historyPanel;
    }

    /**
     * @return the outputConsolePanel
     */
    public OutputConsolePanel getOutputConsolePanel() {
        return outputConsolePanel;
    }

    /**
     * @deprecated Use {@link GeneralPreferences#getVirtualMode()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static boolean getVirtualMode() {
        return GeneralPreferences.getVirtualMode();
    }

    public void setVirtualMode(final boolean value) {
        virtualModeBtn.setSelected(value);

        setVirtualModeInternal(value);
    }

    // TODO: 25/01/2023 Return value never used
    @Deprecated(since = "3.0.0", forRemoval = true)
    boolean setVirtualModeInternal(final boolean value) {
        boolean ok = false;
        try {
            // start / end image cache
            if (value)
                ok = ImageCache.init(ApplicationPreferences.getCacheMemoryMB(), ApplicationPreferences.getCachePath());
            else
                ok = ImageCache.shutDownIfEmpty();
        }
        catch (final Exception e) {
            IcyLogger.error(InspectorPanel.class, e, e.getLocalizedMessage());
        }

        // failed to change virtual mode state ?
        if (!ok) {
            // trying to disable cache ? --> show a message so user can understand why it didn't worked
            if (!value)
                new FailedAnnounceFrame("Cannot disable Image cache now. Some open images or sequences are using it.");

            // restore button state
            virtualModeBtn.setSelected(!value);
            return false;
        }

        // switch virtual mode state
        GeneralPreferences.setVirtualMode(value);
        // refresh viewers toolbar
        for (final Viewer viewer : Icy.getMainInterface().getViewers())
            viewer.refreshToolBar();
        // refresh title (display virtual mode or not)
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        if (mainFrame != null)
            mainFrame.refreshTitle();

        return true;
    }

    /**
     * Call this to disable 'virtual mode' button
     */
    public void imageCacheDisabled() {
        // image cache is disabled so we can't use caching
        setVirtualMode(false);
        virtualModeBtn.setEnabled(false);
        virtualModeBtn.setToolTipText("Image cache is disabled, cannot use the virtual mode");
    }

    /**
     * Return the index of specified tab component
     */
    protected int getIndexOfTab(final Component component) {
        return mainPane.indexOfComponent(component);
    }

    @Override
    public void viewerActivated(final Viewer viewer) {
        sequencePanel.viewerActivated(viewer);
        layersPanel.viewerActivated(viewer);
    }

    @Override
    public void viewerDeactivated(final Viewer viewer) {
        // nothing to do here
    }

    /**
     * Called when focused viewer has changed
     */
    @Override
    public void activeViewerChanged(final ViewerEvent event) {
        sequencePanel.activeViewerChanged(event);
        layersPanel.activeViewerChanged(event);
    }

    @Override
    public void sequenceActivated(final Sequence sequence) {
        sequencePanel.sequenceActivated(sequence);
        roisPanel.sequenceActivated(sequence);
        historyPanel.sequenceActivated(sequence);
    }

    @Override
    public void sequenceDeactivated(final Sequence sequence) {
        // nothing to do here
    }

    /**
     * Called by mainInterface when focused sequence has changed
     */
    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        sequencePanel.activeSequenceChanged(event);
        roisPanel.activeSequenceChanged(event);
    }

    /**
     * Change tab background color when skin changed
     */
    @Override
    public void updateUI() {
        super.updateUI();

        if (outputConsolePanel == null || mainPane == null)
            return;

        final int index = getIndexOfTab(outputConsolePanel);

        if ((index != -1) && (mainPane.getSelectedIndex() != index)) {
            ThreadUtil.invokeLater(() -> {
                final boolean isRed = LookAndFeelUtil.isRed(mainPane.getBackgroundAt(index));
                final boolean isBlue = LookAndFeelUtil.isBlue(mainPane.getBackgroundAt(index));

                if (!isRed && !isBlue)
                    return;

                if (isRed)
                    mainPane.setBackgroundAt(index, LookAndFeelUtil.getRed());
                else
                    mainPane.setBackgroundAt(index, LookAndFeelUtil.getBlue());
                mainPane.setForegroundAt(index, LookAndFeelUtil.getAccentForeground());
            });
        }
    }
}
