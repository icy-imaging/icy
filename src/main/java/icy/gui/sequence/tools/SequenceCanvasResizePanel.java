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

package icy.gui.sequence.tools;

import icy.image.IcyBufferedImageUtil.FilterType;
import icy.sequence.Sequence;

import javax.swing.*;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SequenceCanvasResizePanel extends SequenceBaseResizePanel {
    private PositionAlignmentPanel positionAlignmentPanel;

    public SequenceCanvasResizePanel(final Sequence sequence) {
        super(sequence);

        keepRatioCheckBox.setSelected(false);

        positionAlignmentPanel.addActionListener(e -> updatePreview());
    }

    @Override
    protected void initialize() {
        super.initialize();

        final JLabel lblNewLabel_1 = new JLabel("Content alignment");
        final GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
        gbc_lblNewLabel_1.gridx = 5;
        gbc_lblNewLabel_1.gridy = 0;
        settingPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        positionAlignmentPanel = new PositionAlignmentPanel();
        final GridBagConstraints gbc_positionAlignmentPanel = new GridBagConstraints();
        gbc_positionAlignmentPanel.gridheight = 4;
        gbc_positionAlignmentPanel.insets = new Insets(0, 0, 5, 5);
        gbc_positionAlignmentPanel.fill = GridBagConstraints.BOTH;
        gbc_positionAlignmentPanel.gridx = 5;
        gbc_positionAlignmentPanel.gridy = 1;
        settingPanel.add(positionAlignmentPanel, gbc_positionAlignmentPanel);
    }

    @Override
    public FilterType getFilterType() {
        return null;
    }

    @Override
    public boolean getResizeContent() {
        return false;
    }

    @Override
    public int getXAlign() {
        return positionAlignmentPanel.getXAlign();
    }

    @Override
    public int getYAlign() {
        return positionAlignmentPanel.getYAlign();
    }
}
