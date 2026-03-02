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
package fr.icy.system.thread;

import java.util.concurrent.FutureTask;

/**
 * The SingleProcessor class is a specialized extension of the Processor class that operates
 * with a fixed single-threaded execution model. It provides functionality to submit tasks
 * for execution while adhering to the constraints of single-threaded processing. The processor
 * can optionally enable or disable task queuing.
 * <p>
 * Features of the class include:
 * - Single-threaded execution with one thread for task processing.
 * - Optional task queue handling based on the enableQueue parameter in the constructors.
 * - Tasks can be rejected if certain conditions are met, such as when queuing is disabled
 * or the processor is actively processing tasks.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SingleProcessor extends Processor {
    private final boolean queueEnabled;

    /**
     * Constructs a SingleProcessor instance with specified task queuing behavior and thread name.
     * The processor is configured to operate with a single-threaded execution model, where tasks
     * can optionally be queued based on the enableQueue parameter.
     *
     * @param enableQueue Determines if task queuing is enabled. If {@code true}, tasks submitted
     *                    while the processor is busy are queued for later execution. If {@code false},
     *                    tasks submitted while the processor is busy are rejected.
     * @param name        The name of the thread used by this processor for task execution.
     */
    public SingleProcessor(final boolean enableQueue, final String name) {
        super(1, 1);

        queueEnabled = enableQueue;
        setRejectedExecutionHandler(new DiscardPolicy());
        setThreadName(name);
    }

    /**
     * Constructs a SingleProcessor instance with specified task queuing behavior.
     * The processor is configured to operate with a single-threaded execution model, where tasks
     * can optionally be queued based on the enableQueue parameter.
     *
     * @param enableQueue Determines if task queuing is enabled. If {@code true}, tasks submitted
     *                    while the processor is busy are queued for later execution. If {@code false},
     *                    tasks submitted while the processor is busy are rejected.
     */
    public SingleProcessor(final boolean enableQueue) {
        this(enableQueue, "SingleProcessor");
    }

    /**
     * Submits a task for execution, considering the state of the processor and its queuing behavior.
     * If the processor is not actively processing tasks, the task is submitted immediately.
     * Otherwise, if task queuing is enabled, all previously queued tasks are cleared, and the new task is submitted.
     * If queuing is disabled and the processor is already processing tasks, the task is ignored.
     *
     * @param <T> the result type of the task
     * @param task the task to be submitted for execution
     * @return the submitted {@code FutureTask} if the task was accepted for execution;
     *         {@code null} if the task was ignored
     */
    @Override
    protected synchronized <T> FutureTask<T> submit(final FutureTaskAdapter<T> task) {
        // add task only if not already processing or queue empty
        if (getActiveCount() == 0)
            return super.submit(task);

        if (queueEnabled) {
            removeAllWaitingTasks();
            return super.submit(task);
        }

        // return null mean the task was ignored
        return null;
    }

    /**
     * Indicates whether task queuing is enabled for this processor.
     * Task queuing determines whether tasks submitted while the processor is actively
     * processing other tasks can be queued for later execution.
     *
     * @return {@code true} if task queuing is enabled; {@code false} otherwise.
     */
    public boolean isQueueEnabled() {
        return queueEnabled;
    }
}
