package plugins.kernel.roi.tool.morphology.watershed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WatershedNode implements Comparable<WatershedNode>
{
    /**
     * Unlabeled node. Initial state of all nodes
     */
    public static final int NO_LABEL = -1;
    /**
     * Node that will be taken into account during this flooding iteration.
     */
    public static final int TO_BE_MARKED = -2;
    /**
     * Node that won't be handled but will serve as flag to indicate that distance must be increased on the flooding step.
     */
    public static final int HEIGHT_FLAG = -3;
    /**
     * Watershed node. When the node has been declared to divide to different sets of labeled nodes
     */
    public static final int WATERSHED = 0;

    public static final WatershedNode FLAG_NODE;
    static
    {
        FLAG_NODE = new WatershedNode(-1, -1, -1, -1, new ArrayList<WatershedNode>(0));
        FLAG_NODE.label = HEIGHT_FLAG;
    }

    private int x;
    private int y;
    private int z;
    private double height;
    private List<WatershedNode> neighbors;

    private double distance;
    private int label;

    public WatershedNode(int x, int y, int z, double height, List<WatershedNode> neighbors)
    {
        Objects.requireNonNull(neighbors, "List of neighbors is null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.height = height;
        this.neighbors = neighbors;
        this.distance = 0;
        this.label = NO_LABEL;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    void addNeighbor(WatershedNode n)
    {
        neighbors.add(n);
    }

    private List<WatershedNode> unmodifiableNegihbors;

    public List<WatershedNode> getNeighbors()
    {
        synchronized (this)
        {
            if (unmodifiableNegihbors == null)
            {
                unmodifiableNegihbors = Collections.unmodifiableList(neighbors);
            }
        }
        return unmodifiableNegihbors;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    public double getDistance()
    {
        return distance;
    }

    public void setLabel(int label)
    {
        this.label = label;
    }

    public int getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + "), height=" + height + ", label=" + label + ", distance=" + distance;
    }

    @Override
    public int compareTo(WatershedNode o)
    {
        if (o.height < height)
            return 1;
        if (o.height > height)
            return -1;
        return 0;
    }

    public void suffleNeighbors()
    {
        Collections.shuffle(neighbors);
    }

}
