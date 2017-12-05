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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.decorator.StudentSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit student sections page (where a single student is assigned to sections).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class EditStudentSectionsBean extends FilteredSectionListingBean implements Serializable {
	private static final long serialVersionUID = 1L;

	protected static final String UNASSIGNED = "unassigned";

	protected String studentUid;
	protected String studentName;
	protected List<SectionDecorator> enrolledSections;
	protected String elementToFocus;
	
	protected boolean showNegativeSpots;
	
	public EditStudentSectionsBean() {
		 showNegativeSpots = true;
	}
	
	public void init() {
		setDefaultPrefs();

		// Get the filter settings
		String categoryFilter = getCategoryFilter();
		String myFilter = getMyFilter();
		
		// Get the student's name
		User student = getSectionManager().getSiteEnrollment(getSiteContext(), studentUid);
		studentName = student.getDisplayName();

		// Get all sections in the site
		List sectionSet = getAllSiteSections();
		sections = new ArrayList<SectionDecorator>();
		enrolledSections = new ArrayList<SectionDecorator>();

		// Keep track of whether there are no sections in this site
		siteWithoutSections = sectionSet.isEmpty();

		// Generate the category select items
		categorySelectItems = generateCategorySelectItems();

		// Compute the filter state
		computeFilterState(sectionSet);

		// Get the section enrollments for this student
		Set enrolled = getEnrolledSections(studentUid);

		// Get the total enrollments for all groups
		Map sectionSize = getSectionManager().getEnrollmentCount(sectionSet);
		
		// Get the TAs for all groups
		Map<String,List<ParticipationRecord>> sectionTAs = getSectionManager().getSectionTeachingAssistantsMap(sectionSet);
		
		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			String catName = getCategoryName(section.getCategory());
			
			boolean hideSectionInTable = false;

			// If we are filtering by categories, and the section is not in this category, skip it
			if(StringUtils.trimToNull(categoryFilter) != null && ! categoryFilter.equals(section.getCategory())) {
				if(log.isDebugEnabled()) log.debug("Filtering out " + section.getTitle() + ", since it is not in category " + categoryFilter);
				hideSectionInTable = true;
			}

			// Generate the string showing the TAs
						
			List<ParticipationRecord> tas = (List<ParticipationRecord>) sectionTAs.get(section.getUuid());
			List<String> taNames = generateTaNames(tas);
			List<String> taUids = generateTaUids(tas);

			// If we're filtering by my sections, and the TAs in the section don't include me, skip this section
			if("MY".equals(myFilter)) {
				String userUid = getUserUid();
				if( ! taUids.contains(userUid)) {
					if(log.isDebugEnabled()) log.debug("Filtering out " + section.getTitle() + ", since user " + userUid + " is not a TA");
					hideSectionInTable = true;
				}
			}

			// Sort the TA names
			Collections.sort(taNames);

			// Get the enrollments and membership so we can decorate the section
			int totalEnrollments = sectionSize.containsKey(section.getUuid()) ? 
					(Integer) sectionSize.get(section.getUuid()) : 0;
					
			boolean member = isEnrolledInSection(enrolled, section);
			boolean memberOtherSection = isEnrolledInOtherSection(enrolled, section);
			
			StudentSectionDecorator decoratedSection = new StudentSectionDecorator(
					section, catName, taNames, totalEnrollments, member, memberOtherSection, showNegativeSpots);
			
			if(!hideSectionInTable) {
				sections.add(decoratedSection);
			}
			
			if(member) {
				enrolledSections.add(decoratedSection);
			}
		}
		Collections.sort(sections, getComparator());
		Collections.sort(enrolledSections, getComparator());
	}
	
	protected boolean isEnrolledInSection(Set enrolledSections, CourseSection section) {
		for(Iterator iter = enrolledSections.iterator(); iter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			if(enr.getLearningContext().equals(section)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isEnrolledInOtherSection(Set enrolledSections, CourseSection section) {
		String category = section.getCategory();
		for(Iterator iter = enrolledSections.iterator(); iter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			if(((CourseSection)enr.getLearningContext()).getCategory().equals(category)) {
				return true;
			}
		}
		return false;
	}

	protected void checkMaxEnrollments(String sectionUuid) {
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
	}

	protected Comparator<SectionDecorator> getComparator() {
		String sortColumn = getSortColumn();
		boolean sortAscending = isSortAscending();
		if("title".equals(sortColumn)) {
			return SectionDecorator.getTitleComparator(sortAscending); 
		} else if("instructor".equals(sortColumn)) {
			return SectionDecorator.getManagersComparator(sortAscending); 
		} else if("available".equals(sortColumn)) {
			return SectionDecorator.getEnrollmentsComparator(sortAscending, true); 
		} else if("meetingDays".equals(sortColumn)) {
			return SectionDecorator.getDayComparator(sortAscending); 
		} else if("meetingTimes".equals(sortColumn)) {
			return SectionDecorator.getTimeComparator(sortAscending); 
		} else if("location".equals(sortColumn)) {
			return SectionDecorator.getLocationComparator(sortAscending); 
		}
		log.error("Invalid sort specified.");
		return null;
	}
	
	public void processJoinSection(ActionEvent event) {
		String sectionUuid = (String)FacesContext.getCurrentInstance().getExternalContext()
		.getRequestParameterMap().get("sectionUuid");
		//is this section still joinable?
		CourseSection section = getSectionManager().getSection(sectionUuid);
		
		// The section might have been deleted
		if(section == null) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("error_section_deleted"));
			return;
		}

		try {
			getSectionManager().addSectionMembership(studentUid, Role.STUDENT, sectionUuid);
		} catch (RoleConfigurationException rce) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
		}
		// Don't focus on this component, since it won't be there any more.  Focus on the unjoin component
		String componentId = event.getComponent().getClientId(FacesContext.getCurrentInstance());
		elementToFocus = componentId.replaceAll(":join", ":unjoin");
	}
	
	public void processDrop(ActionEvent event) {
		String sectionUuid = (String)FacesContext.getCurrentInstance().getExternalContext()
		.getRequestParameterMap().get("sectionUuid");
		CourseSection section = getSectionManager().getSection(sectionUuid);
		
		// The section might have been deleted
		if(section == null) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("error_section_deleted"));
			return;
		}

		getSectionManager().dropSectionMembership(studentUid, sectionUuid);

		// Don't focus on this component, since it won't be there any more.  Focus on the join component
		String componentId = event.getComponent().getClientId(FacesContext.getCurrentInstance());
		elementToFocus = componentId.replaceAll(":unjoin", ":join");
	}

	/**
	 * Sets the student id to use in displaying the page.
	 * 
	 * @param studentUuid
	 */
	public void setStudentUid(String studentUid) {
		this.studentUid = studentUid;
	}


	public String getStudentName() {
		return studentName;
	}

	public String getElementToFocus() {
		return elementToFocus;
	}

	public void setElementToFocus(String elementToFocus) {
		this.elementToFocus = elementToFocus;
	}

	public String getUnassignedValue() {
		return UNASSIGNED;
	}

	public List<SectionDecorator> getEnrolledSections() {
		return enrolledSections;
	}

	@Override
	public String getSortColumn() {
		return getPrefs().getEditStudentSectionsSortColumn();
	}

	@Override
	public boolean isSortAscending() {
		return getPrefs().isEditStudentSectionsSortAscending();
	}

	@Override
	public void setSortAscending(boolean sortAscending) {
		getPrefs().setEditStudentSectionsSortAscending(sortAscending);
	}

	@Override
	public void setSortColumn(String sortColumn) {
		getPrefs().setEditStudentSectionsSortColumn(sortColumn);
	}

	@Override
	public String getCategoryFilter() {
		return getPrefs().getEditStudentSectionsCategoryFilter();
	}

	@Override
	public String getMyFilter() {
		return getPrefs().getEditStudentSectionsMyFilter();
	}

	@Override
	public void setCategoryFilter(String categoryFilter) {
		getPrefs().setEditStudentSectionsCategoryFilter(categoryFilter);
	}

	@Override
	public void setMyFilter(String myFilter) {
		getPrefs().setEditStudentSectionsMyFilter(myFilter);
	}
}
