package org.sakaiproject.importer.cover;

import java.util.Collection;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.component.cover.ComponentManager;

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
	
	public static boolean isValidArchive(byte[] archiveFileData) {
		return getInstance().isValidArchive(archiveFileData);
	}
	
	public static ImportDataSource parseFromFile(byte[] archiveFileData) {
		return getInstance().parseFromFile(archiveFileData);
	}

}
