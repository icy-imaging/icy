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

package org.bioimageanalysis.icy.gui.sequence;

import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.gui.component.button.IcyButton;
import org.bioimageanalysis.icy.gui.frame.GenericFrame;
import org.bioimageanalysis.icy.gui.listener.ActiveSequenceListener;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.common.math.UnitUtil.UnitPrefix;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.datetime.DateUtil;
import org.bioimageanalysis.icy.gui.EventUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Date;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SequenceInfosPanel extends JPanel implements ActiveSequenceListener {
    // GUI
    private JLabel dimensionLabel;
    private JLabel resXLabel;
    private JLabel resYLabel;
    private JLabel resZLabel;
    private JLabel resTLabel;
    private JLabel sizeLabel;
    private JLabel channelLabel;

    //private IcyButton editBtn;
    private IcyButton editBtn;
    //private IcyButton detailBtn;
    private IcyButton detailBtn;

    private JLabel pathLabel;
    JTextField pathField;
    private JTextField nameField;

    private JLabel dataTypeLabel;
    private JLabel creationDateLabel;
    private JLabel userNameLabel;

    // internals
    private final Runnable infosRefresher;

    public SequenceInfosPanel() {
        super();

        initialize();

        editBtn.addActionListener(e -> {
            // it should be the current focused sequence
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            if (seq != null)
                new SequencePropertiesDialog(seq);
        });

        detailBtn.addActionListener(e -> {
            // it should be the current focused sequence
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            if (seq != null) {
                final GenericFrame g = new GenericFrame(seq.getName() + " - Metadata", new SequenceMetadataPanel(seq));

                g.addToDesktopPane();
                g.center();
                g.requestFocus();
            }
        });

        infosRefresher = () -> ThreadUtil.invokeNow(() -> updateInfosInternal(Icy.getMainInterface().getActiveSequence()));

        updateInfosInternal(null);
    }

    public void initialize() {
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{70, 40, 90, 40};
        gridBagLayout.rowHeights = new int[]{18, 0, 18, 18, 18, 0, 18, 18, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lbl_name = new JLabel("Name");
        lbl_name.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl_name.setToolTipText("Sequence name");
        final GridBagConstraints gbc_lbl_name = new GridBagConstraints();
        gbc_lbl_name.anchor = GridBagConstraints.WEST;
        gbc_lbl_name.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_name.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_name.gridx = 0;
        gbc_lbl_name.gridy = 0;
        add(lbl_name, gbc_lbl_name);

        nameField = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                final Dimension result = super.getPreferredSize();
                // prevent enlarging panel
                result.width = 100;
                return result;
            }
        };
        nameField.setOpaque(false);
        nameField.setBorder(null);
        nameField.setEditable(false);

        final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridwidth = 3;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 0;
        add(nameField, gbc_scrollPane);

        pathLabel = new JLabel("Path");
        pathLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        pathLabel.setToolTipText("Sequence file path");
        final GridBagConstraints gbc_pathLabel = new GridBagConstraints();
        gbc_pathLabel.fill = GridBagConstraints.VERTICAL;
        gbc_pathLabel.anchor = GridBagConstraints.WEST;
        gbc_pathLabel.insets = new Insets(0, 0, 5, 5);
        gbc_pathLabel.gridx = 0;
        gbc_pathLabel.gridy = 1;
        add(pathLabel, gbc_pathLabel);

        pathField = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                final Dimension result = super.getPreferredSize();
                // prevent enlarging panel
                result.width = 100;
                return result;
            }
        };
        pathField.setOpaque(false);
        pathField.setBorder(null);
        pathField.setEditable(false);
        pathField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.isConsumed())
                    return;

                if (EventUtil.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    try {
                        SystemUtil.openFolder(FileUtil.getDirectory(pathField.getText()));
                    }
                    catch (final IOException e1) {
                        IcyLogger.error(SequenceInfosPanel.class, e1, "Unable to open folder: " + pathField.getText());
                    }

                    e.consume();
                }
            }
        });

        final GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridwidth = 3;
        gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_1.gridx = 1;
        gbc_scrollPane_1.gridy = 1;
        add(pathField, gbc_scrollPane_1);

        final JLabel lbl_dim = new JLabel("Dimension");
        lbl_dim.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl_dim.setToolTipText("Size of X, Y, Z and T dimension");
        final GridBagConstraints gbc_lbl_dim = new GridBagConstraints();
        gbc_lbl_dim.anchor = GridBagConstraints.WEST;
        gbc_lbl_dim.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_dim.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_dim.gridx = 0;
        gbc_lbl_dim.gridy = 2;
        add(lbl_dim, gbc_lbl_dim);

        dimensionLabel = new JLabel();
        dimensionLabel.setText("---");
        final GridBagConstraints gbc_dimensionLabel = new GridBagConstraints();
        gbc_dimensionLabel.anchor = GridBagConstraints.WEST;
        gbc_dimensionLabel.gridwidth = 3;
        gbc_dimensionLabel.fill = GridBagConstraints.VERTICAL;
        gbc_dimensionLabel.insets = new Insets(0, 0, 5, 0);
        gbc_dimensionLabel.gridx = 1;
        gbc_dimensionLabel.gridy = 2;
        add(dimensionLabel, gbc_dimensionLabel);

        final JLabel lbl_channel = new JLabel("Channel");
        lbl_channel.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl_channel.setToolTipText("Number of channel");
        final GridBagConstraints gbc_lbl_channel = new GridBagConstraints();
        gbc_lbl_channel.anchor = GridBagConstraints.WEST;
        gbc_lbl_channel.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_channel.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_channel.gridx = 0;
        gbc_lbl_channel.gridy = 3;
        add(lbl_channel, gbc_lbl_channel);

        channelLabel = new JLabel();
        channelLabel.setText("---");
        final GridBagConstraints gbc_channelLabel = new GridBagConstraints();
        gbc_channelLabel.anchor = GridBagConstraints.WEST;
        gbc_channelLabel.fill = GridBagConstraints.VERTICAL;
        gbc_channelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_channelLabel.gridx = 1;
        gbc_channelLabel.gridy = 3;
        add(channelLabel, gbc_channelLabel);

        final JLabel lblNewLabel = new JLabel("Data type");
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblNewLabel.setToolTipText("Data type");
        final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
        gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 2;
        gbc_lblNewLabel.gridy = 3;
        add(lblNewLabel, gbc_lblNewLabel);

        dataTypeLabel = new JLabel("---");
        final GridBagConstraints gbc_dataTypeLabel = new GridBagConstraints();
        gbc_dataTypeLabel.fill = GridBagConstraints.VERTICAL;
        gbc_dataTypeLabel.anchor = GridBagConstraints.WEST;
        gbc_dataTypeLabel.insets = new Insets(0, 0, 5, 0);
        gbc_dataTypeLabel.gridx = 3;
        gbc_dataTypeLabel.gridy = 3;
        add(dataTypeLabel, gbc_dataTypeLabel);

        final JLabel lbl_size = new JLabel("Size");
        lbl_size.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl_size.setToolTipText("Size");
        final GridBagConstraints gbc_lbl_size = new GridBagConstraints();
        gbc_lbl_size.anchor = GridBagConstraints.WEST;
        gbc_lbl_size.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_size.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_size.gridx = 0;
        gbc_lbl_size.gridy = 4;
        add(lbl_size, gbc_lbl_size);

        sizeLabel = new JLabel();
        sizeLabel.setText("---");
        final GridBagConstraints gbc_sizeLabel = new GridBagConstraints();
        gbc_sizeLabel.anchor = GridBagConstraints.WEST;
        gbc_sizeLabel.fill = GridBagConstraints.VERTICAL;
        gbc_sizeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_sizeLabel.gridx = 1;
        gbc_sizeLabel.gridy = 4;
        add(sizeLabel, gbc_sizeLabel);

        final JLabel lblNewLabel_2 = new JLabel("Owner(s)");
        lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblNewLabel_2.setToolTipText("Owner(s) user name (person who created, generated or modified the dataset)");
        final GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.fill = GridBagConstraints.VERTICAL;
        gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 2;
        gbc_lblNewLabel_2.gridy = 4;
        add(lblNewLabel_2, gbc_lblNewLabel_2);

        userNameLabel = new JLabel("---");
        final GridBagConstraints gbc_userNameLabel = new GridBagConstraints();
        gbc_userNameLabel.anchor = GridBagConstraints.WEST;
        gbc_userNameLabel.fill = GridBagConstraints.VERTICAL;
        gbc_userNameLabel.insets = new Insets(0, 0, 5, 0);
        gbc_userNameLabel.gridx = 3;
        gbc_userNameLabel.gridy = 4;
        add(userNameLabel, gbc_userNameLabel);

        final JLabel lblNewLabel_1 = new JLabel("Date");
        lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblNewLabel_1.setToolTipText("Creation / acquisition date");
        final GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 5;
        add(lblNewLabel_1, gbc_lblNewLabel_1);

        creationDateLabel = new JLabel("---");
        final GridBagConstraints gbc_creationDateLabel = new GridBagConstraints();
        gbc_creationDateLabel.fill = GridBagConstraints.VERTICAL;
        gbc_creationDateLabel.anchor = GridBagConstraints.WEST;
        gbc_creationDateLabel.insets = new Insets(0, 0, 5, 5);
        gbc_creationDateLabel.gridx = 1;
        gbc_creationDateLabel.gridy = 5;
        add(creationDateLabel, gbc_creationDateLabel);

        final JLabel lbl_time = new JLabel("Time interval");
        lbl_time.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl_time.setToolTipText("Time Interval");
        final GridBagConstraints gbc_lbl_time = new GridBagConstraints();
        gbc_lbl_time.anchor = GridBagConstraints.WEST;
        gbc_lbl_time.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_time.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_time.gridx = 2;
        gbc_lbl_time.gridy = 5;
        add(lbl_time, gbc_lbl_time);

        resTLabel = new JLabel();
        resTLabel.setText("---");
        final GridBagConstraints gbc_resTLabel = new GridBagConstraints();
        gbc_resTLabel.anchor = GridBagConstraints.WEST;
        gbc_resTLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resTLabel.insets = new Insets(0, 0, 5, 0);
        gbc_resTLabel.gridx = 3;
        gbc_resTLabel.gridy = 5;
        add(resTLabel, gbc_resTLabel);

        final JLabel lbl_psx = new JLabel("Pixel size");
        lbl_psx.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl_psx.setToolTipText("Pixel size for X, Y, Z dimension");
        final GridBagConstraints gbc_lbl_psx = new GridBagConstraints();
        gbc_lbl_psx.anchor = GridBagConstraints.WEST;
        gbc_lbl_psx.fill = GridBagConstraints.VERTICAL;
        gbc_lbl_psx.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_psx.gridx = 0;
        gbc_lbl_psx.gridy = 6;
        add(lbl_psx, gbc_lbl_psx);

        resXLabel = new JLabel();
        resXLabel.setText("---");
        final GridBagConstraints gbc_resXLabel = new GridBagConstraints();
        gbc_resXLabel.anchor = GridBagConstraints.WEST;
        gbc_resXLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resXLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resXLabel.gridx = 1;
        gbc_resXLabel.gridy = 6;
        add(resXLabel, gbc_resXLabel);

        resYLabel = new JLabel();
        resYLabel.setText("---");
        final GridBagConstraints gbc_resYLabel = new GridBagConstraints();
        gbc_resYLabel.anchor = GridBagConstraints.WEST;
        gbc_resYLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resYLabel.insets = new Insets(0, 0, 5, 5);
        gbc_resYLabel.gridx = 2;
        gbc_resYLabel.gridy = 6;
        add(resYLabel, gbc_resYLabel);

        resZLabel = new JLabel();
        resZLabel.setText("---");
        final GridBagConstraints gbc_resZLabel = new GridBagConstraints();
        gbc_resZLabel.anchor = GridBagConstraints.WEST;
        gbc_resZLabel.fill = GridBagConstraints.VERTICAL;
        gbc_resZLabel.insets = new Insets(0, 0, 5, 0);
        gbc_resZLabel.gridx = 3;
        gbc_resZLabel.gridy = 6;
        add(resZLabel, gbc_resZLabel);

        editBtn = new IcyButton("Edit", SVGResource.EDIT);
        editBtn.setToolTipText("Edit sequence properties");

        final GridBagConstraints gbc_editBtn = new GridBagConstraints();
        gbc_editBtn.gridwidth = 2;
        gbc_editBtn.fill = GridBagConstraints.BOTH;
        gbc_editBtn.insets = new Insets(0, 0, 0, 5);
        gbc_editBtn.gridx = 0;
        gbc_editBtn.gridy = 7;
        add(editBtn, gbc_editBtn);

        detailBtn = new IcyButton("Show metadata", SVGResource.DESCRIPTION);
        detailBtn.setText("Metadata");
        detailBtn.setToolTipText("Show all associated metadata informations");

        final GridBagConstraints gbc_detailBtn = new GridBagConstraints();
        gbc_detailBtn.gridwidth = 2;
        gbc_detailBtn.fill = GridBagConstraints.BOTH;
        gbc_detailBtn.gridx = 2;
        gbc_detailBtn.gridy = 7;
        add(detailBtn, gbc_detailBtn);
    }

    public void updateInfos() {
        ThreadUtil.runSingle(infosRefresher);
    }

    public void updateInfosInternal(final Sequence sequence) {
        if (sequence != null) {
            final int sizeX = sequence.getSizeX();
            final int sizeY = sequence.getSizeY();
            final int sizeZ = sequence.getSizeZ();
            final int sizeT = sequence.getSizeT();
            final int sizeC = sequence.getSizeC();

            final double pxSizeX = sequence.getPixelSizeX();
            final double pxSizeY = sequence.getPixelSizeY();
            final double pxSizeZ = sequence.getPixelSizeZ();

            final String path = sequence.getFilename();

            nameField.setText(sequence.getName());
            // path
            if (StringUtil.isEmpty(path)) {
                pathLabel.setVisible(false);
                pathField.setVisible(false);
            }
            else {
                pathLabel.setVisible(true);
                pathField.setVisible(true);
                pathField.setText(path);
            }
            dimensionLabel.setText(sizeX + " x " + sizeY + " x " + sizeZ + " x " + sizeT);
            channelLabel.setText(StringUtil.toString(sizeC));
            dataTypeLabel.setText(sequence.getDataType().toString());
            sizeLabel.setText(UnitUtil.getBytesString((double) sizeX * (double) sizeY * sizeZ * sizeT * sizeC * sequence.getDataType().getSize()));

            resXLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeX, 2, UnitPrefix.MICRO));
            resYLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeY, 2, UnitPrefix.MICRO));
            resZLabel.setText(UnitUtil.getBestUnitInMeters(pxSizeZ, 2, UnitPrefix.MICRO));
            // acquisition time
            final long timeStamp = sequence.getPositionT();
            if (timeStamp != 0)
                creationDateLabel.setText(DateUtil.format("dd/MM/yyyy", new Date(timeStamp)));
            else
                creationDateLabel.setText("Unknow");
            resTLabel.setText(UnitUtil.displayTimeAsStringWithUnits(sequence.getTimeInterval() * 1000d, false));

            // owner user name
            String userNames = "";
            for (final String s : sequence.getUserNames()) {
                if (StringUtil.isEmpty(userNames))
                    userNames = s;
                else
                    userNames += "; " + s;
            }
            userNameLabel.setText(StringUtil.limit(userNames, 20, true));

            nameField.setToolTipText(sequence.getName());
            pathField.setToolTipText(path + "    (double click to see file location)");
            dimensionLabel.setToolTipText(
                    "Size X : " + sizeX + "   Size Y : " + sizeY + "   Size Z : " + sizeZ + "   Size T : " + sizeT);
            if (sizeC > 1)
                channelLabel.setToolTipText(sizeC + " channels");
            else
                channelLabel.setToolTipText(sizeC + " channel");
            dataTypeLabel.setToolTipText(sequence.getDataType().toLongString());
            sizeLabel.setToolTipText(sizeLabel.getText());

            resXLabel.setToolTipText("X pixel resolution: " + resXLabel.getText());
            resYLabel.setToolTipText("Y pixel resolution: " + resYLabel.getText());
            resZLabel.setToolTipText("Z pixel resolution: " + resZLabel.getText());
            if (timeStamp != 0)
                creationDateLabel.setToolTipText(DateUtil.format("EEE d MMMMM yyyy - HH:mm:ss", new Date(timeStamp)));
            else
                creationDateLabel.setToolTipText("");
            resTLabel.setToolTipText("T time resolution: " + resTLabel.getText());
            userNameLabel.setToolTipText(userNames);

            editBtn.setEnabled(true);
            detailBtn.setEnabled(true);
        }
        else {
            pathLabel.setVisible(false);
            pathField.setVisible(false);

            nameField.setText("-");
            dimensionLabel.setText("-");
            channelLabel.setText("-");
            dataTypeLabel.setText("-");
            sizeLabel.setText("-");
            resXLabel.setText("-");
            resYLabel.setText("-");
            resZLabel.setText("-");
            creationDateLabel.setText("-");
            resTLabel.setText("-");
            userNameLabel.setText("-");

            nameField.setToolTipText("");
            dimensionLabel.setToolTipText("");
            channelLabel.setToolTipText("");
            dataTypeLabel.setToolTipText("");
            sizeLabel.setToolTipText("");
            resXLabel.setToolTipText("X pixel resolution");
            resYLabel.setToolTipText("Y pixel resolution");
            resZLabel.setToolTipText("Z pixel resolution");
            creationDateLabel.setToolTipText("");
            resTLabel.setToolTipText("T time resolution");
            userNameLabel.setToolTipText("");

            editBtn.setEnabled(false);
            detailBtn.setEnabled(false);
        }

        revalidate();
    }

    @Override
    public void sequenceActivated(final Sequence sequence) {
        updateInfos();
    }

    @Override
    public void sequenceDeactivated(final Sequence sequence) {
        // nothing to do here
    }

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        switch (event.getSourceType()) {
            case SEQUENCE_DATA:
            case SEQUENCE_TYPE:
            case SEQUENCE_META:
                updateInfos();
                break;
        }
    }
}
