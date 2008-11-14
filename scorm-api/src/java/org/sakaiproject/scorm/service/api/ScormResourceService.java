package org.sakaiproject.scorm.service.api;

import java.io.InputStream;
import java.util.List;

import org.sakaiproject.scorm.exceptions.InvalidArchiveException;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public interface ScormResourceService {

	public String convertArchive(String resourceId, String title) throws InvalidArchiveException;
	
	public Archive getArchive(String resourceId);
	
	public InputStream getArchiveStream(String resourceId);

	public int getMaximumUploadFileSize();
	
	public String getResourcePath(String resourceId, String launchLine);
	
	public List<ContentPackageResource> getResources(String archiveResourceId);
		
	public List<Archive> getUnvalidatedArchives();
	
	public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden, int priority);

	public void removeResources(String collectionId) throws ResourceNotDeletedException;
	

}
