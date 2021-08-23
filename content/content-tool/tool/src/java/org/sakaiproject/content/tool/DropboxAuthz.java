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
 * Manages authorization information for dropbox contexts
 */
@Slf4j
public class DropboxAuthz
{
	// ======= realm permissions =======

	/** If the current user is admin */
	private boolean isSuperUser = false;

	/** The user has permission to maintain the site's entire dropbox */
	private boolean hasDropboxMaintain = false;

	/** If the user has dropbox.maintain.own.groups, this lists the group members whose dropboxes the user is authorized to maintain */
	private Set<String> dropboxMaintainOwnGroupsMembers = Collections.emptySet();

	/** The user has permission to maintain their own user dropbox */
	private boolean hasDropboxOwn = false;

	/** If the user has site.update */
	private boolean hasSiteUpdate = false;

	/** User can revise any file in the dropbox(es) they maintain */
	private boolean hasContentReviseAny = false;

	/** User can revise their own files in the dropbox(es) they maintain */
	private boolean hasContentReviseOwn = false;

	/** User can delete any file in the dropbox(es) they maintain */
	private boolean hasContentDeleteAny = false;

	/** User can delete their own files in the dropbox(es) they maintain */
	private boolean hasContentDeleteOwn = false;


	// ======= content permissions =======

	// The following collections represent the permissions that the user *might* be granted at each level - their value is to determine which permissions must be excluded.

	// level 1: /group-user/<siteId>
	private Collection<ContentPermissions> level1Permissions = Collections.emptySet();

	// level 2: /group-user/<siteId>/<userId>
	private Collection<ContentPermissions> level2Permissions = Collections.emptySet();

	// level 3: /group-user/<siteId>/userId>/*
	private Collection<ContentPermissions> level3Permissions = Collections.emptySet();


	/** load permissions only once per instance */
	private boolean permissionsLoaded = false;

	private ContentHostingService getContentHostingService()
	{
		return (ContentHostingService)ComponentManager.get(ContentHostingService.class);
	}

	private SecurityService getSecurityService()
	{
		return (SecurityService)ComponentManager.get(SecurityService.class);
	}

	private ToolManager getToolManager()
	{
		return (ToolManager)ComponentManager.get(ToolManager.class);
	}

	private SessionManager getSessionManager()
	{
		return (SessionManager)ComponentManager.get(SessionManager.class);
	}

	private SiteService getSiteService()
	{
		return (SiteService)ComponentManager.get(SiteService.class);
	}

	private UserDirectoryService getUserDirectoryService()
	{
		return (UserDirectoryService)ComponentManager.get(UserDirectoryService.class);
	}


