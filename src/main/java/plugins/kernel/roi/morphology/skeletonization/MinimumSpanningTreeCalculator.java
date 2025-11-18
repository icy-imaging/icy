package plugins.kernel.roi.morphology.skeletonization;

import java.util.PriorityQueue;

import icy.image.IcyBufferedImage;
import icy.image.ImageDataIterator;
import icy.sequence.Sequence;
import icy.sequence.SequenceCursor;
import icy.sequence.SequenceDataIterator;
import icy.type.DataType;
import icy.type.dimension.Dimension3D;
import icy.type.point.Point3D;

/**
 * This class computes the minimum spanning tree of a distance map sequence taking as seed the maximum distance found on the distance map (Only one seed means
 * that it does not work with disconnected components).
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class MinimumSpanningTreeCalculator
{
    /**
     * This class represents the possibility of moving from some position to a given one {@code position} with a given cost {@code cost}.
     */
    private static class CostElement implements Comparable<CostElement>
    {
        private final double cost;
        private final Point3D position;

        public CostElement(double cost, Point3D position)
        {
            this.cost = cost;
            this.position = position;
        }

        /**
         * @return Cost to get to the position.
         */
        public double getCost()
        {
            return cost;
        }

        /**
         * @return Position.
         */
        public Point3D getPosition()
        {
            return position;
        }

        @Override
        public int compareTo(CostElement o)
        {
            if (cost < o.cost)
                return -1;
            if (cost > o.cost)
                return 1;
            return 0;
        }

    }

    /**
     * Distance map on which the minimum spanning tree is built from.
     */
    private Sequence distanceMap;
    private Dimension3D pixelSize;

    /**
     * @param distanceMap
     *        Distance map from which the minimum spanning tree is built from.
     */
    public MinimumSpanningTreeCalculator(Sequence distanceMap, Dimension3D pixelSize)
    {
        this.distanceMap = distanceMap;
        this.pixelSize = pixelSize;
    }

    /**
     * 3D image where each pixel contains information of its parent pixel.
     */
    private Sequence tree;

    /**
     * This method tries to reuse the previous result of the computation if it has already been computed. Otherwise, the tree is computed before returning.
     * 
     * @return Minimum spanning tree. A 3D image where each pixel contains information of its parent pixel.
     * @throws InterruptedException
     *         If computing the tree gets interrupted.
     */
    public Sequence getTree() throws InterruptedException
    {
        if (tree == null)
        {
            compute();
        }
        return tree;
    }

    /**
     * 3D image where each pixel contains information of the cost to get to the seed.
     */
    private Sequence costs;

    /**
     * This method tries to reuse the previous result of the computation if it has already been computed. Otherwise, the tree is computed before returning.
     * 
     * @return Travel costs. A 3D image where each pixel contains the information about the cost to get to the seed from that position.
     * @throws InterruptedException
     *         If computing the tree gets interrupted.
     */
    public Sequence getCosts() throws InterruptedException
    {
        if (costs == null)
        {
            compute();
        }
        return costs;
    }

    private Sequence visited;

    /**
     * @return Visited pixels. A 3D image where each pixel is set to 1 if it has been visited on the process of computing the tree.
     * @throws InterruptedException
     *         If computing the tree gets interrupted.
     */
    public Sequence getVisitedPixels() throws InterruptedException
    {
        if (visited == null)
        {
            compute();
        }
        return visited;

    }

    /**
     * Starting position for the Dijkstra algorithm.
     */
    private Point3D seedPoint;
    /**
     * Queue used to find the minimum spanning tree.
     */
    private PriorityQueue<CostElement> queue;

    /**
     * Computes the minimum spanning tree. As a result, two sequences are generated: A parent sequence, providing the parent position of each pixel; and a cost
     * sequence, which contains the costs to get to each of the pixels.
     */
    public void compute() throws InterruptedException
    {
        seedPoint = findSeed();
        if (seedPoint != null)
        {
            initializeResult();
            initializePriorityQueue();
            queue.add(new CostElement(0d, seedPoint));
            setCost(seedPoint, 0);
            setTreeParent(seedPoint, seedPoint);

            while (!queue.isEmpty())
            {
                if (Thread.interrupted())
                    throw new InterruptedException("ROI minimum spanning tree computation interrupted.");

                CostElement currentElement = queue.poll();
                visitPosition(currentElement.getPosition());
                checkNeighbors(currentElement);
            }
            commitChanges();
        }
    }

    /**
     * @return Seed position with maximal positive distance on the distance map. Null if no maximal seed is found.
     * @throws InterruptedException 
     */
    private Point3D findSeed() throws InterruptedException
    {
        Point3D seedPoint = new Point3D.Double(-1, -1, -1);
        double maxValue = Double.NEGATIVE_INFINITY;
        for (SequenceDataIterator it = new SequenceDataIterator(distanceMap); !it.done(); it.next())
        {
            if (it.get() > maxValue)
            {
                seedPoint.setLocation(it.getPositionX(), it.getPositionY(), it.getPositionZ());
                maxValue = it.get();
            }
        }
        if (maxValue <= 0d)
            seedPoint = null;

        return seedPoint;
    }

    /**
     * Initializes the tree and costs sequences. tree values will all point to position (-1,-1,-1) and costs will be {@link Double.POSITIVE_INFINITY}.
     */
    private void initializeResult()
    {
        tree = new Sequence("MinimumSpanningTree");
        costs = new Sequence("Costs");
        visited = new Sequence("Visited");
        for (int z = 0; z < distanceMap.getSizeZ(); z++)
        {
            IcyBufferedImage treeImage = new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 3,
                    DataType.INT);
            fillImage(treeImage, 0, -1);
            fillImage(treeImage, 1, -1);
            fillImage(treeImage, 2, -1);
            tree.addImage(treeImage);

            IcyBufferedImage costImage = new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 1,
                    DataType.DOUBLE);
            fillImage(costImage, 0, Double.POSITIVE_INFINITY);
            costs.addImage(costImage);

            IcyBufferedImage visitedImage = new IcyBufferedImage(distanceMap.getSizeX(), distanceMap.getSizeY(), 1,
                    DataType.BYTE);
            visited.addImage(visitedImage);
        }

        directionalCosts = new double[3 * 3 * 3];
        double kSq, jSq, iSq;
        for (int k = -1; k < 2; k++)
        {
            kSq = k * pixelSize.getSizeZ();
            kSq *= kSq;
            for (int j = -1; j < 2; j++)
            {
                jSq = j * pixelSize.getSizeY();
                jSq *= jSq;
                for (int i = -1; i < 2; i++)
                {
                    if (i == 0 && j == 0 && k == 0)
                        continue;

                    iSq = i * pixelSize.getSizeX();
                    iSq *= iSq;
                    directionalCosts[(k + 1) * 9 + (j + 1) * 3 + (i + 1)] = Math.sqrt(kSq + jSq + iSq);
                }
            }
        }
    }

    /**
     * Sets all the pixels of the given {@code channel} in the given {@code image} with the provided {@code value}.
     * 
     * @param image
     *        Target image.
     * @param channel
     *        Target channel.
     * @param value
     *        value to set.
     */
    private void fillImage(IcyBufferedImage image, int channel, double value)
    {
        ImageDataIterator it;
        for (it = new ImageDataIterator(image, channel); !it.done(); it.next())
        {
            it.set(value);
        }
        it.flush();
    }

    /**
     * Creates the priority queue.
     */
    private void initializePriorityQueue()
    {
        queue = new PriorityQueue<MinimumSpanningTreeCalculator.CostElement>();
    }

    private void visitPosition(Point3D position)
    {
        getVisitedCursor().set((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 0, 1);
    }

    private double[] directionalCosts;

    /**
     * Goes through every neighbor of the
     * 
     * @param currentElement
     */
    private void checkNeighbors(CostElement currentElement)
    {
        int z, y, x;
        Point3D candidatePosition = new Point3D.Double();
        double candidateCost;
        CostElement candidateElement;
        for (int k = -1; k < 2; k++)
        {
            z = (int) (currentElement.getPosition().getZ()) + k;
            if (z < 0 || z >= tree.getSizeZ())
                continue;

            for (int j = -1; j < 2; j++)
            {
                y = (int) (currentElement.getPosition().getY()) + j;
                if (y < 0 || y >= tree.getSizeY())
                    continue;

                for (int i = -1; i < 2; i++)
                {
                    if (k == 0 && j == 0 && i == 0)
                        continue;

                    x = (int) (currentElement.getPosition().getX()) + i;
                    if (x < 0 || x >= tree.getSizeX())
                        continue;

                    candidatePosition.setLocation(currentElement.getPosition().getX() + i,
                            currentElement.getPosition().getY() + j, currentElement.getPosition().getZ() + k);

                    if (isPositionVisited(candidatePosition))
                        continue;

                    candidateCost = currentElement.getCost()
                            + getInvSqDistance(candidatePosition) * getDirectionalCost(i, j, k);
                    if (candidateCost < getCost(candidatePosition))
                    {
                        setCost(candidatePosition, candidateCost);
                        setTreeParent(candidatePosition, currentElement.getPosition());
                        candidateElement = new CostElement(candidateCost, (Point3D) candidatePosition.clone());
                        queue.add(candidateElement);
                    }
                }
            }
        }
    }

    private boolean isPositionVisited(Point3D position)
    {
        return getVisitedCursor().get((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 0) != 0;
    }

    private SequenceCursor visitedCursor;

    private SequenceCursor getVisitedCursor()
    {
        if (visitedCursor == null)
        {
            visitedCursor = new SequenceCursor(visited);
        }
        return visitedCursor;
    }

    private double getDirectionalCost(int i, int j, int k)
    {
        return directionalCosts[i + 1 + (j + 1) * 3 + (k + 1) * 9];
    }

    private void setCost(Point3D position, double value)
    {
        getCostsCursor().set((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 0, value);
    }

    private SequenceCursor costsCursor;

    private SequenceCursor getCostsCursor()
    {
        if (costsCursor == null)
        {
            costsCursor = new SequenceCursor(costs);
        }
        return costsCursor;
    }

    private void setTreeParent(Point3D position, Point3D parent)
    {
        getTreeCursor().set((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 0, parent.getX());
        getTreeCursor().set((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 1, parent.getY());
        getTreeCursor().set((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 2, parent.getZ());
    }

    private SequenceCursor treeCursor;

    private SequenceCursor getTreeCursor()
    {
        if (treeCursor == null)
        {
            treeCursor = new SequenceCursor(tree);
        }
        return treeCursor;
    }

    private double getInvSqDistance(Point3D position)
    {
        return getInvSquared(
                getDistanceMapCursor().get((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 0));
    }

    private double getInvSquared(double distance)
    {
        return distance <= 0d ? Double.POSITIVE_INFINITY : 1d / (distance * distance);
    }

    private SequenceCursor distanceMapCursor;

    private SequenceCursor getDistanceMapCursor()
    {
        if (distanceMapCursor == null)
        {
            distanceMapCursor = new SequenceCursor(distanceMap);
        }
        return distanceMapCursor;
    }

    private double getCost(Point3D position)
    {
        return getCostsCursor().get((int) position.getX(), (int) position.getY(), (int) position.getZ(), 0, 0);
    }

    private void commitChanges()
    {
        getCostsCursor().commitChanges();
        getTreeCursor().commitChanges();
        getVisitedCursor().commitChanges();
    }
}
