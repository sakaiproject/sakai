/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.cover;

import org.sakaiproject.calendar.api.OpaqueUrl;
import org.sakaiproject.component.cover.ComponentManager;

/**
* <p>OpaqueUrlDao is a static Cover for the {@link org.sakaiproject.calendar.api.OpaqueUrlDao OpaqueUrlDao};
* see that interface for usage details.</p>
*/
public class OpaqueUrlDao
{
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.calendar.api.OpaqueUrlDao getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.calendar.api.OpaqueUrlDao) ComponentManager.get(org.sakaiproject.calendar.api.OpaqueUrlDao.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.calendar.api.OpaqueUrlDao) ComponentManager.get(org.sakaiproject.calendar.api.OpaqueUrlDao.class);
		}
	}
	private static org.sakaiproject.calendar.api.OpaqueUrlDao m_instance = null;

	public static OpaqueUrl newOpaqueUrl(String userUUID, String calendarRef)
	{
		org.sakaiproject.calendar.api.OpaqueUrlDao service = getInstance();
		if (service == null)
			return null;

		return service.newOpaqueUrl(userUUID, calendarRef);
	}
	
	public static OpaqueUrl getOpaqueUrl(String userUUID, String calendarRef)
	{
		org.sakaiproject.calendar.api.OpaqueUrlDao service = getInstance();
		if (service == null)
			return null;

		return service.getOpaqueUrl(userUUID, calendarRef);
	}

	public static OpaqueUrl getOpaqueUrl(String opaqueUUID)
	{
		org.sakaiproject.calendar.api.OpaqueUrlDao service = getInstance();
		if (service == null)
			return null;

		return service.getOpaqueUrl(opaqueUUID);
	}

	public static void deleteOpaqueUrl(String userUUID, String calendarRef)
	{
		org.sakaiproject.calendar.api.OpaqueUrlDao service = getInstance();
		if (service == null)
			return;

		service.deleteOpaqueUrl(userUUID, calendarRef);
	}
}
