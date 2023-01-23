package icy.gui.menu;

import icy.action.FileActions;
import icy.action.GeneralActions;
import icy.action.PreferencesActions;
import icy.file.Loader;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.abstract_.PluginSequenceImporter;
import icy.preferences.IcyPreferences;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.type.collection.list.RecentFileList;
import icy.util.StringUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.util.List;

final class ApplicationMenuFile extends AbstractApplicationMenu {
    // Menu list
    private final JMenu menuCreate;
    private final JMenu menuOpenRecent;
    private final JMenu menuImport;

    // Menuitem list
    private final JMenuItem itemCreateSequence;
    private final JMenuItem itemCreateGraySequence;
    private final JMenuItem itemCreateRGBSequence;
    private final JMenuItem itemCreateRGBASequence;
    private final JMenuItem itemOpen;
    private final JMenuItem itemCloseSequence;
    private final JMenuItem itemCloseOther;
    private final JMenuItem itemCloseAll;
    private final JMenuItem itemSaveSequence;
    private final JMenuItem itemSaveSequenceAs;
    private final JMenuItem itemSaveMetadata;
    private JMenuItem itemPreferences;
    private JMenuItem itemQuit;

    public ApplicationMenuFile() {
        super("File");

        menuCreate = new JMenu("New");
        setIcon(menuCreate, GoogleMaterialDesignIcons.NOTE_ADD);
        add(menuCreate);

        itemCreateSequence = new JMenuItem("Sequence");
        setIcon(itemCreateSequence, GoogleMaterialDesignIcons.IMAGE);
        itemCreateSequence.addActionListener(FileActions.newSequenceAction);
        menuCreate.add(itemCreateSequence);

        itemCreateGraySequence = new JMenuItem("Grayscale sequence", new IcyIcon("gray", false));
        itemCreateGraySequence.addActionListener(FileActions.newGraySequenceAction);
        menuCreate.add(itemCreateGraySequence);

        itemCreateRGBSequence = new JMenuItem("RGB sequence", new IcyIcon("rgb", false));
        itemCreateRGBSequence.addActionListener(FileActions.newRGBSequenceAction);
        menuCreate.add(itemCreateRGBSequence);

        itemCreateRGBASequence = new JMenuItem("RGBA sequence", new IcyIcon("argb", false));
        itemCreateRGBASequence.addActionListener(FileActions.newARGBSequenceAction);
        menuCreate.add(itemCreateRGBASequence);

        itemOpen = new JMenuItem("Open...");
        setIcon(itemOpen, GoogleMaterialDesignIcons.FOLDER_OPEN);
        itemOpen.addActionListener(FileActions.openSequenceAction);
        add(itemOpen);

        // TODO Make recent files menu
        menuOpenRecent = new JMenu("Open Recent");
        setIcon(menuOpenRecent, GoogleMaterialDesignIcons.FOLDER);
        menuOpenRecent.setEnabled(false);
        add(menuOpenRecent);

        final RecentFileList recentFileList = new RecentFileList(IcyPreferences.applicationRoot().node("loader"));
        // remove obsolete entries
        recentFileList.clean();
        final int nbRecentFiles = recentFileList.getSize();
        for (int i = 0; i < nbRecentFiles; i ++) {
            final String entry = recentFileList.getEntryAsName(i, 100, true);
            if (!StringUtil.isEmpty(entry)) {
                System.err.println(entry);
                final JMenuItem itemFile = new JMenuItem(entry);
                final String[] paths = recentFileList.getEntry(i);
                itemFile.addActionListener(e -> Loader.load(CollectionUtil.asList(paths), false, true, true));
                menuOpenRecent.add(itemFile);
                if (!menuOpenRecent.isEnabled())
                    menuOpenRecent.setEnabled(true);
            }
        }

        itemCloseSequence = new JMenuItem("Close Sequence");
        setIcon(itemCloseSequence, GoogleMaterialDesignIcons.CLEAR);
        itemCloseSequence.addActionListener(FileActions.closeCurrentSequenceAction);
        add(itemCloseSequence);

        itemCloseOther = new JMenuItem("Close Other Sequences");
        setIcon(itemCloseOther, GoogleMaterialDesignIcons.CLEAR_ALL);
        itemCloseOther.addActionListener(FileActions.closeOthersSequencesAction);
        add(itemCloseOther);

        itemCloseAll = new JMenuItem("Close All Sequences");
        setIcon(itemCloseAll, GoogleMaterialDesignIcons.CLEAR_ALL);
        itemCloseAll.addActionListener(FileActions.closeAllSequencesAction);
        add(itemCloseAll);

        addSeparator();

        itemSaveSequence = new JMenuItem("Save Sequence");
        setIcon(itemSaveSequence, GoogleMaterialDesignIcons.SAVE);
        itemSaveSequence.addActionListener(FileActions.saveSequenceAction);
        add(itemSaveSequence);

        itemSaveSequenceAs = new JMenuItem("Save Sequence As...");
        setIcon(itemSaveSequenceAs, GoogleMaterialDesignIcons.SAVE);
        itemSaveSequenceAs.addActionListener(FileActions.saveAsSequenceAction);
        add(itemSaveSequenceAs);

        itemSaveMetadata = new JMenuItem("Save Metadata");
        setIcon(itemSaveMetadata, GoogleMaterialDesignIcons.ART_TRACK);
        itemSaveMetadata.addActionListener(FileActions.saveMetaDataAction);
        add(itemSaveMetadata);

        addSeparator();

        // TODO Make import actions
        menuImport = new JMenu("Import");
        add(menuImport);

        if (!SystemUtil.isMac()) {
            addSeparator();

            itemPreferences = new JMenuItem("Preferences...");
            setIcon(itemPreferences, GoogleMaterialDesignIcons.SETTINGS);
            itemPreferences.addActionListener(PreferencesActions.preferencesAction);
            add(itemPreferences);

            addSeparator();

            itemQuit = new JMenuItem("Quit Icy");
            setIcon(itemQuit, GoogleMaterialDesignIcons.POWER_SETTINGS_NEW);
            itemQuit.addActionListener(GeneralActions.exitApplicationAction);
            add(itemQuit);
        }

        reloadFileMenu();
        enableImport(false); // TODO: 19/01/2023 Remove this when reload import finished

        addSkinChangeListener();
        addGlobalSequenceListener();
    }

