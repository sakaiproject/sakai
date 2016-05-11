package org.sakaiproject.content.entityproviders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.tool.ListItem;
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
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Entity provider for the Content / Resources tool
 */
@Slf4j
public class ContentEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

	public final static String ENTITY_PREFIX = "content";
	public static final String PREFIX = "resources.";
	public static final String SYS = "sys.";
	private static final String STATE_RESOURCES_TYPE_REGISTRY = PREFIX + SYS + "type_registry";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	public boolean entityExists(String id) {
		boolean rv = false;
		
        // check whether this is a folder first
		try
		{
			ContentCollection collection = contentHostingService.getCollection(id);
			rv = true;
		}
		catch (IdUnusedException e)
		{
			// not a collection id, will check for resource id later
			if(log.isDebugEnabled()) {
				log.debug(this + " entityeExists: error getting collection " + id + " " + e.getMessage());
			}
		}
		catch (TypeException e)
		{
			// not a collection type, will check for resource type later
			if(log.isDebugEnabled()) {
				log.debug(this + " entityeExists: error getting collection " + id + " " + e.getMessage());
			}
		}
		catch (PermissionException e)
		{
			// not allowed to get collection, will check for resource later
			if(log.isDebugEnabled()) {
				log.debug(this + " entityeExists: error getting collection " + id + " " + e.getMessage());
			}
		}
		
		if (!rv)
		{
			// now check for resource 
			try
			{
				ContentResource resource = contentHostingService.getResource(id);
				rv = true;
			}
			catch (IdUnusedException e)
			{
				if(log.isDebugEnabled()) {
					log.debug(this + " entityeExists: error getting resource " + id + " " + e.getMessage());
				}
			}
			catch (TypeException e)
			{
				if(log.isDebugEnabled()) {
					log.debug(this + " entityeExists: error getting resource " + id + " " + e.getMessage());
				}
			}
			catch (PermissionException e)
			{
				if(log.isDebugEnabled()) {
					log.debug(this + " entityeExists: error getting resource " + id + " " + e.getMessage());
				}
			}
		}
		return rv;
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
		
		// return the ListItem list for the site
		return getSiteListItems(siteId);
		
	}

	private List<ContentItem> getSiteListItems(String siteId) {
		List<ContentItem> rv = new ArrayList<ContentItem>();
		String wsCollectionId = contentHostingService.getSiteCollection(siteId);
		boolean allowUpdateSite = siteService.allowUpdateSite(siteId);
      
		try
        {
			// mark the site collection as expanded
			Set<String> expandedCollections = new CopyOnWriteArraySet<String>();
			
        	ContentCollection wsCollection = contentHostingService.getCollection(wsCollectionId);
			ListItem wsRoot = ListItem.getListItem(wsCollection, null, 
					(ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry"), 
					true, 
					expandedCollections, 
					null, null, 0, 
					null, 
					false, null);
			List<ListItem> wsRootList = wsRoot.convert2list();
			for (ListItem lItem : wsRootList)
			{
				String id = lItem.getId();
				if (lItem.isCollection())
				{
					try
					{
						ContentCollection collection = contentHostingService.getCollection(id);
						//convert to our simplified object 
						ContentItem item = new ContentItem();
						item.setType("collection");
						item.setSize(contentHostingService.getCollectionSize(id));
						if (allowUpdateSite) // to be consistent with UI
							item.setQuota(Long.toString(contentHostingService.getQuota(collection)));
						item.setUsage(Long.toString(collection.getBodySizeK() * 1024));
						
						List<String> collectionMembers = collection.getMembers();
						if (collectionMembers != null)
						{
							item.setNumChildren(collectionMembers.size());
						}
						
						ResourceProperties props = collection.getProperties();
						// set the proper ContentItem values
						setContentItemValues(collection, item, props);
						
						if (item.getTitle() == null && id.equals(wsCollectionId))
						{
							// for the root level collection, use site title as the collection title
							try
							{
								Site site = siteService.getSite(siteId);
								item.setTitle(site.getTitle());
							}
							catch (IdUnusedException e)
							{
								log.warn(this + " getSiteListItems: Cannot find site with id=" + siteId);
							}
						}
						
						rv.add(item);
					}
					catch (IdUnusedException e)
					{
						if(log.isDebugEnabled()) {
							log.debug(this + " getSiteListItems: error getting collection " + id + " " + e.getMessage());
						}
					}
					catch (TypeException e)
					{
						if(log.isDebugEnabled()) {
							log.debug(this + " getSiteListItems: error getting collection " + id + " " + e.getMessage());
						}
					}
					catch (PermissionException e)
					{
						if(log.isDebugEnabled()) {
							log.debug(this + " getSiteListItems: error getting collection " + id + " " + e.getMessage());
						}
					}
				}
				else
				{
					try
					{
						ContentResource resource = contentHostingService.getResource(id);
						
						//convert to our simplified object 
						ContentItem item = new ContentItem();
						
						ResourceProperties props = resource.getProperties();
						item.setType(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
						try
						{
							item.setSize(Long.parseLong(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)));
						}
						catch (NumberFormatException nException)
						{
							log.warn(this + " getSiteListItems problem of getting resource length for " + id);
						}
						
						// set the proper ContentItem values
						setContentItemValues(resource, item, props);
						
						rv.add(item);
					}
					catch (IdUnusedException e)
					{
						if(log.isDebugEnabled()) {
							log.debug(this + " getSiteListItems: error getting resource " + id + " " + e.getMessage());
						}
					}
					catch (TypeException e)
					{
						if(log.isDebugEnabled()) {
							log.debug(this + " getSiteListItems: error getting resource " + id + " " + e.getMessage());
						}
					}
					catch (PermissionException e)
					{
						if(log.isDebugEnabled()) {
							log.debug(this + " getSiteListItems: error getting resource " + id + " " + e.getMessage());
						}
					}
				}
			}
        }
        catch (IdUnusedException e)
        {
        	if(log.isDebugEnabled()) {
				log.debug(this + " getSiteListItems: error getting site collection " + wsCollectionId + " " + e.getMessage());
			}
        }
        catch (TypeException e)
        {
        	if(log.isDebugEnabled()) {
				log.debug(this + " getSiteListItems: error getting site collection " + wsCollectionId + " " + e.getMessage());
			}
        }
        catch (PermissionException e)
        {
        	if(log.isDebugEnabled()) {
				log.debug(this + " getSiteListItems: error getting site collection " + wsCollectionId + " " + e.getMessage());
			}
        }
		return rv;
	}
	
	/**
	 * set various attributes of ContentItem object
	 * @param entity
	 * @param item
	 * @param props
	 */
	private void setContentItemValues(ContentEntity entity,
			ContentItem item, ResourceProperties props) {
		item.setTitle(props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
		item.setDescription(props.getProperty(ResourceProperties.PROP_DESCRIPTION));
		item.setUrl(entity.getUrl());
		String authorId = props.getProperty(ResourceProperties.PROP_CREATOR);
		item.setAuthorId(authorId);
		item.setAuthor(getDisplayName(authorId));
		item.setModifiedDate(props.getProperty(ResourceProperties.PROP_MODIFIED_DATE));
		item.setContainer(entity.getContainingCollection().getReference());
		item.setVisible( !entity.isHidden() && entity.isAvailable() );
		item.setHidden( entity.isHidden() );
		if(entity.getReleaseDate() != null) {
			item.setFromDate(entity.getReleaseDate().toStringGmtFull());
		}
		if(entity.getRetractDate()!=null) {
			item.setEndDate(entity.getRetractDate().toStringGmtFull());
		}
		item.setCopyrightAlert(props.getProperty(props.getNamePropCopyrightAlert()) );
	}
	
	/**
	 * 
	 * Get a list of resources in a user's  workspace
	 * 
	 * user/eid
	 */
	@EntityCustomAction(action = "user", viewKey = EntityView.VIEW_LIST)
	public List<ContentItem> getContentCollectionForUserWorkspace(EntityView view) {
		
		// this parameter can be either user's id or eid
		String userEidOrId = view.getPathSegment(2);

		if(log.isDebugEnabled()) {
			log.debug("Content for user workspace: " + userEidOrId);
		}

		// check siteId supplied
		if (StringUtils.isBlank(userEidOrId)) {
			throw new IllegalArgumentException("eid must be set in order to get the resources for a user's workspace, via the URL /content/user/eid");
		}
		
		//get Id for user based on supplied eid
		String userId = null;
		try {
			User u = userDirectoryService.getUserByEid(userEidOrId);
			if(u != null){
				userId = u.getId();
			}
		} catch (UserNotDefinedException e) {
			// test whether this is user id
			try {
				User u = userDirectoryService.getUser(userEidOrId);
				if(u != null){
					userId = u.getId();
				}
			} catch (UserNotDefinedException ee) {
				if(log.isDebugEnabled()) {
					log.debug(this + " getContentCollectionForUserWorkspace: error user " + userEidOrId + " " + e.getMessage());
				}
				throw new EntityNotFoundException(this + " getContentCollectionForUserWorkspace Invalid user: " + userEidOrId + " for either eid or id", e.getMessage());
			}
		}
			
		//get user siteId
		String siteId = siteService.getUserSiteId(userId);
		
		// return the ListItem list for the site
		return getSiteListItems(siteId);
		
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
		
		// return the ListItem list for the site
		return getSiteListItems(siteId);
		
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
			item.setSize(Long.parseLong(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)));
			item.setType(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
			
			// set the proper ContentItem values
			setContentItemValues(resource, item, props);
			
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
		private String authorId;
		
		@Getter @Setter
		private String modifiedDate;

		@Getter @Setter
		private String container;

		@Getter @Setter
		private boolean isVisible;

		@Getter @Setter
		private boolean isHidden;

		@Getter @Setter
		private long numChildren=0;

		@Getter @Setter
		private String fromDate;

		@Getter @Setter
		private String endDate;
  
		@Getter @Setter
		private String copyrightAlert;
      
		@Getter @Setter
		private String quota;
		
		@Getter @Setter
		private String usage;
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
			if(log.isDebugEnabled()) {
				log.debug(this +  " getDisplayName error getting user " + uuid + " " + e.getMessage());
			}
			//dont throw, return null
			return null;
		}
	}
	
	
}
