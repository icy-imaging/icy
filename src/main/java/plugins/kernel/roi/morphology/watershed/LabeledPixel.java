package plugins.kernel.roi.morphology.watershed;

import java.util.ArrayList;
import java.util.List;

public class LabeledPixel
{
    public final WatershedPixel pixel;
    public final Integer label;
    public final List<LabeledPixel> neighbors;

    public LabeledPixel(WatershedPixel pixel, Integer label)
    {
        this.pixel = pixel;
        this.label = label;
        this.neighbors = new ArrayList<>(8);
    }
    
    public double getHeight() {
        return this.pixel.height;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((pixel == null) ? 0 : pixel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof LabeledPixel))
            return false;
        LabeledPixel other = (LabeledPixel) obj;
        if (label == null)
        {
            if (other.label != null)
                return false;
        }
        else if (!label.equals(other.label))
            return false;
        if (pixel == null)
        {
            if (other.pixel != null)
                return false;
        }
        else if (!pixel.equals(other.pixel))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "PxLbl[" + pixel + ", l=" + label + "]";
    }

}
