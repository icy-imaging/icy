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

import icy.file.FileUtil;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.preferences.GeneralPreferences;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.EventUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.EventListener;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class OutputConsolePanel extends ToolbarPanel implements ClipboardOwner {
    private static OutputConsolePanel instance = null;

    public static OutputConsolePanel getInstance() {
        if (instance == null)
            instance = new OutputConsolePanel();

        return instance;
    }

    public interface OutputConsoleChangeListener extends EventListener {
        void outputConsoleChanged(OutputConsolePanel source, boolean isError);
    }

    private class WindowsOutPrintStream extends PrintStream {
        boolean isStdErr;

        public WindowsOutPrintStream(final PrintStream out, final boolean isStdErr) {
            super(out);

            this.isStdErr = isStdErr;
        }

        @Override
        public void write(@Nonnull final byte[] buf, final int off, final int len) {
            try {
                super.write(buf, off, len);

                final String text = new String(buf, off, len);
                addText(text, isStdErr);
                // want file log as well ?
                if (fileLogButton.isSelected() && (logWriter != null)) {
                    // write and save to file immediately
                    logWriter.write(text);
                    logWriter.flush();
                }
            }
            catch (final Throwable t) {
                addText(t.getMessage(), isStdErr);
            }
        }
    }

    private final JTextPane textPane;
    private final StyledDocument doc;
    final SimpleAttributeSet normalAttributes;
    final SimpleAttributeSet errorAttributes;

    private final JSpinner logMaxLineField;
    private final JTextField logMaxLineTextField;
    private final IcyToggleButton scrollLockButton;
    private final IcyToggleButton fileLogButton;

    int nbUpdate;
    Writer logWriter;

    public OutputConsolePanel() {
        super(new Dimension(300, 200));

        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        nbUpdate = 0;

        errorAttributes = new SimpleAttributeSet();
        normalAttributes = new SimpleAttributeSet();

        StyleConstants.setFontFamily(errorAttributes, "arial");
        StyleConstants.setFontSize(errorAttributes, 11);
        StyleConstants.setForeground(errorAttributes, Color.red.brighter());

        StyleConstants.setFontFamily(normalAttributes, "arial");
        StyleConstants.setFontSize(normalAttributes, 11);
        //StyleConstants.setForeground(normalAttributes, Color.black);

        logMaxLineField = new JSpinner(new SpinnerNumberModel(GeneralPreferences.getOutputLogSize(), 100, 1000000, 100));
        logMaxLineTextField = ((JSpinner.DefaultEditor) logMaxLineField.getEditor()).getTextField();
        final IcyButton clearLogButton = new IcyButton(GoogleMaterialDesignIcons.DELETE);
        final IcyButton copyLogButton = new IcyButton(GoogleMaterialDesignIcons.CONTENT_COPY);
        final IcyButton reportLogButton = new IcyButton(GoogleMaterialDesignIcons.BUG_REPORT);
        scrollLockButton = new IcyToggleButton(GoogleMaterialDesignIcons.LOCK_OPEN, GoogleMaterialDesignIcons.LOCK);
        fileLogButton = new IcyToggleButton(GoogleMaterialDesignIcons.FILE_DOWNLOAD);
        fileLogButton.setSelected(GeneralPreferences.getOutputLogToFile());

        // ComponentUtil.setFontSize(textPane, 10);
        textPane.setEditable(false);

        //clearLogButton.setFlat(true);
        //copyLogButton.setFlat(true);
        //reportLogButton.setFlat(true);
        //scrollLockButton.setFlat(true);
        //fileLogButton.setFlat(true);

        logMaxLineField.setPreferredSize(new Dimension(80, 24));
        // no focusable
        logMaxLineField.setFocusable(false);
        logMaxLineTextField.setFocusable(false);
        logMaxLineTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                // get focus on double click to enable manual edition
                if (EventUtil.isLeftMouseButton(e)) {
                    logMaxLineField.setFocusable(true);
                    logMaxLineTextField.setFocusable(true);
                    logMaxLineTextField.requestFocus();
                }
            }
        });
        logMaxLineTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // cancel manual edition ? --> remove focus
                    if (logMaxLineTextField.isFocusable()) {
                        logMaxLineTextField.setFocusable(false);
                        logMaxLineField.setFocusable(false);
                    }
                }
            }
        });
        logMaxLineField.addChangeListener(e -> {
            GeneralPreferences.setOutputLogSize(getLogMaxLine());

            // manual edition ? --> remove focus
            if (logMaxLineTextField.isFocusable()) {
                logMaxLineTextField.setFocusable(false);
                logMaxLineField.setFocusable(false);
            }

            try {
                limitLog();
            }
            catch (final Exception ex) {
                // ignore
            }
        });
        logMaxLineField.setToolTipText("Double-click to edit the maximum number of line (max = 1000000)");
        clearLogButton.setToolTipText("Clear all");
        copyLogButton.setToolTipText("Copy to clipboard");
        reportLogButton.setToolTipText("Report content to dev team");
        scrollLockButton.setToolTipText("Scroll Lock");
        fileLogButton.setToolTipText("Enable/Disable log file saving (log.txt)");

        clearLogButton.addActionListener(e -> textPane.setText(""));
        copyLogButton.addActionListener(e -> {
            final Clipboard clipboard = getToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(getText()), OutputConsolePanel.this);
        });
        reportLogButton.addActionListener(e -> {
            final ProgressFrame progressFrame = new ProgressFrame("Sending report...");
            try {
                // send report
                IcyExceptionHandler.report(getText());
            }
            finally {
                progressFrame.close();
            }
        });
        fileLogButton.addActionListener(e -> GeneralPreferences.setOutputLogFile(fileLogButton.isSelected()));

        final JPanel bottomPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4),
                GuiUtil.createLineBoxPanel(clearLogButton, Box.createHorizontalStrut(4), copyLogButton,
                        Box.createHorizontalStrut(4), reportLogButton, Box.createHorizontalGlue(),
                        Box.createHorizontalStrut(4), new JLabel("Limit"), Box.createHorizontalStrut(4),
                        logMaxLineField, Box.createHorizontalStrut(4), scrollLockButton, Box.createHorizontalStrut(4),
                        fileLogButton));

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textPane, BorderLayout.CENTER);

        final JScrollPane scrollPane = new JScrollPane(panel);

        setLayout(new BorderLayout());

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        validate();

        // redirect standard output
        System.setOut(new WindowsOutPrintStream(System.out, false));
        System.setErr(new WindowsOutPrintStream(System.err, true));

        try {
            // define log file writer (always clear log.txt file if present)
            logWriter = new FileWriter(FileUtil.getApplicationDirectory() + "/icy.log", false);
        }
        catch (final IOException e1) {
            logWriter = null;
        }
    }

    public void addText(final String text, final boolean isError) {
        ThreadUtil.invokeLater(() -> {
            try {
                nbUpdate++;

                // insert text
                synchronized (doc) {
                    if (isError)
                        doc.insertString(doc.getLength(), text, errorAttributes);
                    else
                        doc.insertString(doc.getLength(), text, normalAttributes);

                    // do clean sometime..
                    if ((nbUpdate & 0x7F) == 0)
                        limitLog();

                    // scroll lock feature
                    if (!scrollLockButton.isSelected())
                        textPane.setCaretPosition(doc.getLength());
                }
            }
            catch (final Exception e) {
                // ignore
            }

            changed(isError);
        });
    }

    /**
     * Get console content.
     */
    public String getText() {
        try {
            synchronized (doc) {
                return doc.getText(0, doc.getLength());
            }
        }
        catch (final BadLocationException e) {
            return "";
        }
    }

    /**
     * Apply maximum line limitation to the log output
     */
    public void limitLog() throws BadLocationException {
        final Element root = doc.getDefaultRootElement();
        final int numLine = root.getElementCount();
        final int logMaxLine = getLogMaxLine();

        // limit to maximum wanted lines
        if (numLine > logMaxLine) {
            final Element line = root.getElement(numLine - (logMaxLine + 1));
            // remove "old" lines
            doc.remove(0, line.getEndOffset());
        }
    }

    /**
     * Returns maximum log line number
     */
    public int getLogMaxLine() {
        return ((Integer) logMaxLineField.getValue()).intValue();
    }

    /**
     * Sets maximum log line number
     */
    public void setLogMaxLine(final int value) {
        logMaxLineField.setValue(Integer.valueOf(value));
    }

    private void changed(final boolean isError) {
        fireChangedEvent(isError);
    }

    public void fireChangedEvent(final boolean isError) {
        for (final OutputConsoleChangeListener listener : listenerList.getListeners(OutputConsoleChangeListener.class))
            listener.outputConsoleChanged(this, isError);
    }

    public void addOutputConsoleChangeListener(final OutputConsoleChangeListener listener) {
        listenerList.add(OutputConsoleChangeListener.class, listener);
    }

    public void removeOutputConsoleChangeListener(final OutputConsoleChangeListener listener) {
        listenerList.remove(OutputConsoleChangeListener.class, listener);
    }

    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        // ignore
    }
}
