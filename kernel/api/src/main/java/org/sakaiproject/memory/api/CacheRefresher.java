/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

import org.sakaiproject.event.api.Event;

/**
 * This defines a kind of listener method which will be called whenever a cache miss occurs.
 * In other words, if the cache is asked to retrieve an object by a key which does not exist
 * in the cache then this method will be called if defined for that cache. Then the returned
 * value will be returned from the lookup (or a null of no new value was found) and
 * also inserted into the cache (unless the value was null)<br/>
 * <b>WARNING:</b> This can make your cache misses very costly so you will want to be careful
 * about what you make this method actually do
 * <br/>
 * Similar to https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/integration/CacheLoader.java
 *
 * Original comment:<br/>
 * Utility API for classes that will refresh a cache entry when expired.
 *
 * @deprecated This class will be renamed to CacheLoader to align with JSR-107
 */
public interface CacheRefresher { // CacheLoader<K, V> {
    // TODO - rename this to CacheLoader or something like that -AZ

    /**
     * Attempt to retrieve a value for this key from the cache user when none can be found in the cache
     *
     * @param key
     *        The cache key whose value was not found in the cache
     * @param oldValue (ALWAYS NULL)
     *        The old expired value of the key.
     * @param event (ALWAYS NULL)
     *        The event which triggered this refresh.
     * @return a new value for use in the cache for this key or null if no value exists for this item
     * @deprecated this method will eventually drop the oldValue and event params and be replaced
     * by one with a signature like: Object refresh(Object key); 07/Oct/2007
     */
    public Object refresh(Object key, Object oldValue, Event event);

    // JSR-107 CacheLoader methods below
    /**
     * Loads an object. Application developers should implement this
     * method to customize the loading of a value for a cache entry. This method
     * is called by a cache when a requested entry is not in the cache. If
     * the object can't be loaded <code>null</code> should be returned.
     *
     * @param key the key identifying the object being loaded
     * @return The value for the entry that is to be stored in the cache or
     *         <code>null</code> if the object can't be loaded
     * @throws CacheLoaderException if there is problem executing the loader.
     */
    //V load(K key) throws CacheLoaderException;
    /**
     * Loads multiple objects. Application developers should implement this
     * method to customize the loading of cache entries. This method is called
     * when the requested object is not in the cache. If an object can't be loaded,
     * it is not returned in the resulting map.
     *
     * @param keys keys identifying the values to be loaded
     * @return A map of key, values to be stored in the cache.
     * @throws CacheLoaderException if there is problem executing the loader.
     */
    //Map<K, V> loadAll(Iterable<? extends K> keys) throws CacheLoaderException;

}
