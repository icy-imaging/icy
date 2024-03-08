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

package icy.resource.icon;

import icy.gui.util.LookAndFeelUtil;
import icy.image.ImageUtil;
import icy.system.logging.IcyLogger;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;

/**
 * Build an {@link ImageIcon} from SVG.
 * @author Thomas Musset
 */
public final class IcySVGImageIcon extends ImageIcon {
    /**
     * The BufferedImage generated from the SVG document.
     */
    private BufferedImage bufferedImage;
    private BufferedImage cache;

    private final boolean colored;

    /**
     * The width of the rendered image.
     */
    private int width;

    /**
     * The height of the rendered image.
     */
    private int height;

    private final @Nullable Color customColor;

    /**
     * Create a new SVGIcon object with specified foreground color and custom size.<br>
     * Internal use only.
     */
    private IcySVGImageIcon(final @Nullable URI uri, final @Nullable Color color, final int size, final boolean colored) {
        this.colored = (color == null) && colored;
        this.customColor = color;

        if (uri == null) {
            IcyLogger.warn(IcySVGImageIcon.class, "Unable to draw SVG icon because URI is null");
            bufferedImage = null;
            cache = null;
            return;
        }

        try {
            generateBufferedImage(new TranscoderInput(uri.toASCIIString()), size, size);
        }
        catch (final TranscoderException e) {
            IcyLogger.warn(IcySVGImageIcon.class, e, "Unable to load SVG icon.");
        }
    }

    /**
     * Create a new SVGIcon object with specified foreground color and custom size.
     */
    public IcySVGImageIcon(final @NotNull SVGIcon icon, final @NotNull Color color, final int size) {
        this(icon.getURI(), color, size, icon.isColored());
    }

    /**
     * Create a new SVGIcon object with custom size.
     */
    public IcySVGImageIcon(final @NotNull SVGIcon icon, final int size) {
        this(icon.getURI(), null, size, icon.isColored());
    }

    /**
     * Create a new SVGIcon object with specified foreground color and default size.
     */
    public IcySVGImageIcon(final @NotNull SVGIcon icon, final @NotNull Color color) {
        this(icon.getURI(), color, LookAndFeelUtil.getDefaultIconSize(), icon.isColored());
    }

    /**
     * Create a new SVGIcon object with custom size.
     */
    public IcySVGImageIcon(final @NotNull SVGIcon icon) {
        this(icon.getURI(), null, LookAndFeelUtil.getDefaultIconSize(), icon.isColored());
    }

    /**
     * Generate the BufferedImage.
     */
    private void generateBufferedImage(final TranscoderInput in, final int w, final int h) throws TranscoderException {
        final BufferedImageTranscoder t = new BufferedImageTranscoder();
        if (w != 0 && h != 0)
            t.setDimensions(w, h);

        t.transcode(in, null);
        if (colored) {
            cache = null;
            bufferedImage = t.getBufferedImage();
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
        }
        else {
            cache = t.getBufferedImage();
            bufferedImage = new BufferedImage(cache.getWidth(), cache.getHeight(), BufferedImage.TYPE_INT_ARGB);
            width = cache.getWidth();
            height = cache.getHeight();
        }
    }

    /**
     * Returns this icon's <code>Image</code>.
     *
     * @return the <code>Image</code> object for this <code>ImageIcon</code>
     */
    @Override
    public Image getImage() {
        if (cache == null && bufferedImage == null)
            return super.getImage();

        if (!colored) {
            if (customColor == null)
                ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, Color.BLACK);
            else
                ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, customColor);
        }

        return bufferedImage;
    }

    /**
     * Draw the icon at the specified location.  Icon implementations
     * may use the Component argument to get properties useful for
     * painting, e.g. the foreground or background color.
     *
     * @param c a {@code Component} to get properties useful for painting
     * @param g the graphics context
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        if (cache == null && bufferedImage == null)
            return;

        if (!colored) {
            if (customColor == null)
                ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, c.getForeground());
            else
                ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, customColor);
        }

        g.drawImage(bufferedImage, x, y, null);
    }

    /**
     * Returns the icon's width.
     *
     * @return an int specifying the fixed width of the icon.
     */
    @Override
    public int getIconWidth() {
        return width;
    }

    /**
     * Returns the icon's height.
     *
     * @return an int specifying the fixed height of the icon.
     */
    @Override
    public int getIconHeight() {
        return height;
    }

    /**
     * A transcoder that generates a BufferedImage.
     */
    private static final class BufferedImageTranscoder extends ImageTranscoder {
        /**
         * The BufferedImage generated from the SVG document.
         */
        private BufferedImage bufferedImage;

        /**
         * Creates a new ARGB image with the specified dimension.
         * @param width the image width in pixels
         * @param height the image height in pixels
         */
        @Override
        public BufferedImage createImage(final int width, final int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Writes the specified image to the specified output.
         * @param img the image to write
         * @param output the output where to store the image
         */
        @Override
        public void writeImage(final BufferedImage img, final TranscoderOutput output) {
            bufferedImage = img;
        }

        /**
         * Returns the BufferedImage generated from the SVG document.
         */
        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        /**
         * Set the dimensions to be used for the image.
         */
        public void setDimensions(final int w, final int h) {
            hints.put(KEY_WIDTH, (float) w);
            hints.put(KEY_HEIGHT, (float) h);
        }
    }
}
