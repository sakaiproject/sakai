/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.entityproviders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.tool.ListItem;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.model.EntityContent;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
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
	private static final String PARAMETER_DEPTH = "depth";
	private static final String PARAMETER_TIMESTAMP = "timestamp";

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
	@EntityCustomAction(action="resources", viewKey=EntityView.VIEW_LIST)
	public List<EntityContent> getResources(EntityView view, Map<String, Object> params)
			throws EntityPermissionException {
		Map<String, Object> parameters = getQueryMap((String)params.get("queryString"));
		Time timeStamp = getTime((String)parameters.get(PARAMETER_TIMESTAMP));

		int requestedDepth = 1;
		int currentDepth = 0;
		if (parameters.containsKey(PARAMETER_DEPTH)) {
			if ("all".equals((String)parameters.get(PARAMETER_DEPTH))) {
				requestedDepth = Integer.MAX_VALUE;
			} else {
				requestedDepth = Integer.parseInt((String)parameters.get(PARAMETER_DEPTH));
			}
		}

		String[] segments = view.getPathSegments();

		StringBuffer resourceId = new StringBuffer();
		for (int i=2; i<segments.length; i++) {
			resourceId.append("/"+segments[i]);
		}
		resourceId.append("/");

		Reference reference = entityManager.newReference(
				ContentHostingService.REFERENCE_ROOT+resourceId.toString());

		// We could have used contentHostingService.getAllEntities(id) bit it doesn't do
		// permission checks on any contained resources (documentation wrong).
		// contentHostingService.getAllResources(String id) does do permission checks
		// but it doesn't include collections in it's returned list.
		// Also doing the recursion ourselves means that we don't loads lots of entities
		// when the depth of recursion is low.
		ContentCollection collection= null;
		try {
			collection = contentHostingService.getCollection(reference.getId());

		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("IdUnusedException in Resource Entity Provider");

		} catch (TypeException e) {
			throw new IllegalArgumentException("TypeException in Resource Entity Provider");

		} catch (PermissionException e) {
			throw new SecurityException("PermissionException in Resource Entity Provider");
		}

		List<EntityContent> resourceDetails = new ArrayList<EntityContent>();
		if (collection!=null) {
			EntityContent resourceDetail = getResourceDetails(collection, currentDepth, requestedDepth, timeStamp);
			if (resourceDetail != null) {
				resourceDetails.add(resourceDetail);
			} else {
				log.error("Initial permission check passed but subsequent permission check failed on "+ reference.getId());
			}
		}
		return resourceDetails;
	}

	/**
	 *
	 * @param entity The entity to load details of.
	 * @param currentDepth How many collections we have already processed
	 * @param requestedDepth The maximum number depth of the tree to scan.
	 * @param timeStamp All returned details must be newer than this timestamp.
	 * @return EntityContent containing details of all resources the user can access.
	 * <code>null</code> is returned if the current user isn't allowed to access the resource.
	 */
	private EntityContent getResourceDetails(
			ContentEntity entity, int currentDepth, int requestedDepth, Time timeStamp) {
		boolean allowed = (entity.isCollection()) ?
				contentHostingService.allowGetCollection(entity.getId()) :
				contentHostingService.allowGetResource(entity.getId());
		if (!allowed) {
			// If the user isn't allowed to see this we return null.
			return null;
		}
		EntityContent tempRd = EntityDataUtils.getResourceDetails(entity);

		// If it's a collection recurse down into it.
		if ((requestedDepth > currentDepth) && entity.isCollection()) {

			ContentCollection collection = (ContentCollection)entity;
			// This is all members, no permission check has been done yet.
			List<ContentEntity> contents = collection.getMemberResources();

			Comparator comparator = getComparator(entity);
			if (null != comparator) {
				Collections.sort(contents, comparator);
			}

			for (Iterator<ContentEntity> i = contents.iterator(); i.hasNext();) {
				ContentEntity content = i.next();
				EntityContent resource = getResourceDetails(content, currentDepth+1, requestedDepth, timeStamp);

				if (resource != null && resource.after(timeStamp)) {
					tempRd.addResourceChild(resource);
				}
			}
		}

		return tempRd;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private Comparator getComparator(ContentEntity entity) {

		boolean hasCustomSort = false;
		try	{
			hasCustomSort = entity.getProperties().getBooleanProperty(
					ResourceProperties.PROP_HAS_CUSTOM_SORT);

		} catch(Exception e) {
			// ignore -- let value of hasCustomSort stay false
		}

		if(hasCustomSort) {
			return contentHostingService.newContentHostingComparator(
					ResourceProperties.PROP_CONTENT_PRIORITY, true);
		} else {
			return contentHostingService.newContentHostingComparator(
					ResourceProperties.PROP_DISPLAY_NAME, true);
		}
	}

	/**
	 *
	 * @param queryString
	 * @return
	 */
	private Map<String, Object> getQueryMap(String queryString) {

		Map<String, Object> params = new HashMap<String, Object>();
		if (null != queryString && !queryString.isEmpty()) {
			String[] strings = queryString.split("&");
			for (int i=0; i<strings.length; i++) {
				String parameter = strings[i];
				int j = parameter.indexOf("=");
				params.put(parameter.substring(0, j), parameter.substring(j+1));
			}
		}
		return params;
	}

	/**
	 *
	 * @param timestamp  use formatter A: yyyyMMddHHmmssSSS
	 * @return
	 */
	private Time getTime(String timestamp) {

		try {

			if (null != timestamp) {
				DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				Date date = format.parse(timestamp);
				Calendar c = Calendar.getInstance();
				c.setTime(date);

				return TimeService.newTimeGmt(format.format(date));
			}

		} catch (ParseException e) {
			return TimeService.newTimeGmt("20201231235959999");
		}
		return null;
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
						
						String contentType = resource.getContentType();
						
						// only Web Link resource will have not-null webLinkUrl value assigned
						item.webLinkUrl = getWebLinkResourceUrlString(resource, contentType);
						
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
	 * return the original URL string based on Web Link resource content 
	 * @param resource
	 * @param contentType
	 * @return
	 */
	private String getWebLinkResourceUrlString(ContentResource resource, String contentType) {
		String urlString = null;
		
		// for Web Link resources
		// return the original URL link, instead of access URL
		if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
		{
			try
			{
				byte[] content = resource.getContent();
				if (content != null && content.length > 0)
				{
					try
					{
						// An invalid URI format will get caught by the outermost catch block 
						URI uri = new URI(new String(content, "UTF-8"));
						urlString = uri.toString();
					}
					catch (UnsupportedEncodingException e)
					{
						log.warn("UnsupportedEncodingException for " + new String(content) + " " + e.getMessage());
	
					}
					catch (URISyntaxException e)
					{
						log.warn("Error parsing URI for " + new String(content) + " " + e.getMessage());
					}
				}
			}
			catch (ServerOverloadException e)
			{
				log.warn("Cannot get content for resource ref=" + resource.getReference() + " error: " + e.getMessage());
			}
		}
		
		return urlString;
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
	
	@Setter
	private EntityManager entityManager;
	
	

	
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
		
		// not null only for Web Link resource
		@Getter @Setter
		private String webLinkUrl;

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
