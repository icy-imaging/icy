/**
 * 
 */
package plugins.kernel.roi.morphology;

import java.util.ArrayList;
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

    private ROI roi;
    private Dimension3D pixelSize;
    private double distance;

    public ROIErosionCalculator(ROI roi, Dimension3D pixelSize, double distance)
    {
        this.roi = roi;
        this.pixelSize = pixelSize;
        this.distance = distance;
    }

    private ROI erosionRoi;

    public ROI getErosion() throws InterruptedException
    {
        if (erosionRoi == null)
            compute();
        return erosionRoi;
    }

    public void compute() throws InterruptedException
    {

        if (roi.getBounds5D().getSizeZ() == 1)
        {
            Dimension5D roiDims = roi.getBounds5D().getDimension();
            Dimension5D dims = new Dimension5D.Integer();
            dims.setSizeX(Math.ceil(roiDims.getSizeX()));
            dims.setSizeY(Math.ceil(roiDims.getSizeY()));
            dims.setSizeZ(Math.ceil(roiDims.getSizeZ()));
            dims.setSizeT(Math.ceil(roiDims.getSizeT()));
            dims.setSizeC(Math.ceil(roiDims.getSizeC()));
            Sequence dt = ROIUtil.computeDistanceMap(roi, dims, pixelSize, true);
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
            this.erosionRoi = erosionRoi;

        }
        else if (roi.getBounds5D().getSizeZ() > 1)
        {
            List<ROI> listRois = new ArrayList<ROI>();
            listRois.add(roi);
            Sequence dt = ROIUtil.computeDistanceMap(listRois, roi.getBounds5D().getDimension(), pixelSize, true);
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
            this.erosionRoi = erosionRoi;

        }
        else
        {
            erosionRoi = null;
        }

    }

}
