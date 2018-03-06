/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.api;

import java.io.OutputStream;
import java.time.Instant;
import java.util.*;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Element;

/**
 * <p>
 * AssignmentService is the service that handles assignments.
 * </p>
 * <p>
 * Each Assignment has an associated AssignmentContent (an AssignmentContent can belong to more the one Assignment) and a list of AssignmentSubmission (the student responses to the Assignment).
 * </p>
 */

public interface AssignmentService extends EntityProducer {
    Entity createAssignmentEntity(String assignmentId);

    Entity createAssignmentEntity(Assignment assignment);

    /**
     * Check permissions for receiving assignment submission notification email
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to receive the email, false if not.
     */
    public boolean allowReceiveSubmissionNotification(String context);

    /**
     * Get the List of Users who can add assignment
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return the List (User) of users who are allowed to receive the email, false if not.
     */
    public List<User> allowReceiveSubmissionNotificationUsers(String context);

    /**
     * @param context
     * @return
     */
    public Collection<Group> getGroupsAllowRemoveAssignment(String context);

    /**
     * Check permissions for adding an Assignment.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to add an Assignment, false if not.
     */
    public boolean allowAddAssignment(String context);

    /**
     * Check permissions for updating an Assignment based on context.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to update assignments, false if not.
     */
    public boolean allowUpdateAssignmentInContext(String context);

    /**
     * Check if the user has permission to add a site-wide (not grouped) assignment.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return true if the user has permission to add a channel-wide (not grouped) assignment.
     */
    boolean allowAddSiteAssignment(String context);

    /**
     * Check permissions for removing an Assignment.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to remove an Assignment, false if not.
     */
    public boolean allowRemoveAssignmentInContext(String context);

    /**
     * Check permissions for all.groups.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed all.groups, false if not.
     */
    public boolean allowAllGroups(String context);

    /**
     * Check permissions for reading an Assignment.
     *
     * @param assignmentReference -
     *                            The Assignment's reference.
     * @return True if the current User is allowed to get the Assignment, false if not.
     */
    public boolean allowGetAssignment(String assignmentReference);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has add assignment permissions in.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return The Collection (Group) of groups defined for the context of this site that the end user has add assignment permissions in, empty if none.
     */
    public Collection<Group> getGroupsAllowAddAssignment(String context);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has update assignment permissions in.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return The Collection (Group) of groups defined for the context of this site that the end user has update assignment permissions in, empty if none.
     */
    public Collection<Group> getGroupsAllowUpdateAssignment(String context);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has grade assignment permissions in.
     *
     * @return The Collection (Group) of groups defined for the context of this site that the end user has grade assignment permissions in, empty if none.
     */
    public Collection<Group> getGroupsAllowGradeAssignment(String assignmentReference);

    /**
     * Check permissions for updating an Assignment.
     *
     * @param assignmentReference -
     *                            The Assignment's reference.
     * @return True if the current User is allowed to update the Assignment, false if not.
     */
    public boolean allowUpdateAssignment(String assignmentReference);

    /**
     * Check permissions for removing an Assignment.
     *
     * @param assignmentReference -
     *                            The Assignment's reference.
     * @return True if the current User is allowed to remove the Assignment, false if not.
     */
    public boolean allowRemoveAssignment(String assignmentReference);

    /**
     * Check permissions for add AssignmentSubmission
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to add an AssignmentSubmission, false if not.
     */
    public boolean allowAddSubmission(String context);

    /**
     * @param assignment - An Assignment object. Needed for the groups to be checked.
     * @return
     */
    public boolean allowAddSubmissionCheckGroups(Assignment assignment);

    /**
     * Get the List of Users who can addSubmission() for this assignment.
     *
     * @param assignmentReference -
     *                            a reference to an assignment
     * @return the List (User) of users who can addSubmission() for this assignment.
     */
    public List<User> allowAddSubmissionUsers(String assignmentReference);

    /* Get the List of Users who can grade submission for this assignment.
    *
    * @param assignmentReference -
    *        a reference to an assignment
    * @return the List (User) of users who can grade submission for this assignment.
    */
    public List<User> allowGradeAssignmentUsers(String assignmentReference);

    /**
     * Get the list of users who can add submission for at lease one assignment within the context
     *
     * @param context the context string
     * @return the list of user (ids)
     */
    public List<String> allowAddAnySubmissionUsers(String context);

    /**
     * Get the List of Users who can add assignment
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return the List (User) of users who can add assignment
     */
    public List<User> allowAddAssignmentUsers(String context);

    /**
     * Check permissions for reading a Submission.
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to get the AssignmentSubmission, false if not.
     */
    public boolean allowGetSubmission(String submissionReference);

