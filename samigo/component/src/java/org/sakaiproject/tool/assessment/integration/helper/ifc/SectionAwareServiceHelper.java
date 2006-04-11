/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana
* University, Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.assessment.integration.helper.ifc;

import java.util.*;


/**
 * Facade to external role and authorization service.
 * based Ray's gradebook code
 */
public interface SectionAwareServiceHelper{
	public boolean isUserAbleToGrade(String siteid, String userUid);
	public boolean isUserAbleToGradeAll(String siteid, String userUid);
	public boolean isUserAbleToGradeSection(String sectionUid, String userUid);
	public boolean isUserAbleToEdit(String siteid, String userUid);
	public boolean isUserGradable(String siteid, String userUid);

	/**
	 * @return
	 *	an EnrollmentRecord list for each student that the current user
	 *  is allowed to grade.
	 */
	public List getAvailableEnrollments(String siteid, String userUid);

	/**
	 * @return
	 *	a CourseSection list for each group that the current user
	 *  belongs to.
	 */
	public List getAvailableSections(String siteid, String userUid);

	/**
	 * The section enrollment list will not be returned unless the user
	 * has access to it.
	 *
	 * @return
	 *  an EnrollmentRecord list for all the students in the given group.
	 */
	public List getSectionEnrollments(String siteid, String sectionUid, String userUid);

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
	public List findMatchingEnrollments(String siteid, String searchString, String optionalSectionUid, String userUid);

 /**
  * @param sectionId
  *
  * @param studentId
  *
  * @param  Role 
  * @return
  *  whether a member belongs to a section under a certain role
  */

	public boolean isSectionMemberInRoleStudent(String sectionId, String studentId);

}

