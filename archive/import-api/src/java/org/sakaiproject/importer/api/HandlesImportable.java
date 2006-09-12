package org.sakaiproject.importer.api;

public interface HandlesImportable {
	boolean canHandleType(String typeName);
	void handle(Importable thing, String siteId);

}
