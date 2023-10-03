package icy.type.geom;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * @deprecated Use {@link GeomUtil} instead
 */
@Deprecated(since = "2.4.3", forRemoval = true)
public class Line2DUtil
{
    /**
     * @param lineA
     *        line 1
     * @param lineB
     *        line 2
     * @return intersection point
     * @deprecated Use {@link GeomUtil#getIntersection(Line2D, Line2D)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static Point2D getIntersection(Line2D lineA, Line2D lineB)
    {
        return GeomUtil.getIntersection(lineA, lineB);
    }

    /**
     * @param lineA
     *        line 1
     * @param lineB
     *        line 2
     * @param limitToSegmentA
     * @param limitToSegmentB
     * @return intersection point
     * @deprecated Use {@link GeomUtil#getIntersection(Line2D, Line2D)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static Point2D getIntersection(Line2D lineA, Line2D lineB, boolean limitToSegmentA, boolean limitToSegmentB)
    {
        return GeomUtil.getIntersection(lineA, lineB, limitToSegmentA, limitToSegmentB);
    }
}
