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

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.CacheEventListener;

import org.sakaiproject.memory.api.CacheEventListener.CacheEntryEvent;
import org.sakaiproject.memory.api.CacheEventListener.EventType;
import org.sakaiproject.memory.api.CacheStatistics;
import org.sakaiproject.memory.api.Configuration;

/**
 * Ehcache based implementation of a Cache.
 * Includes support for listener, loader (Uses the Ehcache CacheEventListener), and stats
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
@Slf4j
public class EhcacheCache<K, V> extends BasicCache<K, V> implements CacheEventListener {
    /**
     * Underlying cache implementation
     */
    protected Ehcache cache;

    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param cache the ehcache that backs this Sakai cache
     */
    public EhcacheCache(Ehcache cache) {
        super(cache.getName());
        this.cache = cache;
        // check if distributed
        if (cache.getCacheConfiguration() != null && cache.getCacheConfiguration().getTerracottaConfiguration() != null) {
            this.distributed = cache.getCacheConfiguration().getTerracottaConfiguration().isClustered();
        }
    }

    @Override
    public void put(K key, V payload) {
        cache.put(new Element(key, payload));
    }

    @Override
    public boolean containsKey(K key) {
        if (cache.isKeyInCache(key)) {
            // commented out the old way because this mechanism is the recommended way, note that this returning true is no guarantee the data will be there and that should NOT be assumed by anyone using this method -AZ
            //return (cache.get(key) != null);
            return true;
        }
        return false;
    } // containsKey

    @Override
    public V get(K key) {
        final Element element = cache.get(key);
        V value;
        if (element == null) {
            if (loader != null) {
                // trigger the cache loader on cache miss
                try {
                    //noinspection unchecked
                    value = (V) loader.load(key);
                } catch (Exception e1) {
                    value = null;
                    log.error("Cache loader failed trying to load (" + key + ") for cache (" + getName() + "), return value will be null:" + e1, e1);
                }
            } else {
                // convert to the null value when not found
                value = null;
            }
        } else {
            value = (V) element.getObjectValue();
            ArrayList<String> w = new ArrayList();
        }
        return value;
    } // get

    @Override
    public void clear() {
        cache.removeAll(false); // no listener triggers
        cache.getStatistics().clearStatistics();
    } // clear

    @Override
    public Configuration getConfiguration() {
        return new Configuration() {
            @Override
            public boolean isStatisticsEnabled() {
                return cache.isStatisticsEnabled();
            }

            @Override
            public long getMaxEntries() {
                return cache.getCacheConfiguration().getMaxEntriesLocalHeap();
            }

            @Override
            public long getTimeToLiveSeconds() {
                return cache.getCacheConfiguration().getTimeToLiveSeconds();
            }

            @Override
            public long getTimeToIdleSeconds() {
                return cache.getCacheConfiguration().getTimeToIdleSeconds();
            }

            @Override
            public boolean isEternal() {
                return cache.getCacheConfiguration().isEternal();
            }

            @Override
            public Properties getAll() {
                CacheConfiguration cc = cache.getCacheConfiguration();
                Properties p = new Properties();
                p.put("maxEntries", cc.getMaxEntriesLocalHeap());
                p.put("timeToLiveSeconds", cc.getTimeToLiveSeconds());
                p.put("timeToIdleSeconds", cc.getTimeToIdleSeconds());
                p.put("eternal", cc.isEternal());
                p.put("statisticsEnabled", cache.isStatisticsEnabled());
                return p;
            }
        };
    }

    @Override
    public String getName() {
        return this.cache.getName();
    }

    @Override
    public void close() {
        this.cache.dispose();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return (T) cache;
    }

    @Override
    public void registerCacheEventListener(org.sakaiproject.memory.api.CacheEventListener cacheEventListener) {
        super.registerCacheEventListener(cacheEventListener);
        if (cacheEventListener == null) {
            cache.getCacheEventNotificationService().unregisterListener(this);
        } else {
            cache.getCacheEventNotificationService().registerListener(this);
        }
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        if (this.cache == null) {
            throw new IllegalStateException("Cannot get stats, no cache exists");
        }
        return new EhcacheCacheStatistics(this.cache);
    }

    @Override
    public Properties getProperties(boolean includeExpensiveDetails) {
        Properties p = new Properties();
        p.put("name", cache.getName());
        p.put("class", this.getClass().getSimpleName());
        p.put("cacheClass", cache.getClass().getName());
        p.put("guid", cache.getGuid());
        p.put("disabled", cache.isDisabled());
        p.put("statsEnabled", cache.isStatisticsEnabled());
        p.put("status", cache.getStatus().toString());
        p.put("maxEntries", cache.getCacheConfiguration().getMaxEntriesLocalHeap());
        p.put("timeToLiveSecs", cache.getCacheConfiguration().getTimeToLiveSeconds());
        p.put("timeToIdleSecs", cache.getCacheConfiguration().getTimeToIdleSeconds());
        p.put("distributed", isDistributed());
        p.put("eternal", cache.getCacheConfiguration().isEternal());
        if (includeExpensiveDetails) {
            p.put("size", cache.getSize());
            p.put("avgGetTime", cache.getStatistics().getAverageGetTime());
            p.put("hits", cache.getStatistics().getCacheHits());
            p.put("misses", cache.getStatistics().getCacheMisses());
            p.put("evictions", cache.getStatistics().getEvictionCount());
            p.put("count", cache.getStatistics().getMemoryStoreObjectCount());
            p.put("searchPerSec", cache.getStatistics().getSearchesPerSecond());
        }
        return p;
    }

    @Override
    public boolean remove(K key) {
        //final Object value = get(key);
        boolean found = cache.remove(key);
        return found;
    } // remove

    @Override
    public String getDescription() {
        final StringBuilder buf = new StringBuilder();
        buf.append(cache.getName()).append(" Ehcache");
        if (loader != null) {
            buf.append(" Loader");
        }
        if (cacheEventListener != null) {
            buf.append(" Listener");
        }
        if (isDistributed()) {
            buf.append(" Distributed");
        }
        final long hits = cache.getStatistics().getCacheHits();
        final long misses = cache.getStatistics().getCacheMisses();
        final long total = hits + misses;
        final long hitRatio = ((total > 0) ? ((100l * hits) / total) : 0);
        // Even when we're not collecting statistics ehcache knows how many objects are in the cache
        buf.append(": ").append(" count:").append(cache.getStatistics().getObjectCount());
        if (cache.isStatisticsEnabled()) {
            buf.append(" hits:").append(hits).append(" misses:").append(misses).append(" hit%:").append(hitRatio);
        } else {
            buf.append(" NO statistics (not enabled for cache)");
        }
        return buf.toString();
    }

    // BULK operations - KNL-1246

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        HashMap<K, V> map = new HashMap<>();
        if (!keys.isEmpty()) {
            Map<Object, Element> mapElements = cache.getAll(keys);
            for (Map.Entry<Object, Element> entry : mapElements.entrySet()) {
                map.put((K)entry.getKey(), (V)entry.getValue().getObjectValue());
            }
        }
        return map;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map != null && !map.isEmpty()) {
            HashSet<Element> elements = new HashSet<Element>(map.size());
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    elements.add( new Element(entry.getKey(), entry.getValue()) );
                }
            }
            if (!elements.isEmpty()) {
                cache.putAll(elements);
            }
        }
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        if (!keys.isEmpty()) {
            cache.removeAll(keys);
        }
    }

    @Override
    public void removeAll() {
        cache.removeAll();
    }

    /**
     * Simply reducing code duplication
     *
     * @param eventType the event type
     * @param element   the cache element
     * @return a list of CacheEntryEvent objects (always with one entry)
     */
    private ArrayList<CacheEntryEvent> makeCacheEntryEvents(EventType eventType, Element element) {
        CacheEntryEvent<?, ?> cee = new CacheEntryEvent<String, Object>(this, element.getObjectKey().toString(), element.getObjectValue(), eventType);
        //noinspection unchecked
        this.cacheEventListener.evaluate(cee);
        ArrayList<CacheEntryEvent> events = new ArrayList<CacheEntryEvent>(1);
        events.add(cee);
        return events;
    }


    /***************************************************************************************************************
     * Ehcache CacheEventListener implementation
     */

    @Override
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        if (this.cacheEventListener != null) {
            ArrayList<CacheEntryEvent> events = makeCacheEntryEvents(EventType.REMOVED, element);
            //noinspection unchecked
            this.cacheEventListener.onRemoved(events);
        }
    }

    @Override
    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        if (this.cacheEventListener != null) {
            ArrayList<CacheEntryEvent> events = makeCacheEntryEvents(EventType.CREATED, element);
            //noinspection unchecked
            this.cacheEventListener.onCreated(events);
        }
    }

    @Override
    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        if (this.cacheEventListener != null) {
            ArrayList<CacheEntryEvent> events = makeCacheEntryEvents(EventType.UPDATED, element);
            //noinspection unchecked
            this.cacheEventListener.onUpdated(events);
        }
    }

    @Override
    public void notifyElementExpired(Ehcache ehcache, Element element) {
        if (this.cacheEventListener != null) {
            ArrayList<CacheEntryEvent> events = makeCacheEntryEvents(EventType.EXPIRED, element);
            //noinspection unchecked
            this.cacheEventListener.onExpired(events);
        }
    }

    @Override
    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        notifyElementExpired(ehcache, element);
    }

    @Override
    public void notifyRemoveAll(Ehcache ehcache) {
    } // NOT USED

    @Override
    public void dispose() {
    } // NOT USED

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        throw new CloneNotSupportedException("CacheEventListener implementations should throw CloneNotSupportedException if they do not support clone");
    }

    /**
     * Ehcache stats implementation
     */
    public static class EhcacheCacheStatistics implements CacheStatistics {
        final long hits;
        final long misses;

        public EhcacheCacheStatistics(Ehcache cache) {
            this.hits = cache.getStatistics().getCacheHits();
            this.misses = cache.getStatistics().getCacheMisses();
        }

        @Override
        public long getCacheHits() {
            return hits;
        }

        @Override
        public long getCacheMisses() {
            return misses;
        }
    }

}
