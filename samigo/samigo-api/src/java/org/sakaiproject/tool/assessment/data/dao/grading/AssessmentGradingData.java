/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.dao.grading;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AssessmentGradingData
implements java.io.Serializable
// need to implement org.osid.assessment.ItemTaken in the future
// - daisyf 10/11/04
{
	private static final long serialVersionUID = 7526471155622776147L;

	private Long assessmentGradingId;
	private String agentId;
	//private PublishedAssessmentIfc publishedAssessment;
	private Date submittedDate;
	private Boolean isLate;
	private Boolean forGrade;
	private Float totalAutoScore;
	private Float totalOverrideScore;
	private Float finalScore; // final total score
	private String comments;
	private Integer status;
	private String gradedBy;
	private Date gradedDate;
	private Set itemGradingSet = new HashSet();
	private Date attemptDate;
	private Integer timeElapsed;
	private int totalSubmitted;
	private Long publishedAssessmentId;
	private String publishedAssessmentTitle;
	private Boolean isAutoSubmitted;
	private Integer lastVisitedPart = 0;
	private Integer lastVisitedQuestion = 0;






	// Because of SAK-16456, we no longer need to show the auto/human graded status per submission, I don't think we 
	// need to distinguish status 2 and 3 anymore. But I just leave them here...


	/**
	 * status = 1: submit but not grade yet
	 */
	public static final Integer SUBMITTED = Integer.valueOf(1);
	/**
	 * status = 2: grader has went to total score page and graded + AUTO_GRADED
	 */
	public static final Integer AUTO_GRADED = Integer.valueOf(2);

	/**
	 * status = 3: grader has went to total score page and graded + at least one question NEED_HUMAN_ATTENTION
	 */
	public static final Integer NEED_HUMAN_ATTENTION = Integer.valueOf(3);

	/**
	 * status = 4: the assessment has be republished. This assessment has been submitted. Therefore, this it needs to be resubmit
	 */
	public static final Integer ASSESSMENT_UPDATED_NEED_RESUBMIT = Integer.valueOf(4);

	/**
	 * status = 5: there is no submission but grader update something in the score page
	 */
	public static final Integer NO_SUBMISSION = Integer.valueOf(5);

	/**
	 *  status = 6: the assessment has be republished. This assessment has begun but not yet been submitted (saved/in progress). Therefore, just warn the student about the update ("resubmit" is not applicable here).
	 */
	public static final Integer ASSESSMENT_UPDATED = Integer.valueOf(6);



	public AssessmentGradingData() {
	}

	// this constructor do not contains Set of ItemGradingData
	public AssessmentGradingData(Long assessmentGradingId,
			Long publishedAssessmentId, String publishedAssessmentTitle, String agentId,
			Date submittedDate, Boolean isLate,
			Boolean forGrade, Float totalAutoScore, Float totalOverrideScore,
			Float finalScore, String comments, Integer status, String gradedBy,
			Date gradedDate,  Date attemptDate, Integer timeElapsed, Boolean isAutoSubmitted
	){
		this.assessmentGradingId = assessmentGradingId;
		this.publishedAssessmentId = publishedAssessmentId;
		this.publishedAssessmentTitle = publishedAssessmentTitle;
		this.agentId = agentId;
		this.submittedDate = submittedDate;
		this.isLate = isLate;
		this.forGrade = forGrade;
		this.totalAutoScore = totalAutoScore;
		this.totalOverrideScore = totalOverrideScore;
		this.finalScore = finalScore;
		this.comments = comments;
		this.status = status;
		this.gradedBy = gradedBy;
		this.gradedDate = gradedDate;
		this.attemptDate = attemptDate;
		this.timeElapsed = timeElapsed;
		this.isAutoSubmitted = isAutoSubmitted;
	}

	public AssessmentGradingData(Long assessmentGradingId,
			Long publishedAssessmentId, String publishedAssessmentTitle, String agentId,
			Date submittedDate, Boolean isLate,
			Boolean forGrade, Float totalAutoScore, Float totalOverrideScore,
			Float finalScore, String comments, Integer status, String gradedBy,
			Date gradedDate,  Date attemptDate, Integer timeElapsed){
		this(assessmentGradingId, publishedAssessmentId, publishedAssessmentTitle, agentId,
				submittedDate, isLate, forGrade, totalAutoScore, totalOverrideScore, finalScore, 
				comments, status, gradedBy, gradedDate, attemptDate, timeElapsed, Boolean.valueOf(false));
	}

	public AssessmentGradingData(Long assessmentGradingId,
			Long publishedAssessmentId, String agentId,
			Date submittedDate, Boolean isLate,
			Boolean forGrade, Float totalAutoScore, Float totalOverrideScore,
			Float finalScore, String comments, Integer status, String gradedBy,
			Date gradedDate,  Date attemptDate, Integer timeElapsed){
		this.assessmentGradingId = assessmentGradingId;
		this.publishedAssessmentId = publishedAssessmentId;
		this.agentId = agentId;
		this.submittedDate = submittedDate;
		this.isLate = isLate;
		this.forGrade = forGrade;
		this.totalAutoScore = totalAutoScore;
		this.totalOverrideScore = totalOverrideScore;
		this.finalScore = finalScore;
		this.comments = comments;
		this.status = status;
		this.gradedBy = gradedBy;
		this.gradedDate = gradedDate;
		this.attemptDate = attemptDate;
		this.timeElapsed = timeElapsed;
	}

	public AssessmentGradingData(Long publishedAssessmentId, int totalSubmitted){
		this.publishedAssessmentId = publishedAssessmentId;
		this.totalSubmitted = totalSubmitted;
	}

	public Long getAssessmentGradingId() {
		return assessmentGradingId;
	}
	public void setAssessmentGradingId(Long assessmentGradingId) {
		this.assessmentGradingId = assessmentGradingId;
	}
	/*
  public PublishedAssessmentIfc getPublishedAssessment() {
    return publishedAssessment;
  }
  public void setPublishedAssessment(PublishedAssessmentIfc publishedAssessment) {
    this.publishedAssessment = publishedAssessment;
  }
	 */

	public Long getPublishedAssessmentId() {
		return publishedAssessmentId;
	}
	public void setPublishedAssessmentId(Long publishedAssessmentId) {
		this.publishedAssessmentId = publishedAssessmentId;
	}

	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public Date getSubmittedDate() {
		return submittedDate;
	}
	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}
	public Boolean getIsLate() {
		return isLate;
	}
	public void setIsLate(Boolean isLate) {
		this.isLate = isLate;
	}
	public Boolean getForGrade() {
		return forGrade;
	}
	public void setForGrade(Boolean forGrade) {
		this.forGrade = forGrade;
	}
	public Float getTotalAutoScore() {
		return this.totalAutoScore;
	}

	public void setTotalAutoScore(Float totalAutoScore) {
		if (totalAutoScore != null){
			if (totalAutoScore.floatValue()< 0){
				this.totalAutoScore=new Float("0");
			}else{
				this.totalAutoScore = totalAutoScore;
			}
		}else{
			this.totalAutoScore =  null ;
		}
	}

	public Float getTotalOverrideScore() {
		return this.totalOverrideScore;
	}

	public void setTotalOverrideScore(Float totalOverrideScore) {
		this.totalOverrideScore = totalOverrideScore;
	}
	public Float getFinalScore() {
		/*
    if (this.totalAutoScore != null && this.totalOverrideScore != null ){
      float total = 0;
      if (this.totalAutoScore != null)
         total += this.totalAutoScore.floatValue();
      if (this.totalOverrideScore != null)
         total += this.totalOverrideScore.floatValue();
      this.finalScore = new Float(total);
    }

    // remove rounding , SAK-2848 
    // Round to the nearest 1/10th.
    if (this.finalScore !=null ){
      float alignment = this.finalScore.floatValue();
      int tmp = Math.round(alignment * 10.0f);
      alignment = (float)tmp / 10.0f;
      this.finalScore = new Float(alignment);
    }
		 */
		return this.finalScore;
	}
	public void setFinalScore(Float finalScore) {
		this.finalScore = finalScore;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getGradedBy() {
		return gradedBy;
	}
	public void setGradedBy(String gradedBy) {
		this.gradedBy = gradedBy;
	}
	public Date getGradedDate() {
		return gradedDate;
	}
	public void setGradedDate(Date gradedDate) {
		this.gradedDate = gradedDate;
	}

	/**
	 * In some cases, students are allowed to submit multiple assessment
	 * for grading. However, the grader has the choice to select one to
	 * represent how well the student does overall. status = 1 means
	 * this submitted assessment is selected.
	 */
	// daisy's comment: I am not sure Integer(1) is being used at all. 11/18/05
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

	public Set getItemGradingSet() {
		return itemGradingSet;
	}

	public void setItemGradingSet(Set itemGradingSet) {
		this.itemGradingSet = itemGradingSet;
	}

	public Date getAttemptDate() {
		return attemptDate;
	}
	public void setAttemptDate(Date attemptDate) {
		this.attemptDate = attemptDate;
	}

	public Integer getTimeElapsed() {
		return timeElapsed;
	}
	public void setTimeElapsed(Integer timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	public int getTotalSubmitted() {
		return totalSubmitted;
	}
	public void setTotalSubmitted(int totalSubmitted) {
		this.totalSubmitted = totalSubmitted;
	}

	public String getPublishedAssessmentTitle()
	{
		return publishedAssessmentTitle;
	}

	public void setPublishedAssessmentTitle(String publishedAssessmentTitle) {
		this.publishedAssessmentTitle = publishedAssessmentTitle;
	}

	// daisy added this for mining partial assessmentGradingData object
	// 11/17/05
	private Long publishedItemId;

	private boolean isRecorded;

	public Long getPublishedItemId() {
		return publishedItemId;
	}

	public void setItemGradingId(Long publishedItemId) {
		this.publishedItemId =  publishedItemId;
	}

	public AssessmentGradingData(Long assessmentGradingId, Long publishedItemId,
			String agentId, Float finalScore, Date submittedDate) {
		this.assessmentGradingId = assessmentGradingId;
		this.publishedItemId = publishedItemId;
		this.agentId = agentId;
		this.finalScore = finalScore;
		this.submittedDate = submittedDate;
	}

	public Boolean getIsAutoSubmitted() {
		return isAutoSubmitted;
	}

	public void setIsAutoSubmitted(Boolean isAutoSubmitted) {
		this.isAutoSubmitted = isAutoSubmitted;
	}

	public Integer getLastVisitedPart()
	{
		return lastVisitedPart;
	}

	public void setLastVisitedPart(Integer lastVisitedPart) {
		this.lastVisitedPart = lastVisitedPart;
	}

	public Integer getLastVisitedQuestion()
	{
		return lastVisitedQuestion;
	}

	public void setLastVisitedQuestion(Integer lastVisitedQuestion) {
		this.lastVisitedQuestion = lastVisitedQuestion;
	}

	public boolean getIsRecorded() {
		return isRecorded;
	}

	public void setIsRecorded(boolean isRecorded) {
		this.isRecorded = isRecorded;
	}


}
