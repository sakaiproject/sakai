/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
public class MemCache implements Cache, Runnable, Observer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(MemCache.class);

	/** Map holding cached entries. */
	protected Map m_map = null;

	/** The object that will deal with expired entries. */
	protected CacheRefresher m_refresher = null;

	/** The string that all resources in this cache will start with. */
	protected String m_resourcePattern = null;

	/** The number of seconds to sleep between expiration checks. */
	protected long m_refresherSleep = 60;

	/** If true, we are disabled. */
	protected boolean m_disabled = false;

	/** If true, we have all the entries that there are in the cache. */
	protected boolean m_complete = false;

	/** Alternate isComplete, based on patterns. */
	protected Set m_partiallyComplete = new HashSet();

	/** If true, we are going to hold any events we see in the m_heldEvents list for later processing. */
	protected boolean m_holdEventProcessing = false;

	/** The events we are holding for later processing. */
	protected List m_heldEvents = new Vector();

	/** Constructor injected memory service. */
	protected BasicMemoryService m_memoryService = null;

	/** Constructor injected event tracking service. */
	protected EventTrackingService m_eventTrackingService = null;

	/** If true, we do soft references, else we do hard ones. */
	protected boolean m_softRefs = true;

	/** Count of access requests. */
	protected long m_getCount = 0;

	/** Count of access requests satisfied with a cached entry. */
	protected long m_hitCount = 0;

	/** Count of things put into the cache. */
	protected long m_putCount = 0;

	/** My (optional) DerivedCache. */
	protected DerivedCache m_derivedCache = null;

	/**
	 * The cache entry. Holds a time stamped payload.
	 */
	protected class CacheEntry extends SoftReference
	{
		/** currentTimeMillis when this expires. */
		protected long m_expires = 0;

		/** The time (seconds) to keep this cached (0 means don't exipre). */
		protected int m_duration = 0;

		/** Set if our payload is supposed to be null. */
		protected boolean m_nullPayload = false;

		/** Hard reference to the payload, if needed. */
		protected Object m_hardPayload = null;

		/**
		 * Construct to cache the payload for the duration.
		 * 
		 * @param payload
		 *        The thing to cache.
		 * @param duration
		 *        The time (seconds) to keep this cached.
		 */
		public CacheEntry(Object payload, int duration)
		{
			// put the payload into the soft reference
			super(payload);

			// if we are not doing soft refs, make the hard ref and clear the soft
			if (!m_softRefs)
			{
				this.clear();
				m_hardPayload = payload;
			}

			// is it supposed to be null?
			m_nullPayload = (payload == null);

			m_duration = duration;
			reset();

		} // CacheEntry

		/**
		 * Access the hard payload directly.
		 * @return The hard payload.
		 */
		public Object getHardPayload()
		{
			return m_hardPayload;
		}

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

			// for our hard, not soft, option
			if (!m_softRefs)
			{
				return m_hardPayload;
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
					put(key, payload, m_duration);
				}
				else
				{
					if (m_memoryService.getCacheLogging())
					{
						M_log.info("cache miss: no refresh: key: " + key);
					}
				}
			}

			// TODO: stash hard ref in the current LRU...

			return payload;
		}

		/**
		 * Check for expiration.
		 * 
		 * @return true if expired, false if still good.
		 */
		public boolean hasExpired()
		{
			return ((m_duration > 0) ? (System.currentTimeMillis() > m_expires) : false);
		}

		/**
		 * Access the duration.
		 * 
		 * @return The time (seconds) before the entry expires.
		 */
		public int getDuration()
		{
			return m_duration;
		}

		/**
		 * If we have a duration, reset our expiration time.
		 */
		public void reset()
		{
			if (m_duration > 0) m_expires = System.currentTimeMillis() + (m_duration * 1000);
		}

	} // CacheEntry

	/**
	 * Construct the Cache. No automatic refresh handling.
	 */
	public MemCache(BasicMemoryService memoryService, EventTrackingService eventTrackingService)
	{
		// inject our dependencies
		m_memoryService = memoryService;
		m_eventTrackingService = eventTrackingService;

		m_map = new ConcurrentHashMap();

		// register as a cacher
		m_memoryService.registerCacher(this);
	}

	/**
	 * Construct the Cache. Attempts to keep complete on Event notification by calling the refresher.
	 * 
	 * @param refresher
	 *        The object that will handle refreshing of event notified modified or added entries.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */
	public MemCache(BasicMemoryService memoryService, EventTrackingService eventTrackingService, CacheRefresher refresher,
			String pattern)
	{
		this(memoryService, eventTrackingService);
		m_refresher = refresher;
		m_resourcePattern = pattern;

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
	 */
	public MemCache(BasicMemoryService memoryService, EventTrackingService eventTrackingService, CacheRefresher refresher,
			long sleep)
	{
		this(memoryService, eventTrackingService);
		m_refresherSleep = sleep;

		if (refresher != null)
		{
			m_refresher = refresher;

			// start the expiration thread
			start();
		}
	}

	/**
	 * Construct the Cache. Event scanning if pattern not null - will expire entries.
	 * 
	 * @param sleep
	 *        The number of seconds to sleep between expiration checks.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for expiration.
	 */
	public MemCache(BasicMemoryService memoryService, EventTrackingService eventTrackingService, long sleep, String pattern)
	{
		this(memoryService, eventTrackingService);
		m_refresherSleep = sleep;
		m_resourcePattern = pattern;

		// start the expiration thread
		start();

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
		clear();

		// if we are not in a global shutdown
		if (!ComponentManager.hasBeenClosed())
		{
			// remove my registration
			m_memoryService.unregisterCacher(this);
	
			// remove my event notification registration
			m_eventTrackingService.deleteObserver(this);
		}

		// stop our expiration thread (if any)
		stop();
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
	 */
	public void put(Object key, Object payload, int duration)
	{
		if (disabled()) return;

		m_map.put(key, new CacheEntry(payload, duration));

		m_putCount++;

		if (m_derivedCache != null) m_derivedCache.notifyCachePut(key, payload);
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
		put(key, payload, 0);
	}

	/**
	 * Test for an entry in the cache - expired or not.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a cache entry, false if not.
	 */
	public boolean containsKeyExpiredOrNot(Object key)
	{
		if (disabled()) return false;

		// is it there?
		boolean rv = m_map.containsKey(key);

		m_getCount++;
		if (rv)
		{
			m_hitCount++;
		}

		return rv;

	} // containsKeyExpiredOrNot

	/**
	 * Test for a non expired entry in the cache.
	 * 
	 * @param key
	 *        The cache key.
	 * @return true if the key maps to a non-expired cache entry, false if not.
	 */
	public boolean containsKey(Object key)
	{
		if (disabled()) return false;

		m_getCount++;

		// is it there?
		CacheEntry entry = (CacheEntry) m_map.get(key);
		if (entry != null)
		{
			// has it expired?
			if (entry.hasExpired())
			{
				// if so, remove it
				remove(key);
				return false;
			}
			m_hitCount++;
			return true;
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
	 */
	public Object getExpiredOrNot(Object key)
	{
		if (disabled()) return null;

		// is it there?
		CacheEntry entry = (CacheEntry) m_map.get(key);
		if (entry != null)
		{
			return entry.getPayload(key);
		}

		return null;

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
		if (disabled()) return null;

		// get it if there
		CacheEntry entry = (CacheEntry) m_map.get(key);
		if (entry != null)
		{
			// has it expired?
			if (entry.hasExpired())
			{
				// if so, remove it
				remove(key);
				return null;
			}
			return entry.getPayload(key);
		}

		return null;

	} // get

	/**
	 * Get all the non-expired non-null entries.
	 * 
	 * @return all the non-expired non-null entries, or an empty list if none.
	 */
	public List getAll()
	{
		List rv = new Vector();

		if (disabled()) return rv;
		if (m_map.isEmpty()) return rv;

		// for each entry in the cache
		for (Iterator iKeys = m_map.entrySet().iterator(); iKeys.hasNext();)
		{
			Map.Entry e = (Map.Entry) iKeys.next();
			CacheEntry entry = (CacheEntry) e.getValue();

			// skip expired
			if (entry.hasExpired()) continue;

			Object payload = entry.getPayload(e.getKey());

			// skip nulls
			if (payload == null) continue;

			// skip inappropriate types
			// if ((type != null) && (!(type.isInstance(payload)))) continue;

			// filter out those not matching the filter
			// if ((filter != null) && (((String) keys[i]).startsWith(filter))) continue;

			// we'll take it
			rv.add(payload);
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
		List rv = new Vector();

		if (disabled()) return rv;
		if (m_map.isEmpty()) return rv;

		// for each entry in the cache
		for (Iterator iKeys = m_map.entrySet().iterator(); iKeys.hasNext();)
		{
			Map.Entry e = (Map.Entry) iKeys.next();
			CacheEntry entry = (CacheEntry) e.getValue();

			// skip expired
			if (entry.hasExpired()) continue;

			Object payload = entry.getPayload(e.getKey());

			// skip nulls
			if (payload == null) continue;

			// take only if keys start with path, and have no SEPARATOR following other than at the end %%%
			String keyPath = referencePath((String) e.getKey());
			if (!keyPath.equals(path)) continue;

			// we'll take it
			rv.add(payload);
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
		List rv = new Vector();
		rv.addAll(m_map.keySet());
		return rv;

	} // getKeys

	/**
	 * Get all the keys, eache modified to remove the resourcePattern prefix. Note: only works with String keys.
	 * 
	 * @return The List of keys converted from references to ids (String).
	 */
	public List getIds()
	{
		List rv = new Vector();

		for (Iterator it = m_map.keySet().iterator(); it.hasNext();)
		{
			String key = (String) it.next();
			int i = key.indexOf(m_resourcePattern);
			if (i != -1) key = key.substring(i + m_resourcePattern.length());
			rv.add(key);
		}

		return rv;

	} // getKeys

	/**
	 * Clear all entries.
	 */
	public void clear()
	{
		m_map.clear();
		m_complete = false;
		m_partiallyComplete.clear();
		m_getCount = 0;
		m_hitCount = 0;
		m_putCount = 0;

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
		if (disabled()) return;

		CacheEntry entry = (CacheEntry) m_map.remove(key);
		
		if (m_derivedCache != null)
		{
			Object old = null;
			if (entry != null)
			{
				old = entry.getHardPayload();
			}

			m_derivedCache.notifyCacheRemove(key, old);
		}

	} // remove

	/**
	 * Disable the cache.
	 */
	public void disable()
	{
		m_disabled = true;
		m_eventTrackingService.deleteObserver(this);
		clear();

	} // disable

	/**
	 * Enable the cache.
	 */
	public void enable()
	{
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
		return m_disabled;

	} // disabled

	/**
	 * Are we complete?
	 * 
	 * @return true if we have all the possible entries cached, false if not.
	 */
	public boolean isComplete()
	{
		if (disabled()) return false;

		return m_complete;

	} // isComplete

	/**
	 * Set the cache to be complete, containing all possible entries.
	 */
	public void setComplete()
	{
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
		m_partiallyComplete.add(path);

	} // setComplete

	/**
	 * Set the cache to hold events for later processing to assure an atomic "complete" load.
	 */
	public void holdEvents()
	{
		m_holdEventProcessing = true;

	} // holdEvents

	/**
	 * Restore normal event processing in the cache, and process any held events now.
	 */
	public void processEvents()
	{
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
		clear();

	} // resetCache

	/**
	 * Return the size of the cacher - indicating how much memory in use.
	 * 
	 * @return The size of the cacher.
	 */
	public long getSize()
	{
		return m_map.size();
	}

	/**
	 * Return a description of the cacher.
	 * 
	 * @return The cacher's description.
	 */
	public String getDescription()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("MemCache");
		if (m_softRefs)
		{
			buf.append(" soft");
		}
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
		if (m_refresher != null)
		{
			buf.append(" " + m_refresher.toString());
		}
		if (m_thread != null)
		{
			buf.append(" thread_sleep: " + m_refresherSleep);
		}
		if (m_partiallyComplete.size() > 0)
		{
			buf.append(" partially_complete[");
			for (Iterator i = m_partiallyComplete.iterator(); i.hasNext();)
			{
				buf.append(" " + i.next());
			}
			buf.append("]");
		}

		buf.append("  puts:" + m_putCount + "  gets:" + m_getCount + "  hits:" + m_hitCount + "  hit%:"
				+ ((m_getCount > 0) ? "" + ((100l * m_hitCount) / m_getCount) : "n/a"));

		return buf.toString();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Runnable implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The thread which runs the expiration check. */
	protected Thread m_thread = null;

	/** My thread's quit flag. */
	protected boolean m_threadStop = false;

	/**
	 * Start the expiration thread.
	 */
	protected void start()
	{
		m_threadStop = false;

		m_thread = new Thread(this, getClass().getName());
		m_thread.setDaemon(true);
		m_thread.setPriority(Thread.MIN_PRIORITY + 2);
		m_thread.start();

	} // start

	/**
	 * Stop the expiration thread.
	 */
	protected void stop()
	{
		if (m_thread == null) return;

		// signal the thread to stop
		m_threadStop = true;

		// wake up the thread
		m_thread.interrupt();

		m_thread = null;

	} // stop

	/**
	 * Run the expiration thread.
	 */
	public void run()
	{
		// since we might be running while the component manager is still being created and populated, such as at server
		// startup, wait here for a complete component manager
		ComponentManager.waitTillConfigured();

		// loop till told to stop
		while ((!m_threadStop) && (!Thread.currentThread().isInterrupted()))
		{
			long startTime = 0;
			try
			{
				if (M_log.isDebugEnabled())
				{
					startTime = System.currentTimeMillis();
					M_log.debug(this + ".checking ...");
				}

				// collect keys of expired entries in the cache
				List expired = new Vector();
				for (Iterator iKeys = m_map.entrySet().iterator(); iKeys.hasNext();)
				{
					Map.Entry e = (Map.Entry) iKeys.next();
					String key = (String) e.getKey();
					CacheEntry entry = (CacheEntry) e.getValue();

					// if it has expired
					if (entry.hasExpired())
					{
						expired.add(key);
					}
				}

				// if we have a refresher, for each expired, try to refresh
				if (m_refresher != null)
				{
					for (Iterator iKeys = expired.iterator(); iKeys.hasNext();)
					{
						String key = (String) iKeys.next();
						CacheEntry entry = (CacheEntry) m_map.get(key);
						if (entry != null)
						{
							Object newValue = m_refresher.refresh(key, entry.getPayload(null), null);

							remove(key);

							// if the response is not null, replace and rejuvinate
							if (newValue != null)
							{
								put(key, newValue, entry.getDuration());
							}
						}
					}
				}

				// if no refresher, for each expired, remove
				else
				{
					for (Iterator iKeys = expired.iterator(); iKeys.hasNext();)
					{
						String key = (String) iKeys.next();
						remove(key);
					}
				}
			}
			catch (Throwable e)
			{
				M_log.warn(this + ": exception: ", e);
			}

			if (M_log.isDebugEnabled())
			{
				M_log.debug(this + ".done. Time: " + (System.currentTimeMillis() - startTime));
			}

			// take a small nap
			try
			{
				Thread.sleep(m_refresherSleep * 1000);
			}
			catch (Exception ignore)
			{
			}

		} // while

	} // run

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
			M_log.debug(this + ".update() [" + m_resourcePattern + "] resource: " + key + " event: " + event.getEvent());

		// do we have this in our cache?
		Object oldValue = get(key);
		if (m_map.containsKey(key))
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
