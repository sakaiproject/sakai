/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AssessmentGradingFacade
    implements AssessmentGradingIfc
// need to implement org.osid.assessment.ItemTaken in the future
// - daisyf 10/11/04
{

  private Long assessmentGradingId;
  private String agentId;
  private PublishedAssessmentIfc publishedAssessment;
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
  private Set itemGradingSet;
  // use the following properties to keep track of time taken for completing an assessment
  private Date attemptDate; // the time stamp when user take the assessment whether
  // it is the 1st or subsequent attempts
  private Integer timeElapsed; // the elapsed time accumulated in second
  private int totalSubmitted;
  private Long publishedAssessmentId;
  private String publishedAssessmentTitle;

  public AssessmentGradingFacade() {
  }

  public AssessmentGradingFacade(AssessmentGradingData data) {
    this.assessmentGradingId = data.getAssessmentGradingId();
    this.publishedAssessment = data.getPublishedAssessment();
    this.agentId = data.getAgentId();
    this.publishedAssessmentId = data.getPublishedAssessmentId();
    this.publishedAssessmentTitle = data.getPublishedAssessmentTitle();
    this.submittedDate = data.getSubmittedDate();
    this.isLate = data.getIsLate();
    this.forGrade = data.getForGrade();
    this.totalAutoScore = data.getTotalAutoScore();
    this.totalOverrideScore = data.getTotalOverrideScore();
    this.finalScore = data.getFinalScore();
    this.comments = data.getComments();
    this.status = data.getStatus();
    this.gradedBy = data.getGradedBy();
    this.gradedDate = data.getGradedDate();
    this.itemGradingSet = data.getItemGradingSet();
    this.attemptDate = data.getAttemptDate();
    this.timeElapsed = data.getTimeElapsed();
  }

  public Long getAssessmentGradingId() {
    return assessmentGradingId;
  }

  public void setAssessmentGradingId(Long assessmentGradingId) {
    this.assessmentGradingId = assessmentGradingId;
  }

  public PublishedAssessmentIfc getPublishedAssessment() {
    return publishedAssessment;
  }

  public void setPublishedAssessment(PublishedAssessmentIfc publishedAssessment) {
    this.publishedAssessment = publishedAssessment;
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

   // Round to the nearest 1/10th.
   if (totalAutoScore != null)
   {
    float alignment = totalAutoScore.floatValue();
    int tmp = Math.round(alignment * 10.0f);
    alignment = (float)tmp / 10.0f;
    totalAutoScore = new Float(alignment);
   }
    return totalAutoScore;
  }

  public void setTotalAutoScore(Float totalAutoScore) {
    this.totalAutoScore = totalAutoScore;
  }

  public Float getTotalOverrideScore() {

   // Round to the nearest 1/10th.
   if (totalOverrideScore != null)
   {
    float alignment = totalOverrideScore.floatValue();
    int tmp = Math.round(alignment * 10.0f);
    alignment = (float)tmp / 10.0f;
    totalOverrideScore = new Float(alignment);
   }
    return totalOverrideScore;
  }

  public void setTotalOverrideScore(Float totalOverrideScore) {
    this.totalOverrideScore = totalOverrideScore;
  }

  public Float getFinalScore() {

  // Round to the nearest 1/10th.
  if ( finalScore != null)
  {
    float alignment = finalScore.floatValue();
    int tmp = Math.round(alignment * 10.0f);
    alignment = (float)tmp / 10.0f;
    finalScore = new Float(alignment);
  }

    return finalScore;
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

  public Long getPublishedAssessmentId() {
    return publishedAssessmentId;
  }

  public void setPublishedAssessmentId(Long publishedAssessmentId) {
    this.publishedAssessmentId = publishedAssessmentId;
  }

  public String getPublishedAssessmentTitle()
  {
    return publishedAssessmentTitle;
  }

  public void setPublishedAssessmentTitle(String publishedAssessmentTitle) {
    this.publishedAssessmentTitle = publishedAssessmentTitle;
  }

  public int getTotalSubmitted() {
    return totalSubmitted;
  }
  public void setTotalSubmitted(int totalSubmitted) {
    this.totalSubmitted = totalSubmitted;
  }

}
