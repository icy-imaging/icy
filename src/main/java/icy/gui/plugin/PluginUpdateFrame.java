/*
 * Copyright 2010-2023 Institut Pasteur.
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
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */
package icy.gui.plugin;

import icy.gui.frame.ActionFrame;
import icy.gui.util.GuiUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginUpdater;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class PluginUpdateFrame extends ActionFrame {
    JList<PluginDescriptor> pluginList;
    private final DefaultListModel<PluginDescriptor> listModel;

    public PluginUpdateFrame(final List<PluginDescriptor> toInstallPlugins) {
        super("Plugin Update", true);

        setPreferredSize(new Dimension(640, 500));

        final JPanel titlePanel = GuiUtil.createCenteredBoldLabel("Select the plugin(s) to update in the list");
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JTextArea changeLogArea = new JTextArea();
        changeLogArea.setEditable(false);
        final JLabel changeLogTitleLabel = GuiUtil.createBoldLabel("Change log :");

        listModel = new DefaultListModel<>();
        pluginList = new JList<>(listModel);
        for (final PluginDescriptor plugin : toInstallPlugins)
            listModel.addElement(plugin);

        pluginList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pluginList.getSelectionModel().addListSelectionListener(e -> {
            final PluginDescriptor plugin = pluginList.getSelectedValue();

            if (plugin != null) {
                // plugin.loadChangeLog() can take lot of time, better to do that in background...
                ThreadUtil.bgRun(() -> {
                    plugin.loadChangeLog();
                    final String changeLog = plugin.getChangeLog();

                    if (StringUtil.isEmpty(changeLog))
                        changeLogArea.setText("no change log");
                    else
                        changeLogArea.setText(changeLog);
                    changeLogArea.setCaretPosition(0);
                    changeLogTitleLabel.setText(plugin.getName() + " change log");
                });
            }
        });
        pluginList.setSelectionInterval(0, toInstallPlugins.size() - 1);

        getOkBtn().setText("Update");
        getCancelBtn().setText("Close");
        setCloseAfterAction(false);
        // launch update
        setOkAction(e -> doUpdate());

        final JScrollPane medScrollPane = new JScrollPane(pluginList);
        final JScrollPane changeLogScrollPane = new JScrollPane(GuiUtil.createTabArea(changeLogArea, 4));
        final JPanel bottomPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4), GuiUtil.createCenteredLabel(changeLogTitleLabel), Box.createVerticalStrut(4), changeLogScrollPane);

        final JPanel mainPanel = getMainPanel();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, medScrollPane, bottomPanel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        pack();
        addToDesktopPane();
        setVisible(true);
        center();
        requestFocus();

        // set splitter to middle
        splitPane.setDividerLocation(0.5d);
    }

    /**
     * update selected plugins
     */
    protected void doUpdate() {

        final ArrayList<PluginDescriptor> plugins = new ArrayList<>(pluginList.getSelectedValuesList());

        for (final PluginDescriptor plugin : plugins)
            listModel.removeElement(plugin);

        // no more plugin to update ? close frame
        if (listModel.isEmpty())
            close();

        // process plugins update in background
        ThreadUtil.bgRun(() -> {
            if (!plugins.isEmpty())
                PluginUpdater.updatePlugins(plugins, true);
        });
    }
}
