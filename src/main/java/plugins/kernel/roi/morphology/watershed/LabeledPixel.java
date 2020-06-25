package plugins.kernel.roi.morphology.watershed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;

public class LabeledPixel implements Comparable<LabeledPixel>
{
    private static final int NO_LABEL = 0;
    private static final int TO_BE_LABELED = -1;

    private final Point3D position;
    private final AtomicDouble height;
    private final List<LabeledPixel> neighbors;
    private final AtomicInteger label;
    private final AtomicDouble distance;
    private final AtomicInteger level;

    public LabeledPixel(Point3D position, double height)
    {
        this.position = position;
        this.height = new AtomicDouble(height);
        this.neighbors = new ArrayList<>(8);
        this.label = new AtomicInteger(0);
        this.distance = new AtomicDouble(Double.NaN);
        this.level = new AtomicInteger(0);
    }

    public Point3D getPosition()
    {
        return position;
    }

    public double getHeight()
    {
        return height.get();
    }

    public void setHeight(double newHeight)
    {
        this.height.set(newHeight);
    }

    public List<LabeledPixel> getNeighbors()
    {
        return neighbors;
    }

    public void addNeighbor(LabeledPixel neighbor)
    {
        neighbors.add(neighbor);
    }

    public int getLabel()
    {
        return label.get();
    }

    public void setLabel(int newLabel)
    {
        this.label.set(newLabel);
    }

    public double getDistance()
    {
        return distance.get();
    }

    public void setDistance(double newDistance)
    {
        this.distance.set(newDistance);
    }

    public boolean isLabeled()
    {
        return label.get() > NO_LABEL;
    }
    
    public boolean isNoLabel() {
        return label.get() == NO_LABEL;
    }

    public void setToBeLabeled()
    {
        setLabel(TO_BE_LABELED);
    }

    public boolean isToBeLabeled()
    {
        return getLabel() == TO_BE_LABELED;
    }

    public void setLevel(int newLevel)
    {
        this.level.set(newLevel);
    }

    public int getLevel()
    {
        return level.get();
    }

    @Override
    public int compareTo(LabeledPixel o)
    {
        return Double.compare(getHeight(), o.getHeight());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((position == null) ? 0 : position.hashCode());
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
        return "Pixel " + position + " [h=" + height + ", l=" + label + ", d=" + distance + "]";
    }

}
