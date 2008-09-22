package org.sakaiproject.site.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.util.StringUtil;

public class SiteParticipantHelper {
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SiteParticipantHelper.class);
	
	private static String NULL_STRING = "";
	
	private static org.sakaiproject.authz.api.GroupProvider groupProvider = (org.sakaiproject.authz.api.GroupProvider) ComponentManager
	.get(org.sakaiproject.authz.api.GroupProvider.class);
	
	private static org.sakaiproject.authz.api.AuthzGroupService authzGroupService = (org.sakaiproject.authz.api.AuthzGroupService) ComponentManager
	.get(org.sakaiproject.authz.api.AuthzGroupService.class);
	
	private static org.sakaiproject.coursemanagement.api.CourseManagementService cms = (org.sakaiproject.coursemanagement.api.CourseManagementService) ComponentManager
	.get(org.sakaiproject.coursemanagement.api.CourseManagementService.class);
	
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
				Map<String, User> eidToUserMap = getEidUserMapFromEnrollments(enrollments);
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
								AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realm.getId());
								AuthzGroupService.save(realmEdit);
								// refetch updated realm
								realm = AuthzGroupService.getAuthzGroup(realm.getId());
								member = realm.getMember(userId);
						    } catch (Exception exc) {
						    	M_log.warn("SiteParticipantHelper.addParticipantsFromEnrollment " + exc.getMessage());
						    }
						}
						
						if (member != null && member.isProvided())
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
								participant.credits = participant.credits.concat(", <br />" + e.getCredits());
							}
							else
							{
								participant = new Participant();
								participant.credits = e.getCredits();
								participant.name = user.getSortName();
								participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
								participant.regId = "";
								participant.removeable = false;
								participant.role = member.getRole()!=null?member.getRole().getId():"";
								participant.addSectionEidToList(sectionTitle);
								participant.uniqname = userId;
								participant.active = member.isActive();
							}
							participantsMap.put(userId, participant);
							}
							catch (Exception ee)
							{
								M_log.warn("SiteParticipantHelper.addParticipantsFromEnrollmentSet: " + ee.getMessage() + " user id = " + userId, ee);
							}
						}
					} catch (UserNotDefinedException exception) {
						// deal with missing user quietly without throwing a
						// warning message
						M_log.warn("SiteParticipantHelper.addParticipantsFromEnrollmentSet: " + exception.getMessage() + " user id = " + e.getUserId(), exception);
					}
				}
			}
		}
	}

	/**
	 * Collect all student user data in one call.
	 * 
	 * @param enrollments
	 * @return
	 */
	public static Map<String, User> getEidUserMapFromEnrollments(Collection<Enrollment> enrollments) {
		Set<String> enrollmentEids = new HashSet<String>();
		for (Enrollment enrollment : enrollments) {
			enrollmentEids.add(enrollment.getUserId());
		}
		Map<String, User> eidToUserMap = new HashMap<String, User>();
		List<User> enrollmentUsers = UserDirectoryService.getUsersByEids(enrollmentEids);
		for (User user : enrollmentUsers) {
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
			for (Iterator mIterator = memberships.iterator();mIterator.hasNext();)
			{
				Membership m = (Membership) mIterator.next();
				try 
				{
					User user = UserDirectoryService.getUserByEid(m.getUserId());
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
							AuthzGroup realmEdit = AuthzGroupService.getAuthzGroup(realm.getId());
							AuthzGroupService.save(realmEdit);
							// refetch updated realm
							realm = AuthzGroupService.getAuthzGroup(realm.getId());
							member = realm.getMember(userId);
					    } catch (Exception exc) {
					    	M_log.warn("SiteParticipantHelper:addParticipantsFromMembership " + exc.getMessage());
					    }
					}
					
					if (member != null && member.isProvided())
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
							participant.providerRole = member.getRole()!=null?member.getRole().getId():"";
							participant.regId = "";
							participant.removeable = false;
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
					M_log.warn("SiteParticipantHelper.addParticipantsFromMemberships: user id = " + m.getUserId(), exception);
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
			AuthzGroup realm = AuthzGroupService.getAuthzGroup(realmId);
			rv = realm.getProviderGroupId();
		} catch (GroupNotDefinedException e) {
			M_log.warn("SiteParticipantHelper.getExternalRealmId: site realm not found", e);
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
						// in case of Section eid
						EnrollmentSet enrollmentSet = section.getEnrollmentSet();
						SiteParticipantHelper.addParticipantsFromEnrollmentSet(participantsMap, realm, providerCourseEid, enrollmentSet, section.getTitle());
						// add memberships
						Set memberships = cms.getSectionMemberships(providerCourseEid);
						if (memberships != null && memberships.size() > 0)
						{
							SiteParticipantHelper.addParticipantsFromMemberships(participantsMap, realm, memberships, section.getTitle());
						}
						
						// now look or the not-included member from CourseOffering object
						CourseOffering co = cms.getCourseOffering(section.getCourseOfferingEid());
						if (co != null)
						{
							
							Set<Membership> coMemberships = cms.getCourseOfferingMemberships(section.getCourseOfferingEid());
							if (coMemberships != null && coMemberships.size() > 0)
							{
								SiteParticipantHelper.addParticipantsFromMemberships(participantsMap, realm, coMemberships, co.getTitle());
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
											SiteParticipantHelper.addParticipantsFromMemberships(participantsMap, realm, cSetMemberships, cSet.getTitle());
										}
									}
								}
							}
						}
					}
					
				}
				catch (IdNotFoundException e)
				{
					M_log.warn("SiteParticipantHelper.prepareParticipants: "+ e.getMessage() + " sectionId=" + providerCourseEid, e);
				}
			}
			
			// now for those not provided users
			Set<Member> grants = realm.getMembers();
			for (Iterator<Member> i = grants.iterator(); i.hasNext();) {
				Member g = (Member) i.next();
				try {
					User user = UserDirectoryService.getUserByEid(g.getUserEid());
					String userId = user.getId();
					if (!participantsMap.containsKey(userId))
					{
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
					// deal with missing user quietly without throwing a
					// warning message
					M_log.warn("SiteParticipantHelper.prepareParticipants: "+ e.getMessage(), e);
				}
			}

		} catch (GroupNotDefinedException ee) {
			M_log.warn("SiteParticipantHelper.prepareParticipants:  IdUnusedException " + realmId, ee);
		}
		return participantsMap.values();
	}

}
