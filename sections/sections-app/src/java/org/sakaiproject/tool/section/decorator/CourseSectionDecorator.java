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
package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Decorates CourseSections for display in the UI.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSectionDecorator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected CourseSection section;
	protected String categoryForDisplay;
	protected List decoratedMeetings;

	/**
	 * Creates a decorator based on the course section alone.  Useful for only
	 * displaying meeting times.
	 * 
	 * @param section
	 */
	public CourseSectionDecorator(CourseSection section) {
		this.section = section;
		this.decoratedMeetings = new ArrayList();
		if(section.getMeetings() != null) {
			for(Iterator iter = section.getMeetings().iterator(); iter.hasNext();) {
				decoratedMeetings.add(new MeetingDecorator((Meeting)iter.next()));
			}
		}
	}

	 public CourseSectionDecorator(CourseSection section, String categoryForDisplay) {
		this(section);
		this.categoryForDisplay = categoryForDisplay;
	}

	public CourseSectionDecorator() {
		// Needed for serialization
	}

	// TODO Added for debugging. Should be more efficient to make section transient,
	// and store and retrieve a section UID to keep track of which section goes
	// with which row.
	public CourseSection getSection() {
		return section;
	}
	
	public List getDecoratedMeetings() {
		return decoratedMeetings;
	}

	// Decorator methods
	public String getCategoryForDisplay() {
		return categoryForDisplay;
	}

	// Delegate methods

	public String getCategory() {
		return section.getCategory();
	}

	public Course getCourse() {
		return section.getCourse();
	}

	public Integer getMaxEnrollments() {
		return section.getMaxEnrollments();
	}

	public String getTitle() {
		return section.getTitle();
	}

	public String getUuid() {
		return section.getUuid();
	}
	
	public class MeetingDecorator implements Serializable {
		private static final long serialVersionUID = 1L;
		private Meeting meeting;
		
		public MeetingDecorator() {
			// Needed for serialization
		}

		public MeetingDecorator(Meeting meeting) {
			this.meeting = meeting;
		}
		
		private List getDayList() {
			List list = new ArrayList();
			if(meeting.isMonday())
				list.add("day_of_week_monday");
			if(meeting.isTuesday())
				list.add("day_of_week_tuesday");
			if(meeting.isWednesday())
				list.add("day_of_week_wednesday");
			if(meeting.isThursday())
				list.add("day_of_week_thursday");
			if(meeting.isFriday())
				list.add("day_of_week_friday");
			if(meeting.isSaturday())
				list.add("day_of_week_saturday");
			if(meeting.isSunday())
				list.add("day_of_week_sunday");
			return list;
		}
		
		private List getAbbreviatedDayList() {
			List list = new ArrayList();
			if(meeting.isMonday())
				list.add("day_of_week_monday_abbrev");
			if(meeting.isTuesday())
				list.add("day_of_week_tuesday_abbrev");
			if(meeting.isWednesday())
				list.add("day_of_week_wednesday_abbrev");
			if(meeting.isThursday())
				list.add("day_of_week_thursday_abbrev");
			if(meeting.isFriday())
				list.add("day_of_week_friday_abbrev");
			if(meeting.isSaturday())
				list.add("day_of_week_saturday_abbrev");
			if(meeting.isSunday())
				list.add("day_of_week_sunday_abbrev");
			return list;
		}

		public String getMeetingTimes() {
			String daySepChar = JsfUtil.getLocalizedMessage("day_of_week_sep_char");
			String timeSepChar = JsfUtil.getLocalizedMessage("time_sep_char");

			StringBuffer sb = new StringBuffer();

			// Days of the week
			for(Iterator iter = getAbbreviatedDayList().iterator(); iter.hasNext();) {
				String day = (String)iter.next();
				sb.append(JsfUtil.getLocalizedMessage(day));
				if(iter.hasNext()) {
					sb.append(daySepChar);
				}
			}

			// Start time
			DateFormat df = new SimpleDateFormat("h:mm a");
			sb.append(" ");
			if(meeting.getStartTime() != null) {
				sb.append(df.format(new Date(meeting.getStartTime().getTime())).toLowerCase());
			}

			// End time
			if(meeting.getStartTime() != null &&
					meeting.getEndTime() != null) {
				sb.append(timeSepChar);
			}

			if(meeting.getEndTime() != null) {
				sb.append(df.format(new Date(meeting.getEndTime().getTime())).toLowerCase());
			}

			return sb.toString();
		}

		public String getMeetingDays() {
			String daySepChar = JsfUtil.getLocalizedMessage("day_of_week_sep_char");

			StringBuffer sb = new StringBuffer();
			for(Iterator iter = getDayList().iterator(); iter.hasNext();) {
				String day = (String)iter.next();
				sb.append(JsfUtil.getLocalizedMessage(day));
				if(iter.hasNext()) {
					sb.append(daySepChar);
				}
			}
			return sb.toString();
		}

		// Meeting delegate methods

		public Time getEndTime() {
			return meeting.getEndTime();
		}

		public String getLocation() {
			return meeting.getLocation();
		}

		public Time getStartTime() {
			return meeting.getStartTime();
		}

		public boolean isFriday() {
			return meeting.isFriday();
		}

		public boolean isMonday() {
			return meeting.isMonday();
		}

		public boolean isSaturday() {
			return meeting.isSaturday();
		}

		public boolean isSunday() {
			return meeting.isSunday();
		}

		public boolean isThursday() {
			return meeting.isThursday();
		}

		public boolean isTuesday() {
			return meeting.isTuesday();
		}

		public boolean isWednesday() {
			return meeting.isWednesday();
		}
	}
}
