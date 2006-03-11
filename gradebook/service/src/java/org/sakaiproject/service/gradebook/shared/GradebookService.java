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
 * For the Sakai 2.0 Baseline, the gradebook has two programmatic clients: site
 * management (to be able to create a new gradebook instance and associate it
 * with other parts of the site) and Samigo (to add and score externally-managed
 * assignments).
 *
 * <p>
 * <b>WARNING</b>: Unlike some external services in Sakai, the caller of the
 * Gradebook is responsible for "doing the right thing" as far as authorization
 * goes. No unauthorized person should be allowed to reach code that calls these
 * services, so don't just go wrapping this API in an open web service and expect
 * things to work out!
 * <p>
 * The reason is that one of our principal service consumers is an online
 * assessment application which will be updating a student's scores automatically
 * based on the student's performance in the application. In other words, as
 * far as the Gradebook Service code can tell, the student is changing their
 * own grades. Since this is probably the primary security problem that any
 * online gradebook has to be worry about, there's really not much point in
 * going further.
 * <p>
 * Of course, the Gradebook application (as opposed to the external service)
 * does guard against students having such free access. Any other applications
 * that use the business logic need to take the same precautions.
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

    // Assessment management hooks.

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
	 * Check to see if an assignment with the given name already exists
	 * in the given gradebook. This will give external assessment systems
	 * a chance to avoid the ConflictingAssignmentNameException.
	 */
	public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle)
        throws GradebookNotFoundException;

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

}


