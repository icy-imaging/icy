/**
 * 
 */
package plugins.kernel.roi.descriptor.intensity;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceEvent.SequenceEventSourceType;

/**
 * Standard Deviation intensity ROI descriptor class (see {@link ROIDescriptor})
 * 
 * @author Stephane
 */
public class ROIStandardDeviationDescriptor extends ROIDescriptor
{
    public static final String ID = "Standard deviation";

    public ROIStandardDeviationDescriptor()
    {
        super(ID, "Standard Deviation", Double.class);
    }

    @Override
    public String getDescription()
    {
        return "Standard deviation";
    }

    @Override
    public boolean separateChannel()
    {
        return true;
    }

    @Override
    public boolean needRecompute(SequenceEvent change)
    {
        return (change.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA);
    }

    @Override
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        return Double.valueOf(computeStandardDeviation(roi, sequence));
    }

    /**
     * Computes and returns the compute standard deviation for the specified ROI on given sequence.<br>
     * It may returns <code>Double.Nan</code> if the operation is not supported for that ROI.
     * 
     * @param roi
     *        the ROI on which we want to compute the standard deviation
     * @param sequence
     *        the sequence used to compute the pixel intensity
     * @throws UnsupportedOperationException
     *         if the operation is not supported for this ROI
     * @throws InterruptedException 
     */
    public static double computeStandardDeviation(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException
    {
        return ROIIntensityDescriptorsPlugin.computeIntensityDescriptors(roi, sequence, false).deviation;
    }
}
