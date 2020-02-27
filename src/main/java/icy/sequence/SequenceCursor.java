package icy.sequence;

import java.util.concurrent.atomic.AtomicBoolean;

public class SequenceCursor
{
    private Sequence seq;
    private VolumetricImageCursor[] volumeCursors;
    private AtomicBoolean sequenceChanged;

    /**
     * 
     */
    public SequenceCursor(Sequence seq)
    {
        this.seq = seq;
        this.volumeCursors = new VolumetricImageCursor[seq.getSizeT()];
        this.sequenceChanged = new AtomicBoolean();
        this.currentT = -1;
    }

    public double get(int x, int y, int z, int t, int c) throws IndexOutOfBoundsException, RuntimeException
    {
        return getVolumeCursor(t).get(x, y, z, c);
    }

    public synchronized void set(int x, int y, int z, int t, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getVolumeCursor(t).set(x, y, z, c, val);
        sequenceChanged.set(true);
    }

    public synchronized void setSafe(int x, int y, int z, int t, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getVolumeCursor(t).setSafe(x, y, z, c, val);
        sequenceChanged.set(true);
    }

    private int currentT;
    private VolumetricImageCursor currentCursor;

    private synchronized VolumetricImageCursor getVolumeCursor(int t) throws IndexOutOfBoundsException
    {
        if (currentT != t)
        {
            currentCursor = new VolumetricImageCursor(seq, t);
        }
        return currentCursor;
    }

    public synchronized void commitChanges()
    {
        if (sequenceChanged.get())
        {
            for (int i = 0; i < volumeCursors.length; i++)
            {
                if (volumeCursors[i] != null)
                    volumeCursors[i].commitChanges();
            }
            sequenceChanged.set(false);
        }
    }
}
