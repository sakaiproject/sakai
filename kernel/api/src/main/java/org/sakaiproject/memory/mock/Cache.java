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

import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.DerivedCache;
import org.sakaiproject.memory.api.GenericMultiRefCache;

import java.util.*;

/**
 * Mock Cache for use in testing
 * Partly functional (no listener/loader support)
 */
public class Cache implements GenericMultiRefCache, org.sakaiproject.memory.api.Cache {

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
    public Object get(String key) {
        return map.get(key);
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
    public void remove(String key) {
        map.remove(key);
    }

    // multi ref cache

    @Override
    public void put(String key, Object payload, String ref, Collection<String> dependRefs) {
        map.put(key, payload);
    }

    // Sakai items below

    @Override
    public void attachDerivedCache(DerivedCache arg0) {
    }

    /**
     * @deprecated REMOVE THIS
     */
    public void destroy() {
        close();
    }

    @Override
    public void attachLoader(CacheRefresher cacheLoader) {
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public void put(Object key, Object payload, int duration) {
        put((String)key, payload);
    }


    // CompletionCache methods below

    @Override
    public void disable() {

    }

    @Override
    public void enable() {

    }

    @Override
    public boolean disabled() {
        return false;
    }

}
