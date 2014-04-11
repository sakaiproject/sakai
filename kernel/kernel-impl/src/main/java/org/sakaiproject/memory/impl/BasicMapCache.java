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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheLoader;
import org.sakaiproject.memory.api.CacheStatistics;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains general common implementation info related to a cache.
 * No support for listener or cache loader.
 * NOTE:
 * The Observer is used for automatic cache flushing when the item is updated (removes cache entries on match).
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public abstract class BasicMapCache implements Cache, Observer {
    final Log log = LogFactory.getLog(BasicMapCache.class);
    /**
     * the name for this cache
     */
    protected String cacheName = "cache";
    /**
     * Optional object for dealing with cache events
     */
    protected org.sakaiproject.memory.api.CacheEventListener cacheEventListener = null;
    /**
     * Optional object that will deal with loading missing entries into the cache on get()
     */
    protected CacheLoader loader = null;
    /**
     * Optional string that handles expiration of resources in this cache based on keys starting with this pattern
     */
    protected String m_resourcePattern = null;

    // TODO remove the pattern handling code
    /**
     * Constructor injected event tracking service.
     */
    protected EventTrackingService m_eventTrackingService = null;
    /**
     * Underlying cache implementation
     * Simple and naive basic implementation of caching... not meant to be used
     */
    private Map<String, Object> cache;

    /**
     * Construct the Cache
     * Set the listeners and cache refreshers later
     *
     * @param name                 the name for this cache
     */
    public BasicMapCache(String name) {
        this.cache = new ConcurrentHashMap<String, Object>();
        this.cacheName = name;
    }

    /**
     * Construct the Cache. Attempts to keep complete on Event notification.
     * Set the listeners and cache refreshers later
     *
     * @param name                 the name for this cache
     * @param eventTrackingService Sakai ETS (relates to pattern use)
     * @param pattern              "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
     * TODO remove this
     * @deprecated use BasicMapCache(String name) instead
     */
    public BasicMapCache(String name, EventTrackingService eventTrackingService, String pattern) {
        this(name);
        // inject our dependencies
        m_eventTrackingService = eventTrackingService;
        m_resourcePattern = pattern;
        // register to get events - first, before others
        if (pattern != null && !"".equals(pattern)) {
            m_eventTrackingService.addPriorityObserver(this);
        }
    }

    @Override
    public void put(String key, Object payload) {
        cache.put(key, payload);
    }

    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    } // containsKey

    @Override
    public Object get(String key) {
        return cache.get(key);
    } // get

    @Override
    public void clear() {
        cache.clear();
    } // clear

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public void close() {
        clear();
        this.cache = null;
        // if we are not in a global shutdown
        if (!ComponentManager.hasBeenClosed()) {
            // remove my event notification registration
            m_eventTrackingService.deleteObserver(this);
        }
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return (T) cache;
    }

    @Override
    public void registerCacheEventListener(org.sakaiproject.memory.api.CacheEventListener cacheEventListener) {
        this.cacheEventListener = cacheEventListener;
    }

    @Override
    public boolean remove(String key) {
        Object o = cache.remove(key);
        return (o != null);
    } // remove

    @Override
    public String getDescription() {
        final StringBuilder buf = new StringBuilder();
        buf.append("BasicMap (").append(getName()).append(")");
        if (m_resourcePattern != null) {
            buf.append(" ").append(m_resourcePattern);
        }
        return buf.toString();
    }

    @Override
    public void attachLoader(CacheLoader cacheLoader) {
        this.loader = cacheLoader;
    }


    // TODO remove the pattern handling code
    /****************************************************************************************************************
     * Observer implementation
     ****************************************************************************************************************/

    /**
     * This method is called whenever any observed object is changed. We then filter out the ones
     * we don't care about and take action to remove the item from the cache.
     *
     * @param o   the observable object.
     * @param arg an argument passed to the <code>notifyObservers</code> method.
     */
    public void update(Observable o, Object arg) {
        // arg is Event
        if (!(arg instanceof Event)) return;
        Event event = (Event) arg;

        // if this is just a read, not a modify event, we can ignore it
        if (!event.getModify()) return;

        String key = event.getResource();
        // if this resource is not in my pattern of resources, we can ignore it
        if (!key.startsWith(m_resourcePattern)) return;

        if (log.isDebugEnabled())
            log.debug(this + ".update() [" + m_resourcePattern
                    + "] resource: " + key + " event: " + event.getEvent());

        // remove the entry if it exists in the cache
        remove(key);
    }


    // **************************************************************************
    // DEPRECATED methods - REMOVE THESE
    // **************************************************************************

    /**
     * @deprecated REMOVE THIS
     */
    public void destroy() {
        this.close();
    }

    /**
     * @deprecated REMOVE THIS
     */
    public void put(Object key, Object payload, int duration) {
        put((String) key, payload);
    }

}
