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

package icy.file;

import icy.common.exception.UnsupportedFormatException;
import icy.file.SequenceFileSticher.SequenceFileGroup;
import icy.file.SequenceFileSticher.SequenceIdent;
import icy.file.SequenceFileSticher.SequencePosition;
import icy.file.SequenceFileSticher.SequenceType;
import icy.gui.dialog.LoaderDialog;
import icy.image.AbstractImageProvider;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.image.colormap.IcyColorMap;
import icy.sequence.MetaDataUtil;
import icy.system.IcyExceptionHandler;
import icy.type.collection.CollectionUtil;
import icy.type.collection.array.Array1DUtil;
import icy.util.OMEUtil;
import icy.util.StringUtil;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.Channel;
import ome.xml.model.Pixels;
import ome.xml.model.Plane;
import org.jetbrains.annotations.NotNull;
import plugins.kernel.importer.LociImporterPlugin;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.List;
import java.util.*;

/**
 * Special importer able to group a list of path ({@link SequenceFileGroup}) to build a single Sequence out of it.<br>
 * Note that this importer is limited to single series group, we don't allow group mixing several series.
 *
 * @author Stephane
 * @author Thomas Musset
 */
public class SequenceFileGroupImporter extends AbstractImageProvider implements SequenceFileImporter {
    static final int MAX_IMPORTER = 16;

    protected static class FileCursor {
        public final SequencePosition position;
        public final int index;
        public final int internalZ;
        public final int internalT;
        public final int internalC;

        public FileCursor(final SequencePosition position, final int index, final int z, final int t, final int c) {
            super();

            this.position = position;
            this.index = index;
            this.internalZ = z;
            this.internalT = t;
            this.internalC = c;
        }
    }

    protected record TileIndex(Rectangle region, int index) {
    }

    protected SequenceFileGroup currentGroup;
    protected OMEXMLMetadata currentMetadata;
    // position index array (stored in XYZTC order) to quickly find an image given its (XY)ZTC position
    protected SequencePosition[] positions;

    protected boolean ordering;
    protected int openFlags;

    // internals
    protected int indYMul;
    protected int indZMul;
    protected int indTMul;
    protected int indCMul;
    protected int indSMul;

    /**
     * Shared importer for multi threading
     */
    protected final Map<String, SequenceFileImporter> importersPool;

    public SequenceFileGroupImporter() {
        super();

        currentGroup = null;
        currentMetadata = null;
        // default
        ordering = true;
        importersPool = new HashMap<>();
    }

    public void setOrdering(final boolean ordering) {
        this.ordering = ordering;
    }

    public boolean getOrdering() {
        return ordering;
    }

    @Override
    public boolean acceptFile(final String path) {
        boolean result = false;

        if ((currentGroup != null) && (currentGroup.ident.importer != null))
            result = currentGroup.ident.importer.acceptFile(path);

        // use Loader to test it
        if (!result)
            result = Loader.isSupportedImageFile(path);

        return result;
    }

    @Override
    public @NotNull List<FileFilter> getFileFilters() {
        // return a generic image file filter here
        final List<FileFilter> result = new ArrayList<>();
        result.add(LoaderDialog.allImagesFileFilter);
        return result;
    }

    /**
     * @return <code>true</code> if a opened is currently opened
     */
    public boolean isOpen() {
        return getOpenedGroup() != null;
    }

    /**
     * Return the current / last loaded image without the image group
     */
    @Override
    public String getOpened() {
        if (currentGroup != null) {
            // single image in the group ? --> directly return its path
            if (currentGroup.positions.size() == 1)
                return currentGroup.positions.get(0).getPath();

            // return the base path of the group
            return currentGroup.getBasePath();
        }

        return null;
    }

    /**
     * @return current opened group
     */
    public SequenceFileGroup getOpenedGroup() {
        return currentGroup;
    }

