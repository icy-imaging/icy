/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
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
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import icy.gui.component.NumberTextField;
import icy.preferences.MagicWandPreferences;
import plugins.kernel.roi.tool.MagicWand.MagicWandConnectivity;
import plugins.kernel.roi.tool.MagicWand.MagicWandGradientToleranceMode;

/**
 * @author Stephane
 */
public class MagicWandPreferencePanel extends PreferencePanel
{
    public static final String NODE_NAME = "Magic Wand";

    /**
     * gui
     */
    private NumberTextField gradientToleranceValueField;
    private JComboBox connectivityField;
    private JComboBox gradientToleranceModeField;

    /**
     * @param parent
     */
    MagicWandPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PreferenceFrame.NODE_NAME);

        initGui();
        load();
    }

    private void initGui()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {160, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        mainPanel.setLayout(gridBagLayout);

        JLabel lblNewLabel = new JLabel("Connectivity");
        lblNewLabel.setToolTipText("Wagic Wand pixel connectivity method");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        mainPanel.add(lblNewLabel, gbc_lblNewLabel);

        connectivityField = new JComboBox();
        connectivityField.setToolTipText("Select connectivy method (default = 8 points)");
        connectivityField.setModel(new DefaultComboBoxModel(MagicWandConnectivity.values()));
        connectivityField.setSelectedIndex(1);
        GridBagConstraints gbc_connectivityField = new GridBagConstraints();
        gbc_connectivityField.fill = GridBagConstraints.HORIZONTAL;
        gbc_connectivityField.insets = new Insets(0, 0, 5, 0);
        gbc_connectivityField.gridx = 1;
        gbc_connectivityField.gridy = 0;
        mainPanel.add(connectivityField, gbc_connectivityField);

        JLabel lblNewLabel_1 = new JLabel("Gradient tolerance");
        lblNewLabel_1.setToolTipText("Gradient tolerance method to use (disabled by default)");
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 1;
        mainPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        gradientToleranceModeField = new JComboBox();
        gradientToleranceModeField.setToolTipText("Gradient tolerance mode");
        gradientToleranceModeField.setModel(new DefaultComboBoxModel(MagicWandGradientToleranceMode.values()));
        gradientToleranceModeField.setSelectedIndex(0);
        GridBagConstraints gbc_gradientToleranceModeField = new GridBagConstraints();
        gbc_gradientToleranceModeField.insets = new Insets(0, 0, 5, 0);
        gbc_gradientToleranceModeField.fill = GridBagConstraints.HORIZONTAL;
        gbc_gradientToleranceModeField.gridx = 1;
        gbc_gradientToleranceModeField.gridy = 1;
        mainPanel.add(gradientToleranceModeField, gbc_gradientToleranceModeField);

        JLabel lblNewLabel_2 = new JLabel("Gradient tolerance value");
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 2;
        mainPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        gradientToleranceValueField = new NumberTextField();
        gradientToleranceValueField.setText("20");
        GridBagConstraints gbc_gradientToleranceValueField = new GridBagConstraints();
        gbc_gradientToleranceValueField.fill = GridBagConstraints.HORIZONTAL;
        gbc_gradientToleranceValueField.gridx = 1;
        gbc_gradientToleranceValueField.gridy = 2;
        mainPanel.add(gradientToleranceValueField, gbc_gradientToleranceValueField);
    }

    @Override
    protected void load()
    {
        gradientToleranceModeField.setSelectedItem(MagicWandPreferences.getGradientToleranceMode());
        gradientToleranceValueField.setNumericValue(MagicWandPreferences.getGradientToleranceValue());
        connectivityField.setSelectedItem(MagicWandPreferences.getConnectivity());
    }

    @Override
    protected void save()
    {
        MagicWandPreferences.setGradientToleranceMode(
                (MagicWandGradientToleranceMode) gradientToleranceModeField.getSelectedItem());
        MagicWandPreferences.setGradientToleranceValue(gradientToleranceValueField.getNumericValue());
        MagicWandPreferences.setConnectivity((MagicWandConnectivity) connectivityField.getSelectedItem());
    }
}
