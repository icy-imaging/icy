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

package icy.image;

import icy.sequence.MetaDataUtil;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;

import java.awt.*;

/**
 * Image provider interface.<br>
 * This interface is designed to any class capable of delivering image data at different level
 * access.<br>
 * Takes an uniquely identified resource as input and returns image data at different level access.<br>
 * <br>
 * Example of possible class implementing this interface:
 * <ul>
 * <li>LOCI (Bio-Formats) image reader class.</li>
 * <li>image database interface.</li>
 * </ul>
 *
 * @author Stephane
 * @author Thomas MUSSET
 */
public interface ImageProvider {
    /**
     * Returns the image metadata in OME format (metadata provides many informations about the image).<br>
     * <br>
     * Number of series (mandatory field) :<br>
     * {@link MetaDataUtil#getNumSeries(OMEXMLMetadata)}<br>
     * Dimension (mandatory fields) :<br>
     * {@link MetaDataUtil#getSizeX(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeY(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeZ(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeT(OMEXMLMetadata, int)}<br>
     * {@link MetaDataUtil#getSizeC(OMEXMLMetadata, int)}<br>
     * Internal data type (mandatory field) :<br>
     * {@link MetaDataUtil#getDataType(OMEXMLMetadata, int)}<br>
     * Physical and time position (mandatory fields) :<br>
     * {@link MetaDataUtil#getPositionX(OMEXMLMetadata, int, int, int, int, double)}<br>
     * {@link MetaDataUtil#getPositionY(OMEXMLMetadata, int, int, int, int, double)}<br>
     * {@link MetaDataUtil#getPositionZ(OMEXMLMetadata, int, int, int, int, double)}<br>
     * {@link MetaDataUtil#getPositionT(OMEXMLMetadata, int, long)}<br>
     * {@link MetaDataUtil#getPositionTOffset(OMEXMLMetadata, int, int, int, int, double)}<br>
     * <br>
     * and many others informations depending the available metadata in the image format.
     */
    OMEXMLMetadata getOMEXMLMetaData() throws Exception;

    /**
     * @deprecated Use {@link #getOMEXMLMetaData()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    default OMEXMLMetadataImpl getMetaData() throws Exception {
        return (OMEXMLMetadataImpl) getOMEXMLMetaData();
    }

    /**
     * Returns the (optimal) tile width for the specified series of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.<br>
     * This method should returns <code>0</code> if tile loading is not supported and <code>-1</code> if any tile size
     * can be used.
     *
     * @param series Series index for multi series image (use 0 if unsure).
     * @return optimal tile width
     */
    int getTileWidth(int series) throws Exception;

    /**
     * Returns the (optimal) tile height for the specified series of the image.<br>
     * This method allow to know the best tile size to use when using the sub region image loading
     * operations.<br>
     * This method should returns <code>0</code> if tile loading is not supported and <code>-1</code> if any tile size
     * can be used.
     *
     * @param series Series index for multi series image (use 0 if unsure).
     * @return optimal tile height
     */
    int getTileHeight(int series) throws Exception;

    /**
     * Returns <code>true</code> if the given sub resolution is available from this series.<br>
     * Note that even if a sub resolution isn't available, the ImageProvider should provide it by generating it manually
     * (using the {@link IcyBufferedImageUtil#downscaleBy2(IcyBufferedImage, boolean, int)} method.<br>
     * <br>
     * Final image resolution can be calculated with: <code>image.originalResolution / (2^resolution)</code><br>
     * So <i>resolution 0</i> is the original resolution and it's always available (should always returns
     * <code>true</code>).<br>
     * <i>Resolution 1</i> is half of the original resolution<br>
     * <i>Resolution 2</i> is quarter of the original resolution<br>
     * <i>...</i>
     *
     * @param series     Series index for multi series image (use 0 if unsure).
     * @param resolution Resolution level
     */
    boolean isResolutionAvailable(int series, int resolution) throws Exception;

    /**
     * Returns the image thumbnail for the specified series of the image.<br>
     *
     * @param series Series index for multi series image (use 0 if unsure).
     * @return thumbnail image.
     */
    IcyBufferedImage getThumbnail(int series) throws Exception;

