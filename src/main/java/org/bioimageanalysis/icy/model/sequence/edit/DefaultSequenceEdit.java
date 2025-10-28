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

import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import javax.swing.undo.CannotUndoException;
import java.util.Set;

/**
 * Default lazy sequence undoable edit (do a complete sequence copy to restore previous state).<br>
 * Do not handle redo operation to not consume too much memory.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class DefaultSequenceEdit extends AbstractSequenceEdit {
    Sequence previous;
    Set<ROI> previousRois;
    Set<Overlay> previousOverlays;

    public DefaultSequenceEdit(final Sequence previous, final Sequence sequence, final SVGResource icon) {
        super(sequence, icon);

        this.previous = previous;
        // need to store ROI and overlays
        previousRois = previous.getROISet();
        previousOverlays = previous.getOverlaySet();
    }

    public DefaultSequenceEdit(final Sequence previous, final Sequence sequence) {
        this(previous, sequence, null);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        // undo
        final Sequence sequence = getSequence();
        sequence.beginUpdate();
        try {
            // restore data & metadata
            sequence.copyFrom(previous, true);

            // restore ROIs
            for (final ROI roi : previousRois)
                if (!sequence.contains(roi))
                    sequence.addROI(roi);
            for (final ROI roi : sequence.getROIs())
                if (!previousRois.contains(roi))
                    sequence.removeROI(roi);

            // restore Overlays
            for (final Overlay overlay : previousOverlays)
                if (!sequence.contains(overlay))
                    sequence.addOverlay(overlay);
            for (final Overlay overlay : sequence.getOverlays())
                if (!previousOverlays.contains(overlay))
                    sequence.removeOverlay(overlay);
        }
        catch (final InterruptedException e) {
            MessageDialog.showDialog("Undo operation interrupted", e.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
        }
        finally {
            sequence.endUpdate();
        }
    }

    @Override
    public boolean canRedo() {
        return false;
    }
}
