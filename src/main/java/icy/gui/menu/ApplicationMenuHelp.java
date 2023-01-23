package icy.gui.menu;

import icy.network.NetworkUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public final class ApplicationMenuHelp extends AbstractApplicationMenu {
    private final JMenuItem itemHelp;
    private final JMenuItem itemGettingStarted;
    private final JMenuItem itemSubmitBug;
    private final JMenuItem itemShowLog;

    public ApplicationMenuHelp() {
        super("Help");

        itemHelp = new JMenuItem("Get Help");
        setIcon(itemHelp, GoogleMaterialDesignIcons.HELP_OUTLINE);
        itemHelp.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.IMAGE_SC_ICY_URL));
        itemHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));
        add(itemHelp);

        itemGettingStarted = new JMenuItem("Getting Started");
        setIcon(itemGettingStarted, GoogleMaterialDesignIcons.FLAG);
        itemGettingStarted.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL + "trainings/"));
        add(itemGettingStarted);

        // TODO change action to use Icy's internal bug report system ?
        itemSubmitBug = new JMenuItem("Submit a Bug Report");
        setIcon(itemSubmitBug, GoogleMaterialDesignIcons.BUG_REPORT);
        itemSubmitBug.addActionListener(e -> NetworkUtil.openBrowser("https://gitlab.pasteur.fr/bia/icy/-/issues"));
        add(itemSubmitBug);

        // TODO: 19/01/2023 Check if log exist before trying to open it ?
        itemShowLog = new JMenuItem("Show Log");
        setIcon(itemShowLog, GoogleMaterialDesignIcons.DESCRIPTION);
        itemShowLog.addActionListener(e -> {
            try {
                final File log = new File("./icy.log");
                if (log.isFile())
                    Desktop.getDesktop().open(log);
            }
            catch (IOException ex) {
                System.err.println("An error occured while opening log file");
            }
        });
        add(itemShowLog);

        addSkinChangeListener();
    }
}
