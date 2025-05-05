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

package org.bioimageanalysis.icy.gui.toolbar.panel;

import org.bioimageanalysis.icy.gui.action.SequenceOperationActions;
import org.bioimageanalysis.icy.gui.component.button.IcyButton;
import org.bioimageanalysis.icy.gui.listener.ActiveSequenceListener;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.gui.undo.AbstractIcyUndoableEdit;
import org.bioimageanalysis.icy.gui.undo.IcyUndoManager;
import org.bioimageanalysis.icy.gui.undo.IcyUndoManagerListener;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class UndoManagerPanel extends ToolbarPanel implements ActiveSequenceListener, ListSelectionListener, IcyUndoManagerListener, ChangeListener {
    private static UndoManagerPanel instance = null;

    public static UndoManagerPanel getInstance() {
        if (instance == null)
            instance = new UndoManagerPanel();
        return instance;
    }

    static final String[] columnNames = {"", "Action"};

    private IcyUndoManager undoManager;

    // GUI
    private final AbstractTableModel tableModel;
    private final ListSelectionModel tableSelectionModel;

    // internals
    private boolean isSelectionAdjusting;
    private final IcyButton undoButton;
    private final IcyButton redoButton;
    private final JSpinner historySizeField;
    private final IcyButton clearAllButLastButton;
    private final IcyButton clearAllButton;
    private final Runnable refresher;

    public UndoManagerPanel() {
        super(new Dimension(400, 0));

        undoManager = null;
        isSelectionAdjusting = false;

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
                if (undoManager != null)
                    return undoManager.getEditsCount() + 1;

                return 1;
            }

            @Override
            public Object getValueAt(final int row, final int column) {
                if (row == 0) {
                    if (column == 0)
                        return null;

                    if (undoManager != null)
                        return "Initial state";

                    return "No opened sequence";
                }

                if (undoManager != null) {
                    final AbstractIcyUndoableEdit edit = undoManager.getEdit(row - 1);

                    switch (column) {
                        case 0:
                            return edit.getIcon();

                        case 1:
                            return edit.getPresentationName();
                    }
                }

                return "";
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                if (columnIndex == 0)
                    return Icon.class;

                return String.class;
            }
        };

        final JTable table = new JTable(tableModel);
        table.setToolTipText("Click on an action to undo or redo until that point");

        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // columns setting
        col = colModel.getColumn(0);
        col.setPreferredWidth(20);
        col.setMinWidth(20);
        col.setMaxWidth(20);

        col = colModel.getColumn(1);
        col.setPreferredWidth(100);
        col.setMinWidth(60);

        table.setRowHeight(20);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(true);
        table.setAutoCreateRowSorter(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        final JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

        middlePanel.add(table.getTableHeader());
        final JScrollPane sc = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sc.setToolTipText("");
        middlePanel.add(sc);

        final JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        undoButton = new IcyButton(SVGIcon.UNDO);
        undoButton.addActionListener(SequenceOperationActions.undoAction);
        undoButton.setHideActionText(true);
        bottomPanel.add(undoButton);

        redoButton = new IcyButton(SVGIcon.REDO);
        redoButton.addActionListener(SequenceOperationActions.redoAction);
        redoButton.setHideActionText(true);
        bottomPanel.add(redoButton);

        final Component horizontalGlue = Box.createHorizontalGlue();
        bottomPanel.add(horizontalGlue);

        final JLabel lblNewLabel = new JLabel("History size");
        lblNewLabel.setToolTipText("");
        bottomPanel.add(lblNewLabel);

        final Component horizontalStrut = Box.createHorizontalStrut(8);
        bottomPanel.add(horizontalStrut);

        historySizeField = new JSpinner();
        historySizeField.setModel(new SpinnerNumberModel(50, 1, 200, 1));
        historySizeField.setToolTipText("Maximum size of the history (lower value will reduce memory usage)");
        bottomPanel.add(historySizeField);

        final Component horizontalStrut_2 = Box.createHorizontalStrut(8);
        bottomPanel.add(horizontalStrut_2);

        clearAllButLastButton = new IcyButton(SVGIcon.DELETE_SWEEP);
        clearAllButLastButton.addActionListener(SequenceOperationActions.undoClearAllButLastAction);
        clearAllButLastButton.setHideActionText(true);
        bottomPanel.add(clearAllButLastButton);

        clearAllButton = new IcyButton(SVGIcon.DELETE);
        clearAllButton.addActionListener(SequenceOperationActions.undoClearAction);
        clearAllButton.setHideActionText(true);
        bottomPanel.add(clearAllButton);

        setLayout(new BorderLayout());

        add(middlePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        historySizeField.setValue(Integer.valueOf(GeneralPreferences.getHistorySize()));
        historySizeField.addChangeListener(this);

        refresher = this::refreshTableDataAndActions;

        refresher.run();

        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    public void setUndoManager(@Nullable final IcyUndoManager value) {
        if (undoManager != value) {
            if (undoManager != null)
                undoManager.removeListener(this);

            undoManager = value;

            if (undoManager != null)
                undoManager.addListener(this);

            // refresh data and actions
            ThreadUtil.bgRunSingle(refresher);
        }
    }

    public AbstractIcyUndoableEdit getLastSelectedEdit() {
        if (undoManager != null) {
            final int index = tableSelectionModel.getMaxSelectionIndex();

            if (index > 0)
                return undoManager.getSignificantEdit(index - 1);
        }

        return null;
    }

    private void refreshTableDataAndActions() {
        ThreadUtil.invokeNow(() -> {
            isSelectionAdjusting = true;
            try {
                tableModel.fireTableDataChanged();

                if (undoManager != null)
                    tableSelectionModel.setSelectionInterval(0, undoManager.getNextAddIndex());
                else
                    tableSelectionModel.setSelectionInterval(0, 0);
            }
            finally {
                isSelectionAdjusting = false;
            }

            if (undoManager != null) {
                undoButton.setEnabled(undoManager.canUndo());
                redoButton.setEnabled(undoManager.canRedo());
                clearAllButLastButton.setEnabled(undoManager.canUndo());
                clearAllButton.setEnabled(undoManager.canUndo() || undoManager.canRedo());
            }
            else {
                undoButton.setEnabled(false);
                redoButton.setEnabled(false);
                clearAllButLastButton.setEnabled(false);
                clearAllButton.setEnabled(false);
            }
        });
    }

    /**
     * called when selection has changed
     */
    private void selectionChanged() {
        // process undo / redo operation
        if (undoManager != null) {
            final AbstractIcyUndoableEdit selectedEdit = getLastSelectedEdit();

            // first entry
            if (selectedEdit == null)
                undoManager.undoAll();
            else
                undoManager.undoOrRedoTo(selectedEdit);
        }
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || isSelectionAdjusting)
            return;

        if (tableSelectionModel.getMinSelectionIndex() != 0)
            tableSelectionModel.setSelectionInterval(0, tableSelectionModel.getMaxSelectionIndex());
        else
            selectionChanged();
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        final int value = ((Integer) historySizeField.getValue()).intValue();

        // change size of all current active undo manager
        for (final Sequence sequence : Icy.getMainInterface().getSequences()) {
            final IcyUndoManager um = sequence.getUndoManager();

            if (um != null)
                um.setLimit(value);
        }

        GeneralPreferences.setHistorySize(value);

        refreshTableDataAndActions();
    }

    @Override
    public void undoManagerChanged(final IcyUndoManager source) {
        ThreadUtil.bgRunSingle(refresher);
    }

    @Override
    public void sequenceActivated(@Nullable final Sequence sequence) {
        if (sequence == null)
            setUndoManager(null);
        else
            setUndoManager(sequence.getUndoManager());
    }

    @Override
    public void sequenceDeactivated(@Nullable final Sequence sequence) {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(@Nullable final SequenceEvent event) {
        // nothing here
    }
}