    /**
     * @deprecated Better to use {@link #open(Collection, int)} or {@link #open(SequenceFileGroup, int)} for this importer
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    @Override
    public boolean open(@NotNull final String path, final int flags) throws UnsupportedFormatException, IOException, InterruptedException {
        open(CollectionUtil.createArrayList(path), flags);

        return true;
    }

    /**
     * Open a list of ids.<br>
     * Prefer to use {@link SequenceFileSticher#groupFiles(SequenceFileImporter, Collection, boolean, icy.gui.frame.progress.FileFrame)
     * then use #open(SequenceFileGroup, int) instead.
     */
    public void open(final Collection<String> ids, final int flags) throws InterruptedException, ClosedByInterruptException {
        open(SequenceFileSticher.groupFiles(null, ids, ordering, null), flags);
    }

    /**
     * Open a SequenceFileGroup
     */
    public void open(final SequenceFileGroup group, final int flags) {
        try {
            close();
        }
        catch (final Exception e) {
            // should not prevent from opening
            IcyExceptionHandler.showErrorMessage(e, true, true);
        }

        // can't open null group
        if (group == null)
            return;

        currentGroup = group;
        // we need to rebuild metadata
        currentMetadata = null;

        // store flags for later
        openFlags = flags;

        // don't try to parse metadata on image file type which don't contains anything useful
        if (!Loader.hasMetadata(group.basePath))
            openFlags |= FLAG_METADATA_MINIMUM;

        buildIndexes();
    }

    /**
     * Open the specified importer to the given path (internal use only).
     *
     * @see #releaseImporter(String, SequenceFileImporter)
     */
    protected boolean openImporter(final SequenceFileImporter result, final String path) throws Exception {
        // importer is already opened ? --> close it first
        if (!StringUtil.isEmpty(result.getOpened()))
            result.close();

        // disable grouping for LOCI importer as we do it here
        if (result instanceof LociImporterPlugin)
            ((LociImporterPlugin) result).setGroupFiles(false);

        return result.open(path, openFlags);
    }

    /**
     * Create and open a new importer for the given path (internal use only)
     */
    protected SequenceFileImporter createImporter(final String path) throws Exception {
        if (!isOpen())
            return null;

        // we should always use the same importer for a file group
        final SequenceFileImporter result = currentGroup.ident.importer.getClass().getDeclaredConstructor().newInstance();

        // open the importer for given path
        openImporter(result, path);

        // // try to use the default group importer
        // SequenceFileImporter result = currentGroup.ident.importer;
        //
        // // can use default one ? --> clone it
        // if ((result != null) && result.acceptFile(path))
        // result = result.getClass().newInstance();
        // else
        // {
        // // get importer for this path
        // result = Loader.getSequenceFileImporter(path, true);
        // }
        //
        // // try to open it
        // if (result != null)
        // openImporter(result, path);

        return result;
    }

    /**
     * Return the path of the first available importer (internal use only)
     */
    protected String getFirstImporterPath() {
        synchronized (importersPool) {
            if (!importersPool.isEmpty())
                return importersPool.keySet().iterator().next();
        }

        for (final SequencePosition pos : currentGroup.positions) {
            if (pos != null)
                return pos.getPath();
        }

        return null;
    }

