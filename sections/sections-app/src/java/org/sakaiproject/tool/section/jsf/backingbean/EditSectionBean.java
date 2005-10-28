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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit and delete sections pages.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class EditSectionBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EditSectionBean.class);
	
	private String sectionUuid;
	private String title;
	private String location;
	private Integer maxEnrollments;
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
		if(sectionUuid == null || isNotValidated()) {
			String sectionUuidFromParam = JsfUtil.getStringFromParam("sectionUuid");
			if(sectionUuidFromParam != null) {
				sectionUuid = sectionUuidFromParam;
			}
			CourseSection section = getSectionManager().getSection(sectionUuid);
			SimpleDateFormat sdf = new SimpleDateFormat(JsfUtil.TIME_PATTERN_DISPLAY);
			
			title = section.getTitle();
			location = section.getLocation();
			maxEnrollments = section.getMaxEnrollments();
			monday = section.isMonday();
			tuesday = section.isTuesday();
			wednesday = section.isWednesday();
			thursday = section.isThursday();
			friday = section.isFriday();
			saturday = section.isSaturday();
			sunday = section.isSunday();
			if(section.getStartTime() != null) {
				startTime = sdf.format(section.getStartTime());
				Calendar cal = new GregorianCalendar();
				cal.setTime(section.getStartTime());
				startTimeAm = cal.get(Calendar.HOUR_OF_DAY) < 11;
			}
			if(section.getEndTime() != null) {
				endTime = sdf.format(section.getEndTime());
				Calendar cal = new GregorianCalendar();
				cal.setTime(section.getEndTime());
				endTimeAm = cal.get(Calendar.HOUR_OF_DAY) < 11;
			}
		}
	}

	public String update() {
		// Since the validation and conversion rules rely on the *relative*
		// values of one component to another, we can't use JSF validators and
		// converters.  So we check everything here.
		boolean validationFailure = false;
		
		// Ensure that this title isn't being used by another section
		if(isDuplicateSectionTitle()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + title);
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_duplicate_title", new String[] {title}), "editSectionForm:titleInput");
			validationFailure = true;
		}
		
		if(isInvalidTime(startTime)) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... start time is invalid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.convert.DateTimeConverter.CONVERSION"), "editSectionForm:startTime");
			validationFailure = true;
		}
		
		if(isInvalidTime(endTime)) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... end time is invalid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.convert.DateTimeConverter.CONVERSION"), "editSectionForm:endTime");
			validationFailure = true;
		}

		if(isEndTimeWithoutStartTime()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... start time without end time");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_end_without_start"), "editSectionForm:startTime");
			validationFailure = true;
		}
		
		if(isInvalidMaxEnrollments()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... max enrollments is not valid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.validator.LongRangeValidator.MINIMUM", new String[] {"0"}),
					"editSectionForm:maxEnrollmentInput");
			validationFailure = true;
		}

		if(isEndTimeBeforeStartTime()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... end time is before start time");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_end_before_start"), "editSectionForm:endTime");
			validationFailure = true;
		}

		if(validationFailure) {
			return null;
		}
		
		// Perform the update
		getSectionManager().updateSection(sectionUuid, title, maxEnrollments,
				location, JsfUtil.convertStringToTime(startTime, startTimeAm),
				JsfUtil.convertStringToTime(endTime, endTimeAm), monday, tuesday,
				wednesday, thursday, friday, saturday, sunday);
		
		// Add a success message
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"section_update_successful", new String[] {title}));
		
		// Add a warning if max enrollments has been exceeded
		CourseSection section = getSectionManager().getSection(sectionUuid);
		Integer maxEnrollments = section.getMaxEnrollments();
		int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());
		if(maxEnrollments != null && totalEnrollments > maxEnrollments.intValue()) {
			JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
					"edit_student_over_max_warning", new String[] {
							section.getTitle(),
							Integer.toString(totalEnrollments),
							Integer.toString(totalEnrollments - maxEnrollments.intValue()) }));
		}
		return "overview";
	}
		
	/**
	 * Returns true if the title is a duplicate of another section.
	 * 
	 * @return
	 */
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
	
	/**
	 * Returns true if the string fails to represent a time.
	 * 
	 * @param str
	 * @return
	 */
	private boolean isInvalidTime(String str) {
		// Java's date formatters allow for impossible field values (eg hours > 12)
		// so we do manual checks here.  Ugh.
		if(StringUtils.trimToNull(str) == null) {
			// Empty strings are ok
			return false;
		}
		
		if(str.indexOf(':') != -1) {
			// This is a fully specified time
			String[] sa = str.split(":");
			if(sa.length != 2) {
				if(log.isDebugEnabled()) log.debug("This is not a valid time... it has more than 1 ':'.");
				return true;
			}
			return outOfRange(sa[0], 2, 1, 12) || outOfRange(sa[1], 2, 0, 59);
		} else {
			return outOfRange(str, 2, 1, 12);
		}
	}

	/**
	 * Returns true if the string is longer than len, less than low, or higher than high.
	 * 
	 * @param str The string
	 * @param len The max length of the string
	 * @param low The lowest possible numeric value
	 * @param high The highest possible numeric value
	 * @return
	 */
	private boolean outOfRange(String str, int len, int low, int high) {
		if(str.length() > len) {
			return true;
		}
		try {
			int i = Integer.parseInt(str);
			if(i < low || i > high) {
				return true;
			}
		} catch (NumberFormatException nfe) {
			if(log.isDebugEnabled()) log.debug("time must be a number");
			return true;
		}
		return false;
	}

	private boolean isEndTimeWithoutStartTime() {
		if(startTime == null & endTime != null) {
			if(log.isDebugEnabled()) log.debug("You can not set an end time without setting a start time.");
			return true;
		}
		return false;
	}
	
	private boolean isEndTimeBeforeStartTime() {
		if(startTime != null & endTime != null) {
			Time start = JsfUtil.convertStringToTime(startTime, startTimeAm);
			Time end = JsfUtil.convertStringToTime(endTime, endTimeAm);
			if(start.after(end)) {
				if(log.isDebugEnabled()) log.debug("You can not set an end time earlier than the start time.");
				if(log.isDebugEnabled()) log.debug("start time = " + start.getTime());
				if(log.isDebugEnabled()) log.debug("end time = " + end.getTime());
				return true;
			}
		}
		return false;
	}

	private boolean isInvalidMaxEnrollments() {
		return maxEnrollments != null && maxEnrollments.intValue() < 0;
	}
	
	public String getDays() {
		CourseSection section = getSectionManager().getSection(sectionUuid);
		CourseSectionDecorator decorator = new CourseSectionDecorator(section, null);
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
