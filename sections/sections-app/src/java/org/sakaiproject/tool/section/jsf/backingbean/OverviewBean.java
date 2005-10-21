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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.tool.section.decorator.InstructorSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the overview page.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class OverviewBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(OverviewBean.class);

	private boolean externallyManaged;
	private String rowClasses;

	private List sections;
	private List sectionsToDelete;
	private List categoryIds;
	private List categoryNames; // Must be ordered exactly like the category ids

	public void init() {
		// Determine whether this course is externally managed
		externallyManaged = getCourse().isExternallyManaged();

		if(log.isDebugEnabled()) log.debug("at top of init; sections=" + sections);
		if (log.isDebugEnabled() && (sections != null)) {
			for(int i = 0; i < sections.size(); i++) {
				InstructorSectionDecorator decoratedSection = (InstructorSectionDecorator)sections.get(i);
				log.debug("decoratedSection[" + i + "] isFlaggedForRemoval=" + decoratedSection.isFlaggedForRemoval() + ", section=" + decoratedSection.getSection());
			}
		}

		// Get all sections in the site
		List sectionSet = getAllSiteSections();
		sections = new ArrayList();

		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			String catName = getCategoryName(section.getCategory());

			// Generate the string showing the TAs
			// FIXME Get this query out of the loop!
			List tas = getSectionManager().getSectionTeachingAssistants(section.getUuid());
			List taNames = new ArrayList();
			for(Iterator taIter = tas.iterator(); taIter.hasNext();) {
				ParticipationRecord ta = (ParticipationRecord)taIter.next();
				taNames.add(StringUtils.abbreviate(ta.getUser().getSortName(), getPrefs().getMaxNameLength()));
			}

			Collections.sort(taNames);

			int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());

			InstructorSectionDecorator decoratedSection = new InstructorSectionDecorator(
					section, catName, taNames, totalEnrollments);
			sections.add(decoratedSection);
		}

		// Get the category ids
		categoryIds = getSectionCategories();

		// Get category names, ordered just like the category ids
		categoryNames = new ArrayList();
		for(Iterator iter = categoryIds.iterator(); iter.hasNext();) {
			String catId = (String)iter.next();
			categoryNames.add(getCategoryName(catId));
		}

		// Sort the collection set
		Collections.sort(sections, getComparator());

		// Add the row css classes
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			InstructorSectionDecorator decoratedSection = (InstructorSectionDecorator)iter.next();
			if(iter.hasNext()) {
				InstructorSectionDecorator nextSection = (InstructorSectionDecorator)sections.get(++index);
				if(nextSection.getCategory().equals(decoratedSection.getCategory())) {
					sb.append("section");
				} else {
					sb.append("sectionPadRow");
				}
				sb.append(",");
			} else {
				sb.append("section");
			}
		}
		rowClasses = sb.toString();
	}

	private Comparator getComparator() {
		String sortColumn = getPrefs().getOverviewSortColumn();
		boolean sortAscending = getPrefs().isOverviewSortAscending();

		if(sortColumn.equals("managers")) {
			return InstructorSectionDecorator.getManagersComparator(sortAscending, categoryNames, categoryIds);
		} else if(sortColumn.equals("max")) {
			return InstructorSectionDecorator.getEnrollmentsComparator(sortAscending, false, categoryNames, categoryIds);
		} else if(sortColumn.equals("available")) {
			return InstructorSectionDecorator.getEnrollmentsComparator(sortAscending, true, categoryNames, categoryIds);
		} else {
			return InstructorSectionDecorator.getFieldComparator(sortColumn, sortAscending, categoryNames, categoryIds);
		}
	}

	public String confirmDelete() {
		sectionsToDelete = new ArrayList();
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			InstructorSectionDecorator decoratedSection = (InstructorSectionDecorator)iter.next();
			if(decoratedSection.isFlaggedForRemoval()) {
				sectionsToDelete.add(decoratedSection);
			}
		}
		if(sectionsToDelete.isEmpty()) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("overview_delete_section_choose"));
			return null; // Don't go anywhere
		} else {
			return "deleteSections";
		}
	}
	
	public String deleteSections() {
		for(Iterator iter = sectionsToDelete.iterator(); iter.hasNext();) {
			InstructorSectionDecorator decoratedSection = (InstructorSectionDecorator)iter.next();
			getSectionManager().disbandSection(decoratedSection.getUuid());
		}
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("overview_delete_section_success"));
		return "overview";
	}

	public List getSections() {
		return sections;
	}
	public boolean isExternallyManaged() {
		return externallyManaged;
	}
	public String getRowClasses() {
		return rowClasses;
	}

	public List getSectionsToDelete() {
		return sectionsToDelete;
	}
}
