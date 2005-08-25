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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.tool.section.decorator.InstructorSectionDecorator;

public class OverviewBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(OverviewBean.class);
	
	private String sortColumn;
	private boolean sortAscending;
	private boolean externallyManaged;
	
	private List sections;
	private List categoryIds;
	private List categoryNames; // Must be ordered exactly like the category ids
	
	public OverviewBean() {
		sortColumn = "title";
		sortAscending = true;
	}
	
	public void init() {
		// Determine whether this course is externally managed
		externallyManaged = getCourse().isExternallyManaged();

		// Get all sections in the site
		Set sectionSet = getAllSiteSections();
		sections = new ArrayList();
		
		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			String catName = getCategoryName(section.getCategory());
			
			// Generate the string showing the TAs
			// FIXME Get this query out of the loop!
			List tas = getSectionManager().getSectionTeachingAssistants(section.getUuid());
			StringBuffer taNames = new StringBuffer();
			for(Iterator taIter = tas.iterator(); taIter.hasNext();) {
				ParticipationRecord ta = (ParticipationRecord)taIter.next();
				taNames.append(ta.getUser().getDisplayName());
				if(taIter.hasNext()) {
					taNames.append(", ");
				}
			}

			int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());
			
			InstructorSectionDecorator decoratedSection = new InstructorSectionDecorator(
					section, catName, taNames.toString(), totalEnrollments);
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
	}
	
	private Comparator getComparator() {
		// TODO Clean up comparators (using BeanUtils?) and add remaining comparators
		
		if(sortColumn.equals("title")) {
			return 	new Comparator() {
				public int compare(Object o1, Object o2) {
					if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
						InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
						InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

						// First compare the category name, then compare the title
						int categoryNameComparison = categoryNameComparison(section1, section2);
						if(categoryNameComparison == 0) {
							// These are in the same category, so compare by title
							int titleComparison = section1.getTitle().compareTo(section2.getTitle());
							return sortAscending ? titleComparison : (-1 * titleComparison);
						}
						// These are in different categories, so sort them by category name
						return categoryNameComparison;
					}
					if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
							+ o1 + "," + o2);
					return 0;
				}
			};
		}
		
		if(sortColumn.equals("time")) {
			return new Comparator() {
				public int compare(Object o1, Object o2) {
					if(o1 instanceof InstructorSectionDecorator && o2 instanceof InstructorSectionDecorator) {
						InstructorSectionDecorator section1 = (InstructorSectionDecorator)o1;
						InstructorSectionDecorator section2 = (InstructorSectionDecorator)o2;

						// First compare the category name, then compare the time
						int categoryNameComparison = categoryNameComparison(section1, section2);
						if(categoryNameComparison == 0) {
							// These are in the same category, so compare by time
							String meetingTimes1 = section1.getMeetingTimes();
							String meetingTimes2 = section2.getMeetingTimes();
							if(meetingTimes1 == null && meetingTimes2 != null) {
								return -1;
							}
							if(meetingTimes2 == null && meetingTimes1 != null) {
								return 1;
							}
							if(meetingTimes1 == null && meetingTimes2 == null) {
								return 0;
							}
							int timeComparison = meetingTimes1.compareTo(meetingTimes2);
							return sortAscending ? timeComparison : (-1 * timeComparison);
						}
						// These are in different categories, so sort them by category name
						return categoryNameComparison;
					}
					if(log.isDebugEnabled()) log.debug("One of these is not an InstructorSectionDecorator: "
							+ o1 + "," + o2);
					return 0;
				}
			};
		}

		if(log.isInfoEnabled()) log.info("The sort column is not set properly (sortColumn= " + sortColumn + ")");
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				return 0;
			}
		};
	}
	

	private int categoryNameComparison(InstructorSectionDecorator section1, InstructorSectionDecorator section2) {
		String section1Name = (String)categoryNames.get(categoryIds.indexOf(section1.getCategory()));
		String section2Name = (String)categoryNames.get(categoryIds.indexOf(section2.getCategory()));
		return section1Name.compareTo(section2Name);
	}

	public List getSections() {
		return sections;
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
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
