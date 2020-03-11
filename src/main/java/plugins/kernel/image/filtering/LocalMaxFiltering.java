package plugins.kernel.image.filtering;

import icy.sequence.Sequence;
import plugins.kernel.image.filtering.selection.SelectionFilter;
import plugins.kernel.image.filtering.selection.LocalMaxMethod;

public class LocalMaxFiltering
{
    private Sequence sequence;
    private int[] radius;

    public static LocalMaxFiltering create(Sequence sequence, int... radius)
    {
        return new LocalMaxFiltering(sequence, radius);
    }

    private LocalMaxFiltering(Sequence sequence, int... radius)
    {
        this.sequence = sequence;
        this.radius = radius;
    }

    private Sequence result;

    public void computeFiltering() throws RuntimeException, InterruptedException
    {
        SelectionFilter filter = new LocalMaxMethod();
        result = filter.processSequence(this.sequence, this.radius);
    }

    public Sequence getFilteredSequence()
    {
        return result;
    }
}
