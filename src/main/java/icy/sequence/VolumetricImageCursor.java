package icy.sequence;

import java.util.concurrent.atomic.AtomicBoolean;

import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageCursor;
import icy.type.DataType;

public class VolumetricImageCursor
{
    VolumetricImage vol;
    int[] volSize;
    DataType volType;

    AtomicBoolean volumeChanged;

    IcyBufferedImageCursor[] planeCursors;

    /**
     * 
     */
    public VolumetricImageCursor(VolumetricImage vol)
    {
        this.vol = vol;
        IcyBufferedImage firstPlane = vol.getFirstImage();
        this.volSize = new int[] {firstPlane.getSizeX(), firstPlane.getSizeY(), vol.getImages().size()};
        this.volType = firstPlane.getDataType_();
        planeCursors = new IcyBufferedImageCursor[vol.getSize()];
        volumeChanged = new AtomicBoolean(false);
        currentZ = -1;
    }

    public VolumetricImageCursor(Sequence seq, int t)
    {
        this(seq.getVolumetricImage(t));
    }

    public double get(int x, int y, int z, int c) throws RuntimeException
    {
        return getPlaneCursor(z).get(x, y, c);

    }

    private IcyBufferedImageCursor currentCursor;
    private int currentZ;

    private synchronized IcyBufferedImageCursor getPlaneCursor(int z)
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

    public synchronized void set(int x, int y, int z, int c, double val) throws RuntimeException
    {
        getPlaneCursor(z).set(x, y, c, val);
        volumeChanged.set(true);
    }

    public synchronized void setSafe(int x, int y, int z, int c, double val)
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
