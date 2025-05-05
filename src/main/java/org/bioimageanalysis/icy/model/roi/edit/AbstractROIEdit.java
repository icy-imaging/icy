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

import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.undo.AbstractIcyUndoableEdit;
import org.bioimageanalysis.icy.model.roi.ROI;

/**
 * Base ROI undoable edit.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class AbstractROIEdit extends AbstractIcyUndoableEdit {
    public AbstractROIEdit(final ROI roi, final String name, final SVGIcon icon) {
        super(roi, name, icon);
    }

    public AbstractROIEdit(final ROI roi, final String name) {
        this(roi, name, roi.getIcon());
    }

    public AbstractROIEdit(final ROI roi) {
        this(roi, "ROI changed", roi.getIcon());
    }

    public ROI getROI() {
        return (ROI) getSource();
    }
}
