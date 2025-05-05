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

package org.bioimageanalysis.extension.kernel.roi.morphology;

import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROI2D;
import org.bioimageanalysis.icy.model.roi.ROI3D;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceDataIterator;
import org.bioimageanalysis.icy.model.sequence.VolumetricImage;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.common.type.DataIteratorUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension3D;
import org.bioimageanalysis.icy.common.geom.dimension.Dimension5D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class computes the distance transform of a list containing {@link ROI}s. As a result, an area ROI is generated as a result of the size of
 * {@code imageSize}. It can compute both 2D and 3D distance maps. The distance is computed taking into account the pixel size in three dimensions passed in
 * {@code pixelSize}.
 * The method for computing the distance transform is based on the algorithm proposed in the article "New Algorithms for euclidean distance transformation of an
 * n-dimensional digitized picture with applications" by Toyofumi Saito and Jun-Ichiro Toriwaki published in Pattern Recognition Vol. 27 No. 11, 1994.
 *
 * @author Daniel Felipe Gonzalez Obando
 * @author Thomas Musset
 */
public class ROIDistanceTransformCalculator {
    private final Dimension5D imageSize;
    private final Dimension3D pixelSize;
    private final boolean constrainImageBorders;
    private final List<ROI> rois;

    public ROIDistanceTransformCalculator(final Dimension5D imageSize, final Dimension3D pixelSize, final boolean constrainImageBorders) {
        this.imageSize = imageSize;
        this.pixelSize = pixelSize;
        this.constrainImageBorders = constrainImageBorders;
        this.rois = new ArrayList<>();
    }

    public <T extends ROI> void addROI(final T roi) {
        rois.add(roi);
    }

    public <T extends ROI> void addAll(final Collection<T> rois) {
        this.rois.addAll(rois);
    }

    Sequence distanceMap;

    public Sequence getDistanceMap() throws InterruptedException {
        if (distanceMap == null)
            compute();
        return distanceMap;
    }

    public void compute() throws InterruptedException {
        initializeDistanceMap();
        drawROIs();
        processTimePoints();
    }

    double[] buffer;

    private void initializeDistanceMap() {
        distanceMap = new Sequence();
        distanceMap.setPixelSizeX(pixelSize.getSizeX());
        distanceMap.setPixelSizeY(pixelSize.getSizeY());
        distanceMap.setPixelSizeZ(pixelSize.getSizeZ());
        for (int m = 0; m < imageSize.getSizeT(); m++) {
            final VolumetricImage volume = new VolumetricImage();
            for (int k = 0; k < imageSize.getSizeZ(); k++) {
                final IcyBufferedImage plane = new IcyBufferedImage((int) imageSize.getSizeX(), (int) imageSize.getSizeY(), 1,
                        DataType.DOUBLE);
                volume.setImage(k, plane);
            }
            distanceMap.addVolumetricImage(m, volume);
        }
        buffer = new double[Math.max((int) imageSize.getSizeY(), (int) imageSize.getSizeZ())];
    }

    private void drawROIs() throws InterruptedException {
        for (final ROI roi : rois) {
            int oldC = 0;
            if (roi instanceof ROI2D) {
                oldC = ((ROI2D) roi).getC();
                ((ROI2D) roi).setC(0);
            }
            else if (roi instanceof ROI3D) {
                oldC = ((ROI3D) roi).getC();
                ((ROI3D) roi).setC(0);
            }
            try {
                DataIteratorUtil.set(new SequenceDataIterator(distanceMap, roi, true), 1d);
            }
            finally {
                if (roi instanceof ROI2D) {
                    ((ROI2D) roi).setC(oldC);
                }
                else if (roi instanceof ROI3D) {
                    ((ROI3D) roi).setC(oldC);
                }
            }
        }
    }

    private VolumetricImage currentVolumeImage;

