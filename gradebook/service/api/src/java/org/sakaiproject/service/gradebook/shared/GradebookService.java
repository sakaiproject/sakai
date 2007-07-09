/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.util.Collection;
import java.util.Date;
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


	/**
     * Checks to see whether a gradebook with the given uid exists.
     *
     * @param gradebookUid The gradebook UID to check
     * @return Whether the gradebook exists
     */
    public boolean isGradebookDefined(String gradebookUid);

	/**
	 * Check to see if the current user is allowed to grade the given student in
	 * the given gradebook. This will give clients a chance to avoid a security
	 * exception.
	 */
	public boolean isUserAbleToGradeStudent(String gradebookUid,
			String studentUid);

	/**
	 * @return Returns a list of Assignment objects describing the assignments
	 *         that are currently defined in the given gradebook.
	 */
	public List getAssignments(String gradebookUid)
			throws GradebookNotFoundException;

	/**
	 * @param gradebookUid
	 * @param assignmentName
	 * @return the assignment definition, or null if not found
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public Assignment getAssignment(String gradebookUid, String assignmentName) 
		throws GradebookNotFoundException;

	/**
	 * Besides the declared exceptions, possible runtime exceptions include:
	 * <ul>
	 * <li> SecurityException - If the current user is not authorized to view
	 * the student's score
	 * </ul>
	 * 
	 * @return Returns the current score for the student, or null if no score
	 *         has been assigned yet.
	 */
	public Double getAssignmentScore(String gradebookUid,
			String assignmentName, String studentUid)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Get the comment (if any) currently provided for the given combination
	 * of student and assignment. 
	 * 
	 * @param gradebookUid
	 * @param assignmentName
	 * @param studentUid
	 * @return null if no comment is avaailable
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public CommentDefinition getAssignmentScoreComment(String gradebookUid,
			String assignmentName, String studentUid)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Besides the declared exceptions, possible runtime exceptions include:
	 * <ul>
	 * <li> SecurityException - If the current user is not authorized to grade
	 * the student, or if the assignment is externally maintained.
	 * <li> StaleObjectModificationException - If the student's scores have been
	 * edited by someone else during this transaction.
	 * </ul>
	 * 
	 * @param clientServiceDescription
	 *            What to display as the programmatic source of the score (e.g.,
	 *            "Message Center").
	 */
	public void setAssignmentScore(String gradebookUid, String assignmentName,
			String studentUid, Double score, String clientServiceDescription)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Provide a student-viewable comment on the score (or lack of score) associated
	 * with the given assignment.
	 * 
	 * @param gradebookUid
	 * @param assignmentName
	 * @param studentUid
	 * @param comment a plain text comment, or null to remove any currrent comment
	 * @throws GradebookNotFoundException
	 * @throws AssessmentNotFoundException
	 */
	public void setAssignmentScoreComment(String gradebookUid, String assignmentName,
			String studentUid, String comment)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Check to see if an assignment with the given name already exists in the
	 * given gradebook. This will give clients a chance to avoid the
	 * ConflictingAssignmentNameException.
	 */
	public boolean isAssignmentDefined(String gradebookUid,
			String assignmentTitle) throws GradebookNotFoundException;

	/**
	 * Get an archivable definition of gradebook data suitable for migration
	 * between sites. Assignment definitions and the currently selected grading
	 * scale are included. Student view options and all information related
	 * to specific students or instructors (such as scores) are not.
	 * @param gradebookUid
	 * @return a versioned XML string
	 */
	public String getGradebookDefinitionXml(String gradebookUid);
	
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
	 * Create a new Gradebook-managed assignment.
	 * 
	 * @param assignmentDefinition
	 */
	public void addAssignment(String gradebookUid, Assignment assignmentDefinition);
	
	/**
	 * Modify the definition of an existing Gradebook-managed assignment.
	 * 
	 * Clients should be aware that it's allowed to change the points value of an
	 * assignment even if students have already been scored on it. Any existing
	 * scores will not be adjusted.
	 * 
	 * This method cannot be used to modify the defintions of externally-managed
	 * assessments or to make Gradebook-managed assignments externally managed. 
	 * 
	 * @param assignmentName the name of the assignment that needs to be changed
	 * @param assignmentDefinition the new properties of the assignment
	 */
	public void updateAssignment(String gradebookUid, String assignmentName, Assignment assignmentDefinition);
	
	// Site management hooks.

	/**
	 * @deprecated Replaced by
	 *             {@link GradebookFrameworkService#addGradebook(String, String)}
	 */
	public void addGradebook(String uid, String name);

	/**
	 * @deprecated Replaced by
	 *             {@link GradebookFrameworkService#deleteGradebook(String)}
	 */
	public void deleteGradebook(String uid) throws GradebookNotFoundException;

	/**
	 * @deprecated Replaced by
	 *             {@link GradebookFrameworkService#setAvailableGradingScales(Collection)}
	 */
	public void setAvailableGradingScales(Collection gradingScaleDefinitions);

	/**
	 * @deprecated Replaced by
	 *             {@link GradebookFrameworkService#setDefaultGradingScale(String)}
	 */
	public void setDefaultGradingScale(String uid);

	// External assessment management hooks.

	/**
	 * @deprecated Replaced by {@link GradebookExternalAssessmentService#addExternalAssessment(String, String, String, String, double, Date, String)}
	 */
	public void addExternalAssessment(String gradebookUid, String externalId,
			String externalUrl, String title, double points, Date dueDate,
			String externalServiceDescription)
			throws GradebookNotFoundException,
			ConflictingAssignmentNameException, ConflictingExternalIdException,
			AssignmentHasIllegalPointsException;

	/**
	 * @deprecated Replaced by {@link GradebookExternalAssessmentService#updateExternalAssessment(String, String, String, String, double, Date)}
	 */
	public void updateExternalAssessment(String gradebookUid,
			String externalId, String externalUrl, String title, double points,
			Date dueDate) throws GradebookNotFoundException,
			AssessmentNotFoundException, ConflictingAssignmentNameException,
			AssignmentHasIllegalPointsException;

	/**
	 * @deprecated Replaced by {@link GradebookExternalAssessmentService#removeExternalAssessment(String, String)}
	 */
	public void removeExternalAssessment(String gradebookUid, String externalId)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * @deprecated Replaced by {@link GradebookExternalAssessmentService#updateExternalAssessmentScore(String, String, String, Double)}
	 */
	public void updateExternalAssessmentScore(String gradebookUid,
			String externalId, String studentUid, Double points)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * @deprecated Replaced by {@link GradebookExternalAssessmentService#updateExternalAssessmentScores(String, String, Map)}
	 */
	public void updateExternalAssessmentScores(String gradebookUid,
			String externalId, Map studentUidsToScores)
			throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * @deprecated Replaced by {@link GradebookExternalAssessmentService#isExternalAssignmentDefined(String, String)}
	 */
	public boolean isExternalAssignmentDefined(String gradebookUid,
			String externalId) throws GradebookNotFoundException;

	public Map getImportCourseGrade(String gradebookUid);
	  
	/**return Object to avoid circular dependency with sakai-gradebook-tool */
	public Object getGradebook(String uid) throws GradebookNotFoundException;

}
