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

import java.io.Serializable;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.Meeting;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Group;

public class CourseSectionImpl implements CourseSection, Comparable, Serializable {

	private static final long serialVersionUID = 1L;
	private static final String TIME_FORMAT_LONG = "h:mm a";
	private static final Log log = LogFactory.getLog(CourseSectionImpl.class);
	public static final String SEP_CHARACTER = ",";
	public static final String CATEGORY = "sections_category";
	public static final String END_TIME = "sections_end_time";
	public static final String START_TIME = "sections_start_time";
	public static final String LOCATION = "sections_location";
	public static final String MAX_ENROLLMENTS = "sections_max_enrollments";
	public static final String MONDAY = "sections_monday";
	public static final String TUESDAY = "sections_tuesday";
	public static final String WEDNESDAY = "sections_wednesday";
	public static final String THURSDAY = "sections_thursday";
	public static final String FRIDAY = "sections_friday";
	public static final String SATURDAY = "sections_saturday";
	public static final String SUNDAY = "sections_sunday";

	// Fields from Site Section
	protected String description;

	// Fields from CourseSection
    protected String uuid;
	protected Course course;
	protected String category;
    protected Integer maxEnrollments;
    protected List meetings;

    // Fields shared between the two interfaces
	protected String id;
    protected String title;

    // Transient holder for the framework group being decorated.
    private transient Group group;
    
