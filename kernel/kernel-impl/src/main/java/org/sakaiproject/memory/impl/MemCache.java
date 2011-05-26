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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.impl;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;

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
import org.sakaiproject.memory.api.DerivedCache;

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

	/** If true, we are disabled. */
	protected boolean m_disabled = false;

	/** If true, we have all the entries that there are in the cache. */
	protected boolean m_complete = false;

	/** Alternate isComplete, based on patterns. */
	protected Set<String> m_partiallyComplete = new HashSet<String>();

	/** If true, we are going to hold any events we see in the m_heldEvents list for later processing. */
	protected boolean m_holdEventProcessing = false;

	/** The events we are holding for later processing. */
	protected List<Event> m_heldEvents = new Vector<Event>();

	/** Constructor injected memory service. */
	protected BasicMemoryService m_memoryService = null;

	/** Constructor injected event tracking service. */
	protected EventTrackingService m_eventTrackingService = null;

	/** My (optional) DerivedCache. */
	protected DerivedCache m_derivedCache = null;

	/**
	 * The cache entry. Holds a time stamped payload.
	 */
	protected class CacheEntry extends SoftReference
	{
		/** Set if our payload is supposed to be null. */
		protected boolean m_nullPayload = false;

		/**
		 * Construct to cache the payload for the duration.
		 * 
		 * @param payload
		 *        The thing to cache.
		 * @param duration
		 *        The time (seconds) to keep this cached.
		 * @deprecated
		 */
		public CacheEntry(Object payload, int duration)
		{
			// put the payload into the soft reference
			super(payload);

			// is it supposed to be null?
			m_nullPayload = (payload == null);

		} // CacheEntry

		/**
		 * Get the cached object.
		 * 
		 * @param key
		 *        The key for this entry (if null, we won't try to refresh if missing)
		 * @return The cached object.
		 */
		public Object getPayload(Object key)
		{
			// if we hold null, this is easy
			if (m_nullPayload)
			{
				return null;
			}

			// get the payload
			Object payload = this.get();

			// if it has been garbage collected, and we can, refresh it
			if (payload == null)
			{
				if ((m_refresher != null) && (key != null))
				{
					// ask the refresher for the value
					payload = m_refresher.refresh(key, null, null);

					if (m_memoryService.getCacheLogging())
					{
						M_log.info("cache miss: refreshing: key: " + key + " new payload: " + payload);
					}

					// store this new value
					put(key, payload);
				}
				else
				{
					if (m_memoryService.getCacheLogging())
					{
						M_log.info("cache miss: no refresh: key: " + key);
					}
				}
			}

			return payload;
		}

	} // CacheEntry

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
		if (pattern != null)
		{
			m_eventTrackingService.addPriorityObserver(this);
		}
	}

	/**
	 * Construct the Cache. Automatic refresh handling if refresher is not null.
	 * 
	 * @param refresher
	 *        The object that will handle refreshing of expired entries.
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @deprecated long sleep no longer used with ehcache
	 */
	public MemCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService,
			CacheRefresher refresher, long sleep, Ehcache cache)
	{
		this(memoryService, eventTrackingService, cache);
		if (refresher != null)
		{
			m_refresher = refresher;
		}
	}

	/**
	 * Construct the Cache. Automatic refresh handling if refresher is not null.
	 * 
	 * @param refresher
	 *        The object that will handle refreshing of expired entries.
	 */
	public MemCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService,
			CacheRefresher refresher, Ehcache cache)
	{
		this(memoryService, eventTrackingService, cache);
		if (refresher != null)
		{
			m_refresher = refresher;
		}
	}

	/**
	 * Construct the Cache. Event scanning if pattern not null - will expire entries.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for expiration.
	 * @deprecated long sleep no longer used with ehcache
	 */
	public MemCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, long sleep,
			String pattern, Ehcache cache)
	{
		this(memoryService, eventTrackingService, pattern, cache);
	}

	/**
	 * Construct the Cache. Event scanning if pattern not null - will expire entries.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for expiration.
	 */
	public MemCache(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, String pattern,
			Ehcache cache)
	{
		this(memoryService, eventTrackingService, cache);
		m_resourcePattern = pattern;

		// register to get events - first, before others
		if (pattern != null)
		{
			m_eventTrackingService.addPriorityObserver(this);
		}
	}

	/**
	 * Clean up.
	 */
	public void destroy()
	{
		cache.removeAll();  //TODO Do we boolean doNotNotifyCacheReplicators? Ian?
		cache.getStatistics().clearStatistics();

		// if we are not in a global shutdown
		if (!ComponentManager.hasBeenClosed())
		{
			// remove my event notification registration
			m_eventTrackingService.deleteObserver(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void attachDerivedCache(DerivedCache cache)
	{
		// Note: only one is supported
		if (cache == null)
		{
			m_derivedCache = null;
		}
		else
		{
			if (m_derivedCache != null)
			{
				M_log.warn("attachDerivedCache - already got one!");
			}
			else
			{
				m_derivedCache = cache;
			}
			
			// If has the (ehcache) EventCacheListener marker interface then
			// also attach the cache as a listener that implements the
			// ehcache event listener interface.
			
			if (cache instanceof CacheEventListener) {
				// add ehcahe event listener
				Ehcache ehc = this.cache;
				ehc.getCacheEventNotificationService().registerListener((CacheEventListener)cache);
			}
		}
	}

	/**
	 * Cache an object
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 * @deprecated 
	 */
	public void put(Object key, Object payload, int duration)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("put(Object " + key + ", Object " + payload + ", int "
					+ duration + ")");
		}
		put(key, payload);
	}

	/**
	 * Cache an object - don't automatically exipire it.
	 * 
	 * @param key
	 *        The key with which to find the object.
	 * @param payload
	 *        The object to cache.
	 * @param duration
	 *        The time to cache the object (seconds).
	 */
	public void put(Object key, Object payload)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("put(Object " + key + ", Object " + payload + ")");
		}

		if (disabled()) return;
		
		cache.put(new Element(key, payload));

		if (m_derivedCache != null) m_derivedCache.notifyCachePut(key, payload);
	}


	/**
	 * Test for an entry in the cache - expired or not.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a cache entry, false if not.
	 * @deprecated
	 */
	public boolean containsKeyExpiredOrNot(Object key)
	{
		if ( disabled() ) {
			return false;
		}
		return cache.isKeyInCache(key);
	} // containsKeyExpiredOrNot

	/**
	 * Test for a non expired entry in the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a non-expired cache entry, false if not.
	 */
	public boolean containsKey(Object key) {
		if (M_log.isDebugEnabled()) 
		{
			M_log.debug("containsKey(Object " + key + ")");
		}
		if (disabled())
			return false;

		if ( cache.isKeyInCache(key) ) {
			return ( cache.get(key) != null );
		}
		return false;
	} // containsKey

	/**
	 * Expire this object.
	 * 
	 * @param key
	 *        The cache key.
	 */
	public void expire(Object key)
	{
		if (M_log.isDebugEnabled()) 
		{
			M_log.debug("expire(Object " + key + ")");
		}
		
		if (disabled()) return;

		// remove it
		remove(key);

	} // expire

	/**
	 * Get the entry, or null if not there (expired entries are returned, too).
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found. (Note: use containsKey() to remove this ambiguity).
	 * @deprecated
	 */
	public Object getExpiredOrNot(Object key)
	{
		if (M_log.isDebugEnabled())
		{
			M_log.debug("getExpiredOrNot(Object " + key + ")");
		}

		return get(key);

	} // getExpiredOrNot

	/**
	 * Get the non expired entry, or null if not there (or expired)
	 * 
	 * @param key
	 *        The cache key.
	 * @return The payload, or null if the payload is null, the key is not found, or the entry has expired (Note: use containsKey() to remove this ambiguity).
	 */
	public Object get(Object key)
	{
		if (M_log.isDebugEnabled())
		{
			M_log.debug("get(Object " + key + ")");
		}

		if (disabled()) return null;

		final Element e = cache.get(key);
		return(e != null ? e.getObjectValue() : null);

	} // get

	/**
	 * Get all the non-expired non-null entries.
	 * 
	 * @return all the non-expired non-null entries, or an empty list if none.
	 * @deprecated
	 */
	public List getAll()
	{ //TODO Why would you ever getAll objects from cache?
		M_log.debug("getAll()");
		
		if (disabled()) return Collections.emptyList();

		final List<Object> keys = cache.getKeysWithExpiryCheck();
		final List<Object> rv = new ArrayList<Object>(keys.size()); // return value
		for (Object key : keys) {
			final Object value = cache.get(key).getObjectValue();
			if (value != null)
				rv.add(value);
		}

		return rv;

	} // getAll

	/**
	 * Get all the non-expired non-null entries that are in the specified reference path. Note: only works with String keys.
	 * 
	 * @param path
	 *        The reference path.
	 * @return all the non-expired non-null entries, or an empty list if none.
	 */
	public List getAll(String path)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("getAll(String " + path + ")");
		}
		
		if (disabled()) return Collections.emptyList();
		
		final List<Object> keys = cache.getKeysWithExpiryCheck();
		final List<Object> rv = new ArrayList<Object>(keys.size()); // return value
		for (Object key : keys) {
			// take only if keys start with path, and have no SEPARATOR following other than at the end %%%
			if (key instanceof String && referencePath((String) key).equals(path)) {
				rv.add(cache.get(key).getObjectValue());
			}
		}
		return rv;

	} // getAll

	/**
	 * Get all the keys
	 * 
	 * @return The List of key values (Object).
	 */
	public List getKeys()
	{
		M_log.debug("getKeys()");
		
		return cache.getKeys();
		
	} // getKeys

	/**
	 * Get all the keys, each modified to remove the resourcePattern prefix. Note: only works with String keys.
	 * 
	 * @return The List of keys converted from references to ids (String).
	 */
	public List getIds() {
		M_log.debug("getIds()");

		if (disabled())
			return Collections.emptyList();

		final List<Object> keys = cache.getKeysWithExpiryCheck();
		final List<Object> rv = new ArrayList<Object>(keys.size()); // return
																	// value
		for (Object key : keys) {
			if (key instanceof String) {
				int i = ((String) key).indexOf(m_resourcePattern);
				if (i != -1)
					key = ((String) key).substring(i
							+ m_resourcePattern.length());
				rv.add(key);
			}
		}
		return rv;

	} // getIds

	/**
	 * Clear all entries.
	 */
	public void clear()
	{
		M_log.debug("clear()");

		cache.removeAll();
		cache.getStatistics().clearStatistics();

		if (m_derivedCache != null) m_derivedCache.notifyCacheClear();

	} // clear

	/**
	 * Remove this entry from the cache.
	 * 
	 * @param key
	 *        The cache key.
	 */
	public void remove(Object key)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("remove(Object " + key + ")");
		}

		if (disabled()) return;

		// We could get things wrong here.
		final Object value = get(key);
		boolean found = cache.remove(key);

		if (m_derivedCache != null)
		{
			Object old = null;
			if (found)
			{
				old = value;
			}

			m_derivedCache.notifyCacheRemove(key, old);
		}

	} // remove

	/**
	 * Disable the cache.
	 */
	public void disable()
	{
		M_log.debug("disable()");
		
		m_disabled = true;
		m_eventTrackingService.deleteObserver(this);
		clear();

	} // disable

	/**
	 * Enable the cache.
	 */
	public void enable()
	{
		M_log.debug("enable()");

		m_disabled = false;

		if (m_resourcePattern != null)
		{
			m_eventTrackingService.addPriorityObserver(this);
		}

	} // enable

	/**
	 * Is the cache disabled?
	 * 
	 * @return true if the cache is disabled, false if it is enabled.
	 */
	public boolean disabled()
	{
		M_log.debug("disabled()");

		return m_disabled;

	} // disabled

	/**
	 * Are we complete?
	 * 
	 * @return true if we have all the possible entries cached, false if not.
	 */
	public boolean isComplete()
	{
		M_log.debug("isComplete()");

		if (disabled()) return false;

		return m_complete;

	} // isComplete

	/**
	 * Set the cache to be complete, containing all possible entries.
	 */
	public void setComplete()
	{
		M_log.debug("setComplete()");

		if (disabled()) return;

		m_complete = true;

	} // isComplete

	/**
	 * Are we complete for one level of the reference hierarchy?
	 * 
	 * @param path
	 *        The reference to the completion level.
	 * @return true if we have all the possible entries cached, false if not.
	 */
	public boolean isComplete(String path)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("isComplete(String " + path + ")");
		}

		return m_partiallyComplete.contains(path);

	} // isComplete

	/**
	 * Set the cache to be complete for one level of the reference hierarchy.
	 * 
	 * @param path
	 *        The reference to the completion level.
	 */
	public void setComplete(String path)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("setComplete(String " + path + ")");
		}

		m_partiallyComplete.add(path);

	} // setComplete

	/**
	 * Set the cache to hold events for later processing to assure an atomic "complete" load.
	 */
	public void holdEvents()
	{
		M_log.debug("holdEvents()");
		
		m_holdEventProcessing = true;

	} // holdEvents

	/**
	 * Restore normal event processing in the cache, and process any held events now.
	 */
	public void processEvents()
	{
		M_log.debug("processEvents()");

		m_holdEventProcessing = false;

		for (int i = 0; i < m_heldEvents.size(); i++)
		{
			Event event = (Event) m_heldEvents.get(i);
			continueUpdate(event);
		}

		m_heldEvents.clear();

	} // holdEvents

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Cacher implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Clear out as much as possible anything cached; re-sync any cache that is needed to be kept.
	 */
	public void resetCache()
	{
		M_log.debug("resetCache()");
		
		clear();

	} // resetCache

	/**
	 * Return the size of the cacher - indicating how much memory in use.
	 * 
	 * @return The size of the cacher.
	 */
	public long getSize()
	{
		M_log.debug("getSize()");
		
		return cache.getStatistics().getObjectCount();
	}

	/**
	 * Return a description of the cacher.
	 * 
	 * @return The cacher's description.
	 */
	public String getDescription()
	{
		final StringBuilder buf = new StringBuilder();
		buf.append("MemCache");
		if (m_disabled)
		{
			buf.append(" disabled");
		}
		if (m_complete)
		{
			buf.append(" complete");
		}
		if (m_resourcePattern != null)
		{
			buf.append(" " + m_resourcePattern);
		}
		if (m_partiallyComplete.size() > 0)
		{
			buf.append(" partially_complete[");
			for (Object element : m_partiallyComplete) {
				buf.append(" " + element);
			}
			buf.append("]");
		}
		final long hits = cache.getStatistics().getCacheHits();
		final long misses = cache.getStatistics().getCacheMisses();
		final long total = hits + misses;
		buf.append("  hits:" + hits + "  misses:" + misses + "  hit%:"
				+ ((total > 0) ? "" + ((100l * hits) / total) : "n/a"));

		return buf.toString();
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
		if (disabled()) return;

		// arg is Event
		if (!(arg instanceof Event)) return;
		Event event = (Event) arg;

		// if this is just a read, not a modify event, we can ignore it
		if (!event.getModify()) return;

		String key = event.getResource();

		// if this resource is not in my pattern of resources, we can ignore it
		if (!key.startsWith(m_resourcePattern)) return;

		// if we are holding event processing
		if (m_holdEventProcessing)
		{
			m_heldEvents.add(event);
			return;
		}

		continueUpdate(event);

	} // update

	/**
	 * Complete the update, given an event that we know we need to act upon.
	 * 
	 * @param event
	 *        The event to process.
	 */
	protected void continueUpdate(Event event)
	{
		String key = event.getResource();

		if (M_log.isDebugEnabled())
			M_log.debug(this + ".update() [" + m_resourcePattern
					+ "] resource: " + key + " event: " + event.getEvent());

		// do we have this in our cache?
		Object oldValue = get(key);
		if (containsKey(key))
		{
			// invalidate our copy
			remove(key);
		}

		// if we are being complete, we need to get this cached.
		if (m_complete)
		{
			// we can only get it cached if we have a refresher
			if (m_refresher != null)
			{
				// ask the refresher for the value
				Object value = m_refresher.refresh(key, oldValue, event);
				if (value != null)
				{
					put(key, value);
				}
			}
			else
			{
				// we can no longer claim to be complete
				m_complete = false;
			}
		}

		// if we are partially complete
		else if (!m_partiallyComplete.isEmpty())
		{
			// what is the reference path that this key lives within?
			String path = referencePath(key);

			// if we are partially complete for this path
			if (m_partiallyComplete.contains(path))
			{
				// we can only get it cached if we have a refresher
				if (m_refresher != null)
				{
					// ask the refresher for the value
					Object value = m_refresher.refresh(key, oldValue, event);
					if (value != null)
					{
						put(key, value);
					}
				}
				else
				{
					// we can no longer claim to be complete for this path
					m_partiallyComplete.remove(path);
				}
			}
		}

	} // continueUpdate

	/**
	 * Compute the reference path (i.e. the container) for a given reference.
	 * 
	 * @param ref
	 *        The reference string.
	 * @return The reference root for the given reference.
	 */
	protected String referencePath(String ref)
	{
		String path = null;

		// Note: there may be a trailing separator
		int pos = ref.lastIndexOf("/", ref.length() - 2);

		// if no separators are found, place it even before the root!
		if (pos == -1)
		{
			path = "";
		}

		// use the string up to and including that last separator
		else
		{
			path = ref.substring(0, pos + 1);
		}

		return path;

	} // referencePath

}
