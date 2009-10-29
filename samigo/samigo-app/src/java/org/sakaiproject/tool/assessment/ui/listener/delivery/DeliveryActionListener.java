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



package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.ItemGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ContentsDeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FeedbackComponent;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FibBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FinBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class DeliveryActionListener
  implements ActionListener
{

  static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static Log log = LogFactory.getLog(DeliveryActionListener.class);
  //private static ContextUtil cu;
  private boolean resetPageContents = true;

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
      log.debug("**** MacNetscape="+person.getIsMacNetscapeBrowser());
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
      HashMap itemGradingHash = new HashMap();
      GradingService service = new GradingService();
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      AssessmentGradingData ag = null;

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
                if (delivery.getFeedbackComponent().getShowResponse() || delivery.getFeedbackComponent().getShowStudentQuestionScore() || delivery.getFeedbackComponent().getShowGraderComment())
                  itemGradingHash = service.getSubmitData(id, agent, scoringoption);
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
            	  agData = (AssessmentGradingData) service.getLastSubmittedAssessmentGradingByAgentId(id, agent, new Long(assessmentGradingId));
              }
              else {
            	  agData = (AssessmentGradingData) service.getHighestSubmittedAssessmentGrading(id, agent, new Long(assessmentGradingId));
              }
              if (agData == null) {
            	  delivery.setOutcome("reviewAssessmentError");
            	  return;
              }
              log.debug("GraderComments: getComments()" + agData.getComments());
              delivery.setGraderComment(agData.getComments());
              delivery.setAssessmentGradingId(agData.getAssessmentGradingId());
              delivery.setOutcome("takeAssessment");
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
            		  ArrayList thisone = (ArrayList) itemGradingHash.get(data.getPublishedItemId());
            		  if (thisone == null) {
            			  thisone = new ArrayList();
            		  }
            		  thisone.add(data);
            		  itemGradingHash.put(data.getPublishedItemId(), thisone);
            	  }

            	  // For file upload and audio questions, adding the corresponding itemGradingData into itemGradingHash and itemGradingSet to display correctly in delivery
            	  // this hash compose (itemGradingId, array list of MediaData)
            	  HashMap mediaItemGradingHash = service.getMediaItemGradingHash(ag.getAssessmentGradingId()); 
            	  Set<Map.Entry<Long, ArrayList>> set = mediaItemGradingHash.entrySet();
            	  for (Map.Entry<Long, ArrayList> me : set) {
            		  Long publishedItemId = (Long) me.getKey();
            		  ArrayList al = (ArrayList) me.getValue();
            		  ArrayList itemGradingArray = (ArrayList) itemGradingHash.get(publishedItemId);
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
            	  setTimer(delivery, publishedAssessment, true);
            	  setStatus(delivery, pubService, Long.valueOf(id));
            	  
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
              log.debug("****Set begin time " + delivery.getBeginTime());
              log.debug("****Set elapsed time " + delivery.getTimeElapse());
              break;

      default: break;
      }

      // overload itemGradingHash with the sequence in case renumbering is turned off.
      overloadItemData(delivery, itemGradingHash, publishedAssessment);

      // get table of contents
      HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);
      delivery.setTableOfContents(getContents(publishedAssessment, itemGradingHash,
                                              delivery, publishedAnswerHash));
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
  protected AssessmentGradingData setAssessmentGradingFromItemData(DeliveryBean delivery,
                      HashMap itemGradingHash, boolean setNullOK)
  {
    AssessmentGradingData agrading = null;
    Iterator keys = itemGradingHash.keySet().iterator();
    // for each publishedItem, looks like we are getting the 1st itemGradingData
    GradingService gradingService = new GradingService();
    if (keys.hasNext()) 
    {
      ItemGradingData igd = (ItemGradingData) ( (ArrayList) itemGradingHash.get(
        keys.next())).toArray()[0];
      AssessmentGradingData agd = gradingService.load(igd.getAssessmentGradingId().toString());
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
    //log.info("**** delivery grdaing"+delivery.getAssessmentGrading());
  }

  /**
   * Put the setShows on.
   * @param delivery the delivery bean
   */
  private void setDeliveryFeedbackOnforEvaluation(DeliveryBean delivery)
  {
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
          ContextUtil.lookupParam("questionnumber") != null &&
          !ContextUtil.lookupParam("questionnumber").trim().equals(""))
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
                                           HashMap itemGradingHash,
                                           DeliveryBean delivery,
                                           HashMap publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;

    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
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
    DeliveryBean delivery, HashMap itemGradingHash, HashMap publishedAnswerHash)
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
    DeliveryBean delivery, HashMap itemGradingHash, HashMap publishedAnswerHash)
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
    PublishedAssessmentFacade publishedAssessment, HashMap itemGradingHash,
    DeliveryBean delivery, HashMap publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;

    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
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
    int itemIndex, int sectionIndex, HashMap itemGradingHash, 
    DeliveryBean delivery, HashMap publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;
    int sectionCount = 0;

    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
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
    int itemIndex, int sectionIndex, HashMap itemGradingHash,
    DeliveryBean delivery, HashMap publishedAnswerHash)
  {
    ContentsDeliveryBean contents = new ContentsDeliveryBean();
    float currentScore = 0;
    float maxScore = 0;
    int sectionCount = 0;
    int questionCount = 0; // This is to increment the part if we run
    // out of questions
    // get parts
    ArrayList partSet = publishedAssessment.getSectionArraySorted();
    Iterator iter = partSet.iterator();
    ArrayList partsContents = new ArrayList();
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
      ArrayList itemlist = secFacade.getItemArray();
      long seed = getSeed(secFacade, delivery, (long) AgentFacade.getAgentString().hashCode());

      ArrayList sortedlist = getItemArraySortedWithRandom(secFacade, itemlist, seed); 
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
  private SectionContentsBean getPartBean(SectionDataIfc part, HashMap itemGradingHash,
                                          DeliveryBean delivery, HashMap publishedAnswerHash)
  {
    float maxPoints = 0;
    float points = 0;
    int unansweredQuestions = 0;

    //SectionContentsBean sec = new SectionContentsBean();
    //sec.setSectionId(part.getSectionId().toString()); 
    // daisy change to use this existing constructor instead 11/09/05
    SectionContentsBean sec = new SectionContentsBean(part);

    ArrayList itemSet = null;
    ArrayList itemlist = part.getItemArray();
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
    ArrayList itemContents = new ArrayList();
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
    SectionDataIfc part, int itemIndex, HashMap itemGradingHash, 
    DeliveryBean delivery, HashMap publishedAnswerHash)
  {
    float maxPoints = 0;
    float points = 0;
    int unansweredQuestions = 0;
    int itemCount = 0;

    //SectionContentsBean sec = new SectionContentsBean();
    SectionContentsBean sec = new SectionContentsBean(part);
    ArrayList itemlist = part.getItemArray();
    long seed = getSeed(part, delivery, (long) AgentFacade.getAgentString().hashCode());
    ArrayList itemSet= getItemArraySortedWithRandom(part, itemlist, seed);

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
    ArrayList itemContents = new ArrayList();
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
  private ItemContentsBean getQuestionBean(ItemDataIfc item, HashMap itemGradingHash,
                                           DeliveryBean delivery, HashMap publishedAnswerHash)
  {
    ItemContentsBean itemBean = new ItemContentsBean();
    itemBean.setItemData(item);
    itemBean.setMaxPoints(item.getScore().floatValue());
    itemBean.setPoints( (float) 0);

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
      ( (ArrayList) itemGradingHash.get(item.getItemId()));

    if (itemBean.getItemGradingDataArray().size() > 0) {
    	itemBean.setItemGradingIdForFilePicker(((ItemGradingData) itemBean.getItemGradingDataArray().get(0)).getItemGradingId());
    }
    // Set comments and points
    Iterator i = itemBean.getItemGradingDataArray().iterator();
    ArrayList itemGradingAttachmentList = new ArrayList();
    while (i.hasNext())
    {
      ItemGradingData data = (ItemGradingData) i.next();
      // All itemgradingdata comments for the same item are identical <- u sure? daisyf
      itemBean.setGradingComment(data.getComments());
      if (data.getAutoScore() != null)
      {
        itemBean.setPoints(itemBean.getExactPoints() +
                           data.getAutoScore().floatValue());
      }
      // set attempts remaining for audio, there is only one itemGradingData
      // per question in this case  
      if (data.getAttemptsRemaining() !=null ){
        itemBean.setAttemptsRemaining(data.getAttemptsRemaining());
      }
      itemGradingAttachmentList.addAll(data.getItemGradingAttachmentList());
      //itemBean.setItemGradingAttachmentList(data.getItemGradingAttachmentList());
    }
    itemBean.setItemGradingAttachmentList(itemGradingAttachmentList);

    // set question feedback.
    if (item.getTypeId().equals(TypeIfc.ESSAY_QUESTION) ||
        item.getTypeId().equals(TypeIfc.FILE_UPLOAD) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
        item.getTypeId().equals(TypeIfc.AUDIO_RECORDING))
    {
      itemBean.setFeedback(item.getGeneralItemFeedback());
    }
 
    else if ( itemBean.getMaxPoints()>0) {
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
    	
    	
    	ArrayList itemgradingList = itemBean.getItemGradingDataArray();
    	Iterator iterAnswer = itemgradingList.iterator();
    	boolean haswronganswer =true;
    	HashMap fibmap = new HashMap();
    	int mcmc_match_counter = 0;
    	// if no answers yet, then display incorrect feedback. 
    	// if there are answers, then initialize haswronganswer =false;  // correct feedback
    	if (iterAnswer.hasNext()){
    		haswronganswer =false;
    	}
    	
    	//calculate total # of correct answers. 
    	int correctAnswers = 0;
    	if ((item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) )|| (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) )||(item.getTypeId().equals(TypeIfc.MATCHING) )){
    		Iterator itemTextIter = item.getItemTextArray().iterator();
    		while (itemTextIter.hasNext()){
    			ItemTextIfc itemText = (ItemTextIfc) itemTextIter.next();
    			ArrayList answerArray = itemText.getAnswerArray();
    			
    			if (answerArray != null){
    				for (int indexAnswer =0; indexAnswer<answerArray.size(); indexAnswer++){
    					AnswerIfc a = (AnswerIfc) answerArray.get(indexAnswer);
    					if (a.getIsCorrect().booleanValue())
    						correctAnswers++;
    				}
    			}
    		}
    	}
    	//log.debug("correctAnswers: " + correctAnswers);
    	
    	while (iterAnswer.hasNext())
    	{
    		
    		ItemGradingIfc data = (ItemGradingIfc) iterAnswer.next();
    		
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
    			  boolean correctanswer = gs.getFINResult( data,   item,  publishedAnswerHash);
    			  if (!correctanswer){
    				  haswronganswer =true;
      		    	break;
    			  }
    		  }
    		  else if  ((item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) )||(item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) )||(item.getTypeId().equals(TypeIfc.MATCHING) )){
      		    if ((answer !=null) && (answer.getIsCorrect() == null || !answer.getIsCorrect().booleanValue())){
    		    	haswronganswer =true;
    		    	
    		    	break;
    		    }
      		    else if (answer !=null) {  
      		    	// for matching, if no selection has been made, answer = null.  we dont want to increment mcmc_match_counter if answer is null
      		    	
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
    	if ((item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) )|| (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) )|| (item.getTypeId().equals(TypeIfc.MATCHING) )){
    		if (mcmc_match_counter==correctAnswers){
    			haswronganswer=false;
    		}
    		else {
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

    ArrayList myanswers = new ArrayList();
    ResourceLoader rb = null;
    // Generate the answer key
    String key = "";
    Iterator key1 = item.getItemTextArraySorted().iterator();
    int j = 1;
    while (key1.hasNext())
    {
      // We need to store the answers in an arraylist in case they're
      // randomized -- we assign labels here, and then step through
      // them again later, and we have to make sure the order is the
      // same each time.
      myanswers = new ArrayList(); // Start over each time so we don't
      // get duplicates.
      ItemTextIfc text = (ItemTextIfc) key1.next();
      Iterator key2 = null;

      // Never randomize Fill-in-the-blank or Numeric Response, always randomize matching
      if (randomize && !(item.getTypeId().equals(TypeIfc.FILL_IN_BLANK)||item.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC)) || item.getTypeId().equals(TypeIfc.MATCHING))
          {
            ArrayList shuffled = new ArrayList();
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

        Collections.shuffle(shuffled, 
        		new Random( (long) item.getText().hashCode() + agentString.hashCode()));
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
                item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)))
        {
          // Ignore, it's a null answer
        }
        else
        {
          // Set the label and key
          if (item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
              item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
              item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
              item.getTypeId().equals(TypeIfc.MATCHING))
          {
            answer.setLabel(Character.toString(alphabet.charAt(k++)));
            if (answer.getIsCorrect() != null &&
                answer.getIsCorrect().booleanValue())
            {
              String addition = "";
              if (item.getTypeId().equals(TypeIfc.MATCHING))
              {
                addition = Integer.toString(j++) + ":";
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
          myanswers.add(answer);
        }
      }
    }
    itemBean.setKey(key);

    // Delete this
    itemBean.setShuffledAnswers(myanswers);

    // This creates the list of answers for an item
    ArrayList answers = new ArrayList();
    if (item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
        item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
        item.getTypeId().equals(TypeIfc.TRUE_FALSE) ||
        item.getTypeId().equals(TypeIfc.MATCHING))
    {
      Iterator iter = myanswers.iterator();
      while (iter.hasNext())
      {
        SelectionBean selectionBean = new SelectionBean();
        selectionBean.setItemContentsBean(itemBean);
        AnswerIfc answer = (AnswerIfc) iter.next();
        selectionBean.setAnswer(answer);

        // It's saved lower case in the db -- this is a kludge
        if (item.getTypeId().equals(TypeIfc.TRUE_FALSE) && // True/False
            answer.getText().equals("true"))
        {
          answer.setText(rb.getString("true_msg"));
        }
        if (item.getTypeId().equals(TypeIfc.TRUE_FALSE) && // True/False
            answer.getText().equals("false"))
        {
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
      }
    }
    // Delete this
    itemBean.setAnswers(answers);
    itemBean.setSelectionArray(answers);

    if (item.getTypeId().equals(TypeIfc.MATCHING)) // matching
    {
      populateMatching(item, itemBean, publishedAnswerHash);

    }
    else if (item.getTypeId().equals(TypeIfc.FILL_IN_BLANK)) // fill in the blank
    {
      populateFib(item, itemBean);
    }
    else if (item.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC)) //numeric response
    {
      populateFin(item, itemBean);
    }
    else if (item.getTypeId().equals(TypeIfc.ESSAY_QUESTION)) 
    {
      itemBean.setResponseText(FormattedText.convertFormattedTextToPlaintext(itemBean.getResponseText()));
    }
    else if (item.getTypeId().equals(TypeIfc.TRUE_FALSE) || 
    		item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
            item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
            item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ) 
    {
      itemBean.setRationale(FormattedText.convertFormattedTextToPlaintext(itemBean.getRationale()));
    }

    return itemBean;
  }

  public void populateMatching(ItemDataIfc item, ItemContentsBean bean, HashMap publishedAnswerHash)
  {
    Iterator iter = item.getItemTextArraySorted().iterator();
    int j = 1;
    ArrayList beans = new ArrayList();
    ArrayList newAnswers = null;
    while (iter.hasNext())
    {
      ItemTextIfc text = (ItemTextIfc) iter.next();
      MatchingBean mbean = new MatchingBean();
      newAnswers = new ArrayList();
      mbean.setText(Integer.toString(j++) + ". " + text.getText());
      mbean.setItemText(text);
      mbean.setItemContentsBean(bean);

      ArrayList choices = new ArrayList();
      ArrayList shuffled = new ArrayList();
      Iterator iter2 = text.getAnswerArraySorted().iterator();
      while (iter2.hasNext())
      {
        shuffled.add(iter2.next());

      }
      Collections.shuffle(shuffled,
                          new Random( (long) item.getText().hashCode() +
                          getAgentString().hashCode()));

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
              mbean.setIsCorrect(true);
            }
            else
            {
              mbean.setFeedback(pubAnswer.getInCorrectAnswerFeedback());
              mbean.setIsCorrect(false);
            }
          }
          break;
        }
      }

      beans.add(mbean);
    }
    bean.setMatchingArray(beans);
    bean.setAnswers(newAnswers); // Change the answers to just text
  }

  public void populateFib(ItemDataIfc item, ItemContentsBean bean)
  {
    // Only one text in FIB
    ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
    ArrayList fibs = new ArrayList();
    String alltext = text.getText();
    ArrayList texts = extractFIBTextArray(alltext);
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

      ArrayList datas = bean.getItemGradingDataArray();
      if (datas == null || datas.isEmpty())
      {
        fbean.setIsCorrect(false);
      }
      else
      {
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext())
        {
          ItemGradingData data = (ItemGradingData) iter2.next();
          if ((data.getPublishedAnswerId()!=null) && data.getPublishedAnswerId().equals(answer.getId()))
          {
            fbean.setItemGradingData(data);
            fbean.setResponse(FormattedText.convertFormattedTextToPlaintext(data.getAnswerText()));
            fbean.setIsCorrect(false);
            if (answer.getText() == null)
            {
              answer.setText("");
            }
            StringTokenizer st2 = new StringTokenizer(answer.getText(), "|");
            while (st2.hasMoreTokens())
            {
              String nextT = st2.nextToken();
              log.debug("nextT = " + nextT);
//  mark answer as correct if autoscore > 0
 
/*
              if (data.getAnswerText() != null &&
                  data.getAnswerText().equalsIgnoreCase(nextT))
*/
              if (data.getAutoScore() != null &&
                  data.getAutoScore().floatValue() > 0.0)
              {
                fbean.setIsCorrect(true);
              }
// need to check if case sensitive, mutual exclusive.
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

  private static ArrayList extractFIBTextArray(String alltext)
  {
    ArrayList texts = new ArrayList();

    while (alltext.indexOf("{") > -1)
    {
      int alltextLeftIndex = alltext.indexOf("{");
      int alltextRightIndex = alltext.indexOf("}");

      String tmp = alltext.substring(0, alltextLeftIndex);
      alltext = alltext.substring(alltextRightIndex + 1);
      texts.add(tmp);
      // there are no more "}", exit loop
      if (alltextRightIndex == -1)
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
   
  public void populateFin(ItemDataIfc item, ItemContentsBean bean)
  {
    // Only one text in FIN
    ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
    ArrayList fins = new ArrayList();
    String alltext = text.getText();
    ArrayList texts = extractFINTextArray(alltext);
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

      ArrayList datas = bean.getItemGradingDataArray();
      if (datas == null || datas.isEmpty())
      {
        fbean.setIsCorrect(false);
      }
      else
      {
        Iterator iter2 = datas.iterator();
        while (iter2.hasNext())
        {
          ItemGradingData data = (ItemGradingData) iter2.next();
          
          
          log.debug(" " + data.getPublishedAnswerId() + " = " + answer.getId());
          
          if ((data.getPublishedAnswerId()!=null) && (data.getPublishedAnswerId().equals(answer.getId())))
          {
        	  
            fbean.setItemGradingData(data);
            fbean.setResponse(FormattedText.convertFormattedTextToPlaintext(data.getAnswerText()));
            fbean.setIsCorrect(false);
            if (answer.getText() == null)
            {
              answer.setText("");
            }
            StringTokenizer st2 = new StringTokenizer(answer.getText(), "|");
            while (st2.hasMoreTokens())
            {
              String nextT = st2.nextToken();
              log.debug("nextT = " + nextT);
//  mark answer as correct if autoscore > 0
 
/*
              if (data.getAnswerText() != null &&
                  data.getAnswerText().equalsIgnoreCase(nextT))
*/
              if (data.getAutoScore() != null &&
                  data.getAutoScore().floatValue() > 0.0)
               {
                fbean.setIsCorrect(true);
              }
// need to check if case sensitive, mutual exclusive.
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

  private static ArrayList extractFINTextArray(String alltext)
  {
    ArrayList texts = new ArrayList();

    while (alltext.indexOf("{") > -1)
    {
      int alltextLeftIndex = alltext.indexOf("{");
      int alltextRightIndex = alltext.indexOf("}");

      String tmp = alltext.substring(0, alltextLeftIndex);
      alltext = alltext.substring(alltextRightIndex + 1);
      texts.add(tmp);
      // there are no more "}", exit loop
      if (alltextRightIndex == -1)
      {
        break;
      }
    }
    texts.add(alltext);
    return texts;
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

    //log.debug("testExtractFIBTextArray result="+testExtractFIBTextArray(verbose));;

  }
*/
  
  
  public String getAgentString(){
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
    String agentString = person.getId();
    //log.info("***agentString="+agentString);
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
    if (id == null){
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

  public void overloadItemData(DeliveryBean delivery, HashMap itemGradingHash, 
                               PublishedAssessmentFacade publishedAssessment){

    // We're going to overload itemGradingHash with the sequence in case
    // renumbering is turned off.
    itemGradingHash.put("sequence", Long.valueOf(0));
    long items = 0;
    int sequenceno = 1;
    if (publishedAssessment != null && publishedAssessment.getSectionArraySorted() != null) {    	
    	Iterator i1 = publishedAssessment.getSectionArraySorted().iterator();
    	while (i1.hasNext())
    	{
    		SectionDataIfc section = (SectionDataIfc) i1.next();
    		Iterator i2 = null;

    		ArrayList itemlist = section.getItemArray();
    		long seed = 0;
    		if (delivery.getActionMode()==DeliveryBean.GRADE_ASSESSMENT) {
    			StudentScoresBean studentscorebean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
    			seed = getSeed(section, delivery, (long) studentscorebean.getStudentId().hashCode());
    		}
    		else {
    			seed = getSeed(section, delivery, (long) AgentFacade.getAgentString().hashCode());
    		}
    		ArrayList sortedlist = getItemArraySortedWithRandom(section, itemlist, seed);
    		i2 = sortedlist.iterator();

    		while (i2.hasNext()) {
    			items = items + 1; // bug 464
    			ItemDataIfc item = (ItemDataIfc) i2.next();
    			itemGradingHash.put("sequence" + item.getItemId().toString(),
    					Integer.valueOf(sequenceno++));
    		}
    	}
    }
    itemGradingHash.put("items", Long.valueOf(items));
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

  protected void setTimer(DeliveryBean delivery, PublishedAssessmentFacade publishedAssessment, boolean fromBeginAssessment){
    // i hope to use the property timedAssessment but it appears that this property
    // is not recorded properly in DB - daisyf
    int timeLimit = 0;
    AssessmentAccessControlIfc control = publishedAssessment.getAssessmentAccessControl();
    if (delivery.getHasTimeLimit() && control != null && control.getTimeLimit()!=null) {
    	if (fromBeginAssessment) {
    		timeLimit = Integer.parseInt(delivery.updateTimeLimit(publishedAssessment.getAssessmentAccessControl().getTimeLimit().toString()));
    	}
    	else {
    		if (delivery.getTimeLimit() != null) {
    			timeLimit = Integer.parseInt(delivery.getTimeLimit());
    		}
    	}
    }
      
    if (timeLimit==0) 
      delivery.setTimeRunning(false);
    else{
      delivery.setTimeRunning(true);
      delivery.setTimeLimit(timeLimit+"");

      //if assessment is half done, load setting saved in DB,
      // else set time elapsed as 0
      AssessmentGradingData ag = delivery.getAssessmentGrading();
      delivery.setBeginTime(ag.getAttemptDate());
      if (delivery.isTimeRunning() && delivery.getBeginAssessment()){
        if (ag.getTimeElapsed() != null){
          delivery.setTimeElapse(ag.getTimeElapsed().toString());
	}
        else{  // this is a new timed assessment
          delivery.setTimeElapse("0");
	}
        queueTimedAssessment(delivery, timeLimit, fromBeginAssessment, publishedAssessment);
        delivery.setBeginAssessment(false);
      }
      else{ // in midst of assessment taking, sync it with timedAG
        TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
        TimedAssessmentGradingModel timedAG = queue.get(ag.getAssessmentGradingId());
        if (timedAG != null)
          syncTimeElapsedWithServer(timedAG, delivery, fromBeginAssessment);
      }

      if (delivery.getLastTimer()==0){
        delivery.setLastTimer((new Date()).getTime()); //set the time when the user click Begin Assessment
      }
    }
  }

  private void queueTimedAssessment(DeliveryBean delivery, int timeLimit, boolean fromBeginAssessment, PublishedAssessmentFacade publishedAssessment){
    AssessmentGradingData ag = delivery.getAssessmentGrading();
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    TimedAssessmentGradingModel timedAG = queue.get(ag.getAssessmentGradingId());
    if (timedAG == null){
      timedAG = new TimedAssessmentGradingModel(ag.getAssessmentGradingId(), 
                timeLimit, timeLimit - ag.getTimeElapsed().intValue(),
                new Date(), new Date(), // need modify later
		false, getTimerId(delivery), publishedAssessment);
      queue.add(timedAG);
      //log.debug("***0. queue="+queue);
      //log.debug("***1. put timedAG in queue, timedAG="+timedAG);
    }
    else{
      // if timedAG exists && beginAssessment==true, this is dodgy. It means that
      // users may have exited the assessment via unusual mean (e.g. clicking at
      // a leftnav bar link). In order to return to the assessment that he is taking,
      // he must go through the beginAssessment screen again, hence, beginAssessment is set
      // to true again. In this case, we need to sync up the JScript time with the server time
      // We need to correct 2 settings based on timedAG: delivery.timeElapse
      syncTimeElapsedWithServer(timedAG, delivery, fromBeginAssessment);
    }
  }

  private void syncTimeElapsedWithServer(TimedAssessmentGradingModel timedAG, DeliveryBean delivery, boolean fromBeginAssessment){
	    AssessmentGradingData ag = delivery.getAssessmentGrading();
	    //boolean zeroTimeElapsed = false;
	    boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(delivery.getPublishedAssessment().getAssessmentAccessControl().getLateHandling());
	    if (delivery.getDueDate() != null && !acceptLateSubmission) {
	    	int timeBeforeDue  = Math.round((float)(delivery.getDueDate().getTime() - (new Date()).getTime())/1000.0f); //in sec
	    	int timeLimit = Integer.parseInt(delivery.getPublishedAssessment().getAssessmentAccessControl().getTimeLimit().toString());
			if (timeBeforeDue < timeLimit && fromBeginAssessment) {
				//zeroTimeElapsed = true;
				delivery.setTimeElapse("0");
				timedAG.setBeginDate(new Date());
				return;
			}
	    }

        int timeElapsed  = Math.round((float)((new Date()).getTime() - timedAG.getBeginDate().getTime())/1000.0f); //in sec
        // this is to cover the scenerio when user took an assessment, Save & Exit, Then returned at a
        // later time, we need to account for the time taht he used before
        int timeTakenBefore = Math.round(timedAG.getTimeLimit() - timedAG.getTimeLeft()); // in sec
        //log.debug("***time passed before reload next page="+timeElapsed+timeTakenBefore);
	    ag.setTimeElapsed(Integer.valueOf(timeElapsed+timeTakenBefore));

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
    adata.setTotalOverrideScore(Float.valueOf(0));
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
  private ArrayList getItemArraySortedWithRandom(SectionDataIfc part, ArrayList list, long seed) {

    Integer numberToBeDrawn= null;

    if ((part.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) && (part.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))) {

      // same ordering for each student
      ArrayList randomsample = new ArrayList();
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
}
