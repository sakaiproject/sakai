/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation
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

package org.sakaiproject.courier.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.courier.api.CourierService;
import org.sakaiproject.courier.api.Delivery;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * BasicCourierService is the implementation for the CourierService.
 * </p>
 */
public class BasicCourierService implements CourierService
{
	/** Our logger. */
	private static final Log M_log = LogFactory.getLog(BasicCourierService.class);

        protected static final int nLocks = 100;

	/** Stores a List of Deliveries for each address, keyed by address. */
	protected Map<String, List<Delivery>> m_addresses =
	    new ConcurrentHashMap<String, List<Delivery>>(nLocks, 0.75f, nLocks);

       /** 
	** Array of objects for locking. You take the hash MOD the size of the
	** array to find the element in the array to lock. This allows better
	** concurrency than a global lock on the hash table. I still use
	** ConcurrentHashMap because otherwise I worry about two attempts
	** to add an entry that happen to use the same lock. So I'm 
	** depending upon ConcurrentHashMap to maintain the soundness of
	** the data structure. Unfortunately the synchronization in it doesn't
	** help against situations where I get a value and then test or
	** modify it. That needs real locks.
	**/

        protected Object[] locks;


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
		m_addresses.clear();
		locks = new Object[nLocks];
		for (int i = 0; i < nLocks; i++)
		    locks[i] = new Object();
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
		m_addresses.clear();
		locks = null;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CourierService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

         protected int slot(String s) {
	     
	     int hash = Math.abs(s.hashCode());

	     return hash % nLocks;
	 }

	/**
	 * Queue up a delivery for the client window identified in the Delivery 
	 * object. The particular form of delivery is determined by the type of 
	 * Delivery object sent.
	 * 
	 * @param delivery
	 *        The Delivery (or extension) object to deliver.
	 */
	public void deliver(Delivery delivery)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("deliver(Delivery " + delivery + ")");

		final String address = delivery.getAddress();

		synchronized(locks[slot(address)]) {

		    // find the entry in m_addresses
		    List<Delivery> deliveries = m_addresses.get(address);

		    // create if needed
		    if (deliveries == null) {
				deliveries = new ArrayList<Delivery>();
				m_addresses.put(address, deliveries);
		    }

		    // if this doesn't exist in the list already, add it
		    if (!deliveries.contains(delivery)) {
		    	deliveries.add(delivery);
		    }
		}
	}

	/**
	 * Clear any pending delivery requests to the particular client window for 
	 * this element.
	 * 
	 * @param address
	 *        The address of the client window.
	 * @param elementId
	 *        The id of the html element.
	 */
	public void clear(String address, String elementId)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("clear(String " + address + ", String " + elementId
					+ ")");

		synchronized(locks[slot(address)]) {
		    
		    // find the entry in m_addresses
		    List<Delivery> deliveries = m_addresses.get(address);
		    
		    // if not there we are done
		    if (deliveries == null) return;

		    // remove any Deliveries with this elementId
		    for (Iterator<Delivery> it = deliveries.iterator(); it.hasNext();) {
			Delivery delivery = it.next();
			if (!StringUtil.different(delivery.getElement(), elementId)) {
			    it.remove();
			}
		    }

		    // if none left, remove it from the list
		    if (deliveries.isEmpty()) {
			m_addresses.remove(address);
		    }
		}
	}

	/**
	 * Clear any pending delivery requests to this session client window.
	 * 
	 * @param address
	 *        The address of client window.
	 */
	public void clear(String address)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("clear(String " + address + ")");

		synchronized(locks[slot(address)]) {
		    // remove this portal from m_addresses
		    m_addresses.remove(address);
		}
	}

	/**
	 * Access and de-queue the Deliveries queued up for a particular session client window.
	 * 
	 * @param address
	 *        The address of client window.
	 * @return a List of Delivery objects addressed to this session client window.
	 */
	@SuppressWarnings("unchecked")
	public List getDeliveries(String address)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("getDeliveries(String " + address + ")");

		List<Delivery> deliveries;

		synchronized(locks[slot(address)]) {

		    // find the entry in m_addresses
		    deliveries = m_addresses.get(address);
		    if (deliveries == null) { // if null, return something
		    	return Collections.EMPTY_LIST; // this should return null!
		    } else {
		    	m_addresses.remove(address);
		    }
		}

		// do this outside of the sync. no reason to hold
		// other operations now that we've atomically gotten the list
		// I'm worried about delays and maybe even deadlocks if we
		// do arbitrary code while holding a lock.

		// "act" all the deliveries
		for (Delivery delivery : deliveries)
		    {
			delivery.act();
		    }

		return deliveries;
	}

	/**
	 * Check to see if there are any deliveries queued up for a particular
	 * session client window.
	 * 
	 * @param address
	 *            The address of the client window.
	 * @return true if there are deliveries for this client window, false if
	 *         not.
         *
	 * WARNING: This method is almost certainly not what you want.
	 * I can see no way to use it that won't have synchronization problems.
	 */
	public boolean hasDeliveries(String address)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("hasDeliveries(String " + address + ")");

		List<Delivery> deliveries;

		// find the entry in m_addresses
		synchronized(locks[slot(address)]) {
		    deliveries = m_addresses.get(address);
		}
		if (deliveries == null) return false;

		return (!deliveries.isEmpty());
	}
}
