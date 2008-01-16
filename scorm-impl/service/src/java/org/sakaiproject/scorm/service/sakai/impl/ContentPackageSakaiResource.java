package org.sakaiproject.scorm.service.sakai.impl;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class ContentPackageSakaiResource extends ContentPackageResource {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ContentPackageSakaiResource.class);
	
	private ContentResource contentResource;
	
	public ContentPackageSakaiResource(String path, ContentResource contentResource) {
		super(path);
		this.contentResource = contentResource;
		this.setLength(contentResource.getContentLength());
	}
	
	@Override
	public InputStream getInputStream() throws ResourceNotFoundException {
		InputStream inputStream = null;
		
		try {
			inputStream = contentResource.streamContent();
		} catch (Exception e) {
			log.error("Could not stream content from this path: " + getPath(), e);
			throw new ResourceNotFoundException(getPath());
		}
		
		return inputStream;
	}

	@Override
	public String getMimeType() {
		return contentResource.getContentType();
	}
}
