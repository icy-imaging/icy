/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
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
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.system;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import icy.gui.dialog.LoaderDialog;
import icy.gui.frame.AboutFrame;
import icy.gui.preferences.GeneralPreferencePanel;
import icy.gui.preferences.PreferenceFrame;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;
import icy.util.ReflectionUtil;

/**
 * OSX application compatibility class
 * 
 * @author stephane
 */
public class AppleUtil
{
    static final Thread fixThread = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            appleFixLiveRun();
        }
    }, "AppleFix");

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init()
    {
        // only when we have the GUI
        if (!Icy.getMainInterface().isHeadLess())
        {
            try
            {
                // java 8 or <
                if (SystemUtil.getJavaVersionAsNumber() < 9d)
                {
                    final ClassLoader classLoader = SystemUtil.getSystemClassLoader();
                    final Class appClass = classLoader.loadClass("com.apple.eawt.Application");
                    final Object app = appClass.getDeclaredConstructor().newInstance();

                    final Class listenerClass = classLoader.loadClass("com.apple.eawt.ApplicationListener");
                    final Object listener = Proxy.newProxyInstance(classLoader, new Class[] {listenerClass}, new InvocationHandler()
                    {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                        {
                            final Object applicationEvent = args[0];
                            final Class appEventClass = applicationEvent.getClass();
                            final Method m = appEventClass.getMethod("setHandled", boolean.class);

                            if (method.getName().equals("handleQuit"))
                            {
                                m.invoke(applicationEvent, Boolean.valueOf(Icy.exit(false)));
                            }
                            if (method.getName().equals("handleAbout"))
                            {
                                new AboutFrame();
                                m.invoke(applicationEvent, Boolean.valueOf(true));
                            }
                            if (method.getName().equals("handleOpenFile"))
                            {
                                new LoaderDialog();
                                m.invoke(applicationEvent, Boolean.valueOf(true));
                            }
                            if (method.getName().equals("handlePreferences"))
                            {
                                new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
                                m.invoke(applicationEvent, Boolean.valueOf(true));
                            }

                            return null;
                        }
                    });

                    Method m;
                    
                    m = appClass.getMethod("addApplicationListener", listenerClass);
                    m.invoke(app, listener);
                    m = appClass.getMethod("setDockIconImage", java.awt.Image.class);
                    m.invoke(app, ResourceUtil.IMAGE_ICY_256);
                    m = appClass.getMethod("addPreferencesMenuItem");
                    m.invoke(app);
                }
                // java 9 or >
                else
                {
                    final Desktop desktop = Desktop.getDesktop();
                    final Class<?> desktopClass = desktop.getClass();

                    // desktop.setAboutHandler(e -> { new AboutFrame(); });
                    // desktop.setPreferencesHandler(e -> { new PreferenceFrame(GeneralPreferencePanel.NODE_NAME); });
                    // desktop.setQuitHandler((e, r) -> { Icy.exit(false); });
                    // desktop.setOpenFileHandler(e -> { new LoaderDialog(); });

                    // use reflection so we can compile with Java 8
                    final ClassLoader classLoader = desktopClass.getClassLoader();
                    final Class<?> aboutHandlerClass = ClassUtil.findClass("java.awt.desktop.AboutHandler");
                    final Class<?> preferencesHandlerClass = ClassUtil.findClass("java.awt.desktop.PreferencesHandler");
                    final Class<?> quitHandlerClass = ClassUtil.findClass("java.awt.desktop.QuitHandler");
                    final Class<?> openFilesHandlerClass = ClassUtil.findClass("java.awt.desktop.OpenFilesHandler");

                    final Object proxyHandler = Proxy.newProxyInstance(classLoader,
                            new Class<?>[] {aboutHandlerClass, preferencesHandlerClass, quitHandlerClass, openFilesHandlerClass}, new InvocationHandler()
                            {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                                {
                                    final String methodName = method.getName();
                                    // final Class<?>[] parameterTypes = method.getParameterTypes();

                                    switch (methodName)
                                    {
                                        case "openFiles":
                                            new LoaderDialog();
                                            break;

                                        case "handleAbout​":
                                            new AboutFrame();
                                            break;

                                        case "handlePreferences":
                                            new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
                                            break;

                                        case "handleQuitRequestWith":
                                            // just exit
                                            Icy.exit(false);
                                            
//                                            if (!Icy.exit(false))
//                                                ReflectionUtil.invokeMethod(args[1], "cancelQuit​");
//                                            else
//                                                ReflectionUtil.invokeMethod(args[1], "performQuit​");
                                            break;

                                        default:
                                            // nothing to do
                                            break;
                                    }

                                    return null;
                                }
                            });

                    // desktop.setAboutHandler(e -> { new AboutFrame(); });
                    // desktop.setPreferencesHandler(e -> { new PreferenceFrame(GeneralPreferencePanel.NODE_NAME); });
                    // desktop.setQuitHandler((e, r) -> { Icy.exit(false); });
                    // desktop.setOpenFileHandler(e -> { new LoaderDialog(); });
                    
                    Method m;

                    m = ReflectionUtil.getMethod(desktopClass, "setAboutHandler", aboutHandlerClass);
                    m.invoke(desktop, proxyHandler);
                    m = ReflectionUtil.getMethod(desktopClass, "setPreferencesHandler", preferencesHandlerClass);
                    m.invoke(desktop, proxyHandler);
                    m = ReflectionUtil.getMethod(desktopClass, "setQuitHandler", quitHandlerClass);
                    m.invoke(desktop, proxyHandler);
                    m = ReflectionUtil.getMethod(desktopClass, "setOpenFileHandler", openFilesHandlerClass);
                    m.invoke(desktop, proxyHandler);

                    // final TaskBar taskbar = Taskbar.getTaskBar();
                    // taskbar.setIconImage(ResourceUtil.IMAGE_ICY_256);

                    final Class<?> taskBarClass = ClassUtil.findClass("java.awt.Taskbar");
                    final Object taskBar = taskBarClass.getDeclaredMethod("getTaskbar").invoke(null);

                    m = ReflectionUtil.getMethod(taskBarClass, "setIconImage", Image.class);
                    m.invoke(taskBar, ResourceUtil.IMAGE_ICY_256);
                }

                // set menu bar name
                SystemUtil.setProperty("com.apple.mrj.application.apple.menu.about.name", "Icy");
                SystemUtil.setProperty("apple.awt.application.name", "Icy");
            }
            catch (Exception e)
            {
                System.err.println("Warning: can't install OSX application wrapper...");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

        // start the fix thread
        fixThread.start();
    }

    /**
     * Apple fix live run (fixes specific OS X JVM stuff)
     */
    static void appleFixLiveRun()
    {
        while (true)
        {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();

            // fix memory leak introduced in java 1.6.0_29 in Mac OS X JVM
            // TODO : remove this when issue will be resolved in JVM
            final PropertyChangeListener[] leak = toolkit.getPropertyChangeListeners("apple.awt.contentScaleFactor");

            // remove listener
            for (int i = 0; i < leak.length; i++)
                toolkit.removePropertyChangeListener("apple.awt.contentScaleFactor", leak[i]);

            // no need more...
            ThreadUtil.sleep(500);
        }
    }
}
