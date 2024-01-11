/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.gui.menu;

import icy.action.FileActions;
import icy.action.GeneralActions;
import icy.action.PreferencesActions;
import icy.file.Loader;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas MUSSET
 */
public final class ApplicationMenuFile extends AbstractApplicationMenu {
    @NotNull
    private static final ApplicationMenuFile instance = new ApplicationMenuFile();

    @NotNull
    public static synchronized ApplicationMenuFile getInstance() {
        return instance;
    }

    @NotNull
    private final IcyMenu menuImport;
    @NotNull
    private final IcyMenuItem itemCloseSequence;
    @NotNull
    private final IcyMenuItem itemCloseOther;
    @NotNull
    private final IcyMenuItem itemCloseAll;
    @NotNull
    private final IcyMenuItem itemSaveSequence;
    @NotNull
    private final IcyMenuItem itemSaveSequenceAs;
    @NotNull
    private final IcyMenuItem itemSaveMetadata;

    private ApplicationMenuFile() {
        super("File");

        final IcyMenu menuCreate = new IcyMenu("New", GoogleMaterialDesignIcons.NOTE_ADD);
        add(menuCreate);

        final IcyMenuItem itemCreateSequence = new IcyMenuItem("Sequence", GoogleMaterialDesignIcons.IMAGE);
        itemCreateSequence.addActionListener(FileActions.newSequenceAction);
        menuCreate.add(itemCreateSequence);

        final IcyMenuItem itemCreateGraySequence = new IcyMenuItem("Grayscale sequence", new IcyIcon("gray", 18, false));
        itemCreateGraySequence.addActionListener(FileActions.newGraySequenceAction);
        menuCreate.add(itemCreateGraySequence);

        final IcyMenuItem itemCreateRGBSequence = new IcyMenuItem("RGB sequence", new IcyIcon("rgb", 18, false));
        itemCreateRGBSequence.addActionListener(FileActions.newRGBSequenceAction);
        menuCreate.add(itemCreateRGBSequence);

        final IcyMenuItem itemCreateRGBASequence = new IcyMenuItem("RGBA sequence", new IcyIcon("argb", 18, false));
        itemCreateRGBASequence.addActionListener(FileActions.newARGBSequenceAction);
        menuCreate.add(itemCreateRGBASequence);

        final IcyMenuItem itemOpen = new IcyMenuItem("Open...", GoogleMaterialDesignIcons.FOLDER_OPEN);
        itemOpen.addActionListener(FileActions.openSequenceAction);
        add(itemOpen);

        // TODO Make recent files menu
        final IcyMenu menuOpenRecent = new IcyMenu("Open Recent", GoogleMaterialDesignIcons.FOLDER);
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
                final IcyMenuItem itemFile = new IcyMenuItem(entry);
                final String[] paths = recentFileList.getEntry(i);
                itemFile.addActionListener(e -> Loader.load(CollectionUtil.asList(paths), false, true, true));
                menuOpenRecent.add(itemFile);
                if (!menuOpenRecent.isEnabled())
                    menuOpenRecent.setEnabled(true);
            }
        }

        itemCloseSequence = new IcyMenuItem("Close Sequence", GoogleMaterialDesignIcons.CLEAR);
        itemCloseSequence.addActionListener(FileActions.closeCurrentSequenceAction);
        add(itemCloseSequence);

        itemCloseOther = new IcyMenuItem("Close Other Sequences", GoogleMaterialDesignIcons.CLEAR_ALL);
        itemCloseOther.addActionListener(FileActions.closeOthersSequencesAction);
        add(itemCloseOther);

        itemCloseAll = new IcyMenuItem("Close All Sequences", GoogleMaterialDesignIcons.CLEAR_ALL);
        itemCloseAll.addActionListener(FileActions.closeAllSequencesAction);
        add(itemCloseAll);

        addSeparator();

        itemSaveSequence = new IcyMenuItem("Save Sequence", GoogleMaterialDesignIcons.SAVE);
        itemSaveSequence.addActionListener(FileActions.saveSequenceAction);
        add(itemSaveSequence);

        itemSaveSequenceAs = new IcyMenuItem("Save Sequence As...", GoogleMaterialDesignIcons.SAVE);
        itemSaveSequenceAs.addActionListener(FileActions.saveAsSequenceAction);
        add(itemSaveSequenceAs);

        itemSaveMetadata = new IcyMenuItem("Save Metadata", GoogleMaterialDesignIcons.ART_TRACK);
        itemSaveMetadata.addActionListener(FileActions.saveMetaDataAction);
        add(itemSaveMetadata);

        addSeparator();

        // TODO Make import actions
        menuImport = new IcyMenu("Import");
        add(menuImport);

        if (!SystemUtil.isMac()) {
            addSeparator();

            final IcyMenuItem itemPreferences = new IcyMenuItem("Preferences...", GoogleMaterialDesignIcons.SETTINGS);
            itemPreferences.addActionListener(PreferencesActions.preferencesAction);
            add(itemPreferences);

            addSeparator();

            final IcyMenuItem itemQuit = new IcyMenuItem("Quit Icy", GoogleMaterialDesignIcons.POWER_SETTINGS_NEW);
            itemQuit.addActionListener(GeneralActions.exitApplicationAction);
            add(itemQuit);
        }

        reloadFileMenu();
        enableImport(false); // TODO: 19/01/2023 Remove this when reload import finished

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

    // TODO change this action
    public void addRecentFile(final String filename) {

    }

    // TODO change this action
    public void addRecentFile(final List<String> filenames) {

    }

    // TODO change this action
    public void addRecentLoadedFile(final File file) {

    }

    // TODO change this action
    public List<File> getRecentFileList() {
        return new ArrayList<>();
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
        //reloadImportMenu();
    }
}
