/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * This is an implementation of SakaiFacade interface and it provides all
 * necessary methods, which are depend on the Sakai Services. This will allow
 * the separation of Signup Tool and the Sakai Tools
 * </P>
 * 
 * @author gl256
 * 
 */
public class SakaiFacadeImpl implements SakaiFacade {

	private static Log log = LogFactory.getLog(SakaiFacadeImpl.class);

	private FunctionManager functionManager;

	/**
	 * set a Sakai FunctionManager object
	 * 
	 * @param functionManager
	 *            a Sakai FunctionManager object
	 */
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	private ToolManager toolManager;

	/**
	 * set a Sakai ToolManager object
	 * 
	 * @param toolManager
	 *            a Sakai ToolManager object
	 */
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	/**
	 * get the ToolManager object.
	 * 
	 * @return a ToolManager object.
	 */
	public ToolManager getToolManager() {
		return this.toolManager;
	}

	private SecurityService securityService;

	/**
	 * set a Sakai SecurityService object
	 * 
	 * @param securityService
	 *            a Sakai SecurityService object
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SessionManager sessionManager;

	/**
	 * set a Sakai SessionManager object
	 * 
	 * @param sessionManager
	 *            a Sakai SessionManager object
	 */
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SiteService siteService;

	/**
	 * set a Sakai SiteService object
	 * 
	 * @param siteService
	 *            a Sakai SiteService object
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * {@inheritDoc}
	 */
	public SiteService getSiteService() {
		return siteService;
	}

	private UserDirectoryService userDirectoryService;

	/**
	 * set a Sakai UserDirectoryService object
	 * 
	 * @param userDirectoryService
	 *            a Sakai UserDirectoryService object
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private CalendarService calendarService;

	/**
	 * set a Sakai CalendarService object
	 * 
	 * @param calendarService
	 *            a Sakai CalendarService object
	 */
	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	private ServerConfigurationService serverConfigurationService;

