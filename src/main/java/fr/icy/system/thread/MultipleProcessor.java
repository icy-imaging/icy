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

/**
 * The {@code MultipleProcessor} class extends the {@code Processor} class and provides
 * multi-threaded processing capabilities with configurable task queuing behavior.
 * This class allows for customized thread management and task handling, making it
 * suitable for scenarios requiring concurrent processing with optional queuing of tasks.
 */
public class MultipleProcessor extends Processor {
    /**
     * Constructs a {@code MultipleProcessor} instance with specified configurations
     * for maximum task queue size, thread count, and thread naming. This processor
     * is designed for multi-threaded task execution and provides a custom rejected
     * execution handler.
     *
     * @param maxWaiting The maximum number of tasks that can be queued while waiting.
     *                   If the queue reaches this limit, additional tasks may be rejected.
     * @param numThread  The number of threads available for processing tasks, determining
     *                   the level of concurrency.
     * @param name       The name assigned to threads created by this processor.
     */
    public MultipleProcessor(final int maxWaiting, final int numThread, final String name) {
        super(maxWaiting, numThread);

        setRejectedExecutionHandler(new DiscardPolicy());
        setThreadName(name);
    }

    /**
     * Constructs a {@code MultipleProcessor} instance with specified configurations
     * for maximum task queue size and thread count. A default thread name is used
     * for threads created by the processor.
     *
     * @param maxWaiting The maximum number of tasks that can be queued while waiting.
     *                   If the queue reaches this limit, additional tasks may be rejected.
     * @param numThread  The number of threads available for processing tasks, determining
     *                   the level of concurrency.
     */
    public MultipleProcessor(final int maxWaiting, final int numThread) {
        this(maxWaiting, numThread, "MultipleProcessor");
    }

    /**
     * Constructs a {@code MultipleProcessor} instance with the specified thread count
     * and thread naming. The maximum task queue size is set to a default value.
     *
     * @param numThread The number of threads available for processing tasks, determining
     *                  the level of concurrency.
     * @param name      The name assigned to threads created by this processor.
     */
    public MultipleProcessor(final int numThread, final String name) {
        this(-1, numThread, name);
    }

    /**
     * Constructs a {@code MultipleProcessor} instance with the specified thread count.
     * A default thread name is used for threads created by the processor.
     *
     * @param numThread The number of threads available for processing tasks, determining
     *                  the level of concurrency.
     */
    public MultipleProcessor(final int numThread) {
        this(numThread, "MultipleProcessor");
    }
}
