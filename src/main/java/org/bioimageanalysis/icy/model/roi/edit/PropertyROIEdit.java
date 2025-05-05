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

import org.bioimageanalysis.icy.model.roi.ROI;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Property change implementation for ROI undoable edition
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PropertyROIEdit extends AbstractROIEdit {
    String propertyName;
    Object previousValue;
    Object currentValue;

    public PropertyROIEdit(final ROI roi, final String propertyName, final Object previousValue, final Object currentValue, final boolean mergeable) {
        super(roi, "ROI " + propertyName + " changed");

        this.propertyName = propertyName;
        this.previousValue = previousValue;
        this.currentValue = currentValue;

        setMergeable(mergeable);
    }

    public PropertyROIEdit(final ROI roi, final String propertyName, final Object previousValue, final Object currentValue) {
        this(roi, propertyName, previousValue, currentValue, true);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        // undo
        if (previousValue instanceof String)
            getROI().setProperty(propertyName, (String) previousValue);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();

        // redo
        if (currentValue instanceof String)
            getROI().setProperty(propertyName, (String) currentValue);
    }

    @Override
    public boolean addEdit(final UndoableEdit edit) {
        if (!isMergeable())
            return false;

        if (edit instanceof final PropertyROIEdit propEdit) {
            // same ROI and same property ?
            if ((propEdit.getROI() == getROI()) && propEdit.propertyName.equals(propertyName)) {
                // collapse edits
                currentValue = propEdit.currentValue;
                return true;
            }
        }

        return false;
    }

    @Override
    public void die() {
        super.die();

        previousValue = null;
        currentValue = null;
    }
}
