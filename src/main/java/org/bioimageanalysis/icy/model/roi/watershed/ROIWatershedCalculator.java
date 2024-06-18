/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package org.bioimageanalysis.icy.model.roi.watershed;

import org.bioimageanalysis.extension.kernel.filtering.GaussianFiltering;
import org.bioimageanalysis.extension.kernel.filtering.LocalMaxFiltering;
import org.bioimageanalysis.extension.kernel.filtering.convolution.ConvolutionException;
import org.bioimageanalysis.extension.kernel.roi.morphology.ROIDistanceTransformCalculator;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceDataIterator;
import org.bioimageanalysis.icy.model.sequence.VolumetricImage;
import org.bioimageanalysis.icy.model.sequence.VolumetricImageCursor;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.common.type.DataIteratorUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension3D;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension5D;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.math.Random;
import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DArea;
import org.bioimageanalysis.extension.kernel.roi.roi2d.ROI2DPoint;
import org.bioimageanalysis.extension.kernel.roi.roi3d.ROI3DArea;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIWatershedCalculator implements Callable<Void> {
    public static class Builder {
        private final Dimension5D imageSize;
        private final Dimension3D pixelSize;
        private final List<ROI> rois;
        private final List<ROI> seeds;
        private boolean newBasinsAllowed;

        public Builder(final Dimension5D imageSize, final Dimension3D pixelSize) {
            Objects.requireNonNull(imageSize);
            Objects.requireNonNull(pixelSize);
            this.imageSize = imageSize;
            this.pixelSize = pixelSize;
            this.rois = new ArrayList<>();
            this.seeds = new ArrayList<>();
            this.newBasinsAllowed = true;
        }

        public <T extends ROI> Builder addObject(final T roi) {
            Objects.requireNonNull(roi);
            this.rois.add(roi);
            return this;
        }

        public <T extends ROI> Builder addObjects(final Collection<T> rois) {
            Objects.requireNonNull(rois);
            this.rois.addAll(rois);
            return this;
        }

        public <T extends ROI> Builder addSeed(final T roi) {
            Objects.requireNonNull(roi);
            this.seeds.add(roi);
            return this;
        }

        public <T extends ROI> Builder addSeeds(final Collection<T> rois) {
            Objects.requireNonNull(rois);
            this.seeds.addAll(rois);
            return this;
        }

        public Builder setNewBasinsAllowed(final boolean allow) {
            this.newBasinsAllowed = allow;
            return this;
        }

        public ROIWatershedCalculator build() {
            final ROIWatershedCalculator calculator = new ROIWatershedCalculator();
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

    private ROIWatershedCalculator() {
    }

    private void setImageSize(final Dimension5D imageSize) {
        this.imageSize = imageSize;
    }

    private void setPixelSize(final Dimension3D pixelSize) {
        this.pixelSize = pixelSize;
    }

    private void setDomainRois(final List<ROI> domainRois) {
        this.domainRois = domainRois;
    }

    private void setSeedRois(final List<ROI> seedRois) {
        this.seedRois = seedRois;
    }

    private void setNewBasinsAllowed(final boolean newBasinsAllowed) {
        this.newBasinsAllowed = newBasinsAllowed;
    }

    @Override
    public Void call() throws Exception {
        labeledRois = null;
        computeDomainDistanceMap();
        prepareSeeds();
        computeWatershedOnFrames();
        return null;
    }

    private Sequence domainDistanceMap;

    private void computeDomainDistanceMap() throws InterruptedException {
        final ROIDistanceTransformCalculator dt = new ROIDistanceTransformCalculator(imageSize, pixelSize, true);
        dt.addAll(domainRois);
        try {
            this.domainDistanceMap = dt.getDistanceMap();
        }
        catch (final InterruptedException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new RuntimeException("Error computing domain distance map: " + e.getMessage(), e);
        }
    }

    private void prepareSeeds() throws InterruptedException {
        if (seedRois.isEmpty() && !newBasinsAllowed) {
            seedRois.addAll(getDomainLocalMaximumROIs());
        }

        initializeLabelSequence();
        int seedLabel = 1;
        for (final ROI roi : seedRois) {
            addSeedToLabelSequence(roi, seedLabel++);
        }
    }

    private List<? extends ROI> getDomainLocalMaximumROIs()
            throws IllegalArgumentException, ConvolutionException, InterruptedException {
        final LocalMaxFiltering localMaxFiltering = LocalMaxFiltering.create(getSmoothedDomainDistanceMap(), 3);
        localMaxFiltering.computeFiltering();

        final List<ROI> localMaximaRois = new ArrayList<>();
        for (final SequenceDataIterator localMaxIt = new SequenceDataIterator(
                localMaxFiltering.getFilteredSequence()); !localMaxIt.done(); localMaxIt.next()) {
            if (Thread.interrupted())
                throw new InterruptedException("ROI local maxima computation interrupted.");

            if (localMaxIt.get() > 0) {
                localMaximaRois.add(new ROI2DPoint(new Point5D.Integer(
                        localMaxIt.getPositionX(),
                        localMaxIt.getPositionY(),
                        localMaxIt.getPositionZ(),
                        localMaxIt.getPositionT(),
                        localMaxIt.getPositionC()
                )));
            }
        }
        return localMaximaRois;
    }

    private Sequence getSmoothedDomainDistanceMap()
            throws IllegalArgumentException, ConvolutionException, InterruptedException {
        GaussianFiltering smoothingFilter = GaussianFiltering.create(this.domainDistanceMap, new double[]{2, 2, 2});
        try {
            smoothingFilter.computeFiltering();
        }
        catch (final ConvolutionException e) {
            IcyLogger.warn(ROIWatershedCalculator.class, e, "z sigma 2 too large.. trying 1");
            try {
                smoothingFilter = GaussianFiltering.create(this.domainDistanceMap, new double[]{2, 2, 1});
                smoothingFilter.computeFiltering();
            }
            catch (final ConvolutionException e1) {
                IcyLogger.warn(ROIWatershedCalculator.class, e1, "z sigma 1 too large.. using original distance map");
                return this.domainDistanceMap;
            }
        }

        return smoothingFilter.getFilteredSequence();
    }

    private Sequence labelSequence;

    private void initializeLabelSequence() throws InterruptedException {
        labelSequence = new Sequence("labels");
        for (int l = 0; l < imageSize.getSizeT(); l++) {
            if (Thread.interrupted())
                throw new InterruptedException("ROI watershed descriptor computation interrupted.");

            final VolumetricImage volume = new VolumetricImage();
            for (int k = 0; k < imageSize.getSizeZ(); k++) {
                if (Thread.interrupted())
                    throw new InterruptedException("ROI watershed descriptor computation interrupted.");

                final IcyBufferedImage plane = new IcyBufferedImage((int) imageSize.getSizeX(), (int) imageSize.getSizeY(), 1, DataType.INT);
                volume.setImage(k, plane);
            }
            labelSequence.addVolumetricImage(l, volume);
        }
    }

    private void addSeedToLabelSequence(final ROI seedRoi, final int label) throws InterruptedException {
        DataIteratorUtil.set(new SequenceDataIterator(labelSequence, seedRoi, true), label);
    }

    private Map<Integer, List<LabeledPixel>> labeledPixels;

    private void computeWatershedOnFrames() {
        labeledPixels = new HashMap<>(labelSequence.getSizeT());
        final FloodingStructure.Builder floodingStructureBuilder = new FloodingStructure.Builder(domainDistanceMap,
                labelSequence);
        for (int frame = 0; frame < labelSequence.getSizeT(); frame++) {
            floodingStructureBuilder.setFrame(frame);
            final FloodingStructure frameFloodingStructure = floodingStructureBuilder.build();
            computeFrameFlooding(frameFloodingStructure, frame);
            updateLabelSequence(frame, frameFloodingStructure);
            labeledPixels.put(frame, frameFloodingStructure.getFloodingPixels());
        }
    }

    private void computeFrameFlooding(final FloodingStructure frameFloodingStructure, final int frame) {
        final AtomicInteger labelGenerator = new AtomicInteger(seedRois.size());

        final List<Double> heights = new ArrayList<>(frameFloodingStructure.getHeights());
        Collections.reverse(heights);
        final AtomicInteger heightPixelIndex = new AtomicInteger(0);
        final AtomicInteger nextPixelIndex = new AtomicInteger(0);
        final Queue<LabeledPixel> pendingPixels = new LinkedList<>();
        for (final double height : heights) {
            final List<LabeledPixel> pixelsAtHeight = findPixelsAtHeight(frameFloodingStructure.getFloodingPixels(), height, heightPixelIndex);
            pendingPixels.addAll(pixelsAtHeight);
            extendBasins(pendingPixels);
            finishCurrentHeight(frame, frameFloodingStructure.getFloodingPixels(), height, nextPixelIndex, labelGenerator);
        }
    }

    private List<LabeledPixel> findPixelsAtHeight(final List<LabeledPixel> floodingPixels, final double height, final AtomicInteger startingPixelIndex) {
        final List<LabeledPixel> candidatePixelsAtHeight = new LinkedList<>();
        for (int pixelIndex = startingPixelIndex.get(); pixelIndex < floodingPixels.size(); pixelIndex++) {
            final LabeledPixel pixel = floodingPixels.get(pixelIndex);

            if (pixel.getHeight() < height) {
                startingPixelIndex.set(pixelIndex);
                break;
            }

            if (!pixel.isLabeled()) {
                pixel.setToBeLabeled();
            }

            for (final LabeledPixel neighborPixel : pixel.getNeighbors())
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

    private void extendBasins(final Queue<LabeledPixel> pendingPixels) {
        final LabeledPixel flagPixel = new LabeledPixel(new Point3D(0, 0, 0), -1);
        pendingPixels.add(flagPixel);
        int currentLevel = 1;

        while (true) {
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
            for (final LabeledPixel neighborPixel : Objects.requireNonNull(currentPixel).getNeighbors()) {
                if (neighborPixel.getLevel() <= currentLevel && neighborPixel.isLabeled()) {

                    if (currentPixel.isToBeLabeled()) {
                        shouldExtend = true;
                    }

                    if (neighborLabel == 0) {
                        neighborLabel = neighborPixel.getLabel();
                    }
                    else if (neighborLabel != neighborPixel.getLabel()) {
                        isBasinBorder = true;
                    }

                    final double distance = neighborPixel.getHeight() - currentPixel.getHeight();
                    if (bestNeighbor == null) {
                        bestNeighbor = neighborPixel;
                        bestDistance = distance;
                    }
                    else if (distance > bestDistance) {
                        bestNeighbor = neighborPixel;
                        bestDistance = distance;
                    }

                }
                else if (neighborPixel.isToBeLabeled() && neighborPixel.getLevel() == 0) {
                    neighborPixel.setLevel(currentLevel + 1);
                    pendingPixels.add(neighborPixel);
                }
            }

            if (shouldExtend || isBasinBorder) {
                currentPixel.setLabel(bestNeighbor.getLabel());
                if (!newBasinsAllowed) {
                    floodBasin(currentPixel, bestNeighbor.getLabel());
                }
            }
        }
    }

    private void finishCurrentHeight(final int frame, final List<LabeledPixel> floodingPixels, final double height, final AtomicInteger nextPixelIndex, final AtomicInteger labelGenerator) {
        final VolumetricImageCursor labelCursor = new VolumetricImageCursor(labelSequence, frame);
        for (int pixelIndex = nextPixelIndex.get(); pixelIndex < floodingPixels.size(); pixelIndex++) {
            final LabeledPixel pixel = floodingPixels.get(pixelIndex);
            if (pixel.getHeight() < height) {
                nextPixelIndex.set(pixelIndex);
                break;
            }

            pixel.setLevel(0);

            if (pixel.isToBeLabeled()) {
                if (newBasinsAllowed) {
                    floodBasin(pixel, labelGenerator.incrementAndGet());
                }
                else {
                    final int pixelLabel = (int) labelCursor.get(pixel.getPosition().x, pixel.getPosition().y, pixel.getPosition().z, 0);
                    if (pixelLabel > 0) {
                        floodBasin(pixel, pixelLabel);
                    }
                }
            }
        }
    }

    private void floodBasin(final LabeledPixel startPixel, final int label) {
        startPixel.setLabel(label);

        final Queue<LabeledPixel> pendingBasinPixels = new LinkedList<>();
        pendingBasinPixels.add(startPixel);
        while (!pendingBasinPixels.isEmpty()) {
            final LabeledPixel currentPixel = pendingBasinPixels.poll();

            for (final LabeledPixel neighborPixel : currentPixel.getNeighbors()) {
                if (neighborPixel.isToBeLabeled() || (neighborPixel.isNoLabel() && neighborPixel.getHeight() >= currentPixel.getHeight())) {
                    neighborPixel.setLabel(label);
                    neighborPixel.setLevel(startPixel.getLevel());
                    pendingBasinPixels.add(neighborPixel);
                }
            }
        }
    }

    private void updateLabelSequence(final int frame, final FloodingStructure frameFloodingStructure) {
        final VolumetricImageCursor labelCursor = new VolumetricImageCursor(labelSequence, frame);
        for (final LabeledPixel labeledPixel : frameFloodingStructure.getFloodingPixels()) {
            labelCursor.set(labeledPixel.getPosition().x, labeledPixel.getPosition().y, labeledPixel.getPosition().z, 0, labeledPixel.getLabel());
        }
        labelCursor.commitChanges();
    }

    public Sequence getLabelSequence() {
        return labelSequence;
    }

    public List<ROI> getSeeds() {
        return seedRois;
    }

    private List<ROI> labeledRois;

    public List<ROI> getLabelRois() {
        if (labeledRois == null) {
            labeledRois = new ArrayList<>();
            for (int frame = 0; frame < labelSequence.getSizeT(); frame++) {
                labeledRois.addAll(getFrameLabelRois(frame));
            }
        }
        return labeledRois;
    }

    private List<ROI> getFrameLabelRois(final int frame) {
        final List<ROI> frameRois = new LinkedList<>();
        if (labelSequence.getSizeZ() == 1) {
            final Map<Integer, ROI2DArea> rois = new HashMap<>();
            try {
                for (final LabeledPixel pixel : labeledPixels.get(frame)) {
                    ROI2DArea labelRoi = rois.get(pixel.getLabel());
                    if (labelRoi == null) {
                        labelRoi = new ROI2DArea();
                        labelRoi.setName("" + pixel.getLabel());
                        final int r;
                        final int g;
                        final int b;
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
            finally {
                for (final ROI2DArea roi : rois.values())
                    roi.endUpdate();
            }
            frameRois.addAll(rois.values());
        }
        else {
            final Map<Integer, ROI3DArea> rois = new HashMap<>();
            try {
                for (final LabeledPixel pixel : labeledPixels.get(frame)) {
                    ROI3DArea labelRoi = rois.get(pixel.getLabel());
                    if (labelRoi == null) {
                        labelRoi = new ROI3DArea(new org.bioimageanalysis.icy.common.geom.point.Point3D.Integer(pixel.getPosition().x,
                                pixel.getPosition().y, pixel.getPosition().z));
                        labelRoi.setName("" + pixel.getLabel());
                        final int r;
                        final int g;
                        final int b;
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
            finally {
                for (final ROI3DArea roi : rois.values())
                    roi.endUpdate();
            }
            frameRois.addAll(rois.values());
        }
        return frameRois;
    }

}
