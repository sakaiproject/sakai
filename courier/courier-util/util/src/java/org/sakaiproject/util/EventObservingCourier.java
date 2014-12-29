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

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;

/**
 * <p>
 * EventObservingCourier is an ObservingCourier that watches Events, of a particular reference root. It automatically registers / un-registers as an observer with the event service.
 * </p>
 */
public class EventObservingCourier extends org.sakaiproject.util.ObservingCourier implements SessionBindingListener
{
	/** Constructor discovered injected EventTrackingService. */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Construct.
	 * 
	 * @param location
	 *        The key identifying the Portal Page Instance.
	 * @param elementId
	 *        The key identifying the element on the Portal Page that would need a courier delivered message when things change.
	 * @param The
	 *        event resource pattern - we watch for only events whose ref start with this.
	 */
	public EventObservingCourier(String location, String elementId, String resourcePattern)
	{
		super(location, elementId);
		m_resourcePattern = resourcePattern;

		// "inject" a eventTrackingService
		m_eventTrackingService = org.sakaiproject.event.cover.EventTrackingService.getInstance();

		// register to listen to events
		m_eventTrackingService.addObserver(this);
		// %%% add the pattern to have it filtered there?
	}

	/** The event resource pattern - we watch for only events that start with this */
	protected String m_resourcePattern = null;

	public String getResourcePattern()
	{
		return m_resourcePattern;
	}

	public void setResourcePattern(String pattern)
	{
		m_resourcePattern = pattern;
	}

	// %%% re-register? add the pattern to have it filtered there?

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

		// if this is just a read, not a modify event, we can ignore it
		if (!event.getModify()) return false;

		String key = null;

		// filter out events not for us
		if (m_resourcePattern != null)
		{
			key = event.getResource();

			// if this resource is not in my pattern of resources, we can ignore it
			if (!key.startsWith(m_resourcePattern)) return false;
		}

		return true;
	}


	/**********************************************************************************************************************************************************************************************************************************************************
	 * SessionBindingListener implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public void valueBound(SessionBindingEvent event)
	{
	}

	public void valueUnbound(SessionBindingEvent event)
	{
		// stop observing the presence location
		m_eventTrackingService.deleteObserver(this);
	}
}
