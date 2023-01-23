package icy.common.listener.weak;

import icy.common.listener.SkinChangeListener;
import icy.gui.util.LookAndFeelUtil;

public class WeakSkinChangeListener extends WeakListener<SkinChangeListener> implements SkinChangeListener {
    public WeakSkinChangeListener(SkinChangeListener listener)
    {
        super(listener);
    }

    @Override
    public void removeListener(Object source)
    {
        LookAndFeelUtil.removeListener(this);
    }

    @Override
    public void skinChanged()
    {
        final SkinChangeListener listener = getListener();

        if (listener != null)
            listener.skinChanged();
    }
}
