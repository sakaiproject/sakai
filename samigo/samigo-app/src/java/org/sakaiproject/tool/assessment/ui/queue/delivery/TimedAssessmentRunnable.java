/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004 - 2017 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.queue.delivery;

import java.util.concurrent.ScheduledFuture;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p>Title: TimedAssessmentRunnable</p>
 * <p>Description: A task that monitors and submits a timed assessment</p>
 */
@Slf4j
public class TimedAssessmentRunnable implements Runnable {

  private static final ResourceBundle eventLogMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.EventLogMessages");
  private long timedAGId;
  TimedAssessmentQueue queue;


  public TimedAssessmentRunnable(long id){
    this.timedAGId = id;
    this.queue = TimedAssessmentQueue.getInstance();
  }

  
  public void run(){
    try {
      TimedAssessmentGradingModel timedAG = this.queue.get(this.timedAGId);
      String serverName = ServerConfigurationService.getServerName();

      boolean submitted = timedAG.getSubmittedForGrade();
      long bufferedExpirationTime = timedAG.getBufferedExpirationDate().getTime(); // in millesec
      long currentTime = (new Date()).getTime(); // in millisec
  
      log.debug("SAMIGO_TIMED_ASSESSMENT:TICKTOCK ID:" + this.timedAGId + 
         " submitted:" + submitted + 
         " time_left:" + (bufferedExpirationTime-currentTime));
  
      if (!submitted){
        if (currentTime > bufferedExpirationTime){ // time's up, i.e. timeLeft + latency buffer reached
          timedAG.setSubmittedForGrade(true);
          // set all the properties right and persist status to DB
          GradingService service = new GradingService();
          AssessmentGradingData ag = service.load(String.valueOf(this.timedAGId), false);

          log.info("SAMIGO_TIMED_ASSESSMENT:SUBMIT ID:" + this.timedAGId + 
             " userId:" + ag.getAgentId());

          if (!ag.getForGrade()) {
            Date submitDate = new Date();

            log.info("SAMIGO_TIMED_ASSESSMENT:SUBMIT:FORGRADE ID:" + this.timedAGId + 
               " userId:" + ag.getAgentId());


            // Create a new session here so this is associated in the database with the correct userid
            UsageSession usageSession = UsageSessionService.startSession(ag.getAgentId(), serverName, "TimedAssessmentRunnable");

            // Change user id for the Gradebook update (if required) and so the event is associated with the correct userid
            Session session = SessionManager.getCurrentSession();
            if (session == null) {
            	session = SessionManager.startSession();
            }

            session.setUserId(ag.getAgentId());
            session.setUserEid(UserDirectoryService.getUserEid(ag.getAgentId()));
            

            ag.setForGrade(Boolean.TRUE);
            ag.setTimeElapsed(timedAG.getTimeLimit());
            ag.setStatus(AssessmentGradingData.SUBMITTED); // this will change status 0 -> 1
            ag.setIsLate(islate(ag.getPublishedAssessmentId()));
            ag.setSubmittedDate(submitDate);
            // SAK-7302, users taking a timed assessment may exit without completing the assessment
            // set these two scores to 0 instaed of null
            if (ag.getFinalScore() == null) ag.setFinalScore(Double.valueOf("0"));
            if (ag.getTotalAutoScore() == null) ag.setTotalAutoScore(Double.valueOf("0"));

            service.completeItemGradingData(ag);
            service.saveOrUpdateAssessmentGrading(ag);

            EventLogService eventService = new EventLogService();
            EventLogFacade eventLogFacade = new EventLogFacade();
            List eventLogDataList = eventService.getEventLogData(ag.getAssessmentGradingId());
            EventLogData eventLogData= (EventLogData) eventLogDataList.get(0);
            eventLogData.setErrorMsg(eventLogMessages.getString("timer_submit"));
            eventLogData.setEndDate(submitDate);
            if(eventLogData.getStartDate() != null) {
              double minute= 1000*60;
              int eclipseTime = (int)Math.ceil(((submitDate.getTime() - eventLogData.getStartDate().getTime())/minute));
              eventLogData.setEclipseTime(eclipseTime); 
            } else {
              eventLogData.setEclipseTime(null); 
              eventLogData.setErrorMsg(eventLogMessages.getString("error_take"));
            }
            eventLogFacade.setData(eventLogData);
            eventService.saveOrUpdateEventLog(eventLogFacade);
            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            String siteId = publishedAssessmentService.getPublishedAssessmentOwner(ag.getPublishedAssessmentId());

            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_THREAD,
               "siteId=" + siteId + ", submissionId=" + ag.getAssessmentGradingId(),
               siteId,
               true,
               NotificationService.NOTI_REQUIRED));

            Map<String, Object> notiValues = new HashMap<>();
            notiValues.put("assessmentGradingID", ag.getAssessmentGradingId());
            notiValues.put("userID", ag.getAgentId());
            notiValues.put("submissionDate", submitDate.toString());
            notiValues.put("publishedAssessmentID", ag.getPublishedAssessmentId());

            PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(ag.getPublishedAssessmentId().toString());

            String confirmationNumber = ag.getAssessmentGradingId() + 
               "-" + publishedAssessment.getPublishedAssessmentId() + 
               "-" + ag.getAgentId() + 
               "-" + ag.getSubmittedDate().toString();

            notiValues.put( "confirmationNumber", confirmationNumber );

            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD,
               notiValues.toString(),
               siteId,
               true,
               SamigoConstants.NOTI_EVENT_ASSESSMENT_TIMED_SUBMITTED));

            GradingService g = new GradingService();
            g.notifyGradebookByScoringType(ag, publishedAssessment);

            log.info("SAMIGO_TIMED_ASSESSMENT:SUBMIT:FORGRADE assessmentId:" + eventLogData.getAssessmentId() + 
               " userEid:" + eventLogData.getUserEid() + 
               " siteId:" + siteId + 
               " submissionId:" + ag.getAssessmentGradingId());
            //Invalidate the session
            UsageSessionService.logout();
          }
        }
      } else { //submitted, remove from queue if transaction buffer is also reached
        if (currentTime > (bufferedExpirationTime + timedAG.getTransactionBuffer()*1000L)){
          this.queue.remove(this.timedAGId);
        }
      }
    } catch (Exception ex) {
      log.error("SAMIGO_TIMED_ASSESSMENT:SUBMIT:ERROR - " + ex);
      this.queue.remove(this.timedAGId);
    }
  }


  private Boolean islate(Long publishedId) {
    PublishedAssessmentService service = new PublishedAssessmentService();
    PublishedAssessmentData pub = service.getBasicInfoOfPublishedAssessment(publishedId.toString());
    if (pub.getDueDate()!=null && pub.getDueDate().before(new Date())) {
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

}


