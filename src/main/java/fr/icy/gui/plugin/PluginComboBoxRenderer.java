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

package fr.icy.gui.plugin;

import fr.icy.extension.ExtensionLoader;
import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.gui.LookAndFeelUtil;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.renderer.CustomComboBoxRenderer;

import javax.swing.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PluginComboBoxRenderer extends CustomComboBoxRenderer {
    private final boolean showLabel;

    public PluginComboBoxRenderer(final JComboBox<String> combo, final boolean showLabel) {
        super(combo);

        this.showLabel = showLabel;
    }

    @Override
    protected void updateItem(final JList<?> list, final Object value) {
        if (value instanceof String) {
            final PluginDescriptor plugin = ExtensionLoader.getPlugin((String) value);

            if (plugin != null) {
                //setIcon(ResourceUtil.scaleIcon(plugin.getIcon(), 16));
                final int size = LookAndFeelUtil.getDefaultIconSize();

                try {
                    final IcySVG icons = plugin.getSVG();
                    if (icons != null) {
                        setIcon(icons.getIcon(size));
                    }
                }
                catch (final Throwable t) {
                    //
                }

                /*final ImageIcon imgIcon = plugin.getIcon();
                if (imgIcon != null)
                    setIcon(new ImageIcon(imgIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH))); // TODO: 10/02/2023 Set smooth mode for ResourceUtil.scaleIcon
                else
                    setIcon(new IcySVGIcon(SVGIcon.BROKEN_IMAGE));*/
                if (showLabel)
                    setText(plugin.getName());
                else
                    setText("");
                setToolTipText(plugin.getDescription());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
