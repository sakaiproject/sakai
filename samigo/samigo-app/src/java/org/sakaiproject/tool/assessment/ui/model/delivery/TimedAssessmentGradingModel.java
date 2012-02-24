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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.model.delivery;

import java.util.Date;

import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimedAssessmentGradingModel
{
  private Long assessmentGradingId;
  private int timeLimit; // in seconds
  private int timeLeft; // in seconds
  private Date beginDate;
  private Date expirationDate;
  private Date bufferedExpirationDate;
  private Date localBeginDate;
  private Date localExpirationDate;
  private boolean submittedForGrade=false;
  private String timerId;
  private PublishedAssessmentFacade publishedAssessment;
  
  /* 30 sec, this is to allow JScript clock to catch up before server submit the
   * assessment for grade
   */ 
  private int latencyBuffer=30; 

  /* timedAG is removed from timedAssessmentQueue immediately if the submission for
   * grade is initiated by user OR the AutoSubmit script in the web page. If user exit
   * application unexpectedly (e.g.closing browser), server will do the submission 
   * when time is up and also clean up the timedAG in queue. So we add a transactionBuffer
   * here before server clean up
   */ 
  private int transactionBuffer=30; // 30 sec

  public TimedAssessmentGradingModel() {
  }

  public TimedAssessmentGradingModel(Long assessmentGradingId,
      int timeLimit, int timeLeft,
      Date beginDate, Date localBeginDate, 
      boolean submittedForGrade, String timerId, PublishedAssessmentFacade publishedAssessment){
    this.assessmentGradingId = assessmentGradingId;
    this.timeLimit = timeLimit;
    this.timeLeft = timeLeft;
    this.beginDate = beginDate;
    this.expirationDate = new Date(beginDate.getTime() + timeLeft*1000L);
    this.bufferedExpirationDate = new Date(beginDate.getTime() + timeLeft*1000L + latencyBuffer*1000);
    this.submittedForGrade = submittedForGrade;
    this.localBeginDate = localBeginDate;
    this.localExpirationDate = new Date(localBeginDate.getTime() + timeLeft*1000L);
    this.timerId = timerId;
    this.publishedAssessment = publishedAssessment;
  }

  public TimedAssessmentGradingModel(Long assessmentGradingId,
      int timeLimit, int timeLeft, 
      int latencyBuffer, int transactionBuffer,
      Date beginDate, Date localBeginDate, 
      boolean submittedForGrade, String timerId, PublishedAssessmentFacade publishedAssessment){
    this.assessmentGradingId = assessmentGradingId;
    this.timeLimit = timeLimit;
    this.timeLeft = timeLeft;
    this.latencyBuffer = latencyBuffer;
    this.transactionBuffer = transactionBuffer;
    this.beginDate = beginDate;
    this.expirationDate = new Date(beginDate.getTime() + timeLeft*1000L);
    this.bufferedExpirationDate = new Date(beginDate.getTime() + timeLeft*1000L + latencyBuffer*1000);
    this.submittedForGrade = submittedForGrade;
    this.localBeginDate = localBeginDate;
    this.localExpirationDate = new Date(localBeginDate.getTime() + timeLeft*1000L);
    this.timerId = timerId;
    this.publishedAssessment = publishedAssessment;
  }

  public Long getAssessmentGradingId() {
    return assessmentGradingId;
  }

  public void setAssessmentGradingId(Long assessmentGradingId) {
    this.assessmentGradingId = assessmentGradingId;
  }

  public int getTimeLimit() {
    return timeLimit;
  }

  public void setTimeLimit(int timeLimit) {
    this.timeLimit = timeLimit;
  }

  public int getTimeLeft() {
    return timeLeft;
  }

  public void setTimeLeft(int timeLeft) {
    this.timeLeft = timeLeft;
  }

  public int getLatencyBuffer() {
    return latencyBuffer;
  }

  public void setLatencyBuffer(int latencyBuffer) {
    this.latencyBuffer = latencyBuffer;
  }

  public int getTransactionBuffer() {
    return transactionBuffer;
  }

  public void setTransactionBuffer(int transactionBuffer) {
    this.transactionBuffer = transactionBuffer;
  }

  public Date getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Date beginDate) {
    this.beginDate = beginDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Date getBufferedExpirationDate() {
    return bufferedExpirationDate;
  }

  public void setBufferedExpirationDate(Date bufferedExpirationDate) {
    this.bufferedExpirationDate = bufferedExpirationDate;
  }

  public Date getLocalBeginDate() {
    return localBeginDate;
  }

  public void setLocalBeginDate(Date localBeginDate) {
    this.localBeginDate = localBeginDate;
  }

  public Date getLocalExpirationDate() {
    return localExpirationDate;
  }

  public void setLocalExpirationDate(Date localExpirationDate) {
    this.localExpirationDate = localExpirationDate;
  }

  public boolean getSubmittedForGrade() {
    return submittedForGrade;
  }

  public void setSubmittedForGrade(boolean submittedForGrade) {
    this.submittedForGrade = submittedForGrade;
  }

  public String getTimerId(){
    return timerId;
  }

  public void setTimerId(String timerId) {
    this.timerId = timerId;
  }
  
  public PublishedAssessmentFacade getPublishedAssessment(){
	  return publishedAssessment;
  }

  public void setPublishedAssessment(PublishedAssessmentFacade publishedAssessment) {
	  this.publishedAssessment = publishedAssessment;
  }
}
