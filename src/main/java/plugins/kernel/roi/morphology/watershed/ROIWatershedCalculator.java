package plugins.kernel.roi.morphology.watershed;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import icy.image.IcyBufferedImage;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceCursor;
import icy.sequence.SequenceDataIterator;
import icy.sequence.VolumetricImage;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.type.dimension.Dimension3D;
import icy.type.dimension.Dimension5D;
import icy.type.point.Point5D;
import icy.util.Random;
import plugins.kernel.image.filtering.GaussianFiltering;
import plugins.kernel.image.filtering.LocalMaxFiltering;
import plugins.kernel.image.filtering.convolution.ConvolutionException;
import plugins.kernel.roi.morphology.ROIDistanceTransformCalculator;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi2d.ROI2DPoint;

public class ROIWatershedCalculator
{

    private Dimension5D imageSize;
    private Dimension3D pixelSize;
    private List<ROI> rois;
    private List<ROI> seeds;
    private boolean addNewLabelsAllowed;

    public ROIWatershedCalculator(Dimension5D imageSize, Dimension3D pixelSize)
    {
        this.imageSize = imageSize;
        this.pixelSize = pixelSize;
        this.rois = new ArrayList<ROI>();
        this.seeds = new ArrayList<ROI>();
        this.addNewLabelsAllowed = true;
    }

    public <T extends ROI> void add(T roi)
    {
        rois.add(roi);
    }

    public <T extends ROI> void addAll(Collection<T> rois)
    {
        this.rois.addAll(rois);
    }

    public <T extends ROI> void addSeed(T roi)
    {
        seeds.add(roi);
    }

    public <T extends ROI> void addAllSeeds(Collection<T> rois)
    {
        seeds.addAll(rois);
    }

    public void setNewLabelsAllowed(boolean addNewLabelsAllowed)
    {
        this.addNewLabelsAllowed = addNewLabelsAllowed;
    }

    public boolean isNewLabelsAllowed()
    {
        return addNewLabelsAllowed;
    }

    private List<ROI> resultingRois;

    public void compute() throws InterruptedException
    {
        computeDistanceMap();
        prepareSeeds();
        computeFlooding();
    }

    private Sequence distanceMap;

