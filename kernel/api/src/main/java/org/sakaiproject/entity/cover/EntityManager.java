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

package org.sakaiproject.entity.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * EntityManager is a static Cover for the {@link org.sakaiproject.entity.api.EntityManager EntityManager}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class EntityManager
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.entity.api.EntityManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.entity.api.EntityManager) ComponentManager
						.get(org.sakaiproject.entity.api.EntityManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.entity.api.EntityManager) ComponentManager
					.get(org.sakaiproject.entity.api.EntityManager.class);
		}
	}

	private static org.sakaiproject.entity.api.EntityManager m_instance = null;

	public static void registerEntityProducer(org.sakaiproject.entity.api.EntityProducer param0, java.lang.String param1)
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return;

		service.registerEntityProducer(param0, param1);
	}

	public static java.util.List getEntityProducers()
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return null;

		return service.getEntityProducers();
	}

	public static org.sakaiproject.entity.api.Reference newReference(java.lang.String param0)
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return null;

		return service.newReference(param0);
	}

	public static org.sakaiproject.entity.api.Reference newReference(org.sakaiproject.entity.api.Reference param0)
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return null;

		return service.newReference(param0);
	}

	public static java.util.List newReferenceList()
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return null;

		return service.newReferenceList();
	}

	public static java.util.List newReferenceList(java.util.List param0)
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return null;

		return service.newReferenceList(param0);
	}

	public static boolean checkReference(java.lang.String param0)
	{
		org.sakaiproject.entity.api.EntityManager service = getInstance();
		if (service == null) return false;

		return service.checkReference(param0);
	}
}
