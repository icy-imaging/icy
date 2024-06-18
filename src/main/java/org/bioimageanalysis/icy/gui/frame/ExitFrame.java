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

package org.bioimageanalysis.icy.gui.frame;

import org.bioimageanalysis.icy.Icy;

import javax.swing.*;
import java.awt.*;

public class ExitFrame extends JFrame {

    private JPanel buttonPanel;

    boolean forced;
    Timer timer;
    private JPanel forceQuitPanel;

    /**
     * Create the frame.
     */
    public ExitFrame(final int forceDelay) {
        super();

        forced = false;
        timer = new Timer(forceDelay, e -> displayForcePanel());

        initialize();

        // default
        forceQuitPanel.setVisible(false);
        buttonPanel.setVisible(false);

        if (forceDelay > 0)
            timer.start();
        else if (forceDelay == 0)
            displayForcePanel();

        getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        // pack, center and display
        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public ExitFrame() {
        this(-1);
    }

    public void displayForcePanel() {
        forceQuitPanel.setVisible(true);
        buttonPanel.setVisible(true);

        // pack and center
        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
    }

    public boolean isForced() {
        return forced;
    }

    // TODO: 26/05/2023 Remove setFont and setBorder for new ui
    void initialize() {
        setTitle("Exit");
        setSize(new Dimension(400, 140));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel_1 = new JPanel();
        //panel_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        getContentPane().add(panel_1, BorderLayout.CENTER);
        panel_1.setLayout(new BorderLayout(0, 0));

        buttonPanel = new JPanel();
        panel_1.add(buttonPanel, BorderLayout.SOUTH);

        final JButton button = new JButton("Force Quit");
        button.addActionListener(e -> {
            forced = true;
            // close frame
            dispose();
        });
        //button.setFont(new Font("Tahoma", Font.BOLD, 14));
        buttonPanel.add(button);

        final JPanel panel = new JPanel();
        panel_1.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        final JPanel panel_2 = new JPanel();
        panel.add(panel_2);

        final JLabel label = new JLabel("Please wait while exiting...");
        panel_2.add(label);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        //label.setFont(new Font("Tahoma", Font.BOLD, 16));

        forceQuitPanel = new JPanel();
        panel.add(forceQuitPanel);

        final JLabel forceQuitLabel = new JLabel("Click on 'Force Quit' to kill remaining tasks and exit.");
        forceQuitPanel.add(forceQuitLabel);
        //forceQuitLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        forceQuitLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
}
