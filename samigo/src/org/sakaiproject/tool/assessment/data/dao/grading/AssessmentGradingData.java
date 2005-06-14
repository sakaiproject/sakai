package org.sakaiproject.tool.assessment.data.dao.grading;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

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

public class AssessmentGradingData
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
  private Date attemptDate;
  private Integer timeElapsed;
  private int totalSubmitted;
  private Long publishedAssessmentId;
  private String publishedAssessmentTitle;
  public AssessmentGradingData() {
  }

  // this constructor do not contains Set of ItemGradingData
  public AssessmentGradingData(Long assessmentGradingId,
      Long publishedAssessmentId, String publishedAssessmentTitle, String agentId,
      Date submittedDate, Boolean isLate,
      Boolean forGrade, Float totalAutoScore, Float totalOverrideScore,
      Float finalScore, String comments, Integer status, String gradedBy,
      Date gradedDate,  Date attemptDate, Integer timeElapsed
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
    if (getItemGradingSet()!=null){
      this.totalAutoScore = new Float("0");
      float autoScore = 0;
      Iterator iter = getItemGradingSet().iterator();
      while (iter.hasNext()){
        ItemGradingData i = (ItemGradingData)iter.next();
        if (i.getAutoScore()!=null)
          autoScore += i.getAutoScore().floatValue();
      }
      this.totalAutoScore = new Float(autoScore);
    }
    
    // Round to the nearest 1/10th.
    float alignment = this.totalAutoScore.floatValue();
    int tmp = Math.round(alignment * 10.0f); 
    alignment = (float)tmp / 10.0f;
    this.totalAutoScore = new Float(alignment);

    return this.totalAutoScore;
  }
  public void setTotalAutoScore(Float totalAutoScore) {
    this.totalAutoScore = totalAutoScore;
  }

  public Float getTotalOverrideScore() {
    if (getItemGradingSet()!=null){
      this.totalOverrideScore = new Float("0");
      float overrideScore = 0;
      Iterator iter = getItemGradingSet().iterator();
      while (iter.hasNext()){
        ItemGradingData i = (ItemGradingData)iter.next();
        if (i.getOverrideScore() != null)
          overrideScore += i.getOverrideScore().floatValue();
      }
    this.totalOverrideScore = new Float(overrideScore);
    }

    // Round to the nearest 1/10th.
    float alignment = this.totalOverrideScore.floatValue();
    int tmp = Math.round(alignment * 10.0f); 
    alignment = (float)tmp / 10.0f;
    this.totalOverrideScore = new Float(alignment);

    return this.totalOverrideScore;
  }

  public void setTotalOverrideScore(Float totalOverrideScore) {
    this.totalOverrideScore = totalOverrideScore;
  }
  public Float getFinalScore() {
    if (this.totalAutoScore != null && this.totalOverrideScore != null ){
      float total = 0;
      if (this.totalAutoScore != null)
         total += this.totalAutoScore.floatValue();
      if (this.totalOverrideScore != null)
         total += this.totalOverrideScore.floatValue();
      this.finalScore = new Float(total);
    }
   
    // Round to the nearest 1/10th.
    float alignment = this.finalScore.floatValue();
    int tmp = Math.round(alignment * 10.0f); 
    alignment = (float)tmp / 10.0f;
    this.finalScore = new Float(alignment);

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
}
