package plugins.kernel.roi.tool.morphology;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import icy.image.IcyBufferedImage;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.VolumetricImage;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
import icy.type.TypeUtil;
import icy.type.dimension.Dimension3D;
import icy.type.dimension.Dimension5D;
import icy.util.Random;
import plugins.kernel.roi.roi2d.ROI2DArea;

public class ROIWatershed
{

    private Dimension5D imageSize;
    private Dimension3D pixelSize;
    private List<ROI> rois;
    private List<ROI> seeds;
    private boolean addNewBasins;

    public ROIWatershed(Dimension5D imageSize, Dimension3D pixelSize)
    {
        this.imageSize = imageSize;
        this.pixelSize = pixelSize;
        this.rois = new ArrayList<ROI>();
        this.seeds = new ArrayList<ROI>();
        this.addNewBasins = true;
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

    public void setAddNewBasins(boolean addNewBasins)
    {
        this.addNewBasins = addNewBasins;
    }

    public boolean isAddNewBasins()
    {
        return addNewBasins;
    }

    private Sequence distanceMap;
    private Sequence seedLabels;
    private WatershedStructure watershedStructure;
    private List<ROI> resultingROIs;

    public void compute()
    {
        computeDistanceTransform();
        prepareSeeds();
        createWatershedStructure();
        computeFlooding();
    }

    private void computeDistanceTransform()
    {
        ROIDistanceTransform dt = new ROIDistanceTransform(imageSize, pixelSize);
        dt.addAll(rois);
        this.distanceMap = dt.getDistanceMap();
    }

    int seedNumber;

    private void prepareSeeds()
    {
        seedNumber = 0;
        if (!seeds.isEmpty() && !addNewBasins)
        {
            seedLabels = new Sequence();
            for (int l = 0; l < imageSize.getSizeT(); l++)
            {
                VolumetricImage volume = new VolumetricImage();
                for (int k = 0; k < imageSize.getSizeZ(); k++)
                {
                    IcyBufferedImage plane = new IcyBufferedImage((int) imageSize.getSizeX(),
                            (int) imageSize.getSizeY(), 1, DataType.INT);
                    volume.setImage(k, plane);
                }
                seedLabels.addVolumetricImage(l, volume);
            }

            seedNumber = 0;
            for (ROI roi : seeds)
            {
                seedNumber++;
                DataIteratorUtil.set(new SequenceDataIterator(seedLabels, roi, true), seedNumber);
            }
        }
    }

    private void createWatershedStructure()
    {
        watershedStructure = new WatershedStructure(distanceMap, seedLabels);
    }

    int currentT;

    private void computeFlooding()
    {
        resultingROIs = new ArrayList<ROI>();
        for (currentT = 0; currentT < this.distanceMap.getSizeT(); currentT++)
        {
            floodCurrentFrame();
            createResultingROIs();
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
        currentLabel = (addNewBasins) ? WatershedNode.WATERSHED : seedNumber;

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
                node.setLabel(WatershedNode.MASK);
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
                        if (currentNode.getLabel() == WatershedNode.MASK)
                        {
                            if (addNewBasins)
                                currentNode.setLabel(neighborNode.getLabel());
                            else
                                setBasinLabel(currentNode, neighborNode.getLabel());
                        }
                        else if (currentNode.getLabel() != neighborNode.getLabel())
                        {
                            currentNode.setLabel(WatershedNode.WATERSHED);
                        }
                    }
                    else if (currentNode.getLabel() == WatershedNode.MASK)
                    {
                        currentNode.setLabel(neighborNode.getLabel());
                    }
                }
                else if (neighborNode.getLabel() == WatershedNode.MASK && neighborNode.getDistance() == 0d)
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

            if (currentNode.getLabel() == WatershedNode.MASK)
            {
                if (addNewBasins)
                    addNewBasin(currentNode);
                else
                {
                    int seedLabel = TypeUtil.toInt(seedLabels.getData(currentT, currentNode.getZ(), 0,
                            currentNode.getY(), currentNode.getX()));
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
        queue.add(currentNode);

        while (!queue.isEmpty())
        {
            WatershedNode node = queue.poll();
            List<WatershedNode> nodeNeighbors = node.getNeighbors();
            for (WatershedNode neighbor : nodeNeighbors)
            {
                if (neighbor.getLabel() == WatershedNode.MASK)
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
                if (neighbor.getLabel() == WatershedNode.MASK
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
                resultingROIs.add(roi);
            }
        }
    }

    public List<ROI> getResultRois()
    {
        if (resultingROIs == null)
            compute();

        return resultingROIs;
    }

}
