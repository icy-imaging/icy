/**
 * 
 */
package plugins.kernel.image.filtering.selection;

/**
 * Search for neighboring pixels up to a given distance and keeps only local maximum pixels.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class LocalMaxMethod extends ThreadedSelectionFilter
{

    @Override
    public String getFilterName()
    {
        return "LOCAL_MAX";
    }

    @Override
    public double processNeighborhood(double currentValue, double[] neighborhood, int neighborhoodSize)
    {
        double neighborValue, defaultValue = 0d;
        for (int i = 0; i < neighborhoodSize; i++)
        {
            neighborValue = neighborhood[i];
            if (neighborValue > currentValue)
                return 0d;
            if (defaultValue == 0d && neighborValue < currentValue)
                defaultValue = 1d;
        }

        return defaultValue;
    }

}
