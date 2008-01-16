package org.sakaiproject.scorm.service.sakai.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.scorm.content.impl.ScormCHH;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.tool.api.ToolManager;

public abstract class CHHResourceService extends SakaiResourceService {

	private static Log log = LogFactory.getLog(SakaiResourceService.class);

	private static final String MANIFEST_RESOURCE_ID_PROPERTY = "manifest_resource_id";
	
	protected abstract ContentHostingService contentService();
	protected abstract ToolManager toolManager();
	protected abstract ScormCHH scormCHH();
	protected abstract ServerConfigurationService configurationService();
	
	public String convertArchive(String resourceId) {
		try {
			ContentResourceEdit modify = contentService().editResource(resourceId);
			
			modify.setContentHandler(scormCHH());
			modify.setResourceType("org.sakaiproject.content.types.scormContentPackage");
	        
			ResourcePropertiesEdit props = modify.getPropertiesEdit();
			
			props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ScormCHH");
			//props.addProperty(MANIFEST_RESOURCE_ID_PROPERTY, manifestResourceId);
			
			int noti = NotificationService.NOTI_NONE;
	        contentService().commitResource(modify, noti);
		} catch (Exception e) {
			log.error("Unable to convert archive to a Scorm content package", e);
		}
		
		return resourceId;
	}
		
	public ContentPackageResource getResource(String resourceId, String path) {
		String fullResourceId = new StringBuilder(resourceId).append("/").append(path).toString();
		
		try {
			ContentResource resource = contentService().getResource(fullResourceId);

			return new ContentPackageSakaiResource(path, resource);
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
	

	
	
}
