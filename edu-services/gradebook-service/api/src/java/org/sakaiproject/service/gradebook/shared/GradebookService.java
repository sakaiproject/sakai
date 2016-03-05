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
package org.sakaiproject.service.gradebook.shared;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Set;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This is the externally exposed API of the gradebook application.
 * 
 * This interface is principally intended for clients of application services --
 * that is, clients who want to "act like the Gradebook would" to automate what
 * would normally be done in the UI, including any authorization checks.
 * 
 * As a result, these methods may throw security exceptions. Call the service's
 * authorization-check methods if you want to avoid them.
 * 
 * <p>WARNING: For documentation of the deprecated methods, please see the
 * service interfaces which own them.
 */
public interface GradebookService {
	// Application service hooks.
	public static final int GRADE_TYPE_POINTS = 1;
	public static final int GRADE_TYPE_PERCENTAGE = 2;
	public static final int GRADE_TYPE_LETTER = 3;
	
	public static final int CATEGORY_TYPE_NO_CATEGORY = 1;
	public static final int CATEGORY_TYPE_ONLY_CATEGORY = 2;
	public static final int CATEGORY_TYPE_WEIGHTED_CATEGORY = 3;

	public static final String[] validLetterGrade = {"a+", "a", "a-", "b+", "b", "b-",
    "c+", "c", "c-", "d+", "d", "d-", "f"};
	
	// These Strings have been kept for backwards compatibility as they are used everywhere,
	// however the {@link GraderPermission} enum should be used going forward.
	@Deprecated public static final String gradePermission = GraderPermission.GRADE.toString();
	@Deprecated public static final String viewPermission = GraderPermission.VIEW.toString();
	
	public static final String enableLetterGradeString = "gradebook_enable_letter_grade";
	
