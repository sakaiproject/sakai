/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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

package org.sakaiproject.log.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * LogConfigurationManager is a static Cover for the {@link org.sakaiproject.log.api.LogConfigurationManager LogConfigurationManager}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class LogConfigurationManager
{
	/** Possibly cached component instance. */
	private static org.sakaiproject.log.api.LogConfigurationManager m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.log.api.LogConfigurationManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.log.api.LogConfigurationManager) ComponentManager
						.get(org.sakaiproject.log.api.LogConfigurationManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.log.api.LogConfigurationManager) ComponentManager
					.get(org.sakaiproject.log.api.LogConfigurationManager.class);
		}
	}

	public static boolean setLogLevel(java.lang.String param0, java.lang.String param1)
			throws org.sakaiproject.log.api.LogPermissionException
	{
		org.sakaiproject.log.api.LogConfigurationManager manager = getInstance();
		if (manager == null) return false;

		return manager.setLogLevel(param0, param1);
	}
}
