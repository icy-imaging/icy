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

package org.bioimageanalysis.icy.model.roi.tool;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIUtil;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.edit.ROIReplacesSequenceEdit;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.point.Point5D.Double;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle2DUtil;
import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DLine;

/**
 * ROI Helper class for ROI cutting action
 * 
 * @author Stephane
 */
public class ROILineCutter extends ROI2DLine
{
    public class ROILineCutterPainter extends ROI2DLinePainter
    {
        @Override
        public void mouseReleased(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            super.mouseReleased(e, imagePoint, canvas);

            // do that in background as it can take sometime
            ThreadUtil.bgRun(new Runnable()
            {
                @Override
                public void run()
                {
                    // get sequences where we are attached first
                    final List<Sequence> sequences = getSequences();

                    // remove the ROI, we don't need it anymore...
                    ROILineCutter.this.remove(false);

                    try
                    {
                        // and do cutting now
                        splitOverlappedROIs(sequences);
                    }
                    catch (UnsupportedOperationException e)
                    {
                        IcyExceptionHandler.handleException(e, false);
                    }
                    catch (InterruptedException ie)
                    {
                        // just ignore
                    }
                }
            });
        }

        @Override
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified)
        {
            final Line2D extendedLine = getExtendedLine(sequence);

            if (extendedLine != null)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                // draw extended line
                g2.setStroke(new BasicStroke((float) (ROI.getAdjustedStroke(canvas, stroke) / 2f)));
                g2.setColor(getDisplayColor());
                g2.draw(extendedLine);

                g2.dispose();
            }

            super.drawShape(g, sequence, canvas, getLine(), simplified);
        }
    }

    public ROILineCutter(Point5D pt)
    {
        super(pt);
    }

    public ROILineCutter()
    {
        super();
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROILineCutterPainter();
    }

    protected Line2D getExtendedLine(Sequence sequence)
    {
        return Rectangle2DUtil.getIntersectionLine(sequence.getBounds2D(), getLine());
    }

    /**
     * This is a special function of this ROI, it cuts all overlapped ROI from given Sequences based on the current ROI
     * shape (line).
     * 
     * @return <code>true</code> if some ROIS were cuts
     * @throws InterruptedException
     * @throws UnsupportedOperationException
     */
    public boolean splitOverlappedROIs(List<Sequence> sequences)
            throws UnsupportedOperationException, InterruptedException
    {
        boolean result = false;

        for (Sequence sequence : sequences)
        {
            final List<ROI> removedROI = new ArrayList<ROI>();
            final List<ROI> addedROI = new ArrayList<ROI>();

            sequence.beginUpdate();
            try
            {
                for (ROI roi : sequence.getROIs())
                {
                    final List<ROI> resultRois = ROIUtil.split(roi, getLine());

                    // ROI was cut ?
                    if (resultRois != null)
                    {
                        removedROI.add(roi);
                        addedROI.addAll(resultRois);
                        result = true;
                    }
                }

                if (!removedROI.isEmpty())
                {
                    sequence.removeROIs(removedROI, false);
                    sequence.addROIs(addedROI, false);

                    // add undo operation
                    sequence.addUndoableEdit(new ROIReplacesSequenceEdit(sequence, removedROI, addedROI));
                }
            }
            finally
            {
                sequence.endUpdate();
            }
        }

        return result;
    }
}
