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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.timesheet.api.TimeSheetEntry;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * AssignmentService is the service that handles assignments.
 * </p>
 * <p>
 * Each AssignmentTransferBean has an associated AssignmentContent (an AssignmentContent can belong to more the one Assignment) and a list of AssignmentSubmission (the student responses to the Assignment).
 * </p>
 */

public interface AssignmentService extends EntityProducer {

    Entity createAssignmentEntity(String assignmentId);

    /**
     * Check permissions for receiving assignment submission notification email
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to receive the email, false if not.
     */
    boolean allowReceiveSubmissionNotification(String context);

    /**
     * Get the List of Users who can add assignment
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return the List (User) of users who are allowed to receive the email, false if not.
     */
    List<User> allowReceiveSubmissionNotificationUsers(String context);

    /**
     * @param context
     * @return
     */
    Collection<Group> getGroupsAllowRemoveAssignment(String context);

    /**
     * Check permissions for adding an Assignment.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to add an Assignment, false if not.
     */
    boolean allowAddAssignment(String context);

    /**
     * Check permissions for adding an Assignment.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @param user -
     *             The user for which the permission will be checked
     * @return True if the provided User is allowed to add an Assignment, false if not.
     */
    boolean allowAddAssignment(String context, String userId);

    /**
     * Check permissions for updating an AssignmentTransferBean based on context.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to update assignments, false if not.
     */
    boolean allowUpdateAssignmentInContext(String context);

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
    boolean allowRemoveAssignmentInContext(String context);

    /**
     * Check permissions for all.groups.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed all.groups, false if not.
     */
    boolean allowAllGroups(String context);

    /**
     * Check permissions for reading an Assignment.
     *
     * @param assignmentReference -
     *                            The Assignment's reference.
     * @return True if the current User is allowed to get the Assignment, false if not.
     */
    boolean allowGetAssignment(String assignmentReference);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has add assignment permissions in.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return The Collection (Group) of groups defined for the context of this site that the end user has add assignment permissions in, empty if none.
     */
    Collection<Group> getGroupsAllowAddAssignment(String context);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has add assignment permissions in.
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @param userId -
     *               The user for which the permission will be checked
     * @return The Collection (Group) of groups defined for the context of this site that the end user has add assignment permissions in, empty if none.
     */
    Collection<Group> getGroupsAllowAddAssignment(String context, String userId);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has update assignment permissions in.
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return The Collection (Group) of groups defined for the context of this site that the end user has update assignment permissions in, empty if none.
     */
    Collection<Group> getGroupsAllowUpdateAssignment(String context);

    /**
     * Get the collection of Groups defined for the context of this site that the end user has grade assignment permissions in.
     *
     * @return The Collection (Group) of groups defined for the context of this site that the end user has grade assignment permissions in, empty if none.
     */
    Collection<Group> getGroupsAllowGradeAssignment(String assignmentReference);

    /**
     * Check permissions for updating an Assignment.
     *
     * @param assignmentReference -
     *                            The Assignment's reference.
     * @return True if the current User is allowed to update the Assignment, false if not.
     */
    boolean allowUpdateAssignment(String assignmentReference);

    /**
     * Check permissions for removing an Assignment.
     *
     * @param assignmentReference -
     *                            The Assignment's reference.
     * @return True if the current User is allowed to remove the Assignment, false if not.
     */
    boolean allowRemoveAssignment(String assignmentReference);

    /**
     * Check permissions for add AssignmentSubmission
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return True if the current User is allowed to add an AssignmentSubmission, false if not.
     */
    boolean allowAddSubmission(String context);

    /**
     * @param assignment - An AssignmentTransferBean object. Needed for the groups to be checked.
     * @return
     */
    boolean allowAddSubmissionCheckGroups(String assignmentId);

    /**
     * Get the List of Users who can addSubmission() for this assignment.
     *
     * @param assignmentReference -
     *                            a reference to an assignment
     * @return the List (User) of users who can addSubmission() for this assignment.
     */
    List<User> allowAddSubmissionUsers(String assignmentReference);

