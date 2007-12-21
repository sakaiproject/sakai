package org.sakaiproject.scorm.model.api;

import java.util.Properties;

public class Learner {

	private String id;
	private String displayName;
	private String displayId;
	private String sortName;
	private Properties properties;
	
	public Learner(String id) {
		this.id = id;
	}
	
	public Learner(String id, String displayName, String displayId) {
		this.id = id;
		this.displayName = displayName;
		this.displayId = displayId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayId() {
		return displayId;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
}
