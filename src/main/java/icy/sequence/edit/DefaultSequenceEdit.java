/**
 * 
 */
package icy.sequence.edit;

import java.awt.Image;
import java.util.Set;

import javax.swing.undo.CannotUndoException;

import icy.gui.dialog.MessageDialog;
import icy.painter.Overlay;
import icy.roi.ROI;
import icy.sequence.Sequence;

/**
 * Default lazy sequence undoable edit (do a complete sequence copy to restore previous state).<br>
 * Do not handle redo operation to not consume too much memory.
 * 
 * @author Stephane
 */
public class DefaultSequenceEdit extends AbstractSequenceEdit
{
    Sequence previous;
    Set<ROI> previousRois;
    Set<Overlay> previousOverlays;

    public DefaultSequenceEdit(Sequence previous, Sequence sequence, Image icon)
    {
        super(sequence, icon);

        this.previous = previous;
        // need to store ROI and overlays
        previousRois = previous.getROISet();
        previousOverlays = previous.getOverlaySet();
    }

    public DefaultSequenceEdit(Sequence previous, Sequence sequence)
    {
        this(previous, sequence, null);
    }

    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        // undo
        final Sequence sequence = getSequence();
        sequence.beginUpdate();
        try
        {
            // restore data & metadata
            sequence.copyFrom(previous, true);

            // restore ROIs
            for (ROI roi : previousRois)
                if (!sequence.contains(roi))
                    sequence.addROI(roi);
            for (ROI roi : sequence.getROIs())
                if (!previousRois.contains(roi))
                    sequence.removeROI(roi);

            // restore Overlays
            for (Overlay overlay : previousOverlays)
                if (!sequence.contains(overlay))
                    sequence.addOverlay(overlay);
            for (Overlay overlay : sequence.getOverlays())
                if (!previousOverlays.contains(overlay))
                    sequence.removeOverlay(overlay);
        }
        catch (InterruptedException e)
        {
            MessageDialog.showDialog("Undo operation interrupted", e.getLocalizedMessage(),
                    MessageDialog.ERROR_MESSAGE);
        }
        finally
        {
            sequence.endUpdate();
        }
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
