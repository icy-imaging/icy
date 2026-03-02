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

package fr.icy.gui.frame;

import fr.icy.Icy;
import fr.icy.common.math.Random;
import fr.icy.extension.ExtensionLoader;
import fr.icy.gui.LookAndFeelUtil;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.awt.*;

public final class SplashScreenWindow extends JWindow implements ExtensionLoader.ExtensionLoaderProgressListener {
    private final JProgressBar progressBar;

    public SplashScreenWindow() {
        super();

        final int r = Random.nextInt(6);
        final String s = (LookAndFeelUtil.isDarkMode()) ? "D" : "L";

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(
                new JLabel(
                        new ImageIcon(Icy.class.getResource("/image/splash/" + s + r + ".png")),
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );

        progressBar = new JProgressBar(0, 1);
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        getContentPane().add(progressBar, BorderLayout.SOUTH);
        setBounds(0, 0, 600, 300);
        setAlwaysOnTop(true);

        pack();

        setLocationRelativeTo(null);

        ExtensionLoader.addProgressListener(this);
    }

    @Override
    public void updateProgress(final ExtensionLoader.@NonNull ExtensionLoaderProgressEvent e) {
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(e.total());

        if (e.finished()) {
            progressBar.setString("Extensions loaded: " + e.total() + "/" + e.total());
            progressBar.setValue(e.total());
            return;
        }

        //progressBar.setString("Loading " + e.name() + ": " + e.actual() + "/" + e.total());
        progressBar.setString("Loading extensions: " + e.actual() + "/" + e.total());
        progressBar.setValue(e.actual());

        //ThreadUtil.sleep(200);
    }
}
