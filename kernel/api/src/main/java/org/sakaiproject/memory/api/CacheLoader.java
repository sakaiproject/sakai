/******************************************************************************
 * $URL$
 * $Id$
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.memory.api;

import java.util.Map;

/**
 * This defines a kind of listener method which will be called whenever a cache miss occurs.
 * In other words, if the cache is asked to retrieve an object by a key which does not exist
 * in the cache then this method will be called if defined for that cache. Then the returned
 * value will be returned from the lookup (or a null if no new value was found) and
 * also inserted into the cache (unless the value was null)<br/>
 * <b>WARNING:</b> This can make your cache misses very costly so you will want to be careful
 * about what you make this method actually do
 * <br/>
 * Similar to https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/integration/CacheLoader.java
 *
 * Aligns with JSR-107 CacheLoader
 * See https://jira.sakaiproject.org/browse/KNL-1162
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public interface CacheLoader<K, V> {
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
     * @throws java.lang.RuntimeException if there is problem executing the loader.
     */
    V load(K key);// throws CacheLoaderException;

    /**
     * Loads multiple objects. Application developers should implement this
     * method to customize the loading of cache entries. This method is called
     * when the requested object is not in the cache. If an object can't be loaded,
     * it is not returned in the resulting map.
     *
     * @param keys keys identifying the values to be loaded
     * @return A map of key, values to be stored in the cache.
     * @throws java.lang.RuntimeException if there is problem executing the loader.
     */
    Map<K, V> loadAll(Iterable<? extends K> keys);// throws CacheLoaderException;

}
