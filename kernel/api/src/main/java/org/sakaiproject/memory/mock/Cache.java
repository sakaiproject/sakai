/******************************************************************************
 * $URL: https://source.sakaiproject.org/svn/master/trunk/header.java $
 * $Id: header.java 307632 2014-03-31 15:29:37Z azeckoski@unicon.net $
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

package org.sakaiproject.memory.mock;

import org.sakaiproject.memory.api.*;

import java.util.*;

/**
 * Mock Cache for use in testing
 * Partly functional (no listener/loader/stats support)
 */
@SuppressWarnings("deprecation") // TODO remove GenericMultiRefCache
public class Cache implements org.sakaiproject.memory.api.Cache<String,Object> {

    String name;
    private Map<String, Object> map = new HashMap<String, Object>();
    Cache(String name) {
        this.name = name;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Configuration getConfiguration() {
        return new SimpleConfiguration(1000l);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        map = null;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return (T) map;
    }

    @Override
    public void registerCacheEventListener(CacheEventListener cacheEventListener) {}

    @Override
    public CacheStatistics getCacheStatistics() {
        return new CacheStatistics() {
            @Override
            public long getCacheHits() {
                return 0;
            }
            @Override
            public long getCacheMisses() {
                return 0;
            }
        };
    }

    @Override
    public Properties getProperties(boolean includeExpensiveDetails) {
        return new Properties();
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public Map<String, Object> getAll(Set<? extends String> keys) {
        Map<String, Object> m = new HashMap<String, Object>(this.map);
        for (String key : keys) {
            m.remove(key);
        }
        return m;
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public void put(String key, Object object) {
        map.put(key, object);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        this.map.putAll(map);
    }

    @Override
    public boolean remove(String key) {
        Object o = map.remove(key);
        return (o != null);
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
        for (String key : keys) {
            this.map.remove(key);
        }
    }

    @Override
    public void removeAll() {
        clear();
    }

    // Sakai items below

    @Override
    public void attachLoader(CacheLoader cacheLoader) {
    }

    @Override
    public boolean isDistributed() {
        return false;
    }

    @Override
    public String getDescription() {
        return name;
    }

}
