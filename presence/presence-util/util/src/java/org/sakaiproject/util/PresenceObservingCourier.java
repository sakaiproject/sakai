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

import org.sakaiproject.event.api.Event;
import org.sakaiproject.presence.cover.PresenceService;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * <p>
 * PresenceObservingCourier is an EventObservingCourier which watches for only presence service events at a particular location, and delivers a direct refresh delivery.
 * </p>
 */
public class PresenceObservingCourier extends EventObservingCourier
{
	/**
	 * This variant, watches presense changes at the specified location, and sends the notifications to 
	 * that same location.  The elementID is null so the main window is refreshed when the notification 
	 * is received.
	 * 
	 * @param location
	 *        The location under observation *and* the location for the delivery of the events.
	 */
	public PresenceObservingCourier(String location)
	{
		super(location, null, PresenceService.presenceReference(location));
	}

	/**
	 * This variant watches changes in a window and sends change notifications to the same window.
         * In the case where there are multiple iframes in the tool, elementID sends notification to 
	 * the frame specified by elementId.
	 * 
	 * @param location
	 *        The location under observation *and* the location for the delivery of the events.
	 * @param elementId
	 *        The html element to refresh.  If this is null the main window is refreshed.
	 */
	public PresenceObservingCourier(String location, String elementId)
	{
		super(location, elementId, PresenceService.presenceReference(location));
	}

	/**
	 * This variant watches changes in one window (watchLocation) and sends the notifications to a different
	 * window (location)..  
	 * 
	 * @param location
	 *        The location which will receive the notifications.
	 * @param elementId
	 *        The html element to refresh.  If this is null, the main window is refreshed.
	 * @param watchLocation
	 *        The location being observed.
	 */
	public PresenceObservingCourier(String location, String elementId, String watchLocation)
	{
		super(location, elementId, PresenceService.presenceReference(watchLocation));
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
		// arg is Event
		if (!(arg instanceof Event)) return false;
		Event event = (Event) arg;
		String key = event.getResource();

		// reject non presence events
		String function = event.getEvent();
		if (!(function.equals(PresenceService.EVENT_PRESENCE) || function.equals(PresenceService.EVENT_ABSENCE))) return false;

		// look for matches to the pattern
		if (m_resourcePattern != null)
		{
			if (!key.equals(m_resourcePattern)) return false;
		}

		return true;
	}

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

		boolean inFrame = ServerConfigurationService.getBoolean("display.users.present.iframe", false);
		if ( inFrame ) 
		{
			if (m_elementId == null)
			{
				m_courierService.deliver(new DirectRefreshDelivery(getDeliveryId()));
			}
			else
			{
				m_courierService.deliver(new DirectRefreshDelivery(getDeliveryId(), m_elementId));
			}
		}
		else
		{
			String updatePresence = "try { updatePresence(); } catch(err) { }";
			m_courierService.deliver(new TestDelivery(getDeliveryId(), m_elementId, updatePresence));
		}
	}
}

