package org.sakaiproject.component.section.sakai21.advisor;

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
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.section.sakai21.CourseSectionImpl;
import org.sakaiproject.component.section.sakai21.MeetingImpl;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.SectionCategory;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;

/**
 * An ExternalSectionAdvisor that connects to the CM service and uses the providerId
 * mechanism to keep section memberships in-line with the external section.
 * 
 * If you do not use the org.sakaiproject.coursemanagement package to accomplish
 * your enterprise integration, or if you don't use Sakai's group provider mechanism,
 * you can not rely on this ExternalSectionAdvisor implementaiton.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public class CourseManagementProviderExternalSectionAdvisor implements ExternalSectionAdvisor {
	private static final Log log = LogFactory.getLog(CourseManagementProviderExternalSectionAdvisor.class);
	
	protected AuthzGroupService authzGroupService;
	protected CourseManagementService courseManagementService;
	
	public void replaceManualSectionsWithExternalSections(Site site) {
		// Remove existing sections.  Iterate over a new collection to avoid concurrent modification exceptions
    	Collection groups = new HashSet(site.getGroups());
		for(Iterator iter = groups.iterator(); iter.hasNext();) {
			Group group = (Group)iter.next();
			CourseSectionImpl courseSection = new CourseSectionImpl(group);
			if(courseSection.getCategory() != null) {
				if(log.isDebugEnabled()) log.debug("Removing group " + group.getId() + " from site " + site.getId());
				site.removeGroup(group);
			}
		}
		
		// Find the provider IDs to use in creating new groups
		Set providerIds = authzGroupService.getProviderIds(site.getReference());
		for(Iterator iter = providerIds.iterator(); iter.hasNext();) {
			String providerId = (String)iter.next();
			if(log.isDebugEnabled()) log.debug("Adding an externally managed section with id = " + providerId);
			addCourseSectionToSite(site, providerId);
		}
	}

	/**
	 * Adds a CourseSection (a decorated group) to a site.  The CourseSection is
	 * constructed by finding the official section from CM and converting it to a CourseSection.
	 * 
	 * @param site The site in which we are adding a CourseSection 
	 * @param sectionId The Enterprise ID of the section to add.
	 * 
	 * @return The CourseSection that was added to the site
	 */
	protected CourseSection addCourseSectionToSite(Site site, String sectionEid) {
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
				Meeting officialMeeting = (Meeting)meetingIter.next();
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
	
		return section;
	}
	
	
	public void updateInternalSections(Site site) {
		if(log.isDebugEnabled()) log.debug("updateInternalSections(Site " + site.getId() + ")");
		
		// Get the existing groups from the site, and add them to a new collection so we can remove elements from the original collection
		Collection<Group> groups = new HashSet<Group>(site.getGroups());
		
		// Remove all sections (this will be done by the SiteService impl anyhow, so there's no performance loss in doing a mass remove/readd of sections)
		for(Iterator<Group> iter = groups.iterator(); iter.hasNext();) {
			Group group = iter.next();
			CourseSectionImpl section = new CourseSectionImpl(group);
			if(section.getCategory() != null) {
				if(log.isDebugEnabled()) log.debug("Removing section " + section.getUuid());
				site.removeGroup(group);
			}
		}
		
		// Get the provider Ids associated with this site
		Set providerIds = authzGroupService.getProviderIds(site.getReference());

		// Add new groups (decorated as sections) based on the site's providerIds
		for(Iterator iter = providerIds.iterator(); iter.hasNext();) {
			String providerId = (String)iter.next();
			addCourseSectionToSite(site, providerId);
		}
	}
	
	public Map<String, String> getSectionCategoryMap() {
		List categories = courseManagementService.getSectionCategories();
		Map categoryMap = new HashMap();
		for(Iterator iter = categories.iterator(); iter.hasNext();) {
			SectionCategory cat = (SectionCategory)iter.next();
			categoryMap.put(cat.getCategoryCode(), cat.getCategoryDescription());
		}
		return categoryMap;
	}

	// Dependency injection
	
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setCourseManagementService(
			CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}
}
