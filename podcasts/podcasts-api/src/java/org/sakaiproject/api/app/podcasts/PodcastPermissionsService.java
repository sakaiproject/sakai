/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.api.app.podcasts;

import java.util.Collection;
import java.util.Date;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.GroupAwareEntity;

public interface PodcastPermissionsService {
	/** This string gives the update function (permission) string for checking permissions **/
	public static final String UPDATE_PERMISSIONS = "site.upd";
	public static final String NEW_PERMISSIONS = "content.new";
	public static final String READ_PERMISSIONS = "content.read";
	public static final String REVISE_ANY_PERMISSIONS = "content.revise.any";
	public static final String REVISE_OWN_PERMISSIONS = "content.revise.own";
	public static final String DELETE_ANY_PERMISSIONS = "content.delete.any";
	public static final String DELETE_OWN_PERMISSIONS = "content.delete.own";
	public static final String ALL_GROUPS_PERMISSIONS = "content.all.groups";
	public static final String HIDDEN_PERMISSIONS = "content.hidden";

	/**
	 * Determines if authenticated user has 'read' access to podcast collection folder
	 * 
	 * @param id
	 * 			The id for the podcast collection folder
	 * 
	 * @return
	 * 		TRUE - has read access, FALSE - does not
	 */
	public boolean allowAccess(String id);

	/**
	 * Determines if user can modify the site. Used by feed.
	 * 
	 * @return boolean true if user can modify, false otherwise
	 */
	public boolean canUpdateSite();
	public boolean canUpdateSite(String siteId);

	/**
	 * Determines if user has the function (permission) passed in
	 * Used by feed since there is no site id in context
	 * 
	 * @param function
	 * 			The permission to check
	 * @param resourceId
	 * 			The id for the podcasts folder
	 * @param siteId
	 * 			The id for the site to check (used by feed)
	 *
	 * @return boolean TRUE if user has function (permission) or site.upd, FALSE otherwise.
	 */
	public boolean hasPerm(String function, String resourceId);
	public boolean hasPerm(String function, String resourceId, String siteId);

	/**
	 * Determine if this entity has been restricted to specific group(s)
	 */
	public boolean isGrouped(GroupAwareEntity entity);

	/**
	 * Determine if current user can access group restriced entity
	 */
	public boolean canAccessViaGroups(Collection groups, String siteId);

	/**
	 * Returns TRUE if resource has HIDDEN property set, before its release
	 * date or after its retract date
	 */
	public boolean isResourceHidden(ContentEntity podcastResource, Date tempDate);
}
