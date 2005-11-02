/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.integration.helper.standalone;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.assessment.integration.helper.ifc.SectionAwareServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.standalone.FacadeUtils;


/**
 * An implementation of Samigo-specific authorization (based on Gradebook's) needs based
 * on the shared Section Awareness API.
 */
public class SectionAwareServiceHelperImpl extends AbstractSectionsImpl implements SectionAwareServiceHelper {
    private static final Log log = LogFactory.getLog(SectionAwareServiceHelperImpl.class);

	public boolean isUserAbleToGrade(String siteid, String userUid) {
		return (getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.INSTRUCTOR) || getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.TA));
	}

	public boolean isUserAbleToGradeAll(String siteid, String userUid) {
		return getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToGradeSection(String sectionUid, String userUid) {
		return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}

	public boolean isUserAbleToEdit(String siteid, String userUid) {
		return getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserGradable(String siteid, String userUid) {
		return getSectionAwareness().isSiteMemberInRole(siteid, userUid, Role.STUDENT);
	}

	/**
	 */
	public List getAvailableEnrollments(String siteid, String userUid) {
		List enrollments;
		if (isUserAbleToGradeAll(siteid, userUid)) {
			enrollments = getSectionAwareness().getSiteMembersInRole(siteid, Role.STUDENT);
		} else {
			// We use a map because we may have duplicate students among the section
			// participation records.
			Map enrollmentMap = new HashMap();
			List sections = getAvailableSections(siteid, userUid);
			for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
				CourseSection section = (CourseSection)iter.next();
				List sectionEnrollments = getSectionEnrollmentsTrusted(section.getUuid(), userUid);
				for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
					EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
				}
			}
			enrollments = new ArrayList(enrollmentMap.values());
		}
		return enrollments;
	}

	public List getAvailableSections(String siteid, String userUid) {
		List availableSections = new ArrayList();

		SectionAwareness sectionAwareness = getSectionAwareness();
		if (sectionAwareness ==null) {
		}
                else {

		// Get the list of sections. For now, just use whatever default
		// sorting we get from the Section Awareness component.
/*
		List sectionCategories = sectionAwareness.getSectionCategories(siteid);
		for (Iterator catIter = sectionCategories.iterator(); catIter.hasNext(); ) {
			String category = (String)catIter.next();
			List sections = sectionAwareness.getSectionsInCategory(siteid, category);
			for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
				CourseSection section = (CourseSection)iter.next();
				if (isUserAbleToGradeAll(siteid, userUid) || isUserAbleToGradeSection(section.getUuid(), userUid)) {
					availableSections.add(section);
				}
			}
		}
                }
*/
                List sections = sectionAwareness.getSections(siteid);
                for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
                        CourseSection section = (CourseSection)iter.next();
                     //    if (isUserAbleToGradeAll(gradebookUid) || isUserAbleToGradeSection(section.getUuid())) {
                                if (isUserAbleToGradeAll(siteid, userUid) || isUserAbleToGradeSection(section.getUuid(), userUid)) {
                                availableSections.add(section);
                        }
                }
                }


		return availableSections;
	}


	private List getSectionEnrollmentsTrusted(String sectionUid, String userUid) {
		return getSectionAwareness().getSectionMembersInRole(sectionUid, Role.STUDENT);
	}

	public List getSectionEnrollments(String siteid, String sectionUid, String userUid) {
		List enrollments;
		if (isUserAbleToGradeAll(siteid, userUid) || isUserAbleToGradeSection(sectionUid, userUid)) {
			enrollments = getSectionEnrollmentsTrusted(sectionUid, userUid);
		} else {
			enrollments = new ArrayList();
			log.warn("getSectionEnrollments for sectionUid=" + sectionUid + " called by unauthorized userUid=" + userUid);
		}
		return enrollments;
	}

	public List findMatchingEnrollments(String siteid, String searchString, String optionalSectionUid, String userUid) {
		List enrollments;
        List allEnrollmentsFilteredBySearch = getSectionAwareness().findSiteMembersInRole(siteid, Role.STUDENT, searchString);

		if (allEnrollmentsFilteredBySearch.isEmpty() ||
			((optionalSectionUid == null) && isUserAbleToGradeAll(siteid, userUid))) {
			enrollments = allEnrollmentsFilteredBySearch;
		} else {
			if (optionalSectionUid == null) {
				enrollments = getAvailableEnrollments(siteid, userUid);
			} else {
				// The user has selected a particular section.
				enrollments = getSectionEnrollments(siteid, optionalSectionUid, userUid);
			}
			Set availableStudentUids = FacadeUtils.getStudentUids(enrollments);
			enrollments = new ArrayList();
			for (Iterator iter = allEnrollmentsFilteredBySearch.iterator(); iter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				if (availableStudentUids.contains(enr.getUser().getUserUid())) {
					enrollments.add(enr);
				}
			}
		}

		return enrollments;
	}


        public boolean isSectionMemberInRoleStudent(String sectionId, String studentId) {
                return getSectionAwareness().isSectionMemberInRole(sectionId, studentId, Role.STUDENT);
        }

}
