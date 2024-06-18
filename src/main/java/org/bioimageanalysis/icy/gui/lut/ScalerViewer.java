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
package org.bioimageanalysis.icy.gui.lut;

import org.bioimageanalysis.icy.common.collection.array.Array1DUtil;
import org.bioimageanalysis.icy.common.color.ColorUtil;
import org.bioimageanalysis.icy.common.math.Histogram;
import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.common.math.Scaler;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.gui.EventUtil;
import org.bioimageanalysis.icy.gui.GraphicsUtil;
import org.bioimageanalysis.icy.gui.component.panel.HistogramPanel;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.gui.viewer.ViewerEvent;
import org.bioimageanalysis.icy.gui.viewer.ViewerEvent.ViewerEventType;
import org.bioimageanalysis.icy.gui.viewer.ViewerListener;
import org.bioimageanalysis.icy.model.lut.LUT.LUTChannel;
import org.bioimageanalysis.icy.model.lut.LUT.LUTChannelEvent;
import org.bioimageanalysis.icy.model.lut.LUT.LUTChannelEvent.LUTChannelEventType;
import org.bioimageanalysis.icy.model.lut.LUT.LUTChannelListener;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent.SequenceEventSourceType;
import org.bioimageanalysis.icy.model.sequence.SequenceListener;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.EventListener;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ScalerViewer extends JPanel implements SequenceListener, LUTChannelListener, ViewerListener {
    protected enum actionType {
        NULL, MODIFY_LOWBOUND, MODIFY_HIGHBOUND, MODIFY_MIDDLE
    }

    public interface ScalerPositionListener extends EventListener {
        void positionChanged(double index, int value, double normalizedValue);
    }

    public class ScalerHistogramPanel extends HistogramPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        /**
         * internals
         */
        private actionType action;
        private final Point2D positionInfo;
        private boolean mouseOnLeft;

        public ScalerHistogramPanel(final Scaler s) {
            super(s.getAbsLeftIn(), s.getAbsRightIn(), s.isIntegerData());

            action = actionType.NULL;
            positionInfo = new Point2D.Double();
            mouseOnLeft = false;

            // we want to display our own background
            // setOpaque(false);
            // dimension (don't change it or you will regret !)
            setMinimumSize(new Dimension(100, 100));
            setPreferredSize(new Dimension(240, 100));

            // add listeners
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        /**
         * update mouse cursor
         */
        private void updateCursor(final Point pos) {
            final int cursor;

            if (action != actionType.NULL)
                cursor = Cursor.W_RESIZE_CURSOR;
            else if (isOverX(pos, getLowBoundPos()) || isOverX(pos, getHighBoundPos()) || isOverX(pos, getMiddlePos()))
                cursor = Cursor.HAND_CURSOR;
            else
                cursor = Cursor.DEFAULT_CURSOR;

            // only if different
            if (getCursor().getType() != cursor)
                setCursor(Cursor.getPredefinedCursor(cursor));
        }

        private void setPositionInfo(final double index, final int value, final double normalizedValue) {
            if ((positionInfo.getX() != index) || (positionInfo.getY() != value)) {
                positionInfo.setLocation(index, normalizedValue);
                scalerPositionChanged(index, value, normalizedValue);
                repaint();
            }
        }

        /**
         * Check if Point p is over area (u, *)
         *
         * @param p
         *        point
         * @param u
         *        area position
         * @return boolean
         */
        private boolean isOverX(final Point p, final int u) {
            return isOver(p.x, p.y, u, -1, ISOVER_DEFAULT_MARGIN);
        }

        /**
         * Check if (x, y) is over area (u, v)
         *
         * @param x
         * @param y
         *        pointer
         * @param u
         * @param v
         *        area position
         * @param margin
         *        allowed margin
         * @return boolean
         */
        private boolean isOver(final int x, final int y, final int u, final int v, final int margin) {
            final boolean x_ok;
            final boolean y_ok;

            x_ok = (u == -1) || ((x >= (u - margin)) && (x <= (u + margin)));
            y_ok = (v == -1) || ((y >= (v - margin)) && (y <= (v + margin)));

            return x_ok && y_ok;
        }

        public int getLowBoundPos() {
            return dataToPixel(getLowBound());
        }

        public int getHighBoundPos() {
            return dataToPixel(getHighBound());
        }

        public int getMiddlePos() {
            return (getHighBoundPos() + getLowBoundPos()) / 2;
        }

        private void setLowBoundPos(final int pos) {
            setLowBound(pixelToData(pos));
        }

        private void setHighBoundPos(final int pos) {
            setHighBound(pixelToData(pos));
        }

        @Override
        protected void paintComponent(final Graphics g) {
            updateHisto();

            super.paintComponent(g);

            final Graphics2D g2 = (Graphics2D) g.create();
            try {
                // display mouse position infos
                if (positionInfo.getX() != -1) {
                    final int x = dataToPixel(positionInfo.getX());
                    final int hRange = getClientHeight() - 1;
                    final int bottom = hRange + getClientY();
                    final int y = bottom - (int) (positionInfo.getY() * hRange);

                    g2.setColor(ColorUtil.xor(getForeground()));
                    g2.drawLine(x, bottom, x, y);
                }

                paintBounds(g2);

                if (!StringUtil.isEmpty(message)) {
                    // string display
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

                    final Rectangle hintBounds = GraphicsUtil.getHintBounds(g2, message, 10, 4);

                    if (mouseOnLeft)
                        GraphicsUtil.drawHint(g2, message, getWidth() - (10 + hintBounds.width), 4, getForeground(),
                                getBackground());
                    else
                        GraphicsUtil.drawHint(g2, message, 10, 4, getForeground(), getBackground());
                }
            }
            finally {
                g2.dispose();
            }
        }

        /**
         * draw bounds
         */
        private void paintBounds(final Graphics2D g) {
            final int h = getClientHeight() - 1;
            final int y = getClientY();
            final int lowBound = getLowBoundPos();
            final int highBound = getHighBoundPos();
            final int middle = getMiddlePos();

            g.setColor(ColorUtil.mix(Color.blue, Color.white, false));
            g.drawRect(lowBound - 2, y, 2, h);
            g.setColor(Color.blue);
            g.fillRect(lowBound - 1, y + 1, 1, h - 1);
            g.setColor(ColorUtil.mix(Color.red, Color.white, false));
            g.drawRect(highBound - 1, y, 2, h);
            g.setColor(Color.red);
            g.fillRect(highBound, y + 1, 1, h - 1);
            g.setColor(ColorUtil.mix(Color.green, Color.white, false));
            g.drawRect(middle - 1, y + 10, 1, h - 10);
            g.setColor(Color.green);
            g.fillRect(middle, y + 11, 0, h - 11);
        }

        private void updateMessage(final MouseEvent e) {
            final Point pos = e.getPoint();
            final boolean shift = EventUtil.isShiftDown(e);
            final boolean left = EventUtil.isLeftMouseButton(e);
            String text;

            if (getBinNumber() > 0) {
                final int bin = pixelToBin(pos.x);
                double index = pixelToData(pos.x);
                final int value = getBinSize(bin);

                // use integer index with integer data type
                if (isIntegerType())
                    index = Math.floor(index);

                final String valueText = "value : " + MathUtil.roundSignificant(index, 5, true);
                final String pixelText = "pixel number : " + value;

                text = valueText + "\n" + pixelText;

                setPositionInfo(index, value, getAdjustedBinSize(bin));
            }
            else
                text = "";

            // info message when pressing left button or when dragging
            if (action != actionType.NULL) {
                if (!StringUtil.isEmpty(text))
                    text += "\n";

                if (shift)
                    text += "GLOBAL MOVE";
                else
                    text += "Maintain 'Shift' for global move";
            }

            setMessage(text);
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.isConsumed())
                return;

            if (e.getClickCount() == 2) {
                showRangeSettingDialog();
                e.consume();
            }
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            updateCursor(e.getPoint());
            updateMessage(e);
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            if (getCursor().getType() != Cursor.getDefaultCursor().getType())
                setCursor(Cursor.getDefaultCursor());

            // hide message
            setMessage("");
            setPositionInfo(-1, -1, -1);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            if (e.isConsumed())
                return;

            final Point pos = e.getPoint();

            if (EventUtil.isLeftMouseButton(e)) {
                if (isOverX(pos, getLowBoundPos()))
                    action = actionType.MODIFY_LOWBOUND;
                else if (isOverX(pos, getHighBoundPos()))
                    action = actionType.MODIFY_HIGHBOUND;
                else if (isOverX(pos, getMiddlePos()))
                    action = actionType.MODIFY_MIDDLE;

                updateCursor(e.getPoint());
                e.consume();
            }
            else if (EventUtil.isRightMouseButton(e)) {
                showSettingPopup(pos);
                e.consume();
            }

            updateMessage(e);
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            if (EventUtil.isLeftMouseButton(e)) {
                action = actionType.NULL;
                updateCursor(e.getPoint());
            }

            updateMessage(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            if (e.isConsumed())
                return;

            final Point pos = e.getPoint();
            final boolean shift = EventUtil.isShiftDown(e);

            mouseOnLeft = pos.x < (getWidth() / 2);

            switch (action) {
                case MODIFY_LOWBOUND:
                    setLowBoundPos(pos.x);
                    // also modify others bounds
                    if (shift) {
                        final double newLowBound = getLowBound();
                        for (final LUTChannel lc : lutChannel.getLut().getLutChannels())
                            lc.setMin(newLowBound);
                    }
                    e.consume();
                    break;

                case MODIFY_HIGHBOUND:
                    setHighBoundPos(pos.x);
                    // also modify others bounds
                    if (shift) {
                        final double newHighBound = getHighBound();
                        for (final LUTChannel lc : lutChannel.getLut().getLutChannels())
                            lc.setMax(newHighBound);
                    }
                    e.consume();
                    break;

                case MODIFY_MIDDLE:
                    final double width = (getHighBound() - getLowBound()) / 2d;
                    final double value = pixelToData(pos.x);
                    final double min = value - width;
                    final double max = value + width;

                    // global change
                    if (shift) {
                        for (final LUTChannel lc : lutChannel.getLut().getLutChannels()) {
                            if ((min >= lc.getMinBound()) && (max <= lc.getMaxBound())) {
                                lc.setMin(min);
                                lc.setMax(max);
                            }
                        }
                    }
                    else {
                        if ((min >= lutChannel.getMinBound()) && (max <= lutChannel.getMaxBound())) {
                            setLowBound(min);
                            setHighBound(max);
                        }
                    }
                    e.consume();
                    break;
            }

            updateMessage(e);
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            final Point pos = e.getPoint();

            mouseOnLeft = pos.x < (getWidth() / 2);

            updateCursor(e.getPoint());
            updateMessage(e);
        }

        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {

        }
    }

    private static final int ISOVER_DEFAULT_MARGIN = 3;

    /**
     * associated viewer &amp; lutChannel
     */
    Viewer viewer;
    LUTChannel lutChannel;
    /**
     * histogram
     */
    private final ScalerHistogramPanel histogram;
    private boolean histoNeedRefresh;

    /**
     * listeners
     */
    private final EventListenerList scalerMapPositionListeners;

    /**
     * internals
     */
    private final Runnable histoUpdater;
    String message;
    private int retry;

    /**
     *
     */
    public ScalerViewer(final Viewer viewer, final LUTChannel lutChannel) {
        super();

        this.viewer = viewer;
        this.lutChannel = lutChannel;

        message = "";
        retry = 0;
        scalerMapPositionListeners = new EventListenerList();
        histoUpdater = () -> {
            try {
                // refresh histogram
                refreshHistoDataInternal();
            }
            catch (final Exception e) {
                // just ignore error, it's permitted here
            }
        };

        histogram = new ScalerHistogramPanel(lutChannel.getScaler());
        // listen for need refresh event
        histogram.addListener(source -> internalRequestHistoDataRefresh());
        histoNeedRefresh = false;

        setLayout(new BorderLayout());
        add(histogram, BorderLayout.CENTER);
        validate();

        // force first refresh
        internalRequestHistoDataRefresh();

        // add listeners
        final Sequence sequence = viewer.getSequence();

        if (sequence != null)
            sequence.addListener(this);
        viewer.addListener(this);
        lutChannel.addListener(this);
    }

    public void requestHistoDataRefresh() {
        internalRequestHistoDataRefresh();
    }

    private boolean isHistoVisible() {
        if (!isValid())
            return false;

        return getVisibleRect().intersects(histogram.getBounds());
    }

    void internalRequestHistoDataRefresh() {
        if (isHistoVisible())
            refreshHistoData();
        else
            histoNeedRefresh = true;
    }

    void updateHisto() {
        if (histoNeedRefresh) {
            refreshHistoData();
            histoNeedRefresh = false;
        }
    }

    private void refreshHistoData() {
        // send refresh operation
        ThreadUtil.bgRunSingle(histoUpdater);
    }

    // this method is called by processor, we don't mind about exception here
    void refreshHistoDataInternal() {
        final Histogram histo = histogram.getHistogram();
        final Sequence seq = viewer.getSequence();

        histogram.reset();
        try {
            if (seq != null) {
                final int maxZ;
                final int maxT;
                int t = viewer.getPositionT();
                int z = viewer.getPositionZ();

                if (t != -1)
                    maxT = t;
                else {
                    t = 0;
                    maxT = seq.getSizeT() - 1;
                }

                if (z != -1)
                    maxZ = z;
                else {
                    z = 0;
                    maxZ = seq.getSizeZ() - 1;
                }

                final int c = lutChannel.getChannel();

                for (; t <= maxT; t++) {
                    for (; z <= maxZ; z++) {
                        final Object data = seq.getDataXY(t, z, c);

                        // need to test for empty sequence
                        if (data != null) {
                            final DataType dataType = seq.getDataType();
                            final int len = Array.getLength(data);

                            for (int i = 0; i < len; i++) {
                                if ((i & 0xFFF) == 0) {
                                    // need to be recalculated so don't waste time here...
                                    if (ThreadUtil.hasWaitingBgSingleTask(histoUpdater))
                                        return;
                                }

                                histo.addValue(Array1DUtil.getValue(data, i, dataType));
                            }
                        }
                    }
                }
            }

            retry = 0;
        }
        catch (final Exception e) {
            // just redo it later
            if (retry++ < 3)
                refreshHistoData();
        }
        finally {
            // notify that histogram computation is done
            histogram.done();

            // histogram changed in the meantime --> recompute
            if (histo != histogram.getHistogram())
                refreshHistoData();
        }
    }

    /**
     * @return the histogram
     */
    public HistogramPanel getHistogram() {
        return histogram;
    }

    /**
     * @return the histoData
     */
    public double[] getHistoData() {
        return histogram.getHistogramData();
    }

    /**
     * @return the scaler
     */
    public Scaler getScaler() {
        return lutChannel.getScaler();
    }

    public double getLowBound() {
        return lutChannel.getMin();
    }

    public double getHighBound() {
        return lutChannel.getMax();
    }

    void setLowBound(final double value) {
        lutChannel.setMin(value);
    }

    void setHighBound(final double value) {
        lutChannel.setMax(value);
    }

    /**
     * tasks to do on scaler changes
     */
    public void onScalerChanged() {
        final Scaler s = getScaler();

        histogram.setMinMaxIntValues(s.getAbsLeftIn(), s.getAbsRightIn(), s.isIntegerData());

        // repaint component now as bounds may have changed
        repaint();
    }

    /**
     * process on sequence change
     */
    void onSequenceDataChanged() {
        final LUTViewer lutViewer = viewer.getLutViewer();

        // update histogram
        if ((lutViewer != null) && lutViewer.getAutoRefreshHistogram())
            requestHistoDataRefresh();
    }

    /**
     * process on position changed
     */
    private void onPositionChanged() {
        final LUTViewer lutViewer = viewer.getLutViewer();

        // update histogram
        if ((lutViewer != null) && lutViewer.getAutoRefreshHistogram())
            requestHistoDataRefresh();
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param value
     *        the message to set
     */
    public void setMessage(final String value) {
        if (!StringUtil.equals(message, value)) {
            message = value;
            repaint();
        }
    }

    /**
     * Should be called when histogram scaling type changed
     */
    public void scaleTypeChanged(final boolean log) {
        histogram.setLogScaling(log);
    }

    /**
     * show popup menu
     */
    protected void showSettingPopup(final Point pos) {
        // rebuild menu
        final JPopupMenu menu = new JPopupMenu("Actions");

        final JMenuItem refreshItem = new JMenuItem("Refresh now");
        refreshItem.addActionListener(e -> requestHistoDataRefresh());
        final JMenuItem setBoundsItem = new JMenuItem("Set range");
        setBoundsItem.addActionListener(e -> showRangeSettingDialog());
        final JMenuItem exportItem = new JMenuItem("Export to excel");
        exportItem.addActionListener(e -> {
            try {
                getHistogram().getHistogram().doXLSExport();
            }
            catch (final Exception e1) {
                MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
            }
        });

        menu.add(refreshItem);
        menu.add(setBoundsItem);
        menu.add(exportItem);

        menu.pack();
        menu.validate();

        // display menu
        menu.show(this, pos.x, pos.y);
    }

    void showRangeSettingDialog() {
        final ScalerBoundsSettingDialog boundsSettingDialog = new ScalerBoundsSettingDialog(lutChannel);

        boundsSettingDialog.pack();
        boundsSettingDialog.setLocationRelativeTo(this);
        boundsSettingDialog.setVisible(true);
    }

    /**
     * Add a listener
     */
    public void addScalerPositionListener(final ScalerPositionListener listener) {
        scalerMapPositionListeners.add(ScalerPositionListener.class, listener);
    }

    /**
     * Remove a listener
     */
    public void removeScalerPositionListener(final ScalerPositionListener listener) {
        scalerMapPositionListeners.remove(ScalerPositionListener.class, listener);
    }

    /**
     * mouse position on scaler info changed
     */
    public void scalerPositionChanged(final double index, final int value, final double normalizedValue) {
        for (final ScalerPositionListener listener : scalerMapPositionListeners.getListeners(ScalerPositionListener.class))
            listener.positionChanged(index, value, normalizedValue);
    }

    @Override
    public void lutChannelChanged(final LUTChannelEvent event) {
        if (event.getType() == LUTChannelEventType.SCALER_CHANGED)
            onScalerChanged();
    }

    @Override
    public void viewerChanged(final ViewerEvent event) {
        if (event.getType() == ViewerEventType.POSITION_CHANGED)
            onPositionChanged();
    }

    @Override
    public void viewerClosed(final Viewer viewer) {
        viewer.removeListener(this);
    }

    @Override
    public void sequenceChanged(final SequenceEvent sequenceEvent) {
        if (sequenceEvent.getSourceType() == SequenceEventSourceType.SEQUENCE_DATA)
            onSequenceDataChanged();
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
        sequence.removeListener(this);
    }
}
