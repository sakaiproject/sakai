package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;

public class AssessmentFeedback
    implements java.io.Serializable, AssessmentFeedbackIfc
{

  private Long id;
  private AssessmentBaseIfc assessmentBase;
  private Integer feedbackDelivery; // immediate, on specific date , no feedback
  private Integer editComponents; // 0 = cannot
  private Boolean showQuestionText;
  private Boolean showStudentResponse;
  private Boolean showCorrectResponse;
  private Boolean showStudentScore;
  private Boolean showQuestionLevelFeedback;
  private Boolean showSelectionLevelFeedback; // must be MC
  private Boolean showGraderComments;
  private Boolean showStatistics;

  /**
   * Creates a new SubmissionModel object.
   */
  public AssessmentFeedback()
  {
  }

  public AssessmentFeedback(
      Integer feedbackDelivery, Integer editComponents,
      Boolean showQuestionText,
      Boolean showStudentResponse, Boolean showCorrectResponse,
      Boolean showStudentScore,
      Boolean showQuestionLevelFeedback, Boolean showSelectionLevelFeedback,
      Boolean showGraderComments, Boolean showStatistics)
  {
    this.feedbackDelivery = feedbackDelivery;
    this.editComponents = editComponents;
    this.showQuestionText = showQuestionText;
    this.showStudentResponse = showStudentResponse;
    this.showCorrectResponse = showCorrectResponse;
    this.showStudentScore = showStudentScore;
    this.showQuestionLevelFeedback = showQuestionLevelFeedback;
    this.showSelectionLevelFeedback = showSelectionLevelFeedback; // must be MC
    this.showGraderComments = showGraderComments;
    this.showStatistics = showStatistics;
  }

  public Object clone() throws CloneNotSupportedException{
    Object cloned = new AssessmentFeedback(
        this.getFeedbackDelivery(), this.getEditComponents(),
        this.getShowQuestionText(),
        this.getShowStudentResponse(), this.getShowCorrectResponse(),
        this.getShowStudentScore(), this.getShowQuestionLevelFeedback(),
        this.getShowSelectionLevelFeedback(), this.getShowGraderComments(),
        this.getShowStatistics());
    return cloned;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public void setAssessmentBase(AssessmentBaseIfc assessmentBase)
  {
    this.assessmentBase = assessmentBase;
  }

  public AssessmentBaseIfc getAssessmentBase()
  {
    if (assessmentBase.getIsTemplate().equals(Boolean.TRUE))
      return (AssessmentTemplateIfc)assessmentBase;
    else
      return (AssessmentIfc)assessmentBase;
  }

  public Integer getFeedbackDelivery()
  {
    return feedbackDelivery;
  }

  public void setFeedbackDelivery(Integer feedbackDelivery)
  {
    this.feedbackDelivery = feedbackDelivery;
  }

  public Integer getEditComponents() {
    return editComponents;
  }

  public void setEditComponents(Integer editComponents) {
    this.editComponents = editComponents;
  }

  public Boolean getShowQuestionText()
  {
    return showQuestionText;
  }

  public void setShowQuestionText(Boolean showQuestionText)
  {
    this.showQuestionText = showQuestionText;
  }

  public Boolean getShowStudentResponse()
  {
    return showStudentResponse;
  }

  public void setShowStudentResponse(Boolean showStudentResponse)
  {
    this.showStudentResponse = showStudentResponse;
  }

  public Boolean getShowCorrectResponse()
  {
    return showCorrectResponse;
  }

  public void setShowCorrectResponse(Boolean showCorrectResponse)
  {
    this.showCorrectResponse = showCorrectResponse;
  }

  public Boolean getShowStudentScore()
  {
    return showStudentScore;
  }

  public void setShowStudentScore(Boolean showStudentScore)
  {
    this.showStudentScore = showStudentScore;
  }

  public Boolean getShowQuestionLevelFeedback()
  {
    return showQuestionLevelFeedback;
  }

  public void setShowQuestionLevelFeedback(Boolean showQuestionLevelFeedback)
  {
    this.showQuestionLevelFeedback = showQuestionLevelFeedback;
  }

  public Boolean getShowSelectionLevelFeedback()
  {
    return showSelectionLevelFeedback;
  }

  public void setShowSelectionLevelFeedback(Boolean showSelectionLevelFeedback)
  {
    this.showSelectionLevelFeedback = showSelectionLevelFeedback;
  }

  public Boolean getShowGraderComments()
  {
    return showGraderComments;
  }

  public void setShowGraderComments(Boolean showGraderComments)
  {
    this.showGraderComments = showGraderComments;
  }

  public Boolean getShowStatistics()
  {
    return showStatistics;
  }

  public void setShowStatistics(Boolean showStatistics)
  {
    this.showStatistics = showStatistics;
  }

}
