/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/delivery/DeliveryActionListener.java $
 * $Id: DeliveryActionListener.java 13044 2006-07-28 03:23:48Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;


public class LinearAccessDeliveryActionListener extends DeliveryActionListener
  implements ActionListener
{
  private static Log log = LogFactory.getLog(LinearAccessDeliveryActionListener.class);

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("LinearAccessDeliveryActionListener.processAction() ");
      // get managed bean
      DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
      // set publishedId, note that id can be changed by isPreviewingMode()
      String id = getPublishedAssessmentId(delivery);
      String agent = getAgentString();

      // Clear elapsed time, set not timed out
      clearElapsedTime(delivery);

      // get assessment from deliveryBean if id matches. otherwise, this is the 1st time
      // that DeliveryActionListener is called, so pull it from DB
      PublishedAssessmentFacade publishedAssessment = getPublishedAssessment(delivery, id);
      // set show student score
      setShowStudentScore(delivery, publishedAssessment);
      setShowStudentQuestionScore(delivery, publishedAssessment);
      setDeliverySettings(delivery, publishedAssessment);
      
      // itemGradingHash will end up with 
      // (Long publishedItemId, ArrayList itemGradingDatas) and
      // (String "sequence"+itemId, Integer sequence) and
      // (String "items", Long itemscount)
      GradingService service = new GradingService();
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      AssessmentGradingData ag = null;
      
      // this returns a HashMap with (publishedItemId, itemGrading)
      HashMap itemGradingHash = service.getLastItemGradingData(id, agent); 
      
      if (itemGradingHash!=null && itemGradingHash.size()>0){
    	  log.debug("itemGradingHash!=null && itemGradingHash.size()>0");
    	  ag = setAssessmentGradingFromItemData(delivery, itemGradingHash, true);
    	  setAttemptDateIfNull(ag);
      }
      else{
    	  ag = service.getLastSavedAssessmentGradingByAgentId(id, agent);
    	  if (ag == null) {
    		  ag = createAssessmentGrading(publishedAssessment);
    	  }
    	  else {
    		  setAttemptDateIfNull(ag);
    	  }
      }
	  delivery.setAssessmentGrading(ag);
	  
      log.debug("itemgrading size = " + ag.getItemGradingSet().size());
      delivery.setAssessmentGradingId(delivery.getAssessmentGrading().getAssessmentGradingId());
      
      //ag can't be null beyond this point and must have persisted to DB
      // version 2.1.1 requirement
      setFeedbackMode(delivery);
      if (ae != null && ae.getComponent().getId().startsWith("beginAssessment")) {
    	  setTimer(delivery, publishedAssessment, true);
    	  setStatus(delivery, pubService, Long.valueOf(id));
    	  // If it comes from Begin Assessment button clicks, reset isNoQuestion to false
    	  // because we want to always display the first page
    	  // Otherwise, if isNoQuestion set to true in the last delivery and 
    	  // if the first part has no question, it will not be rendered
    	  // See getPageContentsByQuestion() for more details
    	  // Of course it is better to do this inside getPageContentsByQuestion()
    	  // However, ae is not passed in getPageContentsByQuestion()
    	  // and there are multiple places to modify if I want to get ae inside getPageContentsByQuestion()
    	  delivery.setNoQuestions(false);
    	      	  
    	  int action = delivery.getActionMode();
    	  if (action == DeliveryBean.TAKE_ASSESSMENT) {
    		  EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.take", "publishedAssessmentId=" + delivery.getAssessmentId() + ", agentId=" + getAgentString(), true));
    	  }
    	  else if (action == DeliveryBean.TAKE_ASSESSMENT_VIA_URL) {
    		  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    		  String siteId = publishedAssessmentService.getPublishedAssessmentOwner(Long.valueOf(delivery.getAssessmentId()));
    		  EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.take.via_url", "publishedAssessmentId=" + delivery.getAssessmentId() + ", agentId=" + getAgentString(), siteId, true, NotificationService.NOTI_REQUIRED));
    	  }    	  
      }
      else {
    	  setTimer(delivery, publishedAssessment, false);
      }

      // extend session time out
      SessionUtil.setSessionTimeout(FacesContext.getCurrentInstance(), delivery, true);
      log.debug("Set begin time " + delivery.getBeginTime());
      log.debug("Set elapsed time " + delivery.getTimeElapse());
                  
      // overload itemGradingHash with the sequence in case renumbering is turned off.
      overloadItemData(delivery, itemGradingHash, publishedAssessment);

      // get the position of last question which has ben answered/viewed by the student
      log.debug("before partIndex = " + delivery.getPartIndex());
      log.debug("before questionIndex = " + delivery.getQuestionIndex());
      setPosition(delivery);
      log.debug("after partIndex = " + delivery.getPartIndex());
      log.debug("after questionIndex = " + delivery.getQuestionIndex());

      HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);

      // get current page contents
      delivery.setPageContents(getPageContents(publishedAssessment, delivery, itemGradingHash, publishedAnswerHash));
  }

  private void setPosition(DeliveryBean delivery) {
	  GradingService gradingService = new GradingService();
	  AssessmentGradingData assessmentGradingData = delivery.getAssessmentGrading();
	  log.debug("assessmentGradingData.getAssessmentGradingId() = " + assessmentGradingData.getAssessmentGradingId());
	  ArrayList alist = gradingService.getLastItemGradingDataPosition(assessmentGradingData.getAssessmentGradingId(), assessmentGradingData.getAgentId());
	  int partIndex = ((Integer)alist.get(0)).intValue();
	  if (partIndex == 0) {
		  delivery.setPartIndex(0);
	  }
	  else {
		  delivery.setPartIndex(partIndex - 1);
	  }
	  delivery.setQuestionIndex(((Integer)alist.get(1)).intValue());
  }
}
