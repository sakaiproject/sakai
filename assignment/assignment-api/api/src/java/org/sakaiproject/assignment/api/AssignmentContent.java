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

import java.util.List;

import org.sakaiproject.entity.api.AttachmentContainer;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * AssignmentContent is the an interface for the Sakai assignments module. It represents the part of the assignment content that is "unchanging" for different versions of the assignment.
 * </p>
 */
public interface AssignmentContent extends Entity, AttachmentContainer
{
	/**
	 * Access the creator of this object.
	 * 
	 * @return String - the user id of the creator.
	 */
	public String getCreator();

	/**
	 * Access the title.
	 * 
	 * @return The AssignmentContent's title.
	 */
	public String getTitle();

	/**
	 * Access the context at the time of creation.
	 * 
	 * @return String - the context string.
	 */
	public String getContext();

	/**
	 * Access the instructions for the assignment
	 * 
	 * @return The Assignment's instructions.
	 */
	public String getInstructions();

	/**
	 * Access the time that this object was created.
	 * 
	 * @return The Time object representing the time of creation.
	 */
	public Time getTimeCreated();

	/**
	 * Access the time of last modificaiton.
	 * 
	 * @return The Time of last modification.
	 */
	public Time getTimeLastModified();

	/**
	 * Access the author of last modificaiton
	 * 
	 * @return the User
	 */
	public String getAuthorLastModified();

	/**
	 * Access the type of submission.
	 * 
	 * @return An integer representing the type of submission.
	 */
	public int getTypeOfSubmission();

	/**
	 * Access the grade type
	 * 
	 * @return The integer representing the type of grade.
	 */
	public int getTypeOfGrade();

    /**
     * Access a string describing the type of grade.
     * @deprecated Use getTypeOfGradeString() instead.
     * @param gradeType -
     *        The integer representing the type of grade.
     * @return Description of the type of grade.
     */
    public String getTypeOfGradeString(int gradeType);

	/**
	 * Access a string describing the type of grade.
	 * 
	 * @return Description of the type of grade.
	 */
	public String getTypeOfGradeString();

	/**
	 * Access a string describing the type of submission.
	 * @return Description of the type of submission.
	 */
	public String getTypeOfSubmissionString(); 
	/**
	 * Gets the maximum grade if grade type is SCORE_GRADE_TYPE(3)
	 * 
	 * @return int The maximum grade score, or zero if the grade type is not SCORE_GRADE_TYPE(3).
	 */
	public int getMaxGradePoint();
	
	/**
	 * 
	 * @return The number of decimal points to store the grade score 
	 */
	public int getFactor();

	/**
	 * Get the maximum grade for grade type = SCORE_GRADE_TYPE(3) Formated to show one decimal place
	 * 
	 * @return The maximum grade score.
	 */
	public String getMaxGradePointDisplay();

	/**
	 * Get whether this project can be a group project.
	 * 
	 * @return True if this can be a group project, false otherwise.
	 */
	public boolean getGroupProject();

	/**
	 * Access whether group projects should be individually graded.
	 * 
	 * @return true if projects are individually graded, false if grades are given to the group.
	 */
	public boolean individuallyGraded();

	/**
	 * Access whether grades can be released once submissions are graded.
	 * 
	 * @return True if grades can be released once submission are graded, false if they must be released manually.
	 */
	public boolean releaseGrades();

	/**
	 * Access the Honor Pledge type; values are NONE and ENGINEERING_HONOR_PLEDGE.
	 * 
	 * @return The type of pledge.
	 */
	public int getHonorPledge();

	/**
	 * Access whether this AssignmentContent allows attachments.
	 * 
	 * @return true if the AssignmentContent allows attachments, false otherwise.
	 */
	public boolean getAllowAttachments();
	
	
	
	/**
	 * Access whether this AssignmentContent allows review service.
	 * 
	 * @return true if the AssignmentContent allows review service, false otherwise.
	 */
	public boolean getAllowReviewService();

	/**
	 * Access whether this AssignmentContent allows students to view review service reports.
	 * 
	 * @return true if the AssignmentContent allows students to view review service reports, false otherwise.
	 */
	
	public boolean getAllowStudentViewReport();

	/**
	 * Access whether this AssignmentContent allows students to view review service grades.
	 * 
	 * @return true if the AssignmentContent allows students to view review service grades, false otherwise.
	 */
	
	public boolean getAllowStudentViewExternalGrade();

	/**
	 * Access the list of authors.
	 * 
	 * @return List of the author's user-ids.
	 */
	public List getAuthors();

	/**
	 * Access whether this AssignmentContent is in use by an Assignment.
	 * 
	 * @return boolean - Is this AssignmentContent used by an Assignment.
	 */
	public boolean inUse();
	
	public String getSubmitReviewRepo();

	public void setSubmitReviewRepo(String m_submitReviewRepo);

	public String getGenerateOriginalityReport();

	public void setGenerateOriginalityReport(String m_generateOriginalityReport);

	public boolean isCheckTurnitin();

	public void setCheckTurnitin(boolean m_checkTurnitin);

	public boolean isCheckInternet();

	public void setCheckInternet(boolean m_checkInternet);

	public boolean isCheckPublications();

	public void setCheckPublications(boolean m_checkPublications);

	public boolean isCheckInstitution();

	public boolean getHideDueDate();

	public void setCheckInstitution(boolean m_checkInstitution);
	
	public boolean isExcludeBibliographic();

	public void setExcludeBibliographic(boolean m_excludeBibliographic);
	
	public boolean isExcludeQuoted();

	public void setExcludeQuoted(boolean m_excludeQuoted);
	
	public boolean isAllowAnyFile();

	public void setAllowAnyFile(boolean m_allowAnyFile);
	
	/**
	 * Exclude type options:
	 * 0 none
	 * 1 word count
	 * 2 percentage
	 * @return
	 */
	 
	public int getExcludeType();
	
	/**
	 * Exclude type options:
	 * 0 none
	 * 1 word count
	 * 2 percentage
	 * 
	 * @param m_excludeType
	 */
	public void setExcludeType(int m_excludeType);
	
	/**
	 * if word count: 1-? (max integer)
	 * if percentage: 0-100 
	 * @return
	 */
	public int getExcludeValue();
	
	/**
	 * if word count: 1-? (max integer)
	 * if percentage: 0-100
	 * @param m_excludeValue
	 */
	public void setExcludeValue(int m_excludeValue);
}