	/**
	 * {@inheritDoc}
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	/**
	 * set a Sakai ServerConfigurationService object
	 * 
	 * @param serverConfigurationService
	 *            a Sakai ServerConfigurationService object
	 */
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/**
	 * regist all the permission levels, which Signup Tool required. Place any
	 * code that should run when this class is initialized by spring here
	 */
	public void init() {
		log.debug("init");
		// register Sakai permissions for this tool
		functionManager.registerFunction(SIGNUP_VIEW);
		functionManager.registerFunction(SIGNUP_VIEW_ALL);
		functionManager.registerFunction(SIGNUP_ATTEND);
		functionManager.registerFunction(SIGNUP_ATTEND_ALL);
		functionManager.registerFunction(SIGNUP_CREATE_SITE);
		functionManager.registerFunction(SIGNUP_CREATE_GROUP);
		functionManager.registerFunction(SIGNUP_CREATE_GROUP_ALL);
		functionManager.registerFunction(SIGNUP_DELETE_SITE);
		functionManager.registerFunction(SIGNUP_DELETE_GROUP);
		functionManager.registerFunction(SIGNUP_DELETE_GROUP_ALL);
		functionManager.registerFunction(SIGNUP_UPDATE_SITE);
		functionManager.registerFunction(SIGNUP_UPDATE_GROUP);
		functionManager.registerFunction(SIGNUP_UPDATE_GROUP_ALL);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isUserAdmin(String userId) {
		return securityService.isSuperUser(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	/**
	 * get the User object
	 * 
	 * @param userId
	 *            a sakai internal user Id
	 * @return an User object
	 */
	public User getUser(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get user for id: " + userId);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserDisplayName(String userId) {
		try {
			return userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get user displayname for id: " + userId);
			return "--------";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentLocationId() {
		try {
			return toolManager.getCurrentPlacement().getContext();
		} catch (Exception e) {
			log.info("Failed to get current location id");
			return NO_LOCATION;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentPageId() {
		return getSiteSignupPageId(getCurrentLocationId());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSiteSignupPageId(String siteId) {
		try {
			Site appliedSite = siteService.getSite(siteId);
			String signupToolId = toolManager.getCurrentPlacement().getToolId();

			SitePage page = null;
			List pageList = appliedSite.getPages();
			for (int i = 0; i < pageList.size(); i++) {
				page = (SitePage) pageList.get(i);
				List pageToolList = page.getTools();
				if (pageToolList != null && !pageToolList.isEmpty()) {
					/* take first one only for efficiency */
					ToolConfiguration toolConf = (ToolConfiguration) pageToolList.get(0);
					if (toolConf != null) {
						String toolId = toolConf.getToolId();
						if (toolId.equalsIgnoreCase(signupToolId)) {
							return page.getId();
						}
					}
				}

			}
		} catch (Exception e) {
			log.warn("Failed to get current page id");
		}
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocationTitle(String locationId) {
		try {
			Site site = siteService.getSite(locationId);
			return site.getTitle();
		} catch (IdUnusedException e) {
			log.warn("Cannot get the info about locationId: " + locationId);
			return "----------";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupSite> getUserSites(String userId) {
		List<SignupSite> signupSites = new ArrayList<SignupSite>();
		List tempL = siteService.getSites(SiteService.SelectionType.ACCESS, null, null, null,
				SiteService.SortType.TITLE_ASC, null);
		for (Iterator iter = tempL.iterator(); iter.hasNext();) {
			Site element = (Site) iter.next();
			// exclude my workspace & admin related sites
			if (!siteService.isUserSite(element.getId()) && !siteService.isSpecialSite(element.getId())) {
				// if the tools is not available in the site then don't add it.
				Collection tools = element.getTools("sakai.signup");
				if (tools == null || tools.isEmpty())
					continue;

				SignupSite tmpSite = new SignupSite();
				tmpSite.setSiteId(element.getId());
				tmpSite.setTitle(element.getTitle());
				signupSites.add(tmpSite);
				List<SignupGroup> groupList = new ArrayList<SignupGroup>();
				Collection tmpGroup = element.getGroups();
				for (Iterator iterator = tmpGroup.iterator(); iterator.hasNext();) {
					Group grp = (Group) iterator.next();
					SignupGroup sgrp = new SignupGroup();
					sgrp.setGroupId(grp.getId());
					sgrp.setTitle(grp.getTitle());
					groupList.add(sgrp);
				}
				tmpSite.setSignupGroups(groupList);
			}

		}
		return signupSites;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowedGroup(String userId, String permission, String siteId, String groupId) {
		return isAllowed(userId, permission, siteService.siteGroupReference(siteId, groupId));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowedSite(String userId, String permission, String siteId) {
		return isAllowed(userId, permission, siteService.siteReference(siteId));
	}

	/* check permission */
	private boolean isAllowed(String userId, String permission, String realmId) {
		if (securityService.unlock(userId, permission, realmId)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserId(String eid) throws UserNotDefinedException {
		return userDirectoryService.getUserId(eid);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	// TODO: should we check view permission for each attendee(user)
	public List<SignupUser> getAllUsers(SignupMeeting meeting) {
		List<SignupSite> signupSites = meeting.getSignupSites();
		Set<SignupUser> signupUsers = new TreeSet<SignupUser>();
		for (SignupSite signupSite : signupSites) {
			if (signupSite.isSiteScope()) {
				getUsersForSiteWithSiteScope(signupUsers, signupSite);
			} else {
				List<SignupGroup> signupGroups = signupSite.getSignupGroups();
				for (SignupGroup signupGroup : signupGroups) {
					getUsersForGroup(signupUsers, signupSite, signupGroup);
				}
			}

		}
		return new ArrayList<SignupUser>(signupUsers);
	}

	/* get all users in a specific group */
	@SuppressWarnings("unchecked")
	private void getUsersForGroup(Set<SignupUser> signupUsers, SignupSite signupSite, SignupGroup signupGroup) {
		Site site = null;
		try {
			site = siteService.getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error("Cannot get the info about siteId: " + e.getMessage());
			return;
		}
		Group group = site.getGroup(signupGroup.getGroupId());
		if (group == null)
			return;
		Set<Member> members = group.getMembers();
		SignupUser signupUser = null;
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedGroup(member.getUserId(), SIGNUP_VIEW, site.getId(), group.getId()) || isAllowedSite(
							member.getUserId(), SIGNUP_VIEW_ALL, site.getId()))) {
				User user = getUser(member.getUserId());
				if (user == null) {
					log.info("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
					signupUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());
					processAddOrUpdateSignupUsers(signupUsers, signupUser);
					continue;
				}

				signupUser = new SignupUser(member.getUserEid(), member.getUserId(), user.getFirstName(), user
						.getLastName(), member.getRole(), site.getId(), site.isPublished());
				processAddOrUpdateSignupUsers(signupUsers, signupUser);
				// comment: member.getUserDisplayId() not used
			}
		}
	}

	private boolean hasPredefinedViewPermisson(Member member) {
		/*
		 * just assume student role has the signup.view permission and could add
		 * more roles to exclude
		 */
		return STUDENT_ROLE_ID.equalsIgnoreCase(member.getRole().getId());
	}

	/* get all users in a site */
	@SuppressWarnings("unchecked")
	private void getUsersForSiteWithSiteScope(Set<SignupUser> signupUsers, SignupSite signupSite) {
		Site site = null;
		try {
			site = siteService.getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}

		if (site == null)
			return;

		boolean isSitePublished = site.isPublished();
		SignupUser signupUser = null;
		Set<Member> members = site.getMembers();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedSite(member.getUserId(), SIGNUP_VIEW, site.getId()) || isAllowedSite(member
							.getUserId(), SIGNUP_VIEW_ALL, site.getId()))) {
				User user = getUser(member.getUserId());
				if (user == null) {
					log.info("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
					signupUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());
					processAddOrUpdateSignupUsers(signupUsers, signupUser);
					continue;
				}

				signupUser = new SignupUser(member.getUserEid(), member.getUserId(), user.getFirstName(), user
						.getLastName(), member.getRole(), site.getId(), site.isPublished());
				processAddOrUpdateSignupUsers(signupUsers, signupUser);

			}
		}
	}

	/**
	 * It will make sure that the user has a point to a published site and
	 * possible with the same creator's siteId if possible
	 * 
	 * @param signupUsers
	 *            a List of SignupUser objects.
	 * @param signupUser
	 *            a SignupUser object
	 */
	private void processAddOrUpdateSignupUsers(Set<SignupUser> signupUsers, SignupUser signupUser) {
		boolean update = true;
		if (!signupUsers.isEmpty() && signupUsers.contains(signupUser)) {
			for (SignupUser sUser : signupUsers) {
				if (sUser.equals(signupUser)) {
					if (!sUser.isPublishedSite() && signupUser.isPublishedSite() || signupUser.isPublishedSite()
							&& signupUser.getMainSiteId().equals(getCurrentLocationId())) {
						update = true;
					} else {
						update = false;
					}

					break;
				}
			}
		}

		if (update)
			signupUsers.add(signupUser);
	}

	/**
	 * {@inheritDoc}
	 */
	public Calendar getCalendar(String siteId) throws IdUnusedException, PermissionException {
		String calendarId = calendarService.calendarReference(siteId, SiteService.MAIN_CONTAINER);
		Calendar calendar = calendarService.getCalendar(calendarId);
		return calendar;
	}

	/**
	 * {@inheritDoc}
	 */
	public Calendar getCalendarById(String calendarId) throws IdUnusedException, PermissionException {
		Calendar calendar = calendarService.getCalendar(calendarId);
		return calendar;
	}

	/**
	 * {@inheritDoc}
	 */
	public Group getGroup(String siteId, String groupId) throws IdUnusedException {
		Site site = siteService.getSite(siteId);
		return site.getGroup(groupId);
	}

	private TimeService timeService;

	/**
	 * {@inheritDoc}
	 */
	public TimeService getTimeService() {
		return timeService;
	}

	/**
	 * This is a setter.
	 * 
	 * @param timeService
	 *            a TimeService object.
	 */
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

}
