/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.content.impl;

import java.util.Arrays;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Handles authorization in the context of the dropbox tool.
 *
 * The DropboxAuthz class in the Content tool takes a top down approach for performance,
 * It finds the user's authz information up front so that it can quickly answer:
 * "Which actions can we do on this entity?", responding in UI terms.
 *
 * This class handles authz after the action is attempted (I.e. server-side checks).
 * It answers only:
 * "Can we do this action to this entity?"
 */
@Slf4j
public class DropboxAuthzHandler
{
	@Setter
	protected static ContentHostingService contentService = null;
	@Setter
	protected static SecurityService securityService = null;
	@Setter
	protected static SessionManager sessionManager = null;
	@Setter
	protected static SiteService siteService = null;
	@Setter
	protected static UserDirectoryService userDirectoryService = null;

	private static final List<String> destructiveLocks = Arrays.asList(
		ContentHostingService.AUTH_DROPBOX_REMOVE_OWN,
		ContentHostingService.AUTH_DROPBOX_REMOVE_ANY
	);

	/**
	 * Determines if the current user is authorized to perform the specified lock on the specified dropbox entity
	 * @param lock the action that requires authorization
	 * @param id the dropbox entity's id
	 * @throws IllegalArgumentException if the lock is blank or the id is not a dropbox entity id
	 */
	public static boolean isAuthorizedViaDropbox(String lock, String id)
	{
		if (StringUtils.isBlank(lock))
		{
			throw new IllegalArgumentException("isAuthorizedViaDropbox() invoked with blank lock");
		}
		if (id == null || !id.startsWith(ContentHostingService.COLLECTION_DROPBOX))
		{
			throw new IllegalArgumentException("isAuthorizedViaDropbox() invoked with an id that is not a dropbox entity");
		}

		if (securityService.isSuperUser())
		{
			return true;
		}

		String dropboxLock = convertLockToDropbox(lock);

		/*
		 * Dropbox entity format:
		 * "/group-user/siteId/userId/..."
		 * Parts:
		 * "", "group-user", siteId, userId, ...
		 */
		String[] parts = id.split(Entity.SEPARATOR);

		if (parts.length < 3)
		{
			// This is the collection of all site dropboxes
			return false;
		}

		String siteId = getDropboxSite(id);

		// Ensure the site exists
		if (!siteService.siteExists(siteId))
		{
			// Site doesn't exist
			log.warn("isAuthorizedViaDropbox invoked for dropbox entity (" + id + ") in non-existent site");
			return false;
		}

		if (parts.length == 3)
		{
			// It's the site dropbox (/group-user/siteId)
			return isAuthorizedOnSiteDropbox(dropboxLock, siteId);
		}

		if (canMaintainEntity(id))
		{
			if (parts.length == 4 && userExists(getDropboxOwner(id)))
			{
				// It's a user's dropbox, don't allow destructive permissions
				return isAuthorizedOnEntity(dropboxLock, id, false);
			}
			return isAuthorizedOnEntity(dropboxLock, id, true);
		}

		return false;
	}

	/**
	 * Convert resources' write and delete locks to use dropbox's permissions
	 */
	private static String convertLockToDropbox(String lock)
	{
		switch(lock)
		{
			case ContentHostingService.AUTH_RESOURCE_WRITE_ANY:
				return ContentHostingService.AUTH_DROPBOX_WRITE_ANY;
			case ContentHostingService.AUTH_RESOURCE_WRITE_OWN:
				return ContentHostingService.AUTH_DROPBOX_WRITE_OWN;
			case ContentHostingService.AUTH_RESOURCE_REMOVE_ANY:
				return ContentHostingService.AUTH_DROPBOX_REMOVE_ANY;
			case ContentHostingService.AUTH_RESOURCE_REMOVE_OWN:
				return ContentHostingService.AUTH_DROPBOX_REMOVE_OWN;
			default:
		}
		return lock;
	}

	/**
	 * Determines if the user has authorization for the specified lock on the specified site dropbox (/group-user/<siteId>).
	 * Users with dropbox.maintain can add, read, and if applicable via content permissions, write.
	 * Users with dropbox.groups.own and dropbox.own can only read.
	 * Remove is always prohibited on the site dropbox.
	 */
	private static boolean isAuthorizedOnSiteDropbox(String lock, String siteId)
	{
		if (contentService.isDropboxMaintainer(siteId))
		{
			switch(lock)
			{
				case ContentHostingService.AUTH_RESOURCE_ADD:
				case ContentHostingService.AUTH_RESOURCE_READ:
					return true;
				case ContentHostingService.AUTH_DROPBOX_WRITE_OWN:
					// reject if user didn't create the site dropbox
					if (!isCurrentUserCreator(ContentHostingService.COLLECTION_DROPBOX + siteId))
					{
						return false;
					}
					// user created it; flow through:
				case ContentHostingService.AUTH_DROPBOX_WRITE_ANY:
					return securityService.unlock(lock, siteService.siteReference(siteId));
			}
		}
		else if (lock.equals(ContentHostingService.AUTH_RESOURCE_READ))
		{
			// User doesn't have dropbox.maintain, but other dropbox users need read permission on the site dropbox.
			String siteReference = siteService.siteReference(siteId);
			return securityService.unlock(ContentHostingService.AUTH_DROPBOX_OWN, siteReference) ||
				securityService.unlock(ContentHostingService.AUTH_DROPBOX_GROUPS, siteReference);
		}
		return false;
	}

