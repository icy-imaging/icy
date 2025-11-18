package plugins.kernel.image.filtering.convolution;

public class ConvolutionException extends RuntimeException
{

    static final long serialVersionUID = -9042546074193202791L;

    public ConvolutionException()
    {
        super();
    }

    public ConvolutionException(String message)
    {
        super(message);
    }

    public ConvolutionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
