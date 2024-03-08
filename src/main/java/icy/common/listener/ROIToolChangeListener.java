package icy.common.listener;

import icy.plugin.interface_.PluginROI;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface ROIToolChangeListener extends EventListener {
    void toolChanged(@NotNull PluginROI plugin);
}
