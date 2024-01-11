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

import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.frame.IcyExternalFrame;
import icy.gui.inspector.InspectorPanel;
import icy.gui.menu.ApplicationMenuBar;
import icy.gui.toolbar.InspectorBar;
import icy.gui.toolbar.StatusBar;
import icy.gui.toolbar.container.StatusPanel;
import icy.gui.util.ComponentUtil;
import icy.gui.util.WindowPositionSaver;
import icy.gui.viewer.Viewer;
import icy.image.cache.ImageCache;
import icy.main.Icy;
import icy.math.HungarianAlgorithm;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.system.FileDrop;
import icy.system.FileDrop.FileDropListener;
import icy.system.SystemUtil;
import icy.type.collection.CollectionUtil;
import icy.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fab &amp; Stephane
 * @author Thomas MUSSET
 */
public class MainFrame extends JFrame {
    private static Rectangle getDefaultBounds() {
        final Rectangle r = SystemUtil.getMaximumWindowBounds();

        r.width -= 100;
        r.height -= 100;
        r.x += 50;
        r.y += 50;

        return r;
    }

    /**
     * Returns the list of external viewers.
     *
     * @param bounds         If not null only viewers visible in the specified bounds are returned.
     * @param wantNotVisible Also return not visible viewers
     * @param wantIconized   Also return iconized viewers
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static Viewer[] getExternalViewers(final Rectangle bounds, final boolean wantNotVisible, final boolean wantIconized) {
        final List<Viewer> result = new ArrayList<>();

        for (final Viewer viewer : Icy.getMainInterface().getViewers()) {
            if (viewer.isExternalized()) {
                final IcyExternalFrame externalFrame = viewer.getIcyExternalFrame();

                if ((wantNotVisible || externalFrame.isVisible())
                        && (wantIconized || !ComponentUtil.isMinimized(externalFrame))
                        && ((bounds == null) || bounds.contains(ComponentUtil.getCenter(externalFrame))))
                    result.add(viewer);
            }
        }

        return result.toArray(new Viewer[0]);
    }

    /**
     * Returns the list of internal viewers.
     *
     * @param wantNotVisible Also return not visible viewers
     * @param wantIconized   Also return iconized viewers
     */
    public static Viewer[] getExternalViewers(final boolean wantNotVisible, final boolean wantIconized) {
        return getExternalViewers(null, wantNotVisible, wantIconized);
    }

    public static final String TITLE = "Icy";

    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final String PROPERTY_DETACHEDMODE = "detachedMode";

    public static final int TILE_HORIZONTAL = 0;
    public static final int TILE_VERTICAL = 1;
    public static final int TILE_GRID = 2;

    public static final String ID_PREVIOUS_STATE = "previousState";

    //final MainRibbon mainRibbon;
    JSplitPane mainPane;
    private final JSplitPane centerPanel;
    private final IcyDesktopPane desktopPane;
    private final InspectorBar inspectorBar;
    private final StatusBar statusBar;
    @Deprecated(since = "3.0.0", forRemoval = true)
    InspectorPanel inspector;
    @Deprecated(since = "3.0.0", forRemoval = true)
    boolean detachedMode;
    @Deprecated(since = "3.0.0", forRemoval = true)
    int lastInspectorWidth;
    @Deprecated(since = "3.0.0", forRemoval = true)
    boolean inspectorWidthSet;

    // state save for detached mode
    @Deprecated(since = "3.0.0", forRemoval = true)
    private int previousHeight;
    @Deprecated(since = "3.0.0", forRemoval = true)
    private boolean previousMaximized;
    @Deprecated(since = "3.0.0", forRemoval = true)
    private boolean previousInspectorInternalized;

    // we need to keep reference on it as the object only use weak reference
    final WindowPositionSaver positionSaver;

