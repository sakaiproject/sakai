package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.apache.log4j.*;
import java.io.Serializable;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

public class Answer
    implements Serializable, AnswerIfc, Comparable {
  static Category errorLogger = Category.getInstance("errorLogger");

  private static final long serialVersionUID = 7526471155622776147L;

  private Long id;
  private ItemTextIfc itemText;
  private ItemDataIfc item;
  private String text;
  private Long sequence;
  private String label;
  private Boolean isCorrect;
  private String grade;
  private Float score;
  private Set answerFeedbackSet;
  private HashMap answerFeedbackMap;

  public Answer() {}

  public Answer(ItemTextIfc itemText, String text, Long sequence, String label,
                Boolean isCorrect, String grade, Float score) {
    this.itemText = itemText;
    this.item = itemText.getItem();
    this.text = text;
    this.sequence = sequence;
    this.label = label;
    this.isCorrect = isCorrect;
    this.grade = grade;
    this.score = score;
  }

  public Answer(ItemTextIfc itemText, String text, Long sequence, String label,
                Boolean isCorrect, String grade, Float score,
                Set answerFeedbackSet) {
    this.itemText = itemText;
    this.item = itemText.getItem();
    this.text = text;
    this.sequence = sequence;
    this.label = label;
    this.isCorrect = isCorrect;
    this.grade = grade;
    this.score = score;
    this.answerFeedbackSet = answerFeedbackSet;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ItemTextIfc getItemText() {
    return itemText;
  }

  public void setItemText(ItemTextIfc itemText) {
    this.itemText = itemText;
  }

  public ItemDataIfc getItem() {
    return item;
  }

  public void setItem(ItemDataIfc item) {
    this.item = item;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Long getSequence() {
    return sequence;
  }

  public void setSequence(Long sequence) {
    this.sequence = sequence;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public Float getScore() {
    return score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public Set getAnswerFeedbackSet() {
    return answerFeedbackSet;
  }

  public ArrayList getAnswerFeedbackArray() {
    ArrayList list = new ArrayList();
    Iterator iter = answerFeedbackSet.iterator();
    while (iter.hasNext()){
      list.add(iter.next());
    }
    return list;
  }

  public void setAnswerFeedbackSet(Set answerFeedbackSet) {
    this.answerFeedbackSet = answerFeedbackSet;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  public String getAnswerFeedback(String typeId) {
    //System.out.println("**trying to get feeback = "+typeId);
    if (this.answerFeedbackMap == null)
      this.answerFeedbackMap = getAnswerFeedbackMap();
    return (String)this.answerFeedbackMap.get(typeId);
  }

  public HashMap getAnswerFeedbackMap() {
    HashMap answerFeedbackMap = new HashMap();
    if (this.answerFeedbackSet != null){
      for (Iterator i = this.answerFeedbackSet.iterator(); i.hasNext(); ) {
        AnswerFeedback answerFeedback = (AnswerFeedback) i.next();
        answerFeedbackMap.put(answerFeedback.getTypeId(), answerFeedback.getText());
      }
    }
    return answerFeedbackMap;
  }

  public String getCorrectAnswerFeedback() {
    return getAnswerFeedback(AnswerFeedbackIfc.CORRECT_FEEDBACK);
  }

  public String getInCorrectAnswerFeedback() {
    return getAnswerFeedback(AnswerFeedbackIfc.INCORRECT_FEEDBACK);
  }

  public String getGeneralAnswerFeedback() {
    return getAnswerFeedback(AnswerFeedbackIfc.GENERAL_FEEDBACK);
  }

  public String getTheAnswerFeedback() {
    return getAnswerFeedback(AnswerFeedbackIfc.ANSWER_FEEDBACK);
  }

  public int compareTo(Object o) {
      Answer a = (Answer)o;
      return sequence.compareTo(a.sequence);
  }


}