    /**
     * Returns the pixel data located for specified position of the image.<br>
     * Data is returned in form of a single dimension array, the type of this array depends from the image data type<br>
     * which can be retrieve from the metadata (see {@link OMEXMLMetadataImpl#getPixelsType(int)}
     *
     * @param series     Series index for multi series image (use 0 if unsure).
     * @param resolution Wanted resolution level for the image (use 0 if unsure).<br>
     *                   The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *                   So for instance level 0 is the default image resolution while level 1 is base image
     *                   resolution / 2 and so on...
     * @param region     The 2D region we want to retrieve (considering the original image resolution).<br>
     *                   If set to <code>null</code> then the whole image is returned.
     * @param z          Z position of the image (slice) we want retrieve data from
     * @param t          T position of the image (frame) we want retrieve data from
     * @param c          C position of the image (channel) we want retrieve (-1 is not accepted here).
     * @return native type array containing image pixel data.<br>
     * @see #isResolutionAvailable(int, int)
     */
    Object getPixels(int series, int resolution, Rectangle region, int z, int t, int c) throws Exception;

    /**
     * Returns the image located at specified position.
     *
     * @param series     Series index for multi series image (use 0 if unsure).
     * @param resolution Wanted resolution level for the image (use 0 if unsure).<br>
     *                   The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *                   So for instance level 0 is the default image resolution while level 1 is base image
     *                   resolution / 2 and so on...
     * @param region     The 2D region we want to retrieve (considering the original image resolution).<br>
     *                   If set to <code>null</code> then the whole image is returned.
     * @param z          Z position of the image (slice) we want retrieve
     * @param t          T position of the image (frame) we want retrieve
     * @param c          C position of the image (channel) we want retrieve (-1 means all channel).
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    IcyBufferedImage getImage(int series, int resolution, Rectangle region, int z, int t, int c) throws Exception;

    /**
     * Returns the image located at specified position.
     *
     * @param series     Series index for multi series image (use 0 if unsure).
     * @param resolution Wanted resolution level for the image (use 0 if unsure).<br>
     *                   The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *                   So for instance level 0 is the default image resolution while level 1 is base image
     *                   resolution / 2 and so on...
     * @param region     The 2D region we want to retrieve (considering the original image resolution).<br>
     *                   If set to <code>null</code> then the whole image is returned.
     * @param z          Z position of the image (slice) we want retrieve
     * @param t          T position of the image (frame) we want retrieve
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    IcyBufferedImage getImage(int series, int resolution, Rectangle region, int z, int t) throws Exception;

    /**
     * Returns the image located at specified position.
     *
     * @param series     Series index for multi series image (use 0 if unsure).
     * @param resolution Wanted resolution level for the image (use 0 if unsure).<br>
     *                   The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *                   So for instance level 0 is the default image resolution while level 1 is base image
     *                   resolution / 2 and so on...
     * @param z          Z position of the image (slice) we want retrieve
     * @param t          T position of the image (frame) we want retrieve
     * @param c          C position of the image (channel) we want retrieve (-1 means all channel).
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    IcyBufferedImage getImage(int series, int resolution, int z, int t, int c) throws Exception;

    /**
     * Returns the image located at specified position.
     *
     * @param series     Series index for multi series image (use 0 if unsure).
     * @param resolution Wanted resolution level for the image (use 0 if unsure).<br>
     *                   The retrieved image resolution is equal to <code>image.originalResolution / (2^resolution)</code><br>
     *                   So for instance level 0 is the default image resolution while level 1 is base image
     *                   resolution / 2 and so on...
     * @param z          Z position of the image (slice) we want retrieve
     * @param t          T position of the image (frame) we want retrieve
     * @return image
     * @see #isResolutionAvailable(int, int)
     */
    IcyBufferedImage getImage(int series, int resolution, int z, int t) throws Exception;

    /**
     * Returns the image located at specified position.
     *
     * @param series Series index for multi series image (use 0 if unsure).
     * @param z      Z position of the image (slice) we want retrieve
     * @param t      T position of the image (frame) we want retrieve
     * @return image
     */
    IcyBufferedImage getImage(int series, int z, int t) throws Exception;

    /**
     * Returns the image located at specified position.
     *
     * @param z Z position of the image (slice) we want retrieve
     * @param t T position of the image (frame) we want retrieve
     * @return image
     */
    IcyBufferedImage getImage(int z, int t) throws Exception;
}
