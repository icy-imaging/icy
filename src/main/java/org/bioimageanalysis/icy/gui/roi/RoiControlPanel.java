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

package org.bioimageanalysis.icy.gui.roi;

import org.bioimageanalysis.icy.gui.clipboard.Clipboard;
import org.bioimageanalysis.icy.gui.clipboard.Clipboard.ClipboardListener;
import org.bioimageanalysis.icy.gui.component.panel.AbstractRoisPanel;
import org.bioimageanalysis.icy.gui.component.field.IcyTextField;
import org.bioimageanalysis.icy.gui.component.field.IcyTextField.TextChangeListener;
import org.bioimageanalysis.icy.gui.component.spinner.SpecialValueSpinner;
import org.bioimageanalysis.icy.gui.component.button.ColorChooserButton;
import org.bioimageanalysis.icy.gui.component.button.ColorChooserButton.ColorChangeListener;
import org.bioimageanalysis.icy.gui.component.button.IcyToggleButton;
import org.bioimageanalysis.icy.gui.component.model.SpecialValueSpinnerModel;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIconPack;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.ROIEvent;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.model.roi.edit.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class RoiControlPanel extends JPanel implements ColorChangeListener, TextChangeListener, ClipboardListener, ChangeListener, ActionListener {
    // GUI
    JLabel posCFieldLabel;
    JLabel posTFieldLabel;
    JLabel posZFieldLabel;
    JLabel posZSpinnerLabel;
    JLabel posTSpinnerLabel;
    JLabel posCSpinnerLabel;
    IcyTextField posXField;
    IcyTextField posYField;
    IcyTextField posTField;
    IcyTextField posZField;
    IcyTextField sizeXField;
    IcyTextField sizeZField;
    IcyTextField sizeYField;
    IcyTextField sizeTField;
    IcyTextField posCField;
    IcyTextField sizeCField;
    SpecialValueSpinner posZSpinner;
    SpecialValueSpinner posTSpinner;
    SpecialValueSpinner posCSpinner;
    ColorChooserButton colorButton;
    JSlider alphaSlider;
    JSpinner strokeSpinner;
    IcyToggleButton readOnlyToggle;
    JCheckBox displayNameCheckBox;
    JButton setAsDefaultBtn;

    // internals
    private final AbstractRoisPanel roisPanel;
    final Semaphore modifyingRoi;
    private final List<Reference<ROI>> modifiedRois;
    final Runnable roiActionsRefresher;
    final Runnable roiPropertiesRefresher;

    public RoiControlPanel(final AbstractRoisPanel roisPanel) {
        super();

        this.roisPanel = roisPanel;

        modifyingRoi = new Semaphore(1);
        modifiedRois = new ArrayList<>();

        roiActionsRefresher = this::refreshROIActionsInternal;
        roiPropertiesRefresher = this::refreshROIPropertiesInternal;

        initialize();

        colorButton.addColorChangeListener(this);
        strokeSpinner.addChangeListener(this);
        alphaSlider.addChangeListener(this);
        readOnlyToggle.addActionListener(this);
        displayNameCheckBox.addActionListener(this);

        posXField.addTextChangeListener(this);
        posYField.addTextChangeListener(this);
        posZField.addTextChangeListener(this);
        posZSpinner.addChangeListener(this);
        posTField.addTextChangeListener(this);
        posTSpinner.addChangeListener(this);
        posCField.addTextChangeListener(this);
        posCSpinner.addChangeListener(this);
        sizeXField.addTextChangeListener(this);
        sizeYField.addTextChangeListener(this);
        sizeZField.addTextChangeListener(this);
        sizeTField.addTextChangeListener(this);
        sizeCField.addTextChangeListener(this);

        setAsDefaultBtn.addActionListener(this);

        Clipboard.addListener(this);

        refreshROIActionsInternal();
        refreshROIPropertiesInternal();
    }

    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        final JPanel actionPanel = new JPanel();
        actionPanel.setBorder(
                new TitledBorder(
                        null,
                        "Properties",
                        TitledBorder.LEADING,
                        TitledBorder.TOP,
                        null
                )
        );
        add(actionPanel);
        final GridBagLayout gbl_actionPanel = new GridBagLayout();
        gbl_actionPanel.columnWidths = new int[]{0, 0, 0, 60, 0, 0, 0};
        gbl_actionPanel.rowHeights = new int[]{0, 0, 0};
        gbl_actionPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_actionPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        actionPanel.setLayout(gbl_actionPanel);

        final JLabel lblColor = new JLabel("Color");
        final GridBagConstraints gbc_lblColor = new GridBagConstraints();
        gbc_lblColor.anchor = GridBagConstraints.WEST;
        gbc_lblColor.insets = new Insets(0, 0, 5, 5);
        gbc_lblColor.gridx = 0;
        gbc_lblColor.gridy = 0;
        actionPanel.add(lblColor, gbc_lblColor);

        colorButton = new ColorChooserButton();
        final GridBagConstraints gbc_colorButton = new GridBagConstraints();
        gbc_colorButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_colorButton.insets = new Insets(0, 0, 5, 5);
        gbc_colorButton.gridx = 1;
        gbc_colorButton.gridy = 0;
        actionPanel.add(colorButton, gbc_colorButton);
        colorButton.setToolTipText("ROI color");

        final JLabel lblContentOpacity = new JLabel("Opacity");
        final GridBagConstraints gbc_lblContentOpacity = new GridBagConstraints();
        gbc_lblContentOpacity.insets = new Insets(0, 0, 5, 5);
        gbc_lblContentOpacity.gridx = 2;
        gbc_lblContentOpacity.gridy = 0;
        actionPanel.add(lblContentOpacity, gbc_lblContentOpacity);

        alphaSlider = new JSlider();
        alphaSlider.setFocusable(false);
        final GridBagConstraints gbc_alphaSlider = new GridBagConstraints();
        gbc_alphaSlider.gridwidth = 2;
        gbc_alphaSlider.fill = GridBagConstraints.HORIZONTAL;
        gbc_alphaSlider.insets = new Insets(0, 0, 5, 5);
        gbc_alphaSlider.gridx = 3;
        gbc_alphaSlider.gridy = 0;
        actionPanel.add(alphaSlider, gbc_alphaSlider);
        alphaSlider.setPreferredSize(new Dimension(80, 20));
        alphaSlider.setMaximumSize(new Dimension(32767, 20));
        alphaSlider.setMinimumSize(new Dimension(36, 20));
        alphaSlider.setToolTipText("ROI content opacity");

        readOnlyToggle = new IcyToggleButton(new SVGIconPack(SVGIcon.LOCK_OPEN, SVGIcon.LOCK));
        readOnlyToggle.setSelected(true);
        readOnlyToggle.setBorderPainted(false);
        readOnlyToggle.setFocusPainted(false);
        readOnlyToggle.setIconTextGap(0);
        readOnlyToggle.setMaximumSize(new Dimension(29, 29));
        readOnlyToggle.setMinimumSize(new Dimension(29, 29));
        readOnlyToggle.setPreferredSize(new Dimension(29, 29));
        readOnlyToggle.setToolTipText("Enable/Disable ROI edition");
        final GridBagConstraints gbc_readOnlyToggle = new GridBagConstraints();
        gbc_readOnlyToggle.insets = new Insets(0, 0, 5, 0);
        gbc_readOnlyToggle.gridwidth = 1;
        gbc_readOnlyToggle.gridx = 5;
        gbc_readOnlyToggle.gridy = 0;
        actionPanel.add(readOnlyToggle, gbc_readOnlyToggle);

        final JLabel lblNewLabel = new JLabel("Stroke");
        final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 1;
        actionPanel.add(lblNewLabel, gbc_lblNewLabel);

        strokeSpinner = new JSpinner();
        strokeSpinner.setToolTipText("ROI stroke size (for visualization only)");
        strokeSpinner.setModel(new SpinnerNumberModel(1.0, 1.0, 9.0, 1.0));
        final GridBagConstraints gbc_strokeSpinner = new GridBagConstraints();
        gbc_strokeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_strokeSpinner.insets = new Insets(0, 0, 0, 5);
        gbc_strokeSpinner.gridx = 1;
        gbc_strokeSpinner.gridy = 1;
        actionPanel.add(strokeSpinner, gbc_strokeSpinner);

        displayNameCheckBox = new JCheckBox("Show name");
        displayNameCheckBox.setToolTipText("Show the ROI name");
        displayNameCheckBox.setMargin(new Insets(2, 0, 2, 2));
        displayNameCheckBox.setIconTextGap(10);
        displayNameCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        final GridBagConstraints gbc_displayNameCheckBox = new GridBagConstraints();
        gbc_displayNameCheckBox.anchor = GridBagConstraints.WEST;
        gbc_displayNameCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_displayNameCheckBox.gridwidth = 2;
        gbc_displayNameCheckBox.gridx = 2;
        gbc_displayNameCheckBox.gridy = 1;
        actionPanel.add(displayNameCheckBox, gbc_displayNameCheckBox);

        setAsDefaultBtn = new JButton("As default");
        setAsDefaultBtn.setEnabled(false);
        setAsDefaultBtn.setMargin(new Insets(2, 2, 2, 2));
        setAsDefaultBtn.setIconTextGap(0);
        setAsDefaultBtn.setToolTipText("Set the current color, opacity, stroke and show name values as the default settings");
        final GridBagConstraints gbc_setAsDefaultBtn = new GridBagConstraints();
        gbc_setAsDefaultBtn.fill = GridBagConstraints.HORIZONTAL;
        gbc_setAsDefaultBtn.gridwidth = 2;
        gbc_setAsDefaultBtn.insets = new Insets(0, 0, 0, 5);
        gbc_setAsDefaultBtn.gridx = 4;
        gbc_setAsDefaultBtn.gridy = 1;
        actionPanel.add(setAsDefaultBtn, gbc_setAsDefaultBtn);

        final JPanel positionAndSizePanel = new JPanel();
        add(positionAndSizePanel);
        final GridBagLayout gbl_positionAndSizePanel = new GridBagLayout();
        gbl_positionAndSizePanel.columnWidths = new int[]{0, 0, 0};
        gbl_positionAndSizePanel.rowHeights = new int[]{0, 0};
        gbl_positionAndSizePanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        gbl_positionAndSizePanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        positionAndSizePanel.setLayout(gbl_positionAndSizePanel);

        final JPanel positionPanel = new JPanel();
        positionPanel.setBorder(
                new TitledBorder(
                        null,
                        "Position",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        null
                )
        );
        final GridBagConstraints gbc_positionPanel = new GridBagConstraints();
        gbc_positionPanel.anchor = GridBagConstraints.NORTH;
        gbc_positionPanel.insets = new Insets(0, 0, 0, 5);
        gbc_positionPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_positionPanel.gridx = 0;
        gbc_positionPanel.gridy = 0;
        positionAndSizePanel.add(positionPanel, gbc_positionPanel);
        final GridBagLayout gbl_positionPanel = new GridBagLayout();
        gbl_positionPanel.columnWidths = new int[]{20, 0, 0};
        gbl_positionPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_positionPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_positionPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        positionPanel.setLayout(gbl_positionPanel);

        final JLabel lblX = new JLabel("X");
        final GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.gridx = 0;
        gbc_lblX.gridy = 0;
        positionPanel.add(lblX, gbc_lblX);

        posXField = new IcyTextField();
        final GridBagConstraints gbc_posXField = new GridBagConstraints();
        gbc_posXField.insets = new Insets(0, 0, 5, 0);
        gbc_posXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posXField.gridx = 1;
        gbc_posXField.gridy = 0;
        positionPanel.add(posXField, gbc_posXField);
        posXField.setToolTipText("X position of the ROI");
        posXField.setColumns(8);

        final JLabel lblY = new JLabel("Y");
        final GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 0;
        gbc_lblY.gridy = 1;
        positionPanel.add(lblY, gbc_lblY);

        posYField = new IcyTextField();
        final GridBagConstraints gbc_posYField = new GridBagConstraints();
        gbc_posYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posYField.insets = new Insets(0, 0, 5, 0);
        gbc_posYField.gridx = 1;
        gbc_posYField.gridy = 1;
        positionPanel.add(posYField, gbc_posYField);
        posYField.setToolTipText("Y position of the ROI");
        posYField.setColumns(8);

        posZFieldLabel = new JLabel("Z");
        final GridBagConstraints gbc_posZFieldLabel = new GridBagConstraints();
        gbc_posZFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posZFieldLabel.gridx = 0;
        gbc_posZFieldLabel.gridy = 2;
        positionPanel.add(posZFieldLabel, gbc_posZFieldLabel);

        posZField = new IcyTextField();
        final GridBagConstraints gbc_posZField = new GridBagConstraints();
        gbc_posZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posZField.insets = new Insets(0, 0, 5, 0);
        gbc_posZField.gridx = 1;
        gbc_posZField.gridy = 2;
        positionPanel.add(posZField, gbc_posZField);
        posZField.setVisible(false);
        posZField.setToolTipText("Z position of the ROI");
        posZField.setColumns(8);

        posZSpinnerLabel = new JLabel("Z");
        final GridBagConstraints gbc_posZSpinnerLabel = new GridBagConstraints();
        gbc_posZSpinnerLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posZSpinnerLabel.gridx = 0;
        gbc_posZSpinnerLabel.gridy = 3;
        positionPanel.add(posZSpinnerLabel, gbc_posZSpinnerLabel);

        posZSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        final GridBagConstraints gbc_posZSpinner = new GridBagConstraints();
        gbc_posZSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_posZSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_posZSpinner.gridx = 1;
        gbc_posZSpinner.gridy = 3;
        positionPanel.add(posZSpinner, gbc_posZSpinner);
        posZSpinner.setToolTipText("Attach the ROI to a specific Z slice (set to -1 for ALL)");

        posTFieldLabel = new JLabel("T");
        final GridBagConstraints gbc_posTFieldLabel = new GridBagConstraints();
        gbc_posTFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posTFieldLabel.gridx = 0;
        gbc_posTFieldLabel.gridy = 4;
        positionPanel.add(posTFieldLabel, gbc_posTFieldLabel);

        posTField = new IcyTextField();
        final GridBagConstraints gbc_posTField = new GridBagConstraints();
        gbc_posTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posTField.insets = new Insets(0, 0, 5, 0);
        gbc_posTField.gridx = 1;
        gbc_posTField.gridy = 4;
        positionPanel.add(posTField, gbc_posTField);
        posTField.setVisible(false);
        posTField.setToolTipText("T position of the ROI");
        posTField.setColumns(8);

        posTSpinnerLabel = new JLabel("T");
        final GridBagConstraints gbc_posTSpinnerLabel = new GridBagConstraints();
        gbc_posTSpinnerLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posTSpinnerLabel.gridx = 0;
        gbc_posTSpinnerLabel.gridy = 5;
        positionPanel.add(posTSpinnerLabel, gbc_posTSpinnerLabel);

        posTSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        final GridBagConstraints gbc_posTSpinner = new GridBagConstraints();
        gbc_posTSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_posTSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_posTSpinner.gridx = 1;
        gbc_posTSpinner.gridy = 5;
        positionPanel.add(posTSpinner, gbc_posTSpinner);
        posTSpinner.setToolTipText("Attach the ROI to a specific T frame (set to -1 for ALL)");

        posCFieldLabel = new JLabel("C");
        final GridBagConstraints gbc_posCFieldLabel = new GridBagConstraints();
        gbc_posCFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_posCFieldLabel.gridx = 0;
        gbc_posCFieldLabel.gridy = 6;
        positionPanel.add(posCFieldLabel, gbc_posCFieldLabel);

        posCField = new IcyTextField();
        final GridBagConstraints gbc_posCField = new GridBagConstraints();
        gbc_posCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_posCField.insets = new Insets(0, 0, 5, 0);
        gbc_posCField.gridx = 1;
        gbc_posCField.gridy = 6;
        positionPanel.add(posCField, gbc_posCField);
        posCField.setVisible(false);
        posCField.setToolTipText("C position of the ROI");
        posCField.setColumns(8);

        posCSpinnerLabel = new JLabel("C");
        final GridBagConstraints gbc_posCSpinnerLabel = new GridBagConstraints();
        gbc_posCSpinnerLabel.insets = new Insets(0, 0, 0, 5);
        gbc_posCSpinnerLabel.gridx = 0;
        gbc_posCSpinnerLabel.gridy = 7;
        positionPanel.add(posCSpinnerLabel, gbc_posCSpinnerLabel);

        posCSpinner = new SpecialValueSpinner(new SpecialValueSpinnerModel(-1, -1, 0, 1, -1, "ALL"));
        final GridBagConstraints gbc_posCSpinner = new GridBagConstraints();
        gbc_posCSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_posCSpinner.gridx = 1;
        gbc_posCSpinner.gridy = 7;
        positionPanel.add(posCSpinner, gbc_posCSpinner);
        posCSpinner.setToolTipText("Attach the ROI to a specific C channel (set to -1 for ALL)");

        final JPanel sizePanel = new JPanel();
        sizePanel.setBorder(
                new TitledBorder(
                        null,
                        "Dimension",
                        TitledBorder.LEADING,
                        TitledBorder.TOP,
                        null
                )
        );
        final GridBagConstraints gbc_sizePanel = new GridBagConstraints();
        gbc_sizePanel.anchor = GridBagConstraints.NORTH;
        gbc_sizePanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizePanel.gridx = 1;
        gbc_sizePanel.gridy = 0;
        positionAndSizePanel.add(sizePanel, gbc_sizePanel);

        final GridBagLayout gbl_sizePanel = new GridBagLayout();
        gbl_sizePanel.columnWidths = new int[]{20, 0, 0};
        gbl_sizePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
        gbl_sizePanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_sizePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        sizePanel.setLayout(gbl_sizePanel);

        final JLabel lblNewLabel_2 = new JLabel("X");
        final GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 0;
        sizePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        sizeXField = new IcyTextField();
        final GridBagConstraints gbc_sizeXField = new GridBagConstraints();
        gbc_sizeXField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeXField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeXField.gridx = 1;
        gbc_sizeXField.gridy = 0;
        sizePanel.add(sizeXField, gbc_sizeXField);
        sizeXField.setToolTipText("Size of dimension X for the ROI");
        sizeXField.setColumns(8);

        final JLabel lblY_1 = new JLabel("Y");
        final GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.gridx = 0;
        gbc_lblY_1.gridy = 1;
        sizePanel.add(lblY_1, gbc_lblY_1);

        sizeYField = new IcyTextField();
        final GridBagConstraints gbc_sizeYField = new GridBagConstraints();
        gbc_sizeYField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeYField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeYField.gridx = 1;
        gbc_sizeYField.gridy = 1;
        sizePanel.add(sizeYField, gbc_sizeYField);
        sizeYField.setToolTipText("Size of dimension Y for the ROI");
        sizeYField.setColumns(8);

        final JLabel sizeZFieldLabel = new JLabel("Z");
        final GridBagConstraints gbc_sizeZFieldLabel = new GridBagConstraints();
        gbc_sizeZFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_sizeZFieldLabel.gridx = 0;
        gbc_sizeZFieldLabel.gridy = 2;
        sizePanel.add(sizeZFieldLabel, gbc_sizeZFieldLabel);

        sizeZField = new IcyTextField();
        final GridBagConstraints gbc_sizeZField = new GridBagConstraints();
        gbc_sizeZField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeZField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeZField.gridx = 1;
        gbc_sizeZField.gridy = 2;
        sizePanel.add(sizeZField, gbc_sizeZField);
        sizeZField.setToolTipText("Size of dimension Z for the ROI");
        sizeZField.setColumns(8);

        final JLabel sizeTFieldLabel = new JLabel("T");
        final GridBagConstraints gbc_sizeTFieldLabel = new GridBagConstraints();
        gbc_sizeTFieldLabel.insets = new Insets(0, 0, 5, 5);
        gbc_sizeTFieldLabel.gridx = 0;
        gbc_sizeTFieldLabel.gridy = 3;
        sizePanel.add(sizeTFieldLabel, gbc_sizeTFieldLabel);

        sizeTField = new IcyTextField();
        final GridBagConstraints gbc_sizeTField = new GridBagConstraints();
        gbc_sizeTField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeTField.insets = new Insets(0, 0, 5, 0);
        gbc_sizeTField.gridx = 1;
        gbc_sizeTField.gridy = 3;
        sizePanel.add(sizeTField, gbc_sizeTField);
        sizeTField.setToolTipText("Size of dimension T for the ROI");
        sizeTField.setColumns(8);

        final JLabel sizeCFieldLabel = new JLabel("C");
        final GridBagConstraints gbc_sizeCFieldLabel = new GridBagConstraints();
        gbc_sizeCFieldLabel.insets = new Insets(0, 0, 0, 5);
        gbc_sizeCFieldLabel.gridx = 0;
        gbc_sizeCFieldLabel.gridy = 4;
        sizePanel.add(sizeCFieldLabel, gbc_sizeCFieldLabel);

        sizeCField = new IcyTextField();
        final GridBagConstraints gbc_sizeCField = new GridBagConstraints();
        gbc_sizeCField.fill = GridBagConstraints.HORIZONTAL;
        gbc_sizeCField.gridx = 1;
        gbc_sizeCField.gridy = 4;
        sizePanel.add(sizeCField, gbc_sizeCField);
        sizeCField.setToolTipText("Size of dimension C for the ROI");
        sizeCField.setColumns(8);
    }

    public void selectionChanged() {
        refreshROIActionsAndProperties();
    }

    /**
     * Get the visible ROI in the ROI control panel
     */
    List<ROI> getVisibleRois() {
        return roisPanel.getVisibleRois();
    }

    /**
     * Get all selected ROIs
     */
    List<ROI> getSelectedRois() {
        return getSelectedRois(true);
    }

    /**
     * Get the selected ROI in the ROI control panel.
     *
     * @param wantReadOnly If <code>true</code> the returned list will also contains ROI in Read-Only state
     */
    List<ROI> getSelectedRois(final boolean wantReadOnly) {
        final List<ROI> selected = roisPanel.getSelectedRois();

        if (wantReadOnly)
            return selected;

        final List<ROI> result = new ArrayList<>(selected.size());

        for (final ROI roi : selected)
            if (!roi.isReadOnly())
                result.add(roi);

        return result;
    }

    static double formatPosition(final double pos, final double size) {
        // special case of infinite dimension
        if (size == Double.POSITIVE_INFINITY)
            return -1d;

        return MathUtil.roundSignificant(pos, 5, true);
    }

    static String getPositionAsString(final double pos, final double size) {
        // special case of infinite dimension
        if (size == Double.POSITIVE_INFINITY)
            return "all";

        return StringUtil.toString(MathUtil.roundSignificant(pos, 5, true));
    }

    static double formatSize(final double value) {
        return MathUtil.roundSignificant(value, 5, true);
    }

    static String getSizeAsString(final double value) {
        // special case of infinite dimension
        if (value == Double.POSITIVE_INFINITY)
            return MathUtil.INFINITE_STRING;

        return StringUtil.toString(formatSize(value));
    }

    /**
     * Refresh the ROI actions state.
     */
    public void refreshROIActions() {
        ThreadUtil.runSingle(roiActionsRefresher);
    }

    /**
     * Refresh the ROI actions state (internal)
     */
    void refreshROIActionsInternal() {
        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        final List<ROI> selectedRois = getSelectedRois();
        final ROI roi = (selectedRois.size() > 0) ? selectedRois.get(0) : null;

        boolean canSetPos = false;
        boolean canSetBnd = false;
        boolean readOnly = true;
        // set read only flag
        for (final ROI r : selectedRois) {
            readOnly &= r.isReadOnly();
            canSetPos |= r.canSetPosition();
            canSetBnd |= r.canSetBounds();
        }

        final boolean hasSelected = (roi != null);
        final boolean canSetPosition = canSetPos;
        final boolean canSetBounds = canSetBnd;
        final boolean editable = !readOnly;
        final int dim = (roi != null) ? roi.getDimension() : 0;

        // wait a bit to avoid eating too much time with refresh
        ThreadUtil.sleep(1);

        ThreadUtil.invokeNow(() -> {
            // modifyingRoi.acquireUninterruptibly();
            if (modifyingRoi.tryAcquire()) {
                try {
                    if (sequence != null) {
                        ((SpecialValueSpinnerModel) posZSpinner.getModel()).setMaximum(Integer.valueOf(sequence.getSizeZ() - 1));
                        ((SpecialValueSpinnerModel) posTSpinner.getModel()).setMaximum(Integer.valueOf(sequence.getSizeT() - 1));
                        ((SpecialValueSpinnerModel) posCSpinner.getModel()).setMaximum(Integer.valueOf(sequence.getSizeC() - 1));
                    }
                    else {
                        ((SpecialValueSpinnerModel) posZSpinner.getModel()).setMaximum(Integer.valueOf(0));
                        ((SpecialValueSpinnerModel) posTSpinner.getModel()).setMaximum(Integer.valueOf(0));
                        ((SpecialValueSpinnerModel) posCSpinner.getModel()).setMaximum(Integer.valueOf(0));
                    }
                }
                finally {
                    modifyingRoi.release();
                }
            }

            posXField.setEnabled(canSetPosition && editable);
            posYField.setEnabled(canSetPosition && editable);
            posZField.setEnabled(canSetPosition && editable);
            posTField.setEnabled(canSetPosition && editable);
            posCField.setEnabled(canSetPosition && editable);
            posZSpinner.setEnabled(canSetPosition && editable);
            posTSpinner.setEnabled(canSetPosition && editable);
            posCSpinner.setEnabled(canSetPosition && editable);
            sizeXField.setEnabled(canSetBounds && editable);
            sizeYField.setEnabled(canSetBounds && editable && (dim > 1));
            sizeZField.setEnabled(canSetBounds && editable && (dim > 2));
            sizeTField.setEnabled(canSetBounds && editable && (dim > 3));
            sizeCField.setEnabled(canSetBounds && editable && (dim > 4));

            if (dim > 2) {
                posZField.setVisible(true);
                posZFieldLabel.setVisible(true);
                posZSpinner.setVisible(false);
                posZSpinnerLabel.setVisible(false);
            }
            else {
                posZField.setVisible(false);
                posZFieldLabel.setVisible(false);
                posZSpinner.setVisible(true);
                posZSpinnerLabel.setVisible(true);
            }

            if (dim > 3) {
                posTField.setVisible(true);
                posTFieldLabel.setVisible(true);
                posTSpinner.setVisible(false);
                posTSpinnerLabel.setVisible(false);
            }
            else {
                posTField.setVisible(false);
                posTFieldLabel.setVisible(false);
                posTSpinner.setVisible(true);
                posTSpinnerLabel.setVisible(true);
            }

            if (dim > 4) {
                posCField.setVisible(true);
                posCFieldLabel.setVisible(true);
                posCSpinner.setVisible(false);
                posCSpinnerLabel.setVisible(false);
            }
            else {
                posCField.setVisible(false);
                posCFieldLabel.setVisible(false);
                posCSpinner.setVisible(true);
                posCSpinnerLabel.setVisible(true);
            }

            colorButton.setEnabled(hasSelected);
            strokeSpinner.setEnabled(hasSelected);
            alphaSlider.setEnabled(hasSelected);
            readOnlyToggle.setEnabled(hasSelected);
            readOnlyToggle.setSelected(!editable);
            displayNameCheckBox.setEnabled(hasSelected);

            setAsDefaultBtn.setEnabled(hasSelected);
        });
    }

    /**
     * Refresh ROI properties
     */
    public void refreshROIProperties() {
        ThreadUtil.runSingle(roiPropertiesRefresher);
    }

    /**
     * Refresh ROI properties (internal)
     */
    void refreshROIPropertiesInternal() {
        final List<ROI> rois = getSelectedRois();
        final ROI roi = (rois.size() > 0) ? rois.get(0) : null;
        final Rectangle5D bounds = (roi != null) ? roi.getBounds5D() : null;

        // wait a bit to avoid eating too much time with refresh
        ThreadUtil.sleep(1);

        ThreadUtil.invokeNow(() -> {
            // modifyingRoi.acquireUninterruptibly();
            if (!modifyingRoi.tryAcquire())
                return;

            try {
                if (roi != null) {
                    colorButton.setColor(roi.getColor());
                    strokeSpinner.setValue(Double.valueOf(roi.getStroke()));
                    alphaSlider.setValue((int) (roi.getOpacity() * 100));
                    displayNameCheckBox.setSelected(roi.getShowName());
                }
                else {
                    // no ROI selected
                    colorButton.setColor(Color.gray);
                    strokeSpinner.setValue(Double.valueOf(1d));
                    alphaSlider.setValue(0);
                    displayNameCheckBox.setSelected(false);
                }

                if (bounds != null) {
                    posXField.setText(getPositionAsString(bounds.getX(), bounds.getSizeX()));
                    posYField.setText(getPositionAsString(bounds.getY(), bounds.getSizeY()));
                    posZSpinner.setValue(Integer.valueOf((int) formatPosition(bounds.getZ(), bounds.getSizeZ())));
                    posZField.setText(getPositionAsString(bounds.getZ(), bounds.getSizeZ()));
                    posTSpinner.setValue(Integer.valueOf((int) formatPosition(bounds.getT(), bounds.getSizeT())));
                    posTField.setText(getPositionAsString(bounds.getT(), bounds.getSizeT()));
                    posCSpinner.setValue(Integer.valueOf((int) formatPosition(bounds.getC(), bounds.getSizeC())));
                    posCField.setText(getPositionAsString(bounds.getC(), bounds.getSizeC()));

                    sizeXField.setText(getSizeAsString(bounds.getSizeX()));
                    sizeYField.setText(getSizeAsString(bounds.getSizeY()));
                    sizeZField.setText(getSizeAsString(bounds.getSizeZ()));
                    sizeTField.setText(getSizeAsString(bounds.getSizeT()));
                    sizeCField.setText(getSizeAsString(bounds.getSizeC()));
                }
                else {
                    posXField.setText("");
                    posYField.setText("");
                    posZField.setText("");
                    posTField.setText("");
                    posCField.setText("");
                    posZSpinner.setValue(Integer.valueOf(0));
                    posTSpinner.setValue(Integer.valueOf(0));
                    posCSpinner.setValue(Integer.valueOf(0));

                    sizeXField.setText("");
                    sizeYField.setText("");
                    sizeZField.setText("");
                    sizeTField.setText("");
                    sizeCField.setText("");
                }
            }
            finally {
                modifyingRoi.release();
            }
        });
    }

    /**
     * Refresh ROI actions and properties
     */
    public void refreshROIActionsAndProperties() {
        ThreadUtil.runSingle(roiActionsRefresher);
        ThreadUtil.runSingle(roiPropertiesRefresher);
    }

    @Override
    public void textChanged(final IcyTextField source, final boolean validate) {
        // source not anymore enable --> cancel validation
        if (!source.isEnabled())
            return;

        // keep trace of modified ROI and wait for validation
        if (!validate) {
            modifiedRois.clear();
            // better to use weak reference here to not retain ROI
            for (final ROI roi : getSelectedRois())
                modifiedRois.add(new WeakReference<>(roi));

            return;
        }

        // at this point the text is validated so we can recover modified ROIs...
        final List<ROI> rois = new ArrayList<>();
        for (final Reference<ROI> ref : modifiedRois) {
            final ROI roi = ref.get();
            if (roi != null)
                rois.add(roi);
        }

        // nothing to edit ?
        if (rois.isEmpty())
            return;

        // can get semaphore ?
        if (!modifyingRoi.tryAcquire())
            return;

        try {
            // position fields ?
            if ((source == posXField) || (source == posYField) || (source == posZField) || (source == posTField) || (source == posCField)) {
                final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                // correct value ?
                if (!Double.isNaN(value)) {
                    final List<ROI> roiToModify = new ArrayList<>();

                    // get the ROI we were modifying
                    for (final ROI roi : rois) {
                        // can't edit read only ROI (should not arrive)
                        if (roi.isReadOnly())
                            continue;

                        // roi support position change ?
                        if (roi.canSetPosition())
                            roiToModify.add(roi);
                    }

                    // we have effectively some ROI to modify ?
                    if (!roiToModify.isEmpty()) {
                        Point5D positionSet = null;

                        // single change ?
                        if (roiToModify.size() == 1) {
                            final ROI roi = roiToModify.get(0);

                            // get current ROI position
                            final Point5D savePosition = roi.getPosition5D();
                            final Point5D position = (Point5D) savePosition.clone();

                            if (source == posXField)
                                position.setX(value);
                            else if (source == posYField)
                                position.setY(value);
                            else if (source == posZField)
                                position.setZ(value);
                            else if (source == posTField)
                                position.setT(value);
                            else
                                position.setC(value);

                            // set new position
                            roi.setPosition5D(position);
                            // keep trace of accepted ROI position
                            positionSet = roi.getPosition5D();

                            // add position change to undo manager
                            Icy.getMainInterface().getUndoManager().addEdit(new PositionROIEdit(roi, savePosition, false));
                        }
                        else {
                            final List<Point5D> savePositions = new ArrayList<>();
                            final List<Point5D> newPositions = new ArrayList<>();

                            // save previous positions
                            for (final ROI roi : roiToModify)
                                savePositions.add(roi.getPosition5D());

                            for (final ROI roi : roiToModify) {
                                // get current ROI position
                                final Point5D position = roi.getPosition5D();

                                if (source == posXField)
                                    position.setX(value);
                                else if (source == posYField)
                                    position.setY(value);
                                else if (source == posZField)
                                    position.setZ(value);
                                else if (source == posTField)
                                    position.setT(value);
                                else
                                    position.setC(value);

                                // set new position
                                roi.setPosition5D(position);
                                // keep trace of first accepted ROI position
                                if (positionSet == null)
                                    positionSet = roi.getPosition5D();

                                // save new position
                                newPositions.add(position);
                            }

                            // add position change to undo manager
                            Icy.getMainInterface().getUndoManager().addEdit(new PositionROIsEdit(roiToModify, savePositions, newPositions, false)
                            );
                        }

                        if (positionSet != null) {
                            double p;

                            // fix field value if needed
                            if (source == posXField)
                                p = positionSet.getX();
                            else if (source == posYField)
                                p = positionSet.getY();
                            else if (source == posZField)
                                p = positionSet.getZ();
                            else if (source == posTField)
                                p = positionSet.getT();
                            else
                                p = positionSet.getC();

                            // change infinite by -1
                            if (p == Double.NEGATIVE_INFINITY)
                                p = -1d;

                            source.setText(Double.toString(p));
                        }
                    }
                }
            }
            // size fields ?
            else if ((source == sizeXField) || (source == sizeYField) || (source == sizeZField) || (source == sizeTField) || (source == sizeCField)) {
                final double value = StringUtil.parseDouble(source.getText(), Double.NaN);

                // correct value ?
                if (!Double.isNaN(value)) {
                    final List<ROI> roiToModify = new ArrayList<>();

                    // get the ROI we were modifying
                    for (final ROI roi : rois) {
                        // can't edit read only ROI (should not arrive)
                        if (roi.isReadOnly())
                            continue;

                        // roi support position change ?
                        if (roi.canSetPosition())
                            roiToModify.add(roi);
                    }

                    // we have effectively some ROI to modify ?
                    if (!roiToModify.isEmpty()) {
                        Rectangle5D boundsSet = null;

                        if (roiToModify.size() == 1) {
                            final ROI roi = roiToModify.get(0);

                            // get current ROI size
                            final Rectangle5D saveBounds = roi.getBounds5D();
                            final Rectangle5D bounds = (Rectangle5D) saveBounds.clone();

                            if (source == sizeXField)
                                bounds.setSizeX(value);
                            else if (source == sizeYField)
                                bounds.setSizeY(value);
                            else if (source == sizeZField)
                                bounds.setSizeZ(value);
                            else if (source == sizeTField)
                                bounds.setSizeT(value);
                            else
                                bounds.setSizeC(value);

                            roi.setBounds5D(bounds);
                            // keep trace of first accepted ROI bounds
                            boundsSet = roi.getBounds5D();

                            // add position change to undo manager
                            Icy.getMainInterface().getUndoManager().addEdit(new BoundsROIEdit(roi, saveBounds, false));
                        }
                        else {
                            final List<Rectangle5D> saveBoundsList = new ArrayList<>();
                            final List<Rectangle5D> newBoundsList = new ArrayList<>();

                            // save previous size
                            for (final ROI roi : roiToModify)
                                saveBoundsList.add(roi.getBounds5D());

                            for (final ROI roi : roiToModify) {
                                // get current ROI size
                                final Rectangle5D bounds = roi.getBounds5D();

                                if (source == sizeXField)
                                    bounds.setSizeX(value);
                                else if (source == sizeYField)
                                    bounds.setSizeY(value);
                                else if (source == sizeZField)
                                    bounds.setSizeZ(value);
                                else if (source == sizeTField)
                                    bounds.setSizeT(value);
                                else
                                    bounds.setSizeC(value);

                                roi.setBounds5D(bounds);
                                // keep trace of first accepted ROI bounds
                                if (boundsSet == null)
                                    boundsSet = roi.getBounds5D();

                                // save new size
                                newBoundsList.add(bounds);
                            }

                            // add position change to undo manager
                            Icy.getMainInterface().getUndoManager().addEdit(new BoundsROIsEdit(roiToModify, saveBoundsList, newBoundsList, false)
                            );
                        }

                        if (boundsSet != null) {
                            final double p;

                            // fix field value if needed
                            if (source == sizeXField)
                                p = boundsSet.getSizeX();
                            else if (source == sizeYField)
                                p = boundsSet.getSizeY();
                            else if (source == sizeZField)
                                p = boundsSet.getSizeZ();
                            else if (source == sizeTField)
                                p = boundsSet.getSizeT();
                            else
                                p = boundsSet.getSizeC();

                            source.setText(Double.toString(p));
                        }
                    }
                }
            }
        }
        finally {
            modifyingRoi.release();
        }
    }

    @Override
    public void colorChanged(final ColorChooserButton source) {
        // source not anymore enable --> cancel change
        if (!source.isEnabled())
            return;

        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        if (sequence == null)
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        final List<ROI> rois = getSelectedRois();
        final List<Object> oldValues = new ArrayList<>();
        final Color color = source.getColor();

        sequence.beginUpdate();
        try {
            // set new color
            for (final ROI roi : rois) {
                // save previous color
                oldValues.add(roi.getColor());
                roi.setColor(color);
            }
        }
        finally {
            sequence.endUpdate();
            modifyingRoi.release();
        }

        // add color change to undo manager
        sequence.addUndoableEdit(new PropertyROIsEdit(rois, ROI.PROPERTY_COLOR, oldValues, color));
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        if (!(e.getSource() instanceof final JComponent source))
            return;

        // source not anymore enable --> cancel change
        if (!source.isEnabled())
            return;

        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        if (sequence == null)
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        try {
            if (source == strokeSpinner) {
                final List<ROI> rois = getSelectedRois();
                final List<Object> oldValues = new ArrayList<>();
                final double stroke = ((Double) strokeSpinner.getValue()).doubleValue();

                sequence.beginUpdate();
                try {
                    for (final ROI roi : rois) {
                        // save previous stroke
                        oldValues.add(Double.valueOf(roi.getStroke()));
                        roi.setStroke(stroke);
                    }
                }
                finally {
                    sequence.endUpdate();
                }

                // add stroke change to undo manager
                sequence.addUndoableEdit(new PropertyROIsEdit(rois, ROI.PROPERTY_STROKE, oldValues, Double.valueOf(stroke)));
            }
            else if (source == alphaSlider) {
                final List<ROI> rois = getSelectedRois();
                final List<Object> oldValues = new ArrayList<>();
                final float opacity = alphaSlider.getValue() / 100f;

                sequence.beginUpdate();
                try {
                    for (final ROI roi : rois) {
                        // save previous opacity
                        oldValues.add(Float.valueOf(roi.getOpacity()));
                        roi.setOpacity(opacity);
                    }
                }
                finally {
                    sequence.endUpdate();
                }

                // add opacity change to undo manager
                sequence.addUndoableEdit(new PropertyROIsEdit(rois, ROI.PROPERTY_OPACITY, oldValues, Float.valueOf(opacity)));
            }
            else if ((source == posZSpinner) || (source == posTSpinner) || (source == posCSpinner)) {
                final List<ROI> rois = getSelectedRois();

                // nothing to edit
                if ((rois == null) || rois.isEmpty())
                    return;

                final SpecialValueSpinner spinner = (SpecialValueSpinner) source;
                final double value = ((Integer) spinner.getValue()).intValue();

                // correct value ?
                if (!Double.isNaN(value)) {
                    final List<ROI> roiToModify = new ArrayList<>();

                    // get the ROI we were modifying
                    for (final ROI roi : rois) {
                        // can't edit read only ROI (should not arrive)
                        if (roi.isReadOnly())
                            continue;

                        // roi support position change ?
                        if (roi.canSetPosition())
                            roiToModify.add(roi);
                    }

                    // we have effectively some ROI to modify ?
                    if (!roiToModify.isEmpty()) {
                        Point5D positionSet = null;

                        // single change ?
                        if (roiToModify.size() == 1) {
                            final ROI roi = roiToModify.get(0);

                            // get current ROI position
                            final Point5D savePosition = roi.getPosition5D();
                            final Point5D position = (Point5D) savePosition.clone();

                            if (source == posZSpinner)
                                position.setZ(value);
                            else if (source == posTSpinner)
                                position.setT(value);
                            else
                                position.setC(value);

                            // set new position
                            roi.setPosition5D(position);
                            // keep trace of accepted ROI position
                            positionSet = roi.getPosition5D();

                            // add position change to undo manager
                            Icy.getMainInterface().getUndoManager().addEdit(new PositionROIEdit(roi, savePosition, false));
                        }
                        else {
                            final List<Point5D> savePositions = new ArrayList<>();
                            final List<Point5D> newPositions = new ArrayList<>();

                            // save previous positions
                            for (final ROI roi : roiToModify)
                                savePositions.add(roi.getPosition5D());

                            for (final ROI roi : roiToModify) {
                                // get current ROI position
                                final Point5D position = roi.getPosition5D();

                                if (source == posZSpinner)
                                    position.setZ(value);
                                else if (source == posTSpinner)
                                    position.setT(value);
                                else
                                    position.setC(value);

                                // set new position
                                roi.setPosition5D(position);
                                // keep trace of first accepted ROI position
                                if (positionSet == null)
                                    positionSet = roi.getPosition5D();

                                // save new position
                                newPositions.add(position);
                            }

                            // add position change to undo manager
                            Icy.getMainInterface().getUndoManager().addEdit(new PositionROIsEdit(roiToModify, savePositions, newPositions, false));
                        }

                        if (positionSet != null) {
                            double p;

                            // fix field value if needed
                            if (source == posZSpinner)
                                p = positionSet.getZ();
                            else if (source == posTSpinner)
                                p = positionSet.getT();
                            else
                                p = positionSet.getC();

                            // change infinite by -1
                            if (p == Double.NEGATIVE_INFINITY)
                                p = -1d;

                            spinner.setValue(Integer.valueOf((int) p));
                        }
                    }
                }
            }
        }
        finally {
            modifyingRoi.release();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (!(e.getSource() instanceof final JComponent source))
            return;

        // source not anymore enable --> cancel change
        if (!source.isEnabled())
            return;

        final Sequence sequence = Icy.getMainInterface().getActiveSequence();
        if (sequence == null)
            return;

        if (!modifyingRoi.tryAcquire())
            return;

        try {
            if (source == readOnlyToggle) {
                for (final ROI roi : getSelectedRois()) {
                    roi.setReadOnly(readOnlyToggle.isSelected());
                    roi.setSelected(true);
                }
            }
            else if (source == displayNameCheckBox) {
                sequence.beginUpdate();
                try {
                    final boolean display = displayNameCheckBox.isSelected();

                    for (final ROI roi : getSelectedRois())
                        roi.setShowName(display);
                }
                finally {
                    sequence.endUpdate();
                }
            }
            else if (source == setAsDefaultBtn) {
                final boolean display = displayNameCheckBox.isSelected();
                final Color color = colorButton.getColor();
                final double stroke = ((Double) strokeSpinner.getValue()).doubleValue();
                final float opacity = alphaSlider.getValue() / 100f;

                // set default ROI Overlay properties
                ROI.setDefaultColor(color);
                ROI.setDefaultShowName(display);
                ROI.setDefaultOpacity(opacity);
                ROI.setDefaultStroke(stroke);
            }
        }
        finally {
            modifyingRoi.release();
        }
    }

    @Override
    public void clipboardChanged() {
        refreshROIActions();
    }

    // one of the selected ROI changed
    public void roiChanged(final ROIEvent event) {
        switch (event.getType()) {
            case ROI_CHANGED:
                // refresh the properties
                refreshROIProperties();
                break;

            case FOCUS_CHANGED:
                // nothing to do here
                break;

            case PROPERTY_CHANGED:
                final String propertyName = event.getPropertyName();

                if (ROI.PROPERTY_READONLY.equals(propertyName))
                    refreshROIActions();
                break;

            case SELECTION_CHANGED:
                // handle externally with the setSelectedROI() method
                break;

            default:
                break;
        }
    }
}
