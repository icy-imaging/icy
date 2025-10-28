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

package org.bioimageanalysis.icy.gui.menu;

import org.bioimageanalysis.icy.gui.action.FileActions;
import org.bioimageanalysis.icy.gui.action.GeneralActions;
import org.bioimageanalysis.icy.gui.action.PreferencesActions;
import org.bioimageanalysis.icy.io.Loader;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenu;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;
import org.bioimageanalysis.icy.system.preferences.IcyPreferences;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.collection.CollectionUtil;
import org.bioimageanalysis.icy.common.collection.list.RecentFileList;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuFile extends AbstractApplicationMenu {
    private static final @NotNull ApplicationMenuFile instance = new ApplicationMenuFile();

    public static synchronized @NotNull ApplicationMenuFile getInstance() {
        return instance;
    }

    private final @NotNull IcyMenu menuOpenRecent;
    private final @NotNull IcyMenuItem itemRemoveRecentFiles;

    private final RecentFileList recentFileList;

    private ApplicationMenuFile() {
        super("File");

        recentFileList = new RecentFileList(IcyPreferences.applicationRoot().node("loader"));

        final IcyMenu menuCreate = new IcyMenu("New", SVGResource.PICTURE_ADD);
        add(menuCreate);

        final IcyMenuItem itemCreateSequence = new IcyMenuItem(FileActions.newSequenceAction, SVGResource.IMAGE);
        menuCreate.add(itemCreateSequence);

        final IcyMenuItem itemCreateGraySequence = new IcyMenuItem(FileActions.newGraySequenceAction, SVGResource.GRAYSCALE_IMAGE);
        menuCreate.add(itemCreateGraySequence);

        final IcyMenuItem itemCreateRGBSequence = new IcyMenuItem(FileActions.newRGBSequenceAction, SVGResource.RGB_IMAGE);
        menuCreate.add(itemCreateRGBSequence);

        // TODO rework svg icon for ARGB sequence
        final IcyMenuItem itemCreateRGBASequence = new IcyMenuItem(FileActions.newARGBSequenceAction, SVGResource.ARGB_IMAGE);
        menuCreate.add(itemCreateRGBASequence);

        final IcyMenuItem itemOpen = new IcyMenuItem(FileActions.openSequenceAction, SVGResource.FOLDER_OPEN);
        add(itemOpen);

        // TODO Make recent files menu
        menuOpenRecent = new IcyMenu("Open Recent", SVGResource.FOLDER);
        add(menuOpenRecent);

        itemRemoveRecentFiles = new IcyMenuItem(FileActions.clearRecentFilesAction, SVGResource.DELETE);
        itemRemoveRecentFiles.addActionListener(e -> menuOpenRecent.setEnabled(false));

        final IcyMenuItem itemOpenRegion = new IcyMenuItem(FileActions.openSequenceRegionAction, SVGResource.PICTURE_IN_PICTURE);
        add(itemOpenRegion);

        final IcyMenuItem itemCloseSequence = new IcyMenuItem(FileActions.closeCurrentSequenceAction, SVGResource.CLOSE);
        add(itemCloseSequence);

        final IcyMenuItem itemCloseOther = new IcyMenuItem(FileActions.closeOthersSequencesAction, SVGResource.CLEAR_ALL);
        add(itemCloseOther);

        final IcyMenuItem itemCloseAll = new IcyMenuItem(FileActions.closeAllSequencesAction, SVGResource.CLEAR_ALL);
        add(itemCloseAll);

        addSeparator();

        final IcyMenuItem itemSaveSequence = new IcyMenuItem(FileActions.saveSequenceAction, SVGResource.SAVE);
        add(itemSaveSequence);

        final IcyMenuItem itemSaveSequenceAs = new IcyMenuItem(FileActions.saveAsSequenceAction, SVGResource.SAVE_AS);
        add(itemSaveSequenceAs);

        final IcyMenuItem itemSaveMetadata = new IcyMenuItem(FileActions.saveMetaDataAction, SVGResource.PICTURE_METADATA);
        add(itemSaveMetadata);

        //if (!SystemUtil.isMac()) {
            addSeparator();

            final IcyMenuItem itemPreferences = new IcyMenuItem(PreferencesActions.preferencesAction, SVGResource.SETTINGS);
            add(itemPreferences);

            addSeparator();

            final IcyMenuItem itemQuit = new IcyMenuItem(GeneralActions.exitApplicationAction, SVGResource.POWER_SETTINGS_NEW);
            add(itemQuit);
        //}

        reloadRecentFiles();
    }

    private void reloadRecentFiles() {
        ThreadUtil.invokeLater(() -> {
            menuOpenRecent.removeAll();
            menuOpenRecent.add(itemRemoveRecentFiles);
            menuOpenRecent.addSeparator();

            // remove obsolete entries
            recentFileList.clean();
            final int nbRecentFiles = recentFileList.getSize();
            if (nbRecentFiles > 0) {
                int valid = 0;
                for (int i = 0; i < nbRecentFiles; i++) {
                    final String entry = recentFileList.getEntryAsName(i, 100, true);
                    if (!StringUtil.isEmpty(entry)) {
                        final IcyMenuItem itemFile = new IcyMenuItem(entry, SVGResource.IMAGE);
                        final String[] paths = recentFileList.getEntry(i);
                        itemFile.addActionListener(e -> Loader.load(CollectionUtil.asList(paths), false, true, true));
                        menuOpenRecent.add(itemFile);
                        valid++;
                    }
                }

                menuOpenRecent.setEnabled(valid > 0);
            }
            else
                menuOpenRecent.setEnabled(false);
        });
    }

    public RecentFileList getRecentFileList() {
        return recentFileList;
    }

    public void addRecentLoadedFile(final List<File> files) {
        addRecentLoadedFile(files.toArray(new File[0]));
    }

    public void addRecentLoadedFile(final File[] files) {
        recentFileList.addEntry(files);
    }

    public void addRecentLoadedFile(final File file) {
        addRecentLoadedFile(new File[]{file});
    }

    /**
     * Add a list of recently opened files (String format)
     */
    public void addRecentFile(final List<String> paths) {
        addRecentFile(paths.toArray(new String[0]));
    }

    public void addRecentFile(final String[] paths) {
        recentFileList.addEntry(paths);
    }

    public void addRecentFile(final String path) {
        addRecentFile(new String[]{path});
    }

    @Override
    public void sequenceOpened(final Sequence sequence) {
        reloadRecentFiles();
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
        reloadRecentFiles();
    }
}
