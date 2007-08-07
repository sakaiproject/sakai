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
import java.util.Map;

/**
 * Facade to external role and authorization service.
 */
public interface Authz {
	public boolean isUserAbleToGrade(String gradebookUid);
	public boolean isUserAbleToGradeAll(String gradebookUid);
	public boolean isUserAbleToEditAssessments(String gradebookUid);
	public boolean isUserAbleToViewOwnGrades(String gradebookUid);
	public boolean isUserHasGraderPermissions(String gradebookUid);
	public boolean isUserHasGraderPermissions(Long gradebookId, String userUid);

	/**
	 * 
	 * @param gradebookUid
	 * @param itemId
	 * @param studentUid
	 * @return is user authorized to grade this gradebook item for this student?
	 * 		first checks for special grader perms. if none, uses default perms
	 */
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid)  throws IllegalArgumentException;
	
	/**
	 * 
	 * @param gradebookUid
	 * @param itemName
	 * @param studentUid
	 * @return is user authorized to grade this gradebook item for this student?
	 * 		first checks for special grader perms. if none, uses default perms
	 */
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid) throws IllegalArgumentException;
	
	/**
	 * @param gradebookUid
	 * @return all of the CourseSections for this site
	 */
	public List getAllSections(String gradebookUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @return all CourseSections that the current user may view or grade
	 */
	public List getViewableSections(String gradebookUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param categoryId
	 * 			The category id that the desired item is associated with
	 * @param optionalSearchString
	 * 			a substring search for student name or display UID; the exact rules are
	 *  		up to the implementation - leave null to use all students
	 * @param optionalSectionUid
	 * 			null if the search should be made across all sections
	 * @return a map of EnrollmentRecords to grade/view permission that the current user is authorized to
	 * 			view or grade for the given gradebook item
	 */
	public Map findMatchingEnrollmentsForItem(String gradebookUid, Long categoryId, String optionalSearchString, String optionalSectionUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param optionalSearchString
	 * @param optionalSectionUid
	 * @return Map of EnrollmentRecord --> function (grade/view) for all students that the current user has permission to
	 * 			view/grade every item in the gradebook. If he/she can grade everything, GRADE function is
	 * 			returned. Otherwise, function is VIEW. May only modify course grade if he/she can grade
	 * 			everything in the gradebook for that student. If he/she can grade only a subset of the items, the
	 * 			student is not returned.
	 */
	public Map findMatchingEnrollmentsForViewableCourseGrade(String gradebookUid, String optionalSearchString, String optionalSectionUid);
	/**
	 * 
	 * @param gradebookUid
	 * @param optionalSearchString
	 * 			a substring search for student name or display UID; the exact rules are
	 *  		up to the implementation - leave null to use all students
	 * @param optionalSectionUid
	 * 			null if the search should be made across all sections
	 * @return a map of EnrollmentRecords to a map of item id and function (grade/view) that the user is
	 * 			authorized to view/grade
	 */
	public Map findMatchingEnrollmentsForViewableItems(String gradebookUid, String optionalSearchString, String optionalSectionUid);
	
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
