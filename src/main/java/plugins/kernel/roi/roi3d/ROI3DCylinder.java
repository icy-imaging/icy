/**
 * 
 */
package plugins.kernel.roi.roi3d;

import java.awt.geom.RectangularShape;

import icy.resource.ResourceUtil;
import icy.type.geom.BoxShape3D;
import icy.type.geom.Cylinder3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.roi.roi2d.ROI2DEllipse;

/**
 * Class defining a 3D Cylinder ROI.
 * 
 * @author Stephane
 */
public class ROI3DCylinder extends ROI3DBoxShape
{
    /**
     * Construct 3D cylinder ROI
     * 
     * @param cylinder
     *        source 3D cylinder
     */
    public ROI3DCylinder(Cylinder3D cylinder)
    {
        super(cylinder);

        // set icon
        setIcon(ResourceUtil.ICON_ROI_CYLINDER);
    }

    /**
     * Construct 3D cylinder ROI
     * 
     * @param box
     *        source 3D box
     */
    public ROI3DCylinder(BoxShape3D box)
    {
        this(new Cylinder3D(box));
    }

    /**
     * Construct 3D cylinder ROI
     * 
     * @param pt
     *        source 3D point
     */
    public ROI3DCylinder(Point3D pt)
    {
        this(new Cylinder3D(pt.getX(), pt.getY(), pt.getZ(), 0d, 0d, 0d));
    }

    /**
     * Construct 3D cylinder ROI
     * 
     * @param pt
     *        source 5D point
     */
    public ROI3DCylinder(Point5D pt)
    {
        this(pt.toPoint3D());
    }

    /**
     * Construct 3D cylinder ROI from 2D rect and Z information
     * 
     * @param r
     *        2D rect
     */

    public ROI3DCylinder(RectangularShape r, double z, double sizeZ)
    {
        this(new Rectangle3D.Double(r.getX(), r.getY(), z, r.getWidth(), r.getHeight(), sizeZ));
    }

    /**
     * Construct empty 3D cylinder ROI
     */
    public ROI3DCylinder()
    {
        this(new Rectangle3D.Double());
    }

    @Override
    protected ROI2DEllipse createROI2DShape()
    {
        return new ROI2DEllipse();
    }

    @Override
    public String getDefaultName()
    {
        return "Cylinder3D";
    }
}
