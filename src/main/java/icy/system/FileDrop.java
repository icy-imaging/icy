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

package icy.system;

import icy.system.logging.IcyLogger;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

/**
 * <p>
 * This class makes it easy to drag and drop files from the operating
 * system to a Java program. Any <i>Component</i> can be
 * dropped onto, but only <i>JComponent</i>s will indicate
 * the drop event with a changed
 * </p>
 * <p>To use this class, construct a new <i>FileDrop</i> by passing it the target component and a
 * <i>Listener</i> to receive notification when file(s) have been dropped. Here is an example:
 * </p>
 * <code>
 *      JPanel myPanel = new JPanel();
 *      new FileDrop( myPanel, new FileDrop.Listener()
 *      {   public void filesDropped( File[] files )
 *          {
 *              // handle file drop
 *              ...
 *          }
 *      });
 * </code>
 * <p>
 * You can specify the border that will appear when files are being dragged by calling the
 * constructor with a <i>Border</i>. Only <i>JComponent</i>s will show any indication with a
 * </p>
 * <p>You can turn on some debugging features by passing a <i>PrintStream</i> object (such as
 * <i>System.out</i>) into the full constructor. A <i>null</i> value will result in no extra
 * debugging information being output.
 * </p>
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * </p>
 * <p>
 * <em>Original author: Robert Harder, rharder@usa.net</em>
 * </p>
 * <p>
 * 2007-09-12 Nathan Blomquist -- Linux (KDE/Gnome) support added.<br>
 * 2012-04-12 Stephane Dallogneville -- cleanup, modified for ICY
 * </p>
 *
 * @author Robert Harder
 * @author rharder@users.sf.net
 * @author Stephane Dallongeville
 * @version 1.0.2
 */
public class FileDrop {
    public static class TransferableObject implements Transferable {
        /**
         * The MIME type for {@link #DATA_FLAVOR} is
         * <i>application/x-net.iharder.TransferableObject</i>.
         *
         * @since 1.1
         */
        public final static String MIME_TYPE = "application/x-net.iharder.TransferableObject";

        /**
         * The default {@link DataFlavor} for {@link TransferableObject} has
         * the representation class <i>net.iharder.TransferableObject.class</i> and the MIME
         * type <i>application/x-net.iharder.TransferableObject</i>.
         *
         * @since 1.1
         */
        public final static DataFlavor DATA_FLAVOR = new DataFlavor(TransferableObject.class, MIME_TYPE);

        private Fetcher fetcher;
        private Object data;

        private DataFlavor customFlavor;

        /**
         * Creates a new {@link TransferableObject} that wraps <var>data</var>.
         * Along with the {@link #DATA_FLAVOR} associated with this class,
         * this creates a custom data flavor with a representation class
         * determined from <code>data.getClass()</code> and the MIME type
         * <i>application/x-net.iharder.TransferableObject</i>.
         *
         * @param data
         *        The data to transfer
         * @since 1.1
         */
        public TransferableObject(final Object data) {
            this.data = data;
            this.customFlavor = new DataFlavor(data.getClass(), MIME_TYPE);
        }

        /**
         * Creates a new {@link TransferableObject} that will return the
         * object that is returned by <var>fetcher</var>.
         * No custom data flavor is set other than the default {@link #DATA_FLAVOR}.
         *
         * @see Fetcher
         * @param fetcher
         *        The {@link Fetcher} that will return the data object
         * @since 1.1
         */
        public TransferableObject(final Fetcher fetcher) {
            this.fetcher = fetcher;
        }

        /**
         * Creates a new {@link TransferableObject} that will return the
         * object that is returned by <var>fetcher</var>.
         * Along with the {@link #DATA_FLAVOR} associated with this class,
         * this creates a custom data flavor with a representation class <var>dataClass</var>
         * and the MIME type <i>application/x-net.iharder.TransferableObject</i>.
         *
         * @see Fetcher
         * @param dataClass
         *        The {@link Class} to use in the custom data flavor
         * @param fetcher
         *        The {@link Fetcher} that will return the data object
         * @since 1.1
         */
        public TransferableObject(final Class<?> dataClass, final Fetcher fetcher) {
            this.fetcher = fetcher;
            this.customFlavor = new DataFlavor(dataClass, MIME_TYPE);
        }

        /**
         * Returns the custom {@link DataFlavor} associated
         * with the encapsulated object or <i>null</i> if the {@link Fetcher} constructor was used
         * without passing a {@link Class}.
         *
         * @return The custom data flavor for the encapsulated object
         * @since 1.1
         */
        public DataFlavor getCustomDataFlavor() {
            return customFlavor;
        }

