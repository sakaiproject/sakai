/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;
import org.sakaiproject.component.section.facade.impl.sakai.RoleImpl;
import org.sakaiproject.tool.section.decorator.EnrollmentDecorator;
import org.sakaiproject.tool.section.decorator.InstructorSectionDecorator;

public class RosterBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RosterBean.class);

	private static final String CAT_COLUMN_PREFIX = "cat";
	
	private String searchText;
	private int firstRow;
	private int maxDisplayedRows;
	private int enrollmentsSize;
	private String sortColumn;
	private boolean sortAscending;
	private boolean externallyManaged;

	private List enrollments;
	private Set sections;
	private List categories;
	
	public RosterBean() {
		maxDisplayedRows = 10;
	}
	
	public void init() {
		// Determine whether this course is externally managed
		externallyManaged = getCourse().isExternallyManaged();

		// Get the default search text
		if(StringUtils.trimToNull(searchText) == null) {
			searchText = getLocalizedMessage("roster_search_text");
		}

		// Get the sections
		sections = getAllSiteSections();
		
		// Get the site enrollments
		List siteStudents;
		if(searchText.equals(getLocalizedMessage("roster_search_text"))) {
			siteStudents = getSectionAwareness().getSiteMembersInRole(getSiteContext(), RoleImpl.STUDENT);
		} else {
			siteStudents = getSectionAwareness().findSiteMembersInRole(getSiteContext(), RoleImpl.STUDENT, searchText);
		}
		
		// Get the section enrollments
		Set studentUuids = new HashSet();
		for(Iterator iter = siteStudents.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			studentUuids.add(record.getUser().getUserUuid());
		}
		SectionEnrollments sectionEnrollments = getSectionManager().getSectionEnrollments(getSiteContext(), studentUuids);
		
		// Construct the decorated enrollments for the UI
		List unpagedEnrollments = new ArrayList();
		categories = getSectionAwareness().getSectionCategories();
		
		for(Iterator iter = siteStudents.iterator(); iter.hasNext();) {
			EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
			
			// Build a map of categories to sections in which the student is enrolled
			Map map = new HashMap();
			for(Iterator catIter = categories.iterator(); catIter.hasNext();) {
				String cat = (String)catIter.next();
				CourseSection section = sectionEnrollments.getSection(enrollment.getUser().getUserUuid(), cat);
				map.put(cat, section);
			}
			EnrollmentDecorator decorator = new EnrollmentDecorator(enrollment, map);
			unpagedEnrollments.add(decorator);
		}
		
		// TODO Not sure yet where sorting comes in... so implement the page filter last		
		enrollments = new ArrayList();
		int lastRow;
		if(firstRow + maxDisplayedRows > unpagedEnrollments.size()) {
			lastRow = unpagedEnrollments.size();
		} else {
			lastRow = firstRow + maxDisplayedRows;
		}
		enrollments.addAll(unpagedEnrollments.subList(firstRow, lastRow));
		enrollmentsSize = unpagedEnrollments.size();
	}

	/**
	 * Gets the categories that are currently being used in this site context.
	 * 
	 * TODO Should this be a method in SectionManager?
	 * @param categories
	 * @param sections
	 * @return
	 */
	private List getUsedCategories(List categories, Collection sections) {
		List used = new ArrayList();
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			String cat = section.getCategory();
			if(categories.contains(cat)) {
				used.add(cat);
			}
		}
		return used;
	}
	
	public HtmlDataTable getRosterDataTable() {
		return null;
	}
	
	public void setRosterDataTable(HtmlDataTable rosterDataTable) {
		List usedCategories = getUsedCategories(categories, sections);
		
		if (rosterDataTable.findComponent(CAT_COLUMN_PREFIX + "0") == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			// Add columns for each category. Be sure to create unique IDs
			// for all child components.
			int colpos = 0;
			for (Iterator iter = usedCategories.iterator(); iter.hasNext(); colpos++) {
				String category = (String)iter.next();
				String categoryName = getCategoryName(category);

				UIColumn col = new UIColumn();
				col.setId(CAT_COLUMN_PREFIX + colpos);

                HtmlCommandSortHeader sortHeader = new HtmlCommandSortHeader();
                sortHeader.setId(CAT_COLUMN_PREFIX + "sorthdr_" + colpos);
                sortHeader.setRendererType("org.apache.myfaces.SortHeader");
                sortHeader.setArrow(true);
                sortHeader.setColumnName(category);
                //sortHeader.setActionListener(app.createMethodBinding("#{rosterBean.sort}", new Class[] {ActionEvent.class}));

				HtmlOutputText headerText = new HtmlOutputText();
				headerText.setId(CAT_COLUMN_PREFIX + "hdr_" + colpos);
				headerText.setValue(categoryName);

                sortHeader.getChildren().add(headerText);
                col.setHeader(sortHeader);

				HtmlOutputText contents = new HtmlOutputText();
				contents.setId(CAT_COLUMN_PREFIX + "cell_" + colpos);
				contents.setValueBinding("value",
					app.createValueBinding("#{enrollment.categoryToSectionMap['" + category + "'].title}"));
				col.getChildren().add(contents);
				rosterDataTable.getChildren().add(col);
			}
		}
	}

	public void search(ActionEvent event) {
		firstRow = 0;
	}

	public void clearSearch(ActionEvent event) {
		searchText = null;
	}
	
	public List getEnrollments() {
		return enrollments;
	}
	public int getEnrollmentsSize() {
		return enrollmentsSize;
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
	public boolean isExternallyManaged() {
		return externallyManaged;
	}
	public String getSearchText() {
		return searchText;
	}
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	public int getFirstRow() {
		return firstRow;
	}
	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}
	public int getMaxDisplayedRows() {
		return maxDisplayedRows;
	}
	public void setMaxDisplayedRows(int maxDisplayedRows) {
		this.maxDisplayedRows = maxDisplayedRows;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
