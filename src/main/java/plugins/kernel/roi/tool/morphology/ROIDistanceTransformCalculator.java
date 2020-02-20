package plugins.kernel.roi.tool.morphology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import icy.image.IcyBufferedImage;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.VolumetricImage;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.type.dimension.Dimension3D;
import icy.type.dimension.Dimension5D;

public class ROIDistanceTransformCalculator
{
    private Dimension5D imageSize;
    private Dimension3D pixelSize;
    private List<ROI> rois;

    public ROIDistanceTransformCalculator(Dimension5D imageSize, Dimension3D pixelSize)
    {
        this.imageSize = imageSize;
        this.pixelSize = pixelSize;
        this.rois = new ArrayList<>();
    }

    public <T extends ROI> void addROI(T roi)
    {
        rois.add(roi);
    }

    public <T extends ROI> void addAll(Collection<T> rois)
    {
        this.rois.addAll(rois);
    }

    Sequence distanceMap;

    public Sequence getDistanceMap()
    {
        if (distanceMap == null)
            compute();
        return distanceMap;
    }

    public void compute()
    {
        initializeDistanceMap();
        drawROIs();
        processTimePoints();
    }

    double[] buffer;

    private void initializeDistanceMap()
    {
        distanceMap = new Sequence();
        for (int m = 0; m < imageSize.getSizeT(); m++)
        {
            VolumetricImage volume = new VolumetricImage();
            for (int k = 0; k < imageSize.getSizeZ(); k++)
            {
                IcyBufferedImage plane = new IcyBufferedImage((int) imageSize.getSizeX(), (int) imageSize.getSizeY(), 1,
                        DataType.DOUBLE);
                volume.setImage(k, plane);
            }
            distanceMap.addVolumetricImage(m, volume);
        }
        buffer = new double[Math.max((int) imageSize.getSizeY(), (int) imageSize.getSizeZ())];
    }

    private void drawROIs()
    {
        for (ROI roi : rois)
        {
            DataIteratorUtil.set(new SequenceDataIterator(distanceMap, roi, true), 1d);
        }
    }

    private VolumetricImage currentVolumeImage;

    private void processTimePoints()
    {
        for (int t = 0; t < imageSize.getSizeT(); t++)
        {
            currentVolumeImage = distanceMap.getVolumetricImage(t);
            processCurrentVolume();
        }
        distanceMap.dataChanged();
    }

    private double[][] currentVolumePlanes;
    private int currentK;
    private int currentJ;
    private int currentI;

    private double squaredSizeY;
    private double squaredSizeZ;

    private void processCurrentVolume()
    {
        currentVolumePlanes = new double[(int) imageSize.getSizeZ()][];
        squaredSizeY = pixelSize.getSizeY() * pixelSize.getSizeY();
        squaredSizeZ = pixelSize.getSizeZ() * pixelSize.getSizeZ();
        for (int k = 0; k < imageSize.getSizeZ(); k++)
        {
            double[] currentPlaneData = currentVolumeImage.getImage(k).getDataXYAsDouble(0);
            currentVolumePlanes[k] = currentPlaneData;
            currentK = k;
            processCurrentPlane();
        }

        if (imageSize.getSizeZ() > 1)
        {
            for (int j = 0; j < imageSize.getSizeY(); j++)
            {
                currentJ = j;
                for (int i = 0; i < imageSize.getSizeX(); i++)
                {
                    currentI = i;
                    processCurrentFiber();
                }
            }
        }
        for (int k = 0; k < imageSize.getSizeZ(); k++)
        {
            for (int j = 0; j < imageSize.getSizeY(); j++)
            {
                currentJ = j;
                for (int i = 0; i < imageSize.getSizeX(); i++)
                {
                    setValueAt(i, j, k, Math.sqrt(getValueAt(i, j, k)));
                }
            }
        }
    }

    private void processCurrentPlane()
    {
        for (int j = 0; j < imageSize.getSizeY(); j++)
        {
            currentJ = j;
            processCurrentRow();
        }

        for (int i = 0; i < imageSize.getSizeX(); i++)
        {
            currentI = i;
            processCurrentCol();
        }
    }

    private void processCurrentRow()
    {
        forwardPassCurrentRow();
        backwardPassCurrentRow();
    }

    private void forwardPassCurrentRow()
    {
        double affectedValue = 0;

        for (int i = 0; i < imageSize.getSizeX(); i++)
        {
            if (getValueAt(i, currentJ, currentK) != 0d)
            {
                affectedValue = affectedValue + pixelSize.getSizeX();
            }
            else
            {
                affectedValue = 0d;
            }
            setValueAt(i, currentJ, currentK, affectedValue * affectedValue);
        }
    }

    private void backwardPassCurrentRow()
    {
        double affectedValue = 0;
        for (int i = (int) imageSize.getSizeX() - 1; i >= 0; i--)
        {
            if (getValueAt(i, currentJ, currentK) != 0d)
            {
                affectedValue = affectedValue + pixelSize.getSizeX();
            }
            else
            {
                affectedValue = 0d;
            }
            setValueAt(i, currentJ, currentK,
                    Math.min(getValueAt(i, currentJ, currentK), affectedValue * affectedValue));
        }
    }

