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

package fr.icy.model.cache;

import fr.icy.io.FileUtil;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class EHCache3 extends AbstractCache {
    private final @NonNull PersistentCacheManager manager;

    private static final @NonNull String ALIAS = "ehCache3";

    public EHCache3(final int cacheSizeMB, final @NonNull String path) {
        super();

        // delete previous cache file
        FileUtil.delete(path, true);

        final long freeBytes = new File(FileUtil.getDrive(path)).getUsableSpace();
        // subtract 200 MB to available space for safety, use 64 MB at min (well, not really useful then)
        final long freeMB = (freeBytes <= 0) ? Long.MAX_VALUE : Math.max(64, (freeBytes / (1024 * 1024)) - 200);

        manager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File(path)))
                .withCache(ALIAS,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, DataArray.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        //.heap(10, EntryUnit.ENTRIES)
                                        .heap(cacheSizeMB, MemoryUnit.MB)
                                        //.offheap((cacheSizeMB), MemoryUnit.MB)
                                        .disk(Math.min(freeMB, 500000L), MemoryUnit.MB, true)
                        )
                ).build();
        manager.init();
    }

    private @NonNull Cache<Integer, DataArray> getCache() {
        return manager.getCache(ALIAS, Integer.class, DataArray.class);
    }

    @Contract(pure = true)
    @Override
    public @NonNull String getName() {
        return "EHCache 3";
    }

    /**
     * @return <i>true</i> if the cache is empty
     */
    @Override
    public boolean isEmpty() {
        final Cache<Integer, DataArray> cache = getCache();
        return !cache.iterator().hasNext();
    }

    /**
     * Test the presence of a key in the cache
     */
    @Override
    public boolean isInCache(final @NonNull Integer key) {
        return getCache().containsKey(key);
    }

    /**
     * Return used memory for cache (in bytes)
     */
    @Contract(pure = true)
    @Override
    public long usedMemory() {
        return 0L;
    }

    /**
     * Return used disk space for cache (in bytes)
     */
    @Contract(pure = true)
    @Override
    public long usedDisk() {
        return 0L;
    }

    /**
     * Get all element keys in the cache
     */
    @Override
    @NonNull Collection<Integer> getAllKeys() {
        final Cache<Integer, DataArray> cache = getCache();
        final Iterator<Cache.Entry<Integer, DataArray>> iterator = cache.iterator();
        final ArrayList<Integer> list = new ArrayList<>();
        while (iterator.hasNext()) {
            final Cache.Entry<Integer, DataArray> entry = iterator.next();
            list.add(entry.getKey());
        }
        return list;
    }

    /**
     * Get an object from the cache from its key
     */
    // TODO check if data is really persistent (to prevent throwing exception)
    @Override
    public @NonNull Object get(final @NonNull Integer key) throws CacheException {
        if (profiling)
            startProf();

        DataArray result = null;

        try {
            final Cache<Integer, DataArray> cache = getCache();
            final DataArray o = cache.get(key);

            if (o != null)
                result = o;

            // check if eternal data was lost (it seems that sometime EhCache loss data put in eternal state !!)
            if (result == null)
                throw new CacheException("ImageCache error: data '" + key + "' couldn't be retrieved (data lost)");

            return result.getArray();
        }
        finally {
            if (profiling)
                endProf();
        }
    }

    /**
     * Put an object in the cache with its associated key
     */
    @Override
    public void set(final @NonNull Integer key, final @NonNull Object object) throws CacheException {
        if (profiling)
            startProf();

        try {
            final Cache<Integer, DataArray> cache = getCache();

            cache.put(key, new DataArray(object));
        }
        catch (final Exception e) {
            throw new CacheException("ImageCache error: data '" + key + "' couldn't be saved in cache", e);
        }
        finally {
            if (profiling)
                endProf();
        }
    }

    /**
     * Clean the cache (evict all no eternal data)
     */
    @Override
    public void clean() {
        System.gc();
    }

    /**
     * Clear the cache
     */
    @Override
    public void clear() throws CacheException {
        if (profiling)
            startProf();

        try {
            final Cache<Integer, DataArray> cache = getCache();
            cache.clear();
        }
        catch (final Exception e) {
            throw new CacheException("ImageCache: an error occurred while clearing cache", e);
        }
        finally {
            if (profiling)
                endProf();
        }
    }

    /**
     * Remove an object from the cache from its key
     */
    @Override
    public void remove(final @NonNull Integer key) throws CacheException {
        if (profiling)
            startProf();

        try {
            final Cache<Integer, DataArray> cache = getCache();
            cache.remove(key);
        }
        catch (final Exception e) {
            throw new CacheException("ImageCache: an error occurred while removing data '" + key + "' from cache", e);
        }
        finally {
            if (profiling)
                endProf();
        }
    }

    /**
     * Call it when you're done with the cache (release resources and cleanup)
     */
    @Override
    public void end() {
        manager.close();
    }


    // THIS CLASS CANNOT BE A RECORD !!!
    private static final class DataArray implements Serializable {
        private final Object array;

        @Contract(pure = true)
        public DataArray(final Object array) {
            this.array = array;
        }

        @Contract(pure = true)
        public Object getArray() {
            return array;
        }
    }
}
