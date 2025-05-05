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

package org.bioimageanalysis.icy.gui.component.icon;

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.model.image.ImageUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.net.URI;

/**
 * Build an {@link Icon} from SVG.
 * @author Thomas Musset
 */
public final class IcySVGIcon extends UserAgentAdapter implements Icon {
    /**
     * The BufferedImage generated from the SVG document.
     */
    private BufferedImage bufferedImage;
    private BufferedImage cache;

    /**
     * The width of the rendered image.
     */
    private int width;

    /**
     * The height of the rendered image.
     */
    private int height;

    private final boolean colored;

    private final @Nullable Color customColor;


    private final @Nullable LookAndFeelUtil.ColorType colorType;

    /**
     * Main constructor, internal only.
     */
    private IcySVGIcon(final @Nullable URI uri, final @Nullable LookAndFeelUtil.ColorType colorType, final int width, final int height, final boolean colored) {
        this.colorType = colorType;
        this.colored = colored;
        this.customColor = null;

        if (uri == null) {
            IcyLogger.warn(IcySVGIcon.class, "Unable to draw SVG icon because URI is null");
            bufferedImage = null;
            cache = null;
            return;
        }

        try {
            generateBufferedImage(new TranscoderInput(uri.toASCIIString()), width, height);
        }
        catch (final TranscoderException e) {
            IcyLogger.warn(IcySVGIcon.class, e, "Unable to load SVG icon.");
        }
    }

    /**
     * Create a new SVGIcon object with automatic foreground color and custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final @NotNull LookAndFeelUtil.ColorType colorType, final int width, final int height) {
        this(icon.getURI(), colorType, width, height, icon.isColored());
    }

    /**
     * Create a new SVGIcon object with automatic foreground color and custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final @NotNull LookAndFeelUtil.ColorType colorType, final int size) {
        this(icon.getURI(), colorType, size, size, icon.isColored());
    }

    /**
     * Create a new SVGIcon object with automatic foreground color and default size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final @NotNull LookAndFeelUtil.ColorType colorType) {
        this(icon.getURI(), colorType, LookAndFeelUtil.getDefaultIconSize(), LookAndFeelUtil.getDefaultIconSize(), icon.isColored());
    }

    /**
     * Create a new SVGIcon object with custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final int width, final int height) {
        this(icon.getURI(), null, width, height, icon.isColored());
    }

    /**
     * Create a new SVGIcon object with custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final int size) {
        this(icon.getURI(), null, size, size, icon.isColored());
    }

    /**
     * Create a new SVGIcon object with custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon) {
        this(icon.getURI(), null, LookAndFeelUtil.getDefaultIconSize(), LookAndFeelUtil.getDefaultIconSize(), icon.isColored());
    }

    /**
     * Create a new SVGIcon object with specified foreground color and custom size.<br>
     * Internal use only.
     */
    private IcySVGIcon(final @Nullable URI uri, final @NotNull Color color, final int width, final int height) {
        this.colorType = null;
        this.colored = false;
        this.customColor = color;

        if (uri == null) {
            IcyLogger.warn(IcySVGIcon.class, "Unable to draw SVG icon because URI is null");
            bufferedImage = null;
            cache = null;
            return;
        }

        try {
            generateBufferedImage(new TranscoderInput(uri.toASCIIString()), width, height);
        }
        catch (final TranscoderException e) {
            IcyLogger.warn(IcySVGIcon.class, e, "Unable to load SVG icon.");
        }
    }

    /**
     * Create a new SVGIcon object with specified foreground color and custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final @NotNull Color color, final int width, final int height) {
        this(icon.getURI(), color, width, height);
    }

    /**
     * Create a new SVGIcon object with specified foreground color and custom size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final @NotNull Color color, final int size) {
        this(icon.getURI(), color, size, size);
    }

    /**
     * Create a new SVGIcon object with specified foreground color and default size.
     */
    public IcySVGIcon(final @NotNull SVGIcon icon, final @NotNull Color color) {
        this(icon.getURI(), color, LookAndFeelUtil.getDefaultIconSize(), LookAndFeelUtil.getDefaultIconSize());
    }

    /**
     * Generate the BufferedImage.
     * @param in transcoder input.
     * @param w width.
     * @param h height.
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
     * Returns the default size of this user agent.
     */
    @Override
    public Dimension2D getViewportSize() {
        return new Dimension(width, height);
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
            if (colorType == null) {
                if (customColor == null)
                    ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, c.getForeground());
                else
                    ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, customColor);
            }
            else
                ImageUtil.paintColorImageFromAlphaImage(cache, bufferedImage, LookAndFeelUtil.getUIColor(colorType));
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
