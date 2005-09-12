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

package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

public class EditSectionBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(EditSectionBean.class);
	
	private String sectionUuid;
	private String title;
	private String category;
	private String location;
	private Integer maxEnrollments;
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	
	private Date startTime;
	private Date endTime;
	private boolean startTimeAm;
	private boolean endTimeAm;
	
	public void init() {
		if(sectionUuid == null || isNotValidated()) {
			String sectionUuidFromParam = JsfUtil.getStringFromParam("sectionUuid");
			if(sectionUuidFromParam != null) {
				sectionUuid = sectionUuidFromParam;
			}
			CourseSection section = getSectionManager().getSection(sectionUuid);

			title = section.getTitle();
			category = section.getCategory();
			location = section.getLocation();
			maxEnrollments = section.getMaxEnrollments();
			monday = section.isMonday();
			tuesday = section.isTuesday();
			wednesday = section.isWednesday();
			thursday = section.isThursday();
			friday = section.isFriday();
			saturday = section.isSaturday();
			sunday = section.isSunday();
			startTime = section.getStartTime();
			endTime = section.getEndTime();
			if(section.getStartTime() != null) {
				startTimeAm = section.getStartTime().getHours() < 11;
			}
			if(section.getEndTime() != null) {
				endTimeAm = section.getEndTime().getHours() < 11;
			}
		}
	}

	public String update() {
		if(isDuplicateSectionTitle()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + title);
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_duplicate_title", new String[] {title}), "editSectionForm:titleInput");
			return "failure";
		}
		getSectionManager().updateSection(sectionUuid, title, maxEnrollments,
				location, getDateAsTime(true), getDateAsTime(false), monday, tuesday,
				wednesday, thursday, friday, saturday, sunday);
		JsfUtil.addRedirectSafeMessage(JsfUtil.getLocalizedMessage(
				"section_update_successful", new String[] {title}));
		return "overview";
	}
	
	/**
	 * The JSF DateTimeConverter can not convert into java.sql.Time.  So we do
	 * the conversion manually from a java.util.Date.
	 * 
	 * @param useStartTime Whether to get the Time for the startTime field.
	 * @return
	 */
	private Time getDateAsTime(boolean useStartTime) {
		if(useStartTime) {
			if(startTime == null) {
				return null;
			}
			if(startTimeAm) {
				// Check to make sure that the hours are indeed am hours
				if(startTime.getHours() > 11) {
					startTime.setHours(startTime.getHours() - 12);
				}
			} else {
				if(startTime.getHours() < 11) {
					startTime.setHours(startTime.getHours() + 12);
				}
			}
			return new Time(startTime.getTime());
		} else {
			if(endTime == null) {
				return null;
			}
			if(endTimeAm) {
				// Check to make sure that the hours are indeed am hours
				if(endTime.getHours() > 11) {
					endTime.setHours(endTime.getHours() - 12);
				}
			} else {
				endTime.setHours(endTime.getHours() + 12);
			}
			return new Time(endTime.getTime());
		}
	}

	private boolean isDuplicateSectionTitle() {
		for(Iterator iter = getAllSiteSections().iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			// Skip this section, since it is OK for it to keep the same title
			if(section.getUuid().equals(sectionUuid)) {
				continue;
			}
			if(section.getTitle().equals(title)) {
				if(log.isDebugEnabled()) log.debug("Conflicting section name found.");
				return true;
			}
		}
		return false;
	}
	
	public String delete() {
		getSectionManager().disbandSection(sectionUuid);
		return "overview";
	}
	
	public String getDays() {
		CourseSection section = getSectionManager().getSection(sectionUuid);
		CourseSectionDecorator decorator = new CourseSectionDecorator(section, getCategoryName(category));
		return decorator.getMeetingDays();
	}
	
	public String getSectionUuid() {
		return sectionUuid;
	}
	public void setSectionUuid(String sectionUuid) {
		this.sectionUuid = sectionUuid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public boolean isEndTimeAm() {
		return endTimeAm;
	}

	public void setEndTimeAm(boolean endTimeAm) {
		this.endTimeAm = endTimeAm;
	}

	public boolean isFriday() {
		return friday;
	}

	public void setFriday(boolean friday) {
		this.friday = friday;
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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public boolean isStartTimeAm() {
		return startTimeAm;
	}

	public void setStartTimeAm(boolean startTimeAm) {
		this.startTimeAm = startTimeAm;
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

	public boolean isTuesday() {
		return tuesday;
	}

	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
