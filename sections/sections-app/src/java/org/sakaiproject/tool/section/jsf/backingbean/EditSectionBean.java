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
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit and delete sections pages.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class EditSectionBean extends AddSectionsBean implements SectionEditor, Serializable {

	private static final long serialVersionUID = 1L;

	private String sectionUuid;
	private LocalSectionModel section;
	private transient String elementToFocus;

	/** A list composed of a single section.  This is used so we can share UI code with AddSections */
	private List<CourseSection> sections;
	

	/**
	 * @inheritDoc
	 */
	public void init() {
		// TODO Remove this code.  Replace it with a request param in faces-config
		if(sectionUuid == null || isNotValidated()) {
			String sectionUuidFromParam = JsfUtil.getStringFromParam("sectionUuid");
			if(sectionUuidFromParam != null) {
				sectionUuid = sectionUuidFromParam;
			}
			CourseSection sectionFromService = getSectionManager().getSection(sectionUuid);
			section = new LocalSectionModel(sectionFromService);
			sections = new ArrayList<CourseSection>();
			sections.add(section);
		}
        initDaysOfWeek();
	}

	public void processAddMeeting(ActionEvent action) {
		if(log.isDebugEnabled()) log.debug("processing an 'add meeting' action from " + this.getClass().getName());
		section.getMeetings().add(new LocalMeetingModel());
		elementToFocus = action.getComponent().getClientId(FacesContext.getCurrentInstance());
	}
	
	/**
	 * Since the validation and conversion rules rely on the *relative*
	 * values of one component to another, we can't use JSF validators and
	 * converters.  So we check everything here.
	 * 
	 * @return
	 */
	protected boolean validationFails() {
		
		boolean validationFailure = false;
		
		// We also need to keep track of whether an invalid time was entered,
		// so we can skip the time comparisons
		boolean invalidTimeEntered = false;

		// Ensure that this title isn't being used by another section
		if(isDuplicateSectionTitle()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + section.getTitle());
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_duplicate_title", new String[] {section.getTitle()}), "editSectionForm:titleInput");
			validationFailure = true;
		}

		// Ensure that the max enrollments is a number >= 0.
		if(isInvalidMaxEnrollments()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... max enrollments is not valid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.validator.LongRangeValidator.MINIMUM", new String[] {"0"}),
					"editSectionForm:maxEnrollmentInput");
			validationFailure = true;
		}

        // Ensure that the user didn't choose to limit the size of the section without specifying a max size
			if(Boolean.TRUE.toString().equals(section.getLimitSize()) && section.getMaxEnrollments() == null) {
				String componentId = "editSectionForm:sectionTable:" + 0 + ":maxEnrollmentInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"sections_specify_limit"), componentId);
				validationFailure = true;
			}


		// Ensure that the times entered in the meetings are valid, and that they end after they start
		int meetingIndex = 0;
		for(Iterator iter = section.getMeetings().iterator(); iter.hasNext(); meetingIndex++) {
			LocalMeetingModel meeting = (LocalMeetingModel)iter.next();
			if( ! meeting.isStartTimeDefault() && super.isInvalidTime(meeting.getStartTimeString())) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... start time is invalid");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"javax.faces.convert.DateTimeConverter.CONVERSION"), "editSectionForm:sectionTable:0:meetingsTable:" + 0 + ":startTime");
				validationFailure = true;
				invalidTimeEntered = true;
			}
			
			if( ! meeting.isEndTimeDefault() && super.isInvalidTime(meeting.getEndTimeString())) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... end time is invalid");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"javax.faces.convert.DateTimeConverter.CONVERSION"), "editSectionForm:sectionTable:0:meetingsTable:" + 0 + ":endTime");
				validationFailure = true;
				invalidTimeEntered = true;
			}

			if(!invalidTimeEntered && super.isEndTimeWithoutStartTime(meeting)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... start time without end time");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_update_failure_end_without_start"), "editSectionForm:sectionTable:0:meetingsTable:" + 0 + ":startTime");
				validationFailure = true;
			}
			
			if(!invalidTimeEntered && super.isEndTimeBeforeStartTime(meeting)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... end time is before start time");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_update_failure_end_before_start"), "editSectionForm:sectionTable:0:meetingsTable:" + 0 + ":endTime");
				validationFailure = true;
			}
		}

		return validationFailure;
	}

	/**
	 * Updates the section in persistence.
	 * 
	 * @return
	 */
	public String update() {
		if(log.isInfoEnabled()) log.info("Updating section " + sectionUuid);
		
		if(validationFails()) {
			return null;
		}
		
		// Perform the update
		
		getSectionManager().updateSection(sectionUuid, section.getTitle(),
				section.getMaxEnrollments(), section.getMeetings());
		
		// Add a success message
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"section_update_successful", new String[] {section.getTitle()}));
		
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
			CourseSection sec = (CourseSection)iter.next();
			// Skip this section, since it is OK for it to keep the same title
			if(sec.getUuid().equals(sectionUuid)) {
				continue;
			}
			if(sec.getTitle().equals(section.getTitle())) {
				if(log.isDebugEnabled()) log.debug("Conflicting section name found.");
				return true;
			}
		}
		return false;
	}

	private boolean isInvalidMaxEnrollments() {
		return section.getMaxEnrollments() != null && section.getMaxEnrollments().intValue() < 0;
	}

// TODO What was this method used for?

//	public String getDays() {
//		CourseSection section = getSectionManager().getSection(sectionUuid);
//		CourseSectionDecorator decorator = new CourseSectionDecorator(section, null);
//		return decorator.getMeetingDays();
//	}
	
	public String getSectionUuid() {
		return sectionUuid;
	}
	public void setSectionUuid(String sectionUuid) {
		this.sectionUuid = sectionUuid;
	}

	public LocalSectionModel getSection() {
		return section;
	}

	public void setSection(LocalSectionModel section) {
		this.section = section;
	}
	
	public List<CourseSection> getSections() {
		return sections;
	}

	/**
	 * Gets the css to use in the table generated for display.  Needed for sharing IU
	 * code with AddSections.
	 * 
	 * @return
	 */
	public String getRowStyleClasses() {
		return "sectionPadRow";
	}

	/**
	 * This method is needed so we can share UI code with AddSections.
	 * @return
	 */
	public List getMeetings() {
		return section.getMeetings();
	}

	public String getElementToFocus() {
		return elementToFocus;
	}

	public void setElementToFocus(String scrollDepth) {
		this.elementToFocus = scrollDepth;
	}

}