    /**
     * Check permissions for updating Submission.
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to update the AssignmentSubmission, false if not.
     */
    public boolean allowUpdateSubmission(String submissionReference);

    /**
     * Check permissions for remove Submission
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to remove the AssignmentSubmission, false if not.
     */
    public boolean allowRemoveSubmission(String submissionReference);

    boolean allowReviewService(Site site);

    /**
     * Check permissions for grading Submission
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to grade the AssignmentSubmission, false if not.
     */
    public boolean allowGradeSubmission(String submissionReference);

    /**
     * Creates and adds a new Assignment to the service.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return AssignmentEdit The new Assignment object.
     * @throws IdInvalidException  if the id contains prohibited characers.
     * @throws IdUsedException     if the id is already used in the service.
     * @throws PermissionException if current User does not have permission to do this.
     */
    public Assignment addAssignment(String context) throws PermissionException;

    /**
     * Add a new assignment to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
     *
     * @param el The XML DOM Element defining the assignment.
     * @return A locked AssignmentEdit object (reserving the id).
     * @throws IdInvalidException  if the assignment id is invalid.
     * @throws IdUsedException     if the assignment id is already used.
     * @throws PermissionException if the current user does not have permission to add an assignnment.
     */
    public Assignment mergeAssignment(Element el) throws IdInvalidException, IdUsedException, PermissionException;

    /**
     * Creates and adds a new Assignment to the service which is a copy of an existing Assignment.
     *
     * @param context The context for the new assignment
     * @param assignmentId The id of the Assignment to be duplicated.
     * @return The new Assignment object, or null if the original Assignment does not exist.
     * @throws PermissionException if current User does not have permission to do this.
     */
    public Assignment addDuplicateAssignment(String context, String assignmentId) throws IdInvalidException, PermissionException, IdUsedException, IdUnusedException;

    /**
     * Delete this Assignment
     *
     * @param assignment - The Assignment to delete.
     * @throws PermissionException if current User does not have permission to do this.
     */
    public void deleteAssignment(Assignment assignment) throws PermissionException;

    /**
     * Softly delete this Assignment
     *
     * @param assignment - The Assignment to softly delete.
     * @throws PermissionException if current User does not have permission to do this.
     */
    public void softDeleteAssignment(Assignment assignment) throws PermissionException;

    /**
     * Softly delete this Assignment and remove all references to it.
     *
     * @param assignment - The Assignment to softly delete.
     * @throws PermissionException if current User does not have permission to do this.
     */
    public void deleteAssignmentAndAllReferences(Assignment assignment) throws PermissionException;

    /**
     * Adds an AssignmentSubmission
     *
     * @param assignmentId The assignment id
     * @param submitter    The submitter id
     * @return The new AssignmentSubmission.
     * @throws IdInvalidException  if the submission id is invalid.
     * @throws IdUsedException     if the submission id is already used.
     * @throws PermissionException if the current User does not have permission to do this.
     */
    public AssignmentSubmission addSubmission(String assignmentId, String submitter) throws PermissionException;

    /**
     * Add a new AssignmentSubmission to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
     *
     * @param el The XML DOM Element defining the submission.
     * @return A locked AssignmentSubmission object (reserving the id).
     * @throws IdInvalidException  if the submission id is invalid.
     * @throws IdUsedException     if the submission id is already used.
     * @throws PermissionException if the current user does not have permission to add a submission.
     */
    public AssignmentSubmission mergeSubmission(Element el) throws IdInvalidException, IdUsedException, PermissionException;

    /**
     * Removes an AssignmentSubmission and all references to it
     *
     * @param submission -
     *                   the AssignmentSubmission to remove.
     * @throws PermissionException if current User does not have permission to do this.
     */
    public void removeSubmission(AssignmentSubmission submission) throws PermissionException;

    /**
     * @param assignment
     * @throws PermissionException
     */
    public void updateAssignment(Assignment assignment) throws PermissionException;

    /**
     * @param submission
     * @throws PermissionException
     */
    public void updateSubmission(AssignmentSubmission submission) throws PermissionException;

    /**
     * @param reference
     * @return
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public Assignment getAssignment(Reference reference) throws IdUnusedException, PermissionException;

    /**
     * Access the Assignment with the specified id.
     *
     * @param assignmentId -
     *                     The id of the Assignment.
     * @return The Assignment corresponding to the id, or null if it does not exist.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to read this.
     */
    public Assignment getAssignment(String assignmentId) throws IdUnusedException, PermissionException;

    /**
     * Retrieves the current status of the specified assignment.
     *
     * @return
     * @throws IdUnusedException
     * @throws PermissionException
     */
    public AssignmentConstants.Status getAssignmentCannonicalStatus(String assignmentId) throws IdUnusedException, PermissionException;

