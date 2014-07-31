/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.site.api.Group;

public class CourseSectionUtil {
	public static final CourseSection decorateGroupWithCmSection(Group group, Section officialSection) {
		
		if (group != null && officialSection != null)
		{
		CourseSectionImpl section = new CourseSectionImpl(group);
		section.setTitle(officialSection.getTitle());
		section.setDescription(officialSection.getDescription());
		section.setCategory(officialSection.getCategory());
		section.setMaxEnrollments(officialSection.getMaxSize());
		Set officialMeetings = officialSection.getMeetings();
		if(officialMeetings != null) {
			List<Meeting> meetings = new ArrayList<Meeting>();
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
		
		// SAK-22831 ensure eid is set in the section so the sections_eid group property is also set when the group is decorated
		section.setEid(officialSection.getEid());
		
		// Ensure that the group is decorated properly, so the group properties are
		// persisted with the correct section metadata
		section.decorateGroup(group);
		return section;
	}
		return null;
	}

}
