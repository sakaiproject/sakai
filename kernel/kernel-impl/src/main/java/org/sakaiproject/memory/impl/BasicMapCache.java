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

package org.sakaiproject.memory.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.memory.api.Configuration;

/**
 * Contains general common implementation info related to a cache.
 * No support for listener or cache loader.
 * Should only be used for general testing of POC.
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class BasicMapCache<K, V> extends BasicCache<K, V> {
    /**
     * Underlying cache implementation
     * Simple and naive basic implementation of caching... not meant to be used
     */
    Map<K, V> cache;

    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param name                 the name for this cache
     */
    public BasicMapCache(String name) {
        super(name);
        this.cache = new ConcurrentHashMap<>();
    }

    public BasicMapCache(String name, Map<? extends K, ? extends V> map) {
        super(name);
        //noinspection unchecked
        this.cache.putAll(map);
    }

    @Override
    public void put(K key, V payload) {
        cache.put(key, payload);
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    } // containsKey

    @Override
    public V get(K key) {
        return cache.get(key);
    } // get

    @Override
    public void clear() {
        cache.clear();
    } // clear

    @Override
    public Configuration getConfiguration() {
        throw new IllegalArgumentException("Configuration not supported for BasicMapCache");
    }

    @Override
    public void close() {
        clear();
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return (T) cache;
    }

    @Override
    public boolean remove(K key) {
        Object o = cache.remove(key);
        return (o != null);
    } // remove

    @Override
    public String getDescription() {
        return "BasicMap ("+getName()+"): mapSize="+cache.size();
    }

    @Override
    public void removeAll() {
        clear(); // OK because no listeners or loaders support
    }

}
