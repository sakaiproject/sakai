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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.tool.section.decorator.InstructorSectionDecorator;
import org.sakaiproject.tool.section.decorator.StudentSectionDecorator;

public class StudentViewBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(StudentViewBean.class);
	
	private String sortColumn;
	private boolean sortAscending;
	private String sectionFilter;
	private boolean externallyManaged;
	private boolean joinAllowed;
	private boolean switchAllowed;
	
	private List sections;
	private String rowClasses;
	
	private List categoryIds;
	private List categoryNames; // Must be ordered exactly like the category ids

	public StudentViewBean() {
		sortColumn = "meetingTimes";
		sortAscending = true;
	}
	
	public void init() {
		Course course = getCourse();

		// Determine whether this course is externally managed
		externallyManaged = course.isExternallyManaged();
		
		// Determine whether the sections are joinable and/or switchable
		joinAllowed = course.isSelfRegistrationAllowed();
		switchAllowed = course.isSelfSwitchingAllowed();

		// Get all sections in the site
		List sectionSet = getAllSiteSections();
		sections = new ArrayList();
		
		// Get the category ids
		categoryIds = getSectionCategories();
		
		// Get category names, ordered just like the category ids
		categoryNames = new ArrayList();
		for(Iterator iter = categoryIds.iterator(); iter.hasNext();) {
			String catId = (String)iter.next();
			categoryNames.add(getCategoryName(catId));
		}

		// Get the section enrollments for this student
		Set enrolledSections = getMyEnrolledSections();

		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			String catName = getCategoryName(section.getCategory());
			
			// Generate the string showing the TAs
			List tas = getSectionManager().getSectionTeachingAssistants(section.getUuid());
			List taNames = new ArrayList();
			for(Iterator taIter = tas.iterator(); taIter.hasNext();) {
				ParticipationRecord ta = (ParticipationRecord)taIter.next();
				taNames.add(ta.getUser().getSortName());
			}

			Collections.sort(taNames);

			int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());
			boolean member = isEnrolledInSection(enrolledSections, section);
			boolean memberOtherSection = isEnrolledInOtherSection(enrolledSections, section);
			
			StudentSectionDecorator decoratedSection = new StudentSectionDecorator(
					section, catName, taNames, totalEnrollments, member, memberOtherSection);
			sections.add(decoratedSection);
		}
		
		Collections.sort(sections, getComparator());
		
		// Remove the sections that don't match the filter.  Since the display logic
		// requires that we have all of the sections in memory to decide on the switch,
		// join, and member flags, we can't filter until now.
		filterSections();
		
		// Add the row css classes
		buildRowClasses();
	}

	private void filterSections() {
		if("MY".equals(sectionFilter)) {
			List filteredSections = new ArrayList();
			for(Iterator iter = sections.iterator(); iter.hasNext();) {
				StudentSectionDecorator decoratedSection = (StudentSectionDecorator)iter.next();
				if(decoratedSection.isMember()) {
					filteredSections.add(decoratedSection);
				}
			}
			sections = filteredSections;
		}
	}

	private void buildRowClasses() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			StudentSectionDecorator decoratedSection = (StudentSectionDecorator)iter.next();
			if(iter.hasNext()) {
				StudentSectionDecorator nextSection = (StudentSectionDecorator)sections.get(++index);
				if(nextSection.getCategory().equals(decoratedSection.getCategory())) {
					if(decoratedSection.isMember()) {
						sb.append("member");
					} else {
						sb.append("nonmember");
					}
				} else {
					if(decoratedSection.isMember()) {
						sb.append("memberPadRow");
					} else {
						sb.append("nonmemberPadRow");
					}
				}
				sb.append(",");
			} else {
				if(decoratedSection.isMember()) {
					sb.append("member");
				} else {
					sb.append("nonmember");
				}
			}
		}
		rowClasses = sb.toString();
	}

	private boolean isEnrolledInSection(Set enrolledSections, CourseSection section) {
		for(Iterator iter = enrolledSections.iterator(); iter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			if(enr.getLearningContext().equals(section)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEnrolledInOtherSection(Set enrolledSections, CourseSection section) {
		String category = section.getCategory();
		for(Iterator iter = enrolledSections.iterator(); iter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			if(((CourseSection)enr.getLearningContext()).getCategory().equals(category)) {
				return true;
			}
		}
		return false;
	}

	private Comparator getComparator() {
		if(sortColumn.equals("instructor")) {
			return InstructorSectionDecorator.getManagersComparator(sortAscending, categoryNames, categoryIds); 
		} else if(sortColumn.equals("category")) {
			// These are already sorted by category, so just sort by title
			return InstructorSectionDecorator.getFieldComparator("title", sortAscending, categoryNames, categoryIds);
		} else if(sortColumn.equals("available")) {
			return InstructorSectionDecorator.getAvailableEnrollmentsComparator(sortAscending, categoryNames, categoryIds); 
		} else if(sortColumn.equals("change")) {
			return StudentSectionDecorator.getChangeComparator(sortAscending, categoryNames, categoryIds, joinAllowed, switchAllowed);
		} else {
			return InstructorSectionDecorator.getFieldComparator(sortColumn, sortAscending, categoryNames, categoryIds); 
		}
	}

	public void processJoinSection(ActionEvent event) {
		String sectionUuid = (String)FacesContext.getCurrentInstance().getExternalContext()
		.getRequestParameterMap().get("sectionUuid");
		getSectionManager().joinSection(sectionUuid);
	}
	
	public void processSwitchSection(ActionEvent event) {
		String sectionUuid = (String)FacesContext.getCurrentInstance().getExternalContext()
		.getRequestParameterMap().get("sectionUuid");
		getSectionManager().switchSection(sectionUuid);
	}

	public List getSections() {
		return sections;
	}
	
	public String getRowClasses() {
		return rowClasses;
	}
	
	public boolean isSortAscending() {
		return sortAscending;
	}
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}
	public String getSortColumn() {
		return sortColumn;
	}
	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}
	public String getSectionFilter() {
		return sectionFilter;
	}
	public void setSectionFilter(String sectionFilter) {
		this.sectionFilter = sectionFilter;
	}

	public boolean isExternallyManaged() {
		return externallyManaged;
	}

	public boolean isJoinAllowed() {
		return joinAllowed;
	}

	public boolean isSwitchAllowed() {
		return switchAllowed;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
