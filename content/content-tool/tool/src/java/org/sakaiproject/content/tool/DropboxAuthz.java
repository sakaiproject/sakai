package org.sakaiproject.content.tool;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.sakaiproject.content.tool.ResourcesAction.ContentPermissions;


/**
 * Represents the current user's authorization information for dropbox contexts.
 *
 * Recommended use:
 * Instantiate one instance per-request using DropboxHelper.getDropboxAuthz(),
 * then re-use the instance to avoid duplicate permission checks.
 */
public class DropboxAuthz
{

	/** Use DropboxHelper.getDropboxAuthz() */
	protected DropboxAuthz() { }

	// ======= realm permissions =======

	/** If the current user is admin */
	public boolean isSuperUser = false;

	/** The user has permission to maintain the site's entire dropbox */
	public boolean hasDropboxMaintain = false;

	/** If the user has dropbox.maintain.own.groups, this lists the group members whose dropboxes the user is authorized to maintain */
	public Set<String> dropboxMaintainOwnGroupsMembers = Collections.emptySet();

	/** The user has permission to maintain their own user dropbox */
	public boolean hasDropboxOwn = false;

	/** If the user has site.update */
	public boolean hasSiteUpdate = false;

	/** User can revise any file in the dropbox(es) they maintain */
	public boolean hasContentReviseAny = false;

	/** User can revise their own files in the dropbox(es) they maintain */
	public boolean hasContentReviseOwn = false;

	/** User can delete any file in the dropbox(es) they maintain */
	public boolean hasContentDeleteAny = false;

	/** User can delete their own files in the dropbox(es) they maintain */
	public boolean hasContentDeleteOwn = false;


	// ======= content permissions =======

	// The following collections represent the permissions that the user *might* be granted at each level - their value is to determine which permissions must be excluded.

	// level 1: /group-user/<siteId>
	public Collection<ContentPermissions> level1Permissions = Collections.emptySet();

	// level 2: /group-user/<siteId>/<userId>
	public Collection<ContentPermissions> level2Permissions = Collections.emptySet();

	// level 3: /group-user/<siteId>/userId>/*
	public Collection<ContentPermissions> level3Permissions = Collections.emptySet();

}
