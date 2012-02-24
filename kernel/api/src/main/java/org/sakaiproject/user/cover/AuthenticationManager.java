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

package org.sakaiproject.user.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * AuthenticationManager is a static Cover for the {@link org.sakaiproject.user.api.AuthenticationManager AuthenticationManager}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class AuthenticationManager
{
	/** Possibly cached component instance. */
	private static org.sakaiproject.user.api.AuthenticationManager m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.user.api.AuthenticationManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.user.api.AuthenticationManager) ComponentManager
						.get(org.sakaiproject.user.api.AuthenticationManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.user.api.AuthenticationManager) ComponentManager
					.get(org.sakaiproject.user.api.AuthenticationManager.class);
		}
	}

	public static org.sakaiproject.user.api.Authentication authenticate(org.sakaiproject.user.api.Evidence param0)
			throws org.sakaiproject.user.api.AuthenticationException
	{
		org.sakaiproject.user.api.AuthenticationManager manager = getInstance();
		if (manager == null) return null;

		return manager.authenticate(param0);
	}
}
