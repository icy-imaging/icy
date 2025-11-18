/**
 * 
 */
package plugins.kernel.roi.morphology.skeletonization;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceCursor;
import icy.type.DataType;
import icy.type.dimension.Dimension3D;
import icy.type.point.Point3D;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class TopologicalDescriptor
{
    public static class CostElement implements Comparable<CostElement>
    {
        private final double cost;
        private final Point3D.Integer point;

        public CostElement(double cost, final Point3D.Integer point)
        {
            this.cost = cost;
            this.point = point;
        }

        public double getCost()
        {
            return cost;
        }

        public Point3D.Integer getPoint()
        {
            return point;
        }

        // inverted to use max in priority queue
        @Override
        public int compareTo(CostElement ce)
        {
            if (cost > ce.getCost())
                return -1;
            if (cost < ce.getCost())
                return 1;
            return 0;
        }

    }

    public static final double DEFAULT_VISITING_RADIUS_SCALE = 1.5d;

    private Sequence distanceMap;
    private Sequence costMap;
    private Sequence minSpanningTree;
    private Dimension3D pixelSize;
    private double visitingRadiusScale;

    public TopologicalDescriptor(Sequence distanceMap, Sequence costMap, Sequence minSpanningTree,
            Dimension3D pixelSize)
    {
        this(distanceMap, costMap, minSpanningTree, pixelSize, DEFAULT_VISITING_RADIUS_SCALE);
    }

    public TopologicalDescriptor(Sequence distanceMap, Sequence costMap, Sequence minSpanningTree,
            Dimension3D pixelSize, double visitingRadiusScale)
    {
        this.distanceMap = distanceMap;
        this.costMap = costMap;
        this.minSpanningTree = minSpanningTree;
        this.pixelSize = pixelSize;
        this.visitingRadiusScale = visitingRadiusScale;
    }

    private PriorityQueue<CostElement> queue;
    private int currentBranchId;

    private Point3D.Integer rootPosition;

    public void compute()
    {
        fillFloodQueue();
        initializeResult();
        currentBranchId = 1;

        if (!queue.isEmpty())
            rootPosition = queue.peek().point;

        while (!queue.isEmpty())
        {
            CostElement currentElement = queue.poll();
            Point3D.Integer currentPosition = currentElement.point;
            if (!isPositionLabeled(currentPosition))
            {
                visitSphere(currentPosition);
                setLeafPosition(currentPosition);
                Point3D.Integer parentPosition = getParentPosition(currentPosition);
                Point3D.Double distPos = new Point3D.Double();
                double dist = 0;
                double lastRadius = getDistanceMapCursor().get(currentPosition.x, currentPosition.y, currentPosition.z,
                        0, 0);
                do
                {
                    currentPosition = parentPosition;
                    parentPosition = getParentPosition(currentPosition);
                    distPos.setLocation(parentPosition);
                    distPos.z -= currentPosition.z;
                    distPos.y -= currentPosition.y;
                    distPos.x -= currentPosition.x;
                    distPos.x *= pixelSize.getSizeX();
                    distPos.y *= pixelSize.getSizeY();
                    distPos.z *= pixelSize.getSizeZ();
                    dist += Math.sqrt(distPos.x * distPos.x + distPos.y * distPos.y + distPos.z * distPos.z);

                    if (!isSkeletonPosition(currentPosition))
                    {
                        setSkeletonPosition(currentPosition);
                        if (dist > lastRadius * 1.1)
                            visitSphere(currentPosition);
                    }
                    else
                    {
                        setBranchPosition(currentPosition);
                        visitSphere(currentPosition);
                        currentBranchId++;
                        break;
                    }
                }
                while (!parentPosition.equals(currentElement.point));
                visitSphere(currentPosition);
            }
        }
        commitChanges();
        Icy.getMainInterface().addSequence(distanceMap);
        Icy.getMainInterface().addSequence(labels);
        Icy.getMainInterface().addSequence(skeleton);
        Icy.getMainInterface().addSequence(leafMap);
        Icy.getMainInterface().addSequence(branchMap);
    }

    private void fillFloodQueue()
    {
        queue = new PriorityQueue<>();

        Point3D.Integer point;
        CostElement ce;
        for (int z = 0; z < distanceMap.getSizeZ(); z++)
        {
            for (int y = 0; y < distanceMap.getSizeY(); y++)
            {
                for (int x = 0; x < distanceMap.getSizeX(); x++)
                {
                    if (getDistanceMapCursor().get(x, y, z, 0, 0) > 0)
                    {
                        point = new Point3D.Integer(x, y, z);
                        ce = new CostElement(computeEndness(point), point);
                        queue.add(ce);
                    }
                }
            }
        }
    }

    private SequenceCursor distanceMapCursor;

    private SequenceCursor getDistanceMapCursor()
    {
        if (distanceMapCursor == null)
            distanceMapCursor = new SequenceCursor(distanceMap);
        return distanceMapCursor;
    }

    private double computeEndness(Point3D.Integer point)
    {
        double distance = getDistanceMapCursor().get(point.x, point.y, point.z, 0, 0);
        return getCostMapCursor().get(point.x, point.y, point.z, 0, 0) / (distance * distance);
    }

    private SequenceCursor costMapCursor;

    private SequenceCursor getCostMapCursor()
    {
        if (costMapCursor == null)
            costMapCursor = new SequenceCursor(costMap);
        return costMapCursor;
    }

    private Sequence labels;
    private Sequence leafMap;
    private Sequence branchMap;
    private Sequence skeleton;

    private void initializeResult()
    {
        labels = new Sequence("Labels");
        leafMap = new Sequence("LeafNodes");
        branchMap = new Sequence("BranchNodes");
        skeleton = new Sequence("Skeleton");

        for (int k = 0; k < distanceMap.getSizeZ(); k++)
        {
            labels.addImage(new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 1, DataType.INT));
            leafMap.addImage(new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 1, DataType.BYTE));
            branchMap.addImage(new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 1, DataType.BYTE));
            skeleton.addImage(new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 1, DataType.BYTE));
        }
    }

    private void visitSphere(Point3D.Integer center)
    {
        if (!isPositionInSequence(center))
            return;

        double sphereRadius = getDistanceMapCursor().get(center.x, center.y, center.z, 0, 0) * visitingRadiusScale;
        double squaredSphereRadius = Math.ceil(sphereRadius * sphereRadius);

        Queue<Point3D.Integer> sphereQueue = new LinkedList<Point3D.Integer>();
        sphereQueue.add(center);

        Set<Point3D.Integer> visitedPoints = new TreeSet<Point3D.Integer>(Comparator.comparingDouble(Point3D::getX)
                .thenComparingDouble(Point3D::getY).thenComparing(Point3D::getZ));
        Point3D.Integer currentPosition, candidatePosition;
        while (!sphereQueue.isEmpty())
        {
            currentPosition = sphereQueue.poll();
            if (visitedPoints.contains(currentPosition))
                continue;

            visitedPoints.add(currentPosition);

            if (!isPositionLabeled(currentPosition))
            {
                getLabelsCursor().set(currentPosition.x, currentPosition.y, currentPosition.z, 0, 0, currentBranchId);
            }

            for (int k = -1; k < 2; k++)
            {
                for (int j = -1; j < 2; j++)
                {
                    for (int i = -1; i < 2; i++)
                    {
                        candidatePosition = new Point3D.Integer(currentPosition.x + i, currentPosition.y + j,
                                currentPosition.z + k);
                        if (!isPositionInSequence(candidatePosition))
                            continue;
                        if (visitedPoints.contains(candidatePosition))
                            continue;
                        if (getSquaredDistance(center, candidatePosition) > squaredSphereRadius)
                            continue;

                        sphereQueue.add(candidatePosition);
                    }
                }
            }
        }
    }

    private double getSquaredDistance(Point3D.Integer a, Point3D.Integer b)
    {
        double dx = (b.x - a.x) * pixelSize.getSizeX();
        double dy = (b.y - a.y) * pixelSize.getSizeY();
        double dz = (b.z - a.z) * pixelSize.getSizeZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean isPositionInSequence(Point3D.Integer p)
    {
        return p.x >= 0 && p.x < distanceMap.getSizeX() && p.y >= 0 && p.y < distanceMap.getSizeY() && p.z >= 0
                && p.z < distanceMap.getSizeZ();
    }

    private boolean isPositionLabeled(Point3D.Integer point)
    {
        return getLabelsCursor().get(point.x, point.y, point.z, 0, 0) != 0;
    }

    private SequenceCursor labelsCursor;

    private SequenceCursor getLabelsCursor()
    {
        if (labelsCursor == null)
            labelsCursor = new SequenceCursor(labels);
        return labelsCursor;
    }

    private Point3D.Integer getParentPosition(Point3D.Integer point)
    {
        Point3D.Integer parentPosition = new Point3D.Integer();
        parentPosition.x = (int) getMinSpanningTreeCursor().get(point.x, point.y, point.z, 0, 0);
        parentPosition.y = (int) getMinSpanningTreeCursor().get(point.x, point.y, point.z, 0, 1);
        parentPosition.z = (int) getMinSpanningTreeCursor().get(point.x, point.y, point.z, 0, 2);
        return parentPosition;
    }

    private SequenceCursor minSpanningTreeCursor;

    private SequenceCursor getMinSpanningTreeCursor()
    {
        if (minSpanningTreeCursor == null)
            minSpanningTreeCursor = new SequenceCursor(minSpanningTree);
        return minSpanningTreeCursor;
    }

    private void setLeafPosition(Point3D.Integer p)
    {
        setSkeletonPosition(p);
        getLeafsCursor().set(p.x, p.y, p.z, 0, 0, 1);
    }

    private void setSkeletonPosition(Point3D.Integer p)
    {
        getSkeletonCursor().set(p.x, p.y, p.z, 0, 0, 1);
    }

    private SequenceCursor leafsCursor;

    private SequenceCursor getLeafsCursor()
    {
        if (leafsCursor == null)
        {
            leafsCursor = new SequenceCursor(leafMap);
        }
        return leafsCursor;
    }

    private boolean isSkeletonPosition(Point3D.Integer p)
    {
        return getSkeletonCursor().get(p.x, p.y, p.z, 0, 0) != 0;
    }

    private SequenceCursor skeletonCursor;

    private SequenceCursor getSkeletonCursor()
    {
        if (skeletonCursor == null)
        {
            skeletonCursor = new SequenceCursor(skeleton);
        }
        return skeletonCursor;
    }

    private void setBranchPosition(Point3D.Integer p)
    {
        getBranchCursor().set(p.x, p.y, p.z, 0, 0, 1);
    }

    private SequenceCursor branchesCursor;

    private SequenceCursor getBranchCursor()
    {
        if (branchesCursor == null)
            branchesCursor = new SequenceCursor(branchMap);
        return branchesCursor;
    }

    private void commitChanges()
    {
        getLabelsCursor().commitChanges();
        getLabelsCursor().commitChanges();
        getBranchCursor().commitChanges();
        getSkeletonCursor().commitChanges();
    }

    public Point3D.Integer getRootPosition()
    {
        return rootPosition;
    }
}
