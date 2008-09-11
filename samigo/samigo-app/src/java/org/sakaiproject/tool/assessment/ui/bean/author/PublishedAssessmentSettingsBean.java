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



package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecuredIPAddressIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.cover.ToolManager;


public class PublishedAssessmentSettingsBean
  implements Serializable {
  private static Log log = LogFactory.getLog(PublishedAssessmentSettingsBean.class);
  
  private static final IntegrationContextFactory integrationContextFactory =
    IntegrationContextFactory.getInstance();
  private static final PublishingTargetHelper ptHelper =
    integrationContextFactory.getPublishingTargetHelper();
  private static final GradebookServiceHelper gbsHelper =
      integrationContextFactory.getGradebookServiceHelper();
  private static final boolean integrated =
      integrationContextFactory.isIntegrated();
  /**
   *  we use the calendar widget which uses 'MM/dd/yyyy hh:mm:ss a'
   *  used to take the internal format from calendar picker and move it
   *  transparently in and out of the date properties
   *
   */
  // private static final String DISPLAY_DATEFORMAT = "MM/dd/yyyy hh:mm:ss a";
  //private String display_dateFormat= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec");
  //private SimpleDateFormat displayFormat = new SimpleDateFormat(display_dateFormat);

  private String displayDateFormat;
  private SimpleDateFormat displayFormat;


  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -630950053380808339L;
  private PublishedAssessmentFacade assessment;
  private Long assessmentId;
  private String title;
  private String creator;
  private String description;
  private boolean hasQuestions;

  // meta data
  private String objectives;
  private String keywords;
  private String rubrics;
  private String authors;

  // these are properties in PublishedAccessControl
  private Date startDate;
  private Date dueDate;
  private Date retractDate;
  private Date feedbackDate;
  private Integer timeLimit; // in seconds, calculated from timedHours & timedMinutes
  private Integer timedHours;
  private Integer timedMinutes;
  private Integer timedSeconds;
  private boolean timedAssessment = false;
  private boolean autoSubmit = false;
  private String assessmentFormat; // question (1)/part(2)/assessment(3) on separate page
  private String itemNavigation; // linear (1)or random (2)
  private String itemNumbering; // continuous between parts(1), restart between parts(2)
  private String unlimitedSubmissions;
  private String submissionsAllowed;
  private String submissionsSaved; // bad name, this is autoSaved
  private String lateHandling;
  private String submissionMessage;
  private SelectItem[] publishingTargets;
  private String[] targetSelected;
  private String firstTargetSelected;
  private String releaseTo;
  private String username;
  private String password;
  private String finalPageUrl;
  private String ipAddresses;

  // properties of PublishedFeedback
  private String feedbackDelivery; // immediate, on specific date , no feedback
  private String feedbackAuthoring;
  private String editComponents; // 0 = cannot
  private boolean showQuestionText = false;
  private boolean showStudentResponse = false;
  private boolean showCorrectResponse = false;
  private boolean showStudentScore = false;
  private boolean showStudentQuestionScore = false;
  private boolean showQuestionLevelFeedback = false;
  private boolean showSelectionLevelFeedback = false; // must be MC
  private boolean showGraderComments = false;
  private boolean showStatistics = false;
  
  // properties of PublishedEvaluationModel
  private String anonymousGrading;
  private boolean gradebookExists;
  private String toDefaultGradebook;
  private String scoringType;
  private String bgColor;
  private String bgImage;
  private HashMap values = new HashMap();

  // extra properties
  private String publishedUrl;
  private String alias;

  private String outcome;
  
  private boolean isValidDate = true;;
  private boolean isValidStartDate = true;
  private boolean isValidDueDate = true;
  private boolean isValidRetractDate = true;
  private boolean isValidFeedbackDate = true;
  
  private String originalStartDateString;
  private String originalDueDateString;
  private String originalRetractDateString;
  private String originalFeedbackDateString;
  private boolean updateMostCurrentSubmission = false;
  
  private boolean isMarkForReview;
  
  /*
   * Creates a new AssessmentBean object.
   */
  public PublishedAssessmentSettingsBean() {
  }

  public PublishedAssessmentFacade getAssessment() {
    return assessment;
  }

  public void setAssessment(PublishedAssessmentFacade assessment) {
    try {
      this.assessment = assessment;
      // set the valueMap
      setValueMap(assessment.getAssessmentMetaDataMap());
      this.assessmentId = assessment.getPublishedAssessmentId();
      this.title = assessment.getTitle();
      this.creator = AgentFacade.getDisplayName(assessment.getCreatedBy());
      this.description = assessment.getDescription();
      // assessment meta data
      this.authors = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
          AUTHORS);
      this.objectives = assessment.getAssessmentMetaDataByLabel(
          AssessmentMetaDataIfc.OBJECTIVES);
      this.keywords = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
          KEYWORDS);
      this.rubrics = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
          RUBRICS);
      this.bgColor = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
          BGCOLOR);
      this.bgImage = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
          BGIMAGE);

      setDisplayFormat(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec"));
      resetIsValidDate();
      resetOriginalDateString();
      
      // these are properties in AssessmentAccessControl
      AssessmentAccessControlIfc accessControl = null;
      accessControl = assessment.getAssessmentAccessControl();
      if (accessControl != null) {
        this.startDate = accessControl.getStartDate();
        this.dueDate = accessControl.getDueDate();
        this.retractDate = accessControl.getRetractDate();
        this.feedbackDate = accessControl.getFeedbackDate();

        // deal with releaseTo
        this.releaseTo = accessControl.getReleaseTo(); // list of String
        this.publishingTargets = getPublishingTargets();
        this.targetSelected = getTargetSelected(releaseTo);
        this.firstTargetSelected = getFirstTargetSelected(releaseTo);

        this.timeLimit = accessControl.getTimeLimit(); // in seconds
        if (timeLimit !=null && timeLimit.intValue()>0)
          setTimeLimitDisplay(timeLimit.intValue());
        if ((new Integer(1)).equals(assessment.getAssessmentAccessControl().getTimedAssessment()))
          this.timedAssessment = true;
        if ((new Integer(1)).equals(assessment.getAssessmentAccessControl().getAutoSubmit()))
          this.autoSubmit = true;
        if (accessControl.getAssessmentFormat()!=null)
          this.assessmentFormat = accessControl.getAssessmentFormat().toString(); // question/part/assessment on separate page
        if (accessControl.getItemNavigation()!=null)
          this.itemNavigation = accessControl.getItemNavigation().toString(); // linear or random
        if (accessControl.getItemNumbering()!=null)
          this.itemNumbering = accessControl.getItemNumbering().toString();
        if (accessControl.getSubmissionsSaved()!=null)
          this.submissionsSaved = accessControl.getSubmissionsSaved().toString();

        if (accessControl.getMarkForReview() != null && (Integer.valueOf(1)).equals(accessControl.getMarkForReview())) {
            this.isMarkForReview = true;
        }
        else {
        	this.isMarkForReview = false;
        }
        
        // default to unlimited if control value is null
        if (accessControl.getUnlimitedSubmissions()!=null && !accessControl.getUnlimitedSubmissions().booleanValue()){
          this.unlimitedSubmissions=AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString();
          this.submissionsAllowed = accessControl.getSubmissionsAllowed().toString();
        }
        else{
          this.unlimitedSubmissions=AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString();
          this.submissionsAllowed="";
        }

        if (accessControl.getLateHandling() !=null)
          this.lateHandling = accessControl.getLateHandling().toString();
        if (accessControl.getSubmissionsSaved()!=null)
          this.submissionsSaved = accessControl.getSubmissionsSaved().toString();
        this.submissionMessage = accessControl.getSubmissionMessage();
        this.username = accessControl.getUsername();
        this.password = accessControl.getPassword();
        this.finalPageUrl = accessControl.getFinalPageUrl();
      }

      // properties of AssesmentFeedback
      AssessmentFeedbackIfc feedback = assessment.getAssessmentFeedback();
      if (feedback != null) {
        if (feedback.getFeedbackDelivery()!=null)
          this.feedbackDelivery = feedback.getFeedbackDelivery().toString();
      if (feedback.getFeedbackAuthoring()!=null)
          this.feedbackAuthoring = feedback.getFeedbackAuthoring().toString();
      
      if ((Boolean.TRUE).equals(feedback.getShowQuestionText()))
          this.showQuestionText = true;
        else
            this.showQuestionText = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowStudentResponse()))
          this.showStudentResponse = true;
        else
            this.showStudentResponse = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowCorrectResponse()))
          this.showCorrectResponse = true;
        else
            this.showCorrectResponse = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowStudentScore()))
          this.showStudentScore = true;
        else
            this.showStudentScore = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowStudentQuestionScore()))
          this.showStudentQuestionScore = true;
        else
            this.showStudentQuestionScore = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowQuestionLevelFeedback()))
          this.showQuestionLevelFeedback = true;
        else
            this.showQuestionLevelFeedback = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowSelectionLevelFeedback()))
          this.showSelectionLevelFeedback = true;// must be MC
        else
            this.showSelectionLevelFeedback = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowGraderComments()))
          this.showGraderComments = true;
        else
            this.showGraderComments = false;
        
        if ((Boolean.TRUE).equals(feedback.getShowStatistics()))
          this.showStatistics = true;
        else
            this.showStatistics = false;
      }

      // properties of EvaluationModel
      EvaluationModelIfc evaluation = assessment.getEvaluationModel();
      if (evaluation != null) {
        if (evaluation.getAnonymousGrading()!=null)
          this.anonymousGrading = evaluation.getAnonymousGrading().toString();
        if (evaluation.getToGradeBook()!=null )
          this.toDefaultGradebook = evaluation.getToGradeBook();
        if (evaluation.getScoringType()!=null)
          this.scoringType = evaluation.getScoringType().toString();
        
        String currentSiteId = AgentFacade.getCurrentSiteId();
        this.gradebookExists = gbsHelper.isGradebookExist(currentSiteId);
        /*
        GradebookService g = null;
        if (integrated)
        {
          g = (GradebookService) SpringBeanLocator.getInstance().
            getBean("org.sakaiproject.service.gradebook.GradebookService");
        }

        this.gradebookExists = gbsHelper.gradebookExists(
          GradebookFacade.getGradebookUId(), g);
        */
      }

      //set IPAddresses
      setIpAddresses(assessment);

      // publishedUrl
      FacesContext context = FacesContext.getCurrentInstance();
      ExternalContext extContext = context.getExternalContext();
      // get the alias to the pub assessment
      this.alias = assessment.getAssessmentMetaDataByLabel(
          AssessmentMetaDataIfc.ALIAS);
      String server = ( (javax.servlet.http.HttpServletRequest) extContext.
                       getRequest()).getRequestURL().toString();
      int index = server.indexOf(extContext.getRequestContextPath() + "/"); // "/samigo/"
      server = server.substring(0, index);
      String url = server + extContext.getRequestContextPath();
      this.publishedUrl = url + "/servlet/Login?id=" + this.alias;
    }
    catch (RuntimeException ex) {
      log.warn(ex.getMessage());
    }
  }

  // properties from Assessment
  public Long getAssessmentId() {
    return this.assessmentId;
  }

  public void setAssessmentId(Long assessmentId) {
    this.assessmentId = assessmentId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCreator() {
    return this.creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  // properties form AssessmentMetaData
  public String getObjectives() {
    return this.objectives;
  }

  public void setObjectives(String objectives) {
    this.objectives = objectives;
  }

  public String getKeywords() {
    return this.keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getRubrics() {
    return this.rubrics;
  }

  public void setRubrics(String rubrics) {
    this.rubrics = rubrics;
  }

  public String getAuthors() {
    return this.authors;
  }

  public void setAuthors(String authors) {
    this.authors = authors;
  }

  public String getBgColor() {
    return this.bgColor;
  }

  public void setBgColor(String bgColor) {
    this.bgColor = bgColor;
  }

  public String getBgImage() {
    return this.bgImage;
  }

  public void setBgImage(String bgImage) {
    this.bgImage = bgImage;
  }

  public boolean getHasQuestions() {
    return this.hasQuestions;
  }

  public void setHasQuestions(boolean hasQuestions) {
    this.hasQuestions = hasQuestions;
  }

  // copied from AssessmentAccessControl ;-)
  public Date getStartDate() {
    return this.startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getDueDate() {
    return this.dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getFeedbackDate() {
    return this.feedbackDate;
  }

  public void setFeedbackDate(Date feedbackDate) {
    this.feedbackDate = feedbackDate;
  }

  public Date getRetractDate() {
    return this.retractDate;
  }

  public void setRetractDate(Date retractDate) {
    this.retractDate = retractDate;
  }

  public String getReleaseTo() {
    String anonymousString="";
    String authenticatedString="";
    if (values.get("hasAnonymousRelease")!=null)
      anonymousString = values.get("hasAnonymousRelease").toString();
    if ( values.get("hasAuthenticatedRelease")!=null)
      authenticatedString = values.get("hasAuthenticatedRelease").toString();
    if (("true").equals(anonymousString) && ("true").equals(authenticatedString))
      this.releaseTo="anonymous; authenticated users";
    else if (("true").equals(anonymousString))
      this.releaseTo="anonymous";
    else if (("true").equals(authenticatedString))
      this.releaseTo="authenticated users";
    return this.releaseTo;
  }

  public void setReleaseTo(String releaseTo) {
    this.releaseTo = releaseTo;
  }

  public Integer getTimeLimit() {
    return new Integer(timedHours.intValue()*3600
        + timedMinutes.intValue()*60
        + timedSeconds.intValue());
  }

  public void setTimeLimit(Integer timeLimit) {
    this.timeLimit = timeLimit;
  }

  public void setTimedHours(Integer timedHours) {
    this.timedHours = timedHours;
  }

  public Integer getTimedHours() {
    return timedHours;
  }

  public void setTimedMinutes(Integer timedMinutes) {
    this.timedMinutes =  timedMinutes;
  }

  public Integer getTimedMinutes() {
    return timedMinutes;
  }

  public void setTimedSeconds(Integer timedSeconds) {
    this.timedSeconds =  timedSeconds;
  }

  public Integer getTimedSeconds() {
    return timedSeconds;
  }

  public boolean getTimedAssessment() {
    return timedAssessment;
  }

  public void setTimedAssessment(boolean timedAssessment) {
    this.timedAssessment = timedAssessment;
  }

  public boolean getAutoSubmit() {
    return autoSubmit;
  }

  public void setAutoSubmit(boolean autoSubmit) {
    this.autoSubmit = autoSubmit;
  }

  public String getAssessmentFormat() {
    return assessmentFormat;
  }

  public void setAssessmentFormat(String assessmentFormat) {
    this.assessmentFormat = assessmentFormat;
  }

  public String getItemNavigation() {
    return itemNavigation;
  }

  public void setItemNavigation(String itemNavigation) {
    this.itemNavigation = itemNavigation;
  }

  public String getItemNumbering() {
    return itemNumbering;
  }

  public void setItemNumbering(String itemNumbering) {
    this.itemNumbering = itemNumbering;
  }

  public String getUnlimitedSubmissions() {
    return unlimitedSubmissions;
  }

  public void setUnlimitedSubmissions(String unlimitedSubmissions) {
    this.unlimitedSubmissions = unlimitedSubmissions;
  }

  public String getSubmissionsAllowed() {
    return submissionsAllowed;
  }

  public void setSubmissionsAllowed(String submissionsAllowed) {
      this.submissionsAllowed = submissionsAllowed;
  }

  public void setLateHandling(String lateHandling) {
    this.lateHandling = lateHandling;
  }

  public String getLateHandling() {
    return lateHandling;
  }

  // bad name - this is autoSaved
  public void setSubmissionsSaved(String submissionSaved) {
    this.submissionsSaved = submissionSaved;
  }

  public String getSubmissionsSaved() {
    return submissionsSaved;
  }

  public void setSubmissionMessage(String submissionMessage) {
    this.submissionMessage = submissionMessage;
  }

  public String getSubmissionMessage() {
    return submissionMessage;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setFinalPageUrl(String finalPageUrl) {
    this.finalPageUrl = finalPageUrl;
  }

  public String getFinalPageUrl() {
    return finalPageUrl;
  }

  public String getFeedbackDelivery() {
    return feedbackDelivery;
  }

  public void setFeedbackDelivery(String feedbackDelivery) {
    this.feedbackDelivery = feedbackDelivery;
  }

  public String getFeedbackAuthoring() {
    return feedbackAuthoring;
  }

  public void setFeedbackAuthoring(String feedbackAuthoring) {
    this.feedbackAuthoring = feedbackAuthoring;
  }

  public boolean getShowQuestionText() {
    return showQuestionText;
  }

  public void setShowQuestionText(boolean showQuestionText) {
    this.showQuestionText = showQuestionText;
  }

  public boolean getShowStudentResponse() {
    return showStudentResponse;
  }

  public void setShowStudentResponse(boolean showStudentResponse) {
    this.showStudentResponse = showStudentResponse;
  }

  public boolean getShowCorrectResponse() {
    return showCorrectResponse;
  }

  public void setShowCorrectResponse(boolean showCorrectResponse) {
    this.showCorrectResponse = showCorrectResponse;
  }

  public boolean getShowStudentScore() {
    return showStudentScore;
  }

  public void setShowStudentScore(boolean showStudentScore) {
    this.showStudentScore = showStudentScore;
  }

  public boolean getShowStudentQuestionScore() {
    return showStudentQuestionScore;
  }

  public void setShowStudentQuestionScore(boolean showStudentQuestionScore) {
    this.showStudentQuestionScore = showStudentQuestionScore;
  }

  public boolean getShowQuestionLevelFeedback() {
    return showQuestionLevelFeedback;
  }

  public void setShowQuestionLevelFeedback(boolean showQuestionLevelFeedback) {
    this.showQuestionLevelFeedback = showQuestionLevelFeedback;
  }

  public boolean getShowSelectionLevelFeedback() {
    return showSelectionLevelFeedback;
  }

  public void setShowSelectionLevelFeedback(boolean showSelectionLevelFeedback) {
    this.showSelectionLevelFeedback = showSelectionLevelFeedback;
  }

  public boolean getShowGraderComments() {
    return showGraderComments;
  }

  public void setShowGraderComments(boolean showGraderComments) {
    this.showGraderComments = showGraderComments;
  }

  public boolean getShowStatistics() {
    return showStatistics;
  }

  public void setShowStatistics(boolean showStatistics) {
    this.showStatistics = showStatistics;
  }

  public String getAnonymousGrading() {
    return this.anonymousGrading;
  }

  public void setAnonymousGrading(String anonymousGrading) {
    this.anonymousGrading = anonymousGrading;
  }

  public String getToDefaultGradebook() {
    return this.toDefaultGradebook;
  }

  public void setToDefaultGradebook(String toDefaultGradebook) {
    this.toDefaultGradebook = toDefaultGradebook;
  }

  public boolean getGradebookExists() {
	return this.gradebookExists;
  }

  public void setGradebookExists(boolean gradebookExists) {
	this.gradebookExists = gradebookExists;
  }

  public String getScoringType() {
    return this.scoringType;
  }

  public void setScoringType(String scoringType) {
    this.scoringType = scoringType;
  }


  public void setValue(String key, Object value){
    this.values.put(key, value);
  }

  public Object getValue(String key)
  {
    if (this.values.get(key) == null)
    {
      return Boolean.FALSE;
    }

    return values.get(key);
  }

  public void setValueMap(HashMap newMap){
    this.values = newMap;
  }

  public HashMap getValueMap(){
    return values;
  }

  public String getDateString(Date date) {
    if (date!=null){
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      int mon = c.get(Calendar.MONTH);
      int day = c.get(Calendar.DAY_OF_MONTH);
      int year = c.get(Calendar.YEAR);
      String dateString = mon + "/" + day + "/" + year;
      return dateString;
    }
    else
      return "";
  }

  public void setTimeLimitDisplay(int time){
    this.timedHours=new Integer(time/60/60);
    this.timedMinutes = new Integer((time/60)%60);
    this.timedSeconds = new Integer(time % 60);
  }

  // followings are set of SelectItem[] used in authorSettings.jsp
  public SelectItem[] getHours() {
    return hours;
  }

  public static void setHours(SelectItem[] hours) {
    PublishedAssessmentSettingsBean.hours =  hours;
  }

  public SelectItem[] getMins() {
    return mins;
  }

  public static void setMins(SelectItem[] mins) {
    PublishedAssessmentSettingsBean.mins =  mins;
  }

  private static List months;
  private static List days;
  private static SelectItem[] mins;
  private static SelectItem[] hours;
  static{
    months = new ArrayList();
    for (int i=1; i<=12; i++){
      months.add(new SelectItem(new Integer(i)));
    }
    days = new ArrayList();
    for (int i=1; i<32; i++){
      days.add(new SelectItem(new Integer(i)));
    }
    hours = new SelectItem[24];
    for (int i=0; i<24; i++){
      if (i < 10)
        hours[i] = new SelectItem(new Integer(i),"0"+i);
      else
        hours[i] = new SelectItem(new Integer(i),i+"");
    }
    mins = new SelectItem[60];
    for (int i=0; i<60; i++){
      if (i < 10)
        mins[i] = new SelectItem(new Integer(i),"0"+i);
      else
        mins[i] = new SelectItem(new Integer(i),i+"");
    }
  }

  public String getIpAddresses() {
    return ipAddresses;
  }

  public void setIpAddresses(PublishedAssessmentFacade assessment) {
      // ip addresses
      this.ipAddresses = "";
      Set ipAddressSet = assessment.getSecuredIPAddressSet();
      if (ipAddressSet != null){
        Iterator iter = ipAddressSet.iterator();
        while (iter.hasNext()) {
          SecuredIPAddressIfc ip = (SecuredIPAddressIfc) iter.next();
          this.ipAddresses = ip.getIpAddress()+"\n"+this.ipAddresses;
        }
      }
  }

  // the following methods are used to take the internal format from
  // calendar picker and move it transparently in and out of the date
  // properties

  /**
   * date from internal string of calendar widget
   * @param date Date object
   * @return date String "MM-dd-yyyy hh:mm:ss a"
   */
  private String getDisplayFormatFromDate(Date date) {
    String dateString = "";
    if (date == null) {
      return dateString;
    }

    try {
      //dateString = displayFormat.format(date);
      TimeUtil tu = new TimeUtil();
      dateString = tu.getDisplayDateTime(displayFormat, date);

    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.");
      ex.printStackTrace();
    }
    return dateString;
  }

  /**
   * format according to internal requirements of calendar widget
   * @param dateString "MM-dd-yyyy hh:mm:ss a"
   * @return Date object
   */
  private Date getDateFromDisplayFormat(String dateString) {
    Date date = null;
    this.isValidDate = true;
    if (dateString == null || dateString.trim().equals("")) {
      return date;
    }
    if (!dateValidation(dateString)) {
    	this.isValidDate = false;
    	return null;
    }
    try {
      //date = (Date) displayFormat.parse(dateString);
      TimeUtil tu = new TimeUtil();
      date = tu.getServerDateTime(displayFormat, dateString);

    }
    catch (Exception ex) {
      // we will leave it as a null date
      log.warn("Unable to format date.");
      ex.printStackTrace();
    }

    return date;
  }

  private boolean dateValidation(String dateString) {
	  int date = 0;
	  int month = 0;
	  int year = 0;
	  int hour = 0;
	  int minute = 0;
	  int second = 0;
	  String amPM = "";
	  
	  String [] splittedDateString = dateString.split(" ");
	  if (splittedDateString.length != 3) {
		  return false;
	  }
	  // Verify for MM/dd/yyyy format or dd/MM/yyyy format
	  String [] dateArray = splittedDateString[0].split("/");
	  if (dateArray.length != 3) {
		  return false;
	  }
	  try {
		  if (displayDateFormat.toLowerCase().startsWith("dd")) {
			  date = Integer.parseInt(dateArray[0]);
			  month = Integer.parseInt(dateArray[1]);
			  year = Integer.parseInt(dateArray[2].substring(0, 4));
		  }
		  else {
			  date = Integer.parseInt(dateArray[1]);
			  month = Integer.parseInt(dateArray[0]);
			  year = Integer.parseInt(dateArray[2].substring(0, 4));
		  }
	  }
	  catch(NumberFormatException  ne){
		  log.error("NumberFormatException: " + ne.getMessage());
		  return false;
	  }
	  catch(IndexOutOfBoundsException ie) {
		  log.error("IndexOutOfBoundsException: " + ie.getMessage());
		  return false;
	  }
	  if (month > 12 || month < 1) {
		  return false;
	  }
	  if ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && (date > 31 || date < 1)) {
		  return false;
	  }
	  if ((month == 4 || month == 6 || month == 9 || month == 11) && (date > 30 || date < 1)) {
		  return false;
	  }
	  if (month == 2) {
		  if (date < 1) {
			  return false;
		  }
		  if (isLeapYear(year) == true) {
			  if (date > 29) {
				  return false;
			  }
		  }
		  else {
			  if (date > 28) {
				  return false;
			  }
		  }
	  }  
	  
	  // Verify for hh:mm:ss format
	  String [] time = splittedDateString[1].split(":");
	  if (splittedDateString.length != 3) {
		  return false;
	  }
	  hour = Integer.parseInt(time[0]);
	  minute = Integer.parseInt(time[1]);
	  second = Integer.parseInt(time[2]);
	  if (hour < 0 || hour > 24) {
		  return false;
	  }
	  if (minute < 0 || minute > 60) {
		  return false;
	  }
	  if (second < 0 || second > 60) {
		  return false;
	  }
	  
	  // Verify for AM or PM format
	  amPM = splittedDateString[2];
	  if (!(amPM.toUpperCase().equals("AM") || amPM.toUpperCase().equals("PM"))) {
		  return false;
	  }
	  return true;
  }
  
  
  private boolean isLeapYear(int year) {
	  if (year % 100 == 0) {
		  if (year % 400 == 0) { 
			  return true; 
		  }
	  }
	  else {
		  if ((year % 4) == 0) { 
			  return true; 
		  }
	  }
	  return false;
  }

  public String getStartDateString()
  {
	if (!this.isValidStartDate) {
		return this.originalStartDateString;
	}
	else {
		return getDisplayFormatFromDate(startDate);
	}
  }
   
  public void setStartDateString(String startDateString)
  {
	this.isValidStartDate = true;  
	Date tempDate = getDateFromDisplayFormat(startDateString);
	if (!this.isValidDate) {
		this.isValidStartDate = false;
		this.originalStartDateString = startDateString;
	}
	else {
		this.startDate= tempDate;
	}
  }

  public String getDueDateString()
  {
    if (!this.isValidDueDate) {
		return this.originalDueDateString;
	}
	else {
		return getDisplayFormatFromDate(dueDate);
	}	  
  }
  public void setDueDateString(String dueDateString)
  {
	this.isValidDueDate = true;
	Date tempDate = getDateFromDisplayFormat(dueDateString);
	if (!this.isValidDate) {
		this.isValidDueDate = false;
		this.originalDueDateString = dueDateString;
	}
	else {
		this.dueDate= tempDate;
	}
  }
  public String getRetractDateString()
  {
    if (!this.isValidRetractDate) {
		return this.originalRetractDateString;
	}
	else {
		return getDisplayFormatFromDate(retractDate);
	}	  	  
  }
  public void setRetractDateString(String retractDateString)
  {
	this.isValidRetractDate = true;
	Date tempDate = getDateFromDisplayFormat(retractDateString);
	if (!this.isValidDate) {
		this.isValidRetractDate = false;
		this.originalRetractDateString = retractDateString;
	}
	else {
		this.retractDate= tempDate;
	}
  }
  public String getFeedbackDateString()
  {
    if (!this.isValidFeedbackDate) {
		return this.originalFeedbackDateString;
	}
	else {
		return getDisplayFormatFromDate(feedbackDate);
	}	  	  	  
  }
  public void setFeedbackDateString(String feedbackDateString)
  {
	this.isValidFeedbackDate = true;
	Date tempDate = getDateFromDisplayFormat(feedbackDateString);
	if (!this.isValidDate) {
		this.isValidFeedbackDate = false;
		this.originalFeedbackDateString = feedbackDateString;
	}
	else {
		this.feedbackDate= tempDate;
	}	  
  }
  
  public String getPublishedUrl() {
    return this.publishedUrl;
  }

  public void setPublishedUrl(String publishedUrl) {
    this.publishedUrl = publishedUrl;
  }

  public String getAlias() {
    return this.alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public SelectItem[] getPublishingTargets() {
    HashMap targets = ptHelper.getTargets();
    Set e = targets.keySet();
    Iterator iter = e.iterator();
    // sort the targets
    String[] titles = new String[targets.size()];
    while (iter.hasNext()){
	for (int m = 0; m < e.size(); m++) {
	    String t = (String)iter.next();
	    //log.info("target "+m+"="+t);
	    titles[m] = t;
	}
    }
    Arrays.sort(titles);
    SelectItem[] target = new SelectItem[targets.size()];
    for (int i=0; i<titles.length; i++){
	target[i] = new SelectItem(titles[i]);
    }
    /**
    SelectItem[] target = new SelectItem[targets.size()];
    while (iter.hasNext()) {
      for (int i = 0; i < e.size(); i++) {
        target[i] = new SelectItem( (String) iter.next());
      }
    }
    */
    return target;
  }

  public void setTargetSelected(String[] targetSelected) {
    this.targetSelected = targetSelected;
  }

  public String[] getTargetSelected() {
    return targetSelected;
  }

  public String[] getTargetSelected(String releaseTo) {
	if (releaseTo != null){
	  String [] releaseToArray = new String[1];
	  releaseToArray[0] = releaseTo;
	  this.targetSelected = releaseToArray;
	}
    return this.targetSelected;
  }

  public void setFirstTargetSelected(String firstTargetSelected){
    this.firstTargetSelected = firstTargetSelected.trim();
    this.targetSelected[0] = firstTargetSelected.trim();
  }

  public String getFirstTargetSelected(){
    return firstTargetSelected;
  }

  public String getFirstTargetSelected(String releaseTo){
    if (releaseTo != null){
    	String [] releaseToArray = new String[1];
        releaseToArray[0] = releaseTo;
        this.targetSelected = releaseToArray;
        this.firstTargetSelected = targetSelected[0].trim();
    }
    return this.firstTargetSelected;
  }

  	/**
	 * @return Returns the outcome.
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * @param outcome
	 *            The outcome to set.
	 */
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public boolean getActive() {
		Date currentDate = new Date();
		if ((this.dueDate != null && currentDate.after(this.dueDate))
				|| (this.retractDate != null && currentDate
						.after(this.retractDate))) {
			return false;
		}
		return true;
	}

	public void setDisplayFormat(String displayDateFormat) {
		this.displayDateFormat = displayDateFormat;
		this.displayFormat = new SimpleDateFormat(displayDateFormat);
	}

	public boolean getIsValidStartDate() {
		return this.isValidStartDate;
	}

	public boolean getIsValidDueDate() {
		return this.isValidDueDate;
	}

	public boolean getIsValidRetractDate() {
		return this.isValidRetractDate;
	}

	public boolean getIsValidFeedbackDate() {
		return this.isValidFeedbackDate;
	}
	
	public void resetIsValidDate() {
		this.isValidStartDate = true;
		this.isValidDueDate = true;
		this.isValidRetractDate = true;
		this.isValidFeedbackDate = true;
	}
	  
	public void resetOriginalDateString() {
		this.originalStartDateString = "";
		this.originalDueDateString = "";
		this.originalRetractDateString = "";
		this.originalFeedbackDateString = "";
	 }

	public boolean getupdateMostCurrentSubmission() {
		return this.updateMostCurrentSubmission;
	}

	public void setUpdateMostCurrentSubmission(boolean updateMostCurrentSubmission) {
	    this.updateMostCurrentSubmission = updateMostCurrentSubmission;
	}
	
	public String editSettingBeforePublish() {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setFromPage("saveSettingsAndConfirmPublish");
		return "editPublishedAssessmentSettings";
	}

	  /**
	 * gopalrc Nov 2007
	 * Returns all groups for site
	 * @return
	 */
	public SelectItem[] getGroupsForSite() {
		SelectItem[] groupSelectItems = new SelectItem[0];
		TreeMap sortedSelectItems = new TreeMap();
		Site site = null;
		try {
			site = SiteService.getSite(ToolManager.getCurrentPlacement()
					.getContext());
			Collection groups = site.getGroups();
			if (groups != null && groups.size() > 0) {
				groupSelectItems = new SelectItem[groups.size()];
				Iterator groupIter = groups.iterator();
				while (groupIter.hasNext()) {
					Group group = (Group) groupIter.next();
					//String groupType = group.getProperties().getProperty("sections_category");
					//groupType = groupType == null ? "" : " (" + groupType + ")";
					String groupDescription = group.getDescription() == null
							|| group.getDescription().equals("") ? "" : " : "
							+ group.getDescription();
					String displayDescription = group.getTitle()
							+ groupDescription;
					sortedSelectItems.put(group.getTitle().toUpperCase(),
							new SelectItem(group.getId(), displayDescription));
				}
				Set keySet = sortedSelectItems.keySet();
				groupIter = keySet.iterator();
				int i = 0;
				while (groupIter.hasNext()) {
					groupSelectItems[i++] = (SelectItem) sortedSelectItems
							.get(groupIter.next());
				}
			}
		} catch (IdUnusedException ex) {
			// No site available
		}
		return groupSelectItems;
	}

	/**
	 * gopalrc Nov 2007
	 * Returns the total number of groups for this site
	 * @return
	 */
	public int getNumberOfGroupsForSite() {
		int numGroups = 0;
		try {
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement()
					.getContext());
			Collection groups = site.getGroups();
			if (groups != null) {
				numGroups = groups.size();
			}
		} catch (IdUnusedException ex) {
			// No site available
		}
		return numGroups;
	}

	/**
	 * gopalrc Nov 2007
	 * The authorized groups
	 */
	private String[] groupsAuthorized;

	/**
	 * gopalrc Nov 2007
	 * Returns the groups to which this assessment is released
	 * @return
	 */
	public String[] getGroupsAuthorized() {
		groupsAuthorized = null;
		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance()
				.getAuthzQueriesFacade();
		List authorizations = authz.getAuthorizationByFunctionAndQualifier(
				"TAKE_PUBLISHED_ASSESSMENT", getAssessmentId().toString());
		if (authorizations != null && authorizations.size() > 0) {
			groupsAuthorized = new String[authorizations.size()];
			Iterator authsIter = authorizations.iterator();
			int i = 0;
			while (authsIter.hasNext()) {
				AuthorizationData ad = (AuthorizationData) authsIter.next();
				groupsAuthorized[i++] = ad.getAgentIdString();
			}
		}
		return groupsAuthorized;
	}

	public boolean getIsMarkForReview() {
		return this.isMarkForReview;
	}

	public void setIsMarkForReview(boolean isMarkForReview) {
		this.isMarkForReview = isMarkForReview;
	}
}