    private void enableFileMenu(final boolean b) {
        itemCloseSequence.setEnabled(b);
        itemCloseAll.setEnabled(b);
        itemSaveSequence.setEnabled(b);
        itemSaveSequenceAs.setEnabled(b);
        itemSaveMetadata.setEnabled(b);
    }

    private void enableCloseOther(final boolean b) {
        itemCloseOther.setEnabled(b);
    }

    private void enableImport(final boolean b) {
        menuImport.setEnabled(b);
    }

    private void reloadFileMenu() {
        final List<Sequence> sequences = Icy.getMainInterface().getSequences();
        final int size = sequences.size();
        if (size == 0) {
            // No sequence -> disable all
            enableFileMenu(false);
            enableCloseOther(false);
        }
        else {
            enableFileMenu(true);
            // Two or more sequences -> enable Close Other
            enableCloseOther(size > 1);
        }
    }

    private void reloadImportMenu() {
        ThreadUtil.invokeLater(() -> {
            final List<PluginDescriptor> plugins = PluginLoader.getPlugins(PluginSequenceImporter.class);
            for (final PluginDescriptor descriptor : plugins) {
                // TODO add plugin importer menu
            }
        });
    }

    @Override
    public void sequenceOpened(final Sequence sequence) {
        reloadFileMenu();
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
        reloadFileMenu();
    }

    @Override
    public void pluginLoaderChanged(final PluginLoader.PluginLoaderEvent e) {
        reloadImportMenu();
    }
}
