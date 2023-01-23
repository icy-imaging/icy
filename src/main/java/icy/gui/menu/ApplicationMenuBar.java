package icy.gui.menu;

import javax.swing.*;

public final class ApplicationMenuBar extends JMenuBar {
    public ApplicationMenuBar() {
        final JMenu menuFile = new ApplicationMenuFile();
        add(menuFile);

        final JMenu menuSequence = new ApplicationMenuSequence();
        add(menuSequence);

        final JMenu menuROI = new ApplicationMenuROI();
        add(menuROI);

        final JMenu menuPlugins = new ApplicationMenuPlugins();
        add(menuPlugins);

        final JMenu menuView = new ApplicationMenuView();
        add(menuView);

        final JMenu menuHelp = new ApplicationMenuHelp();
        add(menuHelp);
    }
}
