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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class EditStudentSectionsBean extends FilteredSectionListingBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EditStudentSectionsBean.class);

	protected static final String UNASSIGNED = "unassigned";

	protected String studentUid;
	protected String studentName;
	protected List<SectionDecorator> enrolledSections;
	
	public void init() {
		// Get the student's name
		User student = getSectionManager().getSiteEnrollment(getSiteContext(), studentUid);
		studentName = student.getDisplayName();

		// Get all sections in the site
		List sectionSet = getAllSiteSections();
		sections = new ArrayList<SectionDecorator>();
		enrolledSections = new ArrayList<SectionDecorator>();

		// Generate the category select items
		categorySelectItems = generateCategorySelectItems();

		// Get the section enrollments for this student
		Set enrolled = getEnrolledSections(studentUid);

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
			List<ParticipationRecord> tas = getSectionManager().getSectionTeachingAssistants(section.getUuid());
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
			int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());
			boolean member = isEnrolledInSection(enrolled, section);
			boolean memberOtherSection = isEnrolledInOtherSection(enrolled, section);
			
			StudentSectionDecorator decoratedSection = new StudentSectionDecorator(
					section, catName, taNames, totalEnrollments, member, memberOtherSection);
			
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
		if(sortColumn.equals("title")) {
			return SectionDecorator.getTitleComparator(sortAscending); 
		} else if(sortColumn.equals("instructor")) {
			return SectionDecorator.getManagersComparator(sortAscending); 
		} else if(sortColumn.equals("available")) {
			return SectionDecorator.getEnrollmentsComparator(sortAscending, true); 
		} else if(sortColumn.equals("meetingDays")) {
			return SectionDecorator.getDayComparator(sortAscending); 
		} else if(sortColumn.equals("meetingTimes")) {
			return SectionDecorator.getTimeComparator(sortAscending); 
		} else if(sortColumn.equals("location")) {
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
			// There's nothing we can do in the UI, really.
			log.warn("Attempted to add user " + studentUid + " to a non-existent (recently deleted?) section: " + sectionUuid);
			return;
		}

		try {
			getSectionManager().addSectionMembership(studentUid, Role.STUDENT, sectionUuid);
		} catch (RoleConfigurationException rce) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
		}
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
	
	public String getUnassignedValue() {
		return UNASSIGNED;
	}

	public List<SectionDecorator> getEnrolledSections() {
		return enrolledSections;
	}
}
