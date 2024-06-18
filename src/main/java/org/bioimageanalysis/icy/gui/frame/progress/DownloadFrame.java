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

package org.bioimageanalysis.icy.gui.frame.progress;

import org.bioimageanalysis.icy.common.math.RateMeter;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class DownloadFrame extends CancelableProgressFrame {
    /**
     * calculated download rate
     */
    private double rate;

    /**
     * internal
     */
    private final RateMeter meter;

    public DownloadFrame() {
        this("", 0);
    }

    public DownloadFrame(final String path) {
        this(path, 0);
    }

    public DownloadFrame(final String path, final double length) {
        super(StringUtil.isEmpty(path) ? "" : StringUtil.limit("Downloading " + path, 64));

        meter = new RateMeter();
        this.length = length;
        rate = 0;
    }

    public void setPath(final String path) {
        setMessage(StringUtil.limit("Downloading " + path, 64));
    }

    @Override
    protected String buildMessage(final String text) {
        String mess = text + "  [";

        // information on position
        if (position != -1d)
            mess += UnitUtil.getBytesString(position);
        else
            mess += "???";

        mess += " / ";

        if (length > 0d)
            mess += UnitUtil.getBytesString(length);
        else
            mess += "???";

        if (rate > 0)
            mess += " - " + UnitUtil.getBytesString(rate) + "/s";

        mess += "]";

        return super.buildMessage(mess);
    }

    @Override
    public void setLength(final double length) {
        if (getLength() != length)
            meter.reset();

        super.setLength(length);
    }

    @Override
    public void setPosition(final double position) {
        // update rate
        if (getPosition() < position)
            rate = meter.updateFromTotal(position);
        else
            rate = 0;

        super.setPosition(position);
    }
}
