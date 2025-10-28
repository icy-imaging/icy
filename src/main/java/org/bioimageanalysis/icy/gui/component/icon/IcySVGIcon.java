/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Icon from SVG content.
 *
 * @author Thomas Musset
 * @since 3.0.0
 */
public final class IcySVGIcon implements Icon {
    private SVGDocument svgDocument;
    private BridgeContext ctx;
    private GraphicsNode svgNode;
    private final int width;
    private final int height;

    private final @Nullable LookAndFeelUtil.ColorType colorType;

    private IcySVGIcon(final String svgContent, final int width, final int height, final @Nullable Color color, final @Nullable LookAndFeelUtil.ColorType colorType) {
        this.width = width;
        this.height = height;
        this.colorType = colorType;

        // load SVG document
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        svgDocument = null;
        try {
            svgDocument = factory.createSVGDocument(null, new StringReader(svgContent));
        }
        catch (final IOException e) {
            // Should not occur
            IcyLogger.error(this.getClass(), e, "SVG could not be read.");
        }

        if (svgDocument != null) {
            // Construire le GraphicsNode
            if (color != null)
                recolorSvg(color);
            else if (colorType != null) {
                final Color ui = LookAndFeelUtil.getUIColor(colorType);
                if (ui != null)
                    recolorSvg(ui);
            }
            //rebuildNode();
            /*final UserAgentAdapter userAgent = new UserAgentAdapter();
            ctx = new BridgeContext(userAgent);
            ctx.setDynamicState(BridgeContext.STATIC);
            final GVTBuilder builder = new GVTBuilder();
            svgNode = builder.build(ctx, svgDocument);*/
        }
        else {
            ctx = null;
            svgNode = null;
        }
    }

    private void rebuildNode() {
        if (svgDocument == null) {
            ctx = null;
            svgNode = null;
            return;
        }

        final UserAgentAdapter userAgent = new UserAgentAdapter();
        ctx = new BridgeContext(userAgent);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        final GVTBuilder builder = new GVTBuilder();
        svgNode = builder.build(ctx, svgDocument);
    }

    private void recolorSvg(@NotNull final Color color) {
        final String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        final NodeList elements = svgDocument.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            final Element el = (Element) elements.item(i);
            if (el.hasAttribute("fill") && !el.getAttribute("fill").startsWith("url(") && !el.getAttribute("fill").equalsIgnoreCase("none")) {
                el.setAttribute("fill", hex);
            }
            if (el.hasAttribute("stroke") && !el.getAttribute("stroke").startsWith("url(") && !el.getAttribute("stroke").equalsIgnoreCase("none")) {
                el.setAttribute("stroke", hex);
            }
        }
    }

    @NotNull
    static IcySVGIcon fromBytes(final byte @NotNull [] data, final int width, final int height) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, null, null);
    }

    @NotNull
    static IcySVGIcon fromBytes(final byte @NotNull [] data, final int width, final int height, final @NotNull Color color) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, color, null);
    }

    @NotNull
    static IcySVGIcon fromBytes(final byte @NotNull [] data, final int width, final int height, final @NotNull LookAndFeelUtil.ColorType colorType) {
        final String svgContent = new String(data, StandardCharsets.UTF_8);
        return new IcySVGIcon(svgContent, width, height, null, colorType);
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
                recolorSvg(ui);
        }
        rebuildNode();

        if (svgNode == null || ctx == null)
            return;

        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));

        // Récupérer la taille originale du SVG
        final Rectangle bounds = new Rectangle(0, 0, (int) ctx.getDocumentSize().getWidth(), (int) ctx.getDocumentSize().getHeight());

        final double scaleX = (double) width / bounds.getWidth();
        final double scaleY = (double) height / bounds.getHeight();

        final AffineTransform at = new AffineTransform();
        at.scale(scaleX, scaleY);

        g2d.transform(at);
        svgNode.paint(g2d);
        g2d.dispose();
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
}
