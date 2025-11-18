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

package org.bioimageanalysis.icy.model.image;

import loci.formats.gui.SignedByteBuffer;
import loci.formats.gui.SignedShortBuffer;
import loci.formats.gui.UnsignedIntBuffer;
import org.bioimageanalysis.icy.common.collection.array.Array1DUtil;
import org.bioimageanalysis.icy.common.collection.array.Array2DUtil;
import org.bioimageanalysis.icy.common.collection.array.ArrayUtil;
import org.bioimageanalysis.icy.common.collection.array.ByteArrayConvert;
import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.event.UpdateEventHandler;
import org.bioimageanalysis.icy.common.exception.TooLargeArrayException;
import org.bioimageanalysis.icy.common.exception.UnsupportedFormatException;
import org.bioimageanalysis.icy.common.listener.ChangeListener;
import org.bioimageanalysis.icy.common.math.ArrayMath;
import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.model.cache.ImageCache;
import org.bioimageanalysis.icy.model.colormap.IcyColorMap;
import org.bioimageanalysis.icy.model.colormap.LinearColorMap;
import org.bioimageanalysis.icy.model.colormodel.IcyColorModel;
import org.bioimageanalysis.icy.model.colormodel.IcyColorModelEvent;
import org.bioimageanalysis.icy.model.colormodel.IcyColorModelListener;
import org.bioimageanalysis.icy.model.image.IcyBufferedImageEvent.IcyBufferedImageEventType;
import org.bioimageanalysis.icy.model.lut.LUT;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceIdImporter;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.system.thread.Processor;

