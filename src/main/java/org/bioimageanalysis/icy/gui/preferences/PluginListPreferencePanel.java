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

package org.bioimageanalysis.icy.gui.preferences;

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.gui.component.table.IcyTable;
import org.bioimageanalysis.icy.gui.component.field.IcyTextField;
import org.bioimageanalysis.icy.gui.component.field.IcyTextField.TextChangeListener;
import org.bioimageanalysis.icy.gui.plugin.PluginDetailPanel;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences.RepositoryInfo;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class PluginListPreferencePanel extends PreferencePanel implements TextChangeListener, ListSelectionListener {
    static final String[] columnNames = {"", "Extension", "Name", "Version", "State", "Enabled"};
    static final String[] columnIds = {"Icon", "Extension", "Name", "Version", "State", "Enabled"};

    List<PluginDescriptor> plugins;

    /**
     * gui
     */
    final AbstractTableModel tableModel;
    final JTable table;

    final JComboBox<RepositoryInfo> repository;
    final JPanel repositoryPanel;
    final IcyTextField filter;
    final JButton refreshButton;
    final JButton documentationButton;
    final JButton detailButton;
    final JButton action1Button;
    final JButton action2Button;

    private final Runnable buttonsStateUpdater;
    private final Runnable tableDataRefresher;
    private final Runnable pluginsListRefresher;
    private final Runnable repositoriesUpdater;

    final ActionListener repositoryActionListener;

    PluginListPreferencePanel(final PreferenceFrame parent, final String nodeName, final String parentName) {
        super(parent, nodeName, parentName);

        plugins = new ArrayList<>();

        buttonsStateUpdater = () -> {
            // need to be done on EDT
            ThreadUtil.invokeNow(this::updateButtonsStateInternal);
        };
        tableDataRefresher = () -> {
            // need to be done on EDT
            ThreadUtil.invokeNow(this::refreshTableDataInternal);
        };
        pluginsListRefresher = this::refreshPluginsInternal;
        repositoriesUpdater = () -> {
            // need to be done on EDT
            ThreadUtil.invokeNow(this::updateRepositoriesInternal);
        };
        repositoryActionListener = e -> repositoryChanged();

        repository = new JComboBox<>();
        repository.setToolTipText("Select a repository");
        repository.addActionListener(repositoryActionListener);

        repositoryPanel = new JPanel();
        repositoryPanel.setLayout(new BoxLayout(repositoryPanel, BoxLayout.PAGE_AXIS));
        repositoryPanel.setVisible(false);

        final JPanel internalRepPanel = new JPanel();
        internalRepPanel.setLayout(new BoxLayout(internalRepPanel, BoxLayout.LINE_AXIS));

        internalRepPanel.add(new JLabel("Repository :"));
        internalRepPanel.add(Box.createHorizontalStrut(8));
        internalRepPanel.add(repository);
        internalRepPanel.add(Box.createHorizontalGlue());

        repositoryPanel.add(internalRepPanel);
        repositoryPanel.add(Box.createVerticalStrut(8));

        // need filter before load()
        filter = new IcyTextField();
        filter.addTextChangeListener(this);

        // build buttons panel
        final Dimension buttonsDim = new Dimension(100, 24);

        refreshButton = new JButton("Reload list");
        refreshButton.addActionListener(e -> reloadPlugins());
        ComponentUtil.setFixedSize(refreshButton, buttonsDim);

        documentationButton = new JButton("Online doc");
        documentationButton.setToolTipText("Open the online documentation");
        documentationButton.addActionListener(e -> {
            final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();

            // open plugin web page
            if (selectedPlugins.size() == 1)
                NetworkUtil.openBrowser(selectedPlugins.get(0).getWeb());
        });
        ComponentUtil.setFixedSize(documentationButton, buttonsDim);

        detailButton = new JButton("Show detail");
        detailButton.addActionListener(e -> {
            final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();

            // open the detail
            if (selectedPlugins.size() == 1)
                new PluginDetailPanel(selectedPlugins.get(0));
        });
        ComponentUtil.setFixedSize(detailButton, buttonsDim);

        action1Button = new JButton("null");
        action1Button.addActionListener(e -> doAction1());
        action1Button.setVisible(false);
        ComponentUtil.setFixedSize(action1Button, buttonsDim);

        action2Button = new JButton("null");
        action2Button.addActionListener(e -> doAction2());
        action2Button.setVisible(false);
        ComponentUtil.setFixedSize(action2Button, buttonsDim);

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(34));
        buttonsPanel.add(documentationButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(detailButton);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(action1Button);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(action2Button);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(Box.createVerticalGlue());

        // build table
        tableModel = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public String getColumnName(final int column) {
                return columnNames[column];
            }

            @Override
            public int getRowCount() {
                return plugins.size();
            }

            @Override
            public Object getValueAt(final int row, final int column) {
                if (row < plugins.size()) {
                    final PluginDescriptor plugin = plugins.get(row);

                    switch (column) {
                        case 0: {
                            if (plugin.isIconLoaded())
                                return plugin.getSVG().getIcon(30, LookAndFeelUtil.ColorType.BUTTON_DEFAULT);

                            loadIconAsync(plugin);
                            return new IcySVG(SVGResource.HOURGLASS).getIcon(30, LookAndFeelUtil.ColorType.BUTTON_DEFAULT);
                        }

                        case 1:
                            return plugin.getExtension().getName();

                        case 2:
                            return plugin.getName();

                        case 3:
                            return plugin.getVersion().toShortString();

                        case 4:
                            return getStateValue(plugin);

                        case 5:
                            return Boolean.valueOf(isActive(plugin));
                    }
                }

                return "";
            }

            @Override
            public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
                if (rowIndex < plugins.size()) {
                    final PluginDescriptor plugin = plugins.get(rowIndex);

                    if (columnIndex == 5) {
                        if (aValue instanceof Boolean)
                            setActive(plugin, ((Boolean) aValue).booleanValue());
                    }
                }
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return (column == 5);
            }

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> ImageIcon.class;
                    case 5 -> Boolean.class;
                    default -> String.class;
                };
            }
        };

        table = new IcyTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setIdentifier(columnIds[0]);
        col.setMinWidth(32);
        col.setPreferredWidth(32);
        col.setMaxWidth(32);

        col = colModel.getColumn(1);
        col.setIdentifier(columnIds[1]);
        col.setMinWidth(60);
        col.setPreferredWidth(80);
        col.setMaxWidth(200);

        col = colModel.getColumn(2);
        col.setIdentifier(columnIds[2]);
        col.setMinWidth(120);
        col.setPreferredWidth(200);
        col.setMaxWidth(500);

        col = colModel.getColumn(3);
        col.setIdentifier(columnIds[3]);
        col.setMinWidth(50);
        col.setPreferredWidth(100);
        col.setMaxWidth(200);

        col = colModel.getColumn(4);
        col.setIdentifier(columnIds[4]);
        col.setMinWidth(70);
        col.setPreferredWidth(90);
        col.setMaxWidth(120);

        col = colModel.getColumn(5);
        col.setIdentifier(columnIds[5]);
        col.setMinWidth(60);
        col.setPreferredWidth(60);
        col.setMaxWidth(60);

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowHeight(32);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        // sort on name by default
        table.getRowSorter().toggleSortOrder(1);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent me) {
                if (!me.isConsumed()) {
                    if (me.getClickCount() == 2) {
                        // show detail
                        detailButton.doClick();
                        me.consume();
                    }
                }
            }
        });

        final JPanel tableTopPanel = new JPanel();

        tableTopPanel.setLayout(new BoxLayout(tableTopPanel, BoxLayout.PAGE_AXIS));

        tableTopPanel.add(Box.createVerticalStrut(2));
        tableTopPanel.add(repositoryPanel);
        tableTopPanel.add(filter);
        tableTopPanel.add(Box.createVerticalStrut(8));
        tableTopPanel.add(table.getTableHeader());

        final JPanel tablePanel = new JPanel();

        tablePanel.setLayout(new BorderLayout());

        tablePanel.add(tableTopPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.EAST);

        mainPanel.validate();
    }

    protected void loadIconAsync(final PluginDescriptor plugin) {
        ThreadUtil.bgRun(() -> {
            // icon correctly loaded ?
            if (plugin.loadSVG())
                refreshTableData();
        });
    }

    @Override
    protected void closed() {
        super.closed();

        // do not retains plugins when frame is closed
        plugins.clear();
    }

    private List<PluginDescriptor> filterList(final List<PluginDescriptor> list, final String filter) {
        final List<PluginDescriptor> result = new ArrayList<>();
        final boolean empty = StringUtil.isEmpty(filter, true);
        final String filterUp;

        if (!empty)
            filterUp = filter.toUpperCase();
        else
            filterUp = "";

        for (final PluginDescriptor plugin : list) {
            final String classname = plugin.getClassName().toUpperCase();
            final String name = plugin.getName().toUpperCase();
            final String desc = plugin.getDescription().toUpperCase();

            // search in name and description
            if (empty || (classname.contains(filterUp)) || (name.contains(filterUp)) || (desc.contains(filterUp)))
                result.add(plugin);
        }

        return result;
    }

    protected boolean isActive(final PluginDescriptor plugin) {
        return false;
    }

    protected void setActive(final PluginDescriptor plugin, final boolean value) {
    }

    protected abstract void doAction1();

    protected abstract void doAction2();

    protected abstract void repositoryChanged();

    protected abstract void reloadPlugins();

    protected abstract String getStateValue(PluginDescriptor plugin);

    protected abstract List<PluginDescriptor> getPlugins();

    protected int getPluginTableIndex(final int rowIndex) {
        if (rowIndex == -1)
            return rowIndex;

        try {

            return table.convertRowIndexToView(rowIndex);
        }
        catch (final IndexOutOfBoundsException e) {
            return -1;
        }
    }

    protected int getPluginIndex(final PluginDescriptor plugin) {
        return plugins.indexOf(plugin);
    }

    protected int getPluginModelIndex(final PluginDescriptor plugin) {
        return getPluginIndex(plugin);
    }

    protected int getPluginTableIndex(final PluginDescriptor plugin) {
        return getPluginTableIndex(getPluginModelIndex(plugin));
    }

    protected int getPluginIndex(final String pluginClassName) {
        for (int i = 0; i < plugins.size(); i++) {
            final PluginDescriptor plugin = plugins.get(i);

            if (plugin.getClassName().equals(pluginClassName))
                return i;
        }

        return -1;
    }

    protected int getPluginModelIndex(final String pluginClassName) {
        return getPluginIndex(pluginClassName);
    }

    protected int getPluginTableIndex(final String pluginClassName) {
        return getPluginTableIndex(getPluginModelIndex(pluginClassName));
    }

    List<PluginDescriptor> getSelectedPlugins() {
        final List<PluginDescriptor> result = new ArrayList<>();

        final int[] rows = table.getSelectedRows();
        if (rows.length == 0)
            return result;

        final List<PluginDescriptor> cachedPlugins = plugins;

        for (final int row : rows) {
            try {
                final int index = table.convertRowIndexToModel(row);
                if (index < cachedPlugins.size())
                    result.add(cachedPlugins.get(index));
            }
            catch (final IndexOutOfBoundsException e) {
                // ignore as async process can cause it
            }
        }

        return result;
    }

    /**
     * Select the specified list of ROI in the ROI Table
     */
    void setSelectedPlugins(final HashSet<PluginDescriptor> newSelected) {
        final List<PluginDescriptor> modelPlugins = plugins;
        final ListSelectionModel selectionModel = table.getSelectionModel();

        // start selection change
        selectionModel.setValueIsAdjusting(true);
        try {
            // start by clearing selection
            selectionModel.clearSelection();

            for (int i = 0; i < modelPlugins.size(); i++) {
                final PluginDescriptor plugin = modelPlugins.get(i);

                // HashSet provide fast "contains"
                if (newSelected.contains(plugin)) {
                    try {
                        // convert model index to view index
                        final int ind = table.convertRowIndexToView(i);
                        if (ind != -1)
                            selectionModel.addSelectionInterval(ind, ind);
                    }
                    catch (final IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }
        }
        finally {
            // end selection change
            selectionModel.setValueIsAdjusting(false);
        }
    }

    protected void refreshPluginsInternal() {
        plugins = filterList(getPlugins(), filter.getText());
        // refresh table data
        refreshTableData();
    }

    protected final void refreshPlugins() {
        ThreadUtil.runSingle(pluginsListRefresher);
    }

    protected void updateButtonsStateInternal() {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final boolean singleSelection = (selectedPlugins.size() == 1);
        final PluginDescriptor singlePlugin = singleSelection ? selectedPlugins.get(0) : null;

        detailButton.setEnabled(singleSelection);
        documentationButton.setEnabled(singleSelection && !StringUtil.isEmpty(singlePlugin.getWeb()));
    }

    protected final void updateButtonsState() {
        ThreadUtil.runSingle(buttonsStateUpdater);
    }

    protected void updateRepositoriesInternal() {
        // final RepositoryPreferencePanel panel = (RepositoryPreferencePanel)
        // getPreferencePanel(RepositoryPreferencePanel.class);
        // // refresh repositories list (use list from GUI)
        // final ArrayList<RepositoryInfo> repositeries = panel.repositories;

        // refresh repositories list
        final List<RepositoryInfo> repositeries = RepositoryPreferences.getRepositeries();
        final RepositoryInfo savedRepository = (RepositoryInfo) repository.getSelectedItem();

        // needed to disable events during update time
        repository.removeActionListener(repositoryActionListener);

        repository.removeAllItems();
        for (final RepositoryInfo repos : repositeries)
            if (repos.isEnabled())
                repository.addItem(repos);

        repository.addActionListener(repositoryActionListener);

        boolean selected = false;

        // try to set back the old selected repository
        if (savedRepository != null) {
            final String repositoryName = savedRepository.getName();

            for (int ind = 0; ind < repository.getItemCount(); ind++) {
                final RepositoryInfo repo = repository.getItemAt(ind);

                if ((repo != null) && (repo.getName().equals(repositoryName))) {
                    repository.setSelectedIndex(ind);
                    selected = true;
                    break;
                }
            }
        }

        // manually launch the action
        if (!selected)
            repository.setSelectedIndex((repository.getItemCount() > 0) ? 0 : -1);

        // avoid automatic minimum size here
        repository.setMinimumSize(new Dimension(48, 18));
    }

    protected final void updateRepositories() {
        ThreadUtil.runSingle(repositoriesUpdater);
    }

    protected void refreshTableDataInternal() {
        // TODO
        /*final List<ExtensionDescriptor> extensions = ExtensionLoader.getExtensions();
        for (final ExtensionDescriptor extension : extensions) {
            IcyLogger.debug(this.getClass(), "Extension: " + extension.getName());
            IcyLogger.debug(this.getClass(), "Extension version: " + extension.getVersion());
            IcyLogger.debug(this.getClass(), "Extension plugins: " + extension.getPlugins().size());
        }*/

        final List<PluginDescriptor> plugins = getSelectedPlugins();

        try {
            tableModel.fireTableDataChanged();
        }
        catch (final Throwable t) {
            // sometime sorting can throw exception, ignore them...
        }

        // restore previous selected plugins if possible
        setSelectedPlugins(new HashSet<>(plugins));
        // update button state
        buttonsStateUpdater.run();
    }

    protected final void refreshTableData() {
        ThreadUtil.runSingle(tableDataRefresher);
    }

    protected void pluginsChanged() {
        refreshPlugins();
    }

    @Override
    protected void load() {

    }

    @Override
    protected void save() {
        // reload repositories as some parameter as beta flag can have changed
        updateRepositories();
    }

    @Override
    public void textChanged(final IcyTextField source, final boolean validate) {
        pluginsChanged();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        final int selected = table.getSelectedRow();

        if (!e.getValueIsAdjusting() && (selected != -1)) {
            final int fi = e.getFirstIndex();
            final int li = e.getLastIndex();

            if ((fi == -1) || ((fi <= selected) && (li >= selected)))
                updateButtonsState();
        }
    }
}
