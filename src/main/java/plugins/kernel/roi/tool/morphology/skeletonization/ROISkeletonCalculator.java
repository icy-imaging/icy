package plugins.kernel.roi.tool.morphology.skeletonization;

import java.awt.Rectangle;

import icy.main.Icy;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.type.dimension.Dimension3D;
import icy.type.rectangle.Rectangle5D;
import plugins.kernel.roi.roi2d.ROI2DArea;

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

    private void setDistanceMapRoi2D()
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
