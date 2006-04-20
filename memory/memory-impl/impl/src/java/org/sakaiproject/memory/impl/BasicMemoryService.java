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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

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

	/** Set of registered cachers. */
	protected Set m_cachers = new HashSet();

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
	 * Configuraiton
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

		m_cachers.clear();

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
		StringBuffer buf = new StringBuffer();
		buf.append("** Memory report\n");

		Iterator it = m_cachers.iterator();
		while (it.hasNext())
		{
			Cacher cacher = (Cacher) it.next();
			buf.append(cacher.getSize() + " in " + cacher.getDescription() + "\n");
		}

		String rv = buf.toString();
		M_log.info(rv);

		return rv;
	}

	/**
	 * Do a reset of all cachers
	 */
	protected void doReset()
	{
		if (!m_cachers.isEmpty())
		{
			// tell all our memory users to reset their memory use
			Iterator it = m_cachers.iterator();
			while (it.hasNext())
			{
				Cacher cacher = (Cacher) it.next();
				cacher.resetCache();
			}

			// run the garbage collector now
			System.runFinalization();
			System.gc();
		}

		M_log.info("doReset():  Low Memory Recovery to: " + Runtime.getRuntime().freeMemory());

	} // doReset

	/**
	 * Register as a cache user
	 */
	public void registerCacher(Cacher cacher)
	{
		m_cachers.add(cacher);

	} // registerCacher

	/**
	 * Unregister as a cache user
	 */
	public void unregisterCacher(Cacher cacher)
	{
		m_cachers.remove(cacher);

	} // unregisterCacher

	/**
	 * {@inheritDoc}
	 */
	public Cache newCache(CacheRefresher refresher, String pattern)
	{
		return new MemCache(this, eventTrackingService(), refresher, pattern);
	}

	/**
	 * {@inheritDoc}
	 */
	public Cache newHardCache(CacheRefresher refresher, String pattern)
	{
		return new HardCache(this, eventTrackingService(), refresher, pattern);
	}

	/**
	 * {@inheritDoc}
	 */
	public Cache newHardCache(long sleep, String pattern)
	{
		return new HardCache(this, eventTrackingService(), sleep, pattern);
	}

	/**
	 * {@inheritDoc}
	 */
	public Cache newCache(CacheRefresher refresher, long sleep)
	{
		return new MemCache(this, eventTrackingService(), refresher, sleep);
	}

	/**
	 * {@inheritDoc}
	 */
	public Cache newHardCache(CacheRefresher refresher, long sleep)
	{
		return new HardCache(this, eventTrackingService(), refresher, sleep);
	}

	/**
	 * {@inheritDoc}
	 */
	public Cache newCache()
	{
		return new MemCache(this, eventTrackingService());
	}

	/**
	 * {@inheritDoc}
	 */
	public Cache newHardCache()
	{
		return new HardCache(this, eventTrackingService());
	}

	/**
	 * {@inheritDoc}
	 */
	public MultiRefCache newMultiRefCache(long sleep)
	{
		return new MultiRefCacheImpl(this, eventTrackingService(), sleep);
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
}
