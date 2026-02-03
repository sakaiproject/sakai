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

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.business.entity.SebConfig;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.RegisteredSecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecuredIPAddressIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentAttachmentListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.assessment.util.ExtendedTimeValidator;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.AlphaNumericComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.sakaiproject.component.api.ServerConfigurationService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/* For author: Assessment Settings backing bean.*/
@Slf4j
@ManagedBean(name="publishedSettings")
@SessionScoped
public class PublishedAssessmentSettingsBean extends SpringBeanAutowiringSupport implements Serializable {

  private static final IntegrationContextFactory integrationContextFactory =
    IntegrationContextFactory.getInstance();
  private static final GradebookServiceHelper gbsHelper =
      integrationContextFactory.getGradebookServiceHelper();

  private String displayDateFormat;
  private SimpleDateFormat displayFormat;


  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -630950053380808339L;
  @Getter private PublishedAssessmentFacade assessment;
  private Long assessmentId;
  private String title;
  private String creator;
  private String description;
  
  // meta data
  private String objectives;
  private String keywords;
  private String rubrics;
  private String authors;
  @Getter @Setter private Boolean trackQuestions;

  // these are properties in PublishedAccessControl
  private Date startDate;
  private Date dueDate;
  private Date retractDate;
  private Date feedbackDate;
  @Getter @Setter private Date feedbackEndDate;
  @Setter private boolean feedbackScoreThresholdEnabled = false;
  @Getter @Setter private String feedbackScoreThreshold;
  private Integer timeLimit; // in seconds, calculated from timedHours & timedMinutes
  private Integer timedHours;
  private Integer timedMinutes;
  private Integer timedSeconds;
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
  private SelectItem[] publishingTargets;
  @Setter @Getter private String[] targetSelected;
  @Getter private String firstTargetSelected;
  private String releaseTo;
  private String password;
  private String finalPageUrl;
  private String ipAddresses;
  private boolean secureDeliveryAvailable;
  private SelectItem[] secureDeliveryModuleSelections;
  private String secureDeliveryModule;
  private String secureDeliveryModuleExitPassword;
  @Setter private SelectItem[] sebConfigModeSelections;
  @Setter private SelectItem[] booleanSelections;
  @Getter @Setter private String sebConfigMode;
  @Getter @Setter private String sebConfigUploadId;
          @Setter private String sebConfigFileName;
  @Getter @Setter private String sebConfigKey;
  @Getter @Setter private String sebExamKeys;
  @Getter @Setter private Boolean sebAllowUserQuitSeb;
  @Getter @Setter private Boolean sebShowTaskbar;
  @Getter @Setter private Boolean sebShowTime;
  @Getter @Setter private Boolean sebShowKeyboardLayout;
  @Getter @Setter private Boolean sebShowWifiControl;
  @Getter @Setter private Boolean sebAllowAudioControl;
  @Getter @Setter private Boolean sebAllowSpellChecking;

  @Getter @Setter private String currentSiteId;

  // properties of PublishedFeedback
  private String feedbackDelivery; // immediate, on specific date , no feedback
  private String feedbackComponentOption; // 2 = select options, 1 = total scores only 
  private String feedbackAuthoring;
  private boolean showQuestionText = false;
  private boolean showStudentResponse = false;
  private boolean showCorrectResponse = false;
  private boolean showStudentScore = false;
  private boolean showStudentQuestionScore = false;
  private boolean showQuestionLevelFeedback = false;
  private boolean showSelectionLevelFeedback = false; // must be MC
  private boolean showGraderComments = false;
  private boolean showStatistics = false;
  private boolean showCorrection = true;
  
  // properties of PublishedEvaluationModel
  private boolean anonymousGrading;
  @Getter @Setter private String toDefaultGradebook;
  @Getter @Setter private String gradebookName;
  
  @Setter private List<SelectItem> existingGradebook = new ArrayList<>();
  @Setter @Getter private boolean gradebookEnabled;


  private String scoringType;
  private String bgColor;
  private String bgImage;
  private HashMap values = new HashMap();

  // extra properties
  @Getter @Setter private String publishedUrl;
  @Getter @Setter private String alias;

  @Getter @Setter private String outcome;
  
  private boolean isValidStartDate = true;
  private boolean isValidDueDate = true;
  private boolean isValidRetractDate = true;
  private boolean isValidFeedbackDate = true;
  private boolean isValidFeedbackEndDate = true;
  
  private String originalStartDateString;
  private String originalDueDateString;
  private String originalRetractDateString;
  private String originalFeedbackDateString;
  @Getter @Setter private String originalFeedbackEndDateString;
  private boolean updateMostCurrentSubmission = false;
  
  private boolean isMarkForReview;
  private boolean honorPledge;
  private List attachmentList;
  @Setter private boolean editPubAnonyGradingRestricted = false;
  private String releaseToGroupsAsString;
  private String blockDivs;

