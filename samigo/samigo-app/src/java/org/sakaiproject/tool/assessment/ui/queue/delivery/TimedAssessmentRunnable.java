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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Title: TimedAssessmentRunnable</p>
 * <p>Description: A task that monitors and submits a timed assessment</p>
 */
@Slf4j
public class TimedAssessmentRunnable implements Runnable {

  private static final ResourceBundle eventLogMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.EventLogMessages");

  private EventTrackingService eventTrackingService;
  private ThreadLocalManager threadLocalManager;
  private ServerConfigurationService serverConfigurationService;
  private SessionManager sessionManager;
  private UsageSessionService usageSessionService;

  private long timedAGId;
  TimedAssessmentQueue queue;


  public TimedAssessmentRunnable(long id){
    eventTrackingService = ComponentManager.get(EventTrackingService.class);
    threadLocalManager = ComponentManager.get(ThreadLocalManager.class);
    serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
    sessionManager = ComponentManager.get(SessionManager.class);
    usageSessionService = ComponentManager.get(UsageSessionService.class);

    this.timedAGId = id;
    this.queue = TimedAssessmentQueue.getInstance();
  }

  
  public void run(){
    try {
      TimedAssessmentGradingModel timedAG = this.queue.get(this.timedAGId);
      String serverName = serverConfigurationService.getServerName();

      boolean submitted = timedAG.isSubmittedForGrade();
      long bufferedExpirationTime = timedAG.getBufferedExpirationDate().getTime(); // in millesec
      long currentTime = (new Date()).getTime(); // in millisec
  
      log.debug("SAMIGO_TIMED_ASSESSMENT:TICKTOCK ID:{} submitted:{} time_left:{}", this.timedAGId, submitted, bufferedExpirationTime - currentTime);
  
      if (!submitted){
        if (currentTime > bufferedExpirationTime){ // time's up, i.e. timeLeft + latency buffer reached

          // set all the properties right and persist status to DB
          GradingService service = new GradingService();
          AssessmentGradingData ag = service.load(String.valueOf(this.timedAGId), false);
          PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
          String siteId = publishedAssessmentService.getPublishedAssessmentOwner(ag.getPublishedAssessmentId());
          PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessment(ag.getPublishedAssessmentId().toString());
          ExtendedTimeDeliveryService assessmentExtended = new ExtendedTimeDeliveryService(publishedAssessment, ag.getAgentId());
          Integer extendedTime = null;

          // The specific student has more time than the thread knows about
          if (assessmentExtended != null && assessmentExtended.hasExtendedTime()) {
            extendedTime = assessmentExtended.getTimeLimit();
          }
          // Maybe the instructor extended the time allowed after the student began?
          else if (publishedAssessment != null && publishedAssessment.getTimeLimit() != null) {
            extendedTime = publishedAssessment.getTimeLimit();
          }

          // Did the instructor add more time after student started assessment?
          if (extendedTime != null && extendedTime > timedAG.getTimeLimit()) {
            log.info("SAMIGO_TIMED_ASSESSMENT:EXTENDED ID:{} old_limit:{}, extended_time:{}", this.timedAGId, timedAG.getTimeLimit(), extendedTime);
            timedAG.setNewTimeLimit(extendedTime);
            return;
          }

          log.info("SAMIGO_TIMED_ASSESSMENT:SUBMIT ID:{} userId:{}", this.timedAGId, ag.getAgentId());

          timedAG.setSubmittedForGrade(true);

          if (!ag.getForGrade()) {
            Date submitDate = new Date();

            log.info("SAMIGO_TIMED_ASSESSMENT:SUBMIT:FORGRADE ID:{} userId:{}", this.timedAGId, ag.getAgentId());


            // Create a new session here so this is associated in the database with the correct userid
            usageSessionService.startSession(ag.getAgentId(), serverName, "TimedAssessmentRunnable");

            // Change user id for the Gradebook update (if required) and so the event is associated with the correct userid
            Session session = sessionManager.getCurrentSession();
            if (session == null) {
            	session = sessionManager.startSession();
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
            EventLogData eventLogData;
            // There should already be data for this attempt in the db but there are edge cases where it could be missing
            List<EventLogData> eventLogDataList = eventService.getEventLogData(ag.getAssessmentGradingId());
            if (eventLogDataList != null && !eventLogDataList.isEmpty()) {
              eventLogData = eventLogDataList.get(0);
            }
            else {
              eventLogData = new EventLogData();
              eventLogData.setAssessmentId(ag.getPublishedAssessmentId());
              eventLogData.setProcessId(ag.getAssessmentGradingId());
              eventLogData.setStartDate(null);
              eventLogData.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(publishedAssessment.getTitle()));
              eventLogData.setUserEid(UserDirectoryService.getUserEid(ag.getAgentId()));
              eventLogData.setSiteId(siteId);
            }
            eventLogData.setErrorMsg(eventLogMessages.getString("timer_submit"));
            eventLogData.setEndDate(submitDate);
            if(eventLogData.getStartDate() != null) {
              double minute= 1000*60;
              int eclipseTime = (int)Math.round(((submitDate.getTime() - eventLogData.getStartDate().getTime())/minute));
              eventLogData.setEclipseTime(eclipseTime); 
            } else {
              eventLogData.setEclipseTime(null); 
              eventLogData.setErrorMsg(eventLogMessages.getString("error_take"));
            }
            eventLogFacade.setData(eventLogData);
            eventService.saveOrUpdateEventLog(eventLogFacade);

            Map<String, Object> notiValues = new HashMap<>();
            notiValues.put("assessmentGradingID", ag.getAssessmentGradingId());
            notiValues.put("userID", ag.getAgentId());
            notiValues.put("submissionDate", submitDate.toString());
            notiValues.put("publishedAssessmentID", ag.getPublishedAssessmentId());

            String confirmationNumber = ag.getAssessmentGradingId() + 
               "-" + publishedAssessment.getPublishedAssessmentId() + 
               "-" + ag.getAgentId() + 
               "-" + ag.getSubmittedDate().toString();

            notiValues.put( "confirmationNumber", confirmationNumber );

            eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD,
               notiValues.toString(),
               siteId,
               true,
               SamigoConstants.NOTI_EVENT_ASSESSMENT_TIMED_SUBMITTED));

            GradingService g = new GradingService();
            g.notifyGradebookByScoringType(ag, publishedAssessment);

            log.info("SAMIGO_TIMED_ASSESSMENT:SUBMIT:FORGRADE assessmentId:{} userEid:{} siteId:{} submissionId:{}",
                    eventLogData.getAssessmentId(), eventLogData.getUserEid(), siteId, ag.getAssessmentGradingId());
          }
        }
      } else { //submitted, remove from queue if transaction buffer is also reached
        if (currentTime > bufferedExpirationTime + timedAG.getTransactionBuffer() * 1000L){
          this.queue.remove(this.timedAGId);
        }
      }
    } catch (Exception ex) {
      log.warn("SAMIGO_TIMED_ASSESSMENT:SUBMIT:ERROR - {}", ex.getMessage(), ex);
      this.queue.remove(this.timedAGId);
    } finally {
      usageSessionService.logout();
      threadLocalManager.clear();
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


