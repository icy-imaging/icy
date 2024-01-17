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

package icy.sequence;

import icy.common.CollapsibleEvent;
import icy.common.UpdateEventHandler;
import icy.common.exception.TooLargeArrayException;
import icy.common.listener.ChangeListener;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.file.SequenceFileGroupImporter;
import icy.gui.viewer.Viewer;
import icy.image.*;
import icy.image.colormap.IcyColorMap;
import icy.image.colormodel.IcyColorModel;
import icy.image.colormodel.IcyColorModelEvent;
import icy.image.colormodel.IcyColorModelListener;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.math.MathUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.painter.*;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.preferences.GeneralPreferences;
import icy.roi.*;
import icy.sequence.SequenceEvent.SequenceEventSourceType;
import icy.sequence.SequenceEvent.SequenceEventType;
import icy.sequence.edit.*;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.collection.CollectionUtil;
import icy.type.collection.array.Array1DUtil;
import icy.type.dimension.Dimension5D;
import icy.type.rectangle.Rectangle5D;
import icy.undo.IcyUndoManager;
import icy.undo.IcyUndoableEdit;
import icy.util.OMEUtil;
import icy.util.StringUtil;
import ome.xml.meta.OMEXMLMetadata;
import org.w3c.dom.Node;

import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * Image sequence object.<br>
 * A <code>Sequence</code> is basically a 5 dimension (XYCZT) image where :<br>
 * XY dimension = planar image<br>
 * C dimension = channel<br>
 * Z dimension = depth<br>
 * T dimension = time<br>
 * <br>
 * The XYC dimensions are bounded into the {@link IcyBufferedImage} object so <code>Sequence</code> define a list of
 * {@link IcyBufferedImage} where each image is associated to a Z and T
 * information.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */

public class Sequence implements SequenceModel, IcyColorModelListener, IcyBufferedImageListener, ChangeListener, ROIListener, OverlayListener, AutoCloseable {
    public static final String DEFAULT_NAME = "no name";

    public static final String ID_ID = "id";
    public static final String ID_NAME = "name";
    public static final String ID_POSITION_X = "positionX";
    public static final String ID_POSITION_Y = "positionY";
    public static final String ID_POSITION_Z = "positionZ";
    public static final String ID_POSITION_T = "positionT";
    public static final String ID_POSITION_T_OFFSET = "positionTOffset";
    public static final String ID_PIXEL_SIZE_X = "pixelSizeX";
    public static final String ID_PIXEL_SIZE_Y = "pixelSizeY";
    public static final String ID_PIXEL_SIZE_Z = "pixelSizeZ";
    public static final String ID_TIME_INTERVAL = "timeInterval";
    public static final String ID_CHANNEL_NAME = "channelName";
    public static final String ID_USER_NAME = "userName";
    public static final String ID_VIRTUAL = "virtual";

    public static final String PROPERTY_ID = ID_ID;
    public static final String PROPERTY_NAME = ID_NAME;
    public static final String PROPERTY_POSITION_X = ID_POSITION_X;
    public static final String PROPERTY_POSITION_Y = ID_POSITION_Y;
    public static final String PROPERTY_POSITION_Z = ID_POSITION_Z;
    public static final String PROPERTY_POSITION_T = ID_POSITION_T;
    public static final String PROPERTY_POSITION_T_OFFSET = ID_POSITION_T_OFFSET;
    public static final String PROPERTY_PIXEL_SIZE_X = ID_PIXEL_SIZE_X;
    public static final String PROPERTY_PIXEL_SIZE_Y = ID_PIXEL_SIZE_Y;
    public static final String PROPERTY_PIXEL_SIZE_Z = ID_PIXEL_SIZE_Z;
    public static final String PROPERTY_TIME_INTERVAL = ID_TIME_INTERVAL;
    public static final String PROPERTY_CHANNEL_NAME = ID_CHANNEL_NAME;
    public static final String PROPERTY_USER_NAME = ID_USER_NAME;
    public static final String PROPERTY_VIRTUAL = ID_VIRTUAL;

    /**
     * id generator
     */
    protected static int id_gen = 1;

    /**
     * volumetric images (4D [XYCZ])
     */
    protected final TreeMap<Integer, VolumetricImage> volumetricImages;
    /**
     * painters
     */
    protected final Set<Overlay> overlays;
    /**
     * ROIs
     */
    protected final Set<ROI> rois = new HashSet<>();

    /**
     * id of sequence (uniq during an Icy session)
     */
    protected final int id;
    /**
     * colorModel of sequence
     */
    protected IcyColorModel colorModel;
    /**
     * default lut for this sequence
     */
    protected LUT defaultLut;
    /**
     * user lut for this sequence (saved in metadata)
     */
    protected LUT userLut;
    /**
     * Origin filename (from/to which the sequence has been loaded/saved)<br>
     * null --&gt; no file attachment<br>
     * directory or metadata file --&gt; multiples files attachment<br>
     * image file --&gt; single file attachment
     */
    protected String filename;
    /**
     * Returns the {@link ImageProvider} used to load the sequence data.<br>
     * It can return <code>null</code> if the Sequence was not loaded from a specific resource or if it was saved in between.
     */
    protected ImageProvider imageProvider;

    /**
     * Resolution level from the original image<br>
     * 0 --&gt; full image resolution<br>
     * 1 --&gt; resolution / 2<br>
     * 2 --&gt; resolution / 4<br>
     * 3 --&gt; ...<br>
     * Default value is 0
     */
    protected int originResolution;
    /**
     * Region (X,Y) from original image if this image is a crop of the original image.<br>
     * Default value is <code>null</code> (no crop)
     */
    protected Rectangle originXYRegion;
    /**
     * Z range from original image if this image is a crop in Z of the original image.<br>
     * Default value is -1, -1 if we have the whole Z range.
     */
    protected int originZMin;
    protected int originZMax;
    /**
     * T range from original image if this image is a crop in T of the original image.<br>
     * Default value is -1, -1 if we have the whole T range.
     */
    protected int originTMin;
    protected int originTMax;
    /**
     * Channel position from original image if this image is a single channel extraction of the original image.<br>
     * Default value is -1 which mean that all channels were preserved.
     */
    protected int originChannel;

    /**
     * Extended / custom Sequence properties (saved as metadata)
     */
    protected final Map<String, String> properties;

    /**
     * Metadata
     */
    protected OMEXMLMetadata metaData;
    /**
     * automatic update of channel bounds
     */
    protected boolean autoUpdateChannelBounds;
    /**
     * persistent object to load/save data (XML format)
     */
    protected final SequencePersistent persistent;
    /**
     * undo manager
     */
    protected final IcyUndoManager undoManager;

    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;
    /**
     * listeners
     */
    protected final List<SequenceListener> listeners;
    protected final List<SequenceModelListener> modelListeners;

    /**
     * internals
     */
    protected boolean channelBoundsInvalid;

    /**
     * Creates a new empty sequence with specified meta data object and name.
     *
     * @param meta OME metadata
     * @param name string
     */
    public Sequence(final OMEXMLMetadata meta, final String name) {
        super();

        // set id
        synchronized (Sequence.class) {
            id = id_gen;
            id_gen++;
        }

        // set metadata object
        metaData = Objects.requireNonNullElseGet(meta, () -> MetaDataUtil.createMetadata(name));

        // set name
        if (!StringUtil.isEmpty(name))
            MetaDataUtil.setName(metaData, 0, name);
        else {
            // default name
            if (StringUtil.isEmpty(MetaDataUtil.getName(metaData, 0)))
                MetaDataUtil.setName(metaData, 0, DEFAULT_NAME + StringUtil.toString(id, 3));
        }
        filename = null;
        imageProvider = null;

        originResolution = 0;
        originXYRegion = null;
        originZMin = -1;
        originZMax = -1;
        originTMin = -1;
        originTMax = -1;
        originChannel = -1;
        properties = new HashMap<>();

        // default pixel size and time interval
        if (Double.isNaN(MetaDataUtil.getPixelSizeX(metaData, 0, Double.NaN)))
            MetaDataUtil.setPixelSizeX(metaData, 0, 1d);
        if (Double.isNaN(MetaDataUtil.getPixelSizeY(metaData, 0, Double.NaN)))
            MetaDataUtil.setPixelSizeY(metaData, 0, 1d);
        if (Double.isNaN(MetaDataUtil.getPixelSizeZ(metaData, 0, Double.NaN)))
            MetaDataUtil.setPixelSizeZ(metaData, 0, 1d);
        if (Double.isNaN(MetaDataUtil.getTimeInterval(metaData, 0, Double.NaN))) {
            final double ti = MetaDataUtil.getTimeIntervalFromTimePositions(metaData, 0);
            // we got something --> set it as the time interval
            if (ti != 0d)
                MetaDataUtil.setTimeInterval(metaData, 0, ti);
                // set to 1d by default
            else
                MetaDataUtil.setTimeInterval(metaData, 0, 1d);
        }

        double result = MetaDataUtil.getTimeInterval(metaData, 0, 0d);

        // not yet defined ?
        if (result == 0d) {
            result = MetaDataUtil.getTimeIntervalFromTimePositions(metaData, 0);
            // we got something --> set it as the time interval
            if (result != 0d)
                MetaDataUtil.setTimeInterval(metaData, 0, result);
        }

        volumetricImages = new TreeMap<>();
        overlays = new HashSet<>();
        persistent = new SequencePersistent(this);
        undoManager = new IcyUndoManager(this, GeneralPreferences.getHistorySize());

        updater = new UpdateEventHandler(this, false);
        listeners = new ArrayList<>();
        modelListeners = new ArrayList<>();

        // no colorModel yet
        colorModel = null;
        defaultLut = null;
        userLut = null;
        channelBoundsInvalid = false;
        // automatic update of channel bounds
        autoUpdateChannelBounds = true;
    }

    /**
     * Creates a sequence with specified name and containing the specified image
     *
     * @param image image
     * @param name  string
     */
    public Sequence(final String name, final IcyBufferedImage image) {
        this(name, (BufferedImage) image);
    }

    /**
     * Creates a sequence with specified name and containing the specified image
     *
     * @param image image
     * @param name  string
     */
    public Sequence(final String name, final BufferedImage image) {
        this(null, name);

        addImage(image);
    }

    /**
     * Creates a new empty sequence with specified metadata.
     *
     * @param meta OME metadata
     */
    public Sequence(final OMEXMLMetadata meta) {
        this(meta, null);
    }

    /**
     * Creates a sequence containing the specified image.
     *
     * @param image image
     */
    public Sequence(final IcyBufferedImage image) {
        this((BufferedImage) image);
    }

    /**
     * Creates a sequence containing the specified image.
     *
     * @param image image
     */
    public Sequence(final BufferedImage image) {
        this((OMEXMLMetadata) null, null);

        addImage(image);
    }

    /**
     * Creates an empty sequence with specified name.
     *
     * @param name string
     */
    public Sequence(final String name) {
        this(null, name);
    }

    /**
     * Creates an empty sequence.
     */
    public Sequence() {
        this((OMEXMLMetadata) null, null);
    }

    @Override
    @Deprecated(forRemoval = true)
    protected void finalize() throws Throwable {
        // cancel any pending prefetch tasks for this sequence
        SequencePrefetcher.cancel(this);

        try {
            // close image provider if needed
            if ((imageProvider != null) && (imageProvider instanceof Closeable))
                ((Closeable) imageProvider).close();
        }
        catch (final IOException e) {
            // ignore
        }

        super.finalize();
    }

    @Override
    public void close() throws Exception {
        // cancel any pending prefetch tasks for this sequence
        SequencePrefetcher.cancel(this);

        try {
            // close image provider if needed
            if ((imageProvider != null) && (imageProvider instanceof Closeable))
                ((Closeable) imageProvider).close();
        }
        catch (final IOException e) {
            // ignore
        }
    }

    /**
     * This method close all attached viewers
     */
    public void closeSequence() {
        Icy.getMainInterface().closeSequence(this);
    }

    /**
     * Called when sequence has been closed (all viewers displaying it closed).<br>
     * <i>Used internally, you should not call it this method directly !</i>
     */
    public void closed() {
        // cancel any pending prefetch tasks for this sequence
        SequencePrefetcher.cancel(this);

        // do this in background as it can take sometime
        while (!ThreadUtil.bgRun(() -> {
            // Sequence persistence enabled --> save XML
            if (GeneralPreferences.getSequencePersistence())
                saveXMLData();
        })) {
            // wait until the process execute
            ThreadUtil.sleep(10L);
        }

        // notify close
        fireClosedEvent();
    }

    /**
     * Copy data and metadata from the specified Sequence
     *
     * @param source   the source sequence to copy data from
     * @param copyName if set to <code>true</code> it will also copy the name from the source sequence
     */
    public void copyFrom(final Sequence source, final boolean copyName) throws InterruptedException {
        copyDataFrom(source);
        copyMetaDataFrom(source, copyName);
    }

    /**
     * Copy data from the specified Sequence
     *
     * @param source sequence
     */
    public void copyDataFrom(final Sequence source) throws InterruptedException {
        final int sizeT = source.getSizeT();
        final int sizeZ = source.getSizeZ();

        beginUpdate();
        try {
            removeAllImages();
            for (int t = 0; t < sizeT; t++) {
                for (int z = 0; z < sizeZ; z++) {
                    // check for interruption
                    if (Thread.interrupted())
                        throw new InterruptedException("Sequence copy data process interrupted.");

                    final IcyBufferedImage img = source.getImage(t, z);

                    if (img != null)
                        setImage(t, z, IcyBufferedImageUtil.getCopy(img));
                    else
                        source.setImage(t, z, null);
                }
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Copy metadata from the specified Sequence
     *
     * @param source   the source sequence to copy metadata from
     * @param copyName if set to <code>true</code> it will also copy the name from the source sequence
     */
    public void copyMetaDataFrom(final Sequence source, final boolean copyName) {
        // copy all metadata from source (preserve user name if we want to keep same name)
        metaData = OMEUtil.createOMEXMLMetadata(source.getOMEXMLMetadata(), !copyName);

        // restore name if needed
        if (copyName)
            setName(source.getName());

        // notify metadata changed
        metaChanged(null);
    }

    /**
     * Create a complete restore point for this sequence.
     *
     * @param name restore point name (visible in the History panel)
     * @return false if for some reason the operation failed (out of memory for instance)
     * @see #undo()
     */
    public boolean createUndoPoint(final String name) throws InterruptedException {
        try {
            undoManager.addEdit(new DefaultSequenceEdit(SequenceUtil.getCopy(this, false, false, false), this));
            return true;
        }
        catch (final InterruptedException t) {
            throw t;
        }
        catch (final Throwable t) {
            return false;
        }
    }

    /**
     * Create a restore point for sequence data.
     *
     * @param name restore point name (visible in the History panel)
     * @return false if for some reason the operation failed (out of memory for instance)
     * @see #undo()
     */
    public boolean createUndoDataPoint(final String name) throws InterruptedException {
        try {
            undoManager.addEdit(new DataSequenceEdit(SequenceUtil.getCopy(this, false, false, false), this, name));
            return true;
        }
        catch (final InterruptedException t) {
            throw t;
        }
        catch (final Throwable t) {
            return false;
        }
    }

    /**
     * Create a restore point for sequence metadata.
     *
     * @param name restore point name (visible in the History panel)
     * @return false if for some reason the operation failed (out of memory for instance)
     * @see #undo()
     */
    public boolean createUndoMetadataPoint(final String name) {
        try {
            undoManager.addEdit(new MetadataSequenceEdit(OMEUtil.createOMEXMLMetadata(metaData, false), this, name));
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }

    /**
     * Add an Undoable edit to the Sequence UndoManager
     *
     * @param edit the undoable edit to add
     * @return <code>false</code> if the operation failed
     */
    public boolean addUndoableEdit(final IcyUndoableEdit edit) {
        if (edit != null)
            return undoManager.addEdit(edit);

        return false;
    }

    /**
     * Undo to the last <i>Undoable</i> change set in the Sequence {@link UndoManager}
     *
     * @return <code>true</code> if the operation succeed
     * @see #createUndoPoint(String)
     * @see UndoManager#undo()
     */
    public boolean undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
            return true;
        }

        return false;
    }

    /**
     * Redo the next <i>Undoable</i> change set in the Sequence {@link UndoManager}
     *
     * @return <code>true</code> if the operation succeed
     * @see #createUndoPoint(String)
     * @see UndoManager#redo()
     */
    public boolean redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
            return true;
        }

        return false;
    }

    /**
     * Clear all undo operations from the {@link UndoManager}.<br>
     * You should use this method after you modified the sequence without providing any <i>undo</i>
     * support.
     */
    public void clearUndoManager() {
        getUndoManager().discardAllEdits();
    }

    protected void setColorModel(final IcyColorModel cm) {
        // remove listener
        if (colorModel != null)
            colorModel.removeListener(this);

        colorModel = cm;

        // add listener
        if (cm != null)
            cm.addListener(this);

        // sequence type changed
        typeChanged();
        // sequence component bounds changed
        componentBoundsChanged(cm, -1);
        // sequence colormap changed
        colormapChanged(cm, -1);
    }

    /**
     * @return all VolumetricImage as TreeMap (contains t position)
     */
    public TreeMap<Integer, VolumetricImage> getVolumetricImages() {
        synchronized (volumetricImages) {
            return new TreeMap<>(volumetricImages);
        }
    }

    /**
     * @return all VolumetricImage
     */
    public ArrayList<VolumetricImage> getAllVolumetricImage() {
        synchronized (volumetricImages) {
            return new ArrayList<>(volumetricImages.values());
        }
    }

    /**
     * @return first viewer attached to this sequence
     */
    public Viewer getFirstViewer() {
        return Icy.getMainInterface().getFirstViewer(this);
    }

    /**
     * @return viewers attached to this sequence
     */
    public ArrayList<Viewer> getViewers() {
        return Icy.getMainInterface().getViewers(this);
    }

