/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
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
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Entity provider for the Dropbox tool
 */
@Slf4j
public class DropboxEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

	public final static String ENTITY_PREFIX = "dropbox";

	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/**
	 * site/siteId/user/userEid
	 */
	@EntityCustomAction(action = "site", viewKey = EntityView.VIEW_LIST)
	public List<DropboxItem> getDropboxCollectionForSiteAndUser(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);
		
		// get userId
		String userEid = view.getPathSegment(4);

		if(log.isDebugEnabled()) {
			log.debug("Dropbox for site: " + siteId + " and user: " + userEid);
		}

		// check siteId and userEid supplied
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(userEid)) {
			throw new IllegalArgumentException(
					"siteId and userEid must be set in order to get the dropbox for a site, via the URL /dropbox/site/siteId/user/userId");
		}

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
		ToolConfiguration toolConfig = site.getToolForCommonId("sakai.dropbox");
		if(toolConfig == null || !toolManager.isVisible(site, toolConfig)) {
			throw new EntityNotFoundException("No access to tool in site: " + siteId, siteId);
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
				
		//check user has permission to this dropbox in this site
		boolean isAllowed = canAccessDropbox(siteId, userId);
		
		if(!isAllowed) {
			throw new SecurityException("No access to site: " + siteId + " and dropbox: " + userEid);
		}
		
		//get collectionId for the dropbox
		String collectionId = getDropBoxCollectionId(siteId, userId);
		
		//get list of resources in dropbox		
		List<ContentResource> resources = contentHostingService.getAllResources(collectionId);

		List<DropboxItem> dropboxItems = new ArrayList<DropboxItem>();
		for(ContentResource resource: resources) {
				
			//convert to our simplified object 
			DropboxItem item = new DropboxItem();
			
			ResourceProperties props = resource.getProperties();
			
			item.setTitle(props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			item.setDescription(props.getProperty(ResourceProperties.PROP_DESCRIPTION));
			item.setType(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
			item.setSize(Long.parseLong(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)));
			item.setUrl(resource.getUrl());
			
			dropboxItems.add(item);
			
		}
		
		return dropboxItems;
		
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
	 * Simplified helper class to represent an individual item in a user's dropbox
	 */
	public static class DropboxItem {
		
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
		
	}
	
	/**
	 * Can the current user actually access the requested dropbox?
	 * Admin, dropbox.maintain and dropbox.own are allowed
	 * 
	 * @param siteId	- siteId specified
	 * @param dropboxUserId - userId of the dropbox
	 * @return
	 */
	private boolean canAccessDropbox(String siteId, String dropboxUserId) {
		
		String currentUserId = userDirectoryService.getCurrentUser().getId();
		
		//admin
		if (securityService.isSuperUser(currentUserId)) {
			return true;
		}
		
		String siteRef = "";
		if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
			siteRef = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
		}
		
		//owner - current user must match dropboxid, and have permission in the site
		if(StringUtils.equals(currentUserId, dropboxUserId) && securityService.unlock(currentUserId, ContentHostingService.AUTH_DROPBOX_OWN, siteRef)) {
			return true;
		}
		
		//maintainer, must have permission in the site
		if(securityService.unlock(currentUserId, ContentHostingService.AUTH_DROPBOX_MAINTAIN, siteRef)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get the collection Id for the dropbox
	 * @param siteId
	 * @param userId
	 * @return
	 */
	private String getDropBoxCollectionId(String siteId, String userId) {
		return ContentHostingService.COLLECTION_DROPBOX + siteId + "/" + userId + "/";
	}
	
	
}
