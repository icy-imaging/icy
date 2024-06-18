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
import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.common.listener.ProgressListener;
import org.bioimageanalysis.icy.common.listener.weak.WeakListener;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.network.auth.AuthenticationInfo;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.audit.Audit;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.NetworkPreferences;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import javax.net.ssl.*;
import java.awt.*;
import java.awt.Desktop.Action;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class NetworkUtil {
    /**
     * URL
     */
    public static final String WEBSITE_HOST = "icy.bioimageanalysis.org";
    public static final String WEBSITE_URL = "https://" + WEBSITE_HOST + "/";

    public static final String IMAGE_SC_URL = "https://forum.image.sc";
    public static final String IMAGE_SC_ICY_URL = IMAGE_SC_URL + "/tag/icy";

    /**
     * Parameters id
     */
    public static final String ID_KERNELVERSION = "kernelVersion";
    public static final String ID_BETAALLOWED = "betaAllowed";
    public static final String ID_JAVANAME = "javaName";
    public static final String ID_JAVAVERSION = "javaVersion";
    public static final String ID_JAVABITS = "javaBits";
    public static final String ID_OSNAME = "osName";
    public static final String ID_OSVERSION = "osVersion";
    public static final String ID_OSARCH = "osArch";
    public static final String ID_PLUGINCLASSNAME = "pluginClassName";
    public static final String ID_PLUGINVERSION = "pluginVersion";
    public static final String ID_DEVELOPERID = "developerId";
    public static final String ID_ERRORLOG = "errorLog";

    /**
     * Proxy config ID
     */
    public static final int NO_PROXY = 0;
    public static final int SYSTEM_PROXY = 1;
    public static final int USER_PROXY = 2;

    public interface InternetAccessListener {
        /**
         * Internet connection available.
         */
        void internetUp();

        /**
         * Internet connection no more available.
         */
        void internetDown();
    }

    /**
     * Weak listener wrapper for NetworkConnectionListener.
     *
     * @author Stephane
     */
    public static class WeakInternetAccessListener extends WeakListener<InternetAccessListener> implements InternetAccessListener {
        public WeakInternetAccessListener(final InternetAccessListener listener) {
            super(listener);
        }

        @Override
        public void removeListener(final Object source) {
            removeInternetAccessListener(this);
        }

        @Override
        public void internetUp() {
            final InternetAccessListener listener = getListener();

            if (listener != null)
                listener.internetUp();
        }

        @Override
        public void internetDown() {
            final InternetAccessListener listener = getListener();

            if (listener != null)
                listener.internetDown();
        }
    }

    /**
     * Internet monitor thread
     */
    private static class InternetMonitorThread extends Thread {
        public InternetMonitorThread() {
            super("Internet monitor");
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    final Socket socket = new Socket();

                    // timeout = 3 seconds
                    socket.setSoTimeout(3000);
                    socket.connect(new InetSocketAddress(WEBSITE_HOST, 80), 3000);
                    socket.close();

                    // we have internet access
                    setInternetAccess(true);
                }
                catch (final Throwable t1) {
                    // in case we use proxy
                    try {
                        final URLConnection urlConnection = openConnection("https://www.google.com", true, false);

                        if (urlConnection != null) {
                            urlConnection.setConnectTimeout(3000);
                            urlConnection.setReadTimeout(3000);
                            urlConnection.getInputStream();

                            // we have internet access
                            setInternetAccess(true);
                        }
                        else
                            // we don't have internet access
                            setInternetAccess(false);
                    }
                    catch (final Throwable t2) {
                        // we don't have internet access
                        setInternetAccess(false);
                    }
                }

                // wait a bit depending connection state
                ThreadUtil.sleep(hasInternetAccess() ? 30000 : 5000);
            }
        }
    }

    /**
     * 'Accept all' host name verifier
     */
    private static class FakeHostnameVerifier implements HostnameVerifier {
        public FakeHostnameVerifier() {
            super();
        }

        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            // always return true
            return true;
        }
    }

    /**
     * List of all listeners on network connection changes.
     */
    private final static Set<InternetAccessListener> listeners = new HashSet<>();

    /**
     * Internet monitor
     */
    private static final InternetMonitorThread internetMonitor = new InternetMonitorThread();

    /**
     * Network module enabled flag. Set at Icy start up
     */
    public static final boolean networkEnabled = !Icy.isNetworkDisabled();
    /**
     * Internet access up flag
     */
    private static boolean internetAccess;
    /**
     * internal HTTPS compatibility for the new web site
     */
    private static boolean httpsSupported;

    public static void init() {
        internetAccess = false;
        httpsSupported = false;

        if (networkEnabled) {
            // check for HTTPS "let's encrypt" certificate compatibility
            final Version javaVersion = SystemUtil.getJavaVersionAsVersion();
            final int javaInt = javaVersion.getMajor();

            if (javaInt == 7)
                httpsSupported = javaVersion.isGreaterOrEqual(new Version(7, 0, 111));
            else if (javaInt == 8)
                httpsSupported = javaVersion.isGreaterOrEqual(new Version(8, 0, 101));
            else
                httpsSupported = (javaInt >= 9);

            updateNetworkSetting();
            // accept all HTTPS connections by default
            installTruster();
        }

        // String addr;
        //
        // // --> connection HTTPS: fails with java 7, ok with java 8 (let's encrypt certificate)
        // addr = "https://icy.yhello.co";
        // // addr = "http://icy.yhello.co/update/update.php?arch=win64&version=1.9.8.2";
        // // addr = "https://icy.yhello.co/update/update.php?arch=win64&version=1.9.8.2";
        // // addr = "https://icy.yhello.co/register/getLinkedUserInfo.php?IcyId=4817172";
        // // addr = "https://randomuser.me/";
        //
        // try
        // {
        // HttpURLConnection uc = (HttpURLConnection) new URL(addr).openConnection();
        // uc.connect();
        // if (uc instanceof HttpsURLConnection)
        // System.out.println(((HttpsURLConnection) uc).getLocalPrincipal());
        // if (uc instanceof HttpURLConnection)
        // System.out.println(((HttpURLConnection) uc).getResponseCode() + " - "
        // + ((HttpURLConnection) uc).getResponseMessage());
        //
        // InputStream inputStream = uc.getInputStream();
        // inputStream.read();
        // uc.disconnect();
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // }

        // start monitor thread
        if (networkEnabled || !Icy.getMainInterface().isHeadLess()) {
            internetMonitor.setPriority(Thread.MIN_PRIORITY);
            internetMonitor.start();
        }
    }

    private static void installTruster() {
        // enable support for TLS v1.X (used by new Icy web site: TLS 1.2 or TLS 1.3)
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        try {
            // install the accept all host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier(new FakeHostnameVerifier());

            // create a trust manager that does not validate certificate chains (Accept all certificates)
            final TrustManager[] trustAllCerts = new TrustManager[]{new javax.net.ssl.X509ExtendedTrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
                    // ignore
                    // System.out.println(certs.length);
                }

                @Override
                public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
                    // ignore
                    // System.out.println(certs.length + " - " + authType);
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket) {
                    // ignore
                    // System.out.println(chain.length + " - " + authType);
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) {
                    // ignore
                    // System.out.println(chain.length + " - " + authType);
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket) {
                    // ignore
                    // System.out.println(chain.length + " - " + authType);
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) {
                    // ignore
                    // System.out.println(chain.length + " - " + authType);
                }
            }};

            // install the all-trusting trust manager
            SSLContext sc;

            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        }
        catch (final Exception e) {
            IcyLogger.error(NetworkUtil.class, e, e.getLocalizedMessage());
        }
    }

    /**
     * Update network setting from the actual preferences
     */
    public static void updateNetworkSetting() {
        if (networkEnabled) {
            HttpURLConnection.setFollowRedirects(false);

            final int proxySetting = NetworkPreferences.getProxySetting();

            if (proxySetting == NO_PROXY) {
                // no proxy
                disableProxySetting();
                disableHTTPProxySetting();
                disableHTTPSProxySetting();
                disableFTPProxySetting();
                disableSOCKSProxySetting();
                disableSystemProxy();
            }
            else if (proxySetting == SYSTEM_PROXY) {
                // system proxy
                disableProxySetting();
                disableHTTPProxySetting();
                disableHTTPSProxySetting();
                disableFTPProxySetting();
                disableSOCKSProxySetting();
                enableSystemProxy();
            }
            else {
                final String user = NetworkPreferences.getProxyUser();
                final String pass = NetworkPreferences.getProxyPassword();
                final boolean auth = NetworkPreferences.getProxyAuthentication() && (!StringUtil.isEmpty(user))
                        && (!StringUtil.isEmpty(pass));
                String host;

                // authentication enabled ?
                if (auth) {
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, pass.toCharArray());
                        }
                    });
                }

                // manual proxy
                disableSystemProxy();

                // HTTP proxy (use it as general proxy)
                host = NetworkPreferences.getProxyHTTPHost();
                if (!StringUtil.isEmpty(host)) {
                    final int port = NetworkPreferences.getProxyHTTPPort();

                    setProxyHost(host);
                    setProxyPort(port);
                    setHTTPProxyHost(host);
                    setHTTPProxyPort(port);
                    if (auth) {
                        setHTTPProxyUser(user);
                        setHTTPProxyPassword(pass);
                    }
                    enableProxySetting();
                    enableHTTPProxySetting();
                }
                else {
                    disableProxySetting();
                    disableHTTPProxySetting();
                }

                // HTTPS proxy
                host = NetworkPreferences.getProxyHTTPSHost();
                if (!StringUtil.isEmpty(host)) {
                    setHTTPSProxyHost(host);
                    setHTTPSProxyPort(NetworkPreferences.getProxyHTTPSPort());
                    if (auth) {
                        setHTTPSProxyUser(user);
                        setHTTPSProxyPassword(pass);
                    }
                    enableHTTPSProxySetting();
                }
                else
                    disableHTTPSProxySetting();

                // FTP proxy
                host = NetworkPreferences.getProxyFTPHost();
                if (!StringUtil.isEmpty(host)) {
                    setFTPProxyHost(host);
                    setFTPProxyPort(NetworkPreferences.getProxyFTPPort());
                    if (auth) {
                        setFTPProxyUser(user);
                        setFTPProxyPassword(pass);
                    }
                    enableFTPProxySetting();
                }
                else
                    disableFTPProxySetting();

                // SOCKS proxy
                host = NetworkPreferences.getProxySOCKSHost();
                if (!StringUtil.isEmpty(host)) {
                    setSOCKSProxyHost(host);
                    setSOCKSProxyPort(NetworkPreferences.getProxySOCKSPort());
                    if (auth) {
                        setSOCKSProxyUser(user);
                        setSOCKSProxyPassword(pass);
                    }
                    enableSOCKSProxySetting();
                }
                else
                    disableSOCKSProxySetting();
            }
        }
    }

    static void setInternetAccess(final boolean value) {
        if (networkEnabled) {
            if (internetAccess != value) {
                internetAccess = value;

                fireInternetConnectionEvent(value);

                // local stuff to do on connection recovery
                if (value) {
                    // process id audit
                    Audit.onConnect();
                }
            }
        }
    }

    private static void fireInternetConnectionEvent(final boolean value) {
        if (value) {
            for (final InternetAccessListener l : listeners)
                l.internetUp();
        }
        else {
            for (final InternetAccessListener l : listeners)
                l.internetDown();
        }
    }

    /**
     * Adds a new listener on internet access change.
     */
    public static void addInternetAccessListener(final InternetAccessListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener on internet access change.
     */
    public static void removeInternetAccessListener(final InternetAccessListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns true if we currently have Internet connection.
     */
    public static boolean hasInternetAccess() {
        return internetAccess;
    }

    /**
     * Returns true if HTTPS is supported for the new web site.
     */
    public static boolean isHTTPSSupported() {
        return httpsSupported;
    }

    /**
     * Open an URL in the default system browser
     */
    public static boolean openBrowser(final String url) {
        return openBrowser(URLUtil.getURL(url));
    }

    /**
     * Open an URL in the default system browser
     */
    public static boolean openBrowser(final URL url) {
        if (url == null)
            return false;

        try {
            return openBrowser(url.toURI());
        }
        catch (final URISyntaxException e) {
            // use other method
            return systemOpenBrowser(url.toString());
        }
    }

    /**
     * Open an URL in the default system browser
     */
    public static boolean openBrowser(final URI uri) {
        if (uri == null)
            return false;

        final Desktop desktop = SystemUtil.getDesktop();

        if ((desktop != null) && desktop.isSupported(Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            }
            catch (final IOException e) {
                // ignore
            }
        }

        // not
        return systemOpenBrowser(uri.toString());
    }

    /**
     * Open an URL in the default system browser (low level method)
     */
    private static boolean systemOpenBrowser(final String url) {
        if (StringUtil.isEmpty(url))
            return false;

        try {
            if (SystemUtil.isMac()) {
                final Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                final Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            }
            else if (SystemUtil.isWindows())
                //Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            else {
                // assume Unix or Linux
                final String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    //if (Runtime.getRuntime().exec("which " + browsers[count]).waitFor() == 0)
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0)
                        browser = browsers[count];
                }
                if (browser == null)
                    throw new Exception("Could not find web browser");

                Runtime.getRuntime().exec(new String[]{browser, url});
            }

            return true;
        }
        catch (final Exception e) {
            IcyLogger.error(NetworkUtil.class, e, "Error while opening system browser.");
            return false;
        }
    }

    /**
     * Download data from specified URL string and return it as an array of byte
     */
    public static byte[] download(final String path, final ProgressListener listener, final boolean displayError) {
        return download(path, null, null, listener, displayError);
    }

    /**
     * Download data from specified URL string and return it as an array of byte
     * Process authentication process if login / pass are not null.
     */
    public static byte[] download(final String path, final String login, final String pass, final ProgressListener listener, final boolean displayError) {
        final File file = new File(FileUtil.getGenericPath(path));

        // path define a file ?
        if (file.exists())
            return download(file, listener, displayError);

        final URL url = URLUtil.getURL(path);

        // error while building URL ?
        if (url == null) {
            if (displayError)
                IcyLogger.error(NetworkUtil.class, "Can't download '" + path + "', incorrect path !");

            return null;
        }

        return download(url, login, pass, listener, displayError);
    }

    /**
     * Download data from specified URL and return it as an array of byte
     */
    public static byte[] download(final URL url, final ProgressListener listener, final boolean displayError) {
        return download(url, null, null, listener, displayError);
    }

    /**
     * Download data from specified URL and return it as an array of byte.<br>
     * Process authentication process if login / pass fields are not null.<br>
     * It returns <code>null</code> if an error occurred.
     */
    public static byte[] download(final URL url, final String login, final String pass, final ProgressListener listener, final boolean displayError) {
        // check if this is a file
        if (URLUtil.isFileURL(url)) {
            try {
                return download(new File(url.toURI()), listener, displayError);
            }
            catch (final URISyntaxException e) {
                if (displayError)
                    IcyLogger.error(NetworkUtil.class, e, "Can't download from '" + url + "', incorrect path !");

                return null;
            }
        }

        // get connection object and connect it
        final URLConnection uc = openConnection(url, login, pass, true, true, displayError);

        // error --> exit
        try (final InputStream ip = getInputStream(uc, displayError)) {
            if (ip == null)
                return null;
            return download(ip, uc.getContentLength(), listener);
        }
        catch (final Exception e) {
            if (displayError) {
                IcyLogger.error(NetworkUtil.class, e, "Error while downloading '" + uc.getURL() + "'.");
            }

            return null;
        }
        // ignore...
    }

    /**
     * Download data from File and return it as an array of byte.<br>
     * It returns <code>null</code> if an error occurred (file not found or not existing, IO
     * error...)
     */
    public static byte[] download(final File f, final ProgressListener listener, final boolean displayError) {
        if (!f.exists()) {
            IcyLogger.error(NetworkUtil.class, "File not found: " + f.getPath());
            return null;
        }

        try {
            return download(new FileInputStream(f), f.length(), listener);
        }
        catch (final Exception e) {
            if (displayError) {
                IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.download('" + f.getPath() + "',...) error.");
            }

            return null;
        }
    }

    /**
     * Download data from specified InputStream and return it as an array of byte.<br>
     * Returns <code>null</code> if load operation was interrupted by user.
     */
    public static byte[] download(final InputStream in, final long len, final ProgressListener listener) throws IOException {
        final int READ_BLOCKSIZE = 64 * 1024;
        final BufferedInputStream bin;

        if (in instanceof BufferedInputStream)
            bin = (BufferedInputStream) in;
        else
            bin = new BufferedInputStream(in);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream((int) ((len > 0) ? len : READ_BLOCKSIZE));
        // read per block of 64 KB
        final byte[] data = new byte[READ_BLOCKSIZE];

        try {
            int off = 0;
            int count = 0;

            while (count >= 0) {
                count = bin.read(data);
                if (count <= 0) {
                    // unexpected length
                    if ((len != -1) && (off != len))
                        throw new EOFException("Unexpected end of file at " + off + " (" + len + " expected)");
                }
                else
                    off += count;

                // copy to dynamic buffer
                if (count > 0)
                    bout.write(data, 0, count);

                if (listener != null) {
                    // download canceled ?
                    if (!listener.notifyProgress(off, len)) {
                        in.close();
                        IcyLogger.warn(NetworkUtil.class, "Interrupted by user.");
                        return null;
                    }
                }
            }
        }
        finally {
            bin.close();
        }

        return bout.toByteArray();
    }

    /**
     * Download data from specified InputStream and return it as an array of byte.<br>
     * It returns <code>null</code> if an error occurred.
     */
    public static byte[] download(final InputStream in) throws IOException {
        return download(in, -1, null);
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     *
     * @param url
     *        url to connect.
     * @param login
     *        login if the connection requires authentication.<br>
     *        Set it to null if no authentication needed.
     * @param pass
     *        login if the connection requires authentication.
     *        Set it to null if no authentication needed.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param doConnect
     *        do the connection before return the {@link URLConnection} object
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(final URL url, final String login, final String pass, final boolean disableCache, final boolean doConnect, final boolean displayError) {
        if (url == null) {
            if (displayError)
                IcyLogger.error(NetworkUtil.class, "NetworkUtil.openConnection(...) error: URL is null !");

            return null;
        }

        URLConnection uc = null;

        try {
            uc = url.openConnection();
            boolean redirect;

            do {
                redirect = false;

                if (disableCache)
                    disableCache(uc);

                // authentication
                if (!StringUtil.isEmpty(login) && !StringUtil.isEmpty(pass))
                    setAuthentication(uc, login, pass);

                if (doConnect) {
                    // try to connect
                    if (!connect(uc, displayError))
                        // error ? --> return null
                        return null;

                    // we test response code for HTTP connection
                    if (uc instanceof HttpURLConnection) {
                        final int respCode = ((HttpURLConnection) uc).getResponseCode();

                        redirect = (respCode == HttpURLConnection.HTTP_MOVED_PERM)
                                || (respCode == HttpURLConnection.HTTP_MOVED_TEMP)
                                || (respCode == HttpURLConnection.HTTP_SEE_OTHER);

                        // redirection ?
                        if (redirect) {
                            // restart connection with new URL
                            String location = uc.getHeaderField("Location");
                            ((HttpURLConnection) uc).disconnect();
                            location = URLDecoder.decode(location, StandardCharsets.UTF_8);
                            // TODO check URL to URI replacement
                            //uc = new URL(location).openConnection();
                            uc = new URI(location).toURL().openConnection();
                        }
                    }
                }
            }
            while (redirect);

            return uc;
        }
        catch (final IOException | URISyntaxException e) {
            if (displayError) {
                // HTTPS not supported while we have a HTTPS connection to icy web site
                if (!isHTTPSSupported() && (uc != null) && uc.getURL().toString().toLowerCase().startsWith("https://icy")) {
                    IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.openConnection('" + uc.getURL() + "') error: HTTPS connection not supported.");
                }
                else {
                    IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.openConnection('" + url + "') error.");
                }
            }

            return null;
        }
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     *
     * @param url
     *        url to connect.
     * @param login
     *        login if the connection requires authentication.<br>
     *        Set it to null if no authentication needed.
     * @param pass
     *        login if the connection requires authentication.
     *        Set it to null if no authentication needed.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(final URL url, final String login, final String pass, final boolean disableCache, final boolean displayError) {
        return openConnection(url, login, pass, disableCache, false, displayError);
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     *
     * @param url
     *        url to connect.
     * @param auth
     *        Authentication informations.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(final URL url, final AuthenticationInfo auth, final boolean disableCache, final boolean displayError) {
        if ((auth != null) && auth.isEnabled())
            return openConnection(url, auth.getLogin(), auth.getPassword(), disableCache, displayError);

        return openConnection(url, null, null, disableCache, displayError);
    }

    /**
     * Returns a new {@link URLConnection} from specified URL (null if an error occurred).
     *
     * @param url
     *        url to connect.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(final URL url, final boolean disableCache, final boolean displayError) {
        return openConnection(url, null, null, disableCache, displayError);
    }

    /**
     * Returns a new {@link URLConnection} from specified path.<br>
     * Returns <code>null</code> if an error occurred.
     *
     * @param path
     *        path to connect.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static URLConnection openConnection(final String path, final boolean disableCache, final boolean displayError) {
        return openConnection(URLUtil.getURL(path), disableCache, displayError);
    }

    /**
     * Connect the specified {@link URLConnection}.<br>
     * Returns false if the connection failed or if response code is not ok.
     *
     * @param uc
     *        URLConnection to connect.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static boolean connect(final URLConnection uc, final boolean displayError) {
        try {
            // final URL prevUrl = uc.getURL();

            // connect
            uc.connect();

            // // we have to test that as sometime url are automatically modified / fixed by host!
            // if (!uc.getURL().toString().toLowerCase().equals(prevUrl.toString().toLowerCase()))
            // {
            // // TODO : do something better
            // System.out.println("Host URL change rejected : " + prevUrl + " --> " + uc.getURL());
            // return false;
            // }

            // we test response code for HTTP connection
            if (uc instanceof final HttpURLConnection huc) {
                // not ok ?
                if (huc.getResponseCode() >= 0x400) {
                    if (displayError) {
                        IcyLogger.error(NetworkUtil.class, "NetworkUtil.connect('" + huc.getURL() + "' error: " + huc.getResponseMessage());
                    }

                    return false;
                }
            }
        }
        catch (final Exception e) {
            if (displayError) {
                if (uc.getURL().getProtocol().equalsIgnoreCase("file"))
                    IcyLogger.error(NetworkUtil.class, e, e.getLocalizedMessage());
                else {
                    if (!hasInternetAccess())
                        IcyLogger.error(NetworkUtil.class, "Can't connect to '" + uc.getURL() + "' (no internet connection).");
                    else {
                        // HTTPS not supported while we have a HTTPS connection to icy web site
                        if (!isHTTPSSupported() && uc.getURL().toString().toLowerCase().startsWith("https://icy")) {
                            IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.connect('" + uc.getURL() + "') error: HTTPS connection not supported (see detail below).");
                        }
                        else {
                            IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.connect('" + uc.getURL() + "' error.");
                        }
                    }
                }
            }

            return false;
        }

        return true;
    }

    /**
     * Returns a new {@link InputStream} from specified {@link URLConnection} (null if an error
     * occurred).
     *
     * @param uc
     *        URLConnection object.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static InputStream getInputStream(final URLConnection uc, final boolean displayError) {
        if (uc == null) {
            if (displayError) {
                IcyLogger.error(NetworkUtil.class, "NetworkUtil.getInputStream(URLConnection uc) error: URLConnection object is null !");
            }

            return null;
        }

        try {
            return uc.getInputStream();
        }
        catch (final IOException e) {
            if (displayError) {
                if (!hasInternetAccess())
                    IcyLogger.error(NetworkUtil.class, "Can't connect to '" + uc.getURL() + "' (no internet connection).");
                    // HTTPS not supported while we have a HTTPS connection to icy web site
                else if (!isHTTPSSupported() && uc.getURL().toString().toLowerCase().startsWith("https://icy")) {
                    IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.getInputStream('" + uc.getURL() + "') error: HTTPS connection not supported !");
                }
                else {
                    IcyLogger.error(NetworkUtil.class, e, "NetworkUtil.getInputStream('" + uc.getURL() + "') error.");
                }
            }

            return null;
        }
    }

    /**
     * Returns a new {@link InputStream} from specified URL (null if an error occurred).
     *
     * @param url
     *        url we want to connect and retrieve the InputStream.
     * @param login
     *        login if the connection requires authentication.<br>
     *        Set it to null if no authentication needed.
     * @param pass
     *        login if the connection requires authentication.
     *        Set it to null if no authentication needed.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static InputStream getInputStream(final URL url, final String login, final String pass, final boolean disableCache, final boolean displayError) {
        final URLConnection uc = openConnection(url, login, pass, disableCache, true, displayError);

        if (uc != null)
            return getInputStream(uc, displayError);

        return null;
    }

    /**
     * Returns a new {@link InputStream} from specified URL (null if an error occurred).
     *
     * @param url
     *        url we want to connect and retrieve the InputStream.
     * @param auth
     *        Authentication informations.
     * @param disableCache
     *        Disable proxy cache if any.
     * @param displayError
     *        Display error message in console if something wrong happen.
     */
    public static InputStream getInputStream(final URL url, final AuthenticationInfo auth, final boolean disableCache, final boolean displayError) {
        if ((auth != null) && (auth.isEnabled()))
            return getInputStream(url, auth.getLogin(), auth.getPassword(), disableCache, displayError);

        return getInputStream(url, null, null, disableCache, displayError);
    }

    public static void disableCache(final URLConnection uc) {
        uc.setDefaultUseCaches(false);
        uc.setUseCaches(false);
        uc.setRequestProperty("Cache-Control", "no-cache");
        uc.setRequestProperty("Pragma", "no-cache");
    }

    /**
     * Process authentication on specified {@link URLConnection} with specified login and pass.
     */
    public static void setAuthentication(final URLConnection uc, final String login, final String pass) {
        final String req = login + ":" + pass;
        final String encoded;

        // we are now always using Java 8 at least
        // if (SystemUtil.getJavaVersionAsNumber() >= 8d)
        encoded = java.util.Base64.getEncoder().encodeToString(req.getBytes());
        // else
        // encoded = new sun.misc.BASE64Encoder().encode(req.getBytes());

        uc.setRequestProperty("Authorization", "Basic " + encoded);
    }

    public static String getContentString(final Map<String, String> values) {
        final StringBuilder result = new StringBuilder();

        for (final Entry<String, String> entry : values.entrySet()) {
            final String key = entry.getKey();

            if (!StringUtil.isEmpty(key)) {
                final String value = entry.getValue();

                result.append("&").append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append("=");

                if (!StringUtil.isEmpty(value))
                    result.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        }

        // remove the initial "&" character
        return result.substring(1);
    }

    public static String postData(final String target, final Map<String, String> values, final String login, final String pass) throws IOException {
        return postData(target, getContentString(values), login, pass);
    }

    public static String postData(final String target, final String content, final String login, final String pass) throws IOException {
        final StringBuilder response = new StringBuilder();

        final URLConnection uc = openConnection(target, true, true);

        if (uc == null)
            return null;

        // set connection parameters
        uc.setDoInput(true);
        uc.setDoOutput(true);

        // authentication needed ?
        if (login != null)
            setAuthentication(uc, login, pass);

        // make server believe we are form data...
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (final DataOutputStream out = new DataOutputStream(uc.getOutputStream())) {
            // write out the bytes of the content string to the stream
            out.writeBytes(content);
            out.flush();
        }

        // read response from the input stream.
        final InputStream inStream = getInputStream(uc, false);
        if (inStream == null)
            return null;

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(inStream))) {
            String temp;
            while ((temp = in.readLine()) != null)
                response.append(temp).append("\n");
        }

        return response.toString();
    }

    public static String postData(final String target, final Map<String, String> values) throws IOException {
        return postData(target, values, null, null);
    }

    public static String postData(final String target, final String content) throws IOException {
        return postData(target, content, null, null);
    }

    public static void enableSystemProxy() {
        SystemUtil.setProperty("java.net.useSystemProxies", "true");
    }

    public static void disableSystemProxy() {
        SystemUtil.setProperty("java.net.useSystemProxies", "false");
    }

    public static void enableProxySetting() {
        SystemUtil.setProperty("proxySet", "true");
    }

    public static void disableProxySetting() {
        SystemUtil.setProperty("proxySet", "false");
    }

    public static void enableHTTPProxySetting() {
        SystemUtil.setProperty("http.proxySet", "true");
    }

    public static void disableHTTPProxySetting() {
        SystemUtil.setProperty("http.proxySet", "false");
    }

    public static void enableHTTPSProxySetting() {
        SystemUtil.setProperty("https.proxySet", "true");
    }

    public static void disableHTTPSProxySetting() {
        SystemUtil.setProperty("https.proxySet", "false");
    }

    public static void enableFTPProxySetting() {
        SystemUtil.setProperty("ftp.proxySet", "true");
    }

    public static void disableFTPProxySetting() {
        SystemUtil.setProperty("ftp.proxySet", "false");
    }

    public static void enableSOCKSProxySetting() {
        SystemUtil.setProperty("socksProxySet", "true");
    }

    public static void disableSOCKSProxySetting() {
        SystemUtil.setProperty("socksProxySet", "false");
    }

    public static void setProxyHost(final String host) {
        SystemUtil.setProperty("proxy.server", host);
    }

    public static void setProxyPort(final int port) {
        SystemUtil.setProperty("proxy.port", Integer.toString(port));
    }

    public static void setHTTPProxyHost(final String host) {
        SystemUtil.setProperty("http.proxyHost", host);
    }

    public static void setHTTPProxyPort(final int port) {
        SystemUtil.setProperty("http.proxyPort", Integer.toString(port));
    }

    public static void setHTTPProxyUser(final String user) {
        SystemUtil.setProperty("http.proxyUser", user);
    }

    public static void setHTTPProxyPassword(final String password) {
        SystemUtil.setProperty("http.proxyPassword", password);
    }

    public static void setHTTPSProxyHost(final String host) {
        SystemUtil.setProperty("https.proxyHost", host);
    }

    public static void setHTTPSProxyPort(final int port) {
        SystemUtil.setProperty("https.proxyPort", Integer.toString(port));
    }

    public static void setHTTPSProxyUser(final String user) {
        SystemUtil.setProperty("https.proxyUser", user);
    }

    public static void setHTTPSProxyPassword(final String password) {
        SystemUtil.setProperty("https.proxyPassword", password);
    }

    public static void setFTPProxyHost(final String host) {
        SystemUtil.setProperty("ftp.proxyHost", host);
    }

    public static void setFTPProxyPort(final int port) {
        SystemUtil.setProperty("ftp.proxyPort", Integer.toString(port));
    }

    public static void setFTPProxyUser(final String user) {
        SystemUtil.setProperty("ftp.proxyUser", user);
    }

    public static void setFTPProxyPassword(final String password) {
        SystemUtil.setProperty("ftp.proxyPassword", password);
    }

    public static void setSOCKSProxyHost(final String host) {
        SystemUtil.setProperty("socksProxyHost", host);
    }

    public static void setSOCKSProxyPort(final int port) {
        SystemUtil.setProperty("socksProxyPort", Integer.toString(port));
    }

    public static void setSOCKSProxyUser(final String user) {
        SystemUtil.setProperty("socksProxyUser", user);
    }

    public static void setSOCKSProxyPassword(final String password) {
        SystemUtil.setProperty("socksProxyPassword", password);
    }

    public static String getProxyHost() {
        return SystemUtil.getProperty("proxy.server");
    }

    public static int getProxyPort() {
        try {
            return Integer.parseInt(SystemUtil.getProperty("proxy.port"));
        }
        catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static String getHTTPProxyHost() {
        return SystemUtil.getProperty("http.proxyHost");
    }

    public static int getHTTPProxyPort() {
        try {
            return Integer.parseInt(SystemUtil.getProperty("http.proxyPort"));
        }
        catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static String getHTTPSProxyHost() {
        return SystemUtil.getProperty("https.proxyHost");
    }

    public static int getHTTPSProxyPort() {
        try {
            return Integer.parseInt(SystemUtil.getProperty("https.proxyPort"));
        }
        catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static String getFTPProxyHost() {
        return SystemUtil.getProperty("ftp.proxyHost");
    }

    public static int getFTPProxyPort() {
        try {
            return Integer.parseInt(SystemUtil.getProperty("ftp.proxyPort"));
        }
        catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static String getSOCKSProxyHost() {
        return SystemUtil.getProperty("socksProxyHost");
    }

    public static int getSOCKSProxyPort() {
        try {
            return Integer.parseInt(SystemUtil.getProperty("socksProxyPort"));
        }
        catch (final NumberFormatException e) {
            return 0;
        }
    }
}
