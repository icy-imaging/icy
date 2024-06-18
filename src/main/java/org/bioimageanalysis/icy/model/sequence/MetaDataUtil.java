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
package org.bioimageanalysis.icy.model.sequence;

import loci.common.services.ServiceException;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import ome.units.quantity.Time;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.Image;
import ome.xml.model.*;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.OMEUtil;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.joda.time.Instant;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Meta data utilities class.<br>
 * Basically provide safe access to metadata.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class MetaDataUtil {
    public static final String DEFAULT_CHANNEL_NAME = "ch ";

    /**
     * @return Returns OME root element (create it if needed).
     * @param metaData
     *        OME metadata
     */
    public static OME getOME(final OMEXMLMetadata metaData) {
        OME result = (OME) metaData.getRoot();

        if (result == null) {
            metaData.createRoot();
            result = (OME) metaData.getRoot();
        }

        return result;
    }

    /**
     * @return Returns the number of image series of the specified metaData description.
     * @param metaData
     *        OME metadata
     */
    public static int getNumSeries(final OMEXMLMetadata metaData) {
        return metaData.getImageCount();
    }

    /**
     * @return Return image series object at specified index for the specified metaData description.
     * @param metaData
     *        OME metadata
     */
    public static Image getSeries(final OMEXMLMetadata metaData, final int index) {
        final OME ome = getOME(metaData);

        if (index < ome.sizeOfImageList())
            return ome.getImage(index);

        return null;
    }

    /**
     * @return Ensure the image series at specified index exist for the specified metaData description.
     * @param index
     *        int
     * @param ome
     *        OME
     */
    public static Image ensureSeries(final OME ome, final int index) {
        // create missing image
        while (ome.sizeOfImageList() <= index) {
            final Image img = new Image();
            ome.addImage(img);
        }

        final Image result = ome.getImage(index);

        if (result.getPixels() == null) {
            final Pixels pix = new Pixels();
            // wanted default dimension order
            pix.setDimensionOrder(DimensionOrder.XYCZT);
            // create default pixels object
            result.setPixels(pix);
        }

        return result;
    }

    /**
     * Set the number of image series for the specified metaData description.
     *
     * @param metaData
     *        OME metadata
     * @param num
     *        int
     */
    public static void setNumSeries(final OMEXMLMetadata metaData, final int num) {
        final OME ome = getOME(metaData);

        // keep only desired number of image
        while (ome.sizeOfImageList() > num)
            ome.removeImage(ome.getImage(ome.sizeOfImageList() - 1));

        // create missing image
        ensureSeries(ome, num - 1);
    }

    /**
     * @return Return pixels object at specified index for the specified metaData description.
     * @param index
     *        int
     * @param ome
     *        OME
     */
    public static Pixels getPixels(final OME ome, final int index) {
        if (ome != null) {
            if (index < ome.sizeOfImageList())
                return ome.getImage(index).getPixels();
        }

        return null;
    }

    /**
     * @return Return pixels object at specified index for the specified metaData description.
     * @param metaData
     *        OME metadata
     * @param index
     *        int
     */
    public static Pixels getPixels(final OMEXMLMetadata metaData, final int index) {
        return getPixels(getOME(metaData), index);
    }

    /**
     * @return Return plane index for the specified T, Z, C position.
     * @param pix
     *        pixel
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    public static int getPlaneIndex(final Pixels pix, final int t, final int z, final int c) {
        // can't compute plane index --> return 0 by default
        if ((t < 0) || (z < 0) || (c < 0))
            return 0;
        // trivial opti...
        if ((t == 0) && (z == 0) && (c == 0))
            return 0;

        final int sizeT = OMEUtil.getValue(pix.getSizeT(), 0);
        final int sizeZ = OMEUtil.getValue(pix.getSizeZ(), 0);
        int sizeC = OMEUtil.getValue(pix.getSizeC(), 0);

        // can't compute plane index --> return 0 by default
        if ((sizeT == 0) || (sizeZ == 0) || (sizeC == 0))
            return 0;

        int adjC = c;

        if (pix.sizeOfChannelList() > 0) {
            final Channel channel = pix.getChannel(0);
            if (channel != null) {
                final int spp = OMEUtil.getValue(channel.getSamplesPerPixel(), 0);
                // channel are packed in pixel so consider sizeC = 1
                if ((spp != 0) && (spp == sizeC)) {
                    sizeC = 1;
                    adjC = 0;
                }
            }
        }

        // first try to get index from real plan position
        final int len = pix.sizeOfPlaneList();
        for (int i = 0; i < len; i++) {
            final Plane plane = pix.getPlane(i);

            // plane found --> return index
            if ((OMEUtil.getValue(plane.getTheT(), -1) == t) && (OMEUtil.getValue(plane.getTheZ(), -1) == z)
                    && (OMEUtil.getValue(plane.getTheC(), -1) == c))
                return i;
        }

        DimensionOrder dimOrder = pix.getDimensionOrder();
        // use default dimension order
        if (dimOrder == null)
            dimOrder = DimensionOrder.XYCZT;

        // use computed method
        return FormatTools.getIndex(dimOrder.getValue(), sizeZ, sizeC, sizeT, sizeZ * sizeC * sizeT, z, adjC, t);
    }

    public static Plane getPlane(final Pixels pix, final int index) {
        if (pix != null) {
            if (index < pix.sizeOfPlaneList())
                return pix.getPlane(index);
        }

        return null;
    }

    /**
     * @return Return plane object for the specified T, Z, C position.
     * @param pix
     *        Pixel
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    public static Plane getPlane(final Pixels pix, final int t, final int z, final int c) {
        return getPlane(pix, getPlaneIndex(pix, t, z, c));
    }

    /**
     * @return Ensure the plane at specified index exist for the specified Pixels object.
     * @param index
     *        int
     * @param pix
     *        Pixel
     */
    public static Plane ensurePlane(final Pixels pix, final int index) {
        // create missing plane
        while (pix.sizeOfPlaneList() <= index)
            pix.addPlane(new Plane());

        return pix.getPlane(index);
    }

    /**
     * @return Ensure the plane at specified T, Z, C position exist for the specified Pixels object.
     * @param pix
     *        Pixel
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    public static Plane ensurePlane(final Pixels pix, final int t, final int z, final int c) {
        return ensurePlane(pix, getPlaneIndex(pix, t, z, c));
    }

    /**
     * Remove the plane at specified position.
     *
     * @param index
     *        int
     * @param img
     *        image
     * @return <code>true</code> if the operation succeed, <code>false</code> otherwise
     */
    public static boolean removePlane(final Image img, final int index) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return false;

        final int numPlane = pix.sizeOfPlaneList();

        // single plane information or no plane here --> return false
        if ((numPlane <= 1) || (index >= numPlane))
            return false;

        final Plane plane = getPlane(pix, index);

        // remove plane
        pix.removePlane(plane);

        // remove associated annotation
        for (int i = 0; i < plane.sizeOfLinkedAnnotationList(); i++)
            img.unlinkAnnotation(plane.getLinkedAnnotation(i));

        // clean some data
        if (pix.sizeOfBinDataList() == numPlane)
            pix.removeBinData(pix.getBinData(index));
        if (pix.sizeOfTiffDataList() == numPlane)
            pix.removeTiffData(pix.getTiffData(index));

        return true;
    }

    /**
     * Remove the plane at specified position.
     *
     * @param img
     *        image
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return <code>true</code> if the operation succeed, <code>false</code> otherwise
     */
    public static boolean removePlane(final Image img, final int t, final int z, final int c) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return false;

        return removePlane(img, getPlaneIndex(pix, t, z, c));
    }

    /**
     * Remove the plane at specified position.
     *
     * @param metadata
     *        OME metadata
     * @param series
     *        int
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return <code>true</code> if the operation succeed, <code>false</code> otherwise
     */
    public static boolean removePlane(final OMEXMLMetadata metadata, final int series, final int t, final int z, final int c) {
        final Image img = getSeries(metadata, series);
        if (img == null)
            return false;

        return removePlane(img, t, z, c);
    }

    /**
     * Remove planes at given position
     *
     * @param posT
     *        T position where we want to remove metadata (-1 for all)
     * @param posZ
     *        Z position where we want to remove metadata (-1 for all)
     * @param posC
     *        C position where we want to remove metadata (-1 for all)
     * @param metadata
     *        OME metadta
     * @param series
     *        int
     */
    public static void removePlanes(final OMEXMLMetadata metadata, final int series, final int posT, final int posZ, final int posC) {
        final int minT, maxT;
        final int minZ, maxZ;
        final int minC, maxC;

        if (posT < 0) {
            minT = 0;
            maxT = getSizeT(metadata, series) - 1;
        }
        else {
            minT = posT;
            maxT = posT;
        }
        if (posZ < 0) {
            minZ = 0;
            maxZ = getSizeZ(metadata, series) - 1;
        }
        else {
            minZ = posZ;
            maxZ = posZ;
        }
        if (posC < 0) {
            minC = 0;
            maxC = getSizeC(metadata, series) - 1;
        }
        else {
            minC = posC;
            maxC = posC;
        }

        for (int t = minT; t <= maxT; t++)
            for (int z = minZ; z <= maxZ; z++)
                for (int c = minC; c <= maxC; c++)
                    MetaDataUtil.removePlane(metadata, 0, t, z, c);
    }

    /**
     * @return Returns the data type of the specified image series.
     * @param metaData
     *        OME metedata
     * @param series
     *        int
     */
    public static DataType getDataType(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return DataType.getDataTypeFromPixelType(pix.getType());

        // assume byte by default
        return DataType.UBYTE;
    }

    /**
     * @return Returns the width (sizeX) of the specified image series.
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     */
    public static int getSizeX(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeX(), 0);

        return 0;
    }

    /**
     * Returns the height (sizeY) of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     */
    public static int getSizeY(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeY(), 0);

        return 0;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the number of channel (sizeC) of the specified image series.
     */
    public static int getSizeC(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeC(), 0);

        return 0;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the depth (sizeZ) of the specified image series.
     */
    public static int getSizeZ(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getSizeZ(), 0);

        return 0;
    }

    /**
     * @param pix
     *        Pixel
     * @return Returns the number of frame (sizeT) of the specified Pixels object.
     */
    private static int getSizeT(final Pixels pix) {
        return OMEUtil.getValue(pix.getSizeT(), 0);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the number of frame (sizeT) of the specified image series.
     */
    public static int getSizeT(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return getSizeT(pix);

        return 0;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the total data size (in bytes) of the specified image series.
     */
    public static long getDataSize(final OMEXMLMetadata metaData, final int series) {
        return getDataSize(metaData, series, 0);
    }

    /**
     * @return Returns the total data size (in bytes) of the specified image series
     * @param resolution
     *        for the given resolution (0 = full, 1 = 1/2, ...)
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     */
    public static long getDataSize(final OMEXMLMetadata metaData, final int series, final int resolution) {
        return getDataSize(metaData, series, resolution, getSizeZ(metaData, series), getSizeT(metaData, series));
    }

    /**
     * @return Returns the total data size (in bytes) of the specified image series
     * @param resolution
     *        for the given resolution (0 = full, 1 = 1/2, ...) and size informations
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeT
     *        int
     * @param sizeZ
     *        int
     */
    public static long getDataSize(final OMEXMLMetadata metaData, final int series, final int resolution, final int sizeZ, final int sizeT) {
        return getDataSize(metaData, series, resolution, sizeZ, sizeT, getSizeC(metaData, series));
    }

    /**
     * @return Returns the total data size (in bytes) of the specified image series
     * @param resolution
     *        for the given resolution (0 = full, 1 = 1/2, ...) and size informations
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeC
     *        int
     * @param sizeT
     *        int
     * @param sizeZ
     *        int
     */
    public static long getDataSize(final OMEXMLMetadata metaData, final int series, final int resolution, final int sizeZ, final int sizeT, final int sizeC) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            long sizeXY = (long) OMEUtil.getValue(pix.getSizeX(), 0) * (long) OMEUtil.getValue(pix.getSizeY(), 0);

            if (resolution > 0)
                sizeXY /= Math.pow(4d, resolution);

            return sizeXY * sizeC * sizeZ * sizeT * Objects.requireNonNull(DataType.getDataTypeFromPixelType(pix.getType())).getSize();
        }

        return 0L;
    }

    /**
     * Sets the data type of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param dataType
     *        object
     */
    public static void setDataType(final OMEXMLMetadata metaData, final int series, final DataType dataType) {
        metaData.setPixelsType(dataType.toPixelType(), series);
    }

    /**
     * Sets the width (sizeX) of the specified image series (need to be &gt;= 1).
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeX
     *        int
     */
    public static void setSizeX(final OMEXMLMetadata metaData, final int series, final int sizeX) {
        metaData.setPixelsSizeX(OMEUtil.getPositiveInteger(sizeX), series);
    }

    /**
     * Sets the height (sizeY) of the specified image series (need to be &gt;= 1).
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeY
     *        int
     */
    public static void setSizeY(final OMEXMLMetadata metaData, final int series, final int sizeY) {
        metaData.setPixelsSizeY(OMEUtil.getPositiveInteger(sizeY), series);
    }

    /**
     * Sets the number of channel (sizeC) of the specified image series (need to be &gt;= 1).
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeC
     *        int
     */
    public static void setSizeC(final OMEXMLMetadata metaData, final int series, final int sizeC) {
        metaData.setPixelsSizeC(OMEUtil.getPositiveInteger(sizeC), series);
    }

    /**
     * Sets the depth (sizeZ) of the specified image series (need to be &gt;= 1).
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeZ
     *        int
     */
    public static void setSizeZ(final OMEXMLMetadata metaData, final int series, final int sizeZ) {
        metaData.setPixelsSizeZ(OMEUtil.getPositiveInteger(sizeZ), series);
    }

    /**
     * Sets the number of frame (sizeT) of the specified image series (need to be gt;= 1).
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param sizeT
     *        int
     */
    public static void setSizeT(final OMEXMLMetadata metaData, final int series, final int sizeT) {
        metaData.setPixelsSizeT(OMEUtil.getPositiveInteger(sizeT), series);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the id of the specified image series.
     */
    public static String getImageID(final OMEXMLMetadata metaData, final int series) {
        final Image img = getSeries(metaData, series);

        if (img != null)
            return StringUtil.getValue(img.getID(), "");

        return "";
    }

    /**
     * Set the id of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        string
     */
    public static void setImageID(final OMEXMLMetadata metaData, final int series, final String value) {
        metaData.setImageID(value, series);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the name of the specified image series.
     */
    public static String getName(final OMEXMLMetadata metaData, final int series) {
        final Image img = getSeries(metaData, series);

        if (img != null)
            return StringUtil.getValue(img.getName(), "");

        return "";
    }

    /**
     * Set the name of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        string
     */
    public static void setName(final OMEXMLMetadata metaData, final int series, final String value) {
        metaData.setImageName(value, series);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @return Returns X pixel size (in &micro;m) of the specified image series.
     */
    public static double getPixelSizeX(final OMEXMLMetadata metaData, final int series, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeX(), defaultValue);

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @return Returns Y pixel size (in &micro;m) of the specified image series.
     */
    public static double getPixelSizeY(final OMEXMLMetadata metaData, final int series, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeY(), defaultValue);

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @return Returns Z pixel size (in &micro;m) of the specified image series.
     */
    public static double getPixelSizeZ(final OMEXMLMetadata metaData, final int series, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return OMEUtil.getValue(pix.getPhysicalSizeZ(), defaultValue);

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Computes and returns the T time interval (in second) from internal time positions.<br>
     *         If there is no internal time positions <code>0d</code> is returned.
     */
    public static double getTimeIntervalFromTimePositions(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        // try to compute time interval from time position
        if (pix != null)
            return computeTimeIntervalFromTimePosition(pix);

        return 0d;
    }

    /**
     * @param pix
     *        Pixel
     * @param defaultValue
     *        double
     * @return Returns T time interval (in second) for the specified image series.
     */
    private static double getTimeInterval(final Pixels pix, final double defaultValue) {
        final Time timeInc = pix.getTimeIncrement();

        if (timeInc != null)
            return OMEUtil.getValue(timeInc, defaultValue);

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @return Returns T time interval (in second) for the specified image series.
     */
    public static double getTimeInterval(final OMEXMLMetadata metaData, final int series, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return getTimeInterval(pix, defaultValue);

        return defaultValue;
    }

    /**
     * Set X pixel size (in &micro;m) of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     */
    public static void setPixelSizeX(final OMEXMLMetadata metaData, final int series, final double value) {
        metaData.setPixelsPhysicalSizeX(OMEUtil.getLength(value), series);
    }

    /**
     * Set Y pixel size (in &micro;m) of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     */
    public static void setPixelSizeY(final OMEXMLMetadata metaData, final int series, final double value) {
        metaData.setPixelsPhysicalSizeY(OMEUtil.getLength(value), series);
    }

    /**
     * Set Z pixel size (in &micro;m) of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     */
    public static void setPixelSizeZ(final OMEXMLMetadata metaData, final int series, final double value) {
        metaData.setPixelsPhysicalSizeZ(OMEUtil.getLength(value), series);
    }

    /**
     * Set T time resolution (in second) of the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     */
    public static void setTimeInterval(final OMEXMLMetadata metaData, final int series, final double value) {
        metaData.setPixelsTimeIncrement(OMEUtil.getTime(value), series);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return Returns the X field position (in &micro;m) for the image at the specified Z, T, C position.
     */
    public static double getPositionX(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            final Plane plane = getPlane(pix, t, z, c);

            if (plane != null)
                return OMEUtil.getValue(plane.getPositionX(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return Returns the Y field position (in &micro;m) for the image at the specified Z, T, C position.
     */
    public static double getPositionY(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            final Plane plane = getPlane(pix, t, z, c);

            if (plane != null)
                return OMEUtil.getValue(plane.getPositionY(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return Returns the Z field position (in &micro;m) for the image at the specified Z, T, C position.
     */
    public static double getPositionZ(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            final Plane plane = getPlane(pix, t, z, c);

            if (plane != null)
                return OMEUtil.getValue(plane.getPositionZ(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @return as {@link #getTimeStamp(OMEXMLMetadata, int, long)}
     */
    public static double getPositionT(final OMEXMLMetadata metaData, final int series, final long defaultValue) {
        return getTimeStamp(metaData, series, defaultValue);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @return Returns the time stamp (elapsed milliseconds from the Java epoch of 1970-01-01 T00:00:00Z) for the specified image.
     */
    public static long getTimeStamp(final OMEXMLMetadata metaData, final int series, final long defaultValue) {
        final Image img = getSeries(metaData, series);

        if (img != null) {
            final Timestamp time = img.getAcquisitionDate();

            if (time != null)
                return time.asInstant().getMillis();
        }

        return defaultValue;
    }

    /**
     * @param pix
     *        Pixel
     * @param defaultValue
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return Returns the time position offset (in second) relative to first image for the image at the specified Z, T, C position.
     */
    private static double getPositionTOffset(final Pixels pix, final int t, final int z, final int c, final double defaultValue) {
        double result = -1d;
        final Plane plane = getPlane(pix, t, z, c);

        if (plane != null)
            result = OMEUtil.getValue(plane.getDeltaT(), -1d);

        // got it from DeltaT
        if (result != -1d)
            return result;

        // try from time interval instead
        result = getTimeInterval(pix, -1d);

        // we were able to get time interval ? just multiply it by T index
        if (result != -1d)
            return result * t;

        return defaultValue;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param defaultValue
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     * @return Returns the time position offset (in second) relative to first image for the image at the specified Z, T, C position.
     */
    public static double getPositionTOffset(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c,
                                            final double defaultValue) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return getPositionTOffset(pix, t, z, c, defaultValue);

        return defaultValue;
    }

    /**
     * @param pix
     *        Pixel
     *        Computes time interval (in second) from the time position informations.<br>
     * @return Returns <code>0d</code> if time position informations are missing ot if we have only 1 frame in the image.
     */
    private static double computeTimeIntervalFromTimePosition(final Pixels pix) {
        final int sizeT = getSizeT(pix);

        if (sizeT <= 1)
            return 0d;

        double result = 0d;
        double last = -1d;
        int lastT = 0;
        int num = 0;

        for (int t = 0; t < sizeT; t++) {
            final Plane plane = getPlane(pix, t, 0, 0);

            if (plane != null) {
                final double timePos = OMEUtil.getValue(plane.getDeltaT(), Double.NaN);

                if (!Double.isNaN(timePos)) {
                    if (last != -1d) {
                        // get delta
                        result += (timePos - last) / (t - lastT);
                        num++;
                    }

                    last = timePos;
                    lastT = t;
                }
            }
        }

        // we need at least 1 delta
        if (num == 0)
            return 0d;

        return result / num;
    }

    /**
     * Sets the X field position (in &micro;m) for the image at the specified Z, T, C position.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    public static void setPositionX(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double value) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            final Plane plane = ensurePlane(pix, t, z, c);

            if (plane != null)
                plane.setPositionX(OMEUtil.getLength(value));
        }
    }

    /**
     * Sets the Y field position (in &micro;m) for the image at the specified Z, T, C position.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     * @param c
     *        int
     * @param z
     *        int
     * @param t
     *        int
     */
    public static void setPositionY(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double value) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            final Plane plane = ensurePlane(pix, t, z, c);

            if (plane != null)
                plane.setPositionY(OMEUtil.getLength(value));
        }
    }

    /**
     * Sets the Z field position (in &micro;m) for the image at the specified Z, T, C position.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    public static void setPositionZ(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double value) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null) {
            final Plane plane = ensurePlane(pix, t, z, c);

            if (plane != null)
                plane.setPositionZ(OMEUtil.getLength(value));
        }
    }

    /**
     * Same as {@link #setTimeStamp(OMEXMLMetadata, int, long)}
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        long
     */
    public static void setPositionT(final OMEXMLMetadata metaData, final int series, final long value) {
        setTimeStamp(metaData, series, value);
    }

    /**
     * Sets the time stamp (elapsed milliseconds from the Java epoch of 1970-01-01 T00:00:00Z) for the specified image.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        long
     */
    public static void setTimeStamp(final OMEXMLMetadata metaData, final int series, final long value) {
        final Image img = getSeries(metaData, series);

        if (img != null)
            img.setAcquisitionDate(new Timestamp(new Instant(value)));
    }

    /**
     * Sets the time position offset (in second) relative to the first image for the image at the specified Z, T, C position.
     *
     * @param pix
     *        Pixel
     * @param value
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    private static void setPositionTOffset(final Pixels pix, final int t, final int z, final int c, final double value) {
        final Plane plane = getPlane(pix, t, z, c);

        if (plane != null)
            plane.setDeltaT(OMEUtil.getTime(value));
    }

    /**
     * Sets the time position offset (in second) relative to the first image for the image at the specified Z, T, C position.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param value
     *        double
     * @param c
     *        int
     * @param t
     *        int
     * @param z
     *        int
     */
    public static void setPositionTOffset(final OMEXMLMetadata metaData, final int series, final int t, final int z, final int c, final double value) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            setPositionTOffset(pix, t, z, c, value);
    }

    /**
     * @param channel
     *        int
     * @return Get default name for specified channel.
     */
    public static String getDefaultChannelName(final int channel) {
        return DEFAULT_CHANNEL_NAME + channel;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @return Returns the number of channel for the specified image series in metaData description.
     */
    public static int getNumChannel(final OMEXMLMetadata metaData, final int series) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return pix.sizeOfChannelList();

        return 0;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param index
     *        int
     * @return Return channel object at specified index for the specified image series.
     */
    public static Channel getChannel(final OMEXMLMetadata metaData, final int series, final int index) {
        final Pixels pix = getPixels(metaData, series);

        if ((pix != null) && (index < pix.sizeOfChannelList()))
            return pix.getChannel(index);

        return null;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param index
     *        int
     * @return Ensure the channel at specified index exist for the specified image series.
     */
    public static Channel ensureChannel(final OMEXMLMetadata metaData, final int series, final int index) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            return ensureChannel(pix, index);

        return null;
    }

    /**
     * @param pix
     *        Pixel
     * @param index
     *        int
     * @return Ensure the channel at specified index exist for the specified image series.
     */
    public static Channel ensureChannel(final Pixels pix, final int index) {
        // create missing channel
        while (pix.sizeOfChannelList() <= index)
            pix.addChannel(new Channel());

        return pix.getChannel(index);
    }

    /**
     * Remove a channel for the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param index
     *        int
     */
    public static void removeChannel(final OMEXMLMetadata metaData, final int series, final int index) {
        final Pixels pix = getPixels(metaData, series);

        if (pix != null)
            removeChannel(pix, index);
    }

    /**
     * Remove a channel from the specified Pixels object.
     *
     * @param pix
     *        Pixel
     * @param index
     *        int
     */
    public static void removeChannel(final Pixels pix, final int index) {
        if (pix.sizeOfChannelList() > index)
            pix.removeChannel(pix.getChannel(index));
    }

    /**
     * Set the number of channel for the specified image series in metaData description.<br>
     * This is different from {@link #getSizeC(OMEXMLMetadata, int)}.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param num
     *        int
     */
    public static void setNumChannel(final OMEXMLMetadata metaData, final int series, final int num) {
        final OME ome = getOME(metaData);

        ensureSeries(ome, series);

        final Image img = ome.getImage(series);
        Pixels pix = img.getPixels();

        if (pix == null) {
            // create pixels object
            pix = new Pixels();
            img.setPixels(pix);
        }

        // keep only desired number of image
        while (pix.sizeOfChannelList() > num)
            removeChannel(pix, pix.sizeOfChannelList() - 1);

        // create missing image
        ensureChannel(pix, num - 1);
    }

    /**
     * Initialize default channel name until specified index if they are missing from the meta data
     * description.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param channel
     *        int
     */
    private static void prepareMetaChannelName(final OMEXMLMetadata metaData, final int series, final int channel) {
        int c = getNumChannel(metaData, series);

        while (channel >= c) {
            // set default channel name
            metaData.setChannelName(getDefaultChannelName(c), series, c);
            c++;
        }
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param channel
     *        int
     * @return Returns name of specified channel image series.
     */
    public static String getChannelName(final OMEXMLMetadata metaData, final int series, final int channel) {
        // needed as LOCI does not initialize them on read
        prepareMetaChannelName(metaData, series, channel);

        final String result = StringUtil.getValue(metaData.getChannelName(series, channel),
                getDefaultChannelName(channel));
        final String cleaned = XMLUtil.filterString(result);

        // cleaned string != original value --> set it
        if (!cleaned.equals(result))
            setChannelName(metaData, series, channel, cleaned);

        return cleaned;
    }

    /**
     * Set name of specified channel image series.
     *
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param channel
     *        int
     * @param value
     *        string
     */
    public static void setChannelName(final OMEXMLMetadata metaData, final int series, final int channel, final String value) {
        // needed as LOCI only add current channel if it's missing
        prepareMetaChannelName(metaData, series, channel - 1);

        metaData.setChannelName(value, series, channel);
    }

    /**
     * @param metaData
     *        OME metadata
     * @param series
     *        int
     * @param channel
     *        int
     * @return Returns Color of specified channel image series.
     */
    public static Color getChannelColor(final OMEXMLMetadata metaData, final int series, final int channel) {
        // needed as LOCI does not initialize them on read
        prepareMetaChannelName(metaData, series, channel);

        return OMEUtil.getJavaColor(metaData.getChannelColor(series, channel));
    }

    /**
     * @return the last Experimenter user name (it may have several experimenter)
     * @param metaData
     *        OME metadata
     */
    public static String getUserName(final OMEXMLMetadata metaData) {
        if (metaData.getExperimenterCount() > 0)
            return metaData.getExperimenterUserName(0);

        return "";
    }

    /**
     * @return the list of Experimenter user name
     * @param metaData
     *        OME metadata
     */
    public static List<String> getUserNames(final OMEXMLMetadata metaData) {
        final List<String> result = new ArrayList<>();

        for (int i = 0; i < metaData.getExperimenterCount(); i++)
            result.add(metaData.getExperimenterUserName(i));

        return result;
    }

    /**
     * @param metaData
     *        OME metadata
     * @param userName
     *        user name to search for
     * @return <i>true</i> if the given user name is already existing in metadata user name list
     */
    public static boolean containsUserName(final OMEXMLMetadata metaData, final String userName) {
        for (int i = 0; i < metaData.getExperimenterCount(); i++)
            if (StringUtil.equals(metaData.getExperimenterUserName(i), userName))
                return true;

        return false;
    }

    /**
     * Set the Experimenter user name.<br>
     * If we already have experimenter user name then the function will add a new experimenter user name
     * to keep trace of <i>history</i>
     *
     * @param metaData
     *        OME metadata
     * @param userName
     *        user name to set
     * @see #getUserNames(OMEXMLMetadata)
     */
    public static void setUserName(final OMEXMLMetadata metaData, final String userName) {
        // not already present ? --> add new experimenter user name
        if (!containsUserName(metaData, userName)) {
            final int ind = metaData.getExperimenterCount();
            metaData.setExperimenterUserName(userName, ind);

            // need to set an ID ?
            if (StringUtil.isEmpty(metaData.getExperimenterID(ind)))
                metaData.setExperimenterID("Experimenter:" + ind, ind);
        }
    }

    /**
     * @param name
     *        string
     * @return Create and return a default (OME XML) Metadata object with default image name.
     */
    public static OMEXMLMetadata createMetadata(final String name) {
        final OMEXMLMetadata result = OMEUtil.createOMEXMLMetadata();
        final OME ome = getOME(Objects.requireNonNull(result));

        ensureSeries(ome, 0);

        result.setImageID(MetadataTools.createLSID("Image", 0), 0);
        result.setImageName(name, 0);

        return result;
    }

    /**
     * Set metadata object with the given image properties.
     *
     * @param metadata
     *        metadata object to fill.
     * @param sizeX
     *        width in pixels (need to be &gt;= 1)
     * @param sizeY
     *        height in pixels (need to be &gt;= 1)
     * @param sizeC
     *        number of channel (need to be &gt;= 1)
     * @param sizeZ
     *        number of Z slices (need to be &gt;= 1)
     * @param sizeT
     *        number of T frames (need to be &gt;= 1)
     * @param dataType
     *        data type.
     * @param separateChannel
     *        true if we want channel data to be separated.
     */
    public static void setMetaData(final OMEXMLMetadata metadata, final int sizeX, final int sizeY, final int sizeC, final int sizeZ, final int sizeT, final DataType dataType, final boolean separateChannel) {
        OME ome = (OME) metadata.getRoot();

        if (ome == null) {
            metadata.createRoot();
            ome = (OME) metadata.getRoot();
        }

        // keep only one image
        setNumSeries(metadata, 1);
        // clean TiffData metadata (can produce error on reloading)
        cleanTiffData(ome.getImage(0));
        // clean binData metadata (can produce error on reloading)
        cleanBinData(ome.getImage(0));

        if (StringUtil.isEmpty(metadata.getImageID(0)))
            metadata.setImageID(MetadataTools.createLSID("Image", 0), 0);
        if (StringUtil.isEmpty(metadata.getImageName(0)))
            metadata.setImageName("Sample", 0);

        if (StringUtil.isEmpty(metadata.getPixelsID(0)))
            metadata.setPixelsID(MetadataTools.createLSID("Pixels", 0), 0);

        // prefer big endian as JVM is big endian
        metadata.setPixelsBigEndian(Boolean.TRUE, 0);
        metadata.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        // force XYCZT dimension order
        metadata.setPixelsDimensionOrder(DimensionOrder.XYCZT, 0);

        // adjust pixel type and dimension size
        metadata.setPixelsType(dataType.toPixelType(), 0);
        metadata.setPixelsSizeX(OMEUtil.getPositiveInteger(sizeX), 0);
        metadata.setPixelsSizeY(OMEUtil.getPositiveInteger(sizeY), 0);
        metadata.setPixelsSizeC(OMEUtil.getPositiveInteger(sizeC), 0);
        metadata.setPixelsSizeZ(OMEUtil.getPositiveInteger(sizeZ), 0);
        metadata.setPixelsSizeT(OMEUtil.getPositiveInteger(sizeT), 0);

        // clean plane metadata outside allowed range
        cleanPlanes(ome.getImage(0));

        // get time interval information
        double timeInterval = MetaDataUtil.getTimeInterval(metadata, 0, 0d);
        // not defined ?
        if (timeInterval == 0d) {
            // try to compute it from time positions
            timeInterval = getTimeIntervalFromTimePositions(metadata, 0);
            // we got something --> set it as the time interval
            if (timeInterval != 0d)
                MetaDataUtil.setTimeInterval(metadata, 0, timeInterval);
        }

        // fix channel number depending separate channel flag
        if (separateChannel) {
            // set channel number
            setNumChannel(metadata, 0, sizeC);

            for (int c = 0; c < sizeC; c++) {
                if (StringUtil.isEmpty(metadata.getChannelID(0, c)))
                    metadata.setChannelID(MetadataTools.createLSID("Channel", 0, c), 0, c);
                metadata.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(1)), 0, c);
                // metadata.getChannelName(0, c);
            }
        }
        else {
            // set channel number
            setNumChannel(metadata, 0, 1);

            if (StringUtil.isEmpty(metadata.getChannelID(0, 0)))
                metadata.setChannelID(MetadataTools.createLSID("Channel", 0, 0), 0, 0);
            metadata.setChannelSamplesPerPixel(new PositiveInteger(Integer.valueOf(sizeC)), 0, 0);
        }
    }

    /**
     * Generates meta data for the given image properties.
     *
     * @param sizeX
     *        width in pixels.
     * @param sizeY
     *        height in pixels.
     * @param sizeC
     *        number of channel.
     * @param sizeZ
     *        number of Z slices.
     * @param sizeT
     *        number of T frames.
     * @param dataType
     *        data type.
     * @param separateChannel
     *        true if we want channel data to be separated.
     * @return OMEXMLMetadata
     */
    public static OMEXMLMetadata generateMetaData(final int sizeX, final int sizeY, final int sizeC, final int sizeZ, final int sizeT, final DataType dataType, final boolean separateChannel) {
        final OMEXMLMetadata result = createMetadata("Sample");

        setMetaData(result, sizeX, sizeY, sizeC, sizeZ, sizeT, dataType, separateChannel);

        return result;
    }

    /**
     * @param dataType
     *        object
     * @param separateChannel
     *        boolean
     * @param sizeX
     *        int
     * @param sizeY
     *        int
     * @param sizeC
     *        int
     * @return Generates Meta Data for the given arguments.
     * @see #setMetaData(OMEXMLMetadata, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(final int sizeX, final int sizeY, final int sizeC, final DataType dataType, final boolean separateChannel) throws ServiceException {
        return generateMetaData(sizeX, sizeY, sizeC, 1, 1, dataType, separateChannel);
    }

    /**
     * @param separateChannel
     *        boolean
     * @param image
     *        image
     * @return Generates Meta Data for the given BufferedImage.
     * @see #setMetaData(OMEXMLMetadata, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(final IcyBufferedImage image, final boolean separateChannel) throws ServiceException {
        return generateMetaData(image.getSizeX(), image.getSizeY(), image.getSizeC(), image.getDataType(), separateChannel);
    }

    /**
     * @param sequence
     *        sequence
     * @param separateChannel
     *        boolean
     * @return Generates Meta Data for the given Sequence.
     * @see #setMetaData(OMEXMLMetadata, int, int, int, int, int, DataType, boolean)
     */
    public static OMEXMLMetadata generateMetaData(final Sequence sequence, final boolean separateChannel) {
        // do a copy as we mean use several time the same source sequence metadata (preserve user name here)
        final OMEXMLMetadata result = OMEUtil.createOMEXMLMetadata(sequence.getOMEXMLMetadata(), false);

        setMetaData(result, sequence.getSizeX(), sequence.getSizeY(), sequence.getSizeC(), sequence.getSizeZ(),
                sequence.getSizeT(), sequence.getDataType(), separateChannel);

        return result;
    }

    /**
     * Keep only the specified image series.
     *
     * @param metaData
     *        OME metadata
     * @param num
     *        int
     */
    public static void keepSingleSerie(final OMEXMLMetadata metaData, final int num) {
        final OME ome = getOME(metaData);
        final int numSeries = ome.sizeOfImageList();
        final Image img = getSeries(metaData, num);

        // nothing to do
        if (img == null)
            return;

        // keep only the desired image
        for (int i = numSeries - 1; i >= 0; i--)
            if (i != num)
                ome.removeImage(ome.getImage(i));

        final Set<Object> toKeep = new HashSet<>();

        // try to keep associated dataset only
        toKeep.clear();
        for (int i = 0; i < img.sizeOfLinkedDatasetList(); i++)
            toKeep.add(img.getLinkedDataset(i));
        if (!toKeep.isEmpty()) {
            for (int i = ome.sizeOfDatasetList() - 1; i >= 0; i--) {
                final Dataset obj = ome.getDataset(i);
                if (!toKeep.contains(obj))
                    ome.removeDataset(obj);
            }
        }
        // just assume they are indirectly linked
        else if (ome.sizeOfDatasetList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeDataset(ome.getDataset(i));
        }

        // try to keep associated ROI only
        toKeep.clear();
        for (int i = 0; i < img.sizeOfLinkedROIList(); i++)
            toKeep.add(img.getLinkedROI(i));
        if (!toKeep.isEmpty()) {
            for (int i = ome.sizeOfROIList() - 1; i >= 0; i--) {
                final ROI obj = ome.getROI(i);
                if (!toKeep.contains(obj))
                    ome.removeROI(obj);
            }
        }
        // just assume they are indirectly linked
        else if (ome.sizeOfROIList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeROI(ome.getROI(i));
        }

        // try to keep associated experiment only
        final Experiment exp = img.getLinkedExperiment();
        if (exp != null) {
            for (int i = ome.sizeOfExperimentList() - 1; i >= 0; i--) {
                final Experiment obj = ome.getExperiment(i);
                if (obj != exp)
                    ome.removeExperiment(obj);
            }
        }
        else if (ome.sizeOfExperimentList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeExperiment(ome.getExperiment(i));
        }

        // try to keep associated experimenter only
        final Experimenter expr = img.getLinkedExperimenter();
        if (expr != null) {
            for (int i = ome.sizeOfExperimenterList() - 1; i >= 0; i--) {
                final Experimenter obj = ome.getExperimenter(i);
                if (obj != expr)
                    ome.removeExperimenter(obj);
            }
        }
        else if (ome.sizeOfExperimenterList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeExperimenter(ome.getExperimenter(i));
        }

        // try to keep associated experimenter group only
        final ExperimenterGroup exprGroup = img.getLinkedExperimenterGroup();
        if (exprGroup != null) {
            for (int i = ome.sizeOfExperimenterGroupList() - 1; i >= 0; i--) {
                final ExperimenterGroup obj = ome.getExperimenterGroup(i);
                if (obj != exprGroup)
                    ome.removeExperimenterGroup(obj);
            }
        }
        else if (ome.sizeOfExperimenterGroupList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeExperimenterGroup(ome.getExperimenterGroup(i));
        }

        // try to keep associated instrument only
        final Instrument instr = img.getLinkedInstrument();
        if (instr != null) {
            for (int i = ome.sizeOfInstrumentList() - 1; i >= 0; i--) {
                final Instrument obj = ome.getInstrument(i);
                if (obj != instr)
                    ome.removeInstrument(obj);
            }
        }
        else if (ome.sizeOfInstrumentList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeInstrument(ome.getInstrument(i));
        }

        // others misc data to clean
        if (ome.sizeOfPlateList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removePlate(ome.getPlate(i));
        }
        if (ome.sizeOfProjectList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeProject(ome.getProject(i));
        }
        if (ome.sizeOfScreenList() == numSeries) {
            for (int i = numSeries - 1; i >= 0; i--)
                if (i != num)
                    ome.removeScreen(ome.getScreen(i));
        }
    }

    /**
     * Keep only the specified plane metadata.
     *
     * @param index
     *        int
     * @param img
     *        image
     */
    public static void keepSinglePlane(final Image img, final int index) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        final int numPlane = pix.sizeOfPlaneList();
        final Plane plane = getPlane(pix, index);

        // keep only the desired plane
        for (int i = numPlane - 1; i >= 0; i--) {
            if (i != index)
                pix.removePlane(pix.getPlane(i));
        }

        final Set<Object> toKeep = new HashSet<>();

        // try to keep associated annotation only
        toKeep.clear();
        for (int i = 0; i < plane.sizeOfLinkedAnnotationList(); i++)
            toKeep.add(plane.getLinkedAnnotation(i));
        if (!toKeep.isEmpty()) {
            for (int i = img.sizeOfLinkedAnnotationList() - 1; i >= 0; i--) {
                final Annotation obj = img.getLinkedAnnotation(i);
                if (!toKeep.contains(obj))
                    img.unlinkAnnotation(obj);
            }
        }
        // just assume they are indirectly linked
        else if (img.sizeOfLinkedAnnotationList() == numPlane) {
            for (int i = numPlane - 1; i >= 0; i--)
                if (i != index)
                    img.unlinkAnnotation(img.getLinkedAnnotation(i));
        }

        // clean some data
        if (pix.sizeOfBinDataList() == numPlane) {
            for (int i = numPlane - 1; i >= 0; i--)
                if (i != index)
                    pix.removeBinData(pix.getBinData(i));
        }
        if (pix.sizeOfTiffDataList() == numPlane) {
            for (int i = numPlane - 1; i >= 0; i--)
                if (i != index)
                    pix.removeTiffData(pix.getTiffData(i));
        }
    }

    /**
     * Keep only plane(s) at specified C, Z, T position from the given metadata.
     *
     * @param img
     *        image metadata to clean plane from
     * @param posT
     *        keep Plane at given T position (-1 to keep all)
     * @param posZ
     *        keep Plane at given Z position (-1 to keep all)
     * @param posC
     *        keep Plane at given C position (-1 to keep all)
     */
    public static void keepPlanes(final Image img, final int posT, final int posZ, final int posC) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        final int sizeT = OMEUtil.getValue(pix.getSizeT(), 0);
        final int sizeZ = OMEUtil.getValue(pix.getSizeZ(), 0);
        final int sizeC = OMEUtil.getValue(pix.getSizeC(), 0);

        for (int t = 0; t < sizeT; t++) {
            final boolean removeT = (posT != -1) && (posT != t);

            for (int z = 0; z < sizeZ; z++) {
                final boolean removeZ = (posZ != -1) && (posZ != z);

                for (int c = 0; c < sizeC; c++) {
                    final boolean removeC = (posC != -1) && (posC != c);

                    if (removeT || removeZ || removeC)
                        removePlane(img, t, z, c);
                }
            }
        }

        final int numPlane = pix.sizeOfPlaneList();
        for (int i = 0; i < numPlane; i++) {
            final Plane plane = pix.getPlane(i);

            // we keep only a single T/Z/C position ? --> fix them to 0
            if (posT != -1)
                plane.setTheT(OMEUtil.getNonNegativeInteger(0));
            if (posZ != -1)
                plane.setTheZ(OMEUtil.getNonNegativeInteger(0));
            if (posC != -1)
                plane.setTheC(OMEUtil.getNonNegativeInteger(0));
        }
    }

    /**
     * Keep only plane(s) at specified C, Z, T position from the given metadata.
     *
     * @param posT
     *        keep Plane at given T position (-1 to keep all)
     * @param posZ
     *        keep Plane at given Z position (-1 to keep all)
     * @param posC
     *        keep Plane at given C position (-1 to keep all)
     * @param metadata
     *        OME metadata
     * @param series
     *        int
     */
    public static void keepPlanes(final OMEXMLMetadata metadata, final int series, final int posT, final int posZ, final int posC) {
        final Image img = getSeries(metadata, series);

        if (img != null)
            keepPlanes(img, posT, posZ, posC);
    }

    /**
     * Clean plane(s) which are outside the pixel sizeC / sizeZ and sizeT.
     *
     * @param img
     *        image metadata to clean plane from
     */
    public static void cleanPlanes(final Image img) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        final int sizeT = OMEUtil.getValue(pix.getSizeT(), 0);
        final int sizeZ = OMEUtil.getValue(pix.getSizeZ(), 0);
        final int sizeC = OMEUtil.getValue(pix.getSizeC(), 0);
        if ((sizeT < 1) || (sizeZ < 1) || (sizeC < 1))
            return;

        // get allowed maximum plane
        final int allowedMaxPlaneIndex = getPlaneIndex(pix, sizeT - 1, sizeZ - 1, sizeC - 1);
        // current number of plane
        int maxPlaneIndex = pix.sizeOfPlaneList() - 1;

        // remove plan outside allowed region
        while (maxPlaneIndex > allowedMaxPlaneIndex)
            removePlane(img, maxPlaneIndex--);
    }

    /**
     * Clean TiffData packet
     *
     * @param img
     *        image metadata to clean TiffData from
     */
    public static void cleanTiffData(final Image img) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        while (pix.sizeOfTiffDataList() > 0)
            pix.removeTiffData(pix.getTiffData(pix.sizeOfTiffDataList() - 1));
    }

    /**
     * Clean BinData packet
     *
     * @param img
     *        image metadata to clean BinData from
     */
    public static void cleanBinData(final Image img) {
        final Pixels pix = img.getPixels();
        if (pix == null)
            return;

        while (pix.sizeOfBinDataList() > 0)
            pix.removeBinData(pix.getBinData(pix.sizeOfBinDataList() - 1));
    }

    /**
     * Cleanup the meta data (sometime we have empty data structure sitting there)
     *
     * @param metaData
     *        OME metadata
     */
    public static void clean(final OMEXMLMetadata metaData) {
        final OME ome = getOME(metaData);
        final StructuredAnnotations annotations = ome.getStructuredAnnotations();

        if (annotations != null) {
            for (int i = annotations.sizeOfXMLAnnotationList() - 1; i >= 0; i--) {
                final XMLAnnotation xmlAnnotation = annotations.getXMLAnnotation(i);

                if (isEmpty(xmlAnnotation))
                    annotations.removeXMLAnnotation(xmlAnnotation);
            }
        }
    }

    /**
     * Returns <code>true</code> if the specified XML annotation are empty.
     */
    public static boolean isEmpty(final XMLAnnotation xmlAnnotation) {
        return StringUtil.isEmpty(xmlAnnotation.getDescription()) && StringUtil.isEmpty(xmlAnnotation.getValue());
    }

}
