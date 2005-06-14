package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface AssessmentFeedbackIfc
    extends java.io.Serializable
{

  public static Integer IMMEDIATE_FEEDBACK = new Integer(1);
  public static Integer FEEDBACK_BY_DATE = new Integer(2);
  public static Integer NO_FEEDBACK = new Integer(3);

  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  Integer getFeedbackDelivery();

  void setFeedbackDelivery(Integer feedbackDelivery);

  Integer getEditComponents();

  void setEditComponents(Integer editComponents);

  Boolean getShowQuestionText();

  void setShowQuestionText(Boolean showQuestionText);

  Boolean getShowStudentResponse();

  void setShowStudentResponse(Boolean showStudentResponse);

  Boolean getShowCorrectResponse();

  void setShowCorrectResponse(Boolean showCorrectResponse);

  Boolean getShowStudentScore();

  void setShowStudentScore(Boolean showStudentScore);

  Boolean getShowQuestionLevelFeedback();

  void setShowQuestionLevelFeedback(Boolean showQuestionLevelFeedback);

  Boolean getShowSelectionLevelFeedback();

  void setShowSelectionLevelFeedback(Boolean showSelectionLevelFeedback);

  Boolean getShowGraderComments();

  void setShowGraderComments(Boolean showGraderComments);

  Boolean getShowStatistics();

  void setShowStatistics(Boolean showStatistics);
}
