/**
 * Copyright (c) 2026 The Apereo Foundation
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
 */
package org.sakaiproject.lti.impl.testutil;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;

/**
 * Minimal in-memory {@link Cache} for unit tests.
 */
public class MapBackedCache implements Cache {

    private final String name;
    private final ConcurrentMap<Object, Object> store = new ConcurrentHashMap<>();

    public MapBackedCache(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return store;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (!store.containsKey(key)) {
            return null;
        }
        return () -> store.get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value = store.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        throw new UnsupportedOperationException("Not needed for tests");
    }

    @Override
    public void put(Object key, Object value) {
        store.put(key, value);
    }

    @Override
    public void evict(Object key) {
        store.remove(key);
    }

    @Override
    public void clear() {
        store.clear();
    }
}
