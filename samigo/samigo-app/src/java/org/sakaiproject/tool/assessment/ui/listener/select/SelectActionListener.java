/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
//import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBeanie;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.bean.select.SelectAssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SelectActionListener
    implements ActionListener {
  private static Log log = LogFactory.getLog(SelectActionListener.class);
  //private static ContextUtil cu;
  private static BeanSort bs;
  private static BeanSort bs2;
  public SelectActionListener() {
  }

  /**
   * @todo need to have grading information
   * @todo add more orderBy logical constants
   * @todo should be able to set ascending/descending order,
    something like:
    ArrayList submittedPublishedList =
    publishedAssessmentService.getAllActivePublishedAssessments(
    this.getSubmittedOrderBy(select), select.isSubmittedAscending());
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
      AbortProcessingException {

    //#0 - permission checking before proceeding - daisyf
    // if it is anonymos login, let it pass 'cos there is no site and authz is 
    // about permission in a site
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    PersonBean personBean = (PersonBean) ContextUtil.lookupBean("person");
    DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
    if (!deliveryBean.getAnonymousLogin() && !authzBean.getTakeAssessment())
      return;

    // get service and managed bean
    PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();
    SelectAssessmentBean select = (SelectAssessmentBean) ContextUtil.lookupBean(
        "select");

    select.setHasHighestMultipleSubmission(false);  // reset property
    
    // look for some sort information passed as parameters
    processSortInfo(select);

    // ----------------- prepare Takeable assessment list -------------
    // 1a. get total no. of submission (for grade) per assessment by the given agent in current site
    HashMap h = publishedAssessmentService.getTotalSubmissionPerAssessment(
                AgentFacade.getAgentString(), AgentFacade.getCurrentSiteId());
    // store it in personBean 'cos we would be using it to check if the total submisison
    // allowed is met later - extra protection to avoid students being too enterprising
    // e.g. open multiple windows so they can ride on the last attempt multiple times.
    personBean.setTotalSubmissionPerAssessmentHash(h);

    // 1b. get all the published assessmnet available in the site
    // note that agentId is not really used
    ArrayList publishedAssessmentList =
        publishedAssessmentService.getBasicInfoOfAllPublishedAssessments(
        AgentFacade.getAgentString(), this.getTakeableOrderBy(select),
        select.isTakeableAscending(), AgentFacade.getCurrentSiteId());

    
    // filter out the one that the given user do not have right to access
    ArrayList takeableList = getTakeableList(publishedAssessmentList,  h);

    // 1c. prepare delivery bean
    ArrayList takeablePublishedList = new ArrayList();
    for (int i = 0; i < takeableList.size(); i++) {
      // note that this object is PublishedAssessmentFacade(assessmentBaseId,
      // title, releaseTo, startDate, dueDate, retractDate,lateHandling,
      // unlimitedSubmissions, submissionsAllowed). It
      // carries the min. info to create an index list. - daisyf
      PublishedAssessmentFacade f = (PublishedAssessmentFacade)
          takeableList.get(i);
      DeliveryBeanie delivery = new DeliveryBeanie();
      delivery.setAssessmentId(f.getPublishedAssessmentId().toString());
      delivery.setAssessmentTitle(f.getTitle());
      delivery.setDueDate(f.getDueDate());
      delivery.setTimeRunning(false);// set to true in BeginDeliveryActionListener

      // check pastDue
      if (f.getDueDate()!=null && (new Date()).after(f.getDueDate()))
        delivery.setPastDue(true);
      else
        delivery.setPastDue(false);
      takeablePublishedList.add(delivery);
    }

    // --------------- prepare Submitted assessment grading list --------------
    // 1. get the most recent submission of a user
    /*
    ArrayList recentSubmittedList =
        publishedAssessmentService.getBasicInfoOfLastSubmittedAssessments(
        AgentFacade.getAgentString(), this.getSubmittedOrderBy(select),
        Boolean.getBoolean( (String) ContextUtil.lookupParam("reviewAscending")));
        */

    List containRandomPartAssessmentIds = publishedAssessmentService.getContainRandomPartAssessmentIds();
    
    // 1. get the most recent submission, or the highest submissions of each assessment for a user, depending on grading option
    ArrayList recentSubmittedList =
        publishedAssessmentService.getBasicInfoOfLastOrHighestSubmittedAssessmentsByScoringOption(
			  AgentFacade.getAgentString(), AgentFacade.getCurrentSiteId());

    HashMap publishedAssessmentHash = getPublishedAssessmentHash(publishedAssessmentList);
    ArrayList submittedAssessmentGradingList = new ArrayList();
    //log.info("recentSubmittedList size="+recentSubmittedList.size());
    boolean hasHighest = false;
    boolean hasMultipleSubmission = false;
    HashMap feedbackHash = publishedAssessmentService.getFeedbackHash();
    for (int k = 0; k < recentSubmittedList.size(); k++) {
        hasHighest = false;
        hasMultipleSubmission = false;

      AssessmentGradingFacade g = (AssessmentGradingFacade)
          recentSubmittedList.get(k);

        DeliveryBeanie delivery = new DeliveryBeanie();
        delivery.setAssessmentId(g.getPublishedAssessmentId().toString());
        
        Integer submissionAllowed = getSubmissionAllowed(g.getPublishedAssessmentId(), publishedAssessmentHash);
        if (submissionAllowed.intValue() == -1) {
        	log.debug("submissionAllowed == -1");
        	continue;
        }
        if (submissionAllowed.intValue() == 0) { // unlimited submissions
          delivery.setMultipleSubmissions(true);
      	  hasMultipleSubmission=true;
        }
        else if (submissionAllowed.intValue() == 1) {
       		delivery.setMultipleSubmissions(false);
       		hasMultipleSubmission=false;
       	}
        else if (submissionAllowed.intValue() > 1) {
       		delivery.setMultipleSubmissions(true);
       		hasMultipleSubmission=true;
       	}
        
        delivery.setScoringOption(getScoringType(g.getPublishedAssessmentId(), publishedAssessmentHash));
        
        /*
        PublishedAssessmentIfc pub = publishedAssessmentService.getPublishedAssessment(delivery.getAssessmentId());
        AssessmentAccessControlIfc ac = pub.getAssessmentAccessControl();
         
        if (ac.getSubmissionsAllowed()!=null){
          	if (ac.getSubmissionsAllowed().intValue()> 1){
          		delivery.setMultipleSubmissions(true);
          		hasMultipleSubmission=true;
          	}
          	else {
          		delivery.setMultipleSubmissions(false);
          	}
          }
          else {
        	  delivery.setMultipleSubmissions(true);
        	  hasMultipleSubmission=true;
          }
                
        delivery.setScoringOption(pub.getEvaluationModel().getScoringType().toString());
        */
        
        if ((EvaluationModelIfc.HIGHEST_SCORE.toString()).equals(delivery.getScoringOption())){
        	hasHighest=true;
        }
        if (hasHighest && hasMultipleSubmission){
        	select.setHasHighestMultipleSubmission(true);
        }
        delivery.setAssessmentTitle(g.getPublishedAssessmentTitle());
        delivery.setFeedbackDelivery(getFeedbackDelivery(g.getPublishedAssessmentId(),
                                                 publishedAssessmentHash));
        delivery.setFeedbackDate(getFeedbackDate(g.getPublishedAssessmentId(),
                                                 publishedAssessmentHash));
        if (g.getFinalScore() != null) {
          delivery.setGrade(g.getFinalScore().toString());
          delivery.setRawScore(g.getFinalScore().toString()); // Bug 318 fix. It seems raw score should also be based on final score.
	  delivery.setRaw(g.getFinalScore().longValue());
        }

        delivery.setTimeElapse(getTimeElapsed(g.getTimeElapsed()));
        delivery.setSubmissionDate(g.getSubmittedDate());
	/*
        if (g.getSubmittedDate() != null && g.getAttemptDate() != null) {
          long time = g.getSubmittedDate().getTime() -
              g.getAttemptDate().getTime();
          //log.info("Time = " + time);
          long hours = time / (1000 * 60 * 60);
          long remainder = time - (hours * 1000 * 60 * 60);
          long minutes = remainder / (1000 * 60);
          delivery.setSubTime(time);
          delivery.setSubmissionHours(Long.toString(hours));
          delivery.setSubmissionMinutes(Long.toString(minutes));
        }
        else {
          delivery.setSubmissionHours("n/a");
          delivery.setSubmissionMinutes("n/a");
        }
	*/

        delivery.setSubmitted(true); // records are all submitted for grade
        // check is feedback is available
        String hasFeedback = hasFeedback(g, publishedAssessmentHash);
        delivery.setFeedback(hasFeedback);
        if (containRandomPartAssessmentIds.contains(g.getPublishedAssessmentId())) {
        	delivery.setHasRandomDrawPart(true);
        }
        else {
        	delivery.setHasRandomDrawPart(false);
        }
        // check if score is available
        //HashMap feedbackHash = publishedAssessmentService.getFeedbackHash();
        delivery.setShowScore(showScore(g, hasFeedback, feedbackHash));

        String hasStats = hasStats(g, feedbackHash);
        delivery.setStatistics(hasStats);

        // to do: set statistics and time for delivery here.
        submittedAssessmentGradingList.add(delivery);
    }
    // to do: set statistics and time for delivery here.

    ArrayList scored = new ArrayList();
    ArrayList notScored = new ArrayList();
    Iterator iter = submittedAssessmentGradingList.iterator();
    while (iter.hasNext())
    {
      DeliveryBeanie db = (DeliveryBeanie) iter.next();
      if (getSubmittedOrderBy(select).equals("raw") &&
           db.getShowScore().equals("true"))
        scored.add(db);
      else
        notScored.add(db);
    }

    bs = new BeanSort(scored, getSubmittedOrderBy(select));
    bs2 = new BeanSort(notScored, getSubmittedOrderBy(select));

     if (getSubmittedOrderBy(select).equals("subTime")||getSubmittedOrderBy(select).equals("raw") )
    {
      bs.toNumericSort();
      bs2.toNumericSort();
    }
    else if ( getSubmittedOrderBy(select).equals("submissionDate")||getSubmittedOrderBy(select).equals("feedbackDate"))
    {
         bs.toDateSort();
         bs2.toDateSort();
    }
    else
    {
         bs.toStringSort();
         bs2.toStringSort();
    }
    bs.sort();
    bs2.sort();

    submittedAssessmentGradingList = new ArrayList();
    submittedAssessmentGradingList.addAll(scored);
    submittedAssessmentGradingList.addAll(notScored);

    if (!select.isReviewableAscending())
    {
	Collections.reverse(submittedAssessmentGradingList);
    }

    // set the managed beanlist properties that we need
    select.setTakeableAssessments(takeablePublishedList);
    select.setReviewableAssessments(submittedAssessmentGradingList);

  }

  /**
   * get orderby parameter for takable table
   * @param select the SelectAssessment bean
   * @return
   */
  private String getTakeableOrderBy(SelectAssessmentBean select) {
    String sort = select.getTakeableSortOrder();
    //bean.setTakeableSortOrder(PublishedAssessmentFacadeQueries.TITLE);
    String returnType =  PublishedAssessmentFacadeQueries.TITLE;
    if (sort != null && sort.equals("due"))
    {
	returnType = PublishedAssessmentFacadeQueries.DUE;
    }

    return returnType;
  }

  /**
   * get orderby parameter for submitted table
   * @param select the SelectAssessment bean
   * @return
   */
  private String getSubmittedOrderBy(SelectAssessmentBean select) {
    String sort = select.getReviewableSortOrder();
    String returnType = "assessmentTitle";
    if (sort != null)
    {
     if (sort.equals("raw"))
     {
	returnType = "raw";
     }
     else if (sort.equals("grade"))
     {
        returnType = "grade";
     }
     else if (sort.equals("time"))
     {
        returnType = "subTime";
     }
     else if (sort.equals("submitted"))
     {
        returnType = "submissionDate";
     }
     else if (sort.equals("feedbackDate"))
     {
        returnType = "feedbackDate";
     }
    }

    return returnType;
  }

  /**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(SelectAssessmentBean bean) {
    String takeOrder = ContextUtil.lookupParam("takeableSortType");
    String reviewOrder = ContextUtil.lookupParam("reviewableSortType");
    String reviewAscending = ContextUtil.lookupParam("reviewableAscending");
    String takeAscending = ContextUtil.lookupParam("takeAscending");

    if (takeOrder != null && !takeOrder.trim().equals("")) {
      bean.setTakeableSortOrder(takeOrder);
    }

    if (reviewOrder != null && !reviewOrder.trim().equals("")) {
      bean.setReviewableSortOrder(reviewOrder);
    }

    if (takeAscending != null && !takeAscending.trim().equals("")) {
      try {
        bean.setTakeableAscending((Boolean.valueOf(takeAscending)).booleanValue());
      }
      catch (Exception ex) { //skip
        log.warn(ex.getMessage());
      }
    }
    else
    {
	bean.setTakeableAscending(true);
    }

    if (reviewAscending != null && !reviewAscending.trim().equals("")) {
      try {
        bean.setReviewableAscending(Boolean.valueOf(reviewAscending).booleanValue());
      }
      catch (Exception ex) { //skip
       log.warn(ex.getMessage());
      }
    }
    else
    {
	bean.setReviewableAscending(true);
    }

  }

  // 3. go through the pub list retrieved from DB and check if
  // agent is authorizaed and filter out the one that does not meet the
  // takeable criteria.
  // SAK-1464: we also want to filter out assessment released To Anonymous Users
  private ArrayList getTakeableList(ArrayList assessmentList, HashMap h) {
    ArrayList takeableList = new ArrayList();
    GradingService gradingService = new GradingService();
    HashMap numberRetakeHash = gradingService.getNumberRetakeHash(AgentFacade.getAgentString());
    HashMap actualNumberRetake = gradingService.getActualNumberRetakeHash(AgentFacade.getAgentString());
    for (int i = 0; i < assessmentList.size(); i++) {
      PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
      if (f.getReleaseTo()!=null && !("").equals(f.getReleaseTo())
          && f.getReleaseTo().indexOf("Anonymous Users") == -1 ) {
        if (isAvailable(f, h, numberRetakeHash, actualNumberRetake))
          takeableList.add(f);
      }
    }
    return takeableList;
  }

  public boolean isAvailable(PublishedAssessmentFacade f, HashMap h, HashMap numberRetakeHash, HashMap actualNumberRetakeHash) {
    boolean returnValue = false;
    //1. prepare our significant parameters
    Integer status = f.getStatus();
    Date currentDate = new Date();
    Date startDate = f.getStartDate();
    Date retractDate = f.getRetractDate();
    Date dueDate = f.getDueDate();
    
    if (!Integer.valueOf(1).equals(status)) {
    	return false;
    }
    boolean acceptLateSubmission = AssessmentAccessControlIfc.
        ACCEPT_LATE_SUBMISSION.equals(
        f.getLateHandling());
    int maxSubmissionsAllowed = 9999;
    if ( (Boolean.FALSE).equals(f.getUnlimitedSubmissions())){
      maxSubmissionsAllowed = f.getSubmissionsAllowed().intValue();
    }

    int numberRetake = 0;
    if (numberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
    	numberRetake = (((StudentGradingSummaryData) numberRetakeHash.get(f.getPublishedAssessmentId())).getNumberRetake()).intValue();
    }
    int totalSubmitted = 0;
    
    //boolean notSubmitted = false;
    if (h.get(f.getPublishedAssessmentId()) != null){
      totalSubmitted = ( (Integer) h.get(f.getPublishedAssessmentId())).
          intValue();
    }
    
      //2. time to go through all the criteria
    if (retractDate == null || retractDate.after(currentDate)) {
    	if (startDate == null || startDate.before(currentDate)) {
			if (dueDate != null && dueDate.before(currentDate)) {
				if (acceptLateSubmission) {
					if (totalSubmitted == 0) {
						return true;
					}
				}
				int actualNumberRetake = 0;
				if (actualNumberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
					actualNumberRetake = ((Integer) actualNumberRetakeHash.get(f.getPublishedAssessmentId())).intValue();
				}
				if (actualNumberRetake < numberRetake) {
					returnValue = true;
				}
			}
			else {
				if (totalSubmitted < maxSubmissionsAllowed + numberRetake) {
					returnValue = true;
				}
			}
    	}
    }
    
    return returnValue;
  }

  /** submitted list = pub assessment that either has immediate feedback or that
   * the feedback date has past. If users has submitted to any of these assessment
   * we also want to show their submitted date. If they have not submitted to
   * these assessment, they still should be able to access it.The list returns
   * contains AssessmentGradingData with the PublishedAssessment Id and title.
   */
  private String hasFeedback(AssessmentGradingFacade a, HashMap publishedAssessmentHash){
    String hasFeedback = "false";
    Date currentDate = new Date();
    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
        get(a.getPublishedAssessmentId());
    //log.info("****LOOG PublishedAssessmentFacade = "+a.getPublishedAssessmentId());
    if (p==null) {// published assessment may have been deleted
      //log.info("*** pub has been deleted ="+a.getPublishedAssessmentId());
      return hasFeedback;
    }

    if ((AssessmentFeedbackIfc.IMMEDIATE_FEEDBACK).equals(p.getFeedbackDelivery())
    	|| (AssessmentFeedbackIfc.FEEDBACK_ON_SUBMISSION).equals(p.getFeedbackDelivery())	
        || ((AssessmentFeedbackIfc.FEEDBACK_BY_DATE).equals(p.getFeedbackDelivery()) && p.getFeedbackDate()!= null && currentDate.after(p.getFeedbackDate())))
    {
      hasFeedback="true";
    }
    return hasFeedback;
  }


  private String hasStats(AssessmentGradingFacade a, HashMap feedbackHash){
    String hasStats = "false";

    AssessmentFeedbackIfc f= (AssessmentFeedbackIfc)feedbackHash.get(a.getPublishedAssessmentId());

    if (f!=null){
	if ( (Boolean.TRUE).equals(f.getShowStatistics()))
       {
	hasStats = "true";
       }
    }
    //log.debug("hasStats == " + hasStats);
    return hasStats;
  }

  private String showScore(AssessmentGradingFacade a,
                           String hasFeedback, HashMap feedbackHash){
    String showScore = "false";
    // must meet 2 conditions: hasFeedback==true && feedback.getShowStudentScore()==true
    AssessmentFeedbackIfc f= (AssessmentFeedbackIfc)feedbackHash.get(a.getPublishedAssessmentId());
    if (f!=null){
      if ( (Boolean.TRUE).equals(f.getShowStudentScore()) &&
          hasFeedback.equals("true"))
        showScore = "true";
    }
    return showScore;
  }

  private String getScoringType(Long publishedAssessmentId, HashMap publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(publishedAssessmentId);
	    if (p!=null) {
	    	if (p.getScoringType() != null) {
	    		return p.getScoringType().toString();
	    	}
	    }
        return null;
  }
  

  private Integer getSubmissionAllowed(Long publishedAssessmentId, HashMap publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(publishedAssessmentId);
	    if (p!=null)
	    	return p.getSubmissionsAllowed();
	    else {
	    	log.debug("The published assessment is not valid");
	    	return new Integer(-1);
	    }
  }
  
  private Date getFeedbackDate(Long publishedAssessmentId, HashMap publishedAssessmentHash){
    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
        get(publishedAssessmentId);
    if (p!=null)
      return p.getFeedbackDate();
    else
      return null;
  }

  private String getFeedbackDelivery(Long publishedAssessmentId, HashMap publishedAssessmentHash){
    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
        get(publishedAssessmentId);
    if (p!=null)
      return p.getFeedbackDelivery().toString();
    else
      return null;
  }

  private String getTimeElapsed(Integer s){
    String timeElapsedInString = "n/a";
    if (s!=null && s.intValue() >0){
      int totalSec = s.intValue();
      int hr = totalSec / 3600;
      int min = (totalSec % 3600)/60;
      int sec = (totalSec % 3600)%60;
      timeElapsedInString = "";
      if (hr > 0) timeElapsedInString = hr + " hr ";
      if (min > 0) timeElapsedInString = timeElapsedInString + min + " min ";
      if (sec > 0) timeElapsedInString = timeElapsedInString + sec + " sec ";
    }
    return timeElapsedInString;
  }

  public HashMap getPublishedAssessmentHash(ArrayList publishedAssessmentList){
    HashMap h = new HashMap();
    for (int i=0; i<publishedAssessmentList.size();i++){
      PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentList.get(i);
      h.put(p.getPublishedAssessmentId(), p);
    }
    return h;
  }
  
}
