/*
 * Copyright (c) 2016, The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.sakaiproject.tool.assessment.ui.listener.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
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
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBeanie;
import org.sakaiproject.tool.assessment.ui.bean.select.SelectAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class SelectActionListener implements ActionListener {
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
    DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
    PersonBean personBean = (PersonBean) ContextUtil.lookupBean("person");
    SelectAssessmentBean select = (SelectAssessmentBean) ContextUtil.lookupBean("select");
    if (!deliveryBean.isAnonymousLogin() && !authzBean.getTakeAssessment() && StringUtils.isEmpty(select.getReviewAssessmentId())) {
      log.debug("Early return - deliveryBean.isAnonymousLogin() {}; authzBean.getTakeAssessment() {};", deliveryBean.isAnonymousLogin(), authzBean.getTakeAssessment());
      return;
    }

    String siteId = AgentFacade.getCurrentSiteId() != null ? AgentFacade.getCurrentSiteId() : deliveryBean.getSiteId();

    // Get service
    PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();

    select.setHasHighestMultipleSubmission(false);  // reset property
    select.setHasAverageMultipleSubmissions(false);
    select.setHasAnyAssessmentBeenModified(false);

    // look for some sort information passed as parameters
    processSortInfo(select);

    // ----------------- prepare Takeable assessment list -------------
    // 1a. get total no. of submission (for grade) per assessment by the given agent in current site
    Map<Long, Integer> h = publishedAssessmentService.getTotalSubmissionPerAssessment(
                AgentFacade.getAgentString(), siteId);
    // store it in personBean 'cos we would be using it to check if the total submisison
    // allowed is met later - extra protection to avoid students being too enterprising
    // e.g. open multiple windows so they can ride on the last attempt multiple times.
    personBean.setTotalSubmissionPerAssessmentHash(h);

    // 1b. get all the published assessmnet available in the site
    // note that agentId is not really used
    List<PublishedAssessmentFacade> publishedAssessmentList =
        publishedAssessmentService.getBasicInfoOfAllPublishedAssessments(
        AgentFacade.getAgentString(), this.getTakeableOrderBy(select),
        select.isTakeableAscending(), siteId);

    GradingService gradingService = new GradingService();
    List list = gradingService.getUpdatedAssessmentList(AgentFacade.getAgentString(), siteId);
    List updatedAssessmentNeedResubmitList = new ArrayList();
    List updatedAssessmentList = new ArrayList();
    if (list != null && list.size() == 2) {
    	updatedAssessmentNeedResubmitList = (List) list.get(0);
    	updatedAssessmentList = (List) list.get(1);
    }

    // filter out the one that the given user do not have right to access
    List<PublishedAssessmentFacade> takeableList = getTakeableList(publishedAssessmentList, h, updatedAssessmentNeedResubmitList, updatedAssessmentList);

    // 1c. prepare delivery bean
    List<DeliveryBeanie> takeablePublishedList = new ArrayList<>();
    for (PublishedAssessmentFacade f : takeableList) {
      // note that this object is PublishedAssessmentFacade(assessmentBaseId,
      // title, releaseTo, startDate, dueDate, retractDate,lateHandling,
      // unlimitedSubmissions, submissionsAllowed). It
      // carries the min. info to create an index list. - daisyf
      DeliveryBeanie delivery = new DeliveryBeanie();
      delivery.setAssessmentId(f.getPublishedAssessmentId().toString());
      delivery.setAssessmentTitle(f.getTitle());
      delivery.setDueDate(f.getDueDate());
      delivery.setTimeRunning(false);// set to true in BeginDeliveryActionListener

      setTimedAssessment(delivery, f);

      // check pastDue
      delivery.setPastDue(f.getDueDate() != null && new Date().after(f.getDueDate()));

      delivery.setAssessmentUpdatedNeedResubmit(
        updatedAssessmentNeedResubmitList.contains(f.getPublishedAssessmentId())
      );

      delivery.setAssessmentUpdated(
        updatedAssessmentList.contains(f.getPublishedAssessmentId())
      );

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
    List<AssessmentGradingData> recentSubmittedList =
    	publishedAssessmentService.getBasicInfoOfLastOrHighestOrAverageSubmittedAssessmentsByScoringOption( AgentFacade.getAgentString(), siteId, !"1".equals(select.getDisplayAllAssessments()));

    Map<Long, PublishedAssessmentFacade> publishedAssessmentHash = getPublishedAssessmentHash(publishedAssessmentList);
    List<DeliveryBeanie> submittedAssessmentGradingList = new ArrayList();

    boolean hasHighest;
    boolean hasMultipleSubmission;
    Map feedbackHash = publishedAssessmentService.getFeedbackHash(siteId);
    select.setHasAnyAssessmentRetractForEdit(false);

    Set<Long> recentSubmittedIds = recentSubmittedList.stream()
      .map(AssessmentGradingData::getPublishedAssessmentId)
      .collect(Collectors.toSet());

    List<Long> containRandomPartAssessmentIds = publishedAssessmentService.getContainRandomPartAssessmentIds(recentSubmittedIds);

    for (AssessmentGradingData g : recentSubmittedList) {
    	hasHighest = false;

      DeliveryBeanie delivery = new DeliveryBeanie();
      delivery.setAssessmentGradingId(g.getAssessmentGradingId());
      delivery.setAssessmentId(g.getPublishedAssessmentId().toString());

      Integer submissionAllowed = getSubmissionAllowed(g.getPublishedAssessmentId(), publishedAssessmentHash);
      if (submissionAllowed == -1) {
        log.debug("submissionAllowed == -1");
        continue;
      }

      hasMultipleSubmission = (submissionAllowed == 0 || submissionAllowed > 1);
      delivery.setMultipleSubmissions(hasMultipleSubmission);

      String scoringOption = getScoringType(g.getPublishedAssessmentId(), publishedAssessmentHash);
      delivery.setScoringOption(scoringOption);

      hasHighest = EvaluationModelIfc.HIGHEST_SCORE.toString().equals(scoringOption);

      if (hasHighest && hasMultipleSubmission) {
        select.setHasHighestMultipleSubmission(true);
      }

      if (hasMultipleSubmission && ((EvaluationModelIfc.AVERAGE_SCORE.toString()).equals(delivery.getScoringOption()))) {
        select.setHasAverageMultipleSubmissions(true);
      }

      delivery.setAssessmentTitle(g.getPublishedAssessmentTitle());
      delivery.setFeedbackDelivery(getFeedbackDelivery(g.getPublishedAssessmentId(), publishedAssessmentHash));
      delivery.setFeedbackComponentOption(getFeedbackComponentOption(g.getPublishedAssessmentId(), publishedAssessmentHash));
      delivery.setFeedbackDate(getFeedbackDate(g.getPublishedAssessmentId(), publishedAssessmentHash));
      delivery.setFeedbackEndDate(getFeedbackEndDate(g.getPublishedAssessmentId(), publishedAssessmentHash));
      delivery.setFeedbackScoreThreshold(getFeedbackScoreThreshold(g.getPublishedAssessmentId(), publishedAssessmentHash));

      if (g.getFinalScore() != null) {
        String scoreStr = g.getFinalScore().toString();

        delivery.setFinalScore(scoreStr);
        delivery.setGrade(scoreStr);
        delivery.setRawScore(scoreStr); // Bug 318 fix. It seems raw score should also be based on final score.
        delivery.setRaw(g.getFinalScore().longValue());
      }

      delivery.setTimeElapse(getTimeElapsed(g.getTimeElapsed()));
      delivery.setSubmissionDate(g.getSubmittedDate());
      delivery.setHasAssessmentBeenModified(getHasAssessmentBeenModified(select, g, publishedAssessmentHash));
      delivery.setSubmitted(true); // records are all submitted for grade

      PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.get(g.getPublishedAssessmentId());
      // check is feedback is available
      String hasFeedback = hasFeedback(p, g.getFinalScore());
      delivery.setFeedback(hasFeedback);

      boolean isAssessmentRetractForEdit = isAssessmentRetractForEdit(p);
      delivery.setIsAssessmentRetractForEdit(isAssessmentRetractForEdit);
      if (isAssessmentRetractForEdit) {
        select.setHasAnyAssessmentRetractForEdit(true);
      }

      delivery.setHasRandomDrawPart(containRandomPartAssessmentIds.contains(g.getPublishedAssessmentId()));

      // check if score is available
      //HashMap feedbackHash = publishedAssessmentService.getFeedbackHash();
      delivery.setShowScore(showScore(g, hasFeedback, feedbackHash));
      delivery.setStatistics(hasStats(g, feedbackHash));

      delivery.setIsRecordedAssessment(g.getIsRecorded());

      // to do: set statistics and time for delivery here.
      submittedAssessmentGradingList.add(delivery);
    }

    BeanSort bs = new BeanSort(submittedAssessmentGradingList, "assessmentTitle");
    bs.toStringSort();
    bs.sort();

    List<DeliveryBeanie> averageScoreAssessmentGradingList = submittedAssessmentGradingList.stream()
        .filter(db -> EvaluationModelIfc.AVERAGE_SCORE.toString().equals(db.getScoringOption()))
        .collect(Collectors.toList());

    String lastPublishedAssessmentId = null;
    Map<String, Double> averageScoreMap = new HashMap<>();
    double totalScores = 0d;
    int totalSubmissions = 0;

    for (DeliveryBeanie db : averageScoreAssessmentGradingList) {
      String currentId = db.getAssessmentId();
      double score = Double.parseDouble(db.getFinalScore());

      if (currentId.equals(lastPublishedAssessmentId)) {
        totalScores += score;
        totalSubmissions++;
      } else {
        if (lastPublishedAssessmentId != null) {
            double averageScore = totalScores / totalSubmissions;
            averageScoreMap.put(lastPublishedAssessmentId, averageScore);
        }

        lastPublishedAssessmentId = currentId;
        totalScores = score;
        totalSubmissions = 1;
      }
    }

    if (lastPublishedAssessmentId != null) {
      double averageScore = totalScores / totalSubmissions;
      averageScoreMap.put(lastPublishedAssessmentId, averageScore);
    }

    /// --mustansar
    List<DeliveryBeanie> reviewableList = new ArrayList();
    List<DeliveryBeanie> recordedList=new ArrayList<>();
    String assessmentIdNew="";

    for (DeliveryBeanie beanie : submittedAssessmentGradingList) {
    	String assessmentIdOld= beanie.getAssessmentId();
    	String scoring = beanie.getScoringOption();

    	boolean processRecordedAvg = AVG_SCORE.equals(scoring) && !assessmentIdNew.equals(assessmentIdOld);
    	boolean processRecordedHighestOrLast = (HIGH_SCORE.equals(scoring) || LAST_SCORE.equals(scoring)) && beanie.getIsRecordedAssessment();

      if (processRecordedAvg || processRecordedHighestOrLast) {
        beanie.setIsRecordedAssessment(false);

        DeliveryBeanie recorded = new DeliveryBeanie();
        recorded.setIsRecordedAssessment(true);
        recorded.setScoringOption(beanie.getScoringOption());

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
        recorded.setFeedbackEndDate(beanie.getFeedbackEndDate());
        recorded.setFeedbackScoreThreshold(beanie.getFeedbackScoreThreshold());
        recorded.setFeedbackDelivery(beanie.getFeedbackDelivery());
        recorded.setFeedbackComponentOption(beanie.getFeedbackComponentOption());

        // check if assessment allows multiple submissions or if this user has been allowed to submit multiple times
        Long assessId = NumberUtils.toLong(beanie.getAssessmentId(), -1L);
        int numSubmissions = (Integer) ObjectUtils.defaultIfNull(h.get(assessId), 0);
        recorded.setMultipleSubmissions(beanie.isMultipleSubmissions() || numSubmissions > 1);

        if (processRecordedAvg) {
          assessmentIdNew = beanie.getAssessmentId();
          String avgScoreStr = averageScoreMap.get(assessmentIdNew).toString();

          recorded.setFinalScore(avgScoreStr);
          recorded.setGrade(avgScoreStr);
          recorded.setRawScore(avgScoreStr);
        } else {  // highest or last
          recorded.setFinalScore(beanie.getFinalScore());
          recorded.setGrade(beanie.getGrade());
          recorded.setRawScore(beanie.getRawScore());
        }

        recordedList.add(recorded);
        reviewableList.add(recorded);
        reviewableList.add(beanie);
      } else if (StringUtils.equalsAny(select.getDisplayAllAssessments(), "2", "3")) {
        reviewableList.add(beanie);
      }
    }

    // display warning legend if any quizzes have been marked as modified in the review section
    select.setHasAnyAssessmentBeenModified(recordedList.stream().anyMatch(db -> db.getHasAssessmentBeenModified()));

    switch (select.getDisplayAllAssessments()) {
      case "1":
        submittedAssessmentGradingList = recordedList;
        break;
      case "2":
        submittedAssessmentGradingList = reviewableList;
        break;
      case "3":
        String reviewAssessmentId = select.getReviewAssessmentId();

        if (reviewAssessmentId != null) {
          submittedAssessmentGradingList = reviewableList.stream()
            .filter(reviewable -> reviewable.getAssessmentId().equals(reviewAssessmentId))
          .collect(Collectors.toList());
        } else {
          submittedAssessmentGradingList = new ArrayList();
        }
        break;
    }

    if (!select.isReviewableAscending()) {
      Collections.reverse(submittedAssessmentGradingList);
    }

    // If secure delivery modules are installed, then insert their html fragments
    select.setSecureDeliveryHTMLFragments( "" );
    SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();

    if (secureDelivery.isSecureDeliveryAvaliable()) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        select.setSecureDeliveryHTMLFragments( secureDelivery.getInitialHTMLFragments(request, new ResourceLoader().getLocale() ) );

        for (DeliveryBeanie db : takeablePublishedList) {
            // We have to refetch the published assessment because the hash above doesn't have the metadata
            PublishedAssessmentFacade paf = publishedAssessmentService.getPublishedAssessmentQuick(db.getAssessmentId());
            final String moduleId = paf.getAssessmentMetaDataByLabel(SecureDeliveryServiceAPI.MODULE_KEY);

            db.setAlternativeDeliveryUrl(secureDelivery.getAlternativeDeliveryUrl(
                    moduleId,
                    Long.valueOf(db.getAssessmentId()),
                    AgentFacade.getAgentString()
                ).orElse("")
            );
        }
    }

    // set the managed beanlist properties that we need
    select.setTakeableAssessments(takeablePublishedList);
    select.setReviewableAssessments(submittedAssessmentGradingList);

    if ("3".equals(select.getDisplayAllAssessments()) && !submittedAssessmentGradingList.isEmpty()) {
      select.setReviewAssessmentTitle(submittedAssessmentGradingList.get(0).getAssessmentTitle());
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

    String displaySubmissions = ContextUtil.lookupParam("selectSubmissions");

    // Set "2" as default in case null value
    displaySubmissions = displaySubmissions == null ? "2" : displaySubmissions;

    // Set "3" if an assessment is reviewed
    displaySubmissions = StringUtils.isEmpty(bean.getReviewAssessmentId()) ? displaySubmissions : "3";

    bean.setDisplayAllAssessments(displaySubmissions);

    log.debug("displaySubmissions: {}", displaySubmissions);
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
        bean.setTakeableAscending((Boolean.valueOf(takeAscending)));
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
        bean.setReviewableAscending(Boolean.valueOf(reviewAscending));
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
  private List<PublishedAssessmentFacade> getTakeableList(List assessmentList, Map <Long,Integer> h, List updatedAssessmentNeedResubmitList, List updatedAssessmentList) {
    List<PublishedAssessmentFacade> takeableList = new ArrayList<>();
    GradingService gradingService = new GradingService();
    Map<Long, StudentGradingSummaryData> numberRetakeHash = gradingService.getNumberRetakeHash(AgentFacade.getAgentString());
    Map<Long, Integer> actualNumberRetake = gradingService.getActualNumberRetakeHash(AgentFacade.getAgentString());
    ExtendedTimeDeliveryService extendedTimeDeliveryService;
    for (int i = 0; i < assessmentList.size(); i++) {
      PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
			// Handle extended time info
			extendedTimeDeliveryService = new ExtendedTimeDeliveryService(f);
			if (extendedTimeDeliveryService.hasExtendedTime()) {
				f.setStartDate(extendedTimeDeliveryService.getStartDate());
				f.setDueDate(extendedTimeDeliveryService.getDueDate());
				//Override late handling here, availability check done later
				if (extendedTimeDeliveryService.getRetractDate() != null) {
					f.setRetractDate(extendedTimeDeliveryService.getRetractDate());
					f.setLateHandling(AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
				}
				f.setTimeLimit(extendedTimeDeliveryService.getTimeLimit());
			}
      if (f.getReleaseTo()!=null && !("").equals(f.getReleaseTo())
          && !f.getReleaseTo().contains("Anonymous Users") ) {
        if (isAvailable(f, h, numberRetakeHash, actualNumberRetake, updatedAssessmentNeedResubmitList, updatedAssessmentList)) {
          takeableList.add(f);
        }
      }
    }
    return takeableList;
  }

  public boolean isAvailable(PublishedAssessmentFacade f, Map <Long, Integer> h, Map<Long, StudentGradingSummaryData> numberRetakeHash, Map <Long, Integer> actualNumberRetakeHash, List updatedAssessmentNeedResubmitList, List updatedAssessmentList) {
    boolean returnValue = false;
    //1. prepare our significant parameters
    Integer status = f.getStatus();
    Date currentDate = new Date();
    Date startDate = f.getStartDate();
    Date dueDate = f.getDueDate();
    Date retractDate = f.getRetractDate();
    boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(f.getLateHandling());

    if (dueDate == null && (retractDate != null && acceptLateSubmission)) {
        dueDate = retractDate;
    }

    if (!Integer.valueOf(1).equals(status)) {
    	return false;
    }
    
    if (startDate != null && startDate.after(currentDate)) {
    	return false;
    }

    int totalSubmitted = 0;

    //boolean notSubmitted = false;
    if (h.get(f.getPublishedAssessmentId()) != null){
      totalSubmitted = ((Integer) h.get(f.getPublishedAssessmentId()));
    }
    
    if (acceptLateSubmission && (dueDate != null && dueDate.before(currentDate)) && retractDate == null && totalSubmitted == 0) {
      return true;
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
      maxSubmissionsAllowed = f.getSubmissionsAllowed();
    }

    int numberRetake = 0;
    if (numberRetakeHash.get(f.getPublishedAssessmentId()) != null) {
    	numberRetake = (((StudentGradingSummaryData) numberRetakeHash.get(f.getPublishedAssessmentId())).getNumberRetake());
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
					actualNumberRetake = (actualNumberRetakeHash.get(f.getPublishedAssessmentId()));
				}
				if (actualNumberRetake < numberRetake) {
					returnValue = true;
				} else {
					returnValue = false;
				}
			}
    	} else {
	    	returnValue = false;
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
  private String hasFeedback(PublishedAssessmentFacade p, Double finalScore){
    String hasFeedback = "na";
    Date currentDate = new Date();
    
    if (p==null) {// published assessment may have been deleted
      return hasFeedback;
    }

    switch(p.getFeedbackDelivery().intValue()){
        case 1: //AssessmentFeedbackIfc.IMMEDIATE_FEEDBACK
        case 4: //AssessmentFeedbackIfc.FEEDBACK_ON_SUBMISSION
            hasFeedback = "show";
            break;
        case 2: //AssessmentFeedbackIfc.FEEDBACK_BY_DATE
            if(p.getFeedbackDate()!= null && p.getFeedbackEndDate() == null){
                hasFeedback = currentDate.after(p.getFeedbackDate()) ? "show" : "blank";
            } else if(p.getFeedbackDate()!= null && p.getFeedbackEndDate() != null){
                hasFeedback = currentDate.after(p.getFeedbackDate()) && currentDate.before(p.getFeedbackEndDate()) ? "show" : "blank";
            }
            if("show".equals(hasFeedback) && p.getFeedbackScoreThreshold() != null){
                try{
                    //We need the total score of the assessment
                    PublishedAssessmentData assessmentData = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().loadPublishedAssessment(p.getPublishedAssessmentId());
                    double maxScore = assessmentData.getTotalScore() != null ? assessmentData.getTotalScore().doubleValue() : 0.0;
                    Double earnedScorePercentage = maxScore != 0.0 ? new Double(finalScore.doubleValue() * 100.0 / maxScore) : new Double(0.0);
                    Double scoreThresholdDouble = p.getFeedbackScoreThreshold();
                    //Display when the earned score percentage is lower than the score threshold
                    hasFeedback = earnedScorePercentage.compareTo(scoreThresholdDouble) < 0 ? "show" : "blank";
                } catch(Exception ex){
                    log.error("Error comparing the feedback score threshold {}. ", ex);
                }
            }
            break;
        default: 
            hasFeedback="na";
            break;
    }

    return hasFeedback;
  }

  private boolean isAssessmentRetractForEdit(PublishedAssessmentFacade p){
	  if (p == null) {// published assessment may have been deleted
	      return false;
	    }
	  
	  return AssessmentIfc.RETRACT_FOR_EDIT_STATUS.equals(p.getStatus());
  }
  
  private String hasStats(AssessmentGradingData a, Map feedbackHash){
    String hasStats = "false";

    AssessmentFeedbackIfc f= (AssessmentFeedbackIfc)feedbackHash.get(a.getPublishedAssessmentId());

    if (f!=null){
	if ( (Boolean.TRUE).equals(f.getShowStatistics()))
       {
	hasStats = "true";
       }
    }
    return hasStats;
  }

  private String showScore(AssessmentGradingData a,
                           String hasFeedback, Map feedbackHash){
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

  private String getScoringType(Long publishedAssessmentId, Map publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(publishedAssessmentId);
	    if (p!=null) {
	    	if (p.getScoringType() != null) {
	    		return p.getScoringType().toString();
	    	}
	    }
        return null;
  }
  

  private Integer getSubmissionAllowed(Long publishedAssessmentId, Map publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(publishedAssessmentId);
	    if (p!=null)
	    	return p.getSubmissionsAllowed();
	    else {
	    	log.debug("The published assessment is not valid");
	    	return Integer.valueOf(-1);
	    }
  }
  
  private Date getFeedbackDate(Long publishedAssessmentId, Map publishedAssessmentHash){
    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
        get(publishedAssessmentId);
    if (p!=null)
      return p.getFeedbackDate();
    else
      return null;
  }

  private Date getFeedbackEndDate(Long publishedAssessmentId, Map publishedAssessmentHash) {
      PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.get(publishedAssessmentId);
      return p != null ? p.getFeedbackEndDate() : null;
  }

  private Double getFeedbackScoreThreshold(Long publishedAssessmentId, Map publishedAssessmentHash){
      PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash. get(publishedAssessmentId);
      return p != null ? p.getFeedbackScoreThreshold() : null;
  }

  private String getFeedbackDelivery(Long publishedAssessmentId, Map publishedAssessmentHash){
    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
        get(publishedAssessmentId);
    if (p!=null)
      return p.getFeedbackDelivery().toString();
    else
      return null;
  }
  
  private String getFeedbackComponentOption(Long publishedAssessmentId, Map publishedAssessmentHash){
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
  
  private boolean getHasAssessmentBeenModified(SelectAssessmentBean select, AssessmentGradingData g, Map publishedAssessmentHash){
	    PublishedAssessmentFacade p = (PublishedAssessmentFacade)publishedAssessmentHash.
	        get(g.getPublishedAssessmentId());
	    if (p != null) {
	    	if (!Integer.valueOf(AssessmentIfc.RETRACT_FOR_EDIT_STATUS).equals(p.getStatus()) && g.getSubmittedDate() != null && p.getLastModifiedDate().after(g.getSubmittedDate())) {
	    		log.debug("AssessmentGradingId = " + g.getAssessmentGradingId());
	    		log.debug("LastModifiedDate = " + p.getLastModifiedDate());
	    		log.debug("SubmittedDate = " + g.getSubmittedDate());
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

  public Map<Long, PublishedAssessmentFacade> getPublishedAssessmentHash(List<PublishedAssessmentFacade> publishedAssessmentList){
    Map<Long, PublishedAssessmentFacade> h = new HashMap<>();
    for (int i=0; i<publishedAssessmentList.size();i++){
      PublishedAssessmentFacade p = publishedAssessmentList.get(i);
      h.put(p.getPublishedAssessmentId(), p);
    }
    return h;
  }
  
  private void setTimedAssessment(DeliveryBeanie delivery, PublishedAssessmentFacade pubAssessment){
	  if (pubAssessment.getTimeLimit() != null) {
		  int seconds = pubAssessment.getTimeLimit();
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
