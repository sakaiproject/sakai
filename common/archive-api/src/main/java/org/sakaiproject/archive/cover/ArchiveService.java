/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * ArchiveService is a static Cover for the {@link org.sakaiproject.archive.api.ArchiveService ArchiveService}; see that interface for usage details.
 * </p>
 */
public class ArchiveService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.archive.api.ArchiveService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.archive.api.ArchiveService) ComponentManager
						.get(org.sakaiproject.archive.api.ArchiveService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.archive.api.ArchiveService) ComponentManager
					.get(org.sakaiproject.archive.api.ArchiveService.class);
		}
	}

	private static org.sakaiproject.archive.api.ArchiveService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.archive.api.ArchiveService.SERVICE_NAME;

	public static java.lang.String archive(java.lang.String param0)
	{
		org.sakaiproject.archive.api.ArchiveService service = getInstance();
		if (service == null) return null;

		return service.archive(param0);
	}

	public static java.lang.String merge(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.archive.api.ArchiveService service = getInstance();
		if (service == null) return null;

		return service.merge(param0, param1, param2);
	}
}
