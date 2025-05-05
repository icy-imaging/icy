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

package org.bioimageanalysis.extension.kernel.roi.morphology;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2D;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIUtil;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceDataIterator;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DArea;
import org.bioimageanalysis.extension.kernel.roi.roi3d.ROI3DArea;

public class ROIDilationCalculator
{

    private ROI roi;
    private Dimension3D pixelSize;
    private double distance;

    public ROIDilationCalculator(ROI roi, Dimension3D pixelSize, double distance)
    {
        this.roi = roi;
        this.pixelSize = pixelSize;
        this.distance = distance;
    }

    private ROI dilationRoi;

    public ROI getDilation() throws InterruptedException
    {
        if (dilationRoi == null)
            compute();
        return dilationRoi;
    }

    private BooleanMask2D roiBooleanMask;

    private void compute() throws InterruptedException
    {
        Rectangle5D roiBounds = roi.getBounds5D();
        if (roiBounds.getSizeZ() == 1 || Double.isInfinite(roiBounds.getSizeZ()))
        {
            this.roiBooleanMask = roi.getBooleanMask2D(0,
                    Double.isInfinite(roiBounds.getSizeT()) ? 0 : (int) roiBounds.getT(), 0, true);
            Rectangle maskBounds = (Rectangle) roiBooleanMask.bounds.clone();
            roiBooleanMask.bounds.x = 0;
            roiBooleanMask.bounds.y = 0;
            setDistanceMapRoi2D();

            Sequence dt = ROIUtil.computeDistanceMap(distanceMapRoi2d, distanceMapRoi2d.getBounds5D().getDimension(),
                    pixelSize, false);
            boolean[] dilationMask = new boolean[distanceMapRoiMaskRect.width * distanceMapRoiMaskRect.height];
            SequenceDataIterator dtIt = new SequenceDataIterator(dt, distanceMapRoi2d);
            while (!dtIt.done())
            {
                double pixelValue = dtIt.get();
                if (pixelValue <= distance)
                {
                    dilationMask[dtIt.getPositionX() + dtIt.getPositionY() * distanceMapRoiMaskRect.width] = true;
                }
                dtIt.next();
            }
            BooleanMask2D dilationMask2D = new BooleanMask2D(distanceMapRoiMaskRect, dilationMask);
            dilationMask2D.add(this.roiBooleanMask.bounds, this.roiBooleanMask.mask);
            dilationMask2D.bounds.x += maskBounds.x;
            dilationMask2D.bounds.y += maskBounds.y;
            ROI2DArea dilationRoi2d = new ROI2DArea(dilationMask2D);
            dilationRoi2d.setZ(Double.isInfinite(roiBounds.getZ()) ? -1 : (int) roiBounds.getZ());
            dilationRoi2d.setT(Double.isInfinite(roiBounds.getT()) ? -1 : (int) roiBounds.getT());
            dilationRoi = dilationRoi2d;
        }
        else if (roi.getBounds5D().getSizeZ() > 1)
        {
            List<ROI> listRois = new ArrayList<ROI>();
            listRois.add(roi);
            Sequence dt = ROIUtil.computeDistanceMap(listRois, roi.getBounds5D().getDimension(), pixelSize, false);
            ROI3DArea erosionRoi = new ROI3DArea();
            SequenceDataIterator dtIt = new SequenceDataIterator(dt, roi);
            while (!dtIt.done())
            {
                double pixelValue = dtIt.get();
                if (pixelValue > distance)
                {
                    erosionRoi.addPoint(dtIt.getPositionX(), dtIt.getPositionY(), dtIt.getPositionZ());
                }
                dtIt.next();
            }
            this.dilationRoi = erosionRoi;

        }
        else
        {
            dilationRoi = null;
        }
    }

    ROI2DArea distanceMapRoi2d;
    Rectangle distanceMapRoiMaskRect;

    private void setDistanceMapRoi2D()
    {
        Rectangle distanceMapRoiMaskRect = new Rectangle((int) (this.roiBooleanMask.bounds.x - Math.ceil(distance)),
                (int) (this.roiBooleanMask.bounds.y - Math.ceil(distance)),
                (int) (this.roiBooleanMask.bounds.width + 2 * Math.ceil(distance)),
                (int) (this.roiBooleanMask.bounds.height + 2 * Math.ceil(distance)));
        this.distanceMapRoiMaskRect = new Rectangle(distanceMapRoiMaskRect);
        boolean[] distanceMapMask = new boolean[(int) distanceMapRoiMaskRect.width * distanceMapRoiMaskRect.height];
        Arrays.fill(distanceMapMask, true);
        BooleanMask2D distanceMapRoiMask = new BooleanMask2D(distanceMapRoiMaskRect, distanceMapMask);
        distanceMapRoiMask.subtract(this.roiBooleanMask.bounds, this.roiBooleanMask.mask);
        distanceMapRoiMask.bounds.x = 0;
        distanceMapRoiMask.bounds.y = 0;
        distanceMapRoi2d = new ROI2DArea(distanceMapRoiMask);
        distanceMapRoi2d.setZ(0);
        distanceMapRoi2d.setT(0);
        distanceMapRoi2d.setC(0);
    }

}