    /**
     * Return an opened importer for given path.<br>
     * Note that you should call {@link #releaseImporter(String, SequenceFileImporter)} when you're done with it.
     *
     * @see #releaseImporter(String, SequenceFileImporter)
     */
    public SequenceFileImporter getImporter(final String path) throws Exception {
        if (StringUtil.isEmpty(path))
            return null;

        try {
            final int numImporter;
            SequenceFileImporter result;

            synchronized (importersPool) {
                numImporter = importersPool.size();
                result = importersPool.remove(path);
            }

            // no available importer for this path ?
            if (result == null) {
                // we have already enough importers (we don't want to create too much of them)
                if (numImporter >= MAX_IMPORTER) {
                    // recycle first importer found one
                    result = getImporter(getFirstImporterPath());

                    // correctly
                    if (result != null)
                        openImporter(result, path);
                }

                // need to create a new importer
                if (result == null)
                    result = createImporter(path);
            }

            return result;
        }
        catch (final InstantiationException | IllegalAccessException e) {
            // better to re-throw as RuntimeException
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Release the importer obtained through {@link #getImporter(String)} to the importer pool.
     *
     * @see #getImporter(String)
     */
    public void releaseImporter(final String path, final SequenceFileImporter importer) {
        synchronized (importersPool) {
            // it's better to specify path instead of using SequenceFileImporter.getOpened() as internally format can change
            importersPool.put(path, importer);
        }
    }

    protected void buildIndexes() {
        final SequenceFileGroup group = currentGroup;
        final SequenceType baseType = group.ident.baseType;

        // compute index multipliers
        indYMul = group.totalSizeX / baseType.sizeX;
        indZMul = indYMul * (group.totalSizeY / baseType.sizeY);
        indTMul = indZMul * (group.totalSizeZ / baseType.sizeZ);
        indCMul = indTMul * (group.totalSizeT / baseType.sizeT);

        final int totalLen = indCMul * (group.totalSizeC / baseType.sizeC);

        // allocate index array
        positions = new SequencePosition[totalLen];

        // find maxX and maxY
        for (final SequencePosition pos : currentGroup.positions) {
            // compute index
            final int ind = getIdIndex(pos.getIndexX(), pos.getIndexY(), pos.getIndexZ(), pos.getIndexT(), pos.getIndexC());
            // set path for this index
            positions[ind] = pos;
        }
    }

    /**
     * @return <code>true</code> if this image group requires several images to build a full XY plan image.
     */
    public boolean isStitchedImage() {
        if (!isOpen())
            return false;

        // if total size XY != single image size XY --> we have a stitched image
        return (currentGroup.totalSizeX != currentGroup.ident.baseType.sizeX) || (currentGroup.totalSizeY != currentGroup.ident.baseType.sizeY);
    }

    /**
     * @return cursor for the given [z,t,c] position
     */
    protected FileCursor getCursor(final int z, final int t, final int c) {
        final SequenceType baseType = currentGroup.ident.baseType;

        final int externalZ = z / baseType.sizeZ;
        final int externalT = t / baseType.sizeT;
        final int externalC = c / baseType.sizeC;

        final int internalZ = z % baseType.sizeZ;
        final int internalT = t % baseType.sizeT;
        final int internalC = c % baseType.sizeC;

        final int idInd = getIdIndex(0, 0, externalZ, externalT, externalC);

        return new FileCursor((idInd < positions.length) ? positions[idInd] : null, idInd, internalZ, internalT, internalC);
    }

    /**
     * @return all required TileIndex for the given XY region
     */
    protected List<TileIndex> getTileIndexes(final Rectangle xyRegion) {
        if (!isStitchedImage())
            return CollectionUtil.createArrayList(new TileIndex(xyRegion, 0));

        final List<TileIndex> result = new ArrayList<>();

        final SequenceType baseType = currentGroup.ident.baseType;
        final int tsx = baseType.sizeX;
        final int tsy = baseType.sizeY;

        // get tile list
        final List<Rectangle> tiles = ImageUtil.getTileList(xyRegion, tsx, tsy);

        // each tile represent a single image
        for (final Rectangle tile : tiles) {
            // find the image index
            final int x = tile.x / tsx;
            final int y = tile.y / tsy;

            // add tile info
            result.add(new TileIndex(tile.intersection(xyRegion), x + (y * indYMul)));
        }

        return result;
    }

    /**
     * @return file path for the given [z,t,c] position
     */
    public String getPath(final int z, final int t, final int c) {
        if (!isOpen())
            return "";

        final SequencePosition pos = getCursor(z, t, c).position;

        if (pos != null)
            return pos.getPath();

        return "";
    }

    protected int getIdIndex(final int x, final int y, final int z, final int t, final int c) {
        return x + (y * indYMul) + (z * indZMul) + (t * indTMul) + (c * indCMul);
    }

    /**
     * Close all opened internals importer without closing the Group Importer itself (importer remaing active)
     */
    public void closeInternalsImporters() throws Exception {
        synchronized (importersPool) {
            // close all importers
            for (final SequenceFileImporter imp : importersPool.values())
                imp.close();

            importersPool.clear();
        }
    }

    @Override
    public void close() throws Exception {
        closeInternalsImporters();

        // release position indexes array
        positions = null;
        // release everything
        currentMetadata = null;
        currentGroup = null;
    }

    @Override
    public OMEXMLMetadata getOMEXMLMetaData() throws Exception {
        if (!isOpen())
            return null;

        // need to build metadata ?
        if (currentMetadata == null)
            currentMetadata = buildMetaData();

        return currentMetadata;
    }

    protected OMEXMLMetadata buildMetaData() throws Exception {
        final SequenceFileGroup group = currentGroup;
        final SequenceIdent ident = group.ident;
        final SequenceType baseType = ident.baseType;
        // single image in the group ? --> directly use its path as name
        final String name = FileUtil.getFileName((group.positions.size() == 1) ? group.positions.get(0).getPath() : group.getBasePath(), false);

        OMEXMLMetadata result = null;

        // try to recover general metadata from first image
        if ((openFlags & SequenceFileImporter.FLAG_METADATA_MASK) != SequenceFileImporter.FLAG_METADATA_MINIMUM) {
            final SequencePosition position = getCursor(0, 0, 0).position;

            // do we have an image for this position ?
            if (position != null) {
                final SequenceFileImporter imp = getImporter(position.getPath());

                if (imp != null) {
                    // create from original metadata
                    result = OMEUtil.createOMEXMLMetadata(imp.getOMEXMLMetaData(), 0);
                    // set name
                    MetaDataUtil.setName(result, 0, name);
                }
            }
        }

        // minimum metadata or couldn't be retrieved ? --> create it from scratch
        if (result == null)
            result = MetaDataUtil.createMetadata(name);

        // minimum metadata
        MetaDataUtil.setMetaData(result, group.totalSizeX, group.totalSizeY, group.totalSizeC, group.totalSizeZ, group.totalSizeT, baseType.dataType, true);
        // pixel size & time interval
        if (baseType.pixelSizeX > 0d)
            MetaDataUtil.setPixelSizeX(result, 0, baseType.pixelSizeX);
        if (baseType.pixelSizeY > 0d)
            MetaDataUtil.setPixelSizeY(result, 0, baseType.pixelSizeY);
        if (baseType.pixelSizeZ > 0d)
            MetaDataUtil.setPixelSizeZ(result, 0, baseType.pixelSizeZ);
        if (baseType.timeInterval > 0d)
            MetaDataUtil.setTimeInterval(result, 0, baseType.timeInterval);

        // get OME Pixels object (easier to deal with)
        final Pixels resultPixels = MetaDataUtil.getPixels(result, 0);
        // allocate Plane objects
        MetaDataUtil.ensurePlane(resultPixels, group.totalSizeT - 1, group.totalSizeZ - 1, group.totalSizeC - 1);

        // iterate over all dimension
        for (int c = 0; c < group.totalSizeC; c++) {
            for (int z = 0; z < group.totalSizeZ; z++) {
                for (int t = 0; t < group.totalSizeT; t++) {
                    final FileCursor cursor = getCursor(z, t, c);
                    final SequencePosition position = cursor.position;

                    // do we have an image for this position ?
                    if (position != null) {
                        // get original plane metadata
                        Plane metaPlane = null;

                        // default metadata
                        if ((openFlags & SequenceFileImporter.FLAG_METADATA_MASK) != SequenceFileImporter.FLAG_METADATA_MINIMUM) {
                            final SequenceFileImporter imp = getImporter(position.getPath());

                            // importer ok ?
                            if (imp != null) {
                                try {
                                    // get metadata for this file
                                    final OMEXMLMetadata meta = imp.getOMEXMLMetaData();
                                    // get OME Pixels object (easier to deal with)
                                    final Pixels metaPixels = MetaDataUtil.getPixels(meta, 0);

                                    // first ZT plane ? --> fill channel data
                                    if ((z == 0) && (t == 0)) {
                                        final Channel metaChannel;

                                        // get origin channel (or create it if needed)
                                        if (cursor.internalC < metaPixels.sizeOfChannelList())
                                            metaChannel = metaPixels.getChannel(cursor.internalC);
                                        else
                                            metaChannel = new Channel();

                                        // get Channel object directly from source metadata
                                        resultPixels.setChannel(c, metaChannel);
                                        // change the path
                                        result.setChannelID(MetadataTools.createLSID("Channel", 0, c), 0, c);
                                    }

                                    // retrieve the original Plane object
                                    metaPlane = MetaDataUtil.getPlane(metaPixels, cursor.internalT, cursor.internalZ, cursor.internalC);

                                    if (metaPlane != null) {
                                        // remove linked annotation from plane
                                        for (int a = (metaPlane.sizeOfLinkedAnnotationList() - 1); a >= 0; a--)
                                            metaPlane.unlinkAnnotation(metaPlane.getLinkedAnnotation(a));
                                    }
                                }
                                finally {
                                    releaseImporter(position.getPath(), imp);
                                }
                            }
                        }

                        // plane not retrieved from original metadata ? --> create a new empty one
                        if (metaPlane == null)
                            metaPlane = new Plane();

                        // retrieve plane index (use FormatsTools to get it as metadata is not yet complete here)
                        final int resultPlaneInd = FormatTools.getIndex(result.getPixelsDimensionOrder(0).getValue(), group.totalSizeZ, group.totalSizeC,
                                group.totalSizeT, group.totalSizeZ * group.totalSizeC * group.totalSizeT, z, c, t);

                        // set plane
                        resultPixels.setPlane(resultPlaneInd, metaPlane);

                        // adjust CZT info
                        metaPlane.setTheC(OMEUtil.getNonNegativeInteger(c));
                        metaPlane.setTheZ(OMEUtil.getNonNegativeInteger(z));
                        metaPlane.setTheT(OMEUtil.getNonNegativeInteger(t));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public int getTileHeight(final int series) throws Exception {
        if (!isOpen())
            return 0;

        // total size Y
        final int tsy = currentGroup.totalSizeY;
        // image size Y
        final int isy = currentGroup.ident.baseType.sizeY;

        // single image for XY plane or stitched image with sizeY > 2048 --> use image tile height
        if ((tsy == isy) || (isy > 2048)) {
            final String path = getFirstImporterPath();
            final SequenceFileImporter imp = getImporter(path);

            if (imp != null) {
                try {
                    return imp.getTileHeight(series);
                }
                finally {
                    releaseImporter(path, imp);
                }
            }
        }

        // use image size Y as tile height
        return isy;
    }

    @Override
    public int getTileWidth(final int series) throws Exception {
        if (!isOpen())
            return 0;

        // total size X
        final int tsx = currentGroup.totalSizeX;
        // image size X
        final int isx = currentGroup.ident.baseType.sizeX;

        // single image for XY plane or stitched image with sizeX > 2048 --> use image tile width
        if ((tsx == isx) || (isx > 2048)) {
            final String path = getFirstImporterPath();
            final SequenceFileImporter imp = getImporter(path);

            if (imp != null) {
                try {
                    return imp.getTileWidth(series);
                }
                finally {
                    releaseImporter(path, imp);
                }
            }
        }

        // use image size X as tile width
        return isx;
    }

    // internal use only
    private Object getPixelsInternal(final SequencePosition pos, final int series, final int resolution, final Rectangle region, final int z, final int t, final int c) throws Exception {
        if (pos == null) {
            final SequenceType bt = currentGroup.ident.baseType;
            System.err.println("SequenceIdGroupImporter.getPixelsInternal: no image for tile [" + (region.x / bt.sizeX) + "," + (region.y / bt.sizeY) + "] !");
            return null;
        }

        // get importer for this image
        final SequenceFileImporter imp = getImporter(pos.getPath());

        if (imp == null) {
            System.err.println("SequenceIdGroupImporter.getPixelsInternal: cannot get importer for image '" + pos.getPath() + "' !");
            return null;
        }

        try {
            // get pixels from importer (it actually represents a tile of the resulting image)
            return imp.getPixels(series, resolution, region, z, t, c);
        }
        finally {
            // release importer
            releaseImporter(pos.getPath(), imp);
        }
    }

    @Override
    public Object getPixels(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c) throws Exception {
        if (!isOpen())
            return null;

        final SequenceFileGroup group = currentGroup;
        final SequenceIdent ident = group.ident;
        final SequenceType baseType = ident.baseType;

        // define XY region to load (original resolution)
        Rectangle region = new Rectangle(group.totalSizeX, group.totalSizeY);
        if (rectangle != null)
            region = region.intersection(rectangle);

        // get cursor and tile indexes
        final FileCursor cursor = getCursor(z, t, c);
        final List<TileIndex> tiles = getTileIndexes(region);

        // single tile ?
        if (tiles.size() == 1)
            return getPixelsInternal(positions[cursor.index + tiles.get(0).index], series, resolution, region, cursor.internalZ, cursor.internalT, cursor.internalC);

        // define XY region to load (wanted resolution)
        final Rectangle finalRegion = new Rectangle(region.x >> resolution, region.y >> resolution, region.width >> resolution, region.height >> resolution);

        // multiple tiles, create result buffer
        final Object result = Array1DUtil.createArray(baseType.dataType, finalRegion.width * finalRegion.height);
        final boolean signed = baseType.dataType.isSigned();
        // deltas to put tile to region origin
        final int dx = -finalRegion.x;
        final int dy = -finalRegion.y;

        // each tile represent a single image
        for (final TileIndex tile : tiles) {
            // adjusted tile region
            final Rectangle tileRegion = tile.region.intersection(region);
            // get tile pixels
            final Object pixels = getPixelsInternal(positions[cursor.index + tile.index], series, resolution, tileRegion, cursor.internalZ, cursor.internalT,
                    cursor.internalC);

            // cannot retrieve pixels for this tile ? --> ignore
            if (pixels == null)
                continue;

            // tile region (wanted resolution)
            final Rectangle finalTileRegion = new Rectangle(tileRegion.x >> resolution, tileRegion.y >> resolution, tileRegion.width >> resolution,
                    tileRegion.height >> resolution);
            // destination
            final Point pt = finalTileRegion.getLocation();
            pt.translate(dx, dy);

            // copy tile to result
            Array1DUtil.copyRect(pixels, finalTileRegion.getSize(), null, result, finalRegion.getSize(), pt, signed);
        }

        // return full region pixels object
        return result;
    }

    // internal use only
    private IcyBufferedImage getImageInternal(final SequencePosition pos, final int series, final int resolution, final Rectangle region, final int z, final int t, final int c) throws Exception {
        if (pos == null) {
            final SequenceType bt = currentGroup.ident.baseType;
            System.err.println("SequenceIdGroupImporter.getImageInternal: no image for tile [" + (region.x / bt.sizeX) + "," + (region.y / bt.sizeY) + "] !");
            return null;
        }

        // get importer for this image
        final SequenceFileImporter imp = getImporter(pos.getPath());

        if (imp == null) {
            System.err.println("SequenceIdGroupImporter.getImageInternal: cannot get importer for image '" + pos.getPath() + "' !");
            return null;
        }

        try {
            // get image from importer (it actually represents a tile of the resulting image)
            return imp.getImage(series, resolution, region, z, t, c);
        }
        finally {
            // release importer
            releaseImporter(pos.getPath(), imp);
        }
    }

    // internal use only, at this point c cannot be -1
    private IcyBufferedImage getImageInternal(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c) throws Exception {
        if (!isOpen())
            return null;

        final SequenceFileGroup group = currentGroup;
        final SequenceIdent ident = group.ident;
        final SequenceType baseType = ident.baseType;

        // define XY region to load
        Rectangle region = new Rectangle(group.totalSizeX, group.totalSizeY);
        if (rectangle != null)
            region = region.intersection(rectangle);

        // get cursor and tile indexes
        final FileCursor cursor = getCursor(z, t, c);
        final List<TileIndex> tiles = getTileIndexes(region);

        // single tile ?
        if (tiles.size() == 1)
            return getImageInternal(positions[cursor.index + tiles.get(0).index], series, resolution, region, cursor.internalZ, cursor.internalT, cursor.internalC);

        // define XY region to load (wanted resolution)
        final Rectangle finalRegion = new Rectangle(region.x >> resolution, region.y >> resolution, region.width >> resolution, region.height >> resolution);

        // multiple tiles, create result image
        final IcyBufferedImage result = new IcyBufferedImage(finalRegion.width, finalRegion.height, 1, baseType.dataType);
        // colormap save
        IcyColorMap colormap = null;
        // deltas to put tile to region origin
        final int dx = -finalRegion.x;
        final int dy = -finalRegion.y;

        // each tile represent a single image
        for (final TileIndex tile : tiles) {
            // adjusted tile region
            final Rectangle tileRegion = tile.region.intersection(region);
            // get tile pixels
            final IcyBufferedImage image = getImageInternal(positions[cursor.index + tile.index], series, resolution, tileRegion, cursor.internalZ, cursor.internalT, cursor.internalC);

            // cannot retrieve pixels for this tile ? --> ignore
            if (image == null)
                continue;

            // store colormap
            if (colormap == null)
                colormap = image.getColorMap(0);

            // destination
            final Point pt = new Point(tileRegion.x >> resolution, tileRegion.y >> resolution);
            pt.translate(dx, dy);

            // copy tile to image result
            result.copyData(image, null, pt);
        }

        // set colormap
        if (colormap != null)
            result.setColorMap(0, colormap);

        // return full image region
        return result;
    }

    @Override
    public IcyBufferedImage getImage(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c) throws Exception {
        if (!isOpen())
            return null;

        final SequenceFileGroup group = currentGroup;
        final int sizeC = (c == -1) ? group.totalSizeC : 1;
        final List<IcyBufferedImage> result = new ArrayList<>();

        // multi channel ?
        if (sizeC > 1) {
            // handle channel independently we can have channel in separate file
            for (int ch = 0; ch < sizeC; ch++)
                result.add(getImageInternal(series, resolution, rectangle, z, t, ch));
        }
        // single channel
        else
            result.add(getImageInternal(series, resolution, rectangle, z, t, (c == -1) ? 0 : c));

        // we have a null image in the result ?
        if (result.contains(null)) {
            final SequenceIdent ident = group.ident;
            final SequenceType baseType = ident.baseType;

            // define XY region to load
            Rectangle region = new Rectangle(group.totalSizeX, group.totalSizeY);
            if (rectangle != null)
                region = region.intersection(rectangle);

            final int sizeX = region.width >> resolution;
            final int sizeY = region.height >> resolution;

            // replace null by empty image
            for (int i = 0; i < result.size(); i++)
                if (result.get(i) == null)
                    result.set(i, new IcyBufferedImage(sizeX, sizeY, 1, baseType.dataType));
        }

        // then build a single image from all channels
        return IcyBufferedImage.createFrom(result);
    }

    @Override
    public IcyBufferedImage getThumbnail(final int series) throws Exception {
        final OMEXMLMetadata meta = getOMEXMLMetaData();

        // probably no group opened
        if (meta == null)
            return null;

        // stitched image ? --> use default implementation (downscale middle image)
        if (isStitchedImage())
            return super.getThumbnail(series);

        final int sizeZ = MetaDataUtil.getSizeZ(meta, series);
        final int sizeT = MetaDataUtil.getSizeT(meta, series);

        // get cursor for image at middle position
        final FileCursor cursor = getCursor(sizeZ / 2, sizeT / 2, 0);
        final String path;

        if (cursor.position != null)
            // get path for this image
            path = cursor.position.getPath();
        else
            // no image for this position ? --> get first available path
            path = getFirstImporterPath();

        final SequenceFileImporter imp = getImporter(path);

        if (imp == null) {
            // should not happen
            System.err.println("SequenceIdGroupImporter.getThumbnail: cannot find importer...");
            return null;
        }

        try {
            return imp.getThumbnail(series);
        }
        finally {
            releaseImporter(path, imp);
        }
    }
}
