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

package icy.gui.toolbar.panel;

import icy.action.CanvasActions;
import icy.canvas.CanvasLayerEvent;
import icy.canvas.CanvasLayerListener;
import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.component.IcyTextField;
import icy.gui.component.IcyTextField.TextChangeListener;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.component.editor.VisibleCellEditor;
import icy.gui.component.renderer.VisibleCellRenderer;
import icy.gui.main.ActiveViewerListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.main.Icy;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class LayersPanel extends ToolbarPanel implements ActiveViewerListener, CanvasLayerListener, TextChangeListener, ListSelectionListener {
    private static LayersPanel instance = null;

    public static LayersPanel getInstance() {
        if (instance == null)
            instance = new LayersPanel();
        return instance;
    }

    private class CanvasRefresher implements Runnable {
        IcyCanvas newCanvas;

        public CanvasRefresher() {
            super();
        }

        @Override
        public void run() {
            final IcyCanvas c = newCanvas;

            // change canvas
            if (canvas != c) {
                if (canvas != null)
                    canvas.removeLayerListener(LayersPanel.this);

                canvas = c;

                if (canvas != null)
                    canvas.addLayerListener(LayersPanel.this);
            }

            refreshLayersInternal();
        }
    }

    static final String[] columnNames = {"Name", ""};

    private List<Layer> layers;
    private IcyCanvas canvas;

    // GUI
    private final AbstractTableModel tableModel;
    private final ListSelectionModel tableSelectionModel;
    private final JXTable table;
    private final IcyTextField nameFilter;
    private final IcyToggleButton tglbtnLayerVisibility;
    private ActionListener visibilityToggleActionListener;
    private LayerControlPanel controlPanel;

    // internals
    private boolean isSelectionAdjusting;
    private boolean isLayerEditing;

    private final Runnable layersRefresher;
    private final Runnable tableDataRefresher;
    private final Runnable controlPanelRefresher;
    private final CanvasRefresher canvasRefresher;

    public LayersPanel() {
        super(new Dimension(400, 0));

        layers = new ArrayList<>();
        canvas = null;
        isSelectionAdjusting = false;
        isLayerEditing = false;

        layersRefresher = this::refreshLayersInternal;
        tableDataRefresher = this::refreshTableData;
        controlPanelRefresher = () -> controlPanel.refresh();
        canvasRefresher = new CanvasRefresher();

        // build GUI
        final JPanel panelNorth = new JPanel();
        final GridBagLayout gbl_panelNorth = new GridBagLayout();
        gbl_panelNorth.columnWidths = new int[]{46, 22, 0};
        gbl_panelNorth.rowHeights = new int[]{23, 0};
        gbl_panelNorth.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        gbl_panelNorth.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panelNorth.setLayout(gbl_panelNorth);

        nameFilter = new IcyTextField();
        nameFilter.setToolTipText("Enter a string sequence to filter Layer on name");
        nameFilter.addTextChangeListener(this);
        final GridBagConstraints gbc_nameFilter = new GridBagConstraints();
        gbc_nameFilter.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameFilter.insets = new Insets(0, 0, 0, 5);
        gbc_nameFilter.gridx = 0;
        gbc_nameFilter.gridy = 0;
        panelNorth.add(nameFilter, gbc_nameFilter);

        tglbtnLayerVisibility = new IcyToggleButton(new SVGIconPack(SVGIcon.VISIBILITY_OFF, SVGIcon.VISIBILITY));
        tglbtnLayerVisibility.setFocusable(false);
        tglbtnLayerVisibility.setToolTipText("Change visibility for selected layer(s)");
        final GridBagConstraints gbc_tglbtnLayerVisibility = new GridBagConstraints();
        gbc_tglbtnLayerVisibility.anchor = GridBagConstraints.WEST;
        gbc_tglbtnLayerVisibility.gridx = 1;
        gbc_tglbtnLayerVisibility.gridy = 0;
        panelNorth.add(tglbtnLayerVisibility, gbc_tglbtnLayerVisibility);

        table = new JXTable();
        table.setAutoStartEditOnKeyStroke(false);
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setColumnControlVisible(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);

        controlPanel = new LayerControlPanel(this);

        setLayout(new BorderLayout(0, 0));
        add(panelNorth, BorderLayout.NORTH);
        add(
                new JScrollPane(
                        table,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                ),
                BorderLayout.CENTER
        );
        add(controlPanel, BorderLayout.SOUTH);

        validate();

        // add show/hide listener
        initVisibilityToggleListener();
        tglbtnLayerVisibility.addActionListener(visibilityToggleActionListener);

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
                return layers.size();
            }

            @Override
            public Object getValueAt(final int row, final int column) {
                // safe
                if (row >= layers.size())
                    return null;

                final Layer layer = layers.get(row);

                return switch (column) {
                    case 0 ->
                        // layer name
                            layer.getName();
                    case 1 ->
                        // layer visibility
                            Boolean.valueOf(layer.isVisible());
                    default -> "";
                };
            }

            @Override
            public void setValueAt(final Object value, final int row, final int column) {
                // safe
                if (row >= layers.size())
                    return;

                isLayerEditing = true;
                try {
                    final Layer layer = layers.get(row);

                    switch (column) {
                        case 0:
                            layer.setName((String) value);
                            break;

                        case 1:
                            // layer visibility
                            layer.setVisible(((Boolean) value).booleanValue());
                            setToggleButtonState(((Boolean) value).booleanValue());
                            break;
                    }
                }
                finally {
                    isLayerEditing = false;
                }
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                // safe
                if (row >= layers.size())
                    return false;

                final boolean editable;

                // name field ?
                if (column == 0) {
                    final Layer layer = layers.get(row);
                    //editable = (layer != null) ? !layer.isReadOnly() : false;
                    editable = layer != null && !layer.isReadOnly();
                }
                else
                    editable = true;

                return editable;
            }

            @SuppressWarnings("SwitchStatementWithTooFewBranches")
            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return switch (columnIndex) {
                    default ->
                        // layer name
                            String.class;
                    case 1 ->
                        // layer visibility
                            Boolean.class;
                };
            }
        };
        // set table model
        table.setModel(tableModel);
        // disable extra actions from column control
        ((ColumnControlButton) table.getColumnControl()).setAdditionalActionsVisible(false);
        // remove the internal find command (we have our own filter)
        table.getActionMap().remove("find");

        TableColumnExt col;

        // columns setting - name
        col = table.getColumnExt(0);
        col.setPreferredWidth(140);
        col.setToolTipText("Layer name (double click in a cell to edit)");

        // columns setting - visible
        col = table.getColumnExt(1);
        col.setPreferredWidth(20);
        col.setMinWidth(20);
        col.setMaxWidth(20);
        col.setCellEditor(new VisibleCellEditor());
        col.setCellRenderer(new VisibleCellRenderer());
        col.setToolTipText("Make the layer visible or not");
        col.setResizable(false);

        // table selection model
        tableSelectionModel = table.getSelectionModel();
        tableSelectionModel.addListSelectionListener(this);
        tableSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // create shortcuts
        buildActionMap();

        // and refresh layers
        refreshLayers();

        Icy.getMainInterface().addActiveViewerListener(this);

    }

    private void initVisibilityToggleListener() {
        visibilityToggleActionListener = e -> {
            final boolean visibilityValue = tglbtnLayerVisibility.isSelected();
            for (final Layer l : getSelectedLayers()) {
                l.setVisible(visibilityValue);
            }
        };
    }

    void buildActionMap() {
        final InputMap imap = table.getInputMap(JComponent.WHEN_FOCUSED);
        final ActionMap amap = table.getActionMap();

        imap.put(CanvasActions.unselectAction.getKeyStroke(), CanvasActions.unselectAction.getName());
        imap.put(CanvasActions.deleteLayersAction.getKeyStroke(), CanvasActions.deleteLayersAction.getName());
        // also allow backspace key for delete operation here
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), CanvasActions.deleteLayersAction.getName());

        // disable search feature (we have our own filter)
        amap.remove("find");
        amap.put(CanvasActions.unselectAction.getName(), CanvasActions.unselectAction);
        amap.put(CanvasActions.deleteLayersAction.getName(), CanvasActions.deleteLayersAction);
    }

    public void setNameFilter(final String name) {
        nameFilter.setText(name);
    }

    /**
     * refresh Layer list (and refresh table data according)
     */
    private void refreshLayers() {
        ThreadUtil.runSingle(layersRefresher);
    }

    /**
     * refresh layer list (internal)
     */
    void refreshLayersInternal() {
        if (canvas != null)
            layers = filterList(canvas.getLayers(false), nameFilter.getText());
        else
            layers.clear();

        // refresh table data
        ThreadUtil.runSingle(tableDataRefresher);
    }

    /**
     * Return index of specified Layer in the Layer list
     */
    private int getLayerIndex(final Layer layer) {
        return layers.indexOf(layer);
    }

    /**
     * Return index of specified Layer in the model
     */
    private int getLayerModelIndex(final Layer layer) {
        return getLayerIndex(layer);
    }

    /**
     * Return index of specified Layer in the table
     */
    private int getLayerTableIndex(final Layer layer) {
        final int ind = getLayerModelIndex(layer);

        if (ind == -1)
            return ind;

        try {
            return table.convertRowIndexToView(ind);
        }
        catch (final IndexOutOfBoundsException e) {
            return -1;
        }
    }

    public ArrayList<Layer> getSelectedLayers() {
        final ArrayList<Layer> result = new ArrayList<>();

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

            if ((index >= 0) && (index < layers.size()))
                result.add(layers.get(index));
        }

        return result;
    }

    public void clearSelected() {
        setSelectedLayersInternal(new ArrayList<>());
    }

    void setSelectedLayersInternal(final List<Layer> newSelected) {
        isSelectionAdjusting = true;
        try {
            table.clearSelection();

            if (newSelected != null) {
                boolean allHidden = newSelected.size() > 0;
                for (final Layer layer : newSelected) {
                    final int index = getLayerTableIndex(layer);

                    if (index > -1) {
                        tableSelectionModel.addSelectionInterval(index, index);
                        allHidden = allHidden && !layer.isVisible();
                    }
                }

                setToggleButtonState(!allHidden);
            }
        }
        finally {
            isSelectionAdjusting = false;
        }

        // notify selection changed
        selectionChanged();
    }

    private void setToggleButtonState(final boolean active) {
        tglbtnLayerVisibility.setSelected(active);
    }

    List<Layer> filterList(final List<Layer> list, final String nameFilterText) {
        final List<Layer> result = new ArrayList<>();

        final boolean nameEmpty = StringUtil.isEmpty(nameFilterText, true);
        final String nameFilterUp;

        if (!nameEmpty)
            nameFilterUp = nameFilterText.trim().toLowerCase();
        else
            nameFilterUp = "";

        for (final Layer layer : list) {
            // search in name and type
            if (nameEmpty || (layer.getName().toLowerCase().contains(nameFilterUp)))
                result.add(layer);
        }

        return result;
    }

    private void refreshTableData() {
        final List<Layer> save = getSelectedLayers();

        // need to be done on EDT
        ThreadUtil.invokeNow(() -> {
            isSelectionAdjusting = true;
            try {
                tableModel.fireTableDataChanged();
            }
            finally {
                isSelectionAdjusting = false;
            }

            setSelectedLayersInternal(save);
        });
    }

    /**
     * Called when selection changed
     */
    private void selectionChanged() {
        final ArrayList<Layer> newSelected = getSelectedLayers();
        boolean allHidden = newSelected.size() > 0;
        for (final Layer layer : newSelected) {
            final int index = getLayerTableIndex(layer);
            if (index > -1) {
                allHidden = allHidden && !layer.isVisible();
            }
        }

        setToggleButtonState(!allHidden);
        // refresh control panel
        ThreadUtil.runSingle(controlPanelRefresher);
    }

    @Override
    public void textChanged(final IcyTextField source, final boolean validate) {
        if (source == nameFilter)
            refreshLayers();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        // internal change --> ignore
        if (isSelectionAdjusting || e.getValueIsAdjusting())
            return;

        selectionChanged();
    }

    @Override
    public void viewerActivated(final Viewer viewer) {
        if (viewer != null)
            canvasRefresher.newCanvas = viewer.getCanvas();
        else
            canvasRefresher.newCanvas = null;

        ThreadUtil.runSingle(canvasRefresher);
    }

    @Override
    public void viewerDeactivated(final Viewer viewer) {
        // nothing here
    }

    @Override
    public void activeViewerChanged(final ViewerEvent event) {
        if (event.getType() == ViewerEventType.CANVAS_CHANGED) {
            canvasRefresher.newCanvas = event.getSource().getCanvas();
            ThreadUtil.runSingle(canvasRefresher);
        }
    }

    @Override
    public void canvasLayerChanged(final CanvasLayerEvent event) {
        // refresh layer from externals changes
        if (isLayerEditing)
            return;

        switch (event.getType()) {
            case ADDED:
            case REMOVED:
                refreshLayers();
                break;

            case CHANGED:
                final String property = event.getProperty();

                if (Layer.PROPERTY_NAME.equals(property) || Layer.PROPERTY_OPACITY.equals(property) || Layer.PROPERTY_VISIBLE.equals(property))
                    // refresh table data
                    ThreadUtil.runSingle(tableDataRefresher);
                break;
        }
    }

}
