/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MultiRefCache;

/**
 * <p>
 * MultiRefCacheImpl implements the MultiRefCache.
 * </p>
 * <p>
 * The references that each cache entry are sensitive to are kept in a separate map for easy access.<br />
 * Manipulation of this map is synchronized. This map is not used for cache access, just when items are added and removed.<br />
 * The cache map itself becomes synchronized when it's manipulated (not when reads occur), so this added sync. for the refs fits the existing pattern.
 * </p>
 */
public class MultiRefCacheImpl extends MemCache implements MultiRefCache,
		CacheEventListener 
	{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(MultiRefCacheImpl.class);

	/** Map of reference string -> Collection of cache keys. */
	protected final Map<String, Map<Object, Object>> m_refsStore = new ConcurrentHashMap<String, Map<Object, Object>>();

	/** AuthzGroupService used when putting items into the cache. */
	private AuthzGroupService m_authzGroupService;

	protected class MultiRefCacheEntry extends CacheEntry
	{
		/** These are the entity reference strings that this entry is sensitive to. */
		protected List<Object> m_refs = new CopyOnWriteArrayList<Object>();

		/**
		 * Construct to cache the payload for the duration.
		 * 
		 * @param payload
		 *        The thing to cache.
		 * @param duration
		 *        The time (seconds) to keep this cached.
		 * @param ref
		 *        One entity reference that, if changed, will invalidate this entry.
		 * @param azgRefs
		 *        AuthzGroup refs that, if the changed, will invalidate this entry.
		 */
		public MultiRefCacheEntry(Object payload, int duration, String ref, Collection<Object> azgRefs)
		{
			super(payload, duration);
			if (ref != null) m_refs.add(ref);
			if (azgRefs != null) m_refs.addAll(azgRefs);
		}

		/**
		 * @inheritDoc
		 */
		public List<Object> getRefs()
		{
			return m_refs;
		}
	}

	/**
	 * Construct the Cache - checks for expiration periodically.
	 * @param authzGroupService TODO
	 * @deprecated long sleep no longer used with ehcache
	 */
	public MultiRefCacheImpl(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, AuthzGroupService authzGroupService, long sleep, Ehcache cache)
	{
		super(memoryService, eventTrackingService, sleep, "", cache);
		this.m_authzGroupService = authzGroupService;
		cache.getCacheEventNotificationService().registerListener(this);
		
	}

	/**
	 * Construct the Cache - checks for expiration periodically.
	 * @param authzGroupService TODO
	 */
	public MultiRefCacheImpl(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, AuthzGroupService authzGroupService, Ehcache cache)
	{
		super(memoryService, eventTrackingService, "", cache);
		this.m_authzGroupService = authzGroupService;
		cache.getCacheEventNotificationService().registerListener(this);

	}

	/**
	 * @inheritDoc
	 */
	public void put(Object key, Object payload, int duration, String ref, Collection azgIds)
	{
		if(M_log.isDebugEnabled())
		{
			M_log.debug("put(Object " + key + ", Object " + payload + ", int "
					+ duration + ", String " + ref + ", Collection " + azgIds
					+ ")");
		}
		if (disabled()) return;

		// make refs for any azg ids
		Collection azgRefs = null;
		if (azgIds != null)
		{
			azgRefs = new Vector(azgIds.size());
			for (Iterator i = azgIds.iterator(); i.hasNext();)
			{
				String azgId = (String) i.next();
				azgRefs.add(m_authzGroupService.authzGroupReference(azgId));
			}
		}

		// create our extended cache entry for the cache map - the reference strings are recorded
		super.put(key, new MultiRefCacheEntry(payload, duration, ref, azgRefs));

		if (ref != null)
		{
			addRefCachedKey(ref, key);
		}

		if (azgRefs != null)
		{
			for (Iterator i = azgRefs.iterator(); i.hasNext();)
			{
				String azgRef = (String) i.next();
				addRefCachedKey(azgRef, key);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void put(Object key, Object payload, int duration)
	{
		put(key, payload, duration, null, null);
	}

	/**
	 * @inheritDoc
	 */
	public void put(Object key, Object payload)
	{
		put(key, payload, 0, null, null);
	}

	/**
	 * Make sure there's an entry in refs for this ref that includes this key.
	 * 
	 * @param ref
	 *        The entity reference string.
	 * @param key
	 *        The cache entry key dependent on this entity ref.
	 */
	protected void addRefCachedKey(String ref, Object key)
	{
		Map<Object,Object> cachedKeys = m_refsStore.get(ref);
		// Isn't this a threading issue. Two thread hit this and both put their own data into m_refsStore.
		if ( cachedKeys == null ) {
			cachedKeys = new ConcurrentHashMap<Object, Object>();
			m_refsStore.put(ref, cachedKeys);
		}
		cachedKeys.put(key, key);
	}

	/**
	 * @inheritDoc
	 */
	public void clear()
	{
		super.clear();
		m_refsStore.clear();
	}

	private void cleanEntityReferences(Object key, Object value)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("maintainEntityReferences(Object " + key
					+ ", Object " + value + ")");
		
		if (value == null) return;

		final MultiRefCacheEntry cachedEntry = (MultiRefCacheEntry) value;
		
		// remove this key from any of the entity references in m_refs that are dependent on this entry
		for (Iterator iRefs = cachedEntry.getRefs().iterator(); iRefs.hasNext();)
		{
			String ref = (String) iRefs.next();
			Map<Object, Object> keys = m_refsStore.get(ref);
			if ((keys != null) && (keys.containsKey(key)))
			{
				keys.remove(key);

				// remove the ref entry if it no longer has any cached keys in
				// its collection
				if (keys.isEmpty())
				{
					m_refsStore.remove(ref);
				}
			}				
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Cacher implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public String getDescription()
	{
		return "MultiRefCache: " + super.getDescription();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Observer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public void update(Observable o, Object arg)
	{
		if (disabled()) return;

		// arg is Event
		if (!(arg instanceof Event)) return;
		Event event = (Event) arg;

		// if this is just a read, not a modify event, we can ignore it
		if (!event.getModify()) return;

		// if we are holding event processing
		if (m_holdEventProcessing)
		{
			m_heldEvents.add(event);
			return;
		}

		continueUpdate(event);
	}

	/**
	 * Complete the update, given an event that we know we need to act upon.
	 * 
	 * @param event
	 *        The event to process.
	 */
	protected void continueUpdate(Event event)
	{
		String ref = event.getResource();

		if (M_log.isDebugEnabled())
			M_log.debug("update() [" + m_resourcePattern + "] resource: " + ref
					+ " event: " + event.getEvent());

		// do we have this in our ref list
		if (m_refsStore.containsKey(ref))
		{
			// get the copy of the Collection of cache keys for this reference (the actual collection will be reduced as the removes occur)
			Map<Object, Object> cachedKeys = m_refsStore.get(ref);
			for (Iterator iKeys = cachedKeys.keySet().iterator(); iKeys.hasNext();)
			{
					remove(iKeys.next()); // evict primary authz cache
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean isComplete()
	{
		// we do not support being complete
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isComplete(String path)
	{
		// we do not support being complete
		return false;
	}

	/**
	 * @inheritDoc
	 * @see org.sakaiproject.memory.impl.MemCache#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		MultiRefCacheEntry mrce = (MultiRefCacheEntry) super.get(key);
		return (mrce != null ? mrce.getPayload(key) : null);
	}

	//////////////////////////////////////////////////////////////////////
	//  CacheEventListener methods. Cleanup HashMap of m_refs on eviction.
	//////////////////////////////////////////////////////////////////////

	public void dispose() 
	{
		M_log.debug("dispose()");
		// may not be necessary...
		m_refsStore.clear();
	}

	public void notifyElementEvicted(Ehcache cache, Element element) 
	{
		if (M_log.isDebugEnabled())
			M_log.debug("notifyElementEvicted(Ehcache " + cache + ")");
		
		cleanEntityReferences(element.getObjectKey(), element
				.getObjectValue());
	}

	public void notifyElementExpired(Ehcache cache, Element element) 
	{
		if (M_log.isDebugEnabled())
			M_log.debug("notifyElementExpired(Ehcache " + cache + ")");
		
		cleanEntityReferences(element.getObjectKey(), element
				.getObjectValue());
	}

	public void notifyElementPut(Ehcache cache, Element element)
			throws CacheException 
	{
		if (M_log.isDebugEnabled())
			M_log.debug("notifyElementPut(Ehcache " + cache + ")");
		
		// do nothing...
		
	}

	public void notifyElementRemoved(Ehcache cache, Element element)
			throws CacheException 
	{
		if (M_log.isDebugEnabled())
			M_log.debug("notifyElementRemoved(Ehcache " + cache + ")");
		
		cleanEntityReferences(element.getObjectKey(), element
				.getObjectValue());
	}

	public void notifyElementUpdated(Ehcache cache, Element element)
			throws CacheException 
	{
		if (M_log.isDebugEnabled())
			M_log.debug("notifyElementUpdated(Ehcache " + cache + ")");
		
		// do nothing...
		
	}

	public void notifyRemoveAll(Ehcache cache) 
	{
		if (M_log.isDebugEnabled())
			M_log.debug("notifyRemoveAll(Ehcache " + cache + ")");
		
		m_refsStore.clear();
	}

	/**
	 * @see CacheEventListener#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		M_log.debug("clone()");
		
		// Creates a clone of this listener. This method will only be called by ehcache before a cache is initialized.
		// This may not be possible for listeners after they have been initialized. Implementations should throw CloneNotSupportedException if they do not support clone.
		throw new CloneNotSupportedException(
				"CacheEventListener implementations should throw CloneNotSupportedException if they do not support clone");
	}
	
	

//	/**
//	 * Check that the cache and the m_refs are consistent.
//	 */
//	protected void checkState()
//	{
//		// every cache entry's every ref must have an entry in m_refs, and that entry must include the cache entry's key
//		for (Iterator iEntries = m_map.entrySet().iterator(); iEntries.hasNext();)
//		{
//			Map.Entry entry = (Map.Entry) iEntries.next();
//			MultiRefCacheEntry ce = (MultiRefCacheEntry) entry.getValue();
//			for (Iterator iRefs = ce.getRefs().iterator(); iRefs.hasNext();)
//			{
//				String ref = (String) iRefs.next();
//				Collection keys = (Collection) m_refs.get(ref);
//				if (keys == null)
//					m_logger.warn("** cache entry's ref not found in m_refs: cache key: " + entry.getKey() + " ref: " + ref);
//				if ((keys != null) && (!keys.contains(entry.getKey())))
//					m_logger.warn("** cache entry's ref's m_refs entry does not contain cache key: key: " + entry.getKey()
//							+ " ref: " + ref);
//			}
//		}
//
//		// every reference in m_refs every key must exist in the cache, and must include the ref in the cache entry's refs
//		for (Iterator iRefs = m_refs.entrySet().iterator(); iRefs.hasNext();)
//		{
//			Map.Entry entry = (Map.Entry) iRefs.next();
//			Collection keys = (Collection) entry.getValue();
//			for (Iterator iKeys = keys.iterator(); iKeys.hasNext();)
//			{
//				String key = (String) iKeys.next();
//				if (m_map.get(key) == null)
//					m_logger.warn("** m_ref's entry's key not found in cache: ref: " + entry.getKey() + " cache key: " + key);
//				if ((m_map.get(key) != null) && (!(((MultiRefCacheEntry) m_map.get(key)).getRefs().contains(entry.getKey()))))
//					m_logger.warn("** m_ref's entry's key->cache entry does not have the ref: ref: " + entry.getKey() + " cache key: " + key);
//			}
//		}
//	}

}
