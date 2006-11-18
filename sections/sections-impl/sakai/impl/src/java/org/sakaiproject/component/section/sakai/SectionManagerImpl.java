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
package org.sakaiproject.component.section.sakai;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseGroup;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.SectionEnrollments;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.MembershipException;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.component.section.sakai.facade.SakaiUtil;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.SectionCategory;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
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
import org.sakaiproject.site.api.SiteAdvisor;
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
public class SectionManagerImpl implements SectionManager, SiteAdvisor {

	private static final Log log = LogFactory.getLog(SectionManagerImpl.class);
	
    // Sakai services
    protected SiteService siteService;
    protected AuthzGroupService authzGroupService;
    protected SecurityService securityService;
    protected UserDirectoryService userDirectoryService;
    protected SessionManager sessionManager;
    protected EntityManager entityManager;
    protected EventTrackingService eventTrackingService;
	protected CourseManagementService courseManagementService;

    // Configuration setting
    protected String config;
    
    /**
     * Initialization called once all dependencies are set.
     */
    public void init() {
    	if(log.isInfoEnabled()) log.info("init()");
		siteService.addSiteAdvisor(this);
    }

    /**
     * Cleans up any resources in use before destroying this service.
     */
    public void destroy() {
    	if(log.isInfoEnabled()) log.info("destroy()");
    	siteService.removeSiteAdvisor(this);
    }
    
    // SiteAdvisor methods

    /**
	 * {@inheritDoc}
	 */
    public void update(Site site) {
    	
    	log.info("###################################");
    	log.info("Updating Site with provider ID=" + site.getProviderGroupId());
    	log.info("###################################");
    	
    	// NOTE: This code will be called any time a site is saved (including site creation).
    	// Be very careful...

		// If we're on a non-course site, do nothing
		if( ! "course".equalsIgnoreCase(site.getType())) {
			if(log.isDebugEnabled()) log.debug("SiteAdvisor " + this.getClass().getCanonicalName() + " ignoring site " + site.getTitle() + ", which is not a course site");
			return;
		}
		
		// Get our app config and the site properties
		ExternalIntegrationConfig appConfig = getConfiguration(null);
		ResourceProperties siteProps = site.getProperties();

		// If we're configured to be mandatory auto or mandatory manual, handle those conditions and return
		if(handlingMandatoryConfigs(appConfig, siteProps)) {
			if(log.isDebugEnabled()) log.debug(this.getClass().getCanonicalName() + " finished decorating site " + site.getTitle() + " for " + appConfig);
			return;
		}
		
		// Set the defaults for non-mandatory sites
		setSiteDefaults(site, appConfig, siteProps);
		
		// If this site is manually managed, we do nothing
		if("false".equals(siteProps.getProperty(CourseImpl.EXTERNALLY_MAINTAINED))) {
			if(log.isDebugEnabled()) log.debug("SiteAdvisor " + this.getClass().getCanonicalName() + " ignoring sections in site " + site.getTitle() + ".  The site is internally managed.");
			return;
		}
		
		// This is an externally managed site.  Update the sections from CM.
		replaceSectionsWithExternalSections(site);
	}

