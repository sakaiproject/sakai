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

/**
 * Roster interface to Sakai functionality.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public interface SakaiProxy {

	public final static String OVERVIEW_STATE = "overview";
	public final static String PICTURES_STATE = "pics";
	
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
	 * 
	 * @return the value of the <code>roster_view_email</code> Sakai property.
	 */
	public Boolean getViewEmail();
	
	/**
	 * Returns the value of the <code>roster.display.userDisplayId</code> Sakai property.
	 * 
	 * @return the value of the <code>roster.display.userDisplayId</code> Sakai property.
	 */
	public Boolean getViewUserDisplayId();
	
	/**
	 * Returns the value of the <code>roster.usePicturesAsDefaultView</code> Sakai property.
	 * 
	 * @return the value of the <code>roster.usePicturesAsDefaultView</code> Sakai property.
	 */
	public Boolean getUsePicturesAsDefaultView();
		
	/**
	 * Returns the list of viewable members from the specified site.
	 * 
	 * @param siteId the ID of the site.
	 * @return the list of viewable members from the specified site.
	 */
	public List<RosterMember> getSiteMembership(String siteId);
	
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
}
