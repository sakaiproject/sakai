/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.grading;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;

public interface ItemGradingIfc
    extends java.io.Serializable{

  Long getItemGradingId();

  void setItemGradingId(Long itemGradingId);

  Long getAssessmentGradingId();

  void setAssessmentGradingId(Long assessmentGradingId);

  Long getPublishedItemId();

  void setPublishedItemId(Long publishedItemId);

  Long getPublishedItemTextId();

  void setPublishedItemTextId(Long publishedItemTextId);

    /*
  AssessmentGradingIfc getAssessmentGrading();

  void setAssessmentGrading(AssessmentGradingIfc assessmentGrading);
    */
    /*
  ItemDataIfc getPublishedItem();

  void setPublishedItem(ItemDataIfc publishedItem);

  ItemTextIfc getPublishedItemText();

  void setPublishedItemText(ItemTextIfc publishedItemText);
    */

  String getAgentId();

  void setAgentId(String agentId);

  // answer stores the answer selected by students for
  // multiple choice, multiple select and multiple response question
  // when autograding is possible
  void setPublishedAnswerId(Long publishedAnswerId);

  Long getPublishedAnswerId();

  AnswerIfc getPublishedAnswer();

  void setPublishedAnswer(AnswerIfc PublishedAnswer);

  // rationale stores the reason that the student provided for their choice of
  // the selected answer
  String getRationale();

  void setRationale(String rationale);

  // answer text stored answer submitted for SAQ, audio response, file upload
  // when autograding is not possible and grader must read the answer before
  // score can be awarded.
  String getAnswerText();

  void setAnswerText(String answerText);

  Date getSubmittedDate();

  void setSubmittedDate(Date submittedDate);

  Float getAutoScore();

  void setAutoScore(Float autoScore);

  Float getOverrideScore();

  void setOverrideScore(Float overrideScore);

  // comments are added by grader
  String getComments();

  void setComments(String comments);

  String getGradedBy();

  void setGradedBy(String gradedBy);

  Date getGradedDate();

  void setGradedDate(Date gradedDate);

  Boolean getReview();

  void setReview(Boolean review);
  
  public Integer getAttemptsRemaining();
  
  public void setAttemptsRemaining(Integer attemptsRemaining);
  
  public Set getItemGradingAttachmentSet();

  public void setItemGradingAttachmentSet(Set itemGradingAttachmentSet);

  public List getItemGradingAttachmentList();

}