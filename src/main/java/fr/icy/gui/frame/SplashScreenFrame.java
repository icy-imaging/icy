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

package fr.icy.gui.frame;

import fr.icy.common.math.Random;
import fr.icy.common.reflect.ClassUtil;
import fr.icy.io.FileUtil;
import fr.icy.io.ResourceUtil;
import fr.icy.model.image.ImageUtil;
import fr.icy.shared.logging.CustomLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Animated ICY Logo.
 *
 * @author Fabrice de Chaumont
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class SplashScreenFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(SplashScreenFrame.class.getName());

    public static class SplashPanel extends JPanel {
        private static final String SPLASH_FOLDER = ResourceUtil.IMAGE_PATH + "splash";
        private static final int DEFAULT_WIDTH = 960;
        private static final int DEFAULT_HEIGTH = 300;

        private BufferedImage image;

        public SplashPanel() {
            super(new BorderLayout());
            /*image = null;

            String[] files = FileUtil.getFiles(SplashPanel.class.getResource(SPLASH_FOLDER).getFile(), null, false, false, false);

            LOGGER.log(CustomLevel.DEBUG, "Loading splashscreen image from " + SPLASH_FOLDER + ". Found " + files.length + " files.");

            if (files.length > 0)
                image = ImageUtil.load(files[Random.nextInt(files.length)]);
            else {
                try {
                    files = ClassUtil.getResourcesInPackage(SPLASH_FOLDER, null, true, false, false, false).toArray(new String[0]);
                    if (files.length > 0) {
                        final URL url = getClass().getResource("/" + files[Random.nextInt(files.length)]);
                        if (url != null)
                            image = ImageUtil.load(url, true);
                    }
                }
                catch (final Exception e) {
                    LOGGER.log(CustomLevel.WARNING, "Cannot load splashscreen image.", e);
                }
            }

            if (image != null)
                setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            else
                setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGTH));*/

            setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGTH));
            JLabel root = new JLabel("Loading extensions, please wait…");
            add(root, BorderLayout.CENTER);

            /*JProgressBar pb = new JProgressBar(0, 100);
            pb.setIndeterminate(true);
            add(pb, BorderLayout.SOUTH);*/
        }

        /*@Override
        public void paint(final Graphics g) {
            super.paint(g);

            //if (image != null)
                //g.drawImage(image, 0, 0, this);
        }*/
    }

    public SplashScreenFrame() {
        super("Icy");

        setAlwaysOnTop(true);

        final SplashPanel splash = new SplashPanel();

        setUndecorated(true);

        add(splash);
        pack();

        setLocationRelativeTo(null);

    }
}
