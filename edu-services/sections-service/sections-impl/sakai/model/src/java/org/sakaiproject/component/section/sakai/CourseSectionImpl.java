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

import java.io.Serializable;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.time.api.TimeService;

@Slf4j
public class CourseSectionImpl implements CourseSection, Comparable<CourseSection>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TIME_FORMAT_LONG = "h:mm a";
    private static final String TIME_FORMAT_DATE_TZ = "dd/MM/yyyy HH:mm zzzz";
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
    public static final String EID = "sections_eid";

    // Fields from Site Group
    protected String description;

    // Fields from CourseSection
    protected String uuid;
    protected Course course;
    protected String category;
    protected Integer maxEnrollments;
    protected List<Meeting> meetings;
    protected String title;
    protected String eid;
    protected boolean isLocked;
    
    protected boolean lazy_eid = false;
    
    // Transient holder for the framework group being decorated.
    private transient Group group;

    // To get the time zone from user. 
    private static final TimeService timeService = (TimeService)ComponentManager.get("org.sakaiproject.time.api.TimeService");
    
    /**
     * Convenience constructor to create a CourseSection with a single meeting.
     * 
     * @param course
     * @param title
     * @param uuid
     * @param category
     * @param maxEnrollments
     * @param location
     * @param startTime
     * @param endTime
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     */
    public CourseSectionImpl(Course course, String title, String uuid, String category,
    		Integer maxEnrollments, String location, Time startTime,
    		Time endTime, boolean monday, boolean tuesday,
    		boolean wednesday, boolean thursday, boolean friday, boolean saturday,
    		boolean sunday) {
    	
		this.course = course;
		this.title = title;
		this.uuid = uuid;
		this.category = category;
		this.maxEnrollments = maxEnrollments;
		this.meetings = new ArrayList<Meeting>();
		this.meetings.add(new MeetingImpl(location, startTime, endTime, monday,
				tuesday, wednesday, thursday, friday, saturday, sunday));
	}

	public CourseSectionImpl(Group group) {
		this.group = group;
		this.uuid = group.getReference();
		this.course = new CourseImpl(group.getContainingSite());
		this.title = group.getTitle();
		this.description = group.getDescription();
		this.isLocked = group.isLocked();

		ResourceProperties props = group.getProperties();
		this.category = props.getProperty(CourseSectionImpl.CATEGORY);
		this.meetings = new ArrayList<Meeting>();
		// We always start with a single empty meeting
		meetings.add(new MeetingImpl());
		String str = props.getProperty(MAX_ENROLLMENTS);
		if(StringUtils.trimToNull(str) != null) {
			try {
				this.maxEnrollments = Integer.valueOf(str);
			} catch(NumberFormatException nfe) {
				if(log.isDebugEnabled()) log.debug("can not parse integer property for " + CourseSectionImpl.MAX_ENROLLMENTS);
			}
		}
		
		// Parse the meetings for this group. Use a field that can't be null, such as "monday" (which must be T/F)
		long numMeetings = 0;
		String mondays = props.getProperty(CourseSectionImpl.MONDAY);
		if(mondays != null && !"".equals(mondays.trim())) {
			try {
				numMeetings = mondays.split(CourseSectionImpl.SEP_CHARACTER).length;
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
			switch (shiftDay(getIndexedStringProperty(i, props.getProperty(START_TIME)))) {
				case 0:
					meetings.add(new MeetingImpl(location, startTime, endTime, monday, tuesday, wednesday, thursday, friday, saturday, sunday));
					break;
				case 1:
					meetings.add(new MeetingImpl(location, startTime, endTime, sunday, monday, tuesday, wednesday, thursday, friday, saturday));
					break;
				case -1:
					meetings.add(new MeetingImpl(location, startTime, endTime, tuesday, wednesday, thursday, friday, saturday, sunday, monday));
					break;
				case 2:
					meetings.add(new MeetingImpl(location, startTime, endTime, saturday, sunday, monday, tuesday, wednesday, thursday, friday));
					break;
				case -2:
					meetings.add(new MeetingImpl(location, startTime, endTime, wednesday, thursday, friday, saturday, sunday, monday, tuesday));
					break;
				default:
					log.error("Can not change meeting days with time: "+getIndexedStringProperty(i, props.getProperty(START_TIME)));
			}
		}
		
	}

	private boolean getIndexedBooleanProperty(int index, String complexString) {
		String[] sa = StringUtils.splitPreserveAllTokens(complexString, CourseSectionImpl.SEP_CHARACTER);
		if(sa == null) {
			return false;
		}
		if(index >=sa.length) {
			log.debug("Can not get " + index + " index from string " + complexString);
			return false;
		}
		return Boolean.parseBoolean(sa[index]);
	}

	private String getIndexedStringProperty(int index, String complexString) {
		if(complexString == null || "".equals(complexString.trim())) {
			return null;
		}
		String[] sa = StringUtils.splitPreserveAllTokens(complexString, CourseSectionImpl.SEP_CHARACTER);
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
    	SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_DATE_TZ);
    	// Time zone from user
    	TimeZone userTz = timeService.getLocalTimeZone();
    	sdf.setTimeZone(userTz);

    	// Today at 0.00
    	Calendar date = new GregorianCalendar(userTz);    	    	
    	date.set(Calendar.HOUR_OF_DAY, 0);
    	date.set(Calendar.MINUTE, 0);
    	
    	// Add the RawOffset of server, to write REAL TIME in STRING detached from server
    	date.setTimeInMillis(date.getTimeInMillis()+time.getTime()+TimeZone.getDefault().getRawOffset());
    	sdf.setCalendar(date);
    	
    	return sdf.format(date.getTime());
    }

    public static final Time convertStringToTime(String str) {
    	if(StringUtils.trimToNull(str) == null) {
    		return null;
    	}
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_LONG);
        	return new Time(sdf.parse(str).getTime());
    	} catch (Exception e) {
    		
    		// Stored in other format, with date and time zone. 
    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_DATE_TZ);
    			
    			Calendar src = new GregorianCalendar();
    			src.setTime(sdf.parse(str));
    			src.setTimeInMillis(src.getTimeInMillis());
    			
    			TimeZone srcTz = sdf.getTimeZone();
    			TimeZone userTz = timeService.getLocalTimeZone();
    			TimeZone serverTz = TimeZone.getDefault();
    			
    			Calendar now = new GregorianCalendar(userTz);
    			
    			// STORED IN DAYLIGHT SAVING TIME and NOW IS STANDARD
    			if (srcTz.inDaylightTime(src.getTime()) && !srcTz.inDaylightTime(now.getTime())) 
    			{
    				src.setTimeInMillis(src.getTimeInMillis()+srcTz.getDSTSavings());
    			}
    			
    			// STORED IN STANDAR TIME and NOW IS DAYLIGHT SAVING TIME
    			if (srcTz.inDaylightTime(now.getTime()) && !srcTz.inDaylightTime(src.getTime())) 
    			{
    				src.setTimeInMillis(src.getTimeInMillis()-srcTz.getDSTSavings());
    			}
    			
    			// DO THE SAME IN SERVER TIMEZONE
    			if (serverTz.inDaylightTime(src.getTime()) && !serverTz.inDaylightTime(now.getTime())) 
    			{
    				src.setTimeInMillis(src.getTimeInMillis()-serverTz.getDSTSavings());
    			}
    			
    			if (serverTz.inDaylightTime(now.getTime()) && !serverTz.inDaylightTime(src.getTime())) 
    			{
    				src.setTimeInMillis(src.getTimeInMillis()+serverTz.getDSTSavings());
    			}
    			 
    			src.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
    			src.set(Calendar.YEAR, now.get(Calendar.YEAR));
    			src.set(Calendar.MONTH, now.get(Calendar.MONTH));

    			return new Time(src.getTimeInMillis()+userTz.getOffset(now.getTimeInMillis())-serverTz.getOffset(now.getTimeInMillis()));
    	    	
    		} catch (Exception ex) {	
    			if(log.isDebugEnabled()) log.debug("Unable to parse " + str);
    			return null;
    		}
    	}
    }
    
    
    /**
     * Check if converted time to the time zone of user is the previous day,
     * the same day or next day.  
     * @param startTime denotes the time set by user (in his own TimeZone)
     * @return if converted from stored time zone to user time zone get the previous day returns -1
     * 		   if converted from stored time zone to user time zone get the same day returns 0
     * 		   if converted from stored time zone to user time zone get the next day returns +1
     */
    private static final int shiftDay(String str)
    {    	
    	if(StringUtils.trimToNull(str) == null) {
    		return 0;
    	}
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_LONG);
    		sdf.parse(str);
        	return 0;
    	} catch (Exception e) {
    		
    		// Stored in other format, with date and time zone. 
    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_DATE_TZ);
    			
    			Calendar src = new GregorianCalendar();
    			src.setTime(sdf.parse(str));    			
    			
    			TimeZone srcTz = sdf.getTimeZone();
    			TimeZone userTz = timeService.getLocalTimeZone();
    			
    			Calendar user = new GregorianCalendar(userTz);
    			src.set(Calendar.DAY_OF_MONTH, user.get(Calendar.DAY_OF_MONTH));
    			src.set(Calendar.YEAR, user.get(Calendar.YEAR));
    			src.set(Calendar.MONTH, user.get(Calendar.MONTH));
    			    			
    			user.setTimeInMillis(src.getTimeInMillis());
    			src.setTimeZone(srcTz);
    			
    			int shift = user.get(Calendar.DAY_OF_MONTH) - src.get(Calendar.DAY_OF_MONTH);
    	    	
    	    	// Days from two differents months
    	    	if (shift > 8) {
    	    		src.add(Calendar.MONTH, -1);
    	    		shift-=src.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    	    	}
    	    	else if (shift < -8) {
    	    		user.add(Calendar.MONTH, -1);
    	    		shift+=user.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    	    	}
    	    	
    	    	return shift;
    	    	
    		} catch (Exception ex) {	
    			if(log.isDebugEnabled()) log.debug("Unable to parse " + str);
    			return 0;
    		}
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

    	// If we have a non-null eid, update the group.
    	if(StringUtils.trimToNull(eid) != null) {
    		props.addProperty(CourseSectionImpl.EID, eid);
    	}
    	
    	// Add the properties that containing the meeting metadata
    	StringBuilder locationBuffer = new StringBuilder();

    	// Ensure that we've got a meetings object
    	if(meetings == null) {
    		meetings = new ArrayList<>();
    	}
    	
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
    		// Ensure that the location has no SEP_CHARACTERs in it
    		String meetingLocation = meeting.getLocation();
    		if(meetingLocation == null) {
    			meetingLocation = "";
    		} else {
    			meetingLocation = meetingLocation.replaceAll(CourseSectionImpl.SEP_CHARACTER, "");
    		}
    		locationBuffer.append(meetingLocation);
    		if(iter.hasNext()) {
    			locationBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.LOCATION + " to " + locationBuffer.toString());
    	props.addProperty(CourseSectionImpl.LOCATION, locationBuffer.toString());

    	StringBuilder startTimeBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
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

    	StringBuilder endTimeBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
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
    		
    	StringBuilder mondayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	mondayBuffer.append(meeting.isMonday());
    		if(iter.hasNext()) {
    			mondayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.MONDAY + " to " + mondayBuffer.toString());
    	props.addProperty(CourseSectionImpl.MONDAY, mondayBuffer.toString());

    	StringBuilder tuesdayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	tuesdayBuffer.append(meeting.isTuesday());
    		if(iter.hasNext()) {
    			tuesdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.TUESDAY + " to " + tuesdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.TUESDAY, tuesdayBuffer.toString());

    	StringBuilder wednesdayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	wednesdayBuffer.append(Boolean.valueOf(meeting.isWednesday()));
    		if(iter.hasNext()) {
    			wednesdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.WEDNESDAY + " to " + wednesdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.WEDNESDAY, wednesdayBuffer.toString());

    	StringBuilder thursdayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	thursdayBuffer.append(Boolean.valueOf(meeting.isThursday()));
    		if(iter.hasNext()) {
    			thursdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.THURSDAY + " to " + thursdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.THURSDAY, thursdayBuffer.toString());

    	StringBuilder fridayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	fridayBuffer.append(Boolean.valueOf(meeting.isFriday()));
    		if(iter.hasNext()) {
    			fridayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.FRIDAY + " to " + fridayBuffer.toString());
    	props.addProperty(CourseSectionImpl.FRIDAY, fridayBuffer.toString());
    	
    	StringBuilder saturdayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
    		Meeting meeting = (Meeting)iter.next();
        	saturdayBuffer.append(Boolean.valueOf(meeting.isSaturday()));
    		if(iter.hasNext()) {
    			saturdayBuffer.append(CourseSectionImpl.SEP_CHARACTER);
    		}
    	}
    	if(log.isDebugEnabled()) log.debug("Setting group property " + CourseSectionImpl.SATURDAY+ " to " + saturdayBuffer.toString());
    	props.addProperty(CourseSectionImpl.SATURDAY, saturdayBuffer.toString());

    	StringBuilder sundayBuffer = new StringBuilder();
    	for(Iterator<Meeting> iter = meetings.iterator(); iter.hasNext();) {
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

	public String getEid() {

		if (!lazy_eid) {
			// Get the EID from the group.  If the EID property exists, use it.  If it doesn't
			// exist, but the group has a provider ID, copy the provider ID to the EID field.
			ResourceProperties props = group.getProperties();
			String groupEid = StringUtils.trimToNull(props.getProperty(CourseSectionImpl.EID));
			if(groupEid == null) {
				// Try the provider ID
				String providerId = StringUtils.trimToNull(group.getProviderGroupId());
				if(providerId != null) {
					// There is a provider id, so update the group and this section
					props.addProperty(CourseSectionImpl.EID, providerId);
					this.eid = providerId;
				}
			} else {
				this.eid = groupEid;
			}
			
			lazy_eid = true;
		}
		
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
		lazy_eid = true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Meeting> getMeetings() {
		return meetings;
	}

	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
	}

	public boolean isLocked(){
		return isLocked;
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
	public int compareTo(CourseSection other) {
		if(other == this) {
			return 0;
		}
		if(this.category != null && other.getCategory() == null) {
			return -1;
		} else if(this.category == null && other.getCategory() != null) {
			return 1;
		}
		if(this.category == null && other.getCategory() == null) {
			return this.title.toLowerCase().compareTo(other.getTitle().toLowerCase());
		}
		int categoryComparison = this.category.compareTo(other.getCategory());
		if(categoryComparison == 0) {
			return this.title.toLowerCase().compareTo(other.getTitle().toLowerCase());
		} else {
			return categoryComparison;
		}
		
	}
	
	public String toString() {
		return new ToStringBuilder(this).append(title).append(uuid).append(category).append(maxEnrollments).toString();
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
