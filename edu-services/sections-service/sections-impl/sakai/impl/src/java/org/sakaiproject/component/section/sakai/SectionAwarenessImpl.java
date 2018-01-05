/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section.sakai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.section.sakai.facade.SakaiUtil;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * A sakai based implementation of the Section Awareness API, using the
 * grouping capability of the framework as the persistence mechanism.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class SectionAwarenessImpl implements SectionAwareness {

	// Sakai services
	protected SiteService siteService;
	protected SecurityService securityService;
	protected EntityManager entityManager;
	protected FunctionManager functionManager;
	protected UserDirectoryService userDirectoryService;
	protected CourseManagementService courseManagementService;

	/**
	 * Bean initialization (called by spring) registers authorization functions
	 * with the AuthzGroup system.
	 */
	public void init() {
    	if(log.isInfoEnabled()) log.info("init()");
		functionManager.registerFunction("section.role.student");
		functionManager.registerFunction("section.role.ta");
		functionManager.registerFunction("section.role.instructor");
	}
	
	public void destroy() {
    	if(log.isInfoEnabled()) log.info("destroy()");
	}
	
	/**
	 * @inheritDoc
	 */
	public List getSections(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
    	List<CourseSectionImpl> sectionList = new ArrayList<CourseSectionImpl>();
    	Collection<Group> sections;
    	try {
    		sections = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return sectionList;
    	}
    	for(Iterator<Group> iter = sections.iterator(); iter.hasNext();) {
    		Group group = (Group)iter.next();
    		sectionList.add(new CourseSectionImpl(group));
    	}
    	Collections.sort(sectionList);
    	return sectionList;
    }

	/**
	 * @inheritDoc
	 */
	public List<String>  getSectionCategories(String siteContext) {
		List<String> catCodes = new ArrayList<String>();
		for(Iterator<String>  iter = courseManagementService.getSectionCategories().iterator(); iter.hasNext();) {
			catCodes.add(iter.next());
		}
		return catCodes;
	}
	
	/**
	 * @inheritDoc
	 */
	public CourseSection getSection(final String sectionUuid) {
		Group group;
		group = siteService.findGroup(sectionUuid);
		if(group == null) {
			log.error("Unable to find section " + sectionUuid);
			return null;
		}
		return new CourseSectionImpl(group);
	}

	/**
	 * @inheritDoc
	 */
	public List getSiteMembersInRole(final String siteContext, final Role role) {
		if(role.isInstructor()) {
			return getSiteInstructors(siteContext);
		} else if(role.isTeachingAssistant()) {
			return getSiteTeachingAssistants(siteContext);
		} else if(role.isStudent()) {
			return getSiteEnrollments(siteContext);
		} else {
			log.error("Can not get site members in role " + role);
			return new ArrayList();
		}
	}
	
	private List getSiteInstructors(String siteContext) {
        Course course = getCourse(siteContext);
        if(course == null) {
        	return new ArrayList();
        }
        List sakaiMembers = securityService.unlockUsers(SectionAwareness.INSTRUCTOR_MARKER,
        		course.getUuid());
        List<InstructorRecordImpl> membersList = new ArrayList<InstructorRecordImpl>();
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.user.api.User sakaiUser = (org.sakaiproject.user.api.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
		if (user != null) {
    			InstructorRecordImpl record = new InstructorRecordImpl(course, user);
    			membersList.add(record);
		}
        }
        return membersList;
	}

	private List getSiteEnrollments(String siteContext) {
        Course course = getCourse(siteContext);
        if(course == null) {
        	log.error("Could not find course site " + siteContext);
        	return new ArrayList();
        }
        List sakaiMembers = securityService.unlockUsers(SectionAwareness.STUDENT_MARKER, course.getUuid());
        if(log.isDebugEnabled()) log.debug("Site students size = " + sakaiMembers.size());
        List<EnrollmentRecordImpl> membersList = new ArrayList<EnrollmentRecordImpl>();
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.user.api.User sakaiUser = (org.sakaiproject.user.api.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
		if (user != null) {
 	   		EnrollmentRecordImpl record = new EnrollmentRecordImpl(course, null, user);
    			membersList.add(record);
		}
        }
        return membersList;
	}

	private List getSiteTeachingAssistants(String siteContext) {
        Course course = getCourse(siteContext);
        if(course == null) {
        	return new ArrayList();
        }
        List sakaiMembers = securityService.unlockUsers(SectionAwareness.TA_MARKER, course.getUuid());
        if(log.isDebugEnabled()) log.debug("Site TAs size = " + sakaiMembers.size());

        List<TeachingAssistantRecordImpl> membersList = new ArrayList<TeachingAssistantRecordImpl>();
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.user.api.User sakaiUser = (org.sakaiproject.user.api.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
		if (user != null) {
    			TeachingAssistantRecordImpl record = new TeachingAssistantRecordImpl(course, user);
    			membersList.add(record);
		}
        }
        return membersList;
	}
	
	private Course getCourse(final String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting course for context " + siteContext);
    	Site site;
    	try {
    		site = siteService.getSite(siteContext);
    	} catch (IdUnusedException e) {
    		log.error("Could not find site with id = " + siteContext);
    		return null;
    	}
    	return new CourseImpl(site);
	}
	
	/**
	 * @inheritDoc
	 */
	public List findSiteMembersInRole(final String siteContext, final Role role, final String pattern) {
		List fullList = getSiteMembersInRole(siteContext, role);
		List<ParticipationRecord> filteredList = new ArrayList<ParticipationRecord>();
		for(Iterator iter = fullList.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			User user = record.getUser();
			if(user.getDisplayName().toLowerCase().startsWith(pattern.toLowerCase()) ||
			   user.getSortName().toLowerCase().startsWith(pattern.toLowerCase()) ||
			   user.getDisplayId().toLowerCase().startsWith(pattern.toLowerCase())) {
				filteredList.add(record);
			}
		}
		return filteredList;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isSiteMemberInRole(String siteContext, String userUid, Role role) {
		String authzRef = getSiteReference(siteContext);
		return isUserInRole(userUid, role, authzRef);
	}

	/**
	 * Checks whether a user is in a role in any learningContext (site or section)
	 * 
	 * @param userUid
	 * @param role
	 * @param authzRef
	 * @return
	 */
	private boolean isUserInRole(String userUid, Role role, String authzRef) {
		org.sakaiproject.user.api.User user;
		try {
			user = userDirectoryService.getUser(userUid);
		} catch (UserNotDefinedException ide) {
			log.error("Could not find user with id " + userUid);
			return false;
		}
		if(role.isNone()) {
			// Make sure that the user is in fact NOT in any role with a role marker
			if(securityService.unlock(user, SectionAwareness.INSTRUCTOR_MARKER, authzRef) ||
					securityService.unlock(user, SectionAwareness.STUDENT_MARKER, authzRef) ||
					securityService.unlock(user, SectionAwareness.TA_MARKER, authzRef)) {
				return false;
			} else {
				return true;
			}
		}
		return securityService.unlock(user, getLock(role), authzRef);
	}

	private String getSiteReference(String siteContext) {
        final Reference ref = entityManager.newReference(siteService.siteReference(siteContext));
        return ref.getReference();
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionMembers(final String sectionUuid) {
		Group group = siteService.findGroup(sectionUuid);
		CourseSection section = new CourseSectionImpl(group);
		Set taRoles = getSectionTaRoles(group);
		Set studentRoles = getSectionStudentRoles(group);
		Set members = group.getMembers();
		
		List<ParticipationRecord> sectionMembershipRecords = new ArrayList<ParticipationRecord>();
		for(Iterator iter = members.iterator(); iter.hasNext();) {
			Member member = (Member)iter.next();
			String roleString = member.getRole().getId();
			User user = SakaiUtil.getUserFromSakai(member.getUserId());
			if (user != null) {
				ParticipationRecord record = null;
				if(taRoles.contains(roleString)) {
					record = new TeachingAssistantRecordImpl(section, user);
				} else if(studentRoles.contains(roleString)) {
					record = new EnrollmentRecordImpl(section, null, user);
				}
				if(record != null) {
					sectionMembershipRecords.add(record);
				}
			}
		}
		return sectionMembershipRecords;		
	}

    /**
     * Gets the group-scoped role to use when adding a student to a group.
     * 
     * @param group The authzGroup
     * @return The role id, or null if there is not role with the student marker.
     */
	private Set getSectionStudentRoles(AuthzGroup group) {
    	return group.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
    }

    /**
     * Gets the group-scoped role to use when adding a TA to a group.
     * 
     * @param group The authzGroup
     * @return The role id, or null if there is not role with the TA marker.
     */
    private Set getSectionTaRoles(Group group) {
    	return group.getRolesIsAllowed(SectionAwareness.TA_MARKER);
    }

	/**
	 * @inheritDoc
	 */
	public List getSectionMembersInRole(final String sectionUuid, final Role role) {
		if(role.isTeachingAssistant()) {
			return getSectionTeachingAssistants(sectionUuid);
		} else if(role.isStudent()) {
			return getSectionEnrollments(sectionUuid);
		} else {
			log.error("Can't get section members in role " + role);
			return new ArrayList();
		}
	}

	private List getSectionEnrollments(String sectionUuid) {
		Group group = siteService.findGroup(sectionUuid);
		CourseSection section = getSection(sectionUuid);
		if(section == null) {
			return new ArrayList();
		}
		if(log.isDebugEnabled()) log.debug("Getting section enrollments in " + sectionUuid);
		Set studentRoles = getSectionStudentRoles(group);
		if(studentRoles == null || studentRoles.isEmpty()) {
			return new ArrayList();
		}
		Set<String> sakaiUserUids = new HashSet<String>();
		for(Iterator iter = studentRoles.iterator(); iter.hasNext();) {
			String role = (String)iter.next();
			sakaiUserUids.addAll(group.getUsersHasRole(role));
		}
		List sakaiUsers = userDirectoryService.getUsers(sakaiUserUids);

        List<EnrollmentRecord> membersList = new ArrayList<EnrollmentRecord>();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
        	User user = SakaiUtil.convertUser((org.sakaiproject.user.api.User) iter.next());
		if (user != null) {
    			EnrollmentRecordImpl record = new EnrollmentRecordImpl(section, null, user);
    			membersList.add(record);
		}
        }
        return membersList;
	}

	private List getSectionTeachingAssistants(String sectionUuid) {
		Group group = siteService.findGroup(sectionUuid);
		CourseSection section = getSection(sectionUuid);
		if(section == null) {
			return new ArrayList();
		}
		if(log.isDebugEnabled()) log.debug("Getting section enrollments in " + sectionUuid);
		Set taRoles = getSectionTaRoles(group);
		if(taRoles == null || taRoles.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("There is no role for TAs in this site... returning an empty list");
			return new ArrayList();
		}
		Set sakaiUserUids = new HashSet();
		for(Iterator iter = taRoles.iterator(); iter.hasNext();) {
			String role = (String)iter.next();
			sakaiUserUids.addAll(group.getUsersHasRole(role));
		}
		List sakaiUsers = userDirectoryService.getUsers(sakaiUserUids);

        List<TeachingAssistantRecordImpl> membersList = new ArrayList<TeachingAssistantRecordImpl>();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
        	User user = SakaiUtil.convertUser((org.sakaiproject.user.api.User) iter.next());
		if (user != null) {
    			TeachingAssistantRecordImpl record = new TeachingAssistantRecordImpl(section, user);
    			membersList.add(record);
		}
        }
        return membersList;
	}

	//////////////////////////////////////////////////
	
	
	/**
	 * @inheritDoc
	 */
	public boolean isSectionMemberInRole(final String sectionUuid, final String userUid, final Role role) {
		return isUserInRole(userUid, role, sectionUuid);
	}

	/**
	 * @inheritDoc
	 */
	public String getSectionName(final String sectionUuid) {
		CourseSection section = getSection(sectionUuid);
		return section.getTitle();
	}

	/**
	 * @inheritDoc
	 */
	public String getSectionCategory(final String sectionUuid) {
		CourseSection section = getSection(sectionUuid);
		return section.getCategory();
	}

	/**
	 * @inheritDoc
	 */
	public List getSectionsInCategory(final String siteContext, final String categoryId) {
    	if(log.isDebugEnabled()) log.debug("Getting " + categoryId +
    			" sections for context " + siteContext);
    	List<CourseSection> sectionList = new ArrayList<CourseSection>();
    	Collection groups;
    	try {
    		groups = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	for(Iterator iter = groups.iterator(); iter.hasNext();) {
    		Group group = (Group)iter.next();
    		if(categoryId.equals(group.getProperties().getProperty(CourseSectionImpl.CATEGORY))) {
        		sectionList.add(new CourseSectionImpl(group));
    		}
    	}
    	return sectionList;
	}

	/**
	 * @inheritDoc
	 */
	public String getCategoryName(String categoryId, Locale locale) {
		String description = courseManagementService.getSectionCategoryDescription(categoryId);
		if(description == null) {
			return categoryId;
		}
		return description;
	}

	/**
	 * @inheritDoc
	 */
	public List getUnassignedMembersInRole(final String siteContext, final Role role) {
		List siteMembers = getSiteMembersInRole(siteContext, role);

		// Get all userUids of all users in sections
		List<String> sectionedUserUids = new ArrayList<String>();
		List sections = getSections(siteContext);
		for(Iterator sectionIter = sections.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			List sectionUsers = securityService.unlockUsers(getLock(role), section.getUuid());
			for(Iterator userIter = sectionUsers.iterator(); userIter.hasNext();) {
				org.sakaiproject.user.api.User user = (org.sakaiproject.user.api.User)userIter.next();
				sectionedUserUids.add(user.getId());
			}
		}

		// Now generate the list of unsectioned enrollments by subtracting the two collections
		// Since the APIs return different kinds of objects, we need to iterate
		List<ParticipationRecord> unsectionedMembers = new ArrayList<ParticipationRecord>();
		for(Iterator iter = siteMembers.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			if(! sectionedUserUids.contains(record.getUser().getUserUid())) {
				unsectionedMembers.add(record);
			}
		}
		return unsectionedMembers;
	}
	
	private String getLock(Role role) {
		if(role.isInstructor()) {
			return SectionAwareness.INSTRUCTOR_MARKER;
		} else if(role.isTeachingAssistant()) {
			return SectionAwareness.TA_MARKER;
		} else if(role.isStudent()) {
			return SectionAwareness.STUDENT_MARKER;
		} else {
			return null;
		}
	}
	
	// Dependency injection

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

}
