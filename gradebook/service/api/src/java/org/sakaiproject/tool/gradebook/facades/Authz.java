/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades;

import java.util.List;

/**
 * Facade to external role and authorization service.
 */
public interface Authz {
	public boolean isUserAbleToGrade(String gradebookUid);
	public boolean isUserAbleToGradeAll(String gradebookUid);
	public boolean isUserAbleToGradeSection(String sectionUid);
	public boolean isUserAbleToEditAssessments(String gradebookUid);
	public boolean isUserAbleToViewOwnGrades(String gradebookUid);

	/**
	 * This method is used by the external gradebook service but not
	 * by the gradebook application itself.
	 */
	public boolean isUserAbleToGradeStudent(String gradebookUid, String studentUid);

	/**
	 * @return
	 *	an EnrollmentRecord list for each student that the current user
	 *  is allowed to grade.
	 */
	public List getAvailableEnrollments(String gradebookUid);

	/**
	 * @return
	 *	a CourseSection list for each group that the current user
	 *  belongs to.
	 */
	public List getAvailableSections(String gradebookUid);
	
	/**
	 * @param gradebookUid
	 * @return all of the CourseSections for this site
	 */
	public List getAllSections(String gradebookUid);

	/**
	 * The section enrollment list will not be returned unless the user
	 * has access to it.
	 *
	 * @return
	 *  an EnrollmentRecord list for all the students in the given group.
	 */
	public List getSectionEnrollments(String gradebookUid, String sectionUid);

	/**
	 * @param searchString
	 *  a substring search for student name or display UID; the exact rules are
	 *  up to the implementation
	 *
	 * @param optionalSectionUid
	 *  null if the search should be made across all sections
	 *
	 * @return
	 *  an EnrollmentRecord list for all matching available students.
	 */
	public List findMatchingEnrollments(String gradebookUid, String searchString, String optionalSectionUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param studentUid
	 * @return a list of all section memberships for the given studentUid
	 */
	public List findStudentSectionMemberships(String gradebookUid, String studentUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param studentUid
	 * @return a list of the section membership names for the give studentUid
	 */
	public List getStudentSectionMembershipNames(String gradebookUid, String studentUid);
}
