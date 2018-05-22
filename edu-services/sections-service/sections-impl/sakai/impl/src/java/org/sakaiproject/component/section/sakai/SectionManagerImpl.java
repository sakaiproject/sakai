/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupFullException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.section.sakai.facade.SakaiUtil;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.SectionEnrollments;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.MembershipException;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.exception.SectionFullException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteAdvisor;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * A sakai-based implementation of the Section Management API, using the
 * new grouping capability of the framework.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public abstract class SectionManagerImpl implements SectionManager, SiteAdvisor {

    // Sakai services set by method injection
    protected abstract SiteService siteService();

    // Sakai services set by dependency injection
    protected AuthzGroupService authzGroupService;
    protected GroupProvider groupProvider;
    protected SecurityService securityService;
    protected UserDirectoryService userDirectoryService;
    protected SessionManager sessionManager;
    protected EntityManager entityManager;
    protected EventTrackingService eventTrackingService;
	protected CourseManagementService courseManagementService;
	protected ThreadLocalManager threadLocalManager;

    // Configuration setting
    protected ExternalIntegrationConfig config;
    
    /**
     * Initialization called once all dependencies are set.
     */
    public void init() {
    	if(log.isInfoEnabled()) log.info("init()");
		siteService().addSiteAdvisor(this);
		
		// A group provider may not exist, so we can't use spring to inject it
		groupProvider = (GroupProvider)ComponentManager.get(GroupProvider.class);
    }

    /**
     * Cleans up any resources in use before destroying this service.
     */
    public void destroy() {
    	if(log.isInfoEnabled()) log.info("destroy()");
    	siteService().removeSiteAdvisor(this);
    }
    
    // SiteAdvisor methods

    /**
	 * {@inheritDoc}
	 */
    public void update(Site site) {
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
		if(handlingMandatoryConfigs(appConfig, site)) {
			if(log.isDebugEnabled()) log.debug(this.getClass().getCanonicalName() + " finished decorating site " + site.getTitle() + " for " + appConfig);
			return;
		}
		
		// Set the defaults for non-mandatory sites
		setSiteDefaults(site, appConfig, siteProps);
		
		// If this site is manually managed, it could have been just changed to be manual.
		// In that case, we flip the formerly "provided" users to be non-provided so they stay in the section.
		if("false".equals(siteProps.getProperty(CourseImpl.EXTERNALLY_MAINTAINED))) {
			if(log.isDebugEnabled()) log.debug("SiteAdvisor " + this.getClass().getCanonicalName() + " stripping provider IDs from all sections in site " + site.getTitle() + ".  The site is internally managed.");
			for(Iterator iter = getSiteGroups(site).iterator(); iter.hasNext();) {
				Group group = (Group)iter.next();
				if(group.getProviderGroupId() == null) {
					// This wasn't provided, so skip it
					continue;
				}
				group.setProviderGroupId(null);
				
				// Add members to the groups based on the current (provided) memberships
				Set members = group.getMembers();
				for(Iterator memberIter = members.iterator(); memberIter.hasNext();) {
					Member member = (Member)memberIter.next();
					if(member.isProvided()) {
						try {
							group.insertMember(member.getUserId(), member.getRole().getId(), member.isActive(), false);
						} catch (IllegalStateException e) {
							log.error(".update: User with id {} cannot be inserted in group with id {} because the group is locked", member.getUserId(), group.getId());
							return;
						}
					}
				}
			}
		} else {
			// This is an externally managed site, so remove any groups without a category and without a providerId
			for(Iterator<Group> iter = getSiteGroups(site).iterator(); iter.hasNext();) {
				Group group = iter.next();
				ResourceProperties props = group.getProperties();
				if(group.getProviderGroupId() == null && (props != null &&
					props.getProperty(CourseSectionImpl.CATEGORY) != null)) {
					// This is a section, and it doesn't have a provider id
					iter.remove();
				}
			}
			// Update the sections from CM.
			syncSections(site);
		}
	}

    private boolean handlingMandatoryConfigs(ExternalIntegrationConfig appConfig, Site site) {
		ResourceProperties siteProps = site.getProperties();

		switch(appConfig) {
		case MANUAL_MANDATORY:
			// If we're configured to treat all sites as manual, set the site to manual control
			siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.FALSE.toString());
			return true;

		case AUTOMATIC_MANDATORY:
			// If we're configured to treat all sites as automatic, set the site to external control and update the sections
			siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.TRUE.toString());
			siteProps.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.FALSE.toString());
			siteProps.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.FALSE.toString());
			syncSections(site);
			return true;

		default:
			return false;
		}
    }

    private void setSiteDefaults(Site site, ExternalIntegrationConfig appConfig, ResourceProperties siteProps) {
		// If the site doesn't have a property set for "Externally Maintained", it's either a new site or one
		// that was created before this SiteAdvisor was registered.
		if(siteProps.getProperty(CourseImpl.EXTERNALLY_MAINTAINED) == null) {
			// Set this property to external if the app config is AUTOMATIC_DEFAULT, else make it internally managed
			// FIXME -- This might have unforseen consequences...
			if(log.isDebugEnabled()) log.debug("Site '" + site.getTitle() + "' has no EXTERNALLY_MAINTAINED flag.");
			if(appConfig == ExternalIntegrationConfig.AUTOMATIC_DEFAULT) {
				siteProps.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.TRUE.toString());
				siteProps.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.FALSE.toString());
				siteProps.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.FALSE.toString());
			} else if(appConfig == ExternalIntegrationConfig.MANUAL_DEFAULT) {
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
	private void syncSections(Site site) {
		if(log.isInfoEnabled()) log.info("Synchronizing internal sections with externally defined sections in site " + site.getId());

		// Use the group provider to split the complex string
		if(groupProvider == null) {
			log.warn("SectionManager can not automatically generate sections without" +
					"a properly configured GroupProvider");
			return;
		}

		// Get the provider Ids associated with this site.  We can't use
		// authzGroupService.getProviderIds(), since we're inspecting the provider IDs
		// on the in-memory object, not what's in persistence
		String siteProviderId = site.getProviderGroupId();
		List<String> providerIdList;
		if(StringUtils.trimToNull(siteProviderId) == null) {
			providerIdList = new ArrayList<String>();
		} else {
			String[] providerIdArray = groupProvider.unpackId(siteProviderId);
			providerIdList = Arrays.asList(providerIdArray);			
		}

		Set<Group> sectionsToSync = new HashSet<Group>();
		
		// Remove any formerly provided sections that are no longer in the list of provider ids,
		// and sync existing sections if they are still listed in the provider ids.
		Collection<Group> groups = getSiteGroups(site);
		if(groups != null) {
			for(Iterator<Group> iter = groups.iterator(); iter.hasNext();) {
				Group group = iter.next();
				String providerId = group.getProviderGroupId();
				if(providerId == null) {
					// This wasn't provided, so skip it
					continue;
				}
				if(group.getProperties() == null || StringUtils.trimToNull(
						group.getProperties().getProperty(CourseSectionImpl.CATEGORY)) == null) {
					// This isn't a section, so skip it
					continue;
				}
				if(providerIdList.contains(providerId)) {
					if(log.isDebugEnabled()) log.debug("Synchronizing section " + group.getReference());
					sectionsToSync.add(group);
				} else {
					if(log.isDebugEnabled()) log.debug("Removing section " + group.getReference());
					iter.remove();
				}
			}
		}
		
		// Sync existing sections
		Set<String> sectionProviderIdsToSync = new HashSet<String>();
		for(Iterator<Group> iter = sectionsToSync.iterator(); iter.hasNext();) {
			Group group = iter.next();
			sectionProviderIdsToSync.add(group.getProviderGroupId());
			syncExternalCourseSectionWithSite(group);
		}

		// Add provided sections that we're not synchronizing
		for(Iterator<String> iter = providerIdList.iterator(); iter.hasNext();) {
			String providerId = iter.next();
			if( ! sectionProviderIdsToSync.contains(providerId)) {
				addExternalCourseSectionToSite(site, providerId);
			}
		}
	}
	
	/**
	 * Synchronizes the state of an internal CourseSection with the state of an externally
	 * defined (CM) section.  This is done so meeting times, locations, etc are kept
	 * in sync with changes made to these data outside Sakai.
	 * 
	 * @param group
	 */
	private void syncExternalCourseSectionWithSite(Group group) {
		String providerId = group.getProviderGroupId();
		Section officialSection = null;
		try {
			officialSection = courseManagementService.getSection(providerId);
		} catch (IdNotFoundException ide) {
			log.error("Site " + group.getContainingSite().getId() + " has a provider id, " + providerId + ", that has no matching section in CM.");
			return;
		}
		CourseSectionUtil.decorateGroupWithCmSection(group, officialSection);
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
		return CourseSectionUtil.decorateGroupWithCmSection(group, officialSection);
	}
	
	// SectionManager Methods
	
	/**
	 * Filters out framework groups that do not have a category.  A section's
	 * category is determined by 
	 * 
	 */
	public List<CourseSection> getSections(String siteContext) {
		if(log.isDebugEnabled()) log.debug("Getting sections for context " + siteContext);
		List<CourseSection> sectionList = new ArrayList<CourseSection>();
		Collection sections;
		try {
			sections = getSiteGroups(getSite(siteContext));
		} catch (IdUnusedException e) {
			log.error("No site with id = " + siteContext);
			return new ArrayList<CourseSection>();
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
	public List<CourseSection> getSectionsInCategory(String siteContext, String categoryId) {
		if(log.isDebugEnabled()) log.debug("Getting " + categoryId + " sections for context " + siteContext);
		List<CourseSection> sectionList = new ArrayList<CourseSection>();
		Collection sections;
		try {
			sections = getSiteGroups(getSite(siteContext));
		} catch (IdUnusedException e) {
			log.error("No site with id = " + siteContext);
			return new ArrayList<CourseSection>();
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
	 * Like getSectionsInCategory but returns ids which are more useful to other methods 
	 * 
	 * @param siteContext The site context
     * @param categoryId
     *
     * @return A List of section uuids
	 */
	private List<String> getSectionIdsInCategory(String siteContext, String categoryId) {
		if(log.isDebugEnabled()) log.debug("Getting " + categoryId + " sections for context " + siteContext);
		List<String> sectionList = new ArrayList<String>();
		Collection sections;
		try {
			sections = getSiteGroups(getSite(siteContext));
		} catch (IdUnusedException e) {
			log.error("No site with id = " + siteContext);
			return sectionList;
		}
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			Group group = (Group)iter.next();
			if(categoryId.equals(group.getProperties().getProperty(CourseSectionImpl.CATEGORY))) {
				sectionList.add(group.getReference());
			}
		}
		return sectionList;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseSection getSection(String sectionUuid) {
		Group group;
		group = findGroup(sectionUuid);
		if(group == null) {
			log.error("Unable to find section " + sectionUuid);
			return null;
		}
		return new CourseSectionImpl(group);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ParticipationRecord> getSiteInstructors(String siteContext) {
		CourseImpl course = (CourseImpl)getCourse(siteContext);
		if(course == null) {
			return new ArrayList<ParticipationRecord>();
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
	public List<ParticipationRecord> getSiteTeachingAssistants(String siteContext) {
		CourseImpl course = (CourseImpl)getCourse(siteContext);
		if(course == null) {
			return new ArrayList<ParticipationRecord>();
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
	public List<EnrollmentRecord> getSiteEnrollments(String siteContext) {
		CourseImpl course = (CourseImpl)getCourse(siteContext);
		if(course == null) {
			return new ArrayList<EnrollmentRecord>();
		}
		Site site = course.getSite();
		Set sakaiUserIds = site.getUsersIsAllowed(SectionAwareness.STUDENT_MARKER);
		List sakaiMembers = userDirectoryService.getUsers(sakaiUserIds);
		List<EnrollmentRecord> membersList = new ArrayList<EnrollmentRecord>();
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
	public List<ParticipationRecord> getSectionTeachingAssistants(String sectionUuid) {
		Group group = findGroup(sectionUuid);
		CourseSection section = getSection(sectionUuid);
		if(section == null) {
			return new ArrayList<ParticipationRecord>();
		}
		if(log.isDebugEnabled()) log.debug("Getting section enrollments in " + sectionUuid);
		String taRole;
		try {
			taRole = getSectionTaRole(group);
		} catch (RoleConfigurationException rce) {
			return new ArrayList<ParticipationRecord>();
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
	public List<EnrollmentRecord> getSectionEnrollments(String sectionUuid) {
		Group group = findGroup(sectionUuid);
		CourseSection section = getSection(sectionUuid);
		if(section == null) {
			return new ArrayList<EnrollmentRecord>();
		}
		if(log.isDebugEnabled()) log.debug("Getting section enrollments in " + sectionUuid);
		String studentRole;
		try {
			studentRole = getSectionStudentRole(group);
		} catch (RoleConfigurationException rce) {
			log.error(rce.getMessage());
			return new ArrayList<EnrollmentRecord>();
		}
		 
		Set sakaiUserUids = group.getUsersHasRole(studentRole);
		List sakaiUsers = userDirectoryService.getUsers(sakaiUserUids);

		List<EnrollmentRecord> membersList = new ArrayList<EnrollmentRecord>();
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
	public List<EnrollmentRecord> findSiteEnrollments(String siteContext, String pattern) {
		List<EnrollmentRecord> fullList = getSiteEnrollments(siteContext);
		List<EnrollmentRecord> filteredList = new ArrayList<EnrollmentRecord>();
		for(Iterator iter = fullList.iterator(); iter.hasNext();) {
			EnrollmentRecord record = (EnrollmentRecord)iter.next();
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
	public List<String> getSectionCategories(String siteContext) {
		return  courseManagementService.getSectionCategories();
	}

	/**
	 * {@inheritDoc}
	 */
	public Course getCourse(String siteContext) {
		if(log.isDebugEnabled()) log.debug("Getting course for context " + siteContext);
		Site site;
		try {
			site = getSite(siteContext);
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
		try {
			return joinSection(sectionUuid, 0);
		} catch (SectionFullException e) {
			// will never happen
			return null;
		}
	}			
	
	/**
	 * {@inheritDoc}
	 */
	public EnrollmentRecord joinSection(String sectionUuid, int maxSize) throws RoleConfigurationException, SectionFullException {
		CourseSection section = getSection(sectionUuid);
		
		// It's possible that this section has been deleted
		if(section == null) {
			log.info("Section " + sectionUuid + " has been deleted, so it can't be joined.");
			return null;
		}

		// Disallow if we're in an externally managed site
		ensureInternallyManaged(section.getCourse().getUuid());
		
		// Don't let users join multiple sections in the same category
		ArrayList categorySections = (ArrayList) getSectionIdsInCategory(section.getCourse().getSiteContext(), section.getCategory());
		String userUid = userDirectoryService.getCurrentUser().getId();
		
		// We are not checking the user's role here, but if the user is attempting to join a section
		// in this category as a student, then the user's role in any existing category in this section
		// must be the same
		List membershipInCategory = authzGroupService.getAuthzUserGroupIds(categorySections, userUid);
		
		List realMembershipInCategory=new ArrayList();
		
		for(String msic: (List <String>) membershipInCategory)
		{
			String realmGroup=msic;
			Group group = findGroup(realmGroup);
			Member member = group.getMember(userUid);
			if(member == null) {
				if (log.isDebugEnabled()) { 
					log.debug("getAuthzUserGroupIds said {} is member of {} but getMember disagrees", userUid, group.getId());
				}
				continue;
			}
			else {
				realMembershipInCategory.add(realmGroup);
			}
		}
		
		if (!realMembershipInCategory.isEmpty()) {
			log.info("User {} can not enroll in section {}. This user is already in section {} ",userUid, sectionUuid, membershipInCategory.get(0));
			return null;
		}

		return joinSection(section, maxSize);
	}

	/**
	 * Join a section by CourseSection
	 */
	private EnrollmentRecord joinSection(CourseSection section, int maxSize) throws RoleConfigurationException, SectionFullException {
		
		Group group = findGroup(section.getUuid());
		String role = getSectionStudentRole(group);
		try {
			authzGroupService.joinGroup(section.getUuid(), role, maxSize);
			postEvent("section.student.join", section.getUuid());
		} catch (AuthzPermissionException e) {
			log.info("access denied while attempting to join authz group: ", e);
			return null;
		} catch (GroupNotDefinedException e) {
			log.info("can not find group while attempting to join authz group: ", e);
			return null;
		} catch (GroupFullException e) {
			throw new SectionFullException("section full");
 		}
		
		// Return the membership record that the app understands
		String userUid = sessionManager.getCurrentSessionUserId();
		User user = SakaiUtil.getUserFromSakai(userUid);
		
		// Remove from thread local cache
		clearGroup(section.getUuid());
		clearSite(group.getContainingSite().getId());
		
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

	private String getSectionTaRole(AuthzGroup group) throws RoleConfigurationException {
		Set roleStrings = group.getRolesIsAllowed(SectionAwareness.TA_MARKER);
		if(roleStrings.size() != 1) {
			if(log.isDebugEnabled()) log.debug("Group " + group +
				" must have one and only one role with permission " +
				SectionAwareness.TA_MARKER);
			throw new RoleConfigurationException("Can't add a user to a section as a TA, since there is no TA-flagged role");
		}
		return (String)roleStrings.iterator().next();
	}

	private String getSectionInstructorRole(AuthzGroup group) throws RoleConfigurationException {
		Set roleStrings = group.getRolesIsAllowed(SectionAwareness.INSTRUCTOR_MARKER);
		if(roleStrings.size() != 1) {
			if(log.isDebugEnabled()) log.debug("Group " + group +
				" must have one and only one role with permission " +
				SectionAwareness.INSTRUCTOR_MARKER);
			throw new RoleConfigurationException("Can't add a user to a section as an Instructor, since there is no Instructor-flagged role");
		}
		return (String)roleStrings.iterator().next();
	}

	/**
	 * {@inheritDoc}
	 */
	public void switchSection(String newSectionUuid) throws RoleConfigurationException {
		try {
			switchSection(newSectionUuid, 0);
		} catch (SectionFullException e) {
			// will never happen
			return;
		}
	}
	 		
	/**
	 * {@inheritDoc}
	 */
	public void switchSection(String newSectionUuid, int maxSize) throws RoleConfigurationException, SectionFullException {
		CourseSection newSection = getSection(newSectionUuid);

		// It's possible that this section has been deleted
		if(newSection == null) {
			return;
		}

		// Disallow if we're in an externally managed site
		ensureInternallyManaged(newSection.getCourse().getUuid());
		
		String userUid = sessionManager.getCurrentSessionUserId();
		
		// Join the new section (could fail if it has filled up since the UI check)
		try {
			if (joinSection(newSection, maxSize) == null)
				// Joining a new section has failed, so don't remove the user from old section 
				return;
		} catch (GroupFullException e) {
			throw new SectionFullException("section full");
		}

		// Find out which sections in this category the student belongs to (should be only one)
		ArrayList categorySections = (ArrayList) getSectionIdsInCategory(newSection.getCourse().getSiteContext(), newSection.getCategory());
		List membershipInCategory = authzGroupService.getAuthzUserGroupIds(categorySections, userUid);
		
		// Remove any section membership for a section of the same category.
		// We can not use dropEnrollmentFromCategory because security checks won't
		// allow a student to update the authZ groups directly.
		boolean errorDroppingSection = false;
		
		String oldSectionUuid = null;
		for(Iterator iter = membershipInCategory.iterator(); iter.hasNext();) {
			
			String sectionUuid = (String) iter.next();
			
			// Skip the current section
			if(sectionUuid.equals(newSectionUuid)) {
				continue;
			}
			try {
				authzGroupService.unjoinGroup(sectionUuid);
				oldSectionUuid = sectionUuid;
			} catch (GroupNotDefinedException e) {
				errorDroppingSection = true;
				log.error("There is no authzGroup with id " + sectionUuid);
			} catch (AuthzPermissionException e) {
				errorDroppingSection = true;
				log.error("Permission denied while " + userUid + " attempted to unjoin authzGroup " + sectionUuid);
			}
		}

		// Only allow the user to remain in the new section if there were no errors dropping section(s)
		if(errorDroppingSection) {
			// Unjoin the newly joined section
			try {
				authzGroupService.unjoinGroup(newSectionUuid);
			} catch (GroupNotDefinedException e) {
				log.debug("Error unjoining newly joined group " + newSectionUuid);
			} catch (AuthzPermissionException e) {
				log.error("Permission denied while " + userUid + " attempted to unjoin authzGroup " + newSectionUuid);
			} 
			return;
		} 	
		
		// Success, post the events
		postEvent("section.student.unjoin", oldSectionUuid);
		postEvent("section.student.switch", newSectionUuid);

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
		
		try {
			group.insertMember(userUid, role, true, false);
		} catch (IllegalStateException e) {
			log.error(".addTaToSection: User with id {} cannot be inserted in group with id {} because the group is locked", userUid, group.getId());
			return null;
		}

		try {
			siteService().saveGroupMembership(group.getContainingSite());
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
		try {
			group.insertMember(userUid, studentRole, true, false);
		} catch (IllegalStateException e) {
			log.error(".addStudentToSection: User with id {} cannot be inserted in group with id {} because the group is locked", userUid, group.getId());
			return null;
		}

		try {
			siteService().saveGroupMembership(group.getContainingSite());
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
			try {
				group.deleteMember(userUid);
			} catch (IllegalStateException e) {
				log.error(".setSectionMemberships: User with id {} cannot be deleted from group with id {} because the group is locked", userUid, group.getId());
			}
		}
		
		// Add the new members (sure would be nice to have transactions here!)
		for(Iterator iter = userUids.iterator(); iter.hasNext();) {
			String userUid = (String)iter.next();
			try {
				group.insertMember(userUid, sakaiRoleString, true, false);
			} catch (IllegalStateException e) {
				log.error(".setSectionMemberships: User with id {} cannot be inserted in group with id {} because the group is locked", userUid, group.getId());
			}
		}

		try {
			siteService().saveGroupMembership(group.getContainingSite());
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
		if(studentRole != null && studentRole.equals(member.getRole().getId())) {
			// We can not drop students from a section in externally managed sites
			ensureInternallyManaged(section.getCourse().getUuid());
		}
		
		try {
			group.deleteMember(userUid);
			siteService().saveGroupMembership(group.getContainingSite());
			postEvent("section.student.drop", sectionUuid);
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
		} catch (IllegalStateException e) {
			log.error(".dropSectionMembership: User with id {} cannot be deleted from group with id {} because the group is locked", userUid, group.getId());
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
			site = getSite(siteContext);
		} catch (IdUnusedException ide) {
			log.error("Unable to find site " + siteContext);
			return;
		}
		Collection groups = getSiteGroups(site);
		for(Iterator iter = groups.iterator(); iter.hasNext();) {
			// Drop the user from this section if they are enrolled
			Group group= (Group)iter.next();
			CourseSectionImpl section = new CourseSectionImpl(group);
			// Don't drop someone from a non-section groups
			if(section.getCategory() == null) {
				continue;
			}
			if(section.getCategory().equals(category)) {				
				// Sakai's current site service code will mark the group as changed
				// even if the specified user is not a member, resulting in many
				// unnecessary (and possibly unintended) updates.
				if (group.getMember(studentUid) != null) {
					try {
						group.deleteMember(studentUid);
					} catch (IllegalStateException e) {
						log.error(".dropEnrollmentFromCategory: User with id {} cannot be deleted from group with id {} because the group is locked", studentUid, group.getId());
						return;
					}
				}
			}
		}
		try {
			siteService().saveGroupMembership(site);
			postEvent("section.student.drop.category", site.getReference());
		} catch (IdUnusedException e) {
			log.error("unable to find site: ", e);
			return;
		} catch (PermissionException e) {
			log.error("access denied while attempting to save site: ", e);
			return;
		}
	}
	
	protected static final String GRP_PREFIX = "section_grp_";
	protected Group findGroup(String learningContextUuid) {
		if(log.isDebugEnabled()) log.debug("findGroup called for " + learningContextUuid);
		Group grp = getGroupInCache(learningContextUuid);
		if(grp == null) {
			if(log.isDebugEnabled()) log.debug("Looking up group " + learningContextUuid + " from the site service");
			grp = siteService().findGroup(learningContextUuid);
			//SAK-19996 there are conditions under which this could be null -DH
			if (grp != null) {
				Site site = grp.getContainingSite();

				// Make sure there aren't multiple copies of the same sites and groups in the
				// cache.
				String siteId = site.getId();
				Site cachedSite = getSiteInCache(siteId);
				if (cachedSite != null) {
					grp = cachedSite.getGroup(learningContextUuid);
				} else {
					setSiteInCache(siteId, site);
				}
				setGroupInCache(learningContextUuid, grp);
			}
		}
		return grp;
	}
	protected Group getGroupInCache(String learningContextUuid) {
		return (Group)threadLocalManager.get(GRP_PREFIX + learningContextUuid);
	}
	protected void setGroupInCache(String learningContextUuid, Group grp) {
		threadLocalManager.set(GRP_PREFIX + learningContextUuid, grp);
	}
	
	protected void clearGroup(String learningContextUuid) {
		threadLocalManager.set(GRP_PREFIX + learningContextUuid, null);
	}
	
	protected static final String SITE_PREFIX = "section_site_";
	protected Site getSite(String siteId) throws IdUnusedException {
		Site site = getSiteInCache(siteId);
		if(site == null) {
			if(log.isDebugEnabled()) log.debug("Looking up site " + siteId + " from the site service");
			site = siteService().getSite(siteId);
			setSiteInCache(siteId, site);
		}
		return site;
	}
	protected Collection<Group> getSiteGroups(Site site) {
		Collection<Group> groups = site.getGroups();
		for(Group group : groups) {
			setGroupInCache(group.getId(), group);
		}
		return groups;
	}
	protected Site getSiteInCache(String siteId) {
		return (Site)threadLocalManager.get(SITE_PREFIX + siteId);
	}
	protected void setSiteInCache(String siteId, Site site) {
		threadLocalManager.set(SITE_PREFIX + siteId, site);
	}

	protected void clearSite(String learningContextUuid) {
		threadLocalManager.set(SITE_PREFIX + learningContextUuid, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getTotalEnrollments(String learningContextUuid) {
		
		Group group = findGroup(learningContextUuid); 
		
		if (group == null) {
			log.error("learning context " + learningContextUuid + " not found");
			return 0;
		}

		Set users = group.getUsersIsAllowed(SectionAwareness.STUDENT_MARKER);
		
		return users.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Map getTotalEnrollmentsMap(String learningContextUuid) {

		Map roleMap = new HashMap<Role, Integer>();

		Group group = findGroup(learningContextUuid); 

		if (group == null) {
			log.error("learning context " + learningContextUuid + " is neither a site nor a section");
			return roleMap;
		}

        roleMap.put(Role.STUDENT, group.getUsersIsAllowed(SectionAwareness.STUDENT_MARKER).size());
        roleMap.put(Role.TA, group.getUsersIsAllowed(SectionAwareness.TA_MARKER).size());
        roleMap.put(Role.INSTRUCTOR, group.getUsersIsAllowed(SectionAwareness.INSTRUCTOR_MARKER).size());
        return roleMap;
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
		
		// Get the site
		Reference ref = entityManager.newReference(courseUuid);
		Site site;
		try {
			site = getSite(ref.getId());
		} catch (IdUnusedException e) {
			log.error("Unable to find site " + courseUuid);
			return null;
		}

		CourseSection section = new CourseSectionImpl(getCourse(site.getId()), title,
				null, category,maxEnrollments, location, startTime, endTime, monday,
				tuesday, wednesday, thursday, friday, saturday, sunday);

		List<CourseSection> sections = new ArrayList<CourseSection>();
		sections.add(section);

		Collection<CourseSection> retValue = addSections(courseUuid, sections);
		if (retValue.size() == 1){
            return retValue.iterator().next();
        }
        log.error("section creation failed");
        return null;
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

	private List<Meeting> filterMeetings(List<Meeting> meetings) {
		// Remove any empty meetings
		List<Meeting> filteredMeetings = new ArrayList<Meeting>();
		for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
			Meeting meeting = iter.next();
			if( ! meeting.isEmpty()) {
				filteredMeetings.add(meeting);
			}
		}
		return filteredMeetings;
	}
	
	private CourseSection addSectionToSite(Site site, String title, String category, Integer maxEnrollments, List<Meeting> meetings) {
		Group group = site.addGroup();
		
		// Construct a CourseSection for this group
		CourseSectionImpl courseSection = new CourseSectionImpl(group);
		
		// Set the fields of the course section
		courseSection.setTitle(title);
		courseSection.setDescription(group.getContainingSite().getTitle() + ", " + title);
		courseSection.setCategory(category);
		courseSection.setMaxEnrollments(maxEnrollments);
		courseSection.setMeetings(filterMeetings(meetings));
		
		// Decorate the framework group
		courseSection.decorateGroup(group);

		return courseSection;
	}
	
	public Collection<CourseSection> addSections(String courseUuid, Collection<CourseSection> sections) {
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(courseUuid);

		// Get the site
		Reference ref = entityManager.newReference(courseUuid);
		Site site;
		try {
			site = getSite(ref.getId());
		} catch (IdUnusedException e) {
			log.error("Unable to find site " + courseUuid);
			return null;
		}

		List<CourseSection> addedSections = new ArrayList<CourseSection>();
		
		// Add the decorated groups to the site
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			addedSections.add(
					addSectionToSite(site, section.getTitle(), section.getCategory(),
							section.getMaxEnrollments(), section.getMeetings()));
		}
		
		// Save the site, along with the new section
		try {
			siteService().save(site);
			clearSite(ref.getId());
			for(Iterator<CourseSection> iter = addedSections.iterator(); iter.hasNext();) {
				postEvent("section.add", iter.next().getUuid());
			}
			return addedSections;
		} catch (IdUnusedException ide) {
			log.error("Error saving site... could not find site " + site.getId(), ide);
		} catch (PermissionException pe) {
			log.error("Error saving site... permission denied for site " + site.getId(), pe);
		}
		return null;
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

	public void updateSection(String sectionUuid, String title, Integer maxEnrollments, List<Meeting> meetings) {
		CourseSectionImpl section = (CourseSectionImpl)getSection(sectionUuid);
		
		if(section == null) {
			throw new RuntimeException("Unable to find section " + sectionUuid);
		}

		// Disallow if we're in an externally managed site
		ensureInternallyManaged(section.getCourse().getUuid());
		
		// Decorate the framework section
		Group group = findGroup(sectionUuid);
		if (group != null) {
			Site site = group.getContainingSite(); 

			if (site != null) {
				// Set the decorator's fields
				section.setTitle(title);
				section.setDescription(site.getTitle() + ", " + title);
				section.setMaxEnrollments(maxEnrollments);
				section.setMeetings(filterMeetings(meetings));

				section.decorateGroup(group);

				// Save the site with its new section
				try {
					siteService().save(site);
					clearSite(site.getId());
					postEvent("section.update", sectionUuid);
				} catch (IdUnusedException ide) {
					log.error("Error saving site... could not find site for section " + group, ide);
				} catch (PermissionException pe) {
					log.error("Error saving site... permission denied for section " + group, pe);
				}
			} else {
				log.error("Error updating section: could not find site for section " + sectionUuid);
			}
		} else {
			log.error("Error updating section: could not find group for section " + sectionUuid);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void disbandSection(String sectionUuid) {
		Set<String> set = new HashSet<String>();
		set.add(sectionUuid);
		disbandSections(set);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void disbandSections(Set<String> sectionUuids) {
		if(sectionUuids == null || sectionUuids.isEmpty()) {
			return;
		}

		// Determine the course (site) we're in
		String firstSectionUuid = sectionUuids.iterator().next();
		CourseSection firstSection = getSection(firstSectionUuid);
		if(firstSection == null) {
			if(log.isDebugEnabled()) log.debug("Unable to remove section " + firstSectionUuid);
			return;
		}
		Course course = firstSection.getCourse();

		// Disallow if we're in an externally managed site
		ensureInternallyManaged(course.getUuid());

		Site site = null;
		for(Iterator<String> iter = sectionUuids.iterator(); iter.hasNext();) {
			String sectionUuid = iter.next();
			if(log.isDebugEnabled()) log.debug("Disbanding section " + sectionUuid);
			Group group = findGroup(sectionUuid);

			// TODO Add token in UI to intercept double clicks in action buttons
			// SAK-3553 (Clicking remove button twice during section remove operation results in blank iframe.)
			if(group == null) {
				log.warn("Unable to find group with uuid " + sectionUuid);
				return;
			}
			if(site == null) {
				site = group.getContainingSite();
			}
			try {
				site.deleteGroup(group);
			} catch (IllegalStateException e) {
				log.error(".disbandSections: Group with id {} cannot be removed because is locked", group.getId());
			}
		}

		try {
			siteService().save(site);
			clearSite(site.getId());
			for(Iterator<String> iter = sectionUuids.iterator(); iter.hasNext();) {
				String sectionUuid = iter.next();
				postEvent("section.disband", sectionUuid);
			}
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
			site = getSite(siteId);
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
			site = getSite(siteId);
		} catch (IdUnusedException e) {
			throw new RuntimeException("Can not find site " + courseUuid, e);
		}
		ResourceProperties props = site.getProperties();
		
		// Update the site
		props.addProperty(CourseImpl.EXTERNALLY_MAINTAINED, Boolean.toString(externallyManaged));
		if(externallyManaged) {
			// Also set the self join/switch to false
			props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.toString(false));
			props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.toString(false));
		}

		try {
			siteService().save(site);
			clearSite(ref.getId());
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
			site = getSite(siteId);
		} catch (IdUnusedException e) {
			throw new RuntimeException("Can not find site " + courseUuid, e);
		}
		ResourceProperties props = site.getProperties();
		return Boolean.toString(true).equals(props.getProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED));
	}
	

	public void setJoinOptions(String courseUuid, boolean joinAllowed, boolean switchAllowed) {
		// Disallow if we're in an externally managed site
		ensureInternallyManaged(courseUuid);

		Reference ref = entityManager.newReference(courseUuid);
		String siteId = ref.getId();
		Site site;
		try {
			site = getSite(siteId);
		} catch (IdUnusedException e) {
			throw new RuntimeException("Can not find site " + courseUuid, e);
		}
		ResourceProperties props = site.getProperties();
		
		// Get the existing join and switch settings, so we know what's changed
		boolean oldJoin = Boolean.valueOf(props.getProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED)).booleanValue();
		boolean oldSwitch = Boolean.valueOf(props.getProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED)).booleanValue();
		
		props.addProperty(CourseImpl.STUDENT_REGISTRATION_ALLOWED, Boolean.toString(joinAllowed));
		props.addProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED, Boolean.toString(switchAllowed));
		try {
			siteService().save(site);
			clearSite(site.getId());
			if(joinAllowed != oldJoin) {
				postEvent("section.student.reg=" + joinAllowed, site.getReference());
			}
			if(switchAllowed != oldSwitch) {
				postEvent("section.student.switch=" + switchAllowed, site.getReference());
			}
			
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
			site = getSite(siteId);
		} catch (IdUnusedException e) {
			throw new RuntimeException("Can not find site " + courseUuid, e);
		}
		ResourceProperties props = site.getProperties();
		return Boolean.toString(true).equals(props.getProperty(CourseImpl.STUDENT_SWITCHING_ALLOWED));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<EnrollmentRecord> getUnsectionedEnrollments(String courseUuid, String category) {
		Reference siteRef = entityManager.newReference(courseUuid);
		String siteId = siteRef.getId();

		// Get all of the sections and userUids of enrolled students
		List siteEnrollments = getSiteEnrollments(siteId);
		
		// Get all userUids of students enrolled in sections of this category
		
		List<String> sectionedStudentUids = new ArrayList<String>();
		List<String> categorySections = getSectionIdsInCategory(siteId, category);
		
		Set usersbygroup = authzGroupService.getUsersIsAllowedByGroup(SectionAwareness.STUDENT_MARKER, categorySections);
		
		for (Iterator iterator = usersbygroup.iterator(); iterator.hasNext();) {  
		     String[] entry = (String[]) iterator.next(); 
		     sectionedStudentUids.add(entry[0]);
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
	public Map getEnrollmentCount(List sectionSet) {
		ArrayList<String> siteGroupRefs = new ArrayList<String>(sectionSet.size());
		
		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSectionImpl section = (CourseSectionImpl)sectionIter.next();			
			siteGroupRefs.add(section.getGroup().getReference()); 
		}		
		
		return authzGroupService.getUserCountIsAllowed(SectionAwareness.STUDENT_MARKER, siteGroupRefs);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String,List<ParticipationRecord>> getSectionTeachingAssistantsMap(List sectionSet)
	{
		ArrayList<String> siteGroupRefs = new ArrayList<String>(sectionSet.size());
		Map<String,List<ParticipationRecord>> sectionTaMap = new HashMap<String,List<ParticipationRecord>>();
		
		// Iterate through sections and create an empty TA set for each
		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSectionImpl section = (CourseSectionImpl)sectionIter.next();			
			siteGroupRefs.add(section.getGroup().getReference());
			
			List<ParticipationRecord> membersList = new ArrayList<ParticipationRecord>();
			sectionTaMap.put(section.getGroup().getReference(), membersList);
		}		

		Set usersbygroup = authzGroupService.getUsersIsAllowedByGroup(SectionAwareness.TA_MARKER, siteGroupRefs);
		
		// Iterate through user/group pairs and add to the Map
		for (Iterator iterator = usersbygroup.iterator(); iterator.hasNext();) {  
		     String[] entry = (String[]) iterator.next();
		     
		     String useruid = entry[0];
		     String sectionUuid = entry[1];		     
		     
		     List<ParticipationRecord> membersList = sectionTaMap.get(sectionUuid);
		     
		     try {
		    	 org.sakaiproject.user.api.User sakaiUser = userDirectoryService.getUser(useruid);
			     User user = SakaiUtil.convertUser(sakaiUser);
			     TeachingAssistantRecordImpl record = new TeachingAssistantRecordImpl(user);
			     membersList.add(record);					
		     } catch (UserNotDefinedException ex) {
		    	 if (log.isDebugEnabled()) log.debug("Userid " + useruid + " found in group " + sectionUuid + " does not exist");
		     }
		  } 
		
		return sectionTaMap;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Set<EnrollmentRecord> getSectionEnrollments(String userUid, String courseUuid) {

		if(log.isDebugEnabled()) log.debug("getSectionEnrollments for userUid " + userUid + " courseUuid " + courseUuid);
		
		// Get the user
		org.sakaiproject.user.api.User sakaiUser;
		try {
			sakaiUser = userDirectoryService.getUser(userUid); 
		} catch (UserNotDefinedException ide) {
			log.error("Can not find user with id " + userUid);
			return new HashSet<EnrollmentRecord>();
		}
		User sectionUser = SakaiUtil.convertUser(sakaiUser);
		
		// Get all of the sections
		Reference siteRef = entityManager.newReference(courseUuid);
		String siteId = siteRef.getId();
		List sections = getSections(siteId);
		
		// Generate a set of sections for which this user is enrolled
		Set<EnrollmentRecord> sectionEnrollments = new HashSet<EnrollmentRecord>();
		ArrayList<String> siteGroupRefs = new ArrayList<String>(sections.size());
		
		for(Iterator sectionIter = sections.iterator(); sectionIter.hasNext();) {
			CourseSectionImpl section = (CourseSectionImpl)sectionIter.next();			
			siteGroupRefs.add(section.getGroup().getReference()); 
		}

		List groups = authzGroupService.getAuthzUserGroupIds(siteGroupRefs, userUid);

		// Check membership of groups
        for (Iterator i = groups.iterator(); i.hasNext();)
        {
			Group group = findGroup((String)i.next());
			Member member = group.getMember(userUid);
			if(member == null) {
				if (log.isDebugEnabled()) log.debug("getAuthzUserGroupIds said " + userUid + " is member of " + group.getId() + " but getMember disagrees");
				continue;
			}
			if(member.getRole().isAllowed(SectionAwareness.STUDENT_MARKER)) {
				sectionEnrollments.add(new EnrollmentRecordImpl(new CourseSectionImpl(group), null, sectionUser));
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

	public ExternalIntegrationConfig getConfiguration(Object obj) {
		if(config == null) {
			log.warn("No integration configuration property has been set.  Using " + ExternalIntegrationConfig.MANUAL_DEFAULT);
			config = ExternalIntegrationConfig.MANUAL_DEFAULT;
		}
		return config;
	}

	// Dependency injection

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
		if(ExternalIntegrationConfig.AUTOMATIC_MANDATORY.toString().equals(config)) {
			this.config = ExternalIntegrationConfig.AUTOMATIC_MANDATORY;
		} else if(ExternalIntegrationConfig.AUTOMATIC_DEFAULT.toString().equals(config)){
			this.config = ExternalIntegrationConfig.AUTOMATIC_DEFAULT;
		} else if(ExternalIntegrationConfig.MANUAL_DEFAULT.toString().equals(config)) {
			this.config = ExternalIntegrationConfig.MANUAL_DEFAULT;
		} else if(ExternalIntegrationConfig.MANUAL_MANDATORY.toString().equals(config)) {
			this.config = ExternalIntegrationConfig.MANUAL_MANDATORY;
		} else {
			log.warn("Unknown section integration config specified: " + config + ".  Using " + ExternalIntegrationConfig.MANUAL_DEFAULT);
		}
	}

	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	public void setGroupProvider(GroupProvider groupProvider) {
		this.groupProvider = groupProvider;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}
	public Calendar getOpenDate(String siteId) {
		//Reference ref = entityManager.newReference(courseUuid);
		//String siteId = ref.getId();
		Site site;
		try {
			site = siteService().getSite(siteId);
		} catch (IdUnusedException e) {
			throw new RuntimeException("Can not find site " + siteId, e);
		}
		ResourceProperties props = site.getProperties();
		String open=props.getProperty(CourseImpl.STUDENT_OPEN_DATE);
		Calendar c=null;
		if (open!=null && open.length()>0){
			c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(open));
		}
		return c;
	}

	public void setOpenDate(String courseUuid, Calendar openDate) {
		// Disallow if the service is configured to be mandatory
		Reference ref = entityManager.newReference(courseUuid);
		String siteId = ref.getId();
		Site site;
		try {
			site = siteService().getSite(siteId);
		} catch (IdUnusedException e) {
			throw new RuntimeException("Can not find site " + courseUuid, e);
		}
		ResourceProperties props = site.getProperties();
		// Update the site
		if (openDate!=null) {
			props.addProperty(CourseImpl.STUDENT_OPEN_DATE, Long.toString(openDate.getTimeInMillis()));
		} else {
			props.addProperty(CourseImpl.STUDENT_OPEN_DATE, null);
		}
		try {
			siteService().save(site);
			if(log.isDebugEnabled()) log.debug("Saved site " + site.getTitle());
		} catch (IdUnusedException ide) {
			log.error("Error saving site... could not find site " + site, ide);
		} catch (PermissionException pe) {
			log.error("Error saving site... permission denied for " + site, pe);
		}
	}
}
