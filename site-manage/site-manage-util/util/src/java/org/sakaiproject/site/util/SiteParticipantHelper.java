/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.site.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SiteParticipantHelper {

	private static String NULL_STRING = "";
	
	private static org.sakaiproject.authz.api.GroupProvider groupProvider = (org.sakaiproject.authz.api.GroupProvider) ComponentManager
	.get(org.sakaiproject.authz.api.GroupProvider.class);
	
	private static AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);
	
	private static org.sakaiproject.coursemanagement.api.CourseManagementService cms = (org.sakaiproject.coursemanagement.api.CourseManagementService) ComponentManager
	.get(org.sakaiproject.coursemanagement.api.CourseManagementService.class);
	
	private static org.sakaiproject.user.api.ContextualUserDisplayService cus = (org.sakaiproject.user.api.ContextualUserDisplayService) ComponentManager
	.get(org.sakaiproject.user.api.ContextualUserDisplayService.class);

	private static org.sakaiproject.authz.api.SecurityService securityService = (org.sakaiproject.authz.api.SecurityService) ComponentManager
	.get(org.sakaiproject.authz.api.SecurityService.class );

	private static org.sakaiproject.component.api.ServerConfigurationService scs = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager
	.get(org.sakaiproject.component.api.ServerConfigurationService.class);

	private static String showOrphanedMembers = scs.getString("site.setup.showOrphanedMembers", "admins");

	private static ResourceLoader rb = new ResourceLoader("UserDirectoryProvider");

	// SAK-23257: restrict the roles available for participants
	private static final String	SAK_PROP_RESTRICTED_ROLES 	= "sitemanage.addParticipants.restrictedRoles";
	
	/**
	 * Add participant from provider-defined enrollment set
	 * @param participants
	 * @param realm
	 * @param providerCourseEid
	 * @param enrollmentSet
	 */
	public static void addParticipantsFromEnrollmentSet(Map participantsMap, AuthzGroup realm, String providerCourseEid, EnrollmentSet enrollmentSet, String sectionTitle) {
		boolean refreshed = false;
		
		if (enrollmentSet != null)
		{
			Set enrollments = cms.getEnrollments(enrollmentSet.getEid());
			if (enrollments != null)
			{
				Map<String, User> eidToUserMap = getEidUserMapFromCollection(enrollments);
				for (Iterator eIterator = enrollments.iterator();eIterator.hasNext();)
				{
					Enrollment e = (Enrollment) eIterator.next();
					
					// ignore the dropped enrollments
					if(e.isDropped()){
						continue;
					}
					
					try 
					{
						User user = eidToUserMap.get(e.getUserId());
						if (user == null) {
							throw new UserNotDefinedException(e.getUserId());
						}
						String userId = user.getId();
						Member member = realm.getMember(userId);
						
						// this person is in the cm, so they should be in the realm
						// force a refresh. Only do this once, since a refresh should get everyone
						// it would be nice for AuthzGroupService to expose refresh, but a save
						// will do it
						if (member == null && !refreshed) {
						    try {
								// do it only once
								refreshed = true;
								// refresh the realm
								AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm.getId());
								authzGroupService.save(realmEdit);
								// refetch updated realm
								realm = authzGroupService.getAuthzGroup(realm.getId());
								member = realm.getMember(userId);
						    } catch (Exception exc) {
						    	log.warn("SiteParticipantHelper.addParticipantsFromEnrollment " + exc.getMessage());
						    }
						}
						
						if (member != null)
						{
							try
							{
							// get or add provided participant
							Participant participant;
							if (participantsMap.containsKey(userId))
							{
								participant = (Participant) participantsMap.get(userId);
								//does this section contain the eid already
								if (!participant.getSectionEidList().contains(sectionTitle)) {
									participant.addSectionEidToList(sectionTitle);
								}
								if (e.getCredits() != null && e.getCredits().length() >0)
								{
									participant.credits = participant.credits.concat(", <br />" + e.getCredits());
								}
							}
							else
							{
								participant = new Participant();
								participant.credits = e.getCredits() != null?e.getCredits():"";
								participant.name = user.getSortName();
								if (member.isProvided())
								{
									participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
									participant.removeable = false;
								}
								else
								{
									participant.providerRole="";
									participant.removeable = true;
								}
								// get contextual user display id
								String regId = cus != null ? cus.getUserDisplayId(user, "Site Info"):"";
								participant.regId = regId != null?regId:"";
								participant.role = member.getRole()!=null?member.getRole().getId():"";
								participant.addSectionEidToList(sectionTitle);
								participant.uniqname = userId;
								participant.active = member.isActive();
							}
							participantsMap.put(userId, participant);
							}
							catch (Exception ee)
							{
								log.warn("SiteParticipantHelper.addParticipantsFromEnrollmentSet: " + ee.getMessage() + " user id = " + userId, ee);
							}
						}
					} catch (UserNotDefinedException exception) {
						// deal with missing user quietly without throwing a
						// warning message
						log.warn("SiteParticipantHelper.addParticipantsFromEnrollmentSet: " + exception.getMessage() + " user id = " + e.getUserId());
					}
				}
			}
		}
	}
	
	/**
	 * Collect all member users data in one call
	 * 
	 * @param memberships
	 * @return
	 */
	public static Map<String, User> getEidUserMapFromCollection(Collection<Object> cObjects) {
		Set<String> rvEids = new HashSet<String>();
		for (Object cObject : cObjects) {
			
			if (cObject instanceof Enrollment)
			{
				rvEids.add(((Enrollment) cObject).getUserId());
			} else if (cObject instanceof Membership)
			{
				rvEids.add(((Membership) cObject).getUserId());
			} else if (cObject instanceof Member)
			{
				rvEids.add(((Member) cObject).getUserEid());
			} 
		}
		Map<String, User> eidToUserMap = new HashMap<String, User>();
		List<User> rvUsers = UserDirectoryService.getUsersByEids(rvEids);
		for (User user : rvUsers) {
			eidToUserMap.put(user.getEid(), user);
		}
		return eidToUserMap;
	}

	/**
	 * Add participant from provider-defined membership set
	 * @param participants
	 * @param realm
	 * @param providerCourseEid
	 * @param memberships
	 */
	public static void addParticipantsFromMemberships(Map participantsMap, AuthzGroup realm, Set memberships, String sectionTitle) {
		boolean refreshed = false;
		
		if (memberships != null)
		{
			Map<String, User> eidToUserMap = getEidUserMapFromCollection(memberships);
			for (Iterator<Membership> mIterator = memberships.iterator();mIterator.hasNext();)
			{
				Membership m = (Membership) mIterator.next();
				try 
				{
					User user = eidToUserMap.get(m.getUserId());
					if (user == null) {
						throw new UserNotDefinedException(m.getUserId());
					}
					String userId = user.getId();
					Member member = realm.getMember(userId);
					
					// this person is in the cm, so they should be in the realm
					// force a refresh. Only do this once, since a refresh should get everyone
					// it would be nice for AuthzGroupService to expose refresh, but a save
					// will do it
					if (member == null && !refreshed) {
					    try {
							// do it only once
							refreshed = true;
							// refresh the realm
							AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realm.getId());
							authzGroupService.save(realmEdit);
							// refetch updated realm
							realm = authzGroupService.getAuthzGroup(realm.getId());
							member = realm.getMember(userId);
					    } catch (Exception exc) {
					    	log.warn("SiteParticipantHelper:addParticipantsFromMembership " + exc.getMessage());
					    }
					}
					
					if (member != null)
					{
						// get or add provided participant
						Participant participant;
						if (participantsMap.containsKey(userId))
						{
							participant = (Participant) participantsMap.get(userId);
							participant.addSectionEidToList(sectionTitle);						}
						else
						{
							participant = new Participant();
							participant.credits = "";
							participant.name = user.getSortName();
							if (member.isProvided())
							{
								participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
								participant.removeable = false;
							}
							participant.regId = "";
							participant.role = member.getRole()!=null?member.getRole().getId():"";
							participant.addSectionEidToList(sectionTitle);
							participant.uniqname = userId;
							participant.active=member.isActive();
						}
						
						participantsMap.put(userId, participant);
					}
				} catch (UserNotDefinedException exception) {
					// deal with missing user quietly without throwing a
					// warning message
					log.debug("SiteParticipantHelper:addParticipantsFromMemberships: user not defined id = " + m.getUserId());
				}
			}
		}
	}
	
	/**
	 * add participant from member list defined in realm
	 * @param participantsMap
	 * @param grants
	 */
    	private static void addParticipantsFromMembers(Map<String, Participant> participantsMap, Set grants, String realmId) {
		// get all user info once
		Map<String, User> eidToUserMap = getEidUserMapFromCollection(grants);
		boolean refreshed = false;
		
		for (Iterator<Member> i = grants.iterator(); i.hasNext();) {
			Member g = (Member) i.next();
			try {
				User user = eidToUserMap.get(g.getUserEid());
				if (user == null) {
					throw new UserNotDefinedException(g.getUserEid());
				}
				String userId = user.getId();
				if (!participantsMap.containsKey(userId))
				{
				    // we should have seen all provided users by now. If any
				    // are left, they are out of date. Refresh the realm
				    // but also skip the users. Otherwise if the owner submits
				    // the screen, they get created.
				        if (g.isProvided()) {
					    if (!refreshed) {
						refreshed = true;
						try {
						    // refresh the realm
						    AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
						    authzGroupService.save(realmEdit);
						} catch (Exception exc) {
						    log.warn("SiteParticipantHelper:addParticipantsFromMembers " + exc.getMessage());
						}

					    }
					    continue;
					}

					Participant participant;
					if (participantsMap.containsKey(userId))
					{
						participant = (Participant) participantsMap.get(userId);
					}
					else
					{
						participant = new Participant();
					}
					participant.name = user.getSortName();
					participant.uniqname = userId;
					participant.role = g.getRole()!=null?g.getRole().getId():"";
					participant.removeable = true;
					participant.active = g.isActive();
					participantsMap.put(userId, participant);
				}
			} catch (UserNotDefinedException e) {

				if (("admins".equals(showOrphanedMembers) && securityService.isSuperUser()) || ("maintainers".equals(showOrphanedMembers))) {
					// add non-registered participant
					String userId = g.getUserId();
					Participant participant = new Participant();
					participant.name = makeUserDisplayName(userId);
					participant.uniqname = userId;
					participant.role = g.getRole() != null ? g.getRole().getId() :"";
					participant.removeable = true;
					participant.active = g.isActive();
					participantsMap.put(userId, participant);
				}

				if (log.isDebugEnabled()) {
					log.debug("SiteParticipantHelper:addParticipantsFromMembers: user not defined "+ g.getUserEid());
				}
			}
		}
	}
	
	/**
	 * getExternalRealmId
	 * 
	 */
	private static String getExternalRealmId(String siteId) {
		String realmId = SiteService.siteReference(siteId);
		String rv = null;
		try {
			AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
			rv = realm.getProviderGroupId();
		} catch (GroupNotDefinedException e) {
			log.warn("SiteParticipantHelper.getExternalRealmId: site realm not found " + realmId);
		}
		return rv;

	} // getExternalRealmId
	
	/**
	 * getProviderCourseList a course site/realm id in one of three formats, for
	 * a single section, for multiple sections of the same course, or for a
	 * cross-listing having multiple courses. getProviderCourseList parses a
	 * realm id into year, term, campus_code, catalog_nbr, section components.
	 * 
	 * @param id
	 *            is a String representation of the course realm id (external
	 *            id).
	 */
	public static List<String> getProviderCourseList(String siteId) {
		String id = getExternalRealmId(siteId);
		
		Vector<String> rv = new Vector<String>();
		if (id == null || NULL_STRING.equals(id) ) {
			return rv;
		}
		// Break Provider Id into course id parts
		String[] courseIds = groupProvider.unpackId(id);
		
		// Iterate through course ids
		for (int i=0; i<courseIds.length; i++) {
			String courseId = (String) courseIds[i];

			rv.add(courseId);
		}
		return rv;

	} // getProviderCourseList
	
	public static Collection<Participant> prepareParticipants(String siteId, List<String> providerCourseList) {
		String realmId = SiteService.siteReference(siteId);
		Map<String, Participant> participantsMap = new ConcurrentHashMap<String, Participant>();
		try {
			AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
			realm.getProviderGroupId();
			
			// iterate through the provider list first
			for (Iterator<String> i=providerCourseList.iterator(); i.hasNext();)
			{
				String providerCourseEid = (String) i.next();
				try
				{
					Section section = cms.getSection(providerCourseEid);
					
					if (section != null)
					{
						String sectionTitle = section.getTitle();
					
						// in case of Section eid
						EnrollmentSet enrollmentSet = section.getEnrollmentSet();
						addParticipantsFromEnrollmentSet(participantsMap, realm, providerCourseEid, enrollmentSet, sectionTitle);
						
						// Include official instructors of record for the enrollment set.
						addOfficialInstructorOfRecord(participantsMap, realm, sectionTitle, enrollmentSet);
						
						// add memberships
						Set memberships = cms.getSectionMemberships(providerCourseEid);
						if (memberships != null && memberships.size() > 0)
						{
							addParticipantsFromMemberships(participantsMap, realm, memberships, sectionTitle);
						}
						
						// now look or the not-included member from CourseOffering object
						CourseOffering co = cms.getCourseOffering(section.getCourseOfferingEid());
						if (co != null)
						{
							
							Set<Membership> coMemberships = cms.getCourseOfferingMemberships(section.getCourseOfferingEid());
							if (coMemberships != null && coMemberships.size() > 0)
							{
								addParticipantsFromMemberships(participantsMap, realm, coMemberships, co.getTitle());
							}
							
							// now look or the not-included member from CourseSet object
							Set<String> cSetEids = co.getCourseSetEids();
							if (cSetEids != null)
							{
								for(Iterator<String> cSetEidsIterator = cSetEids.iterator(); cSetEidsIterator.hasNext();)
								{
									String cSetEid = cSetEidsIterator.next();
									CourseSet cSet = cms.getCourseSet(cSetEid);
									if (cSet != null)
									{
										Set<Membership> cSetMemberships = cms.getCourseSetMemberships(cSetEid);
										if (cSetMemberships != null && cSetMemberships.size() > 0)
										{
											addParticipantsFromMemberships(participantsMap, realm, cSetMemberships, cSet.getTitle());
										}
									}
								}
							}
						}
					}
					
				}
				catch (IdNotFoundException e)
				{
					log.warn("SiteParticipantHelper.prepareParticipants: "+ e.getMessage() + " sectionId=" + providerCourseEid);
				}
			}
			
			// now for those not provided users
			Set<Member> grants = realm.getMembers();
			if (grants != null && !grants.isEmpty())
			{
				// add participant from member defined in realm
				addParticipantsFromMembers(participantsMap, grants, realmId);
			}

		} catch (GroupNotDefinedException ee) {
			log.warn("SiteParticipantHelper.prepareParticipants:  IdUnusedException " + realmId);
		}
		return participantsMap.values();
	}

	private static void addOfficialInstructorOfRecord(Map<String, Participant> participantsMap, AuthzGroup realm, String sectionTitle, EnrollmentSet enrollmentSet) {
		
		if (enrollmentSet != null)
		{
			Set<String>instructorEids = cms.getInstructorsOfRecordIds(enrollmentSet.getEid());
			if ((instructorEids != null) && (instructorEids.size() > 0)) {
				for (String userEid : instructorEids) {
					// This logic is copied-and-pasted from addParticipantsFromMemberships
					// and really should be in a shared method, but refactoring would make
					// it harder to merge changes.
					try
					{
						User user = UserDirectoryService.getUserByEid(userEid);
						String userId = user.getId();
						Member member = realm.getMember(userId);
						if (member != null)
						{
							// get or add provided participant
							Participant participant;
							if (participantsMap.containsKey(userId))
							{
								participant = (Participant) participantsMap.get(userId);
								if (!participant.section.contains(sectionTitle))
								{
									participant.section = participant.section.concat(", <br />" + sectionTitle);
								}
							}
							else
							{
								participant = new Participant();
								participant.credits = "";
								participant.name = user.getSortName();
								if (member.isProvided())
								{
									participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
									participant.removeable = false;
								}
								participant.regId = "";
								participant.removeable = false;
								participant.role = member.getRole()!=null?member.getRole().getId():"";
								participant.section = sectionTitle;
								participant.uniqname = userId;
							}
							participantsMap.put(userId, participant);
						}
					} catch (UserNotDefinedException exception) {
						// deal with missing user quietly without throwing a
						// warning message
						log.warn(exception.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * Get a list of restricted roles, taking into account the current site type
	 * 
	 * @param siteType
	 * 				the current site's type
	 * @return a list of restricted role IDs for the given site type
	 */
	public static Set<String> getRestrictedRoles( String siteType )
	{
		// Add all root level restricted roles
		Set<String> retVal = new HashSet<String>();
		retVal.addAll(Arrays.asList(ArrayUtils.nullToEmpty(scs.getStrings(SAK_PROP_RESTRICTED_ROLES))));
		
		// Add all site type specficic restricted roles
		if(siteType != null && !"".equals(siteType)) {
			retVal.addAll(Arrays.asList(ArrayUtils.nullToEmpty(scs.getStrings(SAK_PROP_RESTRICTED_ROLES + "." + siteType))));
		}
		
		return retVal;
	}

	/**
	 * Get a list of the 'allowed roles', taking into account the current site type
	 * and the list of restricted roles defined in sakai.properties.
	 * If the properties are not found, just return all the roles.
	 * If the user is an admin, return all the roles.
	 * 
	 * SAK-23257
	 * 
	 * @param siteType
	 * 				the current site's type
	 * @return A list of 'allowed' role objects for the given site type
	 */
	public static List<Role> getAllowedRoles( String siteType, List<Role> allRolesForSiteType )
	{
		List<Role> retVal = new ArrayList<Role>();
		if (siteType == null) {
			siteType = "";
		}
		
		// Get all the restricted roles for this site type, as well as all restricted roles at the top level (restricted for all site types)
		Set<String> restrictedRoles = getRestrictedRoles(siteType);
				
		// Loop through all the roles for this site type
		for( Role role : allRolesForSiteType )
		{
			if (!authzGroupService.isRoleAssignable(role.getId())) {
				continue;
			}
			// If the user is an admin, or if the properties weren't found (empty set), just add the role to the list
			if( securityService.isSuperUser() || restrictedRoles.isEmpty() )
			{
				retVal.add( role );
			}
        	
        	// Otherwise, only add the role to the list of 'allowed' roles if it's not present in the set of 'restricted' roles
         	else
         	{
        		if( !restrictedRoles.contains( role.getId() ) && !restrictedRoles.contains( role.getId().toLowerCase() ) )
        		{
        			retVal.add( role );
        		}
         	}
        }
		
		return retVal;
	}

	public static List<Role> getAllowedRoles( String siteType, Set<Role> allRolesForSiteType )
	{
		List<Role> list = new ArrayList<Role>(allRolesForSiteType.size());
		list.addAll(allRolesForSiteType);
		return getAllowedRoles( siteType, list );
	}
	
	public static String makeUserDisplayName( String userId ) 
	{
		String userDisplayName = NULL_STRING;

		userDisplayName = rb.getFormattedMessage("udp.unregistered", userId);

		return userDisplayName;
	}

}
