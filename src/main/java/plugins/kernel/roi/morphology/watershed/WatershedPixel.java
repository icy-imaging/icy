package plugins.kernel.roi.morphology.watershed;

public class WatershedPixel
{
    public final Position3D position;
    public final double height;

    public WatershedPixel(Position3D position, double height)
    {
        this.position = position;
        this.height = height; 
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof WatershedPixel))
            return false;
        WatershedPixel other = (WatershedPixel) obj;
        if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
            return false;
        if (position == null)
        {
            if (other.position != null)
                return false;
        }
        else if (!position.equals(other.position))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "WSPx[pos=" + position + ", h=" + height + "]";
    }

    
}
