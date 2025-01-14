/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.grading.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;

/**
 * Facade to external role and authorization service.
 */
public interface GradingAuthz {

    public static final String
        PERMISSION_GRADE_ALL = "gradebook.gradeAll",
        PERMISSION_GRADE_SECTION = "gradebook.gradeSection",
        PERMISSION_EDIT_ASSIGNMENTS = "gradebook.editAssignments",
        PERMISSION_VIEW_OWN_GRADES = "gradebook.viewOwnGrades",
        PERMISSION_VIEW_STUDENT_NUMBERS = "gradebook.viewStudentNumbers";

    public boolean isUserAbleToGrade(String siteId);
    public boolean isUserAbleToGrade(String siteId, String userUid);
    public boolean isUserAbleToGradeAll(String siteId);
    public boolean isUserAbleToGradeAll(String siteId, String userUid);
    public boolean isUserAbleToEditAssessments(String siteId);
    public boolean isUserAbleToViewOwnGrades(String siteId);
    public boolean isUserAbleToViewStudentNumbers(String siteId);

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @param itemId
     * @param studentUid
     * @return is user authorized to grade this gradebook item for this student?
     *      first checks for special grader perms. if none, uses default perms
     */
    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String siteId, Long itemId, String studentUid)  throws IllegalArgumentException;

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @param itemId
     * @param studentUid
     * @return is user authorized to view this gradebook item for this student?
     *      first checks for special grader perms. if none, uses default perms
     */
    public boolean isUserAbleToViewItemForStudent(String gradebookUid, String siteId, Long itemId, String studentUid)  throws IllegalArgumentException;

    /**
     * @param siteId
     * @return all of the CourseSections for this site
     */
    public List<CourseSection> getAllSections(String siteId);

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @return all CourseSections that the current user may view or grade
     */
    public List<CourseSection> getViewableSections(String gradebookUid, String siteId);

    /**
     * @param userUid
     * @param gradebookUid
	 * @param siteId
     * @param categoryId
     *          The category id that the desired item is associated with
     * @param gbCategoryType
     *          The category type setting for this gradebook
     * @param optionalSearchString
     *          a substring search for student name or display UID; the exact rules are
     *          up to the implementation - leave null to use all students
     * @param optionalSectionUid
     *          null if the search should be made across all sections
     * @return a map of EnrollmentRecords to grade/view permission that the given user is authorized to
     *          view or grade for the given gradebook item
     */
    public Map<EnrollmentRecord, String> findMatchingEnrollmentsForItemForUser(String userUid, String gradebookUid, String siteId, Long categoryId, Integer gbCategoryType, String optionalSearchString, String optionalSectionUid);

    /**
     *
     * @param gradebookUid
	 * @param siteId
     * @param categoryId
     *          The category id that the desired item is associated with
     * @param gbCategoryType
     *          The category type setting for this gradebook
     * @param optionalSearchString
     *          a substring search for student name or display UID; the exact rules are
     *          up to the implementation - leave null to use all students
     * @param optionalSectionUid
     *          null if the search should be made across all sections
     * @return a map of EnrollmentRecords to grade/view permission that the current user is authorized to
     *          view or grade for the given gradebook item
     */
    public Map<EnrollmentRecord, String> findMatchingEnrollmentsForItem(String gradebookUid, String siteId, Long categoryId, Integer gbCategoryType, String optionalSearchString, String optionalSectionUid);

    /**
     * Check to see if current user may grade or view the given student for the given item in the given gradebook.
     * Returns string representation of function per GradingService vars (view/grade) or null if no permission
     * @param gradebookUid
     * @param siteId
     * @param itemId
     * @param studentUid
     * @return GradingService.gradePermission, GradingService.viewPermission, or null if no permission
     */
    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String siteId, Long itemId, String studentUid);
}
