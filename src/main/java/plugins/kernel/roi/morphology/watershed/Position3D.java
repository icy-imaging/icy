package plugins.kernel.roi.morphology.watershed;

public class Position3D
{
    public final int x, y, z;

    public Position3D(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position3D(int x, int y)
    {
        this(x, y, 0);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof Position3D))
            return false;
        Position3D other = (Position3D) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

    private String stringValue;

    @Override
    public String toString()
    {
        if (stringValue == null)
            stringValue = "(" + x + ", " + y + ", " + z + ")";
        return stringValue;
    }
}
