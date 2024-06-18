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

package org.bioimageanalysis.icy.model.sequence.edit;

import ome.xml.meta.OMEXMLMetadata;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import javax.swing.undo.CannotUndoException;

/**
 * Default lazy sequence metadata undoable edit (do a complete sequence metadata copy to restore
 * previous state).<br>
 * Do not handle redo operation to not consume too much memory.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class MetadataSequenceEdit extends AbstractSequenceEdit {
    OMEXMLMetadata previous;

    public MetadataSequenceEdit(final OMEXMLMetadata previous, final Sequence sequence, final String name, final SVGIcon icon) {
        super(sequence, name, icon);

        this.previous = previous;
    }

    public MetadataSequenceEdit(final OMEXMLMetadata previous, final Sequence sequence, final SVGIcon icon) {
        this(previous, sequence, "Sequence metadata changed", icon);

        this.previous = previous;
    }

    public MetadataSequenceEdit(final OMEXMLMetadata previous, final Sequence sequence, final String name) {
        this(previous, sequence, name, null);

        this.previous = previous;
    }

    public MetadataSequenceEdit(final OMEXMLMetadata previous, final Sequence sequence) {
        this(previous, sequence, "Sequence metadata changed", null);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        // undo
        getSequence().setMetaData(previous);
    }

    @Override
    public boolean canRedo() {
        return false;
    }
}
