/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.roster.api;

import java.util.List;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * Roster interface to Sakai functionality.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public interface SakaiProxy {

	public String getCurrentUserId();
	
	public String getCurrentSiteId();
	
	public String getDefaultSortColumn();
	
	public Boolean getFirstNameLastName();
	
	public Boolean getHideSingleGroupFilter();
	
	public Boolean getViewEmailColumn();
	
	public List<String> getRoleTypes(String siteId);
	
	public Site getSite(String siteId);
	
	/**
	 * Returns a list of <code>RosterMember</code>s for the specified site
	 * group. Set <code>groupId</code> to <code>null</code> to retrieve all
	 * members for a site.
	 * 
	 * @param siteId
	 * @param groupId
	 * @return
	 */
	public List<RosterMember> getMembership(String siteId, String groupId);

	
	public String getCurrentSessionUserId();
	
	public User getUser(String userId);
	
	public Boolean hasUserPermission(String userId, String permission, String siteId);
	
//	public List<CourseSection> getViewableSectionsForCurrentUser();
//	public List<CourseSection> getViewableEnrollmentStatusSectionsForCurrentUser();
}
