/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.courier.impl;

// imports
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
	private static Log M_log = LogFactory.getLog(BasicCourierService.class);

	/** Stores a List of Deliveries for each address, keyed by address. */
	protected Map m_addresses = new Hashtable();

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
		m_addresses.clear();
		M_log.info("init()");
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_addresses.clear();
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CourierService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Queue up a delivery for the client window identified in the Delivery object. The particular form of delivery is determined by the type of Delivery object sent.
	 * 
	 * @param delivery
	 *        The Delivery (or extension) object to deliver.
	 */
	public void deliver(Delivery delivery)
	{
		if (M_log.isDebugEnabled()) M_log.debug("deliver(): " + delivery);

		String address = delivery.getAddress();

		// find the entry in m_addresses
		List deliveries = (List) m_addresses.get(address);

		// create if needed
		if (deliveries == null)
		{
			synchronized (m_addresses)
			{
				deliveries = (List) m_addresses.get(address);
				if (deliveries == null)
				{
					deliveries = new Vector();
					m_addresses.put(address, deliveries);
				}
			}
		}

		// if this doesn't exist in the list already, add it
		synchronized (deliveries)
		{
			if (!deliveries.contains(delivery))
			{
				deliveries.add(delivery);
			}
		}
	}

	/**
	 * Clear any pending delivery requests to the particular client window for this element.
	 * 
	 * @param address
	 *        The address of the client window.
	 * @param elementId
	 *        The id of the html element.
	 */
	public void clear(String address, String elementId)
	{
		if (M_log.isDebugEnabled()) M_log.debug("clear(): " + address + ", " + elementId);

		// find the entry in m_addresses
		List deliveries = (List) m_addresses.get(address);

		// if not there we are done
		if (deliveries == null) return;

		// remove any Deliveries with this elementId
		synchronized (deliveries)
		{
			for (Iterator it = deliveries.iterator(); it.hasNext();)
			{
				Delivery delivery = (Delivery) it.next();
				if (!StringUtil.different(delivery.getElement(), elementId))
				{
					it.remove();
				}
			}
		}

		// if none left, remove it from the list
		if (deliveries.isEmpty())
		{
			synchronized (m_addresses)
			{
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
		if (M_log.isDebugEnabled()) M_log.debug("clear(): " + address);

		// remove this portal from m_addresses
		synchronized (m_addresses)
		{
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
	public List getDeliveries(String address)
	{
		if (M_log.isDebugEnabled()) M_log.debug("getDeliveries(): " + address);

		// find the entry in m_addresses
		List deliveries = null;
		synchronized (m_addresses)
		{
			deliveries = (List) m_addresses.get(address);
			if (deliveries != null)
			{
				m_addresses.remove(address);
			}
		}

		// if empty, return something
		if (deliveries == null)
		{
			deliveries = new Vector();
		}

		synchronized (deliveries)
      {
   		// "act" all the deliveries
   		for (Iterator it = deliveries.iterator(); it.hasNext();)
   		{
   			Delivery delivery = (Delivery) it.next();
   			delivery.act();
   		}
      }
		return deliveries;
	}

	/**
	 * Check to see if there are any deliveries queued up for a particular session client window.
	 * 
	 * @param address
	 *        The address of the client window.
	 * @return true if there are deliveries for this client window, false if not.
	 */
	public boolean hasDeliveries(String address)
	{
		if (M_log.isDebugEnabled()) M_log.debug("hasDeliveries(): " + address);

		// find the entry in m_addresses
		List deliveries = (List) m_addresses.get(address);
		if (deliveries == null) return false;

		return (!deliveries.isEmpty());
	}
}