    private void computeDistanceMap() throws InterruptedException
    {
        ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, true);
        dt.addAll(rois);
        this.distanceMap = dt.getDistanceMap();
    }

    private Sequence labels;
    int labelCount;
    private List<ROI> seedRois;

    private void prepareSeeds() throws InterruptedException
    {
        seedRois = new ArrayList<ROI>();
        labelCount = 0;

        initializeSeedLabels();
        if (seeds.isEmpty())
        {
            if (!isNewLabelsAllowed())
            {
                Sequence localMax = getLocalMaxima();
                SequenceCursor seedCursor = new SequenceCursor(labels);
                for (SequenceDataIterator localMaxIt = new SequenceDataIterator(localMax); !localMaxIt
                        .done(); localMaxIt.next())
                {
                    if (Thread.interrupted())
                        throw new InterruptedException();
                    if (localMaxIt.get() > 0)
                    {
                        labelCount++;
                        seedCursor.set(localMaxIt.getPositionX(), localMaxIt.getPositionY(), localMaxIt.getPositionZ(),
                                localMaxIt.getPositionT(), 0, labelCount);

                        ROI seedRoi = new ROI2DPoint(new Point5D.Integer(localMaxIt.getPositionX(),
                                localMaxIt.getPositionY(), localMaxIt.getPositionZ(), localMaxIt.getPositionT(),
                                localMaxIt.getPositionC()));
                        seedRoi.setName("Seed " + labelCount);
                        seedRois.add(seedRoi);
                    }
                }
                seedCursor.commitChanges();
            }
        }
        else
        {
            for (ROI roi : seeds)
            {
                labelCount++;
                DataIteratorUtil.set(new SequenceDataIterator(labels, roi, true), labelCount);
                roi.setName("Seed " + labelCount);
                seedRois.add(roi);
            }
        }
    }

    private void initializeSeedLabels() throws InterruptedException
    {
        labels = new Sequence();
        for (int l = 0; l < imageSize.getSizeT(); l++)
        {
            if (Thread.interrupted())
                throw new InterruptedException();

            VolumetricImage volume = new VolumetricImage();
            for (int k = 0; k < imageSize.getSizeZ(); k++)
            {
                if (Thread.interrupted())
                    throw new InterruptedException();

                IcyBufferedImage plane = new IcyBufferedImage((int) imageSize.getSizeX(), (int) imageSize.getSizeY(), 1,
                        DataType.INT);
                volume.setImage(k, plane);
            }
            labels.addVolumetricImage(l, volume);
        }
    }

    private Sequence getLocalMaxima() throws RuntimeException, InterruptedException
    {
        LocalMaxFiltering localMaxFiltering = LocalMaxFiltering.create(getSmoothedDistanceMap(), 3);
        localMaxFiltering.computeFiltering();
        return localMaxFiltering.getFilteredSequence();
    }

    private Sequence getSmoothedDistanceMap()
            throws IllegalArgumentException, ConvolutionException, InterruptedException
    {
        GaussianFiltering smoothingFilter = GaussianFiltering.create(this.distanceMap, new double[] {2, 2, 2});
        smoothingFilter.computeFiltering();
        return smoothingFilter.getFilteredSequence();
    }

    private void createWatershedStructure()
    {

        watershedStructure = new WatershedStructure(distanceMap, labels);
    }

    int currentT;

    private void computeFlooding() throws InterruptedException
    {

        WatershedFloodingOrderCalculator flooding = new WatershedFloodingOrderCalculator.Builder(distanceMap, labels).create();
        for (int t = 0; t < this.distanceMap.getSizeT(); t++)
        {
            if (Thread.interrupted())
                throw new InterruptedException();

            try
            {
                computeFrameWatershed(flooding, t);
            }
            catch (InterruptedException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error while flooding: " + e.getMessage(), e);
            }
        }

        resultingRois = new ArrayList<>();
        // TODO use label extractor to create ROIs

        // old code

        resultingRois = new ArrayList<ROI>();
        for (currentT = 0; currentT < this.distanceMap.getSizeT(); currentT++)
        {
            if (Thread.interrupted())
                throw new InterruptedException();

            floodCurrentFrame();
            createResultingROIs();
        }
    }

    private void computeFrameWatershed(WatershedFloodingOrderCalculator floodingOrderCalulator, int t) throws InterruptedException
    {
        try
        {
            floodingOrderCalulator.setFrame(t);
            floodingOrderCalulator.call();

            floodFrame(t, floodingOrderCalulator);
            
        }
        catch (InterruptedException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception while performing flooding: " + e.getMessage(), e);
        }
    }

    private void floodFrame(int t, WatershedFloodingOrderCalculator floodingOrderCalulator)
    {
        List<Double> heights = floodingOrderCalulator.frameHeights.stream().collect(Collectors.toList());
        Collections.reverse(heights);
        AtomicInteger pixelIndex = new AtomicInteger();
        for (Double currentHeight: heights) {
            findFloodingPixels(floodingOrderCalulator, pixelIndex);
        }
    }

    private Queue<WatershedNode> queue = new LinkedList<WatershedNode>();
    private int currentLabel;

    private double currentHeight;
    private int heightIndex1;
    private int heightIndex2;

    private void floodCurrentFrame()
    {
        watershedStructure.computeStructureForTime(currentT);
        currentLabel = (addNewLabelsAllowed) ? WatershedNode.WATERSHED : labelCount;

        heightIndex1 = 0;
        heightIndex2 = 0;

        List<Double> heights = new LinkedList<Double>(watershedStructure.getHeights());
        Collections.reverse(heights);
        for (double currentHeight : heights)
        {
            this.currentHeight = currentHeight;
            findNodesForCurrentHeight();
            extendBasins();
            processNextHeight();
        }
    }

    private void findNodesForCurrentHeight()
    {
        for (int nodeIndex = heightIndex1; nodeIndex < watershedStructure.size(); nodeIndex++)
        {
            WatershedNode node = watershedStructure.getNode(nodeIndex);
            if (node.getHeight() != currentHeight)
            {
                heightIndex1 = nodeIndex;
                break;
            }

            if (node.getLabel() <= WatershedNode.NO_LABEL)
            {
                node.setLabel(WatershedNode.TO_BE_MARKED);
            }

            for (WatershedNode neighbor : node.getNeighbors())
            {
                if (neighbor.getLabel() > WatershedNode.NO_LABEL)
                {
                    node.setDistance(1); // TODO fix to take into account pixel size
                    queue.add(node);
                    break;
                }
            }
        }
    }

    private void extendBasins()
    {
        double currentDistance = 1; // TODO fix to take into account pixel size
        queue.add(WatershedNode.FLAG_NODE);
        while (true)
        {
            WatershedNode currentNode = queue.poll();

            if (currentNode.getLabel() == WatershedNode.HEIGHT_FLAG)
            { // Finished all nodes on current distance
                if (queue.isEmpty())
                { // Finished queue
                    break;
                }
                else
                { // Other nodes still exist at a farther distance
                    queue.add(WatershedNode.FLAG_NODE);
                    currentDistance++;
                    currentNode = queue.poll();
                }
            }

            for (WatershedNode neighborNode : currentNode.getNeighbors())
            {
                if (neighborNode.getDistance() <= currentDistance && neighborNode.getLabel() > WatershedNode.NO_LABEL)
                {
                    if (neighborNode.getLabel() > WatershedNode.WATERSHED)
                    {
                        if (currentNode.getLabel() == WatershedNode.TO_BE_MARKED)
                        {
                            if (addNewLabelsAllowed)
                                currentNode.setLabel(neighborNode.getLabel());
                            else
                                setBasinLabel(currentNode, neighborNode.getLabel());
                        }
                        else if (currentNode.getLabel() != neighborNode.getLabel())
                        {
                            currentNode.setLabel(WatershedNode.WATERSHED);
                        }
                    }
                    else if (currentNode.getLabel() == WatershedNode.TO_BE_MARKED)
                    {
                        currentNode.setLabel(neighborNode.getLabel());
                    }
                }
                else if (neighborNode.getLabel() == WatershedNode.TO_BE_MARKED && neighborNode.getDistance() == 0d)
                {
                    neighborNode.setDistance(currentDistance + 1);
                    queue.add(neighborNode);
                }
            }

        }
    }

    private void processNextHeight()
    {
        for (int nodeIndex = heightIndex2; nodeIndex < watershedStructure.size(); nodeIndex++)
        {
            WatershedNode currentNode = watershedStructure.getNode(nodeIndex);

            if (currentNode.getHeight() != currentHeight)
            {
                heightIndex2 = nodeIndex;
                break;
            }

            currentNode.setDistance(0);

            if (currentNode.getLabel() == WatershedNode.TO_BE_MARKED)
            {
                if (addNewLabelsAllowed)
                    addNewBasin(currentNode);
                else
                {
                    int seedLabel = TypeUtil.toInt(
                            labels.getData(currentT, currentNode.getZ(), 0, currentNode.getY(), currentNode.getX()));
                    if (seedLabel > 0)
                        setBasinLabel(currentNode, seedLabel);
                }
            }
        }
    }

    private void addNewBasin(WatershedNode currentNode)
    {
        currentLabel++;
        currentNode.setLabel(currentLabel);
        seedRois.add(new ROI2DPoint(
                new Point5D.Integer(currentNode.getX(), currentNode.getY(), currentNode.getZ(), currentT, 0)));
        queue.add(currentNode);

        while (!queue.isEmpty())
        {
            WatershedNode node = queue.poll();
            List<WatershedNode> nodeNeighbors = node.getNeighbors();
            for (WatershedNode neighbor : nodeNeighbors)
            {
                if (neighbor.getLabel() == WatershedNode.TO_BE_MARKED)
                {
                    neighbor.setLabel(currentLabel);
                    queue.add(neighbor);
                }
            }
        }
    }

    private Queue<WatershedNode> queue1 = new LinkedList<WatershedNode>();

    private void setBasinLabel(WatershedNode currentNode, int seedLabel)
    {
        currentNode.setLabel(seedLabel);
        currentNode.setDistance(0);
        queue1.add(currentNode);

        while (!queue1.isEmpty())
        {
            WatershedNode node = queue1.poll();
            List<WatershedNode> nodeNeighbors = node.getNeighbors();
            for (WatershedNode neighbor : nodeNeighbors)
            {
                if (neighbor.getLabel() == WatershedNode.TO_BE_MARKED
                        || (neighbor.getLabel() == WatershedNode.NO_LABEL && neighbor.getHeight() > node.getHeight()))
                {
                    neighbor.setLabel(seedLabel);
                    neighbor.setDistance(0);
                    queue1.add(neighbor);
                }
            }
        }
    }

    private void createResultingROIs()
    {
        if (imageSize.getSizeZ() == 1)
        {
            ArrayList<boolean[]> masks = new ArrayList<boolean[]>(currentLabel + 1);
            int arraySize = (int) imageSize.getSizeX() * (int) imageSize.getSizeY();
            for (int i = 0; i < currentLabel + 1; i++)
            {
                masks.add(new boolean[arraySize]);
            }
            for (WatershedNode watershedNode : watershedStructure)
            {
                int maskIndex = watershedNode.getLabel() - 1;
                if (maskIndex >= 0)
                {
                    masks.get(maskIndex)[watershedNode.getY() * (int) imageSize.getSizeX()
                            + watershedNode.getX()] = true;

                }
                else if (maskIndex < -1)
                {
                    masks.get(currentLabel)[watershedNode.getY() * (int) imageSize.getSizeX()
                            + watershedNode.getX()] = true;
                }
            }
            for (int i = 0; i <= currentLabel; i++)
            {
                ROI2DArea roi = new ROI2DArea(new BooleanMask2D(
                        new Rectangle((int) imageSize.getSizeX(), (int) imageSize.getSizeY()), masks.get(i)));
                int r, g, b;
                r = Random.nextInt(256);
                g = Random.nextInt(256);
                b = (765 - r - g) % 256;
                if (i < currentLabel)
                    roi.setColor(new Color(r, g, b));
                else
                {
                    roi.setColor(Color.WHITE);
                    roi.setOpacity(1);
                }
                resultingRois.add(roi);
            }
        }
    }

    public List<ROI> getResultRois() throws InterruptedException
    {
        if (resultingRois == null)
            compute();

        return resultingRois;
    }

    public List<ROI> getUsedSeeds()
    {
        return seedRois;
    }

}