  private boolean categoriesEnabled;
  private List<SelectItem> categoriesSelectList;
  private String categorySelected;
  
  private String bgColorSelect;
  private String bgImageSelect;
  
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
  private final String HIDDEN_FEEDBACK_END_DATE_FIELD = "feedbackEndDateISO8601";

  private final String SEB_CONFIG_MODE_BUNDLE_PREFIX = "seb_config_mode_";
  private final String YES_BUNDLE_STRING = "assessment_is_timed";
  private final String NO_BUNDLE_STRING = "assessment_not_timed";

  private static final ResourceLoader assessmentSettingMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");

  @Autowired
  @Qualifier("org.sakaiproject.grading.api.GradingService")
  private org.sakaiproject.grading.api.GradingService gradingService;

  @Autowired
  @Qualifier("org.sakaiproject.tool.api.SessionManager")
  private SessionManager sessionManager;

  @Autowired
  @Qualifier("org.sakaiproject.tool.api.ToolManager")
  private ToolManager toolManager;

  @Autowired
  @Qualifier("org.sakaiproject.util.api.FormattedText")
  private FormattedText formattedText;

  @Autowired
  @Qualifier("org.sakaiproject.time.api.UserTimeService")
  private UserTimeService userTimeService;
  
  private static final String SAMIGO_SETTINGS_BACKGROUNDCOLOR_ENABLED = "samigo.settings.backgroundcolor.enabled";
  @Autowired
  @Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
  private ServerConfigurationService serverConfigurationService;
  @Setter @Getter private boolean backgroundColorEnabled = serverConfigurationService.getBoolean(SAMIGO_SETTINGS_BACKGROUNDCOLOR_ENABLED, false);

  @Setter private boolean gradebookGroupEnabled = getGradebookGroupEnabled();

    /*
   * Creates a new AssessmentBean object.
   */
  public PublishedAssessmentSettingsBean() {
  }

