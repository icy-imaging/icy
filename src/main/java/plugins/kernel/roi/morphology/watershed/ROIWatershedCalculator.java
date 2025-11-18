package plugins.kernel.roi.morphology.watershed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import icy.image.IcyBufferedImage;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.VolumetricImage;
import icy.sequence.VolumetricImageCursor;
import icy.type.DataIteratorUtil;
import icy.type.DataType;
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
import plugins.kernel.roi.roi3d.ROI3DArea;

public class ROIWatershedCalculator implements Callable<Void>
{
    public static class Builder
    {
        private Dimension5D imageSize;
        private Dimension3D pixelSize;
        private List<ROI> rois;
        private List<ROI> seeds;
        private boolean newBasinsAllowed;

        public Builder(Dimension5D imageSize, Dimension3D pixelSize)
        {
            Objects.requireNonNull(imageSize);
            Objects.requireNonNull(pixelSize);
            this.imageSize = imageSize;
            this.pixelSize = pixelSize;
            this.rois = new ArrayList<ROI>();
            this.seeds = new ArrayList<ROI>();
            this.newBasinsAllowed = true;
        }

        public <T extends ROI> Builder addObject(T roi)
        {
            Objects.requireNonNull(roi);
            this.rois.add(roi);
            return this;
        }

        public <T extends ROI> Builder addObjects(Collection<T> rois)
        {
            Objects.requireNonNull(rois);
            this.rois.addAll(rois);
            return this;
        }

        public <T extends ROI> Builder addSeed(T roi)
        {
            Objects.requireNonNull(roi);
            this.seeds.add(roi);
            return this;
        }

        public <T extends ROI> Builder addSeeds(Collection<T> rois)
        {
            Objects.requireNonNull(rois);
            this.seeds.addAll(rois);
            return this;
        }

        public Builder setNewBasinsAllowed(boolean allow)
        {
            this.newBasinsAllowed = allow;
            return this;
        }

        public ROIWatershedCalculator build()
        {
            ROIWatershedCalculator calculator = new ROIWatershedCalculator();
            calculator.setImageSize(imageSize);
            calculator.setPixelSize(pixelSize);
            calculator.setDomainRois(rois);
            calculator.setSeedRois(seeds);
            calculator.setNewBasinsAllowed(newBasinsAllowed);
            return calculator;
        }
    }

    private Dimension3D pixelSize;
    private Dimension5D imageSize;
    private List<ROI> seedRois;
    private List<ROI> domainRois;
    private boolean newBasinsAllowed;

    private ROIWatershedCalculator()
    {
    }

    private void setImageSize(Dimension5D imageSize)
    {
        this.imageSize = imageSize;
    }

    private void setPixelSize(Dimension3D pixelSize)
    {
        this.pixelSize = pixelSize;
    }

    private void setDomainRois(List<ROI> domainRois)
    {
        this.domainRois = domainRois;
    }

    private void setSeedRois(List<ROI> seedRois)
    {
        this.seedRois = seedRois;
    }

    private void setNewBasinsAllowed(boolean newBasinsAllowed)
    {
        this.newBasinsAllowed = newBasinsAllowed;
    }

    @Override
    public Void call() throws Exception
    {
        labeledRois = null;
        computeDomainDistanceMap();
        prepareSeeds();
        computeWatershedOnFrames();
        return null;
    }

    private Sequence domainDistanceMap;

