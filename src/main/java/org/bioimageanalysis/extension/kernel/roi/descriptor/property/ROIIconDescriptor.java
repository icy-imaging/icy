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

package org.bioimageanalysis.extension.kernel.roi.descriptor.property;

import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGImageIcon;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.roi.ROIEvent;
import org.bioimageanalysis.icy.model.roi.ROIEvent.ROIEventType;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Icon descriptor class (see {@link ROIDescriptor}).<br>
 * Return the ROI icon a 20 pixels side icon
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIIconDescriptor extends ROIDescriptor {
    public static final String ID = "Icon";

    public ROIIconDescriptor() {
        super(ID, "Icon", Image.class);
    }

    @Override
    public String getDescription() {
        return "Icon";
    }

    @Override
    public boolean needRecompute(final @NotNull ROIEvent change) {
        return (change.getType() == ROIEventType.PROPERTY_CHANGED) && (StringUtil.equals(change.getPropertyName(), ROI.PROPERTY_ICON));
    }

    @Override
    public Object compute(final ROI roi, final Sequence sequence) throws UnsupportedOperationException {
        return getIcon(roi);
    }

    /**
     * Returns ROI icon
     */
    @Contract("null -> null")
    public static Image getIcon(final ROI roi) {
        if (roi == null)
            return null;

        return new IcySVGImageIcon(roi.getIcon(), LookAndFeelUtil.isDarkMode() ? Color.WHITE : Color.BLACK).getImage();
    }
}
