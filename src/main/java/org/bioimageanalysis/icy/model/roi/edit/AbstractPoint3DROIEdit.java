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

import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.model.overlay.anchor.Anchor3D;
import org.bioimageanalysis.icy.model.roi.ROI3D;

/**
 * Base class of 3D control point change implementation for ROI undoable edition.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class AbstractPoint3DROIEdit extends AbstractROIEdit {
    protected Anchor3D point;

    public AbstractPoint3DROIEdit(final ROI3D roi, final Anchor3D point, final String name, final SVGResource icon) {
        super(roi, name, icon);

        this.point = point;
    }

    public AbstractPoint3DROIEdit(final ROI3D roi, final Anchor3D point, final String name) {
        this(roi, point, name, roi.getIcon());
    }

    public AbstractPoint3DROIEdit(final ROI3D roi, final Anchor3D point, final SVGResource icon) {
        this(roi, point, "ROI point changed", icon);
    }

    public AbstractPoint3DROIEdit(final ROI3D roi, final Anchor3D point) {
        this(roi, point, "ROI point changed", roi.getIcon());
    }

    public ROI3D getROI3D() {
        return (ROI3D) getSource();
    }

    public Anchor3D getPoint() {
        return point;
    }

    @Override
    public void die() {
        super.die();

        point = null;
    }
}
