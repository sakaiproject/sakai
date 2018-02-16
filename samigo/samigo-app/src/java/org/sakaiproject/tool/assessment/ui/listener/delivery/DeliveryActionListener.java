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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.Phase;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ContentsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FeedbackComponent;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FibBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FinBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ImageMapQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatrixSurveyBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.tool.assessment.util.FormatException;
import org.sakaiproject.tool.assessment.util.SamigoLRSStatements;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class DeliveryActionListener
  implements ActionListener
{

  static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  //private static ContextUtil cu;
  private boolean resetPageContents = true;
  private long previewGradingId = (long)(Math.random() * 1000);
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
    log.debug("DeliveryActionListener.processAction() ");

    try
    {
      PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
      // 1. get managed bean
      DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
      
      
      // set publishedId, note that id can be changed by isPreviewingMode()
      String id = getPublishedAssessmentId(delivery);
      String agent = getAgentString();

      // 2. get assessment from deliveryBean if id matches. otherwise, this is the 1st time
      // that DeliveryActionListener is called, so pull it from DB
      PublishedAssessmentFacade publishedAssessment = getPublishedAssessment(delivery, id);
      int action = delivery.getActionMode();
      if (DeliveryBean.REVIEW_ASSESSMENT == action && AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(publishedAssessment.getStatus())) {
      	// Bug 1547: If this is during review and the assessment is retracted for edit now, 
    	// there is no action needed (the outcome is set in BeginDeliveryActionListener).
      	return;
      }

      if (delivery.pastDueDate() && (DeliveryBean.TAKE_ASSESSMENT == action || DeliveryBean.TAKE_ASSESSMENT_VIA_URL == action)) {
        if (delivery.isAcceptLateSubmission()) {
          if(delivery.getTotalSubmissions() > 0) {
            // Not during a Retake
            if (delivery.getActualNumberRetake() == delivery.getNumberRetake()) {
              return;
            }
          }
        } else {
          if(delivery.isRetracted(false)){
              return;
          }
        }
      }
      // Clear elapsed time, set not timed out
      clearElapsedTime(delivery);

      // set show student score
      setShowStudentScore(delivery, publishedAssessment);
      setShowStudentQuestionScore(delivery, publishedAssessment);
      setDeliverySettings(delivery, publishedAssessment);
    
      // 3. there 3 types of navigation: by question (question will be displayed one at a time), 
      // by part (questions in a part will be displayed together) or by assessment (i.e. 
      // all questions will be displayed on one page). When navigating from TOC, a part number
      // signal that the navigation is either by question or by part. We then must set the 
      // delivery bean to the right place.
      // However, it comes from Begin Assessment button clicks, we need to reset the indexes to 0
      // Otherwise, the first question of the first part will not be displayed 
      if (ae != null && ae.getComponent().getId().startsWith("beginAssessment")) {
    	  if (!delivery.getNavigation().equals("1")) {
    		  // If it comes from Begin Assessment button clicks (in Random assessment), reset the indexes to 0
    		  log.debug("From Begin Assessment button clicks");
    		  delivery.setPartIndex(0);
    		  delivery.setQuestionIndex(0);
    	  }
    	  
    	  // If it comes from Begin Assessment button clicks, reset isNoQuestion to false
    	  // because we want to always display the first page
    	  // Otherwise, if isNoQuestion set to true in the last delivery and 
    	  // if the first part has no question, it will not be rendered
    	  // See getPageContentsByQuestion() for more details
    	  // Of course it is better to do this inside getPageContentsByQuestion()
    	  // However, ae is not passed in getPageContentsByQuestion()
    	  // and there are multiple places to modify if I want to get ae inside getPageContentsByQuestion()
       	  delivery.setNoQuestions(false);
      }
      else {
    	  // If from table of contents page, there is no parameters like partnumber or questionnumber
    	  if (!delivery.getFromTableOfContents()) {
    		  // If comes from TOC, set the indexes from request parameters
        	  goToRightQuestionFromTOC(delivery);
        	  
          }
          else {
        	  delivery.setFromTableOfContents(false);
          }
      }
      
      // 4. this purpose of this listener is to integrated itemGradingData and 
      //    assessmentGradingData to the publishedAssessment during these 3 processes:
      //    taking assessment, reviewing assessment and grading assessment by question. 
      //    When taking or reviewing an assessment, BeginDeliveryActionListener is called
      //    by an event in the jsf page to retrieve the publishedAssessment. When grading
      //    assessment, StudentScoreListener is called to retrieve the published Assessment.
      
      // itemGradingHash will end up with 
      // (Long publishedItemId, ArrayList itemGradingDatas) and
      // (String "sequence"+itemId, Integer sequence) and
      // (String "items", Long itemscount)
      Map itemGradingHash = new HashMap();
      GradingService service = new GradingService();
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      AssessmentGradingData ag = null;
      SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
      boolean isFirstTimeBegin = false;
      StringBuffer eventRef; 
      Event event;
      
      switch (action){
      case 2: // preview assessment
              setFeedbackMode(delivery);
              break;

      case 3: // Review assessment
              setFeedbackMode(delivery); //this determine if we should gather the itemGrading
              Integer scoringoption = publishedAssessment.getEvaluationModel().getScoringType();
              String assessmentGradingId = ContextUtil.lookupParam("assessmentGradingId");
              
              if (("true").equals(delivery.getFeedback())){
                itemGradingHash = new HashMap();
                if (delivery.getFeedbackComponent().getShowResponse() || delivery.getFeedbackComponent().getShowStudentQuestionScore() ||
                        delivery.getFeedbackComponent().getShowItemLevel())
                	itemGradingHash = service.getSubmitData(id, agent, scoringoption, assessmentGradingId);
                ag = setAssessmentGradingFromItemData(delivery, itemGradingHash, false);
                delivery.setAssessmentGrading(ag);
	      }
              setDisplayByAssessment(delivery);
              //setDeliveryFeedbackOnforEvaluation(delivery);
              //setGraderComment(delivery);
              FeedbackComponent component = new FeedbackComponent();
              AssessmentFeedbackIfc info =  (AssessmentFeedbackIfc) publishedAssessment.getAssessmentFeedback();
              if ( info != null) {
            	  	component.setAssessmentFeedback(info);
              }
              delivery.setFeedbackComponent(component);
              AssessmentGradingData agData = null;
              if (EvaluationModelIfc.LAST_SCORE.equals(scoringoption)){
            	  agData = (AssessmentGradingData) service.getLastSubmittedAssessmentGradingByAgentId(id, agent, assessmentGradingId);
              }
              else {
            	  agData = (AssessmentGradingData) service.getHighestSubmittedAssessmentGrading(id, agent, assessmentGradingId);
              }
              if (agData == null) {
            	  delivery.setOutcome("reviewAssessmentError");
            	  return;
              }
              log.debug("GraderComments: getComments()" + agData.getComments());
              delivery.setGraderComment(agData.getComments());
              delivery.setAssessmentGradingAttachmentList(agData.getAssessmentGradingAttachmentList());
              delivery.setHasAssessmentGradingAttachment(
            		  agData.getAssessmentGradingAttachmentList() != null && agData.getAssessmentGradingAttachmentList().size() > 0);
              delivery.setAssessmentGradingId(agData.getAssessmentGradingId());
              delivery.setOutcome("takeAssessment");
              delivery.setSecureDeliveryHTMLFragment( "" );
              delivery.setBlockDelivery( false );
              
              if ( secureDelivery.isSecureDeliveryAvaliable() ) {
            	  
            	  String moduleId = publishedAssessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY );
            	  if ( moduleId != null && ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
              		  
            		  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            		  PhaseStatus status = secureDelivery.validatePhase(moduleId, Phase.ASSESSMENT_REVIEW, publishedAssessment, request );
            		  delivery.setSecureDeliveryHTMLFragment( 
            				  secureDelivery.getHTMLFragment(moduleId, publishedAssessment, request, Phase.ASSESSMENT_REVIEW, status, new ResourceLoader().getLocale() ) );             		 
            		  if ( PhaseStatus.FAILURE == status )  {           			 
            			  delivery.setOutcome( "secureDeliveryError" );
            			  delivery.setBlockDelivery( true );
            		  }
            	  }                 	  
              }

              // post event
              eventRef = new StringBuffer("publishedAssessmentId=");
              eventRef.append(delivery.getAssessmentId());
              eventRef.append(", submissionId=");
              eventRef.append(agData.getAssessmentGradingId());
              event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REVIEW, eventRef.toString(), true);
              eventTrackingService.post(event);
              break;
 
      case 4: // Grade assessment
    	  	  String gradingData = ContextUtil.lookupParam("gradingData");
              itemGradingHash = service.getStudentGradingData(gradingData);
              delivery.setAssessmentGradingId(Long.valueOf(gradingData));
              ag = setAssessmentGradingFromItemData(delivery, itemGradingHash, false);
              delivery.setAssessmentGrading(ag);
              setDisplayByAssessment(delivery);
              setFeedbackMode(delivery);
              setDeliveryFeedbackOnforEvaluation(delivery);
              setGraderComment(delivery);
              break;

      case 1: // Take assessment
      case 5: // Take assessment via url
              log.debug("**** DeliveryActionListener #0");

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

              populateSubmissionsRemaining(pubService, publishedAssessment, delivery);
              
              // If this is a linear access and user clicks on Show Feedback, we do not
              // get data from db. Use delivery bean instead
              if (delivery.getNavigation().equals("1") && ae != null && "showFeedback".equals(ae.getComponent().getId())) {
            	  log.debug("Do not get data from db if it is linear access and the action is show feedback but...");
            	  log.debug("except file upload and audio questions");
            	  ag = delivery.getAssessmentGrading();
            	  Set itemGradingSet = ag.getItemGradingSet();
            	  Iterator iter = itemGradingSet.iterator();
            	  while (iter.hasNext())
            	  {
            		  ItemGradingData data = (ItemGradingData) iter.next();
            		  List thisone = (List) itemGradingHash.get(data.getPublishedItemId());
            		  if (thisone == null) {
            			  thisone = new ArrayList();
            		  }
            		  thisone.add(data);
            		  itemGradingHash.put(data.getPublishedItemId(), thisone);
            	  }

                  // For file upload and audio questions, adding the corresponding itemGradingData into itemGradingHash and itemGradingSet to display correctly in delivery
                  // this hash compose (itemGradingId, array list of MediaData)
                  Map<Long, List<ItemGradingData>> mediaItemGradingHash = service.getMediaItemGradingHash(ag.getAssessmentGradingId());
                  Set<Map.Entry<Long, List<ItemGradingData>>> set = mediaItemGradingHash.entrySet();
            	  for (Map.Entry<Long, List<ItemGradingData>> me : set) {
            		  Long publishedItemId = me.getKey();
            		  List<ItemGradingData> al = me.getValue();
            		  List itemGradingArray = (List) itemGradingHash.get(publishedItemId);
            		  if (itemGradingArray != null) {
            			  itemGradingArray.addAll(al);
            		  }
            		  else {
            			  itemGradingArray = new ArrayList();
            			  itemGradingArray.addAll(al);
            		  }
            		  itemGradingHash.put(publishedItemId, itemGradingArray);
            		  itemGradingSet.addAll(itemGradingArray);
            		  ag.setItemGradingSet(itemGradingSet);
            	  }
              }
              else {
            	  log.debug("Get data from db otherwise");
                  // this returns a HashMap with (publishedItemId, itemGrading)
                  itemGradingHash = service.getLastItemGradingData(id, agent); //
                  log.debug("**** DeliveryActionListener #1");

                  if (itemGradingHash!=null && itemGradingHash.size()>0){
                	  log.debug("**** DeliveryActionListener #1a");
                	  ag = setAssessmentGradingFromItemData(delivery, itemGradingHash, true);
                	  setAttemptDateIfNull(ag);
                  }
                  else {
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
              }
              log.debug("**** DeliveryAction, itemgrading size="+ag.getItemGradingSet().size());
              delivery.setAssessmentGradingId(delivery.getAssessmentGrading().getAssessmentGradingId());
              
              // ag can't be null beyond this point and must have persisted to DB
              // version 2.1.1 requirement
              setFeedbackMode(delivery);
              
              if (ae != null && ae.getComponent().getId().startsWith("beginAssessment")) {
            	  setTimer(delivery, publishedAssessment, true, isFirstTimeBegin);
            	  setStatus(delivery, pubService, Long.valueOf(id));
            	  
            	  EventLogService eventService = new EventLogService();
                  EventLogFacade eventLogFacade = new EventLogFacade();
                  String agentEid = AgentFacade.getEid();
                  //ONC-3500
                  if(agentEid == null || agentEid.equals("")){
                	  agentEid= "N/A";
                  }
                  //set event log data
                  EventLogData eventLogData = new EventLogData();
                  eventLogData.setAssessmentId(Long.valueOf(id));
                  eventLogData.setProcessId(delivery.getAssessmentGradingId());
                  eventLogData.setStartDate(new Date());
                  eventLogData.setTitle(publishedAssessment.getTitle());
                  eventLogData.setUserEid(agentEid); 
                  String site_id = AgentFacade.getCurrentSiteId();
                  //take assessment via url
                  if(site_id == null) {
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
                  
            	  if (action == DeliveryBean.TAKE_ASSESSMENT) {
            		  eventRef = new StringBuffer("publishedAssessmentId=");
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
            		  
                      event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TAKE,
                              "siteId=" + site_id + ", " + eventRef.toString(), site_id, true, 0, SamigoLRSStatements.getStatementForTakeAssessment(delivery.getAssessmentTitle(), delivery.getPastDue(), publishedAssessment.getReleaseTo(), false));
                      eventTrackingService.post(event);
            	  }
            	  else if (action == DeliveryBean.TAKE_ASSESSMENT_VIA_URL) {
            		  eventRef = new StringBuffer("publishedAssessmentId=");
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
                      event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_TAKE_VIAURL,
                                "siteId=" + site_id + ", " + eventRef.toString(), site_id, true, NotificationService.NOTI_REQUIRED, SamigoLRSStatements.getStatementForTakeAssessment(delivery.getAssessmentTitle(), delivery.getPastDue(), publishedAssessment.getReleaseTo(), true));
                      eventTrackingService.post(event);
            	  }
              }
              else {
            	  setTimer(delivery, publishedAssessment, false, false);
              }

              if (ae != null && ae.getComponent().getId().startsWith("continueAssessment")) {
                  String site_id = AgentFacade.getCurrentSiteId();
                  //take assessment via url
                  if(site_id == null) {
                      PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
                      site_id = publishedAssessmentService.getPublishedAssessmentOwner(Long.valueOf(delivery.getAssessmentId()));
                  }
            	  eventRef = new StringBuffer("publishedAssessmentId=");
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
                  event = eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_RESUME,
                                "siteId=" + site_id + ", " + eventRef.toString(), site_id, true, NotificationService.NOTI_REQUIRED);
                  eventTrackingService.post(event);
              }
              
              // extend session time out
              SessionUtil.setSessionTimeout(FacesContext.getCurrentInstance(), delivery, true);
              log.debug("****Set begin time " + delivery.getBeginTime());
              log.debug("****Set elapsed time " + delivery.getTimeElapse());
              break;

      default: break;
      }

      // overload itemGradingHash with the sequence in case renumbering is turned off.
      overloadItemData(delivery, itemGradingHash, publishedAssessment);

      // get table of contents
      Map<Long, AnswerIfc> publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);
      delivery.setTableOfContents(getContents(publishedAssessment, itemGradingHash, delivery, publishedAnswerHash));
      // get current page contents
      log.debug("**** resetPageContents="+this.resetPageContents);
      // If it comes from Show Feedback link clicks, call getShowFeedbackPageContents() to 
      // reset the partIndex and questionIndex (the last part of SAK-5750)
      if (this.resetPageContents){
        if (ae != null && ae.getComponent().getId().equals("showFeedback")) {
      	  delivery.setPageContents(getShowFeedbackPageContents(publishedAssessment, delivery, itemGradingHash, publishedAnswerHash));
        }
        else {
    	  delivery.setPageContents(getPageContents(publishedAssessment, delivery, itemGradingHash, publishedAnswerHash));
	}
      }
    }
    catch (RuntimeException e)
    {
    	DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    	String id = getPublishedAssessmentId(delivery);
    	String agentEid = AgentFacade.getEid();
    	if(agentEid == null || agentEid.equals("")) {
    		agentEid= "N/A";
    	}

    	PublishedAssessmentFacade publishedAssessment = getPublishedAssessment(delivery, id);

    	EventLogService eventService = new EventLogService();
    	EventLogFacade eventLogFacade = new EventLogFacade();
    	EventLogData eventLogData = null;

    	List eventLogDataList = eventService.getEventLogData(delivery.getAssessmentGradingId());
    	if(eventLogDataList != null && eventLogDataList.size() > 0) {
    		eventLogData= (EventLogData) eventLogDataList.get(0);
    		eventLogData.setErrorMsg(eventLogMessages.getString("error_begin"));
    	} else {
    		eventLogData = new EventLogData();
    		eventLogData.setErrorMsg(eventLogMessages.getString("error_begin"));
    		eventLogData.setAssessmentId(Long.valueOf(id));
    		eventLogData.setProcessId(delivery.getAssessmentGradingId());
    		eventLogData.setStartDate(new Date());
    		eventLogData.setTitle(publishedAssessment.getTitle());
    		eventLogData.setUserEid(agentEid); 
    		String site_id =AgentFacade.getCurrentSiteId();
    		//take assessment via url
    		if(site_id == null) {
    			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    			site_id = publishedAssessmentService.getPublishedAssessmentOwner(Long.valueOf(delivery.getAssessmentId()));
    		}
    		eventLogData.setSiteId(site_id);
    		eventLogData.setEndDate(null);
    		eventLogData.setEclipseTime(null);
    	}
    			
    	String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
    	eventLogData.setIpAddress(thisIp);
				
        eventLogFacade.setData(eventLogData);
    	eventService.saveOrUpdateEventLog(eventLogFacade);
    	throw e;
    }

  }
  
  /**
   * Look up item grading data and set assesment grading data from it or,
   * if there is none set null if setNullOK.
   * @param delivery the delivery bean
   * @param itemGradingHash the itemGradingHash hash map
   * @param setNullOK if there is none set null if true
   */
  protected AssessmentGradingData setAssessmentGradingFromItemData(DeliveryBean delivery, Map itemGradingHash, boolean setNullOK)
  {
    AssessmentGradingData agrading = null;
    Iterator keys = itemGradingHash.keySet().iterator();
    // for each publishedItem, looks like we are getting the 1st itemGradingData
    GradingService gradingService = new GradingService();
    if (keys.hasNext()) 
    {
      ItemGradingData igd = (ItemGradingData) ( (List) itemGradingHash.get(
        keys.next())).toArray()[0];
      AssessmentGradingData agd = gradingService.load(igd.getAssessmentGradingId().toString(), false);
      agd.setItemGradingSet(gradingService.getItemGradingSet(agd.getAssessmentGradingId().toString()));
      if (!agd.getForGrade().booleanValue()){
        log.debug("setAssessmentGradingFromItemData agd.getTimeElapsed(): " + agd.getTimeElapsed());
        log.debug("setAssessmentGradingFromItemData delivery.getTimeElapse(): " + delivery.getTimeElapse());
        agrading = agd;
      }
      else{ // if assessmentGradingData has been submitted for grade, then
    // we need to reset delivery.assessmentGrading - a problem review from fixing SAK-1781
        if (setNullOK) agrading=null;
      }
    }
    else
    {
      if (setNullOK) agrading=null;
    }
    return agrading;
  }

  /**
   * Put the setShows on.
   * @param delivery the delivery bean
   */
  private void setDeliveryFeedbackOnforEvaluation(DeliveryBean delivery)
  {
	  if (delivery.getFeedbackComponent() == null)
	  {
		  delivery.setFeedbackComponent(new FeedbackComponent());
	  }
	  delivery.getFeedbackComponent().setShowCorrectResponse(true);
	  delivery.getFeedbackComponent().setShowGraderComment(true);
	  delivery.getFeedbackComponent().setShowItemLevel(true);
	  delivery.getFeedbackComponent().setShowQuestion(true);
	  delivery.getFeedbackComponent().setShowResponse(true);
	  delivery.getFeedbackComponent().setShowSelectionLevel(true);
	  delivery.getFeedbackComponent().setShowStats(true);
	  delivery.getFeedbackComponent().setShowStudentScore(true);
	  delivery.getFeedbackComponent().setShowStudentQuestionScore(true);


  }

  /**
   * Sets the delivery bean to the right place when navigating from TOC
   * @param delivery
   * @throws java.lang.NumberFormatException
   */
  private void goToRightQuestionFromTOC(DeliveryBean delivery) throws
    NumberFormatException
  {
    if (ContextUtil.lookupParam("partnumber") != null &&
          !ContextUtil.lookupParam("partnumber").trim().equals("") && 
            !ContextUtil.lookupParam("partnumber").trim().equals("null") &&
          ContextUtil.lookupParam("questionnumber") != null &&
          !ContextUtil.lookupParam("questionnumber").trim().equals("") &&
            !ContextUtil.lookupParam("questionnumber").trim().equals("null"))
    {
        delivery.setPartIndex(Integer.valueOf
                (ContextUtil.lookupParam("partnumber")).intValue() - 1);
        delivery.setQuestionIndex(Integer.valueOf
                (ContextUtil.lookupParam("questionnumber")).intValue() - 1);
    }
  }

  /**
   * Gets a table of contents bean
   * @param publishedAssessment the published assessment
   * @return
   */
  protected ContentsDeliveryBean getContents(PublishedAssessmentFacade
                                           publishedAssessment,
                                           Map itemGradingHash,
                                           DeliveryBean delivery,
                                           Map publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    double currentScore = 0;
    double maxScore = 0;

    // get parts
    List partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    List partsContents = new ArrayList();
    while (iter.hasNext())
    {
      SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                 itemGradingHash, delivery,
                                                 publishedAnswerHash);
      partBean.setNumParts(Integer.toString(partSet.size()));
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();
      partsContents.add(partBean);
    }

    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    return contents;
  }

  /**
   * Gets a contents bean for the current page.
   * Really, just a wrapper utility to delegate to whichever
   * method handles the format being used.
   *
   * @todo these should actually take a copy of contents and filter it
   * for the page unstead of doing a recompute, which is less efficient
   * @param publishedAssessment the published assessment
   * @return
   */
  public ContentsDeliveryBean getPageContents(
    PublishedAssessmentFacade publishedAssessment,
    DeliveryBean delivery, Map itemGradingHash, Map publishedAnswerHash)
  {

    if (delivery.getSettings().isFormatByAssessment())
    {
      return getPageContentsByAssessment(publishedAssessment, itemGradingHash,
                                         delivery, publishedAnswerHash);
    }

    int itemIndex = delivery.getQuestionIndex();
    int sectionIndex = delivery.getPartIndex();

    if (delivery.getSettings().isFormatByPart())
    {
      return getPageContentsByPart(publishedAssessment, itemIndex, sectionIndex,
                                   itemGradingHash, delivery, publishedAnswerHash);
    }
    else if (delivery.getSettings().isFormatByQuestion())
    {
      return getPageContentsByQuestion(publishedAssessment, itemIndex,
                                       sectionIndex, itemGradingHash, delivery, publishedAnswerHash);
    }

    // default... ...shouldn't get here :O
    log.warn("delivery.getSettings().isFormatBy... is NOT set!");
    return getPageContentsByAssessment(publishedAssessment, itemGradingHash, 
                                       delivery, publishedAnswerHash);

  }

  /**
   * When user clicks on Show Feedback, this method gets a contents bean for the current page.
   * The difference of the above one is we reset partIndex/questionIndex to make the first
   * question to be seen on the top (the last part of SAK-5750).
   *
   * @todo these should actually take a copy of contents and filter it
   * for the page unstead of doing a recompute, which is less efficient
   * @param publishedAssessment the published assessment
   * @return
   */
  public ContentsDeliveryBean getShowFeedbackPageContents(
    PublishedAssessmentFacade publishedAssessment,
    DeliveryBean delivery, Map itemGradingHash, Map publishedAnswerHash)
  {
    
    if (delivery.getSettings().isFormatByAssessment())
    { 
      delivery.setPartIndex(0);	
      delivery.setQuestionIndex(0);
      return getPageContentsByAssessment(publishedAssessment, itemGradingHash,
                                         delivery, publishedAnswerHash);
    }
    if (delivery.getSettings().isFormatByPart())
    {
      delivery.setQuestionIndex(0);
      return getPageContentsByPart(publishedAssessment, delivery.getQuestionIndex(), delivery.getPartIndex(),
                                   itemGradingHash, delivery, publishedAnswerHash);
    }
    else if (delivery.getSettings().isFormatByQuestion())
    {
      return getPageContentsByQuestion(publishedAssessment, delivery.getQuestionIndex(),
    		  delivery.getPartIndex(), itemGradingHash, delivery, publishedAnswerHash);
    }

    // default... ...shouldn't get here :O
    log.warn("delivery.getSettings().isFormatBy... is NOT set!");
    delivery.setPartIndex(0);	
    delivery.setQuestionIndex(0);
    return getPageContentsByAssessment(publishedAssessment, itemGradingHash, 
                                       delivery, publishedAnswerHash);
  }
  /**
   * Gets a contents bean for the current page if is format by assessment.
   *
   * @param publishedAssessment the published assessment
   * @return ContentsDeliveryBean for page
   */
  private ContentsDeliveryBean getPageContentsByAssessment(
    PublishedAssessmentFacade publishedAssessment, Map itemGradingHash,
    DeliveryBean delivery, Map publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    double currentScore = 0;
    double maxScore = 0;

    // get parts
    List partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    List partsContents = new ArrayList();
    while (iter.hasNext())
    {
      SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                 itemGradingHash, delivery,
                                                 publishedAnswerHash);
      partBean.setNumParts(Integer.toString(partSet.size()));
      if (partBean.getItemContentsSize().equals("0")) {
    	  log.debug("getPageContentsByAssessment(): no question");
    	  partBean.setNoQuestions(true);
      }
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();
      partsContents.add(partBean);
    }

    delivery.setPrevious(false);
    delivery.setContinue(false);
    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    contents.setShowStudentScore(delivery.isShowStudentScore());
    return contents;
  }

  /**
   * Gets a contents bean for the current page if is format by part.
   *
   * @param publishedAssessment the published assessment
   * @param itemIndex zero based item offset in part
   * @param sectionIndex zero based section offset in assessment
   * @return ContentsDeliveryBean for page
   */
  private ContentsDeliveryBean getPageContentsByPart(
    PublishedAssessmentFacade publishedAssessment,
    int itemIndex, int sectionIndex, Map itemGradingHash,
    DeliveryBean delivery, Map publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    double currentScore = 0;
    double maxScore = 0;
    int sectionCount = 0;

    // get parts
    List partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    List partsContents = new ArrayList();
    while (iter.hasNext())
    {
      SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                 itemGradingHash, delivery,
                                                 publishedAnswerHash);
      partBean.setNumParts(Integer.toString(partSet.size()));
      if (partBean.getItemContentsSize().equals("0")) {
    	  log.debug("getPageContentsByPart(): no question");
    	  partBean.setNoQuestions(true);
      }
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();
      if (sectionCount++ == sectionIndex)
      {
        partsContents.add(partBean);
        if (iter.hasNext())
        {
          delivery.setContinue(true);
        }
        else
        {
          delivery.setContinue(false);
        }
        if (sectionCount > 1)
        {
          delivery.setPrevious(true);
        }
        else
        {
          delivery.setPrevious(false);
        }
      }
    }

    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    contents.setShowStudentScore(delivery.isShowStudentScore());
    return contents;
  }

  /**
   * Gets a contents bean for the current page if is format by question.
   *
   * @param publishedAssessment the published assessment
   * @param itemIndex zero based item offset in part
   * @param sectionIndex zero based section offset in assessment
   * @return ContentsDeliveryBean for page
   */
  private ContentsDeliveryBean getPageContentsByQuestion(
    PublishedAssessmentFacade publishedAssessment,
    int itemIndex, int sectionIndex, Map itemGradingHash,
    DeliveryBean delivery, Map publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    double currentScore = 0;
    double maxScore = 0;
    int sectionCount = 0;
    int questionCount = 0; // This is to increment the part if we run
    // out of questions
    // get parts
    List partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    List partsContents = new ArrayList();
    if (itemIndex < 0)
    {
      sectionIndex--;
      delivery.setPartIndex(sectionIndex);
    }
    while (iter.hasNext()) // has next part
    {
      SectionDataIfc secFacade = (SectionDataIfc) iter.next();
      SectionContentsBean partBean = getPartBean(secFacade, itemGradingHash, delivery, 
                                                 publishedAnswerHash);
      partBean.setNumParts(Integer.toString(partSet.size()));
      currentScore += partBean.getPoints();
      maxScore += partBean.getMaxPoints();

      //questionCount = secFacade.getItemSet().size();
      // need to  get ItemArraySort, insteand of getItemSet, to return corr number for random draw parts
      List<ItemDataIfc> itemlist = secFacade.getItemArray();
      long seed = getSeed(secFacade, delivery, (long) AgentFacade.getAgentString().hashCode());

      List<ItemDataIfc> sortedlist = getItemArraySortedWithRandom(secFacade, itemlist, seed); 
      questionCount = sortedlist.size();

      if ((delivery.getNoQuestions() || questionCount != 0) && itemIndex > (questionCount - 1) && sectionCount == sectionIndex) {
        sectionIndex++;
        delivery.setPartIndex(sectionIndex);
        itemIndex = 0;
        delivery.setQuestionIndex(itemIndex);
        delivery.setNoQuestions(false);
      }
      if (itemIndex < 0 && sectionCount == sectionIndex)
      {
        itemIndex = questionCount - 1;
        delivery.setQuestionIndex(itemIndex);
      }

      if (sectionCount++ == sectionIndex)
      {
   		SectionContentsBean partBeanWithQuestion = 
     			this.getPartBeanWithOneQuestion(secFacade, itemIndex, itemGradingHash,
     					delivery, publishedAnswerHash);
      	partBeanWithQuestion.setNumParts(Integer.toString(partSet.size()));
      	partsContents.add(partBeanWithQuestion);
      	
      	if (questionCount == 0) {
      		partBeanWithQuestion.setNoQuestions(true);
      		delivery.setNoQuestions(true);
      	}
      	else {
      		partBeanWithQuestion.setNoQuestions(false);
      		delivery.setNoQuestions(false);
      	}
      	
        if (iter.hasNext() || itemIndex < (questionCount - 1))
        {
          delivery.setContinue(true);
        }
        else
        {
          delivery.setContinue(false);
        }
        if (itemIndex > 0 || sectionIndex > 0)
        {
          delivery.setPrevious(true);
        }
        else
        {
          delivery.setPrevious(false);
        }
      }
    }

    contents.setCurrentScore(currentScore);
    contents.setMaxScore(maxScore);
    contents.setPartsContents(partsContents);
    contents.setShowStudentScore(delivery.isShowStudentScore());
    return contents;
  }

  /**
     * Populate a SectionContentsBean properties and populate with ItemContentsBean
   * @param part this section
   * @return
   */
  private SectionContentsBean getPartBean(SectionDataIfc part, Map itemGradingHash,
                                          DeliveryBean delivery, Map publishedAnswerHash)
  {
    double maxPoints = 0;
    double points = 0;
    int unansweredQuestions = 0;

    //SectionContentsBean sec = new SectionContentsBean();
    //sec.setSectionId(part.getSectionId().toString()); 
    // daisy change to use this existing constructor instead 11/09/05
    SectionContentsBean sec = new SectionContentsBean(part);

    List<ItemDataIfc> itemSet = null;
    List<ItemDataIfc> itemlist = part.getItemArray();
    long seed = 0;
    if (delivery.getActionMode()==DeliveryBean.GRADE_ASSESSMENT) {
      StudentScoresBean studentscorebean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
      seed = getSeed(part, delivery, (long) studentscorebean.getStudentId().hashCode());
    }
    else {
      seed = getSeed(part, delivery, (long) AgentFacade.getAgentString().hashCode());
    }
    itemSet= getItemArraySortedWithRandom(part, itemlist, seed);

    // i think this is already set by new SectionContentsBean(part) - daisyf
    sec.setQuestions(itemSet.size()); 

    if (delivery.getSettings().getItemNumbering().equals
        (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
    {
      sec.setNumbering(itemSet.size());
    }
    else
    {
      sec.setNumbering( ( (Long) itemGradingHash.get("items")).intValue());
    }

    sec.setText(part.getTitle());
    sec.setDescription(part.getDescription());

    // i think these are already set by new SectionContentsBean(part) - daisyf
    sec.setNumber("" + part.getSequence());
    sec.setMetaData(part);

    Iterator iter = itemSet.iterator();
    List itemContents = new ArrayList();
    int i = 0;
    while (iter.hasNext())
    {
      ItemDataIfc thisitem = (ItemDataIfc) iter.next();
      ItemContentsBean itemBean = getQuestionBean(thisitem, itemGradingHash, 
                                                  delivery, publishedAnswerHash);

      // Deal with numbering
      itemBean.setNumber(++i);
      if (delivery.getSettings().getItemNumbering().equals
          (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
      {
        itemBean.setSequence(Integer.toString(itemBean.getNumber()));
      }
      else
      {
        itemBean.setSequence( ( (Integer) itemGradingHash.get("sequence" +
          thisitem.getItemId().toString())).toString());
      }

      // scoring
      maxPoints += itemBean.getMaxPoints();
      points += itemBean.getExactPoints();
      itemBean.setShowStudentScore(delivery.isShowStudentScore());
      itemBean.setShowStudentQuestionScore(delivery.isShowStudentQuestionScore());

      if (itemBean.isUnanswered())
      {
        unansweredQuestions++;
      }
      itemContents.add(itemBean);
    }

    // scoring information
    sec.setMaxPoints(maxPoints);
    sec.setPoints(points);
    sec.setShowStudentQuestionScore(delivery.isShowStudentQuestionScore());
    sec.setUnansweredQuestions(unansweredQuestions);
    sec.setItemContents(itemContents);
    sec.setAttachmentList(part.getSectionAttachmentList());
    return sec;
  }

  /**
   * Populate a SectionContentsBean properties and populate with ItemContentsBean
   * @param part this section
   * @return
   */
  private SectionContentsBean getPartBeanWithOneQuestion(
    SectionDataIfc part, int itemIndex, Map itemGradingHash,
    DeliveryBean delivery, Map publishedAnswerHash)
  {
    double maxPoints = 0;
    double points = 0;
    int unansweredQuestions = 0;
    int itemCount = 0;

    //SectionContentsBean sec = new SectionContentsBean();
    SectionContentsBean sec = new SectionContentsBean(part);
    List<ItemDataIfc> itemlist = part.getItemArray();
    long seed = getSeed(part, delivery, (long) AgentFacade.getAgentString().hashCode());
    List<ItemDataIfc> itemSet= getItemArraySortedWithRandom(part, itemlist, seed);

    sec.setQuestions(itemSet.size());

    if (delivery.getSettings().getItemNumbering().equals
        (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
    {
      sec.setNumbering(itemSet.size());
    }
    else
    {
      sec.setNumbering( ( (Long) itemGradingHash.get("items")).intValue());
    }

    sec.setText(part.getTitle());
    sec.setDescription(part.getDescription());
    sec.setNumber("" + part.getSequence());

    // get items
    Iterator iter = itemSet.iterator();
    List itemContents = new ArrayList();
    int i = 0;
    while (iter.hasNext())
    {
      ItemDataIfc thisitem = (ItemDataIfc) iter.next();
      ItemContentsBean itemBean = getQuestionBean(thisitem, itemGradingHash,
                                                  delivery, publishedAnswerHash);

      // Numbering
      itemBean.setNumber(++i);
      if (delivery.getSettings().getItemNumbering().equals
          (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
      {
        itemBean.setSequence(Integer.toString(itemBean.getNumber()));
      }
      else
      {
        itemBean.setSequence( ( (Integer) itemGradingHash.get("sequence" +
          thisitem.getItemId().toString())).toString());
      }

      // scoring
      maxPoints += itemBean.getMaxPoints();
      points += itemBean.getExactPoints();
      itemBean.setShowStudentScore(delivery.isShowStudentScore());
      itemBean.setShowStudentQuestionScore(delivery.isShowStudentQuestionScore());

      if (itemBean.isUnanswered())
      {
        unansweredQuestions++;
      }
      if (itemCount++ == itemIndex)
      {
        itemContents.add(itemBean);
      }
    }

    // scoring information
    sec.setMaxPoints(maxPoints);
    sec.setPoints(points);
    sec.setShowStudentQuestionScore(delivery.isShowStudentQuestionScore());
    sec.setUnansweredQuestions(unansweredQuestions);
    sec.setItemContents(itemContents);

    return sec;
  }

  /**
   * populate a single ItemContentsBean from an item for delivery
   * @param item  an Item
   * @return
   */
  private ItemContentsBean getQuestionBean(ItemDataIfc item, Map itemGradingHash,
                                           DeliveryBean delivery, Map publishedAnswerHash)
  {
    ItemContentsBean itemBean = new ItemContentsBean();
    itemBean.setItemData(item);
    itemBean.setMaxPoints(item.getScore().doubleValue());
    itemBean.setPoints( (double) 0);

    // update maxNumAttempts for audio
    if (item.getTriesAllowed() != null)
    {
      itemBean.setTriesAllowed(item.getTriesAllowed());
    }

    // save timeallowed for audio recording
    if (item.getDuration() != null)
    {
      itemBean.setDuration(item.getDuration());
    }

    itemBean.setItemGradingDataArray
      ( (List) itemGradingHash.get(item.getItemId()));

    if (itemBean.getItemGradingDataArray().size() > 0) {
    	itemBean.setItemGradingIdForFilePicker(((ItemGradingData) itemBean.getItemGradingDataArray().get(0)).getItemGradingId());
    }
    // Set comments and points
    Iterator i = itemBean.getItemGradingDataArray().iterator();
    List itemGradingAttachmentList = new ArrayList();
    while (i.hasNext())
    {
      ItemGradingData data = (ItemGradingData) i.next();
      // All itemgradingdata comments for the same item are identical <- u sure? daisyf
      itemBean.setGradingComment(data.getComments());
      if (data.getAutoScore() != null)
      {
        itemBean.setPoints(itemBean.getExactPoints() +
                           data.getAutoScore().doubleValue());
      }
      // set attempts remaining for audio, there is only one itemGradingData
      // per question in this case  
      if (data.getAttemptsRemaining() !=null ){
        itemBean.setAttemptsRemaining(data.getAttemptsRemaining());
      }
      
      // set the itemGradingAttachment only for Review and Grading flows because itemGradingAttachment 
      // can exist in these two flows only (grader can only enter comments for submitted assessments) 
      if (delivery.getActionMode() == 3 || delivery.getActionMode() == 4) {
    	  itemGradingAttachmentList.addAll(data.getItemGradingAttachmentList());
      }
      else {
    	  itemGradingAttachmentList.addAll(new ArrayList<ItemGradingAttachment>());
      }
      //itemBean.setItemGradingAttachmentList(data.getItemGradingAttachmentList());
    }
    
    //If the value close enough to the maximum value just set it to the maximum value (precision issue)
    if (Precision.equals(itemBean.getExactPoints(),itemBean.getMaxPoints(),0.001d)) {
      itemBean.setPoints(itemBean.getMaxPoints());
    }
    
    itemBean.setItemGradingAttachmentList(itemGradingAttachmentList);

    // set question feedback.
    if (item.getTypeId().equals(TypeIfc.ESSAY_QUESTION) ||
        item.getTypeId().equals(TypeIfc.FILE_UPLOAD) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
        item.getTypeId().equals(TypeIfc.AUDIO_RECORDING) ||
        item.getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY))

    {
      itemBean.setFeedback(item.getGeneralItemFeedback());
    }
 
    else if (itemBean.getMaxPoints()>0 && !item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT)) {
        // 
        // This is not really needed because the next Else{} will cover all other question types. 
    	// However it's much cheaper to check scores rather than looping through and check each answers. 
    	// I'm keeping it here.  In most cases, this condition will be met. 
   	
    	if (itemBean.getExactPoints() >= itemBean.getMaxPoints())
    	{
    		
    		itemBean.setFeedback(item.getCorrectItemFeedback());
    	}
    	else
    	{
    		itemBean.setFeedback(item.getInCorrectItemFeedback());
    	}
    }
    else {
    	// run this check if the question is worth 0 points.  see SAK-5669
    	// In this case, we can't just check the scores to determine which feedback to show.
    	// this doesn't happen very often. 
    	
    	
    	List<ItemGradingData> itemgradingList = itemBean.getItemGradingDataArray();
    	Iterator<ItemGradingData> iterAnswer = itemgradingList.iterator();
    	boolean haswronganswer =true;
    	Map fibmap = new HashMap();
    	int mcmc_match_counter = 0;
    	// if no answers yet, then display incorrect feedback. 
    	// if there are answers, then initialize haswronganswer =false;  // correct feedback
    	if (iterAnswer.hasNext()){
    		haswronganswer =false;
    	}
    	
    	//calculate total # of correct answers. 
    	int correctAnswers = 0;
    	if ((item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) || (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) )|| (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) )||(item.getTypeId().equals(TypeIfc.MATCHING) )){
    		Iterator itemTextIter = item.getItemTextArray().iterator();
    		while (itemTextIter.hasNext()){
    			ItemTextIfc itemText = (ItemTextIfc) itemTextIter.next();
    			List answerArray = itemText.getAnswerArray();
    			
    			if (answerArray != null){
    				for (int indexAnswer =0; indexAnswer<answerArray.size(); indexAnswer++){
    					AnswerIfc a = (AnswerIfc) answerArray.get(indexAnswer);
    					if (a.getIsCorrect().booleanValue())
    						correctAnswers++;
    				}
    			}
    		}
    	}

    	//check if there's wrong answer in the answer list, matrix survey question won't affect it, since the answer is always right, 
    	//so don't need to check the matrix survey question
    	while (iterAnswer.hasNext())
    	{
    		
    		ItemGradingData data = iterAnswer.next();
    		
    		  AnswerIfc answer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId());
    		  
    		  if (item.getTypeId().equals(TypeIfc.FILL_IN_BLANK)) {
       			  GradingService gs = new GradingService();
    			  boolean correctanswer = gs.getFIBResult( data, fibmap,  item,  publishedAnswerHash);
    			  if (!correctanswer){
    				  haswronganswer =true;
      		    	break;
    			  }
    			  
    		  }
    		  else if (item.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC)) {
    			  GradingService gs = new GradingService();
    			  try {
    				  boolean correctanswer = gs.getFINResult( data,   item,  publishedAnswerHash);
    				  if (!correctanswer){
        				  haswronganswer =true;
        				  break;
    				  }
    			  }
    			  catch (FormatException e) {
    				  log.debug("should not come to here");
    			  }
    		  }
    		  else if  ((item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) || (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) )||(item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) )||(item.getTypeId().equals(TypeIfc.MATCHING) )){
      		    if ((answer !=null) && (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())){
    		    	haswronganswer =true;
    		    	
    		    	break;
    		    }
      		    else if (answer !=null) {  
      		    	// for matching, if no selection has been made, answer = null.  
      		    	//we don't want to increment mcmc_match_counter if answer is null
      		    	mcmc_match_counter++;
      		    }
    		  }
    		  else {
    			  // for other question types, tf, mcsc, mcmc and matching
     		    if ((answer !=null) && (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())){
    		    	haswronganswer =true;
    		    	break;
    		    }
    		  }
    		   
    	}
    	if ( (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) || (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) )|| (item.getTypeId().equals(TypeIfc.MATCHING) )){
    		if (mcmc_match_counter != correctAnswers){
    			haswronganswer=true;
    		}
    	}
    	
    	if (haswronganswer) {
    		itemBean.setFeedback(item.getInCorrectItemFeedback());
    	}
    	else {
    		
    		itemBean.setFeedback(item.getCorrectItemFeedback());
    	}
    	
    }
    
    
      // Do we randomize answer list?

    boolean randomize = false;
    i = item.getItemMetaDataSet().iterator();
    while (i.hasNext())
    {
      ItemMetaDataIfc meta = (ItemMetaDataIfc) i.next();
      if (meta.getLabel().equals(ItemMetaDataIfc.RANDOMIZE))
      {
        if (meta.getEntry().equals("true"))
        {
          randomize = true;
          break;
        }
      }
    }

    List myanswers = new ArrayList();
    ResourceLoader rb = null;
	rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");

    // Generate the answer key
    String key = "";
    Iterator key1 = item.getItemTextArraySorted().iterator();
    int j = 0;
    while (key1.hasNext())
    {
    	j++;
      // We need to store the answers in an arraylist in case they're
      // randomized -- we assign labels here, and then step through
      // them again later, and we have to make sure the order is the
      // same each time.
      myanswers = new ArrayList(); // Start over each time so we don't
      // get duplicates.
      ItemTextIfc text = (ItemTextIfc) key1.next();
      Iterator key2 = null;

      if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS) && text.isEmiQuestionItemText())
      {
    	  //changes for VULA-1861 - EMI answer key format in delivery is confusing
    	  int correctAnswerCnt = 0;
    	  Iterator answersIter =  text.getAnswerArraySorted().iterator();
    	  while (answersIter.hasNext()) {
    	    AnswerIfc answer = (AnswerIfc) answersIter.next();
    	    if(answer.getIsCorrect()) {
    	    	correctAnswerCnt += 1;
    	    }
    	  }    		
    	  String required=null;    	  
    	  if (text.getRequiredOptionsCount() != null && text.getRequiredOptionsCount().intValue() < correctAnswerCnt) {
    		  required=text.getRequiredOptionsCount().toString();
        	  key += " | " + text.getSequence() + ": " + required + " " + rb.getString("of") + " ";
    	  } else {
        	  key += " | " + text.getSequence() + ": ";
    	  }
      }

      List<Long> alwaysRandomizeTypes = Arrays.asList(TypeIfc.MATCHING);
      List<Long> neverRandomizeTypes = Arrays.asList(TypeIfc.FILL_IN_BLANK,
              TypeIfc.FILL_IN_NUMERIC,
              TypeIfc.MATRIX_CHOICES_SURVEY,
              TypeIfc.CALCULATED_QUESTION,
    		  TypeIfc.IMAGEMAP_QUESTION);

      if (alwaysRandomizeTypes.contains(item.getTypeId()) ||
              (randomize && !neverRandomizeTypes.contains(item.getTypeId())))
      {
            List shuffled = new ArrayList();
            Iterator i1 = text.getAnswerArraySorted().iterator();
            while (i1.hasNext())

        {
          shuffled.add(i1.next());

          // Randomize matching the same way for each
        }

        // Show the answers in the same order that student did.
		String agentString = "";
		if (delivery.getActionMode() == DeliveryBean.GRADE_ASSESSMENT) {
			StudentScoresBean studentscorebean = (StudentScoresBean) ContextUtil
					.lookupBean("studentScores");
			agentString = studentscorebean.getStudentId();
		} else {
			agentString = getAgentString();
		}

        String itemText = (item.getText() == null) ? "" : item.getText();
        Collections.shuffle(shuffled, 
        		new Random( (long) itemText.hashCode() + (getAgentString() + "_" + item.getItemId().toString()).hashCode()));
        /*
        if (item.getTypeId().equals(TypeIfc.MATCHING))
        {
          Collections.shuffle(shuffled,
                              new Random( (long) item.getText().hashCode() +
                getAgentString().hashCode()));
        }
        else
        {
          Collections.shuffle(shuffled,
                              new Random( (long) item.getText().hashCode() +
                                         getAgentString().hashCode()));
        }
        */
        key2 = shuffled.iterator();
      }
      else
      {
        key2 = text.getAnswerArraySorted().iterator();
      }
      int k = 0;
      while (key2.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) key2.next();

        // Don't save the answer if it has no text
        if ( (answer.getText() == null || answer.getText().trim().equals(""))
            && (item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
                item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
                item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
                item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS) ||
                item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
                item.getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY)))
        {
          // Ignore, it's a null answer
        }
        else
        {
          // Set the label and key
          if ((!item.getPartialCreditFlag() && item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE)) ||
              item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
              item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
              item.getTypeId().equals(TypeIfc.MATCHING) ||
              item.getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION))
          {
            answer.setLabel(Character.toString(alphabet.charAt(k++)));
            if (answer.getIsCorrect() != null &&
                answer.getIsCorrect().booleanValue())
            {
              String addition = "";
              if (item.getTypeId().equals(TypeIfc.MATCHING))
              {
                addition = Integer.toString(j) + ":";
              }

              if ("".equals(key))
              {
                key += addition + answer.getLabel();
              }
              else
              {
                key += ", " + addition + answer.getLabel();
              }
            }
          }
          	
          if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS) && text.isEmiQuestionItemText()
        		  && answer.getIsCorrect())
          {
        	  key += answer.getLabel();
          }

          //multiple choice partial credit:
          if (item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) && item.getPartialCreditFlag()){
        	  Double pc =  Double.valueOf(answer.getPartialCredit());
        	  if (pc == null) {
        		  pc = Double.valueOf(0d);
        	  }
        	  if(pc > 0){
        		  if (rb == null) { 	 
        			  rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
        		  }
        		  String correct = rb.getString("alt_correct");
        		  if(("").equals(key)){
        			  key = answer.getLabel() + "&nbsp;<span style='color: green'>(" + pc + "%&nbsp;" + correct + ")</span>";
        		  }else{
        			  key += ",&nbsp;" + answer.getLabel() + "&nbsp;<span style='color: green'>(" + pc + "%&nbsp;" + correct + ")</span>";
        		  }
        	  }
          }

          if (item.getTypeId().equals(TypeIfc.TRUE_FALSE) &&
              answer.getIsCorrect() != null &&
              answer.getIsCorrect().booleanValue())
          {
        	if (rb == null) { 	 
        		rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
        	}
        	if (answer.getText().equalsIgnoreCase("true") || answer.getText().equalsIgnoreCase(rb.getString("true_msg"))) {
        		key = rb.getString("true_msg");
        	}
        	else {
        		key = rb.getString("false_msg");
        	}
          }
          if (item.getTypeId().equals(TypeIfc.FILE_UPLOAD) ||
              item.getTypeId().equals(TypeIfc.ESSAY_QUESTION) ||
              item.getTypeId().equals(TypeIfc.AUDIO_RECORDING))
          {
            key += answer.getText();
          }
          if (item.getTypeId().equals(TypeIfc.FILL_IN_BLANK)||item.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC))
          {
            if ("".equals(key))
            {
              key += answer.getText();
            }
            else
            {
              key += ", " + answer.getText();
            }
          }
          // CALCULATED_QUESTION
          if (item.getTypeId().equals(TypeIfc.CALCULATED_QUESTION))
          {
                key = commaDelimtedCalcQuestionAnswers(item, delivery, itemBean);
          }
          //myanswers will get the answer even for matrix and multiple choices survey
          myanswers.add(answer);
        }
      }
    }
    
    if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS))
    {
  	  key += " | ";
    }

    if (item.getTypeId().equals(TypeIfc.MATCHING)) {
      StringBuilder distractorKeys = new StringBuilder();
      for (ItemTextIfc thisItemText : itemBean.getItemData().getItemTextArray()) {
        boolean hasCorrectAnswer = false;
        for (AnswerIfc thisItemAnswer : thisItemText.getAnswerArray()) {
          if (thisItemAnswer.getIsCorrect()) {
            hasCorrectAnswer = true;
          }
        }
        if (!hasCorrectAnswer) {
          distractorKeys.append(", ").append(thisItemText.getSequence()).append(":").append(Character.toString(alphabet.charAt(myanswers.size())));
        }
      }
      if (distractorKeys.length() > 0) {
          key = key + distractorKeys.toString();
      }
      String individualKeys[] = key.split(",");
      for (int k = 0; k < individualKeys.length; k++) {
        String thisIndividualKey = individualKeys[k].trim();
        individualKeys[k] = thisIndividualKey;
      }
      Arrays.sort(individualKeys);
      StringBuilder sortedKeysBuffer = new StringBuilder();
      for (int k = 0; k < individualKeys.length; k++) {
        if (k == individualKeys.length - 1) {
          sortedKeysBuffer.append(" ").append(individualKeys[k]);
        } else {
          sortedKeysBuffer.append(" ").append(individualKeys[k]).append(",");
        }
      }
      key = sortedKeysBuffer.toString();
    }

    itemBean.setKey(key);

    // Delete this
    itemBean.setShuffledAnswers(myanswers);

    // This creates the list of answers for an item
    List answers = new ArrayList();
    if (item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
        item.getTypeId().equals(TypeIfc.TRUE_FALSE) ||
        item.getTypeId().equals(TypeIfc.MATCHING))
    {
      Iterator iter = myanswers.iterator();
      SelectionBean selectionBean = null;
      
      while (iter.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) iter.next();
    	selectionBean = new SelectionBean();
        selectionBean.setItemContentsBean(itemBean);
        selectionBean.setAnswer(answer);
        
        // It's saved lower case in the db -- this is a kludge
        if (item.getTypeId().equals(TypeIfc.TRUE_FALSE) && // True/False
            answer.getText().equals("true"))
        {
          if (rb == null) { 	 
        	rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
          }
          answer.setText(rb.getString("true_msg"));
        }
        if (item.getTypeId().equals(TypeIfc.TRUE_FALSE) && // True/False
            answer.getText().equals("false"))
        {
          if (rb == null) { 	 
        	rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
          }
          answer.setText(rb.getString("false_msg"));

        }
        String label = "";
        if (answer.getLabel() == null)
        {
          answer.setLabel("");

          // Delete this when everything works.
        }
        if (!answer.getLabel().equals(""))
        {
          label += answer.getLabel() + ". " + answer.getText();
        }
        else
        {
          label = answer.getText();

          // Set the response to true or false for each answer
        }
        selectionBean.setResponse(false); // do this for each answer of choice, why?
        Iterator iter1 = itemBean.getItemGradingDataArray().iterator();
        while (iter1.hasNext())
        {
          ItemGradingData data = (ItemGradingData) iter1.next();
          AnswerIfc pubAnswer = (AnswerIfc) publishedAnswerHash.
                                      get(data.getPublishedAnswerId()); 
          if (pubAnswer != null &&
              (pubAnswer.equals(answer) ||
               data.getPublishedAnswerId().equals(answer.getId())))
          {
            selectionBean.setItemGradingData(data);
            selectionBean.setResponse(true); //<-- is this redundant?
          }
        }

        if (delivery.getFeedbackComponent() != null &&
            delivery.getFeedback().equals("true") &&
            delivery.getFeedbackComponent().getShowSelectionLevel())
        {
          // If right answer, set feedback to correct, otherwise incorrect
          if (answer.getIsCorrect() == null)
          {
            selectionBean.setFeedback(answer.getGeneralAnswerFeedback());
          }
          else if (selectionBean.getResponse() &&
                   answer.getIsCorrect().booleanValue() ||
                   !selectionBean.getResponse() &&
                   !answer.getIsCorrect().booleanValue())
          {
            selectionBean.setFeedback(answer.getCorrectAnswerFeedback());
          }
          else
          {
            selectionBean.setFeedback(answer.getInCorrectAnswerFeedback());

          }
        }

        // Delete this
        String description = "";
        if (delivery.getFeedback().equals("true") &&
            delivery.getFeedbackComponent().getShowCorrectResponse() &&
            answer.getIsCorrect() != null)
        {
          description = answer.getIsCorrect().toString();

          // Delete this
        }
        SelectItem newItem =
          new SelectItem(answer.getId().toString(), label, description);

        if (item.getTypeId().equals(TypeIfc.TRUE_FALSE))
        {
          answers.add(newItem);
        }
        else
        {
          answers.add(selectionBean);
        }
      } //end while
    }
    // Delete this
    itemBean.setAnswers(answers);
    itemBean.setSelectionArray(answers);

    if (item.getTypeId().equals(TypeIfc.MATCHING)) // matching
    {
      populateMatching(item, itemBean, publishedAnswerHash);
    }
    else if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS))
    {
        populateEMI(item, itemBean, publishedAnswerHash);
    }
    else if (item.getTypeId().equals(TypeIfc.FILL_IN_BLANK)) // fill in the blank
    {
      populateFib(item, itemBean, publishedAnswerHash);
    }
    else if (item.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC)) //numeric response
    {
      populateFin(item, itemBean, publishedAnswerHash);
    }
    else if (item.getTypeId().equals(TypeIfc.ESSAY_QUESTION)) 
    {
    	String responseText = itemBean.getResponseText();
    	// SAK-17021
    	// itemBean.setResponseText(FormattedText.convertFormattedTextToPlaintext(responseText));
    	itemBean.setResponseText(responseText);
    }
    else if (item.getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY))
    {
    	populateMatrixChoices(item, itemBean, publishedAnswerHash);
    }
    // CALCULATED_QUESTION
    else if (item.getTypeId().equals(TypeIfc.CALCULATED_QUESTION))
    {
        populateCalculatedQuestion(item, itemBean, delivery);
    }
    else if (item.getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION))
    {
        populateImageMapQuestion(item, itemBean, publishedAnswerHash);
    }
    
    return itemBean;
  }

  // This method treats EMI in a similar way as multiple MCMR questions
  public void populateEMI(ItemDataIfc item, ItemContentsBean bean, Map publishedAnswerHash)
  {
    Iterator itemTextIter = item.getItemTextArraySorted().iterator();
    //int j = 1;
    List beans = new ArrayList();
    List newAnswers = null;
    
    // Iterate through the PublishedItemTexts
    // Each ItemText represents a sub-question question
    // and is used to populate a MatchingBean
    while (itemTextIter.hasNext())
    {
      ItemTextIfc text = (ItemTextIfc) itemTextIter.next();
      
      //Don't use the non-question item ItemTexts
      //i.e. ones which do not contain actual question-answer combos
 	  if (!text.isEmiQuestionItemText()) continue;

      MatchingBean mbean = new MatchingBean();
      newAnswers = new ArrayList();
      mbean.setText(text.getText());
      mbean.setItemSequence(text.getSequence() + "");

      mbean.setItemText(text);
      mbean.setItemContentsBean(bean);

      Iterator itemTextAnwersIter = text.getAnswerArraySorted().iterator();
     
      int i = 0;

      ResourceLoader rb = null;
      if (rb == null) { 	 
  		rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
  	  }
     
      // Now add the user responses (ItemGrading)
      int responseCount = 0;
      List userResponseLabels = new ArrayList();
      Iterator itemGradingIter = bean.getItemGradingDataArray().iterator();
      while (itemGradingIter.hasNext())
      {
        ItemGradingData data = (ItemGradingData) itemGradingIter.next();
        if (data.getPublishedItemTextId().equals(text.getId()))
        {
            // We found an existing grading data for this itemtext (sub-question)
            // mbean.setItemGradingData(data);
            AnswerIfc pubAnswer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId()); 
            if (pubAnswer != null) {
            	userResponseLabels.add(pubAnswer.getLabel());
            	data.setPublishedAnswerId(pubAnswer.getId());
            }
        }
      }

      //Sort the user Response Labels and create the response string
      Collections.sort(userResponseLabels);
      String previousResponse = "";
      Iterator sortedLabels = userResponseLabels.iterator();
      while (sortedLabels.hasNext()) {
    	  previousResponse += sortedLabels.next().toString();
      }
      mbean.setResponse(previousResponse);
      beans.add(mbean);
    }
    
    bean.setMatchingArray(beans);
    bean.setAnswers(newAnswers); // Change the answers to just text
    bean.setIsMultipleItems(beans.size() > 1);
  }

  public void populateMatching(ItemDataIfc item, ItemContentsBean bean, Map publishedAnswerHash)
  {
	  // used only for questions with distractors where the user has selected None of the Above
	  final Long NONE_OF_THE_ABOVE = -1l;
	  
    Iterator iter = item.getItemTextArraySorted().iterator();
    int j = 1;
    List beans = new ArrayList();
    List newAnswers = null;
    while (iter.hasNext())
    {
      ItemTextIfc text = (ItemTextIfc) iter.next();
      MatchingBean mbean = new MatchingBean();
      newAnswers = new ArrayList();
      mbean.setText(Integer.toString(j++) + ". " + text.getText());
      mbean.setItemText(text);
      mbean.setItemContentsBean(bean);

      List choices = new ArrayList();
      List shuffled = new ArrayList();
      Iterator iter2 = text.getAnswerArraySorted().iterator();
      while (iter2.hasNext())
      {
        shuffled.add(iter2.next());

      }
      Collections.shuffle(shuffled,
                          new Random( (long) item.getText().hashCode() +
                          (getAgentString() + "_" + item.getItemId().toString()).hashCode()));

/*
      Collections.shuffle
        (shuffled, new Random( (long) item.getText().hashCode()));
*/
      iter2 = shuffled.iterator();

      int i = 0;
      ResourceLoader rb = null;
      if (rb == null) { 	 
  		rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
  	  }
      choices.add(new SelectItem("0", rb.getString("matching_select"), "")); // default value for choice
      while (iter2.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) iter2.next();
        newAnswers.add(Character.toString(alphabet.charAt(i)) +
                       ". " + answer.getText());
        choices.add(new SelectItem(answer.getId().toString(),
                                   Character.toString(alphabet.charAt(i++)),
                                   ""));
      }
      
      GradingService gs = new GradingService();
      if (gs.hasDistractors(item)) {
        String noneOfTheAboveOption = Character.toString(alphabet.charAt(i++));
        newAnswers.add(noneOfTheAboveOption + "." + " None of the Above");
        choices.add(new SelectItem(NONE_OF_THE_ABOVE.toString(), noneOfTheAboveOption, ""));
      }

      mbean.setChoices(choices); // Set the A/B/C... pulldown

      iter2 = bean.getItemGradingDataArray().iterator();
      while (iter2.hasNext())
      {

        ItemGradingData data = (ItemGradingData) iter2.next();

        if (data.getPublishedItemTextId().equals(text.getId()))
        {
          // We found an existing grading data for this itemtext
          mbean.setItemGradingData(data);
          AnswerIfc pubAnswer = (AnswerIfc) publishedAnswerHash.get(data.getPublishedAnswerId()); 
          if (pubAnswer != null)
          {
            mbean.setAnswer(pubAnswer);
            mbean.setResponse(data.getPublishedAnswerId().toString());
            if (pubAnswer.getIsCorrect() != null &&
                pubAnswer.getIsCorrect().booleanValue())
            {
              mbean.setFeedback(pubAnswer.getCorrectAnswerFeedback());
              mbean.setIsCorrect(Boolean.TRUE);
            }
            else
            {
              mbean.setFeedback(pubAnswer.getInCorrectAnswerFeedback());
              mbean.setIsCorrect(Boolean.FALSE);
            }
          } else if (NONE_OF_THE_ABOVE.equals(data.getPublishedAnswerId())) {
        	  mbean.setResponse(data.getPublishedAnswerId().toString());
          }
          break;
        }
      }

      beans.add(mbean);
    }
    bean.setMatchingArray(beans);
    bean.setAnswers(newAnswers); // Change the answers to just text
  }

  public void populateFib(ItemDataIfc item, ItemContentsBean bean, Map<Long, AnswerIfc> publishedAnswerHash)
  {
    // Only one text in FIB
    ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
    List fibs = new ArrayList();
    String alltext = text.getText();
    List texts = extractFIBFINTextArray(alltext);
    int i = 0;
    Iterator iter = text.getAnswerArraySorted().iterator();
    while (iter.hasNext())
    {
      AnswerIfc answer = (AnswerIfc) iter.next();
      FibBean fbean = new FibBean();
      fbean.setItemContentsBean(bean);
      fbean.setAnswer(answer);
      if(texts.toArray().length>i)
        fbean.setText( (String) texts.toArray()[i++]);
      else
        fbean.setText("");
      fbean.setHasInput(true);

      List<ItemGradingData> datas = bean.getItemGradingDataArray();
      if (datas == null || datas.isEmpty())
      {
        fbean.setIsCorrect(Boolean.FALSE);
      }
      else
      {
        Iterator<ItemGradingData> iter2 = datas.iterator();
        while (iter2.hasNext())
        {
          ItemGradingData data = iter2.next();
          if ((data.getPublishedAnswerId()!=null) && data.getPublishedAnswerId().equals(answer.getId()))
          {
            fbean.setItemGradingData(data);
            fbean.setResponse(FormattedText.convertFormattedTextToPlaintext(data.getAnswerText()));
            if (answer.getText() == null)
            {
              answer.setText("");
            }
            
            if (data.getIsCorrect() == null) {
            	GradingService gs = new GradingService();
            	HashMap<Long, Set<String>> fibmap = new HashMap<Long, Set<String>>();
            	fbean.setIsCorrect(gs.getFIBResult(data, fibmap, item, publishedAnswerHash));
            }
            else {
            	if (data.getIsCorrect().booleanValue()) {
            		fbean.setIsCorrect(Boolean.TRUE);
            	}
            	else {
            		fbean.setIsCorrect(Boolean.FALSE);
            	}
            }
          }
        }
      }
      fibs.add(fbean);
    }

    FibBean fbean = new FibBean();
    if(texts.toArray().length>i)
      fbean.setText( (String) texts.toArray()[i]);
    else
      fbean.setText("");
    fbean.setHasInput(false);
    fibs.add(fbean);

    bean.setFibArray(fibs);
  }

  private static List extractFIBFINTextArray(String alltext)
  {
    List texts = new ArrayList();

    while (alltext.indexOf("{}") > -1)
    {
      int alltextLeftIndex = alltext.indexOf("{}");
      //int alltextRightIndex = alltext.indexOf("}");

      String tmp = alltext.substring(0, alltextLeftIndex);
      alltext = alltext.substring(alltextLeftIndex + 2);
      texts.add(tmp);
      // there are no more "{}", exit loop. 
      // why do we this check? will it ever come to here?
      if (alltextLeftIndex == -1)
      {
        break;
      }
    }
    texts.add(alltext);
    return texts;
  }

  /**
   * Tests that malformed FIB text does not create an excessive number of loops.
   * Quickie test, nice to have: refine, move to JUnit.
   * @param verbose
   * @return
   */
  /*
  private static boolean testExtractFIBTextArray(boolean verbose)
  {
    boolean status = true;
    String[] testsuite = {
      "aaa{bbb}ccc{ddd}eee", // correct
      "aaa{bbb}ccc{", //incorrect
      "aaa{bbb}ccc}", //incorrect
      "aaa{bbb{ccc}ddd}eee" //incorrect
    };

    ArrayList testResult;

    try
    {
      for (int i = 0; i < testsuite.length; i++)
      {
        testResult = extractFIBTextArray(testsuite[i]);
        if (verbose)
        {
          log.debug("Extracting: " + testsuite[i]);
          for (int j = 0; j < testResult.size(); j++)
          {
            log.debug("testResult.get(" + j +
                               ")="+testResult.get(j));
          }
        }
        if (testResult.size() > 10)
        {
          if (verbose)
          {
            log.debug("Extraction failed: exceeded reasonable size.");
          }
          return false;
        }
      }
    }
    catch (Exception ex)
    {
      if (verbose)
      {
        log.debug("Extraction failed: " + ex);
      }
      return false;
    }

    return status;
  } 
  */
   
  public void populateFin(ItemDataIfc item, ItemContentsBean bean, Map<Long, AnswerIfc> publishedAnswerHash)
  {
    // Only one text in FIN
    ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
    List fins = new ArrayList();
    String alltext = text.getText();
    List texts = extractFIBFINTextArray(alltext);
    int i = 0;
    Iterator iter = text.getAnswerArraySorted().iterator();
    while (iter.hasNext())
    {
      AnswerIfc answer = (AnswerIfc) iter.next();
      FinBean fbean = new FinBean();
      fbean.setItemContentsBean(bean);
      fbean.setAnswer(answer);
      if(texts.toArray().length>i)
        fbean.setText( (String) texts.toArray()[i++]);
      else
        fbean.setText("");
      fbean.setHasInput(true);

      List<ItemGradingData> datas = bean.getItemGradingDataArray();
      if (datas == null || datas.isEmpty())
      {
        fbean.setIsCorrect(Boolean.FALSE);
      }
      else
      {
        Iterator<ItemGradingData> iter2 = datas.iterator();
        while (iter2.hasNext())
        {
          ItemGradingData data = iter2.next();
          
          
          log.debug(" " + data.getPublishedAnswerId() + " = " + answer.getId());
          
          if ((data.getPublishedAnswerId()!=null) && (data.getPublishedAnswerId().equals(answer.getId())))
          {
        	  
            fbean.setItemGradingData(data);
            fbean.setResponse(FormattedText.convertFormattedTextToPlaintext(data.getAnswerText()));
            if (answer.getText() == null)
            {
              answer.setText("");
            }
            
            if (data.getIsCorrect() == null) {
            	GradingService gs = new GradingService();
            	HashMap<Long, Set<String>> fibmap = new HashMap<Long, Set<String>>();
            	fbean.setIsCorrect(gs.getFINResult(data, item, publishedAnswerHash));
            }
            else {
            	if (data.getIsCorrect().booleanValue()) {
            		fbean.setIsCorrect(Boolean.TRUE);
            	}
            	else {
            		fbean.setIsCorrect(Boolean.FALSE);
            	}
            }
          }
        }
      }
      fins.add(fbean);
    }

    FinBean fbean = new FinBean();
    if(texts.toArray().length>i)
      fbean.setText( (String) texts.toArray()[i]);
     else
      fbean.setText("");
    fbean.setHasInput(false);
    fins.add(fbean);

    bean.setFinArray(fins);
  }

  public void populateMatrixChoices(ItemDataIfc item, ItemContentsBean bean, Map publishedAnswerHash){

	  List matrixArray = new ArrayList();

	  List<Integer> columnIndexList = new ArrayList<Integer>();
	  List itemTextArray = item.getItemTextArraySorted();
	  List answerArray = ((ItemTextIfc)itemTextArray.get(0)).getAnswerArraySorted(); 
	  MatrixSurveyBean mbean = null;

	  List<String> stringList = new ArrayList<String>();

	  for(int i=0; i<answerArray.size(); i++){
		  String str = ((AnswerIfc) answerArray.get(i)).getText();

		  if(str!=null && str.trim().length()>0) {
			  stringList.add(str);
		  }
	  }

	  for (int k=0; k<stringList.size(); k++){
		  columnIndexList.add(Integer.valueOf(k));
	  }	

	  String [] columnChoices = stringList.toArray(new String[stringList.size()]);
	  List<ItemTextIfc> iList = new ArrayList<ItemTextIfc>();

	  for (int i=0; i<itemTextArray.size(); i++)
	  {
		  String str = ((ItemTextIfc) itemTextArray.get(i)).getText();
		  if (str!=null && str.trim().length()>0)
			  iList.add((ItemTextIfc)itemTextArray.get(i));
	  }

	  for(int i=0; i<iList.size(); i++)
	  {	
		  ItemTextIfc text = iList.get(i);
		  List answers = ((ItemTextIfc)itemTextArray.get(i)).getAnswerArraySorted();
		  List<AnswerIfc> alist = new ArrayList<AnswerIfc>();
		  List<String> slist = new ArrayList<String>();
		  for(int j= 0; j<answers.size(); j++)
		  {
			  if ((AnswerIfc)answers.get(j) != null && !"".equals(((AnswerIfc)answers.get(j)).getText().trim()))	{
				  alist.add((AnswerIfc)answers.get(j));
				  slist.add(((AnswerIfc)answers.get(j)).getId().toString());
			  }
		  }
		  AnswerIfc [] answerIfcs =alist.toArray(new AnswerIfc[alist.size()]);
		  String[] answerSid = slist.toArray(new String[slist.size()]);
		  mbean = new MatrixSurveyBean();
		  mbean.setItemText(text);
		  mbean.setItemContentsBean(bean);
		  mbean.setAnswerArray(answerIfcs);
		  mbean.setAnswerSid(answerSid);
		  List<ItemGradingData> itemGradingArray = bean.getItemGradingDataArray();
		  for (int k=0; k< itemGradingArray.size(); k++)
		  {
			  ItemGradingData data = itemGradingArray.get(k);
			  if((data.getPublishedItemTextId()).longValue() == text.getId().longValue()){
				  mbean.setItemGradingData(data);
				  if (data.getPublishedAnswerId() != null)
					  mbean.setResponseId(data.getPublishedAnswerId().toString());
				  break;
			  }
		  }
		  matrixArray.add(mbean);
	  }
	  bean.setColumnArray(columnChoices);
	  bean.setColumnIndexList(columnIndexList);
	  bean.setMatrixArray(matrixArray);
	  bean.setForceRanking(Boolean.parseBoolean(item.getItemMetaDataByLabel(ItemMetaDataIfc.FORCE_RANKING)));
	  if (item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH) != null)
	  {
		  bean.setRelativeWidth(Integer.parseInt(item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH)));
	  }
	  bean.setAddComment(Boolean.parseBoolean(item.getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX)));
	  bean.setCommentField(item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD));
  }

  /**
   * Tests that malformed FIN text does not create an excessive number of loops.
   * Quickie test, nice to have: refine, move to JUnit.
   * @param verbose
   * @return
   */

  /*

  private static boolean testExtractFINTextArray(boolean verbose)
  {
    boolean status = true;
    String[] testsuite = {
      "aaa{bbb}ccc{ddd}eee", // correct
      "aaa{bbb}ccc{", //incorrect
      "aaa{bbb}ccc}", //incorrect
      "aaa{bbb{ccc}ddd}eee" //incorrect
    };

    ArrayList testResult;

    try
    {
      for (int i = 0; i < testsuite.length; i++)
      {
        testResult = extractFINTextArray(testsuite[i]);
        if (verbose)
        {
          log.debug("Extracting: " + testsuite[i]);
          for (int j = 0; j < testResult.size(); j++)
          {
            log.debug("testResult.get(" + j +
                               ")="+testResult.get(j));
          }
        }
        if (testResult.size() > 10)
        {
          if (verbose)
          {
            log.debug("Extraction failed: exceeded reasonable size.");
          }
          return false;
        }
      }
    }
    catch (Exception ex)
    {
      if (verbose)
      {
        log.debug("Extraction failed: " + ex);
      }
      return false;
    }

    return status;
  }
  */
  
  
  /*
  public static void main (String args[])
  {
    boolean verbose = true;
    if (args.length>0 && "false".equals(args[0]))
    {
      verbose = false;
    }

  }
*/

  public void populateSubmissionsRemaining(PublishedAssessmentService service, PublishedAssessmentIfc pubAssessment, DeliveryBean delivery) {
      AssessmentAccessControlIfc control = pubAssessment.getAssessmentAccessControl();

      int totalSubmissions = service.getTotalSubmission(AgentFacade.getAgentString(), pubAssessment.getPublishedAssessmentId().toString()).intValue();
      delivery.setTotalSubmissions(totalSubmissions);

      if (!(Boolean.TRUE).equals(control.getUnlimitedSubmissions())){
        // when re-takes are allowed always display 1 as number of remaining submission
        int submissionsRemaining = control.getSubmissionsAllowed().intValue() - totalSubmissions;
        if (submissionsRemaining < 1) {
            submissionsRemaining = 1;
        }
        delivery.setSubmissionsRemaining(submissionsRemaining);
      }
  }

  /**
   * CALCULATED_QUESTION
   * This method essentially will convert a CalculatedQuestion item which is initially structured
   * like a Matching item and reshape it into something more akin to a Fill-in-the-blank numeric
   * item.
   */
  public void populateCalculatedQuestion(ItemDataIfc item, ItemContentsBean bean, DeliveryBean delivery)
  {
      long gradingId = determineCalcQGradingId(delivery);
      String agentId = determineCalcQAgentId(delivery, bean);

      HashMap<Integer, String> answersMap = new HashMap<Integer, String>();
      GradingService service = new GradingService();
      // texts is the display text that will show in the question. AnswersMap gets populated with
      // pairs such as key:x, value:42.0
      List<String> texts = service.extractCalcQAnswersArray(answersMap, item, gradingId, agentId);
      String questionText = texts.get(0);

      ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
      List<FinBean> fins = new ArrayList<FinBean>();
      bean.setInstruction(questionText); // will be referenced in table of contents

      int numOfAnswers = answersMap.size();

      int i = 0;
      List<AnswerIfc> calcQuestionEntities = text.getAnswerArraySorted();

      // Here's where I had to do it a little messy, so I'll explain. The variable are like
      // matching pairs so they are stored as answers. But this question has real answers
      // too. So I recycle the answer object I stored the variables in again to represent
      // answers too.
      // I sort this list by answer id so that it will come back from the student in a 
      // predictable order.
      Collections.sort(calcQuestionEntities, new Comparator<AnswerIfc>(){
          public int compare(AnswerIfc a1, AnswerIfc a2) {
              return a1.getId().compareTo(a2.getId());
          }
      });

      Iterator<AnswerIfc> iter = calcQuestionEntities.iterator();
      while (iter.hasNext())
      {
          AnswerIfc answer = iter.next();
          
          // Checks if the 'answer' object is a variable or a real answer
          if(service.extractVariables(answer.getText()).isEmpty()){
              continue;
          }

          FinBean fbean = new FinBean();
          fbean.setItemContentsBean(bean);
          fbean.setAnswer(answer);
          fbean.setText((String) texts.toArray()[i++]);
          fbean.setHasInput(true); // input box

          List<ItemGradingData> datas = bean.getItemGradingDataArray();
          if (datas == null || datas.isEmpty())
          {
              fbean.setIsCorrect(Boolean.FALSE);
          } else {
              for (ItemGradingData data : datas) {

                  if ((data.getPublishedAnswerId()!=null) && (data.getPublishedAnswerId().equals(answer.getId())))
                  {
                      fbean.setItemGradingData(data);
                      fbean.setResponse(FormattedText.convertFormattedTextToPlaintext(data.getAnswerText()));
                      if (answer.getText() == null)
                      {
                          answer.setText("");
                      }
                      fbean.setIsCorrect(service.getCalcQResult(data, item, answersMap, i));
                  }
              }
          }
          fins.add(fbean);
      }

      FinBean fbean = new FinBean();
      if(texts.toArray().length>i)
          fbean.setText( (String) texts.toArray()[i]);
      else
          fbean.setText("");
      fbean.setHasInput(false);
      fins.add(fbean);

      bean.setFinArray((ArrayList) fins);

  }

  public void populateImageMapQuestion(ItemDataIfc item, ItemContentsBean bean, Map publishedAnswerHash)
  {	
	bean.setImageSrc(item.getImageMapSrc());
	
	Iterator iter = item.getItemTextArraySorted().iterator();
    int j = 1;
    List beans = new ArrayList();
    List newAnswers = new ArrayList();
    while (iter.hasNext())
    {
      
      ItemTextIfc text = (ItemTextIfc) iter.next();
      ImageMapQuestionBean mbean = new  ImageMapQuestionBean();
      mbean.setText(Integer.toString(j++) + ". " + text.getText());
      mbean.setItemText(text);
      mbean.setItemContentsBean(bean);

      Iterator iter2 = text.getAnswerArraySorted().iterator();
      
      ResourceLoader rb = null;
      if (rb == null) { 	 
  		rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");
  	  }
      
      while (iter2.hasNext())
      {

        AnswerIfc answer = (AnswerIfc) iter2.next();
        newAnswers.add(answer.getText());
      }
      
      GradingService gs = new GradingService();
      
      iter2 = bean.getItemGradingDataArray().iterator();
      while (iter2.hasNext())
      {
        ItemGradingData data = (ItemGradingData) iter2.next();
        if (data.getPublishedItemTextId().equals(text.getId()))
        {
          mbean.setItemGradingData(data);
		  if (data.getIsCorrect() != null &&
				  data.getIsCorrect().booleanValue())
		  {
			  mbean.setIsCorrect(true);
		  }
		  else
		  {
			  mbean.setIsCorrect(false);
		  }
 
          if (data.getAnswerText() != null)
          {
            mbean.setResponse(data.getAnswerText());
          }
          break;
        }
      }

      beans.add(mbean);
    }
    bean.setMatchingArray(beans);
    bean.setAnswers(newAnswers); // Change the answers to just text
  
  }
  
  public String getAgentString(){
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
    String agentString = person.getId();
    return agentString;
  }

  //added by daisy, used by DeliverBean.addMediaToItemGrading
  public void processAction(ActionEvent ae, boolean resetPageContents) throws
    AbortProcessingException{
    this.resetPageContents = resetPageContents;
    processAction(null);
  }

  public String getPublishedAssessmentId(DeliveryBean delivery){
    String id = ContextUtil.lookupParam("publishedId");
    if (id == null || id.equals("") || id.equals("null")){    
      id = delivery.getAssessmentId();
    }
    return id;
  }

  protected void clearElapsedTime(DeliveryBean delivery){
    if (!delivery.isTimeRunning()) {
      delivery.setTimeElapse(null);
    }
    delivery.setTimeOutSubmission("false");

    if (delivery.getTimeElapse() == null){
      delivery.setTimeElapse("0");
    }
  }

  protected void setFeedbackMode(DeliveryBean delivery){
    int action = delivery.getActionMode();
    String showfeedbacknow = ContextUtil.lookupParam("showfeedbacknow");
    delivery.setFeedback("false");
    delivery.setNoFeedback("false");
    switch (action){
    case 1: // take assessment
    case 2: // preview assessment
    case 5: // take assessment via url
            if (showfeedbacknow != null && showfeedbacknow.equals("true")) {
              delivery.setFeedback("true");
            }
            break;
    case 3: // review assessment
            if (delivery.getFeedbackComponent()!=null 
                && (delivery.getFeedbackComponent().getShowImmediate() 
                	|| delivery.getFeedbackComponent().getShowOnSubmission()
                    || (delivery.getFeedbackComponent().getShowDateFeedback())
                        && delivery.getSettings()!=null
                        && delivery.getSettings().getFeedbackDate()!=null
                        && delivery.getSettings().getFeedbackDate().before(new Date()))) {
              delivery.setFeedback("true");
            }
            break;
    case 4: // grade assessment
            delivery.setFeedback("true");
            break; 
    default:break;
    }

    String nofeedback = ContextUtil.lookupParam("nofeedback");
    if (nofeedback != null && nofeedback.equals("true")) {
      delivery.setNoFeedback("true");
    }
  }

  public PublishedAssessmentFacade getPublishedAssessment(DeliveryBean delivery, String id){
    PublishedAssessmentFacade publishedAssessment = null;
    if (delivery.getPublishedAssessment() != null &&
        delivery.getPublishedAssessment().getPublishedAssessmentId().toString().
        equals(id)) {
      publishedAssessment = delivery.getPublishedAssessment();
    }
    else {
      try{
        publishedAssessment =
          (new PublishedAssessmentService()).getPublishedAssessment(id);
	delivery.setPublishedAssessment(publishedAssessment);
      }
      catch(Exception e){
        log.warn(e.getMessage());
      }
    }
    return publishedAssessment;
  }

  public void setShowStudentScore(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment){
    if (Boolean.TRUE.equals(
        publishedAssessment.getAssessmentFeedback().getShowStudentScore())) {
      if (delivery.getFeedbackComponent()!=null && 
          delivery.getFeedbackComponent().getShowDateFeedback() && !delivery.getFeedbackOnDate())
        delivery.setShowStudentScore(false);
      else
        delivery.setShowStudentScore(true);
    }
    else {
      delivery.setShowStudentScore(false);
    }
  }

  public void setShowStudentQuestionScore(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment){
	int action = delivery.getActionMode();
	// Score should always be shown in grading flow 
	if (DeliveryBean.GRADE_ASSESSMENT == action) {
		delivery.setShowStudentQuestionScore(true);
	}
	else if (Boolean.TRUE.equals(publishedAssessment.getAssessmentFeedback().getShowStudentQuestionScore())) {
      if (delivery.getFeedbackComponent()!=null && 
    		  ((delivery.getFeedbackComponent().getShowDateFeedback() && !delivery.getFeedbackOnDate()) ||
    		  ((DeliveryBean.TAKE_ASSESSMENT == action || DeliveryBean.TAKE_ASSESSMENT_VIA_URL == action) && delivery.getFeedbackComponent().getShowOnSubmission())))
        delivery.setShowStudentQuestionScore(false);
      else
        delivery.setShowStudentQuestionScore(true);
    }
    else {
      delivery.setShowStudentQuestionScore(false);
    }
  }

  private void setDisplayByAssessment(DeliveryBean delivery){
    delivery.getSettings().setFormatByAssessment(true);
    delivery.getSettings().setFormatByPart(false);
    delivery.getSettings().setFormatByQuestion(false);
  }

  protected void setDeliverySettings(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment){
    if (delivery.getSettings() == null){
      BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
      listener.populateBeanFromPub(delivery, publishedAssessment);
    }
  }

  public void overloadItemData(DeliveryBean delivery, Map itemGradingHash,
                               PublishedAssessmentFacade publishedAssessment){

    // We're going to overload itemGradingHash with the sequence in case
    // renumbering is turned off.
    itemGradingHash.put("sequence", 0L);
    long items = 0;
    int sequenceno = 1;
    if (publishedAssessment != null && publishedAssessment.getSectionArraySorted() != null) {    	
    	Iterator i1 = publishedAssessment.getSectionArraySorted().iterator();
    	while (i1.hasNext())
    	{
    		SectionDataIfc section = (SectionDataIfc) i1.next();
    		Iterator i2 = null;

    		List<ItemDataIfc> itemlist = section.getItemArray();
    		long seed = 0;
    		if (delivery.getActionMode()==DeliveryBean.GRADE_ASSESSMENT) {
    			StudentScoresBean studentscorebean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
    			seed = getSeed(section, delivery, (long) studentscorebean.getStudentId().hashCode());
    		}
    		else {
    			seed = getSeed(section, delivery, (long) AgentFacade.getAgentString().hashCode());
    		}
    		List<ItemDataIfc> sortedlist = getItemArraySortedWithRandom(section, itemlist, seed);
    		i2 = sortedlist.iterator();

    		while (i2.hasNext()) {
    			items = items + 1; // bug 464
    			ItemDataIfc item = (ItemDataIfc) i2.next();
    			itemGradingHash.put("sequence" + item.getItemId().toString(), sequenceno++);
    		}
    	}
    }
    itemGradingHash.put("items", items);
  }

  private void setGraderComment(DeliveryBean delivery){
    if (delivery.getAssessmentGrading() != null) {
      delivery.setGraderComment
        (delivery.getAssessmentGrading().getComments());
    }
    else {
      delivery.setGraderComment(null);
    }
  }

  protected void setTimer(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment, boolean fromBeginAssessment, boolean isFirstTimeBegin){
    // i hope to use the property timedAssessment but it appears that this property
    // is not recorded properly in DB - daisyf
    int timeLimit = 0;
    AssessmentAccessControlIfc control = publishedAssessment.getAssessmentAccessControl();
    AssessmentGradingData ag = delivery.getAssessmentGrading();

    delivery.setBeginTime(ag.getAttemptDate());

		// Handle Extended Time Information
		ExtendedTimeDeliveryService extendedTimeDeliveryService = new ExtendedTimeDeliveryService(publishedAssessment);
		if (extendedTimeDeliveryService.hasExtendedTime()) {
			if (extendedTimeDeliveryService.getTimeLimit() > 0)
				publishedAssessment.setTimeLimit(extendedTimeDeliveryService.getTimeLimit());
			publishedAssessment.setDueDate(extendedTimeDeliveryService.getDueDate());
			publishedAssessment.setRetractDate(extendedTimeDeliveryService.getRetractDate());
		}
    
    String timeLimitInSetting = control.getTimeLimit() == null ? "0" : control.getTimeLimit().toString();
    String timeBeforeDueRetract = delivery.getTimeBeforeDueRetract(timeLimitInSetting);
    boolean isTimedAssessmentBySetting = delivery.getHasTimeLimit() && 
    		control.getTimeLimit() != null && control.getTimeLimit().intValue() > 0;
    //boolean turnIntoTimedAssessment = false;
    boolean releaseToAnonymouse = control.getReleaseTo() != null && control.getReleaseTo().indexOf("Anonymous Users")> -1;
    
    // Turn into timed assessment if:
    // 1. Not release to anonymous users
    // 2. Not from Auto Save, File Upload, or Table of Content (skipFlag == true)
    // 3. Not during Retake
    // 4. Not a timed assessment (timed assessment setting is not selected)
    // 5. time limit less than 30 min
    if (!delivery.getTurnIntoTimedAssessment() && !releaseToAnonymouse && !delivery.getSkipFlag() && delivery.getActualNumberRetake() >= delivery.getNumberRetake() && 
    		!isTimedAssessmentBySetting && (!"0".equals(timeBeforeDueRetract) && Integer.parseInt(timeBeforeDueRetract) <= 1800)) {
    	delivery.setTurnIntoTimedAssessment(true);
    }
    
    // reset skipFlag
    delivery.setSkipFlag(false);  
    
    delivery.setTurnIntoTimedAssessment(delivery.getTurnIntoTimedAssessment());
    if (delivery.getTurnIntoTimedAssessment() && !delivery.getHasShowTimeWarning() && (delivery.getDueDate() != null || delivery.getRetractDate() != null)) {
    	delivery.setShowTimeWarning(true);
    	delivery.setHasShowTimeWarning(true);
    }
    else {
    	delivery.setShowTimeWarning(false);
    }

    if (isTimedAssessmentBySetting) {
//    	if (fromBeginAssessment) {
//    		timeLimit = Integer.parseInt(delivery.updateTimeLimit(timeLimitInSetting, timeBeforeDueRetract));
//    	}
//    	else {
//    		if (delivery.getTimeLimit() != null) {
//    			timeLimit = Integer.parseInt(delivery.getTimeLimit());
//    		}
//    	}
    	timeLimit = delivery.evaluateTimeLimit(publishedAssessment,fromBeginAssessment, extendedTimeDeliveryService.getTimeLimit());
    }
    else if (delivery.getTurnIntoTimedAssessment()) {
   		timeLimit = Integer.parseInt(delivery.updateTimeLimit(timeLimitInSetting));
    }
    

    if (timeLimit==0) 
      delivery.setTimeRunning(false);
    else{
      delivery.setTimeRunning(true);
      delivery.setTimeLimit(timeLimit+"");

      // if assessment is half done, load setting saved in DB,
      // else set time elapsed as 0
      if (delivery.isTimeRunning() && delivery.getBeginAssessment()){
    	// no backend timer for non-timed assessment  
    	if (!delivery.getTurnIntoTimedAssessment()) {
    		queueTimedAssessment(delivery, timeLimit, fromBeginAssessment, publishedAssessment, isFirstTimeBegin);
    	}
        delivery.setBeginAssessment(false);
      }
      else{ // in midst of assessment taking, sync it with timedAG
        TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
        TimedAssessmentGradingModel timedAG = queue.get(ag.getAssessmentGradingId());
        if (timedAG != null)
          syncTimeElapsedWithServer(timedAG, delivery, isFirstTimeBegin);
      }

      if (delivery.getLastTimer()==0){
        delivery.setLastTimer((new Date()).getTime()); //set the time when the user click Begin Assessment
      }
    }
  }

  private void queueTimedAssessment(DeliveryBean delivery, int timeLimit, boolean fromBeginAssessment, PublishedAssessmentFacade publishedAssessment, boolean isFirstTimeBegin){
	  AssessmentGradingData ag = delivery.getAssessmentGrading();
	  TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
	  TimedAssessmentGradingModel timedAG = queue.get(ag.getAssessmentGradingId());
	  if (isFirstTimeBegin){
		  timedAG = new TimedAssessmentGradingModel(ag.getAssessmentGradingId(), 
				  timeLimit, timeLimit,
				  new Date(), new Date(), // need modify later
				  false, getTimerId(delivery), publishedAssessment);
		  queue.add(timedAG);
		  
		  delivery.setTimeElapse("0");
	  }
	  else{
		  if (timedAG == null){
			  int timeElapsed  = Math.round((new Date().getTime() - ag.getAttemptDate().getTime())/1000.0f);
			  timedAG = new TimedAssessmentGradingModel(ag.getAssessmentGradingId(), 
					  timeLimit, timeLimit - timeElapsed,
					  new Date(), new Date(), 
					  false, getTimerId(delivery), publishedAssessment);
			  queue.add(timedAG);
			  delivery.setTimeElapse(String.valueOf(timeElapsed));
		  }
		  else {
			  // if timedAG exists && beginAssessment==true, this is dodgy. It means that
			  // users may have exited the assessment via unusual mean (e.g. clicking at
			  // a leftnav bar link). In order to return to the assessment that he is taking,
			  // he must go through the beginAssessment screen again, hence, beginAssessment is set
			  // to true again. In this case, we need to sync up the JScript time with the server time
			  // We need to correct 2 settings based on timedAG: delivery.timeElapse
			  syncTimeElapsedWithServer(timedAG, delivery, false);
		  }
	  }
  }

  private void syncTimeElapsedWithServer(TimedAssessmentGradingModel timedAG, DeliveryBean delivery, boolean isFirstTimeBegin){
	    AssessmentGradingData ag = delivery.getAssessmentGrading();
	    //boolean zeroTimeElapsed = false;
	    /*
	    boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(delivery.getPublishedAssessment().getAssessmentAccessControl().getLateHandling());
	    int timeLimit = Integer.parseInt(delivery.getPublishedAssessment().getAssessmentAccessControl().getTimeLimit().toString());
		// due date
	    if (delivery.getDueDate() != null && !acceptLateSubmission) {
	    	int timeBeforeDue  = Math.round((double)(delivery.getDueDate().getTime() - (new Date()).getTime())/1000.0d); //in sec
	    	if (timeBeforeDue < timeLimit && fromBeginAssessment) {
				delivery.setTimeElapse("0");
				timedAG.setBeginDate(new Date());
				return;
			}
	    }
	    // late submission date
	    if (delivery.getRetractDate() != null) {
	    	int timeBeforeRetract  = Math.round((double)(delivery.getRetractDate().getTime() - (new Date()).getTime())/1000.0d); //in sec
	    	if (timeBeforeRetract < timeLimit && fromBeginAssessment) {
				delivery.setTimeElapse("0");
				timedAG.setBeginDate(new Date());
				return;
			}
	    }

		*/

        // this is to cover the scenerio when user took an assessment, Save & Exit, Then returned at a
        // later time, we need to account for the time taht he used before
        int timeElapsed  = Math.round((new Date().getTime() - ag.getAttemptDate().getTime())/1000.0f);
        log.debug("***setTimeElapsed="+timeElapsed);
	    ag.setTimeElapsed(Integer.valueOf(timeElapsed));

	    // not sure why isLate lost its value, so setting it again here
	    ag.setIsLate(Boolean.FALSE);
	    GradingService gradingService = new GradingService();
	    gradingService.saveOrUpdateAssessmentGradingOnly(ag);
	    delivery.setTimeElapse(ag.getTimeElapsed().toString());
	  }

  protected AssessmentGradingData createAssessmentGrading(
                                PublishedAssessmentFacade publishedAssessment){
    AssessmentGradingData adata = new AssessmentGradingData();
    adata.setAgentId(getAgentString());
    adata.setPublishedAssessmentId(publishedAssessment.getPublishedAssessmentId());
    adata.setForGrade(Boolean.FALSE);
    adata.setAttemptDate(new Date());
    adata.setIsLate(Boolean.FALSE);
    adata.setStatus(Integer.valueOf(0));
    adata.setTotalOverrideScore(Double.valueOf(0));
    adata.setTimeElapsed(Integer.valueOf("0"));
    GradingService gradingService = new GradingService();
    gradingService.saveOrUpdateAssessmentGrading(adata);
    return adata;
  }

  protected void setAttemptDateIfNull(AssessmentGradingData ag){
	  if (ag.getAttemptDate() == null) {
		  ag.setAttemptDate(new Date());
		  GradingService gradingService = new GradingService();
		  gradingService.saveOrUpdateAssessmentGrading(ag);
	  }
  }
  
  /* this method takes the list returned from the data/dao class, and checks for part type and returns a sorted list of items. If part type is not random then return the original list
  */
  private List<ItemDataIfc> getItemArraySortedWithRandom(SectionDataIfc part, List<ItemDataIfc> list, long seed) {

    Integer numberToBeDrawn= null;

    if ((part.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) && (part.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))) {

      // same ordering for each student
      List randomsample = new ArrayList();
      Collections.shuffle(list,  new Random(seed));

      if (part.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN) !=null ) {
        numberToBeDrawn= Integer.valueOf(part.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
      }

      int samplesize = 0;
      if (numberToBeDrawn != null) {
    	  samplesize = numberToBeDrawn.intValue();
      }

      for (int i=0; i<samplesize; i++){
        randomsample.add(list.get(i));
      }
      return randomsample;

    }
    else if((part.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_ORDERING)!=null ) && (part.getSectionMetaDataByLabel(SectionDataIfc.QUESTIONS_ORDERING).equals(SectionDataIfc.RANDOM_WITHIN_PART.toString())) ){
         // same ordering for each student
    Collections.shuffle(list,  new Random(seed));
    return list;

    }
    else {
    Collections.sort(list);
    return list;
    }
  }

  // SAK-6990: if DeliveryActionListener is called by beginAssessment.jsp, a timerId
  // is assigned to timedAssessment that we need to attach to TimedAssessmentGradingModel
  private String getTimerId(DeliveryBean delivery){
    String timerId = (String) ContextUtil.lookupParam("timerId");
    log.debug("***timerId="+timerId);
    return timerId;
  }
  
  private long getSeed(SectionDataIfc sectionData, DeliveryBean delivery, long userSeed) {
	  long seed = userSeed;
	  log.debug("input seed = " + seed);
	  if (sectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE) != null && sectionData.getSectionMetaDataByLabel(SectionDataIfc.RANDOMIZATION_TYPE).equals(SectionDataIfc.PER_SUBMISSION)) {
		  Long id = delivery.getAssessmentGradingId();
		  log.debug("assessmentGradingId = " + id);
		  if (delivery.getActionMode() == 2) { // this happens during preview assessment
			  log.debug("preview assessment: seed = " + seed);
		  }
		  else if (id != null) {
			  seed = (long) (id.toString() + "_" + sectionData.getSectionId().toString()).hashCode();
			  log.debug("seed = " + seed);
		  }
		  else {
			  log.error("assessmentGradingId is null");
		  }
	  }
	  return seed;
  }

  // Set the published assessment status here
  // If it is retracted for edit, redirect to an error page
  protected void setStatus(DeliveryBean delivery, PublishedAssessmentService pubService, Long publishedAssessmentId) {
	Integer status = pubService.getPublishedAssessmentStatus(publishedAssessmentId);
	delivery.getPublishedAssessment().setStatus(status);
  }
  
  /**
   * CALCULATED_QUESTION
   * This returns the comma delimted answer key for display such as "42.1,23.19"
   */
  private String commaDelimtedCalcQuestionAnswers(ItemDataIfc item, DeliveryBean delivery, ItemContentsBean itemBean) {
	  long gradingId = determineCalcQGradingId(delivery);
	  String agentId = determineCalcQAgentId(delivery, itemBean);
	  
	  String keysString = "";
	  GradingService service = new GradingService();
	
	HashMap<Integer, String> answersMap = new HashMap<Integer, String>();
	service.extractCalcQAnswersArray(answersMap, item, gradingId, agentId); // return value not used, answersMap is populated
	
	int answerSequence = 1; // this corresponds to the sequence value assigned in extractCalcQAnswersArray()
	int decimalPlaces = 3;
	while(answerSequence <= answersMap.size()) {
		  String answer = (String)answersMap.get(answerSequence);
		  decimalPlaces = Integer.valueOf(answer.substring(answer.indexOf(',')+1, answer.length()));
		  answer = answer.substring(0, answer.indexOf("|")); // cut off extra data e.g. "|2,3"
		  
		  // We need the key formatted in scientificNotation
		  answer = service.toScientificNotation(answer, decimalPlaces);
		  
		  keysString = keysString.concat(answer + ",");
		  answerSequence++;
	  }
	  if (keysString.length() > 2) {
		  keysString = keysString.substring(0, keysString.length()-1); // truncating the comma on the end
	  }
	  return keysString;
  }
  
  /**
   * CALCULATED_QUESTION
   * We need the agentIds in order to properly set the pseudorandom seed
   * for calculated questions.
   */
  private String determineCalcQAgentId(DeliveryBean delivery, ItemContentsBean itemBean) {
	  String agentId = "";
	  if (delivery.getAssessmentGradingId() != null) { //not a preview
		  	if (delivery.getAssessmentGrading() != null) {
		  		agentId = delivery.getAssessmentGrading().getAgentId();
		  	}
		  	else { // Instructor or TA is accessing the data
		  		if (itemBean.getItemGradingDataArray().size() == 0) {
		  			return "error";
		  		}
		  		Iterator iterForAgent = itemBean.getItemGradingDataArray().iterator();
		  		ItemGradingData dataForAgent = (ItemGradingData) iterForAgent.next();
		  		agentId = dataForAgent.getAgentId();
		  	}
	  }
	  else { // preview
	  	// give the instructor a random value each time for this
	  	agentId = "instructor_preview"; // doesn't really matter for preview
	  }
	  return agentId;
  }
  
  /**
   * CALCULATED_QUESTION
   * We need the gradingIds in order to properly set the pseudorandom seed
   * for calculated questions.
   */
  private long determineCalcQGradingId(DeliveryBean delivery) {
	  long gradingId = 0;
	  if (delivery.getAssessmentGradingId() != null) { //not a preview
		  	gradingId = delivery.getAssessmentGradingId();
	  }
	  else { // preview
	  	// give the instructor a random value each time for this
	  	gradingId = previewGradingId;
	  }
	  return gradingId;
  }
  
}

