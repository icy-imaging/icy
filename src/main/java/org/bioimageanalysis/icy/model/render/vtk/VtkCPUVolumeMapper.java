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

package org.bioimageanalysis.icy.model.render.vtk;

import vtk.vtkFixedPointVolumeRayCastMapper;

/**
 * Intermediate CPU volume raycaster class to customize it.<br>
 * Afaik it was done to try to get rid of some bug with VTK 8.1 / VTK 8.2 when IntermixIntersectingGeometry is set to On (which is the default).
 * Volume and polydata couldn't be mixed (volume disappears as soon polydata becomes visible)
 * 
 * @author Stephane
 */
public class VtkCPUVolumeMapper extends vtkFixedPointVolumeRayCastMapper
{
    public VtkCPUVolumeMapper()
    {
        super();

        // IntermixIntersectingGeometryOff();
        // IntermixIntersectingGeometryOn();
        // GetRayCastImage().SetUseZBuffer(0);

        // AddObserver("VolumeMapperRenderEndEvent", this, "VolumeMapperRenderEndEvent");
    }

    void VolumeMapperRenderEndEvent()
    {
        // System.out.println("+++ VolumeMapperRenderEndEvent +++");
    }
}