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

package icy.gui.plugin;

import icy.gui.frame.error.ErrorReportFrame;
import icy.plugin.PluginDescriptor;
import icy.system.logging.IcyLogger;
import icy.util.StringUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @deprecated Use {@link ErrorReportFrame} instead
 */
@Deprecated(since = "2.4.3", forRemoval = true)
public class PluginErrorReportPanel extends JPanel {
    // GUI
    JTextPane errorMessageTextPane;
    JLabel label;
    JTextPane commentTextPane;
    JButton reportButton;
    JButton closeButton;
    JPanel bottomPanel;
    JScrollPane messageScrollPane;
    JPanel commentPanel;
    JPanel messagePanel;

    final PluginDescriptor plugin;
    final String devId;
    final String title;
    final String message;

    public PluginErrorReportPanel(final PluginDescriptor plugin, final String devId, final String title, final String message) {
        super();

        this.plugin = plugin;
        this.devId = devId;
        this.title = message;
        this.message = message;

        initialize();

        String str;

        if (plugin != null)
            str = "<html><br>The plugin named <b>" + plugin.getName() + "</b> has encountered a problem";
        else
            str = "<html><br>The plugin from the developer <b>" + devId + "</b> has encountered a problem";

        if (StringUtil.isEmpty(title))
            str += ".<br><br>";
        else
            str += " :<br><i>" + title + "</i><br><br>";

        str += "Reporting this problem is anonymous and will help improving this plugin.<br><br></html>";

        label.setText(str);

        try {
            errorMessageTextPane.getStyledDocument().insertString(errorMessageTextPane.getStyledDocument().getLength(),
                    message, new SimpleAttributeSet());
        }
        catch (final BadLocationException e) {
            IcyLogger.error(PluginErrorReportPanel.class, e, "PluginErrorReport(...) error.");
        }
        errorMessageTextPane.setCaretPosition(0);

        final Document doc = commentTextPane.getDocument();

        try {
            final SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setItalic(attributes, true);
            StyleConstants.setForeground(attributes, Color.GRAY);
            doc.insertString(0, "Please type here your comment", attributes);
        }
        catch (final BadLocationException e1) {
            // ignore
        }

        commentTextPane.addMouseListener(new MouseAdapter() {
            // Displays a message at the beginning that
            // disappears when first clicked
            boolean firstClickDone = false;

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (!firstClickDone) {
                    commentTextPane.setText("");

                    final SimpleAttributeSet attributes = new SimpleAttributeSet();
                    StyleConstants.setItalic(attributes, false);
                    StyleConstants.setForeground(attributes, Color.BLACK);
                    try {
                        doc.insertString(0, " ", attributes);
                    }
                    catch (final BadLocationException e1) {
                        // ignore
                    }

                    firstClickDone = true;
                }
            }
        });
    }

    @SuppressWarnings("unused")
    PluginErrorReportPanel() {
        this(new PluginDescriptor(plugins.kernel.canvas.Canvas2DPlugin.class), null, null, "Error !!");
    }

    private void initialize() {
        if (plugin != null)
            label = new JLabel("", plugin.getIcon(), SwingConstants.CENTER);
        else
            label = new JLabel("", SwingConstants.CENTER);

        // center
        errorMessageTextPane = new JTextPane();
        errorMessageTextPane.setEditable(false);
        errorMessageTextPane.setContentType("text/html");

        messageScrollPane = new JScrollPane(errorMessageTextPane);

        messagePanel = new JPanel();
        messagePanel.setBorder(new TitledBorder(null, "Message", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        messagePanel.setLayout(new BorderLayout(0, 0));
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        // comment pane
        commentTextPane = new JTextPane();
        commentTextPane.setEditable(true);
        commentTextPane.setToolTipText("Give here some informations about the context or how to reproduce the bug to help the developer in resolving the issue");

        final JScrollPane scComment = new JScrollPane(commentTextPane);
        scComment.setPreferredSize(new Dimension(23, 60));
        scComment.setMinimumSize(new Dimension(23, 60));

        commentPanel = new JPanel();
        commentPanel.setBorder(new TitledBorder(null, "Comment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        commentPanel.setLayout(new BorderLayout(0, 0));
        commentPanel.add(scComment, BorderLayout.CENTER);

        // buttons panel
        reportButton = new JButton("Report");
        closeButton = new JButton("Close");

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(reportButton);
        buttonsPanel.add(closeButton);

        // bottom
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 0));

        bottomPanel.add(commentPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout(0, 0));

        add(label, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
