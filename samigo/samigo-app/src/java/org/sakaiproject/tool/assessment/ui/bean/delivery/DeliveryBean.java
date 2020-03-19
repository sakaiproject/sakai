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
package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSecuredIPAddress;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingAttachment;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.DataException;
import org.sakaiproject.tool.assessment.services.FinFormatException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.SaLengthException;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.Phase;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI.PhaseStatus;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.tool.assessment.util.MimeTypesLocator;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/* For delivery: Delivery backing bean */
@Slf4j
@ManagedBean(name="delivery")
@SessionScoped
public class DeliveryBean implements Serializable {

  //SAM-2517
  private UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
  
  private static final String MATHJAX_SRC_PATH_SAKAI_PROP = "portal.mathjax.src.path";
  private static final String MATHJAX_SRC_PATH = ServerConfigurationService.getString(MATHJAX_SRC_PATH_SAKAI_PROP);

  @Getter @Setter
  private String assessmentId;
  @Getter @Setter
  private String assessmentTitle;
  @Getter @Setter
  private boolean honorPledge;
  @Getter @Setter
  private List markedForReview;
  @Getter @Setter
  private List blankItems;
  @Getter @Setter
  private List markedForReviewIdents;
  @Getter @Setter
  private List blankItemIdents;
  @Getter @Setter
  private boolean reviewMarked;
  @Getter @Setter
  private boolean reviewAll;
  @Getter @Setter
  private boolean reviewBlank;
  @Getter @Setter
  private boolean displayMardForReview;
  @Getter @Setter
  private int itemIndex;
  @Getter @Setter
  private int size;
  @Getter @Setter
  private String action;
  @Getter @Setter
  private Date beginTime;
  @Getter @Setter
  private String endTime;
  @Getter @Setter
  private String currentTime;
  @Getter @Setter
  private String multipleAttempts;
  @Getter @Setter
  private String timeOutSubmission;
  @Getter @Setter
  private String submissionTicket;
  @Getter
  private String timeElapse;
  @Getter @Setter
  private int sectionIndex;
  @Getter @Setter
  private boolean previous;
  @Getter @Setter
  private String duration;
  // this is the PublishedAccessControl.finalPageUrl
  @Getter @Setter
  private String url;
  @Getter @Setter
  private String confirmation;
  @Getter @Setter
  private String outcome;
  @Getter @Setter
  private String receiptEmailSetting;
  @Getter @Setter
  private Map<String, MediaData> submissionFiles = new HashMap<>();

  //Settings
  @Setter
  private String questionLayout;
  @Getter @Setter
  private String navigation;
  @Getter @Setter
  private String numbering;
  @Getter @Setter
  private String feedback;
  @Getter @Setter
  private String noFeedback;
  @Getter @Setter
  private String statistics;
  @Getter @Setter
  private String creatorName;
  @Getter @Setter
  private FeedbackComponent feedbackComponent;
  @Getter @Setter
  private String feedbackComponentOption;
  @Getter @Setter
  private boolean feedbackOnDate;
  @Getter @Setter
  private String errorMessage;
  @Setter
  private SettingsDeliveryBean settings;
  @Getter @Setter
  private Date dueDate;
  @Getter @Setter
  private Date adjustedTimedAssesmentDueDate;
  @Setter
  private Date retractDate;
  @Getter @Setter
  private boolean statsAvailable;
  @Getter @Setter
  private boolean submitted;
  // True if the assessment was completely submitted
  @Getter @Setter
  private boolean assessmentSubmitted = false;
  @Getter @Setter
  private boolean graded;
  @Setter
  private String graderComment;
  @Getter @Setter
  private List<AssessmentGradingAttachment> assessmentGradingAttachmentList;
  @Getter @Setter
  private boolean hasAssessmentGradingAttachment;
  @Getter @Setter
  private String rawScore;
  @Getter @Setter
  private String grade;
  @Getter @Setter
  private Date submissionDate;
  @Getter @Setter
  private Date submissionTime;
  @Getter @Setter
  private String image;
  @Getter @Setter
  private boolean hasImage;
  @Getter @Setter
  private String instructorMessage;
  @Getter @Setter
  private String courseName;
  @Getter @Setter
  private String timeLimit;
  @Getter @Setter
  private int timeLimit_hour;
  @Getter @Setter
  private int timeLimit_minute;
  @Getter @Setter
  private String timeLimitString;
  /**
   * Bean with table of contents information and
   * a list of all the sections in the assessment
   * which in  turn has a list of all the item contents.
   */
  @Getter @Setter
  private ContentsDeliveryBean tableOfContents;
  @Getter @Setter
  private String submissionId;
  @Getter @Setter
  private String submissionMessage;
  @Getter @Setter
  private String instructorName;
  /**
   * Bean with a list of all the sections in the current page
   * which in turn has a list of all the item contents for the page.
   *
   * This is like the table of contents, but the selections are restricted to
   * that on one page.
   *
   *  Since these properties are on a page delivery basis--if:
   *  1. there is only one item per page the list of items will
   *  contain one item and the list of parts will return one part, or if--
   *
   *  2. there is one part per page the list of items will be that
   *  for that part only and there will only be one part, however--
   *
   *  3. if it is all parts and items on a single page there
   *  will be a list of all parts and items.
   *
   */
  @Getter @Setter
  private ContentsDeliveryBean pageContents;
  @Setter
  private int submissionsRemaining;
  @Getter @Setter
  private int totalSubmissions;
  @Getter @Setter
  private boolean forGrade;

  @Getter @Setter
  private String password;
  @Getter @Setter
  private int numberRetake;
  private boolean lastSave;
  @Getter @Setter
  private int actualNumberRetake;
  @Getter @Setter
  private Map itemContentsMap;

  @Getter @Setter
  private String minutesLeft;
  @Getter @Setter
  private String secondsLeft;
  
  // For paging
  @Getter @Setter
  private int partIndex;
  @Getter @Setter
  private int questionIndex;
  private boolean next_page;
  @Getter @Setter
  private boolean reload = true;

  // daisy added these for SelectActionListener
  @Getter @Setter
  private boolean notTakeable = true;
  @Getter @Setter
  private boolean pastDue;
  @Getter @Setter
  private long subTime;
  @Getter @Setter
  private long raw;
  @Getter @Setter
  private String takenHours;
  @Getter @Setter
  private String takenMinutes;
  private AssessmentGradingData adata;
  @Getter
  private PublishedAssessmentFacade publishedAssessment;
  @Getter @Setter
  private Date feedbackDate;
  @Getter @Setter
  private Date feedbackEndDate;
  @Getter @Setter
  private String feedbackDelivery;
  @Getter @Setter
  private String showScore;
  @Getter @Setter
  private boolean hasTimeLimit;
  private boolean isMoreThanOneQuestion;
  @Getter @Setter
  private Integer scoringType;
  
  // daisyf added for servlet Login.java, to support anonymous login with publishedUrl
  @Getter @Setter
  private boolean anonymousLogin = false;
  @Getter @Setter
  private String contextPath;

  // SAK-6990 daisyf added this for timed assessment, to check if mutiple windows were open during timed assessment
  @Getter @Setter
  private String timerId=null;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -1090852048737428722L;
  @Getter @Setter
  private boolean showStudentScore;
  @Getter @Setter
  private boolean showStudentQuestionScore;

  // SAM-387 esmiley added to track if timer has been started in timed assessments
  @Getter @Setter
  private boolean timeRunning;
  // SAM-535 esmiley added to track JavaScript
  @Getter @Setter
  private String javaScriptEnabledCheck;

  //cwent
  @Setter
  private String siteId;

  @Getter @Setter
  private boolean beginAssessment;

  // this instance tracks if the Agent is taking a test via URL, as well as
  // current agent string (if assigned). SAK-1927: esmiley
  private AgentFacade deliveryAgent;

  @Getter @Setter
  private boolean noQuestions = false;

  // this assessmentGradingId is used to generate seed in getSeed(...) of DeliveryActaionListener.java
  // We need this because the assessmentGradingData in delivery bean might not be the one we want to display
  // especially for review assessment and grade assessment. In other word, if student has started taking another assessment, 
  // the assessmentGradingData in deliver bean will be the newly created one. Then, of course, the assessmentGradingId 
  // will be the new id which is not what we want in review assessment or grade assessment
  @Getter @Setter
  private Long assessmentGradingId;

  @Getter @Setter
  private boolean fromTableOfContents;
  @Getter @Setter
  private int fileUploadSizeMax;

  @Getter @Setter
  private boolean isAnyInvalidFinInput;
  @Getter @Setter
  private String redrawAnchorName;
  
  // If set to true, delivery of the assessment is blocked.
  // This attribute is set to true if a secure delivery module is selected for the assessment and the security
  // check fails. If a module is selected, but is no longer installed or is disabled then the check is bypassed
  // and this attribute remains false.
  @Getter @Setter
  private boolean blockDelivery = false;
  
  // HTML fragment injected by the secure delivery module selected. Nothing is injected if no module has
  // been selected or if the selected module is no longer installed or disabled.
  @Getter @Setter
  private String secureDeliveryHTMLFragment;

  @Getter @Setter
  private boolean isFromPrint;
  
  private ExtendedTimeDeliveryService extendedTimeDeliveryService = null;

  @Getter @Setter
  private boolean showTimeWarning;
  @Getter @Setter
  private boolean showTimer = true;
  @Getter @Setter
  private boolean hasShowTimeWarning;
  @Getter @Setter
  private boolean turnIntoTimedAssessment;
  @Getter @Setter
  private boolean submitFromTimeoutPopup;
  @Getter @Setter
  private boolean skipFlag;
  @Getter
  private Date deadline;
  @Getter @Setter
  private boolean firstTimeTaking;
  @Setter
  private boolean timeExpired = false;
  @Getter @Setter
  private long lastTimer=0;

  // Rubrics
  @Getter @Setter
  private String rbcsToken;

  private static String ACCESSBASE = ServerConfigurationService.getAccessUrl();
  private static String RECPATH = ServerConfigurationService.getString("samigo.recommendations.path");

  private static ResourceBundle eventLogMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.EventLogMessages");

