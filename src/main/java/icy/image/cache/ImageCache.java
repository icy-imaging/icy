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

import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.system.logging.IcyLogger;

import java.util.*;

/**
 * Image Cache static util class.<br>
 * The cache store and return 1D array data corresponding to the internal {@link IcyBufferedImage#getDataXY(int)} image data.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class ImageCache {
    private static AbstractCache cache = null;

    public static synchronized boolean init(final int cacheSizeMB, final String path) {
        if (cache == null && !Icy.isCacheDisabled()) {
            try {
                final String cacheName = "/icy3_cache";
                cache = new EHCache3(cacheSizeMB, path + cacheName);

                IcyLogger.info(ImageCache.class, String.format(
                        "Image cache initialized (reserved memory = %d MB, disk cache location = '%s%s'",
                        cacheSizeMB,
                        path,
                        cacheName
                ));
            }
            catch (final Exception e) {
                IcyLogger.error(ImageCache.class, e, "Error while initialize image cache.");
            }
        }

        return cache != null;
    }

    /**
     * Called when we want to shutdown the cache when no anymore in use
     *
     * @return false if cache still contains data so it cannot be shutdown
     */
    public static synchronized boolean shutDownIfEmpty() {
        if (cache != null) {
            // clean the cache
            cache.clean();
            // not empty ? --> cannot shutdown
            if (!cache.isEmpty()) {
                if (!getAllKeys().isEmpty())
                    return false;
            }

            shutDown();
        }

        return true;
    }

    /**
     * Called when the cache is no longer used (Releasing all resources and performing cleanup).
     *
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static synchronized void shutDown() {
        if (cache != null) {
            cache.end();
            cache = null;

            IcyLogger.info(ImageCache.class, "Image cache shutdown..");
        }
    }

    /**
     * @param image Image to check.
     * @return {@code true} if the image is present in the image cache. {@code false} otherwise.
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static boolean isInCache(final IcyBufferedImage image) throws RuntimeException {
        checkCacheLoaded();
        return cache.isInCache(getKey(image));
    }

    /**
     * @return Used memory for cache (in bytes).
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static long usedMemory() throws RuntimeException {
        checkCacheLoaded();
        return cache.usedMemory();
    }

    /**
     * @return Used disk space for cache (in bytes).'
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static long usedDisk() throws RuntimeException {
        checkCacheLoaded();
        return cache.usedDisk();
    }

    /**
     * Gets all data {@link IcyBufferedImage} in the cache.
     *
     * @return All images stored in the cache.
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static Collection<IcyBufferedImage> getAllKeys() throws RuntimeException {
        checkCacheLoaded();
        return getImages(cache.getAllKeys(), false);
    }

    private static Collection<IcyBufferedImage> getImages(final Collection<Integer> keys, final boolean getNull) {
        final List<IcyBufferedImage> result = new ArrayList<>(keys.size());

        for (final Integer key : keys) {
            final IcyBufferedImage image = getImage(key);

            if (getNull || (image != null))
                result.add(image);
        }

        return result;
    }

    private static IcyBufferedImage getImage(final Integer key) {
        return IcyBufferedImage.getIcyBufferedImage(key);
    }

    /**
     * Gets all data array from cache from a given Collection of {@link IcyBufferedImage}.
     *
     * @param keys Collection of images used to retrieve data from cache.
     * @return All the data stored on the cache associated to the images given as parameter.
     * @throws CacheException   If an error occurs during cache retrieval.
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static Map<IcyBufferedImage, Object> get(final Collection<IcyBufferedImage> keys) throws CacheException, RuntimeException {
        checkCacheLoaded();
        final Map<IcyBufferedImage, Object> result = new HashMap<>();

        for (final IcyBufferedImage key : keys)
            result.put(key, cache.get(getKey(key)));

        return result;
    }

    /**
     * Gets the corresponding data array (2D native array) from cache from a given {@link IcyBufferedImage}.
     *
     * @param key Image used to retrieve data from cache.
     * @return Retrieved data array (2D native array).
     * @throws CacheException   If an error occurs during cache retrieval.
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static Object get(final IcyBufferedImage key) throws CacheException, RuntimeException {
        checkCacheLoaded();
        return cache.get(getKey(key));
    }

    /**
     * Puts the specified data array (2D native array) into cache with its associated key.
     *
     * @param key     Image used as key for the array.
     * @param object  Data array to store.
     * @throws CacheException   If an error occurs during cache storage.
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static void set(final IcyBufferedImage key, final Object object) throws CacheException, RuntimeException {
        checkCacheLoaded();
        cache.set(getKey(key), object);
    }

    /**
     * Removes an object from the cache from its key.
     *
     * @param key Image identifying the object to remove.
     * @throws CacheException   If an error occurs during cache removal.
     * @throws RuntimeException If the cache module has not been loaded.
     */
    public static void remove(final IcyBufferedImage key) throws CacheException, RuntimeException {
        checkCacheLoaded();
        cache.remove(getKey(key));
    }

    private static Integer getKey(final IcyBufferedImage image) {
        return Integer.valueOf(System.identityHashCode(image));
    }

    private static void checkCacheLoaded() throws RuntimeException {
        if (!isInit()) {
            throw new RuntimeException("Cache module is not been enabled. Check launch parameters!");
        }
    }

    public static boolean isInit() {
        return cache != null;
    }

    /**
     * @deprecated Use {@link #isInit()} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static boolean isEnabled() {
        return isInit();
    }
}
