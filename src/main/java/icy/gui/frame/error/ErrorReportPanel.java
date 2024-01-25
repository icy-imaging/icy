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

package icy.gui.frame.error;

import icy.gui.component.IcyTextField;
import icy.preferences.GeneralPreferences;
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
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ErrorReportPanel extends JPanel {
    // GUI
    private JTextPane errorMessageTextPane;
    private JTextPane commentTextPane;
    private IcyTextField emailTextField;
    JButton reportButton;
    JButton closeButton;
    private JLabel label;

    public ErrorReportPanel(final Icon icon, final String title, final String message) {
        super();

        initialize();

        if (!StringUtil.isEmpty(title))
            label.setText(title);
        if (icon != null)
            label.setIcon(icon);

        try {
            errorMessageTextPane.getStyledDocument().insertString(errorMessageTextPane.getStyledDocument().getLength(), message, new SimpleAttributeSet());
        }
        catch (final BadLocationException e) {
            IcyLogger.error(ErrorReportPanel.class, e, "PluginErrorReport(...) error.");
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

        // set default email
        emailTextField.setText(GeneralPreferences.getUserEmail());
    }

    @SuppressWarnings("unused")
    ErrorReportPanel() {
        this(null, "Test", "An error occured");
    }

    private void initialize() {
        // top
        label = new JLabel("An error occured !", SwingConstants.CENTER);

        // center
        errorMessageTextPane = new JTextPane();
        errorMessageTextPane.setEditable(false);
        //errorMessageTextPane.setContentType("text/html");

        final JScrollPane messageScrollPane = new JScrollPane(errorMessageTextPane);

        final JPanel messagePanel = new JPanel();
        messagePanel.setBorder(new TitledBorder(null, "Message", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        messagePanel.setLayout(new BorderLayout(0, 0));
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        final JPanel userPanel = new JPanel();
        userPanel.setBorder(new TitledBorder(null, "Comment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        userPanel.setLayout(new BorderLayout(0, 0));

        // buttons panel
        reportButton = new JButton("Report");
        closeButton = new JButton("Close");

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(reportButton);
        buttonsPanel.add(closeButton);

        // bottom
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 0));

        bottomPanel.add(userPanel, BorderLayout.CENTER);

        final JPanel commentPanel = new JPanel();
        userPanel.add(commentPanel, BorderLayout.CENTER);
        commentPanel.setLayout(new BorderLayout(0, 0));

        // comment pane
        commentTextPane = new JTextPane();
        commentTextPane.setEditable(true);

        final JScrollPane scComment = new JScrollPane(commentTextPane);
        commentPanel.add(scComment, BorderLayout.NORTH);
        scComment.setPreferredSize(new Dimension(23, 60));
        scComment.setMinimumSize(new Dimension(23, 60));

        final JPanel emailPanel = new JPanel();
        userPanel.add(emailPanel, BorderLayout.SOUTH);
        final GridBagLayout gbl_emailPanel = new GridBagLayout();
        gbl_emailPanel.columnWidths = new int[]{0, 0, 0};
        gbl_emailPanel.rowHeights = new int[]{0, 0};
        gbl_emailPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_emailPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        emailPanel.setLayout(gbl_emailPanel);

        final JLabel lblEmail = new JLabel("Your email");
        final GridBagConstraints gbc_lblEmail = new GridBagConstraints();
        gbc_lblEmail.insets = new Insets(0, 0, 0, 5);
        gbc_lblEmail.anchor = GridBagConstraints.WEST;
        gbc_lblEmail.gridx = 0;
        gbc_lblEmail.gridy = 0;
        emailPanel.add(lblEmail, gbc_lblEmail);

        emailTextField = new IcyTextField();
        emailTextField.setToolTipText("You can enter your email so the developer can contact you if you wish");
        final GridBagConstraints gbc_emailTextField = new GridBagConstraints();
        gbc_emailTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_emailTextField.gridx = 1;
        gbc_emailTextField.gridy = 0;
        emailPanel.add(emailTextField, gbc_emailTextField);
        emailTextField.setColumns(10);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout(0, 0));

        add(label, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * @return Returns formatted report message (ready to send to web site).
     *
     * @throws BadLocationException exception
     */
    public String getReportMessage() throws BadLocationException {
        final String email = emailTextField.getText();
        final Document commentDoc = commentTextPane.getDocument();
        final Document errorDoc = errorMessageTextPane.getDocument();
        final String comment = commentDoc.getText(0, commentDoc.getLength());
        String result = "";

        if (!StringUtil.isEmpty(email)) {
            result += "Email: " + email + "\n";
            GeneralPreferences.setUserEmail(email);
        }
        if (!StringUtil.isEmpty(comment))
            result += "Comment:\n" + comment + "\n\n";

        result += errorDoc.getText(0, errorDoc.getLength());

        return result;
    }
}