  private static String questionProgressUnansweredPath = ServerConfigurationService.getString("samigo.questionprogress.unansweredpath", "/images/whiteBubble15.png");
  private static String questionProgressAnsweredPath = ServerConfigurationService.getString("samigo.questionprogress.answeredpath", "/images/blackBubble15.png");
  private static String questionProgressMardPath = ServerConfigurationService.getString("samigo.questionprogress.mardpath", "/images/questionMarkBubble15.png");

  // delivery action
  public static final int TAKE_ASSESSMENT = 1;
  public static final int PREVIEW_ASSESSMENT = 2;
  public static final int REVIEW_ASSESSMENT = 3;
  public static final int GRADE_ASSESSMENT = 4;
  public static final int TAKE_ASSESSMENT_VIA_URL = 5;
  @Getter @Setter
  private int actionMode;
  @Getter
  private String actionString;

  @Getter
  private String timeElapseAfterFileUpload;
  @Getter @Setter
  private double timeElapseDouble=0;
  @Getter @Setter
  private double timeElapseAfterFileUploadDouble;
  @Getter @Setter
  private String protocol;
  @Getter @Setter
  private long timeStamp;
  @Setter
  private Map publishedItemHash = new HashMap();
  @Setter
  private Map publishedItemTextHash = new HashMap();
  @Setter
  private Map publishedAnswerHash = new HashMap();
  @Getter @Setter
  private List attachmentList;

  /**
   * Creates a new DeliveryBean object.
   */
  public DeliveryBean() {
    deliveryAgent = new AgentFacade();
  }

  public TimeZone getUserTimeZone() {
    return userTimeService.getLocalTimeZone();
  }

  public String getBeginTimeString() {
	    if (beginTime == null) {
	      return "";
	    }

      return userTimeService.dateTimeFormat(beginTime, new ResourceLoader().getLocale(), DateFormat.MEDIUM);
  }

  public String getCurrentTimeElapse() {
    syncTimeElapsedWithServer();
    return timeElapse;
  }

  public void setTimeElapse(String timeElapse) {
    try{
      if (timeElapse!=null && !("").equals(timeElapse)
          && getTimeLimit()!=null && !("").equals(getTimeLimit())){
        double limit = (new Double(getTimeLimit()));
        double elapsed = (new Double(timeElapse));
        if (limit > elapsed)
          this.timeElapse = timeElapse;
        else
          this.timeElapse = getTimeLimit();
        setTimeElapseDouble((new Double(timeElapse)));
      }
    } catch (Exception e){
      log.warn("setTimeElapse error:"+e.getMessage());
    }
  }

  /**
   *
   *
   * @return
   */
  public int getDisplayIndex() {
    return this.itemIndex + 1;
  }

  //Settings
  public String getQuestionLayout() {
      if(getSettings().isFormatByQuestion()) {
          questionLayout = "1";
      } else if (getSettings().isFormatByPart()) {
          questionLayout = "2";
      } else if (getSettings().isFormatByAssessment()) {
          questionLayout = "3";
      }

    return questionLayout;
  }

  /*
   * Types of feedback in FeedbackComponent:
   *
   * SHOW CORRECT SCORE
   * SHOW STUDENT SCORE
   * SHOW ITEM LEVEL
   * SHOW SECTION LEVEL
   * SHOW GRADER COMMENT
   * SHOW STATS
   * SHOW QUESTION
   * SHOW RESPONSE
   */

  /**
   * @return
   */
  public SettingsDeliveryBean getSettings() {
    // SAM-1438 - We occasionally see the settings bean as null during
    // submission, within a JSF phase of deliverAssessment.jsp but it is
    // generally not reproducible. This block protects against the bug
    // by loading up the settings for this assessment. They are not assigned
    // to the local settings variable as to avoid changing any more behavior
    // than is needed. This is effectively a failsafe and diagnostic that
    // should not really be necessary.
	  
    if (settings == null) {
      Session session = SessionManager.getCurrentSession();
      StringBuilder sb = new StringBuilder(400);
      sb.append("SAM-1438 - Delivery settings bean is null.\n");
      if (session != null) {
        sb.append("         - User EID  : ").append(session.getUserEid()).append("\n");
        sb.append("         - User ID   : ").append(session.getUserId()).append("\n");
        sb.append("         - Session ID: ").append(session.getId()).append("\n");
      } else {
        sb.append("         - Session is null. Cannot determine user.\n");
      }
      sb.append("         - Published Assessment ID: ");
      if (publishedAssessment == null) {
        sb.append("<null>\n");
      } else {
        sb.append(publishedAssessment.getPublishedAssessmentId()).append("\n");
        sb.append("         - Assessment Title       : ").append(publishedAssessment.getTitle()).append("\n");
        sb.append("         - Assessment Site ID     : ").append(publishedAssessment.getOwnerSiteId());
        BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
        //settings variable may be populated by populateBeanFromPub
        listener.populateBeanFromPub(this, publishedAssessment);
      }
      log.warn(sb.toString());
    }

    //If the settings is still null, need to return something
    if (settings == null) {
      log.warn("SAM-2410 - Delivery settings bean is still null, returning an empty bean");
      SettingsDeliveryBean tempSettings = new SettingsDeliveryBean();
      return tempSettings;
    }
	
    return settings;
  }

  public String getAdjustedTimedAssesmentDueDateString () {
    if (adjustedTimedAssesmentDueDate == null) {
      return "";
    }

    return userTimeService.dateTimeFormat(adjustedTimedAssesmentDueDate, new ResourceLoader().getLocale(), DateFormat.MEDIUM);
  }

  public Date getRetractDate() {
    return isAcceptLateSubmission() ? retractDate : null;
  }

  public String getGraderComment() {
    if (graderComment == null) {
      return "";
    }
    return graderComment;
  }

  public String getRoundedRawScore() {
   try {
      String newscore= ContextUtil.getRoundedValue(rawScore, 2);
      return Validator.check(newscore, "N/A");
    } catch (Exception e) {
      // encountered some weird number format/locale
      return Validator.check(rawScore, "0");
    }
  }
  
  public String getRoundedRawScoreViaURL() {
	  if (adata.getFinalScore() != null){
		  rawScore = adata.getFinalScore().toString();
	  } else {
		  rawScore = "0";
	  }

	  try {
		  String newscore= ContextUtil.getRoundedValue(rawScore, 2);
		  return Validator.check(newscore, "N/A");
	  } catch (Exception e) {
		  // encountered some weird number format/locale
		  return Validator.check(rawScore, "0");
	  }
  }

  public void syncSubmissionsRemaining() {
    if (adata != null) {
      Long publishedAssessmentId = adata.getPublishedAssessmentId();
      AssessmentAccessControlIfc control = publishedAssessment.getAssessmentAccessControl();
      PublishedAssessmentService service = new PublishedAssessmentService();
      GradingService gradingService = new GradingService();
      int totalSubmissions =
          (service.getTotalSubmission(
                  AgentFacade.getAgentString(), publishedAssessmentId.toString()))
              .intValue();
      setTotalSubmissions(totalSubmissions);
      if (!(Boolean.TRUE).equals(control.getUnlimitedSubmissions())) {
        int submissionsRemaining = control.getSubmissionsAllowed().intValue() - totalSubmissions;
        setNumberRetake(
            gradingService.getNumberRetake(publishedAssessmentId, AgentFacade.getAgentString()));
        setSubmissionsRemaining(submissionsRemaining);
      }
    }
  }

  public int getSubmissionsRemaining() {
    syncSubmissionsRemaining();
    return submissionsRemaining;
  }

  public String submitForGradeFromTimer() {
    return submitForGrade(true, false);
  }
  
  public String submitForGrade() {
    return submitForGrade(false, false);
  }
  
  public String submitFromTimeoutPopup() {
 	  return submitForGrade(false, true);
  }
   
