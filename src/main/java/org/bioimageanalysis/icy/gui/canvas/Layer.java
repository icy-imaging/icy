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
package org.bioimageanalysis.icy.gui.canvas;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.overlay.Overlay.OverlayPriority;
import org.bioimageanalysis.icy.model.overlay.OverlayEvent;
import org.bioimageanalysis.icy.model.overlay.OverlayEvent.OverlayEventType;
import org.bioimageanalysis.icy.model.overlay.OverlayListener;
import org.bioimageanalysis.icy.model.overlay.WeakOverlayListener;
import org.bioimageanalysis.icy.model.roi.ROI;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer class.<br>
 * This class encapsulate {@link Overlay} in a canvas to<br>
 * add specific display properties (visibility, transparency...).
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Layer implements OverlayListener, Comparable<Layer> {
    public interface LayerListener {
        void layerChanged(Layer source, String propertyName);
    }

    public final static String PROPERTY_NAME = Overlay.PROPERTY_NAME;
    public final static String PROPERTY_PRIORITY = Overlay.PROPERTY_PRIORITY;
    public final static String PROPERTY_READONLY = Overlay.PROPERTY_READONLY;
    public final static String PROPERTY_CANBEREMOVED = Overlay.PROPERTY_CANBEREMOVED;
    public final static String PROPERTY_RECEIVEKEYEVENTONHIDDEN = Overlay.PROPERTY_RECEIVEKEYEVENTONHIDDEN;
    public final static String PROPERTY_RECEIVEMOUSEEVENTONHIDDEN = Overlay.PROPERTY_RECEIVEMOUSEEVENTONHIDDEN;
    public final static String PROPERTY_OPACITY = "opacity";
    public final static String PROPERTY_VISIBLE = "visible";

    public final static String DEFAULT_NAME = "layer";

    /**
     * Returns true if the Layer need to be repainted when the specified property has changed.
     */
    public static boolean isPaintProperty(final String propertyName) {
        if (propertyName == null)
            return false;

        return propertyName.equals(PROPERTY_OPACITY) || propertyName.equals(PROPERTY_PRIORITY)
                || propertyName.equals(PROPERTY_VISIBLE);
    }

    private final Overlay overlay;
    // cache for ROI
    private WeakReference<ROI> roi;

    private boolean visible;
    private float alpha;

    /**
     * listeners
     */
    protected final List<LayerListener> listeners;

    public Layer(final Overlay overlay) {
        this.overlay = overlay;

        overlay.addOverlayListener(new WeakOverlayListener(this));

        visible = true;
        alpha = 1f;
        roi = null;

        listeners = new ArrayList<>();
    }

    /**
     * Returns the attached {@link Overlay}.
     */
    public Overlay getOverlay() {
        return overlay;
    }

    /**
     * Returns layer priority (internally use the overlay priority).
     *
     * @see Overlay#getPriority()
     */
    public OverlayPriority getPriority() {
        return overlay.getPriority();
    }

    /**
     * Set the layer priority (internally set the overlay priority).
     *
     * @see Overlay#setPriority(OverlayPriority)
     */
    public void setPriority(final OverlayPriority priority) {
        overlay.setPriority(priority);
    }

    /**
     * Returns layer name (internally use the overlay name).
     *
     * @see Overlay#getName()
     */
    public String getName() {
        return overlay.getName();
    }

    /**
     * Set the layer name (internally set the overlay name)
     *
     * @see Overlay#setName(String)
     */
    public void setName(final String name) {
        overlay.setName(name);
    }

    /**
     * Returns the read only property name (internally use the overlay read only property).
     *
     * @see Overlay#isReadOnly()
     */
    public boolean isReadOnly() {
        return overlay.isReadOnly();
    }

    /**
     * Set read only property (internally set the overlay read only property).
     *
     * @see Overlay#setReadOnly(boolean)
     */
    public void setReadOnly(final boolean readOnly) {
        overlay.setReadOnly(readOnly);
    }

    /**
     * Returns <code>true</code> if the layer can be freely removed from the Canvas where it
     * appears and <code>false</code> otherwise.<br>
     *
     * @see Overlay#getCanBeRemoved()
     */
    public boolean getCanBeRemoved() {
        return overlay.getCanBeRemoved();
    }

    /**
     * Set the <code>canBeRemoved</code> property.<br>
     * Set it to false if you want to prevent the layer to be removed from the Canvas where it
     * appears.
     *
     * @see Overlay#setCanBeRemoved(boolean)
     */
    public void setCanBeRemoved(final boolean value) {
        overlay.setCanBeRemoved(value);
    }

    /**
     * @see Overlay#getReceiveKeyEventOnHidden()
     */
    public boolean getReceiveKeyEventOnHidden() {
        return overlay.getReceiveKeyEventOnHidden();
    }

    /**
     * @see Overlay#setReceiveKeyEventOnHidden(boolean)
     */
    public void setReceiveKeyEventOnHidden(final boolean value) {
        overlay.setReceiveKeyEventOnHidden(value);
    }

    /**
     * @see Overlay#getReceiveMouseEventOnHidden()
     */
    public boolean getReceiveMouseEventOnHidden() {
        return overlay.getReceiveMouseEventOnHidden();
    }

    /**
     * @see Overlay#setReceiveMouseEventOnHidden(boolean)
     */
    public void setReceiveMouseEventOnHidden(final boolean value) {
        overlay.setReceiveMouseEventOnHidden(value);
    }

    /**
     * @return the attachedROI
     */
    public ROI getAttachedROI() {
        if (roi == null)
            // search for attached ROI
            roi = new WeakReference<>(Icy.getMainInterface().getROI(overlay));

        return roi.get();
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *        the visible to set
     */
    public void setVisible(final boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            changed(PROPERTY_VISIBLE);
        }
    }

    /**
     * @return the layer opacity
     */
    public float getOpacity() {
        return alpha;
    }

    /**
     * Set the layer opacity
     */
    public void setOpacity(final float value) {
        if (alpha != value) {
            alpha = value;
            changed(PROPERTY_OPACITY);
        }
    }

    /**
     * Called on layer property change
     */
    protected void changed(final String propertyName) {
        // notify listener
        fireChangedEvent(propertyName);
    }

    /**
     * fire event
     */
    private void fireChangedEvent(final String propertyName) {
        final List<LayerListener> list;

        synchronized (listeners) {
            list = new ArrayList<>(listeners);
        }

        for (final LayerListener listener : list)
            listener.layerChanged(this, propertyName);
    }

    /**
     * Add a listener
     */
    public void addListener(final LayerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeListener(final LayerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void overlayChanged(final OverlayEvent event) {
        // only interested by property change here
        if (event.getType() == OverlayEventType.PROPERTY_CHANGED)
            changed(event.getPropertyName());
    }

    @Override
    public int compareTo(final Layer layer) {
        // compare with overlay
        return getOverlay().compareTo(layer.getOverlay());
    }
}
