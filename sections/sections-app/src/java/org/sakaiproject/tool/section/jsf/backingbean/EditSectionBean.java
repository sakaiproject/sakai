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

import javax.faces.context.FacesContext;

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
	private int maxEnrollments;
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	
	private String startTime;
	private String endTime;
	private boolean startTimeAm;
	private boolean endTimeAm;
	
	public void init() {
		// Get the section to edit
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
		startTimeAm = section.isStartTimeAm();
		endTimeAm = section.isEndTimeAm();
	}

	public String update() {
		getSectionManager().updateSection(sectionUuid, title, maxEnrollments,
				location, startTime, startTimeAm, endTime, endTimeAm, monday, tuesday,
				wednesday, thursday, friday, saturday, sunday);
		JsfUtil.addRedirectSafeMessage(JsfUtil.getLocalizedMessage(
				"section_update_successful", new String[] {title}));
		return "overview";
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

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
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

	public int getMaxEnrollments() {
		return maxEnrollments;
	}

	public void setMaxEnrollments(int maxEnrollments) {
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

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
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
