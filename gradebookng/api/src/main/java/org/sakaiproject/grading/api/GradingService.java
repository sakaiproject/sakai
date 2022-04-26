/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.grading.api;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

/**
 * This is the externally exposed API of the gradebook application.
 *
 * This interface is principally intended for clients of application services -- that is, clients who want to "act like the Gradebook would"
 * to automate what would normally be done in the UI, including any authorization checks.
 *
 * As a result, these methods may throw security exceptions. Call the service's authorization-check methods if you want to avoid them.
 *
 * <p>
 * WARNING: For documentation of the deprecated methods, please see the service interfaces which own them.
 */
public interface GradingService extends EntityProducer {
    // Application service hooks.

    // These have been deprecated in favour of the {@link GradingType} enum
    @Deprecated
    public static final int GRADE_TYPE_POINTS = 1;
    @Deprecated
    public static final int GRADE_TYPE_PERCENTAGE = 2;
    @Deprecated
    public static final int GRADE_TYPE_LETTER = 3;

    public static final int CATEGORY_TYPE_NO_CATEGORY = 1;
    public static final int CATEGORY_TYPE_ONLY_CATEGORY = 2;
    public static final int CATEGORY_TYPE_WEIGHTED_CATEGORY = 3;
    public static final String SAKAI_GBASSIGNMENT = "sakai:gbassignment";

    public static final String[] validLetterGrade = { "a+", "a", "a-", "b+", "b", "b-",
            "c+", "c", "c-", "d+", "d", "d-", "f" };

    // These Strings have been kept for backwards compatibility as they are used everywhere,
    // however the {@link GraderPermission} enum should be used going forward.
    @Deprecated
    public static final String gradePermission = GraderPermission.GRADE.toString();
    @Deprecated
    public static final String viewPermission = GraderPermission.VIEW.toString();
    @Deprecated
    public static final String noPermission = GraderPermission.NONE.toString();

    public static final String enableLetterGradeString = "gradebook_enable_letter_grade";

