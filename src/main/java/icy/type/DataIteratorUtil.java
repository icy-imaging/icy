/**
 * 
 */
package icy.type;

import icy.image.ImageDataIterator;
import icy.sequence.SequenceDataIterator;

/**
 * Utilities for {@link DataIterator} classes.
 * 
 * @author Stephane
 */
public class DataIteratorUtil
{
    /**
     * Returns the number of element contained in the specified {@link DataIterator}.
     * 
     * @throws InterruptedException
     */
    public static long count(DataIterator it) throws InterruptedException
    {
        long result = 0;

        it.reset();

        while (!it.done())
        {
            it.next();
            result++;

            // check for interruption from time to time as this can be a long process
            if (((result & 0xFFF) == 0xFFF) && Thread.interrupted())
                throw new InterruptedException("DataIteratorUtil.count(..) process interrupted.");
        }

        return result;
    }

    /**
     * Sets the specified value to the specified {@link DataIterator}.
     * 
     * @throws InterruptedException
     */
    public static void set(DataIterator it, double value) throws InterruptedException
    {
        it.reset();

        try
        {
            int i = 0;
            while (!it.done())
            {
                it.set(value);
                it.next();

                // check for interruption from time to time as this can be a long process
                if (((i & 0xFFF) == 0xFFF) && Thread.interrupted())
                    throw new InterruptedException("DataIteratorUtil.set(..) process interrupted.");
            }
        }
        finally
        {
            // not really nice to do that here, but it's to preserve backward compatibility
            if (it instanceof SequenceDataIterator)
                ((SequenceDataIterator) it).flush();
            else if (it instanceof ImageDataIterator)
                ((ImageDataIterator) it).flush();
        }
    }
}
