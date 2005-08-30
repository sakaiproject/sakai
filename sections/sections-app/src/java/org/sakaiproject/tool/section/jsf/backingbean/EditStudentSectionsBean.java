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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;

public class EditStudentSectionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(EditStudentSectionsBean.class);
	
	private String studentUuid;
	private String studentName;
	
	private Map categoryToSectionList;
	
	public void init() {
		// Get the student's name
		User student = getUser(studentUuid);
		studentName = student.getDisplayName();
		
		// Get the list of used categories
		Course course = getCourse();
		List allSections = getAllSiteSections();

		// Get the student's current enrollments
		Set studentEnrollments = getSectionManager().getSectionEnrollments(studentUuid, course.getUuid());
		
		// Build the map of categories to lists of decorated sections
		categoryToSectionList = new HashMap();
		for(Iterator iter = allSections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			String cat = section.getCategory();
			List sectionsInCat;
			if(categoryToSectionList.get(cat) == null) {
				sectionsInCat = new ArrayList();
			} else {
				sectionsInCat = (List)categoryToSectionList.get(cat);
			}
			sectionsInCat.add(section);
			categoryToSectionList.put(cat, sectionsInCat);
		}
		
		getSectionManager().getSectionEnrollments(studentUuid, course.getUuid());
	}

	public String update() {
		return "overview";
	}
	
	public String cancel() {
		return "overview";
	}

	public Map getCategoryToSectionList() {
		return categoryToSectionList;
	}

	public void setCategoryToSectionList(Map categoryToSectionList) {
		this.categoryToSectionList = categoryToSectionList;
	}

	public void setStudentUuid(String studentUuid) {
		this.studentUuid = studentUuid;
	}
	
	public String getStudentName() {
		return studentName;
	}
}

/**********************************************************************************
 * $Id$
 *********************************************************************************/
