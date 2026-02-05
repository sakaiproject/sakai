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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.grading.api.model.Category;
import org.sakaiproject.grading.api.model.CourseGrade;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.grading.api.model.GradingEvent;
import org.sakaiproject.grading.api.model.GradingScale;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

import org.hibernate.HibernateException;
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

    public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_DOWN);

    /**
     * Array of chars that are not allowed at the beginning of a gb item title
     */
    public static final String[] INVALID_CHARS_AT_START_OF_GB_ITEM_NAME = { "#", "*", "[" };

    /**
     * Check to see if the current user is allowed to view the list of gradebook assignments.
     *
     * @param gradebookUid
     */
    public boolean isUserAbleToViewAssignments(String gradebookUid);

    /**
     * Check to see if current user may grade or view the given student for the given item in the given gradebook. Returns string
     * representation of function per GradingService vars (view/grade) or null if no permission
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return GradingService.gradePermission, GradingService.viewPermission, or null if no permission
     */
    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String siteId, Long assignmentId, String studentUid);

    /**
     * @return Returns a list of Assignment objects describing the assignments that are currently defined in the given gradebook, sorted by
     *         the given sort type.
     */
    public List<Assignment> getAssignments(String gradebookUid, String siteId, SortType sortBy);

    /**
     * Get an assignment based on its id
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @return the associated Assignment with the given assignmentId
     * @throws AssessmentNotFoundException
     */
    public Assignment getAssignment(String gradebookUid, String siteId, Long assignmentId) throws AssessmentNotFoundException;

    /**
     * Get an assignment based on its name. This is provided for backward compatibility only.
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentName
     * @return the associated Assignment with the given name
     * @throws AssessmentNotFoundException
     *
     * @deprecated Use {@link #getAssignment(String,Long)} instead.
     */
    @Deprecated
    public Assignment getAssignment(String gradebookUid, String siteId, String assignmentName)
            throws AssessmentNotFoundException;

    public Assignment getExternalAssignment(String gradebookUid, String externalId);
    public List<String> getGradebookUidByExternalId(String externalId);

    /**
     * Get an assignment based on its name or id. This is intended as a migration path from the deprecated
     * {@link #getAssignment(String,String)} to the new {@link #getAssignment(String,Long)}
     *
     * This method will attempt to lookup the name as provided then fall back to the ID as a Long (If it is a Long) You should use
     * {@link #getAssignment(String,Long)} if you always can use the Long instead.
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentName
     * @return the associated Assignment with the given name
     * @throws AssessmentNotFoundException
     *
     */
    public Assignment getAssignmentByNameOrId(String gradebookUid, String siteId, String assignmentName)
            throws AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return Returns a GradeDefinition for the student, respecting the grade entry type for the gradebook (ie in %, letter grade, or
     *         points format). Returns null if no grade
     * @throws AssessmentNotFoundException
     */
    public GradeDefinition getGradeDefinitionForStudentForItem(String gradebookUid, String siteId,
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
     * @param comment a plain text comment, or null to remove any current comment
     * @throws AssessmentNotFoundException
     */
    public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, String comment)
            throws AssessmentNotFoundException;

    /**
     * Delete a student-viewable comment on the score (or lack of score) associated with the given assignment.
     *
     * @param gradebookUid
     * @param assignmentId
     * @param studentUid
     * @throws AssessmentNotFoundException
     */
    public void deleteAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Check to see if an assignment with the given name already exists in the given gradebook. This will give clients a chance to avoid the
     * ConflictingAssignmentNameException.
     *
     * This is not deprecated as we currently need the ability to check for duplciate assignment names in the given gradebook
     *
     */
    public boolean isAssignmentDefined(String gradebookUid, String siteId, String assignmentTitle);

    /**
     * Transfer the gradebook information and assignments from one gradebook to another
     *
     * @param gradebookInformation GradebookInformation to copy
     * @param assignments list of Assignments to copy
     * @param toGradebookUid target gradebook uid
     * @param fromContext source context identifier (site id)
     * @param options list of options to control the transfer behavior (e.g., COPY_SETTINGS_OPTION)
     */
    public Map<String,String> transferGradebook(GradebookInformation gradebookInformation, List<Assignment> assignments,
            String toGradebookUid, String fromContext, List<String> options);

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @return a {@link GradebookInformation} object that contains information about this Gradebook that may be useful to consumers outside
     *         the Gradebook tool
     *
     */
    public GradebookInformation getGradebookInformation(String gradebookUid, String siteId);

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
     * Gets the  optional category definition for the supplied id. This is the preferred way to get
     * a category object as it minimises the changes of accidental db updates with the live
     * Category entity.
     */
    public Optional<CategoryDefinition> getCategoryDefinition(Long categoryId, String siteId);

    /**
     * Updates the db category from the supplied definition object
     */
    public void updateCategory(CategoryDefinition category);

    /**
     * Get the categories for the given gradebook
     *
     * @param gradebookUid
     * @param siteId
     * @return {@link CategoryDefinition}s for the categories defined for the given gradebook. Returns an empty list if the gradebook does
     *         not have categories.
     */
    public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid, String siteId);

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
     * @param gradebookUid
     * @param siteId
     * @param assignmentDefinition
     * @return the id of the newly created assignment
     */
    public Long addAssignment(String gradebookUid, String siteId, Assignment assignmentDefinition);

    /**
     * Modify the definition of an existing Gradebook item.
     *
     * Clients should be aware that it's allowed to change the points value of an assignment even if students have already been scored on
     * it. Any existing scores will not be adjusted.
     *
     * This method can be used to manage both internal and external gradebook items, however the title, due date and total points will not
     * be edited for external gradebook items.
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentId the id of the assignment that needs to be changed
     * @param assignmentDefinition the new properties of the assignment
     */
    public void updateAssignment(String gradebookUid, String siteId, Long assignmentId, Assignment assignmentDefinition);

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @return list of gb items that the current user is authorized to view sorted by the provided SortType. If user has gradeAll
     *         permission, returns all gb items. If user has gradeSection perm with no grader permissions, returns all gb items. If user has
     *         gradeSection with grader perms, returns only the items that the current user is authorized to view or grade. If user does not
     *         have grading privileges but does have viewOwnGrades perm, will return all released gb items.
     */
    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid, String siteId, SortType sortBy);

    /**
     * @param userUid
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @return a map of studentId to view/grade function for the given gradebook and gradebook item that the given userUid is allowed to
     *         view or grade. students who are not viewable or gradable will not be returned. if the given user does not have grading
     *         privileges, an empty map is returned
     */
    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, String siteId, Long assignmentId);

    /**
     * Retrieves a gradebook by its unique identifier.
     *
     * <p>This method looks up an existing gradebook using the provided UID. The UID can represent
     * either a site identifier or a group identifier within a site.</p>
     *
     * @param uid the unique identifier of the gradebook to retrieve; must not be null or blank
     * @return the {@link Gradebook} associated with the given UID, or {@code null} if no
     *         gradebook exists with that identifier
     *
     * @see #getGradebook(String, String)
     * @since 1.0
     */
    Gradebook getGradebook(String uid);

    /**
     * Retrieves a gradebook by its unique identifier, creating a new one if it doesn't exist.
     *
     * <p>This method first attempts to find an existing gradebook with the given UID. If no gradebook
     * is found, it creates a new one with an appropriate name based on whether the UID represents
     * a site or a group within a site.</p>
     *
     * <p>For group gradebooks (when uid != siteId), the gradebook name is constructed by combining
     * a localized "group.gradebook" prefix with the group's title.</p>
     *
     * @param uid the unique identifier of the gradebook to retrieve or create
     * @param siteId the site identifier used for context when creating new gradebooks
     * @return the existing or newly created {@link Gradebook}, or {@code null} if the gradebook
     *         cannot be found and creation fails
     *
     * @see #addGradebook(String, String)
     */
    public Gradebook getGradebook(String uid, String siteId);

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
     * @param siteId
     * @param assignmentId
     * @param studentIds
     * @return a list of GradeDefinition with the grade information for the given students for the given gradableObjectId
     * @throws SecurityException if the current user is not authorized to view or grade a student in the passed list
     */
    public List<GradeDefinition> getGradesForStudentsForItem(String gradebookUid, String siteId, Long assignmentId, List<String> studentIds);

    /**
     * This method gets grades for multiple gradebook items with emphasis on performance. This is particularly useful for reporting tools
     *
     * @param gradebookUid
     * @param siteId
     * @param gradableObjectIds
     * @param studentIds
     * @return a Map of GradableObjectIds to a List of GradeDefinitions containing the grade information for the given students for the
     *         given gradableObjectIds. Comments are excluded which can be useful for performance. If a student does not have a grade on a
     *         gradableObject, the GradeDefinition will be omitted
     * @throws SecurityException if the current user is not authorized with gradeAll in this gradebook
     * @throws IllegalArgumentException if gradableObjectIds is null/empty, or if gradableObjectIds contains items that are not members of
     *             the gradebook with uid = gradebookUid
     */
    public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(String gradebookUid, String siteId, List<Long> gradableOjbectIds, List<String> studentIds);

    /**
     * This method gets grades for multiple gradebook items including comments with emphasis on performance.
     *
     * @param gradebookUid
     * @param siteId
     * @param gradableObjectIds
     * @param studentIds
     * @return a Map of GradableObjectIds to a List of GradeDefinitions containing the grade information for the given students for the
     *         given gradableObjectIds. Comments are included. If a student does not have a grade on a
     *         gradableObject, the GradeDefinition will be omitted
     * @throws SecurityException if the current user is not authorized with gradeAll in this gradebook
     * @throws IllegalArgumentException if gradableObjectIds is null/empty, or if gradableObjectIds contains items that are not members of
     *             the gradebook with uid = gradebookUid
     */
    public Map<Long, List<GradeDefinition>> getGradesWithCommentsForStudentsForItems(String gradebookUid, String siteId, List<Long> gradableObjectIds, List<String> studentIds);

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
    public Set<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap);

    /**
     * Save a student score and comment for a gradebook item. The input score must be valid according to the given gradebook's grade entry
     * type.
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @param studentId
     * @param grade - must be in format according to gradebook's grade entry type
     * @param comment
     * @throws InvalidGradeException - if grade is invalid. grade and comment will not be saved
     * @throws AssessmentNotFoundException
     * @throws SecurityException if current user is not authorized to grade student
     */
    public void saveGradeAndCommentForStudent(String gradebookUid, String siteId, Long assignmentId, String studentId, String grade, String comment)
            throws InvalidGradeException, AssessmentNotFoundException;

    /**
     * Given a list of GradeDefinitions for students for a given gradebook and gradable object, will save the associated scores and
     * comments. Scores must be in a format according to the gradebook's grade entry type (ie points, %, letter).
     *
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @param gradeDefList
     * @throws InvalidGradeException if any of the grades are not valid - none will be saved
     * @throws SecurityException if the user does not have access to a student in the list - no grades or comments will be saved for any
     *             student
     * @throws AssessmentNotFoundException
     */
    public void saveGradesAndComments(String gradebookUid, String siteId, Long assignmentId, List<GradeDefinition> gradeDefList)
            throws InvalidGradeException, AssessmentNotFoundException;

    public void saveGradeAndExcuseForStudent(String gradebookUid, String siteId, Long assignmentId, String studentId, String grade, boolean excuse)
        throws InvalidGradeException, AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @return the constant representation of the grade entry type (ie points, %, letter grade)
     */
    public GradeType getGradeEntryType(String gradebookUid);

    /**
     * Get student's assignment's score as string.
     * @param gradebookUid
     * @param siteId
     * @param assignmentId
     * @param studentUid
     * @return String of score
     */
    public String getAssignmentScoreString(String gradebookUid, String siteId, Long assignmentId, String studentUid)
            throws AssessmentNotFoundException;

    /**
     * Set student's score for assignment.
     *
     * @param gradebookUid The uid of the gradebook to use
     * @param siteId The site id of the gradebook
     * @param assignmentId The id of the grading item that we want to grade for this student
     * @param studentUid The particular student we're grading
     * @param score the String score for the student
     * @param clientServiceDescription The client service or tool setting the grade (eg: assignments)
     * @param externalId An optional param idenifiying the tool id that "manages" this item. Can be null.
     *
     */
    public void setAssignmentScoreString(String gradebookUid, String siteId, Long assignmentId, String studentUid, String score, String clientServiceDescription, String externalId)
            throws AssessmentNotFoundException;

    /**
     * Computes the Average Course Grade as a letter.
     *
     * @param gradebookUid
     * @param siteId
     * @return
     */
    public String getAverageCourseGrade(String gradebookUid, String siteId);

    public Long getCourseGradeId(final Long gradebookId);

    /**
     * Update the ordering of an assignment. This can be performed on internal and external assignments.
     * @param gradebookUid uid of the gradebook
     * @param siteId
     * @param assignmentId id of the assignment in the gradebook
     * @param order the new order for this assignment. Note it is 0 based index ordering.
     * @return
     */
    public void updateAssignmentOrder(String gradebookUid, String siteId, Long assignmentId, Integer order);

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
     * @param equalWeightAssignments whether category is equal-weighting regardless of points
     * @return percentage and dropped items, or empty if no calculations were made
     *
     */
    Optional<CategoryScoreData> calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId, boolean includeNonReleasedItems, Boolean equalWeightAssignments);

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
    Optional<CategoryScoreData> calculateCategoryScore(Gradebook gradebook, String studentUuid, CategoryDefinition category,
            final List<Assignment> categoryAssignments, Map<Long, String> gradeMap, boolean includeNonReleasedItems);

    /**
     * Calculate category scores for all categories for a student in one efficient operation.
     * This is much more efficient than calling calculateCategoryScore repeatedly for each category.
     *
     * @param gradebookId the gradebook id
     * @param studentUuid the student uuid
     * @param includeNonReleasedItems whether to include non-released items
     * @param categoryType the category type of the gradebook
     * @return map of categoryId to CategoryScoreData for all categories that have calculable scores
     */
    Map<Long, CategoryScoreData> calculateAllCategoryScores(Long gradebookId, String studentUuid,
            boolean includeNonReleasedItems, Integer categoryType);

    /**
     * Calculate category scores for multiple students and all categories in one bulk operation.
     * This is the most efficient method when you need category scores for multiple students.
     *
     * @param gradebookId the gradebook id
     * @param studentUuids list of student uuids
     * @param includeNonReleasedItems whether to include non-released items
     * @param categoryType the category type of the gradebook
     * @return nested map: studentUuid -> categoryId -> CategoryScoreData
     */
    Map<String, Map<Long, CategoryScoreData>> calculateAllCategoryScoresForStudents(Long gradebookId,
            List<String> studentUuids, boolean includeNonReleasedItems, Integer categoryType);

    /**
     * Get the course grade for a student
     *
     * @param gradebookUid
     * @param siteId
     * @param userUuid uuid of the user
     * @return The {@link CourseGradeTransferBean} for the student
     */
    CourseGradeTransferBean getCourseGradeForStudent(String gradebookUid, String siteId, String userUuid);

    /**
     * Get the course grade for a list of students
     *
     * @param gradebookUid
     * @param siteId
     * @param userUuids uuids of the users
     * @return a Map of {@link CourseGradeTransferBean} for the students. Key is the student uuid.
     */
    Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String gradebookUid, String siteId, List<String> userUuids);

    /**
     * Get the course grade for a list of students using the given grading schema
     *
     * @param gradebookUid
     * @param siteId
     * @param userUuids uuids of the users
     * @param schema the grading schema (bottom percents) to use in the calculation
     * @return a Map of {@link CourseGrade} for the students. Key is the student uuid.
     */
    Map<String, CourseGradeTransferBean> getCourseGradeForStudents(String gradebookUid, String siteId, List<String> userUuids, Map<String, Double> schema);

    /**
     * Get a list of CourseSections that the current user has access to in the given gradebook. This is a combination of sections and groups
     * and is permission filtered.
     *
     * @param gradebookUid
     * @param siteId
     * @return list of CourseSection objects.
     */
    List<CourseSection> getViewableSections(String gradebookUid, String siteId);

    /**
     * Update the settings for this gradebook
     *
     * @param gradebookUid
     * @param siteId
     * @param gbInfo GradebookInformation object
     */
    void updateGradebookSettings(String gradebookUid, String siteId, GradebookInformation gbInfo);

    /**
     * Return the GradeMappings for the given gradebook. The normal getGradebook(siteId) doesn't return the GradeMapping.
     *
     * @param gradebookId
     * @return Set of GradeMappings for the gradebook
     */
    Set getGradebookGradeMappings(Long gradebookId);

    /**
     * Allows an instructor to set a course grade override for the given student
     *
     * @param gradebookUid uuid of the gradebook
     * @param siteId
     * @param studentUuid uuid of the student
     * @param grade the new course grade
     */
    void updateCourseGradeForStudent(String gradebookUid, String siteId, String studentUuid, String grade, String gradeScale);

    /**
     * Updates the categorized order of an assignment
     *
     * @param gradebookUid uuid of the gradebook
     * @param siteId
     * @param categoryId id of the category
     * @param assignmentId id of the assignment
     * @param order new position of the assignment
     */
    void updateAssignmentCategorizedOrder(String gradebookUid, String siteId, Long categoryId, Long assignmentId, Integer order);

    /**
     * Return the grade changes made since a given time
     *
     * @param assignmentIds list of assignment ids to check
     * @param since timestamp from which to check for changes
     * @return set of changes made
     */
    List<GradingEvent> getGradingEvents(List<Long> assignmentIds, Date since);

    /**
     * categoryId is null, assignment category will be unassigned
     * @param gradebookUid
     * @param siteId
     * @param externalId
     * @param externalUrl
     * @param title
     * @param points
     * @param dueDate
     * @param externalServiceDescription
     * @param externalData if there is some data that the external service wishes to store.
     * @param ungraded
     * @param categoryId
     * @param gradableReference
     * @throws ConflictingAssignmentNameException
     * @throws ConflictingExternalIdException
     * @throws AssignmentHasIllegalPointsException
     * @throws InvalidCategoryException
     */
    public void addExternalAssessment(String gradebookUid, String siteId, String externalId, String externalUrl, String title, Double points,
                                      Date dueDate, String externalServiceDescription, String externalData, Boolean ungraded, Long categoryId, String gradableReference)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException, InvalidCategoryException;

    /**
     *  Update an external assessment
     * @param gradebookUid
     * @param externalId
     * @param externalUrl
     * @param externalData
     * @param title
     * @param categoryId
     * @param points
     * @param dueDate
     * @param ungraded
     * @throws AssessmentNotFoundException
     * @throws ConflictingAssignmentNameException
     * @throws AssignmentHasIllegalPointsException
     */
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String externalData, String title, Long categoryId, Double points, Date dueDate, Boolean ungraded)
            throws AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;

    /**
     * Remove the assessment reference from the gradebook.
     *
     * @param gradebookUid
     * @param externalId
     * @param externalApp
     */
    public void removeExternalAssignment(String gradebookUid, String externalId, String externalApp)
        throws AssessmentNotFoundException;

  /**
   * Updates an external score for an external assignment in the gradebook.
   *
   * @param gradebookUid
   *    The Uid of the gradebook
   * @param siteId
   * @param externalId
   *    The external ID of the assignment/assessment
   * @param studentUid
   *    The unique id of the student
   * @param points
   *    The number of points earned on this assessment, or null if a score
   *    should be removed
   */
  public void updateExternalAssessmentScore(String gradebookUid, String siteId, String externalId,
            String studentUid, String points)
            throws AssessmentNotFoundException;

    /**
     *
     * @param gradebookUid
     * @param siteId
     * @param externalId
     * @param studentUidsToScores
     * @throws AssessmentNotFoundException
     *
     * @deprecated Replaced by
     *      {@link updateExternalAssessmentScoresString(String, String, Map<String, String)}
     */
    @Deprecated
    public void updateExternalAssessmentScores(String gradebookUid, String siteId,
        String externalId, Map<String, Double> studentUidsToScores)
        throws AssessmentNotFoundException;

    /**
     * Updates a set of external scores for an external assignment in the gradebook.
     *
     * @param gradebookUid
     *  The Uid of the gradebook
     * @param siteId
     * @param externalId
     *  The external ID of the assignment/assessment
     * @param studentUidsToScores
     *  A map whose String keys are the unique ID strings of the students and whose
     *  String values are points earned on this assessment or null if the score
     *  should be removed.
     */
    public void updateExternalAssessmentScoresString(String gradebookUid, String siteId,
            String externalId, Map<String, String> studentUidsToScores)
    throws AssessmentNotFoundException;

    /**
     * Updates an external comment for an external assignment in the gradebook.
     *
     * @param gradebookUid
     *  The Uid of the gradebook
     * @param siteId
     * @param externalId
     *  The external ID of the assignment/assessment
     * @param studentUid
     *  The unique id of the student
     * @param comment
     *  The comment to be added to this grade, or null if a comment
     *  should be removed
     */
    public void updateExternalAssessmentComment(String gradebookUid, String siteId,
            String externalId, String studentUid, String comment )
                    throws AssessmentNotFoundException;
    /**
     * Updates a set of external comments for an external assignment in the gradebook.
     *
     * @param gradebookUid
     *  The Uid of the gradebook
     * @param siteId
     * @param externalId
     *  The external ID of the assignment/assessment
     * @param studentUidsToScores
     *  A map whose String keys are the unique ID strings of the students and whose
     *  String values are comments or null if the comments
     *  should be removed.
     */
    public void updateExternalAssessmentComments(String gradebookUid, String siteId,
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
     * @param name
     */
    public Gradebook addGradebook(String uid, String name);

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

    public boolean isGradebookGroupEnabled(String siteId);
    public List<Gradebook> getGradebookGroupInstances(String siteId);
    public List<String> getGradebookGroupInstancesIds(String siteId);
    public Assignment getAssignmentById(String siteId, Long assignmentId);
    public GradebookAssignment getGradebookAssigment(String siteId, Long assignmentId);
    public String getGradebookUidByAssignmentById(String siteId, Long assignmentId);
    public void buildGradebookPointsMap(String gbUid, String siteId, String assignmentRef, Map<String, Double> gradebookPointsMap, String newCategoryString);
    public boolean checkMultiSelectorList(String siteId, List<String> groupList, List<String> multiSelectorList, boolean isCategory);
    public Map<String, String> buildCategoryGradebookMap(List<String> selectedGradebookUids, String categoriesString, String siteId);
    public Long getMatchingUserGradebookItemId(String siteId, String userId, String gradebookItemIdString);
    public List<String> getGradebookInstancesForUser(String siteId, String userId);
    public void initializeGradebooksForSite(String siteId);
    public Double convertStringToDouble(final String doubleAsString);

    /**
     * Fully remove a gradebook from the database. This removes all the associated data and is
     * irreversible without a backup.
     *
     * @param siteId The siteId (aka gradebook uid) to delete.
     */
    public void hardDeleteGradebook(String siteId);

    /**
     * Get the maximum letter grade for the given grade mapping
     *
     * @param gradeMap
     * @return An optional containing the maximum letter grade
     */
    public Optional<String> getMaxLetterGrade(Map<String, Double> gradeMap);

    /**
     * Get the maximum points for the given grade mapping
     *
     * @param gradeMap
     * @return An optional containing the maximum points
     */
    public Optional<Double> getMaxPoints(Map<String, Double> gradeMap);
}
