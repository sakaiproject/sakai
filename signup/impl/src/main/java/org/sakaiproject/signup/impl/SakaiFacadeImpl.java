/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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

package org.sakaiproject.signup.impl;

import static org.sakaiproject.signup.api.SignupConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.sakaiproject.signup.api.SignupUser;
import org.sakaiproject.signup.api.model.SignupGroup;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupSite;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.UserSortNameComparator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@Getter
@Setter
public class SakaiFacadeImpl implements SakaiFacade {

	private FunctionManager functionManager;
	private ToolManager toolManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;
	private CalendarService calendarService;
	private ServerConfigurationService serverConfigurationService;
	private AuthzGroupService authzGroupService;
	private FormattedText formattedText;
	private TimeService timeService;
	private ContentHostingService contentHostingService;
    private Optional<CalendarService> additionalCalendarService;
    private SignupMeetingService signupMeetingService;

	// Returns Google calendar if the calendar has been created in Google
	public Calendar getAdditionalCalendar(String siteId) throws PermissionException {
        if (additionalCalendarService.isPresent()) {
            CalendarService calService = additionalCalendarService.get();
			if (calService.isCalendarToolInitialized(siteId)) {
				try {
					return calService.getCalendar(siteId);
				} catch (IdUnusedException e) {
					log.warn("Could not retrieve additional calendar for site [{}], {}", siteId, e.toString());
				}
			}
		}
		return null;
	}			
	
	/**
	 * regist all the permission levels, which Signup Tool required. Place any
	 * code that should run when this class is initialized by spring here
	 */
	public void init() {
		log.debug("init");
	}

	@Override
	public boolean isUserAdmin(String userId) {
		return securityService.isSuperUser(userId);
	}

