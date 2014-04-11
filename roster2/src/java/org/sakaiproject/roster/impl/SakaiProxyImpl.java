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

package org.sakaiproject.roster.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.roster.api.RosterEnrollment;
import org.sakaiproject.roster.api.RosterFunctions;
import org.sakaiproject.roster.api.RosterGroup;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.RosterSite;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * <code>SakaiProxy</code> acts as a proxy between Roster and Sakai components.
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Log log = LogFactory.getLog(SakaiProxyImpl.class);
		
	private CourseManagementService courseManagementService;
	private FunctionManager functionManager = null;
	private PrivacyManager privacyManager = null;
	private ProfileConnectionsLogic connectionsLogic;
	private SecurityService securityService = null;
	private ServerConfigurationService serverConfigurationService = null;
	private SessionManager sessionManager = null;
	private SiteService siteService;
	private ToolManager toolManager = null;
	private UserDirectoryService userDirectoryService = null;
	
	private static SakaiProxyImpl instance = null;
	
	/**
	 * Returns an instance of <code>SakaiProxyImpl</code>.
	 * 
	 * @return an instance of <code>SakaiProxyImpl</code>.
	 */
	public static SakaiProxyImpl instance() {
		
		if (null == instance) {
			instance = new SakaiProxyImpl();
		}
		return instance;
	}
	
	/**
	 * Creates a new instance of <code>SakaiProxyImpl</code>
	 */
	private SakaiProxyImpl() {

		org.sakaiproject.component.api.ComponentManager componentManager = 
			org.sakaiproject.component.cover.ComponentManager.getInstance();

		boolean connectionsApi;
		try {
			// check for Profile2 1.4.x connections API
			Class.forName("org.sakaiproject.profile2.logic.ProfileConnectionsLogic");
			
			connectionsApi = true;
			
		} catch (ClassNotFoundException e) {
			connectionsApi = false;
		}
		
		log.info("Profile2 >=1.4.x connections API found: " + connectionsApi);
		
		if (true == connectionsApi) {
			connectionsLogic = (ProfileConnectionsLogic) componentManager.get(ProfileConnectionsLogic.class);
		}
		
		courseManagementService = (CourseManagementService) componentManager.get(CourseManagementService.class);
		functionManager = (FunctionManager) componentManager.get(FunctionManager.class);
		privacyManager = (PrivacyManager) componentManager.get(PrivacyManager.class);
		securityService = (SecurityService) componentManager.get(SecurityService.class);
		serverConfigurationService = (ServerConfigurationService) componentManager.get(ServerConfigurationService.class);
		sessionManager = (SessionManager) componentManager.get(SessionManager.class);
		siteService = (SiteService) componentManager.get(SiteService.class);
		toolManager = (ToolManager) componentManager.get(ToolManager.class);
		userDirectoryService = (UserDirectoryService) componentManager.get(UserDirectoryService.class);
		
		init();
		
		log.info("org.sakaiproject.roster.api.SakaiProxy initialized");
	}	
	
	private void init() {
		
		log.info("org.sakaiproject.roster.api.SakaiProxy init()");
		
		List<String> registered = functionManager.getRegisteredFunctions();
		
        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_EXPORT)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_EXPORT, true);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWALL)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWALL, true);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, true);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWGROUP)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, true);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS, true);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWPROFILE)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWPROFILE, true);
        }
        
        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWEMAIL)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWEMAIL, true);
        }
        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALPHOTO)) {
            functionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALPHOTO, true);
        }
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	private Site getSite(String siteId) {

		try {
			return siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			log.warn("site not found: " + e.getId());
			return null;
		}
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
	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Integer getDefaultRosterState() {

		return serverConfigurationService.getInt("roster.defaultState",
				DEFAULT_ROSTER_STATE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDefaultRosterStateString() {
		
		Integer defaultRosterState = getDefaultRosterState();
		
		if (defaultRosterState > -1 && defaultRosterState < ROSTER_STATES.length - 1) {
			return ROSTER_STATES[defaultRosterState];
		} else {
			return ROSTER_STATES[DEFAULT_ROSTER_STATE];
		}
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
	public Boolean getViewEmail() {
		return getViewEmail(getCurrentSiteId());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getViewEmail(String siteId) {

		//To view emails it first needs to be enabled in sakai.properties and the user must have the permission.
		if(serverConfigurationService.getBoolean("roster_view_email",DEFAULT_VIEW_EMAIL)) {
			return hasUserSitePermission(getCurrentUserId(), RosterFunctions.ROSTER_FUNCTION_VIEWEMAIL, siteId);
		}
		return false;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getViewUserDisplayId() {
		return serverConfigurationService.getBoolean(
				"roster.display.userDisplayId", DEFAULT_VIEW_USER_DISPLAY_ID);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getSiteMembership(String siteId, boolean includeConnectionStatus) {
		return getMembership(siteId, null, includeConnectionStatus);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getGroupMembership(String siteId, String groupId) {
		return getMembership(siteId, groupId, false);
	}
	
	private List<RosterMember> getMembership(String siteId, String groupId,
			boolean includeConnectionStatus) {

        String userId = getCurrentUserId();

		List<RosterMember> rosterMembers = new ArrayList<RosterMember>();

		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			log.warn("site not found: " + e.getId());
		}

		if (null == site) {
			return null;
		}

		// permissions are handled inside this method call
		Set<Member> membership = getFilteredMembers(groupId, userId, site);
		if (null == membership) {
			return null;
		}

		for (Member member : membership) {

			try {

				RosterMember rosterMember = 
					getRosterMember(member, site, includeConnectionStatus, userId);

				rosterMembers.add(rosterMember);

			} catch (UserNotDefinedException e) {
				log.warn("user not found: " + e.getId());
			}
		}

		if (rosterMembers.size() == 0) {
			return null;
		}

		return rosterMembers;

	}
		
	private Map<String, RosterMember> getMembershipMapped(String siteId,
			String groupId, boolean filtered) {

		Map<String, RosterMember> rosterMembers = new HashMap<String, RosterMember>();

		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			log.warn("site not found: " + e.getId());
		}

		if (null == site) {
			return null;
		}

        String userId = getCurrentUserId();

		// permissions are handled inside this method call
		Set<Member> membership = null;
		if (true == filtered) {
			membership = getFilteredMembers(groupId, userId, site);
		} else {
			membership = getUnfilteredMembers(groupId, userId, site);
		}
		
		if (null == membership) {
			return null;
		}

		for (Member member : membership) {

			try {

				RosterMember rosterMember = getRosterMember(member, site, false, userId);

				rosterMembers.put(rosterMember.getEid(), rosterMember);

			} catch (UserNotDefinedException e) {
				log.warn("user not found: " + e.getId());
			}
		}

		return rosterMembers;
	}

	private Set<Member> getFilteredMembers(String groupId,
			String currentUserId, Site site) {

		Set<Member> membership = new HashSet<Member>();

		if (isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWALL, site.getReference())) {

			if (null == groupId) {
				// get all members
				membership.addAll(filterHiddenMembers(site.getMembers(),
						currentUserId, site.getId(), site));
			} else if (null != site.getGroup(groupId)) {
				// get all members of requested groupId
				membership.addAll(filterHiddenMembers(site.getGroup(groupId)
						.getMembers(), currentUserId, site.getId(), site
						.getGroup(groupId)));
			} else {
				// assume invalid groupId specified
				return null;
			}

		} else {
			if (null == groupId) {
				// get all members of groups current user is allowed to view
				for (Group group : site.getGroups()) {

					if (isAllowed(currentUserId,
							RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, group.getReference())) {

						membership.addAll(filterHiddenMembers(group
								.getMembers(), currentUserId, site.getId(),
								group));
					}
				}
			} else if (null != site.getGroup(groupId)) {
				// get all members of requested groupId if current user is
				// member
				if (isAllowed(currentUserId,
						RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, site
								.getGroup(groupId).getReference())) {

					membership.addAll(filterHiddenMembers(site
							.getGroup(groupId).getMembers(), currentUserId,
							site.getId(), site.getGroup(groupId)));
				}
			} else {
				// assume invalid groupId specified or user not member
				return null;
			}
		}
		
		if(log.isDebugEnabled()) log.debug("membership.size(): " + membership.size());
		
		//remove duplicates. Yes, its a Set but there can be dupes because its storing objects and from multiple groups.
		Set<String> check = new HashSet<String>();
		Set<Member> cleanedMembers = new HashSet<Member>();
		for (Member m : membership) {
			if(check.add(m.getUserId())) {
				cleanedMembers.add(m);
			}
		}
		
		if(log.isDebugEnabled()) log.debug("cleanedMembers.size(): " + cleanedMembers.size());

		return cleanedMembers;
	}
	
	@SuppressWarnings("unchecked")
	private Set<Member> filterHiddenMembers(Set<Member> membership,
			String currentUserId, String siteId, AuthzGroup authzGroup) {

		if(log.isDebugEnabled()) log.debug("filterHiddenMembers");
		
		if (isAllowed(currentUserId,
				RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, authzGroup.getReference())) {

			if(log.isDebugEnabled()) log.debug("permission to view all, including hidden");

			return membership;
		}

		Set<Member> filteredMembership = new HashSet<Member>();
		
		Set<String> userIds = new HashSet<String>();
		for (Member member : membership) {
			userIds.add(member.getUserEid());
		}

		Set<String> hiddenUserIds = privacyManager.findHidden(
				"/site/" + siteId, userIds);

		//get the list of visible roles, optional config.
		//if set, the only users visible in the tool will be those with their role defined in this list
		String[] visibleRoles = serverConfigurationService.getStrings("roster2.visibleroles");
		
		boolean filterRoles = ArrayUtils.isNotEmpty(visibleRoles);

		if(log.isDebugEnabled()) log.debug("visibleRoles: " + ArrayUtils.toString(visibleRoles));
		if(log.isDebugEnabled()) log.debug("filterRoles: " + filterRoles);
		
		// determine filtered membership
		for (Member member : membership) {
			
			// skip if privacy restricted
			if (hiddenUserIds.contains(member.getUserEid())) {
				continue;
			}
			
			// now filter out users based on their role
			if(filterRoles) {
				String memberRoleId = member.getRole().getId();
				if(ArrayUtils.contains(visibleRoles, memberRoleId)){
					filteredMembership.add(member);
					if(log.isDebugEnabled()) log.debug("Filter added: " + member.getUserEid());
				}
			} else {
				if(log.isDebugEnabled()) log.debug("Added: " + member.getUserEid());
				filteredMembership.add(member);
			}
			
		}
		
		if(log.isDebugEnabled()) log.debug("filteredMembership.size(): " + filteredMembership.size());
		
		return filteredMembership;
	}
	
	private Set<Member> getUnfilteredMembers(String groupId,
			String currentUserId, Site site) {
		
		Set<Member> membership = new HashSet<Member>();

		if (null == groupId) {
			// get all members
			membership.addAll(site.getMembers());
		} else if (null != site.getGroup(groupId)) {
			// get all members of requested groupId
			membership.addAll(site.getGroup(groupId)
						.getMembers());
		} else {
			// assume invalid groupId specified
			return null;
		}

		return membership;
	}
	
	private RosterMember getRosterMember(Member member, Site site,
			boolean includeConnectionStatus, String currentUserId) throws UserNotDefinedException {

		String userId = member.getUserId();

		User user = userDirectoryService.getUser(userId);

		RosterMember rosterMember = new RosterMember(userId);
		rosterMember.setEid(user.getEid());
		rosterMember.setDisplayId(member.getUserDisplayId());
		rosterMember.setRole(member.getRole().getId());

		rosterMember.setEmail(user.getEmail());
		rosterMember.setDisplayName(user.getDisplayName());
		rosterMember.setSortName(user.getSortName());

		Collection<Group> groups = site.getGroupsWithMember(userId);
		Iterator<Group> groupIterator = groups.iterator();

		while (groupIterator.hasNext()) {

			Group group = groupIterator.next();
			rosterMember.addGroup(group.getId(), group.getTitle());
		}

        if (true == includeConnectionStatus && connectionsLogic != null) {
            rosterMember.setConnectionStatus(connectionsLogic
                    .getConnectionStatus(currentUserId, userId));
        }

		return rosterMember;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getEnrollmentMembership(String siteId,
			String enrollmentSetId) {

		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			log.warn("site not found: " + e.getId());
		}

		if (null == site) {
			return null;
		}

		if (!isAllowed(getCurrentUserId(),
				RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS, site.getReference())) {

			return null;
		}

		EnrollmentSet enrollmentSet = courseManagementService
				.getEnrollmentSet(enrollmentSetId);

		if (null == enrollmentSet) {
			return null;
		}

		Map<String, String> statusCodes = courseManagementService
				.getEnrollmentStatusDescriptions(new ResourceLoader()
						.getLocale());

		Map<String, RosterMember> membership = getMembershipMapped(siteId,
				null, false);

		List<RosterMember> enrolledMembers = new ArrayList<RosterMember>();

		for (Enrollment enrollment : courseManagementService
				.getEnrollments(enrollmentSet.getEid())) {

			RosterMember member = membership.get(enrollment.getUserId());
			member.setCredits(enrollment.getCredits());
			member.setEnrollmentStatus(statusCodes.get(enrollment.getEnrollmentStatus()));

			enrolledMembers.add(member);
		}

		if (0 == enrolledMembers.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			return null;
		}
		return enrolledMembers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RosterSite getRosterSite(String siteId) {

		String currentUserId = getCurrentUserId();
		if (null == currentUserId) {
			if(log.isDebugEnabled()) log.debug("No currentUserId. Returning null");
			return null;
		}
		
		if(log.isDebugEnabled()) log.debug("currentUserId: " + currentUserId);


		Site site = getSite(siteId);
		if (null == site) {
			if(log.isDebugEnabled()) log.debug("No site. Returning null");
			return null;
		}
		
		if(log.isDebugEnabled()) log.debug("site: " + site.getId());
		
		if(log.isDebugEnabled()) log.debug("Checking permissions for site: " +  site.getReference());
		// for DelegatedAccess to work, you must use the SecurityService.unlock method of checking permissions.
		if(!isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWALL, site.getReference())) {
			if(log.isDebugEnabled()) log.debug("roster.viewallmembers = false");

			if(!isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, site.getReference())) {
				if(log.isDebugEnabled()) log.debug("roster.viewgroup = false");
				return null;
			} else {
				if(log.isDebugEnabled()) log.debug("roster.viewgroup = true. Access ok.");
			}
		} else {
			if(log.isDebugEnabled()) log.debug("roster.viewallmembers = true. Access ok.");
		}

		
		RosterSite rosterSite = new RosterSite(site.getId());

		rosterSite.setTitle(site.getTitle());

		List<RosterGroup> siteGroups = getViewableSiteGroups(currentUserId,
				site);

		if (0 == siteGroups.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			rosterSite.setSiteGroups(null);
		} else {
			rosterSite.setSiteGroups(siteGroups);
		}

		List<String> userRoles = new ArrayList<String>();
		for (Role role : site.getRoles()) {
			userRoles.add(role.getId());
		}

		if (0 == userRoles.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			rosterSite.setUserRoles(null);
		} else {
			rosterSite.setUserRoles(userRoles);
		}

		GroupProvider groupProvider = (GroupProvider) ComponentManager
				.get(GroupProvider.class);

		Map<String, String> statusCodes = courseManagementService
				.getEnrollmentStatusDescriptions(new ResourceLoader()
						.getLocale());

		rosterSite.setEnrollmentStatusDescriptions(new ArrayList<String>(
				statusCodes.values()));

		if (null == groupProvider) {
			log.warn("no group provider installed");
			// to avoid IndexOutOfBoundsException in EB code
			rosterSite.setSiteEnrollmentSets(null);
			return rosterSite;

		}
		
		List<RosterEnrollment> siteEnrollmentSets = getEnrollmentSets(siteId,
				groupProvider);

		if (0 == siteEnrollmentSets.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			rosterSite.setSiteEnrollmentSets(null);
		} else {
			rosterSite.setSiteEnrollmentSets(siteEnrollmentSets);
		}

		return rosterSite;
	}

	private List<RosterGroup> getViewableSiteGroups(String currentUserId,
			Site site) {
		List<RosterGroup> siteGroups = new ArrayList<RosterGroup>();

		boolean viewAll = isAllowed(currentUserId,
				RosterFunctions.ROSTER_FUNCTION_VIEWALL, site);

		for (Group group : site.getGroups()) {

			if (viewAll
					|| isAllowed(currentUserId,
							RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, group.getReference())) {

				RosterGroup rosterGroup = new RosterGroup(group.getId());
				rosterGroup.setTitle(group.getTitle());

				List<String> userIds = new ArrayList<String>();

				for (Member member : group.getMembers()) {
					userIds.add(member.getUserId());
				}

				rosterGroup.setUserIds(userIds);

				siteGroups.add(rosterGroup);
			}
		}
		return siteGroups;
	}

	private List<RosterEnrollment> getEnrollmentSets(String siteId,
			GroupProvider groupProvider) {
		List<RosterEnrollment> siteEnrollmentSets = new ArrayList<RosterEnrollment>();

		String[] sectionIds = groupProvider.unpackId(getSite(siteId)
				.getProviderGroupId());
		
		// avoid duplicates
		List<String> enrollmentSetIdsProcessed = new ArrayList<String>();

		for (String sectionId : sectionIds) {

			Section section = courseManagementService.getSection(sectionId);
			if (null == section) {
				continue;
			}

			EnrollmentSet enrollmentSet = section.getEnrollmentSet();
			if (null == enrollmentSet) {
				continue;
			}

			if (enrollmentSetIdsProcessed.contains(enrollmentSet.getEid())) {
				continue;
			}

			RosterEnrollment rosterEnrollmentSet = new RosterEnrollment(enrollmentSet.getEid());
			rosterEnrollmentSet.setTitle(enrollmentSet.getTitle());
			siteEnrollmentSets.add(rosterEnrollmentSet);

			enrollmentSetIdsProcessed.add(enrollmentSet.getEid());
		}
		return siteEnrollmentSets;
	}
	
	private boolean isAllowed(String userId, String permision, AuthzGroup authzGroup) {
		
		if (securityService.isSuperUser(userId)) {
			return true;
		}
		
		return authzGroup.isAllowed(userId, permision);
	}
	
	/**
	 * Calls the SecurityService unlock method. This is the method you must use in order for Delegated Access to work.
	 * Note that the SecurityService automatically handles super users.
	 * 
	 * @param userId		user uuid
	 * @param permission	permission to check for
	 * @param reference		reference to entity. The getReference() method should get you out of trouble.
	 * @return				true if user has permission, false otherwise
	 */
	private boolean isAllowed(String userId, String permission, String reference) {
		return securityService.unlock(userId, permission, reference);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean hasUserSitePermission(String userId, String permission, String siteId) {
				
		Site site = getSite(siteId);
		if (null == site) {
			return false;
		} else {
			return isAllowed(userId, permission, site.getReference());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean hasUserGroupPermission(String userId, String permission,
			String siteId, String groupId) {
				
		Site site = getSite(siteId);
		if (null == site) {
			return false;
		} else {
			if (null == site.getGroup(groupId)) {
				return false;
			} else {
				return isAllowed(userId, permission, site.getGroup(groupId).getReference());
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSakaiSkin() {
		String skin = serverConfigurationService.getString("skin.default");
		String siteSkin = siteService.getSiteSkin(getCurrentSiteId());
		return siteSkin != null ? siteSkin : (skin != null ? skin : "default");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSiteMaintainer(String siteId) {
		return hasUserSitePermission(getCurrentUserId(), SiteService.SECURE_UPDATE_SITE, siteId);
	}

}
