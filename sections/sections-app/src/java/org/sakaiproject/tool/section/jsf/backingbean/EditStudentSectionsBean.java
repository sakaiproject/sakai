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

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit student sections page (where a single student is assigned to sections).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class EditStudentSectionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String UNASSIGNED = "unassigned";

	private static final Log log = LogFactory.getLog(EditStudentSectionsBean.class);
	
	private String studentUid;
	private String studentName;
	private List usedCategories;
	private Map sectionItems;
	private Map sectionEnrollment;
	
	private String nameSeparator;
	private String sectionDescriptionSepChar;
	private String fullIndicator;
	
	public void init() {
		// Get the description separator character
		sectionDescriptionSepChar = JsfUtil.getLocalizedMessage("edit_student_sections_description_sep_char");
		nameSeparator = JsfUtil.getLocalizedMessage("name_list_separator");
		fullIndicator = JsfUtil.getLocalizedMessage("edit_student_sections_full");
		
		// Get the student's name
		String studentUidFromParam = JsfUtil.getStringFromParam("studentUid");
		if(studentUidFromParam != null) {
			studentUid = studentUidFromParam;
		}
		
		User student = getSectionManager().getSiteEnrollment(getSiteContext(), studentUid);
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
			List taRecords = getSectionManager().getSectionTeachingAssistants(section.getUuid());
			int numEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());
			sectionsInCat.add(new SelectItem(section.getUuid(), getSectionLabel(section, taRecords, numEnrollments)));
			sectionItems.put(cat, sectionsInCat);
			
			// Ensure that each used category has at least an entry in the sectionEnrollment map
			sectionEnrollment.put(cat, UNASSIGNED);
		}

		// Build the map of categories to the section uuids for the student's current enrollments
		Set studentEnrollments = getSectionManager().getSectionEnrollments(studentUid, course.getUuid());
		for(Iterator iter = studentEnrollments.iterator(); iter.hasNext();) {
			EnrollmentRecord record = (EnrollmentRecord)iter.next();
			CourseSection section = (CourseSection)record.getLearningContext();
			sectionEnrollment.put(section.getCategory(), section.getUuid());
		}
	}

	/**
	 * Builds the section description for the UI.
	 * 
	 * @param section
	 * @param taRecords
	 * @param numEnrollments
	 * @return
	 */
	private String getSectionLabel(CourseSection section, List taRecords, int numEnrollments) {
		CourseSectionDecorator sec = new CourseSectionDecorator(section, null); // We dont't need the category
		StringBuffer sb = new StringBuffer(section.getTitle());

		
		if(taRecords != null && taRecords.size() > 0) {
			sb.append(" ");
			sb.append(sectionDescriptionSepChar);
			sb.append(" ");
			
			for(Iterator iter = taRecords.iterator(); iter.hasNext();) {
				ParticipationRecord record = (ParticipationRecord)iter.next();
				sb.append(record.getUser().getDisplayName());
				if(iter.hasNext()) {
					sb.append(nameSeparator);
					sb.append(" ");
				}
			}
		}
		
		String meetingTimes = sec.getMeetingTimes();
		if(StringUtils.trimToNull(meetingTimes) != null) {
			sb.append(" ");
			sb.append(sectionDescriptionSepChar);
			sb.append(" ");
			
			sb.append(meetingTimes);
		}
		
		if(section.getMaxEnrollments() != null && numEnrollments >= section.getMaxEnrollments().intValue()) {
			sb.append(" ");
			sb.append(sectionDescriptionSepChar);
			sb.append(" ");
			sb.append(fullIndicator);
		}
		
		return sb.toString();
	}
	
	public String update() {
		// Add the success message first, before any caveats (see below)
		String[] params = {studentName};
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("edit_student_sections_successful", params));

		String siteContext = getSiteContext();
		for(Iterator iter = sectionEnrollment.keySet().iterator(); iter.hasNext();) {
			String category = (String)iter.next();
			String sectionUuid = (String)sectionEnrollment.get(category);
			if(sectionUuid.equals(UNASSIGNED)) {
				// Remove any existing section enrollment for this category
				if(log.isDebugEnabled()) log.debug("Unassigning " + studentUid + " from category " + category);
				getSectionManager().dropEnrollmentFromCategory(studentUid, siteContext, category);
			} else {
				if(log.isDebugEnabled()) log.debug("Assigning " + studentUid + " to section " + sectionUuid);
				getSectionManager().addSectionMembership(studentUid, Role.STUDENT, sectionUuid);

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
		}
		return "roster";
	}
	
	public void setStudentUid(String studentUuid) {
		this.studentUid = studentUuid;
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
