/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.roster;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public abstract class RosterManagerImpl implements RosterManager {
    private static final Log log = LogFactory.getLog(RosterManagerImpl.class);

    public abstract ProfileManager profileManager();
    public abstract PrivacyManager privacyManager();
    public abstract SectionAwareness sectionService();
    public abstract SiteService siteService();
    public abstract ToolManager toolManager();
    public abstract FunctionManager functionManager();
    public abstract UserDirectoryService userDirectoryService();
    public abstract AuthzGroupService authzGroupService();
    public abstract SecurityService securityService();
    public abstract CourseManagementService cmService();
    public abstract EventTrackingService eventTrackingService();
    
    public void init() {
        log.info("init()");

        Collection<String> registered = functionManager().getRegisteredFunctions(RosterFunctions.ROSTER_FUNCTION_PREFIX);
        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_EXPORT)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_EXPORT);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWALL)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWALL);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALPHOTO)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALPHOTO);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWGROUP)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWGROUP);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS);
        }

        if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWPROFILE)) {
            functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWPROFILE);
        }

    }

    public void destroy() {
        log.debug("destroy()");
    }

    private Participant createParticipantByUser(User user, Profile profile) {
        Set<String> userIds = new HashSet<String>();
        userIds.add(user.getId());

        String roleTitle = getUserRoleTitle(user);
        return new ParticipantImpl(user, profile, roleTitle, null);
    }


    /*
      * (non-Javadoc)
      *
      * @see org.sakaiproject.api.app.roster.RosterManager#getParticipantById(java.lang.String)
      */
    public Participant getParticipantById(String participantId) {
        if (log.isDebugEnabled()) {
            log.debug("getParticipantById(String" + participantId + ")");
        }
        if (participantId != null) {
            try {
                User user = userDirectoryService().getUser(participantId);
                Profile profile = profileManager().getUserProfileById(
                        participantId);
                return createParticipantByUser(user, profile);
            } catch (UserNotDefinedException e) {
                log.error("getParticipantById: " + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Retrieve a complete list of site participants that are viewable by the
     * current user.
     *
     * We have three different view scenarios:
     *
     * <ol>
     *
     * <li> View all: These users can see every site member, regardless of
     * privacy settings. </li>
     *
     * <li> View sections: These users can see every member of sections or
     * groups for which this user is a TA, regardless of privacy settings. These
     * users also see the other site members who have chosen to show themselves
     * (privacy setting = off). </li>
     *
     * <li> View non-hidden participants: These users see only site members who
     * have chosen to show themselves (privacy setting = off). </li>
     *
     * </ol>
     *
     * @return List
     */
    public List<Participant> getRoster() {
        List<Participant> participants;

        User currentUser = userDirectoryService().getCurrentUser();
        boolean viewAllInSite = userHasSitePermission(currentUser,
                RosterFunctions.ROSTER_FUNCTION_VIEWALL);

        Map<Group, Set<String>> groupMembers = getGroupMembers();

        // Users with "viewall" see everybody
        if (viewAllInSite) {
            participants = getParticipantsInSite();
        } else {
            participants = getParticipantsInGroups(currentUser, groupMembers);
        }

        filterHiddenUsers(currentUser, participants, groupMembers);
        return participants;
    }

    public List<Participant> getRoster(String groupReference) {
        User currentUser = userDirectoryService().getCurrentUser();
        Map<Group, Set<String>> groupMembers = getGroupMembers(groupReference);
        List<Participant> participants = getParticipantsInGroups(currentUser, groupMembers);
        filterHiddenUsers(currentUser, participants, groupMembers);
        return participants;
    }

    /**
     * Gets a Map of the groups in this site (key) to the user IDs for the members in the group (value)
     * @return
     */
    private Map<Group, Set<String>> getGroupMembers() {
        Map<Group, Set<String>> groupMembers = new HashMap<Group, Set<String>>();
        Site site;
        try {
            site = siteService().getSite(getSiteId());
        } catch (IdUnusedException ide) {
            log.warn(ide);
            return groupMembers;
        }
        Collection<Group> groups = site.getGroups();
        //for(Iterator<Group> groupIter = groups.iterator(); groupIter.hasNext();) {
        for (Group group : groups) {
            //Group group = groupIter.next();
            Set<String> userIds = new HashSet<String>();
            Set<Member> members = group.getMembers();
            for(Iterator<Member> memberIter = members.iterator(); memberIter.hasNext();) {
                Member member = memberIter.next();
                userIds.add(member.getUserId());
            }
            groupMembers.put(group, userIds);
        }
        return groupMembers;
    }

    /**
     * Gets a Map of a group to the user IDs for the members in the group
     * @return
     */
    private Map<Group, Set<String>> getGroupMembers(String groupReference) {
        Map<Group, Set<String>> groupMembers = new HashMap<Group, Set<String>>();
        Group group = siteService().findGroup(groupReference);
        if(group == null) {
            log.warn("Group " + groupReference + " not found");
            return groupMembers;
        }
        //
        Set<String> userIds = new HashSet<String>();
        Set<Member> members = group.getMembers();
        for(Iterator<Member> memberIter = members.iterator(); memberIter.hasNext();) {
            Member member = memberIter.next();
            userIds.add(member.getUserId());
        }
        groupMembers.put(group, userIds);
        return groupMembers;
    }

    private void filterHiddenUsers(User currentUser, List<Participant> participants, Map<Group, Set<String>> groupMembers) {
        // If the user has view hidden in the site, don't filter anyone out
        if(userHasSitePermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN)) {
            return;
        }

        // Keep track of the users for which the current user has the group-scoped view hidden permission
        Set<String> visibleMembersForCurrentUser = new HashSet<String>();
        for (Entry<Group, Set<String>> e : groupMembers.entrySet()) {
            if(userHasGroupPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, e.getKey().getReference())) {
                visibleMembersForCurrentUser.addAll(e.getValue());
            }
        }

        // Iterate through the participants, removing the hidden ones that are not in visibleMembersForCurrentUser
        Set<String> userIds = new HashSet<String>();
        for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
            Participant participant = iter.next();
            userIds.add(participant.getUser().getId());
        }

        Set<String> hiddenUsers = privacyManager().findHidden("/site/" + getSiteId(), userIds);

        for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
            Participant participant = iter.next();
            String userId = participant.getUser().getId();
            if(hiddenUsers.contains(userId) && ! visibleMembersForCurrentUser.contains(userId)) {
                iter.remove();
            }
        }
    }

    private List<Participant> getParticipantsInSite() {
        Map<String, UserRole> userMap = getUserRoleMap(getSiteReference());
        Map<String, Profile> profiles = profileManager().getProfiles(userMap.keySet());
        return buildParticipantList(userMap, profiles);
    }

    private List<Participant> getParticipantsInGroups(User currentUser, Map<Group, Set<String>> groupMembers) {
        boolean userHasSiteViewAll = userHasSitePermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWALL);
        Set<String> viewableUsers = new HashSet<String>();
        //for(Iterator<Group> iter = groupMembers.keySet().iterator(); iter.hasNext();) {
            //Group group = iter.next();
        for(Entry<Group,Set<String>> e : groupMembers.entrySet()) {
            if(userHasGroupPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWALL, e.getKey().getReference())
                    || userHasSiteViewAll) {
                viewableUsers.addAll(e.getValue());
            }
        }

        // Build the list of participants

        // Use the site reference because we need to display the site role, not the group role
        Map<String, UserRole> userMap = getUserRoleMap(getSiteReference());
        Map<String, Profile> profilesMap = profileManager().getProfiles(viewableUsers);
        return buildParticipantList(userMap, profilesMap);
    }

    private List<Participant> buildParticipantList(Map<String, UserRole> userMap, Map<String, Profile> profilesMap) {
        List<Participant> participants = new ArrayList<Participant>();
        Site site = null;
        try {
			site = siteService().getSite(getSiteId());
		} catch (IdUnusedException e) {
			log.error("getGroupsWithMember: " + e.getMessage(), e);
			return participants;
		}
		Collection<Group> groups = site.getGroups();
		
        for (Iterator<Entry<String, Profile>> iter = profilesMap.entrySet().iterator(); iter.hasNext();) {
            Entry<String, Profile> entry = iter.next();
            String userId = entry.getKey();
            Profile profile = entry.getValue();

            UserRole userRole = userMap.get(userId);

            // Profiles may exist for users that have been removed.  If there's a profile
            // for a missing user, skip the profile.  See SAK-10936
            if(userRole == null || userRole.user == null) {
                log.warn("A profile exists for non-existent user " + userId);
                continue;
            }
            
            String groupsString = "";
            for (Group group : groups)
            {
            	Member member = group.getMember(userId);
            	StringBuffer sb = new StringBuffer();
            	if (member !=null)
            	{
        			sb.append(group.getTitle() + ", ");
            	}
            	groupsString = sb.toString();
            }
            
            if (groupsString != "")
            {
            	int endIndex = groupsString.lastIndexOf(", ");
		if(endIndex > 0)
		{
            		groupsString = groupsString.substring(0, endIndex);
		}
            }

            participants.add(new ParticipantImpl(userRole.user, profile, userRole.role, groupsString));
        }
        return participants;
    }
    
    private Comparator<Group> sortGroups() {
    	Comparator<Group> groupComparator = new Comparator<Group>() {
			public int compare(Group one, Group another)
			{
				return Collator.getInstance().compare(one.getTitle(),another.getTitle());
			}
    	};
        return groupComparator;
    }

    static class UserRole {
        User user;
        String role;

        UserRole(User user, String role)
        {
            this.user = user;
            this.role = role;
        }
    }

    /**
     * Gets a map of user IDs to UserRole (User + Role) objects.
     *
     * @return
     */
    private Map<String, UserRole> getUserRoleMap(String authzRef) {
        Map<String, UserRole> userMap = new HashMap<String, UserRole>();
        Set<String> userIds = new HashSet<String>();
        Set<Member> members;

        // Get the member set
        try {
            members = authzGroupService().getAuthzGroup(authzRef).getMembers();
        } catch (GroupNotDefinedException e) {
            log.error("getUsersInAllSections: " + e.getMessage(), e);
            return userMap;
        }

        // Build a map of userId to role
        Map<String, String> roleMap = new HashMap<String, String>();
        for(Iterator<Member> iter = members.iterator(); iter.hasNext();)
        {
            Member member = iter.next();
            if (member.isActive()) {
	            // SAK-17286 Only list users that are 'active' not 'inactive'
				userIds.add(member.getUserId());
	            roleMap.put(member.getUserId(), member.getRole().getId());
			}
        }

        // Get the user objects
        List<User> users = userDirectoryService().getUsers(userIds);
        for (Iterator<User> iter = users.iterator(); iter.hasNext();)
        {
            User user = iter.next();
            String role = roleMap.get(user.getId());
            userMap.put(user.getId(), new UserRole(user, role));
        }
        return userMap;
    }


    /*
      * (non-Javadoc)
      *
      * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasExportPerm()
      */
    public boolean currentUserHasExportPerm() {
        return userHasSitePermission(userDirectoryService().getCurrentUser(),
                RosterFunctions.ROSTER_FUNCTION_EXPORT);
    }

    /**
     * Check if given user has the given permission
     *
     * @param user
     * @param permissionName
     * @return boolean
     */
    private boolean userHasSitePermission(User user, String permissionName) {
        if (user == null || permissionName == null) {
            if(log.isDebugEnabled()) log.debug("userHasSitePermission passed a null");
            return false;
        }
        String siteReference = getSiteReference();
        boolean result = securityService().unlock(user, permissionName, siteReference);
        if(log.isDebugEnabled()) log.debug("user " + user.getEid() + ", permission " + permissionName + ", site " + siteReference + " has permission? " + result);
        return result;
    }

    private boolean userHasGroupPermission(User user, String permissionName, String groupReference) {
        if (user == null || permissionName == null || groupReference == null) {
            if(log.isDebugEnabled()) log.debug("userHasGroupPermission passed a null");
            return false;
        }
        boolean result =  authzGroupService().isAllowed(user.getId(), permissionName, groupReference);
        if(log.isDebugEnabled()) log.debug("user " + user.getEid() + ", permission " + permissionName + ", group " + groupReference + " has permission? " + result);
        return result;
    }

    /**
     * @return siteId
     */
    private String getSiteReference() {
        return siteService().siteReference(getSiteId());
    }

    private String getSiteId() {
        return toolManager().getCurrentPlacement().getContext();
    }

    /**
     *
     * @param user
     * @return
     */
    private String getUserRoleTitle(User user) {
        return authzGroupService().getUserRole(user.getId(),getSiteId());
    }

    /**
     * Determine if sectioning exists in this site
     *
     * @return
     */
    public boolean siteHasSections() {
        return ! sectionService().getSections(getSiteId()).isEmpty();
    }

    public List<CourseSection> getViewableSectionsForCurrentUser() {
        User user = userDirectoryService().getCurrentUser();
        List<CourseSection> sections = sectionService().getSections(getSiteId());
        // If the user can view all groups in the site, return them all
        if(userHasSitePermission(user, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP)) {
            return sections;
        }

        for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
            CourseSection section = iter.next();
            if( ! userHasGroupPermission(user, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP, section.getUuid())) {
                iter.remove();
            }
        }
        return sections;
    }

    public List<CourseSection> getViewableEnrollmentStatusSectionsForCurrentUser() {
        User user = userDirectoryService().getCurrentUser();
        List<CourseSection> sections = getViewableSectionsForCurrentUser();

        // If the user can view enrollment statuses at the site level, return all of the sections
        boolean siteScopedEnrollmentPermission = userHasSitePermission(user, RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS);

        for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
            CourseSection section = iter.next();
            if(section.getEid() != null) {
                // This is an official section.  Does it have an enrollment set?
                Section cmSection = cmService().getSection(section.getEid());
                if(cmSection.getEnrollmentSet() == null) {
                    iter.remove();
                } else {
                    // Does the current user have access to view enrollments for this section?
                    if( ! siteScopedEnrollmentPermission && ! userHasGroupPermission(user, RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS, section.getUuid())) {
                        iter.remove();
                    }
                }
            }

        }
        return sections;
    }

    public boolean isProfilesViewable() {
        return userHasSitePermission(userDirectoryService().getCurrentUser(),
                RosterFunctions.ROSTER_FUNCTION_VIEWPROFILE);
    }

    public boolean isOfficialPhotosViewable() {
        return userHasSitePermission(userDirectoryService().getCurrentUser(),
                RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALPHOTO);
    }

    public boolean isGroupMembershipViewable() {
    	Site site;
        try {
            site = siteService().getSite(getSiteId());
        } catch (IdUnusedException ide) {
            log.warn("isGroupMembershipViewable: " + ide);
            return false;
        }
        Collection groups = site.getGroups();
        if (groups.isEmpty())
        	return false;
        return true;
    }
}
