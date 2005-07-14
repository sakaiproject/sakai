/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.data.ifc.grading;
import java.util.Date;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

public interface ItemGradingIfc
    extends java.io.Serializable{

  Long getItemGradingId();

  void setItemGradingId(Long itemGradingId);

  AssessmentGradingIfc getAssessmentGrading();

  void setAssessmentGrading(AssessmentGradingIfc assessmentGrading);

  ItemDataIfc getPublishedItem();

  void setPublishedItem(ItemDataIfc publishedItem);

  ItemTextIfc getPublishedItemText();

  void setPublishedItemText(ItemTextIfc publishedItemText);

  String getAgentId();

  void setAgentId(String agentId);

  // answer stores the answer selected by students for
  // multiple choice, multiple select and multiple response question
  // when autograding is possible
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
}
