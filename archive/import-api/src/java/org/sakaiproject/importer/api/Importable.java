package org.sakaiproject.importer.api;

public interface Importable {
	String getGuid();
	String getTypeName();
	String getLegacyGroup();
	String getContextPath();
	Importable getParent();
	void setParent(Importable parent);
	void setLegacyGroup(String legacyGroup);
	void setContextPath(String path);
}
