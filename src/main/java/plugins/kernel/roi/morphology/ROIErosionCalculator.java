/**
 * 
 */
package plugins.kernel.roi.morphology;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import icy.roi.ROI;
import icy.roi.ROIUtil;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.type.dimension.Dimension3D;
import icy.type.dimension.Dimension5D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
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

    private static Point5D positionZero = new Point5D.Double();
    
    public void compute() throws InterruptedException
    {
        Rectangle5D roiBounds = roi.getBounds5D();
        if (roiBounds.getSizeZ() == 1 || Double.isInfinite(roiBounds.getSizeZ()))
        {
            Dimension5D roiDims = roiBounds.getDimension();
            Dimension5D dims = new Dimension5D.Integer();
            dims.setSizeX(Math.ceil(roiDims.getSizeX()));
            dims.setSizeY(Math.ceil(roiDims.getSizeY()));
            dims.setSizeZ(Double.isInfinite(roiBounds.getSizeZ())? 1: Math.ceil(roiDims.getSizeZ()));
            dims.setSizeT(Double.isInfinite(roiBounds.getSizeT())? 1: Math.ceil(roiDims.getSizeT()));
            dims.setSizeC(Double.isInfinite(roiBounds.getSizeC())? 1: Math.ceil(roiDims.getSizeC()));
            roi.setPosition5D(positionZero);
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
            roi.setPosition5D(roiBounds.getPosition());
            Point2D erosionPosition = erosionRoi.getPosition2D();
            erosionPosition.setLocation(erosionPosition.getX() + roiBounds.getX(), erosionPosition.getY() + roiBounds.getY());
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
