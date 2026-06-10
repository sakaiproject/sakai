/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionLevelManager {
	String PERMISSION_LEVEL_NAME_OWNER = "Owner";
	String PERMISSION_LEVEL_NAME_AUTHOR = "Author";
	String PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR = "Nonediting Author";
	String PERMISSION_LEVEL_NAME_CONTRIBUTOR = "Contributor";
	String PERMISSION_LEVEL_NAME_REVIEWER = "Reviewer";
	String PERMISSION_LEVEL_NAME_NONE = "None";
	String PERMISSION_LEVEL_NAME_CUSTOM = "Custom";

	/**
	 * Resolves one of the built-in default permission levels by its name.
	 *
	 * @param name one of the {@code PERMISSION_LEVEL_NAME_*} constants
	 * @return the matching default PermissionLevel, or null if the name is not a recognized level
	 */
	PermissionLevel getPermissionLevelByName(String name);

	/**
	 * Determines the type uuid of the given permission level by comparing it against the
	 * built-in default levels.
	 *
	 * @param level the permission level to inspect
	 * @return the type uuid of the matching default level, or null if it matches no default level
	 * @throws IllegalArgumentException if level is null
	 */
	String getPermissionLevelType(PermissionLevel level);

	/**
	 * Creates a new, unpersisted PermissionLevel populated from the given permission mask.
	 *
	 * @param name the name of the permission level
	 * @param typeUuid the type uuid to associate with the level
	 * @param mask a map of permission property names (the {@code PermissionLevel} permission
	 *             constants) to their boolean values
	 * @return the newly created PermissionLevel (not yet persisted)
	 * @throws IllegalArgumentException if any argument is null
	 */
	PermissionLevel createPermissionLevel(String name, String typeUuid, Map<String, Boolean> mask);

	/**
	 * @return the PermissionLevel representing the "Owner" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Owner permission level.
	 * @throws IllegalStateException if no "Owner" type exists
	 */
	PermissionLevel getDefaultOwnerPermissionLevel();

	/**
	 * @return the PermissionLevel representing the "Author" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Author permission level.
	 * @throws IllegalStateException if no "Author" type exists
	 */
	PermissionLevel getDefaultAuthorPermissionLevel();

	/**
	 * @return the PermissionLevel representing the "Nonediting Author" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Nonediting Author permission level.
	 * @throws IllegalStateException if no "Nonediting Author" type exists
	 */
	PermissionLevel getDefaultNoneditingAuthorPermissionLevel();

	/**
	 * @return the PermissionLevel representing the "Reviewer" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Reviewer permission level.
	 * @throws IllegalStateException if no "Reviewer" type exists
	 */
	PermissionLevel getDefaultReviewerPermissionLevel();

	/**
	 * @return the PermissionLevel representing the "Contributor" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default Contributor permission level.
	 * @throws IllegalStateException if no "Contributor" type exists
	 */
	PermissionLevel getDefaultContributorPermissionLevel();

	/**
	 * @return the PermissionLevel representing the "None" level. If no level
	 * exists in MFR_PERMISSION_LEVEL_T, returns a default None permission level.
	 * @throws IllegalStateException if no "None" type exists
	 */
	PermissionLevel getDefaultNonePermissionLevel();

    /**
     * Creates a new, unpersisted DBMembershipItem.
     *
     * @param name the name of the membership item (e.g. a role or group name)
     * @param permissionLevelName the name of the associated permission level, may be null
     * @param type the membership item type (one of the {@code MembershipItem.TYPE_*} values)
     * @return the newly created DBMembershipItem (not yet persisted)
     * @throws IllegalArgumentException if name or type is null
     */
    DBMembershipItem createDBMembershipItem(String name, String permissionLevelName, Integer type);

    /**
     * Persists the given membership item.
     *
     * @param item the membership item to save
     * @return the persisted (managed) DBMembershipItem instance
     */
    DBMembershipItem saveDBMembershipItem(DBMembershipItem item);

    /**
     * Persists the given permission level.
     *
     * @param level the permission level to save
     * @return the persisted (managed) PermissionLevel instance
     */
    PermissionLevel savePermissionLevel(PermissionLevel level);

    /**
     * @return a non-null, alphabetically sorted list of the names for the non-custom permission levels
     */
    List<String> getOrderedPermissionLevelNames();

    /**
     * Returns the value of a single named permission on the given permission level.
     *
     * @param customPermissionName the permission name, one of the {@code PermissionLevel} permission constants
     * @param permissionLevel the permission level to read the value from
     * @return the boolean value of the named permission, or null if the name is not recognized
     * @throws IllegalArgumentException if either argument is null
     */
    Boolean getCustomPermissionByName(String customPermissionName, PermissionLevel permissionLevel);

    /**
     * @return the list of all custom permission names (the {@code PermissionLevel} permission constants)
     */
    List<String> getCustomPermissions();

    /**
     * Retrieves all forum-level membership items for the given area (site).
     *
     * @param areaId the id of the area
     * @return a non-null list of the forum membership items for the area
     */
    List<DBMembershipItem> getAllMembershipItemsForForumsForSite(final Long areaId);

    /**
     * Retrieves all topic-level membership items for the given area (site).
     *
     * @param areaId the id of the area
     * @return a non-null list of the topic membership items for the area
     */
    List<DBMembershipItem> getAllMembershipItemsForTopicsForSite(final Long areaId);

    /**
     * Deletes the given membership items along with their associated permission levels.
     *
     * @param membershipSet the membership items to delete; null or empty is a no-op
     */
    void deleteMembershipItems(Set<DBMembershipItem> membershipSet);

}
