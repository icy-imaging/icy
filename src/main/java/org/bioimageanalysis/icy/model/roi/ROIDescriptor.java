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

package org.bioimageanalysis.icy.model.roi;

import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginLauncher;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROIDescriptor;
import org.bioimageanalysis.icy.model.roi.ROIEvent.ROIEventType;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Abstract class providing the basic methods to retrieve properties and compute a specific
 * descriptor for a region of interest (ROI)
 *
 * @author Stephane Dallongeville
 * @author Alexandre Dufour
 * @author Thomas Musset
 */
public abstract class ROIDescriptor<O> {
    /**
     * Returns all available ROI descriptors (see {@link ROIDescriptor}) and their attached plugin
     * (see {@link PluginROIDescriptor}).<br>
     * This list can be extended by installing new plugin(s) implementing the {@link PluginROIDescriptor} interface.
     *
     * @see ROIDescriptor#compute(ROI, Sequence)
     * @see PluginROIDescriptor#compute(ROI, Sequence)
     */
    public static @NotNull Map<ROIDescriptor<?>, PluginROIDescriptor> getDescriptors() {
        final Map<ROIDescriptor<?>, PluginROIDescriptor> result = new HashMap<>();
        final Set<PluginDescriptor> pluginDescriptors = ExtensionLoader.getPlugins(PluginROIDescriptor.class);

        for (final PluginDescriptor pluginDescriptor : pluginDescriptors) {
            try {
                final PluginROIDescriptor plugin = (PluginROIDescriptor) PluginLauncher.create(pluginDescriptor);
                final List<ROIDescriptor<?>> descriptors = plugin.getDescriptors();

                if (descriptors != null) {
                    for (final ROIDescriptor<?> roiDescriptor : descriptors)
                        result.put(roiDescriptor, plugin);
                }
            }
            catch (final Throwable e) {
                // show a message in the output console
                IcyLogger.error(ROIDescriptor.class, e, e.getLocalizedMessage());
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(pluginDescriptor, IcyExceptionHandler.getErrorMessage(e, true));
            }
        }

        return result;
    }

    /**
     * Returns the descriptor identified by the given id from the given list of {@link ROIDescriptor}.<br>
     * It can return <code>null</code> if the descriptor is not found in the given list.
     *
     * @param id the id of the descriptor
     * @see #getDescriptors()
     * @see #computeDescriptor(String, ROI, Sequence)
     */
    public static @Nullable ROIDescriptor<?> getDescriptor(final @NotNull Collection<ROIDescriptor<?>> descriptors, final String id) {
        for (final ROIDescriptor<?> roiDescriptor : descriptors)
            if (StringUtil.equals(roiDescriptor.getId(), id))
                return roiDescriptor;

        return null;
    }

    /**
     * Returns the descriptor identified by the given id from the given list of {@link ROIDescriptor}.<br>
     * It can return <code>null</code> if the descriptor is not found in the given list.
     *
     * @param id the id of the descriptor
     * @see #getDescriptors()
     * @see #computeDescriptor(String, ROI, Sequence)
     */
    public static ROIDescriptor<?> getDescriptor(final String id) {
        return getDescriptor(getDescriptors().keySet(), id);
    }

    /**
     * Computes the specified descriptor from the input {@link ROIDescriptor} set on given ROI
     * and returns the result (or <code>null</code> if the descriptor is not found).
     *
     * @param roiDescriptors the input {@link ROIDescriptor} set (see {@link #getDescriptors()} method)
     * @param descriptorId   the id of the descriptor we want to compute
     * @param roi            the ROI on which the descriptor(s) should be computed
     * @param sequence       an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     * specified set
     * @throws UnsupportedOperationException if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *                                       <code>null</code> while the calculation requires it, or if
     *                                       the specified Z, T or C position are not supported by the descriptor
     * @throws InterruptedException          if the thread was interrupted during the computation of the descriptor
     */
    public static @Nullable Object computeDescriptor(final Collection<ROIDescriptor<?>> roiDescriptors, final String descriptorId, final ROI roi, final Sequence sequence)
            throws UnsupportedOperationException, InterruptedException {
        final ROIDescriptor<?> roiDescriptor = getDescriptor(roiDescriptors, descriptorId);

        if (roiDescriptor != null)
            return roiDescriptor.compute(roi, sequence);

        return null;
    }

    /**
     * Computes the specified descriptor on given ROI and returns the result (or <code>null</code> if the descriptor is
     * not found).
     *
     * @param descriptorId the id of the descriptor we want to compute
     * @param roi          the ROI on which the descriptor(s) should be computed
     * @param sequence     an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     * specified set
     * @throws UnsupportedOperationException if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *                                       <code>null</code> while the calculation requires it, or if
     *                                       the specified Z, T or C position are not supported by the descriptor
     * @throws InterruptedException          if the thread was interrupted during the computation of the descriptor
     */
    public static Object computeDescriptor(final String descriptorId, final ROI roi, final Sequence sequence) throws UnsupportedOperationException, InterruptedException {
        return computeDescriptor(getDescriptors().keySet(), descriptorId, roi, sequence);
    }

