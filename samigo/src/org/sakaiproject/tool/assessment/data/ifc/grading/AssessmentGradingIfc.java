package org.sakaiproject.tool.assessment.data.ifc.grading;
import java.util.Date;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

public interface AssessmentGradingIfc
    extends java.io.Serializable{

  Long getAssessmentGradingId();

  void setAssessmentGradingId(Long assessmentGradingId);

  PublishedAssessmentIfc getPublishedAssessment();

  void setPublishedAssessment(PublishedAssessmentIfc publishedAssessment);

  String getAgentId();

  void setAgentId(String agentId);

  //AgentIfc getAgent();

  Date getSubmittedDate();

  void setSubmittedDate(Date submittedDate);

  // Is isLate determined by comparing the submitted date with the duedate
  // of published assessment or core assessment?
  // if the former, then we need to store the duedate info in DB
  // if latter, isLate is determined on the fly -
  // 'cos core assessment due date can be changed.
  Boolean getIsLate();

  void setIsLate(Boolean isLate);

  Boolean getForGrade();

  void setForGrade(Boolean forGrade);

  // sum of item score through auto scoring
  Float getTotalAutoScore();

  void setTotalAutoScore(Float totalAutoScore);

  // sum of item score through instructor grading
  Float getTotalOverrideScore();

  void setTotalOverrideScore(Float totalOverrideScore);

  // grader can override the total score with a final score
  Float getFinalScore();

  void setFinalScore(Float finalScore);

  String getComments();

  void setComments(String comments);

  String getGradedBy();

  void setGradedBy(String GradedBy);

  Date getGradedDate();

  void setGradedDate(Date GradedDate);

  Integer getStatus();

  void setStatus(Integer status);

  Set getItemGradingSet();

  void setItemGradingSet(Set itemGradingSet);

  Date getAttemptDate();

  void setAttemptDate(Date attemptDate);

  Integer getTimeElapsed();

  void setTimeElapsed(Integer timeElapsed);

}
