/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the add sections page.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AddSectionsBean extends CourseDependentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(AddSectionsBean.class);
	
	private int numToAdd;
	private String category;
	private List categoryItems;
	private List sections;
	private String rowStyleClasses;
	
	private transient boolean sectionsChanged;
	
	public void init() {
		if(sections == null || sectionsChanged) {
			if(log.isDebugEnabled()) log.debug("initializing add sections bean");
			List categories = getSectionCategories();
			populateSections();
			categoryItems = new ArrayList();
			for(Iterator iter = categories.iterator(); iter.hasNext();) {
				String cat = (String)iter.next();
				categoryItems.add(new SelectItem(cat, getCategoryName(cat)));
			}
		}
	}

	public void processChangeSections(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in sections to add");
		sectionsChanged = true;
	}
	
	private void populateSections() {
		sections = new ArrayList();
		StringBuffer rowClasses = new StringBuffer();
		if(StringUtils.trimToNull(category) != null) {
			if(log.isDebugEnabled()) log.debug("populating sections");
			int offset = getSectionManager().getSectionsInCategory(getSiteContext(), category).size();
			for(int i=0; i<numToAdd; i++) {
				sections.add(new LocalSectionModel(getCategoryName(category) + (i+1+offset)));
				rowClasses.append("sectionPadRow");
				if(i+1<numToAdd) {
					rowClasses.append(",");
				}
			}
			rowStyleClasses = rowClasses.toString();
		}
	}
	
	private boolean isDuplicateSectionTitle(String title, Collection existingSections) {
		for(Iterator iter = existingSections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			if(section.getTitle().equals(title)) {
				if(log.isDebugEnabled()) log.debug("Conflicting section name found: " + title);
				return true;
			}
		}
		return false;
	}
	
	public String addSections() {
		Collection existingSections = getAllSiteSections();

		// Since the validation and conversion rules rely on the *relative*
		// values of one component to another, we can't use JSF validators and
		// converters.  So we check everything here.
		boolean validationFailure = false;
		int index = 0;
		for(Iterator iter = sections.iterator(); iter.hasNext(); index++) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			
			// Ensure that this title isn't being used by another section
			if(isDuplicateSectionTitle(sectionModel.getTitle(), existingSections)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + sectionModel.getTitle());
				String componentId = "addSectionsForm:sectionTable_" + index + ":titleInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_add_failure_duplicate_title", new String[] {sectionModel.getTitle()}), componentId);
				validationFailure = true;
			}
			
			if(isInvalidTime(sectionModel.getStartTime())) {
				if(log.isDebugEnabled()) log.debug("Failed to add section... start time is invalid");
				String componentId = "addSectionsForm:sectionTable_" + index + ":startTime";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"javax.faces.convert.DateTimeConverter.CONVERSION"), componentId);
				validationFailure = true;
			}
			
			if(isInvalidTime(sectionModel.getEndTime())) {
				if(log.isDebugEnabled()) log.debug("Failed to add section... end time is invalid");
				String componentId = "addSectionsForm:sectionTable_" + index + ":endTime";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"javax.faces.convert.DateTimeConverter.CONVERSION"), componentId);
				validationFailure = true;
			}

			if(isEndTimeWithoutStartTime(sectionModel)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... start time without end time");
				String componentId = "addSectionsForm:sectionTable_" + index + ":startTime";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_update_failure_end_without_start"), componentId);
				validationFailure = true;
			}
			
			if(isInvalidMaxEnrollments(sectionModel)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... max enrollments is not valid");
				String componentId = "addSectionsForm:sectionTable_" + index + ":maxEnrollmentInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"javax.faces.validator.LongRangeValidator.MINIMUM", new String[] {"0"}), componentId);
				validationFailure = true;
			}
			
			if(isEndTimeBeforeStartTime(sectionModel)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... end time is before start time");
				String componentId = "addSectionsForm:sectionTable_" + index + ":endTime";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_update_failure_end_before_start"), componentId);
				validationFailure = true;
			}
		}
		
		if(validationFailure) {
			setNotValidated(true);
			return "failure";
		}

		// Validation passed, so save the new sections
		String courseUuid = getCourse().getUuid();
		StringBuffer titles = new StringBuffer();
		String sepChar = JsfUtil.getLocalizedMessage("section_separator");
		
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			titles.append(sectionModel.getTitle());
			if(iter.hasNext()) {
				titles.append(sepChar);
				titles.append(" ");
			}

			getSectionManager().addSection(courseUuid, sectionModel.getTitle(),
					category, sectionModel.getMaxEnrollments(), sectionModel.getLocation(),
					JsfUtil.convertStringToTime(sectionModel.getStartTime(), sectionModel.isStartTimeAm()),
					JsfUtil.convertStringToTime(sectionModel.getEndTime(), sectionModel.isEndTimeAm()),
					sectionModel.isMonday(), sectionModel.isTuesday(), sectionModel.isWednesday(),
					sectionModel.isThursday(), sectionModel.isFriday(), sectionModel.isSaturday(),
					sectionModel.isSunday());
		}
		String[] params = new String[3];
		params[0] = titles.toString();
		if(sections.size() == 1) {
			params[1] = JsfUtil.getLocalizedMessage("add_section_successful_singular");
			params[2] = JsfUtil.getLocalizedMessage("section_singular");
		} else {
			params[1] = JsfUtil.getLocalizedMessage("add_section_successful_plural");
			params[2] = JsfUtil.getLocalizedMessage("section_plural");
		}
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("add_section_successful", params));
		return "overview";
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

	private boolean isEndTimeWithoutStartTime(LocalSectionModel sectionModel) {
		if(sectionModel.getStartTime() == null & sectionModel.getEndTime() != null) {
			if(log.isDebugEnabled()) log.debug("You can not set an end time without setting a start time.");
			return true;
		}
		return false;
	}
	
	private boolean isEndTimeBeforeStartTime(LocalSectionModel sectionModel) {
		if(sectionModel.getStartTime() != null & sectionModel.getEndTime() != null) {
			Time start = JsfUtil.convertStringToTime(sectionModel.getStartTime(), sectionModel.isStartTimeAm());
			Time end = JsfUtil.convertStringToTime(sectionModel.getEndTime(), sectionModel.isEndTimeAm());
			if(start.after(end)) {
				if(log.isDebugEnabled()) log.debug("You can not set an end time earlier than the start time.");
				return true;
			}
		}
		return false;
	}

	
	private boolean isInvalidMaxEnrollments(LocalSectionModel sectionModel) {
		return sectionModel.getMaxEnrollments() != null && sectionModel.getMaxEnrollments().intValue() < 0;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getNumToAdd() {
		return numToAdd;
	}

	public void setNumToAdd(int numToAdd) {
		this.numToAdd = numToAdd;
	}

	public List getCategoryItems() {
		return categoryItems;
	}
	
	public class LocalSectionModel implements Serializable {
		private static final long serialVersionUID = 1L;

		public LocalSectionModel() {}
		public LocalSectionModel(String title) {this.title = title;}
		
		private String title, location, startTime, endTime;
	    private Integer maxEnrollments;
		private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
		private boolean startTimeAm, endTimeAm;

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
		public boolean isWednesday() {
			return wednesday;
		}
		public void setWednesday(boolean wednesday) {
			this.wednesday = wednesday;
		}
	}

	public List getSections() {
		return sections;
	}

	public String getRowStyleClasses() {
		return rowStyleClasses;
	}
}
