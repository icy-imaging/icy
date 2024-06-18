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
import vtk.vtkMapper;
import vtk.vtkPlaneSource;
import vtk.vtkPolyDataMapper;

/**
 * Class helper to draw VTK plane object
 * 
 * @author Stephane
 */
public class VtkPlaneObject
{
    private final vtkPlaneSource planeSource;
    private final vtkMapper mapper;
    private final vtkActor actor;

    public VtkPlaneObject()
    {
        super();

        planeSource = new vtkPlaneSource();
        planeSource.SetCenter(0d, 0d, 0d);
        planeSource.SetNormal(0d, 1d, 0d);
        planeSource.SetXResolution(1);
        planeSource.SetYResolution(1);

        mapper = new vtkPolyDataMapper();
        mapper.SetInputConnection(planeSource.GetOutputPort());

        actor = new vtkActor();
        // pickable
        actor.SetPickable(1);
        actor.SetMapper(mapper);
    }

    public void release()
    {
        actor.Delete();
        mapper.Delete();
        planeSource.Delete();
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
        return planeSource.GetCenter();
    }

    public void setCenter(double[] value)
    {
        planeSource.SetCenter(value);
    }

    public void setCenter(double x, double y, double z)
    {
        planeSource.SetCenter(x, y, z);
    }

    public double[] getNormal()
    {
        return planeSource.GetNormal();
    }

    public void setNormal(double[] value)
    {
        planeSource.SetNormal(value);
    }

    public void setNormal(double x, double y, double z)
    {
        planeSource.SetNormal(x, y, z);
    }

    public double[] getOrigin()
    {
        return planeSource.GetOrigin();
    }

    public void setOrigin(double[] value)
    {
        planeSource.SetOrigin(value);
    }

    public void setOrigin(double x, double y, double z)
    {
        planeSource.SetOrigin(x, y, z);
    }

    public double[] getPoint1()
    {
        return planeSource.GetPoint1();
    }

    public void setPoint1(double x, double y, double z)
    {
        planeSource.SetPoint1(x, y, z);
    }

    public void setPoint1(double[] id0)
    {
        planeSource.SetPoint1(id0);
    }

    public double[] getPoint2()
    {
        return planeSource.GetPoint2();
    }

    public void setPoint2(double x, double y, double z)
    {
        planeSource.SetPoint2(x, y, z);
    }

    public void setPoint2(double[] id0)
    {
        planeSource.SetPoint2(id0);
    }

    public int getXResolution()
    {
        return planeSource.GetXResolution();
    }

    public void setXResolution(int value)
    {
        planeSource.SetXResolution(value);
    }

    public int getYResolution()
    {
        return planeSource.GetYResolution();
    }

    public void setYResolution(int value)
    {
        planeSource.SetYResolution(value);
    }

    public void setWireframeMode()
    {
        actor.GetProperty().SetRepresentationToWireframe();
    }

    public void setSurfaceMode()
    {
        actor.GetProperty().SetRepresentationToSurface();
    }

    public void setEdgeVisibile(boolean value)
    {
        actor.GetProperty().SetEdgeVisibility(value ? 1 : 0);
    }

    public void setEdgeColor(Color color)
    {
        actor.GetProperty().SetEdgeColor(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d);
    }
}