    private boolean handlingMandatoryConfigs(ExternalIntegrationConfig appConfig, ResourceProperties siteProps) {
		switch(appConfig) {
		// If we're configured to treat all sites as manual, set the site to manual control
		case MANUAL_MANDATORY:
			siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.FALSE.toString());
			return true;

		// If we're configured to treat all sites as automatic, set the site to external control
		case AUTOMATIC_MANDATORY:
			siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.TRUE.toString());
			siteProps.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.FALSE.toString());
			siteProps.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.FALSE.toString());
			return true;

		default:
			return false;
		}
    }

    private void setSiteDefaults(Site site, ExternalIntegrationConfig appConfig, ResourceProperties siteProps) {
		// If the site doesn't have a property set for "Externally Maintained", it's either a new site or one
		// that was created before this SiteAdvisor was registered.
		if(siteProps.getProperty(CourseImpl.EXTERNALLY_MAINTAINED) == null) {
			// Set this property to external if the app config is AUTOMATIC, else make it internally managed
			// FIXME -- This might have unforseen consequences...
			if(log.isDebugEnabled()) log.debug("Site '" + site.getTitle() + "' has no EXTERNALLY_MAINTAINED flag.");
			if(appConfig == ExternalIntegrationConfig.AUTOMATIC) {
				siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.TRUE.toString());
				siteProps.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.FALSE.toString());
				siteProps.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.FALSE.toString());
			} else {
				siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.FALSE.toString());
				return;
			}
		}
	}

    /**
     * Replaces all existing sections (whether internally or externally defined) with
     * sections that are externally defined.
     * 
     * @param site
     * @param appConfig
     */
	private void replaceSectionsWithExternalSections(Site site) {
		if(log.isInfoEnabled()) log.info("Replacing sections with externally managed sections in site " + site.getId());

		// Get the existing groups from the site, and add them to a new collection so we can remove elements from the original collection
		Collection<Group> groups = new HashSet<Group>(site.getGroups());
		
		// Remove all sections (this will be done by the SiteService impl anyhow, so there's no performance loss in doing a mass remove/readd of sections)
		for(Iterator<Group> iter = groups.iterator(); iter.hasNext();) {
			Group group = iter.next();
			if(group.getProperties().getProperty(CourseSectionImpl.CATEGORY) != null) {
				if(log.isDebugEnabled()) log.debug("Removing section " + group.getReference());
				site.removeGroup(group);
			}
		}
		
		// Dereference the groups collection
		groups = null;
		
		// Get the provider Ids associated with this site
		Set providerIds = authzGroupService.getProviderIds(site.getReference());

		// TODO Does the app configuration matter here?  It shouldn't
		if(providerIds.size() <= 1) {
			return;
		}

		// Add new groups (decorated as sections) based on the site's providerIds
		for(Iterator iter = providerIds.iterator(); iter.hasNext();) {
			String providerId = (String)iter.next();
			addExternalCourseSectionToSite(site, providerId);
		}
	}
	
	/**
	 * Adds an externally managed CourseSection (a decorated group) to a site.  The CourseSection is
	 * constructed by finding the official section from CM and converting it to a CourseSection.
	 * 
	 * @param site The site in which we are adding a CourseSection 
	 * @param sectionId The Enterprise ID of the section to add.
	 * 
	 * @return The CourseSection that was added to the site
	 */
	private CourseSection addExternalCourseSectionToSite(Site site, String sectionEid) {
		if(log.isDebugEnabled()) log.debug("Adding section " + sectionEid + " to site " + site.getId());

		// Create a new sakai section (group) for this providerId
		Section officialSection = null;
		try {
			officialSection = courseManagementService.getSection(sectionEid);
		} catch (IdNotFoundException ide) {
			log.error("Site " + site.getId() + " has a provider id, " + sectionEid + ", that has no matching section in CM.");
			return null;
		}
		Group group = site.addGroup();
		group.setProviderGroupId(sectionEid);
		CourseSectionImpl section = new CourseSectionImpl(group);
		
		// The "decorating" metadata isn't yet part of the section, so set it manually
		section.setTitle(officialSection.getTitle());
		section.setCategory(officialSection.getCategory());
		section.setMaxEnrollments(officialSection.getMaxSize());
		Set officialMeetings = officialSection.getMeetings();
		if(officialMeetings != null) {
			List<MeetingImpl> meetings = new ArrayList<MeetingImpl>();
			for(Iterator meetingIter = officialMeetings.iterator(); meetingIter.hasNext();) {
				org.sakaiproject.coursemanagement.api.Meeting officialMeeting = (org.sakaiproject.coursemanagement.api.Meeting)meetingIter.next();
				MeetingImpl meeting = new MeetingImpl(officialMeeting.getLocation(),
						officialMeeting.getStartTime(), officialMeeting.getFinishTime(),
						officialMeeting.isMonday(), officialMeeting.isTuesday(), officialMeeting.isWednesday(),
						officialMeeting.isThursday(), officialMeeting.isFriday(), officialMeeting.isSaturday(), officialMeeting.isSunday());
				meetings.add(meeting);
			}
			section.setMeetings(meetings);
		}
		// Ensure that the group is decorated properly, so the group properties are
		// persisted with the correct section metadata
		section.decorateGroup(group);
	
		// Ensure that the group has the correct provider ID
		group.setProviderGroupId(sectionEid);
		
		return section;
	}
	
	// SectionManager Methods
	
	/**
	 * Filters out framework groups that do not have a category.  A section's
	 * category is determined by 
	 * 
	 */
	public List getSections(String siteContext) {
		if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
		List<CourseSection> sectionList = new ArrayList<CourseSection>();
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
		List<CourseSection> sectionList = new ArrayList<CourseSection>();
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
		List<ParticipationRecord> membersList = new ArrayList<ParticipationRecord>();
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
		List<ParticipationRecord> membersList = new ArrayList<ParticipationRecord>();
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
		List<ParticipationRecord> membersList = new ArrayList<ParticipationRecord>();
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

		List<ParticipationRecord> membersList = new ArrayList<ParticipationRecord>();
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

		List<ParticipationRecord> membersList = new ArrayList<ParticipationRecord>();
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
	 * {@inheritDoc}
	 */
	public String getCategoryName(String categoryId, Locale locale) {
		return courseManagementService.getSectionCategoryDescription(categoryId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getSectionCategories(String siteContext) {
		List categoryObjects =  courseManagementService.getSectionCategories();
		List<String> categoryIds = new ArrayList<String>();
		for(Iterator iter = categoryObjects.iterator(); iter.hasNext();) {
			SectionCategory cat = (SectionCategory)iter.next();
			categoryIds.add(cat.getCategoryCode());
		}
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
		List<ParticipationRecord> allSectionEnrollments = new ArrayList<ParticipationRecord>();
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
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(getSection(sectionUuid).getCourse().getUuid());

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
	
	/**
	 * {@inheritDoc}
	 */
	public void switchSection(String newSectionUuid) throws RoleConfigurationException {
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(getSection(newSectionUuid).getCourse().getUuid());

		CourseSection newSection = getSection(newSectionUuid);

		// It's possible that this section has been deleted
		if(newSection == null) {
			return;
		}
		
		// Disallow if we're in an externally managed site
		if(isExternallyManaged(newSection.getCourse().getUuid())) {
			log.warn("Can not switch sections in an externally managed site");
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
			// Disallow if we're in an externally managed site
			ensureInternallyManaged(getSection(sectionUuid).getCourse().getUuid());
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
		if(role.isStudent()) {
			// Disallow if we're in an externally managed site
			ensureInternallyManaged(getSection(sectionUuid).getCourse().getUuid());
		}
		
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
		
		// If we're trying to drop a student, ensure that we're not automatically managed
		Member member = group.getMember(userUid);
		String studentRole = null;
		try {
			studentRole = getSectionStudentRole(group);
		} catch (RoleConfigurationException e1) {
			log.error("Can't find the student role for section" + sectionUuid);
		}
		if(studentRole != null && studentRole.equals(member.getRole())) {
			// We can not drop students from a section in externally managed sites
			ensureInternallyManaged(section.getCourse().getUuid());
		}
		
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
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(getCourse(siteContext).getUuid());
		
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
		
		Meeting meeting = new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
		List<Meeting> meetings = new ArrayList<Meeting>();
		meetings.add(meeting);

		return addSection(courseUuid, title, category, maxEnrollments, meetings);
	}
	
	/**
	 * Throws a SecurityException if an attempt is made to modify a section or its
	 * student memberships when the site is configured for external management.
	 */
	private void ensureInternallyManaged(String courseUuid) {
		if(isExternallyManaged(courseUuid)) {
			throw new SecurityException("Can not make changes to sections or student memberships in site " + courseUuid + ".  It is externally managed.");
		}
	}

	public CourseSection addSection(String courseUuid, String title, String category, Integer maxEnrollments, List meetings) {
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(courseUuid);

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
		List<Meeting> meetings = new ArrayList<Meeting>();
		MeetingImpl meeting = new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
		meetings.add(meeting);

		// Update the section with a single meeting
		updateSection(sectionUuid, title, maxEnrollments, meetings);
	}

	public void updateSection(String sectionUuid, String title, Integer maxEnrollments, List meetings) {
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(getSection(sectionUuid).getCourse().getUuid());

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
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(getSection(sectionUuid).getCourse().getUuid());
		
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
		// Disallow if the service is configured to be mandatory
		ExternalIntegrationConfig appConfig = getConfiguration(null);
		if(appConfig == ExternalIntegrationConfig.AUTOMATIC_MANDATORY ||
				appConfig == ExternalIntegrationConfig.MANUAL_MANDATORY) {
			throw new SecurityException("Can not change the external management of a site, since this service is configured to be " + appConfig);
		}

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
		
		// We're changing from manual (internally managed) to automatic (externally managed),
		// so we need to replace the internally defined sections with externally defined ones.
		
		if( ! previouslyExternallyManaged && externallyManaged) {
			replaceSectionsWithExternalSections(site);
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
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(courseUuid);

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
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(courseUuid);

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
		List<String> sectionedStudentUids = new ArrayList<String>();
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
		List<EnrollmentRecord> unsectionedEnrollments = new ArrayList<EnrollmentRecord>();
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
		Set<EnrollmentRecord> sectionEnrollments = new HashSet<EnrollmentRecord>();
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
		List<CourseGroupImpl> courseGroups = new ArrayList<CourseGroupImpl>(groups.size());
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
		Set<String> userUids = new HashSet<String>();
		for(Iterator iter = members.iterator(); iter.hasNext();) {
			Member member = (Member)iter.next();
			userUids.add(member.getUserId());
		}
		return userUids;
	}

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
	
	public ExternalIntegrationConfig getConfiguration(Object obj) {
		if(ExternalIntegrationConfig.AUTOMATIC_MANDATORY.toString().equals(config)) {
			return ExternalIntegrationConfig.AUTOMATIC_MANDATORY;
		} else if(ExternalIntegrationConfig.MANUAL_MANDATORY.toString().equals(config)) {
			return ExternalIntegrationConfig.MANUAL_MANDATORY;
		} else if(ExternalIntegrationConfig.AUTOMATIC.toString().equals(config)){
			return ExternalIntegrationConfig.AUTOMATIC;
		} else {
			// our default configuration
			return ExternalIntegrationConfig.MANUAL;
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

	public void setConfig(String config) {
		this.config = config;
	}

	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}
}