    public MainFrame() throws HeadlessException {
        super(TITLE);

        // RibbonFrame force these properties to false
        // but this might add problems with mac OSX
        // JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        // ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);

        // FIXME : remove this when Ribbon with have fixed KeyTipLayer component
        //getRootPane().getLayeredPane().getComponent(0).setVisible(false);

        // SubstanceRibbonFrameTitlePane titlePane = (SubstanceRibbonFrameTitlePane)
        // LookAndFeelUtil.getTitlePane(this);
        // JCheckBox comp = new JCheckBox("test")
        // comp.setP
        // titlePane.add();
        //
        // "substancelaf.internal.titlePane.extraComponentKind"
        // titlePane.m

        final Rectangle defaultBounds = getDefaultBounds();

        positionSaver = new WindowPositionSaver(this, "frame/main", defaultBounds.getLocation(), defaultBounds.getSize());
        previousInspectorInternalized = positionSaver.getPreferences().getBoolean(ID_PREVIOUS_STATE, true);

        // set "always on top" state
        setAlwaysOnTop(GeneralPreferences.getAlwaysOnTop());
        // default close operation
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // build ribbon
        //mainRibbon = new MainRibbon(getRibbon());

        // set application icons
        if (!SystemUtil.isMac())
            setIconImages(ResourceUtil.getIcyIconImages());

        // set minimized state
        //getRibbon().setMinimized(GeneralPreferences.getRibbonMinimized());

        // Application menubar
        setJMenuBar(ApplicationMenuBar.getInstance());

        // main center pane (contains desktop pane)
        //centerPanel = new JPanel();
        //centerPanel.setLayout(new BorderLayout());
        centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // desktop pane
        desktopPane = new IcyDesktopPane();
        /*desktopPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final Insets insets = mainPane.getInsets();
                    final int lastLoc = mainPane.getLastDividerLocation();
                    final int currentLoc = mainPane.getDividerLocation();
                    final int maxLoc = mainPane.getWidth() - (mainPane.getDividerSize() + insets.left);

                    // just hide / unhide inspector
                    if (currentLoc != maxLoc)
                        mainPane.setDividerLocation(maxLoc);
                    else
                        mainPane.setDividerLocation(lastLoc);

                    // if (isInpectorInternalized())
                    // externalizeInspector();
                    // else
                    // internalizeInspector();
                }
            }
        });*/

        // set the desktop pane in center pane
        //centerPanel.add(desktopPane, BorderLayout.CENTER);
        centerPanel.setTopComponent(desktopPane);
        centerPanel.setBottomComponent(StatusPanel.getInstance());
        centerPanel.setResizeWeight(1);
        centerPanel.setDividerLocation(1.0d);

        // action on file drop
        final FileDropListener desktopFileDropListener = files -> Loader.load(CollectionUtil.asList(FileUtil.toPaths(files)), (files.length <= 1) && !files[0].isDirectory(), true, true);
        /*final FileDropExtListener bandFileDropListener = (evt, files) -> {
            if (getRibbon().getSelectedTask() == mainRibbon.getImageJTask())
            {
                final ImageJWrapper imageJ = mainRibbon.getImageJTask().getImageJ();
                final JPanel imageJPanel = imageJ.getSwingPanel();

                // drop point was inside ImageJ ?
                if (imageJPanel.contains(ComponentUtil.convertPoint(getRibbon(), evt.getLocation(), imageJPanel)))
                {
                    if (files.length > 0)
                    {
                        final String file = files[0].getAbsolutePath();

                        ThreadUtil.bgRun(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                IJ.open(file);
                            }
                        });
                    }

                    return;
                }
            }

            // classic file loading
            Loader.load(CollectionUtil.asList(FileUtil.toPaths(files)), false, true, true);
        };*/

        // handle file drop in desktop pane and in ribbon pane
        new FileDrop(desktopPane, BorderFactory.createLineBorder(Color.cyan.brighter(), 2), false, desktopFileDropListener);
        /*new FileDrop(getRibbon(), BorderFactory.createLineBorder(Color.blue.brighter(), 1), false,
                bandFileDropListener);*/

        // listen ribbon minimization event
        /*getRibbon().addPropertyChangeListener(JRibbon.PROPERTY_MINIMIZED, new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                final boolean value = ((Boolean) evt.getNewValue()).booleanValue();

                // pack the frame in detached mode
                if (detachedMode)
                    pack();

                // save state in preference
                GeneralPreferences.setRibbonMinimized(value);
            }
        });*/

        inspectorBar = new InspectorBar();
        statusBar = new StatusBar();

        mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPane.setLeftComponent(centerPanel);
        mainPane.setRightComponent(icy.gui.toolbar.container.InspectorPanel.getInstance());

        mainPane.setResizeWeight(1);
        mainPane.setDividerLocation(1.0d);
        mainPane.setEnabled(false);

        add(mainPane, BorderLayout.CENTER);
        add(inspectorBar, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Process init.<br>
     * Inspector is an ExternalizablePanel and requires MainFrame to be created.
     */
    public void init() {
        // inspector
        inspector = new InspectorPanel();
        inspectorWidthSet = false;

        /*addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // only need to do it at first display
                if (!inspectorWidthSet) {
                    // main frame resized --> adjust divider location so inspector keep its size.
                    // we need to use this method as getWidth() do not return immediate correct
                    // value on OSX when initial state is maximized.
                    if (inspector.isInternalized())
                        mainPane.setDividerLocation(getWidth() - lastInspectorWidth);

                    inspectorWidthSet = true;
                }

                if (detachedMode) {
                    // fix height
                    final int prefH = getPreferredSize().height;

                    if (getHeight() > prefH)
                        setSize(getWidth(), prefH);
                }
            }
        });*/

        // main pane
        mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel, icy.gui.toolbar.container.InspectorPanel.getInstance());

        mainPane.setResizeWeight(1);
        mainPane.setEnabled(false);


        // get saved inspector width
        /*lastInspectorWidth = inspector.getPreferredSize().width;
        // add the divider and border size if inspector was visible
        if (lastInspectorWidth > 16)
            lastInspectorWidth += 6 + 8;
            // just force size for collapsed (divider + minimum border)
        else
            lastInspectorWidth = 6 + 4;

        if (inspector.isInternalized()) {
            mainPane.setRightComponent(inspector);
        }
        else {
            inspector.setParent(mainPane);
        }
        mainPane.setResizeWeight(1);

        inspector.addStateListener((source, externalized) -> {
            if (externalized) {
            }
            else {
                // restore previous location
                mainPane.setDividerLocation(getWidth() - lastInspectorWidth);
            }
        });

        previousHeight = getHeight();
        previousMaximized = ComponentUtil.isMaximized(this);
        detachedMode = GeneralPreferences.getMultiWindowMode();

        // detached mode
        if (detachedMode) {
            // resize window to ribbon dimension
            if (previousMaximized)
                ComponentUtil.setMaximized(this, false);
            setSize(getWidth(), getMinimumSize().height);
        }
        else
            add(mainPane, BorderLayout.CENTER);*/

        add(mainPane, BorderLayout.CENTER);

        add(inspectorBar, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        validate();

        // initialize now some stuff that need main frame to be initialized
        //mainRibbon.init();
        // refresh title
        refreshTitle();

        setVisible(true);

        // can be done after setVisible
        //buildActionMap();
    }

    /*void buildActionMap() {
        // global input map
        buildActionMap(mainPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), mainPane.getActionMap());
    }*/

    /*private void buildActionMap(InputMap imap, ActionMap amap) {
        imap.put(GeneralActions.searchAction.getKeyStroke(), GeneralActions.searchAction.getName());
        imap.put(FileActions.openSequenceAction.getKeyStroke(), FileActions.openSequenceAction.getName());
        imap.put(FileActions.saveAsSequenceAction.getKeyStroke(), FileActions.saveAsSequenceAction.getName());
        imap.put(GeneralActions.onlineHelpAction.getKeyStroke(), GeneralActions.onlineHelpAction.getName());
        imap.put(SequenceOperationActions.undoAction.getKeyStroke(), SequenceOperationActions.undoAction.getName());
        imap.put(SequenceOperationActions.redoAction.getKeyStroke(), SequenceOperationActions.redoAction.getName());

        amap.put(GeneralActions.searchAction.getName(), GeneralActions.searchAction);
        amap.put(FileActions.openSequenceAction.getName(), FileActions.openSequenceAction);
        amap.put(FileActions.saveAsSequenceAction.getName(), FileActions.saveAsSequenceAction);
        amap.put(GeneralActions.onlineHelpAction.getName(), GeneralActions.onlineHelpAction);
        amap.put(SequenceOperationActions.undoAction.getName(), SequenceOperationActions.undoAction);
        amap.put(SequenceOperationActions.redoAction.getName(), SequenceOperationActions.redoAction);
    }*/

    /**
     * Returns the center pane, this pane contains the desktop pane.<br>
     * Feel free to add temporary top/left/right or bottom pane to it.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public JSplitPane getCenterPanel() {
        return centerPanel;
    }

    /**
     * Returns the desktopPane which contains InternalFrame.
     */
    public IcyDesktopPane getDesktopPane() {
        return desktopPane;
    }

    /**
     * Return all internal frames
     */
    public ArrayList<JInternalFrame> getInternalFrames() {
        if (desktopPane != null)
            return CollectionUtil.asArrayList(desktopPane.getAllFrames());

        return new ArrayList<>();
    }

    /**
     * @return the inspector
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public InspectorPanel getInspector() {
        return inspector;
    }

    /**
     * Return true if the main frame is in "detached" mode
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isDetachedMode() {
        return detachedMode;
    }

    /**
     * Return content pane dimension (available area in main frame).<br>
     * If the main frame is in "detached" mode this actually return the system desktop dimension.
     */
    public Dimension getDesktopSize() {
        if (detachedMode)
            return SystemUtil.getMaximumWindowBounds().getSize();

        return desktopPane.getSize();
    }

    /**
     * Return content pane width
     */
    public int getDesktopWidth() {
        return getDesktopSize().width;
    }

    /**
     * Return content pane height
     */
    public int getDesktopHeight() {
        return getDesktopSize().height;
    }

    public int getPreviousHeight() {
        return previousHeight;
    }

    public boolean getPreviousMaximized() {
        return previousMaximized;
    }

    /**
     * Returns true if the inspector is internalized in main container.<br>
     * Always returns false in detached mode.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean isInpectorInternalized() {
        return inspector.isInternalized();
    }

    /**
     * Internalize the inspector in main container.<br>
     * The method fails and returns false in detached mode.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean internalizeInspector() {
        if (inspector.isExternalized() && inspector.isInternalizationAutorized()) {
            inspector.internalize();
            return true;
        }

        return false;
    }

    /**
     * Externalize the inspector in main container.<br>
     * Returns false if the methods failed.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public boolean externalizeInspector() {
        if (inspector.isInternalized() && inspector.isExternalizationAutorized()) {
            // save diviser location
            lastInspectorWidth = getWidth() - mainPane.getDividerLocation();
            inspector.externalize();
            return true;
        }

        return false;
    }

    /**
     * Organize all frames in cascade
     */
    public void organizeCascade() {
        // all screen devices
        final GraphicsDevice[] screenDevices = SystemUtil.getLocalGraphicsEnvironment().getScreenDevices();
        // screen devices to process
        final ArrayList<GraphicsDevice> devices = new ArrayList<>();

        // detached mode ?
        if (isDetachedMode()) {
            // process all available screen for cascade organization
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    devices.add(dev);
        }
        else {
            // process desktop pane cascade organization
            desktopPane.organizeCascade();

            // we process screen where the mainFrame is not visible
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    if (!dev.getDefaultConfiguration().getBounds().contains(getLocation()))
                        devices.add(dev);
        }

        // organize frames on different screen
        for (GraphicsDevice dev : devices)
            organizeCascade(dev);
    }

    /**
     * Organize frames in cascade on the specified graphics device.
     */
    protected void organizeCascade(GraphicsDevice graphicsDevice) {
        final GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        final Rectangle bounds = graphicsConfiguration.getBounds();
        final Insets inset = getToolkit().getScreenInsets(graphicsConfiguration);

        // adjust bounds of current screen
        bounds.x += inset.left;
        bounds.y += inset.top;
        bounds.width -= inset.left + inset.right;
        bounds.height -= inset.top + inset.bottom;

        // prepare viewers to process
        final Viewer[] viewers = getExternalViewers(bounds, false, false);

        // this screen contains the main frame ?
        if (bounds.contains(getLocation())) {
            // move main frame at top
            setLocation(bounds.x, bounds.y);

            final int mainFrameW = getWidth();
            final int mainFrameH = getHeight();

            // adjust available bounds of current screen
            if (mainFrameW > mainFrameH) {
                bounds.y += mainFrameH;
                bounds.height -= mainFrameH;
            }
            else {
                bounds.x += mainFrameW;
                bounds.width -= mainFrameW;
            }
        }

        // available space
        final int w = bounds.width;
        final int h = bounds.height;

        final int xMax = bounds.x + w;
        final int yMax = bounds.y + h;

        final int fw = (int) (w * 0.6f);
        final int fh = (int) (h * 0.6f);

        int x = bounds.x + 32;
        int y = bounds.y + 32;

        for (Viewer v : viewers) {
            final IcyExternalFrame externalFrame = v.getIcyExternalFrame();

            if (externalFrame.isMaximized())
                externalFrame.setMaximized(false);
            externalFrame.setBounds(x, y, fw, fh);
            externalFrame.toFront();

            x += 30;
            y += 20;
            if ((x + fw) > xMax)
                x = bounds.x + 32;
            if ((y + fh) > yMax)
                y = bounds.y + 32;
        }
    }

    /**
     * Organize all frames in tile.<br>
     *
     * @param type tile type.<br>
     *             TILE_HORIZONTAL, TILE_VERTICAL or TILE_GRID.
     */
    public void organizeTile(int type) {
        // all screen devices
        final GraphicsDevice[] screenDevices = SystemUtil.getLocalGraphicsEnvironment().getScreenDevices();
        // screen devices to process
        final ArrayList<GraphicsDevice> devices = new ArrayList<>();

        // detached mode ?
        if (isDetachedMode()) {
            // process all available screen for cascade organization
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    devices.add(dev);
        }
        else {
            // process desktop pane tile organization
            desktopPane.organizeTile(type);

            // we process screen where the mainFrame is not visible
            for (GraphicsDevice dev : screenDevices)
                if (dev.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
                    if (!dev.getDefaultConfiguration().getBounds().contains(getLocation()))
                        devices.add(dev);
        }

        // organize frames on different screen
        for (GraphicsDevice dev : devices)
            organizeTile(dev, type);
    }

    /**
     * Organize frames in tile on the specified graphics device.
     */
    protected void organizeTile(GraphicsDevice graphicsDevice, int type) {
        final GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        final Rectangle bounds = graphicsConfiguration.getBounds();
        final Insets inset = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        // adjust bounds of current screen
        bounds.x += inset.left;
        bounds.y += inset.top;
        bounds.width -= inset.left + inset.right;
        bounds.height -= inset.top + inset.bottom;

        // prepare viewers to process
        final Viewer[] viewers = getExternalViewers(bounds, false, false);

        // this screen contains the main frame ?
        if (bounds.contains(getLocation())) {
            // move main frame at top
            setLocation(bounds.x, bounds.y);

            final int mainFrameW = getWidth();
            final int mainFrameH = getHeight();

            // adjust available bounds of current screen
            if (mainFrameW > mainFrameH) {
                bounds.y += mainFrameH;
                bounds.height -= mainFrameH;
            }
            else {
                bounds.x += mainFrameW;
                bounds.width -= mainFrameW;
            }
        }

        final int numFrames = viewers.length;

        // nothing to do
        if (numFrames == 0)
            return;

        // available space
        final int w = bounds.width;
        final int h = bounds.height;
        final int x = bounds.x;
        final int y = bounds.y;

        int numCol;
        int numLine;

        switch (type) {
            case MainFrame.TILE_HORIZONTAL:
                numCol = 1;
                numLine = numFrames;
                break;

            case MainFrame.TILE_VERTICAL:
                numCol = numFrames;
                numLine = 1;
                break;

            default:
                numCol = (int) Math.sqrt(numFrames);
                if (numFrames != (numCol * numCol))
                    numCol++;
                numLine = numFrames / numCol;
                if (numFrames > (numCol * numLine))
                    numLine++;
                break;
        }

        final double[][] framesDistances = new double[numCol * numLine][numFrames];

        final int dx = w / numCol;
        final int dy = h / numLine;
        int k = 0;

        for (int i = 0; i < numLine; i++) {
            for (int j = 0; j < numCol; j++, k++) {
                final double[] distances = framesDistances[k];
                final double fx = x + (j * dx) + (dx / 2d);
                final double fy = y + (i * dy) + (dy / 2d);

                for (int f = 0; f < numFrames; f++) {
                    final Point2D.Double center = ComponentUtil.getCenter(viewers[f].getExternalFrame());
                    distances[f] = Point2D.distanceSq(center.x, center.y, fx, fy);
                }
            }
        }

        final int[] framePos = new HungarianAlgorithm(framesDistances).resolve();

        k = 0;
        for (int i = 0; i < numLine; i++) {
            for (int j = 0; j < numCol; j++, k++) {
                final int f = framePos[k];

                if (f < numFrames) {
                    final IcyExternalFrame externalFrame = viewers[f].getIcyExternalFrame();

                    if (externalFrame.isMaximized())
                        externalFrame.setMaximized(false);
                    externalFrame.setBounds(x + (j * dx), y + (i * dy), dx, dy);
                    externalFrame.toFront();
                }
            }
        }
    }

    /**
     * Set detached window mode.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void setDetachedMode(boolean value) {
        if (detachedMode != value) {
            // detached mode
            if (value) {
                // save inspector state
                previousInspectorInternalized = inspector.isInternalized();
                // save it in preferences...
                positionSaver.getPreferences().putBoolean(ID_PREVIOUS_STATE, previousInspectorInternalized);

                // externalize inspector
                externalizeInspector();
                // no more internalization possible
                inspector.setInternalizationAutorized(false);

                // save the current height & state
                previousHeight = getHeight();
                previousMaximized = ComponentUtil.isMaximized(this);

                // hide main pane and remove maximized state
                remove(mainPane);
                ComponentUtil.setMaximized(this, false);
                // and pack the frame
                pack();
            }
            // single window mode
            else {
                // show main pane & resize window back to original dimension
                add(mainPane, BorderLayout.CENTER);
                setSize(getWidth(), previousHeight);
                if (previousMaximized)
                    ComponentUtil.setMaximized(this, true);
                // recompute layout
                validate();

                // internalization possible
                inspector.setInternalizationAutorized(true);
                // restore inspector internalization
                if (previousInspectorInternalized)
                    internalizeInspector();
            }

            detachedMode = value;

            // notify mode change
            firePropertyChange(PROPERTY_DETACHEDMODE, !value, value);
        }
    }

    /**
     * Refresh application title
     */
    public void refreshTitle() {
        final String login = GeneralPreferences.getUserLogin();
        final String userName = GeneralPreferences.getUserName();
        final String virtual = ImageCache.isEnabled() && GeneralPreferences.getVirtualMode() ? " (virtual mode)" : "";

        if (!StringUtil.isEmpty(userName))
            setTitle(TITLE + virtual + " - " + userName);
        else if (!StringUtil.isEmpty(login))
            setTitle(TITLE + virtual + " - " + login);
        else
            setTitle(TITLE + virtual);
    }

    @Override
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void reshape(int x, int y, int width, int height) {
        final Rectangle r = new Rectangle(x, y, width, height);
        final boolean detached;

        // test detached mode by using mainPane parent as resize is called inside setDetachedMode(..) and
        // detachedMode variable is not yet updated
        if (mainPane == null)
            detached = detachedMode;
        else
            detached = mainPane.getParent() == null;

        if (detached) {
            // fix height
            final int prefH = getPreferredSize().height;

            if (r.height > prefH)
                r.height = prefH;
        }

        ComponentUtil.fixPosition(this, r);

        super.reshape(r.x, r.y, r.width, r.height);
    }
}
