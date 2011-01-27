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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public final static String DEFAULT_SORT_COLUMN = "sortName";
	public final static Boolean DEFAULT_FIRST_NAME_LAST_NAME = false;
	public final static Boolean DEFAULT_HIDE_SINGLE_GROUP_FILTER = false;
	public final static Boolean DEFAULT_VIEW_EMAIL = true;
	public final static Boolean DEFAULT_VIEW_USER_DISPLAY_ID = true;
	public final static Boolean DEFAULT_USE_PICTURES_AS_DEFAULT_VIEW = false;
	
	private CourseManagementService courseManagementService;
	private FunctionManager functionManager = null;
	private PrivacyManager privacyManager = null;
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
        
	}
	
	private boolean isSuperUser() {
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

		return serverConfigurationService.getBoolean("roster_view_email",
				DEFAULT_VIEW_EMAIL);
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
	public Boolean getUsePicturesAsDefaultView() {
		return serverConfigurationService.getBoolean(
				"roster.usePicturesAsDefaultView", DEFAULT_USE_PICTURES_AS_DEFAULT_VIEW);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getSiteMembership(String siteId) {
		return getMembership(siteId, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getGroupMembership(String siteId, String groupId) {
		return getMembership(siteId, groupId);
	}
	
	private List<RosterMember> getMembership(String siteId, String groupId) {
		
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
		Set<Member> membership = getFilteredMembers(groupId, getCurrentUserId(), site);
		if (null == membership) {
			return null;
		}
		
		for (Member member : membership) {

			try {

				RosterMember rosterMember = getRosterMember(member, site);

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

		// permissions are handled inside this method call
		Set<Member> membership = null;
		if (true == filtered) {
			membership = getFilteredMembers(groupId, getCurrentUserId(), site);
		} else {
			membership = getUnfilteredMembers(groupId, getCurrentUserId(), site);
		}
		
		if (null == membership) {
			return null;
		}

		for (Member member : membership) {

			try {

				RosterMember rosterMember = getRosterMember(member, site);

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

		if (isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWALL,
				site)) {

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
							RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, group)) {

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
								.getGroup(groupId))) {

					membership.addAll(filterHiddenMembers(site
							.getGroup(groupId).getMembers(), currentUserId,
							site.getId(), site.getGroup(groupId)));
				}
			} else {
				// assume invalid groupId specified or user not member
				return null;
			}
		}
		return membership;
	}
	
	@SuppressWarnings("unchecked")
	private Set<Member> filterHiddenMembers(Set<Member> membership,
			String currentUserId, String siteId, AuthzGroup authzGroup) {

		if (isAllowed(currentUserId,
				RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, authzGroup)) {

			return membership;
		}

		Set<Member> filteredMembership = new HashSet<Member>();

		Set<String> userIds = new HashSet<String>();
		for (Member member : membership) {
			userIds.add(member.getUserEid());
		}

		Set<String> hiddenUserIds = privacyManager.findHidden(
				"/site/" + siteId, userIds);

		for (Member member : membership) {
			if (!hiddenUserIds.contains(member.getUserEid())) {
				filteredMembership.add(member);
			}
		}

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
	
	private RosterMember getRosterMember(Member member, Site site)
			throws UserNotDefinedException {
		
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
				RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS, site)) {

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
			member.setStatus(statusCodes.get(enrollment.getEnrollmentStatus()));

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
			return null;
		}

		Site site = getSite(siteId);
		if (null == site) {
			return null;
		}

		// return null if user is not a site member and not an admin user
		if (null == site.getMember(currentUserId) && !isSuperUser()) {
			return null;
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
							RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, group)) {

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
	 * {@inheritDoc}
	 */
	public Boolean hasUserSitePermission(String userId, String permission, String siteId) {
				
		Site site = getSite(siteId);
		if (null == site) {
			return false;
		} else {
			return isAllowed(userId, permission, site);
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
				return isAllowed(userId, permission, site.getGroup(groupId));
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
		
}
