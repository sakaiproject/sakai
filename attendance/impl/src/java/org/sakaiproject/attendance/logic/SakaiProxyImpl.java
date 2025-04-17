/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.logic;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.*;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Implementation of our SakaiProxy API
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Slf4j
public class SakaiProxyImpl implements SakaiProxy {

	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentSiteTitle() {
		try {
			return siteService.getSite(getCurrentSiteId()).getTitle();
		} catch (IdUnusedException e) {
			log.error("getCurrentSiteTitle()", e);
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public User getCurrentUser() {
		return getUser(getCurrentUserId());
	}

	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserDisplayName() {
	   return userDirectoryService.getCurrentUser().getDisplayName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserRoleInCurrentSite() {
		return getCurrentUserRole(getCurrentSiteId());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserRole(String siteId) {
		if (securityService.unlock("section.role.instructor", "/site/" + siteId)) {
			return "Instructor";
		}
		else if (securityService.unlock("section.role.ta", "/site/" + siteId)) {
			return "TA";
		}
		else if (securityService.unlock("section.role.student", "/site/" + siteId)) {
			return "Student";
		}
		return authzGroupService.getUserRole(getCurrentUserId(), "/site/" + siteId);
	}

	/**
 	* {@inheritDoc}
 	*/
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}

	/**
 	* {@inheritDoc}
 	*/
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}

	/**
 	* {@inheritDoc}
 	*/
	public boolean getConfigParam(String param, boolean dflt) {
		return serverConfigurationService.getBoolean(param, dflt);
	}

	/**
 	* {@inheritDoc}
 	*/
	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getCurrentSiteMembershipIds() {
		return getSiteMembershipIds(getCurrentSiteId());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getSiteMembershipIds(final String siteId) {
		List<User> members = getSiteMembership(siteId);
		List<String> studentIds = new ArrayList<>();
		members.forEach(user-> studentIds.add(user.getId()));
		return studentIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getCurrentSiteMembership() {
		return getSiteMembership(getCurrentSiteId());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getSiteMembership(final String siteId) {
		return securityService.unlockUsers("section.role.student", "/site/" + siteId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getGroupMembershipIdsForCurrentSite(String groupId) {
		return getGroupMembershipIds(getCurrentSiteId(), groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getGroupMembershipIds(String siteId, String groupId) {
		List<String> returnList = new ArrayList<String>();
		for(User user : getGroupMembership(siteId, groupId)) {
			returnList.add(user.getId());
		}
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getGroupMembershipForCurrentSite(String groupId) {
		return getGroupMembership(getCurrentSiteId(), groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getGroupMembership(String siteId, String groupId) {
		try {
			Group group = siteService.getSite(siteId).getGroup(groupId);
			if(group != null) {
				return securityService.unlockUsers("section.role.student", group.getReference());
			}
		} catch (IdUnusedException e) {
			log.error("Unable to get group membership", e);
		}
		return new ArrayList<User>();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getSectionMembership(String siteId, String groupId) {
		try {
			Group group = siteService.getSite(siteId).getGroup(groupId);
			if(group != null && group.getProviderGroupId() != null) {
				return securityService.unlockUsers("section.role.student", group.getReference());
			}
		} catch (IdUnusedException e) {
			log.error("Unable to get group membership", e);
		}
		return new ArrayList<User>();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAvailableGroupsForSite(String siteId) {
		try {
			List<String> returnList = new ArrayList<String>();
			Site site = siteService.getSite(siteId);
			for(Group group : site.getGroups()) {
				returnList.add(group.getId());
			}
			return returnList;
		} catch (IdUnusedException e) {
			log.error("getAvailableGroupIdsForSite " + siteId + " IdUnusedException");
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAvailableGroupsForCurrentSite() {
		return getAvailableGroupsForSite(getCurrentSiteId());
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUser(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.error("Unable to get user " + userId + " " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final User getUserByEID(String userEid) {
		try {
			return userDirectoryService.getUserByEid(userEid);
		} catch (UserNotDefinedException e) {
			log.error("Unable to get user " + userEid + " " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserSortName(String userId) {
		User u = getUser(userId);
		if(u != null){
			return u.getSortName();
		}

		return "";
	}

	/*
	* {@inheritDoc}
	 */
	public String getUserSortNameByEID(final String userEid) {
		User u = getUserByEID(userEid);
		if(u != null){
			return u.getSortName();
		}
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserDisplayId(String userId) {
		User u = getUser(userId);

		if(u != null) {
			return u.getDisplayId();
		}

		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGroupTitleForCurrentSite(String groupId) {
		return getGroupTitle(getCurrentSiteId(), groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGroupTitle(String siteId, String groupId) {
		try {
			if(siteId != null && !siteId.isEmpty() && groupId != null && !groupId.isEmpty()) {
				return siteService.getSite(siteId).getGroup(groupId).getTitle();
			}
		} catch (IdUnusedException e) {
			log.error("Unable to get group title", e);
			e.printStackTrace();
		}
		return "";
	}

	public String getUserGroupWithinSite(List<String> groupIds, String userId, String siteId){
		ArrayList<String> content = new ArrayList<String>(groupIds);
		for (int count=0; count<content.size(); count++){	//add the rest of the path in for each groupId.
			String fullPathBuilder = "/site/"+siteId+"/group/"+content.get(count);
			content.set(count, fullPathBuilder);
		}
		List<AuthzGroup> results = authzGroupService.getAuthzUserGroupIds(content, userId);
		Iterator traverseResults = results.iterator();
		while(traverseResults.hasNext()){
			String now = (String) traverseResults.next();
			try {
				AuthzGroup nowGroup =  authzGroupService.getAuthzGroup(now);
				if (StringUtils.isBlank(nowGroup.getProviderGroupId())){	//return Blank if Provider is blank. this will happen for ad-hoc groups but get passed over for legitimate Sections.
					return "";
				}
				return getGroupTitle(siteId, nowGroup.getId());
			} catch (GroupNotDefinedException e) {
				return "";
			}
		}
		return "";	//return Nothing for no results.
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.debug("SakaiProxyImpl init()");
	}

	private Preferences getCurrentUserPreferences() {
		return preferencesService.getPreferences(getCurrentUserId());
	}

	private Session getCurrentSession() {
		return sessionManager.getCurrentSession();
	}

	@Getter @Setter
	private ToolManager toolManager;

	@Getter @Setter
	private SessionManager sessionManager;

	@Getter @Setter
	private UserDirectoryService userDirectoryService;

	@Getter @Setter
	private SecurityService securityService;

	@Getter @Setter
	private EventTrackingService eventTrackingService;

	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;

	@Getter @Setter
	private SiteService siteService;

	@Getter @Setter
	private PreferencesService preferencesService;

	@Getter @Setter
	private AuthzGroupService authzGroupService;
}
