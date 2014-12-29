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

package org.sakaiproject.importer.cover;

import java.util.Collection;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ResetOnCloseInputStream;
import org.sakaiproject.component.cover.ComponentManager;
import java.io.InputStream;

public class ImportService {
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.importer.api.ImportService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = 
				(org.sakaiproject.importer.api.ImportService) ComponentManager.get(org.sakaiproject.importer.api.ImportService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.importer.api.ImportService) ComponentManager.get(org.sakaiproject.importer.api.ImportService.class);
		}
	}
	private static org.sakaiproject.importer.api.ImportService m_instance = null;
	
	public static void doImportItems(Collection importables, String siteId) {
		getInstance().doImportItems(importables, siteId);
	}
	
	public static boolean isValidArchive(ResetOnCloseInputStream archiveFileData) {
		return getInstance().isValidArchive(archiveFileData);
	}
	
	public static ImportDataSource parseFromFile(ResetOnCloseInputStream archiveFileData) {
		return getInstance().parseFromFile(archiveFileData);
	}

}
