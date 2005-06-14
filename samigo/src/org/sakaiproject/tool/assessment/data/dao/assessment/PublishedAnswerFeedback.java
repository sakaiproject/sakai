package org.sakaiproject.tool.assessment.data.dao.assessment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;

import java.io.*;

import org.apache.log4j.*;

public class PublishedAnswerFeedback
    implements Serializable, AnswerFeedbackIfc {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long id;
  private AnswerIfc answer;
  private String typeId;
  private String text;

  public PublishedAnswerFeedback() {}

  public PublishedAnswerFeedback(AnswerIfc answer, String typeId, String text) {
    this.answer = answer;
    this.typeId = typeId;
    this.text = text;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AnswerIfc getAnswer() {
    return answer;
  }

  public void setAnswer(AnswerIfc answer) {
    this.answer = answer;
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
