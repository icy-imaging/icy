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

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.listener.AcceptListener;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.gui.listener.GlobalSequenceListener;
import org.bioimageanalysis.icy.model.sequence.Sequence;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * The sequence chooser is a component derived from JComboBox. <br>
 * The combo auto refresh its content regarding to the sequence opened in ICY.<br>
 * You can get it with getSequenceSelected()
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */

public class SequenceChooser extends JComboBox<Sequence> implements GlobalSequenceListener {
    public interface SequenceChooserListener {
        /**
         * Called when the sequence chooser selection changed for specified sequence.
         */
        void sequenceChanged(Sequence sequence);
    }

    protected final class SequenceComboModel extends DefaultComboBoxModel<Sequence> {
        /**
         * cached items list
         */
        final List<WeakReference<Sequence>> cachedList;

        public SequenceComboModel() {
            super();

            cachedList = new ArrayList<>();
            updateList();
        }

        public void updateList() {
            // save selected item
            final Object selected = getSelectedItem();

            final int oldSize = cachedList.size();

            cachedList.clear();

            // add null entry at first position
            if (nullEntryName != null)
                cachedList.add(new WeakReference<>(null));

            final List<Sequence> sequences = Icy.getMainInterface().getSequences();

            // add active sequence entry at second position
            if ((sequences.size() > 0) && (activeSequence != null))
                cachedList.add(new WeakReference<>(activeSequence));

            // add others sequence
            for (final Sequence seq : sequences)
                if ((filter == null) || filter.accept(seq))
                    cachedList.add(new WeakReference<>(seq));

            final int newSize = cachedList.size();

            // some elements has been removed
            if (newSize < oldSize)
                fireIntervalRemoved(this, newSize, oldSize - 1);
                // some elements has been added
            else if (newSize > oldSize)
                fireIntervalAdded(this, oldSize, newSize - 1);

            // and some elements changed
            fireContentsChanged(this, 0, newSize - 1);

            // restore selected item
            setSelectedItem(selected);
        }

        @Override
        public Sequence getElementAt(final int index) {
            return cachedList.get(index).get();
        }

        @Override
        public int getSize() {
            return cachedList.size();
        }
    }

    public static final String SEQUENCE_SELECT_CMD = "sequence_select";

    /**
     * var
     */
    AcceptListener filter;
    final String nullEntryName;

    /**
     * listeners
     */
    protected final List<SequenceChooserListener> listeners;

    /**
     * internals
     */
    protected WeakReference<Sequence> previousSelectedSequence;
    protected final SequenceComboModel model;
    protected final Sequence activeSequence;

    /**
     * Create a new Sequence chooser component (JComboBox for sequence selection).
     *
     * @param activeSequenceEntry
     *        If true the combobox will display an <i>Active Sequence</i> entry so when we select it
     *        the {@link #getSelectedSequence()} method returns the current active sequence.
     * @param nullEntryName
     *        If this parameter is not <code>null</code> the combobox will display an extra entry
     *        with the given string to define <code>null</code> sequence selection so when this
     *        entry will be selected the {@link #getSelectedSequence()} will return <code>null</code>.
     * @param nameMaxLength
     *        Maximum authorized length for the sequence name display in the combobox (extra
     *        characters are truncated).<br>
     *        That prevent the combobox to be resized to very large width.
     */
    public SequenceChooser(final boolean activeSequenceEntry, final String nullEntryName, final int nameMaxLength) {
        super();

        this.nullEntryName = nullEntryName;

        if (activeSequenceEntry)
            activeSequence = new Sequence("active sequence");
        else
            activeSequence = null;

        model = new SequenceComboModel();
        setModel(model);
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            final JLabel result = new JLabel();

            if (value != null) {
                final String name = value.getName();

                result.setText(StringUtil.limit(name, nameMaxLength));
                result.setToolTipText(name);
            }
            else
                result.setText(nullEntryName);

            return result;
        });

        addActionListener(this);

        // default
        listeners = new ArrayList<>();
        setActionCommand(SEQUENCE_SELECT_CMD);
        previousSelectedSequence = new WeakReference<>(null);
        setSelectedItem(null);

        // fix height
        ComponentUtil.setFixedHeight(this, 26);
    }

    /**
     * Create a new Sequence chooser component (JComboBox for sequence selection).
     *
     * @param activeSequenceEntry
     *        If true the combobox will display an <i>Active Sequence</i> entry so when we select it
     *        the {@link #getSelectedSequence()} method returns the current active sequence.
     * @param nullEntryName
     *        If this parameter is not <code>null</code> the combobox will display an extra entry
     *        with the given string to define <code>null</code> sequence selection so when this
     *        entry will be selected the {@link #getSelectedSequence()} will return <code>null</code>.
     */
    public SequenceChooser(final boolean activeSequenceEntry, final String nullEntryName) {
        this(activeSequenceEntry, nullEntryName, 64);
    }

    /**
     * Create a new Sequence chooser component (JComboBox for sequence selection).
     */
    public SequenceChooser() {
        this(true, null, 64);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Icy.getMainInterface().addGlobalSequenceListener(this);
    }

    @Override
    public void removeNotify() {
        Icy.getMainInterface().removeGlobalSequenceListener(this);

        super.removeNotify();
    }

    /**
     * @return the filter
     */
    public AcceptListener getFilter() {
        return filter;
    }

    /**
     * Set a filter for sequence display.<br>
     * Only Sequence accepted by the filter will appear in the combobox.
     */
    public void setFilter(final AcceptListener filter) {
        if (this.filter != filter) {
            this.filter = filter;
            model.updateList();
        }
    }

    /**
     * @return current selected sequence.
     */
    public Sequence getSelectedSequence() {
        final Sequence result = (Sequence) getSelectedItem();

        // special case for active sequence
        if (result == activeSequence)
            return Icy.getMainInterface().getActiveSequence();

        return result;
    }

    /**
     * Select the <i>Active sequence</i> entry if enable.
     */
    public void setActiveSequenceSelected() {
        if (activeSequence != null)
            setSelectedItem(activeSequence);
    }

    /**
     * @param sequence
     *        The sequence to select in the combo box
     */
    public void setSelectedSequence(final Sequence sequence) {
        if (sequence != getSelectedSequence())
            setSelectedItem(sequence);
    }

    // called when sequence selection has changed
    private void sequenceChanged(final Sequence sequence) {
        fireSequenceChanged(sequence);
    }

    private void fireSequenceChanged(final Sequence sequence) {
        for (final SequenceChooserListener listener : getListeners())
            listener.sequenceChanged(sequence);
    }

    public ArrayList<SequenceChooserListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    public void addListener(final SequenceChooserListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(final SequenceChooserListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Sequence selected = getSelectedSequence();

        if (previousSelectedSequence.get() != selected) {
            previousSelectedSequence = new WeakReference<>(selected);
            // sequence changed
            sequenceChanged(selected);
        }
    }

    @Override
    public void sequenceOpened(final Sequence sequence) {
        model.updateList();
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
        model.updateList();
    }
}
