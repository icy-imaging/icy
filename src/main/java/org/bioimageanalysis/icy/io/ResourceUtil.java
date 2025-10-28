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
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;

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
    public static final String BASE_RESOURCE_PATH = "/";

    // image and icon path
    public static final String ICON_PATH = BASE_RESOURCE_PATH + "icon/";
    public static final String IMAGE_PATH = BASE_RESOURCE_PATH + "image/";

    public static final String SVG_ICON_PATH = ICON_PATH + "svg/";
    public static final String SVG_IMAGE_PATH = IMAGE_PATH + "svg/";

    public static final IcySVG ICY_LOGO_SVG = new IcySVG(SVGResource.ICY_WHITE_BG);
    // application icons
    //public static final Icon ICON_ICY_16 = ICY_LOGO_SVG.getIcon(16);
    public static final Icon ICON_ICY_20 = ICY_LOGO_SVG.getIcon(20);
    //public static final Icon ICON_ICY_24 = ICY_LOGO_SVG.getIcon(24);
    //public static final Icon ICON_ICY_32 = ICY_LOGO_SVG.getIcon(32);
    //public static final Icon ICON_ICY_64 = ICY_LOGO_SVG.getIcon(64);
    //public static final Icon ICON_ICY_256 = ICY_LOGO_SVG.getIcon(256);
    // application images
    //public static final Image IMAGE_ICY_16 = ICY_LOGO_SVG.getImage(16);
    //public static final Image IMAGE_ICY_20 = ICY_LOGO_SVG.getImage(20);
    //public static final Image IMAGE_ICY_24 = ICY_LOGO_SVG.getImage(24);
    public static final Image IMAGE_ICY_32 = ICY_LOGO_SVG.getImage(32);
    public static final Image IMAGE_ICY_64 = ICY_LOGO_SVG.getImage(64);
    public static final Image IMAGE_ICY_128 = ICY_LOGO_SVG.getImage(128);
    public static final Image IMAGE_ICY_256 = ICY_LOGO_SVG.getImage(256);

    public static ArrayList<Image> getIcyIconImages() {
        final ArrayList<Image> result = new ArrayList<>();

        result.add(ResourceUtil.IMAGE_ICY_256);
        result.add(ResourceUtil.IMAGE_ICY_128);
        result.add(ResourceUtil.IMAGE_ICY_64);
        result.add(ResourceUtil.IMAGE_ICY_32);
        //result.add(ResourceUtil.IMAGE_ICY_16);

        return result;
    }
}
