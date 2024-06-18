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

package org.bioimageanalysis.icy.model.render.vtk;

import java.awt.*;

/**
 * Class to represent a 3D binary image as a 3D VTK volume object.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class VtkBinaryVolume extends VtkImageVolume {
    /**
     * Always single channel here
     */
    @Override
    protected int getChannelCount() {
        return 1;
    }

    /**
     * Set the color of the binary volume.
     */
    public void setColor(final Color value) {
        volumeProperty.SetColor(VtkUtil.getBinaryColorMap(value));
    }

    /**
     * Set the opacity of the binary volume.
     */
    public void setOpacity(final double value) {
        volumeProperty.SetScalarOpacity(VtkUtil.getBinaryOpacityMap(value));
    }

    /**
     * Enable / Disable the shading (global)
     */
    @Override
    public void setShade(final boolean value) {
        volumeProperty.SetShade(value ? 1 : 0);
    }

    /**
     * Sets the ambient lighting coefficient (global)
     */
    @Override
    public void setAmbient(final double value) {
        volumeProperty.SetAmbient(value);
    }

    /**
     * Sets the diffuse lighting coefficient (global)
     */
    @Override
    public void setDiffuse(final double value) {
        volumeProperty.SetDiffuse(value);
    }

    /**
     * Sets the specular lighting coefficient (global)
     */
    @Override
    public void setSpecular(final double value) {
        volumeProperty.SetSpecular(value);
    }

    /**
     * Sets the specular power (global)
     */
    @Override
    public void setSpecularPower(final double value) {
        volumeProperty.SetSpecularPower(value);
    }
}
