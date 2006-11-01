/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
 **********************************************************************************/
package org.sakaiproject.component.section.sakai21;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseGroup;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.exception.RoleConfigurationException;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.component.section.facade.impl.sakai21.SakaiUtil;
import org.sakaiproject.component.section.sakai21.advisor.ExternalSectionAdvisor;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * A sakai-based implementation of the Section Management API, using the
 * new grouping capability of the framework.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionManagerImpl implements SectionManager {

	private static final Log log = LogFactory.getLog(SectionManagerImpl.class);
	
	protected ResourceBundle sectionCategoryBundle = ResourceBundle.getBundle(
			"org.sakaiproject.api.section.bundle.CourseSectionCategories");

	// Local facades and services
	protected ExternalSectionAdvisor externalSectionAdvisor;
	
    // Sakai services
    protected SiteService siteService;
    protected AuthzGroupService authzGroupService;
    protected SecurityService securityService;
    protected UserDirectoryService userDirectoryService;
    protected SessionManager sessionManager;
    protected EntityManager entityManager;
    protected EventTrackingService eventTrackingService;
    
	/**
	 * Filters out framework groups that do not have a category.  A section's
	 * category is determined by 
	 * 
	 */
	public List getSections(String siteContext) {
    	if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
    	List sectionList = new ArrayList();
    	Collection sections;
    	try {
    		sections = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	for(Iterator iter = sections.iterator(); iter.hasNext();) {
    		Group group = (Group)iter.next();
    		// Only use groups with a category defined.  If there is no category,
    		// it is not a section.
    		if(StringUtils.trimToNull(
    				group.getProperties().getProperty(CourseSectionImpl.CATEGORY)) != null) {
        		sectionList.add(new CourseSectionImpl(group));
    		}
    	}
    	return sectionList;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List getSectionsInCategory(String siteContext, String categoryId) {
    	if(log.isDebugEnabled()) log.debug("Getting " + categoryId + " sections for context " + siteContext);
    	List sectionList = new ArrayList();
    	Collection sections;
    	try {
    		sections = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	for(Iterator iter = sections.iterator(); iter.hasNext();) {
    		Group group = (Group)iter.next();
    		if(categoryId.equals(group.getProperties().getProperty(CourseSectionImpl.CATEGORY))) {
        		sectionList.add(new CourseSectionImpl(group));
    		}
    	}
    	return sectionList;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseSection getSection(String sectionUuid) {
		Group group;
		group = siteService.findGroup(sectionUuid);
		if(group == null) {
			log.error("Unable to find section " + sectionUuid);
			return null;
		}
		return new CourseSectionImpl(group);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSiteInstructors(String siteContext) {
        CourseImpl course = (CourseImpl)getCourse(siteContext);
        if(course == null) {
        	return new ArrayList();
        }
        Site site = course.getSite();
        Set sakaiUserIds = site.getUsersIsAllowed(SectionAwareness.INSTRUCTOR_MARKER);
        List sakaiMembers = userDirectoryService.getUsers(sakaiUserIds);
        List membersList = new ArrayList();
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.user.api.User sakaiUser = (org.sakaiproject.user.api.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
    		InstructorRecordImpl record = new InstructorRecordImpl(course, user);
    		membersList.add(record);
        }
        return membersList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSiteTeachingAssistants(String siteContext) {
        CourseImpl course = (CourseImpl)getCourse(siteContext);
        if(course == null) {
        	return new ArrayList();
        }
        Site site = course.getSite();
        Set sakaiUserIds = site.getUsersIsAllowed(SectionAwareness.TA_MARKER);
        List sakaiMembers = userDirectoryService.getUsers(sakaiUserIds);
        List membersList = new ArrayList();
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.user.api.User sakaiUser = (org.sakaiproject.user.api.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
    		TeachingAssistantRecordImpl record = new TeachingAssistantRecordImpl(course, user);
    		membersList.add(record);
        }
        return membersList;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List getSiteEnrollments(String siteContext) {
        CourseImpl course = (CourseImpl)getCourse(siteContext);
        if(course == null) {
        	return new ArrayList();
        }
        Site site = course.getSite();
        Set sakaiUserIds = site.getUsersIsAllowed(SectionAwareness.STUDENT_MARKER);
        List sakaiMembers = userDirectoryService.getUsers(sakaiUserIds);
        List membersList = new ArrayList();
        for(Iterator iter = sakaiMembers.iterator(); iter.hasNext();) {
        	org.sakaiproject.user.api.User sakaiUser = (org.sakaiproject.user.api.User)iter.next();
        	User user = SakaiUtil.convertUser(sakaiUser);
    		EnrollmentRecordImpl record = new EnrollmentRecordImpl(course, null, user);
    		membersList.add(record);
        }
        return membersList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSectionTeachingAssistants(String sectionUuid) {
		Group group = siteService.findGroup(sectionUuid);
		CourseSection section = getSection(sectionUuid);
		if(section == null) {
			return new ArrayList();
		}
		if(log.isDebugEnabled()) log.debug("Getting section enrollments in " + sectionUuid);
		String taRole;
		try {
			taRole = getSectionTaRole(group);
		} catch (RoleConfigurationException rce) {
			return new ArrayList();
		}
		Set sakaiUserUids = group.getUsersHasRole(taRole);
		List sakaiUsers = userDirectoryService.getUsers(sakaiUserUids);

        List membersList = new ArrayList();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
        	User user = SakaiUtil.convertUser((org.sakaiproject.user.api.User) iter.next());
    		TeachingAssistantRecordImpl record = new TeachingAssistantRecordImpl(section, user);
    		membersList.add(record);
        }
        return membersList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSectionEnrollments(String sectionUuid) {
		Group group = siteService.findGroup(sectionUuid);
		CourseSection section = getSection(sectionUuid);
		if(section == null) {
			return new ArrayList();
		}
		if(log.isDebugEnabled()) log.debug("Getting section enrollments in " + sectionUuid);
		String studentRole;
		try {
			studentRole = getSectionStudentRole(group);
		} catch (RoleConfigurationException rce) {
			log.error(rce);
			return new ArrayList();
		}
		 
		Set sakaiUserUids = group.getUsersHasRole(studentRole);
		List sakaiUsers = userDirectoryService.getUsers(sakaiUserUids);

        List membersList = new ArrayList();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
        	User user = SakaiUtil.convertUser((org.sakaiproject.user.api.User) iter.next());
    		EnrollmentRecordImpl record = new EnrollmentRecordImpl(section, null, user);
    		membersList.add(record);
        }
        return membersList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List findSiteEnrollments(String siteContext, String pattern) {
		List fullList = getSiteEnrollments(siteContext);
		List filteredList = new ArrayList();
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
	 * {@inheritDoc}
	 */
	public String getCategoryName(String categoryId, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle("org.sakaiproject.api.section.bundle.CourseSectionCategories", locale);
		String name;
		try {
			name = bundle.getString(categoryId);
		} catch(MissingResourceException mre) {
			if(log.isDebugEnabled()) log.debug("Could not find the name for category id = " + categoryId + " in locale " + locale.getDisplayName());
			name = null;
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSectionCategories(String siteContext) {
		Enumeration keys = sectionCategoryBundle.getKeys();
		List categoryIds = new ArrayList();
		while(keys.hasMoreElements()) {
			categoryIds.add(keys.nextElement());
		}
		Collections.sort(categoryIds);
		return categoryIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public Course getCourse(String siteContext) {
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
	 * {@inheritDoc}
	 */
	public SectionEnrollments getSectionEnrollmentsForStudents(String siteContext, Set studentUids) {
		if(studentUids == null || studentUids.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("Null or empty set of student Uids passed to getSectionEnrollmentsForStudents");
			return new SectionEnrollmentsImpl(new ArrayList());
		}
		// Get all sections
		List allSections = getSections(siteContext);
		
		// Get all student enrollments in each section, and combine them into a single collection
		List allSectionEnrollments = new ArrayList();
		for(Iterator sectionIter = allSections.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			List sectionEnrollments = getSectionEnrollments(section.getUuid());
			for(Iterator enrollmentIter = sectionEnrollments.iterator(); enrollmentIter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord)enrollmentIter.next();
				if(studentUids.contains(enr.getUser().getUserUid())) {
					allSectionEnrollments.add(enr);
				}
			}
		}
		
		return new SectionEnrollmentsImpl(allSectionEnrollments);
	}

	/**
	 * Posts an event to Sakai's event tracking service.  only posts events
	 * that modify some object.  Read-only events are not tracked.
	 * 
	 * @param message The message to post
	 * @param objectReference The object that was modified in the event
	 */
	private void postEvent(String message, String objectReference) {
		Event event = eventTrackingService.newEvent(message, objectReference, true);
		eventTrackingService.post(event);
	}
	
	/**
	 * {@inheritDoc}
	 */
    public EnrollmentRecord joinSection(String sectionUuid) throws RoleConfigurationException {
    	Group group = siteService.findGroup(sectionUuid);
    	
		// It's possible that this section has been deleted
		if(group == null) {
			log.error("Section " + sectionUuid + " has been deleted, so it can't be joined.");
			return null;
		}

    	String role = getSectionStudentRole(group);
		try {
			authzGroupService.joinGroup(sectionUuid, role);
			postEvent("section.student.join", sectionUuid);
		} catch (AuthzPermissionException e) {
			log.error("access denied while attempting to join authz group: ", e);
			return null;
		} catch (GroupNotDefinedException e) {
			log.error("can not find group while attempting to join authz group: ", e);
			return null;
		}
		
		// Return the membership record that the app understands
		String userUid = sessionManager.getCurrentSessionUserId();
		User user = SakaiUtil.getUserFromSakai(userUid);
		CourseSection section = getSection(sectionUuid);
		
		return new EnrollmentRecordImpl(section, null, user);
    }

    private String getSectionStudentRole(AuthzGroup group) throws RoleConfigurationException {
    	Set roleStrings = group.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
    	if(roleStrings.size() != 1) {
    		if(log.isDebugEnabled()) log.debug("Group " + group +
    			" must have one and only one role with permission " +
    			SectionAwareness.STUDENT_MARKER);
    		throw new RoleConfigurationException("Can't add a user to a section as a student, since there is no student-flagged role");
    	}
    	return (String)roleStrings.iterator().next();
    }

    private String getSectionTaRole(Group group) throws RoleConfigurationException {
    	Set roleStrings = group.getRolesIsAllowed(SectionAwareness.TA_MARKER);
    	if(roleStrings.size() != 1) {
    		if(log.isDebugEnabled()) log.debug("Group " + group +
    			" must have one and only one role with permission " +
    			SectionAwareness.TA_MARKER);
    		throw new RoleConfigurationException("Can't add a user to a section as a TA, since there is no TA-flagged role");
    	}
    	return (String)roleStrings.iterator().next();
    }

//    private String getSectionInstructorRole(AuthzGroup group) throws RoleConfigurationException {
//    	Set roleStrings = group.getRolesIsAllowed(SectionAwareness.INSTRUCTOR_MARKER);
//    	if(roleStrings.size() != 1) {
//    		if(log.isDebugEnabled()) log.debug("Group " + group +
//    			" must have one and only one role with permission " +
//    			SectionAwareness.INSTRUCTOR_MARKER);
//    		throw new RoleConfigurationException("Can't add a user to a section as an instructor, since there is no instructor-flagged role");
//    	}
//    	return (String)roleStrings.iterator().next();
//    }
    
	/**
	 * {@inheritDoc}
	 */
    public void switchSection(String newSectionUuid) throws RoleConfigurationException {
    	CourseSection newSection = getSection(newSectionUuid);

    	// It's possible that this section has been deleted
		if(newSection == null) {
			return;
		}

		String userUid = sessionManager.getCurrentSessionUserId();
    	
		// Remove any section membership for a section of the same category.
    	// We can not use dropEnrollmentFromCategory because security checks won't
    	// allow a student to update the authZ groups directly.
		List categorySections = getSectionsInCategory(newSection.getCourse().getSiteContext(), newSection.getCategory());
		
		boolean errorDroppingSection = false;
		
		String oldSectionUuid = null;
		for(Iterator iter = categorySections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			// Skip the current section
			if(section.getUuid().equals(newSectionUuid)) {
				continue;
			}
			if(this.isMember(userUid, section)) {
				oldSectionUuid = section.getUuid();
				try {
					authzGroupService.unjoinGroup(section.getUuid());
					oldSectionUuid = section.getUuid();
				} catch (GroupNotDefinedException e) {
					errorDroppingSection = true;
					log.error("There is not authzGroup with id " + section.getUuid());
				} catch (AuthzPermissionException e) {
					errorDroppingSection = true;
					log.error("Permission denied while " + userUid + " attempted to unjoin authzGroup " + section.getUuid());
				}
			}
		}

		// Only allow the user to join the new section if there were no errors dropping section(s)
		if(!errorDroppingSection) {
	    	// Join the new section
	    	joinSection(newSectionUuid);
	    	
	    	// Post the events
			postEvent("section.student.unjoin", oldSectionUuid);
			postEvent("section.student.switch", newSectionUuid);
		}

    }

    private boolean isMember(String userUid, CourseSection section) {
    	return authzGroupService.getUserRole(userUid, section.getUuid()) != null;
    }
    
	/**
	 * {@inheritDoc}
	 */
    public ParticipationRecord addSectionMembership(String userUid, Role role, String sectionUuid)
            throws MembershipException, RoleConfigurationException {
    	if(role.isStudent()) {
    		return addStudentToSection(userUid, sectionUuid);
    	} else if(role.isTeachingAssistant()) {
    		return addTaToSection(userUid, sectionUuid);
    	} else {
    		throw new RuntimeException("Adding a user to a section with role instructor or none is not supported");
    	}
    }
	
    private ParticipationRecord addTaToSection(String userUid, String sectionUuid) throws RoleConfigurationException {
		CourseSectionImpl section = (CourseSectionImpl)getSection(sectionUuid);

		// It's possible that this section has been deleted
		if(section == null) {
			return null;
		}

		Group group = section.getGroup();
		User user = SakaiUtil.getUserFromSakai(userUid);

		// Add the membership to the framework
    	String role = getSectionTaRole(group);
    	
    	group.addMember(userUid, role, true, false);
		
		try {
			siteService.saveGroupMembership(group.getContainingSite());
			postEvent("section.add.ta", sectionUuid);
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
			return null;
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
			return null;
		}

		// Return the enrollment record
		return new TeachingAssistantRecordImpl(section, user);
	}

	private EnrollmentRecord addStudentToSection(String userUid, String sectionUuid) throws RoleConfigurationException {
		User user = SakaiUtil.getUserFromSakai(userUid);

		CourseSectionImpl newSection = (CourseSectionImpl)getSection(sectionUuid);
		
		// It's possible that this section has been deleted
		if(newSection == null) {
			return null;
		}
		
		Group group = newSection.getGroup();

		String studentRole = getSectionStudentRole(group);

		// Remove any section membership for a section of the same category.
		dropEnrollmentFromCategory(userUid, newSection.getCourse().getSiteContext(), newSection.getCategory());

		// Add the membership to the framework
		if(studentRole == null) {
			throw new RoleConfigurationException("Can't add a student to a section, since there is no student-flgagged role");
		}
		group.addMember(userUid, studentRole, true, false);

		try {
			siteService.saveGroupMembership(group.getContainingSite());
			postEvent("section.add.student", sectionUuid);
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
			return null;
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
			return null;
		}
		
		// Return the enrollment record
		return new EnrollmentRecordImpl(newSection, null, user);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void setSectionMemberships(Set userUids, Role role, String sectionUuid) throws RoleConfigurationException {
		CourseSectionImpl section = (CourseSectionImpl)getSection(sectionUuid);

		// It's possible that this section has been deleted
		if(section == null) {
			return;
		}

		Group group = section.getGroup();
		String sakaiRoleString;
		if(role.isTeachingAssistant()) {
			sakaiRoleString = getSectionTaRole(group);
		} else if(role.isStudent()) {
			sakaiRoleString = getSectionStudentRole(group);
		} else {
			String str = "Only students and TAs can be added to sections";
			log.error(str);
			throw new RuntimeException(str);
		}
		
		if(sakaiRoleString == null) {
			throw new RoleConfigurationException("Can't set memberships for role " + role +
					".  No sakai role string can be found for this role.");
		}

		// Remove the current members in this role
		Set currentUserIds = group.getUsersHasRole(sakaiRoleString);
		for(Iterator iter = currentUserIds.iterator(); iter.hasNext();) {
			String userUid = (String)iter.next();
			group.removeMember(userUid);
		}
		
		// Add the new members (sure would be nice to have transactions here!)
		for(Iterator iter = userUids.iterator(); iter.hasNext();) {
			String userUid = (String)iter.next();
			group.addMember(userUid, sakaiRoleString, true, false);
		}

		try {
			siteService.saveGroupMembership(group.getContainingSite());
			postEvent("section.members.reset", sectionUuid);
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
		} catch (PermissionException e) {
			log.error("access denied while attempting to save authz group: ", e);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void dropSectionMembership(String userUid, String sectionUuid) {
		CourseSectionImpl section = (CourseSectionImpl)getSection(sectionUuid);
		Group group = section.getGroup();
		group.removeMember(userUid);
		try {
			siteService.saveGroupMembership(group.getContainingSite());
			postEvent("section.student.drop", sectionUuid);
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
		}
    }

	/**
	 * {@inheritDoc}
	 */
	public void dropEnrollmentFromCategory(String studentUid, String siteContext, String category) {
		if(log.isDebugEnabled()) log.debug("Dropping " + studentUid + " from all sections in category " + category + " in site " + siteContext);
		// Get the sections in this category
		Site site;
		try {
			site = siteService.getSite(siteContext);
		} catch (IdUnusedException ide) {
			log.error("Unable to find site " + siteContext);
			return;
		}
		Collection groups = site.getGroups();
		for(Iterator iter = groups.iterator(); iter.hasNext();) {
			// Drop the user from this section if they are enrolled
			Group group= (Group)iter.next();
			CourseSectionImpl section = new CourseSectionImpl(group);
			// Don't drop someone from a non-section groups
			if(section.getCategory() == null) {
				continue;
			}
			if(section.getCategory().equals(category)) {
				group.removeMember(studentUid);
			}
		}
		try {
			siteService.saveGroupMembership(site);
			postEvent("section.student.drop.category", site.getReference());
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
			return;
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTotalEnrollments(String learningContextUuid) {
		AuthzGroup authzGroup;
		try {
			authzGroup = authzGroupService.getAuthzGroup(learningContextUuid);
		} catch (GroupNotDefinedException e) {
			log.error("learning context " + learningContextUuid + " is neither a site nor a section");
			return 0;
		}
		String studentRole;
		try {
			studentRole = getSectionStudentRole(authzGroup);
		} catch (RoleConfigurationException rce) {
			log.warn("Can't get total enrollments, since there is no single student-flagged role in " + learningContextUuid);
			return 0;
		}
		Set users = authzGroup.getUsersHasRole(studentRole);
		return users.size();
	}

	/**
	 * {@inheritDoc}
	 */
    public CourseSection addSection(String courseUuid, String title,
    		String category, Integer maxEnrollments,
    		String location, Time startTime,
    		Time endTime, boolean monday,
    		boolean tuesday, boolean wednesday, boolean thursday,
    		boolean friday, boolean saturday, boolean sunday) {
    	
    	MeetingImpl meeting = new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
    	List meetings = new ArrayList();
    	meetings.add(meeting);

    	return addSection(courseUuid, title, category, maxEnrollments, meetings);
    }
    
	public CourseSection addSection(String courseUuid, String title, String category, Integer maxEnrollments, List meetings) {
    	Reference ref = entityManager.newReference(courseUuid);
    	
    	Site site;
    	try {
    		site = siteService.getSite(ref.getId());
    	} catch (IdUnusedException e) {
    		log.error("Unable to find site " + courseUuid);
    		return null;
    	}
    	Group group = site.addGroup();
    	
    	// Construct a CourseSection for this group
    	CourseSectionImpl courseSection = new CourseSectionImpl(group);
    	
    	// Set the fields of the course section
    	courseSection.setTitle(title);
    	courseSection.setCategory(category);
    	courseSection.setMaxEnrollments(maxEnrollments);
    	courseSection.setMeetings(meetings);
    	
    	// Decorate the framework group
    	courseSection.decorateGroup(group);

    	// Save the site, along with the new section
    	try {
        	siteService.save(site);
			postEvent("section.add", group.getReference());
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site for section " + group, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for section " + group, pe);
    	}
    	return new CourseSectionImpl(group);
	}

	/**
	 * {@inheritDoc}
	 */
    public void updateSection(String sectionUuid, String title,
    		Integer maxEnrollments, String location, Time startTime,
    		Time endTime, boolean monday, boolean tuesday,
    		boolean wednesday, boolean thursday, boolean friday,
    		boolean saturday, boolean sunday) {
    	// Create a list of meetings with a single meeting
    	List meetings = new ArrayList();
    	MeetingImpl meeting = new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
    	meetings.add(meeting);

    	// Update the section with a single meeting
    	updateSection(sectionUuid, title, maxEnrollments, meetings);
	}

	public void updateSection(String sectionUuid, String title, Integer maxEnrollments, List meetings) {
    	CourseSectionImpl section = (CourseSectionImpl)getSection(sectionUuid);
    	
    	if(section == null) {
    		throw new RuntimeException("Unable to find section " + sectionUuid);
    	}
    	
    	// Set the decorator's fields
    	section.setTitle(title);
    	section.setMaxEnrollments(maxEnrollments);
    	section.setMeetings(meetings);
    	
    	// Decorate the framework section
    	Group group = siteService.findGroup(sectionUuid);
    	section.decorateGroup(group);

    	// Save the site with its new section
    	try {
        	siteService.save(group.getContainingSite());
			postEvent("section.update", sectionUuid);
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site for section " + group, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for section " + group, pe);
    	}
	}

	/**
	 * {@inheritDoc}
	 */
    public void disbandSection(String sectionUuid) {
        if(log.isDebugEnabled()) log.debug("Disbanding section " + sectionUuid);

        Group group = siteService.findGroup(sectionUuid);
        
        // TODO Add token in UI to intercept double clicks in action buttons
        // SAK-3553 (Clicking remove button twice during section remove operation results in blank iframe.)
        if(group == null) {
        	log.warn("Unable to find group with uuid " + sectionUuid);
        	return;
        }

        Site site = group.getContainingSite();
        site.removeGroup(group);
        try {
			siteService.save(site);
			postEvent("section.disband", sectionUuid);
		} catch (IdUnusedException e) {
			log.error("Cound not disband section (can't find section): ",e);
		} catch (PermissionException e) {
			log.error("Cound not disband section (access denied): ",e);
		}
    }

	public boolean isExternallyManaged(String courseUuid) {
    	Reference ref = entityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
    	return Boolean.toString(true).equals(props.getProperty(CourseImpl.EXTERNALLY_MAINTAINED));
	}

	public void setExternallyManaged(String courseUuid, boolean externallyManaged) {
    	Reference ref = entityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
		
		// Keep track of whether the previous setting was external or not
		boolean previouslyExternallyManaged = false;
		try {
			previouslyExternallyManaged = props.getBooleanProperty(CourseImpl.EXTERNALLY_MAINTAINED);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("could not find the 'externally managed' state of site " + site.getId());
		}
		
		// Update the site
		props.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.toString(externallyManaged));
		if(externallyManaged) {
	    	// Also set the self join/switch to false
			props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.toString(false));
			props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.toString(false));
		}
    	
    	// Alert the ExternalSectionAdvisor if this site has changed from manual to automatic
    	if( ! previouslyExternallyManaged && externallyManaged) {
    		externalSectionAdvisor.replaceManualSectionsWithExternalSections(site);
    	}

    	try {
        	siteService.save(site);
        	if(log.isDebugEnabled()) log.debug("Saved site " + site.getTitle());
			postEvent("section.external=" + externallyManaged, site.getReference());
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site " + site, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for " + site, pe);
    	}
}

	/**
	 * {@inheritDoc}
	 */
    public boolean isSelfRegistrationAllowed(String courseUuid) {
    	Reference ref = entityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
    	return Boolean.toString(true).equals(props.getProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED));
    }

	/**
	 * {@inheritDoc}
	 */
    public void setSelfRegistrationAllowed(String courseUuid, boolean allowed) {
    	Reference ref = entityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
		props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, new Boolean(allowed).toString());
    	try {
        	siteService.save(site);
			postEvent("section.student.reg=" + allowed, site.getReference());
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site " + site, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for " + site, pe);
    	}
    }

	/**
	 * {@inheritDoc}
	 */
    public boolean isSelfSwitchingAllowed(String courseUuid) {
    	Reference ref = entityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
    	return Boolean.toString(true).equals(props.getProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED));
    }

	/**
	 * {@inheritDoc}
	 */
    public void setSelfSwitchingAllowed(String courseUuid, boolean allowed) {
    	Reference ref = entityManager.newReference(courseUuid);
    	String siteId = ref.getId();
    	Site site;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException e) {
    		throw new RuntimeException("Can not find site " + courseUuid, e);
    	}
		ResourceProperties props = site.getProperties();
		props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, new Boolean(allowed).toString());
		
    	try {
        	siteService.save(site);
			postEvent("section.student.switch=" + allowed, site.getReference());
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site " + site, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for " + site, pe);
    	}
    }
    
	/**
	 * {@inheritDoc}
	 */
	public List getUnsectionedEnrollments(String courseUuid, String category) {
		Reference siteRef = entityManager.newReference(courseUuid);
		String siteId = siteRef.getId();

		// Get all of the sections and userUids of enrolled students
		List siteEnrollments = getSiteEnrollments(siteId);
		
		// Get all userUids of students enrolled in sections of this category
		List sectionedStudentUids = new ArrayList();
		List categorySections = getSectionsInCategory(siteId, category);
		for(Iterator sectionIter = categorySections.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			List sectionUsers = getSectionEnrollments(section.getUuid());
			
			if(log.isDebugEnabled()) log.debug("There are " + sectionUsers.size() +
					" students in section " + section.getUuid());

			for(Iterator userIter = sectionUsers.iterator(); userIter.hasNext();) {
				ParticipationRecord record = (ParticipationRecord)userIter.next();
				sectionedStudentUids.add(record.getUser().getUserUid());
			}
		}

		// Now generate the list of unsectioned enrollments by subtracting the two collections
		// Since the APIs return different kinds of objects, we need to iterate
		List unsectionedEnrollments = new ArrayList();
		for(Iterator iter = siteEnrollments.iterator(); iter.hasNext();) {
			EnrollmentRecord record = (EnrollmentRecord)iter.next();
			if(! sectionedStudentUids.contains(record.getUser().getUserUid())) {
				unsectionedEnrollments.add(record);
			}
		}
		return unsectionedEnrollments;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getSectionEnrollments(String userUid, String courseUuid) {
		// Get the user
		org.sakaiproject.user.api.User sakaiUser;
		try {
			sakaiUser = userDirectoryService.getUser(userUid); 
		} catch (UserNotDefinedException ide) {
			log.error("Can not find user with id " + userUid);
			return new HashSet();
		}
		User sectionUser = SakaiUtil.convertUser(sakaiUser);
		
		// Get all of the sections
		Reference siteRef = entityManager.newReference(courseUuid);
		String siteId = siteRef.getId();
		List sections = getSections(siteId);
		
		// Generate a set of sections for which this user is enrolled
		Set sectionEnrollments = new HashSet();
		for(Iterator sectionIter = sections.iterator(); sectionIter.hasNext();) {
			CourseSectionImpl section = (CourseSectionImpl)sectionIter.next();
			Group group = section.getGroup();
			Member member = group.getMember(userUid);
			if(member == null) {
				continue;
			}
			if(member.getRole().isAllowed(SectionAwareness.STUDENT_MARKER)) {
				sectionEnrollments.add(new EnrollmentRecordImpl(section, null, sectionUser));
			}
		}
		return sectionEnrollments;
	}

	/**
	 * {@inheritDoc}
	 */
	public User getSiteEnrollment(String siteContext, String studentUid) {
		return SakaiUtil.getUserFromSakai(studentUid);
	}
	

	// Groups

	public CourseGroup addCourseGroup(String courseUuid, String title, String description) {
    	Reference ref = entityManager.newReference(courseUuid);
    	
    	Site site;
    	try {
    		site = siteService.getSite(ref.getId());
    	} catch (IdUnusedException e) {
    		log.error("Unable to find site " + courseUuid);
    		return null;
    	}
    	Group group = site.addGroup();
    	group.setTitle(title);
    	group.setDescription(description);

    	// Save the site, along with the new group
    	try {
        	siteService.save(site);
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site for group " + group, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for group " + group, pe);
    	}
    	return new CourseGroupImpl(group);
	}

	public void disbandCourseGroup(String courseGroupUuid) {
		Group group = siteService.findGroup(courseGroupUuid);
		if(group == null) {
			log.warn("can not disband a non-existent group: " + courseGroupUuid);
			return;
		}
		Site site = group.getContainingSite();
    	site.removeGroup(group);
    	// Save the site and its newly removed group
    	try {
        	siteService.save(site);
    	} catch (IdUnusedException ide) {
    		log.error("Error saving site... could not find site for group " + group, ide);
    	} catch (PermissionException pe) {
    		log.error("Error saving site... permission denied for group " + group, pe);
    	}
	}

	public CourseGroup getCourseGroup(String courseGroupUuid) {
		return new CourseGroupImpl(siteService.findGroup(courseGroupUuid));
	}

	public List getCourseGroups(String siteContext) {
		Collection groups = null;
    	try {
    		groups = siteService.getSite(siteContext).getGroups();
    	} catch (IdUnusedException e) {
    		log.error("No site with id = " + siteContext);
    		return new ArrayList();
    	}
    	List courseGroups = new ArrayList(groups.size());
    	for(Iterator iter = groups.iterator(); iter.hasNext();) {
    		Group group = (Group)iter.next();
    		// We only want the groups with no category.  If a category exists, this is a section.
    		String category = group.getProperties().getProperty(CourseSectionImpl.CATEGORY);
    		if(category ==  null) {
        		courseGroups.add(new CourseGroupImpl(group));
    		}
    	}
    	Collections.sort(courseGroups);
    	return courseGroups;
	}

	public Set getUsersInGroup(String courseGroupUuid) {
		Group group = siteService.findGroup(courseGroupUuid);
		Set members = group.getMembers();
		Set userUids = new HashSet();
		for(Iterator iter = members.iterator(); iter.hasNext();) {
			Member member = (Member)iter.next();
			userUids.add(member.getUserId());
		}
		return userUids;
	}

//	private Role getRole(Member member) {
//		org.sakaiproject.authz.api.Role sakaiRole = member.getRole();
//		if(sakaiRole.isAllowed(SectionAwareness.STUDENT_MARKER)) {
//			return Role.STUDENT;
//		}
//		if(sakaiRole.isAllowed(SectionAwareness.TA_MARKER)) {
//			return Role.TA;
//		}
//		if(sakaiRole.isAllowed(SectionAwareness.INSTRUCTOR_MARKER)) {
//			return Role.INSTRUCTOR;
//		}
//		return Role.NONE;
//	}

	public void setUsersInGroup(String courseGroupUuid, Set userUids) {
		Group group = siteService.findGroup(courseGroupUuid);

		// Remove the existing members
		group.removeMembers();
		
		// Add the new set of members
		for(Iterator iter = userUids.iterator(); iter.hasNext();) {
			String userUid = (String)iter.next();
			// FIXME What role should we use when adding a user to a group?
			String sakaiRole = null;
			group.addMember(userUid, sakaiRole, true, false);
		}
		
		// Save the site (and hence, the group)
		try {
			siteService.saveGroupMembership(group.getContainingSite());
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
		}
	}
	
//	private String getSakaiGroupRole(Group group, ParticipationRecord record) {
//		try {
//			Role role = record.getRole();
//			if(role.isInstructor()) {
//				return getSectionInstructorRole(group);
//			}
//			if(role.isTeachingAssistant()) {
//				return getSectionTaRole(group);
//			}
//			if(role.isStudent()) {
//				return getSectionStudentRole(group);
//			}
//		} catch (RoleConfigurationException rce) {
//			// TODO Why is this a checked exception?
//			throw new RuntimeException(rce);
//		}
//		return null;
//	}

	public void updateCourseGroup(CourseGroup courseGroup) {
		Group group = siteService.findGroup(courseGroup.getUuid());
		group.setDescription(courseGroup.getDescription());
		group.setTitle(courseGroup.getTitle());

		// Save the site (and hence, the group)
		try {
			siteService.saveGroupMembership(group.getContainingSite());
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
		}
	}
	
    // Dependency injection

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
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

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setExternalSectionAdvisor(ExternalSectionAdvisor externalSectionAdvisor) {
		this.externalSectionAdvisor = externalSectionAdvisor;
	}
}