import javax.media.jai.PlanarImage;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.nio.channels.ClosedByInterruptException;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class IcyBufferedImage extends BufferedImage implements IcyColorModelListener, ChangeListener, AutoCloseable {
    static class WeakIcyBufferedImageReference extends WeakReference<IcyBufferedImage> {
        final int hc;

        WeakIcyBufferedImageReference(final IcyBufferedImage image) {
            super(image);

            hc = System.identityHashCode(image);
        }

        @Override
        public int hashCode() {
            return hc;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof WeakIcyBufferedImageReference)
                return obj.hashCode() == hashCode();

            return super.equals(obj);
        }
    }

    /**
     * @param imp        importer
     * @param series     series index
     * @param resolution resolution
     * @param region     region
     * @param t          T, Z, C position
     */
    public record ImageSourceInfo(SequenceIdImporter imp, int series, int resolution, Rectangle region, int t, int z, int c) {
        @Override
        public String toString() {
            return imp.toString() + " s=" + series + " r=" + resolution + " t=" + t + " z=" + z + " c=" + c;
        }
    }

    private static class ImageDataLoaderWorker implements Callable<Object> {
        final WeakReference<IcyBufferedImage> imageRef;

        ImageDataLoaderWorker(final IcyBufferedImage image) {
            super();

            this.imageRef = new WeakReference<>(image);
        }

        @Override
        public Object call() throws Exception {
            final IcyBufferedImage image = imageRef.get();

            // image has been released, we probably don't need its data anymore...
            if (image == null)
                return null;

            // not null here
            final ImageSourceInfo imageSourceInfo = image.imageSourceInfo;
            final SequenceIdImporter imp = imageSourceInfo.imp;

            // importer not opened ? --> cannot load
            if (StringUtil.isEmpty(imp.getOpened()))
                throw new IOException("Cannot load image data: Sequence importer is closed.");

            final int sizeC = image.getSizeC();
            // create the result array (always 2D native type)
            final Object[] result = Array2DUtil.createArray(image.getDataType(), sizeC);
            assert result != null;

            // all channels ?
            if ((imageSourceInfo.c == -1) && (sizeC > 1)) {
                // better to directly load image
                final IcyBufferedImage newImage = imp.getImage(imageSourceInfo.series, imageSourceInfo.resolution,
                        imageSourceInfo.region, imageSourceInfo.z, imageSourceInfo.t);
                // we want data in memory
                newImage.setVolatile(false);
                // then get data
                for (int c = 0; c < sizeC; c++)
                    result[c] = newImage.getDataXY(c);
            }
            else {
                // all channel for single channel image --> channel 0
                final int startC = (imageSourceInfo.c == -1) ? 0 : imageSourceInfo.c;
                // directly load pixel data
                for (int c = 0; c < sizeC; c++)
                    result[c] = imp.getPixels(imageSourceInfo.series, imageSourceInfo.resolution,
                            imageSourceInfo.region, imageSourceInfo.z, imageSourceInfo.t, startC + c);
            }

            return result;
        }

        IcyBufferedImage getImage() {
            return imageRef.get();
        }
    }

    private static class ImageDataLoaderTask extends FutureTask<Object> {
        final ImageDataLoaderWorker worker;

        ImageDataLoaderTask(final ImageDataLoaderWorker worker) {
            super(worker);

            this.worker = worker;
        }

        IcyBufferedImage getImage() {
            return worker.getImage();
        }
    }

    private static class ImageDataLoader {
        final Processor processor;

        public ImageDataLoader() {
            super();

            processor = new Processor(SystemUtil.getNumberOfCPUs() * 2);
            processor.setThreadName("Image data loader");
        }

        Object loadImageData(final IcyBufferedImage image) throws ExecutionException, InterruptedException {
            final ImageDataLoaderTask task = new ImageDataLoaderTask(new ImageDataLoaderWorker(image));

            processor.execute(task);

            try {
                return task.get();
            }
            catch (final InterruptedException e) {
                // process interrupted ? remove task from executor queue if possible
                processor.remove(task);
                // cancel the task (without interrupting current running task as this close the importer)
                task.cancel(false);

                // re throw interrupt
                throw e;
            }
        }

        void cancelTasks(final IcyBufferedImage image) {
            final List<ImageDataLoaderTask> tasks = new ArrayList<>();
            final BlockingQueue<Runnable> queue = processor.getQueue();

            synchronized (queue) {
                for (final Runnable task : queue) {
                    final ImageDataLoaderTask imgTask = (ImageDataLoaderTask) task;
                    final IcyBufferedImage imgImage = imgTask.getImage();

                    if ((imgImage == null) || (imgImage == image))
                        tasks.add(imgTask);
                }
            }

            // remove pending tasks for that image
            for (final ImageDataLoaderTask task : tasks) {
                processor.remove(task);
                task.cancel(false);
            }
        }
    }

    /**
     * Used for image / data loading from importer
     */
    static final ImageDataLoader imageDataLoader = new ImageDataLoader();

    /**
     * Used internally to find out an image from its identity hash code
     */
    static final Map<Integer, WeakIcyBufferedImageReference> images = new HashMap<>();

    /**
     * Retrieve an {@link IcyBufferedImage} from its identity hash code
     */
    public static IcyBufferedImage getIcyBufferedImage(final Integer idHashCode) {
        final WeakIcyBufferedImageReference ref;

        synchronized (images) {
            ref = images.get(idHashCode);
        }

        if (ref != null)
            return ref.get();

        return null;
    }

    /**
     * Retrieve an {@link IcyBufferedImage} from its identity hash code
     */
    public static IcyBufferedImage getIcyBufferedImage(final int idHashCode) {
        return getIcyBufferedImage(Integer.valueOf(idHashCode));
    }

    /**
     * Create an IcyBufferedImage (multi component) from a list of BufferedImage.<br>
     * IMPORTANT: source images can be used as part or as the whole result so consider them as 'lost'.
     *
     * @param imageList list of {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     * @throws IllegalArgumentException if imageList is empty or contains incompatible images.
     */
    public static IcyBufferedImage createFrom(final List<? extends BufferedImage> imageList) throws IllegalArgumentException {
        if (imageList.size() == 0)
            throw new IllegalArgumentException("imageList should contains at least 1 image");

        final List<IcyBufferedImage> icyImageList = new ArrayList<>();

        // transform images to icy images
        for (final BufferedImage image : imageList)
            icyImageList.add(IcyBufferedImage.createFrom(image));

        final IcyBufferedImage firstImage = icyImageList.get(0);

        if (icyImageList.size() == 1)
            return firstImage;

        final DataType dataType = firstImage.getDataType();
        final int width = firstImage.getWidth();
        final int height = firstImage.getHeight();

        // calculate channel number
        int numChannel = 0;
        for (final IcyBufferedImage image : icyImageList)
            numChannel += image.getSizeC();

        final Object[] data = Array2DUtil.createArray(dataType, numChannel);
        assert data != null;
        final IcyColorMap[] colormaps = new IcyColorMap[numChannel];

        // get data from all images
        int destC = 0;
        for (final IcyBufferedImage image : icyImageList) {
            if (dataType != image.getDataType())
                throw new IllegalArgumentException("All images contained in imageList should have the same dataType");
            if ((width != image.getWidth()) || (height != image.getHeight()))
                throw new IllegalArgumentException("All images contained in imageList should have the same dimension");

            for (int c = 0; c < image.getSizeC(); c++) {
                data[destC] = image.getDataXY(c);
                colormaps[destC++] = image.getColorMap(c);
            }
        }

        // create result image
        final IcyBufferedImage result = new IcyBufferedImage(width, height, data, dataType.isSigned());

        // restore colormaps
        for (int c = 0; c < result.getSizeC(); c++)
            result.setColorMap(c, colormaps[c], false);

        return result;
    }

    /**
     * Create an IcyBufferedImage from a {@link PlanarImage}.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     *
     * @param image {@link PlanarImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(final PlanarImage image, final boolean signedDataType) {
        final DataBuffer db = image.getData().getDataBuffer();
        final int w = image.getWidth();
        final int h = image.getHeight();

        return switch (db) {
            case final DataBufferByte dataBufferByte -> new IcyBufferedImage(w, h, dataBufferByte.getBankData(), signedDataType);
            case final DataBufferShort dataBufferShort -> new IcyBufferedImage(w, h, dataBufferShort.getBankData(), signedDataType);
            case final DataBufferUShort dataBufferUShort -> new IcyBufferedImage(w, h, dataBufferUShort.getBankData(), signedDataType);
            case final DataBufferInt dataBufferInt -> new IcyBufferedImage(w, h, dataBufferInt.getBankData(), signedDataType);
            case final DataBufferFloat dataBufferFloat -> new IcyBufferedImage(w, h, dataBufferFloat.getBankData(), true);
            case final javax.media.jai.DataBufferFloat dataBufferFloat -> new IcyBufferedImage(w, h, dataBufferFloat.getBankData(), true);
            case final DataBufferDouble dataBufferDouble -> new IcyBufferedImage(w, h, dataBufferDouble.getBankData(), true);
            case final javax.media.jai.DataBufferDouble dataBufferDouble -> new IcyBufferedImage(w, h, dataBufferDouble.getBankData(), true);
            // JAI keep dataType and others stuff in their BufferedImage
            case null, default -> IcyBufferedImage.createFrom(image.getAsBufferedImage());
        };
    }

    /**
     * Create an IcyBufferedImage from a {@link PlanarImage}.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     *
     * @param image {@link PlanarImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(final PlanarImage image) {
        return createFrom(image, false);
    }

    /**
     * Create an IcyBufferedImage from a BufferedImage.<br>
     * IMPORTANT : source image can be used as part or as the whole result<br>
     * so consider it as lost.
     *
     * @param image {@link BufferedImage}
     * @return {@link IcyBufferedImage}
     */
    public static IcyBufferedImage createFrom(final BufferedImage image) {
        // IcyBufferedImage --> no conversion needed
        if (image instanceof IcyBufferedImage)
            return (IcyBufferedImage) image;

        // sort of IcyBufferedImage (JAI can return that type) --> no conversion needed
        if (image.getColorModel() instanceof IcyColorModel)
            return new IcyBufferedImage(
                    IcyColorModel.createInstance((IcyColorModel) image.getColorModel(), false, false),
                    image.getRaster()
            );

        final int w = image.getWidth();
        final int h = image.getHeight();
        final int type = image.getType();
        final BufferedImage temp;
        final Graphics g;

        // we first want a component based image
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
                temp = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
                g = temp.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                break;

            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                temp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
                g = temp.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                break;

            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
                temp = image;
                break;

            default:
                // if we have severals components with an unknown / incompatible sampleModel
                if ((image.getColorModel().getNumComponents() > 1)
                        && (!(image.getSampleModel() instanceof ComponentSampleModel))) {
                    // change it to a basic ABGR components image
                    temp = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
                    g = temp.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                }
                else
                    temp = image;
                break;
        }

        // convert initial data type in our data type
        final DataType dataType = DataType.getDataTypeFromDataBufferType(temp.getColorModel().getTransferType());
        // get number of components
        final int numComponents = temp.getRaster().getNumBands();

        // create a compatible image in our format
        final IcyBufferedImage result = new IcyBufferedImage(w, h, numComponents, dataType);

        // copy data from the source image
        result.copyData(temp);

        // in some case we want to restore colormaps from source image
        switch (type) {
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_BYTE_INDEXED:
                if (numComponents == 2)
                    result.setColorMaps(image);
                break;

            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR:
                if (numComponents == 4)
                    result.setColorMap(3, LinearColorMap.alpha_, true);
                break;
        }

        return result;
    }

    /**
     * Image source information used for delayed image loading
     */
    protected ImageSourceInfo imageSourceInfo;

    /**
     * automatic update of channel bounds
     */
    protected boolean autoUpdateChannelBounds;

    /**
     * volatile data state
     */
    protected boolean volatile_;

    /**
     * required cached field as raster is volatile
     */
    protected final int width;
    protected final int height;
    protected final int minX;
    protected final int minY;
    protected final int offsetX;
    protected final int offsetY;

    /**
     * data initialized state
     */
    protected boolean dataInitialized;

    // internal lock counter
    protected int lockedCount = 0;
    // internal constructed state (needed for proper data initialization)
    private boolean constructed = false;

    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;
    /**
     * listeners
     */
    protected final List<IcyBufferedImageListener> listeners;

    // override internal colorModel field
    protected ColorModel colorModel;
    // override internal colorModel field
    protected WritableRaster raster;

    /**
     * Build an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     *
     * @param cm                      {@link IcyColorModel}
     * @param wr                      {@link WritableRaster}
     * @param autoUpdateChannelBounds If true then channel bounds are automatically calculated.
     * @param dataInitialized         When set to <code>true</code> (default), we assume the image is created with initialized data (stored in the {@link WritableRaster} object)
     *                                otherwise data will be initialized / loaded on first data request (lazy loading).
     * @param forceVolatileData       If set to <code>true</code> then image data is volatile regardless of {@link GeneralPreferences#getVirtualMode()} state and can be lost if not
     *                                specifically stored using <code>setDataxx(..)</code> methods.<br>
     *                                Image cache is used to handle data storage and can move data on disk when memory is getting low.<br>
     *                                Note that Default value for this parameter is <code>false</code>.
     */
    protected IcyBufferedImage(final IcyColorModel cm, final WritableRaster wr, final boolean autoUpdateChannelBounds, final boolean dataInitialized, final boolean forceVolatileData) {
        super(cm, cm.createDummyWritableRaster(wr.getWidth(), wr.getHeight()), false, null);

        // store it in the hashmap (weak reference)
        synchronized (images) {
            images.put(Integer.valueOf(System.identityHashCode(this)), new WeakIcyBufferedImageReference(this));
        }

        colorModel = cm;
        imageSourceInfo = null;
        width = wr.getWidth();
        height = wr.getHeight();
        minX = wr.getMinX();
        minY = wr.getMinY();
        offsetX = wr.getSampleModelTranslateX();
        offsetY = wr.getSampleModelTranslateY();

        // automatic update of channel bounds
        this.autoUpdateChannelBounds = autoUpdateChannelBounds;

        updater = new UpdateEventHandler(this, false);
        listeners = new ArrayList<>();

        // we want volatile data ?
        if (forceVolatileData || GeneralPreferences.getVirtualMode()) {
            volatile_ = true;
            raster = null;
        }
        else {
            volatile_ = false;
            raster = wr;
        }

        this.dataInitialized = dataInitialized;

        // we have initialized data ? --> save them in cache
        if (dataInitialized) {
            // save data in cache (for volatile image)
            saveRasterInCache(wr);
            // update image components bounds
            if (autoUpdateChannelBounds)
                updateChannelsBounds();
        }

        // add listener to colorModel
        cm.addListener(this);

        constructed = true;
    }

    /**
     * Build an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     *
     * @param cm                      {@link IcyColorModel}
     * @param wr                      {@link WritableRaster}
     * @param autoUpdateChannelBounds If true then channel bounds are automatically calculated.<br>
     */
    protected IcyBufferedImage(final IcyColorModel cm, final WritableRaster wr, final boolean autoUpdateChannelBounds) {
        this(cm, wr, autoUpdateChannelBounds, true, false);
    }

    /**
     * Create an Icy formatted BufferedImage, takes an IcyColorModel and a WritableRaster as input
     *
     * @param cm {@link IcyColorModel}
     * @param wr {@link WritableRaster}
     */
    protected IcyBufferedImage(final IcyColorModel cm, final WritableRaster wr) {
        this(cm, wr, false, true, false);
    }

    /**
     * Create an Icy formatted BufferedImage with specified IcyColorModel, width and height.<br>
     * Private version, {@link IcyColorModel} is directly used internally.
     */
    protected IcyBufferedImage(final IcyColorModel cm, final int width, final int height, final boolean forceVolatileData) {
        this(cm, cm.createCompatibleWritableRaster(width, height), false, false, forceVolatileData);
    }

    /**
     * Create an Icy formatted BufferedImage with specified IcyColorModel, width and height.<br>
     * Private version, {@link IcyColorModel} is directly used internally.
     */
    protected IcyBufferedImage(final IcyColorModel cm, final int width, final int height) {
        this(cm, cm.createCompatibleWritableRaster(width, height), false, false, false);
    }

    /**
     * Create an Icy formatted BufferedImage with specified colorModel, width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as it allow you to
     * directly send the data array and disable the channel bounds calculation.
     *
     * @param cm                      the color model
     * @param data                    image data<br>
     *                                Should be a 2D array with first dimension giving the number of component<br>
     *                                and second dimension equals to <code>width * height</code><br>
     *                                The array data type specify the internal data type and should match the given color
     *                                model parameter.
     * @param autoUpdateChannelBounds If true then channel bounds are automatically calculated.<br>
     *                                When set to false, you have to set bounds manually by calling
     *                                {@link #updateChannelsBounds()} or #setC
     */
    protected IcyBufferedImage(final IcyColorModel cm, final Object[] data, final int width, final int height, final boolean autoUpdateChannelBounds) {
        this(cm, cm.createWritableRaster(data, width, height), autoUpdateChannelBounds);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as
     * it allow you to directly send the data array and disable the channel bounds calculation.
     *
     * @param data                    image data<br>
     *                                Should be a 2D array with first dimension giving the number of component<br>
     *                                and second dimension equals to <code>width * height</code><br>
     *                                The array data type specify the internal data type.
     * @param signed                  use signed data for data type
     * @param autoUpdateChannelBounds If true then channel bounds are automatically calculated.<br>
     *                                When set to false, you have to set bounds manually by calling
     *                                {@link #updateChannelsBounds()} or #setC
     */
    public IcyBufferedImage(final int width, final int height, final Object[] data, final boolean signed, final boolean autoUpdateChannelBounds) {
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height, autoUpdateChannelBounds);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480]);</code>
     *
     * @param data   image data<br>
     *               Should be a 2D array with first dimension giving the number of component<br>
     *               and second dimension equals to <code>width * height</code><br>
     *               The array data type specify the internal data type.
     * @param signed use signed data for data type
     */
    public IcyBufferedImage(final int width, final int height, final Object[] data, final boolean signed) {
        this(IcyColorModel.createInstance(data.length, ArrayUtil.getDataType(data[0], signed)), data, width, height, false);
    }

    /**
     * Create an Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[3][640 * 480]);</code>
     *
     * @param data image data<br>
     *             Should be a 2D array with first dimension giving the number of component<br>
     *             and second dimension equals to <code>width * height</code><br>
     *             The array data type specify the internal data type.
     */
    public IcyBufferedImage(final int width, final int height, final Object[] data) {
        this(width, height, data, false);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480], true);</code><br>
     * <br>
     * This constructor provides the best performance for massive image creation and computation as it allow you to
     * directly send the data array and disable the channel bounds calculation.
     *
     * @param data                    image data array.<br>
     *                                The length of the array should be equals to <code>width * height</code>.<br>
     *                                The array data type specify the internal data type.
     * @param signed                  use signed data for data type
     * @param autoUpdateChannelBounds If true then channel bounds are automatically calculated.<br>
     *                                When set to false, you have to set bounds manually by calling
     *                                {@link #updateChannelsBounds()} or #setC
     * @see #IcyBufferedImage(int, int, Object[], boolean, boolean)
     */
    public IcyBufferedImage(final int width, final int height, final Object data, final boolean signed, final boolean autoUpdateChannelBounds) {
        this(width, height, ArrayUtil.encapsulate(data), signed, autoUpdateChannelBounds);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480]);</code>
     *
     * @param data   image data<br>
     *               The length of the array should be equals to <code>width * height</code>.<br>
     *               The array data type specify the internal data type.
     * @param signed use signed data for data type
     */
    public IcyBufferedImage(final int width, final int height, final Object data, final boolean signed) {
        this(width, height, ArrayUtil.encapsulate(data), signed);
    }

    /**
     * Create a single channel Icy formatted BufferedImage with specified width, height and input
     * data.<br>
     * ex : <code>img = new IcyBufferedImage(640, 480, new byte[640 * 480]);</code>
     *
     * @param data image data<br>
     *             The length of the array should be equals to <code>width * height</code>.<br>
     *             The array data type specify the internal data type.
     */
    public IcyBufferedImage(final int width, final int height, final Object data) {
        this(width, height, ArrayUtil.encapsulate(data));
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height,<br>
     * number of component and dataType.
     *
     * @param dataType image data type {@link DataType}
     */
    public IcyBufferedImage(final int width, final int height, final int numComponents, final DataType dataType, final boolean forceVolatileData) {
        this(IcyColorModel.createInstance(numComponents, dataType), width, height, forceVolatileData);
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height,<br>
     * number of component and dataType.
     *
     * @param dataType image data type {@link DataType}
     */
    public IcyBufferedImage(final int width, final int height, final int numComponents, final DataType dataType) {
        this(IcyColorModel.createInstance(numComponents, dataType), width, height, false);
    }

    /**
     * Create an ICY formatted BufferedImage with specified width, height and IcyColorModel
     * type.<br>
     */
    public IcyBufferedImage(final int width, final int height, final IcyColorModel cm) {
        this(width, height, cm.getNumComponents(), cm.getDataType());
    }

    @Override
    public void close() throws Exception {
        // cancel any pending loading tasks for this image
        imageDataLoader.cancelTasks(this);
        // image has been released, be sure to clear cache
        if (ImageCache.isInit())
            ImageCache.remove(this);

        // remove it from hashmap
        synchronized (images) {
            images.remove(Integer.valueOf(System.identityHashCode(this)));
        }
    }

    public ImageSourceInfo getImageSourceInfo() {
        return imageSourceInfo;
    }

    /**
     * Set the image source information that will be used later for lazy image data loading.
     */
    public void setImageSourceInfo(final SequenceIdImporter imp, final int series, final int resolution, final Rectangle region, final int t, final int z, final int c) {
        imageSourceInfo = new ImageSourceInfo(imp, series, resolution, region, t, z, c);
    }

    /**
     * Returns <code>true</code> if data is initialized
     */
    public boolean isDataInitialized() {
        return dataInitialized;
    }

    /**
     * Same as {@link #isDataInitialized()}
     *
     * @see #isDataInitialized()
     */
    public boolean isDataLoaded() {
        return isDataInitialized();
    }

    /**
     * Returns <code>true</code> if image data is volatile.<br>
     * Volatile data means <b>there is no strong reference on the internal data arrays</b> (data can be cached on disk) and so <b>any <i>external</i> changes on
     * them can be lost</b> if they has not been specifically set using setDataxx() methods.
     * Volatile is useful when you want to load many images with low memory consumption but you should use it carefully as it has some limitations.<br>
     *
     * @see #setVolatile(boolean)
     */
    public boolean isVolatile() {
        return volatile_;
    }

    /**
     * Sets the <i>volatile</i> state for this image.<br>
     * Volatile data means <b>there is no strong reference on the internal data arrays</b> (data can be cached on disk) and so <b>any <i>external</i> changes on
     * them can be lost</b> if they has not been specifically set using setDataxx() methods.
     * Volatile is useful when you want to load many images with low memory consumption but you should use it carefully as it has some limitations.<br>
     * Setting the image to volatile <b>immediately release internal strong reference on data arrays</b> (see {@link #lockRaster()} and
     * {@link #releaseRaster(boolean)} methods).<br>
     *
     * @throws OutOfMemoryError              if there is not enough memory available to store image
     *                                       data when setting back to <i>non volatile</i> state
     * @throws UnsupportedOperationException if cache engine is not initialized (error at initialization).
     */
    public void setVolatile(final boolean value) throws OutOfMemoryError, UnsupportedOperationException {
        if (value == volatile_)
            return;

        // we want volatile data but cache engine isn't enabled ?
        if (value && !ImageCache.isInit())
            throw new UnsupportedOperationException("IcyBufferedImage.setVolatile(..) error: Image cache is disabled.");

        // important to set it before doing state switch
        volatile_ = value;

        // we want volatile data ?
        if (value) {
            if (raster != null) {
                // save data in cache as we will release the strong reference
                saveRasterInCache(raster);
                // immediately remove strong reference to data
                raster = null;
            }
        }
        // no volatile anymore ?
        else {
            try {
                // data not yet initialized ? we don't want to force data loading for that
                if (!isDataInitialized())
                    raster = buildRaster(createEmptyRasterData());
                    // otherwise we just load the data and store it
                else
                    raster = getRaster();

                // clear data set in cache (no more used)
                ImageCache.remove(this);
            }
            catch (final OutOfMemoryError e) {
                IcyLogger.error(IcyBufferedImage.class, e, "IcyBufferedImage.setVolatile(false) error: not enough memory to set image data back in memory.");
                throw e;
            }
            catch (final Throwable e) {
                IcyLogger.error(IcyBufferedImage.class, e, "IcyBufferedImage.setVolatile(..) error.");
            }
        }
    }

    /**
     * Force loading data for this image (so channel bounds can be correctly computed even with lazy loading)
     */
    public void loadData() {
        if (!isDataInitialized())
            // that is enough to get data loaded
            getRaster();
    }

    protected synchronized WritableRaster getRasterInternal() {
        // always try first from direct reference
        WritableRaster result = raster;

        // data not yet initialized ?
        if (constructed && !isDataInitialized()) {
            // initialize data
            final Object rasterData = initializeData();

            // could not initialize data ? --> use temporary empty data (we want to retry data initialization later)
            if (rasterData == null)
                return buildRaster(createEmptyRasterData());

            // save them in cache (for volatile image) but don't need to be eternal
            saveRasterDataInCache(rasterData);
            // we have the parent raster ? --> update its data (important to do it before setting data initialized)
            if (result != null)
                setRasterData(result, rasterData);

            // data is initialized (important to set it before updating channel bounds)
            dataInitialized = true;

            // update image channels bounds
            if (autoUpdateChannelBounds)
                updateChannelsBounds();
        }

        // we don't have the direct reference raster (mean we have a volatile image) ?
        if (result == null)
            // get it from cache
            result = loadRasterFromCache();

        return result;
    }

    @Override
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * Set the {@link IcyColorModel}
     */
    public void setColorModel(final IcyColorModel cm) {
        if (colorModel != cm) {
            if (colorModel instanceof IcyColorModel)
                ((IcyColorModel) colorModel).removeListener(this);

            colorModel = cm;

            if (cm != null)
                cm.addListener(this);
        }
    }

    @Override
    public int getTransparency() {
        return getColorModel().getTransparency();
    }

    @Override
    public boolean isAlphaPremultiplied() {
        return getColorModel().isAlphaPremultiplied();
    }

    @Override
    public WritableRaster getRaster() {
        return getRasterInternal();
    }

    @Override
    public WritableRaster getAlphaRaster() {
        return getColorModel().getAlphaRaster(getRaster());
    }

    /**
     * @return <code>true</code> if raster data is strongly referenced, <code>false</code> otherwise.<br>
     * Note that it doesn't necessary mean that we called {@link #lockRaster()} method.
     * @see #lockRaster()
     * @see #isVolatile()
     */
    public synchronized boolean isRasterLocked() {
        return raster != null;
    }

    /**
     * Ensure raster data remains strongly referenced until we call {@link #releaseRaster(boolean)}.<br>
     * This is important to lock / release raster for Volatile image when you are modifying data externally otherwise data could be lost.
     *
     * @see #releaseRaster(boolean)
     * @see #isVolatile()
     */
    public synchronized void lockRaster() {
        if (lockedCount++ != 0)
            return;

        // strong reference
        raster = getRaster();
    }

    /**
     * Release the raster object.
     *
     * @param saveInCache force to save raster data in cache (for volatile image only)
     */
    public synchronized void releaseRaster(final boolean saveInCache) {
        if (--lockedCount != 0)
            return;

        // volatile data ?
        if (isVolatile()) {
            if (raster != null) {
                // force saving changed data in cache before releasing raster
                if (saveInCache)
                    saveRasterInCache(raster);
                // set strong reference to null (allow to release raster object)
                raster = null;
            }
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth(final ImageObserver observer) {
        return width;
    }

    @Override
    public int getHeight(final ImageObserver observer) {
        return height;
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getTileWidth() {
        return getWidth();
    }

    @Override
    public int getTileHeight() {
        return getHeight();
    }

    @Override
    public int getTileGridXOffset() {
        return offsetX;
    }

    @Override
    public int getTileGridYOffset() {
        return offsetY;
    }

    @Override
    public SampleModel getSampleModel() {
        return getRaster().getSampleModel();
    }

    @Override
    public void coerceData(final boolean isAlphaPremultiplied) {
        // don't need to do any conversion here...
    }

    @Override
    public WritableRaster copyData(final WritableRaster outRaster) {
        if (outRaster == null)
            return (WritableRaster) getData();

        final WritableRaster wr = getRaster();
        final int width = outRaster.getWidth();
        final int height = outRaster.getHeight();
        final int startX = outRaster.getMinX();
        final int startY = outRaster.getMinY();

        Object tdata = null;

        for (int i = startY; i < startY + height; i++) {
            tdata = wr.getDataElements(startX, i, width, 1, tdata);
            outRaster.setDataElements(startX, i, width, 1, tdata);
        }

        return outRaster;
    }

    @Override
    public Raster getTile(final int tileX, final int tileY) {
        if ((tileX == 0) && (tileY == 0))
            return getRaster();

        throw new ArrayIndexOutOfBoundsException("BufferedImages only have one tile with index 0,0");
    }

    @Override
    public WritableRaster getWritableTile(final int tileX, final int tileY) {
        lockRaster();
        return getRaster();
    }

    @Override
    public void releaseWritableTile(final int tileX, final int tileY) {
        releaseRaster(true);
    }

    @Override
    public Raster getData() {
        final WritableRaster wr = getRaster();
        final WritableRaster result = Raster.createWritableRaster(wr.getSampleModel(), new Point(wr.getSampleModelTranslateX(), wr.getSampleModelTranslateY()));

        // REMIND : this allocates a whole new tile if raster is a
        // subtile. (It only copies in the requested area)
        // We should do something smarter.
        final int width = wr.getWidth();
        final int height = wr.getHeight();
        final int startX = wr.getMinX();
        final int startY = wr.getMinY();

        Object tdata = null;

        for (int i = startY; i < startY + height; i++) {
            tdata = wr.getDataElements(startX, i, width, 1, tdata);
            result.setDataElements(startX, i, width, 1, tdata);
        }

        return result;
    }

    @Override
    public Raster getData(final Rectangle rect) {
        final WritableRaster wr = getRaster();
        final SampleModel sm = wr.getSampleModel();
        final SampleModel nsm = sm.createCompatibleSampleModel(rect.width, rect.height);
        final WritableRaster result = Raster.createWritableRaster(nsm, rect.getLocation());
        final int width = rect.width;
        final int height = rect.height;
        final int startX = rect.x;
        final int startY = rect.y;

        Object tdata = null;

        for (int i = startY; i < startY + height; i++) {
            tdata = wr.getDataElements(startX, i, width, 1, tdata);
            result.setDataElements(startX, i, width, 1, tdata);
        }

        return result;
    }

    @Override
    public void setData(final Raster r) {
        final WritableRaster wr = getRaster();
        int width = r.getWidth();
        int height = r.getHeight();
        int startX = r.getMinX();
        int startY = r.getMinY();

        int[] tdata = null;

        // Clip to the current Raster
        final Rectangle rclip = new Rectangle(startX, startY, width, height);
        final Rectangle bclip = new Rectangle(0, 0, wr.getWidth(), wr.getHeight());
        final Rectangle intersect = rclip.intersection(bclip);

        // empty --> nothing to do
        if (intersect.isEmpty())
            return;

        width = intersect.width;
        height = intersect.height;
        startX = intersect.x;
        startY = intersect.y;

        // remind use get/setDataElements for speed if Rasters are
        // compatible
        for (int i = startY; i < startY + height; i++) {
            tdata = r.getPixels(startX, i, width, 1, tdata);
            wr.setPixels(startX, i, width, 1, tdata);
        }

        // save modified data in cache (for volatile data)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    @Override
    public int getRGB(final int x, final int y) {
        return getColorModel().getRGB(getRaster().getDataElements(x, y, null));
    }

    @Override
    public int[] getRGB(final int startX, final int startY, final int w, final int h, int[] rgbArray, final int offset, final int scansize) {
        final IcyColorModel cm = getIcyColorModel();
        final WritableRaster wr = getRaster();
        int yoff = offset;
        int off;
        final int nbands = wr.getNumBands();
        final int dataType = wr.getDataBuffer().getDataType();
        final Object data = switch (dataType) {
            case DataBuffer.TYPE_BYTE -> new byte[nbands];
            case DataBuffer.TYPE_USHORT -> new short[nbands];
            case DataBuffer.TYPE_INT -> new int[nbands];
            case DataBuffer.TYPE_FLOAT -> new float[nbands];
            case DataBuffer.TYPE_DOUBLE -> new double[nbands];
            default -> throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
        };

        if (rgbArray == null)
            rgbArray = new int[offset + h * scansize];

        for (int y = startY; y < startY + h; y++, yoff += scansize) {
            off = yoff;
            for (int x = startX; x < startX + w; x++)
                rgbArray[off++] = cm.getRGB(wr.getDataElements(x, y, data));
        }

        return rgbArray;
    }

    @Override
    public synchronized void setRGB(final int x, final int y, final int rgb) {
        final WritableRaster wr = getRaster();
        wr.setDataElements(x, y, getColorModel().getDataElements(rgb, null));
        // FIXME: implement delayed cache saving to avoid very poor performance here
        saveRasterInCache(wr);
    }

    @Override
    public void setRGB(final int startX, final int startY, final int w, final int h, final int[] rgbArray, final int offset, final int scansize) {
        final IcyColorModel cm = getIcyColorModel();
        final WritableRaster wr = getRaster();
        int yoff = offset;
        int off;
        Object pixel = null;

        for (int y = startY; y < startY + h; y++, yoff += scansize) {
            off = yoff;
            for (int x = startX; x < startX + w; x++) {
                pixel = cm.getDataElements(rgbArray[off++], pixel);
                wr.setDataElements(x, y, pixel);
            }
        }

        // save changes in cache (for volatile data)
        saveRasterInCache(wr);
        // data changed
        dataChanged();
    }

    /**
     * Return the owner sequence (can be null if the image is not owned in any Sequence)
     */
    public Sequence getOwnerSequence() {
        // just use the listeners to find it (Sequence always listen image event)
        for (final IcyBufferedImageListener listener : listeners)
            if (listener instanceof Sequence)
                return (Sequence) listener;

        return null;
    }

    protected WritableRaster loadRasterFromCache() {
        Object rasterData = null;
        boolean datalost = false;

        try {
            // get data from cache
            rasterData = ImageCache.get(this);
        }
        catch (final Throwable e) {
            datalost = true;
            IcyLogger.error(IcyBufferedImage.class, e, "Data lost: " + e.getLocalizedMessage());
        }

        // should happen only for unmodified data
        if (rasterData == null) {
            // we should be able to initialize data back
            rasterData = initializeData();

            // couldn't initialize data ? create empty data without saving in cache (we want to retry later)
            if (rasterData == null) {
                rasterData = createEmptyRasterData();

                //// don't notify twice
                // if (!datalost)
                // System.err.println("IcyBufferedImage.loadRasterFromCache: cannot get image data (data lost)");
            }
            else {
                // data could not be loaded from cache but was correctly restored
                if (datalost)
                    IcyLogger.error(IcyBufferedImage.class, "Data re-initialized (changes are lost).");

                // save it in cache (not eternal here as this is default data)
                saveRasterDataInCache(rasterData);
            }
        }

        return buildRaster(rasterData);
    }

    /**
     * Explicitly save the image data in cache (only for volatile image)
     *
     * @see #isVolatile()
     */
    public void saveDataInCache() {
        // need to be saved only if initialized
        if (isDataInitialized())
            saveRasterInCache(getRaster());
    }

    protected void saveRasterInCache(final WritableRaster wr) {
        //saveRasterDataInCache(getRasterData(wr), true);
        saveRasterDataInCache(getRasterData(wr));
    }

    protected void saveRasterDataInCache(final Object rasterData) {
        //saveRasterDataInCache(rasterData, true);
        if (isVolatile()) {
            try {
                ImageCache.set(this, rasterData);
            }
            catch (final Throwable e) {
                IcyLogger.error(IcyBufferedImage.class, e, "Unable to save raster data in cache.");
            }
        }
    }

    /**
     * Build raster from an Object (internally the Object is always a 2D array of native data type)
     */
    protected WritableRaster buildRaster(final Object data) {
        return IcyColorModel.createWritableRaster(data, getWidth(), getHeight());
    }

    /**
     * Returns raster data in Object format (internally we always have a 2D array of native data type)
     */
    protected static Object getRasterData(final WritableRaster wr) {
        final DataBuffer db = wr.getDataBuffer();

        return switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE -> ((DataBufferByte) db).getBankData();
            case DataBuffer.TYPE_USHORT -> ((DataBufferUShort) db).getBankData();
            case DataBuffer.TYPE_SHORT -> ((DataBufferShort) db).getBankData();
            case DataBuffer.TYPE_INT -> ((DataBufferInt) db).getBankData();
            case DataBuffer.TYPE_FLOAT -> ((DataBufferFloat) db).getBankData();
            case DataBuffer.TYPE_DOUBLE -> ((DataBufferDouble) db).getBankData();
            default -> null;
        };
    }

    /**
     * Set raster data (Object is always a 2D array of native data type)
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    protected void setRasterData(final WritableRaster wr, final Object data) {
        final DataBuffer db = wr.getDataBuffer();

        final Object dest = switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE -> ((DataBufferByte) db).getBankData();
            case DataBuffer.TYPE_USHORT -> ((DataBufferUShort) db).getBankData();
            case DataBuffer.TYPE_SHORT -> ((DataBufferShort) db).getBankData();
            case DataBuffer.TYPE_INT -> ((DataBufferInt) db).getBankData();
            case DataBuffer.TYPE_FLOAT -> ((DataBufferFloat) db).getBankData();
            case DataBuffer.TYPE_DOUBLE -> ((DataBufferDouble) db).getBankData();
            default -> null;
        };

        if (dest != null) {
            final int len = Array.getLength(dest);
            for (int i = 0; i < len; i++) {
                final Object destSub = Array.get(dest, i);
                System.arraycopy(Array.get(data, i), 0, destSub, 0, Array.getLength(destSub));
            }
        }
    }

    protected Object initializeData() {
        try {
            // load data from importer
            return loadDataFromImporter();
        }
        catch (final InterruptedException e) {
            // FIXME: better to disable it as when we interrupt ROI descriptor computation (by switching or closing an image)
            // then we can have a lot of those messages appearing
            // System.err.println(
            // "IcyBufferedImage.loadDataFromImporter() warning: image loading from ImageProvider was interrupted (image data not retrieved).");

            // we want to keep the interrupted state here
            Thread.currentThread().interrupt();

            return null;
        }
        catch (final ClosedByInterruptException e) {
            // this one should never happen as loading is done in a separate thread (executor)
            IcyLogger.error(IcyBufferedImage.class, "IcyBufferedImage.loadDataFromImporter() error: image loading from ImageProvider was interrupted (further image won't be loaded) !");

            // we want to keep the interrupted state here
            Thread.currentThread().interrupt();

            return null;
        }
        catch (final Exception e) {
            IcyLogger.error(IcyBufferedImage.class, e, "IcyBufferedImage.loadDataFromImporter() warning: cannot get image from ImageProvider (possible data loss).");

            return null;
        }
    }

    protected Object createEmptyRasterData() {
        final DataType dataType = getDataType();
        final int sizeC = getSizeC();
        final int sizeXY = getSizeX() * getSizeY();

        // create the result array (always 2D native type)
        final Object[] result = Array2DUtil.createArray(dataType, sizeC);
        assert result != null;

        for (int c = 0; c < sizeC; c++)
            result[c] = Array1DUtil.createArray(dataType, sizeXY);

        return result;
    }

    protected Object loadDataFromImporter() throws UnsupportedFormatException, IOException, InterruptedException {
        // image source information not defined (not attached to importer) ? --> create empty data
        if (imageSourceInfo == null)
            return createEmptyRasterData();

        try {
            // get data from importer using
            return imageDataLoader.loadImageData(this);
        }
        catch (final ExecutionException e) {
            final Throwable cause = e.getCause();

            if (cause instanceof UnsupportedFormatException)
                throw ((UnsupportedFormatException) cause);
            if (cause instanceof IOException)
                throw ((IOException) cause);
            throw new IOException(cause);
        }
    }

    /**
     * @return true is channel bounds are automatically updated when image data is modified.
     * @see #setAutoUpdateChannelBounds(boolean)
     */
    public boolean getAutoUpdateChannelBounds() {
        return autoUpdateChannelBounds;
    }

    /**
     * If set to <code>true</code> (default) then channel bounds will be automatically recalculated
     * when image data is modified.<br>
     * This can consume some time if you make many updates on a large image.<br>
     * In this case you should do your updates in a {@link #beginUpdate()} ... {@link #endUpdate()}
     * block to avoid
     * severals recalculation.
     */
    public void setAutoUpdateChannelBounds(final boolean value) {
        if (autoUpdateChannelBounds != value) {
            if (value)
                updateChannelsBounds();

            autoUpdateChannelBounds = value;
        }
    }

    /**
     * Return a single component image corresponding to the component c of current image.<br>
     * This actually create a new image which share its data with internal image
     * so any modifications to one affect the other.<br>
     * if <code>(c == -1)</code> then current image is directly returned<br>
     * if <code>((c == 0) || (sizeC == 1))</code> then current image is directly returned<br>
     * if <code>((c &lt; 0) || (c &gt;= sizeC))</code> then it returns <code>null</code>
     *
     * @see IcyBufferedImageUtil#extractChannel(IcyBufferedImage, int)
     * @since version 1.0.3.3b
     */
    public IcyBufferedImage getImage(final int c) {
        if (c == -1)
            return this;

        final int sizeC = getSizeC();

        if ((c < 0) || (c >= sizeC))
            return null;
        if (sizeC == 1)
            return this;

        return new IcyBufferedImage(getWidth(), getHeight(), getDataXY(c), isSignedDataType());
    }

    /**
     * Get calculated image channel bounds (min and max values)
     */
    protected double[] getCalculatedChannelBounds(final int channel) {
        // don't load data for that, just wait that data is loaded naturally
        if (!isDataInitialized())
            return new double[]{0d, 0d};

        final DataType dataType = getDataType();

        final boolean signed = dataType.isSigned();
        final Object data = getDataXY(channel);

        final double min = ArrayMath.min(data, signed);
        final double max = ArrayMath.max(data, signed);

        return new double[]{min, max};
    }

    /**
     * Adjust specified bounds depending internal data type
     */
    protected double[] adjustBoundsForDataType(final double[] bounds) {
        double min, max;

        min = bounds[0];
        max = bounds[1];

        // only for integer data type
        if (!isFloatDataType()) {
            // we force min to 0 if > 0
            if (min > 0d)
                min = 0d;
            // we force max to 0 if < 0
            if (max < 0d)
                max = 0d;
        }

        final DataType dataType = getDataType();

        switch (dataType.getJavaType()) {
            default:
            case BYTE:
                // return default bounds ([0..255] / [-128..127])
                return dataType.getDefaultBounds();

            case SHORT:
            case INT:
            case LONG:
                min = MathUtil.prevPow2((long) min + 1);
                max = MathUtil.nextPow2Mask((long) max);
                break;

            case FLOAT:
            case DOUBLE:
                // if [min..max] is included in [-1..1]
                if ((min >= -1d) && (max <= 1d)) {
                    min = MathUtil.prevPow10(min);
                    max = MathUtil.nextPow10(max);
                }
                break;
        }

        return new double[]{min, max};
    }

    /**
     * Get the data type minimum value.
     */
    public double getDataTypeMin() {
        return getDataType().getMinValue();
    }

    /**
     * Get the data type maximum value.
     */
    public double getDataTypeMax() {
        return getDataType().getMaxValue();
    }

    /**
     * Get data type bounds (min and max values)
     */
    public double[] getDataTypeBounds() {
        return new double[]{getDataTypeMin(), getDataTypeMax()};
    }

    /**
     * Get the minimum type value for the specified channel.
     */
    public double getChannelTypeMin(final int channel) {
        return getIcyColorModel().getComponentAbsMinValue(channel);
    }

    /**
     * Get the maximum type value for the specified channel.
     */
    public double getChannelTypeMax(final int channel) {
        return getIcyColorModel().getComponentAbsMaxValue(channel);
    }

    /**
     * Get type bounds (min and max values) for the specified channel.
     */
    public double[] getChannelTypeBounds(final int channel) {
        return getIcyColorModel().getComponentAbsBounds(channel);
    }

    /**
     * Get type bounds (min and max values) for all channels.
     */
    public double[][] getChannelsTypeBounds() {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelTypeBounds(c);

        return result;
    }

    /**
     * Get global type bounds (min and max values) for all channels.
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
     * Get the minimum value for the specified channel.
     */
    public double getChannelMin(final int channel) {
        return getIcyColorModel().getComponentUserMinValue(channel);
    }

    /**
     * Get maximum value for the specified channel.
     */
    public double getChannelMax(final int channel) {
        return getIcyColorModel().getComponentUserMaxValue(channel);
    }

    /**
     * Get bounds (min and max values) for the specified channel.
     */
    public double[] getChannelBounds(final int channel) {
        return getIcyColorModel().getComponentUserBounds(channel);
    }

    /**
     * Get bounds (min and max values) for all channels.
     */
    public double[][] getChannelsBounds() {
        final int sizeC = getSizeC();
        final double[][] result = new double[sizeC][];

        for (int c = 0; c < sizeC; c++)
            result[c] = getChannelBounds(c);

        return result;
    }

    /**
     * Get global bounds (min and max values) for all channels.
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
     * Set the preferred data type minimum value for the specified channel.
     */
    public void setChannelTypeMin(final int channel, final double min) {
        getIcyColorModel().setComponentAbsMinValue(channel, min);
    }

    /**
     * Set the preferred data type maximum value for the specified channel.
     */
    public void setChannelTypeMax(final int channel, final double max) {
        getIcyColorModel().setComponentAbsMaxValue(channel, max);
    }

    /**
     * /**
     * Set the preferred data type min and max values for the specified channel.
     */
    public void setChannelTypeBounds(final int channel, final double min, final double max) {
        getIcyColorModel().setComponentAbsBounds(channel, min, max);
    }

    /**
     * Set the preferred data type bounds (min and max values) for all channels.
     */
    public void setChannelsTypeBounds(final double[][] bounds) {
        getIcyColorModel().setComponentsAbsBounds(bounds);
    }

    /**
     * Set channel minimum value.
     */
    public void setChannelMin(final int channel, final double min) {
        final IcyColorModel cm = getIcyColorModel();

        if ((min < cm.getComponentAbsMinValue(channel)))
            cm.setComponentAbsMinValue(channel, min);
        cm.setComponentUserMinValue(channel, min);
    }

    /**
     * Set channel maximum value.
     */
    public void setChannelMax(final int channel, final double max) {
        final IcyColorModel cm = getIcyColorModel();

        if ((max > cm.getComponentAbsMaxValue(channel)))
            cm.setComponentAbsMinValue(channel, max);
        cm.setComponentUserMaxValue(channel, max);
    }

    /**
     * Set channel bounds (min and max values)
     */
    public void setChannelBounds(final int channel, final double min, final double max) {
        final IcyColorModel cm = getIcyColorModel();
        final double[] typeBounds = cm.getComponentAbsBounds(channel);

        if ((min < typeBounds[0]) || (max > typeBounds[1]))
            cm.setComponentAbsBounds(channel, min, max);
        cm.setComponentUserBounds(channel, min, max);
    }

    /**
     * Set all channel bounds (min and max values)
     */
    public void setChannelsBounds(final double[][] bounds) {
        // we use the setChannelBounds(..) method so we do range check
        for (int c = 0; c < bounds.length; c++) {
            final double[] b = bounds[c];
            setChannelBounds(c, b[0], b[1]);
        }
    }

    /**
     * Update channels bounds (min and max values).
     */
    public void updateChannelsBounds() {
        final IcyColorModel cm = getIcyColorModel();

        if (cm != null) {
            final int sizeC = getSizeC();

            for (int c = 0; c < sizeC; c++) {
                // get data type bounds
                final double[] bounds = getCalculatedChannelBounds(c);

                cm.setComponentAbsBounds(c, adjustBoundsForDataType(bounds));
                cm.setComponentUserBounds(c, bounds);

                // we do user bounds adjustment on "non ALPHA" component only
                // if (cm.getColorMap(c).getType() != IcyColorMapType.ALPHA)
                // cm.setComponentUserBounds(c, bounds);
            }
        }
    }

    /**
     * Return true if point is inside the image
     */
    public boolean isInside(final Point p) {
        return isInside(p.x, p.y);
    }

    /**
     * Return true if point of coordinate (x, y) is inside the image
     */
    public boolean isInside(final int x, final int y) {
        return (x >= 0) && (x < getSizeX()) && (y >= 0) && (y < getSizeY());
    }

    /**
     * Return true if point of coordinate (x, y) is inside the image
     */
    public boolean isInside(final double x, final double y) {
        return (x >= 0) && (x < getSizeX()) && (y >= 0) && (y < getSizeY());
    }

    /**
     * Return the IcyColorModel
     *
     * @return IcyColorModel
     */
    public IcyColorModel getIcyColorModel() {
        return (IcyColorModel) getColorModel();
    }

    /**
     * Return the data type of this image
     *
     * @return dataType
     * @see DataType
     */
    public DataType getDataType() {
        return getIcyColorModel().getDataType();
    }

    /**
     * Return true if this is a float data type image
     */
    public boolean isFloatDataType() {
        return getDataType().isFloat();
    }

    /**
     * Return true if this is a signed data type image
     */
    public boolean isSignedDataType() {
        return getDataType().isSigned();
    }

    /**
     * @return the number of components of this image
     */
    public int getSizeC() {
        return getColorModel().getNumComponents();
    }

    /**
     * @return the width of the image
     */
    public int getSizeX() {
        return getWidth();
    }

    /**
     * @return the height of the image
     */
    public int getSizeY() {
        return getHeight();
    }

    /**
     * Return 2D dimension of image {sizeX, sizeY}
     */
    public Dimension getDimension() {
        return new Dimension(getSizeX(), getSizeY());
    }

    /**
     * Return 2D bounds of image {0, 0, sizeX, sizeY}
     */
    public Rectangle getBounds() {
        return new Rectangle(getSizeX(), getSizeY());
    }

    /**
     * Return the number of sample.<br>
     * This is equivalent to<br>
     * <code>getSizeX() * getSizeY() * getSizeC()</code>
     */
    public int getNumSample() {
        return getSizeX() * getSizeY() * getSizeC();
    }

    /**
     * Return the offset for specified (x, y) location
     */
    public int getOffset(final int x, final int y) {
        return (y * getWidth()) + x;
    }

    /**
     * create a compatible LUT for this image.
     *
     * @param createColorModel set to <code>true</code> to create a LUT using a new compatible ColorModel else it
     *                         will use the image
     *                         internal ColorModel
     */
    public LUT createCompatibleLUT(final boolean createColorModel) {
        final IcyColorModel cm;

        if (createColorModel)
            cm = IcyColorModel.createInstance(getIcyColorModel(), false, false);
        else
            cm = getIcyColorModel();

        return new LUT(cm);
    }

    /**
     * create a compatible LUT for this image
     */
    public LUT createCompatibleLUT() {
        return createCompatibleLUT(true);
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public Object getDataXYC() {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataXYCAsByte();
            case SHORT -> getDataXYCAsShort();
            case INT -> getDataXYCAsInt();
            case FLOAT -> getDataXYCAsFloat();
            case DOUBLE -> getDataXYCAsDouble();
            default -> null;
        };
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public Object getDataXY(final int c) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataXYAsByte(c);
            case SHORT -> getDataXYAsShort(c);
            case INT -> getDataXYAsInt(c);
            case FLOAT -> getDataXYAsFloat(c);
            case DOUBLE -> getDataXYAsDouble(c);
            default -> null;
        };
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public Object getDataCopyXYC() {
        return getDataCopyXYC(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXYC(final Object out, final int offset) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyXYCAsByte((byte[]) out, offset);
            case SHORT -> getDataCopyXYCAsShort((short[]) out, offset);
            case INT -> getDataCopyXYCAsInt((int[]) out, offset);
            case FLOAT -> getDataCopyXYCAsFloat((float[]) out, offset);
            case DOUBLE -> getDataCopyXYCAsDouble((double[]) out, offset);
            default -> null;
        };
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c
     */
    public Object getDataCopyXY(final int c) {
        return getDataCopyXY(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyXY(final int c, final Object out, final int offset) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyXYAsByte(c, (byte[]) out, offset);
            case SHORT -> getDataCopyXYAsShort(c, (short[]) out, offset);
            case INT -> getDataCopyXYAsInt(c, (int[]) out, offset);
            case FLOAT -> getDataCopyXYAsFloat(c, (float[]) out, offset);
            case DOUBLE -> getDataCopyXYAsDouble(c, (double[]) out, offset);
            default -> null;
        };
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]
     */
    public Object getDataCopyCXY() {
        return getDataCopyCXY(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyCXY(final Object out, final int offset) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyCXYAsByte((byte[]) out, offset);
            case SHORT -> getDataCopyCXYAsShort((short[]) out, offset);
            case INT -> getDataCopyCXYAsInt((int[]) out, offset);
            case FLOAT -> getDataCopyCXYAsFloat((float[]) out, offset);
            case DOUBLE -> getDataCopyCXYAsDouble((double[]) out, offset);
            default -> null;
        };

    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public Object getDataCopyC(final int x, final int y) {
        return getDataCopyC(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public Object getDataCopyC(final int x, final int y, final Object out, final int offset) {
        return switch (getDataType().getJavaType()) {
            case BYTE -> getDataCopyCAsByte(x, y, (byte[]) out, offset);
            case SHORT -> getDataCopyCAsShort(x, y, (short[]) out, offset);
            case INT -> getDataCopyCAsInt(x, y, (int[]) out, offset);
            case FLOAT -> getDataCopyCAsFloat(x, y, (float[]) out, offset);
            case DOUBLE -> getDataCopyCAsDouble(x, y, (double[]) out, offset);
            default -> null;
        };
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXY(final int c, final Object values) {
        lockRaster();
        try {
            ArrayUtil.arrayToArray(values, getDataXY(c), getDataType().isSigned());
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataC(final int x, final int y, final Object values) {
        switch (getDataType().getJavaType()) {
            case BYTE:
                setDataCAsByte(x, y, (byte[]) values);
                break;

            case SHORT:
                setDataCAsShort(x, y, (short[]) values);
                break;

            case INT:
                setDataCAsInt(x, y, (int[]) values);
                break;

            case FLOAT:
                setDataCAsFloat(x, y, (float[]) values);
                break;

            case DOUBLE:
                setDataCAsDouble(x, y, (double[]) values);
                break;

            default:
                // nothing here
                break;
        }
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public byte[][] getDataXYCAsByte() {
        return ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public short[][] getDataXYCAsShort() {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort) db).getBankData();
        return ((DataBufferShort) db).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public int[][] getDataXYCAsInt() {
        return ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public float[][] getDataXYCAsFloat() {
        return ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 2D array data [C][XY]
     */
    public double[][] getDataXYCAsDouble() {
        return ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public byte[] getDataXYAsByte(final int c) {
        return ((DataBufferByte) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public short[] getDataXYAsShort(final int c) {
        final DataBuffer db = getRaster().getDataBuffer();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort) db).getData(c);
        return ((DataBufferShort) db).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public int[] getDataXYAsInt(final int c) {
        return ((DataBufferInt) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public float[] getDataXYAsFloat(final int c) {
        return ((DataBufferFloat) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a direct reference to internal 1D array data [XY] for specified c
     */
    public double[] getDataXYAsDouble(final int c) {
        return ((DataBufferDouble) getRaster().getDataBuffer()).getData(c);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public byte[] getDataCopyXYCAsByte() {
        return getDataCopyXYCAsByte(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public byte[] getDataCopyXYCAsByte(final byte[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[][] banks = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++) {
            final byte[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public short[] getDataCopyXYCAsShort() {
        return getDataCopyXYCAsShort(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public short[] getDataCopyXYCAsShort(final short[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] banks;
        if (db instanceof DataBufferUShort)
            banks = ((DataBufferUShort) db).getBankData();
        else
            banks = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++) {
            final short[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public int[] getDataCopyXYCAsInt() {
        return getDataCopyXYCAsInt(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public int[] getDataCopyXYCAsInt(final int[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[][] banks = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++) {
            final int[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public float[] getDataCopyXYCAsFloat() {
        return getDataCopyXYCAsFloat(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public float[] getDataCopyXYCAsFloat(final float[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[][] banks = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++) {
            final float[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY]
     */
    public double[] getDataCopyXYCAsDouble() {
        return getDataCopyXYCAsDouble(null, 0);
    }

    /**
     * Return a 1D array data copy [XYC] of internal 2D array data [C][XY] If (out != null) then
     * it's used to store result at the specified offset
     */
    public double[] getDataCopyXYCAsDouble(final double[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[][] banks = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));
        int offset = off;

        for (int c = 0; c < sizeC; c++) {
            final double[] src = banks[c];
            System.arraycopy(src, 0, result, offset, (int) len);
            offset += len;
        }

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public byte[] getDataCopyXYAsByte(final int c) {
        return getDataCopyXYAsByte(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyXYAsByte(final int c, final byte[] out, final int off) {
        final int len = getSizeX() * getSizeY();
        final byte[] src = ((DataBufferByte) getRaster().getDataBuffer()).getData(c);
        final byte[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public short[] getDataCopyXYAsShort(final int c) {
        return getDataCopyXYAsShort(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyXYAsShort(final int c, final short[] out, final int off) {
        final int len = getSizeX() * getSizeY();
        final DataBuffer db = getRaster().getDataBuffer();
        final short[] src;
        if (db instanceof DataBufferUShort)
            src = ((DataBufferUShort) db).getData(c);
        else
            src = ((DataBufferShort) db).getData(c);
        final short[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public int[] getDataCopyXYAsInt(final int c) {
        return getDataCopyXYAsInt(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyXYAsInt(final int c, final int[] out, final int off) {
        final int len = getSizeX() * getSizeY();
        final int[] src = ((DataBufferInt) getRaster().getDataBuffer()).getData(c);
        final int[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public float[] getDataCopyXYAsFloat(final int c) {
        return getDataCopyXYAsFloat(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyXYAsFloat(final int c, final float[] out, final int off) {
        final int len = getSizeX() * getSizeY();
        final float[] src = ((DataBufferFloat) getRaster().getDataBuffer()).getData(c);
        final float[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     */
    public double[] getDataCopyXYAsDouble(final int c) {
        return getDataCopyXYAsDouble(c, null, 0);
    }

    /**
     * Return a 1D array data copy [XY] of internal 1D array data [XY] for specified c<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyXYAsDouble(final int c, final double[] out, final int off) {
        final int len = getSizeX() * getSizeY();
        final double[] src = ((DataBufferDouble) getRaster().getDataBuffer()).getData(c);
        final double[] result = Array1DUtil.allocIfNull(out, len);

        System.arraycopy(src, 0, result, off, len);

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public byte[] getDataCopyCXYAsByte() {
        return getDataCopyCXYAsByte(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyCXYAsByte(final byte[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final byte[][] banks = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++) {
            final byte[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public short[] getDataCopyCXYAsShort() {
        return getDataCopyCXYAsShort(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyCXYAsShort(final short[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] banks;
        if (db instanceof DataBufferUShort)
            banks = ((DataBufferUShort) db).getBankData();
        else
            banks = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++) {
            final short[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public int[] getDataCopyCXYAsInt() {
        return getDataCopyCXYAsInt(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCXYAsInt(final int[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final int[][] banks = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++) {
            final int[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public float[] getDataCopyCXYAsFloat() {
        return getDataCopyCXYAsFloat(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyCXYAsFloat(final float[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final float[][] banks = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++) {
            final float[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     */
    public double[] getDataCopyCXYAsDouble() {
        return getDataCopyCXYAsDouble(null, 0);
    }

    /**
     * Return a 1D array data copy [CXY] of internal 2D array data [C][XY]<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyCXYAsDouble(final double[] out, final int off) {
        final long sizeC = getSizeC();
        final long len = (long) getSizeX() * (long) getSizeY();
        if ((len * sizeC) >= Integer.MAX_VALUE)
            throw new TooLargeArrayException();

        final double[][] banks = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, (int) (len * sizeC));

        for (int c = 0; c < sizeC; c++) {
            final double[] src = banks[c];
            int offset = c + off;
            for (int i = 0; i < len; i++, offset += sizeC)
                result[offset] = src[i];
        }

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public byte[] getDataCopyCAsByte(final int x, final int y) {
        return getDataCopyCAsByte(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public byte[] getDataCopyCAsByte(final int x, final int y, final byte[] out, final int off) {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final byte[][] data = ((DataBufferByte) getRaster().getDataBuffer()).getBankData();
        final byte[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public short[] getDataCopyCAsShort(final int x, final int y) {
        return getDataCopyCAsShort(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public short[] getDataCopyCAsShort(final int x, final int y, final short[] out, final int off) {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final DataBuffer db = getRaster().getDataBuffer();
        final short[][] data;
        if (db instanceof DataBufferUShort)
            data = ((DataBufferUShort) db).getBankData();
        else
            data = ((DataBufferShort) db).getBankData();
        final short[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public int[] getDataCopyCAsInt(final int x, final int y) {
        return getDataCopyCAsInt(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public int[] getDataCopyCAsInt(final int x, final int y, final int[] out, final int off) {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final int[][] data = ((DataBufferInt) getRaster().getDataBuffer()).getBankData();
        final int[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public float[] getDataCopyCAsFloat(final int x, final int y) {
        return getDataCopyCAsFloat(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public float[] getDataCopyCAsFloat(final int x, final int y, final float[] out, final int off) {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final float[][] data = ((DataBufferFloat) getRaster().getDataBuffer()).getBankData();
        final float[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position
     */
    public double[] getDataCopyCAsDouble(final int x, final int y) {
        return getDataCopyCAsDouble(x, y, null, 0);
    }

    /**
     * Return a 1D array data copy [C] of specified (x, y) position<br>
     * If (out != null) then it's used to store result at the specified offset
     */
    public double[] getDataCopyCAsDouble(final int x, final int y, final double[] out, final int off) {
        final int sizeC = getSizeC();
        final int offset = x + (y * getWidth());
        final double[][] data = ((DataBufferDouble) getRaster().getDataBuffer()).getBankData();
        final double[] result = Array1DUtil.allocIfNull(out, sizeC);

        for (int c = 0; c < sizeC; c++)
            // ignore band offset as it's always 0 here
            result[c + off] = data[c][offset];

        return result;
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsByte(final int c, final byte[] values) {
        lockRaster();
        try {
            System.arraycopy(values, 0, getDataXYAsByte(c), 0, getSizeX() * getSizeY());
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsShort(final int c, final short[] values) {
        lockRaster();
        try {
            System.arraycopy(values, 0, getDataXYAsShort(c), 0, getSizeX() * getSizeY());
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsInt(final int c, final int[] values) {
        lockRaster();
        try {
            System.arraycopy(values, 0, getDataXYAsInt(c), 0, getSizeX() * getSizeY());
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsFloat(final int c, final float[] values) {
        lockRaster();
        try {
            System.arraycopy(values, 0, getDataXYAsFloat(c), 0, getSizeX() * getSizeY());
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set internal 1D byte array data ([XY]) for specified component
     */
    public void setDataXYAsDouble(final int c, final double[] values) {
        lockRaster();
        try {
            System.arraycopy(values, 0, getDataXYAsDouble(c), 0, getSizeX() * getSizeY());
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsByte(final int x, final int y, final byte[] values) {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final byte[][] data = ((DataBufferByte) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsShort(final int x, final int y, final short[] values) {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final DataBuffer db = wr.getDataBuffer();
        final short[][] data;
        if (db instanceof DataBufferUShort)
            data = ((DataBufferUShort) db).getBankData();
        else
            data = ((DataBufferShort) db).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsInt(final int x, final int y, final int[] values) {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final int[][] data = ((DataBufferInt) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsFloat(final int x, final int y, final float[] values) {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final float[][] data = ((DataBufferFloat) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Set 1D array data [C] of specified (x, y) position
     */
    public void setDataCAsDouble(final int x, final int y, final double[] values) {
        final int offset = x + (y * getWidth());
        final int len = values.length;
        final WritableRaster wr = getRaster();
        final double[][] data = ((DataBufferDouble) wr.getDataBuffer()).getBankData();

        for (int comp = 0; comp < len; comp++)
            // ignore band offset as it's always 0 here
            data[comp][offset] = values[comp];

        // save changed data in cache (need to do cache behind here and still that is terribly slow !!)
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position as a double
     * whatever is the internal data type
     */
    public double getData(final int x, final int y, final int c) {
        return Array1DUtil.getValue(getDataXY(c), getOffset(x, y), getDataType());
    }

    /**
     * Set the value located at (x, y, c) position as a double
     * whatever is the internal data type
     */
    public void setData(final int x, final int y, final int c, final double value) {
        lockRaster();
        try {
            // set value
            Array1DUtil.setValue(getDataXY(c), getOffset(x, y), getDataType(), value);
        }
        finally {
            // FIXME : save changed data in cache (need to do cache behind here and still that is terribly slow !!)
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Returns the data value located at position (x, y, c) as double whatever is the internal data type.<br>
     * The value is interpolated depending the current double (x,y) coordinates.<br>
     * It returns 0d if value is out of range.
     */
    public double getDataInterpolated(final double x, final double y, final int c) {
        final int xi = (int) x;
        final int xip = xi + 1;
        final int yi = (int) y;
        final int yip = yi + 1;
        final int sx = getSizeX();
        final int sy = getSizeY();

        double result = 0d;

        // at least one pixel inside
        if ((xi < sx) && (yi < sy) && (xip >= 0) && (yip >= 0)) {
            final double ratioNextX = x - (double) xi;
            final double ratioCurX = 1d - ratioNextX;
            final double ratioNextY = y - (double) yi;
            final double ratioCurY = 1d - ratioNextY;

            if (yi >= 0) {
                if (xi >= 0)
                    result += getData(xi, yi, c) * (ratioCurX * ratioCurY);
                if (xip < sx)
                    result += getData(xip, yi, c) * (ratioNextX * ratioCurY);
            }
            if (yip < sy) {
                if (xi >= 0)
                    result += getData(xi, yip, c) * (ratioCurX * ratioNextY);
                if (xip < sx)
                    result += getData(xip, yip, c) * (ratioNextX * ratioNextY);
            }
        }

        return result;
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public byte getDataAsByte(final int x, final int y, final int c) {
        // ignore band offset as it's always 0 here
        return (((DataBufferByte) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsByte(final int x, final int y, final int c, final byte value) {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferByte) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public short getDataAsShort(final int x, final int y, final int c) {
        // ignore band offset as it's always 0 here
        final DataBuffer db = getRaster().getDataBuffer();

        if (db instanceof DataBufferUShort)
            return (((DataBufferUShort) db).getData(c))[x + (y * getWidth())];

        return (((DataBufferShort) db).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsShort(final int x, final int y, final int c, final short value) {
        final WritableRaster wr = getRaster();
        final DataBuffer db = wr.getDataBuffer();
        if (db instanceof DataBufferUShort)
            // ignore band offset as it's always 0 here
            (((DataBufferUShort) db).getData(c))[x + (y * getWidth())] = value;
        else
            (((DataBufferShort) db).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public int getDataAsInt(final int x, final int y, final int c) {
        // ignore band offset as it's always 0 here
        return (((DataBufferInt) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsInt(final int x, final int y, final int c, final int value) {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferInt) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public float getDataAsFloat(final int x, final int y, final int c) {
        // ignore band offset as it's always 0 here
        return (((DataBufferFloat) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsFloat(final int x, final int y, final int c, final float value) {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferFloat) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Return the value located at (x, y, c) position
     */
    public double getDataAsDouble(final int x, final int y, final int c) {
        // ignore band offset as it's always 0 here
        return (((DataBufferDouble) getRaster().getDataBuffer()).getData(c))[x + (y * getWidth())];
    }

    /**
     * Set the value located at (x, y, c) position
     */
    public void setDataAsDouble(final int x, final int y, final int c, final double value) {
        final WritableRaster wr = getRaster();
        // ignore band offset as it's always 0 here
        (((DataBufferDouble) wr.getDataBuffer()).getData(c))[x + (y * getWidth())] = value;
        // save changed data in cache
        saveRasterInCache(wr);
        // notify data changed
        dataChanged();
    }

    /**
     * Same as getRGB but by using the specified LUT instead of internal one
     *
     * @see BufferedImage#getRGB(int, int)
     */
    public int getRGB(final int x, final int y, final LUT lut) {
        return getIcyColorModel().getRGB(getRaster().getDataElements(x, y, null), lut);
    }

    /**
     * Internal copy data from an icy image (notify data changed)
     *
     * @param srcImage   source icy image
     * @param srcRect    source region
     * @param dstPt      destination X,Y position
     * @param srcChannel source channel
     * @param dstChannel destination channel
     */
    protected void fastCopyData(final IcyBufferedImage srcImage, final Rectangle srcRect, final Point dstPt, final int srcChannel, final int dstChannel) {
        final int srcSizeX = srcImage.getSizeX();
        final int dstSizeX = getSizeX();

        // limit to source image size
        final Rectangle adjSrcRect = srcRect.intersection(new Rectangle(srcSizeX, srcImage.getSizeY()));
        // negative destination x position
        if (dstPt.x < 0)
            // adjust source rect
            adjSrcRect.x -= dstPt.x;
        // negative destination y position
        if (dstPt.y < 0)
            // adjust source rect
            adjSrcRect.y -= dstPt.y;

        final Rectangle dstRect = new Rectangle(dstPt.x, dstPt.y, adjSrcRect.width, adjSrcRect.height);
        // limit to destination image size
        final Rectangle adjDstRect = dstRect.intersection(new Rectangle(dstSizeX, getSizeY()));

        final int w = Math.min(adjSrcRect.width, adjDstRect.width);
        final int h = Math.min(adjSrcRect.height, adjDstRect.height);

        // nothing to copy
        if ((w == 0) || (h == 0))
            return;

        lockRaster();
        try {
            final boolean signed = srcImage.getDataType().isSigned();
            final Object src = srcImage.getDataXY(srcChannel);
            final Object dst = getDataXY(dstChannel);

            int srcOffset = adjSrcRect.x + (adjSrcRect.y * srcSizeX);
            int dstOffset = adjDstRect.x + (adjDstRect.y * dstSizeX);

            for (int y = 0; y < h; y++) {
                ArrayUtil.arrayToArray(src, srcOffset, dst, dstOffset, w, signed);
                srcOffset += srcSizeX;
                dstOffset += dstSizeX;
            }
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Internal copy data from a compatible image (notify data changed)
     *
     * @param srcChannel         source image
     * @param dstChannel         int
     * @param src_db             buffer
     * @param dst_db             buffer
     * @param indices            array
     * @param band_offsets       array
     * @param pixelStride_src    int
     * @param scanlineStride_src int
     * @param bank_offsets       array
     * @param decOffsetSrc       int
     * @param maxX               int
     * @param maxY               int
     */
    protected void internalCopyData(
            final int srcChannel, final int dstChannel, final DataBuffer src_db, final DataBuffer dst_db,
            final int[] indices, final int[] band_offsets, final int[] bank_offsets, final int scanlineStride_src,
            final int pixelStride_src, final int maxX, final int maxY, final int decOffsetSrc
    ) {
        final int scanlineStride_dst = getSizeX();

        final int bank = indices[srcChannel];
        final int offset = band_offsets[srcChannel] + bank_offsets[bank] - decOffsetSrc;

        switch (getDataType().getJavaType()) {
            case BYTE: {
                final byte[] src;
                final byte[] dst = ((DataBufferByte) dst_db).getData(dstChannel);

                // LOCI use its own buffer classes
                if (src_db instanceof SignedByteBuffer)
                    src = ((SignedByteBuffer) src_db).getData(bank);
                else
                    src = ((DataBufferByte) src_db).getData(bank);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++) {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++) {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case SHORT: {
                final short[] src;
                final short[] dst;

                // LOCI use its own buffer classes
                if (src_db instanceof SignedShortBuffer)
                    src = ((SignedShortBuffer) src_db).getData(bank);
                else if (src_db instanceof DataBufferShort)
                    src = ((DataBufferShort) src_db).getData(bank);
                else
                    src = ((DataBufferUShort) src_db).getData(bank);

                if (dst_db instanceof DataBufferShort)
                    dst = ((DataBufferShort) dst_db).getData(dstChannel);
                else
                    dst = ((DataBufferUShort) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++) {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++) {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case INT: {
                final int[] src;
                final int[] dst = ((DataBufferInt) dst_db).getData(dstChannel);

                // LOCI use its own buffer classes
                if (src_db instanceof UnsignedIntBuffer)
                    src = ((UnsignedIntBuffer) src_db).getData(bank);
                else
                    src = ((DataBufferInt) src_db).getData(bank);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++) {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++) {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case FLOAT: {
                final float[] src = ((DataBufferFloat) src_db).getData(bank);
                final float[] dst = ((DataBufferFloat) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++) {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++) {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            case DOUBLE: {
                final double[] src = ((DataBufferDouble) src_db).getData(bank);
                final double[] dst = ((DataBufferDouble) dst_db).getData(dstChannel);

                int offset_src = offset;
                int offset_dst = 0;
                for (int y = 0; y < maxY; y++) {
                    int offset_src_pix = offset_src;
                    int offset_dst_pix = offset_dst;

                    for (int x = 0; x < maxX; x++) {
                        dst[offset_dst_pix] = src[offset_src_pix];
                        offset_src_pix += pixelStride_src;
                        offset_dst_pix++;
                    }

                    offset_src += scanlineStride_src;
                    offset_dst += scanlineStride_dst;
                }
                break;
            }

            default:
                // do nothing here
                break;
        }
    }

    /**
     * Copy channel data from a compatible sample model and writable raster (notify data changed).
     *
     * @param sampleModel  source sample model
     * @param srcChannel   source channel (-1 for all channels)
     * @param dstChannel   destination channel (only significant if source channel != -1)
     * @param sourceRaster WritableRaster
     * @return <code>true</code> if the copy operation succeed, <code>false</code> otherwise
     */
    public boolean copyData(final ComponentSampleModel sampleModel, final WritableRaster sourceRaster, final int srcChannel, final int dstChannel) {
        // not compatible sample model
        if (DataType.getDataTypeFromDataBufferType(sampleModel.getDataType()) != getDataType())
            return false;

        final DataBuffer src_db = sourceRaster.getDataBuffer();
        final WritableRaster dst_raster = getRaster();
        final DataBuffer dst_db = dst_raster.getDataBuffer();
        final int[] indices = sampleModel.getBankIndices();
        final int[] band_offsets = sampleModel.getBandOffsets();
        final int[] bank_offsets = src_db.getOffsets();
        final int scanlineStride_src = sampleModel.getScanlineStride();
        final int pixelStride_src = sampleModel.getPixelStride();
        final int maxX = Math.min(getSizeX(), sampleModel.getWidth());
        final int maxY = Math.min(getSizeY(), sampleModel.getHeight());
        final int decOffsetSrc = sourceRaster.getSampleModelTranslateX()
                + (sourceRaster.getSampleModelTranslateY() * scanlineStride_src);

        // all channels
        if (srcChannel == -1) {
            final int numBands = sampleModel.getNumBands();

            for (int band = 0; band < numBands; band++)
                internalCopyData(band, band, src_db, dst_db, indices, band_offsets, bank_offsets, scanlineStride_src,
                        pixelStride_src, maxX, maxY, decOffsetSrc);
        }
        else {
            internalCopyData(srcChannel, dstChannel, src_db, dst_db, indices, band_offsets, bank_offsets,
                    scanlineStride_src, pixelStride_src, maxX, maxY, decOffsetSrc);
        }

        // save in cache changed data
        saveRasterInCache(dst_raster);
        // notify data changed
        dataChanged();

        return true;
    }

    /**
     * Copy data to specified location from an data array.
     *
     * @param data       source data array (should be same type than image data type)
     * @param dataDim    source data dimension (array length should be &gt;= Dimension.width * Dimension.heigth)
     * @param signed     if the source data array should be considered as signed data (meaningful for integer
     *                   data type only)
     * @param dstPt      destination X,Y position (assume [0,0] if null)
     * @param dstChannel destination channel
     */
    public void copyData(final Object data, final Dimension dataDim, final boolean signed, final Point dstPt, final int dstChannel) {
        if ((data == null) || (dataDim == null))
            return;

        // source image size
        final Rectangle adjSrcRect = new Rectangle(dataDim);
        // negative destination x position
        if (dstPt.x < 0)
            // adjust source rect
            adjSrcRect.x -= dstPt.x;
        // negative destination y position
        if (dstPt.y < 0)
            // adjust source rect
            adjSrcRect.y -= dstPt.y;

        final Rectangle dstRect = new Rectangle(dstPt.x, dstPt.y, adjSrcRect.width, adjSrcRect.height);
        // limit to destination image size
        final Rectangle adjDstRect = dstRect.intersection(new Rectangle(getSizeX(), getSizeY()));

        final int w = Math.min(adjSrcRect.width, adjDstRect.width);
        final int h = Math.min(adjSrcRect.height, adjDstRect.height);

        // nothing to copy
        if ((w == 0) || (h == 0))
            return;

        lockRaster();
        try {
            final Object dst = getDataXY(dstChannel);
            final int srcSizeX = dataDim.width;
            final int dstSizeX = getSizeX();

            int srcOffset = adjSrcRect.x + (adjSrcRect.y * srcSizeX);
            int dstOffset = adjDstRect.x + (adjDstRect.y * dstSizeX);

            for (int y = 0; y < h; y++) {
                // do data copy (and conversion if needed)
                ArrayUtil.arrayToArray(data, srcOffset, dst, dstOffset, w, signed);
                srcOffset += srcSizeX;
                dstOffset += dstSizeX;
            }
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Copy data from an image (notify data changed)
     *
     * @param srcImage   source image
     * @param srcRect    source region to copy (assume whole image if null)
     * @param dstPt      destination X,Y position (assume [0,0] if null)
     * @param srcChannel source channel (-1 for all channels)
     * @param dstChannel destination channel (only significant if source channel != -1)
     */
    public void copyData(final IcyBufferedImage srcImage, final Rectangle srcRect, final Point dstPt, final int srcChannel, final int dstChannel) {
        if (srcImage == null)
            return;

        final Rectangle adjSrcRect;
        final Point adjDstPt;

        adjSrcRect = Objects.requireNonNullElseGet(srcRect, () -> new Rectangle(srcImage.getSizeX(), srcImage.getSizeY()));

        adjDstPt = Objects.requireNonNullElseGet(dstPt, () -> new Point(0, 0));

        // copy all possible components
        if (srcChannel == -1) {
            final int sizeC = Math.min(srcImage.getSizeC(), getSizeC());

            beginUpdate();
            try {
                for (int c = 0; c < sizeC; c++)
                    fastCopyData(srcImage, adjSrcRect, adjDstPt, c, c);
            }
            finally {
                endUpdate();
            }
        }
        else
            fastCopyData(srcImage, adjSrcRect, adjDstPt, srcChannel, dstChannel);
    }

    /**
     * Copy data from an image (notify data changed)
     *
     * @param srcImage source image
     * @param srcRect  source region to copy (assume whole image if null)
     * @param dstPt    destination (assume [0,0] if null)
     */
    public void copyData(final IcyBufferedImage srcImage, final Rectangle srcRect, final Point dstPt) {
        if (srcImage == null)
            return;

        copyData(srcImage, srcRect, dstPt, -1, 0);
    }

    /**
     * Copy data from an image (notify data changed)
     *
     * @param srcImage   source image
     * @param srcChannel source channel to copy (-1 for all channels)
     * @param dstChannel destination channel to receive data (only significant if source channel != -1)
     */
    public void copyData(final BufferedImage srcImage, final int srcChannel, final int dstChannel) {
        if (srcImage == null)
            return;

        if (srcImage instanceof IcyBufferedImage)
            copyData(((IcyBufferedImage) srcImage), null, null, srcChannel, dstChannel);
        else {
            final boolean done;

            // try to use faster copy for compatible image
            if (srcImage.getSampleModel() instanceof ComponentSampleModel)
                done = copyData((ComponentSampleModel) srcImage.getSampleModel(), srcImage.getRaster(), srcChannel,
                        dstChannel);
            else
                done = false;

            if (!done) {
                final WritableRaster wr = getRaster();
                // image not compatible, use generic (and slow) data copy
                srcImage.copyData(wr);
                // save changed data in cache
                saveRasterInCache(wr);
                // notify data changed
                dataChanged();
            }
        }
    }

    /**
     * Copy data from an image (notify data changed)
     *
     * @param srcImage source image
     */
    public void copyData(final BufferedImage srcImage) {
        copyData(srcImage, -1, -1);
    }

    /**
     * Return raw data component as an array of byte
     *
     * @param c      component index
     * @param out    output array (can be null)
     * @param offset output offset
     * @param little little endian order
     */
    public byte[] getRawData(final int c, final byte[] out, final int offset, final boolean little) {
        // alloc output array if needed
        final byte[] result = Array1DUtil.allocIfNull(out,
                offset + (getSizeX() * getSizeY() * getDataType().getSize()));

        return ByteArrayConvert.toByteArray(getDataXY(c), 0, result, offset, little);
    }

    /**
     * Return raw data component as an array of byte
     *
     * @param c      component index
     * @param little little endian order
     */
    public byte[] getRawData(final int c, final boolean little) {
        return getRawData(c, null, 0, little);
    }

    /**
     * Return raw data for all components as an array of byte
     *
     * @param out    output array (can be null)
     * @param offset output offset
     * @param little little endian order
     */
    public byte[] getRawData(final byte[] out, final int offset, final boolean little) {
        final int sizeXY = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int sizeType = getDataType().getSize();

        // alloc output array if needed
        final byte[] result = Array1DUtil.allocIfNull(out, offset + (sizeC * sizeXY * sizeType));

        int outOff = offset;
        for (int c = 0; c < sizeC; c++) {
            getRawData(c, result, outOff, little);
            outOff += sizeXY * sizeType;
        }

        return result;
    }

    /**
     * Return raw data for all components as an array of byte
     *
     * @param little little endian order
     */
    public byte[] getRawData(final boolean little) {
        return getRawData(null, 0, little);
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     *
     * @param c      component index
     * @param data   data as byte array
     * @param offset input offset
     * @param little little endian order
     */
    public void setRawData(final int c, final byte[] data, final int offset, final boolean little) {
        if (data == null)
            return;

        lockRaster();
        try {
            ByteArrayConvert.byteArrayTo(data, offset, getDataXY(c), 0, -1, little);
        }
        finally {
            releaseRaster(true);
        }

        // notify data changed
        dataChanged();
    }

    /**
     * Set raw data component from an array of byte (notify data changed)
     *
     * @param c      component index
     * @param data   data as byte array
     * @param little little endian order
     */
    public void setRawData(final int c, final byte[] data, final boolean little) {
        setRawData(c, data, 0, little);
    }

    /**
     * Set raw data for all components from an array of byte (notify data changed).<br>
     * Data are arranged in the following dimension order: XYC
     *
     * @param data   data as byte array
     * @param offset input offset
     * @param little little endian order
     */
    public void setRawData(final byte[] data, final int offset, final boolean little) {
        if (data == null)
            return;

        final int sizeXY = getSizeX() * getSizeY();
        final int sizeC = getSizeC();
        final int sizeType = getDataType().getSize();

        beginUpdate();
        try {
            int inOff = offset;
            for (int c = 0; c < sizeC; c++) {
                setRawData(c, data, inOff, little);
                inOff += sizeXY * sizeType;
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Set raw data for all components from an array of byte (notify data changed)
     *
     * @param data   data as byte array
     * @param little little endian order
     */
    public void setRawData(final byte[] data, final boolean little) {
        setRawData(data, 0, little);
    }

    /**
     * Return the colormap of the specified channel.
     */
    public IcyColorMap getColorMap(final int channel) {
        return getIcyColorModel().getColorMap(channel);
    }

    /**
     * Set colormaps from specified image.
     */
    public void setColorMaps(final BufferedImage srcImage) {
        getIcyColorModel().setColorMaps(srcImage.getColorModel());
    }

    /**
     * Set the colormap for the specified channel.
     *
     * @param channel  channel we want to set the colormap
     * @param map      source colorspace to copy
     * @param setAlpha also set the alpha information
     */
    public void setColorMap(final int channel, final IcyColorMap map, final boolean setAlpha) {
        getIcyColorModel().setColorMap(channel, map, setAlpha);
    }

    /**
     * Set the colormap for the specified channel.
     *
     * @param channel channel we want to set the colormap
     * @param map     source colorspace to copy
     */
    public void setColorMap(final int channel, final IcyColorMap map) {
        getIcyColorModel().setColorMap(channel, map, map.isAlpha());
    }

    /**
     * notify image data has changed
     */
    public void dataChanged() {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.DATA_CHANGED));
    }

    /**
     * notify image colorMap has changed
     */
    protected void colormapChanged(final int component) {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.COLORMAP_CHANGED, component));
    }

    /**
     * notify image channels bounds has changed
     */
    public void channelBoundsChanged(final int channel) {
        updater.changed(new IcyBufferedImageEvent(this, IcyBufferedImageEventType.BOUNDS_CHANGED, channel));
    }

    /**
     * fire change event
     */
    protected void fireChangeEvent(final IcyBufferedImageEvent e) {
        for (final IcyBufferedImageListener listener : new ArrayList<>(listeners))
            listener.imageChanged(e);
    }

    public void addListener(final IcyBufferedImageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final IcyBufferedImageListener listener) {
        listeners.remove(listener);
    }

    public void beginUpdate() {
        updater.beginUpdate();
        lockRaster();
    }

    public void endUpdate() {
        updater.endUpdate();
    }

    public boolean isUpdating() {
        releaseRaster(true);
        return updater.isUpdating();
    }

    @Override
    public void onChanged(final CollapsibleEvent object) {
        final IcyBufferedImageEvent event = (IcyBufferedImageEvent) object;

        switch (event.getType()) {
            // do here global process on image data change
            case DATA_CHANGED:
                // update image components bounds
                if (autoUpdateChannelBounds)
                    updateChannelsBounds();
                break;

            // do here global process on image bounds change
            case BOUNDS_CHANGED:
                break;

            // do here global process on image colormap change
            case COLORMAP_CHANGED:
                break;
        }

        // notify listener we have changed
        fireChangeEvent(event);
    }

    @Override
    public void colorModelChanged(final IcyColorModelEvent e) {
        switch (e.getType()) {
            case COLORMAP_CHANGED:
                colormapChanged(e.getComponent());
                break;

            case SCALER_CHANGED:
                channelBoundsChanged(e.getComponent());
                break;
        }
    }

    @Override
    public String toString() {
        return "IcyBufferedImage: " + getSizeX() + " x " + getSizeY() + " - " + getSizeC() + " ch (" + getDataType() + ")";
    }
}
