/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.presence.cover;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * PresenceService is a static Cover for the {@link org.sakaiproject.presence.api.PresenceService PresenceService}; see that interface for usage details.
 * </p>
 */
public class PresenceService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.presence.api.PresenceService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.presence.api.PresenceService) ComponentManager
						.get(org.sakaiproject.presence.api.PresenceService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.presence.api.PresenceService) ComponentManager
					.get(org.sakaiproject.presence.api.PresenceService.class);
		}
	}

	private static org.sakaiproject.presence.api.PresenceService m_instance = null;

	public static final java.lang.String REFERENCE_ROOT = org.sakaiproject.presence.api.PresenceService.REFERENCE_ROOT;

	public static final java.lang.String EVENT_PRESENCE = org.sakaiproject.presence.api.PresenceService.EVENT_PRESENCE;

	public static final java.lang.String EVENT_ABSENCE = org.sakaiproject.presence.api.PresenceService.EVENT_ABSENCE;

	public static java.lang.String presenceReference(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.presenceReference(param0);
	}

	public static java.lang.String locationId(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.locationId(param0, param1, param2);
	}

	public static java.lang.String getLocationDescription(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.getLocationDescription(param0);
	}

	public static void setPresence(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return;

		service.setPresence(param0);
	}

	public static void setPresence(java.lang.String param0, int param1)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return;

		service.setPresence(param0, param1);
	}

	public static void removePresence(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return;

		service.removePresence(param0);
	}

	public static void removeSessionPresence(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return;

		service.removeSessionPresence(param0);
	}
	
	public static java.util.List<UsageSession> getPresence(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.getPresence(param0);
	}

	public static java.util.List<User> getPresentUsers(java.lang.String param0)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.getPresentUsers(param0);
	}

	public static java.util.List<User> getPresentUsers(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.getPresentUsers(param0, param1);
	}

	public static java.util.List<String> getLocations()
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return null;

		return service.getLocations();
	}

	public static int getTimeout()
	{
		org.sakaiproject.presence.api.PresenceService service = getInstance();
		if (service == null) return 0;

		return service.getTimeout();
	}
}
