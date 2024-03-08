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

package icy.gui.viewer;

import icy.action.CanvasActions;
import icy.action.CanvasActions.ToggleLayersAction;
import icy.action.SequenceOperationActions.ToggleVirtualSequenceAction;
import icy.action.ViewerActions;
import icy.action.WindowActions;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvasEvent;
import icy.canvas.IcyCanvasListener;
import icy.common.listener.ProgressListener;
import icy.file.FileUtil;
import icy.file.SequenceFileGroupImporter;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.component.renderer.LabelComboBoxRenderer;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.SaverDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.lut.LUTViewer;
import icy.gui.lut.abstract_.IcyLutViewer;
import icy.gui.plugin.PluginComboBoxRenderer;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.interface_.PluginCanvas;
import icy.preferences.GeneralPreferences;
import icy.resource.icon.IcySVGIcon;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.system.IcyExceptionHandler;
import icy.system.IcyHandledException;
import icy.system.thread.ThreadUtil;
import icy.util.GraphicsUtil;
import icy.util.Random;
import icy.util.StringUtil;
import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.canvas.VtkCanvasPlugin;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Viewer send an event if the IcyCanvas change.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Viewer extends IcyFrame implements KeyListener, SequenceListener, IcyCanvasListener, PluginLoaderListener {
    private class ViewerMainPanel extends JPanel {
        public ViewerMainPanel() {
            super();
        }

        public void drawTextCenter(final Graphics2D g, final String text, final float alpha) {
            final Rectangle2D rect = GraphicsUtil.getStringBounds(g, text);
            final int w = (int) rect.getWidth();
            final int h = (int) rect.getHeight();
            final int x = (getWidth() - (w + 8 + 2)) / 2;
            final int y = (getHeight() - (h + 8 + 2)) / 2;

            g.setColor(Color.gray);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.fillRoundRect(x, y, w + 8, h + 8, 8, 8);

            g.setColor(Color.white);
            g.drawString(text, x + 4, y + 2 + h);
        }

        @Override
        public void paint(final Graphics g) {
            // currently modifying canvas ?
            super.paint(g);

            // display a message
            if (settingCanvas) {
                final Graphics2D g2 = (Graphics2D) g.create();

                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawTextCenter(g2, "Loading canvas...", 0.8f);
                g2.dispose();
            }
        }
    }

    /**
     * only show it once per Icy session
     */
    private static boolean toolTipVirtualDone = false;

    /**
     * associated LUT
     */
    private LUT lut;
    /**
     * associated canvas
     */
    private IcyCanvas canvas;
    /**
     * associated sequence
     */
    Sequence sequence;

    /***/
    private final EventListenerList listeners = new EventListenerList();

    /**
     * GUI
     */
    private JToolBar toolBar;
    private ViewerMainPanel mainPanel;
    private LUTViewer lutViewer;

    JComboBox<String> canvasComboBox;
    JComboBox<JLabel> lockComboBox;
    JToggleButton layersEnabledButton;
    IcyButton screenShotButton;
    IcyButton screenShotAlternateButton;
    IcyButton duplicateButton;
    IcyToggleButton switchStateButton;
    IcyToggleButton virtualButton;

    /**
     * internals
     */
    boolean initialized;
    private final Runnable lutUpdater;
    boolean settingCanvas;

    public Viewer(final Sequence sequence, final boolean visible) {
        super("Viewer", true, true, true, true);

        if (sequence == null)
            throw new IllegalArgumentException("Can't open a null sequence.");

        this.sequence = sequence;

        // default
        canvas = null;
        lut = null;
        lutViewer = null;
        initialized = false;
        settingCanvas = false;

        lutUpdater = () -> {
            // don't need to update that too much
            ThreadUtil.sleep(1);

            ThreadUtil.invokeNow(() -> {
                final LUT lut = getLut();

                // closed --> ignore
                if (lut != null) {
                    // refresh LUT viewer
                    setLutViewer(new LUTViewer(Viewer.this, lut));
                    // notify
                    fireViewerChanged(ViewerEventType.LUT_CHANGED);
                }
            });
        };

        mainPanel = new ViewerMainPanel();
        mainPanel.setLayout(new BorderLayout());

        // set menu directly in system menu so we don't need a extra MenuBar
        setSystemMenuCallback(Viewer.this::getMenu);

        // build tool bar
        buildToolBar();

        // create a new compatible LUT
        final LUT lut = sequence.createCompatibleLUT();
        // restore user LUT if needed
        if (sequence.hasUserLUT()) {
            final LUT userLut = sequence.getUserLUT();

            // restore colormaps (without alpha)
            lut.setColorMaps(userLut, false);
            // then restore scalers
            lut.setScalers(userLut);
        }

        // set lut (this modify lutPanel)
        setLut(lut);
        // set default canvas to first available canvas plugin (Canvas2D should be first)
        setCanvas(IcyCanvas.getCanvasPluginNames().get(0));

        setLayout(new BorderLayout());

        add(toolBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // setting frame
        refreshViewerTitle();
        setFocusable(true);
        // set position depending window mode
        setLocationInternal(20 + Random.nextInt(100), 20 + Random.nextInt(60));
        setLocationExternal(100 + Random.nextInt(200), 100 + Random.nextInt(150));
        setSize(640, 480);

        // initial position in sequence
        if (sequence.isEmpty())
            setPositionZ(0);
        else
            setPositionZ(((sequence.getSizeZ() + 1) / 2) - 1);

        addFrameListener(new IcyFrameAdapter() {
            @Override
            public void icyFrameOpened(final IcyFrameEvent e) {
                if (!initialized) {
                    if ((Viewer.this.sequence != null) && !Viewer.this.sequence.isEmpty()) {
                        adjustViewerToImageSize();
                        initialized = true;
                    }
                }
            }

            @Override
            public void icyFrameActivated(final IcyFrameEvent e) {
                Icy.getMainInterface().setActiveViewer(Viewer.this);
            }

            @Override
            public void icyFrameExternalized(final IcyFrameEvent e) {
                refreshToolBar();
            }

            @Override
            public void icyFrameInternalized(final IcyFrameEvent e) {
                refreshToolBar();
            }

            @Override
            public void icyFrameClosing(final IcyFrameEvent e) {
                onClosing();
            }
        });

        addKeyListener(this);
        sequence.addListener(this);
        PluginLoader.addListener(this);

        // do this when viewer is initialized
        Icy.getMainInterface().registerViewer(this);
        // automatically add it to the desktop pane
        addToDesktopPane();

        if (visible) {
            setVisible(true);
            requestFocus();
            toFront();
        }
        else
            setVisible(false);

        // can be done after setVisible
        buildActionMap();
    }

    public Viewer(final Sequence sequence) {
        this(sequence, true);
    }

    void buildActionMap() {
        // global input map
        buildActionMap(getInputMap(JComponent.WHEN_FOCUSED), getActionMap());
    }

    protected void buildActionMap(final InputMap imap, final ActionMap amap) {
        imap.put(WindowActions.gridTileAction.getKeyStroke(), WindowActions.gridTileAction.getName());
        imap.put(WindowActions.horizontalTileAction.getKeyStroke(), WindowActions.horizontalTileAction.getName());
        imap.put(WindowActions.verticalTileAction.getKeyStroke(), WindowActions.verticalTileAction.getName());
        imap.put(CanvasActions.globalDisableSyncAction.getKeyStroke(), CanvasActions.globalDisableSyncAction.getName());
        imap.put(CanvasActions.globalSyncGroup1Action.getKeyStroke(), CanvasActions.globalSyncGroup1Action.getName());
        imap.put(CanvasActions.globalSyncGroup2Action.getKeyStroke(), CanvasActions.globalSyncGroup2Action.getName());
        imap.put(CanvasActions.globalSyncGroup3Action.getKeyStroke(), CanvasActions.globalSyncGroup3Action.getName());
        imap.put(CanvasActions.globalSyncGroup4Action.getKeyStroke(), CanvasActions.globalSyncGroup4Action.getName());

        amap.put(WindowActions.gridTileAction.getName(), WindowActions.gridTileAction);
        amap.put(WindowActions.horizontalTileAction.getName(), WindowActions.horizontalTileAction);
        amap.put(WindowActions.verticalTileAction.getName(), WindowActions.verticalTileAction);
        amap.put(CanvasActions.globalDisableSyncAction.getName(), CanvasActions.globalDisableSyncAction);
        amap.put(CanvasActions.globalSyncGroup1Action.getName(), CanvasActions.globalSyncGroup1Action);
        amap.put(CanvasActions.globalSyncGroup2Action.getName(), CanvasActions.globalSyncGroup2Action);
        amap.put(CanvasActions.globalSyncGroup3Action.getName(), CanvasActions.globalSyncGroup3Action);
        amap.put(CanvasActions.globalSyncGroup4Action.getName(), CanvasActions.globalSyncGroup4Action);
    }

    /**
     * Called when user want to close viewer
     */
    protected void onClosing() {
        // this sequence was not saved
        if ((sequence != null) && (sequence.getFilename() == null)) {
            // save new sequence enabled ?
            if (GeneralPreferences.getSaveNewSequence()) {
                final int res = ConfirmDialog.confirmEx("Save sequence",
                        "Do you want to save '" + sequence.getName() + "' before closing it ?",
                        ConfirmDialog.YES_NO_CANCEL_OPTION);

                switch (res) {
                    case JOptionPane.YES_OPTION:
                        // save the image
                        new SaverDialog(sequence);

                    case JOptionPane.NO_OPTION:
                        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        break;

                    case JOptionPane.CANCEL_OPTION:
                    default:
                        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        break;
                }

                return;
            }
        }

        // just close it
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Called when viewer is closed.<br>
     * Release as much references we can here because of the
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4759312">
     * JInternalFrame bug</a>.
     */
    @Override
    public void onClosed() {
        // notify close
        fireViewerClosed();

        // remove listeners
        sequence.removeListener(this);
        if (canvas != null)
            canvas.removeCanvasListener(this);
        PluginLoader.removeListener(this);

        icy.main.Icy.getMainInterface().unRegisterViewer(this);

        // AWT JDesktopPane keep reference on last closed JInternalFrame
        // it's good to free as much reference we can here
        if (canvas != null)
            canvas.shutDown();
        if (lutViewer != null)
            lutViewer.dispose();
        if (mainPanel != null)
            mainPanel.removeAll();
        if (toolBar != null)
            toolBar.removeAll();

        // remove all listeners for this viewer
        final ViewerListener[] vls = listeners.getListeners(ViewerListener.class);
        for (final ViewerListener vl : vls)
            listeners.remove(ViewerListener.class, vl);

        lutViewer = null;
        mainPanel = null;

        canvas = null;
        sequence = null;
        lut = null;
        toolBar = null;
        canvasComboBox = null;
        lockComboBox = null;
        duplicateButton = null;
        layersEnabledButton = null;
        screenShotAlternateButton = null;
        screenShotButton = null;
        switchStateButton = null;
        virtualButton = null;

        super.onClosed();
    }

    void adjustViewerToImageSize() {
        if (canvas instanceof final IcyCanvas2D cnv) {
            final int ix = cnv.getImageSizeX();
            final int iy = cnv.getImageSizeY();

            if ((ix > 0) && (iy > 0)) {
                // find scale factor to fit image in a 640x540 sized window
                // and limit zoom to 100%
                final double scale = Math.min(Math.min(640d / ix, 540d / iy), 1d);

                cnv.setScaleX(scale);
                cnv.setScaleY(scale);

                // this actually resize viewer as canvas size depend from it
                cnv.fitCanvasToImage();
            }
        }

        // minimum size to start : 400, 240
        final Dimension size = new Dimension(Math.max(getWidth(), 400), Math.max(getHeight(), 240));
        // minimum size global : 200, 140
        final Dimension minSize = new Dimension(200, 140);

        // adjust size of both frames
        setSizeExternal(size);
        setSizeInternal(size);
        setMinimumSizeInternal(minSize);
        setMinimumSizeExternal(minSize);
    }

    /**
     * Rebuild and return viewer menu
     */
    JMenu getMenu() {
        final JMenu result = getDefaultSystemMenu();

        final JMenuItem overlayItem = new JMenuItem(CanvasActions.toggleLayersAction);
        if ((canvas != null) && canvas.isLayersVisible())
            overlayItem.setText("Hide layers");
        else
            overlayItem.setText("Show layers");
        final JMenuItem duplicateItem = new JMenuItem(ViewerActions.duplicateAction);

        // set menu
        result.insert(overlayItem, 0);
        result.insertSeparator(1);
        result.insert(duplicateItem, 2);

        return result;
    }

    protected void buildLockCombo() {
        final ArrayList<JLabel> labels = new ArrayList<>();

        // get sync action labels
        labels.add(new JLabel("Sync OFF", new IcySVGIcon(SVGIcon.FILTER_NONE, Color.DARK_GRAY), JLabel.LEFT));
        labels.add(new JLabel("Sync 1", new IcySVGIcon(SVGIcon.FILTER_1, Color.GREEN.darker()), JLabel.LEFT));
        labels.add(new JLabel("Sync 2", new IcySVGIcon(SVGIcon.FILTER_2, Color.ORANGE.darker()), JLabel.LEFT));
        labels.add(new JLabel("Sync 3", new IcySVGIcon(SVGIcon.FILTER_3, Color.BLUE.darker()), JLabel.LEFT));
        labels.add(new JLabel("Sync 4", new IcySVGIcon(SVGIcon.FILTER_4, Color.RED.darker()), JLabel.LEFT));

        // build comboBox with lock id
        lockComboBox = new JComboBox<>(new Vector<>(labels));
        // set specific renderer
        lockComboBox.setRenderer(new LabelComboBoxRenderer(lockComboBox));
        // limit size
        lockComboBox.setMaximumSize(lockComboBox.getMinimumSize());
        lockComboBox.setToolTipText("Select synchronisation group");
        // don't want focusable here
        lockComboBox.setFocusable(false);
        // needed because of VTK
        lockComboBox.setLightWeightPopupEnabled(false);

        // action on canvas change
        lockComboBox.addActionListener(e -> {
            // adjust lock id
            setViewSyncId(lockComboBox.getSelectedIndex());
        });
    }

    void buildCanvasCombo() {
        // build comboBox with canvas plugins
        canvasComboBox = new JComboBox<>(new Vector<>(IcyCanvas.getCanvasPluginNames()));
        // specific renderer
        canvasComboBox.setRenderer(new PluginComboBoxRenderer(canvasComboBox, false));
        //canvasComboBox.setBackground(Color.WHITE);
        // TODO: 27/01/2023 Set dropdown menu background to white
        // limit size
        //ComponentUtil.setFixedWidth(canvasComboBox, 48);
        canvasComboBox.setMaximumSize(canvasComboBox.getMinimumSize());
        canvasComboBox.setToolTipText("Select canvas type");
        // don't want focusable here
        canvasComboBox.setFocusable(false);
        // needed because of VTK
        canvasComboBox.setLightWeightPopupEnabled(false);

        // action on canvas change
        canvasComboBox.addActionListener(e -> {
            // set selected canvas
            if (canvasComboBox.getSelectedItem() instanceof String)
                setCanvas((String) canvasComboBox.getSelectedItem());
        });
    }

    /**
     * build the toolBar
     */
    protected void buildToolBar() {
        // build combo box
        buildLockCombo();
        buildCanvasCombo();

        // build buttons
        layersEnabledButton = new IcyToggleButton(new SVGIconPack(SVGIcon.LAYERS_CLEAR, SVGIcon.LAYERS));
        layersEnabledButton.addActionListener(new ToggleLayersAction(true));
        layersEnabledButton.setHideActionText(true);
        layersEnabledButton.setFocusable(false);
        layersEnabledButton.setSelected(true);

        screenShotButton = new IcyButton(SVGIcon.PHOTO_CAMERA);
        screenShotButton.addActionListener(CanvasActions.screenShotAction);
        screenShotButton.setFocusable(false);
        screenShotButton.setHideActionText(true);

        screenShotAlternateButton = new IcyButton(SVGIcon.ASPECT_RATIO);
        screenShotAlternateButton.addActionListener(CanvasActions.screenShotAlternateAction);
        screenShotAlternateButton.setFocusable(false);
        screenShotAlternateButton.setHideActionText(true);

        duplicateButton = new IcyButton(SVGIcon.CONTENT_COPY);
        duplicateButton.addActionListener(ViewerActions.duplicateAction);
        duplicateButton.setFocusable(false);
        duplicateButton.setHideActionText(true);
        // duplicateButton.setToolTipText("Duplicate view (no data duplication)");

        switchStateButton = new IcyToggleButton(SVGIcon.OPEN_IN_NEW);
        switchStateButton.addActionListener(getSwitchStateAction());
        switchStateButton.setSelected(isExternalized());
        switchStateButton.setFocusable(false);
        switchStateButton.setHideActionText(true);

        virtualButton = new IcyToggleButton(new SVGIconPack(SVGIcon.FLASH_OFF, SVGIcon.FLASH_ON));
        virtualButton.addActionListener(new ToggleVirtualSequenceAction(false));
        virtualButton.setFocusable(false);
        virtualButton.setHideActionText(true);

        // and build the toolbar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        // so we don't have any border
        toolBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        //ComponentUtil.setPreferredHeight(toolBar, 26);

        updateToolbarComponents();
    }

    void updateToolbarComponents() {
        if (toolBar != null) {
            toolBar.removeAll();

            toolBar.add(lockComboBox);
            toolBar.addSeparator();
            toolBar.add(canvasComboBox);
            toolBar.addSeparator();
            toolBar.add(layersEnabledButton);
            if (canvas != null)
                canvas.customizeToolbar(toolBar);
            toolBar.add(Box.createHorizontalGlue());
            toolBar.addSeparator();
            toolBar.add(screenShotButton);
            toolBar.add(screenShotAlternateButton);
            toolBar.addSeparator();
            toolBar.add(duplicateButton);
            toolBar.add(switchStateButton);
            toolBar.addSeparator();
            toolBar.add(virtualButton);
        }
    }

    /**
     * Force lock combo-box refresh (should be used internally only)
     */
    public void refreshLockCombo() {
        final int syncId = getViewSyncId();

        lockComboBox.setEnabled(isSynchronizedViewSupported());
        lockComboBox.setSelectedIndex(syncId);

        switch (syncId) {
            case 0:
                //lockComboBox.setBackground(Color.gray);
                lockComboBox.setToolTipText("Synchronization disabled");
                break;

            case 1:
                //lockComboBox.setBackground(Color.GREEN.darker().darker());
                lockComboBox.setToolTipText("Full synchronization group 1 (view and Z/T position)");
                break;

            case 2:
                //lockComboBox.setBackground(Color.YELLOW.darker().darker());
                lockComboBox.setToolTipText("Full synchronization group 2 (view and Z/T position)");
                break;

            case 3:
                //lockComboBox.setBackground(Color.BLUE.darker().darker());
                lockComboBox.setToolTipText("View synchronization group (view synched but not Z/T position)");
                break;

            case 4:
                //lockComboBox.setBackground(Color.RED.darker().darker());
                lockComboBox.setToolTipText("Slice synchronization group (Z/T position synched but not view)");
                break;
        }
    }

    /**
     * Force canvas combo-box refresh (should be used internally only)
     */
    public void refreshCanvasCombo() {
        if (canvas != null) {
            // get plugin class name for this canvas
            final String pluginName = IcyCanvas.getPluginClassName(canvas.getClass().getName());

            if (pluginName != null) {
                // align canvas combo to plugin name
                if (canvasComboBox.getSelectedItem() != null && !canvasComboBox.getSelectedItem().equals(pluginName))
                    canvasComboBox.setSelectedItem(pluginName);
            }
        }
    }

    /**
     * Force toolbar refresh (should be used internally only)
     */
    public void refreshToolBar() {
        // FIXME : switchStateButton stay selected after action
        //final boolean layersVisible = (canvas != null) ? canvas.isLayersVisible() : false;
        final boolean layersVisible = (canvas != null && canvas.isLayersVisible());

        layersEnabledButton.setSelected(layersVisible);
        if (layersVisible)
            layersEnabledButton.setToolTipText("Hide layers");
        else
            layersEnabledButton.setToolTipText("Show layers");

        final Sequence seq = getSequence();
        final boolean virtual = (seq != null) && seq.isVirtual();
        // update virtual state
        virtualButton.setSelected(virtual);
        // update enable state of the button
        //((IcyAbstractAction) virtualButton.getAction()).enabledChanged();
        if (Icy.isCacheDisabled()) {
            virtualButton.setToolTipText("Image cache is disabled, cannot use virtual sequence");
            virtualButton.setEnabled(false);
        }
        else {
            virtualButton.setEnabled(true);
            if (virtual) {
                virtualButton.setToolTipText("Disable virtual sequence (caching)");

                // virtual was enabled for this sequence --> show tooltip to explain
                if (!toolTipVirtualDone) {
                    final URL resource = Icy.class.getResource("/image/help/viewer_virtual.jpg");
                    if (resource != null) {
                        final ToolTipFrame tooltip = new ToolTipFrame("<html>" + "<img src=\""
                                + resource + "\" /><br>"
                                + "<b>Your image has been made <i>virtual</i></b>.<br> This means that its data can be stored on disk to spare memory but this is at the cost of slower display / processing.<br>"
                                + "Also you should note that <b>some plugins aren't compatible with <i>virtual</i> images</b> and so the result may be inconsistent (possible data lost)."
                                + "</html>", 30, "viewerVirtual");
                        tooltip.setSize(400, 180);
                    }

                    toolTipVirtualDone = true;
                }
            }
            else
                virtualButton.setToolTipText("Enable virtual sequence (caching)");
        }

        if (switchStateButton != null)
            switchStateButton.setSelected(isExternalized());

        // refresh combos
        refreshLockCombo();
        refreshCanvasCombo();

        toolBar.repaint();
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence() {
        return sequence;
    }

    /**
     * Set the specified LUT for the viewer.
     */
    public void setLut(final LUT value) {
        if ((lut != value) && (sequence != null) && sequence.isLutCompatible(value)) {
            // set new lut & notify change
            lut = value;
            lutChanged();
        }
    }

    /**
     * Returns the viewer LUT
     */
    public LUT getLut() {
        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return lut;

        // sequence can be asynchronously modified so we have to test change on Getter
        if ((lut == null) || !sequence.isLutCompatible(lut)) {
            // sequence type has changed, we need to recreate a compatible LUT
            final LUT newLut = sequence.createCompatibleLUT();

            // keep the color map of previous LUT if they have the same number of channels
            if ((lut != null) && (lut.getNumChannel() == newLut.getNumChannel()))
                newLut.getColorSpace().setColorMaps(lut.getColorSpace(), true);

            // set the new lut
            lut = newLut;
            lutChanged();
        }

        return lut;
    }

    /**
     * Set the specified canvas for the viewer (from the {@link PluginCanvas} class name).
     *
     * @see IcyCanvas#getCanvasPluginNames()
     */
    public void setCanvas(final String pluginClassName) {
        // not the same canvas ?
        if ((canvas == null) || !StringUtil.equals(canvas.getClass().getName(), IcyCanvas.getCanvasClassName(pluginClassName))) {
            try {
                IcyCanvas newCanvas;
                String className = pluginClassName;

                // VTK Canvas ?
                if (StringUtil.equals(className, VtkCanvasPlugin.class.getName())) {
                    // VTK canvas doesn't support duplicated view
                    for (final Viewer v : Icy.getMainInterface().getViewers()) {
                        // we have another viewer with duplicated VTK view ? --> show warning at least
                        if ((v != this) && (v.getSequence() == getSequence()) && (v.getCanvas() instanceof VtkCanvas)) {
                            if (!ConfirmDialog.confirm("Caution",
                                    "You may experience problems using duplicated 3D view containing objects (ROI). Do you want to continue ?",
                                    ConfirmDialog.OK_CANCEL_OPTION))
                                // use current canvas class
                                className = canvas.getClass().getName();

                            break;
                        }
                    }
                }

                settingCanvas = true;
                // show loading message
                mainPanel.paintImmediately(mainPanel.getBounds());
                try {
                    // try to create the new canvas
                    newCanvas = IcyCanvas.create(className, this);
                }
                catch (final Throwable e) {
                    switch (e) {
                        case final IcyHandledException ignored -> {
                            // just ignore
                        }
                        case final UnsupportedOperationException ignored -> MessageDialog.showDialog(e.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                        case final Exception ignored -> IcyExceptionHandler.handleException(new ClassNotFoundException("Cannot find '" + className + "' class --> cannot create the canvas.", e), true);
                        default -> IcyExceptionHandler.handleException(e, true);
                    }

                    // create a new instance of current canvas
                    newCanvas = IcyCanvas.create(canvas.getClass().getName(), this);
                }
                finally {
                    settingCanvas = false;
                }

                final int saveX;
                final int saveY;
                final int saveZ;
                final int saveT;
                final int saveC;

                // save properties and shutdown previous canvas
                if (canvas != null) {
                    // save position
                    saveX = canvas.getPositionX();
                    saveY = canvas.getPositionY();
                    saveZ = canvas.getPositionZ();
                    saveT = canvas.getPositionT();
                    saveC = canvas.getPositionC();

                    canvas.removePropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
                    canvas.removeCanvasListener(this);
                    // --> this actually can do some restore operation (as the palette) after creation of the new canvas
                    canvas.shutDown();
                    // remove from mainPanel
                    mainPanel.remove(canvas);
                }
                else
                    saveX = saveY = saveZ = saveT = saveC = -1;

                // prepare new canvas
                newCanvas.addCanvasListener(this);
                newCanvas.addPropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
                // add to mainPanel
                mainPanel.add(newCanvas, BorderLayout.CENTER);

                // restore position
                if (saveX != -1)
                    newCanvas.setPositionX(saveX);
                if (saveY != -1)
                    newCanvas.setPositionY(saveY);
                if (saveZ != -1)
                    newCanvas.setPositionZ(saveZ);
                if (saveT != -1)
                    newCanvas.setPositionT(saveT);
                if (saveC != -1)
                    newCanvas.setPositionC(saveC);

                // canvas set :)
                canvas = newCanvas;
            }
            catch (final Throwable e) {
                IcyExceptionHandler.handleException(e, true);
            }

            mainPanel.revalidate();

            // refresh viewer menu (so overlay checkbox is correctly set)
            updateSystemMenu();
            updateToolbarComponents();
            refreshToolBar();

            // fix the OSX lost keyboard focus on canvas change in detached mode.
            KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle(getCanvas());

            // notify canvas changed to listener
            fireViewerChanged(ViewerEventType.CANVAS_CHANGED);
        }
    }

    /**
     * @deprecated Use {@link #setCanvas(String)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void setCanvas(final IcyCanvas value) {
        if (canvas == value)
            return;

        final int saveX;
        final int saveY;
        final int saveZ;
        final int saveT;
        final int saveC;

        if (canvas != null) {
            // save position
            saveX = canvas.getPositionX();
            saveY = canvas.getPositionY();
            saveZ = canvas.getPositionZ();
            saveT = canvas.getPositionT();
            saveC = canvas.getPositionC();

            canvas.removePropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
            canvas.removeCanvasListener(this);
            canvas.shutDown();
            // remove from mainPanel
            mainPanel.remove(canvas);
        }
        else
            saveX = saveY = saveZ = saveT = saveC = -1;

        // set new canvas
        canvas = value;

        if (canvas != null) {
            canvas.addCanvasListener(this);
            canvas.addPropertyChangeListener(IcyCanvas.PROPERTY_LAYERS_VISIBLE, this);
            // add to mainPanel
            mainPanel.add(canvas, BorderLayout.CENTER);

            // restore position
            if (saveX != -1)
                canvas.setPositionX(saveX);
            if (saveY != -1)
                canvas.setPositionY(saveY);
            if (saveZ != -1)
                canvas.setPositionZ(saveZ);
            if (saveT != -1)
                canvas.setPositionT(saveT);
            if (saveC != -1)
                canvas.setPositionC(saveC);
        }

        mainPanel.revalidate();

        // refresh viewer menu (so overlay checkbox is correctly set)
        updateSystemMenu();
        updateToolbarComponents();
        refreshToolBar();

        // fix the OSX lost keyboard focus on canvas change in detached mode.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle(getCanvas());

        // notify canvas changed to listener
        fireViewerChanged(ViewerEventType.CANVAS_CHANGED);
    }

    /**
     * Returns true if the viewer initialization (correct image resizing) is completed.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Return the viewer Canvas object
     */
    public IcyCanvas getCanvas() {
        return canvas;
    }

    /**
     * Return the viewer Canvas panel
     */
    public JPanel getCanvasPanel() {
        if (canvas != null)
            return canvas.getPanel();

        return null;
    }

    /**
     * Return the viewer Lut panel
     */
    public LUTViewer getLutViewer() {
        return lutViewer;
    }

    /**
     * Set the {@link LUTViewer} for this viewer.
     */
    public void setLutViewer(final LUTViewer value) {
        if (lutViewer != value) {
            if (lutViewer != null)
                lutViewer.dispose();
            lutViewer = value;
        }
    }

    /**
     * @deprecated Use {@link #getLutViewer()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyLutViewer getLutPanel() {
        return getLutViewer();
    }

    /**
     * @deprecated Use {@link #setLutViewer(LUTViewer)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void setLutPanel(final IcyLutViewer lutViewer) {
        setLutViewer((LUTViewer) lutViewer);
    }

    /**
     * Return the viewer ToolBar object
     */
    public JToolBar getToolBar() {
        return toolBar;
    }

    /**
     * @return current T (-1 if all selected/displayed)
     */
    public int getPositionT() {
        if (canvas != null)
            return canvas.getPositionT();

        return 0;
    }

    /**
     * Set the current T position (for multi frame sequence).
     */
    public void setPositionT(final int t) {
        if (canvas != null)
            canvas.setPositionT(t);
    }

    /**
     * @return current Z (-1 if all selected/displayed)
     */
    public int getPositionZ() {
        if (canvas != null)
            return canvas.getPositionZ();

        return 0;
    }

    /**
     * Set the current Z position (for stack sequence).
     */
    public void setPositionZ(final int z) {
        if (canvas != null)
            canvas.setPositionZ(z);
    }

    /**
     * @return current C (-1 if all selected/displayed)
     */
    public int getPositionC() {
        if (canvas != null)
            return canvas.getPositionC();

        return 0;
    }

    /**
     * Set the current C (channel) position (multi channel sequence)
     */
    public void setPositionC(final int c) {
        if (canvas != null)
            canvas.setPositionC(c);
    }

    /**
     * @deprecated Use {@link #getPositionT()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public int getT() {
        return getPositionT();
    }

    /**
     * @deprecated Use {@link #setPositionT(int)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void setT(final int t) {
        setPositionT(t);
    }

    /**
     * @deprecated Use {@link #getPositionZ()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public int getZ() {
        return getPositionZ();
    }

    /**
     * @deprecated Use {@link #setPositionZ(int)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void setZ(final int z) {
        setPositionZ(z);
    }

    /**
     * @deprecated Use {@link #getPositionZ()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public int getC() {
        return getPositionC();
    }

    /**
     * @deprecated Use {@link #setPositionZ(int)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void setC(final int c) {
        setPositionC(c);
    }

    /**
     * Get maximum T value
     */
    public int getMaxT() {
        if (canvas != null)
            return canvas.getMaxPositionT();

        return 0;
    }

    /**
     * Get maximum Z value
     */
    public int getMaxZ() {
        if (canvas != null)
            return canvas.getMaxPositionZ();

        return 0;
    }

    /**
     * Get maximum C value
     */
    public int getMaxC() {
        if (canvas != null)
            return canvas.getMaxPositionC();

        return 0;
    }

    /**
     * return true if current canvas's viewer does support synchronized view
     */
    public boolean isSynchronizedViewSupported() {
        if (canvas != null)
            return canvas.isSynchronizationSupported();

        return false;
    }

    /**
     * @return the viewSyncId
     */
    public int getViewSyncId() {
        if (canvas != null)
            return canvas.getSyncId();

        return -1;
    }

    /**
     * Set the view synchronization group id (0 means unsynchronized).
     *
     * @param id the view synchronization id to set
     * @see IcyCanvas#setSyncId(int)
     */
    public boolean setViewSyncId(final int id) {
        if (canvas != null)
            return canvas.setSyncId(id);

        return false;
    }

    /**
     * Return true if this viewer has its view synchronized
     */
    public boolean isViewSynchronized() {
        if (canvas != null)
            return canvas.isSynchronized();

        return false;
    }

    /**
     * Delegation for {@link IcyCanvas#getImage(int, int, int)}
     */
    public IcyBufferedImage getImage(final int t, final int z, final int c) {
        if (canvas != null)
            return canvas.getImage(t, z, c);

        return null;
    }

    /**
     * @deprecated Use {@link #getImage(int, int, int)} with C = -1 instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IcyBufferedImage getImage(final int t, final int z) {
        return getImage(t, z, -1);
    }

    /**
     * Get the current image
     *
     * @return current image
     */
    public IcyBufferedImage getCurrentImage() {
        if (canvas != null)
            return canvas.getCurrentImage();

        return null;
    }

    /**
     * Return the number of "selected" samples
     */
    public int getNumSelectedSamples() {
        if (canvas != null)
            return canvas.getNumSelectedSamples();

        return 0;
    }

    /**
     * @see icy.canvas.IcyCanvas#getRenderedImage(int, int, int, boolean)
     */
    public BufferedImage getRenderedImage(final int t, final int z, final int c, final boolean canvasView) throws InterruptedException {
        if (canvas == null)
            return null;

        return canvas.getRenderedImage(t, z, c, canvasView);
    }

    /**
     * @see icy.canvas.IcyCanvas#getRenderedSequence(boolean, icy.common.listener.ProgressListener)
     */
    public Sequence getRenderedSequence(final boolean canvasView, final ProgressListener progressListener) throws InterruptedException {
        if (canvas == null)
            return null;

        return canvas.getRenderedSequence(canvasView, progressListener);
    }

    /**
     * Returns the T navigation panel.
     */
    protected TNavigationPanel getTNavigationPanel() {
        if (canvas == null)
            return null;

        return canvas.getTNavigationPanel();
    }

    /**
     * Returns the frame rate (given in frame per second) for play command.
     */
    public int getFrameRate() {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            return tNav.getFrameRate();

        return 0;
    }

    /**
     * Sets the frame rate (given in frame per second) for play command.
     */
    public void setFrameRate(final int fps) {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.setFrameRate(fps);
    }

    /**
     * Returns true if <code>repeat</code> is enabled for play command.
     */
    public boolean isRepeat() {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            return tNav.isRepeat();

        return false;
    }

    /**
     * Set <code>repeat</code> mode for play command.
     */
    public void setRepeat(final boolean value) {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.setRepeat(value);
    }

    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying() {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            return tNav.isPlaying();

        return false;
    }

    /**
     * Start sequence play.
     *
     * @see #stopPlay()
     * @see #setRepeat(boolean)
     */
    public void startPlay() {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.startPlay();
    }

    /**
     * Stop sequence play.
     *
     * @see #startPlay()
     */
    public void stopPlay() {
        final TNavigationPanel tNav = getTNavigationPanel();

        if (tNav != null)
            tNav.stopPlay();
    }

    /**
     * Return true if only this viewer is currently displaying its attached sequence
     */
    public boolean isUnique() {
        return Icy.getMainInterface().isUniqueViewer(this);
    }

    protected void lutChanged() {
        // can be called from external thread, replace it in AWT dispatch thread
        ThreadUtil.bgRunSingle(lutUpdater);
    }

    protected void positionChanged(final DimensionId dim) {
        fireViewerChanged(ViewerEventType.POSITION_CHANGED, dim);

        refreshViewerTitle();
    }

    /**
     * Add a listener
     */
    public void addListener(final ViewerListener listener) {
        listeners.add(ViewerListener.class, listener);
    }

    /**
     * Remove a listener
     */
    public void removeListener(final ViewerListener listener) {
        listeners.remove(ViewerListener.class, listener);
    }

    void fireViewerChanged(final ViewerEventType eventType, final DimensionId dim) {
        final ViewerEvent event = new ViewerEvent(this, eventType, dim);

        for (final ViewerListener viewerListener : listeners.getListeners(ViewerListener.class))
            viewerListener.viewerChanged(event);
    }

    void fireViewerChanged(final ViewerEventType event) {
        fireViewerChanged(event, DimensionId.NULL);
    }

    protected void fireViewerClosed() {
        for (final ViewerListener viewerListener : listeners.getListeners(ViewerListener.class))
            viewerListener.viewerClosed(this);
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        // forward to canvas
        if ((canvas != null) && (!e.isConsumed()))
            canvas.keyPressed(e);
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        // forward to canvas
        if ((canvas != null) && (!e.isConsumed()))
            canvas.keyReleased(e);
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        // forward to canvas
        if ((canvas != null) && (!e.isConsumed()))
            canvas.keyTyped(e);
    }

    /**
     * Refresh frame's title
     */
    public void refreshViewerTitle() {
        // have to test this as we release sequence reference on closed
        if (sequence != null) {
            final int posZ = getPositionZ();
            final int posT = getPositionT();

            // dataset name
            String title = sequence.getName();

            // grouped dataset with a 2D viewer ?
            if ((canvas != null) && (posZ != -1) && (posT != -1) && (sequence.getImageProvider() instanceof SequenceFileGroupImporter)) {
                final String subName = FileUtil.getFileName(sequence.getFilename(posT, posZ, 0), false);

                // add current displayed image name
                if (!StringUtil.isEmpty(subName))
                    title += " - " + subName;
            }

            // update title
            setTitle(title);
        }
        else
            setTitle("no image");
    }

    @Override
    public void sequenceChanged(final SequenceEvent event) {
        switch (event.getSourceType()) {
            case SEQUENCE_META:
                final String meta = (String) event.getSource();

                if (StringUtil.isEmpty(meta) || StringUtil.equals(meta, Sequence.ID_NAME))
                    refreshViewerTitle();
                // update virtual state if needed
                if (initialized && StringUtil.equals(meta, Sequence.ID_VIRTUAL))
                    refreshToolBar();
                break;

            case SEQUENCE_DATA:
            case SEQUENCE_COLORMAP:
                break;

            case SEQUENCE_TYPE:
                // might need initialization
                if (!initialized && (sequence != null) && !sequence.isEmpty()) {
                    adjustViewerToImageSize();
                    initialized = true;
                }

                // we update LUT on type change directly on getLut() method

                // // try to keep current LUT if possible
                if (sequence != null && !sequence.isLutCompatible(lut))
                    // need to update the lut according to the colormodel change
                    setLut(sequence.createCompatibleLUT());
                break;

            case SEQUENCE_COMPONENTBOUNDS:
                // refresh lut scalers from sequence default lut
                final LUT sequenceLut = sequence.getDefaultLUT();

                if (!sequenceLut.isCompatible(lut) || (lutViewer == null))
                    lut.setScalers(sequenceLut);
                break;
        }
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
    }

    @Override
    public void canvasChanged(final IcyCanvasEvent event) {
        switch (event.getType()) {
            case POSITION_CHANGED:
                // common process on position change
                positionChanged(event.getDim());
                break;

            case SYNC_CHANGED:
                ThreadUtil.invokeLater(this::refreshLockCombo);
                break;
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        super.propertyChange(evt);

        // Canvas property "layer visible" changed ?
        if (StringUtil.equals(evt.getPropertyName(), IcyCanvas.PROPERTY_LAYERS_VISIBLE)) {
            refreshToolBar();
            updateSystemMenu();
        }
    }

    @Override
    public void pluginLoaderChanged(final PluginLoaderEvent e) {
        ThreadUtil.invokeLater(() -> {
            // refresh available canvas
            buildCanvasCombo();
            // and refresh components
            refreshCanvasCombo();
            updateToolbarComponents();
        });
    }
}
