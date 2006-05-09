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

import java.util.*;

/**
 * This is the externally exposed API of the gradebook application.
 *
 * The Gradebook project has three types of programmatic clients.
 *
 * 1. The framework. This manages the application, performing various
 * administrative functions which aren't handled by the application itself,
 * including creating and removing gradebooks, and managing system-wide grading
 * schemes.
 *
 * 2. External assessment engines. These use the Gradebook as a passive
 * mirror of their own assignments and scores. Gradebook users will thus
 * be able to see those assignments alongside Gradebook-managed assignments,
 * and combine them when calculating a course grade. The Gradebook application
 * itself will not change the externally-managed assignments and scores.
 *
 * 3. Clients of application services -- that is, clients who want to "act
 * like the Gradebook would" to automate what would normally be done in the UI.
 *
 * Currently all three clients are served by this single interface. The Gradebook
 * team plans to split the interface by client type in a future release.
 *
 * <p>
 * <b>WARNING</b>: Because the Gradebook project team is not responsible
 * for defining the first two types of client, the Gradebook service
 * does not attempt to guess at their authorization needs. Our administrative
 * and external-assessment methods simply follow orders and assume that the caller
 * has taken the responsibility of "doing the right thing." DO NOT wrap these
 * methods in an open web service!
 * <p>
 * The behave-like-the-application service methods DO mimic normal Gradebook
 * authorization as part of mimicking normal Gradebook behavior. These methods
 * may throw security exceptions. Call the service's authorization-check methods
 * if you want to avoid them.
 */
public interface GradebookService {
	// Site management hooks.

    /**
     * Creates a new gradebook with the given UID and name
     *
     * @param uid The UID used to specify a gradebook and its associated data.
     * It is the caller's responsibility to ensure that this is unique within
     * gradebook storage.
     *
     * @param name The name of the gradebook, to be used for logging and other
     * conveniences by the application.  This should be the name of the site or
     * the course.  It is only used for convenience, and does not need to be unique.
     */
	public void addGradebook(String uid, String name);

    /**
     * Deletes the gradebook with the given UID, along with all its associated data.
     */
	public void deleteGradebook(String uid)
		throws GradebookNotFoundException;

    /**
     * Checks to see whether a gradebook with the given uid exists.
     *
     * @param gradebookUid The gradebook UID to check
     * @return Whether the gradebook exists
     */
    public boolean isGradebookDefined(String gradebookUid);

    /**
     * @deprecated	Replaced by {@link #isGradebookDefined(String)}
     */
    public boolean gradebookExists(String gradebookUid);

    // External assessment management hooks.

	/**
	 * Add an externally-managed assessment to a gradebook to be treated as a
	 * read-only assignment. The gradebook application will not modify the
	 * assessment properties or create any scores for the assessment.
     * Since each assignment in a given gradebook must have a unique name,
     * conflicts are possible.
     *
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
     * @param points
     *            this is the total amount of points available and must be greater than zero
     *
	 * @param externalServiceDescription
	 *            what to display as the source of the assignment (e.g., "from Samigo")
     *
	 */
	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate, String externalServiceDescription)
            throws GradebookNotFoundException, ConflictingAssignmentNameException,
            ConflictingExternalIdException, AssignmentHasIllegalPointsException;


    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate)
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
    public void updateExternalAssessmentScore(String gradebookUid, String externalId,
			String studentUid, Double points)
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
	 *  Double values are points earned on this assessment or null if the score
	 *  should be removed.
	 */
	public void updateExternalAssessmentScores(String gradebookUid,
		String externalId, Map studentUidsToScores)
		throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
	 * Check to see if an assignment with the given name already exists
	 * in the given gradebook. This will give external assessment systems
	 * a chance to avoid the ConflictingAssignmentNameException.
	 */
	public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle)
        throws GradebookNotFoundException;

    // Application service hooks.

    /**
     * Check to see if the current user is allowed to grade the given student
     * in the given gradebook. This will give clients a chance to avoid a
     * security exception.
     */
    public boolean isUserAbleToGradeStudent(String gradebookUid, String studentUid);

    /**
     * @return Returns a list of Assignment objects describing the assignments
     *         that are currently defined in the given gradebook.
     */
    public List getAssignments(String gradebookUid)
    	throws GradebookNotFoundException;

    /**
     * Besides the declared exceptions, possible runtime exceptions include:
     * <ul>
     *   <li> SecurityException - If the current user is not authorized to grade
     *        the student
     * </ul>
     * @return Returns the current score for the student, or null if no score
     *         has been assigned yet.
     */
    public Double getAssignmentScore(String gradebookUid, String assignmentName, String studentUid)
        throws GradebookNotFoundException, AssessmentNotFoundException;

    /**
     * Besides the declared exceptions, possible runtime exceptions include:
     * <ul>
     *   <li> SecurityException - If the current user is not authorized to grade
     *        the student, or if the assignment is externally maintained.
     *   <li> StaleObjectModificationException - If the student's scores have been
     *        edited by someone else during this transaction.
     * </ul>
     *
     * @param clientServiceDescription
     *            What to display as the programmatic source of the score (e.g., "Message Center").
     */
    public void setAssignmentScore(String gradebookUid, String assignmentName, String studentUid, Double score, String clientServiceDescription)
        throws GradebookNotFoundException, AssessmentNotFoundException;

	/**
     * @param gradingScaleDefinitions
     *	A collection of GradingScaleDefinition beans.
	 */

	public void setAvailableGradingScales(Collection gradingScaleDefinitions);

	public void setDefaultGradingScale(String uid);
}