    /* Get the List of Users who can grade submission for this assignment.
    *
    * @param assignmentReference -
    *        a reference to an assignment
    * @return the List (User) of users who can grade submission for this assignment.
    */
    List<User> allowGradeAssignmentUsers(String assignmentReference);

    /**
     * Get the list of users who can add submission for at lease one assignment within the context
     *
     * @param context the context string
     * @return the list of user (ids)
     */
    List<String> allowAddAnySubmissionUsers(String context);

    /**
     * Get the List of Users who can add assignment
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return the List (User) of users who can add assignment
     */
    List<User> allowAddAssignmentUsers(String context);

    /**
     * Check permissions for reading a Submission.
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to get the AssignmentSubmission, false if not.
     */
    boolean allowGetSubmission(String submissionReference);

    /**
     * Check permissions for updating Submission.
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to update the AssignmentSubmission, false if not.
     */
    boolean allowUpdateSubmission(String submissionReference);

    /**
     * Check permissions for remove Submission
     *
     * @param submissionReference -
     *                            The Submission's reference.
     * @return True if the current User is allowed to remove the AssignmentSubmission, false if not.
     */
    boolean allowRemoveSubmission(String submissionReference);

    boolean allowReviewService(Site site);

    /**
     * Check permissions for grading submissions on an assignment
     *
     * @param assignmentReference The assignment's reference.
     * @return true if the current user is allowed to grade submissions for the assignment.
     */
    boolean allowGradeSubmission(String assignmentReference);

    /**
     * Creates and adds a new AssignmentTransferBean to the service.
     *
     * @param context The site id for this assignment
     * @return AssignmentTransferBean The new AssignmentTransferBean object, ready for editing.
     * @throws IdInvalidException  if the id contains prohibited characers.
     * @throws IdUsedException     if the id is already used in the service.
     * @throws PermissionException if current User does not have permission to do this.
     */
    AssignmentTransferBean addAssignment(String context) throws PermissionException;

    /**
     * Creates and adds a new AssignmentTransferBean to the service which is a copy of an existing Assignment.
     *
     * @param context The context for the new assignment
     * @param assignmentId The id of the AssignmentTransferBean to be duplicated.
     * @return The new AssignmentTransferBean object, or null if the original AssignmentTransferBean does not exist.
     * @throws PermissionException if current User does not have permission to do this.
     */
    AssignmentTransferBean addDuplicateAssignment(String context, String assignmentId) throws IdInvalidException, PermissionException, IdUsedException, IdUnusedException;

    /**
     * Delete this Assignment
     *
     * @param assignment - The AssignmentTransferBean to delete.
     * @throws PermissionException if current User does not have permission to do this.
     */
    void deleteAssignment(AssignmentTransferBean assignment) throws PermissionException;

    /**
     * Softly delete an assignment
     *
     * @param assignmentId - The assignment to softly delete.
     * @throws PermissionException if current User does not have permission to do this.
     */
    void softDeleteAssignment(String assignmentId) throws PermissionException;

    /**
     * Softly delete this AssignmentTransferBean and remove all references to it.
     *
     * @param assignment - The AssignmentTransferBean to softly delete.
     * @throws PermissionException if current User does not have permission to do this.
     */
    void deleteAssignmentAndAllReferences(AssignmentTransferBean assignment) throws PermissionException;

    /**
     * Adds a new submission to an Assignment
     *
     * @param assignmentId The assignment's id the submission will be added to
     * @param submitter    The submitter's id of who is submitting this submission, can also be a group id for a group submission
     * @return new {@link AssignmentSubmission}, or null if the submission could not be created
     * @throws PermissionException if the current User does not have permission to do this.
     */
    SubmissionTransferBean addSubmission(String assignmentId, String submitter) throws PermissionException;

    /**
     * Removes an AssignmentSubmission and all references to it
     *
     * @param submission -
     *                   the AssignmentSubmission to remove.
     * @throws PermissionException if current User does not have permission to do this.
     */
    void removeSubmission(String submissionId) throws PermissionException;

