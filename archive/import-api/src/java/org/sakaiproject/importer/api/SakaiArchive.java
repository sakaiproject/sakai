package org.sakaiproject.importer.api;

import java.util.Collection;

public interface SakaiArchive {

	void buildSourceFolder(Collection fnlList);
	String getSourceFolder();
}
