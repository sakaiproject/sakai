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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.event.CacheManagerEventListener;
import net.sf.ehcache.hibernate.EhCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.Cacher;
import org.sakaiproject.memory.api.MemoryPermissionException;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.MultiRefCache;

/**
 * <p>
 * MemBasicMemoryServiceoryService is an implementation for the MemoryService which reports memory usage and runs a periodic garbage collection to keep memory available.
 * </p>
 */
public abstract class BasicMemoryService implements MemoryService, Observer
{

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BasicMemoryService.class);

	/** Event for the memory reset. */
	protected static final String EVENT_RESET = "memory.reset";

	/** The underlying cache manager; injected */
	protected CacheManager cacheManager;

	/** If true, output verbose caching info. */
	protected boolean m_cacheLogging = false;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the UsageSessionService collaborator.
	 */
	protected abstract UsageSessionService usageSessionService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Configuration: cache verbose debug
	 */
	public void setCacheLogging(boolean value)
	{
		m_cacheLogging = value;
	}

	public boolean getCacheLogging()
	{
		return m_cacheLogging;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// get notified of events to watch for a reset
			eventTrackingService().addObserver(this);

			M_log.info("init()");
			
			if (cacheManager == null)
				throw new IllegalStateException(
						"CacheManager was not injected properly!");
			
			cacheManager.getCacheManagerEventListenerRegistry().registerListener(new CacheManagerEventListener() {

				private Status status = Status.STATUS_UNINITIALISED;
				public void dispose() throws CacheException
				{
					status = Status.STATUS_SHUTDOWN;
				}

				public Status getStatus()
				{
					return status;
				}

				public void init() throws CacheException
				{
					status = Status.STATUS_ALIVE;
				}

				public void notifyCacheAdded(String name)
				{
					Ehcache cache = cacheManager.getEhcache(name);
					M_log.info("Added Cache name ["+name+"] as Cache [" + cache.getName() +"] " +
							"Max Elements in Memory ["+cache.getMaxElementsInMemory()+"] "+
							"Max Elements on Disk ["+cache.getMaxElementsOnDisk()+"] "+
							"Time to Idle (seconds) ["+cache.getTimeToIdleSeconds()+"] "+
							"Time to Live (seconds) ["+cache.getTimeToLiveSeconds()+"] "+
							"Memory Store Eviction Policy ["+cache.getMemoryStoreEvictionPolicy()+"] ");

				}

				public void notifyCacheRemoved(String name)
				{
					M_log.info("Cache Removed "+name);	
					
				}
				
			});
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// if we are not in a global shutdown, remove my event notification registration
		if (!ComponentManager.hasBeenClosed())
		{
			eventTrackingService().deleteObserver(this);
		}

		cacheManager.clearAll();

		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * MemoryService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Return the amount of available memory.
	 * 
	 * @return the amount of available memory.
	 */
	public long getAvailableMemory()
	{
		return Runtime.getRuntime().freeMemory();

	} // getAvailableMemory

	/**
	 * Cause less memory to be used by clearing any optional caches.
	 */
	public void resetCachers() throws MemoryPermissionException
	{
		// check that this is a "super" user with the security service
		if (!securityService().isSuperUser())
		{
			// TODO: session id or session user id?
			throw new MemoryPermissionException(usageSessionService().getSessionId(), EVENT_RESET, "");
		}

		// post the event so this and any other app servers in the cluster will reset
		eventTrackingService().post(eventTrackingService().newEvent(EVENT_RESET, "", true));

	} // resetMemory

	/**
	 * Compute a status report on all memory users
	 */
	public String getStatus()
	{
		final StringBuilder buf = new StringBuilder();
		buf.append("** Memory report\n");
		buf.append("freeMemory: " + Runtime.getRuntime().freeMemory());
		buf.append(" totalMemory: "); buf.append(Runtime.getRuntime().totalMemory());
		buf.append(" maxMemory: "); buf.append(Runtime.getRuntime().maxMemory());
		buf.append("\n\n");

		List<Ehcache> allCaches = getAllCaches(true);
		
		// summary
		for (Ehcache cache : allCaches) {
			final long hits = cache.getStatistics().getCacheHits();
			final long misses = cache.getStatistics().getCacheMisses();
			final long total = hits + misses;
			final long hitRatio = ((total > 0) ? ((100l * hits) / total) : 0);
			buf.append(cache.getName() + ": " + 
					" count:" + cache.getStatistics().getObjectCount() +
					" hits:" + hits +
					" misses:" + misses + 
					" hit%:" + hitRatio);
			buf.append("\n");
		}

		// extended report
		buf.append("\n** Extended Cache Report\n");
		for (Object ehcache : allCaches) {
			buf.append(ehcache.toString());
			buf.append("\n");
		}
		
// Iterator<Cacher> it = m_cachers.iterator();
//		while (it.hasNext())
//		{
//			Cacher cacher = (Cacher) it.next();
//			buf.append(cacher.getSize() + " in " + cacher.getDescription() + "\n");
//		}

		final String rv = buf.toString();
		M_log.info(rv);

		return rv;
	}
	
	/**
	 * Return all caches from the CacheManager
	 * @param sorted Should the caches be sorted by name?
	 * @return
	 */
	private List<Ehcache> getAllCaches(boolean sorted)
	{
		M_log.debug("getAllCaches()");

		final String[] cacheNames = cacheManager.getCacheNames();
		if(sorted) Arrays.sort(cacheNames);
		final List<Ehcache> caches = new ArrayList<Ehcache>(cacheNames.length);
		for (String cacheName : cacheNames) {
			caches.add(cacheManager.getEhcache(cacheName));
		}
		return caches;
	}

	/**
	 * Do a reset of all cachers
	 */
	protected void doReset()
	{
		M_log.debug("doReset()");

		final List<Ehcache> allCaches = getAllCaches(false);
		for (Ehcache ehcache : allCaches) {
			ehcache.removeAll(); //TODO should we doNotNotifyCacheReplicators? Ian?
			ehcache.clearStatistics();
		}

		// run the garbage collector now
		System.runFinalization();
		System.gc();

		M_log.info("doReset():  Low Memory Recovery to: " + Runtime.getRuntime().freeMemory());

	} // doReset

	/**
	 * Register as a cache user
	 * @deprecated
	 */
	synchronized public void registerCacher(Cacher cacher)
	{
		// not needed with ehcache

	} // registerCacher

	/**
	 * Unregister as a cache user
	 * @deprecated
	 */
	synchronized public void unregisterCacher(Cacher cacher)
	{
		// not needed with ehcache

	} // unregisterCacher

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newCache(CacheRefresher refresher, String pattern)
	{
		return new MemCache(this, eventTrackingService(), refresher, pattern,
				instantiateCache("MemCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newHardCache(CacheRefresher refresher, String pattern)
	{
		return new HardCache(this, eventTrackingService(), refresher, pattern,
				instantiateCache("HardCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newHardCache(long sleep, String pattern)
	{
		return new HardCache(this, eventTrackingService(), sleep, pattern,
				instantiateCache("HardCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newCache(CacheRefresher refresher, long sleep)
	{
		return new MemCache(this, eventTrackingService(), refresher, sleep,
				instantiateCache("MemCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newHardCache(CacheRefresher refresher, long sleep)
	{
		return new MemCache(this, eventTrackingService(), refresher, sleep,
				instantiateCache("HardCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newCache()
	{
		return new MemCache(this, eventTrackingService(),
				instantiateCache("MemCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public Cache newHardCache()
	{
		return new HardCache(this, eventTrackingService(),
				instantiateCache("HardCache"));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public MultiRefCache newMultiRefCache(long sleep)
	{
		return new MultiRefCacheImpl(
				this,
				eventTrackingService(),
				instantiateCache("MultiRefCache"));
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

		// look for the memory reset event
		String function = event.getEvent();
		if (!function.equals(EVENT_RESET)) return;

		// do the reset here, too!
		doReset();
	}
	
	/**
	 * 
	 * @param cacheName
	 * @param legacyMode
	 *            If true always create a new Cache. If false, cache must be
	 *            defined in bean factory.
	 * @return
	 */
	private Ehcache instantiateCache(String cacheName)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("createNewCache(String " + cacheName + ")");

		String name = cacheName;
		if (name == null || "".equals(name))
		{
			name = "DefaultCache" + UUID.randomUUID().toString();			
		}
		name = MemoryService.class.getName()+"."+name;
		
		
		// Cache creation should all go to the cache manager and be
		// configured via the cache manager setup.
		
		if ( cacheManager.cacheExists(name) ) {
			return cacheManager.getEhcache(name);
		} 
		cacheManager.addCache(name);
		return cacheManager.getEhcache(name);
		
		
		
		
		/*
		

		if(legacyMode)
		{
			if (cacheManager.cacheExists(name)) {
				M_log.warn("Cache already exists and is bound to CacheManager; creating new cache from defaults: "
						+ name);
				// favor creation of new caches for backwards compatibility
				// in the future, it seems like you would want to return the same
				// cache if it already exists
				name = name + UUID.randomUUID().toString();
			}
		}

		Ehcache cache = null;
		
		// try to locate a named cache in the bean factory
		try {
			cache = (Ehcache) ComponentManager.get(name);
		} catch (Throwable e) {
			cache = null;
			M_log.error("Error occurred when trying to load cache from bean factory!", e);
		}
		
		
		if(cache != null) // found the cache
		{
			M_log.info("Loaded Named Cache " + cache);

			return cache;
		}
		else // did not find the cache
		{
			if(legacyMode)
			{
				cacheManager.addCache(name); // create a new cache
				cache = cacheManager.getEhcache(name);
				M_log.info("Loaded Default Cache " + cache);				
			}
			else
			{
				M_log.error("Could not find named cache in the bean factory!:"
								+ name);
			}

			return cache;			
		}
		*/
	}

	public void setCacheManager(net.sf.ehcache.CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public Cache newCache(String cacheName, CacheRefresher refresher,
			String pattern) {
		return new MemCache(this, eventTrackingService(), refresher, pattern,
				instantiateCache(cacheName));
	}

	public Cache newCache(String cacheName, String pattern) {
		return new MemCache(this, eventTrackingService(), pattern,
				instantiateCache(cacheName));
	}

	public Cache newCache(String cacheName, CacheRefresher refresher) {
		return new MemCache(this, eventTrackingService(), refresher,
				instantiateCache(cacheName));
	}

	public Cache newCache(String cacheName) {
		return new MemCache(this, eventTrackingService(),
				instantiateCache(cacheName));
	}

	public MultiRefCache newMultiRefCache(String cacheName) {
		return new MultiRefCacheImpl(
				this,
				eventTrackingService(),
				instantiateCache(cacheName));
	}

}
