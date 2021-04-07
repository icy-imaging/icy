/**
 * 
 */
package plugins.kernel.roi.tool.plugin;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.type.point.Point5D;
import plugins.kernel.roi.tool.ROIMagicWand;

/**
 * Plugin class for ROIMagicWand.
 * 
 * @author Stephane
 */
public class ROIMagicWandPlugin extends Plugin implements PluginROI
{
    @Override
    public String getROIClassName()
    {
        return ROIMagicWand.class.getName();
    }

    @Override
    public ROI createROI(Point5D pt)
    {
        return new ROIMagicWand(pt);
    }

    @Override
    public ROI createROI()
    {
        return new ROIMagicWand();
    }
}
