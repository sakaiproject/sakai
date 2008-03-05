package org.sakaiproject.scorm.service.standalone.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class ContentPackageFile extends ContentPackageResource {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ContentPackageFile.class);
	
	private String archiveResourceId;
	private File file;
	
	public ContentPackageFile(String archiveResourceId, String path, File file) {
		super(path);
		this.archiveResourceId = archiveResourceId;
		this.file = file;
		this.setLength(file.length());
		this.setLastModified(file.lastModified());
	}
	
	@Override
	public InputStream getInputStream() throws ResourceNotFoundException {
		InputStream inputStream = null;
		
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.error("Failed to retrieve file at " + file, e);
			throw new ResourceNotFoundException(getPath());
		}
		return inputStream;
	}

	/*public String getPath() {
		String fullPath = new StringBuilder("/").append(archiveResourceId).append(super.getPath()).toString();
		return fullPath.replace(" ", "%20");
	}*/

	@Override
	public String getMimeType() {
		String mimeType = new MimetypesFileTypeMap().getContentType(file);
		
		if (file.getName().endsWith(".css"))
			mimeType = "text/css";
		else if (file.getName().endsWith(".swf"))
			mimeType = "application/x-Shockwave-Flash";
		
		return mimeType;
	}
}