    /**
     * @param assignment
     * @return List<String> Any alerts
     * @throws PermissionException
     */
    List<String> updateAssignment(AssignmentTransferBean assignment) throws PermissionException;

    /**
     * @param submission
     * @throws PermissionException
     */
    void updateSubmission(SubmissionTransferBean submission) throws PermissionException;

    /**
     * @param reference
     * @return
     * @throws IdUnusedException
     * @throws PermissionException
     */
    AssignmentTransferBean getAssignment(Reference reference) throws IdUnusedException, PermissionException;

    /**
     * Access the AssignmentTransferBean with the specified id.
     *
     * @param assignmentId -
     *                     The id of the Assignment.
     * @return The AssignmentTransferBean corresponding to the id, or null if it does not exist.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to read this.
     */
    AssignmentTransferBean getAssignment(String assignmentId) throws IdUnusedException, PermissionException;

    Optional<AssignmentTransferBean> getAssignmentForSubmission(String submissionId);

    /**
     * Retrieves the current status of the specified assignment.
     *
     * @return
     * @throws IdUnusedException
     * @throws PermissionException
     */
    AssignmentConstants.Status getAssignmentCannonicalStatus(String assignmentId) throws IdUnusedException, PermissionException;

    /**
     * Access the AssignmentSubmission with the specified id.
     *
     * @param submissionId -
     *                     The id of the AssignmentSubmission.
     * @return The AssignmentSubmission corresponding to the id, or null if it does not exist.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to read this.
     */
    SubmissionTransferBean getSubmission(String submissionId) throws IdUnusedException, PermissionException;

    /**
     * Access all the Assignemnts that are not deleted and self-drafted ones
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return List All the Assignments will be listed
     */
    Collection<AssignmentTransferBean> getAssignmentsForContext(String context);

    /**
     * Access all the Assignments that are deleted
     *
     * @param context -
     *                Describes the portlet context - generated with DefaultId.getChannel().
     * @return List All the deleted assignments will be listed
     */
    Collection<AssignmentTransferBean> getDeletedAssignmentsForContext(String context);

    /**
     * Retrieve a map of Assignments to a list of User IDs of those who
     * may submit each assignment. This map is filtered to only those
     * assignments that can be viewed by the current user.
     *
     * @param context the Site ID to search
     * @return All submittable Assignments in the site mapped to the User IDs of those who can submit them.
     */
    Map<AssignmentTransferBean, List<String>> getSubmittableAssignmentsForContext(String context);

    /**
     * Access a User's AssignmentSubmission to a particular Assignment.
     *
     * @param assignmentId -
     *                     The id of the assignment.
     * @param person       -
     *                     The User who's Submission you would like.
     * @return AssignmentSubmission The user's submission for that Assignment, or null if one does not exist.
     */
    SubmissionTransferBean getSubmission(String assignmentId, User person) throws PermissionException;

    /**
     * Access a User or Group's AssignmentSubmission to a particular Assignment.
     *
     * @param assignmentReference -
     *                            The id of the assignment.
     * @param submitterId         -
     *                            The string id of the person or group who's Submission you would like.
     * @return AssignmentSubmission The user's submission for that Assignment, or null if one does not exist.
     */
    SubmissionTransferBean getSubmission(String assignmentReference, String submitterId) throws PermissionException;

    /**
     * Get the submissions for an assignment.
     *
     * @param assignment -
     *                   the AssignmentTransferBean who's submissions you would like.
     * @return List over all the submissions for an Assignment.
     */
    Set<SubmissionTransferBean> getSubmissions(String assignmentId);

    /**
     * @param assignmentId
     * @return
     */
    String getAssignmentStatus(String assignmentId);

    /**
     * @param submissionId
     * @return
     */
    String getSubmissionStatus(String submissionId, boolean returnFormattedDate);