	/**
	 * Gathers all the authz information required to determine how this user may interact with dropbox entities
	 */
	private void loadPermissions()
	{
		ContentHostingService contentService = getContentHostingService();
		SecurityService securityService = getSecurityService();
		SiteService siteService = getSiteService();

		isSuperUser = securityService.isSuperUser();

		Site site = null;
		try
		{
			site = siteService.getSite(getToolManager().getCurrentPlacement().getContext());
		} catch (IdUnusedException ex) {
			log.warn("Can't find current site", ex);
			return;
		}
		if (site != null)
		{
			String siteId = site.getId();
			String siteReference = site.getReference();
			hasDropboxMaintain = contentService.isDropboxMaintainer(siteId);

			// Add fellow group members if the user has dropbox.maintain.own.groups
			if (contentService.isDropboxGroups(siteId))
			{
				// get the user's fellow group members
				Collection<Group> siteGroups = site.getGroupsWithMember(getCurrentUserId());
				dropboxMaintainOwnGroupsMembers = siteGroups.stream().flatMap(g -> g.getMembers().stream()).map(Member::getUserId).collect(Collectors.toSet());
			}

			hasDropboxOwn = securityService.unlock(ContentHostingService.AUTH_DROPBOX_OWN, siteReference);

			hasSiteUpdate = siteService.allowUpdateSite(siteId);

			hasContentReviseAny = securityService.unlock(ContentHostingService.AUTH_DROPBOX_WRITE_ANY, siteReference);
			hasContentReviseOwn = securityService.unlock(ContentHostingService.AUTH_DROPBOX_WRITE_OWN, siteReference);
			hasContentDeleteAny = securityService.unlock(ContentHostingService.AUTH_DROPBOX_REMOVE_ANY, siteReference);
			hasContentDeleteOwn = securityService.unlock(ContentHostingService.AUTH_DROPBOX_REMOVE_OWN, siteReference);
		}


		// Set the possible permissions at each level
		if (hasDropboxMaintain)
		{
			level1Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
			level2Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
			level3Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
		}
		else if (!dropboxMaintainOwnGroupsMembers.isEmpty())
		{
			// level1Permissions remains empty
			level2Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.READ
			);
			level3Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
		}
		else if (hasDropboxOwn)
		{
			//level1Permissions is empty
			level2Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.READ
			);
			level3Permissions = Arrays.asList(
				ContentPermissions.CREATE,
				ContentPermissions.DELETE,
				ContentPermissions.READ,
				ContentPermissions.REVISE
			);
		}
	}

	/**
	 * Determines and applies the permissions that the user is authorized to perform to the specified item
	 */
	public void configureDropboxItemPermissions(ListItem item)
	{
		item.setPermissions(getDropboxPermissionsForEntity(item.getEntity()));
	}

	/**
	 * Determines the permissions that the user is authorized to perform to the specified entity
	 */
	public Collection<ContentPermissions> getDropboxPermissionsForEntity(Entity entity)
	{
		ContentHostingService contentService = getContentHostingService();

		// Get the permissions - but only if we haven't already done so
		if (!permissionsLoaded)
		{
			loadPermissions();
			permissionsLoaded = true;
		}

		/*
		 * Prepare a set of permissions to assign to the entity.
		 * Start empty, but with capacity to hold all ContentPermission values
		 */
		Set<ContentPermissions> entityPermissions = new HashSet<>(ContentPermissions.values().length);

		if (isSuperUser)
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
				entityPermissions.addAll(level1Permissions);
			}
			else if (contentService.isIndividualDropbox(entityId))
			{
				// Level 2
				if (canMaintainEntity(entityId))
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
						Collection<ContentPermissions> level2NonDestructivePermissions = level2Permissions.stream().filter(cp -> cp != ContentPermissions.DELETE).collect(Collectors.toSet());
						filterAndCopyLevelPermissionsForEntity(level2NonDestructivePermissions, entityPermissions, entity);
					}
					else
					{
						// It's a sibling - treat like a regular resource
						filterAndCopyLevelPermissionsForEntity(level2Permissions, entityPermissions, entity);
					}
				}
			}
			else if (contentService.isInsideIndividualDropbox(entityId))
			{
				// Level 3
				if (canMaintainEntity(entityId))
				{
					filterAndCopyLevelPermissionsForEntity(level3Permissions, entityPermissions, entity);
				}
			}
			else
			{
				log.warn("The specified entity is not a dropbox entity: " + entityId);
				return Collections.emptySet();
			}

			if (hasSiteUpdate)
			{
				entityPermissions.add(ContentPermissions.SITE_UPDATE);
			}
		}

		return entityPermissions;
	}

	/**
	 * Populates the specified entityPermissions collection with a subset of the specified levelPermissions.
	 * DELETE and REVISE will only be copied in consideration with who the file owner is, in combination with the realm permissions:
	 * content.delete.any, content.delete.own, content.revise.any, and content.revise.own
	 * @param levelPermissions the list of permissions that the entity *might* get at its level
	 * @param entityPermissions the target collection to copy permissions into
	 * @param entity the entity that the user is being assigned permissions over
	 */
	private void filterAndCopyLevelPermissionsForEntity(Collection<ContentPermissions> levelPermissions, Collection<ContentPermissions> entityPermissions, Entity entity)
	{
		entityPermissions.addAll(
			levelPermissions.stream().filter(permission ->
			{
				switch (permission)
				{
					case DELETE:
						return canDeleteEntity(entity);
					case REVISE:
						return canReviseEntity(entity);
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
	 */
	private boolean canMaintainEntity(String entityId)
	{
		ContentHostingService contentService = getContentHostingService();
		if (isSuperUser)
		{
			return true;
		}

		if (hasDropboxMaintain)
		{
			return true;
		}

		String dropboxOwner = getDropboxOwner(entityId);
		if (dropboxMaintainOwnGroupsMembers.contains(dropboxOwner))
		{
			// User has dropbox.maintain.own.groups, and the dropbox owner is a groupmate
			return true;
		}

		return hasDropboxOwn && getCurrentUserId().equals(dropboxOwner);
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

	/**
	 * Determines if we can delete an entity from a resource permissions standpoint
	 */
	private boolean canDeleteEntity(Entity entity)
	{
		ContentHostingService contentService = getContentHostingService();

		// Match behaviour of the Resources tool - to delete folders requires both delete and revise permission
		boolean requiresRevise = contentService.isCollection(entity.getId());

		// Use mutable booleans to benefit performance if we need to check the file's creator twice
		MutableBoolean isUserIsCreatorKnown = new MutableBoolean(false);
		MutableBoolean isUserCreator = new MutableBoolean(false);

		if (hasContentDeleteAny && (!requiresRevise || canReviseEntity(entity, isUserIsCreatorKnown, isUserCreator)))
		{
			return true;
		}
		if (hasContentDeleteOwn && (!requiresRevise || canReviseEntity(entity, isUserIsCreatorKnown, isUserCreator)))
		{
			return isCurrentUserCreator(entity, isUserIsCreatorKnown, isUserCreator);
		}
		return false;
	}

	/**
	 * Convenience overload
	 */
	private boolean canReviseEntity(Entity entity)
	{
		return canReviseEntity(entity, new MutableBoolean(false), new MutableBoolean(false));
	}

	/**
	 * Determines if we can revise an entity from a resource permissions standpoint
	 * Takes MutableBooleans to benefit performance when called more than once on the same entity
	 */
	private boolean canReviseEntity(Entity entity, MutableBoolean isUserIsCreatorKnown, MutableBoolean userIsCreator)
	{
		if (hasContentReviseAny)
		{
			return true;
		}
		if (hasContentReviseOwn)
		{
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
	private boolean isCurrentUserCreator(Entity entity, MutableBoolean isUserIsCreatorKnown, MutableBoolean userIsCreator)
	{
		if (isUserIsCreatorKnown.isTrue())
		{
			return userIsCreator.isTrue();
		}
		ResourceProperties properties = entity.getProperties();
		String creator = properties.getProperty(ResourceProperties.PROP_CREATOR);
		userIsCreator.setValue(getCurrentUserId().equals(creator));
		isUserIsCreatorKnown.setValue(true);
		return userIsCreator.isTrue();
	}

	private String getCurrentUserId()
	{
		return getSessionManager().getCurrentSessionUserId();
	}

	private boolean userExists(String userId)
	{
		try
		{
			return getUserDirectoryService().getUser(userId) != null;
		}
		catch (UserNotDefinedException e)
		{
			return false;
		}
	}
}
