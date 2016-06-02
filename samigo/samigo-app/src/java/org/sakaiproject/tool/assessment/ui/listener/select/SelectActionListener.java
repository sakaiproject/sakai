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



package org.sakaiproject.tool.assessment.ui.listener.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBeanie;
import org.sakaiproject.tool.assessment.ui.bean.select.SelectAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.util.ExtendedTimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SelectActionListener
    implements ActionListener {
  private static Logger log = LoggerFactory.getLogger(SelectActionListener.class);
  //private static ContextUtil cu;
  private static BeanSort bs;
  private static BeanSort bs2;
  private static ExtendedTimeService extendedTimeService = null;
  private static String EXTENDED_TIME_KEY = "extendedTime";

  private static final String AVG_SCORE = EvaluationModelIfc.AVERAGE_SCORE.toString();
  private static final String HIGH_SCORE = EvaluationModelIfc.HIGHEST_SCORE.toString();
  private static final String LAST_SCORE = EvaluationModelIfc.LAST_SCORE.toString();

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
    select.setHasAnyAssessmentBeenModified(false);
    
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
    
    GradingService gradingService = new GradingService();
    List list = gradingService.getUpdatedAssessmentList(AgentFacade.getAgentString(), AgentFacade.getCurrentSiteId());
    List updatedAssessmentNeedResubmitList = new ArrayList();
    List updatedAssessmentList = new ArrayList();
    if (list != null && list.size() == 2) {
    	updatedAssessmentNeedResubmitList = (List) list.get(0);
    	updatedAssessmentList = (List) list.get(1);
    }
    
    // filter out the one that the given user do not have right to access
    ArrayList takeableList = getTakeableList(publishedAssessmentList,  h, updatedAssessmentNeedResubmitList, updatedAssessmentList);
    
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
      setTimedAssessment(delivery, f);
      // check pastDue
      if (f.getDueDate()!=null && (new Date()).after(f.getDueDate()))
        delivery.setPastDue(true);
      else
        delivery.setPastDue(false);
      
      if (updatedAssessmentNeedResubmitList.contains(f.getPublishedAssessmentId())) {
    	  delivery.setAssessmentUpdatedNeedResubmit(true);
      }
      else {
    	  delivery.setAssessmentUpdatedNeedResubmit(false);
      }
      
      if (updatedAssessmentList.contains(f.getPublishedAssessmentId())) {
    	  delivery.setAssessmentUpdated(true);
      }
      else {
    	  delivery.setAssessmentUpdated(false);
      }
    	  
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
    processDisplayInfo(select);
    
    // 1. get the most recent submission, or the highest submissions of each assessment for a user, depending on grading option
    ArrayList recentSubmittedList = 
    	publishedAssessmentService.getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption( AgentFacade.getAgentString(), AgentFacade.getCurrentSiteId(),"2".equals(select.getDisplayAllAssessments()));
   
    HashMap publishedAssessmentHash = getPublishedAssessmentHash(publishedAssessmentList);
    ArrayList submittedAssessmentGradingList = new ArrayList();
    //log.info("recentSubmittedList size="+recentSubmittedList.size());
    boolean hasHighest = false;
    boolean hasMultipleSubmission = false;
    HashMap feedbackHash = publishedAssessmentService.getFeedbackHash();
    DeliveryBeanie deliveryAnt = null;
    boolean isUnique = true;
    HashSet<Long> recentSubmittedIds = new HashSet<Long>();
    select.setHasAnyAssessmentRetractForEdit(false);
    for (int k = 0; k < recentSubmittedList.size(); k++) {
    	AssessmentGradingData g = (AssessmentGradingData)
    	recentSubmittedList.get(k);
    	recentSubmittedIds.add(g.getPublishedAssessmentId());
    }

    List containRandomPartAssessmentIds = publishedAssessmentService.getContainRandomPartAssessmentIds(recentSubmittedIds);

    for (int k = 0; k < recentSubmittedList.size(); k++) {
    	hasHighest = false;
    	hasMultipleSubmission = false;

    	AssessmentGradingData g = (AssessmentGradingData)
    	recentSubmittedList.get(k);

        DeliveryBeanie delivery = new DeliveryBeanie();
        delivery.setAssessmentGradingId(g.getAssessmentGradingId());
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
        
        if ((EvaluationModelIfc.HIGHEST_SCORE.toString()).equals(delivery.getScoringOption())){
        	hasHighest=true;
        }
        if (hasHighest && hasMultipleSubmission){
        	select.setHasHighestMultipleSubmission(true);
        }
        
        if(hasMultipleSubmission && ((EvaluationModelIfc.AVERAGE_SCORE.toString()).equals(delivery.getScoringOption()))){
        	select.setHasAverageMultipleSubmissions(true);
        }
        delivery.setAssessmentTitle(g.getPublishedAssessmentTitle());
        delivery.setFeedbackDelivery(getFeedbackDelivery(g.getPublishedAssessmentId(),
                                                 publishedAssessmentHash));
        delivery.setFeedbackComponentOption(getFeedbackComponentOption(g.getPublishedAssessmentId(),
                                                 publishedAssessmentHash));
        delivery.setFeedbackDate(getFeedbackDate(g.getPublishedAssessmentId(),
                                                 publishedAssessmentHash));
        if (g.getFinalScore() != null) {
          delivery.setFinalScore(g.getFinalScore().toString());	
          delivery.setGrade(g.getFinalScore().toString());
          delivery.setRawScore(g.getFinalScore().toString()); // Bug 318 fix. It seems raw score should also be based on final score.
          delivery.setRaw(g.getFinalScore().longValue());
        }

        delivery.setTimeElapse(getTimeElapsed(g.getTimeElapsed()));
        delivery.setSubmissionDate(g.getSubmittedDate());
        delivery.setHasAssessmentBeenModified(getHasAssessmentBeenModified(select, g, publishedAssessmentHash));

        delivery.setSubmitted(true); // records are all submitted for grade
        PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.get(g.getPublishedAssessmentId());
        // check is feedback is available
        String hasFeedback = hasFeedback(p);
        delivery.setFeedback(hasFeedback);
        boolean isAssessmentRetractForEdit = isAssessmentRetractForEdit(p);
        delivery.setIsAssessmentRetractForEdit(isAssessmentRetractForEdit);
        if (isAssessmentRetractForEdit) {
        	select.setHasAnyAssessmentRetractForEdit(true);
        }
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

        delivery.setIsRecordedAssessment(g.getIsRecorded());
        
        // to do: set statistics and time for delivery here.
        submittedAssessmentGradingList.add(delivery);
    }
    
    BeanSort bs = new BeanSort(submittedAssessmentGradingList, "assessmentTitle");
    bs.toStringSort();
    bs.sort();
    
    Iterator iter = submittedAssessmentGradingList.iterator();
    ArrayList averageScoreAssessmentGradingList = new ArrayList();
    
    while (iter.hasNext())
    {
      DeliveryBeanie db = (DeliveryBeanie) iter.next();
      if (db.getScoringOption().equals(EvaluationModelIfc.AVERAGE_SCORE.toString()))
    	  averageScoreAssessmentGradingList.add(db);
    }
    
    String lastPublishedAssessmentId = "";
    HashMap averageScoreMap = new HashMap();
    double totalScores= 0d;
	int totalSubmissions= 0;
	double averageScore = 0d;
    
	for (int i = 0; i < averageScoreAssessmentGradingList.size(); i++)
	{
		DeliveryBeanie db = (DeliveryBeanie) averageScoreAssessmentGradingList.get(i);
		if ((lastPublishedAssessmentId != null && lastPublishedAssessmentId.equals(db.getAssessmentId())) || averageScoreAssessmentGradingList.size() == 1) {
			totalScores += Double.parseDouble(db.getFinalScore());
			totalSubmissions++;
			if (i == averageScoreAssessmentGradingList.size() - 1) {
				averageScore = totalScores/totalSubmissions;
				averageScoreMap.put(db.getAssessmentId(), Double.valueOf(averageScore));
			}
		}
		else {
			if (i > 0) {
				averageScore = totalScores/totalSubmissions;
				averageScoreMap.put(lastPublishedAssessmentId, Double.valueOf(averageScore));
			}
			lastPublishedAssessmentId = db.getAssessmentId(); 
			totalScores = Double.parseDouble(db.getFinalScore());
			totalSubmissions = 1;
			
			if (i == averageScoreAssessmentGradingList.size() - 1) {
				averageScore = totalScores/totalSubmissions;
				averageScoreMap.put(db.getAssessmentId(), Double.valueOf(averageScore));
			}
		}
	}
    
    /// --mustansar
    ArrayList reviewableList=new ArrayList();
    ArrayList recordedList=new ArrayList();
    Iterator it=submittedAssessmentGradingList.iterator();
    String assessmentIdNew="";
    while(it.hasNext()){
    	DeliveryBeanie beanie=(DeliveryBeanie)it.next();
    	String assessmentIdOld= beanie.getAssessmentId();

    	String scoring = beanie.getScoringOption();
    	boolean processRecordedAvg = AVG_SCORE.equals(scoring) && !assessmentIdNew.equals(assessmentIdOld);
    	boolean processRecordedHighestOrLast = (HIGH_SCORE.equals(scoring) || LAST_SCORE.equals(scoring)) && beanie.getIsRecordedAssessment();

    	if (processRecordedAvg || processRecordedHighestOrLast)
    	{
    		beanie.setIsRecordedAssessment(false);
    		DeliveryBeanie recorded=new DeliveryBeanie();
    		recorded.setStatistics(beanie.getStatistics());
    		recorded.setHasRandomDrawPart(beanie.getHasRandomDrawPart());
    		recorded.setDueDate(beanie.getDueDate());
    		recorded.setHasAssessmentBeenModified(beanie.getHasAssessmentBeenModified());
    		recorded.setIsAssessmentRetractForEdit(beanie.getIsAssessmentRetractForEdit());
    		recorded.setPastDue(beanie.getPastDue());
    		recorded.setShowScore(beanie.getShowScore());
    		recorded.setSubTime(beanie.getSubTime());
    		recorded.setAssessmentGradingId(beanie.getAssessmentGradingId());
    		recorded.setAssessmentTitle(beanie.getAssessmentTitle());
    		recorded.setAssessmentId(beanie.getAssessmentId());
    		recorded.setFeedback(beanie.getFeedback());
    		recorded.setFeedbackDate(beanie.getFeedbackDate());
    		recorded.setFeedbackDelivery(beanie.getFeedbackDelivery());
    		recorded.setFeedbackComponentOption(beanie.getFeedbackComponentOption());
    		recorded.setIsRecordedAssessment(true);
    		recorded.setScoringOption(beanie.getScoringOption());

    		// check if assessment allows multiple submissions or if this user has been allowed to submit multiple times
    		Long assessId = NumberUtils.toLong(beanie.getAssessmentId(), -1L);
    		int numSubmissions = (Integer) ObjectUtils.defaultIfNull(h.get(assessId), 0);
    		boolean multiple = beanie.isMultipleSubmissions() || numSubmissions > 1;
    		recorded.setMultipleSubmissions(multiple);
    		
    		if (processRecordedAvg)
    		{
    			assessmentIdNew = beanie.getAssessmentId();
    			recorded.setFinalScore(averageScoreMap.get(assessmentIdNew).toString());
    			recorded.setGrade(averageScoreMap.get(assessmentIdNew).toString());
    			recorded.setRawScore(averageScoreMap.get(assessmentIdNew).toString());
    		}
    		else  // highest or last
    		{
    			recorded.setFinalScore(beanie.getFinalScore());
    			recorded.setGrade(beanie.getGrade());
    			recorded.setRawScore(beanie.getRawScore());
    		}

    		recordedList.add(recorded);
    		reviewableList.add(recorded);
    		reviewableList.add(beanie);  
    	}
    	else if ("2".equals(select.getDisplayAllAssessments())) { 
    		reviewableList.add(beanie);
    	}  
    }
    
    
    if ("2".equals(select.getDisplayAllAssessments())){
    	submittedAssessmentGradingList=reviewableList;    
    }
    else {
    	submittedAssessmentGradingList = recordedList;
    }

    if (!select.isReviewableAscending())
    {
    	Collections.reverse(submittedAssessmentGradingList);
    }

    // set the managed beanlist properties that we need
    select.setTakeableAssessments(takeablePublishedList);
    select.setReviewableAssessments(submittedAssessmentGradingList);

    // If secure delivery modules are installed, then insert their html fragments      
    select.setSecureDeliveryHTMLFragments( "" );
    SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
    if ( secureDelivery.isSecureDeliveryAvaliable() ) {
    	
    	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    	select.setSecureDeliveryHTMLFragments( secureDelivery.getInitialHTMLFragments(request, new ResourceLoader().getLocale() ) );
    }
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
     if ("raw".equals(sort))
     {
	returnType = "raw";
     }
     else if ("grade".equals(sort))
     {
        returnType = "grade";
     }
     else if ("time".equals(sort))
     {
        returnType = "subTime";
     }
     else if ("submitted".equals(sort))
     {
        returnType = "submissionDate";
     }
     else if ("feedbackDate".equals(sort))
     {
        returnType = "feedbackDate";
     }
    }

    return returnType;
  }

  /**@author Mutansar Mehmood 
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processDisplayInfo(SelectAssessmentBean bean) {

	  String displaySubmissions=ContextUtil.lookupParam("selectSubmissions");
	  //String displayRecorded=ContextUtil.lookupParam("recordedSubmissions");
	  if(displaySubmissions!=null && displaySubmissions.equalsIgnoreCase("1")){
		  bean.setDisplayAllAssessments("1");
	  }
	  else{
		  bean.setDisplayAllAssessments("2");
	  }

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

    if (takeOrder != null && !takeOrder.trim().equals("") && !takeOrder.equals("null")) {
      bean.setTakeableSortOrder(takeOrder);
    }

    if (reviewOrder != null && !reviewOrder.trim().equals("")) {
      bean.setReviewableSortOrder(reviewOrder);
    }

    if (takeAscending != null && !takeAscending.trim().equals("") && !takeAscending.equals("null")) {
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
  private ArrayList getTakeableList(ArrayList assessmentList, HashMap h, List updatedAssessmentNeedResubmitList, List updatedAssessmentList) {
    ArrayList takeableList = new ArrayList();
    GradingService gradingService = new GradingService();
    HashMap numberRetakeHash = gradingService.getNumberRetakeHash(AgentFacade.getAgentString());
    HashMap actualNumberRetake = gradingService.getActualNumberRetakeHash(AgentFacade.getAgentString());
    for (int i = 0; i < assessmentList.size(); i++) {
      PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
			// Handle extended time info
			extendedTimeService = new ExtendedTimeService(f);
			if (extendedTimeService.hasExtendedTime()) {
				f.setStartDate(extendedTimeService.getStartDate());
				f.setDueDate(extendedTimeService.getDueDate());
				f.setRetractDate(extendedTimeService.getRetractDate());
				if (extendedTimeService.getTimeLimit() != 0) {
					f.setTimeLimit(extendedTimeService.getTimeLimit());
				}
			}
      if (f.getReleaseTo()!=null && !("").equals(f.getReleaseTo())
          && f.getReleaseTo().indexOf("Anonymous Users") == -1 ) {
        if (isAvailable(f, h, numberRetakeHash, actualNumberRetake, updatedAssessmentNeedResubmitList, updatedAssessmentList)) {
          takeableList.add(f);
        }
      }
    }
    return takeableList;
  }

  public boolean isAvailable(PublishedAssessmentFacade f, HashMap h, HashMap numberRetakeHash, HashMap actualNumberRetakeHash, List updatedAssessmentNeedResubmitList, List updatedAssessmentList) {
    boolean returnValue = false;
    //1. prepare our significant parameters
    Integer status = f.getStatus();
    Date currentDate = new Date();
    Date startDate = f.getStartDate();
    Date dueDate = f.getDueDate();
    Date retractDate = f.getRetractDate();
    boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(f.getLateHandling());
    
    if (!Integer.valueOf(1).equals(status)) {
    	return false;
    }
    
    if (startDate != null && startDate.after(currentDate)) {
    	return false;
    }
    
    if (acceptLateSubmission
            && (dueDate != null && dueDate.before(currentDate))
            && (retractDate == null || retractDate.before(currentDate))) {
    	return false;
    }
    
    if (updatedAssessmentNeedResubmitList.contains(f.getPublishedAssessmentId()) || updatedAssessmentList.contains(f.getPublishedAssessmentId())) {
    	return true;
    }
    
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
    // Tests if dueDate has passed
    if (dueDate != null && dueDate.before(currentDate)) {
    	// DUE DATE HAS PASSED
    	if (acceptLateSubmission) {
    		// LATE SUBMISSION ARE HANDLED: The assessment is available in these situations:
    		//    * Is the first submission
    		//    * A retake has been granted 
    		// (if late submission are handled, a previous test implies that retract date has not yet passed)
			if (totalSubmitted == 0) {
				returnValue = true;
			} else {
				int actualNumberRetake = 0;
				if (actualNumberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
					actualNumberRetake = ((Integer) actualNumberRetakeHash.get(f.getPublishedAssessmentId())).intValue();
				}
				if (actualNumberRetake < numberRetake) {
					returnValue = true;
				} else {
					returnValue = false;
				}
			}
    	} else {
    		// LATE SUBMISSION ARE NOT HANDLED: Test retract date and retakes
    		if (retractDate == null || retractDate.after(currentDate)) {
				int actualNumberRetake = 0;
				if (actualNumberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
					actualNumberRetake = ((Integer) actualNumberRetakeHash.get(f.getPublishedAssessmentId())).intValue();
				}
				if (actualNumberRetake < numberRetake) {
					returnValue = true;
				} else {
					returnValue = false;
				}
    		}
    		else{
	    		// Retract date has passed: Assessment is not available    		
	    		returnValue = false;
    		}
    	}
	}
	else {
		if (totalSubmitted < maxSubmissionsAllowed + numberRetake) {
			returnValue = true;
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
  private String hasFeedback(PublishedAssessmentFacade p){
    String hasFeedback = "na";
    Date currentDate = new Date();
    
    if (p==null) {// published assessment may have been deleted
      //log.info("*** pub has been deleted ="+a.getPublishedAssessmentId());
      return hasFeedback;
    }

    if ((AssessmentFeedbackIfc.IMMEDIATE_FEEDBACK).equals(p.getFeedbackDelivery())
    	|| (AssessmentFeedbackIfc.FEEDBACK_ON_SUBMISSION).equals(p.getFeedbackDelivery())	
        || ((AssessmentFeedbackIfc.FEEDBACK_BY_DATE).equals(p.getFeedbackDelivery()) && p.getFeedbackDate()!= null && currentDate.after(p.getFeedbackDate())))
    {
      hasFeedback="show";
    }
    
    if ((AssessmentFeedbackIfc.FEEDBACK_BY_DATE).equals(p.getFeedbackDelivery()) && (p.getFeedbackDate()!= null && currentDate.before((p.getFeedbackDate()))))
    {
      hasFeedback="blank";
    }
    
    return hasFeedback;
  }

  private boolean isAssessmentRetractForEdit(PublishedAssessmentFacade p){
	  if (p == null) {// published assessment may have been deleted
	      //log.info("*** pub has been deleted ="+a.getPublishedAssessmentId());
	      return false;
	    }
	  
	  if (AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(p.getStatus())) {
		  return true;
	  }
	  return false;
  }
  
  private String hasStats(AssessmentGradingData a, HashMap feedbackHash){
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

  private String showScore(AssessmentGradingData a,
                           String hasFeedback, HashMap feedbackHash){
    String showScore = "na";
    // must meet 2 conditions: hasFeedback==true && feedback.getShowStudentScore()==true
    AssessmentFeedbackIfc f= (AssessmentFeedbackIfc)feedbackHash.get(a.getPublishedAssessmentId());
    if (f!=null && f.getFeedbackComponentOption()!=null) { 
      boolean showScorecore = (Boolean.TRUE).equals(f.getShowStudentScore()) || Integer.valueOf(1).equals(f.getFeedbackComponentOption());
      if (showScorecore && "show".equals(hasFeedback))
        showScore = "show";
      if (showScorecore && "blank".equals(hasFeedback))
        showScore = "blank";
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
	    	return Integer.valueOf(-1);
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
  
  private String getFeedbackComponentOption(Long publishedAssessmentId, HashMap publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(publishedAssessmentId);
	    if (p!=null) {
	      Integer option = p.getFeedbackComponentOption();
	      if (option == null)
   		    return null;
	      else
	    	return option.toString();
	    } else
	      return null;
	  }
  
  private boolean getHasAssessmentBeenModified(SelectAssessmentBean select, AssessmentGradingData g, HashMap publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(g.getPublishedAssessmentId());
	    if (p != null) {
	    	if (!Integer.valueOf(AssessmentIfc.RETRACT_FOR_EDIT_STATUS).equals(p.getStatus()) && g.getSubmittedDate() != null && p.getLastModifiedDate().after(g.getSubmittedDate())) {
	    		log.debug("AssessmentGradingId = " + g.getAssessmentGradingId());
	    		log.debug("LastModifiedDate = " + p.getLastModifiedDate());
	    		log.debug("SubmittedDate = " + g.getSubmittedDate());
	    		select.setHasAnyAssessmentBeenModified(true);
	    		return true;
	    	}
	    }
	    return false;
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
  
  private void setTimedAssessment(DeliveryBeanie delivery, PublishedAssessmentFacade pubAssessment){
	  if (pubAssessment.getTimeLimit() != null) {
		  int seconds = pubAssessment.getTimeLimit().intValue();
		  int hour = 0;
		  int minute = 0;
		  if (seconds>=3600) {
			  hour = Math.abs(seconds/3600);
			  minute =Math.abs((seconds-hour*3600)/60);
		  }
		  else {
			  minute = Math.abs(seconds/60);
		  }
		  delivery.setTimeLimit_hour(hour);
		  delivery.setTimeLimit_minute(minute);
	  }

	  else{
		  delivery.setTimeLimit_hour(0);
		  delivery.setTimeLimit_minute(0);
	  }
  }
}
