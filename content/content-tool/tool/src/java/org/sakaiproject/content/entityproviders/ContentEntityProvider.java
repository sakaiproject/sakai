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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.tool.ListItem;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.exception.EntityException;
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
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Entity provider for the Content / Resources tool
 */
@Slf4j
@Setter
public class ContentEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

	public final static String ENTITY_PREFIX = "content";
	public static final String PREFIX = "resources.";
	public static final String SYS = "sys.";
	private static final String STATE_RESOURCES_TYPE_REGISTRY = PREFIX + SYS + "type_registry";
	private static final String PARAMETER_DEPTH = "depth";
	private static final String PARAMETER_TIMESTAMP = "timestamp";

	private ContentHostingService contentHostingService;
	private SiteService siteService;
	private ToolManager toolManager;
	private SecurityService securityService;
	private UserDirectoryService userDirectoryService;
	private EntityManager entityManager;
	private ServerConfigurationService serverConfigurationService;

	private SecurityAdvisor allowedAdvisor = (userId1, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;

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

		log.debug("Content for site: {}", siteId);

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

	@EntityCustomAction(action="htmlForRef", viewKey=EntityView.VIEW_SHOW)
	public ActionReturn getHtmlForRef(EntityView view, Map<String, Object> params) throws EntityPermissionException {

		String ref = (String) params.get("ref");

		if (StringUtils.isBlank(ref)) {
			throw new EntityException("You need to supply the ref parameter.", null, HttpServletResponse.SC_BAD_REQUEST);
		}

		return new ActionReturn(contentHostingService.getHtmlForRef(ref));
	}

	/**
	 *
	 * Upload new file to a site. Puts into a folder based on current user permissions.
	 * student-uploads -> folder for site members without write privileges, does not duplicate
	 * instructor-uploads -> folder for site members with write privileges
	 * Both folders are set to hidden with accessible content
	 *
	 */
	@EntityCustomAction(action = "direct-upload", viewKey = EntityView.VIEW_NEW)
	public String uploadFileToSite(EntityView view,  Map<String, Object> params) {
		if (!serverConfigurationService.getBoolean("content.direct.upload.enabled", Boolean.TRUE)) {
			throw new SecurityException("The direct-upload service is not enabled for your instance.");
		}
		User currentUser = userDirectoryService.getCurrentUser();
		if (currentUser == null || StringUtils.isBlank(currentUser.getId())) {
			throw new SecurityException("You must be logged in to use the direct-upload service.");
		}
		String context = (String) params.get("context");
		String collectionId = contentHostingService.getSiteCollection(context);
		String uploadFolderName = "";
		if (contentHostingService.allowAddCollection(collectionId)) {
			// If user has content.new permission add file to instructor folder
			uploadFolderName = contentHostingService.getInstructorUploadFolderName();
		} else if (contentHostingService.allowGetCollection(collectionId)) {
			// If user has content.read permission add file to student folder
			uploadFolderName = contentHostingService.getStudentUploadFolderName();
		} else {
			// If user does not have either permission in the collection throw a security exception
			throw new SecurityException("You must have at least read access for resources in the site to use direct-upload.");
		}
		String errorMessage;
		try {
			ContentCollection uploadsFolder = getDirectUploadsFolder(collectionId, uploadFolderName);
			if (uploadsFolder == null) {
				errorMessage = "Unable to get uploads folder.";
			} else {
				DiskFileItem fileItem = (DiskFileItem) params.get("upload");
				String[] fileNameParts = fileItem.getName().split("\\.(?=[^.]+$)");
				String basename = StringUtils.equals(uploadFolderName, contentHostingService.getStudentUploadFolderName()) ? currentUser.getDisplayId() + "_" + fileNameParts[0] : fileNameParts[0];
				securityService.pushAdvisor(allowedAdvisor);
				ContentResourceEdit resourceEdit = contentHostingService.addResource(
						uploadsFolder.getId(),
						basename,
						fileNameParts[1],
						ContentHostingService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				resourceEdit.setContent(fileItem.getInputStream());
				resourceEdit.setContentType(fileItem.getContentType());
				resourceEdit.setContentLength(fileItem.getSize());
				contentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);
				JSONObject success = new JSONObject();
				success.put("uploaded", 1);
				success.put("fileName", fileItem.getName());
				success.put("url", resourceEdit.getUrl());
				return success.toJSONString();
			}
		} catch (TypeException | InconsistentException | IdInvalidException | IdUnusedException e) {
			log.error("Exception when trying to get the direct upload folder for site {}.", context, e);
			errorMessage = "Unable to get uploads folder.";
		} catch (OverQuotaException e) {
			log.error("Over quota exception for site " + context + ".", e);
			errorMessage = "Resource quota exceeded.";
		} catch (ServerOverloadException e) {
			log.error("Server overload exception for site " + context + ".", e);
			errorMessage = "Server is busy. Please try again.";
		} catch (PermissionException e) {
			log.error("Permission exception trying to direct upload.", e);
			errorMessage = "You do not have permission to perform this action.";
		} catch (IdLengthException e) {
			log.error("Resource file name too long.", e);
			errorMessage = "File name too long.";
		} catch (IdUniquenessException e) {
			log.error("Unable to find unique id.", e);
			errorMessage = "A file with that name already exists.";
		} catch (IOException e) {
			log.error("IOException with direct upload.", e);
			errorMessage = "An error occurred trying to upload the file.";
		} finally {
			securityService.popAdvisor(allowedAdvisor);
		}
		// There was an error.
		JSONObject errorMsg = new JSONObject();
		errorMsg.put("message", errorMessage);
		JSONObject error = new JSONObject();
		error.put("uploaded", 0);
		error.put("error", errorMsg);
		return error.toJSONString();
	}

	private ContentCollection getDirectUploadsFolder(final String collectionId, final String uploadFolderName) throws TypeException, InconsistentException, IdInvalidException {
		ContentCollection returnCollection = null;
		try {
			securityService.pushAdvisor(allowedAdvisor);
			returnCollection = contentHostingService.getCollection(collectionId + uploadFolderName + "/");
		} catch (IdUnusedException e) {
			// Folder doesn't exist so create it
			try {
				ContentCollectionEdit uploadsFolder = contentHostingService.addCollection(collectionId + uploadFolderName + "/");
				ResourcePropertiesEdit props = uploadsFolder.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, uploadFolderName);
				props.addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
				if (StringUtils.equals(uploadFolderName, contentHostingService.getStudentUploadFolderName())) {
					// Properties for student folder only
					props.addProperty(ResourceProperties.PROP_CREATOR, developerHelperService.ADMIN_USER_ID);
					props.addProperty(ResourceProperties.PROP_MODIFIED_BY, developerHelperService.ADMIN_USER_ID);
					props.addProperty(ResourceProperties.PROP_DO_NOT_DUPLICATE, Boolean.TRUE.toString());
				}
				contentHostingService.commitCollection(uploadsFolder);
				returnCollection = contentHostingService.getCollection(collectionId + uploadFolderName + "/");
			} catch (IdUsedException | PermissionException | IdUnusedException ex) {
				// This shouldn't be possible. Security advisor permits everything and we just
				// found that the id for this collection was unused and created it.
				log.warn("Id used or permission exception.", ex);
			}
		} catch (PermissionException pe) {
			log.warn("Permission exception getting collection in direct upload.", pe);
		} finally {
			securityService.popAdvisor(allowedAdvisor);
		}
		return returnCollection;
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
		return new String[] { Formats.XML, Formats.JSON, Formats.HTML};
	}
	
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
