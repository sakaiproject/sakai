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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

public class EditStudentSectionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String UNASSIGNED = "unassigned";

	private static final Log log = LogFactory.getLog(EditStudentSectionsBean.class);
	
	private String studentUuid;
	private String studentName;
	private List usedCategories;
	private Map sectionItems;
	private Map sectionEnrollment;
	
	public void init() {
		// Get the student's name
		String studentUuidFromParam = JsfUtil.getStringFromParam("studentUuid");
		if(studentUuidFromParam != null) {
			studentUuid = studentUuidFromParam;
		}
		User student = getUser(studentUuid);
		studentName = student.getDisplayName();
		
		// Get the site course and sections
		Course course = getCourse();
		List allSections = getAllSiteSections();

		// Get the list of used categories
		Set usedCategorySet = super.getUsedCategories(getSectionCategories(), allSections); 
		usedCategories = new ArrayList();
		for(Iterator iter=usedCategorySet.iterator(); iter.hasNext();) {
			usedCategories.add(iter.next());
		}
		Collections.sort(usedCategories);
		
		// Build the map of categories to section select items
		sectionItems = new HashMap();
		sectionEnrollment = new HashMap();
		for(Iterator iter = allSections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			String cat = section.getCategory();
			List sectionsInCat;
			if(sectionItems.get(cat) == null) {
				sectionsInCat = new ArrayList();
			} else {
				sectionsInCat = (List)sectionItems.get(cat);
			}
			sectionsInCat.add(new SelectItem(section.getUuid(), section.getTitle()));
			sectionItems.put(cat, sectionsInCat);
			
			// Ensure that each used category has at least an entry in the sectionEnrollment map
			sectionEnrollment.put(cat, UNASSIGNED);
		}

		// Build the map of categories to the section uuids for the student's current enrollments
		Set studentEnrollments = getSectionManager().getSectionEnrollments(studentUuid, course.getUuid());
		for(Iterator iter = studentEnrollments.iterator(); iter.hasNext();) {
			EnrollmentRecord record = (EnrollmentRecord)iter.next();
			CourseSection section = (CourseSection)record.getLearningContext();
			sectionEnrollment.put(section.getCategory(), section.getUuid());
		}
	}

	public String update() {
		String siteContext = getSiteContext();
		for(Iterator iter = sectionEnrollment.keySet().iterator(); iter.hasNext();) {
			String category = (String)iter.next();
			String sectionUuid = (String)sectionEnrollment.get(category);
			if(sectionUuid.equals(UNASSIGNED)) {
				// Remove any existing section enrollment for this category
				if(log.isDebugEnabled()) log.debug("Unassigning " + studentUuid + " from category " + category);
				getSectionManager().dropEnrollmentFromCategory(studentUuid, siteContext, category);
			} else {
				if(log.isDebugEnabled()) log.debug("Assigning " + studentUuid + " to section " + sectionUuid);
				getSectionManager().addSectionMembership(studentUuid, Role.STUDENT, sectionUuid);
			}
		}
		return "roster";
	}
	
	public void setStudentUuid(String studentUuid) {
		this.studentUuid = studentUuid;
	}
	
	public String getStudentName() {
		return studentName;
	}

	public List getUsedCategories() {
		return usedCategories;
	}

	public Map getSectionEnrollment() {
		return sectionEnrollment;
	}

	public void setSectionEnrollment(Map sectionEnrollment) {
		this.sectionEnrollment = sectionEnrollment;
	}

	public Map getSectionItems() {
		return sectionItems;
	}

	public void setSectionItems(Map sectionItems) {
		this.sectionItems = sectionItems;
	}

	public String getUnassignedValue() {
		return UNASSIGNED;
	}
}

/**********************************************************************************
 * $Id$
 *********************************************************************************/
