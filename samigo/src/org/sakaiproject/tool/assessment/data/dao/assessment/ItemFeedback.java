package org.sakaiproject.tool.assessment.data.dao.assessment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

import java.io.*;

import org.apache.log4j.*;

public class ItemFeedback
    implements Serializable, ItemFeedbackIfc {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;
  public static String CORRECT_FEEDBACK = "Correct Feedback";
  public static String INCORRECT_FEEDBACK = "InCorrect Feedback";

  private Long id;
  private ItemDataIfc item;
  private String typeId;
  private String text;

  public ItemFeedback() {}

  public ItemFeedback(ItemData item, String typeId, String text) {
    this.item = item;
    this.typeId = typeId;
    this.text = text;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ItemDataIfc getItem() {
    return item;
  }

  public void setItem(ItemDataIfc item) {
    this.item = item;
  }

  public String getTypeId() {
    return typeId;
  }

  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

}
