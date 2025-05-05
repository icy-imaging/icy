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

package org.bioimageanalysis.extension.kernel.filtering.selection;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.thread.Processor;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.common.collection.array.Array1DUtil;
import org.bioimageanalysis.icy.model.OMEUtil;

public abstract class ThreadedSelectionFilter implements SelectionFilter
{
    private static final Processor service;
    static
    {
        service = new Processor(Runtime.getRuntime().availableProcessors());
        service.setThreadName("ThreadedFilter");
    }

    @Override
    public Sequence processSequence(Sequence sequence, int... radius) throws RuntimeException, InterruptedException
    {
        Sequence out = new Sequence(OMEUtil.createOMEXMLMetadata(sequence.getOMEXMLMetadata(), true));
        out.setName(sequence.getName() + "_" + getFilterName());

        if (radius.length == 0)
            throw new IllegalArgumentException("Provide at least one filter radius");

        final int width = sequence.getSizeX();
        final int height = sequence.getSizeY();
        final int depth = sequence.getSizeZ();
        final int channels = sequence.getSizeC();
        final DataType type = sequence.getDataType();
        final boolean signed = sequence.isSignedDataType();

        final int kWidth = radius[0];
        final int kHeight = radius.length == 1 ? kWidth : radius[1];
        final int kDepth = radius.length == 1 ? kWidth : radius.length == 2 ? 0 : radius[2];

        final Object[] in_Z_XY = new Object[depth];

        final double[] cache = new double[width * height];

        // create an array of tasks for multi-thread processing
        // => rationale: one task per image line
        ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(height);

        for (int t = 0; t < sequence.getSizeT(); t++)
        {
            for (int z = 0; z < depth; z++)
                out.setImage(t, z, new IcyBufferedImage(width, height, channels, type));

            for (int c = 0; c < channels; c++)
            {
                for (int z = 0; z < depth; z++)
                    in_Z_XY[z] = sequence.getImage(t, z, c).getDataXY(0);

                for (int z = 0; z < depth; z++)
                {
                    final int minZinclusive = Math.max(z - kDepth, 0);
                    final int maxZexclusive = Math.min(z + kDepth + 1, depth);
                    final Object _inXY = in_Z_XY[z];
                    final Object _outXY = out.getDataXY(t, z, c);

                    // clear the task array
                    tasks.clear();

                    for (int y = 0; y < height; y++)
                    {
                        final int minYinclusive = Math.max(y - kHeight, 0);
                        final int maxYexclusive = Math.min(y + kHeight + 1, height);
                        final int lineOffset = y * width;

                        final int maxNeighbors = (1 + (maxZexclusive - minZinclusive) * 2)
                                * (1 + (maxYexclusive - minYinclusive) * 2) * (1 + kWidth * 2);

                        // submit a new filtering task for the current line
                        tasks.add(submitTask(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                double[] neighborhood = new double[maxNeighbors];

                                int inX, inY, inZ;
                                int inXY, outXY = lineOffset;

                                double currentPixel;

                                // process each pixel of the current line
                                for (int x = 0; x < width; x++, outXY++)
                                {
                                    currentPixel = Array1DUtil.getValue(_inXY, outXY, type);

                                    int localNeighborHoodSize = 0;
                                    int minXinclusive = Math.max(x - kWidth, 0);
                                    int maxXexclusive = Math.min(x + kWidth + 1, width);

                                    // browse the neighborhood along Z
                                    for (inZ = minZinclusive; inZ < maxZexclusive; inZ++)
                                    {
                                        Object neighborSlice = in_Z_XY[inZ];

                                        // browse the neighborhood along Y
                                        for (inY = minYinclusive; inY < maxYexclusive; inY++)
                                        {
                                            // this is the line offset
                                            inXY = inY * width + minXinclusive;

                                            // browse the neighborhood X
                                            for (inX = minXinclusive; inX < maxXexclusive; inX++, inXY++, localNeighborHoodSize++)
                                            {
                                                neighborhood[localNeighborHoodSize] = Array1DUtil
                                                        .getValue(neighborSlice, inXY, type);
                                            }
                                        }
                                    }

                                    // the neighborhood has been browsed and stored.
                                    // => the filter can be applied here

                                    cache[outXY] = processNeighborhood(currentPixel, neighborhood,
                                            localNeighborHoodSize);
                                }

                                Array1DUtil.doubleArrayToSafeArray(cache, lineOffset, _outXY, lineOffset, width,
                                        signed);

                            }
                        }));

                        if (Thread.interrupted())
                            throw new InterruptedException("Selection filter process interrupted.");

                    } // end for(y)

                    try
                    {
                        for (Future<?> f : tasks)
                            f.get();
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                        return out;
                    }
                    catch (ExecutionException e)
                    {
                        Thread.currentThread().interrupt();
                        return out;
                    }
                    finally
                    {
                        // it won't copy data but ensure data preservation with new cache engine
                        out.setDataXY(t, z, c, _outXY);
                    }

                    if (Thread.interrupted())
                        throw new InterruptedException("Selection filter process interrupted.");
                } // end for(z)
            } // end for(c)
        } // end for(t)

        return out;
    }

    private Future<?> submitTask(Runnable task)
    {
        return service.submit(task);
    }
}
