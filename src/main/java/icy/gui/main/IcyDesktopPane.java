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

package icy.gui.main;

import icy.gui.frame.IcyInternalFrame;
import icy.gui.util.ComponentUtil;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.math.HungarianAlgorithm;
import icy.resource.icon.IcySVGImageIcon;
import icy.resource.icon.SVGIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * Icy {@link JDesktopPane} class.<br>
 * This is the main container of the application.<br>
 * It support overlays so we can use to display message, notification or logo in
 * background. First added overlays is painted first, so take care of that. Call
 * the IcyDesktopPane.repaint() method to update overlays.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class IcyDesktopPane extends JDesktopPane implements ContainerListener, MouseListener, MouseMotionListener, MouseWheelListener {
    public interface DesktopOverlay extends MouseListener, MouseMotionListener, MouseWheelListener {
        void paint(Graphics g, int width, int height);
    }

    public static class AbstractDesktopOverlay extends MouseAdapter implements DesktopOverlay {
        @Override
        public void paint(final Graphics g, final int width, final int height) {
            // nothing by default
        }
    }

    /**
     * Background overlay.
     */
    public class BackgroundDesktopOverlay extends AbstractDesktopOverlay implements ImageObserver {
        private final Image backGround;

        public BackgroundDesktopOverlay() {
            super();

            backGround = new IcySVGImageIcon(SVGIcon.ICY_TRANSPARENT, Color.GRAY, 512).getImage();
        }

        @Override
        public void paint(final Graphics g, final int width, final int height) {
            final IcyDesktopPane desktop = Icy.getMainInterface().getDesktopPane();
            if (desktop == null)
                return;

            final Color bgColor = desktop.getBackground();

            /*if (desktop != null)
                bgColor = desktop.getBackground();
            else
                bgColor = Color.lightGray;*/

            final int bgImgWidth = backGround.getWidth(this);
            final int bgImgHeight = backGround.getHeight(this);

            // compute image scaling
            //final double scale = Math.max((double) width / (double) bgImgWidth, (double) height / (double) bgImgHeight) * 1.5d;
            final Graphics2D g2 = (Graphics2D) g.create();

            // fill background color
            g2.setBackground(bgColor);
            g2.clearRect(0, 0, width, height);

            //int imgWidth = Math.min(desktop.getWidth(), desktop.getHeight());
            //int imgHeight = imgWidth;

            // paint image over background in transparency
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .2f));
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            //g2.drawImage(backGround, 0, 0, (int) (scale * bgImgWidth), (int) (scale * bgImgHeight), bgColor, this);
            //g2.drawImage(backGround, (desktop.getWidth()/2)-(imgWidth/2), (desktop.getHeight()/2)-(imgWidth/2), imgWidth, imgHeight, bgColor, this);
            g2.drawImage(backGround, (desktop.getWidth() / 2) - (bgImgWidth / 2), (desktop.getHeight() / 2) - (bgImgHeight / 2), bgImgWidth, bgImgHeight, null, this);

            g2.dispose();
        }

        @Override
        public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int width, final int height) {
            if ((infoflags & ImageObserver.ALLBITS) != 0) {
                repaint();
                return false;
            }

            return true;
        }
    }

    private final ComponentAdapter componentAdapter;
    // private final InternalFrameAdapter internalFrameAdapter;

    private final ArrayList<DesktopOverlay> overlays;

    public IcyDesktopPane() {
        super();

        overlays = new ArrayList<>();

        // setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        componentAdapter = new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                checkPosition((JInternalFrame) e.getSource());
            }

            @Override
            public void componentMoved(final ComponentEvent e) {
                checkPosition((JInternalFrame) e.getSource());
            }
        };

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addContainerListener(this);

        // add the background overlay
        overlays.add(new BackgroundDesktopOverlay());
    }

    // int i = 0;

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final int w = getWidth();
        final int h = getHeight();

        // paint overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.paint(g, w, h);
    }

    private void registerFrame(final @NotNull JInternalFrame frame) {
        frame.addComponentListener(componentAdapter);
    }

    void unregisterFrame(final @NotNull JInternalFrame frame) {
        frame.removeComponentListener(componentAdapter);
    }

    void checkPosition(final @NotNull JInternalFrame frame) {
        final Rectangle rect = frame.getBounds();

        if (fixPosition(rect))
            frame.setBounds(rect);
    }

    boolean fixPosition(final @NotNull Rectangle rect) {
        final int limit = getY();
        if (rect.y < limit) {
            rect.y = limit;
            return true;
        }

        return false;
    }

    /**
     * Returns the list of internal viewers.
     *
     * @param wantNotVisible Also return not visible viewers
     * @param wantIconized   Also return iconized viewers
     */
    public static Viewer[] getInternalViewers(final boolean wantNotVisible, final boolean wantIconized) {
        final List<Viewer> result = new ArrayList<>();

        for (final Viewer viewer : Icy.getMainInterface().getViewers()) {
            if (viewer.isInternalized()) {
                final IcyInternalFrame internalFrame = viewer.getIcyInternalFrame();

                if ((wantNotVisible || internalFrame.isVisible()) && (wantIconized || !internalFrame.isIcon()))
                    result.add(viewer);
            }
        }

        return result.toArray(new Viewer[0]);
    }

    /**
     * Organize all internal viewers in cascade
     */
    public void organizeCascade() {
        // get internal viewers
        final Viewer[] viewers = getInternalViewers(false, false);

        // available space (always keep 32 available pixels at south)
        final int w = getWidth();
        final int h = getHeight() - 32;

        final int fw = (int) (w * 0.6f);
        final int fh = (int) (h * 0.6f);

        final int xMax = w; // w - 0;
        final int yMax = h; // h - 0;

        int x = 32; // 0 + 32;
        int y = 32; // 0 + 32;

        for (final Viewer v : viewers) {
            final IcyInternalFrame internalFrame = v.getIcyInternalFrame();

            if (internalFrame.isMaximized())
                internalFrame.setMaximized(false);
            internalFrame.setBounds(x, y, fw, fh);
            internalFrame.toFront();

            x += 30;
            y += 20;
            if ((x + fw) > xMax)
                x = 32;
            if ((y + fh) > yMax)
                y = 32;
        }
    }

    /**
     * Organize all internal viewers in tile.
     *
     * @param type tile type<br>
     *             MainFrame.TILE_HORIZONTAL, MainFrame.TILE_VERTICAL or
     *             MainFrame.TILE_GRID
     */
    public void organizeTile(final int type) {
        // get internal viewers
        final Viewer[] viewers = getInternalViewers(false, false);

        final int numFrames = viewers.length;

        // nothing to do
        if (numFrames == 0)
            return;

        // available space (always keep 32 available pixels at south)
        final int w = getWidth();
        final int h = getHeight() - 32;

        int numCol;
        int numLine;

        switch (type) {
            case MainFrame.TILE_HORIZONTAL:
                numCol = 1;
                numLine = numFrames;
                break;

            case MainFrame.TILE_VERTICAL:
                numCol = numFrames;
                numLine = 1;
                break;

            default:
                numCol = (int) Math.sqrt(numFrames);
                if (numFrames != (numCol * numCol))
                    numCol++;
                numLine = numFrames / numCol;
                if (numFrames > (numCol * numLine))
                    numLine++;
                break;
        }

        final double[][] framesDistances = new double[numCol * numLine][numFrames];

        final int dx = w / numCol;
        final int dy = h / numLine;
        int k = 0;

        for (int i = 0; i < numLine; i++) {
            for (int j = 0; j < numCol; j++, k++) {
                final double[] distances = framesDistances[k];
                final double x = (j * dx) + (dx / 2d);
                final double y = (i * dy) + (dy / 2d);

                for (int f = 0; f < numFrames; f++) {
                    final Point2D.Double center = ComponentUtil.getCenter(viewers[f].getInternalFrame());
                    distances[f] = Point2D.distanceSq(center.x, center.y, x, y);
                }
            }
        }

        final int[] framePos = new HungarianAlgorithm(framesDistances).resolve();

        k = 0;
        for (int i = 0; i < numLine; i++) {
            for (int j = 0; j < numCol; j++, k++) {
                final int f = framePos[k];

                if (f < numFrames) {
                    final IcyInternalFrame internalFrame = viewers[f].getIcyInternalFrame();

                    if (internalFrame.isMaximized())
                        internalFrame.setMaximized(false);
                    internalFrame.setBounds(j * dx, i * dy, dx, dy);
                    internalFrame.toFront();
                }
            }
        }
    }

    /**
     * @deprecated Use {@link #organizeTile(int)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void organizeTile() {
        organizeTile(MainFrame.TILE_GRID);
    }

    /**
     * Add the specified overlay to the desktop.
     */
    public void addOverlay(final DesktopOverlay overlay) {
        if (!overlays.contains(overlay))
            overlays.add(overlay);
    }

    /**
     * remove the specified overlay from the desktop.
     */
    public boolean removeOverlay(final DesktopOverlay overlay) {
        return overlays.remove(overlay);
    }

    @Override
    public void componentAdded(final @NotNull ContainerEvent e) {
        final Component comp = e.getChild();

        if (comp instanceof JInternalFrame)
            registerFrame((JInternalFrame) comp);
    }

    @Override
    public void componentRemoved(final @NotNull ContainerEvent e) {
        final Component comp = e.getChild();

        if (comp instanceof JInternalFrame)
            unregisterFrame((JInternalFrame) comp);
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseWheelMoved(e);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseDragged(e);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseMoved(e);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseClicked(e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseReleased(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseEntered(e);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        // send to overlays
        for (final DesktopOverlay overlay : overlays)
            overlay.mouseExited(e);
    }
}
