/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
}
