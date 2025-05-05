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

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Processor class.<br>
 * Allow you to queue and execute tasks on a defined set of thread.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Processor extends ThreadPoolExecutor {
    public static final int DEFAULT_MAX_WAITING = 1024;
    public static final int DEFAULT_MAX_PROCESSING = SystemUtil.getNumberOfCPUs();

    protected class ProcessorThreadFactory implements ThreadFactory {
        String name;

        public ProcessorThreadFactory(final String name) {
            super();

            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(final String value) {
            this.name = value;
        }

        String getThreadName() {
            return name;
        }

        @Override
        public Thread newThread(final @NotNull Runnable r) {
            final Thread result = new Thread(r, getThreadName());

            result.setPriority(priority);

            return result;
        }
    }

    protected static class ProcessorRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            // ignore if we try to submit process while Icy is exiting
            if (!Icy.isExiting())
                throw new RejectedExecutionException("Cannot add new task, ignore execution of " + r);
        }
    }

    protected static class FutureTaskAdapter<T> extends FutureTask<T> {
        public Runnable runnable;
        public Callable<T> callable;
        final boolean handleException;

        public FutureTaskAdapter(final Runnable runnable, final T result, final boolean handleException) {
            super(runnable, result);

            this.runnable = runnable;
            this.callable = null;
            this.handleException = handleException;
        }

        public FutureTaskAdapter(final Runnable runnable, final boolean handleException) {
            this(runnable, null, handleException);
        }

        public FutureTaskAdapter(final Callable<T> callable, final boolean handleException) {
            super(callable);

            this.runnable = null;
            this.callable = callable;
            this.handleException = handleException;
        }

        @Override
        protected void done() {
            super.done();

            if (handleException) {
                try {
                    get();
                }
                catch (final Exception e) {
                    IcyExceptionHandler.handleException(e.getCause(), true);
                }
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

    /**
     * parameters
     */
    int priority;

    /**
     * internal
     */
    protected Runnable waitingExecution;
    protected long lastAdd;

    /**
     * Create a new Processor with specified number of maximum waiting and processing tasks.<br>
     *
     * @param maxWaiting
     *        The length of waiting queue.
     * @param numThread
     *        The maximum number of processing thread.
     * @param priority
     *        Processor priority<br>
     *        <code>Processor.MIN_PRIORITY</code><br>
     *        <code>Processor.NORM_PRIORITY</code><br>
     *        <code>Processor.MAX_PRIORITY</code>
     */
    public Processor(final int maxWaiting, final int numThread, final int priority) {
        super(numThread, numThread, 2L, TimeUnit.SECONDS, (maxWaiting == -1) ? new LinkedBlockingQueue<>() : new LinkedBlockingQueue<>(maxWaiting));

        setThreadFactory(new ProcessorThreadFactory("Processor"));
        setRejectedExecutionHandler(new ProcessorRejectedExecutionHandler());
        allowCoreThreadTimeOut(true);

        this.priority = priority;

        waitingExecution = null;
    }

    /**
     * Create a new Processor with specified number of maximum waiting and processing tasks.
     *
     * @param maxWaiting
     *        The length of waiting queue.
     * @param numThread
     *        The maximum number of processing thread.
     */
    public Processor(final int maxWaiting, final int numThread) {
        this(maxWaiting, numThread, NORM_PRIORITY);
    }

    /**
     * Create a new Processor with specified number of processing thread.
     *
     * @param numThread
     *        The maximum number of processing thread.
     */
    public Processor(final int numThread) {
        this(-1, numThread, NORM_PRIORITY);
    }

    /**
     * Create a new Processor with default number of maximum waiting and processing tasks.
     */
    public Processor() {
        this(DEFAULT_MAX_WAITING, DEFAULT_MAX_PROCESSING);
    }

    @Override
    public boolean remove(final Runnable task) {
        // don't forget to remove the reference here
        if (waitingExecution == task)
            waitingExecution = null;

        return super.remove(task);
    }

    /**
     * @param handledException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param runnable
     *        the runnable task being wrapped
     * @param value
     *        the default value for the returned future
     * @param <T> generic Object
     * @return a <i>RunnableFuture</i> which when run will run the underlying runnable and which,
     *         as a <i>Future</i>, will yield the given value as its result and provide for
     *         cancellation of the underlying task.
     */
    protected <T> FutureTaskAdapter<T> newTaskFor(final boolean handledException, final Runnable runnable, final T value) {
        return new FutureTaskAdapter<>(runnable, value, handledException);
    }

    /**
     * @param handledException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param callable
     *        the callable task being wrapped
     * @param <T> generic Object
     * @return a <i>RunnableFuture</i> which when run will call the
     *         underlying callable and which, as a <i>Future</i>, will yield
     *         the callable's result as its result and provide for
     *         cancellation of the underlying task.
     */
    protected <T> FutureTaskAdapter<T> newTaskFor(final boolean handledException, final Callable<T> callable) {
        return new FutureTaskAdapter<>(callable, handledException);
    }

    @Override
    public void execute(final @NotNull Runnable task) {
        super.execute(task);
        // save the last executed task
        waitingExecution = task;
    }

    /**
     * Submit the given task (internal use only).
     * @param task task to run
     * @param <T> generic Object
     * @return Object
     */
    protected synchronized <T> FutureTask<T> submit(final FutureTaskAdapter<T> task) {
        execute(task);
        return task;
    }

    @Override
    public @NotNull Future<?> submit(final @NotNull Runnable task) {
        return submit(newTaskFor(false, task, null));
    }

    @Override
    public <T> @NotNull Future<T> submit(final @NotNull Runnable task, final T result) {
        return submit(newTaskFor(false, task, result));
    }

    @Override
    public <T> @NotNull Future<T> submit(final @NotNull Callable<T> task) {
        return submit(newTaskFor(false, task));
    }

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's <i>get</i> method will
     * return <i>null</i> upon <em>successful</em> completion.
     *
     * @param handleException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param task
     *        the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public Future<?> submit(final boolean handleException, final Runnable task) {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(handleException, task, null));
    }

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's <i>get</i> method will
     * return the given result upon successful completion.
     *
     * @param handleException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param task
     *        the task to submit
     * @param result
     *        the result to return
     * @param <T> generic Object
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public <T> Future<T> submit(final boolean handleException, final Runnable task, final T result) {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(handleException, task, result));
    }

    /**
     * Submits a value-returning task for execution and returns a
     * Future representing the pending results of the task. The
     * Future's <i>get</i> method will return the task's result upon
     * successful completion.
     * <p>
     * If you would like to immediately block waiting for a task, you can use constructions of the form
     * <i>result = exec.submit(aCallable).get();</i>
     * <p>
     * Note: The {@link Executors} class includes a set of methods that can convert some other common closure-like
     * objects, for example, {@link java.security.PrivilegedAction} to {@link Callable} form so they can be submitted.
     *
     * @param handleException
     *        if set to <code>true</code> then any occurring exception during the runnable
     *        processing will be catch by {@link IcyExceptionHandler}.
     * @param task
     *        the task to submit
     * @param <T> generic Object
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException
     *         if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException
     *         if the task is null
     */
    public <T> Future<T> submit(final boolean handleException, final Callable<T> task) {
        if (task == null)
            throw new NullPointerException();

        return submit(newTaskFor(handleException, task));
    }

    /**
     * @return Return true if one or more process are executing or we still have waiting tasks.
     */
    public boolean isProcessing() {
        return (getActiveCount() > 0) || hasWaitingTasks();
    }

    /**
     * Wait for all tasks completion
     */
    public void waitAll() {
        while (isProcessing())
            ThreadUtil.sleep(1);
    }

    /**
     * shutdown and wait current tasks completion
     */
    public void shutdownAndWait() {
        shutdown();
        while (!isTerminated())
            ThreadUtil.sleep(1);
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority
     *        the priority to set
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    /**
     * @return Return the thread name.
     */
    public String getThreadName() {
        return ((ProcessorThreadFactory) getThreadFactory()).getName();
    }

    /**
     * Set the wanted thread name.
     * @param defaultThreadName thread name
     */
    public void setThreadName(final String defaultThreadName) {
        ((ProcessorThreadFactory) getThreadFactory()).setName(defaultThreadName);
    }

    /**
     * @return Get the number of free slot in queue
     */
    public int getFreeSlotNumber() {
        return getQueue().remainingCapacity();
    }

    /**
     * @return Return true if queue is full
     */
    public boolean isFull() {
        return getFreeSlotNumber() == 0;
    }

    /**
     * @return Return waiting tasks
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks() {
        final BlockingQueue<Runnable> q = getQueue();
        final List<FutureTaskAdapter<?>> result = new ArrayList<>();

        synchronized (q) {
            for (final Runnable r : q)
                result.add((FutureTaskAdapter<?>) r);
        }

        return result;
    }

    /**
     * @param task running task
     * @return Return waiting tasks for the specified Runnable instance
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks(final Runnable task) {
        final List<FutureTaskAdapter<?>> result = new ArrayList<>();

        // scan all tasks
        for (final FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.runnable == task)
                result.add(f);

        return result;
    }

    /**
     * @param task called task
     * @return Return waiting tasks for the specified Callable instance
     */
    protected List<FutureTaskAdapter<?>> getWaitingTasks(final Callable<?> task) {
        final List<FutureTaskAdapter<?>> result = new ArrayList<>();

        // scan all tasks
        for (final FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.callable == task)
                result.add(f);

        return result;
    }

    /**
     * @return Return the number of waiting task
     */
    public int getWaitingTasksCount() {
        final int result = getQueue().size();

        // TODO : be sure that waitingExecution pass to null when task has been taken in account.
        // Queue can be empty right after a task submission.
        // For this particular case we return 1 if a task has been submitted
        // and not taken in account with a timeout of 1 second.
        if ((result == 0) && ((waitingExecution != null) && ((System.currentTimeMillis() - lastAdd) < 1000)))
            return 1;

        return result;
    }

    /**
     * @param task running task
     * @return Return the number of task waiting in queue for the specified <i>Runnable</i> instance.
     */
    public int getWaitingTasksCount(final Runnable task) {
        int result = 0;

        for (final FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.runnable == task)
                result++;

        return result;
    }

    /**
     * @param task running task
     * @return Return the number of task waiting in queue for the specified <i>Callable</i> instance.
     */
    public int getWaitingTasksCount(final Callable<?> task) {
        int result = 0;

        for (final FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.callable == task)
                result++;

        return result;
    }

    /**
     * @return Return true if we have at least one task waiting in queue
     */
    public boolean hasWaitingTasks() {
        return (getWaitingTasksCount() > 0);
    }

    /**
     * @param task running task
     * @return Return true if we have at least one task in queue for the specified <i>Runnable</i> instance.
     */
    public boolean hasWaitingTasks(final Runnable task) {
        // scan all tasks
        for (final FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.runnable == task)
                return true;

        return false;
    }

    /**
     * @param task called task
     * @return Return true if we have at least one task in queue for the specified <i>Callable</i> instance.
     */
    public boolean hasWaitingTasks(final Callable<?> task) {
        // scan all tasks
        for (final FutureTaskAdapter<?> f : getWaitingTasks())
            if (f.callable == task)
                return true;

        return false;
    }

    /**
     * @param task running task
     * @return Remove first waiting task for the specified <i>FutureTaskAdapter</i> instance.
     */
    protected boolean removeFirstWaitingTask(final FutureTaskAdapter<?> task) {
        if (task == null)
            return false;

        synchronized (getQueue()) {
            // remove first task of specified instance
            for (final FutureTaskAdapter<?> f : getWaitingTasks())
                if (f == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * @param task running task
     * @return Remove first waiting task for the specified <i>Runnable</i> instance.
     */
    public boolean removeFirstWaitingTask(final Runnable task) {
        if (task == null)
            return false;

        synchronized (getQueue()) {
            // remove first task of specified instance
            for (final FutureTaskAdapter<?> f : getWaitingTasks())
                if (f.runnable == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * @param task called task
     * @return Remove first waiting task for the specified <i>Callable</i> instance.
     */
    public boolean removeFirstWaitingTask(final Callable<?> task) {
        if (task == null)
            return false;

        synchronized (getQueue()) {
            // remove first task of specified instance
            for (final FutureTaskAdapter<?> f : getWaitingTasks())
                if (f.callable == task)
                    return remove(f);
        }

        return false;
    }

    /**
     * @param task running task
     * @return Remove all waiting tasks for the specified <i>Runnable</i> instance.
     */
    public boolean removeWaitingTasks(final Runnable task) {
        boolean result = false;

        synchronized (getQueue()) {
            // remove all tasks of specified instance
            for (final FutureTaskAdapter<?> f : getWaitingTasks(task))
                result |= remove(f);
        }

        return result;
    }

    /**
     * @param task called task
     * @return Remove all waiting tasks for the specified <i>Callable</i> instance.
     */
    public boolean removeWaitingTasks(final Callable<?> task) {
        boolean result = false;

        synchronized (getQueue()) {
            // remove all tasks of specified instance
            for (final FutureTaskAdapter<?> f : getWaitingTasks(task))
                result |= remove(f);
        }

        return result;
    }

    /**
     * Clear all waiting tasks
     */
    public void removeAllWaitingTasks() {
        waitingExecution = null;

        final BlockingQueue<Runnable> q = getQueue();

        synchronized (q) {
            // remove all tasks
            q.clear();
        }
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        super.beforeExecute(t, r);

        // ok we can remove reference...
        waitingExecution = null;
    }
}
