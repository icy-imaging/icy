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

import org.bioimageanalysis.icy.gui.component.panel.AbstractRoisPanel.BaseColumnInfo;
import org.bioimageanalysis.icy.gui.component.button.IcyButton;
import org.bioimageanalysis.icy.system.preferences.XMLPreferences;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.roi.ROIDescriptor;
import org.bioimageanalysis.icy.model.roi.ROIUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class RoiSettingPanel extends JPanel implements ActionListener, ItemListener {
    // GUI
    private JScrollPane scrollPaneView;
    private JScrollPane scrollPaneExport;
    private JTable tableView;
    private JTable tableExport;
    private JPanel panelExportTop;
    private JCheckBox chkHeaderSelectAllToDisplay;
    private JCheckBox chkHeaderSelectAllToExport;
    private JToggleButton btnHeaderColumnsToDisplay;
    private JToggleButton btnHeaderColumnsToExport;
    private IcyButton btnUpView;
    private IcyButton btnDownView;
    private IcyButton btnUpExport;
    private IcyButton btnDownExport;

    // internals
    List<BaseColumnInfo> idsView;
    List<BaseColumnInfo> idsExport;

    private final AbstractTableModel viewModel;
    private final AbstractTableModel exportModel;

    private final XMLPreferences prefView;
    private final XMLPreferences prefExport;

    /**
     * Create the panel.
     */
    public RoiSettingPanel(final XMLPreferences viewPreferences, final XMLPreferences exportPreferences) {
        super();

        prefView = viewPreferences;
        prefExport = exportPreferences;

        final Set<ROIDescriptor> descriptors = ROIUtil.getROIDescriptors().keySet();

        idsView = new ArrayList<>();
        idsExport = new ArrayList<>();

        // build view and export lists
        for (final ROIDescriptor descriptor : descriptors) {
            idsView.add(new BaseColumnInfo(descriptor, prefView, false));
            idsExport.add(new BaseColumnInfo(descriptor, prefExport, true));
        }

        sortLists();

        initialize();

        viewModel = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        // name
                        return String.class;

                    case 1:
                        // visibility
                        return Boolean.class;
                }

                return String.class;
            }

            @Override
            public String getColumnName(final int column) {
                switch (column) {
                    case 0:
                        return "Column name";

                    case 1:
                        return "Visible";
                }

                return "";
            }

            @Override
            public int getRowCount() {
                return idsView.size();
            }

            @Override
            public Object getValueAt(final int rowIndex, final int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        // name
                        return idsView.get(rowIndex).descriptor.getName();

                    case 1:
                        // visibility
                        return Boolean.valueOf(idsView.get(rowIndex).visible);
                }

                return null;
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return (columnIndex == 1);
            }

            @Override
            public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
                // visibility
                if (columnIndex == 1)
                    idsView.get(rowIndex).visible = ((Boolean) aValue).booleanValue();
            }
        };

        exportModel = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        // name
                        return String.class;

                    case 1:
                        // visibility
                        return Boolean.class;
                }

                return String.class;
            }

            @Override
            public String getColumnName(final int column) {
                switch (column) {
                    case 0:
                        return "Column name";

                    case 1:
                        return "Visible";
                }

                return "";
            }

            @Override
            public int getRowCount() {
                return idsExport.size();
            }

            @Override
            public Object getValueAt(final int rowIndex, final int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        // name
                        return idsExport.get(rowIndex).descriptor.getName();

                    case 1:
                        // visibility
                        return Boolean.valueOf(idsExport.get(rowIndex).visible);
                }

                return null;
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return (columnIndex == 1);
            }

            @Override
            public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
                // visibility
                if (columnIndex == 1)
                    idsExport.get(rowIndex).visible = ((Boolean) aValue).booleanValue();
            }
        };

        TableColumnModel columnModel;
        TableColumn column;

        tableView.setModel(viewModel);
        columnModel = tableView.getColumnModel();

        column = columnModel.getColumn(0);
        column.setPreferredWidth(150);
        column.setMinWidth(80);
        column = columnModel.getColumn(1);
        column.setResizable(false);
        column.setPreferredWidth(50);
        column.setMaxWidth(50);
        column.setMinWidth(30);

        tableExport.setModel(exportModel);
        columnModel = tableExport.getColumnModel();

        column = columnModel.getColumn(0);
        column.setPreferredWidth(150);
        column.setMinWidth(80);
        column = columnModel.getColumn(1);
        column.setResizable(false);
        column.setPreferredWidth(50);
        column.setMaxWidth(50);
        column.setMinWidth(30);

        chkHeaderSelectAllToDisplay.addItemListener(this);
        btnHeaderColumnsToDisplay.addItemListener(this);
        btnUpView.addActionListener(this);
        btnDownView.addActionListener(this);

        chkHeaderSelectAllToExport.addItemListener(this);
        btnHeaderColumnsToExport.addItemListener(this);
        btnUpExport.addActionListener(this);
        btnDownExport.addActionListener(this);

    }

    private void initialize() {
        setLayout(new BorderLayout(0, 0));

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        final JPanel panelView = new JPanel();
        splitPane.setLeftComponent(panelView);
        panelView.setLayout(new BorderLayout(0, 0));

        scrollPaneView = new JScrollPane();
        panelView.add(scrollPaneView, BorderLayout.CENTER);

        tableView = new JTable();
        tableView.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableView.setRowSelectionAllowed(true);
        scrollPaneView.setViewportView(tableView);

        final JPanel panelViewTop = new JPanel();
        panelView.add(panelViewTop, BorderLayout.NORTH);
        final GridBagLayout gbl_panelViewTop = new GridBagLayout();
        gbl_panelViewTop.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_panelViewTop.rowHeights = new int[]{14, 0};
        gbl_panelViewTop.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelViewTop.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panelViewTop.setLayout(gbl_panelViewTop);

        chkHeaderSelectAllToDisplay = new JCheckBox();
        chkHeaderSelectAllToDisplay.setToolTipText("Click to select/deselect all descriptors in this list");
        final GridBagConstraints gbc_chkHeaderSelectAllToDisplay = new GridBagConstraints();
        gbc_chkHeaderSelectAllToDisplay.insets = new Insets(0, 0, 0, 0);
        gbc_chkHeaderSelectAllToDisplay.gridx = 0;
        gbc_chkHeaderSelectAllToDisplay.gridy = 0;
        panelViewTop.add(chkHeaderSelectAllToDisplay, gbc_chkHeaderSelectAllToDisplay);

        btnHeaderColumnsToDisplay = new JToggleButton("Columns to display");
        btnHeaderColumnsToDisplay.setToolTipText("Click to alphabetically reorder descriptors in this list");
        final GridBagConstraints gbc_btnHeaderColumnsToDisplay = new GridBagConstraints();
        gbc_btnHeaderColumnsToDisplay.insets = new Insets(0, 0, 0, 0);
        gbc_btnHeaderColumnsToDisplay.gridx = 1;
        gbc_btnHeaderColumnsToDisplay.gridy = 0;
        panelViewTop.add(btnHeaderColumnsToDisplay, gbc_btnHeaderColumnsToDisplay);

        btnUpView = new IcyButton(SVGIcon.KEYBOARD_ARROW_UP);
        btnUpView.setToolTipText("Change order of selected column(s)");
        final GridBagConstraints gbc_btnUpView = new GridBagConstraints();
        gbc_btnUpView.insets = new Insets(0, 0, 0, 5);
        gbc_btnUpView.gridx = 2;
        gbc_btnUpView.gridy = 0;
        panelViewTop.add(btnUpView, gbc_btnUpView);

        btnDownView = new IcyButton(SVGIcon.KEYBOARD_ARROW_DOWN);
        btnDownView.setToolTipText("Change order of selected column(s)");
        final GridBagConstraints gbc_btnDownView = new GridBagConstraints();
        gbc_btnDownView.gridx = 3;
        gbc_btnDownView.gridy = 0;
        panelViewTop.add(btnDownView, gbc_btnDownView);

        final JPanel panelExport = new JPanel();
        splitPane.setRightComponent(panelExport);
        panelExport.setLayout(new BorderLayout(0, 0));

        scrollPaneExport = new JScrollPane();
        panelExport.add(scrollPaneExport, BorderLayout.CENTER);

        tableExport = new JTable();
        tableExport.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableExport.setRowSelectionAllowed(true);
        scrollPaneExport.setViewportView(tableExport);

        panelExportTop = new JPanel();
        panelExport.add(panelExportTop, BorderLayout.NORTH);
        final GridBagLayout gbl_panelExportTop = new GridBagLayout();
        gbl_panelExportTop.columnWidths = new int[]{0, 0, 0, 0, 0};
        gbl_panelExportTop.rowHeights = new int[]{14, 0};
        gbl_panelExportTop.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_panelExportTop.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panelExportTop.setLayout(gbl_panelExportTop);

        chkHeaderSelectAllToExport = new JCheckBox();
        chkHeaderSelectAllToExport.setToolTipText("Click to select/deselect all descriptors in this list");
        final GridBagConstraints gbc_chkHeaderSelectAllToExport = new GridBagConstraints();
        gbc_chkHeaderSelectAllToExport.insets = new Insets(0, 0, 0, 0);
        gbc_chkHeaderSelectAllToExport.gridx = 0;
        gbc_chkHeaderSelectAllToExport.gridy = 0;
        panelExportTop.add(chkHeaderSelectAllToExport, gbc_chkHeaderSelectAllToExport);

        btnHeaderColumnsToExport = new JToggleButton("Columns to export (XLS or CSV)");
        btnHeaderColumnsToExport.setToolTipText("Click to alphabetically reorder descriptors in this list");
        final GridBagConstraints gbc_btnHeaderColumnsToExport = new GridBagConstraints();
        gbc_btnHeaderColumnsToExport.insets = new Insets(0, 0, 0, 0);
        gbc_btnHeaderColumnsToExport.gridx = 1;
        gbc_btnHeaderColumnsToExport.gridy = 0;
        panelExportTop.add(btnHeaderColumnsToExport, gbc_btnHeaderColumnsToExport);

        btnUpExport = new IcyButton(SVGIcon.KEYBOARD_ARROW_UP);
        btnUpExport.setToolTipText("Change order of selected column(s)");
        final GridBagConstraints gbc_btnUpExport = new GridBagConstraints();
        gbc_btnUpExport.insets = new Insets(0, 0, 0, 5);
        gbc_btnUpExport.gridx = 2;
        gbc_btnUpExport.gridy = 0;
        panelExportTop.add(btnUpExport, gbc_btnUpExport);

        btnDownExport = new IcyButton(SVGIcon.KEYBOARD_ARROW_DOWN);
        btnDownExport.setToolTipText("Change order of selected column(s)");
        final GridBagConstraints gbc_btnDownExport = new GridBagConstraints();
        gbc_btnDownExport.gridx = 3;
        gbc_btnDownExport.gridy = 0;
        panelExportTop.add(btnDownExport, gbc_btnDownExport);
    }

    void fixOrders() {
        int order;

        order = 0;
        for (final BaseColumnInfo columnInfo : idsView)
            columnInfo.order = order++;
        order = 0;
        for (final BaseColumnInfo columnInfo : idsExport)
            columnInfo.order = order++;
    }

    /**
     * Sort lists on their order
     */
    void sortLists() {
        // sort tables
        Collections.sort(idsView);
        Collections.sort(idsExport);
        // and fix orders
        fixOrders();
    }

    /**
     * Save columns setting to preferences
     */
    public void save() {
        sortLists();

        for (final BaseColumnInfo columnInfo : idsView)
            columnInfo.save(prefView);
        for (final BaseColumnInfo columnInfo : idsExport)
            columnInfo.save(prefExport);
    }

    List<BaseColumnInfo> getSelected(final JTable table, final List<BaseColumnInfo> columnInfos) {
        final List<BaseColumnInfo> result = new ArrayList<>();
        final int[] selected = table.getSelectedRows();

        for (final int index : selected)
            result.add(columnInfos.get(index));

        return result;
    }

    void restoreSelected(final JTable table, final List<BaseColumnInfo> columnInfos, final List<BaseColumnInfo> selected) {
        final ListSelectionModel selectionModel = table.getSelectionModel();

        selectionModel.setValueIsAdjusting(true);
        try {
            selectionModel.clearSelection();

            for (final BaseColumnInfo bci : selected) {
                final int index = columnInfos.indexOf(bci);
                if (index >= 0)
                    selectionModel.addSelectionInterval(index, index);
            }
        }
        finally {
            selectionModel.setValueIsAdjusting(false);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object source = e.getSource();

        if (source == btnUpView || source == btnDownView || source == btnUpExport || source == btnDownExport) {
            moveTableItem(e);
        }
    }

    private void moveTableItem(final ActionEvent e) {
        final Object source = e.getSource();
        final JTable table;
        final List<BaseColumnInfo> columnInfos;
        final int v;

        if ((source == btnUpView) || (source == btnDownView)) {
            table = tableView;
            columnInfos = idsView;
        }
        else if ((source == btnUpExport) || (source == btnDownExport)) {
            table = tableExport;
            columnInfos = idsExport;
        }
        else {
            table = null;
            columnInfos = null;
        }

        if ((source == btnUpView) || (source == btnUpExport))
            v = -1;
        else if ((source == btnDownView) || (source == btnDownExport))
            v = 1;
        else
            v = 0;

        if ((table != null) && (columnInfos != null)) {
            final List<BaseColumnInfo> selected = getSelected(table, columnInfos);

            // update order of selected area
            for (final BaseColumnInfo bci : selected)
                bci.order = bci.order + v;

            if (v == -1) {
                // change order of previous item
                final int firstSelected = table.getSelectionModel().getMinSelectionIndex();
                //if ((firstSelected != -1) && (firstSelected > 0))
                if (firstSelected > 0)
                    columnInfos.get(firstSelected - 1).order += table.getSelectedRowCount();
            }
            else {
                // change order of next item
                final int lastSelected = table.getSelectionModel().getMaxSelectionIndex();
                if ((lastSelected != -1) && (lastSelected < (columnInfos.size() - 1)))
                    columnInfos.get(lastSelected + 1).order -= table.getSelectedRowCount();
            }

            // sort lists
            sortLists();

            restoreSelected(table, columnInfos, selected);
            // refresh table data
            if (source == btnUpView || source == btnDownView)
                viewModel.fireTableRowsUpdated(0, idsView.size() - 1);
            else if (source == btnUpExport || source == btnDownExport)
                exportModel.fireTableRowsUpdated(0, idsExport.size() - 1);
        }
    }

    @Override
    public void itemStateChanged(final ItemEvent e) {
        final Object source = e.getSource();
        if (source == chkHeaderSelectAllToDisplay || source == chkHeaderSelectAllToExport) {
            toggleSelectAll(e);
        }
        else if (source == btnHeaderColumnsToDisplay || source == btnHeaderColumnsToExport) {
            sortTableElements(e);
        }
    }

    private void toggleSelectAll(final ItemEvent e) {
        final Object source = e.getSource();
        final JTable table;
        final List<BaseColumnInfo> columnInfos;
        final boolean selectionValue;

        if (source == chkHeaderSelectAllToDisplay) {
            table = tableView;
            columnInfos = idsView;
            selectionValue = chkHeaderSelectAllToDisplay.isSelected();
        }
        else if (source == chkHeaderSelectAllToExport) {
            table = tableExport;
            columnInfos = idsExport;
            selectionValue = chkHeaderSelectAllToExport.isSelected();
        }
        else {
            table = null;
            columnInfos = null;
            selectionValue = false;
        }

        if (table != null && columnInfos != null) {
            for (int j = 0; j < table.getRowCount(); j++) {
                table.setValueAt(selectionValue, j, 1);
            }

            // refresh table data
            if (source == chkHeaderSelectAllToDisplay)
                viewModel.fireTableRowsUpdated(0, idsView.size() - 1);
            else if (source == chkHeaderSelectAllToExport)
                exportModel.fireTableRowsUpdated(0, idsExport.size() - 1);
        }
    }

    private void sortTableElements(final ItemEvent e) {
        final Object source = e.getSource();
        final JTable table;
        final List<BaseColumnInfo> columnInfos;
        final boolean orderIncremental;

        if (source == btnHeaderColumnsToDisplay) {
            table = tableView;
            columnInfos = idsView;
            orderIncremental = btnHeaderColumnsToDisplay.isSelected();
        }
        else if (source == btnHeaderColumnsToExport) {
            table = tableExport;
            columnInfos = idsExport;
            orderIncremental = btnHeaderColumnsToExport.isSelected();
        }
        else {
            table = null;
            columnInfos = null;
            orderIncremental = false;
        }

        if (table != null && columnInfos != null) {
            final List<BaseColumnInfo> selected = getSelected(table, columnInfos);
            final List<BaseColumnInfo> included = getIncluded(table, columnInfos);
            final List<BaseColumnInfo> notIncluded = getNotIncluded(table, columnInfos);
            included.sort(Comparator.comparingInt(info -> info.order));

            for (int i = 0; i < included.size(); i++) {
                included.get(i).order = i;
            }
            // TODO: 25/01/2023 Check if same result
            //notIncluded.sort((orderIncremental ? Comparator.comparing(i -> i.descriptor.getName().toLowerCase()) : Comparator.<BaseColumnInfo, String> comparing(i -> i.descriptor.getName().toLowerCase()).reversed()));
            if (orderIncremental) notIncluded.sort(Comparator.comparing(i -> i.descriptor.getName().toLowerCase()));
            else
                notIncluded.sort(Comparator.<BaseColumnInfo, String>comparing(i -> i.descriptor.getName().toLowerCase()).reversed());

            for (int i = 0; i < notIncluded.size(); i++) {
                notIncluded.get(i).order = included.size() + i;
            }

            // sort lists
            sortLists();

            restoreSelected(table, columnInfos, selected);
            if (source == btnHeaderColumnsToDisplay)
                viewModel.fireTableRowsUpdated(0, idsView.size() - 1);
            else if (source == btnHeaderColumnsToExport)
                exportModel.fireTableRowsUpdated(0, idsExport.size() - 1);
        }
    }

    private List<BaseColumnInfo> getIncluded(final JTable table, final List<BaseColumnInfo> columnInfos) {
        final List<BaseColumnInfo> result = new ArrayList<>();
        final int[] included = getIncludedIndices(table);

        for (final int index : included)
            result.add(columnInfos.get(index));

        return result;
    }

    private int[] getIncludedIndices(final JTable table) {
        final int[] included = new int[table.getRowCount()];
        int pos = 0;
        for (int j = 0; j < table.getRowCount(); j++) {
            if ((Boolean) table.getValueAt(j, 1)) {
                included[pos++] = j;
            }
        }
        return Arrays.copyOf(included, pos);
    }

    private List<BaseColumnInfo> getNotIncluded(final JTable table, final List<BaseColumnInfo> columnInfos) {
        final List<BaseColumnInfo> result = new ArrayList<>();
        final int[] notIncluded = getNotIncludedIndices(table);

        for (final int index : notIncluded)
            result.add(columnInfos.get(index));

        return result;
    }

    private int[] getNotIncludedIndices(final JTable table) {
        final int[] notIncluded = new int[table.getRowCount()];
        int pos = 0;
        for (int j = 0; j < table.getRowCount(); j++) {
            if (!(Boolean) table.getValueAt(j, 1)) {
                notIncluded[pos++] = j;
            }
        }
        return Arrays.copyOf(notIncluded, pos);
    }
}