	public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_DOWN);
	
	/** 
     * An enum for defining valid/invalid information for a points possible/relative weight
     * value for a gradebook item. See {@link GradebookService#isPointsPossibleValid(String, Assignment, Double)}
     * for usage
     */
    public enum PointsPossibleValidation {
        /**
         * The points possible/relative weight is valid
         */
        VALID,
        /**
         * The points possible/relative weight is invalid because it is null 
         * and a value is required.
         */
        INVALID_NULL_VALUE,
        /**
         * The points possible/relative weight is invalid because it
         * is a value <= 0
         */
        INVALID_NUMERIC_VALUE,
        /**
         * The points possible/relative weight is invalid because it contains
         * more than 2 decimal places
         */
        INVALID_DECIMAL
    }
	
    /**
     * Comparator to ensure correct ordering of letter grades, catering for + and - in the grade
     */
    public static Comparator<String> lettergradeComparator = new Comparator<String>() {
		@Override
    	public int compare(String o1, String o2){
			if(o1.toLowerCase().charAt(0) == o2.toLowerCase().charAt(0)) {
				if(o1.length() == 2 && o2.length() == 2) {
					if(o1.charAt(1) == '+') {
						return -1; //SAK-30094
					} else {
						return 1;
					}
				}
				if(o1.length() == 1 && o2.length() == 2) {
					if(o2.charAt(1) == '+') {
						return 1; //SAK-30094
					} else {
						return -1;
					}
				}
				if(o1.length() == 2 && o2.length() == 1) {
					if(o1.charAt(1) == '+') {
						return -1; //SAK-30094
					} else {
						return 1;
					}
				}
				return 0;
			}
			else {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		}
	};
	
	/**
     * Checks to see whether a gradebook with the given uid exists.
     *
     * @param gradebookUid The gradebook UID to check
     * @return Whether the gradebook exists
     */
    public boolean isGradebookDefined(String gradebookUid);

	/**
	 * Check to see if the current user is allowed to grade the given item for the given student in
	 * the given gradebook. This will give clients a chance to avoid a security
	 * exception.
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 */
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long assignmentId, String studentUid);
		
	/**
	 * Check to see if the current user is allowed to view the given item for the given student in
	 * the given gradebook. This will give clients a chance to avoid a security
	 * exception.
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @return
	 */
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long assignmentId, String studentUid);
		
	/**
	 * Check to see if current user may grade or view the given student for the given item in the given gradebook.
	 * Returns string representation of function per GradebookService vars (view/grade) or null if no permission
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @return GradebookService.gradePermission, GradebookService.viewPermission, or null if no permission
	 */
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long assignmentId, String studentUid);
		

	/**
	 * @return Returns a list of Assignment objects describing the assignments
	 *         that are currently defined in the given gradebook.
	 */
	public List<Assignment> getAssignments(String gradebookUid)
			throws GradebookNotFoundException;
	
	/**
	 * @return Returns a list of Assignment objects describing the assignments
	 *         that are currently defined in the given gradebook, sorted by the given sort type.
	 */
	public List<Assignment> getAssignments(String gradebookUid, SortType sortBy)
			throws GradebookNotFoundException;

	/**
	 * Get an assignment based on its id
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @return the associated Assignment with the given assignmentId
	 * @throws AssessmentNotFoundException
	 */
	public Assignment getAssignment(String gradebookUid, Long assignmentId)
		throws AssessmentNotFoundException;
	
	/**
	 * Get an assignment based on its name.
	 * This is provided for backward compatibility only.
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

	/**
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @return Returns a GradeDefinition for the student, respecting the grade 
	 * entry type for the gradebook (ie in %, letter grade, or points format).
	 * Returns null if no grade
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public GradeDefinition getGradeDefinitionForStudentForItem(String gradebookUid,
			Long assignmentId, String studentUid)
			throws GradebookNotFoundException, AssessmentNotFoundException;
	
	/**
	 * Get the comment (if any) currently provided for the given combination
	 * of student and assignment. 
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @return null if no comment is avaailable
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public CommentDefinition getAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid)
			throws GradebookNotFoundException, AssessmentNotFoundException;


	/**
	 * Provide a student-viewable comment on the score (or lack of score) associated
	 * with the given assignment.
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @param comment a plain text comment, or null to remove any currrent comment
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, String comment)
			throws GradebookNotFoundException, AssessmentNotFoundException;


	/**
	 * Check to see if an assignment with the given name already exists in the
	 * given gradebook. This will give clients a chance to avoid the
	 * ConflictingAssignmentNameException.
	 * 
	 * This is not deprecated as we currently need the ability to check for duplciate assignment names in the given gradebook
	 * 
	 */
	public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle) 
			throws GradebookNotFoundException;

	/**
	 * Get an archivable definition of gradebook data suitable for migration
	 * between sites. Assignment definitions and the currently selected grading
	 * scale are included. Student view options and all information related
	 * to specific students or instructors (such as scores) are not.
	 * @param gradebookUid
	 * @return a versioned XML string
	 * 
	 * @deprecated This is used by the old gradebook1 entityproducer and will soon be redundant
	 */
	@Deprecated
	public String getGradebookDefinitionXml(String gradebookUid);
	
	
	/**
	 * Attempt to transfer gradebook data with Category and weight and settings
	 * 
	 * @param fromGradebookUid
	 * @param toGradebookUid
	 * @param fromGradebookXml
	 * 
	 * @deprecated This is used by the old gradebook1 entityproducer and will soon be redundant
	 */
	@Deprecated
	public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml);
	
	/**
	 * Transfer the gradebook information and assignments from one gradebook to another
	 * 
	 * @param gradebookInformation GradebookInformation to copy
	 * @param assignments list of Assignments to copy
	 * @param toGradebookUid target gradebook uid
	 */
	public void transferGradebook(final GradebookInformation gradebookInformation, final List<Assignment> assignments, final String toGradebookUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @return a {@link GradebookInformation} object that contains information about this
	 * Gradebook that may be useful to consumers outside the Gradebook tool
     * 
	 */
	public GradebookInformation getGradebookInformation(String gradebookUid);
	
	/**
	 * Attempt to merge archived gradebook data (notably the assignnments) into a new gradebook.
	 * 
	 * Assignment definitions whose names match assignments that are already in
	 * the targeted gradebook will be skipped.
	 * 
	 * Imported assignments will not automatically be released to students, even if they
	 * were released in the original gradebook.
	 * 
	 * Externally managed assessments will not be imported, since such imports
	 * should be handled by the external assessment engine.
	 * 
	 * If possible, the targeted gradebook's selected grading scale will be set
	 * to match the archived grading scale. If there are any mismatches that make
	 * this impossible, the existing grading scale will be left alone, but assignment
	 * imports will still happen.
	 * 
	 * @param toGradebookUid
	 * @param fromGradebookXml
	 */
	public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml);
	
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
     * 
     * Get the categories for the given gradebook. This method cannot be used outside
     * of the gradebook because it returns the org.sakaiproject.tool.gradebook.Category object.
     * If you require info on the categories from a consumer outside the gradebook, use 
     * {@link #getCategoryDefinitions(String)}
     *
     * @param gradebookId
     * @return List of categories
     * @throws HibernateException
     * 
     * @deprecated 
     */
    @Deprecated
    public List getCategories(final Long gradebookId);
    
    /**
     * Get the categories for the given gradebook
	 * 
	 * @param gradebookUid
	 * @return {@link CategoryDefinition}s for the categories defined for the given gradebook.
	 * Returns an empty list if the gradebook does not have categories.
	 * @throws GradebookNotFoundException
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
	 * Clients should be aware that it's allowed to change the points value of an
	 * assignment even if students have already been scored on it. Any existing
	 * scores will not be adjusted.
	 * 
	 * This method can be used to manage both internal and external gradebook items,
	 * however the title, due date and total points will not be edited for external gradebook items.
	 * 
	 * @param assignmentId the id of the assignment that needs to be changed
	 * @param assignmentDefinition the new properties of the assignment
	 */
	public void updateAssignment(String gradebookUid, Long assignmentId, Assignment assignmentDefinition);
	
	/**
	 * 
	 * @param gradebookUid
	 * @return list of gb items that the current user is authorized to view.
	 * If user has gradeAll permission, returns all gb items.
	 * If user has gradeSection perm with no grader permissions,
	 * returns all gb items. 
	 * If user has gradeSection with grader perms, returns only the items that
	 * the current user is authorized to view or grade.
	 * If user does not have grading privileges but does have viewOwnGrades perm,
	 * will return all released gb items.
	 */
	public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid);
	
	/**
	 *
	 * @param gradebookUid
	 * @return list of gb items that the current user is authorized to view
	 * sorted by the provided SortType.
	 * If user has gradeAll permission, returns all gb items.
	 * If user has gradeSection perm with no grader permissions,
	 * returns all gb items.
	 * If user has gradeSection with grader perms, returns only the items that
	 * the current user is authorized to view or grade.
	 * If user does not have grading privileges but does have viewOwnGrades perm,
	 * will return all released gb items.
	 */
	public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid, SortType sortBy);

	/**
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @return a map of studentId to view/grade function  for the given 
	 * gradebook and gradebook item. students who are not viewable or gradable
	 * will not be returned. if the current user does not have grading privileges,
	 * an empty map is returned
	 */
	public Map<String, String> getViewableStudentsForItemForCurrentUser(String gradebookUid, Long assignmentId);
	
	/**
     * @param userUid
     * @param gradebookUid
     * @param assignmentId
     * @return a map of studentId to view/grade function for the given 
     * gradebook and gradebook item that the given userUid is allowed to view or grade.
     * students who are not viewable or gradable will not be returned. if the
     * given user does not have grading privileges, an empty map is returned
     */
    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, Long assignmentId);
	

    /**
	 * This is the preferred method to retrieve a Map of student ids and course grades for a site.
	 * Use this method instead of older methods like getCalculatedCourseGrade (removed in Sakai 11)
	 * @param gradebookUid
	 * @return A mapping from user display IDs to grades.  If no grade is available for a user, default to zero.
	 */
	public Map<String,String> getImportCourseGrade(String gradebookUid);
	
	/**
	 * @param gradebookUid
	 * @param useDefault If true, assume zero for missing grades.  Otherwise, null.
	 * @return A mapping from user display IDs to grades.
	 */
	public Map<String,String> getImportCourseGrade(String gradebookUid, boolean useDefault);
	
	/**
 	 * @param gradebookUid
 	 * @param useDefault If true, assume zero for missing grades.  Otherwise, null.
 	 * @param mapTheGrades If true, map the numerical grade to letter grade. If false, return a string of the numerical grade.
 	 * @return A mapping from user display IDs to grades.
 	 */
	public Map<String,String> getImportCourseGrade(String gradebookUid, boolean useDefault, boolean mapTheGrades);

	/**
	 * Get the Gradebook. Note that this returns Object to avoid circular dependency with sakai-gradebook-tool
	 * Consumers will need to cast to {@link org.sakaiproject.tool.gradebook.Gradebook}
	 *
	 */
	public Object getGradebook(String uid) throws GradebookNotFoundException;

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
	 * @return true if a gradable object with the given id exists and was not
	 * removed
	 */
	public boolean isGradableObjectDefined(Long gradableObjectId);
	
	/**
	 * Using the grader permissions, return map of section uuid to section name
	 * that includes all sections that the current user may view or grade
	 * @param gradebookUid
	 * @return
	 */
	public Map<String,String> getViewableSectionUuidToNameMap(String gradebookUid);
	
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
	 * @return true if the given user is allowed to grade all students in this
	 * gradebook
	 */
	public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid);
	
	/**
	 * @param gradebookUid
	 * @return true if the current user has some form of
     * grading privileges in the gradebook (grade all, grade section, etc)
	 */
	public boolean currentUserHasGradingPerm(String gradebookUid);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param userUid
	 * @return true if the given user has some form of
	 * grading privileges in the gradebook (grade all, grade section, etc)
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
	 * Get the grade records for the given list of students and the given assignment.
	 * This can only be called by an instructor or TA that has access, not student.
	 * 
	 * See {@link #getGradeDefinitionForStudentForItem} for the method call that can be made as a student.
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentIds
	 * @return a list of GradeDefinition with the grade information for the given
	 * students for the given gradableObjectId
	 * @throws SecurityException if the current user is not authorized to view
	 * or grade a student in the passed list
	 */
	public List<GradeDefinition> getGradesForStudentsForItem(String gradebookUid, Long assignmentId, List<String> studentIds);

	/**
	 * This method gets grades for multiple gradebook items with emphasis on performance. This is particularly useful for reporting tools
	 * @param gradebookUid
	 * @param gradableObjectIds
	 * @param studentIds
	 * @return a Map of GradableObjectIds to a List of GradeDefinitions containing the grade information for the given
	 * students for the given gradableObjectIds. Comments are excluded which can be useful for performance.
	 * If a student does not have a grade on a gradableObject, the GradeDefinition will be omitted
	 * @throws SecurityException if the current user is not authorized with gradeAll in this gradebook
	 * @throws IllegalArgumentException if gradableObjectIds is null/empty,
	 * or if gradableObjectIds contains items that are not members of the gradebook with uid = gradebookUid
	 */
	public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(String gradebookUid, List<Long> gradableOjbectIds, List<String> studentIds);
	
	/**
	 * 
	 * @param gradebookUuid
	 * @param grade
	 * @return true if the given grade is a valid grade given the gradebook's grade
	 * entry type.  ie, if gradebook is set to grade entry by points, will check for valid point value.
	 * if entry by letter, will check for valid letter, etc
	 * @throws GradebookNotFoundException if no gradebook exists with given gradebookUid
	 */
	public boolean isGradeValid(String gradebookUuid, String grade)
		throws GradebookNotFoundException;
	
	/**
	 * 
	 * @param gradebookUid
	 * @param studentIdToGradeMap - the student's username mapped to their grade
	 * that you want to validate
	 * @return a list of the studentIds that were associated with invalid grades
	 * given the gradebook's grade entry type. useful if validating a list
	 * of student/grade pairs for a single gradebook (more efficient than calling
	 * gradeIsValid repeatedly). returns empty list if all grades are valid
	 * @throws GradebookNotFoundException if no gradebook exists with given gradebookUid
	 */
	public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap)
		throws GradebookNotFoundException;
	
	/**
	 * Save a student score and comment for a gradebook item. The input score must
	 * be valid according to the given gradebook's grade entry type.
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentId
	 * @param grade - must be in format according to gradebook's grade entry type
	 * @param comment
	 * @throws InvalidGradeException - if grade is invalid. grade and comment will not be saved
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 * @throws SecurityException if current user is not authorized to grade student
	 */
	public void saveGradeAndCommentForStudent(String gradebookUid, Long assignmentId, String studentId, String grade, String comment)
			throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException;
	
	/**
	 * Given a list of GradeDefinitions for students for a given gradebook and gradable object,
	 * will save the associated scores and comments.  Scores must be in a format 
	 * according to the gradebook's grade entry type (ie points, %, letter).
	 * @param gradebookUid
	 * @param assignmentId
	 * @param gradeDefList
	 * @throws InvalidGradeException if any of the grades are not valid - none will be saved
	 * @throws SecurityException if the user does not have access to a student in the list -
	 * no grades or comments will be saved for any student
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public void saveGradesAndComments(String gradebookUid, Long assignmentId, List<GradeDefinition> gradeDefList)
		throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * 
	 * @param gradebookUid
	 * @return the constant representation of the grade entry type
	 * (ie points, %, letter grade)
	 * @throws GradebookNotFoundException if no gradebook exists w/ the given uid
	 */
	public int getGradeEntryType(String gradebookUid) throws GradebookNotFoundException;

	/**
	 * Get a Map of overridden CourseGrade for students.
	 * @param gradebookUid
	 * @return Map of enrollment displayId as key, point as value string
	 * 
	 */
	public Map<String,String> getEnteredCourseGrade(String gradebookUid);

	/**
	 * Get student's assignment's score as string.
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @return String of score
	 */
	public String getAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid)
			throws GradebookNotFoundException, AssessmentNotFoundException;
	
	/**
	 * Get student's assignment's score as string.
	 * This is provided for backward compatibility only.
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
			throws GradebookNotFoundException, AssessmentNotFoundException;
	
	/**
	 * Set student's score for assignment.
	 * @param gradebookUid
	 * @param assignmentId
	 * @param studentUid
	 * @param score
	 * @param clientServiceDescription
	 * 
	 */
	public void setAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid, String score, String clientServiceDescription)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Set student's score for assignment.
	 * This is provided for backward compatibility only.
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
	public void setAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid, String score, String clientServiceDescription)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	
	/**
	 * Finalize the gradebook's course grades by setting all still-unscored assignments
	 * to zero scores.
	 * @param gradebookUid
	 * @throws GradebookNotFoundException
	 */
	public void finalizeGrades(String gradebookUid)
			throws GradebookNotFoundException;
	
	/**
	 * 
	 * @param gradebookUid
	 * @param assignmentId
	 * @return the lowest possible grade allowed for the given assignmentId.
	 * For example, in a points or %-based gradebook, the lowest possible grade for
	 * a gradebook item is 0.  In a letter-grade gb, it may be 'F' depending on
	 * the letter grade mapping. Ungraded items have a lowest value of null.
	 * @throws SecurityException if user does not have permission to view assignments
	 * in the given gradebook
	 * @throws AssessmentNotFoundException if there is no gradebook item with the given gradebookItemId
	 */
	public String getLowestPossibleGradeForGbItem(final String gradebookUid, final Long assignmentId);
	
	/**
	 * 
	 * @param gradebookUid (non-null)
	 * @param assignment (non-null) the Assignment object representing the gradebook item for which you are
	 * setting the points possible (aka relative weight). May be a new gradebook item without
	 * an id yet.
	 * @param pointsPossible the points possible/relative weight you would like to validate
	 * for the gradebookItem above.
	 * @return {@link PointsPossibleValidation} value indicating the validity of the given
	 * points possible/relative weight or a problem code defining why it is invalid
	 */
	public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, Assignment assignment, Double pointsPossible);
	   
	/**
	 * Computes the Average Course Grade as a letter.   
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
	public void updateAssignmentOrder(final String gradebookUid, final Long assignmentId, final Integer order);

	 /**
     * Gets the grading events for the given student and the given assignment
     * @param studentId
     * @param assignmentId
     * @return List of GradingEvent objects.
     */
    @SuppressWarnings("rawtypes")
	public List getGradingEvents(final String studentId, final long assignmentId);
    
    /**
     * Calculate the category score for the given gradebook, student and category, looking up the grades.
     * Safe to call in context of a student.
     * 
     * @param gradebookId Id of the gradebook
     * @param studentUuid uuid of the student
     * @param categoryId id of category
     * @return percentage or null if no calculations were made
     * 
     */
	Double calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId);
	
	/**
     * Calculate the category score for the given gradebook, category, viewable assignment list and grade map.
     * This doesn't do any additional grade lookups.
     * Safe to call in context of a student.
     * 
     * @param gradebook the gradebook. As this method is called for every student at once, this is passed in to save additional lookups by id.
     * @param studentUuid uuid of the student
     * @param categoryId id of category
     * @param assignments list of assignments the student can view
     * @param gradeMap map of assignmentId to grade, to use for the calculations
     * @return percentage or null if no calculations were made
     */
	Double calculateCategoryScore(Object gradebook, String studentUuid, CategoryDefinition category, final List<Assignment> viewableAssignments, Map<Long,String> gradeMap);

    /**
     * Get the course grade for a student
     * 
     * @param gradebookUid
     * @param userUuid uuid of the user
     * @return The {@link CourseGrade} for the student
     */
	CourseGrade getCourseGradeForStudent(String gradebookUid, String userUuid);
	
	 /**
     * Get the course grade for a list of students
     * 
     * @param gradebookUid
     * @param userUuids uuids of the users
     * @return a List of {@link CourseGrade} for the students
     */
	List<CourseGrade> getCourseGradeForStudents(String gradebookUid, List<String> userUuids);
	
	/**
	 * Get a list of CourseSections that the current user has access to in the given gradebook.
	 * This is a combination of sections and groups and is permission filtered.
	 * @param gradebookUid
	 * @return list of CourseSection objects.
	 */
	@SuppressWarnings("rawtypes")
	List getViewableSections(String gradebookUid);

	/**
	 * Update the settings for this gradebook
	 * 
	 * @param gradebookUid
	 * @param gbInfo GradebookInformation object
	 */
	void updateGradebookSettings(String gradebookUid, GradebookInformation gbInfo);

	/**
	 * Return the GradeMappings for the given gradebook. The normal getGradebook(siteId)
	 * doesn't return the GradeMapping.
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
	 * @param gradebookUid 	uuid of the gradebook
	 * @param studentUuid	uuid of the student
	 * @param grade			the new course grade
	 */
	void updateCourseGradeForStudent(String gradebookUid, String studentUuid, String grade);

	/**
	 * Updates the categorized order of an assignment
	 * @param gradebookUid 	uuid of the gradebook
	 * @param categoryId	id of the category
	 * @param assignmentId	id of the assignment
	 * @param order	new position of the assignment
	 */
	void updateAssignmentCategorizedOrder(final String gradebookUid, final Long categoryId, final Long assignmentId, Integer order);
}
