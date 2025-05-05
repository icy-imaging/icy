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

package org.bioimageanalysis.updater;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * @author stephane
 * @author Thomas Musset
 */
public class UpdateFrame extends JFrame {
    /**
     * gui
     */
    final JLabel title = new JLabel();
    final JProgressBar progress = new JProgressBar();
    final JTextPane infos = new JTextPane();
    final JButton closeBtn = new JButton("close");

    private final SimpleAttributeSet errorAttributes;
    private final SimpleAttributeSet normalAttributes;
    private final StyledDocument doc;

    public UpdateFrame(final String title) throws HeadlessException {
        super(title);

        errorAttributes = new SimpleAttributeSet();
        normalAttributes = new SimpleAttributeSet();

        StyleConstants.setFontFamily(errorAttributes, "arial");
        StyleConstants.setFontSize(errorAttributes, 11);
        StyleConstants.setForeground(errorAttributes, Color.red.brighter());

        StyleConstants.setFontFamily(normalAttributes, "arial");
        StyleConstants.setFontSize(normalAttributes, 11);
        //StyleConstants.setForeground(normalAttributes, Color.black);

        doc = infos.getStyledDocument();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);

        setMinimumSize(new Dimension(640, 300));
        setPreferredSize(new Dimension(640, 300));
        setLocation(150, 150);

        build();
    }

    public void build() {
        setLayout(new BorderLayout());

        final JPanel topPanel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        final JPanel labelPanel = new JPanel();
        final JPanel progressPanel = new JPanel();

        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.LINE_AXIS));

        title.setText("Waiting shutdown, please wait...");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setHorizontalTextPosition(SwingConstants.CENTER);

        infos.setEditable(false);
        infos.setMinimumSize(new Dimension(540, 160));
        infos.setPreferredSize(new Dimension(540, 160));

        progress.setMinimum(0);
        progress.setMaximum(100);

        closeBtn.setEnabled(false);
        closeBtn.addActionListener(e -> {
            // close frame
            UpdateFrame.this.dispose();
        });

        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(title);
        labelPanel.add(Box.createHorizontalGlue());

        progressPanel.add(Box.createHorizontalGlue());
        progressPanel.add(progress);
        progressPanel.add(Box.createHorizontalGlue());

        topPanel.add(labelPanel);
        topPanel.add(progressPanel);

        mainPanel.add(new JScrollPane(infos), BorderLayout.CENTER);

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(closeBtn);
        bottomPanel.add(Box.createHorizontalGlue());

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        validate();
        pack();
    }

    @Override
    public void setTitle(final String text) {
        title.setText(text);
    }

    public void setCanClose(final boolean value) {
        closeBtn.setEnabled(value);
    }

    public void addMessage(final String message, final boolean error) {
        try {
            // insert text
            synchronized (doc) {
                if (error)
                    doc.insertString(doc.getLength(), message, errorAttributes);
                else
                    doc.insertString(doc.getLength(), message, normalAttributes);

                infos.setCaretPosition(doc.getLength());
            }
        }
        catch (final Exception e) {
            // ignore
        }
    }

    public void setProgress(final int value) {
        progress.setValue(value);
    }

    public void setProgressVisible(final boolean value) {
        progress.setVisible(value);
    }
}
