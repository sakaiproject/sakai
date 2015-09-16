/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is an abstraction of the general concept of a cache in Sakai<br/>
 * <p>
 * A Cache holds objects with keys with a limited lifespan.
 * </p>
 * <p>
 * When the object expires, the cache may call upon a CacheRefresher to update the key's value. The update is done in a separate thread.
 * </p>
 * This is designed to align with JSR-107
 * https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/Cache.java
 * See https://jira.sakaiproject.org/browse/KNL-1162
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public interface Cache <K, V> { // Cache<K, V> extends Iterable<Cache.Entry<K, V>>, Closeable {

    /**
     * Get the cached payload, or null if not there (or expired)<br/>
     * NOTE: this will attempt to call the CacheRefresher and get
     * a new value for this key if one cannot be found
     * (partially cluster safe)<br/>
     * <b>NOTE:</b> Does not work like <b>get</b> from JSR-107 spec
     * (which returns an Element instead of the value and only returns null if the entry does not exist)
     *
     * @param key the unique key for a cached object
     * @return The cached payload, or null if the payload is not found in the cache,
     * (use {@link #containsKey(K)} to differentiate between not found and stored null)
     */
    V get(K key); // V get(K key);

    /**
     * Gets a collection of entries from the {@link Cache}, returning them as
     * {@link Map} of the values associated with the set of keys requested.
     * <p>
     * If the cache is configured read-through, and a get for a key would
     * return null because an entry is missing from the cache, the Cache's
     * {@link CacheLoader} is called in an attempt to load the entry. If an
     * entry cannot be loaded for a given key, the key will not be present in
     * the returned Map.
     *
     * @param keys The keys whose associated values are to be returned.
     * @return A map of entries that were found for the given keys. Keys not found
     *         in the cache are not in the returned map.
     * @throws NullPointerException  if keys is null or if keys contains a null
     * @throws IllegalStateException if the cache is closed
     * @throws RuntimeException      if there is a problem fetching the values
     * @throws ClassCastException    if the implementation is configured to perform
     *                               runtime-type-checking, and the key or value
     *                               types are incompatible with those that have been
     *                               configured for the {@link Cache}
     */
    Map<K, V> getAll(Set<? extends K> keys);

    /**
     * Test if an entry exists in the cache for a key,
     * this allows us to differentiate between not found and stored null
     *
     * More formally, returns <tt>true</tt> if and only if this cache contains a
     * mapping for a key <tt>k</tt> such that <tt>key.equals(k)</tt>.
     * (There can be at most one such mapping.)
     *
     * (This method works like the JSR-107 spec)
     *
     * @param key the unique key for a cached object
     * @return true if the cache contains an entry with this key, false otherwise
     */
    boolean containsKey(K key);

    //JSR-107 void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener);

    /**
     * Cache an object which will be expired based on the system cache configuration,
     * if the object already exists in the cache then it will be replaced,
     * (This method works like the JSR-107 spec)
     *
     * @param key the unique key for a cached object
     * @param payload the cache payload (thing to cache),
     * this should be {@link Serializable} if this is supposed to
     * be distributable or able to be stored in the disk cache,
     * null may be used (this will cause the cache to store a null value for this key)
     */
    void put(K key, V payload);

    //JSR-107 V getAndPut(K key, V value);

    /**
     * Copies all of the entries from the specified map to the {@link Cache}.
     * <p>
     * The effect of this call is equivalent to that of calling
     * put(key,value) on this cache once for each mapping
     * from key <tt>k</tt> to value <tt>v</tt> in the specified map.
     * <p>
     * The order in which the individual puts occur is undefined.
     * <p>
     * The behavior of this operation is undefined if entries in the cache
     * corresponding to entries in the map are modified or removed while this
     * operation is in progress. or if map is modified while the operation is in
     * progress.
     * <p>
     * In Default Consistency mode, individual puts occur atomically but not
     * the entire putAll.  Listeners may observe individual updates.
     *
     * @param map mappings to be stored in this cache
     * @throws NullPointerException  if map is null or if map contains null keys
     *                               or values.
     * @throws IllegalStateException if the cache is closed
     * @throws RuntimeException      if there is a problem doing the put.
     * @throws ClassCastException    if the implementation is configured to perform
     *                               runtime-type-checking, and the key or value
     *                               types are incompatible with those that have been
     *                               configured for the {@link Cache}
     */
    void putAll(Map<? extends K, ? extends V> map);

    //JSR-107 boolean putIfAbsent(K key, V value);

    /**
     * Remove this entry from the cache or do nothing if the entry is not in the cache
     * (cluster safe)
     * <p>
     * More formally, if this cache contains a mapping from key <tt>k</tt> to
     * value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is removed.
     * (The cache can contain at most one such mapping.)
     *
     * The cache will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key the unique key for a cached object
     * @return true if the item was removed from the cache, false otherwise
     */
    boolean remove(K key);

    //JSR-107 boolean remove(K key, V oldValue);
    //JSR-107 V getAndRemove(K key);
    //JSR-107 boolean replace(K key, V oldValue, V newValue);
    //JSR-107 boolean replace(K key, V value);
    //JSR-107 V getAndReplace(K key, V value);

    /**
     * Removes entries for the specified keys.
     * <p>
     * The order in which the individual entries are removed is undefined.
     * <p>
     * For every entry in the key set, the following are called:
     * <ul>
     *   <li>any registered CacheEventListener</li>
     *   <li>if the cache is a write-through cache, the CacheLoader</li>
     * </ul>
     * If the key set is empty, the CacheLoader is not called.
     *
     * @param keys the keys to remove
     * @throws NullPointerException  if keys is null or if it contains a null key
     * @throws IllegalStateException if the cache is closed
     * @throws RuntimeException      if there is a problem during the remove
     * @throws ClassCastException    if the implementation is configured to perform
     *                               runtime-type-checking, and the key or value
     *                               types are incompatible with those that have been
     *                               configured for the {@link Cache}
     */
    void removeAll(Set<? extends K> keys);

    /**
     * Removes all of the mappings from this cache.
     * <p>
     * The order that the individual entries are removed is undefined.
     * <p>
     * For every mapping that exists the following are called:
     * <ul>
     *   <li>any registered CacheEventListener</li>
     *   <li>if the cache is a write-through cache, the CacheLoader</li>
     * </ul>
     * If the key set is empty, the CacheLoader is not called.
     * <p>
     * This is potentially an expensive operation as listeners are invoked.
     * Use {@link #clear()} to avoid this.
     *
     * @throws IllegalStateException if the cache is closed
     * @throws RuntimeException      if there is a problem during the remove
     * @see #clear()
     */
    void removeAll();

    /**
     * Clear all entries from the cache (this effectively resets the cache),
     * without notifying listeners
     * (works like <b>clear</b> from JSR-107 spec)
     */
    void clear();

    /**
     * Provides a standard way to access the configuration of a cache using
     * JCache configuration or additional proprietary configuration.
     * <p>
     * The returned value must be immutable.
     * <p>
     * If the provider's implementation does not support the specified class,
     * the {@link IllegalArgumentException} is thrown.
     *
     * @return the implementation of {@link Configuration}
     * @throws IllegalArgumentException if the caching provider doesn't support configuration
     * JSR-107: <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz);
     */
    Configuration getConfiguration();

    /**
     * Return the name of the cache.
     *
     * @return the name of the cache.
     */
    String getName();

    //JSR-107 CacheManager getCacheManager();

    /**
     * Closing a {@link Cache} signals to the CacheManager that produced or
     * owns the {@link Cache} that it should no longer be managed. At this
     * point in time the CacheManager:
     * <ul>
     * <li>must close and release all resources being coordinated on behalf of the
     * Cache by the CacheManager.
     * </li>
     * <li>not return the name of the Cache when the CacheManager getCacheNames()
     * method is called</li>
     * </ul>
     * Once closed any attempt to use an operational method on a Cache will throw an
     * {@link IllegalStateException}.
     *
     * @throws SecurityException when the operation could not be performed
     *                           due to the current security settings
     */
    void close();

    //JSR-107 boolean isClosed();

    /**
     * Provides a standard way to access the underlying concrete caching
     * implementation to provide access to further, proprietary features.
     * <p>
     * If the provider's implementation does not support the specified class,
     * the {@link IllegalArgumentException} is thrown.
     *
     * @param clazz the proprietary class or interface of the underlying concrete
     *              cache. It is this type that is returned.
     * @return an instance of the underlying concrete cache
     * @throws IllegalArgumentException if the caching provider doesn't support
     *                                  the specified class.
     * @throws SecurityException        when the operation could not be performed
     *                                  due to the current security settings
     */
    <T> T unwrap(java.lang.Class<T> clazz);

    /**
     * Attach a cache event listener to the cache.<br/>
     * The event listener ({@link CacheEventListener}) is then notified of the cache contents changing events,
     * setting this to null will clear the event listener
     * (not cluster safe)
     *
     * @param cacheEventListener the object which implements {@link CacheEventListener}
     */
    void registerCacheEventListener(CacheEventListener cacheEventListener);
    //JSR-107 void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration);
    //JSR-107 void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration);

    //JSR-107 Iterator<Entry<K, V>> iterator();
    /* JSR-107 spec Entry
    interface Entry<K, V> {
        K getKey();
        V getValue();
        <T> T unwrap(Class<T> clazz);
    }*/

    /**
     * Allows access to the statistics for a cache
     *
     * @return statistics for this Cache (numbers may always be zeros)
     */
    CacheStatistics getCacheStatistics();

    // SAKAI specific (no JSR-107 analog)

    /**
     * Get the {@link java.util.Properties} for this Cache
     * (likely the ones used to create it). Changing this
     * will not change the Cache configuration.
     *
     * @param includeExpensiveDetails if true then details about the Cache which
     *                                are expensive to calculate or generate will
     *                                also be included (like stats type information),
     *                                leave this false to avoid slowing down the cache
     * @return the Properties for this Cache
     */
    Properties getProperties(boolean includeExpensiveDetails);

    /**
     * Return a description of this cache (typically stats info about it)<br/>
     * <b>WARNING:</b> This is costly and should not be called often
     *
     * @return a string which summarizes the state of the cache
     */
    String getDescription();

    /**
     * Attach a cache loader to this cache<br/>
     * The loader ({@link CacheLoader}) will put items in the cache as they are requested (if they can be found),
     * setting this to null will clear the loader
     * (not cluster safe)
     * <b>NOTE</b>: special method to allow self-populating for this cache
     *
     * @param cacheLoader the object which implements {@link CacheLoader}
     */
    void attachLoader(CacheLoader cacheLoader);

    /**
     * Allows a check to see if a cache is distributed or not.
     * This should be false for any local caches or caches which do not replicate or expire
     * entries when they are updated or removed.
     * This is necessary in Sakai to allow legacy cluster caching support through events
     * while also allowing for true distributed caches.
     *
     * @return true if this cache has distributed replication or expiration support, false otherwise
     */
    boolean isDistributed();

}
