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
import org.bioimageanalysis.icy.model.sequence.Sequence;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.List;
import java.util.Set;

/**
 * Property change implementation for multiple ROI undoable edition
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PropertyROIsEdit extends AbstractROIsEdit {
    String propertyName;
    List<Object> previousValues;
    Object currentValue;

    public PropertyROIsEdit(final List<? extends ROI> rois, final String propertyName, final List<Object> previousValues, final Object currentValue, final boolean mergeable) {
        super(rois, (rois.size() > 1) ? "ROIs " + propertyName + " changed" : "ROI " + propertyName + " changed");

        if (rois.size() != previousValues.size())
            throw new IllegalArgumentException("ROI list and previous values list size do not match (" + rois.size() + " != " + previousValues.size() + ")");

        this.propertyName = propertyName;
        this.previousValues = previousValues;
        this.currentValue = currentValue;

        setMergeable(mergeable);
    }

    public PropertyROIsEdit(final List<? extends ROI> rois, final String propertyName, final List<Object> previousValues, final Object currentValue) {
        this(rois, propertyName, previousValues, currentValue, true);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        final Set<Sequence> sequences = getSequences();

        // undo
        for (final Sequence sequence : sequences)
            sequence.beginUpdate();
        try {
            int ind = 0;
            for (final ROI roi : getROIs())
                if (previousValues.get(ind) instanceof String)
                    roi.setProperty(propertyName, (String) previousValues.get(ind++));
        }
        finally {
            for (final Sequence sequence : sequences)
                sequence.endUpdate();
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();

        final Set<Sequence> sequences = getSequences();

        // redo
        for (final Sequence sequence : sequences)
            sequence.beginUpdate();
        try {
            for (final ROI roi : getROIs())
                if (currentValue instanceof String)
                    roi.setProperty(propertyName, (String) currentValue);
        }
        finally {
            for (final Sequence sequence : sequences)
                sequence.endUpdate();
        }
    }

    @Override
    public boolean addEdit(final UndoableEdit edit) {
        if (!isMergeable())
            return false;

        if (edit instanceof final PropertyROIsEdit propEdit) {
            // same property and same ROI list ?
            if (propEdit.propertyName.equals(propertyName) && propEdit.getROIs().equals(getROIs())) {
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

        previousValues = null;
        currentValue = null;
    }
}
