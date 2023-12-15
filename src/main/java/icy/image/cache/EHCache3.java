/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

import icy.file.FileUtil;
import icy.system.logging.IcyLogger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class EHCache3 extends AbstractCache {
    private final Set<Integer> eternalStoredKeys;
    private final Cache<Integer, Object> cache;

    public EHCache3(final int cacheSizeMB, final String path) {
        super();

        eternalStoredKeys = new HashSet<>();

        // TODO: 05/12/2023 Check if it's still necessary to do this
        // get old ehcache agent JAR files
        final String[] oldFiles = FileUtil.getFiles(FileUtil.getTempDirectory(), pathname -> {
            // old ehcache temp agent JAR files
            return FileUtil.getFileName(pathname.getAbsolutePath(), false).startsWith("ehcache");
        }, false, false, false);
        // delete these files as ehcache don't do it itself
        for (final String file : oldFiles)
            FileUtil.delete(file, false);

        // delete previous cache file
        FileUtil.delete(path, true);

        final long freeBytes = new File(FileUtil.getDrive(path)).getUsableSpace();
        // subtract 200 MB to available space for safety, use 64 MB at min (well, not realy usefull then)
        final long freeMB = (freeBytes <= 0) ? Long.MAX_VALUE : Math.max(64, (freeBytes / (1024 * 1024)) - 200);

        try (final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(
                        "ehCache3",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Integer.class,
                                Object.class,
                                ResourcePoolsBuilder.heap(10L)
                        ))
                .build()) {
            cacheManager.init();

            cache = cacheManager.getCache("ehCache3", Integer.class, Object.class);
        }
    }

    @Override
    public String getName() {
        return "EHCache 3";
    }

    /**
     * @return <i>true</i> if cache is empty
     */
    @Override
    public boolean isEmpty() {
        return !cache.iterator().hasNext();
    }

    /**
     * Test presence of a key in the cache
     */
    @Override
    public boolean isInCache(final Integer key) {
        return cache.containsKey(key);
    }

    /**
     * Test presence of a key in the cache
     */
    @Override
    public boolean isOnMemoryCache(final Integer key) {
        return cache.containsKey(key);
    }

    /**
     * Test presence of a key in the cache
     */
    @Override
    public boolean isOnDiskCache(final Integer key) {
        return cache.containsKey(key);
    }

    /**
     * Return used memory for cache (in bytes)
     */
    @Override
    public long usedMemory() {
        return 0;
    }

    /**
     * Return used disk space for cache (in bytes)
     */
    @Override
    public long usedDisk() {
        return 0;
    }

    /**
     * Get all element keys in the cache
     */
    @Override
    public Collection<Integer> getAllKeys() throws CacheException {
        return null;
    }

    /**
     * Get an object from cache from its key
     */
    @Override
    public Object get(final Integer key) throws CacheException {
        if (profiling)
            startProf();

        final boolean checkNull;
        Object result = null;

        // test if we need to check for null result
        synchronized (eternalStoredKeys) {
            checkNull = eternalStoredKeys.contains(key);
        }

        try {
            final Object o = cache.get(key);

            if (o != null)
                result = o;

            // check if eternal data was lost (it seems that sometime EhCache loss data put in eternal state !!)
            if (checkNull && (result == null))
                throw new CacheException("ImageCache error: data '" + key + "' couldn't be retrieved (data lost)");

            return result;
        }
        finally {
            if (profiling)
                endProf();
        }
    }

    /**
     * Put an object in cache with its associated key
     */
    @Override
    public void set(final Integer key, final Object object, final boolean eternal) throws CacheException {
        if (profiling)
            startProf();

        synchronized (eternalStoredKeys) {
            if ((object != null) && eternal)
                eternalStoredKeys.add(key);
            else
                eternalStoredKeys.remove(key);
        }

        try {
            cache.put(key, object);

            IcyLogger.debug(String.format("Image put in cache : %d", key.intValue()));
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

        eternalStoredKeys.clear();

        try {
            cache.clear();
        }
        catch (final Exception e) {
            throw new CacheException("ImageCache: an error occured while clearing cache", e);
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
    public void remove(final Integer key) throws CacheException {
        if (profiling)
            startProf();

        synchronized (eternalStoredKeys) {
            // remove from keyset
            eternalStoredKeys.remove(key);
        }

        try {
            cache.remove(key);
        }
        catch (final Exception e) {
            throw new CacheException("ImageCache: an error occured while removing data '" + key + "' from cache", e);
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

    }
}
