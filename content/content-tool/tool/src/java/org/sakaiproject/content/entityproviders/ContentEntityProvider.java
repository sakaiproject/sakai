package org.sakaiproject.content.entityproviders;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Entity provider for the Content / Resources tool
 */
@CommonsLog
public class ContentEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

	public final static String ENTITY_PREFIX = "content";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * 
	 * Get a list of resources in a site
	 * 
	 * site/siteId
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<ContentItem> getContentCollectionForSite(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("Content for site: " + siteId);
		}

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException("siteId a must be set in order to get the resources for a site, via the URL /content/site/siteId");
		}
		
		return getResources(siteId);
		
	}
	
	/**
	 * 
	 * Get a list of resources in a user's  workspace
	 * 
	 * user/eid
	 */
	@EntityCustomAction(action = "user", viewKey = EntityView.VIEW_LIST)
	public List<ContentItem> getContentCollectionForUserWorkspace(EntityView view) {
		
		// get userEid
		String userEid = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("Content for user workspace: " + userEid);
		}

		// check siteId supplied
		if (StringUtils.isBlank(userEid)) {
			throw new IllegalArgumentException("eid must be set in order to get the resources for a user's workspace, via the URL /content/user/eid");
		}
		
		//get Id for user based on supplied eid
		String userId = null;
		try {
			User u = userDirectoryService.getUserByEid(userEid);
			if(u != null){
				userId = u.getId();
			}
		} catch (UserNotDefinedException e) {
			throw new EntityNotFoundException("Invalid user: " + userEid, userEid);
		}
			
		//get user siteId
		String siteId = siteService.getUserSiteId(userId);
		
		//check user can access this site - specifically check here so we dont expose the site uuid in the main check
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid user workspace: " + userEid, userEid);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to user workspace: " + userEid, userEid);
		}
		
		return getResources(siteId);
		
	}
	
	/**
	 * 
	 * Get a list of resources in the current user's my workspace
	 * 
	 * user/eid
	 */
	@EntityCustomAction(action = "my", viewKey = EntityView.VIEW_LIST)
	public List<ContentItem> getContentCollectionForMyWorkspace(EntityView view) {
		
		if(log.isDebugEnabled()) {
			log.debug("Content for my workspace");
		}
		
		//get user
		String userId = userDirectoryService.getCurrentUser().getId();
		if(StringUtils.isBlank(userId)) {
			throw new SecurityException("You need to be logged in in order to access your workspace content items.");
		}
			
		//get user siteId
		String siteId = siteService.getUserSiteId(userId);
		
		return getResources(siteId);
		
	}
	
	/**
	 * Get the list of resources for a site. The API handles visibility checks automatically.
	 * 
	 * @param siteId could be normal worksite siteid or my workspace siteid
	 * @return
	 */
	private List<ContentItem> getResources(String siteId) {
		
		//check user can access this site
		Site site;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}
		
		//check user can access the tool, it might be hidden
		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.resources");
		if(toolConfig == null || !toolManager.isVisible(site, toolConfig)) {
			throw new EntityNotFoundException("No access to tool in site: " + siteId, siteId);
		}
		
		//get the items
		List<ContentItem> items = new ArrayList<ContentItem>();
			
		String currentSiteCollectionId = contentHostingService.getSiteCollection(siteId);
		log.debug("currentSiteCollectionId: " + currentSiteCollectionId);
			
		List<ContentResource> resources = contentHostingService.getAllResources(currentSiteCollectionId);
		
		for(ContentResource resource: resources) {
				
			//convert to our simplified object 
			ContentItem item = new ContentItem();
			
			ResourceProperties props = resource.getProperties();
			
			item.setTitle(props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			item.setDescription(props.getProperty(ResourceProperties.PROP_DESCRIPTION));
			item.setType(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
			item.setSize(Long.parseLong(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)));
			item.setUrl(resource.getUrl());
			item.setAuthor(getDisplayName(props.getProperty(ResourceProperties.PROP_CREATOR)));
			item.setModifiedDate(props.getProperty(ResourceProperties.PROP_MODIFIED_DATE));
			item.setContainer(resource.getContainingCollection().getReference());
			
			items.add(item);
		}
		
		return items;
	}
	
	
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON};
	}

	@Setter
	private ContentHostingService contentHostingService;

	@Setter
	private SiteService siteService;
	
	@Setter
	private ToolManager toolManager;
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private UserDirectoryService userDirectoryService;
	
	
	

	
	/**
	 * Simplified helper class to represent an individual content item
	 */
	public static class ContentItem {
		
		@Getter @Setter
		private String title;
		
		@Getter @Setter
		private String description;
		
		@Getter @Setter
		private String url;
		
		@Getter @Setter
		private String type;
		
		@Getter @Setter
		private long size;
		
		@Getter @Setter
		private String author;
		
		@Getter @Setter
		private String modifiedDate;
		
		@Getter @Setter
		private String container;
		
	}
	
	/**
	 * Helper to get the displayname for a user.
	 * @param uuid uuid of the user
	 * @return displayname or null. We dont want to expose the uuid.
	 */
	private String getDisplayName(String uuid) {
		try {
			return userDirectoryService.getUser(uuid).getDisplayName();
		} catch (UserNotDefinedException e) {
			//dont throw, return null
			return null;
		}
	}
	
	
}
