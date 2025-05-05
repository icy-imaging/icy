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

package org.bioimageanalysis.icy.network;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor.PluginIdent;
import org.bioimageanalysis.icy.extension.plugin.PluginLoader;
import org.bioimageanalysis.icy.system.preferences.ApplicationPreferences;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class WebInterface {
    public static final String BASE_URL = NetworkUtil.WEBSITE_URL + "interface/";

    public static final String PARAM_ACTION = "action";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String PARAM_ID = "id";
    public static final String PARAM_CLASSNAME = "classname";
    public static final String PARAM_FIELD = "field";

    // search specific parameter(s)
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_POSTTYPE = "pt";

    // bug report specific parameter(s)
    public static final String PARAM_KERNELVERSION = "kernelVersion";
    public static final String PARAM_JAVANAME = "javaName";
    public static final String PARAM_JAVAVERSION = "javaVersion";
    public static final String PARAM_JAVABITS = "javaBits";
    public static final String PARAM_OSNAME = "osName";
    public static final String PARAM_OSVERSION = "osVersion";
    public static final String PARAM_OSARCH = "osArch";
    public static final String PARAM_PLUGINCLASSNAME = "pluginClassName";
    public static final String PARAM_PLUGINVERSION = "pluginVersion";
    public static final String PARAM_DEVELOPERID = "developerId";
    public static final String PARAM_ERRORLOG = "errorLog";

    // action types
    public static final String ACTION_TYPE_SEARCH = "search";
    public static final String ACTION_TYPE_BUGREPORT = "bugReport";
    // search types
    public static final String SEARCH_TYPE_PLUGIN = "plugin";
    public static final String SEARCH_TYPE_SCRIPT = "script";
    public static final String SEARCH_TYPE_PROTOCOL = "protocol";

    /**
     * Process search on the website in a specific resource and return result in a XML Document.
     *
     * @param text
     *        Text used for the search request, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <ul>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li><i>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     *        </ul>
     * @param type
     *        type of resource we want to search in.<br>
     *        Accepted values are:<br>
     *        <ul>
     *        <li>SEARCH_TYPE_PLUGIN</li>
     *        <li>SEARCH_TYPE_SCRIPT</li>
     *        <li>SEARCH_TYPE_PROTOCOL</li>
     *        <li>null (all resources)</li>
     *        </ul>
     * @return result in XML Document format
     */
    public static Document doSearch(final String text, final String type) {
        // build request (encode search text in UTF8)
        String request = BASE_URL + "?" + PARAM_ACTION + "=" + ACTION_TYPE_SEARCH + "&" + PARAM_SEARCH + "=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

        // specific type ?
        if (!StringUtil.isEmpty(type))
            request += "&" + PARAM_POSTTYPE + "=" + type;

        // add client id
        // request += "&" + PARAM_CLIENT_ID + "=2532495";
        request += "&" + PARAM_CLIENT_ID + "=" + ApplicationPreferences.getId();

        // send request to web site and get result
        return XMLUtil.loadDocument(URLUtil.getURL(request), true);
    }

    /**
     * Process search on the website in all resources and return result in a XML Document.
     *
     * @param text
     *        Text used for the search request, it can contains several words and use operators.<br>
     *        Examples:<br>
     *        <ul>
     *        <li><i>spot detector</i> : any of word should be present</li>
     *        <li><i>+spot +detector</i> : both words should be present</li>
     *        <li><i>"spot detector"</i> : the exact expression should be present</li>
     *        <li><i>+"spot detector" -tracking</i> : <i>spot detector</i> should be present and <i>tracking</i> absent</li>
     *        </ul>
     * @return result in XML Document format
     */
    public static Document doSearch(final String text) {
        return doSearch(text, null);
    }

    /**
     * Report an error log from a given plugin or developer id to Icy web site.
     *
     * @param plugin
     *        The plugin responsible of the error or <code>null</code> if the error comes from the
     *        application or if we are not able to get the plugin descriptor.
     * @param devId
     *        The developer id of the plugin responsible of the error when the plugin descriptor was
     *        not found or <code>null</code> if the error comes from the application.
     * @param errorLog
     *        Error log to report.
     */
    public static void reportError(final PluginDescriptor plugin, final String devId, final String errorLog) {
        final String icyId;
        final String javaId;
        final String osId;
        final String memory;
        String pluginId;
        final StringBuilder pluginDepsId;
        final Map<String, String> values = new HashMap<>();

        // bug report action
        values.put(PARAM_ACTION, ACTION_TYPE_BUGREPORT);

        // add informations about java / system / OS
        values.put(PARAM_KERNELVERSION, Icy.VERSION.toString());
        values.put(PARAM_JAVANAME, SystemUtil.getJavaName());
        values.put(PARAM_JAVAVERSION, SystemUtil.getJavaVersion());
        values.put(PARAM_JAVABITS, Integer.toString(SystemUtil.getJavaArchDataModel()));
        values.put(PARAM_OSNAME, SystemUtil.getOSName());
        values.put(PARAM_OSVERSION, SystemUtil.getOSVersion());
        values.put(PARAM_OSARCH, SystemUtil.getOSArch());

        icyId = "Icy Version " + Icy.VERSION + "<br>";
        javaId = SystemUtil.getJavaName() + " " + SystemUtil.getJavaVersion() + " (" + SystemUtil.getJavaArchDataModel()
                + " bit)<br>";
        osId = "Running on " + SystemUtil.getOSName() + " " + SystemUtil.getOSVersion() + " (" + SystemUtil.getOSArch()
                + ")<br>";
        memory = "Max java memory : " + UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory()) + "<br>";

        if (plugin != null) {
            // default
            pluginId = "Plugin " + plugin;

            pluginId += "<br><br>";

            final String className = plugin.getClassName();

            // add plugin informations if available
            values.put(PARAM_PLUGINCLASSNAME, className);
            values.put(PARAM_PLUGINVERSION, plugin.getVersion().toString());

            if (plugin.getRequired().size() > 0) {
                pluginDepsId = new StringBuilder("Dependances:<br>");
                for (final PluginIdent ident : plugin.getRequired()) {
                    final PluginDescriptor installed = PluginLoader.getPlugin(ident.getClassName());

                    if (installed == null)
                        pluginDepsId.append("Class ").append(ident.getClassName()).append(" not found !<br>");
                    else
                        pluginDepsId.append("Plugin ").append(installed).append(" is correctly installed<br>");
                }
                pluginDepsId.append("<br>");
            }
            else
                pluginDepsId = new StringBuilder();
        }
        else {
            // no plugin information available
            values.put(PARAM_PLUGINCLASSNAME, "");
            values.put(PARAM_PLUGINVERSION, "");
            pluginId = "";
            pluginDepsId = new StringBuilder();
        }

        // add dev id
        if (!StringUtil.isEmpty(devId))
            values.put(PARAM_DEVELOPERID, devId);
        else if (plugin != null)
            // package author package name is developer id
            values.put(PARAM_DEVELOPERID, plugin.getAuthorPackageName());
        else
            // empty
            values.put(PARAM_DEVELOPERID, "");

        // add client id
        // values.put(PARAM_CLIENT_ID, "2532495");
        values.put(PARAM_CLIENT_ID, Integer.toString(ApplicationPreferences.getId()));

        // and finally the error log itself
        values.put(PARAM_ERRORLOG,
                icyId + javaId + osId + memory + "<br>" + pluginId + pluginDepsId + errorLog.replaceAll("\n", "<br>"));

        // send report in background task (we don't want to wait for response from server)
        ThreadUtil.bgRun(() -> {
            try {
                final String result = NetworkUtil.postData(BASE_URL, values);

                if (result == null)
                    IcyLogger.warn(WebInterface.class, "Error while reporting data, verifying your internet connection.");
            }
            catch (final IOException e) {
                IcyLogger.error(WebInterface.class, e, "Error while reporting data.");
            }
        });
    }
}
