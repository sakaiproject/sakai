package org.sakaiproject.tool.assessment.data.ifc.assessment;


public interface ItemFeedbackIfc
    extends java.io.Serializable
{

  public static String CORRECT_FEEDBACK = "Correct Feedback";
  public static String INCORRECT_FEEDBACK = "InCorrect Feedback";
  public static String GENERAL_FEEDBACK = "General Feedback";

  Long getId();

  void setId(Long id);

  ItemDataIfc getItem();

  void setItem(ItemDataIfc item);

  String getTypeId();

  void setTypeId(String typeId);

  String getText();

  void setText(String text);

}
