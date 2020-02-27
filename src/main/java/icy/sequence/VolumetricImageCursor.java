package icy.sequence;

import java.util.concurrent.atomic.AtomicBoolean;

import icy.image.IcyBufferedImageCursor;

public class VolumetricImageCursor
{
    private VolumetricImage vol;

    private AtomicBoolean volumeChanged;

    private IcyBufferedImageCursor[] planeCursors;

    /**
     * 
     */
    public VolumetricImageCursor(VolumetricImage vol)
    {
        this.vol = vol;
        planeCursors = new IcyBufferedImageCursor[vol.getSize()];
        volumeChanged = new AtomicBoolean(false);
        currentZ = -1;
    }

    public VolumetricImageCursor(Sequence seq, int t)
    {
        this(seq.getVolumetricImage(t));
    }

    public double get(int x, int y, int z, int c) throws IndexOutOfBoundsException, RuntimeException
    {
        return getPlaneCursor(z).get(x, y, c);

    }

    private IcyBufferedImageCursor currentCursor;
    private int currentZ;

    private synchronized IcyBufferedImageCursor getPlaneCursor(int z) throws IndexOutOfBoundsException
    {
        if (currentZ != z)
        {
            if (planeCursors[z] == null)
            {
                currentCursor = new IcyBufferedImageCursor(vol.getImage(z));
                planeCursors[z] = currentCursor;
            }
        }
        return currentCursor;
    }

    public synchronized void set(int x, int y, int z, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getPlaneCursor(z).set(x, y, c, val);
        volumeChanged.set(true);
    }

    public synchronized void setSafe(int x, int y, int z, int c, double val)
            throws IndexOutOfBoundsException, RuntimeException
    {
        getPlaneCursor(z).setSafe(x, y, c, val);
        volumeChanged.set(true);
    }

    public synchronized void commitChanges()
    {
        if (volumeChanged.get())
        {
            for (int i = 0; i < planeCursors.length; i++)
            {
                if (planeCursors[i] != null)
                    planeCursors[i].commitChanges();
            }
        }
    }
}