	@Override
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	@Override
	public User getUser(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get user for id: {}", userId);
			return null;
		}
	}
	
	@Override
	public User getUserQuietly(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.debug("getUserQuietly with id: {} does not exist : {}", userId, e.toString());
			return null;
		}
	}
	
	@Override
	public boolean checkForUser(String userId) {
		try {
			User u = userDirectoryService.getUser(userId);
			if (u != null) {
				return true;
			} 
		} catch (UserNotDefinedException e) {
			log.debug("User with id: {} does not exist : {}", userId, e.toString());
		}
		return false;
	}

	@Override
	public String getUserDisplayName(String userId) {
		try {
			return userDirectoryService.getUser(userId).getDisplayName();
		} catch (UserNotDefinedException e) {
			log.warn("Cannot get user displayname for id: " + userId);
			return "--------";
		}
	}
	
	@Override
	public String getUserDisplayLastFirstName(String userId) {
		try {
			final String dispLastName = userDirectoryService.getUser(userId).getLastName();
			final String dispFirstName = userDirectoryService.getUser(userId).getFirstName();
			if(StringUtils.isEmpty(dispLastName) && StringUtils.isEmpty(dispFirstName)){
				//Case: local user can have no first and last names
				return userDirectoryService.getUser(userId).getDisplayId();
			}
			else{
				return dispLastName + ", " + dispFirstName;
			}

		} catch (UserNotDefinedException e) {
			log.warn("Cannot get user displayname for id: " + userId);
			return "--------";
		}
	}

	@Override
	public String getCurrentLocationId() {
		try {
			return toolManager.getCurrentPlacement().getContext();
		} catch (Exception e) {
			log.info("Failed to get current location id");
			return NO_LOCATION;
		}
	}

	@Override
	public String getCurrentPageId() {
		return getSiteSignupPageId(getCurrentLocationId());
	}

	@Override
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

	@Override
	public String getLocationTitle(String locationId) {
		try {
			Site site = siteService.getSite(locationId);
			return site.getTitle();
		} catch (IdUnusedException e) {
			log.warn("Cannot get the info about locationId: " + locationId);
			return "----------";
		}
	}
	
	@Override
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

	@Override
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

	@Override
	public boolean isAllowedGroup(String userId, String permission, String siteId, String groupId) {
		return isAllowed(userId, permission, siteService.siteGroupReference(siteId, groupId));
	}

	@Override
	public boolean isAllowedSite(String userId, String permission, String siteId) {
		return isAllowed(userId, permission, siteService.siteReference(siteId));
	}

	/* check permission */
	private boolean isAllowed(String userId, String permission, String realmId) {
        return securityService.unlock(userId, permission, realmId);
    }

	@Override
	public String getUserId(String eid) throws UserNotDefinedException {
		return userDirectoryService.getUserId(eid);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SignupUser> getAllUsers(SignupMeeting meeting) {
		List<SignupSite> signupSites = meeting.getSignupSites();
		List<SignupUser> signupUsers = new ArrayList<>();
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
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SignupUser> getAllPossibleAttendees(SignupMeeting meeting){
		List<SignupSite> signupSites = meeting.getSignupSites();
		ArrayList<SignupUser> signupUsers = new ArrayList<>();
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
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SignupUser> getAllPossibleCoordinators(SignupMeeting meeting) {
		List<SignupUser> coordinators = new ArrayList<>();
		List<SignupUser> signUpUsers = getAllUsers(meeting);
		List<SignupSite> signupSites = meeting.getSignupSites();
		Set<String> userIdsHasPermissionToCreate = new HashSet<String>();
		if (signupSites != null) {
			for (SignupSite site: signupSites) {
				userIdsHasPermissionToCreate.addAll(getUserIdsHasPermissionToCreate(site));
			}
		}
		for (SignupUser signUpUser: signUpUsers) {
			if (userIdsHasPermissionToCreate.contains(signUpUser.getInternalUserId())) {
				coordinators.add(signUpUser);
			}
		}
		return coordinators;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SignupUser> getAllPossbileCoordinatorsOnFastTrack(SignupMeeting meeting) {
		List<SignupUser> coordinators = new ArrayList<>();
		List<SignupSite> signupSites = meeting.getSignupSites();
		Set<String> userIdsHasPermissionToCreate = new HashSet<String>();
		if (signupSites != null) {
			for (SignupSite site: signupSites) {
				//Only thing we miss here is that the view permission is not checked. the Chance is very small.
				userIdsHasPermissionToCreate.addAll(getUserIdsHasPermissionToCreate(site));
			}
		}
		
		List<User> sakaiUsers = userDirectoryService.getUsers(userIdsHasPermissionToCreate);
		Collections.sort(sakaiUsers, new UserSortNameComparator());
		for (User user : sakaiUsers) {
			SignupUser signupUser = new SignupUser(user.getEid(), user.getId(), user.getFirstName(), user.getLastName(), 
					null, "", true);
	 				coordinators.add(signupUser);	
	 	}

		return coordinators;
	}
	
	private Set<String> getUserIdsHasPermissionToCreate(SignupSite site) {
		Set<String> userIds = new HashSet<String>();
		userIds.addAll(getUserIdsWithPermission(SIGNUP_CREATE_SITE, site.getSiteId()));
		if (!site.isSiteScope()) {
			List<SignupGroup> signupGroups = site.getSignupGroups();
			for (SignupGroup group: signupGroups) {
				userIds.addAll(getUserIdsWithPermission(SIGNUP_CREATE_GROUP_ALL, site.getSiteId(), group.getGroupId()));
				userIds.addAll(getUserIdsWithPermission(SIGNUP_CREATE_GROUP, site.getSiteId(), group.getGroupId()));
			}
		}
		return userIds;
	}
	
	private List<String> getUserIdsWithPermission(String permission, String siteId) {
		return getUserIdsWithPermissionOnRealm(permission, siteService.siteReference(siteId));
	}
	
	private List<String> getUserIdsWithPermission(String permission, String siteId, String groupId) {
		return getUserIdsWithPermissionOnRealm(permission, siteService.siteGroupReference(siteId, groupId));
	}
	
	private List<String> getUserIdsWithPermissionOnRealm(String permission, String realmId) {
		List<String> rv = new ArrayList<String>();
		for (User user: securityService.unlockUsers(permission, realmId)) {
			rv.add(user.getId());
		}
		return rv;
	}
	
	
	@Override
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
	
	@Override
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
	private void getUsersForGroup(List<SignupUser> signupUsers, SignupSite signupSite, SignupGroup signupGroup) {
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

		//SIGNUP-241 : get bulk SakaiUsers for performance
		List<String> userIds = new ArrayList<String>();
		Map<String,Role> memberRoleMap = new Hashtable<String, Role>();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedGroup(member.getUserId(), SIGNUP_VIEW, site.getId(), group.getId()) || isAllowedSite(
							member.getUserId(), SIGNUP_VIEW_ALL, site.getId()))) {
				//User user = getUserQuietly(member.getUserId());
				memberRoleMap.put(member.getUserId(), member.getRole());
				userIds.add(member.getUserId());
			}
		}
		
		addAndPopulateSignupUsersInfo(signupUsers,memberRoleMap,userIds, site);
	}
	
	private void addAndPopulateSignupUsersInfo(List<SignupUser> signupUsers, Map<String,Role> memberRoleMap, List<String> userIds, Site site){
		//it should filter out non-existing userIds
		List<User> sakaiUsers = userDirectoryService.getUsers(userIds);
		Collections.sort(sakaiUsers, new UserSortNameComparator());
		
		if(sakaiUsers !=null){
			for (User user : sakaiUsers) {
				SignupUser signupUser = new SignupUser(user.getEid(), user.getId(), user.getFirstName(), user.getLastName(), 
						memberRoleMap.get(user.getId()), site.getId(), site.isPublished());
				processAddOrUpdateSignupUsers(signupUsers, signupUser);
				// comment: member.getUserDisplayId() not used			
			}
		}
		
	}
	
	/* get all users in a specific group */
	@SuppressWarnings("unchecked")
	private void getAttendeesForGroup(List<SignupUser> signupUsers, SignupSite signupSite, SignupGroup signupGroup) {
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

		//SIGNUP-241 : get bulk SakaiUsers for performance
		List<String> userIds = new ArrayList<String>();
		Map<String,Role> memberRoleMap = new Hashtable<String, Role>();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedGroup(member.getUserId(), SIGNUP_ATTEND, site.getId(), group.getId()) || isAllowedSite(
							member.getUserId(), SIGNUP_ATTEND_ALL, site.getId()))) {
				memberRoleMap.put(member.getUserId(), member.getRole());
				userIds.add(member.getUserId());
			}
		}
		
		addAndPopulateSignupUsersInfo(signupUsers,memberRoleMap,userIds, site);
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
	private void getUsersForSiteWithSiteScope(List<SignupUser> signupUsers, SignupSite signupSite) {
		Site site = null;
		try {
			site = siteService.getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}

		if (site == null)
			return;

		//SIGNUP-241 : get bulk SakaiUsers for performance
		List<String> userIds = new ArrayList<>();
		Map<String,Role> memberRoleMap = new Hashtable<>();
		Set<Member> members = site.getMembers();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedSite(member.getUserId(), SIGNUP_VIEW, site.getId()) || isAllowedSite(member
							.getUserId(), SIGNUP_VIEW_ALL, site.getId()))) {
				memberRoleMap.put(member.getUserId(), member.getRole());
				userIds.add(member.getUserId());
			}
		}
		
		addAndPopulateSignupUsersInfo(signupUsers,memberRoleMap,userIds, site);

	}
	
	/* get all users in a site */
	@SuppressWarnings("unchecked")
	private void getAttendeesForSiteWithSiteScope(List<SignupUser> signupUsers, SignupSite signupSite) {
		Site site = null;
		try {
			site = siteService.getSite(signupSite.getSiteId());
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}

		if (site == null)
			return;

		Set<Member> members = site.getMembers();
		//SIGNUP-241 : get bulk SakaiUsers for performance
		List<String> userIds = new ArrayList<>();
		Map<String,Role> memberRoleMap = new Hashtable<>();
		for (Member member : members) {
			if (member.isActive()
					&& (hasPredefinedViewPermisson(member)
							|| isAllowedSite(member.getUserId(), SIGNUP_ATTEND, site.getId()) || isAllowedSite(member
							.getUserId(), SIGNUP_ATTEND_ALL, site.getId()) || isAllowedSite(member
									.getUserId(), SIGNUP_UPDATE_SITE, site.getId()))) {
				memberRoleMap.put(member.getUserId(), member.getRole());
				userIds.add(member.getUserId());
			}
		}
		
		addAndPopulateSignupUsersInfo(signupUsers,memberRoleMap,userIds, site);
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
	private void processAddOrUpdateSignupUsers(List<SignupUser> signupUsers, SignupUser signupUser) {
		boolean update = true;
		if (!signupUsers.isEmpty() && signupUsers.contains(signupUser)) {
			for (SignupUser sUser : signupUsers) {
				if (sUser.getEid().equals(signupUser.getEid())) {
					update = !sUser.isPublishedSite() && signupUser.isPublishedSite() || signupUser.isPublishedSite()
							&& signupUser.getMainSiteId().equals(getCurrentLocationId());

					break;
				}
			}
		}

		if (update && !signupUsers.contains(signupUser)) {
			signupUsers.add(signupUser);
		}
	}

	@Override
	public Calendar getCalendar(String siteId) throws PermissionException {
		String calendarId = calendarService.calendarReference(siteId, SiteService.MAIN_CONTAINER);
		return getCalendarById(calendarId);
	}

	@Override
	public Calendar getCalendarById(String calendarId) throws PermissionException {
		Calendar calendar = null;
		try {
			calendar = calendarService.getCalendar(calendarId);
		} catch (IdUnusedException e) {
			try {
				CalendarEdit calendarEdit = calendarService.addCalendar(calendarId);
				calendarService.commitCalendar(calendarEdit);
				calendar = getCalendarById(calendarId);
			} catch (IdInvalidException highlyUnlikely) {
				log.error(highlyUnlikely.getMessage());
			} catch (IdUsedException extremelyUnlikely) {
				log.error(extremelyUnlikely.getMessage());
			}
		}
		return calendar;
	}

	@Override
	public Group getGroup(String siteId, String groupId) throws IdUnusedException {
		Site site = siteService.getSite(siteId);
		return site.getGroup(groupId);
	}


	@Override
	public Collection<User> getUsersByEmail(String email) {
		return userDirectoryService.findUsersByEmail(email);
	}
	
	@Override
	public User getUserByEmail(String email) {
		
		Collection<User> users = userDirectoryService.findUsersByEmail(email);
		
		if(users.isEmpty()) {
			return null;
		}
		
		return users.iterator().next();
	}
	
	@Override
	public User getUserByEid(String eid) {
		try {
			return userDirectoryService.getUserByEid(eid);
		} catch (UserNotDefinedException e) {
			log.debug("User with eid: {} does not exist.", eid);
		}
		return null;
	}
	
	@Override
	public boolean isCsvExportEnabled() {
		return serverConfigurationService.getBoolean("signup.csv.export.enabled", false);
	}

	@Override
	public SecurityAdvisor pushAllowCalendarEdit(final Calendar calendar) {
		SecurityAdvisor advisor = (userId, function, reference) -> {
			if(calendar != null && calendar.canModifyAnyEvent(function)) {
				return SecurityAdvisor.SecurityAdvice.ALLOWED;
			} else {
				return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
			}
		};

		enableSecurityAdvisor(advisor);

		return advisor;
	}
	
	@Override
	public SecurityAdvisor pushSecurityAdvisor() {
		SecurityAdvisor advisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;

		enableSecurityAdvisor(advisor);

		return advisor;
	}

	@Override
	public void popSecurityAdvisor(SecurityAdvisor advisor) {
		disableSecurityAdvisor(advisor);
	}
	
	@Override
	public String createGroup(String siteId, String title, String description, List<String> userUuids) {
				
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("createGroup failed for site: {}", site.getId(), e);
			return null;
		}
							
		SecurityAdvisor securityAdvisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
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
		    	try {
		    		group.deleteMembers();
		    	} catch (IllegalStateException e) {
		    		log.error(".createGroup: Members from group with id {} cannot be deleted because the group is locked", group.getId());
		    	}
		    			    	
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
	
	@Override
	public boolean addUsersToGroup(Collection<String> userIds, String siteId, String groupId, String timeslottoGroup) {

		log.debug("addUsersToGroup(userIds={}, siteId={}, groupId={}", Collections.singletonList(userIds), siteId, groupId);
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("addUserToGroup failed to retrieve site: {}", siteId, e);
			return false;
		}
							
		SecurityAdvisor securityAdvisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
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
				
				//the synchronise is from the time slot to Site group membership
				if(timeslottoGroup.equals("toGroup")) {
					
					List<String> updateusers = getGroupMembers(siteId, groupId);

					//first clone a new group member
					Set<String> tmpUsers = new HashSet<>(updateusers);

					//retrieve only the difference members from TimeSlot and SiteGroup
					tmpUsers.removeAll(userIds);
				
					//remove the differences from group members
					for (String mem: tmpUsers){
						try {
							group.deleteMember(mem);
						} catch (IllegalStateException e) {
							log.error(".addUsersToGroup: User with id {} cannot be deleted from group with id {} because the group is locked", mem, group.getId());
							return false;
						}
					}
				}

				siteService.save(site);
			
				return true;
			
			} catch (Exception e) {
				log.error("addUsersToGroup failed for users: {} and group: {}", Collections.singletonList(userIds), groupId, e);
			} finally {
				disableSecurityAdvisor(securityAdvisor);
			}
		return false;
	}
	
	@Override
	public boolean synchonizeGroupTitle(String siteId, String groupId, String newTitle){
		Site site = null;
		boolean changed = false;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("synchronizeGroup failed to retrieve site: {}", siteId, e);
			return false;
		}

		SecurityAdvisor securityAdvisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
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
	
	
	@Override
	public boolean removeUserFromGroup(String userId, String siteId, String groupId) {

		log.debug("removeUserFromGroup(userId={}, siteId={}, groupId={}", userId, siteId, groupId);
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("removeUserFromGroup failed to retrieve site: {}", siteId, e);
			return false;
		}
							
		SecurityAdvisor securityAdvisor = (userId1, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
		enableSecurityAdvisor(securityAdvisor);
		
		Group group = site.getGroup(groupId);
		
		try {
			group.deleteMember(userId);
			siteService.save(site);
			
			return true;
			
		} catch (IllegalStateException e) {
			log.error(".removeUserFromGroup: User with id {} cannot be deleted from group with id {} because the group is locked", userId, group.getId());
		} catch (Exception e) {
			log.error("removeUserFromGroup failed for user: " + userId + " and group: " + groupId, e);
		} finally {
			disableSecurityAdvisor(securityAdvisor);
		}
		
		return false;
	}
	
	@Override
	public List<String> getGroupMembers(String siteId, String groupId) {
		
		List<String> users = new ArrayList<>();
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("getGroupMembers failed to retrieve site: {}", siteId, e);
			return users;
		}
							
		SecurityAdvisor securityAdvisor = (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
		enableSecurityAdvisor(securityAdvisor);
		
		try {
			Group group = site.getGroup(groupId);
			Set<Member> members = group.getMembers();
		
			for(Member m: members) {
				users.add(m.getUserId());
				log.warn("Added user: {} to group: {}", m.getUserId(), groupId);
			}
			return users;
		} catch (Exception e) {
			log.error("getGroupMembers failed for site: {} and group: {}", siteId, groupId, e);
		} finally {
			disableSecurityAdvisor(securityAdvisor);
		}
		
		return users;
	}
	
	@Override
	public boolean checkForGroup(String siteId, String groupId) {

		log.debug("checkForGroup: siteId={}, groupId={}", siteId, groupId);
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (Exception e) {
			log.error("checkForGroup failed to retrieve site: {}", siteId, e);
			return false;
		}
		
		Group group = site.getGroup(groupId);
		
		if(group != null) {
			return true;
		}
		return false;
	}

	@Override
	public String getToolResetUrl() {

		Placement placement = toolManager.getCurrentPlacement();
		return serverConfigurationService.getPortalUrl() + "/site/" + placement.getContext() + "/tool-reset/" + placement.getId();
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
		try {
			group.insertMember(userUuid, r != null ? r.getId() : memberRole != null? memberRole.getId() : "", m != null ? m.isActive() : true, false);
		} catch (AuthzRealmLockException arle) {
			log.warn("GROUP LOCK REGRESSION: {}", arle.getMessage(), arle);
		}
		
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
