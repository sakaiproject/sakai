/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.impl.conversion;

import java.util.List;

import org.sakaiproject.entity.api.serialize.SerializableEntity;

/**
 * <pre>
 * This is an accessor interface for the Submission to enable the conversion to do its work
 * All methods should be straight getters and setters with no logic.
 * </pre>
 *
 */
public interface SerializableSubmissionAccess 
{
	/**
	 * @return the id
	 */
	public String getId();

	/**
	 * @param id the id to set
	 */
	public void setId(String id);

	/**
	 * @return the grade
	 */
	public String getGrade();

	/**
	 * @param grade the grade to set
	 */
	public void setGrade(String grade);

	/**
	 * @return the assignment
	 */
	public String getAssignment();

	/**
	 * @param assignment the assignment to set
	 */
	public void setAssignment(String assignment);

	/**
	 * @return the context
	 */
	public String getContext();

	/**
	 * @param context the context to set
	 */
	public void setContext(String context);

	/**
	 * @return the datereturned
	 */
	public String getDatereturned();

	/**
	 * @param datereturned the datereturned to set
	 */
	public void setDatereturned(String datereturned);

	/**
	 * @return the feedbackcomment
	 */
	public String getFeedbackcomment();

	/**
	 * @param feedbackcomment the feedbackcomment to set
	 */
	public void setFeedbackcomment(String feedbackcomment);

	/**
	 * @return the feedbackcomment_html
	 */
	public String getFeedbackcomment_html();

	/**
	 * @param feedbackcomment_html the feedbackcomment_html to set
	 */
	public void setFeedbackcomment_html(String feedbackcomment_html);

	/**
	 * @return the feedbacktext
	 */
	public String getFeedbacktext();

	/**
	 * @param feedbacktext the feedbacktext to set
	 */
	public void setFeedbacktext(String feedbacktext);

	/**
	 * @return the feedbacktext_html
	 */
	public String getFeedbacktext_html();

	/**
	 * @param feedbacktext_html the feedbacktext_html to set
	 */
	public void setFeedbacktext_html(String feedbacktext_html);

	/**
	 * @return the graded
	 */
	public String getGraded();

	/**
	 * @param graded the graded to set
	 */
	public void setGraded(String graded);

	/**
	 * @return the gradereleased
	 */
	public String getGradereleased();

	/**
	 * @param gradereleased the gradereleased to set
	 */
	public void setGradereleased(String gradereleased);

	/**
	 * @return the lastmod
	 */
	public String getLastmod();

	/**
	 * @param lastmod the lastmod to set
	 */
	public void setLastmod(String lastmod);

	/**
	 * @return the pledgeflag
	 */
	public String getPledgeflag();

	/**
	 * @param pledgeflag the pledgeflag to set
	 */
	public void setPledgeflag(String pledgeflag);

	/**
	 * @return the returned
	 */
	public String getReturned();

	/**
	 * @param returned the returned to set
	 */
	public void setReturned(String returned);

	/**
	 * @return the reviewReport
	 */
	public String getReviewReport();

	/**
	 * @param reviewReport the reviewReport to set
	 */
	public void setReviewReport(String reviewReport);

	/**
	 * @return the reviewScore
	 */
	public String getReviewScore();

	/**
	 * @param reviewScore the reviewScore to set
	 */
	public void setReviewScore(String reviewScore);

	/**
	 * @return the reviewStatus
	 */
	public String getReviewStatus();

	/**
	 * @param reviewStatus the reviewStatus to set
	 */
	public void setReviewStatus(String reviewStatus);

	/**
	 * @return the submitted
	 */
	public String getSubmitted();

	/**
	 * @param submitted the submitted to set
	 */
	public void setSubmitted(String submitted);

	/**
	 * @return the submittedattachments
	 */
	public List<String> getSubmittedattachments();

	/**
	 * @param submittedattachments the submittedattachments to set
	 */
	public void setSubmittedattachments(List<String> submittedattachments);

	/**
	 * @return the feedbackattachments
	 */
	public List<String> getFeedbackattachments();

	/**
	 * @param feedbackattachments the feedbackattachments to set
	 */
	public void setFeedbackattachments(List<String> feedbackattachments);

	/**
	 * @return the datesubmitted
	 */
	public String getDatesubmitted();

	/**
	 * @param datesubmitted the datesubmitted to set
	 */
	public void setDatesubmitted(String datesubmitted);

	/**
	 * @return the submittedtext
	 */
	public String getSubmittedtext();

	/**
	 * @param submittedtext the submittedtext to set
	 */
	public void setSubmittedtext(String submittedtext);

	/**
	 * @return the submittedtext_html
	 */
	public String getSubmittedtext_html();

	/**
	 * @param submittedtext_html the submittedtext_html to set
	 */
	public void setSubmittedtext_html(String submittedtext_html);

	/**
	 * @return the submitters
	 */
	public List<String> getSubmitters();

	/**
	 * @return the submitter id
	 */
	public String getSubmitterId();
        /**
	 * @param id the submitter id to set
	 */
	public void setSubmitterId(String id);
        public void setGrades(List<String> grades);
        public List<String> getGrades();

	/**
	 * @param submitters the submitters to set
	 */
	public void setSubmitters(List<String> submitters);
	
	/**
	 * 
	 * @return
	 */
	public SerializableEntity getSerializableProperties();
	
}
