/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.cover.SessionManager;

import java.util.ResourceBundle;
/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */

public class SubmitTimedAssessmentThread extends TimerTask
{

  private static Log log = LogFactory.getLog(SubmitTimedAssessmentThread.class);
  private static ResourceBundle eventLogMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.EventLogMessages");
  
  public SubmitTimedAssessmentThread(){}

  public void run(){
    log.debug("run!!");
    ArrayList<TimedAssessmentGradingModel> removeTimedAGList = new ArrayList<TimedAssessmentGradingModel>();
    // get the queue, go through the queue till it is empty     
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    Iterator iter = queue.iterator();
    while (iter.hasNext()){
      TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)iter.next();
      log.debug("****** going through timedAG in queue, timedAG"+timedAG);
      boolean submitted = timedAG.getSubmittedForGrade();
      long bufferedExpirationTime = timedAG.getBufferedExpirationDate().getTime(); // in millesec
      long currentTime = (new Date()).getTime(); // in millisec

      log.debug("****** submitted="+submitted);
      log.debug("****** currentTime="+currentTime);
      log.debug("****** bufferedExpirationTime="+bufferedExpirationTime);
      log.debug("****** expired="+(currentTime > bufferedExpirationTime));
      if (!submitted){
        if (currentTime > bufferedExpirationTime){ // time's up, i.e. timeLeft + latency buffer reached
          timedAG.setSubmittedForGrade(true);
          // set all the properties right and persist status to DB
          GradingService service = new GradingService();
          AssessmentGradingData ag = service.load(timedAG.getAssessmentGradingId().toString(), false);
          if (!ag.getForGrade().booleanValue()) {
            // Change user id for the Gradebook update (if required) and so the event is associated with the correct userid
            Session s = SessionManager.getCurrentSession();
            if (s != null) {
              s.setUserId(ag.getAgentId());
            }

            ag.setForGrade(Boolean.TRUE);
            ag.setTimeElapsed(Integer.valueOf(timedAG.getTimeLimit()));
            ag.setStatus(AssessmentGradingData.SUBMITTED); // this will change status 0 -> 1
            ag.setIsLate(islate(ag.getPublishedAssessmentId()));
	    Date submitDate = new Date();
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
          if(submitDate != null && eventLogData.getStartDate() != null) {
        	  double minute= 1000*60;
        	  int eclipseTime = (int)Math.ceil(((submitDate.getTime() - eventLogData.getStartDate().getTime())/minute));
        	  eventLogData.setEclipseTime(Integer.valueOf(eclipseTime)); 
          } else {
        	  eventLogData.setEclipseTime(null); 
        	  eventLogData.setErrorMsg(eventLogMessages.getString("error_take"));
          }
            eventLogFacade.setData(eventLogData);
            eventService.saveOrUpdateEventLog(eventLogFacade);

            PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
            String siteId = publishedAssessmentService.getPublishedAssessmentOwner(ag.getPublishedAssessmentId());

            EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.thread_submit", "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + ag.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));

            Map<String, Object> notiValues = new HashMap<String, Object>();

            notiValues.put("assessmentGradingID", ag.getAssessmentGradingId());
            notiValues.put("userID", ag.getAgentId());
            notiValues.put("submissionDate", submitDate.toString());
            notiValues.put("publishedAssessmentID", ag.getPublishedAssessmentId());

            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TIMED_SUBMITTED, notiValues.toString(), siteId, true, SamigoConstants.NOTI_EVENT_ASSESSMENT_TIMED_SUBMITTED));
            notifyGradebookByScoringType(ag, timedAG.getPublishedAssessment());
            log.debug("**** 4a. time's up, timeLeft+latency buffer reached, saved to DB");
            log.info("Submitted timed assessment assessmentId=" + eventLogData.getAssessmentId() + " userEid=" + eventLogData.getUserEid() + " siteId=" + siteId + ", submissionId=" + ag.getAssessmentGradingId());
          }
        }
      }
      else{ //submitted, remove from queue if transaction buffer is also reached
        if (currentTime > (bufferedExpirationTime + timedAG.getTransactionBuffer()*1000L)){
          //queue.remove(timedAG);
          removeTimedAGList.add(timedAG);
          log.debug("**** 4b. transaction buffer reached");
        }
      }
    }
    Iterator i = removeTimedAGList.iterator();
    while(i.hasNext()) {
    	log.debug("removing from queue");
    	queue.remove((TimedAssessmentGradingModel)i.next());
    }
  }

  private Boolean islate(Long publishedId) {
	PublishedAssessmentService service = new PublishedAssessmentService();
	PublishedAssessmentData pub = service.getBasicInfoOfPublishedAssessment(publishedId.toString());
	if (pub.getDueDate()!=null && pub.getDueDate().before(new Date()))
		return Boolean.TRUE;
	else
		return Boolean.FALSE;
  }

  private void notifyGradebookByScoringType(AssessmentGradingData ag, PublishedAssessmentFacade publishedAssessment){
	  if (publishedAssessment == null || publishedAssessment.getEvaluationModel() == null) {
		  // should not come to here
		  log.debug("publishedAssessment is null or publishedAssessment.getEvaluationModel() is null");
		  return;
	  }
	  if (publishedAssessment.getEvaluationModel().getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
		  AssessmentGradingData assessmentGrading = ag; // data is the last submission
		  GradingService g = new GradingService();
		  // need to decide what to tell gradebook
		  if (publishedAssessment.getEvaluationModel().getScoringType().equals(EvaluationModelIfc.HIGHEST_SCORE)) {
			  assessmentGrading = g.getHighestSubmittedAssessmentGrading(publishedAssessment.getPublishedAssessmentId().toString(), ag.getAgentId());
		  }
		  g.notifyGradebook(assessmentGrading, publishedAssessment);
	  }
  }
}
