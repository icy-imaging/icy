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

package org.bioimageanalysis.icy.gui.canvas;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.event.UpdateEventHandler;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.listener.ChangeListener;
import org.bioimageanalysis.icy.common.listener.ProgressListener;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginCanvas;
import org.bioimageanalysis.icy.gui.EventUtil;
import org.bioimageanalysis.icy.gui.GuiUtil;
import org.bioimageanalysis.icy.gui.action.CanvasActions;
import org.bioimageanalysis.icy.gui.action.GeneralActions;
import org.bioimageanalysis.icy.gui.action.RoiActions;
import org.bioimageanalysis.icy.gui.action.WindowActions;
import org.bioimageanalysis.icy.gui.canvas.CanvasLayerEvent.LayersEventType;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvasEvent.IcyCanvasEventType;
import org.bioimageanalysis.icy.gui.canvas.Layer.LayerListener;
import org.bioimageanalysis.icy.gui.viewer.*;
import org.bioimageanalysis.icy.model.OMEUtil;
import org.bioimageanalysis.icy.model.colormodel.IcyColorModel;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.lut.LUT;
import org.bioimageanalysis.icy.model.lut.LUTEvent;
import org.bioimageanalysis.icy.model.lut.LUTEvent.LUTEventType;
import org.bioimageanalysis.icy.model.lut.LUTListener;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.DimensionId;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventType;
import org.bioimageanalysis.icy.model.sequence.SequenceListener;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;

/**
 * An IcyCanvas is a basic Canvas used into the viewer. It contains a visual representation
 * of the sequence and provides some facilities as basic transformation and view
 * synchronization.<br>
 * Also IcyCanvas receives key events from Viewer when they are not consumed.<br>
 * <br>
 * By default transformations are applied in following order :<br>
 * Rotation, Translation then Scaling.<br>
 * The rotation transformation is relative to canvas center.<br>
 * <br>
 * Free feel to implement and override this design or not. <br>
 * <br>
 * (Canvas2D and Canvas3D derives from IcyCanvas)<br>
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 */
public abstract class IcyCanvas extends JPanel implements KeyListener, ViewerListener, SequenceListener, LUTListener, ChangeListener, LayerListener {
    protected class IcyCanvasImageOverlay extends Overlay {
        public IcyCanvasImageOverlay() {
            super((getSequence() == null) ? "Image" : getSequence().getName(), OverlayPriority.IMAGE_NORMAL);

            // we fix the image overlay
            canBeRemoved = false;
            readOnly = false;
        }

        @Override
        public void paint(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
            // default lazy implementation (very slow)
            if (g != null)
                g.drawImage(getCurrentImage(), null, 0, 0);
        }
    }

    /**
     * Returns all {@link PluginCanvas} plugins (plugins.kernel plugin are returned first).
     */
    public static @NotNull @Unmodifiable List<PluginDescriptor> getCanvasPlugins() {
        // get all canvas plugins
        final List<PluginDescriptor> result = new ArrayList<>(ExtensionLoader.getPlugins(PluginCanvas.class));
        if (result.isEmpty())
            return Collections.emptyList();

        // VTK is not loaded ?
        /*if (!Icy.isVtkLibraryLoaded()) {
            // remove VtkCanvas
            final int ind = PluginDescriptor.getIndex(result, VtkCanvasPlugin.class.getName());
            if (ind != -1)
                result.remove(ind);
        }*/

        // sort plugins list
        result.sort(new Comparator<PluginDescriptor>() {
            @Override
            public int compare(final PluginDescriptor o1, final PluginDescriptor o2) {
                //return Integer.valueOf(getOrder(o1)).compareTo(Integer.valueOf(getOrder(o2)));
                return Integer.compare(getOrder(o1), getOrder(o2));
            }

            int getOrder(final PluginDescriptor p) {
                if (p.getClassName().endsWith(".Canvas2DPlugin"))
                    return 0;
                if (p.getClassName().endsWith(".VtkCanvasPlugin"))
                    return 1;

                return 10;
            }
        });

        return List.copyOf(result);
    }

    /**
     * Returns all {@link PluginCanvas} plugins class name (plugins.kernel plugin are returned first).
     */
    public static List<String> getCanvasPluginNames() {
        // get all canvas plugins
        final List<PluginDescriptor> plugins = getCanvasPlugins();
        final List<String> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins)
            result.add(plugin.getClassName());

