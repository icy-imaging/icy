package icy.vtk;

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