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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.service.legacy.authzGroup.Member;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.entity.ResourcePropertiesEdit;
import org.sakaiproject.service.legacy.site.Group;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.user.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CourseSectionImpl implements CourseSection, Group, Serializable {

	private static final long serialVersionUID = 1L;
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
	
	private transient Group sectionInstance;
	private String sectionInstanceId;

	public CourseSectionImpl(Group section) {
		this.sectionInstance = section;
		this.sectionInstanceId = section.getId();
	}
	
	private void writeObject(ObjectOutputStream s) throws IOException {
	    s.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream s) throws IOException  {
	    try {
	    	s.defaultReadObject();
	    } catch (ClassNotFoundException e) {
	    	log.error("Could not de-serialize... ", e);
	    	return;
	    }
	    sectionInstance = SiteService.findGroup(sectionInstanceId);
	}
	
    public static final String convertTimeToString(Time time) {
    	if(time == null) {
    		return null;
    	}
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm");
    	return sdf.format(time);
    }

    public static final Time convertStringToTime(String str) {
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat("h:mm");
        	return new Time(sdf.parse(str).getTime());
    	} catch (Exception e) {
    		if(log.isDebugEnabled()) log.debug("Unable to parse " + str);
    		return null;
    	}
    }

    public Course getCourse() {
		Site site;
		try {
			site = SiteService.getSite(sectionInstance.getSiteId());
		} catch (IdUnusedException e) {
			log.error("could not find site with id = " + sectionInstance.getSiteId());
			return null;
		}
		return new CourseImpl(site);
	}
	
	/**
	 * The uuid is the Sakai Section's reference
	 */
	public String getUuid() {
		return sectionInstance.getReference(); 
	}
	
	public String getCategory() {
		return sectionInstance.getProperties().getProperty(CATEGORY);
	}

	public void setCategory(String category) {
		// TODO Set the category
	}

	public Time getStartTime() {
		String str = sectionInstance.getProperties().getProperty(START_TIME);
		return convertStringToTime(str);
	}

	public void setStartTime(Time startTime) {
		// TODO Set the end time
	}

	public Time getEndTime() {
		String str = sectionInstance.getProperties().getProperty(END_TIME);
		return convertStringToTime(str);
	}

	public void setEndTime(Time endTime) {
		// TODO Set the end time
	}

	public boolean isFriday() {
		String str = sectionInstance.getProperties().getProperty(FRIDAY);
		return "true".equals(str);
	}

	public void setFriday(boolean friday) {
		// TODO Set friday
	}

	public String getLocation() {
		return sectionInstance.getProperties().getProperty(LOCATION);
	}

	public void setLocation(String location) {
		// TODO Set the location
	}

	public Integer getMaxEnrollments() {
		String str = sectionInstance.getProperties().getProperty(MAX_ENROLLMENTS);
		return Integer.valueOf(str);
	}

	public void setMaxEnrollments(Integer maxEnrollments) {
		// TODO Set the max enrollments
	}

	public boolean isMonday() {
		String str = sectionInstance.getProperties().getProperty(MONDAY);
		return "true".equals(str);
	}

	public void setMonday(boolean monday) {
		// TODO Set monday
	}

	public boolean isSaturday() {
		String str = sectionInstance.getProperties().getProperty(SATURDAY);
		return "true".equals(str);
	}

	public void setSaturday(boolean saturday) {
		// TODO Set Saturday
	}

	public Group getSectionInstance() {
		return sectionInstance;
	}

	public void setSectionInstance(Group sectionInstance) {
		this.sectionInstance = sectionInstance;
	}

	public boolean isSunday() {
		String str = sectionInstance.getProperties().getProperty(SUNDAY);
		return "true".equals(str);
	}

	public void setSunday(boolean sunday) {
		// TODO Set sunday
	}

	public boolean isThursday() {
		String str = sectionInstance.getProperties().getProperty(THURSDAY);
		return "true".equals(str);
	}

	public void setThursday(boolean thursday) {
		// TODO Set thursday
	}

	public boolean isTuesday() {
		String str = sectionInstance.getProperties().getProperty(TUESDAY);
		return "true".equals(str);
	}

	public void setTuesday(boolean tuesday) {
		// TODO Set tuesday
	}

	public boolean isWednesday() {
		String str = sectionInstance.getProperties().getProperty(WEDNESDAY);
		return "true".equals(str);
	}

	public void setWednesday(boolean wednesday) {
		// TODO Set wednesday
	}
	
	public String getDescription() {
		String daySepChar = ",";
		String timeSepChar = "-";
		
		StringBuffer sb = new StringBuffer();
		
		// Days of the week
		List dayList = new ArrayList();
		if(isMonday())
			dayList.add("M");
		if(isTuesday())
			dayList.add("T");
		if(isWednesday())
			dayList.add("W");
		if(isThursday())
			dayList.add("Th");
		if(isFriday())
			dayList.add("F");
		if(isSaturday())
			dayList.add("Sa");
		if(isSunday())
			dayList.add("Su");

		for(Iterator iter = dayList.iterator(); iter.hasNext();) {
			sb.append(iter.next());
			if(iter.hasNext()) {
				sb.append(daySepChar);
			}
		}

		// Start time
		DateFormat df = new SimpleDateFormat("h:mm a");
		sb.append(" ");
		Time startTime = getStartTime();
		if(startTime != null) {
			sb.append(df.format(new Date(startTime.getTime())).toLowerCase());
		}

		// End time
		Time endTime = getEndTime();
		if(startTime != null &&
				endTime != null) {
			sb.append(timeSepChar);
		}

		if(endTime != null) {
			sb.append(df.format(new Date(endTime.getTime())).toLowerCase());
		}
		
		return sb.toString();
	}

	
	// Delegates from Site Service's Section
	
	public String getId() {
		return sectionInstance.getId();
	}

	public ResourceProperties getProperties() {
		return sectionInstance.getProperties();
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return sectionInstance.getPropertiesEdit();
	}

	public String getReference() {
		return sectionInstance.getReference();
	}

	public String getSiteId() {
		return sectionInstance.getSiteId();
	}

	public String getTitle() {
		return sectionInstance.getTitle();
	}

	public String getUrl() {
		return sectionInstance.getUrl();
	}

	public boolean isActiveEdit() {
		return sectionInstance.isActiveEdit();
	}

	public void setDescription(String description) {
		sectionInstance.setDescription(description);
	}

	public void setTitle(String title) {
		sectionInstance.setTitle(title);
	}

	public Element toXml(Document doc, Stack stack) {
		return sectionInstance.toXml(doc, stack);
	}

	public Site getContainingSite() {
		return sectionInstance.getContainingSite();
	}

	public void addMember(String userId, String roleId, boolean active, boolean provided) {
		sectionInstance.addMember(userId, roleId, active, provided);
	}

	public Role addRole(String id) throws IdUsedException {
		return sectionInstance.addRole(id);
	}

	public Role addRole(String id, Role other) throws IdUsedException {
		return sectionInstance.addRole(id, other);
	}

	public User getCreatedBy() {
		return sectionInstance.getCreatedBy();
	}

	public org.sakaiproject.service.legacy.time.Time getCreatedTime() {
		return sectionInstance.getCreatedTime();
	}

	public String getMaintainRole() {
		return sectionInstance.getMaintainRole();
	}

	public Member getMember(String userId) {
		return sectionInstance.getMember(userId);
	}

	public Set getMembers() {
		return sectionInstance.getMembers();
	}

	public User getModifiedBy() {
		return sectionInstance.getModifiedBy();
	}

	public org.sakaiproject.service.legacy.time.Time getModifiedTime() {
		return sectionInstance.getModifiedTime();
	}

	public String getProviderGroupId() {
		return sectionInstance.getProviderGroupId();
	}

	public Role getRole(String id) {
		return sectionInstance.getRole(id);
	}

	public Set getRoles() {
		return sectionInstance.getRoles();
	}

	public Role getUserRole(String userId) {
		return sectionInstance.getUserRole(userId);
	}

	public Set getUsers() {
		return sectionInstance.getUsers();
	}

	public Set getUsersHasRole(String role) {
		return sectionInstance.getUsersHasRole(role);
	}

	public Set getUsersIsAllowed(String function) {
		return sectionInstance.getUsersIsAllowed(function);
	}

	public boolean hasRole(String userId, String role) {
		return sectionInstance.hasRole(userId, role);
	}

	public boolean isAllowed(String userId, String function) {
		return sectionInstance.isAllowed(userId, function);
	}

	public boolean isEmpty() {
		return sectionInstance.isEmpty();
	}

	public void removeMember(String userId) {
		sectionInstance.removeMember(userId);
	}

	public void removeMembers() {
		sectionInstance.removeMembers();
	}

	public void removeRole(String role) {
		sectionInstance.removeRole(role);
	}

	public void removeRoles() {
		sectionInstance.removeRoles();
	}

	public void setMaintainRole(String role) {
		sectionInstance.setMaintainRole(role);
	}

	public void setProviderGroupId(String id) {
		sectionInstance.setProviderGroupId(id);
	}

	public int compareTo(Object o) {
		return sectionInstance.compareTo(o);
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
