package org.sakaiproject.importer.api;

import java.util.Collection;

public interface ImportService {
	void doImportItems(Collection importables, String siteId);
	boolean isValidArchive(byte[] archiveFileData);
	ImportDataSource parseFromFile(byte[] archiveFileData);

}
