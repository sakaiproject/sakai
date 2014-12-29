/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.cluster.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * ClusterService is a static Cover for the {@link org.sakaiproject.cluster.api.ClusterService ClusterService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class ClusterService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.cluster.api.ClusterService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.cluster.api.ClusterService) ComponentManager
						.get(org.sakaiproject.cluster.api.ClusterService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.cluster.api.ClusterService) ComponentManager
					.get(org.sakaiproject.cluster.api.ClusterService.class);
		}
	}

	private static org.sakaiproject.cluster.api.ClusterService m_instance = null;

	public static java.util.List<String> getServers()
	{
		org.sakaiproject.cluster.api.ClusterService service = getInstance();
		if (service == null) return null;

		return service.getServers();
	}
}
