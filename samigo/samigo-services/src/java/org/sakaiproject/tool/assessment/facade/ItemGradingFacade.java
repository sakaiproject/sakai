/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/ItemGradingFacade.java $
 * $Id: ItemGradingFacade.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
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

package org.sakaiproject.tool.assessment.facade;

//import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
//import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
//import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import java.util.Date;
import java.lang.Float;


public class ItemGradingFacade
    implements ItemGradingIfc
// need to implement org.osid.assessment.ItemTaken in the future
// - daisyf 10/11/04
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 6779809228748217781L;
private Long itemGradingId;
  private Long assessmentGradingId;
  private Long publishedItemId;
  private Long publishedItemTextId;
    //private ItemDataIfc publishedItem;
    //private ItemTextIfc publishedItemText;
  private String agentId;
  private Long publishedAnswerId;
  private AnswerIfc publishedAnswer;
  private String rationale;
  private String answerText;
  private Date submittedDate;
  private Float autoScore;
  private Float overrideScore;
  private String comments;
  private String gradedBy;
  private Date gradedDate;
  private Boolean review;
  private Integer attemptsRemaining;
  
  public ItemGradingFacade() {
  }
  public Long getItemGradingId() {
    return itemGradingId;
  }
  public void setItemGradingId(Long itemGradingId) {
    this.itemGradingId = itemGradingId;
  }
  public Long getAssessmentGradingId() {
    return assessmentGradingId;
  }
  public void setAssessmentGradingId(Long assessmentGradingId) {
    this.assessmentGradingId = assessmentGradingId;
  }

  public Long getPublishedItemId() {
    return publishedItemId;
  }
  public void setPublishedItemId(Long publishedItemId) {
    this.publishedItemId = publishedItemId;
  }

  public Long getPublishedItemTextId() {
    return publishedItemTextId;
  }
  public void setPublishedItemTextId(Long publishedItemTextId) {
    this.publishedItemTextId = publishedItemTextId;
  }

    /*
  public ItemDataIfc getPublishedItem() {
    return publishedItem;
  }
  public void setPublishedItem(ItemDataIfc publishedItem) {
    this.publishedItem = publishedItem;
  }
  public ItemTextIfc getPublishedItemText() {
    return publishedItemText;
  }
  public void setPublishedItemText(ItemTextIfc publishedItemText) {
    this.publishedItemText = publishedItemText;
  }
    */

  public String getAgentId() {
    return agentId;
  }
  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }
  public Long getPublishedAnswerId() {
    return publishedAnswerId;
  }
  public void setPublishedAnswerId(Long publishedAnswerId) {
    this.publishedAnswerId = publishedAnswerId;
  }
  public AnswerIfc getPublishedAnswer() {
    return publishedAnswer;
  }
  public void setPublishedAnswer(AnswerIfc publishedAnswer) {
    this.publishedAnswer = publishedAnswer;
  }
  public String getRationale() {
    return rationale;
  }
  public void setRationale(String rationale) {
    this.rationale = rationale;
  }
  public String getAnswerText() {
    return answerText;
  }
  public void setAnswerText(String answerText) {
    this.answerText = answerText;
  }
  public Date getSubmittedDate() {
    return submittedDate;
  }
  public void setSubmittedDate(Date submittedDate) {
    this.submittedDate = submittedDate;
  }
  public Float getAutoScore() { 
    return autoScore;
  }
  public void setAutoScore(Float autoScore) {
    this.autoScore = autoScore;
  }
  public Float getOverrideScore() {  
    return overrideScore;
  }
  public void setOverrideScore(Float overrideScore) {
    this.overrideScore = overrideScore;
  }
  public String getComments() {
    return comments;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
  public String getGradedBy() {
    return gradedBy;
  }
  public void setGradedBy(String gradedBy) {
    this.gradedBy = gradedBy;
  }
  public Date getGradedDate() {
    return gradedDate;
  }
  public void setGradedDate(Date gradedDate) {
    this.gradedDate = gradedDate;
  }

  public Boolean getReview() {
    return review;
  }

  public void setReview(Boolean newReview) {
   review = newReview;
  }
  
  public Integer getAttemptsRemaining() {
	    return attemptsRemaining;
  }

  public void setAttemptsRemaining(Integer attemptsRemaining) {
	    this.attemptsRemaining = attemptsRemaining;
  }
}
