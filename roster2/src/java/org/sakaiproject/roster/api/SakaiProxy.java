/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.api;

import java.util.List;

/**
 * Roster interface to Sakai functionality.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public interface SakaiProxy {

	public final static String[] ROSTER_STATES = new String[] { "overview",
			"pics", "group_membership", "status" };

	public final static String DEFAULT_SORT_COLUMN = "sortName";
	public final static Boolean DEFAULT_FIRST_NAME_LAST_NAME = false;
	public final static Boolean DEFAULT_HIDE_SINGLE_GROUP_FILTER = false;
	public final static Boolean DEFAULT_VIEW_EMAIL = true;
	public final static Boolean DEFAULT_VIEW_USER_DISPLAY_ID = true;
	public final static Integer DEFAULT_ROSTER_STATE = 0;
	
	/**
	 * Returns the ID of the current user.
	 * 
	 * @return the ID of the current user.
	 */
	public String getCurrentUserId();
		
	/**
	 * Returns the ID of the current site.
	 * 
	 * @return the ID of the current site.
	 */
	public String getCurrentSiteId();
	
	/**
	 * Returns the value of the <code>roster.default.state</code> Sakai
	 * property.
	 * 
	 * @return the value of the <code>roster.default.state</code> Sakai
	 *         property.
	 */
	public Integer getDefaultRosterState();
	
	/**
	 * Returns the value of the <code>roster.default.state</code> Sakai property
	 * mapped to the corresponding state string.
	 * 
	 * @return the value of the <code>roster.default.state</code> Sakai property
	 *         mapped to the corresponding state string.
	 */
	public String getDefaultRosterStateString();
	
	/**
	 * Returns the value of the <code>roster.defaultSortColumn</code> Sakai
	 * property.
	 * 
	 * @return the value of the <code>roster.defaultSortColumn</code> Sakai
	 * property.
	 */
	public String getDefaultSortColumn();
	
	/**
	 * Returns the value of the <code>roster.display.firstNameLastName</code>
	 * Sakai property.
	 * 
	 * @return the value of the <code>roster.display.firstNameLastName</code>
	 *         Sakai property.
	 */
	public Boolean getFirstNameLastName();
	
	/**
	 * Returns the value of the <code>roster.display.hideSingleGroupFilter</code>
	 * Sakai property.
	 * 
	 * @return the value of the <code>roster.display.hideSingleGroupFilter</code>
	 *         Sakai property.
	 */
	public Boolean getHideSingleGroupFilter();
		
	/**
	 * Returns the value of the <code>roster_view_email</code> Sakai property.
	 * Also checks user has permission in the site.
	 * @return the value of the <code>roster_view_email</code> Sakai property.
	 */
	public Boolean getViewEmail();
	
	/**
	 * Returns the value of the <code>roster_view_email</code> Sakai property.
	 * Also checks user has permission in the site.
	 * @return the value of the <code>roster_view_email</code> Sakai property.
	 */
	public Boolean getViewEmail(String siteId);
	
	/**
	 * Returns the value of the <code>roster.display.userDisplayId</code> Sakai property.
	 * 
	 * @return the value of the <code>roster.display.userDisplayId</code> Sakai property.
	 */
	public Boolean getViewUserDisplayId();
		
	/**
	 * Returns the list of viewable members from the specified site.
	 * 
	 * @param siteId the ID of the site.
	 * @param includeConnectionStatus specify <code>true</code> if
	 *            <code>RosterMember</code> objects should be populated with the
	 *            Profile2 connection statuses to the current user, else specify
	 *            <code>false</code>.
	 * @return the list of viewable members from the specified site.
	 */
	public List<RosterMember> getSiteMembership(String siteId, boolean includeConnectionStatus);
	
	/**
	 * Returns the list of viewable members from the specified group.
	 * 
	 * @param siteId the ID of the site the group belongs to.
	 * @param groupId the ID of the group.
	 * @return the list of viewable members from the specified group.
	 */
	public List<RosterMember> getGroupMembership(String siteId, String groupId);
	
	/**
	 * Returns site information for the specified site.
	 * 
	 * @param siteId the ID of the site.
	 * @return site information for the specified site.
	 */
	public RosterSite getRosterSite(String siteId);
	
	/**
	 * Returns the enrollment set members for the specified site and enrollment
	 * set.
	 * 
	 * @param siteId the ID of the site.
	 * @param enrollmentSetId the ID of the enrollment set.
	 * @return the enrollment set members for the specified site and enrollment
	 *         set.
	 */
	public List<RosterMember> getEnrollmentMembership(String siteId, String enrollmentSetId);
	
	/**
	 * Returns whether or not the specified user is allowed the specified
	 * permission within the specified site.
	 * 
	 * @param userId the ID of the user.
	 * @param permission the permission.
	 * @param siteId the ID of the site to check.
	 * @return <code>true</code> if the user has permission, otherwise returns
	 *         <code>false</code>.
	 */
	public Boolean hasUserSitePermission(String userId, String permission, String siteId);
	
	/**
	 * Returns whether or not the specified user is allowed the specified
	 * permission within the specified site group.
	 * 
	 * @param userId the ID of the user.
	 * @param permission the permission.
	 * @param siteId the ID of the <code>Site</code> the group belongs to.
	 * @param groupId the ID of th3e group.
	 * @return <code>true</code> if the user has permission, otherwise returns
	 *         <code>false</code>.
	 */
	public Boolean hasUserGroupPermission(String userId, String permission,
			String siteId, String groupId);
	
	/**
	 * Returns the name of the skin of the current site.
	 * 
	 * @return the name of the skin of the current site.
	 */
	public String getSakaiSkin();

	/**
	 * Returns whether or not the current user is a super user.
	 * 
	 * @return <code>true</code> if the current user is a super user, else
	 *         returns <code>false</code.
	 */
	public boolean isSuperUser();
	
	/**
	 * Checks if the user has site.upd in the given site
	 */
	public boolean isSiteMaintainer(String siteId);
}
