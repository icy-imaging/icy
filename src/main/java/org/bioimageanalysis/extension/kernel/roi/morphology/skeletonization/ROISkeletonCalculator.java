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

package org.bioimageanalysis.extension.kernel.roi.morphology.skeletonization;

import java.awt.Rectangle;

import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2D;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIUtil;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DArea;

public class ROISkeletonCalculator
{
    private ROI roi;
    private Dimension3D pixelSize;

    public ROISkeletonCalculator(ROI roi, Dimension3D pixelSize)
    {
        this.roi = roi;
        this.pixelSize = pixelSize;
    }

    private ROI skeletonRoi;

    public ROI getSkeletonROI() throws InterruptedException
    {
        if (this.skeletonRoi == null)
            compute();

        return skeletonRoi;
    }

    private BooleanMask2D roiBooleanMask;

    private void compute() throws InterruptedException
    {
        Rectangle5D roiBounds = roi.getBounds5D();
        if (roiBounds.getSizeZ() == 1 || Double.isInfinite(roiBounds.getSizeZ()))
        {
            this.roiBooleanMask = roi.getBooleanMask2D(0, 0, 0, true);
            setDistanceMapRoi2D();

            Sequence dt = ROIUtil.computeDistanceMap(distanceMapRoi2d, distanceMapRoi2d.getBounds5D().getDimension(), pixelSize,
                    false);

            MinimumSpanningTreeCalculator mstCalculator = new MinimumSpanningTreeCalculator(dt, pixelSize);
            Sequence costs = mstCalculator.getCosts();
            Sequence mst = mstCalculator.getTree();
            TopologicalDescriptor topologicalDescriptor = new TopologicalDescriptor(dt, costs, mst, pixelSize);
            topologicalDescriptor.compute();
        }

    }

    ROI2DArea distanceMapRoi2d;

    private void setDistanceMapRoi2D() throws InterruptedException
    {
        Rectangle distanceMapRoiMaskRect = new Rectangle((int) (this.roiBooleanMask.bounds.x - Math.ceil(1)),
                (int) (this.roiBooleanMask.bounds.y - Math.ceil(1)),
                (int) (this.roiBooleanMask.bounds.width + 2 * Math.ceil(1)),
                (int) (this.roiBooleanMask.bounds.height + 2 * Math.ceil(1)));
        boolean[] distanceMapMask = new boolean[(int) distanceMapRoiMaskRect.width * distanceMapRoiMaskRect.height];
        BooleanMask2D distanceMapRoiMask = new BooleanMask2D(distanceMapRoiMaskRect, distanceMapMask);
        distanceMapRoiMask.add(this.roiBooleanMask.bounds, this.roiBooleanMask.mask);
        distanceMapRoiMask.bounds.x = 0;
        distanceMapRoiMask.bounds.y = 0;
        distanceMapRoi2d = new ROI2DArea(distanceMapRoiMask);
        distanceMapRoi2d.setZ(0);
        distanceMapRoi2d.setT(0);
        distanceMapRoi2d.setC(0);
    }
}
