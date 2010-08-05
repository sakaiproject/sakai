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
package org.sakaiproject.roster.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.roster.api.RosterGroup;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.RosterSite;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <code>SakaiProxy</code> acts as a proxy between Roster and Sakai components.
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Log log = LogFactory.getLog(SakaiProxyImpl.class);
	
	public final static String DEFAULT_SORT_COLUMN = "sortName";
	public final static Boolean DEFAULT_FIRST_NAME_LAST_NAME = false;
	public final static Boolean DEFAULT_HIDE_SINGLE_GROUP_FILTER = false;
	public final static Boolean DEFAULT_VIEW_EMAIL_COLUMN = true;
	
	//private AuthzGroupService authzGroupService = null;
	//private CourseManagementService courseManagementService;
	private FunctionManager functionManager = null;
	//private SecurityService securityService = null;
	private ServerConfigurationService serverConfigurationService = null;
	private SessionManager sessionManager = null;
	private SiteService siteService;
	private ToolManager toolManager = null;
	private UserDirectoryService userDirectoryService = null;
	
	/**
	 * Creates a new instance of <code>SakaiProxyImpl</code>
	 */
	public SakaiProxyImpl() {

		log.info("SakaiProxy initialized");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserId() {
		
		// TODO should this be done via SessionManager instead?
		
		if (null == userDirectoryService.getCurrentUser()) {
			log.warn("cannot retrieve current user");
			return null;
		}
		
		return userDirectoryService.getCurrentUser().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDefaultSortColumn() {
		
		return serverConfigurationService
				.getString("roster.defaultSortColumn", DEFAULT_SORT_COLUMN);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getFirstNameLastName() {

		return serverConfigurationService.getBoolean(
				"roster.display.firstNameLastName", DEFAULT_FIRST_NAME_LAST_NAME);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getHideSingleGroupFilter() {

		return serverConfigurationService.getBoolean(
				"roster.display.hideSingleGroupFilter",
				DEFAULT_HIDE_SINGLE_GROUP_FILTER);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getViewEmailColumn() {

		return serverConfigurationService.getBoolean("roster_view_email",
				DEFAULT_VIEW_EMAIL_COLUMN);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getMembership(String siteId, String groupId) {

		List<RosterMember> rosterMembers = new ArrayList<RosterMember>();

		if (false == hasUserPermission(getCurrentUserId(),
				RosterFunctions.ROSTER_FUNCTION_VIEWALL, siteId)) {
			
			return rosterMembers;
		}

		try {

			Site site = siteService.getSite(siteId);

			Set<Member> membership;

			if (null == site.getGroup(groupId)) {
				membership = site.getMembers();
			} else {
				membership = site.getGroup(groupId).getMembers();
			}

			for (Member member : membership) {

				String userId = member.getUserId();

				RosterMember rosterMember = new RosterMember();
				rosterMember.setUserId(userId);
				rosterMember.setDisplayId(member.getUserDisplayId());
				rosterMember.setRole(member.getRole().getId());

				User user = userDirectoryService.getUser(userId);

				rosterMember.setEmail(user.getEmail());
				rosterMember.setDisplayName(user.getDisplayName());
				rosterMember.setSortName(user.getSortName());

				Collection<Group> groups = site.getGroupsWithMember(userId);
				Iterator<Group> groupIterator = groups.iterator();

				while (groupIterator.hasNext()) {
					Group group = groupIterator.next();
					
					rosterMember.addGroup(group.getId(), group.getTitle());
				}

				rosterMembers.add(rosterMember);
			}

		} catch (IdUnusedException e) {
			e.printStackTrace();
		} catch (UserNotDefinedException e) {
			e.printStackTrace();
		}

		return rosterMembers;
	}
	
	public RosterSite getSiteDetails(String siteId) {
		
		String currentUserId = getCurrentSessionUserId();
		if (null == currentUserId) {
			return null;
		}
		
		Site site = getSite(siteId);
		// only if user is a site member
		if (null == site.getMember(currentUserId)) {
			return null;
		}
		
		if (null == site) {
			return null;
		}
		
		RosterSite rosterSite = new RosterSite();
		
		rosterSite.setId(site.getId());
		rosterSite.setTitle(site.getTitle());
		
		List<RosterGroup> siteGroups = new ArrayList<RosterGroup>();
		
		for (Group group : site.getGroups()) {
			RosterGroup rosterGroup = new RosterGroup();
			rosterGroup.setId(group.getId());
			rosterGroup.setTitle(group.getTitle());
			
			List<String> userIds = new ArrayList<String>();
			for (Member member : group.getMembers()) {
				userIds.add(member.getUserId());
			}
			
			rosterGroup.setUserIds(userIds);
			
			siteGroups.add(rosterGroup);
		}
		
		rosterSite.setSiteGroups(siteGroups);
		return rosterSite;
	}

	/**
	 * {@inheritDoc}
	 */
//	public List<RosterMember> getMembershipByGroup(String siteId) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	/**
	 * {@inheritDoc}
	 */
	public Site getSite(String siteId) {

		try {
			return siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUser(String userId) {
		
		if (StringUtils.isBlank(userId)) {
			return null;
		}
		
		User user = null;
		try {
			user = userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {

			e.printStackTrace();
		}
		return user;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getRoleTypes(String siteId) {
		
		List<String> roleTypes = new ArrayList<String>();
		try {
			Site site = siteService.getSite(siteId);
			Set<Role> roles = site.getRoles();
			
			for (Role role : roles) {
				roleTypes.add(role.getId());
			}
			
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}

		return roleTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentSessionUserId() {
		
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean hasUserPermission(String userId, String permission,
			String siteId) {

		try {
			Site site = siteService.getSite(siteId);
			Role userRole = site.getUserRole(userId);
			
			if (null != userRole && userRole.getAllowedFunctions().contains(permission)) {
				return true;
			}
			
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/* Spring injections */
	
//	public void setCourseManagementService(
//			CourseManagementService courseManagementService) {
//		this.courseManagementService = courseManagementService;
//	}
	
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}
	
//	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
//		this.authzGroupService = authzGroupService;
//	}
	
//	public void setSecurityService(SecurityService securityService) {
//		this.securityService = securityService;
//	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	public void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
}