    private void processCurrentCol()
    {
        for (int j = 0; j < imageSize.getSizeY(); j++)
        {
            buffer[j] = getValueAt(currentI, j, currentK);
        }
        forwardPassCurrentCol();
        backwardPassCurrentCol();
    }

    private void forwardPassCurrentCol()
    {
        int a = 0;
        for (int j = 0; j < imageSize.getSizeY(); j++)
        {
            if (a > 0)
                a--;
            double dj = buffer[j] / squaredSizeY;
            double dj1 = j == 0 ? 0d : buffer[j - 1] / squaredSizeY;
            if (dj > dj1 + 1d)
            {
                int b = (int) Math.ceil((dj - dj1 - 1d) / 2d);
                if (j + b > imageSize.getSizeY())
                    b = (int) imageSize.getSizeY() - j;
                for (int n = a; n < b; n++)
                {
                    int nSq = n + 1;
                    nSq *= nSq;
                    double m = dj1 + nSq;
                    if (buffer[j + n] / squaredSizeY <= m)
                        break;
                    if (m < getValueAt(currentI, j + n, currentK) / squaredSizeY)
                        setValueAt(currentI, j + n, currentK, m * squaredSizeY);
                }
                a = b;
            }
            else
            {
                a = 0;
            }
        }
    }

    private void backwardPassCurrentCol()
    {
        int a = 0;
        for (int j = (int) imageSize.getSizeY() - 1; j >= 0; j--)
        {
            if (a > 0)
                a--;
            double dj = buffer[j] / squaredSizeY;
            double dj1 = j == (int) imageSize.getSizeY() - 1 ? 0 : buffer[j + 1] / squaredSizeY;
            if (dj > dj1 + 1d)
            {
                int b = (int) Math.ceil((dj - dj1 - 1d) / 2d);
                if (j - b < 0)
                    b = j;
                for (int n = a; n < b; n++)
                {
                    double nSq = n + 1;
                    nSq *= nSq;
                    double m = dj1 + nSq;
                    if (buffer[j - n] / squaredSizeY <= m)
                        break;
                    if (m < getValueAt(currentI, j - n, currentK) / squaredSizeY)
                        setValueAt(currentI, j - n, currentK, m * squaredSizeY);
                }
                a = b;
            }
            else
            {
                a = 0;
            }
        }
    }

    private void processCurrentFiber()
    {
        for (int k = 0; k < imageSize.getSizeZ(); k++)
        {
            buffer[k] = getValueAt(currentI, currentJ, k);
        }
        forwardPassCurrentFiber();
        backwardPassCurrentFiber();
    }

    private void forwardPassCurrentFiber()
    {
        int a = 0;
        for (int k = 0; k < imageSize.getSizeZ(); k++)
        {
            if (a > 0)
                a--;
            double dk = buffer[k] / squaredSizeZ;
            double dk1 = k == 0 ? 0d : buffer[k - 1] / squaredSizeZ;
            if (dk > dk1 + 1d)
            {
                int b = (int) Math.ceil((dk - dk1 - 1d) / 2d);
                if (k + b > imageSize.getSizeZ())
                    b = (int) imageSize.getSizeZ() - k;
                for (int n = a; n < b; n++)
                {
                    int nSq = n + 1;
                    nSq *= nSq;
                    double m = dk1 + nSq;
                    if (buffer[k + n] / squaredSizeZ <= m)
                        break;
                    if (m < getValueAt(currentI, currentJ, k + n) / squaredSizeZ)
                        setValueAt(currentI, currentJ, k + n, m * squaredSizeZ);
                }
                a = b;
            }
            else
            {
                a = 0;
            }
        }

    }

    private void backwardPassCurrentFiber()
    {
        int a = 0;
        for (int k = (int) imageSize.getSizeZ() - 1; k >= 0; k--)
        {
            if (a > 0)
                a--;
            double dk = buffer[k] / squaredSizeZ;
            double dk1 = k == (int) imageSize.getSizeZ() - 1 ? 0 : buffer[k + 1] / squaredSizeZ;
            if (dk > dk1 + 1d)
            {
                int b = (int) Math.ceil((dk - dk1 - 1d) / 2d);
                if (k - b < 0)
                    b = k;
                for (int n = a; n < b; n++)
                {
                    double nSq = n + 1;
                    nSq *= nSq;
                    double m = dk1 + nSq;
                    if (buffer[k - n] / squaredSizeZ <= m)
                        break;
                    if (m < getValueAt(currentI, currentJ, k - n) / squaredSizeZ)
                        setValueAt(currentI, currentJ, k - n, m * squaredSizeZ);
                }
                a = b;
            }
            else
            {
                a = 0;
            }
        }
    }

    private double getValueAt(int x, int y, int z)
    {
        return currentVolumePlanes[z][y * (int) imageSize.getSizeX() + x];
    }

    private void setValueAt(int x, int y, int z, double value)
    {
        currentVolumePlanes[z][y * (int) imageSize.getSizeX() + x] = value;
    }

}
