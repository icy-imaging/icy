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

package org.bioimageanalysis.icy.model.colormap;

public class GlowColorMap extends IcyColorMap
{
    public GlowColorMap(boolean overUnderMark)
    {
        super(overUnderMark ? "Glow Under Over" : "Glow");

        beginUpdate();
        try
        {
            if (overUnderMark)
            {
                red.setControlPoint(0, 0);
                red.setControlPoint(64, 255);
                red.setControlPoint(192, 255);
                red.setControlPoint(254, 255);
                red.setControlPoint(255, 0);
            }
            else
            {
                red.setControlPoint(0, 0);
                red.setControlPoint(64, 255);
                red.setControlPoint(192, 255);
                red.setControlPoint(255, 255);
            }

            if (overUnderMark)
            {
                green.setControlPoint(0, 255);
                green.setControlPoint(1, 0);
                green.setControlPoint(64, 0);
                green.setControlPoint(192, 255);
                green.setControlPoint(254, 255);
                green.setControlPoint(255, 0);
            }
            else
            {
                green.setControlPoint(0, 0);
                green.setControlPoint(64, 0);
                green.setControlPoint(192, 255);
                green.setControlPoint(255, 255);
            }

            blue.setControlPoint(0, 0);
            blue.setControlPoint(192, 0);
            blue.setControlPoint(255, 255);
        }
        finally
        {
            endUpdate();
        }
    }
}
