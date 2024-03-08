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

package icy.gui.system;

import icy.image.cache.ImageCache;
import icy.math.UnitUtil;
import icy.network.NetworkUtil;
import icy.resource.icon.IcySVGImageIcon;
import icy.resource.icon.SVGIcon;
import icy.system.SystemUtil;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;
import icy.util.ColorUtil;
import icy.util.GraphicsUtil;
import vtk.vtkObjectBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Memory monitor.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class MemoryMonitorPanel extends JPanel implements MouseListener {
    private static final int NBVAL = 94;

    /**
     * 0 est la valeur la plus ancienne.
     */
    private final double[][] valeur;
    private final String[] infos;

    //private final Color cacheTextColor = ColorUtil.mix(Color.yellow, Color.white);
    private final Color cacheTextColor = ColorUtil.mix(Color.yellow, Color.darkGray);
    //private final Color cpuColor = ColorUtil.mix(Color.blue, Color.white);
    private final Color cpuColor = ColorUtil.mix(Color.blue, Color.darkGray);
    //private final Color cpuTextColor = ColorUtil.mix(cpuColor, Color.white);
    //private final Color cpuTextColor = ColorUtil.mix(cpuColor, Color.white);
    private final Color cpuTextColor = ColorUtil.mix(cpuColor, Color.gray);
    private final Color memColor = Color.green;
    //private final Color memTextColor = ColorUtil.mix(memColor, Color.white);
    private final Color memTextColor = ColorUtil.mix(memColor, Color.darkGray);
    //private final Color connectionColor = ColorUtil.mix(Color.red, Color.white);
    private final Color connectionColor = ColorUtil.mix(Color.red, Color.darkGray);
    private final BasicStroke cpuStroke = new BasicStroke(2);
    private final BasicStroke memStroke = new BasicStroke(3);
    private final Font textFont = new Font("Arial", Font.BOLD, 9);
    //private BufferedImage background = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    //private final Image networkImage = ImageUtil.getColorImageFromAlphaImage(ResourceUtil.ICON_NETWORK, Color.gray); //SIGNAL_WIFI_OFF
    private final Image networkImage = new IcySVGImageIcon(SVGIcon.BROKEN_IMAGE, Color.GRAY, 20).getImage();
    //private final Image deleteImage = ImageUtil.getColorImageFromAlphaImage(ResourceUtil.ICON_DELETE, Color.red);
    private final Image deleteImage = new IcySVGImageIcon(SVGIcon.BROKEN_IMAGE, Color.RED.brighter(), 16).getImage();

    boolean displayHelpMessage = false;
    int lastCacheUpdate;

    public MemoryMonitorPanel() {
        super();

        final Timer updateTimer = new Timer("Memory / CPU monitor");

        // init tables
        valeur = new double[NBVAL][2];
        for (int i = 0; i < NBVAL; i++) {
            // mem
            valeur[i][0] = 0;
            // cpu load
            valeur[i][1] = 0;
        }
        infos = new String[3];
        for (int i = 0; i < 2; i++)
            infos[i] = "";

        lastCacheUpdate = 10;

        setMinimumSize(new Dimension(120, 50));
        setPreferredSize(new Dimension(140, 55));

        addMouseListener(this);

        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStats();
            }
        }, 100, 100);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();

        final int w = getWidth();
        final int h = getHeight();

        // refresh BG
        /*if ((background.getWidth() != w) || (background.getHeight() != h)) {
            background = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D background_g2 = background.createGraphics();
            GraphicsUtil.paintIcyBackGround(w, h, background_g2);
        }*/

        // draw cached BG
        //g2.drawImage(background, 0, 0, null);

        // enabled AA
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // display graph
        if (valeur != null) {
            float x;
            double max;
            double ymul;
            final float step = w / 100f;

            // draw used memory
            g2.setStroke(memStroke);
            g2.setColor(memColor);

            max = SystemUtil.getJavaMaxMemory();
            ymul = (h - 8) / max;
            x = 6;
            for (int i = 0; i < NBVAL - 1; i++) {
                final double v1 = Math.min(valeur[i][0], max);
                final double v2 = Math.min(valeur[i + 1][0], max);
                final int y1 = h - (int) (v1 * ymul);
                final int y2 = h - (int) (v2 * ymul);
                g2.drawLine((int) x, y1 - 4, (int) (x + step), y2 - 4);
                x += step;
            }

            // draw CPU load
            g2.setStroke(cpuStroke);
            g2.setColor(cpuColor);

            max = 100d;
            ymul = (h - 8) / max;
            x = 6;
            for (int i = 0; i < NBVAL - 1; i++) {
                final double v1 = Math.min(valeur[i][1], max);
                final double v2 = Math.min(valeur[i + 1][1], max);
                final int y1 = h - (int) (v1 * ymul);
                final int y2 = h - (int) (v2 * ymul);
                g2.drawLine((int) x, y1 - 4, (int) (x + step), y2 - 4);
                x += step;
            }
        }

        // display text
        g2.setFont(textFont);

        // display Used & Max Memory
        //g2.setColor(Color.black);
        //GraphicsUtil.drawHCenteredString(g2, infos[0], (w / 2) + 1, 6 + 1, false);
        g2.setColor(memTextColor);
        GraphicsUtil.drawHCenteredString(g2, infos[0], w / 2, 6, false);
        // display CPU Load
        //g2.setColor(Color.black);
        //GraphicsUtil.drawHCenteredString(g2, infos[1], (w / 2) + 1, 18 + 1, false);
        g2.setColor(cpuTextColor);
        GraphicsUtil.drawHCenteredString(g2, infos[1], w / 2, 18, false);
        // display cache Load
        //g2.setColor(Color.black);
        //GraphicsUtil.drawHCenteredString(g2, infos[2], (w / 2) + 1, 30 + 1, false);
        g2.setColor(cacheTextColor);
        GraphicsUtil.drawHCenteredString(g2, infos[2], w / 2, 30, false);

        String text;

        // display internet connection
        if (!NetworkUtil.hasInternetAccess()) {
            //g2.drawImage(networkImage, 10, 30, 16, 16, null);
            g2.drawImage(networkImage, 5, 5, 20, 20, null);
            //g2.drawImage(deleteImage, 13, 35, 10, 10, null);
            g2.drawImage(deleteImage, 20, 9, 16, 16, null);

            if (displayHelpMessage) {
                text = "Not connected to internet";

                //g2.setColor(Color.black);
                //GraphicsUtil.drawHCenteredString(g2, text, (w / 2) + 1, 30 + 1, false);
                g2.setColor(connectionColor);
                GraphicsUtil.drawHCenteredString(g2, text, w / 2, 30, false);
            }
        }

        if (displayHelpMessage) {
            text = "click to force a garbage collector event";
            //g2.setColor(Color.black);
            //GraphicsUtil.drawHCenteredString(g2, text, (w / 2) + 1, 44 + 1, false);
            g2.setColor(Color.white);
            GraphicsUtil.drawHCenteredString(g2, text, w / 2, 44, false);
        }

        g2.dispose();
    }

    void updateStats() {
        final double usedMemory = SystemUtil.getJavaUsedMemory();
        final int cpuLoad = SystemUtil.getCpuLoad();

        // save used memory
        newValue(0, usedMemory);
        // save CPU load
        newValue(1, cpuLoad);

        setInfo(0, "Memory: " + UnitUtil.getBytesString(usedMemory) + " / "
                + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()));
        setInfo(1, "CPU: " + cpuLoad + "%");
        if (ImageCache.isInit()) {
            // don't update cache stats (take sometime) at each frame
            if (--lastCacheUpdate == 0) {
                try {
                    setInfo(2, "Cache - Memory: " + UnitUtil.getBytesString(ImageCache.usedMemory()) + "  Disk: "
                            + UnitUtil.getBytesString(ImageCache.usedDisk()));
                }
                catch (final Throwable t) {
                    // can happen when we exit Icy as the cache engine may be shutdown
                    // we can ignore safely
                }

                lastCacheUpdate = 10;
            }
        }
        else
            setInfo(2, "Cache disabled");

        repaint();
    }

    /**
     * Scroll les valeurs et en ajoute ( un seeker serait plus joli...)
     */
    public void newValue(final int curve, final double val) {
        for (int i = 0; i < NBVAL - 1; i++)
            valeur[i][curve] = valeur[i + 1][curve];

        valeur[NBVAL - 1][curve] = val;
    }

    public void setInfo(final int infonb, final String info) {
        infos[infonb] = info;
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        ThreadUtil.bgRun(() -> {
            final double freeBefore = SystemUtil.getJavaFreeMemory();

            // force garbage collector
            System.gc();

            final double freeAfter = SystemUtil.getJavaFreeMemory();
            final double released = freeAfter - freeBefore;
            final double usedMemory = SystemUtil.getJavaUsedMemory();

            IcyLogger.info(MemoryMonitorPanel.class, String.format(
                    "Max / Used memory: %s / %s (released by GC: %s)",
                    UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()),
                    UnitUtil.getBytesString((usedMemory > 0) ? usedMemory : 0),
                    UnitUtil.getBytesString((released > 0) ? released : 0)
            ));
        });

        // double click --> force VTK garbage collection (need to be done in EDT or it crashes on OSX)
        if (event.getClickCount() > 1) {
            vtkObjectBase.JAVA_OBJECT_MANAGER.gc(true);
            IcyLogger.info(MemoryMonitorPanel.class, "VTK GC forced");
        }
    }

    @Override
    public void mouseEntered(final MouseEvent arg0) {
        displayHelpMessage = true;
    }

    @Override
    public void mouseExited(final MouseEvent arg0) {
        displayHelpMessage = false;
    }

    @Override
    public void mousePressed(final MouseEvent arg0) {

    }

    @Override
    public void mouseReleased(final MouseEvent arg0) {

    }
}
