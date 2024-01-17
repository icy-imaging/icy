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

package icy.image.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
abstract class AbstractCache {
    boolean profiling;
    long profilingTime;
    long startTime;

    AbstractCache() {
        super();

        profiling = false;
    }

    abstract String getName();

    final void setProfiling(final boolean value) {
        profiling = value;
    }

    final void resetProfiling() {
        profilingTime = 0L;
    }

    final long getProfilingTime() {
        return profilingTime;
    }

    final protected void startProf() {
        startTime = System.nanoTime();
    }

    final protected void endProf() {
        profilingTime += (System.nanoTime() - startTime) / 1000000L;
    }

    /**
     * @return <i>true</i> if cache is empty
     */
    abstract boolean isEmpty();

    /**
     * Test presence of a key in the cache
     */
    abstract boolean isInCache(@NotNull Integer key);

    /**
     * Test presence of a key in the cache
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    boolean isOnMemoryCache(final @NotNull Integer key) {
        return isInCache(key);
    }

    /**
     * Test presence of a key in the cache
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    boolean isOnDiskCache(final @NotNull Integer key) {
        return isInCache(key);
    }

    /**
     * Return used memory for cache (in bytes)
     */
    abstract long usedMemory();

    /**
     * Return used disk space for cache (in bytes)
     */
    abstract long usedDisk();

    /**
     * Get all element keys in the cache
     */
    @NotNull Collection<Integer> getAllKeys() {
        return Collections.emptyList();
    }

    /**
     * Get an object from cache from its key
     */
    abstract @Nullable Object get(@NotNull Integer key) throws CacheException;

    /**
     * Put an object in cache with its associated key
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    void set(@NotNull final Integer key, @NotNull final Object object, final boolean eternal) throws CacheException {

    }

    /**
     * Put an object in cache with its associated key
     */
    abstract void set(@NotNull Integer key, @NotNull Object object) throws CacheException;

    /**
     * Clean the cache (evict all no eternal data)
     */
    abstract void clean();

    /**
     * Clear the cache
     */
    abstract void clear() throws CacheException;

    /**
     * Remove an object from the cache from its key
     */
    abstract void remove(@NotNull Integer key) throws CacheException;

    /**
     * Call it when you're done with the cache (release resources and cleanup)
     */
    abstract void end();
}
