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

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkCylinderSource;
import vtk.vtkMapper;
import vtk.vtkPolyDataMapper;

/**
 * Class helper to draw VTK cylinder object
 * 
 * @author Stephane
 */
public class VtkCylinderObject
{
    private final vtkCylinderSource cylinderSource;
    private final vtkMapper mapper;
    private final vtkActor actor;

    public VtkCylinderObject()
    {
        super();

        cylinderSource = new vtkCylinderSource();
        cylinderSource.SetCenter(0d, 0d, 0d);
        cylinderSource.SetRadius(0.1d);
        cylinderSource.SetHeight(1d);
        cylinderSource.SetResolution(8);
        cylinderSource.SetCapping(1);

        mapper = new vtkPolyDataMapper();
        mapper.SetInputConnection(cylinderSource.GetOutputPort());

        actor = new vtkActor();
        // pickable
        actor.SetPickable(1);
        actor.SetMapper(mapper);
    }

    public void release()
    {
        actor.Delete();
        mapper.Delete();
        cylinderSource.Delete();
    }

    public vtkActor getActor()
    {
        return actor;
    }

    public void setColor(Color color)
    {
        actor.GetProperty().SetColor(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d);
    }

    public double[] getCenter()
    {
        return cylinderSource.GetCenter();
    }

    public void setCenter(double[] value)
    {
        cylinderSource.SetCenter(value);
    }

    public void setCenter(double x, double y, double z)
    {
        cylinderSource.SetCenter(x, y, z);
    }

    public double getHeight()
    {
        return cylinderSource.GetHeight();
    }

    public void setHeight(double value)
    {
        cylinderSource.SetHeight(value);
    }

    public double getRadius()
    {
        return cylinderSource.GetRadius();
    }

    public void setRadius(double value)
    {
        cylinderSource.SetRadius(value);
    }

    public int getResolution()
    {
        return cylinderSource.GetResolution();
    }

    public void setResolution(int value)
    {
        cylinderSource.SetResolution(value);
    }

    public boolean getCapping()
    {
        return (cylinderSource.GetCapping() != 0) ? true : false;
    }

    public void setCapping(boolean value)
    {
        cylinderSource.SetCapping(value ? 1 : 0);
    }
}
