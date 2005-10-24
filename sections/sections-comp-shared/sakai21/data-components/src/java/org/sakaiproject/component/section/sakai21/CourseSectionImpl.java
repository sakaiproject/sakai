/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.sakai21;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.site.Group;

public class CourseSectionImpl implements CourseSection, Serializable {

	private static final long serialVersionUID = 1L;
	private static final String TIME_FORMAT_LONG = "h:mm a";
	private static final Log log = LogFactory.getLog(CourseSectionImpl.class);
	
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
    protected String location;
    protected Integer maxEnrollments;
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	private Time startTime;
	private Time endTime;

    // Fields shared between the two interfaces
	protected String id;
    protected String title;

    // Transient holder for the framework group being decorated.
    private transient Group group;
    
	public CourseSectionImpl(Group group) {
		this.group = group;
		ResourceProperties props = group.getProperties();
		id = group.getId();
		uuid = group.getReference();
		title = group.getTitle();
		course = new CourseImpl(group.getContainingSite());
		category = props.getProperty(CourseSectionImpl.CATEGORY);
		location = props.getProperty(CourseSectionImpl.LOCATION);
		endTime = CourseSectionImpl.convertStringToTime(props.getProperty(END_TIME));
		startTime = CourseSectionImpl.convertStringToTime(props.getProperty(START_TIME));
		String str = props.getProperty(MAX_ENROLLMENTS);

		if(StringUtils.trimToNull(str) != null) {
			try {
				maxEnrollments = Integer.valueOf(str);
			} catch(NumberFormatException nfe) {
				if(log.isDebugEnabled()) log.debug("can not parse integer property for " + CourseSectionImpl.MAX_ENROLLMENTS);
			}
		}
		try {
			monday = props.getBooleanProperty(CourseSectionImpl.MONDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.MONDAY);
		}
		try {
			tuesday = props.getBooleanProperty(CourseSectionImpl.TUESDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.TUESDAY);
		}
		try {
			wednesday = props.getBooleanProperty(CourseSectionImpl.WEDNESDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.WEDNESDAY);
		}
		try {
			thursday = props.getBooleanProperty(CourseSectionImpl.THURSDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.THURSDAY);
		}
		try {
			friday = props.getBooleanProperty(CourseSectionImpl.FRIDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.FRIDAY);
		}
		try {
			saturday = props.getBooleanProperty(CourseSectionImpl.SATURDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.SATURDAY);
		}
		try {
			sunday = props.getBooleanProperty(CourseSectionImpl.SUNDAY);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("can not parse boolean property for " + CourseSectionImpl.SUNDAY);
		}
		
		// Always generate the description last, so the fields are properly set
		description = generateDescription();

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

    private String generateDescription() {
		String daySepChar = ",";
		String timeSepChar = "-";
		
		StringBuffer sb = new StringBuffer();
		
		// Days of the week
		List dayList = new ArrayList();
		if(monday)
			dayList.add("M");
		if(tuesday)
			dayList.add("T");
		if(wednesday)
			dayList.add("W");
		if(thursday)
			dayList.add("Th");
		if(friday)
			dayList.add("F");
		if(saturday)
			dayList.add("Sa");
		if(sunday)
			dayList.add("Su");

		for(Iterator iter = dayList.iterator(); iter.hasNext();) {
			sb.append(iter.next());
			if(iter.hasNext()) {
				sb.append(daySepChar);
			}
		}

		// Start time
		DateFormat df = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_LONG);
		sb.append(" ");
		if(startTime != null) {
			sb.append(df.format(new Date(startTime.getTime())).toLowerCase());
		}

		// End time
		if(startTime != null &&
				endTime != null) {
			sb.append(timeSepChar);
		}
		if(endTime != null) {
			sb.append(df.format(new Date(endTime.getTime())).toLowerCase());
		}
		return sb.toString();
    }

    /**
     * Decorates the framework's section (group) with metadata from this CourseSection.
     * 
     * @param group The framework group
     */
    public void decorateSection(Group group) {
    	ResourceProperties props = group.getProperties();
    	SimpleDateFormat sdf = new SimpleDateFormat(CourseSectionImpl.TIME_FORMAT_LONG);
    	group.setTitle(title);
    	group.setDescription(description);
    	props.addProperty(CourseSectionImpl.CATEGORY, category);
    	props.addProperty(CourseSectionImpl.LOCATION, location);
    	if(startTime == null) {
    		props.removeProperty(CourseSectionImpl.START_TIME);
    	} else {
        	props.addProperty(CourseSectionImpl.START_TIME, sdf.format(startTime));
    	}
    	if(endTime == null) {
    		props.removeProperty(CourseSectionImpl.END_TIME);
    	} else {
        	props.addProperty(CourseSectionImpl.END_TIME, sdf.format(endTime));
    	}
    	if(maxEnrollments == null) {
    		props.removeProperty(CourseSectionImpl.MAX_ENROLLMENTS);
    	} else {
    		props.addProperty(CourseSectionImpl.MAX_ENROLLMENTS, maxEnrollments.toString());
    	}
    	props.addProperty(CourseSectionImpl.MONDAY, Boolean.toString(monday));
    	props.addProperty(CourseSectionImpl.TUESDAY, Boolean.toString(tuesday));
    	props.addProperty(CourseSectionImpl.WEDNESDAY, Boolean.toString(wednesday));
    	props.addProperty(CourseSectionImpl.THURSDAY, Boolean.toString(thursday));
    	props.addProperty(CourseSectionImpl.FRIDAY, Boolean.toString(friday));
    	props.addProperty(CourseSectionImpl.SATURDAY, Boolean.toString(saturday));
    	props.addProperty(CourseSectionImpl.SUNDAY, Boolean.toString(sunday));
    }
    
	/**
	 * Sets the fields for an update so we can easily call decorateSection.
	 * 
	 * @param title
	 * @param location
	 * @param maxEnrollments
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
    public void setUpdateFields(String title, String location, Integer maxEnrollments, Time startTime,
			Time endTime, boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
			boolean friday, boolean saturday, boolean sunday) {
		this.title = title;
		this.location = location;
		this.maxEnrollments = maxEnrollments;
		this.startTime = startTime;
		this.endTime = endTime;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
		
    	this.description = generateDescription();
	}

    /**
	 * Sets the fields for an add section operation so we can easily call decorateSection.
     * 
     * @param category
     * @param title
     * @param location
     * @param maxEnrollments
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
    public void setAddFields(String category, String title, String location, Integer maxEnrollments,
    		Time startTime, Time endTime, boolean monday, boolean tuesday, boolean wednesday,
    		boolean thursday, boolean friday, boolean saturday, boolean sunday) {
    	setUpdateFields(title, location, maxEnrollments, startTime, endTime, monday,
    			tuesday, wednesday, thursday, friday, saturday, sunday);
    	this.category = category;
    	this.description = generateDescription();
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

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public boolean isFriday() {
		return friday;
	}

	public void setFriday(boolean friday) {
		this.friday = friday;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getMaxEnrollments() {
		return maxEnrollments;
	}

	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}

	public boolean isMonday() {
		return monday;
	}

	public void setMonday(boolean monday) {
		this.monday = monday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public boolean isSunday() {
		return sunday;
	}

	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
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



/**********************************************************************************
 * $Id$
 *********************************************************************************/
