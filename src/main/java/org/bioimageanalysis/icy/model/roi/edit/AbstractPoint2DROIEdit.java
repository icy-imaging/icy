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

package org.bioimageanalysis.icy.model.roi.edit;

import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DShape;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor2D;

/**
 * Base class of 2D control point change implementation for ROI undoable edition.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class AbstractPoint2DROIEdit extends AbstractROIEdit {
    protected Anchor2D point;

    public AbstractPoint2DROIEdit(final ROI2DShape roi, final Anchor2D point, final String name, final SVGIcon icon) {
        super(roi, name, icon);

        this.point = point;
    }

    public AbstractPoint2DROIEdit(final ROI2DShape roi, final Anchor2D point, final String name) {
        this(roi, point, name, roi.getIcon());
    }

    public AbstractPoint2DROIEdit(final ROI2DShape roi, final Anchor2D point, final SVGIcon icon) {
        this(roi, point, "ROI point changed", icon);
    }

    public AbstractPoint2DROIEdit(final ROI2DShape roi, final Anchor2D point) {
        this(roi, point, "ROI point changed", roi.getIcon());
    }

    public ROI2DShape getROI2DShape() {
        return (ROI2DShape) getSource();
    }

    public Anchor2D getPoint() {
        return point;
    }

    @Override
    public void die() {
        super.die();

        point = null;
    }
}
