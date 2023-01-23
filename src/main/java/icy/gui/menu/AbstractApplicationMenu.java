package icy.gui.menu;

import icy.common.listener.SkinChangeListener;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.main.GlobalROIListener;
import icy.gui.main.GlobalSequenceListener;
import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.resource.icon.IcyIconFont;
import icy.resource.icon.IcyIconFont.State;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import jiconfont.IconCode;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractApplicationMenu extends JMenu implements SkinChangeListener, GlobalSequenceListener, PluginLoaderListener, ActiveSequenceListener, GlobalROIListener {
    protected final Set<JMenuItem> items = new HashSet<>();

    public AbstractApplicationMenu(final String text) {
        super(text);
    }

    protected final void setIcon(final JMenuItem item, final IconCode iconCode) {
        if (item == null || iconCode == null)
            return;

        item.setIcon(new IcyIconFont(iconCode, State.FOREGROUND));
        item.setDisabledIcon(new IcyIconFont(iconCode, State.DISABLED));
        item.setSelectedIcon(new IcyIconFont(iconCode, State.SELECTED));

        items.add(item);
    }

    protected final void removeIcon(final JMenuItem item) {
        if (item == null)
            return;

        item.setIcon(null);
        item.setDisabledIcon(null);
        item.setSelectedIcon(null);

        items.remove(item);
    }

    private void reloadIcon(final JMenuItem item) {
        if (item == null)
            return;

        final Icon icon = item.getIcon();
        final Icon iconDisabled = item.getDisabledIcon();
        final Icon iconSelected = item.getSelectedIcon();
        if (!(icon instanceof IcyIconFont) || !(iconDisabled instanceof IcyIconFont) || !(iconSelected instanceof IcyIconFont))
            return;

        ((IcyIconFont) icon).updateIcon();
        ((IcyIconFont) iconDisabled).updateIcon();
        ((IcyIconFont) iconSelected).updateIcon();
    }

    private void reloadIcons() {
        for (JMenuItem item : items)
            reloadIcon(item);
    }

    protected final void addSkinChangeListener() {
        LookAndFeelUtil.addListener(this);
    }

    protected final void removeSkinChangeListener() {
        LookAndFeelUtil.removeListener(this);
    }

    protected final void addGlobalSequenceListener() {
        Icy.getMainInterface().addGlobalSequenceListener(this);
    }

    protected final void removeGlobalSequenceListener() {
        Icy.getMainInterface().removeGlobalSequenceListener(this);
    }

    protected final void addPluginLoaderListener() {
        PluginLoader.addListener(this);
    }

    protected final void removePluginLoaderListener() {
        PluginLoader.removeListener(this);
    }

    protected final void addActiveSequenceListener() {
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    protected final void removeActiceSequenceListener() {
        Icy.getMainInterface().removeActiveSequenceListener(this);
    }

    protected final void addGlobalROIListener() {
        Icy.getMainInterface().addGlobalROIListener(this);
    }

    protected final void removeGlobalROIListener() {
        Icy.getMainInterface().removeGlobalROIListener(this);
    }

    @Override
    public final void skinChanged() {
        reloadIcons();
    }

    @Override
    public void sequenceOpened(final Sequence sequence) {}

    @Override
    public void sequenceClosed(final Sequence sequence) {}

    @Override
    public void pluginLoaderChanged(final PluginLoaderEvent e) {}

    @Override
    public void sequenceActivated(final Sequence sequence) {}

    @Override
    public void sequenceDeactivated(final Sequence sequence) {}

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {}

    @Override
    public void roiAdded(final ROI roi) {}

    @Override
    public void roiRemoved(final ROI roi) {}
}