    /**
     * @param submissionId
     * @return
     */
    AssignmentConstants.SubmissionStatus getSubmissionCanonicalStatus(String submissionId, boolean canGrade);

    /**
     * Return a sorted list of users representing a group.
     */
    List<User> getSortedGroupUsers(Group g);

    /**
     * Count the number of submissions for a given assignment.
     *
     * @param assignmentRef the assignment reference of the submissions to count.
     * @param graded count the number of submissions which have been submitted and are graded
     *               respectively graded is true and ungraded is false or null for both.
     * @return int count of submissions for the specified assignment.
     */
    int countSubmissions(String assignmentRef, Boolean graded);

    /**
     * Access the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
     *
     * @param ref The reference, either to a specific assignment, or just to an assignment context.
     * @return The grades spreadsheet bytes.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to access this.
     */
    byte[] getGradesSpreadsheet(String ref) throws IdUnusedException, PermissionException;

    /**
     * Access the submissions zip for the assignment reference.
     *
     * @param ref The assignment reference.
     * @param out The outputStream to stream the zip file into
     * @return The submissions zip bytes.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to access this.
     */
    void getSubmissionsZip(OutputStream out, String ref, String queryString) throws IdUnusedException, PermissionException;

    boolean permissionCheck(String permission, String resource, String user);

    boolean permissionCheckInGroups(String permission, String assignmentId, String user);
    /**
     * Access the internal reference which can be used to assess security clearance.
     *
     * @param id The assignment id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    String assignmentReference(String context, String id);

    /**
     * Access the internal reference which can be used to assess security clearance.
     * This will make a database call, so might not be the best choice in every situation.
     * But, if you don't have the context (site id) and need the reference, it's the only way to get it.
     *
     * @param id The assignment id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    String assignmentReference(String id);

    /**
     * Access the internal reference which can be used to access the resource from within the system.
     *
     * @param id The submission id string.
     * @return The the internal reference which can be used to access the resource from within the system.
     */
    String submissionReference(String context, String id, String assignmentId);

    /**
     * Whether a specific user can submit to this assignment thereby creating a submission
     * <p>
     * Of particular importance is whether <b>userId</b> is <b>blank</b> or <b>not</b>,
     * a blank userId will perform all security checks against the current user
     * while a non blank userId will perform all security checks against the specified user.
     *
     * @param assignment the AssignmentTransferBean to check for allowing to submit to
     * @param userId the specified user is checked vs the current user
     * @return true if the specified user or the current user can submit to the assignment, otherwise false
     */
    boolean canSubmit(AssignmentTransferBean assignment, String userId);

    /**
     * Whether the current user can submit to this assignment thereby creating a submission
     *
     * @param assignment the AssignmentTransferBean to check for allowing to submit to
     * @return true if the current user can submit to the assignment, otherwise false
     */
    boolean canSubmit(AssignmentTransferBean assignment);

    /**
     * @param searchFilterOnly
     * @param allOrOneGroup
     * @param searchString
     * @param aRef
     * @param contextString
     * @return
     */
    Collection<Group> getSubmitterGroupList(String searchFilterOnly, String allOrOneGroup, String searchString, String aRef, String contextString);

    /**
     * Allow that the instructor can submit an assignment on behalf of student
     */
    boolean getAllowSubmitByInstructor();

    /**
     * get appropriate submitter id list with group choice and site id
     *
     * @param searchFilterOnly If true, return only those ids that matches the group filter and search criteria
     * @param allOrOneGroup    "all" or specific group reference
     * @param aRef             AssignmentTransferBean Reference
     * @param search           The search string
     * @param contextString    Site id
     * @return
     */
    List<String> getSubmitterIdList(String searchFilterOnly, String allOrOneGroup, String search, String aRef, String contextString);