    public void setAssessment(PublishedAssessmentFacade assessment) {
    try {
      // Clear cached gradebook items when loading a new assessment
      this.existingGradebook = null;

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
      this.trackQuestions = Boolean.valueOf(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.TRACK_QUESTIONS));

      if((assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.BGIMAGE)!=null )
    		  && (!assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.BGIMAGE).equals(""))){
    	  this.bgImageSelect="1";
    	  this.bgColorSelect=null;
      }
      else{
    	  this.bgImageSelect=null;
    	  this.bgColorSelect="1";
	   }
	   ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
		this.extendedTimes = extendedTimeFacade.getEntriesForPub(this.assessment.getData());

      resetExtendedTime();

      SebConfig sebConfig = SebConfig.of(assessment.getAssessmentMetaDataMap());
      if (sebConfig.getConfigMode() != null) {
        this.setSebConfigMode(sebConfig.getConfigMode().toString());
        this.setSebConfigKey(sebConfig.getConfigKey());
        this.setSebExamKeys(StringUtils.join(sebConfig.getExamKeys(), "\n"));
        this.setSebAllowUserQuitSeb(sebConfig.getAllowUserQuitSeb());
        this.setSebShowTaskbar(sebConfig.getShowTaskbar());
        this.setSebShowTime(sebConfig.getShowTime());
        this.setSebShowKeyboardLayout(sebConfig.getShowKeyboardLayout());
        this.setSebShowWifiControl(sebConfig.getShowWifiControl());
        this.setSebAllowAudioControl(sebConfig.getAllowAudioControl());
        this.setSebConfigUploadId(sebConfig.getConfigUploadId());
        this.setSebAllowSpellChecking(sebConfig.getAllowSpellChecking());
      }

      setDisplayFormat(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec"));
      resetIsValidDate();
      resetOriginalDateString();
      
      // these are properties in AssessmentAccessControl
      AssessmentAccessControlIfc accessControl;
      accessControl = assessment.getAssessmentAccessControl();
      if (accessControl != null) {
        this.startDate = accessControl.getStartDate();
        this.dueDate = accessControl.getDueDate();
        this.retractDate = accessControl.getRetractDate();
        this.feedbackDate = accessControl.getFeedbackDate();
        this.feedbackEndDate = accessControl.getFeedbackEndDate();
        this.feedbackScoreThreshold = accessControl.getFeedbackScoreThreshold() != null ? String.valueOf(accessControl.getFeedbackScoreThreshold()) : StringUtils.EMPTY;
        this.feedbackScoreThresholdEnabled = StringUtils.isNotBlank(this.feedbackScoreThreshold);

        // deal with releaseTo
        this.releaseTo = accessControl.getReleaseTo(); // list of String
        this.publishingTargets = getPublishingTargets();
        this.targetSelected = getTargetSelected(releaseTo);
        this.firstTargetSelected = getFirstTargetSelected(releaseTo);

        this.timeLimit = accessControl.getTimeLimit(); // in seconds
        if (timeLimit !=null && timeLimit>0)
          setTimeLimitDisplay(timeLimit);
        else 
            resetTimeLimitDisplay();
        if (( Integer.valueOf(1)).equals(accessControl.getTimedAssessment()))
          this.timedAssessment = true;
        this.autoSubmit = (Integer.valueOf(1)).equals(accessControl.getAutoSubmit());
        if (accessControl.getAssessmentFormat()!=null)
          this.assessmentFormat = accessControl.getAssessmentFormat().toString(); // question/part/assessment on separate page
        if (accessControl.getItemNavigation()!=null)
          this.itemNavigation = accessControl.getItemNavigation().toString(); // linear or random
        if (accessControl.getItemNumbering()!=null)
          this.itemNumbering = accessControl.getItemNumbering().toString();
        if(accessControl.getDisplayScoreDuringAssessments()!=null)
        	this.displayScoreDuringAssessments=accessControl.getDisplayScoreDuringAssessments().toString();
        if (accessControl.getSubmissionsSaved()!=null)
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
      this.showCorrection = (Boolean.TRUE).equals(feedback.getShowCorrection());
      }

      // properties of EvaluationModel
      EvaluationModelIfc evaluation = assessment.getEvaluationModel();
      if (evaluation != null) {
        if (evaluation.getAnonymousGrading()!=null)
          this.anonymousGrading = evaluation.getAnonymousGrading().toString().equals("1");

        this.toDefaultGradebook = evaluation.getToGradeBook() != null ? evaluation.getToGradeBook() : EvaluationModelIfc.NOT_TO_GRADEBOOK.toString();
        this.gradebookName = EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(this.toDefaultGradebook) ? assessment.getAssessmentToGradebookNameMetaData() : "";

        if (evaluation.getScoringType()!=null)
          this.scoringType = evaluation.getScoringType().toString();
        
        this.currentSiteId = AgentFacade.getCurrentSiteId();

        this.categoriesEnabled = populateCategoryEnabled();
        this.categoriesSelectList = populateCategoriesSelectList();

        this.gradebookEnabled = populateGradebookEnabled();

        if (this.gradebookGroupEnabled) {
          Object categoryListMetaData = assessment.getAssessmentMetaDataMap().get(AssessmentMetaDataIfc.CATEGORY_LIST);
          this.categorySelected = categoryListMetaData != null ? (String) categoryListMetaData : "-1";
        } else {
          this.categorySelected = initializeCategorySelected(assessment.getData().getCategoryId());
        }

        //this.categorySelected = getCategoryForAssessmentName(assessment.getTitle());

      }

      //set IPAddresses
      setIpAddresses(assessment);

      // publishedUrl
      this.publishedUrl = generatePublishedURL(assessment);

      // secure delivery
      SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI(); 
      this.secureDeliveryAvailable = secureDeliveryService.isSecureDeliveryAvaliable();
      this.secureDeliveryModuleSelections = getSecureDeliverModuleSelections();
      this.secureDeliveryModule = (String) values.get( SecureDeliveryServiceAPI.MODULE_KEY );
      this.secureDeliveryModuleExitPassword = secureDeliveryService.decryptPassword( this.secureDeliveryModule, 
    		  assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.EXITPWD_KEY ) );

      if ( secureDeliveryModule == null ) {
    	  this.secureDeliveryModule = SecureDeliveryServiceAPI.NONE_ID;
      }
      else if ( ! secureDeliveryService.isSecureDeliveryModuleAvailable( secureDeliveryModule ) ) {
    	  log.warn( "Published assessment " + this.assessmentId + " requires secure delivery module " + this.secureDeliveryModule + 
    	  			" but module is disabled or no longer installed. Secure delivery module will revert to NONE" );
    	  secureDeliveryModule = SecureDeliveryServiceAPI.NONE_ID;
      }
    }
    catch (RuntimeException ex) {
      log.warn(ex.getMessage());
    }
  }

  private String getCategoryForAssessmentName(String assessmentName) {
    Long categoryId = null;
    String decodedAssessmentName = TextFormat.convertFormattedTextToPlaintext(assessmentName);

    String gradebookUid = toolManager.getCurrentPlacement().getContext();

    List<Assignment> gbAssignments = gradingService.getAssignments(gradebookUid, gradebookUid, SortType.SORT_BY_NONE);
    for (Assignment assignment : gbAssignments) {
      if (StringUtils.equals(assessmentName, assignment.getName()) || StringUtils.equals(decodedAssessmentName, assignment.getName())) {
        categoryId = assignment.getCategoryId();
      }
    }
    String catSelected = "-1";
    if (categoryId != null) {
      String catId;
      for (SelectItem catIdAndName : categoriesSelectList) {
        catId = catIdAndName.getValue().toString();
        if (StringUtils.equals(catId, categoryId.toString())) {
          catSelected = catId;
        }
      }
    }
    return catSelected;
  }

  /**
   * Populate the categoriesSelectList property with a list of string names
   * of the categories in the gradebook
   */
  private List<SelectItem> populateCategoriesSelectList() {
    if (!this.gradebookGroupEnabled) {
      List<CategoryDefinition> categoryDefinitions;
      List<SelectItem> selectList = new ArrayList<>();

      String gradebookUid = toolManager.getCurrentPlacement().getContext();
      categoryDefinitions = gradingService.getCategoryDefinitions(gradebookUid, gradebookUid);

      selectList.add(new SelectItem("-1", assessmentSettingMessages.getString("gradebook_uncategorized"))); // -1 for a cat id means unassigned
      for (CategoryDefinition categoryDefinition: categoryDefinitions) {
          selectList.add(new SelectItem(categoryDefinition.getId().toString(), categoryDefinition.getName()));
      }

      return selectList;
  } else {
      return new ArrayList<>();
  }
}

  private boolean populateCategoryEnabled() {
    if (!this.gradebookGroupEnabled) {
      String gradebookUid = toolManager.getCurrentPlacement().getContext();

      GradebookInformation gbInfo = gradingService.getGradebookInformation(gradebookUid, gradebookUid);
      if (gbInfo != null) {
          return !Objects.equals(gbInfo.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY);
      } else {
          return false;
      }
    } else {
      List<Gradebook> gbList = gradingService.getGradebookGroupInstances(AgentFacade.getCurrentSiteId());

      for (Gradebook gb : gbList){
        GradebookInformation test = gradingService.getGradebookInformation(gb.getUid(), AgentFacade.getCurrentSiteId());
        if (!Objects.equals(test.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
          return true;
        }
      }

      return false;
    }
  }

  public boolean populateGradebookEnabled() {
    if (!this.gradebookGroupEnabled) {
      List gradebookItemList = getExistingGradebook();
      return (gradebookItemList != null && !gradebookItemList.isEmpty()) ? true : false;
    } else {
      return true;
    }
  }

  public void setCategoriesEnabled(boolean categoriesEnabled) {
    this.categoriesEnabled = categoriesEnabled;
  }

  public boolean getCategoriesEnabled() {
    return categoriesEnabled;
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
    return timedHours*3600
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

  public boolean getShowCorrection() {
    return showCorrection;
  }

  public void setShowCorrection(boolean showCorrection) {
    this.showCorrection = showCorrection;
  }

  public boolean getAnonymousGrading() {
    return this.anonymousGrading;
  }

  public void setAnonymousGrading(boolean anonymousGrading) {
    this.anonymousGrading = anonymousGrading;
  }

  public String getScoringType() {
    return this.scoringType;
  }

  public void setScoringType(String scoringType) {
    this.scoringType = scoringType;
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

  public boolean isHonorPledge() { return honorPledge; }

  public void setHonorPledge(boolean honorPledge) { this.honorPledge = honorPledge; }

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

  public void setValueMap(HashMap newMap) {
	  HashMap h = new HashMap();

	  for (Iterator it = newMap.entrySet().iterator(); it.hasNext();) {
		  Map.Entry entry = (Map.Entry) it.next();
		  String key = (String) entry.getKey();
		  Object o = entry.getValue();
		  if (!("ASSESSMENT_AUTHORS".equals(key))) {
			  h.put(key, o);
		  }
	  }
	  this.values = h;
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
	    this.timedHours = 0;
	    this.timedMinutes = 0;
	    this.timedSeconds = 0;
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

  public void setIpAddresses(PublishedAssessmentFacade assessment) {
      // ip addresses
      this.ipAddresses = "";
      Set ipAddressSet = assessment.getSecuredIPAddressSet();
      if (ipAddressSet != null){
    	  List<String> ipList = new ArrayList<>();
    	  Iterator iter = ipAddressSet.iterator();
    	  while (iter.hasNext()) {
    		  SecuredIPAddressIfc ip = (SecuredIPAddressIfc) iter.next();
    		  if (ip.getIpAddress()!=null)
    			  ipList.add(ip.getIpAddress());
    	  }
    	  Collections.sort(ipList);
    	  for (String ip : ipList) {
    		  this.ipAddresses += ip + "\n";
    	  }
      }
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
    if (date == null) return StringUtils.EMPTY;

    try {
      return tu.getDisplayDateTime(displayFormat, date);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return StringUtils.EMPTY;
  }

  public String getStartDateInClientTimezoneString() {
    if (!this.isValidStartDate) {
      return this.originalStartDateString;
    }
    else {
      return userTimeService.dateTimeFormat(startDate, assessmentSettingMessages.getLocale(), DateFormat.LONG);
    }
  }

  public String getStartDateString() {
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

  public String getDueDateInClientTimezoneString() {
    if (!this.isValidDueDate) {
      return this.originalDueDateString;
    }
    else {
      return userTimeService.dateTimeFormat(dueDate, assessmentSettingMessages.getLocale(), DateFormat.LONG);
    }
  }

  public String getDueDateString() {
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

  public String getFeedbackDateInClientTimezoneString() {
    if (!this.isValidFeedbackDate) {
      return this.originalFeedbackDateString;
    }
    else {
      return userTimeService.dateTimeFormat(feedbackDate, assessmentSettingMessages.getLocale(), DateFormat.LONG);
    }
  }

  public String getFeedbackDateString() {
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

  public String getFeedbackEndDateInClientTimezoneString() {
    if (!this.isValidFeedbackEndDate) {
      return this.originalFeedbackEndDateString;
    }
    else {
      return userTimeService.dateTimeFormat(feedbackEndDate, assessmentSettingMessages.getLocale(), DateFormat.LONG);
    }
  }

  public String getFeedbackEndDateString() {
    if (!this.isValidFeedbackEndDate) {
      return this.originalFeedbackEndDateString;
    }
    else {
      return getDisplayFormatFromDate(feedbackEndDate);
    }
  }

  public void setFeedbackEndDateString(String feedbackEndDateString) {
    if (StringUtils.isBlank(feedbackEndDateString)) {
      this.isValidFeedbackEndDate = true;
      this.feedbackEndDate = null;
    } else {

      Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam(HIDDEN_FEEDBACK_END_DATE_FIELD));

      if (tempDate != null) {
        this.isValidFeedbackEndDate = true;
        this.feedbackEndDate = tempDate;
      } else {
        log.error("setFeedbackEndDateString could not parse hidden date field {}.", ContextUtil.lookupParam(HIDDEN_FEEDBACK_DATE_FIELD));
        this.isValidFeedbackEndDate = false;
        this.originalFeedbackEndDateString = feedbackEndDateString;
      }
    }
  }

  public boolean getFeedbackScoreThresholdEnabled() {
    return feedbackScoreThresholdEnabled;
  }

  public SelectItem[] getPublishingTargets() {
    boolean hasGroups = getNumberOfGroupsForSite() > 0;

    if (hasGroups) {
      return new SelectItem[] {
        new SelectItem("Anonymous Users", assessmentSettingMessages.getString("anonymous_users")),
        new SelectItem(AgentFacade.getCurrentSiteName(), assessmentSettingMessages.getString("entire_site")),
        new SelectItem(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS, assessmentSettingMessages.getString("selected_groups"))
      };
    } else {
      return new SelectItem[] {
        new SelectItem("Anonymous Users", assessmentSettingMessages.getString("anonymous_users")),
        new SelectItem(AgentFacade.getCurrentSiteName(), assessmentSettingMessages.getString("entire_site"))
      };
    }
  }

  public String[] getTargetSelected(String releaseTo) {
    if (releaseTo != null) {
      this.targetSelected = new String[]{releaseTo};
    }
    return this.targetSelected;
  }


  public void setFirstTargetSelected(String firstTargetSelected) {
    this.firstTargetSelected = firstTargetSelected.trim();
    if (this.targetSelected == null || this.targetSelected.length == 0) {
      this.targetSelected = new String[]{this.firstTargetSelected};
    } else {
      this.targetSelected[0] = this.firstTargetSelected;
    }
  }

  public String getFirstTargetSelected(String releaseTo) {
    if (releaseTo != null) {
      // Get the available targets
      SelectItem[] publishingTargets = getPublishingTargets();
      boolean isValid = false;

      // Check if the provided value matches any of the publishing targets
      for (SelectItem target : publishingTargets) {
        if (target != null && releaseTo.equals(target.getValue())) {
          isValid = true;
          break;
        }
      }

      // If valid, set it; otherwise, default to the current site name as maybe the site name changed
      this.firstTargetSelected = isValid ? releaseTo.trim() : AgentFacade.getCurrentSiteName();

      // Update the targetSelected array
      this.targetSelected = new String[]{this.firstTargetSelected};
    }
    return this.firstTargetSelected;
  }

    public boolean getActive() {
		Date currentDate = new Date();
		return !((this.dueDate != null && currentDate.after(this.dueDate))
				|| (this.retractDate != null && currentDate
						.after(this.retractDate)));
	}

	public void setDisplayFormat(String displayDateFormat) {
		this.displayDateFormat = displayDateFormat;
		this.displayFormat = new SimpleDateFormat(displayDateFormat, new ResourceLoader().getLocale());
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

	public boolean getIsValidFeedbackEndDate() {
		return this.isValidFeedbackEndDate;
	}

	public void resetIsValidDate() {
		this.isValidStartDate = true;
		this.isValidDueDate = true;
		this.isValidRetractDate = true;
		this.isValidFeedbackDate = true;
		this.isValidFeedbackEndDate = true;
	}
	  
	public void resetOriginalDateString() {
		this.originalStartDateString = "";
		this.originalDueDateString = "";
		this.originalRetractDateString = "";
		this.originalFeedbackDateString = "";
		this.originalFeedbackEndDateString = "";
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
	 * Returns all groups for site
	 * @return
	 */
  public SelectItem[] getGroupsForSite(){
      SelectItem[] groupSelectItems = new SelectItem[0];
      // This TreeMap will sort the group names nicely in AlphaNumeric order
      SortedMap<String, SelectItem> sortedSelectItems = new TreeMap<>(new AlphaNumericComparator());
      try {
          Site site = SiteService.getSite(toolManager.getCurrentPlacement().getContext());
          Collection<Group> groups = site.getGroups();
          if (groups != null && groups.size() > 0) {
              for (Group group : groups) {
                  sortedSelectItems.put(group.getTitle(), new SelectItem(group.getId(), group.getTitle()));
              }

              groupSelectItems = sortedSelectItems.values().toArray(new SelectItem[0]);
          }
      } catch (IdUnusedException ex) {
          log.warn("No site found while attempting to get groups, {}", ex.toString());
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
	 */
	public int getNumberOfGroupsForSite() {
		int numGroups = 0;
		try {
			Site site = SiteService.getSite(toolManager.getCurrentPlacement().getContext());
			Collection<Group> groups = site.getGroups();
			if (groups != null) {
				numGroups = groups.size();
			}
		} catch (IdUnusedException ex) {
			// No site available
		}
		return numGroups;
	}

	/**
	 * The authorized groups
	 */
	private String[] groupsAuthorized;

	/**
	 * Returns the groups to which this assessment is released
     * @param publishedAssessmentId
	 * @return
	 */
	public String[] getGroupsAuthorized(String publishedAssessmentId) {
		groupsAuthorized = null;
		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance()
				.getAuthzQueriesFacade();
		String id;
		if (publishedAssessmentId != null) {
			id = publishedAssessmentId;
		}
		else {
			id = getAssessmentId().toString();
		}
		List authorizations = authz.getAuthorizationByFunctionAndQualifier(
				"TAKE_PUBLISHED_ASSESSMENT", id);
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
	
	public String[] getGroupsAuthorized() {
		return getGroupsAuthorized(null);
	}

	public boolean getIsMarkForReview() {
		return this.isMarkForReview;
	}

	public void setIsMarkForReview(boolean isMarkForReview) {
		this.isMarkForReview = isMarkForReview;
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
			ToolSession currentToolSession = sessionManager.getCurrentToolSession();
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, filePickerList);
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.redirect("sakai.filepicker.helper/tool");
		}
		catch(Exception e){
			log.error("fail to redirect to attachment page: " + e.getMessage());
		}
		return "editPublishedAssessmentSettings";
	}

	public void setAssessmentAttachment(){
		SaveAssessmentAttachmentListener lis = new SaveAssessmentAttachmentListener(false);
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
				PublishedAssessmentService assessmentService = new PublishedAssessmentService();
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

    public boolean getEditPubAnonyGradingRestricted(){
	    return this.editPubAnonyGradingRestricted;
	}

	public void setReleaseToGroupsAsString(Map<String, String> releaseToGroupsMap){
		this.releaseToGroupsAsString = releaseToGroupsMap.values().stream()
			.map(Object::toString).collect(Collectors.joining(", "));
	}

	public String getReleaseToGroupsAsString() {
		return releaseToGroupsAsString;
	}

	public String getReleaseToGroupsAsHtml() {
		return formattedText.escapeHtml(releaseToGroupsAsString,false);
	}

	public void setBlockDivs(String blockDivs){
		this.blockDivs = blockDivs;
	}

	public String getBlockDivs() {
		return blockDivs;
	}

      /**
     * Returns the saved category id if it's there. Otherwise returns
     * "-1". This is needed to choose which select item is selected
     * when the authorSettings page loads.
     * @param categoryId
     * @return
     */
    private String initializeCategorySelected(Long categoryId) {
      if (!this.gradebookGroupEnabled) {
        String catSelected = "-1";
        if (categoryId != null) {
            String catId;
            for (SelectItem catIdAndName : categoriesSelectList) {
                catId = catIdAndName.getValue().toString();
                if (catId.equals(categoryId.toString())) {
                    catSelected = catId;
                }
            }
        }
        return catSelected;
      } else {
        return categoryId != null ? categoryId.toString() : "-1";
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

	public String getSebConfigFileName() {
		if (this.sebConfigUploadId != null && StringUtils.startsWith(this.sebConfigUploadId, "/")) {
			return StringUtils.substring(this.sebConfigUploadId, StringUtils.lastIndexOf(this.sebConfigUploadId, "/") + 1);
		} else {
			return null;
		}
	}

	public SelectItem[] getSecureDeliverModuleSelections() {
		
		SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI(); 
		Set<RegisteredSecureDeliveryModuleIfc> modules = secureDeliveryService.getSecureDeliveryModules( new ResourceLoader().getLocale() );
 		  
		List<SelectItem> selections = new ArrayList<>();
		for ( RegisteredSecureDeliveryModuleIfc module : modules ) {
			if (!SecureDeliveryServiceAPI.NONE_ID.equals(module.getId()) && !module.isEnabled()) continue;
 
			selections.add(new SelectItem( module.getId(), module.getName() ));
		}
 		  
		return selections.toArray(new SelectItem[selections.size()]);
	}

	// Create SelectItem array by mapping ConfigMode enum to bundle strings and in values
	public SelectItem[] getSebConfigModeSelections() {
		SebConfig.ConfigMode[] configModes = SebConfig.ConfigMode.values();

		return Arrays.stream(configModes).map(configMode -> {
			String configModeString = assessmentSettingMessages.getString(SEB_CONFIG_MODE_BUNDLE_PREFIX + StringUtils.lowerCase(configMode.toString()));

			return new SelectItem(configMode.toString(), configModeString);
		}).collect(Collectors.toList()).toArray(new SelectItem[configModes.length]);
	}

	public SelectItem[] getBooleanSelections() {
		SelectItem[] selectItemArray = {
			new SelectItem(true, assessmentSettingMessages.getString(YES_BUNDLE_STRING)),
			new SelectItem(false, assessmentSettingMessages.getString(NO_BUNDLE_STRING))
		};

		return selectItemArray;
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
			site = SiteService.getSite(toolManager.getCurrentPlacement().getContext());
			SectionAwareness sectionAwareness = PersistenceService.getInstance().getSectionAwareness();
			List enrollments = sectionAwareness.getSiteMembersInRole(site.getId(), Role.STUDENT);
			Map<String, String> studentTargets = new HashMap<>();
			Map<String, String> orderedStudents = new HashMap<>();

			// Add students to target set
			if (enrollments != null && enrollments.size() > 0) {
				for (Iterator iter = enrollments.iterator(); iter.hasNext();) {
					EnrollmentRecord enrollmentRecord = (EnrollmentRecord) iter.next();
					String userId = enrollmentRecord.getUser().getUserUid();
					String userDisplayName = enrollmentRecord.getUser().getSortName() + " (" + enrollmentRecord.getUser().getDisplayId() + ")";
					studentTargets.put(userId, userDisplayName);
				}
			}

			// Order students map
			orderedStudents = ContextUtil.sortByValue(studentTargets);

			// Add in students to select item list
			int listSize = 1 + orderedStudents.size();
			usersInSite = new SelectItem[listSize];
			usersInSite[0] = new SelectItem("", assessmentSettingMessages.getString("extendedTime_select_User"));
			int selectCount = 1;
			for (Map.Entry<String,String> student : orderedStudents.entrySet()) {
				usersInSite[selectCount++] = new SelectItem(student.getKey(), student.getValue());
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

  public Date getExtendedTimeStart() {
    return this.extendedTime.getStartDate();
  }

  public void setExtendedTimeStartString(String exTimeStartString) {
    Date tempDate = tu.parseISO8601String(ContextUtil.lookupParam("newEntry-start_date-iso8601"));
    if(tempDate != null) {
      this.extendedTime.setStartDate(tempDate);
    }
  }

  public Date getExtendedTimeDue() {
    return this.extendedTime.getDueDate();
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

  public Date getExtendedTimeRetract() {
    return this.extendedTime.getRetractDate();
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

  //Internal to be able to supress error easier
  public void addExtendedTime() {
      ExtendedTime entry = this.extendedTime;
      FacesContext context = FacesContext.getCurrentInstance();
      if (new ExtendedTimeValidator().validateEntry(entry, context, this)) {
          AssessmentAccessControlIfc accessControl = new AssessmentAccessControl();
          accessControl.setStartDate(this.startDate);
          accessControl.setDueDate(this.dueDate);
          accessControl.setLateHandling(Integer.valueOf(this.lateHandling));
          accessControl.setRetractDate(this.retractDate);
          this.extendedTime.syncDates(accessControl);
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

        public String getDisplayScoreDuringAssessments(){
 		return displayScoreDuringAssessments;
 	}
  
 	public void setDisplayScoreDuringAssessments(String displayScoreDuringAssessments){
 		this.displayScoreDuringAssessments = displayScoreDuringAssessments;
 	}

  public List getCategoriesSelectList() {
    return categoriesSelectList;
  }

  public void setCategoriesSelectList(List<SelectItem> categoriesSelectList) {
    this.categoriesSelectList = categoriesSelectList;
  }

  public String getCategorySelected() {
    return categorySelected;
  }

  public void setCategorySelected(String categorySelected) {
    this.categorySelected = categorySelected;
  }

  public String generatePublishedURL(PublishedAssessmentFacade paf) {
      FacesContext context = FacesContext.getCurrentInstance();
      ExternalContext extContext = context.getExternalContext();

      // get the alias to the pub assessment
      this.alias = paf.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
      String server = ((javax.servlet.http.HttpServletRequest) extContext.getRequest()).getRequestURL().toString();
      int index = server.indexOf(extContext.getRequestContextPath() + "/"); // "/samigo-app/"
      server = server.substring(0, index);
      String url = server + extContext.getRequestContextPath();
      return url + "/servlet/Login?id=" + this.alias;
  }

    public List<SelectItem> getExistingGradebook() {
        if (existingGradebook == null || existingGradebook.isEmpty()) {
            existingGradebook = populateExistingGradebookItems();
        }
        return existingGradebook;
    }

    // This method builds the gradebook assignment selector in the assessment settings.
    private List<SelectItem> populateExistingGradebookItems() {
      if (!this.gradebookGroupEnabled) {
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        List<SelectItem> target = new ArrayList<>();
        try {
            HashMap<String, String> gAssignmentIdTitles = new HashMap<>();
            HashMap<String, String> gradebookAssignmentsLabel = new HashMap<>();
            AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");

            for (Object assessment : author.getAllAssessments()) {
                if (assessment instanceof AssessmentFacade) {
                    AssessmentFacade assessmentFacade = (AssessmentFacade) assessment;
                    String gradebookItemId = assessmentFacade.getAssessmentToGradebookNameMetaData();
                    if (StringUtils.isNotBlank(gradebookItemId)) {
                        String associatedAssignmentTitles = "";
                        if (gAssignmentIdTitles.containsKey(gradebookItemId)) {
                            // get the current associated assignment titles first
                            associatedAssignmentTitles = gAssignmentIdTitles.get(gradebookItemId) + ", ";
                        }

                        // append the current assignment title
                        associatedAssignmentTitles += assessmentFacade.getTitle();

                        // put the current associated assignment titles back
                        gAssignmentIdTitles.put(gradebookItemId, associatedAssignmentTitles);
                    }
                }

                if (assessment instanceof PublishedAssessmentFacade) {
                    PublishedAssessmentFacade pubAssessmentFacade = (PublishedAssessmentFacade) assessment;
                    AssessmentIfc publishedAssessment = publishedAssessmentService.getAssessment(pubAssessmentFacade.getPublishedAssessmentId());
                    String gradebookItemId = publishedAssessment.getAssessmentToGradebookNameMetaData();
                    if (StringUtils.isNotBlank(gradebookItemId)) {
                        String associatedAssignmentTitles = "";
                        if (gAssignmentIdTitles.containsKey(gradebookItemId)) {
                            // get the current associated assignment titles first
                            associatedAssignmentTitles = gAssignmentIdTitles.get(gradebookItemId) + ", ";
                        }

                        // append the current assignment title
                        associatedAssignmentTitles += pubAssessmentFacade.getTitle();

                        // put the current associated assignment titles back
                        gAssignmentIdTitles.put(gradebookItemId, associatedAssignmentTitles);
                    }
                }
            }

            List<Assignment> gradebookAssignmentList = gradingService.getAssignments(AgentFacade.getCurrentSiteId(), AgentFacade.getCurrentSiteId(), SortType.SORT_BY_NONE);
            for (Assignment gradebookAssignment : gradebookAssignmentList) {
                if (!gradebookAssignment.getExternallyMaintained()) {
                    // If the gradebook item is external use the externalId, otherwise use the gradebook item id.
                    String gradebookItemId = String.valueOf(gradebookAssignment.getId());
                    // The label is just the gradebook assignment name.
                    String label = gradebookAssignment.getName();
                    if (gAssignmentIdTitles.get(gradebookItemId) != null) {
                        label += " ( " + assessmentSettingMessages.getFormattedMessage("usedGradebookAssessment", new Object[]{gAssignmentIdTitles.get(gradebookItemId)}) + " )";
                    }
                    gradebookAssignmentsLabel.put(gradebookItemId, label);
                }
            }

            for (String labelKey : gradebookAssignmentsLabel.keySet()) {
                target.add(new SelectItem(labelKey, gradebookAssignmentsLabel.get(labelKey)));
            }
        } catch (Exception gnfe1) {
            log.error("Severe error getting gradebook items {}.", gnfe1.getMessage());
            return Arrays.asList(new SelectItem());
        }

        return target;

      } else {
        return new ArrayList<SelectItem>();
      }
    }

    public boolean getGradebookGroupEnabled() {
      this.gradebookGroupEnabled = gradingService.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());
      return this.gradebookGroupEnabled;
    }

}
