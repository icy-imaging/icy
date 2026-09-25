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

package fr.icy.system.audit;

import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.extension.plugin.abstract_.Plugin;
import fr.icy.io.FileUtil;
import fr.icy.io.xml.XMLPersistent;
import fr.icy.io.xml.XMLPersistentHelper;
import fr.icy.io.xml.XMLUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class AuditStorage implements XMLPersistent {
    private static final Logger LOGGER = Logger.getLogger(AuditStorage.class.getName());

    private static final String ID_PLUGIN = "plugin";

    private static final String AUDIT_FILENAME = "icy_usage.xml";

    private static final long SAVE_INTERVAL = 1000 * 60;

    //private final Map<PluginIdent, PluginStorage> pluginStats;
    private long lastSaveTime;

    public AuditStorage() {
        super();

        //pluginStats = new HashMap<>();

        try {
            // load usage data from XML file
            XMLPersistentHelper.loadFromXML(this, FileUtil.getTempDirectory() + FileUtil.separator + AUDIT_FILENAME);
            // clean obsoletes data
            clean();
        }
        catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Can't load usage statistics data.", e);
        }

        lastSaveTime = System.currentTimeMillis();
    }

    public void save() {
        try {
            // save XML data
            XMLPersistentHelper.saveToXML(this, FileUtil.getTempDirectory() + FileUtil.separator + AUDIT_FILENAME);
        }
        catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Can't save usage statistics data.", e);
        }
    }

    private void clean() {
        /*final List<PluginIdent> empties = new ArrayList<>();

        synchronized (pluginStats) {
            // clean statistics
            for (final Entry<PluginIdent, PluginStorage> entry : pluginStats.entrySet()) {
                entry.getValue().clean();
                if (entry.getValue().isEmpty())
                    empties.add(entry.getKey());
            }

            // remove empty ones
            for (final PluginIdent ident : empties)
                pluginStats.remove(ident);
        }*/
    }

    private void autoSave() {
        final long currentTime = System.currentTimeMillis();

        // interval elapsed
        if ((currentTime - lastSaveTime) > SAVE_INTERVAL) {
            // save statistics to disk
            save();
            lastSaveTime = currentTime;
        }
    }

    /*private PluginStorage getStorage(final PluginIdent ident) {
        PluginStorage result;

        synchronized (pluginStats) {
            result = pluginStats.get(ident);

            if (result == null) {
                result = new PluginStorage();
                pluginStats.put(ident, result);
            }
        }

        return result;
    }*/

    public void pluginLaunched(final Plugin plugin) {
        PluginDescriptor descriptor = null;

        try {
            descriptor = plugin.getDescriptor();
        }
        catch (final Throwable t) {
            // ignore possible ClassNotFound error here…
            LOGGER.log(Level.WARNING, "Can't get plugin descriptor.", t);
        }

        // ignore if no descriptor
        if (descriptor == null)
            return;
        // ignore if missing version info
        if (descriptor.getVersion().isEmpty())
            return;
        // ignore kernel plugins
        if (descriptor.isKernelPlugin())
            return;

        // increment launch
        //getStorage(descriptor.getIdent()).incLaunch(DateUtil.keepDay(System.currentTimeMillis()));

        // save to disk if needed
        autoSave();
    }

    public void pluginInstanced(final Plugin plugin) {
        PluginDescriptor descriptor = null;

        try {
            descriptor = plugin.getDescriptor();
        }
        catch (final Throwable t) {
            // ignore possible ClassNotFound error here…
            LOGGER.log(Level.WARNING, "Can't get plugin descriptor.", t);
        }

        // ignore if no descriptor
        if (descriptor == null)
            return;
        // ignore if missing version info
        if (descriptor.getVersion().isEmpty())
            return;
        // ignore kernel plugins
        if (descriptor.isKernelPlugin())
            return;

        // increment instance
        //getStorage(descriptor.getIdent()).incInstance(DateUtil.keepDay(System.currentTimeMillis()));

        // save to disk if needed
        autoSave();
    }

    /**
     * Upload statistics to the website
     */
    public boolean upload(final int id) {
        /*final List<PluginIdent> dones = new ArrayList<>();
        final List<Entry<PluginIdent, PluginStorage>> entries;

        synchronized (pluginStats) {
            entries = new ArrayList<>(pluginStats.entrySet());
        }

        try {
            for (final Entry<PluginIdent, PluginStorage> entry : entries) {
                if (entry.getValue().upload(id, entry.getKey()))
                    dones.add(entry.getKey());

                // interrupt upload
                if (Thread.interrupted())
                    break;
            }
        }
        finally {
            // remove stats that have been correctly uploaded
            synchronized (pluginStats) {
                for (final PluginIdent ident : dones)
                    pluginStats.remove(ident);
            }
        }

        return pluginStats.isEmpty();*/
        return true;
    }

    @Override
    public boolean loadFromXML(final Node node) {
        if (node == null)
            return false;

        /*synchronized (pluginStats) {
            pluginStats.clear();
            for (final Node n : XMLUtil.getChildren(node, ID_PLUGIN)) {
                final PluginIdent ident = new PluginIdent();
                final PluginStorage storage = new PluginStorage();

                ident.loadFromXMLShort(n);
                storage.loadFromXML(n);

                pluginStats.put(ident, storage);
            }
        }*/

        return true;
    }

    @Override
    public boolean saveToXML(final Node node) {
        if (node == null)
            return false;

        XMLUtil.removeAllChildren(node);

        /*synchronized (pluginStats) {
            for (final Entry<PluginIdent, PluginStorage> entry : pluginStats.entrySet()) {
                final Node n = XMLUtil.addElement(node, ID_PLUGIN);

                entry.getKey().saveToXMLShort(n);
                entry.getValue().saveToXML(n);
            }
        }*/

        return true;
    }

    private static class PluginStorage implements XMLPersistent {
        //private static final String ID_CLASSNAME = PluginIdent.ID_CLASSNAME;
        //private static final String ID_VERSION = PluginIdent.ID_VERSION;

        private static final String ID_LAUNCH = "launch";
        private static final String ID_INSTANCE = "instance";

        private static final String ID_STATS_LAUNCH = "stats_" + ID_LAUNCH;
        private static final String ID_STATS_INSTANCE = "stats_" + ID_INSTANCE;
        private static final String ID_DATE = "date";
        private static final String ID_VALUE = "value";

        private static final long DAY_TO_KEEP = 30L;

        private final Map<Long, Long> launchStats;
        private final Map<Long, Long> instanceStats;

        public PluginStorage() {
            super();

            launchStats = new HashMap<>();
            instanceStats = new HashMap<>();
        }

        public void clean() {
            final List<Long> olds = new ArrayList<>();
            final long dayInterval = 1000 * 60 * 60 * 24;
            final long timeLimit = System.currentTimeMillis() - (DAY_TO_KEEP * dayInterval);

            // find obsoletes entries
            //olds.clear();
            for (final Long date : launchStats.keySet())
                if (date < timeLimit)
                    olds.add(date);

            // remove them
            for (final Long date : olds)
                launchStats.remove(date);

            // find obsoletes entries
            olds.clear();
            for (final Long date : instanceStats.keySet())
                if (date < timeLimit)
                    olds.add(date);

            // remove them
            for (final Long date : olds)
                instanceStats.remove(date);
        }

        public boolean isEmpty() {
            return launchStats.isEmpty() && instanceStats.isEmpty();
        }

        public long getLaunch(final Long date) {
            final Long result = launchStats.get(date);

            if (result == null)
                return 0L;

            return result;
        }

        public long getInstance(final Long date) {
            final Long result = instanceStats.get(date);

            if (result == null)
                return 0L;

            return result;
        }

        public void incLaunch(final long date) {
            final Long key = date;
            launchStats.put(key, getLaunch(key) + 1L);
        }

        public void incInstance(final long date) {
            final Long key = date;
            instanceStats.put(key, getInstance(key) + 1L);
        }

        private @NonNull Map<String, String> getIdParam(final int id) {
            // is ID ok?
            if (id != -1) {
                final Map<String, String> values = new HashMap<>();

                // set ID
                values.put(Audit.ID_ICY_ID, Integer.toString(id));

                return values;
            }

            //return null;
            return new HashMap<>();
        }

        /*public boolean upload(final int id, final PluginIdent ident) {
            // init params
            final Map<String, String> params = getIdParam(id);
            int offset;

            // set plugin identity
            params.put(ID_CLASSNAME, ident.getClassName());
            params.put(ID_VERSION, ident.getVersion().toString());

            offset = 0;
            // build params for launch statistic
            for (final Entry<Long, Long> entry : launchStats.entrySet()) {
                // set date
                params.put(ID_STATS_LAUNCH + "[" + offset + "][" + ID_DATE + "]", entry.getKey().toString());
                // set value
                params.put(ID_STATS_LAUNCH + "[" + offset + "][" + ID_VALUE + "]", entry.getValue().toString());
                offset++;
            }

            offset = 0;
            // build params for instance statistic
            for (final Entry<Long, Long> entry : instanceStats.entrySet()) {
                // set date
                params.put(ID_STATS_INSTANCE + "[" + offset + "][" + ID_DATE + "]", entry.getKey().toString());
                // set value
                params.put(ID_STATS_INSTANCE + "[" + offset + "][" + ID_VALUE + "]", entry.getValue().toString());
                offset++;
            }

            try {
                // null return means the website did not accept them…
                if (NetworkUtil.postData(Audit.URL_AUDIT_PLUGIN, params) == null)
                    return false;
            }
            catch (final IOException e) {
                return false;
            }

            // clear stats just to be sure to not send them twice
            launchStats.clear();
            instanceStats.clear();

            return true;
        }*/

        @Contract("null -> false")
        @Override
        public boolean loadFromXML(final Node node) {
            if (node == null)
                return false;

            launchStats.clear();
            for (final Node n : XMLUtil.getChildren(node, ID_LAUNCH)) {
                final long date = XMLUtil.getElementLongValue(n, ID_DATE, 0L);
                final long value = XMLUtil.getElementLongValue(n, ID_VALUE, 0L);

                launchStats.put(date, value);
            }

            instanceStats.clear();
            for (final Node n : XMLUtil.getChildren(node, ID_INSTANCE)) {
                final long date = XMLUtil.getElementLongValue(n, ID_DATE, 0L);
                final long value = XMLUtil.getElementLongValue(n, ID_VALUE, 0L);

                instanceStats.put(date, value);
            }

            return true;
        }

        @Contract("null -> false")
        @Override
        public boolean saveToXML(final Node node) {
            if (node == null)
                return false;

            for (final Entry<Long, Long> entry : launchStats.entrySet()) {
                final Node n = XMLUtil.addElement(node, ID_LAUNCH);

                XMLUtil.setElementLongValue(n, ID_DATE, entry.getKey());
                XMLUtil.setElementLongValue(n, ID_VALUE, entry.getValue());
            }

            for (final Entry<Long, Long> entry : instanceStats.entrySet()) {
                final Node n = XMLUtil.addElement(node, ID_INSTANCE);

                XMLUtil.setElementLongValue(n, ID_DATE, entry.getKey());
                XMLUtil.setElementLongValue(n, ID_VALUE, entry.getValue());
            }

            return true;
        }
    }
}
