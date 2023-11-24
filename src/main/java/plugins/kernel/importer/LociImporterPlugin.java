/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package plugins.kernel.importer;

import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ProgressListener;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.dialog.LoaderDialog.AllImagesFileFilter;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.image.ImageUtil;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.LinearColorMap;
import icy.plugin.abstract_.PluginSequenceFileImporter;
import icy.preferences.GeneralPreferences;
import icy.sequence.MetaDataUtil;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ByteArrayConvert;
import icy.type.rectangle.Rectangle2DUtil;
import icy.util.ColorUtil;
import icy.util.OMEUtil;
import icy.util.StringUtil;
import jxl.biff.drawing.PNGReader;
import loci.formats.*;
import loci.formats.gui.AWTImageTools;
import loci.formats.gui.ExtensionFileFilter;
import loci.formats.in.*;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * LOCI Bio-Formats library importer class.
 *
 * @author Stephane
 */
public class LociImporterPlugin extends PluginSequenceFileImporter {
    protected static class LociAllFileFilter extends AllImagesFileFilter {
        @Override
        public String getDescription() {
            return "All image files / Bio-Formats";
        }
    }

    /**
     * Used for multi thread tile image reading.
     *
     * @author Stephane
     */
    class LociTilePixelsReader {
        static class TilePixelsWorkBuffer {
            final byte[] rawBuffer;
            final byte[] channelBuffer;
            final Object pixelBuffer;

            public TilePixelsWorkBuffer(final int sizeX, final int sizeY, final int rgbChannel, final DataType dataType) {
                super();

                // allocate arrays
                rawBuffer = new byte[sizeX * sizeY * rgbChannel * dataType.getSize()];
                channelBuffer = new byte[sizeX * sizeY * dataType.getSize()];
                pixelBuffer = Array1DUtil.createArray(dataType, sizeX * sizeY);
            }
        }

        class TilePixelsReaderWorker implements Runnable {
            final Rectangle region;
            boolean done;
            boolean failed;

            public TilePixelsReaderWorker(final Rectangle region) {
                super();

                this.region = region;
                done = false;
                failed = false;
            }

            @Override
            public void run() {
                Object pixels;

                try {
                    // get reader and working buffers
                    final IFormatReader r = getReader();
                    final TilePixelsWorkBuffer buf = buffers.pop();

                    try {
                        try {
                            pixels = getPixelsInternal(r, region, z, t, c, false, downScaleLevel, buf.rawBuffer, buf.channelBuffer, buf.pixelBuffer);
                        }
                        finally {
                            // release reader
                            releaseReader(r);
                        }

                        // need to adjust input region
                        final Rectangle adjRegion = new Rectangle(region);
                        int downScale = downScaleLevel;
                        while (downScale > 0) {
                            // reduce region size by a factor of 2
                            adjRegion.setBounds(adjRegion.x / 2, adjRegion.y / 2, adjRegion.width / 2, adjRegion.height / 2);
                            downScale--;
                        }

                        // define destination in destination
                        final Point pt = adjRegion.getLocation();
                        pt.translate(-imageRegion.x, -imageRegion.y);

                        // copy tile to result
                        Array1DUtil.copyRect(pixels, adjRegion.getSize(), null, result, imageRegion.getSize(), pt, signed);
                    }
                    finally {
                        // release working buffer
                        buffers.push(buf);
                    }
                }
                catch (final Exception e) {
                    failed = true;
                }

                done = true;
            }
        }

        // final image region
        final Rectangle imageRegion;
        // required image down scaling
        final int downScaleLevel;
        // resolution shift divider
        final int resShift;
        final int z;
        final int t;
        final int c;
        final boolean signed;
        final Object result;
        final Stack<TilePixelsWorkBuffer> buffers;

        public LociTilePixelsReader(final int series, final int resolution, final Rectangle region, final int z, final int t, final int c, final int tileW, final int tileH, final ProgressListener listener) throws IOException, UnsupportedFormatException {
            super();

            this.z = z;
            this.t = t;
            this.c = c;

            final OMEXMLMetadata meta = getOMEXMLMetaData();
            final int sizeX = MetaDataUtil.getSizeX(meta, series);
            final int sizeY = MetaDataUtil.getSizeY(meta, series);
            final DataType type = MetaDataUtil.getDataType(meta, series);
            signed = type.isSigned();

            // define XY region to load
            Rectangle adjRegion = new Rectangle(sizeX, sizeY);
            if (region != null)
                adjRegion = adjRegion.intersection(region);

            // prepare main reader and get needed downScale
            downScaleLevel = prepareReader(series, resolution);
            // real resolution shift used by reader
            resShift = getResolutionShift();

            // adapt region size to final image resolution
            imageRegion = new Rectangle(adjRegion.x >> resolution, adjRegion.y >> resolution, adjRegion.width >> resolution, adjRegion.height >> resolution);
            // adapt region size to reader resolution
            adjRegion = new Rectangle(adjRegion.x >> resShift, adjRegion.y >> resShift, adjRegion.width >> resShift, adjRegion.height >> resShift);

            // allocate result (adapted to final wanted resolution)
            result = Array1DUtil.createArray(type, imageRegion.width * imageRegion.height);

            // allocate working buffers
            final int rgbChannelCount = reader.getRGBChannelCount();

            int tw = tileW;
            int th = tileH;

            // adjust tile size if needed
            if (tw <= 0)
                tw = getTileWidth(series);
            if (tw <= 0)
                tw = 512;
            if (th <= 0)
                th = getTileHeight(series);
            if (th <= 0)
                th = 512;

            final int numThread = Math.max(1, SystemUtil.getNumberOfCPUs() - 1);

            buffers = new Stack<>();
            for (int i = 0; i < numThread; i++)
                buffers.push(new TilePixelsWorkBuffer(tw, th, rgbChannelCount, type));

            // create processor
            try (final Processor readerProcessor = new Processor(numThread)) {
                readerProcessor.setThreadName("Pixels tile reader");

                // get all required tiles
                final List<Rectangle> tiles = ImageUtil.getTileList(adjRegion, tw, th);

                // submit all tasks
                for (final Rectangle tile : tiles) {
                    // wait a bit if the process queue is full
                    while (readerProcessor.isFull()) {
                        try {
                            Thread.sleep(0);
                        }
                        catch (final InterruptedException e) {
                            // interrupt all processes
                            readerProcessor.shutdownNow();
                            break;
                        }
                    }

                    // submit next task
                    readerProcessor.submit(new TilePixelsReaderWorker(tile.intersection(adjRegion)));

                    // display progression
                    if (listener != null) {
                        // process cancel requested ?
                        if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size())) {
                            // interrupt processes
                            readerProcessor.shutdownNow();
                            break;
                        }
                    }
                }

