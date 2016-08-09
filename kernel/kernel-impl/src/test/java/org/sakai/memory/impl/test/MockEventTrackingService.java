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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakai.memory.impl.test;

import java.util.Observer;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * @author ieb
 *
 */
public class MockEventTrackingService implements EventTrackingService
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#addLocalObserver(java.util.Observer)
	 */
	public void addLocalObserver(Observer observer)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#addObserver(java.util.Observer)
	 */
	public void addObserver(Observer observer)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#addPriorityObserver(java.util.Observer)
	 */
	public void addPriorityObserver(Observer observer)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#deleteObserver(java.util.Observer)
	 */
	public void deleteObserver(Observer observer)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#newEvent(java.lang.String, java.lang.String, boolean)
	 */
	public Event newEvent(String event, String resource, boolean modify)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#newEvent(java.lang.String, java.lang.String, boolean, int)
	 */
	public Event newEvent(String event, String resource, boolean modify, int priority)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#newEvent(java.lang.String, java.lang.String, java.lang.String, boolean, int)
	 */
	public Event newEvent(String event, String resource, String context, boolean modify, int priority)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#newEvent(java.lang.String, java.lang.String, java.lang.String, boolean, int, LRS_Statement)
	 */
	public Event newEvent(String event, String resource, String context, boolean modify, int priority, LRS_Statement lrsStatement)
	{
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#post(org.sakaiproject.event.api.Event)
	 */
	public void post(Event event)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#post(org.sakaiproject.event.api.Event, org.sakaiproject.event.api.UsageSession)
	 */
	public void post(Event event, UsageSession session)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.event.api.EventTrackingService#post(org.sakaiproject.event.api.Event, org.sakaiproject.user.api.User)
	 */
	public void post(Event event, User user)
	{
		// TODO Auto-generated method stub
		
	}

	public void setEventDelayHandler(EventDelayHandler handler)
	{
		// TODO Auto-generated method stub
		
	}

	public void cancelDelays(String resource)
	{
		// TODO Auto-generated method stub
		
	}

	public void cancelDelays(String resource, String event)
	{
		// TODO Auto-generated method stub
		
	}

	public void delay(Event event, Time fireTime)
	{
		// TODO Auto-generated method stub
		
	}
}
