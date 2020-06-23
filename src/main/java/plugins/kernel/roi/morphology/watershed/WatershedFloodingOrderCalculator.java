package plugins.kernel.roi.morphology.watershed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import icy.sequence.Sequence;
import icy.sequence.VolumetricImageCursor;

public class WatershedFloodingOrderCalculator implements Callable<Void>
{
    public static class Builder
    {

        private Sequence distanceMap;
        private Sequence seedLabels;

        public Builder(Sequence distanceMap, Sequence seedLabels)
        {
            this.distanceMap = distanceMap;
            this.seedLabels = seedLabels;
        }

        public WatershedFloodingOrderCalculator create()
        {
            WatershedFloodingOrderCalculator flooding = new WatershedFloodingOrderCalculator();
            flooding.setDistanceMap(distanceMap);
            flooding.setSeedLabels(seedLabels);
            return flooding;
        }
    }

    private Sequence labels;
    private Sequence distanceMap;
    private int frame;

    private WatershedFloodingOrderCalculator()
    {
    }

    private void setSeedLabels(Sequence labels)
    {
        this.labels = labels;
    }

    private void setDistanceMap(Sequence distanceMap)
    {
        this.distanceMap = distanceMap;
    }

    public void setFrame(int t)
    {
        this.frame = t;
    }

    double frameMinH;
    double frameMaxH;
    List<LabeledPixel> framePixels;
    TreeSet<Double> frameHeights;

    @Override
    public Void call() throws Exception
    {
        VolumetricImageCursor frameDistanceCursor = new VolumetricImageCursor(distanceMap, frame);
        VolumetricImageCursor frameLabelCursor = new VolumetricImageCursor(labels, frame);

        frameMinH = Double.MAX_VALUE;
        frameMaxH = -Double.MAX_VALUE;

        int rowSize = labels.getSizeX();
        int planeSize = rowSize * labels.getSizeY();
        int frameSize = planeSize * labels.getSizeZ();

        framePixels = new ArrayList<>(frameSize);
        frameHeights = new TreeSet<Double>();

        int kOffset, jOffset;

        for (int k = 0; k < labels.getSizeZ(); k++)
        {
            if (Thread.interrupted())
                throw new InterruptedException();

            kOffset = k * planeSize;
            for (int j = 0; j < labels.getSizeY(); j++)
            {

                jOffset = j * rowSize;
                for (int i = 0; i < labels.getSizeX(); i++)
                {

                    double pixelHeight = frameDistanceCursor.get(i, j, k, 0);
                    int pixelLabel = (int) frameLabelCursor.get(i, j, k, 0);
                    if (pixelHeight == 0)
                    {
                        framePixels.add(null);
                    }
                    else
                    {
                        LabeledPixel pixel = new LabeledPixel(new WatershedPixel(new Position3D(i, j, k), pixelHeight),
                                pixelLabel);
                        addPixelNeighbors(pixel, planeSize, rowSize, kOffset, jOffset);
                        framePixels.add(pixel);
                        frameHeights.add(pixelHeight);

                        frameMinH = Math.min(frameMinH, pixelHeight);
                        frameMaxH = Math.max(frameMaxH, pixelHeight);
                    }
                }
            }
        }

        framePixels = framePixels.stream().filter(p -> p != null)
                .sorted(Comparator.comparingDouble(LabeledPixel::getHeight)).collect(Collectors.toList());
        Collections.reverse(framePixels);

        return null;
    }

    private void addPixelNeighbors(LabeledPixel node, int planeSize, int rowSize, int kOffset, int jOffset)
    {
        int i = node.pixel.position.x;
        int j = node.pixel.position.y;
        int k = node.pixel.position.z;
        // k-1
        if (k - 1 > 0)
        {
            // j-1
            if (j - 1 > 0)
            {
                // i-1
                if (i - 1 > 0)
                    setNeighborNode(node, (kOffset - planeSize) + (jOffset - rowSize) + (i - 1));
                // i
                setNeighborNode(node, (kOffset - planeSize) + (jOffset - rowSize) + i);
                // i+1
                if (i + 1 < rowSize)
                    setNeighborNode(node, (kOffset - planeSize) + (jOffset - rowSize) + i + 1);
            }

            // j
            // i-1
            if (i - 1 > 0)
                setNeighborNode(node, (kOffset - planeSize) + (jOffset) + (i - 1));
            // i
            setNeighborNode(node, (kOffset - planeSize) + (jOffset) + i);
            // i+1
            if (i + 1 < rowSize)
                setNeighborNode(node, (kOffset - planeSize) + (jOffset) + i + 1);

            // j+1
            if (j + 1 < labels.getSizeY())
            {
                // i-1
                if (i - 1 > 0)
                    setNeighborNode(node, (kOffset - planeSize) + (jOffset + rowSize) + (i - 1));
                // i
                setNeighborNode(node, (kOffset - planeSize) + (jOffset + rowSize) + i);
                // i+1
                if (i + 1 < rowSize)
                    setNeighborNode(node, (kOffset - planeSize) + (jOffset + rowSize) + i + 1);
            }
        }

        // k
        // j-1
        if (j - 1 > 0)
        {
            // i-1
            if (i - 1 > 0)
                setNeighborNode(node, (kOffset) + (jOffset - rowSize) + (i - 1));
            // i
            setNeighborNode(node, (kOffset) + (jOffset - rowSize) + i);
            // i+1
            if (i + 1 < rowSize)
                setNeighborNode(node, (kOffset) + (jOffset - rowSize) + i + 1);
        }
        // j
        // i-1
        if (i - 1 > 0)
            setNeighborNode(node, (kOffset) + (jOffset) + (i - 1));
    }

    private void setNeighborNode(LabeledPixel currentNode, int neighborPosition)
    {
        LabeledPixel neighborNode = framePixels.get(neighborPosition);
        currentNode.neighbors.add(neighborNode);
        neighborNode.neighbors.add(currentNode);
    }

    public List<LabeledPixel> getFrameFloodingOrder()
    {
        return Collections.unmodifiableList(framePixels);
    }

    public Set<Double> getFrameHeights()
    {
        return Collections.unmodifiableSet(frameHeights);
    }

    public double getFrameMinHeight()
    {
        return frameMinH;
    }

    public double getFrameMaxHeight()
    {
        return frameMaxH;
    }

}
