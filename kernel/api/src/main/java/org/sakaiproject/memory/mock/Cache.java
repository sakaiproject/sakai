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

import org.sakaiproject.memory.api.DerivedCache;
import org.sakaiproject.memory.api.GenericMultiRefCache;

import java.util.*;

/**
 * Mock Cache for use in testing
 * Partly functional
 */
public class Cache implements GenericMultiRefCache {

    private Map<Object, Object> map = new HashMap<Object, Object>();

    String name;
    Cache(String name) {
        this.name = name;
    }

    public void attachDerivedCache(DerivedCache arg0) {
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsKeyExpiredOrNot(Object key) {
        return containsKey(key);
    }

    public void destroy() {
        map = null;
    }

    public void disable() {
    }

    public boolean disabled() {
        return false;
    }

    public void enable() {
    }

    public void expire(Object o) {
        map.remove(o);
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public List getAll() {
        List<Object> all = new ArrayList<Object>();
        for(Object o : map.values()) {
            all.add(o);
        }
        return all;
    }

    public List getAll(String key) {
        List<Object> all = new ArrayList<Object>();
        all.add(get(key));
        return all;
    }

    public Object getExpiredOrNot(Object key) {
        return get(key);
    }

    public List getIds() {
        return getKeys();
    }

    public List getKeys() {
        List<Object> keys = new ArrayList<Object>();
        for(Object k : map.keySet()) {
            keys.add(k);
        }
        return keys;
    }

    public void holdEvents() {
    }

    public boolean isComplete() {
        return false;
    }

    public boolean isComplete(String arg0) {
        return false;
    }

    public void processEvents() {
    }

    public void put(Object key, Object object) {
        map.put(key, object);
    }

    public void put(Object key, Object object, int arg2) {
        map.put(key, object);
    }

    public void remove(Object key) {
        map.remove(key);
    }

    public void setComplete() {
    }

    public void setComplete(String arg0) {
    }

    public String getDescription() {
        return name;
    }

    public long getSize() {
        return map.size();
    }

    public void resetCache() {
        clear();
    }

    public void put(Object key, Object payload, String ref, Collection<String> dependRefs) {
        map.put(key, payload);
    }

}
