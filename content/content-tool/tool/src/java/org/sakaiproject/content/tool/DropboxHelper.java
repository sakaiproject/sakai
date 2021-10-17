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
 package org.sakaiproject.content.tool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableBoolean;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.tool.ResourcesAction.ContentPermissions;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * A collection of methods specifically to handle logic in Drop Box contexts
 */
@Slf4j
public class DropboxHelper {

	/** Prevent instantiation */
	private DropboxHelper() { }

	private static ContentHostingService getContentHostingService() {
		return (ContentHostingService)ComponentManager.get(ContentHostingService.class);
	}

	private static SecurityService getSecurityService() {
		return (SecurityService)ComponentManager.get(SecurityService.class);
	}

	private static ToolManager getToolManager() {
		return (ToolManager)ComponentManager.get(ToolManager.class);
	}

	private static SessionManager getSessionManager() {
		return (SessionManager)ComponentManager.get(SessionManager.class);
	}

	private static SiteService getSiteService() {
		return (SiteService)ComponentManager.get(SiteService.class);
	}

	private static UserDirectoryService getUserDirectoryService() {
		return (UserDirectoryService)ComponentManager.get(UserDirectoryService.class);
	}

	/**
	 * Gathers all the authz information required to determine how this user may interact with dropbox entities.
	 *
	 * Call this exactly once per request, then use the return value to eliminate further permission checks.
	 *
	 * @return authorization information represented in a DropboxAuthz instance.
	 */
	public static DropboxAuthz getAuthorization() {

		ContentHostingService contentService = getContentHostingService();
		SecurityService securityService = getSecurityService();
		SiteService siteService = getSiteService();

		DropboxAuthz dropboxAuthz = new DropboxAuthzImpl();

		dropboxAuthz.isSuperUser = securityService.isSuperUser();

		Site site = null;
		try {
			site = siteService.getSite(getToolManager().getCurrentPlacement().getContext());
		} catch (IdUnusedException ex) {
			log.warn("Can't find current site", ex);
			return dropboxAuthz;
		}
		if (site != null) {
			String siteId = site.getId();
			String siteReference = site.getReference();
			dropboxAuthz.hasDropboxMaintain = contentService.isDropboxMaintainer(siteId);

			// Add fellow group members if the user has dropbox.maintain.own.groups
			if (contentService.isDropboxGroups(siteId)) {
				// get the user's fellow group members
				Collection<Group> siteGroups = site.getGroupsWithMember(getCurrentUserId());
				dropboxAuthz.dropboxMaintainOwnGroupsMembers = siteGroups.stream().flatMap(g -> g.getMembers().stream()).map(Member::getUserId).collect(Collectors.toSet());
			}

			dropboxAuthz.hasDropboxOwn = securityService.unlock(ContentHostingService.AUTH_DROPBOX_OWN, siteReference);

			dropboxAuthz.hasSiteUpdate = siteService.allowUpdateSite(siteId);

			dropboxAuthz.hasContentReviseAny = securityService.unlock(ContentHostingService.AUTH_DROPBOX_WRITE_ANY, siteReference);
			dropboxAuthz.hasContentReviseOwn = securityService.unlock(ContentHostingService.AUTH_DROPBOX_WRITE_OWN, siteReference);
			dropboxAuthz.hasContentDeleteAny = securityService.unlock(ContentHostingService.AUTH_DROPBOX_REMOVE_ANY, siteReference);
			dropboxAuthz.hasContentDeleteOwn = securityService.unlock(ContentHostingService.AUTH_DROPBOX_REMOVE_OWN, siteReference);
		}


		// Set the possible permissions at each level
		if (dropboxAuthz.hasDropboxMaintain) {
			dropboxAuthz.level1Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
			dropboxAuthz.level2Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
			dropboxAuthz.level3Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
		} else if (!dropboxAuthz.dropboxMaintainOwnGroupsMembers.isEmpty()) {
			// level1Permissions remain empty
			dropboxAuthz.level2Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.READ
			);
			dropboxAuthz.level3Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
		} else if (dropboxAuthz.hasDropboxOwn) {
			//level1Permissions is empty
			dropboxAuthz.level2Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.READ
			);
			dropboxAuthz.level3Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
		}

		return dropboxAuthz;
	}

	/**
	 * Determines and applies the permissions that the user is authorized to perform to the specified item.
	 * @param dropboxAuthz the user's authorization information
	 * @param item the item whose permissions are to be configured
	 */
	public static void configureDropboxItemPermissions(DropboxAuthz dropboxAuthz, ListItem item) {
		item.setPermissions(getDropboxPermissionsForEntity(dropboxAuthz, item.getEntity()));
	}

	/**
	 * Determines the permissions that the user is authorized to perform to the specified entity.
	 * @param dropboxAuthz the user's authorization information
	 * @param entity the entity on which content permissions must be determined
	 * @return the authorized content permissions on this entity
	 */
	public static Collection<ContentPermissions> getDropboxPermissionsForEntity(DropboxAuthz dropboxAuthz, Entity entity) {

		ContentHostingService contentService = getContentHostingService();

		/*
		 * Prepare a set of permissions to assign to the entity.
		 * Start empty, but with capacity to hold all ContentPermission values
		 */
		Set<ContentPermissions> entityPermissions = new HashSet<>(ContentPermissions.values().length);

		if (dropboxAuthz.isSuperUser)
		{
			return EnumSet.allOf(ContentPermissions.class);
		}
		else
		{
			String entityId = entity.getId();
			/*
			 * Dropbox entities have the form: /group-user/<siteId>/...
			 * Verify the entity is from the current site:
			 */
			String siteId = getToolManager().getCurrentPlacement().getContext();
			String[] parts = entityId.split(Entity.SEPARATOR);
			if (parts.length < 3 || !parts[2].equals(siteId))
			{
				// Not a dropbox context, or this is the wrong site
				log.warn("Attempted to apply permissions on an entity from another site: " + entityId);
				return Collections.emptySet();
			}

			if (contentService.isSiteLevelDropbox(entityId))
			{
				// Level 1
				// No additional logic required beyond what was completed in loadPermissions()
				entityPermissions.addAll(dropboxAuthz.level1Permissions);
			}
			else if (contentService.isIndividualDropbox(entityId))
			{
				// Level 2
				if (canMaintainEntity(dropboxAuthz, entityId))
				{
					/*
					 * ContentHostingService.isIndividualDropbox(...) is true for all resources with the form:
					 * /group-user/<siteId>/<entityName>
					 * Note: maintainers with CREATE on level1 can create siblings to individual dropboxes.
					 * We should differentiate individual dropboxes from maintainer-created siblings
					 */
					String userId = parts[3];
					if (userExists(userId))
					{
						// It's an individual's dropbox - filter out destructive permissions
						Collection<ContentPermissions> level2NonDestructivePermissions = dropboxAuthz.level2Permissions.stream().filter(cp -> cp != ContentPermissions.DELETE).collect(Collectors.toSet());
						filterAndCopyLevelPermissionsForEntity(dropboxAuthz, level2NonDestructivePermissions, entityPermissions, entity);
					}
					else
					{
						// It's a sibling - treat like a regular resource
						filterAndCopyLevelPermissionsForEntity(dropboxAuthz, dropboxAuthz.level2Permissions, entityPermissions, entity);
					}
				}
			}
			else if (contentService.isInsideIndividualDropbox(entityId))
			{
				// Level 3
				if (canMaintainEntity(dropboxAuthz, entityId))
				{
					filterAndCopyLevelPermissionsForEntity(dropboxAuthz, dropboxAuthz.level3Permissions, entityPermissions, entity);
				}
			}
			else
			{
				log.warn("The specified entity is not a dropbox entity: " + entityId);
				return Collections.emptySet();
			}

			if (dropboxAuthz.hasSiteUpdate) {
				entityPermissions.add(ContentPermissions.SITE_UPDATE);
			}
		}

		return entityPermissions;
	}

	/**
	 * Populates the specified entityPermissions collection with a subset of the specified levelPermissions.
	 * DELETE and REVISE will only be copied in consideration with who the file owner is, in combination with the realm permissions:
	 * content.delete.any, content.delete.own, content.revise.any, and content.revise.own
	 * @param dropboxAuthz the user's authorization information
	 * @param levelPermissions the list of permissions that the entity *might* get at its level
	 * @param entityPermissions the target collection to copy permissions into
	 * @param entity the entity that the user is being assigned permissions over
	 * @return 
	 */
	private static void filterAndCopyLevelPermissionsForEntity(DropboxAuthz dropboxAuthz, Collection<ContentPermissions> levelPermissions, Collection<ContentPermissions> entityPermissions, Entity entity) {
		entityPermissions.addAll(
			levelPermissions.stream().filter(permission -> {
				switch (permission) {
					case DELETE:
						return canDeleteEntity(dropboxAuthz, entity);
					case REVISE:
						return canReviseEntity(dropboxAuthz, entity);
					default:
						return true;
				}
			}).collect(Collectors.toSet())
		);
	}

	/**
	 * Assumes the entity in question is level 2 or 3, and in the current site.
	 * Returns true if the entity falls in a dropbox the user can maintain,
	 * that is, user has dropbox.maintain, or
	 * the user has dropbox.maintain.own.groups and the entity is in a groupmate's dropbox, or
	 * the user has dropbox.own and the entity is in their own dropbox
	 * @param dropboxAuthz the user's authorization information
	 * @param entityId a Dropbox entity reference String
	 */
	private static boolean canMaintainEntity(DropboxAuthz dropboxAuthz, String entityId) {

		if (dropboxAuthz.isSuperUser) {
			return true;
		}

		if (dropboxAuthz.hasDropboxMaintain) {
			return true;
		}

		String dropboxOwner = getDropboxOwner(entityId);
		if (dropboxAuthz.dropboxMaintainOwnGroupsMembers.contains(dropboxOwner)) {
			// User has dropbox.maintain.own.groups, and the dropbox owner is a groupmate
			return true;
		}

		return dropboxAuthz.hasDropboxOwn && getCurrentUserId().equals(dropboxOwner);
	}

	/**
	 * Gets the userId from a dropbox entity.
	 */
	private static String getDropboxOwner(String entityId) {
		String[] parts = entityId.split(Entity.SEPARATOR);
		// /group-user/siteId/userId/...
		// parts: "", "group-user", "siteId", "userId", ...
		return parts[3];
	}

	/**
	 * Determines if we can delete an entity from a resource permissions standpoint
	 */
	private static boolean canDeleteEntity(DropboxAuthz dropboxAuthz, Entity entity) {

		ContentHostingService contentService = getContentHostingService();

		// Match behaviour of the Resources tool - to delete folders requires both delete and revise permission
		boolean requiresRevise = contentService.isCollection(entity.getId());

		// Use mutable booleans to benefit performance if we need to check the file's creator twice
		MutableBoolean isUserIsCreatorKnown = new MutableBoolean(false);
		MutableBoolean isUserCreator = new MutableBoolean(false);

		if (dropboxAuthz.hasContentDeleteAny && (!requiresRevise || canReviseEntity(dropboxAuthz, entity, isUserIsCreatorKnown, isUserCreator))) {
			return true;
		}
		if (dropboxAuthz.hasContentDeleteOwn && (!requiresRevise || canReviseEntity(dropboxAuthz, entity, isUserIsCreatorKnown, isUserCreator))) {
			return isCurrentUserCreator(entity, isUserIsCreatorKnown, isUserCreator);
		}
		return false;
	}

	/**
	 * Convenience overload
	 */
	private static boolean canReviseEntity(DropboxAuthz dropboxAuthz, Entity entity) {
		return canReviseEntity(dropboxAuthz, entity, new MutableBoolean(false), new MutableBoolean(false));
	}

	/**
	 * Determines if we can revise an entity from a resource permissions standpoint
	 * Takes MutableBooleans to benefit performance when called more than once on the same entity
	 */
	private static boolean canReviseEntity(DropboxAuthz dropboxAuthz, Entity entity, MutableBoolean isUserIsCreatorKnown, MutableBoolean userIsCreator) {
		if (dropboxAuthz.hasContentReviseAny) {
			return true;
		}
		if (dropboxAuthz.hasContentReviseOwn) {
			return isCurrentUserCreator(entity, isUserIsCreatorKnown, userIsCreator);
		}
		return false;
	}

	/**
	 * True iff the user is the creator of the specified entity.
	 *
	 * Takes MutableBooleans to benefit performance when called more than once on the same entity:
	 * If isUserIsCreatorKnown is false, it is mutated to true, and userIsCreator is mutated to the return value. Otherwise,
	 * short circuits and returns the value of userIsCreator.
	 *
	 * @param entity the entity on which we're checking the PROP_CREATOR property
	 * @param isUserIsCreatorKnown whether to short circuit, returning the userIsCreator param
	 * @param userIsCreator the value to return
	 */
	private static boolean isCurrentUserCreator(Entity entity, MutableBoolean isUserIsCreatorKnown, MutableBoolean userIsCreator) {
		if (isUserIsCreatorKnown.isTrue()) {
			return userIsCreator.isTrue();
		}
		ResourceProperties properties = entity.getProperties();
		String creator = properties.getProperty(ResourceProperties.PROP_CREATOR);
		userIsCreator.setValue(getCurrentUserId().equals(creator));
		isUserIsCreatorKnown.setValue(true);
		return userIsCreator.isTrue();
	}

	private static String getCurrentUserId() {
		return getSessionManager().getCurrentSessionUserId();
	}

	private static boolean userExists(String userId) {
		try {
			return getUserDirectoryService().getUser(userId) != null;
		} catch (UserNotDefinedException e) {
			return false;
		}
	}

	/**
	 * Parent class is protected to ensure instances are created with getAuthorization().
	 */
	protected static class DropboxAuthzImpl extends DropboxAuthz { }
}