    private void computeDomainDistanceMap() throws InterruptedException
    {
        ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, true);
        dt.addAll(domainRois);
        try
        {
            this.domainDistanceMap = dt.getDistanceMap();
        }
        catch (InterruptedException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error computing domain distance map: " + e.getMessage(), e);
        }
    }

    private void prepareSeeds() throws InterruptedException
    {
        if (seedRois.isEmpty() && !newBasinsAllowed)
        {
            seedRois.addAll(getDomainLocalMaximumROIs());
        }

        initializeLabelSequence();
        int seedLabel = 1;
        for (ROI roi : seedRois)
        {
            addSeedToLabelSequence(roi, seedLabel++);
        }
    }

    private List<? extends ROI> getDomainLocalMaximumROIs()
            throws IllegalArgumentException, ConvolutionException, InterruptedException
    {
        LocalMaxFiltering localMaxFiltering = LocalMaxFiltering.create(getSmoothedDomainDistanceMap(), 3);
        localMaxFiltering.computeFiltering();

        List<ROI> localMaximaRois = new ArrayList<>();
        for (SequenceDataIterator localMaxIt = new SequenceDataIterator(
                localMaxFiltering.getFilteredSequence()); !localMaxIt.done(); localMaxIt.next())
        {
            if (Thread.interrupted())
                throw new InterruptedException("ROI local maxima computation interrupted.");

            if (localMaxIt.get() > 0)
            {
                localMaximaRois
                        .add(new ROI2DPoint(new Point5D.Integer(localMaxIt.getPositionX(), localMaxIt.getPositionY(),
                                localMaxIt.getPositionZ(), localMaxIt.getPositionT(), localMaxIt.getPositionC())));
            }
        }
        return localMaximaRois;
    }

    private Sequence getSmoothedDomainDistanceMap()
            throws IllegalArgumentException, ConvolutionException, InterruptedException
    {
        GaussianFiltering smoothingFilter = GaussianFiltering.create(this.domainDistanceMap, new double[] {2, 2, 2});
        try
        {
            smoothingFilter.computeFiltering();
        }
        catch (ConvolutionException e)
        {
            System.err.println("z sigma 2 too large.. trying 1");
            try
            {
                smoothingFilter = GaussianFiltering.create(this.domainDistanceMap, new double[] {2, 2, 1});
                smoothingFilter.computeFiltering();
            }
            catch (ConvolutionException e1)
            {
                System.err.println("z sigma 1 too large.. using original distance map");
                return this.domainDistanceMap;
            }
        }

        return smoothingFilter.getFilteredSequence();
    }

    private Sequence labelSequence;

    private void initializeLabelSequence() throws InterruptedException
    {
        labelSequence = new Sequence("labels");
        for (int l = 0; l < imageSize.getSizeT(); l++)
        {
            if (Thread.interrupted())
                throw new InterruptedException("ROI watershed descriptor computation interrupted.");

            VolumetricImage volume = new VolumetricImage();
            for (int k = 0; k < imageSize.getSizeZ(); k++)
            {
                if (Thread.interrupted())
                    throw new InterruptedException("ROI watershed descriptor computation interrupted.");

                IcyBufferedImage plane = new IcyBufferedImage((int) imageSize.getSizeX(), (int) imageSize.getSizeY(), 1,
                        DataType.INT);
                volume.setImage(k, plane);
            }
            labelSequence.addVolumetricImage(l, volume);
        }
    }

    private void addSeedToLabelSequence(ROI seedRoi, int label) throws InterruptedException
    {
        DataIteratorUtil.set(new SequenceDataIterator(labelSequence, seedRoi, true), label);
    }

    private Map<Integer, List<LabeledPixel>> labeledPixels;

    private void computeWatershedOnFrames()
    {
        labeledPixels = new HashMap<Integer, List<LabeledPixel>>(labelSequence.getSizeT());
        FloodingStructure.Builder floodingStructureBuilder = new FloodingStructure.Builder(domainDistanceMap,
                labelSequence);
        for (int frame = 0; frame < labelSequence.getSizeT(); frame++)
        {
            floodingStructureBuilder.setFrame(frame);
            FloodingStructure frameFloodingStructure = floodingStructureBuilder.build();
            computeFrameFlooding(frameFloodingStructure, frame);
            updateLabelSequence(frame, frameFloodingStructure);
            labeledPixels.put(frame, frameFloodingStructure.getFloodingPixels());
        }
    }

    private void computeFrameFlooding(FloodingStructure frameFloodingStructure, int frame)
    {
        AtomicInteger labelGenerator = new AtomicInteger(seedRois.size());

        List<Double> heights = frameFloodingStructure.getHeights().stream().collect(Collectors.toList());
        Collections.reverse(heights);
        AtomicInteger heightPixelIndex = new AtomicInteger(0);
        AtomicInteger nextPixelIndex = new AtomicInteger(0);
        Queue<LabeledPixel> pendingPixels = new LinkedList<>();
        for (double height : heights)
        {
            List<LabeledPixel> pixelsAtHeight = findPixelsAtHeight(frameFloodingStructure.getFloodingPixels(), height,
                    heightPixelIndex);
            pendingPixels.addAll(pixelsAtHeight);
            extendBasins(pendingPixels);
            finishCurrentHeight(frame, frameFloodingStructure.getFloodingPixels(), height, nextPixelIndex,
                    labelGenerator);
        }
    }

    private List<LabeledPixel> findPixelsAtHeight(List<LabeledPixel> floodingPixels, double height,
            AtomicInteger startingPixelIndex)
    {
        List<LabeledPixel> candidatePixelsAtHeight = new LinkedList<LabeledPixel>();
        for (int pixelIndex = startingPixelIndex.get(); pixelIndex < floodingPixels.size(); pixelIndex++)
        {
            LabeledPixel pixel = floodingPixels.get(pixelIndex);

            if (pixel.getHeight() < height)
            {
                startingPixelIndex.set(pixelIndex);
                break;
            }

            if (!pixel.isLabeled())
            {
                pixel.setToBeLabeled();
            }

            for (LabeledPixel neighborPixel : pixel.getNeighbors())
            // Check if pixel can be labeled
            {
                if (neighborPixel.isLabeled())
                // pixel has a labeled neighbor, thus pixel can be labeled
                {
                    // double distance = neighborPixel.getHeight() - pixel.getHeight();
                    pixel.setLevel(1);
                    candidatePixelsAtHeight.add(pixel);
                    break;
                }
            }

        }
        return candidatePixelsAtHeight;
    }

    private void extendBasins(Queue<LabeledPixel> pendingPixels)
    {
        LabeledPixel flagPixel = new LabeledPixel(new Point3D(0, 0, 0), -1);
        pendingPixels.add(flagPixel);
        int currentLevel = 1;

        while (true)
        {
            LabeledPixel currentPixel = pendingPixels.poll();

            if (currentPixel == flagPixel)
            // Finished current level
            {
                if (pendingPixels.isEmpty())
                // Finished all pending pixels
                {
                    break;
                }
                else
                // Other pixel are still pending
                {
                    pendingPixels.add(flagPixel);
                    currentLevel++;
                    currentPixel = pendingPixels.poll();
                }
            }

            boolean shouldExtend = false;
            int neighborLabel = 0;
            boolean isBasinBorder = false;
            LabeledPixel bestNeighbor = null;
            double bestDistance = Double.NaN;
            for (LabeledPixel neighborPixel : currentPixel.getNeighbors())
            {
                if (neighborPixel.getLevel() <= currentLevel && neighborPixel.isLabeled())
                {

                    if (currentPixel.isToBeLabeled())
                    {
                        shouldExtend = true;
                    }

                    if (neighborLabel == 0)
                    {
                        neighborLabel = neighborPixel.getLabel();
                    }
                    else if (neighborLabel != neighborPixel.getLabel())
                    {
                        isBasinBorder = true;
                    }

                    double distance = neighborPixel.getHeight() - currentPixel.getHeight();
                    if (bestNeighbor == null)
                    {
                        bestNeighbor = neighborPixel;
                        bestDistance = distance;
                    }
                    else if (distance > bestDistance)
                    {
                        bestNeighbor = neighborPixel;
                        bestDistance = distance;
                    }

                }
                else if (neighborPixel.isToBeLabeled() && neighborPixel.getLevel() == 0)
                {
                    neighborPixel.setLevel(currentLevel + 1);
                    pendingPixels.add(neighborPixel);
                }
            }

            if (shouldExtend || isBasinBorder)
            {
                currentPixel.setLabel(bestNeighbor.getLabel());
                if (!newBasinsAllowed)
                {
                    floodBasin(currentPixel, bestNeighbor.getLabel());
                }
            }
        }
    }

    private void finishCurrentHeight(int frame, List<LabeledPixel> floodingPixels, double height,
            AtomicInteger nextPixelIndex, AtomicInteger labelGenerator)
    {
        VolumetricImageCursor labelCursor = new VolumetricImageCursor(labelSequence, frame);
        for (int pixelIndex = nextPixelIndex.get(); pixelIndex < floodingPixels.size(); pixelIndex++)
        {
            LabeledPixel pixel = floodingPixels.get(pixelIndex);
            if (pixel.getHeight() < height)
            {
                nextPixelIndex.set(pixelIndex);
                break;
            }

            pixel.setLevel(0);

            if (pixel.isToBeLabeled())
            {
                if (newBasinsAllowed)
                {
                    floodBasin(pixel, labelGenerator.incrementAndGet());
                }
                else
                {
                    int pixelLabel = (int) labelCursor.get(pixel.getPosition().x, pixel.getPosition().y,
                            pixel.getPosition().z, 0);
                    if (pixelLabel > 0)
                    {
                        floodBasin(pixel, pixelLabel);
                    }
                }
            }
        }
    }

    private void floodBasin(LabeledPixel startPixel, int label)
    {
        startPixel.setLabel(label);

        Queue<LabeledPixel> pendingBasinPixels = new LinkedList<>();
        pendingBasinPixels.add(startPixel);
        while (!pendingBasinPixels.isEmpty())
        {
            LabeledPixel currentPixel = pendingBasinPixels.poll();

            for (LabeledPixel neighborPixel : currentPixel.getNeighbors())
            {
                if (neighborPixel.isToBeLabeled()
                        || (neighborPixel.isNoLabel() && neighborPixel.getHeight() >= currentPixel.getHeight()))
                {
                    neighborPixel.setLabel(label);
                    neighborPixel.setLevel(startPixel.getLevel());
                    pendingBasinPixels.add(neighborPixel);
                }
            }
        }
    }

    private void updateLabelSequence(int frame, FloodingStructure frameFloodingStructure)
    {
        VolumetricImageCursor labelCursor = new VolumetricImageCursor(labelSequence, frame);
        for (LabeledPixel labeledPixel : frameFloodingStructure.getFloodingPixels())
        {
            labelCursor.set(labeledPixel.getPosition().x, labeledPixel.getPosition().y, labeledPixel.getPosition().z, 0,
                    labeledPixel.getLabel());
        }
        labelCursor.commitChanges();
    }

    public Sequence getLabelSequence()
    {
        return labelSequence;
    }

    public List<ROI> getSeeds()
    {
        return seedRois;
    }

    private List<ROI> labeledRois;

    public List<ROI> getLabelRois()
    {
        if (labeledRois == null)
        {
            labeledRois = new ArrayList<ROI>();
            for (int frame = 0; frame < labelSequence.getSizeT(); frame++)
            {
                labeledRois.addAll(getFrameLabelRois(frame));
            }
        }
        return labeledRois;
    }

    private List<ROI> getFrameLabelRois(int frame)
    {
        List<ROI> frameRois = new LinkedList<ROI>();
        if (labelSequence.getSizeZ() == 1)
        {
            Map<Integer, ROI2DArea> rois = new HashMap<>();
            try
            {
                for (LabeledPixel pixel : labeledPixels.get(frame))
                {
                    ROI2DArea labelRoi = rois.get(pixel.getLabel());
                    if (labelRoi == null)
                    {
                        labelRoi = new ROI2DArea();
                        labelRoi.setName("" + pixel.getLabel());
                        int r, g, b;
                        r = Random.nextInt(256);
                        g = Random.nextInt(256);
                        b = (765 - r - g) % 256;
                        labelRoi.setColor(new Color(r, g, b));
                        labelRoi.beginUpdate();
                        rois.put(pixel.getLabel(), labelRoi);
                    }

                    labelRoi.addPoint(pixel.getPosition().x, pixel.getPosition().y);
                }
            }
            finally
            {
                for (ROI2DArea roi : rois.values())
                    roi.endUpdate();
            }
            frameRois.addAll(rois.values());
        }
        else
        {
            Map<Integer, ROI3DArea> rois = new HashMap<>();
            try
            {
                for (LabeledPixel pixel : labeledPixels.get(frame))
                {
                    ROI3DArea labelRoi = rois.get(pixel.getLabel());
                    if (labelRoi == null)
                    {
                        labelRoi = new ROI3DArea(new icy.type.point.Point3D.Integer(pixel.getPosition().x,
                                pixel.getPosition().y, pixel.getPosition().z));
                        labelRoi.setName("" + pixel.getLabel());
                        int r, g, b;
                        r = Random.nextInt(256);
                        g = Random.nextInt(256);
                        b = (765 - r - g) % 256;
                        labelRoi.setColor(new Color(r, g, b));
                        labelRoi.beginUpdate();
                        rois.put(pixel.getLabel(), labelRoi);
                    }

                    labelRoi.addPoint(pixel.getPosition().x, pixel.getPosition().y, pixel.getPosition().z);
                }
            }
            finally
            {
                for (ROI3DArea roi : rois.values())
                    roi.endUpdate();
            }
            frameRois.addAll(rois.values());
        }
        return frameRois;
    }

}
