package org.sakaiproject.scorm.model.api;

import java.io.InputStream;

public class ContentPackageResource {

	private InputStream inputStream;
	private String mimeType;
	
	public ContentPackageResource() {}
	
	public ContentPackageResource(InputStream inputStream, String mimeType) {
		this.inputStream = inputStream;
		this.mimeType = mimeType;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	

}
