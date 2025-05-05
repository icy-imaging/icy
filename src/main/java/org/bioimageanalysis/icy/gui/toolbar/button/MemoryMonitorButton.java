/*
 * Copyright (c) 2010-2024. Institut Pasteur.
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

package org.bioimageanalysis.icy.gui.toolbar.button;

import org.bioimageanalysis.icy.gui.action.PreferencesActions;
import org.bioimageanalysis.icy.gui.component.button.IcyButton;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import vtk.vtkObjectBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Thomas Musset
 */
public final class MemoryMonitorButton extends IcyButton implements MouseListener {
    private final IcySVGIcon OK = new IcySVGIcon(SVGIcon.CHECK_CIRCLE, Color.GREEN.darker());
    private final IcySVGIcon WARN = new IcySVGIcon(SVGIcon.WARNING, Color.YELLOW.darker());
    private final IcySVGIcon ERROR = new IcySVGIcon(SVGIcon.ERROR, Color.RED.darker());

    private final JPopupMenu popup;

    public MemoryMonitorButton() {
        super("CPU: 0% | RAM: 0%", SVGIcon.CHECK_CIRCLE);
        setFlat(true);
        setHorizontalAlignment(JButton.LEFT);
        setIcon(OK);
        setToolTipText("<html>Left click to free memory<br>Right click to open menu</html>");

        addMouseListener(this);

        popup = new JPopupMenu();
        final IcyMenuItem settings = new IcyMenuItem("Open Settings...", SVGIcon.SETTINGS);
        settings.addActionListener(PreferencesActions.generalPreferencesAction);
        final IcyMenuItem GB = new IcyMenuItem("Free Java Memory", SVGIcon.DELETE_SWEEP);
        GB.addActionListener(e -> forceGC());
        final IcyMenuItem VTKGB = new IcyMenuItem("Free VTK Memory", SVGIcon.DELETE_SWEEP);
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

        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    private void updateStats() {
        final double usedMemory = SystemUtil.getJavaUsedMemory();
        final int percentMemoryUsed = (int) Math.ceil((usedMemory / SystemUtil.getJavaMaxMemory()) * 100);
        final int cpuLoad = SystemUtil.getCpuLoad();

        final String cpu = (cpuLoad == 100) ? String.valueOf(cpuLoad) : (cpuLoad >= 10) ? " " + cpuLoad : "  " + cpuLoad;
        final String ram = (percentMemoryUsed == 100) ? String.valueOf(percentMemoryUsed) : (percentMemoryUsed >= 10) ? " " + percentMemoryUsed : "  " + percentMemoryUsed;

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

            IcyLogger.info(MemoryMonitorButton.class, String.format(
                    "Max | Used memory: %s | %s (released by GC: %s)",
                    UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()),
                    UnitUtil.getBytesString((usedMemory > 0) ? usedMemory : 0),
                    UnitUtil.getBytesString((released > 0) ? released : 0)
            ));
        });
    }

    /**
     * Force VTK garbage collection.
     */
    private void forceVTKGC() {
        // TODO: 19/06/2023 Check if still needs to be done in EDT or it crashes on OSX
        ThreadUtil.bgRun(() -> {
            vtkObjectBase.JAVA_OBJECT_MANAGER.gc(true);
            IcyLogger.info(MemoryMonitorButton.class, "VTK GC forced");
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