    /**
     * Alternative to getSubmittedIdList which returns full user and submissions, since submitterIdList retrieves them anyway
     *
     * @param searchFilterOnly If true, return only those ids that matches the group filter and search criteria
     * @param allOrOneGroup    "all" or specific group reference
     * @param aRef             AssignmentTransferBean Reference
     * @param search           The search string
     * @param contextString    Site id
     * @return
     */
    Map<User, SubmissionTransferBean> getSubmitterMap(String searchFilterOnly, String allOrOneGroup, String search, String aRef, String contextString);

    /**
     * Given an AssignmentTransferBean and a User, rationalize who the submitter should be taking into account the assignment configuration
     * Will check the assignments access and group configuration to determine the submitter id
     * @param assignment The assignment
     * @param userId The user id
     * @return the correct submitter id to use for creating a submission or null if one can't be determined
     */
    String getSubmitterIdForAssignment(AssignmentTransferBean assignment, String userId);

    /**
     * Retrieves the selected group users based on the given parameters.
     *
     * @param allOrOneGroup           Determines if all groups or a specific group should be considered.
     * @param contextString           The context string indicating the site title.
     * @param assignment              The assignment object for which the users are selected.
     * @param allowAddSubmissionUsers The list of users allowed to submit the assignment.
     * @return The list of selected group users based on the specified criteria.
     */
    List<User> getSelectedGroupUsers(String allOrOneGroup, String contextString, AssignmentTransferBean assignment, List<User> allowAddSubmissionUsers);
    
    /**
     * @param accentedString
     * @return
     */
    String escapeInvalidCharsEntry(String accentedString);

    /**
     * If the assignment uses anonymous grading returns true, else false
     *
     * Params: AssignmentSubmission s
     */
    boolean assignmentUsesAnonymousGrading(String assignmentId);

    /**
     * Get Scale Factor from the property assignment.grading.decimals
     *
     * @return
     */
    Integer getScaleFactor();

    /**
     * Get a link directly into an assignment itself, supplying the permissions to use when
     * generating the link. Depending on your status, you get a different view on the assignment.
     *
     * @param context               The site id
     * @param assignmentId          The assignment id
     * @param allowReadAssignmentTransferBean   Is the current user allowed to read?
     * @param allowAddAssignmentTransferBean    Is the current user allowed to add assignments?
     * @param allowSubmitAssignmentTransferBean Is the current user allowed to submit assignments?
     * @param allowGradeAssignmentTransferBean Is the current user allowed to grade assignments?
     * @return The url as a String
     */
    String getDeepLinkWithPermissions(String context, String assignmentId, boolean allowReadAssignment
            , boolean allowAddAssignment, boolean allowSubmitAssignment, boolean allowGradeAssignment) throws Exception;

    /**
     * Get a link directly into an assignment itself. Depending on your status, you
     * get a different view on the assignment.
     *
     * @param context      The site id
     * @param assignmentId The assignment id
     * @param userId       The user id
     * @return The url as a String
     */
    String getDeepLink(String context, String assignmentId, String userId) throws Exception;

    /**
     * get csv separator for exporting to CSV. It can be a comma or point configured through
     * csv.separator sakai property
     *
     * @return
     */
    String getCsvSeparator();

    /**
     * @param assignmentId
     * @return A String of xml content
     */
    String getXmlAssignment(String assignmentId);

    /**
     * Gets the effective grade for the submitter on this submission. If there is an
     * override, that is the grade returned. Otherwise, the main submission grade is
     * returned
     *
     * @param submissionId The id of the overall submission
     * @param submitter The individual submitter we're interested in
     */
    String getGradeForSubmitter(String submissionId, String submitter);

    /**
     * Returns true if this submitter has an overridden grade. This is useful as it avoids
     * bug prone comparisons of the submission grade and individual submitter grades
     *
     * @param submission The overall submission (must be on a group assignment)
     * @param submitter The individual submitter we're interested in
     * @return true if overridden, false if not a group assignment or not overridden.
     */
    boolean isGradeOverridden(String submissionId, String submitter);

    /**
     * @param grade
     * @param typeOfGrade
     * @param scaleFactor
     * @return The grade, formatted for display
     */
    String getGradeDisplay(String grade, Assignment.GradeType typeOfGrade, Integer scaleFactor);

