/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import org.sakaiproject.util.ResourceLoader;
 

public class AnswerBean implements Serializable{

  private static final long serialVersionUID = 7526471155622776147L;

  private String text;
  private Long sequence;
  private String label;
  private String feedback;
  private Boolean isCorrect;
  private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");

  public static final String choiceLabels = rb.getString("choice_labels"); 
  public AnswerBean() {}

  public AnswerBean(String ptext, Long pseq, String plabel, String pfdbk, Boolean pcorr, String pgrade , Float pscore) {
    this.text = ptext;
    this.sequence = pseq;
    this.label = plabel;
    this.feedback= pfdbk;
    this.isCorrect = pcorr;

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

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback= feedback;
  }

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public static String[] getChoiceLabels() {
	  String[] lables = choiceLabels.split(":");
	  return lables;
  }

}
