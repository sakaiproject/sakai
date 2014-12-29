/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AssessmentGradingSummaryData
    
// need to implement org.osid.assessment.ItemTaken in the future
// - daisyf 10/11/04
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -7857133156727718632L;
private Long assessmentGradingSummaryId;
  private PublishedAssessmentIfc publishedAssessment;
  private String agentId;
  private Integer totalSubmitted;
  private Integer totalSubmittedForGrade;
  private AssessmentGradingData lastSubmittedAssessmentGrading;
  private Date lastSubmittedDate;
  private Boolean lastSubmittedAssessmentIsLate;
  private Double sumOf_autoScoreForGrade;
  private Double average_autoScoreForGrade;
  private Double highest_autoScoreForGrade;
  private Double lowest_autoScoreForGrade;
  private Double last_autoScoreForGrade;
  private Double sumOf_overrideScoreForGrade;
  private Double average_overrideScoreForGrade;
  private Double highest_overrideScoreForGrade;
  private Double lowest_overrideScoreForGrade;
  private Double last_overrideScoreForGrade;
  private Integer scoringType;
  private AssessmentGradingData acceptedAssessmentGrading;
  private Boolean acceptedAssessmentIsLate;
  private Double finalAssessmentScore;
  private Boolean feedToGradeBook;

  public AssessmentGradingSummaryData() {
  }
  public Long getAssessmentGradingSummaryId() {
    return assessmentGradingSummaryId;
  }
  public void setAssessmentGradingSummaryId(Long assessmentGradingSummaryId) {
    this.assessmentGradingSummaryId = assessmentGradingSummaryId;
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
  public Integer getTotalSubmitted() {
    return totalSubmitted;
  }
  public void setTotalSubmitted(Integer totalSubmitted) {
    this.totalSubmitted = totalSubmitted;
  }
  public Integer getTotalSubmittedForGrade() {
    return totalSubmittedForGrade;
  }
  public void setTotalSubmittedForGrade(Integer totalSubmittedForGrade) {
    this.totalSubmittedForGrade = totalSubmittedForGrade;
  }
  public AssessmentGradingData getLastSubmittedAssessmentGrading() {
    return lastSubmittedAssessmentGrading;
  }
  public void setLastSubmittedAssessmentGrading(AssessmentGradingData lastSubmittedAssessmentGrading) {
    this.lastSubmittedAssessmentGrading = lastSubmittedAssessmentGrading;
  }
  public Date getLastSubmittedDate() {
    return lastSubmittedDate;
  }
  public void setLastSubmittedDate(Date lastSubmittedDate) {
    this.lastSubmittedDate = lastSubmittedDate;
  }
  public Boolean getLastSubmittedAssessmentIsLate() {
    return lastSubmittedAssessmentIsLate;
  }
  public void setLastSubmittedAssessmentIsLate(Boolean lastSubmittedAssessmentIsLate) {
    this.lastSubmittedAssessmentIsLate = lastSubmittedAssessmentIsLate;
  }
  public Double getSumOf_autoScoreForGrade() {
    return sumOf_autoScoreForGrade;
  }
  public void setSumOf_autoScoreForGrade(Double sumOf_autoScoreForGrade) {
    this.sumOf_autoScoreForGrade = sumOf_autoScoreForGrade;
  }
  public Double getAverage_autoScoreForGrade() {
    return average_autoScoreForGrade;
  }
  public void setAverage_autoScoreForGrade(Double average_autoScoreForGrade) {
    this.average_autoScoreForGrade = average_autoScoreForGrade;
  }
  public Double getHighest_autoScoreForGrade() {
    return highest_autoScoreForGrade;
  }
  public void setHighest_autoScoreForGrade(Double highest_autoScoreForGrade) {
    this.highest_autoScoreForGrade = highest_autoScoreForGrade;
  }
  public Double getLowest_autoScoreForGrade() {
    return lowest_autoScoreForGrade;
  }
  public void setLowest_autoScoreForGrade(Double lowest_autoScoreForGrade) {
    this.lowest_autoScoreForGrade = lowest_autoScoreForGrade;
  }
  public Double getLast_autoScoreForGrade() {
    return last_autoScoreForGrade;
  }
  public void setLast_autoScoreForGrade(Double last_autoScoreForGrade) {
    this.last_autoScoreForGrade = last_autoScoreForGrade;
  }
  public Double getSumOf_overrideScoreForGrade() {
    return sumOf_overrideScoreForGrade;
  }
  public void setSumOf_overrideScoreForGrade(Double sumOf_overrideScoreForGrade) {
    this.sumOf_overrideScoreForGrade = sumOf_overrideScoreForGrade;
  }
  public Double getAverage_overrideScoreForGrade() {
    return average_overrideScoreForGrade;
  }
  public void setAverage_overrideScoreForGrade(Double average_overrideScoreForGrade) {
    this.average_overrideScoreForGrade = average_overrideScoreForGrade;
  }
  public Double getHighest_overrideScoreForGrade() {
    return highest_overrideScoreForGrade;
  }
  public void setHighest_overrideScoreForGrade(Double highest_overrideScoreForGrade) {
    this.highest_overrideScoreForGrade = highest_overrideScoreForGrade;
  }
  public Double getLowest_overrideScoreForGrade() {
    return lowest_overrideScoreForGrade;
  }
  public void setLowest_overrideScoreForGrade(Double lowest_overrideScoreForGrade) {
    this.lowest_overrideScoreForGrade = lowest_overrideScoreForGrade;
  }
  public Double getLast_overrideScoreForGrade() {
    return last_overrideScoreForGrade;
  }
  public void setLast_overrideScoreForGrade(Double last_overrideScoreForGrade) {
    this.last_overrideScoreForGrade = last_overrideScoreForGrade;
  }
  public Integer getScoringType() {
    return scoringType;
  }
  public void setScoringType(Integer scoringType) {
    this.scoringType = scoringType;
  }

  public AssessmentGradingData getAcceptedAssessmentGrading() {
    return acceptedAssessmentGrading;
  }
  public void setAcceptedAssessmentGrading(AssessmentGradingData acceptedAssessmentGrading) {
    this.acceptedAssessmentGrading = acceptedAssessmentGrading;
  }
  public Boolean getAcceptedAssessmentIsLate() {
    return acceptedAssessmentIsLate;
  }
  public void setAcceptedAssessmentIsLate(Boolean acceptedAssessmentIsLate) {
    this.acceptedAssessmentIsLate = acceptedAssessmentIsLate;
  }
  public Double getFinalAssessmentScore() {
    return finalAssessmentScore;
  }
  public void setFinalAssessmentScore(Double finalAssessmentScore) {
    this.finalAssessmentScore = finalAssessmentScore;
  }
  public Boolean getFeedToGradeBook() {
    return feedToGradeBook;
  }
  public void setFeedToGradeBook(Boolean feedToGradeBook) {
    this.feedToGradeBook = feedToGradeBook;
  }

}
