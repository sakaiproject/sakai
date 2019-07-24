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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

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
	 * @param participantsMap
	 * @param realm
	 * @param providerCourseEid
	 * @param enrollmentSet
	 * @param sectionTitle
	 */
	public static void addParticipantsFromEnrollmentSet(Map<String, Participant> participantsMap, AuthzGroup realm, String providerCourseEid, EnrollmentSet enrollmentSet, String sectionTitle) {
		addParticipantsFromEnrollmentSet(participantsMap, realm, providerCourseEid, enrollmentSet, sectionTitle, null, null, null);
	}
	
	/**
	 * Add participant from provider-defined enrollment set
	 * @param participantsMap
	 * @param realm
	 * @param providerCourseEid
	 * @param enrollmentSet
	 * @param sectionTitle
	 * @param filterType
	 *          the type of filter (section, group or role)
	 * @param filterID
	 *          the ID to filter on
	 * @param groupMembership
	 */
	public static void addParticipantsFromEnrollmentSet(Map<String, Participant> participantsMap, AuthzGroup realm, String providerCourseEid, EnrollmentSet enrollmentSet, String sectionTitle,
															String filterType, String filterID, Set<Member> groupMembership) {
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
								participant.regId = cus != null ? cus.getUserDisplayId(user, "Site Info") : user.getDisplayId();
								participant.role = member.getRole()!=null?member.getRole().getId():"";
								participant.addSectionEidToList(sectionTitle);
								participant.uniqname = userId;
								participant.active = member.isActive();
							}

							conditionallyAddParticipantToMap(participantsMap, filterType, filterID, userId, participant, groupMembership);
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
		* Conditionally add the provided Participant object to the participants map
		* if the participant meets the given conditions (filter)
		*
		* @param participantsMap
		*			the map to add the participant to if it meets the conditions
		* @param filterType
		*			the conditional type of filter
		* @param filterID
		*			the conditional ID of the filter
		* @param userID
		*			the user ID of the participant
		* @param participant
		*			the Participant object representing the participant
		* @param groupMembership
		*			the list of group memberships to compare against for group filters
		*/
	private static void conditionallyAddParticipantToMap(Map<String, Participant> participantsMap, String filterType, String filterID, String userID,
															Participant participant, Set<Member> groupMembership) {

		// If a section filter is selected, the section filtering has already been applied prior to calling this method.
		// Therefore, in this case all members passed to this function belong to the desired section; so we always add them to the map in this case.
		if ((SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE.equals(filterType) && participant.role.equals(filterID))
				|| SiteConstants.PARTICIPANT_FILTER_TYPE_ALL.equals(filterType)
				|| StringUtils.isEmpty(filterType)
				|| SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION.equals(filterType)) {
			participantsMap.put(userID, participant);
		} else if (SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP.equals(filterType) && groupMembership != null) {
			for (Member m : groupMembership) {
				if (userID.equals(m.getUserId())) {
					participantsMap.put(userID, participant);
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
	 * @param participantsMap
	 * @param realm
	 * @param memberships
	 * @param sectionTitle
	 * @param filterType
	 * @param filterID
	 * @param groupMembership
	 */
	public static void addParticipantsFromMemberships(Map participantsMap, AuthzGroup realm, Set memberships, String sectionTitle, String filterType, String filterID,
														Set<Member> groupMembership) {
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
							participant.regId = user.getDisplayId();
							participant.role = member.getRole()!=null?member.getRole().getId():"";
							participant.addSectionEidToList(sectionTitle);
							participant.uniqname = userId;
							participant.active=member.isActive();
						}
						
						conditionallyAddParticipantToMap(participantsMap, filterType, filterID, userId, participant, groupMembership);
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
	 * Add participant from provider-defined membership set
	 * @param participantsMap
	 * @param realm
	 * @param memberships
	 * @param sectionTitle
	 */
	public static void addParticipantsFromMemberships(Map participantsMap, AuthzGroup realm, Set memberships, String sectionTitle) {
		addParticipantsFromMemberships(participantsMap, realm, memberships, sectionTitle, null, null, null);
	}

	/**
	 * add participant from member list defined in realm
	 * @param participantsMap
	 * @param grants
	 * @param realmId
	 * @param filterID
	 * @param groupMembership
	 */
	private static void addParticipantsFromMembers(Map<String, Participant> participantsMap, Set grants, String realmId, String filterType, String filterID, Set<Member> groupMembership) {
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
					participant.regId = user.getDisplayId();
					participant.uniqname = userId;
					participant.role = g.getRole()!=null?g.getRole().getId():"";
					participant.removeable = true;
					participant.active = g.isActive();

					conditionallyAddParticipantToMap(participantsMap, filterType, filterID, userId, participant, groupMembership);
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

					conditionallyAddParticipantToMap(participantsMap, filterType, filterID, userId, participant, groupMembership);
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

	/**
	 * Set the membership for the given group from the given site
	 *
	 * @param siteID
	 *          the ID of the site which contains the group in question
	 * @param groupID
	 *          the ID of the group to add participants from
	 * @return
	 *          the set of members for the given group in the given site
	 */
	public static Set<Member> setGroupMembership(String siteID, String groupID) {
		Set<Member> groupMembership = new HashSet<>();
		try {

			// Get the site
			Site site = SiteService.getSite(siteID);
			if (site != null) {

				// Get the group from the site
				Group group = site.getGroup(groupID);
				if (group != null) {

					// Get the group's members
					Set members = group.getMembers();
					if (members != null) {
						groupMembership = members;
					}
				}
			}
		} catch (IdUnusedException ex) {
			log.warn("SiteParticipantHelper.addParticipantsFromGroupMembership: {} siteID={}", ex.getMessage(), siteID);
		}

		return groupMembership;
	}

	public static Collection<Participant> prepareParticipants(String siteID, List<String> providerCourseList) {
		return prepareParticipants(siteID, providerCourseList, null, null);
	}

	public static Collection<Participant> prepareParticipants(String siteId, List<String> providerCourseList, String filterType, String filterID) {
		boolean isSectionFilter = false;
		Map<String, Participant> participantsMap = new ConcurrentHashMap<>();
		Set<Member> groupMembership = new HashSet<>();

		// If the filter is for a group, get the membership for the group for comparison later
		if (SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP.equals(filterType)) {
			groupMembership = setGroupMembership(siteId, filterID);
		}

		// Determine if the filter is for a particular section
		else if (SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION.equals(filterType)) {
			isSectionFilter = true;
		}

		String realmId = SiteService.siteReference(siteId);
		try {
			AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
			realm.getProviderGroupId();

			if (providerCourseList != null) {
				for (String sectionEID : providerCourseList) {

					// Only retrieve memberships for this section if there is no section filter set,
					// OR if there is a section filter set AND the current section EID is that of the selected section filter
					if (!isSectionFilter || (isSectionFilter && sectionEID.equals(filterID))) {
						try {
							Section section = cms.getSection(sectionEID);
							if (section != null) {
								String sectionTitle = section.getTitle();

								// In case of Section eid
								EnrollmentSet enrollmentSet = section.getEnrollmentSet();
								addParticipantsFromEnrollmentSet(participantsMap, realm, sectionEID, enrollmentSet, sectionTitle, filterType, filterID, groupMembership);

								// Include official instructors of record for the enrollment set
								addOfficialInstructorOfRecord(participantsMap, realm, sectionTitle, enrollmentSet, filterType, filterID, groupMembership);

								// Add memberships
								Set<Membership> memberships = cms.getSectionMemberships(sectionEID);
								if (memberships != null && !memberships.isEmpty()) {
									addParticipantsFromMemberships(participantsMap, realm, memberships, sectionTitle, filterType, filterID, groupMembership);
								}

								// Now look for the not-included members from CourseOffering object
								CourseOffering courseOffering = cms.getCourseOffering(section.getCourseOfferingEid());
								if (courseOffering != null) {
									Set<Membership> coMemberships = cms.getCourseOfferingMemberships(section.getCourseOfferingEid());
									if (coMemberships != null && !coMemberships.isEmpty()) {
										addParticipantsFromMemberships(participantsMap, realm, coMemberships, courseOffering.getTitle(), filterType, filterID, groupMembership);
									}

									// Now look for the not-included members from CourseSet object
									Set<String> courseSetEIDs = courseOffering.getCourseSetEids();
									if (courseSetEIDs != null) {
										for (String courseSetEID : courseSetEIDs) {
											CourseSet courseSet = cms.getCourseSet(courseSetEID);
											if (courseSet != null) {
												Set<Membership> courseSetMemberships = cms.getCourseSetMemberships(courseSetEID);
												if (courseSetMemberships != null && !courseSetMemberships.isEmpty()) {
													addParticipantsFromMemberships(participantsMap, realm, courseSetMemberships, courseSet.getTitle(), filterType, filterID, groupMembership);
												}
											}
										}
									}
								}
							}
						} catch (IdNotFoundException e) {
							log.warn("SiteParticipantHelper.prepareParticipants: {} sectionId={}", e.getMessage(), sectionEID);
						}
					}
				}
			}

			// Only get non-provided users if there is no section filter set
			if (!isSectionFilter) {
				Set<Member> nonProvidedMembers = realm.getMembers();
				if (nonProvidedMembers != null && !nonProvidedMembers.isEmpty()) {
					addParticipantsFromMembers(participantsMap, nonProvidedMembers, realmId, filterType, filterID, groupMembership);
				}
			}
		} catch (GroupNotDefinedException ee) {
			log.warn("SiteParticipantHelper.prepareParticipants:  IdUnusedException {}", realmId);
		}

		return participantsMap.values();
	}

	private static void addOfficialInstructorOfRecord(Map<String, Participant> participantsMap, AuthzGroup realm, String sectionTitle, EnrollmentSet enrollmentSet,
														String filterType, String filterID, Set<Member> groupMembership) {
		
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
								participant.regId = user.getDisplayId();
								participant.removeable = false;
								participant.role = member.getRole()!=null?member.getRole().getId():"";
								participant.section = sectionTitle;
								participant.uniqname = userId;
							}

							conditionallyAddParticipantToMap(participantsMap, filterType, filterID, userId, participant, groupMembership);
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
