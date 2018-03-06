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



package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.facade.*;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentTemplateIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.RegisteredSecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecuredIPAddressIfc;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.PublishingTargetHelper;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentAttachmentListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 *
 * Used to be org.navigoproject.ui.web.asi.author.assessment.AssessmentActionForm.java
 */
@Slf4j
public class AssessmentSettingsBean
    implements Serializable {

    private static final IntegrationContextFactory integrationContextFactory =
      IntegrationContextFactory.getInstance();
    private static final GradebookServiceHelper gbsHelper =
      integrationContextFactory.getGradebookServiceHelper();
    private static final PublishingTargetHelper ptHelper =
      integrationContextFactory.getPublishingTargetHelper();
    private static final boolean integrated =
      integrationContextFactory.isIntegrated();

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -630950053380808339L;
    private String outcomeSave;
    private String outcomePublish;
  private AssessmentFacade assessment;
  private Long assessmentId;
  private String title;
  private String creator;
  private String description;
  private boolean hasQuestions;
  private String templateTitle;
  private String templateDescription;

  // meta data
  private String objectives;
  private String keywords;
  private String rubrics;
  private String authors;
  
  // these are properties in AssessmentAccessControl
  private Date startDate;
  private Date dueDate;
  private Date retractDate;
  private Date feedbackDate;
  private Integer timeLimit = 0; // in seconds, calculated from timedHours & timedMinutes
  private Integer timedHours = 0;
  private Integer timedMinutes = 0;
  private Integer timedSeconds = 0;
  private boolean timedAssessment = false;
  private boolean autoSubmit = false;
  private String assessmentFormat; // question (1)/part(2)/assessment(3) on separate page
  private String itemNavigation; // linear (1)or random (2)
  private String itemNumbering; // continuous between parts(1), restart between parts(2)
  private String displayScoreDuringAssessments;
  private String unlimitedSubmissions;
  private String submissionsAllowed;
  private String submissionsSaved; // bad name, this is autoSaved
  private String lateHandling;
  private String instructorNotification = Integer.toString( SamigoConstants.NOTI_PREF_INSTRUCTOR_EMAIL_DEFAULT ); // Default is 'No - I do not want to receive any emails';
  private String submissionMessage;
  private String releaseTo;
  private SelectItem[] publishingTargets;
  private String[] targetSelected;
  private String firstTargetSelected;
  private String password;
  private String finalPageUrl;
  private String ipAddresses;
  private boolean secureDeliveryAvailable;
  private SelectItem[] secureDeliveryModuleSelections;
  private String secureDeliveryModule;
  private String secureDeliveryModuleExitPassword;

  // properties of AssesmentFeedback
  private String feedbackDelivery; // immediate, on specific date , no feedback
  private String feedbackComponentOption; // 2 = select options, 1 = total scores only 

//private String editComponents; // 0 = cannot
  private boolean showQuestionText = true;
  private boolean showStudentResponse = false;
  private boolean showCorrectResponse = false;
  private boolean showStudentScore = false;
  private boolean showStudentQuestionScore = false;
  private boolean showQuestionLevelFeedback = false;
  private boolean showSelectionLevelFeedback = false; // must be MC
  private boolean showGraderComments = false;
  private boolean showStatistics = false;

  // properties of EvaluationModel
  private boolean anonymousGrading;
  private boolean gradebookExists;
  private boolean toDefaultGradebook;
  private String scoringType;
  private String bgColor;
  private String bgImage;
  private HashMap values = new HashMap(); // contains only "can edit" element
  private String bgColorSelect;
  private String bgImageSelect;

  // extra properties
  private boolean noTemplate;
  private String publishedUrl;
  private String alias;
  private static boolean error;

  private List attachmentList;

  private boolean isValidStartDate = true;
  private boolean isValidDueDate = true;
  private boolean isValidRetractDate = true;
  private boolean isValidFeedbackDate = true;
  
  private String originalStartDateString;
  private String originalDueDateString;
  private String originalRetractDateString;
  private String originalFeedbackDateString;
  
  private boolean isMarkForReview;
  private boolean honorPledge;
  private String releaseToGroupsAsString;
  private String blockDivs;
  
  private List<ExtendedTime> extendedTimes;
  private ExtendedTime extendedTime;
  private ExtendedTime transitoryExtendedTime;
  private boolean editingExtendedTime = false;
  
  // SAM-2323 jQuery-UI datepicker
  private final TimeUtil tu = new TimeUtil();
  private final String HIDDEN_START_DATE_FIELD = "startDateISO8601";
  private final String HIDDEN_END_DATE_FIELD = "endDateISO8601";
  private final String HIDDEN_RETRACT_DATE_FIELD = "retractDateISO8601";
  private final String HIDDEN_FEEDBACK_DATE_FIELD = "feedbackDateISO8601";
  
  private SimpleDateFormat displayFormat;

  private ResourceLoader assessmentSettingMessages;

  /*
   * Creates a new AssessmentBean object.
   */
  public AssessmentSettingsBean() {
      this.assessmentSettingMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
  }

  public AssessmentFacade getAssessment() {
    return assessment;
  }

 
  public void setAssessment(AssessmentFacade assessment) {
    try {
      //1.  set the template info
      AssessmentService service = new AssessmentService();
      AssessmentTemplateIfc template = null;
      if (assessment.getAssessmentTemplateId()!=null){
        template = service.getAssessmentTemplate(
          assessment.getAssessmentTemplateId().toString());
      }
      if (template != null){
        setNoTemplate(false);
        this.templateTitle = template.getTitle();
        this.templateDescription = template.getDescription();        
      }
      else{
        setNoTemplate(true);
      }
      //2. set the assessment info
      this.assessment = assessment;
      // set the valueMap
      setValueMap(assessment.getAssessmentMetaDataMap());
      this.assessmentId = assessment.getAssessmentId();
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
      if((assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
						    BGIMAGE)!=null )&&(!assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
																										BGIMAGE).equals(""))){
	    this.bgImageSelect="1";
            this.bgColorSelect=null;
	}
	else{
            this.bgImageSelect=null;
	    this.bgColorSelect="1";
	}
        ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
        this.extendedTimes = extendedTimeFacade.getEntriesForAss(assessment.getData());

        resetExtendedTime();

      // these are properties in AssessmentAccessControl
      AssessmentAccessControlIfc accessControl;
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
        // SAK-1850 - when importing assessment forget to set releaseTo, we will catch it
        // and set it to host site
        if (!validateTarget(firstTargetSelected)){
          releaseTo = AgentFacade.getCurrentSiteName();
          firstTargetSelected = AgentFacade.getCurrentSiteName();
          targetSelected = getTargetSelected(releaseTo);
        }
        groupsAuthorized = null;

        this.timeLimit = accessControl.getTimeLimit(); // in seconds
        if (timeLimit !=null && timeLimit>0)
          setTimeLimitDisplay(timeLimit);
        else 
          resetTimeLimitDisplay();
        if ((Integer.valueOf(1)).equals(accessControl.getTimedAssessment()))
          this.timedAssessment = true;
        this.autoSubmit = (Integer.valueOf(1)).equals(accessControl.getAutoSubmit());
        if (accessControl.getAssessmentFormat()!=null)
          this.assessmentFormat = accessControl.getAssessmentFormat().toString(); // question/part/assessment on separate page
        if (accessControl.getItemNavigation()!=null)
          this.itemNavigation = accessControl.getItemNavigation().toString(); // linear or random
        if (accessControl.getItemNumbering()!=null)
          this.itemNumbering = accessControl.getItemNumbering().toString();
        if (accessControl.getDisplayScoreDuringAssessments()!=null)
        	this.displayScoreDuringAssessments=accessControl.getDisplayScoreDuringAssessments().toString();
        if (accessControl.getSubmissionsSaved()!=null) // bad name, this is autoSaved
          this.submissionsSaved = accessControl.getSubmissionsSaved().toString();

        this.isMarkForReview = accessControl.getMarkForReview() != null && (Integer.valueOf(1)).equals(accessControl.getMarkForReview());
        if (accessControl.getHonorPledge() != null)
          this.honorPledge = accessControl.getHonorPledge();
        // default to unlimited if control value is null
        if (accessControl.getUnlimitedSubmissions()!=null && !accessControl.getUnlimitedSubmissions()){
          this.unlimitedSubmissions=AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString();
          this.submissionsAllowed = accessControl.getSubmissionsAllowed().toString();
        }
        else{
          this.unlimitedSubmissions=AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString();
          this.submissionsAllowed="";
        }
        if (accessControl.getLateHandling() !=null)
          this.lateHandling = accessControl.getLateHandling().toString();
        if (accessControl.getInstructorNotification() != null) {
          this.instructorNotification = accessControl.getInstructorNotification().toString();
          this.assessment.setInstructorNotification(Integer.valueOf(this.instructorNotification));
        }
        if (accessControl.getSubmissionsSaved()!=null)
          this.submissionsSaved = accessControl.getSubmissionsSaved().toString();
        this.submissionMessage = accessControl.getSubmissionMessage();
        this.password = accessControl.getPassword();
        this.finalPageUrl = accessControl.getFinalPageUrl();
      }

      // properties of AssesmentFeedback
      AssessmentFeedbackIfc feedback = assessment.getAssessmentFeedback();
      if (feedback != null) {
        if (feedback.getFeedbackDelivery()!=null)
          this.feedbackDelivery = feedback.getFeedbackDelivery().toString();
        
        if (feedback.getFeedbackComponentOption()!=null)
            this.feedbackComponentOption = feedback.getFeedbackComponentOption().toString();

        if (feedback.getFeedbackAuthoring()!=null)
          this.feedbackAuthoring = feedback.getFeedbackAuthoring().toString();

        this.showQuestionText = (Boolean.TRUE).equals(feedback.getShowQuestionText());
        this.showStudentResponse = (Boolean.TRUE).equals(feedback.getShowStudentResponse());
        this.showCorrectResponse = (Boolean.TRUE).equals(feedback.getShowCorrectResponse());
        this.showStudentScore = (Boolean.TRUE).equals(feedback.getShowStudentScore());
        this.showStudentQuestionScore = (Boolean.TRUE).equals(feedback.getShowStudentQuestionScore());
        this.showQuestionLevelFeedback = (Boolean.TRUE).equals(feedback.getShowQuestionLevelFeedback());
        this.showSelectionLevelFeedback = (Boolean.TRUE).equals(feedback.getShowSelectionLevelFeedback()); // must be MC
        this.showGraderComments = (Boolean.TRUE).equals(feedback.getShowGraderComments());
        this.showStatistics = (Boolean.TRUE).equals(feedback.getShowStatistics());
      }

      // properties of EvaluationModel
      EvaluationModelIfc evaluation = assessment.getEvaluationModel();
      if (evaluation != null) {
        if (evaluation.getAnonymousGrading()!=null)
          this.anonymousGrading = evaluation.getAnonymousGrading().toString().equals("1");
        if (evaluation.getToGradeBook()!=null )
          this.toDefaultGradebook = evaluation.getToGradeBook().equals("1");
        if (evaluation.getScoringType()!=null)
          this.scoringType = evaluation.getScoringType().toString();

        String currentSiteId = AgentFacade.getCurrentSiteId();
        this.gradebookExists = gbsHelper.isGradebookExist(currentSiteId);
      }

      // ip addresses
      this.ipAddresses = "";
      Set ipAddressSet = assessment.getSecuredIPAddressSet();
      if (ipAddressSet != null){
        Iterator iter = ipAddressSet.iterator();
        while (iter.hasNext()) {
          SecuredIPAddressIfc ip = (SecuredIPAddressIfc) iter.next();
          if (ip.getIpAddress()!=null)
            this.ipAddresses = ip.getIpAddress()+"\n"+this.ipAddresses;
        }
      }
      // attachment
      this.attachmentList = assessment.getAssessmentAttachmentList();
      
      // secure delivery
      SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI(); 
      this.secureDeliveryAvailable = secureDeliveryService.isSecureDeliveryAvaliable();
      this.secureDeliveryModuleSelections = getSecureDeliverModuleSelections();
      this.secureDeliveryModule = (String) assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY );
      this.secureDeliveryModuleExitPassword = secureDeliveryService.decryptPassword( this.secureDeliveryModule, 
    		  (String) assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.EXITPWD_KEY ) );
      
      if ( secureDeliveryModule == null || secureDeliveryModule.trim().length() == 0) {
    	  this.secureDeliveryModule = SecureDeliveryServiceAPI.NONE_ID;
      }
      else if ( ! secureDeliveryService.isSecureDeliveryModuleAvailable( secureDeliveryModule ) ) {
    	  log.warn( "Assessment " + this.assessmentId + " requires secure delivery module " + this.secureDeliveryModule + 
    	  			" but the module is no longer available. Secure delivery module will revert to NONE" );
    	  secureDeliveryModule = SecureDeliveryServiceAPI.NONE_ID;
      }
    }
    catch (RuntimeException ex) {
    	log.error(ex.getMessage(), ex);
    }
  }

 public String getBgColorSelect()
    {
  return this.bgColorSelect;
    }
    public void setBgColorSelect(String bgColorSelect)
    {
	this.bgColorSelect=bgColorSelect;
    }

    public String getBgImageSelect()
    {
  return this.bgImageSelect;
    }
    public void setBgImageSelect(String bgImageSelect)
    {
	this.bgImageSelect=bgImageSelect;
    }

    //Huong adding for outcome error
    public String getOutcomeSave()
    {
  return this.outcomeSave;
    }
    public void setOutcomeSave(String outcomeSave)
    {
  this.outcomeSave=outcomeSave;
    }
    
    public String getOutcomePublish()
    {
  return this.outcomePublish;
    }
    public void setOutcomePublish(String outcomePublish)
    {
  this.outcomePublish=outcomePublish;
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
      if((this.getBgColorSelect()!=null) && (this.getBgColorSelect().equals("1")))
    return this.bgColor;
      else
	  return "";
    
  }

  public void setBgColor(String bgColor) {
    if((this.getBgColorSelect()!=null) && (this.getBgColorSelect().equals("1")))
    this.bgColor = bgColor;
    else
	this.bgColor="";
   
  }


  public String getBgImage() {
      if((this.getBgImageSelect()!=null) && (this.getBgImageSelect().equals("1")))
	  return this.bgImage;
      else return "";
     
  }

  public void setBgImage(String bgImage) {
 if((this.getBgImageSelect()!=null) && (this.getBgImageSelect().equals("1")))
     
    this.bgImage = bgImage;
 else this.bgImage="";
     
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
    this.releaseTo="";
    if (targetSelected != null){
        for( String user : targetSelected )
        {
            if (!"".equals(releaseTo))
                releaseTo = releaseTo + ", " + user;
            else
                releaseTo = user;
        }
    }
    return this.releaseTo;
  }

  public void setReleaseTo(String releaseTo) {
    this.releaseTo = releaseTo;
  }

  public Integer getTimeLimit() {
    return  timedHours*3600
            + timedMinutes*60
            + timedSeconds;
  }

  public void setTimeLimit(Integer timeLimit) {
    this.timeLimit = timeLimit;
  }

  public void setTimedHours(Integer timedHours) {
    this.timedHours = (timedHours==null)?0:timedHours;
  }

  public Integer getTimedHours() {
    return timedHours;
  }

  public void setTimedMinutes(Integer timedMinutes) {
    this.timedMinutes =  (timedMinutes==null)?0:timedMinutes;
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
  
  public String getDisplayScoreDuringAssessments(){
	  return displayScoreDuringAssessments;
  }
  
  public void setDisplayScoreDuringAssessments(String displayScoreDuringAssessments){
	  this.displayScoreDuringAssessments = displayScoreDuringAssessments;
  }

  public String getUnlimitedSubmissions() {
    return unlimitedSubmissions;
  }

  public void setUnlimitedSubmissions(String unlimitedSubmissions) {
	  String itemNavigationUpdated = ContextUtil.lookupParam("itemNavigationUpdated");
	  if (itemNavigationUpdated != null && Boolean.parseBoolean(itemNavigationUpdated)) {
		  if (itemNavigation != null && "1".equals(itemNavigation)) {
			  this.unlimitedSubmissions = AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString();
		  }
		  else {
			  this.unlimitedSubmissions = unlimitedSubmissions;
		  }
	  }
	  else {
		  this.unlimitedSubmissions = unlimitedSubmissions;
	  }
  }

  public String getSubmissionsAllowed() {
    return submissionsAllowed;
  }

  public void setSubmissionsAllowed(String submissionsAllowed) {
	  String itemNavigationUpdated = ContextUtil.lookupParam("itemNavigationUpdated");
	  if (itemNavigationUpdated != null && Boolean.parseBoolean(itemNavigationUpdated)) {
		  if (itemNavigation != null && "1".equals(itemNavigation)) {
			  this.submissionsAllowed = "1";
		  }
		  else {
			  this.submissionsAllowed = submissionsAllowed;
		  }
	  }
	  else {
		  this.submissionsAllowed = submissionsAllowed;
	  }
  }

  public void setLateHandling(String lateHandling) {
    this.lateHandling = lateHandling;
  }

  public String getLateHandling() {
    return lateHandling;
  }

  public void setInstructorNotification(String instructorNotification) {
    this.instructorNotification = instructorNotification;
    this.assessment.setInstructorNotification(Integer.valueOf(this.instructorNotification));
  }

  public String getInstructorNotification() {
    return instructorNotification;
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

  public String getFeedbackComponentOption() {
		return feedbackComponentOption;
  }

  public void setFeedbackComponentOption(String feedbackComponentOption) {
		this.feedbackComponentOption = feedbackComponentOption;
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

  public boolean getAnonymousGrading() {
    return this.anonymousGrading;
  }

  public void setAnonymousGrading(boolean anonymousGrading) {
    this.anonymousGrading = anonymousGrading;
  }

  public boolean getToDefaultGradebook() {
    return this.toDefaultGradebook;
  }

  public void setToDefaultGradebook(boolean toDefaultGradebook) {
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

  public boolean isHonorPledge() { return honorPledge; }

  public void setHonorPledge(boolean honorPledge) { this.honorPledge = honorPledge; }

    public void setValue(String key, Object value){
    this.values.put(key, value);
  }

  // retrieve value in valueMap
  public Boolean getValue(String key)
  {
    Boolean returnValue = Boolean.FALSE;
    Object o = this.values.get(key);

    if (("true").equals((String)o))
       returnValue = Boolean.TRUE;
    return returnValue;
  }
  
  public String getSecureDeliveryModule() {
	return secureDeliveryModule;
  }
	    
  public void setSecureDeliveryModule(String secureDeliveryModule) {
	this.secureDeliveryModule = secureDeliveryModule;
  }

  public String getSecureDeliveryModuleExitPassword() {
	return secureDeliveryModuleExitPassword;
  }

  public void setSecureDeliveryModuleExitPassword(String secureDeliveryModuleExitPassword) {
	this.secureDeliveryModuleExitPassword = secureDeliveryModuleExitPassword;
  }
  
  public void setSecureDeliveryModuleSelections(SelectItem[] secureDeliveryModuleSelections) {
	this.secureDeliveryModuleSelections = secureDeliveryModuleSelections;
  }
  
  public SelectItem[] getSecureDeliveryModuleSelections() {
	return secureDeliveryModuleSelections;
  }

  public boolean isSecureDeliveryAvailable() {
	return secureDeliveryAvailable;
  }

  public void setSecureDeliveryAvailable(boolean secureDeliveryAvailable) {
    this.secureDeliveryAvailable = secureDeliveryAvailable;
  }

  // newMap contains both the regular metadata such as "objectives" as well as
  // "can edit" element. However, we only want to have "can edit" elements inside
  // our valueMap, so we need to weed out the metadata
  public void setValueMap(HashMap newMap){
	  HashMap h = new HashMap();

	  for (Iterator it = newMap.entrySet().iterator(); it.hasNext();) {
		  Map.Entry entry = (Map.Entry) it.next();
		  String key = (String) entry.getKey();
		  Object o = entry.getValue();

		  if (("ASSESSMENT_AUTHORS".equals(key)) ||
				  ("ASSESSMENT_KEYWORDS".equals(key)) ||
				  ("ASSESSMENT_OBJECTIVES".equals(key)) ||
				  ("ASSESSMENT_RUBRICS".equals(key)) ||
				  ("ASSESSMENT_BGCOLOR".equals(key)) ||
				  ("ASSESSMENT_BGIMAGE".equals(key)) || 
				  (SecureDeliveryServiceAPI.MODULE_KEY.equals(key)) ||
				  (SecureDeliveryServiceAPI.EXITPWD_KEY.equals(key)));
		  else{
			  h.put(key, o);
		  }
	  }
	  this.values = h ;
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
    this.timedHours=time/60/60;
    this.timedMinutes = (time/60)%60;
    this.timedSeconds = time % 60;
  }

  public void resetTimeLimitDisplay(){
    this.timedHours=0;
    this.timedMinutes = 0;
    this.timedSeconds = 0;
  }

  // followings are set of SelectItem[] used in authorSettings.jsp
  public SelectItem[] getHours() {
    return hours;
  }

  public static void setHours(SelectItem[] hours) {
    AssessmentSettingsBean.hours =  hours;
  }

  public SelectItem[] getMins() {
    return mins;
  }

  public static void setMins(SelectItem[] mins) {
    AssessmentSettingsBean.mins =  mins;
  }

  private static List months;
  private static List days;
  private static SelectItem[] mins;
  private static SelectItem[] hours;
  // don't know what this is for, had to add it --esmiley
  private String feedbackAuthoring;// this was left out, but referenced in UI

  static{
    months = new ArrayList();
    for (int i=1; i<=12; i++){
      months.add(new SelectItem(Integer.valueOf(i)));
    }
    days = new ArrayList();
    for (int i=1; i<32; i++){
      days.add(new SelectItem(Integer.valueOf(i)));
    }
    hours = new SelectItem[24];
    for (int i=0; i<24; i++){
      if (i < 10)
        hours[i] = new SelectItem(Integer.valueOf(i),"0"+i);
      else
        hours[i] = new SelectItem(Integer.valueOf(i),i+"");
    }
    mins = new SelectItem[60];
    for (int i=0; i<60; i++){
      if (i < 10)
        mins[i] = new SelectItem(Integer.valueOf(i),"0"+i);
      else
        mins[i] = new SelectItem(Integer.valueOf(i),i+"");
    }
  }

  public String getIpAddresses() {
    return ipAddresses;
  }

  public void setIpAddresses(String ipAddresses) {
    this.ipAddresses = ipAddresses;
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

    if (displayFormat == null) {   
    	setDisplayFormat(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec"));
    }

    try {
      // Do not manipulate the date based on the client browser timezone.
      dateString = tu.getDisplayDateTime(displayFormat, date, false);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
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
    if (startDateString == null || startDateString.trim().equals("")) {
      this.isValidStartDate = true;
      this.startDate = null;
    }
    else {

      Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam(HIDDEN_START_DATE_FIELD));

      if (tempDate != null) {
        this.isValidStartDate = true;
        this.startDate = tempDate;
      }
      else {
        log.error("setStartDateString could not parse hidden start date: " + ContextUtil.lookupParam(HIDDEN_START_DATE_FIELD));
        this.isValidStartDate = false;
        this.originalStartDateString = startDateString;
      }
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
    if (dueDateString == null || dueDateString.trim().equals("")) {
      this.isValidDueDate = true;
      this.dueDate = null;
    }
    else {

      Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam(HIDDEN_END_DATE_FIELD));

      if (tempDate != null) {
        this.isValidDueDate = true;
        this.dueDate = tempDate;
      }
      else {
        log.error("setDueDateString could not parse hidden date field: " + ContextUtil.lookupParam(HIDDEN_END_DATE_FIELD));
        this.isValidDueDate = false;
        this.originalDueDateString = dueDateString;
      }
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
    if (retractDateString == null || retractDateString.trim().equals("")) {
      this.isValidRetractDate = true;
      this.retractDate = null;
    }
    else {

      Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam(HIDDEN_RETRACT_DATE_FIELD));

      if (tempDate != null) {
        this.isValidRetractDate = true;
        this.retractDate = tempDate;
      } else {

        log.error("setRetractDateString could not parse hidden date field: " + ContextUtil.lookupParam(HIDDEN_RETRACT_DATE_FIELD));
        this.isValidRetractDate = false;
        this.originalRetractDateString = retractDateString;
      }
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

  public void setFeedbackDateString(String feedbackDateString) {
    if (feedbackDateString == null || feedbackDateString.trim().equals("")) {
      this.isValidFeedbackDate = true;
      this.feedbackDate = null;
    }
    else {

      Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam(HIDDEN_FEEDBACK_DATE_FIELD));

      if (tempDate != null) {
        this.isValidFeedbackDate = true;
        this.feedbackDate = tempDate;
      }
      else {
      log.error("setFeedbackDateString could not parse hidden date field: " + ContextUtil.lookupParam(HIDDEN_FEEDBACK_DATE_FIELD));
      this.isValidFeedbackDate = false;
      this.originalFeedbackDateString = feedbackDateString;
      }
    }
  }

  public String getTemplateTitle() {
    return this.templateTitle;
  }

  public void setTemplateTitle(String title) {
    this.templateTitle = title;
  }

  public String getTemplateDescription() {
    return this.templateDescription;
  }

  public void setTemplateDescription(String templateDescription) {
    this.templateDescription = templateDescription;
  }

  public boolean getNoTemplate() {
    return this.noTemplate;
  }

  public void setNoTemplate(boolean noTemplate) {
    this.noTemplate = noTemplate;
  }

  public boolean validateTarget(String firstTarget){
    HashMap targets = ptHelper.getTargets();
    return targets.get(firstTarget) != null;
  }

  public SelectItem[] getPublishingTargets(){
    HashMap targets = ptHelper.getTargets();
    Set e = targets.keySet();
    Iterator iter = e.iterator();
    int numSelections = getNumberOfGroupsForSite() > 0 ? 3 : 2;
   	SelectItem[] target = new SelectItem[numSelections];

    while (iter.hasNext()){
	    String t = (String)iter.next();
	    if ("Anonymous Users".equals(t)) {
	    	target[0] = new SelectItem(t, assessmentSettingMessages.getString("anonymous_users"));
	    }
	    else if (numSelections == 3 && t.equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
	      target[2] = new SelectItem(t, assessmentSettingMessages.getString("selected_groups"));
	    }
	    else if (t.equals(AgentFacade.getCurrentSiteName())) {
	      target[1] = new SelectItem(t, assessmentSettingMessages.getString("entire_site"));
	    }
    }
    return target;
  }


  public void setTargetSelected(String[] targetSelected){
    this.targetSelected = targetSelected;
  }

  public String[] getTargetSelected(){
    return targetSelected;
  }

  public String[] getTargetSelected(String releaseTo){
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

    public String checkDate(){
  FacesContext context=FacesContext.getCurrentInstance();
  ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
        String err;
  if(AssessmentSettingsBean.error)
      {
	err=rb.getString("deliveryDate_error");
    context.addMessage(null,new FacesMessage(err));
    log.error("START DATE ADD MESSAGE");
    return "deliveryDate_error";
      }
  else
      {
    return "saveSettings";
      }

    }
  public String getFeedbackAuthoring()
  {
    return feedbackAuthoring;
  }
  public void setFeedbackAuthoring(String feedbackAuthoring)
  {
    this.feedbackAuthoring = feedbackAuthoring;
  }

  public List getAttachmentList() {
    return attachmentList;
  }

  public void setAttachmentList(List attachmentList)
  {
    this.attachmentList = attachmentList;
  }

  private boolean hasAttachment = false;
  public boolean getHasAttachment(){
    if (attachmentList!=null && attachmentList.size() >0)
      this.hasAttachment = true;
    return this.hasAttachment;
  }

  public String addAttachmentsRedirect() {
    // 1. redirect to add attachment
    try	{
      List filePickerList = new ArrayList();
      if (attachmentList != null){
        filePickerList = prepareReferenceList(attachmentList);
      }
      ToolSession currentToolSession = SessionManager.getCurrentToolSession();
      currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, filePickerList);
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
    }
    catch(Exception e){
      log.error("fail to redirect to attachment page: " + e.getMessage());
    }
    return "editAssessmentSettings";
  }

  public void setAssessmentAttachment(){
    SaveAssessmentAttachmentListener lis = new SaveAssessmentAttachmentListener(true);
    lis.processAction(null);
  }

  private List prepareReferenceList(List attachmentList){
    List list = new ArrayList();
    for (int i=0; i<attachmentList.size(); i++){
      ContentResource cr = null;
      AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
      try{
        cr = AssessmentService.getContentHostingService().getResource(attach.getResourceId());
      }
      catch (PermissionException e) {
    	  log.warn("PermissionException from ContentHostingService:"+e.getMessage());
      }
      catch (IdUnusedException e) {
    	  log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
          // <-- bad sign, some left over association of assessment and resource, 
          // use case: user remove resource in file picker, then exit modification without
          // proper cancellation by clicking at the left nav instead of "cancel".
          // Also in this use case, any added resource would be left orphan. 
          AssessmentService assessmentService = new AssessmentService();
          assessmentService.removeAssessmentAttachment(attach.getAttachmentId().toString());
      }
      catch (TypeException e) {
    	  log.warn("TypeException from ContentHostingService:"+e.getMessage());
      }
      if (cr!=null){
    	Reference ref = EntityManager.newReference(cr.getReference());
        if (ref !=null ) list.add(ref);
      }
    }
    return list;
  }

  private HashMap resourceHash = new HashMap();
  public HashMap getResourceHash() {
      return resourceHash;
  }

  public void setResourceHash(HashMap resourceHash)
  {
      this.resourceHash = resourceHash;
  }
  
  public void setDisplayFormat(String displayDateFormat)
  {
	  this.displayFormat = new SimpleDateFormat(displayDateFormat, new ResourceLoader().getLocale());
  }
  
  public boolean getIsValidStartDate()
  {
	  return this.isValidStartDate;
  }
  
  public boolean getIsValidDueDate()
  {
	  return this.isValidDueDate;
  }
  
  public boolean getIsValidRetractDate()
  {
	  return this.isValidRetractDate;
  }
  
  public boolean getIsValidFeedbackDate()
  {
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
  
  /**
   * Returns all groups for site
   * @return
   */
  public SelectItem[] getGroupsForSite(){
      SelectItem[] groupSelectItems = new SelectItem[0];
      TreeMap sortedSelectItems = new TreeMap();
      Site site;
      try {
          site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
          Collection groups = site.getGroups();
          if (groups != null && groups.size() > 0) {
              groupSelectItems = new SelectItem[groups.size()];
              Iterator groupIter = groups.iterator();
              while (groupIter.hasNext()) {
                  Group group = (Group) groupIter.next();
                  String title = group.getTitle();
                  String groupId = group.getId();
                  String uniqueTitle = title + groupId;
                  sortedSelectItems.put(uniqueTitle.toUpperCase(), new SelectItem(group.getId(), title));
              }
              Set keySet = sortedSelectItems.keySet();
              groupIter = keySet.iterator();
              int i = 0;
              while (groupIter.hasNext()) {
                  groupSelectItems[i++] = (SelectItem) sortedSelectItems.get(groupIter.next());
              }
          }
      } catch (IdUnusedException ex) {
          // No site available
      }
      return groupSelectItems;
  }

  public SelectItem[] getGroupsForSiteWithNoGroup() {
      SelectItem[] items = getGroupsForSite();
      SelectItem[] itemsWithNoGroup = new SelectItem[items.length + 1];
      itemsWithNoGroup[0] = new SelectItem("", assessmentSettingMessages.getString("extendedTime_select_group"));
      for(int i = 1; i <= items.length; i++) {
          itemsWithNoGroup[i] = items[i-1];
      }

      return itemsWithNoGroup;
  }
  
  
  /**
   * Returns the total number of groups for this site
   * @return
   */
  public int getNumberOfGroupsForSite(){
	  int numGroups = 0;
	  try {
		 Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		 Collection groups = site.getGroups();
	     if (groups != null) {
	    	 numGroups = groups.size();
	     }
	  }
	  catch (IdUnusedException ex) {
		  // No site available
	  }
	  return numGroups;
  }

  /**
   * The authorized groups
   */
  private String[] groupsAuthorized;
  private boolean noGroupSelectedError;
  
  /**
   * Returns the groups to which this assessment is released
   * @return
   */
  public String[] getGroupsAuthorized() {
	 if (noGroupSelectedError || groupsAuthorized != null) {
		 return groupsAuthorized;
	 }
	 AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
	 if (authz!=null){
		 List authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_ASSESSMENT", getAssessmentId().toString());
		 if (authorizations != null && authorizations.size()>0) {
			 groupsAuthorized = new String[authorizations.size()];
			 Iterator authsIter = authorizations.iterator();
			 int i = 0;
			 while (authsIter.hasNext()) {
				 AuthorizationData ad = (AuthorizationData) authsIter.next();
				 groupsAuthorized[i++] = ad.getAgentIdString();
			 }
		 }
	 }
	 return groupsAuthorized;
  }
  
  public void setGroupsAuthorized(String[] groupsAuthorized){
	  this.groupsAuthorized = groupsAuthorized;
  }
  
  public void setNoGroupSelectedError(boolean noGroupSelectedError) {
	  this.noGroupSelectedError = noGroupSelectedError;
  }
  
  /** 
   * To compensate for strange stateful behaviour of this bean
   * 
   * TODO: troubleshoot stateful behaviour if time allows
   * - found that it's due to the bean having "session" scope
   * - but changing it to "request" scope causes other issues 
   * 
   * @return
   */
  public String[] getGroupsAuthorizedToSave() {
	  return groupsAuthorized;
  }  
  
  public boolean getIsMarkForReview()
  {
	  return this.isMarkForReview;
  }
  
  public void setIsMarkForReview(boolean isMarkForReview)
  {
	  this.isMarkForReview = isMarkForReview;
  }
  
  public void setReleaseToGroupsAsString(String releaseToGroupsAsString){
	  this.releaseToGroupsAsString = releaseToGroupsAsString;
  }
  
  public String getReleaseToGroupsAsString() {
	  return releaseToGroupsAsString;
  }
  
  public String getReleaseToGroupsAsHtml() {
	  return FormattedText.escapeHtml(releaseToGroupsAsString,false);
  }
  
  public void setBlockDivs(String blockDivs){
	  this.blockDivs = blockDivs;
  }
  
  public String getBlockDivs() {
	  return blockDivs;
  }
  
  public SelectItem[] getSecureDeliverModuleSelections() {
	  
	  SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI(); 
	  Set<RegisteredSecureDeliveryModuleIfc> modules = secureDeliveryService.getSecureDeliveryModules( new ResourceLoader().getLocale() );
 
	  SelectItem[] selections = new SelectItem[ modules.size() ];
	  int index = 0;
	  for ( RegisteredSecureDeliveryModuleIfc module : modules ) {
		  
		  selections[index] = new SelectItem( module.getId(), module.getName() );
		  ++index;
	  }
	  
	  return selections;
  }


	public void setExtendedTimes(List<ExtendedTime> extendedTimes) {
		this.extendedTimes = extendedTimes;
	}

	public List<ExtendedTime> getExtendedTimes() {
		return extendedTimes;
	}

	public int getExtendedTimesSize() {
        return this.extendedTimes.size();
    }

	/**
	 *
	 * @return
	 */
	public SelectItem[] getUsersInSite() {
		SelectItem[] usersInSite = null;
		Site site;

		try {
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			SectionAwareness sectionAwareness = PersistenceService.getInstance().getSectionAwareness();
			// List sections = sectionAwareness.getSections(site.getId());
			List enrollments = sectionAwareness.getSiteMembersInRole(site.getId(), Role.STUDENT);

			// Treemaps are used here because they auto-sort
			TreeMap studentTargets = new TreeMap<>();

			// Add students to target set
			if (enrollments != null && enrollments.size() > 0) {
				for (Iterator iter = enrollments.iterator(); iter.hasNext();) {
					EnrollmentRecord enrollmentRecord = (EnrollmentRecord) iter.next();
					String userId = enrollmentRecord.getUser().getUserUid();
					String userDisplayName = enrollmentRecord.getUser().getSortName();
					studentTargets.put(userDisplayName, userId);
				}
			}

			// Add targets to selectItem array. We put the alpha name in as the
			// key so it would
			// be alphabetized. Now we pull it out and build the select item
			// list.
			int listSize = 1 + studentTargets.size();
			usersInSite = new SelectItem[listSize];
			usersInSite[0] = new SelectItem("", assessmentSettingMessages.getString("extendedTime_select_User"));
			int selectCount = 1;

			// Add in students to select item list
			Set keySet = studentTargets.keySet();
			Iterator iter = keySet.iterator();
			while (iter.hasNext()) {
				String alphaName = (String) iter.next();
				String sakaiId = (String) studentTargets.get(alphaName);
				usersInSite[selectCount++] = new SelectItem(sakaiId, alphaName);
			}

		} catch (IdUnusedException ex) {
			// No site available
		}
		return usersInSite;
	}

	public ExtendedTime getExtendedTime() {
        return this.extendedTime;
    }

    public String getExtendedTimeStartString() {
        return tu.getDateTimeWithTimezoneConversion(this.extendedTime.getStartDate());
    }

    public void setExtendedTimeStartString(String exTimeStartString) {
        Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam("newEntry-start_date-iso8601"));
        if(tempDate != null) {
            this.extendedTime.setStartDate(tempDate);
        }
    }

    public String getExtendedTimeDueString() {
        return tu.getDateTimeWithTimezoneConversion(this.extendedTime.getDueDate());
    }

    public void setExtendedTimeDueString(String exTimeDueString) {
        Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam("newEntry-due_date-iso8601"));
        if(tempDate != null) {
            this.extendedTime.setDueDate(tempDate);
        }
    }

    public String getExtendedTimeRetractString() {
        return tu.getDateTimeWithTimezoneConversion(this.extendedTime.getRetractDate());
    }

    public void setExtendedTimeRetractString(String exTimeRetractString) {
        Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam("newEntry-retract_date-iso8601"));
        if(tempDate != null) {
            this.extendedTime.setRetractDate(tempDate);
        }
    }

    public void setTransitoryExtendedTime(ExtendedTime newExTime) {
        this.transitoryExtendedTime = newExTime;
    }

    //From the form
    public void addExtendedTime() {
  	  addExtendedTime(true);
    }

    //Internal to be able to supress error easier
    public void addExtendedTime(boolean errorToContext) {
        ExtendedTime entry = this.extendedTime;
        if (StringUtils.isBlank(entry.getUser()) && StringUtils.isBlank(entry.getGroup())) {
            if (errorToContext) {
                FacesContext context = FacesContext.getCurrentInstance();
                String errorString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "extended_time_user_and_group_set");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, errorString, null));
            }
        }
        else {
            this.extendedTime.syncDates();
            this.extendedTimes.add(this.extendedTime);
            resetExtendedTime();
        }
    }

    public void deleteExtendedTime() {
        this.extendedTimes.remove(this.transitoryExtendedTime);
        this.transitoryExtendedTime = null;
    }

    public void editExtendedTime() {
      this.editingExtendedTime = true;
      this.extendedTime = new ExtendedTime(this.transitoryExtendedTime);
      //Remove from the list but keep transitory available for cancel
      this.extendedTimes.remove(this.transitoryExtendedTime);
    }

    public void saveEditedExtendedTime() {
      //Re-add after editing
      addExtendedTime();
    }

    public void cancelEdit() {
      //Reset the time back again
      this.extendedTime = this.transitoryExtendedTime;
      addExtendedTime();
    }

    public boolean getEditingExtendedTime() {
      return this.editingExtendedTime;
    }

    private void resetExtendedTime() {
        this.extendedTime = new ExtendedTime(this.getAssessment().getData());
        this.transitoryExtendedTime = null;
        this.editingExtendedTime = false;
    }
}
