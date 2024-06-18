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

package org.bioimageanalysis.extension.kernel.roi.roi3d;

import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DEllipse;
import org.bioimageanalysis.icy.common.geom.cylinder.Cylinder3D;
import org.bioimageanalysis.icy.common.geom.point.Point3D;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle3D;
import org.bioimageanalysis.icy.common.geom.shape.BoxShape3D;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;

import java.awt.geom.RectangularShape;

/**
 * Class defining a 3D Cylinder ROI.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI3DCylinder extends ROI3DBoxShape {
    /**
     * Construct 3D cylinder ROI
     *
     * @param cylinder
     *        source 3D cylinder
     */
    public ROI3DCylinder(final Cylinder3D cylinder) {
        super(cylinder);

        // set icon
        setIcon(SVGIcon.BROKEN_IMAGE); // TODO change this icon
    }

    /**
     * Construct 3D cylinder ROI
     *
     * @param box
     *        source 3D box
     */
    public ROI3DCylinder(final BoxShape3D box) {
        this(new Cylinder3D(box));
    }

    /**
     * Construct 3D cylinder ROI
     *
     * @param pt
     *        source 3D point
     */
    public ROI3DCylinder(final Point3D pt) {
        this(new Cylinder3D(pt.getX(), pt.getY(), pt.getZ(), 0d, 0d, 0d));
    }

    /**
     * Construct 3D cylinder ROI
     *
     * @param pt
     *        source 5D point
     */
    public ROI3DCylinder(final Point5D pt) {
        this(pt.toPoint3D());
    }

    /**
     * Construct 3D cylinder ROI from 2D rect and Z information
     *
     * @param r
     *        2D rect
     */

    public ROI3DCylinder(final RectangularShape r, final double z, final double sizeZ) {
        this(new Rectangle3D.Double(r.getX(), r.getY(), z, r.getWidth(), r.getHeight(), sizeZ));
    }

    /**
     * Construct empty 3D cylinder ROI
     */
    public ROI3DCylinder() {
        this(new Rectangle3D.Double());
    }

    @Override
    protected ROI2DEllipse createROI2DShape() {
        return new ROI2DEllipse();
    }

    @Override
    public String getDefaultName() {
        return "Cylinder3D";
    }
}