    /**
     * @param factor
     * @param maxGradePoint
     * @return
     */
    String getMaxPointGradeDisplay(int factor, int maxGradePoint);

    /**
     * @param submission
     * @return
     */
    Optional<SubmitterTransferBean> getSubmissionSubmittee(String submissionId);

    /**
     * @param submission
     * @return
     */
    Collection<User> getSubmissionSubmittersAsUsers(String submissionId);

    /**
     * peer assessment is set for this assignment and the current time
     * falls between the assignment close time and the peer asseessment period time
     *
     * @return
     */
    boolean isPeerAssessmentOpen(String assignmentId);

    /**
     * peer assessment is set for this assignment but the close time hasn't passed
     *
     * @return
     */
    boolean isPeerAssessmentPending(String assignmentId);

    /**
     * peer assessment is set for this assignment but the current time is passed
     * the peer assessment period
     *
     * @return
     */
    boolean isPeerAssessmentClosed(String assignmentId);

    /**
     * @param assignment
     */
    void resetAssignment(String assignmentId);

    /**
     * @param submissionId
     */
    void postReviewableSubmissionAttachments(String submissionId);

    /**
     * This will return the assignment tool id.
     * This is used when creating a new gradebook item.
     */
    String getToolId();

    /**
     * This will return the reference removing from it the auxiliar prefix.
     * This is used when interacting with the ContentHostingService.
     */
    String removeReferencePrefix(String referenceId);

    String getUsersLocalDateTimeString(Instant date);

    String getUsersLocalDateTimeStringFromProperties(String date);

    List<ContentReviewResult> getContentReviewResults(String submissionId);

    List<ContentReviewResult> getSortedContentReviewResults(String submissionId);

    /**
     * Determines whether it is appropriate to display the content review results for a submission.
     * For instance, this will be false if the submission is a draft or if the user doesn't have
     * permission. Note: this doesn't check if content review is enabled in the site / if the
     * associated assignment has content review enabled; it is assumed that this has been handled by
     * the caller.
     *
     * @param submissionId
     * @return true if content review results for the given submission can be displayed.
     */
    boolean isContentReviewVisibleForSubmission(String submissionId);

    /**
     * Gets all attachments in the submission that are acceptable to the content review service
     */
    List<ContentResource> getAllAcceptableAttachments(String submissionId);

    /**
     * Get an assignment that is linked with a gradebook item
     * @param context the context (site id)
     * @param linkId the link id of the gradebook item, usually the gradebook item name or id
     * @return the matching assignment if found or empty if none
     * @throws IdUnusedException if the assignment doesn't exist
     * @throws PermissionException if the current user is not allowed to access the assignment
     */
    Optional<AssignmentTransferBean> getAssignmentForGradebookLink(String context, String linkId) throws IdUnusedException, PermissionException;

    Optional<org.sakaiproject.grading.api.Assignment> getGradingItemForAssignment(String gbItemId);

    /**
     * Returns a list of users that belong to multiple groups, if the user is considered a "student" in the group
     * by the standards of the Assignments tool.
     * @param siteId the site id
     * @param asnGroups the assignment groups to check membership in
     * @return list of users with multiple group memberships and the groups they belong to
     */
    List<MultiGroupRecord> checkAssignmentForUsersInMultipleGroups(String siteId, Collection<Group> asnGroups);

    /**
     * Returns a list of users in the submission group that also belong to other assignment groups, if the user is considered
     * a "student" in the group by the standards of the Assignments tool.
     * @param siteId the site id
     * @param submissionGroup the group the submission is from
     * @param asnGroups the assignment groups to check membership in
     * @return list of submission group users with multiple group memberships and the groups they belong to
     */
    List<MultiGroupRecord> checkSubmissionForUsersInMultipleGroups(String siteId, Group submissionGroup, Collection<Group> asnGroups);

    boolean isValidTimeSheetTime(String timeSheet);

