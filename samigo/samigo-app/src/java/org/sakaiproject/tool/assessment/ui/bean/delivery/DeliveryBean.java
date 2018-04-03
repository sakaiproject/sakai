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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
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
import org.sakaiproject.tool.assessment.services.FinFormatException;
import org.sakaiproject.tool.assessment.services.GradingService;
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
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.web.session.SessionUtil;
import org.sakaiproject.tool.assessment.util.ExtendedTimeDeliveryService;
import org.sakaiproject.tool.assessment.util.MimeTypesLocator;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 *
 * @author casong
 * @author esmiley@stanford.edu added agentState
 * $Id$
 *
 * Used to be org.navigoproject.ui.web.asi.delivery.XmlDeliveryForm.java
 */
@Slf4j
public class DeliveryBean
  implements Serializable
{

  //SAM-2517
  private ServerConfigurationService serverConfigurationService;
  
  private static final String MATHJAX_ENABLED = "mathJaxEnabled";
  private static final String MATHJAX_SRC_PATH_SAKAI_PROP = "portal.mathjax.src.path";
  private static final String MATHJAX_SRC_PATH = ServerConfigurationService.getString(MATHJAX_SRC_PATH_SAKAI_PROP);
  
  private String assessmentId;
  private String assessmentTitle;
  private boolean honorPledge;
  private List markedForReview;
  private List blankItems;
  private List markedForReviewIdents;
  private List blankItemIdents;
  private boolean reviewMarked;
  private boolean reviewAll;
  private boolean reviewBlank;
  private boolean displayMardForReview;
  private int itemIndex;
  private int size;
  private String action;
  private Date beginTime;
  private String endTime;
  private String currentTime;
  private String multipleAttempts;
  private String timeOutSubmission;
  private String submissionTicket;
  private String timeElapse;
  private int sectionIndex;
  private boolean previous;
  private String duration;
  private String url;
  private String confirmation;
  private String outcome;
  private String receiptEmailSetting;

  //Settings
  private String questionLayout;
  private String navigation;
  private String numbering;
  private String feedback;
  private String noFeedback;
  private String statistics;
  private String creatorName;

  private FeedbackComponent feedbackComponent;
  private String feedbackComponentOption;
  private boolean feedbackOnDate;
  private String errorMessage;
  private SettingsDeliveryBean settings;
  private java.util.Date dueDate;
  private java.util.Date adjustedTimedAssesmentDueDate;
  private java.util.Date retractDate;
  private boolean statsAvailable;
  private boolean submitted;
  private boolean graded;
  private String graderComment;
  private List<AssessmentGradingAttachment> assessmentGradingAttachmentList;
  private boolean hasAssessmentGradingAttachment;
  private String rawScore;
  private String grade;
  private java.util.Date submissionDate;
  private java.util.Date submissionTime;
  private String image;
  private boolean hasImage;
  private String instructorMessage;
  private String courseName;
  private String timeLimit;
  private int timeLimit_hour;
  private int timeLimit_minute;
  private String timeLimitString;
  private ContentsDeliveryBean tableOfContents;
  private String submissionId;
  private String submissionMessage;
  private String instructorName;
  private ContentsDeliveryBean pageContents;
  private int submissionsRemaining;
  private int totalSubmissions;  
  private boolean forGrade;
  private String password;
  private int numberRetake;
  private int actualNumberRetake;
  private Map itemContentsMap;
  
  // For paging
  private int partIndex;
  private int questionIndex;
  private boolean next_page;
  private boolean reload = true;

  // daisy added these for SelectActionListener
  private boolean notTakeable = true;
  private boolean pastDue;
  private long subTime;
  private long raw;
  private String takenHours;
  private String takenMinutes;
  private AssessmentGradingData adata;
  private PublishedAssessmentFacade publishedAssessment;
  private java.util.Date feedbackDate;
  private String feedbackDelivery;
  private String showScore;
  private boolean hasTimeLimit;
  private boolean isMoreThanOneQuestion;
  private Integer scoringType;
  
  // daisyf added for servlet Login.java, to support anonymous login with
  // publishedUrl
  private boolean anonymousLogin = false;
  private String contextPath;

  // daisyf added this for timed assessment for SAK-6990, to check if mutiple windows were open 
  // during timed assessment
  private String timerId=null;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -1090852048737428722L;
  private boolean showStudentScore;
  private boolean showStudentQuestionScore;

  // SAM-387
  // esmiley added to track if timer has been started in timed assessments
  private boolean timeRunning;
  // SAM-535
  // esmiley added to track JavaScript
  private String javaScriptEnabledCheck;

  //cwent
  private String siteId;

  private boolean beginAssessment;

  // this instance tracks if the Agent is taking a test via URL, as well as
  // current agent string (if assigned). SAK-1927: esmiley
  private AgentFacade deliveryAgent;

  private String display_dayDateFormat= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_day_date_no_sec");
  private SimpleDateFormat dayDisplayFormat = new SimpleDateFormat(display_dayDateFormat, new ResourceLoader().getLocale());
  private String display_dateFormat= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_date_no_sec");
  private SimpleDateFormat displayFormat = new SimpleDateFormat(display_dateFormat, new ResourceLoader().getLocale());
  private boolean noQuestions = false;

  // this assessmentGradingId is used to generate seed in getSeed(...) of DeliveryActaionListener.java
  // We need this because the assessmentGradingData in delivery bean might not be the one we want to display
  // especially for review assessment and grade assessment. In other word, if student has started taking another assessment, 
  // the assessmentGradingData in deliver bean will be the newly created one. Then, of course, the assessmentGradingId 
  // will be the new id which is not what we want in review assessment or grade assessment
  private Long assessmentGradingId;
  
  private boolean fromTableOfContents;
  private int fileUploadSizeMax;
  private boolean studentRichText;
  
  private boolean isAnyInvalidFinInput;
  private String redrawAnchorName;
  
  // If set to true, delivery of the assessment is blocked.
  // This attribute is set to true if a secure delivery module is selected for the assessment and the security
  // check fails. If a module is selected, but is no longer installed or is disabled then the check is bypassed
  // and this attribute remains false.
  private boolean blockDelivery = false; 
  
  // HTML fragment injected by the secure delivery module selected. Nothing is injected if no module has
  // been selected or if the selected module is no longer installed or disabled.
  private String secureDeliveryHTMLFragment; 
  
  private boolean isFromPrint;
  
  private ExtendedTimeDeliveryService extendedTimeDeliveryService = null;

  private boolean showTimeWarning;
  private boolean hasShowTimeWarning;
  private boolean turnIntoTimedAssessment;
  private boolean submitFromTimeoutPopup;
  private boolean  skipFlag;
  private Date deadline;
  
  private boolean  firstTimeTaking;
  boolean timeExpired = false;
  
  private static String ACCESSBASE = ServerConfigurationService.getAccessUrl();
  private static String RECPATH = ServerConfigurationService.getString("samigo.recommendations.path"); 

  private static ResourceBundle eventLogMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.EventLogMessages");

  private static String questionProgressUnansweredPath = ServerConfigurationService.getString("samigo.questionprogress.unansweredpath", "/images/whiteBubble15.png");
  private static String questionProgressAnsweredPath = ServerConfigurationService.getString("samigo.questionprogress.answeredpath", "/images/blackBubble15.png");
  private static String questionProgressMardPath = ServerConfigurationService.getString("samigo.questionprogress.mardpath", "/images/questionMarkBubble15.png");
  
  /**
   * Creates a new DeliveryBean object.
   */
  public DeliveryBean()
  {
    deliveryAgent = new AgentFacade();
  }

  /**
   *
   *
   * @return
   */
  public int getItemIndex()
  {
    return this.itemIndex;
  }

  /**
   *
   *
   * @param itemIndex
   */
  public void setItemIndex(int itemIndex)
  {
    this.itemIndex = itemIndex;
  }

  /**
   *
   *
   * @return
   */
  public int getSize()
  {
    return this.size;
  }

  /**
   *
   *
   * @param size
   */
  public void setSize(int size)
  {
    this.size = size;
  }

  /**
   *
   *
   * @return
   */
  public String getAssessmentId()
  {
    return assessmentId;
  }

  /**
   *
   *
   * @param assessmentId
   */
  public void setAssessmentId(String assessmentId)
  {
    this.assessmentId = assessmentId;
  }

  /**
   * 
   * @return 
   */
  public String getReceiptEmailSetting()
  {
      return receiptEmailSetting;
  }

  /**
   * 
   * @param receiptEmailSetting 
   */
  public void setReceiptEmailSetting( String receiptEmailSetting )
  {
      this.receiptEmailSetting = receiptEmailSetting;
  }

  /**
   *
   *
   * @return
   */
  public List getMarkedForReview()
  {
    return markedForReview;
  }

  /**
   *
   *
   * @param markedForReview
   */
  public void setMarkedForReview(List markedForReview)
  {
    this.markedForReview = markedForReview;
  }

  /**
   *
   *
   * @return
   */
  public boolean getReviewMarked()
  {
    return this.reviewMarked;
  }

  /**
   *
   *
   * @param reviewMarked
   */
  public void setReviewMarked(boolean reviewMarked)
  {
    this.reviewMarked = reviewMarked;
  }

  /**
   *
   *
   * @return
   */
  public boolean getReviewAll()
  {
    return this.reviewAll;
  }

  /**
   *
   *
   * @param reviewAll
   */
  public void setReviewAll(boolean reviewAll)
  {
    this.reviewAll = reviewAll;
  }

  /**
   *
   *
   * @return
   */
  public String getAction()
  {
    return this.action;
  }

  /**
   *
   *
   * @param action
   */
  public void setAction(String action)
  {
    this.action = action;
  }

  /**
   *
   *
   * @return
   */
  public Date getBeginTime()
  {
    return beginTime;
  }

  
  public String getBeginTimeString() {
	  String beginTimeString = "";
	    if (beginTime == null) {
	      return beginTimeString;
	    }

	    try {
	      TimeUtil tu = new TimeUtil();
	      beginTimeString = tu.getDisplayDateTime(dayDisplayFormat, beginTime, true);
	    }
	    catch (Exception ex) {
	      // we will leave it as an empty string
	      log.warn("Unable to format date.", ex);
	    }
	    return beginTimeString;
  }
  
  /**
   *
   *
   * @param beginTime
   */
  public void setBeginTime(Date beginTime)
  {
    this.beginTime = beginTime;
  }

  /**
   *
   *
   * @return
   */
  public String getEndTime()
  {
    return endTime;
  }

  /**
   *
   *
   * @param endTime
   */
  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
  }

  /**
   *
   *
   * @return
   */
  public String getCurrentTime()
  {
    return this.currentTime;
  }

  /**
   *
   *
   * @param currentTime
   */
  public void setCurrentTime(String currentTime)
  {
    this.currentTime = currentTime;
  }

  /**
   *
   *
   * @return
   */
  public String getMultipleAttempts()
  {
    return this.multipleAttempts;
  }

  /**
   *
   *
   * @param multipleAttempts
   */
  public void setMultipleAttempts(String multipleAttempts)
  {
    this.multipleAttempts = multipleAttempts;
  }

  /**
   *
   *
   * @return
   */
  public String getTimeOutSubmission()
  {
    return this.timeOutSubmission;
  }

  /**
   *
   *
   * @param timeOutSubmission
   */
  public void setTimeOutSubmission(String timeOutSubmission)
  {
    this.timeOutSubmission = timeOutSubmission;
  }

  /**
   *
   *
   * @return
   */
  public java.util.Date getSubmissionTime()
  {
    return submissionTime;
  }

  /**
   *
   *
   * @param submissionTime
   */
  public void setSubmissionTime(java.util.Date submissionTime)
  {
    this.submissionTime = submissionTime;
  }

  /**
   *
   *
   * @return
   */
  public String getTimeElapse()
  {
    return timeElapse;
  }

  /**
   *
   *
   * @param timeElapse
   */
  public void setTimeElapse(String timeElapse)
  {
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
    }
    catch (Exception e){
      log.warn("setTimeElapse error:"+e.getMessage());
    }
  }

  /**
   *
   *
   * @return
   */
  public String getSubmissionTicket()
  {
    return submissionTicket;
  }

  /**
   *
   *
   * @param submissionTicket
   */
  public void setSubmissionTicket(String submissionTicket)
  {
    this.submissionTicket = submissionTicket;
  }

  /**
   *
   *
   * @return
   */
  public int getDisplayIndex()
  {
    return this.itemIndex + 1;
  }

  /**
   *
   *
   * @return
   */
  public String getAssessmentTitle()
  {
    return assessmentTitle;
  }

  /**
   *
   *
   * @param assessmentTitle
   */
  public void setAssessmentTitle(String assessmentTitle)
  {
    this.assessmentTitle = assessmentTitle;
  }

  /**
   *
   *
   * @return
   */
  public List getBlankItems()
  {
    return this.blankItems;
  }

  /**
   *
   *
   * @param blankItems
   */
  public void setBlankItems(List blankItems)
  {
    this.blankItems = blankItems;
  }

  /**
   *
   *
   * @return
   */
  public boolean getReviewBlank()
  {
    return reviewBlank;
  }

  /**
   *
   *
   * @param reviewBlank
   */
  public void setReviewBlank(boolean reviewBlank)
  {
    this.reviewBlank = reviewBlank;
  }

  /**
   *
   *
   * @return
   */
  public List getMarkedForReviewIdents()
  {
    return markedForReviewIdents;
  }

  /**
   *
   *
   * @param markedForReviewIdents
   */
  public void setMarkedForReviewIdents(List markedForReviewIdents)
  {
    this.markedForReviewIdents = markedForReviewIdents;
  }

  /**
   *
   *
   * @return
   */
  public List getBlankItemIdents()
  {
    return blankItemIdents;
  }

  /**
   *
   *
   * @param blankItemIdents
   */
  public void setBlankItemIdents(List blankItemIdents)
  {
    this.blankItemIdents = blankItemIdents;
  }

  /**
   *
   *
   * @return
   */
  public int getSectionIndex()
  {
    return this.sectionIndex;
  }

  /**
   *
   *
   * @param sectionIndex
   */
  public void setSectionIndex(int sectionIndex)
  {
    this.sectionIndex = sectionIndex;
  }

  /**
   *
   *
   * @return
   */
  public boolean getPrevious()
  {
    return previous;
  }

  /**
   *
   *
   * @param previous
   */
  public void setPrevious(boolean previous)
  {
    this.previous = previous;
  }

  //Settings
  public String getQuestionLayout()
  {
      if(getSettings().isFormatByQuestion()) {
          questionLayout = "1";
      }
      else if (getSettings().isFormatByPart()) {
          questionLayout = "2";
      }
      else if (getSettings().isFormatByAssessment()) {
          questionLayout = "3";
      }

    return questionLayout;
  }

  /**
   *
   *
   * @param questionLayout
   */
  public void setQuestionLayout(String questionLayout)
  {
    this.questionLayout = questionLayout;
  }

  /**
   *
   *
   * @return
   */
  public String getNavigation()
  {
    return navigation;
  }

  /**
   *
   *
   * @param navigation
   */
  public void setNavigation(String navigation)
  {
    this.navigation = navigation;
  }

  /**
   *
   *
   * @return
   */
  public String getNumbering()
  {
    return numbering;
  }

  /**
   *
   *
   * @param numbering
   */
  public void setNumbering(String numbering)
  {
    this.numbering = numbering;
  }

  /**
   *
   *
   * @return
   */
  public String getFeedback()
  {
    return feedback;
  }

  /**
   *
   *
   * @param feedback
   */
  public void setFeedback(String feedback)
  {
    this.feedback = feedback;
  }

  /**
   *
   *
   * @return
   */
  public String getNoFeedback()
  {
    return noFeedback;
  }

  /**
   *
   *
   * @param noFeedback
   */
  public void setNoFeedback(String noFeedback)
  {
    this.noFeedback = noFeedback;
  }

  /**
   *
   *
   * @return
   */
  public String getStatistics()
  {
    return statistics;
  }

  /**
   *
   *
   * @param statistics
   */
  public void setStatistics(String statistics)
  {
    this.statistics = statistics;
  }

  /**
   *
   *
   * @return
   */
  public FeedbackComponent getFeedbackComponent()
  {
    return feedbackComponent;
  }

  /**
   *
   *
   * @param feedbackComponent
   */
  public void setFeedbackComponent(FeedbackComponent feedbackComponent)
  {
    this.feedbackComponent = feedbackComponent;
  }

  public String getFeedbackComponentOption()
  {
    return feedbackComponentOption;
  }

  public void setFeedbackComponentOption(String feedbackComponentOption)
  {
    this.feedbackComponentOption = feedbackComponentOption;
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
  public SettingsDeliveryBean getSettings()
  {
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
      }
      else {
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

  /**
   * @param settings
   */
  public void setSettings(SettingsDeliveryBean settings)
  {
    this.settings = settings;
  }

  /**
   * @return
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }

  /**
   * @param string
   */
  public void setErrorMessage(String string)
  {
    errorMessage = string;
  }

  /**
   * @return
   */
  public String getDuration()
  {
    return duration;
  }

  /**
   * @param string
   */
  public void setDuration(String string)
  {
    duration = string;
  }

  /**
   * @return
   */
  public String getCreatorName()
  {
    return creatorName;
  }

  /**
   * @param string
   */
  public void setCreatorName(String string)
  {
    creatorName = string;
  }

  public java.util.Date getDueDate()
  {
    return dueDate;
  }

  public String getDueDateString()
  {
    String dateString = "";
    if (dueDate == null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getDisplayDateTime(displayFormat, dueDate, true);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }
  
  public String getDayDueDateString()
  {
    String dateString = "";
    if (dueDate == null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getDisplayDateTime(dayDisplayFormat, dueDate, true);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }
  
  public void setDueDate(java.util.Date dueDate)
  {
    this.dueDate = dueDate;
  }
  
  public Date getAdjustedTimedAssesmentDueDate() {
	  return adjustedTimedAssesmentDueDate;
  }
  
  public String getAdjustedTimedAssesmentDueDateString () {
	  String adjustedTimedAssesmentDueDateString = "";
	    if (adjustedTimedAssesmentDueDate == null) {
	      return adjustedTimedAssesmentDueDateString;
	    }

	    try {
	      TimeUtil tu = new TimeUtil();
	      adjustedTimedAssesmentDueDateString = tu.getDisplayDateTime(dayDisplayFormat, adjustedTimedAssesmentDueDate, true);
	    }
	    catch (Exception ex) {
	      // we will leave it as an empty string
	      log.warn("Unable to format date.", ex);
	    }
	    return adjustedTimedAssesmentDueDateString;
  }
  
  public void setAdjustedTimedAssesmentDueDate (Date adjustedTimedAssesmentDueDate) {
	  this.adjustedTimedAssesmentDueDate = adjustedTimedAssesmentDueDate;
  }
  
  public java.util.Date getRetractDate()
  {
    return retractDate;
  }

  public String getDayRetractDateString()
  {
    String dateString = "";
    if (retractDate == null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getDisplayDateTime(dayDisplayFormat, retractDate, true);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }
  
  public void setRetractDate(java.util.Date retractDate)
  {
    this.retractDate = retractDate;
  }
  
  public boolean isStatsAvailable()
  {
    return statsAvailable;
  }

  public void setStatsAvailable(boolean statsAvailable)
  {
    this.statsAvailable = statsAvailable;
  }

  public boolean isSubmitted()
  {
    return submitted;
  }

  public void setSubmitted(boolean submitted)
  {
    this.submitted = submitted;
  }

  public boolean isGraded()
  {
    return graded;
  }

  public void setGraded(boolean graded)
  {
    this.graded = graded;
  }

  public boolean getFeedbackOnDate()
  {
    return feedbackOnDate;
  }

  public void setFeedbackOnDate(boolean feedbackOnDate)
  {
    this.feedbackOnDate = feedbackOnDate;
  }

  public String getGraderComment()
  {
    if (graderComment == null)
    {
      return "";
    }
    return graderComment;
  }

  public void setGraderComment(String newComment)
  {
    graderComment = newComment;
  }
  
  public List<AssessmentGradingAttachment> getAssessmentGradingAttachmentList() {
	return assessmentGradingAttachmentList;
  }
  
  public void setAssessmentGradingAttachmentList(
		List<AssessmentGradingAttachment> assessmentGradingAttachmentList) {
	this.assessmentGradingAttachmentList = assessmentGradingAttachmentList;
  }
  
  public boolean isHasAssessmentGradingAttachment() {
	return hasAssessmentGradingAttachment;
  }
  
  public void setHasAssessmentGradingAttachment(boolean hasAssessmentGradingAttachment) {
	this.hasAssessmentGradingAttachment = hasAssessmentGradingAttachment;
  }

  public String getRawScore()
  {
    return rawScore;
  }

  public String getRoundedRawScore() {
   try {
      String newscore= ContextUtil.getRoundedValue(rawScore, 2);
      return Validator.check(newscore, "N/A");
    }
    catch (Exception e) {
      // encountered some weird number format/locale
      return Validator.check(rawScore, "0");
    }

  }
  
  public String getRoundedRawScoreViaURL() {
	  if (adata.getFinalScore() != null){
		  rawScore = adata.getFinalScore().toString();
	  }
	  else {
		  rawScore = "0";
	  }

	  try {
		  String newscore= ContextUtil.getRoundedValue(rawScore, 2);
		  return Validator.check(newscore, "N/A");
	  }
	  catch (Exception e) {
		  // encountered some weird number format/locale
		  return Validator.check(rawScore, "0");
	  }
  }


  public void setRawScore(String rawScore)
  {
    this.rawScore = rawScore;
  }

  public long getRaw()
  {
    return raw;
  }

  public void setRaw(long raw)
  {
    this.raw = raw;
  }

  public String getGrade()
  {
    return grade;
  }

  public void setGrade(String grade)
  {
    this.grade = grade;
  }

  public java.util.Date getSubmissionDate()
  {
    return submissionDate;
  }

  public String getSubmissionDateString()
  {
    String dateString = "";
    if (submissionDate== null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getDisplayDateTime(displayFormat, submissionDate, true);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }

  public void setSubmissionDate(java.util.Date submissionDate)
  {
    this.submissionDate = submissionDate;
  }

  public String getImage()
  {
    return image;
  }

  public void setImage(String image)
  {
    this.image = image;
  }

  public boolean isHasImage()
  {
    return hasImage;
  }

  public void setHasImage(boolean hasImage)
  {
    this.hasImage = hasImage;
  }

  public String getInstructorMessage()
  {
    return instructorMessage;
  }

  public void setInstructorMessage(String instructorMessage)
  {
    this.instructorMessage = instructorMessage;
  }

  public String getCourseName()
  {
    return courseName;
  }

  public void setCourseName(String courseName)
  {
    this.courseName = courseName;
  }

  public String getTimeLimit()
  {
    return timeLimit;
  }

  public void setTimeLimit(String timeLimit)
  {
	  this.timeLimit = timeLimit;
  }

  public int getTimeLimit_hour()
  {
    return timeLimit_hour;
  }

  public void setTimeLimit_hour(int timeLimit_hour)
  {
    this.timeLimit_hour = timeLimit_hour;
  }

  public int getTimeLimit_minute()
  {
    return timeLimit_minute;
  }

  public void setTimeLimit_minute(int timeLimit_minute)
  {
    this.timeLimit_minute = timeLimit_minute;
  }
  
  public String getTimeLimitString()
  {
    return timeLimitString;
  }

  public void setTimeLimitString(String timeLimitString)
  {
    this.timeLimitString = timeLimitString;
  }
 

  /**
   * Bean with table of contents information and
   * a list of all the sections in the assessment
   * which in  turn has a list of all the item contents.
   * @return table of contents
   */
  public ContentsDeliveryBean getTableOfContents()
  {
    return tableOfContents;
  }

  /**
   * Bean with table of contents information and
   * a list of all the sections in the assessment
   * which in  turn has a list of all the item contents.
   * @param tableOfContents table of contents
   */
  public void setTableOfContents(ContentsDeliveryBean tableOfContents)
  {
    this.tableOfContents = tableOfContents;
  }

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
   * @return ContentsDeliveryBean
   */
  public ContentsDeliveryBean getPageContents()
  {
    return pageContents;
  }

  /**
   * Bean with a list of all the sections in the current page
   * which in turn has a list of all the item contents for the page.
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
   * @param pageContents ContentsDeliveryBean
   */
  public void setPageContents(ContentsDeliveryBean pageContents)
  {
    this.pageContents = pageContents;
  }

  public String getSubmissionId()
  {
    return submissionId;
  }

  public void setSubmissionId(String submissionId)
  {
    this.submissionId = submissionId;
  }

  public String getSubmissionMessage()
  {
    return submissionMessage;
  }

  public void setSubmissionMessage(String submissionMessage)
  {
    this.submissionMessage = submissionMessage;
  }

  public int getSubmissionsRemaining()
  {
    return submissionsRemaining;
  }

  public void setSubmissionsRemaining(int submissionsRemaining)
  {
    this.submissionsRemaining = submissionsRemaining;
  }
  
  public int getTotalSubmissions()
  {
    return totalSubmissions;
  }

  public void setTotalSubmissions(int totalSubmissions)
  {
    this.totalSubmissions = totalSubmissions;
  }

  public String getInstructorName()
  {
    return instructorName;
  }

  public void setInstructorName(String instructorName)
  {
    this.instructorName = instructorName;
  }

  public boolean getForGrade()
  {
    return forGrade;
  }

  public void setForGrade(boolean newfor)
  {
    forGrade = newfor;
  }

  public String submitForGradeFromTimer()
  {
	    return submitForGrade(true, false);
  }
  
  public String submitForGrade()
  {
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
	  }
	  else {
		  setSubmitFromTimeoutPopup(false);
	  }
	  
	  SessionUtil.setSessionTimeout(FacesContext.getCurrentInstance(), this, false);

	  syncTimeElapsedWithServer();
	  
	  SubmitToGradingActionListener listener = new SubmitToGradingActionListener();
	  // submission remaining and totalSubmissionPerAssessmentHash is updated inside 
	  // SubmitToGradingListener
	  try {
		  listener.processAction(null);
	  }
	  catch (FinFormatException | SaLengthException e) {
		  log.debug(e.getMessage());
		  return "takeAssessment";
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

	  if (!isFromTimer) {
		  if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) // this is for accessing via published url
		  {
			  returnValue="anonymousThankYou";
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_VIA_URL, resource, siteId, true, NotificationService.NOTI_REQUIRED));
		  }
		  else {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED, resource, true));
		  }
	  }
	  else {
		  if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) // this is for accessing via published url
		  {
			  returnValue="anonymousThankYou";
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_VIA_URL, resource , siteId, true, NotificationService.NOTI_REQUIRED));
		  }
		  else {
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
		  		  	
		  String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
		  eventLogData.setIpAddress(thisIp);
		  		  
	 	  eventLogFacade.setData(eventLogData);
	 	  eventService.saveOrUpdateEventLog(eventLogFacade);
	  }
	  notificationValues.put("assessmentGradingID", local_assessmentGradingID);
	  notificationValues.put("userID", adata.getAgentId());
	  notificationValues.put("submissionDate", getSubmissionDateString());
	  notificationValues.put("publishedAssessmentID", adata.getPublishedAssessmentId());
	  notificationValues.put("confirmationNumber", confirmation);
	  
      String [] releaseToGroups = getReleaseToGroups();
      if (releaseToGroups != null){
          notificationValues.put("releaseToGroups", Arrays.stream(releaseToGroups)
                                                          .collect(Collectors.joining(";")));
      }	  

	  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_NOTI, notificationValues.toString(), AgentFacade.getCurrentSiteId(), true, SamigoConstants.NOTI_EVENT_ASSESSMENT_SUBMITTED));
 
	  return returnValue;
	  }
	  catch(Exception e) {
		  EventLogService eventService = new EventLogService();
		  EventLogFacade eventLogFacade = new EventLogFacade();
		  EventLogData eventLogData;

		  List eventLogDataList = eventService.getEventLogData(adata.getAssessmentGradingId());
		  if(eventLogDataList != null && eventLogDataList.size() > 0) {
			  eventLogData= (EventLogData) eventLogDataList.get(0);
			  eventLogData.setErrorMsg(eventLogMessages.getString("error_submit"));
			  eventLogData.setEndDate(new Date());
			  			  
			  String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
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
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
	  {
		  if (adata != null) {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_FROM_LASTPAGE, "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + adata.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));
		  }
		  else {
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
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
	  {
		  syncTimeElapsedWithServer();

		  SubmitToGradingActionListener listener =
			  new SubmitToGradingActionListener();
		  try {
			  listener.processAction(null);
		  }
		  catch (FinFormatException | SaLengthException fine) {
			  log.debug(fine.getMessage());
			  return "takeAssessment";
		  }
	  }

	  skipFlag = true;
	  DeliveryActionListener l2 = new DeliveryActionListener();
	  l2.processAction(null);
	  return "confirmsubmit";
  }
  
  public String confirmSubmitTOC()
  {
	  if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
	  {
		  if (adata != null) {
			  EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_FROM_TOC, "siteId=" + AgentFacade.getCurrentSiteId() + ", submissionId=" + adata.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));
		  }
		  else {
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
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
	  {
		  syncTimeElapsedWithServer();
		  SubmitToGradingActionListener listener =
			  new SubmitToGradingActionListener();
		  try {
			  listener.processAction(null);
		  }
		  catch (FinFormatException | SaLengthException e) {
			  log.debug(e.getMessage());
			  return "takeAssessment";
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
  
  public String saveNoCheck() {
	  return saveAndExit(false);
  }
  
  public String saveAndExit(boolean needToCheck)
  {
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
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    {
    	syncTimeElapsedWithServer();
    	SubmitToGradingActionListener listener =
    		new SubmitToGradingActionListener();
    	try {
    		listener.processAction(null);
    	}
    	catch (FinFormatException | SaLengthException e) {
  		  log.debug(e.getMessage());
  		  return "takeAssessment";
    	}
    }
    
    String returnValue;
    if (needToCheck) {
    	if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
    		returnValue = "anonymousQuit";
    	}
    	else {
    		returnValue = "saveForLaterWarning";
    	}
    }
    else {
    	if (this.actionMode == TAKE_ASSESSMENT_VIA_URL) {
    		returnValue = "notSubmitted";
    	}
    	else {
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
  
  public String next_page()
  {

   return next_helper(false);
  }

  public String goto_question()
  {
    return next_helper(true);
  }


  private String next_helper(boolean isGoToQuestion)
  {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
            || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    {
      syncTimeElapsedWithServer();

      SubmitToGradingActionListener listener =
              new SubmitToGradingActionListener();
      try {
        listener.processAction(null);
      }
      catch (FinFormatException | SaLengthException e) {
		  log.debug(e.getMessage());
		  return "takeAssessment";
	  }
    }

    int oPartIndex = partIndex;
    int oQuestionIndex = questionIndex;

    if (getSettings().isFormatByPart())
    {
      String partIndexString = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("partnumber");
      if(isGoToQuestion) {
        partIndex = Integer.parseInt(partIndexString);
      } else {
        partIndex++;
      }
    }
    if (getSettings().isFormatByQuestion())
    {
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

  public String same_page()
  {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
        || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    {
      syncTimeElapsedWithServer();
        	
      SubmitToGradingActionListener listener =
        new SubmitToGradingActionListener();
      try {
    	  listener.processAction(null);
      }
	  catch (FinFormatException | SaLengthException e) {
		  log.debug(e.getMessage());
		  return "takeAssessment";
	  }
    }

    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    reload = false;
    return "takeAssessment";
  }
  
  public String auto_save()
  {
	  skipFlag = true;
	  return save_work();
  }
  
  public String save_work()
  {

	  String nextAction = checkBeforeProceed();
	  log.debug("***** next Action={}", nextAction);
	  if (!("safeToProceed").equals(nextAction)){
		  return nextAction;
	  }
	  
	  forGrade = false;

	  if (this.actionMode == TAKE_ASSESSMENT
			  || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
	  {
		  syncTimeElapsedWithServer();
		  SubmitToGradingActionListener listener =
			  new SubmitToGradingActionListener();
		  try {
			  listener.processAction(null);
		  }
		  catch (FinFormatException | SaLengthException e) {
			  log.debug(e.getMessage());
			  return "takeAssessment";
		  }
	  }
	  
	  DeliveryActionListener l2 = new DeliveryActionListener();
	  l2.processAction(null);

	  reload = false;
	  return "takeAssessment";
  }

  public String previous()
  {
    String nextAction = checkBeforeProceed();
    log.debug("***** next Action={}", nextAction);
    if (!("safeToProceed").equals(nextAction)){
      return nextAction;
    }

    if (getSettings().isFormatByPart())
    {
      partIndex--;
      questionIndex = 0;
    }
    if (getSettings().isFormatByQuestion())
    {
      questionIndex--;

    }
    forGrade = false;

    if (this.actionMode == TAKE_ASSESSMENT
        || this.actionMode == TAKE_ASSESSMENT_VIA_URL)
    {
      syncTimeElapsedWithServer();
      SubmitToGradingActionListener listener =
        new SubmitToGradingActionListener();
      try {
    	  listener.processAction(null);
      }
	  catch (FinFormatException | SaLengthException e) {
		  log.debug(e.getMessage());
		  return "takeAssessment";
	  }
    }
    
    DeliveryActionListener l2 = new DeliveryActionListener();
    l2.processAction(null);

    reload = false;
    return "takeAssessment";
  }

  public String confirmSubmitPrevious()
  {
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

  // this is the PublishedAccessControl.finalPageUrl
  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getConfirmation()
  {
    return confirmation;
  }

  public void setConfirmation(String confirmation)
  {
    this.confirmation = confirmation;
  }

  /**
   * if required, assessment password
   * @return password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * if required, assessment password
   * @param string assessment password
   */
  public void setPassword(String string)
  {
    password = string;
  }

  public String validatePassword()
  {
    log.debug("**** password={}", password);
    log.debug("**** setting password={}", getSettings().getPassword());
    
    if (StringUtils.isBlank(password))
    {
    	return "passwordAccessError";
    }
    if(StringUtils.isNotBlank(getSettings().getPassword()))
    {
    	if (!StringUtils.equals(StringUtils.trim(password), StringUtils.trim(getSettings().getPassword())))
    	{
    		return "passwordAccessError";
    	}
    }

	// in post 2.1, clicking at Begin Assessment takes users to the
	// 1st question.
	return "takeAssessment";

  }


  public String validateIP()
  {
    String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.
                     getCurrentInstance().getExternalContext().getRequest()).
      getRemoteAddr();
    Iterator addresses = getSettings().getIpAddresses().iterator();
    while (addresses.hasNext())
    {
      String next = ( (PublishedSecuredIPAddress) addresses.next()).
        getIpAddress();
      if (next != null && next.contains( "*" ))
      {
        next = next.substring(0, next.indexOf("*"));
      }
      if (next == null || next.trim().equals("") ||
          thisIp.trim().startsWith(next.trim()))
      {
        // in post 2.1, clicking at Begin Assessment takes users to the
        // 1st question.
        return "takeAssessment";
      }
    }
    
    return "ipAccessError";
  }

  public String validate()
  {
    try
    {
      String results = "takeAssessment";
      EventLogService eventService = new EventLogService();
      EventLogFacade eventLogFacade = new EventLogFacade();
      EventLogData eventLogData = new EventLogData();
      
      // #1. check password
      if (!getSettings().getPassword().equals(""))
      {
        results = validatePassword();
        log.debug("*** checked password={}", results);
        
        if("passwordAccessError".equals(results)) {
        	updatEventLog("error_pw_access");   	
        }
      }

      // #2. check IP
      if (!"passwordAccessError".equals(results) &&
          getSettings().getIpAddresses() != null &&
          !getSettings().getIpAddresses().isEmpty())
      {
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
              }
    		  else {
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
      if ("".equals(results))
      {
        // in post 2.1, clicking at Begin Assessment takes users to the
        // 1st question.
        return "takeAssessment";
      }
      return results;
    } catch (Exception e)
    {
    	log.error("accessError{}", e.getMessage());
      EventLogService eventService = new EventLogService();
		 EventLogFacade eventLogFacade = new EventLogFacade();
		 EventLogData eventLogData = null;

		 List eventLogDataList = eventService.getEventLogData(adata.getAssessmentGradingId());
		 if(eventLogDataList != null && eventLogDataList.size() > 0) {
			 eventLogData= (EventLogData) eventLogDataList.get(0);
			 eventLogData.setErrorMsg(eventLogMessages.getString("error_access"));
		 }
		 
		 String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
		 eventLogData.setIpAddress(thisIp);
		 
		 eventLogFacade.setData(eventLogData);
		 eventService.saveOrUpdateEventLog(eventLogFacade);
      return "accessError";
    }
  }

  public void updatEventLog(String errorMsg)
  {
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
	  eventLogData.setTitle(publishedAssessment.getTitle());
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
	  	  
	  String thisIp = ( (javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
	  eventLogData.setIpAddress(thisIp);
	  	  
	  eventLogFacade.setData(eventLogData);    
	  eventService.saveOrUpdateEventLog(eventLogFacade);        	
  }
  
  public String pvalidate()
  {
    // in post 2.1, clicking at Begin Assessment takes users to the
    // 1st question.
    return "takeAssessment";
  }

  // Skipped paging methods
  public int getPartIndex()
  {
    return partIndex;
  }

  public void setPartIndex(int newindex)
  {
    partIndex = newindex;
  }

  public int getQuestionIndex()
  {
    return questionIndex;
  }

  public void setQuestionIndex(int newindex)
  {
    questionIndex = newindex;
  }

  public boolean getDoContinue()
  {
    return next_page;
  }

  public void setContinue(boolean docontinue)
  {
    next_page = docontinue;
  }

  public boolean getReload()
  {
    return reload;
  }

  public void setReload(boolean doreload)
  {
    reload = doreload;
  }

  // Store for paging
  public AssessmentGradingData getAssessmentGrading()
  {
    return adata;
  }

  public void setAssessmentGrading(AssessmentGradingData newdata)
  {
    adata = newdata;
  }

  private byte[] getMediaStream(String mediaLocation)
  {
    byte[] mediaByte = new byte[0];
    try
    {
      mediaByte = Files.readAllBytes(new File(mediaLocation).toPath());
    }
    catch (FileNotFoundException ex)
    {
      log.error("File not found in DeliveryBean.getMediaStream(): {}", ex.getMessage());
    }
    catch (IOException ex)
    {
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
  public void addMediaToItemGrading(javax.faces.event.ValueChangeEvent e)
  {
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
  public String addMediaToItemGrading(String mediaLocation)
    {
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
    //int assessmentIndex = mediaLocation.indexOf("assessment");
    int questionIndex = mediaLocation.indexOf("question");
    int agentIndex = mediaLocation.indexOf("/", questionIndex + 8);
    int myfileIndex = mediaLocation.lastIndexOf("/");
    //cwen
    if(agentIndex < 0 )
    {
      agentIndex = mediaLocation.indexOf("\\", questionIndex + 8);
    }
    //String pubAssessmentId = mediaLocation.substring(assessmentIndex + 10, questionIndex - 1);
    String questionId = mediaLocation.substring(questionIndex + 8, agentIndex);
    log.debug("***3a. addMediaToItemGrading, questionId ={}", questionId);
    log.debug("***3b. addMediaToItemGrading, assessmentId ={}", assessmentId);
    if (agent == null){
      String agentId = mediaLocation.substring(agentIndex, myfileIndex -1);
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
    if (itemGradingData == null)
    {
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
    return "takeAssessment"; // which doesn't exists to force it to reload

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

    
    if (SAVETODB)
    { // put the byte[] in
      byte[] mediaByte = getMediaStream(mediaLocation);
      mediaData = new MediaData(itemGradingData, mediaByte,
                                Long.valueOf(mediaByte.length + ""),
                                mimeType, "description", null,
                                updatedFilename, false, false, 1,
                                agent, new Date(),
                                agent, new Date(), null);
    }
    else
    { // put the location in
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
    try{
      	if (SAVETODB) {
		boolean success = media.delete();
    		if (!success){
      		log.warn("Error: media.delete() failed for mediaId ={}", mediaId);
		}
  	}


    }
    catch(Exception e){
      log.warn(e.getMessage());
    }
  }


  public boolean mediaIsValid()
  {
    boolean returnValue =true;
    // check if file is too big
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext external = context.getExternalContext();
    Long fileSize = (Long)((ServletContext)external.getContext()).getAttribute("TEMP_FILEUPLOAD_SIZE");
    Long maxSize = (Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX");

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

  public boolean getNotTakeable()
  {
    return notTakeable;
  }

  public void setNotTakeable(boolean notTakeable)
  {
    this.notTakeable = notTakeable;
  }

  public boolean getPastDue()
  {
    return pastDue;
  }

  public void setPastDue(boolean pastDue)
  {
    this.pastDue = pastDue;
  }

  public long getSubTime()
  {
    return subTime;
  }

  public void setSubTime(long newSubTime)
  {
    subTime = newSubTime;
  }

  public String getSubmissionHours()
  {
    return takenHours;
  }

  public void setSubmissionHours(String newHours)
  {
    takenHours = newHours;
  }

  public String getSubmissionMinutes()
  {
    return takenMinutes;
  }

  public void setSubmissionMinutes(String newMinutes)
  {
    takenMinutes = newMinutes;
  }

  public PublishedAssessmentFacade getPublishedAssessment()
  {
    return publishedAssessment;
  }

  public void setPublishedAssessment(PublishedAssessmentFacade publishedAssessment)
  {
	  this.publishedAssessment = publishedAssessment;
	  //Setup extendedTimeDeliveryService
	  if (extendedTimeDeliveryService == null && 
			  (publishedAssessment != null && publishedAssessment.getPublishedAssessmentId() != null)) {
		  extendedTimeDeliveryService = new ExtendedTimeDeliveryService(publishedAssessment);
	  }
  }

  public java.util.Date getFeedbackDate()
  {
    return feedbackDate;
  }

  public String getFeedbackDateString()
  {
    String dateString = "";
    if (feedbackDate== null) {
      return dateString;
    }

    try {
      TimeUtil tu = new TimeUtil();
      dateString = tu.getDisplayDateTime(displayFormat, feedbackDate, true);
    }
    catch (Exception ex) {
      // we will leave it as an empty string
      log.warn("Unable to format date.", ex);
    }
    return dateString;
  }

  public void setFeedbackDate(java.util.Date feedbackDate)
  {
    this.feedbackDate = feedbackDate;
  }

  public String getFeedbackDelivery()
  {
    return feedbackDelivery;
  }

  public void setFeedbackDelivery(String feedbackDelivery)
  {
    this.feedbackDelivery = feedbackDelivery;
  }

  public String getShowScore()
  {
    return showScore;
  }

  public void setShowScore(String showScore)
  {
    this.showScore = showScore;
  }

  public boolean getHasTimeLimit()
  {
    return hasTimeLimit;
  }

  public void setHasTimeLimit(boolean hasTimeLimit)
  {
    this.hasTimeLimit = hasTimeLimit;
  }

  public String getOutcome()
  {
    return outcome;
  }

  public void setOutcome(String outcome)
  {
    this.outcome = outcome;
  }

  public String doit()
  {
    return outcome;
  }

  public boolean getAnonymousLogin()
  {
    return anonymousLogin;
  }

  public void setAnonymousLogin(boolean anonymousLogin)
  {
    this.anonymousLogin = anonymousLogin;
  }

  public ItemGradingData getItemGradingData(String publishedItemId)
  {
    ItemGradingData selected = null;
    if (adata != null)
    {
      Set items = adata.getItemGradingSet();
      if (items != null)
      {
        Iterator iter = items.iterator();
        while (iter.hasNext())
        {
          ItemGradingData itemGradingData = (ItemGradingData) iter.next();
          String itemPublishedId = itemGradingData.getPublishedItemId().
            toString();
          if ( (publishedItemId).equals(itemPublishedId))
          {
            log.debug("*** addMediaToItemGrading, same : found it");
            selected = itemGradingData;
          }
          else
          {
            log.debug("*** addMediaToItemGrading, not the same");
          }
        }
        log.debug("*** addMediaToItemGrading, publishedItemId ={}", publishedItemId);
        if (selected != null)
        {
          log.debug("*** addMediaToItemGrading, itemGradingData.publishedItemId ={}", selected.getPublishedItemId());
        }
      }
    }
    return selected;
  }

  public String getContextPath()
  {
    return contextPath;
  }

  public void setContextPath(String contextPath)
  {
    this.contextPath = contextPath;
  }

  public boolean isShowStudentScore()
  {
    return showStudentScore;
  }

  public void setShowStudentScore(boolean showStudentScore)
  {
    this.showStudentScore = showStudentScore;
  }

  public boolean isShowStudentQuestionScore()
  {
    return showStudentQuestionScore;
  }

  public void setShowStudentQuestionScore(boolean param)
  {
    this.showStudentQuestionScore = param;
  }


  public boolean isTimeRunning()
  {
    return timeRunning;
  }

  public void setTimeRunning(boolean timeRunning)
  {
    this.timeRunning = timeRunning;
  }

  /**
   * Used for a JavaScript enable check.
   * @return 
   */
  public String getJavaScriptEnabledCheck()
  {
    return this.javaScriptEnabledCheck;
  }

  /**
   * Used for a JavaScript enable check.
   * @param javaScriptEnabledCheck
   */
  public void setJavaScriptEnabledCheck(String javaScriptEnabledCheck)
  {
    this.javaScriptEnabledCheck = javaScriptEnabledCheck;
  }
  //cwen
  public void setSiteId(String siteId)
  {
    this.siteId = siteId;
  }

  public String getSiteId()
  {
    siteId = null;
    Placement currentPlacement = ToolManager.getCurrentPlacement();
    if(currentPlacement != null)
      siteId = currentPlacement.getContext();
    return siteId;
  }

  public String getAgentAccessString()
  {
    return deliveryAgent.getAgentInstanceString();
  }
  public void setAgentAccessString(String agentString)
  {
    deliveryAgent.setAgentInstanceString(agentString);
  }

  public boolean getSaveToDb(){
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext external = context.getExternalContext();
    String saveToDb = (String)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SAVE_MEDIA_TO_DB");
    return ("true").equals(saveToDb);
  }

  public void attachToItemContentBean(ItemGradingData itemGradingData, String questionId){
    List list = new ArrayList();
    list.add(itemGradingData);
    //find out sectionId from questionId
    log.debug("**** attachToItemContentBean, questionId={}", questionId);
    /*
    PublishedAssessmentService publishedService = new
  PublishedAssessmentService();
    PublishedItemData publishedItem = publishedService.
  loadPublishedItem(questionId);
    */
    PublishedItemData publishedItem = (PublishedItemData) getPublishedItemHash().get(new Long(questionId));
    PublishedSectionData publishedSection = (PublishedSectionData) publishedItem.getSection();
    String sectionId = publishedSection.getSectionId().toString();
    SectionContentsBean partSelected = null;

    //get all partContents
    List parts = getPageContents().getPartsContents();
    for (int i=0; i<parts.size(); i++){
      SectionContentsBean part = (SectionContentsBean)parts.get(i);
      log.debug("**** question's sectionId{}", sectionId);
      log.debug("**** partId{}", part.getSectionId());
      if (sectionId.equals(part.getSectionId())){
        partSelected = part;
        break;
      }
    }
    //locate the itemContentBean - the hard way, sigh...
    List items = new ArrayList();
    if (partSelected!=null)
      items = partSelected.getItemContents();
    for (int j=0; j<items.size(); j++){
      ItemContentsBean item = (ItemContentsBean)items.get(j);
      if ((publishedItem.getItemId()).equals(item.getItemData().getItemId())){ // comparing itemId not object
        item.setItemGradingDataArray(list);
        break;
      }
    }
  }

  // delivery action
  public static final int TAKE_ASSESSMENT = 1;
  public static final int PREVIEW_ASSESSMENT = 2;
  public static final int REVIEW_ASSESSMENT = 3;
  public static final int GRADE_ASSESSMENT = 4;
  public static final int TAKE_ASSESSMENT_VIA_URL = 5;
  private int actionMode;
  private String actionString;

  public void setActionString(String actionString){
    this.actionString = actionString;
    // the follwoing two values will be evaluated when reviewing assessment
    // based on PublishedFeedback settings
    setFeedback("false");
    setNoFeedback("true");

    if (null != actionString)
    {
        switch( actionString )
        {
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

  public String getActionString(){
    return actionString;
  }

  private void setActionMode(int actionMode){
    this.actionMode = actionMode;
  }

  public int getActionMode(){
    return actionMode;
  }

  private long time=0;
  public void setLastTimer(long time){
    this.time = time;
  }

  public long getLastTimer(){
    return time;
  }

  public boolean getBeginAssessment(){
    return beginAssessment;
  }

  public void setBeginAssessment(boolean beginAssessment){
    this.beginAssessment = beginAssessment;
  }

  public boolean getTimeExpired(){
    if (adata == null) {
    	return false;
    }
    return timeExpired;
  }
  
  public void setTimeExpired(Boolean timeExpired) {
	  this.timeExpired = timeExpired;
  }

  private void removeTimedAssessmentFromQueue(){
    if (adata==null) {
      return;
    }
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)queue.
                                             get(adata.getAssessmentGradingId());
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
	        GradingService gradingService = new GradingService();
	        gradingService.saveOrUpdateAssessmentGrading(adata);
	        setTimeElapse(adata.getTimeElapsed().toString());
	      }
	    }
	    else{ 
	      // if we are in other mode, timer need not be accurate
	      // Anyway, we don't have adata, so we haven't been using the TimerTask to keep track of it.
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
		    else{ 
		      // if we are in other mode, timer need not be accurate
		      // Anyway, we don't have adata, so we haven't been using the TimerTask to keep track of it.
		    }
	  }

  private String timeElapseAfterFileUpload;
  public String getTimeElapseAfterFileUpload()
  {
    return timeElapseAfterFileUpload;
  }
  public void setTimeElapseAfterFileUpload(String timeElapseAfterFileUpload)
  {
    this.timeElapseAfterFileUpload = timeElapseAfterFileUpload;
    if (timeElapseAfterFileUpload!=null && !("").equals(timeElapseAfterFileUpload))
      setTimeElapseAfterFileUploadDouble(( Double.valueOf(timeElapseAfterFileUpload)));
  }

  private double timeElapseDouble=0;
  public double getTimeElapseDouble()
  {
    return timeElapseDouble;
  }
  public void setTimeElapseDouble(double timeElapseDouble)
  {
    this.timeElapseDouble = timeElapseDouble;
  }

  private double timeElapseAfterFileUploadDouble;
  public double getTimeElapseAfterFileUploadDouble()
  {
    return timeElapseAfterFileUploadDouble;
  }
  public void setTimeElapseAfterFileUploadDouble(double timeElapseAfterFileUploadDouble)
  {
    this.timeElapseAfterFileUploadDouble = timeElapseAfterFileUploadDouble;
  }

  private String protocol;
  public String getProtocol(){
    return protocol;
  }
  public void setProtocol(String protocol){
    this.protocol = protocol;
  }

  private long timeStamp;
  public long getTimeStamp(){
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp){
    this.timeStamp=timeStamp;
  }

  private Map publishedItemHash = new HashMap();
  public Map getPublishedItemHash(){
    if (this.publishedItemHash.isEmpty()){
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      this.publishedItemHash = pubService.preparePublishedItemHash(getPublishedAssessment());
    }
    return this.publishedItemHash;
  }

  public void setPublishedItemHash(Map publishedItemHash){
    this.publishedItemHash = publishedItemHash;
  }

  private Map publishedItemTextHash = new HashMap();
  public Map getPublishedItemTextHash(){
    if (this.publishedItemTextHash.isEmpty()){
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      this.publishedItemTextHash = pubService.preparePublishedItemTextHash(getPublishedAssessment());
    }
    return this.publishedItemTextHash;
  }

  public void setPublishedItemTextHash(Map publishedItemTextHash){
    this.publishedItemTextHash = publishedItemTextHash;
  }

  private Map publishedAnswerHash = new HashMap();
  public Map getPublishedAnswerHash(){
    if (this.publishedAnswerHash.isEmpty()){
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      this.publishedAnswerHash = pubService.preparePublishedAnswerHash(getPublishedAssessment());
    }
    return this.publishedAnswerHash;
  }

   public void setPublishedAnswerHash(Map publishedAnswerHash){
    this.publishedAnswerHash = publishedAnswerHash;
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
	   }
	   else {
		   log.debug("isMoreThanOneQuestion set to true");
		   isMoreThanOneQuestion = true;   
	   }
	   return isMoreThanOneQuestion;
   }

  private List attachmentList;
  public List getAttachmentList() {
    return attachmentList;
  }

  public void setAttachmentList(List attachmentList)
  {
    this.attachmentList = attachmentList;
  }

  private boolean hasAttachment = false;
  public boolean getHasAttachment(){
    boolean hasAttachment = false;
    if (attachmentList!=null && attachmentList.size() >0){
        hasAttachment = true;
    }
    return hasAttachment;
  }
 
  public boolean getNoQuestions() {
	  return noQuestions;
  }

  public void setNoQuestions(boolean noQuestions)
  {
	  this.noQuestions = noQuestions;
  }
  
  public String checkBeforeProceed(boolean isSubmitForGrade, boolean isFromTimer, boolean isViaUrlLogin){
    // public method, who know if publishedAssessment is set, so check
    // to be sure
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
    		}
    		else {
    			log.debug("take from bean: actualNumberRetake ={}", actualNumberRetake);
    			// Not during a Retake
    			if (actualNumberRetake == numberRetake) {
    				return "noLateSubmission";
    			}
    			// During a Retake
    			else if (actualNumberRetake == numberRetake - 1) {
    				log.debug("actualNumberRetake == numberRetake - 1: through Retake");
    			}
    			// Should not come to here
    			else {
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
    				}
    				// If No Late, this is a timed assessment, and not during a Retake, go through (see above reason)
    				else if (this.isTimedAssessment()) {
    					log.debug("No Late Submission && timedAssessment"); 
    				}
    				else {
    					log.debug("noLateSubmission");
    					return "noLateSubmission";
    				}
    			}
    			// During a Retake
    			else if (actualNumberRetake == numberRetake - 1) {
    				log.debug("actualNumberRetake == numberRetake - 1: through Retake");
    			}
    			// Should not come to here
    			else {
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
	  return checkBeforeProceed(isSubmitForGrade, isFromTimer, false);
  }

  private boolean getHasSubmissionLeft(int numberRetake){
    boolean hasSubmissionLeft = false;
    int maxSubmissionsAllowed = 9999;
    if ( (Boolean.FALSE).equals(publishedAssessment.getAssessmentAccessControl().getUnlimitedSubmissions())){
      maxSubmissionsAllowed = publishedAssessment.getAssessmentAccessControl().getSubmissionsAllowed();
      if ("takeAssessmentViaUrl".equals(actionString) && !anonymousLogin && settings == null) {
    	  SettingsDeliveryBean settingsDeliveryBean = new SettingsDeliveryBean();
    	  settingsDeliveryBean.setAssessmentAccessControl(publishedAssessment);
    	  settingsDeliveryBean.setMaxAttempts(maxSubmissionsAllowed);
    	  settings = settingsDeliveryBean; 
      }
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
    boolean pastDue = true;
    Date currentDate = new Date();
		Date dueDate;
		if (extendedTimeDeliveryService.hasExtendedTime()) {
			dueDate = extendedTimeDeliveryService.getDueDate();
		} else {
			dueDate = publishedAssessment.getAssessmentAccessControl().getDueDate();
		}
    if (dueDate == null || dueDate.after(currentDate)){
        pastDue = false;
    }
    return pastDue;
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
    }
    else if (acceptLateSubmission) {
    	retractDate = publishedAssessment.getAssessmentAccessControl().getRetractDate();
    }
    if (retractDate == null || retractDate.after(currentDate)){
        isRetracted = false;
    }
    return isRetracted;
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
        }
        else {
        	return true;
        }
      }
      catch(Exception e){
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
	  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	  String currentSiteId = publishedAssessmentService.getPublishedAssessmentSiteId(getAssessmentId());
	  url.append(currentSiteId);
	  url.append("/page/");
	  url.append(getCurrentPageId(currentSiteId));
	  return url.toString();
  }

  public String getTimerId()
  {
      return timerId;
  }

  public void setTimerId(String timerId)
  {
    this.timerId = timerId;
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
		}
		else {
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

	  public Long getAssessmentGradingId()
	  {
	      return assessmentGradingId;
	  }

	  public void setFromTableOfContents(boolean fromTableOfContents)
	  {
	    this.fromTableOfContents = fromTableOfContents;
	  }
	  
	  public boolean getFromTableOfContents()
	  {
	      return fromTableOfContents;
	  }

	  public void setAssessmentGradingId(Long assessmentGradingId)
	  {
	    this.assessmentGradingId = assessmentGradingId;
	  }

	  public String updateTimeLimit(String timeLimit) {  
  	    if (numberRetake == -1 || actualNumberRetake == -1) {
		    	GradingService gradingService = new GradingService();
		    	numberRetake = gradingService.getNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString());
		    	log.debug("numberRetake = {}", numberRetake);
		    	actualNumberRetake = gradingService.getActualNumberRetake(publishedAssessment.getPublishedAssessmentId(), AgentFacade.getAgentString());
		    	log.debug("actualNumberRetake ={}", actualNumberRetake);
		 }
		
		if (!("previewAssessment").equals(actionString) && actualNumberRetake >= numberRetake && beginTime != null) { 
			timeLimit = getTimeBeforeDueRetract(timeLimit);
		}
		
		return timeLimit;
	  }
	  
	  public String getDeadlineString() {
		  String deadlineString = "";
		    if (deadline == null) {
		      return deadlineString;
		    }

		    try {
		      TimeUtil tu = new TimeUtil();
		      deadlineString = tu.getDisplayDateTime(dayDisplayFormat, deadline, true);
		    }
		    catch (Exception ex) {
		      // we will leave it as an empty string
		      log.warn("Unable to format date.", ex);
		    }
		    return deadlineString;
	  }
	  
	  public Date getDeadline() {
		  return deadline;
	  }
	  
	  public void setDeadline() {
		  if (this.firstTimeTaking) {
			  boolean acceptLateSubmission = isAcceptLateSubmission();

			  if (dueDate != null) {
				  if (!acceptLateSubmission) {
					  deadline = dueDate;
				  }
				  else {
					  if (totalSubmissions > 0) {
						  deadline = dueDate;
					  }
					  else {
						  if (retractDate != null) {
							  deadline = retractDate;
						  }
					  }
				  }
			  }
			  else {
				  if (retractDate != null) {
					  deadline = retractDate;
				  }
			  }
		  }
		  else {
			  if (dueDate != null) {
				  deadline = dueDate;
			  }
			  else {
				  if (retractDate != null) {
					  deadline = retractDate;
				  }
				  else {
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
			  }
			  else {
				  if (totalSubmissions > 0) {
					  finalTimeLimit = getTimeBeforeDue(timeLimit);
				  }
				  else {
					  if (retractDate != null) {
						  finalTimeLimit = getTimeBeforeRetract(timeLimit);
					  }
				  }
			  }
		  }
		  else {
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
		  }
		  else {
			  int timeBeforeDue  = Math.round((dueDate.getTime() - System.currentTimeMillis())/1000.0f);
			  timeLimit = String.valueOf(timeBeforeDue);
		  }
		  
		  return timeLimit;
	  }
	  
	  private String getTimeBeforeRetract(String timeLimit) {
		  
		  if (timeLimit != null && Integer.parseInt(timeLimit) > 0 && beginTime != null) {
			  int timeBeforeRetract  = Math.round((retractDate.getTime() - beginTime.getTime())/1000.0f);
			  if (timeBeforeRetract < Integer.parseInt(timeLimit)) {
				  timeLimit = String.valueOf(timeBeforeRetract);
			  }
		  }
		  else {
			  int timeBeforeRetract  = Math.round((retractDate.getTime() - System.currentTimeMillis())/1000.0f);
			  timeLimit = String.valueOf(timeBeforeRetract);
		  }
		  return timeLimit;
	  }
	  
	  
	  private boolean isTimedAssessment() {
		  return !this.getPublishedAssessment().getAssessmentAccessControl().getTimeLimit().equals(0);
	  }
	  
	  public String cleanRadioButton() {

		  // We get the id of the question
		  String radioId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("radioId");
		  StringBuilder redrawAnchorName = new StringBuilder("p");
		  String tmpAnchorName = "";
		  List parts = this.pageContents.getPartsContents();

		  for (int i=0; i<parts.size(); i++) {
			  SectionContentsBean sectionContentsBean = (SectionContentsBean) parts.get(i);
			  String partSeq = sectionContentsBean.getNumber();
			  
			  List items = sectionContentsBean.getItemContents();
			  for (int j=0; j<items.size(); j++) {
				  ItemContentsBean item = (ItemContentsBean)items.get(j);
				  
				  //Just delete the checkbox of the current question
				  if (!item.getItemData().getItemId().toString().equals(radioId)) continue;

				  String itemSeq = item.getItemData().getSequence().toString();
				  redrawAnchorName.append(partSeq);
				  redrawAnchorName.append("q");
				  redrawAnchorName.append(itemSeq);
				  if (tmpAnchorName.equals("") || tmpAnchorName.compareToIgnoreCase(redrawAnchorName.toString()) > 0) {
					  tmpAnchorName = redrawAnchorName.toString();
				  }
				  
				  if (item.getItemData().getTypeId().longValue() == TypeIfc.MULTIPLE_CHOICE.longValue() || 
						  item.getItemData().getTypeId().longValue() == TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION.longValue() ||
						  item.getItemData().getTypeId().longValue() == TypeIfc.MULTIPLE_CHOICE_SURVEY.longValue() ||
						  item.getItemData().getTypeId().longValue() == TypeIfc.MATRIX_CHOICES_SURVEY.longValue()) {
					  item.setUnanswered(true);
					  if(item.getItemData().getTypeId().longValue() == TypeIfc.MATRIX_CHOICES_SURVEY.longValue()){
						  for (int k=0; k<item.getMatrixArray().size(); k++) {
							  MatrixSurveyBean selection = (MatrixSurveyBean)item.getMatrixArray().get(k);
							  selection.setResponseFromCleanRadioButton();
						  }
					  }else{
						  for (int k=0; k<item.getSelectionArray().size(); k++) {
							  SelectionBean selection = (SelectionBean)item.getSelectionArray().get(k);
							  //selection.setResponse(false);
							  selection.setResponseFromCleanRadioButton();
						  }
					  }
					  
					  List itemGradingData = new ArrayList();
					  for( ItemGradingData itemgrading : item.getItemGradingDataArray() ){
						  if (itemgrading.getItemGradingId() != null && itemgrading.getItemGradingId().intValue() > 0) {
							  itemGradingData.add(itemgrading);
							  itemgrading.setPublishedAnswerId(null);
						  }
					  }
					  item.setItemGradingDataArray(itemGradingData);
				  }

				  if (item.getItemData().getTypeId().longValue() == TypeIfc.TRUE_FALSE.longValue()) {
					  item.setResponseId(null);
					  Iterator iter = item.getItemGradingDataArray().iterator();
					  if (iter.hasNext())
					  {
						  ItemGradingData data = (ItemGradingData) iter.next();
						  data.setPublishedAnswerId(null);
					  }
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
		  return save_work();
	  }

	  /**
	   *
	   *
	   * @return
	   */
	  public boolean getDisplayMardForReview()
	  {
	    return this.displayMardForReview;
	  }

	  /**
	   *
	   * @param displayMardForReview
	   */
	  public void setDisplayMardForReview(boolean displayMardForReview)
	  {
	    this.displayMardForReview = displayMardForReview;
	  }

	  public Map getItemContentsMap()
	  {
	    return this.itemContentsMap;
	  }

	  public void setItemContentsMap(Map itemContentsMap)
	  {
	    this.itemContentsMap = itemContentsMap;
	  }
	  
	  public String getAutoSaveRepeatMilliseconds()
	  {
  	    String s = ServerConfigurationService.getString("samigo.autoSave.repeat.milliseconds");
  	    try {
  	    	Integer.parseInt(s);
  	    }
  	    catch (NumberFormatException ex) {
  	    	s = "-1";
  	    }
  	    log.debug("auto save every {} milliseconds", s);
	    return s;
	  }
	  
	  public void setFileUploadSizeMax(int fileUploadSizeMax)
	  {
	    this.fileUploadSizeMax = fileUploadSizeMax;
	  }
	  
	  public int getFileUploadSizeMax()
	  {
	      return fileUploadSizeMax;
	  }	  

	  public boolean getStudentRichText()
	  {
	      String studentRichText = ServerConfigurationService.getString("samigo.studentRichText", "true");
		  return Boolean.parseBoolean(studentRichText);
	  } 

	  public void setDisplayFormat()
	  {
		  display_dateFormat= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_date_no_sec");
		  displayFormat = new SimpleDateFormat(display_dateFormat, new ResourceLoader().getLocale());
	  }

	  public Integer getScoringType()
	  {
	      return scoringType;
	  }

	  public void setScoringType(Integer scoringType)
	  {
	    this.scoringType = scoringType;
	  }

	  public String getSecureDeliveryHTMLFragment() {
		  return this.secureDeliveryHTMLFragment;
	  }

	  public void setSecureDeliveryHTMLFragment(String secureDeliveryHTMLFragment) {
		  this.secureDeliveryHTMLFragment = secureDeliveryHTMLFragment;
	  }
	  public boolean isBlockDelivery() {
		  return blockDelivery;
	  }
	  public void setBlockDelivery(boolean blockDelivery) {
		  this.blockDelivery = blockDelivery;
	  }

	  
	  public boolean getIsFromPrint()
	  {
	    return isFromPrint;
	  }

	  public void setIsFromPrint(boolean isFromPrint)
	  {
	    this.isFromPrint = isFromPrint;
	  }
	  
	  public boolean getIsAnyInvalidFinInput()
	  {
	  	return isAnyInvalidFinInput;
	  }
	  
	  public void setIsAnyInvalidFinInput(boolean isAnyInvalidFinInput)
	  {
		  this.isAnyInvalidFinInput = isAnyInvalidFinInput;
	  }
	  
	  public String getRedrawAnchorName()
	  {
	    return redrawAnchorName;
	  }

	  public void setRedrawAnchorName(String redrawAnchorName)
	  {
	    this.redrawAnchorName = redrawAnchorName;
	  }

	public boolean isHonorPledge() {
		return honorPledge;
	}

	public void setHonorPledge(boolean honorPledge) {
		this.honorPledge = honorPledge;
	}
	 
	  public String getRecURL()
	  {
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

	  public void setNumberRetake(int numberRetake) {
		  this.numberRetake = numberRetake;
	  }
	  
	  public int getNumberRetake() {
		 return numberRetake;
	  }
	  
	  public void setActualNumberRetake(int actualNumberRetake) {
		  this.actualNumberRetake = actualNumberRetake;
	  }
	  
	  public int getActualNumberRetake() {
		  return actualNumberRetake;
	  }
	  public boolean getShowTimeWarning()
	  {
	    return showTimeWarning;
	  }

	  public void setShowTimeWarning(boolean showTimeWarning)
	  {
	    this.showTimeWarning = showTimeWarning;
	  }
	  
	  public boolean getHasShowTimeWarning()
	  {
	    return hasShowTimeWarning;
	  }

	  public void setHasShowTimeWarning(boolean hasShowTimeWarning)
	  {
	    this.hasShowTimeWarning = hasShowTimeWarning;
	  }
	  
	  public boolean getTurnIntoTimedAssessment()
	  {
	    return turnIntoTimedAssessment;
	  }

	  public void setTurnIntoTimedAssessment(boolean turnIntoTimedAssessment)
	  {
	    this.turnIntoTimedAssessment = turnIntoTimedAssessment;
	  }

	  public boolean getsubmitFromTimeoutPopup() {
		  return submitFromTimeoutPopup;
	  }

	  public void setSubmitFromTimeoutPopup(boolean submitFromTimeoutPopup) {
		  this.submitFromTimeoutPopup = submitFromTimeoutPopup;
	  }
	  
	  public void setSkipFlag(boolean skipFlag) {
		  this.skipFlag = skipFlag;
	  }
	  
	  public boolean getSkipFlag() {
		  return skipFlag;
	  }
	  
	  public void setFirstTimeTaking(boolean firstTimeTaking) {
		  this.firstTimeTaking = firstTimeTaking;
	  }
	  
	  public boolean getFirstTimeTaking() {
		  return firstTimeTaking;
	  }
	  //SAM-2517
	  public boolean getIsMathJaxEnabled(){ 
		  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		  String siteId = publishedAssessmentService.getPublishedAssessmentOwner(Long.parseLong(getAssessmentId()));
		  String strMathJaxEnabled = getCurrentSite(siteId).getProperties().getProperty(MATHJAX_ENABLED); 
		  return StringUtils.contains(strMathJaxEnabled, "sakai.samigo");
	  }
	  public String getMathJaxHeader(){
		  StringBuilder headMJ = new StringBuilder();
		  headMJ.append("<script type=\"text/x-mathjax-config\">\nMathJax.Hub.Config({\ntex2jax: { inlineMath: [['$$','$$'],['\\\\(','\\\\)']] }, TeX: { equationNumbers: { autoNumber: 'AMS' } }\n});\n</script>\n");
		  headMJ.append("<script src=\"").append(MATHJAX_SRC_PATH).append("\"  language=\"JavaScript\" type=\"text/javascript\"></script>\n");
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
}
