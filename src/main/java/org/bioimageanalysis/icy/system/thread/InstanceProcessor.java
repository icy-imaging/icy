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

import java.util.concurrent.FutureTask;

/**
 * Single instance processor.<br>
 * It allows to run processes on a thread and verify the constraint that only one single instance of
 * a specific process can be queued at a given time.
 */
public class InstanceProcessor extends Processor
{
    /**
     * Create an InstanceProcessor
     * @param maxWaiting waiting threads
     * @param priority priority of the thread
     */
    public InstanceProcessor(int maxWaiting, int priority)
    {
        super(maxWaiting, 1, priority);

        setThreadName("InstanceProcessor");
    }

    /**
     * Create an InstanceProcessor
     * @param priority priority of the thread
     */
    public InstanceProcessor(int priority)
    {
        this(-1, priority);
    }

    /**
     * Create an InstanceProcessor
     */
    public InstanceProcessor()
    {
        this(Processor.NORM_PRIORITY);
    }

    /**
     * Try to submit the specified task for execution and returns a Future representing that task.<br>
     * The Future's <i>get</i> method will return <i>null</i> upon <em>successful</em>
     * completion.<br>
     * Returns a <code>null</code> Future object if processor has already this task pending in queue
     * (in this case the new task is simply ignored)..
     */
    @Override
    protected synchronized <T> FutureTask<T> submit(FutureTaskAdapter<T> task)
    {
        // task already present in queue --> return null (mean the task was ignored)
        if ((task.runnable != null) && hasWaitingTasks(task.runnable))
            return null;
        if ((task.callable != null) && hasWaitingTasks(task.callable))
            return null;

        // add task only if not already present in queue
        return super.submit(task);
    }
}