        return result;
    }

    /**
     * Returns the plugin class name corresponding to the specified Canvas class name.<br>
     * Returns <code>null</code> if we can't find a corresponding plugin.
     */
    public static String getPluginClassName(final String canvasClassName) {
        for (final PluginDescriptor plugin : IcyCanvas.getCanvasPlugins()) {
            final String className = getCanvasClassName(plugin);

            // we found the corresponding plugin
            if (canvasClassName.equals(className))
                // get plugin class name
                return plugin.getClassName();
        }

        return null;
    }

    /**
     * Returns the canvas class name corresponding to the specified {@link PluginCanvas} plugin.<br>
     * Returns <code>null</code> if we can't retrieve the corresponding canvas class name.
     */
    public static String getCanvasClassName(final PluginDescriptor plugin) {
        try {
            if (plugin != null) {
                //final PluginCanvas pluginCanvas = (PluginCanvas) plugin.getPluginClass().newInstance();
                final PluginCanvas<? extends IcyCanvas> pluginCanvas = (PluginCanvas<? extends IcyCanvas>) plugin.getPluginClass().getDeclaredConstructor().newInstance();
                // return canvas class name
                return pluginCanvas.getCanvasClassName();
            }
        }
        catch (final Exception e) {
            IcyLogger.error(IcyCanvas.class, e, "Unable to start plugin canvas: " + plugin.getName());
        }

        return null;
    }

    /**
     * Returns the canvas class name corresponding to the specified {@link PluginCanvas} class name. <br>
     * Returns <code>null</code> if we can't find retrieve the corresponding canvas class name.
     */
    public static String getCanvasClassName(final String pluginClassName) {
        return getCanvasClassName(ExtensionLoader.getPlugin(pluginClassName));
    }

    /**
     * Create a {@link IcyCanvas} object from its class name or {@link PluginCanvas} class name.<br>
     * Throws an exception if an error occurred (canvas class was not found or it could not be
     * creatd).
     *
     * @param viewer {@link Viewer} to which to canvas is attached.
     * @throws ClassCastException if the specified class name is not a canvas plugin or canvas class name
     * @throws Exception          if the specified canvas cannot be created for some reasons
     */
    @SuppressWarnings("unchecked")
    public static IcyCanvas create(final String className, final Viewer viewer) throws ClassCastException, Exception {
        // search for the specified className
        final Class<?> clazz = ClassUtil.findClass(className);
        final Class<? extends PluginCanvas<? extends IcyCanvas>> pluginCanvasClazz;

        try {
            // we first check if we have a IcyCanvas Plugin class here
            pluginCanvasClazz = (Class<? extends PluginCanvas<? extends IcyCanvas>>) clazz.asSubclass(PluginCanvas.class);
        }
        catch (final ClassCastException e0) {
            // check if this is a IcyCanvas class
            final Class<? extends IcyCanvas> canvasClazz = clazz.asSubclass(IcyCanvas.class);

            // get constructor (Viewer)
            final Constructor<? extends IcyCanvas> constructor = canvasClazz.getConstructor(Viewer.class);
            // build canvas
            return constructor.newInstance(viewer);
        }

        // create canvas from plugin
        //return pluginCanvasClazz.newInstance().createCanvas(viewer);
        return pluginCanvasClazz.getDeclaredConstructor().newInstance().createCanvas(viewer);
    }

    public static void addVisibleLayerToList(final Layer layer, final ArrayList<Layer> list) {
        if ((layer != null) && (layer.isVisible()))
            list.add(layer);
    }

    public static final String PROPERTY_LAYERS_VISIBLE = "layersVisible";

    /**
     * Navigations bar
     */
    final protected ZNavigationPanel zNav;
    final protected TNavigationPanel tNav;

    /**
     * The panel where mouse informations are displayed
     */
    protected final MouseImageInfosPanel mouseInfPanel;

    /**
     * The panel contains all settings and informations data such as<br>
     * scale factor, rendering mode...
     * Will be retrieved by the inspector to get information on the current canvas.
     */
    protected JPanel panel;

    /**
     * attached viewer
     */
    protected final Viewer viewer;
    /**
     * layers visible flag
     */
    protected boolean layersVisible;
    /**
     * synchronization group :<br>
     * 0 = unsynchronized
     * 1 = full synchronization group 1
     * 2 = full synchronization group 2
     * 3 = view synchronization group (T and Z navigation are not synchronized)
     * 4 = slice synchronization group (only T and Z navigation are synchronized)
     */
    protected int syncId;

    /**
     * Overlay/Layer used to display sequence image
     */
    protected final Overlay imageOverlay;
    protected final Layer imageLayer;

    /**
     * Layers attached to canvas<br>
     * There are representing sequence overlays with some visualization properties
     */
    protected final Map<Overlay, Layer> layers;
    /**
     * Priority ordered layers.
     */
    protected final List<Layer> orderedLayers;

    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;
    /**
     * listeners
     */
    protected final List<IcyCanvasListener> listeners = new ArrayList<>();
    protected final List<CanvasLayerListener> layerListeners;

    /**
     * Current X position (should be -1 when canvas handle multi X dimension view).
     */
    protected int posX;
    /**
     * Current Y position (should be -1 when canvas handle multi Y dimension view).
     */
    protected int posY;
    /**
     * Current Z position (should be -1 when canvas handle multi Z dimension view).
     */
    protected int posZ;
    /**
     * Current T position (should be -1 when canvas handle multi T dimension view).
     */
    protected int posT;
    /**
     * Current C position (should be -1 when canvas handle multi C dimension view).
     */
    protected int posC;

    /**
     * Current mouse position (canvas coordinate space)
     */
    protected Point mousePos;

    /**
     * internals
     */
    protected LUT lut;
    protected boolean synchMaster;
    protected boolean orderedLayersOutdated;
    private final Runnable guiUpdater;
    protected boolean isLoopingT;

    /**
     * Constructor
     */
    public IcyCanvas(final Viewer viewer) {
        super();

        // default
        this.viewer = viewer;

        layersVisible = true;
        layers = new HashMap<>();
        orderedLayers = new ArrayList<>();
        syncId = 0;
        synchMaster = false;
        orderedLayersOutdated = false;
        updater = new UpdateEventHandler(this, false);

        // default position
        mousePos = new Point(0, 0);
        posX = -1;
        posY = -1;
        posZ = -1;
        posT = -1;
        posC = -1;

        // GUI stuff
        panel = new JPanel();

        layerListeners = new ArrayList<>();

        // Z navigation
        zNav = new ZNavigationPanel();
        zNav.addChangeListener(e -> {
            // set the new Z position
            setPositionZ(zNav.getValue());
        });

        // T navigation
        tNav = new TNavigationPanel();
        tNav.addChangeListener(e -> {
            // set the new T position
            setPositionT(tNav.getValue());
        });
        tNav.addLoopingStateChangeListener(e -> isLoopingT = tNav.isRepeat());

        isLoopingT = tNav.isRepeat();

        // mouse info panel
        mouseInfPanel = new MouseImageInfosPanel();

        // default canvas layout
        setLayout(new BorderLayout());

        add(zNav, BorderLayout.WEST);
        add(GuiUtil.createPageBoxPanel(tNav, mouseInfPanel), BorderLayout.SOUTH);

        // asynchronous updater for GUI
        guiUpdater = () -> ThreadUtil.invokeNow(() -> {
            // update sliders bounds if needed
            updateZNav();
            updateTNav();

            // adjust X position if needed
            final int maxX = getMaxPositionX();
            final int curX = getPositionX();
            if ((curX != -1) && (curX > maxX))
                setPositionX(maxX);

            // adjust Y position if needed
            final int maxY = getMaxPositionY();
            final int curY = getPositionY();
            if ((curY != -1) && (curY > maxY))
                setPositionY(maxY);

            // adjust C position if needed
            final int maxC = getMaxPositionC();
            final int curC = getPositionC();
            if ((curC != -1) && (curC > maxC))
                setPositionC(maxC);

            // adjust Z position if needed
            final int maxZ = getMaxPositionZ();
            final int curZ = getPositionZ();
            if ((curZ != -1) && (curZ > maxZ))
                setPositionZ(maxZ);

            // adjust T position if needed
            final int maxT = getMaxPositionT();
            final int curT = getPositionT();
            if ((curT != -1) && (curT > maxT))
                setPositionT(maxT);

            // refresh mouse panel informations (data values can have changed)
            mouseInfPanel.updateInfos(IcyCanvas.this);
        });

        // create image overlay
        imageOverlay = createImageOverlay();

        // create layers from overlays
        beginUpdate();
        try {
            // first add image layer
            imageLayer = addLayer(getImageOverlay());

            final Sequence sequence = getSequence();

            if (sequence != null) {
                // then add sequence overlays to layer list
                for (final Overlay overlay : sequence.getOverlays())
                    addLayer(overlay);
            }
            else
                IcyLogger.error(IcyCanvas.class, "Sequence null when canvas created.");
        }
        finally {
            endUpdate();
        }

        // add listeners
        viewer.addListener(this);
        final Sequence seq = getSequence();
        if (seq != null)
            seq.addListener(this);

        // set lut (no event wanted here)
        lut = null;
        setLut(viewer.getLut(), false);
    }

    /**
     * Called by the viewer when canvas is closed to release some resources.<br>
     * Be careful to not restore previous state here (as the colormap) because generally <code>shutdown</code> is called
     * <b>after</b> the creation of the other canvas.
     */
    public void shutDown() {
        // remove navigation panel listener
        zNav.removeAllChangeListener();
        tNav.removeAllChangeListener();

        // remove listeners
        if (lut != null)
            lut.removeListener(this);
        final Sequence seq = getSequence();
        if (seq != null)
            seq.removeListener(this);
        viewer.removeListener(this);

        // remove all layers
        beginUpdate();
        try {
            for (final Layer layer : getLayers())
                removeLayer(layer);
        }
        finally {
            endUpdate();
        }

        // release layers
        synchronized (orderedLayers) {
            orderedLayers.clear();
        }

        // remove all IcyCanvas & Layer listeners
        synchronized (listeners) {
            listeners.clear();
        }
        synchronized (layerListeners) {
            layerListeners.clear();
        }
    }

    /**
     * Force canvas refresh
     */
    public abstract void refresh();

    protected Overlay createImageOverlay() {
        // default image overlay
        return new IcyCanvasImageOverlay();
    }

    /**
     * Returns the {@link Overlay} used to display the current sequence image
     */
    public Overlay getImageOverlay() {
        return imageOverlay;
    }

    /**
     * Returns the {@link Layer} object used to display the current sequence image
     */
    public Layer getImageLayer() {
        return imageLayer;
    }

    /**
     * Return true if layers are visible on the canvas.
     */
    public boolean isLayersVisible() {
        return layersVisible;
    }

    public boolean isLoopingInT() {
        return isLoopingT;
    }

    /**
     * Make layers visible on this canvas (default = true).
     */
    public void setLayersVisible(final boolean value) {
        if (layersVisible != value) {
            layersVisible = value;
            layersVisibleChanged();
            firePropertyChange(PROPERTY_LAYERS_VISIBLE, !value, value);
        }
    }

    /**
     * Global layers visibility changed
     */
    protected void layersVisibleChanged() {
        final Component comp = getViewComponent();

        if (comp != null)
            comp.repaint();
    }

    /**
     * @return the viewer
     */
    public Viewer getViewer() {
        return viewer;
    }

    /**
     * @return the sequence
     */
    public Sequence getSequence() {
        return viewer.getSequence();
    }

    /**
     * @return the main view component
     */
    public abstract Component getViewComponent();

    /**
     * @return the Z navigation bar panel
     */
    public ZNavigationPanel getZNavigationPanel() {
        return zNav;
    }

    /**
     * @return the T navigation bar panel
     */
    public TNavigationPanel getTNavigationPanel() {
        return tNav;
    }

    /**
     * @return the mouse image informations panel
     */
    public MouseImageInfosPanel getMouseImageInfosPanel() {
        return mouseInfPanel;
    }

    /**
     * @return the LUT
     */
    public LUT getLut() {
        // ensure we have the good lut
        setLut(viewer.getLut(), true);

        return lut;
    }

    /**
     * set canvas LUT
     */
    private void setLut(final LUT lut, final boolean event) {
        if (this.lut != lut) {
            if (this.lut != null)
                this.lut.removeListener(this);

            this.lut = lut;

            // add listener to the new lut
            if (lut != null)
                lut.addListener(this);

            // launch a lutChanged event if wanted
            if (event)
                lutChanged(new LUTEvent(lut, -1, LUTEventType.COLORMAP_CHANGED));
        }
    }

    /**
     * Called by the parent viewer when building the toolbar.<br>
     * This way the canvas can customize it by adding specific command for instance.<br>
     *
     * @param toolBar the parent toolbar to customize
     */
    public abstract void customizeToolbar(final JToolBar toolBar);

    /**
     * Returns the setting panel of this canvas.<br>
     * The setting panel is displayed in the inspector so user can change canvas parameters.
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Returns all layers attached to this canvas.<br>
     *
     * @param sorted If <code>true</code> the returned list is sorted on the layer priority.<br>
     *               Sort operation is cached so the method could take sometime when sort cache need to be
     *               rebuild.
     */
    public List<Layer> getLayers(final boolean sorted) {
        if (sorted) {
            // need to rebuild sorted layer list ?
            if (orderedLayersOutdated) {
                // build and sort the list
                synchronized (layers) {
                    orderedLayers.clear();
                    orderedLayers.addAll(layers.values());
                    //orderedLayers = new ArrayList<Layer>(layers.values());
                }

                try {
                    Collections.sort(orderedLayers);
                }
                catch (final Exception e) {
                    // catch exceptions here as some we can have "IllegalArgumentException: Comparison method violates
                    // its general contract!"
                }

                orderedLayersOutdated = false;
            }

            return new ArrayList<>(orderedLayers);
        }

        synchronized (layers) {
            return new ArrayList<>(layers.values());
        }
    }

    /**
     * Returns all layers attached to this canvas.<br>
     * The returned list is sorted on the layer priority.<br>
     * Sort operation is cached so the method could take sometime when cache need to be rebuild.
     */
    public List<Layer> getLayers() {
        return getLayers(true);
    }

    /**
     * Returns all visible layers (visible property set to <code>true</code>) attached to this
     * canvas.
     *
     * @param sorted If <code>true</code> the returned list is sorted on the layer priority.<br>
     *               Sort operation is cached so the method could take sometime when sort cache need to be
     *               rebuild.
     */
    public List<Layer> getVisibleLayers(final boolean sorted) {
        final List<Layer> olayers = getLayers(sorted);
        final List<Layer> result = new ArrayList<>(olayers.size());

        for (final Layer l : olayers)
            if (l.isVisible())
                result.add(l);

        return result;
    }

    /**
     * Returns all visible layers (visible property set to <code>true</code>) attached to this
     * canvas.<br>
     * The list is sorted on the layer priority.
     */
    public ArrayList<Layer> getVisibleLayers() {
        return (ArrayList<Layer>) getVisibleLayers(true);
    }

    /**
     * Directly returns a {@link Set} of all Overlay displayed by this canvas.
     */
    public Set<Overlay> getOverlays() {
        synchronized (layers) {
            return new HashSet<>(layers.keySet());
        }
    }

    /**
     * @return the SyncId
     */
    public int getSyncId() {
        return syncId;
    }

    /**
     * Set the synchronization group id (0 means unsynchronized).<br>
     *
     * @param id the syncId to set
     * @return <code>false</code> if the canvas do not support synchronization group.
     */
    public boolean setSyncId(final int id) {
        if (!isSynchronizationSupported())
            return false;

        if (this.syncId != id) {
            this.syncId = id;

            // notify sync has changed
            updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.SYNC_CHANGED));
        }

        return true;
    }

    /**
     * Return true if this canvas support synchronization
     */
    public boolean isSynchronizationSupported() {
        // default (override it when supported)
        return false;
    }

    /**
     * Return true if this canvas is synchronized
     */
    public boolean isSynchronized() {
        return syncId > 0;
    }

    /**
     * Return true if current canvas is synchronized and is currently the synchronize leader.
     */
    public boolean isSynchMaster() {
        return synchMaster;
    }

    /**
     * Return true if current canvas is synchronized and it's not the synchronize master
     */
    public boolean isSynchSlave() {
        if (isSynchronized()) {
            if (isSynchMaster())
                return false;

            // search for a master in synchronized canvas
            for (final IcyCanvas cnv : getSynchronizedCanvas())
                if (cnv.isSynchMaster())
                    return true;
        }

        return false;
    }

    /**
     * Return true if this canvas is synchronized on view (offset, zoom and rotation).
     */
    public boolean isSynchOnView() {
        return (syncId == 1) || (syncId == 2) || (syncId == 3);
    }

    /**
     * Return true if this canvas is synchronized on slice (T and Z position)
     */
    public boolean isSynchOnSlice() {
        return (syncId == 1) || (syncId == 2) || (syncId == 4);
    }

    /**
     * Return true if this canvas is synchronized on cursor (mouse cursor)
     */
    public boolean isSynchOnCursor() {
        return (syncId > 0);
    }

    /**
     * Return true if we get the synchronizer master from synchronized canvas
     */
    protected boolean getSynchMaster() {
        return getSynchMaster(getSynchronizedCanvas());
    }

    /**
     * Return true if we get the synchronizer master from specified canvas list.
     */
    protected boolean getSynchMaster(final List<IcyCanvas> canvasList) {
        for (final IcyCanvas canvas : canvasList)
            if (canvas.isSynchMaster())
                return canvas == this;

        // no master found so we are master
        synchMaster = true;

        return true;
    }

    /**
     * Release synchronizer master
     */
    protected void releaseSynchMaster() {
        synchMaster = false;
    }

    /**
     * Return the list of canvas which are synchronized with the current one
     */
    private List<IcyCanvas> getSynchronizedCanvas() {
        final List<IcyCanvas> result = new ArrayList<>();

        if (isSynchronized()) {
            final List<Viewer> viewers = Icy.getMainInterface().getViewers();

            for (int i = viewers.size() - 1; i >= 0; i--) {
                final IcyCanvas cnv = viewers.get(i).getCanvas();

                if ((cnv == this) || (cnv.getSyncId() != syncId))
                    viewers.remove(i);
            }

            for (final Viewer v : viewers) {
                final IcyCanvas cnv = v.getCanvas();

                // only permit same class
                if (cnv.getClass().isInstance(this))
                    result.add(cnv);
            }
        }

        return result;
    }

    /**
     * Synchronize views of specified list of canvas
     */
    protected void synchronizeCanvas(final List<IcyCanvas> canvasList, final IcyCanvasEvent event, final boolean processAll) {
        final IcyCanvasEventType type = event.getType();
        final DimensionId dim = event.getDim();

        // position synchronization
        if (isSynchOnSlice()) {
            if (processAll || (type == IcyCanvasEventType.POSITION_CHANGED)) {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL)) {
                    final int x = getPositionX();
                    final int y = getPositionY();
                    final int z = getPositionZ();
                    final int t = getPositionT();
                    final int c = getPositionC();

                    for (final IcyCanvas cnv : canvasList) {
                        if (x != -1)
                            cnv.setPositionX(x);
                        if (y != -1)
                            cnv.setPositionY(y);
                        if (z != -1)
                            cnv.setPositionZ(z);
                        if (t != -1)
                            cnv.setPositionT(t);
                        if (c != -1)
                            cnv.setPositionC(c);
                    }
                }
                else {
                    for (final IcyCanvas cnv : canvasList) {
                        final int pos = getPosition(dim);
                        if (pos != -1)
                            cnv.setPosition(dim, pos);
                    }
                }
            }
        }

        // view synchronization
        if (isSynchOnView()) {
            if (processAll || (type == IcyCanvasEventType.SCALE_CHANGED)) {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL)) {
                    final double sX = getScaleX();
                    final double sY = getScaleY();
                    final double sZ = getScaleZ();
                    final double sT = getScaleT();
                    final double sC = getScaleC();

                    for (final IcyCanvas cnv : canvasList) {
                        cnv.setScaleX(sX);
                        cnv.setScaleY(sY);
                        cnv.setScaleZ(sZ);
                        cnv.setScaleT(sT);
                        cnv.setScaleC(sC);
                    }
                }
                else {
                    for (final IcyCanvas cnv : canvasList)
                        cnv.setScale(dim, getScale(dim));
                }
            }

            if (processAll || (type == IcyCanvasEventType.ROTATION_CHANGED)) {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL)) {
                    final double rotX = getRotationX();
                    final double rotY = getRotationY();
                    final double rotZ = getRotationZ();
                    final double rotT = getRotationT();
                    final double rotC = getRotationC();

                    for (final IcyCanvas cnv : canvasList) {
                        cnv.setRotationX(rotX);
                        cnv.setRotationY(rotY);
                        cnv.setRotationZ(rotZ);
                        cnv.setRotationT(rotT);
                        cnv.setRotationC(rotC);
                    }
                }
                else {
                    for (final IcyCanvas cnv : canvasList)
                        cnv.setRotation(dim, getRotation(dim));
                }
            }

            // process offset in last as it can be limited depending destination scale value
            if (processAll || (type == IcyCanvasEventType.OFFSET_CHANGED)) {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL)) {
                    final int offX = getOffsetX();
                    final int offY = getOffsetY();
                    final int offZ = getOffsetZ();
                    final int offT = getOffsetT();
                    final int offC = getOffsetC();

                    for (final IcyCanvas cnv : canvasList) {
                        cnv.setOffsetX(offX);
                        cnv.setOffsetY(offY);
                        cnv.setOffsetZ(offZ);
                        cnv.setOffsetT(offT);
                        cnv.setOffsetC(offC);
                    }
                }
                else {
                    for (final IcyCanvas cnv : canvasList)
                        cnv.setOffset(dim, getOffset(dim));
                }
            }
        }

        // cursor synchronization
        if (isSynchOnCursor()) {
            // mouse synchronization
            if (processAll || (type == IcyCanvasEventType.MOUSE_IMAGE_POSITION_CHANGED)) {
                // no information about dimension --> set all
                if (processAll || (dim == DimensionId.NULL)) {
                    final double mipX = getMouseImagePosX();
                    final double mipY = getMouseImagePosY();
                    final double mipZ = getMouseImagePosZ();
                    final double mipT = getMouseImagePosT();
                    final double mipC = getMouseImagePosC();

                    for (final IcyCanvas cnv : canvasList) {
                        cnv.setMouseImagePosX(mipX);
                        cnv.setMouseImagePosY(mipY);
                        cnv.setMouseImagePosZ(mipZ);
                        cnv.setMouseImagePosT(mipT);
                        cnv.setMouseImagePosC(mipC);
                    }
                }
                else {
                    for (final IcyCanvas cnv : canvasList)
                        cnv.setMouseImagePos(dim, getMouseImagePos(dim));
                }
            }
        }
    }

    /**
     * Get position for specified dimension
     */
    public int getPosition(final DimensionId dim) {
        return switch (dim) {
            case X -> getPositionX();
            case Y -> getPositionY();
            case Z -> getPositionZ();
            case T -> getPositionT();
            case C -> getPositionC();
            default -> 0;
        };

    }

    /**
     * @return current X (-1 if all selected)
     */
    public int getPositionX() {
        return -1;
    }

    /**
     * @return current Y (-1 if all selected)
     */
    public int getPositionY() {
        return -1;
    }

    /**
     * @return current Z (-1 if all selected)
     */
    public int getPositionZ() {
        return posZ;
    }

    /**
     * @return current T (-1 if all selected)
     */
    public int getPositionT() {
        return posT;
    }

    /**
     * @return current C (-1 if all selected)
     */
    public int getPositionC() {
        return posC;
    }

    /**
     * Returns the 5D canvas position (-1 mean that the complete dimension is selected)
     */
    public Point5D.Integer getPosition5D() {
        return new Point5D.Integer(getPositionX(), getPositionY(), getPositionZ(), getPositionT(), getPositionC());
    }

    /**
     * Get maximum position for specified dimension
     */
    public double getMaxPosition(final DimensionId dim) {
        return switch (dim) {
            case X -> getMaxPositionX();
            case Y -> getMaxPositionY();
            case Z -> getMaxPositionZ();
            case T -> getMaxPositionT();
            case C -> getMaxPositionC();
            default -> 0;
        };

    }

    /**
     * Get maximum X value
     */
    public int getMaxPositionX() {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeX() - 1);
    }

    /**
     * Get maximum Y value
     */
    public int getMaxPositionY() {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeY() - 1);
    }

    /**
     * Get maximum Z value
     */
    public int getMaxPositionZ() {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeZ() - 1);
    }

    /**
     * Get maximum T value
     */
    public int getMaxPositionT() {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeT() - 1);
    }

    /**
     * Get maximum C value
     */
    public int getMaxPositionC() {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        return Math.max(0, getImageSizeC() - 1);
    }

    /**
     * Get the maximum 5D position for the canvas.
     *
     * @see #getPosition5D()
     */
    public Point5D.Integer getMaxPosition5D() {
        return new Point5D.Integer(getMaxPositionX(), getMaxPositionY(), getMaxPositionZ(), getMaxPositionT(),
                getMaxPositionC());
    }

    /**
     * Get canvas view size for specified Dimension
     */
    public int getCanvasSize(final DimensionId dim) {
        return switch (dim) {
            case X -> getCanvasSizeX();
            case Y -> getCanvasSizeY();
            case Z -> getCanvasSizeZ();
            case T -> getCanvasSizeT();
            case C -> getCanvasSizeC();
            default ->
                // size not supported
                    -1;
        };

    }

    /**
     * Returns the canvas view size X.
     */
    public int getCanvasSizeX() {
        final Component comp = getViewComponent();
        int res = 0;

        if (comp != null) {
            // by default we use view component width
            res = comp.getWidth();
            // preferred width if size not yet set
            if (res == 0)
                res = comp.getPreferredSize().width;
        }

        return res;
    }

    /**
     * Returns the canvas view size Y.
     */
    public int getCanvasSizeY() {
        final Component comp = getViewComponent();
        int res = 0;

        if (comp != null) {
            // by default we use view component width
            res = comp.getHeight();
            // preferred width if size not yet set
            if (res == 0)
                res = comp.getPreferredSize().height;
        }

        return res;
    }

    /**
     * Returns the canvas view size Z.
     */
    public int getCanvasSizeZ() {
        // by default : no Z dimension
        return 1;
    }

    /**
     * Returns the canvas view size T.
     */
    public int getCanvasSizeT() {
        // by default : no T dimension
        return 1;
    }

    /**
     * Returns the canvas view size C.
     */
    public int getCanvasSizeC() {
        // by default : no C dimension
        return 1;
    }

    /**
     * Returns the mouse position (in canvas coordinate space).
     */
    public Point getMousePos() {
        return (Point) mousePos.clone();
    }

    /**
     * Get mouse image position for specified Dimension
     */
    public double getMouseImagePos(final DimensionId dim) {
        return switch (dim) {
            case X -> getMouseImagePosX();
            case Y -> getMouseImagePosY();
            case Z -> getMouseImagePosZ();
            case T -> getMouseImagePosT();
            case C -> getMouseImagePosC();
            default -> 0;
        };

    }

    /**
     * mouse X image position
     */
    public double getMouseImagePosX() {
        // default implementation
        return getPositionX();
    }

    /**
     * mouse Y image position
     */
    public double getMouseImagePosY() {
        // default implementation
        return getPositionY();
    }

    /**
     * mouse Z image position
     */
    public double getMouseImagePosZ() {
        // default implementation
        return getPositionZ();
    }

    /**
     * mouse T image position
     */
    public double getMouseImagePosT() {
        // default implementation
        return getPositionT();
    }

    /**
     * mouse C image position
     */
    public double getMouseImagePosC() {
        // default implementation
        return getPositionC();
    }

    /**
     * Returns the 5D mouse image position
     */
    public Point5D.Double getMouseImagePos5D() {
        return new Point5D.Double(getMouseImagePosX(), getMouseImagePosY(), getMouseImagePosZ(), getMouseImagePosT(),
                getMouseImagePosC());
    }

    /**
     * Get offset for specified Dimension
     */
    public int getOffset(final DimensionId dim) {
        return switch (dim) {
            case X -> getOffsetX();
            case Y -> getOffsetY();
            case Z -> getOffsetZ();
            case T -> getOffsetT();
            case C -> getOffsetC();
            default -> 0;
        };
    }

    /**
     * X offset
     */
    public int getOffsetX() {
        return 0;
    }

    /**
     * Y offset
     */
    public int getOffsetY() {
        return 0;
    }

    /**
     * Z offset
     */
    public int getOffsetZ() {
        return 0;
    }

    /**
     * T offset
     */
    public int getOffsetT() {
        return 0;
    }

    /**
     * C offset
     */
    public int getOffsetC() {
        return 0;
    }

    /**
     * Returns the 5D offset.
     */
    public Point5D.Integer getOffset5D() {
        return new Point5D.Integer(getOffsetX(), getOffsetY(), getOffsetZ(), getOffsetT(), getOffsetC());
    }

    /**
     * Get scale factor for specified Dimension
     */
    public double getScale(final DimensionId dim) {
        return switch (dim) {
            case X -> getScaleX();
            case Y -> getScaleY();
            case Z -> getScaleZ();
            case T -> getScaleT();
            case C -> getScaleC();
            default -> 1d;
        };
    }

    /**
     * X scale factor
     */
    public double getScaleX() {
        return 1d;
    }

    /**
     * Y scale factor
     */
    public double getScaleY() {
        return 1d;
    }

    /**
     * Z scale factor
     */
    public double getScaleZ() {
        return 1d;
    }

    /**
     * T scale factor
     */
    public double getScaleT() {
        return 1d;
    }

    /**
     * C scale factor
     */
    public double getScaleC() {
        return 1d;
    }

    /**
     * Get rotation angle (radian) for specified Dimension
     */
    public double getRotation(final DimensionId dim) {
        return switch (dim) {
            case X -> getRotationX();
            case Y -> getRotationY();
            case Z -> getRotationZ();
            case T -> getRotationT();
            case C -> getRotationC();
            default -> 1d;
        };
    }

    /**
     * X rotation angle (radian)
     */
    public double getRotationX() {
        return 0d;
    }

    /**
     * Y rotation angle (radian)
     */
    public double getRotationY() {
        return 0d;
    }

    /**
     * Z rotation angle (radian)
     */
    public double getRotationZ() {
        return 0d;
    }

    /**
     * T rotation angle (radian)
     */
    public double getRotationT() {
        return 0d;
    }

    /**
     * C rotation angle (radian)
     */
    public double getRotationC() {
        return 0d;
    }

    /**
     * Get image size for specified Dimension
     */
    public int getImageSize(final DimensionId dim) {
        return switch (dim) {
            case X -> getImageSizeX();
            case Y -> getImageSizeY();
            case Z -> getImageSizeZ();
            case T -> getImageSizeT();
            case C -> getImageSizeC();
            default -> 0;
        };
    }

    /**
     * Get image size X
     */
    public int getImageSizeX() {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeX();

        return 0;
    }

    /**
     * Get image size Y
     */
    public int getImageSizeY() {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeY();

        return 0;
    }

    /**
     * Get image size Z
     */
    public int getImageSizeZ() {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeZ();

        return 0;
    }

    /**
     * Get image size T
     */
    public int getImageSizeT() {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeT();

        return 0;
    }

    /**
     * Get image size C
     */
    public int getImageSizeC() {
        final Sequence seq = getSequence();

        if (seq != null)
            return seq.getSizeC();

        return 0;
    }

    /**
     * Set position for specified dimension
     */
    public void setPosition(final DimensionId dim, final int value) {
        switch (dim) {
            case X:
                setPositionX(value);
                break;
            case Y:
                setPositionY(value);
                break;
            case Z:
                setPositionZ(value);
                break;
            case T:
                setPositionT(value);
                break;
            case C:
                setPositionC(value);
                break;
        }
    }

    /**
     * Set X position
     */
    public void setPositionX(final int x) {
        final int adjX = Math.max(-1, Math.min(x, getMaxPositionX()));

        if (getPositionX() != adjX)
            setPositionXInternal(adjX);
    }

    /**
     * Set Y position
     */
    public void setPositionY(final int y) {
        final int adjY = Math.max(-1, Math.min(y, getMaxPositionY()));

        if (getPositionY() != adjY)
            setPositionYInternal(adjY);
    }

    /**
     * Set Z position
     */
    public void setPositionZ(final int z) {
        final int adjZ = Math.max(-1, Math.min(z, getMaxPositionZ()));

        if (getPositionZ() != adjZ)
            setPositionZInternal(adjZ);
    }

    /**
     * Set T position
     */
    public void setPositionT(final int t) {
        final int adjT = Math.max(-1, Math.min(t, getMaxPositionT()));

        if (getPositionT() != adjT)
            setPositionTInternal(adjT);
    }

    /**
     * Set C position
     */
    public void setPositionC(final int c) {
        final int adjC = Math.max(-1, Math.min(c, getMaxPositionC()));

        if (getPositionC() != adjC)
            setPositionCInternal(adjC);
    }

    /**
     * Set X position internal
     */
    protected void setPositionXInternal(final int x) {
        posX = x;
        // common process on position change
        positionChanged(DimensionId.X);
    }

    /**
     * Set Y position internal
     */
    protected void setPositionYInternal(final int y) {
        posY = y;
        // common process on position change
        positionChanged(DimensionId.Y);
    }

    /**
     * Set Z position internal
     */
    protected void setPositionZInternal(final int z) {
        posZ = z;
        // common process on position change
        positionChanged(DimensionId.Z);
    }

    /**
     * Set T position internal
     */
    protected void setPositionTInternal(final int t) {
        posT = t;
        // common process on position change
        positionChanged(DimensionId.T);
    }

    /**
     * Set C position internal
     */
    protected void setPositionCInternal(final int c) {
        posC = c;
        // common process on position change
        positionChanged(DimensionId.C);
    }

    /**
     * Set mouse position (in canvas coordinate space).<br>
     * The method returns <code>true</code> if the mouse position actually changed.
     */
    public boolean setMousePos(final int x, final int y) {
        if ((mousePos.x != x) || (mousePos.y != y)) {
            mousePos.x = x;
            mousePos.y = y;

            // mouse image position probably changed so this method should be overridden
            // to implement the correct calculation for the mouse image position change

            return true;
        }

        return false;
    }

    /**
     * Set mouse position (in canvas coordinate space)
     */
    public void setMousePos(final Point point) {
        setMousePos(point.x, point.y);
    }

    /**
     * Set mouse image position for specified dimension (required for synchronization)
     */
    public void setMouseImagePos(final DimensionId dim, final double value) {
        switch (dim) {
            case X:
                setMouseImagePosX(value);
                break;
            case Y:
                setMouseImagePosY(value);
                break;
            case Z:
                setMouseImagePosZ(value);
                break;
            case T:
                setMouseImagePosT(value);
                break;
            case C:
                setMouseImagePosC(value);
                break;
        }
    }

    /**
     * Set mouse X image position
     */
    public void setMouseImagePosX(final double value) {
        if (getMouseImagePosX() != value)
            // internal set
            setMouseImagePosXInternal(value);
    }

    /**
     * Set mouse Y image position
     */
    public void setMouseImagePosY(final double value) {
        if (getMouseImagePosY() != value)
            // internal set
            setMouseImagePosYInternal(value);
    }

    /**
     * Set mouse Z image position
     */
    public void setMouseImagePosZ(final double value) {
        if (getMouseImagePosZ() != value)
            // internal set
            setMouseImagePosZInternal(value);
    }

    /**
     * Set mouse T image position
     */
    public void setMouseImagePosT(final double value) {
        if (getMouseImagePosT() != value)
            // internal set
            setMouseImagePosTInternal(value);
    }

    /**
     * Set mouse C image position
     */
    public void setMouseImagePosC(final double value) {
        if (getMouseImagePosC() != value)
            // internal set
            setMouseImagePosCInternal(value);
    }

    /**
     * Set offset X internal
     */
    protected void setMouseImagePosXInternal(final double value) {
        // notify change
        mouseImagePositionChanged(DimensionId.X);
    }

    /**
     * Set offset Y internal
     */
    protected void setMouseImagePosYInternal(final double value) {
        // notify change
        mouseImagePositionChanged(DimensionId.Y);
    }

    /**
     * Set offset Z internal
     */
    protected void setMouseImagePosZInternal(final double value) {
        // notify change
        mouseImagePositionChanged(DimensionId.Z);
    }

    /**
     * Set offset T internal
     */
    protected void setMouseImagePosTInternal(final double value) {
        // notify change
        mouseImagePositionChanged(DimensionId.T);
    }

    /**
     * Set offset C internal
     */
    protected void setMouseImagePosCInternal(final double value) {
        // notify change
        mouseImagePositionChanged(DimensionId.C);
    }

    /**
     * Set offset for specified dimension
     */
    public void setOffset(final DimensionId dim, final int value) {
        switch (dim) {
            case X:
                setOffsetX(value);
                break;
            case Y:
                setOffsetY(value);
                break;
            case Z:
                setOffsetZ(value);
                break;
            case T:
                setOffsetT(value);
                break;
            case C:
                setOffsetC(value);
                break;
        }
    }

    /**
     * Set offset X
     */
    public void setOffsetX(final int value) {
        if (getOffsetX() != value)
            // internal set
            setOffsetXInternal(value);
    }

    /**
     * Set offset Y
     */
    public void setOffsetY(final int value) {
        if (getOffsetY() != value)
            // internal set
            setOffsetYInternal(value);
    }

    /**
     * Set offset Z
     */
    public void setOffsetZ(final int value) {
        if (getOffsetZ() != value)
            // internal set
            setOffsetZInternal(value);
    }

    /**
     * Set offset T
     */
    public void setOffsetT(final int value) {
        if (getOffsetT() != value)
            // internal set
            setOffsetTInternal(value);
    }

    /**
     * Set offset C
     */
    public void setOffsetC(final int value) {
        if (getOffsetC() != value)
            // internal set
            setOffsetCInternal(value);
    }

    /**
     * Set offset X internal
     */
    protected void setOffsetXInternal(final int value) {
        // notify change
        offsetChanged(DimensionId.X);
    }

    /**
     * Set offset Y internal
     */
    protected void setOffsetYInternal(final int value) {
        // notify change
        offsetChanged(DimensionId.Y);
    }

    /**
     * Set offset Z internal
     */
    protected void setOffsetZInternal(final int value) {
        // notify change
        offsetChanged(DimensionId.Z);
    }

    /**
     * Set offset T internal
     */
    protected void setOffsetTInternal(final int value) {
        // notify change
        offsetChanged(DimensionId.T);
    }

    /**
     * Set offset C internal
     */
    protected void setOffsetCInternal(final int value) {
        // notify change
        offsetChanged(DimensionId.C);
    }

    /**
     * Set scale factor for specified dimension
     */
    public void setScale(final DimensionId dim, final double value) {
        switch (dim) {
            case X:
                setScaleX(value);
                break;
            case Y:
                setScaleY(value);
                break;
            case Z:
                setScaleZ(value);
                break;
            case T:
                setScaleT(value);
                break;
            case C:
                setScaleC(value);
                break;
        }
    }

    /**
     * Set scale factor X
     */
    public void setScaleX(final double value) {
        if (getScaleX() != value)
            // internal set
            setScaleXInternal(value);
    }

    /**
     * Set scale factor Y
     */
    public void setScaleY(final double value) {
        if (getScaleY() != value)
            // internal set
            setScaleYInternal(value);
    }

    /**
     * Set scale factor Z
     */
    public void setScaleZ(final double value) {
        if (getScaleZ() != value)
            // internal set
            setScaleZInternal(value);
    }

    /**
     * Set scale factor T
     */
    public void setScaleT(final double value) {
        if (getScaleT() != value)
            // internal set
            setScaleTInternal(value);
    }

    /**
     * Set scale factor C
     */
    public void setScaleC(final double value) {
        if (getScaleC() != value)
            // internal set
            setScaleCInternal(value);
    }

    /**
     * Set scale factor X internal
     */
    protected void setScaleXInternal(final double value) {
        // notify change
        scaleChanged(DimensionId.X);
    }

    /**
     * Set scale factor Y internal
     */
    protected void setScaleYInternal(final double value) {
        // notify change
        scaleChanged(DimensionId.Y);
    }

    /**
     * Set scale factor Z internal
     */
    protected void setScaleZInternal(final double value) {
        // notify change
        scaleChanged(DimensionId.Z);
    }

    /**
     * Set scale factor T internal
     */
    protected void setScaleTInternal(final double value) {
        // notify change
        scaleChanged(DimensionId.T);
    }

    /**
     * Set scale factor C internal
     */
    protected void setScaleCInternal(final double value) {
        // notify change
        scaleChanged(DimensionId.C);
    }

    /**
     * Set rotation angle (radian) for specified dimension
     */
    public void setRotation(final DimensionId dim, final double value) {
        switch (dim) {
            case X:
                setRotationX(value);
                break;
            case Y:
                setRotationY(value);
                break;
            case Z:
                setRotationZ(value);
                break;
            case T:
                setRotationT(value);
                break;
            case C:
                setRotationC(value);
                break;
        }
    }

    /**
     * Set X rotation angle (radian)
     */
    public void setRotationX(final double value) {
        if (getRotationX() != value)
            // internal set
            setRotationXInternal(value);
    }

    /**
     * Set Y rotation angle (radian)
     */
    public void setRotationY(final double value) {
        if (getRotationY() != value)
            // internal set
            setRotationYInternal(value);
    }

    /**
     * Set Z rotation angle (radian)
     */
    public void setRotationZ(final double value) {
        if (getRotationZ() != value)
            // internal set
            setRotationZInternal(value);
    }

    /**
     * Set T rotation angle (radian)
     */
    public void setRotationT(final double value) {
        if (getRotationT() != value)
            // internal set
            setRotationTInternal(value);
    }

    /**
     * Set C rotation angle (radian)
     */
    public void setRotationC(final double value) {
        if (getRotationC() != value)
            // internal set
            setRotationCInternal(value);
    }

    /**
     * Set X rotation angle internal
     */
    protected void setRotationXInternal(final double value) {
        // notify change
        rotationChanged(DimensionId.X);
    }

    /**
     * Set Y rotation angle internal
     */
    protected void setRotationYInternal(final double value) {
        // notify change
        rotationChanged(DimensionId.Y);
    }

    /**
     * Set Z rotation angle internal
     */
    protected void setRotationZInternal(final double value) {
        // notify change
        rotationChanged(DimensionId.Z);
    }

    /**
     * Set T rotation angle internal
     */
    protected void setRotationTInternal(final double value) {
        // notify change
        rotationChanged(DimensionId.T);
    }

    /**
     * Set C rotation angle internal
     */
    protected void setRotationCInternal(final double value) {
        // notify change
        rotationChanged(DimensionId.C);
    }

    /**
     * Called when mouse image position changed
     */
    public void mouseImagePositionChanged(final DimensionId dim) {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.MOUSE_IMAGE_POSITION_CHANGED, dim));
    }

    /**
     * Called when canvas offset changed
     */
    public void offsetChanged(final DimensionId dim) {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.OFFSET_CHANGED, dim));
    }

    /**
     * Called when scale factor changed
     */
    public void scaleChanged(final DimensionId dim) {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.SCALE_CHANGED, dim));
    }

    /**
     * Called when rotation angle changed
     */
    public void rotationChanged(final DimensionId dim) {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.ROTATION_CHANGED, dim));
    }

    /**
     * Convert specified canvas delta X to image delta X.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaX(final int value) {
        return value / getScaleX();
    }

    /**
     * Convert specified canvas delta Y to image delta Y.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaY(final int value) {
        return value / getScaleY();
    }

    /**
     * Convert specified canvas delta Z to image delta Z.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaZ(final int value) {
        return value / getScaleZ();
    }

    /**
     * Convert specified canvas delta T to image delta T.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaT(final int value) {
        return value / getScaleT();
    }

    /**
     * Convert specified canvas delta C to image delta C.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageDeltaC(final int value) {
        return value / getScaleC();
    }

    /**
     * Convert specified canvas delta X to log image delta X.<br>
     * The conversion is still affected by zoom ratio but with specified logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaX(final int value, final double logFactor) {
        final double scaleFactor = getScaleX();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert specified canvas delta X to log image delta X.<br>
     * The conversion is still affected by zoom ratio but with logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaX(final int value) {
        return canvasToImageLogDeltaX(value, 5d);
    }

    /**
     * Convert specified canvas delta Y to log image delta Y.<br>
     * The conversion is still affected by zoom ratio but with specified logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaY(final int value, final double logFactor) {
        final double scaleFactor = getScaleY();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert specified canvas delta Y to log image delta Y.<br>
     * The conversion is still affected by zoom ratio but with logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaY(final int value) {
        return canvasToImageLogDeltaY(value, 5d);
    }

    /**
     * Convert specified canvas delta Z to log image delta Z.<br>
     * The conversion is still affected by zoom ratio but with specified logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaZ(final int value, final double logFactor) {
        final double scaleFactor = getScaleZ();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert specified canvas delta Z to log image delta Z.<br>
     * The conversion is still affected by zoom ratio but with logarithm form.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.canvasToImageLogDelta(...) method instead for rotation transformed delta.
     */
    public double canvasToImageLogDeltaZ(final int value) {
        return canvasToImageLogDeltaZ(value, 5d);
    }

    /**
     * Convert specified image delta X to canvas delta X.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaX(final double value) {
        return (int) (value * getScaleX());
    }

    /**
     * Convert specified image delta Y to canvas delta Y.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaY(final double value) {
        return (int) (value * getScaleY());
    }

    /**
     * Convert specified image delta Z to canvas delta Z.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaZ(final double value) {
        return (int) (value * getScaleZ());
    }

    /**
     * Convert specified image delta T to canvas delta T.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaT(final double value) {
        return (int) (value * getScaleT());
    }

    /**
     * Convert specified image delta C to canvas delta C.<br>
     * WARNING: Does not take in account the rotation transformation.<br>
     * Use the IcyCanvasXD.imageToCanvasDelta(...) method instead for rotation transformed delta.
     */
    public int imageToCanvasDeltaC(final double value) {
        return (int) (value * getScaleC());
    }

    /**
     * Helper to forward mouse press event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mousePressed(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mousePressed(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse press event to the overlays.
     *
     * @param event original mouse event
     */
    public void mousePressed(final MouseEvent event) {
        mousePressed(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse release event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseReleased(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseReleased(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse release event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseReleased(final MouseEvent event) {
        mouseReleased(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse click event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseClick(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseClick(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse click event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseClick(final MouseEvent event) {
        mouseClick(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse move event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseMove(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseMove(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse mouse event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseMove(final MouseEvent event) {
        mouseMove(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse drag event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseDrag(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseDrag(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse drag event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseDrag(final MouseEvent event) {
        mouseDrag(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse enter event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseEntered(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseEntered(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse entered event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseEntered(final MouseEvent event) {
        mouseEntered(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse exit event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseExited(final MouseEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseExited(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse exited event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseExited(final MouseEvent event) {
        mouseExited(event, getMouseImagePos5D());
    }

    /**
     * Helper to forward mouse wheel event to the overlays.
     *
     * @param event original mouse event
     * @param pt    mouse image position
     */
    public void mouseWheelMoved(final MouseWheelEvent event, final Point5D.Double pt) {
        final boolean globalVisible = isLayersVisible();

        // send mouse event to overlays after so mouse canvas position is ok
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveMouseEventOnHidden())
                layer.getOverlay().mouseWheelMoved(event, pt, this);
        }
    }

    /**
     * Helper to forward mouse wheel event to the overlays.
     *
     * @param event original mouse event
     */
    public void mouseWheelMoved(final MouseWheelEvent event) {
        mouseWheelMoved(event, getMouseImagePos5D());
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        final boolean globalVisible = isLayersVisible();
        final Point5D.Double pt = getMouseImagePos5D();

        // forward event to overlays
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveKeyEventOnHidden())
                layer.getOverlay().keyPressed(e, pt, this);
        }

        if (!e.isConsumed()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_0:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (CanvasActions.globalDisableSyncAction.isEnabled()) {
                            CanvasActions.globalDisableSyncAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e)) {
                        if (CanvasActions.disableSyncAction.isEnabled()) {
                            CanvasActions.disableSyncAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_1:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (CanvasActions.globalSyncGroup1Action.isEnabled()) {
                            CanvasActions.globalSyncGroup1Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e)) {
                        if (CanvasActions.syncGroup1Action.isEnabled()) {
                            CanvasActions.syncGroup1Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_2:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (CanvasActions.globalSyncGroup2Action.isEnabled()) {
                            CanvasActions.globalSyncGroup2Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e)) {
                        if (CanvasActions.syncGroup2Action.isEnabled()) {
                            CanvasActions.syncGroup2Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_3:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (CanvasActions.globalSyncGroup3Action.isEnabled()) {
                            CanvasActions.globalSyncGroup3Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e)) {
                        if (CanvasActions.syncGroup3Action.isEnabled()) {
                            CanvasActions.syncGroup3Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_4:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (CanvasActions.globalSyncGroup4Action.isEnabled()) {
                            CanvasActions.globalSyncGroup4Action.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isNoModifier(e)) {
                        if (CanvasActions.syncGroup4Action.isEnabled()) {
                            CanvasActions.syncGroup4Action.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_G:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (WindowActions.gridTileAction.isEnabled()) {
                            WindowActions.gridTileAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_H:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (WindowActions.horizontalTileAction.isEnabled()) {
                            WindowActions.horizontalTileAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_A:
                    if (EventUtil.isMenuControlDown(e, true)) {
                        if (RoiActions.selectAllAction.isEnabled()) {
                            RoiActions.selectAllAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_V:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (WindowActions.verticalTileAction.isEnabled()) {
                            WindowActions.verticalTileAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isMenuControlDown(e, true)) {
                        if (GeneralActions.pasteImageAction.isEnabled()) {
                            GeneralActions.pasteImageAction.execute();
                            e.consume();
                        }
                        else if (RoiActions.pasteAction.isEnabled()) {
                            RoiActions.pasteAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isAltDown(e, true)) {
                        if (RoiActions.pasteLinkAction.isEnabled()) {
                            RoiActions.pasteLinkAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_C:
                    if (EventUtil.isMenuControlDown(e, true)) {
                        // do this one first else copyImage hide it
                        if (RoiActions.copyAction.isEnabled()) {
                            // copy them to icy clipboard
                            RoiActions.copyAction.execute();
                            e.consume();
                        }
                        else if (GeneralActions.copyImageAction.isEnabled()) {
                            // copy image to system clipboard
                            GeneralActions.copyImageAction.execute();
                            e.consume();
                        }
                    }
                    else if (EventUtil.isAltDown(e, true)) {
                        if (RoiActions.copyLinkAction.isEnabled()) {
                            // copy link of selected ROI to clipboard
                            RoiActions.copyLinkAction.execute();
                            e.consume();
                        }
                    }
                    break;

                // global layer
                case KeyEvent.VK_L:
                    if (EventUtil.isShiftDown(e, true)) {
                        if (CanvasActions.globalToggleLayersAction.isEnabled()) {
                            CanvasActions.globalToggleLayersAction.execute();
                            e.consume();
                        }
                    }
                    break;

                case KeyEvent.VK_SPACE:
                    if (tNav.isPlaying())
                        tNav.stopPlay();
                    else
                        tNav.startPlay();
                    e.consume();
                    break;
            }
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        final boolean globalVisible = isLayersVisible();
        final Point5D.Double pt = getMouseImagePos5D();

        // forward event to overlays
        for (final Layer layer : getLayers(true)) {
            if ((globalVisible && layer.isVisible()) || layer.getReceiveKeyEventOnHidden())
                layer.getOverlay().keyReleased(e, pt, this);
        }
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        // nothing to do by default
    }

    /**
     * Gets the image at position (t, z, c).
     */
    public IcyBufferedImage getImage(final int t, final int z, final int c) {
        if ((t == -1) || (z == -1))
            return null;

        final Sequence sequence = getSequence();

        // have to test this as sequence reference can be release in viewer
        if (sequence != null)
            return sequence.getImage(t, z, c);

        return null;
    }

    /**
     * Get the current image.
     */
    public IcyBufferedImage getCurrentImage() {
        return getImage(getPositionT(), getPositionZ(), getPositionC());
    }

    /**
     * Returns a RGB or ARGB (depending support) BufferedImage representing the canvas view for
     * image at position (t, z, c).<br>
     * Free feel to the canvas to handle or not a specific dimension.
     *
     * @param t          T position of wanted image (-1 for complete sequence)
     * @param z          Z position of wanted image (-1 for complete stack)
     * @param c          C position of wanted image (-1 for all channels)
     * @param canvasView render with canvas view if true else use default sequence dimension
     */
    public abstract BufferedImage getRenderedImage(int t, int z, int c, boolean canvasView) throws InterruptedException;

    /**
     * Return a sequence which contains rendered images.<br>
     * Default implementation, override it if needed in your canvas.
     *
     * @param canvasView       render with canvas view if true else use default sequence dimension
     * @param progressListener progress listener which receive notifications about progression
     */
    public Sequence getRenderedSequence(final boolean canvasView, final ProgressListener progressListener) throws InterruptedException {
        final Sequence seqIn = getSequence();
        // create output sequence
        final Sequence result = new Sequence();

        if (seqIn != null) {
            // derive original metadata
            result.setMetaData(OMEUtil.createOMEXMLMetadata(seqIn.getOMEXMLMetadata(), true));

            int t = getPositionT();
            int z = getPositionZ();
            int c = getPositionC();
            final int sizeT = getImageSizeT();
            final int sizeZ = getImageSizeZ();
            final int sizeC = getImageSizeC();

            int pos = 0;
            int len = 1;
            if (t != -1)
                len *= sizeT;
            if (z != -1)
                len *= sizeZ;
            if (c != -1)
                len *= sizeC;

            result.beginUpdate();
            // This cause position changed event to not be sent during rendering.
            // Painters have to take care of that, they should check the canvas position
            // in the paint() method
            beginUpdate();
            try {
                if (t != -1) {
                    for (t = 0; t < sizeT; t++) {
                        if (z != -1) {
                            for (z = 0; z < sizeZ; z++) {
                                // check for interruption
                                if (Thread.currentThread().isInterrupted())
                                    throw new InterruptedException("Canvas rendering interrupted..");

                                if (c != -1) {
                                    final List<BufferedImage> images = new ArrayList<>();

                                    for (c = 0; c < sizeC; c++) {
                                        images.add(getRenderedImage(t, z, c, canvasView));
                                        pos++;
                                        if (progressListener != null)
                                            progressListener.notifyProgress(pos, len);
                                    }

                                    result.setImage(t, z, IcyBufferedImage.createFrom(images));
                                }
                                else {
                                    result.setImage(t, z, getRenderedImage(t, z, -1, canvasView));
                                    pos++;
                                    if (progressListener != null)
                                        progressListener.notifyProgress(pos, len);
                                }
                            }
                        }
                        else {
                            result.setImage(t, 0, getRenderedImage(t, -1, -1, canvasView));
                            pos++;
                            if (progressListener != null)
                                progressListener.notifyProgress(pos, len);
                        }
                    }
                }
                else {
                    if (z != -1) {
                        for (z = 0; z < sizeZ; z++) {
                            if (c != -1) {
                                final ArrayList<BufferedImage> images = new ArrayList<>();

                                for (c = 0; c < sizeC; c++) {
                                    images.add(getRenderedImage(-1, z, c, canvasView));
                                    pos++;
                                    if (progressListener != null)
                                        progressListener.notifyProgress(pos, len);
                                }

                                result.setImage(0, z, IcyBufferedImage.createFrom(images));
                            }
                            else {
                                result.setImage(0, z, getRenderedImage(-1, z, -1, canvasView));
                                pos++;
                                if (progressListener != null)
                                    progressListener.notifyProgress(pos, len);
                            }
                        }
                    }
                    else {
                        if (c != -1) {
                            final ArrayList<BufferedImage> images = new ArrayList<>();

                            for (c = 0; c < sizeC; c++) {
                                images.add(getRenderedImage(-1, -1, c, canvasView));
                                pos++;
                                if (progressListener != null)
                                    progressListener.notifyProgress(pos, len);
                            }

                            result.setImage(0, 0, IcyBufferedImage.createFrom(images));
                        }
                        else {
                            result.setImage(0, 0, getRenderedImage(-1, -1, -1, canvasView));
                            pos++;
                            if (progressListener != null)
                                progressListener.notifyProgress(pos, len);
                        }
                    }
                }
                result.setPixelSizeX(seqIn.getPixelSizeX() / (canvasView ? getScaleX() : 1d));
                result.setPixelSizeY(seqIn.getPixelSizeY() / (canvasView ? getScaleY() : 1d));
                result.setPixelSizeZ(seqIn.getPixelSizeZ() / (canvasView ? getScaleZ() : 1d));
            }
            finally {
                endUpdate();
                result.endUpdate();
            }
        }

        return result;
    }

    /**
     * Return the number of "selected" samples
     */
    public int getNumSelectedSamples() {
        final Sequence sequence = getSequence();

        // have to test this as we release sequence reference on closed
        if (sequence == null)
            return 0;

        final int base_len = getImageSizeX() * getImageSizeY() * getImageSizeC();

        if (getPositionT() == -1) {
            if (getPositionZ() == -1)
                return base_len * getImageSizeZ() * getImageSizeT();

            return base_len * getImageSizeT();
        }

        if (getPositionZ() == -1)
            return base_len * getImageSizeZ();

        return base_len;
    }

    /**
     * Returns the frame rate (given in frame per second) for play command (T navigation panel).
     */
    public int getFrameRate() {
        return tNav.getFrameRate();
    }

    /**
     * Sets the frame rate (given in frame per second) for play command (T navigation panel).
     */
    public void setFrameRate(final int fps) {
        tNav.setFrameRate(fps);
    }

    /**
     * update Z slider state
     */
    protected void updateZNav() {
        final int maxZ = getMaxPositionZ();
        final int z = getPositionZ();

        zNav.setMaximum(maxZ);
        if (z != -1) {
            zNav.setValue(z);
            zNav.setVisible(maxZ > 0);
        }
        else
            zNav.setVisible(false);
    }

    /**
     * update T slider state
     */
    protected void updateTNav() {
        final int maxT = getMaxPositionT();
        final int t = getPositionT();

        tNav.setMaximum(maxT);
        if (t != -1) {
            tNav.setValue(t);
            tNav.setVisible(maxT > 0);
        }
        else
            tNav.setVisible(false);
    }

    /**
     * Find the layer corresponding to the specified Overlay
     */
    public Layer getLayer(final Overlay overlay) {
        return layers.get(overlay);
    }

    /**
     * Find the layer corresponding to the specified ROI (use the ROI overlay internally).
     */
    public Layer getLayer(final ROI roi) {
        return getLayer(roi.getOverlay());
    }

    /**
     * Returns true if the canvas contains a layer for the specified {@link Overlay}.
     */
    public boolean hasLayer(final Overlay overlay) {
        synchronized (layers) {
            return layers.containsKey(overlay);
        }
    }

    public boolean hasLayer(final Layer layer) {
        final Overlay overlay = layer.getOverlay();

        // faster to test from overlay
        if (overlay != null)
            return hasLayer(overlay);

        synchronized (layers) {
            return layers.containsValue(layer);
        }
    }

    public Layer addLayer(final Overlay overlay) {
        if (!hasLayer(overlay))
            return addLayer(new Layer(overlay));

        return null;
    }

    protected Layer addLayer(final Layer layer) {
        if (layer != null) {
            // listen layer
            layer.addListener(this);

            // add to list
            synchronized (layers) {
                layers.put(layer.getOverlay(), layer);
                //if (Layer.DEFAULT_NAME.equals(layer))
                if (Layer.DEFAULT_NAME.equals(layer.getName()))
                    layer.setName("layer " + layers.size());
            }

            // added
            layerAdded(layer);
        }

        return layer;
    }

    /**
     * Remove the layer for the specified {@link Overlay} from the canvas.<br>
     * Returns <code>true</code> if the method succeed.
     */
    public boolean removeLayer(final Overlay overlay) {
        final Layer layer;

        // remove from list
        synchronized (layers) {
            layer = layers.remove(overlay);
        }

        if (layer != null) {
            // stop listening layer
            layer.removeListener(this);
            // notify remove
            layerRemoved(layer);

            return true;
        }

        return false;
    }

    /**
     * Remove the specified layer from the canvas.
     */
    public void removeLayer(final Layer layer) {
        removeLayer(layer.getOverlay());
    }

    /**
     * Returns <code>true</code> if the specified overlay is visible in the canvas.<br>
     */
    public boolean isVisible(final Overlay overlay) {
        final Layer layer = getLayer(overlay);

        if (layer != null)
            return layer.isVisible();

        return false;
    }

    /**
     * Returns all canvas layer listener
     */
    public List<CanvasLayerListener> getLayerListeners() {
        synchronized (layerListeners) {
            return new ArrayList<>(layerListeners);
        }
    }

    /**
     * Add a layer listener
     */
    public void addLayerListener(final CanvasLayerListener listener) {
        synchronized (layerListeners) {
            if (listener != null)
                layerListeners.add(listener);
        }
    }

    /**
     * Remove a layer listener
     */
    public void removeLayerListener(final CanvasLayerListener listener) {
        synchronized (layerListeners) {
            layerListeners.remove(listener);
        }
    }

    protected void fireLayerChangedEvent(final CanvasLayerEvent event) {
        for (final CanvasLayerListener listener : getLayerListeners())
            listener.canvasLayerChanged(event);
    }

    /**
     * Returns all canvas listener
     */
    public List<IcyCanvasListener> getListeners() {
        synchronized (listeners) {
            return new ArrayList<>(listeners);
        }
    }

    /**
     * Add a IcyCanvas listener
     */
    public void addCanvasListener(final IcyCanvasListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a IcyCanvas listener
     */
    public void removeCanvasListener(final IcyCanvasListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected void fireCanvasChangedEvent(final IcyCanvasEvent event) {
        for (final IcyCanvasListener listener : getListeners())
            listener.canvasChanged(event);
    }

    public void beginUpdate() {
        updater.beginUpdate();
    }

    public void endUpdate() {
        updater.endUpdate();
    }

    public boolean isUpdating() {
        return updater.isUpdating();
    }

    /**
     * layer added
     */
    protected void layerAdded(final Layer layer) {
        // handle with updater
        updater.changed(new CanvasLayerEvent(layer, LayersEventType.ADDED));
    }

    /**
     * layer removed
     */
    protected void layerRemoved(final Layer layer) {
        // handle with updater
        updater.changed(new CanvasLayerEvent(layer, LayersEventType.REMOVED));
    }

    /**
     * layer has changed
     */
    @Override
    public void layerChanged(final Layer layer, final String propertyName) {
        // handle with updater
        updater.changed(new CanvasLayerEvent(layer, LayersEventType.CHANGED, propertyName));
    }

    /**
     * canvas changed (packed event).<br>
     * do global changes processing here
     */
    public void changed(final IcyCanvasEvent event) {
        final IcyCanvasEventType eventType = event.getType();

        // handle synchronized canvas
        if (isSynchronized()) {
            final List<IcyCanvas> synchCanvasList = getSynchronizedCanvas();

            // this is the synchronizer master so dispatch view changes to others canvas
            if (getSynchMaster(synchCanvasList)) {
                try {
                    // synchronize all events when the view has just been synchronized
                    final boolean synchAll = (eventType == IcyCanvasEventType.SYNC_CHANGED);
                    synchronizeCanvas(synchCanvasList, event, synchAll);
                }
                finally {
                    releaseSynchMaster();
                }
            }
        }

        switch (eventType) {
            case POSITION_CHANGED:
                final int curZ = getPositionZ();
                final int curT = getPositionT();
                final int curC = getPositionC();

                switch (event.getDim()) {
                    case Z:
                        // ensure Z slider position
                        if (curZ != -1)
                            zNav.setValue(curZ);
                        break;

                    case T:
                        // ensure T slider position
                        if (curT != -1)
                            tNav.setValue(curT);
                        break;

                    case C:
                        // single channel mode
                        final int maxC = getMaxPositionC();

                        // disabled others channels
                        for (int c = 0; c <= maxC; c++)
                            getLut().getLutChannel(c).setEnabled((curC == -1) || (curC == c));
                        break;

                    case NULL:
                        // ensure Z slider position
                        if (curZ != -1)
                            zNav.setValue(curZ);
                        // ensure T slider position
                        if (curT != -1)
                            tNav.setValue(curT);
                        break;
                }
                // refresh mouse panel informations
                mouseInfPanel.updateInfos(this);
                break;

            case MOUSE_IMAGE_POSITION_CHANGED:
                // refresh mouse panel informations
                mouseInfPanel.updateInfos(this);
                break;
        }

        // notify listeners that canvas have changed
        fireCanvasChangedEvent(event);
    }

    /**
     * layer property has changed (packed event)
     */
    protected void layerChanged(final CanvasLayerEvent event) {
        final String property = event.getProperty();

        // we need to rebuild sorted layer list
        if ((event.getType() != LayersEventType.CHANGED) || (property == null) || (Layer.PROPERTY_PRIORITY.equals(property)))
            orderedLayersOutdated = true;

        // notify listeners that layers have changed
        fireLayerChangedEvent(event);
    }

    /**
     * position has changed<br>
     *
     * @param dim define the position which has changed
     */
    protected void positionChanged(final DimensionId dim) {
        // handle with updater
        updater.changed(new IcyCanvasEvent(this, IcyCanvasEventType.POSITION_CHANGED, dim));
    }

    @Override
    public void lutChanged(final LUTEvent event) {
        final int curC = getPositionC();

        // single channel mode ?
        if (curC != -1) {
            final int channel = event.getComponent();

            // channel is enabled --> change C position
            if ((channel != -1) && getLut().getLutChannel(channel).isEnabled())
                setPositionC(channel);
            else
                // ensure we have 1 channel enable
                getLut().getLutChannel(curC).setEnabled(true);
        }

        lutChanged(event.getComponent());
    }

    /**
     * lut changed
     */
    protected void lutChanged(final int component) {
        // nothing to do by default
    }

    /**
     * sequence meta data has changed
     */
    protected void sequenceMetaChanged(final String metadataName) {
        // nothing to do by default
    }

    /**
     * sequence type has changed
     */
    protected void sequenceTypeChanged() {
        // nothing to do by default
    }

    /**
     * sequence component bounds has changed
     */
    protected void sequenceComponentBoundsChanged(final IcyColorModel colorModel, final int component) {
        // nothing to do by default
    }

    /**
     * sequence component bounds has changed
     */
    protected void sequenceColorMapChanged(final IcyColorModel colorModel, final int component) {
        // nothing to do by default
    }

    /**
     * sequence data has changed
     *
     * @param image image which has changed (null if global data changed)
     * @param type  event type
     */
    protected void sequenceDataChanged(final IcyBufferedImage image, final SequenceEventType type) {
        ThreadUtil.runSingle(guiUpdater);
    }

    /**
     * Sequence overlay has changed
     *
     * @param overlay overlay which has changed
     * @param type    event type
     */
    protected void sequenceOverlayChanged(final Overlay overlay, final SequenceEventType type) {
        switch (type) {
            case ADDED:
                addLayer(overlay);
                break;

            case REMOVED:
                removeLayer(overlay);
                break;

            case CHANGED:
                // nothing to do here
                break;
        }
    }

    /**
     * sequence roi has changed
     *
     * @param roi  roi which has changed (null if global roi changed)
     * @param type event type
     */
    protected void sequenceROIChanged(final ROI roi, final SequenceEventType type) {
        // nothing here

    }

    @Override
    public void viewerChanged(final ViewerEvent event) {
        switch (event.getType()) {
            case POSITION_CHANGED:
                // ignore this event as we are launching it
                break;

            case LUT_CHANGED:
                // set new lut
                setLut(viewer.getLut(), true);
                break;

            case CANVAS_CHANGED:
                // nothing to do
                break;
        }
    }

    @Override
    public void viewerClosed(final Viewer viewer) {
        // nothing to do here
    }

    @Override
    public final void sequenceChanged(final SequenceEvent event) {
        switch (event.getSourceType()) {
            case SEQUENCE_META:
                sequenceMetaChanged((String) event.getSource());
                break;

            case SEQUENCE_TYPE:
                sequenceTypeChanged();
                break;

            case SEQUENCE_COMPONENTBOUNDS:
                sequenceComponentBoundsChanged((IcyColorModel) event.getSource(), event.getParam());
                break;

            case SEQUENCE_COLORMAP:
                sequenceColorMapChanged((IcyColorModel) event.getSource(), event.getParam());
                break;

            case SEQUENCE_DATA:
                sequenceDataChanged((IcyBufferedImage) event.getSource(), event.getType());
                break;

            case SEQUENCE_OVERLAY:
                final Overlay overlay = (Overlay) event.getSource();

                sequenceOverlayChanged(overlay, event.getType());
                break;

            case SEQUENCE_ROI:
                sequenceROIChanged((ROI) event.getSource(), event.getType());
                break;
        }
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
        // nothing to do here
    }

    @Override
    public void onChanged(final CollapsibleEvent event) {
        if (event instanceof CanvasLayerEvent)
            layerChanged((CanvasLayerEvent) event);

        if (event instanceof IcyCanvasEvent)
            changed((IcyCanvasEvent) event);
    }
}
