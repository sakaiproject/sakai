package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.Properties;

public class Learner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

	public String getDisplayId() {
		return displayId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getId() {
		return id;
	}

	public Properties getProperties() {
		return properties;
	}

	public String getSortName() {
		return sortName;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

}