    /**
     * Access the AssignmentSubmission with the specified id.
     *
     * @param submissionId -
     *                     The id of the AssignmentSubmission.
     * @return The AssignmentSubmission corresponding to the id, or null if it does not exist.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to read this.
     */
    public AssignmentSubmission getSubmission(String submissionId) throws IdUnusedException, PermissionException;

    /**
     * Access all the Assignemnts that are not deleted and self-drafted ones
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return List All the Assignments will be listed
     */
    public Collection<Assignment> getAssignmentsForContext(String context);

    /**
     * Access all the Assignments that are deleted
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return List All the deleted assignments will be listed
     */
    public Collection<Assignment> getDeletedAssignmentsForContext(String context);

    /**
     * Retrieve a map of Assignments to a list of User IDs of those who
     * may submit each assignment. This map is filtered to only those
     * assignments that can be viewed by the current user.
     *
     * @param context the Site ID to search
     * @return All submittable Assignments in the site mapped to the User IDs of those who can submit them.
     */
    public Map<Assignment, List<String>> getSubmittableAssignmentsForContext(String context);

    /**
     * Access a User's AssignmentSubmission to a particular Assignment.
     *
     * @param assignmentId -
     *                     The id of the assignment.
     * @param person       -
     *                     The User who's Submission you would like.
     * @return AssignmentSubmission The user's submission for that Assignment, or null if one does not exist.
     */
    public AssignmentSubmission getSubmission(String assignmentId, User person) throws PermissionException;

    /**
     * Access a User or Group's AssignmentSubmission to a particular Assignment.
     *
     * @param assignmentReference -
     *                            The id of the assignment.
     * @param submitterId         -
     *                            The string id of the person or group who's Submission you would like.
     * @return AssignmentSubmission The user's submission for that Assignment, or null if one does not exist.
     */
    public AssignmentSubmission getSubmission(String assignmentReference, String submitterId) throws PermissionException;

    /**
     * Access a User's AssignmentSubmission inside a list of AssignmentSubmission object.
     *
     * @param -      submissions
     *               The list of submissions
     * @param person -
     *               The User who's Submission you would like.
     * @return AssignmentSubmission The user's submission for that Assignment, or null if one does not exist.
     */
    @Deprecated
    public AssignmentSubmission getSubmission(List<AssignmentSubmission> submissions, User person);

    /**
     * Get the submissions for an assignment.
     *
     * @param assignment -
     *                   the Assignment who's submissions you would like.
     * @return List over all the submissions for an Assignment.
     */
    public Set<AssignmentSubmission> getSubmissions(Assignment assignment);

    /**
     * @param assignmentId
     * @return
     */
    public String getAssignmentStatus(String assignmentId);

    /**
     * @param submissionId
     * @return
     */
    public String getSubmissionStatus(String submissionId);

    /**
     * Return a sorted list of users representing a group.
     */
    public List<User> getSortedGroupUsers(Group g);

    /**
     * Count the number of submissions for a given assignment.
     *
     * @param assignmentRef the assignment reference of the submissions to count.
     * @param graded count the number of submissions which have been submitted and are graded
     *               respectively graded is true and ungraded is false or null for both.
     * @return int count of submissions for the specified assignment.
     */
    public int countSubmissions(String assignmentRef, Boolean graded);

    /**
     * Access the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
     *
     * @param ref The reference, either to a specific assignment, or just to an assignment context.
     * @return The grades spreadsheet bytes.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to access this.
     */
    public byte[] getGradesSpreadsheet(String ref) throws IdUnusedException, PermissionException;

    /**
     * Access the submissions zip for the assignment reference.
     *
     * @param ref The assignment reference.
     * @param out The outputStream to stream the zip file into
     * @return The submissions zip bytes.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to access this.
     */
    public void getSubmissionsZip(OutputStream out, String ref, String queryString) throws IdUnusedException, PermissionException;

    /**
     * Access the internal reference which can be used to assess security clearance.
     *
     * @param id The assignment id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    public String assignmentReference(String context, String id);

    /**
     * Access the internal reference which can be used to assess security clearance.
     * This will make a database call, so might not be the best choice in every situation.
     * But, if you don't have the context (site id) and need the reference, it's the only way to get it.
     *
     * @param id The assignment id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    public String assignmentReference(String id);

    /**
     * Access the internal reference which can be used to access the resource from within the system.
     *
     * @param id The submission id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    public String submissionReference(String context, String id, String assignmentId);

    /**
     * Whether a specific user can submit
     * @param context
     * @param a
     * @param userId
     * @return
     */
    public boolean canSubmit(String context, Assignment a, String userId);

    /**
     * Whether the current user can submit
     * @param context
     * @param a
     * @return
     */
    public boolean canSubmit(String context, Assignment a);


