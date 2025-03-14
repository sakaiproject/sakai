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
import java.util.Set;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.GradeMapping;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookManager;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.site.api.Site;

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

    MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_DOWN);

    /**
     * An enum for defining valid/invalid information for a points possible/relative weight value for a gradebook item. See
     * {@link GradingService#isPointsPossibleValid(Assignment, Double)} for usage
     */
    enum PointsPossibleValidation {
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
    String[] INVALID_CHARS_AT_START_OF_GB_ITEM_NAME = { "#", "*", "[" };

    /**
     * Comparator to ensure correct ordering of letter grades, catering for + and - in the grade This is duplicated in GradebookNG. If
     * changing here, please change there as well. TODO combine them
     */
    Comparator<String> lettergradeComparator = (o1, o2) -> {
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
    };

    /**
     * Check to see if the current user is allowed to view the list of gradebook assignments.
     *
     * @param siteId
     */
    boolean isUserAbleToViewAssignments(String siteId);

    /**
     * Check to see if the current user is allowed to grade the given item for the given student in the given gradebook. This will give
     * clients a chance to avoid a security exception.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     */
    boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long assignmentId, String studentUid);

    /**
     * Check to see if the current user is allowed to view the given item for the given student in the given gradebook. This will give
     * clients a chance to avoid a security exception.
     *
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return
     */
    boolean isUserAbleToViewItemForStudent(String siteId, Long assignmentId, String studentUid);

    /**
     * Check to see if current user may grade or view the given student for the given item in the given gradebook. Returns string
     * representation of function per GradingService vars (view/grade) or null if no permission
     *
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return GradingService.gradePermission, GradingService.viewPermission, or null if no permission
     */
    String getGradeViewFunctionForUserForStudentForItem(String siteId, Long assignmentId, String studentUid);


    /**
     * @return Returns a list of Assignment objects describing the assignments that are currently defined in the given gradebook.
     */
    List<Assignment> getAssignments(String siteId);

    /**
     * @return Returns a list of Assignment objects describing the assignments that are currently defined in the given gradebook, sorted by
     *         the given sort type.
     */
    List<Assignment> getAssignments(String siteId, SortType sortBy);

    /**
     * Get an assignment based on its id
     *
     * @param siteId
     * @param assignmentId
     * @return the associated Assignment with the given assignmentId
     * @throws AssessmentNotFoundException
     */
    Assignment getAssignment(String siteId, Long assignmentId) throws AssessmentNotFoundException;

    /**
     * Get an assignment based on its name. This is provided for backward compatibility only.
     *
     * @param siteId
     * @param assignmentName
     * @return the associated Assignment with the given name
     * @throws AssessmentNotFoundException
     * @deprecated Use {@link #getAssignment(String, Long)} instead.
     */
    @Deprecated
    Assignment getAssignment(String siteId, String assignmentName)
            throws AssessmentNotFoundException;

    Assignment getExternalAssignment(String siteId, String externalId);

    /**
     * Get an assignment based on its name or id. This is intended as a migration path from the deprecated
     * {@link #getAssignment(String, String)} to the new {@link #getAssignment(String, Long)}
     * <p>
     * This method will attempt to lookup the name as provided then fall back to the ID as a Long (If it is a Long) You should use
     * {@link #getAssignment(String, Long)} if you always can use the Long instead.
     *
     * @param siteId
     * @param assignmentName
     * @return the associated Assignment with the given name
     * @throws AssessmentNotFoundException
     */
    Assignment getAssignmentByNameOrId(String siteId, String assignmentName)
            throws AssessmentNotFoundException;

    /**
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return Returns a GradeDefinition for the student, respecting the grade entry type for the gradebook (ie in %, letter grade, or
     * points format). Returns null if no grade
     * @throws AssessmentNotFoundException
     */
    GradeDefinition getGradeDefinitionForStudentForItem(String siteId,
            Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Get the comment (if any) currently provided for the given combination of student and assignment.
     *
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return null if no comment is avaailable
     * @throws AssessmentNotFoundException
     */
    CommentDefinition getAssignmentScoreComment(String siteId, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return
     * @throws AssessmentNotFoundException
     */
    boolean getIsAssignmentExcused(String siteId, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Provide a student-viewable comment on the score (or lack of score) associated with the given assignment.
     *
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @param comment      a plain text comment, or null to remove any current comment
     * @throws AssessmentNotFoundException
     */
    void setAssignmentScoreComment(String siteId, Long assignmentId, String studentUid, String comment)
            throws AssessmentNotFoundException;

    /**
     * Delete a student-viewable comment on the score (or lack of score) associated with the given assignment.
     *
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @throws AssessmentNotFoundException
     */
    void deleteAssignmentScoreComment(String siteId, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Check to see if an assignment with the given name already exists in the given gradebook. This will give clients a chance to avoid the
     * ConflictingAssignmentNameException.
     *
     * This is not deprecated as we currently need the ability to check for duplciate assignment names in the given gradebook
     *
     */
    boolean isAssignmentDefined(String siteId, String assignmentTitle);

    /**
     * Transfer the gradebook information and assignments from one gradebook to another
     *
     * @param gradebookInformation GradebookInformation to copy
     * @param assignments list of Assignments to copy
     * @param toGradebookUid target gradebook uid
     */
    Map<String,String> transferGradebook(GradebookInformation gradebookInformation, List<Assignment> assignments,
            String toGradebookUid, String fromContext);

    /**
     * @param siteId
     * @return a {@link GradebookInformation} object that contains information about this Gradebook that may be useful to consumers outside
     * the Gradebook tool
     */
    GradebookInformation getGradebookInformation(String siteId);

    /**
     * Removes an assignment from a gradebook. The assignment should not be deleted, but the assignment and all grade records associated
     * with the assignment should be ignored by the application. A removed assignment should not count toward the total number of points in
     * the gradebook.
     *
     * @param assignmentId The assignment id
     */
    void removeAssignment(Long assignmentId) throws StaleObjectModificationException;

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
    List<Category> getCategories(String gradebookId);

    /**
     * Gets the  optional category definition for the supplied id. This is the preferred way to get
     * a category object as it minimises the changes of accidental db updates with the live
     * Category entity.
     */
    Optional<CategoryDefinition> getCategoryDefinition(Long categoryId);

    /**
     * Updates the db category from the supplied definition object
     */
    void updateCategory(CategoryDefinition category);

    /**
     * Get the categories for the given gradebook
     *
     * @param siteId
     * @return {@link CategoryDefinition}s for the categories defined for the given gradebook. Returns an empty list if the gradebook does
     * not have categories.
     */
    List<CategoryDefinition> getCategoryDefinitions(String siteId);

    /**
     * remove category from gradebook
     *
     * @param categoryId
     * @throws StaleObjectModificationException
     */

    void removeCategory(Long categoryId) throws StaleObjectModificationException;

    /**
     * Create a new Gradebook-managed assignment.
     *
     * @param siteId
     * @param assignmentDefinition
     * @return the id of the newly created assignment
     */
    Long addAssignment(String siteId, Assignment assignmentDefinition);

    /**
     * Modify the definition of an existing Gradebook item.
     * <p>
     * Clients should be aware that it's allowed to change the points value of an assignment even if students have already been scored on
     * it. Any existing scores will not be adjusted.
     * <p>
     * This method can be used to manage both internal and external gradebook items, however the title, due date and total points will not
     * be edited for external gradebook items.
     *
     * @param siteId
     * @param assignmentId         the id of the assignment that needs to be changed
     * @param assignmentDefinition the new properties of the assignment
     */
    void updateAssignment(String siteId, Long assignmentId, Assignment assignmentDefinition);

    /**
     * @param siteId
     * @return list of gb items that the current user is authorized to view. If user has gradeAll permission, returns all gb items. If user
     * has gradeSection perm with no grader permissions, returns all gb items. If user has gradeSection with grader perms, returns
     * only the items that the current user is authorized to view or grade. If user does not have grading privileges but does have
     * viewOwnGrades perm, will return all released gb items.
     */
    List<Assignment> getViewableAssignmentsForCurrentUser(String siteId);

    /**
     * @param siteId
     * @return list of gb items that the current user is authorized to view sorted by the provided SortType. If user has gradeAll
     * permission, returns all gb items. If user has gradeSection perm with no grader permissions, returns all gb items. If user has
     * gradeSection with grader perms, returns only the items that the current user is authorized to view or grade. If user does not
     * have grading privileges but does have viewOwnGrades perm, will return all released gb items.
     */
    List<Assignment> getViewableAssignmentsForCurrentUser(String siteId, SortType sortBy);

    /**
     * @param siteId
     * @param assignmentId
     * @return a map of studentId to view/grade function for the given gradebook and gradebook item. students who are not viewable or
     * gradable will not be returned. if the current user does not have grading privileges, an empty map is returned
     */
    Map<String, String> getViewableStudentsForItemForCurrentUser(String siteId, Long assignmentId);

    /**
     * @param userUid
     * @param siteId
     * @param assignmentId
     * @return a map of studentId to view/grade function for the given gradebook and gradebook item that the given userUid is allowed to
     * view or grade. students who are not viewable or gradable will not be returned. if the given user does not have grading
     * privileges, an empty map is returned
     */
    Map<String, String> getViewableStudentsForItemForUser(String userUid, String siteId, Long assignmentId);

    /**
     * Get the Gradebook. Note that this returns Object to avoid circular dependency with sakai-gradebook-tool Consumers will need to cast
     * to {@link org.sakaiproject.tool.gradebook.Gradebook}
     *
     */
    Gradebook getGradebook(String siteId);

    /**
     * Check if there are students that have not submitted
     *
     * @param siteId
     * @return
     */
    boolean checkStudentsNotSubmitted(String siteId);

    /**
     * Check if a gradeable object with the given id exists
     *
     * @param gradableObjectId
     * @return true if a gradable object with the given id exists and was not removed
     */
    boolean isGradableObjectDefined(Long gradableObjectId);

    /**
     * Using the grader permissions, return map of section uuid to section name that includes all sections that the current user may view or
     * grade
     *
     * @param siteId
     * @return
     */
    Map<String, String> getViewableSectionUuidToNameMap(String siteId);

    /**
     * Check if the current user has the gradebook.gradeAll permission
     *
     * @param siteId
     * @return true if current user has the gradebook.gradeAll permission
     */
    boolean currentUserHasGradeAllPerm(String siteId);

    /**
     * Check if the given user is allowed to grade all students in this gradebook
     *
     * @param siteId
     * @param userUid
     * @return true if the given user is allowed to grade all students in this gradebook
     */
    boolean isUserAllowedToGradeAll(String siteId, String userUid);

    /**
     * @param siteId
     * @return true if the current user has some form of grading privileges in the gradebook (grade all, grade section, etc)
     */
    boolean currentUserHasGradingPerm(String siteId);

    /**
     * @param siteId
     * @param userUid
     * @return true if the given user has some form of grading privileges in the gradebook (grade all, grade section, etc)
     */
    boolean isUserAllowedToGrade(String siteId, String userUid);

    /**
     * @param siteId
     * @return true if the current user has the gradebook.editAssignments permission
     */
    boolean currentUserHasEditPerm(String siteId);

    /**
     * @param siteId
     * @return true if the current user has the gradebook.viewOwnGrades permission
     */
    boolean currentUserHasViewOwnGradesPerm(String siteId);

    /**
     * @param siteId
     * @return true if the current user has the gradebook.viewStudentNumbers permission
     */
    boolean currentUserHasViewStudentNumbersPerm(String siteId);

    /**
     * Get the grade records for the given list of students and the given assignment. This can only be called by an instructor or TA that
     * has access, not student.
     * <p>
     * See {@link #getGradeDefinitionForStudentForItem} for the method call that can be made as a student.
     *
     * @param siteId
     * @param assignmentId
     * @param studentIds
     * @return a list of GradeDefinition with the grade information for the given students for the given gradableObjectId
     * @throws SecurityException if the current user is not authorized to view or grade a student in the passed list
     */
    List<GradeDefinition> getGradesForStudentsForItem(String siteId, Long assignmentId, List<String> studentIds);

    /**
     * This method gets grades for multiple gradebook items with emphasis on performance. This is particularly useful for reporting tools
     *
     * @param gradableObjectIds
     * @param siteId
     * @param studentIds
     * @return a Map of GradableObjectIds to a List of GradeDefinitions containing the grade information for the given students for the
     * given gradableObjectIds. Comments are excluded which can be useful for performance. If a student does not have a grade on a
     * gradableObject, the GradeDefinition will be omitted
     * @throws SecurityException        if the current user is not authorized with gradeAll in this gradebook
     * @throws IllegalArgumentException if gradableObjectIds is null/empty, or if gradableObjectIds contains items that are not members of
     *                                  the gradebook with uid = gradebookUid
     */
    Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(String siteId, List<Long> gradableOjbectIds,
                                                                                 List<String> studentIds);

    /**
     * @param siteId
     * @param grade
     * @return true if the given grade is a valid grade given the gradebook's grade entry type. ie, if gradebook is set to grade entry by
     * points, will check for valid point value. if entry by letter, will check for valid letter, etc
     */
    boolean isGradeValid(String siteId, String grade);

    /**
     * Determines if the given string contains a valid numeric grade.
     * @param grade the grade as a string, expected to contain a numeric value
     * @return true if the string contains a valid numeric grade
     */
    boolean isValidNumericGrade(String grade);

    /**
     * @param siteId
     * @param studentIdToGradeMap - the student's username mapped to their grade that you want to validate
     * @return a list of the studentIds that were associated with invalid grades given the gradebook's grade entry type. useful if
     * validating a list of student/grade pairs for a single gradebook (more efficient than calling gradeIsValid repeatedly).
     * returns empty list if all grades are valid
     */
    List<String> identifyStudentsWithInvalidGrades(String siteId, Map<String, String> studentIdToGradeMap);

    /**
     * Save a student score and comment for a gradebook item. The input score must be valid according to the given gradebook's grade entry
     * type.
     *
     * @param siteId
     * @param assignmentId
     * @param studentId
     * @param grade        - must be in format according to gradebook's grade entry type
     * @param comment
     * @throws InvalidGradeException       - if grade is invalid. grade and comment will not be saved
     * @throws AssessmentNotFoundException
     * @throws SecurityException           if current user is not authorized to grade student
     */
    void saveGradeAndCommentForStudent(String siteId, Long assignmentId, String studentId, String grade, String comment)
            throws InvalidGradeException, AssessmentNotFoundException;

    /**
     * Given a list of GradeDefinitions for students for a given gradebook and gradable object, will save the associated scores and
     * comments. Scores must be in a format according to the gradebook's grade entry type (ie points, %, letter).
     *
     * @param siteId
     * @param assignmentId
     * @param gradeDefList
     * @throws InvalidGradeException       if any of the grades are not valid - none will be saved
     * @throws SecurityException           if the user does not have access to a student in the list - no grades or comments will be saved for any
     *                                     student
     * @throws AssessmentNotFoundException
     */
    void saveGradesAndComments(String siteId, Long assignmentId, List<GradeDefinition> gradeDefList)
            throws InvalidGradeException, AssessmentNotFoundException;

    void saveGradeAndExcuseForStudent(String siteId, Long assignmentId, String studentId, String grade, boolean excuse)
        throws InvalidGradeException, AssessmentNotFoundException;

    /**
     * @param siteId
     * @return the constant representation of the grade entry type (ie points, %, letter grade)
     */
    Integer getGradeEntryType(String siteId);

    /**
     * Get a Map of overridden CourseGrade for students.
     *
     * @param siteId
     * @return Map of enrollment displayId as key, point as value string
     */
    Map<String, String> getEnteredCourseGrade(String siteId);

    /**
     * Get student's assignment's score as string.
     *
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return String of score
     */
    String getAssignmentScoreString(String siteId, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Get student's assignment's score as string. This is provided for backward compatibility only.
     *
     * @param siteId
     * @param assignmentName
     * @param studentUid
     * @return String of score
     * @deprecated See {@link #getAssignmentScoreString(String, Long, String)}
     */
    @Deprecated
    String getAssignmentScoreString(String siteId, String assignmentName, String studentUid)
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
    String getAssignmentScoreStringByNameOrId(String gradebookUid, String assignmentName, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Set student's score for assignment.
     *
     * @param siteId                   The uid of the gradebook to use (it's the site id)
     * @param assignmentId             The id of the grading item that we want to grade for this student
     * @param studentUid               The particular student we're grading
     * @param score                    the String score for the student
     * @param clientServiceDescription The client service or tool setting the grade (eg: assignments)
     * @param externalId               An optional param idenifiying the tool id that "manages" this item. Can be null.
     */
    void setAssignmentScoreString(String siteId, Long assignmentId, String studentUid, String score, String clientServiceDescription, String externalId)
            throws AssessmentNotFoundException;

    /**
     * Set student's score for assignment.
     *
     * @param siteId                   The uid of the gradebook to use (it's the site id)
     * @param assignmentId             The id of the grading item that we want to grade for this student
     * @param studentUid               The particular student we're grading
     * @param score                    the String score for the student
     * @param clientServiceDescription The client service or tool setting the grade (eg: assignments)
     */
    void setAssignmentScoreString(String siteId, Long assignmentId, String studentUid, String score, String clientServiceDescription)
            throws AssessmentNotFoundException;

    /**
     * Set student's score for assignment. This is provided for backward compatibility only.
     *
     * @param siteId
     * @param assignmentName
     * @param studentUid
     * @param score
     * @param clientServiceDescription
     * @deprecated See {@link #setAssignmentScoreString(String, Long, String, String, String)}
     */
    @Deprecated
    void setAssignmentScoreString(String siteId, String assignmentName, String studentUid, String score,
                                  String clientServiceDescription)
            throws AssessmentNotFoundException;


    /**
     * Finalize the gradebook's course grades by setting all still-unscored assignments to zero scores.
     *
     * @param gradebookUid
     */
    void finalizeGrades(String gradebookUid);

    /**
     * @param siteId
     * @param assignmentId
     * @return the lowest possible grade allowed for the given assignmentId. For example, in a points or %-based gradebook, the lowest
     * possible grade for a gradebook item is 0. In a letter-grade gb, it may be 'F' depending on the letter grade mapping. Ungraded
     * items have a lowest value of null.
     * @throws SecurityException           if user does not have permission to view assignments in the given gradebook
     * @throws AssessmentNotFoundException if there is no gradebook item with the given gradebookItemId
     */
    String getLowestPossibleGradeForGbItem(String siteId, Long assignmentId);

    /**
     * @param assignment     (non-null) the Assignment object representing the gradebook item for which you are setting the points possible (aka
     *                       relative weight). May be a new gradebook item without an id yet.
     * @param pointsPossible the points possible/relative weight you would like to validate for the gradebookItem above.
     * @return {@link PointsPossibleValidation} value indicating the validity of the given points possible/relative weight or a problem code
     * defining why it is invalid
     */
    PointsPossibleValidation isPointsPossibleValid(Assignment assignment, Double pointsPossible);

    /**
     * Computes the Average Course Grade as a letter.
     *
     * @param siteId
     * @return
     */
    String getAverageCourseGrade(String siteId);

    Long getCourseGradeId(final String gradebookId);

    /**
     * Update the ordering of an assignment. This can be performed on internal and external assignments.
     *
     * @param siteId       uid of the gradebook
     * @param assignmentId id of the assignment in the gradebook
     * @param order        the new order for this assignment. Note it is 0 based index ordering.
     * @return
     */
    void updateAssignmentOrder(String siteId, Long assignmentId, Integer order);

    /**
     * Gets the grading events for the given student and the given assignment
     *
     * @param studentId
     * @param assignmentId
     * @return List of GradingEvent objects.
     */
    List<GradingEvent> getGradingEvents(String studentId, long assignmentId);

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
    Optional<CategoryScoreData> calculateCategoryScore(String gradebookId, String studentUuid, Long categoryId, boolean includeNonReleasedItems, Integer categoryType, Boolean equalWeightAssignments);

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
     * @param siteId
     * @param userUuid uuid of the user
     * @return The {@link CourseGradeTransferBean} for the student
     */
    CourseGradeTransferBean getCourseGradeForStudent(String siteId, String userUuid);

    /**
     * Get the course grade for a list of students
     *
     * @param siteId
     * @param userUuids uuids of the users
     * @return a Map of {@link CourseGradeTransferBean} for the students. Key is the student uuid.
     */
    Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String siteId, List<String> userUuids);

    /**
     * Get the course grade for a list of students using the given grading schema
     *
     * @param siteId
     * @param userUuids uuids of the users
     * @param schema    the grading schema (bottom percents) to use in the calculation
     * @return a Map of {@link CourseGrade} for the students. Key is the student uuid.
     */
    Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String siteId, List<String> userUuids, Map<String, Double> schema);

    /**
     * Get a list of CourseSections that the current user has access to in the given gradebook. This is a combination of sections and groups
     * and is permission filtered.
     *
     * @param siteId
     * @return list of CourseSection objects.
     */
    List<CourseSection> getViewableSections(String siteId);

    /**
     * Update the settings for this gradebook
     *
     * @param siteId
     * @param gbInfo GradebookInformation object
     */
    void updateGradebookSettings(String siteId, GradebookInformation gbInfo);

    /**
     * Return the GradeMappings for the given gradebook. The normal getGradebook(siteId) doesn't return the GradeMapping.
     *
     * @param gradebookId
     * @return Set of GradeMappings for the gradebook
     */
    Set<GradeMapping> getGradebookGradeMappings(String gradebookId);

    /**
     * Allows an instructor to set a course grade override for the given student
     *
     * @param siteId      uuid of the gradebook
     * @param studentUuid uuid of the student
     * @param grade       the new course grade
     */
    void updateCourseGradeForStudent(String siteId, String studentUuid, String grade, String gradeScale);

    /**
     * Updates the categorized order of an assignment
     *
     * @param siteId       uuid of the gradebook
     * @param categoryId   id of the category
     * @param assignmentId id of the assignment
     * @param order        new position of the assignment
     */
    void updateAssignmentCategorizedOrder(String siteId, Long categoryId, Long assignmentId, Integer order);

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
    @Deprecated
    void addExternalAssessment(String siteId, String externalId, String externalUrl,
                               String title, double points, Date dueDate, String externalServiceDescription, String externalData)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException;

    /**
     * Add an externally-managed assessment to a gradebook to be treated as a
     * read-only assignment. The gradebook application will not modify the
     * assessment properties or create any scores for the assessment.
     * Since each assignment in a given gradebook must have a unique name,
     * conflicts are possible.
     *
     * @param siteId
     * @param externalId                 some unique identifier which Samigo uses for the assessment.
     *                                   The externalId is globally namespaced within the gradebook, so
     *                                   if other apps decide to put assessments into the gradebook,
     *                                   they should prefix their externalIds with a well known (and
     *                                   unique within sakai) string.
     * @param externalUrl                a link to go to if the instructor or student wants to look at the assessment
     *                                   in Samigo; if null, no direct link will be provided in the
     *                                   gradebook, and the user will have to navigate to the assessment
     *                                   within the other application
     * @param title
     * @param points                     this is the total amount of points available and must be greater than zero.
     *                                   It could be null if it's an ungraded item.
     * @param dueDate
     * @param externalServiceDescription
     * @param externalData               if there is some data that the external service wishes to store.
     * @param ungraded
     */
    void addExternalAssessment(String siteId, String externalId, String externalUrl, String title, Double points,
                               Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException;

    /**
     * This method is identical to {@link #addExternalAssessment(String, String, String, String, Double, Date, String, String, Boolean)} but
     * allows you to also specify the associated Category for this assignment. If the gradebook is set up for categories and
     * categoryId is null, assignment category will be unassigned
     *
     * @param siteId
     * @param externalId
     * @param externalUrl
     * @param title
     * @param points
     * @param dueDate
     * @param externalServiceDescription
     * @param externalData               if there is some data that the external service wishes to store.
     * @param ungraded
     * @param categoryId
     * @throws ConflictingAssignmentNameException
     * @throws ConflictingExternalIdException
     * @throws AssignmentHasIllegalPointsException
     * @throws InvalidCategoryException
     */
    void addExternalAssessment(String siteId, String externalId, String externalUrl, String title, Double points,
                               Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded, Long categoryId)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException, InvalidCategoryException;

    /**
     * This method is identical to {@link #addExternalAssessment(String, String, String, String, Double, Date, String, String, Boolean, Long)} but
     * allows you to also specify the reference for the thing being graded via the gradableReference.
     *
     * @param siteId
     * @param externalId
     * @param externalUrl
     * @param title
     * @param points
     * @param dueDate
     * @param externalServiceDescription
     * @param externalData               if there is some data that the external service wishes to store.
     * @param ungraded
     * @param categoryId
     * @param gradableReference
     * @throws ConflictingAssignmentNameException
     * @throws ConflictingExternalIdException
     * @throws AssignmentHasIllegalPointsException
     * @throws InvalidCategoryException
     */
    void addExternalAssessment(String siteId, String externalId, String externalUrl, String title, Double points,
                                      Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded, Long categoryId, String gradableReference)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException, InvalidCategoryException;



        /**
         * @deprecated Replaced by
         *      {@link updateExternalAssessment(String, String, String, String, Double, Date, Boolean)}
         */
    @Deprecated
    void updateExternalAssessment(String siteId, String externalId, String externalUrl, String externalData,
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
    void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String externalData,
                                         String title, Double points, Date dueDate, Boolean ungraded)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;

    void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String externalData, String title, Long categoryId, Double points, Date dueDate, Boolean ungraded)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;

    /**
     * Remove the assessment reference from the gradebook. Although Samigo
     * doesn't currently delete assessments, an instructor can retract an
     * assessment to keep it from students. Since such an assessment would
     * presumably no longer be used to calculate final grades, Samigo should
     * also remove that assessment from the gradebook.
     *
     * @param siteId
     * @param externalId the UID of the assessment
     */
    void removeExternalAssignment(String siteId, String externalId)
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
  void updateExternalAssessmentScore(String gradebookUid, String externalId,
            String studentUid, String points)
            throws AssessmentNotFoundException;

    /**
     * @param siteId
     * @param externalId
     * @param studentUidsToScores
     * @throws AssessmentNotFoundException
     * @deprecated Replaced by
     * {@link updateExternalAssessmentScoresString(String, String, Map<String, String)}
     */
    @Deprecated
    void updateExternalAssessmentScores(String siteId,
        String externalId, Map<String, Double> studentUidsToScores)
        throws AssessmentNotFoundException;

    /**
     * Updates a set of external scores for an external assignment in the gradebook.
     *
     * @param siteId              The site id
     * @param externalId          The external ID of the assignment/assessment
     * @param studentUidsToScores A map whose String keys are the unique ID strings of the students and whose
     *                            String values are points earned on this assessment or null if the score
     *                            should be removed.
     */
    void updateExternalAssessmentScoresString(String siteId,
            String externalId, Map<String, String> studentUidsToScores)
    throws AssessmentNotFoundException;

    /**
     * Updates an external comment for an external assignment in the gradebook.
     *
     * @param siteId     The site id
     * @param externalId The external ID of the assignment/assessment
     * @param studentUid The unique id of the student
     * @param comment    The comment to be added to this grade, or null if a comment
     *                   should be removed
     */
    void updateExternalAssessmentComment(String siteId,
            String externalId, String studentUid, String comment )
                    throws AssessmentNotFoundException;
    /**
     * Updates a set of external comments for an external assignment in the gradebook.
     *
     * @param studentUidsToScores A map whose String keys are the unique ID strings of the students and whose
     *                            String values are comments or null if the comments
     *                            should be removed.
     * @param siteId              The Uid of the gradebook
     * @param externalId          The external ID of the assignment/assessment
     */
    void updateExternalAssessmentComments(String siteId,
            String externalId, Map<String, String> studentUidsToComments)
                    throws AssessmentNotFoundException;

    /**
     * Check to see if an assignment with the given external id already exists
     * in the given gradebook. This will give external assessment systems
     * a chance to avoid the ConflictingExternalIdException.
     *
     * @param siteId     The site id
     * @param externalId The external assessment's external identifier
     */
    boolean isExternalAssignmentDefined(String siteId, String externalId);

    /**
     * Check with the appropriate external service if a specific assignment is
     * available only to groups.
     *
     * @param gradebookId The gradebooks unique id
     * @param externalId  The external assessment's external identifier
     */
    boolean isExternalAssignmentGrouped(String gradebookId, String externalId);

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
    boolean isExternalAssignmentVisible(String gradebookUid, String externalId, String userId);

    /**
     * Retrieve all assignments for a gradebook that are marked as externally
     * maintained and are visible to the current user. Assignments may be included
     * with a null providerAppKey, indicating that the gradebook references the
     * assignment, but no provider claims responsibility for it.
     *
     * @param siteId The site id
     * @return A map from the externalId of each activity to the providerAppKey
     */
    Map<String, String> getExternalAssignmentsForCurrentUser(String siteId);

    /**
     * Retrieve a list of all visible, external assignments for a set of users.
     *
     * @param siteId     The gradebook's unique identifier
     * @param studentIds The collection of student IDs for which to retrieve assignments
     * @return A map from the student ID to all visible, external activity IDs
     */
    Map<String, List<String>> getVisibleExternalAssignments(String siteId, Collection<String> studentIds);

    /**
     * Register a new ExternalAssignmentProvider for handling the integration of external
     * assessment sources with the sakai gradebook
     * Registering more than once will overwrite the current with the new one
     *
     * @param provider the provider implementation object
     */
    void registerExternalAssignmentProvider(ExternalAssignmentProvider provider);

    /**
     * Remove/unregister any ExternalAssignmentProvider which is currently registered,
     * does nothing if they provider does not exist
     *
     * @param providerAppKey the unique app key for a provider
     */
    void unregisterExternalAssignmentProvider(String providerAppKey);

    /**
     * Break the connection between an external assessment engine and an assessment which
     * it created, giving it up to the Gradebook application to control from now on.
     *
     * @param siteId
     * @param externalId
     */
    void setExternalAssessmentToGradebookAssignment(String siteId, String externalId);

    /**
     * Get the category of a gradebook with the externalId given
     *
     * @param siteId
     * @param externalId
     * @return
     */
    Long getExternalAssessmentCategoryId(String siteId, String externalId);

    /**
     * Checks to see whether a gradebook has the categories option enabled.
     *
     * @param siteId The gradebook UID to check
     * @return Whether the gradebook has categories enabled
     */
    boolean isCategoriesEnabled(String siteId);

    GradebookManager addGradebookManager(Site site);

    /**
     * Deletes the gradebook with the given UID, along with all its associated
     * data.
     */
    void deleteGradebook(String gradebookId);

    /**
     * @param gradingScaleDefinitions
     *  A collection of GradingScaleDefinition beans.
     */
    void setAvailableGradingScales(Collection<GradingScaleDefinition> gradingScaleDefinitions);

    /**
     * @param uid
     *  The UID of the grading scale to use as the default for new gradebooks.
     */
    void setDefaultGradingScale(String uid);

    /**
     *  Get all of the available Grading Scales in the system.
     *  @return List of GradingScale
     */
    List<GradingScale> getAvailableGradingScales();

    /**
     *  Get all of the available Grading Scales in the system, as shared DTOs.
     *  @return List of GradingScaleDefinition
     */
    List<GradingScaleDefinition> getAvailableGradingScaleDefinitions();

    /**
     * Adds a new grade scale to an existing gradebook.
     *
     * @param scaleUuid The uuid of the scale we want to be added to the gradebook
     * @param siteId    The gradebook with GradeMappings where we will add the grading scale.
     */
    void saveGradeMappingToGradebook(String scaleUuid, String siteId);

    /**
     * Update a grademapping with new values.
     *
     * @param gradeMappingId id of GradeMapping to update
     * @param gradeMap the updated map of grades
     *
     */
    void updateGradeMapping(Long gradeMappingId, Map<String, Double> gradeMap);

    String getUrlForAssignment(Assignment assignment);
    
    /**
     * Set the gradebook mode for a site (single gradebook per site or per group)
     *
     * @param siteId The site ID
     * @param mode The mode to set (SITE or GROUP)
     * @return True if successful, false otherwise
     */
    boolean setGradebookMode(String siteId, GradebookManager.Access mode);
    
    /**
     * Get the current gradebook manager for a site
     *
     * @param siteId The site ID
     * @return The GradebookManager for the site
     */
    GradebookManager getGradebookManager(String siteId) throws IdUnusedException;
    
    /**
     * Get all gradebooks associated with a site
     *
     * @param siteId The site ID
     * @return List of all gradebooks associated with the site
     */
    List<Gradebook> getGradebooksForSite(String siteId);
    
    /**
     * Associate a group with a gradebook
     *
     * @param siteId  The site ID
     * @param groupId The group ID
     * @return True if successful, false otherwise
     */
    boolean mapGroupToGradebook(String siteId, String groupId);
}
