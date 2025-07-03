/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

package org.bioimageanalysis.icy.gui.dialog;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to let the user select the appropriate importer to open a file when several importers are
 * available.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ImporterSelectionDialog extends ActionDialog {
    JList<Object> importerList;
    JLabel pathLabel;

    public ImporterSelectionDialog(final List<?> importers, final String path) {
        super("Select importer");

        initializeGui();

        pathLabel.setText("  " + FileUtil.getFileName(path));

        importerList.setListData(getItems(importers).toArray());
        if (importers.size() > 0)
            importerList.setSelectedIndex(0);

        importerList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                // double click ?
                if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
                    // have an item selected ? select it !
                    if (importerList.getSelectedIndex() != -1)
                        getOkBtn().doClick();
                }
            }
        });

        setPreferredSize(new Dimension(360, 240));
        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        setVisible(true);
    }

    private void initializeGui() {
        setTitle("Importer selection");

        importerList = new JList<>();
        importerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final JScrollPane scrollPane = new JScrollPane(importerList);
        scrollPane.setPreferredSize(new Dimension(320, 80));
        scrollPane.setMinimumSize(new Dimension(320, 80));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getMainPanel().add(scrollPane, BorderLayout.CENTER);

        final JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(4, 0, 4, 0));
        getMainPanel().add(panel, BorderLayout.NORTH);
        final GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{0, 0};
        gbl_panel.rowHeights = new int[]{0, 0, 0};
        gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        final JLabel newLabel = new JLabel(" Select the importer to open the following file:");
        newLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        final GridBagConstraints gbc_newLabel = new GridBagConstraints();
        gbc_newLabel.anchor = GridBagConstraints.WEST;
        gbc_newLabel.insets = new Insets(0, 0, 5, 0);
        gbc_newLabel.gridx = 0;
        gbc_newLabel.gridy = 0;
        panel.add(newLabel, gbc_newLabel);

        pathLabel = new JLabel("  ");
        pathLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        final GridBagConstraints gbc_pathLabel = new GridBagConstraints();
        gbc_pathLabel.anchor = GridBagConstraints.WEST;
        gbc_pathLabel.gridx = 0;
        gbc_pathLabel.gridy = 1;
        panel.add(pathLabel, gbc_pathLabel);
    }

    private @NotNull List<ImporterPluginItem> getItems(final @NotNull List<?> importers) {
        final List<ImporterPluginItem> result = new ArrayList<>();

        for (final Object importer : importers) {
            final PluginDescriptor plugin = ExtensionLoader.getPlugin(importer.getClass().getName());
            if (plugin != null)
                result.add(new ImporterPluginItem(plugin, importer));
        }

        return result;
    }

    public Object getSelectedImporter() {
        return ((ImporterPluginItem) importerList.getSelectedValue()).importer();
    }

    private record ImporterPluginItem(PluginDescriptor plugin, Object importer) {
        @Override
        public @NotNull String toString() {
            return plugin.toString();
        }
    }
}
