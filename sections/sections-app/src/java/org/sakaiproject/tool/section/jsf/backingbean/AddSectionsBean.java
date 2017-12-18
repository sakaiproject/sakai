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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;
import org.sakaiproject.util.ResourceLoader;

/**
 * Controls the add sections page.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class AddSectionsBean extends CourseDependentBean implements SectionEditor, Serializable {
	private static final long serialVersionUID = 1L;

	private Integer numToAdd;
	private String category;
	private List<SelectItem> categoryItems;
	private List<SelectItem> numSectionsSelectItems;
	private List<CourseSection> sections;
	private String rowStyleClasses;
	private 	String elementToFocus;
	private transient boolean sectionsChanged;
    private String[] daysOfWeek = null;

	/**
	 * @inheritDoc
	 */
	public void init() {
		if(log.isDebugEnabled()) log.debug("sections = " + sections);
		if(log.isDebugEnabled()) log.debug("sectionsChanged = " + sectionsChanged);

		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager.getInstance();
		ServerConfigurationService serverConfigurationService = (ServerConfigurationService) cm.get(ServerConfigurationService.class); 

		int limit = serverConfigurationService.getInt("sections.maxgroups.category", 10);
		if (limit <= 0) limit = 10;

		numSectionsSelectItems = new ArrayList<SelectItem>(limit);
		for(int i = 0; i < limit;) {
			Integer currVal = ++i;
			numSectionsSelectItems.add(new SelectItem(currVal));
		}
		if(numToAdd == null) numToAdd = 1;
		
		if(sections == null || sectionsChanged) {
			if(log.isDebugEnabled()) log.debug("initializing add sections bean");
			List categories = getSectionCategories();
			populateSections();
			categoryItems = new ArrayList<SelectItem>();
			for(Iterator iter = categories.iterator(); iter.hasNext();) {
				String cat = (String)iter.next();
				categoryItems.add(new SelectItem(cat, getCategoryName(cat)));
			}
		}
        initDaysOfWeek();
	}

	/**
	 * Responds to a change in the sections selector in the UI.
	 * 
	 * @param event
	 */
	public void processChangeNumSections(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in number of sections to add");
		sectionsChanged = true;
	}
	
	public void processChangeSectionsCategory(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in category of sections to add");
		sectionsChanged = true;
	}
	
	public void processAddMeeting(ActionEvent action) {
		if(log.isDebugEnabled()) log.debug("processing an 'add meeting' action from " + this.getClass().getName());
		int index = Integer.parseInt(JsfUtil.getStringFromParam("sectionIndex"));
		sections.get(index).getMeetings().add(new LocalMeetingModel());
		elementToFocus = action.getComponent().getClientId(FacesContext.getCurrentInstance());
	}

	/**
	 * Populates the section collection and row css classes.
	 *
	 */
	private void populateSections() {
		if(log.isDebugEnabled()) log.debug("populating sections");
		Course course = getCourse();
		
		sections = new ArrayList<CourseSection>();
		StringBuilder rowClasses = new StringBuilder();
		if(StringUtils.trimToNull(category) != null) {
			if(log.isDebugEnabled()) log.debug("populating sections");
			String categoryName = getCategoryName(category);
			int offset = getSectionManager().getSectionsInCategory(getSiteContext(), category).size();
			for(int i=1; i<=numToAdd; i++) {
				LocalSectionModel section = new LocalSectionModel(course,  categoryName + (i+offset), category, null);
				section.getMeetings().add(new LocalMeetingModel());
				sections.add(section);
				if(i>1) {
					rowClasses.append("nextSectionRow");
				}
				if(i < numToAdd) {
					rowClasses.append(",");
				}
			}
			rowStyleClasses = rowClasses.toString();
		}
	}
	
	/**
	 * Checks whether a string is currently being used as a title for another section.
	 * 
	 * @param title
	 * @param existingSections
	 * @return
	 */
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
	
	/**
	 * Adds the sections, or generates validation messages for bad inputs.
	 * 
	 * @return
	 */
	public String addSections() {
		if(validationFails()) {
			setNotValidated(true);
			return "failure";
		}

		// Validation passed, so save the new sections
		String courseUuid = getCourse().getUuid();
		StringBuilder titles = new StringBuilder();
		String sepChar = JsfUtil.getLocalizedMessage("section_separator");
		
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			titles.append(sectionModel.getTitle());
			if(iter.hasNext()) {
				titles.append(sepChar);
				titles.append(" ");
			}
		}

		getSectionManager().addSections(courseUuid, sections);

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
	 * Since the validation and conversion rules rely on the *relative*
	 * values of one component to another, we can't use JSF validators and
	 * converters.  So we check everything here.
	 * 
	 * @return
	 */
	protected boolean validationFails() {
		Collection<CourseSection> existingSections = getAllSiteSections();

		// Keep track of whether a validation failure occurs
		boolean validationFailure = false;
		
		// We also need to keep track of whether an invalid time was entered,
		// so we can skip the time comparisons
		boolean invalidTimeEntered = false;

		int sectionIndex = 0;
		for(Iterator iter = sections.iterator(); iter.hasNext(); sectionIndex++) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			
			// Ensure that this title isn't being used by another section
			if(isDuplicateSectionTitle(sectionModel.getTitle(), existingSections)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + sectionModel.getTitle());
				String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":titleInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_add_failure_duplicate_title", new String[] {sectionModel.getTitle()}), componentId);
				validationFailure = true;
			}
			
			// Add this new section to the list of existing sections, so any other new sections don't conflict with this section's title
			existingSections.add(sectionModel);
			
			
			// Ensure that the user didn't choose to limit the size of the section without specifying a max size
			if(Boolean.TRUE.toString().equals(sectionModel.getLimitSize()) && sectionModel.getMaxEnrollments() == null) {
				String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":maxEnrollmentInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"sections_specify_limit"), componentId);
				validationFailure = true;
			}
			
			int meetingIndex = 0;
			for(Iterator meetingsIterator = sectionModel.getMeetings().iterator(); meetingsIterator.hasNext(); meetingIndex++) {
				LocalMeetingModel meeting = (LocalMeetingModel)meetingsIterator.next();
				if( ! meeting.isStartTimeDefault() && isInvalidTime(meeting.getStartTimeString())) {
					if(log.isDebugEnabled()) log.debug("Failed to add section... meeting start time " + meeting.getStartTimeString() + " is invalid");

					String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":meetingsTable:" + meetingIndex + ":startTime";

					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"javax.faces.convert.DateTimeConverter.CONVERSION"), componentId);
					validationFailure = true;
					invalidTimeEntered = true;
				}
				
				if( ! meeting.isEndTimeDefault() &&  isInvalidTime(meeting.getEndTimeString())) {
					if(log.isDebugEnabled()) log.debug("Failed to add section... meeting end time " + meeting.getEndTimeString() + " is invalid");
					String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":meetingsTable:" + meetingIndex + ":endTime";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"javax.faces.convert.DateTimeConverter.CONVERSION"), componentId);
					validationFailure = true;
					invalidTimeEntered = true;
				}

				// No need to check this if we already have invalid times
				if(!invalidTimeEntered && isEndTimeWithoutStartTime(meeting)) {
					if(log.isDebugEnabled()) log.debug("Failed to update section... start time without end time");
					String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":meetingsTable:" + meetingIndex + ":startTime";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"section_update_failure_end_without_start"), componentId);
					validationFailure = true;
				}
				
				if(isInvalidMaxEnrollments(sectionModel)) {
					if(log.isDebugEnabled()) log.debug("Failed to update section... max enrollments is not valid");
					String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":maxEnrollmentInput";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"javax.faces.validator.LongRangeValidator.MINIMUM", new String[] {"0"}), componentId);
					validationFailure = true;
				}
				
				// Don't bother checking if the time values are invalid
				if(!invalidTimeEntered && isEndTimeBeforeStartTime(meeting)) {
					if(log.isDebugEnabled()) log.debug("Failed to update section... end time is before start time");
					String componentId = "addSectionsForm:sectionTable:" + sectionIndex + ":meetingsTable:" + meetingIndex + ":endTime";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"section_update_failure_end_before_start"), componentId);
					validationFailure = true;
				}
			}
		}
		return validationFailure;
	}

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components, this method checks whether an end time has
	 * been entered without a start time.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	protected boolean isEndTimeWithoutStartTime(LocalMeetingModel meeting) {
		if(meeting.getStartTime() == null && meeting.getEndTime() != null) {
			if(log.isDebugEnabled()) log.debug("You can not set an end time without setting a start time.");
			return true;
		}
		return false;
	}

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components, this method checks whether two times, as
	 * expressed by string start and end times and booleans indicating am/pm,
	 * express times where the end time proceeds a start time.
	 * 
	 * @param meeting
	 * @return
	 */
	public static boolean isEndTimeBeforeStartTime(LocalMeetingModel meeting) {
		String startTime = null;
		if( ! meeting.isStartTimeDefault()) {
			startTime = meeting.getStartTimeString();
		}

		String endTime = null;
		if( ! meeting.isEndTimeDefault()) {
			endTime = meeting.getEndTimeString();
		}

		boolean startTimeAm = meeting.isStartTimeAm();
		boolean endTimeAm = meeting.isEndTimeAm();

		if(StringUtils.trimToNull(startTime) != null && StringUtils.trimToNull(endTime) != null) {
			Time start = JsfUtil.convertStringToTime(startTime, startTimeAm);
			Time end = JsfUtil.convertStringToTime(endTime, endTimeAm);
			if(start.after(end)) {
				if(log.isDebugEnabled()) log.debug("You can not set an end time earlier than the start time.");
				return true;
			}
		}

        if(StringUtils.trimToNull(startTime) != null && StringUtils.trimToNull(endTime) != null) {
			Time start = JsfUtil.convertStringToTime(startTime, startTimeAm);
			Time end = JsfUtil.convertStringToTime(endTime, endTimeAm);
			if(start.equals(end)) {
				if(log.isDebugEnabled()) log.debug("You can not set an end time that same as start time.");
				return true;
			}
		}
        return false;
	}
	
	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components, this method checks whether a string can
	 * represent a valid time.
	 * 
	 * Returns true if the string fails to represent a time.  Java's date formatters
	 * allow for impossible field values (eg hours > 12) so we do manual checks here.
	 * Ugh.
	 * 
	 * @param str The string that might represent a time.
	 * 
	 * @return
	 */
	protected boolean isInvalidTime(String str) {
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
	private static boolean outOfRange(String str, int len, int low, int high) {
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

	public List<SelectItem> getCategoryItems() {
		return categoryItems;
	}

	public List<SelectItem> getNumSectionsSelectItems() {
		return numSectionsSelectItems;
	}
	
	public List<CourseSection> getSections() {
		return sections;
	}

	public String getRowStyleClasses() {
		return rowStyleClasses;
	}

	public String getElementToFocus() {
		return elementToFocus;
	}

	public void setElementToFocus(String scrollDepth) {
		this.elementToFocus = scrollDepth;
	}

    public String getMonday() {
        return daysOfWeek[Calendar.MONDAY];
    }

    public String getTuesday() {
        return daysOfWeek[Calendar.TUESDAY];
    }

    public String getWednesday() {
        return daysOfWeek[Calendar.WEDNESDAY];
    }

    public String getThursday () {
        return daysOfWeek[Calendar.THURSDAY];
    }

    public String getFriday() {
        return daysOfWeek[Calendar.FRIDAY];
    }

    public String getSaturday() {
        return daysOfWeek[Calendar.SATURDAY];
    }

    public String getSunday() {
        return daysOfWeek[Calendar.SUNDAY];
    }

    protected void initDaysOfWeek(){
       ResourceLoader rl = new ResourceLoader();
       DateFormatSymbols dfs = new DateFormatSymbols(rl.getLocale());
       daysOfWeek = dfs.getWeekdays();
    }
}