    protected final String id;
    protected final String name;
    protected final Class<?> type;

    /**
     * Create a new {@link ROIDescriptor} with given id, name and type
     */
    @Contract(pure = true)
    protected ROIDescriptor(final String id, final String name, final Class<?> type) {
        super();

        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Create a new {@link ROIDescriptor} with given name and type
     */
    @Contract(pure = true)
    protected ROIDescriptor(final String name, final Class<?> type) {
        this(name, name, type);
    }

    /**
     * Returns the id of this descriptor.<br>
     * By default it uses the descriptor's name but it can be overridden to be different.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this descriptor.<br>
     * The name is used as title (column header) in the ROI panel so keep it short and self
     * explanatory.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a single line description (used as tooltip) for this descriptor
     */
    public abstract String getDescription();

    /**
     * Returns the unit of this descriptor (<code>ex: "px", "mm", "µm2"...</code>).<br>
     * It can return an empty or <code>null</code> string (default implementation) if there is no
     * specific unit attached to the descriptor.<br>
     * Note that unit is concatenated to the name to build the title (column header) in the ROI
     * panel.
     *
     * @param sequence the sequence on which we want to compute the descriptor (if required) to get access to
     *                 the pixel size informations and return according unit
     */
    public String getUnit(final Sequence sequence) {
        return null;
    }

    /**
     * Returns the type of result for this descriptor
     *
     * @see #compute(ROI, Sequence)
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the [minimum, maximum] allowed value for this descriptor [(useful only for Number type descriptor)
     *
     * @see #compute(ROI, Sequence)
     */
    public Object[] getBounds() {
        if (type == Byte.class)
            return new Object[]{Byte.valueOf((byte) 0), Byte.valueOf(Byte.MAX_VALUE)};
        if (type == Short.class)
            return new Object[]{Short.valueOf((short) 0), Long.valueOf(Short.MAX_VALUE)};
        if (type == Integer.class)
            return new Object[]{Integer.valueOf(0), Integer.valueOf(Integer.MAX_VALUE)};
        if (type == Long.class)
            return new Object[]{Long.valueOf(0), Long.valueOf(Long.MAX_VALUE)};
        if (type == Float.class)
            return new Object[]{Float.valueOf(0f), Float.valueOf(1f)};
        if (type == Double.class)
            return new Object[]{Double.valueOf(0d), Double.valueOf(1d)};

        return null;
    }

    /**
     * Returns <code>true</code> if this descriptor compute its result on {@link Sequence} data and *per channel* (as
     * pixel intensity information).<br>
     * By default it returns <code>false</code>, override this method if a descriptor require per channel computation.
     *
     * @see #compute(ROI, Sequence)
     */
    public boolean separateChannel() {
        return false;
    }

    /**
     * Returns <code>true</code> if this descriptor need to be recomputed when the specified Sequence change event
     * happen.<br>
     * By default it returns <code>false</code>, override this method if a descriptor need a specific implementation.
     *
     * @see #compute(ROI, Sequence)
     */
    public boolean needRecompute(final SequenceEvent change) {
        return false;
    }

    /**
     * Returns <code>true</code> if this descriptor need to be recomputed when the specified ROI change event happen.<br>
     * By default it returns <code>true</code> on ROI content change, override this method if a descriptor need a
     * specific implementation.
     *
     * @see #compute(ROI, Sequence)
     */
    public boolean needRecompute(final @NotNull ROIEvent change) {
        return (change.getType() == ROIEventType.ROI_CHANGED);
    }

    /**
     * Computes the descriptor on the specified ROI and return the result.
     *
     * @param roi      the ROI on which the descriptor(s) should be computed
     * @param sequence an optional sequence where the pixel informations can be retrieved (see {@link #separateChannel()})
     * @return the result of this descriptor computed from the specified parameters.
     * @throws UnsupportedOperationException if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *                                       <code>null</code> while the calculation requires it
     * @throws InterruptedException          if the thread was interrupted during the computation of the descriptor
     */
    public abstract O compute(ROI roi, Sequence sequence) throws UnsupportedOperationException, InterruptedException;

    /*
     * We want a unique id for each {@link ROIDescriptor}
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ROIDescriptor)
            return StringUtil.equals(((ROIDescriptor<?>) obj).getId(), getId());

        return super.equals(obj);
    }

    /*
     * We want a unique id for each {@link ROIDescriptor}
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        // default implementation
        return getName();
    }
}