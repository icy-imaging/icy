package plugins.kernel.roi.roi3d;

import java.awt.geom.Rectangle2D;

import icy.resource.ResourceUtil;
import icy.type.geom.BoxShape3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import plugins.kernel.roi.roi2d.ROI2DRectangle;

/**
 * Class defining a 3D Box ROI.
 * 
 * @author Stephane
 */
public class ROI3DBox extends ROI3DBoxShape
{
    /**
     * Construct 3D box ROI
     * 
     * @param box
     *        source 3D box
     */
    public ROI3DBox(BoxShape3D box)
    {
        super(box);

        // set icon
        setIcon(ResourceUtil.ICON_ROI_BOX);
    }

    /**
     * Construct 3D box ROI
     * 
     * @param pt
     *        source 3D point
     */
    public ROI3DBox(Point3D pt)
    {
        this(new Rectangle3D.Double(pt.getX(), pt.getY(), pt.getZ(), 0d, 0d, 0d));
    }

    /**
     * Generic constructor for interactive mode
     * 
     * @param pt
     *        source 5D point
     */
    public ROI3DBox(Point5D pt)
    {
        this(pt.toPoint3D());
    }

    /**
     * Construct 3D box ROI from 2D rectangle and Z information
     * 
     * @param r
     *        2D rect
     */

    public ROI3DBox(Rectangle2D r, double z, double sizeZ)
    {
        this(new Rectangle3D.Double(r.getX(), r.getY(), z, r.getWidth(), r.getHeight(), sizeZ));
    }

    /**
     * Construct empty 3D box ROI
     */
    public ROI3DBox()
    {
        this(new Rectangle3D.Double());
    }

    @Override
    protected ROI2DRectangle createROI2DShape()
    {
        return new ROI2DRectangle();
    }

    @Override
    public String getDefaultName()
    {
        return "Box3D";
    }
}
