/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

package fr.icy.gui.component.model;

import fr.icy.common.string.StringUtil;
import org.jetbrains.annotations.Contract;
import org.w3c.dom.*;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class XMLTreeModel implements TreeModel {
    public static class XMLAdapterNode {
        public Node node;

        /**
         * Creates a new instance of the XMLAdapterNode class
         */
        @Contract(pure = true)
        public XMLAdapterNode(final Node node) {
            super();

            this.node = node;
        }

        /**
         * Return all children
         */
        public List<Node> getChildren() {
            final List<Node> result = new ArrayList<>();

            if (node.hasAttributes()) {
                final NamedNodeMap attributes = node.getAttributes();
                final int count = attributes.getLength();

                for (int i = 0; i < count; i++)
                    result.add(attributes.item(i));
            }

            final NodeList nodes = node.getChildNodes();
            final int count = nodes.getLength();

            for (int i = 0; i < count; i++) {
                final Node node = nodes.item(i);

                if (node instanceof Element)
                    result.add(node);
            }

            return result;
        }

        /**
         * Return index of child in this node.
         *
         * @param child The child to look for
         * @return index of child, -1 if not present (error)
         */
        public int index(final XMLAdapterNode child) {
            int result = 0;

            for (final Node node : getChildren()) {
                if (child.node == node)
                    return result;

                result++;
            }

            return -1; // Should never get here.
        }

        /**
         * Returns an adapter node given a valid index found through
         * the method: public int index(XMLAdapterNode child)
         *
         * @param index find this by calling index(XMLAdapterNode)
         * @return the desired child
         */
        public XMLAdapterNode child(final int index) {
            final Node n = getChildren().get(index);

            if (n == null)
                return null;

            return new XMLAdapterNode(n);
        }

        /**
         * Return the number of element children for this element/node
         *
         * @return int number of element children
         */
        public int childCount() {
            return getChildren().size();
        }

        /**
         * Return the value of this node from its sub text nodes
         */
        protected String getValue() {
            final NodeList nodes = node.getChildNodes();
            final int count = nodes.getLength();
            final StringBuilder result = new StringBuilder();

            for (int i = 0; i < count; i++) {
                final Node node = nodes.item(i);

                // text node
                if (!(node instanceof Element)) {
                    final String value = node.getNodeValue();

                    if ((value != null) && !StringUtil.equals(value, "null"))
                        result.append(value).append(" ");
                }
            }

            return result.toString().trim();
        }

        @Override
        public String toString() {
            final String nodeName = node.getNodeName();
            final String nodeValue = node.getNodeValue();

            if (!StringUtil.isEmpty(nodeValue) && !StringUtil.equals(nodeValue, "null"))
                return nodeName + " = " + nodeValue;

            return nodeName;
        }
    }

    protected Document document;

    /**
     * listeners
     */
    protected EventListenerList listeners = new EventListenerList();

    public XMLTreeModel(final Document doc) {
        super();

        if (doc == null)
            throw new NullPointerException();

        document = doc;
    }

    @Override
    public Object getRoot() {
        if (document.getDocumentElement() == null)
            return null;

        return new XMLAdapterNode(document.getDocumentElement());
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        return ((XMLAdapterNode) parent).child(index);
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        return ((XMLAdapterNode) parent).index((XMLAdapterNode) child);
    }

    @Override
    public int getChildCount(final Object parent) {
        return ((XMLAdapterNode) parent).childCount();
    }

    @Override
    public boolean isLeaf(final Object node) {
        return ((XMLAdapterNode) node).childCount() == 0;
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // ignore here
    }

    /*
     * Use these methods to add and remove event listeners.
     * (Needed to satisfy TreeModel interface, but not used.)
     */

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @param l the listener to add
     * @see #removeTreeModelListener
     */
    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        listeners.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     *
     * @param l the listener to remove
     * @see #addTreeModelListener
     */
    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        listeners.remove(TreeModelListener.class, l);
    }

    public void fireTreeNodesChanged(final TreeModelEvent e) {
        for (final TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeNodesChanged(e);
    }

    public void fireTreeNodesInserted(final TreeModelEvent e) {
        for (final TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeNodesInserted(e);
    }

    public void fireTreeNodesRemoved(final TreeModelEvent e) {
        for (final TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeNodesRemoved(e);
    }

    public void fireTreeStructureChanged(final TreeModelEvent e) {
        for (final TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeStructureChanged(e);
    }
}
