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

package org.bioimageanalysis.icy.gui.sequence.tools;

import org.bioimageanalysis.icy.model.image.IcyBufferedImageUtil.FilterType;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import javax.swing.*;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 */
public class SequenceResizePanel extends SequenceBaseResizePanel {
    private JComboBox<String> filterComboBox;

    public SequenceResizePanel(final Sequence sequence) {
        super(sequence);

        keepRatioCheckBox.setSelected(true);

        filterComboBox.addActionListener(e -> updatePreview());
    }

    @Override
    protected void initialize() {
        super.initialize();

        final JLabel lblFilterType = new JLabel("Filter type");
        final GridBagConstraints gbc_lblFilterType = new GridBagConstraints();
        gbc_lblFilterType.fill = GridBagConstraints.BOTH;
        gbc_lblFilterType.insets = new Insets(0, 0, 5, 5);
        gbc_lblFilterType.gridx = 5;
        gbc_lblFilterType.gridy = 0;
        settingPanel.add(lblFilterType, gbc_lblFilterType);

        filterComboBox = new JComboBox<>();
        filterComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"Nearest", "Bilinear", "Bicubic"}));
        filterComboBox.setSelectedIndex(1);
        final GridBagConstraints gbc_filterComboBox = new GridBagConstraints();
        gbc_filterComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_filterComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_filterComboBox.gridx = 5;
        gbc_filterComboBox.gridy = 1;
        settingPanel.add(filterComboBox, gbc_filterComboBox);
    }

    @Override
    public FilterType getFilterType() {
        return switch (filterComboBox.getSelectedIndex()) {
            default -> FilterType.NEAREST;
            case 1 -> FilterType.BILINEAR;
            case 2 -> FilterType.BICUBIC;
        };
    }

    @Override
    public boolean getResizeContent() {
        return true;
    }

    @Override
    public int getXAlign() {
        return SwingConstants.CENTER;
    }

    @Override
    public int getYAlign() {
        return SwingConstants.CENTER;
    }
}
