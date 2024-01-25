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

package icy.system.thread;

import icy.main.Icy;
import icy.system.logging.IcyLogger;

/**
 * @deprecated Use {@link InstanceProcessor} instead.
 */
@Deprecated(since = "2.4.3", forRemoval = true)
public class IdProcessor extends Processor {

    /**
     * Create an IdProcessor
     *
     * @deprecated uses default constructor instead
     * @param maxProcess maximum of resources allocated
     * @param maxProcessPerId maximum of resources for each id
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public IdProcessor(final int maxProcess, final int maxProcessPerId) {
        super();
    }

    /**
     * Create an IdProcessor
     * @param priority priority of the thread
     */
    public IdProcessor(final int priority) {
        super(-1, 1, priority);
        setThreadName("IdProcessor");
    }

    /**
     * Create an IdProcessor
     */
    public IdProcessor() {
        this(Processor.NORM_PRIORITY);
    }

    /**
     * Add a task to processor
     */
    @Override
    public synchronized boolean addTask(final Runnable task, final boolean onAWTEventThread, final int id) {
        if (task == null)
            return false;

        // we remove pending task if any
        removeFirstWaitingTask(id);

        if (!super.addTask(task, onAWTEventThread, id)) {
            if (!Icy.isExiting()) {
                // error while adding task
                IcyLogger.error(IdProcessor.class, "Cannot add task, ignore execution: " + task);
                return false;
            }
        }

        return true;
    }
}