    /**
     * @param searchFilterOnly
     * @param allOrOneGroup
     * @param searchString
     * @param aRef
     * @param contextString
     * @return
     */
    public Collection<Group> getSubmitterGroupList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString);

    /**
     * Allow that the instructor can submit an assignment on behalf of student
     */
    public boolean getAllowSubmitByInstructor();

    /**
     * get appropriate submitter id list with group choice and site id
     *
     * @param searchFilterOnly If true, return only those ids that matches the group filter and search criteria
     * @param allOrOneGroup    "all" or specific group reference
     * @param aRef             Assignment Reference
     * @param search           The search string
     * @param contextString    Site id
     * @return
     */
    public List<String> getSubmitterIdList(String searchFilterOnly, String allOrOneGroup, String search, String aRef, String contextString);

    /**
     * Alternative to getSubmittedIdList which returns full user and submissions, since submitterIdList retrieves them anyway
     *
     * @param searchFilterOnly If true, return only those ids that matches the group filter and search criteria
     * @param allOrOneGroup    "all" or specific group reference
     * @param aRef             Assignment Reference
     * @param search           The search string
     * @param contextString    Site id
     * @return
     */
    public Map<User, AssignmentSubmission> getSubmitterMap(String searchFilterOnly, String allOrOneGroup, String search, String aRef, String contextString);

    /**
     * @param accentedString
     * @return
     */
    public String escapeInvalidCharsEntry(String accentedString);

    /**
     * If the assignment uses anonymous grading returns true, else false
     *
     * Params: AssignmentSubmission s
     */
    public boolean assignmentUsesAnonymousGrading(Assignment a);

    /**
     * Get Scale Factor from the property assignment.grading.decimals
     *
     * @return
     */
    public Integer getScaleFactor();

    /**
     * Get a link directly into an assignment itself, supplying the permissions to use when
     * generating the link. Depending on your status, you get a different view on the assignment.
     *
     * @param context               The site id
     * @param assignmentId          The assignment id
     * @param allowReadAssignment   Is the curent user allowed to read?
     * @param allowAddAssignment    Is the curent user allowed to add assignments?
     * @param allowSubmitAssignment Is the curent user allowed to submit assignments?
     * @return The url as a String
     */
    public String getDeepLinkWithPermissions(String context, String assignmentId, boolean allowReadAssignment
            , boolean allowAddAssignment, boolean allowSubmitAssignment) throws Exception;

    /**
     * Get a link directly into an assignment itself. Depending on your status, you
     * get a different view on the assignment.
     *
     * @param context      The site id
     * @param assignmentId The assignment id
     * @return The url as a String
     */
    public String getDeepLink(String context, String assignmentId) throws Exception;

    /**
     * get csv separator for exporting to CSV. It can be a comma or point configured through
     * csv.separator sakai property
     *
     * @return
     */
    public String getCsvSeparator();

    /**
     * @param assignment
     * @return
     */
    public String getXmlAssignment(Assignment assignment);

    /**
     * @param assignmentId
     * @param userId
     * @return
     */
    public String getGradeForUserInGradeBook(String assignmentId, String userId);

    /**
     * @param grade
     * @param typeOfGrade
     * @param scaleFactor
     * @return
     */
    public String getGradeDisplay(String grade, Assignment.GradeType typeOfGrade, Integer scaleFactor);

    /**
     * @param factor
     * @param maxGradePoint
     * @return
     */
    public String getMaxPointGradeDisplay(int factor, int maxGradePoint);

    /**
     * @param submission
     * @return
     */
    public Optional<AssignmentSubmissionSubmitter> getSubmissionSubmittee(AssignmentSubmission submission);

    /**
     * @param submission
     * @return
     */
    public Collection<User> getSubmissionSubmittersAsUsers(AssignmentSubmission submission);

    /**
     * peer assessment is set for this assignment and the current time
     * falls between the assignment close time and the peer asseessment period time
     *
     * @return
     */
    public boolean isPeerAssessmentOpen(Assignment assignment);

    /**
     * peer assessment is set for this assignment but the close time hasn't passed
     *
     * @return
     */
    public boolean isPeerAssessmentPending(Assignment assignment);

    /**
     * peer assessment is set for this assignment but the current time is passed
     * the peer assessment period
     *
     * @return
     */
    public boolean isPeerAssessmentClosed(Assignment assignment);

    /**
     * @param assignment
     */
    public void resetAssignment(Assignment assignment);

    /**
     * @param submissionId
     */
    public void postReviewableSubmissionAttachments(AssignmentSubmission submission);

    /**
    * This will return the internationalized title of the tool.
    * This is used when creating a new gradebook item.
    */
    public String getToolTitle();

    String getUsersLocalDateTimeString(Instant date);

    public List<ContentReviewResult> getContentReviewResults(AssignmentSubmission submission);
}
