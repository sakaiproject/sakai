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
import java.util.Collections;
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
			List tas = getTeachingAssistants(section.getUuid());
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
		
		// TODO Sort the collection set properly
		Collections.sort(sections);
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