	public CourseSectionImpl(Group group) {
		this.meetings = new ArrayList();
		// We always start with a single empty meeting
		meetings.add(new MeetingImpl());
		this.group = group;
		ResourceProperties props = group.getProperties();
		this.id = group.getId();
		this.uuid = group.getReference();
		this.title = group.getTitle();
		this.description = group.getContainingSite().getTitle() + ", " + this.title;
		this.course = new CourseImpl(group.getContainingSite());
		this.category = props.getProperty(CourseSectionImpl.CATEGORY);
		String str = props.getProperty(MAX_ENROLLMENTS);
		if(StringUtils.trimToNull(str) != null) {
			try {
				this.maxEnrollments = Integer.valueOf(str);
			} catch(NumberFormatException nfe) {
				if(log.isDebugEnabled()) log.debug("can not parse integer property for " + CourseSectionImpl.MAX_ENROLLMENTS);
			}
		}

		// Parse the meetings for this group
		long numMeetings = 0;
		String locations = props.getProperty(CourseSectionImpl.LOCATION);
		if(locations != null) {
			try {
				numMeetings = locations.split(CourseSectionImpl.SEP_CHARACTER).length;
				if(log.isDebugEnabled()) log.debug("Found " + numMeetings + " meetings in group " + group);
			} catch (Exception e) {
				log.warn("Could not parse the number of meetings for group " + group);
			}
		}
		
		// If we have legitimate meetings, remove the placeholder
		if(numMeetings > 0) {
			meetings.clear();
			if(log.isDebugEnabled()) log.debug("Constructing a CourseSectionImpl with " + numMeetings + " meetings");
		} else {
			if(log.isDebugEnabled()) log.debug("Constructing a CourseSectionImpl with one default meeting");
		}
				
		// Iterate through the meeting properties and add the meetings to the group
		for(int i=0; i < numMeetings; i++) {
			String location = getIndexedStringProperty(i, props.getProperty(CourseSectionImpl.LOCATION));
			Time endTime = CourseSectionImpl.convertStringToTime(getIndexedStringProperty(i, props.getProperty(END_TIME)));
			Time startTime = CourseSectionImpl.convertStringToTime(getIndexedStringProperty(i, props.getProperty(START_TIME)));

			boolean monday = false;
			boolean tuesday = false;
			boolean wednesday = false;
			boolean thursday = false;
			boolean friday = false;
			boolean saturday = false;
			boolean sunday = false;
			
			try {
				monday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.MONDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.MONDAY);
			}
			try {
				tuesday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.TUESDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.TUESDAY);
			}
			try {
				wednesday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.WEDNESDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.WEDNESDAY);
			}
			try {
				thursday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.THURSDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.THURSDAY);
			}
			try {
				friday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.FRIDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.FRIDAY);
			}
			try {
				saturday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.SATURDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.SATURDAY);
			}
			try {
				sunday = getIndexedBooleanProperty(i, props.getProperty(CourseSectionImpl.SUNDAY));
			} catch (Exception e) {
				if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + i + " member of complex string " + CourseSectionImpl.SUNDAY);
			}
			
			// Now that we've parsed the group, add the meeting to the list
			meetings.add(new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday));
		}
		
	}
	
	private boolean getIndexedBooleanProperty(int index, String complexString) {
		String[] sa = complexString.split(CourseSectionImpl.SEP_CHARACTER);
		if(index >=sa.length) {
			log.warn("Can not get " + index + " index from string " + complexString);
			return false;
		}
		return Boolean.parseBoolean(sa[index]);
	}

	private String getIndexedStringProperty(int index, String complexString) {
		if(complexString == null) {
			return null;
		}
		String[] sa = complexString.split(CourseSectionImpl.SEP_CHARACTER);
		if(index >=sa.length) {
			log.warn("Can not get " + index + " index from string " + complexString);
			return null;
		}
		return sa[index];
	}

    public static final String convertTimeToString(Time time) {
    	if(time == null) {
    		return null;
    	}
		SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_LONG);
    	return sdf.format(time);
    }

    public static final Time convertStringToTime(String str) {
    	if(StringUtils.trimToNull(str) == null) {
    		return null;
    	}
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_LONG);
        	return new Time(sdf.parse(str).getTime());
    	} catch (Exception e) {
    		if(log.isDebugEnabled()) log.debug("Unable to parse " + str);
    		return null;
    	}
    }

    /**
     * Decorates the framework's section (group) with metadata from this CourseSection.
     * 
     * @param group The framework group
     */
    public void decorateGroup(Group group) {
    	ResourceProperties props = group.getProperties();
    	group.setTitle(title);
    	group.setDescription(description);
    	props.addProperty(CourseSectionImpl.CATEGORY, category);
    	if(maxEnrollments == null) {
    		props.removeProperty(CourseSectionImpl.MAX_ENROLLMENTS);
    	} else {
    		props.addProperty(CourseSectionImpl.MAX_ENROLLMENTS, maxEnrollments.toString());
    	}
    	
    	// Add the properties that containing the meeting metadata
    	StringBuffer locationBuffer = new StringBuffer();

    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
    		String meetingLocation = meeting.getLocation();
    		if(meetingLocation == null) {
    			meetingLocation = "";
    		}
    		locationBuffer.append(meetingLocation);
    		if(iter.hasNext()) {
    			locationBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.LOCATION + " to " + locationBuffer.toString());
    	props.addProperty(CourseSectionImpl.LOCATION, locationBuffer.toString());

    	StringBuffer startTimeBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
    		Time meetingStart = meeting.getStartTime();
    		if(meetingStart != null) {
        		startTimeBuffer.append(CourseSectionImpl.convertTimeToString(meetingStart));
    		}
    		if(iter.hasNext()) {
    			startTimeBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.START_TIME + " to " + startTimeBuffer.toString());
    	props.addProperty(CourseSectionImpl.START_TIME, startTimeBuffer.toString());

    	StringBuffer endTimeBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
    		Time meetingEnd = meeting.getEndTime();
    		if(meetingEnd != null) {
        		endTimeBuffer.append(CourseSectionImpl.convertTimeToString(meetingEnd));
    		}
    		if(iter.hasNext()) {
    			endTimeBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.END_TIME + " to " + endTimeBuffer.toString());
    	props.addProperty(CourseSectionImpl.END_TIME, endTimeBuffer.toString());
    		
    	StringBuffer mondayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	mondayBuffer.append(meeting.isMonday());
    		if(iter.hasNext()) {
    			mondayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.MONDAY + " to " + mondayBuffer.toString());
    	props.addProperty(CourseSectionImpl.MONDAY, mondayBuffer.toString());

    	StringBuffer tuesdayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	tuesdayBuffer.append(meeting.isTuesday());
    		if(iter.hasNext()) {
    			tuesdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.TUESDAY + " to " + tuesdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.TUESDAY, tuesdayBuffer.toString());

    	StringBuffer wednesdayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	wednesdayBuffer.append(Boolean.valueOf(meeting.isWednesday()));
    		if(iter.hasNext()) {
    			wednesdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.WEDNESDAY + " to " + wednesdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.WEDNESDAY, wednesdayBuffer.toString());

    	StringBuffer thursdayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	thursdayBuffer.append(Boolean.valueOf(meeting.isThursday()));
    		if(iter.hasNext()) {
    			thursdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.THURSDAY + " to " + thursdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.THURSDAY, thursdayBuffer.toString());

    	StringBuffer fridayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	fridayBuffer.append(Boolean.valueOf(meeting.isFriday()));
    		if(iter.hasNext()) {
    			fridayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.FRIDAY + " to " + fridayBuffer.toString());
    	props.addProperty(CourseSectionImpl.FRIDAY, fridayBuffer.toString());
    	
    	StringBuffer saturdayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	saturdayBuffer.append(Boolean.valueOf(meeting.isSaturday()));
    		if(iter.hasNext()) {
    			saturdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.SATURDAY+ " to " + saturdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.SATURDAY, saturdayBuffer.toString());

    	StringBuffer sundayBuffer = new StringBuffer();
    	for(Iterator iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	sundayBuffer.append(Boolean.valueOf(meeting.isSunday()));
    		if(iter.hasNext()) {
    			sundayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.SUNDAY+ " to " + sundayBuffer.toString());
    	props.addProperty(CourseSectionImpl.SUNDAY, sundayBuffer.toString());
    }
    
    /* Bean getters & setters */
    
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List getMeetings() {
		return meetings;
	}

	public void setMeetings(List meetings) {
		this.meetings = meetings;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getMaxEnrollments() {
		return maxEnrollments;
	}

	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof CourseSectionImpl) {
			CourseSectionImpl other = (CourseSectionImpl)o;
			return new EqualsBuilder()
				.append(getUuid(), other.getUuid())
				.isEquals();
		}
		return false;
	}
	
	public int hashCode() {
		return new HashCodeBuilder()
			.append(getUuid())
			.toHashCode();
	}

	/**
	 * Compares CourseSectionImpls based on their category ID and title.  Sections
	 * without a category are sorted last.
	 */
	public int compareTo(Object o) {
		if(o == this) {
			return 0;
		}
		if(o instanceof CourseSectionImpl) {
			CourseSectionImpl other = (CourseSectionImpl)o;
			if(this.category != null && other.category == null) {
				return -1;
			} else if(this.category == null && other.category != null) {
				return 1;
			}
			if(this.category == null && other.category == null) {
				return this.title.toLowerCase().compareTo(other.title.toLowerCase());
			}
			int categoryComparison = this.category.compareTo(other.category);
			if(categoryComparison == 0) {
				return this.title.toLowerCase().compareTo(other.title.toLowerCase());
			} else {
				return categoryComparison;
			}
		} else {
			throw new ClassCastException("Can not compare CourseSectionImpl to " + o.getClass());
		}
		
	}

	/**
	 * Access the group object being decorated.  This field is transient, so this
	 * is likely to return null.  This method should not be added to the CourseSection
	 * interface, since it is implementation dependent.
	 * 
	 * @return The transient Group object being modeled.
	 */
	public Group getGroup() {
		return group;
	}
}
