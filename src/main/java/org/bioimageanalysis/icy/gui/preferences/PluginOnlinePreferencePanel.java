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

package org.bioimageanalysis.icy.gui.preferences;

import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginInstaller;
import org.bioimageanalysis.icy.extension.plugin.PluginInstaller.PluginInstallerListener;
import org.bioimageanalysis.icy.extension.plugin.PluginRepositoryLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginRepositoryLoader.PluginRepositoryLoaderListener;
import org.bioimageanalysis.icy.gui.dialog.ConfirmDialog;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences.RepositoryInfo;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PluginOnlinePreferencePanel extends PluginListPreferencePanel implements PluginRepositoryLoaderListener, PluginInstallerListener {
    private enum PluginOnlineState {
        NULL, INSTALLING, REMOVING, HAS_INSTALL, INSTALLED, INSTALLED_FAULTY, OLDER, NEWER
    }

    public static final String NODE_NAME = "Online Plugin";

    PluginOnlinePreferencePanel(final PreferenceFrame parent) {
        super(parent, NODE_NAME, PluginPreferencePanel.NODE_NAME);

        PluginRepositoryLoader.addListener(this);
        PluginInstaller.addListener(this);

        // remove last column not used here
        table.removeColumn(table.getColumn(columnIds[4]));

        repositoryPanel.setVisible(true);
        action1Button.setText("Delete");
        action1Button.setVisible(true);
        action2Button.setText("Install");
        action2Button.setVisible(true);

        updateButtonsState();
        updateRepositories();
    }

    @Override
    protected void closed() {
        super.closed();

        PluginRepositoryLoader.removeListener(this);
        PluginInstaller.removeListener(this);
    }

    private PluginOnlineState getPluginOnlineState(final PluginDescriptor plugin) {
        if (plugin == null)
            return PluginOnlineState.NULL;

        if ((PluginInstaller.isInstallingPlugin(plugin)))
            return PluginOnlineState.INSTALLING;
        if ((PluginInstaller.isDesinstallingPlugin(plugin)))
            return PluginOnlineState.REMOVING;

        // has a local version ?
        if (plugin.isInstalled()) {
            // get local version
            final PluginDescriptor localPlugin = ExtensionLoader.getPlugin(plugin.getClassName());

            if (localPlugin != null) {
                if (plugin.equals(localPlugin))
                    return PluginOnlineState.INSTALLED;
                if (plugin.isLower(localPlugin))
                    return PluginOnlineState.OLDER;
                if (plugin.isGreater(localPlugin))
                    return PluginOnlineState.NEWER;
            }

            // local version not loaded --> faulty
            return PluginOnlineState.INSTALLED_FAULTY;
        }

        return PluginOnlineState.HAS_INSTALL;
    }

    @Override
    protected void doAction1() {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final List<PluginDescriptor> toRemove = new ArrayList<>();

        for (final PluginDescriptor plugin : selectedPlugins) {
            final PluginOnlineState state = getPluginOnlineState(plugin);

            // remove plugin
            if ((state == PluginOnlineState.INSTALLED) || (state == PluginOnlineState.INSTALLED_FAULTY))
                toRemove.add(plugin);
        }

        // nothing to remove
        if (toRemove.isEmpty())
            return;

        // get dependants plugins
        final List<PluginDescriptor> dependants = PluginInstaller.getLocalDependenciesFrom(toRemove);
        // delete the one we plan to remove
        dependants.removeAll(toRemove);

        final StringBuilder message = new StringBuilder("<html>");

        if (!dependants.isEmpty()) {
            message.append("The following plugin(s) won't work anymore :<br>");

            for (final PluginDescriptor depPlug : dependants)
                message.append(depPlug.getName()).append(" ").append(depPlug.getVersion()).append("<br>");

            message.append("<br>");
        }

        message.append("Are you sure you want to remove selected plugin(s) ?</html>");

        if (ConfirmDialog.confirm(message.toString())) {
            // remove plugins
            for (final PluginDescriptor plugin : toRemove)
                PluginInstaller.desinstall(plugin, false, true);
        }

        // refresh state
        refreshTableData();
    }

    @Override
    protected void doAction2() {
        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();

        for (final PluginDescriptor plugin : selectedPlugins) {
            final PluginOnlineState state = getPluginOnlineState(plugin);

            if ((state == PluginOnlineState.HAS_INSTALL) || (state == PluginOnlineState.NEWER)
                    || (state == PluginOnlineState.OLDER)) {
                final boolean doInstall;

                if (state == PluginOnlineState.OLDER)
                    doInstall = ConfirmDialog.confirm("You'll replace your plugin by an older version !\nAre you sure you want to continue ?");
                else
                    doInstall = true;

                // install plugin
                if (doInstall)
                    PluginInstaller.install(plugin, true);
            }
        }

        // refresh state
        refreshTableData();
    }

    @Override
    protected void repositoryChanged() {
        refreshPlugins();
    }

    @Override
    protected void reloadPlugins() {
        PluginRepositoryLoader.reload();
        // so we display the empty list during reload
        pluginsChanged();
    }

    @Override
    protected String getStateValue(final PluginDescriptor plugin) {
        return switch (getPluginOnlineState(plugin)) {
            case INSTALLING -> "installing...";
            case REMOVING -> "removing...";
            case NEWER -> "update available";
            case OLDER -> "outdated";
            case INSTALLED -> "installed";
            case INSTALLED_FAULTY -> "faulty";
            default -> "";
        };
    }

    @Override
    protected List<PluginDescriptor> getPlugins() {
        // loading...
        if (!PluginRepositoryLoader.isLoaded())
            return new ArrayList<>();

        // get selected repository
        final Object selectedItem = repository.getSelectedItem();

        // load plugins from repository
        if (selectedItem != null)
            return PluginRepositoryLoader.getPlugins((RepositoryInfo) selectedItem);

        return PluginRepositoryLoader.getPlugins();
    }

    @Override
    protected void updateButtonsStateInternal() {
        super.updateButtonsStateInternal();

        final List<PluginDescriptor> selectedPlugins = getSelectedPlugins();
        final boolean selected = (selectedPlugins.size() > 0);

        if (PluginRepositoryLoader.isLoaded()) {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
            repository.setEnabled(true);
        }
        else {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
            repository.setEnabled(false);
        }

        if (!selected) {
            action1Button.setEnabled(false);
            action2Button.setEnabled(false);
            return;
        }

        PluginOnlineState state;

        state = PluginOnlineState.NULL;
        for (final PluginDescriptor plugin : selectedPlugins) {
            switch (getPluginOnlineState(plugin)) {
                case REMOVING:
                    if (state == PluginOnlineState.NULL)
                        state = PluginOnlineState.REMOVING;
                    break;

                case INSTALLED:
                case INSTALLED_FAULTY:
                    if ((state == PluginOnlineState.NULL) || (state == PluginOnlineState.REMOVING))
                        state = PluginOnlineState.INSTALLED;
                    break;
            }
        }

        // some online plugins are already installed ?
        switch (state) {
            case REMOVING:
                // special case where plugins are currently begin removed
                action1Button.setText("Deleting...");
                action1Button.setEnabled(false);
                break;

            case INSTALLED:
                action1Button.setText("Delete");
                action1Button.setEnabled(true);
                break;

            case NULL:
                action1Button.setText("Delete");
                action1Button.setEnabled(false);
                break;
        }

        state = PluginOnlineState.NULL;
        for (final PluginDescriptor plugin : selectedPlugins) {
            switch (getPluginOnlineState(plugin)) {
                case INSTALLING:
                    if (state == PluginOnlineState.NULL)
                        state = PluginOnlineState.INSTALLING;
                    break;

                case OLDER:
                    if ((state == PluginOnlineState.NULL) || (state == PluginOnlineState.INSTALLING))
                        state = PluginOnlineState.OLDER;
                    break;

                case NEWER:
                    if ((state == PluginOnlineState.NULL) || (state == PluginOnlineState.INSTALLING)
                            || (state == PluginOnlineState.OLDER))
                        state = PluginOnlineState.NEWER;
                    break;

                case HAS_INSTALL:
                    state = PluginOnlineState.HAS_INSTALL;
                    break;
            }
        }

        switch (state) {
            case INSTALLING:
                action2Button.setText("Installing...");
                action2Button.setEnabled(false);
                break;

            case HAS_INSTALL:
                action2Button.setText("Install");
                action2Button.setEnabled(true);
                break;

            case OLDER:
                action2Button.setText("Revert");
                action2Button.setEnabled(true);
                break;

            case NEWER:
                action2Button.setText("Update");
                action2Button.setEnabled(true);
                break;

            case NULL:
                action2Button.setText("Install");
                action2Button.setEnabled(false);
                break;
        }
    }

    @Override
    public void pluginRepositeryLoaderChanged(final PluginDescriptor plugin) {
        if (plugin != null) {
            final int ind = getPluginModelIndex(plugin.getClassName());

            if (ind != -1) {
                ThreadUtil.invokeNow(() -> {
                    try {
                        tableModel.fireTableRowsUpdated(ind, ind);
                    }
                    catch (final Exception e) {
                        // ignore possible exception here
                    }
                });
            }
        }
        else pluginsChanged();
    }

    @Override
    public void pluginInstalled(final PluginDescriptor plugin, final boolean success) {
        refreshTableData();
    }

    @Override
    public void pluginRemoved(final PluginDescriptor plugin, final boolean success) {
        refreshTableData();
    }
}