  private String submitForGrade(boolean isFromTimer, boolean submitFromTimeoutPopup) {
	try{
	  Map<String, Object> notificationValues = new HashMap<>();
	  Long local_assessmentGradingID = adata.getAssessmentGradingId();
	  if (this.actionMode == PREVIEW_ASSESSMENT) {
		  return "editAssessment";
	  }
	  
	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_CLICKSUB, "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + adata.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));

	  if (!submitFromTimeoutPopup) {
		  String nextAction = checkBeforeProceed(true, isFromTimer);
		  log.debug("***** next Action={}", nextAction);
		  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_CHECKED, "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + adata.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));

		  if (!("safeToProceed").equals(nextAction)){
			  return nextAction;
		  }
	  }
	  setForGrade(true);
	  
	  if (submitFromTimeoutPopup) {
		  setSubmitFromTimeoutPopup(true);
	  } else {
		  setSubmitFromTimeoutPopup(false);
	  }
	  
	  SessionUtil.setSessionTimeout(FacesContext.getCurrentInstance(), this, false);

	  syncTimeElapsedWithServer();
	  
	  SubmitToGradingActionListener listener = new SubmitToGradingActionListener();
	  // submission remaining and totalSubmissionPerAssessmentHash is updated inside 
    // SubmitToGradingListener
    try {
      listener.processAction(null);
    } catch (FinFormatException | SaLengthException e) {
      log.debug(e.getMessage());
      return "takeAssessment";
    } catch (DataException e) {
      log.error(e.getMessage());
      return "discrepancyInData";
    }
	  
	  // We don't need to call completeItemGradingData to create new ItemGradingData for linear access
	  // because each ItemGradingData is created when it is viewed/answered 
	  if (!"1".equals(navigation)) {
		  GradingService gradingService = new GradingService();
		  gradingService.completeItemGradingData(adata);
	  }

	  String returnValue="submitAssessment";
	  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	  String siteId = publishedAssessmentService.getPublishedAssessmentOwner(adata.getPublishedAssessmentId());
	  String resource = "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + local_assessmentGradingID;

	  setAssessmentSubmitted(true);

	  if (!isFromTimer) {
		  if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) {// this is for accessing via published url		  
			  returnValue="anonymousThankYou";
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_VIA_URL, resource, siteId, true, NotificationService.NOTI_REQUIRED));
		  } else {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED, resource, true));
		  }
	  } else {
		  if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) {// this is for accessing via published url		  
			  returnValue="anonymousThankYou";
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_VIA_URL, resource , siteId, true, NotificationService.NOTI_REQUIRED));
		  } else {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER, resource, true));
		  }
	  }
	  forGrade = false;
	  SelectActionListener l2 = new SelectActionListener();
	  l2.processAction(null);
	  reload = true;

	  // finish within time limit, clean timedAssessment from queue
	  removeTimedAssessmentFromQueue();
	  
	  // finish secure delivery
	  setSecureDeliveryHTMLFragment( "" );
	  setBlockDelivery( false );
	  SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
	  if ( secureDelivery.isSecureDeliveryAvaliable() ) {
		  String moduleId = publishedAssessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY );
		  if ( moduleId != null && ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
			  
			  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			  PhaseStatus status = secureDelivery.validatePhase(moduleId, Phase.ASSESSMENT_FINISH, publishedAssessment, request );
			  	setSecureDeliveryHTMLFragment( 
			  			secureDelivery.getHTMLFragment(moduleId, publishedAssessment, request, Phase.ASSESSMENT_FINISH, status, new ResourceLoader().getLocale() ) );
		  }
	  }
	 
	  EventLogService eventService = new EventLogService();
 	  EventLogFacade eventLogFacade = new EventLogFacade();
 	 
 	  List eventLogDataList = eventService.getEventLogData(adata.getAssessmentGradingId());
	  if(eventLogDataList != null && eventLogDataList.size() > 0) {
	 	  EventLogData eventLogData= (EventLogData) eventLogDataList.get(0);
	 	  eventLogData.setErrorMsg(eventLogMessages.getString("no_error"));
	 	  Date endDate = new Date();
	 	  eventLogData.setEndDate(endDate);
	 	  if(eventLogData.getStartDate() != null) {
	 	      double minute= 1000*60;
	 	      int eclipseTime = (int)Math.ceil(((endDate.getTime() - eventLogData.getStartDate().getTime())/minute));
	 	      eventLogData.setEclipseTime(eclipseTime);
	 	  } else {
	 	      eventLogData.setEclipseTime(null);
	 	      eventLogData.setErrorMsg(eventLogMessages.getString("error_take"));
	 	  }

		  String thisIp = ( (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
		  eventLogData.setIpAddress(thisIp);

	 	  eventLogFacade.setData(eventLogData);
	 	  eventService.saveOrUpdateEventLog(eventLogFacade);
	  }
	  notificationValues.put("assessmentGradingID", local_assessmentGradingID);
	  notificationValues.put("userID", adata.getAgentId());
	  notificationValues.put("submissionDate", getSubmissionDate());
	  notificationValues.put("publishedAssessmentID", adata.getPublishedAssessmentId());
	  notificationValues.put("confirmationNumber", confirmation);
	  
      String [] releaseToGroups = getReleaseToGroups();
      if (releaseToGroups != null){
          notificationValues.put("releaseToGroups", Arrays.stream(releaseToGroups).collect(Collectors.joining(";")));
      }

	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_NOTI, notificationValues.toString(), AgentFacade.getCurrentSiteId(), true, SamigoConstants.NOTI_EVENT_ASSESSMENT_SUBMITTED));
 
	  return returnValue;
	  } catch(Exception e) {
		  EventLogService eventService = new EventLogService();
		  EventLogFacade eventLogFacade = new EventLogFacade();
		  EventLogData eventLogData;

		  List eventLogDataList = eventService.getEventLogData(adata.getAssessmentGradingId());
		  if(eventLogDataList != null && eventLogDataList.size() > 0) {
			  eventLogData= (EventLogData) eventLogDataList.get(0);
			  eventLogData.setErrorMsg(eventLogMessages.getString("error_submit"));
			  eventLogData.setEndDate(new Date());
			  			  
			  String thisIp = ( (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
			  eventLogData.setIpAddress(thisIp);
		  	  			  
			  eventLogFacade.setData(eventLogData);
			  eventService.saveOrUpdateEventLog(eventLogFacade);
		  }

		  return null;
	 }
  }
  
  private String[] getReleaseToGroups(){      
      String [] releaseToGroups = null;
      
      if (getPublishedAssessment().getAssessmentAccessControl().getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
          PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
          releaseToGroups = publishedAssessmentSettings.getGroupsAuthorized(adata.getPublishedAssessmentId().toString());
      }
      
      return releaseToGroups;
  }

  public String confirmSubmit()
  {	  
	  if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
		  if (adata != null) {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_FROM_LASTPAGE, "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + adata.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));
		  } else {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_FROM_LASTPAGE, "siteId=" + AgentFacade.getCurrentSiteId() + ", adata is null", siteId, true, NotificationService.NOTI_REQUIRED));
		  }
	  }
	  
	  String nextAction = checkBeforeProceed();
	  log.debug("***** next Action={}", nextAction);
	  if (!("safeToProceed").equals(nextAction)){
		  return nextAction;
	  }

	  setForGrade(false);
	  
	  if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
		  syncTimeElapsedWithServer();
		  SubmitToGradingActionListener listener = new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      } catch (FinFormatException | SaLengthException e) {
        log.debug(e.getMessage());
        return "takeAssessment";
      } catch (DataException e) {
        log.error(e.getMessage());
        return "discrepancyInData";
      }
	  }

	  skipFlag = true;
	  DeliveryActionListener l2 = new DeliveryActionListener();
	  l2.processAction(null);
	  return "confirmsubmit";
  }
  
  public String confirmSubmitTOC() {
	  if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
		  if (adata != null) {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_FROM_TOC, "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + adata.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));
		  } else {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_FROM_TOC, "siteId=" + AgentFacade.getCurrentSiteId() + ", adata is null", siteId, true, NotificationService.NOTI_REQUIRED));
		  }
	  }
	  
	  String nextAction = checkBeforeProceed();
	  log.debug("***** next Action={}", nextAction);
	  if (!("safeToProceed").equals(nextAction)){
		  return nextAction;
	  }

	  setForGrade(false);
	  
	  if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
		  syncTimeElapsedWithServer();
		  SubmitToGradingActionListener listener = new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      } catch (FinFormatException | SaLengthException e) {
        log.debug(e.getMessage());
        return "takeAssessment";
      } catch (DataException e) {
        log.error(e.getMessage());
        return "discrepancyInData";
      }
	  }
	  
	  setFromTableOfContents(true);
	  DeliveryActionListener l2 = new DeliveryActionListener();
	  l2.processAction(null);

	  setContinue(false);
	  return "confirmsubmit";
  }

  public String saveAndExit() {
	  return saveAndExit(true);
  }
  
  public String saveAndExit(boolean needToCheck) {
	  if (needToCheck) {
		  String nextAction = checkBeforeProceed();
		  log.debug("***** next Action={}", nextAction);
		  if (!("safeToProceed").equals(nextAction)){
			  return nextAction;
		  }
	  }

    FacesContext context = FacesContext.getCurrentInstance();
    SessionUtil.setSessionTimeout(context, this, false);
    log.debug("***DeliverBean.saveAndEXit face context ={}", context);

    forGrade = false;
    if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
    	syncTimeElapsedWithServer();
    	SubmitToGradingActionListener listener = new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      } catch (FinFormatException | SaLengthException e) {
        log.debug(e.getMessage());
        return "takeAssessment";
      } catch (DataException e) {
        log.error(e.getMessage());
        return "discrepancyInData";
      }
    }
    
    String returnValue;
    if (needToCheck) {
    	if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
    		returnValue = "anonymousQuit";
    	} else {
    		returnValue = "saveForLaterWarning";
    	}
    } else {
    	if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
    		returnValue = "notSubmitted";
    	} else {
    		returnValue = "select";
    	}
    }

    SelectActionListener l2 = new SelectActionListener();
    l2.processAction(null);
    reload = true;

    // quit within time limit, clean timedAssessment from queue,
    // removeTimedAssessmentFromQueue();
    return returnValue;
  }
  
  public String nextPage() {
   return nextHelper(false);
  }

  public String gotoQuestion() {
    return nextHelper(true);
  }

  private String nextHelper(boolean isGoToQuestion) {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
            || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
      syncTimeElapsedWithServer();

      SubmitToGradingActionListener listener =
              new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      } catch (FinFormatException | SaLengthException e) {
        log.debug(e.getMessage());
        return "takeAssessment";
      } catch (DataException e) {
        log.error(e.getMessage());
        return "discrepancyInData";
      }
    }

    int oPartIndex = partIndex;
    int oQuestionIndex = questionIndex;

    if (getSettings().isFormatByPart()) {
      String partIndexString = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("partnumber");
      if(isGoToQuestion) {
        partIndex = Integer.parseInt(partIndexString);
      } else {
        partIndex++;
      }
    }
    if (getSettings().isFormatByQuestion()) {
      String questionIndexString = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("questionnumber");
      if(isGoToQuestion) {
        questionIndex = Integer.parseInt(questionIndexString);
      } else {
        questionIndex++;
      }
    }

    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);


    if ("1".equals(navigation) && this.actionMode != PREVIEW_ASSESSMENT) {
      LinearAccessDeliveryActionListener linearAccessDeliveryActionListener = new LinearAccessDeliveryActionListener();
      if(isGoToQuestion) {
        linearAccessDeliveryActionListener.saveLastVisitedPosition(this, oPartIndex, oQuestionIndex);
      } else {
        linearAccessDeliveryActionListener.saveLastVisitedPosition(this, partIndex, questionIndex);
      }
    }
    reload = false;
    return "takeAssessment";
  }

  public String samePage() {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
        || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
      syncTimeElapsedWithServer();
        	
      SubmitToGradingActionListener listener =
        new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      } catch (FinFormatException | SaLengthException e) {
        log.debug(e.getMessage());
        return "takeAssessment";
      } catch (DataException e) {
        log.error(e.getMessage());
        return "discrepancyInData";
      }
    }

    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    reload = false;
    return "takeAssessment";
  }
  
  public String autoSave() {
	  skipFlag = true;
	  return saveWork();
  }
  
  public String saveWork() {
      String nextAction = checkBeforeProceed();
      log.debug("***** next Action={}", nextAction);
      if (!("safeToProceed").equals(nextAction)) {
          return nextAction;
      }
      forGrade = false;
      if (this.actionMode == TAKE_ASSESSMENT ||
          this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
          syncTimeElapsedWithServer();
          SubmitToGradingActionListener listener =
              new SubmitToGradingActionListener();
          TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
          TimedAssessmentGradingModel timedAG = queue.get(adata.getAssessmentGradingId());
          if (timedAG != null && Integer.parseInt(timeElapse) >= timedAG.getTimeLimit()) {
			  // This is a final save after thread timer expiration
			  // remove the buffers to speed up the submit.
			  // setup the confirmation for AJAX request
			  timedAG.setLatencyBuffer(0);
			  timedAG.setTransactionBuffer(0);
			  timedAG.setBufferedExpirationDate(timedAG.getExpirationDate());
			  String confirmation =
				  adata.getAssessmentGradingId() +
				  "-" +
				  publishedAssessment.getPublishedAssessmentId() +
				  "-" +
				  adata.getAgentId() +
				  "-" +
				  timedAG.getExpirationDate().getTime();
			  setConfirmation(confirmation);
			  lastSave = true;
          }
        try {
          listener.processAction(null);
        } catch (FinFormatException | SaLengthException e) {
          log.debug(e.getMessage());
          return "takeAssessment";
        } catch (DataException e) {
          log.error(e.getMessage());
          return "discrepancyInData";
        }
      }
      DeliveryActionListener l2 = new DeliveryActionListener();
      l2.processAction(null);
      reload = false;
      return "takeAssessment";
  }

  public String previous() {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    if (getSettings().isFormatByPart()) {
      partIndex--;
      questionIndex = 0;
    }
    if (getSettings().isFormatByQuestion()) {
      questionIndex--;

    }
    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
        || this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
      syncTimeElapsedWithServer();
      SubmitToGradingActionListener listener =
        new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      } catch (FinFormatException | SaLengthException e) {
        log.debug(e.getMessage());
        return "takeAssessment";
      } catch (DataException e) {
        log.error(e.getMessage());
        return "discrepancyInData";
      }
    }
    
    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    reload = false;
    return "takeAssessment";
  }

  public String confirmSubmitPrevious() {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    forGrade = false;

    syncTimeElapsedWithServer();

    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    return "takeAssessment";
  }

  public String validatePassword() {
    log.debug("**** password={}", password);
    log.debug("**** setting password={}", getSettings().getPassword());
    
    if (StringUtils.isBlank(password)) {
    	return "passwordAccessError";
    }
    if(StringUtils.isNotBlank(getSettings().getPassword()) && !StringUtils.equals(StringUtils.trim(password), StringUtils.trim(getSettings().getPassword()))) {
    	return "passwordAccessError";
    }

	// in post 2.1, clicking at Begin Assessment takes users to the 1st question.
	return "takeAssessment";
  }

  public String validateIP() {
    String thisIp = ( (HttpServletRequest) FacesContext.
                     getCurrentInstance().getExternalContext().getRequest()).
      getRemoteAddr();
    Iterator addresses = getSettings().getIpAddresses().iterator();
    while (addresses.hasNext()) {
      String next = ( (PublishedSecuredIPAddress) addresses.next()).
        getIpAddress();
      if (next != null && next.contains( "*" )) {
        next = next.substring(0, next.indexOf("*"));
      }
      if (next == null || next.trim().equals("") ||
          thisIp.trim().startsWith(next.trim())) {
        // in post 2.1, clicking at Begin Assessment takes users to the 1st question.
        return "takeAssessment";
      }
    }
    
    return "ipAccessError";
  }

  public String validate() {
    try {
      String results = "takeAssessment";

      // #1. check password
      if (!getSettings().getPassword().equals("")) {
        results = validatePassword();
        log.debug("*** checked password={}", results);
        
        if("passwordAccessError".equals(results)) {
        	updatEventLog("error_pw_access");
        }
      }

      // #2. check IP
      if (!"passwordAccessError".equals(results) &&
          getSettings().getIpAddresses() != null &&
          !getSettings().getIpAddresses().isEmpty()) {
         results = validateIP();
         log.debug("*** checked password & IP={}", results);
         
         if(("ipAccessError").equals(results)) {
        	updatEventLog("error_ip_access");
         }         
      }

      // secure delivery START phase
      // should occur before timer check, so that timer will be stopped if access is denied
      setSecureDeliveryHTMLFragment( "" );
      setBlockDelivery( false );
      SecureDeliveryServiceAPI secureDelivery = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
      if ( "takeAssessment".equals(results) && secureDelivery.isSecureDeliveryAvaliable() ) {
   
    	  String moduleId = publishedAssessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY );
    	  if ( moduleId != null && ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
    		  HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    		  PhaseStatus status = secureDelivery.validatePhase(moduleId, Phase.ASSESSMENT_START, publishedAssessment, request );
    		  setSecureDeliveryHTMLFragment( 
    		  			secureDelivery.getHTMLFragment(moduleId, publishedAssessment, request, Phase.ASSESSMENT_START, status, new ResourceLoader().getLocale() ) );
    		  setBlockDelivery( PhaseStatus.FAILURE == status );
    		  if ( PhaseStatus.SUCCESS == status ) {
    			  results = "takeAssessment";
              } else {
    			  results = "secureDeliveryError";
    			  updatEventLog("error_secure_delivery");
              }
    	  }
      }

      // if results != "takeAssessment", stop the clock if it is a timed assessment
      // Trouble was the timer was started by DeliveryActionListener before validate() is being run.
      // So, we need to remove the timer thread as soon as we realized that the validation fails.
      if (!("takeAssessment".equals(results)) && adata!=null) {
        TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
        TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)queue.
                                             get(adata.getAssessmentGradingId());
        if (timedAG != null){
          String agTimerId = timedAG.getTimerId();
          if (agTimerId != null && agTimerId.equals(timerId)){
            // SAK-6990: it is only safe to removed if u are sure that timedAG is started by your beginAssessment.jsp
            // we added a hidden field timerId on beginAssessment.jsp. Upon successful security check, a timedAG
            // will be created that carried this timerId. If user open another browser to take the same timed 
            // assessment. If the security check of the new one fails, it won't stop the clock for existing one.
            queue.remove(timedAG);
            timeRunning = false;
          }
        }
        return results;
      }
      
      // check before proceed
      String nextAction = checkBeforeProceed();
      log.debug("***** next Action={}", nextAction);
      if (!("safeToProceed").equals(nextAction)){
        return nextAction;
      }
      
      // #3. results="" => no security checking required
      if ("".equals(results)) {
        // in post 2.1, clicking at Begin Assessment takes users to the
        // 1st question.
        return "takeAssessment";
      }
      return results;
    } catch (Exception e) {
    	log.error("accessError{}", e.getMessage());
      EventLogService eventService = new EventLogService();
		 EventLogFacade eventLogFacade = new EventLogFacade();
		 EventLogData eventLogData = null;

		 List eventLogDataList = eventService.getEventLogData(adata.getAssessmentGradingId());
		 if(eventLogDataList != null && eventLogDataList.size() > 0) {
			 eventLogData= (EventLogData) eventLogDataList.get(0);
			 eventLogData.setErrorMsg(eventLogMessages.getString("error_access"));
		 }
		 
		 String thisIp = ( (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
		 eventLogData.setIpAddress(thisIp);
		 
		 eventLogFacade.setData(eventLogData);
		 eventService.saveOrUpdateEventLog(eventLogFacade);
      return "accessError";
    }
  }

  // Used for AJAX update of timer submission status
  //
  public int getSubmissionStatus() {
    // 0 - non-timer submission state
    // 1 - timer thread finished, assessment submitted
    // 2 - timer thread running, time is up, final saved.
    // 3 - timer thread running, time not up.
    // 4 - timer thread running, time is up, not final saved.
    if (adata != null) {
      TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
      TimedAssessmentGradingModel timedAG = queue.get(adata.getAssessmentGradingId());
      if (timedAG != null) {
        if (Integer.parseInt(getTimeElapse()) >= timedAG.getTimeLimit()) {
          if (lastSave) {
            return 2;
          } else {
            return 4;
          }
        } else {
          return 3;
        }
      } else {
        if (lastSave) {
          return 1;
        } else {
          return 0;
        }
      }
    } else {
      return 0;
    }
  }

  public void updatEventLog(String errorMsg) {
	  EventLogService eventService = new EventLogService();
      EventLogFacade eventLogFacade = new EventLogFacade();
      EventLogData eventLogData = new EventLogData();
      
	  eventLogData.setAssessmentId(publishedAssessment.getPublishedAssessmentId());
	  eventLogData.setStartDate(new Date());
	  String agentEid = AgentFacade.getEid();
	  //ONC-3500
	  if(agentEid == null || "".equals(agentEid)){
		  agentEid= "N/A";
	  }
	  eventLogData.setUserEid(agentEid);
	  eventLogData.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(publishedAssessment.getTitle()));
	  String site_id= AgentFacade.getCurrentSiteId();
	  if(site_id == null) {
		  //take assessment via url
		  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		  site_id = publishedAssessmentService.getPublishedAssessmentOwner(publishedAssessment.getPublishedAssessmentId());
	  }
	  eventLogData.setSiteId(site_id);
	  eventLogData.setProcessId(null);
	  eventLogData.setEndDate(null);
	  eventLogData.setEclipseTime(null);
	  eventLogData.setErrorMsg(eventLogMessages.getString(errorMsg));
	  	  
	  String thisIp = ( (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
	  eventLogData.setIpAddress(thisIp);
	  	  
	  eventLogFacade.setData(eventLogData);
	  eventService.saveOrUpdateEventLog(eventLogFacade);
  }
  
  public String pvalidate() {
    // in post 2.1, clicking at Begin Assessment takes users to the
    // 1st question.
    return "takeAssessment";
  }

  public boolean getDoContinue() {
    return next_page;
  }

  public void setContinue(boolean docontinue) {
    next_page = docontinue;
  }

  // Store for paging
  public AssessmentGradingData getAssessmentGrading() {
    return adata;
  }

  public void setAssessmentGrading(AssessmentGradingData newdata) {
    adata = newdata;
  }

  private byte[] getMediaStream(String mediaLocation) {
    byte[] mediaByte = new byte[0];
    try {
      mediaByte = Files.readAllBytes(new File(mediaLocation).toPath());
    } catch (FileNotFoundException ex) {
      log.error("File not found in DeliveryBean.getMediaStream(): {}", ex.getMessage());
    } catch (IOException ex) {
      log.error("IO Exception in DeliveryBean.getMediaStream(): {}", ex.getMessage());
    }
    return mediaByte;
  }

  /**
   * This method is used by jsf/delivery/deliveryFileUpload.jsp
   *   <corejsf:upload
   *     target="/jsf/upload_tmp/assessment#{delivery.assessmentId}/
   *             question#{question.itemData.itemId}/admin"
   *     valueChangeListener="#{delivery.addMediaToItemGrading}" />
     * @param e
   */
  public void addMediaToItemGrading(javax.faces.event.ValueChangeEvent e) {
    if (isTimeRunning() && getTimeExpired())
      setOutcome("timeExpired");

    String mediaLocation = (String) e.getNewValue();
    String action = addMediaToItemGrading(mediaLocation);
    syncTimeElapsedWithServer();
    log.debug("****time passed after fileupload before loading of next question{}", getTimeElapse());
    setTimeElapseAfterFileUpload(getTimeElapse());
    setOutcome(action);
  }

  /**
   * This method is used by jsf/delivery/deliverAudioRecording.jsp and
   * is called by addMediaToItemGrading(javax.faces.event.ValueChangeEvent e)
   *
   * @param mediaLocation the  media location
   * @return the action string
   */
  public String addMediaToItemGrading(String mediaLocation) {
    log.debug("****{} {}", mediaLocation, new Date());
    
    if (!mediaIsValid()) {
    	reload = true;
        return "takeAssessment";
    }
    
    GradingService gradingService = new GradingService();
    //PublishedAssessmentService publishedService = new PublishedAssessmentService();
    Map itemHash = getPublishedItemHash();
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
    String agent = person.getId();

    // 2. format of the media location is: assessmentXXX/questionXXX/agentId/myfile
    // 3. get the questionId (which is the PublishedItemData.itemId)
    String fileMediaLocation = mediaLocation.replace("/".equals(File.separator) ? "\\" : "/" , File.separator);
    String[] mediaLocationParts = fileMediaLocation.split(Pattern.quote(File.separator));
    int numberOfMediaLocationParts = mediaLocationParts.length;

    if (numberOfMediaLocationParts < 4) {
        reload = true;
        return "takeAssessment";
    }

    String agentId = mediaLocationParts[numberOfMediaLocationParts - 2];
    String questionId = mediaLocationParts[numberOfMediaLocationParts - 3];
    String assessmentId = mediaLocationParts[numberOfMediaLocationParts - 4];
    questionId = StringUtils.remove(questionId, "question");
    assessmentId = StringUtils.remove(assessmentId, "assessment");

    log.debug("***3a. addMediaToItemGrading, questionId ={}", questionId);
    log.debug("***3b. addMediaToItemGrading, assessmentId ={}", assessmentId);
    if (agent == null){
      log.debug("**** agentId={}", agentId);
      agent = agentId;
    }
    log.debug("***3c. addMediaToItemGrading, agent ={}", agent);

    // 4. prepare itemGradingData and attach it to assessmentGarding
    PublishedItemData item = (PublishedItemData)itemHash.get(new Long(questionId));
    log.debug("***4a. addMediaToItemGrading, itemText(0) ={}", item.getItemTextArray().get(0));
    // there is only one text in audio question
    PublishedItemText itemText = (PublishedItemText) item.
      getItemTextArraySorted().get(0);
    ItemGradingData itemGradingData = getItemGradingData(questionId);
    boolean newItemGradingData = false;
    if (itemGradingData == null) {
      newItemGradingData = true;
      itemGradingData = new ItemGradingData();
      itemGradingData.setAssessmentGradingId(adata.getAssessmentGradingId());
      itemGradingData.setPublishedItemId(item.getItemId());
      itemGradingData.setPublishedItemTextId(itemText.getId());
      itemGradingData.setSubmittedDate(new Date());
      itemGradingData.setAgentId(agent);
      itemGradingData.setOverrideScore( Double.valueOf(0));
    }
    itemGradingData.setAutoScore(Double.valueOf(0));
    setAssessmentGrading(adata);

    // 5. save ItemGradingData alone 'cos assessmentGrading score won't be changed
    // we don't need to update every itemGrading in assessmentGrading 
    gradingService.saveItemGrading(itemGradingData);

    //if media is uploaded, create media record and attach to itemGradingData
    saveMedia(agent, mediaLocation, itemGradingData, gradingService);

    // 8. do whatever need doing
    DeliveryActionListener dlistener = new DeliveryActionListener();
    // false => do not reset the entire current delivery.pageContents.
    // we will do it ourselves and only update the question that this media
    // is attached to
    skipFlag = true;
    dlistener.processAction(null, false);
    if (newItemGradingData)
      attachToItemContentBean(itemGradingData, questionId);

    reload = true;
    return "takeAssessment";// which doesn't exists to force it to reload
  }

  public void saveMedia(String agent, String mediaLocation, ItemGradingData itemGradingData,
                        GradingService gradingService){
    // 1. create a media record
    File media = new File(mediaLocation);
    String mimeType = MimeTypesLocator.getInstance().getContentType(media);
    boolean SAVETODB = getSaveToDb();
    log.debug("**** SAVETODB={}", SAVETODB);
    MediaData mediaData;
    log.debug("***6a. addMediaToItemGrading, itemGradinDataId={}", itemGradingData.getItemGradingId());
    // 1b. get filename
    String fullname = media.getName().trim();
    int underscore_index = fullname.lastIndexOf("_");
    int dot_index = fullname.lastIndexOf(".");
    String filename = fullname.substring(0,underscore_index);

    if (dot_index >= 0) {
    	filename = filename + fullname.substring(dot_index);
    }
    log.debug("**** filename={}", filename);

    String updatedFilename = gradingService.getFileName(itemGradingData.getItemGradingId(), agent, filename);
    log.debug("**** updatedFilename={}", updatedFilename);
    
    if (SAVETODB) { // put the byte[] in
      byte[] mediaByte = getMediaStream(mediaLocation);
      mediaData = new MediaData(itemGradingData, mediaByte,
                                Long.valueOf(mediaByte.length + ""),
                                mimeType, "description", null,
                                updatedFilename, false, false, 1,
                                agent, new Date(),
                                agent, new Date(), null);
    } else { // put the location in
      mediaData = new MediaData(itemGradingData, null,
    		  					Long.valueOf(media.length() + ""),
                                mimeType, "description", mediaLocation,
                                updatedFilename, false, false, 1,
                                agent, new Date(),
                                agent, new Date(), null);

    }
    Long mediaId = gradingService.saveMedia(mediaData);
    log.debug("mediaId={}", mediaId);
    log.debug("***6c. addMediaToItemGrading, media.itemGradinDataId={}", ( (ItemGradingData) mediaData.getItemGradingData()).getItemGradingId());
    log.debug("***6d. addMediaToItemGrading, mediaId={}", mediaData.getMediaId());

    // 2. store mediaId in itemGradingRecord.answerText
    log.debug("***7. addMediaToItemGrading, adata={}", adata);
    itemGradingData.setAnswerText(mediaId + "");
    gradingService.saveItemGrading(itemGradingData);
    // 3. if saveToDB, remove file from file system
    try {
      	if (SAVETODB) {
      	    boolean success = media.delete();
      	    if (!success){
      		    log.warn("Error: media.delete() failed for mediaId ={}", mediaId);
      	    }
      	}
    } catch(Exception e) {
      log.warn(e.getMessage());
    }
  }

  public boolean mediaIsValid() {
    boolean returnValue =true;
    // check if file is too big
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext external = context.getExternalContext();
    Long fileSize = (Long)((ServletContext)external.getContext()).getAttribute("TEMP_FILEUPLOAD_SIZE");
    Long maxSize = Long.valueOf(ServerConfigurationService.getInt("samigo.sizeMax", 40960));

    ((ServletContext)external.getContext()).removeAttribute("TEMP_FILEUPLOAD_SIZE");
    if (fileSize!=null){
      float fileSize_float = fileSize.floatValue()/1024;
      int tmp = Math.round(fileSize_float * 10.0f);
      fileSize_float = (float)tmp / 10.0f;
      float maxSize_float = maxSize.floatValue()/1024;
      int tmp0 = Math.round(maxSize_float * 10.0f);
      maxSize_float = (float)tmp0 / 10.0f;

      String err1=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "file_upload_error");
      String err2=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "file_uploaded");
      String err3=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "max_size_allowed");
      String err4=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "upload_again");
      String err = err2 + fileSize_float + err3 + maxSize_float + err4;
      context.addMessage("file_upload_error",new FacesMessage(err1));
      context.addMessage("file_upload_error",new FacesMessage(err));
      returnValue = false;
    }
    return returnValue;
  }

  public void setPublishedAssessment(PublishedAssessmentFacade publishedAssessment) {
	  this.publishedAssessment = publishedAssessment;
	  //Setup extendedTimeDeliveryService
	  if (extendedTimeDeliveryService == null && 
			  (publishedAssessment != null && publishedAssessment.getPublishedAssessmentId() != null)) {
		  extendedTimeDeliveryService = new ExtendedTimeDeliveryService(publishedAssessment);
	  }
  }

  public String doit() {
    return outcome;
  }

  public ItemGradingData getItemGradingData(String publishedItemId) {
    ItemGradingData selected = null;
    if (adata != null) {
      Set items = adata.getItemGradingSet();
      if (items != null) {
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
          ItemGradingData itemGradingData = (ItemGradingData) iter.next();
          String itemPublishedId = itemGradingData.getPublishedItemId().
            toString();
          if ( (publishedItemId).equals(itemPublishedId)) {
            log.debug("*** addMediaToItemGrading, same : found it");
            selected = itemGradingData;
          } else {
            log.debug("*** addMediaToItemGrading, not the same");
          }
        }
        log.debug("*** addMediaToItemGrading, publishedItemId ={}", publishedItemId);
        if (selected != null) {
          log.debug("*** addMediaToItemGrading, itemGradingData.publishedItemId ={}", selected.getPublishedItemId());
        }
      }
    }
    return selected;
  }

  public String getSiteId() {

    if (StringUtils.isNotBlank(siteId)) {
      return siteId;
    } else {
      Placement currentPlacement = ToolManager.getCurrentPlacement();
      if (currentPlacement != null) {
        siteId = currentPlacement.getContext();
      }
      return siteId;
    }
  }

  public String getAgentAccessString() {
    return deliveryAgent.getAgentInstanceString();
  }
  public void setAgentAccessString(String agentString) {
    deliveryAgent.setAgentInstanceString(agentString);
  }

  public boolean getSaveToDb(){
    return ServerConfigurationService.getBoolean("samigo.saveMediaToDb", true);
  }

  public void attachToItemContentBean(ItemGradingData itemGradingData, String questionId){
    List<ItemGradingData> list = new ArrayList<>();
    list.add(itemGradingData);
    //find out sectionId from questionId
    log.debug("**** attachToItemContentBean, questionId={}", questionId);

    PublishedItemData publishedItem = (PublishedItemData) getPublishedItemHash().get(new Long(questionId));
    PublishedSectionData publishedSection = (PublishedSectionData) publishedItem.getSection();
    String sectionId = publishedSection.getSectionId().toString();
    SectionContentsBean partSelected = null;

    //get all partContents
    List<SectionContentsBean> parts = getPageContents().getPartsContents();
    for (SectionContentsBean part : parts) {
      log.debug("**** question's sectionId{}", sectionId);
      log.debug("**** partId{}", part.getSectionId());
      if (sectionId.equals(part.getSectionId())) {
        partSelected = part;
        break;
      }
    }
    //locate the itemContentBean - the hard way, sigh...
    List<ItemContentsBean> items = new ArrayList<>();
    if (partSelected!=null)
      items = partSelected.getItemContents();
    for (ItemContentsBean item : items) {
      if ((publishedItem.getItemId()).equals(item.getItemData().getItemId())) { // comparing itemId not object
        item.setItemGradingDataArray(list);
        break;
      }
    }
  }

  public void setActionString(String actionString){
    this.actionString = actionString;
    // the follwoing two values will be evaluated when reviewing assessment
    // based on PublishedFeedback settings
    setFeedback("false");
    setNoFeedback("true");

    if (null != actionString) {
        switch( actionString ) {
          case "previewAssessment":
              setActionMode(PREVIEW_ASSESSMENT);
              break;
          case "reviewAssessment":
              setActionMode(REVIEW_ASSESSMENT);
              break;
          case "gradeAssessment":
              setFeedback("true");
              setNoFeedback("false");
              setActionMode(GRADE_ASSESSMENT);
              break;
          case "takeAssessment":
              setActionMode(TAKE_ASSESSMENT);
              break;
          case "takeAssessmentViaUrl":
              setActionMode(TAKE_ASSESSMENT_VIA_URL);
              break;
        }
    }
  }

  public boolean getTimeExpired(){
    if (adata == null) {
    	return false;
    }
    return timeExpired;
  }

  private void removeTimedAssessmentFromQueue(){
    if (adata==null) {
      return;
    }
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)queue.get(adata.getAssessmentGradingId());
    if (timedAG != null){
      queue.remove(timedAG);
      timeRunning = false;
    }
  }

  public void syncTimeElapsedWithServer(){
	    if (("takeAssessment").equals(actionString) || ("takeAssessmentViaUrl").equals(actionString)){
	      if (adata==null) {
	         if (log.isDebugEnabled()) {
	            log.debug("aData is null for actionString"+actionString);
	         }
	         return;
	      }
	      TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
	      TimedAssessmentGradingModel timedAG = queue.get(adata.getAssessmentGradingId());
	      if (timedAG != null){
	        int timeElapsed  = Math.round((new Date().getTime() - adata.getAttemptDate().getTime())/1000.0f);
	        log.debug("***setTimeElapsed={}", timeElapsed);
	        adata.setTimeElapsed(timeElapsed);
	        setTimeElapse(adata.getTimeElapsed().toString());
	      }
	    }
	  }
	  
	  public void syncTimeElapsedWithServerLinear(){
		    if (("takeAssessment").equals(actionString) || ("takeAssessmentViaUrl").equals(actionString)){
		      if (adata==null) {
		          if (log.isDebugEnabled()) {
		              log.debug("aData is null for actionString"+actionString);
		          }
		          return;
		      }
		      TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
		      TimedAssessmentGradingModel timedAG = queue.get(adata.getAssessmentGradingId());
		      if (timedAG != null){
		    	int timeElapsed  = Math.round((new Date().getTime() - adata.getAttemptDate().getTime())/1000.0f);
		        adata.setTimeElapsed(timeElapsed);
		        GradingService gradingService = new GradingService();
		        gradingService.saveOrUpdateAssessmentGradingOnly(adata);
		        setTimeElapse(adata.getTimeElapsed().toString());
		      }
		    }
	  }

  public void setTimeElapseAfterFileUpload(String timeElapseAfterFileUpload) {
    this.timeElapseAfterFileUpload = timeElapseAfterFileUpload;
    if (timeElapseAfterFileUpload!=null && !("").equals(timeElapseAfterFileUpload))
      setTimeElapseAfterFileUploadDouble(( Double.valueOf(timeElapseAfterFileUpload)));
  }

  public Map getPublishedItemHash(){
    if (this.publishedItemHash.isEmpty()){
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      this.publishedItemHash = pubService.preparePublishedItemHash(getPublishedAssessment());
    }
    return this.publishedItemHash;
  }

  public Map getPublishedItemTextHash(){
    if (this.publishedItemTextHash.isEmpty()){
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      this.publishedItemTextHash = pubService.preparePublishedItemTextHash(getPublishedAssessment());
    }
    return this.publishedItemTextHash;
  }

  public Map getPublishedAnswerHash(){
    if (this.publishedAnswerHash.isEmpty()){
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      this.publishedAnswerHash = pubService.preparePublishedAnswerHash(getPublishedAssessment());
    }
    return this.publishedAnswerHash;
  }
   
   public boolean getIsMoreThanOneQuestion() {
	   log.debug("getIsMoreThanOneQuestion() starts");
	   List partsContents = this.pageContents.getPartsContents();
	   if (partsContents.size() == 1) {
		   String size = ((SectionContentsBean) partsContents.get(0)).getItemContentsSize();
		   log.debug("ItemContentsSize = {}", size);
		   if ("1".equals(size)) {
			   log.debug("isMoreThanOneQuestion set to false");
			   isMoreThanOneQuestion = false;
		   }
	   } else {
		   log.debug("isMoreThanOneQuestion set to true");
		   isMoreThanOneQuestion = true;
	   }
	   return isMoreThanOneQuestion;
   }

  public boolean getHasAttachment(){
    boolean hasAttachment = false;
    if (attachmentList!=null && attachmentList.size() >0){
        hasAttachment = true;
    }
    return hasAttachment;
  }

  public String checkBeforeProceed(boolean isSubmitForGrade, boolean isFromTimer, boolean isViaUrlLogin){
    // public method, who know if publishedAssessment is set, so check to be sure
    if (getPublishedAssessment() == null){
      return "error";
    }

    boolean acceptLateSubmission = isAcceptLateSubmission();

    if (this.actionMode == PREVIEW_ASSESSMENT) {
		  return "safeToProceed";
    }
  
    GradingService service = new GradingService();
    AssessmentGradingData assessmentGrading=null;
    if (adata!=null){
      assessmentGrading = service.load(adata.getAssessmentGradingId().toString(), false);
    }

    if (!canAccess(isViaUrlLogin)) {
      return "accessDenied";
    }

    if (isRemoved()){
        return "isRemoved";
    }
    
    log.debug("check 1");
    // check 0: check for start date
    if (!isAvailable()){
      return ("assessmentNotAvailable");
    }
    
    log.debug("check 2");
    // check 2: is it still available?
    if (!isFromTimer && isRetracted(isSubmitForGrade) && acceptLateSubmission){
     return "isRetracted";
    }
    
    log.debug("check 3");
    // check 3: is it still available?
    if (isRetractedForEdit()){
        return "isRetractedForEdit";
    }
    
    log.debug("check 4");
    // check 4: check for multiple window & browser trick 
    boolean discrepancyInData = false;
    if (assessmentGrading!=null && !checkDataIntegrity(assessmentGrading)){
      discrepancyInData = true;
    }

    log.debug("check 5");
    // check 5: if workingassessment has been submiited?
    // this is to prevent student submit assessment and use a 2nd window to 
    // continue working on the submitted work.
    if (assessmentGrading!=null && getAssessmentHasBeenSubmitted(assessmentGrading)){
      return "assessmentHasBeenSubmitted";
    }

    log.debug("check 6");
    // check 6: is it need to resubmit? If yes, we don't check on submission number, dates, or time.
    if (isNeedResubmit() && !discrepancyInData){
        return "safeToProceed";
    }
    
    if (numberRetake == -1 || actualNumberRetake == -1) {
    	GradingService gradingService = new GradingService();
    	numberRetake = gradingService.getNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString());
    	actualNumberRetake = gradingService.getActualNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString());
    }
    	
    log.debug("check 7");
    // check 7: any submission attempt left?
    if (!getHasSubmissionLeft(numberRetake)){
      return "noSubmissionLeft";
    }

    log.debug("check 8");
    // check 8: has dueDate arrived? if so, does it allow late submission?
    // If it is a timed assessment and "No Late Submission" and not during a Retake, always go through. Because in this case the
   	// assessment will be auto-submitted anyway - when time is up or when current date reaches due date (if the time limited is
   	// longer than due date,) for either case, we want to redirect to the normal "submision successful page" after submitting.
    if (pastDueDate()){
    	// If Accept Late and there is no submission yet, go through
    	if (acceptLateSubmission){
    		if(totalSubmissions == 0) {
    			log.debug("Accept Late Submission && totalSubmissions == 0");
    		} else {
    			log.debug("take from bean: actualNumberRetake ={}", actualNumberRetake);
    			// Not during a Retake
    			if (actualNumberRetake == numberRetake) {
    				return "noLateSubmission";
    			} else if (actualNumberRetake == numberRetake - 1) {// During a Retake
    				log.debug("actualNumberRetake == numberRetake - 1: through Retake");
    			} else {// Should not come to here
    				log.error("Should NOT come to here - wrong actualNumberRetake or numberRetake");
    			}
    		}
    	} else {
    		if(!isRetracted(isSubmitForGrade)){
    			log.debug("take from bean: actualNumberRetake ={}", actualNumberRetake);
    			// Not during a Retake
    			if (actualNumberRetake == numberRetake) {
    				// When taking the assessment via URL (from LoginServlet), if pass due date, throw an error 
    				if (isViaUrlLogin) {
    					return "noLateSubmission";
    				} else if (this.isTimedAssessment()) {// If No Late, this is a timed assessment, and not during a Retake, go through (see above reason)
    					log.debug("No Late Submission && timedAssessment");
    				} else {
    					log.debug("noLateSubmission");
    					return "noLateSubmission";
    				}
    			} else if (actualNumberRetake == numberRetake - 1) {// During a Retake
    				log.debug("actualNumberRetake == numberRetake - 1: through Retake");
    			} else {// Should not come to here
    				log.error("Should NOT come to here - wrong actualNumberRetake or numberRetake");
    			}
    		} else {
    		     return "isRetracted";
    		}
    	}
    }

    if (discrepancyInData){
        return ("discrepancyInData");
    }
    
    log.debug("check9");
    // check 9: is timed assessment? and time has expired?
    if (isTimeRunning() && getTimeExpired() && !turnIntoTimedAssessment){
      return "timeExpired";
    }
    
    return "safeToProceed";
  }
  
  public String checkFromViaUrlLogin(){
	  return checkBeforeProceed(false, false, true);
  }
  
  public String checkBeforeProceed(){
	  return checkBeforeProceed(false, false);
  }
  
  public String checkBeforeProceed(boolean isSubmitForGrade, boolean isFromTimer){
	  boolean isViaUrlLogin = false;
	  if(AgentFacade.getCurrentSiteId() == null){
	    isViaUrlLogin = true;
	  }
	  return checkBeforeProceed(isSubmitForGrade, isFromTimer, isViaUrlLogin);
  }

  private boolean getHasSubmissionLeft(int numberRetake){
    boolean hasSubmissionLeft = false;
    int maxSubmissionsAllowed = 9999;
    if ( (Boolean.FALSE).equals(publishedAssessment.getAssessmentAccessControl().getUnlimitedSubmissions())){
      maxSubmissionsAllowed = publishedAssessment.getAssessmentAccessControl().getSubmissionsAllowed();
    }
    if ("takeAssessmentViaUrl".equals(actionString) && !anonymousLogin && settings == null) {
      SettingsDeliveryBean settingsDeliveryBean = new SettingsDeliveryBean();
      settingsDeliveryBean.setAssessmentAccessControl(publishedAssessment);
      settingsDeliveryBean.setMaxAttempts(maxSubmissionsAllowed);
      settings = settingsDeliveryBean;
    }
    if (totalSubmissions < maxSubmissionsAllowed + numberRetake){
      hasSubmissionLeft = true;
    }
    return hasSubmissionLeft;
  }

  private boolean isAvailable(){
	  boolean isAvailable = true;
	  Date currentDate = new Date();
		Date startDate;
		if (extendedTimeDeliveryService.hasExtendedTime()) {
			startDate = extendedTimeDeliveryService.getStartDate();
		} else {
			startDate = publishedAssessment.getAssessmentAccessControl().getStartDate();
		}
	  if (startDate != null && startDate.after(currentDate)){
		  isAvailable = false;
	  }
	  return isAvailable;
  }
  
  public boolean pastDueDate(){
    boolean pastDueDate = true;
    Date currentDate = new Date();
    Date due = extendedTimeDeliveryService.hasExtendedTime() ? extendedTimeDeliveryService.getDueDate() : publishedAssessment.getAssessmentAccessControl().getDueDate();

    if (due == null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(publishedAssessment.getAssessmentAccessControl().getLateHandling())) {
      Date retract = extendedTimeDeliveryService.hasExtendedTime() ? extendedTimeDeliveryService.getRetractDate() : publishedAssessment.getAssessmentAccessControl().getRetractDate();
      if (due == null && retract != null) {
        due = retract;
      }
    }

    if (due == null || due.after(currentDate)) {
      pastDueDate = false;
    }
    return pastDueDate;
  }

  public boolean isAcceptLateSubmission() {
	  boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(publishedAssessment.getAssessmentAccessControl().getLateHandling());
	  //If using extended Time Delivery, the late submission setting is based on retracted
	  if (extendedTimeDeliveryService.hasExtendedTime()) {
		  //Accept it if it's not retracted on the extended time entry
		  acceptLateSubmission = (extendedTimeDeliveryService.getRetractDate() != null) ? !isRetracted(false) : false;
	  }
	  return acceptLateSubmission;
  }

  public boolean isRetracted(boolean isSubmitForGrade){
    boolean isRetracted = true;
    Date currentDate = new Date();
    Date retractDate = null;
    boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(publishedAssessment.getAssessmentAccessControl().getLateHandling());
    if (extendedTimeDeliveryService.hasExtendedTime()) {
    	retractDate = extendedTimeDeliveryService.getRetractDate();
    } else if (acceptLateSubmission) {
    	retractDate = publishedAssessment.getAssessmentAccessControl().getRetractDate();
    }
    if (retractDate == null || retractDate.after(currentDate)){
        isRetracted = false;
    }
    return isRetracted;
  }

  private boolean canAccess(boolean fromUrl) {
    if (isAnonymousLogin()) {
      return true;
    }

    String siteId = fromUrl ? publishedAssessment.getOwnerSiteId() : AgentFacade.getCurrentSiteId();
    return PersistenceService.getInstance()
        .getAuthzQueriesFacade()
        .hasPrivilege(SamigoConstants.AUTHZ_TAKE_ASSESSMENT, siteId);
  }

  private boolean isRemoved(){
	  Integer status = publishedAssessment.getStatus();
	  return status.equals(AssessmentBaseIfc.DEAD_STATUS);
  }
  
  private boolean isRetractedForEdit(){
	  Integer status = publishedAssessment.getStatus();
	  return status.equals(AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS);
  }
  
  private boolean isNeedResubmit(){
	  if (adata == null) {
		  return false;
	  }
	  Integer status = adata.getStatus();
	  return status.equals(AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT);
  }
  
  private boolean checkDataIntegrity(AssessmentGradingData assessmentGrading){
    // get assessmentGrading from DB, this is to avoid same assessment being
    // opened in the differnt browser
    if (assessmentGrading !=null){
      long DBdate = 0;
      if (assessmentGrading.getSubmittedDate()!=null){
         DBdate = assessmentGrading.getSubmittedDate().getTime();
      }
      String browserDateString = ContextUtil.lookupParam("lastSubmittedDate1");
      if (browserDateString == null){
        browserDateString = ContextUtil.lookupParam("lastSubmittedDate2");
      }

      // SAK-7106:jsf doesn't like id with same name even though there is a rendering condition there
      // so we have to use 2 differnt id and check it this way instead.
      long browserDate=0;
      try{
        if (browserDateString!=null){
          browserDate = Long.parseLong(browserDateString);
        } else {
        	return true;
        }
      } catch(Exception e){
	  log.warn(e.getMessage());
      }
      
      log.debug("last modified date in DB={}", DBdate);
      log.debug("last modified date in browser={}", browserDate);
      log.debug("date is equal={}", DBdate==browserDate);
      return (DBdate==browserDate);
    }
    else return true;
  }

  private boolean getAssessmentHasBeenSubmitted(AssessmentGradingData assessmentGrading){
    // get assessmentGrading from DB, this is to avoid same assessment being
    // opened in the differnt browser
    if (assessmentGrading !=null){
      return assessmentGrading.getForGrade();
    }
    else return false;
  }

  public String getPortal(){
   return ServerConfigurationService.getString("portalPath");
  }

  public String getSelectURL(){
   	  Session session = SessionManager.getCurrentSession();
	  String returnUrl = (String)session.getAttribute("LESSONBUILDER_RETURNURL_SAMIGO");
	  if (returnUrl != null)
	      return returnUrl;
	  StringBuilder url = new StringBuilder(ServerConfigurationService.getString("portalPath"));
	  url.append("/site/");
	  String currentSiteId = AgentFacade.getCurrentSiteId();
	  if(currentSiteId == null){
	      PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	      currentSiteId = publishedAssessmentService.getPublishedAssessmentSiteId(getAssessmentId());
	  }
	  url.append(currentSiteId);
	  url.append("/page/");
	  url.append(getCurrentPageId(currentSiteId));
	  return url.toString();
  }
  
	private Site getCurrentSite(String id) {
		Site site = null;
		//Placement placement = ToolManager.getCurrentPlacement();
		//String currentSiteId = placement.getContext();
		try {
			site = SiteService.getSite(id);
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}
		return site;
	}
	
	public String getAudioQuestionLink() {
		Site currentSite = getCurrentSite(getSiteId());
		String placement = null;
		
		if (currentSite != null) {
			placement = ToolManager.getCurrentPlacement().getId();
		}

		if (placement != null) {
			return "/portal/tool/" + placement + "/jsf/author/audioRecordingPopup.faces";
		} else {
			return "/samigo-app/jsf/author/audioRecordingPopup.faces";
		}
	}
	
	private String getCurrentPageId(String id) {
		Site currentSite = getCurrentSite(id);
		if (currentSite == null) {
			return "";
		}
		SitePage page;
		String toolId;
		try {
			// get page
			List pageList = currentSite.getPages();
			for (int i = 0; i < pageList.size(); i++) {
				page = (SitePage) pageList.get(i);
				List pageToolList = page.getTools();
				//toolId = ((ToolConfiguration) pageToolList.get(0)).getTool().getId();
				
				//  issue with null tool
				if (pageToolList.get(0)==null && ((ToolConfiguration) pageToolList.get(0)).getTool()==null) {
					continue;
				}
				toolId = ((ToolConfiguration) pageToolList.get(0)).getToolId();
				
				if (toolId.equalsIgnoreCase("sakai.samigo")) {
					return page.getId();
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		return "";
	}

	  public String updateTimeLimit(String timeLimit) {
  	    if (numberRetake == -1 || actualNumberRetake == -1) {
		    	GradingService gradingService = new GradingService();
		    	numberRetake = gradingService.getNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString());
		    	log.debug("numberRetake = {}", numberRetake);
		    	actualNumberRetake = gradingService.getActualNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString());
		    	log.debug("actualNumberRetake ={}", actualNumberRetake);
		}
		String returnedTimeLimit = timeLimit;
		if (!("previewAssessment").equals(actionString) && actualNumberRetake >= numberRetake && beginTime != null) {
			returnedTimeLimit = getTimeBeforeDueRetract(timeLimit);
		}
		
		return returnedTimeLimit;
	  }

	  public String getDeadlineString() {
		    if (deadline == null) {
		      return "";
		    }

		    return userTimeService.dateTimeFormat(deadline, new ResourceLoader().getLocale(), DateFormat.MEDIUM);
	  }

	  public void setDeadline() {
		  if (this.firstTimeTaking) {
			  boolean acceptLateSubmission = isAcceptLateSubmission();

			  if (dueDate != null) {
				  if (!acceptLateSubmission) {
					  deadline = dueDate;
				  } else {
					  if (totalSubmissions > 0) {
						  deadline = dueDate;
					  } else {
						  if (retractDate != null) {
							  deadline = retractDate;
						  }
					  }
				  }
			  } else {
				  if (retractDate != null) {
					  deadline = retractDate;
				  }
			  }
		  } else {
			  if (dueDate != null) {
				  deadline = dueDate;
			  } else {
				  if (retractDate != null) {
					  deadline = retractDate;
				  } else {
					  deadline = null;
				  }
			  }
		  }
	  }
	  
	  public String getTimeBeforeDueRetract(String timeLimit) {
		  boolean acceptLateSubmission = isAcceptLateSubmission();
		  
		  String finalTimeLimit = timeLimit;
		  if (dueDate != null) {
			  if (!acceptLateSubmission) {
				  finalTimeLimit = getTimeBeforeDue(timeLimit);
			  } else {
				  if (totalSubmissions > 0) {
					  finalTimeLimit = getTimeBeforeDue(timeLimit);
				  } else {
					  if (retractDate != null) {
						  finalTimeLimit = getTimeBeforeRetract(timeLimit);
					  }
				  }
			  }
		  } else {
			  if (retractDate != null) {
				  finalTimeLimit = getTimeBeforeRetract(timeLimit);
			  }
		  }
		 
		  return finalTimeLimit;
	  }
	  
	  private String getTimeBeforeDue(String timeLimit) {
		  if (timeLimit != null && Integer.parseInt(timeLimit) > 0 && beginTime != null) {
			  int timeBeforeDue  = Math.round((dueDate.getTime() - beginTime.getTime())/1000.0f);
			  if (timeBeforeDue < Integer.parseInt(timeLimit)) {
				  timeLimit = String.valueOf(timeBeforeDue);
			  }
		  } else {
			  int timeBeforeDue  = Math.round((dueDate.getTime() - System.currentTimeMillis())/1000.0f);
			  timeLimit = String.valueOf(timeBeforeDue);
		  }
		  
		  return timeLimit;
	  }
	  
	  private String getTimeBeforeRetract(String timeLimit) {
		  String returnedTime = timeLimit;
		  if (timeLimit != null && Integer.parseInt(timeLimit) > 0 && beginTime != null) {
			  int timeBeforeRetract  = Math.round((retractDate.getTime() - beginTime.getTime())/1000.0f);
			  if (timeBeforeRetract < Integer.parseInt(timeLimit)) {
				  returnedTime = String.valueOf(timeBeforeRetract);
			  }
		  } else {
			  int timeBeforeRetract  = Math.round((retractDate.getTime() - System.currentTimeMillis())/1000.0f);
			  returnedTime = String.valueOf(timeBeforeRetract);
		  }
		  return returnedTime;
	  }	  
	  
	  private boolean isTimedAssessment() {
		  return !this.getPublishedAssessment().getAssessmentAccessControl().getTimeLimit().equals(0);
	  }
	  
	  public String cleanRadioButton() {

		  // We get the id of the question
		  String radioId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("radioId");
		  StringBuilder redrawAnchorName = new StringBuilder("p");
		  String tmpAnchorName = "";
		  List<SectionContentsBean> parts = this.pageContents.getPartsContents();

        for (SectionContentsBean part : parts) {
          String partSeq = part.getNumber();

          List<ItemContentsBean> items = part.getItemContents();
          for (ItemContentsBean item : items) {
            //Just delete the checkbox of the current question
            if (!item.getItemData().getItemId().toString().equals(radioId)) continue;

            String itemSeq = item.getItemData().getSequence().toString();
            redrawAnchorName.append(partSeq);
            redrawAnchorName.append("q");
            redrawAnchorName.append(itemSeq);
            if ("".equals(tmpAnchorName) || tmpAnchorName.compareToIgnoreCase(redrawAnchorName.toString()) > 0) {
              tmpAnchorName = redrawAnchorName.toString();
            }

            if (item.getItemData().getTypeId().longValue() == TypeIfc.MULTIPLE_CHOICE.longValue() ||
                    item.getItemData().getTypeId().longValue() == TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.longValue() ||
                    item.getItemData().getTypeId().longValue() == TypeIfc.MULTIPLE_CHOICE_SURVEY.longValue() ||
                    item.getItemData().getTypeId().longValue() == TypeIfc.MATRIX_CHOICES_SURVEY.longValue()) {
              item.setUnanswered(true);
              if (item.getItemData().getTypeId().longValue() == TypeIfc.MATRIX_CHOICES_SURVEY.longValue()) {
                for (int k = 0; k < item.getMatrixArray().size(); k++) {
                  MatrixSurveyBean selection = (MatrixSurveyBean) item.getMatrixArray().get(k);
                  selection.setResponseFromCleanRadioButton();
                }
              } else {
                for (int k = 0; k < item.getSelectionArray().size(); k++) {
                  SelectionBean selection = (SelectionBean) item.getSelectionArray().get(k);
                  //selection.setResponse(false);
                  selection.setResponseFromCleanRadioButton();
                }
              }

              List<ItemGradingData> itemGradingData = new ArrayList<>();
              for (ItemGradingData itemgrading : item.getItemGradingDataArray()) {
                if (itemgrading.getItemGradingId() != null && itemgrading.getItemGradingId().intValue() > 0) {
                  itemGradingData.add(itemgrading);
                  itemgrading.setPublishedAnswerId(null);
                }
              }
              item.setItemGradingDataArray(itemGradingData);
            }

            if (item.getItemData().getTypeId().longValue() == TypeIfc.TRUE_FALSE.longValue()) {
              item.setResponseId(null);
              item.getItemGradingDataArray().stream().findAny().ifPresent(d -> d.setPublishedAnswerId(null));
            }
            item.setReview(false);
            item.setRationale("");
          }
        }

		  syncTimeElapsedWithServer();
		  
		  // Set the anchor
		  setRedrawAnchorName(tmpAnchorName);
		  
		  return "takeAssessment";
	  }
	  
	  public String cleanAndSaveRadioButton(){
		  cleanRadioButton();
		  return saveWork();
	  }

	  public int getAutoSaveRepeatMilliseconds() {
  	    return ServerConfigurationService.getInt("samigo.autoSave.repeat.milliseconds", 300000);
	  }

	  public boolean getStudentRichText() {
	      String studentRichText = ServerConfigurationService.getString("samigo.studentRichText", "true");
		  return Boolean.parseBoolean(studentRichText);
	  }

	  public String getRecURL() {
  	    if (RECPATH == null || RECPATH.trim().equals("")) {
  	    	return "";
  	    }
		String recURL = ACCESSBASE + RECPATH;
	    return recURL;
	  }

	/**
	 * Return the time limit as a String
	 * 
	 * @param pubAssessment
	 * @param fromBeginAssessment
	 * @param extTimeVal
	 * @return
	 */
	public int evaluateTimeLimit(PublishedAssessmentFacade pubAssessment, Boolean fromBeginAssessment, int extTimeVal) {
		publishedAssessment = pubAssessment; // synchronize the passed in values
		Integer timeLimit = 0;
		Integer originalTimeLimit = publishedAssessment.getAssessmentAccessControl().getTimeLimit();
		int extTimeAdjust = 0;

		// Calcuate the adjustment due to extended time if necessary
		if (extTimeVal > 0) {
			extTimeAdjust = extTimeVal - originalTimeLimit; // adjustment to add
															// to time remaining
		}

		if (fromBeginAssessment) {
			timeLimit = Integer.parseInt(this.updateTimeLimit(originalTimeLimit.toString())) + extTimeAdjust;
		} else {
			if (this.getTimeLimit() != null) {
				timeLimit = Integer.parseInt(this.getTimeLimit());
			}
		}
		this.setTimeLimit(timeLimit.toString());

		return timeLimit;
	}

	  //SAM-2517
	  public boolean getIsMathJaxEnabled(){
		  String siteId = AgentFacade.getCurrentSiteId();
		  if(siteId == null) {
		    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		    siteId = publishedAssessmentService.getPublishedAssessmentOwner(Long.parseLong(getAssessmentId()));
		  }
		  return Boolean.parseBoolean(getCurrentSite(siteId).getProperties().getProperty(Site.PROP_SITE_MATHJAX_ALLOWED));
	  }
	  public String getMathJaxHeader(){
		  StringBuilder headMJ = new StringBuilder();
		  headMJ.append("<script type=\"text/x-mathjax-config\">\nMathJax.Hub.Config({\nmessageStyle: \"none\",\ntex2jax: { inlineMath: [['$$','$$'],['\\\\(','\\\\)']] }, TeX: { equationNumbers: { autoNumber: 'AMS' } }\n});\n</script>\n");
		  headMJ.append("<script src=\"").append(MATHJAX_SRC_PATH).append("\" type=\"text/javascript\"></script>\n");
		  return headMJ.toString();
	  }

    public String getQuestionProgressUnansweredPath () {
      return questionProgressUnansweredPath;
    }

    public String getQuestionProgressAnsweredPath () {
      return questionProgressAnsweredPath;
    }

    public String getQuestionProgressMardPath () {
      return questionProgressMardPath;
    }

    public String getMinReqScale() {
      return ServerConfigurationService.getString("samigo.ajaxTimerMinReqScale","5000");
    }

    public void calculateMinutesAndSecondsLeft() {
        int milliseconds = getAutoSaveRepeatMilliseconds();
        if (milliseconds > 0) {
            Date d = new Date(milliseconds);
            this.setMinutesLeft(String.valueOf(d.getMinutes()));
            this.setSecondsLeft(String.valueOf(d.getSeconds()));
        }
    }

    public String getCDNQuery() {
        return PortalUtils.getCDNQuery();
    }
}
