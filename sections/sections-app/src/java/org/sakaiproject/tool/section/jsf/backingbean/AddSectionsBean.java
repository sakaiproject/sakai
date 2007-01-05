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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the add sections page.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AddSectionsBean extends CourseDependentBean implements SectionEditor, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(AddSectionsBean.class);
	
	private int numToAdd;
	private String category;
	private List<SelectItem> categoryItems;
	private List<LocalSectionModel> sections;
	private String rowStyleClasses;
	private 	String elementToFocus;
	private transient boolean sectionsChanged;
	
	/**
	 * @inheritDoc
	 */
	public void init() {
		if(log.isDebugEnabled()) log.debug("sections = " + sections);
		if(log.isDebugEnabled()) log.debug("sectionsChanged = " + sectionsChanged);
		
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
	}

	/**
	 * Responds to a change in the sections selector in the UI.
	 * 
	 * @param event
	 */
	public void processChangeSections(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in sections to add");
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
		sections = new ArrayList<LocalSectionModel>();
		StringBuffer rowClasses = new StringBuffer();
		if(StringUtils.trimToNull(category) != null) {
			if(log.isDebugEnabled()) log.debug("populating sections");
			int offset = getSectionManager().getSectionsInCategory(getSiteContext(), category).size();
			for(int i=1; i<=numToAdd; i++) {
				LocalSectionModel section = new LocalSectionModel(getCategoryName(category) + (i+offset));
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
		StringBuffer titles = new StringBuffer();
		String sepChar = JsfUtil.getLocalizedMessage("section_separator");
		
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			titles.append(sectionModel.getTitle());
			if(iter.hasNext()) {
				titles.append(sepChar);
				titles.append(" ");
			}

			List<Meeting> meetings = new ArrayList<Meeting>();
			for(Iterator<Meeting> meetingIter = sectionModel.getMeetings().iterator(); meetingIter.hasNext();) {
				Meeting meeting = meetingIter.next();
				meetings.add(meeting);
			}
			getSectionManager().addSection(courseUuid, sectionModel.getTitle(),
					category,  sectionModel.getMaxEnrollments(), meetings);
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
	 * Since the validation and conversion rules rely on the *relative*
	 * values of one component to another, we can't use JSF validators and
	 * converters.  So we check everything here.
	 * 
	 * @return
	 */
	protected boolean validationFails() {
		Collection existingSections = getAllSiteSections();

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
				String componentId = "addSectionsForm:sectionTable_" + sectionIndex + ":titleInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_add_failure_duplicate_title", new String[] {sectionModel.getTitle()}), componentId);
				validationFailure = true;
			}
			int meetingIndex = 0;
			for(Iterator meetingsIterator = sectionModel.getMeetings().iterator(); meetingsIterator.hasNext(); meetingIndex++) {
				LocalMeetingModel meeting = (LocalMeetingModel)meetingsIterator.next();
				if(JsfUtil.isInvalidTime(meeting.getStartTimeString())) {
					if(log.isDebugEnabled()) log.debug("Failed to add section... meeting start time " + meeting.getStartTimeString() + " is invalid");

					String componentId = "addSectionsForm:sectionTable_" + sectionIndex + ":meetingsTable_" + meetingIndex + ":startTime";

					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"javax.faces.convert.DateTimeConverter.CONVERSION"), componentId);
					validationFailure = true;
					invalidTimeEntered = true;
				}
				
				if(JsfUtil.isInvalidTime(meeting.getEndTimeString())) {
					if(log.isDebugEnabled()) log.debug("Failed to add section... meeting end time " + meeting.getEndTimeString() + " is invalid");
					String componentId = "addSectionsForm:sectionTable_" + sectionIndex + ":meetingsTable_" + meetingIndex + ":endTime";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"javax.faces.convert.DateTimeConverter.CONVERSION"), componentId);
					validationFailure = true;
					invalidTimeEntered = true;
				}

				if(JsfUtil.isEndTimeWithoutStartTime(meeting.getStartTimeString(), meeting.getEndTimeString())) {
					if(log.isDebugEnabled()) log.debug("Failed to update section... start time without end time");
					String componentId = "addSectionsForm:sectionTable_" + sectionIndex + ":meetingsTable_" + meetingIndex + ":startTime";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"section_update_failure_end_without_start"), componentId);
					validationFailure = true;
				}
				
				if(isInvalidMaxEnrollments(sectionModel)) {
					if(log.isDebugEnabled()) log.debug("Failed to update section... max enrollments is not valid");
					String componentId = "addSectionsForm:sectionTable_" + sectionIndex + ":maxEnrollmentInput";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"javax.faces.validator.LongRangeValidator.MINIMUM", new String[] {"0"}), componentId);
					validationFailure = true;
				}
				
				// Don't bother checking if the time values are invalid
				if(!invalidTimeEntered && JsfUtil.isEndTimeBeforeStartTime(meeting.getStartTimeString(),
						meeting.isStartTimeAm(), meeting.getEndTimeString(), meeting.isEndTimeAm())) {
					if(log.isDebugEnabled()) log.debug("Failed to update section... end time is before start time");
					String componentId = "addSectionsForm:sectionTable_" + sectionIndex + ":meetingsTable_" + meetingIndex + ":endTime";
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
							"section_update_failure_end_before_start"), componentId);
					validationFailure = true;
				}
			}
		}
		return validationFailure;
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
	
	public List<LocalSectionModel> getSections() {
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

}
