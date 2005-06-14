package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public  interface AnswerIfc
    extends java.io.Serializable
{
  Long getId();

  void setId(Long id);

  ItemTextIfc getItemText();

  void setItemText(ItemTextIfc itemText);

  ItemDataIfc getItem();

  void setItem(ItemDataIfc item) ;

  String getText();

  void setText(String text);

  Long getSequence();

  void setSequence(Long sequence);

  String getLabel();

  void setLabel(String label);

  Boolean getIsCorrect();

  void setIsCorrect(Boolean isCorrect);

  String getGrade();

  void setGrade(String grade);

  Float getScore();

  void setScore(Float score);

  Set getAnswerFeedbackSet();

  ArrayList getAnswerFeedbackArray();

  void setAnswerFeedbackSet(Set answerFeedbackSet);

  String getAnswerFeedback(String typeId);

  HashMap getAnswerFeedbackMap();

  String getCorrectAnswerFeedback();

  String getInCorrectAnswerFeedback();

  String getGeneralAnswerFeedback();

}
