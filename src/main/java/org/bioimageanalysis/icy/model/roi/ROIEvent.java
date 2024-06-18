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

import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.string.StringUtil;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIEvent implements CollapsibleEvent {
    public enum ROIEventType {
        FOCUS_CHANGED, SELECTION_CHANGED,
        /**
         * ROI position or/and content change event.<br>
         * property = {@link ROI#ROI_CHANGED_POSITION} when only
         * position has changed
         */
        ROI_CHANGED,
        /**
         * ROI property change event.<br>
         * check property field to know which property has actually changed
         */
        PROPERTY_CHANGED
    }

    private final ROI source;
    private final ROIEventType type;
    private final String propertyName;

    public ROIEvent(final ROI source, final ROIEventType type, final String propertyName) {
        super();

        this.source = source;
        this.type = type;
        this.propertyName = propertyName;
    }

    public ROIEvent(final ROI source, final String propertyName) {
        this(source, ROIEventType.PROPERTY_CHANGED, propertyName);
    }

    public ROIEvent(final ROI source, final ROIEventType type) {
        this(source, type, null);
    }

    /**
     * @return the source
     */
    public ROI getSource() {
        return source;
    }

    /**
     * @return the type
     */
    public ROIEventType getType() {
        return type;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public boolean collapse(final CollapsibleEvent event) {
        if (equals(event)) {
            // nothing to do here
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int res = source.hashCode() ^ type.hashCode();

        if (type == ROIEventType.PROPERTY_CHANGED)
            res ^= propertyName.hashCode();

        return res;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final ROIEvent e) {
            if ((e.getSource() == source) && (e.getType() == type)) {
                return switch (type) {
                    case ROI_CHANGED -> StringUtil.equals(propertyName, e.getPropertyName());
                    case FOCUS_CHANGED, SELECTION_CHANGED -> true;
                    case PROPERTY_CHANGED -> StringUtil.equals(propertyName, e.getPropertyName());
                };
            }
        }

        return super.equals(obj);
    }
}