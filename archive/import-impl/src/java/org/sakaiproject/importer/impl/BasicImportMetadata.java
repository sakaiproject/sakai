package org.sakaiproject.importer.impl;

import org.sakaiproject.archive.api.ImportMetadata;

public class BasicImportMetadata implements ImportMetadata {
	private String fileName;
	private String id;
	private String legacyTool;
	private String sakaiServiceName;
	private String sakaiTool;
	private boolean isMandatory;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isMandatory() {
		return isMandatory;
	}
	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}
	public String getLegacyTool() {
		return legacyTool;
	}
	public void setLegacyTool(String legacyTool) {
		this.legacyTool = legacyTool;
	}
	public String getSakaiServiceName() {
		return sakaiServiceName;
	}
	public void setSakaiServiceName(String sakaiServiceName) {
		this.sakaiServiceName = sakaiServiceName;
	}
	public String getSakaiTool() {
		return sakaiTool;
	}
	public void setSakaiTool(String sakaiTool) {
		this.sakaiTool = sakaiTool;
	}

}