    /**
     * Set the volatile state for this Sequence (see {@link IcyBufferedImage#setVolatile(boolean)}).<br>
     *
     * @param value boolean
     * @throws OutOfMemoryError              if there is not enough memory available to store image
     *                                       data when setting back to <i>non volatile</i> state
     * @throws UnsupportedOperationException if cache engine is not enabled
     */
    public void setVolatile(final boolean value) throws OutOfMemoryError, UnsupportedOperationException {
        final boolean vol = isVolatile();

        // switching from volatile to not volatile ?
        if (vol && !value) {
            try {
                // check that can open the image
                Loader.checkOpening(0, getSizeX(), getSizeY(), getSizeC(), getSizeZ(), getSizeT(), getDataType(), "");
            }
            catch (final OutOfMemoryError e) {
                // better to keep trace of that in console...
                System.err.println("Sequence.setVolatile(false) error: not enough memory to set sequence data back in memory.");

                throw new OutOfMemoryError("Sequence.setVolatile(false) error: not enough memory to set sequence data back in memory.");
            }
        }

        try {
            // change volatile state for all images
            for (final IcyBufferedImage image : getAllImage())
                if (image != null)
                    image.setVolatile(value);

            if (vol != value)
                metaChanged(ID_VIRTUAL);
        }
        catch (final OutOfMemoryError e) {
            // not enough memory to complete the operation --> restore previous state
            for (final IcyBufferedImage image : getAllImage())
                if (image != null)
                    image.setVolatile(!value);

            throw e;
        }
    }

    /**
     * Same as {@link #setVolatile(boolean)}
     *
     * @param value boolean
     * @throws OutOfMemoryError              if there is not enough memory available to store image
     *                                       data when setting back to <i>non volatile</i> state
     * @throws UnsupportedOperationException if cache engine is not initialized (error at initialization).
     */
    public void setVirtual(final boolean value) throws OutOfMemoryError, UnsupportedOperationException {
        setVolatile(value);
    }

    /**
     * @return true if this sequence contains volatile image (see {@link IcyBufferedImage#isVolatile()}).
     */
    public boolean isVolatile() {
        final IcyBufferedImage img = getFirstNonNullImage();
        if (img != null)
            return img.isVolatile();

        return false;
    }

    /**
     * @return Same as {@link #isVolatile()}
     */
    public boolean isVirtual() {
        return isVolatile();
    }

    /**
     * @return get sequence id (this id is unique during an ICY session)
     */
    public int getId() {
        return id;
    }

    /**
     * @param value Sequence name
     */
    public void setName(final String value) {
        if (!Objects.equals(getName(), value)) {
            MetaDataUtil.setName(metaData, 0, value);
            metaChanged(ID_NAME);
        }
    }

    public String getName() {
        return MetaDataUtil.getName(metaData, 0);
    }

    /**
     * Returns the origin filename for the specified image position.<br>
     * This method is useful for sequence loaded from multiple files.
     *
     * @param c int
     * @param t int
     * @param z int
     * @return the origin filename for the given image position
     */
    public String getFilename(final int t, final int z, final int c) {
        final ImageProvider importer = getImageProvider();

        // group importer ? we can retrieve the original filename
        if (importer instanceof SequenceFileGroupImporter)
            return ((SequenceFileGroupImporter) importer).getPath(z, t, c);

        return filename;
    }

    /**
     * Returns the origin filename (from/to which the sequence has been loaded/saved).<br>
     * This filename information is also used to store the XML persistent data.<br>
     * null / empty --&gt; no file attachment<br>
     * image file --&gt; single file attachment
     * directory or metadata file --&gt; multiples files attachment<br>
     *
     * @return the origin filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the origin filename (from/to which the sequence has been loaded/saved).<br>
     * When you set the filename you need to ensure that "sub part" information are correctly reset (setOriginXXX(...)
     * methods) as this filename will be used to generate the XML persistent data file name.<br>
     * null / empty --&gt; no file attachment<br>
     * image file --&gt; single file attachment
     * directory or metadata file --&gt; multiples files attachment<br>
     *
     * @param filename the filename to set
     */
    public void setFilename(final String filename) {
        if (!Objects.equals(this.filename, filename)) {
            this.filename = filename;
        }
    }

    /**
     * Returns the {@link ImageProvider} used to load the sequence data.<br>
     * It can return <code>null</code> if the Sequence was not loaded from a specific resource or if it was saved in between.<br>
     *
     * @return the {@link ImageProvider} used to load the Sequence
     */
    public ImageProvider getImageProvider() {
        return imageProvider;
    }

    /**
     * Set the {@link ImageProvider} used to load the sequence data.<br>
     * When you set the <i>ImageProvider</i> you need to ensure we can use it (should be opened for {@link SequenceIdImporter}).<br>
     * Also "sub part" informations has to be correctly set (setOriginXXX(...) methods) as we may use it to retrieve sequence data from the
     * {@link ImageProvider}.
     *
     * @param value image
     */
    public void setImageProvider(final ImageProvider value) {
        try {
            // close previous
            if ((imageProvider != null) && (imageProvider instanceof Closeable))
                ((Closeable) imageProvider).close();
        }
        catch (final IOException e) {
            // ignore
        }

        imageProvider = value;
    }

    /**
     * @param folderExt If the filename of this sequence refer a folder then we extend it with 'folderExt' to build the base name.
     * @return the output base filename.<br>
     * This function is supposed to be used internally only.
     * @see #getOutputExtension()
     */
    public String getOutputBaseName(final String folderExt) {
        String result = getFilename();

        if (StringUtil.isEmpty(result))
            return "";

        // remove some problematic character for XML file
        result = FileUtil.cleanPath(result);

        // filename reference a directory --> use "<directory>/<folderExt>"
        if (FileUtil.isDirectory(result))
            result += "/" + folderExt;
            // otherwise remove extension
        else
            result = FileUtil.setExtension(result, "");

        return result;
    }

    /**
     * @return the output filename extension (not the file extension, just extension from base name).<br>
     * The extension is based on some internals informations as serie index and resolution level.<br>
     * This function is supposed to be used internally only.
     * @see #getOutputBaseName(String)
     */
    public String getOutputExtension() {
        String result = "";

        // retrieve the serie index
        final int serieNum = getSeries();

        // multi serie image --> add a specific extension
        if (serieNum != 0)
            result += "_S" + serieNum;

        // retrieve the resolution
        final int resolution = getOriginResolution();

        // sub resolution --> add a specific extension
        if (resolution != 0)
            result += "_R" + resolution;

        // retrieve the XY region offset
        final Rectangle xyRegion = getOriginXYRegion();

        // not null --> add a specific extension
        if (xyRegion != null)
            result += "_XY(" + xyRegion.x + "," + xyRegion.y + "-" + xyRegion.width + "," + xyRegion.height + ")";

        // retrieve the Z range
        final int zMin = getOriginZMin();
        final int zMax = getOriginZMax();

        // sub Z range --> add a specific extension
        if ((zMin != -1) || (zMax != -1)) {
            if (zMin == zMax)
                result += "_Z" + zMin;
            else
                result += "_Z(" + zMin + "-" + zMax + ")";
        }

        // retrieve the T range
        final int tMin = getOriginTMin();
        final int tMax = getOriginTMax();

        // sub T range --> add a specific extension
        if ((tMin != -1) || (tMax != -1)) {
            if (tMin == tMax)
                result += "_T" + tMin;
            else
                result += "_T(" + tMin + "-" + tMax + ")";
        }

        // retrieve the original channel
        final int channel = getOriginChannel();

        // single channel extraction --> add a specific extension
        if (channel != -1)
            result += "_C" + channel;

        return result;
    }

    /**
     * @param withExtension Add the original file extension is set to <code>true</code>
     * @return Return the desired output filename for this Sequence.<br>
     * It uses the origin filename and add a specific extension depending some internals properties.
     * @see #getFilename()
     * @see #getOutputBaseName(String)
     * @see #getOutputExtension()
     */
    public String getOutputFilename(final boolean withExtension) {
        String result = getFilename();

        if (StringUtil.isEmpty(result))
            return "";

        final String ext = FileUtil.getFileExtension(result, true);

        result = getOutputBaseName(FileUtil.getFileName(result, false)) + getOutputExtension();
        if (withExtension)
            result += ext;

        return result;
    }

