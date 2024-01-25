/*
 * Copyright 2010-2015 Institut Pasteur.
 *
 * This file is part of Icy.
 *
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
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */

package icy.image;

import icy.gui.util.FontUtil;
import icy.network.URLUtil;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;
import icy.util.GraphicsUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Image utilities class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ImageUtil {
    public static String getImageTypeString(final int type) {
        return switch (type) {
            case BufferedImage.TYPE_CUSTOM -> "TYPE_CUSTOM";
            case BufferedImage.TYPE_INT_RGB -> "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB -> "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE -> "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR -> "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR -> "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR -> "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE -> "TYPE_4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_USHORT_565_RGB -> "TYPE_USHORT_565_RGB";
            case BufferedImage.TYPE_USHORT_555_RGB -> "TYPE_USHORT_555_RGB";
            case BufferedImage.TYPE_BYTE_GRAY -> "TYPE_BYTE_GRAY";
            case BufferedImage.TYPE_USHORT_GRAY -> "TYPE_USHORT_GRAY";
            case BufferedImage.TYPE_BYTE_BINARY -> "TYPE_BYTE_BINARY";
            case BufferedImage.TYPE_BYTE_INDEXED -> "TYPE_BYTE_INDEXED";
            default -> "UNKNOWN TYPE";
        };
    }

    public static String getTransparencyString(final int transparency) {
        return switch (transparency) {
            case Transparency.OPAQUE -> "OPAQUE";
            case Transparency.BITMASK -> "BITMASK";
            case Transparency.TRANSLUCENT -> "TRANSLUCENT";
            default -> "UNKNOWN TRANSPARENCY";
        };
    }

    /**
     * Wait for dimension information of specified image being loaded.
     *
     * @param image
     *        image we are waiting informations for.
     */
    public static void waitImageReady(final Image image) {
        if (image != null) {
            final long st = System.currentTimeMillis();

            // wait 2 seconds max
            while ((image.getWidth(null) == -1) && ((System.currentTimeMillis() - st) < 2000))
                ThreadUtil.sleep(1);
        }
    }

    /**
     * Create a 8 bits indexed buffered image from specified <code>IndexColorModel</code><br>
     * and byte array data.
     */
    public static BufferedImage createIndexedImage(final int w, final int h, final IndexColorModel cm, final byte[] data) {
        final WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(data, w * h, 0), w, h, w, 1, new int[]{0}, null);

        return new BufferedImage(cm, raster, false, null);
    }

    /**
     * Load an image from specified path
     */
    public static BufferedImage load(final String path, final boolean displayError) {
        return load(URLUtil.getURL(path), displayError);
    }

    /**
     * Load an image from specified path
     */
    public static BufferedImage load(final String path) {
        return load(path, true);
    }

    /**
     * Load an image from specified url
     */
    public static BufferedImage load(final URL url, final boolean displayError) {
        if (url != null) {
            try {
                return ImageIO.read(url);
            }
            // important to catch Exception as sometime we got NPE here (inflater closed)
            catch (final Exception e) {
                if (displayError)
                    IcyLogger.error(ImageUtil.class, e, "Can't load image from " + url);
            }
        }

        return null;
    }

    /**
     * Asynchronously load an image from specified url.<br>
     * Use {@link #waitImageReady(Image)} to know if width and height property
     */
    public static Image loadAsync(final URL url) {
        return Toolkit.getDefaultToolkit().createImage(url);
    }

    /**
     * Asynchronously load an image from specified path.<br>
     * Use {@link #waitImageReady(Image)} to know if width and height property
     */
    public static Image loadAsync(final String path) {
        return Toolkit.getDefaultToolkit().createImage(path);
    }

    /**
     * Load an image from specified url
     */
    public static BufferedImage load(final URL url) {
        return load(url, true);
    }

    /**
     * Load an image from specified file
     */
    public static BufferedImage load(final File file, final boolean displayError) {
        if (file != null) {
            try {
                return ImageIO.read(file);
            }
            catch (final IOException e) {
                if (displayError)
                    IcyLogger.error(ImageUtil.class, e, "Can't load image from " + file);
            }
        }

        return null;
    }

    /**
     * Load an image from specified file
     */
    public static BufferedImage load(final File file) {
        return load(file, true);
    }

    /**
     * Load an image from specified InputStream
     */
    public static BufferedImage load(final InputStream input, final boolean displayError) {
        if (input != null) {
            try {
                return ImageIO.read(input);
            }
            catch (final Exception e) {
                if (displayError)
                    IcyLogger.error(ImageUtil.class, e, "Can't load image from stream " + input);
            }
        }

        return null;
    }

    /**
     * Load an image from specified InputStream
     */
    public static BufferedImage load(final InputStream input) {
        return load(input, true);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(String, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final String path, final boolean displayError) {
        return load(path, displayError);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(String)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final String path) {
        return load(path);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(URL, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final URL url, final boolean displayError) {
        return load(url, displayError);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(URL)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static Image loadImage(final URL url) {
        return load(url);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(File, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final File file, final boolean displayError) {
        return load(file, displayError);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(File)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final File file) {
        return load(file);
    }

    /**
     * @deprecated Use {@link ImageUtil#load(InputStream, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final InputStream input, final boolean displayError) {
        return load(input, displayError);

    }

    /**
     * @deprecated Use {@link ImageUtil#load(InputStream)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage loadImage(final InputStream input) {
        return load(input);

    }

    /**
     * Save an image to specified path in specified format
     */
    public static boolean save(final RenderedImage image, final String format, final String path) {
        if (path != null) {
            try {
                return ImageIO.write(image, format, new FileOutputStream(path));
            }
            catch (final IOException e) {
                IcyLogger.error(ImageUtil.class, e, "Can't save image to " + path);
            }
        }

        return false;
    }

    /**
     * Save an image to specified file in specified format
     */
    public static boolean save(final RenderedImage image, final String format, final File file) {
        if (file != null) {
            try {
                return ImageIO.write(image, format, file);
            }
            catch (final IOException e) {
                IcyLogger.error(ImageUtil.class, e, "Can't save image to " + file);
            }
        }

        return false;
    }

    /**
     * @deprecated Use {@link ImageUtil#save(RenderedImage, String, String)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static boolean saveImage(final RenderedImage image, final String format, final String path) {
        return save(image, format, path);
    }

    /**
     * @deprecated Use {@link ImageUtil#save(RenderedImage, String, File)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static boolean saveImage(final RenderedImage image, final String format, final File file) {
        return save(image, format, file);
    }

    /**
     * Return a RenderedImage from the given Image object.
     */
    public static RenderedImage toRenderedImage(final Image image) {
        return toBufferedImage(image);
    }

    /**
     * Return a ARGB BufferedImage from the given Image object.
     * If the image is already a BufferedImage image then it's directly returned
     */
    public static BufferedImage toBufferedImage(final Image image) {
        if (image instanceof BufferedImage)
            return (BufferedImage) image;

        // be sure image data are ready
        waitImageReady(image);
        final BufferedImage bufImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = bufImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufImage;
    }

    /**
     * Scale an image with specified size.
     */
    public static BufferedImage scale(final Image image, final int width, final int height) {
        if (image != null) {
            final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = result.createGraphics();

            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();

            return result;
        }

        return null;
    }

    /**
     * Scale an image with specified size (try to keep best quality).
     */
    public static BufferedImage scaleQuality(final Image image, final int width, final int height) {
        if (image != null) {
            Image current = image;

            // be sure image data are ready
            waitImageReady(image);

            int w = image.getWidth(null);
            int h = image.getHeight(null);

            do {
                if (w > width) {
                    w /= 2;
                    if (w < width)
                        w = width;
                }
                else
                    w = width;

                if (h > height) {
                    h /= 2;
                    if (h < height)
                        h = height;
                }
                else
                    h = height;

                final BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D g = result.createGraphics();

                g.setComposite(AlphaComposite.Src);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(current, 0, 0, w, h, null);

                g.dispose();

                current = result;
            }
            while (w != width || h != height);

            return (BufferedImage) current;
        }

        return null;
    }

    /**
     * Convert an image to a BufferedImage.<br>
     * If <code>out</code>is null, by default a <code>BufferedImage.TYPE_INT_ARGB</code> is created.
     */
    public static BufferedImage convert(final Image in, final BufferedImage out) {
        final BufferedImage result;

        // be sure image data are ready
        waitImageReady(in);

        // no output type specified ? use ARGB
        result = Objects.requireNonNullElseGet(out, () -> new BufferedImage(in.getWidth(null), in.getHeight(null), BufferedImage.TYPE_INT_ARGB));

        final Graphics g = result.getGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();

        return result;
    }

    /**
     * Returns <code>true</code> if the specified image is a grayscale image whatever is the image
     * type (GRAY, RGB, ARGB...)
     */
    public static boolean isGray(final BufferedImage image) {
        if (image == null)
            return false;

        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY)
            return true;
        if (image.getType() == BufferedImage.TYPE_USHORT_GRAY)
            return true;

        final int[] rgbArray = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

        for (final int value : rgbArray) {
            final int c0 = (value/* >> 0*/) & 0xFF;
            final int c1 = (value >> 8) & 0xFF;
            if (c0 != c1)
                return false;

            final int c2 = (value >> 16) & 0xFF;
            if (c0 != c2)
                return false;
        }

        return true;
    }

    /**
     * Convert an image to grey image (<code>BufferedImage.TYPE_BYTE_GRAY</code>).
     */
    public static BufferedImage toGray(final Image image) {
        if (image != null) {
            // be sure image data are ready
            waitImageReady(image);
            return convert(image, new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_BYTE_GRAY));
        }

        return null;
    }

    /**
     * Convert an image to RGB image (<code>BufferedImage.TYPE_INT_RGB</code>).
     */
    public static BufferedImage toRGBImage(final Image image) {
        if (image != null) {
            // be sure image data are ready
            waitImageReady(image);
            return convert(image, new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB));
        }

        return null;
    }

    /**
     * Convert an image to ARGB image (<code>BufferedImage.TYPE_INT_ARGB</code>).
     */
    public static BufferedImage toARGBImage(final Image image) {
        if (image != null) {
            // be sure image data are ready
            waitImageReady(image);
            return convert(image, new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB));
        }

        return null;
    }

    /**
     * @deprecated Use {@link ImageUtil#scale(Image, int, int)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage scaleImage(final Image image, final int width, final int height) {
        return scale(image, width, height);
    }

    /**
     * @deprecated Use {@link ImageUtil#scaleQuality(Image, int, int)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage scaleImageQuality(final Image image, final int width, final int height) {
        return scaleQuality(image, width, height);
    }

    /**
     * @deprecated Use {@link ImageUtil#convert(Image, BufferedImage)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage convertImage(final Image in, final BufferedImage out) {
        return convert(in, out);
    }

    /**
     * @deprecated Use {@link ImageUtil#toGray(Image)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static BufferedImage toGrayImage(final Image image) {
        return toGray(image);
    }

    /**
     * Create a copy of the input image.<br>
     * Result is always a <code>BufferedImage.TYPE_INT_ARGB</code> type image.
     */
    public static BufferedImage getCopy(final Image in) {
        return convert(in, null);
    }

    /**
     * Return true if image has the same size
     */
    public static boolean sameSize(final BufferedImage im1, final BufferedImage im2) {
        return (im1.getWidth() == im2.getWidth()) && (im1.getHeight() == im2.getHeight());
    }

    /**
     * Get the list of tiles to cover the given XY region.<br>
     * Note that the resulting tiles surface may be larger than input region as we enforce using specified tile size / position to cover the whole region.
     *
     * @param region
     *        the XY region to cover
     * @param tileW
     *        tile width
     * @param tileH
     *        tile height
     */
    public static List<Rectangle> getTileList(final Rectangle region, final int tileW, final int tileH) {
        final List<Rectangle> result = new ArrayList<>();

        if ((tileW <= 0) || (tileH <= 0) || region.isEmpty())
            return result;

        final int startX;
        final int startY;
        final int endX;
        final int endY;

        startX = (region.x / tileW) * tileW;
        startY = (region.y / tileH) * tileH;
        endX = ((region.x + (region.width - 1)) / tileW) * tileW;
        endY = ((region.y + (region.height - 1)) / tileH) * tileH;

        for (int y = startY; y <= endY; y += tileH)
            for (int x = startX; x <= endX; x += tileW)
                result.add(new Rectangle(x, y, tileW, tileH));

        return result;
    }

    /**
     * Get the list of tiles to fill the given XY plan size.<br>
     * Note that the resulting tiles surface may be larger than input region as we enforce using specified tile size / position to cover the whole region.
     *
     * @param sizeX
     *        plan sizeX
     * @param sizeY
     *        plan sizeY
     * @param tileW
     *        tile width
     * @param tileH
     *        tile height
     */
    public static List<Rectangle> getTileList(final int sizeX, final int sizeY, final int tileW, final int tileH) {
        return getTileList(new Rectangle(0, 0, sizeX, sizeY), tileW, tileH);
    }

    /**
     * Apply simple color filter with specified alpha factor to the image
     */
    public static void applyColorFilter(final Image image, final Color color, final float alpha) {
        if (image != null) {
            // be sure image data are ready
            waitImageReady(image);

            // should be Graphics2D compatible
            final Graphics2D g = (Graphics2D) image.getGraphics();
            final Rectangle rect = new Rectangle(image.getWidth(null), image.getHeight(null));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(color);
            g.fill(rect);
            g.dispose();
        }
    }

    /**
     * Return an image which contains specified color depending original alpha intensity image
     */
    public static Image getColorImageFromAlphaImage(final Image alphaImage, final Color color) {
        return paintColorImageFromAlphaImage(alphaImage, null, color);
    }

    /**
     * Paint the specified color in 'out' image depending original alpha intensity from 'alphaImage'
     */
    public static Image paintColorImageFromAlphaImage(final Image alphaImage, final Image out, final Color color) {
        if (alphaImage == null)
            return null;

        final int w;
        final int h;
        final Image result;

        if (out == null) {
            // be sure image data are ready
            waitImageReady(alphaImage);

            w = alphaImage.getWidth(null);
            h = alphaImage.getHeight(null);

            if ((w == -1) || (h == -1))
                return null;

            result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        else {
            // be sure image data are ready
            waitImageReady(out);

            w = out.getWidth(null);
            h = out.getHeight(null);

            if ((w == -1) || (h == -1))
                return null;

            result = out;
        }

        final Graphics2D g = (Graphics2D) result.getGraphics();

        // clear
        g.setBackground(new Color(0x00000000, true));
        g.clearRect(0, 0, w, h);

        // be sure image data are ready
        waitImageReady(alphaImage);
        // draw icon
        g.drawImage(alphaImage, 0, 0, null);

        // set fill color
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, w, h);

        g.dispose();

        return result;
    }

    /**
     * Draw text in the specified image with specified parameters.<br>
     */
    public static void drawText(final Image image, final String text, final float x, final float y, final int size, final Color color) {
        final Graphics2D g = (Graphics2D) image.getGraphics();

        // prepare setting
        g.setColor(color);
        g.setFont(FontUtil.setSize(g.getFont(), size));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw icon
        g.drawString(text, x, y);

        g.dispose();
    }

    /**
     * Draw text at top right in the specified image with specified parameters.<br>
     */
    public static void drawTextTopRight(final Image image, final String text, final int size, final boolean bold, final Color color) {
        final Graphics2D g = (Graphics2D) image.getGraphics();

        // prepare setting
        g.setColor(color);
        g.setFont(FontUtil.setSize(g.getFont(), size));
        if (bold)
            g.setFont(FontUtil.setStyle(g.getFont(), Font.BOLD));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // get string bounds
        final Rectangle2D bounds = GraphicsUtil.getStringBounds(g, text);

        // be sure image data are ready
        waitImageReady(image);

        final float w = image.getWidth(null);

        // draw text
        g.drawString(text, w - ((float) bounds.getWidth()), 0 - (float) bounds.getY());

        g.dispose();
    }
}