        /**
         * Returns a two- or three-element array containing first
         * the custom data flavor, if one was created in the constructors,
         * second the default {@link #DATA_FLAVOR} associated with {@link TransferableObject}, and
         * third the {@link DataFlavor#stringFlavor}.
         *
         * @return An array of supported data flavors
         * @since 1.1
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            if (customFlavor != null)
                return new DataFlavor[]{customFlavor, DATA_FLAVOR, DataFlavor.stringFlavor};
            return new DataFlavor[]{DATA_FLAVOR, DataFlavor.stringFlavor};
        }

        /**
         * Returns the data encapsulated in this {@link TransferableObject}.
         * If the {@link Fetcher} constructor was used, then this is when
         * the {@link Fetcher#getObject getObject()} method will be called.
         * If the requested data flavor is not supported, then the {@link Fetcher#getObject
         * getObject()} method will not be called.
         *
         * @param flavor
         *        The data flavor for the data to return
         * @return The dropped data
         * @since 1.1
         */
        @Override
        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
            // Native object
            if (flavor.equals(DATA_FLAVOR))
                return fetcher == null ? data : fetcher.getObject();

            // String
            if (flavor.equals(DataFlavor.stringFlavor))
                return fetcher == null ? data.toString() : fetcher.getObject().toString();

            // We can't do anything else
            throw new UnsupportedFlavorException(flavor);
        }

        /**
         * Returns <i>true</i> if <var>flavor</var> is one of the supported
         * flavors. Flavors are supported using the <code>equals(...)</code> method.
         *
         * @param flavor
         *        The data flavor to check
         * @return Whether or not the flavor is supported
         * @since 1.1
         */
        @Override
        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            // Native object
            if (flavor.equals(DATA_FLAVOR))
                return true;

            // String
            if (flavor.equals(DataFlavor.stringFlavor))
                return true;

