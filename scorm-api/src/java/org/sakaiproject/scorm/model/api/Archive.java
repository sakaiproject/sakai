package org.sakaiproject.scorm.model.api;

import java.io.Serializable;


public class Archive implements Serializable {
	private static final long serialVersionUID = 1L;

	private String resourceId;
	private String title;
	private boolean isHidden;
	private boolean isValidated;
	private String mimeType;
	private String path;
	
	public Archive(String resourceId, String title) {
		this.resourceId = resourceId;
		this.title = title;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isValidated() {
		return isValidated;
	}

	public void setValidated(boolean isValidated) {
		this.isValidated = isValidated;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}

