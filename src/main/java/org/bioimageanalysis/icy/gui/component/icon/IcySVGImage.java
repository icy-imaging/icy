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

import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.bioimageanalysis.icy.model.image.ImageUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * BufferedImage from SVG content.
 *
 * @author Thomas Musset
 * @since 3.0.0
 */
public class IcySVGImage extends BufferedImage {
    private IcySVGImage(final String svgContent, final int width, final int height, final @Nullable Color color) {
        super(width, height, BufferedImage.TYPE_INT_ARGB);

        try {
            final PNGTranscoder transcoder = new PNGTranscoder() {
                @Override
                protected @NotNull ImageRenderer createRenderer() {
                    final ImageRenderer r = super.createRenderer();

                    final RenderingHints rh = r.getRenderingHints();

                    rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
                    rh.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
                    rh.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
                    /*rh.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
                    rh.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
                    rh.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE));
                    rh.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE));
                    rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON));*/

                    r.setRenderingHints(rh);

                    return r;
                }
            };
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);


            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final TranscoderInput input = new TranscoderInput(new StringReader(svgContent));
            final TranscoderOutput output = new TranscoderOutput(baos);
            transcoder.transcode(input, output);
            baos.flush();

            final BufferedImage temp;
            if (color != null) {
                final BufferedImage cache = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                ImageUtil.paintColorImageFromAlphaImage(cache, temp, color);
            }
            else {
                temp = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
            }

            if (temp != null)
                getGraphics().drawImage(temp, 0, 0, null);
        }
        catch (final Exception e) {
            IcyLogger.warn(this.getClass(), e, "Can't create SVG image from SVG content");
        }
    }

    @Contract("_, _, _ -> new")
    static @NotNull IcySVGImage fromBytes(final byte @NotNull [] data, final int width, final int height) {
        return new IcySVGImage(new String(data, StandardCharsets.UTF_8), width, height, null);
    }

    @Contract("_, _, _, _ -> new")
    static @NotNull IcySVGImage fromBytes(final byte @NotNull [] data, final int width, final int height, final @NotNull Color color) {
        return new IcySVGImage(new String(data, StandardCharsets.UTF_8), width, height, color);
    }
}
