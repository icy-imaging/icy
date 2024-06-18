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

/**
 * 
 */
package org.bioimageanalysis.extension.kernel.filtering.selection;

/**
 * Search for neighboring pixels up to a given distance and keeps only local maximum pixels.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class LocalMaxMethod extends ThreadedSelectionFilter
{

    @Override
    public String getFilterName()
    {
        return "LOCAL_MAX";
    }

    @Override
    public double processNeighborhood(double currentValue, double[] neighborhood, int neighborhoodSize)
    {
        double neighborValue, defaultValue = 0d;
        for (int i = 0; i < neighborhoodSize; i++)
        {
            neighborValue = neighborhood[i];
            if (neighborValue > currentValue)
                return 0d;
            if (defaultValue == 0d && neighborValue < currentValue)
                defaultValue = 1d;
        }

        return defaultValue;
    }

}
