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

package org.bioimageanalysis.icy.system.thread;

import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

/**
 * Thread utilities class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
@SuppressWarnings("resource") // For keeping thread active
public class ThreadUtil {
    /**
     * This class is used to catch exception in the EDT.
     */
    public static class CaughtRunnable implements Runnable {
        private final Runnable runnable;

        public CaughtRunnable(final Runnable runnable) {
            super();

            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            }
            catch (final Throwable t) {
                IcyExceptionHandler.handleException(t, true);
            }
        }
    }

    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = Thread.MIN_PRIORITY;

    /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = Thread.NORM_PRIORITY;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = Thread.MAX_PRIORITY;

    // low priority background processor
    private static final Processor bgProcessor;
    // single Runnable / Callable instance processor
    private static final InstanceProcessor[] instanceProcessors;
    // low priority single Runnable / Callable instance processor
    private static final InstanceProcessor[] bgInstanceProcessors;

    static {
        int wantedThread = SystemUtil.getNumberOfCPUs();
        wantedThread = Math.max(wantedThread, 4);

        // 64 bits JVM, can have higher limit
        bgProcessor = new Processor(Math.min(wantedThread, 16));
        instanceProcessors = new InstanceProcessor[Math.min(wantedThread, 8)];
        bgInstanceProcessors = new InstanceProcessor[Math.min(wantedThread, 8)];

        bgProcessor.setPriority(MIN_PRIORITY);
        bgProcessor.setThreadName("Background processor");
        bgProcessor.setKeepAliveTime(3, TimeUnit.SECONDS);

        for (int i = 0; i < instanceProcessors.length; i++) {
            // keep these thread active
            instanceProcessors[i] = new InstanceProcessor(NORM_PRIORITY);
            instanceProcessors[i].setThreadName("Background instance processor");
            instanceProcessors[i].setKeepAliveTime(3, TimeUnit.SECONDS);
            bgInstanceProcessors[i] = new InstanceProcessor(MIN_PRIORITY);
            bgInstanceProcessors[i].setThreadName("Background instance processor (low priority)");
            bgInstanceProcessors[i].setKeepAliveTime(3, TimeUnit.SECONDS);
        }
    }

    /**
     * Shutdown all background runner.
     */
    public static void shutdown() {
        bgProcessor.shutdown();
        for (int i = 0; i < instanceProcessors.length; i++) {
            instanceProcessors[i].shutdown();
            bgInstanceProcessors[i].shutdown();
        }
    }

    /**
     * @return Return true if all background runner are shutdown and terminated.
     */
    public static boolean isShutdownAndTerminated() {
        for (int i = 0; i < instanceProcessors.length; i++) {
            if (!instanceProcessors[i].isTerminated())
                return false;
            if (!bgInstanceProcessors[i].isTerminated())
                return false;
        }
        return bgProcessor.isTerminated();
    }

    /**
     * @return true if the current thread is an AWT event dispatching thread.
     */
    public static boolean isEventDispatchThread() {
        return EventQueue.isDispatchThread();
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread.<br>
     * Any exception is automatically caught by Icy exception handler.
     * @param runnable running task
     * @param wait
     *        If set to true, the method wait until completion, in this case you have to take
     *        attention to not cause any dead lock.
     * @see #invokeLater(Runnable)
     * @see #invokeNow(Runnable)
     */
    public static void invoke(final Runnable runnable, final boolean wait) {
        if (wait)
            invokeNow(runnable);
        else
            invokeLater(runnable);
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread now and wait
     * until completion.<br>
     * Any exception is automatically caught by Icy exception handler, if you want to catch them use
     * {@link #invokeNow(Callable)} instead.<br>
     * Use this method carefully as it may lead to dead lock.
     * @param runnable running task
     */
    public static void invokeNow(final Runnable runnable) {
        if (isEventDispatchThread()) {
            try {
                runnable.run();
            }
            catch (final Throwable t) {
                // the runnable thrown an exception
                IcyExceptionHandler.handleException(t, true);
            }
        }
        else {
            try {
                EventQueue.invokeAndWait(runnable);
            }
            catch (final InvocationTargetException e) {
                // the runnable thrown an exception
                IcyExceptionHandler.handleException(e.getTargetException(), true);
            }
            catch (final InterruptedException e) {
                // interrupt exception
                IcyLogger.error(ThreadUtil.class, e, "ThreadUtil.invokeNow(...) error.");
            }
        }
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread.<br>
     * If we already are on the EDT the <code>Runnable</code> is executed immediately else it will
     * be executed later.
     *
     * @see #invokeLater(Runnable, boolean)
     * @param runnable running task
     */
    public static void invokeLater(final Runnable runnable) {
        invokeLater(runnable, false);
    }

    /**
     * Invoke the specified <code>Runnable</code> on the AWT event dispatching thread.<br>
     * Depending the <code>forceLater</code> parameter the <code>Runnable</code> can be executed
     * immediately if we are on the EDT.
     * @param runnable running task
     * @param forceLater
     *        If <code>true</code> the <code>Runnable</code> is forced to execute later even if we
     *        are on the Swing EDT.
     */
    public static void invokeLater(final Runnable runnable, final boolean forceLater) {
        final Runnable r = new CaughtRunnable(runnable);

        if ((!forceLater) && isEventDispatchThread())
            r.run();
        else
            EventQueue.invokeLater(r);
    }

    /**
     * @return Invoke the specified <code>Callable</code> on the AWT event dispatching thread now and return
     * the result.<br>
     * The returned result can be <code>null</code> when a {@link Throwable} exception happen.<br>
     * Use this method carefully as it may lead to dead lock.
     *
     * @throws InterruptedException
     *         if the current thread was interrupted while waiting
     * @throws Exception
     *         if the computation threw an exception
     * @param callable called threas
     * @param <T> generic Object
     */
    public static <T> T invokeNow(final Callable<T> callable) throws Exception {
        if (SwingUtilities.isEventDispatchThread())
            return callable.call();

        final FutureTask<T> task = new FutureTask<>(callable);

        try {
            EventQueue.invokeAndWait(task);
        }
        catch (final InvocationTargetException e) {
            if (e.getTargetException() instanceof Exception)
                throw (Exception) e.getTargetException();

            throw new Exception(e.getTargetException());
        }

        try {
            return task.get();
        }
        catch (final ExecutionException e) {
            if (e.getCause() instanceof Exception)
                throw (Exception) e.getCause();

            throw new Exception(e.getCause());
        }
    }

    /**
     * @return Invoke the specified {@link Callable} on the AWT event dispatching thread.<br>
     * Depending the <code>forceLater</code> parameter the <code>Callable</code> can be executed
     * immediately if we are on the EDT.
     * @param callable thread
     * @param forceLater
     *        If <code>true</code> the <code>Callable</code> is forced to execute later even if we
     *        are on the EDT.
     * @param <T> generic Object
     */
    public static <T> Future<T> invokeLater(final Callable<T> callable, final boolean forceLater) {
        final FutureTask<T> task = new FutureTask<>(callable);
        invokeLater(task, forceLater);
        return task;
    }

    /**
     * Retrieve the instance processor (normal priority) to use for specified runnable.
     */
    private static InstanceProcessor getInstanceProcessor(final Runnable runnable) {
        // get processor index from the hash code
        return instanceProcessors[runnable.hashCode() % instanceProcessors.length];
    }

    /**
     * Retrieve the instance processor (normal priority) to use for specified callable.
     */
    private static InstanceProcessor getInstanceProcessor(final Callable<?> callable) {
        // get processor index from the hash code
        return instanceProcessors[callable.hashCode() % instanceProcessors.length];
    }

    /**
     * Retrieve the instance processor (low priority) to use for specified runnable.
     */
    private static InstanceProcessor getBgInstanceProcessor(final Runnable runnable) {
        // get processor index from the hash code
        return bgInstanceProcessors[runnable.hashCode() % bgInstanceProcessors.length];
    }

    /**
     * Retrieve the instance processor (low priority) to use for specified callable.
     */
    private static InstanceProcessor getBgInstanceProcessor(final Callable<?> callable) {
        // get processor index from the hash code
        return bgInstanceProcessors[callable.hashCode() % bgInstanceProcessors.length];
    }

    /**
     * Adds background processing (low priority) of specified Runnable.<br>
     * Returns <code>false</code> if background process queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     * @param runnable task
     * @return true if submitted task is not null
     */
    public static boolean bgRun(final Runnable runnable) {
        return (bgProcessor.submit(true, runnable) != null);
    }

    /**
     * Adds background processing (low priority) of specified Callable task.<br>
     * Returns a Future representing the pending result of the task or <code>null</code> if
     * background process queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     * @param callable threas
     * @param <T> generic Object
     * @return running process in background
     */
    public static <T> Future<T> bgRun(final Callable<T> callable) {
        return bgProcessor.submit(callable);
    }

    /**
     * Adds single processing (low priority) of specified Runnable.<br>
     * If this <code>Runnable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * @return Returns <code>false</code> if single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     * @param runnable running thread
     */
    public static boolean bgRunSingle(final Runnable runnable) {
        final InstanceProcessor processor = getBgInstanceProcessor(runnable);
        return (processor.submit(true, runnable) != null);
    }

    /**
     * Adds single processing (low priority) of specified Callable task.<br>
     * If this <code>Callable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * @return Returns a Future representing the pending result of the task or <code>null</code> if
     * single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     * @param <T> generic Object
     * @param callable thread
     */
    public static <T> Future<T> bgRunSingle(final Callable<T> callable) {
        final InstanceProcessor processor = getBgInstanceProcessor(callable);
        return processor.submit(callable);
    }

    /**
     * Add single processing (normal priority) of specified Runnable.<br>
     * If this <code>Runnable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * @return Return <code>false</code> if single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     * @param runnable running thread
     */
    public static boolean runSingle(final Runnable runnable) {
        final InstanceProcessor processor = getInstanceProcessor(runnable);
        return (processor.submit(true, runnable) != null);
    }

    /**
     * Add single processing (normal priority) of specified Callable task.<br>
     * If this <code>Callable</code> instance is already pending in single processes queue then
     * nothing is done.<br>
     * @return Return a Future representing the pending result of the task or <code>null</code> if
     * single processes queue is full.<br>
     * Don't use this method for long process (more than 1 second) as the number of thread is
     * limited and others processes may be executed too late.
     * @param callable thread
     * @param <T> generic Object
     */
    public static <T> Future<T> runSingle(final Callable<T> callable) {
        final InstanceProcessor processor = getInstanceProcessor(callable);
        return processor.submit(callable);
    }

    /**
     * @param runnable running thread
     * @return Return true if the specified runnable is waiting to be processed in background processing.
     */
    public static boolean hasWaitingBgTask(final Runnable runnable) {
        return bgProcessor.getWaitingTasksCount(runnable) > 0;
    }

    /**
     * @param callable thread
     * @return Return true if the specified callable is waiting to be processed in background processing.
     */
    public static boolean hasWaitingBgTask(final Callable<?> callable) {
        return bgProcessor.getWaitingTasksCount(callable) > 0;
    }

    /**
     * @param runnable running thread
     * @return Return true if the specified runnable is waiting to be processed<br>
     * in single scheme background processing (low priority).
     */
    public static boolean hasWaitingBgSingleTask(final Runnable runnable) {
        final InstanceProcessor processor = getBgInstanceProcessor(runnable);
        return processor.hasWaitingTasks(runnable);
    }

    /**
     * @param callable thread
     * @return Return true if the specified callable is waiting to be processed<br>
     * in single scheme background processing (low priority).
     */
    public static boolean hasWaitingBgSingleTask(final Callable<?> callable) {
        final InstanceProcessor processor = getBgInstanceProcessor(callable);
        return processor.hasWaitingTasks(callable);
    }

    /**
     * @param runnable running thread
     * @return Return true if the specified runnable is waiting to be processed<br>
     * in single scheme background processing (normal priority).
     */
    public static boolean hasWaitingSingleTask(final Runnable runnable) {
        final InstanceProcessor processor = getInstanceProcessor(runnable);
        return processor.hasWaitingTasks(runnable);
    }

    /**
     * @param callable thread
     * @return Return true if the specified callable is waiting to be processed<br>
     * in single scheme background processing (normal priority).
     */
    public static boolean hasWaitingSingleTask(final Callable<?> callable) {
        final InstanceProcessor processor = getInstanceProcessor(callable);
        return processor.hasWaitingTasks(callable);
    }

    /**
     * @return Return the number of active background tasks.
     */
    public static int getActiveBgTaskCount() {
        return bgProcessor.getActiveCount();
    }

    /**
     * @return Create a thread pool with the given name.<br>
     * The number of processing thread is automatically calculated given the number of core of the
     * system.
     * @param name thread's name
     *
     * @see Processor#Processor(int, int, int)
     */
    public static ExecutorService createThreadPool(final String name) {
        final Processor result = new Processor(SystemUtil.getNumberOfCPUs());

        result.setThreadName(name);

        return result;
    }

    /**
     * Same as {@link Thread#sleep(long)} except Exception is caught and ignored.
     * @param milli time of sleeping process in ms
     */
    public static void sleep(final long milli) {
        try {
            Thread.sleep(milli);
        }
        catch (final InterruptedException e) {
            // have to interrupt the thread
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Same as {@link Thread#sleep(long)} except Exception is caught and ignored.
     * @param milli time of sleeping process in ms
     */
    public static void sleep(final int milli) {
        try {
            Thread.sleep(milli);
        }
        catch (final InterruptedException e) {
            // have to interrupt the thread
            Thread.currentThread().interrupt();
        }
    }
}
