/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.courier.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.courier.api.CourierService;
import org.sakaiproject.courier.api.Delivery;
import org.sakaiproject.courier.api.DeliveryProvider;
import org.sakaiproject.courier.api.Expirable;
import org.sakaiproject.util.StringUtil;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * BasicCourierService is the implementation for the CourierService.
 * </p>
 * 
 * @deprecated The BasicCourierService has stability issues and will be removed
 * 		in a future release (after 10.0)
 * 		<a href="https://jira.sakaiproject.org/browse/SAK-22053">SAK-22053</a>
 *		@see CourierService  
 */
@Deprecated
@Slf4j
public class BasicCourierService implements CourierService, Runnable
{
	private static final int nLocks = 100;

	/** Stores a List of Deliveries for each address, keyed by address. */
	private Map<String, List<Delivery>> m_addresses = new ConcurrentHashMap<>(nLocks, 0.75f, nLocks);

	/** 
	 * Configuration: do maintenance cleanup aggressively 
	 * True indicates that the entire address will be removed from the m_addresses map, false means that only the expired delivery will be removed
	 */
	@Setter private boolean m_aggressiveCleanup = false;

	/** Configuration: how often to check for inactive deliveries (seconds).  0 means no checking */
	@Setter private int m_checkEvery = 300;

	/**
	 * Array of objects for locking. You take the hash MOD the size of the
	 * array to find the element in the array to lock. This allows better
	 * concurrency than a global lock on the hash table. I still use
	 * ConcurrentHashMap because otherwise I worry about two attempts
	 * to add an entry that happen to use the same lock. So I'm
	 * depending upon ConcurrentHashMap to maintain the soundness of
	 * the data structure. Unfortunately the synchronization in it doesn't
	 * help against situations where I get a value and then test or
	 * modify it. That needs real locks.
	 */
	private Object[] locks;

	private List<DeliveryProvider> deliveryProviders = new Vector<>();

	private ScheduledExecutorService scheduler;
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	public void init()
	{
		log.info("init()");
		m_addresses.clear();
		locks = new Object[nLocks];
		for (int i = 0; i < nLocks; i++)
		    locks[i] = new Object();
		
		m_checkEvery = ServerConfigurationService.getInt("courier.maintThreadChecks", 300);
		m_aggressiveCleanup = ServerConfigurationService.getBoolean("courier.aggressiveCleanup", false);
		
		// start the maintenance thread
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleWithFixedDelay(
				this,
				0,
				m_checkEvery, // run every
				TimeUnit.SECONDS
		);
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		log.info("destroy()");
		scheduler.shutdown();
		m_addresses.clear();
		locks = null;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CourierService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	private int slot(String s) {
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
		log.debug("deliver(Delivery {})", delivery);

		final String address = delivery.getAddress();

		synchronized(locks[slot(address)]) {
		    // find the entry in m_addresses
		    List<Delivery> deliveries = m_addresses.computeIfAbsent(address, k -> new ArrayList<>());

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
		log.debug("clear(String {}, String {})", address, elementId);

		synchronized(locks[slot(address)]) {
		    
		    // find the entry in m_addresses
		    List<Delivery> deliveries = m_addresses.get(address);
		    
		    // if not there we are done
		    if (deliveries == null) return;

		    // remove any Deliveries with this elementId
		    deliveries.removeIf(delivery -> !StringUtil.different(delivery.getElement(), elementId));

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
		log.debug("clear(String {})", address);

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
	public List<Delivery> getDeliveries(String address)
	{
		log.debug("getDeliveries(String {})", address);

		List<Delivery> deliveries;

		synchronized(locks[slot(address)]) {

		    // find the entry in m_addresses
		    deliveries = m_addresses.get(address);
		    if (deliveries == null) { // if null, return something
		    	return Collections.emptyList(); // this should return null!
		    } else {
		    	m_addresses.remove(address);
		    }
		}

		// do this outside of the sync. no reason to hold
		// other operations now that we've atomically gotten the list
		// I'm worried about delays and maybe even deadlocks if we
		// do arbitrary code while holding a lock.

		// "act" all the deliveries
		deliveries.forEach(Delivery::act);

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
		log.debug("hasDeliveries(String {})", address);

		List<Delivery> deliveries;

		// find the entry in m_addresses
		synchronized(locks[slot(address)]) {
		    deliveries = m_addresses.get(address);
		}
		if (deliveries == null) return false;

		return (!deliveries.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.courier.api.CourierService#getDeliveryProviders()
	 */
	public List<DeliveryProvider> getDeliveryProviders() {
		log.debug("getDeliveryProviders()");

		if(deliveryProviders.isEmpty()) {
			return null;
		}
		return new ArrayList<>(deliveryProviders);
	}

	/**
	 * Run the maintenance thread. Every m_checkEvery seconds, check for expired deliveries.
	 */
	@Override
	public void run() {
		Thread.currentThread().setName("Sakai.BasicCourierService.Maintenance");
		// since we might be running while the component manager is still being created and populated, such as at server
		// startup, wait here for a complete component manager
		ComponentManager.waitTillConfigured();

		log.debug("Maintenance thread start...");
		try {
			long now = System.currentTimeMillis();
			for (List<Delivery> deliveries : m_addresses.values()) {
				for(Iterator<Delivery> iter = deliveries.iterator(); iter.hasNext();) {
					Delivery delivery = iter.next();
					if (delivery instanceof Expirable) {
						String address = delivery.getAddress();
						Expirable expDelivery = (Expirable) delivery;
						long created = expDelivery.getCreated();
						int ttl = expDelivery.getTtl();
						//Don't need to worry about it if ttl is not set.
						if (ttl > 0) {
							long sDiff = (now - created) / 1000;
							// If the time since creation is larger than the ttl, remove it
							if (sDiff > ttl) {
								if (m_aggressiveCleanup) {
									log.debug("Removing all ({}) expired deliveries for address: {}", deliveries.size(), address);
									clear(address);
									break;
								}
								else {
									log.debug("Removing a single expired delivery for address: {}", address);
									iter.remove();
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("run(): exception: {}", e.getMessage(), e);
		}
		log.debug("Maintenance thread end...");
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.courier.api.CourierService#registerDeliveryProvider(org.sakaiproject.courier.api.DeliveryProvider)
	 */
	public void registerDeliveryProvider(DeliveryProvider provider) {
		log.debug("registerDeliveryProvider(DeliveryProvider {})", provider);
		this.deliveryProviders.add(provider);
	}
}
