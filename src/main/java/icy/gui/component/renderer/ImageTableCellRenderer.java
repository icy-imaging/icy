/**
 *
 */
package icy.gui.component.renderer;

import icy.gui.component.ColorIcon;
import icy.resource.ResourceUtil;

import java.awt.Color;
import java.awt.Image;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.IconRenderer;

/**
 * @author Stephane
 * @deprecated Will be removed with Substance.
 */
@Deprecated
public class ImageTableCellRenderer extends IconRenderer {
    final int size;

    public ImageTableCellRenderer(int size) {
        super();

        this.size = size;
        setIconTextGap(0);
    }

    public ImageTableCellRenderer() {
        this(-1);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Image)
            setIcon(ResourceUtil.getImageIcon((Image) value, size));
        else if (value instanceof Color)
            setIcon(new ColorIcon((Color) value, size, size));
        else
            super.setValue(value);
    }
}
