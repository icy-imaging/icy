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

package icy.plugin.abstract_;

import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ProgressListener;
import icy.image.AbstractImageProvider;
import icy.image.IcyBufferedImage;
import icy.plugin.interface_.PluginNoEDTConstructor;
import icy.sequence.SequenceIdImporter;
import ome.xml.meta.OMEXMLMetadata;

import java.awt.*;
import java.io.IOException;

/**
 * Plugin specialized for Sequence id import operation (see the {@link SequenceIdImporter} interface)
 *
 * @author Stephane
 * @author Thomas MUSSET
 * @see PluginImporter
 * @see PluginFileImporter
 * @see PluginSequenceFileImporter
 * @see PluginSequenceImporter
 */
public abstract class PluginSequenceIdImporter extends Plugin implements SequenceIdImporter, PluginNoEDTConstructor {
    // default helper
    protected class InternalSequenceIdImporterHelper extends AbstractImageProvider implements SequenceIdImporter {
        @Override
        public String getOpened() {
            return PluginSequenceIdImporter.this.getOpened();
        }

        @Override
        public boolean open(final String id, final int flags) throws UnsupportedFormatException, IOException, InterruptedException {
            return PluginSequenceIdImporter.this.open(id, flags);
        }

        @Override
        public void close() throws Exception {
            PluginSequenceIdImporter.this.close();
        }

        @Override
        public IcyBufferedImage getImage(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c) throws Exception {
            return PluginSequenceIdImporter.this.getImage(series, resolution, rectangle, z, t, c);
        }
    }

    protected final InternalSequenceIdImporterHelper interfaceHelper;

    public PluginSequenceIdImporter() {
        super();

        interfaceHelper = new InternalSequenceIdImporterHelper();
    }

    // default implementation as ImageProvider interface changed
    @Override
    public OMEXMLMetadata getOMEXMLMetaData() throws Exception {
        return interfaceHelper.getOMEXMLMetaData();
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileWidth(final int series) throws Exception {
        return interfaceHelper.getTileWidth(series);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public int getTileHeight(final int series) throws Exception {
        return interfaceHelper.getTileHeight(series);
    }

    // default implementation, override it if you need specific value for faster tile access
    @Override
    public boolean isResolutionAvailable(final int series, final int resolution) {
        return interfaceHelper.isResolutionAvailable(series, resolution);
    }

    // default implementation
    @Override
    public IcyBufferedImage getThumbnail(final int series) throws Exception {
        return interfaceHelper.getThumbnail(series);
    }

    // default implementation: use the getImage(..) method then return data.
    // It should be the opposite side for performance reason, override this method if possible
    @Override
    public Object getPixels(final int series, final int resolution, final Rectangle rectangle, final int z, final int t, final int c) throws Exception {
        return interfaceHelper.getPixels(series, resolution, rectangle, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(final int series, final int resolution, final Rectangle rectangle, final int z, final int t) throws Exception {
        return interfaceHelper.getImage(series, resolution, rectangle, z, t);
    }

    // default implementation using the region getImage(..) method, better to override
    @Override
    public IcyBufferedImage getImage(final int series, final int resolution, final int z, final int t, final int c) throws Exception {
        return interfaceHelper.getImage(series, resolution, z, t, c);
    }

    @Override
    public IcyBufferedImage getImage(final int series, final int resolution, final int z, final int t) throws Exception {
        return interfaceHelper.getImage(series, resolution, z, t);
    }

    @Override
    public IcyBufferedImage getImage(final int series, final int z, final int t) throws Exception {
        return interfaceHelper.getImage(series, z, t);
    }

    @Override
    public IcyBufferedImage getImage(final int z, final int t) throws Exception {
        return interfaceHelper.getImage(z, t);
    }

    /**
     * See {@link AbstractImageProvider#getPixelsByTile(int, int, Rectangle, int, int, int, int, int, ProgressListener)}
     */
    public Object getPixelsByTile(final int series, final int resolution, final Rectangle region, final int z, final int t, final int c, final int tileW, final int tileH, final ProgressListener listener) throws Exception {
        return interfaceHelper.getPixelsByTile(series, resolution, region, z, t, c, tileW, tileH, listener);
    }

    /**
     * See {@link AbstractImageProvider#getResolutionFactor(int, int)}
     */
    public int getResolutionFactor(final int series, final int wantedSize) throws Exception {
        return interfaceHelper.getResolutionFactor(series, wantedSize);
    }
}