    private void processTimePoints() throws InterruptedException {
        for (int t = 0; t < imageSize.getSizeT(); t++) {
            if (Thread.interrupted())
                throw new InterruptedException("ROI distance transform descriptor computation interrupted.");
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
    private double maxDistance;

    private void processCurrentVolume() throws InterruptedException {
        currentVolumePlanes = new double[(int) imageSize.getSizeZ()][];
        squaredSizeY = pixelSize.getSizeY() * pixelSize.getSizeY();
        squaredSizeZ = pixelSize.getSizeZ() * pixelSize.getSizeZ();
        maxDistance = Math.max(
                Math.max(imageSize.getSizeX() * pixelSize.getSizeX(), imageSize.getSizeY() * pixelSize.getSizeY()),
                imageSize.getSizeZ() * pixelSize.getSizeZ());
        maxDistance *= maxDistance;
        for (int k = 0; k < imageSize.getSizeZ(); k++) {
            if (Thread.interrupted())
                throw new InterruptedException("ROI distance transform descriptor computation interrupted.");

            final double[] currentPlaneData = currentVolumeImage.getImage(k).getDataXYAsDouble(0);
            currentVolumePlanes[k] = currentPlaneData;
            currentK = k;
            processCurrentPlane();
        }

        if (imageSize.getSizeZ() > 1) {
            for (int j = 0; j < imageSize.getSizeY(); j++) {
                if (Thread.interrupted())
                    throw new InterruptedException("ROI distance transform descriptor computation interrupted.");

                currentJ = j;
                for (int i = 0; i < imageSize.getSizeX(); i++) {
                    currentI = i;
                    processCurrentFiber();
                }
            }
        }
        for (int k = 0; k < imageSize.getSizeZ(); k++) {
            if (Thread.interrupted())
                throw new InterruptedException("ROI distance transform descriptor computation interrupted.");

            for (int j = 0; j < imageSize.getSizeY(); j++) {
                currentJ = j;
                for (int i = 0; i < imageSize.getSizeX(); i++) {
                    setValueAt(i, j, k, Math.sqrt(getValueAt(i, j, k)));
                }
            }
        }
    }

    private void processCurrentPlane() {
        for (int j = 0; j < imageSize.getSizeY(); j++) {
            currentJ = j;
            processCurrentRow();
        }

        for (int i = 0; i < imageSize.getSizeX(); i++) {
            currentI = i;
            processCurrentCol();
        }
    }

    private void processCurrentRow() {
        forwardPassCurrentRow();
        backwardPassCurrentRow();
    }

    private void forwardPassCurrentRow() {
        double affectedValue = constrainImageBorders ? 0 : maxDistance;

        for (int i = 0; i < imageSize.getSizeX(); i++) {
            if (getValueAt(i, currentJ, currentK) != 0d) {
                affectedValue = affectedValue + pixelSize.getSizeX();
            }
            else {
                affectedValue = 0d;
            }
            setValueAt(i, currentJ, currentK, affectedValue * affectedValue);
        }
    }

    private void backwardPassCurrentRow() {
        double affectedValue = constrainImageBorders ? 0 : maxDistance;
        for (int i = (int) imageSize.getSizeX() - 1; i >= 0; i--) {
            if (getValueAt(i, currentJ, currentK) != 0d) {
                affectedValue = affectedValue + pixelSize.getSizeX();
            }
            else {
                affectedValue = 0d;
            }
            setValueAt(i, currentJ, currentK,
                    Math.min(getValueAt(i, currentJ, currentK), affectedValue * affectedValue));
        }
    }

    private void processCurrentCol() {
        for (int j = 0; j < imageSize.getSizeY(); j++) {
            buffer[j] = getValueAt(currentI, j, currentK);
        }
        forwardPassCurrentCol();
        backwardPassCurrentCol();
    }

    private void forwardPassCurrentCol() {
        int a = 0;
        for (int j = constrainImageBorders ? 0 : 1; j < imageSize.getSizeY(); j++) {
            if (a > 0)
                a--;
            final double dj = buffer[j] / squaredSizeY;
            final double dj1 = (j == 0) ? 0d : (buffer[j - 1] / squaredSizeY);
            if (dj > dj1 + 1d) {
                double b = Math.ceil((dj - dj1 - 1d) / 2d);
                if (j + b > imageSize.getSizeY())
                    b = (int) imageSize.getSizeY() - j;
                final int bi = (int) b;
                for (int n = a; n < bi; n++) {
                    int nSq = n + 1;
                    nSq *= nSq;
                    final double m = dj1 + nSq;
                    try {
                        if (buffer[j + n] / squaredSizeY <= m)
                            break;
                    }
                    catch (final Exception e) {
                        IcyLogger.error(ROIDistanceTransformCalculator.class, e, e.getLocalizedMessage());
                        throw e;
                    }
                    if (m < getValueAt(currentI, j + n, currentK) / squaredSizeY)
                        setValueAt(currentI, j + n, currentK, m * squaredSizeY);
                }
                a = bi;
            }
            else {
                a = 0;
            }
        }
    }

    private void backwardPassCurrentCol() {
        int a = 0;
        for (int j = (int) imageSize.getSizeY() - (constrainImageBorders ? 1 : 2); j >= 0; j--) {
            if (a > 0)
                a--;
            final double dj = buffer[j] / squaredSizeY;
            final double dj1 = (j == (int) imageSize.getSizeY() - 1) ? 0 : (buffer[j + 1] / squaredSizeY);
            if (dj > dj1) {
                double b = Math.ceil((dj - dj1 - 1d) / 2d);
                if (j - b < 0)
                    b = j;
                final int bi = (int) b;

                for (int n = a; n <= bi; n++) {
                    double nSq = n + 1;
                    nSq *= nSq;
                    final double m = dj1 + nSq;
                    if (buffer[j - n] / squaredSizeY <= m)
                        break;
                    if (m < getValueAt(currentI, j - n, currentK) / squaredSizeY)
                        setValueAt(currentI, j - n, currentK, m * squaredSizeY);
                }
                a = bi;
            }
            else {
                a = 0;
            }
        }
    }

    private void processCurrentFiber() {
        for (int k = 0; k < imageSize.getSizeZ(); k++) {
            buffer[k] = getValueAt(currentI, currentJ, k);
        }
        forwardPassCurrentFiber();
        backwardPassCurrentFiber();
    }

    private void forwardPassCurrentFiber() {
        int a = 0;
        for (int k = constrainImageBorders ? 0 : 1; k < imageSize.getSizeZ(); k++) {
            if (a > 0)
                a--;
            final double dk = buffer[k] / squaredSizeZ;
            final double dk1 = (k == 0) ? 0d : (buffer[k - 1] / squaredSizeZ);
            if (dk > dk1 + 1d) {
                double b = Math.ceil((dk - dk1 - 1d) / 2d);
                if (k + b > imageSize.getSizeZ())
                    b = (int) imageSize.getSizeZ() - k;
                final int bi = (int) b;
                for (int n = a; n < bi; n++) {
                    int nSq = n + 1;
                    nSq *= nSq;
                    final double m = dk1 + nSq;
                    if (buffer[k + n] / squaredSizeZ <= m)
                        break;
                    if (m < getValueAt(currentI, currentJ, k + n) / squaredSizeZ)
                        setValueAt(currentI, currentJ, k + n, m * squaredSizeZ);
                }
                a = bi;
            }
            else {
                a = 0;
            }
        }

    }

    private void backwardPassCurrentFiber() {
        int a = 0;
        for (int k = (int) imageSize.getSizeZ() - (constrainImageBorders ? 1 : 2); k >= 0; k--) {
            if (a > 0)
                a--;
            final double dk = buffer[k] / squaredSizeZ;
            final double dk1 = (k == (int) imageSize.getSizeZ() - 1) ? 0 : (buffer[k + 1] / squaredSizeZ);
            if (dk > dk1 + 1d) {
                double b = (int) Math.ceil((dk - dk1 - 1d) / 2d);
                if (k - b < 0)
                    b = k;
                final int bi = (int) b;
                for (int n = a; n <= bi; n++) {
                    double nSq = n + 1;
                    nSq *= nSq;
                    final double m = dk1 + nSq;
                    if (buffer[k - n] / squaredSizeZ <= m)
                        break;
                    if (m < getValueAt(currentI, currentJ, k - n) / squaredSizeZ)
                        setValueAt(currentI, currentJ, k - n, m * squaredSizeZ);
                }
                a = bi;
            }
            else {
                a = 0;
            }
        }
    }

    private double getValueAt(final int x, final int y, final int z) {
        return currentVolumePlanes[z][y * (int) imageSize.getSizeX() + x];
    }

    private void setValueAt(final int x, final int y, final int z, final double value) {
        currentVolumePlanes[z][y * (int) imageSize.getSizeX() + x] = value;
    }

}
