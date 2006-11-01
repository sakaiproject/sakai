package org.sakaiproject.component.section.sakai21.advisor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.section.sakai21.CourseSectionImpl;
import org.sakaiproject.component.section.sakai21.MeetingImpl;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public class CourseManagementProviderExternalSectionAdvisor implements ExternalSectionAdvisor {
	private static final Log log = LogFactory.getLog(CourseManagementProviderExternalSectionAdvisor.class);
	
	protected AuthzGroupService authzGroupService;
	protected SiteService siteService;
	protected CourseManagementService courseManagementService;
	
	public void replaceManualSectionsWithExternalSections(Site site) {
		// Remove existing groups.  Iterate over a new collection to avoid concurrent modification exceptions
    	Collection groups = new HashSet(site.getGroups());
		for(Iterator iter = groups.iterator(); iter.hasNext();) {
			Group group = (Group)iter.next();
			if(log.isDebugEnabled()) log.debug("Removing group " + group.getId() + " from site " + site.getId());
			site.removeGroup(group);
		}
		
		// Find the provider IDs to use in creating new groups
		Set providerIds = authzGroupService.getProviderIds(site.getReference());
		for(Iterator iter = providerIds.iterator(); iter.hasNext();) {
			String providerId = (String)iter.next();
			if(log.isDebugEnabled()) log.debug("Adding an externally managed section with id = " + providerId);
			
			// Create a new sakai section (group) for this providerId
			Section officialSection = courseManagementService.getSection(providerId);
			Group group = site.addGroup();
			group.setProviderGroupId(providerId);
			CourseSectionImpl section = new CourseSectionImpl(group);
			
			// The "decorating" metadata isn't yet part of the section, so set it manually
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
		}
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setCourseManagementService(
			CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
}
