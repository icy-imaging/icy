package plugins.kernel.roi.morphology.watershed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import icy.sequence.Sequence;

public class WatershedStructure implements Iterable<WatershedNode>
{
    private List<WatershedNode> nodeStructure;
    private Set<Double> heights;
    private Sequence distanceMap;
    private Sequence seedLabels;
    private double[][] currentT;
    private int[][] currentTSeeds;
    private double minH;
    private double maxH;

    public WatershedStructure(Sequence distanceMap, Sequence seedLabels)
    {
        this.distanceMap = distanceMap;
        this.seedLabels = seedLabels;
    }

    public void computeStructureForTime(int t)
    {
        this.currentT = distanceMap.getDataXYZAsDouble(t, 0);
        this.currentTSeeds = (seedLabels != null) ? seedLabels.getDataXYZAsInt(t, 0) : null;
        this.minH = Double.MAX_VALUE;
        this.maxH = Double.MIN_VALUE;

        nodeStructure = new ArrayList<WatershedNode>(distanceMap.getSizeX() * distanceMap.getSizeY());
        heights = new TreeSet<Double>();

        int planeSize = distanceMap.getSizeX() * distanceMap.getSizeY();
        int rowSize = distanceMap.getSizeX();
        int kOffset;
        int jOffset;

        for (int k = 0; k < distanceMap.getSizeZ(); k++)
        {
            kOffset = k * planeSize;
            for (int j = 0; j < distanceMap.getSizeY(); j++)
            {
                jOffset = j * rowSize;
                for (int i = 0; i < distanceMap.getSizeX(); i++)
                {
                    double height = currentT[k][jOffset + i];
                    int label = (currentTSeeds != null) ? currentTSeeds[k][jOffset + i] : 0;
                    if (height == 0)
                    {
                        nodeStructure.add(null);
                        continue;
                    }

                    WatershedNode currentNode = new WatershedNode(i, j, k, height, new ArrayList<WatershedNode>(8));
                    nodeStructure.add(currentNode);
                    heights.add(currentNode.getHeight());
                    if (label > 0)
                    {
                        currentNode.setLabel(label);
                    }

                    if (height < minH)
                        minH = height;
                    if (height > maxH)
                        maxH = height;

                    // k-1
                    if (k - 1 > 0)
                    {
                        // j-1
                        if (j - 1 > 0)
                        {
                            // i-1
                            if (i - 1 > 0)
                                setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset - rowSize) + (i - 1));
                            // i
                            setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset - rowSize) + i);
                            // i+1
                            if (i + 1 < rowSize)
                                setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset - rowSize) + i + 1);
                        }

                        // j
                        // i-1
                        if (i - 1 > 0)
                            setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset) + (i - 1));
                        // i
                        setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset) + i);
                        // i+1
                        if (i + 1 < rowSize)
                            setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset) + i + 1);

                        // j+1
                        if (j + 1 < distanceMap.getSizeY())
                        {
                            // i-1
                            if (i - 1 > 0)
                                setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset + rowSize) + (i - 1));
                            // i
                            setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset + rowSize) + i);
                            // i+1
                            if (i + 1 < rowSize)
                                setNeighborhood(currentNode, (kOffset - planeSize) + (jOffset + rowSize) + i + 1);
                        }
                    }

                    // k
                    // j-1
                    if (j - 1 > 0)
                    {
                        // i-1
                        if (i - 1 > 0)
                            setNeighborhood(currentNode, (kOffset) + (jOffset - rowSize) + (i - 1));
                        // i
                        setNeighborhood(currentNode, (kOffset) + (jOffset - rowSize) + i);
                        // i+1
                        if (i + 1 < rowSize)
                            setNeighborhood(currentNode, (kOffset) + (jOffset - rowSize) + i + 1);
                    }
                    // j
                    // i-1
                    if (i - 1 > 0)
                        setNeighborhood(currentNode, (kOffset) + (jOffset) + (i - 1));

                    // currentNode.suffleNeighbors();
                }
            }
        }

        nodeStructure = nodeStructure.stream().filter(new Predicate<WatershedNode>()
        {
            @Override
            public boolean test(WatershedNode n)
            {
                return n != null;
            }
        }).sorted().collect(Collectors.<WatershedNode> toList());
        Collections.reverse(nodeStructure); // high distances are deeper
    }

    private void setNeighborhood(WatershedNode currentNode, int neighborNodePosition)
    {
        WatershedNode neighborNode = nodeStructure.get(neighborNodePosition);
        if (neighborNode != null)
        {
            currentNode.addNeighbor(neighborNode);
            neighborNode.addNeighbor(currentNode);
        }
    }

    public int size()
    {
        return nodeStructure.size();
    }

    @Override
    public Iterator<WatershedNode> iterator()
    {
        return nodeStructure.iterator();
    }

    public double getMinHeight()
    {
        return minH;
    }

    public double getMaxHeight()
    {
        return maxH;
    }

    private Set<Double> unmodifiableHeights;

    public Set<Double> getHeights()
    {
        if (unmodifiableHeights == null)
            unmodifiableHeights = Collections.unmodifiableSet(heights);
        return unmodifiableHeights;
    }

    public WatershedNode getNode(int nodeIndex)
    {
        return nodeStructure.get(nodeIndex);
    }

}
