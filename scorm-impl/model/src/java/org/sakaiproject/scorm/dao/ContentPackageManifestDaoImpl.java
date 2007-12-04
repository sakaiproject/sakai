package org.sakaiproject.scorm.dao;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public abstract class ContentPackageManifestDaoImpl implements ContentPackageManifestDao {

	private static final String MANIFEST_RESOURCE_ID_PROPERTY = "manifest_resource_id";
	
	private static Log log = LogFactory.getLog(ContentPackageManifestDaoImpl.class);
	
	// Dependency injected lookup methods
	protected abstract ContentHostingService contentService();
	
	public ContentPackageManifest find(String courseId) {
		ContentPackageManifest manifest = null;
		
		try {
			ContentResource contentPackageResource = contentService().getResource(courseId);
			String manifestResourceId = (String)contentPackageResource.getProperties().get(MANIFEST_RESOURCE_ID_PROPERTY);
			ContentResource manifestResource = contentService().getResource(manifestResourceId);
			
			byte[] bytes = manifestResource.getContent();
		
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			
	        ObjectInputStream ie = new ObjectInputStream(in);
	        manifest = (ContentPackageManifest)ie.readObject();
	        ie.close();
	        in.close();
	        
	        manifest.setResourceId(contentPackageResource.getUrl());
	        
	        
		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return manifest;
	}

}
