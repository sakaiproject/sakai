/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.memory.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * MemoryService is a static Cover for the {@link org.sakaiproject.memory.api.MemoryService MemoryService}; see that interface for usage details.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class MemoryService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.memory.api.MemoryService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.memory.api.MemoryService) ComponentManager
						.get(org.sakaiproject.memory.api.MemoryService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.memory.api.MemoryService) ComponentManager
					.get(org.sakaiproject.memory.api.MemoryService.class);
		}
	}

	private static org.sakaiproject.memory.api.MemoryService m_instance = null;

	public static long getAvailableMemory()
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return 0;

		return service.getAvailableMemory();
	}

	public static void resetCachers() throws org.sakaiproject.memory.api.MemoryPermissionException
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return;

		service.resetCachers();
	}

	public static void registerCacher(org.sakaiproject.memory.api.Cacher param0)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return;

		service.registerCacher(param0);
	}

	public static void unregisterCacher(org.sakaiproject.memory.api.Cacher param0)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return;

		service.unregisterCacher(param0);
	}

	public static org.sakaiproject.memory.api.Cache newCache(org.sakaiproject.memory.api.CacheRefresher param0,
			java.lang.String param1)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newCache(param0, param1);
	}

	public static org.sakaiproject.memory.api.Cache newHardCache(org.sakaiproject.memory.api.CacheRefresher param0,
			java.lang.String param1)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newHardCache(param0, param1);
	}

	public static org.sakaiproject.memory.api.Cache newCache(org.sakaiproject.memory.api.CacheRefresher param0, long param1)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newCache(param0, param1);
	}

	public static org.sakaiproject.memory.api.Cache newHardCache(org.sakaiproject.memory.api.CacheRefresher param0, long param1)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newHardCache(param0, param1);
	}

	public static org.sakaiproject.memory.api.Cache newHardCache(long param0, java.lang.String param1)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newHardCache(param0, param1);
	}

	public static org.sakaiproject.memory.api.Cache newCache()
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newCache();
	}

	public static org.sakaiproject.memory.api.Cache newHardCache()
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newHardCache();
	}

	public static org.sakaiproject.memory.api.MultiRefCache newMultiRefCache(long param0)
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.newMultiRefCache(param0);
	}

	public static java.lang.String getStatus()
	{
		org.sakaiproject.memory.api.MemoryService service = getInstance();
		if (service == null) return null;

		return service.getStatus();
	}
}
