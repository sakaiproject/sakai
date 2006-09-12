package org.sakaiproject.importer.impl.importables;

import org.sakaiproject.importer.api.Importable;

public abstract class AbstractImportable implements Importable {
	
	protected String guid;
	protected String legacyGroup;
	protected String contextPath;
	protected Importable parent;
	
	public Importable getParent() {
		return parent;
	}
	public void setParent(Importable parent) {
		this.parent = parent;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getLegacyGroup() {
		return legacyGroup;
	}
	public void setLegacyGroup(String legacyGroup) {
		this.legacyGroup = legacyGroup;
	}
	public String getContextPath() {
		return contextPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
}
