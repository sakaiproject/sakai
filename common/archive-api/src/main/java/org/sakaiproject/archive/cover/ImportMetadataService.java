/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
 * ...
 */
public class ImportMetadataService
{
	private static org.sakaiproject.archive.api.ImportMetadataService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.archive.api.ImportMetadataService.SERVICE_NAME;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.archive.api.ImportMetadataService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.archive.api.ImportMetadataService) ComponentManager
						.get(org.sakaiproject.archive.api.ImportMetadataService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.archive.api.ImportMetadataService) ComponentManager
					.get(org.sakaiproject.archive.api.ImportMetadataService.class);
		}
	}

	public static org.sakaiproject.archive.api.ImportMetadata getImportMapById(String id)
	{
		org.sakaiproject.archive.api.ImportMetadataService service = getInstance();
		if (service == null) return null;

		return service.getImportMapById(id);
	}

	public static java.util.List getImportMetadataElements(org.w3c.dom.Document doc)
	{
		org.sakaiproject.archive.api.ImportMetadataService service = getInstance();
		if (service == null) return null;

		return service.getImportMetadataElements(doc);
	}

	/**
	 * @param username
	 * @param siteDoc
	 * @return
	 */
	public static boolean hasMaintainRole(String username, org.w3c.dom.Document siteDoc)
	{
		org.sakaiproject.archive.api.ImportMetadataService service = getInstance();
		if (service == null) return false;

		return service.hasMaintainRole(username, siteDoc);
	}
}
