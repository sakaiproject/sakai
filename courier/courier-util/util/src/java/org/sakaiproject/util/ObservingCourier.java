/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.courier.api.CourierService;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>
 * ObservingCourier is an observer which uses the courier service to notify when things change.
 * </p>
 */
public abstract class ObservingCourier implements org.sakaiproject.courier.api.ObservingCourier, Observer
{
	/** Constructor discovered injected CourierService. */
	protected CourierService m_courierService = null;

	/** The location (id not ref). */
	protected String m_location = null;

	/**
	 * Construct.
	 * 
	 * @param location
	 *        The key identifying the client window to which this courier delivers updates.
	 * @param elementId
	 *        The key identifying the element on the Portal Page that would need a courier delivered message when things change.
	 */
	public ObservingCourier(String location, String elementId)
	{
		m_deliveryId = SessionManager.getCurrentSession().getId() + location;
		m_elementId = elementId;
		m_location = location;

		// "inject" a CourierService
		m_courierService = org.sakaiproject.courier.cover.CourierService.getInstance();
	}

	/** The key identifying the Portal PageSession. */
	protected String m_deliveryId = "";

	public String getDeliveryId()
	{
		return m_deliveryId;
	}

	public void setDeliveryId(String id)
	{
		m_deliveryId = id;
	}

	/** The key identifying the element on the Portal Page. */
	protected String m_elementId = "";

	public String getElementId()
	{
		return m_elementId;
	}

	public void setElementId(String id)
	{
		m_elementId = id;
	}

	/** The enabled state. */
	protected boolean m_enabled = true;

	public boolean getEnabled()
	{
		return m_enabled;
	}

	public void enable()
	{
		m_enabled = true;
	}

	public void disable()
	{
		m_enabled = false;
	}

	/**
	 * Accept notification that the portal element has just been delivered. If there are pending requests to deliver, they can be cleared.
	 */
	public void justDelivered()
	{
		m_courierService.clear(getDeliveryId(), getElementId());
	}

	/**
	 * Check to see if we want to process or ignore this update.
	 * 
	 * @param arg
	 *        The arg from the update.
	 * @return true to continue, false to quit.
	 */
	public boolean check(Object arg)
	{
		return true;
	}

	/**
	 * Access the location this observer is observing.
	 * 
	 * @return the location this observer is observing.
	 */
	public String getLocation()
	{
		return m_location;
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
		// ignore changes when not enabled
		if (!getEnabled())
		{
			return;
		}

		if (!check(arg)) return;

		m_courierService.deliver(new DirectRefreshDelivery(getDeliveryId(), getElementId()));
	}
}
