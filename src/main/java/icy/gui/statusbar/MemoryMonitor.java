/*
 * Copyright (c) 2010-2023. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package icy.gui.statusbar;

import icy.gui.component.button.IcyButton;
import icy.gui.component.menu.IcyMenuItem;
import icy.gui.util.LookAndFeelUtil;
import icy.math.UnitUtil;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import vtk.vtkObjectBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Thomas MUSSET
 */
public final class MemoryMonitor extends IcyButton implements MouseListener {

    private final Icon OK = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.CHECK_CIRCLE, LookAndFeelUtil.getDefaultIconSizeAsFloat(), Color.GREEN.darker());
    private final Icon WARN = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.WARNING, LookAndFeelUtil.getDefaultIconSizeAsFloat(), Color.YELLOW.darker());
    private final Icon ERROR = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ERROR, LookAndFeelUtil.getDefaultIconSizeAsFloat(), Color.RED.darker());

    private final JPopupMenu popup;

    public MemoryMonitor() {
        super("CPU: 100% | RAM: 100%");
        setPreferredSize(new Dimension(185, 27));
        setHorizontalAlignment(JButton.LEFT);
        setIcon(OK);
        setToolTipText("<html>Left click to free memory<br>Right click to open menu</html>");

        addMouseListener(this);

        popup = new JPopupMenu();
        final IcyMenuItem settings = new IcyMenuItem("Open Settings...", GoogleMaterialDesignIcons.SETTINGS);
        final IcyMenuItem GB = new IcyMenuItem("Free Java Memory", GoogleMaterialDesignIcons.DELETE_SWEEP);
        GB.addActionListener(e -> forceGC());
        final IcyMenuItem VTKGB = new IcyMenuItem("Free VTK Memory", GoogleMaterialDesignIcons.DELETE_SWEEP);
        VTKGB.addActionListener(e -> forceVTKGC());
        popup.add(settings);
        popup.addSeparator();
        popup.add(GB);
        popup.add(VTKGB);

        final Timer timer = new Timer("CPU-RAM", true);
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateStats();
            }
        };

        timer.scheduleAtFixedRate(task, 100, 100);
    }

    private void updateStats() {
        final double usedMemory = SystemUtil.getJavaUsedMemory();
        final int percentMemoryUsed = (int) Math.ceil((usedMemory / SystemUtil.getJavaMaxMemory()) * 100);
        final int cpuLoad = SystemUtil.getCpuLoad();

        final String cpu = (cpuLoad == 100)? String.valueOf(cpuLoad) : (cpuLoad >= 10)? " " + cpuLoad : "  " + cpuLoad;
        final String ram = (percentMemoryUsed == 100)? String.valueOf(percentMemoryUsed) : (percentMemoryUsed >= 10)? " " + percentMemoryUsed : "  " + percentMemoryUsed;

        final String s = String.format("CPU: %s%% | RAM: %s%%", cpu, ram);

        if (cpuLoad >= 90 || percentMemoryUsed >= 90d)
            setIcon(ERROR);
        else if (cpuLoad >= 60 || percentMemoryUsed >= 60)
            setIcon(WARN);
        else
            setIcon(OK);

        setText(s);
    }

    /**
     * Force JVM garbage collector.
     */
    private void forceGC() {
        ThreadUtil.bgRun(() -> {
            final double freeBefore = SystemUtil.getJavaFreeMemory();

            // force garbage collector
            System.gc();

            final double freeAfter = SystemUtil.getJavaFreeMemory();
            final double released = freeAfter - freeBefore;
            final double usedMemory = SystemUtil.getJavaUsedMemory();

            System.out.println("Max / Used memory: " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory())
                    + " / " + UnitUtil.getBytesString((usedMemory > 0) ? usedMemory : 0) + " (released by GC: "
                    + UnitUtil.getBytesString((released > 0) ? released : 0) + ")");
        });
    }

    /**
     * Force VTK garbage collection.
     */
    private void forceVTKGC() {
        // TODO: 19/06/2023 Check if still needs to be done in EDT or it crashes on OSX
        ThreadUtil.bgRun(() -> {
            vtkObjectBase.JAVA_OBJECT_MANAGER.gc(true);
            System.out.println("VTK GC forced");
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param e {@inheritDoc}
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            forceGC();
        else if (e.getButton() == MouseEvent.BUTTON3)
            popup.show(this, 0, -popup.getPreferredSize().height);
    }

    /**
     * {@inheritDoc}
     *
     * @param e {@inheritDoc}
     */
    @Override
    public void mousePressed(final MouseEvent e) {

    }

    /**
     * {@inheritDoc}
     *
     * @param e {@inheritDoc}
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

    }

    /**
     * {@inheritDoc}
     *
     * @param e {@inheritDoc}
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

    }

    /**
     * {@inheritDoc}
     *
     * @param e {@inheritDoc}
     */
    @Override
    public void mouseExited(final MouseEvent e) {

    }
}