            // We can't do anything else
            return false;
        }

        /**
         * Instead of passing your data directly to the {@link TransferableObject} constructor, you
         * may want to know exactly when your data was received
         * in case you need to remove it from its source (or do anyting else to it).
         * When the {@link #getTransferData getTransferData(...)} method is called
         * on the {@link TransferableObject}, the {@link Fetcher}'s {@link #getObject getObject()}
         * method will be called.
         *
         * @author Robert Harder
         * @version 1.1
         * @since 1.1
         */
        public interface Fetcher {
            /**
             * Return the object being encapsulated in the {@link TransferableObject}.
             *
             * @return The dropped object
             * @since 1.1
             */
            Object getObject();
        }
    }

    transient Border normalBorder;
    transient DropTargetListener dropListener;

    // Default border color
    private static final Color defaultBorderColor = new Color(0f, 0f, 1f, 0.25f);

    /**
     * Constructor with a default border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to <i>out</i>. A common
     * way to use this constructor is with <i>System.out</i> or <i>System.err</i>. A
     * <i>null</i> value for
     * the parameter <i>out</i> will result in no debugging output.
     *
     * @param c
     *        Component on which files will be dropped.
     * @param listener
     *        Listens for <i>filesDropped</i>.
     * @since 1.0
     */
    public FileDrop(final Component c, final FileDropListener listener) {
        this(c, BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), false, listener);
    }

    /**
     * Constructor with a default border, debugging optionally turned on
     * and the option to recursively set drop targets.
     * If your component is a <i>Container</i>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     * With Debugging turned on, more status messages will be displayed to <i>out</i>. A common
     * way to use this constructor is with <i>System.out</i> or <i>System.err</i>. A
     * <i>null</i> value for
     * the parameter <i>out</i> will result in no debugging output.
     *
     * @param c
     *        Component on which files will be dropped.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <i>filesDropped</i>.
     * @since 1.0
     */
    public FileDrop(final Component c, final boolean recursive, final FileDropListener listener) {
        this(c, BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), recursive, listener);
    }

    /**
     * Constructor with a specified border
     *
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <i>JComponent</i> when dragging occurs.
     * @param listener
     *        Listens for <i>filesDropped</i>.
     * @since 1.0
     */
    public FileDrop(final Component c, final Border dragBorder, final FileDropListener listener) {
        this(c, dragBorder, false, listener);
    }

    /**
     * Constructor with a specified border and the option to recursively set drop targets.
     * If your component is a <i>Container</i>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     *
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <i>JComponent</i> when dragging occurs.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <i>filesDropped</i>.
     * @since 1.0
     */
    public FileDrop(final Component c, final Border dragBorder, final boolean recursive, final FileDropListener listener) {
        this(c, dragBorder, recursive, listener, null);
    }

    /**
     * Constructor with a specified border and the option to recursively set drop targets.
     * If your component is a <i>Container</i>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     *
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <i>JComponent</i> when dragging occurs.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <i>filesDropped</i>.
     * @since 1.0
     */
    public FileDrop(final Component c, final Border dragBorder, final boolean recursive, final FileDropExtListener listener) {
        this(c, dragBorder, recursive, null, listener);
    }

    /**
     * Full constructor with a specified border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to <i>out</i>. A common
     * way to use this constructor is with <i>System.out</i> or <i>System.err</i>. A
     * <i>null</i> value for
     * the parameter <i>out</i> will result in no debugging output.
     *
     * @param c
     *        Component on which files will be dropped.
     * @param dragBorder
     *        Border to use on <i>JComponent</i> when dragging occurs.
     * @param recursive
     *        Recursively set children as drop targets.
     * @param listener
     *        Listens for <i>filesDropped</i>.
     * @since 1.0
     */
    FileDrop(final Component c, final Border dragBorder, final boolean recursive, final FileDropListener listener, final FileDropExtListener listenerExt) {
        // Make a drop listener
        dropListener = new DropTargetListener() {
            @Override
            public void dragEnter(final DropTargetDragEvent evt) {
                // Is this an acceptable drag event?
                if (isDragOk(evt)) {
                    // If it's a Swing component, set its border
                    if (c instanceof JComponent) {
                        final JComponent jc = (JComponent) c;
                        normalBorder = jc.getBorder();
                        jc.setBorder(dragBorder);
                    }

                    // Acknowledge that it's okay to enter
                    // evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                }
                else
                    // Reject the drag event
                    evt.rejectDrag();
            }

            @Override
            public void dragOver(final DropTargetDragEvent evt) { // This is called continually as long as the mouse is
                // over the drag target.
            }

            @Override
            public void drop(final DropTargetDropEvent evt) {
                try { // Get whatever was dropped
                    final Transferable tr = evt.getTransferable();

                    // Is it a file list?
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        // Say we'll take it.
                        // evt.acceptDrop ( DnDConstants.ACTION_COPY_OR_MOVE );
                        evt.acceptDrop(DnDConstants.ACTION_COPY);

                        // Get a useful list
                        @SuppressWarnings("unchecked") final List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        // Iterator<File> iterator = fileList.iterator();

                        // Convert list to array
                        final File[] filesTemp = new File[fileList.size()];
                        fileList.toArray(filesTemp);

                        // Alert listener to drop.
                        if (listener != null)
                            listener.filesDropped(filesTemp);
                        if (listenerExt != null)
                            listenerExt.filesDropped(evt, filesTemp);

                        // Mark that drop is completed.
                        evt.getDropTargetContext().dropComplete(true);
                    }
                    else
                    // this section will check for a reader flavor.
                    {
                        final DataFlavor[] flavors = tr.getTransferDataFlavors();
                        boolean handled = false;
                        for (final DataFlavor flavor : flavors) {
                            if (flavor.isRepresentationClassReader()) {
                                // Say we'll take it.
                                // evt.acceptDrop (
                                // DnDConstants.ACTION_COPY_OR_MOVE );
                                evt.acceptDrop(DnDConstants.ACTION_COPY);

                                final Reader reader = flavor.getReaderForText(tr);

                                final BufferedReader br = new BufferedReader(reader);

                                if (listener != null)
                                    listener.filesDropped(createFileArray(br));
                                if (listenerExt != null)
                                    listenerExt.filesDropped(evt, createFileArray(br));

                                // Mark that drop is completed.
                                evt.getDropTargetContext().dropComplete(true);
                                handled = true;
                                break;
                            }
                        }

                        if (!handled)
                            evt.rejectDrop();
                    }
                }
                catch (final IOException io) {
                    IcyLogger.error(FileDrop.class, io, "FileDrop: IOException - abort.");
                    evt.rejectDrop();
                }
                catch (final UnsupportedFlavorException ufe) {
                    IcyLogger.error(FileDrop.class, ufe, "FileDrop: UnsupportedFlavorException - abort.");
                    evt.rejectDrop();
                }
                finally {
                    // If it's a Swing component, reset its border
                    if (c instanceof final JComponent jc)
                        jc.setBorder(normalBorder);
                }
            }

            @Override
            public void dragExit(final DropTargetEvent evt) {
                // If it's a Swing component, reset its border
                if (c instanceof final JComponent jc)
                    jc.setBorder(normalBorder);
            }

            @Override
            public void dropActionChanged(final DropTargetDragEvent evt) {
                // Is this an acceptable drag event?
                if (isDragOk(evt))
                    // evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                else
                    evt.rejectDrag();
            }
        };

        // Make the component (and possibly children) drop targets
        makeDropTarget(c, recursive);
    }

    private static final String ZERO_CHAR_STRING = "" + (char) 0;

    static File[] createFileArray(final BufferedReader bReader) {
        try {
            final List<File> list = new ArrayList<>();
            String line; // = null;
            while ((line = bReader.readLine()) != null) {
                try {
                    // kde seems to append a 0 char to the end of the reader
                    if (ZERO_CHAR_STRING.equals(line))
                        continue;

                    final File file = new File(new java.net.URI(line));
                    list.add(file);
                }
                catch (final Exception ex) {
                    IcyLogger.error(FileDrop.class, ex, "Error with " + line + ": " + ex.getLocalizedMessage());
                }
            }

            return list.toArray(new File[0]);
        }
        catch (final IOException ex) {
            IcyLogger.error(FileDrop.class, ex, "FileDrop: IOException");
        }

        return new File[0];
    }

    private void makeDropTarget(final Component c, final boolean recursive) {
        // Make drop target
        final DropTarget dt = new DropTarget();
        try {
            dt.addDropTargetListener(dropListener);
        }
        catch (final TooManyListenersException e) {
            IcyLogger.error(FileDrop.class, e, "FileDrop: Drop will not work due to previous error. Do you have another listener attached?");
        }

        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener(evt -> {
            final Component parent = c.getParent();

            if (parent == null)
                c.setDropTarget(null);
            else
                new DropTarget(c, dropListener);
        });

        if (c.getParent() != null)
            new DropTarget(c, dropListener);

        if (recursive && (c instanceof final Container cont)) {
            // Get it's components
            final Component[] comps = cont.getComponents();

            // Set it's components as listeners also
            for (final Component comp : comps)
                makeDropTarget(comp, recursive);
        }
    }

    /** Determine if the dragged data is a file list. */
    boolean isDragOk(final DropTargetDragEvent evt) {
        boolean ok = false;

        // Get data flavors being dragged
        final DataFlavor[] flavors = evt.getCurrentDataFlavors();

        // See if any of the flavors are a file list
        int i = 0;
        while (!ok && i < flavors.length) {
            // Is the flavor a file list?
            final DataFlavor curFlavor = flavors[i];
            if (curFlavor.equals(DataFlavor.javaFileListFlavor) || curFlavor.isRepresentationClassReader())
                ok = true;

            i++;
        }

        return ok;
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     * This will recursively unregister all components contained within
     * <var>c</var> if <var>c</var> is a {@link Container}.
     *
     * @param c
     *        The component to unregister as a drop target
     * @since 1.0
     */
    public static boolean remove(final Component c) {
        return remove(null, c, true);
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     *
     * @param out
     *        Optional {@link PrintStream} for logging drag and drop messages
     * @param c
     *        The component to unregister
     * @param recursive
     *        Recursively unregister components within a container
     * @since 1.0
     */
    public static boolean remove(final PrintStream out, final Component c, final boolean recursive) {
        // Make sure we support
        c.setDropTarget(null);

        if (recursive && (c instanceof Container)) {
            final Component[] comps = ((Container) c).getComponents();
            for (final Component comp : comps)
                remove(out, comp, recursive);
            return true;
        }

        return false;
    }

    /**
     * Implement this inner interface to listen for when files are dropped. For example
     * your class declaration may begin like this: <code>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( File[] files )
     *      {
     *          ...
     *      }
     *      ...
     * </code>
     *
     * @since 1.1
     */
    public interface FileDropListener {

        /**
         * This method is called when files have been successfully dropped.
         *
         * @param files
         *        An array of <i>File</i>s that were dropped.
         * @since 1.0
         */
        void filesDropped(File[] files);
    }

    /**
     * Implement this inner interface to listen for when files are dropped. For example
     * your class declaration may begin like this: <code>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( File[] files )
     *      {
     *          ...
     *      }
     *      ...
     * </code>
     *
     * @since 1.1
     */
    public interface FileDropExtListener {
        /**
         * This method is called when files have been successfully dropped.
         *
         * @param evt
         *        The DropTargetDropEvent which initiated the drop operation.
         * @param files
         *        An array of <i>File</i>s that were dropped.
         * @since 2.0
         */
        void filesDropped(DropTargetDropEvent evt, File[] files);
    }
}
