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

package plugins.kernel.canvas;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.NumberTextField;
import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import icy.gui.component.button.IcyToggleButton;
import icy.resource.ResourceUtil;
import icy.resource.icon.SVGIcon;
import icy.vtk.VtkImageVolume;
import icy.vtk.VtkImageVolume.VtkVolumeBlendType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.EventListener;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class VtkSettingPanel extends JPanel implements ActionListener, TextChangeListener, ColorChangeListener {
    public static final String PROPERTY_BG_COLOR = "renderBGColor";
    public static final String PROPERTY_MAPPER = "volumeMapper";
    public static final String PROPERTY_BLENDING = "volumeBlending";
    public static final String PROPERTY_SAMPLE = "volumeSample";
    public static final String PROPERTY_INTERPOLATION = "volumeInterpolation";
    public static final String PROPERTY_SHADING = "shading";
    public static final String PROPERTY_AMBIENT = "volumeAmbient";
    public static final String PROPERTY_DIFFUSE = "volumeDiffuse";
    public static final String PROPERTY_SPECULAR = "volumeSpecular";

    /**
     * GUI
     */
    private ColorChooserButton bgColorButton;
    private JCheckBox gpuMapperCheckBox;
    private JComboBox<VtkVolumeBlendType> volumeBlendingComboBox;
    private JComboBox<String> volumeSampleComboBox;
    private JComboBox<String> volumeInterpolationComboBox;
    private IcyToggleButton shadingButton;
    private NumberTextField volumeAmbientField;
    private NumberTextField volumeSpecularField;
    private NumberTextField volumeDiffuseField;

    /**
     * Create the panel.
     */
    public VtkSettingPanel() {
        super();

        initialize();

        updateState();

        bgColorButton.addColorChangeListener(this);
        gpuMapperCheckBox.addActionListener(this);
        volumeBlendingComboBox.addActionListener(this);
        volumeInterpolationComboBox.addActionListener(this);
        volumeSampleComboBox.addActionListener(this);
        shadingButton.addActionListener(this);

        volumeAmbientField.addTextChangeListener(this);
        volumeDiffuseField.addTextChangeListener(this);
        volumeSpecularField.addTextChangeListener(this);
    }

    protected void initialize() {
        setBorder(new EmptyBorder(2, 0, 2, 0));
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblBackground = new JLabel("Background color ");
        lblBackground.setToolTipText("Change background color");
        final GridBagConstraints gbc_lblBackground = new GridBagConstraints();
        gbc_lblBackground.anchor = GridBagConstraints.WEST;
        gbc_lblBackground.insets = new Insets(0, 0, 5, 5);
        gbc_lblBackground.gridx = 0;
        gbc_lblBackground.gridy = 0;
        add(lblBackground, gbc_lblBackground);

        bgColorButton = new ColorChooserButton();
        bgColorButton.setToolTipText("Change background color");
        final GridBagConstraints gbc_bgColorButton = new GridBagConstraints();
        gbc_bgColorButton.anchor = GridBagConstraints.WEST;
        gbc_bgColorButton.insets = new Insets(0, 0, 5, 5);
        gbc_bgColorButton.gridx = 1;
        gbc_bgColorButton.gridy = 0;
        add(bgColorButton, gbc_bgColorButton);

        gpuMapperCheckBox = new JCheckBox("New check box");
        gpuMapperCheckBox.setFocusable(false);
        gpuMapperCheckBox.setIconTextGap(8);
        gpuMapperCheckBox.setText("GPU rendering");
        gpuMapperCheckBox.setToolTipText("Enable GPU volume rendering");
        final GridBagConstraints gbc_gpuMapperCheckBox = new GridBagConstraints();
        gbc_gpuMapperCheckBox.anchor = GridBagConstraints.EAST;
        gbc_gpuMapperCheckBox.gridwidth = 2;
        gbc_gpuMapperCheckBox.insets = new Insets(0, 0, 5, 0);
        gbc_gpuMapperCheckBox.gridx = 2;
        gbc_gpuMapperCheckBox.gridy = 0;
        add(gpuMapperCheckBox, gbc_gpuMapperCheckBox);

        final JLabel lblInterpolation = new JLabel("Interpolation  ");
        lblInterpolation.setToolTipText("Select volume rendering interpolation method");
        final GridBagConstraints gbc_lblInterpolation = new GridBagConstraints();
        gbc_lblInterpolation.anchor = GridBagConstraints.WEST;
        gbc_lblInterpolation.insets = new Insets(0, 0, 5, 5);
        gbc_lblInterpolation.gridx = 0;
        gbc_lblInterpolation.gridy = 1;
        add(lblInterpolation, gbc_lblInterpolation);

        volumeInterpolationComboBox = new JComboBox<>();
        volumeInterpolationComboBox.setToolTipText("Select volume rendering interpolation method");
        volumeInterpolationComboBox.setMaximumRowCount(7);
        volumeInterpolationComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"Nearest (Fast)", "Linear", "Cubic (Slow)"}));
        volumeInterpolationComboBox.setSelectedIndex(1);
        final GridBagConstraints gbc_volumeInterpolationComboBox = new GridBagConstraints();
        gbc_volumeInterpolationComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeInterpolationComboBox.gridwidth = 3;
        gbc_volumeInterpolationComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeInterpolationComboBox.gridx = 1;
        gbc_volumeInterpolationComboBox.gridy = 1;
        add(volumeInterpolationComboBox, gbc_volumeInterpolationComboBox);

        final JLabel lblBlending = new JLabel("Blending");
        lblBlending.setToolTipText("Select volume rendering blending method");
        final GridBagConstraints gbc_lblBlending = new GridBagConstraints();
        gbc_lblBlending.anchor = GridBagConstraints.WEST;
        gbc_lblBlending.insets = new Insets(0, 0, 5, 5);
        gbc_lblBlending.gridx = 0;
        gbc_lblBlending.gridy = 2;
        add(lblBlending, gbc_lblBlending);

        volumeBlendingComboBox = new JComboBox<>();
        volumeBlendingComboBox.setToolTipText("Select volume rendering blending method");
        volumeBlendingComboBox.setModel(new DefaultComboBoxModel<>(VtkVolumeBlendType.values()));
        final GridBagConstraints gbc_volumeBlendingComboBox = new GridBagConstraints();
        gbc_volumeBlendingComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeBlendingComboBox.gridwidth = 3;
        gbc_volumeBlendingComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeBlendingComboBox.gridx = 1;
        gbc_volumeBlendingComboBox.gridy = 2;
        add(volumeBlendingComboBox, gbc_volumeBlendingComboBox);

        final JLabel lblSample = new JLabel("Sample");
        lblSample.setToolTipText("Set volume sample resolution (raycaster mapper only)");
        final GridBagConstraints gbc_lblSample = new GridBagConstraints();
        gbc_lblSample.anchor = GridBagConstraints.WEST;
        gbc_lblSample.insets = new Insets(0, 0, 5, 5);
        gbc_lblSample.gridx = 0;
        gbc_lblSample.gridy = 3;
        add(lblSample, gbc_lblSample);
        volumeSampleComboBox = new JComboBox<>();
        volumeSampleComboBox.setToolTipText("Use low value for fine (but slow) rendering and high value for fast (but coarse) rendering");
        volumeSampleComboBox.setMaximumRowCount(11);
        volumeSampleComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"Auto", "1 (Slow)", "2", "3", "4", "5", "6", "7", "8", "9", "10 (Fast)"}));
        volumeSampleComboBox.setSelectedIndex(0);
        final GridBagConstraints gbc_volumeSampleComboBox = new GridBagConstraints();
        gbc_volumeSampleComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeSampleComboBox.gridwidth = 3;
        gbc_volumeSampleComboBox.insets = new Insets(0, 0, 5, 0);
        gbc_volumeSampleComboBox.gridx = 1;
        gbc_volumeSampleComboBox.gridy = 3;
        add(volumeSampleComboBox, gbc_volumeSampleComboBox);

        shadingButton = new IcyToggleButton(SVGIcon.SHADING);
        shadingButton.setIconTextGap(8);
        shadingButton.setText("Shading");
        shadingButton.setFocusable(false);
        shadingButton.setToolTipText("Enable volume shading");
        final GridBagConstraints gbc_shadingBtn = new GridBagConstraints();
        gbc_shadingBtn.anchor = GridBagConstraints.WEST;
        gbc_shadingBtn.insets = new Insets(0, 0, 0, 5);
        gbc_shadingBtn.gridx = 0;
        gbc_shadingBtn.gridy = 4;
        add(shadingButton, gbc_shadingBtn);

        volumeAmbientField = new NumberTextField();
        volumeAmbientField.setToolTipText("Ambient lighting coefficient");
        final GridBagConstraints gbc_volumeAmbientField = new GridBagConstraints();
        gbc_volumeAmbientField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeAmbientField.insets = new Insets(0, 0, 0, 5);
        gbc_volumeAmbientField.gridx = 1;
        gbc_volumeAmbientField.gridy = 4;
        add(volumeAmbientField, gbc_volumeAmbientField);
        volumeAmbientField.setColumns(3);

        volumeDiffuseField = new NumberTextField();
        volumeDiffuseField.setToolTipText("Diffuse lighting coefficient");
        final GridBagConstraints gbc_volumeDiffuseField = new GridBagConstraints();
        gbc_volumeDiffuseField.insets = new Insets(0, 0, 0, 5);
        gbc_volumeDiffuseField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeDiffuseField.gridx = 2;
        gbc_volumeDiffuseField.gridy = 4;
        add(volumeDiffuseField, gbc_volumeDiffuseField);
        volumeDiffuseField.setColumns(3);

        volumeSpecularField = new NumberTextField();
        volumeSpecularField.setToolTipText("Specular lighting coefficient");
        final GridBagConstraints gbc_volumeSpecularField = new GridBagConstraints();
        gbc_volumeSpecularField.fill = GridBagConstraints.HORIZONTAL;
        gbc_volumeSpecularField.gridx = 3;
        gbc_volumeSpecularField.gridy = 4;
        add(volumeSpecularField, gbc_volumeSpecularField);
        volumeSpecularField.setColumns(3);
    }

    protected void updateState() {
        // if (isBoundingBoxVisible())
        // {
        // boundingBoxGridCheckBox.setEnabled(true);
        // boundingBoxRulerCheckBox.setEnabled(true);
        // }
        // else
        // {
        // boundingBoxGridCheckBox.setEnabled(false);
        // boundingBoxRulerCheckBox.setEnabled(false);
        // }

        // switch (getVolumeMapperType())
        // {
        // case RAYCAST_CPU_FIXEDPOINT:
        // case RAYCAST_GPU_OPENGL:
        // volumeSampleComboBox.setEnabled(true);
        // volumeBlendingComboBox.setEnabled(true);
        // break;
        // case TEXTURE2D_OPENGL:
        // case TEXTURE3D_OPENGL:
        // volumeSampleComboBox.setEnabled(false);
        // volumeBlendingComboBox.setEnabled(false);
        // break;
        // }
    }

    public Color getBackgroundColor() {
        return bgColorButton.getColor();
    }

    public void setBackgroundColor(final Color value) {
        bgColorButton.setColor(value);
    }

    public boolean getGPURendering() {
        return gpuMapperCheckBox.isSelected();
    }

    public void setGPURendering(final boolean value) {
        gpuMapperCheckBox.setSelected(value);
    }

    public int getVolumeInterpolation() {
        return volumeInterpolationComboBox.getSelectedIndex();
    }

    public void setVolumeInterpolation(final int value) {
        volumeInterpolationComboBox.setSelectedIndex(value);
    }

    public VtkVolumeBlendType getVolumeBlendingMode() {
        if (volumeBlendingComboBox.getSelectedIndex() == -1)
            return null;

        return (VtkVolumeBlendType) volumeBlendingComboBox.getSelectedItem();
    }

    public void setVolumeBlendingMode(final VtkVolumeBlendType value) {
        volumeBlendingComboBox.setSelectedItem(value);
    }

    public int getVolumeSample() {
        return volumeSampleComboBox.getSelectedIndex();
    }

    public void setVolumeSample(final int value) {
        volumeSampleComboBox.setSelectedIndex(value);
    }

    public double getVolumeAmbient() {
        return volumeAmbientField.getNumericValue();
    }

    public void setVolumeAmbient(final double value) {
        volumeAmbientField.setNumericValue(value);
    }

    public double getVolumeDiffuse() {
        return volumeDiffuseField.getNumericValue();
    }

    public void setVolumeDiffuse(final double value) {
        volumeDiffuseField.setNumericValue(value);
    }

    public double getVolumeSpecular() {
        return volumeSpecularField.getNumericValue();
    }

    public void setVolumeSpecular(final double value) {
        volumeSpecularField.setNumericValue(value);
    }

    /**
     * @see VtkImageVolume#getShade()
     */
    public boolean getVolumeShading() {
        return shadingButton.isSelected();
    }

    /**
     * @see VtkImageVolume#setShade(boolean)
     */
    public void setVolumeShading(final boolean value) {
        if (shadingButton.isSelected() != value)
            shadingButton.doClick();
    }

    /**
     * Add a SettingChange listener
     */
    public void addSettingChangeListener(final SettingChangeListener listener) {
        listenerList.add(SettingChangeListener.class, listener);
    }

    /**
     * Remove a SettingChange listener
     */
    public void removeSettingChangeListener(final SettingChangeListener listener) {
        listenerList.remove(SettingChangeListener.class, listener);
    }

    public void fireSettingChange(final Object source, final String propertyName, final Object oldValue, final Object newValue) {
        if ((oldValue != null) && oldValue.equals(newValue))
            return;

        final PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
        final SettingChangeListener[] listeners = getListeners(SettingChangeListener.class);

        for (final SettingChangeListener listener : listeners)
            listener.settingChange(event);
    }

    @Override
    public void colorChanged(final ColorChooserButton source) {
        if (source == bgColorButton)
            fireSettingChange(source, PROPERTY_BG_COLOR, null, source.getColor());
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object source = e.getSource();

        if (source == gpuMapperCheckBox) {
            fireSettingChange(source, PROPERTY_MAPPER, Boolean.valueOf(!gpuMapperCheckBox.isSelected()), Boolean.valueOf(gpuMapperCheckBox.isSelected()));

            // GPU rendering ? --> force volume sample to 1 (fine rendering, 'auto' is sometime buggy with GPU)
            if (gpuMapperCheckBox.isSelected())
                setVolumeSample(1);
                // CPU rendering ? --> force 'auto' if slow mode was enabled
            else if (getVolumeSample() == 1)
                setVolumeSample(0);
        }
        else if (source == volumeBlendingComboBox)
            fireSettingChange(source, PROPERTY_BLENDING, null, volumeBlendingComboBox.getSelectedItem());
        else if (source == volumeSampleComboBox)
            fireSettingChange(source, PROPERTY_SAMPLE, Integer.valueOf(-1), Integer.valueOf(volumeSampleComboBox.getSelectedIndex()));
        else if (source == volumeInterpolationComboBox)
            fireSettingChange(source, PROPERTY_INTERPOLATION, Integer.valueOf(-1), Integer.valueOf(volumeInterpolationComboBox.getSelectedIndex()));
        else if (source == shadingButton)
            fireSettingChange(source, PROPERTY_SHADING, Integer.valueOf(-1), Boolean.valueOf(shadingButton.isSelected()));

        updateState();
    }

    @Override
    public void textChanged(final IcyTextField source, final boolean validate) {
        if (!validate)
            return;

        if (source == volumeAmbientField)
            fireSettingChange(source, PROPERTY_AMBIENT, Double.valueOf(-1d), Double.valueOf(volumeAmbientField.getNumericValue()));
        else if (source == volumeDiffuseField)
            fireSettingChange(source, PROPERTY_DIFFUSE, Double.valueOf(-1d), Double.valueOf(volumeDiffuseField.getNumericValue()));
        else if (source == volumeSpecularField)
            fireSettingChange(source, PROPERTY_SPECULAR, Double.valueOf(-1d), Double.valueOf(volumeSpecularField.getNumericValue()));

        updateState();
    }

    public interface SettingChangeListener extends EventListener {
        void settingChange(PropertyChangeEvent evt);
    }
}
