package icy.common.listener;

import java.util.EventListener;

public interface ROIToolChangeListener extends EventListener {
    void toolChanged(String tool);
}
