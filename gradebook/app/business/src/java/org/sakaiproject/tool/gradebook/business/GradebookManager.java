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

package org.sakaiproject.tool.gradebook.business;

import java.util.*;

import org.hibernate.HibernateException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingSpreadsheetNameException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.MultipleAssignmentSavingException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.*;

/**
 * Manages Gradebook persistence.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface GradebookManager {
    /**
     * Updates a gradebook's representation in persistence.
     *
     * If the gradebook's selected grade mapping has been modified, the sort
     * value on all explicitly graded course grade records must be updated when
     * the gradebook is updated.
     *
     * A gradebook's selected grade mapping may only change (to a different kind
     * of mapping) if there are no explicitly graded course grade records.
     *
     * @param gradebook The gradebook to update
     * @throws StaleObjectModificationException
     */
    public void updateGradebook(Gradebook gradebook) throws StaleObjectModificationException;

    /**
     * Fetches a gradebook based on its surrogate key
     *
     * @param id The ID of the gradebook
     * @return The gradebook
     */
    public Gradebook getGradebook(Long id);

    /**
     * Internal services use a Long ID to identify a gradebook.
     * External facades use a String UID instead. This method
     * translates.
     */
    public String getGradebookUid(Long id);

    /**
     * Fetches a gradebook based on its unique string id
     *
     * @param uid The UID of the gradebook
     * @return The gradebook
     */
    public Gradebook getGradebook(String uid) throws GradebookNotFoundException;

    public Gradebook getGradebookWithGradeMappings(Long id);

    /**
     * Removes an assignment from a gradebook.  The assignment should not be
     * deleted, but the assignment and all grade records associated with the
     * assignment should be ignored by the application.  A removed assignment
     * should not count toward the total number of points in the gradebook.
     *
     * @param assignmentId The assignment id
     */
    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException;
    
    /**
     * Get an assignment grade record by id
     * @param assignmentGradeRecordId
     * @return AssignmentGradeRecord
     */
    public AssignmentGradeRecord getAssignmentGradeRecordById(Long id);
    
    /**
     * Get a comment by id
     * @param commentId
     * @return Comment
     */
    public Comment getCommentById(Long id);
    
    /**
     * Get an assignment grade record by assignment and student
     * @param = assignment
     * @param = studentUid
     * @return AssignmentGradeRecord
     */
    public AssignmentGradeRecord getAssignmentGradeRecordForAssignmentForStudent(GradebookAssignment assignment, String studentUid);
    
    /**
     * Get all assignment score records for the given set of student UIDs.
     * 
     * @param assignment
     * @param studentUids
     * @return AssignmentGradeRecord list
     */
    public List getAssignmentGradeRecords(GradebookAssignment assignment, Collection studentUids);

    /**
     * Get one course grade record (with autocalculated fields) for the given
     * gradebook and the given student UID
     *
     * @param gradebookId
     * @param studentUid
     * @return CourseGradeRecord
     */
    public CourseGradeRecord getPointsEarnedCourseGradeRecords(CourseGrade courseGrade, String studentUid);

    /**
     * Get all course grade records (with autocalculated fields) for the given
     * gradebook and the given set of student UIDs
     *
     * @param gradebookId
     * @param studentUids
     * @return CourseGradeRecord list
     */
    public List getPointsEarnedCourseGradeRecords(CourseGrade courseGrade, Collection studentUids);

    /**
     * As a side-effect, this version of the method calculates the mean course grade.
     * The proliferation of method signatures is meant to cut back as much as possible on
     * redundant reads from the assignment grade records.
     * 
     * @param courseGrade This input argument is modified to include statistical information
     * @param studentUids
     * @return
     */
    public List<CourseGradeRecord> getPointsEarnedCourseGradeRecordsWithStats(CourseGrade courseGrade, Collection studentUids);
    
    public List<CourseGradeRecord> getPointsEarnedCourseGradeRecords(CourseGrade courseGrade, Collection studentUids, Collection assignments, Map scoreMap);
    public void addToGradeRecordMap(Map gradeRecordMap, List gradeRecords);
    
    /**
     * Adds student grade records to map but takes into account grader permissions
     * passed as studentIdItemIdFunctionMap. If not allowed to view/grade item, grade record is
     * set to null
     * @param gradeRecordMap
     * @param gradeRecords
     * @param studentIdItemIdFunctionMap
     * 			Map of studentId to Map of Item to function (grade/view)
     */
    public void addToGradeRecordMap(Map gradeRecordMap, List gradeRecords, Map studentIdItemIdFunctionMap);
    
    public void addToCategoryResultMap(Map categoryResultMap, List categories, Map gradeRecordMap, Map enrollmentMap);
   
    /**
     * Gets all grade records that belong to a collection of enrollments in a
     * gradebook.
     *
     * @param gradebookId
     * @param studentUids
     */
    public List getAllAssignmentGradeRecords(Long gradebookId, Collection studentUids);
    
    /**
     * Gets all grade records that belong to a collection of enrollments in a gradebook and
     * converts the grade returned based upon the grade entry type.
     * @param gradebookId
     * @param studentUids
     * @return
     */
    public List getAllAssignmentGradeRecordsConverted(Long gradebookId, Collection studentUids);

    /**
     * Gets whether there are explicitly entered course grade records in a gradebook.
     *
     * @param gradebookId The gradebook
     * @return Whether there are course grade records that have a non-null enteredGrade field
     */
    public boolean isExplicitlyEnteredCourseGradeRecords(Long gradebookId);

    /**
     * Gets whether scores have been entered for the given assignment.
     * (This may include scores for students who are not currently enrolled.)
     *
     * @param assignmentId The assignment
     * @return How many scores have been entered for the assignment
     */
    public boolean isEnteredAssignmentScores(Long assignmentId);

    /**
     * Updates the grade records in the GradeRecordSet.
     * Implementations of this method should add a new GradingEvent for each
     * grade record modified, and should update the autocalculated value for
     * each graded student's CourseGradeRecord.
     *
     * @return The set of student UIDs who were given scores higher than the
     * assignment's value.
     */
    public Set updateAssignmentGradeRecords(GradebookAssignment assignment, Collection gradeRecords)
    	throws StaleObjectModificationException;

    public Set updateAssignmentGradesAndComments(GradebookAssignment assignment, Collection gradeRecords, Collection comments)
		throws StaleObjectModificationException;
 
    public void updateComments(Collection comments)
		throws StaleObjectModificationException;

    /**
     * Updates the grade records for the keys (student IDs) in the studentsToPoints map.
     * Map values must be valid strings (that exist in the gradebook's grade
     * mapping) or nulls.
     *
     * @param studentsToPoints A Map of student IDs to grades
     */
    public void updateCourseGradeRecords(CourseGrade courseGrade, final Collection gradeRecords)
        throws StaleObjectModificationException;

    /**
     * Gets all grade records for a single student in a single gradebook,
     * not including the course grade.
     *
     * @param gradebookId The gradebook id
     * @param studentId The unique student identifier
     *
     * @return A List of all of this student's grade records in the gradebook
     */
    public List getStudentGradeRecords(Long gradebookId, String studentId);
    
    /**
     * Get all assignment score records for the given student UID.
     * This method will convert the points in DB to percentage values if 
     * gradebook's grading type is GRADE_TYPE_PERCENTAGE 
     * @param gradebookId
     * @param studentId
     * @return
     */
    public List getStudentGradeRecordsConverted(Long gradebookId, String studentId);

    /**
     * Gets the grading events for the enrollments on the given gradable object.
     *
     * @param gradableObject
     * @param enrollments
     * @return
     */
    public GradingEvents getGradingEvents(GradableObject gradableObject, Collection studentUids);
    
    /**
     * Gets the grading events for the given student for the given gradableObjects
     * @param studentId
     * @param gradableObjects
     * @return Map of GradableObject to associated GradingEvent objects
     */
    public Map getGradingEventsForStudent(final String studentId, final Collection gradableObjects);

    /**
     * Fetches a List of Assignments, but does not populate non-persistent
     * fields.
     *
     * @param gradebookId The gradebook ID
     * @param sortBy The field by which to sort the list.
     * @return A list of Assignments with only persistent fields populated
     */
    public List getAssignments(Long gradebookId, String sortBy, boolean ascending);

    /**
     * Convenience method to get assignments with the default sort ordering
     *
     * @param gradebookId The gradebook ID
     */
    public List getAssignments(Long gradebookId);

    /**
     * Fetches a List of Assignments for a given gradebook, and populates the
     * Assignments with all of the statistics fields available in the GradebookAssignment
     * object.
     *
     * @param gradebookId The gradebook ID
     * @param studentUids The current enrollment list to filter dropped students
     *        from the calculation
     * @param sortBy The field by which to sort the list.
     * @return A list of Assignments with their statistics fields populated
     */
    public List getAssignmentsWithStats(Long gradebookId, String sortBy, boolean ascending);

    /**
     * Same as the other getAssignmentsWithStats except for tacking the
     * CourseGrade (with statistics) at the end of the list. This is
     * combined into one call as a way to avoid either exposing the
     * full enrollment list for the site or fetching it twice.
     */
    public List getAssignmentsAndCourseGradeWithStats(Long gradebookId, String sortBy, boolean ascending);

    /**
     * Fetches an assignment
     *
     * @param assignmentId The assignment ID
     * @return The assignment
     */
    public GradebookAssignment getAssignment(Long assignmentId);

    /**
     * Fetches an assignment and populates its non-persistent statistics
     * fields.
     *
     * @param assignmentId The assignment ID
     * @param studentUids The current enrollment list to filter dropped students
     *        from the calculation
     * @return The GradableObject with all statistics fields populated
     */
    public GradebookAssignment getAssignmentWithStats(Long assignmentId);

   /**
     * Add a new assignment to a gradebook
     *
     * @param gradebookId The gradebook ID to which this new assignment belongs
     * @param name The assignment's name (must be unique in the gradebook and not be null)
     * @param points The number of points possible for this assignment (must not be null)
     * @param dueDate The due date for the assignment (optional)
     * @param isNotCounted True if the assignment should not count towards the final course grade (optional)
     * @param isReleased  True if the assignment should be release/ or visble to students
     * @param isExtraCredit True if the assignment is for extra credit
     * @return The ID of the new assignment
     */

    public Long createAssignment(Long gradebookId, String name, Double points, Date dueDate, Boolean isNotCounted, Boolean isReleased, Boolean isExtraCredit)
            throws ConflictingAssignmentNameException, StaleObjectModificationException;


    /**
     * Updates an existing assignment
     */
    public void updateAssignment(GradebookAssignment assignment)
        throws ConflictingAssignmentNameException, StaleObjectModificationException;

    /**
     * Fetches the course grade for a gradebook as found in the database.
     * No non-persistent fields (such as points earned) are filled in.
     *
     * @param gradebookId The gradebook id
     * @return The course grade
     */
    public CourseGrade getCourseGrade(Long gradebookId);

    public double getTotalPoints(Long gradebookId);
    
    abstract double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, List<AssignmentGradeRecord> studentGradeRecs, List<GradebookAssignment> countedAssigns, boolean literalTotal);

    /**
     * Fetches a spreadsheet that has been saved
     *
      * @param spreadsheetId
     * @return  The saved spreadsheet object
     */
    public Spreadsheet getSpreadsheet(Long spreadsheetId);

    /**
     *
     * @param gradebookId
     * @return  a Collection of spreadsheets
     */
    public List getSpreadsheets(Long gradebookId);

    /**
     *
     * @param spreadsheetid
     * @throws StaleObjectModificationException
     */

    public void removeSpreadsheet(Long spreadsheetid) throws StaleObjectModificationException;

    /**
     * create a net spreadsheet
     *
     * @param gradebookId
     * @param name
     * @param creator
     * @param dateCreated
     * @param content
     * @return
     * @throws ConflictingSpreadsheetNameException StaleObjectModificationException
     */
    public Long createSpreadsheet(Long gradebookId, String name, String creator, Date dateCreated, String content) throws ConflictingSpreadsheetNameException, StaleObjectModificationException;

    /**
     *
     * @param assignment
     * @param studentIds
     * @return
     */
    public List getComments(GradebookAssignment assignment, Collection studentIds);

    /**method to get comments for a assignments for a student in a gradebook
     *
     * @param studentId
     * @param gradebookId
     * @return
     */
    public List getStudentAssignmentComments(String studentId, Long gradebookId);
    
    /**method to create a category for a gradebook
    *
    * @param gradebookId
     * @param name
     * @param weight
     * @param dropLowest
     * @param dropHighest
     * @param keepHighest
     * @param pointValue
     * @param relativeWeight
     * @param is_extra_credit
    * @return id of the new category
    * @throws ConflictingAssignmentNameException StaleObjectModificationException
    */
    public Long createCategory(final Long gradebookId, final String name, final Double weight, final Integer dropLowest, final Integer dropHighest, final Integer keepHighest, final Boolean is_extra_credit) 
    throws ConflictingCategoryNameException, StaleObjectModificationException;
    
    /**method to get all categories for a gradebook
    *
    * @param gradebookId
    * @return List of categories
    * @throws HibernateException
    */
    public List getCategories(final Long gradebookId) throws HibernateException;
    
    /**
     * Add a new assignment to a category
     *
     * @param gradebookId The gradebook ID to which this new assignment belongs
     * @param categoryId The category ID to which this new assignment belongs
     * @param name The assignment's name (must be unique in the gradebook and not be null)
     * @param points The number of points possible for this assignment (must not be null)
     * @param dueDate The due date for the assignment (optional)
     * @param isNotCounted True if the assignment should not count towards the final course grade (optional)
     * @param isReleased  True if the assignment should be release/ or visble to students
     * @param isExtraCredit True if the assignment is for extra credit
     * @return The ID of the new assignment
     * @throws ConflictingAssignmentNameException StaleObjectModificationException IllegalArgumentException
     */
    public Long createAssignmentForCategory(Long gradebookId, Long categoryId, String name, Double points, Date dueDate, Boolean isNotCounted, Boolean isReleased, Boolean isExtraCredit)
    throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException;

    /**method to get all assignments for a category
    *
    * @param categoryId
    * @return List of assignments
    * @throws HibernateException
    */
    public List getAssignmentsForCategory(Long categoryId) throws HibernateException;
    
    /**
     * Fetch a category
     *
     * @param categoryId The category ID
     * @return Category
     */
    public Category getCategory(Long categoryId) throws HibernateException;

    /**
     * Updates an existing category
     * 
     * @param category
     * @throws ConflictingCategoryNameException StaleObjectModificationException
     */
    public void updateCategory(Category category)
    throws ConflictingCategoryNameException, StaleObjectModificationException;

    /**
     * remove category from gradebook
     *
     * @param categoryId
     * @throws StaleObjectModificationException
     */
    public void removeCategory(Long categoryId) throws StaleObjectModificationException;
    
    /**
     * Valicates the weightings for the gradebook. All weightings from the gradebook should
     * add up to 100%. (only not-removed categories are counted for gradebook with setting
     * of "Weighted Categories") 
     *
     * @param gradebookId
     * @return boolean value for validation of the weighting value
     */    
    public boolean validateCategoryWeighting(Long gradebookId);
    
    /**
     * Updates the grade records in the GradeRecordSet.
     * This method calls public Set updateAssignmentGradeRecords(GradebookAssignment assignment, Collection gradeRecords) for DB udpates.
     * Method of public Set updateAssignmentGradeRecords(GradebookAssignment assignment, Collection gradeRecords) should not be
     * called outside of impl of GradebookManager anymore later.
     *
     * @param assignment
     * @param Collection gradeRecords
     * @param grade_type
     * @return The set of student UIDs who were given scores higher than the
     * assignment's value.
     */
    public Set updateAssignmentGradeRecords(GradebookAssignment assignment, Collection gradeRecords, int grade_type);

    /**
     * Updates the grade records in the GradeRecordSet for a student.
     * This method calls public Set updateStudentGradeRecords(GradebookAssignment assignment, Collection gradeRecords) for DB udpates.
     * Method of public Set updateStudentGradeRecords(GradebookAssignment assignment, Collection gradeRecords) should not be
     * called outside of impl of GradebookManager anymore later.
     *
     * @param assignment
     * @param Collection gradeRecords
     * @param grade_type
     * @param studentId
     * @return The set of student UIDs who were given scores higher than the
     * assignment's value.
     */
    public Set updateStudentGradeRecords(Collection gradeRecords, int grade_type, String studentId);

    
    /**
     * Get all assignment score records for the given set of student UIDs.
     * This method will convert the points in DB to percentage values if 
     * gradebook's grading type is GRADE_TYPE_PERCENTAGE 
     * 
     * @param assignment
     * @param studentUids
     * @return AssignmentGradeRecord list
     */
    public List getAssignmentGradeRecordsConverted(GradebookAssignment assignment, Collection studentUids);

    /**
     * Get all categories with stats
     *  
     * @param gradebookId
     * @param assignmentSort assignment sorting string
     * @param assignAscending assignment sorting ascending/descending
     * @param categorySort category sorting string
     * @param categoryAscending category sorting ascending/descending
     * @return Category list - the last object is CourseGrade for this gradebook
     */
    public List getCategoriesWithStats(Long gradebookId, String assignmentSort, boolean assignAscending, String categorySort, boolean categoryAscending);
    
    /**
     * Get all categories with stats
     *  
     * @param gradebookId
     * @param assignmentSort assignment sorting string
     * @param assignAscending assignment sorting ascending/descending
     * @param categorySort category sorting string
     * @param categoryAscending category sorting ascending/descending
     * @param includeDroppedScores whether or not to include dropped scores in the calculations
     * @return Category list - the last object is CourseGrade for this gradebook
     */
    public List getCategoriesWithStats(Long gradebookId, String assignmentSort, boolean assignAscending, String categorySort, boolean categoryAscending, boolean includeDroppedScores);
    
    /**
     * Get all categories with stats
     *  
     * @param gradebookId
     * @param assignmentSort assignment sorting string
     * @param assignAscending assignment sorting ascending/descending
     * @param categorySort category sorting string
     * @param categoryAscending category sorting ascending/descending
     * @param includeDroppedScores whether or not to include dropped scores in the calculations
     * @param studentUids list of students you want the statistics for
     * @return Category list - the last object is CourseGrade for this gradebook
     */
    public List getCategoriesWithStats(Long gradebookId, String assignmentSort, boolean assignAscending, String categorySort, boolean categoryAscending, boolean includeDroppedScores, Set studentUids);
    
    /**
     * 
     * @param gradebookId
     * @param assignmentSort
     * @param assignAscending
     * @param categorySort
     * @param categoryAscending
     * @return a list consisting of Assignments, Categories, and Course Grade for
     * the given gradebookId with stats populated. List consists of Assignments, then Categories, then CG
     */
    public List getAssignmentsCategoriesAndCourseGradeWithStats(Long gradebookId, 
            String assignmentSort, boolean assignAscending, String categorySort, boolean categoryAscending);
    
    /**
     * 
     * @param gradebookId
     * @return list of categories with populated assignmentList
     */ 
    public List getCategoriesWithAssignments(Long gradebookId) ;
    
    /**
     * Get all assignments with no categories
     *  
     * @param gradebookId
     * @param assignmentSort assignment sorting string
     * @param assignAscending assignment sorting ascending/descending
     * @return GradebookAssignment list
     */
    public List getAssignmentsWithNoCategory(final Long gradebookId, String assignmentSort, boolean assignAscending);
    
    /**
     * Get all assignments with no categories and with their stats
     *  
     * @param gradebookId
     * @param assignmentSort assignment sorting string
     * @param assignAscending assignment sorting ascending/descending
     * @return GradebookAssignment list
     */
    public List getAssignmentsWithNoCategoryWithStats(Long gradebookId, String assignmentSort, boolean assignAscending);
    
    /**
     * Convert grading events to percentage or letter value depending upon grade_type
     *  
     * @param assign GradebookAssignment
     * @param events GradingEvents
     * @param studentUids List of student ids
     * @param grade_type gradebook's grade_type
     */
    public void convertGradingEventsConverted(GradebookAssignment assign, GradingEvents events, List studentUids, int grade_type);
    
    /**
     * Convert grading events to percentage or letter value depending upon grade_type
     * @param gradebook
     * @param gradableObjectEventListMap map of student's gradableObjects to their associated grading events
     * @param grade_type gradebook's grade_type
     */
    public void convertGradingEventsConvertedForStudent(Gradebook gradebook, Map gradableObjectEventListMap, int grade_type);
    
    /**
     * Check if there's any students that haven't submit their assignment(s) - null value for points or 
     * AssignmentGradeRecord doesn't exist for student(s). 
     *  
     * @param gradebook
     * @return boolean yes - there are students that haven't submit for some assignments.
     */
    public boolean checkStuendsNotSubmitted(Gradebook gradebook);
    
    /**
     * Insert AssignmentGradeRecord with point of 0 for students that don't have a record
     * for counted assignments. Or set point of null to 0 for counted assignments.
     *  
     * @param gradebook
     */
    public void fillInZeroForNullGradeRecords(Gradebook gradebook);
    
    /**
     * Update grade points in DB for assignment when total point is changed by users for grade_type of GRADE_TYPE_PERCENTAGE. 
     *  
     * @param gradebook
     * @param Assignment old assignment with old total point value
     * @param Double newTotal the old total point for assignment
     * @param studentUids List of student uid.
     */
    public void convertGradePointsForUpdatedTotalPoints(Gradebook gradebook, GradebookAssignment assignment, Double newTotal, List studentUids);
    
    /**
     * Get the default letter grading percentage mappings. 
     * This method will return defult mapping if no mapping for the certain gradebook exists.
     *  
     *  @return LetterGradePercentMapping
     */
    public LetterGradePercentMapping getDefaultLetterGradePercentMapping();
    
    /**
     * Create or update the default letter grading percentage mappings.
     *  
     *  @param gradeMap
     */
    public void createOrUpdateDefaultLetterGradePercentMapping(final Map gradeMap);
    
    /**
     * Create the default letter grading percentage mappings.
     *  
     *  @param gradeMap
     */
    public void createDefaultLetterGradePercentMapping(Map<String, Double> gradeMap);

    /**
     * Get letter grading percentage mappings for a gradebook.
     *  
     *  @param gradebook
     *  @return LetterGradePercentMapping
     */
    public LetterGradePercentMapping getLetterGradePercentMapping(final Gradebook gradebook);

    /**
     * Create letter grading percentage mappings for a gradebook.
     * 
     *  @param gradeMap letter grade percentage map
     *  @param gradebook
     */
    public void saveOrUpdateLetterGradePercentMapping(final Map<String, Double> gradeMap, final Gradebook gradebook);
    
    /**
     * Add a new ungraded assignment to a gradebook
     *
     * @param gradebookId The gradebook ID to which this new assignment belongs
     * @param name The assignment's name (must be unique in the gradebook and not be null)
     * @param dueDate The due date for the assignment (optional)
     * @param isNotCounted True if the assignment should not count towards the final course grade (optional)
     * @param isReleased  True if the assignment should be release/ or visble to students
     * @return The ID of the new assignment
     */
    public Long createUngradedAssignment(Long gradebookId, String name, Date dueDate, Boolean isNotCounted, Boolean isReleased)
    	throws ConflictingAssignmentNameException, StaleObjectModificationException;

    /**
     * Add a new ungraded assignment to a category
     *
     * @param gradebookId The gradebook ID to which this new assignment belongs
     * @param categoryId The category ID to which this new assignment belongs
     * @param name The assignment's name (must be unique in the gradebook and not be null)
     * @param dueDate The due date for the assignment (optional)
     * @param isNotCounted True if the assignment should not count towards the final course grade (optional)
     * @param isReleased  True if the assignment should be release/ or visble to students
     * @return The ID of the new assignment
     * @throws ConflictingAssignmentNameException StaleObjectModificationException IllegalArgumentException
     */
    public Long createUngradedAssignmentForCategory(Long gradebookId, Long categoryId, String name, Date dueDate, Boolean isNotCounted, Boolean isReleased)
    	throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException;
    
    /**
     * Add a permission combination for a user.
     *
     * @param gradebookId The gradebook ID
     * @param userId grader's user_id
     * @param function function that the grader have - grade / view
     * @param categoryId The category ID
     * @param groupId group/section ID
     * @return ID of permission
     * @throws IllegalArgumentException
     *    
     */
    public Long addPermission(Long gradebookId, String userId, String function, Long categoryId, String groupId)
    throws IllegalArgumentException;

    /**
     * Get all permissions for gradebook.
     *
     * @param gradebookId The gradebook ID
     * @return List of permissions
     * @throws IllegalArgumentException
     *    
     */
    public List getPermissionsForGB(Long gradebookId)
    throws IllegalArgumentException;
    
    /**
     * Get all permissions for a given list of category Ids
     * @param gradebookId
     * @param cateIds
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForGBForCategoryIds(final Long gradebookId, final List cateIds) throws IllegalArgumentException;

    /**
     * Update permissions.
     *
     * @param perms Collection of persistent permission objects.
     */
    public void updatePermission(Collection perms);
    
    /**
     * Update permission.
     * 
     * @param perm persistent object of Permission
     * @throws IllegalArgumentException
     */
    public void updatePermission(final Permission perm) throws IllegalArgumentException;
    
    /**
     * Delete permission.
     * 
     * @param perm persistent object of Permission
     * @throws IllegalArgumentException
     */
    public void deletePermission(final Permission perm) throws IllegalArgumentException;
    
    /**
     * Get permissions for a user.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUser(final Long gradebookId, final String userId) throws IllegalArgumentException;

    /**
     * Get permissions for a user for certain categories.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @param cateIds category ID list
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUserForCategory(final Long gradebookId, final String userId, final List cateIds) throws IllegalArgumentException;

    /**
     * Get permission for user when the user can grade/view "any" category.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUserAnyCategory(final Long gradebookId, final String userId) throws IllegalArgumentException;

    /**
     * Get permission for user when the user can grade/view "any" group.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUserAnyGroup(final Long gradebookId, final String userId) throws IllegalArgumentException;
    
    /**
     * Get permission for user when the user can grade/view "any" group for certain catetories.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @param cateIds categorie IDs
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUserAnyGroupForCategory(final Long gradebookId, final String userId, final List cateIds) throws IllegalArgumentException;

    /**
     * Get permission for user when the user can grade/view "any" group for any catetory.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUserAnyGroupAnyCategory(final Long gradebookId, final String userId) throws IllegalArgumentException;
    
    /**
     * Get permission for user when the user can grade/view "any" category for certain groups.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @param groupsIds group IDs
     * @return List of permissions
     * @throws IllegalArgumentException
     */
    public List getPermissionsForUserForGoupsAnyCategory(final Long gradebookId, final String userId, final List groupIds) throws IllegalArgumentException;
    
    /**
     * Get permission for user when the user can grade/view for certain groups.
     * 
     * @param gradebookId gradebook ID
     * @param userId grader ID
     * @param groupsIds group IDs
     * @return List of permissions
     * @throws IllegalArgumentException
     */    
    public List getPermissionsForUserForGroup(final Long gradebookId, final String userId, final List groupIds) throws IllegalArgumentException;
    
    /**
     * Add a list of assignments. If errors occur while saving, it will back off all saved ones.
     *
     * @param gradebookId The gradebook ID to which this new assignment belongs
     * @param assignList List of assignments
     */
    public void createAssignments(Long gradebookId, List assignList) throws MultipleAssignmentSavingException;
    
    /**
     * Check if the assignment's name is valid to add or not.
     *
     *@param gradebookId Long of the gradebook's ID
     * @param assignment GradebookAssignment to be added
     * @return boolean
     */
    public boolean checkValidName(final Long gradebookId, final GradebookAssignment assignment);
    
    public void updateCategoryAndAssignmentsPointsPossible(final Long gradebookId, final Category category)
    throws ConflictingAssignmentNameException, StaleObjectModificationException;    
    
    public void applyDropScores(Collection<AssignmentGradeRecord> gradeRecords);
}
