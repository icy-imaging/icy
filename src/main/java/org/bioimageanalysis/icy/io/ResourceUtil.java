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

package org.bioimageanalysis.icy.io;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGImageIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Resources (images, icons...) utilities class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ResourceUtil {
    // image and icon path
    public static final String ICON_PATH = "/icon/";
    public static final String IMAGE_PATH = "/image/";

    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final String ALPHA_ICON_PATH = "alpha/";
    public static final String SVG_ICON_PATH = ICON_PATH + "svg/";

    // application images
    public static final ImageIcon ICON_ICY_16 = new IcySVGImageIcon(SVGIcon.ICY_WHITE_BG, 16);
    public static final ImageIcon ICON_ICY_20 = new IcySVGImageIcon(SVGIcon.ICY_WHITE_BG, 20);
    public static final ImageIcon ICON_ICY_24 = new IcySVGImageIcon(SVGIcon.ICY_WHITE_BG, 24);
    public static final ImageIcon ICON_ICY_32 = new IcySVGImageIcon(SVGIcon.ICY_WHITE_BG, 32);
    public static final ImageIcon ICON_ICY_64 = new IcySVGImageIcon(SVGIcon.ICY_WHITE_BG, 64);
    public static final ImageIcon ICON_ICY_256 = new IcySVGImageIcon(SVGIcon.ICY_WHITE_BG, 256);

    public static final Image IMAGE_ICY_16 = ICON_ICY_16.getImage();
    public static final Image IMAGE_ICY_20 = ICON_ICY_20.getImage();
    public static final Image IMAGE_ICY_24 = ICON_ICY_24.getImage();
    public static final Image IMAGE_ICY_32 = ICON_ICY_32.getImage();
    public static final Image IMAGE_ICY_64 = ICON_ICY_64.getImage();
    public static final Image IMAGE_ICY_256 = ICON_ICY_256.getImage();

    // alpha mask icons
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_POINT = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_point.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_LINE = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_line.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_OVAL = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_oval.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_POLYLINE = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_polyline.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_POLYGON = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_polygon.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_RECTANGLE = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_rectangle.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_AREA = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_area.png"))).getImage();

    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_CYLINDER = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_cylinder.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_BOX = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_box.png"))).getImage();
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final Image ICON_ROI_FLATPOLYGON3D = new ImageIcon(Objects.requireNonNull(Icy.class.getResource("/icon/alpha/roi_flat_polygon3D.png"))).getImage();

    public static ArrayList<Image> getIcyIconImages() {
        final ArrayList<Image> result = new ArrayList<>();

        result.add(ResourceUtil.IMAGE_ICY_256);
        result.add(ResourceUtil.IMAGE_ICY_64);
        result.add(ResourceUtil.IMAGE_ICY_32);
        result.add(ResourceUtil.IMAGE_ICY_16);

        return result;
    }
}
