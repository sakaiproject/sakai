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
}
