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

import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.preferences.GeneralPreferences;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import icy.system.IcyExceptionHandler;
import icy.util.EventUtil;
import org.jetbrains.annotations.NotNull;

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
import java.io.PrintStream;

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

    private class WindowsOutPrintStream extends PrintStream {
        boolean isStdErr;

        public WindowsOutPrintStream(final PrintStream out, final boolean isStdErr) {
            super(out);

            this.isStdErr = isStdErr;
        }

        @Override
        public void write(final byte @NotNull [] buf, final int off, final int len) {
            try {
                super.write(buf, off, len);

                final String text = new String(buf, off, len);
                if (isStdErr)
                    textPane.append(Color.RED.darker(), text);
                textPane.appendANSI(text);
                // want file log as well ?
                /*if (fileLogButton.isSelected() && (logWriter != null)) {
                    // write and save to file immediately
                    logWriter.write(text);
                    logWriter.flush();
                }*/
            }
            catch (final Throwable t) {
                textPane.append(Color.RED.darker(), t.getLocalizedMessage());
            }
        }
    }

    private class ColorPane extends JTextPane {
        private static final Color D_Black = Color.getHSBColor(0.000f, 0.000f, 0.000f);
        private static final Color D_Red = Color.getHSBColor(0.000f, 1.000f, 0.502f);
        private static final Color D_Blue = Color.getHSBColor(0.667f, 1.000f, 0.502f);
        private static final Color D_Magenta = Color.getHSBColor(0.833f, 1.000f, 0.502f);
        private static final Color D_Green = Color.getHSBColor(0.333f, 1.000f, 0.502f);
        private static final Color D_Yellow = Color.getHSBColor(0.167f, 1.000f, 0.502f);
        private static final Color D_Cyan = Color.getHSBColor(0.500f, 1.000f, 0.502f);
        private static final Color D_White = Color.getHSBColor(0.000f, 0.000f, 0.753f);
        private static final Color B_Black = Color.getHSBColor(0.000f, 0.000f, 0.502f);
        private static final Color B_Red = Color.getHSBColor(0.000f, 1.000f, 1.000f);
        private static final Color B_Blue = Color.getHSBColor(0.667f, 1.000f, 1.000f);
        private static final Color B_Magenta = Color.getHSBColor(0.833f, 1.000f, 1.000f);
        private static final Color B_Green = Color.getHSBColor(0.333f, 1.000f, 1.000f);
        private static final Color B_Yellow = Color.getHSBColor(0.167f, 1.000f, 1.000f);
        private static final Color B_Cyan = Color.getHSBColor(0.500f, 1.000f, 1.000f);
        private static final Color B_White = Color.getHSBColor(0.000f, 0.000f, 1.000f);
        private static final Color cReset = Color.getHSBColor(0.000f, 0.000f, 1.000f);
        private static Color colorCurrent = cReset;
        String remaining = "";

        public void append(final Color c, final String s) {
            final StyleContext sc = StyleContext.getDefaultStyleContext();
            final AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

            synchronized (doc) {
                try {
                    doc.insertString(doc.getLength(), s, aset);
                    setCaretPosition(doc.getLength());
                    limitLog();
                }
                catch (final BadLocationException e) {
                    // ignore
                }
            }
        }

        public void appendANSI(final String s) { // convert ANSI color codes first
            int aPos = 0;   // current char position in addString
            int aIndex; // index of next Escape sequence
            int mIndex; // index of "m" terminating Escape sequence
            String tmpString; // = "";
            boolean stillSearching; // true until no more Escape sequences
            final String addString = remaining + s;
            remaining = "";

            if (addString.length() > 0) {
                aIndex = addString.indexOf("\u001B"); // find first escape
                if (aIndex == -1) { // no escape/color change in this string, so just send it with current color
                    append(colorCurrent, addString);
                    return;
                }
                // otherwise There is an escape character in the string, so we must process it

                if (aIndex > 0) { // Escape is not first char, so send text up to first escape
                    tmpString = addString.substring(0, aIndex);
                    append(colorCurrent, tmpString);
                    aPos = aIndex;
                }
                // aPos is now at the beginning of the first escape sequence

                stillSearching = true;
                while (stillSearching) {
                    mIndex = addString.indexOf("m", aPos); // find the end of the escape sequence
                    if (mIndex < 0) { // the buffer ends halfway through the ansi string!
                        remaining = addString.substring(aPos);
                        stillSearching = false;
                        continue;
                    }
                    else {
                        tmpString = addString.substring(aPos, mIndex + 1);
                        colorCurrent = getANSIColor(tmpString);
                    }
                    aPos = mIndex + 1;
                    // now we have the color, send text that is in that color (up to next escape)

                    aIndex = addString.indexOf("\u001B", aPos);

                    if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
                        tmpString = addString.substring(aPos);
                        append(colorCurrent, tmpString);
                        stillSearching = false;
                        continue; // jump out of loop early, as the whole string has been sent now
                    }

                    // there is another escape sequence, so send part of the string and prepare for the next
                    tmpString = addString.substring(aPos, aIndex);
                    aPos = aIndex;
                    append(colorCurrent, tmpString);

                } // while there's text in the input buffer
            }
        }

        public Color getANSIColor(final String ANSIColor) {
            return switch (ANSIColor) {
                case "\u001B[30m", "\u001B[0;30m" -> D_Black;
                case "\u001B[31m", "\u001B[0;31m" -> B_Red; //D_Red;
                case "\u001B[32m", "\u001B[0;32m" -> B_Green; //D_Green;
                case "\u001B[33m", "\u001B[0;33m" -> B_Yellow; //D_Yellow;
                case "\u001B[34m", "\u001B[0;34m" -> D_Blue;
                case "\u001B[35m", "\u001B[0;35m" -> D_Magenta;
                case "\u001B[36m", "\u001B[0;36m" -> B_Cyan; //D_Cyan;
                case "\u001B[37m", "\u001B[0;37m" -> D_White;
                case "\u001B[1;30m" -> B_Black;
                case "\u001B[1;31m" -> B_Red;
                case "\u001B[1;32m" -> B_Green;
                case "\u001B[1;33m" -> B_Yellow;
                case "\u001B[1;34m" -> B_Blue;
                case "\u001B[1;35m" -> B_Magenta;
                case "\u001B[1;36m" -> B_Cyan;
                case "\u001B[1;37m" -> B_White;
                case "\u001B[0m" -> cReset;
                default -> B_White;
            };
        }
    }

    private final ColorPane textPane;
    private final StyledDocument doc;

    private final JSpinner logMaxLineField;
    private final JTextField logMaxLineTextField;
    private final IcyToggleButton fileLogButton;

    //private Writer logWriter;

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public OutputConsolePanel() {
        super(new Dimension(300, 200));

        textPane = new ColorPane();
        doc = textPane.getStyledDocument();

        logMaxLineField = new JSpinner(new SpinnerNumberModel(GeneralPreferences.getOutputLogSize(), 100, 1000000, 100));
        logMaxLineTextField = ((JSpinner.DefaultEditor) logMaxLineField.getEditor()).getTextField();
        final IcyButton clearLogButton = new IcyButton(SVGIcon.DELETE);
        final IcyButton copyLogButton = new IcyButton(SVGIcon.CONTENT_COPY);
        final IcyButton reportLogButton = new IcyButton(SVGIcon.BUG_REPORT);
        final IcyToggleButton scrollLockButton = new IcyToggleButton(new SVGIconPack(SVGIcon.LOCK_OPEN, SVGIcon.LOCK));
        fileLogButton = new IcyToggleButton(SVGIcon.FILE_SAVE);
        fileLogButton.setSelected(GeneralPreferences.getOutputLogToFile());

        textPane.setEditable(false);
        textPane.setRequestFocusEnabled(false);
        textPane.setFocusable(false);
        textPane.setDragEnabled(false);
        textPane.setBackground(Color.DARK_GRAY);

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
        fileLogButton.setToolTipText("Enable/Disable log file saving (icy.log)");

        fileLogButton.setSelected(false);
        fileLogButton.setEnabled(false);

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

        final JPanel bottomPanel = GuiUtil.createPageBoxPanel(
                Box.createVerticalStrut(4),
                GuiUtil.createLineBoxPanel(
                        clearLogButton,
                        Box.createHorizontalStrut(4),
                        copyLogButton,
                        Box.createHorizontalStrut(4),
                        reportLogButton,
                        Box.createHorizontalGlue(),
                        Box.createHorizontalStrut(4),
                        new JLabel("Limit"),
                        Box.createHorizontalStrut(4),
                        logMaxLineField,
                        Box.createHorizontalStrut(4),
                        scrollLockButton,
                        Box.createHorizontalStrut(4),
                        fileLogButton
                )
        );

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

        /*try {
            // define log file writer (always clear log.txt file if present)
            logWriter = new FileWriter(FileUtil.getApplicationDirectory() + "/icy.log", false);
        }
        catch (final IOException e1) {
            logWriter = null;
        }*/
    }

    /**
     * Get console content.
     */
    private String getText() {
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
    private void limitLog() throws BadLocationException {
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
    private int getLogMaxLine() {
        return ((Integer) logMaxLineField.getValue()).intValue();
    }

    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        // ignore
    }
}
