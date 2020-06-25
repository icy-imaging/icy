package plugins.kernel.roi.morphology.watershed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import icy.sequence.Sequence;
import icy.sequence.VolumetricImageCursor;

public class FloodingStructure
{
    public static class Builder
    {
        private Sequence distanceMap;
        private Sequence labelSequence;
        private int frame;

        public Builder(Sequence distanceMap, Sequence labelSequence)
        {
            this.distanceMap = distanceMap;
            this.labelSequence = labelSequence;
        }

        public Builder setFrame(int frame)
        {
            this.frame = frame;
            return this;
        }

        public FloodingStructure build()
        {
            FloodingStructure structure = new FloodingStructure();
            structure.setDistanceMap(distanceMap);
            structure.setLabelSequence(labelSequence);
            structure.setFrame(frame);
            structure.compute();
            return structure;
        }
    }

    private FloodingStructure()
    {
    }

    private Sequence distanceMap;

    public void setDistanceMap(Sequence distanceMap)
    {
        this.distanceMap = distanceMap;
    }

    private Sequence labelSequence;

    public void setLabelSequence(Sequence labelSequence)
    {
        this.labelSequence = labelSequence;
    }

    private int frame;

    public void setFrame(int frame)
    {
        this.frame = frame;
    }

    private List<LabeledPixel> labeledPixels;
    private Set<Double> heights;

    public void compute()
    {
        finalFloodingPixels = null;
        finalHeights = null;

        VolumetricImageCursor distanceMapCursor = new VolumetricImageCursor(distanceMap, frame);
        VolumetricImageCursor labelSequenceCursor = new VolumetricImageCursor(labelSequence, frame);

        int rowSize = distanceMap.getSizeX();
        int planeSize = rowSize * distanceMap.getSizeY();
        int volumeSize = planeSize * distanceMap.getSizeZ();

        labeledPixels = new ArrayList<>(volumeSize);
        heights = new TreeSet<>();
        List<LabeledPixel> seedPixels = new ArrayList<>();
        double maxHeight = -Double.MAX_VALUE;

        int kOffset, jOffset;
        for (int k = 0; k < distanceMap.getSizeZ(); k++)
        {
            kOffset = k * planeSize;
            for (int j = 0; j < distanceMap.getSizeY(); j++)
            {
                jOffset = j * rowSize;
                for (int i = 0; i < distanceMap.getSizeX(); i++)
                {
                    double height = distanceMapCursor.get(i, j, k, 0);
                    if (height == 0d)
                    {
                        labeledPixels.add(null);
                        continue;
                    }

                    LabeledPixel currentPixel = new LabeledPixel(new Point3D(i, j, k), height);
                    labeledPixels.add(currentPixel);

                    heights.add(height);
                    if (maxHeight < height)
                    {
                        maxHeight = height;
                    }

                    int label = (int) labelSequenceCursor.get(i, j, k, 0);
                    if (label > 0)
                    {
                        currentPixel.setLabel(label);
                        seedPixels.add(currentPixel);
                    }

                    if (k - 1 > 0)
                    {
                        // j-1
//                        if (j - 1 > 0)
//                        {
//                            // i-1
//                            if (i - 1 > 0)
//                                setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset - rowSize) + (i - 1));
//                            // i
//                            setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset - rowSize) + i);
//                            // i+1
//                            if (i + 1 < rowSize)
//                                setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset - rowSize) + i + 1);
//                        }

                        // j
                        // i-1
//                        if (i - 1 > 0)
//                            setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset) + (i - 1));
                        // i
                        setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset) + i);
                        // i+1
//                        if (i + 1 < rowSize)
//                            setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset) + i + 1);

                        // j+1
//                        if (j + 1 < distanceMap.getSizeY())
//                        {
//                            // i-1
//                            if (i - 1 > 0)
//                                setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset + rowSize) + (i - 1));
//                            // i
//                            setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset + rowSize) + i);
//                            // i+1
//                            if (i + 1 < rowSize)
//                                setNeighborhood(currentPixel, (kOffset - planeSize) + (jOffset + rowSize) + i + 1);
//                        }
                    }

                    // k
                    // j-1
                    if (j - 1 > 0)
                    {
                        // i-1
//                        if (i - 1 > 0)
//                            setNeighborhood(currentPixel, (kOffset) + (jOffset - rowSize) + (i - 1));
                        // i
                        setNeighborhood(currentPixel, (kOffset) + (jOffset - rowSize) + i);
                        // i+1
//                        if (i + 1 < rowSize)
//                            setNeighborhood(currentPixel, (kOffset) + (jOffset - rowSize) + i + 1);
                    }
                    // j
                    // i-1
                    if (i - 1 > 0)
                        setNeighborhood(currentPixel, (kOffset) + (jOffset) + (i - 1));

                }
            }
        }
        
        if (!seedPixels.isEmpty())
        {
            heights.add(maxHeight + 1);

            for (LabeledPixel p : seedPixels)
            {
                p.setHeight(maxHeight + 1);
            }
        }

        Comparator.comparingDouble(LabeledPixel::getHeight);
        labeledPixels = labeledPixels.stream().filter(p -> p != null).sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private void setNeighborhood(LabeledPixel pixel, int neighborPosition)
    {
        LabeledPixel neighborPixel = labeledPixels.get(neighborPosition);
        if (neighborPixel != null)
        {
            pixel.addNeighbor(neighborPixel);
            neighborPixel.addNeighbor(pixel);
        }
    }

    private List<LabeledPixel> finalFloodingPixels;

    /**
     * @return flooding pixels ordered in decreasing height.
     */
    public List<LabeledPixel> getFloodingPixels()
    {
        if (finalFloodingPixels == null)
        {
            finalFloodingPixels = Collections.unmodifiableList(labeledPixels);
        }
        return finalFloodingPixels;
    }

    private Set<Double> finalHeights;

    /**
     * @return Set of heights present in the flooding pixels. Heights will be traversed in increasing order when using iterators.
     */
    public Set<Double> getHeights()
    {
        if (finalHeights == null)
        {
            finalHeights = Collections.unmodifiableSet(heights);
        }
        return finalHeights;
    }

}