    /**
     * @return the resolution level from the origin image (defined by {@link #getFilename()}).<br>
     * By default it returns 0 if this sequence corresponds to the full resolution of the original image.<br>
     * 1 --&gt; original resolution / 2<br>
     * 2 --&gt; original resolution / 4<br>
     * 3 --&gt; original resolution / 8<br>
     * ...
     */
    public int getOriginResolution() {
        return originResolution;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value int
     * @see #getOriginResolution()
     */
    public void setOriginResolution(final int value) {
        originResolution = value;
    }

    /**
     * @return the region (X,Y) from original image if this image is a crop of the original image (in original image
     * resolution).<br>
     * Default value is <code>null</code> (full size).
     */
    public Rectangle getOriginXYRegion() {
        return originXYRegion;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value rectangle
     * @see #getOriginXYRegion()
     */
    public void setOriginXYRegion(final Rectangle value) {
        // better to use a copy
        if (value != null)
            originXYRegion = new Rectangle(value);
            // clear it
        else
            originXYRegion = null;
    }

    /**
     * @return the Z range minimum from original image if this image is a crop in Z of the original image.<br>
     * Default value is -1 which mean we have the whole Z range.
     */
    public int getOriginZMin() {
        return originZMin;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value int
     * @see #getOriginZMin()
     */
    public void setOriginZMin(final int value) {
        originZMin = value;
    }

    /**
     * @return the Z range maximum from original image if this image is a crop in Z of the original image.<br>
     * Default value is -1 which mean we have the whole Z range.
     */
    public int getOriginZMax() {
        return originZMax;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value int
     * @see #getOriginZMax()
     */
    public void setOriginZMax(final int value) {
        originZMax = value;
    }

    /**
     * @return the T range minimum from original image if this image is a crop in T of the original image.<br>
     * Default value is -1 which mean we have the whole T range.
     */
    public int getOriginTMin() {
        return originTMin;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value int
     * @see #getOriginTMin()
     */
    public void setOriginTMin(final int value) {
        originTMin = value;
    }

    /**
     * @return the T range maximum from original image if this image is a crop in T of the original image.<br>
     * Default value is -1 which mean we have the whole T range.
     */
    public int getOriginTMax() {
        return originTMax;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value int
     * @see #getOriginTMax()
     */
    public void setOriginTMax(final int value) {
        originTMax = value;
    }

    /**
     * @return the channel position from original image if this image is a single channel extraction of the original
     * image.<br>
     * Default value is -1 which mean that all channels were preserved.
     */
    public int getOriginChannel() {
        return originChannel;
    }

    /**
     * Internal use only, you should not directly use this method.
     *
     * @param value int
     * @see #getOriginChannel()
     */
    public void setOriginChannel(final int value) {
        originChannel = value;
    }

    /**
     * Reset origin information (used after saved operation normally, internal use only).
     */
    public void resetOriginInformation() {
        setSeries(0);
        setOriginChannel(-1);
        setOriginResolution(0);
        setOriginTMin(-1);
        setOriginTMax(-1);
        setOriginZMin(-1);
        setOriginZMax(-1);
        setOriginXYRegion(null);
    }

    /**
     * @return series index if the Sequence comes from a multi serie image.<br>
     * By default it returns 0 if the sequence comes from a single serie image or if this is the
     * first series image.
     */
    public int getSeries() {
        // retrieve the image ID (sequences are always single serie)
        final String id = MetaDataUtil.getImageID(getOMEXMLMetadata(), 0);

        if (id.startsWith("Image:")) {
            final String[] serieNums = id.substring(6).split(":");

            if (serieNums.length > 0)
                return StringUtil.parseInt(serieNums[0], 0);
        }

        return 0;
    }

    /**
     * Set series index if the Sequence comes from a multi serie image (internal use only).
     *
     * @param value int
     */
    public void setSeries(final int value) {
        // retrieve the image ID (sequences are always single serie)
        final String id = MetaDataUtil.getImageID(getOMEXMLMetadata(), 0);

        if (id.startsWith("Image:"))
            MetaDataUtil.setImageID(getOMEXMLMetadata(), 0, "Image:" + value);
    }

    /**
     * @return meta data object
     */
    public OMEXMLMetadata getOMEXMLMetadata() {
        return metaData;
    }

    /**
     * Set the meta data object
     *
     * @param metaData OME metadata
     */
    public void setMetaData(final OMEXMLMetadata metaData) {
        if (this.metaData != metaData) {
            this.metaData = metaData;
            // all meta data changed
            metaChanged(null);
        }
    }

    /**
     * @return the physical position [X,Y,Z] (in &micro;m) of the image represented by this Sequence.
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image from the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we just use value from Plane(0,0,0) then we use the pixels size and time
     * interval
     * information to compute other positions.
     */
    public double[] getPosition() {
        return new double[]{getPositionX(), getPositionY(), getPositionZ()};
    }

    /**
     * @return the X physical position / offset (in &micro;m) of the image represented by this Sequence.<br>
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we just use value from Plane(0,0,0) then we use the pixels size and time
     * interval
     * information to compute other positions.
     */
    public double getPositionX() {
        return MetaDataUtil.getPositionX(metaData, 0, 0, 0, 0, 0d);
    }

    /**
     * @return the Y physical position / offset (in &micro;m) of the image represented by this Sequence.<br>
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we just use value from Plane(0,0,0) then we use the pixels size and time
     * interval
     * information to compute other positions.
     */
    public double getPositionY() {
        return MetaDataUtil.getPositionY(metaData, 0, 0, 0, 0, 0d);
    }

    /**
     * @return the Z physical position / offset (in &micro;m) of the image represented by this Sequence.<br>
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we just use value from Plane(0,0,0) then we use the pixels size and time
     * interval
     * information to compute other positions.
     */
    public double getPositionZ() {
        return MetaDataUtil.getPositionZ(metaData, 0, 0, 0, 0, 0d);
    }

    /**
     * @return Same as {@link #getTimeStamp()}
     */
    public long getPositionT() {
        return getTimeStamp();
    }

    /**
     * @return the timestamp (elapsed milliseconds from the Java epoch of 1970-01-01 T00:00:00Z) of the image represented by this Sequence.
     * @see #getPositionTOffset(int, int, int)
     * @see #getTimeInterval()
     */
    public long getTimeStamp() {
        return MetaDataUtil.getTimeStamp(metaData, 0, 0L);
    }

    /**
     * @return the time position offset (in second for OME compatibility) relative to first image for the image at specified (T,Z,C) position.
     * @see #getTimeInterval()
     * @see #getTimeStamp()
     */
    public double getPositionTOffset(final int t, final int z, final int c) {
        return MetaDataUtil.getPositionTOffset(metaData, 0, t, z, c, 0d);
    }

    /**
     * Sets the X physical position / offset (in &micro;m) of the image represented by this Sequence.<br>
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we always use value from Plane(0,0,0)
     *
     * @param value double
     */
    public void setPositionX(final double value) {
        if (getPositionX() != value) {
            MetaDataUtil.setPositionX(metaData, 0, 0, 0, 0, value);
            metaChanged(ID_POSITION_X);
        }
    }

    /**
     * Sets the X physical position / offset (in &micro;m) of the image represented by this Sequence.<br>
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we always use value from Plane(0,0,0)
     *
     * @param value double
     */
    public void setPositionY(final double value) {
        if (getPositionY() != value) {
            MetaDataUtil.setPositionY(metaData, 0, 0, 0, 0, value);
            metaChanged(ID_POSITION_Y);
        }
    }

    /**
     * Sets the X physical position / offset (in &micro;m) of the image represented by this Sequence.<br>
     * This information can be used to represent the position of the image in the original sample (microscope
     * information) or the position of a sub image the original image (crop operation).<br>
     * Note that OME store this information at Plane level (each Z,T,C), here we always use value from Plane(0,0,0)
     *
     * @param value double
     */
    public void setPositionZ(final double value) {
        if (getPositionZ() != value) {
            MetaDataUtil.setPositionZ(metaData, 0, 0, 0, 0, value);
            metaChanged(ID_POSITION_Z);
        }
    }

    /**
     * @param value long
     *              Same as {@link #setTimeStamp(long)}
     */
    public void setPositionT(final long value) {
        setTimeStamp(value);
    }

    /**
     * Sets the timestamp (elapsed milliseconds from the Java epoch of 1970-01-01 T00:00:00Z) for the image represented by this Sequence.
     *
     * @param value long
     * @see #setPositionTOffset(int, int, int, double)
     * @see #setTimeInterval(double)
     */
    public void setTimeStamp(final long value) {
        if (getTimeStamp() != value) {
            MetaDataUtil.setTimeStamp(metaData, 0, value);
            metaChanged(ID_POSITION_T);
        }
    }

    /**
     * Sets the time position / offset (in second for OME compatibility) relative to first image for the image at specified (T,Z,C) position.
     *
     * @param value long
     * @see #setTimeInterval(double)
     * @see #setTimeStamp(long)
     */
    public void setPositionTOffset(final int t, final int z, final int c, final double value) {
        if (getPositionTOffset(t, z, c) != value) {
            MetaDataUtil.setPositionTOffset(metaData, 0, t, z, c, value);
            metaChanged(ID_POSITION_T_OFFSET, t);
        }
    }

    /**
     * @return pixel size for [X,Y,Z] dimension (in &micro;m to be OME compatible)
     */
    public double[] getPixelSize() {
        return new double[]{getPixelSizeX(), getPixelSizeY(), getPixelSizeZ()};
    }

    /**
     * @return X pixel size (in &micro;m to be OME compatible)
     */
    public double getPixelSizeX() {
        return MetaDataUtil.getPixelSizeX(metaData, 0, 1d);
    }

    /**
     * @return Y pixel size (in &micro;m to be OME compatible)
     */
    public double getPixelSizeY() {
        return MetaDataUtil.getPixelSizeY(metaData, 0, 1d);
    }

    /**
     * @return Z pixel size (in &micro;m to be OME compatible)
     */
    public double getPixelSizeZ() {
        return MetaDataUtil.getPixelSizeZ(metaData, 0, 1d);
    }

    /**
     * @return T time interval (in second for OME compatibility)
     * @see #getPositionTOffset(int, int, int)
     */
    public double getTimeInterval() {
        double result = MetaDataUtil.getTimeInterval(metaData, 0, 0d);

        // not yet defined ?
        if (result == 0d) {
            result = MetaDataUtil.getTimeIntervalFromTimePositions(metaData, 0);
            // we got something --> set it as the time interval
            if (result != 0d)
                MetaDataUtil.setTimeInterval(metaData, 0, result);
        }

        return result;
    }

    /**
     * Set X pixel size (in &micro;m to be OME compatible)
     *
     * @param value double
     */
    public void setPixelSizeX(final double value) {
        if (getPixelSizeX() != value) {
            MetaDataUtil.setPixelSizeX(metaData, 0, value);
            metaChanged(ID_PIXEL_SIZE_X);
        }
    }

    /**
     * Set Y pixel size (in &micro;m to be OME compatible)
     *
     * @param value double
     */
    public void setPixelSizeY(final double value) {
        if (getPixelSizeY() != value) {
            MetaDataUtil.setPixelSizeY(metaData, 0, value);
            metaChanged(ID_PIXEL_SIZE_Y);
        }
    }

    /**
     * Set Z pixel size (in &micro;m to be OME compatible)
     *
     * @param value double
     */
    public void setPixelSizeZ(final double value) {
        if (getPixelSizeZ() != value) {
            MetaDataUtil.setPixelSizeZ(metaData, 0, value);
            metaChanged(ID_PIXEL_SIZE_Z);
        }
    }

    /**
     * Set T time resolution (in second to be OME compatible)
     *
     * @param value double
     * @see #setPositionTOffset(int, int, int, double)
     */
    public void setTimeInterval(final double value) {
        if (MetaDataUtil.getTimeInterval(metaData, 0, 0d) != value) {
            MetaDataUtil.setTimeInterval(metaData, 0, value);
            metaChanged(ID_TIME_INTERVAL);
        }
    }

    /**
     * @param dimCompute dimension order for size calculation<br>
     *                   <ul>
     *                   <li>1 --&gt; pixel size X used for conversion</li>
     *                   <li>2 --&gt; pixel size X and Y used for conversion</li>
     *                   <li>3 or above --&gt; pixel size X, Y and Z used for conversion</li>
     *                   </ul>
     * @param dimResult  dimension order for the result (unit)<br>
     *                   <ul>
     *                   <li>1 --&gt; distance</li>
     *                   <li>2 --&gt; area</li>
     *                   <li>3 or above --&gt; volume</li>
     *                   </ul>
     * @return the pixel size scaling factor to convert a number of pixel/voxel unit into <code>µm</code><br>
     * <br>
     * For instance to get the scale ration for 2D distance:<br>
     * <code>valueMicroMeter = pixelNum * getPixelSizeScaling(2, 1)</code><br>
     * For a 2D surface:<br>
     * <code>valueMicroMeter2 = pixelNum * getPixelSizeScaling(2, 2)</code><br>
     * For a 3D volume:<br>
     * <code>valueMicroMeter3 = pixelNum * getPixelSizeScaling(3, 3)</code><br>
     */
    public double getPixelSizeScaling(final int dimCompute, final int dimResult) {
        double result;

        switch (dimCompute) {
            case 0:
                // incorrect
                return 0d;

            case 1:
                result = getPixelSizeX();
                break;

            case 2:
                result = getPixelSizeX() * getPixelSizeY();
                break;

            default:
                result = getPixelSizeX() * getPixelSizeY() * getPixelSizeZ();
                break;
        }

        result = Math.pow(result, (double) dimResult / (double) dimCompute);

        return result;
    }

    /**
     * @param dimCompute dimension order for size calculation<br>
     *                   <ul>
     *                   <li>1 --&gt; pixel size X used for conversion</li>
     *                   <li>2 --&gt; pixel size X and Y used for conversion</li>
     *                   <li>3 or above --&gt; pixel size X, Y and Z used for conversion</li>
     *                   </ul>
     * @param dimResult  dimension order for the result (unit)<br>
     *                   <ul>
     *                   <li>1 --&gt; distance</li>
     *                   <li>2 --&gt; area</li>
     *                   <li>3 or above --&gt; volume</li>
     *                   </ul>
     * @return the best pixel size unit for the specified dimension order given the sequence's pixel
     * size informations.<br>
     * Compute a 2D distance:
     *
     * <pre>
     *         dimCompute = 2;
     *         dimUnit = 1;
     *         valueMicroMeter = pixelNum * getPixelSizeScaling(dimCompute);
     *         bestUnit = getBestPixelSizeUnit(dimCompute, dimUnit);
     *         finalValue = UnitUtil.getValueInUnit(valueMicroMeter, UnitPrefix.MICRO, bestUnit);
     *         valueString = Double.toString(finalValue) + &quot; &quot; + bestUnit.toString() + &quot;m&quot;;
     *         </pre>
     * <p>
     * Compute a 2D surface:
     *
     * <pre>
     *         dimCompute = 2;
     *         dimUnit = 2;
     *         valueMicroMeter = pixelNum * getPixelSizeScaling(dimCompute);
     *         bestUnit = getBestPixelSizeUnit(dimCompute, dimUnit);
     *         finalValue = UnitUtil.getValueInUnit(valueMicroMeter, UnitPrefix.MICRO, bestUnit);
     *         valueString = Double.toString(finalValue) + &quot; &quot; + bestUnit.toString() + &quot;m2&quot;;
     *         </pre>
     * <p>
     * Compute a 3D volume:
     *
     * <pre>
     *         dimCompute = 3;
     *         dimUnit = 3;
     *         valueMicroMeter = pixelNum * getPixelSizeScaling(dimCompute);
     *         bestUnit = getBestPixelSizeUnit(dimCompute, dimUnit);
     *         finalValue = UnitUtil.getValueInUnit(valueMicroMeter, UnitPrefix.MICRO, bestUnit);
     *         valueString = Double.toString(finalValue) + &quot; &quot; + bestUnit.toString() + &quot;m3&quot;;
     *         </pre>
     * @see #calculateSizeBestUnit(double, int, int)
     */
    public UnitPrefix getBestPixelSizeUnit(final int dimCompute, final int dimResult) {
        return switch (dimResult) {
            case 0 ->
                // keep original
                    UnitPrefix.MICRO;
            case 1 -> UnitUtil.getBestUnit((getPixelSizeScaling(dimCompute, dimResult) * 10), UnitPrefix.MICRO, dimResult);
            case 2 -> UnitUtil.getBestUnit((getPixelSizeScaling(dimCompute, dimResult) * 100), UnitPrefix.MICRO, dimResult);
            default -> UnitUtil.getBestUnit((getPixelSizeScaling(dimCompute, dimResult) * 1000), UnitPrefix.MICRO, dimResult);
        };
    }

    /**
     * @param pixelNumber number of pixel
     * @param dimCompute  dimension order for size calculation<br>
     *                    <ul>
     *                    <li>1 --&gt; pixel size X used for conversion</li>
     *                    <li>2 --&gt; pixel size X and Y used for conversion</li>
     *                    <li>3 or above --&gt; pixel size X, Y and Z used for conversion</li>
     *                    </ul>
     * @param dimResult   dimension order for the result (unit)<br>
     *                    <ul>
     *                    <li>1 --&gt; distance</li>
     *                    <li>2 --&gt; area</li>
     *                    <li>3 or above --&gt; volume</li>
     *                    </ul>
     * @return the size in &micro;m for the specified amount of sample/pixel value in the specified
     * dimension order.<br>
     * <br>
     * For the perimeter in &micro;m:<br>
     * <code>perimeter = calculateSize(contourInPixel, 2, 1)</code><br>
     * For a 2D surface in &micro;m2:<br>
     * <code>surface = calculateSize(interiorInPixel, 2, 2)</code><br>
     * For a 2D surface area in &micro;m2:<br>
     * <code>volume = calculateSize(contourInPixel, 3, 2)</code><br>
     * For a 3D volume in &micro;m3:<br>
     * <code>volume = calculateSize(interiorInPixel, 3, 3)</code><br>
     * @see #calculateSizeBestUnit(double, int, int)
     */
    public double calculateSize(final double pixelNumber, final int dimCompute, final int dimResult) {
        return pixelNumber * getPixelSizeScaling(dimCompute, dimResult);
    }

    /**
     * @param pixelNumber number of pixel
     * @param dimCompute  dimension order for size calculation<br>
     *                    <ul>
     *                    <li>1 --&gt; pixel size X used for conversion</li>
     *                    <li>2 --&gt; pixel size X and Y used for conversion</li>
     *                    <li>3 or above --&gt; pixel size X, Y and Z used for conversion</li>
     *                    </ul>
     * @param dimResult   dimension order for the result (unit)<br>
     *                    <ul>
     *                    <li>1 --&gt; distance</li>
     *                    <li>2 --&gt; area</li>
     *                    <li>3 or above --&gt; volume</li>
     *                    </ul>
     * @return the size converted in the best unit (see {@link #getBestPixelSizeUnit(int, int)} for
     * the specified amount of sample/pixel value in the specified dimension order.<br>
     * Compute a 2D distance:
     *
     * <pre>
     *         dimCompute = 2;
     *         dimUnit = 1;
     *         valueBestUnit = calculateSizeBestUnit(pixelNum, dimCompute, dimUnit);
     *         bestUnit = getBestPixelSizeUnit(dimCompute, dimUnit);
     *         valueString = Double.toString(valueBestUnit) + &quot; &quot; + bestUnit.toString() + &quot;m&quot;;
     *         </pre>
     * <p>
     * Compute a 2D surface:
     *
     * <pre>
     *         dimCompute = 2;
     *         dimUnit = 2;
     *         valueBestUnit = calculateSizeBestUnit(pixelNum, dimCompute, dimUnit);
     *         bestUnit = getBestPixelSizeUnit(dimCompute, dimUnit);
     *         valueString = Double.toString(valueBestUnit) + &quot; &quot; + bestUnit.toString() + &quot;m2&quot;;
     *         </pre>
     * <p>
     * Compute a 3D volume:
     *
     * <pre>
     *         dimCompute = 3;
     *         dimUnit = 3;
     *         valueBestUnit = calculateSizeBestUnit(pixelNum, dimCompute, dimUnit);
     *         bestUnit = getBestPixelSizeUnit(dimCompute, dimUnit);
     *         valueString = Double.toString(valueBestUnit) + &quot; &quot; + bestUnit.toString() + &quot;m3&quot;;
     *         </pre>
     * @see #calculateSize(double, int, int)
     * @see #getBestPixelSizeUnit(int, int)
     */
    public double calculateSizeBestUnit(final double pixelNumber, final int dimCompute, final int dimResult) {
        final double value = calculateSize(pixelNumber, dimCompute, dimResult);
        final UnitPrefix unit = getBestPixelSizeUnit(dimCompute, dimResult);
        return UnitUtil.getValueInUnit(value, UnitPrefix.MICRO, unit, dimResult);
    }

    /**
     * @param pixelNumber      number of pixel
     * @param dimCompute       dimension order for the calculation
     * @param dimResult        dimension order for the result (unit)
     * @param significantDigit wanted significant digit for the result (0 for all)
     * @return the size and appropriate unit in form of String for specified amount of sample/pixel
     * value in the specified dimension order.<br>
     * <br>
     * For instance if you want to retrieve the 2D distance:<br>
     * <code>distanceStr = calculateSize(distanceInPixel, 2, 1, 5)</code><br>
     * For a 2D surface:<br>
     * <code>surfaceStr = calculateSize(surfaceInPixel, 2, 2, 5)</code><br>
     * For a 3D volume:<br>
     * <code>volumeStr = calculateSize(volumeInPixel, 3, 3, 5)</code><br>
     * @see #calculateSize(double, int, int)
     */
    public String calculateSize(final double pixelNumber, final int dimCompute, final int dimResult, final int significantDigit) {
        double value = calculateSize(pixelNumber, dimCompute, dimResult);
        final String postFix = (dimResult > 1) ? StringUtil.toString(dimResult) : "";
        final UnitPrefix unit = UnitUtil.getBestUnit(value, UnitPrefix.MICRO, dimResult);
        // final UnitPrefix unit = getBestPixelSizeUnit(dimCompute, dimResult);

        value = UnitUtil.getValueInUnit(value, UnitPrefix.MICRO, unit, dimResult);
        if (significantDigit != 0)
            value = MathUtil.roundSignificant(value, significantDigit);

        return StringUtil.toString(value) + " " + unit + "m" + postFix;
    }

    /**
     * @param index int
     * @return Get default name for specified channel
     */
    public String getDefaultChannelName(final int index) {
        return MetaDataUtil.getDefaultChannelName(index);
    }

    /**
     * @param index int
     * @return Get name for specified channel
     */
    public String getChannelName(final int index) {
        return MetaDataUtil.getChannelName(metaData, 0, index);
    }

    /**
     * Set name for specified channel
     *
     * @param index int
     * @param value string
     */
    public void setChannelName(final int index, final String value) {
        if (!StringUtil.equals(getChannelName(index), value)) {
            MetaDataUtil.setChannelName(metaData, 0, index, value);
            metaChanged(ID_CHANNEL_NAME, index);
        }
    }

    /**
     * @return the user name of the person who created this Sequence (read only property).<br>
     * UserName generally refer to the user name environment variable value (logged user on the system) when the sequence has been created / acquired
     * on the microscope if this information is present, or later modified / generated using Icy.<br>
     * Note that you can have several user name as the original image may have been modified later by someone else, in which case you can use
     * {@link #getUserNames()} method instead
     */
    public String getUserName() {
        return MetaDataUtil.getUserName(metaData);
    }

    /**
     * @return the user name(s) of the person(s) who created this Sequence (read only properties).<br>
     * UserName generally refer to the user name environment variable value (logged user on the system) when the sequence has been created / acquired
     * on the microscope if this information is present in the original metadata, or later modified / generated using Icy.<br>
     */
    public List<String> getUserNames() {
        return MetaDataUtil.getUserNames(metaData);
    }

    /**
     * @return true is channel bounds are automatically updated when sequence data is modified.
     * @see #setAutoUpdateChannelBounds(boolean)
     */
    public boolean getAutoUpdateChannelBounds() {
        return autoUpdateChannelBounds;
    }

    /**
     * @param value If set to <code>true</code> (default) then channel bounds will be automatically recalculated
     *              when sequence data is modified.<br>
     *              This can consume a lot of time if you make many updates on large sequence.<br>
     *              In this case you should do your updates in a {@link #beginUpdate()} ... {@link #endUpdate()} block to avoid
     *              severals recalculation.
     */
    public void setAutoUpdateChannelBounds(final boolean value) {
        if (autoUpdateChannelBounds != value) {
            if (value)
                updateChannelsBounds(false);

            autoUpdateChannelBounds = value;
        }
    }

    /**
     * Add the specified listener to listeners list
     *
     * @param listener sequence listener
     */
    public void addListener(final SequenceListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the specified listener from listeners list
     *
     * @param listener sequence listener
     */
    public void removeListener(final SequenceListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return Get listeners list
     */
    public SequenceListener[] getListeners() {
        return listeners.toArray(new SequenceListener[0]);
    }

    /**
     * Add the specified {@link icy.sequence.SequenceModel.SequenceModelListener} to listeners list
     *
     * @param listener sequence listener
     */
    @Override
    public void addSequenceModelListener(final SequenceModelListener listener) {
        modelListeners.add(listener);
    }

    /**
     * Remove the specified {@link icy.sequence.SequenceModel.SequenceModelListener} from listeners
     *
     * @param listener sequence listener
     */
    @Override
    public void removeSequenceModelListener(final SequenceModelListener listener) {
        modelListeners.remove(listener);
    }

    /**
     * @return Get the Undo manager of this sequence
     */
    public IcyUndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * @param overlay overlay
     * @return true if the sequence contains the specified overlay
     */
    public boolean contains(final Overlay overlay) {
        if (overlay == null)
            return false;

        synchronized (overlays) {
            return overlays.contains(overlay);
        }
    }

    /**
     * @param roi ROI
     * @return true if the sequence contains the specified ROI
     */
    public boolean contains(final ROI roi) {
        if (roi == null)
            return false;

        synchronized (rois) {
            return rois.contains(roi);
        }
    }

    /**
     * @return true if the sequence contains at least one Overlay.
     */
    public boolean hasOverlay() {
        return overlays.size() > 0;
    }

    /**
     * @return all overlays attached to this sequence
     */
    public List<Overlay> getOverlays() {
        synchronized (overlays) {
            return new ArrayList<>(overlays);
        }
    }

    /**
     * @return all overlays attached to this sequence (HashSet form)
     */
    public Set<Overlay> getOverlaySet() {
        synchronized (overlays) {
            return new HashSet<>(overlays);
        }
    }

    /**
     * @return true if the sequence contains Overlay of specified Overlay class.
     */
    public boolean hasOverlay(final Class<? extends Overlay> overlayClass) {
        synchronized (overlays) {
            for (final Overlay overlay : overlays)
                if (overlayClass.isInstance(overlay))
                    return true;
        }

        return false;
    }

    /**
     * @return overlays of specified class attached to this sequence
     */
    @SuppressWarnings("unchecked")
    public <T extends Overlay> List<T> getOverlays(final Class<T> overlayClass) {
        final List<T> result = new ArrayList<>(overlays.size());

        synchronized (overlays) {
            for (final Overlay overlay : overlays)
                if (overlayClass.isInstance(overlay))
                    result.add((T) overlay);
        }

        return result;
    }

    /**
     * @return true if the sequence contains at least one ROI.
     */
    public boolean hasROI() {
        return rois.size() > 0;
    }

    /**
     * @param sorted If true the returned list is ordered by the ROI id (creation order).
     * @return all ROIs attached to this sequence.
     */
    public List<ROI> getROIs(final boolean sorted) {
        final List<ROI> result;

        synchronized (rois) {
            result = new ArrayList<>(rois);
        }

        // sort it if required
        if (sorted)
            result.sort(ROI.idComparator);

        return result;
    }

    /**
     * @return all ROIs attached to this sequence.
     */
    public ArrayList<ROI> getROIs() {
        return (ArrayList<ROI>) getROIs(false);
    }

    /**
     * @return all ROIs attached to this sequence (HashSet form)
     */
    public HashSet<ROI> getROISet() {
        synchronized (rois) {
            return new HashSet<>(rois);
        }
    }

    /**
     * @param sorted If true the returned list is ordered by the ROI id (creation order).
     * @return all 2D ROIs attached to this sequence.
     */
    public List<ROI2D> getROI2Ds(final boolean sorted) {
        final List<ROI2D> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi instanceof ROI2D)
                    result.add((ROI2D) roi);
        }

        // sort it if required
        if (sorted)
            result.sort(ROI.idComparator);

        return result;
    }

    /**
     * @return all 2D ROIs attached to this sequence.
     */
    public ArrayList<ROI2D> getROI2Ds() {
        return (ArrayList<ROI2D>) getROI2Ds(false);
    }

    /**
     * @param sorted If true the returned list is ordered by the ROI id (creation order).
     * @return all 3D ROIs attached to this sequence.
     */
    public List<ROI3D> getROI3Ds(final boolean sorted) {
        final List<ROI3D> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi instanceof ROI3D)
                    result.add((ROI3D) roi);
        }

        // sort it if required
        if (sorted)
            result.sort(ROI.idComparator);

        return result;
    }

    /**
     * @return all 3D ROIs attached to this sequence.
     */
    public ArrayList<ROI3D> getROI3Ds() {
        return (ArrayList<ROI3D>) getROI3Ds(false);
    }

    /**
     * @param roiClass ROI class
     * @return true if the sequence contains ROI of specified ROI class.
     */
    public boolean hasROI(final Class<? extends ROI> roiClass) {
        synchronized (rois) {
            for (final ROI roi : rois)
                if (roiClass.isInstance(roi))
                    return true;
        }

        return false;
    }

    /**
     * @param roiClass ROI class
     * @param <T>      generic object
     * @param sorted   boolean
     * @return ROIs of specified class attached to this sequence
     */
    @SuppressWarnings("unchecked")
    public <T extends ROI> List<T> getROIs(final Class<T> roiClass, final boolean sorted) {
        final List<T> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roiClass.isInstance(roi))
                    result.add((T) roi);
        }

        // sort it if required
        if (sorted)
            result.sort(ROI.idComparator);

        return result;
    }

    /**
     * @param roiClass ROI Class
     * @return the number of ROI of specified ROI class attached to the sequence.
     */
    public int getROICount(final Class<? extends ROI> roiClass) {
        int result = 0;

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roiClass.isInstance(roi))
                    result++;
        }

        return result;
    }

