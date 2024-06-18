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

import org.bioimageanalysis.icy.gui.component.field.IcyTextField;
import org.bioimageanalysis.icy.gui.sequence.SequencePreviewPanel;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.image.IcyBufferedImageUtil;
import org.bioimageanalysis.icy.model.image.IcyBufferedImageUtil.FilterType;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.sequence.AbstractSequenceModel;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class SequenceBaseResizePanel extends JPanel {
    public enum SizeUnit {
        PIXEL, PERCENT, MICRON
    }

    protected class OriginalModel extends AbstractSequenceModel {
        public OriginalModel() {
            super();
        }

        @Override
        public int getSizeX() {
            return getMaxSizeX();
        }

        @Override
        public int getSizeY() {
            return getMaxSizeY();
        }

        @Override
        public int getSizeZ() {
            return sequence.getSizeZ();
        }

        @Override
        public int getSizeT() {
            return sequence.getSizeT();
        }

        @Override
        public int getSizeC() {
            return sequence.getSizeC();
        }

        @Override
        public BufferedImage getImage(final int t, final int z) {
            return sequence.getImage(t, z);
        }

        @Override
        public BufferedImage getImage(final int t, final int z, final int c) {
            return sequence.getImage(t, z, c);
        }
    }

    protected class ResultModel extends AbstractSequenceModel {
        public ResultModel() {
            super();
        }

        @Override
        public int getSizeX() {
            return getMaxSizeX();
        }

        @Override
        public int getSizeY() {
            return getMaxSizeY();
        }

        @Override
        public int getSizeZ() {
            return sequence.getSizeZ();
        }

        @Override
        public int getSizeT() {
            return sequence.getSizeT();
        }

        @Override
        public int getSizeC() {
            return sequence.getSizeC();
        }

        @Override
        public BufferedImage getImage(final int t, final int z) {
            try {
                return IcyBufferedImageUtil.scale(sequence.getImage(t, z), getNewWidth(), getNewHeight(), getResizeContent(), getXAlign(), getYAlign(), getFilterType());
            }
            catch (final OutOfMemoryError e) {
                return null;
            }
        }

        @Override
        public BufferedImage getImage(final int t, final int z, final int c) {
            return ((IcyBufferedImage) getImage(t, z)).getImage(c);
        }
    }

    final Sequence sequence;

    // GUI
    protected JCheckBox keepRatioCheckBox;
    protected JSpinner heightSpinner;
    protected JSpinner widthSpinner;
    protected SequencePreviewPanel originalPreview;
    protected SequencePreviewPanel resultPreview;
    protected JPanel infoPanel;
    protected IcyTextField widthField;
    protected IcyTextField heightField;
    protected IcyTextField sizeField;
    protected JComboBox<String> sizeUnitComboBox;
    protected JLabel accolLeftLabel;
    protected JPanel panel;
    protected Component horizontalGlue;
    protected Component horizontalGlue_1;
    protected JPanel settingPanel;

    /**
     * Create the panel.
     */
    public SequenceBaseResizePanel(final @NotNull Sequence sequence) {
        super();

        this.sequence = sequence;

        initialize();

        setNewWidth(sequence.getSizeX());
        setNewHeight(sequence.getSizeY());

        accolLeftLabel.setIcon(new IcySVGIcon(SVGIcon.BRACKET_RIGHT, 40));
        accolLeftLabel.setText(null);

        originalPreview.setFitToView(false);
        resultPreview.setFitToView(false);
        originalPreview.setModel(new OriginalModel());
        resultPreview.setModel(new ResultModel());

        updatePreview();

        final ChangeListener spinnerChangeListener = e -> {
            // maintain ratio
            if (keepRatioCheckBox.isSelected()) {
                final Sequence seq = SequenceBaseResizePanel.this.sequence;

                if (e.getSource() == widthSpinner) {
                    // adjust height
                    final double ratio = (double) getNewWidth() / (double) seq.getWidth();
                    setNewHeight((int) Math.round(seq.getHeight() * ratio));
                }
                else {
                    // adjust width
                    final double ratio = (double) getNewHeight() / (double) seq.getHeight();
                    setNewWidth((int) Math.round(seq.getWidth() * ratio));
                }
            }

            updatePreview();
        };
        heightSpinner.addChangeListener(spinnerChangeListener);
        widthSpinner.addChangeListener(spinnerChangeListener);

        sizeUnitComboBox.addActionListener(e -> {
            // convert width and height in new unit
            final int w = Integer.parseInt(widthField.getText());
            final int h = Integer.parseInt(heightField.getText());

            setNewWidth(w);
            setNewHeight(h);
        });
    }

    protected void initialize() {
        setLayout(new BorderLayout(0, 0));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        add(panel, BorderLayout.NORTH);

        infoPanel = new JPanel();
        panel.add(infoPanel);
        infoPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Size in pixel", TitledBorder.LEADING, TitledBorder.TOP));
        final GridBagLayout gbl_infoPanel = new GridBagLayout();
        gbl_infoPanel.columnWidths = new int[]{20, 100, 20, 100, 20, 100, 20, 0};
        gbl_infoPanel.rowHeights = new int[]{0, 0, 0};
        gbl_infoPanel.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_infoPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        infoPanel.setLayout(gbl_infoPanel);

        final JLabel lblOriginalWidth = new JLabel("Width");
        final GridBagConstraints gbc_lblOriginalWidth = new GridBagConstraints();
        gbc_lblOriginalWidth.fill = GridBagConstraints.BOTH;
        gbc_lblOriginalWidth.insets = new Insets(0, 0, 5, 5);
        gbc_lblOriginalWidth.gridx = 1;
        gbc_lblOriginalWidth.gridy = 0;
        infoPanel.add(lblOriginalWidth, gbc_lblOriginalWidth);
        lblOriginalWidth.setToolTipText("");

        final JLabel lblNewLabel_3 = new JLabel("Height");
        final GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
        gbc_lblNewLabel_3.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_3.gridx = 3;
        gbc_lblNewLabel_3.gridy = 0;
        infoPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
        lblNewLabel_3.setToolTipText("");

        final JLabel lblNewLabel_2 = new JLabel("Memory size");
        final GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 5;
        gbc_lblNewLabel_2.gridy = 0;
        infoPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
        lblNewLabel_2.setToolTipText("");

        widthField = new IcyTextField();
        widthField.setToolTipText("Width in pixel");
        widthField.setText("0000");
        widthField.setEditable(false);
        final GridBagConstraints gbc_widthField = new GridBagConstraints();
        gbc_widthField.fill = GridBagConstraints.BOTH;
        gbc_widthField.insets = new Insets(0, 0, 0, 5);
        gbc_widthField.gridx = 1;
        gbc_widthField.gridy = 1;
        infoPanel.add(widthField, gbc_widthField);
        widthField.setColumns(5);

        heightField = new IcyTextField();
        heightField.setToolTipText("Height in pixel");
        heightField.setText("0");
        heightField.setEditable(false);
        final GridBagConstraints gbc_heightField = new GridBagConstraints();
        gbc_heightField.fill = GridBagConstraints.BOTH;
        gbc_heightField.insets = new Insets(0, 0, 0, 5);
        gbc_heightField.gridx = 3;
        gbc_heightField.gridy = 1;
        infoPanel.add(heightField, gbc_heightField);
        heightField.setColumns(5);

        sizeField = new IcyTextField();
        sizeField.setToolTipText("Memory size");
        sizeField.setText("0.0B");
        sizeField.setEditable(false);
        final GridBagConstraints gbc_sizeField = new GridBagConstraints();
        gbc_sizeField.insets = new Insets(0, 0, 0, 5);
        gbc_sizeField.fill = GridBagConstraints.BOTH;
        gbc_sizeField.gridx = 5;
        gbc_sizeField.gridy = 1;
        infoPanel.add(sizeField, gbc_sizeField);
        sizeField.setColumns(5);

        settingPanel = new JPanel();
        panel.add(settingPanel);
        settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Setting", TitledBorder.LEADING, TitledBorder.TOP));
        final GridBagLayout gbl_settingPanel = new GridBagLayout();
        gbl_settingPanel.columnWidths = new int[]{20, 100, 20, 100, 20, 100, 20, 0};
        gbl_settingPanel.rowHeights = new int[]{0, 0, 0, 0, 10, 0, 0, 0};
        gbl_settingPanel.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_settingPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        settingPanel.setLayout(gbl_settingPanel);

        final JLabel lblWidth = new JLabel("Width");
        final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
        gbc_lblWidth.fill = GridBagConstraints.BOTH;
        gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
        gbc_lblWidth.gridx = 1;
        gbc_lblWidth.gridy = 0;
        settingPanel.add(lblWidth, gbc_lblWidth);

        widthSpinner = new JSpinner();
        widthSpinner.setModel(new SpinnerNumberModel(1L, 1L, Long.MAX_VALUE, 1L));
        widthSpinner.setToolTipText("New width to set");
        final GridBagConstraints gbc_widthSpinner = new GridBagConstraints();
        gbc_widthSpinner.fill = GridBagConstraints.BOTH;
        gbc_widthSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_widthSpinner.gridx = 1;
        gbc_widthSpinner.gridy = 1;
        settingPanel.add(widthSpinner, gbc_widthSpinner);

        final JLabel lblNewLabel = new JLabel("Height");
        final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 1;
        gbc_lblNewLabel.gridy = 2;
        settingPanel.add(lblNewLabel, gbc_lblNewLabel);

        accolLeftLabel = new JLabel("");
        accolLeftLabel.setHorizontalAlignment(SwingConstants.CENTER);
        final GridBagConstraints gbc_accolLeftLabel = new GridBagConstraints();
        gbc_accolLeftLabel.fill = GridBagConstraints.BOTH;
        gbc_accolLeftLabel.gridheight = 3;
        gbc_accolLeftLabel.insets = new Insets(0, 0, 5, 5);
        gbc_accolLeftLabel.gridx = 2;
        gbc_accolLeftLabel.gridy = 1;
        settingPanel.add(accolLeftLabel, gbc_accolLeftLabel);
        lblNewLabel.setLabelFor(heightSpinner);

        sizeUnitComboBox = new JComboBox<>();
        sizeUnitComboBox.setMaximumRowCount(3);
        sizeUnitComboBox.setToolTipText("Width / Height unit");
        sizeUnitComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"pixel", "%", UnitUtil.MICRO_STRING + "m"}));
        sizeUnitComboBox.setSelectedIndex(0);
        final GridBagConstraints gbc_sizeUnitComboBox = new GridBagConstraints();
        gbc_sizeUnitComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeUnitComboBox.gridheight = 3;
        gbc_sizeUnitComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_sizeUnitComboBox.gridx = 3;
        gbc_sizeUnitComboBox.gridy = 1;
        settingPanel.add(sizeUnitComboBox, gbc_sizeUnitComboBox);

        heightSpinner = new JSpinner();
        heightSpinner.setModel(new SpinnerNumberModel(1L, 1L, Long.MAX_VALUE, 1L));
        heightSpinner.setToolTipText("New height to set");
        final GridBagConstraints gbc_heightSpinner = new GridBagConstraints();
        gbc_heightSpinner.fill = GridBagConstraints.BOTH;
        gbc_heightSpinner.insets = new Insets(0, 0, 5, 5);
        gbc_heightSpinner.gridx = 1;
        gbc_heightSpinner.gridy = 3;
        settingPanel.add(heightSpinner, gbc_heightSpinner);

        keepRatioCheckBox = new JCheckBox("Keep ratio");
        keepRatioCheckBox.setVerticalAlignment(SwingConstants.TOP);
        keepRatioCheckBox.setToolTipText("Keep original aspect ratio");
        keepRatioCheckBox.setSelected(true);
        final GridBagConstraints gbc_keepRatioCheckBox = new GridBagConstraints();
        gbc_keepRatioCheckBox.gridwidth = 3;
        gbc_keepRatioCheckBox.fill = GridBagConstraints.BOTH;
        gbc_keepRatioCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_keepRatioCheckBox.gridx = 1;
        gbc_keepRatioCheckBox.gridy = 5;
        settingPanel.add(keepRatioCheckBox, gbc_keepRatioCheckBox);

        horizontalGlue = Box.createHorizontalGlue();
        final GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
        gbc_horizontalGlue.fill = GridBagConstraints.HORIZONTAL;
        gbc_horizontalGlue.insets = new Insets(0, 0, 0, 5);
        gbc_horizontalGlue.gridx = 5;
        gbc_horizontalGlue.gridy = 6;
        settingPanel.add(horizontalGlue, gbc_horizontalGlue);

        horizontalGlue_1 = Box.createHorizontalGlue();
        final GridBagConstraints gbc_horizontalGlue_1 = new GridBagConstraints();
        gbc_horizontalGlue_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_horizontalGlue_1.gridx = 6;
        gbc_horizontalGlue_1.gridy = 6;
        settingPanel.add(horizontalGlue_1, gbc_horizontalGlue_1);

        final JPanel previewPanel = new JPanel();
        previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(previewPanel, BorderLayout.CENTER);
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.LINE_AXIS));

        originalPreview = new SequencePreviewPanel("Original");
        previewPanel.add(originalPreview);

        resultPreview = new SequencePreviewPanel("Result");
        previewPanel.add(resultPreview);
    }

    void updatePreview() {
        final int w = getNewWidth();
        final int h = getNewHeight();

        widthField.setText(Integer.toString(w));
        heightField.setText(Integer.toString(h));
        sizeField.setText(UnitUtil.getBytesString(w * h * sequence.getSizeC() * sequence.getSizeZ() * sequence.getSizeT() * sequence.getDataType().getSize()));

        originalPreview.imageChanged();
        resultPreview.imageChanged();
    }

    public Sequence getSequence() {
        return sequence;
    }

    /**
     * pixel resolution X (micron / pixel)
     */
    public double getPixelSizeX() {
        if (sequence != null)
            return sequence.getPixelSizeX();

        return 1d;
    }

    /**
     * pixel resolution X (micron / pixel)
     */
    public double getPixelSizeY() {
        if (sequence != null)
            return sequence.getPixelSizeY();

        return 1d;
    }

    public SizeUnit getSizeUnit() {
        return switch (sizeUnitComboBox.getSelectedIndex()) {
            default -> SizeUnit.PIXEL;
            case 1 -> SizeUnit.PERCENT;
            case 2 -> SizeUnit.MICRON;
        };
    }

    public int unitToPixel(final double value, final int originPixel, final SizeUnit unit, final double micronPerPixel) {
        return switch (unit) {
            default -> (int) Math.round(value);
            case PERCENT -> (int) Math.round((originPixel * value) / 100d);
            case MICRON -> (int) Math.round(value / micronPerPixel);
        };
    }

    public int unitToPixelX(final double value, final int originPixel, final SizeUnit unit) {
        return unitToPixel(value, originPixel, unit, getPixelSizeX());
    }

    public int unitToPixelY(final double value, final int originPixel, final SizeUnit unit) {
        return unitToPixel(value, originPixel, unit, getPixelSizeY());
    }

    public double pixelToUnit(final int value, final int originPixel, final SizeUnit unit, final double micronPerPixel) {
        return switch (unit) {
            default -> value;
            case PERCENT -> (int) Math.round((value * 100d) / originPixel);
            case MICRON -> (int) (value * micronPerPixel);
        };
    }

    public double pixelXToUnit(final int value, final int originPixel, final SizeUnit unit) {
        return pixelToUnit(value, originPixel, unit, getPixelSizeX());
    }

    public double pixelYToUnit(final int value, final int originPixel, final SizeUnit unit) {
        return pixelToUnit(value, originPixel, unit, getPixelSizeY());
    }

    public double getSpinnerSizeValue(final JSpinner spinner) {
        return switch (getSizeUnit()) {
            default -> ((Integer) spinner.getValue()).intValue();
            case PERCENT, MICRON -> ((Double) spinner.getValue()).doubleValue();
        };
    }

    public int getNewWidth() {
        final int result = unitToPixelX(getSpinnerSizeValue(widthSpinner), sequence.getSizeX(), getSizeUnit());

        return Math.min(65535, Math.max(1, result));
    }

    public int getNewHeight() {
        final int result = unitToPixelY(getSpinnerSizeValue(heightSpinner), sequence.getSizeY(), getSizeUnit());

        return Math.min(Math.max(1, result), 65535);
    }

    void setSpinnerSizeValue(final JSpinner spinner, final double value) {
        switch (getSizeUnit()) {
            default:
            case PIXEL:
                spinner.setModel(new SpinnerNumberModel((int) value, 0, 65535, 1));
                // we don't want the model to affect
                ((DefaultEditor) spinner.getEditor()).getTextField().setColumns(1);
                break;

            case PERCENT:
                spinner.setModel(new SpinnerNumberModel(value, 0d, Double.MAX_VALUE, 1d));
                // we don't want the model to affect
                ((DefaultEditor) spinner.getEditor()).getTextField().setColumns(1);
                break;

            case MICRON:
                spinner.setModel(new SpinnerNumberModel(value, 0d, Double.MAX_VALUE, 0.01d));
                // we don't want the model to affect
                ((DefaultEditor) spinner.getEditor()).getTextField().setColumns(1);
                break;
        }
    }

    void setNewWidth(final int value) {
        setSpinnerSizeValue(widthSpinner, pixelXToUnit(value, sequence.getSizeX(), getSizeUnit()));
    }

    void setNewHeight(final int value) {
        setSpinnerSizeValue(heightSpinner, pixelYToUnit(value, sequence.getSizeY(), getSizeUnit()));
    }

    public int getMaxSizeX() {
        return Math.max(getNewWidth(), sequence.getSizeX());
    }

    public int getMaxSizeY() {
        return Math.max(getNewHeight(), sequence.getSizeY());
    }

    public abstract FilterType getFilterType();

    public abstract boolean getResizeContent();

    public abstract int getXAlign();

    public abstract int getYAlign();
}
