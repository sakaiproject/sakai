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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.delivery;
import java.io.Serializable;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;

/**
 * @author casong
 * @author esmiley@stanford.edu
 * $Id$
 */
public class FeedbackComponent implements Serializable
{
	  /** Use serialVersionUID for interoperability. */
	  private final static long serialVersionUID = -1090852048737428722L;

  private boolean showCorrectResponse;
  private boolean showGraderComment;
  private boolean showItemLevel;
  private boolean showQuestion;
  private boolean showResponse;
  private boolean showSelectionLevel;
  private boolean showStats;
  private boolean showImmediate;
  private boolean showOnSubmission;
  private boolean showStudentScore;
  private boolean showStudentQuestionScore;
  private boolean showDateFeedback;
  private boolean showNoFeedback;

  /**
   *
   * @return true if it is correct
   */
  public boolean getShowCorrectResponse()
  {
    return showCorrectResponse;
  }

  /**
   *
   * @return
   */
  public boolean getShowStudentScore(){
    return this.showStudentScore;
  }

 /**
   *
   * @return
   */
  public boolean getShowStudentQuestionScore(){
    return this.showStudentQuestionScore;
  }

  /**
   * If feedback is displayed on the item level.
   * @return if feedback is displayed on the item level.
   */
  public boolean getShowItemLevel(){
    return this.showItemLevel;
  }

  /**
   * If feedback is displayed on the selection level.
   * @return if feedback is displayed on the selection level.
   */
  public boolean getShowSelectionLevel(){
    return this.showSelectionLevel;
  }

  /**
   * If feedback is displayed on the comment level.
   * @return
   */
  public boolean getShowGraderComment(){
    return this.showGraderComment;
  }

  /**
   * If statistics are shown to student.
   * @return
   */
  public boolean getShowStats(){
    return this.showStats;
  }

  /**
   * If informaton  is displayed on the question.
   * @return if information is displayed on the question.
   */
  public boolean getShowQuestion(){
    return this.showQuestion;
  }

  /**
   * If response is displayed.
   * @return if response is displayed
   */
  public boolean getShowResponse(){
    return this.showResponse;
  }

  /**
   * If feedback is displayed on the immediate level.
   * @return true if feedback is displayed on immediately.
   */
  public boolean getShowImmediate() {
    return showImmediate;
  }

  /**
   * If feedback is displayed on the immediate level.
   * @param showImmediate if feedback is displayed on immediately.
   */
  public void setShowImmediate(boolean showImmediate) {
    this.showImmediate = showImmediate;
  }

  /**
   * If feedback is displayed on submission.
   * @return true if feedback is displayed on submission.
   */
  public boolean getShowOnSubmission() {
    return showOnSubmission;
  }

  /**
   * If feedback is displayed after submission.
   * @param showAfterSubmission if feedback is displayed on submission.
   */
  public void setShowOnSubmission(boolean showOnSubmission) {
    this.showOnSubmission = showOnSubmission;
  }
  
  /**
   * If feedback is displayed on date.
   * @return if feedback is displayed on the date level.
   */
  public boolean getShowDateFeedback() {
    return showDateFeedback;
  }

  /**
   * If feedback is displayed on the date level.
   * @param showDateFeedback if feedback is displayed on the date level.
   */
  public void setShowDateFeedback(boolean showDateFeedback) {
    this.showDateFeedback = showDateFeedback;
  }

  /**
   * If feedback is displayed on NO level.
   * @return true if NO feedback is displayed.
   */
  public boolean getShowNoFeedback() {
    return showNoFeedback;
  }

  /**
   * If feedback is displayed on NO level.
   * @param showNoFeedback NO feedback is displayed?
   */
  public void setShowNoFeedback(boolean showNoFeedback) {
    this.showNoFeedback = showNoFeedback;
  }
  /**
   * If correct answer is displayed.
   * @param showCorrectResponse If correct answer is displayed.
   */
  public void setShowCorrectResponse(boolean showCorrectResponse)
  {
    this.showCorrectResponse = showCorrectResponse;
  }

  /**
   * Show comments from grader?
   * @param showGraderComment
   */
  public void setShowGraderComment(boolean showGraderComment)
  {
    this.showGraderComment = showGraderComment;
  }

  /**
   * If feedback is displayed on the item level.
   * @param showItemLevel
   */
  public void setShowItemLevel(boolean showItemLevel)
  {
    this.showItemLevel = showItemLevel;
  }

  /**
   * Show question?
   * @param showQuestion
   */
  public void setShowQuestion(boolean showQuestion)
  {
    this.showQuestion = showQuestion;
  }
  /**
   * Show response?
   * @param showResponse
   */
  public void setShowResponse(boolean showResponse)
  {
    this.showResponse = showResponse;
  }
  /**
   * If feedback is displayed on the selection level.
   * @param showSelectionLevel
   */
  public void setShowSelectionLevel(boolean showSelectionLevel)
  {
    this.showSelectionLevel = showSelectionLevel;
  }

  /**
   * Show statistics?
   * @param showStats
   */
  public void setShowStats(boolean showStats)
  {
    this.showStats = showStats;
  }

  /**
   * Show score?
   * @param showStudentScore
   */
  public void setShowStudentScore(boolean showStudentScore)
  {
    this.showStudentScore = showStudentScore;
  }

  /**
   * Show Question score?
   * @param showStudentQuestionScore
   */
  public void setShowStudentQuestionScore(boolean showStudentQuestionScore)
  {
    this.showStudentQuestionScore = showStudentQuestionScore;
  }

  public void setAssessmentFeedback(AssessmentFeedbackIfc feedback){
    setShowCorrectResponse(feedback.getShowCorrectResponse().booleanValue());
    setShowGraderComment(feedback.getShowGraderComments().booleanValue());
    setShowItemLevel(feedback.getShowQuestionLevelFeedback().booleanValue());
    setShowQuestion(feedback.getShowQuestionText().booleanValue());
    setShowResponse(feedback.getShowStudentResponse().booleanValue());
    setShowSelectionLevel(feedback.getShowSelectionLevelFeedback().booleanValue());
    setShowStats(feedback.getShowStatistics().booleanValue());
    setShowStudentScore(feedback.getShowStudentScore().booleanValue());
    if (feedback.getShowStudentQuestionScore()!=null)
      setShowStudentQuestionScore(feedback.getShowStudentQuestionScore().booleanValue());
    else
      setShowStudentQuestionScore(false);
    Integer feedbackDelivery = feedback.getFeedbackDelivery();
    setShowDateFeedback(AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackDelivery));
    setShowImmediate(AssessmentFeedbackIfc.IMMEDIATE_FEEDBACK.equals(feedbackDelivery));
    setShowOnSubmission(AssessmentFeedbackIfc.FEEDBACK_ON_SUBMISSION.equals(feedbackDelivery));
    setShowNoFeedback(AssessmentFeedbackIfc.NO_FEEDBACK.equals(feedbackDelivery));
  }

}
