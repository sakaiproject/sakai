/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.time.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * TimeService is a static Cover for the {@link org.sakaiproject.time.api.TimeService TimeService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class TimeService
{
	public static java.lang.String APPLICATION_ID = org.sakaiproject.time.api.TimeService.APPLICATION_ID;

	public static java.lang.String TIMEZONE_KEY = org.sakaiproject.time.api.TimeService.TIMEZONE_KEY;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.time.api.TimeService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.time.api.TimeService) ComponentManager
						.get(org.sakaiproject.time.api.TimeService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.time.api.TimeService) ComponentManager.get(org.sakaiproject.time.api.TimeService.class);
		}
	}

	private static org.sakaiproject.time.api.TimeService m_instance = null;

	public static org.sakaiproject.time.api.Time newTimeGmt(java.lang.String param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeGmt(param0);
	}

	public static org.sakaiproject.time.api.Time newTimeGmt(int param0, int param1, int param2, int param3, int param4, int param5,
			int param6)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeGmt(param0, param1, param2, param3, param4, param5, param6);
	}

	public static org.sakaiproject.time.api.Time newTimeGmt(org.sakaiproject.time.api.TimeBreakdown param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeGmt(param0);
	}

	public static org.sakaiproject.time.api.Time newTimeLocal(org.sakaiproject.time.api.TimeBreakdown param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeLocal(param0);
	}

	public static org.sakaiproject.time.api.Time newTimeLocal(int param0, int param1, int param2, int param3, int param4,
			int param5, int param6)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeLocal(param0, param1, param2, param3, param4, param5, param6);
	}

	public static org.sakaiproject.time.api.TimeBreakdown newTimeBreakdown(int param0, int param1, int param2, int param3,
			int param4, int param5, int param6)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeBreakdown(param0, param1, param2, param3, param4, param5, param6);
	}

	public static org.sakaiproject.time.api.TimeRange newTimeRange(org.sakaiproject.time.api.Time param0,
			org.sakaiproject.time.api.Time param1)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeRange(param0, param1);
	}

	public static org.sakaiproject.time.api.TimeRange newTimeRange(org.sakaiproject.time.api.Time param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeRange(param0);
	}

	public static org.sakaiproject.time.api.TimeRange newTimeRange(long param0, long param1)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeRange(param0, param1);
	}

	public static org.sakaiproject.time.api.TimeRange newTimeRange(org.sakaiproject.time.api.Time param0,
			org.sakaiproject.time.api.Time param1, boolean param2, boolean param3)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeRange(param0, param1, param2, param3);
	}

	public static org.sakaiproject.time.api.TimeRange newTimeRange(java.lang.String param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTimeRange(param0);
	}

	public static org.sakaiproject.time.api.Time newTime(long param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTime(param0);
	}

	public static org.sakaiproject.time.api.Time newTime()
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTime();
	}

	public static java.util.TimeZone getLocalTimeZone()
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.getLocalTimeZone();
	}

	public static boolean clearLocalTimeZone(String param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return false;

		return service.clearLocalTimeZone(param0);
	}

	public static java.util.GregorianCalendar getCalendar(java.util.TimeZone param0, int param1, int param2, int param3,
			int param4, int param5, int param6, int param7)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.getCalendar(param0, param1, param2, param3, param4, param5, param6, param7);
	}

	public static org.sakaiproject.time.api.Time newTime(java.util.GregorianCalendar param0)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return null;

		return service.newTime(param0);
	}

	public static boolean different(org.sakaiproject.time.api.Time param0, org.sakaiproject.time.api.Time param1)
	{
		org.sakaiproject.time.api.TimeService service = getInstance();
		if (service == null) return false;

		return service.different(param0, param1);
	}
}
