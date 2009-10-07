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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.queue.delivery;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.cover.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id: SubmitTimedAssessmentThread.java 1294 2005-08-19 17:22:35Z esmiley@stanford.edu $
 */

public class SubmitTimedAssessmentThread extends TimerTask
{

  private static Log log = LogFactory.getLog(SubmitTimedAssessmentThread.class);
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
          AssessmentGradingData ag = service.load(timedAG.getAssessmentGradingId().toString());
          ag.setForGrade(Boolean.TRUE);
          ag.setTimeElapsed(Integer.valueOf(timedAG.getTimeLimit()));
          ag.setStatus(AssessmentGradingIfc.SUBMITTED); // this will change status 0 -> 1
          ag.setIsLate(islate(ag.getPublishedAssessmentId()));
          ag.setSubmittedDate(new Date());
          // SAK-7302, users taking a timed assessment may exit without completing the assessment
          // set these two scores to 0 instaed of null
    	  if (ag.getFinalScore() == null) ag.setFinalScore(Float.valueOf("0"));
    	  if (ag.getTotalAutoScore() == null) ag.setTotalAutoScore(Float.valueOf("0"));
    	  service.completeItemGradingData(ag);
          service.saveOrUpdateAssessmentGrading(ag);
          PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
          String siteId = publishedAssessmentService.getPublishedAssessmentOwner(ag.getPublishedAssessmentId());
          EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.thread_submit", "submissionId=" + ag.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));
          notifyGradebookByScoringType(ag, timedAG.getPublishedAssessment());
          log.debug("**** 4a. time's up, timeLeft+latency buffer reached, saved to DB");
        }
      }
      else{ //submitted, remove from queue if transaction buffer is also reached
        if (currentTime > (bufferedExpirationTime + timedAG.getTransactionBuffer()*1000)){
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

  private void notifyGradebookByScoringType(AssessmentGradingIfc ag, PublishedAssessmentFacade publishedAssessment){
	  if (publishedAssessment == null || publishedAssessment.getEvaluationModel() == null) {
		  // should not come to here
		  log.debug("publishedAssessment is null or publishedAssessment.getEvaluationModel() is null");
		  return;
	  }
	  if (publishedAssessment.getEvaluationModel().getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
		  AssessmentGradingIfc assessmentGrading = ag; // data is the last submission
		  GradingService g = new GradingService();
		  // need to decide what to tell gradebook
		  if (publishedAssessment.getEvaluationModel().getScoringType().equals(EvaluationModelIfc.HIGHEST_SCORE)) {
			  assessmentGrading = g.getHighestSubmittedAssessmentGrading(publishedAssessment.getPublishedAssessmentId().toString(), ag.getAgentId());
		  }
		  Session s = SessionManager.getCurrentSession();
		  if (s != null)
		  {
			  s.setUserId(assessmentGrading.getAgentId());
			  g.notifyGradebook(assessmentGrading, publishedAssessment);
		  }
	  }
  }
}
