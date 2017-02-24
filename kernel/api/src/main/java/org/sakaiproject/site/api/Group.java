/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.api;

import java.io.Serializable;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.PermissionException;

/**
 * <p>
 * A Site Group is a way to divide up a Site into separate units, each with its own authorization group and descriptive information.
 * </p>
 */
public interface Group extends Edit, Serializable, AuthzGroup
{
	/** The property to indicate whether the group is created by Worksite Setup or not */
	static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";
	/** The property to indicate which joinable set the group is part of **/
	static final String GROUP_PROP_JOINABLE_SET = "group_prop_joinable_set";
	/** The property to indicate the max number of users who can join the group **/
	static final String GROUP_PROP_JOINABLE_SET_MAX = "group_prop_joinable_set_max";
	/** The property to indicate whether students can preview the list of users in the set before joining **/
	static final String GROUP_PROP_JOINABLE_SET_PREVIEW = "group_prop_joinable_set_preview";
	/** The property to indicate whether students can view the list of users in the group after joining **/
	static final String GROUP_PROP_VIEW_MEMBERS = "group_prop_view_members";
	/** The property to indicate whether the joinable group is unjoinable or not*/
	static final String GROUP_PROP_JOINABLE_UNJOINABLE = "group_prop_joinable_unjoinable";
	/** The property to indicate whether the group is locked and by which entities references it is locked **/
	static final String GROUP_PROP_LOCKED_BY = "group_prop_locked_by";
	/** Entities references separator for locked by property **/
	static final String GROUP_PROP_SEPARATOR = "#:#";

	/** @return a human readable short title of this group. */
	String getTitle();

	/** @return a text describing the group. */
	String getDescription();

	/**
	 * Access the site in which this group lives.
	 * 
	 * @return the site in which this group lives.
	 */
	public Site getContainingSite();

	/**
	 * Set the human readable short title of this group.
	 * 
	 * @param title
	 *        The new title.
	 */
	void setTitle(String title);

	/**
	 * Set the text describing this group.
	 * 
	 * @param description
	 *        The new description.
	 */
	void setDescription(String description);

	/**
	 * Deletes a member from this group.
	 * Its functionallity is the same as removeMember but throws IllegalStateException.
	 * 
	 * @param userId
	 *        The id of the user to be deleted.
	 * @throws IllegalStateException
	 *        If group is locked and cannot be edited
	 */
	void deleteMember(String userId) throws IllegalStateException;

	/**
	 * Deletes all members from this group.
	 * Its functionallity is the same as removeMembers but throws IllegalStateException.
	 * 
	 * @throws IllegalStateException
	 *        If group is locked and cannot be edited
	 */
	void deleteMembers() throws IllegalStateException;

	/**
	 * Insert a member in this group.
	 * Its functionallity is the same as addMember but throws IllegalStateException.
	 * 
	 * @throws IllegalStateException
	 *        If group is locked and cannot be edited
	 */
	void insertMember(String userId, String roleId, boolean active, boolean provided) throws IllegalStateException;

	/**
	 * Locks this group by an entity.
	 * 
	 * @param entity
	 *        The entity that is going to lock this group.
	 */
	void lockGroup(Entity entity);

	/**
	 * Locks this group by the reference of an entity.
	 * 
	 * @param reference
	 *        The reference of an entity that is going to lock this group.
	 */
	void lockGroup(String lock);

	/**
	 * Unlocks this group by an entity.
	 * 
	 * @param entity
	 *        The entity that will cease on locking this group.
	 */
	void unlockGroup(Entity entity);

	/**
	 * Unlocks this group by the reference of an entity.
	 * 
	 * @param reference
	 *        The reference of an entity that will cease on locking this group.
	 */
	void unlockGroup(String lock);

	/**
	 * Unlocks this group from any entity that might be locking it.
	 */
	void unlockGroup();

	/**
	 * Checks if this group is locked by any entity.
	 * 
	 * @return true if this group is locked by any entity, false if not.
	 */
	boolean isLocked();

	/**
	 * Checks if this group is locked by the reference of an entity.
	 * 
	 * @param reference
	 *        The reference of an entity where want to see if is locking this group.
	 * @return true if this group is locked by the reference of an entity, false if not.
	 */
	boolean isLocked(String lock);
}