    public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_DOWN);

    /**
     * An enum for defining valid/invalid information for a points possible/relative weight value for a gradebook item. See
     * {@link GradingService#isPointsPossibleValid(String, Assignment, Double)} for usage
     */
    public enum PointsPossibleValidation {
        /**
         * The points possible/relative weight is valid
         */
        VALID,
        /**
         * The points possible/relative weight is invalid because it is null and a value is required.
         */
        INVALID_NULL_VALUE,
        /**
         * The points possible/relative weight is invalid because it is a value <= 0
         */
        INVALID_NUMERIC_VALUE,
        /**
         * The points possible/relative weight is invalid because it contains more than 2 decimal places
         */
        INVALID_DECIMAL
    }

    /**
     * Array of chars that are not allowed at the beginning of a gb item title
     */
    public static final String[] INVALID_CHARS_AT_START_OF_GB_ITEM_NAME = { "#", "*", "[" };

    /**
     * Comparator to ensure correct ordering of letter grades, catering for + and - in the grade This is duplicated in GradebookNG. If
     * changing here, please change there as well. TODO combine them
     */
    public static Comparator<String> lettergradeComparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (o1.toLowerCase().charAt(0) == o2.toLowerCase().charAt(0)) {
                // only take the first 2 chars, to cater for GradePointsMapping as well
                String s1 = StringUtils.trim(StringUtils.left(o1, 2));
                String s2 = StringUtils.trim(StringUtils.left(o2, 2));

                if (s1.length() == 2 && s2.length() == 2) {
                    if (s1.charAt(1) == '+') {
                        return -1; // SAK-30094
                    } else {
                        return 1;
                    }
                }
                if (s1.length() == 1 && s2.length() == 2) {
                    if (o2.charAt(1) == '+') {
                        return 1; // SAK-30094
                    } else {
                        return -1;
                    }
                }
                if (s1.length() == 2 && s2.length() == 1) {
                    if (s1.charAt(1) == '+') {
                        return -1; // SAK-30094
                    } else {
                        return 1;
                    }
                }
                return 0;
            } else {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        }
    };

    /**
     * Check to see if the current user is allowed to grade the given item for the given student in the given gradebook. This will give
     * clients a chance to avoid a security exception.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     */
    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long assignmentId, String studentUid);

    /**
     * Check to see if the current user is allowed to view the given item for the given student in the given gradebook. This will give
     * clients a chance to avoid a security exception.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @return
     */
    public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long assignmentId, String studentUid);

    /**
     * Check to see if current user may grade or view the given student for the given item in the given gradebook. Returns string
     * representation of function per GradingService vars (view/grade) or null if no permission
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @return GradingService.gradePermission, GradingService.viewPermission, or null if no permission
     */
    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long assignmentId, String studentUid);


    /**
     * @return Returns a list of Assignment objects describing the assignments that are currently defined in the given gradebook.
     */
    public List<Assignment> getAssignments(String gradebookUid);

    /**
     * @return Returns a list of Assignment objects describing the assignments that are currently defined in the given gradebook, sorted by
     *         the given sort type.
     */
    public List<Assignment> getAssignments(String gradebookUid, SortType sortBy);

    /**
     * Get an assignment based on its id
     *
     * @param gradebookUid
     * @param assignmentId
     * @return the associated Assignment with the given assignmentId
     * @throws AssessmentNotFoundException
     */
    public Assignment getAssignment(String gradebookUid, Long assignmentId) throws AssessmentNotFoundException;

    /**
     * Get an assignment based on its name. This is provided for backward compatibility only.
     *
     * @param gradebookUid
     * @param assignmentName
     * @return the associated Assignment with the given name
     * @throws AssessmentNotFoundException
     *
     * @deprecated Use {@link #getAssignment(String,Long)} instead.
     */
    @Deprecated
    public Assignment getAssignment(String gradebookUid, String assignmentName)
            throws AssessmentNotFoundException;

    public Assignment getExternalAssignment(String gradebookUid, String externalId);

    /**
     * Get an assignment based on its name or id. This is intended as a migration path from the deprecated
     * {@link #getAssignment(String,String)} to the new {@link #getAssignment(String,Long)}
     *
     * This method will attempt to lookup the name as provided then fall back to the ID as a Long (If it is a Long) You should use
     * {@link #getAssignment(String,Long)} if you always can use the Long instead.
     *
     * @param gradebookUid
     * @param assignmentName
     * @return the associated Assignment with the given name
     * @throws AssessmentNotFoundException
     *
     */
    public Assignment getAssignmentByNameOrId(String gradebookUid, String assignmentName)
            throws AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @return Returns a GradeDefinition for the student, respecting the grade entry type for the gradebook (ie in %, letter grade, or
     *         points format). Returns null if no grade
     * @throws AssessmentNotFoundException
     */
    public GradeDefinition getGradeDefinitionForStudentForItem(String gradebookUid,
            Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Get the comment (if any) currently provided for the given combination of student and assignment.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @return null if no comment is avaailable
     * @throws AssessmentNotFoundException
     */
    public CommentDefinition getAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @return
     * @throws AssessmentNotFoundException
     */
    public boolean getIsAssignmentExcused(String gradebookUid, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Provide a student-viewable comment on the score (or lack of score) associated with the given assignment.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @param comment a plain text comment, or null to remove any currrent comment
     * @throws AssessmentNotFoundException
     */
    public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, String comment)
            throws AssessmentNotFoundException;


    /**
     * Check to see if an assignment with the given name already exists in the given gradebook. This will give clients a chance to avoid the
     * ConflictingAssignmentNameException.
     *
     * This is not deprecated as we currently need the ability to check for duplciate assignment names in the given gradebook
     *
     */
    public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle);

    /**
     * Transfer the gradebook information and assignments from one gradebook to another
     *
     * @param gradebookInformation GradebookInformation to copy
     * @param assignments list of Assignments to copy
     * @param toGradebookUid target gradebook uid
     */
    public Map<String,String> transferGradebook(GradebookInformation gradebookInformation, List<Assignment> assignments,
            String toGradebookUid, String fromContext);

    /**
     *
     * @param gradebookUid
     * @return a {@link GradebookInformation} object that contains information about this Gradebook that may be useful to consumers outside
     *         the Gradebook tool
     *
     */
    public GradebookInformation getGradebookInformation(String gradebookUid);

    /**
     * Removes an assignment from a gradebook. The assignment should not be deleted, but the assignment and all grade records associated
     * with the assignment should be ignored by the application. A removed assignment should not count toward the total number of points in
     * the gradebook.
     *
     * @param assignmentId The assignment id
     */
    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException;

    /**
     *
     * Get the categories for the given gradebook. This method cannot be used outside of the gradebook because it returns the
     * org.sakaiproject.tool.gradebook.Category object. If you require info on the categories from a consumer outside the gradebook, use
     * {@link #getCategoryDefinitions(String)}
     *
     * @param gradebookId
     * @return List of categories
     * @throws HibernateException
     *
     * @deprecated
     */
    @Deprecated
    public List<Category> getCategories(Long gradebookId);

    /**
     * Get the categories for the given gradebook
     *
     * @param gradebookUid
     * @return {@link CategoryDefinition}s for the categories defined for the given gradebook. Returns an empty list if the gradebook does
     *         not have categories.
     */
    public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid);

    /**
     * remove category from gradebook
     *
     * @param categoryId
     * @throws StaleObjectModificationException
     */

    public void removeCategory(Long categoryId) throws StaleObjectModificationException;

    /**
     * Create a new Gradebook-managed assignment.
     *
     * @param assignmentDefinition
     * @return the id of the newly created assignment
     */
    public Long addAssignment(String gradebookUid, Assignment assignmentDefinition);

    /**
     * Modify the definition of an existing Gradebook item.
     *
     * Clients should be aware that it's allowed to change the points value of an assignment even if students have already been scored on
     * it. Any existing scores will not be adjusted.
     *
     * This method can be used to manage both internal and external gradebook items, however the title, due date and total points will not
     * be edited for external gradebook items.
     *
     * @param assignmentId the id of the assignment that needs to be changed
     * @param assignmentDefinition the new properties of the assignment
     */
    public void updateAssignment(String gradebookUid, Long assignmentId, Assignment assignmentDefinition);

    /**
     *
     * @param gradebookUid
     * @return list of gb items that the current user is authorized to view. If user has gradeAll permission, returns all gb items. If user
     *         has gradeSection perm with no grader permissions, returns all gb items. If user has gradeSection with grader perms, returns
     *         only the items that the current user is authorized to view or grade. If user does not have grading privileges but does have
     *         viewOwnGrades perm, will return all released gb items.
     */
    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid);

    /**
     *
     * @param gradebookUid
     * @return list of gb items that the current user is authorized to view sorted by the provided SortType. If user has gradeAll
     *         permission, returns all gb items. If user has gradeSection perm with no grader permissions, returns all gb items. If user has
     *         gradeSection with grader perms, returns only the items that the current user is authorized to view or grade. If user does not
     *         have grading privileges but does have viewOwnGrades perm, will return all released gb items.
     */
    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid, SortType sortBy);

    /**
     *
     * @param gradebookUid
     * @param assignmentId
     * @return a map of studentId to view/grade function for the given gradebook and gradebook item. students who are not viewable or
     *         gradable will not be returned. if the current user does not have grading privileges, an empty map is returned
     */
    public Map<String, String> getViewableStudentsForItemForCurrentUser(String gradebookUid, Long assignmentId);

    /**
     * @param userUid
     * @param gradebookUid
     * @param assignmentId
     * @return a map of studentId to view/grade function for the given gradebook and gradebook item that the given userUid is allowed to
     *         view or grade. students who are not viewable or gradable will not be returned. if the given user does not have grading
     *         privileges, an empty map is returned
     */
    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, Long assignmentId);

    /**
     * Get the Gradebook. Note that this returns Object to avoid circular dependency with sakai-gradebook-tool Consumers will need to cast
     * to {@link org.sakaiproject.tool.gradebook.Gradebook}
     *
     */
    public Gradebook getGradebook(String uid);

    /**
     * Check if there are students that have not submitted
     *
     * @param gradebookUid
     * @return
     */
    public boolean checkStudentsNotSubmitted(String gradebookUid);

    /**
     * Check if a gradeable object with the given id exists
     *
     * @param gradableObjectId
     * @return true if a gradable object with the given id exists and was not removed
     */
    public boolean isGradableObjectDefined(Long gradableObjectId);

    /**
     * Using the grader permissions, return map of section uuid to section name that includes all sections that the current user may view or
     * grade
     *
     * @param gradebookUid
     * @return
     */
    public Map<String, String> getViewableSectionUuidToNameMap(String gradebookUid);

    /**
     * Check if the current user has the gradebook.gradeAll permission
     *
     * @param gradebookUid
     * @return true if current user has the gradebook.gradeAll permission
     */
    public boolean currentUserHasGradeAllPerm(String gradebookUid);

    /**
     * Check if the given user is allowed to grade all students in this gradebook
     *
     * @param gradebookUid
     * @param userUid
     * @return true if the given user is allowed to grade all students in this gradebook
     */
    public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid);

    /**
     * @param gradebookUid
     * @return true if the current user has some form of grading privileges in the gradebook (grade all, grade section, etc)
     */
    public boolean currentUserHasGradingPerm(String gradebookUid);

    /**
     *
     * @param gradebookUid
     * @param userUid
     * @return true if the given user has some form of grading privileges in the gradebook (grade all, grade section, etc)
     */
    public boolean isUserAllowedToGrade(String gradebookUid, String userUid);

    /**
     * @param gradebookUid
     * @return true if the current user has the gradebook.editAssignments permission
     */
    public boolean currentUserHasEditPerm(String gradebookUid);

    /**
     * @param gradebookUid
     * @return true if the current user has the gradebook.viewOwnGrades permission
     */
    public boolean currentUserHasViewOwnGradesPerm(String gradebookUid);

    /**
     * @param gradebookUid
     * @return true if the current user has the gradebook.viewStudentNumbers permission
     */
    public boolean currentUserHasViewStudentNumbersPerm(String gradebookUid);

    /**
     * Get the grade records for the given list of students and the given assignment. This can only be called by an instructor or TA that
     * has access, not student.
     *
     * See {@link #getGradeDefinitionForStudentForItem} for the method call that can be made as a student.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentIds
     * @return a list of GradeDefinition with the grade information for the given students for the given gradableObjectId
     * @throws SecurityException if the current user is not authorized to view or grade a student in the passed list
     */
    public List<GradeDefinition> getGradesForStudentsForItem(String gradebookUid, Long assignmentId, List<String> studentIds);

    /**
     * This method gets grades for multiple gradebook items with emphasis on performance. This is particularly useful for reporting tools
     *
     * @param gradebookUid
     * @param gradableObjectIds
     * @param studentIds
     * @return a Map of GradableObjectIds to a List of GradeDefinitions containing the grade information for the given students for the
     *         given gradableObjectIds. Comments are excluded which can be useful for performance. If a student does not have a grade on a
     *         gradableObject, the GradeDefinition will be omitted
     * @throws SecurityException if the current user is not authorized with gradeAll in this gradebook
     * @throws IllegalArgumentException if gradableObjectIds is null/empty, or if gradableObjectIds contains items that are not members of
     *             the gradebook with uid = gradebookUid
     */
    public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(String gradebookUid, List<Long> gradableOjbectIds,
            List<String> studentIds);

    /**
     *
     * @param gradebookUuid
     * @param grade
     * @return true if the given grade is a valid grade given the gradebook's grade entry type. ie, if gradebook is set to grade entry by
     *         points, will check for valid point value. if entry by letter, will check for valid letter, etc
     */
    public boolean isGradeValid(String gradebookUuid, String grade);

    /**
     * Determines if the given string contains a valid numeric grade.
     * @param grade the grade as a string, expected to contain a numeric value
     * @return true if the string contains a valid numeric grade
     */
    public boolean isValidNumericGrade(String grade);

    /**
     *
     * @param gradebookUid
     * @param studentIdToGradeMap - the student's username mapped to their grade that you want to validate
     * @return a list of the studentIds that were associated with invalid grades given the gradebook's grade entry type. useful if
     *         validating a list of student/grade pairs for a single gradebook (more efficient than calling gradeIsValid repeatedly).
     *         returns empty list if all grades are valid
     */
    public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap);

    /**
     * Save a student score and comment for a gradebook item. The input score must be valid according to the given gradebook's grade entry
     * type.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentId
     * @param grade - must be in format according to gradebook's grade entry type
     * @param comment
     * @throws InvalidGradeException - if grade is invalid. grade and comment will not be saved
     * @throws AssessmentNotFoundException
     * @throws SecurityException if current user is not authorized to grade student
     */
    public void saveGradeAndCommentForStudent(String gradebookUid, Long assignmentId, String studentId, String grade, String comment)
            throws InvalidGradeException, AssessmentNotFoundException;

    /**
     * Given a list of GradeDefinitions for students for a given gradebook and gradable object, will save the associated scores and
     * comments. Scores must be in a format according to the gradebook's grade entry type (ie points, %, letter).
     *
     * @param gradebookUid
     * @param assignmentId
     * @param gradeDefList
     * @throws InvalidGradeException if any of the grades are not valid - none will be saved
     * @throws SecurityException if the user does not have access to a student in the list - no grades or comments will be saved for any
     *             student
     * @throws AssessmentNotFoundException
     */
    public void saveGradesAndComments(String gradebookUid, Long assignmentId, List<GradeDefinition> gradeDefList)
            throws InvalidGradeException, AssessmentNotFoundException;

    public void saveGradeAndExcuseForStudent(String gradebookUid, Long assignmentId, String studentId, String grade, boolean excuse)
        throws InvalidGradeException, AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @return the constant representation of the grade entry type (ie points, %, letter grade)
     */
    public GradeType getGradeEntryType(String gradebookUid);

    /**
     * Get a Map of overridden CourseGrade for students.
     *
     * @param gradebookUid
     * @return Map of enrollment displayId as key, point as value string
     *
     */
    public Map<String, String> getEnteredCourseGrade(String gradebookUid);

    /**
     * Get student's assignment's score as string.
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @return String of score
     */
    public String getAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Get student's assignment's score as string. This is provided for backward compatibility only.
     *
     * @param gradebookUid
     * @param assignmentName
     * @param studentUid
     * @return String of score
     *
     * @deprecated See {@link #getAssignmentScoreString(String, Long, String)}
     */
    @Deprecated
    public String getAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Get student's assignment's score as string.
     *
     * This is intended as a migration path from the deprecated {@link #getAssignmentScoreString(String,String)} to the new
     * {@link #getAssignmentScoreString(String,Long)}
     *
     * This method will attempt to lookup the name as provided then fallback to the ID as a Long (If it is a Long) You should use
     * {@link #getAssignmentScoreString(String,Long)} if you always can use the Long instead.
     *
     * @param gradebookUid
     * @param assignmentName
     * @param studentUid
     * @return String of score
     */
    public String getAssignmentScoreStringByNameOrId(String gradebookUid, String assignmentName, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Set student's score for assignment.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @param score
     * @param clientServiceDescription
     *
     */
    public void setAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid, String score,
            String clientServiceDescription)
            throws AssessmentNotFoundException;

    /**
     * Set student's score for assignment. This is provided for backward compatibility only.
     *
     * @param gradebookUid
     * @param assignmentName
     * @param studentUid
     * @param score
     * @param clientServiceDescription
     *
     * @deprecated See {@link #setAssignmentScoreString(String, Long, String, String, String)}
     */
    @Deprecated
    public void setAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid, String score,
            String clientServiceDescription)
            throws AssessmentNotFoundException;


    /**
     * Finalize the gradebook's course grades by setting all still-unscored assignments to zero scores.
     *
     * @param gradebookUid
     */
    public void finalizeGrades(String gradebookUid);

    /**
     *
     * @param gradebookUid
     * @param assignmentId
     * @return the lowest possible grade allowed for the given assignmentId. For example, in a points or %-based gradebook, the lowest
     *         possible grade for a gradebook item is 0. In a letter-grade gb, it may be 'F' depending on the letter grade mapping. Ungraded
     *         items have a lowest value of null.
     * @throws SecurityException if user does not have permission to view assignments in the given gradebook
     * @throws AssessmentNotFoundException if there is no gradebook item with the given gradebookItemId
     */
    public String getLowestPossibleGradeForGbItem(String gradebookUid, Long assignmentId);

    /**
     *
     * @param gradebookUid (non-null)
     * @param assignment (non-null) the Assignment object representing the gradebook item for which you are setting the points possible (aka
     *            relative weight). May be a new gradebook item without an id yet.
     * @param pointsPossible the points possible/relative weight you would like to validate for the gradebookItem above.
     * @return {@link PointsPossibleValidation} value indicating the validity of the given points possible/relative weight or a problem code
     *         defining why it is invalid
     */
    public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, Assignment assignment, Double pointsPossible);

    /**
     * Computes the Average Course Grade as a letter.
     *
     * @param gradebookUid
     * @return
     */
    public String getAverageCourseGrade(String gradebookUid);

    /**
     * Update the ordering of an assignment. This can be performed on internal and external assignments.
     * @param gradebookUid uid of the gradebook
     * @param assignmentId id of the assignment in the gradebook
     * @param order the new order for this assignment. Note it is 0 based index ordering.
     * @return
     */
    public void updateAssignmentOrder(String gradebookUid, Long assignmentId, Integer order);

    /**
     * Gets the grading events for the given student and the given assignment
     *
     * @param studentId
     * @param assignmentId
     * @return List of GradingEvent objects.
     */
    public List<GradingEvent> getGradingEvents(String studentId, long assignmentId);

    /**
     * Calculate the category score for the given gradebook, student and category, looking up the grades. Safe to call in context of a
     * student.
     *
     * @param gradebookId Id of the gradebook
     * @param studentUuid uuid of the student
     * @param categoryId id of category
     * @param isInstructor will determine whether category score includes non-released items
     * @param categoryType category type of the gradebook
     * @param equalWeightAssignments whether category is equal-weighting regardless of points
     * @return percentage and dropped items, or empty if no calculations were made
     *
     */
    Optional<CategoryScoreData> calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId, boolean includeNonReleasedItems, GradingCategoryType categoryType, Boolean equalWeightAssignments);

    /**
     * Calculate the category score for the given gradebook, category, assignments in the category and grade map. This doesn't do any
     * additional grade lookups. Safe to call in context of a student.
     *
     * @param gradebook the gradebook. As this method is called for every student at once, this is passed in to save additional lookups by
     *            id.
     * @param studentUuid uuid of the student
     * @param category the category
     * @param categoryAssignments list of assignments the student can view, and are in the category
     * @param gradeMap map of assignmentId to grade, to use for the calculations
     * @param includeNonReleasedItems relevant for student view
     * @return percentage and dropped items, or empty if no calculations were made
     */
    Optional<CategoryScoreData> calculateCategoryScore(Object gradebook, String studentUuid, CategoryDefinition category,
            final List<Assignment> categoryAssignments, Map<Long, String> gradeMap, boolean includeNonReleasedItems);

    /**
     * Get the course grade for a student
     *
     * @param gradebookUid
     * @param userUuid uuid of the user
     * @return The {@link CourseGradeTransferBean} for the student
     */
    CourseGradeTransferBean getCourseGradeForStudent(String gradebookUid, String userUuid);

    /**
     * Get the course grade for a list of students
     *
     * @param gradebookUid
     * @param userUuids uuids of the users
     * @return a Map of {@link CourseGradeTransferBean} for the students. Key is the student uuid.
     */
    Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String gradebookUid, List<String> userUuids);

    /**
     * Get the course grade for a list of students using the given grading schema
     *
     * @param gradebookUid
     * @param userUuids uuids of the users
     * @param schema the grading schema (bottom percents) to use in the calculation
     * @return a Map of {@link CourseGrade} for the students. Key is the student uuid.
     */
    Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String gradebookUid, List<String> userUuids, Map<String, Double> schema);

    /**
     * Get a list of CourseSections that the current user has access to in the given gradebook. This is a combination of sections and groups
     * and is permission filtered.
     *
     * @param gradebookUid
     * @return list of CourseSection objects.
     */
    List<CourseSection> getViewableSections(String gradebookUid);

    /**
     * Update the settings for this gradebook
     *
     * @param gradebookUid
     * @param gbInfo GradebookInformation object
     */
    void updateGradebookSettings(String gradebookUid, GradebookInformation gbInfo);

    /**
     * Return the GradeMappings for the given gradebook. The normal getGradebook(siteId) doesn't return the GradeMapping.
     *
     * @param gradebookId
     * @return Set of GradeMappings for the gradebook
     */
    Set getGradebookGradeMappings(Long gradebookId);

    /**
     * Return the GradeMappings for the given gradebook.
     * @param gradebookUid
     * @return Set of GradeMappings for the gradebook
     */
    Set getGradebookGradeMappings(String gradebookUid);

    /**
     * Allows an instructor to set a course grade override for the given student
     *
     * @param gradebookUid uuid of the gradebook
     * @param studentUuid uuid of the student
     * @param grade the new course grade
     */
    void updateCourseGradeForStudent(String gradebookUid, String studentUuid, String grade, String gradeScale);

    /**
     * Updates the categorized order of an assignment
     *
     * @param gradebookUid uuid of the gradebook
     * @param categoryId id of the category
     * @param assignmentId id of the assignment
     * @param order new position of the assignment
     */
    void updateAssignmentCategorizedOrder(String gradebookUid, Long categoryId, Long assignmentId, Integer order);

    /**
     * Return the grade changes made since a given time
     *
     * @param assignmentIds list of assignment ids to check
     * @param since timestamp from which to check for changes
     * @return set of changes made
     */
    List<GradingEvent> getGradingEvents(List<Long> assignmentIds, Date since);

    /**
     * @deprecated Replaced by
     *      {@link addExternalAssessment(String, String, String, String, Double, Date, String, Boolean)}
     */
    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
            String title, double points, Date dueDate, String externalServiceDescription, String externalData)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException;

    /**
     * Add an externally-managed assessment to a gradebook to be treated as a
     * read-only assignment. The gradebook application will not modify the
     * assessment properties or create any scores for the assessment.
     * Since each assignment in a given gradebook must have a unique name,
     * conflicts are possible.
     * @param gradebookUid
     * @param externalId some unique identifier which Samigo uses for the assessment.
     *                   The externalId is globally namespaced within the gradebook, so
     *                   if other apps decide to put assessments into the gradebook,
     *                   they should prefix their externalIds with a well known (and
     *                   unique within sakai) string.
     * @param externalUrl a link to go to if the instructor or student wants to look at the assessment
     *                    in Samigo; if null, no direct link will be provided in the
     *                    gradebook, and the user will have to navigate to the assessment
     *                    within the other application
     * @param title
     * @param points this is the total amount of points available and must be greater than zero.
     *               It could be null if it's an ungraded item.
     * @param dueDate
     * @param externalServiceDescription
     * @param externalData if there is some data that the external service wishes to store.
     * @param ungraded
     *
     *
     */
    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points,
                                      Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException;

    /**
     * This method is identical to {@link #addExternalAssessment(String, String, String, String, Double, Date, String, String, Boolean)} but
     * allows you to also specify the associated Category for this assignment. If the gradebook is set up for categories and
     * categoryId is null, assignment category will be unassigned
     * @param gradebookUid
     * @param externalId
     * @param externalUrl
     * @param title
     * @param points
     * @param dueDate
     * @param externalServiceDescription
     * @param externalData if there is some data that the external service wishes to store.
     * @param ungraded
     * @param categoryId
     * @throws ConflictingAssignmentNameException
     * @throws ConflictingExternalIdException
     * @throws AssignmentHasIllegalPointsException
     * @throws InvalidCategoryException
     */
    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points,
                                      Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded, Long categoryId)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException, InvalidCategoryException;

        /**
         * @deprecated Replaced by
         *      {@link updateExternalAssessment(String, String, String, String, Double, Date, Boolean)}
         */
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String externalData,
                                         String title, double points, Date dueDate)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;

    /**
     *  Update an external assessment
     * @param gradebookUid
     * @param externalId
     * @param externalUrl
     * @param externalData
     * @param title
     * @param points
     * @param dueDate
     * @param ungraded
     * @throws AssessmentNotFoundException
     * @throws ConflictingAssignmentNameException
     * @throws AssignmentHasIllegalPointsException
     */
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String externalData,
                                         String title, Double points, Date dueDate, Boolean ungraded)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;

    /**
     * Remove the assessment reference from the gradebook. Although Samigo
     * doesn't currently delete assessments, an instructor can retract an
     * assessment to keep it from students. Since such an assessment would
     * presumably no longer be used to calculate final grades, Samigo should
     * also remove that assessment from the gradebook.
     *
     * @param externalId
     *            the UID of the assessment
     */
    public void removeExternalAssignment(String gradebookUid, String externalId)
        throws AssessmentNotFoundException;

  /**
   * Updates an external score for an external assignment in the gradebook.
   *
   * @param gradebookUid
   *    The Uid of the gradebook
   * @param externalId
   *    The external ID of the assignment/assessment
   * @param studentUid
   *    The unique id of the student
   * @param points
   *    The number of points earned on this assessment, or null if a score
   *    should be removed
   */
  public void updateExternalAssessmentScore(String gradebookUid, String externalId,
            String studentUid, String points)
            throws AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @param externalId
     * @param studentUidsToScores
     * @throws AssessmentNotFoundException
     *
     * @deprecated Replaced by
     *      {@link updateExternalAssessmentScoresString(String, String, Map<String, String)}
     */
    public void updateExternalAssessmentScores(String gradebookUid,
        String externalId, Map<String, Double> studentUidsToScores)
        throws AssessmentNotFoundException;

    /**
     * Updates a set of external scores for an external assignment in the gradebook.
     *
     * @param gradebookUid
     *  The Uid of the gradebook
     * @param externalId
     *  The external ID of the assignment/assessment
     * @param studentUidsToScores
     *  A map whose String keys are the unique ID strings of the students and whose
     *  String values are points earned on this assessment or null if the score
     *  should be removed.
     */
    public void updateExternalAssessmentScoresString(String gradebookUid,
            String externalId, Map<String, String> studentUidsToScores)
    throws AssessmentNotFoundException;

    /**
     * Updates an external comment for an external assignment in the gradebook.
     *
     * @param gradebookUid
     *  The Uid of the gradebook
     * @param externalId
     *  The external ID of the assignment/assessment
     * @param studentUid
     *  The unique id of the student
     * @param comment
     *  The comment to be added to this grade, or null if a comment
     *  should be removed
     */
    public void updateExternalAssessmentComment(String gradebookUid,
            String externalId, String studentUid, String comment )
                    throws AssessmentNotFoundException;
    /**
     * Updates a set of external comments for an external assignment in the gradebook.
     *
     * @param gradebookUid
     *  The Uid of the gradebook
     * @param externalId
     *  The external ID of the assignment/assessment
     * @param studentUidsToScores
     *  A map whose String keys are the unique ID strings of the students and whose
     *  String values are comments or null if the comments
     *  should be removed.
     */
    public void updateExternalAssessmentComments(String gradebookUid,
            String externalId, Map<String, String> studentUidsToComments)
                    throws AssessmentNotFoundException;

    /**
     * Check to see if an assignment with the given external id already exists
     * in the given gradebook. This will give external assessment systems
     * a chance to avoid the ConflictingExternalIdException.
     *
     * @param gradebookUid The gradebook's unique identifier
     * @param externalId The external assessment's external identifier
     */
    public boolean isExternalAssignmentDefined(String gradebookUid, String externalId);

    /**
     * Check with the appropriate external service if a specific assignment is
     * available only to groups.
     *
     * @param gradebookUid The gradebook's unique identifier
     * @param externalId The external assessment's external identifier
     */
    public boolean isExternalAssignmentGrouped(String gradebookUid, String externalId);

    /**
     * Check with the appropriate external service if a specific assignment is
     * available to a specific user (i.e., the user is in an appropriate group).
     * Note that this method will return true if the assignment exists in the
     * gradebook and is marked as externally maintained while no provider
     * recognizes it; this is to maintain a safer default (no change from the
     * 2.8 release) for tools that have not implemented a provider.
     *
     * @param gradebookUid The gradebook's unique identifier
     * @param externalId The external assessment's external identifier
     * @param userId The user ID to check
     */
    public boolean isExternalAssignmentVisible(String gradebookUid, String externalId, String userId);

    /**
     * Retrieve all assignments for a gradebook that are marked as externally
     * maintained and are visible to the current user. Assignments may be included
     * with a null providerAppKey, indicating that the gradebook references the
     * assignment, but no provider claims responsibility for it.
     *
     * @param gradebookUid The gradebook's unique identifier
     * @return A map from the externalId of each activity to the providerAppKey
     */
    public Map<String, String> getExternalAssignmentsForCurrentUser(String gradebookUid);

    /**
     * Retrieve a list of all visible, external assignments for a set of users.
     *
     * @param gradebookUid The gradebook's unique identifier
     * @param studentIds The collection of student IDs for which to retrieve assignments
     * @return A map from the student ID to all visible, external activity IDs
     */
    public Map<String, List<String>> getVisibleExternalAssignments(String gradebookUid, Collection<String> studentIds);

    /**
     * Register a new ExternalAssignmentProvider for handling the integration of external
     * assessment sources with the sakai gradebook
     * Registering more than once will overwrite the current with the new one
     *
     * @param provider the provider implementation object
     */
    public void registerExternalAssignmentProvider(ExternalAssignmentProvider provider);

    /**
     * Remove/unregister any ExternalAssignmentProvider which is currently registered,
     * does nothing if they provider does not exist
     *
     * @param providerAppKey the unique app key for a provider
     */
    public void unregisterExternalAssignmentProvider(String providerAppKey);

    /**
     * Break the connection between an external assessment engine and an assessment which
     * it created, giving it up to the Gradebook application to control from now on.
     *
     * @param gradebookUid
     * @param externalId
     */
    public void setExternalAssessmentToGradebookAssignment(String gradebookUid, String externalId);

    /**
     * Get the category of a gradebook with the externalId given
     *
     * @param gradebookUId
     * @param externalId
     * @return
     */
    public Long getExternalAssessmentCategoryId(String gradebookUId, String externalId);

    /**
     * Checks to see whether a gradebook has the categories option enabled.
     *
     * @param gradebookUid
     *            The gradebook UID to check
     * @return Whether the gradebook has categories enabled
     */
    public boolean isCategoriesEnabled(String gradebookUid);

    /**
     * Creates a new gradebook with the given UID
     *
     * @param uid
     *            The UID used to specify a gradebook and its associated data.
     *            It is the caller's responsibility to ensure that this is
     *            unique within gradebook storage.
     */
    public Gradebook addGradebook(String uid);

    /**
     * Deletes the gradebook with the given UID, along with all its associated
     * data.
     */
    public void deleteGradebook(String uid);

    /**
     * @param gradingScaleDefinitions
     *  A collection of GradingScaleDefinition beans.
     */
    public void setAvailableGradingScales(Collection<GradingScaleDefinition> gradingScaleDefinitions);

    /**
     * @param uid
     *  The UID of the grading scale to use as the default for new gradebooks.
     */
    public void setDefaultGradingScale(String uid);

    /**
     *  Get all of the available Grading Scales in the system.
     *  @return List of GradingScale
     */
    public List<GradingScale> getAvailableGradingScales();

    /**
     *  Get all of the available Grading Scales in the system, as shared DTOs.
     *  @return List of GradingScaleDefinition
     */
    public List<GradingScaleDefinition> getAvailableGradingScaleDefinitions();

    /**
     * Adds a new grade scale to an existing gradebook.
     *
     * @param scaleUuid
     *   The uuid of the scale we want to be added to the gradebook
     * @param gradebookUid
     *   The gradebook with GradeMappings where we will add the grading scale.
     *
     */
    public void saveGradeMappingToGradebook(String scaleUuid, String gradebookUid);

    /**
     * Update a grademapping with new values.
     *
     * @param gradeMappingId id of GradeMapping to update
     * @param gradeMap the updated map of grades
     *
     */
    public void updateGradeMapping(Long gradeMappingId, Map<String, Double> gradeMap);
}
