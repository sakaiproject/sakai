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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.CacheEventListener.CacheEntryEvent;
import org.sakaiproject.memory.api.CacheEventListener.EventType;

import java.util.ArrayList;

/**
 * Ehcache based implementation of a Cache.
 * Includes support for listener and loader (Uses the Ehcache CacheEventListener)
 * NOTE:
 * The Observer is used for automatic cache flushing when the item is updated (removes cache entries on match).
 */
public class EhcacheCache extends BasicMapCache implements CacheEventListener {
    final Log log = LogFactory.getLog(BasicMapCache.class);

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
    }

    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param cache                the ehcache that backs this Sakai cache
     * @param eventTrackingService Sakai ETS (relates to pattern use)
     * @param pattern              "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
     * TODO remove this
     * @deprecated use EhcacheCache(Ehcache cache) instead
     */
    public EhcacheCache(Ehcache cache, EventTrackingService eventTrackingService, String pattern) {
        //noinspection deprecation
        super(cache.getName(), eventTrackingService, pattern);
        this.cache = cache;
    }

    @Override
    public void put(String key, Object payload) {
        cache.put(new Element(key, payload));
    }

    @Override
    public boolean containsKey(String key) {
        if (cache.isKeyInCache(key)) {
            // commented out the old way because this mechanism is the recommended way, note that this returning true is no guarantee the data will be there and that should NOT be assumed by anyone using this method -AZ
            //return (cache.get(key) != null);
            return true;
        }
        return false;
    } // containsKey

    @Override
    public Object get(String key) {
        final Element element = cache.get(key);
        Object value;
        if (element == null) {
            if (loader != null) {
                // trigger the cache loader on cache miss
                try {
                    //noinspection deprecation
                    value = loader.refresh(key, null, null);
                } catch (Exception e1) {
                    value = null;
                    log.error("Cache loader failed trying to load (" + key + ") for cache (" + getName() + "), return value will be null:" + e1, e1);
                }
            } else {
                // convert to the null value when not found
                value = null;
            }
        } else {
            value = element.getObjectValue();
        }
        return value;
    } // get

    @Override
    public void clear() {
        cache.removeAll();
        cache.getStatistics().clearStatistics();
    } // clear

    @Override
    public String getName() {
        return this.cache.getName();
    }

    @Override
    public void close() {
        super.close();
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
    public boolean remove(String key) {
        //final Object value = get(key);
        boolean found = cache.remove(key);
        return found;
    } // remove

    @Override
    public String getDescription() {
        final StringBuilder buf = new StringBuilder();
        buf.append("Ehcache (").append(getName()).append(")");
        if (m_resourcePattern != null) {
            buf.append(" ").append(m_resourcePattern);
        }
        if (loader != null) {
            buf.append(" Loader");
        }
        if (cacheEventListener != null) {
            buf.append(" Listener");
        }
        final long hits = cache.getStatistics().getCacheHits();
        final long misses = cache.getStatistics().getCacheMisses();
        final long total = hits + misses;
        buf.append("  size:").append(cache.getStatistics().getObjectCount()).append("/").append(cache.getCacheConfiguration().getMaxEntriesLocalHeap())
                .append("  hits:").append(hits).append("  misses:").append(misses)
                .append("  hit%:").append((total > 0) ? "" + ((100l * hits) / total) : "n/a");

        return buf.toString();
    }


    /***************************************************************************************************************
     * CacheEventListener implementation
     */

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

}