	/**
	 * Assumes the entity in question is a dropbox entity within a site, but is not the site's top level dropbox entity.
	 * Returns true if the entity can be maintained by the user.
	 * That is, if the user has dropbox.maintain, or
	 * the user has dropbox.maintain.own.groups and the entity is in a groupmate's dropbox, or
	 * the user has dropbox.own and the entity is in their own dropbox.
	 */
	private static boolean canMaintainEntity(String entityId)
	{
		String siteId = getDropboxSite(entityId);
		if (contentService.isDropboxMaintainer(siteId))
		{
			return true;
		}

		String currentUser = sessionManager.getCurrentSessionUserId();
		String dropboxOwner = getDropboxOwner(entityId);

		// Return true if the current user has dropbox.maintain.own.groups and they share a group with the dropbox owner
		if (contentService.isDropboxGroups(siteId))
		{
			try
			{
				Site site = siteService.getSite(siteId);
				if (site.getGroupsWithMembers(new String[]{currentUser, dropboxOwner}).size() > 0)
				{
					return true;
				}
				// continue -> could be the user's own dropbox
			}
			catch (IdUnusedException e)
			{
				// Site doesn't exist
				log.warn("canMaintainEntity invoked for entity (" + entityId + ") in non-existent site");
				return false;
			}
		}

		return currentUser.equals(dropboxOwner) && securityService.unlock(ContentHostingService.AUTH_DROPBOX_OWN, siteService.siteReference(siteId));
	}

	/**
	 * Determines if the current user is authorized to perform the specified lock on the specified entity.
	 * Assumes that the user can maintain the entity.
	 *
	 * When a user can maintain an entity, Add and Read permissions are always authorized,
	 * but Write and Remove depend on content permissions
	 */
	private static boolean isAuthorizedOnEntity(String lock, String entityId, boolean allowDestructivePermission)
	{
		String siteId = getDropboxSite(entityId);
		String siteReference = siteService.siteReference(siteId);

		if (!allowDestructivePermission && destructiveLocks.contains(lock))
		{
			return false;
		}
		switch(lock)
		{
			case ContentHostingService.AUTH_RESOURCE_ADD:
			case ContentHostingService.AUTH_RESOURCE_READ:
				return true;
			case ContentHostingService.AUTH_DROPBOX_WRITE_ANY:
			case ContentHostingService.AUTH_DROPBOX_REMOVE_ANY:
				return securityService.unlock(lock, siteReference);
			case ContentHostingService.AUTH_DROPBOX_WRITE_OWN:
			case ContentHostingService.AUTH_DROPBOX_REMOVE_OWN:
				return isCurrentUserCreator(entityId) && securityService.unlock(lock, siteReference);
		}
		return false;
	}

	/**
	 * Determines if the current user is the creator of the specified entity
	 */
	private static boolean isCurrentUserCreator(String entityId)
	{
		try
		{
			ResourceProperties properties = contentService.getProperties(entityId);
			String creator = properties.getProperty(ResourceProperties.PROP_CREATOR);
			return sessionManager.getCurrentSessionUserId().equals(creator);
		}
		catch (PermissionException e)
		{
			return false;
		}
		catch (IdUnusedException e)
		{
			log.warn("isCurrentUserCreator invoked on entity that does not exist: " + entityId);
			return false;
		}
	}

	private static boolean userExists(String userId)
	{
		try
		{
			return userDirectoryService.getUser(userId) != null;
		}
		catch (UserNotDefinedException e)
		{
			return false;
		}
	}

	/**
	 * Gets the siteId from a dropbox entity.
	 */
	private static String getDropboxSite(String entityId)
	{
		String[] parts = entityId.split(Entity.SEPARATOR);
		// /group-user/siteId/userId/...
		// parts: "", "group-user", "siteId", "userId", ...
		return parts[2];
	}

	/**
	 * Gets the userId from a dropbox entity.
	 */
	private static String getDropboxOwner(String entityId)
	{
		String[] parts = entityId.split(Entity.SEPARATOR);
		// /group-user/siteId/userId/...
		// parts: "", "group-user", "siteId", "userId", ...
		return parts[3];
	}
}
