/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

package org.bioimageanalysis.icy.gui.dialog;

import javax.swing.*;
import java.awt.*;

public class ExtensionInstallerDialog extends JDialog {
    public ExtensionInstallerDialog() {
        super();

        setTitle("Extension Installer");
        setModalityType(ModalityType.APPLICATION_MODAL);

        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }
}
