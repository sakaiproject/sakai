/**
 * Copyright (c) 2010-2017 The Apereo Foundation
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
import java.util.Map;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.user.api.User;


/**
 * Roster interface to Sakai functionality.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public interface SakaiProxy {

	public final static String[] ROSTER_STATES = new String[] { "overview",
			"pics", "group_membership", "status" };

    public final static String MEMBERSHIPS_CACHE = "org.sakaiproject.roster.sortedMembershipsCache";
    public final static String ENROLLMENTS_CACHE = "org.sakaiproject.roster.sortedEnrollmentsCache";
    public final static String SEARCH_INDEX_CACHE = "org.sakaiproject.roster.searchIndexCache";

	public final static String DEFAULT_SORT_COLUMN = "sortName";
	public final static Boolean DEFAULT_FIRST_NAME_LAST_NAME = false;
	public final static Boolean DEFAULT_HIDE_SINGLE_GROUP_FILTER = false;
	public final static Boolean DEFAULT_VIEW_EMAIL = true;
	public final static Boolean DEFAULT_VIEW_CONNECTIONS = true;
	public final static Boolean DEFAULT_VIEW_USER_DISPLAY_ID = true;
	public final static Boolean DEFAULT_VIEW_USER_PROPERTIES = true;
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
	 * Returns the locale_string property of the current site.
	 * 
	 * @return the locale_string property of the current site.
	 */
	public String getCurrentSiteLocale();
	
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
	 * Returns the value of the <code>roster_view_connections</code> Sakai property.
	 * Note: if Profile2 connections (profile2.connections.enabled) is false, this
	 * will also be automatically false.
	 * @return the value of the <code>roster_view_connections</code> Sakai property.
	 */
	public Boolean getViewConnections();
	
	/**
	 * Returns the value of the <code>roster.display.userDisplayId</code> Sakai property.
	 * 
	 * @return the value of the <code>roster.display.userDisplayId</code> Sakai property.
	 */
	public Boolean getViewUserDisplayId();

	/**
	 * Returns the value of the <code>roster_view_user_properties</code> Sakai property.
	 *
	 * @return the value of the <code>roster_view_user_properties</code> Sakai property.
	 */
	public Boolean getViewUserProperty();

	/**
	 * Returns the value of the <code>roster_view_user_properties</code> Sakai property.
	 *
	 * @param siteId a site
	 * @return the value of the <code>roster_view_user_properties</code> Sakai property.
	 */
	public Boolean getViewUserProperty(String siteId);

	/**
	 * Returns the value of the <code>roster.display.officialPicturesByDefault</code> Sakai property.
	 * 
	 * @return the value of the <code>roster.display.officialPicturesByDefault</code> Sakai property.
	 */
    public Boolean getOfficialPicturesByDefault();
	
	/**
	 * Returns the value of the <code>roster.display.pageSize</code> Sakai property.
	 * 
	 * @return the value of the <code>roster.display.pageSize</code> Sakai property.
	 */
	public int getPageSize();

	public Site getSite(String siteId);
		
	public List<RosterMember> getMembership(String currentUserId, String siteId, String groupId, String roleId, String enrollmentSetId, String enrollmentStatus);

	public RosterMember getMember(String siteId, String userId, String enrollmentSetId);

	public List<User> getSiteUsers(String siteId);
	
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
	//public List<RosterMember> getEnrollmentMembership(String siteId, String enrollmentSetId);
	
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
	//public String getSakaiSkin();

	/**
	 * Returns whether or not the current user is a super user.
	 * 
	 * @return <code>true</code> if the current user is a super user, else
	 *         returns <code>false</code>.
	 */
	public boolean isSuperUser();
	
	/**
	 * Checks if the user has site.upd in the given site
	 */
	public boolean isSiteMaintainer(String siteId);

	/**
	 * Attempts to retrieve the search index for the specified site.
	 */
    public Map<String, String> getSearchIndex(String siteId);

    public Map<String, SitePresenceTotal> getPresenceTotalsForSite(String siteId);

    public boolean getShowVisits();
}
