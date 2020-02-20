/**
 * 
 */
package plugins.kernel.roi.tool.morphology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.dimension.Dimension3D;
import icy.type.dimension.Dimension5D;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi3d.ROI3DArea;

/**
 * @author Daniel
 */
public class ROIErosionCalculator
{

    private List<? extends ROI> rois;
    private Dimension5D imageSize;
    private Dimension3D pixelSize;
    private double distance;
    private List<ROI> erosionRois;

    public ROIErosionCalculator(List<? extends ROI> rois, Dimension5D imageSize, Dimension3D pixelSize, double distance)
    {
        this.rois = rois;
        this.imageSize = imageSize;
        this.pixelSize = pixelSize;
        this.distance = distance;
    }

    public List<ROI> getErosion()
    {
        if (erosionRois == null)
            compute();
        return erosionRois;
    }

    public void compute()
    {

        if (imageSize.getSizeZ() == 1)
        {
            Sequence dt = ROIUtil.computeDistanceMap(rois, imageSize, pixelSize);
            erosionRois = new ArrayList<ROI>(rois.size());
            for (ROI roi : rois)
            {
                ROI2DArea erosionRoi = new ROI2DArea();
                SequenceDataIterator dtIt = new SequenceDataIterator(dt, roi);
                while (!dtIt.done())
                {
                    double pixelValue = dtIt.get();
                    if (pixelValue > distance)
                    {
                        erosionRoi.addPoint(dtIt.getPositionX(), dtIt.getPositionY());
                    }
                    dtIt.next();
                }
                erosionRois.add(erosionRoi);
            }

        }
        else if (imageSize.getSizeZ() > 1)
        {
            Sequence dt = ROIUtil.computeDistanceMap(rois, imageSize, pixelSize);
            erosionRois = new ArrayList<ROI>(rois.size());
            for (ROI roi : rois)
            {
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
                erosionRois.add(erosionRoi);
            }

        }
        else
        {
            erosionRois = Collections.emptyList();
        }

    }

}
