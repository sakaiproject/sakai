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

	public String getMimeType() {
		return mimeType;
	}

	public String getPath() {
		return path;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getTitle() {
		return title;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public boolean isValidated() {
		return isValidated;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setValidated(boolean isValidated) {
		this.isValidated = isValidated;
	}

}
