package org.sakaiproject.scorm.service.chh.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.scorm.content.impl.ScormCHH;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.sakai.impl.SakaiResourceService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;

public abstract class CHHResourceService extends SakaiResourceService {

	private static Log log = LogFactory.getLog(SakaiResourceService.class);

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
	private static final String MANIFEST_RESOURCE_ID_PROPERTY = "manifest_resource_id";
	
	protected abstract ContentHostingService contentService();
	protected abstract ToolManager toolManager();
	protected abstract ScormCHH scormCHH();
	
	
	public void convertArchive(String resourceId, String manifestResourceId) {
		try {
			ContentResourceEdit modify = contentService().editResource(resourceId);
	
			modify.setContentHandler(scormCHH());
			modify.setResourceType("org.sakaiproject.content.types.scormContentPackage");
	        
			ResourcePropertiesEdit props = modify.getPropertiesEdit();
			
			props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ScormCHH");
			props.addProperty(MANIFEST_RESOURCE_ID_PROPERTY, manifestResourceId);
			
			int noti = NotificationService.NOTI_NONE;
	        contentService().commitResource(modify, noti);
		} catch (Exception e) {
			log.error("Unable to convert archive to a Scorm content package", e);
		}
	}
	
	public ContentPackageManifest getManifest(String resourceId, String manifestResourceId) {
		ContentPackageManifest manifest = null;
		
		try {
			ContentResource contentPackageResource = contentService().getResource(resourceId);
			manifestResourceId = (String)contentPackageResource.getProperties().get(MANIFEST_RESOURCE_ID_PROPERTY);
			
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
	
	public ContentPackageResource getResource(String resourceId, String path) {
		String fullResourceId = new StringBuilder(resourceId).append("/").append(path).toString();
		
		try {
			ContentResource resource = contentService().getResource(fullResourceId);
		
			InputStream stream = resource.streamContent();
			
			return new ContentPackageResource(stream, resource.getContentType());

		} catch (Exception e) {
			log.error("Failed to retrieve resource from content hosting ", e);
		}
	
		return null;
	}
	
	public String getUrl(SessionBean sessionBean) {
		if (null != sessionBean.getLaunchData()) {
			String launchLine = sessionBean.getLaunchData().getLaunchLine();
			String baseUrl = sessionBean.getBaseUrl();
			StringBuffer fullPath = new StringBuffer().append(baseUrl);
			
			if (!baseUrl.endsWith(Entity.SEPARATOR) && !launchLine.startsWith(Entity.SEPARATOR))
				fullPath.append(Entity.SEPARATOR);

			fullPath.append(launchLine);
						
			return fullPath.toString();
		}
		return null;
	}

	public String putArchive(InputStream stream, String name, String mimeType) {
		String siteId = toolManager().getCurrentPlacement().getContext();
		String collectionId = contentService().getSiteCollection(siteId);
		
		String fileName = new String(name);
		int extIndex = fileName.lastIndexOf('.');
		String basename = fileName.substring(0, extIndex);
		String extension = fileName.substring(extIndex);

		ContentResourceEdit edit = null;
		try {
			edit = contentService().addResource(collectionId,Validator.escapeResourceName(basename),Validator.escapeResourceName(extension), MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				
			edit.setContent(stream);
			edit.setContentType(mimeType);
        
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
	        		
	        contentService().commitResource(edit);			

	        return edit.getId();
		} catch (Exception e) {
			if (edit != null)
				contentService().cancelResource(edit);
			
			log.error("Failed to place resources in Sakai content repository", e);
		}
		
		return null;
	}
		
	public String removeArchive(String resourceId) {		
		String manifestResourceId = null;
		
		try {
			ContentResourceEdit edit = contentService().editResource(resourceId);
			
			if (edit != null) {
				ResourceProperties props = edit.getProperties();
				manifestResourceId = props.getProperty(MANIFEST_RESOURCE_ID_PROPERTY);
			
				contentService().removeResource(edit);
			}
		} catch (Exception e) {
			log.error("Unable to remove archive: " + resourceId, e);
		}
		
		return manifestResourceId;
	}
	
	public void removeManifest(String resourceId, String manifestResourceId) {
		try {
			if (manifestResourceId != null) {
				ContentResourceEdit manifestEdit = contentService().editResource(manifestResourceId);
				
				if (manifestEdit != null) 
					contentService().removeResource(manifestEdit);
			}
		} catch (Exception e) {
			log.error("Unable to remove manifest: " + manifestResourceId, e);
		}
	}
	
}
