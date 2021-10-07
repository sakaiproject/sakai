/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;

public class Answer extends AnswerIfc implements Cloneable, Comparable<AnswerIfc> {

  private static final long serialVersionUID = 7526471155622776147L;

  private ItemData dat = new ItemData();  

  public Answer() {}

  public Answer(ItemTextIfc itemText, String text, Long sequence, String label) {
	  this(itemText, text, sequence, label, null, null, null, null, null);
  }
  
  public Answer(ItemTextIfc itemText, String text, Long sequence, String label,
                Boolean isCorrect, String grade, Double score, Double partialCredit, Double discount) {
    this.itemText = itemText;
    this.item = itemText.getItem();
    this.text = text;
    this.sequence = sequence;
    this.label = label;
    this.isCorrect = isCorrect;
    this.grade = grade;
    this.score = score;
    this.discount=discount;
    this.partialCredit=partialCredit;
  }
	
  public Answer(ItemTextIfc itemText, String text, Long sequence, String label,
                Boolean isCorrect, String grade, Double score, Double partialCredit, Double discount,
                Set answerFeedbackSet) {
    this.itemText = itemText;
    this.item = itemText.getItem();
    this.text = text;
    this.sequence = sequence;
    this.label = label;
    this.isCorrect = isCorrect;
    this.grade = grade;
    this.score = score;
    this.discount=discount;
    this.answerFeedbackSet = answerFeedbackSet;
    this.partialCredit=partialCredit;
  }

  public int compareTo(AnswerIfc o) {
      return sequence.compareTo(o.getSequence());
  }


  public boolean getGeneralAnswerFbIsNotEmpty(){
	  return dat.isNotEmpty(getGeneralAnswerFeedback());
  }

  public boolean getCorrectAnswerFbIsNotEmpty(){
	  return dat.isNotEmpty(getCorrectAnswerFeedback());
  }

  public boolean getIncorrectAnswerFbIsNotEmpty(){
	  return dat.isNotEmpty(getInCorrectAnswerFeedback());
  }

  public boolean getTextIsNotEmpty(){
	  return dat.isNotEmpty(getText());
  }

  protected Answer clone() throws CloneNotSupportedException {
    return (Answer)super.clone();
  }
}
