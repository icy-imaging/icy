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

package icy.gui.inspector;

import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.image.ImageUtil;
import icy.main.Icy;
import icy.swimmingPool.SwimmingObject;
import icy.swimmingPool.SwimmingPool;
import icy.swimmingPool.SwimmingPoolEvent;
import icy.swimmingPool.SwimmingPoolListener;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.ArrayList;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SwimmingPoolPanel extends JPanel implements TextChangeListener, ListSelectionListener, SwimmingPoolListener {
    static final String[] columnNames = {"", "Name", "Type"};

    final SwimmingPool swimmingPool;
    ArrayList<SwimmingObject> objects;

    final AbstractTableModel tableModel;
    final JTable table;

    final JComboBox<Object> objectType;
    final JPanel objectTypePanel;
    final IcyTextField nameFilter;
    final JButton refreshButton;
    final JButton deleteAllButton;
    final JButton deleteButton;

    public SwimmingPoolPanel(final boolean showTypeFilter, final boolean showNameFilter, final boolean showButtons) {
        super();

        swimmingPool = Icy.getMainInterface().getSwimmingPool();
        if (swimmingPool != null)
            swimmingPool.addListener(this);

        objects = new ArrayList<>();

        // GUI

        objectType = new JComboBox<>(new DefaultComboBoxModel<>());
        objectType.setToolTipText("Select type to display");
        objectType.addActionListener(e -> {
            if (objectType.getSelectedIndex() != -1) {
                refreshObjects();
                refreshTableData();
            }
        });

        objectTypePanel = new JPanel();
        objectTypePanel.setLayout(new BoxLayout(objectTypePanel, BoxLayout.PAGE_AXIS));
        objectTypePanel.setVisible(showTypeFilter);

        final JPanel internalRepPanel = new JPanel();
        internalRepPanel.setLayout(new BoxLayout(internalRepPanel, BoxLayout.LINE_AXIS));

        internalRepPanel.add(new JLabel("Object type :"));
        internalRepPanel.add(Box.createHorizontalStrut(8));
        internalRepPanel.add(objectType);
        internalRepPanel.add(Box.createHorizontalGlue());

        objectTypePanel.add(internalRepPanel);
        objectTypePanel.add(Box.createVerticalStrut(8));

        // need filter before load()
        nameFilter = new IcyTextField();
        nameFilter.addTextChangeListener(this);
        nameFilter.setVisible(showNameFilter);

        // build buttons panel

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // refresh list
            refreshObjectTypeList();
            refreshObjects();
        });

        deleteAllButton = new JButton("Delete all");
        deleteAllButton.addActionListener(e -> {
            // delete all objects
            if (swimmingPool != null)
                swimmingPool.removeAll();
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            // delete selected objects
            if (swimmingPool != null)
                for (final SwimmingObject so : getSelectedObjects())
                    swimmingPool.remove(so);
        });

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.setVisible(showButtons);

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createHorizontalStrut(64));
        buttonsPanel.add(deleteAllButton);
        buttonsPanel.add(Box.createHorizontalStrut(8));
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(Box.createHorizontalGlue());

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
                return objects.size();
            }

            @Override
            public Object getValueAt(final int row, final int column) {
                final SwimmingObject so = objects.get(row);

                return switch (column) {
                    case 0 ->  {
                        if (so.getIcon() != null)
                            yield new ImageIcon(ImageUtil.scale(so.getIcon().getImage(), 24, 24));
                        else
                            yield null;
                    }
                    case 1 -> so.getName();
                    case 2 -> so.getObjectSimpleClassName();
                    default -> "";
                };
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                if (columnIndex == 0)
                    return ImageIcon.class;

                return String.class;
            }
        };

        table = new JTable(tableModel);

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setPreferredWidth(32);
        col.setMinWidth(32);
        col.setResizable(false);

        col = colModel.getColumn(1);
        col.setPreferredWidth(100);
        col.setMinWidth(60);

        col = colModel.getColumn(2);
        col.setPreferredWidth(80);
        col.setMinWidth(40);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.setRowHeight(24);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(Box.createVerticalStrut(2));
        if (showTypeFilter)
            add(objectTypePanel);
        if (showNameFilter)
            add(nameFilter);
        if (showTypeFilter || showNameFilter)
            add(Box.createVerticalStrut(8));
        add(table.getTableHeader());
        add(new JScrollPane(table));
        if (showButtons)
            add(buttonsPanel);

        validate();

        refreshObjectTypeList();
        refreshObjects();
    }

    public void setTypeFilter(final String type) {
        objectType.setSelectedItem(type);
    }

    public void setNameFilter(final String name) {
        nameFilter.setText(name);
    }

    protected void refreshObjects() {
        if (swimmingPool != null)
            objects = filterList(swimmingPool.getObjects(), nameFilter.getText());
        else
            objects.clear();
    }

    protected int getObjectIndex(final SwimmingObject object) {
        return objects.indexOf(object);
    }

    protected int getObjectModelIndex(final SwimmingObject object) {
        return getObjectIndex(object);
    }

    protected int getObjectTableIndex(final SwimmingObject object) {
        final int ind = getObjectModelIndex(object);

        if (ind == -1)
            return ind;

        try {
            return table.convertRowIndexToView(ind);
        }
        catch (final IndexOutOfBoundsException e) {
            return -1;
        }
    }

    public ArrayList<SwimmingObject> getSelectedObjects() {
        final ArrayList<SwimmingObject> result = new ArrayList<>();

        for (final int rowIndex : table.getSelectedRows()) {
            int index = -1;

            if (rowIndex != -1) {
                try {
                    index = table.convertRowIndexToModel(rowIndex);
                }
                catch (final IndexOutOfBoundsException e) {
                    // ignore
                }
            }

            //if ((index >= 0) || (index < objects.size())) // Always true
            result.add(objects.get(index));
        }

        return result;
    }

    public void setSelectedObjects(final ArrayList<SwimmingObject> sos) {
        table.clearSelection();

        for (final SwimmingObject so : sos) {
            final int index = getObjectTableIndex(so);

            if (index > -1)
                table.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    protected void refreshObjectTypeList() {
        final DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) objectType.getModel();
        final Object savedItem = model.getSelectedItem();

        model.removeAllElements();
        model.addElement("ALL");

        if (swimmingPool != null) {
            for (final String type : SwimmingObject.getObjectTypes(swimmingPool.getObjects()))
                model.addElement(type);
        }

        if (savedItem != null)
            model.setSelectedItem(savedItem);
        else
            objectType.setSelectedIndex(0);
    }

    private ArrayList<SwimmingObject> filterList(final ArrayList<SwimmingObject> list, final String nameFilterText) {
        final ArrayList<SwimmingObject> result = new ArrayList<>();

        final boolean typeEmpty = objectType.getSelectedIndex() == 0;
        final boolean nameEmpty = StringUtil.isEmpty(nameFilterText, true);
        final String typeFilter;
        final String nameFilterUp;

        if (!typeEmpty && objectType.getSelectedItem() != null)
            typeFilter = objectType.getSelectedItem().toString();
        else
            typeFilter = "";
        if (!nameEmpty)
            nameFilterUp = nameFilterText.toUpperCase();
        else
            nameFilterUp = "";

        for (final SwimmingObject so : list) {
            // search in name and type
            if ((typeEmpty || so.getObjectSimpleClassName().equals(typeFilter)) && (nameEmpty || (so.getName().contains(nameFilterUp))))
                result.add(so);
        }

        return result;
    }

    protected void refreshTableData() {
        ThreadUtil.invokeLater(() -> {
            final ArrayList<SwimmingObject> sos = getSelectedObjects();

            tableModel.fireTableDataChanged();

            // restore previous selected objects if possible
            setSelectedObjects(sos);
        });
    }

    protected void refreshButtonsPanel() {
        deleteButton.setEnabled(getSelectedObjects().size() > 0);
        deleteAllButton.setEnabled(objects.size() > 0);
    }

    protected void pluginsChanged() {
        refreshObjects();
        refreshTableData();
        refreshButtonsPanel();
    }

    @Override
    public void textChanged(final IcyTextField source, final boolean validate) {
        pluginsChanged();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        refreshButtonsPanel();

        // TODO : send event to notify selection change
    }

    @Override
    public void swimmingPoolChangeEvent(final SwimmingPoolEvent swimmingPoolEvent) {
        refreshObjectTypeList();
        refreshObjects();
    }
}
