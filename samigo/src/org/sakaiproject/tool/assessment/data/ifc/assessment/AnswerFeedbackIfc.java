package org.sakaiproject.tool.assessment.data.ifc.assessment;


public interface AnswerFeedbackIfc
    extends java.io.Serializable
{
  public static String CORRECT_FEEDBACK = "Correct Feedback";
  public static String INCORRECT_FEEDBACK = "InCorrect Feedback";
  public static String GENERAL_FEEDBACK = "General Feedback";
  public static String ANSWER_FEEDBACK = "answerfeedback";

  Long getId();

  void setId(Long id);

  AnswerIfc getAnswer();

  void setAnswer(AnswerIfc answer);

  String getTypeId();

  void setTypeId(String typeId);

  String getText();

  void setText(String text);

}
