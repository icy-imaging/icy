package plugins.kernel.image.filtering.selection;

import icy.sequence.Sequence;

public interface SelectionFilter
{

    /**
     * Filter the given sequence with the specified non-linear filter on the specified (square)
     * neighborhood. Note that some operations require double floating-point precision, therefore
     * the input sequence will be internally converted to double precision. However the result will
     * be converted back to the same type as the given input sequence <i>with re-scaling</i>.
     * 
     * @param stopFlag
     *        a flag variable that will stop the filtering process if set to true (this flag is
     *        first set to false when starting this method)
     * @param sequence
     *        the sequence to filter (its data will be overwritten)
     * @param filterType
     *        the type of filter to apply
     * @param radius
     *        the neighborhood radius in each dimension (the actual neighborhood size will be
     *        <code>1+(2*radius)</code> to ensure it is centered on each pixel). If a single
     *        value is given, this value is used for all sequence dimensions. If two values are
     *        given for a 3D sequence, the filter is considered in 2D and applied to each Z
     *        section independently.
     * @throws InterruptedException
     *         If the execution gets cancelled.
     */
    Sequence processSequence(Sequence sequence, int... radius) throws RuntimeException, InterruptedException;

    String getFilterName();

    double processNeighborhood(double currentValue, double[] neighborhood, int neighborhoodSize);

}
