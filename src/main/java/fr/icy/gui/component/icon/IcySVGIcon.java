/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.gui.component.icon;

import fr.icy.gui.LookAndFeelUtil;
import fr.icy.shared.logging.AnsiColor;
import fr.icy.shared.logging.CustomLevel;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Icon from SVG content.
 *
 * @author Thomas Musset
 */
public final class IcySVGIcon implements Icon {
    private static final Logger LOGGER = Logger.getLogger(IcySVGIcon.class.getName());

    public static Badge NONE = null;
    public static Badge INFO = new Badge("/icon/svg/mono/info_alt.svg", new Color(0, 128, 255));
    public static Badge QUESTION = null;
    public static Badge WARNING = new Badge("/icon/svg/mono/warning_alt.svg", new Color(255, 165, 0));
    public static Badge ERROR = new Badge("/icon/svg/mono/error_alt.svg", new Color(200, 20, 0));
    public static Badge ONE = null;
    public static Badge TWO = null;
    public static Badge THREE = null;
    public static Badge FOUR = null;
    public static Badge FIVE = null;
    public static Badge SIX = null;
    public static Badge SEVEN = null;
    public static Badge EIGHT = null;
    public static Badge NINE = null;
    public static Badge MORE = null;

    private SVGDocument svgDocument;
    private final BridgeContext ctx;
    private final GraphicsNode svgNode;
    private final int width;
    private final int height;

    private final LookAndFeelUtil.@Nullable ColorType colorType;

    private @Nullable Badge badge;

    private IcySVGIcon(final String svgContent, final int width, final int height, final @Nullable Color color, final LookAndFeelUtil.@Nullable ColorType colorType, final @Nullable Badge badge) {
        this.width = width;
        this.height = height;
        this.colorType = colorType;
        this.badge = badge;

        // load SVG document
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        //svgDocument = null;
        try {
            svgDocument = factory.createSVGDocument(null, new StringReader(svgContent));
        }
        catch (final IOException e) {
            // Should not occur
            LOGGER.log(Level.SEVERE, "SVG could not be read.", e);
        }

        if (svgDocument != null) {
            // Build the GraphicsNode
            if (color != null)
                recolorSvg(svgDocument, color);
            else if (colorType != null) {
                final Color ui = LookAndFeelUtil.getUIColor(colorType);
                if (ui != null)
                    recolorSvg(svgDocument, ui);
            }
            final UserAgentAdapter userAgent = new UserAgentAdapter();
            ctx = new BridgeContext(userAgent);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            final GVTBuilder builder = new GVTBuilder();
            svgNode = builder.build(ctx, svgDocument);
        }
        else {
            ctx = null;
            svgNode = null;
        }
    }

