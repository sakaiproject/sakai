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

import lombok.Data;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

@Data
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

  public TimedAssessmentGradingModel(Long assessmentGradingId,
      int timeLimit, int timeLeft,
      Date beginDate, Date localBeginDate, 
      boolean submittedForGrade, String timerId, PublishedAssessmentFacade publishedAssessment){
    this.assessmentGradingId = assessmentGradingId;
    this.timeLimit = timeLimit;
    this.timeLeft = timeLeft;
    this.beginDate = beginDate;
    this.submittedForGrade = submittedForGrade;
    this.localBeginDate = localBeginDate;
    this.timerId = timerId;
    this.publishedAssessment = publishedAssessment;

    setExpirationDates(beginDate, localBeginDate, timeLeft);
  }

  /**
   * This method can be used to adjust an in-process assessment
   * @param newTimeLimit amount of time left starting now
   */
  public void setNewTimeLimit(final int newTimeLimit) {
    setExpirationDates(new Date(), new Date(), newTimeLimit - this.timeLimit);
    setTimeLimit(newTimeLimit);
  }

  /**
   * This method can also be used to adjust the expirations for an in-process assessment
   * @param beginDate
   * @param localBeginDate
   * @param timeLeft
   */
  private void setExpirationDates(Date beginDate, Date localBeginDate, int timeLeft) {
    this.timeLeft = timeLeft;
    this.expirationDate = new Date(beginDate.getTime() + timeLeft*1000L);
    this.bufferedExpirationDate = new Date(beginDate.getTime() + timeLeft*1000L + latencyBuffer*1000);
    this.localExpirationDate = new Date(localBeginDate.getTime() + timeLeft*1000L);
  }

}