    /**
     * @return true if the sequence contains at least one selected ROI.
     */
    public boolean hasSelectedROI() {
        return getSelectedROI() != null;
    }

    /**
     * @return the first selected ROI found (null if no ROI selected)
     */
    public ROI getSelectedROI() {
        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected())
                    return roi;
        }

        return null;
    }

    /**
     * @return the first selected 2D ROI found (null if no 2D ROI selected)
     */
    public ROI2D getSelectedROI2D() {
        synchronized (rois) {
            for (final ROI roi : rois)
                if ((roi instanceof ROI2D) && roi.isSelected())
                    return (ROI2D) roi;
        }

        return null;
    }

    /**
     * @return the first selected 3D ROI found (null if no 3D ROI selected)
     */
    public ROI3D getSelectedROI3D() {
        synchronized (rois) {
            for (final ROI roi : rois)
                if ((roi instanceof ROI3D) && roi.isSelected())
                    return (ROI3D) roi;
        }

        return null;
    }

    /**
     * @param roiClass     ROI class restriction
     * @param wantReadOnly also return ROI with read only state
     * @return all selected ROI of given class (Set format).
     */
    public Set<ROI> getSelectedROISet(final Class<? extends ROI> roiClass, final boolean wantReadOnly) {
        final Set<ROI> result = new HashSet<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected() && roiClass.isInstance(roi))
                    if (wantReadOnly || !roi.isReadOnly())
                        result.add(roi);
        }

        return result;
    }

    /**
     * @param roiClass ROI class restriction
     * @param <T>      generic object
     * @return all selected ROI of given class (Set format).
     */
    @SuppressWarnings("unchecked")
    public <T extends ROI> Set<T> getSelectedROISet(final Class<T> roiClass) {
        final Set<T> result = new HashSet<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected() && roiClass.isInstance(roi))
                    result.add((T) roi);
        }

        return result;
    }

    /**
     * @return all selected ROI (Set format).
     */
    public Set<ROI> getSelectedROISet() {
        final Set<ROI> result = new HashSet<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected())
                    result.add(roi);
        }

        return result;
    }

    /**
     * @param roiClass     ROI class restriction
     * @param sorted       If true the returned list is ordered by the ROI id (creation order)
     * @param wantReadOnly also return ROI with read only state
     * @param <T>          generic object
     * @return all selected ROI of given class.
     */
    @SuppressWarnings("unchecked")
    public <T extends ROI> List<T> getSelectedROIs(final Class<T> roiClass, final boolean sorted, final boolean wantReadOnly) {
        final List<T> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected() && roiClass.isInstance(roi))
                    result.add((T) roi);
        }

        // sort it if required
        if (sorted)
            result.sort(ROI.idComparator);

        return result;
    }

    /**
     * @param roiClass     ROI class restriction
     * @param wantReadOnly also return ROI with read only state
     * @return all selected ROI of given class.
     */
    public List<ROI> getSelectedROIs(final Class<? extends ROI> roiClass, final boolean wantReadOnly) {
        final List<ROI> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected() && roiClass.isInstance(roi))
                    if (wantReadOnly || !roi.isReadOnly())
                        result.add(roi);
        }

        return result;
    }

    /**
     * @return all selected ROI
     */
    public ArrayList<ROI> getSelectedROIs() {
        final ArrayList<ROI> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isSelected())
                    result.add(roi);
        }

        return result;
    }

    /**
     * @return all selected 2D ROI
     */
    public ArrayList<ROI2D> getSelectedROI2Ds() {
        final ArrayList<ROI2D> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if ((roi instanceof ROI2D) && roi.isSelected())
                    result.add((ROI2D) roi);
        }

        return result;
    }

    /**
     * @return all selected 3D ROI
     */
    public ArrayList<ROI3D> getSelectedROI3Ds() {
        final ArrayList<ROI3D> result = new ArrayList<>(rois.size());

        synchronized (rois) {
            for (final ROI roi : rois)
                if ((roi instanceof ROI3D) && roi.isSelected())
                    result.add((ROI3D) roi);
        }

        return result;
    }

    /**
     * @return the current focused ROI (null if no ROI focused)
     */
    public ROI getFocusedROI() {
        synchronized (rois) {
            for (final ROI roi : rois)
                if (roi.isFocused())
                    return roi;
        }

        return null;
    }

    /**
     * Set the selected ROI (exclusive selection).<br>
     * Specifying a <code>null</code> ROI here will actually clear all ROI selection.<br>
     * Note that you can use {@link #setSelectedROIs(List)} or {@link ROI#setSelected(boolean)} for
     * multiple ROI selection.
     *
     * @param roi the ROI to select.
     * @return <code>false</code> is the specified ROI is not attached to the sequence.
     */
    public boolean setSelectedROI(final ROI roi) {
        beginUpdate();
        try {
            synchronized (rois) {
                for (final ROI currentRoi : rois)
                    if (currentRoi != roi)
                        currentRoi.setSelected(false);
            }

            if (contains(roi)) {
                roi.setSelected(true);
                return true;
            }
        }
        finally {
            endUpdate();
        }

        return false;
    }

    /**
     * Set selected ROI (unselected all others)
     *
     * @param selected list of ROI
     */
    public void setSelectedROIs(final List<? extends ROI> selected) {
        final List<ROI> oldSelected = getSelectedROIs();

        final int newSelectedSize = (selected == null) ? 0 : selected.size();
        final int oldSelectedSize = oldSelected.size();

        // easy optimization
        if ((newSelectedSize == 0) && (oldSelectedSize == 0))
            return;

        final HashSet<ROI> newSelected;

        // use HashSet for fast .contains() !
        if (selected != null)
            newSelected = new HashSet<>(selected);
        else
            newSelected = new HashSet<>();

        // selection changed ?
        if (!CollectionUtil.equals(oldSelected, newSelected)) {
            beginUpdate();
            try {
                if (newSelectedSize > 0) {
                    for (final ROI roi : getROIs())
                        roi.setSelected(newSelected.contains(roi));
                }
                else {
                    // unselected all ROIs
                    for (final ROI roi : getROIs())
                        roi.setSelected(false);
                }
            }
            finally {
                endUpdate();
            }
        }
    }

    /**
     * Set the focused ROI
     *
     * @param roi ROI
     */
    public boolean setFocusedROI(final ROI roi) {
        // faster .contain()
        final Set<ROI> listRoi = getROISet();

        beginUpdate();
        try {
            for (final ROI currentRoi : listRoi)
                if (currentRoi != roi)
                    currentRoi.internalUnfocus();

            if (listRoi.contains(roi)) {
                roi.internalFocus();
                return true;
            }
        }
        finally {
            endUpdate();
        }

        return false;
    }

    /**
     * Add the specified collection of ROI to the sequence.
     *
     * @param rois    the collection of ROI to attach to the sequence
     * @param canUndo If true the action can be canceled by the undo manager.
     * @return <code>true</code> if the operation succeed or <code>false</code> if some ROIs could
     * not be added (already present)
     */
    public boolean addROIs(final Collection<? extends ROI> rois, final boolean canUndo) {
        if (!rois.isEmpty()) {
            final List<ROI> addedRois = new ArrayList<>();

            beginUpdate();
            try {
                for (final ROI roi : rois) {
                    if (addROI(roi, false))
                        addedRois.add(roi);
                }
            }
            finally {
                endUpdate();
            }

            if (canUndo && !addedRois.isEmpty())
                addUndoableEdit(new ROIAddsSequenceEdit(this, addedRois));

            return addedRois.size() == rois.size();
        }

        return true;
    }

    /**
     * @param roi ROI to attach to the sequence
     * @return Add the specified ROI to the sequence.
     */
    public boolean addROI(final ROI roi) {
        return addROI(roi, false);
    }

    /**
     * Add the specified ROI to the sequence.
     *
     * @param roi     ROI to attach to the sequence
     * @param canUndo If true the action can be canceled by the undo manager.
     * @return <code>true</code> if the operation succeed or <code>false</code> otherwise (already
     * present)
     */
    public boolean addROI(final ROI roi, final boolean canUndo) {
        if ((roi == null) || contains(roi))
            return false;

        synchronized (rois) {
            rois.add(roi);
        }
        // add listener to ROI
        roi.addListener(this);
        // notify roi added
        roiChanged(roi, SequenceEventType.ADDED);
        // then add ROI overlay to sequence
        addOverlay(roi.getOverlay());

        if (canUndo)
            addUndoableEdit(new ROIAddSequenceEdit(this, roi));

        return true;

    }

    /**
     * @param roi ROI to detach from the sequence
     * @return Remove the specified ROI from the sequence.
     */
    public boolean removeROI(final ROI roi) {
        return removeROI(roi, false);
    }

    /**
     * Remove the specified ROI from the sequence.
     *
     * @param roi     ROI to detach from the sequence
     * @param canUndo If true the action can be canceled by the undo manager.
     * @return <code>false</code> if the ROI was not found in the sequence.<br>
     * Returns <code>true</code> otherwise.
     */
    public boolean removeROI(final ROI roi, final boolean canUndo) {
        if (contains(roi)) {
            // remove ROI overlay first
            removeOverlay(roi.getOverlay());

            // remove ROI
            synchronized (rois) {
                rois.remove(roi);
            }
            // remove listener
            roi.removeListener(this);
            // notify roi removed
            roiChanged(roi, SequenceEventType.REMOVED);

            if (canUndo)
                addUndoableEdit(new ROIRemoveSequenceEdit(this, roi));

            return true;
        }

        return false;
    }

    /**
     * Remove the specified collection of ROI from the sequence.
     *
     * @param rois    the collection of ROI to remove from the sequence
     * @param canUndo If true the action can be canceled by the undo manager.
     * @return <code>true</code> if all ROI from the collection has been correctly removed.
     */
    public boolean removeROIs(final Collection<? extends ROI> rois, final boolean canUndo) {
        if (!rois.isEmpty()) {
            final List<ROI> removedRois = new ArrayList<>();

            for (final ROI roi : rois) {
                if (removeROI(roi, false))
                    removedRois.add(roi);
            }

            if (canUndo && !removedRois.isEmpty())
                addUndoableEdit(new ROIRemovesSequenceEdit(this, removedRois));

            return removedRois.size() == rois.size();
        }

        return true;
    }

    /**
     * Remove all selected ROI from the sequence.
     *
     * @param removeReadOnly Specify if we should also remove <i>read only</i> ROI (see {@link ROI#isReadOnly()})
     * @return <code>true</code> if at least one ROI was removed.<br>
     * Returns <code>false</code> otherwise
     */
    public boolean removeSelectedROIs(final boolean removeReadOnly) {
        return removeSelectedROIs(removeReadOnly, false);
    }

    /**
     * Remove all selected ROI from the sequence.
     *
     * @param removeReadOnly Specify if we should also remove <i>read only</i> ROI (see {@link ROI#isReadOnly()})
     * @param canUndo        If true the action can be canceled by the undo manager.
     * @return <code>true</code> if at least one ROI was removed.<br>
     * Returns <code>false</code> otherwise
     */
    public boolean removeSelectedROIs(final boolean removeReadOnly, final boolean canUndo) {
        final List<ROI> undoList = new ArrayList<>();

        beginUpdate();
        try {
            synchronized (rois) {
                for (final ROI roi : getROIs()) {
                    if (roi.isSelected() && (removeReadOnly || !roi.isReadOnly())) {
                        // remove ROI overlay first
                        removeOverlay(roi.getOverlay());

                        rois.remove(roi);
                        // remove listener
                        roi.removeListener(this);
                        // notify roi removed
                        roiChanged(roi, SequenceEventType.REMOVED);

                        // save deleted ROI
                        undoList.add(roi);
                    }
                }
            }

            if (canUndo)
                undoManager.addEdit(new ROIRemovesSequenceEdit(this, undoList));
        }
        finally {
            endUpdate();
        }

        return !undoList.isEmpty();
    }

    /**
     * Remove all ROI from the sequence.
     */
    public void removeAllROI() {
        removeAllROI(false);
    }

    /**
     * Remove all ROI from the sequence.
     *
     * @param canUndo If true the action can be canceled by the undo manager.
     */
    public void removeAllROI(final boolean canUndo) {
        if (!rois.isEmpty()) {
            final List<ROI> allROIs = getROIs();

            // remove all ROI
            for (final ROI roi : allROIs)
                removeROI(roi, false);

            if (canUndo)
                addUndoableEdit(new ROIRemovesSequenceEdit(this, allROIs));
        }
    }

    /**
     * @param painter Used only for backward compatibility with {@link Painter} interface.
     * @return Return the overlay associated to the specified painter.<br>
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    protected Overlay getOverlay(final Painter painter) {
        if (painter instanceof Overlay)
            return (Overlay) painter;

        synchronized (overlays) {
            for (final Overlay overlay : overlays)
                if (overlay instanceof OverlayWrapper)
                    if (((OverlayWrapper) overlay).getPainter() == painter)
                        return overlay;
        }

        return null;
    }

    /**
     * @param overlay overlay
     * @return Add an overlay to the sequence.
     */
    public boolean addOverlay(final Overlay overlay) {
        if ((overlay == null) || contains(overlay))
            return false;

        synchronized (overlays) {
            overlays.add(overlay);
        }

        // add listener
        overlay.addOverlayListener(this);
        // notify overlay added
        overlayChanged(overlay, SequenceEventType.ADDED);

        return true;
    }

    /**
     * @param overlay overlay
     * @return Remove an overlay from the sequence.
     */
    public boolean removeOverlay(final Overlay overlay) {
        final boolean result;

        synchronized (overlays) {
            result = overlays.remove(overlay);
        }

        if (result) {
            // remove listener
            overlay.removeOverlayListener(this);
            // notify overlay removed
            overlayChanged(overlay, SequenceEventType.REMOVED);
        }

        return result;
    }

    /**
     * @param t int
     * @param z int
     * @return Return <i>true</i> if image data at given position is loaded.
     */
    public boolean isDataLoaded(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z, false);

        return (img != null) && img.isDataInitialized();
    }

    /**
     * @param t int
     * @return the VolumetricImage at position t
     */
    public VolumetricImage getVolumetricImage(final int t) {
        synchronized (volumetricImages) {
            return volumetricImages.get(Integer.valueOf(t));
        }
    }

    /**
     * @return the first VolumetricImage
     */
    protected VolumetricImage getFirstVolumetricImage() {
        final Entry<Integer, VolumetricImage> entry;

        synchronized (volumetricImages) {
            entry = volumetricImages.firstEntry();
        }

        if (entry != null)
            return entry.getValue();

        return null;
    }

    /**
     * @return the last VolumetricImage
     */
    protected VolumetricImage getLastVolumetricImage() {
        final Entry<Integer, VolumetricImage> entry;

        synchronized (volumetricImages) {
            entry = volumetricImages.lastEntry();
        }

        if (entry != null)
            return entry.getValue();

        return null;
    }

    /**
     * @return Add an empty volumetricImage at last index + 1
     */
    public VolumetricImage addVolumetricImage() {
        return setVolumetricImage(getSizeT());
    }

    /**
     * @return Add an empty volumetricImage at t position
     */
    protected VolumetricImage setVolumetricImage(final int t) {
        // remove old volumetric image if any
        removeAllImages(t);

        final VolumetricImage volImg = new VolumetricImage(this);

        synchronized (volumetricImages) {
            volumetricImages.put(Integer.valueOf(t), volImg);
        }

        return volImg;
    }

    /**
     * @param volImg img
     * @param t      int
     * @return Add a volumetricImage at t position<br>
     * It actually create a new volumetricImage and add it to the sequence<br>
     * The new created volumetricImage is returned
     */
    public VolumetricImage addVolumetricImage(final int t, final VolumetricImage volImg) {
        if (volImg != null) {
            final VolumetricImage result;

            beginUpdate();
            try {
                // get new volumetric image (remove old one if any)
                result = setVolumetricImage(t);

                for (final Entry<Integer, IcyBufferedImage> entry : volImg.getImages().entrySet())
                    setImage(t, entry.getKey().intValue(), entry.getValue());
            }
            finally {
                endUpdate();
            }

            return result;
        }

        return null;
    }

    /**
     * @param t int
     * @return the last image of VolumetricImage[t]
     */
    public IcyBufferedImage getLastImage(final int t) {
        final VolumetricImage volImg = getVolumetricImage(t);

        if (volImg != null)
            return volImg.getLastImage();

        return null;
    }

    /**
     * @return the first image of first VolumetricImage
     */
    public IcyBufferedImage getFirstImage() {
        final VolumetricImage volImg = getFirstVolumetricImage();

        if (volImg != null)
            return volImg.getFirstImage();

        return null;
    }

    /**
     * @return the first non null image if exist
     */
    public IcyBufferedImage getFirstNonNullImage() {
        synchronized (volumetricImages) {
            for (final VolumetricImage volImg : volumetricImages.values()) {
                final IcyBufferedImage img = volImg.getFirstNonNullImage();
                if (img != null)
                    return img;
            }
        }

        return null;
    }

    /**
     * @return the last image of last VolumetricImage
     */
    public IcyBufferedImage getLastImage() {
        final VolumetricImage volImg = getLastVolumetricImage();

        if (volImg != null)
            return volImg.getLastImage();

        return null;
    }

    /**
     * @param c if <code>(c == -1)</code> then this method is equivalent to {@link #getImage(int, int)}<br>
     *          if <code>((c == 0) || (sizeC == 1))</code> then this method is equivalent to {@link #getImage(int, int)}<br>
     *          if <code>((c &lt; 0) || (c &gt;= sizeC))</code> then it returns <code>null</code>
     * @param t int
     * @param z int
     * @return a single component image corresponding to the component c of the image
     * at time t and depth z.<br>
     * This actually create a new image which share its data with internal image
     * so any modifications to one affect the other.<br>
     * @see IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)
     * @since version 1.0.3.3b
     */
    @Override
    public IcyBufferedImage getImage(final int t, final int z, final int c) {
        final IcyBufferedImage src = getImage(t, z);

        if ((src == null) || (c == -1))
            return src;

        return src.getImage(c);
    }

    /**
     * @param loadData if <code>true</code> then we ensure that image data is loaded (in case of lazy loading) before returning the image
     * @param t        int
     * @param z        int
     * @return image at time t and depth z.
     */
    protected IcyBufferedImage getImage(final int t, final int z, final boolean loadData) {
        final VolumetricImage volImg = getVolumetricImage(t);

        if (volImg != null) {
            final IcyBufferedImage result = volImg.getImage(z);

            if ((result != null) && loadData)
                result.loadData();

            return result;
        }

        return null;
    }

    /**
     * @param t int
     * @param z int
     * @return image at time t and depth z
     */
    @Override
    public IcyBufferedImage getImage(final int t, final int z) {
        // get image (no data loading at this point)
        final IcyBufferedImage result = getImage(t, z, false);

        final int sizeZ = getSizeZ();
        final int sizeT = getSizeT();
        final int prefetchRange = 2;

        // dumb data prefetch around T
        for (int i = -prefetchRange; i <= prefetchRange; i++) {
            final int pt = t + i;

            if ((pt != t) && (pt >= 0) && (pt < sizeT))
                SequencePrefetcher.prefetch(this, pt, z);
        }
        // 3D stack ?
        if (z > 0) {
            // dumb data prefetch around current Z
            for (int i = -prefetchRange; i <= prefetchRange; i++) {
                final int pz = z + i;

                if ((pz != z) && (pz >= 0) && (pz < sizeZ))
                    SequencePrefetcher.prefetch(this, t, pz);
            }
        }

        return result;
    }

    /**
     * @param t int
     * @return all images at specified t position
     */
    public ArrayList<IcyBufferedImage> getImages(final int t) {
        final VolumetricImage volImg = getVolumetricImage(t);

        if (volImg != null)
            return volImg.getAllImage();

        return new ArrayList<>();
    }

    /**
     * @return all images of sequence in [ZT] order:<br>
     *
     * <pre>
     * T=0 Z=0
     * T=0 Z=1
     * T=0 Z=2
     * ...
     * T=1 Z=0
     * ...
     *         </pre>
     */
    public ArrayList<IcyBufferedImage> getAllImage() {
        final ArrayList<IcyBufferedImage> result = new ArrayList<>();

        synchronized (volumetricImages) {
            for (final VolumetricImage volImg : volumetricImages.values())
                result.addAll(volImg.getAllImage());
        }

        return result;
    }

    /**
     * Put an image into the specified VolumetricImage at the given z location
     *
     * @param volImg image
     * @param image  image
     * @param z      int
     */
    protected void setImage(final VolumetricImage volImg, final int z, final BufferedImage image) throws IllegalArgumentException {
        if (volImg != null) {
            // not the same image ?
            if (volImg.getImage(z) != image) {
                final IcyColorModel cm = colorModel;

                // this is different from removeImage as we don't remove empty VolumetricImage
                if (image == null)
                    volImg.removeImage(z);
                else {
                    final IcyBufferedImage icyImg;

                    // convert to icyImage if needed
                    if (image instanceof IcyBufferedImage)
                        icyImg = (IcyBufferedImage) image;
                    else
                        icyImg = IcyBufferedImage.createFrom(image);

                    // possible type change ?
                    final boolean typeChange = (cm == null) || isEmpty()
                            || ((getNumImage() == 1) && (volImg.getImage(z) != null));

                    // not changing type and not compatible
                    if (!typeChange && !isCompatible(icyImg))
                        throw new IllegalArgumentException("Sequence.setImage: image is not compatible !");

                    // we want to share the same color space for all the sequence:
                    // colormap eats a lot of memory so it's better to keep one global and we never
                    // use colormap for single image anyway. But it's important to preserve the colormodel for each
                    // image though as it store the channel bounds informations.
                    if (cm != null)
                        icyImg.setColorModel(IcyColorModel.createSharedCSInstance(cm, true));

                    // set automatic channel update from sequence
                    icyImg.setAutoUpdateChannelBounds(getAutoUpdateChannelBounds());

                    // set image
                    volImg.setImage(z, icyImg);

                    // possible type change --> virtual state may have changed
                    if (typeChange)
                        metaChanged(ID_VIRTUAL);
                }
            }
        }
    }

    /**
     * Set an image at the specified position.<br>
     * Note that the image will be transformed in IcyBufferedImage internally if needed
     *
     * @param t     T position
     * @param z     Z position
     * @param image the image to set
     */
    public void setImage(final int t, final int z, final BufferedImage image) throws IllegalArgumentException {
        final boolean volImgCreated;

        if (image == null)
            return;

        VolumetricImage volImg = getVolumetricImage(t);

        if (volImg == null) {
            volImg = setVolumetricImage(t);
            volImgCreated = true;
        }
        else
            volImgCreated = false;

        try {
            // set image
            setImage(volImg, z, image);
        }
        catch (final IllegalArgumentException e) {
            // image set failed ? remove empty image list if needed
            if (volImgCreated)
                removeAllImages(t);
            // throw exception
            throw e;
        }
    }

    /**
     * Add an image (image is added in Z dimension).<br>
     * This method is equivalent to <code>setImage(max(getSizeT() - 1, 0), getSizeZ(t), image)</code>
     *
     * @param image image
     */
    public void addImage(final BufferedImage image) throws IllegalArgumentException {
        final int t = Math.max(getSizeT() - 1, 0);

        setImage(t, getSizeZ(t), image);
    }

    /**
     * Add an image at specified T position.<br>
     * This method is equivalent to <code>setImage(t, getSizeZ(t), image)</code>
     *
     * @param image image
     * @param t     int
     */
    public void addImage(final int t, final BufferedImage image) throws IllegalArgumentException {
        setImage(t, getSizeZ(t), image);
    }

    /**
     * @param t int
     * @param z int
     * @return Remove the image at the specified position.
     */
    public boolean removeImage(final int t, final int z) {
        final VolumetricImage volImg = getVolumetricImage(t);

        if (volImg != null) {
            final boolean result;

            beginUpdate();
            try {
                result = volImg.removeImage(z);

                // empty ?
                if (volImg.isEmpty())
                    // remove it
                    removeAllImages(t);
            }
            finally {
                endUpdate();
            }

            return result;
        }

        return false;
    }

    /**
     * @param t int
     * @return Remove all images at position <code>t</code>
     */
    public boolean removeAllImages(final int t) {
        final VolumetricImage volImg;

        synchronized (volumetricImages) {
            volImg = volumetricImages.remove(Integer.valueOf(t));
        }

        // we do manual clear to dispatch events correctly
        if (volImg != null)
            volImg.clear();

        return volImg != null;
    }

    /**
     * Remove all images
     */
    public void removeAllImages() {
        beginUpdate();
        try {
            synchronized (volumetricImages) {
                while (!volumetricImages.isEmpty()) {
                    final VolumetricImage volImg = volumetricImages.pollFirstEntry().getValue();
                    // we do manual clear to dispatch events correctly
                    if (volImg != null)
                        volImg.clear();
                }
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Remove empty element of image list
     */
    public void packImageList() {
        beginUpdate();
        try {
            synchronized (volumetricImages) {
                for (final Entry<Integer, VolumetricImage> entry : volumetricImages.entrySet()) {
                    final VolumetricImage volImg = entry.getValue();
                    final int t = entry.getKey().intValue();

                    if (volImg == null) {
                        removeAllImages(t);
                    }
                    else {
                        // pack the list
                        volImg.pack();
                        // empty ? --> remove it
                        if (volImg.isEmpty())
                            removeAllImages(t);
                    }
                }
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * @return return the number of loaded image
     */
    public int getNumImage() {
        int result = 0;

        synchronized (volumetricImages) {
            for (final VolumetricImage volImg : volumetricImages.values())
                if (volImg != null)
                    result += volImg.getNumImage();
        }

        return result;
    }

    /**
     * @return return true if no image in sequence
     */
    public boolean isEmpty() {
        synchronized (volumetricImages) {
            for (final VolumetricImage volImg : volumetricImages.values())
                if ((volImg != null) && (!volImg.isEmpty()))
                    return false;
        }

        return true;
    }

    /**
     * @return true if the sequence uses default attributed name
     */
    public boolean isDefaultName() {
        return getName().startsWith(DEFAULT_NAME);
    }

    /**
     * @param index int
     * @return true is the specified channel uses default attributed name
     */
    public boolean isDefaultChannelName(final int index) {
        return StringUtil.equals(getChannelName(index), getDefaultChannelName(index));
    }

    /**
     * @return return the number of volumetricImage in the sequence
     */
    @Override
    public int getSizeT() {
        synchronized (volumetricImages) {
            if (volumetricImages.isEmpty())
                return 0;

            return volumetricImages.lastKey().intValue() + 1;
        }
    }

    /**
     * @return the global number of z stack in the sequence.
     */
    @Override
    public int getSizeZ() {
        final int sizeT = getSizeT();

        int result = 0;
        for (int i = 0; i < sizeT; i++)
            result = Math.max(result, getSizeZ(i));

        return result;
    }

    /**
     * @return the number of z stack for the volumetricImage[t].
     */
    public int getSizeZ(final int t) {
        // t = -1 means global Z size
        if (t == -1)
            return getSizeZ();

        final VolumetricImage volImg = getVolumetricImage(t);

        if (volImg != null)
            return volImg.getSize();

        return 0;
    }

    /**
     * @return the number of component/channel/band per image
     */
    @Override
    public int getSizeC() {
        final IcyColorModel cm = colorModel;

        // color model defined ? --> get it from color model
        if (cm != null)
            return cm.getNumComponents();

        // else try to get it from metadata
        return MetaDataUtil.getSizeC(metaData, 0);
    }

    /**
     * @return Same as {@link #getSizeY()}
     */
    public int getHeight() {
        return getSizeY();
    }

    /**
     * @return the height of the sequence (0 if the sequence contains no image).
     */
    @Override
    public int getSizeY() {
        // try to get from image first
        final IcyBufferedImage img = getFirstNonNullImage();

        if (img != null)
            return img.getHeight();

        // else try to get from metadata
        return MetaDataUtil.getSizeY(metaData, 0);
    }

    /**
     * @return Same as {@link #getSizeX()}
     */
    public int getWidth() {
        return getSizeX();
    }

    /**
     * @return the width of the sequence (0 if the sequence contains no image).
     */
    @Override
    public int getSizeX() {
        final IcyBufferedImage img = getFirstNonNullImage();

        // try to get it from image first
        if (img != null)
            return img.getWidth();

        // else try to get from metadata
        return MetaDataUtil.getSizeX(metaData, 0);
    }

    /**
     * @return the size of the specified dimension
     */
    public int getSize(final DimensionId dim) {
        return switch (dim) {
            case X -> getSizeX();
            case Y -> getSizeY();
            case C -> getSizeC();
            case Z -> getSizeZ();
            case T -> getSizeT();
            default -> 0;
        };
    }

    /**
     * @return 2D dimension of sequence {sizeX, sizeY}
     */
    public Dimension getDimension2D() {
        return new Dimension(getSizeX(), getSizeY());
    }

    /**
     * @return 5D dimension of sequence {sizeX, sizeY, sizeZ, sizeT, sizeC}
     */
    public Dimension5D.Integer getDimension5D() {
        return new Dimension5D.Integer(getSizeX(), getSizeY(), getSizeZ(), getSizeT(), getSizeC());
    }

    /**
     * @return 2D bounds of sequence {0, 0, sizeX, sizeY}
     * @see #getDimension2D()
     */
    public Rectangle getBounds2D() {
        return new Rectangle(getSizeX(), getSizeY());
    }

    /**
     * @return 5D bounds of sequence {0, 0, 0, 0, 0, sizeX, sizeY, sizeZ, sizeT, sizeC}
     * @see #getDimension5D()
     */
    public Rectangle5D.Integer getBounds5D() {
        return new Rectangle5D.Integer(0, 0, 0, 0, 0, getSizeX(), getSizeY(), getSizeZ(), getSizeT(), getSizeC());
    }

    /**
     * @return the number of sample.<br>
     * This is equivalent to<br>
     * <code>getSizeX() * getSizeY() * getSizeC() * getSizeZ() * getSizeT()</code>
     */
    public int getNumSample() {
        return getSizeX() * getSizeY() * getSizeC() * getSizeZ() * getSizeT();
    }

    /**
     * @param image image
     * @return Test if the specified image is compatible with current loaded images in sequence
     */
    public boolean isCompatible(final IcyBufferedImage image) {
        if ((colorModel == null) || isEmpty())
            return true;

        return (image.getWidth() == getWidth()) && (image.getHeight() == getHeight())
                && isCompatible(image.getIcyColorModel());
    }

    /**
     * @param cm color model
     * @return Test if the specified colorModel is compatible with sequence colorModel
     */
    public boolean isCompatible(final IcyColorModel cm) {
        final IcyColorModel currentCM = colorModel;

        // test that colorModel are compatible
        if (currentCM == null)
            return true;

        return currentCM.isCompatible(cm);
    }

    /**
     * @param lut LUT
     * @return true if specified LUT is compatible with sequence LUT
     */
    public boolean isLutCompatible(final LUT lut) {
        IcyColorModel cm = colorModel;
        // not yet defined ? use default one
        if (cm == null)
            cm = IcyColorModel.createInstance();

        return lut.isCompatible(cm);
    }

    /**
     * @return the colorModel
     */
    public IcyColorModel getColorModel() {
        return colorModel;
    }

    /**
     * @return Same as {@link #createCompatibleLUT()}
     */
    public LUT getDefaultLUT() {
        // color model not anymore compatible with user LUT --> reset it
        if ((defaultLut == null) || ((colorModel != null) && !defaultLut.isCompatible(colorModel)))
            defaultLut = createCompatibleLUT();

        return defaultLut;
    }

    /**
     * @return <code>true</code> if a user LUT has be defined for this sequence.
     */
    public boolean hasUserLUT() {
        return (userLut != null);
    }

    /**
     * @return the users LUT.<br>
     * If user LUT is not defined then a new default LUT is returned.
     * @see #getDefaultLUT()
     */
    public LUT getUserLUT() {
        // color model not anymore compatible with user LUT --> reset it
        if ((userLut == null) || ((colorModel != null) && !userLut.isCompatible(colorModel)))
            userLut = getDefaultLUT();

        return userLut;
    }

    /**
     * @param lut Sets the user LUT (saved in XML persistent metadata).
     */
    public void setUserLUT(final LUT lut) {
        if ((colorModel == null) || lut.isCompatible(colorModel))
            userLut = lut;
    }

    /**
     * @return Creates and returns the default LUT for this sequence.<br>
     * If the sequence is empty it returns a default ARGB LUT.
     */
    public LUT createCompatibleLUT() {
        final IcyColorModel cm = colorModel;
        final IcyColorModel result;

        // not yet defined ? use default one
        if (cm == null)
            result = IcyColorModel.createInstance();
        else
            result = IcyColorModel.createInstance(cm, true, true);

        return new LUT(result);
    }

    /**
     * @param channel channel we want to set the colormap
     * @return Get the default colormap for the specified channel
     * @see #getColorMap(int)
     */
    public IcyColorMap getDefaultColorMap(final int channel) {
        final IcyColorModel cm = colorModel;

        if (cm != null)
            return cm.getColorMap(channel);

        return getDefaultLUT().getLutChannel(channel).getColorMap();
    }

    /**
     * Set the default colormap for the specified channel
     *
     * @param channel  channel we want to set the colormap
     * @param map      source colormap to copy
     * @param setAlpha also copy the alpha information
     * @see #getDefaultColorMap(int)
     */
    public void setDefaultColormap(final int channel, final IcyColorMap map, final boolean setAlpha) {
        final IcyColorModel cm = colorModel;

        if (cm != null)
            cm.setColorMap(channel, map, setAlpha);
    }

    /**
     * Set the default colormap for the specified channel
     *
     * @param channel channel we want to set the colormap
     * @param map     source colormap to copy
     * @see #getDefaultColorMap(int)
     */
    public void setDefaultColormap(final int channel, final IcyColorMap map) {
        setDefaultColormap(channel, map, map.isAlpha());
    }

    /**
     * @param channel channel we want to set the colormap
     * @return Get the user colormap for the specified channel.<br>
     * User colormap is saved in the XML persistent data and reloaded when opening the Sequence.
     * @see #getDefaultColorMap(int)
     */
    public IcyColorMap getColorMap(final int channel) {
        final LUT lut = getUserLUT();

        if (channel < lut.getNumChannel())
            return lut.getLutChannel(channel).getColorMap();

        return null;
    }

    /**
     * Set the user colormap for the specified channel.<br>
     * User colormap is saved in the XML persistent data and reloaded when opening the Sequence.
     *
     * @param channel  channel we want to set the colormap
     * @param map      source colormap to copy
     * @param setAlpha also copy the alpha information
     * @see #getColorMap(int)
     */
    public void setColormap(final int channel, final IcyColorMap map, final boolean setAlpha) {
        final LUT lut = getUserLUT();

        if (channel < lut.getNumChannel())
            lut.getLutChannel(channel).setColorMap(map, setAlpha);
    }

    /**
     * Set the user colormap for the specified channel.<br>
     * User colormap is saved in the XML persistent data and reloaded when opening the Sequence.
     *
     * @param channel channel we want to set the colormap
     * @param map     source colormap to copy
     * @see #getColorMap(int)
     */
    public void setColormap(final int channel, final IcyColorMap map) {
        setColormap(channel, map, map.isAlpha());
    }

    /**
     * @deprecated Use {@link #getDataType()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public DataType getDataType_() {
        return getDataType();
    }

    /**
     * @return the data type of sequence
     */
    public DataType getDataType() {
        final IcyColorModel cm = colorModel;

        if (cm == null)
            return null;

        return cm.getDataType();
    }

    /**
     * @return true if this is a float data type sequence
     */
    public boolean isFloatDataType() {
        return getDataType().isFloat();
    }

    /**
     * @return true if this is a signed data type sequence
     */
    public boolean isSignedDataType() {
        return getDataType().isSigned();
    }

    /**
     * @param curBounds 2D array
     * @param bounds    2D array
     * @return Internal use only.
     */
    private static double[][] adjustBounds(final double[][] curBounds, final double[][] bounds) {
        if (bounds == null)
            return curBounds;

        for (int comp = 0; comp < bounds.length; comp++) {
            final double[] compBounds = bounds[comp];
            final double[] curCompBounds = curBounds[comp];

            if (curCompBounds[0] < compBounds[0])
                compBounds[0] = curCompBounds[0];
            if (curCompBounds[1] > compBounds[1])
                compBounds[1] = curCompBounds[1];
        }

        return bounds;
    }

    /**
     * Recalculate all image channels bounds (min and max values).<br>
     * Internal use only.
     */
    protected void recalculateAllImageChannelsBounds() {
        // nothing to do...
        if ((colorModel == null) || isEmpty())
            return;

        final List<VolumetricImage> volumes = getAllVolumetricImage();

        beginUpdate();
        try {
            // recalculate images bounds (automatically update sequence bounds with event)
            for (final VolumetricImage volImg : volumes)
                for (final IcyBufferedImage img : volImg.getAllImage())
                    img.updateChannelsBounds();
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Update channels bounds (min and max values)<br>
     * At this point we assume images has correct channels bounds information.<br>
     * Internal use only.
     */
    protected void internalUpdateChannelsBounds() {
        final IcyColorModel cm = colorModel;

        // nothing to do...
        if ((cm == null) || isEmpty())
            return;

        double[][] bounds;

        bounds = null;
        // recalculate bounds from all images
        synchronized (volumetricImages) {
            for (final VolumetricImage volImg : volumetricImages.values()) {
                for (final IcyBufferedImage img : volImg.getAllImage()) {
                    if (img != null)
                        bounds = adjustBounds(img.getChannelsTypeBounds(), bounds);
                }
            }
        }

        assert bounds != null;

        // set new computed bounds
        cm.setComponentsAbsBounds(bounds);

        bounds = null;
        // recalculate user bounds from all images
        synchronized (volumetricImages) {
            for (final VolumetricImage volImg : volumetricImages.values()) {
                for (final IcyBufferedImage img : volImg.getAllImage()) {
                    if (img != null)
                        bounds = adjustBounds(img.getChannelsBounds(), bounds);
                }
            }
        }

        assert bounds != null;

        // set new computed bounds
        cm.setComponentsUserBounds(bounds);
    }

    /**
     * Update channels bounds (min and max values).<br>
     *
     * @param forceRecalculation If true we force all images channels bounds recalculation (this can take sometime). <br>
     *                           You can left this flag to false if sequence images have their bounds updated (which
     *                           should be the case by default).
     */
    public void updateChannelsBounds(final boolean forceRecalculation) {
        // force calculation of all images bounds
        if (forceRecalculation)
            recalculateAllImageChannelsBounds();
        // then update sequence bounds
        internalUpdateChannelsBounds();
    }

    /**
     * Update channels bounds (min and max values).<br>
     * All images channels bounds are recalculated (this can take sometime).
     */
    public void updateChannelsBounds() {
        // force recalculation
        updateChannelsBounds(true);
    }

    /**
     * @return Get the data type minimum value.
     */
    public double getDataTypeMin() {
        return getDataType().getMinValue();
    }

    /**
     * @return Get the data type maximum value.
     */
    public double getDataTypeMax() {
        return getDataType().getMaxValue();
    }

    /**
     * @return Get data type bounds (min and max values).
     */
    public double[] getDataTypeBounds() {
        return new double[]{getDataTypeMin(), getDataTypeMax()};
    }

    /**
     * @param channel int
     * @return Get the preferred data type minimum value in the whole sequence for the specified channel.
     */
    public double getChannelTypeMin(final int channel) {
        final IcyColorModel cm = colorModel;

        if (cm == null)
            return 0d;

        return cm.getComponentAbsMinValue(channel);
    }

    /**
     * @param channel int
     * @return Get the preferred data type maximum value in the whole sequence for the specified channel.
     */
    public double getChannelTypeMax(final int channel) {
        final IcyColorModel cm = colorModel;

        if (cm == null)
            return 0d;

        return cm.getComponentAbsMaxValue(channel);
    }

    /**
     * @param channel int
     * @return Get the preferred data type bounds (min and max values) in the whole sequence for the
     * specified channel.
     */
    public double[] getChannelTypeBounds(final int channel) {
        final IcyColorModel cm = colorModel;

        if (cm == null)
            return new double[]{0d, 0d};

        return cm.getComponentAbsBounds(channel);
    }

    /**
     * @return Get the preferred data type bounds (min and max values) in the whole sequence for all
     * channels.
     */
    public double[][] getChannelsTypeBounds() {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelTypeBounds(c);

        return result;
    }

    /**
     * @return Get the global preferred data type bounds (min and max values) for all channels.
     */
    public double[] getChannelsGlobalTypeBounds() {
        final int sizeC = getSizeC();
        final double[] result = getChannelTypeBounds(0);

        for (int c = 1; c < sizeC; c++) {
            final double[] bounds = getChannelTypeBounds(c);
            result[0] = Math.min(bounds[0], result[0]);
            result[1] = Math.max(bounds[1], result[1]);
        }

        return result;
    }

    /**
     * @param channel int
     * @return Get the minimum value in the whole sequence for the specified channel.
     */
    public double getChannelMin(final int channel) {
        if (colorModel == null)
            return 0d;

        return colorModel.getComponentUserMinValue(channel);
    }

    /**
     * @param channel int
     * @return Get maximum value in the whole sequence for the specified channel.
     */
    public double getChannelMax(final int channel) {
        final IcyColorModel cm = colorModel;

        if (cm == null)
            return 0d;

        return cm.getComponentUserMaxValue(channel);
    }

    /**
     * @param channel int
     * @return Get bounds (min and max values) in the whole sequence for the specified channel.
     */
    public double[] getChannelBounds(final int channel) {
        final IcyColorModel cm = colorModel;

        if (cm == null)
            return new double[]{0d, 0d};

        // lazy channel bounds update
        if (channelBoundsInvalid) {
            channelBoundsInvalid = false;
            // images channels bounds are valid at this point
            internalUpdateChannelsBounds();
        }

        return cm.getComponentUserBounds(channel);
    }

    /**
     * @return Get bounds (min and max values) in the whole sequence for all channels.
     */
    public double[][] getChannelsBounds() {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelBounds(c);

        return result;
    }

    /**
     * @return Get global bounds (min and max values) in the whole sequence for all channels.
     */
    public double[] getChannelsGlobalBounds() {
        final int sizeC = getSizeC();
        final double[] result = new double[2];

        result[0] = Double.MAX_VALUE;
        result[1] = -Double.MAX_VALUE;

        for (int c = 0; c < sizeC; c++) {
            final double[] bounds = getChannelBounds(c);

            if (bounds[0] < result[0])
                result[0] = bounds[0];
            if (bounds[1] > result[1])
                result[1] = bounds[1];
        }

        return result;
    }

    /**
     * Force all image data to be loaded (so channels bounds can be correctly computed).<br>
     * Be careful, this function can take sometime.
     */
    public void loadAllData() {
        for (final IcyBufferedImage image : getAllImage())
            if (image != null)
                image.loadData();
    }

    /**
     * @param x int
     * @param y int
     * @param c int
     * @param z int
     * @param t int
     * @return the data value located at position (t, z, c, y, x) as double.<br>
     * It returns 0d if value is not found.
     */
    public double getData(final int t, final int z, final int c, final int y, final int x) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getData(x, y, c);

        return 0d;
    }

    /**
     * @param x int
     * @param y int
     * @param c int
     * @param z int
     * @param t int
     * @return the data value located at position (t, z, c, y, x) as double.<br>
     * The value is interpolated depending the current double (x,y,z) coordinates.<br>
     * It returns 0d if value is out of range.
     */
    public double getDataInterpolated(final int t, final double z, final int c, final double y, final double x) {
        final int zi = (int) z;
        final double ratioNextZ = z - (double) zi;

        double result = 0d;
        IcyBufferedImage img;

        img = getImage(t, zi);
        if (img != null) {
            final double ratioCurZ = 1d - ratioNextZ;
            if (ratioCurZ > 0d)
                result += img.getDataInterpolated(x, y, c) * ratioCurZ;
        }
        img = getImage(t, zi + 1);
        if (img != null) {
            if (ratioNextZ > 0d)
                result += img.getDataInterpolated(x, y, c) * ratioNextZ;
        }

        return result;
    }

    /**
     * @return a direct reference to 4D array data [T][Z][C][XY]
     */
    public Object getDataXYCZT() {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataXYCZTAsByte();
            case SHORT -> getDataXYCZTAsShort();
            case INT -> getDataXYCZTAsInt();
            case FLOAT -> getDataXYCZTAsFloat();
            case DOUBLE -> getDataXYCZTAsDouble();
            default -> null;
        };
    }

    /**
     * @param t int
     * @return a direct reference to 3D array data [Z][C][XY] for specified t
     */
    public Object getDataXYCZ(final int t) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataXYCZAsByte(t);
            case SHORT -> getDataXYCZAsShort(t);
            case INT -> getDataXYCZAsInt(t);
            case FLOAT -> getDataXYCZAsFloat(t);
            case DOUBLE -> getDataXYCZAsDouble(t);
            default -> null;
        };
    }

    /**
     * @param t int
     * @param z int
     * @return a direct reference to 2D array data [C][XY] for specified t, z
     */
    public Object getDataXYC(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYC();

        return null;
    }

    /**
     * @param c int
     * @param t int
     * @param z int
     * @return a direct reference to 1D array data [XY] for specified t, z, c
     */
    public Object getDataXY(final int t, final int z, final int c) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXY(c);

        return null;
    }

    /**
     * @param c int
     * @return a direct reference to 3D byte array data [T][Z][XY] for specified c
     */
    public Object getDataXYZT(final int c) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataXYZTAsByte(c);
            case SHORT -> getDataXYZTAsShort(c);
            case INT -> getDataXYZTAsInt(c);
            case FLOAT -> getDataXYZTAsFloat(c);
            case DOUBLE -> getDataXYZTAsDouble(c);
            default -> null;
        };
    }

    /**
     * @param c int
     * @param t int
     * @return a direct reference to 2D byte array data [Z][XY] for specified t, c
     */
    public Object getDataXYZ(final int t, final int c) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataXYZAsByte(t, c);
            case SHORT -> getDataXYZAsShort(t, c);
            case INT -> getDataXYZAsInt(t, c);
            case FLOAT -> getDataXYZAsFloat(t, c);
            case DOUBLE -> getDataXYZAsDouble(t, c);
            default -> null;
        };
    }

    /**
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]
     */
    public Object getDataCopyXYCZT() {
        return getDataCopyXYCZT(null, 0);
    }

    /**
     * @param out object
     * @param off int
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYCZT(final Object out, final int off) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyXYCZTAsByte((byte[]) out, off);
            case SHORT -> getDataCopyXYCZTAsShort((short[]) out, off);
            case INT -> getDataCopyXYCZTAsInt((int[]) out, off);
            case FLOAT -> getDataCopyXYCZTAsFloat((float[]) out, off);
            case DOUBLE -> getDataCopyXYCZTAsDouble((double[]) out, off);
            default -> null;
        };
    }

    /**
     * @param t int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public Object getDataCopyXYCZ(final int t) {
        return getDataCopyXYCZ(t, null, 0);
    }

    /**
     * @param out object
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYCZ(final int t, final Object out, final int off) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyXYCZAsByte(t, (byte[]) out, off);
            case SHORT -> getDataCopyXYCZAsShort(t, (short[]) out, off);
            case INT -> getDataCopyXYCZAsInt(t, (int[]) out, off);
            case FLOAT -> getDataCopyXYCZAsFloat(t, (float[]) out, off);
            case DOUBLE -> getDataCopyXYCZAsDouble(t, (double[]) out, off);
            default -> null;
        };
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z
     */
    public Object getDataCopyXYC(final int t, final int z) {
        return getDataCopyXYC(t, z, null, 0);
    }

    /**
     * @param out object
     * @param off int
     * @param z   int
     * @param t   int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYC(final int t, final int z, final Object out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYC(out, off);

        return out;
    }

    /**
     * @param c int
     * @param t int
     * @param z int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c
     */
    public Object getDataCopyXY(final int t, final int z, final int c) {
        return getDataCopyXY(t, z, c, null, 0);
    }

    /**
     * @param out object
     * @param off int
     * @param t   int
     * @param c   int
     * @param z   int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXY(final int t, final int z, final int c, final Object out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXY(c, out, off);

        return out;
    }

    /**
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]
     */
    public Object getDataCopyCXYZT() {
        return getDataCopyCXYZT(null, 0);
    }

    /**
     * @param out object
     * @param off int
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyCXYZT(final Object out, final int off) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyCXYZTAsByte((byte[]) out, off);
            case SHORT -> getDataCopyCXYZTAsShort((short[]) out, off);
            case INT -> getDataCopyCXYZTAsInt((int[]) out, off);
            case FLOAT -> getDataCopyCXYZTAsFloat((float[]) out, off);
            case DOUBLE -> getDataCopyCXYZTAsDouble((double[]) out, off);
            default -> null;
        };
    }

    /**
     * @param t int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public Object getDataCopyCXYZ(final int t) {
        return getDataCopyCXYZ(t, null, 0);
    }

    /**
     * @param out object
     * @param off int
     * @param t   int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyCXYZ(final int t, final Object out, final int off) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyCXYZAsByte(t, (byte[]) out, off);
            case SHORT -> getDataCopyCXYZAsShort(t, (short[]) out, off);
            case INT -> getDataCopyCXYZAsInt(t, (int[]) out, off);
            case FLOAT -> getDataCopyCXYZAsFloat(t, (float[]) out, off);
            case DOUBLE -> getDataCopyCXYZAsDouble(t, (double[]) out, off);
            default -> null;
        };
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z
     */
    public Object getDataCopyCXY(final int t, final int z) {
        return getDataCopyCXY(t, z, null, 0);
    }

    /**
     * @param out Object
     * @param off int
     * @param t   int
     * @param z   int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyCXY(final int t, final int z, final Object out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCXY(out, off);

        return out;
    }

    /**
     * @param x int
     * @param y int
     * @param z int
     * @param t int
     * @return a 1D array data copy [C] of specified t, z, x, y
     */
    public Object getDataCopyC(final int t, final int z, final int x, final int y) {
        return getDataCopyC(t, z, x, y, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param x   int
     * @param y   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [C] of specified t, z, x, y<br>
     */
    public Object getDataCopyC(final int t, final int z, final int x, final int y, final Object out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyC(x, y, out, off);

        return out;
    }

    /**
     * @param c int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c
     */
    public Object getDataCopyXYZT(final int c) {
        return getDataCopyXYZT(c, null, 0);
    }

    /**
     * @param c   int
     * @param out object
     * @param off int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYZT(final int c, final Object out, final int off) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyXYZTAsByte(c, (byte[]) out, off);
            case SHORT -> getDataCopyXYZTAsShort(c, (short[]) out, off);
            case INT -> getDataCopyXYZTAsInt(c, (int[]) out, off);
            case FLOAT -> getDataCopyXYZTAsFloat(c, (float[]) out, off);
            case DOUBLE -> getDataCopyXYZTAsDouble(c, (double[]) out, off);
            default -> null;
        };
    }

    /**
     * @param c int
     * @param t int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c
     */
    public Object getDataCopyXYZ(final int t, final int c) {
        return getDataCopyXYZ(t, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @param c   int
     * @param t   int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c<br>
     */
    public Object getDataCopyXYZ(final int t, final int c, final Object out, final int off) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyXYZAsByte(t, c, (byte[]) out, off);
            case SHORT -> getDataCopyXYZAsShort(t, c, (short[]) out, off);
            case INT -> getDataCopyXYZAsInt(t, c, (int[]) out, off);
            case FLOAT -> getDataCopyXYZAsFloat(t, c, (float[]) out, off);
            case DOUBLE -> getDataCopyXYZAsDouble(t, c, (double[]) out, off);
            default -> null;
        };
    }

    /**
     * @return a direct reference to 4D byte array data [T][Z][C][XY]
     */
    public byte[][][][] getDataXYCZTAsByte() {
        final int sizeT = getSizeT();
        final byte[][][][] result = new byte[sizeT][][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYCZAsByte(t);

        return result;

    }

    /**
     * @return a direct reference to 4D byte array data [T][Z][C][XY]
     */
    public short[][][][] getDataXYCZTAsShort() {
        final int sizeT = getSizeT();
        final short[][][][] result = new short[sizeT][][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYCZAsShort(t);

        return result;
    }

    /**
     * @return a direct reference to 4D byte array data [T][Z][C][XY]
     */
    public int[][][][] getDataXYCZTAsInt() {
        final int sizeT = getSizeT();
        final int[][][][] result = new int[sizeT][][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYCZAsInt(t);

        return result;
    }

    /**
     * @return a direct reference to 4D byte array data [T][Z][C][XY]
     */
    public float[][][][] getDataXYCZTAsFloat() {
        final int sizeT = getSizeT();
        final float[][][][] result = new float[sizeT][][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYCZAsFloat(t);

        return result;
    }

    /**
     * @return a direct reference to 4D byte array data [T][Z][C][XY]
     */
    public double[][][][] getDataXYCZTAsDouble() {
        final int sizeT = getSizeT();
        final double[][][][] result = new double[sizeT][][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYCZAsDouble(t);

        return result;
    }

    /**
     * @param t int
     * @return a direct reference to 3D byte array data [Z][C][XY] for specified t
     */
    public byte[][][] getDataXYCZAsByte(final int t) {
        final int sizeZ = getSizeZ(t);
        final byte[][][] result = new byte[sizeZ][][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYCAsByte(t, z);

        return result;
    }

    /**
     * @param t int
     * @return a direct reference to 3D byte array data [Z][C][XY] for specified t
     */
    public short[][][] getDataXYCZAsShort(final int t) {
        final int sizeZ = getSizeZ(t);
        final short[][][] result = new short[sizeZ][][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYCAsShort(t, z);

        return result;
    }

    /**
     * @param t int
     * @return a direct reference to 3D byte array data [Z][C][XY] for specified t
     */
    public int[][][] getDataXYCZAsInt(final int t) {
        final int sizeZ = getSizeZ(t);
        final int[][][] result = new int[sizeZ][][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYCAsInt(t, z);

        return result;
    }

    /**
     * @param t int
     *          Returns a direct reference to 3D byte array data [Z][C][XY] for specified t
     */
    public float[][][] getDataXYCZAsFloat(final int t) {
        final int sizeZ = getSizeZ(t);
        final float[][][] result = new float[sizeZ][][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYCAsFloat(t, z);

        return result;
    }

    /**
     * @param t int
     * @return a direct reference to 3D byte array data [Z][C][XY] for specified t
     */
    public double[][][] getDataXYCZAsDouble(final int t) {
        final int sizeZ = getSizeZ(t);
        final double[][][] result = new double[sizeZ][][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYCAsDouble(t, z);

        return result;
    }

    /**
     * @param t int
     * @param z int
     * @return a direct reference to 2D byte array data [C][XY] for specified t, z
     */
    public byte[][] getDataXYCAsByte(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYCAsByte();

        return null;
    }

    /**
     * @param z int
     * @param t int
     * @return a direct reference to 2D byte array data [C][XY] for specified t, z
     */
    public short[][] getDataXYCAsShort(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYCAsShort();

        return null;
    }

    /**
     * @param t int
     * @param z int
     * @return a direct reference to 2D byte array data [C][XY] for specified t, z
     */
    public int[][] getDataXYCAsInt(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYCAsInt();

        return null;
    }

    /**
     * @param z int
     * @param t int
     * @return a direct reference to 2D byte array data [C][XY] for specified t, z
     */
    public float[][] getDataXYCAsFloat(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYCAsFloat();

        return null;
    }

    /**
     * @param t int
     * @param z int
     * @return a direct reference to 2D byte array data [C][XY] for specified t, z
     */
    public double[][] getDataXYCAsDouble(final int t, final int z) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYCAsDouble();

        return null;
    }

    /**
     * @param z int
     * @param t int
     * @param c int
     * @return a direct reference to 1D byte array data [XY] for specified t, z, c
     */
    public byte[] getDataXYAsByte(final int t, final int z, final int c) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYAsByte(c);

        return null;
    }

    /**
     * @param c int
     * @param t int
     * @param z int
     * @return a direct reference to 1D byte array data [XY] for specified t, z, c
     */
    public short[] getDataXYAsShort(final int t, final int z, final int c) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYAsShort(c);

        return null;
    }

    /**
     * @param z int
     * @param t int
     * @param c int
     * @return a direct reference to 1D byte array data [XY] for specified t, z, c
     */
    public int[] getDataXYAsInt(final int t, final int z, final int c) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYAsInt(c);

        return null;
    }

    /**
     * @param c int
     * @param t int
     * @param z int
     * @return a direct reference to 1D byte array data [XY] for specified t, z, c
     */
    public float[] getDataXYAsFloat(final int t, final int z, final int c) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYAsFloat(c);

        return null;
    }

    /**
     * @param z int
     * @param t int
     * @param c int
     * @return a direct reference to 1D byte array data [XY] for specified t, z, c
     */
    public double[] getDataXYAsDouble(final int t, final int z, final int c) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataXYAsDouble(c);

        return null;
    }

    /**
     * @param c int
     * @return a direct reference to 3D byte array data [T][Z][XY] for specified c
     */
    public byte[][][] getDataXYZTAsByte(final int c) {
        final int sizeT = getSizeT();
        final byte[][][] result = new byte[sizeT][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYZAsByte(t, c);

        return result;
    }

    /**
     * @param c int
     * @return a direct reference to 3D byte array data [T][Z][XY] for specified c
     */
    public short[][][] getDataXYZTAsShort(final int c) {
        final int sizeT = getSizeT();
        final short[][][] result = new short[sizeT][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYZAsShort(t, c);

        return result;
    }

    /**
     * @param c int
     * @return a direct reference to 3D byte array data [T][Z][XY] for specified c
     */
    public int[][][] getDataXYZTAsInt(final int c) {
        final int sizeT = getSizeT();
        final int[][][] result = new int[sizeT][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYZAsInt(t, c);

        return result;
    }

    /**
     * @param c int
     * @return a direct reference to 3D byte array data [T][Z][XY] for specified c
     */
    public float[][][] getDataXYZTAsFloat(final int c) {
        final int sizeT = getSizeT();
        final float[][][] result = new float[sizeT][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYZAsFloat(t, c);

        return result;
    }

    /**
     * @param c int
     * @return a direct reference to 3D byte array data [T][Z][XY] for specified c
     */
    public double[][][] getDataXYZTAsDouble(final int c) {
        final int sizeT = getSizeT();
        final double[][][] result = new double[sizeT][][];

        for (int t = 0; t < sizeT; t++)
            result[t] = getDataXYZAsDouble(t, c);

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a direct reference to 2D byte array data [Z][XY] for specified t, c
     */
    public byte[][] getDataXYZAsByte(final int t, final int c) {
        final int sizeZ = getSizeZ(t);
        final byte[][] result = new byte[sizeZ][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYAsByte(t, z, c);

        return result;
    }

    /**
     * @param t int
     * @param c int
     * @return a direct reference to 2D byte array data [Z][XY] for specified t, c
     */
    public short[][] getDataXYZAsShort(final int t, final int c) {
        final int sizeZ = getSizeZ(t);
        final short[][] result = new short[sizeZ][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYAsShort(t, z, c);

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a direct reference to 2D byte array data [Z][XY] for specified t, c
     */
    public int[][] getDataXYZAsInt(final int t, final int c) {
        final int sizeZ = getSizeZ(t);
        final int[][] result = new int[sizeZ][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYAsInt(t, z, c);

        return result;
    }

    /**
     * @param t int
     * @param c int
     * @return a direct reference to 2D byte array data [Z][XY] for specified t, c
     */
    public float[][] getDataXYZAsFloat(final int t, final int c) {
        final int sizeZ = getSizeZ(t);
        final float[][] result = new float[sizeZ][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYAsFloat(t, z, c);

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a direct reference to 2D byte array data [Z][XY] for specified t, c
     */
    public double[][] getDataXYZAsDouble(final int t, final int c) {
        final int sizeZ = getSizeZ(t);
        final double[][] result = new double[sizeZ][];

        for (int z = 0; z < sizeZ; z++)
            result[z] = getDataXYAsDouble(t, z, c);

        return result;
    }

    /**
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]
     */
    public byte[] getDataCopyXYCZTAsByte() {
        return getDataCopyXYCZTAsByte(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public byte[] getDataCopyXYCZTAsByte(final byte[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYCZAsByte(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]
     */
    public short[] getDataCopyXYCZTAsShort() {
        return getDataCopyXYCZTAsShort(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public short[] getDataCopyXYCZTAsShort(final short[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYCZAsShort(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]
     */
    public int[] getDataCopyXYCZTAsInt() {
        return getDataCopyXYCZTAsInt(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public int[] getDataCopyXYCZTAsInt(final int[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYCZAsInt(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return Returns a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]
     */
    public float[] getDataCopyXYCZTAsFloat() {
        return getDataCopyXYCZTAsFloat(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public float[] getDataCopyXYCZTAsFloat(final float[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYCZAsFloat(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]
     */
    public double[] getDataCopyXYCZTAsDouble() {
        return getDataCopyXYCZTAsDouble(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [XYCZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public double[] getDataCopyXYCZTAsDouble(final double[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYCZAsDouble(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public byte[] getDataCopyXYCZAsByte(final int t) {
        return getDataCopyXYCZAsByte(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public byte[] getDataCopyXYCZAsByte(final int t, final byte[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYCAsByte(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public short[] getDataCopyXYCZAsShort(final int t) {
        return getDataCopyXYCZAsShort(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @param t   int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public short[] getDataCopyXYCZAsShort(final int t, final short[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYCAsShort(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public int[] getDataCopyXYCZAsInt(final int t) {
        return getDataCopyXYCZAsInt(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public int[] getDataCopyXYCZAsInt(final int t, final int[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYCAsInt(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public float[] getDataCopyXYCZAsFloat(final int t) {
        return getDataCopyXYCZAsFloat(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public float[] getDataCopyXYCZAsFloat(final int t, final float[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYCAsFloat(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public double[] getDataCopyXYCZAsDouble(final int t) {
        return getDataCopyXYCZAsDouble(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYCZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public double[] getDataCopyXYCZAsDouble(final int t, final double[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYCAsDouble(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z
     */
    public byte[] getDataCopyXYCAsByte(final int t, final int z) {
        return getDataCopyXYCAsByte(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public byte[] getDataCopyXYCAsByte(final int t, final int z, final byte[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYCAsByte(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z
     */
    public short[] getDataCopyXYCAsShort(final int t, final int z) {
        return getDataCopyXYCAsShort(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public short[] getDataCopyXYCAsShort(final int t, final int z, final short[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYCAsShort(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z
     */
    public int[] getDataCopyXYCAsInt(final int t, final int z) {
        return getDataCopyXYCAsInt(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public int[] getDataCopyXYCAsInt(final int t, final int z, final int[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYCAsInt(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z
     */
    public float[] getDataCopyXYCAsFloat(final int t, final int z) {
        return getDataCopyXYCAsFloat(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public float[] getDataCopyXYCAsFloat(final int t, final int z, final float[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYCAsFloat(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z
     */
    public double[] getDataCopyXYCAsDouble(final int t, final int z) {
        return getDataCopyXYCAsDouble(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return Returns a 1D array data copy [XYC] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public double[] getDataCopyXYCAsDouble(final int t, final int z, final double[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYCAsDouble(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param c int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c
     */
    public byte[] getDataCopyXYAsByte(final int t, final int z, final int c) {
        return getDataCopyXYAsByte(t, z, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c<br>
     */
    public byte[] getDataCopyXYAsByte(final int t, final int z, final int c, final byte[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYAsByte(c, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param c int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c
     */
    public short[] getDataCopyXYAsShort(final int t, final int z, final int c) {
        return getDataCopyXYAsShort(t, z, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c<br>
     */
    public short[] getDataCopyXYAsShort(final int t, final int z, final int c, final short[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYAsShort(c, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param c int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c
     */
    public int[] getDataCopyXYAsInt(final int t, final int z, final int c) {
        return getDataCopyXYAsInt(t, z, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c<br>
     */
    public int[] getDataCopyXYAsInt(final int t, final int z, final int c, final int[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYAsInt(c, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param c int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c
     */
    public float[] getDataCopyXYAsFloat(final int t, final int z, final int c) {
        return getDataCopyXYAsFloat(t, z, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c<br>
     */
    public float[] getDataCopyXYAsFloat(final int t, final int z, final int c, final float[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYAsFloat(c, out, off);

        return out;
    }

    /**
     * @param c int
     * @param t int
     * @param z int
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c
     */
    public double[] getDataCopyXYAsDouble(final int t, final int z, final int c) {
        return getDataCopyXYAsDouble(t, z, c, null, 0);
    }

    /**
     * @param z   int
     * @param t   int
     * @param c   int
     * @param off int
     * @param out If (out != null) then it's used to store result at the specified offset
     * @return a 1D array data copy [XY] of internal 1D array data [XY] for specified t, z, c<br>
     */
    public double[] getDataCopyXYAsDouble(final int t, final int z, final int c, final double[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyXYAsDouble(c, out, off);

        return out;
    }

    /**
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]
     */
    public byte[] getDataCopyCXYZTAsByte() {
        return getDataCopyCXYZTAsByte(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public byte[] getDataCopyCXYZTAsByte(final byte[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyCXYZAsByte(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]
     */
    public short[] getDataCopyCXYZTAsShort() {
        return getDataCopyCXYZTAsShort(null, 0);
    }

    /**
     * @param off If (out != null) then it's used to store result at the specified offset
     * @param out int
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public short[] getDataCopyCXYZTAsShort(final short[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyCXYZAsShort(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]
     */
    public int[] getDataCopyCXYZTAsInt() {
        return getDataCopyCXYZTAsInt(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public int[] getDataCopyCXYZTAsInt(final int[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyCXYZAsInt(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]
     */
    public float[] getDataCopyCXYZTAsFloat() {
        return getDataCopyCXYZTAsFloat(null, 0);
    }

    /**
     * @param off If (out != null) then it's used to store result at the specified offset
     * @param out int
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public float[] getDataCopyCXYZTAsFloat(final float[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyCXYZAsFloat(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]
     */
    public double[] getDataCopyCXYZTAsDouble() {
        return getDataCopyCXYZTAsDouble(null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param off int
     * @return a 1D array data copy [CXYZT] of internal 4D array data [T][Z][C][XY]<br>
     */
    public double[] getDataCopyCXYZTAsDouble(final double[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyCXYZAsDouble(t, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public byte[] getDataCopyCXYZAsByte(final int t) {
        return getDataCopyCXYZAsByte(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public byte[] getDataCopyCXYZAsByte(final int t, final byte[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyCXYAsByte(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public short[] getDataCopyCXYZAsShort(final int t) {
        return getDataCopyCXYZAsShort(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public short[] getDataCopyCXYZAsShort(final int t, final short[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyCXYAsShort(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public int[] getDataCopyCXYZAsInt(final int t) {
        return getDataCopyCXYZAsInt(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public int[] getDataCopyCXYZAsInt(final int t, final int[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyCXYAsInt(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public float[] getDataCopyCXYZAsFloat(final int t) {
        return getDataCopyCXYZAsFloat(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public float[] getDataCopyCXYZAsFloat(final int t, final float[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyCXYAsFloat(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t
     */
    public double[] getDataCopyCXYZAsDouble(final int t) {
        return getDataCopyCXYZAsDouble(t, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXYZ] of internal 3D array data [Z][C][XY] for specified t<br>
     */
    public double[] getDataCopyCXYZAsDouble(final int t, final double[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeC();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyCXYAsDouble(t, z, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z
     */
    public byte[] getDataCopyCXYAsByte(final int t, final int z) {
        return getDataCopyCXYAsByte(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public byte[] getDataCopyCXYAsByte(final int t, final int z, final byte[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCXYAsByte(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z
     */
    public short[] getDataCopyCXYAsShort(final int t, final int z) {
        return getDataCopyCXYAsShort(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public short[] getDataCopyCXYAsShort(final int t, final int z, final short[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCXYAsShort(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z
     */
    public int[] getDataCopyCXYAsInt(final int t, final int z) {
        return getDataCopyCXYAsInt(t, z, null, 0);
    }

    /**
     * @param z   int
     * @param t   int
     * @param off int
     * @param out int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCXYAsInt(final int t, final int z, final int[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCXYAsInt(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z
     */
    public float[] getDataCopyCXYAsFloat(final int t, final int z) {
        return getDataCopyCXYAsFloat(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public float[] getDataCopyCXYAsFloat(final int t, final int z, final float[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCXYAsFloat(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z
     */
    public double[] getDataCopyCXYAsDouble(final int t, final int z) {
        return getDataCopyCXYAsDouble(t, z, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [CXY] of internal 2D array data [C][XY] for specified t, z<br>
     */
    public double[] getDataCopyCXYAsDouble(final int t, final int z, final double[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCXYAsDouble(out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param y int
     * @param x int
     * @return a 1D array data copy [C] of specified t, z, x, y
     */
    public byte[] getDataCopyCAsByte(final int t, final int z, final int x, final int y) {
        return getDataCopyCAsByte(t, z, x, y, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param x   int
     * @param y   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [C] of specified t, z, x, y<br>
     */
    public byte[] getDataCopyCAsByte(final int t, final int z, final int x, final int y, final byte[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCAsByte(x, y, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param y int
     * @param x int
     * @return a 1D array data copy [C] of specified t, z, x, y
     */
    public short[] getDataCopyCAsShort(final int t, final int z, final int x, final int y) {
        return getDataCopyCAsShort(t, z, x, y, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param x   int
     * @param y   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [C] of specified t, z, x, y<br>
     */
    public short[] getDataCopyCAsShort(final int t, final int z, final int x, final int y, final short[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCAsShort(x, y, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param y int
     * @param x int
     * @return a 1D array data copy [C] of specified t, z, x, y
     */
    public int[] getDataCopyCAsInt(final int t, final int z, final int x, final int y) {
        return getDataCopyCAsInt(t, z, x, y, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param x   int
     * @param y   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [C] of specified t, z, x, y<br>
     */
    public int[] getDataCopyCAsInt(final int t, final int z, final int x, final int y, final int[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCAsInt(x, y, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param y int
     * @param x int
     * @return a 1D array data copy [C] of specified t, z, x, y
     */
    public float[] getDataCopyCAsFloat(final int t, final int z, final int x, final int y) {
        return getDataCopyCAsFloat(t, z, x, y, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param x   int
     * @param y   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [C] of specified t, z, x, y<br>
     */
    public float[] getDataCopyCAsFloat(final int t, final int z, final int x, final int y, final float[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCAsFloat(x, y, out, off);

        return out;
    }

    /**
     * @param t int
     * @param z int
     * @param y int
     * @param x int
     * @return a 1D array data copy [C] of specified t, z, x, y
     */
    public double[] getDataCopyCAsDouble(final int t, final int z, final int x, final int y) {
        return getDataCopyCAsDouble(t, z, x, y, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param x   int
     * @param y   int
     * @param z   int
     * @param t   int
     * @param off int
     * @return a 1D array data copy [C] of specified t, z, x, y<br>
     */
    public double[] getDataCopyCAsDouble(final int t, final int z, final int x, final int y, final double[] out, final int off) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            return img.getDataCopyCAsDouble(x, y, out, off);

        return out;
    }

    /**
     * @param c int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c
     */
    public byte[] getDataCopyXYZTAsByte(final int c) {
        return getDataCopyXYZTAsByte(c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c<br>
     */
    public byte[] getDataCopyXYZTAsByte(final int c, final byte[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYZAsByte(t, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c
     */
    public short[] getDataCopyXYZTAsShort(final int c) {
        return getDataCopyXYZTAsShort(c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c<br>
     */
    public short[] getDataCopyXYZTAsShort(final int c, final short[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYZAsShort(t, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c
     */
    public int[] getDataCopyXYZTAsInt(final int c) {
        return getDataCopyXYZTAsInt(c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c<br>
     */
    public int[] getDataCopyXYZTAsInt(final int c, final int[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYZAsInt(t, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c
     */
    public float[] getDataCopyXYZTAsFloat(final int c) {
        return getDataCopyXYZTAsFloat(c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c<br>
     */
    public float[] getDataCopyXYZTAsFloat(final int c, final float[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYZAsFloat(t, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c
     */
    public double[] getDataCopyXYZTAsDouble(final int c) {
        return getDataCopyXYZTAsDouble(c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZT] of internal 3D array data [T][Z][XY] for specified c<br>
     */
    public double[] getDataCopyXYZTAsDouble(final int c, final double[] out, final int off) {
        final long sizeT = getSizeT();
        final long len = (long) getSizeX() * (long) getSizeY() * (long) getSizeZ();
        if ((len * sizeT) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeT));
        int offset = off;

        for (int t = 0; t < sizeT; t++) {
            getDataCopyXYZAsDouble(t, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c
     */
    public byte[] getDataCopyXYZAsByte(final int t, final int c) {
        return getDataCopyXYZAsByte(t, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c<br>
     */
    public byte[] getDataCopyXYZAsByte(final int t, final int c, final byte[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYAsByte(t, z, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c
     */
    public short[] getDataCopyXYZAsShort(final int t, final int c) {
        return getDataCopyXYZAsShort(t, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c<br>
     */
    public short[] getDataCopyXYZAsShort(final int t, final int c, final short[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYAsShort(t, z, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c
     */
    public int[] getDataCopyXYZAsInt(final int t, final int c) {
        return getDataCopyXYZAsInt(t, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c<br>
     */
    public int[] getDataCopyXYZAsInt(final int t, final int c, final int[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYAsInt(t, z, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c
     */
    public float[] getDataCopyXYZAsFloat(final int t, final int c) {
        return getDataCopyXYZAsFloat(t, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param c   int
     * @param off int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c<br>
     */
    public float[] getDataCopyXYZAsFloat(final int t, final int c, final float[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYAsFloat(t, z, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * @param c int
     * @param t int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c
     */
    public double[] getDataCopyXYZAsDouble(final int t, final int c) {
        return getDataCopyXYZAsDouble(t, c, null, 0);
    }

    /**
     * @param out If (out != null) then it's used to store result at the specified offset
     * @param t   int
     * @param c   int
     * @return a 1D array data copy [XYZ] of internal 2D array data [Z][XY] for specified t, c<br>
     */
    public double[] getDataCopyXYZAsDouble(final int t, final int c, final double[] out, final int off) {
        final long sizeZ = getSizeZ();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeZ) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeZ));
        int offset = off;

        for (int z = 0; z < sizeZ; z++) {
            getDataCopyXYAsDouble(t, z, c, result, offset);
            offset += len;
        }

        return result;
    }

    /**
     * Sets 1D array data [XY] for specified t, z, c
     *
     * @param t     int
     * @param c     int
     * @param z     int
     * @param value object
     */
    public void setDataXY(final int t, final int z, final int c, final Object value) {
        final IcyBufferedImage img = getImage(t, z);

        if (img != null)
            img.setDataXY(c, value);
    }

    /**
     * @return Retrieve all custom ROI properties (map of (Key,Value)).
     */
    public Map<String, String> getProperties() {
        return new HashMap<>(properties);
    }

    /**
     * @param name Property name.<br>
     *             Note that it can be default property name (as {@value #PROPERTY_POSITION_X}) in which case the value will be
     *             returned in String format if possible or launch an {@link IllegalArgumentException} when not possible.
     * @return Retrieve a Sequence property value.<br>
     * Returns <code>null</code> if the property value is empty.
     */
    public String getProperty(final String name) {
        if (name == null)
            return null;

        // ignore case for property name
        final String adjName = name.toLowerCase();

        if (StringUtil.equals(adjName, PROPERTY_ID))
            return Integer.toString(getId());
        if (StringUtil.equals(adjName, PROPERTY_NAME))
            return getName();
        if (StringUtil.equals(adjName, PROPERTY_POSITION_X))
            return Double.toString(getPositionX());
        if (StringUtil.equals(adjName, PROPERTY_POSITION_Y))
            return Double.toString(getPositionY());
        if (StringUtil.equals(adjName, PROPERTY_POSITION_Z))
            return Double.toString(getPositionZ());
        if (StringUtil.equals(adjName, PROPERTY_POSITION_T))
            return Long.toString(getPositionT());
        if (StringUtil.equals(adjName, PROPERTY_PIXEL_SIZE_X))
            return Double.toString(getPixelSizeX());
        if (StringUtil.equals(adjName, PROPERTY_PIXEL_SIZE_Y))
            return Double.toString(getPixelSizeY());
        if (StringUtil.equals(adjName, PROPERTY_PIXEL_SIZE_Z))
            return Double.toString(getPixelSizeZ());
        if (StringUtil.equals(adjName, PROPERTY_TIME_INTERVAL))
            return Double.toString(getTimeInterval());
        if (StringUtil.equals(adjName, PROPERTY_USER_NAME))
            return getUserName();
        if (StringUtil.equals(adjName, PROPERTY_VIRTUAL))
            return Boolean.toString(isVirtual());

        if (StringUtil.equals(adjName, PROPERTY_CHANNEL_NAME) || StringUtil.equals(adjName, PROPERTY_POSITION_T_OFFSET))
            throw new IllegalArgumentException("Cannot return value of property '" + adjName + "' as String");

        synchronized (properties) {
            return properties.get(adjName);
        }
    }

    /**
     * Generic way to set Sequence property value.
     *
     * @param name  Property name.<br>
     *              Note that it can be default property name (as {@value #PROPERTY_POSITION_X}) in which case the value will be
     *              set in String format if possible or launch an {@link IllegalArgumentException} when not possible.
     * @param value the value to set in the property (for instance "0.0" for {@link #PROPERTY_POSITION_X})
     */
    public void setProperty(final String name, final String value) {
        if (name == null)
            return;

        // ignore case for property name
        final String adjName = name.toLowerCase();

        if (StringUtil.equals(adjName, PROPERTY_NAME))
            setName(value);
        if (StringUtil.equals(adjName, PROPERTY_POSITION_X))
            setPositionX(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_POSITION_Y))
            setPositionY(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_POSITION_Z))
            setPositionZ(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_POSITION_T))
            setPositionT(Long.valueOf(value).longValue());
        if (StringUtil.equals(adjName, PROPERTY_PIXEL_SIZE_X))
            setPixelSizeX(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_PIXEL_SIZE_Y))
            setPixelSizeY(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_PIXEL_SIZE_Z))
            setPixelSizeZ(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_TIME_INTERVAL))
            setTimeInterval(Double.valueOf(value).doubleValue());
        if (StringUtil.equals(adjName, PROPERTY_VIRTUAL))
            setVirtual(Boolean.valueOf(value).booleanValue());

        if (StringUtil.equals(adjName, PROPERTY_ID) || StringUtil.equals(adjName, PROPERTY_CHANNEL_NAME)
                || StringUtil.equals(adjName, PROPERTY_POSITION_T_OFFSET))
            throw new IllegalArgumentException("Cannot set value of property '" + adjName + "'");

        synchronized (properties) {
            properties.put(adjName, value);
        }

        metaChanged(adjName);
    }

    /**
     * Load XML persistent data from file.<br>
     * This method should only be called once when the sequence has just be loaded from file.<br>
     * Note that it uses {@link #getFilename()} to define the XML filename so be sure that it is correctly filled before
     * calling this method.
     *
     * @return <code>true</code> if XML data has been correctly loaded, <code>false</code> otherwise.
     */
    public boolean loadXMLData() {
        return persistent.loadXMLData();
    }

    /**
     * Synchronize XML data with sequence data :<br>
     * This function refresh all the meta data and ROIs of the sequence and put it in the current
     * XML document.
     */
    public void refreshXMLData() {
        persistent.refreshXMLData();
    }

    /**
     * @return Save attached XML data.
     */
    public boolean saveXMLData() {
        Exception exc = null;
        int retry = 0;

        // definitely ugly but the XML parser may throw some exception in multi thread environnement
        // and we really don't want to lost the sequence metadata !
        while (retry < 5) {
            try {
                return persistent.saveXMLData();
            }
            catch (final Exception e) {
                exc = e;
            }

            retry++;
        }

        System.err.println("Error while saving Sequence XML persistent data :");
        IcyExceptionHandler.showErrorMessage(exc, true);

        return false;
    }

    /**
     * @param name name of node
     * @return {@link Node} if the specified XML data node exist
     * @see #getNode(String)
     */
    public Node isNodeExisting(final String name) {
        return persistent.getNode(name);
    }

    /**
     * @param name name of wanted node
     * @return Get XML data node identified by specified name.<br>
     * The node is created if needed.<br>
     * Note that the following node names are reserved: <i>image, name, meta, rois, lut</i><br>
     * @see #isNodeExisting(String)
     */
    public Node getNode(final String name) {
        final Node result = persistent.getNode(name);

        if (result == null)
            return persistent.setNode(name);

        return result;
    }

    @Override
    public String toString() {
        return String.format("Sequence: %s - %d x %d x %d x %d - %d ch (%s)", getName(), getSizeX(), getSizeY(), getSizeZ(), getSizeT(), getSizeC(), getDataType());
    }

    /**
     * Do common job on "image add" here
     *
     * @param image image
     */
    public void onImageAdded(final IcyBufferedImage image) {
        // colorModel not yet defined ?
        if (colorModel == null)
            // define it from the image colorModel
            setColorModel(IcyColorModel.createInstance(image.getIcyColorModel(), true, true));

        // add listener to image
        image.addListener(this);

        // notify changed
        dataChanged(image, SequenceEventType.ADDED);
    }

    /**
     * Do common job on "image replaced" here
     *
     * @param newImage image
     * @param oldImage image
     */
    public void onImageReplaced(final IcyBufferedImage oldImage, final IcyBufferedImage newImage) {
        // we replaced the only present image
        final boolean typeChange = getNumImage() == 1;
        final IcyColorModel cm = colorModel;

        beginUpdate();
        try {
            if (typeChange) {
                // colorModel not compatible ?
                if (!isCompatible(newImage.getIcyColorModel()))
                    // define it from the new image colorModel
                    setColorModel(IcyColorModel.createInstance(newImage.getIcyColorModel(), true, true));
                    // only inform about a type change if sequence sizeX and sizeY changed
                else if ((oldImage.getSizeX() != newImage.getSizeX()) || (oldImage.getSizeY() != newImage.getSizeY()))
                    typeChanged();
            }

            // TODO: improve cleaning here
            // need that to avoid memory leak as we manually patch the image colorspace
            if (cm != null)
                cm.getIcyColorSpace().removeListener(oldImage.getIcyColorModel());
            // remove listener from old image
            oldImage.removeListener(this);
            // notify about old image remove
            dataChanged(oldImage, SequenceEventType.REMOVED);

            // add listener to new image
            newImage.addListener(this);
            // notify about new image added
            dataChanged(newImage, SequenceEventType.ADDED);
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Do common job on "image remove" here
     *
     * @param image image
     */
    public void onImageRemoved(final IcyBufferedImage image) {
        // TODO: improve cleaning here
        // need that to avoid memory leak as we manually patch the image colorspace
        if (colorModel != null)
            colorModel.getIcyColorSpace().removeListener(image.getIcyColorModel());
        // remove listener from image
        image.removeListener(this);

        // no more image ? --> release the global colorModel
        if (isEmpty())
            setColorModel(null);

        // notify changed
        dataChanged(image, SequenceEventType.REMOVED);
    }

    /**
     * fire change event
     *
     * @param e event
     */
    protected void fireChangedEvent(final SequenceEvent e) {
        final List<SequenceListener> cachedListeners = new ArrayList<>(listeners);

        for (final SequenceListener listener : cachedListeners)
            listener.sequenceChanged(e);

        // provide backward compatibility for painter
        if (e.getSourceType() == SequenceEventSourceType.SEQUENCE_OVERLAY) {
            final Painter painter;

            if (e.getSource() instanceof OverlayWrapper)
                painter = ((OverlayWrapper) e.getSource()).getPainter();
            else
                painter = (Painter) e.getSource();

            final SequenceEvent event = new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_PAINTER, painter,
                    e.getType(), e.getParam());

            for (final SequenceListener listener : cachedListeners)
                listener.sequenceChanged(event);
        }
    }

    /**
     * fire close event
     */
    protected void fireClosedEvent() {
        for (final SequenceListener listener : new ArrayList<>(listeners))
            listener.sequenceClosed(this);
    }

    /**
     * fire model image changed event
     */
    @Override
    public void fireModelImageChangedEvent() {
        for (final SequenceModelListener listener : new ArrayList<>(modelListeners))
            listener.imageChanged();
    }

    /**
     * fire model dimension changed event
     */
    @Override
    public void fireModelDimensionChangedEvent() {
        for (final SequenceModelListener listener : new ArrayList<>(modelListeners))
            listener.dimensionChanged();
    }

    public void beginUpdate() {
        updater.beginUpdate();
    }

    public void endUpdate() {
        updater.endUpdate();

        // no more updating
        if (!updater.isUpdating()) {
            // lazy channel bounds update
            if (channelBoundsInvalid) {
                channelBoundsInvalid = false;
                // images channels bounds are valid at this point
                internalUpdateChannelsBounds();
            }
        }
    }

    public boolean isUpdating() {
        return updater.isUpdating();
    }

    /**
     * @param metaName sequence meta has changed
     */
    public void metaChanged(final String metaName) {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_META, metaName));
    }

    /**
     * @param metaName sequence meta has changed
     * @param param    int
     */
    public void metaChanged(final String metaName, final int param) {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_META, metaName, null, param));
    }

    /**
     * sequence type (colorModel, size) changed
     */
    protected void typeChanged() {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_TYPE));
    }

    /**
     * @param colorModel sequence colorMap changed
     * @param component  component
     */
    protected void colormapChanged(final IcyColorModel colorModel, final int component) {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_COLORMAP, colorModel, component));
    }

    /**
     * @param colorModel sequence component bounds changed
     * @param component  component
     */
    protected void componentBoundsChanged(final IcyColorModel colorModel, final int component) {
        updater.changed(
                new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_COMPONENTBOUNDS, colorModel, component));
    }

    /**
     * @param overlay overlay painter has changed
     * @param type    sequence event
     */
    protected void overlayChanged(final Overlay overlay, final SequenceEventType type) {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_OVERLAY, overlay, type));
    }

    /**
     * @param overlay Notify specified painter of overlay has changed (the sequence should contains the specified
     *                Overlay)
     */
    public void overlayChanged(final Overlay overlay) {
        if (contains(overlay))
            overlayChanged(overlay, SequenceEventType.CHANGED);
    }

    /**
     * @param event Called when an overlay has changed (internal method).<br>
     *              Use {@link #overlayChanged(Overlay)} instead.
     */
    @Override
    public void overlayChanged(final OverlayEvent event) {
        // only take care about overlay painter change here (need redraw)
        if (event.getType() == OverlayEventType.PAINTER_CHANGED)
            overlayChanged(event.getSource(), SequenceEventType.CHANGED);
    }

    /**
     * @param roi Notify specified roi has changed (the sequence should contains the specified ROI)
     */
    public void roiChanged(final ROI roi) {
        if (contains(roi))
            roiChanged(roi, SequenceEventType.CHANGED);
    }

    /**
     * @param roi  Notify specified roi has changed
     * @param type sequence event
     */
    protected void roiChanged(final ROI roi, final SequenceEventType type) {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_ROI, roi, type));
    }

    /**
     * Data has changed (global change)<br>
     * Be careful, this implies all component bounds are recalculated, can be heavy !
     */
    public void dataChanged() {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_DATA, null));
    }

    /**
     * @param image data has changed
     * @param type  sequence event
     */
    protected void dataChanged(final IcyBufferedImage image, final SequenceEventType type) {
        updater.changed(new SequenceEvent(this, SequenceEventSourceType.SEQUENCE_DATA, image, type, 0));
    }

    @Override
    public void colorModelChanged(final IcyColorModelEvent e) {
        switch (e.getType()) {
            case COLORMAP_CHANGED:
                colormapChanged(e.getColorModel(), e.getComponent());
                break;

            case SCALER_CHANGED:
                componentBoundsChanged(e.getColorModel(), e.getComponent());
                break;
        }
    }

    @Override
    public void imageChanged(final IcyBufferedImageEvent e) {
        final IcyBufferedImage image = e.getImage();

        switch (e.getType()) {
            case BOUNDS_CHANGED:
                // update sequence channel bounds
                if (autoUpdateChannelBounds) {
                    // updating sequence ? delay update
                    if (isUpdating())
                        channelBoundsInvalid = true;
                    else
                        // refresh sequence channel bounds from images bounds
                        internalUpdateChannelsBounds();
                }
                break;

            case COLORMAP_CHANGED:
                // ignore that, we don't care about image colormap
                break;

            case DATA_CHANGED:
                // image data changed
                dataChanged(image, SequenceEventType.CHANGED);
                break;
        }
    }

    @Override
    public void roiChanged(final ROIEvent event) {
        // notify the ROI has changed
        roiChanged(event.getSource(), SequenceEventType.CHANGED);
    }

    /**
     * @param e process on sequence change
     */
    @Override
    public void onChanged(final CollapsibleEvent e) {
        final SequenceEvent event = (SequenceEvent) e;

        switch (event.getSourceType()) {
            // do here global process on sequence data change
            case SEQUENCE_DATA:
                // automatic channel bounds update enabled
                if (autoUpdateChannelBounds) {
                    // generic CHANGED event
                    if (event.getSource() == null)
                        // recalculate all images bounds (automatically update sequence bounds in imageChange event)
                        recalculateAllImageChannelsBounds();

                    // refresh sequence channel bounds from images bounds
                    internalUpdateChannelsBounds();
                }

                // fire SequenceModel event
                fireModelImageChangedEvent();
                break;

            // do here global process on sequence type change
            case SEQUENCE_TYPE:
                // fire SequenceModel event
                fireModelDimensionChangedEvent();
                break;

            // do here global process on sequence colormap change
            case SEQUENCE_COLORMAP:
                break;

            // do here global process on sequence component bounds change
            case SEQUENCE_COMPONENTBOUNDS:
                break;

            // do here global process on sequence overlay change
            case SEQUENCE_OVERLAY:
                break;

            // do here global process on sequence ROI change
            case SEQUENCE_ROI:
                break;
        }

        // notify listener we have changed
        fireChangedEvent(event);
    }
}