                // wait for completion
                while (readerProcessor.isProcessing()) {
                    try {
                        Thread.sleep(1);
                    }
                    catch (final InterruptedException e) {
                        // interrupt all processes
                        readerProcessor.shutdownNow();
                        break;
                    }

                    // display progression
                    if (listener != null) {
                        // process cancel requested ?
                        if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size())) {
                            // interrupt processes
                            readerProcessor.shutdownNow();
                            break;
                        }
                    }
                }

                // last wait for completion just in case we were interrupted
                readerProcessor.waitAll();
            }

            // faster memory release
            buffers.clear();
        }
    }

    /**
     * Used for multi thread tile image reading.
     *
     * @author Stephane
     */
    class LociTileImageReader {
        static class TileImageWorkBuffer {
            final byte[] rawBuffer;
            final byte[] channelBuffer;
            final Object[] pixelBuffer;

            public TileImageWorkBuffer(final int sizeX, final int sizeY, final int sizeC, final int rgbChannel, final DataType dataType) {
                super();

                // allocate arrays
                rawBuffer = new byte[sizeX * sizeY * rgbChannel * dataType.getSize()];
                channelBuffer = new byte[sizeX * sizeY * dataType.getSize()];
                pixelBuffer = Array2DUtil.createArray(dataType, sizeC);
                for (int i = 0; i < sizeC; i++)
                    Objects.requireNonNull(pixelBuffer)[i] = Array1DUtil.createArray(dataType, sizeX * sizeY);
            }
        }

        class TileImageReaderWorker implements Runnable {
            final Rectangle region;
            boolean done;
            boolean failed;

            public TileImageReaderWorker(final Rectangle region) {
                super();

                this.region = region;
                done = false;
                failed = false;
            }

            @Override
            public void run() {
                IcyBufferedImage img;

                try {
                    // get reader and working buffers
                    final IFormatReader r = getReader();
                    final TileImageWorkBuffer buf = buffers.pop();

                    try {
                        try {
                            // get image tile
                            if (c == -1) {
                                img = getImageInternal(r, region, z, t, false, downScaleLevel, buf.rawBuffer, buf.channelBuffer, buf.pixelBuffer);
                                // colormaps not yet set ?
                                if (colormaps[0] == null) {
                                    for (int c = 0; c < img.getSizeC(); c++)
                                        colormaps[c] = img.getColorMap(c);
                                }
                            }
                            else {
                                img = getImageInternal(r, region, z, t, c, false, downScaleLevel, buf.rawBuffer, buf.channelBuffer, Objects.requireNonNull(buf.pixelBuffer)[0]);
                                // colormap not yet set ?
                                if (colormaps[0] == null)
                                    colormaps[0] = img.getColorMap(0);
                            }
                        }
                        finally {
                            // release reader
                            releaseReader(r);
                        }

                        // define destination point in destination
                        final Point pt = new Point(region.x >> downScaleLevel, region.y >> downScaleLevel);
                        pt.translate(-imageRegion.x, -imageRegion.y);

                        // copy tile to image result
                        result.copyData(img, null, pt);
                    }
                    finally {
                        // release working buffer
                        buffers.push(buf);
                    }
                }
                catch (final Exception e) {
                    failed = true;
                }

                done = true;
            }
        }

        // final image region
        final Rectangle imageRegion;
        // required image down scaling
        final int downScaleLevel;
        // resolution shift divider
        final int resShift;
        final int z;
        final int t;
        final int c;
        final IcyBufferedImage result;
        final IcyColorMap[] colormaps;
        final Stack<TileImageWorkBuffer> buffers;

        public LociTileImageReader(final int series, final int resolution, final Rectangle region, final int z, final int t, final int c, final int tileW,
                                   final int tileH, final ProgressListener listener) throws IOException, UnsupportedFormatException {
            super();

            this.z = z;
            this.t = t;
            this.c = c;

            final OMEXMLMetadata meta = getOMEXMLMetaData();
            final int sizeX = MetaDataUtil.getSizeX(meta, series);
            final int sizeY = MetaDataUtil.getSizeY(meta, series);
            final DataType type = MetaDataUtil.getDataType(meta, series);
            final int sizeC = (c == -1) ? MetaDataUtil.getSizeC(meta, series) : 1;

            // define XY region to load
            Rectangle adjRegion = new Rectangle(sizeX, sizeY);
            if (region != null)
                adjRegion = adjRegion.intersection(region);

            // prepare main reader and get needed downScale
            downScaleLevel = prepareReader(series, resolution);
            // real resolution shift used by reader
            resShift = getResolutionShift();

            // adapt region size to final image resolution
            imageRegion = new Rectangle(adjRegion.x >> resolution, adjRegion.y >> resolution,
                    adjRegion.width >> resolution, adjRegion.height >> resolution);
            // adapt region size to reader resolution
            adjRegion = new Rectangle(adjRegion.x >> resShift, adjRegion.y >> resShift, adjRegion.width >> resShift,
                    adjRegion.height >> resShift);

            // allocate result (adapted to final wanted resolution)
            result = new IcyBufferedImage(imageRegion.width, imageRegion.height, sizeC, type);
            // allocate colormaps
            colormaps = new IcyColorMap[sizeC];
            // allocate working buffers
            final int rgbChannelCount = reader.getRGBChannelCount();

            int tw = tileW;
            int th = tileH;

            // adjust tile size if needed
            if (tw <= 0)
                tw = getTileWidth(series);
            if (tw <= 0)
                tw = 512;
            if (th <= 0)
                th = getTileHeight(series);
            if (th <= 0)
                th = 512;

            final int numThread = Math.max(1, SystemUtil.getNumberOfCPUs() - 1);

            buffers = new Stack<>();
            for (int i = 0; i < numThread; i++)
                buffers.push(new TileImageWorkBuffer(tw, th, sizeC, rgbChannelCount, type));

            // create processor
            try (final Processor readerProcessor = new Processor(numThread)) {

                readerProcessor.setThreadName("Image tile reader");

                // force working in RAM as we will do many write operations (too slow with cache)
                result.setVolatile(false);
                // to avoid multiple update
                result.beginUpdate();

                try {
                    // get all required tiles
                    final List<Rectangle> tiles = ImageUtil.getTileList(adjRegion, tw, th);

                    // submit all tasks
                    for (final Rectangle tile : tiles) {
                        // wait a bit if the process queue is full
                        while (readerProcessor.isFull()) {
                            try {
                                Thread.sleep(0);
                            }
                            catch (final InterruptedException e) {
                                // interrupt all processes
                                readerProcessor.shutdownNow();
                                break;
                            }
                        }

                        // submit next task
                        readerProcessor.submit(new TileImageReaderWorker(tile.intersection(adjRegion)));

                        // display progression
                        if (listener != null) {
                            // process cancel requested ?
                            if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size())) {
                                // interrupt processes
                                readerProcessor.shutdownNow();
                                break;
                            }
                        }
                    }

                    // wait for completion
                    while (readerProcessor.isProcessing()) {
                        try {
                            Thread.sleep(1);
                        }
                        catch (final InterruptedException e) {
                            // interrupt all processes
                            readerProcessor.shutdownNow();
                            break;
                        }

                        // display progression
                        if (listener != null) {
                            // process cancel requested ?
                            if (!listener.notifyProgress(readerProcessor.getCompletedTaskCount(), tiles.size())) {
                                // interrupt processes
                                readerProcessor.shutdownNow();
                                break;
                            }
                        }
                    }

                    // last wait for completion just in case we were interrupted
                    readerProcessor.waitAll();
                }
                finally {
                    result.endUpdate();
                }
            }

            // set back colormap
            for (int i = 0; i < colormaps.length; i++)
                result.setColorMap(i, colormaps[i], true);

            // restore volatile state
            result.setVolatile(GeneralPreferences.getVirtualMode());

            // faster memory release
            buffers.clear();
        }
    }

    /**
     * Main image reader used to retrieve a specific format reader
     */
    protected final ImageReader mainReader;
    /**
     * Current active reader, it can be a {@link TileStitcher} reader wrapper or the <i>internalReader</i> depending <i>groupFiles</i> state
     */
    protected IFormatReader reader;
    /**
     * Current format reader (just for test and cloning reader operation)O
     */
    protected IFormatReader internalReader;
    /**
     * Current format reader (just for accept test, it can be changed by accept(..) method)
     */
    protected IFormatReader acceptReader;

    /**
     * Shared readers for multi threading
     */
    protected final List<IFormatReader> readersPool;

    /**
     * Metadata options
     */
    protected final DynamicMetadataOptions options;
    /**
     * Advanced settings
     */
    protected boolean originalMetadata;
    protected boolean groupFiles;

    /**
     * internal resolution levels
     */
    protected int[] resolutions;
    /**
     * Internal opened path (Bio-formats does not always keep track of it)
     */
    protected String openedPath;
    /**
     * Internal last used open flags
     */
    protected int openFlags;

    public LociImporterPlugin() {
        super();

        mainReader = new ImageReader();
        // just to be sure
        mainReader.setAllowOpenFiles(true);

        reader = null;
        internalReader = null;
        acceptReader = null;
        readersPool = new ArrayList<>();

        options = new DynamicMetadataOptions();

        options.setMetadataLevel(MetadataLevel.NO_OVERLAYS);
        // we don't care about validation
        options.setValidate(false);
        // disable ND2 native chunk map to avoid some issues with ND2 file
        // TODO: remove when fixed in Bio-formats
        // See images\bio\format\nd2\droplet\ko --> should be a timelaps and not an image series
        options.setBoolean(NativeND2Reader.USE_CHUNKMAP_KEY, Boolean.FALSE);

        originalMetadata = false;
        groupFiles = true;
        resolutions = null;
        openedPath = null;
        openFlags = 0;
    }

    protected void setReader(final Class<? extends IFormatReader> readerClass) throws FormatException {
        IFormatReader newReader = internalReader;

        // no reader defined or not compatible..
        if (!readerClass.isInstance(newReader))
            newReader = mainReader.getReader(readerClass);

        setReaderInternal(newReader);
    }

    protected void setReader(final String path) throws FormatException, IOException {
        IFormatReader newReader = internalReader;

        // no reader defined so just get the good one
        if (newReader == null)
            newReader = mainReader.getReader(path);
        else {
            // don't check if the file is currently opened
            if (!isOpen(path)) {
                // try to check only with extension first then open it if needed
                // Note that OME TIFF can be opened with the classic TIFF reader
                if (!internalReader.isThisType(path, false) && !internalReader.isThisType(path, true))
                    newReader = mainReader.getReader(path);
            }
        }

        setReaderInternal(newReader);
    }

    protected void setReaderInternal(final IFormatReader newReader) {
        // reader changed ?
        if (internalReader != newReader) {
            // update it
            internalReader = newReader;
            // update 'accept' reader
            acceptReader = newReader;
            // use TileStitcher wrapper when grouping is ON
            if (groupFiles)
                reader = TileStitcher.makeTileStitcher(newReader);
            else
                reader = newReader;
        }
    }

    protected void reportError(final String title, final String message, final String filename) {
        // TODO: enable that when LOCI will be ready
        // ThreadUtil.invokeLater(new Runnable()
        // {
        // @Override
        // public void run()
        // {
        // final ErrorReportFrame errorFrame = new ErrorReportFrame(null, title, message);
        //
        // errorFrame.setReportAction(new ActionListener()
        // {
        // @Override
        // public void actionPerformed(ActionEvent e)
        // {
        // try
        // {
        // OMEUtil.reportLociError(filename, errorFrame.getReportMessage());
        // }
        // catch (BadLocationException e1)
        // {
        // System.err.println("Error while sending report:");
        // IcyExceptionHandler.showErrorMessage(e1, false, true);
        // }
        // }
        // });
        // }
        // });
    }

    /**
     * When set to <code>true</code> the importer will also read original metadata (as
     * annotations)
     *
     * @return the readAllMetadata state<br>
     * @see #setReadOriginalMetadata(boolean)
     */
    public boolean getReadOriginalMetadata() {
        return originalMetadata;
    }

    /**
     * When set to <code>true</code> the importer will also read original metadata (as annotations).
     */
    public void setReadOriginalMetadata(final boolean value) {
        originalMetadata = value;
    }

    /**
     * When set to <code>true</code> the importer will try to group files required for the whole
     * dataset.
     *
     * @return the groupFiles
     */
    public boolean isGroupFiles() {
        return groupFiles;
    }

    /**
     * When set to <code>true</code> the importer will try to group files required for the whole
     * dataset.
     */
    public void setGroupFiles(final boolean value) {
        groupFiles = value;
    }

    @Override
    @NotNull
    public List<FileFilter> getFileFilters() {
        final List<FileFilter> result = new ArrayList<>();

        result.add(new LociAllFileFilter());
        result.add(new ExtensionFileFilter(new String[]{"tif", "tiff"}, "TIFF images / Bio-Formats"));
        result.add(new ExtensionFileFilter(new String[]{"png"}, "PNG images / Bio-Formats"));
        result.add(new ExtensionFileFilter(new String[]{"jpg", "jpeg"}, "JPEG images / Bio-Formats"));
        result.add(new ExtensionFileFilter(new String[]{"avi"}, "AVI videos / Bio-Formats"));

        // final IFormatReader[] readers = mainReader.getReaders();

        // for (IFormatReader reader : readers)
        // result.add(new FormatFileFilter(reader, true));

        return result;
    }

    @Override
    public boolean acceptFile(final String path) {
        // easy discard
        if (Loader.canDiscardImageFile(path))
            return false;

        // better for Bio-Formats to have system path format (bug with Bio-Format ?)
        final String adjPath = new File(path).getAbsolutePath();

        while (true) {
            try {
                // this method should not modify the current reader so we use a specific reader for that :)
                if ((acceptReader == null)
                        || (!acceptReader.isThisType(adjPath, false) && !acceptReader.isThisType(adjPath, true)))
                    acceptReader = mainReader.getReader(adjPath);

                return true;
            }
            catch (final ClosedByInterruptException e) {
                // we don't accept this interrupt here --> remove interrupted state & retry
                Thread.interrupted();
            }
            catch (final Exception e) {
                // assume false on exception (FormatException or IOException)
                return false;
            }
        }
    }

    public boolean isOpen(final String path) {
        return StringUtil.equals(getOpened(), FileUtil.getGenericPath(path));
    }

    @Override
    public String getOpened() {
        return openedPath;

        // if (reader != null)
        // return FileUtil.getGenericPath(reader.getCurrentFile());
        //
        // return null;
    }

    @Override
    public boolean open(@Nullable final String path, final int flags) throws UnsupportedFormatException, IOException {
        return openWith(path, flags, null);
    }

    /**
     * Open the image designed by the specified file <code>path</code> to allow image data / metadata access.<br>
     * Calling this method will automatically close the previous opened image.<br>
     * Don't forget to call {@link #close()} to close the image when you're done.<br>
     *
     * @param path        Path of the image file to open.
     * @param flags       operation flag:<br>
     *                    <ul>
     *                    <li>{@link #FLAG_METADATA_MINIMUM} = load minimum metadata informations</li>
     *                    <li>{@link #FLAG_METADATA_ALL} = load all metadata informations</li>
     *                    </ul>
     * @param readerClass the IFormatReader class to use to open the dataset (can be null).
     * @return <code>true</code> if the operation has succeeded and <code>false</code> otherwise.
     */
    public boolean openWith(final String path, final int flags, final Class<? extends IFormatReader> readerClass) throws UnsupportedFormatException, IOException {
        // already opened ?
        if (isOpen(path))
            return true;

        // close first
        close();

        try {
            // better for Bio-Formats to have system path format
            final String adjPath = new File(path).getAbsolutePath();

            // force reader class
            if (readerClass != null)
                setReader(readerClass);
                // determine reader from file
            else
                setReader(adjPath);

            // then open it
            openReader(reader, adjPath, flags);

            // set reader in reader pool
            synchronized (readersPool) {
                readersPool.add(reader);
            }

            // adjust opened path (always in 'generic format')
            openedPath = FileUtil.getGenericPath(path);
            // keep trace of last used flags
            openFlags = flags;
            // need to update resolution levels
            resolutions = null;

            return true;
        }
        catch (final FormatException e) {
            throw translateException(path, e);
        }
    }

    @Override
    public void close() throws IOException {
        // something to close ?
        if (getOpened() != null) {
            openedPath = null;

            synchronized (readersPool) {
                // close all readers
                for (final IFormatReader r : readersPool)
                    r.close();

                readersPool.clear();
            }
        }
    }

    /**
     * Open reader for given file path
     */
    protected void openReader(final IFormatReader reader, final String path, final int flags) throws FormatException, IOException {
        final int adjFlags;

        // for safety
        reader.close();

        // don't try to parse metadata on image file type which don't contains anything useful
        if (!Loader.hasMetadata(path))
            adjFlags = flags | FLAG_METADATA_MINIMUM;
        else
            adjFlags = flags;

        switch (adjFlags & FLAG_METADATA_MASK) {
            case FLAG_METADATA_MINIMUM:
                options.setMetadataLevel(MetadataLevel.MINIMUM);
                // set metadata option
                reader.setOriginalMetadataPopulated(false);
                // don't need to filter metadata
                reader.setMetadataFiltered(false);
                break;

            case FLAG_METADATA_ALL:
                options.setMetadataLevel(MetadataLevel.ALL);
                // set metadata option
                reader.setOriginalMetadataPopulated(true);
                // may need metadata filtering
                reader.setMetadataFiltered(true);
                break;

            default:
                options.setMetadataLevel(MetadataLevel.NO_OVERLAYS);
                // set metadata option
                reader.setOriginalMetadataPopulated(originalMetadata);
                // may need metadata filtering
                reader.setMetadataFiltered(true);
                break;
        }

        // disable flattening sub resolution
        reader.setFlattenedResolutions(false);
        // set file grouping
        reader.setGroupFiles(groupFiles);
        // prepare meta data store structure
        reader.setMetadataStore((MetadataStore) OMEUtil.createOMEXMLMetadata());
        reader.setMetadataOptions(options);

        // set path (id)
        reader.setId(path);
    }

    /**
     * Clone the current used reader conserving its properties and current path
     */
    protected IFormatReader cloneReader() throws FormatException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (internalReader == null)
            return null;

        // create the new internal reader instance
        final IFormatReader newReader = internalReader.getClass().getDeclaredConstructor().newInstance();
        final IFormatReader result;

        // use TileStitcher wrapper when grouping is ON
        if (groupFiles)
            result = TileStitcher.makeTileStitcher(newReader);
        else
            result = newReader;

        // get opened file
        final String path = getOpened();

        if (path != null)
            // open reader for path (adjust path format for Bio-Format)
            openReader(result, new File(path).getAbsolutePath(), openFlags);

        return result;
    }

    /**
     * Returns a reader to use for the current thread (allocate it if needed).<br>
     * Any obtained reader should be released using {@link #releaseReader(IFormatReader)}
     *
     * @see #releaseReader(IFormatReader)
     */
    public IFormatReader getReader() throws FormatException, IOException {
        try {
            final IFormatReader result;

            synchronized (readersPool) {
                if (readersPool.isEmpty())
                    result = cloneReader();
                    // allocate last reader (faster)
                else
                    result = readersPool.remove(readersPool.size() - 1);
            }

            final int s = reader.getSeries();
            final int r = reader.getResolution();

            // ensure we are working on same series and resolution
            if (result.getSeries() != s)
                result.setSeries(s);
            if (result.getResolution() != r)
                result.setResolution(r);

            return result;
        }
        catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            // better to rethrow as RuntimeException
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Release the reader obtained through {@link #getReader()} to the reader pool.
     *
     * @see #getReader()
     */
    public void releaseReader(final IFormatReader r) {
        synchronized (readersPool) {
            readersPool.add(r);
        }
    }

    /**
     * Prepare the reader to read data from specified series but keep the current / default resolution level.<br>
     * WARNING: this method should not be called while image reading operation are still occurring (multi threaded
     * read).
     */
    protected void prepareReader(final int series) {
        // series changed ?
        if (reader.getSeries() != series) {
            // set wanted series
            reader.setSeries(series);
            // reset resolution level
            resolutions = null;
        }

        // need to update resolution levels ?
        if (resolutions == null) {
            // set default resolution
            reader.setResolution(0);

            // get default sizeX
            final double sizeX = reader.getSizeX();
            // get resolution count for this series
            final int resCount = reader.getResolutionCount();

            // init resolution levels
            final List<Integer> validResolutions = new ArrayList<>(16);
            // full resolution at 0 (always)
            validResolutions.add(Integer.valueOf(0));

            // check the sub resolution level
            for (int r = 1; r < resCount; r++) {
                // set resolution level in reader
                reader.setResolution(r);

                // get real resolution level
                final double level = Math.log(sizeX / reader.getSizeX()) / Math.log(2);
                final double levelInt = Math.floor(level);

                // we only want 2^x sub resolution
                if (Math.abs(level - levelInt) < 0.005d)
                    validResolutions.add(Integer.valueOf((int) levelInt));
            }

            // copy back to resolutions
            resolutions = new int[validResolutions.size()];
            for (int i = 0; i < resolutions.length; i++)
                resolutions[i] = validResolutions.get(i).intValue();
        }
    }

    /**
     * Prepare the reader to read data from specified series and at specified resolution.<br>
     * WARNING: this method should not be called while image reading operation are still occurring (multi threaded
     * read).
     *
     * @return the image divisor factor to match the wanted resolution if needed
     */
    protected int prepareReader(final int series, final int resolution) {
        prepareReader(series);

        if (resolution > 0) {
            // find closest (but strictly <=) available resolution level
            int indRes = 1;
            while ((indRes < resolutions.length) && (resolutions[indRes] <= resolution))
                indRes++;

            // get back to correct resolution level index
            indRes--;
            // set resolution level
            reader.setResolution(indRes);

            // return difference between selected resolution level and wanted resolution level
            return resolution - resolutions[indRes];
        }

        // just use default full resolution
        reader.setResolution(0);

        return 0;
    }

    /**
     * Internal use only
     */
    protected int getResolutionShift() {
        return resolutions[reader.getResolution()];
    }

    /**
     * Internal use only
     */
    protected double getResolutionDiviserFactor() {
        return 1d / Math.pow(2, getResolutionShift());
    }

    @Override
    public OMEXMLMetadata getOMEXMLMetaData() throws UnsupportedFormatException, IOException {
        // no image currently opened
        if (getOpened() == null)
            return null;

        // retrieve metadata (don't need thread safe reader for this)
        final OMEXMLMetadata result = (OMEXMLMetadata) reader.getMetadataStore();

        // TileStitcher reduced series number (stitching occurred) ?
        if ((reader.getSeriesCount() == 1) && (MetaDataUtil.getNumSeries(result) > 1)) {
            // adjust series count in metadata
            MetaDataUtil.setNumSeries(result, 1);

            final int sx = reader.getSizeX();
            final int sy = reader.getSizeY();

            // fix metadata regarding reader information if available
            if ((sx > 0) && (sy > 0)) {
                MetaDataUtil.setSizeX(result, 0, sx);
                MetaDataUtil.setSizeY(result, 0, sy);
            }
        }

        return result;
    }

    @Deprecated(since = "2.4.3", forRemoval = true)
    @Override
    public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException {
        return (OMEXMLMetadataImpl) getOMEXMLMetaData();
    }

    @Override
    public int getTileWidth(final int series) throws IOException {
        // no image currently opened
        if (getOpened() == null)
            return 0;

        // prepare reader
        prepareReader(series);

        // don't need thread safe reader for this
        final int result = reader.getOptimalTileWidth();

        if (result == 0)
            return result;

        // we want closest power of 2 (smaller or equal)
        return (int) Math.pow(2, Math.floor(Math.log(result) / Math.log(2d)));
    }

    @Override
    public int getTileHeight(final int series) throws IOException {
        // no image currently opened
        if (getOpened() == null)
            return 0;

        // prepare reader
        prepareReader(series);

        // don't need thread safe reader for this
        final int result = reader.getOptimalTileHeight();

        if (result == 0)
            return result;

        // we want closest power of 2 (smaller or equal)
        return (int) Math.pow(2, Math.floor(Math.log(result) / Math.log(2d)));
    }

    @Override
    public boolean isResolutionAvailable(final int series, final int resolution) {
        // no image currently opened
        if (getOpened() == null)
            return resolution == 0;

        // prepare reader
        prepareReader(series);

        if (resolution > 0) {
            // try to find wanted resolution
            for (final int r : resolutions)
                if (r == resolution)
                    return true;
        }

        return resolution == 0;
    }

    @Override
    public IcyBufferedImage getThumbnail(final int series) throws Exception {
        // no image currently opened
        if (getOpened() == null)
            return null;

        // stitched image ? --> use AbstractImageProvider implementation as getThumbnail(..) from TileSticher doesn't do
        // stitching
        if ((reader.getSizeX() != internalReader.getSizeX()) || (reader.getSizeY() != internalReader.getSizeY()))
            return super.getThumbnail(series);

        try {
            // prepare reader (no down scaling here)
            prepareReader(series, 0);

            final IFormatReader r = getReader();

            try {
                // get image
                return getThumbnail(r, r.getSizeZ() / 2, r.getSizeT() / 2);
            }
            finally {
                releaseReader(r);
            }
        }
        catch (final FormatException e) {
            throw translateException(getOpened(), e);
        }
        catch (final Throwable t) {
            // can happen if we don't have enough memory --> try default implementation
            return super.getThumbnail(series);
        }
    }

    @Override
    public Object getPixels(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c)
            throws UnsupportedFormatException, IOException {
        // no image currently opened
        if (getOpened() == null)
            return null;

        try {
            // prepare reader and get down scale factor
            final int downScaleLevel = prepareReader(series, resolution);
            final IFormatReader r = getReader();

            final Rectangle adjRect;

            // adjust rectangle to current reader resolution if needed
            if (rectangle != null)
                adjRect = Rectangle2DUtil.getScaledRectangle(rectangle, getResolutionDiviserFactor(), false, true).getBounds();
            else
                adjRect = null;

            try {
                // return pixels
                return getPixelsInternal(r, adjRect, z, t, c, false, downScaleLevel);
            }
            finally {
                releaseReader(r);
            }
        }
        catch (final FormatException e) {
            throw translateException(getOpened(), e);
        }
    }

    @Override
    public IcyBufferedImage getImage(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c)
            throws UnsupportedFormatException, IOException {
        // no image currently opened
        if (getOpened() == null)
            return null;

        try {
            // prepare reader and get down scale factor if wanted resolution is not available
            final int downScaleLevel = prepareReader(series, resolution);
            final IFormatReader r = getReader();
            final Rectangle adjRect;

            // adjust rectangle to current reader resolution if needed
            if (rectangle != null)
                adjRect = Rectangle2DUtil.getScaledRectangle(rectangle, getResolutionDiviserFactor(), false, true).getBounds();
            else
                adjRect = null;

            try {
                // get image
                return getImage(r, adjRect, z, t, c, downScaleLevel);
            }
            catch (final IOException e) {
                throw e;
            }
            // not enough memory error ? Or too large XY plan ?
            catch (final OutOfMemoryError | UnsupportedOperationException e) {
                // need rescaling --> try tiling read
                if (downScaleLevel > 0)
                    return getImageByTile(series, resolution, rectangle, z, t, c, getTileWidth(series), getTileHeight(series), null);

                throw e;
            }
            catch (final FormatException e) {
                if (!(e.getCause() instanceof ClosedByInterruptException) && (downScaleLevel > 0))
                    // we can have here a "Image plane too large. Only 2GB of data can be extracted at
                    // one time." error here --> so can try to use tile loading when we need rescaling
                    return getImageByTile(series, resolution, rectangle, z, t, c, getTileWidth(series), getTileHeight(series), null);

                throw e;
            }
            catch (final Exception e) {
                // we can have NegativeArraySizeException here for instance
                // try to use tile loading when we need rescaling
                if (downScaleLevel > 0)
                    return getImageByTile(series, resolution, rectangle, z, t, c, getTileWidth(series), getTileHeight(series), null);

                throw new UnsupportedOperationException(e);
            }
            finally {
                releaseReader(r);
            }
        }
        catch (final FormatException e) {
            throw translateException(getOpened(), e);
        }
    }

    public IcyBufferedImage getImageByTile(final int series, final int resolution, final Rectangle region, final int z, final int t, final int c, final int tileW, final int tileH, final ProgressListener listener) throws UnsupportedFormatException, IOException {
        return new LociTileImageReader(series, resolution, region, z, t, c, tileW, tileH, listener).result;
    }

    @Override
    public Object getPixelsByTile(final int series, final int resolution, final Rectangle region, final int z, final int t, final int c, final int tileW, final int tileH, final ProgressListener listener) throws UnsupportedFormatException, IOException {
        return new LociTilePixelsReader(series, resolution, region, z, t, c, tileW, tileH, listener).result;
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and
     * returns it as an IcyBufferedImage.
     *
     * @param reader {@link IFormatReader}
     * @param z      Z position of the image to load
     * @param t      T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnail(final IFormatReader reader, final int z, final int t) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        return getThumbnail(reader, z, t, -1);
    }

    /**
     * Load a thumbnail version of the image located at (Z, T, C) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.
     *
     * @param reader {@link IFormatReader}
     * @param z      Z position of the thumbnail to load
     * @param t      T position of the thumbnail to load
     * @param c      Channel index
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnail(final IFormatReader reader, final int z, final int t, final int c) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        try {
            // all channel ?
            if (c == -1)
                return getImageInternal(reader, null, z, t, true, 0);

            return getImageInternal(reader, null, z, t, c, true, 0);
        }
        catch (final ClosedByInterruptException e) {
            // loading interrupted --> return null
            return null;
        }
        catch (final Exception e) {
            // LOCI do not support thumbnail for all image, try compatible version
            return getThumbnailCompatible(reader, z, t, c);
        }
    }

    /**
     * Load a thumbnail version of the image located at (Z, T) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.<br>
     * <i>Slow compatible version (load the original image and resize it)</i>
     *
     * @param reader {@link IFormatReader}
     * @param z      Z position of the image to load
     * @param t      T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnailCompatible(final IFormatReader reader, final int z, final int t) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        return getThumbnailCompatible(reader, z, t, -1);
    }

    /**
     * Load a thumbnail version of the image located at (Z, T, C) position from the specified
     * {@link IFormatReader} and returns it as an IcyBufferedImage.<br>
     * <i>Slow compatible version (load the original image and resize it)</i>
     *
     * @param reader {@link IFormatReader}
     * @param z      Z position of the thumbnail to load
     * @param t      T position of the thumbnail to load
     * @param c      Channel index
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getThumbnailCompatible(final IFormatReader reader, final int z, final int t, final int c) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        final IcyBufferedImage image = getImage(reader, null, z, t, c, 0);

        // scale it to desired dimension (fast enough as here we have a small image)
        final IcyBufferedImage result = IcyBufferedImageUtil.scale(image, reader.getThumbSizeX(), reader.getThumbSizeY(), FilterType.BILINEAR);

        // preserve colormaps
        result.setColorMaps(image);

        return result;
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     *
     * @param reader         Reader used to load the image
     * @param rect           Define the image rectangular region we want to retrieve data for (considering current selected image
     *                       resolution).<br>
     *                       Set to <code>null</code> to retrieve the whole image.
     * @param z              Z position of the image to load
     * @param t              T position of the image to load
     * @param c              Channel index to load (-1 = all channels)
     * @param downScaleLevel number of downscale to process (scale level = 1/2^downScaleLevel)
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImage(final IFormatReader reader, final Rectangle rect, final int z, final int t, final int c, final int downScaleLevel) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        // we want all channel ? use method to retrieve whole image
        if (c == -1)
            return getImageInternal(reader, rect, z, t, false, downScaleLevel);

        return getImageInternal(reader, rect, z, t, c, false, downScaleLevel);
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     *
     * @param reader Reader used to load the image
     * @param rect   Define the image rectangular region we want to retrieve data for (considering current selected image
     *               resolution).<br>
     *               Set to <code>null</code> to retrieve the whole image.
     * @param z      Z position of the image to load
     * @param t      T position of the image to load
     * @param c      Channel index to load (-1 = all channels)
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImage(final IFormatReader reader, final Rectangle rect, final int z, final int t, final int c) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        return getImage(reader, rect, z, t, c, 0);
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage.
     *
     * @param reader {@link IFormatReader}
     * @param rect   Define the image rectangular region we want to retrieve data for (considering current selected image
     *               resolution).<br>
     *               Set to <code>null</code> to retrieve the whole image.
     * @param z      Z position of the image to load
     * @param t      T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImage(final IFormatReader reader, final Rectangle rect, final int z, final int t) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        return getImage(reader, rect, z, t, -1, 0);
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage (compatible and slower method).
     *
     * @param reader {@link IFormatReader}
     * @param z      Z position of the image to load
     * @param t      T position of the image to load
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage getImageCompatible(final IFormatReader reader, final int z, final int t) throws FormatException, IOException {
        final int sizeX = reader.getSizeX();
        final int sizeY = reader.getSizeY();
        final List<BufferedImage> imageList = new ArrayList<>();
        final int sizeC = reader.getEffectiveSizeC();

        for (int c = 0; c < sizeC; c++)
            imageList.add(AWTImageTools.openImage(reader.openBytes(reader.getIndex(z, c, t)), reader, sizeX, sizeY));

        // combine channels
        return IcyBufferedImage.createFrom(imageList);

    }

    /**
     * Load pixels of the specified region of image at (Z, T, C) position and returns them as an
     * array.
     *
     * @param reader         Reader used to load the pixels
     * @param rect           Define the pixels rectangular region we want to load (considering current selected image resolution).<br>
     *                       Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param z              Z position of the pixels to load
     * @param t              T position of the pixels to load
     * @param c              Channel index to load
     * @param thumbnail      Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i>
     *                       parameter should contains thumbnail size
     * @param downScaleLevel number of downscale to process (scale level = 1/2^downScaleLevel)
     * @param rawBuffer      pre allocated byte data buffer ([reader.getRGBChannelCount() * SizeX * SizeY *
     *                       Datatype.size]) used to read the whole RGB raw data (can be <code>null</code>)
     * @param channelBuffer  pre allocated byte data buffer ([SizeX * SizeY * Datatype.size]) used to read the
     *                       channel raw data (can be <code>null</code>)
     * @param pixelBuffer    pre allocated 1D array pixel data buffer ([SizeX * SizeY]) used to receive the pixel
     *                       converted data and to build the result image (can be <code>null</code>)
     * @return 1D array containing pixels data.<br>
     * The type of the array depends from the internal image data type
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    protected static Object getPixelsInternal(final IFormatReader reader, final Rectangle rect, final int z, final int t, final int c, final boolean thumbnail, final int downScaleLevel, final byte[] rawBuffer, final byte[] channelBuffer, final Object pixelBuffer) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        // get pixel data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        Objects.requireNonNull(dataType);

        // check we can open the image
        // Loader.checkOpening(resolutions[reader.getResolution()], rect.width, rect.height, 1, 1, 1, dataType,
        // "");

        // prepare informations
        final int rgbChanCount = reader.getRGBChannelCount();
        final boolean interleaved = reader.isInterleaved();
        final boolean little = reader.isLittleEndian();

        // allocate internal image data array if needed
        Object result = Array1DUtil.allocIfNull(pixelBuffer, dataType, rect.width * rect.height);
        // compute channel offsets
        final int baseC = c / rgbChanCount;
        final int subC = c % rgbChanCount;

        // get image data (whole RGB data for RGB channel)
        final byte[] rawData = getBytesInternal(reader, reader.getIndex(z, baseC, t), rect, thumbnail, rawBuffer);

        // current final component
        final int componentByteLen = rawData.length / rgbChanCount;

        // build data array
        if (interleaved) {
            // get channel interleaved data
            final byte[] channelData = Array1DUtil.getInterleavedData(rawData, subC, rgbChanCount, channelBuffer, 0, componentByteLen);
            ByteArrayConvert.byteArrayTo(channelData, 0, result, 0, componentByteLen, little);
        }
        else
            ByteArrayConvert.byteArrayTo(rawData, subC * componentByteLen, result, 0, componentByteLen, little);

        // don't need downscaling ? --> we can return raw pixels immediately
        if (downScaleLevel <= 0)
            return result;

        // do fast downscaling
        int it = downScaleLevel;
        int sizeX = rect.width;
        int sizeY = rect.height;

        while (it-- > 0) {
            result = IcyBufferedImageUtil.downscaleBy2(result, sizeX, sizeY, dataType.isSigned(), true);
            sizeX /= 2;
            sizeY /= 2;
        }

        return result;
    }

    /**
     * Load pixels of the specified region of image at (Z, T, C) position and returns them as an
     * array.
     *
     * @param reader         Reader used to load the pixels
     * @param rect           Define the pixels rectangular region we want to load (considering current selected image resolution).<br>
     *                       Set to <code>null</code> to retrieve the whole image.
     * @param z              Z position of the pixels to load
     * @param t              T position of the pixels to load
     * @param c              Channel index to load
     * @param thumbnail      Set to <code>true</code> to request a thumbnail of the image (<code>rect</code>
     *                       parameter is then ignored)
     * @param downScaleLevel number of downscale to process (scale level = 1/2^downScaleLevel)
     * @return 1D array containing pixels data.<br>
     * The type of the array depends from the internal image data type
     */
    protected static Object getPixelsInternal(final IFormatReader reader, final Rectangle rect, final int z, final int t, final int c, final boolean thumbnail, final int downScaleLevel) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        final Rectangle r;

        if (thumbnail)
            r = new Rectangle(0, 0, reader.getThumbSizeX(), reader.getThumbSizeY());
        else if (rect == null)
            r = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());
        else
            r = rect;

        return getPixelsInternal(reader, r, z, t, c, thumbnail, downScaleLevel, null, null, null);
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified
     * {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     *
     * @param reader         Reader used to load the image
     * @param rect           Define the image rectangular region we want to retrieve data for (considering current selected image
     *                       resolution).<br>
     *                       Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param z              Z position of the image to load
     * @param t              T position of the image to load
     * @param c              Channel index to load
     * @param thumbnail      Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i>
     *                       parameter should contains thumbnail size
     * @param downScaleLevel number of downscale to process (scale level = 1/2^downScaleLevel)
     * @param rawBuffer      pre allocated byte data buffer ([reader.getRGBChannelCount() * SizeX * SizeY *
     *                       Datatype.size]) used to read the whole RGB raw data (can be <code>null</code>)
     * @param channelBuffer  pre allocated byte data buffer ([SizeX * SizeY * Datatype.size]) used to read the
     *                       channel raw data (can be <code>null</code>)
     * @param pixelBuffer    pre allocated 1D array pixel data buffer ([SizeX * SizeY]) used to receive the pixel
     *                       converted data and to build the result image (can be <code>null</code>)
     * @return {@link IcyBufferedImage}
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    protected static IcyBufferedImage getImageInternal(final IFormatReader reader, final Rectangle rect, final int z, final int t, final int c, final boolean thumbnail, final int downScaleLevel, final byte[] rawBuffer, final byte[] channelBuffer, final Object pixelBuffer) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        // get pixel data
        final Object pixelData = getPixelsInternal(reader, rect, z, t, c, thumbnail, downScaleLevel, rawBuffer, channelBuffer, pixelBuffer);
        // get pixel data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());

        // get final sizeX and sizeY
        int sizeX = rect.width;
        int sizeY = rect.height;
        int downScale = downScaleLevel;
        while (downScale > 0) {
            sizeX /= 2;
            sizeY /= 2;
            downScale--;
        }

        // create the single channel result image from pixel data
        final IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, pixelData, Objects.requireNonNull(dataType).isSigned());

        IcyColorMap map = null;
        final int rgbChannel = reader.getRGBChannelCount();

        // indexed color ?
        if (reader.isIndexed()) {
            // only 8 bits and 16 bits lookup table supported
            switch (dataType.getJavaType()) {
                case BYTE:
                    final byte[][] bmap = reader.get8BitLookupTable();
                    if (bmap != null)
                        map = new IcyColorMap("Channel " + c, bmap);
                    break;

                case SHORT:
                    final short[][] smap = reader.get16BitLookupTable();
                    if (smap != null)
                        map = new IcyColorMap("Channel " + c, smap);
                    break;

                default:
                    break;
            }
        }

        // no RGB image ?
        if (rgbChannel <= 1) {
            // colormap not set (or black) ? --> try to use metadata
            if ((map == null) || map.isBlack()) {
                final OMEXMLMetadata metaData = (OMEXMLMetadata) reader.getMetadataStore();
                final Color color = MetaDataUtil.getChannelColor(metaData, reader.getSeries(), c);

                if ((color != null) && !ColorUtil.isBlack(color))
                    map = new LinearColorMap("Channel " + c, color);
                else
                    map = null;
            }
        }
        else {
            map = switch (c) {
                case 0 -> LinearColorMap.red_;
                case 1 -> LinearColorMap.green_;
                case 2 -> LinearColorMap.blue_;
                case 3 -> LinearColorMap.alpha_;
                default -> map;
            };
        }

        // we were able to retrieve a colormap ? --> set it
        if (map != null)
            result.setColorMap(0, map, true);

        return result;
    }

    /**
     * Load a single channel sub image at (Z, T, C) position from the specified
     * {@link IFormatReader}<br>
     * and returns it as an IcyBufferedImage.
     *
     * @param reader         Reader used to load the image
     * @param rect           Define the image rectangular region we want to retrieve data for (considering current selected image
     *                       resolution).<br>
     *                       Set to <code>null</code> to retrieve the whole image.
     * @param z              Z position of the image to load
     * @param t              T position of the image to load
     * @param c              Channel index to load
     * @param thumbnail      Set to <code>true</code> to request a thumbnail of the image (<code>rect</code>
     *                       parameter is then ignored)
     * @param downScaleLevel number of downscale to process (scale level = 1/2^downScaleLevel)
     * @return {@link IcyBufferedImage}
     */
    protected static IcyBufferedImage getImageInternal(final IFormatReader reader, final Rectangle rect, final int z, final int t, final int c, final boolean thumbnail, final int downScaleLevel) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        final Rectangle r;

        if (thumbnail)
            r = new Rectangle(0, 0, reader.getThumbSizeX(), reader.getThumbSizeY());
        else if (rect == null)
            r = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());
        else
            r = rect;

        return getImageInternal(reader, r, z, t, c, thumbnail, downScaleLevel, null, null, null);
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader and return it as
     * an IcyBufferedImage.
     *
     * @param reader         {@link IFormatReader}
     * @param rect           Define the image rectangular region we want to retrieve data for (considering current selected image
     *                       reader resolution).<br>
     *                       Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param z              Z position of the image to load
     * @param t              T position of the image to load
     * @param thumbnail      Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i> parameter should
     *                       contains thumbnail size
     * @param downScaleLevel number of downscale to process after loading the image (scale level = 1/2^downScaleLevel)
     * @param rawBuffer      pre allocated byte data buffer ([reader.getRGBChannelCount() * SizeX * SizeY * Datatype.size]) used to
     *                       read the whole RGB raw data (can be <code>null</code>)
     * @param channelBuffer  pre allocated byte data buffer ([SizeX * SizeY * Datatype.size]) used to read the channel raw data (can be
     *                       <code>null</code>)
     * @param pixelBuffer    pre allocated 2D array ([SizeC][SizeX*SizeY]) pixel data buffer used to receive the pixel converted data
     *                       and to build the result image (can be <code>null</code>)
     * @return {@link IcyBufferedImage}
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    protected static IcyBufferedImage getImageInternal(final IFormatReader reader, final Rectangle rect, final int z, final int t, final boolean thumbnail, final int downScaleLevel, final byte[] rawBuffer, final byte[] channelBuffer, final Object[] pixelBuffer) throws UnsupportedOperationException, OutOfMemoryError, FormatException, IOException {
        // get pixel data type
        final DataType dataType = DataType.getDataTypeFromFormatToolsType(reader.getPixelType());
        // get sizeC
        final int effSizeC = reader.getEffectiveSizeC();
        final int rgbChanCount = reader.getRGBChannelCount();
        final int sizeX = rect.width;
        final int sizeY = rect.height;
        final int sizeC = effSizeC * rgbChanCount;

        // check we can open the image
        // Loader.checkOpening(resolutions[reader.getResolution()], sizeX, sizeY, sizeC, 1, 1, dataType, "");

        final int series = reader.getSeries();
        // prepare informations
        final boolean indexed = reader.isIndexed();
        final boolean little = reader.isLittleEndian();
        final OMEXMLMetadata metaData = (OMEXMLMetadata) reader.getMetadataStore();

        // prepare internal image data array
        final Object[] pixelData;

        if (pixelBuffer == null) {
            // allocate array
            pixelData = Array2DUtil.createArray(Objects.requireNonNull(dataType), sizeC);
            if (pixelData != null)
                for (int i = 0; i < sizeC; i++)
                    pixelData[i] = Array1DUtil.createArray(dataType, sizeX * sizeY);
        }
        else
            pixelData = pixelBuffer;

        // colormap allocation
        final IcyColorMap[] colormaps = new IcyColorMap[effSizeC];

        byte[] rawData;
        for (int effC = 0; effC < effSizeC; effC++) {
            // get data
            rawData = getBytesInternal(reader, reader.getIndex(z, effC, t), rect, thumbnail, rawBuffer);

            // current final component
            final int c = effC * rgbChanCount;
            final int componentByteLen = rawData.length / rgbChanCount;

            // build data array
            int inOffset = 0;
            if (reader.isInterleaved()) {
                final byte[] channelData = (channelBuffer == null) ? new byte[componentByteLen] : channelBuffer;

                for (int sc = 0; sc < rgbChanCount; sc++) {
                    // get channel interleaved data
                    Array1DUtil.getInterleavedData(rawData, inOffset, rgbChanCount, channelData, 0, componentByteLen);
                    ByteArrayConvert.byteArrayTo(channelData, 0, Objects.requireNonNull(pixelData)[c + sc], 0, componentByteLen, little);
                    inOffset++;
                }
            }
            else {
                for (int sc = 0; sc < rgbChanCount; sc++) {
                    ByteArrayConvert.byteArrayTo(rawData, inOffset, Objects.requireNonNull(pixelData)[c + sc], 0, componentByteLen, little);
                    inOffset += componentByteLen;
                }
            }

            // indexed color ?
            if (indexed) {
                // only 8 bits and 16 bits lookup table supported
                switch (Objects.requireNonNull(dataType).getJavaType()) {
                    case BYTE:
                        final byte[][] bmap = reader.get8BitLookupTable();
                        if (bmap != null)
                            colormaps[effC] = new IcyColorMap("Channel " + effC, bmap);
                        break;

                    case SHORT:
                        final short[][] smap = reader.get16BitLookupTable();
                        if (smap != null)
                            colormaps[effC] = new IcyColorMap("Channel " + effC, smap);
                        break;

                    default:
                        colormaps[effC] = null;
                        break;
                }
            }

            // no RGB image ?
            if (rgbChanCount <= 1) {
                // colormap not yet set (or black) ? --> try to use metadata
                if ((colormaps[effC] == null) || colormaps[effC].isBlack()) {
                    final Color color = MetaDataUtil.getChannelColor(metaData, series, effC);

                    if ((color != null) && !ColorUtil.isBlack(color))
                        colormaps[effC] = new LinearColorMap("Channel " + effC, color);
                    else
                        colormaps[effC] = null;
                }
            }
        }

        // create result image
        IcyBufferedImage result = new IcyBufferedImage(sizeX, sizeY, pixelData, Objects.requireNonNull(dataType).isSigned());

        // do downscaling if needed
        result = IcyBufferedImageUtil.downscaleBy2(result, true, downScaleLevel);

        // affect colormap
        result.beginUpdate();
        try {
            // RGB image (can't use colormaps)
            if (rgbChanCount > 1) {
                // RGB at least ? --> set RGB colormap
                if ((sizeC >= 3) && (rgbChanCount >= 3)) {
                    result.setColorMap(0, LinearColorMap.red_, true);
                    result.setColorMap(1, LinearColorMap.green_, true);
                    result.setColorMap(2, LinearColorMap.blue_, true);
                }
                // RGBA ? --> set alpha colormap
                if ((sizeC >= 4) && ((rgbChanCount >= 4) || (reader instanceof PNGReader)
                        || (reader instanceof APNGReader) || (reader instanceof JPEG2000Reader)))
                    result.setColorMap(3, LinearColorMap.alpha_, true);
            }
            // fluo image
            else if (sizeC == effSizeC) {
                // set colormaps
                for (int comp = 0; comp < effSizeC; comp++) {
                    // we were able to retrieve a colormap for that channel ? --> set it
                    if (colormaps[comp] != null)
                        result.setColorMap(comp, colormaps[comp], true);
                }
            }
        }
        finally {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Load the image located at (Z, T) position from the specified IFormatReader<br>
     * and return it as an IcyBufferedImage.
     *
     * @param reader         {@link IFormatReader}
     * @param rect           Define the image rectangular region we want to retrieve data for (considering current selected image
     *                       resolution).<br>
     *                       Set to <code>null</code> to retrieve the whole image.
     * @param z              Z position of the image to load
     * @param t              T position of the image to load
     * @param thumbnail      Set to <code>true</code> to request a thumbnail of the image (<code>rect</code> parameter is then ignored)
     * @param downScaleLevel number of downscale to process (scale level = 1/2^downScaleLevel)
     * @return {@link IcyBufferedImage}
     */
    protected static IcyBufferedImage getImageInternal(final IFormatReader reader, final Rectangle rect, final int z, final int t, final boolean thumbnail, final int downScaleLevel) throws FormatException, IOException {
        final Rectangle r;

        if (thumbnail)
            r = new Rectangle(0, 0, reader.getThumbSizeX(), reader.getThumbSizeY());
        else if (rect == null)
            r = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());
        else
            r = rect;

        return getImageInternal(reader, r, z, t, thumbnail, downScaleLevel, null, null, null);
    }

    /**
     * <b>Internal use only !!</b><br>
     *
     * @param reader    Reader used to load the pixels
     * @param index     plane index
     * @param rect      Define the pixels rectangular region we want to load (considering current selected image resolution).<br>
     *                  Should be adjusted if <i>thumbnail</i> parameter is <code>true</code>
     * @param thumbnail Set to <code>true</code> to request a thumbnail of the image in which case <i>rect</i>
     *                  parameter should contains thumbnail size
     * @param buffer    pre allocated output byte data buffer ([reader.getRGBChannelCount() * rect.width * rect.height *
     *                  Datatype.size]) used to
     *                  read the whole RGB raw data (can be <code>null</code>)
     * @return byte array containing pixels data.<br>
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    protected static byte[] getBytesInternal(final IFormatReader reader, final int index, final Rectangle rect, final boolean thumbnail, final byte[] buffer) throws FormatException, IOException {
        if (thumbnail)
            return reader.openThumbBytes(index);

        final Rectangle imgRect = new Rectangle(0, 0, reader.getSizeX(), reader.getSizeY());

        // TODO: we should check that we open a big image and then use tile loading instead

        // need to allocate
        if (buffer == null) {
            // return whole image
            if ((rect == null) || rect.contains(imgRect))
                return reader.openBytes(index);

            // return region
            return reader.openBytes(index, rect.x, rect.y, rect.width, rect.height);
        }

        // already allocated / whole image
        if ((rect == null) || rect.equals(imgRect))
            return reader.openBytes(index, buffer);

        // return region
        return reader.openBytes(index, buffer, rect.x, rect.y, rect.width, rect.height);
    }

    protected static UnsupportedFormatException translateException(final String path, final FormatException exception) {
        if (exception instanceof UnknownFormatException)
            return new UnsupportedFormatException(path + ": Unknown image format.", exception);
        else if (exception instanceof MissingLibraryException)
            return new UnsupportedFormatException(path + ": Missing library to load the image.", exception);
        else if (exception.getCause() instanceof ClosedByInterruptException)
            return new UnsupportedFormatException(path + ": loading interrupted.", exception.getCause());
        else
            return new UnsupportedFormatException(path + ": Unsupported image.", exception);
    }
}
