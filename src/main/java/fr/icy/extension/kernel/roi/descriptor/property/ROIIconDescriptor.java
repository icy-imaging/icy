/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.extension.kernel.roi.descriptor.property;

import fr.icy.common.string.StringUtil;
import fr.icy.gui.LookAndFeelUtil;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.model.roi.ROI;
import fr.icy.model.roi.ROIDescriptor;
import fr.icy.model.roi.ROIEvent;
import fr.icy.model.roi.ROIEvent.ROIEventType;
import fr.icy.model.sequence.Sequence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Icon descriptor class (see {@link ROIDescriptor}).<br>
 * Return the ROI icon a 20 pixels side icon
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROIIconDescriptor extends ROIDescriptor<Icon> {
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
    public Icon compute(final @NotNull ROI roi, final Sequence sequence) throws UnsupportedOperationException {
        //return getIcon(roi);
        return new IcySVG(roi.getIcon()).getIcon(20, LookAndFeelUtil.ColorType.BUTTON_DEFAULT);
    }

    /**
     * Returns ROI icon
     */
    @Deprecated
    @Contract("null -> null")
    public static Icon getIcon(final ROI roi) {
        if (roi == null)
            return null;

        return new IcySVG(roi.getIcon()).getIcon(20, LookAndFeelUtil.getUIColor(LookAndFeelUtil.ColorType.BUTTON_DEFAULT));
    }
}
