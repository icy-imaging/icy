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

package org.bioimageanalysis.extension.kernel.filtering;

import java.util.Arrays;
import java.util.Objects;

import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceUtil;
import org.bioimageanalysis.extension.kernel.filtering.convolution.Convolution1D;
import org.bioimageanalysis.extension.kernel.filtering.convolution.ConvolutionException;
import org.bioimageanalysis.extension.kernel.filtering.convolution.GaussianKernel1D;

public class GaussianFiltering
{
    private Sequence sequence;
    private double[] sigma;

    public static GaussianFiltering create(Sequence sequence, double[] sigma)
    {
        return new GaussianFiltering(sequence, sigma);
    }

    private GaussianFiltering(Sequence sequence, double[] sigma) throws NullPointerException
    {
        Objects.requireNonNull(sequence, "Null sequence");

        if (sequence.getSizeZ() == 0)
        {
            throw new IllegalArgumentException("Empty image");
        }
        else if (sequence.getSizeZ() == 1 && sigma.length < 2)
        {
            throw new IllegalArgumentException("Sigma array size is not suitable for the input sequence: Image is 2D");
        }
        else if (sequence.getSizeZ() > 1 && sigma.length < 3)
        {
            throw new IllegalArgumentException("Sigma array size is not suitable for the input sequence: Image is 3D");
        }

        this.sequence = sequence;
        this.sigma = sigma;
    }

    private Sequence filteredSequence;

    public void computeFiltering() throws IllegalArgumentException, ConvolutionException, InterruptedException
    {
        double[] gaussianX = GaussianKernel1D.create(sigma[0]).getData();
        double[] gaussianY = GaussianKernel1D.create(sigma[1]).getData();
        double[] gaussianZ = sigma.length >= 3 ? GaussianKernel1D.create(sigma[2]).getData() : null;

        filteredSequence = SequenceUtil.getCopy(sequence);
        filteredSequence.setName(sequence.getName() + "_Gaussian" + Arrays.toString(sigma));

        Convolution1D.convolve(filteredSequence, gaussianX == null ? null : gaussianX,
                gaussianY == null ? null : gaussianY, gaussianZ == null ? null : gaussianZ);
    }

    public Sequence getFilteredSequence()
    {
        return filteredSequence;
    }
}
