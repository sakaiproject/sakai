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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.Phase;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;
import org.sakaiproject.tool.assessment.util.SamigoLRSStatements;

@Slf4j
public class LinearAccessDeliveryActionListener extends DeliveryActionListener
  implements ActionListener
{
  private static ResourceBundle eventLogMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.EventLogMessages");
  private final EventTrackingService eventTrackingService= ComponentManager.get( EventTrackingService.class );

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
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      // set show student score
      setShowStudentScore(delivery, publishedAssessment);
      setShowStudentQuestionScore(delivery, publishedAssessment);
      setDeliverySettings(delivery, publishedAssessment);
      
      if (ae != null && ae.getComponent().getId().startsWith("beginAssessment")) {
    	  // #1. check password
    	  if (!delivery.getSettings().getPassword().equals(""))
    	  {
    		  if ("passwordAccessError".equals(delivery.validatePassword())) {
    			  return;
    		  }
    	  }

    	  // #2. check IP
    	  if (delivery.getSettings().getIpAddresses() != null && !delivery.getSettings().getIpAddresses().isEmpty())
    	  {
    		  if ("ipAccessError".equals(delivery.validateIP())) {
    			  return;
    		  }
    	  }
          
          // #3. secure delivery START phase
          SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
          if ( secureDelivery.isSecureDeliveryAvaliable() ) {
              String moduleId = publishedAssessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY );
              if ( moduleId != null && ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
                  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                  PhaseStatus status = secureDelivery.validatePhase(moduleId, Phase.ASSESSMENT_START, publishedAssessment, request );
                  if ( PhaseStatus.FAILURE == status ) {
                      return;
                  }
              }    	  
          }
      }

      super.populateSubmissionsRemaining(pubService, publishedAssessment, delivery);

      // itemGradingHash will end up with 
      // (Long publishedItemId, ArrayList itemGradingDatas) and
      // (String "sequence"+itemId, Integer sequence) and
      // (String "items", Long itemscount)
      GradingService service = new GradingService();
      AssessmentGradingData ag = null;
      
      // this returns a HashMap with (publishedItemId, itemGrading)
      Map itemGradingHash = service.getLastItemGradingData(id, agent);
      
      boolean isFirstTimeBegin = false;
      if (itemGradingHash!=null && itemGradingHash.size()>0){
    	  log.debug("itemGradingHash!=null && itemGradingHash.size()>0");
    	  ag = setAssessmentGradingFromItemData(delivery, itemGradingHash, true);
    	  setAttemptDateIfNull(ag);
      }
      else{
    	  ag = service.getLastSavedAssessmentGradingByAgentId(id, agent);
    	  if (ag == null) {
    		  ag = createAssessmentGrading(publishedAssessment);
    		  isFirstTimeBegin = true;
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
    	  setTimer(delivery, publishedAssessment, true, isFirstTimeBegin);
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
    	  // ONC event log
    	  EventLogService eventService = new EventLogService();
          EventLogFacade eventLogFacade = new EventLogFacade();
          String agentEid = AgentFacade.getEid();
          //set event log data
          EventLogData eventLogData = new EventLogData();
          eventLogData.setAssessmentId(Long.valueOf(id));
          eventLogData.setProcessId(delivery.getAssessmentGradingId());
          eventLogData.setStartDate(new Date());
          eventLogData.setTitle(publishedAssessment.getTitle());
          eventLogData.setUserEid(agentEid); 
          String site_id= AgentFacade.getCurrentSiteId();
          if(site_id == null) {
        	  //take assessment via url
        	  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    		  site_id = publishedAssessmentService.getPublishedAssessmentOwner(Long.valueOf(delivery.getAssessmentId()));
          }
          eventLogData.setSiteId(site_id);
          
          eventLogData.setErrorMsg(eventLogMessages.getString("no_submission"));
          eventLogData.setEndDate(null);
          eventLogData.setEclipseTime(null);
          		  
          String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
          eventLogData.setIpAddress(thisIp);
		  			  
          eventLogFacade.setData(eventLogData);
          eventService.saveOrUpdateEventLog(eventLogFacade);           	  
          int action = delivery.getActionMode();
    	  if (action == DeliveryBean.TAKE_ASSESSMENT) {
    		  StringBuffer eventRef = new StringBuffer("publishedAssessmentId");
    		  eventRef.append(delivery.getAssessmentId());
    		  eventRef.append(", agentId=");
    		  eventRef.append(getAgentString());
    		  if (delivery.isTimeRunning()) {
    			  eventRef.append(", elapsed=");
    			  eventRef.append(delivery.getTimeElapse());
    			  eventRef.append(", remaining=");
    			  int timeRemaining = Integer.parseInt(delivery.getTimeLimit()) - Integer.parseInt(delivery.getTimeElapse());
    			  eventRef.append(timeRemaining);
    		  }
    		  Event event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TAKE, eventRef.toString(), site_id, true, NotificationService.NOTI_REQUIRED, SamigoLRSStatements.getStatementForTakeAssessment(delivery.getAssessmentTitle(), delivery.getPastDue(), publishedAssessment.getReleaseTo(), false));
    		  eventTrackingService.post(event);
    	  }
    	  else if (action == DeliveryBean.TAKE_ASSESSMENT_VIA_URL) {
    		  StringBuffer eventRef = new StringBuffer("publishedAssessmentId");
    		  eventRef.append(delivery.getAssessmentId());
    		  eventRef.append(", agentId=");
    		  eventRef.append(getAgentString());
    		  if (delivery.isTimeRunning()) {
    			  eventRef.append(", elapsed=");
    			  eventRef.append(delivery.getTimeElapse());
    			  eventRef.append(", remaining=");
    			  int timeRemaining = Integer.parseInt(delivery.getTimeLimit()) - Integer.parseInt(delivery.getTimeElapse());
    			  eventRef.append(timeRemaining);
    		  }
    		  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    		  String siteId = publishedAssessmentService.getPublishedAssessmentOwner(Long.valueOf(delivery.getAssessmentId()));
    		  Event event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TAKE, eventRef.toString(), siteId, true, NotificationService.NOTI_REQUIRED, SamigoLRSStatements.getStatementForTakeAssessment(delivery.getAssessmentTitle(), delivery.getPastDue(), publishedAssessment.getReleaseTo(), true));
    		  eventTrackingService.post(event);
    	  }    	  
      }
      else {
    	  setTimer(delivery, publishedAssessment, false, false);
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
      setPosition(delivery, publishedAssessment, ae);
      log.debug("after partIndex = " + delivery.getPartIndex());
      log.debug("after questionIndex = " + delivery.getQuestionIndex());

      Map publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);

      if(itemGradingHash != null) {
          delivery.setTableOfContents(getContents(publishedAssessment, itemGradingHash,delivery, publishedAnswerHash));
      }
      // get current page contents
      delivery.setPageContents(getPageContents(publishedAssessment, delivery, itemGradingHash, publishedAnswerHash));
  }

  private void setPosition(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment, ActionEvent ae) {
	  GradingService gradingService = new GradingService();
	  if (ae != null && ae.getComponent().getId().startsWith("beginAssessment")) {
          if (delivery.getNumberRetake() == -1 || delivery.getActualNumberRetake() == -1) {
        	  delivery.setNumberRetake(gradingService.getNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString()));
        	  log.debug("numberRetake = " + delivery.getNumberRetake());
        	  delivery.setActualNumberRetake(gradingService.getActualNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString()));
        	  log.debug("actualNumberRetake =" + delivery.getActualNumberRetake());
          }
          
          if (delivery.getActualNumberRetake() == delivery.getNumberRetake() - 1) {
        	  delivery.setPartIndex(0);
        	  delivery.setQuestionIndex(0);
        	  return;
          }
	  }
	  
	  AssessmentGradingData assessmentGradingData = delivery.getAssessmentGrading();
	  log.debug("assessmentGradingData.getAssessmentGradingId() = " + assessmentGradingData.getAssessmentGradingId());
	  if (assessmentGradingData.getLastVisitedPart() != null && assessmentGradingData.getLastVisitedQuestion() != null) {
		  delivery.setPartIndex(assessmentGradingData.getLastVisitedPart().intValue());
		  delivery.setQuestionIndex(assessmentGradingData.getLastVisitedQuestion().intValue());
	  }
	  else {
		  // For backward compatible
		  List<Integer> alist = gradingService.getLastItemGradingDataPosition(assessmentGradingData.getAssessmentGradingId(), assessmentGradingData.getAgentId());
		  int partIndex = alist.get(0);
		  if (partIndex == 0) {
			  delivery.setPartIndex(0);
		  }
		  else {
			  delivery.setPartIndex(partIndex - 1);
		  }
		  delivery.setQuestionIndex(((Integer)alist.get(1)).intValue());
	  }
  }
  
  public void saveLastVisitedPosition(DeliveryBean delivery, int partNumber, int questionNumber) {
	  GradingService gradingService = new GradingService();
	  AssessmentGradingData assessmentGradingData = delivery.getAssessmentGrading();
	  assessmentGradingData.setLastVisitedPart(partNumber);
	  assessmentGradingData.setLastVisitedQuestion(questionNumber);
	  gradingService.saveOrUpdateAssessmentGradingOnly(assessmentGradingData);
  }
}
