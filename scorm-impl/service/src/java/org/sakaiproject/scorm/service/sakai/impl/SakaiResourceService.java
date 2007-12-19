package org.sakaiproject.scorm.service.sakai.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;

public abstract class SakaiResourceService implements ScormResourceService {

	private static Log log = LogFactory.getLog(SakaiResourceService.class);

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
	private static final String MANIFEST_RESOURCE_ID_PROPERTY = "manifest_resource_id";
	
	protected abstract ContentHostingService contentService();
	protected abstract ToolManager toolManager();
	
	public void convertArchive(String resourceId, String manifestResourceId) {
		
		log.warn("NOT IMPLEMENTED - SakaiResourceService.convertArchive!!!");
		
		// FIXME: Need to unpack the archive and place each file individually into the sakai repository
		// (Or else, do the unpacking each time the content package is launched and store in memory for that instance... seems harder)
		
		/*try {
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
		}*/
	}
	
	public ContentPackageManifest getManifest(String resourceId, String manifestResourceId) {
		ContentPackageManifest manifest = null;
		
		try {
			if (manifestResourceId == null) {
				ContentResource contentPackageResource = contentService().getResource(resourceId);
				manifestResourceId = (String)contentPackageResource.getProperties().get(MANIFEST_RESOURCE_ID_PROPERTY);
			}
			ContentResource manifestResource = contentService().getResource(manifestResourceId);
			
			byte[] bytes = manifestResource.getContent();
		
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			
	        ObjectInputStream ie = new ObjectInputStream(in);
	        manifest = (ContentPackageManifest)ie.readObject();
	        ie.close();
	        in.close();
	        
	        // TODO: Make sure we don't need this anymore
	        //manifest.setResourceId(contentPackageResource.getUrl());
	        
	        
		} catch (Exception ioe) {
			log.error("Caught io exception reading manifest from file!", ioe);
		}
		
		return manifest;
	}
	
	public ContentPackageResource getResource(String resourceId, String path) {
	
		log.warn("NOT IMPLEMENTED -- SakaiResourceService.getResource!!!");
		
		//FIXME: Need to grab the resource -- either by unpacking the archive on the fly, or by referring to it as a shared resource
		// Or by grabbing it out of the content repository on its own.
		
		/*String fullResourceId = new StringBuilder(resourceId).append("/").append(path).toString();
		
		try {
			ContentResource resource = contentService().getResource(fullResourceId);
		
			InputStream stream = resource.streamContent();
			
			return new ContentPackageResource(stream, resource.getContentType());

		} catch (Exception e) {
			log.error("Failed to retrieve resource from content hosting ", e);
		}*/
	
		return null;
	}

	public ContentPackageResource getResource(SessionBean sessionBean) {
		return getResource(sessionBean.getCourseId(), sessionBean.getLaunchData().getLaunchLine());
	}
	
	public String getUrl(SessionBean sessionBean) {
		
		log.warn("NOT IMPLEMENTED -- SakaiResourceService.getUrl");
		
		//FIXME: This will probably be a Wicket Url based off the shared resource or content package request target logic
		
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
	
	public String putManifest(String resourceId, ContentPackageManifest manifest) {
		ContentResource resource = null;
		
		String name = "manifest.obj"; // + manifest.getTitle();
		String site = toolManager().getCurrentPlacement().getContext();
		String tool = "scorm";
		String type = "application/octet-stream";
				
		try {	
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(manifest);
			out.close();
			
			resource = contentService().addAttachmentResource(name, site, tool, type, byteOut.toByteArray(), null);
			
			return resource.getId();
		} catch (Exception soe) {
			log.error("Caught an exception adding an attachment resource!", soe);
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
