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
package org.bioimageanalysis.icy.model.lut;

import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.event.UpdateEventHandler;
import org.bioimageanalysis.icy.common.listener.ChangeListener;
import org.bioimageanalysis.icy.common.math.Scaler;
import org.bioimageanalysis.icy.common.math.ScalerEvent;
import org.bioimageanalysis.icy.common.math.ScalerListener;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.io.xml.XMLPersistent;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.colormap.IcyColorMap;
import org.bioimageanalysis.icy.model.colormodel.IcyColorModel;
import org.bioimageanalysis.icy.model.colorspace.IcyColorSpace;
import org.bioimageanalysis.icy.model.colorspace.IcyColorSpaceEvent;
import org.bioimageanalysis.icy.model.colorspace.IcyColorSpaceListener;
import org.bioimageanalysis.icy.model.lut.LUT.LUTChannelEvent.LUTChannelEventType;
import org.bioimageanalysis.icy.model.lut.LUTEvent.LUTEventType;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class LUT implements IcyColorSpaceListener, ScalerListener, ChangeListener, XMLPersistent {
    private final static String ID_NUM_CHANNEL = "numChannel";
    private final static String ID_SCALER = "scaler";
    private final static String ID_COLORMAP = "colormap";

    public interface LUTChannelListener extends EventListener {
        void lutChannelChanged(LUTChannelEvent e);
    }

    public static class LUTChannelEvent {
        public enum LUTChannelEventType {
            SCALER_CHANGED, COLORMAP_CHANGED
        }

        private final LUTChannel lutChannel;
        private final LUTChannelEventType type;

        public LUTChannelEvent(final LUTChannel lutChannel, final LUTChannelEventType type) {
            super();

            this.lutChannel = lutChannel;
            this.type = type;
        }

        /**
         * @return the lutChannel
         */
        public LUTChannel getLutChannel() {
            return lutChannel;
        }

        /**
         * @return the type
         */
        public LUTChannelEventType getType() {
            return type;
        }
    }

    public class LUTChannel {
        /**
         * band index
         */
        private final int channel;

        /**
         * listeners
         */
        private final List<LUTChannelListener> channelListeners;

        public LUTChannel(final int channel) {
            this.channel = channel;

            channelListeners = new ArrayList<>();
        }

        public LUT getLut() {
            return LUT.this;
        }

        /**
         * Copy the colormap and scaler data from the specified source {@link LUTChannel}
         */
        public void copyFrom(final LUTChannel source) {
            setColorMap(source.getColorMap(), true);
            setScaler(source.getScaler());
        }

        public Scaler getScaler() {
            return getScalers()[channel];
        }

        /**
         * Set the specified scaler (do a copy).
         *
         * @param source
         *        source scaler to copy from
         */
        public void setScaler(final Scaler source) {
            final Scaler scaler = getScaler();

            scaler.beginUpdate();
            try {
                scaler.setAbsLeftRightIn(source.getAbsLeftIn(), source.getAbsRightIn());
                scaler.setLeftRightIn(source.getLeftIn(), source.getRightIn());
                scaler.setLeftRightOut(source.getLeftOut(), source.getRightOut());
            }
            finally {
                scaler.endUpdate();
            }
        }

        public IcyColorMap getColorMap() {
            return getColorSpace().getColorMap(channel);
        }

        /**
         * Set the specified colormap (do a copy).
         *
         * @param colorMap
         *        source colorspace to copy
         * @param setAlpha
         *        also set the alpha information
         */
        public void setColorMap(final IcyColorMap colorMap, final boolean setAlpha) {
            getColorSpace().setColorMap(channel, colorMap, setAlpha);
        }

        public double getMin() {
            return getScaler().getLeftIn();
        }

        public void setMin(final double value) {
            getScaler().setLeftIn(value);
        }

        public double getMax() {
            return getScaler().getRightIn();
        }

        public void setMax(final double value) {
            getScaler().setRightIn(value);
        }

        public void setMinMax(final double min, final double max) {
            getScaler().setLeftRightIn(min, max);
        }

        public double getMinBound() {
            return getScaler().getAbsLeftIn();
        }

        public double getMaxBound() {
            return getScaler().getAbsRightIn();
        }

        public void setMinBound(final double value) {
            getScaler().setAbsLeftIn(value);
        }

        public void setMaxBound(final double value) {
            getScaler().setAbsRightIn(value);
        }

        /**
         * Returns the <i>enabled</i> state of this channel LUT
         */
        public boolean isEnabled() {
            return getColorMap().isEnabled();
        }

        /**
         * Enable/disable specified channel LUT
         */
        public void setEnabled(final boolean value) {
            getColorMap().setEnabled(value);
        }

        /**
         * @return the component
         */
        public int getChannel() {
            return channel;
        }

        /**
         * Add a listener.
         */
        public void addListener(final LUTChannelListener listener) {
            channelListeners.add(listener);
        }

        /**
         * Remove a listener.
         */
        public void removeListener(final LUTChannelListener listener) {
            channelListeners.remove(listener);
        }

        /**
         * Fire change event.
         */
        public void fireEvent(final LUTChannelEvent e) {
            for (final LUTChannelListener listener : new ArrayList<>(channelListeners))
                listener.lutChannelChanged(e);
        }
    }

    private final List<LUTChannel> lutChannels = new ArrayList<>();

    final IcyColorSpace colorSpace;
    final Scaler[] scalers;
    final int numChannel;

    private boolean enabled = true;

    /**
     * listeners
     */
    private final List<LUTListener> listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    public LUT(final IcyColorModel cm) {
        colorSpace = cm.getIcyColorSpace();
        scalers = cm.getColormapScalers();
        numChannel = colorSpace.getNumComponents();

        if (scalers.length != numChannel) {
            throw new IllegalArgumentException(
                    "Incorrect size for scalers : " + scalers.length + ".  Expected : " + numChannel);
        }

        final DataType dataType = cm.getDataType();

        for (int channel = 0; channel < numChannel; channel++) {
            // BYTE data type --> fix bounds to data type bounds
            if (dataType == DataType.UBYTE)
                scalers[channel].setLeftRightIn(dataType.getMinValue(), dataType.getMaxValue());

            lutChannels.add(new LUTChannel(channel));
        }

        listeners = new ArrayList<>();
        updater = new UpdateEventHandler(this, false);

        // add listener
        for (final Scaler scaler : scalers)
            scaler.addListener(this);
        colorSpace.addListener(this);

    }

    protected int indexOf(final Scaler scaler) {
        for (int i = 0; i < scalers.length; i++)
            if (scalers[i] == scaler)
                return i;

        return -1;
    }

    public IcyColorSpace getColorSpace() {
        return colorSpace;
    }

    public Scaler[] getScalers() {
        return scalers;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<LUTChannel> getLutChannels() {
        return new ArrayList<>(lutChannels);
    }

    /**
     * Return the {@link LUTChannel} for specified channel index.
     */
    public LUTChannel getLutChannel(final int channel) {
        return lutChannels.get(channel);
    }

    /**
     * @return the number of channel.
     */
    public int getNumChannel() {
        return numChannel;
    }

    /**
     * Copy LUT from the specified source lut
     */
    public void copyFrom(final LUT lut) {
        beginUpdate();
        try {
            setColorMaps(lut, true);
            setScalers(lut);
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Set the scalers from the specified source lut (do a copy)
     */
    public void setScalers(final LUT lut) {
        final Scaler[] srcScalers = lut.getScalers();
        final int len = Math.min(scalers.length, srcScalers.length);

        beginUpdate();
        try {
            for (int i = 0; i < len; i++) {
                final Scaler src = srcScalers[i];
                final Scaler dst = scalers[i];

                dst.setAbsLeftRightIn(src.getAbsLeftIn(), src.getAbsRightIn());
                dst.setLeftRightIn(src.getLeftIn(), src.getRightIn());
                dst.setLeftRightOut(src.getLeftOut(), src.getRightOut());
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Set colormaps from the specified source lut (do a copy).
     *
     * @param lut
     *        source lut to use
     * @param setAlpha
     *        also set the alpha information
     */
    public void setColorMaps(final LUT lut, final boolean setAlpha) {
        getColorSpace().setColorMaps(lut.getColorSpace(), setAlpha);
    }

    /**
     * Set the alpha channel to full opaque for all LUT channel
     */
    public void setAlphaToOpaque() {
        beginUpdate();
        try {
            for (final LUTChannel lutChannel : getLutChannels())
                if (!lutChannel.getColorMap().isAlpha())
                    lutChannel.getColorMap().setAlphaToOpaque();
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Set the alpha channel to linear opacity (0 to 1) for all LUT channel
     */
    public void setAlphaToLinear() {
        beginUpdate();
        try {
            for (final LUTChannel lutChannel : getLutChannels())
                lutChannel.getColorMap().setAlphaToLinear();
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Set the alpha channel to an optimized linear transparency for 3D volume display on all LUT
     * channel
     */
    public void setAlphaToLinear3D() {
        beginUpdate();
        try {
            for (final LUTChannel lutChannel : getLutChannels())
                lutChannel.getColorMap().setAlphaToLinear3D();
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Return true if LUT is compatible with specified ColorModel.<br>
     * (Same number of channels with same data type)
     */
    public boolean isCompatible(final LUT lut) {
        if (numChannel != lut.getNumChannel())
            return false;

        final Scaler[] cmScalers = lut.getScalers();

        // check that data type is compatible
        for (int channel = 0; channel < numChannel; channel++)
            if (scalers[channel].isIntegerData() != cmScalers[channel].isIntegerData())
                return false;

        return true;
    }

    /**
     * Return true if LUT is compatible with specified ColorModel.<br>
     * (Same number of channels with same data type)
     */
    public boolean isCompatible(final IcyColorModel colorModel) {
        if (numChannel != colorModel.getNumComponents())
            return false;

        final Scaler[] cmScalers = colorModel.getColormapScalers();

        // check that data type is compatible
        for (int comp = 0; comp < numChannel; comp++)
            if (scalers[comp].isIntegerData() != cmScalers[comp].isIntegerData())
                return false;

        return true;
    }

    /**
     * Add a listener
     */
    public void addListener(final LUTListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     */
    public void removeListener(final LUTListener listener) {
        listeners.remove(listener);
    }

    public void fireLUTChanged(final LUTEvent e) {
        for (final LUTListener lutListener : new ArrayList<>(listeners))
            lutListener.lutChanged(e);
    }

    @Override
    public void onChanged(final CollapsibleEvent compare) {
        final LUTEvent event = (LUTEvent) compare;

        // notify listener we have changed
        fireLUTChanged(event);

        // propagate event to LUTChannel
        final int channel = event.getComponent();
        final LUTChannelEventType type = (event.getType() == LUTEventType.COLORMAP_CHANGED)
                ? LUTChannelEventType.COLORMAP_CHANGED
                : LUTChannelEventType.SCALER_CHANGED;

        if (channel == -1) {
            for (final LUTChannel lutChannel : lutChannels)
                lutChannel.fireEvent(new LUTChannelEvent(lutChannel, type));
        }
        else {
            final LUTChannel lutChannel = getLutChannel(channel);
            lutChannel.fireEvent(new LUTChannelEvent(lutChannel, type));
        }
    }

    @Override
    public void colorSpaceChanged(final IcyColorSpaceEvent e) {
        // notify LUT colormap changed
        updater.changed(new LUTEvent(this, e.getComponent(), LUTEventType.COLORMAP_CHANGED));
    }

    @Override
    public void scalerChanged(final ScalerEvent e) {
        // notify LUTBand changed
        updater.changed(new LUTEvent(this, indexOf(e.getScaler()), LUTEventType.SCALER_CHANGED));
    }

    public void beginUpdate() {
        updater.beginUpdate();
    }

    public void endUpdate() {
        updater.endUpdate();
    }

    public boolean isUpdating() {
        return updater.isUpdating();
    }

    @Override
    public boolean loadFromXML(final Node node) {
        if (node == null)
            return false;

        // different channel number --> exit
        if (numChannel != XMLUtil.getElementIntValue(node, ID_NUM_CHANNEL, 1))
            return false;

        beginUpdate();
        try {
            for (int ch = 0; ch < numChannel; ch++) {
                Node n;

                n = XMLUtil.getElement(node, ID_SCALER + ch);
                if (n != null)
                    scalers[ch].loadFromXML(n);
                n = XMLUtil.getElement(node, ID_COLORMAP + ch);
                if (n != null)
                    colorSpace.getColorMap(ch).loadFromXML(n);
            }
        }
        finally {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(final Node node) {
        if (node == null)
            return false;

        XMLUtil.setElementIntValue(node, ID_NUM_CHANNEL, numChannel);

        for (int ch = 0; ch < numChannel; ch++) {
            Node n;

            n = XMLUtil.setElement(node, ID_SCALER + ch);
            if (n != null)
                scalers[ch].saveToXML(n);
            n = XMLUtil.setElement(node, ID_COLORMAP + ch);
            if (n != null)
                colorSpace.getColorMap(ch).saveToXML(n);
        }

        return true;
    }
}
