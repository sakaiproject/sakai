/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.util.Date;
import java.util.Map;

/**
 * This service is designed for use by external assessment engines. These use
 * the Gradebook as a passive mirror of their own assignments and scores,
 * letting Gradebook users see those assignments alongside Gradebook-managed
 * assignments, and combine them when calculating a course grade. The Gradebook
 * application itself will not modify externally-managed assignments and scores.
 *
 * <b>WARNING</b>: Because the Gradebook project team is not responsible for
 * defining the external clients' requirements, the Gradebook service does not
 * attempt to guess at their authorization needs. Our administrative and
 * external-assessment methods simply follow orders and assume that the caller
 * has taken the responsibility of "doing the right thing." DO NOT wrap these
 * methods in an open web service!
 */

public interface GradebookExternalAssessmentService {
	/**
 	 * @deprecated Replaced by
	 *		{@link addExternalAssessment(String, String, String, String, Double, Date, String, Boolean)}
	 */
	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate, String externalServiceDescription)
            throws GradebookNotFoundException, ConflictingAssignmentNameException,
            ConflictingExternalIdException, AssignmentHasIllegalPointsException;

	/**
	 * Add an externally-managed assessment to a gradebook to be treated as a
	 * read-only assignment. The gradebook application will not modify the
	 * assessment properties or create any scores for the assessment.
     * Since each assignment in a given gradebook must have a unique name,
     * conflicts are possible.
     *
     * @param gradebookUid
     * @param externalId
     *            some unique identifier which Samigo uses for the assessment.
     *            The externalId is globally namespaced within the gradebook, so
     *            if other apps decide to put assessments into the gradebook,
     *            they should prefix their externalIds with a well known (and
     *            unique within sakai) string.
     * @param externalUrl
     *            a link to go to if the instructor or student wants to look at the assessment
     *            in Samigo; if null, no direct link will be provided in the
     *            gradebook, and the user will have to navigate to the assessment
     *            within the other application
     * @param title          
     * @param points
     *            this is the total amount of points available and must be greater than zero.
     *            it could be null if it's an ungraded item.
     * @param dueDate
     * @param externalServiceDescription
     * @param ungraded
     *
	 * @param externalServiceDescription
	 *            what to display as the source of the assignment (e.g., "from Samigo")
     *
	 */
	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, Double points, Date dueDate, String externalServiceDescription, Boolean ungraded)
            throws GradebookNotFoundException, ConflictingAssignmentNameException,
            ConflictingExternalIdException, AssignmentHasIllegalPointsException;

		/**
		 * @deprecated Replaced by
		 *		{@link updateExternalAssessment(String, String, String, String, Double, Date, Boolean)}
		 */
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate)
	        throws GradebookNotFoundException, AssessmentNotFoundException,
            ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
  			String title, Double points, Date dueDate, Boolean ungraded)
  	        throws GradebookNotFoundException, AssessmentNotFoundException,
              ConflictingAssignmentNameException, AssignmentHasIllegalPointsException;

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
	public void removeExternalAssessment(String gradebookUid, String externalId)
        throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
     * Updates an external score for an external assignment in the gradebook.
     *
	 * @param gradebookUid
	 *	The Uid of the gradebook
	 * @param externalId
	 *	The external ID of the assignment/assessment
	 * @param studentUid
	 *	The unique id of the student
	 * @param points
	 *	The number of points earned on this assessment, or null if a score
	 *	should be removed
	 */
//    public void updateExternalAssessmentScore(String gradebookUid, String externalId,
//			String studentUid, Double points)
//            throws GradebookNotFoundException, AssessmentNotFoundException;
	/**
   * Updates an external score for an external assignment in the gradebook.
   *
   * @param gradebookUid
   *	The Uid of the gradebook
   * @param externalId
   *	The external ID of the assignment/assessment
   * @param studentUid
   *	The unique id of the student
   * @param points
   *	The number of points earned on this assessment, or null if a score
   *	should be removed
   */
  public void updateExternalAssessmentScore(String gradebookUid, String externalId,
			String studentUid, String points)
            throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * @deprecated Replaced by
	 *		{@link updateExternalAssessmentScoresString(String, String, Map)}
	 */
	public void updateExternalAssessmentScores(String gradebookUid,
		String externalId, Map studentUidsToScores)
		throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Updates a set of external scores for an external assignment in the gradebook.
	 *
	 * @param gradebookUid
	 *	The Uid of the gradebook
	 * @param externalId
	 *	The external ID of the assignment/assessment
	 * @param studentUidsToScores
	 *	A map whose String keys are the unique ID strings of the students and whose
	 *  String values are points earned on this assessment or null if the score
	 *  should be removed.
	 */
	public void updateExternalAssessmentScoresString(String gradebookUid,
			String externalId, Map studentUidsToScores)
	throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Check to see if an assignment with the given name already exists
	 * in the given gradebook. This will give external assessment systems
	 * a chance to avoid the ConflictingAssignmentNameException.
	 */
	public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle)
        throws GradebookNotFoundException;

	/**
	 * Check to see if an assignment with the given external id already exists
	 * in the given gradebook. This will give external assessment systems
	 * a chance to avoid the ConflictingExternalIdException.
	 *
	 * @param gradebookUid The gradebook's unique identifier
	 * @param externalId The external assessment's external identifier
	 */
	public boolean isExternalAssignmentDefined(String gradebookUid, String externalId)
        throws GradebookNotFoundException;

	/**
	 * Checks to see whether a gradebook with the given uid exists.
	 *
	 * @param gradebookUid
	 *            The gradebook UID to check
	 * @return Whether the gradebook exists
	 */
	public boolean isGradebookDefined(String gradebookUid);
	
	/**
	 * Break the connection between an external assessment engine and an assessment which
	 * it created, giving it up to the Gradebook application to control from now on.
	 * 
	 * @param gradebookUid
	 * @param externalId
	 */
	public void setExternalAssessmentToGradebookAssignment(String gradebookUid, String externalId);

}
