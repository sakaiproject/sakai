package org.sakaiproject.scorm.model.api;

import java.io.InputStream;
import java.io.Serializable;

public class UnvalidatedResource implements Serializable {

	private static final long serialVersionUID = 1L;

	private InputStream contentStream;
	private String resourceId;
	private String title;
	
	
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

	public InputStream getContentStream() {
		return contentStream;
	}

	public void setContentStream(InputStream contentStream) {
		this.contentStream = contentStream;
	}
	
	
	
}
