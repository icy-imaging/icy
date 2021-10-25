package icy.vtk;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkArrowSource;
import vtk.vtkMapper;
import vtk.vtkPolyDataMapper;

/**
 * Class helper to draw VTK arrow object
 * 
 * @author Stephane
 */
public class VtkArrowObject
{
    private final vtkArrowSource arrowSource;
    private final vtkMapper mapper;
    private final vtkActor actor;

    public VtkArrowObject()
    {
        super();

        arrowSource = new vtkArrowSource();
        arrowSource.SetTipLength(0.25d);
        arrowSource.SetTipRadius(0.07d);
        arrowSource.SetTipResolution(16);
        arrowSource.SetShaftRadius(0.03d);
        arrowSource.SetShaftResolution(16);

        mapper = new vtkPolyDataMapper();
        mapper.SetInputConnection(arrowSource.GetOutputPort());

        actor = new vtkActor();
        // pickable
        actor.SetPickable(1);
        actor.SetMapper(mapper);
    }

    public void release()
    {
        actor.Delete();
        mapper.Delete();
        arrowSource.Delete();
    }

    public vtkActor getActor()
    {
        return actor;
    }

    public void setColor(Color color)
    {
        actor.GetProperty().SetColor(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d);
    }

    public void setTipLenght(double value)
    {
        arrowSource.SetTipLength(value);
    }

    public void SetTipRadius(double value)
    {
        arrowSource.SetTipRadius(value);
    }

    public void SetShaftRadius(double value)
    {
        arrowSource.SetShaftRadius(value);
    }

    public void setScale(double[] value)
    {
        actor.SetScale(value);
    }

    public void setScale(double x, double y, double z)
    {
        actor.SetScale(x, y, z);
    }
}
