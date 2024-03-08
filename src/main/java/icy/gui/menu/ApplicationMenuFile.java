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

package icy.gui.menu;

import icy.action.FileActions;
import icy.action.GeneralActions;
import icy.action.PreferencesActions;
import icy.file.Loader;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
import icy.preferences.IcyPreferences;
import icy.resource.icon.SVGIcon;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.type.collection.list.RecentFileList;
import icy.util.StringUtil;
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

        final IcyMenu menuCreate = new IcyMenu("New", SVGIcon.ADD_PHOTO_ALTERNATE);
        add(menuCreate);

        final IcyMenuItem itemCreateSequence = new IcyMenuItem(FileActions.newSequenceAction, SVGIcon.IMAGE);
        menuCreate.add(itemCreateSequence);

        final IcyMenuItem itemCreateGraySequence = new IcyMenuItem(FileActions.newGraySequenceAction, SVGIcon.GRAYSCALE_IMAGE);
        menuCreate.add(itemCreateGraySequence);

        final IcyMenuItem itemCreateRGBSequence = new IcyMenuItem(FileActions.newRGBSequenceAction, SVGIcon.RGB_IMAGE);
        menuCreate.add(itemCreateRGBSequence);

        // TODO rework svg icon for ARGB sequence
        final IcyMenuItem itemCreateRGBASequence = new IcyMenuItem(FileActions.newARGBSequenceAction, SVGIcon.ARGB_IMAGE);
        menuCreate.add(itemCreateRGBASequence);

        final IcyMenuItem itemOpen = new IcyMenuItem(FileActions.openSequenceAction, SVGIcon.FOLDER_OPEN);
        add(itemOpen);

        // TODO Make recent files menu
        menuOpenRecent = new IcyMenu("Open Recent", SVGIcon.FOLDER);
        add(menuOpenRecent);

        itemRemoveRecentFiles = new IcyMenuItem(FileActions.clearRecentFilesAction, SVGIcon.DELETE);
        itemRemoveRecentFiles.addActionListener(e -> menuOpenRecent.setEnabled(false));

        final IcyMenuItem itemOpenRegion = new IcyMenuItem(FileActions.openSequenceRegionAction, SVGIcon.PICTURE_IN_PICTURE);
        add(itemOpenRegion);

        final IcyMenuItem itemCloseSequence = new IcyMenuItem(FileActions.closeCurrentSequenceAction, SVGIcon.CLOSE);
        add(itemCloseSequence);

        final IcyMenuItem itemCloseOther = new IcyMenuItem(FileActions.closeOthersSequencesAction, SVGIcon.CLEAR_ALL);
        add(itemCloseOther);

        final IcyMenuItem itemCloseAll = new IcyMenuItem(FileActions.closeAllSequencesAction, SVGIcon.CLEAR_ALL);
        add(itemCloseAll);

        addSeparator();

        final IcyMenuItem itemSaveSequence = new IcyMenuItem(FileActions.saveSequenceAction, SVGIcon.SAVE);
        add(itemSaveSequence);

        final IcyMenuItem itemSaveSequenceAs = new IcyMenuItem(FileActions.saveAsSequenceAction, SVGIcon.SAVE_AS);
        add(itemSaveSequenceAs);

        final IcyMenuItem itemSaveMetadata = new IcyMenuItem(FileActions.saveMetaDataAction, SVGIcon.ART_TRACK);
        add(itemSaveMetadata);

        if (!SystemUtil.isMac()) {
            addSeparator();

            final IcyMenuItem itemPreferences = new IcyMenuItem(PreferencesActions.preferencesAction, SVGIcon.SETTINGS);
            add(itemPreferences);

            addSeparator();

            final IcyMenuItem itemQuit = new IcyMenuItem(GeneralActions.exitApplicationAction, SVGIcon.POWER_SETTINGS_NEW);
            add(itemQuit);
        }

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
                        final IcyMenuItem itemFile = new IcyMenuItem(entry, SVGIcon.IMAGE);
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
