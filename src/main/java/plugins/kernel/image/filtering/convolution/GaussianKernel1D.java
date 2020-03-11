package plugins.kernel.image.filtering.convolution;

import java.util.Arrays;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

public class GaussianKernel1D implements IKernel1D
{

    private double[] data;

    private GaussianKernel1D(double[] data)
    {
        this.data = data;
    }

    public static GaussianKernel1D create(double sigma)
    {
        double[] data;
        if (sigma < 1e-10)
        {
            data = new double[] {1};
        }
        else
        {
            double sigma2 = sigma * sigma;
            int k = (int) Math.ceil(sigma * 3.0f);

            int width = 2 * k + 1;

            data = new double[width];

            for (int i = -k; i <= k; i++)
                data[i + k] = 1.0 / (Math.sqrt(2 * Math.PI) * sigma * Math.exp(((i * i) / sigma2) * 0.5f));
            normalize(data);
        }
        return new GaussianKernel1D(data);
    }

    private static void normalize(double[] data)
    {
        double accu = 0;

        for (double d : data)
            accu += d;

        if (accu != 1 && accu != 0)
        {
            for (int i = 0; i < data.length; i++)
                data[i] /= accu;
        }
    }

    @Override
    public Sequence toSequence()
    {
        IcyBufferedImage image = new IcyBufferedImage(data.length, 1, 1, DataType.DOUBLE);
        image.setDataXYAsDouble(0, data);
        Sequence kernel = new Sequence(image);
        kernel.setName(this.toString());
        return kernel;
    }

    @Override
    public double[] getData()
    {
        return data;
    }

    @Override
    public boolean isSeparable()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "GaussianKernel1D: " + Arrays.toString(data);
    }

}
