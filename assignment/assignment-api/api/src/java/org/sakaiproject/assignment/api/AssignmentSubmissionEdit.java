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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.api;

import java.util.List;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * AssignmentSubmissionEdit is an interface for the Sakai assignments module. It represents editable student submissions for assignments.
 * </p>
 */
public interface AssignmentSubmissionEdit extends AssignmentSubmission, Edit
{
	/**
	 * Set the AssignmentSubmissions's context at the time of creation.
	 * 
	 * @param context -
	 *        The context string.
	 */
	public void setContext(String context);

	/**
	 * Set the Assignment for this Submission
	 * 
	 * @param assignment -
	 *        the Assignment
	 */
	public void setAssignment(Assignment assignment);

	/**
	 * Add a User to the submitters list.
	 * 
	 * @param submitter -
	 *        the User to add.
	 */
	public void addSubmitter(User submitter);

	/**
	 * Remove an User from the submitter list
	 * 
	 * @param submitter -
	 *        the User to remove.
	 */
	public void removeSubmitter(User submitter);

	/**
	 * Remove all user from the submitter list
	 */
	public void clearSubmitters();

	/**
	 * Set whether this is a final submission.
	 * 
	 * @param submitted -
	 *        True if a final submission, false if still a draft.
	 */
	public void setSubmitted(boolean submitted);

	/**
	 * Set the time at which this response was submitted; setting it to null signifies the response is unsubmitted.
	 * 
	 * @param timeSubmitted -
	 *        Time of submission.
	 */
	public void setTimeSubmitted(Time timeSubmitted);

	/**
	 * Text submitted in response to the Assignment.
	 * 
	 * @param submissionText -
	 *        The text of the submission.
	 */
	public void setSubmittedText(String submissionText);

	/**
	 * Add an attachment to the list of submitted attachments.
	 * 
	 * @param attachment -
	 *        The Reference object pointing to the attachment.
	 */
	public void addSubmittedAttachment(Reference attachment);

	/**
	 * Remove an attachment from the list of submitted attachments
	 * 
	 * @param attachment -
	 *        The Reference object pointing to the attachment.
	 */
	public void removeSubmittedAttachment(Reference attachment);

	/**
	 * Remove all submitted attachments.
	 */
	public void clearSubmittedAttachments();

	/**
	 * Set the general comments by the grader.
	 * 
	 * @param comment -
	 *        the text of the grader's comments; may be null.
	 */
	public void setFeedbackComment(String comment);

	/**
	 * Set the text part of the instructors feedback; usually an annotated copy of the submittedText
	 * 
	 * @param feedback -
	 *        The text of the grader's feedback.
	 */
	public void setFeedbackText(String feedback);

	/**
	 * Add an attachment to the list of feedback attachments.
	 * 
	 * @param attachment -
	 *        The Resource object pointing to the attachment.
	 */
	public void addFeedbackAttachment(Reference attachment);

	/**
	 * Remove an attachment from the list of feedback attachments.
	 * 
	 * @param attachment -
	 *        The Resource pointing to the attachment to remove.
	 */
	public void removeFeedbackAttachment(Reference attachment);

	/**
	 * Remove all feedback attachments.
	 */
	public void clearFeedbackAttachments();

	/**
	 * Set whether this Submission was rejected by the grader.
	 * 
	 * @param returned -
	 *        true if this response was rejected by the grader, false otherwise.
	 */
	public void setReturned(boolean returned);

	/**
	 * Set whether this Submission has been graded.
	 * 
	 * @param graded -
	 *        true if the submission has been graded, false otherwise.
	 */
	public void setGraded(boolean graded);

	/**
	 * Set the review Score for this assignment
	 * @param score
	 */
	public void setReviewScore(int score);
	
	/**
	 * Set the URL of the Review Report
	 * @param url
	 */
	public void setReviewIconUrl(String url);
	
	/**
	 * Set the content review status
	 * @param status
	 */
	public void setReviewStatus(String status);


    /**
     *
     * @param error
     */
    public void setReviewError(String error);

	/**
	 * Set whether the grade has been released.
	 * 
	 * @param released -
	 *        True if the Submissions's grade has been released, false otherwise.
	 */
	public void setGradeReleased(boolean released);

	/**
	 * Sets the grade for the Submisssion.
	 * 
	 * @param grade -
	 *        The Submission's grade.
	 */
	public void setGrade(String grade);

	/**
	 * Set the time at which the graded Submission was returned; setting it to null means it is not yet graded.
	 * 
	 * @param timeReturned -
	 *        The time at which the graded Submission was returned.
	 */
	public void setTimeReturned(Time timeReturned);

	/**
	 * Set the checked status of the honor pledge flag.
	 * 
	 * @param honorPledgeFlag -
	 *        True if the honor pledge is checked, false otherwise.
	 */
	public void setHonorPledgeFlag(boolean honorPledgeFlag);

	/**
	 * Set the time last modified.
	 * 
	 * @param lastmod -
	 *        The Time at which the Submission was last modified.
	 */
	public void setTimeLastModified(Time lastmod);
	
	
	
	/**
	 * Post attachments to the content review service
	 * @param attachments
	 */
	public void postAttachment(List attachments);
}
