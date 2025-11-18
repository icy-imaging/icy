package plugins.kernel.image.filtering.convolution;

import icy.sequence.Sequence;

public interface IKernel
{
    Sequence toSequence();

    double[] getData();
}
