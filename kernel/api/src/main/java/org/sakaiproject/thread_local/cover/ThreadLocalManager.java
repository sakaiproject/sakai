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

package org.sakaiproject.thread_local.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * ThreadLocalManager is a static Cover for the {@link org.sakaiproject.thread_local.api.ThreadLocalManager ThreadLocalManager}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class ThreadLocalManager
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.thread_local.api.ThreadLocalManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.thread_local.api.ThreadLocalManager) ComponentManager
						.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.thread_local.api.ThreadLocalManager) ComponentManager
					.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class);
		}
	}

	private static org.sakaiproject.thread_local.api.ThreadLocalManager m_instance = null;

	public static void set(java.lang.String param0, java.lang.Object param1)
	{
		org.sakaiproject.thread_local.api.ThreadLocalManager manager = getInstance();
		if (manager == null) return;

		manager.set(param0, param1);
	}

	public static void clear()
	{
		org.sakaiproject.thread_local.api.ThreadLocalManager manager = getInstance();
		if (manager == null) return;

		manager.clear();
	}

	public static java.lang.Object get(java.lang.String param0)
	{
		org.sakaiproject.thread_local.api.ThreadLocalManager manager = getInstance();
		if (manager == null) return null;

		return manager.get(param0);
	}
}