    List<TimeSheetEntry> getTimeSheetEntries(String submissionId);
	
    void saveTimeSheetEntry(SubmitterTransferBean submissionSubmitter, TimeSheetEntry timeSheet) throws PermissionException;

    void deleteTimeSheetEntry(Long timeSheetId) throws PermissionException;

    String getTimeSpent(SubmissionTransferBean submission);

    /**
     * Returns true if the content review implementation successfully created the assignment
     * @param a
     * @param assignmentRef
     * @param openTime
     * @param dueTime
     * @param closeTime
     * @return
     */
    String createContentReviewAssignment(AssignmentTransferBean a, String assignmentRef, Instant openTime, Instant dueTime, Instant closeTime);

    String getTotalTimeSheet(String submissionId);

    Integer timeToInt(String time);

    String intToTime(int time);

    boolean isTimeSheetEnabled(String siteId);

    /**
     * The the name of the content review service being used e.g. Turnitin
     * @return A String containing the name of the content review service
     */
    String getContentReviewServiceName();
    
    String getAssignmentModifier(String modifier);

    boolean allowAddTags(String context);

    /**
     * Sync up the integration with a grading item
     *
     * @param assignment AssignmentTransferBean object with all the data we need.
     * @return List<String> Alerts for display in the UI, perhaps
     */
    List<String> integrateGradebook(AssignmentTransferBean assignment);

    /**
     * Push grades from the assignment into the gradebook item specified by gbItemId
     *
     * @param assignment The assignment instance with the source grades.
     * @param gbItemId The Long id of the associated grading service item
     * @return A list of alert strings
     */
    List<String> updateGradesForGradingItem(AssignmentTransferBean assignment, Long gbItemId);

    /**
     * Update the gradebook grade on a single submission. If the assignment is a group assignment
     * this method will use the last submitter's grade as the grade, not the overall submission
     * grade. This covers the scenario of grade overrides in group assignments. If this is not a
     * group assignment, the overall submission grade is sent to the gradebook.
     *
     * @param assignment The AssignmentTransferBean object that has been submitted to.
     * @param submission The AssignmentSubmission object to send the grades from.
     * @param gbItemId The grading server item id. This is what we will be setting grades on.
     */
    void updateGradeForGradingItem(SubmissionTransferBean submission, Long gbItemId);

    /**
     * Zero the grades on a grading item. Iterates over all the submissions for the assignment, grabbing
     * all the submitters for those submissions and zeroing the grades for them
     *
     * @param assignment The AssignmentTransferBean object on which to iterate over the submissions
     * @param gbItemId The grading service item which we want to zero
     */
    void zeroGradesOnGradingItem(AssignmentTransferBean assignment, Long gbItemId);

    /**
     * Zero the grades on a grading item for one submission.
     *
     * @param assignment The AssignmentTransferBean object on which to iterate over the submissions
     * @param submission The AssignmentSubmission to pull the grades from
     * @param gbItemId The grading service item which we want to zero
     */
    void zeroGradeOnGradingItem(SubmissionTransferBean submission, Long gbItemId);

    /**
     * Zero the grades on a grading item. Iterates over all the submissions for the assignment, grabbing
     * all the submitters for those submissions and zeroing the grades for them
     *
     * @param assignment The AssignmentTransferBean object on which to iterate over the submissions
     * @param gbItemId The grading service item which we want to zero
     */
    void removeNonAssociatedExternalGradebookEntry(AssignmentTransferBean assignment, Long gbItemId);

    /**
     * Common grading routine plus specific operation to differentiate cases when saving, releasing or returning grade.
     *
     * @param submission The submission instance that we wish to grade.
     * @param gradingOption A GradingOption enum
     * @param options Stuff like the grade and feedback comments. This could be fed from a UI form.
     * @param alerts This can be populated with formatted alert strings which could then be displayed
     *                  to the user, or maybe logged.
     */
    void gradeSubmission(SubmissionTransferBean submission, GradingOption gradingOption, Map<String, Object> options, List<String> alerts);
}
