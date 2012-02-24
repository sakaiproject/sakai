/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.event.cover;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * EventTrackingService is a static Cover for the {@link org.sakaiproject.event.api.EventTrackingService EventTrackingService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class EventTrackingService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.event.api.EventTrackingService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.event.api.EventTrackingService) ComponentManager
						.get(org.sakaiproject.event.api.EventTrackingService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.event.api.EventTrackingService) ComponentManager
					.get(org.sakaiproject.event.api.EventTrackingService.class);
		}
	}

	private static org.sakaiproject.event.api.EventTrackingService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.event.api.EventTrackingService.SERVICE_NAME;

	public static void addPriorityObserver(java.util.Observer param0)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.addPriorityObserver(param0);
	}

	public static void addLocalObserver(java.util.Observer param0)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.addLocalObserver(param0);
	}

	public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1, boolean param2)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return null;

		return service.newEvent(param0, param1, param2);
	}

	public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1, boolean param2,
			int param3)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return null;

		return service.newEvent(param0, param1, param2, param3);
	}

	public static org.sakaiproject.event.api.Event newEvent(java.lang.String param0, java.lang.String param1,  java.lang.String param2, boolean param3,
			int param4)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return null;

		return service.newEvent(param0, param1, param2, param3, param4);
	}

	public static void addObserver(java.util.Observer param0)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.addObserver(param0);
	}

	public static void deleteObserver(java.util.Observer param0)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.deleteObserver(param0);
	}

	public static void post(org.sakaiproject.event.api.Event param0, org.sakaiproject.event.api.UsageSession param1)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.post(param0, param1);
	}

	public static void post(org.sakaiproject.event.api.Event param0)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.post(param0);
	}

	public static void delay(Event event, Time fireTime)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.delay(event, fireTime);
	}

	public static void cancelDelays(String resource)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.cancelDelays(resource);
	}

	public static void cancelDelays(String resource, String event)
	{
		org.sakaiproject.event.api.EventTrackingService service = getInstance();
		if (service == null) return;

		service.cancelDelays(resource, event);
	}
}
