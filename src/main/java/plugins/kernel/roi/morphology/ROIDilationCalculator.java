package plugins.kernel.roi.morphology;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.dimension.Dimension3D;
import icy.type.rectangle.Rectangle5D;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi3d.ROI3DArea;

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
            this.roiBooleanMask = roi.getBooleanMask2D(0, 0, 0, true);
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
            dilationRoi = new ROI2DArea(dilationMask2D);
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
