package org.sakaiproject.importer.api;

public interface ImportFileParser {
	boolean isValidArchive(byte[] fileData);
	ImportDataSource parse(byte[] fileData, String unArchiveLocation);
}