    private static void recolorSvg(@NonNull final SVGDocument doc, final @Nullable String oldValue, @NonNull final String newValue, final @MagicConstant(stringValues = {"fill", "stroke", "stop-color"}) String @NonNull ... attributes) {
        final NodeList elements = doc.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            final Element el = (Element) elements.item(i);
            for (final String attribute : attributes) {
                if (el.hasAttribute(attribute)) {
                    if (oldValue == null) {
                        if (!el.getAttribute(attribute).startsWith("url(") && !el.getAttribute(attribute).equalsIgnoreCase("none"))
                            el.setAttribute(attribute, newValue);
                    }
                    else if (el.getAttribute(attribute).equalsIgnoreCase(oldValue))
                        el.setAttribute(attribute, newValue);
                }
            }
        }
    }

    private static void recolorSvg(@NonNull final SVGDocument doc, @Nullable final Color color) {
        final String hex;
        if (color == null)
            hex = "none";
        else
            hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        recolorSvg(doc, null, hex, "fill", "stroke", "stop-color");
    }

    @Contract("_, _, _ -> new")
    static @NonNull IcySVGIcon fromBytes(final byte @NonNull [] data, final int width, final int height) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, null, null, NONE);
    }

    @Contract("_, _, _, _ -> new")
    static @NonNull IcySVGIcon fromBytes(final byte @NonNull [] data, final int width, final int height, final @Nullable Badge badge) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, null, null, badge);
    }

    @Contract("_, _, _, _ -> new")
    static @NonNull IcySVGIcon fromBytes(final byte @NonNull [] data, final int width, final int height, final @NonNull Color color) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, color, null, NONE);
    }

    @Contract("_, _, _, _, _ -> new")
    static @NonNull IcySVGIcon fromBytes(final byte @NonNull [] data, final int width, final int height, final @NonNull Color color, final @Nullable Badge badge) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, color, null, badge);
    }

    @Contract("_, _, _, _ -> new")
    static @NonNull IcySVGIcon fromBytes(final byte @NonNull [] data, final int width, final int height, final LookAndFeelUtil.@NonNull ColorType colorType) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, null, colorType, NONE);
    }

    @Contract("_, _, _, _, _ -> new")
    static @NonNull IcySVGIcon fromBytes(final byte @NonNull [] data, final int width, final int height, final LookAndFeelUtil.@NonNull ColorType colorType, final @Nullable Badge badge) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, null, colorType, badge);
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
        if (colorType != null) {
            final Color ui = LookAndFeelUtil.getUIColor(colorType);
            if (ui != null)
                recolorSvg(svgDocument, ui);
        }

        if (svgNode == null || ctx == null)
            return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);

        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));

        // Get SVG original size
        final Rectangle bounds = new Rectangle(0, 0, (int) ctx.getDocumentSize().getWidth(), (int) ctx.getDocumentSize().getHeight());

        final double scaleX = (double) width / bounds.getWidth();
        final double scaleY = (double) height / bounds.getHeight();

        if (LOGGER.isLoggable(CustomLevel.TRACE)) {
            LOGGER.log(CustomLevel.TRACE, String.format("Paint XY: %d, %d", x, y));
            LOGGER.log(CustomLevel.TRACE, String.format("Paint size: %d, %d", width, height));
            LOGGER.log(CustomLevel.TRACE, String.format("SVG ID: %s", AnsiColor.BRIGHT_GREEN.apply(svgDocument.getDocumentElement().getAttribute("id"))));
            LOGGER.log(CustomLevel.TRACE, String.format("SVG bounds: %s", bounds));
            LOGGER.log(CustomLevel.TRACE, String.format("SVG scale: %f, %f", scaleX, scaleY));
        }

        g2d.scale(scaleX, scaleY);
        svgNode.paint(g2d);
        g2d.dispose();

        if (badge != null) {
            final double x2 = width * .5d;
            final double y2 = height * .5d;
            final double w2 = width * .7d;
            final double h2 = height * .7d;

            final Rectangle bounds2 = new Rectangle(0, 0, (int) badge.getDocumentSize().getWidth(), (int) badge.getDocumentSize().getHeight());
            if (bounds2.getWidth() == 0 || bounds2.getHeight() == 0) { // Shouldn't appear, but just in case
                g2d.dispose();
                return;
            }
            final double scaleX2 = w2 / bounds2.getWidth();
            final double scaleY2 = h2 / bounds2.getHeight();

            if (LOGGER.isLoggable(CustomLevel.TRACE)) {
                LOGGER.log(CustomLevel.TRACE, String.format("Warning X, Y: %f, %f", x2, y2));
                LOGGER.log(CustomLevel.TRACE, String.format("Warning W, H: %f, %f", w2, h2));
                LOGGER.log(CustomLevel.TRACE, String.format("Warning bounds: %s", bounds2));
                LOGGER.log(CustomLevel.TRACE, String.format("Warning scale: %f, %f", scaleX2, scaleY2));
            }

            g2d = (Graphics2D) g.create();
            g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));

            g2d.translate(x2 + x, y2 + y);
            g2d.scale(scaleX2, scaleY2);
            badge.paint(g2d);
            g2d.dispose();
        }
    }

    /**
     * Returns the icon's width.
     *
     * @return an int specifying the fixed width of the icon.
     */
    @Contract(pure = true)
    @Override
    public int getIconWidth() {
        return width;
    }

    /**
     * Returns the icon's height.
     *
     * @return an int specifying the fixed height of the icon.
     */
    @Contract(pure = true)
    @Override
    public int getIconHeight() {
        return height;
    }

    @Contract(pure = true)
    public Badge getStatus() {
        return badge;
    }

    @Contract(mutates = "this")
    public void setStatus(final Badge badge) {
        this.badge = badge;
    }

    public static final class Badge {
        private BridgeContext context = null;
        private GraphicsNode node = null;

        public Badge(final String resource, final @Nullable Color color) {
            try (final InputStream is = getClass().getResourceAsStream(resource)) {
                if (is == null)
                    throw new IOException("Could not find badge icon.");

                final String parser = XMLResourceDescriptor.getXMLParserClassName();
                final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

                final byte[] data = is.readAllBytes().clone();
                final SVGDocument document = factory.createSVGDocument(null, new StringReader(new String(data, StandardCharsets.UTF_8)));
                if (document != null) {
                    final UserAgentAdapter userAgent = new UserAgentAdapter();
                    final GVTBuilder builder = new GVTBuilder();
                    if (color != null)
                        recolorSvg(document, "#000", String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()), "fill");
                    context = new BridgeContext(userAgent);
                    context.setDynamicState(BridgeContext.DYNAMIC);
                    node = builder.build(context, document);
                }
            }
            catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Badge SVG could not be read.", e);
            }
        }

        Dimension2D getDocumentSize() {
            if (context == null)
                return new Dimension(0, 0);

            return context.getDocumentSize();

        }

        void paint(final Graphics2D g2d) {
            if (node == null)
                return;

            node.paint(g2d);
        }
    }
}
