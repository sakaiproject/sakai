/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/impl/src/java/org/sakaiproject/signup/logic/SakaiFacadeImpl.java $
 * $Id: SakaiFacadeImpl.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
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
	
	private AuthzGroupService authzGroupService;
	public AuthzGroupService getAuthzGroupService() {
		return authzGroupService;
	}
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService=authzGroupService;
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
	 * {@inheritDoc}
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
	public User getUserQuietly(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.debug("User with id: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkForUser(String userId) {
		try {
			User u = userDirectoryService.getUser(userId);
			if (u != null) {
				return true;
			} 
		} catch (UserNotDefinedException e) {
			log.debug("User with id: " + userId + " does not exist : " + e.getClass() + " : " + e.getMessage());
		}
		return false;
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
			String signupToolId=null;
			try{
				signupToolId = toolManager.getCurrentPlacement().getToolId();
			}
			catch (Exception e){
				/*for case: cronJob or web-service etc.*/
				signupToolId = toolManager.getTool("sakai.signup").getId();
			}

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
	public List<String> getUserPublishedSiteIds(String userId){
		List<String> siteIds = new ArrayList<String>();
		/*all sites for current user*/
		List<Site> tempL = siteService.getSites(SiteService.SelectionType.ACCESS, null, null, null,
				SiteService.SortType.TITLE_ASC, null);
		
		for (Iterator iter = tempL.iterator(); iter.hasNext();) {
			Site element = (Site) iter.next();
			// exclude my workspace & admin related sites
			if (!siteService.isUserSite(element.getId()) && !siteService.isSpecialSite(element.getId())) {
				// if the tools is not available in the site then don't add it.
				Collection tools = element.getTools("sakai.signup");
				if (tools == null || tools.isEmpty())
					continue;

				if(element.isPublished()){
					siteIds.add(element.getId());
				}
			}

		}
		
		return siteIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupSite> getUserSites(String userId) {
		List<SignupSite> signupSites = new ArrayList<SignupSite>();
		List<Site> tempL = siteService.getSites(SiteService.SelectionType.ACCESS, null, null, null,
				SiteService.SortType.TITLE_ASC, null);
		
		/*Case: Admin is not a member of current site and add it 
		 * in order to let Admin create meetings*/
		if(isUserAdmin(getCurrentUserId())){
			String currentSitId = getCurrentLocationId();
			Site curSite=null;
			try {
				curSite =getSiteService().getSite(currentSitId);
			} catch (IdUnusedException e) {
				log.warn("IdUnusedException:" + e.getMessage());
			}
			if(currentSitId !=null && curSite !=null){
				boolean foundSite=false;
				for (Site s : tempL) {
					if(currentSitId.equals(s.getId())){
						foundSite=true;
						break;
					}						
				}
				if(!foundSite){
					tempL.add(curSite);
				}
			}
		}
		
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
										
					//signup-51 don't show the hidden groups (if property exists, skip this group)
					String gProp = grp.getProperties().getProperty(GROUP_PROP_SIGNUP_IGNORE);
    				if (gProp != null && gProp.equals(Boolean.TRUE.toString())) {
    					continue;
    				}
    				    				
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
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<SignupUser> getAllPossibleAttendees(SignupMeeting meeting){
		List<SignupSite> signupSites = meeting.getSignupSites();
		Set<SignupUser> signupUsers = new TreeSet<SignupUser>();
		for (SignupSite signupSite : signupSites) {
			if (signupSite.isSiteScope()) {
				getAttendeesForSiteWithSiteScope(signupUsers, signupSite);
			} else {
				List<SignupGroup> signupGroups = signupSite.getSignupGroups();
				for (SignupGroup signupGroup : signupGroups) {
					getAttendeesForGroup(signupUsers, signupSite, signupGroup);
				}
			}

		}
		return new ArrayList<SignupUser>(signupUsers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<SignupUser> getAllPossbileCoordinators(SignupMeeting meeting) {
		List<SignupUser> coordinators = new ArrayList<SignupUser>();
		List<SignupUser> signUpUsers = getAllUsers(meeting);
		for (SignupUser u : signUpUsers) {
			if(hasPermissionToCreate(meeting,u.getInternalUserId())){
				coordinators.add(u);
			}
			
		}
		
		return coordinators;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public SignupUser getSignupUser(SignupMeeting meeting, String userId){
		SignupUser signupUser=null;
		List<SignupSite> signupSites = meeting.getSignupSites();
		SignupSite currentSignupSite=null;
		for (SignupSite signupSite : signupSites) {
			Site site = null;
			try {
				site = siteService.getSite(signupSite.getSiteId());
			} catch (IdUnusedException e) {
				log.error(e.getMessage(), e);
			}

			if (site == null)
				continue;
			
			if(site.getId().equals(signupSite.getSiteId()))
				currentSignupSite=signupSite;
			
			/*Case 1: User is required to be a site member*/
			Member member = site.getMember(userId);
			if (member ==null || !member.isActive())
				continue;
			
			if (hasPermissionToAttend(signupSite,userId)) {
				User user = getUser(member.getUserId());
				if (user != null) {
					SignupUser sUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());

					if(signupUser ==null ){
						signupUser =sUser;
					}
					else if(sUser.isPublishedSite() && sUser.getMainSiteId().equals(getCurrentLocationId())){
						return sUser;
					}
					else if(!signupUser.isPublishedSite() && sUser.isPublishedSite())
							signupUser =sUser;
				}
				else
					log.info("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
			}
			
		}//end of for
		
		/*Case 2: site has '.auth' roleId and user is not required to be a site member*/
		if(signupUser ==null){
			signupUser = getSignupUserForLoginRequiredOnlySite(currentSignupSite,userId);
		}
		
		return signupUser;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<User> getUsersWithPermission(String permission) {
		
		try {
			//current site
			Site currentSite = siteService.getSite(getCurrentLocationId());
		
			//get userids with permission in this site/azg
			Set<String> usersWithPermission = authzGroupService.getUsersIsAllowed(permission, Collections.singletonList(currentSite.getReference()));
		
			//get Users
			return  userDirectoryService.getUsers(usersWithPermission);
		} catch (Exception e) {
			log.error("getUsersWithPermission exception: " + e.getClass() + ":" + e.getMessage());
		}
		return Collections.EMPTY_LIST;
	}

		
	private boolean hasPermissionToAttend(SignupSite site, String userId){
		if(isAllowedSite(userId, SIGNUP_ATTEND_ALL, site.getSiteId()))
			return true;
	
		if (site.isSiteScope()) {
			if (isAllowedSite(userId, SIGNUP_ATTEND, site.getSiteId()))
				return true;
		} else {
			List<SignupGroup> signupGroups = site.getSignupGroups();
			for (SignupGroup group : signupGroups) {
				if(isAllowedGroup(userId, SIGNUP_ATTEND, site.getSiteId(), group.getGroupId()))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean hasPermissionToCreate(SignupMeeting meeting, String userId){
		
		List<SignupSite> signupSites = meeting.getSignupSites();
		if(signupSites !=null){
			for (SignupSite site : signupSites) {			
				if(isAllowedSite(userId, SIGNUP_CREATE_SITE, site.getSiteId()))
					return true;
			
				if (site.isSiteScope()) {
					if (isAllowedSite(userId, SIGNUP_CREATE_SITE, site.getSiteId()))
						return true;
				} else {
					List<SignupGroup> signupGroups = site.getSignupGroups();
					for (SignupGroup group : signupGroups) {
						if(isAllowedGroup(userId, SIGNUP_CREATE_GROUP_ALL, site.getSiteId(), group.getGroupId()) || isAllowedGroup(userId, SIGNUP_CREATE_GROUP, site.getSiteId(), group.getGroupId()))
							return true;
					}
				}
			
			}
		}
		
		return false;
	}
	
	/*If site has roleId '.auth', any user is OK if logged in*/
	private SignupUser getSignupUserForLoginRequiredOnlySite(SignupSite signupSite, String userId){
		SignupUser signupUser=null;
		Site site=null;
		try {
			site = siteService.getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}

		if (site == null)
			return null;
		
		Set siteRoles = site.getRoles();
		if(siteRoles !=null){
			for (Iterator iter = siteRoles.iterator(); iter.hasNext();) {
				Role role = (Role) iter.next();
				if(REALM_ID_FOR_LOGIN_REQUIRED_ONLY.equals(role.getId())){
					if(hasPermissionToAttend(signupSite,userId)){
						User user = getUser(userId);
						if(user !=null)
							signupUser = new SignupUser(user.getEid(), userId, user.getFirstName(), user.getLastName(),
								role, site.getId(), site.isPublished());
						break;
					}
				}
				
			}
		}
		
		return signupUser;
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
				User user = getUserQuietly(member.getUserId());
				if (user == null) {
					log.debug("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
					/* will not add into the dropDown list
					signupUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());
					processAddOrUpdateSignupUsers(signupUsers, signupUser);
					*/
					continue;
				}

				signupUser = new SignupUser(member.getUserEid(), member.getUserId(), user.getFirstName(), user
						.getLastName(), member.getRole(), site.getId(), site.isPublished());
				processAddOrUpdateSignupUsers(signupUsers, signupUser);
				// comment: member.getUserDisplayId() not used
			}
		}
	}
	
	/* get all users in a specific group */
	@SuppressWarnings("unchecked")
	private void getAttendeesForGroup(Set<SignupUser> signupUsers, SignupSite signupSite, SignupGroup signupGroup) {
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
							|| isAllowedGroup(member.getUserId(), SIGNUP_ATTEND, site.getId(), group.getId()) || isAllowedSite(
							member.getUserId(), SIGNUP_ATTEND_ALL, site.getId()))) {
				User user = getUserQuietly(member.getUserId());
				if (user == null) {
					log.debug("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
					/* will not add into the dropDown list
					signupUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());
					processAddOrUpdateSignupUsers(signupUsers, signupUser);
					*/
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

		SignupUser signupUser = null;
		Set<Member> members = site.getMembers();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedSite(member.getUserId(), SIGNUP_VIEW, site.getId()) || isAllowedSite(member
							.getUserId(), SIGNUP_VIEW_ALL, site.getId()))) {
				User user = getUserQuietly(member.getUserId());
				if (user == null) {
					log.debug("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
					/* will not add into the dropDown list
					signupUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());
					processAddOrUpdateSignupUsers(signupUsers, signupUser);
					*/
					continue;
				}

				signupUser = new SignupUser(member.getUserEid(), member.getUserId(), user.getFirstName(), user
						.getLastName(), member.getRole(), site.getId(), site.isPublished());
				processAddOrUpdateSignupUsers(signupUsers, signupUser);

			}
		}
	}
	
	/* get all users in a site */
	@SuppressWarnings("unchecked")
	private void getAttendeesForSiteWithSiteScope(Set<SignupUser> signupUsers, SignupSite signupSite) {
		Site site = null;
		try {
			site = siteService.getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}

		if (site == null)
			return;

		SignupUser signupUser = null;
		Set<Member> members = site.getMembers();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedSite(member.getUserId(), SIGNUP_ATTEND, site.getId()) || isAllowedSite(member
							.getUserId(), SIGNUP_ATTEND_ALL, site.getId()) || isAllowedSite(member
									.getUserId(), SIGNUP_UPDATE_SITE, site.getId()))) {
				User user = getUserQuietly(member.getUserId());
				if (user == null) {
					log.debug("user is not found from 'userDirectoryService' for userId:" + member.getUserId());
					/* will not add into the dropDown list
					signupUser = new SignupUser(member.getUserEid(), member.getUserId(), "", member.getUserEid(),
							member.getRole(), site.getId(), site.isPublished());
					processAddOrUpdateSignupUsers(signupUsers, signupUser);
					*/
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
				if (sUser.getEid().equals(signupUser.getEid())) {
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
	
	private ContentHostingService contentHostingService;

	/**
	 * {@inheritDoc}
	 */
	public ContentHostingService getContentHostingService() {
		return contentHostingService;
	}

	/**
	 * This is a setter.
	 * @param contentHostingService
	 * 			a ContentHostingService object
	 */
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<User> getUsersByEmail(String email) {
		return (List<User>)userDirectoryService.findUsersByEmail(email);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public User getUserByEmail(String email) {
		
		List<User> users =  (List<User>)userDirectoryService.findUsersByEmail(email);
		
		if(users.isEmpty()) {
			return null;
		}
		
		return users.get(0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public User getUserByEid(String eid) {
		try {
			return userDirectoryService.getUserByEid(eid);
		} catch (UserNotDefinedException e) {
			log.debug("User with eid: " + eid + " does not exist.");
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isCsvExportEnabled() {
		return serverConfigurationService.getBoolean("signup.csv.export.enabled", false);
	}

	/**
	 * {@inheritDoc}
	 */
    public SecurityAdvisor pushAllowCalendarEdit() {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                if(CalendarService.AUTH_MODIFY_CALENDAR_ANY.equals(function)) {
                    return SecurityAdvice.ALLOWED;
                } else {
                    return SecurityAdvice.NOT_ALLOWED;
                }
            }
        };

        enableSecurityAdvisor(advisor);

        return advisor;
    }
    
    /**
	 * {@inheritDoc}
	 */
    public SecurityAdvisor pushSecurityAdvisor() {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };

        enableSecurityAdvisor(advisor);

        return advisor;
    }

	/**
	 * {@inheritDoc}
	 */
    public void popSecurityAdvisor(SecurityAdvisor advisor) {
        disableSecurityAdvisor(advisor);
    }
	
	/**
	 * {@inheritDoc}
	 */
	public String createGroup(String siteId, String title, String description, List<String> userUuids) {
				
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("createGroup failed for site: " + site.getId(), e);
            return null;
		}
							
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
				
		try {
			
			//add new group
			Group group=site.addGroup();
			
			group.setTitle(GROUP_PREFIX + title);
	        group.setDescription(description);   
	        
	        //set this property so the groups shows in site info
		    group.getProperties().addProperty(GROUP_PROP_SITEINFO_VISIBLE, Boolean.TRUE.toString());
		    
		    //set this so the group does not show in the list of groups in signups
		    //SIGNUP-182 allow groups to be normal groups, ie dont set this property
		    //group.getProperties().addProperty(GROUP_PROP_SIGNUP_IGNORE, Boolean.TRUE.toString());

		    
		    if(userUuids != null) {
		    	group.removeMembers();
		    			    	
		    	for(String userUuid: userUuids) {
		    		group = addUserToGroup(userUuid, group);
		    	}
		    }
	   		    
		    // save the changes
			siteService.save(site);
			
			return group.getId();
			
		} catch (Exception e) {
        	log.error("createGroup failed for site: " + site.getId(), e);
        } finally {
        	disableSecurityAdvisor(securityAdvisor);
        }
		
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addUsersToGroup(Collection<String> userIds, String siteId, String groupId) {
		
		log.debug("addUsersToGroup(userIds=" + Arrays.asList(userIds).toString() + ", siteId=" + siteId + ", groupId=" + groupId);
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("addUserToGroup failed to retrieve site: " + siteId, e);
            return false;
		}
							
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
		
		Group group = site.getGroup(groupId);
		
		if(group == null) {
        	log.error("No group for id: " + groupId);
			return false;
		}
		
		try {
			
			for(String userId: userIds) {
				group = addUserToGroup(userId, group);
			}
			siteService.save(site);
			
			return true;
			
		} catch (Exception e) {
        	log.error("addUsersToGroup failed for users: " + Arrays.asList(userIds).toString() + " and group: " + groupId, e);
        } finally {
        	disableSecurityAdvisor(securityAdvisor);
        }
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean synchonizeGroupTitle(String siteId, String groupId, String newTitle){
		Site site = null;
		boolean changed = false;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("synchronizeGroup failed to retrieve site: " + siteId, e);
            return false;
		}
							
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
		
		Group group = site.getGroup(groupId);
		
		if(group == null) {
        	log.error("No group for id: " + groupId);
			return false;
		}
		
		try {
			
			if(group.getTitle().startsWith(GROUP_PREFIX)){
				//it means that the group title has not been modified via Site-info, we can change it now				
				group.setTitle(GROUP_PREFIX + newTitle);
				siteService.save(site);
				changed = true;
			}
			else if(group.getTitle().contains(newTitle)){
				//it is already changed due to version-multiple-try-saving process
				//Don't do anything this time.
				changed = true;
			}
			
		} catch (Exception e) {
        	log.error("synchGroupTitle failed for group: " + group.getTitle() + " and group: " + groupId, e);
        } finally {
        	disableSecurityAdvisor(securityAdvisor);
        }
		return changed;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeUserFromGroup(String userId, String siteId, String groupId) {
		
		log.debug("removeUserFromGroup(userId=" + userId + ", siteId=" + siteId + ", groupId=" + groupId);
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("removeUserFromGroup failed to retrieve site: " + siteId, e);
            return false;
		}
							
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
		
		Group group = site.getGroup(groupId);
		
		try {
			group.removeMember(userId);
			siteService.save(site);
			
			return true;
			
		} catch (Exception e) {
        	log.error("removeUserFromGroup failed for user: " + userId + " and group: " + groupId, e);
        } finally {
        	disableSecurityAdvisor(securityAdvisor);
        }
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getGroupMembers(String siteId, String groupId) {
		
		List<String> users = new ArrayList<String>();
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("getGroupMembers failed to retrieve site: " + siteId, e);
            return users;
		}
							
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
		
		try {
			Group group = site.getGroup(groupId);
			Set<Member> members = group.getMembers();
		
			for(Member m: members) {
				users.add(m.getUserId());
				log.error("Added user: " + m.getUserId() + " to group: " + groupId);
			}
			return users;
		} catch (Exception e) {
        	log.error("getGroupMembers failed for site: " + siteId + " and group: " + groupId, e);
        } finally {
        	disableSecurityAdvisor(securityAdvisor);
        }
		
		return users;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkForGroup(String siteId, String groupId) {
		
		log.debug("checkForGroup: siteId=" + siteId + ", groupId=" + groupId);
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("checkForGroup failed to retrieve site: " + siteId, e);
            return false;
		}
		
		/*
		SecurityAdvisor securityAdvisor = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference){
				return SecurityAdvice.ALLOWED;
			}
		};
		enableSecurityAdvisor(securityAdvisor);
		*/
		
		Group group = site.getGroup(groupId);
		
		if(group != null) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Helper to add a user to a group. THIS DOES NOT SAVE ANYTHING. It is merely a helper to add the user to the group object and return it.
	 * 
	 * @param userUuid	uuid of user
	 * @param group		Group obj
	 * @return
	 */
	private Group addUserToGroup(String userUuid, Group group) {
		
		Site site = group.getContainingSite();
		
		//same logic as in site-manage
		Role r = site.getUserRole(userUuid);
		Member m = site.getMember(userUuid);
		Role memberRole = m != null ? m.getRole() : null;
		
		//Each user should be marked as non provided
		//Get role first from site definition. 
		//However, if the user is inactive, getUserRole would return null; then use member role instead
		group.addMember(userUuid, r != null ? r.getId() : memberRole != null? memberRole.getId() : "", m != null ? m.isActive() : true, false);
		
		return group;
	}
		
	/**
	 * Setup the security advisor for this transaction
	 */
	private void enableSecurityAdvisor(SecurityAdvisor securityAdvisor) {
		securityService.pushAdvisor(securityAdvisor);
	}

	/**
	 * Remove security advisor from the stack
	 */
	private void disableSecurityAdvisor(SecurityAdvisor securityAdvisor){
		securityService.popAdvisor(securityAdvisor);
	}

}
