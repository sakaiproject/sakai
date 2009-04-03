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

import org.sakaiproject.entity.api.AttachmentContainerEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * AssignmentContentEdit is the an interface for the Sakai assignments module. It represents the editable part of the assignment content that is "unchanging" for different versions of the assignment.
 * </p>
 */
public interface AssignmentContentEdit extends AssignmentContent, AttachmentContainerEdit, Edit
{
	/**
	 * Set the title.
	 * 
	 * @param title -
	 *        The Assignment's title.
	 */
	public void setTitle(String title);

	/**
	 * Set the instructions for the Assignment.
	 * 
	 * @param instructions -
	 *        The Assignment's instructions.
	 */
	public void setInstructions(String instructions);

	/**
	 * Set the AssignmentContent's context at the time of creation.
	 * 
	 * @param context -
	 *        The context string.
	 */
	public void setContext(String context);

	/**
	 * Set the type of submission.
	 * 
	 * @param subType -
	 *        The type of submission.
	 */
	public void setTypeOfSubmission(int subType);

	/**
	 * Set the grade type.
	 * 
	 * @param gradeType -
	 *        The type of grade.
	 */
	public void setTypeOfGrade(int gradeType);

	/**
	 * Set the maximum grade for grade type = SCORE_GRADE_TYPE(3)
	 * 
	 * @param maxPoints -
	 *        The maximum grade score.
	 */
	public void setMaxGradePoint(int maxPoints);

	/**
	 * Set whether this project can be a group project.
	 * 
	 * @param groupProject -
	 *        True if this can be a group project, false otherwise.
	 */
	public void setGroupProject(boolean groupProject);

	/**
	 * Set whether group projects should be individually graded.
	 * 
	 * @param individGraded -
	 *        true if projects are individually graded, false if grades are given to the group.
	 */
	public void setIndividuallyGraded(boolean individGraded);

	/**
	 * Sets whether grades can be released once submissions are graded.
	 * 
	 * @param release -
	 *        true if grades can be released once submission are graded, false if they must be released manually.
	 */
	public void setReleaseGrades(boolean release);

	/**
	 * Set the Honor Pledge type; values are NONE and ENGINEERING_HONOR_PLEDGE.
	 * 
	 * @param pledgeType -
	 *        the Honor Pledge value.
	 */
	public void setHonorPledge(int pledgeType);

	/**
	 * Does this Assignment allow attachments?
	 * 
	 * @param allow -
	 *        true if the Assignment allows attachments, false otherwise?
	 */
	public void setAllowAttachments(boolean allow);

	/**
	 * Does this Assignment allow using the review service?
	 * 
	 * @param allow -
	 *        true if the Assignment allows review service, false otherwise?
	 */
	public void setAllowReviewService(boolean allow);
	
	/**
	 * Set whether this sssignment allow students to view review service reports?
	 * 
	 * @param allow -
	 *        true if the Assignment allows review service, false otherwise?
	 */
	public void setAllowStudentViewReport(boolean allow);
	
	/**
	 * Add an author to the author list.
	 * 
	 * @param author -
	 *        The User to add to the author list.
	 */
	public void addAuthor(User author);

	/**
	 * Remove an author from the author list.
	 * 
	 * @param author -
	 *        the User to remove from the author list.
	 */
	public void removeAuthor(User author);

	/**
	 * Set the time last modified.
	 * 
	 * @param lastmod -
	 *        The Time at which the Content was last modified.
	 */
	public void setTimeLastModified(Time lastmod);
}
