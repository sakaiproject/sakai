package org.sakaiproject.scorm.service.api;

import java.io.InputStream;

import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;

public interface ScormResourceService {

	public void convertArchive(String resourceId, String manifestResourceId);
	
	public ContentPackageManifest getManifest(String resourceId, String manifestResourceId);
	
	public ContentPackageResource getResource(SessionBean sessionBean);
	
	public ContentPackageResource getResource(String resourceId, String path);
	
	public String getUrl(SessionBean sessionBean);
	
	public String putArchive(InputStream stream, String name, String mimeType);
	
	public String putManifest(String resourceId, ContentPackageManifest manifest);
	
	public String removeArchive(String resourceId);
	
	public void removeManifest(String resourceId, String manifestResourceId);
	
}
