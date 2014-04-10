/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.memory.impl;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;

import java.util.Observable;
import java.util.Observer;

/**
 * <p>
 * A Cache of objects with keys with a limited lifespan.
 * </p>
 * <p>
 * When the object expires, the cache calls upon a CacheRefresher to update the key's value. The update is done in a separate thread.
 * </p>
 */
public class MemCache implements Cache, Observer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(MemCache.class);
	
	/** Underlying cache implementation */
	protected net.sf.ehcache.Ehcache cache;

	/** The object that will deal with expired entries. */
	protected CacheRefresher m_refresher = null;

	/** The string that all resources in this cache will start with. */
	protected String m_resourcePattern = null;

	/** Constructor injected memory service. */
	protected BasicMemoryService m_memoryService = null;

	/** Constructor injected event tracking service. */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Construct the Cache. No automatic refresh handling.
	 */
	public MemCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, Ehcache cache)
	{
		// inject our dependencies
		m_memoryService = memoryService;
		m_eventTrackingService = eventTrackingService;
		this.cache = cache;
	}

	/**
	 * Construct the Cache. Attempts to keep complete on Event notification by calling the refresher.
	 *
	 * @param refresher
	 *        The object that will handle refreshing of event notified modified or added entries.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */
	public MemCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService,
			CacheRefresher refresher, String pattern, Ehcache cache)
	{
		this(memoryService, eventTrackingService, cache);
		m_resourcePattern = pattern;
		if (refresher != null)
		{
			m_refresher = refresher;
		}

		// register to get events - first, before others
		if (pattern != null && !"".equals(pattern))
		{
			m_eventTrackingService.addPriorityObserver(this);
		}
	}

    /**
     * @deprecated REMOVE THIS
     */
	public void destroy()
	{
		this.close();
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void put(String key, Object payload)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("put(Object " + key + ", Object " + payload + ")");
		}

		cache.put(new Element(key, payload));
	}

    @Override
	public boolean containsKey(String key) {
		if (M_log.isDebugEnabled()) 
		{
			M_log.debug("containsKey(Object " + key + ")");
		}

		if ( cache.isKeyInCache(key) ) {
			return ( cache.get(key) != null );
		}
		return false;
	} // containsKey

    /**
     * {@inheritDoc}
     */
    @Override
	public Object get(String key)
	{
		if (M_log.isDebugEnabled())
		{
			M_log.debug("get(Object " + key + ")");
		}

		final Element e = cache.get(key);
		return(e != null ? e.getObjectValue() : null);
	} // get

    /**
     * {@inheritDoc}
     */
    @Override
	public void clear()
	{
		M_log.debug("clear()");
		cache.removeAll();
		cache.getStatistics().clearStatistics();
	} // clear

    @Override
    public String getName() {
        return this.cache.getName();
    }

    @Override
    public void close() {
        cache.removeAll();
        cache.getStatistics().clearStatistics();
        this.cache.dispose();
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
        // TODO implement this

        /* If has the (ehcache) EventCacheListener marker interface then
         * also attach the cache as a listener that implements the
         * ehcache event listener interface.
         */
        if (cache instanceof CacheEventListener) {
            // add ehcahe event listener
            Ehcache ehc = this.cache;
            ehc.getCacheEventNotificationService().registerListener((CacheEventListener)cache);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public boolean remove(String key)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("remove(Object " + key + ")");
		}

		// We could get things wrong here.
		final Object value = get(key);
		boolean found = cache.remove(key);
		return found;
	} // remove

    /**
     * {@inheritDoc}
     */
    @Override
	public String getDescription()
	{
		final StringBuilder buf = new StringBuilder();
		buf.append("MemCache (").append(getName()).append(")");
		if (m_resourcePattern != null)
		{
			buf.append(" ").append(m_resourcePattern);
		}
		if (m_refresher != null) {
			buf.append(" Refresher");
		}
		final long hits = cache.getStatistics().getCacheHits();
		final long misses = cache.getStatistics().getCacheMisses();
		final long total = hits + misses;
		buf.append("  size:").append(cache.getStatistics().getObjectCount()).append("/").append(cache.getCacheConfiguration().getMaxEntriesLocalHeap())
				.append("  hits:").append(hits).append("  misses:").append(misses)
				.append("  hit%:").append((total > 0) ? "" + ((100l * hits) / total) : "n/a");

		return buf.toString();
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachLoader(CacheRefresher cacheLoader) {
        this.m_refresher = cacheLoader;
    }


    /**********************************************************************************************************************************************************************************************************************************************************
	 * Observer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * This method is called whenever the observed object is changed. An application calls an <tt>Observable</tt> object's <code>notifyObservers</code> method to have all the object's observers notified of the change. default implementation is to
	 * cause the courier service to deliver to the interface controlled by my controller. Extensions can override.
	 * 
	 * @param o
	 *        the observable object.
	 * @param arg
	 *        an argument passed to the <code>notifyObservers</code> method.
	 */
	public void update(Observable o, Object arg)
	{
		// arg is Event
		if (!(arg instanceof Event)) return;
		Event event = (Event) arg;

		// if this is just a read, not a modify event, we can ignore it
		if (!event.getModify()) return;

		String key = event.getResource();
		// if this resource is not in my pattern of resources, we can ignore it
		if (!key.startsWith(m_resourcePattern)) return;

		if (M_log.isDebugEnabled())
			M_log.debug(this + ".update() [" + m_resourcePattern
					+ "] resource: " + key + " event: " + event.getEvent());

        // remove the entry if it exists in the cache
        remove(key);

	} // continueUpdate


    // **************************************************************************
    // DEPRECATED methods - REMOVE THESE
    // **************************************************************************

    @Override
    public void put(Object key, Object payload, int duration) {
        put((String)key, payload);
    }

}
